package com.soybe.awakeningsystem.celestial;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Everything a celestial weapon throws: a flying copy of the blade (beam), a spinning scythe that comes back, a
 * javelin that bursts and may split, a star falling from the sky, Vehemence's bolt that leaves a cloud of skulls.
 */
public class CelestialBoltEntity extends Projectile {
    public static final int BEAM = 0, SCYTHE = 1, JAVELIN = 2, STAR = 3, VEHEMENCE = 4, SHARD = 5;

    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(CelestialBoltEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<String> SPRITE = SynchedEntityData.defineId(CelestialBoltEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(CelestialBoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(CelestialBoltEntity.class, EntityDataSerializers.INT);

    private CelestialWeapon weapon = CelestialWeapon.EXOBLADE;
    private float damage;
    public int splits;
    private final Set<UUID> hit = new HashSet<>();

    public CelestialBoltEntity(EntityType<? extends CelestialBoltEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static CelestialBoltEntity create(ServerLevel level, LivingEntity owner, CelestialWeapon weapon, int kind, ItemStack stack, String sprite, float damage) {
        CelestialBoltEntity b = new CelestialBoltEntity(CelestialContent.BOLT, level);
        b.setOwner(owner);
        b.weapon = weapon;
        b.damage = damage;
        b.entityData.set(KIND, kind);
        b.entityData.set(COLOR, weapon.color);
        b.entityData.set(SPRITE, sprite == null ? "" : sprite);
        b.entityData.set(ITEM, sprite == null || sprite.isEmpty() ? stack.copyWithCount(1) : ItemStack.EMPTY);
        b.setPos(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());
        return b;
    }

    public void launch(Vec3 dir, double speed) {
        Vec3 v = dir.normalize().scale(speed);
        setDeltaMovement(v);
        double h = v.horizontalDistance();
        setYRot((float) Math.toDegrees(Math.atan2(v.x, v.z)));
        setXRot((float) Math.toDegrees(Math.atan2(v.y, h)));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(ITEM, ItemStack.EMPTY);
        b.define(SPRITE, "");
        b.define(KIND, BEAM);
        b.define(COLOR, 0xFFFFFF);
    }

    public ItemStack item() { return entityData.get(ITEM); }
    public String sprite() { return entityData.get(SPRITE); }
    public int kind() { return entityData.get(KIND); }
    public int color() { return entityData.get(COLOR); }

    private int life() {
        return switch (kind()) {
            case SCYTHE -> 44;
            case STAR -> 60;
            case JAVELIN, VEHEMENCE -> 50;
            case SHARD -> 40;
            default -> 26;
        };
    }

    @Override
    public void tick() {
        super.tick();
        int kind = kind();
        Vec3 v = getDeltaMovement();
        if (kind == SCYTHE && tickCount > 18 && getOwner() != null) {         // the scythe flies back to its owner
            Vec3 home = getOwner().position().add(0, 1.0, 0).subtract(position());
            if (home.lengthSqr() < 2.0) { discard(); return; }
            v = v.scale(0.8).add(home.normalize().scale(0.45));
        }
        if (kind == JAVELIN || kind == SHARD) v = v.add(0, -0.03, 0);
        Vec3 from = position(), to = from.add(v);
        if (!level().isClientSide) {
            BlockHitResult block = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS && kind != SCYTHE && kind != BEAM) {
                setPos(block.getLocation());
                impact(null);
                return;
            }
            if (block.getType() != HitResult.Type.MISS && kind == BEAM) {
                burst(block.getLocation(), 6);
                discard();
                return;
            }
            float r = kind == SCYTHE ? 1.4F : kind == BEAM ? 1.0F : 0.6F;
            for (Entity e : level().getEntities(this, getBoundingBox().expandTowards(v).inflate(r), this::canHitEntity)) {
                if (!(e instanceof LivingEntity le) || !hit.add(e.getUUID())) continue;
                strike(le, damage);
                if (kind == JAVELIN || kind == STAR || kind == VEHEMENCE || kind == SHARD) {
                    setPos(e.position().add(0, e.getBbHeight() * 0.5, 0));
                    impact(le);
                    return;
                }
                if (kind == BEAM && hit.size() >= 4) { discard(); return; }
            }
            if (tickCount > life()) {
                if (kind == STAR || kind == JAVELIN || kind == VEHEMENCE) impact(null);
                else discard();
                return;
            }
        } else trail();
        setDeltaMovement(v);
        setPos(to);
        double h = v.horizontalDistance();
        setYRot((float) Math.toDegrees(Math.atan2(v.x, v.z)));
        setXRot((float) Math.toDegrees(Math.atan2(v.y, h)));
    }

    private void trail() {
        int c = color();
        var p = CelestialWeaponItem.dust(c, kind() == STAR ? 1.2F : 1.0F);
        for (int i = 0; i < 2; i++)
            level().addParticle(p, getX() + (random.nextDouble() - 0.5) * 0.4, getY() + (random.nextDouble() - 0.5) * 0.4, getZ() + (random.nextDouble() - 0.5) * 0.4, 0, 0, 0);
        if (kind() == STAR && random.nextInt(2) == 0) level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
    }

    private void strike(LivingEntity target, float amount) {
        Entity owner = getOwner();
        var src = damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null);
        if (target.hurt(src, amount)) CelestialWeaponItem.applyEffect(weapon, target, owner, amount);
    }

    /** Bursts, splits or leaves its cloud, then is gone. */
    private void impact(LivingEntity direct) {
        if (!(level() instanceof ServerLevel sl)) return;
        int kind = kind();
        Vec3 at = position();
        float radius = kind == STAR ? 2.5F : kind == SHARD ? 1.8F : 3.0F;
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius), this::canHitEntity)) {
            if (e == direct || e.distanceToSqr(at) > radius * radius) continue;
            strike(e, damage * 0.6F);
        }
        burst(at, kind == STAR ? 10 : 22);
        if (kind == VEHEMENCE) {
            Entity owner = getOwner();
            sl.addFreshEntity(CelestialCloudEntity.create(sl, owner instanceof LivingEntity l ? l : null, weapon, CelestialCloudEntity.SKULLS, at, 3.0F, 100, damage * 0.3F));
            sl.playSound(null, at.x, at.y, at.z, SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.7F, 1.6F);
        } else sl.playSound(null, at.x, at.y, at.z, kind == STAR ? SoundEvents.AMETHYST_CLUSTER_BREAK : SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, kind == STAR ? 1.0F : 0.6F, kind == STAR ? 1.2F : 1.6F);
        if (kind == JAVELIN && splits > 0) {
            // the pieces rain down around the burst
            Entity owner = getOwner();
            for (int i = 0; i < splits; i++) {
                double a = Math.PI * 2 * i / splits;
                Vec3 from = at.add(Math.cos(a) * 2.5, 6 + random.nextFloat() * 2, Math.sin(a) * 2.5);
                CelestialBoltEntity s = create(sl, owner instanceof LivingEntity l ? l : null, weapon, SHARD, item(), sprite(), damage * 0.45F);
                s.setPos(from);
                s.launch(at.add(Math.cos(a) * 1.2, 0, Math.sin(a) * 1.2).subtract(from), 0.9);
                sl.addFreshEntity(s);
            }
        }
        discard();
    }

    private void burst(Vec3 at, int n) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(CelestialWeaponItem.dust(color(), 2.0F), at.x, at.y, at.z, n, 0.6, 0.6, 0.6, 0.05);
            sl.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, n / 3, 0.3, 0.3, 0.3, 0.08);
        }
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        Entity owner = getOwner();
        return super.canHitEntity(e) && e instanceof LivingEntity && e.isAlive() && e != owner && !(owner != null && e.isAlliedTo(owner));
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) { return d < 128 * 128; }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = 10000;                     // a bolt left in a saved world just fades away
    }
}
