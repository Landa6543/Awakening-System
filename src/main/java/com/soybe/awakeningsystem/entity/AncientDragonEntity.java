package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.animal.FlyingAnimal;
import java.util.EnumSet;

/**
 * Dragón Ancestral Occidental Colosal.
 * - Textura FOTO 2 restaurada al 100% (escamas volcánicas/glaciales profundas, placas gastróstegas, crestas luminosas).
 * - Alas colosales wyvern contiguas: CERO solapamiento y CERO parpadeo (Z-fighting).
 * - Disparo del aliento elemental directamente desde las fauces del hocico (no del pecho).
 * - Sonidos personalizados feroces con identidad propia (ModSounds.ANCIENT_DRAGON_*).
 * - Físicas de vuelo idénticas al Dragón Celestial Asiático (ascenso dinámico con cabeceo, picada y flotación firme).
 */
public class AncientDragonEntity extends TamableAnimal implements FlyingAnimal, RangedAttackMob, DragonPartParent {

    public static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_SADDLED =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_DRAGON_AGE =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.INT);

    public static final int VARIANT_FIRE = 0;
    public static final int VARIANT_ICE = 1;
    public static final int VARIANT_FOREST = 2;
    public static final int VARIANT_SHADOW = 3;
    public static final int VARIANT_LIGHTNING = 4;
    public static final int VARIANT_CELESTIAL = 5;

    public static final int ADULT_AGE_TICKS = 144000;

    public final DragonBodyPart[] subParts;

    @Nullable
    private java.util.UUID enragedThiefUUID = null;

    private int roarTimer = 0;
    private int breathCooldown = 0;
    private boolean clientSpaceHeld = false;
    private boolean clientShiftHeld = false;
    private boolean clientSprintHeld = false;

    private float prevFlyProgress = 0.0F;
    private float flyProgress = 0.0F;
    private int takeoffImmunityTicks = 0;

    public float getFlyProgress(float partialTick) {
        return Mth.lerp(partialTick, this.prevFlyProgress, this.flyProgress);
    }

    public void setFlightInput(boolean space, boolean shift, boolean sprint) {
        this.clientSpaceHeld = space;
        this.clientShiftHeld = shift;
        this.clientSprintHeld = sprint;
    }

    public AncientDragonEntity(EntityType<? extends AncientDragonEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setTame(false, false);
        this.xpReward = 1500;

        this.subParts = new DragonBodyPart[5];
        this.subParts[0] = new DragonBodyPart(this, "head", 3.2F, 3.2F);
        this.subParts[1] = new DragonBodyPart(this, "neck", 3.0F, 3.0F);
        this.subParts[2] = new DragonBodyPart(this, "body", 5.5F, 4.5F);
        this.subParts[3] = new DragonBodyPart(this, "tail_front", 2.6F, 2.6F);
        this.subParts[4] = new DragonBodyPart(this, "tail_rear", 2.4F, 2.4F);
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
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) 
                || source.is(DamageTypes.LAVA) || source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_WALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA)) {
            return false;
        }
        return super.hurt(source, amount);
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

    public AABB getRenderBoundingBox() {
        return this.getBoundingBox().inflate(64.0D, 32.0D, 64.0D);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.60D)
                .add(Attributes.FLYING_SPEED, 1.80D)
                .add(Attributes.ATTACK_DAMAGE, 32.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.STEP_HEIGHT, 2.5D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, VARIANT_FIRE);
        builder.define(DATA_FLYING, false);
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_ROARING, false);
        builder.define(DATA_DRAGON_AGE, ADULT_AGE_TICKS);
    }

    public int getDragonAge() {
        return this.entityData.get(DATA_DRAGON_AGE);
    }

    public void setDragonAge(int age) {
        this.entityData.set(DATA_DRAGON_AGE, Math.max(0, age));
        this.refreshDimensions();
    }

    /**
     * Escala continua estilo Ice and Fire:
     * - Cría recién nacida (Día 0, age 0): 0.85F
     * - Juvenil (Día 2, age ~48,000): ~1.9F
     * - Adolescente (Día 4, age ~96,000): ~2.95F
     * - Adulto Colosal (Día 6+, age >= 144,000): 4.0F
     */
    public float getDragonScale() {
        int age = getDragonAge();
        if (age >= ADULT_AGE_TICKS) return 4.0F;
        float progress = (float) age / (float) ADULT_AGE_TICKS;
        return 0.85F + progress * (4.0F - 0.85F);
    }

    @Override
    public boolean isBaby() {
        return getDragonAge() < 24000;
    }

    @Override
    public void setBaby(boolean baby) {
        setDragonAge(baby ? 0 : ADULT_AGE_TICKS);
    }

    public boolean isJuvenile() {
        int age = getDragonAge();
        return age >= 24000 && age < 72000;
    }

    public boolean isAdolescent() {
        int age = getDragonAge();
        return age >= 72000 && age < ADULT_AGE_TICKS;
    }

    public boolean isAdult() {
        return getDragonAge() >= ADULT_AGE_TICKS;
    }

    public void setEnragedThief(@Nullable Player thief) {
        if (thief != null) {
            this.enragedThiefUUID = thief.getUUID();
            this.setTarget(thief);
            if (!this.isFlying()) {
                this.setFlying(true);
                this.setNoGravity(true);
                this.takeoffImmunityTicks = 40;
                Vec3 cur = this.getDeltaMovement();
                this.setDeltaMovement(cur.x, 0.85D, cur.z);
                this.playSound(ModSounds.ANCIENT_DRAGON_ROAR.get(), 3.5F, getVoicePitch());
            }
        } else {
            this.enragedThiefUUID = null;
        }
    }

    public @Nullable java.util.UUID getEnragedThiefUUID() {
        return this.enragedThiefUUID;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(this.getDragonScale() / 4.0F);
    }

    @Override
    public float getVoicePitch() {
        float scale = getDragonScale();
        float progress = Math.min(1.0F, Math.max(0.0F, (scale - 0.85F) / (4.0F - 0.85F)));
        return 1.6F - progress * (1.6F - 0.85F);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, 5));
    }

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
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.25D, 35, 24.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.3D, 24.0F, 12.0F) {
            @Override
            public boolean canUse() {
                return !AncientDragonEntity.this.isVehicle() && !AncientDragonEntity.this.isOrderedToSit() && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new AncientDragonWildTakeoffGoal(this));
        this.goalSelector.addGoal(5, new AncientDragonWildLandGoal(this));
        this.goalSelector.addGoal(6, new AncientDragonWildFlightGoal(this));
        this.goalSelector.addGoal(7, new AncientDragonWildWalkGoal(this, 0.55D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F) {
            @Override
            public boolean canUse() {
                return !AncientDragonEntity.this.isTame() && !AncientDragonEntity.this.isVehicle() && !AncientDragonEntity.this.isFlying() && super.canUse();
            }
        });
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !AncientDragonEntity.this.isTame() && !AncientDragonEntity.this.isVehicle() && !AncientDragonEntity.this.isFlying() && super.canUse();
            }
        });

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !AncientDragonEntity.this.isTame() && super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                entity -> !AncientDragonEntity.this.isTame()));
    }

    @Override
    public void tick() {
        if (this.isDeadOrDying() || this.isRemoved()) {
            super.tick();
            return;
        }

        this.yRotO = this.getYRot();
        this.yBodyRotO = this.yBodyRot;
        this.xRotO = this.getXRot();

        super.tick();
        this.updateSubPartPositions();

        this.prevFlyProgress = this.flyProgress;
        if (this.isFlying() && !this.onGround()) {
            this.flyProgress = Math.min(1.0F, this.flyProgress + 0.10F);
        } else {
            this.flyProgress = Math.max(0.0F, this.flyProgress - 0.10F);
        }
    }

    private void updateSubPartPositions() {
        if (this.subParts == null) return;

        float scaleFactor = this.getDragonScale() / 4.0F;
        float yawRad = (float) Math.toRadians(this.getYRot());
        float pitchRad = (float) Math.toRadians(this.getXRot());

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);

        double fwdX = -sinYaw * cosPitch;
        double fwdY = -sinPitch;
        double fwdZ = cosYaw * cosPitch;

        double rightX = cosYaw;
        double rightZ = sinYaw;

        double bx = this.getX();
        double by = this.getY();
        double bz = this.getZ();

        setPartPosition(this.subParts[0], bx + fwdX * 9.0D * scaleFactor, by + (8.5D + fwdY * 9.0D) * scaleFactor, bz + fwdZ * 9.0D * scaleFactor);

        setPartPosition(this.subParts[1], bx + fwdX * 4.8D * scaleFactor, by + (7.8D + fwdY * 4.8D) * scaleFactor, bz + fwdZ * 4.8D * scaleFactor);

        setPartPosition(this.subParts[2], bx, by + 6.5D * scaleFactor, bz);

        setPartPosition(this.subParts[3], bx - fwdX * 4.8D * scaleFactor, by + 5.5D * scaleFactor, bz - fwdZ * 4.8D * scaleFactor);

        setPartPosition(this.subParts[4], bx - fwdX * 9.5D * scaleFactor, by + 4.5D * scaleFactor, bz - fwdZ * 9.5D * scaleFactor);
    }

    private void setPartPosition(DragonBodyPart part, double x, double y, double z) {
        part.xo = part.getX();
        part.yo = part.getY();
        part.zo = part.getZ();
        part.xOld = part.getX();
        part.yOld = part.getY();
        part.zOld = part.getZ();
        part.setPos(x, y, z);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.takeoffImmunityTicks > 0) {
            this.takeoffImmunityTicks--;
        }

        if (this.isVehicle()) {
            this.getNavigation().stop();
            this.setTarget(null);
        }

        if (this.isOrderedToSit()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(false);
            this.getNavigation().stop();
            this.setTarget(null);
            if (!this.level().isClientSide) {
                this.setFlying(false);
            }
        } else if (this.isFlying() && !this.onGround()) {
            this.setNoGravity(true);
        } else {
            this.setNoGravity(false);
            if (!this.level().isClientSide && this.onGround() && this.isFlying() && this.takeoffImmunityTicks == 0 && !this.clientSpaceHeld) {
                this.setFlying(false);
            }
        }
        this.fallDistance = 0.0F;

        if (this.isFlying() && !this.onGround()) {
            if (this.level().isClientSide && this.tickCount % 32 == 0 && (this.clientSpaceHeld || this.getDeltaMovement().horizontalDistanceSqr() > 0.02D || !this.isVehicle())) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        ModSounds.ANCIENT_DRAGON_FLAP.get(), SoundSource.NEUTRAL, 1.8F, 0.85F, false);
            }
        }

        if (this.breathCooldown > 0) {
            this.breathCooldown--;
        }

        if (this.roarTimer > 0) {
            this.roarTimer--;
            if (this.roarTimer == 0) {
                setRoaring(false);
            }
        }

        if (!this.level().isClientSide) {
            int age = getDragonAge();
            if (age < ADULT_AGE_TICKS) {
                setDragonAge(age + 1);
                if (this.tickCount % 60 == 0) {
                    this.refreshDimensions();
                    if (this.subParts != null) {
                        for (DragonBodyPart part : this.subParts) {
                            part.refreshDimensions();
                        }
                    }
                }
            }

            if (this.enragedThiefUUID != null) {
                Player thief = this.level().getPlayerByUUID(this.enragedThiefUUID);
                if (thief != null && thief.isAlive() && !this.isTame()) {
                    double dist = this.distanceTo(thief);
                    if (dist > 100.0D) {
                        this.enragedThiefUUID = null;
                        this.setTarget(null);
                        this.getNavigation().stop();
                    } else {
                        if (this.getTarget() != thief) {
                            this.setTarget(thief);
                        }
                    }
                } else {
                    this.enragedThiefUUID = null;
                    if (this.getTarget() instanceof Player) {
                        this.setTarget(null);
                    }
                }
            }

            if (this.isVehicle()) {
                if (this.onGround() && !this.clientSpaceHeld && isFlying()) {
                    setFlying(false);
                }
            } else {
                if (this.onGround() && isFlying() && this.takeoffImmunityTicks == 0) {
                    setFlying(false);
                } else if (!this.isFlying() && !this.onGround() && this.fallDistance > 5.0F && this.getY() - this.level().getMinBuildHeight() > 6) {
                    setFlying(true);
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.25D, this.getDeltaMovement().z);
                }
            }

            if (!this.isVehicle() && this.tickCount % 2 == 0) {
                java.util.List<AncientDragonEntity> neighbors = this.level().getEntitiesOfClass(
                        AncientDragonEntity.class,
                        this.getBoundingBox().inflate(10.0D),
                        other -> other != this && other.isAlive()
                );
                if (!neighbors.isEmpty()) {
                    double pushX = 0.0D;
                    double pushZ = 0.0D;
                    for (AncientDragonEntity other : neighbors) {
                        double dX = this.getX() - other.getX();
                        double dZ = this.getZ() - other.getZ();
                        double distSq = dX * dX + dZ * dZ;
                        if (distSq > 0.001D && distSq < 100.0D) {
                            double dist = Math.sqrt(distSq);
                            double factor = (10.0D - dist) / 10.0D * 0.06D;
                            pushX += (dX / dist) * factor;
                            pushZ += (dZ / dist) * factor;
                        }
                    }
                    Vec3 cur = this.getDeltaMovement();
                    this.setDeltaMovement(cur.x + pushX, cur.y, cur.z + pushZ);
                }
            }
        }

        if (this.level().isClientSide && this.random.nextInt(3) == 0) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 3.5D;
            double py = this.getY() + 2.5D + this.random.nextDouble() * 2.0D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 3.5D;
            switch (getVariant()) {
                case VARIANT_FIRE -> this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.03, 0);
                case VARIANT_ICE -> this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, -0.02, 0);
                case VARIANT_FOREST -> this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.03, 0);
                case VARIANT_SHADOW -> this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.03, 0);
                case VARIANT_LIGHTNING -> this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.04, 0);
                case VARIANT_CELESTIAL -> this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.02, 0);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.hasPassenger(player)) {
            return InteractionResult.PASS;
        }

        if (!this.isTame()) {
            if (player.getAbilities().instabuild && stack.isEmpty()) {
                if (!this.level().isClientSide) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.triggerRoar(35);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return InteractionResult.PASS;
        }

        if (this.isOwnedBy(player)) {
            if (!this.isAdult() && isGrowthFood(stack)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                int bonusTicks = getGrowthBonus(stack);
                this.setDragonAge(Math.min(ADULT_AGE_TICKS, this.getDragonAge() + bonusTicks));
                this.heal(25.0F);
                this.playSound(SoundEvents.GENERIC_EAT, 1.2F, 1.2F);
                this.playSound(ModSounds.ANCIENT_DRAGON_AMBIENT.get(), 1.4F, getVoicePitch());
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0D, this.getZ(), 12, 0.6D, 0.6D, 0.6D, 0.05D);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (!this.isSaddled() && stack.is(Items.SADDLE)) {
                if (!this.isAdult()) {
                    if (!this.level().isClientSide) {
                        player.displayClientMessage(Component.translatable("message.awakening_system.dragon_too_young_saddle"), true);
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.setSaddled(true);
                this.playSound(ModSounds.ANCIENT_DRAGON_ROAR.get(), 1.5F, getVoicePitch());
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            int newVariant = getVariantFromItem(stack);
            if (newVariant >= 0 && newVariant != getVariant()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.setVariant(newVariant);
                this.triggerRoar(40);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.getHealth() < this.getMaxHealth() && isHealingFood(stack)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(50.0F);
                this.playSound(ModSounds.ANCIENT_DRAGON_AMBIENT.get(), 1.5F, getVoicePitch());
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.onGround() && player.isShiftKeyDown() && stack.isEmpty()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.isSaddled() && !player.isShiftKeyDown() && stack.isEmpty()) {
                if (!this.isAdult()) {
                    if (!this.level().isClientSide) {
                        player.displayClientMessage(Component.translatable("message.awakening_system.dragon_too_young_ride"), true);
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
                if (!this.level().isClientSide) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    private boolean isGrowthFood(ItemStack stack) {
        return stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    private int getGrowthBonus(ItemStack stack) {
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) return 24000;
        if (stack.is(Items.GOLDEN_APPLE)) return 12000;
        if (stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP)) return 3600;
        return 2400;
    }

    private boolean isHealingFood(ItemStack stack) {
        return stack.is(Items.BEEF)
                || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.GOLDEN_APPLE);
    }

    private int getVariantFromItem(ItemStack stack) {
        if (stack.is(Items.FIRE_CHARGE) || stack.is(Items.BLAZE_POWDER)) return VARIANT_FIRE;
        if (stack.is(Items.PACKED_ICE) || stack.is(Items.BLUE_ICE) || stack.is(Items.SNOW_BLOCK)) return VARIANT_ICE;
        if (stack.is(Items.EMERALD) || stack.is(Items.OAK_SAPLING) || stack.is(Items.VINE)) return VARIANT_FOREST;
        if (stack.is(Items.WITHER_ROSE) || stack.is(Items.AMETHYST_SHARD) || stack.is(Items.ECHO_SHARD)) return VARIANT_SHADOW;
        if (stack.is(Items.LIGHTNING_ROD) || stack.is(Items.COPPER_BLOCK) || stack.is(Items.COPPER_INGOT) || stack.is(Items.WIND_CHARGE)) return VARIANT_LIGHTNING;
        if (stack.is(Items.NETHER_STAR) || stack.is(Items.GOLD_BLOCK) || stack.is(Items.TOTEM_OF_UNDYING) || stack.is(Items.SUNFLOWER)) return VARIANT_CELESTIAL;
        return -1;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.breathCooldown > 0) return;
        this.breathCooldown = 35;
        this.triggerRoar(35);

        float yawRad = (float) Math.toRadians(this.getYRot());
        float pitchRad = (float) Math.toRadians(this.getXRot());

        float scaleFactor = this.getDragonScale() / 4.0F;
        double forwardDist = 15.8D * scaleFactor * Math.cos(pitchRad);
        double heightOffset = (5.2D - 15.8D * Math.sin(pitchRad)) * scaleFactor;

        double spawnX = this.getX() - Math.sin(yawRad) * forwardDist;
        double spawnY = this.getY() + heightOffset;
        double spawnZ = this.getZ() + Math.cos(yawRad) * forwardDist;

        double dx = target.getX() - spawnX;
        double dy = target.getY(0.5D) - spawnY;
        double dz = target.getZ() - spawnZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (!this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double spread = 0.06D;
                double vx = (dx / dist) + (this.random.nextDouble() - 0.5D) * spread;
                double vy = (dy / dist) + (this.random.nextDouble() - 0.5D) * spread;
                double vz = (dz / dist) + (this.random.nextDouble() - 0.5D) * spread;

                AncientDragonBreathEntity breath = new AncientDragonBreathEntity(
                        this.level(), this, spawnX, spawnY, spawnZ, vx * 1.7D, vy * 1.7D, vz * 1.7D, getVariant());
                this.level().addFreshEntity(breath);
            }
        }

        this.playSound(ModSounds.ANCIENT_DRAGON_BREATH.get(), 2.5F, 0.95F);
    }

    public void shootPlayerBreath(Player player) {
        if (this.breathCooldown > 0) return;
        this.breathCooldown = 25;
        this.triggerRoar(25);

        float yawRad = (float) Math.toRadians(this.getYRot());
        float pitchRad = (float) Math.toRadians(this.getXRot());

        float scaleFactor = this.getDragonScale() / 4.0F;
        double forwardDist = 15.8D * scaleFactor * Math.cos(pitchRad);
        double heightOffset = (5.2D - 15.8D * Math.sin(pitchRad)) * scaleFactor;

        double spawnX = this.getX() - Math.sin(yawRad) * forwardDist;
        double spawnY = this.getY() + heightOffset;
        double spawnZ = this.getZ() + Math.cos(yawRad) * forwardDist;

        Vec3 look = player.getLookAngle();
        if (!this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double spread = 0.05D;
                double vx = look.x + (this.random.nextDouble() - 0.5D) * spread;
                double vy = look.y + (this.random.nextDouble() - 0.5D) * spread;
                double vz = look.z + (this.random.nextDouble() - 0.5D) * spread;

                AncientDragonBreathEntity breath = new AncientDragonBreathEntity(
                        this.level(), this, spawnX, spawnY, spawnZ, vx * 1.8D, vy * 1.8D, vz * 1.8D, getVariant());
                this.level().addFreshEntity(breath);
            }
        }

        this.playSound(ModSounds.ANCIENT_DRAGON_BREATH.get(), 2.5F, 0.95F);
    }

    public int getBreathCooldown() {
        return this.breathCooldown;
    }

    public void triggerRoar(int ticks) {
        this.roarTimer = ticks;
        setRoaring(true);
        this.playSound(ModSounds.ANCIENT_DRAGON_ROAR.get(), 3.5F, getVoicePitch());
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
                    this.playSound(ModSounds.ANCIENT_DRAGON_ROAR.get(), 1.5F, 1.2F);
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
                        targetPitch = 20.0F;
                    }
                    this.setXRot(Mth.rotLerp(0.20F, this.getXRot(), targetPitch));

                    double flightSpeed = isSprinting ? 2.50D : 1.40D;
                    double vx = 0.0D;
                    double vz = 0.0D;
                    if (forward != 0.0F || strafe != 0.0F) {
                        vx = (fwdX * forward + sideX * strafe) * flightSpeed;
                        vz = (fwdZ * forward + sideZ * strafe) * flightSpeed;
                    } else {
                        Vec3 cur = this.getDeltaMovement();
                        vx = cur.x * 0.85D;
                        vz = cur.z * 0.85D;
                        if (Math.abs(vx) < 0.005D) vx = 0.0D;
                        if (Math.abs(vz) < 0.005D) vz = 0.0D;
                    }

                    double vy = 0.0D;
                    if (this.clientSpaceHeld) {
                        vy = 0.95D;
                    } else if (this.clientShiftHeld) {
                        vy = -1.20D;
                    } else {
                        vy = 0.0D;
                    }

                    if (this.onGround() && !this.clientSpaceHeld) {
                        this.setFlying(false);
                        vy = 0.0D;
                    }

                    this.setDeltaMovement(vx, vy, vz);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.calculateEntityAnimation(true);
                } else {
                    this.setXRot(Mth.rotLerp(0.25F, this.getXRot(), 0.0F));

                    double walkSpeed = isSprinting ? 0.95D : 0.60D;
                    double vx = 0.0D;
                    double vz = 0.0D;
                    if (forward != 0.0F || strafe != 0.0F) {
                        vx = (fwdX * forward + sideX * strafe) * walkSpeed;
                        vz = (fwdZ * forward + sideZ * strafe) * walkSpeed;
                    } else {
                        Vec3 cur = this.getDeltaMovement();
                        vx = cur.x * 0.60D;
                        vz = cur.z * 0.60D;
                        if (Math.abs(vx) < 0.005D) vx = 0.0D;
                        if (Math.abs(vz) < 0.005D) vz = 0.0D;
                    }

                    Vec3 cur = this.getDeltaMovement();
                    double vy = this.onGround() ? -0.06D : Math.max(-0.85D, cur.y - 0.08D);

                    this.setDeltaMovement(vx, vy, vz);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.calculateEntityAnimation(false);
                }
                return;
            }

            if (this.isFlying()) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.985D));
                this.calculateEntityAnimation(true);
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return this.isVehicle() && this.getFirstPassenger() instanceof Player player ? player.isLocalPlayer() : this.isEffectiveAi();
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean shouldRiderFaceForward(Player player) {
        return false;
    }

    @Override
    public void onPassengerTurned(Entity entityToUpdate) {
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return (first instanceof Player player && this.isOwnedBy(player)) ? player : null;
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            float scaleFactor = this.getDragonScale() / 4.0F;
            float pitchRad = this.getXRot() * ((float) Math.PI / 180.0F);
            float yawRad = this.getYRot() * ((float) Math.PI / 180.0F);

            double baseSeatHeight = 9.90D * scaleFactor;

            double pitchLift = -Math.sin(pitchRad) * 0.85D * scaleFactor;
            double dy = Math.max(9.65D * scaleFactor, baseSeatHeight + pitchLift);

            double forwardDist = (0.25D + Math.cos(pitchRad) * 0.10D) * scaleFactor;

            double dx = -Math.sin(yawRad) * forwardDist;
            double dz = Math.cos(yawRad) * forwardDist;

            moveFunction.accept(passenger, this.getX() + dx, this.getY() + dy, this.getZ() + dz);
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        AncientDragonEntity baby = ModEntities.ANCIENT_DRAGON.get().create(level);
        if (baby != null) {
            baby.setVariant(this.getVariant());
        }
        return baby;
    }

    public static boolean checkDragonSpawnRules(EntityType<AncientDragonEntity> type, ServerLevelAccessor level,
                                                 MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type)
                || level.getBlockState(pos.below()).isSolid();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        BlockPos pos = this.blockPosition();
        var biome = level.getBiome(pos);

        if (biome.is(net.minecraft.tags.BiomeTags.IS_NETHER) || biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS)) {
            setVariant(VARIANT_FIRE);
        } else if (biome.is(net.minecraft.tags.BiomeTags.HAS_CLOSER_WATER_FOG) || biome.is(net.minecraft.tags.BiomeTags.IS_TAIGA)) {
            setVariant(VARIANT_ICE);
        } else if (biome.is(net.minecraft.tags.BiomeTags.IS_JUNGLE) || biome.is(net.minecraft.tags.BiomeTags.IS_FOREST)) {
            setVariant(VARIANT_FOREST);
        } else if (biome.is(net.minecraft.tags.BiomeTags.IS_END) || pos.getY() < 0) {
            setVariant(VARIANT_SHADOW);
        } else if (level.getLevel().isThundering() || biome.is(net.minecraft.tags.BiomeTags.IS_SAVANNA)) {
            setVariant(VARIANT_LIGHTNING);
        } else if (pos.getY() > 180 || biome.is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN)) {
            setVariant(this.random.nextBoolean() ? VARIANT_CELESTIAL : VARIANT_LIGHTNING);
        } else {
            setVariant(this.random.nextInt(6));
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ANCIENT_DRAGON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ANCIENT_DRAGON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ANCIENT_DRAGON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 1.2F, 0.65F);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        this.spawnAtLocation(new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 6 + this.random.nextInt(7)));

        this.spawnAtLocation(new ItemStack(SystemItems.RISING_DRAGON_PILL.get(), 1 + this.random.nextInt(2)));

        this.spawnAtLocation(new ItemStack(SystemItems.AWAKENING_COIN.get(), 20 + this.random.nextInt(26)));

        this.spawnAtLocation(new ItemStack(SystemItems.ROULETTE_TOKEN.get(), 3 + this.random.nextInt(4)));

        if (this.random.nextFloat() < 0.50F) {
            this.spawnAtLocation(new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get(), 1));
        }

        if (this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get(), 1));
        }

        this.spawnAtLocation(new ItemStack(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK, 2 + this.random.nextInt(3)));
        this.spawnAtLocation(new ItemStack(net.minecraft.world.level.block.Blocks.GOLD_BLOCK, 4 + this.random.nextInt(5)));

        switch (getVariant()) {
            case VARIANT_FIRE -> {
                this.spawnAtLocation(new ItemStack(Items.BLAZE_ROD, 6 + this.random.nextInt(7)));
                this.spawnAtLocation(new ItemStack(Items.MAGMA_CREAM, 4 + this.random.nextInt(5)));
                if (this.random.nextFloat() < 0.30F) {
                    this.spawnAtLocation(new ItemStack(SystemItems.ANCIENT_DRAGON_SPAWN_EGG.get(), 1));
                }
            }
            case VARIANT_ICE -> {
                this.spawnAtLocation(new ItemStack(Items.BLUE_ICE, 8 + this.random.nextInt(9)));
                this.spawnAtLocation(new ItemStack(Items.PACKED_ICE, 16 + this.random.nextInt(17)));
                if (this.random.nextFloat() < 0.30F) {
                    this.spawnAtLocation(new ItemStack(SystemItems.ANCIENT_DRAGON_ICE_SPAWN_EGG.get(), 1));
                }
            }
            case VARIANT_FOREST -> {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 12 + this.random.nextInt(13)));
                this.spawnAtLocation(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
                if (this.random.nextFloat() < 0.30F) {
                    this.spawnAtLocation(new ItemStack(SystemItems.ANCIENT_DRAGON_FOREST_SPAWN_EGG.get(), 1));
                }
            }
            case VARIANT_SHADOW -> {
                this.spawnAtLocation(new ItemStack(Items.ECHO_SHARD, 4 + this.random.nextInt(5)));
                this.spawnAtLocation(new ItemStack(Items.NETHERITE_SCRAP, 1 + this.random.nextInt(3)));
                this.spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                if (this.random.nextFloat() < 0.30F) {
                    this.spawnAtLocation(new ItemStack(SystemItems.ANCIENT_DRAGON_SHADOW_SPAWN_EGG.get(), 1));
                }
            }
            case VARIANT_LIGHTNING -> {
                this.spawnAtLocation(new ItemStack(Items.COPPER_BLOCK, 4 + this.random.nextInt(5)));
                this.spawnAtLocation(new ItemStack(Items.LIGHTNING_ROD, 2 + this.random.nextInt(2)));
                this.spawnAtLocation(new ItemStack(Items.WIND_CHARGE, 6 + this.random.nextInt(7)));
                if (this.random.nextFloat() < 0.30F) {
                    this.spawnAtLocation(new ItemStack(SystemItems.ANCIENT_DRAGON_LIGHTNING_SPAWN_EGG.get(), 1));
                }
            }
            case VARIANT_CELESTIAL -> {
                this.spawnAtLocation(new ItemStack(Items.GOLD_BLOCK, 6 + this.random.nextInt(5)));
                this.spawnAtLocation(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                this.spawnAtLocation(new ItemStack(Items.SUNFLOWER, 4));
                if (this.random.nextFloat() < 0.30F) {
                    this.spawnAtLocation(new ItemStack(SystemItems.ANCIENT_DRAGON_CELESTIAL_SPAWN_EGG.get(), 1));
                }
            }
        }

        ExperienceOrb.award(level, this.position(), 1500);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putBoolean("IsFlying", isFlying());
        tag.putBoolean("IsSaddled", isSaddled());
        tag.putInt("DragonAge", getDragonAge());
        if (this.enragedThiefUUID != null) {
            tag.putUUID("EnragedThief", this.enragedThiefUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        setFlying(tag.getBoolean("IsFlying"));
        setSaddled(tag.getBoolean("IsSaddled"));
        if (tag.contains("DragonAge")) {
            setDragonAge(tag.getInt("DragonAge"));
        } else {
            setDragonAge(ADULT_AGE_TICKS);
        }
        if (tag.hasUUID("EnragedThief")) {
            this.enragedThiefUUID = tag.getUUID("EnragedThief");
        }
    }

    /**
     * Despegue autónomo: El dragón en tierra descansa unos momentos y luego bate sus alas
     * con fuerza para impulsarse verticalmente hacia el cielo.
     */
    static class AncientDragonWildTakeoffGoal extends Goal {
        private final AncientDragonEntity dragon;
        private int groundRestTicks;

        public AncientDragonWildTakeoffGoal(AncientDragonEntity dragon) {
            this.dragon = dragon;
            this.groundRestTicks = 120 + dragon.getRandom().nextInt(160);
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.dragon.isTame() || this.dragon.isVehicle() || this.dragon.isFlying()) {
                return false;
            }
            if (this.dragon.getTarget() != null && this.dragon.getTarget().isAlive()) {
                return true;
            }
            return --this.groundRestTicks <= 0;
        }

        @Override
        public void start() {
            this.dragon.setFlying(true);
            this.dragon.setNoGravity(true);
            this.dragon.takeoffImmunityTicks = 40;

            float yawRad = (float) Math.toRadians(this.dragon.getYRot());
            double fwdX = -Math.sin(yawRad) * 0.45D;
            double fwdZ = Math.cos(yawRad) * 0.45D;
            this.dragon.setDeltaMovement(fwdX, 0.85D, fwdZ);

            this.dragon.playSound(ModSounds.ANCIENT_DRAGON_FLAP.get(), 2.5F, 0.85F);
            this.groundRestTicks = 160 + this.dragon.getRandom().nextInt(200);
        }
    }

    /**
     * Vuelo majestuoso salvaje: Vuelo de crucero en amplias órbitas colosales por el bioma,
     * o persecución aérea en picada si hay un enemigo / ladrón.
     */
    static class AncientDragonWildFlightGoal extends Goal {
        private final AncientDragonEntity dragon;
        private double orbitAngle = 0.0D;
        private double orbitRadius = 45.0D;
        private double orbitSpeed = 0.015D;
        private double orbitDirection = 1.0D;
        private double altitudeOffset = 25.0D;
        private BlockPos homeRoostPos = null;

        public AncientDragonWildFlightGoal(AncientDragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.dragon.isTame() && !this.dragon.isVehicle() && this.dragon.isFlying();
        }

        @Override
        public void start() {
            if (this.homeRoostPos == null) {
                this.homeRoostPos = this.dragon.blockPosition();
            }
            double dx = this.dragon.getX() - (this.homeRoostPos.getX() + 0.5D);
            double dz = this.dragon.getZ() - (this.homeRoostPos.getZ() + 0.5D);
            this.orbitAngle = Mth.atan2(dz, dx);
            int id = Math.abs(this.dragon.getId());
            this.orbitDirection = (id % 2 == 0) ? 1.0D : -1.0D;
            this.orbitSpeed = 0.020D + (id % 4) * 0.004D;
            this.orbitRadius = 70.0D + (id % 6) * 12.0D;
            this.altitudeOffset = 20.0D + (id % 5) * 8.0D;
        }

        @Override
        public void tick() {
            LivingEntity target = this.dragon.getTarget();

            if (target != null && target.isAlive()) {
                double tX = target.getX();
                double tY = target.getY() + 7.0D;
                double tZ = target.getZ();

                double dx = tX - this.dragon.getX();
                double dy = tY - this.dragon.getY();
                double dz = tZ - this.dragon.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                Vec3 currentMotion = this.dragon.getDeltaMovement();
                Vec3 dir = dist > 0.001D ? new Vec3(dx / dist, dy / dist, dz / dist) : Vec3.ZERO;
                double chaseSpeed = Math.min(1.85D, 0.80D + dist * 0.025D);
                Vec3 desired = dir.scale(chaseSpeed);
                Vec3 blended = currentMotion.lerp(desired, 0.12D);
                if (blended.length() > 2.10D) {
                    blended = blended.normalize().scale(2.10D);
                }
                this.dragon.setDeltaMovement(blended);

                if (dist < 32.0D && this.dragon.getBreathCooldown() <= 0) {
                    this.dragon.performRangedAttack(target, 1.0F);
                }
            } else {
                if (this.homeRoostPos == null) {
                    this.homeRoostPos = this.dragon.blockPosition();
                }

                this.orbitAngle += this.orbitSpeed * this.orbitDirection;
                double centerX = this.homeRoostPos.getX() + 0.5D;
                double centerZ = this.homeRoostPos.getZ() + 0.5D;

                double baseY = Math.max(125.0D, this.homeRoostPos.getY() + this.altitudeOffset);
                double waveY = Math.sin(this.orbitAngle * 1.5D) * 5.0D;
                double targetY = Math.min(this.dragon.level().getMaxBuildHeight() - 20, baseY + waveY);

                double targetX = centerX + Math.cos(this.orbitAngle) * this.orbitRadius;
                double targetZ = centerZ + Math.sin(this.orbitAngle) * this.orbitRadius;

                double dx = targetX - this.dragon.getX();
                double dy = targetY - this.dragon.getY();
                double dz = targetZ - this.dragon.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                Vec3 currentMotion = this.dragon.getDeltaMovement();
                Vec3 dir = dist > 0.001D ? new Vec3(dx / dist, dy / dist, dz / dist) : Vec3.ZERO;
                double cruiseSpeed = 1.25D;
                Vec3 desired = dir.scale(cruiseSpeed);
                Vec3 blended = currentMotion.lerp(desired, 0.08D);
                if (blended.length() > 1.45D) {
                    blended = blended.normalize().scale(1.45D);
                }
                this.dragon.setDeltaMovement(blended);
            }

            Vec3 vel = this.dragon.getDeltaMovement();
            if (vel.horizontalDistanceSqr() > 0.005D) {
                float targetYaw = (float) (Mth.atan2(vel.z, vel.x) * (180.0D / Math.PI)) - 90.0F;
                this.dragon.setYRot(Mth.rotLerp(0.12F, this.dragon.getYRot(), targetYaw));
                this.dragon.yBodyRot = this.dragon.getYRot();
                this.dragon.yHeadRot = this.dragon.getYRot();

                float targetPitch = (float) (-(Mth.atan2(vel.y, vel.horizontalDistance()) * (180.0D / Math.PI)));
                this.dragon.setXRot(Mth.rotLerp(0.12F, this.dragon.getXRot(), targetPitch));
            }
        }
    }

    /**
     * Aterrizaje controlado: Tras volar por un tiempo suficiente, busca posarse
     * sobre un risco, montaña o saliente de roca firme.
     */
    static class AncientDragonWildLandGoal extends Goal {
        private final AncientDragonEntity dragon;
        private BlockPos landingSpot = null;
        private int flightTicks = 0;
        private int landingTimeout = 0;

        public AncientDragonWildLandGoal(AncientDragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.dragon.isTame() || this.dragon.isVehicle() || !this.dragon.isFlying()) {
                this.flightTicks = 0;
                this.landingTimeout = 0;
                return false;
            }
            if (this.dragon.getTarget() != null) {
                this.flightTicks = 0;
                this.landingTimeout = 0;
                return false;
            }
            if (++this.flightTicks < 600) {
                return false;
            }
            if (this.landingSpot == null || !this.dragon.level().getBlockState(this.landingSpot.below()).isSolid()) {
                this.landingSpot = findLandingPos();
            }
            return this.landingSpot != null;
        }

        private BlockPos findLandingPos() {
            BlockPos current = this.dragon.blockPosition();
            Level level = this.dragon.level();
            for (int r = 0; r < 8; r++) {
                int ox = this.dragon.getRandom().nextInt(45) - 22;
                int oz = this.dragon.getRandom().nextInt(45) - 22;
                BlockPos check = current.offset(ox, 0, oz);
                int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, check.getX(), check.getZ());
                BlockPos candidate = new BlockPos(check.getX(), topY, check.getZ());
                if (topY > level.getMinBuildHeight() + 5 && level.getBlockState(candidate.below()).isSolid()) {
                    if (!level.getFluidState(candidate.below()).isSource() && !level.getFluidState(candidate).isSource()) {
                        boolean hasOtherDragonNearby = !level.getEntitiesOfClass(
                                AncientDragonEntity.class,
                                new AABB(candidate).inflate(14.0D),
                                other -> other != this.dragon && other.isAlive()
                        ).isEmpty();
                        if (!hasOtherDragonNearby) {
                            return candidate;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void tick() {
            if (this.landingSpot == null) return;

            if (++this.landingTimeout > 160) {
                this.landingSpot = null;
                this.flightTicks = 0;
                this.landingTimeout = 0;
                return;
            }

            double dx = (this.landingSpot.getX() + 0.5D) - this.dragon.getX();
            double dy = this.landingSpot.getY() - this.dragon.getY();
            double dz = (this.landingSpot.getZ() + 0.5D) - this.dragon.getZ();
            double distH = Math.sqrt(dx * dx + dz * dz);

            Vec3 vel = this.dragon.getDeltaMovement();
            double targetVy = Math.max(-0.35D, Math.min(-0.15D, dy * 0.12D));
            double targetVx = (distH > 0.8D) ? (dx / distH) * Math.min(0.28D, distH * 0.05D) : 0.0D;
            double targetVz = (distH > 0.8D) ? (dz / distH) * Math.min(0.28D, distH * 0.05D) : 0.0D;

            this.dragon.setDeltaMovement(vel.x * 0.75D + targetVx * 0.25D, targetVy, vel.z * 0.75D + targetVz * 0.25D);

            if (this.dragon.onGround()) {
                this.dragon.setFlying(false);
                this.dragon.setNoGravity(false);
                this.dragon.setDeltaMovement(0.0D, 0.0D, 0.0D);
                this.landingSpot = null;
                this.flightTicks = 0;
                this.landingTimeout = 0;
            }
        }
    }

    /**
     * Paseo en tierra firme: Permite al dragón caminar lentamente entre los nidos.
     */
    static class AncientDragonWildWalkGoal extends WaterAvoidingRandomStrollGoal {
        private final AncientDragonEntity dragon;

        public AncientDragonWildWalkGoal(AncientDragonEntity dragon, double speed) {
            super(dragon, speed);
            this.dragon = dragon;
        }

        @Override
        public boolean canUse() {
            return !this.dragon.isTame() && !this.dragon.isVehicle() && !this.dragon.isFlying() && this.dragon.onGround() && super.canUse();
        }
    }
}
