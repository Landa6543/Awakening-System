package com.soybe.awakeningsystem.entity;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Grulla Celestial de las Nubes (Cloud Crane).
 * Montura voladora mística sagrada del reino de cultivo (Murim / Xianxia):
 * - Ave zancuda esbelta de plumaje blanco níveo, remates negros y corona carmesí.
 * - Domesticable con semillas o bayas.
 * - Al descansar se acuesta en el suelo como las aves reales (gallinas/pájaros).
 * - Montable y voladora: control fluido 3D sin tirones ni jitter.
 * - Planeo suave y estela de niebla celestial.
 */
public class CloudCraneEntity extends TamableAnimal implements PlayerRideableJumping {

    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(CloudCraneEntity.class, EntityDataSerializers.BOOLEAN);

    private float playerJumpPendingScale = 0.0F;
    private int inAirTicks = 0;

    public CloudCraneEntity(EntityType<? extends CloudCraneEntity> type, Level level) {
        super(type, level);
        this.setTame(false, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FLYING_SPEED, 0.42D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.15D, 10.0F, 3.0F));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    public boolean isCraneFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public void setCraneFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MELON_SEEDS) ||
               stack.is(Items.PUMPKIN_SEEDS) || stack.is(Items.BEETROOT_SEEDS) ||
               stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES) ||
               stack.is(Items.COD) || stack.is(Items.SALMON);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 1. Si NO está domesticada
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
                        this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.2F);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // Humo
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }

        // 2. Si YA está domesticada y pertenece al jugador
        if (this.isOwnedBy(player)) {
            // Curar con alimento
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.usePlayerItem(player, hand, itemstack);
                this.heal(6.0F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.02D);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Shift + Clic derecho: Acostarse / Levantarse (postura de nido de ave)
            if (player.isShiftKeyDown()) {
                boolean wantsToLieDown = !this.isOrderedToSit();
                this.setOrderedToSit(wantsToLieDown);
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                this.playSound(wantsToLieDown ? SoundEvents.PARROT_FLY : SoundEvents.PARROT_AMBIENT, 0.9F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            // Montar para volar
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
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        // Colocar al jinete exactamente en el lomo (sin flotar) y centrado al 100% (x = 0.0)
        double yOff = this.isInSittingPose() ? 0.18D : 0.52D;
        return new Vec3(0.0D, yOff * (double) scale, -0.10D * (double) scale);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            double yOff = this.isInSittingPose() ? 0.18D : 0.52D;
            // x = 0.0D garantiza alineación simétrica perfecta en 1ra y 3ra persona
            Vec3 localOffset = new Vec3(0.0D, yOff, -0.10D);
            Vec3 worldOffset = localOffset.yRot(-this.getYRot() * ((float) Math.PI / 180.0F));
            moveFunction.accept(passenger, this.getX() + worldOffset.x, this.getY() + worldOffset.y, this.getZ() + worldOffset.z);
        }
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
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.isCraneFlying()) {
                // Vuelo 3D directo sin gravedad terrestre en conflicto
                this.move(MoverType.SELF, this.getDeltaMovement());
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        // 1. Sincronización instantánea de rotación con la mirada del jugador (cero desfase visual)
        this.setRot(player.getYRot(), player.getXRot());
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yBodyRotO = this.yHeadRotO = this.yBodyRot;

        boolean onGround = this.onGround();
        // Consumir el salto del jugador en este tick e INMEDIATAMENTE ponerlo a 0 para que no se quede subiendo infinito
        boolean jumpTriggered = this.playerJumpPendingScale > 0.05F;
        this.playerJumpPendingScale = 0.0F;

        if (jumpTriggered) {
            // Despegue desde tierra o aleteo ascendente
            Vec3 cur = this.getDeltaMovement();
            this.setDeltaMovement(cur.x, 0.52D, cur.z);
            this.setCraneFlying(true);
            this.playSound(SoundEvents.PARROT_FLY, 1.0F, 1.1F);
        }

        if (this.isCraneFlying()) {
            Vec3 look = player.getLookAngle();
            float forward = player.zza;
            float strafe = player.xxa;
            double speed = 0.48D;

            Vec3 cur = this.getDeltaMovement();
            double vx = cur.x;
            double vy = cur.y;
            double vz = cur.z;

            if (forward > 0.0F) {
                // VUELO DIRECCIONAL 3D REAL SEGÚN EL CURSOR:
                // - Mirar arriba -> sube en la dirección del cursor
                // - Mirar abajo -> desciende / pica hacia el suelo
                // - Mirar al horizonte -> vuela nivelado
                vx = look.x * speed;
                vy = look.y * speed;
                vz = look.z * speed;

                if (strafe != 0.0F) {
                    Vec3 side = look.cross(new Vec3(0, 1, 0)).normalize().scale(strafe * speed * 0.35D);
                    vx += -side.x;
                    vz += -side.z;
                }
            } else if (forward < 0.0F) {
                // Retroceso lento en el aire
                vx = -look.x * speed * 0.25D;
                vz = -look.z * speed * 0.25D;
                vy = -0.15D;
            } else {
                // Sin presionar W: planeo aerodinámico natural
                vx = cur.x * 0.88D;
                vz = cur.z * 0.88D;
                // Si mira abajo aunque no presione W, también desciende hacia donde apunta
                if (look.y < -0.10D) {
                    vy = look.y * 0.38D;
                } else {
                    vy = -0.14D; // Caída suave de planeo hacia la tierra para aterrizar
                }
            }

            // Si toca tierra mientras descendía: aterriza y pasa a caminata
            if (onGround && vy <= 0.0D && !jumpTriggered) {
                this.setCraneFlying(false);
                vy = 0.0D;
            }

            this.setDeltaMovement(vx, vy, vz);
            player.fallDistance = 0.0F;
            this.fallDistance = 0.0F;
        } else {
            // En tierra caminando normalmente
            this.setCraneFlying(false);
        }
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return this.isCraneFlying() ? 0.48F : 0.24F;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Si está en tierra, apagar estado de vuelo
        if (this.onGround() && this.isCraneFlying()) {
            this.setCraneFlying(false);
        }

        // Si está en el aire salvaje (sin jinete)
        boolean inAir = !this.onGround() && !this.isInSittingPose();
        if (inAir) {
            this.inAirTicks++;
            if (this.inAirTicks > 2 && !this.isCraneFlying()) {
                this.setCraneFlying(true);
            }
        } else {
            this.inAirTicks = 0;
            if (this.isCraneFlying() && !this.isVehicle()) {
                this.setCraneFlying(false);
            }
        }

        // Planeo suave para aves salvajes sin jinete
        if (!this.onGround() && !this.isInSittingPose() && !this.isVehicle()) {
            Vec3 vel = this.getDeltaMovement();
            if (vel.y < 0.0D) {
                this.setDeltaMovement(vel.x * 0.95D, Math.max(vel.y * 0.65D, -0.12D), vel.z * 0.95D);
            }
            this.fallDistance = 0.0F;
        }

        // Estela mística de nubes en vuelo
        if (this.level().isClientSide && this.isCraneFlying()) {
            if (this.random.nextInt(3) == 0) {
                double ox = (this.random.nextDouble() - 0.5D) * 1.6D;
                double oy = -0.2D + (this.random.nextDouble() - 0.5D) * 0.3D;
                double oz = (this.random.nextDouble() - 0.5D) * 1.6D;

                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        0.0D, -0.01D, 0.0D);
            }
        }
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        this.playerJumpPendingScale = 1.0F;
    }

    @Override
    public boolean canJump() {
        return this.isTame();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        this.playSound(SoundEvents.PARROT_FLY, 0.9F, 1.2F);
    }

    @Override
    public void handleStopJump() {
        this.playerJumpPendingScale = 0.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.25F, 1.0F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob otherParent) {
        return ModEntities.CLOUD_CRANE.get().create(serverLevel);
    }
}
