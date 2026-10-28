package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Custom directional block for Teyvat crystal clusters and Mana Crystal Clusters (Rank E to S).
 * Supports multidirectional placement on floors (UP), ceilings (DOWN), and all four walls (NORTH, SOUTH, EAST, WEST).
 * Returning Shapes.empty() for occlusion and visual shape prevents Minecraft from culling
 * adjacent block faces, completely eliminating the X-ray / see-through invisible world bug.
 */
public class TeyvatCrystalBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected static final VoxelShape UP_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    protected static final VoxelShape DOWN_SHAPE = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    protected static final VoxelShape NORTH_SHAPE = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 14.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(2.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public TeyvatCrystalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        switch (direction) {
            case DOWN:
                return DOWN_SHAPE;
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            case EAST:
                return EAST_SHAPE;
            case UP:
            default:
                return UP_SHAPE;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(direction.getOpposite());
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockState targetState = this.defaultBlockState().setValue(FACING, clickedFace);
        if (targetState.canSurvive(level, pos)) {
            return targetState;
        }
        for (Direction dir : context.getNearestLookingDirections()) {
            BlockState altState = this.defaultBlockState().setValue(FACING, dir);
            if (altState.canSurvive(level, pos)) {
                return altState;
            }
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
}
