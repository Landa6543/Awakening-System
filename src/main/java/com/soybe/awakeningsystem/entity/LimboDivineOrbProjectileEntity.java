package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Orbe Divino del Limbo (Divine Sphere Projectile):
 * Esfera celestial blanca y dorada manifestada por el Ángel Ofanim del Limbo.
 * Viaja en el aire con halos rúnicos giratorios y genera una detonación de juicio sagrado al impactar.
 */
public class LimboDivineOrbProjectileEntity extends ThrowableProjectile implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SPIN_ANIM = RawAnimation.begin().thenLoop("animation.orbe_divino.spin");

    private int lifeTicks = 0;

    public LimboDivineOrbProjectileEntity(EntityType<? extends LimboDivineOrbProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public LimboDivineOrbProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.LIMBO_DIVINE_ORB.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 0.35D;
                double oy = (this.random.nextDouble() - 0.5D) * 0.35D;
                double oz = (this.random.nextDouble() - 0.5D) * 0.35D;
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        0.0D, 0.02D, 0.0D);
                this.level().addParticle(ParticleTypes.INSTANT_EFFECT,
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        0.0D, 0.01D, 0.0D);
            }
        } else {
            if (lifeTicks > 120) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity target) {
                if (this.getOwner() == null || !this.getOwner().is(target)) {
                    target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 16.0F);
                    target.push(this.getDeltaMovement().x * 0.5D, 0.35D, this.getDeltaMovement().z * 0.5D);
                }
            }
            explodeCelestialImpact();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            explodeCelestialImpact();
        }
    }

    private void explodeCelestialImpact() {
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.4F, 1.8F);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 1.3F);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 2, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 45, 0.8D, 0.8D, 0.8D, 0.12D);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 30, 0.6D, 0.6D, 0.6D, 0.08D);
            }
            this.discard();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "orb_controller", 0, state ->
                state.setAndContinue(SPIN_ANIM)
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
