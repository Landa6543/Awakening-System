package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.CameraShakePayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad física de Tajo y Onda de Corte 3D (SlashWaveEntity).
 * Inspirada en los efectos visuales cinemáticos de Iron's Spells, Mahou Tsukai y Ars Nouveau.
 * Ofrece rendering orientado por ángulos de swing (roll, pitch, yaw), estelas y sacudida de pantalla.
 */
public class SlashWaveEntity extends Projectile {

    public static final int TYPE_DARK_VOID = 0;
    public static final int TYPE_CRYSTAL_AQUA = 1;
    public static final int TYPE_SOLAR_LIGHT = 2;
    public static final int TYPE_CRIMSON_BLOOD = 3;
    public static final int TYPE_SILVER_ROSE = 4;
    public static final int TYPE_CHERRY_BLOSSOM = 5;
    public static final int TYPE_COSMIC_AMETHYST = 6;
    public static final int TYPE_PYRO_FLAME = 7;
    public static final int TYPE_FROST_WIND = 8;
    public static final int TYPE_CELESTIAL = 9;
    public static final int TYPE_PRIMORDIAL_GENESIS = 10;

    private static final EntityDataAccessor<Integer> SLASH_TYPE =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> WIDTH =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROLL =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> PIERCE =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_CROSS =
            SynchedEntityData.defineId(SlashWaveEntity.class, EntityDataSerializers.BOOLEAN);

    private final Set<Integer> hitEntityIds = new HashSet<>();
    private float armorPenetration = 0.0F;

    public SlashWaveEntity(EntityType<? extends SlashWaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SlashWaveEntity(Level level, LivingEntity owner, int slashType, float damage,
                           float width, float height, float roll, int lifetime, int pierce) {
        super(ModEntities.SLASH_WAVE.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;

        Vec3 eye = owner.getEyePosition();
        Vec3 look = owner.getLookAngle();
        this.setPos(eye.x + look.x * 0.8D, eye.y - 0.2D + look.y * 0.8D, eye.z + look.z * 0.8D);

        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        this.entityData.set(SLASH_TYPE, slashType);
        this.entityData.set(DAMAGE, damage);
        this.entityData.set(WIDTH, width);
        this.entityData.set(HEIGHT, height);
        this.entityData.set(ROLL, roll);
        this.entityData.set(LIFETIME, lifetime);
        this.entityData.set(PIERCE, pierce);
        this.entityData.set(IS_CROSS, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SLASH_TYPE, TYPE_DARK_VOID);
        builder.define(WIDTH, 2.4F);
        builder.define(HEIGHT, 1.2F);
        builder.define(ROLL, 0.0F);
        builder.define(LIFETIME, 20);
        builder.define(DAMAGE, 20.0F);
        builder.define(PIERCE, 4);
        builder.define(IS_CROSS, false);
    }

    public int getSlashType() { return this.entityData.get(SLASH_TYPE); }
    public void setSlashType(int type) { this.entityData.set(SLASH_TYPE, type); }
    public float getSlashWidth() { return this.entityData.get(WIDTH); }
    public float getSlashHeight() { return this.entityData.get(HEIGHT); }
    public float getRoll() { return this.entityData.get(ROLL); }
    public void setRoll(float roll) { this.entityData.set(ROLL, roll); }
    public int getLifetime() { return this.entityData.get(LIFETIME); }
    public float getDamage() { return this.entityData.get(DAMAGE); }
    public int getPierce() { return this.entityData.get(PIERCE); }
    public boolean isCross() { return this.entityData.get(IS_CROSS); }
    public void setCross(boolean cross) { this.entityData.set(IS_CROSS, cross); }

    public void setArmorPenetration(float pen) { this.armorPenetration = pen; }
    public float getArmorPenetration() { return this.armorPenetration; }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > getLifetime()) {
            this.discard();
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        Vec3 currentPos = this.position();
        Vec3 nextPos = currentPos.add(movement);

        if (movement.lengthSqr() > 0.01D) {
            double horiz = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
            this.setYRot((float) (Math.atan2(movement.z, movement.x) * (180.0D / Math.PI)) - 90.0F);
            this.setXRot((float) (-(Math.atan2(movement.y, horiz) * (180.0D / Math.PI))));
        }

        BlockHitResult bhr = this.level().clip(new ClipContext(currentPos, nextPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (bhr.getType() != HitResult.Type.MISS) {
            onHitBlock(bhr);
            return;
        }

        this.setPos(nextPos);

        if (this.level().isClientSide) {
            spawnClientTrailParticles();
        } else {
            handleServerCollisions(currentPos, nextPos);
        }
    }

    private void handleServerCollisions(Vec3 from, Vec3 to) {
        float rad = getSlashWidth() * 0.6F;
        AABB sweepBox = new AABB(
                Math.min(from.x, to.x) - rad, Math.min(from.y, to.y) - 0.7D, Math.min(from.z, to.z) - rad,
                Math.max(from.x, to.x) + rad, Math.max(from.y, to.y) + 0.7D, Math.max(from.z, to.z) + rad
        );

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, sweepBox,
                e -> e.isAlive() && e != this.getOwner() && !hitEntityIds.contains(e.getId()));

        if (targets.isEmpty()) return;

        LivingEntity owner = (this.getOwner() instanceof LivingEntity le) ? le : null;
        DamageSource ds = (owner instanceof Player p)
                ? this.damageSources().playerAttack(p)
                : (owner != null ? this.damageSources().mobAttack(owner) : this.damageSources().generic());

        float dmg = getDamage();
        int currentPierce = getPierce();

        for (LivingEntity target : targets) {
            if (owner instanceof Player p && target instanceof Player tp && !p.canHarmPlayer(tp)) {
                continue;
            }

            hitEntityIds.add(target.getId());

            target.hurt(ds, dmg);

            applySlashEffect(target, owner);

            if (this.level() instanceof ServerLevel sl) {
                CameraShakePayload.sendToNearby(sl, target.getX(), target.getY(), target.getZ(), 28.0D, 0.45F, 7);
            }

            currentPierce--;
            if (currentPierce <= 0) {
                this.discard();
                break;
            }
        }
        this.entityData.set(PIERCE, Math.max(0, currentPierce));
    }

    private void applySlashEffect(LivingEntity target, LivingEntity owner) {
        Vec3 move = this.getDeltaMovement().normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(move.x * 0.5D, 0.28D, move.z * 0.5D));
        target.hurtMarked = true;

        switch (getSlashType()) {
            case TYPE_DARK_VOID -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
                this.playSound(SoundEvents.WITHER_SHOOT, 1.2F, 1.4F);
            }
            case TYPE_CRYSTAL_AQUA -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 2, false, true));
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 80, 200));
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 1.4F);
                this.playSound(SoundEvents.GLASS_BREAK, 1.1F, 1.6F);
            }
            case TYPE_SOLAR_LIGHT -> {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true));
                this.playSound(SoundEvents.TRIDENT_HIT, 1.3F, 1.6F);
            }
            case TYPE_CRIMSON_BLOOD -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, true));
                if (owner != null && owner.isAlive()) {
                    owner.heal(getDamage() * 0.15F);
                }
                this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.4F, 0.7F);
            }
            case TYPE_SILVER_ROSE -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4, false, true));
                if (owner != null) {
                    owner.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    owner.removeEffect(MobEffects.WEAKNESS);
                    owner.removeEffect(MobEffects.WITHER);
                }
                this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.4F, 1.3F);
            }
            case TYPE_CHERRY_BLOSSOM -> {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 15, 0, false, true));
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 1.4F);
            }
            case TYPE_COSMIC_AMETHYST -> {
                target.setDeltaMovement(0.0D, 0.75D, 0.0D);
                this.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 1.4F, 1.5F);
            }
            case TYPE_PYRO_FLAME -> {
                target.igniteForSeconds(4);
                this.playSound(SoundEvents.BLAZE_SHOOT, 1.3F, 1.2F);
            }
            case TYPE_FROST_WIND -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, true));
                this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.5F, 0.6F);
            }
            case TYPE_CELESTIAL -> {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 1, false, true));
                if (owner != null && owner.isAlive()) {
                    owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, true));
                }
                this.playSound(SoundEvents.BEACON_ACTIVATE, 1.2F, 1.8F);
                this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.0F, 1.6F);
            }
            case TYPE_PRIMORDIAL_GENESIS -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 2, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true));
                if (owner != null && owner.isAlive()) {
                    owner.heal(getDamage() * 0.20F);
                    owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, true));
                }
                this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.0F, 1.5F);
                this.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.3F, 1.2F);
            }
            default -> this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.2F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult bhr) {
        super.onHitBlock(bhr);
        if (!this.level().isClientSide) {
            this.playSound(SoundEvents.ANVIL_LAND, 0.6F, 1.8F);
            if (this.level() instanceof ServerLevel sl) {
                CameraShakePayload.sendToNearby(sl, this.getX(), this.getY(), this.getZ(), 20.0D, 0.3F, 5);
            }
            this.discard();
        }
    }

    private void spawnClientTrailParticles() {
        Vec3 pos = this.position();
        Vec3 mov = this.getDeltaMovement();
        float w = getSlashWidth();

        Vec3 side = new Vec3(-mov.z, 0, mov.x).normalize().scale(w * 0.45D);

        for (int i = -2; i <= 2; i++) {
            double factor = i / 2.0D;
            double px = pos.x + side.x * factor;
            double py = pos.y + (this.random.nextDouble() - 0.5D) * 0.2D;
            double pz = pos.z + side.z * factor;

            switch (getSlashType()) {
                case TYPE_DARK_VOID -> {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.SQUID_INK, px, py, pz, 0, 0, 0);
                }
                case TYPE_CRYSTAL_AQUA -> {
                    this.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.01D, 0);
                }
                case TYPE_SOLAR_LIGHT -> {
                    this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.03D, 0);
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.01D, 0);
                }
                case TYPE_CRIMSON_BLOOD -> {
                    this.level().addParticle(ParticleTypes.CRIMSON_SPORE, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR, px, py, pz, 0, 0.01D, 0);
                }
                case TYPE_SILVER_ROSE -> {
                    this.level().addParticle(ParticleTypes.CHERRY_LEAVES, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.01D, 0);
                }
                case TYPE_CHERRY_BLOSSOM -> {
                    this.level().addParticle(ParticleTypes.CHERRY_LEAVES, px, py, pz, 0, 0.03D, 0);
                }
                case TYPE_COSMIC_AMETHYST -> {
                    this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0.01D, 0);
                }
                case TYPE_PYRO_FLAME -> {
                    this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.03D, 0);
                }
                case TYPE_FROST_WIND -> {
                    this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, 0.02D, 0);
                }
                case TYPE_CELESTIAL -> {
                    this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.03D, 0);
                    this.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.FIREWORK, px, py, pz, 0, 0.01D, 0);
                }
                case TYPE_PRIMORDIAL_GENESIS -> {
                    this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.03D, 0);
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.02D, 0);
                    this.level().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0.02D, 0);
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("SlashType")) setSlashType(tag.getInt("SlashType"));
        if (tag.contains("Width")) this.entityData.set(WIDTH, tag.getFloat("Width"));
        if (tag.contains("Height")) this.entityData.set(HEIGHT, tag.getFloat("Height"));
        if (tag.contains("Roll")) setRoll(tag.getFloat("Roll"));
        if (tag.contains("Lifetime")) this.entityData.set(LIFETIME, tag.getInt("Lifetime"));
        if (tag.contains("Damage")) this.entityData.set(DAMAGE, tag.getFloat("Damage"));
        if (tag.contains("Pierce")) this.entityData.set(PIERCE, tag.getInt("Pierce"));
        if (tag.contains("IsCross")) setCross(tag.getBoolean("IsCross"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("SlashType", getSlashType());
        tag.putFloat("Width", getSlashWidth());
        tag.putFloat("Height", getSlashHeight());
        tag.putFloat("Roll", getRoll());
        tag.putInt("Lifetime", getLifetime());
        tag.putFloat("Damage", getDamage());
        tag.putInt("Pierce", getPierce());
        tag.putBoolean("IsCross", isCross());
    }
}
