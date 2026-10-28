package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.TavernSeatEntity;
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
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Tronco rústico alargado de tamaño reducido para sentarse alrededor de fogatas.
 * Los jugadores pueden hacer clic derecho para sentarse mirando hacia la fogata.
 */
public class CampfireLogSeatBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CampfireLogSeatBlock> CODEC = simpleCodec(CampfireLogSeatBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_ALONG_X = Block.box(0.0D, 0.0D, 2.0D, 16.0D, 8.0D, 14.0D);
    private static final VoxelShape SHAPE_ALONG_Z = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 8.0D, 16.0D);

    public CampfireLogSeatBlock(BlockBehaviour.Properties properties) {
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
        return (dir.getAxis() == Direction.Axis.Z) ? SHAPE_ALONG_X : SHAPE_ALONG_Z;
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
                sp.displayClientMessage(Component.translatable("awakening_system.campfire_log_seat.occupied"), true);
                return ItemInteractionResult.FAIL;
            }

            TavernSeatEntity seat = ModEntities.TAVERN_SEAT.get().create(sl);
            if (seat != null) {
                Direction dir = state.getValue(FACING);
                seat.setPos(pos.getX() + 0.5D, pos.getY() + 0.50D, pos.getZ() + 0.5D);
                seat.setYRot(dir.toYRot());
                seat.setYHeadRot(dir.toYRot());
                sl.addFreshEntity(seat);
                sp.startRiding(seat, true);
                sl.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7F, 1.1F);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}
