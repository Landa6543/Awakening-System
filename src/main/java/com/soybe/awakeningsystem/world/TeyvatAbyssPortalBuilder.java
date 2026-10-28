package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.TeyvatWorldManager;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Generador monumental de la Espiral del Abismo (Spiral Abyss Gateway) de Teyvat.
 * Basado fielmente en la arquitectura canónica de Genshin Impact (Arrecife de Musk / Musk Reef):
 * - Gran arco circular vertical de arenisca ancestral, bronce/cobre labrado y gemas rúnicas.
 * - Sin túnel: Vano circular diáfano de 1 bloque de espesor para el vórtice cósmico.
 * - Cresta romboide cenital (Diamond Crest) con nudo solar en la cúspide.
 * - Dos alas laterales curvadas de ruinas (buttresses) con rotura asimétrica.
 * - Plintos/balustradas que flanquean la escalinata de acceso de 7 bloques de ancho.
 * - Estados:
 *   * ACTIVO (Imagen 1): Vórtice cósmico cian (gate_cyan.png), gemas Sea Lantern y haces celestiales.
 *   * BLOQUEADO (Imagen 2): Vano transparente abierto al cielo/nubes, gemas cálidas ámbar y sello carmesí perimetral.
 */
public final class TeyvatAbyssPortalBuilder {
    private TeyvatAbyssPortalBuilder() {}

    public enum BuildMode {
        FULL_3D,
        FLAT
    }

    public enum CenterMode {
        AIR,
        GLASS,
        PORTAL,
        NETHER
    }

    private static boolean portalGeneratedInTeyvat = false;

    /**
     * Garantiza que la Isla del Arrecife de Musk y la Espiral del Abismo existan en Teyvat.
     */
    public static synchronized void ensureAbyssPortalInTeyvat(ServerLevel level) {
        if (portalGeneratedInTeyvat) return;

        net.minecraft.server.MinecraftServer server = level.getServer();
        if (server != null) {
            try {
                var data = TeyvatWorldManager.TeyvatAbyssSavedData.get(server);
                if (data.isMuskReefBuilt()) {
                    portalGeneratedInTeyvat = true;
                    TeyvatWorldManager.registerAbyssPortal(level.dimension(), TeyvatWorldManager.MUSK_REEF_ISLAND_POS, TeyvatWorldManager.ABYSS_FACING, TeyvatWorldManager.isAbyssActive());
                    return;
                }
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error checking TeyvatAbyssSavedData for Musk Reef: ", e);
            }
        }

        BlockPos islandCenter = TeyvatWorldManager.MUSK_REEF_ISLAND_POS;
        boolean active = TeyvatWorldManager.isAbyssActive();

        BlockPos apexCheck = islandCenter.above(11);
        BlockState apexSt = level.getBlockState(apexCheck);
        if (apexSt.is(Blocks.SEA_LANTERN) || apexSt.is(Blocks.OCHRE_FROGLIGHT) || apexSt.is(Blocks.CUT_SANDSTONE) || apexSt.is(Blocks.STONE_BRICKS)) {
            portalGeneratedInTeyvat = true;
            if (server != null) {
                try {
                    TeyvatWorldManager.TeyvatAbyssSavedData.get(server).setMuskReefBuilt(true);
                } catch (Exception e) {
                    AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for Musk Reef: ", e);
                }
            }
            TeyvatWorldManager.registerAbyssPortal(level.dimension(), islandCenter, TeyvatWorldManager.ABYSS_FACING, active);
            return;
        }

        generateMuskReefIsland(level, islandCenter, TeyvatWorldManager.ABYSS_FACING, active);
        portalGeneratedInTeyvat = true;
        if (server != null) {
            try {
                TeyvatWorldManager.TeyvatAbyssSavedData.get(server).setMuskReefBuilt(true);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.debug("Error setting TeyvatAbyssSavedData for Musk Reef: ", e);
            }
        }
        AwakeningSystemMod.LOGGER.info("[Teyvat] Arrecife de Musk e Isla de la Espiral del Abismo generados en {}", islandCenter);
    }

    /**
     * Genera un islote natural solitario (Arrecife de Musk) rodeado de océano con el portal en el centro.
     */
    public static void generateMuskReefIsland(ServerLevel level, BlockPos islandCenter, Direction facing, boolean active) {
        int cx = islandCenter.getX();
        int cz = islandCenter.getZ();
        int seaLevel = 62;
        int islandHeight = 64;

        int rOcean = 38;
        for (int dx = -rOcean; dx <= rOcean; dx++) {
            for (int dz = -rOcean; dz <= rOcean; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > rOcean) continue;

                int wx = cx + dx;
                int wz = cz + dz;

                int maxClearY = (dist <= 20) ? 80 : (seaLevel + 3);
                for (int y = seaLevel + 1; y <= maxClearY; y++) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                for (int y = 48; y <= seaLevel; y++) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                }

                for (int y = 40; y < 48; y++) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    BlockState bed = (dist > 30) ? Blocks.GRAVEL.defaultBlockState() : Blocks.SAND.defaultBlockState();
                    level.setBlock(p, bed, 2);
                }
            }
        }

        for (int dx = -22; dx <= 22; dx++) {
            for (int dz = -22; dz <= 22; dz++) {
                double angle = Math.atan2(dz, dx);
                double islandR = 17.5 + 2.0 * Math.sin(angle * 3.0) + 1.2 * Math.cos(angle * 2.0);
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > islandR) continue;

                int wx = cx + dx;
                int wz = cz + dz;

                int surfaceY;
                if (dist > islandR - 3.0) {
                    surfaceY = seaLevel + (dist > islandR - 1.2 ? 0 : 1);
                } else {
                    surfaceY = islandHeight;
                }

                for (int y = 48; y < surfaceY - 2; y++) {
                    level.setBlock(new BlockPos(wx, y, wz), Blocks.STONE.defaultBlockState(), 2);
                }
                for (int y = Math.max(48, surfaceY - 2); y < surfaceY; y++) {
                    level.setBlock(new BlockPos(wx, y, wz), Blocks.DIRT.defaultBlockState(), 2);
                }

                BlockPos surfPos = new BlockPos(wx, surfaceY, wz);
                if (surfaceY <= seaLevel + 1 && dist > islandR - 3.0) {
                    level.setBlock(surfPos, Blocks.SAND.defaultBlockState(), 2);
                } else {
                    level.setBlock(surfPos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);

                    if (dist > 8.0 && level.random.nextFloat() < 0.20F) {
                        BlockPos vegPos = surfPos.above();
                        float rf = level.random.nextFloat();
                        if (rf < 0.35F) {
                            level.setBlock(vegPos, Blocks.DANDELION.defaultBlockState(), 2);
                        } else if (rf < 0.65F) {
                            level.setBlock(vegPos, Blocks.CORNFLOWER.defaultBlockState(), 2);
                        } else {
                            level.setBlock(vegPos, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        BlockPos basePos = islandCenter.atY(islandHeight);
        build(level, basePos, facing, BuildMode.FULL_3D, CenterMode.AIR, active);
    }

    public static void build(ServerLevel level, BlockPos baseCenterPos, Direction facing, BuildMode mode, CenterMode centerMode) {
        build(level, baseCenterPos, facing, mode, centerMode, TeyvatWorldManager.isAbyssActive());
    }

    /**
     * Construye la estructura completa con orientación, modo 3D y estado activo/bloqueado.
     */
    public static void build(ServerLevel level, BlockPos baseCenterPos, Direction facing,
                             BuildMode mode, CenterMode centerMode, boolean active) {
        Direction rightDir = facing.getOpposite().getClockWise();
        Direction leftDir = rightDir.getOpposite();

        TeyvatWorldManager.registerAbyssPortal(level.dimension(), baseCenterPos, facing, active);

        if (mode == BuildMode.FULL_3D) {
            ensurePlatformFoundation(level, baseCenterPos, facing, rightDir);
            buildElevatedDaisAndSteps(level, baseCenterPos, facing, rightDir, leftDir);
        }

        buildPortalRingLayer(level, baseCenterPos, facing, rightDir, leftDir, 0, centerMode, active);

        if (mode == BuildMode.FULL_3D) {
            buildArchitecturalRelief(level, baseCenterPos, facing, rightDir, leftDir, active);
        }

        if (level.getServer() != null) {
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                com.soybe.awakeningsystem.DungeonManager.syncRiftsToPlayer(player);
            }
        }

        playBuildEffects(level, baseCenterPos, active);
    }

    /**
     * Actualiza rápidamente el estado visual del portal (gemas cardinales y partículas) sin reconstruir el terreno.
     */
    public static void updatePortalState(ServerLevel level, BlockPos baseCenterPos, Direction facing, boolean active) {
        Direction rightDir = facing.getOpposite().getClockWise();

        int[][] cardinalCoords = {
                {0, 11},
                {0, 3},
                {-4, 7},
                {4, 7}
        };

        BlockState nodeState = active ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.OCHRE_FROGLIGHT.defaultBlockState();
        for (int[] coord : cardinalCoords) {
            int dx = coord[0];
            int dy = coord[1];
            BlockPos p = baseCenterPos.relative(rightDir, dx).above(dy);
            level.setBlock(p, nodeState, 3);
        }

        playBuildEffects(level, baseCenterPos, active);
    }

    /**
     * Construye la capa vertical principal (dz = 0) del arco monumental:
     * - Vano circular diáfano de 7 bloques de diámetro (dy = 4..10, dx = -3..3).
     * - Anillo de cobre envejecido / bronce celestial con gemas cardinales y runas doradas.
     * - Cresta de diamante (Apex Emblem) en la cúspide (dy = 12..15).
     * - Alas curvadas de ruinas ancestrales a los lados (dx = -7..-5 y 5..7).
     * - Plintos balustrados de entrada (dx = -5..-4 y 4..5).
     */
    private static void buildPortalRingLayer(ServerLevel level, BlockPos baseCenterPos, Direction facing,
                                             Direction rightDir, Direction leftDir, int dz,
                                             CenterMode centerMode, boolean active) {
        Direction backDir = facing.getOpposite();

        for (int dx = -7; dx <= 7; dx++) {
            int absDx = Math.abs(dx);
            Direction outDir = (dx < 0) ? leftDir : rightDir;
            Direction inDir = (dx < 0) ? rightDir : leftDir;

            for (int dy = 0; dy <= 15; dy++) {
                BlockPos pos = baseCenterPos.relative(rightDir, dx).above(dy).relative(backDir, dz);
                double rSq = dx * dx + (dy - 7) * (dy - 7);
                double r = Math.sqrt(rSq);

                if (r < 3.45) {
                    BlockState coreState = resolveCenterBlock(centerMode, dx, dy, facing);
                    level.setBlock(pos, coreState, 3);
                    continue;
                }

                if (r <= 4.75) {
                    boolean isApex = (dx == 0 && dy == 11);
                    boolean isBase = (dx == 0 && dy == 3);
                    boolean isLeft = (dx == -4 && dy == 7);
                    boolean isRight = (dx == 4 && dy == 7);

                    if (isApex || isBase || isLeft || isRight) {
                        BlockState gemSt = active ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.OCHRE_FROGLIGHT.defaultBlockState();
                        level.setBlock(pos, gemSt, 3);
                        continue;
                    }

                    boolean isDiagonalRune = (absDx == 3 && (dy == 4 || dy == 10));
                    if (isDiagonalRune) {
                        level.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
                        continue;
                    }

                    level.setBlock(pos, Blocks.WAXED_CUT_COPPER.defaultBlockState(), 3);
                    continue;
                }

                if (dy >= 12 && dy <= 15 && absDx <= 2) {
                    BlockState crestSt = resolveApexCrest(absDx, dx, dy, rightDir, leftDir);
                    if (crestSt != null) {
                        level.setBlock(pos, crestSt, 3);
                        continue;
                    }
                }

                if (absDx >= 5 && absDx <= 7 && dy >= 3 && dy <= 10) {
                    BlockState wingSt = resolveWing(absDx, dx, dy, outDir, inDir);
                    if (wingSt != null) {
                        level.setBlock(pos, wingSt, 3);
                        continue;
                    }
                }

                if (r <= 5.85 && dy >= 3 && dy <= 11) {
                    BlockState frameSt = resolveRingOuterStair(absDx, dx, dy, outDir, inDir);
                    level.setBlock(pos, frameSt, 3);
                    continue;
                }

                if (absDx >= 4 && absDx <= 5 && dy >= 0 && dy <= 3) {
                    BlockState pedestalSt = resolvePedestal(absDx, dy);
                    level.setBlock(pos, pedestalSt, 3);
                    continue;
                }

                if (absDx <= 3 && dy >= 0 && dy <= 2) {
                    if (dy == 2) {
                        level.setBlock(pos, Blocks.SMOOTH_SANDSTONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(pos, Blocks.CUT_SANDSTONE.defaultBlockState(), 3);
                    }
                    continue;
                }

                if (!level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * Construye los relieves arquitectónicos frontales (dz = -1) y traseros (dz = +1).
     * IMPORTANTE: 100% simétrico en ambos lados y con el vano central diáfano.
     */
    private static void buildArchitecturalRelief(ServerLevel level, BlockPos baseCenterPos, Direction facing,
                                                 Direction rightDir, Direction leftDir, boolean active) {
        Direction backDir = facing.getOpposite();

        for (int dz : new int[]{-1, 1}) {
            Direction stairFrontFacing = (dz < 0) ? facing : backDir;

            for (int dx : new int[]{-5, -4, 4, 5}) {
                for (int dy = 0; dy <= 3; dy++) {
                    BlockPos p = baseCenterPos.relative(rightDir, dx).above(dy).relative(backDir, dz);
                    if (dy == 0) {
                        level.setBlock(p, Blocks.CUT_SANDSTONE.defaultBlockState(), 3);
                    } else if (dy == 1 || dy == 2) {
                        level.setBlock(p, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(p, Blocks.SANDSTONE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.HALF, Half.BOTTOM)
                                .setValue(StairBlock.FACING, stairFrontFacing), 3);
                    }
                }
            }

            for (int dy = 4; dy <= 8; dy++) {
                BlockPos pLeft = baseCenterPos.relative(rightDir, -6).above(dy).relative(backDir, dz);
                BlockPos pRight = baseCenterPos.relative(rightDir, 6).above(dy).relative(backDir, dz);
                level.setBlock(pLeft, Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
                level.setBlock(pRight, Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
            }

            BlockPos apexRelief = baseCenterPos.above(13).relative(backDir, dz);
            level.setBlock(apexRelief, Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);

            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = 3; dy <= 11; dy++) {
                    double rSq = dx * dx + (dy - 7) * (dy - 7);
                    if (rSq < 13.0) {
                        BlockPos coreAir = baseCenterPos.relative(rightDir, dx).above(dy).relative(backDir, dz);
                        level.setBlock(coreAir, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * Construye la plataforma escalonada (dais) y las gradas frontales de acceso de 7 bloques de ancho.
     */
    private static void buildElevatedDaisAndSteps(ServerLevel level, BlockPos baseCenterPos, Direction facing,
                                                  Direction rightDir, Direction leftDir) {
        Direction backDir = facing.getOpposite();

        for (int step = 1; step <= 2; step++) {
            int dz = -1 - step;
            int stepY = 2 - step;
            for (int dx = -4; dx <= 4; dx++) {
                BlockPos stairPos = baseCenterPos.relative(rightDir, dx).above(stepY).relative(backDir, dz);
                level.setBlock(stairPos, Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, facing), 3);

                for (int y = stepY - 1; y >= 0; y--) {
                    BlockPos under = baseCenterPos.relative(rightDir, dx).above(y).relative(backDir, dz);
                    level.setBlock(under, Blocks.CUT_SANDSTONE.defaultBlockState(), 3);
                }
            }
        }

        for (int step = 1; step <= 2; step++) {
            int dz = 1 + step;
            int stepY = 2 - step;
            for (int dx = -4; dx <= 4; dx++) {
                BlockPos stairPos = baseCenterPos.relative(rightDir, dx).above(stepY).relative(backDir, dz);
                level.setBlock(stairPos, Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, backDir), 3);

                for (int y = stepY - 1; y >= 0; y--) {
                    BlockPos under = baseCenterPos.relative(rightDir, dx).above(y).relative(backDir, dz);
                    level.setBlock(under, Blocks.CUT_SANDSTONE.defaultBlockState(), 3);
                }
            }
        }

        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -6; dx <= 6; dx++) {
                BlockPos p2 = baseCenterPos.relative(rightDir, dx).above(2).relative(backDir, dz);
                if (level.getBlockState(p2).isAir()) {
                    level.setBlock(p2, Blocks.SMOOTH_SANDSTONE.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * Resuelve el bloque central interior (Vano de 7 bloques de diámetro).
     */
    private static BlockState resolveCenterBlock(CenterMode mode, int dx, int dy, Direction facing) {
        switch (mode) {
            case GLASS -> {
                boolean isCore = (dx == 0 && (dy == 6 || dy == 7)) || (Math.abs(dx) == 1 && dy == 7);
                return isCore ? Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState() : Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState();
            }
            case NETHER -> {
                Direction.Axis axis = (facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
                return Blocks.NETHER_PORTAL.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
            }
            case PORTAL -> {
                Direction.Axis axis = (facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
                return SystemBlocks.ASCENSION_PORTAL.get().defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                        .setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
            }
            case AIR -> {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * Escaleras y losas del perímetro exterior del arco circular (suave y sin salientes de sierra).
     */
    private static BlockState resolveRingOuterStair(int absDx, int dx, int dy, Direction outDir, Direction inDir) {
        if (dy >= 8) {
            return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.HALF, Half.BOTTOM)
                    .setValue(StairBlock.FACING, inDir);
        } else {
            return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.HALF, Half.TOP)
                    .setValue(StairBlock.FACING, outDir);
        }
    }

    /**
     * Emblema Cenital de Diamante (Apex Crest) 100% Simétrico.
     */
    private static BlockState resolveApexCrest(int absDx, int dx, int dy, Direction rightDir, Direction leftDir) {
        Direction outDir = (dx < 0) ? leftDir : rightDir;
        Direction inDir = (dx < 0) ? rightDir : leftDir;

        if (dy == 12) {
            if (absDx <= 1) return Blocks.CUT_SANDSTONE.defaultBlockState();
            if (absDx == 2) {
                return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, outDir);
            }
        } else if (dy == 13) {
            if (absDx == 0) return Blocks.GOLD_BLOCK.defaultBlockState();
            if (absDx == 1) return Blocks.CHISELED_SANDSTONE.defaultBlockState();
            if (absDx == 2) {
                return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, outDir);
            }
        } else if (dy == 14) {
            if (absDx == 0) return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            if (absDx == 1) {
                return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, inDir);
            }
        } else if (dy == 15) {
            if (absDx == 0) {
                return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            }
        }
        return null;
    }

    /**
     * Alas laterales de ruinas (Buttresses) 100% Simétricas a ambos lados (absDx = 5..7, dy = 3..10).
     */
    private static BlockState resolveWing(int absDx, int dx, int dy, Direction outDir, Direction inDir) {
        if (dy == 10) {
            if (absDx == 6) {
                return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            }
            return null;
        } else if (dy == 9) {
            if (absDx == 6) {
                return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, outDir);
            }
            if (absDx == 5) return Blocks.CUT_SANDSTONE.defaultBlockState();
            return null;
        } else if (dy >= 6) {
            if (absDx == 5) return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            if (absDx == 6) return Blocks.WAXED_CUT_COPPER.defaultBlockState();
            if (absDx == 7) {
                return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.HALF, Half.BOTTOM)
                        .setValue(StairBlock.FACING, outDir);
            }
        } else {
            if (absDx == 6) return Blocks.WAXED_CUT_COPPER.defaultBlockState();
            return Blocks.CUT_SANDSTONE.defaultBlockState();
        }
        return null;
    }

    /**
     * Plinto balustrado en la base del portal (100% simétrico).
     */
    private static BlockState resolvePedestal(int absDx, int dy) {
        if (dy == 0) return Blocks.CUT_SANDSTONE.defaultBlockState();
        if (dy == 1) return Blocks.CHISELED_SANDSTONE.defaultBlockState();
        if (dy == 2) return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        return Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
    }

    /**
     * Cimientos sólidos subterráneos para que la estructura nunca quede en el aire.
     */
    private static void ensurePlatformFoundation(ServerLevel level, BlockPos baseCenterPos, Direction facing, Direction rightDir) {
        Direction backDir = facing.getOpposite();

        for (int dz = -4; dz <= 4; dz++) {
            for (int dx = -8; dx <= 8; dx++) {
                BlockPos surfacePos = baseCenterPos.relative(rightDir, dx).relative(backDir, dz);

                for (int depth = 1; depth <= 16; depth++) {
                    BlockPos p = surfacePos.below(depth);
                    BlockState current = level.getBlockState(p);
                    if (current.isSolid() && !current.is(Blocks.AIR) && !current.is(Blocks.WATER)) {
                        break;
                    }
                    BlockState filler = (depth <= 2) ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                    level.setBlock(p, filler, 2);
                }
            }
        }
    }

    /**
     * Efectos de partículas ambientales continuos ejecutados por tick del servidor.
     */
    public static void tickParticles(ServerLevel level, BlockPos portalCenter, Direction facing, boolean active) {
        Direction rightDir = facing.getOpposite().getClockWise();

        if (active) {
            for (int i = 0; i < 3; i++) {
                double offset = (level.random.nextDouble() - 0.5) * 6.5;
                double forward = (level.random.nextDouble() - 0.5) * 1.5;
                double px = portalCenter.getX() + 0.5 + rightDir.getStepX() * offset + facing.getStepX() * forward;
                double py = portalCenter.getY() - 4.5;
                double pz = portalCenter.getZ() + 0.5 + rightDir.getStepZ() * offset + facing.getStepZ() * forward;

                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 1, 0.1, 1.8, 0.1, 0.04);
                level.sendParticles(ParticleTypes.END_ROD, px, py + 0.5, pz, 1, 0.05, 1.2, 0.05, 0.05);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double radius = 3.2 + level.random.nextDouble() * 0.3;
                double ox = Math.cos(angle) * radius;
                double oy = Math.sin(angle) * radius;

                double px = portalCenter.getX() + 0.5 + rightDir.getStepX() * ox;
                double py = portalCenter.getY() + 0.5 + oy;
                double pz = portalCenter.getZ() + 0.5 + rightDir.getStepZ() * ox;

                level.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 1, 0.04, 0.04, 0.04, 0.01);
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, px, py, pz, 2, 0.08, 0.08, 0.08, 0.01);
            }
        }
    }

    private static void playBuildEffects(ServerLevel level, BlockPos centerPos, boolean active) {
        level.playSound(null, centerPos.getX(), centerPos.getY() + 7, centerPos.getZ(),
                active ? SoundEvents.BEACON_ACTIVATE : SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.2F, active ? 1.0F : 0.8F);

        if (active) {
            level.sendParticles(ParticleTypes.END_ROD,
                    centerPos.getX() + 0.5D, centerPos.getY() + 7.5D, centerPos.getZ() + 0.5D,
                    120, 3.2D, 3.2D, 3.2D, 0.08D);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    centerPos.getX() + 0.5D, centerPos.getY() + 7.5D, centerPos.getZ() + 0.5D,
                    90, 2.5D, 2.5D, 2.5D, 0.05D);
        } else {
            level.sendParticles(ParticleTypes.DRAGON_BREATH,
                    centerPos.getX() + 0.5D, centerPos.getY() + 7.5D, centerPos.getZ() + 0.5D,
                    70, 2.5D, 2.5D, 2.5D, 0.04D);
        }
    }
}
