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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tigre Blanco del Trueno (White Thunder Tiger / Byakko).
 * Bestia espiritual suprema del reino Murim:
 * - Depredador ágil y dominante de los bosques y montañas.
 * - Domesticable con carnes crudas/cocinadas o pescados frescos.
 * - Montable cuando está domesticado (control directo de movimiento terrestre de alta velocidad).
 * - Rugido del Trueno (Thunder Roar): Aturde y daña a enemigos cercanos con ondas de choque eléctricas,
 *   otorgando Fuerza y Velocidad al tigre y a su cultivador amo.
 * - Zarpazo Electrizante: Oportunidad de electrocutar al golpear.
 */
public class WhiteThunderTigerEntity extends TamableAnimal implements PlayerRideableJumping {

    private static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(WhiteThunderTigerEntity.class, EntityDataSerializers.BOOLEAN);

    private int roarCooldown = 0;
    private int roarAnimationTicks = 0;
    private float playerJumpPendingScale = 0.0F;

    public WhiteThunderTigerEntity(EntityType<? extends WhiteThunderTigerEntity> type, Level level) {
        super(type, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 65.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROARING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.45F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.25D, 9.0F, 2.5F));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    public boolean isRoaring() {
        return this.entityData.get(DATA_ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(DATA_ROARING, roaring);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF) ||
               stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP) ||
               stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON) ||
               stack.is(Items.SALMON) || stack.is(Items.COOKED_SALMON) ||
               stack.is(Items.COD) || stack.is(Items.COOKED_COD) ||
               stack.is(Items.RABBIT) || stack.is(Items.COOKED_RABBIT);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 1. Si NO está domesticado
        if (!this.isTame()) {
            if (this.isFood(itemstack)) {
                this.usePlayerItem(player, hand, itemstack);
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setOrderedToSit(true);
                        this.level().broadcastEntityEvent(this, (byte) 7); // Corazones
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // Humo
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }

        // 2. Si YA está domesticado y pertenece al jugador
        if (this.isOwnedBy(player)) {
            // Curar con comida si está herido
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.usePlayerItem(player, hand, itemstack);
                this.heal(8.0F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.4D, 0.3D, 0.4D, 0.02D);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Shift + Clic derecho: Alternar sentarse / levantarse
            if (player.isShiftKeyDown()) {
                boolean wantsToSit = !this.isOrderedToSit();
                this.setOrderedToSit(wantsToSit);
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                this.playSound(wantsToSit ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT, 1.0F, 0.7F);
                return InteractionResult.SUCCESS;
            }

            // Si está parado y no tiene jinete: Montar
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
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            // Zarpazo eléctrico: oportunidad de electrocutar/ralentizar
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.8D, target.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.1D);
            }
            this.playSound(com.soybe.awakeningsystem.ModSounds.WHITE_TIGER_ROAR.get(), 0.8F, 1.2F);
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.6F, 1.8F);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.roarCooldown > 0) {
            this.roarCooldown--;
        }

        if (this.roarAnimationTicks > 0) {
            this.roarAnimationTicks--;
            if (this.roarAnimationTicks == 0) {
                this.setRoaring(false);
            }
        }

        // Rugido automático en combate
        if (!this.level().isClientSide && this.isAlive() && this.roarCooldown <= 0) {
            if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 64.0D) {
                triggerThunderRoar();
            }
        }

        // Partículas místicas de relámpago en el pelaje
        if (this.level().isClientSide && this.random.nextInt(3) == 0) {
            double rx = (this.random.nextDouble() - 0.5D) * 1.3D;
            double ry = this.random.nextDouble() * 1.0D + 0.2D;
            double rz = (this.random.nextDouble() - 0.5D) * 1.5D;

            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + rx, this.getY() + ry, this.getZ() + rz,
                    0.0D, 0.04D, 0.0D);

            if (this.isRoaring()) {
                this.level().addParticle(ParticleTypes.FLASH,
                        this.getX(), this.getY() + 1.2D, this.getZ(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    /**
     * Habilidad: Rugido del Trueno (Thunder Roar).
     * Onda de choque expansiva que daña y aturde enemigos cercanos,
     * otorgando buffs al tigre y al cultivador dueño.
     */
    public void triggerThunderRoar() {
        this.roarCooldown = 280;
        this.roarAnimationTicks = 35;
        this.setRoaring(true);

        this.playSound(com.soybe.awakeningsystem.ModSounds.WHITE_TIGER_ROAR.get(), 1.5F, 1.0F);
        this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8F, 1.4F);

        // Buffs de cultivo de trueno
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 260, 1));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 260, 1));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 260, 0));

        LivingEntity owner = this.getOwner();
        if (owner != null && this.distanceToSqr(owner) < 256.0D) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 260, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 260, 1));
        }

        // Onda expansiva de daño y retroceso a enemigos
        AABB blastArea = this.getBoundingBox().inflate(6.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, blastArea,
                e -> e != this && e != owner && (!this.isTame() || !this.isAlliedTo(e)));

        DamageSource roarDamage = this.damageSources().mobAttack(this);
        for (LivingEntity enemy : targets) {
            enemy.hurt(roarDamage, 6.0F);
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));

            // Knockback alejándose del tigre
            double dx = enemy.getX() - this.getX();
            double dz = enemy.getZ() - this.getZ();
            enemy.knockback(0.8D, -dx, -dz);
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0D, this.getZ(), 35, 1.5D, 0.8D, 1.5D, 0.2D);
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.8D, this.getZ(), 3, 0.5D, 0.3D, 0.5D, 0.05D);
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
        this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 0.9F, 0.6F);
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (this.playerJumpPendingScale > 0.0F) {
            if (this.onGround()) {
                double jumpVelocity = 0.56D * (double) this.playerJumpPendingScale;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, jumpVelocity, 0.0D));
                this.hasImpulse = true;
            }
            this.playerJumpPendingScale = 0.0F;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return com.soybe.awakeningsystem.ModSounds.WHITE_TIGER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return com.soybe.awakeningsystem.ModSounds.WHITE_TIGER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return com.soybe.awakeningsystem.ModSounds.WHITE_TIGER_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob otherParent) {
        return ModEntities.WHITE_THUNDER_TIGER.get().create(serverLevel);
    }
}
