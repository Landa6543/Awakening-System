package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
 * Beru (Rey Hormiga / Ant King) - Jefe Supremo de la Mazmorra de la Isla Jeju.
 * Criatura ágil con vuelo instantáneo, cuchillas quitinosas y chirrido ensordecedor.
 */
public class BeruBossEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack1");
    private static final RawAnimation SWING_ANIM = RawAnimation.begin().thenPlay("swing");
    private static final RawAnimation KNEEL_ANIM = RawAnimation.begin().thenLoop("kneel");

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("awakening_system.boss.beru.name"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_12
    ).setDarkenScreen(false);

    private int rushCooldown = 80;
    private int screamCooldown = 140;
    private int aerialDiveCooldown = 180;
    private boolean isDiving = false;

    public BeruBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 450;
        this.setCustomName(Component.translatable("awakening_system.boss.beru.name"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 24.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> target != null && !this.isShadowSoldier()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> this.isShadowSoldier() && target != null && !target.getTags().contains("shadow_soldier")));
    }

    public boolean isShadowSoldier() {
        return this.getTags().contains("shadow_soldier");
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!isShadowSoldier()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (!isShadowSoldier()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 1.0D;
            double py = this.getY() + this.random.nextDouble() * 2.2D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.0D;
            this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.05D, 0);

            if (isShadowSoldier()) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.03D, 0);
            }
        }

        if (this.level() instanceof ServerLevel sl) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);
                boolean enraged = this.getHealth() < this.getMaxHealth() * 0.5F;

                // 1. Chillido Desgarrador de Rey Hormiga
                if (screamCooldown > 0) screamCooldown--;
                if (screamCooldown <= 0 && distSq <= 100.0D) {
                    screamCooldown = enraged ? 100 : 160;
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            ModSounds.COMPANION_BERU_SKILL.get(), SoundSource.HOSTILE, 1.8F, 1.0F);
                    sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.6D, this.getZ(), 1, 0, 0, 0, 0);

                    List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D),
                            e -> e != this && (!isShadowSoldier() || (!e.getTags().contains("shadow_soldier") && !(e instanceof Player))));
                    for (LivingEntity victim : nearby) {
                        victim.hurt(this.damageSources().magic(), 15.0F);
                        victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    }
                }

                // 2. Embestida supersónica (Sonic Rush)
                if (rushCooldown > 0) rushCooldown--;
                if (rushCooldown <= 0 && distSq >= 16.0D && distSq <= 225.0D) {
                    rushCooldown = enraged ? 60 : 90;
                    Vec3 dir = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(dir.x * 2.2D, 0.35D, dir.z * 2.2D);
                    this.hasImpulse = true;

                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.6F, 1.5F);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.2D, this.getZ(), 12, 1.0D, 0.4D, 1.0D, 0.1D);

                    target.hurt(this.damageSources().mobAttack(this), enraged ? 25.0F : 20.0F);
                }

                // 3. Salto y picado aéreo
                if (aerialDiveCooldown > 0) aerialDiveCooldown--;
                if (aerialDiveCooldown <= 0 && distSq <= 144.0D) {
                    aerialDiveCooldown = 200;
                    this.setDeltaMovement(0.0D, 1.6D, 0.0D);
                    this.hasImpulse = true;
                    isDiving = true;
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 2.0F, 1.2F);
                }

                if (isDiving && this.getDeltaMovement().y < -0.2D) {
                    isDiving = false;
                    sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.4F);
                    sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 3, 1.0D, 0.2D, 1.0D, 0.05D);
                    target.hurt(this.damageSources().mobAttack(this), 24.0F);
                }
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level() instanceof ServerLevel sl && !isShadowSoldier()) {
            if (SystemItems.AWAKENING_COIN != null && SystemItems.AWAKENING_COIN.isBound()) {
                sl.addFreshEntity(new ItemEntity(sl, this.getX(), this.getY() + 0.5D, this.getZ(),
                        SystemItems.coin(75)));
            }

            Player killer = (damageSource.getEntity() instanceof Player p) ? p : null;
            if (killer != null) {
                com.soybe.awakeningsystem.SystemData kData = killer.getData(AwakeningSystemMod.PLAYER_SYSTEM);
                if (kData != null && ((kData.hasHunterClass() && "shadow_monarch".equals(kData.hunterClass())) || kData.skills().containsKey("unique_absolute_extraction"))) {
                    ShadowSoulEntity soul = new ShadowSoulEntity(sl, this.getX(), this.getY(), this.getZ(), this, killer.getUUID());
                    soul.setMobNameComponent(Component.translatable("awakening_system.mob.beru_ant_king"));
                    sl.addFreshEntity(soul);
                }
            }

            if (killer instanceof ServerPlayer sp) {
                AwakeningSystemMod.notify(sp, "awakening_system.notification.beru_defeated.title",
                        "awakening_system.notification.beru_defeated.desc", 0xFF9933FF);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (this.isDiving || !this.onGround()) {
                return state.setAndContinue(FLY_ANIM);
            }
            if (state.isMoving()) {
                return state.setAndContinue(WALK_ANIM);
            }
            if (this.isShadowSoldier() && this.getTarget() == null) {
                return state.setAndContinue(KNEEL_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));

        controllers.add(new AnimationController<>(this, "attack", 5, state -> {
            if (this.rushCooldown > 70) {
                return state.setAndContinue(ATTACK_ANIM);
            }
            if (this.screamCooldown > 120) {
                return state.setAndContinue(SWING_ANIM);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.COMPANION_BERU_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.COMPANION_BERU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ANT_SOLDIER_DEATH.get();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
