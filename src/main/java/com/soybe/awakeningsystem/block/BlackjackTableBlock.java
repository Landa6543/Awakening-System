package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.BlackjackTableBlockEntity;
import com.soybe.awakeningsystem.casino.BlackjackServerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlackjackTableBlock extends BaseEntityBlock {
    public static final MapCodec<BlackjackTableBlock> CODEC = simpleCodec(BlackjackTableBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BlackjackPart> PART = EnumProperty.create("part", BlackjackPart.class);

    private static final VoxelShape TABLE_SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 12, 15),
            Block.box(0, 12, 0, 16, 16, 16)
    );

    public BlackjackTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BlackjackPart.FRONT_LEFT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TABLE_SHAPE;
    }

    public BlockPos getMasterPos(BlockState state, BlockPos pos) {
        BlackjackPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        return part.getMasterPos(pos, facing);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            BlockPos masterPos = getMasterPos(state, pos);
            BlackjackServerHandler.openTable(sp, masterPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            BlockPos masterPos = getMasterPos(state, pos);
            BlackjackServerHandler.openTable(sp, masterPos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        for (BlackjackPart part : BlackjackPart.values()) {
            BlockPos partPos = BlackjackPart.getPartPos(pos, part, facing);
            if (!level.getBlockState(partPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(partPos)) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, BlackjackPart.FRONT_LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);

            for (BlackjackPart part : BlackjackPart.values()) {
                if (part == BlackjackPart.FRONT_LEFT) continue;
                BlockPos partPos = BlackjackPart.getPartPos(pos, part, facing);
                level.setBlock(partPos, state.setValue(PART, part), 3);
                level.blockUpdated(partPos, Blocks.AIR);
            }
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos masterPos = getMasterPos(state, pos);
            Direction facing = state.getValue(FACING);

            for (BlackjackPart part : BlackjackPart.values()) {
                BlockPos partPos = BlackjackPart.getPartPos(masterPos, part, facing);
                if (!partPos.equals(pos)) {
                    BlockState s = level.getBlockState(partPos);
                    if (s.is(this)) {
                        level.setBlock(partPos, Blocks.AIR.defaultBlockState(), 35);
                        level.levelEvent(player, 2001, partPos, Block.getId(s));
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == BlackjackPart.FRONT_LEFT) {
            return new BlackjackTableBlockEntity(pos, state);
        }
        return null;
    }
}
