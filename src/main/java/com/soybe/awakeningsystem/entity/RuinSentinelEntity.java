package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Centinela de las Ruinas de Khaenri'ah.
 * Cuatro autómatas biomecánicos autónomos: Cruiser, Defender, Destroyer y Scout.
 */
public class RuinSentinelEntity extends Monster {

    public enum SentinelType {
        CRUISER("Centinela de las Ruinas: Patrullero", 75.0D, 14.0D, 0.28D),
        DEFENDER("Centinela de las Ruinas: Defensor", 95.0D, 12.0D, 0.22D),
        DESTROYER("Centinela de las Ruinas: Destructor", 85.0D, 16.0D, 0.26D),
        SCOUT("Centinela de las Ruinas: Explorador", 70.0D, 14.0D, 0.30D);

        public final String title;
        public final double maxHp;
        public final double damage;
        public final double speed;

        SentinelType(String title, double maxHp, double damage, double speed) {
            this.title = title;
            this.maxHp = maxHp;
            this.damage = damage;
            this.speed = speed;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(RuinSentinelEntity.class, EntityDataSerializers.INT);

    private int attackCooldown = 30;

    public RuinSentinelEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 180;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 85.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, 0);
    }

    public SentinelType getSentinelType() {
        int idx = this.entityData.get(DATA_TYPE);
        SentinelType[] values = SentinelType.values();
        return values[Math.max(0, Math.min(idx, values.length - 1))];
    }

    public void setSentinelType(SentinelType type) {
        this.entityData.set(DATA_TYPE, type.ordinal());
        this.setCustomName(Component.literal("§6" + type.title));
        this.setCustomNameVisible(true);
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(type.maxHp);
            this.setHealth((float) type.maxHp);
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(type.damage);
        }
        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(type.speed);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (!this.level().isClientSide && target != null && target.isAlive()) {
            if (attackCooldown > 0) {
                attackCooldown--;
            } else {
                executeSpecialAttack(target);
            }
        }
    }

    private void executeSpecialAttack(LivingEntity target) {
        this.attackCooldown = 50;
        if (!(this.level() instanceof ServerLevel sl)) return;

        SentinelType type = getSentinelType();
        switch (type) {
            case CRUISER -> {
                // Ráfaga a distancia de energía ruina
                sl.playSound(null, this.blockPosition(), SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.5F, 1.4F);
                Vec3 dir = target.position().subtract(this.position()).normalize();
                for (double d = 1.0D; d <= 12.0D; d += 0.8D) {
                    Vec3 p = this.position().add(dir.scale(d)).add(0, 0.8D, 0);
                    sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                }
                if (this.distanceToSqr(target) <= 144.0D) {
                    target.hurt(this.damageSources().mobAttack(this), 16.0F);
                }
            }
            case DEFENDER -> {
                // Embestida con escudo
                sl.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.6F, 0.8F);
                sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 0.8D, this.getZ(), 1, 0, 0, 0, 0);
            }
            case DESTROYER -> {
                // Taladro sísmico de suelo
                sl.playSound(null, target.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.8F, 0.9F);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, target.getX(), target.getY(), target.getZ(), 15, 0.5D, 0.2D, 0.5D, 0.05D);
                if (this.distanceToSqr(target) <= 64.0D) {
                    target.hurt(this.damageSources().mobAttack(this), 18.0F);
                    target.knockback(0.8D, this.getX() - target.getX(), this.getZ() - target.getZ());
                }
            }
            case SCOUT -> {
                // Onda de choque estática
                sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.2F, 1.8F);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.8D, this.getZ(), 20, 1.0D, 0.5D, 1.0D, 0.1D);
                if (this.distanceToSqr(target) <= 49.0D) {
                    target.hurt(this.damageSources().lightningBolt(), 14.0F);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getSentinelType() == SentinelType.DEFENDER) {
            // Defensor bloquea gran parte del daño frontal
            amount *= 0.55F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.6F, 1.2F);
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.6D, this.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SentinelType", this.entityData.get(DATA_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SentinelType")) {
            setSentinelType(SentinelType.values()[Math.max(0, Math.min(tag.getInt("SentinelType"), SentinelType.values().length - 1))]);
        }
    }
}
