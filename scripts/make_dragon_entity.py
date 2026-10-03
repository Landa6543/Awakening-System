content = """package com.soybe.awakeningsystem.entity;

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
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Dragón Ancestral Occidental (True Hexapod Dragon).
 * - Anatomía de 6 extremidades: 4 patas independientes + 2 alas gigantes articuladas.
 * - Corona de cuernos curvados hacia atrás, espinas en mejillas y cuello en S.
 * - Placas ventrales segmentadas y cola terminada en punta de lanza / flecha.
 * - 4 Variantes Elementales: FUEGO (0), HIELO (1), BOSQUE (2), SOMBRA (3).
 * - Vuelo 3D libre, domesticación, montura y aliento elemental dracónico.
 */
public class AncientDragonEntity extends TamableAnimal implements FlyingAnimal, RangedAttackMob, PlayerRideableJumping {

    public static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_SADDLED =
            SynchedEntityData.defineId(AncientDragonEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int VARIANT_FIRE = 0;
    public static final int VARIANT_ICE = 1;
    public static final int VARIANT_FOREST = 2;
    public static final int VARIANT_SHADOW = 3;

    private int roarTimer = 0;
    private int breathCooldown = 0;
    private float flapTime = 0.0F;
    private float flapTimeO = 0.0F;
    private boolean clientSpaceHeld = false;

    private final FlyingPathNavigation flyingNavigation;
    private final GroundPathNavigation groundNavigation;
    private final FlyingMoveControl flyingMoveControl;
    private final MoveControl groundMoveControl;

    public AncientDragonEntity(EntityType<? extends AncientDragonEntity> type, Level level) {
        super(type, level);
        this.flyingNavigation = new FlyingPathNavigation(this, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
        this.flyingMoveControl = new FlyingMoveControl(this, 18, true);
        this.groundMoveControl = new MoveControl(this);
        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNavigation;
        this.setMaxUpStep(1.5F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FLYING_SPEED, 0.50D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, VARIANT_FIRE);
        builder.define(DATA_FLYING, false);
        builder.define(DATA_ROARING, false);
        builder.define(DATA_SADDLED, false);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, 3));
    }

    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
        if (flying) {
            this.moveControl = this.flyingMoveControl;
            this.navigation = this.flyingNavigation;
        } else {
            this.moveControl = this.groundMoveControl;
            this.navigation = this.groundNavigation;
        }
    }

    public boolean isRoaring() {
        return this.entityData.get(DATA_ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(DATA_ROARING, roaring);
    }

    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(DATA_SADDLED, saddled);
    }

    public float getFlapTime(float partialTick) {
        return Mth.lerp(partialTick, this.flapTimeO, this.flapTime);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.25D, 40, 60, 24.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.35D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.4D, 12.0F, 3.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.flapTimeO = this.flapTime;
        if (this.isFlying() || !this.onGround()) {
            this.flapTime += 0.22F;
            if (this.level().isClientSide && this.tickCount % 20 == 0) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL, 1.0F, 0.9F, false);
            }
        } else {
            this.flapTime += 0.04F;
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
            if (this.isVehicle() && this.getFirstPassenger() instanceof Player) {
                this.setNoGravity(this.isFlying());
            } else {
                if (this.getTarget() != null) {
                    double dist = this.distanceToSqr(this.getTarget());
                    if (dist > 64.0D && !this.isFlying()) {
                        setFlying(true);
                    } else if (dist <= 16.0D && this.onGround()) {
                        setFlying(false);
                    }
                } else if (!this.onGround() && !this.isInWater()) {
                    setFlying(true);
                } else if (this.onGround() && this.isFlying() && !this.isVehicle()) {
                    setFlying(false);
                }
                this.setNoGravity(this.isFlying());
            }
        }

        // Partículas místicas de elemento continuo
        if (this.level().isClientSide && this.random.nextInt(3) == 0) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 2.0D;
            double py = this.getY() + 1.2D + this.random.nextDouble() * 1.5D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 2.0D;
            switch (getVariant()) {
                case VARIANT_FIRE -> this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.02, 0);
                case VARIANT_ICE -> this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, -0.02, 0);
                case VARIANT_FOREST -> this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.02, 0);
                case VARIANT_SHADOW -> this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.02, 0);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. Domesticación
        if (!this.isTame()) {
            if (isTamingFood(stack)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.level().broadcastEntityEvent(this, (byte) 7); // Corazones
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // Humo
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        } else if (this.isOwnedBy(player)) {
            // 2. Colocar Montura
            if (!this.isSaddled() && stack.is(Items.SADDLE)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.setSaddled(true);
                this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // 3. Cambiar Variante con Catalizador
            int newVariant = getVariantFromItem(stack);
            if (newVariant >= 0 && newVariant != getVariant()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.setVariant(newVariant);
                this.playSound(SoundEvents.PLAYER_LEVELUP, 1.2F, 0.8F);
                this.triggerRoar(40);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // 4. Curación
            if (this.getHealth() < this.getMaxHealth() && isHealingFood(stack)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(30.0F);
                this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // 5. Sentarse / Pararse (Shift + Clic derecho)
            if (player.isShiftKeyDown() && stack.isEmpty()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // 6. Montar al dragón (Clic derecho con montura colocada)
            if (this.isSaddled() && !player.isShiftKeyDown() && stack.isEmpty()) {
                if (!this.level().isClientSide) {
                    player.setYRot(this.getYRot());
                    player.setXRot(this.getXRot());
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    private boolean isTamingFood(ItemStack stack) {
        return stack.is(Items.NETHERITE_INGOT)
                || stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.DIAMOND)
                || stack.is(SystemItems.CELESTIAL_DRAGON_PEARL.get());
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
        return -1;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.breathCooldown > 0) return;
        this.breathCooldown = 35;
        this.triggerRoar(35);

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - (this.getEyeY() - 0.3D);
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (!this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                double spread = 0.08D;
                double vx = (dx / dist) + (this.random.nextDouble() - 0.5D) * spread;
                double vy = (dy / dist) + (this.random.nextDouble() - 0.5D) * spread;
                double vz = (dz / dist) + (this.random.nextDouble() - 0.5D) * spread;

                AncientDragonBreathEntity breath = new AncientDragonBreathEntity(
                        this.level(), this, vx * 1.5D, vy * 1.5D, vz * 1.5D, getVariant());
                this.level().addFreshEntity(breath);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.4F, 0.85F);
    }

    public void triggerRoar(int ticks) {
        this.roarTimer = ticks;
        setRoaring(true);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.getFirstPassenger() instanceof Player rider) {
                this.setYRot(rider.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(rider.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;

                float forward = rider.zza;
                float strafe = rider.xxa * 0.5F;

                if (forward <= 0.0F) {
                    forward *= 0.25F;
                }

                // Control de vuelo / salto
                if (this.clientSpaceHeld) {
                    this.setFlying(true);
                    this.clientSpaceHeld = false;
                }

                if (this.isFlying()) {
                    Vec3 look = rider.getLookAngle();
                    double speed = (float) this.getAttributeValue(Attributes.FLYING_SPEED) * 1.35D;
                    double vy = 0.0D;

                    if (rider.jumping) {
                        vy = 0.45D;
                    } else if (rider.isShiftKeyDown()) {
                        vy = -0.45D;
                    } else if (forward > 0) {
                        vy = look.y * speed;
                    }

                    Vec3 move = new Vec3(look.x * speed * forward, vy, look.z * speed * forward);
                    this.setDeltaMovement(move);
                    this.move(MoverType.SELF, this.getDeltaMovement());

                    if (this.onGround() && !rider.jumping) {
                        this.setFlying(false);
                    }
                    return;
                } else {
                    super.travel(new Vec3(strafe, travelVector.y, forward * (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    return;
                }
            }
        }
        super.travel(travelVector);
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
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_WALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return (first instanceof Player player && this.isOwnedBy(player)) ? player : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        // Asiento montado en la cruz del dragón (detrás del cuello, sobre el pecho)
        return new Vec3(0.0D, dimensions.height() * 0.72D, -0.4D);
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        this.clientSpaceHeld = true;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        this.clientSpaceHeld = true;
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        AncientDragonEntity baby = ModEntities.ANCIENT_DRAGON.get().create(level);
        if (baby != null) {
            baby.setVariant(this.getVariant());
        }
        return baby;
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
        } else {
            setVariant(this.random.nextInt(4));
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 1.0F, 0.75F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putBoolean("IsFlying", isFlying());
        tag.putBoolean("IsSaddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        setFlying(tag.getBoolean("IsFlying"));
        setSaddled(tag.getBoolean("IsSaddled"));
    }
}
"""

with open("src/main/java/com/soybe/awakeningsystem/entity/AncientDragonEntity.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Created AncientDragonEntity.java")
