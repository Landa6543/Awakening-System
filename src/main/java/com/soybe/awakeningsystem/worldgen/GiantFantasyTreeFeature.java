package com.soybe.awakeningsystem.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GiantFantasyTreeFeature extends Feature<NoneFeatureConfiguration> {

    public GiantFantasyTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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

        // 1. REQUISITO ESTRICTO: Solo generarse sobre tierra natural / césped
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

        // 2. NUNCA sobre hojas, madera, troncos, raíces aéreas ni líquidos
        if (groundState.is(net.minecraft.tags.BlockTags.LEAVES)
                || groundState.is(net.minecraft.tags.BlockTags.LOGS)
                || groundState.getBlock() instanceof LeavesBlock
                || groundState.is(Blocks.MANGROVE_ROOTS)
                || level.getBlockState(origin).liquid()) {
            return false;
        }

        // 3. El bloque de origen debe ser aire o vegetación reemplazable
        if (!level.isEmptyBlock(origin) && !level.getBlockState(origin).canBeReplaced()) {
            return false;
        }

        // 4. Despeje vertical de 16 bloques hacia arriba (para nunca generarse debajo ni dentro de otro árbol)
        for (int dy = 1; dy <= 16; dy++) {
            BlockState above = level.getBlockState(origin.above(dy));
            if (above.is(net.minecraft.tags.BlockTags.LOGS) || above.is(net.minecraft.tags.BlockTags.LEAVES)) {
                return false;
            }
        }

        // 5. Despeje horizontal para evitar que dos árboles colosales se solapen
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dist = 1; dist <= 4; dist++) {
                BlockPos nearby = origin.relative(d, dist).above(2);
                if (level.getBlockState(nearby).is(net.minecraft.tags.BlockTags.LOGS)) {
                    return false;
                }
            }
        }

        int height = 18 + rand.nextInt(12); // 18 a 29 bloques
        boolean isUmbrellaStyle = rand.nextBoolean(); // Estilo Foto 2 (Paraguas ancho con linternas) o Foto 3 (Copa densa con raíces de manglar)

        var trunkMat = Blocks.DARK_OAK_WOOD.defaultBlockState();
        var rootMat = isUmbrellaStyle ? Blocks.DARK_OAK_WOOD.defaultBlockState() : Blocks.MANGROVE_ROOTS.defaultBlockState();

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

        // 1. Tronco masivo 3x3 o 2x2 anclado firmemente al suelo
        int trunkW = (height > 22) ? 3 : 2;
        for (int dx = 0; dx < trunkW; dx++) {
            for (int dz = 0; dz < trunkW; dz++) {
                for (int y = 0; y < height; y++) {
                    level.setBlock(origin.offset(dx, y, dz), trunkMat, 2);
                }
                // Anclaje vertical hacia abajo hasta tocar suelo firme (elimina troncos flotando)
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

        // 2. Raíces buttress monumentales (Estilo Fotos 2 y 3)
        int[][] rootDirs = {
            {-1, 0}, {trunkW, 0}, {0, -1}, {0, trunkW},
            {-1, -1}, {trunkW, trunkW}, {-1, trunkW}, {trunkW, -1}
        };
        for (int[] dir : rootDirs) {
            int rootLen = 3 + rand.nextInt(4);
            int startH = 2 + rand.nextInt(4);
            for (int r = 1; r <= rootLen; r++) {
                int rx = (dir[0] < 0) ? dir[0] * r : (dir[0] >= trunkW ? trunkW - 1 + (dir[0] - trunkW + 1) * r : dir[0]);
                int rz = (dir[1] < 0) ? dir[1] * r : (dir[1] >= trunkW ? trunkW - 1 + (dir[1] - trunkW + 1) * r : dir[1]);
                for (int rh = -1; rh <= startH - r; rh++) {
                    BlockPos rp = origin.offset(rx, rh, rz);
                    level.setBlock(rp, (rh <= 0 && rand.nextBoolean()) ? rootMat : trunkMat, 2);
                }
            }
        }

        // 3. Ramas y Copa
        if (isUmbrellaStyle) {
            // Estilo Foto 2: Copa ancha horizontal tipo paraguas con linternas colgando en cadenas
            int branchStartY = height - 5;
            int canopyRadius = 8 + rand.nextInt(4);

            // Ramas radiales
            for (int deg = 0; deg < 360; deg += 45) {
                double rad = Math.toRadians(deg);
                for (int step = 1; step <= canopyRadius - 2; step++) {
                    int bx = (int) Math.round(Math.cos(rad) * step);
                    int bz = (int) Math.round(Math.sin(rad) * step);
                    int by = branchStartY + (int) (step * 0.25);
                    level.setBlock(origin.offset(bx, by, bz), trunkMat, 2);
                }
            }

            // Capa densa de hojas horizontal de 3 bloques de grosor
            for (int dx = -canopyRadius; dx <= canopyRadius + trunkW; dx++) {
                for (int dz = -canopyRadius; dz <= canopyRadius + trunkW; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= canopyRadius) {
                        for (int dy = -1; dy <= 2; dy++) {
                            // Cúpula achatada
                            if (dy == 2 && dist > canopyRadius - 2) continue;
                            if (dy == -1 && dist > canopyRadius - 1) continue;

                            BlockPos lp = origin.offset(dx, height + dy, dz);
                            if (level.isEmptyBlock(lp)) {
                                level.setBlock(lp, rand.nextBoolean() ? leafPrimary : leafSecondary, 2);
                            }
                        }

                        // Linternas colgando de cadenas bajo la copa (SOLO si hay hojas/madera sólida directamente encima)
                        if (dist >= 3 && dist <= canopyRadius - 2 && rand.nextFloat() < 0.08F) {
                            BlockPos chainTop = origin.offset(dx, height - 2, dz);
                            BlockState above = level.getBlockState(chainTop.above());
                            if ((above.is(net.minecraft.tags.BlockTags.LEAVES) || above.is(net.minecraft.tags.BlockTags.LOGS) || above.isSolid()) && level.isEmptyBlock(chainTop)) {
                                int chainLen = 1 + rand.nextInt(2);
                                for (int c = 0; c < chainLen; c++) {
                                    level.setBlock(chainTop.below(c), Blocks.CHAIN.defaultBlockState(), 2);
                                }
                                BlockPos lanternPos = chainTop.below(chainLen);
                                if (level.isEmptyBlock(lanternPos)) {
                                    level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Estilo Foto 3: Ramas orgánicas diagonales con cúmulos de hojas
            int branchCount = 4 + rand.nextInt(3);
            for (int b = 0; b < branchCount; b++) {
                double angle = (b * (2 * Math.PI / branchCount)) + (rand.nextDouble() * 0.3);
                int branchLen = 5 + rand.nextInt(4);
                int bStartY = height - 7 + rand.nextInt(4);

                int endX = 0, endY = 0, endZ = 0;
                for (int s = 1; s <= branchLen; s++) {
                    int bx = (int) Math.round(Math.cos(angle) * s);
                    int bz = (int) Math.round(Math.sin(angle) * s);
                    int by = bStartY + (int) (s * 0.5);
                    level.setBlock(origin.offset(bx, by, bz), trunkMat, 2);
                    endX = bx; endY = by; endZ = bz;
                }

                // Cúmulo de hojas esférico en el extremo
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

            // Copa central
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

        // 4. Alfombra floral, musgo y detalles de suelo alrededor de las raíces (Fotos 2 y 3)
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
