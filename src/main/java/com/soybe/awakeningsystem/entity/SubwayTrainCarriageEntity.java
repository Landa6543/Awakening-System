package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.block.SubwayDoorBlock;
import com.soybe.awakeningsystem.block.SubwayRailTrackBlock;
import com.soybe.awakeningsystem.block.SubwayTrainWheelBlock;
import com.soybe.awakeningsystem.block.SubwayWideRailTrackBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * Entidad de Vagón de Metro Funcional (ORV Coche 3707 - Línea 3 de Seúl).
 * Estructura móvil de 3 bloques de ancho que se desplaza sobre vías anchas y estándar:
 * - W: Acelerar hacia adelante en dirección de la cabina y faros.
 * - S: Frenar o dar marcha atrás.
 * - Restricción estricta de vías: se detiene si no hay vías debajo (no avanza sobre tierra/pasto).
 * - Sigue curvas y giros de 90 grados en las vías.
 * - Estacionamiento / Despliegue en andén: con Shift + Clic derecho vuelve a desplegarse
 *   como estructura sólida de bloques en la estación sin dejar la consola botada.
 */
public class SubwayTrainCarriageEntity extends Entity {

    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(SubwayTrainCarriageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Direction> TRACK_DIR = SynchedEntityData.defineId(SubwayTrainCarriageEntity.class, EntityDataSerializers.DIRECTION);

    private static final float MAX_SPEED = 0.70F;
    private static final float ACCELERATION = 0.025F;
    private static final float BRAKE_DECEL = 0.055F;
    private static final float FRICTION = 0.012F;

    public SubwayTrainCarriageEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED, 0.0F);
        builder.define(TRACK_DIR, Direction.NORTH);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 7;
    }

    @Override
    public void tick() {
        super.tick();

        Level level = this.level();
        float currentSpeed = this.entityData.get(SPEED);
        Direction trackFacing = this.entityData.get(TRACK_DIR);

        this.setYRot(trackFacing.toYRot());
        this.yRotO = this.getYRot();

        BlockPos currentPos = this.blockPosition();
        BlockPos belowPos = currentPos.below();
        BlockState trackState = level.getBlockState(belowPos);
        if (!(trackState.getBlock() instanceof SubwayWideRailTrackBlock || trackState.getBlock() instanceof SubwayRailTrackBlock)) {
            trackState = level.getBlockState(belowPos.below());
        }

        boolean onTrack = (trackState.getBlock() instanceof SubwayWideRailTrackBlock || trackState.getBlock() instanceof SubwayRailTrackBlock);

        LivingEntity driver = this.getControllingPassenger();

        if (!onTrack) {
            currentSpeed = 0.0F;
            if (driver instanceof Player player && this.tickCount % 40 == 0) {
                player.displayClientMessage(Component.translatable("entity.awakening_system.subway.track_end"), true);
            }
        } else if (driver instanceof Player player) {
            float forwardInput = player.zza;

            if (forwardInput > 0.0F) {
                currentSpeed = Math.min(MAX_SPEED, currentSpeed + ACCELERATION);
            } else if (forwardInput < 0.0F) {
                if (currentSpeed > 0.0F) {
                    currentSpeed = Math.max(0.0F, currentSpeed - BRAKE_DECEL);
                } else {
                    currentSpeed = Math.max(-MAX_SPEED * 0.4F, currentSpeed - ACCELERATION * 0.5F);
                }
            } else {
                if (currentSpeed > 0.0F) {
                    currentSpeed = Math.max(0.0F, currentSpeed - FRICTION);
                } else if (currentSpeed < 0.0F) {
                    currentSpeed = Math.min(0.0F, currentSpeed + FRICTION);
                }
            }

            if (player.swinging && this.tickCount % 20 == 0) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 1.2F, 1.3F);
                player.displayClientMessage(Component.translatable("entity.awakening_system.subway.horn"), true);
            }
        } else {
            if (Math.abs(currentSpeed) > 0.01F) {
                currentSpeed *= 0.90F;
            } else {
                currentSpeed = 0.0F;
            }
        }

        if (trackState.getBlock() instanceof SubwayWideRailTrackBlock) {
            SubwayWideRailTrackBlock.WideTrackShape shape = trackState.getValue(SubwayWideRailTrackBlock.SHAPE);
            Direction actualFacing = trackFacing;

            switch (shape) {
                case EAST_WEST -> {
                    if (trackFacing.getAxis() != Direction.Axis.X) {
                        actualFacing = (currentSpeed >= 0) ? Direction.EAST : Direction.WEST;
                    }
                }
                case NORTH_SOUTH -> {
                    if (trackFacing.getAxis() != Direction.Axis.Z) {
                        actualFacing = (currentSpeed >= 0) ? Direction.NORTH : Direction.SOUTH;
                    }
                }
                case SOUTH_EAST -> actualFacing = (trackFacing == Direction.NORTH) ? Direction.EAST : Direction.SOUTH;
                case SOUTH_WEST -> actualFacing = (trackFacing == Direction.NORTH) ? Direction.WEST : Direction.SOUTH;
                case NORTH_EAST -> actualFacing = (trackFacing == Direction.SOUTH) ? Direction.EAST : Direction.NORTH;
                case NORTH_WEST -> actualFacing = (trackFacing == Direction.SOUTH) ? Direction.WEST : Direction.NORTH;
            }

            if (actualFacing != trackFacing) {
                this.entityData.set(TRACK_DIR, actualFacing);
                trackFacing = actualFacing;
                this.setYRot(actualFacing.toYRot());
            }
        }

        if (!level.isClientSide()) {
            this.entityData.set(SPEED, currentSpeed);
        }

        Vec3 stepVec = new Vec3(trackFacing.getStepX(), 0.0D, trackFacing.getStepZ()).scale(currentSpeed);
        this.setDeltaMovement(stepVec.x, this.onGround() ? 0.0D : -0.08D, stepVec.z);
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (Math.abs(currentSpeed) > 0.08F && this.tickCount % 12 == 0) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, 0.45F, 0.85F + Math.abs(currentSpeed) * 0.4F);
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        int index = this.getPassengers().indexOf(passenger);
        Direction dir = this.entityData.get(TRACK_DIR);
        Vec3 forward = new Vec3(dir.getStepX(), 0, dir.getStepZ());
        Vec3 right = new Vec3(dir.getClockWise().getStepX(), 0, dir.getClockWise().getStepZ());

        return switch (index) {
            case 0 -> forward.scale(3.5D).add(0.0D, 0.65D, 0.0D);
            case 1 -> forward.scale(1.2D).add(right.scale(1.0D)).add(0.0D, 0.50D, 0.0D);
            case 2 -> forward.scale(1.2D).add(right.scale(-1.0D)).add(0.0D, 0.50D, 0.0D);
            case 3 -> forward.scale(-0.8D).add(right.scale(1.0D)).add(0.0D, 0.50D, 0.0D);
            case 4 -> forward.scale(-0.8D).add(right.scale(-1.0D)).add(0.0D, 0.50D, 0.0D);
            case 5 -> forward.scale(-2.8D).add(right.scale(1.0D)).add(0.0D, 0.50D, 0.0D);
            case 6 -> forward.scale(-2.8D).add(right.scale(-1.0D)).add(0.0D, 0.50D, 0.0D);
            default -> new Vec3(0.0D, 0.50D, 0.0D);
        };
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Direction dir = this.entityData.get(TRACK_DIR).getClockWise();
        return this.position().add(dir.getStepX() * 2.2D, 0.1D, dir.getStepZ() * 2.2D);
    }

    public void parkAndDeployToWorld(Player player) {
        Level level = this.level();
        if (level.isClientSide()) return;

        BlockPos pos = this.blockPosition();
        Direction dir = this.entityData.get(TRACK_DIR);
        Direction cross = dir.getClockWise();

        this.ejectPassengers();

        BlockPos frontWheel = pos.relative(dir, 2);
        BlockPos backWheel = pos.relative(dir.getOpposite(), 2);
        if (level.getBlockState(frontWheel).canBeReplaced()) {
            level.setBlock(frontWheel, SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, dir), 3);
        }
        if (level.getBlockState(backWheel).canBeReplaced()) {
            level.setBlock(backWheel, SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, dir), 3);
        }

        BlockPos doorPos = pos.relative(cross, 1);
        if (level.getBlockState(doorPos).canBeReplaced() && level.getBlockState(doorPos.above()).canBeReplaced()) {
            level.setBlock(doorPos, SystemBlocks.SUBWAY_DOOR.get().defaultBlockState().setValue(SubwayDoorBlock.FACING, cross).setValue(SubwayDoorBlock.HALF, DoubleBlockHalf.LOWER), 3);
            level.setBlock(doorPos.above(), SystemBlocks.SUBWAY_DOOR.get().defaultBlockState().setValue(SubwayDoorBlock.FACING, cross).setValue(SubwayDoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
        }

        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 1.0F, 0.9F);
        player.displayClientMessage(Component.translatable("entity.awakening_system.subway.parked"), false);

        this.discard();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        Level level = this.level();
        float currentSpeed = this.entityData.get(SPEED);

        if (player.isShiftKeyDown() && Math.abs(currentSpeed) < 0.05F) {
            if (!level.isClientSide()) {
                parkAndDeployToWorld(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide()) {
            if (this.canAddPassenger(player)) {
                player.startRiding(this);
                if (this.getPassengers().indexOf(player) == 0) {
                    player.displayClientMessage(Component.translatable("entity.awakening_system.subway.cabin_instructions"), false);
                } else {
                    player.displayClientMessage(Component.translatable("entity.awakening_system.subway.passenger_boarded"), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (!this.level().isClientSide() && source.getEntity() instanceof Player player) {
            if (player.isCreative() || player.isShiftKeyDown()) {
                this.ejectPassengers();
                this.spawnAtLocation(new ItemStack(SystemBlocks.SUBWAY_TRAIN_WHEEL.get(), 2));
                this.spawnAtLocation(new ItemStack(SystemBlocks.SUBWAY_DOOR.get()));
                this.spawnAtLocation(new ItemStack(SystemBlocks.SUBWAY_STAINLESS_STEEL.get(), 4));
                this.discard();
                return true;
            }
        }
        return super.hurt(source, amount);
    }

    public void setTrackDirection(Direction dir) {
        this.entityData.set(TRACK_DIR, dir);
        this.setYRot(dir.toYRot());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("TrackDir")) {
            this.entityData.set(TRACK_DIR, Direction.byName(tag.getString("TrackDir")));
        }
        if (tag.contains("Speed")) {
            this.entityData.set(SPEED, tag.getFloat("Speed"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("TrackDir", this.entityData.get(TRACK_DIR).getName());
        tag.putFloat("Speed", this.entityData.get(SPEED));
    }
}
