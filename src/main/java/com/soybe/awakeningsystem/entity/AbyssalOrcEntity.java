package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
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

public class AbyssalOrcEntity extends Monster {
    public final AnimationState smashAnimationState = new AnimationState();
    public final AnimationState roarAnimationState = new AnimationState();
    public int smashAnimationTimeout = 0;
    public int roarAnimationTimeout = 0;
    public int bloodFrenzyTicks = 0;
    private int roarCooldown = 120;

    public AbyssalOrcEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 45;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy)) {
                    this.resetAttackCooldown();
                    this.mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.level().broadcastEntityEvent(this, (byte) 5); // Smash animation (Ground Slam)
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.3F, 0.75F);

            // Robo de vida durante Frenesí de Sangre
            if (bloodFrenzyTicks > 0) {
                this.heal(8.0F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, this.getX(), this.getY() + 1.2D, this.getZ(), 4, 0.2D, 0.3D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.05D);
                }
            }

            // Habilidad Mágica 1: Fisura Sísmica en Línea Recta (Abyssal Ground Fissure)
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                Vec3 dir = target.position().subtract(this.position()).normalize();
                for (double step = 1.5D; step <= 9.0D; step += 1.5D) {
                    double fx = this.getX() + dir.x * step;
                    double fy = this.getY();
                    double fz = this.getZ() + dir.z * step;

                    sl.sendParticles(ParticleTypes.EXPLOSION, fx, fy + 0.1D, fz, 2, 0.2D, 0.1D, 0.2D, 0.02D);
                    sl.sendParticles(ParticleTypes.LAVA, fx, fy + 0.2D, fz, 4, 0.2D, 0.3D, 0.2D, 0.0D);

                    var box = new net.minecraft.world.phys.AABB(fx - 1.2D, fy - 0.5D, fz - 1.2D, fx + 1.2D, fy + 2.0D, fz + 1.2D);
                    var victims = sl.getEntitiesOfClass(LivingEntity.class, box,
                            e -> e != this && !e.getTags().contains("shadow_soldier") && !e.getType().equals(this.getType()));
                    for (var v : victims) {
                        v.hurt(this.damageSources().magic(), 10.0F);
                        v.setDeltaMovement(v.getDeltaMovement().x, 0.55D, v.getDeltaMovement().z);
                        v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1));
                    }
                }
            }
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        // Daño de espinas por reflejo de sangre durante el Frenesí
        if (hurt && bloodFrenzyTicks > 0 && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            attacker.hurt(this.damageSources().thorns(this), 5.0F);
            this.playSound(SoundEvents.THORNS_HIT, 1.0F, 1.0F);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (smashAnimationTimeout > 0) smashAnimationTimeout--;
            if (roarAnimationTimeout > 0) roarAnimationTimeout--;

            if (bloodFrenzyTicks > 0 && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CRIMSON_SPORE,
                        this.getRandomX(0.7D), this.getY() + this.random.nextDouble() * 2.0D, this.getRandomZ(0.7D),
                        0, 0.03D, 0);
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getRandomX(0.6D), this.getY() + 0.5D, this.getRandomZ(0.6D),
                        0, 0.02D, 0);
            }
        } else {
            if (bloodFrenzyTicks > 0) bloodFrenzyTicks--;
            if (roarCooldown > 0) roarCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null && roarCooldown <= 0 && this.distanceToSqr(target) < 144.0D) {
                this.roarCooldown = 260;
                this.bloodFrenzyTicks = 180; // 9 segundos de Frenesí de Sangre
                this.level().broadcastEntityEvent(this, (byte) 6); // Roar
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 180, 1));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 180, 1));
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 180, 0));
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.5F, 0.8F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.5D, this.getZ(),
                            3, 0.5D, 0.5D, 0.5D, 0.05D);
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.0D, this.getZ(),
                            40, 0.6D, 0.8D, 0.6D, 0.1D);
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 5) {
            this.smashAnimationState.start(this.tickCount);
            this.smashAnimationTimeout = 30;
        } else if (id == 6) {
            this.roarAnimationState.start(this.tickCount);
            this.roarAnimationTimeout = 30;
            this.bloodFrenzyTicks = 180;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_BRUTE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_BRUTE_DEATH;
    }
}
