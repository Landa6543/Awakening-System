package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generador procedural de árboles gigantes de fantasía.
 * Árboles colosales de estilo sombrilla o ramificado con contrafuertes y alfombra floral mística,
 * sin cadenas ni faroles colgantes artificiales.
 */
public class GiantFantasyTreeFeature extends Feature<NoneFeatureConfiguration> {

    public GiantFantasyTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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

        boolean isNaturalSoil = groundState.is(net.minecraft.tags.BlockTags.DIRT)
                || groundState.is(Blocks.GRASS_BLOCK)
                || groundState.is(Blocks.DIRT)
                || groundState.is(Blocks.COARSE_DIRT)
                || groundState.is(Blocks.PODZOL)
                || groundState.is(Blocks.ROOTED_DIRT)
                || groundState.is(Blocks.MOSS_BLOCK);

        if (!isNaturalSoil) {
            return false;
        }

        if (groundState.is(net.minecraft.tags.BlockTags.LEAVES)
                || groundState.is(net.minecraft.tags.BlockTags.LOGS)
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
            if (above.is(net.minecraft.tags.BlockTags.LOGS) || above.is(net.minecraft.tags.BlockTags.LEAVES)) {
                return false;
            }
        }

        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dist = 1; dist <= 4; dist++) {
                BlockPos nearby = origin.relative(d, dist).above(2);
                if (level.getBlockState(nearby).is(net.minecraft.tags.BlockTags.LOGS)) {
                    return false;
                }
            }
        }

        int height = 18 + rand.nextInt(12);
        boolean isUmbrellaStyle = rand.nextBoolean();

        var trunkMat = Blocks.DARK_OAK_WOOD.defaultBlockState();

        int leafVariant = rand.nextInt(3);
        var leafPrimary = switch (leafVariant) {
            case 0 -> Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            case 1 -> Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            default -> Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        };
        var leafSecondary = switch (leafVariant) {
            case 0 -> Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            case 1 -> Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            default -> Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        };

        int trunkW = (height > 22) ? 3 : 2;
        for (int dx = 0; dx < trunkW; dx++) {
            for (int dz = 0; dz < trunkW; dz++) {
                for (int y = 0; y < height; y++) {
                    level.setBlock(origin.offset(dx, y, dz), trunkMat, 2);
                }
                for (int dy = -1; dy >= -15; dy--) {
                    BlockPos bp = origin.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(bp);
                    if (bs.isAir() || bs.canBeReplaced() || bs.liquid() || bs.is(net.minecraft.tags.BlockTags.FLOWERS) || bs.getBlock() instanceof LeavesBlock) {
                        level.setBlock(bp, trunkMat, 2);
                    } else {
                        break;
                    }
                }
            }
        }

        if (isUmbrellaStyle) {
            int canopyRadius = 8 + rand.nextInt(3);
            int branchStartY = height - 4;
            int branchCount = 6 + rand.nextInt(3);
            for (int b = 0; b < branchCount; b++) {
                double rad = (b * 2.0 * Math.PI) / branchCount;
                for (int step = 1; step <= canopyRadius - 2; step++) {
                    int bx = (int) Math.round(Math.cos(rad) * step);
                    int bz = (int) Math.round(Math.sin(rad) * step);
                    int by = branchStartY + (int) (step * 0.25);
                    level.setBlock(origin.offset(bx, by, bz), trunkMat, 2);
                }
            }

            for (int dx = -canopyRadius; dx <= canopyRadius + trunkW; dx++) {
                for (int dz = -canopyRadius; dz <= canopyRadius + trunkW; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= canopyRadius) {
                        for (int dy = -1; dy <= 2; dy++) {
                            if (dy == 2 && dist > canopyRadius - 2) continue;
                            if (dy == -1 && dist > canopyRadius - 1) continue;

                            BlockPos lp = origin.offset(dx, height + dy, dz);
                            if (level.isEmptyBlock(lp)) {
                                level.setBlock(lp, rand.nextBoolean() ? leafPrimary : leafSecondary, 2);
                            }
                        }
                    }
                }
            }
        } else {
            int branchCount = 4 + rand.nextInt(3);
            for (int b = 0; b < branchCount; b++) {
                double angle = (b * 2.0 * Math.PI) / branchCount + (rand.nextDouble() * 0.3 - 0.15);
                int branchLen = 6 + rand.nextInt(4);
                int bStartY = height - 6 + rand.nextInt(4);

                int endX = 0, endY = 0, endZ = 0;
                for (int s = 1; s <= branchLen; s++) {
                    int bx = (int) Math.round(Math.cos(angle) * s);
                    int bz = (int) Math.round(Math.sin(angle) * s);
                    int by = bStartY + (int) (s * 0.5);
                    level.setBlock(origin.offset(bx, by, bz), trunkMat, 2);
                    endX = bx; endY = by; endZ = bz;
                }

                int r = 3 + rand.nextInt(2);
                for (int cdx = -r; cdx <= r; cdx++) {
                    for (int cdy = -r; cdy <= r; cdy++) {
                        for (int cdz = -r; cdz <= r; cdz++) {
                            if (Math.sqrt(cdx * cdx + cdy * cdy + cdz * cdz) <= r) {
                                BlockPos lp = origin.offset(endX + cdx, endY + cdy, endZ + cdz);
                                if (level.isEmptyBlock(lp)) {
                                    level.setBlock(lp, rand.nextBoolean() ? leafPrimary : leafSecondary, 2);
                                }
                            }
                        }
                    }
                }
            }

            int topR = 4;
            for (int cdx = -topR; cdx <= topR; cdx++) {
                for (int cdy = -topR; cdy <= topR; cdy++) {
                    for (int cdz = -topR; cdz <= topR; cdz++) {
                        if (Math.sqrt(cdx * cdx + cdy * cdy + cdz * cdz) <= topR) {
                            BlockPos lp = origin.offset(cdx, height + cdy, cdz);
                            if (level.isEmptyBlock(lp)) {
                                level.setBlock(lp, rand.nextBoolean() ? leafPrimary : leafSecondary, 2);
                            }
                        }
                    }
                }
            }
        }

        int floraR = canopyRadius(isUmbrellaStyle) + 2;
        for (int fx = -floraR; fx <= floraR; fx++) {
            for (int fz = -floraR; fz <= floraR; fz++) {
                if (rand.nextFloat() < 0.22F) {
                    BlockPos fp = origin.offset(fx, 0, fz);
                    if (level.isEmptyBlock(fp) && level.getBlockState(fp.below()).isSolid()) {
                        var dec = switch (rand.nextInt(5)) {
                            case 0 -> Blocks.MOSS_CARPET.defaultBlockState();
                            case 1 -> Blocks.FLOWERING_AZALEA.defaultBlockState();
                            case 2 -> Blocks.PINK_PETALS.defaultBlockState();
                            case 3 -> Blocks.BLUE_ORCHID.defaultBlockState();
                            default -> Blocks.RED_MUSHROOM.defaultBlockState();
                        };
                        level.setBlock(fp, dec, 2);
                    }
                }
            }
        }

        return true;
    }

    private static int canopyRadius(boolean isUmbrella) {
        return isUmbrella ? 8 : 6;
    }
}
