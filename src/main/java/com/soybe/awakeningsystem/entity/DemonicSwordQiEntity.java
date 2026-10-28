package com.soybe.awakeningsystem.entity;

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
 * Proyectil de Tajo de Espada de Qi Carmesí disparado por los cultivadores del Culto Demoníaco.
 */
public class DemonicSwordQiEntity extends ThrowableProjectile {

    private int lifeTicks = 0;

    public DemonicSwordQiEntity(EntityType<? extends DemonicSwordQiEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public DemonicSwordQiEntity(Level level, LivingEntity shooter) {
        super(ModEntities.DEMONIC_SWORD_QI.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 0.3D;
                double oy = (this.random.nextDouble() - 0.5D) * 0.3D;
                double oz = (this.random.nextDouble() - 0.5D) * 0.3D;
                this.level().addParticle(ParticleTypes.CRIMSON_SPORE, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 0, 0, 0);
                this.level().addParticle(ParticleTypes.SWEEP_ATTACK, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 0, 0, 0);
            }
        } else {
            if (lifeTicks > 60) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            if (target != this.getOwner()) {
                DamageSource ds = this.damageSources().mobAttack((LivingEntity) this.getOwner());
                target.hurt(ds, 13.0F);
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.2F);
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.playSound(SoundEvents.ANVIL_LAND, 0.5F, 1.8F);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && result.getType() != HitResult.Type.ENTITY) {
            this.discard();
        }
    }
}
