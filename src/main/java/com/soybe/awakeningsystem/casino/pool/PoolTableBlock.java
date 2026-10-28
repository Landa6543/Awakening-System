package com.soybe.awakeningsystem.casino.pool;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

public class PoolTableBlock extends BaseEntityBlock {
    public static final MapCodec<PoolTableBlock> CODEC = simpleCodec(PoolTableBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<PoolTablePart> PART = EnumProperty.create("part", PoolTablePart.class);

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 12, 15),
            Block.box(0, 10, 0, 16, 16, 16)
    );

    protected final boolean illegal;

    public PoolTableBlock(BlockBehaviour.Properties properties) {
        this(false, properties);
    }

    public PoolTableBlock(boolean illegal, BlockBehaviour.Properties properties) {
        super(properties);
        this.illegal = illegal;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, PoolTablePart.CORNER_NW));
    }

    public boolean isIllegal() {
        return illegal;
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
        return BASE_SHAPE;
    }

    public BlockPos getMasterPos(BlockState state, BlockPos pos) {
        PoolTablePart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        return part.getMasterPos(pos, facing);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            BlockPos masterPos = getMasterPos(state, pos);
            PoolTableServerHandler.openTable(sp, masterPos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            BlockPos masterPos = getMasterPos(state, pos);
            PoolTableServerHandler.openTable(sp, masterPos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction right = facing.getClockWise();

        for (PoolTablePart part : PoolTablePart.values()) {
            BlockPos partPos = pos.relative(right, part.getOffsetX()).relative(facing, part.getOffsetZ());
            if (!level.getBlockState(partPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(partPos)) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, PoolTablePart.CORNER_NW);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            Direction right = facing.getClockWise();

            for (PoolTablePart part : PoolTablePart.values()) {
                if (part == PoolTablePart.CORNER_NW) continue;
                BlockPos partPos = pos.relative(right, part.getOffsetX()).relative(facing, part.getOffsetZ());
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
            Direction right = facing.getClockWise();

            for (PoolTablePart part : PoolTablePart.values()) {
                BlockPos partPos = masterPos.relative(right, part.getOffsetX()).relative(facing, part.getOffsetZ());
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
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == PoolTablePart.CORNER_NW) {
            return new PoolTableBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) == PoolTablePart.CORNER_NW) {
            return createTickerHelper(blockEntityType, ModBlockEntities.POOL_TABLE.get(), PoolTableBlockEntity::tick);
        }
        return null;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        if (player.level() != null) {
            boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(player.level(), pos)
                    || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(player.level(), pos);
            if (isProtected && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
                return 0.0F;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);
        if (!level.isClientSide() && isProtected && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable("awakening_system.block.pool_table.protected"), true);
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        boolean isProtected = com.soybe.awakeningsystem.world.SeriesStructureBuilder.isInsideBlackMarketDen(level, pos)
                || com.soybe.awakeningsystem.world.TeyvatCasinoBuilder.isInsideCasino(level, pos);
        if (isProtected && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
