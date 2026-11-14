package com.soybe.awakeningsystem.celestial;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A lingering cloud: the Mirror Blade's nebula, a slowly turning vortex that drags creatures to its heart, or the
 * cloud of red skulls Vehemence's bolt leaves behind. Both hurt everything inside every half second.
 */
public class CelestialCloudEntity extends Projectile {
    public static final int NEBULA = 0, SKULLS = 1;

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(CelestialCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(CelestialCloudEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(CelestialCloudEntity.class, EntityDataSerializers.INT);

    private CelestialWeapon weapon = CelestialWeapon.MIRROR_BLADE;
    private float damage;

    public CelestialCloudEntity(EntityType<? extends CelestialCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static CelestialCloudEntity create(ServerLevel level, LivingEntity owner, CelestialWeapon weapon, int kind, Vec3 at, float radius, int life, float damage) {
        CelestialCloudEntity c = new CelestialCloudEntity(CelestialContent.CLOUD, level);
        if (owner != null) c.setOwner(owner);
        c.weapon = weapon;
        c.damage = damage;
        c.entityData.set(KIND, kind);
        c.entityData.set(RADIUS, radius);
        c.entityData.set(LIFE, life);
        c.setPos(at.x, at.y + (kind == NEBULA ? 1.2 : 0.8), at.z);
        return c;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(KIND, NEBULA);
        b.define(RADIUS, 3.0F);
        b.define(LIFE, 100);
    }

    public int kind() { return entityData.get(KIND); }
    public float radius() { return entityData.get(RADIUS); }
    public int life() { return entityData.get(LIFE); }

    @Override
    public void tick() {
        super.tick();
        float r = radius();
        if (level().isClientSide) {
            if (random.nextInt(2) == 0) {
                double a = random.nextDouble() * Math.PI * 2, d = random.nextDouble() * r;
                level().addParticle(kind() == NEBULA ? ParticleTypes.PORTAL : ParticleTypes.SMOKE,
                        getX() + Math.cos(a) * d, getY() + (random.nextDouble() - 0.5) * r, getZ() + Math.sin(a) * d, 0, 0, 0);
            }
            return;
        }
        if (tickCount > life()) {
            discard();
            return;
        }
        Entity owner = getOwner();
        Vec3 c = position();
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r), x -> x.isAlive() && x != owner
                && !(owner != null && x.isAlliedTo(owner)))) {
            if (e.distanceToSqr(c) > r * r * 1.4) continue;
            if (kind() == NEBULA) {
                Vec3 pull = c.subtract(e.position()).normalize().scale(0.08);
                e.push(pull.x, pull.y * 0.5, pull.z);
                e.hurtMarked = true;
            }
            if (tickCount % 10 == 0) {
                var src = damageSources().indirectMagic(this, owner instanceof LivingEntity l ? l : null);
                if (e.hurt(src, damage)) {
                    CelestialWeaponItem.applyEffect(weapon, e, owner, damage);
                    if (kind() == SKULLS) e.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0));
                }
            }
        }
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = 100000;
    }
}
