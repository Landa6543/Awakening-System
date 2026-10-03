package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Gran portal y estandarte de fisura dimensional decorativa de 3 bloques de altura y 2 bloques de anchura.
 * Permite que los jugadores caminen a través de su centro hacia el vórtice dimensional animado sin colisión.
 */
public class DimensionalRiftStandBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DimensionalRiftStandBlock> CODEC = simpleCodec(DimensionalRiftStandBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<RiftPart> PART = EnumProperty.create("part", RiftPart.class);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public enum RiftPart implements StringRepresentable {
        LOWER_LEFT("lower_left"),
        LOWER_RIGHT("lower_right"),
        MIDDLE_LEFT("middle_left"),
        MIDDLE_RIGHT("middle_right"),
        UPPER_LEFT("upper_left"),
        UPPER_RIGHT("upper_right");

        private final String name;
        RiftPart(String name) { this.name = name; }
        @Override
        public String getSerializedName() { return this.name; }
    }

    public DimensionalRiftStandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, RiftPart.LOWER_LEFT));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction rightDir = facing.getClockWise();
        BlockPos rightPos = pos.relative(rightDir);

        if (pos.getY() < level.getMaxBuildHeight() - 2) {
            boolean leftClear = level.getBlockState(pos.above()).canBeReplaced(context) &&
                                level.getBlockState(pos.above(2)).canBeReplaced(context);
            boolean rightClear = level.getBlockState(rightPos).canBeReplaced(context) &&
                                 level.getBlockState(rightPos.above()).canBeReplaced(context) &&
                                 level.getBlockState(rightPos.above(2)).canBeReplaced(context);
            if (leftClear && rightClear) {
                return this.defaultBlockState().setValue(FACING, facing).setValue(PART, RiftPart.LOWER_LEFT);
            }
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        Direction rightDir = facing.getClockWise();
        BlockPos rightPos = pos.relative(rightDir);

        level.setBlock(pos.above(), this.defaultBlockState().setValue(FACING, facing).setValue(PART, RiftPart.MIDDLE_LEFT), 3);
        level.setBlock(pos.above(2), this.defaultBlockState().setValue(FACING, facing).setValue(PART, RiftPart.UPPER_LEFT), 3);

        level.setBlock(rightPos, this.defaultBlockState().setValue(FACING, facing).setValue(PART, RiftPart.LOWER_RIGHT), 3);
        level.setBlock(rightPos.above(), this.defaultBlockState().setValue(FACING, facing).setValue(PART, RiftPart.MIDDLE_RIGHT), 3);
        level.setBlock(rightPos.above(2), this.defaultBlockState().setValue(FACING, facing).setValue(PART, RiftPart.UPPER_RIGHT), 3);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            Direction rightDir = facing.getClockWise();
            RiftPart part = state.getValue(PART);

            int yOffset = switch (part) {
                case LOWER_LEFT, LOWER_RIGHT -> 0;
                case MIDDLE_LEFT, MIDDLE_RIGHT -> 1;
                case UPPER_LEFT, UPPER_RIGHT -> 2;
            };
            boolean isRight = switch (part) {
                case LOWER_RIGHT, MIDDLE_RIGHT, UPPER_RIGHT -> true;
                default -> false;
            };

            BlockPos lowerLeft = pos.below(yOffset);
            if (isRight) {
                lowerLeft = lowerLeft.relative(rightDir.getOpposite());
            }
            BlockPos lowerRight = lowerLeft.relative(rightDir);

            for (int y = 0; y < 3; y++) {
                BlockPos pLeft = lowerLeft.above(y);
                BlockPos pRight = lowerRight.above(y);
                if (level.getBlockState(pLeft).is(this)) level.destroyBlock(pLeft, false);
                if (level.getBlockState(pRight).is(this)) level.destroyBlock(pRight, false);
            }

            if (!player.isCreative()) {
                popResource(level, pos, new ItemStack(this));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Sin colisión sólida para permitir el paso a través como portal decorativo
        return Shapes.empty();
    }
}
