package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Guardián del Silencio Lunar (Canon):
 * Jefe protector de la Luna Nueva. Imponente guerrero cósmico con ondas de choque de gravedad lunar,
 * resistencia extrema y golpes sísmicos.
 */
public class LunarGuardianEntity extends Monster {

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(Component.literal("§5✦ Guardián del Silencio Lunar (Canon)"),
                    BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    private int slamCooldown = 60;
    private int teleportCooldown = 120;

    public LunarGuardianEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 26.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (slamCooldown > 0) slamCooldown--;
                if (teleportCooldown > 0) teleportCooldown--;

                double distSq = this.distanceToSqr(target);

                // 1. Golpe de Choque Gravitatorio (Slam en área)
                if (slamCooldown <= 0 && distSq <= 36.0D) {
                    slamCooldown = 100;
                    performGravitySlam();
                }

                // 2. Teletransporte de Sombra Lunar (acercarse si el jugador huye)
                if (teleportCooldown <= 0 && distSq > 64.0D && distSq <= 400.0D) {
                    teleportCooldown = 140;
                    teleportBehind(target);
                }
            }
        } else {
            // Partículas violeta del Silencio de Canon
            if (this.random.nextFloat() < 0.5F) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getRandomX(0.7D), this.getY() + 0.3D, this.getRandomZ(0.7D),
                        0.0D, 0.03D, 0.0D);
            }
            if (this.random.nextFloat() < 0.3F) {
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                        this.getRandomX(0.6D), this.getRandomY(), this.getRandomZ(0.6D),
                        0.0D, 0.04D, 0.0D);
            }
        }
    }

    private void performGravitySlam() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 0.2D, this.getZ(), 3, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.2D, this.getZ(), 60, 2.5D, 0.5D, 2.5D, 0.12D);
        sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.4F, 0.8F);
        sl.playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.5F, 0.7F);

        AABB box = this.getBoundingBox().inflate(5.5D, 2.0D, 5.5D);
        for (LivingEntity nearby : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby != this && nearby.isAlive() && !(nearby instanceof LunarShadowEntity)) {
                nearby.hurt(this.damageSources().mobAttack(this), 18.0F);
                nearby.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1, false, true));
                nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true));
                Vec3 push = nearby.position().subtract(this.position()).normalize().scale(0.8D);
                nearby.setDeltaMovement(push.x, 0.4D, push.z);
            }
        }
    }

    private void teleportBehind(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        Vec3 look = target.getLookAngle();
        double tx = target.getX() - look.x * 2.5D;
        double ty = target.getY();
        double tz = target.getZ() - look.z * 2.5D;

        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 0.6D, 1.0D, 0.6D, 0.1D);
        this.teleportTo(tx, ty, tz);
        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, tx, ty + 1.0D, tz, 50, 0.6D, 1.0D, 0.6D, 0.15D);
        sl.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 0.85F);
    }
}
