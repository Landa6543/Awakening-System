package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Guardián de las Ruinas (Ruin Guard / Field Tiller).
 * Autómata de guerra biomecánico de Khaenri'ah armado con misiles guiados,
 * ataque giratorio de torbellino devastador y un núcleo ocular vulnerable.
 */
public class RuinGuardEntity extends Monster {

    private static final EntityDataAccessor<Boolean> SPINNING =
            SynchedEntityData.defineId(RuinGuardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED =
            SynchedEntityData.defineId(RuinGuardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FIRING_MISSILES =
            SynchedEntityData.defineId(RuinGuardEntity.class, EntityDataSerializers.BOOLEAN);

    private int spinCooldown = 160;
    private int missileCooldown = 220;
    private int stunTimer = 0;

    public RuinGuardEntity(EntityType<? extends RuinGuardEntity> type, Level level) {
        super(type, level);
        this.xpReward = 45;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPINNING, false);
        builder.define(STUNNED, false);
        builder.define(FIRING_MISSILES, false);
    }

    public boolean isSpinning() {
        return this.entityData.get(SPINNING);
    }

    public void setSpinning(boolean spinning) {
        this.entityData.set(SPINNING, spinning);
    }

    public boolean isStunned() {
        return this.entityData.get(STUNNED);
    }

    public void setStunned(boolean stunned) {
        this.entityData.set(STUNNED, stunned);
    }

    public boolean isFiringMissiles() {
        return this.entityData.get(FIRING_MISSILES);
    }

    public void setFiringMissiles(boolean firing) {
        this.entityData.set(FIRING_MISSILES, firing);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RuinGuardSpinGoal(this));
        this.goalSelector.addGoal(2, new RuinGuardMissilesGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.spinCooldown > 0) this.spinCooldown--;
        if (this.missileCooldown > 0) this.missileCooldown--;

        if (isStunned()) {
            this.stunTimer--;
            if (this.stunTimer <= 0) {
                setStunned(false);
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 1.2F);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile && !isStunned()) {
            Vec3 hitPos = source.getSourcePosition();
            if (hitPos != null && hitPos.y > this.getY() + this.getEyeHeight() * 0.75D) {
                if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.2F, 1.8F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getEyeY(), this.getZ(),
                            25, 0.3, 0.3, 0.3, 0.15);

                    setStunned(true);
                    setSpinning(false);
                    setFiringMissiles(false);
                    this.stunTimer = 120;

                    if (source.getEntity() instanceof Player player) {
                        player.displayClientMessage(Component.translatable("boss.awakening_system.ruin_guard.weakpoint"), true);
                    }
                }
                return super.hurt(source, amount * 2.2F);
            }
        }

        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.8F);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.CHAOS_DEVICE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_DEVICE.get(), 2 + this.random.nextInt(3)));
        }
        if (SystemItems.CHAOS_CIRCUIT.isBound() && this.random.nextFloat() < 0.70F) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CIRCUIT.get(), 1 + this.random.nextInt(2)));
        }
        if (SystemItems.CHAOS_CORE.isBound() && this.random.nextFloat() < 0.35F) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), 1));
        }
        this.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 2 + this.random.nextInt(3)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 0.8F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(),
                    1, 0, 0, 0, 0);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 900.0D)) {
                if (p instanceof ServerPlayer sp) {
                    var data = SystemEvents.data(sp);
                    if (isTeyvat) {
                        int moraGain = 4000 + sl.random.nextInt(6000);
                        int primoGain = 10 + sl.random.nextInt(15);
                        data.addMora(moraGain);
                        data.addPrimogems(primoGain);
                        com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                        sp.displayClientMessage(Component.translatable("boss.awakening_system.ruin_guard.defeated", moraGain, primoGain), false);
                    } else {
                        if (SystemItems.AWAKENING_COIN.isBound()) {
                            sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 4));
                        }
                    }
                }
            }
        }
    }

    static class RuinGuardSpinGoal extends Goal {
        private final RuinGuardEntity guard;
        private int spinTicks = 0;

        public RuinGuardSpinGoal(RuinGuardEntity guard) {
            this.guard = guard;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.guard.getTarget();
            return target != null && target.isAlive() && !this.guard.isStunned() && this.guard.spinCooldown <= 0
                    && this.guard.distanceToSqr(target) < 144.0D;
        }

        @Override
        public void start() {
            this.guard.setSpinning(true);
            this.spinTicks = 100;
            this.guard.spinCooldown = 280;
            this.guard.level().playSound(null, this.guard.getX(), this.guard.getY(), this.guard.getZ(),
                    SoundEvents.MINECART_RIDING, SoundSource.HOSTILE, 1.8F, 0.5F);
        }

        @Override
        public void tick() {
            this.spinTicks--;
            LivingEntity target = this.guard.getTarget();
            if (target != null) {
                this.guard.getNavigation().moveTo(target, 1.35D);
            }

            if (this.spinTicks % 10 == 0 && this.guard.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.guard.getX(), this.guard.getY(), this.guard.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.4F, 0.7F);
                List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class, this.guard.getBoundingBox().inflate(2.2D));
                for (LivingEntity v : victims) {
                    if (v != this.guard) {
                        v.hurt(this.guard.damageSources().mobAttack(this.guard), 12.0F);
                        Vec3 kb = v.position().subtract(this.guard.position()).normalize().scale(0.8D);
                        v.setDeltaMovement(kb.x, 0.4D, kb.z);
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.spinTicks > 0 && !this.guard.isStunned();
        }

        @Override
        public void stop() {
            this.guard.setSpinning(false);
        }
    }

    static class RuinGuardMissilesGoal extends Goal {
        private final RuinGuardEntity guard;
        private int warmUp = 0;

        public RuinGuardMissilesGoal(RuinGuardEntity guard) {
            this.guard = guard;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.guard.getTarget();
            return target != null && target.isAlive() && !this.guard.isStunned() && !this.guard.isSpinning()
                    && this.guard.missileCooldown <= 0 && this.guard.distanceToSqr(target) > 64.0D;
        }

        @Override
        public void start() {
            this.guard.setFiringMissiles(true);
            this.warmUp = 25;
            this.guard.missileCooldown = 260;
            this.guard.getNavigation().stop();
            this.guard.level().playSound(null, this.guard.getX(), this.guard.getY(), this.guard.getZ(),
                    SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.5F, 0.6F);
        }

        @Override
        public void tick() {
            LivingEntity target = this.guard.getTarget();
            if (target != null) {
                this.guard.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            this.warmUp--;
            if (this.warmUp == 0 && target != null && !this.guard.level().isClientSide) {
                for (int i = 0; i < 6; i++) {
                    RuinMissileEntity missile = new RuinMissileEntity(this.guard.level(), this.guard, target);
                    double ox = (this.guard.getRandom().nextDouble() - 0.5D) * 1.5D;
                    double oz = (this.guard.getRandom().nextDouble() - 0.5D) * 1.5D;
                    missile.setPos(this.guard.getX() + ox, this.guard.getY() + 2.8D, this.guard.getZ() + oz);
                    double upwardSpread = 0.5D + this.guard.getRandom().nextDouble() * 0.35D;
                    missile.setDeltaMovement(ox * 0.25D, upwardSpread, oz * 0.25D);
                    this.guard.level().addFreshEntity(missile);
                }
                this.guard.level().playSound(null, this.guard.getX(), this.guard.getY(), this.guard.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 2.0F, 0.8F);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.warmUp > -10 && !this.guard.isStunned();
        }

        @Override
        public void stop() {
            this.guard.setFiringMissiles(false);
        }
    }
}
