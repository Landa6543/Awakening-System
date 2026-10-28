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
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Dragón Chino Celestial Ancestral de Murim (Shenlong / Qinglong / 龙).
 * Soberana bestia divina colosal de los Nueve Cielos:
 * - Gran tamaño y cuerpo serpentino multicubicular que se ondula suavemente en tiempo real.
 * - Jefe supremo con barra de vida celestial dorada en Murim.
 * - Aliento de Qi divino, relámpagos ancestrales y barrido de viento huracanado.
 * - Sometible y domesticable con la Perla del Dragón Celestial (montura voladora 3D).
 */
public class CelestialDragonEntity extends TamableAnimal implements FlyingAnimal, RangedAttackMob, PlayerRideableJumping, DragonPartParent {

    public static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(CelestialDragonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_BREATHING =
            SynchedEntityData.defineId(CelestialDragonEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(
            Component.translatable("awakening_system.boss.celestial_dragon.name"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_10
    )).setDarkenScreen(false).setPlayBossMusic(true);

    private int roarAnimationTicks = 0;
    private int thunderCooldown = 200;

    public static final int SEGMENT_COUNT = 32;
    public final float[] segmentYaw = new float[SEGMENT_COUNT];
    public final float[] segmentYawO = new float[SEGMENT_COUNT];
    public final float[] segmentPitch = new float[SEGMENT_COUNT];
    public final float[] segmentPitchO = new float[SEGMENT_COUNT];
    private boolean segmentsInitialized = false;

    public final DragonBodyPart[] subParts;
    public static final int[] PART_SEGMENT_INDICES = new int[] {
        1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31
    };
    public final Vec3[] segmentPositions = new Vec3[SEGMENT_COUNT];

    private boolean clientSpaceHeld = false;
    private boolean clientShiftHeld = false;
    private boolean clientSprintHeld = false;

    public void setFlightInput(boolean space, boolean shift, boolean sprint) {
        this.clientSpaceHeld = space;
        this.clientShiftHeld = shift;
        this.clientSprintHeld = sprint;
    }

    public float getSegmentYaw(int index, float partialTick) {
        if (!this.segmentsInitialized || index < 0 || index >= SEGMENT_COUNT) return this.getYRot();
        return Mth.rotLerp(partialTick, this.segmentYawO[index], this.segmentYaw[index]);
    }

    public float getSegmentPitch(int index, float partialTick) {
        if (!this.segmentsInitialized || index < 0 || index >= SEGMENT_COUNT) return this.getXRot();
        return Mth.lerp(partialTick, this.segmentPitchO[index], this.segmentPitch[index]);
    }

    public CelestialDragonEntity(EntityType<? extends CelestialDragonEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setTame(false, false);

        this.subParts = new DragonBodyPart[16];
        this.subParts[0] = new DragonBodyPart(this, "neck_front", 2.6F, 2.6F);
        this.subParts[1] = new DragonBodyPart(this, "neck_mid", 2.8F, 2.8F);
        this.subParts[2] = new DragonBodyPart(this, "neck_rear", 3.0F, 3.0F);
        this.subParts[3] = new DragonBodyPart(this, "chest_front", 3.2F, 3.2F);
        this.subParts[4] = new DragonBodyPart(this, "chest_mid", 3.4F, 3.4F);
        this.subParts[5] = new DragonBodyPart(this, "body_front", 3.4F, 3.4F);
        this.subParts[6] = new DragonBodyPart(this, "body_mid_1", 3.4F, 3.4F);
        this.subParts[7] = new DragonBodyPart(this, "body_mid_2", 3.2F, 3.2F);
        this.subParts[8] = new DragonBodyPart(this, "body_rear_1", 3.2F, 3.2F);
        this.subParts[9] = new DragonBodyPart(this, "body_rear_2", 3.0F, 3.0F);
        this.subParts[10] = new DragonBodyPart(this, "pelvis", 2.8F, 2.8F);
        this.subParts[11] = new DragonBodyPart(this, "tail_base", 2.6F, 2.6F);
        this.subParts[12] = new DragonBodyPart(this, "tail_upper", 2.4F, 2.4F);
        this.subParts[13] = new DragonBodyPart(this, "tail_mid", 2.2F, 2.2F);
        this.subParts[14] = new DragonBodyPart(this, "tail_lower", 2.0F, 2.0F);
        this.subParts[15] = new DragonBodyPart(this, "tail_tip", 1.8F, 1.8F);

        for (int i = 0; i < SEGMENT_COUNT; i++) {
            this.segmentPositions[i] = Vec3.ZERO;
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public net.neoforged.neoforge.entity.PartEntity<?>[] getParts() {
        return this.subParts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.subParts != null) {
            for (int i = 0; i < this.subParts.length; i++) {
                this.subParts[i].setId(id + i + 1);
            }
        }
    }

    @Override
    public boolean hurtFromPart(DragonBodyPart part, DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        Entity passenger = this.getFirstPassenger();
        if (passenger != null && source.getEntity() == passenger) {
            return false;
        }
        return this.hurt(source, amount);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.subParts != null) {
            for (DragonBodyPart part : this.subParts) {
                part.discard();
            }
        }
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return this.getBoundingBox().inflate(120.0D, 80.0D, 120.0D);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4500.0D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROARING, false);
        builder.define(DATA_BREATHING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new CelestialDragonBreathGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.3D, 24.0F, 12.0F) {
            @Override
            public boolean canUse() {
                return !CelestialDragonEntity.this.isVehicle() && !CelestialDragonEntity.this.isOrderedToSit() && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.85D) {
            @Override
            public boolean canUse() {
                return !CelestialDragonEntity.this.isTame() && !CelestialDragonEntity.this.isVehicle() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F) {
            @Override
            public boolean canUse() {
                return !CelestialDragonEntity.this.isTame() && !CelestialDragonEntity.this.isVehicle() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !CelestialDragonEntity.this.isTame() && !CelestialDragonEntity.this.isVehicle() && super.canUse();
            }
        });

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isVehicle()) {
            this.getNavigation().stop();
            this.setTarget(null);
        }

        if (this.isOrderedToSit()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(false);
            this.getNavigation().stop();
            this.setTarget(null);
        } else {
            this.setNoGravity(this.isVehicle() || !this.onGround());
        }
        this.fallDistance = 0.0F;

        if (!this.segmentsInitialized) {
            for (int i = 0; i < SEGMENT_COUNT; i++) {
                this.segmentYaw[i] = this.segmentYawO[i] = this.getYRot();
                this.segmentPitch[i] = this.segmentPitchO[i] = this.getXRot();
            }
            this.segmentsInitialized = true;
        }

        if (!this.isVehicle() || !(this.getFirstPassenger() instanceof Player)) {
            this.segmentYawO[0] = this.segmentYaw[0];
            this.segmentPitchO[0] = this.segmentPitch[0];
            this.segmentYaw[0] = this.getYRot();
            this.segmentPitch[0] = this.getXRot();

            float followSpeed = 0.22F;
            for (int i = 1; i < SEGMENT_COUNT; i++) {
                this.segmentYawO[i] = this.segmentYaw[i];
                this.segmentPitchO[i] = this.segmentPitch[i];

                float diffYaw = Mth.wrapDegrees(this.segmentYaw[i - 1] - this.segmentYaw[i]);
                this.segmentYaw[i] += diffYaw * followSpeed;

                float diffPitch = this.segmentPitch[i - 1] - this.segmentPitch[i];
                this.segmentPitch[i] += diffPitch * followSpeed;
            }
        }

        if (this.roarAnimationTicks > 0) {
            this.roarAnimationTicks--;
            if (this.roarAnimationTicks == 0) {
                this.entityData.set(DATA_ROARING, false);
            }
        }

        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (this.getTarget() != null && this.getTarget().isAlive()) {
                this.thunderCooldown--;
                if (this.thunderCooldown <= 0) {
                    this.triggerThunderStorm(this.getTarget());
                    this.thunderCooldown = 240 + this.random.nextInt(120);
                }
            }
        } else {
            if (this.random.nextFloat() < 0.35F) {
                double px = this.getX() + (this.random.nextDouble() - 0.5) * 2.5;
                double py = this.getY() + this.random.nextDouble() * 2.0;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 2.5;
                this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.04, 0);
                if (this.isBreathing()) {
                    this.level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, 0, 0.08, 0);
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.isDeadOrDying() || this.isRemoved()) {
            super.tick();
            return;
        }

        this.yRotO = this.getYRot();
        this.yBodyRotO = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.yHeadRotO = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.xRotO = this.getXRot();

        super.tick();

        this.yBodyRot = this.getYRot();
        this.updateSubPartPositions();
    }

    private void updateSubPartPositions() {
        this.segmentPositions[0] = this.position();
        double segDist = 2.0D;
        for (int i = 1; i < SEGMENT_COUNT; i++) {
            float yawRad = this.segmentYaw[i] * ((float) Math.PI / 180.0F);
            float pitchRad = this.segmentPitch[i] * ((float) Math.PI / 180.0F);
            double dx = Math.sin(yawRad) * Math.cos(pitchRad) * segDist;
            double dy = Math.sin(pitchRad) * segDist;
            double dz = -Math.cos(yawRad) * Math.cos(pitchRad) * segDist;
            this.segmentPositions[i] = this.segmentPositions[i - 1].add(dx, dy, dz);
        }

        if (this.subParts != null) {
            for (int k = 0; k < this.subParts.length; k++) {
                int segIdx = PART_SEGMENT_INDICES[k];
                Vec3 p = this.segmentPositions[segIdx];
                DragonBodyPart part = this.subParts[k];
                part.xo = part.getX();
                part.yo = part.getY();
                part.zo = part.getZ();
                part.xOld = part.getX();
                part.yOld = part.getY();
                part.zOld = part.getZ();
                part.setPos(p.x, p.y, p.z);
            }
        }
    }

    private void triggerThunderStorm(LivingEntity target) {
        this.triggerRoar();
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 4; i++) {
                double rx = target.getX() + (this.random.nextDouble() - 0.5) * 8.0;
                double rz = target.getZ() + (this.random.nextDouble() - 0.5) * 8.0;
                BlockPos strikePos = BlockPos.containing(rx, target.getY(), rz);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                    bolt.setVisualOnly(false);
                    serverLevel.addFreshEntity(bolt);
                }
            }
        }
    }

    public void triggerRoar() {
        this.entityData.set(DATA_ROARING, true);
        this.roarAnimationTicks = 35;
        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 3.0F, 0.95F);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.entityData.set(DATA_BREATHING, true);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - (this.getY() + 1.2D);
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        CelestialDragonBreathEntity breath = new CelestialDragonBreathEntity(
                this.level(), this, dx * 0.08, dy * 0.08, dz * 0.08);
        this.level().addFreshEntity(breath);
        this.playSound(ModSounds.CELESTIAL_DRAGON_BREATH.get(), 2.0F, 1.0F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.is(SystemItems.CELESTIAL_DRAGON_PEARL.get()) && !this.isTame()) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.tame(player);
                this.triggerRoar();
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 2.0D, this.getZ(), 80, 2.0D, 2.0D, 2.0D, 0.4D);
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 2.0D, this.getZ(), 60, 2.0D, 1.5D, 2.0D, 0.1D);
                }
                player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.recognized"), false);
                if (player instanceof ServerPlayer sp) {
                    AwakeningSystemMod.notify(sp,
                            "awakening_system.entity.celestial_dragon.bond_title",
                            "awakening_system.entity.celestial_dragon.bond_desc",
                            0xFFFFD700);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && itemstack.is(SystemItems.CELESTIAL_DRAGON_PEARL.get())) {
            if (this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide) {
                    this.heal(this.getMaxHealth());
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.triggerRoar();
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 2.0D, this.getZ(), 30, 1.5D, 1.5D, 1.5D, 0.2D);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 2.0D, this.getZ(), 60, 1.5D, 1.5D, 1.5D, 0.3D);
                    }
                    player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.pearl_restored"), true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                if (player.isSecondaryUseActive()) {
                    boolean newSit = !this.isOrderedToSit();
                    this.setOrderedToSit(newSit);
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    if (newSit) {
                        player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.resting"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.taking_flight"), true);
                    }
                } else {
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    player.startRiding(this);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.isTame() && player.getAbilities().instabuild && itemstack.isEmpty()) {
            if (!this.level().isClientSide) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                player.displayClientMessage(Component.translatable("awakening_system.entity.celestial_dragon.creative_tamed"), false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
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

                float targetPitch;
                if (this.clientSpaceHeld) {
                    targetPitch = -65.0F;
                } else if (this.clientShiftHeld) {
                    targetPitch = 65.0F;
                } else {
                    targetPitch = player.getXRot() * 0.45F;
                }

                float currentPitch = this.getXRot();
                float newPitch = Mth.rotLerp(0.25F, currentPitch, targetPitch);

                this.setXRot(newPitch);
                this.setYRot(player.getYRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();

                this.updateSegmentsRidden();

                float forward = player.zza;
                float strafe = player.xxa;

                boolean isSprinting = this.clientSprintHeld || player.isSprinting();
                double speed = isSprinting ? 1.05D : 0.55D;

                double radYaw = Math.toRadians(player.getYRot());
                double fwdX = -Math.sin(radYaw);
                double fwdZ = Math.cos(radYaw);
                double sideX = Math.cos(radYaw);
                double sideZ = Math.sin(radYaw);

                double vx = 0.0D;
                double vz = 0.0D;

                if (forward != 0.0F || strafe != 0.0F) {
                    vx = (fwdX * forward + sideX * strafe) * speed;
                    vz = (fwdZ * forward + sideZ * strafe) * speed;
                } else {
                    Vec3 cur = this.getDeltaMovement();
                    vx = cur.x * 0.70D;
                    vz = cur.z * 0.70D;
                    if (Math.abs(vx) < 0.005D) vx = 0.0D;
                    if (Math.abs(vz) < 0.005D) vz = 0.0D;
                }

                double vy = 0.0D;
                if (this.clientSpaceHeld) {
                    vy = 0.65D;
                    if (this.onGround()) {
                        vy = 0.75D;
                        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 1.5F, 1.2F);
                    }
                } else if (this.clientShiftHeld) {
                    vy = -0.65D;
                } else if (this.onGround()) {
                    vy = 0.0D;
                } else {
                    if (forward != 0.0F || strafe != 0.0F) {
                        vy = -0.015D;
                    } else {
                        vy = 0.0D;
                    }
                }

                this.setDeltaMovement(vx, vy, vz);
                this.move(MoverType.SELF, this.getDeltaMovement());
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            float pitchRad = this.getXRot() * ((float) Math.PI / 180.0F);
            float yawRad = this.getYRot() * ((float) Math.PI / 180.0F);

            double yl = -5.0D / 16.0D;
            double zl = 2.0D / 16.0D;

            double cosP = Math.cos(pitchRad);
            double sinP = Math.sin(pitchRad);
            double yNeck = (8.0D / 16.0D) + (yl * cosP - zl * sinP);
            double zNeck = (-14.0D / 16.0D) + (yl * sinP + zl * cosP);

            double dy = -3.6D * (yNeck - 1.501D);
            double forwardDist = -3.6D * zNeck;

            double dx = -Math.sin(yawRad) * forwardDist;
            double dz = Math.cos(yawRad) * forwardDist;

            moveFunction.accept(passenger, this.getX() + dx, this.getY() + dy, this.getZ() + dz);
        }
    }

    private void updateSegmentsRidden() {
        if (!this.segmentsInitialized) {
            for (int i = 0; i < SEGMENT_COUNT; i++) {
                this.segmentYaw[i] = this.segmentYawO[i] = this.getYRot();
                this.segmentPitch[i] = this.segmentPitchO[i] = this.getXRot();
            }
            this.segmentsInitialized = true;
        }

        this.segmentYawO[0] = this.segmentYaw[0];
        this.segmentPitchO[0] = this.segmentPitch[0];
        this.segmentYaw[0] = this.getYRot();
        this.segmentPitch[0] = this.getXRot();

        float followSpeed = 0.28F;
        for (int i = 1; i < SEGMENT_COUNT; i++) {
            this.segmentYawO[i] = this.segmentYaw[i];
            this.segmentPitchO[i] = this.segmentPitch[i];

            float diffYaw = Mth.wrapDegrees(this.segmentYaw[i - 1] - this.segmentYaw[i]);
            this.segmentYaw[i] += diffYaw * followSpeed;

            float diffPitch = this.segmentPitch[i - 1] - this.segmentPitch[i];
            this.segmentPitch[i] += diffPitch * followSpeed;
        }
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return this.isVehicle() && this.getFirstPassenger() instanceof Player player ? player.isLocalPlayer() : this.isEffectiveAi();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean shouldRiderFaceForward(Player player) {
        return false;
    }

    @Override
    public void onPassengerTurned(Entity entityToUpdate) {
    }

    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) this.getAttributeValue(Attributes.FLYING_SPEED) * 1.5F;
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        this.clientSpaceHeld = true;
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int jumpPower) {
        this.clientSpaceHeld = true;
    }

    @Override
    public void handleStopJump() {
        this.clientSpaceHeld = false;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof Player player ? player : null;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    public boolean isRoaring() {
        return this.entityData.get(DATA_ROARING);
    }

    public boolean isBreathing() {
        return this.entityData.get(DATA_BREATHING);
    }

    public void setBreathing(boolean breathing) {
        this.entityData.set(DATA_BREATHING, breathing);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get(), 1 + this.random.nextInt(2)));
        this.spawnAtLocation(new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 4 + this.random.nextInt(5)));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isTame() || !this.isAlive()) {
            this.bossEvent.removeAllPlayers();
            return;
        }
        if (this.level() instanceof ServerLevel sl) {
            for (ServerPlayer sp : sl.players()) {
                if (this.distanceToSqr(sp) <= 36.0D * 36.0D) {
                    if (!this.bossEvent.getPlayers().contains(sp)) {
                        this.bossEvent.addPlayer(sp);
                    }
                } else {
                    if (this.bossEvent.getPlayers().contains(sp)) {
                        this.bossEvent.removePlayer(sp);
                    }
                }
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isTame() && this.distanceToSqr(player) <= 36.0D * 36.0D) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CELESTIAL_DRAGON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.CELESTIAL_DRAGON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CELESTIAL_DRAGON_DEATH.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    static class CelestialDragonBreathGoal extends Goal {
        private final CelestialDragonEntity dragon;
        private int attackTime;

        public CelestialDragonBreathGoal(CelestialDragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.dragon.getTarget();
            return target != null && target.isAlive() && this.dragon.distanceToSqr(target) > 25.0D && this.dragon.distanceToSqr(target) < 1600.0D;
        }

        @Override
        public void start() {
            this.attackTime = 0;
            this.dragon.setBreathing(true);
        }

        @Override
        public void stop() {
            this.dragon.setBreathing(false);
        }

        @Override
        public void tick() {
            LivingEntity target = this.dragon.getTarget();
            if (target == null) return;

            this.dragon.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.attackTime++;

            if (this.attackTime % 15 == 0 && this.attackTime <= 45) {
                this.dragon.performRangedAttack(target, 1.0F);
            }
        }
    }
}
