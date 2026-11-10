package com.soybe.awakeningsystem.block;

import com.mojang.serialization.MapCodec;
import com.soybe.awakeningsystem.tower.AltarOfOrigin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The crystal cube of the Altar of Origin. Right-click it to have your potential measured.
 * RANK 0 is the dormant cube; 1..7 are F..S lit in their colour; 8 is EX, the cube cracked open.
 */
public class PotentialCrystalBlock extends Block {
    public static final MapCodec<PotentialCrystalBlock> CODEC = simpleCodec(PotentialCrystalBlock::new);
    public static final IntegerProperty RANK = IntegerProperty.create("rank", 0, 8);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 24.0D, 14.0D);

    /** Tint for each RANK value: dormant, F, E, D, C, B, A, S, EX. */
    public static final int[] COLORS = {0x7FD8FF,0x9A9A9A, 0x6FE3FF, 0x3D7BFF, 0x3CE070, 0xB050FF, 0xFF3A4A, 0xFFC93A, 0xFFFFFF};

    public PotentialCrystalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RANK, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RANK);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) AltarOfOrigin.touch(sp, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
