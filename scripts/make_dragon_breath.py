import os

content = """package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * Proyectil de Aliento Elemental Dracónico disparado por el Dragón Ancestral.
 * Se adapta al elemento del dragón:
 * 0 = Fuego (Llama abrasadora e ignición)
 * 1 = Hielo (Escarcha glacial y ralentización extrema)
 * 2 = Bosque/Tierra (Esporas tóxicas y envenenamiento)
 * 3 = Sombra (Llama oscura y efecto Wither)
 */
public class AncientDragonBreathEntity extends Projectile {

    public static final EntityDataAccessor<Integer> ELEMENT_TYPE =
            SynchedEntityData.defineId(AncientDragonBreathEntity.class, EntityDataSerializers.INT);

    private int lifeTicks = 0;

    public AncientDragonBreathEntity(EntityType<? extends AncientDragonBreathEntity> type, Level level) {
        super(type, level);
    }

    public AncientDragonBreathEntity(Level level, LivingEntity shooter, double vx, double vy, double vz, int elementType) {
        super(ModEntities.ANCIENT_DRAGON_BREATH.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.3D, shooter.getZ());
        this.setDeltaMovement(vx, vy, vz);
        this.entityData.set(ELEMENT_TYPE, elementType);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ELEMENT_TYPE, 0);
    }

    public int getElementType() {
        return this.entityData.get(ELEMENT_TYPE);
    }

    public void setElementType(int type) {
        this.entityData.set(ELEMENT_TYPE, type);
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
            int elem = getElementType();
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double py = this.getY() + (this.random.nextDouble() - 0.5D) * 0.8D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;

            switch (elem) {
                case 0 -> { // Fuego
                    this.level().addParticle(ParticleTypes.FLAME, px, py, pz, movement.x * 0.2, movement.y * 0.2, movement.z * 0.2);
                    this.level().addParticle(ParticleTypes.LAVA, px, py, pz, 0, 0.05, 0);
                    this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02, 0);
                }
                case 1 -> { // Hielo
                    this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, movement.x * 0.1, movement.y * 0.1, movement.z * 0.1);
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.02, 0);
                }
                case 2 -> { // Bosque / Tierra
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, movement.x * 0.1, movement.y * 0.1, movement.z * 0.1);
                    this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, px, py, pz, 0, 0.02, 0);
                    this.level().addParticle(ParticleTypes.ENCHANTED_HIT, px, py, pz, movement.x * 0.2, movement.y * 0.2, movement.z * 0.2);
                }
                case 3 -> { // Sombra
                    this.level().addParticle(ParticleTypes.WITCH, px, py, pz, movement.x * 0.1, movement.y * 0.1, movement.z * 0.1);
                    this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.05, 0);
                    this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2);
                }
            }
        }

        if (this.lifeTicks > 60) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();
            if (target == owner) return;

            DamageSource dmgSource = owner instanceof LivingEntity living
                    ? this.damageSources().mobAttack(living)
                    : this.damageSources().magic();

            int elem = getElementType();
            float baseDamage = 22.0F;

            target.hurt(dmgSource, baseDamage);

            if (target instanceof LivingEntity livingTarget) {
                switch (elem) {
                    case 0 -> { // Fuego
                        livingTarget.igniteForSeconds(6);
                    }
                    case 1 -> { // Hielo
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                        livingTarget.setTicksFrozen(200);
                    }
                    case 2 -> { // Bosque
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 2));
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    }
                    case 3 -> { // Sombra
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 2));
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    }
                }
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2F, 1.4F);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != this.getOwner();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ElementType", getElementType());
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setElementType(tag.getInt("ElementType"));
        this.lifeTicks = tag.getInt("LifeTicks");
    }
}
"""

with open("src/main/java/com/soybe/awakeningsystem/entity/AncientDragonBreathEntity.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Created AncientDragonBreathEntity.java")
