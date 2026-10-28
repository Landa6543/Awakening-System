package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.DungeonManager;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.PagodaTrialManager;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.TowerManager;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.entity.PartEntity;
import net.minecraft.world.entity.Mob;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Dragón Celestial Ancestral (Variante Boss Titánica):
 * - Longitud colosal de más de 100 bloques (64 vértebras interconectadas).
 * - Cuerpo de escala robusta y presencia monumental.
 * - Ruptura destructiva de bloques estilo Ender Dragon a su paso.
 * - Salud titánica de 1500 HP y ataques físicos y de Qi ancestral.
 */
public class AncientCelestialDragonBossEntity extends Monster implements FlyingAnimal, RangedAttackMob, DragonPartParent {

    private static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(AncientCelestialDragonBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BREATHING =
            SynchedEntityData.defineId(AncientCelestialDragonBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_DIVING =
            SynchedEntityData.defineId(AncientCelestialDragonBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FALLEN =
            SynchedEntityData.defineId(AncientCelestialDragonBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(
            Component.translatable("awakening_system.boss.ancient_celestial_dragon.name"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_20
    )).setDarkenScreen(true).setPlayBossMusic(true);

    public static final int SEGMENT_COUNT = 64;
    public final float[] segmentYaw = new float[SEGMENT_COUNT];
    public final float[] segmentYawO = new float[SEGMENT_COUNT];
    public final float[] segmentPitch = new float[SEGMENT_COUNT];
    public final float[] segmentPitchO = new float[SEGMENT_COUNT];
    private boolean segmentsInitialized = false;

    public final DragonBodyPart[] subParts;
    public static final int[] PART_SEGMENT_INDICES = new int[] {
        3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59, 63
    };
    public final Vec3[] segmentPositions = new Vec3[SEGMENT_COUNT];

    private BlockPos homeSanctuaryPos = null;
    private int roarAnimationTicks = 0;
    private int thunderStormCooldown = 160;
    private int blockBreakCooldown = 0;
    private int tailSweepCooldown = 120;
    private int descentTicks = 160;
    private boolean descentCompleted = false;
    private boolean groundImpactDone = false;

    public AncientCelestialDragonBossEntity(EntityType<? extends AncientCelestialDragonBossEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.noPhysics = false;

        this.subParts = new DragonBodyPart[16];
        this.subParts[0] = new DragonBodyPart(this, "neck_front", 3.0F, 3.0F);
        this.subParts[1] = new DragonBodyPart(this, "neck_mid", 3.2F, 3.2F);
        this.subParts[2] = new DragonBodyPart(this, "neck_rear", 3.4F, 3.4F);
        this.subParts[3] = new DragonBodyPart(this, "chest_front", 3.6F, 3.6F);
        this.subParts[4] = new DragonBodyPart(this, "chest_mid", 3.8F, 3.8F);
        this.subParts[5] = new DragonBodyPart(this, "body_front", 3.8F, 3.8F);
        this.subParts[6] = new DragonBodyPart(this, "body_mid_1", 3.8F, 3.8F);
        this.subParts[7] = new DragonBodyPart(this, "body_mid_2", 3.6F, 3.6F);
        this.subParts[8] = new DragonBodyPart(this, "body_rear", 3.6F, 3.6F);
        this.subParts[9] = new DragonBodyPart(this, "waist", 3.4F, 3.4F);
        this.subParts[10] = new DragonBodyPart(this, "pelvis", 3.2F, 3.2F);
        this.subParts[11] = new DragonBodyPart(this, "tail_base", 3.0F, 3.0F);
        this.subParts[12] = new DragonBodyPart(this, "tail_upper", 2.8F, 2.8F);
        this.subParts[13] = new DragonBodyPart(this, "tail_mid", 2.6F, 2.6F);
        this.subParts[14] = new DragonBodyPart(this, "tail_lower", 2.4F, 2.4F);
        this.subParts[15] = new DragonBodyPart(this, "tail_tip", 2.2F, 2.2F);

        for (int i = 0; i < SEGMENT_COUNT; i++) {
            this.segmentPositions[i] = Vec3.ZERO;
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subParts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subParts.length; i++) {
            this.subParts[i].setId(id + i + 1);
        }
    }

    public boolean hurtFromPart(DragonBodyPart part, DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;

        float multiplier = 1.0F;
        if (part.partName.contains("chest")) {
            multiplier = 1.25F;
            this.level().playSound(null, part.getX(), part.getY(), part.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.5F, 0.9F);
        } else if (part.partName.contains("tail")) {
            multiplier = 0.85F;
        }

        boolean damaged = this.hurt(source, amount * multiplier);
        if (damaged && !this.level().isClientSide) {
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.CRIT, part.getX(), part.getY() + 1.0D, part.getZ(), 12, 0.8D, 0.8D, 0.8D, 0.1D);
        }
        return damaged;
    }

    public AABB getRenderBoundingBox() {
        return this.getBoundingBox().inflate(180.0D, 140.0D, 180.0D);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        for (DragonBodyPart part : this.subParts) {
            part.discard();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8000.0D)
                .add(Attributes.FLYING_SPEED, 0.55D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 45.0D)
                .add(Attributes.ARMOR, 28.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROARING, false);
        builder.define(DATA_BREATHING, false);
        builder.define(DATA_DIVING, false);
        builder.define(DATA_FALLEN, false);
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
        this.goalSelector.addGoal(0, new BossReturnToAltarGoal(this));
        this.goalSelector.addGoal(1, new BossDiveAttackGoal(this));
        this.goalSelector.addGoal(2, new BossCelestialBreathGoal(this));
        this.goalSelector.addGoal(3, new BossCirclingFlightGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public void setHomeSanctuaryPos(BlockPos pos) {
        this.homeSanctuaryPos = pos;
    }

    public boolean isRoaring() {
        return this.entityData.get(DATA_ROARING);
    }

    public boolean isBreathing() {
        return this.entityData.get(DATA_BREATHING);
    }

    public boolean isDiving() {
        return this.entityData.get(DATA_DIVING);
    }

    public boolean isFallen() {
        return this.entityData.get(DATA_FALLEN);
    }

    public void triggerDescentRoar() {
        this.entityData.set(DATA_DIVING, true);
        this.descentTicks = 160;
        this.descentCompleted = false;
        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 5.0F, 0.65F);
    }

    public void triggerSingleRoar() {
        this.entityData.set(DATA_ROARING, true);
        this.roarAnimationTicks = 60;
        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 5.0F, 0.65F);
    }

    private void triggerFallenState() {
        if (this.isFallen()) return;
        this.entityData.set(DATA_FALLEN, true);
        this.entityData.set(DATA_DIVING, false);
        this.entityData.set(DATA_BREATHING, false);
        this.descentTicks = 0;
        this.descentCompleted = true;
        this.setNoGravity(false);
        this.setHealth(10.0F);

        if (this.homeSanctuaryPos != null) {
            double ax = this.homeSanctuaryPos.getX() + 0.5D;
            double az = this.homeSanctuaryPos.getZ() + 0.5D;
            double hDx = ax - this.getX();
            double hDz = az - this.getZ();
            this.setDeltaMovement(hDx * 0.15D, -0.65D, hDz * 0.15D);
        } else {
            this.setDeltaMovement(0.0D, -0.65D, 0.0D);
        }

        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 4.5F, 0.55F);
        this.bossEvent.setName(Component.translatable("awakening_system.boss.ancient_celestial_dragon.defeated_state"));
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
        this.bossEvent.setProgress(0.01F);

        this.setTarget(null);
    }

    public float getSegmentYaw(int index, float partialTick) {
        if (!this.segmentsInitialized || index < 0 || index >= SEGMENT_COUNT) return this.getYRot();
        return Mth.rotLerp(partialTick, this.segmentYawO[index], this.segmentYaw[index]);
    }

    public float getSegmentPitch(int index, float partialTick) {
        if (!this.segmentsInitialized || index < 0 || index >= SEGMENT_COUNT) return this.getXRot();
        return Mth.lerp(partialTick, this.segmentPitchO[index], this.segmentPitch[index]);
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

        if (!this.segmentsInitialized) {
            float initPitch = (this.descentTicks > 0) ? 85.0F : this.getXRot();
            float initYaw = this.getYRot();
            for (int i = 0; i < SEGMENT_COUNT; i++) {
                this.segmentYaw[i] = initYaw;
                this.segmentYawO[i] = initYaw;
                this.segmentPitch[i] = initPitch;
                this.segmentPitchO[i] = initPitch;
                this.segmentPositions[i] = new Vec3(this.getX(), this.getY() + (i * 1.65D), this.getZ());
            }
            this.segmentsInitialized = true;
        }

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

        this.segmentPositions[0] = this.position();
        double segDist = 1.65D;
        for (int i = 1; i < SEGMENT_COUNT; i++) {
            float yawRad = this.segmentYaw[i] * ((float) Math.PI / 180.0F);
            float pitchRad = this.segmentPitch[i] * ((float) Math.PI / 180.0F);
            double dx = Math.sin(yawRad) * Math.cos(pitchRad) * segDist;
            double dy = Math.sin(pitchRad) * segDist;
            double dz = -Math.cos(yawRad) * Math.cos(pitchRad) * segDist;
            this.segmentPositions[i] = this.segmentPositions[i - 1].add(dx, dy, dz);
        }

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

        if (!this.level().isClientSide) {
            this.blockBreakCooldown--;
            if (this.blockBreakCooldown <= 0) {
                checkBlockDestruction();
                this.blockBreakCooldown = 2;
            }

            if (!this.isFallen()) {
                this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            }

            if (this.descentTicks > 0) {
                this.descentTicks--;
                this.entityData.set(DATA_DIVING, true);
                this.setXRot(85.0F);
                this.xRotO = 85.0F;

                if (this.homeSanctuaryPos != null) {
                    double targetY = this.homeSanctuaryPos.getY() + 18.0D;
                    if (this.getY() > targetY) {
                        double hDx = (this.homeSanctuaryPos.getX() + 0.5D) - this.getX();
                        double hDz = (this.homeSanctuaryPos.getZ() + 0.5D) - this.getZ();
                        this.setDeltaMovement(hDx * 0.08D, -0.80D, hDz * 0.08D);
                    } else {
                        this.setXRot(0.0F);
                        this.xRotO = 0.0F;
                        this.descentTicks = 0;
                        this.descentCompleted = true;
                        this.entityData.set(DATA_DIVING, false);
                        this.triggerSingleRoar();
                        this.setDeltaMovement(0.0D, 0.20D, 0.45D);
                    }
                }
            } else if (!this.descentCompleted && this.entityData.get(DATA_DIVING)) {
                this.entityData.set(DATA_DIVING, false);
                this.descentCompleted = true;
            }

            if (this.isFallen()) {
                this.setTarget(null);
                if (!this.onGround()) {
                    this.setDeltaMovement(this.getDeltaMovement().x * 0.8D, Math.max(-0.85D, this.getDeltaMovement().y - 0.08D), this.getDeltaMovement().z * 0.8D);
                    this.setXRot(Mth.rotLerp(0.1F, this.getXRot(), 25.0F));
                    this.hasImpulse = true;
                } else {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.setXRot(Mth.rotLerp(0.2F, this.getXRot(), 0.0F));
                    this.bossEvent.setProgress(0.01F);

                    if (!this.groundImpactDone) {
                        this.groundImpactDone = true;
                        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 3.5F, 0.6F);
                        if (this.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 8, 3.0D, 0.5D, 3.0D, 0.0D);
                            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 50, 4.0D, 1.0D, 4.0D, 0.05D);
                        }
                    }
                }
            }

            if (!this.isFallen() && this.getTarget() != null && this.getTarget().isAlive()) {
                this.thunderStormCooldown--;
                if (this.thunderStormCooldown <= 0) {
                    triggerThunderStorm(this.getTarget());
                    this.thunderStormCooldown = 180 + this.random.nextInt(100);
                }

                this.tailSweepCooldown--;
                if (this.tailSweepCooldown <= 0 && this.distanceTo(this.getTarget()) < 24.0D) {
                    triggerTailSweep();
                    this.tailSweepCooldown = 140 + this.random.nextInt(80);
                }
            }
        } else {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 4.5D;
            double py = this.getY() + this.random.nextDouble() * 3.5D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 4.5D;

            this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.08D, 0.0D);
            if (this.isRoaring() || this.isDiving()) {
                this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, (this.random.nextDouble() - 0.5D) * 0.1D, 0.05D, (this.random.nextDouble() - 0.5D) * 0.1D);
            }
            if (this.isBreathing()) {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, 0.0D, 0.12D, 0.0D);
            }
        }

        if (this.roarAnimationTicks > 0) {
            this.roarAnimationTicks--;
            if (this.roarAnimationTicks == 0) {
                this.entityData.set(DATA_ROARING, false);
            }
        }
    }

    /**
     * Mecánica de Ruptura de Bloques Estilo Ender Dragon:
     * Destruye bloques sólidos en su trayectoria de vuelo y embestida.
     * Respeta bloques inmunes como Bedrock, Barreras, Obsidiana, y el Altar Sagrado.
     */
    private void checkBlockDestruction() {
        if (this.isFallen()) return;
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        if (this.level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                || this.level().dimension().equals(DungeonManager.DUNGEON_REALM)
                || this.level().dimension().equals(TowerManager.RANKER_TOWER)
                || this.getTags().contains("no_block_destroy")
                || this.getTags().contains("shadow_soldier")) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(1.2D, 0.8D, 1.2D);
        int minX = Mth.floor(box.minX);
        int minY = Math.max(this.level().getMinBuildHeight(), Mth.floor(box.minY));
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Math.min(this.level().getMaxBuildHeight(), Mth.floor(box.maxY));
        int maxZ = Mth.floor(box.maxZ);

        boolean brokeAny = false;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mpos.set(x, y, z);
                    BlockState state = this.level().getBlockState(mpos);
                    if (!state.isAir() && !state.liquid()) {
                        if (canDestroyBlock(state, mpos)) {
                            this.level().destroyBlock(mpos, false, this);
                            brokeAny = true;
                        }
                    }
                }
            }
        }

        if (brokeAny && this.random.nextFloat() < 0.25F) {
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.6F, 0.65F);
        }
    }

    private boolean canDestroyBlock(BlockState state, BlockPos pos) {
        if (this.level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                || this.level().dimension().equals(DungeonManager.DUNGEON_REALM)
                || this.level().dimension().equals(TowerManager.RANKER_TOWER)
                || this.getTags().contains("no_block_destroy")
                || this.getTags().contains("shadow_soldier")
                || (this.hasCustomName() && this.getCustomName().getString().contains("[SOMBRA]"))) {
            return false;
        }
        if (state.is(BlockTags.DRAGON_IMMUNE) || state.is(BlockTags.WITHER_IMMUNE)) return false;
        if (SystemBlocks.CELESTIAL_DRAGON_ALTAR.isBound() && state.is(SystemBlocks.CELESTIAL_DRAGON_ALTAR.get())) return false;
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER) || state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.REINFORCED_DEEPSLATE)) return false;
        if (state.getDestroySpeed(this.level(), pos) < 0.0F) return false;
        return true;
    }

    private void triggerThunderStorm(LivingEntity target) {
        this.entityData.set(DATA_ROARING, true);
        this.roarAnimationTicks = 45;
        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 4.0F, 0.8F);

        if (this.level() instanceof ServerLevel serverLevel) {
            boolean suppressFire = this.level().dimension().equals(PagodaTrialManager.PAGODA_REALM)
                    || this.level().dimension().equals(DungeonManager.DUNGEON_REALM)
                    || this.level().dimension().equals(TowerManager.RANKER_TOWER)
                    || this.getTags().contains("no_block_destroy")
                    || this.getTags().contains("shadow_soldier");

            for (int i = 0; i < 6; i++) {
                double rx = target.getX() + (this.random.nextDouble() - 0.5D) * 14.0D;
                double rz = target.getZ() + (this.random.nextDouble() - 0.5D) * 14.0D;
                BlockPos strikePos = BlockPos.containing(rx, target.getY(), rz);
                net.minecraft.world.entity.LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                    bolt.setVisualOnly(suppressFire);
                    serverLevel.addFreshEntity(bolt);
                }
            }
            if (suppressFire) {
                target.hurt(this.damageSources().lightningBolt(), 14.0F);
            }
        }
    }

    /**
     * Coletazo Sísmico (Tail Sweep):
     * Barre en un círculo masivo de 16 bloques, arrojando por los aires a todos los enemigos cercanos.
     */
    private void triggerTailSweep() {
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 3.0F, 0.6F);
        AABB sweepBox = this.getBoundingBox().inflate(14.0D, 4.0D, 14.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, sweepBox, e -> e != this && e.isAlive());

        for (LivingEntity e : targets) {
            double dx = e.getX() - this.getX();
            double dz = e.getZ() - this.getZ();
            double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            e.hurt(this.damageSources().mobAttack(this), 28.0F);
            e.setDeltaMovement(dx / dist * 1.8D, 0.85D, dz / dist * 1.8D);
            e.hasImpulse = true;
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.5D, this.getZ(), 8, 4.0D, 0.5D, 4.0D, 0.0D);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.entityData.set(DATA_BREATHING, true);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - (this.getY() + 2.5D);
        double dz = target.getZ() - this.getZ();

        for (int i = 0; i < 2; i++) {
            double spreadX = (this.random.nextDouble() - 0.5D) * 0.04D;
            double spreadZ = (this.random.nextDouble() - 0.5D) * 0.04D;
            CelestialDragonBreathEntity breath = new CelestialDragonBreathEntity(
                    this.level(), this, (dx * 0.075D) + spreadX, dy * 0.075D, (dz * 0.075D) + spreadZ);
            this.level().addFreshEntity(breath);
        }
        this.playSound(ModSounds.CELESTIAL_DRAGON_BREATH.get(), 2.8F, 0.85F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isDeadOrDying() || this.isRemoved()) return false;
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) ||
            source.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) {
            return false;
        }

        if (this.isFallen()) {
            if (this.onGround()) {
                boolean damaged = super.hurt(source, amount);
                if (this.getHealth() <= 0.0F || !this.isAlive()) {
                    this.die(source);
                }
                return damaged;
            } else {
                return false;
            }
        }

        if (this.getHealth() - amount <= 30.0F) {
            triggerFallenState();
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.dead) return;
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();
        this.bossEvent.setVisible(false);
        this.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 5.0F, 0.55F);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 2.0D, this.getZ(), 20, 1.5D, 1.5D, 1.5D, 0.0D);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 5, 2.0D, 1.0D, 2.0D, 0.0D);
            sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(), 100, 3.0D, 2.0D, 3.0D, 0.15D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5D, this.getZ(), 80, 2.5D, 2.0D, 2.5D, 0.25D);
        }
        for (DragonBodyPart part : this.subParts) {
            if (part != null) part.discard();
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, this.getX() + (this.random.nextDouble() - 0.5D) * 4.0D,
                    this.getY() + this.random.nextDouble() * 2.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D,
                    4, 0.2D, 0.4D, 0.2D, 0.05D);
            sl.sendParticles(ParticleTypes.CLOUD, this.getX() + (this.random.nextDouble() - 0.5D) * 3.0D,
                    this.getY() + 0.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 3.0D,
                    3, 0.1D, 0.2D, 0.1D, 0.02D);
        }
        if (this.deathTime >= 20 && !this.level().isClientSide()) {
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        this.spawnAtLocation(new ItemStack(SystemItems.IMMORTAL_DRAGON_SCALE.get(), 18 + this.random.nextInt(10)));
        this.spawnAtLocation(new ItemStack(SystemItems.CELESTIAL_DRAGON_PEARL.get(), 2 + this.random.nextInt(2)));
        this.spawnAtLocation(new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get(), 3));
        this.spawnAtLocation(new ItemStack(Blocks.GOLD_BLOCK, 8));
        this.spawnAtLocation(new ItemStack(Blocks.DIAMOND_BLOCK, 4));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean isFlying() {
        return !this.isFallen() && !this.onGround();
    }

    static class BossDiveAttackGoal extends Goal {
        private final AncientCelestialDragonBossEntity dragon;
        private int diveCooldown = 200;
        private int diveTicks = 0;

        public BossDiveAttackGoal(AncientCelestialDragonBossEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.dragon.isFallen() || this.dragon.descentTicks > 0 || this.dragon.getHealth() <= 100.0F) return false;
            LivingEntity target = this.dragon.getTarget();
            return target != null && target.isAlive() && this.dragon.distanceTo(target) > 16.0D && --this.diveCooldown <= 0;
        }

        @Override
        public void start() {
            this.diveTicks = 50;
            this.diveCooldown = 220 + this.dragon.random.nextInt(100);
            this.dragon.entityData.set(DATA_DIVING, true);
            this.dragon.playSound(ModSounds.CELESTIAL_DRAGON_ROAR.get(), 4.5F, 0.75F);
        }

        @Override
        public void tick() {
            LivingEntity target = this.dragon.getTarget();
            if (target == null) return;

            this.diveTicks--;
            Vec3 dir = target.position().subtract(this.dragon.position()).normalize().scale(0.85D);
            this.dragon.setDeltaMovement(dir);

            if (this.dragon.distanceTo(target) < 6.0D) {
                target.hurt(this.dragon.damageSources().mobAttack(this.dragon), 38.0F);
                this.dragon.checkBlockDestruction();
                this.stop();
            }
        }

        @Override
        public void stop() {
            this.dragon.entityData.set(DATA_DIVING, false);
        }
    }

    static class BossCelestialBreathGoal extends Goal {
        private final AncientCelestialDragonBossEntity dragon;
        private int attackCooldown = 40;

        public BossCelestialBreathGoal(AncientCelestialDragonBossEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.dragon.isFallen() || this.dragon.descentTicks > 0 || this.dragon.getHealth() <= 100.0F) return false;
            LivingEntity target = this.dragon.getTarget();
            return target != null && target.isAlive() && this.dragon.distanceTo(target) < 64.0D;
        }

        @Override
        public void tick() {
            LivingEntity target = this.dragon.getTarget();
            if (target == null) return;

            this.dragon.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.attackCooldown--;

            if (this.attackCooldown <= 0) {
                this.dragon.performRangedAttack(target, 1.0F);
                this.attackCooldown = 50 + this.dragon.random.nextInt(30);
            }
        }
    }

    /**
     * Retorno al Altar al tener Baja Vida (<= 100 HP, similar a la dragona del End):
     * Cuando el dragón celestial desciende a 100 HP o menos, suspende ataques lejanos
     * y regresa directamente al santuario sobrevolando el altar sagrado a baja altura.
     * Al bajar a <= 30 HP, se desploma directo al centro del altar (triggerFallenState)
     * para que nunca caiga al vacío, barrancos o entre árboles.
     */
    static class BossReturnToAltarGoal extends Goal {
        private final AncientCelestialDragonBossEntity dragon;
        private double orbitAngle = 0.0D;
        private final double orbitRadius = 8.5D;

        public BossReturnToAltarGoal(AncientCelestialDragonBossEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.dragon.isFallen() && this.dragon.descentTicks <= 0 && this.dragon.getHealth() <= 100.0F;
        }

        @Override
        public void start() {
            BlockPos center = this.dragon.homeSanctuaryPos != null ? this.dragon.homeSanctuaryPos : this.dragon.blockPosition();
            double dx = this.dragon.getX() - (center.getX() + 0.5D);
            double dz = this.dragon.getZ() - (center.getZ() + 0.5D);
            this.orbitAngle = Mth.atan2(dz, dx);
            this.dragon.setTarget(null);
        }

        @Override
        public void tick() {
            BlockPos center = this.dragon.homeSanctuaryPos != null ? this.dragon.homeSanctuaryPos : this.dragon.blockPosition();
            double centerX = center.getX() + 0.5D;
            double centerZ = center.getZ() + 0.5D;
            double targetY = center.getY() + 8.5D;

            double distH = Math.sqrt((centerX - this.dragon.getX()) * (centerX - this.dragon.getX()) + 
                                     (centerZ - this.dragon.getZ()) * (centerZ - this.dragon.getZ()));

            double targetX;
            double targetZ;

            if (distH > 14.0D) {
                targetX = centerX;
                targetZ = centerZ;
            } else {
                this.orbitAngle += 0.045D;
                targetX = centerX + Math.cos(this.orbitAngle) * this.orbitRadius;
                targetZ = centerZ + Math.sin(this.orbitAngle) * this.orbitRadius;
            }

            double dx = targetX - this.dragon.getX();
            double dy = targetY - this.dragon.getY();
            double dz = targetZ - this.dragon.getZ();

            Vec3 currentMotion = this.dragon.getDeltaMovement();
            double blend = 0.06D;
            Vec3 desiredMotion = new Vec3(dx * blend, dy * 0.06D, dz * blend);
            this.dragon.setDeltaMovement(currentMotion.scale(0.85D).add(desiredMotion));

            Vec3 vel = this.dragon.getDeltaMovement();
            if (vel.horizontalDistanceSqr() > 0.005D) {
                float targetYaw = (float) (Mth.atan2(vel.z, vel.x) * (180.0D / Math.PI)) - 90.0F;
                this.dragon.setYRot(Mth.rotLerp(0.20F, this.dragon.getYRot(), targetYaw));
                this.dragon.yBodyRot = this.dragon.getYRot();

                float targetPitch = (float) (-(Mth.atan2(vel.y, vel.horizontalDistance()) * (180.0D / Math.PI)));
                this.dragon.setXRot(Mth.rotLerp(0.20F, this.dragon.getXRot(), targetPitch));
            }

            this.dragon.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 0.65D);

            if (this.dragon.getHealth() <= 30.0F) {
                this.dragon.triggerFallenState();
            }
        }
    }

    /**
     * Vuelo en Círculos Orbitales (Vuelo de Dragón Celestial Oriental):
     * Traza órbitas continuas y fluidas (radio 38 a 48 bloques) alrededor del santuario o jugador,
     * con ondulación vertical senoidal en altitud como un auténtico dragón oriental.
     */
    static class BossCirclingFlightGoal extends Goal {
        private final AncientCelestialDragonBossEntity dragon;
        private double orbitAngle = 0.0D;
        private double orbitRadius = 42.0D;

        public BossCirclingFlightGoal(AncientCelestialDragonBossEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.dragon.isFallen() && this.dragon.descentTicks <= 0 && this.dragon.getHealth() > 100.0F;
        }

        @Override
        public void start() {
            BlockPos center = getCenter();
            double dx = this.dragon.getX() - (center.getX() + 0.5D);
            double dz = this.dragon.getZ() - (center.getZ() + 0.5D);
            this.orbitAngle = Mth.atan2(dz, dx);
            this.orbitRadius = 38.0D + this.dragon.random.nextDouble() * 10.0D;
        }

        private BlockPos getCenter() {
            if (this.dragon.homeSanctuaryPos != null) return this.dragon.homeSanctuaryPos;
            if (this.dragon.getTarget() != null) return this.dragon.getTarget().blockPosition();
            return this.dragon.blockPosition();
        }

        @Override
        public void tick() {
            BlockPos center = getCenter();
            this.orbitAngle += 0.024D;

            double centerX = center.getX() + 0.5D;
            double centerZ = center.getZ() + 0.5D;
            double baseY = center.getY() + 24.0D;

            double waveY = Math.sin(this.orbitAngle * 2.2D) * 7.5D;
            double targetY = Math.min(this.dragon.level().getMaxBuildHeight() - 15, baseY + waveY);

            double targetX = centerX + Math.cos(this.orbitAngle) * this.orbitRadius;
            double targetZ = centerZ + Math.sin(this.orbitAngle) * this.orbitRadius;

            double dx = targetX - this.dragon.getX();
            double dy = targetY - this.dragon.getY();
            double dz = targetZ - this.dragon.getZ();

            Vec3 currentMotion = this.dragon.getDeltaMovement();
            double blend = 0.045D;
            Vec3 desiredMotion = new Vec3(dx * blend, dy * 0.05D, dz * blend);
            this.dragon.setDeltaMovement(currentMotion.scale(0.88D).add(desiredMotion));

            Vec3 vel = this.dragon.getDeltaMovement();
            if (vel.horizontalDistanceSqr() > 0.005D) {
                float targetYaw = (float) (Mth.atan2(vel.z, vel.x) * (180.0D / Math.PI)) - 90.0F;
                this.dragon.setYRot(Mth.rotLerp(0.18F, this.dragon.getYRot(), targetYaw));
                this.dragon.yBodyRot = this.dragon.getYRot();

                float targetPitch = (float) (-(Mth.atan2(vel.y, vel.horizontalDistance()) * (180.0D / Math.PI)));
                this.dragon.setXRot(Mth.rotLerp(0.18F, this.dragon.getXRot(), targetPitch));
            }

            this.dragon.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 0.55D);
        }
    }
}
