package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.FruitLeavesBlock;
import com.soybe.awakeningsystem.block.LockedTreasureChestBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.NpcProfile;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Constructor Maestro Regional de Teyvat (Genshin Impact).
 * Diseña y levanta de forma procedural estructuras arquitectónicas canónicas para
 * cada una de las naciones de Teyvat (Mondstadt, Liyue, Inazuma, Sumeru, Fontaine, Espinadragón),
 * garantizando cimientos sólidos adaptativos que impiden construcciones flotantes o enterradas.
 */
public final class TeyvatRegionalBuilder {
    private TeyvatRegionalBuilder() {}

    private static volatile boolean arrivalPlazaBuilt = false;

    /**
     * Cimiento Adaptativo Universal:
     * Escanea el área [minX..maxX, minZ..maxZ] y proyecta columnas sólidas hacia abajo hasta
     * encontrar tierra/roca sólida verdadera, evitando que cualquier edificio quede flotando.
     * También excava suavemente cualquier saliente de colina que obstruya el interior.
     */
    public static void buildAdaptiveFoundation(ServerLevel level, int minX, int maxX, int minZ, int maxZ, int ySurface, BlockState surfaceBlock, BlockState fillerBlock) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = ySurface + 1; y <= ySurface + 16; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && (s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(BlockTags.FLOWERS) || s.is(BlockTags.DIRT) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.REPLACEABLE))) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                level.setBlock(new BlockPos(x, ySurface, z), surfaceBlock, 2);

                for (int y = ySurface - 1; y >= ySurface - 36; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS) && s.getFluidState().isEmpty() && !s.is(BlockTags.REPLACEABLE)) {
                        break;
                    }
                    level.setBlock(p, fillerBlock, 2);
                }
            }
        }
    }

    /**
     * 1. Plaza Monumental de Llegada a Teyvat (Spawn Point en 0, 0).
     * Plataforma ceremonial con la Estatua de los Siete de Anemo, Punto de Teletransporte
     * y mirador con vista hacia todas las naciones.
     */
    public static synchronized void ensureTeyvatArrivalPlaza(ServerLevel level) {
        if (arrivalPlazaBuilt) return;
        if (level.getServer() != null) {
            var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getServer());
            if (tData.isArrivalPlazaBuilt()) {
                arrivalPlazaBuilt = true;
                return;
            }
            tData.setArrivalPlazaBuilt(true);
        }
        arrivalPlazaBuilt = true;

        BlockPos center = com.soybe.awakeningsystem.TeyvatWorldManager.getSafeTeyvatSpawn(level);
        int cx = center.getX();
        int cy = center.getY() - 1;
        int cz = center.getZ();

        if (TeyvatStructureManager.placeNbtStructure(level, center, "teyvat_arrival_plaza", -5, -1, -5)) {
            arrivalPlazaBuilt = true;
            return;
        }

        int radius = 5;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= radius * radius) {
                    BlockPos floorPos = new BlockPos(cx + dx, cy, cz + dz);

                    for (int dy = 1; dy <= 7; dy++) {
                        BlockPos airP = floorPos.above(dy);
                        BlockState airState = level.getBlockState(airP);
                        if (!airState.isAir()) {
                            level.setBlock(airP, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }

                    BlockState floorState = (distSq >= 17) ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                            : ((distSq >= 9) ? Blocks.QUARTZ_BRICKS.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState());
                    level.setBlock(floorPos, floorState, 2);

                    for (int downY = 1; downY <= 14; downY++) {
                        BlockPos downPos = floorPos.below(downY);
                        BlockState ds = level.getBlockState(downPos);
                        if (ds.isSolid() && !ds.is(net.minecraft.tags.BlockTags.LEAVES) && !ds.is(net.minecraft.tags.BlockTags.LOGS) && ds.getFluidState().isEmpty()) {
                            break;
                        }
                        level.setBlock(downPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        int[][] lightPillars = {
                {3, 3}, {-3, 3}, {3, -3}, {-3, -3},
                {4, 0}, {-4, 0}, {0, 4}, {0, -4}
        };
        for (int[] p : lightPillars) {
            BlockPos base = new BlockPos(cx + p[0], cy + 1, cz + p[1]);
            level.setBlock(base, Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
            level.setBlock(base.above(1), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
            level.setBlock(base.above(2), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(base.above(3), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }

        TeyvatWorldGenManager.generateTeleportWaypoint(level, new BlockPos(cx, cy + 1, cz));

        arrivalPlazaBuilt = true;
    }

    /**
     * 2. Mansión y Viñedo del Amanecer (Dawn Winery Manor) en Mondstadt.
     * Complejo vinícola señorial de estilo alemán: paredes de piedra y madera noble,
     * techo de tejas de terracota, bodega de añejamiento con barriles, salón de catas y Diluc.
     */
    public static void buildDawnWineryManor(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        int halfW = 12;
        int halfL = 10;
        int height = 11;

        buildAdaptiveFoundation(level, cx - halfW - 2, cx + halfW + 2, cz - halfL - 2, cz + halfL + 2, cy,
                Blocks.COBBLESTONE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState());

        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfL; z <= halfL; z++) {
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }

        for (int y = 1; y <= 4; y++) {
            for (int x = -halfW; x <= halfW; x++) {
                for (int z = -halfL; z <= halfL; z++) {
                    if (Math.abs(x) == halfW || Math.abs(z) == halfL) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                        if (z == halfL && Math.abs(x) <= 1 && y <= 3) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        } else if (y == 2 && ((Math.abs(x) == 4 || Math.abs(x) == 8) || (Math.abs(z) == 4))) {
                            level.setBlock(p, Blocks.GLASS_PANE.defaultBlockState(), 2);
                        } else if (Math.abs(x) == halfW && Math.abs(z) == halfL) {
                            level.setBlock(p, Blocks.DARK_OAK_LOG.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfL; z <= halfL; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
            }
        }

        for (int y = 6; y <= 8; y++) {
            for (int x = -halfW; x <= halfW; x++) {
                for (int z = -halfL; z <= halfL; z++) {
                    if (Math.abs(x) == halfW || Math.abs(z) == halfL) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                        if (y == 7 && ((Math.abs(x) == 3 || Math.abs(x) == 7) || (Math.abs(z) == 5))) {
                            level.setBlock(p, Blocks.GLASS_PANE.defaultBlockState(), 2);
                        } else if (Math.abs(x) % 4 == 0 || Math.abs(z) % 4 == 0) {
                            level.setBlock(p, Blocks.DARK_OAK_LOG.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.WHITE_TERRACOTTA.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        for (int step = 0; step <= halfL; step++) {
            int currentY = cy + 9 + step;
            int zBound = halfL - step;
            for (int x = -halfW - 1; x <= halfW + 1; x++) {
                if (zBound >= 0) {
                    level.setBlock(new BlockPos(cx + x, currentY, cz + zBound), Blocks.BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                    level.setBlock(new BlockPos(cx + x, currentY, cz - zBound), Blocks.BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
                }
            }
        }

        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 2);
        }
        for (int bz = -halfL + 2; bz <= -halfL + 6; bz++) {
            level.setBlock(new BlockPos(cx - halfW + 2, cy + 1, cz + bz), Blocks.BARREL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - halfW + 2, cy + 2, cz + bz), Blocks.BARREL.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx - 3, cy + 4, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 4, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        for (int row = -2; row <= 2; row += 2) {
            for (int vz = halfL + 3; vz <= halfL + 8; vz++) {
                BlockPos vp = new BlockPos(cx + row * 3, cy + 1, cz + vz);
                level.setBlock(vp, Blocks.OAK_FENCE.defaultBlockState(), 2);
                level.setBlock(vp.above(), Blocks.OAK_LEAVES.defaultBlockState(), 2);
            }
        }

        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + halfL - 2), ModEntities.SERIES_NPC.get(), NpcProfile.DILUC);
    }

    /**
     * 3. Gran Santuario Narukami (Inazuma Grand Shrine).
     * Arquitectura sagrada tradicional sintoísta de Inazuma: Torii bermellón,
     * aleros curvos oscuros, Cerezos Sagrados Sakura y el Altar del Cerezo Sagrado.
     */
    public static void buildNarukamiGrandShrine(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        if (TeyvatStructureManager.placeNbtStructure(level, origin, "narukami_grand_shrine", -12, 0, -14)) {
            BlockPos altarPos = new BlockPos(cx, cy + 1, cz + 3);
            SeriesStructureBuilder.spawnNpcIfAbsent(level, altarPos.offset(0, 0, -2), ModEntities.SERIES_NPC.get(), NpcProfile.RAIDEN_SHOGUN);
            return;
        }

        int halfW = 8;
        int halfL = 8;

        buildAdaptiveFoundation(level, cx - halfW - 4, cx + halfW + 4, cz - halfL - 6, cz + halfL + 4, cy,
                Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());

        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfL; z <= halfL; z++) {
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.CHERRY_PLANKS.defaultBlockState(), 2);
            }
        }

        int[][] pillars = {{-halfW, -halfL}, {halfW, -halfL}, {-halfW, halfL}, {halfW, halfL}, {-3, -halfL}, {3, -halfL}};
        for (int[] p : pillars) {
            for (int y = 1; y <= 5; y++) {
                level.setBlock(new BlockPos(cx + p[0], cy + y, cz + p[1]), Blocks.RED_TERRACOTTA.defaultBlockState(), 2);
            }
        }

        for (int y = 1; y <= 4; y++) {
            for (int x = -halfW; x <= halfW; x++) {
                for (int z = -halfL; z <= halfL; z++) {
                    if (Math.abs(x) == halfW || Math.abs(z) == halfL) {
                        BlockPos pos = new BlockPos(cx + x, cy + y, cz + z);
                        if (z == -halfL && Math.abs(x) <= 2) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        } else if (y >= 2 && y <= 3 && Math.abs(x) % 2 == 0) {
                            level.setBlock(pos, Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, Blocks.CHERRY_PLANKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        for (int step = 0; step <= halfW + 1; step++) {
            int yRoof = cy + 5 + (step / 2);
            int bound = halfW + 2 - step;
            if (bound >= 0) {
                for (int x = -bound; x <= bound; x++) {
                    level.setBlock(new BlockPos(cx + x, yRoof, cz - bound), Blocks.NETHER_BRICK_SLAB.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + x, yRoof, cz + bound), Blocks.NETHER_BRICK_SLAB.defaultBlockState(), 2);
                }
                for (int z = -bound; z <= bound; z++) {
                    level.setBlock(new BlockPos(cx - bound, yRoof, cz + z), Blocks.NETHER_BRICK_SLAB.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + bound, yRoof, cz + z), Blocks.NETHER_BRICK_SLAB.defaultBlockState(), 2);
                }
            }
        }

        int toriiZ = cz - halfL - 4;
        for (int ty = 1; ty <= 5; ty++) {
            level.setBlock(new BlockPos(cx - 3, cy + ty, toriiZ), Blocks.RED_CONCRETE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 3, cy + ty, toriiZ), Blocks.RED_CONCRETE.defaultBlockState(), 2);
        }
        for (int tx = -4; tx <= 4; tx++) {
            level.setBlock(new BlockPos(cx + tx, cy + 5, toriiZ), Blocks.RED_CONCRETE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + tx, cy + 6, toriiZ), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState(), 2);
        }

        BlockPos altarPos = new BlockPos(cx, cy + 1, cz + 3);
        level.setBlock(altarPos, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(altarPos.above(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(altarPos.above(2), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        SeriesStructureBuilder.spawnNpcIfAbsent(level, altarPos.offset(0, 0, -2), ModEntities.SERIES_NPC.get(), NpcProfile.RAIDEN_SHOGUN);
    }

    /**
     * 4. Choza Mística y Árbol de Aranara (Sumeru Dharma Forest).
     * Refugio botánico con setas gigantes luminiscentes, madera de selva y enredaderas.
     */
    public static void buildAranaraTreehouse(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        if (TeyvatStructureManager.placeNbtStructure(level, origin, "aranara_treehouse", -6, 0, -6)) {
            BlockPos chestPos = new BlockPos(cx, cy + 1, cz - 2);
            BlockEntity be = level.getBlockEntity(chestPos);
            if (be instanceof Container c) {
                c.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), 60));
                c.setItem(1, SystemItems.coin(250));
                c.setItem(2, new ItemStack(Items.EMERALD, 4));
            }
            SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz), ModEntities.SERIES_NPC.get(), NpcProfile.HOSHIMI_MIYABI);
            return;
        }

        buildAdaptiveFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy,
                Blocks.MOSS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState());

        for (int y = 1; y <= 7; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                        level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.JUNGLE_WOOD.defaultBlockState(), 2);
                    } else {
                        level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        level.setBlock(new BlockPos(cx, cy + 1, cz + 3), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 3), Blocks.AIR.defaultBlockState(), 2);

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 8, cz + z), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + x, cy + 9, cz + z), Blocks.JUNGLE_LEAVES.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.SHROOMLIGHT.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy + 1, cz - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container c) {
            c.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), 60));
            c.setItem(1, SystemItems.coin(250));
            c.setItem(2, new ItemStack(Items.EMERALD, 4));
        }

        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz), ModEntities.SERIES_NPC.get(), NpcProfile.HOSHIMI_MIYABI);
    }

    /**
     * 5. Altar Piramidal del Rey Deshret (Sumeru Hypostyle Desert).
     * Santuario de arenisca dorada cincelada con fuego sagrado y reliquias de las arenas.
     */
    public static void buildDesertPyramidAltar(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        if (TeyvatStructureManager.placeNbtStructure(level, origin, "desert_pyramid_altar", -7, 0, -7)) {
            int topY = cy + 5;
            BlockPos chestPos = new BlockPos(cx, topY, cz + 1);
            BlockEntity be = level.getBlockEntity(chestPos);
            if (be instanceof Container c) {
                c.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), 80));
                c.setItem(1, SystemItems.coin(300));
                c.setItem(2, new ItemStack(Items.GOLD_INGOT, 8));
            }
            return;
        }

        int maxR = 7;
        buildAdaptiveFoundation(level, cx - maxR, cx + maxR, cz - maxR, cz + maxR, cy,
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState());

        for (int lvl = 0; lvl <= 4; lvl++) {
            int r = maxR - lvl;
            int y = cy + lvl;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        level.setBlock(new BlockPos(cx + x, y, cz + z), Blocks.CUT_SANDSTONE.defaultBlockState(), 2);
                    } else if (lvl == 0) {
                        level.setBlock(new BlockPos(cx + x, y, cz + z), Blocks.CHISELED_SANDSTONE.defaultBlockState(), 2);
                    }
                }
            }
        }

        int topY = cy + 5;
        level.setBlock(new BlockPos(cx, topY, cz), Blocks.CHISELED_SANDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, topY + 1, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, topY, cz - 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, topY, cz - 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, topY, cz + 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, topY, cz + 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, topY, cz + 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container c) {
            c.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), 80));
            c.setItem(1, SystemItems.coin(300));
            c.setItem(2, new ItemStack(Items.GOLD_INGOT, 8));
        }
    }

    /**
     * 6. Altar Glacial del Árbol del Frío Eterno (Dragonspine Frost Altar).
     * Ruina gélida de piedra ancestral y hielo compacto con brisas de subcero.
     */
    public static void buildDragonspineFrostAltar(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        if (TeyvatStructureManager.placeNbtStructure(level, origin, "dragonspine_frost_altar", -6, 0, -6)) {
            SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 1, cz + 1), ModEntities.SERIES_NPC.get(), NpcProfile.GANYU);
            return;
        }

        buildAdaptiveFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy,
                Blocks.PACKED_ICE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState());

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                int distSq = x * x + z * z;
                if (distSq <= 25) {
                    level.setBlock(new BlockPos(cx + x, cy, cz + z), distSq % 2 == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState(), 2);
                }
            }
        }

        int[][] icePillars = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] ip : icePillars) {
            for (int h = 1; h <= 4; h++) {
                level.setBlock(new BlockPos(cx + ip[0], cy + h, cz + ip[1]), Blocks.BLUE_ICE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + ip[0], cy + 5, cz + ip[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 1, cz + 1), ModEntities.SERIES_NPC.get(), NpcProfile.GANYU);
    }



    /**
     * Campamento Tribal Hilichurl Canónico de Teyvat:
     * - Suelo apisonado con senderos de tierra estéril y grava.
     * - Choza de paja y madera tribal con pieles y lecho.
     * - Torre de vigía con plataforma elevada de madera y escalera.
     * - Hoguera central de cocina rodeada de troncos para sentarse.
     * - Almacén de botín con Cofre Cerrado, Bloque de Mora macizo y Vasijas de Tesoro de Teyvat.
     * - Defensores tribales: Mitachurl con escudo, Hilichurl con escudo, Hilichurl Berserker y Hilichurl Tirador.
     */
    private static void ensureCampFoundation(ServerLevel level, BlockPos pos) {
        for (int dy = 1; dy <= 12; dy++) {
            BlockPos below = pos.below(dy);
            BlockState bs = level.getBlockState(below);
            if (bs.isSolid()) break;
            level.setBlock(below, Blocks.DIRT.defaultBlockState(), 2);
        }
    }

    public static void buildHilichurlCamp(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        if (TeyvatStructureManager.placeNbtStructure(level, origin, "hilichurl_camp", -10, 0, -10)) {
            spawnHilichurlDefenders(level, origin);
            return;
        }

        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                int distSq = x * x + z * z;
                if (distSq <= 100) {
                    BlockPos p = new BlockPos(cx + x, cy - 1, cz + z);
                    for (int dy = 0; dy <= 6; dy++) {
                        BlockPos down = p.below(dy);
                        if (level.getBlockState(down).isSolid()) break;
                        level.setBlock(down, Blocks.DIRT.defaultBlockState(), 2);
                    }
                    BlockState floor = (distSq < 16) ? Blocks.COARSE_DIRT.defaultBlockState() :
                            ((x + z) % 3 == 0) ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
                    level.setBlock(p, floor, 2);
                    for (int dy = 0; dy <= 7; dy++) {
                        level.setBlock(p.above(1 + dy), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        level.setBlock(new BlockPos(cx, cy, cz), Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy, cz), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy, cz), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz - 2), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz + 2), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);

        int hx = cx - 6;
        int hz = cz - 6;
        for (int dy = 0; dy <= 3; dy++) {
            level.setBlock(new BlockPos(hx - 2, cy + dy, hz - 2), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(hx + 2, cy + dy, hz - 2), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(hx - 2, cy + dy, hz + 2), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(hx + 2, cy + dy, hz + 2), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
        }
        ensureCampFoundation(level, new BlockPos(hx - 2, cy, hz - 2));
        ensureCampFoundation(level, new BlockPos(hx + 2, cy, hz - 2));
        ensureCampFoundation(level, new BlockPos(hx - 2, cy, hz + 2));
        ensureCampFoundation(level, new BlockPos(hx + 2, cy, hz + 2));

        for (int x = -1; x <= 1; x++) {
            BlockPos f1 = new BlockPos(hx + x, cy, hz - 2);
            BlockPos f2 = new BlockPos(hx + x, cy, hz + 2);
            BlockPos f3 = new BlockPos(hx - 2, cy, hz + x);
            level.setBlock(f1, Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            level.setBlock(f2, Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            level.setBlock(f3, Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            ensureCampFoundation(level, f1);
            ensureCampFoundation(level, f2);
            ensureCampFoundation(level, f3);
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(new BlockPos(hx + x, cy + 4, hz + z), Blocks.HAY_BLOCK.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(hx, cy + 5, hz), Blocks.HAY_BLOCK.defaultBlockState(), 2);

        int tx = cx + 6;
        int tz = cz + 6;
        for (int dy = 0; dy <= 5; dy++) {
            level.setBlock(new BlockPos(tx - 1, cy + dy, tz - 1), Blocks.OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(tx + 1, cy + dy, tz - 1), Blocks.OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(tx - 1, cy + dy, tz + 1), Blocks.OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(tx + 1, cy + dy, tz + 1), Blocks.OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(tx, cy + dy, tz - 1), Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, net.minecraft.core.Direction.SOUTH), 2);
        }
        ensureCampFoundation(level, new BlockPos(tx - 1, cy, tz - 1));
        ensureCampFoundation(level, new BlockPos(tx + 1, cy, tz - 1));
        ensureCampFoundation(level, new BlockPos(tx - 1, cy, tz + 1));
        ensureCampFoundation(level, new BlockPos(tx + 1, cy, tz + 1));

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(new BlockPos(tx + x, cy + 6, tz + z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    level.setBlock(new BlockPos(tx + x, cy + 7, tz + z), Blocks.OAK_FENCE.defaultBlockState(), 2);
                }
            }
        }

        int bx = cx + 5;
        int bz = cz - 5;
        BlockPos chestPos = new BlockPos(bx, cy, bz);
        BlockPos moraPos = new BlockPos(bx + 1, cy, bz);
        BlockPos j1 = new BlockPos(bx - 1, cy, bz);
        BlockPos j2 = new BlockPos(bx, cy, bz + 1);
        BlockPos j3 = new BlockPos(cx - 3, cy, cz + 4);
        BlockPos j4 = new BlockPos(cx + 3, cy, cz - 3);

        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState(), 2);
        level.setBlock(moraPos, SystemBlocks.MORA_BLOCK.get().defaultBlockState(), 2);
        level.setBlock(j1, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);
        level.setBlock(j2, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);
        level.setBlock(j3, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);
        level.setBlock(j4, SystemBlocks.TEYVAT_JAR.get().defaultBlockState(), 2);

        ensureCampFoundation(level, chestPos);
        ensureCampFoundation(level, moraPos);
        ensureCampFoundation(level, j1);
        ensureCampFoundation(level, j2);
        ensureCampFoundation(level, j3);
        ensureCampFoundation(level, j4);

        spawnHilichurlDefenders(level, origin);
    }

    public static void spawnHilichurlDefenders(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();
        int bx = cx + 5;
        int bz = cz - 5;
        int tx = cx + 6;
        int tz = cz + 6;

        com.soybe.awakeningsystem.entity.MitachurlEntity mitachurl = new com.soybe.awakeningsystem.entity.MitachurlEntity(com.soybe.awakeningsystem.entity.ModEntities.MITACHURL.get(), level);
        mitachurl.moveTo(bx + 0.5D, cy + 1, bz + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(mitachurl);

        com.soybe.awakeningsystem.entity.HilichurlEntity shooter = new com.soybe.awakeningsystem.entity.HilichurlEntity(com.soybe.awakeningsystem.entity.ModEntities.HILICHURL.get(), level);
        shooter.setRole(com.soybe.awakeningsystem.entity.HilichurlEntity.HilichurlRole.SHOOTER);
        shooter.moveTo(tx + 0.5D, cy + 7, tz + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(shooter);

        com.soybe.awakeningsystem.entity.HilichurlEntity guard = new com.soybe.awakeningsystem.entity.HilichurlEntity(com.soybe.awakeningsystem.entity.ModEntities.HILICHURL.get(), level);
        guard.setRole(com.soybe.awakeningsystem.entity.HilichurlEntity.HilichurlRole.GUARD);
        guard.moveTo(cx - 3 + 0.5D, cy, cz - 1 + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(guard);

        com.soybe.awakeningsystem.entity.HilichurlEntity berserker = new com.soybe.awakeningsystem.entity.HilichurlEntity(com.soybe.awakeningsystem.entity.ModEntities.HILICHURL.get(), level);
        berserker.setRole(com.soybe.awakeningsystem.entity.HilichurlEntity.HilichurlRole.BERSERKER);
        berserker.moveTo(cx + 2 + 0.5D, cy, cz + 3 + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(berserker);
    }

    public static final BlockPos MONDSTADT_POS = new BlockPos(150, 78, 120);
    public static final BlockPos LIYUE_POS = new BlockPos(2519, 164, 6063);
    public static final BlockPos INAZUMA_POS = new BlockPos(13554, 75, 9261);
    public static final BlockPos SUMERU_POS = new BlockPos(6800, 200, 69800);
    public static final BlockPos FONTAINE_POS = new BlockPos(-450, 68, -450);

    private static volatile boolean mondstadtCitadelBuilt = false;
    private static volatile boolean liyueTerraceBuilt = false;
    private static volatile boolean inazumaTenshukakuBuilt = false;
    private static volatile boolean sumeruAcademiyaBuilt = false;
    private static volatile boolean fontainePalaisBuilt = false;

    public static void resetCapitalsBuiltFlags() {
        mondstadtCitadelBuilt = false;
        liyueTerraceBuilt = false;
        inazumaTenshukakuBuilt = false;
        sumeruAcademiyaBuilt = false;
        fontainePalaisBuilt = false;
    }

    /**
     * 1. CIUDADELA DE FAVONIUS Y PLAZA DE LA LIBERTAD (MONDSTADT CAPITAL).
     * Eliminado permanentemente: Las capitales no emplean estructuras procedurales sinteticas.
     */
    public static synchronized void ensureMondstadtCapitalCitadel(ServerLevel level) {
        // No-op intencional
    }

    /**
     * 2. TERRAZA YUJING Y PABELLON YUEHAI (LIYUE CAPITAL).
     * Eliminado permanentemente: Las capitales no emplean estructuras procedurales sinteticas.
     */
    public static synchronized void ensureLiyueYujingTerrace(ServerLevel level) {
        // No-op intencional
    }

    /**
     * 3. PALACIO TENSHUKAKU Y JARDIN ZEN (INAZUMA CAPITAL).
     * Eliminado permanentemente: Las capitales no emplean estructuras procedurales sinteticas.
     */
    public static synchronized void ensureInazumaTenshukaku(ServerLevel level) {
        // No-op intencional
    }

    /**
     * 4. LA GRAN ACADEMIYA Y EL ARBOL DIVINO (SUMERU CAPITAL).
     * Eliminado permanentemente: Las capitales no emplean estructuras procedurales sinteticas.
     */
    public static synchronized void ensureSumeruAcademiya(ServerLevel level) {
        // No-op intencional
    }

    /**
     * 5. PALAIS MERMONIA Y FUENTE DE LUCINE (FONTAINE CAPITAL).
     * Eliminado permanentemente: Las capitales no emplean estructuras procedurales sinteticas.
     */
    public static synchronized void ensureFontainePalais(ServerLevel level) {
        // No-op intencional
    }

    private static volatile boolean cleanupDone = false;

    /**
     * Purga y sanea por completo cualquier residuo de estructuras procedurales antiguas en las 5 capitales
     * de Teyvat (Mondstadt, Liyue, Inazuma, Sumeru y Fontaine) y en el punto de aparicion original,
     * restaurando el relieve natural a cesped y aire, protegiendo waypoints canonicos y estatuas de los Siete.
     */
    public static void cleanupProceduralCitadelAndPlaza(ServerLevel level) {
        if (cleanupDone) return;
        if (level.getServer() != null) {
            var tData = TeyvatWorldGenManager.TeyvatFeaturesData.get(level.getServer());
            if (tData.isProceduralCapitalsCleaned()) {
                cleanupDone = true;
                return;
            }
            tData.setProceduralCapitalsCleaned(true);
            tData.setProceduralCitadelCleaned(true);
        }
        cleanupDone = true;
        try {
            // 1. Mondstadt (150, 78, 120)
            cleanCapitalZone(level, MONDSTADT_POS, -35, 35, -45, 35, 25, 6);

            // 2. Liyue (2519, 164, 6063)
            cleanCapitalZone(level, LIYUE_POS, -32, 32, -32, 35, 25, 6);

            // 3. Inazuma (13554, 75, 9261)
            cleanCapitalZone(level, INAZUMA_POS, -32, 32, -32, 35, 28, 6);

            // 4. Sumeru (6800, 200, 69800)
            cleanCapitalZone(level, SUMERU_POS, -32, 32, -32, 35, 32, 6);

            // 5. Fontaine (-450, 68, -450)
            cleanCapitalZone(level, FONTAINE_POS, -32, 32, -32, 35, 25, 6);

            // 6. Plataforma antigua de spawn en (0, 0)
            cleanCapitalZone(level, BlockPos.ZERO, -15, 15, -15, 15, 15, 4);

            AwakeningSystemMod.LOGGER.info("[Teyvat Cleaner] Las 5 capitales procedurales y residuos antiguos han sido completamente purgados y el terreno restaurado a cesped natural.");
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("[Teyvat Cleaner] Error durante la purga de estructuras procedurales de las capitales", e);
        }
    }

    private static void cleanCapitalZone(ServerLevel level, BlockPos center, int minDx, int maxDx, int minDz, int maxDz, int heightUp, int depthDown) {
        if (level == null || center == null) return;
        int cx = center.getX();
        int cz = center.getZ();

        int minChunkX = (cx + minDx) >> 4;
        int maxChunkX = (cx + maxDx) >> 4;
        int minChunkZ = (cz + minDz) >> 4;
        int maxChunkZ = (cz + maxDz) >> 4;

        for (int chX = minChunkX; chX <= maxChunkX; chX++) {
            for (int chZ = minChunkZ; chZ <= maxChunkZ; chZ++) {
                level.getChunk(chX, chZ, ChunkStatus.FULL, true);
            }
        }

        int cy = level.getHeight(Heightmap.Types.OCEAN_FLOOR, cx, cz);
        if (cy < 50) cy = (center.getY() > 50) ? center.getY() : 64;

        AABB box = new AABB(cx + minDx, cy - depthDown, cz + minDz, cx + maxDx, cy + heightUp, cz + maxDz);
        for (var entity : level.getEntitiesOfClass(SeriesNpcEntity.class, box)) {
            entity.discard();
        }

        boolean isSumeru = center.equals(SUMERU_POS);
        boolean isInazuma = center.equals(INAZUMA_POS);

        for (int x = minDx; x <= maxDx; x++) {
            for (int z = minDz; z <= maxDz; z++) {
                int colX = cx + x;
                int colZ = cz + z;
                for (int y = cy + heightUp; y >= cy - depthDown; y--) {
                    BlockPos p = new BlockPos(colX, y, colZ);
                    BlockState s = level.getBlockState(p);

                    boolean remove = isProceduralDebris(s, y, cy);
                    if (!remove && y > cy) {
                        if (isSumeru && (s.is(Blocks.JUNGLE_WOOD) || s.is(Blocks.AZALEA_LEAVES) || s.is(Blocks.FLOWERING_AZALEA_LEAVES))) {
                            remove = true;
                        } else if (isInazuma && (s.is(Blocks.CHERRY_LEAVES) || s.is(Blocks.CHERRY_LOG))) {
                            remove = true;
                        }
                    }

                    if (remove) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, colX, colZ);
                if (topY > 40) {
                    BlockPos ground = new BlockPos(colX, topY - 1, colZ);
                    BlockState gs = level.getBlockState(ground);
                    if (gs.is(Blocks.DIRT) || gs.is(Blocks.STONE) || gs.is(Blocks.COBBLESTONE)) {
                        level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static boolean isProceduralDebris(BlockState s, int y, int cy) {
        if (s == null || s.isAir()) return false;
        if (s.is(SystemBlocks.CELESTIAL_WAYPOINT.get()) || s.is(SystemBlocks.STATUE_OF_THE_SEVEN.get())) return false;
        if (s.is(Blocks.CHEST) || s.is(Blocks.BARREL) || s.is(Blocks.BEDROCK)) return false;

        if (y > cy && (s.is(Blocks.WATER) || s.is(Blocks.LILY_PAD))) {
            return true;
        }

        return s.is(Blocks.STONE_BRICKS) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.POLISHED_ANDESITE)
                || s.is(Blocks.STONE_BRICK_SLAB) || s.is(Blocks.STONE_BRICK_STAIRS)
                || s.is(Blocks.SPRUCE_PLANKS) || s.is(Blocks.SPRUCE_STAIRS) || s.is(Blocks.DARK_OAK_PLANKS)
                || s.is(Blocks.WHITE_WOOL) || s.is(Blocks.OAK_FENCE) || s.is(Blocks.GRINDSTONE)
                || s.is(Blocks.LIGHTNING_ROD) || s.is(Blocks.RED_CARPET) || s.is(Blocks.QUARTZ_STAIRS)
                || s.is(Blocks.SMOOTH_QUARTZ) || s.is(Blocks.SMOOTH_QUARTZ_SLAB) || s.is(Blocks.CHISELED_STONE_BRICKS)
                || s.is(Blocks.SEA_LANTERN) || s.is(Blocks.RED_BED)
                || s.is(Blocks.POLISHED_GRANITE) || s.is(Blocks.RED_SANDSTONE) || s.is(Blocks.DARK_PRISMARINE)
                || s.is(Blocks.DARK_PRISMARINE_STAIRS) || s.is(Blocks.DARK_PRISMARINE_SLAB) || s.is(Blocks.CRIMSON_PLANKS)
                || s.is(Blocks.CRIMSON_STAIRS) || s.is(Blocks.CRIMSON_FENCE) || s.is(Blocks.CRIMSON_STEM)
                || s.is(Blocks.RED_STAINED_GLASS_PANE) || s.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
                || s.is(Blocks.DEEPSLATE_BRICKS) || s.is(Blocks.DEEPSLATE_TILE_STAIRS) || s.is(Blocks.DEEPSLATE_TILE_SLAB)
                || s.is(Blocks.MUD_BRICKS) || s.is(Blocks.BOOKSHELF) || s.is(Blocks.CHISELED_BOOKSHELF)
                || s.is(Blocks.WEATHERED_COPPER) || s.is(Blocks.OXIDIZED_COPPER) || s.is(Blocks.OXIDIZED_CUT_COPPER_STAIRS)
                || s.is(Blocks.QUARTZ_BRICKS) || s.is(Blocks.QUARTZ_PILLAR) || s.is(Blocks.POLISHED_DIORITE)
                || s.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE) || s.is(Blocks.END_ROD) || s.is(Blocks.PURPLE_CARPET)
                || s.is(Blocks.SMOOTH_STONE) || s.is(Blocks.POLISHED_DEEPSLATE_WALL) || s.is(Blocks.SOUL_LANTERN)
                || s.is(Blocks.GOLD_BLOCK) || s.is(Blocks.LECTERN);
    }

    public static boolean canPlaceTree(LevelAccessor level, BlockPos origin, int clearRadius, int height) {
        return canPlaceTree(level, origin, clearRadius, height, false);
    }

    public static boolean canPlaceTree(LevelAccessor level, BlockPos origin, int clearRadius, int height, boolean fromSapling) {
        BlockState ground = level.getBlockState(origin.below());
        if (!ground.is(BlockTags.DIRT) && !ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.MOSS_BLOCK)
                && !ground.is(BlockTags.SAND) && !ground.is(Blocks.PODZOL) && !ground.is(Blocks.FARMLAND)
                && !ground.is(Blocks.STONE) && !ground.is(Blocks.SANDSTONE) && !ground.is(Blocks.COARSE_DIRT)
                && !ground.is(Blocks.NETHERRACK) && !ground.is(Blocks.CRIMSON_NYLIUM) && !ground.is(Blocks.WARPED_NYLIUM)
                && !ground.is(Blocks.SOUL_SOIL) && !ground.is(Blocks.SOUL_SAND) && !ground.is(Blocks.BASALT) && !ground.is(Blocks.BLACKSTONE)) {
            return false;
        }

        int checkHeight = Math.min(height, 8);
        for (int dy = 1; dy <= checkHeight; dy++) {
            BlockPos p = origin.above(dy);
            BlockState s = level.getBlockState(p);
            if (!s.isAir() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.REPLACEABLE) && !s.is(BlockTags.FLOWERS)) {
                return false;
            }
        }

        if (!fromSapling) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos side = origin.offset(dx, 1, dz);
                    if (level.getBlockState(side).is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Coloca un tronco con orientación adecuada para ramas orgánicas (eje X, Y o Z)
     */
    public static void placeOrientedLog(LevelAccessor level, BlockPos pos, BlockState logState, Direction.Axis axis) {
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.is(BlockTags.REPLACEABLE) || current.is(BlockTags.LEAVES) || current.is(BlockTags.FLOWERS) || current.is(BlockTags.LOGS)) {
            BlockState stateToPlace = logState;
            if (stateToPlace.hasProperty(RotatedPillarBlock.AXIS)) {
                stateToPlace = stateToPlace.setValue(RotatedPillarBlock.AXIS, axis);
            }
            level.setBlock(pos, stateToPlace, 2);
        }
    }

    /**
     * Traza una rama tridimensional continua entre dos puntos con corteza alineada a la trayectoria
     */
    public static void placeBranch(LevelAccessor level, BlockPos from, BlockPos to, BlockState logState) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            placeOrientedLog(level, from, logState, Direction.Axis.Y);
            return;
        }

        float stepX = (float) dx / steps;
        float stepY = (float) dy / steps;
        float stepZ = (float) dz / steps;

        float cx = from.getX() + 0.5f;
        float cy = from.getY() + 0.5f;
        float cz = from.getZ() + 0.5f;

        for (int i = 0; i <= steps; i++) {
            BlockPos current = new BlockPos((int) Math.floor(cx), (int) Math.floor(cy), (int) Math.floor(cz));
            Direction.Axis axis;
            int ax = Math.abs(dx);
            int ay = Math.abs(dy);
            int az = Math.abs(dz);
            if (ax >= ay && ax >= az) {
                axis = Direction.Axis.X;
            } else if (az >= ax && az >= ay) {
                axis = Direction.Axis.Z;
            } else {
                axis = Direction.Axis.Y;
            }
            placeOrientedLog(level, current, logState, axis);
            cx += stepX;
            cy += stepY;
            cz += stepZ;
        }
    }

    /**
     * Coloca un cúmulo esférico/elipsoidal de follaje 3D denso y orgánico (nunca un disco plano)
     */
    public static void placeLeafCluster(LevelAccessor level, BlockPos center, double radiusH, double radiusV, BlockState leaves) {
        BlockState leafState = leaves;
        if (leafState.hasProperty(LeavesBlock.PERSISTENT)) {
            leafState = leafState.setValue(LeavesBlock.PERSISTENT, false);
        }
        int ceilH = (int) Math.ceil(radiusH);
        int ceilV = (int) Math.ceil(radiusV);

        for (int dx = -ceilH; dx <= ceilH; dx++) {
            for (int dz = -ceilH; dz <= ceilH; dz++) {
                for (int dy = -ceilV; dy <= ceilV; dy++) {
                    double normDist = (dx * dx) / (radiusH * radiusH)
                                    + (dy * dy) / (radiusV * radiusV)
                                    + (dz * dz) / (radiusH * radiusH);
                    if (normDist <= 1.05) {
                        BlockPos p = center.offset(dx, dy, dz);
                        BlockState cur = level.getBlockState(p);
                        if (cur.isAir() || cur.is(BlockTags.REPLACEABLE) || cur.is(BlockTags.LEAVES) || cur.is(BlockTags.FLOWERS)) {
                            BlockState finalState = leafState;
                            if (finalState.hasProperty(FruitLeavesBlock.HAS_FRUIT)) {
                                boolean isOuterOrLower = (normDist >= 0.45 && dy <= 0) || (normDist >= 0.75);
                                if (isOuterOrLower && level.getRandom().nextFloat() < 0.40F) {
                                    finalState = finalState.setValue(FruitLeavesBlock.HAS_FRUIT, true);
                                }
                            }
                            level.setBlock(p, finalState, 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Coloca zarcillos colgantes de hojas para árboles selváticos o místicos
     */
    public static void placeHangingTendril(LevelAccessor level, BlockPos start, int length, BlockState leaves) {
        BlockState leafState = leaves;
        if (leafState.hasProperty(LeavesBlock.PERSISTENT)) {
            leafState = leafState.setValue(LeavesBlock.PERSISTENT, false);
        }
        for (int i = 0; i < length; i++) {
            BlockPos p = start.below(i);
            BlockState cur = level.getBlockState(p);
            if (cur.isAir() || cur.is(BlockTags.REPLACEABLE)) {
                level.setBlock(p, leafState, 2);
            } else {
                break;
            }
        }
    }

    /**
     * Raíces basales ensanchadas para eliminar el aspecto de "poste recto"
     */
    public static void placeRootFlares(LevelAccessor level, BlockPos origin, BlockState logState, int height) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos rootPos = origin.relative(dir);
            BlockState cur = level.getBlockState(rootPos);
            if (cur.isAir() || cur.is(BlockTags.REPLACEABLE) || cur.is(BlockTags.LEAVES) || cur.is(BlockTags.FLOWERS)) {
                placeOrientedLog(level, rootPos, logState, Direction.Axis.Y);
            }
            BlockPos belowRoot = rootPos.below();
            if (level.getBlockState(belowRoot).isAir()) {
                placeOrientedLog(level, belowRoot, logState, Direction.Axis.Y);
            }
            if (height >= 2) {
                BlockPos rootUp = rootPos.above();
                if (level.getBlockState(rootUp).isAir()) {
                    placeOrientedLog(level, rootUp, logState, Direction.Axis.Y);
                }
            }
        }
    }

    /**
     * 1. Árbol de Cuihua (Mondstadt / Bishui Plains).
     * Árbol frutal redondeado con frutos cosechables de Fruta del Ocaso (Sunsettias).
     */
    public static boolean generateCuihuaTree(LevelAccessor level, BlockPos origin, RandomSource rand) {
        return generateCuihuaTree(level, origin, rand, false);
    }

    public static boolean generateCuihuaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        if (rand.nextFloat() < 0.35F) {
            return generateLargeCuihuaTree(level, origin, rand, fromSapling);
        }
        return generateStandardCuihua(level, origin, rand, fromSapling);
    }

    public static boolean generateStandardCuihua(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 6 + rand.nextInt(3);
        if (!canPlaceTree(level, origin, 3, height + 4, fromSapling)) return false;

        BlockState log = SystemBlocks.CUIHUA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.CUIHUA_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);

        int forkY = height - 3;
        for (int dy = 0; dy <= forkY; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        BlockPos forkCenter = origin.above(forkY);
        BlockPos tip1 = forkCenter.offset(2, 2, 1);
        BlockPos tip2 = forkCenter.offset(-2, 2, 1);
        BlockPos tip3 = forkCenter.offset(0, 2, -2);
        BlockPos tip4 = forkCenter.offset(1, 2, 2);

        placeBranch(level, forkCenter, tip1, log);
        placeBranch(level, forkCenter, tip2, log);
        placeBranch(level, forkCenter, tip3, log);
        placeBranch(level, forkCenter, tip4, log);

        placeLeafCluster(level, tip1, 2.6, 2.0, leaves);
        placeLeafCluster(level, tip2, 2.6, 2.0, leaves);
        placeLeafCluster(level, tip3, 2.6, 2.0, leaves);
        placeLeafCluster(level, tip4, 2.6, 2.0, leaves);

        placeLeafCluster(level, origin.above(height), 3.4, 2.4, leaves);
        placeLeafCluster(level, origin.above(height + 1), 2.2, 1.4, leaves);

        return true;
    }

    /**
     * Gran Árbol Frutal de Cuihua (Variante Colosal de Huerto Sagrado).
     * Tronco macizo 2x2, amplias raíces basales, 6 ramas maestras y follaje cargado de Frutas del Ocaso.
     */
    public static boolean generateLargeCuihuaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 12 + rand.nextInt(4);
        if (!canPlaceTree(level, origin, 5, height + 5, fromSapling)) {
            return generateStandardCuihua(level, origin, rand, fromSapling);
        }

        BlockState log = SystemBlocks.CUIHUA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.CUIHUA_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 2);
        placeRootFlares(level, origin.offset(1, 0, 1), log, 2);

        int forkY = height - 4;
        for (int dy = 0; dy <= forkY; dy++) {
            placeOrientedLog(level, origin.offset(0, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(0, dy, 1), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 1), log, Direction.Axis.Y);
        }

        BlockPos forkCenter = origin.offset(0, forkY, 0);
        BlockPos b1 = forkCenter.offset(4, 3, 1);
        BlockPos b2 = forkCenter.offset(-4, 3, 1);
        BlockPos b3 = forkCenter.offset(1, 3, -4);
        BlockPos b4 = forkCenter.offset(1, 3, 4);
        BlockPos b5 = forkCenter.offset(3, 4, -3);
        BlockPos b6 = forkCenter.offset(-3, 4, 3);

        placeBranch(level, forkCenter.offset(1, 0, 0), b1, log);
        placeBranch(level, forkCenter.offset(0, 0, 0), b2, log);
        placeBranch(level, forkCenter.offset(0, 0, 0), b3, log);
        placeBranch(level, forkCenter.offset(1, 0, 1), b4, log);
        placeBranch(level, forkCenter.offset(1, 0, 0), b5, log);
        placeBranch(level, forkCenter.offset(0, 0, 1), b6, log);

        BlockPos sub1 = b1.offset(1, -1, 1);
        BlockPos sub2 = b2.offset(-1, -1, -1);
        BlockPos sub3 = b3.offset(-1, -1, -1);
        BlockPos sub4 = b4.offset(1, -1, 1);

        placeBranch(level, b1, sub1, log);
        placeBranch(level, b2, sub2, log);
        placeBranch(level, b3, sub3, log);
        placeBranch(level, b4, sub4, log);

        placeLeafCluster(level, b1, 3.4, 2.2, leaves);
        placeLeafCluster(level, b2, 3.4, 2.2, leaves);
        placeLeafCluster(level, b3, 3.4, 2.2, leaves);
        placeLeafCluster(level, b4, 3.4, 2.2, leaves);
        placeLeafCluster(level, b5, 3.2, 2.0, leaves);
        placeLeafCluster(level, b6, 3.2, 2.0, leaves);

        placeLeafCluster(level, sub1, 2.8, 1.8, leaves);
        placeLeafCluster(level, sub2, 2.8, 1.8, leaves);
        placeLeafCluster(level, sub3, 2.8, 1.8, leaves);
        placeLeafCluster(level, sub4, 2.8, 1.8, leaves);

        BlockPos topCenter = origin.offset(0, height, 0);
        placeLeafCluster(level, topCenter, 4.6, 2.8, leaves);
        placeLeafCluster(level, topCenter.above(2), 3.2, 1.8, leaves);

        return true;
    }

    /**
     * 2. Árbol de Adhigama (Selva Tropical de Sumeru / Dharma Forest).
     * Árbol colosal de selva con raíces tabulares gigantescas, dosel tropical escalonado y lianas.
     */
    public static boolean generateAdhigamaTree(LevelAccessor level, BlockPos origin, RandomSource rand) {
        return generateAdhigamaTree(level, origin, rand, false);
    }

    public static boolean generateAdhigamaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        if (rand.nextFloat() < 0.35F) {
            return generateColossalAdhigamaTree(level, origin, rand, fromSapling);
        }
        return generateStandardAdhigama(level, origin, rand, fromSapling);
    }

    public static boolean generateStandardAdhigama(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 13 + rand.nextInt(6);
        if (!canPlaceTree(level, origin, 4, height + 5, fromSapling)) return false;

        BlockState log = SystemBlocks.ADHIGAMA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ADHIGAMA_LEAVES.get().defaultBlockState();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos r1 = origin.relative(dir);
            BlockPos r2 = origin.relative(dir, 2);
            placeOrientedLog(level, r1, log, Direction.Axis.Y);
            placeOrientedLog(level, r1.above(), log, Direction.Axis.Y);
            placeOrientedLog(level, r2, log, Direction.Axis.Y);
            if (level.getBlockState(r1.below()).isAir()) placeOrientedLog(level, r1.below(), log, Direction.Axis.Y);
            if (level.getBlockState(r2.below()).isAir()) placeOrientedLog(level, r2.below(), log, Direction.Axis.Y);
        }

        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        int midY = height - 6;
        BlockPos midFork = origin.above(midY);
        Direction sideDir = Direction.from2DDataValue(rand.nextInt(4));
        BlockPos midTip = midFork.relative(sideDir, 3).above(1);
        placeBranch(level, midFork, midTip, log);
        placeLeafCluster(level, midTip, 3.2, 1.6, leaves);
        placeHangingTendril(level, midTip.below(), 2 + rand.nextInt(2), leaves);
        placeHangingTendril(level, midTip.relative(sideDir.getClockWise()).below(), 2, leaves);

        BlockPos topFork = origin.above(height - 2);
        BlockPos topNorth = topFork.offset(0, 2, -3);
        BlockPos topSouth = topFork.offset(0, 2, 3);
        BlockPos topEast = topFork.offset(3, 2, 0);
        BlockPos topWest = topFork.offset(-3, 2, 0);

        placeBranch(level, topFork, topNorth, log);
        placeBranch(level, topFork, topSouth, log);
        placeBranch(level, topFork, topEast, log);
        placeBranch(level, topFork, topWest, log);

        placeLeafCluster(level, topNorth, 3.2, 2.0, leaves);
        placeLeafCluster(level, topSouth, 3.2, 2.0, leaves);
        placeLeafCluster(level, topEast, 3.2, 2.0, leaves);
        placeLeafCluster(level, topWest, 3.2, 2.0, leaves);

        placeLeafCluster(level, origin.above(height + 1), 4.2, 2.6, leaves);
        placeLeafCluster(level, origin.above(height + 3), 2.5, 1.4, leaves);

        placeHangingTendril(level, topNorth.offset(1, -1, 0), 2, leaves);
        placeHangingTendril(level, topSouth.offset(-1, -1, 0), 2, leaves);
        placeHangingTendril(level, topEast.offset(0, -1, 1), 3, leaves);
        placeHangingTendril(level, topWest.offset(0, -1, -1), 3, leaves);

        return true;
    }

    /**
     * Titán de la Selva de Adhigama (Variante Colosal de Sumeru / Dharma Forest).
     * Árbol monumental de selva virgen con raíces tabulares aéreas gigantescas, tronco masivo,
     * tres niveles de dosel tropical y lianas colgantes de más de 6 bloques.
     */
    public static boolean generateColossalAdhigamaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 24 + rand.nextInt(8);
        if (!canPlaceTree(level, origin, 6, height + 6, fromSapling)) {
            return generateStandardAdhigama(level, origin, rand, fromSapling);
        }

        BlockState log = SystemBlocks.ADHIGAMA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ADHIGAMA_LEAVES.get().defaultBlockState();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int dist = 1; dist <= 4; dist++) {
                int rHeight = Math.max(1, 5 - dist);
                BlockPos rPos = origin.relative(dir, dist);
                for (int dy = 0; dy < rHeight; dy++) {
                    placeOrientedLog(level, rPos.above(dy), log, Direction.Axis.Y);
                }
                for (int dy = -1; dy >= -3; dy--) {
                    if (level.getBlockState(rPos.above(dy)).isAir()) {
                        placeOrientedLog(level, rPos.above(dy), log, Direction.Axis.Y);
                    }
                }
            }
        }

        for (int dy = 0; dy <= 4; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    placeOrientedLog(level, origin.offset(dx, dy, dz), log, Direction.Axis.Y);
                }
            }
        }
        for (int dy = 5; dy <= height - 4; dy++) {
            placeOrientedLog(level, origin.offset(0, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(0, dy, 1), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 1), log, Direction.Axis.Y);
        }

        int shelfY = 13 + rand.nextInt(3);
        BlockPos shelfFork = origin.above(shelfY);
        BlockPos sTip1 = shelfFork.offset(5, 2, 0);
        BlockPos sTip2 = shelfFork.offset(-5, 2, 0);
        placeBranch(level, shelfFork, sTip1, log);
        placeBranch(level, shelfFork, sTip2, log);
        placeLeafCluster(level, sTip1, 4.0, 2.0, leaves);
        placeLeafCluster(level, sTip2, 4.0, 2.0, leaves);
        placeHangingTendril(level, sTip1.below(), 4 + rand.nextInt(3), leaves);
        placeHangingTendril(level, sTip2.below(), 4 + rand.nextInt(3), leaves);

        int midY = shelfY + 6;
        BlockPos midFork = origin.above(midY);
        BlockPos mTip1 = midFork.offset(0, 2, 5);
        BlockPos mTip2 = midFork.offset(0, 2, -5);
        BlockPos mTip3 = midFork.offset(4, 3, 4);
        placeBranch(level, midFork, mTip1, log);
        placeBranch(level, midFork, mTip2, log);
        placeBranch(level, midFork, mTip3, log);
        placeLeafCluster(level, mTip1, 4.2, 2.2, leaves);
        placeLeafCluster(level, mTip2, 4.2, 2.2, leaves);
        placeLeafCluster(level, mTip3, 3.8, 2.0, leaves);
        placeHangingTendril(level, mTip1.below(), 4, leaves);
        placeHangingTendril(level, mTip2.below(), 4, leaves);

        BlockPos topFork = origin.above(height - 3);
        BlockPos t1 = topFork.offset(4, 3, 0);
        BlockPos t2 = topFork.offset(-4, 3, 0);
        BlockPos t3 = topFork.offset(0, 3, 4);
        BlockPos t4 = topFork.offset(0, 3, -4);
        BlockPos t5 = topFork.offset(3, 4, 3);
        BlockPos t6 = topFork.offset(-3, 4, -3);

        placeBranch(level, topFork, t1, log);
        placeBranch(level, topFork, t2, log);
        placeBranch(level, topFork, t3, log);
        placeBranch(level, topFork, t4, log);
        placeBranch(level, topFork, t5, log);
        placeBranch(level, topFork, t6, log);

        placeLeafCluster(level, t1, 4.2, 2.4, leaves);
        placeLeafCluster(level, t2, 4.2, 2.4, leaves);
        placeLeafCluster(level, t3, 4.2, 2.4, leaves);
        placeLeafCluster(level, t4, 4.2, 2.4, leaves);
        placeLeafCluster(level, t5, 3.8, 2.0, leaves);
        placeLeafCluster(level, t6, 3.8, 2.0, leaves);

        placeLeafCluster(level, origin.above(height + 1), 5.8, 3.4, leaves);
        placeLeafCluster(level, origin.above(height + 4), 3.6, 2.0, leaves);

        placeHangingTendril(level, t1.offset(1, -1, 0), 4, leaves);
        placeHangingTendril(level, t2.offset(-1, -1, 0), 4, leaves);
        placeHangingTendril(level, t3.offset(0, -1, 1), 5, leaves);
        placeHangingTendril(level, t4.offset(0, -1, -1), 5, leaves);

        return true;
    }

    /**
     * 3. Árbol de Sandbearer / Madera Arenosa (Picos Kársticos de Liyue).
     * Árbol de montaña estilo bonsái oriental con tronco retorcido y nubes horizontales de hojas doradas.
     */
    public static boolean generateSandbearerTree(LevelAccessor level, BlockPos origin, RandomSource rand) {
        return generateSandbearerTree(level, origin, rand, false);
    }

    public static boolean generateSandbearerTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        if (rand.nextFloat() < 0.35F) {
            return generateLargeSandbearerTree(level, origin, rand, fromSapling);
        }
        return generateStandardSandbearer(level, origin, rand, fromSapling);
    }

    public static boolean generateStandardSandbearer(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 7 + rand.nextInt(4);
        if (!canPlaceTree(level, origin, 3, height + 4, fromSapling)) return false;

        BlockState log = SystemBlocks.SANDBEARER_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.SANDBEARER_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);

        int bendY = Math.max(2, height / 2);
        for (int dy = 0; dy <= bendY; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }
        int shiftX = (rand.nextBoolean() ? 1 : -1);
        int shiftZ = (rand.nextBoolean() ? 1 : -1);
        BlockPos midTrunk = origin.offset(shiftX, bendY, shiftZ);
        placeOrientedLog(level, midTrunk, log, Direction.Axis.Y);

        for (int dy = bendY + 1; dy <= height; dy++) {
            placeOrientedLog(level, origin.offset(shiftX, dy, shiftZ), log, Direction.Axis.Y);
        }

        BlockPos branchBaseLower = origin.offset(shiftX, height - 3, shiftZ);
        BlockPos branchBaseUpper = origin.offset(shiftX, height - 1, shiftZ);

        BlockPos bough1 = branchBaseLower.offset(3, 1, -2);
        BlockPos bough2 = branchBaseLower.offset(-3, 1, 2);
        BlockPos bough3 = branchBaseUpper.offset(-2, 1, -3);
        BlockPos bough4 = branchBaseUpper.offset(2, 1, 3);

        placeBranch(level, branchBaseLower, bough1, log);
        placeBranch(level, branchBaseLower, bough2, log);
        placeBranch(level, branchBaseUpper, bough3, log);
        placeBranch(level, branchBaseUpper, bough4, log);

        placeLeafCluster(level, bough1, 2.6, 1.5, leaves);
        placeLeafCluster(level, bough2, 2.6, 1.5, leaves);
        placeLeafCluster(level, bough3, 2.4, 1.4, leaves);
        placeLeafCluster(level, bough4, 2.4, 1.4, leaves);

        BlockPos crownTop = origin.offset(shiftX, height + 1, shiftZ);
        placeLeafCluster(level, crownTop, 3.4, 2.0, leaves);
        placeLeafCluster(level, crownTop.above(), 2.0, 1.2, leaves);

        return true;
    }

    /**
     * Gran Bonsái Pagoda de Sandbearer (Variante Ancestral de Liyue).
     * Árbol monumental con tronco serpentino y 5 plataformas horizontales escalonadas de nubes doradas.
     */
    public static boolean generateLargeSandbearerTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 14 + rand.nextInt(5);
        if (!canPlaceTree(level, origin, 6, height + 5, fromSapling)) {
            return generateStandardSandbearer(level, origin, rand, fromSapling);
        }

        BlockState log = SystemBlocks.SANDBEARER_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.SANDBEARER_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 2);

        int bend1 = 4;
        int bend2 = 9;
        int shiftX = rand.nextBoolean() ? 1 : -1;
        int shiftZ = rand.nextBoolean() ? 1 : -1;

        for (int dy = 0; dy <= bend1; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.above(dy).east(), log, Direction.Axis.Y);
        }
        for (int dy = bend1 + 1; dy <= bend2; dy++) {
            placeOrientedLog(level, origin.offset(shiftX, dy, shiftZ), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(shiftX + 1, dy, shiftZ), log, Direction.Axis.Y);
        }
        for (int dy = bend2 + 1; dy <= height; dy++) {
            placeOrientedLog(level, origin.offset(shiftX * 2, dy, shiftZ), log, Direction.Axis.Y);
        }

        BlockPos p1 = origin.offset(shiftX, bend1 + 2, shiftZ);
        BlockPos tip1 = p1.offset(-shiftX * 5, 1, 3);
        placeBranch(level, p1, tip1, log);
        placeLeafCluster(level, tip1, 3.8, 1.6, leaves);

        BlockPos p2 = origin.offset(shiftX, bend1 + 4, shiftZ);
        BlockPos tip2 = p2.offset(shiftX * 5, 1, -3);
        placeBranch(level, p2, tip2, log);
        placeLeafCluster(level, tip2, 3.8, 1.6, leaves);

        BlockPos p3 = origin.offset(shiftX * 2, bend2 + 1, shiftZ);
        BlockPos tip3 = p3.offset(3, 1, 4);
        placeBranch(level, p3, tip3, log);
        placeLeafCluster(level, tip3, 3.5, 1.5, leaves);

        BlockPos p4 = origin.offset(shiftX * 2, bend2 + 2, shiftZ);
        BlockPos tip4 = p4.offset(-4, 1, -3);
        placeBranch(level, p4, tip4, log);
        placeLeafCluster(level, tip4, 3.5, 1.5, leaves);

        BlockPos topPos = origin.offset(shiftX * 2, height + 1, shiftZ);
        placeLeafCluster(level, topPos, 4.4, 2.2, leaves);
        placeLeafCluster(level, topPos.above(), 2.8, 1.4, leaves);

        return true;
    }

    /**
     * 4. Árbol de Madera de Maná (Bosque Místico de Isekai).
     * Árbol místico con corteza cian luminiscente y hojas celestiales con Frutas de Maná.
     */
    public static boolean generateManaTree(LevelAccessor level, BlockPos origin, RandomSource rand) {
        return generateManaTree(level, origin, rand, false);
    }

    public static boolean generateManaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        if (rand.nextFloat() < 0.35F) {
            return generateLargeManaTree(level, origin, rand, fromSapling);
        }
        return generateStandardManaTree(level, origin, rand, fromSapling);
    }

    public static boolean generateStandardManaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 7 + rand.nextInt(4);
        if (!canPlaceTree(level, origin, 3, height + 4, fromSapling)) return false;

        BlockState log = SystemBlocks.MANA_WOOD_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.MANA_WOOD_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);

        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        BlockPos fork = origin.above(height - 2);
        BlockPos branchA = fork.offset(2, 2, 1);
        BlockPos branchB = fork.offset(-2, 2, -1);
        BlockPos branchC = fork.offset(1, 2, -2);

        placeBranch(level, fork, branchA, log);
        placeBranch(level, fork, branchB, log);
        placeBranch(level, fork, branchC, log);

        placeLeafCluster(level, branchA, 2.6, 1.8, leaves);
        placeLeafCluster(level, branchB, 2.6, 1.8, leaves);
        placeLeafCluster(level, branchC, 2.6, 1.8, leaves);

        placeLeafCluster(level, origin.above(height + 1), 3.2, 2.0, leaves);

        placeHangingTendril(level, branchA.below(), 2, leaves);
        placeHangingTendril(level, branchB.below(), 2, leaves);
        placeHangingTendril(level, branchC.below(), 1, leaves);

        return true;
    }

    /**
     * Gran Árbol Cósmico de Maná (Variante Colosal de Isekai).
     */
    public static boolean generateLargeManaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 15 + rand.nextInt(6);
        if (!canPlaceTree(level, origin, 5, height + 5, fromSapling)) {
            return generateStandardManaTree(level, origin, rand, fromSapling);
        }

        BlockState log = SystemBlocks.MANA_WOOD_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.MANA_WOOD_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 2);

        int forkY = height - 4;
        for (int dy = 0; dy <= forkY; dy++) {
            placeOrientedLog(level, origin.offset(0, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(0, dy, 1), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 1), log, Direction.Axis.Y);
        }

        BlockPos fork = origin.above(forkY);
        BlockPos b1 = fork.offset(4, 3, 2);
        BlockPos b2 = fork.offset(-4, 3, -2);
        BlockPos b3 = fork.offset(2, 3, -4);
        BlockPos b4 = fork.offset(-2, 3, 4);

        placeBranch(level, fork, b1, log);
        placeBranch(level, fork, b2, log);
        placeBranch(level, fork, b3, log);
        placeBranch(level, fork, b4, log);

        placeLeafCluster(level, b1, 3.6, 2.2, leaves);
        placeLeafCluster(level, b2, 3.6, 2.2, leaves);
        placeLeafCluster(level, b3, 3.6, 2.2, leaves);
        placeLeafCluster(level, b4, 3.6, 2.2, leaves);

        placeHangingTendril(level, b1.below(), 3, leaves);
        placeHangingTendril(level, b2.below(), 3, leaves);
        placeHangingTendril(level, b3.below(), 2, leaves);
        placeHangingTendril(level, b4.below(), 2, leaves);

        BlockPos topCenter = origin.above(height + 1);
        placeLeafCluster(level, topCenter, 4.8, 2.8, leaves);
        placeLeafCluster(level, topCenter.above(2), 3.0, 1.6, leaves);

        return true;
    }

    /**
     * 5. Árbol Sagrado de Eldoria (Llanuras Doradas de Isekai).
     * Árbol áureo imperial con corona monumental de 5 ramas y gran bóveda sagrada resplandeciente.
     */
    public static boolean generateEldoriaTree(LevelAccessor level, BlockPos origin, RandomSource rand) {
        return generateEldoriaTree(level, origin, rand, false);
    }

    public static boolean generateEldoriaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        if (rand.nextFloat() < 0.35F) {
            return generateLargeEldoriaTree(level, origin, rand, fromSapling);
        }
        return generateStandardEldoriaTree(level, origin, rand, fromSapling);
    }

    public static boolean generateStandardEldoriaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 9 + rand.nextInt(5);
        if (!canPlaceTree(level, origin, 4, height + 5, fromSapling)) return false;

        BlockState log = SystemBlocks.ELDORIA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ELDORIA_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 2);

        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        BlockPos fork = origin.above(height - 2);
        BlockPos b1 = fork.offset(3, 2, 0);
        BlockPos b2 = fork.offset(-3, 2, 0);
        BlockPos b3 = fork.offset(0, 2, 3);
        BlockPos b4 = fork.offset(0, 2, -3);
        BlockPos b5 = fork.offset(2, 2, 2);

        placeBranch(level, fork, b1, log);
        placeBranch(level, fork, b2, log);
        placeBranch(level, fork, b3, log);
        placeBranch(level, fork, b4, log);
        placeBranch(level, fork, b5, log);

        placeLeafCluster(level, b1, 2.8, 2.0, leaves);
        placeLeafCluster(level, b2, 2.8, 2.0, leaves);
        placeLeafCluster(level, b3, 2.8, 2.0, leaves);
        placeLeafCluster(level, b4, 2.8, 2.0, leaves);
        placeLeafCluster(level, b5, 2.8, 2.0, leaves);

        placeLeafCluster(level, origin.above(height + 1), 4.2, 2.6, leaves);
        placeLeafCluster(level, origin.above(height + 3), 2.8, 1.6, leaves);

        return true;
    }

    /**
     * Gran Árbol Imperial de Eldoria (Variante Monumental de Isekai).
     */
    public static boolean generateLargeEldoriaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 18 + rand.nextInt(6);
        if (!canPlaceTree(level, origin, 5, height + 6, fromSapling)) {
            return generateStandardEldoriaTree(level, origin, rand, fromSapling);
        }

        BlockState log = SystemBlocks.ELDORIA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ELDORIA_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 2);

        int forkY = height - 4;
        for (int dy = 0; dy <= forkY; dy++) {
            placeOrientedLog(level, origin.offset(0, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 0), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(0, dy, 1), log, Direction.Axis.Y);
            placeOrientedLog(level, origin.offset(1, dy, 1), log, Direction.Axis.Y);
        }

        BlockPos fork = origin.above(forkY);
        BlockPos b1 = fork.offset(4, 3, 0);
        BlockPos b2 = fork.offset(-4, 3, 0);
        BlockPos b3 = fork.offset(0, 3, 4);
        BlockPos b4 = fork.offset(0, 3, -4);
        BlockPos b5 = fork.offset(3, 4, 3);
        BlockPos b6 = fork.offset(-3, 4, -3);

        placeBranch(level, fork, b1, log);
        placeBranch(level, fork, b2, log);
        placeBranch(level, fork, b3, log);
        placeBranch(level, fork, b4, log);
        placeBranch(level, fork, b5, log);
        placeBranch(level, fork, b6, log);

        placeLeafCluster(level, b1, 3.8, 2.4, leaves);
        placeLeafCluster(level, b2, 3.8, 2.4, leaves);
        placeLeafCluster(level, b3, 3.8, 2.4, leaves);
        placeLeafCluster(level, b4, 3.8, 2.4, leaves);
        placeLeafCluster(level, b5, 3.4, 2.0, leaves);
        placeLeafCluster(level, b6, 3.4, 2.0, leaves);

        BlockPos topCenter = origin.above(height + 1);
        placeLeafCluster(level, topCenter, 5.5, 3.2, leaves);
        placeLeafCluster(level, topCenter.above(2), 3.5, 1.8, leaves);

        return true;
    }

    public static boolean generateAraliaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 8 + rand.nextInt(4);
        if (!canPlaceTree(level, origin, 3, height + 3, fromSapling)) return false;

        BlockState log = SystemBlocks.ARALIA_WOOD.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ARALIA_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);
        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        for (int dy = 4; dy <= height + 2; dy++) {
            double radius = Math.max(1.2, 3.2 - (dy - 4) * 0.35);
            placeLeafCluster(level, origin.above(dy), radius, 1.2, leaves);
        }
        return true;
    }

    public static boolean generateOtogiTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 6 + rand.nextInt(3);
        if (!canPlaceTree(level, origin, 3, height + 3, fromSapling)) return false;

        BlockState log = SystemBlocks.OTOGI_WOOD.get().defaultBlockState();
        BlockState leaves = SystemBlocks.OTOGI_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);
        int bendX = rand.nextBoolean() ? 1 : -1;
        int bendZ = rand.nextBoolean() ? 1 : -1;
        for (int dy = 0; dy <= 3; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }
        for (int dy = 4; dy <= height; dy++) {
            placeOrientedLog(level, origin.offset(bendX, dy, bendZ), log, Direction.Axis.Y);
        }

        BlockPos b1 = origin.offset(bendX + 2, height - 1, bendZ);
        BlockPos b2 = origin.offset(bendX - 2, height, bendZ + 2);
        placeBranch(level, origin.offset(bendX, height - 2, bendZ), b1, log);
        placeBranch(level, origin.offset(bendX, height - 1, bendZ), b2, log);

        placeLeafCluster(level, b1, 2.5, 1.5, leaves);
        placeLeafCluster(level, b2, 2.5, 1.5, leaves);
        placeLeafCluster(level, origin.offset(bendX, height + 1, bendZ), 3.2, 1.8, leaves);

        return true;
    }

    public static boolean generateYumemiruTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 7 + rand.nextInt(3);
        if (!canPlaceTree(level, origin, 4, height + 4, fromSapling)) return false;

        BlockState log = SystemBlocks.YUMEMIRU_WOOD.get().defaultBlockState();
        BlockState leaves = SystemBlocks.YUMEMIRU_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);
        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        BlockPos fork = origin.above(height);
        BlockPos[] branches = {
            fork.offset(3, 1, 0),
            fork.offset(-3, 1, 0),
            fork.offset(0, 1, 3),
            fork.offset(0, 1, -3),
            fork.offset(2, 2, 2),
            fork.offset(-2, 2, -2)
        };

        for (BlockPos b : branches) {
            placeBranch(level, fork, b, log);
            placeLeafCluster(level, b, 2.8, 1.8, leaves);
            if (rand.nextFloat() < 0.6F) {
                placeLeafCluster(level, b.below(), 1.5, 1.0, leaves);
            }
        }
        placeLeafCluster(level, fork.above(2), 3.5, 2.0, leaves);

        return true;
    }

    public static boolean generateMapleTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 7 + rand.nextInt(3);
        if (!canPlaceTree(level, origin, 4, height + 4, fromSapling)) return false;

        BlockState log = SystemBlocks.MAPLE_WOOD.get().defaultBlockState();
        BlockState leaves = SystemBlocks.MAPLE_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);
        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        BlockPos fork = origin.above(height - 1);
        BlockPos[] boughs = {
            fork.offset(3, 1, 2),
            fork.offset(-3, 1, -2),
            fork.offset(2, 2, -3),
            fork.offset(-2, 2, 3)
        };

        for (BlockPos b : boughs) {
            placeBranch(level, fork, b, log);
            placeLeafCluster(level, b, 3.2, 1.6, leaves);
        }
        placeLeafCluster(level, origin.above(height + 1), 4.0, 2.2, leaves);
        placeLeafCluster(level, origin.above(height + 2), 2.5, 1.2, leaves);

        return true;
    }
}

