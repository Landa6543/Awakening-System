package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Cazador de las Ruinas (Ruin Hunter).
 * Autómata de guerra biomecánico de Khaenri'ah de vuelo y combate aéreo.
 * - Levita continuamente propulsado por motores gravitatorios arcanos.
 * - Combate cuerpo a cuerpo veloz: embestida cortante con cuchillas gemelas y torbellino giratorio de 360°.
 * - Modo Bombardeo Aéreo (Artillery Mode): Si el jugador se eleva o toma distancia, asciende a gran altura
 *   y lanza salvas continuas de misiles teledirigidos.
 * - Mecánica del Punto Débil: Durante el bombardeo aéreo, acertar al núcleo ocular con un arco
 *   hace que el Cazador se sobrecargue, se desplome al suelo y quede aturdido durante 7 segundos.
 */
public class RuinHunterEntity extends Monster {

    private static final EntityDataAccessor<Boolean> SPINNING =
            SynchedEntityData.defineId(RuinHunterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOMBARDING =
            SynchedEntityData.defineId(RuinHunterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DASHING =
            SynchedEntityData.defineId(RuinHunterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED =
            SynchedEntityData.defineId(RuinHunterEntity.class, EntityDataSerializers.BOOLEAN);

    private int spinCooldown = 180;
    private int bombardCooldown = 220;
    private int dashCooldown = 140;
    private int stunTimer = 0;

    public RuinHunterEntity(EntityType<? extends RuinHunterEntity> type, Level level) {
        super(type, level);
        this.xpReward = 55;
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 360.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.FOLLOW_RANGE, 42.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPINNING, false);
        builder.define(BOMBARDING, false);
        builder.define(DASHING, false);
        builder.define(STUNNED, false);
    }

    public boolean isSpinning() {
        return this.entityData.get(SPINNING);
    }

    public void setSpinning(boolean spinning) {
        this.entityData.set(SPINNING, spinning);
    }

    public boolean isBombarding() {
        return this.entityData.get(BOMBARDING);
    }

    public void setBombarding(boolean bombarding) {
        this.entityData.set(BOMBARDING, bombarding);
    }

    public boolean isDashing() {
        return this.entityData.get(DASHING);
    }

    public void setDashing(boolean dashing) {
        this.entityData.set(DASHING, dashing);
    }

    public boolean isStunned() {
        return this.entityData.get(STUNNED);
    }

    public void setStunned(boolean stunned) {
        this.entityData.set(STUNNED, stunned);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RuinHunterBombardGoal(this));
        this.goalSelector.addGoal(2, new RuinHunterDashGoal(this));
        this.goalSelector.addGoal(3, new RuinHunterSpinGoal(this));
        this.goalSelector.addGoal(4, new RuinHunterHoverAttackGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isStunned()) {
            this.setDeltaMovement(this.getDeltaMovement().x * 0.75D, -0.45D, this.getDeltaMovement().z * 0.75D);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            } else {
                this.moveRelative(0.045F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            }
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide && !this.isStunned()) {
            double px = this.getX();
            double py = this.getY() + 0.6D;
            double pz = this.getZ();
            if (this.random.nextFloat() < 0.65F) {
                this.level().addParticle(ParticleTypes.SMOKE, px + (this.random.nextDouble() - 0.5D) * 0.8D, py, pz + (this.random.nextDouble() - 0.5D) * 0.8D, 0.0D, -0.05D, 0.0D);
            }
            if (this.random.nextFloat() < 0.35F) {
                this.level().addParticle(ParticleTypes.SMALL_FLAME, px + (this.random.nextDouble() - 0.5D) * 0.6D, py, pz + (this.random.nextDouble() - 0.5D) * 0.6D, 0.0D, -0.08D, 0.0D);
            }
            if (this.isBombarding()) {
                this.level().addParticle(ParticleTypes.FLAME, px, py + 1.6D, pz, 0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.isStunned()) {
            this.stunTimer--;
            this.getNavigation().stop();
            if (this.stunTimer <= 0) {
                this.setStunned(false);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 1.8F, 1.2F);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 1.6F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.2D, this.getZ(), 4, 0.4D, 0.4D, 0.4D, 0.05D);
                }
                this.setDeltaMovement(0.0D, 0.65D, 0.0D);
            }
            return;
        }

        int surfaceY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, this.getBlockX(), this.getBlockZ());
        LivingEntity target = this.getTarget();

        double desiredHeight;
        if (this.isBombarding()) {
            double baseTarget = target != null ? target.getY() + 6.0D : surfaceY + 7.5D;
            desiredHeight = Math.max(surfaceY + 6.5D, baseTarget);
        } else {
            desiredHeight = surfaceY + 2.0D;
        }

        double diffY = desiredHeight - this.getY();
        if (diffY > 0.4D) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.06D, 0.0D));
        } else if (diffY < -0.4D && !this.isBombarding()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.05D, 0.0D));
        }

        if (this.spinCooldown > 0) this.spinCooldown--;
        if (this.bombardCooldown > 0) this.bombardCooldown--;
        if (this.dashCooldown > 0) this.dashCooldown--;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile projectile) {
            if (this.isBombarding()) {
                this.setBombarding(false);
                this.setStunned(true);
                this.stunTimer = 140;

                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0F, 0.85F);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 2.2F, 0.5F);

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.8D, this.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.0D);
                    sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.8D, this.getZ(), 35, 0.6D, 0.6D, 0.6D, 0.25D);
                }

                if (source.getEntity() instanceof Player player) {
                    player.displayClientMessage(Component.translatable("boss.awakening_system.ruin_hunter.weakpoint"), true);
                }
                return super.hurt(source, amount * 2.8F);
            }
        }

        if (this.isStunned()) {
            return super.hurt(source, amount * 1.5F);
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
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        if (SystemItems.CHAOS_DEVICE.isBound()) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_DEVICE.get(), 3 + this.random.nextInt(4)));
        }
        if (SystemItems.CHAOS_CIRCUIT.isBound() && this.random.nextFloat() < 0.85F) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CIRCUIT.get(), 2 + this.random.nextInt(3)));
        }
        if (SystemItems.CHAOS_CORE.isBound() && this.random.nextFloat() < 0.50F) {
            this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), 1 + this.random.nextInt(2)));
        }
        this.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 2 + this.random.nextInt(4)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.8F, 0.9F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(),
                    2, 0.2D, 0.2D, 0.2D, 0.0D);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (Player p : sl.getPlayers(pl -> pl.distanceToSqr(this) < 1200.0D)) {
                if (p instanceof ServerPlayer sp) {
                    var data = SystemEvents.data(sp);
                    if (isTeyvat) {
                        int moraGain = 6000 + sl.random.nextInt(8000);
                        int primoGain = 15 + sl.random.nextInt(20);
                        data.addMora(moraGain);
                        data.addPrimogems(primoGain);
                        AwakeningSystemMod.sync(sp);
                        sp.displayClientMessage(Component.translatable("boss.awakening_system.ruin_hunter.defeated", moraGain, primoGain), false);
                    } else {
                        if (SystemItems.AWAKENING_COIN.isBound()) {
                            sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 6));
                        }
                    }
                }
            }
        }
    }

    static class RuinHunterBombardGoal extends Goal {
        private final RuinHunterEntity hunter;
        private int bombardTicks = 0;

        public RuinHunterBombardGoal(RuinHunterEntity hunter) {
            this.hunter = hunter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hunter.getTarget();
            if (target == null || !target.isAlive() || this.hunter.isStunned() || this.hunter.isSpinning() || this.hunter.isDashing()) {
                return false;
            }
            boolean targetElevated = target.getY() > this.hunter.getY() + 1.8D;
            boolean longRangeTrigger = this.hunter.bombardCooldown <= 0 && this.hunter.distanceToSqr(target) > 64.0D;

            return targetElevated || longRangeTrigger;
        }

        @Override
        public void start() {
            this.hunter.setBombarding(true);
            this.bombardTicks = 120;
            this.hunter.bombardCooldown = 300;
            this.hunter.level().playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 2.0F, 0.5F);
        }

        @Override
        public void tick() {
            LivingEntity target = this.hunter.getTarget();
            if (target != null) {
                this.hunter.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            this.bombardTicks--;

            if (this.bombardTicks % 22 == 0 && target != null && !this.hunter.level().isClientSide) {
                for (int i = 0; i < 4; i++) {
                    RuinMissileEntity missile = new RuinMissileEntity(this.hunter.level(), this.hunter, target);
                    double ox = (this.hunter.getRandom().nextDouble() - 0.5D) * 1.6D;
                    double oz = (this.hunter.getRandom().nextDouble() - 0.5D) * 1.6D;
                    missile.setPos(this.hunter.getX() + ox, this.hunter.getY() + 1.6D, this.hunter.getZ() + oz);
                    missile.setDeltaMovement(ox * 0.2D, 0.4D + this.hunter.getRandom().nextDouble() * 0.3D, oz * 0.2D);
                    this.hunter.level().addFreshEntity(missile);
                }
                this.hunter.level().playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                        SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.8F, 0.8F);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.bombardTicks > 0 && !this.hunter.isStunned() && this.hunter.getTarget() != null && this.hunter.getTarget().isAlive();
        }

        @Override
        public void stop() {
            this.hunter.setBombarding(false);
            LivingEntity target = this.hunter.getTarget();
            if (target != null && !this.hunter.isStunned()) {
                Vec3 diveDir = target.position().subtract(this.hunter.position()).normalize().scale(0.9D);
                this.hunter.setDeltaMovement(diveDir.x, -0.4D, diveDir.z);
            }
        }
    }

    static class RuinHunterDashGoal extends Goal {
        private final RuinHunterEntity hunter;
        private int dashTicks = 0;

        public RuinHunterDashGoal(RuinHunterEntity hunter) {
            this.hunter = hunter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hunter.getTarget();
            return target != null && target.isAlive() && !this.hunter.isStunned() && !this.hunter.isBombarding()
                    && !this.hunter.isSpinning() && this.hunter.dashCooldown <= 0
                    && this.hunter.distanceToSqr(target) >= 16.0D && this.hunter.distanceToSqr(target) <= 180.0D;
        }

        @Override
        public void start() {
            this.hunter.setDashing(true);
            this.dashTicks = 25;
            this.hunter.dashCooldown = 160;
            this.hunter.level().playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.7F);
        }

        @Override
        public void tick() {
            LivingEntity target = this.hunter.getTarget();
            if (target != null) {
                this.hunter.getLookControl().setLookAt(target, 40.0F, 40.0F);
                Vec3 dir = target.getEyePosition().subtract(this.hunter.position()).normalize().scale(0.85D);
                this.hunter.setDeltaMovement(dir.x, dir.y * 0.5D, dir.z);

                if (this.hunter.distanceToSqr(target) < 9.0D) {
                    target.hurt(this.hunter.damageSources().mobAttack(this.hunter), 16.0F);
                    target.setDeltaMovement(dir.x * 1.2D, 0.45D, dir.z * 1.2D);
                    this.hunter.level().playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                            SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.5F, 0.9F);
                    this.dashTicks = 0;
                }
            }
            this.dashTicks--;
        }

        @Override
        public boolean canContinueToUse() {
            return this.dashTicks > 0 && !this.hunter.isStunned();
        }

        @Override
        public void stop() {
            this.hunter.setDashing(false);
        }
    }

    static class RuinHunterSpinGoal extends Goal {
        private final RuinHunterEntity hunter;
        private int spinTicks = 0;

        public RuinHunterSpinGoal(RuinHunterEntity hunter) {
            this.hunter = hunter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hunter.getTarget();
            return target != null && target.isAlive() && !this.hunter.isStunned() && !this.hunter.isBombarding()
                    && !this.hunter.isDashing() && this.hunter.spinCooldown <= 0
                    && this.hunter.distanceToSqr(target) <= 36.0D;
        }

        @Override
        public void start() {
            this.hunter.setSpinning(true);
            this.spinTicks = 60;
            this.hunter.spinCooldown = 220;
            this.hunter.level().playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.5F);
        }

        @Override
        public void tick() {
            LivingEntity target = this.hunter.getTarget();
            if (target != null) {
                this.hunter.getLookControl().setLookAt(target, 20.0F, 20.0F);
                Vec3 dir = target.position().subtract(this.hunter.position()).normalize().scale(0.4D);
                this.hunter.setDeltaMovement(dir.x, dir.y * 0.3D, dir.z);
            }
            this.spinTicks--;

            if (this.spinTicks % 8 == 0 && this.hunter.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5F, 1.1F);
                List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class, this.hunter.getBoundingBox().inflate(1.8D));
                for (LivingEntity v : victims) {
                    if (v != this.hunter) {
                        v.hurt(this.hunter.damageSources().mobAttack(this.hunter), 10.0F);
                        Vec3 kb = v.position().subtract(this.hunter.position()).normalize().scale(0.75D);
                        v.setDeltaMovement(kb.x, 0.35D, kb.z);
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.spinTicks > 0 && !this.hunter.isStunned();
        }

        @Override
        public void stop() {
            this.hunter.setSpinning(false);
        }
    }

    static class RuinHunterHoverAttackGoal extends Goal {
        private final RuinHunterEntity hunter;

        public RuinHunterHoverAttackGoal(RuinHunterEntity hunter) {
            this.hunter = hunter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hunter.getTarget();
            return target != null && target.isAlive() && !this.hunter.isStunned() && !this.hunter.isBombarding()
                    && !this.hunter.isSpinning() && !this.hunter.isDashing();
        }

        @Override
        public void tick() {
            LivingEntity target = this.hunter.getTarget();
            if (target == null) return;

            this.hunter.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.hunter.distanceToSqr(target);

            if (distSq > 9.0D) {
                Vec3 dir = target.getEyePosition().subtract(this.hunter.position()).normalize().scale(0.32D);
                this.hunter.setDeltaMovement(dir.x, dir.y * 0.4D, dir.z);
            } else {
                if (this.hunter.attackAnim == 0) {
                    this.hunter.doHurtTarget(target);
                    this.hunter.level().playSound(null, this.hunter.getX(), this.hunter.getY(), this.hunter.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 0.9F);
                }
            }
        }
    }
}
