package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
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

public class FrostElfAssassinEntity extends Monster {
    public final AnimationState dualSlashAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    public int dualSlashAnimationTimeout = 0;
    public int dashAnimationTimeout = 0;

    private int dashCooldown = 60;
    private int vanishCooldown = 180;

    public FrostElfAssassinEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 75;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy)) {
                    this.resetAttackCooldown();
                    this.mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    this.mob.swing(net.minecraft.world.InteractionHand.OFF_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            this.level().broadcastEntityEvent(this, (byte) 12); // dual slash
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 160, 240));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.4F);
            this.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.8F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, living.getX(), living.getY() + 1.0D, living.getZ(),
                        25, 0.4D, 0.5D, 0.4D, 0.05D);
            }
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        // Habilidad pasiva: Desvanecimiento en Humo Helado si vida < 50%
        if (hurt && !this.level().isClientSide && vanishCooldown <= 0 && this.getHealth() < this.getMaxHealth() * 0.5F) {
            vanishCooldown = 220;
            this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2, false, false, false));
            this.playSound(SoundEvents.SNOW_GOLEM_DEATH, 1.2F, 1.5F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(),
                        30, 0.6D, 0.6D, 0.6D, 0.03D);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.8D, this.getZ(),
                        30, 0.6D, 0.6D, 0.6D, 0.05D);
            }

            // Reposicionarse a un flanco
            LivingEntity target = this.getTarget();
            if (target != null) {
                double angle = Math.toRadians(target.getYRot() + (this.random.nextBoolean() ? 90 : -90));
                double tx = target.getX() + Math.sin(angle) * 3.0D;
                double tz = target.getZ() - Math.cos(angle) * 3.0D;
                net.minecraft.world.phys.Vec3 safePos = com.soybe.awakeningsystem.TeleportHelper.findSafeTeleportPos(this.level(), this, tx, target.getY(), tz, target.position());
                this.teleportTo(safePos.x, safePos.y, safePos.z);
            }
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (dualSlashAnimationTimeout > 0) dualSlashAnimationTimeout--;
            if (dashAnimationTimeout > 0) dashAnimationTimeout--;

            // Estela de escarcha
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getRandomX(0.5D), this.getY() + 0.2D, this.getRandomZ(0.5D),
                        0, 0.01D, 0);
            }
        } else {
            if (dashCooldown > 0) dashCooldown--;
            if (vanishCooldown > 0) vanishCooldown--;

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);

                // Habilidad activa: Embestida Gélida (Frost Dash)
                if (dashCooldown <= 0 && distSq >= 36.0D && distSq <= 225.0D) {
                    dashCooldown = 90;
                    this.level().broadcastEntityEvent(this, (byte) 13); // Dash anim
                    this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.3F, 1.6F);

                    Vec3 dashVec = target.position().subtract(this.position()).normalize().scale(1.8D);
                    this.setDeltaMovement(dashVec.x, 0.25D, dashVec.z);
                    this.hurtMarked = true;

                    if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        for (int i = 0; i < 8; i++) {
                            double px = this.getX() + dashVec.x * (i / 8.0D);
                            double py = this.getY() + 0.3D;
                            double pz = this.getZ() + dashVec.z * (i / 8.0D);
                            sl.sendParticles(ParticleTypes.SNOWFLAKE, px, py, pz, 4, 0.2D, 0.2D, 0.2D, 0.02D);
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 2, 0.1D, 0.1D, 0.1D, 0.01D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 12) {
            this.dualSlashAnimationState.start(this.tickCount);
            this.dualSlashAnimationTimeout = 15;
        } else if (id == 13) {
            this.dashAnimationState.start(this.tickCount);
            this.dashAnimationTimeout = 20;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }
}
