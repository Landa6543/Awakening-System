package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Espectro Elemental (Specter) de Genshin Impact.
 * Criatura flotante nacida de concentraciones elementales de alta densidad.
 * Soporta los 7 elementos: Anemo, Geo, Electro, Dendro, Hydro, Pyro, Cryo.
 * Posee medidor de Furia que se llena al recibir daño; al detonar al morir libera una explosión elemental.
 */
public class SpecterEntity extends Monster {

    public enum Element {
        ANEMO("anemo", "§aAnemo", 0x55ff99),
        GEO("geo", "§6Geo", 0xe6b800),
        ELECTRO("electro", "§dElectro", 0xb366ff),
        DENDRO("dendro", "§2Dendro", 0x33cc33),
        HYDRO("hydro", "§9Hydro", 0x3399ff),
        PYRO("pyro", "§cPyro", 0xff4422),
        CRYO("cryo", "§bCryo", 0x80ffff);

        private final String id;
        private final String displayName;
        private final int color;

        Element(String id, String displayName, int color) {
            this.id = id;
            this.displayName = displayName;
            this.color = color;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }

        public static Element byOrdinal(int ordinal) {
            Element[] values = values();
            if (ordinal < 0 || ordinal >= values.length) return ANEMO;
            return values[ordinal];
        }
    }

    private static final EntityDataAccessor<Integer> ELEMENT =
            SynchedEntityData.defineId(SpecterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FURY =
            SynchedEntityData.defineId(SpecterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED =
            SynchedEntityData.defineId(SpecterEntity.class, EntityDataSerializers.BOOLEAN);

    private int attackCooldown = 40;
    private int deathTimer = 0;
    private boolean isDying = false;

    public SpecterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SpecterMoveControl(this);
        this.xpReward = 65;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELEMENT, 0);
        builder.define(FURY, 0);
        builder.define(ENRAGED, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 85.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public Element getElement() {
        return Element.byOrdinal(this.entityData.get(ELEMENT));
    }

    public void setElement(Element element) {
        this.entityData.set(ELEMENT, element.ordinal());
        updateName();
    }

    public int getFury() {
        return this.entityData.get(FURY);
    }

    public void setFury(int fury) {
        int clamped = Math.max(0, Math.min(100, fury));
        this.entityData.set(FURY, clamped);
        if (clamped >= 100 && !isEnraged()) {
            setEnraged(true);
            if (!this.level().isClientSide) {
                this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.2F, 1.4F);
            }
        }
    }

    public boolean isEnraged() {
        return this.entityData.get(ENRAGED);
    }

    public void setEnraged(boolean enraged) {
        this.entityData.set(ENRAGED, enraged);
        updateName();
    }

    private void updateName() {
        Element el = getElement();
        String enragedPrefix = isEnraged() ? "§4[Furia] " : "";
        this.setCustomName(Component.literal(enragedPrefix + el.getDisplayName() + " §lEspectro"));
        this.setCustomNameVisible(true);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        setElement(Element.byOrdinal(this.random.nextInt(Element.values().length)));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SpecterFloatGoal(this));
        this.goalSelector.addGoal(2, new SpecterAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            // Emisión de partículas elementales
            Element el = getElement();
            if (this.random.nextFloat() < (isEnraged() ? 0.6F : 0.25F)) {
                switch (el) {
                    case PYRO -> this.level().addParticle(ParticleTypes.FLAME, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0.05, 0);
                    case CRYO -> this.level().addParticle(ParticleTypes.SNOWFLAKE, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, -0.02, 0);
                    case ELECTRO -> this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
                    case DENDRO -> this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0.02, 0);
                    case HYDRO -> this.level().addParticle(ParticleTypes.FALLING_WATER, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
                    case GEO -> this.level().addParticle(ParticleTypes.CRIT, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0, 0);
                    case ANEMO -> this.level().addParticle(ParticleTypes.CLOUD, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0, 0.03, 0);
                }
            }
        } else {
            if (isDying) {
                deathTimer++;
                if (deathTimer >= 20) {
                    detonateDeathExplosion();
                    this.discard();
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDying) return false;
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            setFury(getFury() + (int)(amount * 4.0F) + 15);
        }
        return result;
    }

    @Override
    public void die(DamageSource cause) {
        if (!isDying && !this.level().isClientSide) {
            isDying = true;
            deathTimer = 0;
            this.setNoAi(true);
            this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.2F, 1.5F);
            return;
        }
        super.die(cause);
    }

    private void detonateDeathExplosion() {
        if (this.level() instanceof ServerLevel sLevel) {
            Element el = getElement();
            sLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.0F);

            // Efecto AoE por elemento
            AABB area = this.getBoundingBox().inflate(4.0D);
            for (LivingEntity target : sLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                if (target == this) continue;
                target.hurt(sLevel.damageSources().mobAttack(this), isEnraged() ? 24.0F : 14.0F);

                switch (el) {
                    case PYRO -> target.setRemainingFireTicks(100);
                    case CRYO -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    case ELECTRO -> {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sLevel);
                        if (bolt != null) {
                            bolt.moveTo(target.position());
                            bolt.setVisualOnly(true);
                            sLevel.addFreshEntity(bolt);
                        }
                    }
                    case DENDRO -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                    case HYDRO -> {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }
                    case ANEMO -> {
                        Vec3 push = target.position().subtract(this.position()).normalize().scale(1.8D);
                        target.push(push.x, 0.6D, push.z);
                    }
                    case GEO -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                }
            }

            // Drops
            Player nearestPlayer = sLevel.getNearestPlayer(this, 24.0D);
            if (nearestPlayer instanceof ServerPlayer sp) {
                SystemEvents.data(sp).restoreMana(isEnraged() ? 35 : 20);
            }

            ItemStack dropItem = new ItemStack(SystemItems.CRYSTAL_CORE.get(), 1);
            this.spawnAtLocation(dropItem);
        }
    }

    public void performElementalAttack(LivingEntity target) {
        if (this.level() instanceof ServerLevel sLevel) {
            Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            Element el = getElement();

            SmallFireball projectile = new SmallFireball(sLevel, this, dir.scale(0.8D));
            projectile.setPos(getX() + dir.x * 0.8D, getY() + 0.6D, getZ() + dir.z * 0.8D);
            sLevel.addFreshEntity(projectile);

            sLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 1.2F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ElementOrdinal", this.entityData.get(ELEMENT));
        compound.putInt("Fury", this.entityData.get(FURY));
        compound.putBoolean("Enraged", this.entityData.get(ENRAGED));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ElementOrdinal")) {
            setElement(Element.byOrdinal(compound.getInt("ElementOrdinal")));
        }
        if (compound.contains("Fury")) {
            setFury(compound.getInt("Fury"));
        }
        if (compound.contains("Enraged")) {
            setEnraged(compound.getBoolean("Enraged"));
        }
    }

    // --- Control de Vuelo y Objetivos ---

    static class SpecterMoveControl extends MoveControl {
        private final SpecterEntity specter;
        private int floatDuration;

        public SpecterMoveControl(SpecterEntity specter) {
            super(specter);
            this.specter = specter;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.specter.getRandom().nextInt(5) + 2;
                    Vec3 vec3 = new Vec3(this.wantedX - this.specter.getX(), this.wantedY - this.specter.getY(), this.wantedZ - this.specter.getZ());
                    double d0 = vec3.length();
                    vec3 = vec3.normalize();
                    if (d0 > 0.5D) {
                        this.specter.setDeltaMovement(this.specter.getDeltaMovement().add(vec3.scale(0.1D)));
                    }
                }
            }
        }
    }

    static class SpecterFloatGoal extends Goal {
        private final SpecterEntity specter;

        public SpecterFloatGoal(SpecterEntity specter) {
            this.specter = specter;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.specter.getMoveControl().hasWanted() && this.specter.getRandom().nextInt(7) == 0;
        }

        @Override
        public void tick() {
            BlockPos targetPos = this.specter.blockPosition().offset(
                    this.specter.getRandom().nextInt(15) - 7,
                    this.specter.getRandom().nextInt(7) - 3,
                    this.specter.getRandom().nextInt(15) - 7
            );
            if (this.specter.level().isEmptyBlock(targetPos)) {
                this.specter.getMoveControl().setWantedPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 0.25D);
            }
        }
    }

    static class SpecterAttackGoal extends Goal {
        private final SpecterEntity specter;
        private int attackTimer = 0;

        public SpecterAttackGoal(SpecterEntity specter) {
            this.specter = specter;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.specter.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.specter.getTarget();
            if (target == null) return;

            this.specter.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.specter.distanceToSqr(target);

            // Flotar a media distancia
            if (distSq > 100.0D) {
                this.specter.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2.5D, target.getZ(), 0.35D);
            } else if (distSq < 16.0D) {
                Vec3 away = this.specter.position().subtract(target.position()).normalize().scale(5.0D);
                this.specter.getMoveControl().setWantedPosition(this.specter.getX() + away.x, this.specter.getY() + 1.5D, this.specter.getZ() + away.z, 0.35D);
            }

            int requiredCooldown = this.specter.isEnraged() ? 25 : 45;
            if (++attackTimer >= requiredCooldown) {
                attackTimer = 0;
                this.specter.performElementalAttack(target);
            }
        }
    }
}
