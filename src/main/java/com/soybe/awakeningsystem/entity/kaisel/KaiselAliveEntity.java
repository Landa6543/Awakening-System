package com.soybe.awakeningsystem.entity.kaisel;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Kaisel Vivo - Variante salvaje / dragón wyvern que desafía al jugador en la Gran Torre.
 */
public class KaiselAliveEntity extends AbstractKaiselEntity {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.awakening_system.kaisel_alive"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(true);

    private int attackTimer = 0;

    public KaiselAliveEntity(EntityType<? extends AbstractKaiselEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 850;
    }

    @Override
    public boolean isShadowVariant() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new KaiselCombatFlightGoal(this));
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
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            attackTimer++;
            double distSq = this.distanceToSqr(target);

            if (attackTimer >= 40) {
                attackTimer = 0;
                if (distSq < 25.0D) {
                    // Attack 1: Melee strike
                    this.entityData.set(ATTACK_TYPE, 1);
                    this.entityData.set(ATTACKING_ANIM, true);
                    this.doHurtTarget(target);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.5F, 1.2F);
                } else if (distSq < 600.0D) {
                    // Attack 2: Thunder breath / lightning burst
                    this.entityData.set(ATTACK_TYPE, 2);
                    this.entityData.set(ATTACKING_ANIM, true);
                    if (this.level() instanceof ServerLevel sl) {
                        Vec3 dir = target.position().subtract(this.position()).normalize();
                        for (int i = 1; i <= 8; i++) {
                            Vec3 p = this.position().add(dir.scale(i * 2.5D));
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y + 1.0D, p.z, 6, 0.3, 0.3, 0.3, 0.1);
                        }
                        target.hurt(this.damageSources().mobAttack(this), 28.0F);
                        sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.2F, 1.4F);
                    }
                }
            } else if (attackTimer > 15) {
                this.entityData.set(ATTACKING_ANIM, false);
            }
        } else {
            this.entityData.set(ATTACKING_ANIM, false);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
    }

    static class KaiselCombatFlightGoal extends Goal {
        private final KaiselAliveEntity kaisel;

        public KaiselCombatFlightGoal(KaiselAliveEntity kaisel) {
            this.kaisel = kaisel;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return kaisel.getTarget() != null && kaisel.getTarget().isAlive();
        }

        @Override
        public void start() {
            kaisel.setFlying(true);
        }

        @Override
        public void tick() {
            LivingEntity target = kaisel.getTarget();
            if (target == null) return;

            kaisel.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = kaisel.distanceToSqr(target);

            // Orbit or dive towards target
            Vec3 targetPos = target.position();
            double altitude = distSq < 36.0D ? targetPos.y + 1.0D : targetPos.y + 4.5D;
            kaisel.getMoveControl().setWantedPosition(targetPos.x, altitude, targetPos.z, 1.25D);
        }
    }
}
