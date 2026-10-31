package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * Entidad animada con GeckoLib para realizar los ejercicios físicos de la Misión Diaria
 * de Solo Leveling: Flexiones (Push-ups), Abdominales (Sit-ups) y Sentadillas (Squats).
 * El jugador monta esta entidad, ocultando temporalmente su modelo vainilla y ejecutando
 * las animaciones 3D fluidas con su propia skin.
 */
public class WorkoutEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public static final int TYPE_PUSH_UPS = 0;
    public static final int TYPE_SIT_UPS = 1;
    public static final int TYPE_SQUATS = 2;

    private static final EntityDataAccessor<Integer> EXERCISE_TYPE =
            SynchedEntityData.defineId(WorkoutEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REPETITIONS =
            SynchedEntityData.defineId(WorkoutEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_REPS =
            SynchedEntityData.defineId(WorkoutEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_COMPLETE =
            SynchedEntityData.defineId(WorkoutEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EXHAUSTED =
            SynchedEntityData.defineId(WorkoutEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(WorkoutEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final RawAnimation PUSH_UPS_ANIM = RawAnimation.begin().thenLoop("push_ups");
    private static final RawAnimation SIT_UPS_ANIM = RawAnimation.begin().thenLoop("sit_ups");
    private static final RawAnimation SQUATS_ANIM = RawAnimation.begin().thenLoop("squats");
    private static final RawAnimation EXHAUSTED_ANIM = RawAnimation.begin().thenLoop("exhausted");
    private static final RawAnimation QUEST_COMPLETE_ANIM = RawAnimation.begin().thenPlay("quest_complete");

    private int exerciseTick = 0;
    private int completeTimer = 0;

    public WorkoutEntity(EntityType<? extends WorkoutEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(EXERCISE_TYPE, TYPE_PUSH_UPS);
        builder.define(REPETITIONS, 0);
        builder.define(TARGET_REPS, 100);
        builder.define(IS_COMPLETE, false);
        builder.define(IS_EXHAUSTED, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    public int getExerciseType() {
        return this.entityData.get(EXERCISE_TYPE);
    }

    public void setExerciseType(int type) {
        this.entityData.set(EXERCISE_TYPE, Math.max(0, Math.min(2, type)));
    }

    public int getRepetitions() {
        return this.entityData.get(REPETITIONS);
    }

    public void setRepetitions(int reps) {
        this.entityData.set(REPETITIONS, reps);
    }

    public int getTargetReps() {
        return this.entityData.get(TARGET_REPS);
    }

    public void setTargetReps(int target) {
        this.entityData.set(TARGET_REPS, target);
    }

    public boolean isComplete() {
        return this.entityData.get(IS_COMPLETE);
    }

    public void setComplete(boolean complete) {
        this.entityData.set(IS_COMPLETE, complete);
    }

    public boolean isExhausted() {
        return this.entityData.get(IS_EXHAUSTED);
    }

    public void setExhausted(boolean exhausted) {
        this.entityData.set(IS_EXHAUSTED, exhausted);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public int getRepDurationTicks() {
        return switch (getExerciseType()) {
            case TYPE_PUSH_UPS -> 40; // 2.0s
            case TYPE_SIT_UPS -> 44;  // 2.2s
            case TYPE_SQUATS -> 36;   // 1.8s
            default -> 40;
        };
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.getPassengers().isEmpty()) {
                if (this.tickCount > 10) {
                    this.discard();
                }
                return;
            }

            Entity passenger = this.getFirstPassenger();
            if (!(passenger instanceof ServerPlayer player) || !player.isAlive()) {
                this.discard();
                return;
            }

            if (this.isComplete()) {
                completeTimer++;
                if (completeTimer >= 56) { // 2.8s de duracion de animacion quest_complete
                    player.stopRiding();
                    this.discard();
                }
                return;
            }

            exerciseTick++;
            int period = getRepDurationTicks();
            if (exerciseTick >= period) {
                exerciseTick = 0;
                onRepetitionCycle(player);
            }
        }
    }

    private void onRepetitionCycle(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        long day = player.level().getDayTime() / 24000L;
        int type = getExerciseType();
        int current = 0;
        int target = 100;
        Component nameComp;

        switch (type) {
            case TYPE_PUSH_UPS -> {
                data.recordPushUps(day, 1);
                current = data.dailyPushUps();
                nameComp = Component.translatable("awakening_system.workout.name.pushups");
            }
            case TYPE_SIT_UPS -> {
                data.recordSitUps(day, 1);
                current = data.dailySitUps();
                nameComp = Component.translatable("awakening_system.workout.name.situps");
            }
            case TYPE_SQUATS -> {
                data.recordSquats(day, 1);
                current = data.dailySquats();
                nameComp = Component.translatable("awakening_system.workout.name.squats");
            }
            default -> {
                nameComp = Component.translatable("awakening_system.workout.name.exercise");
            }
        }

        this.setRepetitions(current);
        AwakeningSystemMod.sync(player);

        // Sonido de esfuerzo y chime suave de progreso
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.65F, 1.0F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 1.7F);

        // Partículas sutiles de sudor / energía
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 0.3, player.getZ(), 4, 0.15, 0.1, 0.15, 0.02);
        }

        // Mensaje de barra de accion
        player.displayClientMessage(
                Component.translatable("awakening_system.workout.actionbar", nameComp, current, target),
                true
        );

        // Si se alcanzan las 100 repeticiones
        if (current >= target) {
            this.setComplete(true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.85F, 1.1F);

            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 0.6, player.getZ(), 24, 0.4, 0.4, 0.4, 0.1);
            }

            player.sendSystemMessage(
                    Component.translatable("awakening_system.workout.chat.reps_complete", nameComp)
            );

            if (data.isDailyQuestComplete()) {
                player.sendSystemMessage(
                        Component.translatable("awakening_system.workout.chat.all_complete")
                );
            }
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, 0.5D, 0.0D);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.getY(), this.getZ());
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity passenger = this.getFirstPassenger();
        if (passenger != null) {
            passenger.stopRiding();
        }
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "workout_controller", 4, state -> {
            if (this.isComplete()) {
                return state.setAndContinue(QUEST_COMPLETE_ANIM);
            }
            if (this.isExhausted()) {
                return state.setAndContinue(EXHAUSTED_ANIM);
            }
            return switch (this.getExerciseType()) {
                case TYPE_PUSH_UPS -> state.setAndContinue(PUSH_UPS_ANIM);
                case TYPE_SIT_UPS -> state.setAndContinue(SIT_UPS_ANIM);
                case TYPE_SQUATS -> state.setAndContinue(SQUATS_ANIM);
                default -> state.setAndContinue(PUSH_UPS_ANIM);
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("exercise_type")) {
            setExerciseType(tag.getInt("exercise_type"));
        }
        if (tag.contains("repetitions")) {
            setRepetitions(tag.getInt("repetitions"));
        }
        if (tag.contains("target_reps")) {
            setTargetReps(tag.getInt("target_reps"));
        }
        if (tag.contains("is_complete")) {
            setComplete(tag.getBoolean("is_complete"));
        }
        if (tag.hasUUID("owner_uuid")) {
            setOwnerUUID(tag.getUUID("owner_uuid"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("exercise_type", getExerciseType());
        tag.putInt("repetitions", getRepetitions());
        tag.putInt("target_reps", getTargetReps());
        tag.putBoolean("is_complete", isComplete());
        getOwnerUUID().ifPresent(uuid -> tag.putUUID("owner_uuid", uuid));
    }
}
