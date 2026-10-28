package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.TavernSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Pasamanos / asidero colgante (strap handle) para el techo de los vagones del metro de ORV.
 * Cuenta con mecánica interactiva: al hacer clic derecho el jugador se sujeta con una mano,
 * quedando anclado en su lugar sin que las inercias del tren ni empujes de monstruos lo desplacen.
 */
public class SubwayGrabHandleBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<SubwayGrabHandleBlock> CODEC = simpleCodec(SubwayGrabHandleBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(5.0D, 3.0D, 6.0D, 11.0D, 16.0D, 10.0D);
    private static final VoxelShape SHAPE_EAST_WEST   = Block.box(6.0D, 3.0D, 5.0D, 10.0D, 16.0D, 11.0D);

    public SubwayGrabHandleBlock(BlockBehaviour.Properties properties) {
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
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    public static final java.util.Map<java.util.UUID, BlockPos> GRABBED_PLAYERS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            java.util.UUID uuid = player.getUUID();
            if (GRABBED_PLAYERS.containsKey(uuid) && GRABBED_PLAYERS.get(uuid).equals(pos)) {
                GRABBED_PLAYERS.remove(uuid);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
                player.displayClientMessage(Component.translatable("block.awakening_system.subway_handle.released"), true);
                return InteractionResult.CONSUME;
            }

            GRABBED_PLAYERS.put(uuid, pos.immutable());
            player.teleportTo(pos.getX() + 0.5D, player.getY(), pos.getZ() + 0.5D);
            player.setDeltaMovement(0, 0, 0);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.9F, 1.3F);
            player.displayClientMessage(Component.translatable("block.awakening_system.subway_handle.grabbed"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return (dir.getAxis() == Direction.Axis.Z) ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }
}
