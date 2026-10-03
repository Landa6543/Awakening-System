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

import java.util.List;

/**
 * Demoledor de las Ruinas (Ruin Grader).
 * Colosal autómata pesado de Khaenri'ah armado con rayo láser térmico ocular,
 * pisotones sísmicos dobles y embestidas devastadoras de carga.
 */
public class RuinGraderEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_STUNNED =
            SynchedEntityData.defineId(RuinGraderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FIRING_LASER =
            SynchedEntityData.defineId(RuinGraderEntity.class, EntityDataSerializers.BOOLEAN);

    private int attackCooldown = 40;
    private int stunTimer = 0;
    private int laserTicks = 0;

    public RuinGraderEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("§6§lDemoledor de las Ruinas"));
        this.setCustomNameVisible(true);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 420.0D)
                .add(Attributes.ATTACK_DAMAGE, 26.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STUNNED, false);
        builder.define(DATA_FIRING_LASER, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isStunned() {
        return this.entityData.get(DATA_STUNNED);
    }

    public boolean isFiringLaser() {
        return this.entityData.get(DATA_FIRING_LASER);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (isStunned()) {
                stunTimer--;
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                if (stunTimer <= 0) {
                    this.entityData.set(DATA_STUNNED, false);
                }
                return;
            }

            if (isFiringLaser()) {
                laserTicks--;
                if (laserTicks <= 0) {
                    this.entityData.set(DATA_FIRING_LASER, false);
                }
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (attackCooldown > 0) {
                    attackCooldown--;
                } else {
                    executeGraderAttack(target);
                }
            }
        }
    }

    private void executeGraderAttack(LivingEntity target) {
        this.attackCooldown = 55;
        if (!(this.level() instanceof ServerLevel sl)) return;

        double distSq = this.distanceToSqr(target);

        if (distSq > 36.0D) {
            // Láser ocular frontal continuo
            this.entityData.set(DATA_FIRING_LASER, true);
            this.laserTicks = 30;
            sl.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 1.2F);

            Vec3 dir = target.position().subtract(this.position()).normalize();
            for (double d = 2.0D; d <= 20.0D; d += 0.8D) {
                Vec3 p = this.position().add(dir.scale(d)).add(0, 3.2D, 0);
                sl.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 3, 0.05D, 0.05D, 0.05D, 0.02D);
                sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
            }

            if (distSq <= 400.0D) {
                target.hurt(this.damageSources().mobAttack(this), 24.0F);
                target.setRemainingFireTicks(60);
            }
        } else {
            // Pisotón sísmico doble
            sl.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.5F, 0.7F);
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 4, 1.0D, 0.3D, 1.0D, 0.0D);

            AABB stompArea = this.getBoundingBox().inflate(3.5D, 1.5D, 3.5D);
            for (LivingEntity nearby : sl.getEntitiesOfClass(LivingEntity.class, stompArea, e -> e != this && e.isAlive())) {
                nearby.hurt(this.damageSources().mobAttack(this), 28.0F);
                nearby.knockback(1.5D, nearby.getX() - this.getX(), nearby.getZ() - this.getZ());
                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !isStunned()) {
            if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
                // Tiro al núcleo ocular / piernas aturde al Demoledor
                if (this.random.nextFloat() < 0.25F) {
                    this.entityData.set(DATA_STUNNED, true);
                    this.stunTimer = 140; // 7 segundos de aturdimiento
                    if (this.level() instanceof ServerLevel sl) {
                        sl.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 2.0F, 0.8F);
                        sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 3.0D, this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
                    }
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.5F, 0.8F);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0D, this.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);

            boolean isTeyvat = sl.dimension().equals(com.soybe.awakeningsystem.TeyvatWorldManager.TEYVAT_WORLD);
            for (ServerPlayer sp : sl.getPlayers(p -> p.distanceToSqr(this) < 1600.0D)) {
                var data = SystemEvents.data(sp);
                if (isTeyvat) {
                    data.addMora(35000);
                    data.addPrimogems(15);
                    com.soybe.awakeningsystem.AwakeningSystemMod.sync(sp);
                } else {
                    if (SystemItems.AWAKENING_COIN.isBound()) {
                        sp.addItem(new ItemStack(SystemItems.AWAKENING_COIN.get(), 12));
                    }
                }
            }

            if (SystemItems.CHAOS_CORE.isBound()) {
                this.spawnAtLocation(new ItemStack(SystemItems.CHAOS_CORE.get(), 1 + random.nextInt(2)));
            }
            if (isTeyvat && SystemItems.PRIMOGEM.get() != null) {
                this.spawnAtLocation(new ItemStack(SystemItems.PRIMOGEM.get(), 10 + random.nextInt(10)));
            }
        }
    }
}
