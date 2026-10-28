package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Estatua del Dios Ancestral (Statue of God) - Jefe del Templo Cartenon (Doble Mazmorra).
 * Monstruo colosal inmóvil sobre su trono que vigila a los cazadores.
 * Dispara rayos oculares letales y sonríe siniestramente cuando los cazadores incumplen sus mandamientos.
 */
public class StatueOfGodEntity extends Monster implements GeoEntity {
    public static final EntityDataAccessor<Boolean> IS_SMILING =
            SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_FIRING_LASER =
            SynchedEntityData.defineId(StatueOfGodEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation SIT_ANIM = RawAnimation.begin().thenLoop("animation.statue_of_god.sit");
    private static final RawAnimation SMILE_ANIM = RawAnimation.begin().thenLoop("animation.statue_of_god.smile");
    private static final RawAnimation LASER_ANIM = RawAnimation.begin().thenPlay("animation.statue_of_god.laser_bean");

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.statue_of_god.name"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_20
    ).setDarkenScreen(true);

    private int laserCooldown = 80;
    private int stompCooldown = 140;

    public StatueOfGodEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setCustomName(Component.translatable("awakening_system.boss.statue_of_god.name"));
        this.setCustomNameVisible(true);
        this.setNoAi(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 35.0D)
                .add(Attributes.ARMOR, 28.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_SMILING, false);
        builder.define(IS_FIRING_LASER, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 40.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // La estatua permanece 100% fija e inmóvil sobre su trono de piedra ancestral
        super.travel(Vec3.ZERO);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Completamente inmune a retroceso y empuje
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

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Asegurar inmovilidad total del trono en cada tick
        this.setDeltaMovement(0, this.onGround() ? 0 : this.getDeltaMovement().y, 0);
        this.xxa = 0;
        this.yya = 0;
        this.zza = 0;

        // Partículas atmosféricas y de ojos carmesí
        if (this.level().isClientSide) {
            Vec3 eyePos = this.position().add(0, 9.5D, 0);
            this.level().addParticle(ParticleTypes.CRIMSON_SPORE, eyePos.x + (this.random.nextDouble() - 0.5D) * 1.5D,
                    eyePos.y + (this.random.nextDouble() - 0.5D) * 1.0D,
                    eyePos.z + (this.random.nextDouble() - 0.5D) * 1.5D, 0, 0.02D, 0);
        }

        if (this.level() instanceof ServerLevel sl) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                this.entityData.set(IS_SMILING, true);

                if (laserCooldown > 0) laserCooldown--;
                if (laserCooldown <= 0) {
                    laserCooldown = 100;
                    fireEyeLaser(sl, target);
                }

                if (stompCooldown > 0) stompCooldown--;
                if (stompCooldown <= 0 && this.distanceToSqr(target) <= 100.0D) {
                    stompCooldown = 120;
                    triggerDivineQuake(sl);
                }
            } else {
                if (this.entityData.get(IS_SMILING)) {
                    this.entityData.set(IS_SMILING, false);
                }
                if (this.entityData.get(IS_FIRING_LASER)) {
                    this.entityData.set(IS_FIRING_LASER, false);
                }
            }
        }
    }

    private void fireEyeLaser(ServerLevel sl, LivingEntity target) {
        this.entityData.set(IS_FIRING_LASER, true);
        sl.playSound(null, this.getX(), this.getY() + 9.5D, this.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.5F, 0.5F);
        sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.8F, 1.4F);

        Vec3 from = this.position().add(0, 9.5D, 0);
        Vec3 to = target.position().add(0, 1.0D, 0);
        Vec3 dir = to.subtract(from).normalize();
        double dist = from.distanceTo(to);

        // Rayo de partículas de plasma carmesí
        for (double d = 1.0D; d < dist; d += 0.8D) {
            Vec3 p = from.add(dir.scale(d));
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.1D, 0.1D, 0.1D, 0.01D);
            sl.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }

        target.hurt(this.damageSources().magic(), 32.0F);
        target.setRemainingFireTicks(120);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
    }

    private void triggerDivineQuake(ServerLevel sl) {
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);

        List<LivingEntity> victims = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0D),
                e -> e != this && !(e instanceof StatueOfGodEntity));
        for (LivingEntity v : victims) {
            v.hurt(this.damageSources().mobAttack(this), 22.0F);
            v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            Vec3 rep = v.position().subtract(this.position()).normalize().scale(1.5D);
            v.setDeltaMovement(rep.x, 0.6D, rep.z);
        }
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.5D, this.getZ(), 4, 3.0D, 0.2D, 3.0D, 0.1D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.entityData.get(IS_SMILING)) {
            this.entityData.set(IS_SMILING, true);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl) {
            // Recompensas supremas del Cartenon Temple
            if (SystemItems.AWAKENING_COIN != null && SystemItems.AWAKENING_COIN.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 1.0D, this.getZ(),
                        SystemItems.coin(100)));
            }

            Player killer = (damageSource.getEntity() instanceof Player p) ? p : null;
            if (killer instanceof ServerPlayer sp) {
                AwakeningSystemMod.notify(sp, "awakening_system.notification.statue_of_god_defeated.title",
                        "awakening_system.notification.statue_of_god_defeated.desc", 0xFFFFD700);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "posture", 10, state -> {
            if (this.entityData.get(IS_FIRING_LASER)) {
                return state.setAndContinue(LASER_ANIM);
            }
            if (this.entityData.get(IS_SMILING)) {
                return state.setAndContinue(SMILE_ANIM);
            }
            return state.setAndContinue(SIT_ANIM);
        }));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BEACON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STONE_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
