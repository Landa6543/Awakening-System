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

/**
 * Thunderhelm Lawachurl (Cacique Mitachurl Acorazado de Relámpago de Inazuma).
 * Monstruo colosal acorazado con manto Electro, estallidos de rayos y embestidas de tormenta.
 */
public class ThunderhelmLawachurlEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_SHIELDED =
            SynchedEntityData.defineId(ThunderhelmLawachurlEntity.class, EntityDataSerializers.BOOLEAN);

    private float electroShield = 130.0F;
    private int slamCooldown = 60;

    public ThunderhelmLawachurlEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§d§lThunderhelm Lawachurl"));
        this.setCustomNameVisible(true);
        this.xpReward = 280;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 340.0D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 30.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHIELDED, true);
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

    public boolean hasElectroShield() {
        return this.entityData.get(DATA_SHIELDED);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (!this.level().isClientSide && target != null && target.isAlive()) {
            if (slamCooldown > 0) {
                slamCooldown--;
            } else if (this.distanceToSqr(target) <= 64.0D) {
                executeThunderSlam(target);
            }
        }
    }

    private void executeThunderSlam(LivingEntity target) {
        this.slamCooldown = 75;
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 2.0F, 1.2F);
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5D, this.getZ(), 40, 1.5D, 0.5D, 1.5D, 0.1D);

        AABB area = this.getBoundingBox().inflate(3.5D, 1.5D, 3.5D);
        for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != this && e.isAlive())) {
            nearby.hurt(this.damageSources().mobAttack(this), 26.0F);
            nearby.knockback(1.4D, nearby.getX() - this.getX(), nearby.getZ() - this.getZ());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && hasElectroShield()) {
            boolean pyroOrCryo = source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                    || source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING);
            this.electroShield -= pyroOrCryo ? amount * 2.2F : amount * 0.4F;

            if (this.electroShield <= 0.0F) {
                this.electroShield = 0.0F;
                this.entityData.set(DATA_SHIELDED, false);
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
                }
            }
            amount *= 0.4F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_DEATH, SoundSource.HOSTILE, 2.0F, 0.8F);
            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 900.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(12000);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 6));
                    }
                }
            }
            if (isTeyvat && SystemItems.PRIMOGEM.get() != null) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 5 + random.nextInt(5)));
            }
        }
    }
}
