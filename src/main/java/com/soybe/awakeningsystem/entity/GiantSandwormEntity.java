package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.penalty.PenaltyZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sandworm boss entity faithful to Enter Sandworm original mechanics.
 * Features undulating subterranean swimming, charge lunges, continuous corkscrew
 * body rotation, and train-physics trailing segments.
 */
public class GiantSandwormEntity extends Monster {

    public enum Stage {
        WAVE_MOVEMENT,
        CHARGE,
        RESET_Y
    }

    private static final EntityDataAccessor<Integer> BODY_PART_COUNT =
            SynchedEntityData.defineId(GiantSandwormEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.awakening_system.giant_sandworm.boss_title"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    @Nullable
    public GiantSandwormHeadPart head;
    public final List<GiantSandwormBodyPart> bodyParts = new ArrayList<>();

    private Stage stage = Stage.WAVE_MOVEMENT;
    private int waveTicks = 0;
    private int chargeTickCounter = 0;
    private static final int CHARGE_TICK_DURATION = 40;
    private Vec3 chargeDirection = Vec3.ZERO;
    private boolean wasInSolid = false;
    private Player currentTarget;
    private float currentHeadingYaw = 0.0F;
    private float wanderAngle = 0.0F;

    public GiantSandwormEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 200.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BODY_PART_COUNT, 32);
    }

    public void scaleToPlayerLevel(int playerLevel) {
        int safeLvl = Math.max(1, playerLevel);
        double maxHp = 120.0D + (safeLvl * 6.0D);
        double attackDmg = Math.min(6.0D + (safeLvl * 0.35D), 30.0D);
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHp);
            this.setHealth((float) maxHp);
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDmg);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        if (!level.isClientSide()) {
            int parts = 28 + this.random.nextInt(8); // 28 to 35 segments
            this.entityData.set(BODY_PART_COUNT, parts);
            this.cleanupParts();
            this.rebuildParts();
        }
        return data;
    }

    public void rebuildParts() {
        if (level().isClientSide) return;

        this.head = new GiantSandwormHeadPart(ModEntities.GIANT_SANDWORM_HEAD.get(), level());
        this.head.initOwner(this);
        this.head.setPos(this.getX(), this.getY(), this.getZ());
        this.head.setYRot(this.getYRot());
        this.head.setXRot(this.getXRot());
        this.head.yRotO = this.getYRot();
        this.head.xRotO = this.getXRot();
        level().addFreshEntity(this.head);

        int parts = this.entityData.get(BODY_PART_COUNT);
        if (parts < 20 || parts > 45) parts = 32;

        float yawRad = (float) Math.toRadians(this.getYRot());
        double backX = Math.sin(yawRad);
        double backZ = -Math.cos(yawRad);

        this.bodyParts.clear();
        for (int i = 0; i < parts; i++) {
            GiantSandwormBodyPart bodyPart = new GiantSandwormBodyPart(ModEntities.GIANT_SANDWORM_BODY.get(), level());
            bodyPart.initOwner(this);
            double px = this.getX() + backX * (i + 1) * 2.0D;
            double py = this.getY();
            double pz = this.getZ() + backZ * (i + 1) * 2.0D;
            bodyPart.setPos(px, py, pz);
            bodyPart.setYRot(this.getYRot());
            bodyPart.setXRot(this.getXRot());
            bodyPart.yRotO = this.getYRot();
            bodyPart.xRotO = this.getXRot();

            // Taper the tail (last 15 segments taper down gracefully to 1)
            if (parts - i < 15) {
                bodyPart.setRenderScale(Math.max(1, parts - i));
            } else {
                bodyPart.setRenderScale(15);
            }

            level().addFreshEntity(bodyPart);
            this.bodyParts.add(bodyPart);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Crucial: never despawn far away in penalty zone
    }

    @Override
    public void checkDespawn() {
        // Prevent random vanilla monster despawning
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 65536.0D; // 256 block render distance
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();

        if (this.head != null) {
            this.head.yRotO = this.head.getYRot();
            this.head.xRotO = this.head.getXRot();
        }

        if (!level().isClientSide) {
            if (this.head == null || this.bodyParts.isEmpty()) {
                this.rebuildParts();
            }

            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Boss bar only visible when player is within 64 blocks of this worm
            for (Player player : level().players()) {
                if (player instanceof ServerPlayer sp) {
                    if (sp.distanceToSqr(this) <= 4096.0D) {
                        if (!this.bossEvent.getPlayers().contains(sp)) {
                            this.bossEvent.addPlayer(sp);
                        }
                    } else {
                        this.bossEvent.removePlayer(sp);
                    }
                }
            }

            this.currentTarget = findTargetPlayer();

            // Movement logic faithful to original mod
            tickMovementAI();

            // Collision audio and sand eruption particles
            checkBlockCollisions();

            // Synchronize parts train positions and rotations
            tickHeadPart();
            tickBodyParts();
        }
    }

    private Player findTargetPlayer() {
        if (this.currentTarget != null && this.currentTarget.isAlive() && !this.currentTarget.isSpectator()) {
            if (this.distanceToSqr(this.currentTarget) < 40000.0D) {
                return this.currentTarget;
            }
        }

        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Player p : level().players()) {
            if (p.isAlive() && !p.isSpectator()) {
                // If in creative, engage if within 40 blocks so user can test the boss attacks
                if (p.isCreative() && p.distanceToSqr(this) > 1600.0D) {
                    continue;
                }
                double dSq = p.distanceToSqr(this);
                if (dSq < 40000.0D && dSq < nearestDistSq) { // 200 blocks
                    nearest = p;
                    nearestDistSq = dSq;
                }
            }
        }
        return nearest;
    }

    private double getGroundHeight() {
        int blockX = (int) this.getX();
        int blockZ = (int) this.getZ();
        if (level() instanceof ServerLevel sl && !sl.hasChunk(blockX >> 4, blockZ >> 4)) {
            return this.getY();
        }
        int h = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
        if (h <= level().getMinBuildHeight() || h < 40) {
            return 70.0D;
        }
        return (double) h;
    }

    private void tickMovementAI() {
        if (this.currentHeadingYaw == 0.0F && this.getYRot() != 0.0F) {
            this.currentHeadingYaw = this.getYRot();
            this.wanderAngle = this.getYRot();
        }

        double groundY = getGroundHeight();
        double targetSubY = groundY - 4.0D;

        if (stage == Stage.WAVE_MOVEMENT) {
            // If the worm was airborne or launched into the sky, immediately force a dive into the sand
            if (this.getY() > groundY + 1.0D) {
                stage = Stage.RESET_Y;
                chargeTickCounter = 0;
                setDeltaMovement(this.getDeltaMovement().x * 0.7D, -1.2D, this.getDeltaMovement().z * 0.7D);
            } else if (currentTarget != null && currentTarget.isAlive()) {
                double dx = currentTarget.getX() - getX();
                double dz = currentTarget.getZ() - getZ();
                double horizDist = Math.sqrt(dx * dx + dz * dz);

                // If the worm gets too far away (> 75 blocks), subterranean burrow relocation so it NEVER disappears in unloaded chunks
                if (horizDist > 75.0D) {
                    double relocateAngle = currentTarget.getYRot() * (Math.PI / 180.0D) + (this.random.nextDouble() * 1.0D - 0.5D);
                    double relocateDist = 32.0D + this.random.nextDouble() * 10.0D;
                    double targetX = currentTarget.getX() - Math.sin(relocateAngle) * relocateDist;
                    double targetZ = currentTarget.getZ() + Math.cos(relocateAngle) * relocateDist;
                    double targetY = getGroundHeight() - 4.0D;

                    this.setPos(targetX, targetY, targetZ);
                    this.currentHeadingYaw = (float) (Mth.atan2(currentTarget.getX() - targetX, currentTarget.getZ() - targetZ) * 180.0F / (float) Math.PI) + 180.0F;
                    this.rebuildParts();
                    level().playSound(null, blockPosition(), ModSounds.SANDWORM_ROAR.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
                    return;
                }

                // Smooth turning towards player with realistic max turn rate (max 3.0 degrees per tick)
                float desiredYaw = (float) (Mth.atan2(dx, dz) * 180.0F / (float) Math.PI) + 180.0F;
                float yawDiff = Mth.wrapDegrees(desiredYaw - currentHeadingYaw);
                float maxTurn = 3.0F;
                currentHeadingYaw += Mth.clamp(yawDiff, -maxTurn, maxTurn);

                double rad = Math.toRadians(currentHeadingYaw);
                // Subterranean speed scaling: speeds up if player sprints or runs away
                double speedH = horizDist > 40.0D ? 1.05D : (horizDist > 25.0D ? 0.85D : 0.65D);
                double moveX = -Math.sin(rad) * speedH;
                double moveZ = -Math.cos(rad) * speedH;

                // Subterranean wave oscillation around targetSubY (-5.6 to -2.4 blocks below surface)
                double waveOffset = Math.sin(waveTicks * 0.08D) * 1.6D;
                double desiredY = targetSubY + waveOffset;
                double yDelta = Mth.clamp((desiredY - this.getY()) * 0.16D, -0.5D, 0.5D);

                // Chance to breach the sand and leap (charge) toward the player when submerged and in range
                if (horizDist > 14.0D && horizDist < 48.0D && this.getY() <= groundY - 1.5D) {
                    if (this.random.nextInt(60) == 0) {
                        stage = Stage.CHARGE;
                        chargeTickCounter = 0;
                        Vec3 leapAim = currentTarget.position().add(0, 1.5D, 0).subtract(position()).normalize();
                        chargeDirection = leapAim;
                        level().playSound(null, blockPosition(), ModSounds.SANDWORM_ROAR.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
                    }
                }

                setDeltaMovement(moveX, yDelta, moveZ);
            } else {
                // If wandering in penalty zone, steer towards active players so it never wanders off into the infinite void
                if (PenaltyZoneManager.isPenaltyDimension(level())) {
                    for (Player p : level().players()) {
                        if (p.isAlive() && !p.isSpectator()) {
                            double pDx = p.getX() - getX();
                            double pDz = p.getZ() - getZ();
                            wanderAngle = (float) (Mth.atan2(pDx, pDz) * 180.0F / (float) Math.PI) + 180.0F;
                            break;
                        }
                    }
                }

                // Idle subterranean cruising under the dunes in broad, majestic S-curves (no tight circles!)
                if (waveTicks % 160 == 0) {
                    wanderAngle += (this.random.nextFloat() - 0.5F) * 50.0F;
                }
                float sCurve = (float) (Math.sin(waveTicks * 0.02D) * 20.0D);
                float desiredYaw = wanderAngle + sCurve;

                float yawDiff = Mth.wrapDegrees(desiredYaw - currentHeadingYaw);
                currentHeadingYaw += Mth.clamp(yawDiff, -1.2F, 1.2F); // wide turning radius

                double rad = Math.toRadians(currentHeadingYaw);
                double speedH = 0.55D;
                double moveX = -Math.sin(rad) * speedH;
                double moveZ = -Math.cos(rad) * speedH;

                double waveOffset = Math.sin(waveTicks * 0.06D) * 1.4D;
                double desiredY = targetSubY + waveOffset;
                double yDelta = Mth.clamp((desiredY - this.getY()) * 0.14D, -0.4D, 0.4D);

                setDeltaMovement(moveX, yDelta, moveZ);
            }
            waveTicks++;
        } else if (stage == Stage.CHARGE) {
            // Dynamic parabolic leap: bursts up from sand, arcs down naturally
            chargeTickCounter++;
            double progress = (double) chargeTickCounter / CHARGE_TICK_DURATION;

            double leapSpeedH = 1.30D;
            double verticalCurve = (1.0D - (progress * 1.8D)) * 1.1D;

            float leapYaw = (float) (Mth.atan2(chargeDirection.x, chargeDirection.z) * 180.0F / (float) Math.PI) + 180.0F;
            currentHeadingYaw = leapYaw;

            setDeltaMovement(
                    chargeDirection.x * leapSpeedH,
                    verticalCurve,
                    chargeDirection.z * leapSpeedH
            );

            checkAttackPlayer();

            // When charge expires or worm has plunged deep back into the sand
            if (chargeTickCounter >= CHARGE_TICK_DURATION || (chargeTickCounter > 15 && this.getY() < targetSubY)) {
                stage = Stage.RESET_Y;
                chargeTickCounter = 0;
            }
        } else if (stage == Stage.RESET_Y) {
            // Dive phase: smoothly plunge down back into the dunes
            if (this.getY() <= targetSubY + 0.5D) {
                stage = Stage.WAVE_MOVEMENT;
                waveTicks = 0;
                setDeltaMovement(this.getDeltaMovement().x * 0.5D, 0, this.getDeltaMovement().z * 0.5D);
            } else {
                double diveH = 0.55D;
                double diveY = Math.min(-0.90D, (targetSubY - this.getY()) * 0.22D);
                double rad = Math.toRadians(currentHeadingYaw);
                double moveX = -Math.sin(rad) * diveH;
                double moveZ = -Math.cos(rad) * diveH;
                setDeltaMovement(moveX, diveY, moveZ);
            }
        } else {
            stage = Stage.WAVE_MOVEMENT;
            waveTicks = 0;
        }

        // Apply movement velocity
        this.setPos(this.getX() + this.getDeltaMovement().x,
                    this.getY() + this.getDeltaMovement().y,
                    this.getZ() + this.getDeltaMovement().z);

        checkAttackPlayer();
    }

    private void tickHeadPart() {
        if (head == null) return;
        head.setPos(this.getX(), this.getY(), this.getZ());
        Vec3 v = getDeltaMovement();
        if (v.lengthSqr() > 1.0E-6) {
            double velX = v.x;
            double velZ = v.z;
            double velY = v.y;

            double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);
            float newYaw = (float) (Mth.atan2(velX, velZ) * 180.0F / (float) Math.PI) + 180.0F;
            float newPitch = (float) (-(Mth.atan2(velY, horizontalSpeed) * 180.0F / (float) Math.PI));

            head.setYRot(newYaw);
            head.setXRot(newPitch);
            this.setYRot(newYaw);
            this.setXRot(newPitch);
        }
    }

    private void tickBodyParts() {
        Vec3 prevPos = head != null ? head.position() : position();

        for (int i = 0; i < bodyParts.size(); i++) {
            GiantSandwormBodyPart part = bodyParts.get(i);
            if (part == null) continue;

            part.yRotO = part.getYRot();
            part.xRotO = part.getXRot();

            Vec3 partPos = part.position();
            Vec3 delta = prevPos.subtract(partPos);

            double desiredDistance = 2.0D; // authentic segment spacing
            double distance = delta.length();

            if (distance > 1.0E-4) {
                Vec3 dir = delta.scale(1.0D / distance);
                Vec3 targetPos = prevPos.subtract(dir.scale(desiredDistance));
                part.setDeltaMovement(targetPos.subtract(partPos));
                part.setPos(targetPos.x, targetPos.y, targetPos.z);

                double velX = dir.x;
                double velY = dir.y;
                double velZ = dir.z;

                double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);
                float newYaw = (float) (Mth.atan2(velX, velZ) * 180.0F / (float) Math.PI) + 180.0F;
                float newPitch = (float) (-(Mth.atan2(velY, horizontalSpeed) * 180.0F / (float) Math.PI));

                part.setYRot(newYaw);
                part.setXRot(newPitch);
            }

            prevPos = part.position();
        }
    }

    private void checkAttackPlayer() {
        AABB hitBox = this.getBoundingBox().inflate(3.5D);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (Player p : level().getEntitiesOfClass(Player.class, hitBox)) {
            if (p.isAlive() && !p.isSpectator() && !p.isCreative()) {
                p.hurt(level().damageSources().mobAttack(this), damage);
                p.knockback(1.4D, this.getDeltaMovement().x, this.getDeltaMovement().z);
            }
        }
    }

    private void checkBlockCollisions() {
        BlockPos currentPos = blockPosition();
        BlockState state = level().getBlockState(currentPos);
        boolean isSolid = state.isSolid();

        if (isSolid != this.wasInSolid) {
            this.wasInSolid = isSolid;
            if (level() instanceof ServerLevel serverLevel) {
                BlockState sandState = Blocks.SAND.defaultBlockState();
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, sandState),
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        75, 4.0D, 3.0D, 4.0D, 0.35D);

                serverLevel.playSound(null, currentPos, ModSounds.SANDWORM_BOOM.get(), SoundSource.HOSTILE, 3.0F, 0.85F + this.random.nextFloat() * 0.3F);
                serverLevel.playSound(null, currentPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 0.6F);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof Player player) {
            this.currentTarget = player;
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.0D, this.getZ(), 4, 2.0D, 2.0D, 2.0D, 0.1D);
            serverLevel.playSound(null, blockPosition(), ModSounds.SANDWORM_BOOM.get(), SoundSource.HOSTILE, 4.0F, 0.6F);

            if (damageSource.getEntity() instanceof ServerPlayer sp) {
                PenaltyZoneManager.onSandwormKilled(serverLevel, sp);
            } else if (this.currentTarget instanceof ServerPlayer sp) {
                PenaltyZoneManager.onSandwormKilled(serverLevel, sp);
            }
        }

        this.cleanupParts();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.cleanupParts();
    }

    private void cleanupParts() {
        if (this.head != null) {
            this.head.discard();
        }
        for (GiantSandwormBodyPart part : this.bodyParts) {
            part.discard();
        }
        this.bossEvent.removeAllPlayers();
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("wave_ticks", this.waveTicks);
        tag.putString("stage", this.stage.name());
        tag.putFloat("heading_yaw", this.currentHeadingYaw);
        tag.putFloat("wander_angle", this.wanderAngle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.waveTicks = tag.getInt("wave_ticks");
        if (tag.contains("stage")) {
            try {
                this.stage = Stage.valueOf(tag.getString("stage"));
            } catch (Exception ignored) {}
        }
        if (tag.contains("heading_yaw")) this.currentHeadingYaw = tag.getFloat("heading_yaw");
        if (tag.contains("wander_angle")) this.wanderAngle = tag.getFloat("wander_angle");
    }
}
