package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Araña Madre (Mother Spider) - Variante Normal / Base.
 * Criatura arácnida predadora ágil y letal que puede trepar muros, abalanzarse
 * en combate cuerpo a cuerpo y disparar proyectiles de telaraña tóxicos a distancia.
 */
public class MotherSpiderEntity extends Spider implements GeoEntity {

    public static final EntityDataAccessor<Boolean> IS_BOSS =
            SynchedEntityData.defineId(MotherSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(MotherSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SHOOTING_WEB =
            SynchedEntityData.defineId(MotherSpiderEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTicks = 0;
    private int shootWebAnimationTicks = 0;
    private int webShootCooldown = 60;

    public MotherSpiderEntity(EntityType<? extends MotherSpiderEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_BOSS, false);
        builder.define(IS_ATTACKING, false);
        builder.define(IS_SHOOTING_WEB, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MotherSpiderShootWebGoal(this));
        this.goalSelector.addGoal(3, new MotherSpiderMeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isBoss() {
        return this.entityData.get(IS_BOSS);
    }

    public void setBoss(boolean boss) {
        this.entityData.set(IS_BOSS, boss);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public boolean isShootingWeb() {
        return this.entityData.get(IS_SHOOTING_WEB);
    }

    public void setShootingWeb(boolean shooting) {
        this.entityData.set(IS_SHOOTING_WEB, shooting);
    }

    public float getSpiderScale() {
        return isBoss() ? 2.2F : 1.0F;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
            if (this.attackAnimationTicks == 0 && !this.level().isClientSide()) {
                setAttacking(false);
            }
        }

        if (this.shootWebAnimationTicks > 0) {
            this.shootWebAnimationTicks--;
            // En el tick 45 (aproximadamente a la mitad de la animación de 70 ticks), disparar la telaraña
            if (this.shootWebAnimationTicks == 45 && !this.level().isClientSide()) {
                performWebShot();
            }
            if (this.shootWebAnimationTicks == 0 && !this.level().isClientSide()) {
                setShootingWeb(false);
            }
        }

        if (this.webShootCooldown > 0) {
            this.webShootCooldown--;
        }
    }

    public void triggerAttackAnimation() {
        this.attackAnimationTicks = 20; // 1.0s * 20tps
        setAttacking(true);
    }

    public void triggerWebShootAnimation() {
        this.shootWebAnimationTicks = 70; // 3.5s * 20tps
        this.webShootCooldown = isBoss() ? 90 : 130;
        setShootingWeb(true);
    }

    public boolean canShootWeb() {
        return this.webShootCooldown <= 0 && this.shootWebAnimationTicks <= 0 && this.attackAnimationTicks <= 0;
    }

    protected void performWebShot() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.33D) - this.getY(0.65D);
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float dmg = isBoss() ? 12.0F : 6.0F;
        SpiderWebProjectileEntity web = new SpiderWebProjectileEntity(this.level(), this, dmg, isBoss());
        web.setPos(this.getX(), this.getY(0.65D), this.getZ());
        web.shoot(dx, dy + dist * 0.12D, dz, 1.4F, 2.0F);

        this.level().addFreshEntity(web);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.MOTHER_SPIDER_SHOOT_WEB.get(), SoundSource.HOSTILE, 1.4F, isBoss() ? 0.85F : 1.0F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        triggerAttackAnimation();
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            int duration = isBoss() ? 140 : 80;
            int amp = isBoss() ? 1 : 0;
            living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amp, false, true));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.MOTHER_SPIDER_ATTACK.get(), SoundSource.HOSTILE, 1.3F, isBoss() ? 0.85F : 1.0F);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOTHER_SPIDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.MOTHER_SPIDER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOTHER_SPIDER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(ModSounds.ANT_SOLDIER_STEP.get(), 0.20F, isBoss() ? 0.70F : 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsBoss", isBoss());
        compound.putInt("WebShootCooldown", this.webShootCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("IsBoss")) {
            setBoss(compound.getBoolean("IsBoss"));
        }
        if (compound.contains("WebShootCooldown")) {
            this.webShootCooldown = compound.getInt("WebShootCooldown");
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // Drops de telaraña y ojos de araña
        this.spawnAtLocation(new ItemStack(Items.STRING, 2 + this.random.nextInt(3)));
        if (this.random.nextFloat() < 0.60F) {
            this.spawnAtLocation(new ItemStack(Items.SPIDER_EYE, 1 + this.random.nextInt(2)));
        }
        // Awakening Coins
        this.spawnAtLocation(SystemItems.coin(1 + this.random.nextInt(3)));
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 36 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    // ==================== GECKOLIB ANIMATIONS ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "actions", 3, this::actionPredicate));
    }

    private PlayState movementPredicate(AnimationState<MotherSpiderEntity> state) {
        if (this.isDeadOrDying()) {
            return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.mother_spider.death"));
        }
        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.mother_spider.walk"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.mother_spider.idle"));
    }

    private PlayState actionPredicate(AnimationState<MotherSpiderEntity> state) {
        if (this.isDeadOrDying()) {
            return PlayState.STOP;
        }
        if (this.isShootingWeb()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.mother_spider.shoot_web"));
        }
        if (this.isAttacking()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.mother_spider.attack"));
        }
        state.getController().forceAnimationReset();
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // ==================== CUSTOM AI GOALS ====================

    public static class MotherSpiderShootWebGoal extends Goal {
        private final MotherSpiderEntity spider;

        public MotherSpiderShootWebGoal(MotherSpiderEntity spider) {
            this.spider = spider;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.spider.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (!this.spider.canShootWeb()) return false;
            double distSq = this.spider.distanceToSqr(target);
            return distSq >= 16.0D && distSq <= 300.0D && this.spider.hasLineOfSight(target);
        }

        @Override
        public void start() {
            this.spider.getNavigation().stop();
            this.spider.triggerWebShootAnimation();
        }

        @Override
        public boolean canContinueToUse() {
            return this.spider.isShootingWeb();
        }

        @Override
        public void tick() {
            LivingEntity target = this.spider.getTarget();
            if (target != null) {
                this.spider.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }

    public static class MotherSpiderMeleeAttackGoal extends MeleeAttackGoal {
        private final MotherSpiderEntity spider;

        public MotherSpiderMeleeAttackGoal(MotherSpiderEntity spider, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(spider, speedModifier, followingTargetEvenIfNotSeen);
            this.spider = spider;
        }

        @Override
        public boolean canUse() {
            if (this.spider.isShootingWeb()) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.spider.isShootingWeb()) return false;
            return super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.spider.doHurtTarget(target);
            }
        }
    }
}
