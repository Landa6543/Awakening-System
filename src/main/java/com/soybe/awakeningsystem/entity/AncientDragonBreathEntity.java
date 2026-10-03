package com.soybe.awakeningsystem.entity;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Proyectil de Aliento Elemental Dracónico disparado desde el hocico del Dragón Ancestral.
 */
public class AncientDragonBreathEntity extends Projectile {

    public static final EntityDataAccessor<Integer> ELEMENT_TYPE =
            SynchedEntityData.defineId(AncientDragonBreathEntity.class, EntityDataSerializers.INT);

    private int lifeTicks = 0;

    public AncientDragonBreathEntity(EntityType<? extends AncientDragonBreathEntity> type, Level level) {
        super(type, level);
    }

    public AncientDragonBreathEntity(Level level, LivingEntity shooter, double x, double y, double z, double vx, double vy, double vz, int elementType) {
        super(ModEntities.ANCIENT_DRAGON_BREATH.get(), level);
        this.setOwner(shooter);
        this.setPos(x, y, z);
        this.setDeltaMovement(vx, vy, vz);
        this.entityData.set(ELEMENT_TYPE, elementType);
    }

    public AncientDragonBreathEntity(Level level, LivingEntity shooter, double vx, double vy, double vz, int elementType) {
        this(level, shooter, shooter.getX(), shooter.getEyeY() - 0.3D, shooter.getZ(), vx, vy, vz, elementType);
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
            for (int i = 0; i < 5; i++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.0D;
                double py = this.getY() + (this.random.nextDouble() - 0.5D) * 1.0D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.0D;

                switch (getElementType()) {
                    case AncientDragonEntity.VARIANT_FIRE -> {
                        this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.04, 0);
                        if (this.random.nextInt(2) == 0) {
                            this.level().addParticle(ParticleTypes.LAVA, px, py, pz, 0, 0, 0);
                            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 0, 0.02, 0);
                        }
                    }
                    case AncientDragonEntity.VARIANT_ICE -> {
                        this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, -0.02, 0);
                        this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.01, 0);
                        if (this.random.nextInt(2) == 0) {
                            this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, px, py, pz, 0, 0, 0);
                        }
                    }
                    case AncientDragonEntity.VARIANT_FOREST -> {
                        this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, px, py, pz, 0, 0.03, 0);
                        this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.02, 0);
                        if (this.random.nextInt(2) == 0) {
                            this.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0, 0);
                        }
                    }
                    case AncientDragonEntity.VARIANT_SHADOW -> {
                        this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.05, 0);
                        this.level().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0, 0);
                        if (this.random.nextInt(2) == 0) {
                            this.level().addParticle(ParticleTypes.WITCH, px, py, pz, 0, 0.02, 0);
                        }
                    }
                    case AncientDragonEntity.VARIANT_LIGHTNING -> {
                        this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.05, 0);
                        if (this.random.nextInt(2) == 0) {
                            this.level().addParticle(ParticleTypes.FIREWORK, px, py, pz, 0, 0.02, 0);
                        }
                    }
                    case AncientDragonEntity.VARIANT_CELESTIAL -> {
                        this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.03, 0);
                        if (this.random.nextInt(2) == 0) {
                            this.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0.02, 0);
                            this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, px, py, pz, 0, 0.01, 0);
                        }
                    }
                }
            }
        }

        if (this.lifeTicks > 70) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        triggerElementalImpact(result.getEntity());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK) {
            triggerElementalImpact(null);
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    private void triggerElementalImpact(@Nullable Entity directTarget) {
        if (this.level().isClientSide) return;

        Entity owner = this.getOwner();
        DamageSource source = owner instanceof LivingEntity l
                ? this.damageSources().mobAttack(l)
                : this.damageSources().generic();

        double radius = 5.0D;
        AABB impactBox = this.getBoundingBox().inflate(radius, radius, radius);
        var nearby = this.level().getEntitiesOfClass(LivingEntity.class, impactBox, e -> e != owner && e.isAlive());

        switch (getElementType()) {
            case AncientDragonEntity.VARIANT_FIRE -> {
                // EXPLOSIÓN ÍGNEA DESTRUCTORA
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.85F);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.5F, 0.7F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 25, 1.5, 1.0, 1.5, 0.2);
                }
                for (LivingEntity e : nearby) {
                    float dist = (float) e.distanceTo(this);
                    float dmg = Math.max(12.0F, 32.0F - dist * 3.5F);
                    e.hurt(source, dmg);
                    e.igniteForSeconds(10.0F);
                    Vec3 knock = e.position().subtract(this.position()).normalize().scale(0.8D);
                    e.push(knock.x, 0.35D, knock.z);
                }
            }
            case AncientDragonEntity.VARIANT_ICE -> {
                // NOVA GLACIAL CONGELANTE
                this.playSound(SoundEvents.GLASS_BREAK, 2.0F, 0.75F);
                this.playSound(SoundEvents.PLAYER_HURT_FREEZE, 2.0F, 0.65F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 50, 2.0, 1.0, 2.0, 0.15);
                    sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(), 30, 1.5, 1.0, 1.5, 0.2);
                }
                for (LivingEntity e : nearby) {
                    float dist = (float) e.distanceTo(this);
                    float dmg = Math.max(10.0F, 26.0F - dist * 3.0F);
                    e.hurt(source, dmg);
                    e.setTicksFrozen(350);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                }
            }
            case AncientDragonEntity.VARIANT_FOREST -> {
                // NUBE DE ESPORAS TÓXICAS Y ENREDADERAS
                this.playSound(SoundEvents.MUD_BREAK, 2.0F, 0.8F);
                this.playSound(SoundEvents.THORNS_HIT, 1.8F, 0.9F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(), this.getZ(), 60, 2.5, 1.2, 2.5, 0.1);
                    sl.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 20, 1.5, 1.0, 1.5, 0.1);
                }
                for (LivingEntity e : nearby) {
                    float dist = (float) e.distanceTo(this);
                    float dmg = Math.max(8.0F, 24.0F - dist * 2.5F);
                    e.hurt(source, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1));
                }
            }
            case AncientDragonEntity.VARIANT_SHADOW -> {
                // SINGULARIDAD DEL VACÍO / VÓRTICE GRAVITACIONAL
                this.playSound(SoundEvents.WITHER_SHOOT, 2.0F, 0.65F);
                this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.5F, 1.2F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 80, 2.0, 1.5, 2.0, 0.5);
                    sl.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), 40, 1.0, 1.0, 1.0, 0.2);
                }
                for (LivingEntity e : nearby) {
                    float dist = (float) e.distanceTo(this);
                    float dmg = Math.max(12.0F, 30.0F - dist * 3.0F);
                    e.hurt(source, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));

                    // Tirón gravitacional hacia el vórtice
                    Vec3 pull = this.position().subtract(e.position()).normalize().scale(0.75D);
                    e.setDeltaMovement(pull.x, 0.25D, pull.z);
                    e.hurtMarked = true;
                }
            }
            case AncientDragonEntity.VARIANT_LIGHTNING -> {
                // IMPACTO DE RAYO CELESTIAL Y CHOQUE PARALIZANTE
                this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.5F, 0.9F);
                this.playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 2.0F, 1.1F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 80, 2.0, 1.5, 2.0, 0.25);
                    net.minecraft.world.entity.LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(this.getX(), this.getY(), this.getZ());
                        bolt.setVisualOnly(true);
                        sl.addFreshEntity(bolt);
                    }
                }
                for (LivingEntity e : nearby) {
                    float dist = (float) e.distanceTo(this);
                    float dmg = Math.max(14.0F, 34.0F - dist * 3.5F);
                    e.hurt(source, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 35, 1));
                }
            }
            case AncientDragonEntity.VARIANT_CELESTIAL -> {
                // DESTELLO SOLAR RADIANTE Y PURIFICACIÓN
                this.playSound(SoundEvents.TOTEM_USE, 2.0F, 1.1F);
                this.playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 1.2F);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 2, 0, 0, 0, 0);
                    sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 70, 2.0, 1.5, 2.0, 0.15);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY(), this.getZ(), 40, 1.5, 1.0, 1.5, 0.2);
                }
                for (LivingEntity e : nearby) {
                    float dist = (float) e.distanceTo(this);
                    float baseDmg = Math.max(12.0F, 30.0F - dist * 3.0F);
                    // Daño sagrado purificador amplificado contra no-muertos
                    if (e.isInvertedHealAndHarm()) {
                        baseDmg += 16.0F;
                    }
                    e.hurt(source, baseDmg);
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 1));
                    Vec3 push = e.position().subtract(this.position()).normalize().scale(0.9D);
                    e.push(push.x, 0.3D, push.z);
                }
            }
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("LifeTicks", this.lifeTicks);
        compound.putInt("ElementType", getElementType());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.lifeTicks = compound.getInt("LifeTicks");
        setElementType(compound.getInt("ElementType"));
    }
}
