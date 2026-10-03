package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AstralDragonCrescentEntity extends ThrowableProjectile {

    private int lifeTicks = 0;
    private final Set<Integer> hitEntityIds = new HashSet<>();

    public AstralDragonCrescentEntity(EntityType<? extends AstralDragonCrescentEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public AstralDragonCrescentEntity(Level level, LivingEntity shooter) {
        super(ModEntities.ASTRAL_DRAGON_CRESCENT.get(), shooter, level);
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
            Vec3 mov = this.getDeltaMovement();
            Vec3 side = mov.cross(new Vec3(0, 1, 0)).normalize();
            for (double s = -1.2D; s <= 1.2D; s += 0.4D) {
                double px = this.getX() + (side.x * s);
                double py = this.getY() + 0.1D;
                double pz = this.getZ() + (side.z * s);
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.01D, 0);
                this.level().addParticle(ParticleTypes.SWEEP_ATTACK, px, py, pz, 0, 0, 0);
                if (this.random.nextFloat() < 0.3F) {
                    this.level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, 0, 0.02D, 0);
                }
            }
        } else {
            if (lifeTicks > 35) {
                this.discard();
                return;
            }

            AABB crescentBox = this.getBoundingBox().inflate(1.2D, 0.6D, 1.2D);
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, crescentBox, e ->
                    e.isAlive() && e != this.getOwner() && !hitEntityIds.contains(e.getId()) &&
                    !e.getTags().contains("shadow_soldier"));

            for (LivingEntity target : targets) {
                hitEntityIds.add(target.getId());
                Entity owner = this.getOwner();
                DamageSource ds = owner instanceof Player p
                        ? this.damageSources().playerAttack(p)
                        : this.damageSources().magic();
                target.hurt(ds, 18.0F);
                target.setRemainingFireTicks(80);

                Vec3 push = this.getDeltaMovement().normalize().scale(0.8D).add(0, 0.35D, 0);
                target.setDeltaMovement(target.getDeltaMovement().add(push));
                target.hurtMarked = true;

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.PLAYERS, 0.8F, 1.6F);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.08D);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 1.4F);
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) result);
        }
    }
}
