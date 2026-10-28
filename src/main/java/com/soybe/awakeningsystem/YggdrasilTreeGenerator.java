package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Generador procedural de Yggdrasil (El Gran Árbol del Mundo) para la dimensión Isekai.
 * - Despeje total de terreno montañoso y árboles en un radio de 130 bloques (sin montañas que lo tapen).
 * - Cimientos continentales suaves de césped y piedra sin vacío ni agua expuesta.
 * - Manantial Sagrado de Urðr (Urðarbrunnr) en el corazón del árbol con amatista y linternas marinas.
 * - 8 raíces contrafuerte monumentales con arcos transitables.
 * - Tronco colosal acanalado de 145 bloques de altura con torsión orgánica y 6 nervaduras helicoidales.
 * - Ramas mayores 3D calculadas con curvas Bézier que se ahusan orgánicamente.
 * - Cúmulos de follaje de cerezo y azalea florecida con núcleos bioluminiscentes de shroomlight.
 * - CERO escaleras flotantes, CERO mesas de encantamiento sueltas, CERO cadenas o faroles en el aire.
 */
public class YggdrasilTreeGenerator {

    public static final int BASE_Y = 88;
    public static final int TRUNK_HEIGHT = 145;
    public static final BlockPos APEX_SUMMIT_POS = new BlockPos(0, 245, 0);
    public static final BlockPos ASCENSION_GATE_POS = new BlockPos(0, 231, 6);
    public static final BlockPos GROUND_ASCENSION_GATE_POS = new BlockPos(0, BASE_Y, 20);
    public static final BlockPos OVERWORLD_RETURN_GATE_POS = new BlockPos(0, BASE_Y, 20);

    public static void generate(ServerLevel level, BlockPos basePos) {
        AwakeningSystemMod.LOGGER.info("Iniciando generación limpia y majestuosa de Yggdrasil en {}", basePos);
        RandomSource random = RandomSource.create(level.getSeed() ^ 0x5947474452415349L);

        clearAirAndBuildMountainBase(level, basePos);

        generateButtressRoots(level, basePos, random);

        generateTrunk(level, basePos, random);

        generateBoughsAndCanopies(level, basePos, random);

        generateHiddenMurimPortal(level, basePos, random);

        AwakeningSystemMod.LOGGER.info("¡Yggdrasil procedural generado exitosamente sin obstrucciones!");
    }

    public static void ensureMurimPortal(ServerLevel level) {
        if (level.getBlockState(ASCENSION_GATE_POS).isAir()) {
            generateHiddenMurimPortal(level, new BlockPos(0, BASE_Y, 0), level.getRandom());
        }
    }

    /**
     * Genera la majestuosa Puerta de los Nueve Cielos al Reino Murim
     * en la cima del Yggdrasil (Y=298), con un arco ceremonial monumental,
     * faroles de alma, obsidiana llorosa y un mirador sobre las nubes.
     */
    public static void generateHiddenMurimPortal(ServerLevel level, BlockPos basePos, RandomSource random) {
        int cx = ASCENSION_GATE_POS.getX();
        int cy = ASCENSION_GATE_POS.getY();
        int cz = ASCENSION_GATE_POS.getZ();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 6; dz++) {
                double dist = Math.sqrt(dx * dx + (dz - 1) * (dz - 1));
                if (dist > 5.5D) continue;

                BlockPos p = new BlockPos(cx + dx, cy - 1, cz + dz);
                if (dist > 4.6D) {
                    level.setBlock(p, (dx % 2 == 0) ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, ((dx + dz) % 2 == 0) ? Blocks.CHERRY_PLANKS.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 6; dz++) {
                double dist = Math.sqrt(dx * dx + (dz - 1) * (dz - 1));
                if (dist <= 5.0D) {
                    for (int dy = 0; dy <= 6; dy++) {
                        BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 0; dy <= 5; dy++) {
                boolean isBorder = (Math.abs(dx) == 2 || Math.abs(dx) == 3 || dy == 0 || dy == 4 || dy == 5);
                BlockPos p = new BlockPos(cx + dx, cy + dy, cz);
                if (isBorder) {
                    BlockState mat;
                    if (Math.abs(dx) == 3) {
                        mat = (dy == 5) ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
                    } else if (Math.abs(dx) == 2) {
                        mat = (dy == 4 || dy == 5) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    } else if (dy == 0) {
                        mat = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                    } else if (dy == 4) {
                        mat = Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    } else {
                        mat = Blocks.GILDED_BLACKSTONE.defaultBlockState();
                    }
                    level.setBlock(p, mat, 2);
                } else {
                    level.setBlock(p, com.soybe.awakeningsystem.block.SystemBlocks.ASCENSION_PORTAL.get().defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(cx - 3, cy + 6, cz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 6, cz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy, cz + 3), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz + 3), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy, cz + 3), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz + 3), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static final int FAST_FLAG = 18;

    /**
     * Modela la Gran Montaña Sagrada de Yggdrasil alrededor del tronco, raíces monumentales y santuario:
     * - Meseta ceremonial amplia en BASE_Y = 88 con radio de 66 bloques (asienta las 8 raíces de longitud 62 y la plaza).
     * - Ladera natural suave entre r=66 y r=88 que desciende armoniosamente al terreno natural.
     * - Relleno y despeje optimizados con FAST_FLAG para ejecutarse sin congelar el servidor.
     */
    private static void clearAirAndBuildMountainBase(ServerLevel level, BlockPos basePos) {
        int centerX = basePos.getX();
        int centerZ = basePos.getZ();
        int plateauRadius = 66;
        int blendRadius = 88;

        for (int dx = -blendRadius; dx <= blendRadius; dx++) {
            for (int dz = -blendRadius; dz <= blendRadius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > blendRadius) continue;

                int worldX = centerX + dx;
                int worldZ = centerZ + dz;

                int hNat = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ);

                int targetY;
                if (dist <= plateauRadius) {
                    targetY = BASE_Y;
                } else {
                    double t = (dist - plateauRadius) / (double) (blendRadius - plateauRadius);
                    double s = 0.5D * (1.0D - Math.cos(Math.PI * t));
                    targetY = BASE_Y + (int) Math.round(s * (hNat - BASE_Y));
                }

                if (dist <= plateauRadius) {
                    int topClear = Math.max(hNat + 1, BASE_Y + 14);
                    for (int y = targetY + 1; y <= topClear; y++) {
                        BlockPos p = new BlockPos(worldX, y, worldZ);
                        if (!level.getBlockState(p).isAir()) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), FAST_FLAG);
                        }
                    }
                }

                BlockPos surf = new BlockPos(worldX, targetY, worldZ);
                boolean isMoss = (dist <= 30.0D && (dx + dz) % 5 == 0);
                level.setBlock(surf, isMoss ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), FAST_FLAG);

                int minDepth = Math.max(targetY - 4, hNat - 2);
                for (int y = targetY - 1; y >= minDepth; y--) {
                    BlockPos bp = new BlockPos(worldX, y, worldZ);
                    BlockState cur = level.getBlockState(bp);
                    if (cur.isAir() || cur.is(Blocks.WATER) || cur.is(Blocks.SEAGRASS) || cur.is(Blocks.TALL_SEAGRASS) || cur.is(Blocks.KELP) || cur.is(Blocks.KELP_PLANT) || cur.getBlock() instanceof LeavesBlock) {
                        level.setBlock(bp, (y >= targetY - 2) ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState(), FAST_FLAG);
                    } else {
                        break;
                    }
                }
            }
        }

        for (int z = 18; z <= 28; z++) {
            for (int x = -2; x <= 2; x++) {
                BlockPos p = new BlockPos(centerX + x, BASE_Y, centerZ + z);
                if (Math.abs(x) == 2) {
                    level.setBlock(p, Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
                } else if (Math.abs(x) == 1) {
                    level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), FAST_FLAG);
                } else {
                    level.setBlock(p, (z % 2 == 0) ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : Blocks.QUARTZ_BRICKS.defaultBlockState(), FAST_FLAG);
                }
            }
            if (z == 20 || z == 26) {
                level.setBlock(new BlockPos(centerX - 3, BASE_Y, centerZ + z), Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
                level.setBlock(new BlockPos(centerX - 3, BASE_Y + 1, centerZ + z), Blocks.SEA_LANTERN.defaultBlockState(), FAST_FLAG);
                level.setBlock(new BlockPos(centerX + 3, BASE_Y, centerZ + z), Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
                level.setBlock(new BlockPos(centerX + 3, BASE_Y + 1, centerZ + z), Blocks.SEA_LANTERN.defaultBlockState(), FAST_FLAG);
            }
        }
    }

    /**
     * Genera 8 raíces monumentales semienterradas con arcos que sobresalen orgánicamente del terreno.
     * Las raíces nacen del tronco, penetran en la tierra y emergen en arcos transitables cubiertos de musgo y azaleas.
     */
    private static void generateButtressRoots(ServerLevel level, BlockPos basePos, RandomSource random) {
        for (int i = 0; i < 8; i++) {
            double baseAngle = i * (Math.PI / 4.0) + 0.12;
            generateSingleWindingRoot(level, basePos, baseAngle, random, false);
            if (i % 2 == 0) {
                generateSingleWindingRoot(level, basePos, baseAngle + 0.28, random, true);
            }
        }
    }

    private static void generateSingleWindingRoot(ServerLevel level, BlockPos basePos, double baseAngle, RandomSource random, boolean isBranch) {
        int maxDist = isBranch ? 48 : 62;
        int minDist = isBranch ? 16 : 8;
        int steps = maxDist - minDist;

        for (int step = 0; step <= steps; step++) {
            double dist = minDist + step;
            double t = (dist - 8.0) / 54.0;
            double angle = baseAngle + 0.18 * Math.sin(t * Math.PI * 2.5);

            int rx = basePos.getX() + (int) Math.round(Math.cos(angle) * dist);
            int rz = basePos.getZ() + (int) Math.round(Math.sin(angle) * dist);

            int rootY;
            boolean isArch = false;
            if (dist <= 13.0) {
                double f = (13.0 - dist) / 5.0;
                rootY = BASE_Y + (int) Math.round(f * f * 6.0) - 1;
            } else if (dist <= 22.0) {
                rootY = BASE_Y - 3;
            } else if (dist <= 36.0) {
                double archT = (dist - 22.0) / 14.0;
                double archH = Math.sin(archT * Math.PI) * 4.5;
                rootY = BASE_Y + (int) Math.round(archH);
                if (rootY > BASE_Y) isArch = true;
            } else if (dist <= 45.0) {
                rootY = BASE_Y - 3;
            } else if (dist <= 54.0) {
                double archT2 = (dist - 45.0) / 9.0;
                double archH2 = Math.sin(archT2 * Math.PI) * 2.2;
                rootY = BASE_Y + (int) Math.round(archH2);
                if (rootY > BASE_Y) isArch = true;
            } else {
                rootY = BASE_Y - 4;
            }

            int thickness = Math.max(1, (int) Math.round((1.0 - t * 0.6) * (isBranch ? 2.5 : 3.6)));

            for (int ox = -thickness; ox <= thickness; ox++) {
                for (int oz = -thickness; oz <= thickness; oz++) {
                    for (int oy = -thickness; oy <= thickness; oy++) {
                        if (ox * ox + oz * oz + oy * oy <= thickness * thickness) {
                            BlockPos rp = new BlockPos(rx + ox, rootY + oy, rz + oz);
                            boolean surface = (ox * ox + oz * oz + oy * oy) >= (thickness - 1) * (thickness - 1);

                            BlockState mat;
                            if (rp.getY() < BASE_Y && !isArch) {
                                mat = (surface && random.nextInt(3) == 0) ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.DARK_OAK_LOG.defaultBlockState();
                            } else {
                                mat = surface ?
                                        (random.nextInt(3) == 0 ? Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState() : Blocks.DARK_OAK_WOOD.defaultBlockState()) :
                                        Blocks.DARK_OAK_LOG.defaultBlockState();
                            }
                            level.setBlock(rp, mat, 2);

                            if (oy == thickness && rootY + oy >= BASE_Y) {
                                if (random.nextInt(4) == 0) {
                                    level.setBlock(rp.above(), Blocks.MOSS_CARPET.defaultBlockState(), 2);
                                } else if (random.nextInt(8) == 0) {
                                    level.setBlock(rp.above(), Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }

            if (rootY < BASE_Y) {
                BlockPos surfPos = new BlockPos(rx, BASE_Y, rz);
                if (level.getBlockState(surfPos).is(Blocks.GRASS_BLOCK) || level.getBlockState(surfPos).is(Blocks.DIRT)) {
                    level.setBlock(surfPos, (random.nextInt(3) == 0) ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState(), 2);
                }
            }

            if (isArch && rootY - thickness > BASE_Y) {
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        for (int ty = BASE_Y + 1; ty < rootY - thickness; ty++) {
                            BlockPos tun = new BlockPos(rx + ox, ty, rz + oz);
                            level.setBlock(tun, Blocks.AIR.defaultBlockState(), 2);
                        }
                        BlockPos underRoot = new BlockPos(rx + ox, rootY - thickness, rz + oz);
                        if (random.nextInt(3) == 0 && level.getBlockState(underRoot.below()).isAir()) {
                            level.setBlock(underRoot.below(), Blocks.HANGING_ROOTS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Genera el tronco colosal con 6 nervaduras helicoidales que giran orgánicamente.
     * Ensanchado a radio 18 en la base y 175 bloques de altura monumental.
     */
    private static void generateTrunk(ServerLevel level, BlockPos basePos, RandomSource random) {
        int centerX = basePos.getX();
        int centerZ = basePos.getZ();

        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            int worldY = BASE_Y + y;

            double r;
            if (y < 35) {
                r = 18.0 - (y / 35.0) * 5.2;
            } else if (y < 85) {
                r = 12.8 - ((y - 35) / 50.0) * 3.6;
            } else if (y < 135) {
                r = 9.2 - ((y - 85) / 50.0) * 2.2;
            } else {
                r = 7.0 - ((y - 135) / 40.0) * 1.8;
            }

            int cx = centerX + (int) Math.round(4.2 * Math.sin(y * 0.028));
            int cz = centerZ + (int) Math.round(3.2 * Math.cos(y * 0.025));

            int maxR = (int) Math.ceil(r * 1.35);
            for (int dx = -maxR; dx <= maxR; dx++) {
                for (int dz = -maxR; dz <= maxR; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    double angle = Math.atan2(dz, dx);

                    double rib = 1.0 + 0.22 * Math.sin(6.0 * angle + y * 0.052 + 0.3 * Math.sin(y * 0.02));
                    double targetR = r * rib;

                    if (d <= targetR) {
                        BlockPos p = new BlockPos(cx + dx, worldY, cz + dz);
                        boolean isBark = d >= targetR - 1.3;

                        BlockState mat;
                        if (isBark) {
                            boolean inCrevice = rib < 1.0;
                            mat = inCrevice ?
                                    Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState() :
                                    Blocks.DARK_OAK_WOOD.defaultBlockState();
                        } else {
                            mat = Blocks.DARK_OAK_LOG.defaultBlockState();
                        }
                        level.setBlock(p, mat, 2);
                    }
                }
            }
        }
    }

    /**
     * Genera las ramas mayores tridimensionales, cúpulas celestiales de follaje y la Puerta de Ascensión en la cima.
     */
    private static void generateBoughsAndCanopies(ServerLevel level, BlockPos basePos, RandomSource random) {
        int centerX = basePos.getX();
        int centerZ = basePos.getZ();

        BoughTier[] tiers = new BoughTier[] {
                new BoughTier(60, 4, 36.0, 0.0, 14.0),
                new BoughTier(100, 4, 38.0, Math.PI / 4.0, 16.0),
                new BoughTier(138, 4, 28.0, 0.2, 14.0)
        };

        for (BoughTier tier : tiers) {
            int trunkY = BASE_Y + tier.yOffset;
            double cx = centerX + 4.2 * Math.sin(tier.yOffset * 0.028);
            double cz = centerZ + 3.2 * Math.cos(tier.yOffset * 0.025);

            for (int i = 0; i < tier.count; i++) {
                double angle = tier.angleOffset + i * (2.0 * Math.PI / tier.count) + (random.nextDouble() - 0.5) * 0.15;
                double reach = tier.reach + (random.nextDouble() - 0.5) * 4.0;

                Vec3 p0 = new Vec3(cx, trunkY, cz);
                Vec3 p1 = new Vec3(
                        cx + Math.cos(angle) * (reach * 0.45),
                        trunkY - 2.0 + (random.nextDouble() - 0.5) * 2.0,
                        cz + Math.sin(angle) * (reach * 0.45)
                );
                Vec3 p2 = new Vec3(
                        cx + Math.cos(angle) * reach,
                        trunkY + tier.elevation + (random.nextDouble() - 0.5) * 3.0,
                        cz + Math.sin(angle) * reach
                );

                drawBezierBranch(level, p0, p1, p2, 3.2, 1.4, random);

                double subAngle1 = angle + 0.35;
                Vec3 subEnd1 = p2.add(Math.cos(subAngle1) * 10.0, 3.0, Math.sin(subAngle1) * 10.0);
                drawBezierBranch(level, p2, p2.add(0, 1, 0), subEnd1, 1.4, 0.9, random);

                generateCanopyCloud(level, BlockPos.containing(p2), 11, 5, 11, random);
                generateCanopyCloud(level, BlockPos.containing(subEnd1), 8, 4, 8, random);
            }
        }

        BlockPos apexPos = new BlockPos(centerX, BASE_Y + TRUNK_HEIGHT, centerZ);
        generateCanopyCloud(level, apexPos, 18, 7, 18, random);
        generateCanopyCloud(level, apexPos.above(4), 13, 5, 13, random);

    }

    /**
     * Dibuja una rama 3D orgánica siguiendo una curva cuadrática de Bézier.
     */
    private static void drawBezierBranch(ServerLevel level, Vec3 p0, Vec3 p1, Vec3 p2, double rStart, double rEnd, RandomSource random) {
        int steps = 18;
        for (int step = 0; step <= steps; step++) {
            double t = (double) step / steps;
            double omt = 1.0 - t;

            double x = omt * omt * p0.x + 2.0 * omt * t * p1.x + t * t * p2.x;
            double y = omt * omt * p0.y + 2.0 * omt * t * p1.y + t * t * p2.y;
            double z = omt * omt * p0.z + 2.0 * omt * t * p1.z + t * t * p2.z;

            double radius = rStart * (1.0 - t) + rEnd * t;
            int rInt = (int) Math.ceil(radius);

            for (int ox = -rInt; ox <= rInt; ox++) {
                for (int oy = -rInt; oy <= rInt; oy++) {
                    for (int oz = -rInt; oz <= rInt; oz++) {
                        if (ox * ox + oy * oy + oz * oz <= radius * radius) {
                            BlockPos bp = new BlockPos((int) Math.round(x + ox), (int) Math.round(y + oy), (int) Math.round(z + oz));
                            boolean surface = (ox * ox + oy * oy + oz * oz) >= (radius - 0.8) * (radius - 0.8);
                            BlockState mat = surface ? Blocks.DARK_OAK_WOOD.defaultBlockState() : Blocks.DARK_OAK_LOG.defaultBlockState();
                            level.setBlock(bp, mat, FAST_FLAG);
                        }
                    }
                }
            }
        }
    }

    /**
     * Genera una nube de follaje elipsoidal esponjosa y tridimensional con núcleos luminosos de shroomlight.
     */
    private static void generateCanopyCloud(ServerLevel level, BlockPos center, int rx, int ry, int rz, RandomSource random) {
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double normX = (double) dx / rx;
                    double normY = (double) dy / ry;
                    double normZ = (double) dz / rz;
                    double distSq = normX * normX + normY * normY + normZ * normZ;

                    if (distSq <= 1.0 && (distSq >= 0.45 || distSq < 0.12 || random.nextFloat() > 0.4f)) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        BlockState existing = level.getBlockState(pos);

                        if (existing.isAir() || existing.getBlock() instanceof LeavesBlock) {
                            if (distSq < 0.10 && random.nextInt(3) == 0) {
                                level.setBlock(pos, Blocks.SHROOMLIGHT.defaultBlockState(), FAST_FLAG);
                            } else {
                                int roll = random.nextInt(100);
                                BlockState leafState;
                                if (roll < 75) {
                                    leafState = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                                } else if (roll < 95) {
                                    leafState = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                                } else {
                                    leafState = Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                                }
                                level.setBlock(pos, leafState, FAST_FLAG);
                            }
                        }
                    }
                }
            }
        }
    }

    private static record BoughTier(int yOffset, int count, double reach, double angleOffset, double elevation) {}
}
