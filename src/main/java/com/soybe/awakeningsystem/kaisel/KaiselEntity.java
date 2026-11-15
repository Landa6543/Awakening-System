package com.soybe.awakeningsystem.kaisel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * Kaisel, the wyvern. Alive it is a white wyvern that hunts players, walking or flying at them to bite. Once the
 * Monarch extracts its shadow ("Arise", tagged {@code shadow_soldier}) it turns black and cyan and its master can
 * ride it with no saddle: W flies toward where you look (look down to dive), Space takes off and climbs, Ctrl goes
 * faster, Shift dismounts. Unridden, the shadow flies after its master when left behind.
 */
public class KaiselEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(KaiselEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String PREFIX = "animation.kaisel.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(PREFIX + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(PREFIX + "walk");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop(PREFIX + "fly");
    private static final RawAnimation GLIDE = RawAnimation.begin().thenLoop(PREFIX + "glide");

    /** The local player's flight keys, set each tick by the client (only meaningful where the rider's game runs). */
    public static volatile boolean clientUp, clientFast;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int biteCooldown;

    public KaiselEntity(EntityType<? extends KaiselEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(FLYING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ── state ────────────────────────────────────────────────────────────────
    public boolean isShadow() {
        return getTags().contains("shadow_soldier");
    }

    /** The player whose shadow this is, from the {@code shadow_master_<uuid>} tag. */
    public UUID masterId() {
        for (String t : getTags()) if (t.startsWith("shadow_master_")) {
            try { return UUID.fromString(t.substring("shadow_master_".length())); } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
        this.setNoGravity(flying);
    }

    // ── riding ───────────────────────────────────────────────────────────────
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isShadow() && !isVehicle() && !player.isSecondaryUseActive() && player.getUUID().equals(masterId())) {
            if (!level().isClientSide) {
                player.setYRot(getYRot());
                player.setXRot(getXRot());
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return isShadow() && getFirstPassenger() instanceof Player p ? p : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dims, float scale) {
        // on the shoulders, just ahead of the wings
        return new Vec3(0, dims.height() * 0.78, 0.55).yRot(-getYRot() * Mth.DEG_TO_RAD);
    }

    @Override
    public void travel(Vec3 input) {
        if (isAlive() && getControllingPassenger() instanceof Player rider) {
            setYRot(rider.getYRot());
            yRotO = getYRot();
            setXRot(rider.getXRot() * 0.5F);
            yBodyRot = yHeadRot = getYRot();
            rider.fallDistance = 0;
            this.fallDistance = 0;
            if (!isControlledByLocalInstance()) {
                super.travel(Vec3.ZERO);
                return;
            }
            boolean up = clientUp, fast = clientFast;
            if (!isFlying()) {
                if (up) {                                     // take off
                    setFlying(true);
                    setDeltaMovement(getDeltaMovement().add(0, 0.7, 0));
                } else {
                    setSpeed(0.32F * (fast ? 1.5F : 1F));
                    super.travel(new Vec3(rider.xxa * 0.5, 0, rider.zza));
                    return;
                }
            }
            double speed = getAttributeValue(Attributes.FLYING_SPEED) * (fast ? 1.6 : 1.0);
            Vec3 look = rider.getLookAngle(), side = new Vec3(-look.z, 0, look.x).normalize();
            Vec3 want = look.scale(rider.zza * speed).add(side.scale(-rider.xxa * speed * 0.4));
            if (up) want = new Vec3(want.x, Math.max(want.y, 0.55), want.z);
            setDeltaMovement(getDeltaMovement().lerp(want, 0.12));
            move(MoverType.SELF, getDeltaMovement());
            // touching the ground without climbing lands it
            if (onGround() && !up && getDeltaMovement().y <= 0.01) setFlying(false);
            return;
        }
        if (isFlying()) {                                     // unridden flight is steered in aiStep
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91));
            return;
        }
        super.travel(input);
    }

    // ── flying on its own ────────────────────────────────────────────────────
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || isVehicle()) return;
        if (biteCooldown > 0) biteCooldown--;
        Vec3 goal = null;
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            // fly at targets that are high up or far away; walk to the rest
            double dy = target.getY() - getY(), dist = distanceTo(target);
            if (isFlying() || dy > 3 || dist > 14 || !target.onGround()) goal = target.position().add(0, target.getBbHeight() * 0.5, 0);
            if (dist < 4.5 && biteCooldown == 0) {
                biteCooldown = 24;
                triggerAnim("attack", "bite");
                doHurtTarget(target);
            }
        } else if (isShadow() && level() instanceof ServerLevel sl && masterId() != null) {
            Player master = sl.getPlayerByUUID(masterId());
            if (master != null) {
                double dist = distanceTo(master);
                if (dist > 18 || (isFlying() && dist > 6) || (master.getY() - getY() > 4)) goal = master.position().add(0, 4, 0);
                if (dist > 96) teleportTo(master.getX(), master.getY() + 3, master.getZ());
            }
        }
        if (goal != null) {
            if (!isFlying()) {
                setFlying(true);
                setDeltaMovement(getDeltaMovement().add(0, 0.6, 0));
            }
            getNavigation().stop();
            Vec3 to = goal.subtract(position());
            double speed = getAttributeValue(Attributes.FLYING_SPEED) * 0.7;
            Vec3 want = to.lengthSqr() > 1 ? to.normalize().scale(speed) : Vec3.ZERO;
            setDeltaMovement(getDeltaMovement().lerp(want, 0.08));
            float yaw = (float) (Mth.atan2(to.z, to.x) * Mth.RAD_TO_DEG) - 90F;
            setYRot(Mth.approachDegrees(getYRot(), yaw, 6F));
            yBodyRot = yHeadRot = getYRot();
        } else if (isFlying()) {
            // nothing to chase: come down and land
            setDeltaMovement(getDeltaMovement().add(0, -0.04, 0));
            if (onGround()) setFlying(false);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (biteCooldown <= 0) triggerAnim("attack", "bite");
        return super.doHurtTarget(target);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    // ── sounds ───────────────────────────────────────────────────────────────
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    // ── saving ───────────────────────────────────────────────────────────────
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Flying", isFlying());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFlying(tag.getBoolean("Flying"));
    }

    // ── animation ────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 8, state -> {
            if (isFlying() || (isVehicle() && !onGround())) {
                Vec3 v = getDeltaMovement();
                boolean gliding = v.y < -0.08 && v.horizontalDistanceSqr() > 0.2;
                return state.setAndContinue(gliding ? GLIDE : FLY);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        AnimationController<KaiselEntity> attack = new AnimationController<>(this, "attack", 3, state -> PlayState.STOP);
        attack.triggerableAnim("bite", RawAnimation.begin().thenPlay(PREFIX + "bite"));
        attack.triggerableAnim("roar", RawAnimation.begin().thenPlay(PREFIX + "roar"));
        controllers.add(attack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** Used when summoned or ridden off: let out a roar. */
    public void roar() {
        triggerAnim("attack", "roar");
        playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.2F, 1.1F);
    }

    @Override
    public boolean isPushable() {
        return !isVehicle();
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    public static boolean isKaisel(Mob mob) {
        return mob instanceof KaiselEntity;
    }
}
