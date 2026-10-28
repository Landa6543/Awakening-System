package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Misil autoguiado disparado por el Guardián de las Ruinas.
 * Sube en arco y persigue a la víctima dejando estela de fuego y humo antes de detonar.
 */
public class RuinMissileEntity extends ThrowableProjectile {

    private int lifeTicks = 0;
    private LivingEntity target;

    public RuinMissileEntity(EntityType<? extends RuinMissileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public RuinMissileEntity(Level level, LivingEntity shooter, LivingEntity target) {
        super(ModEntities.RUIN_MISSILE.get(), shooter, level);
        this.target = target;
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
            this.level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.02D, 0.0D);
            this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.01D, 0.0D);
        } else {
            if (target != null && target.isAlive() && lifeTicks > 8) {
                Vec3 targetPos = target.getEyePosition();
                Vec3 dir = targetPos.subtract(this.position()).normalize();
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.scale(0.85).add(dir.scale(0.22)));
            }

            if (lifeTicks > 100) {
                explode();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            if (result.getEntity() instanceof LivingEntity victim && victim != this.getOwner()) {
                victim.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), 10.0F);
            }
            explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            explode();
        }
    }

    private void explode() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 1.3F);
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
                    15, 0.3, 0.3, 0.3, 0.08);

            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D))) {
                if (nearby != this.getOwner()) {
                    nearby.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), 8.0F);
                }
            }
        }
        this.discard();
    }
}
