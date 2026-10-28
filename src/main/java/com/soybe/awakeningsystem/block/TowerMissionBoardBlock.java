package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.TowerMissionBoardBlockEntity;
import com.soybe.awakeningsystem.tower.TowerDailyMissionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Tablón Físico de Misiones Diario de la Torre (Sección 14.3).
 * Bloque monumental de madera desgastada y pizarra profunda de 5 bloques de largo y 3 de alto.
 * Los cazadores interactúan con él para examinar contratos rotativos diarios.
 */
public class TowerMissionBoardBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<TowerMissionBoardBlock> CODEC = simpleCodec(TowerMissionBoardBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Dimensiones monumentales 5x3 bloques: 5 de ancho (-32 a 48), 3 de alto (0 a 48)
    private static final VoxelShape SHAPE_NORTH = Block.box(-32.0D, 0.0D, 11.5D, 48.0D, 48.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(-32.0D, 0.0D, 0.0D, 48.0D, 48.0D, 4.5D);
    private static final VoxelShape SHAPE_WEST  = Block.box(11.5D, 0.0D, -32.0D, 16.0D, 48.0D, 48.0D);
    private static final VoxelShape SHAPE_EAST  = Block.box(0.0D, 0.0D, -32.0D, 4.5D, 48.0D, 48.0D);

    public TowerMissionBoardBlock(BlockBehaviour.Properties properties) {
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TowerMissionBoardBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(FACING, clickedFace);
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return switch (dir) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer sp) {
            TowerDailyMissionManager.openBoardFor(sp);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.FAIL;
    }
}
