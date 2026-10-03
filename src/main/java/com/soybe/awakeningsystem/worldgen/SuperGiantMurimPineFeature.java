package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
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
 * Árbol Colosal de Murim: Pino Celestial Milenario del Dao (32 a 46 bloques de altura).
 * Tronco masivo 3x3 con contrafuertes 4x4, ramas en voladizo tipo Shanshui
 * y plataformas horizontales escalonadas de acículas de pino eterno.
 */
public class SuperGiantMurimPineFeature extends Feature<NoneFeatureConfiguration> {

    public SuperGiantMurimPineFeature(Codec<NoneFeatureConfiguration> codec) {
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

    private static void setLeaves(WorldGenLevel level, BlockPos pos) {
        if (isReplaceable(level, pos)) {
            BlockState leafState = SystemBlocks.CELESTIAL_PINE_LEAVES.get().defaultBlockState()
                    .setValue(LeavesBlock.DISTANCE, 1)
                    .setValue(LeavesBlock.PERSISTENT, true);
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
        if (!gState.is(BlockTags.DIRT) && !gState.is(Blocks.STONE) && !gState.is(Blocks.GRASS_BLOCK)
                && !gState.is(Blocks.NETHERRACK) && !gState.is(Blocks.CRIMSON_NYLIUM) && !gState.is(Blocks.WARPED_NYLIUM)
                && !gState.is(Blocks.SOUL_SOIL) && !gState.is(Blocks.BASALT) && !gState.is(Blocks.BLACKSTONE)) {
            return false;
        }

        // Espacio vertical de 25 bloques libres
        for (int y = 1; y <= 25; y++) {
            if (level.getBlockState(origin.above(y)).is(BlockTags.LOGS)) {
                return false;
            }
        }

        int height = 32 + rand.nextInt(15); // 32 a 46 bloques de altura
        BlockState logY = SystemBlocks.CELESTIAL_PINE_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        // 1. Contrafuerte de raíces 4x4 en la base
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                for (int y = -2; y <= 3; y++) {
                    setLog(level, origin.offset(dx, y, dz), logY);
                }
            }
        }

        // Raíces contrafuerte que descienden en cruz
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dist = 2; dist <= 4; dist++) {
                BlockPos rPos = origin.relative(d, dist);
                for (int y = -2; y <= 4 - dist; y++) {
                    setLog(level, rPos.above(y), logY);
                }
            }
        }

        // 2. Tronco masivo 3x3 ascendente con suave inclinación milenaria
        Direction mainLean = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
        BlockPos currentBase = origin;
        for (int y = 3; y < height; y++) {
            for (int dx = 0; dx <= 2; dx++) {
                for (int dz = 0; dz <= 2; dz++) {
                    setLog(level, currentBase.offset(dx, y, dz), logY);
                }
            }

            // Inclinación suave cada 6 bloques
            if (y > 10 && y % 7 == 0) {
                currentBase = currentBase.relative(mainLean);
            }
        }

        // 3. Ramas colosales en voladizo (estilo pintura tradicional china Shanshui)
        int boughCount = 6 + rand.nextInt(4);
        for (int b = 0; b < boughCount; b++) {
            int boughY = 12 + (b * 4) + rand.nextInt(3);
            if (boughY >= height - 3) break;

            Direction dir = Direction.from2DDataValue((b * 2 + rand.nextInt(2)) % 4);
            int length = 5 + rand.nextInt(5);
            BlockPos bStart = currentBase.offset(1, boughY, 1);
            BlockState logAxis = SystemBlocks.CELESTIAL_PINE_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, dir.getAxis());

            BlockPos bCurr = bStart;
            for (int step = 1; step <= length; step++) {
                bCurr = bCurr.relative(dir);
                if (step % 3 == 0) bCurr = bCurr.above();
                setLog(level, bCurr, logAxis);
                setLog(level, bCurr.below(), logAxis); // Grosor de 2 bloques
            }

            // Almohadilla horizontal aplanada de hojas de pino en el extremo
            int padRadius = 3 + rand.nextInt(2);
            for (int dx = -padRadius; dx <= padRadius; dx++) {
                for (int dz = -padRadius; dz <= padRadius; dz++) {
                    if (dx * dx + dz * dz <= padRadius * padRadius + 2) {
                        setLeaves(level, bCurr.offset(dx, 0, dz));
                        setLeaves(level, bCurr.offset(dx, 1, dz));
                        if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                            setLeaves(level, bCurr.offset(dx, 2, dz));
                        }
                    }
                }
            }

            // Farol celestial o piedra espiritual colgando debajo de la rama
            if (rand.nextFloat() < 0.7F) {
                BlockPos underBough = bCurr.below(2);
                if (level.isEmptyBlock(underBough)) {
                    level.setBlock(underBough, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                }
            }
        }

        // 4. Gran corona aplanada superior
        BlockPos crownCenter = currentBase.offset(1, height, 1);
        int topRadius = 6 + rand.nextInt(2);
        for (int dx = -topRadius; dx <= topRadius; dx++) {
            for (int dz = -topRadius; dz <= topRadius; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq <= topRadius * topRadius) {
                    setLeaves(level, crownCenter.offset(dx, 0, dz));
                    setLeaves(level, crownCenter.offset(dx, 1, dz));
                    if (distSq <= (topRadius - 2) * (topRadius - 2)) {
                        setLeaves(level, crownCenter.offset(dx, 2, dz));
                    }
                }
            }
        }

        return true;
    }
}
