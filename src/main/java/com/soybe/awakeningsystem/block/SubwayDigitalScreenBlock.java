package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pantalla digital suspendida del vagón del metro de ORV (Star Stream / Dokkaebi scenario screen).
 * Emite una luz digital brillante anunciando el Escenario Principal #1.
 * Si se colocan dos pantallas contiguas, se conectan automáticamente en una pantalla panorámica dual.
 */
public class SubwayDigitalScreenBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SubwayDigitalScreenBlock> CODEC = simpleCodec(SubwayDigitalScreenBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ScreenType> TYPE = EnumProperty.create("type", ScreenType.class);

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(1.0D, 6.0D, 6.0D, 15.0D, 16.0D, 10.0D);
    private static final VoxelShape SHAPE_EAST_WEST   = Block.box(6.0D, 6.0D, 1.0D, 10.0D, 16.0D, 15.0D);

    public enum ScreenType implements StringRepresentable {
        SINGLE("single"),
        LEFT("left"),
        RIGHT("right");

        private final String name;
        ScreenType(String name) { this.name = name; }
        @Override
        public String getSerializedName() { return this.name; }
    }

    public SubwayDigitalScreenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ScreenType.SINGLE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }

    private ScreenType computeType(LevelAccessor level, BlockPos pos, Direction facing) {
        Direction rightDir = facing.getClockWise();
        Direction leftDir = facing.getCounterClockWise();

        BlockState leftNeighbor = level.getBlockState(pos.relative(leftDir));
        BlockState rightNeighbor = level.getBlockState(pos.relative(rightDir));

        boolean hasLeft = leftNeighbor.is(this) && leftNeighbor.getValue(FACING) == facing;
        boolean hasRight = rightNeighbor.is(this) && rightNeighbor.getValue(FACING) == facing;

        if (hasRight && !hasLeft) {
            return ScreenType.LEFT;
        } else if (hasLeft && !hasRight) {
            return ScreenType.RIGHT;
        }
        return ScreenType.SINGLE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        ScreenType type = computeType(context.getLevel(), context.getClickedPos(), facing);
        return this.defaultBlockState().setValue(FACING, facing).setValue(TYPE, type);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        ScreenType type = computeType(level, pos, facing);
        return state.setValue(TYPE, type);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return (dir.getAxis() == Direction.Axis.Z) ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }
}
