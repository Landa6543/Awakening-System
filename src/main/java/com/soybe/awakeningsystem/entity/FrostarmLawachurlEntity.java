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

import java.util.List;

/**
 * Frostarm Lawachurl (Cacique Mitachurl Acorazado de Hielo de Espinadragón).
 * Monstruo colosal acorazado con manto de hielo Cryo, saltos destructores y ráfagas de escarcha.
 */
public class FrostarmLawachurlEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_SHIELDED =
            SynchedEntityData.defineId(FrostarmLawachurlEntity.class, EntityDataSerializers.BOOLEAN);

    private float cryoShield = 130.0F;
    private int slamCooldown = 60;

    public FrostarmLawachurlEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§b§lFrostarm Lawachurl"));
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

    public boolean hasCryoShield() {
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
                executeFrostSlam(target);
            }
        }
    }

    private void executeFrostSlam(LivingEntity target) {
        this.slamCooldown = 75;
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 1.2F);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.5D, this.getZ(), 40, 1.5D, 0.5D, 1.5D, 0.1D);

        AABB area = this.getBoundingBox().inflate(3.5D, 1.5D, 3.5D);
        for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != this && e.isAlive())) {
            nearby.hurt(this.damageSources().mobAttack(this), 26.0F);
            nearby.knockback(1.4D, nearby.getX() - this.getX(), nearby.getZ() - this.getZ());
            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && hasCryoShield()) {
            boolean pyro = source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);
            this.cryoShield -= pyro ? amount * 2.5F : amount * 0.4F;

            if (this.cryoShield <= 0.0F) {
                this.cryoShield = 0.0F;
                this.entityData.set(DATA_SHIELDED, false);
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
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
