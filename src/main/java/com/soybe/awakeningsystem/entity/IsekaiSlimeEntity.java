package com.soybe.awakeningsystem.entity;

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
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Slime de Fantasía Isekai:
 * Criatura elástica y redondeada con silueta de gota de agua/lágrima anime.
 * Se desplaza mediante rebotes y saltos elásticos rítmicos en lugar de deslizarse.
 * Posee 5 variantes de colores coleccionables con habilidades, gustos alimentarios y recompensas únicas.
 * Incluye variante Gigante domesticable estilo Slime Rancher y montura elástica estilo Terraria.
 */
public class IsekaiSlimeEntity extends Monster implements PlayerRideableJumping, OwnableEntity {

    public enum Variant {
        AZURE(0, "azure"),
        SAKURA(1, "sakura"),
        EMERALD(2, "emerald"),
        GOLD(3, "gold"),
        PURPLE(4, "purple"),
        RUBY(5, "ruby"),
        AMBER(6, "amber"),
        MINT(7, "mint"),
        FROST(8, "frost"),
        OBSIDIAN(9, "obsidian");

        private final int id;
        private final String name;

        Variant(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static Variant byId(int id) {
            for (Variant v : values()) {
                if (v.id == id) return v;
            }
            return AZURE;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(IsekaiSlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_BIG =
            SynchedEntityData.defineId(IsekaiSlimeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_TAMED =
            SynchedEntityData.defineId(IsekaiSlimeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(IsekaiSlimeEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public float targetSquish;
    public float squish;
    public float prevSquish;
    private boolean wasOnGround;
    private int impactTicks;

    private float playerJumpPendingScale = 0.0F;
    private int hopCooldown = 0;
    private int pacifyTicks = 0;
    private int airborneTicks = 0;

    public IsekaiSlimeEntity(EntityType<? extends IsekaiSlimeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
        this.moveControl = new IsekaiSlimeMoveControl(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_IS_BIG, false);
        builder.define(DATA_TAMED, false);
        builder.define(DATA_OWNER_UUID, Optional.empty());
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, variant.getId());
    }

    public boolean isBig() {
        return this.entityData.get(DATA_IS_BIG);
    }

    public void setBig(boolean big) {
        this.entityData.set(DATA_IS_BIG, big);
        this.refreshDimensions();
        if (big) {
            this.xpReward = 15;
            var maxHpAttr = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHpAttr != null && maxHpAttr.getBaseValue() < 60.0D) {
                maxHpAttr.setBaseValue(60.0D);
                this.setHealth(60.0F);
            }
            var atkAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atkAttr != null) {
                atkAttr.setBaseValue(7.0D);
            }
            var stepAttr = this.getAttribute(Attributes.STEP_HEIGHT);
            if (stepAttr != null) {
                stepAttr.setBaseValue(1.25D);
            }
        } else {
            var stepAttr = this.getAttribute(Attributes.STEP_HEIGHT);
            if (stepAttr != null) {
                stepAttr.setBaseValue(1.0D);
            }
        }
    }

    @Override
    public Component getName() {
        if (this.hasCustomName()) {
            return super.getName();
        }
        if (this.isBig()) {
            return Component.translatable("entity.awakening_system.giant_isekai_slime");
        }
        return super.getName();
    }

    public boolean isTame() {
        return this.entityData.get(DATA_TAMED);
    }

    public void setTame(boolean tame) {
        this.entityData.set(DATA_TAMED, tame);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        return uuid == null ? null : this.level().getPlayerByUUID(uuid);
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity != null && entity.getUUID().equals(this.getOwnerUUID());
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return isBig() ? EntityDimensions.scalable(1.65F, 1.85F) : super.getDefaultDimensions(pose);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new IsekaiSlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new IsekaiSlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                e -> !this.isTame() && this.pacifyTicks <= 0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                e -> this.isTame() && e != this && !(e instanceof IsekaiSlimeEntity is && is.isTame())));
    }

    /**
     * Sistema de Comida Favorita estilo Slime Rancher:
     * Cada color tiene su fruta/vegetal predilecto.
     */
    public boolean isFavoriteFood(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return switch (getVariant()) {
            case AZURE -> stack.is(Items.MELON_SLICE) || stack.is(Items.GLOW_BERRIES);
            case SAKURA -> stack.is(Items.SWEET_BERRIES) || stack.is(Items.APPLE);
            case EMERALD -> stack.is(Items.CARROT) || stack.is(Items.POTATO);
            case GOLD -> stack.is(Items.GOLDEN_CARROT) || stack.is(Items.GOLDEN_APPLE);
            case PURPLE -> stack.is(Items.CHORUS_FRUIT) || stack.is(Items.BEETROOT);
            case RUBY -> stack.is(Items.APPLE) || stack.is(Items.NETHER_WART);
            case AMBER -> stack.is(Items.HONEY_BOTTLE) || stack.is(Items.HONEYCOMB);
            case MINT -> stack.is(Items.KELP) || stack.is(Items.DRIED_KELP);
            case FROST -> stack.is(Items.SNOWBALL) || stack.is(Items.SWEET_BERRIES);
            case OBSIDIAN -> stack.is(Items.ENDER_PEARL) || stack.is(Items.FERMENTED_SPIDER_EYE);
        };
    }

    public Component getFavoriteFoodName() {
        return switch (getVariant()) {
            case AZURE -> Component.translatable("item.minecraft.melon_slice");
            case SAKURA -> Component.translatable("item.minecraft.sweet_berries");
            case EMERALD -> Component.translatable("item.minecraft.carrot");
            case GOLD -> Component.translatable("item.minecraft.golden_carrot");
            case PURPLE -> Component.translatable("item.minecraft.chorus_fruit");
            case RUBY -> Component.translatable("item.minecraft.apple");
            case AMBER -> Component.translatable("item.minecraft.honey_bottle");
            case MINT -> Component.translatable("item.minecraft.kelp");
            case FROST -> Component.translatable("item.minecraft.snowball");
            case OBSIDIAN -> Component.translatable("item.minecraft.ender_pearl");
        };
    }

    public Component getVariantName() {
        return switch (getVariant()) {
            case AZURE -> Component.translatable("awakening_system.slime.color.azure");
            case SAKURA -> Component.translatable("awakening_system.slime.color.sakura");
            case EMERALD -> Component.translatable("awakening_system.slime.color.emerald");
            case GOLD -> Component.translatable("awakening_system.slime.color.gold");
            case PURPLE -> Component.translatable("awakening_system.slime.color.purple");
            case RUBY -> Component.translatable("awakening_system.slime.color.ruby");
            case AMBER -> Component.translatable("awakening_system.slime.color.amber");
            case MINT -> Component.translatable("awakening_system.slime.color.mint");
            case FROST -> Component.translatable("awakening_system.slime.color.frost");
            case OBSIDIAN -> Component.translatable("awakening_system.slime.color.obsidian");
        };
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 1. Slime Salvaje
        if (!this.isTame()) {
            if (isFavoriteFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    boolean wasHoney = itemstack.is(Items.HONEY_BOTTLE);
                    itemstack.shrink(1);
                    if (wasHoney) {
                        player.addItem(new ItemStack(Items.GLASS_BOTTLE));
                    }
                }
                this.playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.2F);
                this.playSound(SoundEvents.SLIME_SQUISH, 0.7F, 1.1F);
                this.targetSquish = -0.6F;

                if (!this.level().isClientSide) {
                    ServerLevel sl = (ServerLevel) this.level();
                    if (!this.isBig()) {
                        // Slime pequeño que come su manjar preferido: ¡crece a Slime Gigante!
                        this.setBig(true);
                        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.8D, this.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.05D);
                        sl.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.8D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
                        this.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.4F);
                        player.displayClientMessage(Component.translatable("awakening_system.slime.grew"), true);
                    } else {
                        // Slime Gigante salvaje: probabilidad de domesticar (35% o instantáneo para oro mítico)
                        boolean instantGold = (getVariant() == Variant.GOLD && (itemstack.is(Items.GOLDEN_APPLE) || itemstack.is(Items.GOLDEN_CARROT)));
                        boolean success = instantGold || this.random.nextFloat() < 0.35F;

                        if (success) {
                            this.setTame(true);
                            this.setOwnerUUID(player.getUUID());
                            this.setTarget(null);
                            sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.3D, this.getZ(), 10, 0.5D, 0.4D, 0.5D, 0.05D);
                            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 1.6F);
                            player.displayClientMessage(Component.translatable("awakening_system.slime.tamed", getVariantName()), true);
                        } else {
                            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0D, this.getZ(), 6, 0.4D, 0.3D, 0.4D, 0.02D);
                            this.pacifyTicks = 80; // Saciado por 4 segundos
                            this.setTarget(null);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else if (itemstack.has(net.minecraft.core.component.DataComponents.FOOD)) {
                // Comida no predilecta
                if (!this.level().isClientSide) {
                    ServerLevel sl = (ServerLevel) this.level();
                    sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.02D);
                    player.displayClientMessage(Component.translatable("awakening_system.slime.dislike", getVariantName(), getFavoriteFoodName()), true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }

        // 2. Slime Domesticado
        if (this.isOwnedBy(player)) {
            // Curar con comida favorita
            if (isFavoriteFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.heal(18.0F);
                this.playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.2F);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.3D, this.getZ(), 5, 0.4D, 0.3D, 0.4D, 0.02D);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Si es pequeño y domesticado: alimentarlo puede hacerlo crecer a Gigante
            if (!this.isBig() && isFavoriteFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.setBig(true);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.8D, this.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.05D);
                    sl.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.8D, this.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.1D);
                }
                this.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.4F);
                player.displayClientMessage(Component.translatable("awakening_system.slime.grew"), true);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Montar en Slime Gigante con mano vacía o no-comida (¡Sin silla de montar necesaria!)
            if (this.isBig() && !this.isVehicle() && !player.isShiftKeyDown()) {
                if (!this.level().isClientSide) {
                    player.startRiding(this);
                    player.displayClientMessage(Component.translatable("awakening_system.slime.mount_hint"), true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof Player player && this.isTame() && this.isOwnedBy(player) ? player : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        double yOff = isBig() ? 1.45D : 0.65D;
        return new Vec3(0.0D, yOff * (double) scale, 0.0D);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            double yOff = isBig() ? 1.45D : 0.65D;
            // Acompaña el squash & stretch con amortiguación para evitar cualquier temblor de cámara
            float clampedSquish = Mth.clamp(this.squish, -0.6F, 0.6F);
            yOff += (double) (clampedSquish * 0.12F);
            moveFunction.accept(passenger, this.getX(), this.getY() + yOff, this.getZ());
        }
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        if (jumpPower < 0) jumpPower = 0;
        this.playerJumpPendingScale = jumpPower >= 80 ? 1.0F : 0.60F + 0.40F * ((float) jumpPower / 80.0F);
    }

    @Override
    public boolean canJump() {
        return this.isTame() && (this.onGround() || this.isInWater());
    }

    @Override
    public void handleStartJump(int jumpPower) {}

    @Override
    public void handleStopJump() {}

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            LivingEntity passenger = this.getControllingPassenger();
            if (this.isVehicle() && passenger instanceof Player player) {
                // Sincronizar rotación con la mirada del jugador
                this.setRot(player.getYRot(), player.getXRot() * 0.5F);
                this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();

                // Súper Salto cargado con barra espaciadora
                if (this.playerJumpPendingScale > 0.0F) {
                    double jumpPower = 1.30D + 0.65D * (double) this.playerJumpPendingScale;
                    boolean hasDirection = (player.zza != 0.0F || player.xxa != 0.0F);
                    double fwdSpeed = (hasDirection ? (player.isSprinting() ? 1.10D : 0.88D) : 0.40D) * (double) this.playerJumpPendingScale;
                    float yawRad = player.getYRot() * ((float) Math.PI / 180.0F);
                    Vec3 cur = this.getDeltaMovement();
                    this.setDeltaMovement(
                            cur.x - (double) (Mth.sin(yawRad) * fwdSpeed),
                            jumpPower,
                            cur.z + (double) (Mth.cos(yawRad) * fwdSpeed)
                    );
                    this.hasImpulse = true;
                    this.targetSquish = 1.8F;
                    this.playSound(SoundEvents.SLIME_JUMP, 1.2F, 1.4F);
                    this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 0.9F, 1.25F);
                    if (this.level() instanceof ServerLevel sl) {
                        for (int i = 0; i < 16; i++) {
                            float angle = (float) (i * Math.PI * 2.0 / 16);
                            double px = this.getX() + Math.cos(angle) * 1.3D;
                            double pz = this.getZ() + Math.sin(angle) * 1.3D;
                            sl.sendParticles(ParticleTypes.CLOUD, px, this.getY() + 0.1D, pz, 1, 0, 0.06D, 0, 0.02D);
                            sl.sendParticles(ParticleTypes.ITEM_SLIME, px, this.getY() + 0.2D, pz, 2, 0, 0.12D, 0, 0.06D);
                        }
                    }
                    this.playerJumpPendingScale = 0.0F;
                    super.travel(this.getDeltaMovement());
                    return;
                }

                // COMPORTAMIENTO ACUÁTICO: Flotación tranquila en la superficie y deslizamiento suave (SIN saltar)
                if (this.isInWater() || this.isInLava()) {
                    float forward = player.zza;
                    float strafe = player.xxa;
                    if (forward != 0.0F || strafe != 0.0F) {
                        float moveYaw = player.getYRot();
                        if (forward < 0.0F) moveYaw += 180.0F;
                        if (strafe > 0.0F) moveYaw -= 90.0F * (forward > 0 ? 0.5F : (forward < 0 ? -0.5F : 1.0F));
                        if (strafe < 0.0F) moveYaw += 90.0F * (forward > 0 ? 0.5F : (forward < 0 ? -0.5F : 1.0F));

                        boolean sprinting = player.isSprinting();
                        float rad = moveYaw * ((float) Math.PI / 180.0F);
                        double glideSpeed = sprinting ? 0.68D : 0.46D; // Deslizador acuático suave

                        double vy = this.isUnderWater() ? 0.12D : 0.0D;
                        this.setDeltaMovement(
                                -Math.sin(rad) * glideSpeed,
                                vy,
                                Math.cos(rad) * glideSpeed
                        );
                        this.hasImpulse = true;

                        // Efectos y sonido de deslizamiento sobre agua
                        if (this.level() instanceof ServerLevel sl && this.tickCount % 2 == 0) {
                            sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.1D, this.getZ(), 5, 0.35D, 0.05D, 0.35D, 0.08D);
                            sl.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.1D, this.getZ(), 2, 0.2D, 0.05D, 0.2D, 0.02D);
                        }
                        if (this.tickCount % 8 == 0) {
                            this.playSound(SoundEvents.BOAT_PADDLE_WATER, 0.6F, 1.1F);
                        }
                    } else {
                        // Flotando quieto en el agua sin oscilar ni brincar
                        double vy = this.isUnderWater() ? 0.12D : 0.0D;
                        Vec3 cur = this.getDeltaMovement();
                        this.setDeltaMovement(cur.x * 0.72D, vy, cur.z * 0.72D);
                    }
                    super.travel(this.getDeltaMovement());
                    return;
                }

                // COMPORTAMIENTO EN TIERRA FIRME: Saltos rítmicos elásticos largos estilo Terraria
                if (this.onGround()) {
                    float forward = player.zza;
                    float strafe = player.xxa;
                    if (forward != 0.0F || strafe != 0.0F) {
                        if (this.hopCooldown-- <= 0) {
                            this.hopCooldown = 3;
                            this.targetSquish = 1.2F;
                            this.impactTicks = 0;

                            float moveYaw = player.getYRot();
                            if (forward < 0.0F) moveYaw += 180.0F;
                            if (strafe > 0.0F) moveYaw -= 90.0F * (forward > 0 ? 0.5F : (forward < 0 ? -0.5F : 1.0F));
                            if (strafe < 0.0F) moveYaw += 90.0F * (forward > 0 ? 0.5F : (forward < 0 ? -0.5F : 1.0F));

                            boolean sprinting = player.isSprinting();
                            float rad = moveYaw * ((float) Math.PI / 180.0F);
                            double hopSpeed = sprinting ? 0.96D : 0.76D;
                            double hopY = sprinting ? 0.54D : 0.48D;
                            this.setDeltaMovement(
                                    -Math.sin(rad) * hopSpeed,
                                    hopY,
                                    Math.cos(rad) * hopSpeed
                            );
                            this.hasImpulse = true;
                            this.playSound(SoundEvents.SLIME_JUMP, 0.8F, sprinting ? 1.25F : 1.1F);
                        }
                    } else {
                        this.hopCooldown = 0;
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.72D, 1.0D, 0.72D));
                    }
                } else {
                    // En el aire: mantener aceleración horizontal para que los saltos cubran largas distancias
                    float forward = player.zza;
                    float strafe = player.xxa;
                    if (forward != 0.0F || strafe != 0.0F) {
                        float moveYaw = player.getYRot();
                        if (forward < 0.0F) moveYaw += 180.0F;
                        if (strafe > 0.0F) moveYaw -= 90.0F * (forward > 0 ? 0.5F : (forward < 0 ? -0.5F : 1.0F));
                        if (strafe < 0.0F) moveYaw += 90.0F * (forward > 0 ? 0.5F : (forward < 0 ? -0.5F : 1.0F));
                        float rad = moveYaw * ((float) Math.PI / 180.0F);
                        double drift = 0.04D;
                        Vec3 cur = this.getDeltaMovement();
                        this.setDeltaMovement(
                                cur.x - Math.sin(rad) * drift,
                                cur.y,
                                cur.z + Math.cos(rad) * drift
                        );
                    }
                }

                super.travel(this.getDeltaMovement());
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Los slimes son 100% elásticos e inmunes al daño por caída en todos los tamaños y estados
        if (fallDistance > 1.5F) {
            this.targetSquish = -1.4F;
            this.playSound(SoundEvents.SLIME_SQUISH, this.isBig() ? 0.85F : 0.65F, 0.8F);
            this.spawnBounceParticles();
        }
        return false;
    }

    @Override
    protected int calculateFallDamage(float distance, float damageMultiplier) {
        return 0;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    public void hop(boolean aggressive) {
        this.targetSquish = 1.0F;
        this.impactTicks = 0;
        float yawRad = this.getYRot() * ((float) Math.PI / 180.0F);
        float forwardSpeed = aggressive ? (isBig() ? 0.44F : 0.38F) : (isBig() ? 0.26F : 0.22F);
        float jumpPower = aggressive ? (isBig() ? 0.48F : 0.44F) : (isBig() ? 0.42F : 0.38F);

        Vec3 delta = this.getDeltaMovement();
        this.setDeltaMovement(
                delta.x - (double) (Mth.sin(yawRad) * forwardSpeed),
                (double) jumpPower,
                delta.z + (double) (Mth.cos(yawRad) * forwardSpeed)
        );
        this.hasImpulse = true;

        this.playSound(SoundEvents.SLIME_JUMP, isBig() ? 0.85F : 0.55F,
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * (isBig() ? 0.75F : 0.9F));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();

        // 30% de probabilidad de spawnear como Slime Gigante
        if (random.nextInt(100) < 30) {
            this.setBig(true);
        }

        int roll = random.nextInt(100);
        Variant chosen;
        if (roll < 18) {
            chosen = Variant.AZURE;
        } else if (roll < 34) {
            chosen = Variant.SAKURA;
        } else if (roll < 50) {
            chosen = Variant.EMERALD;
        } else if (roll < 62) {
            chosen = Variant.MINT;
        } else if (roll < 74) {
            chosen = Variant.AMBER;
        } else if (roll < 84) {
            chosen = Variant.FROST;
        } else if (roll < 92) {
            chosen = Variant.RUBY;
        } else if (roll < 97) {
            chosen = Variant.PURPLE;
        } else if (roll < 99) {
            chosen = Variant.OBSIDIAN;
        } else {
            chosen = Variant.GOLD; // Mítico 1%
        }
        setVariant(chosen);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant().getId());
        tag.putBoolean("IsBig", this.isBig());
        tag.putBoolean("Tamed", this.isTame());
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.setVariant(Variant.byId(tag.getInt("Variant")));
        }
        if (tag.contains("IsBig")) {
            this.setBig(tag.getBoolean("IsBig"));
        }
        if (tag.contains("Tamed")) {
            this.setTame(tag.getBoolean("Tamed"));
        }
        if (tag.hasUUID("Owner")) {
            this.setOwnerUUID(tag.getUUID("Owner"));
        }
    }

    @Override
    public void tick() {
        this.prevSquish = this.squish;

        super.tick();

        boolean onGround = this.onGround();
        // Al impactar contra el suelo tras un salto:
        // airborneTicks >= 3 evita temblor/flickering en esquinas y bordes de bloques
        if (onGround) {
            if (this.airborneTicks >= 3 && this.getDeltaMovement().y <= 0.05D) {
                this.targetSquish = isBig() ? -1.45F : -1.25F;
                this.impactTicks = 14;
                this.playSound(SoundEvents.SLIME_SQUISH, isBig() ? 0.9F : 0.65F,
                        ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * (isBig() ? 0.8F : 0.95F));
                this.spawnBounceParticles();
            }
            this.airborneTicks = 0;
        } else {
            this.airborneTicks++;
            if (this.wasOnGround && this.getDeltaMovement().y > 0.1D) {
                this.targetSquish = 1.0F;
                this.impactTicks = 0;
            } else if (this.getDeltaMovement().y > 0.05D) {
                this.targetSquish = 0.85F;
            } else if (this.getDeltaMovement().y < -0.05D) {
                this.targetSquish = 0.1F;
            }
        }

        // Rebotes elásticos fluidos tras el impacto
        if (this.impactTicks > 0) {
            this.impactTicks--;
            if (this.impactTicks == 10) {
                this.targetSquish = 0.5F;
            } else if (this.impactTicks == 6) {
                this.targetSquish = -0.25F;
            } else if (this.impactTicks == 2) {
                this.targetSquish = 0.05F;
            }
        }

        this.wasOnGround = onGround;
        this.squish += (this.targetSquish - this.squish) * 0.45F;
        this.targetSquish *= 0.8F;

        if (onGround && !this.isVehicle()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.72D, 1.0D, 0.72D));
        }

        // Efectos pasivos de partículas según el color del slime
        if (this.level().isClientSide && this.random.nextInt(6) == 0) {
            double spread = isBig() ? 1.2D : 0.6D;
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * spread;
            double py = this.getY() + 0.3D + this.random.nextDouble() * (isBig() ? 1.2D : 0.5D);
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * spread;
            switch (getVariant()) {
                case AZURE -> this.level().addParticle(ParticleTypes.FALLING_WATER, px, py, pz, 0, 0, 0);
                case SAKURA -> this.level().addParticle(ParticleTypes.CHERRY_LEAVES, px, py, pz, 0, -0.02D, 0);
                case EMERALD -> this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.02D, 0);
                case GOLD -> this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.02D, 0);
                case PURPLE -> this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.01D, 0);
                case RUBY -> this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.01D, 0);
                case AMBER -> this.level().addParticle(ParticleTypes.FALLING_HONEY, px, py, pz, 0, 0, 0);
                case MINT -> this.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0.02D, 0);
                case FROST -> this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0, -0.01D, 0);
                case OBSIDIAN -> this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02D, 0);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Estabilización de flotabilidad acuática natural (sin brincos espasmódicos)
        if (this.isInWater() || this.isInLava()) {
            if (this.isUnderWater()) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.08D, this.getDeltaMovement().z);
            } else if (this.getDeltaMovement().y < 0.0D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
            }
        }

        if (this.pacifyTicks > 0) {
            this.pacifyTicks--;
            if (this.getTarget() instanceof Player) {
                this.setTarget(null);
            }
        }

        // Pisotón / Rebote de enemigos estilo Terraria Slime Mount (Goomba Stomp)
        if (this.isVehicle() && this.getDeltaMovement().y < -0.10D) {
            AABB stompBox = this.getBoundingBox().inflate(0.3D, 0.2D, 0.3D).move(0, -0.4D, 0);
            List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, stompBox,
                    e -> e != this && !this.hasPassenger(e) && e.isAlive() && (e instanceof Monster || !this.isOwnedBy(e)));
            for (LivingEntity enemy : enemies) {
                if (enemy.hurt(this.damageSources().mobAttack(this), 14.0F)) {
                    // Rebotar hacia arriba de nuevo!
                    Vec3 v = this.getDeltaMovement();
                    this.setDeltaMovement(v.x, 0.68D, v.z);
                    this.hasImpulse = true;
                    this.targetSquish = 1.3F;
                    this.playSound(SoundEvents.SLIME_ATTACK, 1.2F, 0.8F);
                    this.playSound(SoundEvents.ANVIL_LAND, 0.35F, 1.8F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT, enemy.getX(), enemy.getY() + 0.5D, enemy.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.15D);
                        sl.sendParticles(ParticleTypes.ITEM_SLIME, enemy.getX(), enemy.getY() + 0.5D, enemy.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.1D);
                    }
                    break;
                }
            }
        }
    }

    private void spawnBounceParticles() {
        if (this.level().isClientSide) {
            int count = isBig() ? 16 : 8;
            for (int i = 0; i < count; i++) {
                float angle = this.random.nextFloat() * ((float) Math.PI * 2F);
                float dist = this.random.nextFloat() * (isBig() ? 1.0F : 0.5F) + 0.2F;
                double px = this.getX() + Mth.cos(angle) * dist;
                double pz = this.getZ() + Mth.sin(angle) * dist;
                this.level().addParticle(ParticleTypes.ITEM_SLIME, px, this.getY(), pz, 0, 0.12D, 0);
            }
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int mult = isBig() ? 2 : 1;
        this.spawnAtLocation(new ItemStack(Items.SLIME_BALL, (1 + this.random.nextInt(3)) * mult));

        switch (getVariant()) {
            case AZURE -> {
                if (this.random.nextBoolean() || isBig()) this.spawnAtLocation(new ItemStack(Items.PRISMARINE_CRYSTALS, mult));
            }
            case SAKURA -> {
                this.spawnAtLocation(new ItemStack(Items.CHERRY_LEAVES, (1 + this.random.nextInt(2)) * mult));
                if (this.random.nextInt(3) == 0 || isBig()) this.spawnAtLocation(new ItemStack(Items.SWEET_BERRIES, 2 * mult));
            }
            case EMERALD -> {
                if (this.random.nextBoolean() || isBig()) this.spawnAtLocation(new ItemStack(Items.EMERALD, mult));
            }
            case GOLD -> {
                this.spawnAtLocation(new ItemStack(Items.GOLD_NUGGET, (3 + this.random.nextInt(6)) * mult));
                this.spawnAtLocation(com.soybe.awakeningsystem.SystemItems.coin(150 * mult));
            }
            case PURPLE -> {
                this.spawnAtLocation(new ItemStack(Items.AMETHYST_SHARD, (1 + this.random.nextInt(2)) * mult));
            }
            case RUBY -> {
                if (this.random.nextBoolean() || isBig()) this.spawnAtLocation(new ItemStack(Items.NETHER_WART, (1 + this.random.nextInt(2)) * mult));
                if (isBig()) this.spawnAtLocation(new ItemStack(Items.MAGMA_CREAM, mult));
            }
            case AMBER -> {
                if (this.random.nextBoolean() || isBig()) this.spawnAtLocation(new ItemStack(Items.HONEYCOMB, mult));
            }
            case MINT -> {
                this.spawnAtLocation(new ItemStack(Items.SLIME_BALL, (2 + this.random.nextInt(3)) * mult));
            }
            case FROST -> {
                this.spawnAtLocation(new ItemStack(Items.SNOWBALL, (2 + this.random.nextInt(4)) * mult));
                if (isBig()) this.spawnAtLocation(new ItemStack(Items.PACKED_ICE, mult));
            }
            case OBSIDIAN -> {
                if (this.random.nextInt(3) == 0 || isBig()) this.spawnAtLocation(new ItemStack(Items.ENDER_PEARL, mult));
                if (isBig()) this.spawnAtLocation(new ItemStack(Items.OBSIDIAN, mult));
            }
        }
    }

    public static class IsekaiSlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final IsekaiSlimeEntity slime;
        private boolean isAggressive;

        public IsekaiSlimeMoveControl(IsekaiSlimeEntity slime) {
            super(slime);
            this.slime = slime;
            this.yRot = 180.0F * slime.getYRot() / (float) Math.PI;
            this.jumpDelay = 10 + slime.getRandom().nextInt(15);
        }

        public void setDirection(float yRot, boolean aggressive) {
            this.yRot = yRot;
            this.isAggressive = aggressive;
        }

        public void setWantedMovement(double speed) {
            this.speedModifier = speed;
            this.operation = Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            // Cuando tiene un jinete montado, las físicas las comanda travel()
            if (this.slime.isVehicle()) {
                return;
            }

            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();

            if (this.operation != Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
                return;
            }

            if (this.slime.isInWater() || this.slime.isInLava()) {
                double vy = this.slime.isUnderWater() ? 0.08D : 0.0D;
                if (this.operation == Operation.MOVE_TO) {
                    float rad = this.yRot * ((float) Math.PI / 180.0F);
                    double spd = this.isAggressive ? 0.32D : 0.20D;
                    this.slime.setDeltaMovement(-Math.sin(rad) * spd, vy, Math.cos(rad) * spd);
                } else {
                    Vec3 v = this.slime.getDeltaMovement();
                    this.slime.setDeltaMovement(v.x * 0.75D, vy, v.z * 0.75D);
                }
                this.operation = Operation.WAIT;
                return;
            }

            this.operation = Operation.WAIT;
            if (this.mob.onGround()) {
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.isAggressive ? (5 + this.slime.getRandom().nextInt(6)) : (12 + this.slime.getRandom().nextInt(16));
                    this.slime.hop(this.isAggressive);
                } else {
                    this.slime.setDeltaMovement(this.slime.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }
            }
        }
    }

    static class IsekaiSlimeAttackGoal extends Goal {
        private final IsekaiSlimeEntity slime;
        private int growTiredTicks;
        private int attackCooldown = 0;

        public IsekaiSlimeAttackGoal(IsekaiSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.slime.isVehicle()) return false;
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (this.slime.isTame() && this.slime.isOwnedBy(target)) return false;
            return !(target instanceof Player player && player.isSpectator());
        }

        @Override
        public void start() {
            this.growTiredTicks = 300;
            this.attackCooldown = 0;
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.slime.isVehicle()) return false;
            LivingEntity target = this.slime.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (this.slime.isTame() && this.slime.isOwnedBy(target)) return false;
            if (target instanceof Player player && (player.isSpectator() || player.isCreative())) return false;
            return --this.growTiredTicks > 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target == null) return;
            this.slime.lookAt(target, 15.0F, 15.0F);

            if (this.slime.getMoveControl() instanceof IsekaiSlimeMoveControl moveCtrl) {
                moveCtrl.setDirection(this.slime.getYRot(), true);
                moveCtrl.setWantedMovement(1.25D);
            }

            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            double distSq = this.slime.distanceToSqr(target);
            double reach = this.slime.isBig() ? 4.5D : 3.0D;
            if (this.attackCooldown <= 0 && distSq < reach && this.slime.hasLineOfSight(target)) {
                if (this.slime.doHurtTarget(target)) {
                    this.slime.playSound(SoundEvents.SLIME_ATTACK, 1.0F,
                            ((this.slime.getRandom().nextFloat() - this.slime.getRandom().nextFloat()) * 0.2F + 1.0F));
                    this.attackCooldown = 20;
                }
            }
        }
    }

    static class IsekaiSlimeRandomDirectionGoal extends Goal {
        private final IsekaiSlimeEntity slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public IsekaiSlimeRandomDirectionGoal(IsekaiSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.slime.isVehicle()) return false;
            return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava());
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = 30 + this.slime.getRandom().nextInt(50);
                this.chosenDegrees = (float) this.slime.getRandom().nextInt(360);
            }
            if (this.slime.getMoveControl() instanceof IsekaiSlimeMoveControl moveCtrl) {
                moveCtrl.setDirection(this.chosenDegrees, false);
                moveCtrl.setWantedMovement(1.0D);
            }
        }
    }
}
