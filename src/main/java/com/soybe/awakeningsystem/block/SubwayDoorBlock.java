package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import com.soybe.awakeningsystem.block.entity.SubwayDoorBlockEntity;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;

/**
 * Puerta corrediza doble de acero inoxidable para vagones de metro y andenes (ORV).
 * Cuenta con ventanales de cristal reforzado, franja naranja de la Línea 3 de Seúl,
 * mecanismo de apertura doble sincronizada cuando se colocan en pares y animación suave
 * de deslizamiento neumático en tiempo real (SmoothStep).
 */
public class SubwayDoorBlock extends DoorBlock implements EntityBlock {
    public static final MapCodec<SubwayDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BlockSetType.CODEC.fieldOf("block_set_type").forGetter(DoorBlock::type),
                    propertiesCodec()
            ).apply(instance, SubwayDoorBlock::new)
    );

    public SubwayDoorBlock(BlockSetType type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    public SubwayDoorBlock(BlockBehaviour.Properties properties) {
        this(BlockSetType.IRON, properties);
    }

    @Override
    public MapCodec<SubwayDoorBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SubwayDoorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return blockEntityType == ModBlockEntities.SUBWAY_DOOR.get()
                    ? (lvl, p, st, be) -> SubwayDoorBlockEntity.clientTick(lvl, p, st, (SubwayDoorBlockEntity) be)
                    : null;
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean newOpen = !state.getValue(OPEN);

        setDoorOpen(level, pos, state, newOpen);
        if (newOpen) {
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.9F, 1.1F);
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.45F, 1.8F);
        } else {
            level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.9F, 0.95F);
            level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.45F, 1.6F);
        }

        Direction facing = state.getValue(FACING);
        DoorHingeSide hinge = state.getValue(HINGE);
        Direction sisterDir = (hinge == DoorHingeSide.LEFT) ? facing.getClockWise() : facing.getCounterClockWise();
        BlockPos sisterPos = pos.relative(sisterDir);
        BlockState sisterState = level.getBlockState(sisterPos);

        if (sisterState.is(this) && sisterState.getValue(FACING) == facing && sisterState.getValue(HALF) == state.getValue(HALF)) {
            setDoorOpen(level, sisterPos, sisterState, newOpen);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static final VoxelShape CLOSED_NORTH = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape CLOSED_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    private static final VoxelShape CLOSED_WEST  = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape CLOSED_EAST  = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    private static final VoxelShape OPEN_NORTH_LEFT  = Block.box(0.0D, 0.0D, 13.0D, 3.0D, 16.0D, 16.0D);
    private static final VoxelShape OPEN_NORTH_RIGHT = Block.box(13.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape OPEN_SOUTH_LEFT  = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    private static final VoxelShape OPEN_SOUTH_RIGHT = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 3.0D);
    private static final VoxelShape OPEN_WEST_LEFT   = Block.box(13.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape OPEN_WEST_RIGHT  = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    private static final VoxelShape OPEN_EAST_LEFT   = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 3.0D);
    private static final VoxelShape OPEN_EAST_RIGHT  = Block.box(0.0D, 0.0D, 13.0D, 3.0D, 16.0D, 16.0D);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean open = state.getValue(OPEN);
        DoorHingeSide hinge = state.getValue(HINGE);

        if (!open) {
            return switch (facing) {
                case SOUTH -> CLOSED_SOUTH;
                case WEST -> CLOSED_WEST;
                case EAST -> CLOSED_EAST;
                default -> CLOSED_NORTH;
            };
        }

        return switch (facing) {
            case NORTH -> (hinge == DoorHingeSide.LEFT) ? OPEN_NORTH_LEFT : OPEN_NORTH_RIGHT;
            case SOUTH -> (hinge == DoorHingeSide.LEFT) ? OPEN_SOUTH_LEFT : OPEN_SOUTH_RIGHT;
            case WEST  -> (hinge == DoorHingeSide.LEFT) ? OPEN_WEST_LEFT : OPEN_WEST_RIGHT;
            case EAST  -> (hinge == DoorHingeSide.LEFT) ? OPEN_EAST_LEFT : OPEN_EAST_RIGHT;
            default -> OPEN_NORTH_LEFT;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }
        return getShape(state, level, pos, context);
    }

    public void setDoorOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        level.setBlock(pos, state.setValue(OPEN, open), 10);
        BlockPos otherPos = (state.getValue(HALF) == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this) && otherState.getValue(HALF) != state.getValue(HALF)) {
            level.setBlock(otherPos, otherState.setValue(OPEN, open), 10);
        }
    }
}
