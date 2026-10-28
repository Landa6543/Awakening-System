package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.block.FruitLeavesBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Árbol Colosal de Murim: Gran Ciruelo Espiritual del Monte Hua (26 a 38 bloques de altura).
 * Tronco 3x3 antiguo, ramas arqueadas en cascada con copiosas flores de ciruelo y ciruelas espirituales cosechables.
 */
public class SuperGiantPlumFeature extends Feature<NoneFeatureConfiguration> {

    public SuperGiantPlumFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.canBeReplaced() || s.is(BlockTags.LEAVES) || s.is(BlockTags.FLOWERS) || s.is(Blocks.SNOW);
    }

    private static void setLog(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (isReplaceable(level, pos) || level.getBlockState(pos).is(BlockTags.DIRT)) {
            level.setBlock(pos, state, 2);
        }
    }

    private static void setPlumLeaves(WorldGenLevel level, BlockPos pos, RandomSource rand) {
        if (isReplaceable(level, pos)) {
            boolean hasFruit = rand.nextInt(4) == 0;
            BlockState leafState = SystemBlocks.SPIRIT_PLUM_LEAVES.get().defaultBlockState()
                    .setValue(FruitLeavesBlock.DISTANCE, 1)
                    .setValue(FruitLeavesBlock.PERSISTENT, true)
                    .setValue(FruitLeavesBlock.HAS_FRUIT, hasFruit);
            level.setBlock(pos, leafState, 2);
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        BlockPos ground = origin.below();
        BlockState gState = level.getBlockState(ground);
        if (!gState.is(BlockTags.DIRT) && !gState.is(Blocks.GRASS_BLOCK)
                && !gState.is(Blocks.NETHERRACK) && !gState.is(Blocks.CRIMSON_NYLIUM) && !gState.is(Blocks.WARPED_NYLIUM)
                && !gState.is(Blocks.SOUL_SOIL) && !gState.is(Blocks.BASALT) && !gState.is(Blocks.BLACKSTONE)) {
            return false;
        }

        for (int y = 1; y <= 20; y++) {
            if (level.getBlockState(origin.above(y)).is(BlockTags.LOGS)) {
                return false;
            }
        }

        int height = 26 + rand.nextInt(12);
        BlockState logY = SystemBlocks.SPIRIT_PLUM_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                for (int y = -2; y <= height; y++) {
                    setLog(level, origin.offset(dx, y, dz), logY);
                }
            }
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int r = 1; r <= 3; r++) {
                BlockPos rPos = origin.offset(1, 0, 1).relative(d, r);
                setLog(level, rPos, logY);
                setLog(level, rPos.below(), logY);
            }
        }

        Direction[] dirs = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        for (Direction d : dirs) {
            int branchLen = 6 + rand.nextInt(4);
            BlockPos bPos = origin.offset(1, height - 5 - rand.nextInt(4), 1);
            BlockState logDir = SystemBlocks.SPIRIT_PLUM_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, d.getAxis());

            for (int step = 1; step <= branchLen; step++) {
                bPos = bPos.relative(d);
                if (step <= branchLen / 2) {
                    bPos = bPos.above();
                } else if (step > branchLen / 2 + 1) {
                    bPos = bPos.below();
                }
                setLog(level, bPos, logDir);

                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        for (int dy = -1; dy <= 2; dy++) {
                            if (dx * dx + dz * dz + dy * dy * 1.5D <= 6.5D) {
                                setPlumLeaves(level, bPos.offset(dx, dy, dz), rand);
                            }
                        }
                    }
                }
            }

            for (int hang = 1; hang <= 3 + rand.nextInt(3); hang++) {
                setPlumLeaves(level, bPos.below(hang), rand);
            }
        }

        BlockPos apex = origin.offset(1, height, 1);
        int domeRadius = 7 + rand.nextInt(2);
        for (int dx = -domeRadius; dx <= domeRadius; dx++) {
            for (int dz = -domeRadius; dz <= domeRadius; dz++) {
                for (int dy = -3; dy <= 4; dy++) {
                    double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 2.0D);
                    if (dist <= domeRadius + 0.5D && rand.nextFloat() < 0.9F) {
                        setPlumLeaves(level, apex.offset(dx, dy, dz), rand);
                    }
                }
            }
        }

        return true;
    }
}
