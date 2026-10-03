package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Burbuja aprisionadora disparada por el Mago del Abismo Hydro.
 * Flota suavemente hacia el jugador. Al impactar, lo encierra en una esfera
 * acuática inmovilizadora que dura varios segundos.
 */
public class AbyssBubbleEntity extends ThrowableProjectile {

    private int lifeTicks = 0;
    private LivingEntity target;

    public AbyssBubbleEntity(EntityType<? extends AbyssBubbleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public AbyssBubbleEntity(Level level, LivingEntity shooter, LivingEntity target) {
        super(ModEntities.ABYSS_BUBBLE.get(), shooter, level);
        this.target = target;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();
            this.level().addParticle(ParticleTypes.SPLASH, x + (random.nextDouble() - 0.5) * 0.4, y + (random.nextDouble() - 0.5) * 0.4, z + (random.nextDouble() - 0.5) * 0.4, 0, 0.02, 0);
            this.level().addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.01, 0);
        } else {
            // Persecución lenta y flotante hacia el objetivo
            if (target != null && target.isAlive()) {
                Vec3 targetPos = target.getEyePosition();
                Vec3 dir = targetPos.subtract(this.position()).normalize();
                Vec3 current = this.getDeltaMovement();
                this.setDeltaMovement(current.scale(0.88).add(dir.scale(0.08)));
            }

            if (lifeTicks > 140) {
                popBubble();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity victim && !this.level().isClientSide) {
            if (this.getOwner() == victim) return;

            victim.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), 6.0F);

            // Efecto aprisionador de burbuja Hydro: Inmovilizado y suspendido
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 6, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 0, false, false, false));
            victim.setDeltaMovement(0.0D, 0.05D, 0.0D);

            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                        SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 1.8F, 1.2F);
                sl.sendParticles(ParticleTypes.SPLASH, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.1D);
                sl.sendParticles(ParticleTypes.BUBBLE_POP, victim.getX(), victim.getY() + 1.2D, victim.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.05D);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            popBubble();
        }
    }

    private void popBubble() {
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.HOSTILE, 1.4F, 1.0F);
            sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.08D);
        }
        this.discard();
    }
}
