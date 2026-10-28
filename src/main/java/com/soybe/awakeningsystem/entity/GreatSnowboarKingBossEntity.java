package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Jefe de Campo de Teyvat: El Gran Rey Jabalí de las Nieves (The Great Snowboar King).
 * Soberano prehistórico preservado en el permafrost de Espinadragón.
 * Posee feroces embestidas de carga, patadas sísmicas de hielo y furia berserk helada.
 */
public class GreatSnowboarKingBossEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(GreatSnowboarKingBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED =
            SynchedEntityData.defineId(GreatSnowboarKingBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("boss.awakening_system.snowboar_king.boss_bar"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private int chargeCooldown = 60;
    private int chargeTicks = 0;
    private Vec3 chargeDirection = Vec3.ZERO;

    public GreatSnowboarKingBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable("boss.awakening_system.snowboar_king.name"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.xpReward = 750;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3600.0D)
                .add(Attributes.ATTACK_DAMAGE, 32.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGING, false);
        builder.define(DATA_ENRAGED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (!isEnraged() && (this.getHealth() / this.getMaxHealth()) <= 0.35F) {
                this.entityData.set(DATA_ENRAGED, true);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, 1));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 1));
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.5F, 0.7F);
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 2.5D, this.getZ(), 20, 1.0D, 1.0D, 1.0D, 0.1D);
                }
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (isCharging()) {
                    chargeTicks--;
                    this.setDeltaMovement(chargeDirection.x, this.getDeltaMovement().y, chargeDirection.z);

                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.5D, this.getZ(), 10, 0.8D, 0.3D, 0.8D, 0.05D);
                    }

                    AABB hitBox = this.getBoundingBox().inflate(1.2D);
                    for (LivingEntity nearby : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != this && e.isAlive())) {
                        nearby.hurt(this.damageSources().mobAttack(this), isEnraged() ? 34.0F : 26.0F);
                        nearby.knockback(1.8D, -chargeDirection.x, -chargeDirection.z);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }

                    if (chargeTicks <= 0 || this.horizontalCollision) {
                        this.entityData.set(DATA_CHARGING, false);
                        this.chargeCooldown = isEnraged() ? 35 : 55;
                        if (this.horizontalCollision && this.level() instanceof ServerLevel sl) {
                            sl.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0F, 0.8F);
                            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
                        }
                    }
                } else {
                    if (chargeCooldown > 0) {
                        chargeCooldown--;
                    } else if (this.distanceToSqr(target) > 16.0D && this.distanceToSqr(target) < 400.0D) {
                        this.entityData.set(DATA_CHARGING, true);
                        this.chargeTicks = 35;
                        double speed = isEnraged() ? 0.95D : 0.75D;
                        this.chargeDirection = target.position().subtract(this.position()).normalize().scale(speed);
                        if (this.level() instanceof ServerLevel sl) {
                            sl.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 2.0F, 0.8F);
                        }
                    }
                }
            }
        } else {
            if (this.random.nextFloat() < 0.25F) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 2.0D,
                        this.getY() + this.random.nextDouble() * 2.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 2.0D,
                        0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_DEATH, SoundSource.HOSTILE, 2.5F, 0.8F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(55000);
                    data.addPrimogems(30);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.snowboar_king.defeated_teyvat"), false);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 20));
                    }
                    sp.displayClientMessage(Component.translatable("boss.awakening_system.snowboar_king.defeated_vanilla"), false);
                }
            }

            this.spawnAtLocation(new ItemStack(Items.PORKCHOP, 12 + this.random.nextInt(8)));
            if (isTeyvat && SystemItems.PRIMOGEM.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 25 + this.random.nextInt(10)));
            }
            this.spawnAtLocation(new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2));

            if (SystemItems.SHIVADA_JADE_GEMSTONE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.SHIVADA_JADE_GEMSTONE.get(), 1));
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }
}
