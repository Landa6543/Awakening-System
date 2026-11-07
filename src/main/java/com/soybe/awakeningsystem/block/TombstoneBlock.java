package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.ModBlockEntities;
import com.soybe.awakeningsystem.block.entity.TombstoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Bloque Lápida del Sistema (Deadlock Tombstone).
 * Reemplaza la entidad Lápida para proporcionar colisión física sólida (VoxelShape de ~1.9 bloques de altura),
 * evitando que el jugador la atraviese y garantizando estabilidad 100% nativa con GeckoLib 4.
 */
public class TombstoneBlock extends BaseEntityBlock {
    public static final MapCodec<TombstoneBlock> CODEC = simpleCodec(TombstoneBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Altura 30.5 píxeles = ~1.91 bloques (estatura monumental natural)
    private static final VoxelShape SOLID_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 30.5D, 15.0D);

    public TombstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SOLID_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SOLID_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TombstoneBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof TombstoneBlockEntity tombstone) {
            InteractionResult result = tombstone.interact(player);
            return result == InteractionResult.SUCCESS ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TombstoneBlockEntity tombstone) {
            return tombstone.interact(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TombstoneBlockEntity tombstone) {
            boolean isOwner = tombstone.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false);
            boolean isAdmin = player.hasPermissions(2) || player.isCreative();

            if (!isOwner && !isAdmin) {
                String ownerName = tombstone.getOwnerName();
                player.sendSystemMessage(Component.translatable("awakening_system.tombstone.not_owner",
                        ownerName.isEmpty() ? "Desconocido" : ownerName));
                return state;
            }

            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                tombstone.reclaim(serverPlayer);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.TOMBSTONE.get(), TombstoneBlockEntity::tick);
    }
}
