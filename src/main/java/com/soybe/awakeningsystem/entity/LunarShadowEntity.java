package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Sombra Astral de Aria:
 * Entidad espectral lunar invocada durante las pruebas del Santuario Trilunar.
 * Veloz, ágil, rodeada de resplandor estelar y capaz de proyectar ondas de corte lunar.
 */
public class LunarShadowEntity extends Monster {

    private int dashCooldown = 40;

    public LunarShadowEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 35;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 95.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextFloat() < 0.12F) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getRandomX(0.4D), this.getY() + 0.2D + this.random.nextDouble() * 1.4D, this.getRandomZ(0.4D),
                        0.0D, 0.01D, 0.0D);
            }
            if (this.random.nextFloat() < 0.06F) {
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getRandomX(0.5D), this.getY() + 0.5D + this.random.nextDouble() * 1.0D, this.getRandomZ(0.5D),
                        0.0D, 0.02D, 0.0D);
            }
        } else {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (dashCooldown > 0) dashCooldown--;
                double distSq = this.distanceToSqr(target);

                if (dashCooldown <= 0 && distSq >= 16.0D && distSq <= 196.0D) {
                    dashCooldown = 60 + this.random.nextInt(30);
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 1.35D, 0.25D, dir.z * 1.35D);
                    this.hasImpulse = true;

                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0, 0, 0, 0);
                        sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(), 25, 0.6D, 0.6D, 0.6D, 0.08D);
                        sl.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 1.5F);
                    }
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (hit && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY() + 1.0D, living.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                sl.playSound(null, living.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 1.2F, 1.4F);
            }
        }
        return hit;
    }
}
