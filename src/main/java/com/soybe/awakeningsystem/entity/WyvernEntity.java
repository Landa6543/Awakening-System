package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WyvernEntity extends TamableAnimal implements FlyingAnimal, RangedAttackMob {

    public static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(WyvernEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(WyvernEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_SADDLED =
            SynchedEntityData.defineId(WyvernEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(WyvernEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_WYVERN_AGE =
            SynchedEntityData.defineId(WyvernEntity.class, EntityDataSerializers.INT);

    public static final int VARIANT_POISON = 0;
    public static final int VARIANT_FIRE = 1;
    public static final int VARIANT_LIGHTNING = 2;

    public static final int ADULT_AGE_TICKS = 72000;

    private int roarTimer = 0;
    private int breathCooldown = 0;
    private boolean clientSpaceHeld = false;
    private boolean clientShiftHeld = false;
    private boolean clientSprintHeld = false;

    private float prevFlyProgress = 0.0F;
    private float flyProgress = 0.0F;

    public WyvernEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 16, true);
        this.setTame(false, false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, VARIANT_POISON);
        builder.define(DATA_FLYING, false);
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_ROARING, false);
        builder.define(DATA_WYVERN_AGE, ADULT_AGE_TICKS);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 130.0D)
                .add(Attributes.FLYING_SPEED, 0.58D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25D, 35, 65, 24.0F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.35D, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.3D, 8.0F, 3.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !WyvernEntity.this.isTame() && super.canUse();
            }
        });
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    @Override
    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
    }

    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(DATA_SADDLED, saddled);
    }

    public boolean isRoaring() {
        return this.entityData.get(DATA_ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(DATA_ROARING, roaring);
    }

    public int getWyvernAge() {
        return this.entityData.get(DATA_WYVERN_AGE);
    }

    public void setWyvernAge(int age) {
        this.entityData.set(DATA_WYVERN_AGE, age);
    }

    public boolean isAdult() {
        return getWyvernAge() >= ADULT_AGE_TICKS;
    }

    public void setFlightInput(boolean space, boolean shift, boolean sprint) {
        this.clientSpaceHeld = space;
        this.clientShiftHeld = shift;
        this.clientSprintHeld = sprint;
    }

    @Override
    public void tick() {
        super.tick();

        this.prevFlyProgress = this.flyProgress;
        if (this.isFlying()) {
            this.flyProgress = Math.min(1.0F, this.flyProgress + 0.1F);
        } else {
            this.flyProgress = Math.max(0.0F, this.flyProgress - 0.1F);
        }

        if (this.breathCooldown > 0) breathCooldown--;

        if (this.roarTimer > 0) {
            this.roarTimer--;
            if (this.roarTimer == 0) {
                setRoaring(false);
            }
        }

        if (!this.level().isClientSide() && !this.isAdult()) {
            setWyvernAge(getWyvernAge() + 1);
        }

        if (this.level() instanceof ServerLevel sl && this.tickCount % 5 == 0) {
            Vec3 headPos = this.position().add(this.getLookAngle().scale(1.5D)).add(0, 1.2D, 0);
            if (getVariant() == VARIANT_POISON) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, headPos.x, headPos.y, headPos.z, 2, 0.2D, 0.2D, 0.2D, 0.01D);
            } else if (getVariant() == VARIANT_FIRE) {
                sl.sendParticles(ParticleTypes.FLAME, headPos.x, headPos.y, headPos.z, 2, 0.2D, 0.2D, 0.2D, 0.02D);
            } else {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, headPos.x, headPos.y, headPos.z, 2, 0.2D, 0.2D, 0.2D, 0.05D);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            int var = getVariant();
            if (var == VARIANT_POISON) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 2));
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            } else if (var == VARIANT_FIRE) {
                living.igniteForSeconds(7);
            } else {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            }
            this.playSound(ModSounds.WYVERN_ROAR.get(), 1.2F, 1.1F);
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.breathCooldown > 0) return;
        this.breathCooldown = 40;

        Vec3 look = target.position().subtract(this.position()).normalize();
        double dX = target.getX() - this.getX();
        double dY = target.getY(0.5D) - this.getY(0.7D);
        double dZ = target.getZ() - this.getZ();

        setRoaring(true);
        this.roarTimer = 25;
        this.playSound(ModSounds.WYVERN_BREATH.get(), 1.5F, 1.0F);

        if (this.level() instanceof ServerLevel sl) {
            if (getVariant() == VARIANT_FIRE) {
                LargeFireball fireball = new LargeFireball(this.level(), this, look, 2);
                fireball.setPos(this.getX() + look.x * 1.5D, this.getY(0.7D), this.getZ() + look.z * 1.5D);
                sl.addFreshEntity(fireball);
            } else {
                SmallFireball small = new SmallFireball(this.level(), this, look);
                small.setPos(this.getX() + look.x * 1.5D, this.getY(0.7D), this.getZ() + look.z * 1.5D);
                sl.addFreshEntity(small);
            }
        }
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
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();

                float forward = player.zza;
                float strafe = player.xxa;
                boolean isSprinting = this.clientSprintHeld || player.isSprinting();

                if (this.clientSpaceHeld && !this.isFlying()) {
                    this.setFlying(true);
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.70D, this.getDeltaMovement().z);
                    this.playSound(ModSounds.WYVERN_ROAR.get(), 1.2F, 1.2F);
                }

                boolean flying = this.isFlying();
                double radYaw = Math.toRadians(player.getYRot());
                double fwdX = -Math.sin(radYaw);
                double fwdZ = Math.cos(radYaw);
                double sideX = Math.cos(radYaw);
                double sideZ = Math.sin(radYaw);

                if (flying) {
                    float targetPitch = 0.0F;
                    if (this.clientSpaceHeld) {
                        targetPitch = -20.0F;
                    } else if (this.clientShiftHeld) {
                        targetPitch = 25.0F;
                    }
                    this.setXRot(Mth.rotLerp(0.25F, this.getXRot(), targetPitch));

                    double flightSpeed = isSprinting ? 2.40D : (this.clientShiftHeld ? 2.80D : 1.45D);
                    double vx = 0.0D;
                    double vz = 0.0D;
                    if (forward != 0.0F || strafe != 0.0F) {
                        vx = (fwdX * forward + sideX * strafe) * flightSpeed;
                        vz = (fwdZ * forward + sideZ * strafe) * flightSpeed;
                    } else {
                        Vec3 cur = this.getDeltaMovement();
                        vx = cur.x * 0.88D;
                        vz = cur.z * 0.88D;
                    }

                    double vy = 0.0D;
                    if (this.clientSpaceHeld) {
                        vy = 0.85D;
                    } else if (this.clientShiftHeld) {
                        vy = -1.25D;
                    } else {
                        vy = Math.sin(this.tickCount * 0.15D) * 0.03D;
                    }

                    this.setDeltaMovement(vx, vy, vz);
                    this.move(MoverType.SELF, this.getDeltaMovement());

                    if (this.onGround() && !this.clientSpaceHeld) {
                        this.setFlying(false);
                    }
                    return;
                }
            }
        }
        super.travel(travelVector);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (!this.isTame() && (itemInHand.is(SystemItems.MANA_BEEF.get()) || itemInHand.is(SystemItems.MANA_MUTTON.get()) || itemInHand.is(Items.COOKED_BEEF))) {
            if (!this.level().isClientSide()) {
                itemInHand.shrink(1);
                if (this.random.nextFloat() < 0.35F) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    player.displayClientMessage(Component.translatable("entity.awakening_system.wyvern.tamed"), false);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (this.getHealth() < this.getMaxHealth() && (itemInHand.is(SystemItems.MANA_BEEF.get()) || itemInHand.is(Items.COOKED_BEEF))) {
                itemInHand.shrink(1);
                this.heal(30.0F);
                this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (!this.isSaddled() && (itemInHand.is(Items.SADDLE) || (SystemItems.WYVERN_SADDLE.isBound() && itemInHand.is(SystemItems.WYVERN_SADDLE.get())))) {
                if (!player.getAbilities().instabuild) {
                    itemInHand.shrink(1);
                }
                this.setSaddled(true);
                this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("entity.awakening_system.wyvern.saddled"), false);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (this.isSaddled() && !player.isShiftKeyDown() && itemInHand.isEmpty()) {
                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (player.isShiftKeyDown() && itemInHand.isEmpty()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                player.displayClientMessage(Component.translatable(this.isOrderedToSit() ? "entity.awakening_system.wyvern.waiting" : "entity.awakening_system.wyvern.following"), true);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(SystemItems.MANA_BEEF.get()) || stack.is(SystemItems.MANA_MUTTON.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        WyvernEntity child = new WyvernEntity(ModEntities.WYVERN.get(), level);
        child.setVariant(this.getVariant());
        child.setWyvernAge(0);
        return child;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WyvernVariant", getVariant());
        tag.putBoolean("WyvernFlying", isFlying());
        tag.putBoolean("WyvernSaddled", isSaddled());
        tag.putInt("WyvernAge", getWyvernAge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WyvernVariant")) setVariant(tag.getInt("WyvernVariant"));
        if (tag.contains("WyvernFlying")) setFlying(tag.getBoolean("WyvernFlying"));
        if (tag.contains("WyvernSaddled")) setSaddled(tag.getBoolean("WyvernSaddled"));
        if (tag.contains("WyvernAge")) setWyvernAge(tag.getInt("WyvernAge"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WYVERN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.WYVERN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WYVERN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isFlying()) {
            this.playSound(SoundEvents.RAVAGER_STEP, 0.7F, 0.8F);
        }
    }

    @Override
    protected void playBlockFallSound() {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }
}
