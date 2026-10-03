package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.TavernSeatEntity;
import com.soybe.awakeningsystem.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class TavernChairBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TavernChairBlock> CODEC = simpleCodec(TavernChairBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SEAT_BASE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    private static final VoxelShape BACK_NORTH = Shapes.or(SEAT_BASE, Block.box(2.0D, 8.0D, 12.0D, 14.0D, 18.0D, 14.0D));
    private static final VoxelShape BACK_SOUTH = Shapes.or(SEAT_BASE, Block.box(2.0D, 8.0D, 2.0D, 14.0D, 18.0D, 4.0D));
    private static final VoxelShape BACK_WEST  = Shapes.or(SEAT_BASE, Block.box(12.0D, 8.0D, 2.0D, 14.0D, 18.0D, 14.0D));
    private static final VoxelShape BACK_EAST  = Shapes.or(SEAT_BASE, Block.box(2.0D, 8.0D, 2.0D, 4.0D, 18.0D, 14.0D));

    public TavernChairBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return switch (dir) {
            case SOUTH -> BACK_SOUTH;
            case WEST -> BACK_WEST;
            case EAST -> BACK_EAST;
            default -> BACK_NORTH;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            AABB searchBox = new AABB(pos);
            List<TavernSeatEntity> existing = sl.getEntitiesOfClass(TavernSeatEntity.class, searchBox);
            if (!existing.isEmpty()) {
                sp.displayClientMessage(Component.translatable("awakening_system.tavern_chair.occupied"), true);
                return ItemInteractionResult.FAIL;
            }

            TavernSeatEntity seat = ModEntities.TAVERN_SEAT.get().create(sl);
            if (seat != null) {
                seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.35D, pos.getZ() + 0.5D);
                sl.addFreshEntity(seat);
                sp.startRiding(seat, true);
                sl.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.2F);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}