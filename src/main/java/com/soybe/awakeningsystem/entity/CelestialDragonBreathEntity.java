package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Orbe de Aliento de Qi Celestial disparado por el Dragón Chino Ancestral de Murim.
 * Viaja en espiral veloz y explota desatando rayos dorados y plasma celestial al impactar.
 */
public class CelestialDragonBreathEntity extends Projectile {

    private int lifeTicks = 0;

    public CelestialDragonBreathEntity(EntityType<? extends CelestialDragonBreathEntity> type, Level level) {
        super(type, level);
    }

    public CelestialDragonBreathEntity(Level level, LivingEntity shooter, double vx, double vy, double vz) {
        super(ModEntities.CELESTIAL_DRAGON_BREATH.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ());
        this.setDeltaMovement(vx, vy, vz);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;

        Vec3 movement = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        if (this.level().isClientSide) {
            // Estela visual mística de plasma celestial
            this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.2, (this.random.nextDouble() - 0.5) * 0.2, (this.random.nextDouble() - 0.5) * 0.2);
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1, (this.random.nextDouble() - 0.5) * 0.1, (this.random.nextDouble() - 0.5) * 0.1);
            this.level().addParticle(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.05, (this.random.nextDouble() - 0.5) * 0.05, (this.random.nextDouble() - 0.5) * 0.05);
        } else {
            if (this.lifeTicks > 120) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();
            if (target != owner && target instanceof LivingEntity livingTarget) {
                DamageSource ds = owner instanceof LivingEntity livingOwner ?
                        this.damageSources().mobProjectile(this, livingOwner) :
                        this.damageSources().magic();
                livingTarget.hurt(ds, 16.0F);
                livingTarget.igniteForSeconds(4);
            }
            this.explode();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.explode();
        }
    }

    private void explode() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY(), this.getZ(), 60, 1.2, 1.2, 1.2, 0.25);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 40, 0.8, 0.8, 0.8, 0.2);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.2F, 1.5F);
        this.discard();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }
}
