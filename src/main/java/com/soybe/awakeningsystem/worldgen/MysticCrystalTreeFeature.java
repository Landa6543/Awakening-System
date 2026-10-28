package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MysticCrystalTreeFeature extends Feature<NoneFeatureConfiguration> {

    public MysticCrystalTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        if (level.getLevel().dimension().location().getPath().contains("teyvat")) {
            return false;
        }

        if (origin.getX() * origin.getX() + origin.getZ() * origin.getZ() < 130 * 130) {
            return false;
        }

        BlockPos ground = origin.below();
        BlockState groundState = level.getBlockState(ground);

        boolean isNaturalSoil = groundState.is(BlockTags.DIRT)
                || groundState.is(Blocks.GRASS_BLOCK)
                || groundState.is(Blocks.DIRT)
                || groundState.is(Blocks.COARSE_DIRT)
                || groundState.is(Blocks.PODZOL)
                || groundState.is(Blocks.ROOTED_DIRT)
                || groundState.is(Blocks.MOSS_BLOCK)
                || groundState.is(Blocks.WARPED_NYLIUM);

        if (!isNaturalSoil) return false;

        if (groundState.is(BlockTags.LEAVES)
                || groundState.is(BlockTags.LOGS)
                || groundState.getBlock() instanceof LeavesBlock
                || groundState.is(Blocks.MANGROVE_ROOTS)
                || level.getBlockState(origin).liquid()) {
            return false;
        }

        if (!level.isEmptyBlock(origin) && !level.getBlockState(origin).canBeReplaced()) {
            return false;
        }

        for (int dy = 1; dy <= 16; dy++) {
            BlockState above = level.getBlockState(origin.above(dy));
            if (above.is(BlockTags.LOGS) || above.is(BlockTags.LEAVES)) {
                return false;
            }
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dist = 1; dist <= 3; dist++) {
                BlockPos nearby = origin.relative(d, dist).above(2);
                if (level.getBlockState(nearby).is(BlockTags.LOGS)) {
                    return false;
                }
            }
        }

        int height = 15 + rand.nextInt(10);
        var trunkMat = SystemBlocks.MANA_WOOD_LOG.get().defaultBlockState();
        var foliagePrimary = SystemBlocks.MANA_WOOD_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        var foliageSecondary = SystemBlocks.MANA_WOOD_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        int curX = 0;
        int curZ = 0;
        for (int y = 0; y < height; y++) {
            if (y > 4 && y % 5 == 0) {
                int dirIdx = rand.nextInt(4);
                curX += (dirIdx == 0 ? 1 : (dirIdx == 1 ? -1 : 0));
                curZ += (dirIdx == 2 ? 1 : (dirIdx == 3 ? -1 : 0));
            }
            BlockPos tp = origin.offset(curX, y, curZ);
            level.setBlock(tp, trunkMat, 2);
            if (y < height / 2) {
                level.setBlock(tp.east(), trunkMat, 2);
                level.setBlock(tp.south(), trunkMat, 2);
                level.setBlock(tp.east().south(), trunkMat, 2);
            }
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                for (int dy = -1; dy >= -15; dy--) {
                    BlockPos bp = origin.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(bp);
                    if (bs.isAir() || bs.canBeReplaced() || bs.liquid() || bs.is(BlockTags.FLOWERS) || bs.getBlock() instanceof LeavesBlock) {
                        level.setBlock(bp, trunkMat, 2);
                    } else {
                        break;
                    }
                }
            }
        }

        BlockPos topCenter = origin.offset(curX, height, curZ);
        int canopyR = 4 + rand.nextInt(3);

        for (int dx = -canopyR; dx <= canopyR; dx++) {
            for (int dz = -canopyR; dz <= canopyR; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= canopyR) {
                    for (int dy = -2; dy <= 2; dy++) {
                        if (dist > canopyR - 1 && Math.abs(dy) == 2) continue;
                        BlockPos lp = topCenter.offset(dx, dy, dz);
                        if (level.isEmptyBlock(lp)) {
                            if (dist < 2 && dy == 0 && rand.nextInt(3) == 0) {
                                level.setBlock(lp, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                            } else {
                                level.setBlock(lp, (dx + dz + dy) % 2 == 0 ? foliagePrimary : foliageSecondary, 2);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
