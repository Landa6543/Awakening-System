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
 * Fragmento de escarcha arrojadizo disparado por la Regisvid Criogénica (Cryo Regisvine).
 * Persigue al objetivo dejando estela de cristales helados y causa daño Cryo con congelamiento.
 */
public class HoarfrostShardEntity extends ThrowableProjectile {

    private int lifeTicks = 0;
    private LivingEntity target;

    public HoarfrostShardEntity(EntityType<? extends HoarfrostShardEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public HoarfrostShardEntity(Level level, LivingEntity shooter, LivingEntity target) {
        super(ModEntities.HOARFROST_SHARD.get(), shooter, level);
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
            this.level().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, 0.01D, 0.0D);
            this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, x, y, z, 0.0D, 0.0D, 0.0D);
        } else {
            if (target != null && target.isAlive() && lifeTicks > 4) {
                Vec3 targetPos = target.getEyePosition();
                Vec3 dir = targetPos.subtract(this.position()).normalize();
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.scale(0.88).add(dir.scale(0.18)));
            }

            if (lifeTicks > 120) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity victim) {
            if (victim != this.getOwner()) {
                victim.hurt(this.damageSources().thrown(this, this.getOwner()), 10.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                victim.setTicksFrozen(Math.min(victim.getTicksRequiredToFreeze() + 100, victim.getTicksFrozen() + 100));
                explodeImpact();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            explodeImpact();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
    }

    private void explodeImpact() {
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.2F, 1.4F);
            sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(),
                    20, 0.4, 0.4, 0.4, 0.08);
            sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);
        }
        this.discard();
    }
}
