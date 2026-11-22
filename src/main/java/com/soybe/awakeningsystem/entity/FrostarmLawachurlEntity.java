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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Frostarm Lawachurl (Cacique Mitachurl Acorazado de Hielo de Espinadragón).
 * Monstruo colosal acorazado con manto de hielo Cryo, saltos destructores y ráfagas de escarcha.
 */
public class FrostarmLawachurlEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_SHIELDED =
            SynchedEntityData.defineId(FrostarmLawachurlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LEAPING =
            SynchedEntityData.defineId(FrostarmLawachurlEntity.class, EntityDataSerializers.BOOLEAN);

    private float cryoShield = 130.0F;
    private int slamCooldown = 180;

    public FrostarmLawachurlEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§b§lFrostarm Lawachurl"));
        this.setCustomNameVisible(true);
        this.xpReward = 280;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 340.0D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 30.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHIELDED, true);
        builder.define(LEAPING, false);
    }

    public boolean isLeaping() {
        return this.entityData.get(LEAPING);
    }

    public void setLeaping(boolean leaping) {
        this.entityData.set(LEAPING, leaping);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FrostarmSlamGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean hasCryoShield() {
        return this.entityData.get(DATA_SHIELDED);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
    }

    static class FrostarmSlamGoal extends Goal {
        private final FrostarmLawachurlEntity lawachurl;
        private int leapTicks = 0;

        public FrostarmSlamGoal(FrostarmLawachurlEntity lawachurl) {
            this.lawachurl = lawachurl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.lawachurl.getTarget();
            return target != null && target.isAlive() && this.lawachurl.slamCooldown <= 0
                    && this.lawachurl.distanceToSqr(target) > 25.0D && this.lawachurl.distanceToSqr(target) < 225.0D
                    && this.lawachurl.onGround();
        }

        @Override
        public void start() {
            this.lawachurl.setLeaping(true);
            this.leapTicks = 40;
            this.lawachurl.slamCooldown = 200;

            LivingEntity target = this.lawachurl.getTarget();
            if (target != null) {
                Vec3 dir = target.position().subtract(this.lawachurl.position()).normalize();
                this.lawachurl.setDeltaMovement(dir.x * 1.15D, 0.95D, dir.z * 1.15D);
                this.lawachurl.hasImpulse = true;
            }

            this.lawachurl.level().playSound(null, this.lawachurl.getX(), this.lawachurl.getY(), this.lawachurl.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.8F, 0.8F);
        }

        @Override
        public void tick() {
            this.leapTicks--;

            if (this.leapTicks < 30 && this.lawachurl.onGround() && !this.lawachurl.level().isClientSide) {
                ServerLevel sl = (ServerLevel) this.lawachurl.level();
                sl.playSound(null, this.lawachurl.getX(), this.lawachurl.getY(), this.lawachurl.getZ(),
                        SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 1.2F);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, this.lawachurl.getX(), this.lawachurl.getY() + 0.5D, this.lawachurl.getZ(),
                        60, 2.0D, 0.5D, 2.0D, 0.15D);

                AABB area = this.lawachurl.getBoundingBox().inflate(4.0D, 1.5D, 4.0D);
                for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != this.lawachurl && e.isAlive())) {
                    nearby.hurt(this.lawachurl.damageSources().mobAttack(this.lawachurl), 28.0F);
                    nearby.knockback(1.5D, nearby.getX() - this.lawachurl.getX(), nearby.getZ() - this.lawachurl.getZ());
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
                this.leapTicks = 0;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.leapTicks > 0;
        }

        @Override
        public void stop() {
            this.lawachurl.setLeaping(false);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && hasCryoShield()) {
            boolean pyro = source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
            this.cryoShield -= pyro ? amount * 2.5F : amount * 0.4F;

            if (this.cryoShield <= 0.0F) {
                this.cryoShield = 0.0F;
                this.entityData.set(DATA_SHIELDED, false);
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
                }
            }
            amount *= 0.4F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_DEATH, SoundSource.HOSTILE, 2.0F, 0.8F);
            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 900.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(12000);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 6));
                    }
                }
            }
            if (isTeyvat && SystemItems.PRIMOGEM.get() != null) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 5 + random.nextInt(5)));
            }
        }
    }
}
