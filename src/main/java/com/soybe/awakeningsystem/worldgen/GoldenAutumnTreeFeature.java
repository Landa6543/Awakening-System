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

public class GoldenAutumnTreeFeature extends Feature<NoneFeatureConfiguration> {

    public GoldenAutumnTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource rand = context.random();

        // 0. EXCLUSIÓN DE TEYVAT: Los árboles canónicos de Teyvat se generan proceduralmente
        if (level.getLevel().dimension().location().getPath().contains("teyvat")) {
            return false;
        }

        if (level.getLevel().getServer() != null) {
            var sData = com.soybe.awakeningsystem.world.SeriesStructureWorldGen.SeriesStructureData.get(level.getLevel().getServer());
            if (sData.hasStructureNearby(origin, 45.0D)) {
                return false;
            }
        }

        // EXCLUSIÓN DEL SANTUARIO: Nunca generarse cerca del Gran Árbol del Mundo
        if (origin.getX() * origin.getX() + origin.getZ() * origin.getZ() < 130 * 130) {
            return false;
        }

        BlockPos ground = origin.below();
        BlockState groundState = level.getBlockState(ground);

        // 1. Validación estricta de suelo natural
        boolean isNaturalSoil = groundState.is(BlockTags.DIRT)
                || groundState.is(Blocks.GRASS_BLOCK)
                || groundState.is(Blocks.DIRT)
                || groundState.is(Blocks.COARSE_DIRT)
                || groundState.is(Blocks.PODZOL)
                || groundState.is(Blocks.ROOTED_DIRT)
                || groundState.is(Blocks.MOSS_BLOCK);

        if (!isNaturalSoil) return false;

        // 2. NUNCA sobre hojas, madera, raíces ni líquidos
        if (groundState.is(BlockTags.LEAVES)
                || groundState.is(BlockTags.LOGS)
                || groundState.getBlock() instanceof LeavesBlock
                || groundState.is(Blocks.MANGROVE_ROOTS)
                || level.getBlockState(origin).liquid()) {
            return false;
        }

        // 3. Origen libre
        if (!level.isEmptyBlock(origin) && !level.getBlockState(origin).canBeReplaced()) {
            return false;
        }

        // 4. Despeje vertical de 16 bloques hacia arriba
        for (int dy = 1; dy <= 16; dy++) {
            BlockState above = level.getBlockState(origin.above(dy));
            if (above.is(BlockTags.LOGS) || above.is(BlockTags.LEAVES)) {
                return false;
            }
        }

        // 5. Despeje horizontal
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dist = 1; dist <= 3; dist++) {
                BlockPos nearby = origin.relative(d, dist).above(2);
                if (level.getBlockState(nearby).is(BlockTags.LOGS)) {
                    return false;
                }
            }
        }

        int height = 14 + rand.nextInt(8); // 14 a 21 bloques
        var trunkMat = SystemBlocks.ELDORIA_LOG.get().defaultBlockState();
        var leavesPrimary = SystemBlocks.ELDORIA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        var leavesSecondary = SystemBlocks.ELDORIA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        // Tronco robusto 2x2 anclado hacia abajo
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                for (int y = 0; y < height; y++) {
                    level.setBlock(origin.offset(dx, y, dz), trunkMat, 2);
                }
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

        // Ramas diagonales amplias
        int branchY = height - 4;
        int[][] branchDirs = {{2, 1, 0}, {-2, 1, 0}, {0, 1, 2}, {0, 1, -2}, {2, 2, 2}, {-2, 2, -2}, {-2, 2, 2}, {2, 2, -2}};
        for (int[] bd : branchDirs) {
            level.setBlock(origin.offset(bd[0], branchY + bd[1], bd[2]), trunkMat, 2);
        }

        // Gran copa dorada ovalada
        BlockPos canopyCenter = origin.offset(0, height, 0);
        int r = 5 + rand.nextInt(2);

        for (int dx = -r; dx <= r + 1; dx++) {
            for (int dz = -r; dz <= r + 1; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= r) {
                    for (int dy = -3; dy <= 2; dy++) {
                        if (dist > r - 1 && Math.abs(dy) == 2) continue;
                        if (dist > r - 2 && dy == -3) continue;

                        BlockPos lp = canopyCenter.offset(dx, dy, dz);
                        if (level.isEmptyBlock(lp)) {
                            if (dist < 2.5 && dy == 0 && rand.nextFloat() < 0.25F) {
                                level.setBlock(lp, Blocks.SHROOMLIGHT.defaultBlockState(), 2);
                            } else {
                                level.setBlock(lp, rand.nextBoolean() ? leavesPrimary : leavesSecondary, 2);
                            }
                        }
                    }

                    // Faroles dorados colgando
                    if (dist > 2.0 && dist < r - 1.0 && rand.nextFloat() < 0.08F) {
                        BlockPos hang = canopyCenter.offset(dx, -4, dz);
                        if (level.isEmptyBlock(hang) && level.isEmptyBlock(hang.below())) {
                            level.setBlock(hang, Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y), 2);
                            level.setBlock(hang.below(), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                        }
                    }
                }
            }
        }

        return true;
    }
}
