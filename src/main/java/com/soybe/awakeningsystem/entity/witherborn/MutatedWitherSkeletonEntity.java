package com.soybe.awakeningsystem.entity.witherborn;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Mutated Wither Skeleton (roadmap 14.9): a wither skeleton grown to three and a half blocks, its chest torn
 * open, the ribs arched apart, and in the hollow a floating core of dark energy that beats. Its blows wither.
 * Every few seconds, with a foe near, it throws its arms wide and the core swells and bursts: a wave of withering
 * force all round it, hurting and pushing back. Immune to fire and to wither. An elite of floors 23 and 24.
 */
public class MutatedWitherSkeletonEntity extends Monster {
    private static final EntityDataAccessor<Integer> PULSE = SynchedEntityData.defineId(MutatedWitherSkeletonEntity.class, EntityDataSerializers.INT);
    /** How long the swelling of the core lasts before it bursts. */
    public static final int PULSE_WINDUP = 24;
    private int pulseCooldown = 100;

    public MutatedWitherSkeletonEntity(EntityType<? extends MutatedWitherSkeletonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 40;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(PULSE, 0);
    }

    /** Ticks into the core's swelling (0 when it is not about to burst). */
    public int pulse() {
        return this.entityData.get(PULSE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false) {
            @Override
            public boolean canUse() { return pulse() == 0 && super.canUse(); }
            @Override
            public boolean canContinueToUse() { return pulse() == 0 && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) le.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1), this);
        return hit;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.WITHER && super.canBeAffected(effect);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int p = pulse();
        if (p > 0) {
            this.getNavigation().stop();
            if (p == 1) this.level().playSound(null, this, SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.2F, 0.6F);
            if (p >= PULSE_WINDUP) {
                burst();
                this.entityData.set(PULSE, 0);
                pulseCooldown = 160 + this.random.nextInt(80);
            } else this.entityData.set(PULSE, p + 1);
            return;
        }
        if (pulseCooldown > 0) pulseCooldown--;
        LivingEntity t = this.getTarget();
        if (pulseCooldown <= 0 && t != null && t.isAlive() && this.distanceToSqr(t) < 10 * 10) this.entityData.set(PULSE, 1);
    }

    /** The core bursts: withering force in a ring of six blocks, pushing everything away. */
    private void burst() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        Vec3 c = this.position().add(0, 2.2, 0);
        sl.playSound(null, this, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.4F, 0.7F);
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI * 2 / 48;
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x + Math.cos(a) * 1.2, c.y, c.z + Math.sin(a) * 1.2, 0, Math.cos(a), 0.05, Math.sin(a), 0.45);
            sl.sendParticles(ParticleTypes.SMOKE, c.x + Math.cos(a) * 1.6, c.y - 0.6, c.z + Math.sin(a) * 1.6, 0, Math.cos(a), 0.0, Math.sin(a), 0.3);
        }
        sl.sendParticles(ParticleTypes.SCULK_SOUL, c.x, c.y, c.z, 30, 0.4, 0.4, 0.4, 0.15);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0), e -> e != this && e.isAlive())) {
            if (e instanceof MutatedWitherSkeletonEntity) continue;
            e.hurt(this.damageSources().mobAttack(this), 10.0F);
            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1), this);
            Vec3 push = e.position().subtract(this.position()).multiply(1, 0, 1).normalize().scale(1.1);
            e.push(push.x, 0.35, push.z);
            e.hurtMarked = true;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide && this.random.nextInt(3) == 0) {
            // the core smoulders in the open chest
            double a = this.yBodyRot * Math.PI / 180;
            double fx = -Math.sin(a) * 0.25, fz = Math.cos(a) * 0.25;
            this.level().addParticle(pulse() > 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMOKE,
                    this.getX() + fx + (this.random.nextDouble() - 0.5) * 0.3, this.getY() + 2.3 + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + fz + (this.random.nextDouble() - 0.5) * 0.3, 0, 0.02, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.65F;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.6F, 0.7F);
    }
}
