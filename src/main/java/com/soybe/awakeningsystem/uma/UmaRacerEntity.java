package com.soybe.awakeningsystem.uma;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * Entidad de corredora de Uma Musume para las carreras en el Gran Hipódromo de Teyvat.
 * Cuenta con IA de navegación por carriles, ritmo táctico según arquetipo (Nige, Senkou, Sashi, Oikomi)
 * y efectos visuales de aceleración en la recta final.
 */
public class UmaRacerEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_PROFILE_ID =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_LANE_INDEX =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RACE_PROGRESS =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_RUNNING =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_SPRINTING =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FINISH_RANK =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_SKILL_ACTIVE =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_SKILL_NAME =
            SynchedEntityData.defineId(UmaRacerEntity.class, EntityDataSerializers.STRING);

    private int currentWaypointIndex = 0;
    private float currentStamina = 100.0F;
    private boolean staminaInitialized = false;
    private boolean finished = false;
    private int stuckTicks = 0;
    private double lastTrackX = 0;
    private double lastTrackZ = 0;

    private boolean skillUsed = false;
    private int skillTicksRemaining = 0;

    private net.minecraft.core.BlockPos customTrackCorePos = null;
    private boolean isCompanion = false;
    private int customSpeed = 0;
    private int customStamina = 0;
    private int customPower = 0;
    private int customGuts = 0;
    private int customWisdom = 0;

    public boolean isCompanion() {
        return this.isCompanion;
    }

    public void setCompanion(boolean companion) {
        this.isCompanion = companion;
    }

    public net.minecraft.core.BlockPos getCustomTrackCorePos() {
        return customTrackCorePos;
    }

    public void setCustomTrackCorePos(net.minecraft.core.BlockPos pos) {
        this.customTrackCorePos = pos;
    }

    private float spawnYaw = -90.0F;

    public float getSpawnYaw() {
        return this.spawnYaw;
    }

    public void setSpawnYaw(float yaw) {
        this.spawnYaw = yaw;
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        this.yRotO = yaw;
        this.yHeadRotO = yaw;
        this.yBodyRotO = yaw;
    }

    public void setCustomStats(int speed, int stamina, int power, int guts, int wisdom) {
        this.customSpeed = speed;
        this.customStamina = stamina;
        this.customPower = power;
        this.customGuts = guts;
        this.customWisdom = wisdom;
    }

    public UmaRacerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(false);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFILE_ID, UmaProfile.SPECIAL_WEEK.getId());
        builder.define(DATA_LANE_INDEX, 0);
        builder.define(DATA_RACE_PROGRESS, 0.0F);
        builder.define(DATA_IS_RUNNING, false);
        builder.define(DATA_IS_SPRINTING, false);
        builder.define(DATA_FINISH_RANK, 0);
        builder.define(DATA_IS_SKILL_ACTIVE, false);
        builder.define(DATA_SKILL_NAME, "");
    }

    public UmaProfile getUmaProfile() {
        return UmaProfile.byId(this.entityData.get(DATA_PROFILE_ID));
    }

    public void setUmaProfile(UmaProfile profile) {
        if (profile != null) {
            this.entityData.set(DATA_PROFILE_ID, profile.getId());
            this.setCustomName(net.minecraft.network.chat.Component.literal(profile.getDisplayName()));
            this.setCustomNameVisible(true);
        }
    }

    public void setProfileId(String id) {
        if (id != null) {
            setUmaProfile(UmaProfile.byId(id));
        }
    }

    public int getLaneIndex() {
        return this.entityData.get(DATA_LANE_INDEX);
    }

    public void setLaneIndex(int lane) {
        this.entityData.set(DATA_LANE_INDEX, Math.max(0, Math.min(8, lane)));
    }

    public boolean isRaceRunning() {
        return this.entityData.get(DATA_IS_RUNNING);
    }

    public void setRaceRunning(boolean running) {
        this.entityData.set(DATA_IS_RUNNING, running);
        if (running) {
            this.finished = false;
            this.entityData.set(DATA_FINISH_RANK, 0);
            this.currentWaypointIndex = (this.customTrackCorePos != null) ? 1 : 0;
            this.currentStamina = 100.0F;
            this.skillUsed = false;
            this.skillTicksRemaining = 0;
            this.entityData.set(DATA_IS_SKILL_ACTIVE, false);
            this.entityData.set(DATA_SKILL_NAME, "");
        }
    }

    public boolean isSprinting() {
        return this.entityData.get(DATA_IS_SPRINTING);
    }

    public boolean isSkillActive() {
        return this.entityData.get(DATA_IS_SKILL_ACTIVE);
    }

    public String getSkillName() {
        return this.entityData.get(DATA_SKILL_NAME);
    }

    public boolean isFinished() {
        return this.finished || this.entityData.get(DATA_FINISH_RANK) > 0;
    }

    public int getFinishRank() {
        return this.entityData.get(DATA_FINISH_RANK);
    }

    public void setFinishRank(int rank) {
        this.entityData.set(DATA_FINISH_RANK, rank);
        this.finished = true;
        this.entityData.set(DATA_IS_RUNNING, false);
        this.entityData.set(DATA_IS_SPRINTING, false);
        this.setDeltaMovement(0, 0, 0);
        this.getNavigation().stop();
    }

    public float getRaceProgress() {
        return this.entityData.get(DATA_RACE_PROGRESS);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (isRaceRunning()) {
                if (this.tickCount % 2 == 0) {
                    this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                            this.getY() + 0.1,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                            0.0, 0.05, 0.0);
                }
                if (isSprinting() || getRaceProgress() >= 0.75F) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getY() + 0.8 + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                            0.0, 0.1, 0.0);
                    this.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                            this.getY() + 0.6,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.4,
                            0.0, 0.05, 0.0);
                }
                if (this.currentStamina < 20.0F && this.tickCount % 5 == 0) {
                    this.level().addParticle(ParticleTypes.SPLASH,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                            this.getY() + 1.8,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                            0.0, -0.05, 0.0);
                }
                if (isSkillActive()) {
                    for (int p = 0; p < 3; p++) {
                        this.level().addParticle(ParticleTypes.FIREWORK,
                                this.getX() + (this.random.nextDouble() - 0.5) * 0.6,
                                this.getY() + 0.3 + this.random.nextDouble() * 1.0,
                                this.getZ() + (this.random.nextDouble() - 0.5) * 0.6,
                                (this.random.nextDouble() - 0.5) * 0.05, 0.08, (this.random.nextDouble() - 0.5) * 0.05);
                        this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                                this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                                this.getY() + 0.5 + this.random.nextDouble() * 0.8,
                                this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                                0.0, 0.05, 0.0);
                    }
                }
            } else {
                this.setYRot(this.spawnYaw);
                this.setYHeadRot(this.spawnYaw);
                this.setYBodyRot(this.spawnYaw);
                this.yRotO = this.spawnYaw;
                this.yHeadRotO = this.spawnYaw;
                this.yBodyRotO = this.spawnYaw;
            }
            return;
        }

        if (isRaceRunning() && !isFinished()) {
            tickRaceMovement();
        } else {
            this.setYRot(this.spawnYaw);
            this.setYHeadRot(this.spawnYaw);
            this.setYBodyRot(this.spawnYaw);
            this.yRotO = this.spawnYaw;
            this.yHeadRotO = this.spawnYaw;
            this.yBodyRotO = this.spawnYaw;
            double rad = Math.toRadians(this.spawnYaw + 90.0F);
            this.getLookControl().setLookAt(this.getX() + Math.cos(rad) * 4.0, this.getEyeY(), this.getZ() + Math.sin(rad) * 4.0, 180.0F, 180.0F);
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
    }

    private boolean shouldAdvanceWaypoint(Vec3 currentWp, Vec3 nextWp) {
        if (currentWp == null) return true;
        double distSq = this.distanceToSqr(currentWp.x, this.getY(), currentWp.z);

        if (nextWp != null) {
            if (distSq < 12.25) return true;

            double dirX = nextWp.x - currentWp.x;
            double dirZ = nextWp.z - currentWp.z;
            double lenSq = dirX * dirX + dirZ * dirZ;
            if (lenSq > 0.01) {
                double toSelfX = this.getX() - currentWp.x;
                double toSelfZ = this.getZ() - currentWp.z;
                double dot = dirX * toSelfX + dirZ * toSelfZ;
                if (dot > 0 && distSq < 36.0) {
                    return true;
                }
            }
        } else {
            if (distSq < 0.25) return true;

            Vec3 look = this.getViewVector(1.0F);
            double dirX = look.x;
            double dirZ = look.z;
            double lenSq = dirX * dirX + dirZ * dirZ;
            if (lenSq > 0.01) {
                double toSelfX = this.getX() - currentWp.x;
                double toSelfZ = this.getZ() - currentWp.z;
                double dot = dirX * toSelfX + dirZ * toSelfZ;
                if (dot >= 0 && distSq < 36.0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void tickRaceMovement() {
        Vec3 targetWaypoint = null;
        int totalWaypoints = 1;

        if (this.customTrackCorePos != null) {
            if (this.level().getBlockEntity(this.customTrackCorePos) instanceof com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity terminal) {
                totalWaypoints = terminal.getWaypoints().size();
                if (totalWaypoints < 2) return;

                if (this.currentWaypointIndex >= totalWaypoints) {
                    if (!isFinished()) {
                        terminal.onRunnerFinished(this);
                    }
                    return;
                }

                targetWaypoint = terminal.getCustomWaypoint(this.currentWaypointIndex, getLaneIndex());
                if (targetWaypoint == null) return;

                Vec3 nextWp = (this.currentWaypointIndex + 1 < totalWaypoints) ?
                        terminal.getCustomWaypoint(this.currentWaypointIndex + 1, getLaneIndex()) : null;

                if (shouldAdvanceWaypoint(targetWaypoint, nextWp)) {
                    this.currentWaypointIndex++;
                    float progress = (float) this.currentWaypointIndex / (float) totalWaypoints;
                    this.entityData.set(DATA_RACE_PROGRESS, Math.min(1.0F, progress));

                    if (this.currentWaypointIndex >= totalWaypoints) {
                        terminal.onRunnerFinished(this);
                        return;
                    }
                    targetWaypoint = terminal.getCustomWaypoint(this.currentWaypointIndex, getLaneIndex());
                    if (targetWaypoint == null) return;
                }
            } else {
                return;
            }
        } else {
            TeyvatRacetrackManager manager = TeyvatRacetrackManager.get(this.level());
            if (manager == null || !manager.hasTrack()) return;

            totalWaypoints = manager.getTotalWaypoints();
            if (this.currentWaypointIndex >= totalWaypoints) {
                if (!isFinished()) {
                    manager.onRunnerFinished(this);
                }
                return;
            }

            targetWaypoint = manager.getWaypoint(this.currentWaypointIndex, getLaneIndex());
            if (targetWaypoint == null) return;

            Vec3 nextWp = (this.currentWaypointIndex + 1 < totalWaypoints) ?
                    manager.getWaypoint(this.currentWaypointIndex + 1, getLaneIndex()) : null;
            if (shouldAdvanceWaypoint(targetWaypoint, nextWp)) {
                this.currentWaypointIndex++;
                float progress = (float) this.currentWaypointIndex / (float) totalWaypoints;
                this.entityData.set(DATA_RACE_PROGRESS, Math.min(1.0F, progress));

                if (this.currentWaypointIndex >= totalWaypoints) {
                    manager.onRunnerFinished(this);
                    return;
                }
                targetWaypoint = manager.getWaypoint(this.currentWaypointIndex, getLaneIndex());
                if (targetWaypoint == null) return;
            }
        }

        if (targetWaypoint == null) return;

        if (this.getY() < 55.0 || this.isInWater()) {
            this.teleportTo(targetWaypoint.x, targetWaypoint.y, targetWaypoint.z);
            this.setDeltaMovement(0, 0, 0);
            this.stuckTicks = 0;
            return;
        }

        double movedDistSq = (this.getX() - lastTrackX) * (this.getX() - lastTrackX) +
                (this.getZ() - lastTrackZ) * (this.getZ() - lastTrackZ);
        if (movedDistSq < 0.08) {
            this.stuckTicks++;
            if (this.stuckTicks > 80) {
                this.stuckTicks = 0;
                double tdx = targetWaypoint.x - this.getX();
                double tdz = targetWaypoint.z - this.getZ();
                double tdist = Math.sqrt(tdx * tdx + tdz * tdz);
                if (tdist > 0.1) {
                    this.teleportTo(this.getX() + (tdx / tdist) * 1.5, this.getY(), this.getZ() + (tdz / tdist) * 1.5);
                    this.setDeltaMovement((tdx / tdist) * 0.4, 0.1, (tdz / tdist) * 0.4);
                }
            }
        } else {
            this.stuckTicks = 0;
            this.lastTrackX = this.getX();
            this.lastTrackZ = this.getZ();
        }

        UmaProfile profile = getUmaProfile();
        float progress = getRaceProgress();
        float calculatedSpeed = profile.getBaseSpeed();
        boolean finalSprint = false;

        String tactique = profile.getTactique().toLowerCase();
        if (tactique.contains("dai-nige")) {
            if (progress < 0.45F) {
                calculatedSpeed += 0.18F;
            } else {
                calculatedSpeed -= 0.06F;
            }
        } else if (tactique.contains("nige")) {
            if (progress < 0.60F) {
                calculatedSpeed += 0.08F;
            }
        } else if (tactique.contains("oikomi")) {
            if (progress < 0.62F) {
                calculatedSpeed -= 0.04F;
            } else {
                calculatedSpeed += 0.22F;
                finalSprint = true;
            }
        } else if (tactique.contains("sashi")) {
            if (progress < 0.60F) {
                calculatedSpeed -= 0.02F;
            } else {
                calculatedSpeed += 0.16F;
                finalSprint = true;
            }
        } else {
            if (progress > 0.68F) {
                calculatedSpeed += 0.10F;
                finalSprint = true;
            }
        }

        if (progress > 0.62F && (tactique.contains("oikomi") || tactique.contains("sashi") || tactique.contains("senkou"))) {
            List<UmaRacerEntity> rivals = this.level().getEntitiesOfClass(UmaRacerEntity.class, this.getBoundingBox().inflate(32.0));
            float maxAheadProgress = 0.0F;
            for (UmaRacerEntity rival : rivals) {
                if (rival != this && rival.isRaceRunning()) {
                    float rProg = rival.getRaceProgress();
                    if (rProg > progress && rProg > maxAheadProgress) {
                        maxAheadProgress = rProg;
                    }
                }
            }
            if (maxAheadProgress > progress) {
                float gap = maxAheadProgress - progress;
                float remontadaBonus;
                if (tactique.contains("oikomi")) {
                    remontadaBonus = Math.min(0.24F, 0.12F + gap * 0.8F);
                } else if (tactique.contains("sashi")) {
                    remontadaBonus = Math.min(0.18F, 0.08F + gap * 0.6F);
                } else {
                    remontadaBonus = Math.min(0.10F, 0.04F + gap * 0.4F);
                }
                calculatedSpeed += remontadaBonus;
                finalSprint = true;
            }
        }

        boolean inSlipstream = false;
        double overtakeNudgeX = 0;
        double overtakeNudgeZ = 0;
        Vec3 myLook = this.getViewVector(1.0F);

        List<UmaRacerEntity> nearbyRacers = this.level().getEntitiesOfClass(UmaRacerEntity.class, this.getBoundingBox().inflate(4.0));
        for (UmaRacerEntity rival : nearbyRacers) {
            if (rival != this && rival.isRaceRunning() && !rival.isFinished()) {
                double toRivalX = rival.getX() - this.getX();
                double toRivalZ = rival.getZ() - this.getZ();
                double distSq = toRivalX * toRivalX + toRivalZ * toRivalZ;
                if (distSq > 0.05 && distSq < 14.0) {
                    double dist = Math.sqrt(distSq);
                    double dirX = toRivalX / dist;
                    double dirZ = toRivalZ / dist;
                    double dot = dirX * myLook.x + dirZ * myLook.z;
                    if (dot > 0.65) {
                        inSlipstream = true;
                        calculatedSpeed += 0.05F;
                        
                        if (dist < 2.2) {
                            double perpX = -dirZ;
                            double perpZ = dirX;
                            double side = ((this.getId() + rival.getId()) % 2 == 0) ? 1.0 : -1.0;
                            overtakeNudgeX = perpX * side * 0.16;
                            overtakeNudgeZ = perpZ * side * 0.16;
                        }
                        break;
                    }
                }
            }
        }

        if (!this.staminaInitialized) {
            this.currentStamina = 100.0F + (this.customStamina > 0 ? (this.customStamina / 1200.0F) * 60.0F : 30.0F);
            this.staminaInitialized = true;
        }

        float staminaDrain = finalSprint ? 0.15F : (isSkillActive() ? 0.20F : 0.08F);
        if (inSlipstream) {
            staminaDrain *= 0.75F;
        }
        if (this.customGuts > 0) {
            staminaDrain *= (1.0F - Math.min(0.35F, (this.customGuts / 1200.0F) * 0.30F));
        }
        this.currentStamina = Math.max(0.0F, this.currentStamina - staminaDrain);

        if (this.currentStamina <= 0.0F) {
            calculatedSpeed *= 0.76F;
            if (this.tickCount % 8 == 0 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.2, this.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
            }
        }

        if (!this.skillUsed) {
            int rank = calculateCurrentRank();
            int effectiveWisdom = (this.customWisdom > 0) ? this.customWisdom : 400;
            float wisdomJitter = ((this.getId() % 9) - 4) * 0.015F - (effectiveWisdom / 1200.0F) * 0.02F;
            UmaProfile.SkillTriggerCategory cat = profile.getSkillTriggerCategory();
            boolean triggerSkill = false;

            switch (cat) {
                case LEAD_BREAKAWAY -> {
                    if (rank <= 2 && progress >= (0.36F + wisdomJitter) && progress <= 0.62F) {
                        triggerSkill = true;
                    }
                }
                case CHASE_SURGE -> {
                    if (rank >= 2 && rank <= 4 && progress >= (0.50F + wisdomJitter) && progress <= 0.74F) {
                        triggerSkill = true;
                    }
                }
                case FINAL_SPURT -> {
                    if (rank >= 3 && rank <= 6 && progress >= (0.66F + wisdomJitter) && progress <= 0.88F) {
                        triggerSkill = true;
                    }
                }
                case OIKOMI_SWEEP -> {
                    if (rank >= 4 && progress >= (0.72F + wisdomJitter) && progress <= 0.94F) {
                        triggerSkill = true;
                    }
                }
            }
            if (progress >= (0.84F + Math.abs(wisdomJitter))) {
                triggerSkill = true;
            }

            if (triggerSkill) {
                this.skillUsed = true;
                this.skillTicksRemaining = profile.getSkillDurationTicks();
                this.entityData.set(DATA_IS_SKILL_ACTIVE, true);
                this.entityData.set(DATA_SKILL_NAME, profile.getSpecialSkill());

                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.4, 0.3, 0.12);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.8, this.getZ(), 15, 0.25, 0.3, 0.25, 0.08);

                    TeyvatRacetrackManager manager = TeyvatRacetrackManager.get(sl);
                    if (manager != null) {
                        manager.broadcastToRacetrack(net.minecraft.network.chat.Component.translatable("awakening_system.uma.skill_trigger", profile.getDisplayName(), profile.getSpecialSkill()));
                    }
                }
            }
        }

        if (this.skillTicksRemaining > 0) {
            this.skillTicksRemaining--;
            calculatedSpeed += profile.getSkillSpeedBoost();
            finalSprint = true;
            if (this.skillTicksRemaining <= 0) {
                this.entityData.set(DATA_IS_SKILL_ACTIVE, false);
            }
        }

        if (this.customSpeed > 0) {
            float speedBonus = (this.customSpeed - 350) * 0.00022f;
            float powerBonus = (this.customPower - 350) * 0.00012f;
            calculatedSpeed += (speedBonus + powerBonus);
            if (finalSprint) {
                calculatedSpeed += (this.customGuts - 350) * 0.00018f;
            }
        }

        float variation = (float) Math.sin((this.tickCount + this.getId() * 37) * 0.15) * 0.02F;
        calculatedSpeed += variation;

        this.entityData.set(DATA_IS_SPRINTING, finalSprint);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Math.max(0.20, calculatedSpeed));

        if (targetWaypoint != null) {
            double dx = targetWaypoint.x - this.getX();
            double dz = targetWaypoint.z - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.1) {
                float targetYaw = (float) (net.minecraft.util.Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                this.setYRot(targetYaw);
                this.setYHeadRot(targetYaw);
                this.setYBodyRot(targetYaw);
                this.yRotO = targetYaw;
                this.yHeadRotO = targetYaw;
                this.yBodyRotO = targetYaw;
                this.getLookControl().setLookAt(targetWaypoint.x, targetWaypoint.y + 1.2, targetWaypoint.z, 30.0F, 30.0F);

                double maxAllowedSpd = isSkillActive() ? 0.88 : (finalSprint ? 0.65 : 0.46);
                double spd = Math.max(0.24, Math.min(maxAllowedSpd, calculatedSpeed));
                double mx = (dx / dist) * spd + overtakeNudgeX;
                double mz = (dz / dist) * spd + overtakeNudgeZ;
                this.setDeltaMovement(mx, this.getDeltaMovement().y, mz);

                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    if (isSkillActive()) {
                        if (this.tickCount % 2 == 0) {
                            sl.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 0.8, this.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.6, this.getZ(), 3, 0.15, 0.15, 0.15, 0.04);
                        }
                    } else if (finalSprint || progress >= 0.70F) {
                        if (this.tickCount % 3 == 0) {
                            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.1, 0.05, 0.1, 0.01);
                            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5, this.getZ(), 2, 0.1, 0.1, 0.1, 0.03);
                        }
                    }
                }
            }
            this.getNavigation().moveTo(targetWaypoint.x, targetWaypoint.y, targetWaypoint.z, 1.35);
        }
    }

    private int calculateCurrentRank() {
        List<UmaRacerEntity> racers = this.level().getEntitiesOfClass(UmaRacerEntity.class, this.getBoundingBox().inflate(64.0));
        float myProg = getRaceProgress();
        int rank = 1;
        for (UmaRacerEntity r : racers) {
            if (r != this && r.isRaceRunning() && r.getRaceProgress() > myProg) {
                rank++;
            }
        }
        return rank;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return this.isCompanion;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsCompanion", this.isCompanion);
        compound.putString("UmaProfile", this.entityData.get(DATA_PROFILE_ID));
        compound.putInt("LaneIndex", this.entityData.get(DATA_LANE_INDEX));
        compound.putInt("FinishRank", this.entityData.get(DATA_FINISH_RANK));
        compound.putBoolean("IsRunning", this.entityData.get(DATA_IS_RUNNING));
        compound.putInt("WaypointIndex", this.currentWaypointIndex);
        compound.putFloat("SpawnYaw", this.spawnYaw);
        if (customTrackCorePos != null) {
            compound.putLong("CustomTrackPos", customTrackCorePos.asLong());
        }
        compound.putInt("CustomSpeed", customSpeed);
        compound.putInt("CustomStamina", customStamina);
        compound.putInt("CustomPower", customPower);
        compound.putInt("CustomGuts", customGuts);
        compound.putInt("CustomWisdom", customWisdom);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.isCompanion = compound.getBoolean("IsCompanion");
        if (!this.isCompanion && this.customTrackCorePos == null) {
            this.discard();
            return;
        }
        if (compound.contains("UmaProfile")) {
            setUmaProfile(UmaProfile.byId(compound.getString("UmaProfile")));
        }
        if (compound.contains("LaneIndex")) {
            setLaneIndex(compound.getInt("LaneIndex"));
        }
        if (compound.contains("FinishRank")) {
            setFinishRank(compound.getInt("FinishRank"));
        }
        if (compound.contains("SpawnYaw")) {
            this.spawnYaw = compound.getFloat("SpawnYaw");
        } else {
            this.spawnYaw = this.getYRot();
        }
        if (!this.entityData.get(DATA_IS_RUNNING)) {
            this.setYRot(this.spawnYaw);
            this.setYHeadRot(this.spawnYaw);
            this.setYBodyRot(this.spawnYaw);
            this.yRotO = this.spawnYaw;
            this.yHeadRotO = this.spawnYaw;
            this.yBodyRotO = this.spawnYaw;
        }
        if (compound.contains("IsRunning")) {
            setRaceRunning(compound.getBoolean("IsRunning"));
        }
        if (compound.contains("WaypointIndex")) {
            this.currentWaypointIndex = compound.getInt("WaypointIndex");
        }
        if (compound.contains("CustomTrackPos")) {
            this.customTrackCorePos = net.minecraft.core.BlockPos.of(compound.getLong("CustomTrackPos"));
        }
        this.customSpeed = compound.getInt("CustomSpeed");
        this.customStamina = compound.getInt("CustomStamina");
        this.customPower = compound.getInt("CustomPower");
        this.customGuts = compound.getInt("CustomGuts");
        this.customWisdom = compound.getInt("CustomWisdom");
    }
}
