package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.casino.network.OpenSlotMachinePayload;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.entity.SlotMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SlotMachineBlock extends BaseEntityBlock {
    public static final MapCodec<SlotMachineBlock> CODEC = simpleCodec(SlotMachineBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape BOTTOM_PART = Block.box(1, 0, 2, 15, 14, 15);
    private static final VoxelShape MIDDLE_PART = Block.box(1, 14, 1, 15, 16, 15);
    private static final VoxelShape UPPER_PART = Block.box(1, 0, 5, 15, 16, 15);
    private static final VoxelShape LEFT_PART = Block.box(14, 0, 1, 15, 11, 5);
    private static final VoxelShape RIGHT_PART = Block.box(1, 0, 1, 2, 11, 5);

    private static final VoxelShape LOWER_SHAPE = Shapes.or(BOTTOM_PART, MIDDLE_PART);
    private static final VoxelShape UPPER_SHAPE = Shapes.or(UPPER_PART, LEFT_PART, RIGHT_PART);

    private static final Map<Direction, VoxelShape> LOWER_SHAPES_BY_DIRECTION = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> UPPER_SHAPES_BY_DIRECTION = new EnumMap<>(Direction.class);

    static {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            LOWER_SHAPES_BY_DIRECTION.put(dir, rotateShape(dir, LOWER_SHAPE));
            UPPER_SHAPES_BY_DIRECTION.put(dir, rotateShape(dir, UPPER_SHAPE));
        }
    }

    public SlotMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
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
        Direction facing = state.getValue(FACING);
        DoubleBlockHalf half = state.getValue(HALF);
        return half == DoubleBlockHalf.LOWER
                ? LOWER_SHAPES_BY_DIRECTION.getOrDefault(facing, LOWER_SHAPE)
                : UPPER_SHAPES_BY_DIRECTION.getOrDefault(facing, UPPER_SHAPE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return handleInteraction(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemInteractionResult result = handleInteraction(state, level, pos, player);
        return result == ItemInteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private ItemInteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lowerPos = pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);
            if (lowerState.is(this)) {
                return handleInteraction(lowerState, level, lowerPos, player);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SlotMachineBlockEntity slotEntity) {
                if (slotEntity.isInUse() && !slotEntity.isUsedBy(sp)) {
                    sp.sendSystemMessage(Component.literal("§cEsta Tragamonedas Celestial está ocupada por otro jugador."), true);
                    return ItemInteractionResult.CONSUME;
                }

                if (!slotEntity.tryLock(sp)) {
                    return ItemInteractionResult.CONSUME;
                }

                SystemData data = SystemEvents.data(sp);
                long moraBalance = data.mora();
                PacketDistributor.sendToPlayer(sp, new OpenSlotMachinePayload(pos, moraBalance, 1000, 1));
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.is(this) && otherState.getValue(HALF) != half) {
            level.destroyBlock(otherPos, !player.isCreative());
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new SlotMachineBlockEntity(pos, state) : null;
    }

    private static VoxelShape rotateShape(Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }
}
