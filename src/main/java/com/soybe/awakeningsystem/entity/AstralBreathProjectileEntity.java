package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Proyectil de Aliento Astral disparado por el Dragón Bebé de Cristal.
 * Brilla con esencia estelar y genera daño mágico cristalino con destellos.
 */
public class AstralBreathProjectileEntity extends ThrowableProjectile {

    private int lifeTicks = 0;

    public AstralBreathProjectileEntity(EntityType<? extends AstralBreathProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public AstralBreathProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.ASTRAL_BREATH.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No additional synced data needed
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide) {
            // Rastro de partículas estelares de cristal
            for (int i = 0; i < 2; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 0.25D;
                double oy = (this.random.nextDouble() - 0.5D) * 0.25D;
                double oz = (this.random.nextDouble() - 0.5D) * 0.25D;
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        0.0D, 0.01D, 0.0D);
                this.level().addParticle(ModParticles.SPIRIT_FOXFIRE.get(),
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        0.0D, 0.005D, 0.0D);
            }
        } else {
            if (lifeTicks > 80) { // Desaparece tras 4 segundos de vuelo
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            var target = result.getEntity();
            var owner = this.getOwner();

            if (target != owner && !(target instanceof AstralDragonEntity)) {
                DamageSource damageSource = owner instanceof LivingEntity livingOwner
                        ? this.damageSources().mobProjectile(this, livingOwner)
                        : this.damageSources().magic();

                target.hurt(damageSource, 7.0F);

                this.playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 1.2F, 1.5F);
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.6F);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int i = 0; i < 10; i++) {
                double vx = (this.random.nextDouble() - 0.5D) * 0.2D;
                double vy = this.random.nextDouble() * 0.2D;
                double vz = (this.random.nextDouble() - 0.5D) * 0.2D;
                this.level().addParticle(ParticleTypes.GLOW,
                        this.getX(), this.getY(), this.getZ(), vx, vy, vz);
                this.level().addParticle(ModParticles.SPIRIT_FOXFIRE.get(),
                        this.getX(), this.getY(), this.getZ(), vx, vy, vz);
            }
        }
    }
}
