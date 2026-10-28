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
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Gran Árbol Colosal del Mundo / Arboleda Ancestral de Isekai (35 a 52 bloques de altura).
 * Tronco 3x3/4x4, inmensas raíces vivientes y un dosel arbóreo resplandeciente
 * cargado de Hojas de Maná, Frutos de Maná y hojas de Eldoria.
 */
public class SuperGiantWorldTreeFeature extends Feature<NoneFeatureConfiguration> {

    public SuperGiantWorldTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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

    private static void setManaLeaves(WorldGenLevel level, BlockPos pos, RandomSource rand) {
        if (isReplaceable(level, pos)) {
            boolean isEldoria = rand.nextInt(3) == 0;
            BlockState leafState;
            if (isEldoria) {
                leafState = SystemBlocks.ELDORIA_LEAVES.get().defaultBlockState()
                        .setValue(LeavesBlock.DISTANCE, 1)
                        .setValue(LeavesBlock.PERSISTENT, true);
            } else {
                boolean hasFruit = rand.nextInt(5) == 0;
                leafState = SystemBlocks.MANA_WOOD_LEAVES.get().defaultBlockState()
                        .setValue(FruitLeavesBlock.DISTANCE, 1)
                        .setValue(FruitLeavesBlock.PERSISTENT, true)
                        .setValue(FruitLeavesBlock.HAS_FRUIT, hasFruit);
            }
            level.setBlock(pos, leafState, 2);
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        if (level.getLevel().dimension().location().getPath().contains("teyvat")) {
            return false;
        }

        BlockPos ground = origin.below();
        BlockState gState = level.getBlockState(ground);
        if (!gState.is(BlockTags.DIRT) && !gState.is(Blocks.GRASS_BLOCK) && !gState.is(Blocks.MOSS_BLOCK)) {
            return false;
        }

        int height = 36 + rand.nextInt(15);
        BlockState logY = SystemBlocks.MANA_WOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                for (int y = -3; y <= height; y++) {
                    setLog(level, origin.offset(dx, y, dz), logY);
                }
            }
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dist = 2; dist <= 5; dist++) {
                BlockPos rPos = origin.offset(1, 0, 1).relative(d, dist);
                for (int y = -2; y <= 5 - dist; y++) {
                    setLog(level, rPos.above(y), logY);
                }
            }
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            int branchLen = 6 + rand.nextInt(4);
            int branchStart = height - 8 - rand.nextInt(6);
            BlockPos bPos = origin.offset(1, branchStart, 1);
            BlockState logAxis = SystemBlocks.MANA_WOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, d.getAxis());

            for (int step = 1; step <= branchLen; step++) {
                bPos = bPos.relative(d).above(step % 2 == 0 ? 1 : 0);
                setLog(level, bPos, logAxis);
                setLog(level, bPos.below(), logAxis);

                if (step >= branchLen - 2) {
                    int r = 3 + rand.nextInt(2);
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dz = -r; dz <= r; dz++) {
                            for (int dy = -2; dy <= 2; dy++) {
                                if (dx * dx + dz * dz + dy * dy <= r * r + 1) {
                                    setManaLeaves(level, bPos.offset(dx, dy, dz), rand);
                                }
                            }
                        }
                    }
                }
            }

            if (rand.nextFloat() < 0.85F) {
                BlockPos underB = bPos.below(2);
                if (level.isEmptyBlock(underB)) {
                    level.setBlock(underB, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                }
            }
        }

        BlockPos apex = origin.offset(1, height, 1);
        int crownRadius = 8 + rand.nextInt(3);
        for (int dx = -crownRadius; dx <= crownRadius; dx++) {
            for (int dz = -crownRadius; dz <= crownRadius; dz++) {
                for (int dy = -4; dy <= 5; dy++) {
                    double dist = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.8D);
                    if (dist <= crownRadius + 0.5D && rand.nextFloat() < 0.9F) {
                        setManaLeaves(level, apex.offset(dx, dy, dz), rand);
                    }
                }
            }
        }

        return true;
    }
}
