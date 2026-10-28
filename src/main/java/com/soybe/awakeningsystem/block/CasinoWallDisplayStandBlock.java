package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.block.entity.CasinoWallDisplayStandBlockEntity;
import com.soybe.awakeningsystem.world.TeyvatCasinoBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public class CasinoWallDisplayStandBlock extends BaseEntityBlock {
    public static final MapCodec<CasinoWallDisplayStandBlock> CODEC = simpleCodec(CasinoWallDisplayStandBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected static final VoxelShape SHAPE_NORTH = Block.box(2.0D, 3.0D, 6.0D, 14.0D, 8.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(2.0D, 3.0D, 0.0D, 14.0D, 8.0D, 10.0D);
    protected static final VoxelShape SHAPE_WEST  = Block.box(6.0D, 3.0D, 2.0D, 16.0D, 8.0D, 14.0D);
    protected static final VoxelShape SHAPE_EAST  = Block.box(0.0D, 3.0D, 2.0D, 10.0D, 8.0D, 14.0D);

    public CasinoWallDisplayStandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction dir = state.getValue(FACING);
        BlockPos backPos = pos.relative(dir.getOpposite());
        return level.getBlockState(backPos).isFaceSturdy(level, backPos, dir);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            BlockPos backPos = context.getClickedPos().relative(clickedFace.getOpposite());
            if (context.getLevel().getBlockState(backPos).isFaceSturdy(context.getLevel(), backPos, clickedFace)) {
                return this.defaultBlockState().setValue(FACING, clickedFace);
            }
        }
        for (Direction dir : context.getNearestLookingDirections()) {
            if (dir.getAxis().isHorizontal()) {
                BlockPos backPos = context.getClickedPos().relative(dir.getOpposite());
                if (context.getLevel().getBlockState(backPos).isFaceSturdy(context.getLevel(), backPos, dir)) {
                    return this.defaultBlockState().setValue(FACING, dir);
                }
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CasinoWallDisplayStandBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CasinoWallDisplayStandBlockEntity be) {
            if (be.getDisplayedItem().isEmpty() && !stack.isEmpty()) {
                if (!level.isClientSide()) {
                    ItemStack toDisplay = stack.copyWithCount(1);
                    be.setDisplayedItem(toDisplay);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.1F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.level() != null && TeyvatCasinoBuilder.isInsideCasino(player.level(), pos) && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            return 0.0F; // Vitrina indestructible en supervivencia dentro del Casino
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CasinoWallDisplayStandBlockEntity be) {
            if (!be.getDisplayedItem().isEmpty()) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    boolean isTrophy = be.getDisplayedItem().getItem() instanceof com.soybe.awakeningsystem.item.CasinoTrophyItem;
                    if ((TeyvatCasinoBuilder.isInsideCasino(level, pos) || isTrophy) && !sp.hasPermissions(2) && !sp.getAbilities().instabuild) {
                        sp.displayClientMessage(Component.translatable("awakening_system.block.wall_display_stand.trophy_secured"), true);
                        level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 0.9F);
                        return InteractionResult.FAIL;
                    }

                    ItemStack taken = be.removeDisplayedItem();
                    if (!player.getInventory().add(taken)) {
                        Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, taken);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            if (TeyvatCasinoBuilder.isInsideCasino(level, pos) && !player.getAbilities().instabuild && !player.hasPermissions(2)) {
                return state;
            }
            if (level.getBlockEntity(pos) instanceof CasinoWallDisplayStandBlockEntity be) {
                if (!be.getDisplayedItem().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, be.getDisplayedItem());
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
