package com.soybe.awakeningsystem.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BuddingAbyssalAmethystBlock extends Block {
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingAbyssalAmethystBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        Block nextBlock = null;

        if (targetState.isAir() || targetState.is(Blocks.WATER)) {
            nextBlock = SystemBlocks.SMALL_ABYSSAL_BUD.get();
        } else if (targetState.is(SystemBlocks.SMALL_ABYSSAL_BUD.get()) && targetState.getValue(AbyssalClusterBlock.FACING) == direction) {
            nextBlock = SystemBlocks.MEDIUM_ABYSSAL_BUD.get();
        } else if (targetState.is(SystemBlocks.MEDIUM_ABYSSAL_BUD.get()) && targetState.getValue(AbyssalClusterBlock.FACING) == direction) {
            nextBlock = SystemBlocks.LARGE_ABYSSAL_BUD.get();
        } else if (targetState.is(SystemBlocks.LARGE_ABYSSAL_BUD.get()) && targetState.getValue(AbyssalClusterBlock.FACING) == direction) {
            nextBlock = SystemBlocks.ABYSSAL_RESONANT_CLUSTER.get();
        }

        if (nextBlock != null) {
            BlockState nextState = nextBlock.defaultBlockState().setValue(AbyssalClusterBlock.FACING, direction);
            level.setBlockAndUpdate(targetPos, nextState);
        }
    }
}
