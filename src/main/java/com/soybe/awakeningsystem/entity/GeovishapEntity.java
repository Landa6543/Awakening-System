package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Geovishap (Dragarto Geo de Liyue).
 * Reptil elemental milenario con caparazón pétreo endurecido,
 * embestidas en bola rodante y erupciones de esquirlas de roca.
 */
public class GeovishapEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_ROLLING =
            SynchedEntityData.defineId(GeovishapEntity.class, EntityDataSerializers.BOOLEAN);

    private int rollCooldown = 60;
    private int rollTicks = 0;
    private Vec3 rollDirection = Vec3.ZERO;

    public GeovishapEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§6§lGeovishap"));
        this.setCustomNameVisible(true);
        this.xpReward = 220;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 240.0D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROLLING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isRolling() {
        return this.entityData.get(DATA_ROLLING);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (!this.level().isClientSide && target != null && target.isAlive()) {
            if (isRolling()) {
                rollTicks--;
                this.setDeltaMovement(rollDirection.x, this.getDeltaMovement().y, rollDirection.z);

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.2D, this.getZ(), 6, 0.5D, 0.2D, 0.5D, 0.05D);
                }

                AABB hitBox = this.getBoundingBox().inflate(1.0D);
                for (LivingEntity nearby : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != this && e.isAlive())) {
                    nearby.hurt(this.damageSources().mobAttack(this), 22.0F);
                    nearby.knockback(1.5D, -rollDirection.x, -rollDirection.z);
                }

                if (rollTicks <= 0 || this.horizontalCollision) {
                    this.entityData.set(DATA_ROLLING, false);
                    this.rollCooldown = 70;
                }
            } else {
                if (rollCooldown > 0) {
                    rollCooldown--;
                } else if (this.distanceToSqr(target) > 16.0D && this.distanceToSqr(target) < 256.0D) {
                    this.entityData.set(DATA_ROLLING, true);
                    this.rollTicks = 30;
                    this.rollDirection = target.position().subtract(this.position()).normalize().scale(0.85D);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.playSound(null, this.blockPosition(), SoundEvents.STONE_FALL, SoundSource.HOSTILE, 1.8F, 0.8F);
                    }
                }
            }
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.IRON_GOLEM_DEATH, SoundSource.HOSTILE, 1.6F, 0.8F);
            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 900.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(8000);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 4));
                    }
                }
            }
            if (isTeyvat && SystemItems.PRIMOGEM.get() != null) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 4 + random.nextInt(4)));
            }
        }
    }
}
