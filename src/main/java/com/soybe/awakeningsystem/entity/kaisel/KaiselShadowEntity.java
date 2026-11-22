package com.soybe.awakeningsystem.entity.kaisel;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Kaisel Sombra - Montura alada invocable y soldado de sombra leal del Monarca.
 */
public class KaiselShadowEntity extends AbstractKaiselEntity {

    private int attackTimer = 0;

    public KaiselShadowEntity(EntityType<? extends AbstractKaiselEntity> entityType, Level level) {
        super(entityType, level);
        this.addTag("shadow_soldier");
    }

    @Override
    public boolean isShadowVariant() {
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new KaiselShadowFollowOrFightGoal(this));
        // Target hostile enemies of the owner
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                e -> e instanceof Enemy && !e.getTags().contains("shadow_soldier") && !(e instanceof Player)));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel sl && this.random.nextInt(4) == 0) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.6D, this.getZ(), 1, 0.4, 0.2, 0.4, 0.01);
            sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6D, this.getZ(), 1, 0.4, 0.2, 0.4, 0.01);
        }
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();

        if (this.isVehicle()) {
            this.entityData.set(ATTACKING_ANIM, false);
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            attackTimer++;
            double distSq = this.distanceToSqr(target);

            if (attackTimer >= 35) {
                attackTimer = 0;
                if (distSq < 30.0D) {
                    this.entityData.set(ATTACK_TYPE, 1);
                    this.entityData.set(ATTACKING_ANIM, true);
                    this.doHurtTarget(target);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.NEUTRAL, 1.3F, 0.9F);
                } else if (distSq < 500.0D) {
                    this.entityData.set(ATTACK_TYPE, 2);
                    this.entityData.set(ATTACKING_ANIM, true);
                    if (this.level() instanceof ServerLevel sl) {
                        Vec3 dir = target.position().subtract(this.position()).normalize();
                        for (int i = 1; i <= 7; i++) {
                            Vec3 p = this.position().add(dir.scale(i * 2.2D));
                            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 0.8D, p.z, 4, 0.2, 0.2, 0.2, 0.05);
                        }
                        target.hurt(this.damageSources().mobAttack(this), 22.0F);
                    }
                }
            } else if (attackTimer > 15) {
                this.entityData.set(ATTACKING_ANIM, false);
            }
        } else {
            this.entityData.set(ATTACKING_ANIM, false);
        }
    }

    static class KaiselShadowFollowOrFightGoal extends Goal {
        private final KaiselShadowEntity kaisel;

        public KaiselShadowFollowOrFightGoal(KaiselShadowEntity kaisel) {
            this.kaisel = kaisel;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (kaisel.isVehicle()) return false;
            return kaisel.getTarget() != null || kaisel.getOwnerUUID() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = kaisel.getTarget();
            if (target != null && target.isAlive()) {
                kaisel.setFlying(true);
                kaisel.getLookControl().setLookAt(target, 30.0F, 30.0F);
                Vec3 targetPos = target.position();
                kaisel.getMoveControl().setWantedPosition(targetPos.x, targetPos.y + 3.0D, targetPos.z, 1.2D);
                return;
            }

            // Follow owner
            if (kaisel.getOwnerUUID() != null && kaisel.level() instanceof ServerLevel sl) {
                Player owner = sl.getPlayerByUUID(kaisel.getOwnerUUID());
                if (owner != null && owner.isAlive()) {
                    double distSq = kaisel.distanceToSqr(owner);
                    if (distSq > 400.0D) {
                        kaisel.teleportTo(owner.getX(), owner.getY() + 1.0D, owner.getZ());
                    } else if (distSq > 36.0D) {
                        kaisel.setFlying(true);
                        kaisel.getLookControl().setLookAt(owner, 20.0F, 20.0F);
                        kaisel.getMoveControl().setWantedPosition(owner.getX(), owner.getY() + 2.5D, owner.getZ(), 1.0D);
                    } else {
                        if (owner.onGround() && kaisel.isFlying() && distSq < 16.0D) {
                            kaisel.setFlying(false);
                        }
                    }
                }
            }
        }
    }
}
