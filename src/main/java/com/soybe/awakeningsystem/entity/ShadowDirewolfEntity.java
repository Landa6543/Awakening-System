package com.soybe.awakeningsystem.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Lobo Plateado de las Sombras (Shadow Direwolf - Ranga).
 * Majestuoso cánido del reino Isekai / Sombras:
 * - Domesticable con carnes crudas/cocinadas.
 * - Montable cuando está domesticado (control directo de movimiento terrestre con velocidad alta).
 * - Aullido de Tormenta Sombría: Otorga Fuerza y Velocidad al lobo y su amo, desatando rayos oscuros.
 * - Animación de sentarse y postura fiel.
 */
public class ShadowDirewolfEntity extends TamableAnimal implements PlayerRideableJumping {

    private static final EntityDataAccessor<Boolean> DATA_HOWLING =
            SynchedEntityData.defineId(ShadowDirewolfEntity.class, EntityDataSerializers.BOOLEAN);

    private int howlCooldown = 0;
    private int howlAnimationTicks = 0;
    private float playerJumpPendingScale = 0.0F;

    public ShadowDirewolfEntity(EntityType<? extends ShadowDirewolfEntity> type, Level level) {
        super(type, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HOWLING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.35D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.25D, 9.0F, 2.5F));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    public boolean isHowling() {
        return this.entityData.get(DATA_HOWLING);
    }

    public void setHowling(boolean howling) {
        this.entityData.set(DATA_HOWLING, howling);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF) ||
               stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP) ||
               stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON) ||
               stack.is(Items.ROTTEN_FLESH);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            boolean canInteract = this.isOwnedBy(player) || this.isTame() || (isFood(itemstack) && !this.isTame());
            return canInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (!this.isTame() && isFood(itemstack)) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.playSound(SoundEvents.WOLF_PANT, 1.0F, 0.9F);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.playSound(SoundEvents.WOLF_WHINE, 1.0F, 0.9F);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.heal(8.0F);
                this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.05D);
                }
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()) {
                boolean wantsToSit = !this.isOrderedToSit();
                this.setOrderedToSit(wantsToSit);
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                this.playSound(wantsToSit ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (!this.isInSittingPose() && !this.isVehicle()) {
                this.doPlayerRide(player);
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    protected void doPlayerRide(Player player) {
        if (!this.level().isClientSide) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof Player player && this.isTame() && this.isOwnedBy(player) ? player : null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float f = player.xxa * 0.5F;
        float f1 = player.zza;
        if (f1 <= 0.0F) {
            f1 *= 0.35F;
        }
        return new Vec3(f, 0.0D, f1);
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.35F;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.howlCooldown > 0) {
            this.howlCooldown--;
        }

        if (this.howlAnimationTicks > 0) {
            this.howlAnimationTicks--;
            if (this.howlAnimationTicks == 0) {
                this.setHowling(false);
            }
        }

        if (!this.level().isClientSide && this.isAlive() && this.howlCooldown <= 0) {
            if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 64.0D) {
                triggerShadowHowl();
            }
        }

        if (this.level().isClientSide && this.random.nextInt(3) == 0) {
            Vec3 look = this.getLookAngle();
            double hx = this.getX() + look.x * 0.6D;
            double hy = this.getY() + 1.15D;
            double hz = this.getZ() + look.z * 0.6D;

            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    hx + (this.random.nextDouble() - 0.5D) * 0.2D,
                    hy + (this.random.nextDouble() - 0.5D) * 0.2D,
                    hz + (this.random.nextDouble() - 0.5D) * 0.2D,
                    0.0D, 0.02D, 0.0D);

            if (this.isHowling()) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D,
                        this.getY() + 0.8D + this.random.nextDouble() * 0.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D,
                        0.0D, 0.05D, 0.0D);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.playSound(com.soybe.awakeningsystem.ModSounds.SHADOW_WOLF_BITE.get(), 1.0F, 1.0F);
        }
        return hurt;
    }

    public void triggerShadowHowl() {
        this.howlCooldown = 300;
        this.howlAnimationTicks = 35;
        this.setHowling(true);

        this.playSound(com.soybe.awakeningsystem.ModSounds.SHADOW_WOLF_HOWL.get(), 1.5F, 1.0F);

        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 0));

        LivingEntity owner = this.getOwner();
        if (owner != null && this.distanceToSqr(owner) < 256.0D) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 0));
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.8D, 0.6D, 0.8D, 0.15D);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.5D, this.getZ(), 15, 0.5D, 0.4D, 0.5D, 0.08D);
        }
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        if (jumpPower < 0) {
            jumpPower = 0;
        }
        this.playerJumpPendingScale = jumpPower >= 90 ? 1.0F : 0.4F + 0.4F * ((float) jumpPower / 90.0F);
    }

    @Override
    public boolean canJump() {
        return this.isTame() && this.onGround();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        this.playSound(SoundEvents.WOLF_GROWL, 0.8F, 1.2F);
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (this.playerJumpPendingScale > 0.0F) {
            if (this.onGround()) {
                double jumpVelocity = 0.52D * (double) this.playerJumpPendingScale;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, jumpVelocity, 0.0D));
                this.hasImpulse = true;
            }
            this.playerJumpPendingScale = 0.0F;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getTarget() != null) {
            return SoundEvents.WOLF_GROWL;
        }
        return this.random.nextInt(3) == 0 ? SoundEvents.WOLF_HOWL : SoundEvents.WOLF_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    public float getVoicePitch() {
        // Tono grave e imponente de lobo gigante alfa (0.68F - 0.74F)
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.06F + 0.70F;
    }

    @Override
    protected float getSoundVolume() {
        return 0.85F;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob otherParent) {
        return ModEntities.SHADOW_DIREWOLF.get().create(serverLevel);
    }
}
