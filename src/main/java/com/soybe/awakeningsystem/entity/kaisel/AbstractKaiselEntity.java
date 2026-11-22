package com.soybe.awakeningsystem.entity.kaisel;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public abstract class AbstractKaiselEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(AbstractKaiselEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> ATTACKING_ANIM =
            SynchedEntityData.defineId(AbstractKaiselEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> ATTACK_TYPE =
            SynchedEntityData.defineId(AbstractKaiselEntity.class, EntityDataSerializers.INT);

    // SLR Authentic Animation Keys
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    protected static final RawAnimation GLIDE_ANIM = RawAnimation.begin().thenLoop("fly_with_glide");
    protected static final RawAnimation FAST_FLY_ANIM = RawAnimation.begin().thenLoop("fast_fly");
    protected static final RawAnimation ATTACK_1_ANIM = RawAnimation.begin().thenPlay("attack_1");
    protected static final RawAnimation ATTACK_2_ANIM = RawAnimation.begin().thenPlay("attack_2");

    private UUID ownerUUID = null;

    private boolean clientSpaceHeld = false;
    private boolean clientShiftHeld = false;
    private boolean clientSprintHeld = false;

    public void setFlightInput(boolean space, boolean shift, boolean sprint) {
        this.clientSpaceHeld = space;
        this.clientShiftHeld = shift;
        this.clientSprintHeld = sprint;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return this.isVehicle() && this.getFirstPassenger() instanceof Player player ? player.isLocalPlayer() : this.isEffectiveAi();
    }

    protected AbstractKaiselEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createKaiselAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FLYING_SPEED, 0.55D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.STEP_HEIGHT, 1.25D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLYING, false);
        builder.define(ATTACKING_ANIM, false);
        builder.define(ATTACK_TYPE, 1);
    }

    public abstract boolean isShadowVariant();

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
        this.setNoGravity(flying);
    }

    public boolean isAttackingAnim() {
        return this.entityData.get(ATTACKING_ANIM);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public boolean isOwnedBy(Player player) {
        return player != null && ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
            return false;
        }
        if (isShadowVariant() && source.getEntity() instanceof Player p && isOwnedBy(p)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, 2.15D * (double) scale, 0.1D * (double) scale);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isShadowVariant() || isOwnedBy(player) || player.isCreative()) {
            if (!player.isShiftKeyDown()) {
                if (!this.level().isClientSide()) {
                    if (this.ownerUUID == null) {
                        this.ownerUUID = player.getUUID();
                    }
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            Entity firstPassenger = this.getFirstPassenger();
            if (this.isVehicle() && firstPassenger instanceof Player player) {
                if (!this.isControlledByLocalInstance()) {
                    return;
                }

                this.setYRot(player.getYRot());
                this.setXRot(Mth.clamp(player.getXRot(), -55.0F, 55.0F) * 0.45F);
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
                this.setYHeadRot(this.getYRot());

                float forwardInput = player.zza;
                float strafeInput = player.xxa;

                // Space bar initiates takeoff from ground
                if (this.clientSpaceHeld && !this.isFlying()) {
                    this.setFlying(true);
                    this.setNoGravity(true);
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.55D, this.getDeltaMovement().z);
                    this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.2F, 1.0F);
                }

                if (this.isFlying()) {
                    this.setNoGravity(true);

                    Vec3 look = player.getLookAngle();
                    Vec3 flatForward = new Vec3(look.x, 0.0D, look.z);
                    if (flatForward.lengthSqr() < 0.001D) {
                        flatForward = Vec3.directionFromRotation(0.0F, player.getYRot()).multiply(1.0D, 0.0D, 1.0D);
                    }
                    flatForward = flatForward.normalize();
                    Vec3 right = new Vec3(-flatForward.z, 0.0D, flatForward.x);

                    double speedMult = player.isSprinting() ? 1.45D : 0.95D;

                    // Pitch lift: Look UP -> climb UP; Look DOWN -> descend/dive DOWN smoothly
                    float pitchDeg = player.getXRot();
                    double pitchRad = pitchDeg * Mth.DEG_TO_RAD;
                    double pitchLift = -Mth.sin((float) pitchRad) * (forwardInput > 0 ? (forwardInput * 1.10D) : (pitchDeg > 15.0F ? 0.45D : 0.0D));

                    // Space lifts up, Shift descends down
                    double ascend = this.clientSpaceHeld ? 0.52D : 0.0D;
                    double descend = (player.isShiftKeyDown() || this.clientShiftHeld) ? -0.52D : 0.0D;

                    Vec3 desired = flatForward.scale(forwardInput * speedMult)
                            .add(right.scale(-strafeInput * 0.55D))
                            .add(0.0D, pitchLift + ascend + descend, 0.0D);

                    Vec3 next;
                    if (desired.lengthSqr() > 0.001D) {
                        next = this.getDeltaMovement().scale(0.68D).add(desired.scale(0.32D));
                    } else {
                        // Smooth deceleration / hover in place
                        next = this.getDeltaMovement().scale(0.82D);
                    }

                    this.setDeltaMovement(next);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.hasImpulse = true;

                    // Land smoothly only when physically touching ground
                    boolean touchingFloor = this.onGround() || this.verticalCollisionBelow;
                    if (touchingFloor && !this.clientSpaceHeld && (descend < 0.0D || next.y <= 0.05D)) {
                        this.setFlying(false);
                        this.setNoGravity(false);
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
                    }

                    if (this.level() instanceof ServerLevel sl && isShadowVariant() && this.random.nextInt(3) == 0) {
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.6D, this.getZ(), 2, 0.4, 0.2, 0.4, 0.02);
                    }
                    return;
                } else {
                    // On-ground walking (Ice and Fire style physics with vanilla step-up and walkAnimation)
                    this.setNoGravity(false);
                    this.setXRot(0.0F);

                    float speed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
                    if (player.isSprinting() || this.clientSprintHeld) {
                        speed *= 1.35F;
                    }
                    this.setSpeed(speed);

                    float fwd = forwardInput * (forwardInput > 0 ? 1.0F : 0.35F);
                    float strafe = strafeInput * 0.5F;

                    super.travel(new Vec3(strafe, travelVector.y, fwd));
                    return;
                }
            }
        }

        // Unmounted flight handling:
        if (this.isFlying()) {
            if (!this.level().isClientSide() && this.getTarget() == null) {
                // Not ridden and no combat target: gently descend and land without flying off
                Vec3 cur = this.getDeltaMovement().scale(0.75D);
                double vy = (this.onGround() || this.verticalCollisionBelow) ? 0.0D : Math.max(-0.20D, cur.y - 0.05D);
                this.setDeltaMovement(cur.x, vy, cur.z);
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.onGround() || this.verticalCollisionBelow) {
                    this.setFlying(false);
                    this.setNoGravity(false);
                }
                return;
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.85D));
            if (this.onGround() || this.verticalCollisionBelow) {
                this.setFlying(false);
                this.setNoGravity(false);
            }
            return;
        }

        super.travel(travelVector);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsFlying", isFlying());
        if (ownerUUID != null) {
            compound.putUUID("OwnerUUID", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setFlying(compound.getBoolean("IsFlying"));
        if (compound.hasUUID("OwnerUUID")) {
            setOwnerUUID(compound.getUUID("OwnerUUID"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.entityData.get(ATTACKING_ANIM)) {
                int aType = this.entityData.get(ATTACK_TYPE);
                return state.setAndContinue(aType == 2 ? ATTACK_2_ANIM : ATTACK_1_ANIM);
            }

            // On-ground state (walk or idle) - takes precedence if touching ground
            boolean isGrounded = !this.isFlying() || this.onGround() || this.verticalCollisionBelow;

            if (isGrounded) {
                boolean isMovingOnGround = false;
                Entity passenger = this.getFirstPassenger();
                if (passenger instanceof Player p) {
                    isMovingOnGround = (Math.abs(p.zza) > 0.01F || Math.abs(p.xxa) > 0.01F);
                }
                if (!isMovingOnGround) {
                    isMovingOnGround = state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.001D;
                }

                if (isMovingOnGround) {
                    // the steps keep pace with how fast it really goes, so it walks instead of sliding
                    double v = Math.sqrt(this.getDeltaMovement().horizontalDistanceSqr());
                    double speed = net.minecraft.util.Mth.clamp(v * 9.0D, 0.9D, 3.2D);
                    state.getController().setAnimationSpeed(speed);
                    return state.setAndContinue(WALK_ANIM);
                }

                state.getController().setAnimationSpeed(1.0D);
                return state.setAndContinue(IDLE_ANIM);
            }

            // Airborne flight state
            Entity passenger = this.getFirstPassenger();
            boolean isSprinting = passenger instanceof Player p && (p.isSprinting() || this.clientSprintHeld);
            boolean isMovingAir = false;
            if (passenger instanceof Player p) {
                isMovingAir = (Math.abs(p.zza) > 0.01F || Math.abs(p.xxa) > 0.01F);
            }
            if (!isMovingAir) {
                isMovingAir = this.getDeltaMovement().horizontalDistanceSqr() > 0.005D;
            }

            if (isSprinting) {
                state.getController().setAnimationSpeed(1.20D);
                return state.setAndContinue(FAST_FLY_ANIM);
            } else if (isMovingAir) {
                state.getController().setAnimationSpeed(1.08D);
                return state.setAndContinue(FLY_ANIM);
            } else {
                // Hovering / gliding smoothly
                state.getController().setAnimationSpeed(1.0D);
                return state.setAndContinue(GLIDE_ANIM);
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
