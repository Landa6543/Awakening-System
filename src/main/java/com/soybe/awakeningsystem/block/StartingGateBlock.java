package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Puerta de Salida de Hipódromo (Starting Gate / Starting Box Stall).
 * Compuerta mecánica articulada de 2 a 3 bloques de altura oficial de hipódromo.
 * Estructura de dos bloques (LOWER y UPPER) con cancelas de salida que se abren sincronizadas.
 */
public class StartingGateBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<StartingGateBlock> CODEC = simpleCodec(StartingGateBlock::new);

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape EW_POSTS_LOWER = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D),
            Block.box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D)
    );
    private static final VoxelShape EW_POSTS_UPPER = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 24.0D, 2.0D),
            Block.box(0.0D, 0.0D, 14.0D, 16.0D, 24.0D, 16.0D),
            Block.box(0.0D, 16.0D, 0.0D, 16.0D, 24.0D, 16.0D)
    );
    private static final VoxelShape NS_POSTS_LOWER = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D),
            Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)
    );
    private static final VoxelShape NS_POSTS_UPPER = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 2.0D, 24.0D, 16.0D),
            Block.box(14.0D, 0.0D, 0.0D, 16.0D, 24.0D, 16.0D),
            Block.box(0.0D, 16.0D, 0.0D, 16.0D, 24.0D, 16.0D)
    );

    private static final VoxelShape DOOR_EAST = Block.box(12.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);
    private static final VoxelShape DOOR_WEST = Block.box(0.0D, 0.0D, 2.0D, 4.0D, 16.0D, 14.0D);
    private static final VoxelShape DOOR_SOUTH = Block.box(2.0D, 0.0D, 12.0D, 14.0D, 16.0D, 16.0D);
    private static final VoxelShape DOOR_NORTH = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 4.0D);

    public static VoxelShape getGateShape(Direction facing, boolean open, DoubleBlockHalf half) {
        boolean isEW = facing.getAxis() == Direction.Axis.X;
        VoxelShape base = half == DoubleBlockHalf.LOWER ? (isEW ? EW_POSTS_LOWER : NS_POSTS_LOWER) : (isEW ? EW_POSTS_UPPER : NS_POSTS_UPPER);
        if (open) {
            return base;
        }
        VoxelShape door = switch (facing) {
            case EAST -> DOOR_EAST;
            case WEST -> DOOR_WEST;
            case SOUTH -> DOOR_SOUTH;
            case NORTH -> DOOR_NORTH;
            default -> Shapes.empty();
        };
        return Shapes.or(base, door);
    }

    public StartingGateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(OPEN, false)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, OPEN, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(OPEN, powered)
                    .setValue(POWERED, powered);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getGateShape(state.getValue(FACING), state.getValue(OPEN), state.getValue(HALF));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getGateShape(state.getValue(FACING), state.getValue(OPEN), state.getValue(HALF));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return handleGateToggle(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                              Player player, BlockHitResult hitResult) {
        ItemInteractionResult res = handleGateToggle(state, level, pos);
        return res == ItemInteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private ItemInteractionResult handleGateToggle(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        boolean newOpen = !state.getValue(OPEN);
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos siblingPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState siblingState = level.getBlockState(siblingPos);

        level.setBlock(pos, state.setValue(OPEN, newOpen), 3);
        if (siblingState.is(this) && siblingState.getValue(HALF) != half) {
            level.setBlock(siblingPos, siblingState.setValue(OPEN, newOpen), 3);
        }

        level.playSound(null, pos, newOpen ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS, 1.0F, newOpen ? 1.2F : 0.9F);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos siblingPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState siblingState = level.getBlockState(siblingPos);

        if (siblingState.is(this) && siblingState.getValue(HALF) != half) {
            level.destroyBlock(siblingPos, !player.isCreative());
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP && (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER)) {
                return Blocks.AIR.defaultBlockState();
            }
            if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN && (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos lowerPos = half == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockPos upperPos = half == DoubleBlockHalf.UPPER ? pos : pos.above();

            boolean hasSignal = level.hasNeighborSignal(lowerPos) || level.hasNeighborSignal(upperPos);
            if (state.getValue(POWERED) != hasSignal) {
                level.setBlock(pos, state.setValue(POWERED, hasSignal).setValue(OPEN, hasSignal), 3);

                BlockPos siblingPos = half == DoubleBlockHalf.LOWER ? upperPos : lowerPos;
                BlockState siblingState = level.getBlockState(siblingPos);
                if (siblingState.is(this)) {
                    level.setBlock(siblingPos, siblingState.setValue(POWERED, hasSignal).setValue(OPEN, hasSignal), 3);
                }

                level.playSound(null, pos, hasSignal ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                        SoundSource.BLOCKS, 1.0F, hasSignal ? 1.2F : 0.9F);
            }
        }
    }
}
