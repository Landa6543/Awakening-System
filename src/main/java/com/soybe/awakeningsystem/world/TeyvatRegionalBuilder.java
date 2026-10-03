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
import net.minecraft.world.level.levelgen.Heightmap;

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
                // 1. Despejar espacio aéreo interior (hasta 16 bloques arriba)
                for (int y = ySurface + 1; y <= ySurface + 16; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && (s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(BlockTags.FLOWERS) || s.is(BlockTags.DIRT) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.REPLACEABLE))) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                // 2. Colocar superficie de cimiento
                level.setBlock(new BlockPos(x, ySurface, z), surfaceBlock, 2);

                // 3. Proyectar hacia abajo hasta 36 bloques
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

        BlockPos center = new BlockPos(0, 64, 0);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        if (surfaceY < 60 || surfaceY > 150) surfaceY = 68;
        center = new BlockPos(0, surfaceY, 0);

        // Colocar solo el Waypoint natural sobre el terreno sin losas ni plataformas de piedra
        TeyvatWorldGenManager.generateTeleportWaypoint(level, center.above());

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

        int halfW = 12; // 25 de ancho (-12..12)
        int halfL = 10; // 21 de largo (-10..10)
        int height = 11;

        // Cimientos adaptativos profundos de ladrillos de piedra
        buildAdaptiveFoundation(level, cx - halfW - 2, cx + halfW + 2, cz - halfL - 2, cz + halfL + 2, cy,
                Blocks.COBBLESTONE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState());

        // Suelo interior de roble oscuro pulido
        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfL; z <= halfL; z++) {
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }

        // Muros de piedra en planta baja (cy + 1 .. cy + 4)
        for (int y = 1; y <= 4; y++) {
            for (int x = -halfW; x <= halfW; x++) {
                for (int z = -halfL; z <= halfL; z++) {
                    if (Math.abs(x) == halfW || Math.abs(z) == halfL) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                        // Entrada principal al sur
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

        // Entresuelo / Primera planta (cy + 5)
        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfL; z <= halfL; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
            }
        }

        // Muros superiores de entramado de madera y terracota blanca (cy + 6 .. cy + 8)
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

        // Tejado a dos aguas de terracota roja / ladrillos
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

        // Interior: Gran Mesa de Degustación y Barriles de Vino
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz), Blocks.DARK_OAK_STAIRS.defaultBlockState(), 2);
        }
        // Pilas de barriles de vino de diente de león
        for (int bz = -halfL + 2; bz <= -halfL + 6; bz++) {
            level.setBlock(new BlockPos(cx - halfW + 2, cy + 1, cz + bz), Blocks.BARREL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - halfW + 2, cy + 2, cz + bz), Blocks.BARREL.defaultBlockState(), 2);
        }

        // Farolillos de forja en el vestíbulo
        level.setBlock(new BlockPos(cx - 3, cy + 4, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 4, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Viñedos exteriores al frente
        for (int row = -2; row <= 2; row += 2) {
            for (int vz = halfL + 3; vz <= halfL + 8; vz++) {
                BlockPos vp = new BlockPos(cx + row * 3, cy + 1, cz + vz);
                level.setBlock(vp, Blocks.OAK_FENCE.defaultBlockState(), 2);
                level.setBlock(vp.above(), Blocks.OAK_LEAVES.defaultBlockState(), 2);
            }
        }

        // Diluc en la entrada principal
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

        int halfW = 8;
        int halfL = 8;

        // Cimientos adaptativos
        buildAdaptiveFoundation(level, cx - halfW - 4, cx + halfW + 4, cz - halfL - 6, cz + halfL + 4, cy,
                Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());

        // Pavimento de madera de cerezo pulida
        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfL; z <= halfL; z++) {
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.CHERRY_PLANKS.defaultBlockState(), 2);
            }
        }

        // Columnas bermellón de terracota roja en las 4 esquinas y pórtico
        int[][] pillars = {{-halfW, -halfL}, {halfW, -halfL}, {-halfW, halfL}, {halfW, halfL}, {-3, -halfL}, {3, -halfL}};
        for (int[] p : pillars) {
            for (int y = 1; y <= 5; y++) {
                level.setBlock(new BlockPos(cx + p[0], cy + y, cz + p[1]), Blocks.RED_TERRACOTTA.defaultBlockState(), 2);
            }
        }

        // Muros celosía con papel blanco y madera de cerezo
        for (int y = 1; y <= 4; y++) {
            for (int x = -halfW; x <= halfW; x++) {
                for (int z = -halfL; z <= halfL; z++) {
                    if (Math.abs(x) == halfW || Math.abs(z) == halfL) {
                        BlockPos pos = new BlockPos(cx + x, cy + y, cz + z);
                        if (z == -halfL && Math.abs(x) <= 2) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2); // Puerta abierta
                        } else if (y >= 2 && y <= 3 && Math.abs(x) % 2 == 0) {
                            level.setBlock(pos, Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, Blocks.CHERRY_PLANKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Tejado oriental con aleros voladizos curvos
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

        // Gran Torii bermellón frente a la entrada (cz - halfL - 4)
        int toriiZ = cz - halfL - 4;
        for (int ty = 1; ty <= 5; ty++) {
            level.setBlock(new BlockPos(cx - 3, cy + ty, toriiZ), Blocks.RED_CONCRETE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 3, cy + ty, toriiZ), Blocks.RED_CONCRETE.defaultBlockState(), 2);
        }
        for (int tx = -4; tx <= 4; tx++) {
            level.setBlock(new BlockPos(cx + tx, cy + 5, toriiZ), Blocks.RED_CONCRETE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + tx, cy + 6, toriiZ), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState(), 2);
        }

        // Altar Interior del Cerezo Sagrado con faroles flotantes
        BlockPos altarPos = new BlockPos(cx, cy + 1, cz + 3);
        level.setBlock(altarPos, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(altarPos.above(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(altarPos.above(2), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        // Raiden Shogun guardando el santuario
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

        buildAdaptiveFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy,
                Blocks.MOSS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState());

        // Tronco hueco central de selva
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

        // Entrada
        level.setBlock(new BlockPos(cx, cy + 1, cz + 3), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 3), Blocks.AIR.defaultBlockState(), 2);

        // Cúpula de hojas y musgo con esporas
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 8, cz + z), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + x, cy + 9, cz + z), Blocks.JUNGLE_LEAVES.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.SHROOMLIGHT.defaultBlockState(), 2);

        // Cofre oculto de Sumeru con gemas dendro
        BlockPos chestPos = new BlockPos(cx, cy + 1, cz - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container c) {
            c.setItem(0, new ItemStack(SystemItems.PRIMOGEM.get(), 60));
            c.setItem(1, SystemItems.coin(250));
            c.setItem(2, new ItemStack(Items.EMERALD, 4));
        }

        // Hoshimi Miyabi o Ellen Joe explorando
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

        int maxR = 7;
        buildAdaptiveFoundation(level, cx - maxR, cx + maxR, cz - maxR, cz + maxR, cy,
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState());

        // Pirámide escalonada de 4 niveles
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

        // Cima ceremonial con antorchas de fuego de alma y obelisco
        int topY = cy + 5;
        level.setBlock(new BlockPos(cx, topY, cz), Blocks.CHISELED_SANDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, topY + 1, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, topY, cz - 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, topY, cz - 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, topY, cz + 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, topY, cz + 2), Blocks.SOUL_TORCH.defaultBlockState(), 2);

        // Cofre protegido
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

        // Pilares de estalagmitas de hielo glacial
        int[][] icePillars = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] ip : icePillars) {
            for (int h = 1; h <= 4; h++) {
                level.setBlock(new BlockPos(cx + ip[0], cy + h, cz + ip[1]), Blocks.BLUE_ICE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + ip[0], cy + 5, cz + ip[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        // Núcleo carmesí de frío eterno
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // Ganyu contemplando la cordillera nevada
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 1, cz + 1), ModEntities.SERIES_NPC.get(), NpcProfile.GANYU);
    }

    /**
     * 7. Glorieta Candelabro y Manantial de Fontaine (Fontaine Waters Gazebo).
     * Pabellón lacustre de mármol blanco, farolas de cobre y aguas cristalinas.
     */
    public static void buildFontaineGazebo(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        buildAdaptiveFoundation(level, cx - 5, cx + 5, cz - 5, cz + 5, cy,
                Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.QUARTZ_BRICKS.defaultBlockState());

        // Plataforma circular de cuarzo
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if (x * x + z * z <= 16) {
                    level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                }
            }
        }

        // 6 Columnas de cuarzo estriado
        int[][] colPositions = {{-3, -2}, {3, -2}, {-3, 2}, {3, 2}, {0, -3}, {0, 3}};
        for (int[] cp : colPositions) {
            for (int h = 1; h <= 4; h++) {
                level.setBlock(new BlockPos(cx + cp[0], cy + h, cz + cp[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
        }

        // Tejado abovedado de cuarzo
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + z), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);

        // Fuente central con agua purificada
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.WATER.defaultBlockState(), 2);

        // March 7th o personaje disfrutando de Fontaine
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 1, cz - 1), ModEntities.SERIES_NPC.get(), NpcProfile.MARCH_7TH);
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

        // 1. Cimentación circular adaptativa (radio 10)
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

        // 2. Hoguera Central de Cocina Segura (Sin fuego activo dañino para que el Mitachurl no se autoinmole)
        level.setBlock(new BlockPos(cx, cy, cz), Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy, cz), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy, cz), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz - 2), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz + 2), SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState(), 2);

        // 3. Choza Tribal Hilichurl (cx - 6, cz - 6)
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

        // 4. Torre de Vigía Hilichurl (cx + 6, cz + 6)
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

        // 5. Almacén de Botín y Tesoro (cx + 5, cz - 5)
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

        // 6. Defensores Hilichurl
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

    // =========================================================================
    // COORDENADAS CANÓNICAS DE LAS 5 CAPITALES Y SEDES DE TEYVAT
    // =========================================================================
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
     * Ubicación: (0, 68, -320) en cider_lake / teyvat_plains.
     * Incluye la Gran Plaza circular de adoquín y piedra cincelada, Fuente con Estatua Alada
     * de Barbatos, Molino de Viento tradicional de Favonius, Sede de los Caballeros y Diluc.
     */
    public static synchronized void ensureMondstadtCapitalCitadel(ServerLevel level) {
        if (true) return; // Procedural citadel disabled

        int cx = MONDSTADT_POS.getX();
        int cz = MONDSTADT_POS.getZ();
        int cy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        if (cy < 60 || cy > 130) cy = 68;

        // Comprobar si ya existe
        if (level.getBlockState(new BlockPos(cx, cy + 5, cz - 4)).is(Blocks.SMOOTH_QUARTZ)) {
            mondstadtCitadelBuilt = true;
            return;
        }

        // Cimientos adaptativos masivos para la capital
        buildAdaptiveFoundation(level, cx - 28, cx + 28, cz - 36, cz + 24, cy,
                Blocks.STONE_BRICKS.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState());

        // A. Plaza de la Libertad circular
        for (int x = -22; x <= 22; x++) {
            for (int z = -22; z <= 22; z++) {
                int distSq = x * x + z * z;
                if (distSq <= 22 * 22) {
                    BlockPos p = new BlockPos(cx + x, cy, cz + z);
                    if (distSq >= 20 * 20) {
                        level.setBlock(p, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);
                    } else if (distSq % 4 == 0) {
                        level.setBlock(p, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                    } else if (distSq % 2 == 0) {
                        level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // B. Gran Fuente y Estatua de Barbatos en (cx, cy, cz - 4)
        int fx = cx;
        int fz = cz - 4;
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                int d = Math.max(Math.abs(x), Math.abs(z));
                BlockPos p = new BlockPos(fx + x, cy, fz + z);
                if (d == 4) {
                    level.setBlock(new BlockPos(fx + x, cy + 1, fz + z), Blocks.STONE_BRICK_STAIRS.defaultBlockState(), 2);
                } else if (d > 1) {
                    level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(fx + x, cy + 1, fz + z), Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }
        // Pedestal y Estatua Alada de Barbatos
        for (int h = 1; h <= 3; h++) {
            level.setBlock(new BlockPos(fx, cy + h, fz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(fx, cy + 4, fz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
        level.setBlock(new BlockPos(fx, cy + 5, fz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
        // Alas de Barbatos
        level.setBlock(new BlockPos(fx - 1, cy + 5, fz), Blocks.QUARTZ_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(fx - 2, cy + 6, fz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(fx + 1, cy + 5, fz), Blocks.QUARTZ_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(fx + 2, cy + 6, fz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        // Orbe celestial de viento
        level.setBlock(new BlockPos(fx, cy + 6, fz), Blocks.PRISMARINE.defaultBlockState(), 2);

        // C. Molino de Viento de Favonius en (cx - 18, cy, cz - 10)
        int mx = cx - 18;
        int mz = cz - 10;
        for (int h = 1; h <= 14; h++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                        level.setBlock(new BlockPos(mx + dx, cy + h, mz + dz), (h <= 4) ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Tejado cónico de madera de abeto
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                    level.setBlock(new BlockPos(mx + dx, cy + 15, mz + dz), Blocks.SPRUCE_STAIRS.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(mx, cy + 16, mz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(mx, cy + 17, mz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        // Aspas del molino en forma de X (Lana blanca y vallas de roble)
        int axleY = cy + 13;
        int bladeZ = mz + 3;
        level.setBlock(new BlockPos(mx, axleY, bladeZ), Blocks.GRINDSTONE.defaultBlockState(), 2);
        int[][] bladeOffsets = {{-3, -3}, {-2, -2}, {-1, -1}, {1, 1}, {2, 2}, {3, 3}, {-3, 3}, {-2, 2}, {-1, 1}, {1, -1}, {2, -2}, {3, -3}};
        for (int[] bo : bladeOffsets) {
            level.setBlock(new BlockPos(mx + bo[0], axleY + bo[1], bladeZ), Blocks.WHITE_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(mx + bo[0], axleY + bo[1], bladeZ - 1), Blocks.OAK_FENCE.defaultBlockState(), 2);
        }

        // D. Sede de la Orden de los Caballeros de Favonius en (cx - 10..cx + 10, cz - 34..cz - 20)
        for (int x = -10; x <= 10; x++) {
            for (int z = -34; z <= -20; z++) {
                // Suelo de andesita pulida y alfombra roja central
                BlockPos floorPos = new BlockPos(cx + x, cy, cz + z);
                level.setBlock(floorPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                if (Math.abs(x) <= 1 && z >= -32 && z <= -21) {
                    level.setBlock(floorPos.above(), Blocks.RED_CARPET.defaultBlockState(), 2);
                }

                // Paredes perimetrales de ladrillos de piedra
                boolean isEdge = (x == -10 || x == 10 || z == -34 || z == -20);
                if (isEdge) {
                    for (int h = 1; h <= 8; h++) {
                        BlockPos wallPos = new BlockPos(cx + x, cy + h, cz + z);
                        if (z == -20 && Math.abs(x) <= 1 && h <= 3) {
                            // Gran entrada de arco doble abierta
                            level.setBlock(wallPos, Blocks.AIR.defaultBlockState(), 2);
                        } else if (h == 4 && (Math.abs(x) == 5 || z == -27)) {
                            // Ventanales de cristal teñido celeste
                            level.setBlock(wallPos, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(wallPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        // Techo de la Sede con vigas de abeto
        for (int x = -10; x <= 10; x++) {
            for (int z = -34; z <= -20; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 9, cz + z), Blocks.SPRUCE_SLAB.defaultBlockState(), 2);
            }
        }
        // Mobiliario: Mesa de deliberación de los Caballeros y estanterías
        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz - 28), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        }
        for (int z = -32; z <= -24; z += 2) {
            level.setBlock(new BlockPos(cx - 9, cy + 1, cz + z), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 9, cy + 2, cz + z), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 9, cy + 1, cz + z), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 9, cy + 2, cz + z), Blocks.BOOKSHELF.defaultBlockState(), 2);
        }
        // Lámparas colgantes
        level.setBlock(new BlockPos(cx, cy + 8, cz - 28), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz - 24), Blocks.LANTERN.defaultBlockState(), 2);

        // E. Punto de Teletransporte de la Plaza
        TeyvatWorldGenManager.generateTeleportWaypoint(level, new BlockPos(cx + 6, cy + 1, cz + 6));

        // F. Diluc vigilando la entrada de la Ciudadela
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 3, cy + 1, cz - 18), ModEntities.SERIES_NPC.get(), NpcProfile.DILUC);

        mondstadtCitadelBuilt = true;
        AwakeningSystemMod.LOGGER.info("[Teyvat Capital] Ciudadela de Favonius y Plaza de Mondstadt erigidas en: ({}, {}, {})", cx, cy, cz);
    }

    /**
     * 2. TERRAZA YUJING Y PABELLÓN YUEHAI (LIYUE CAPITAL).
     * Ubicación: (-350, 68, 450) en teyvat_dihua_marsh / teyvat_guyun_stone_forest.
     * Arquitectura clásica de la dinastía de Liyue: Tejas prismarinas verde esmeralda y madera
     * carmesí, estanque de flores de seda y loto, monumento Geo del Rex Lapis, Zhongli y Hu Tao.
     */
    public static synchronized void ensureLiyueYujingTerrace(ServerLevel level) {
        if (true) return; // Procedural Liyue disabled

        int cx = LIYUE_POS.getX();
        int cz = LIYUE_POS.getZ();
        int cy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        if (cy < 60 || cy > 130) cy = 68;

        if (level.getBlockState(new BlockPos(cx, cy + 4, cz + 16)).is(Blocks.DARK_PRISMARINE_STAIRS)) {
            liyueTerraceBuilt = true;
            return;
        }

        buildAdaptiveFoundation(level, cx - 28, cx + 28, cz - 26, cz + 30, cy,
                Blocks.POLISHED_GRANITE.defaultBlockState(), Blocks.RED_SANDSTONE.defaultBlockState());

        // A. Terraza de granito pulido con balaustradas de ladrillos del nether rojos
        for (int x = -24; x <= 24; x++) {
            for (int z = -22; z <= 26; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 24 || z == -22 || z == 26) {
                    level.setBlock(p.above(), Blocks.RED_NETHER_BRICK_WALL.defaultBlockState(), 2);
                } else if ((x + z) % 3 == 0) {
                    level.setBlock(p, Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_GRANITE.defaultBlockState(), 2);
                }
            }
        }

        // B. Pabellón Yuehai (Sede de las Siete Estrellas de Liyue - Qixing) en (cx - 12..cx + 12, cz + 10..cz + 24)
        for (int x = -12; x <= 12; x++) {
            for (int z = 10; z <= 24; z++) {
                BlockPos floorPos = new BlockPos(cx + x, cy, cz + z);
                level.setBlock(floorPos, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                if (Math.abs(x) <= 2 && z >= 12 && z <= 22) {
                    level.setBlock(floorPos.above(), Blocks.YELLOW_CARPET.defaultBlockState(), 2);
                }

                // Pilares ceremoniales de madera carmesí
                boolean isPillar = (Math.abs(x) == 12 || Math.abs(x) == 6 || x == 0) && (z == 10 || z == 17 || z == 24);
                if (isPillar) {
                    for (int h = 1; h <= 7; h++) {
                        level.setBlock(new BlockPos(cx + x, cy + h, cz + z), Blocks.CRIMSON_STEM.defaultBlockState(), 2);
                    }
                } else if (z == 24 || Math.abs(x) == 12) {
                    for (int h = 1; h <= 6; h++) {
                        level.setBlock(new BlockPos(cx + x, cy + h, cz + z), Blocks.CUT_RED_SANDSTONE.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Tejado de pagoda escalonada con tejas de prismarina oscura (icono de Liyue)
        for (int tier = 0; tier <= 2; tier++) {
            int yRoof = cy + 7 + tier * 2;
            int rX = 13 - tier * 3;
            int rZMin = 9 + tier * 2;
            int rZMax = 25 - tier * 2;
            for (int x = -rX; x <= rX; x++) {
                for (int z = rZMin; z <= rZMax; z++) {
                    if (Math.abs(x) == rX || z == rZMin || z == rZMax) {
                        level.setBlock(new BlockPos(cx + x, yRoof, cz + z), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState(), 2);
                    } else if (tier == 2) {
                        level.setBlock(new BlockPos(cx + x, yRoof, cz + z), Blocks.WARPED_SLAB.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Linternas de oro en las 4 esquinas del pabellón
        int[][] lanterns = {{-13, 9}, {13, 9}, {-13, 25}, {13, 25}};
        for (int[] lp : lanterns) {
            level.setBlock(new BlockPos(cx + lp[0], cy + 6, cz + lp[1]), Blocks.LANTERN.defaultBlockState(), 2);
        }

        // C. Estanque de Lotos y Flores de Seda en (cx - 16..cx - 6, cz - 16..cz - 6)
        for (int x = -16; x <= -6; x++) {
            for (int z = -16; z <= -6; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (x == -16 || x == -6 || z == -16 || z == -6) {
                    level.setBlock(p, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.BAMBOO.defaultBlockState(), 2);
                } else {
                    level.setBlock(p.below(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                    if ((x + z) % 3 == 0) {
                        level.setBlock(p.above(), Blocks.LILY_PAD.defaultBlockState(), 2);
                    }
                }
            }
        }

        // D. Monumento Geo al Rex Lapis (Estela celestial)
        int mx = cx + 12;
        int mz = cz - 10;
        level.setBlock(new BlockPos(mx, cy + 1, mz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(mx, cy + 2, mz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(mx, cy + 3, mz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(mx, cy + 4, mz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        // E. Punto de Teletransporte de la Terraza Yujing
        TeyvatWorldGenManager.generateTeleportWaypoint(level, new BlockPos(cx - 4, cy + 1, cz - 4));

        // F. Zhongli y Hu Tao en la explanada de la Terraza
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.SERIES_NPC.get(), NpcProfile.ZHONGLI);
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 5, cy + 1, cz), ModEntities.SERIES_NPC.get(), NpcProfile.HUTAO);

        liyueTerraceBuilt = true;
        AwakeningSystemMod.LOGGER.info("[Teyvat Capital] Terraza Yujing y Pabellón Yuehai erigidos en: ({}, {}, {})", cx, cy, cz);
    }

    /**
     * 3. PALACIO TENSHUKAKU Y JARDÍN ZEN (INAZUMA CAPITAL).
     * Ubicación: (550, 68, 550) en teyvat_narukami_island.
     * Gran castillo Shogunil de tejas de pizarra negra pulida, Torii bermellón sagrado,
     * bosque de cerezos de flor rosa (Sakura), Trono del Rayo y Raiden Shogun.
     */
    public static synchronized void ensureInazumaTenshukaku(ServerLevel level) {
        if (true) return; // Procedural Inazuma disabled

        int cx = INAZUMA_POS.getX();
        int cz = INAZUMA_POS.getZ();
        int cy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        if (cy < 60 || cy > 130) cy = 68;

        if (level.getBlockState(new BlockPos(cx, cy + 12, cz + 10)).is(Blocks.DEEPSLATE_TILE_STAIRS)) {
            inazumaTenshukakuBuilt = true;
            return;
        }

        buildAdaptiveFoundation(level, cx - 26, cx + 26, cz - 26, cz + 28, cy,
                Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.DEEPSLATE_BRICKS.defaultBlockState());

        // A. Puerta Torii Sagrada en (cx, cy, cz - 20)
        int tx = cx;
        int tz = cz - 20;
        for (int h = 1; h <= 6; h++) {
            level.setBlock(new BlockPos(tx - 4, cy + h, tz), Blocks.CRIMSON_STEM.defaultBlockState(), 2);
            level.setBlock(new BlockPos(tx + 4, cy + h, tz), Blocks.CRIMSON_STEM.defaultBlockState(), 2);
        }
        for (int x = -6; x <= 6; x++) {
            level.setBlock(new BlockPos(tx + x, cy + 7, tz), Blocks.DARK_OAK_SLAB.defaultBlockState(), 2);
            if (Math.abs(x) <= 4) {
                level.setBlock(new BlockPos(tx + x, cy + 5, tz), Blocks.CRIMSON_PLANKS.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(tx, cy + 4, tz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // B. Jardín Zen de Cerezos (Sakura)
        int[][] cherryTrees = {{-14, -12}, {14, -12}, {-16, 2}, {16, 2}};
        for (int[] cp : cherryTrees) {
            int px = cx + cp[0];
            int pz = cz + cp[1];
            for (int h = 1; h <= 4; h++) {
                level.setBlock(new BlockPos(px, cy + h, pz), Blocks.CHERRY_LOG.defaultBlockState(), 2);
            }
            for (int lx = -2; lx <= 2; lx++) {
                for (int lz = -2; lz <= 2; lz++) {
                    for (int lh = 3; lh <= 5; lh++) {
                        if (Math.abs(lx) + Math.abs(lz) <= 3) {
                            level.setBlock(new BlockPos(px + lx, cy + lh, pz + lz), Blocks.CHERRY_LEAVES.defaultBlockState(), 2);
                        }
                    }
                }
            }
            level.setBlock(new BlockPos(px, cy + 6, pz), Blocks.CHERRY_LEAVES.defaultBlockState(), 2);
            // Pétalos rosas en el suelo
            level.setBlock(new BlockPos(px + 1, cy + 1, pz + 1), Blocks.PINK_PETALS.defaultBlockState(), 2);
        }

        // C. Palacio Tenshukaku (Sede del Narukami Ogosho) en (cx - 12..cx + 12, cz - 2..cz + 22)
        for (int x = -12; x <= 12; x++) {
            for (int z = -2; z <= 22; z++) {
                BlockPos floorPos = new BlockPos(cx + x, cy, cz + z);
                level.setBlock(floorPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);

                // Paredes de calcita blanca y pilares de roble oscuro
                boolean isEdge = (Math.abs(x) == 12 || z == -2 || z == 22);
                boolean isPillar = isEdge && (Math.abs(x) == 12 || Math.abs(x) == 6 || x == 0);
                if (isPillar) {
                    for (int h = 1; h <= 10; h++) {
                        level.setBlock(new BlockPos(cx + x, cy + h, cz + z), Blocks.DARK_OAK_LOG.defaultBlockState(), 2);
                    }
                } else if (isEdge) {
                    for (int h = 1; h <= 9; h++) {
                        if (z == -2 && Math.abs(x) <= 2 && h <= 3) {
                            level.setBlock(new BlockPos(cx + x, cy + h, cz + z), Blocks.AIR.defaultBlockState(), 2); // Puerta principal
                        } else {
                            level.setBlock(new BlockPos(cx + x, cy + h, cz + z), Blocks.CALCITE.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        // Tejado oriental de tejas de pizarra profunda (2 niveles)
        for (int tier = 0; tier <= 1; tier++) {
            int yRoof = cy + 10 + tier * 3;
            int rX = 14 - tier * 4;
            int rZMin = -4 + tier * 3;
            int rZMax = 24 - tier * 3;
            for (int x = -rX; x <= rX; x++) {
                for (int z = rZMin; z <= rZMax; z++) {
                    if (Math.abs(x) == rX || z == rZMin || z == rZMax) {
                        level.setBlock(new BlockPos(cx + x, yRoof, cz + z), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(), 2);
                    } else if (tier == 1) {
                        level.setBlock(new BlockPos(cx + x, yRoof, cz + z), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState(), 2);
                    }
                }
            }
        }

        // D. Salón del Trono de la Eternidad en (cx, cy + 1, cz + 14)
        for (int x = -1; x <= 1; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 14), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy + 2, cz + 14), Blocks.PURPLE_GLAZED_TERRACOTTA.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 14), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        // Alfombra púrpura ceremonial
        for (int z = 0; z <= 13; z++) {
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
        }

        // E. Punto de Teletransporte de Inazuma
        TeyvatWorldGenManager.generateTeleportWaypoint(level, new BlockPos(cx - 8, cy + 1, cz - 10));

        // F. Raiden Shogun ante el Trono del Palacio
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 12), ModEntities.SERIES_NPC.get(), NpcProfile.RAIDEN_SHOGUN);

        inazumaTenshukakuBuilt = true;
        AwakeningSystemMod.LOGGER.info("[Teyvat Capital] Palacio Tenshukaku y Jardín de Inazuma erigidos en: ({}, {}, {})", cx, cy, cz);
    }

    /**
     * 4. LA GRAN ACADEMIYA Y EL ÁRBOL DIVINO (SUMERU CAPITAL).
     * Ubicación: (-650, 68, 150) en teyvat_dharma_forest.
     * Monumental Árbol Sagrado de Sumeru, Gran Casa de Daena (biblioteca circular de sabiduría),
     * pasarelas de cobre oxidado, cascadas de agua y Hoshimi Miyabi (Guardiana de la Sabiduría).
     */
    public static synchronized void ensureSumeruAcademiya(ServerLevel level) {
        if (true) return; // Procedural Sumeru disabled

        int cx = SUMERU_POS.getX();
        int cz = SUMERU_POS.getZ();
        int cy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        if (cy < 60 || cy > 130) cy = 68;

        if (level.getBlockState(new BlockPos(cx, cy + 12, cz)).is(Blocks.JUNGLE_WOOD)) {
            sumeruAcademiyaBuilt = true;
            return;
        }

        buildAdaptiveFoundation(level, cx - 28, cx + 28, cz - 28, cz + 28, cy,
                Blocks.MUD_BRICKS.defaultBlockState(), Blocks.JUNGLE_WOOD.defaultBlockState());

        // A. Gran Árbol Sagrado de la Sabiduría (Tronco central de 5x5 hasta Y=cy + 20)
        for (int h = 1; h <= 20; h++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 3) {
                        level.setBlock(new BlockPos(cx + dx, cy + h, cz + dz), Blocks.JUNGLE_WOOD.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Copa inmensa de hojas de azalea y bayas brillantes
        for (int lx = -18; lx <= 18; lx++) {
            for (int lz = -18; lz <= 18; lz++) {
                int distSq = lx * lx + lz * lz;
                if (distSq <= 18 * 18) {
                    for (int lh = 18; lh <= 24; lh++) {
                        if ((distSq <= 14 * 14) || ((lx + lz + lh) % 2 == 0)) {
                            BlockPos lp = new BlockPos(cx + lx, cy + lh, cz + lz);
                            level.setBlock(lp, (distSq % 3 == 0) ? Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState() : Blocks.AZALEA_LEAVES.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // B. Gran Casa de Daena (Biblioteca circular que abraza el Árbol Sagrado)
        for (int x = -20; x <= 20; x++) {
            for (int z = -20; z <= 20; z++) {
                int distSq = x * x + z * z;
                if (distSq >= 6 * 6 && distSq <= 20 * 20) {
                    BlockPos p = new BlockPos(cx + x, cy, cz + z);
                    level.setBlock(p, Blocks.PACKED_MUD.defaultBlockState(), 2);

                    // Muros de estanterías en el círculo exterior
                    if (distSq >= 18 * 18) {
                        for (int h = 1; h <= 5; h++) {
                            level.setBlock(new BlockPos(cx + x, cy + h, cz + z), Blocks.BOOKSHELF.defaultBlockState(), 2);
                        }
                    } else if (distSq % 7 == 0) {
                        level.setBlock(p.above(), Blocks.LECTERN.defaultBlockState(), 2);
                    } else if (distSq % 11 == 0) {
                        level.setBlock(p.above(), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
                    }
                }
            }
        }

        // C. Pasarelas y Cúpula de Cobre Oxidado
        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                int d = Math.max(Math.abs(x), Math.abs(z));
                if (d == 14) {
                    level.setBlock(new BlockPos(cx + x, cy + 6, cz + z), Blocks.OXIDIZED_CUT_COPPER_SLAB.defaultBlockState(), 2);
                }
            }
        }

        // D. Canales de agua y fuente de loto
        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy, cz - 12), Blocks.WATER.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy, cz + 12), Blocks.WATER.defaultBlockState(), 2);
        }

        // E. Punto de Teletransporte de Sumeru
        TeyvatWorldGenManager.generateTeleportWaypoint(level, new BlockPos(cx + 8, cy + 1, cz + 8));

        // F. Hoshimi Miyabi (Guardiana de la Sabiduría de la Academiya)
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz - 4), ModEntities.SERIES_NPC.get(), NpcProfile.HOSHIMI_MIYABI);

        sumeruAcademiyaBuilt = true;
        AwakeningSystemMod.LOGGER.info("[Teyvat Capital] La Gran Academiya y Árbol Sagrado de Sumeru erigidos en: ({}, {}, {})", cx, cy, cz);
    }

    /**
     * 5. PALAIS MERMONIA Y FUENTE DE LUCINE (FONTAINE CAPITAL).
     * Ubicación: (-450, 68, -450) en teyvat_fontaine_waters.
     * Monumental palacio neoclásico de mármol blanco, columnas de cuarzo estriado, teatro
     * de la Ópera Epiclese con gran araña de cristal, Fuente de Lucine y March 7th.
     */
    public static synchronized void ensureFontainePalais(ServerLevel level) {
        if (true) return; // Procedural Fontaine disabled

        int cx = FONTAINE_POS.getX();
        int cz = FONTAINE_POS.getZ();
        int cy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        if (cy < 60 || cy > 130) cy = 68;

        if (level.getBlockState(new BlockPos(cx, cy + 8, cz + 10)).is(Blocks.QUARTZ_PILLAR)) {
            fontainePalaisBuilt = true;
            return;
        }

        buildAdaptiveFoundation(level, cx - 28, cx + 28, cz - 28, cz + 28, cy,
                Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.QUARTZ_BRICKS.defaultBlockState());

        // A. Gran Fuente de Lucine en (cx, cy, cz - 14)
        int fx = cx;
        int fz = cz - 14;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                int distSq = x * x + z * z;
                BlockPos p = new BlockPos(fx + x, cy, fz + z);
                if (distSq <= 25) {
                    if (distSq >= 20) {
                        level.setBlock(p.above(), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
                    } else if (distSq >= 4) {
                        level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        level.setBlock(p.above(), Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Columna central de la Fuente de Lucine con cascada
        for (int h = 1; h <= 4; h++) {
            level.setBlock(new BlockPos(fx, cy + h, fz), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(fx, cy + 5, fz), Blocks.WATER.defaultBlockState(), 2);

        // B. Palais Mermonia & Ópera Epiclese en (cx - 16..cx + 16, cz - 2..cz + 24)
        for (int x = -16; x <= 16; x++) {
            for (int z = -2; z <= 24; z++) {
                BlockPos floorPos = new BlockPos(cx + x, cy, cz + z);
                level.setBlock(floorPos, Blocks.POLISHED_DIORITE.defaultBlockState(), 2);

                // Alfombra roja imperial de la corte
                if (Math.abs(x) <= 2 && z >= 0 && z <= 20) {
                    level.setBlock(floorPos.above(), Blocks.RED_CARPET.defaultBlockState(), 2);
                }

                // Muros perimetrales de bloques de cuarzo
                boolean isEdge = (Math.abs(x) == 16 || z == -2 || z == 24);
                if (isEdge) {
                    for (int h = 1; h <= 8; h++) {
                        BlockPos wallPos = new BlockPos(cx + x, cy + h, cz + z);
                        if (z == -2 && Math.abs(x) <= 2 && h <= 4) {
                            level.setBlock(wallPos, Blocks.AIR.defaultBlockState(), 2); // Puerta principal
                        } else if (h >= 3 && h <= 6 && (Math.abs(x) == 8 || z == 11)) {
                            level.setBlock(wallPos, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(wallPos, Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        // Columnas monumentales de fachada neoclásica
        int[][] frontCols = {{-14, -2}, {-10, -2}, {-6, -2}, {6, -2}, {10, -2}, {14, -2}};
        for (int[] fc : frontCols) {
            for (int h = 1; h <= 8; h++) {
                level.setBlock(new BlockPos(cx + fc[0], cy + h, cz + fc[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
        }

        // Cúpula / Tejado con cobre cortado y prismarina
        for (int x = -16; x <= 16; x++) {
            for (int z = -2; z <= 24; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 9, cz + z), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
            }
        }
        // Gran Araña de Cristal de la Ópera en el centro del teatro
        level.setBlock(new BlockPos(cx, cy + 8, cz + 10), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz + 10), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 7, cz + 10), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 7, cz + 10), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz + 9), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz + 11), Blocks.END_ROD.defaultBlockState(), 2);

        // C. Punto de Teletransporte de Fontaine
        TeyvatWorldGenManager.generateTeleportWaypoint(level, new BlockPos(cx + 10, cy + 1, cz - 8));

        // D. March 7th en la corte del Palais Mermonia
        SeriesStructureBuilder.spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.SERIES_NPC.get(), NpcProfile.MARCH_7TH);
        fontainePalaisBuilt = true;
    }

    public static void cleanupProceduralCitadelAndPlaza(ServerLevel level) {
        try {
            // 1. Limpiar Ciudadela de Favonius antigua en (0, -320)
            int cx = MONDSTADT_POS.getX();
            int cz = MONDSTADT_POS.getZ();
            int cy = level.getHeight(Heightmap.Types.OCEAN_FLOOR, cx, cz);
            if (cy < 50) cy = 64;

            for (int x = -35; x <= 35; x++) {
                for (int z = -45; z <= 30; z++) {
                    int colX = cx + x;
                    int colZ = cz + z;
                    for (int y = cy + 25; y >= cy - 6; y--) {
                        BlockPos p = new BlockPos(colX, y, colZ);
                        BlockState s = level.getBlockState(p);
                        if (s.is(Blocks.STONE_BRICKS) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.POLISHED_ANDESITE)
                                || s.is(Blocks.STONE_BRICK_SLAB) || s.is(Blocks.STONE_BRICK_STAIRS)
                                || s.is(Blocks.SPRUCE_PLANKS) || s.is(Blocks.SPRUCE_STAIRS) || s.is(Blocks.DARK_OAK_PLANKS)
                                || s.is(Blocks.WHITE_WOOL) || s.is(Blocks.OAK_FENCE) || s.is(Blocks.GRINDSTONE)
                                || s.is(Blocks.LIGHTNING_ROD) || s.is(Blocks.RED_CARPET) || s.is(Blocks.QUARTZ_STAIRS)
                                || s.is(Blocks.SMOOTH_QUARTZ) || s.is(Blocks.SMOOTH_QUARTZ_SLAB) || s.is(Blocks.CHISELED_STONE_BRICKS)
                                || s.is(Blocks.SEA_LANTERN) || s.is(Blocks.RED_BED)) {
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

            // 2. Limpiar Plaza de Llegada antigua en (0, 0)
            int sx = 0;
            int sz = 0;
            int sy = level.getHeight(Heightmap.Types.OCEAN_FLOOR, sx, sz);
            if (sy < 50) sy = 64;

            for (int x = -15; x <= 15; x++) {
                for (int z = -15; z <= 15; z++) {
                    int colX = sx + x;
                    int colZ = sz + z;
                    for (int y = sy + 15; y >= sy - 2; y--) {
                        BlockPos p = new BlockPos(colX, y, colZ);
                        BlockState s = level.getBlockState(p);
                        if (s.is(Blocks.SMOOTH_STONE) || s.is(Blocks.CHISELED_STONE_BRICKS)
                                || s.is(Blocks.POLISHED_DEEPSLATE_WALL) || s.is(Blocks.SOUL_LANTERN)
                                || s.is(Blocks.STONE_BRICK_SLAB) || s.is(Blocks.STONE_BRICKS)) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                    int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, colX, colZ);
                    if (topY > 40) {
                        BlockPos ground = new BlockPos(colX, topY - 1, colZ);
                        BlockState gs = level.getBlockState(ground);
                        if (gs.is(Blocks.DIRT) || gs.is(Blocks.STONE)) {
                            level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        }
                    }
                }
            }
            AwakeningSystemMod.LOGGER.info("[Teyvat Cleaner] Estructuras procedurales antiguas limpiadas y terreno restaurado a la naturaleza.");
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("[Teyvat Cleaner] Error durante la limpieza de estructuras procedurales", e);
        }
    }

    // =========================================================================
    // ÁRBOLES 100% CANÓNICOS Y PROCEDURALES DE TEYVAT E ISEKAI
    // =========================================================================

    public static boolean canPlaceTree(LevelAccessor level, BlockPos origin, int clearRadius, int height) {
        return canPlaceTree(level, origin, clearRadius, height, false);
    }

    public static boolean canPlaceTree(LevelAccessor level, BlockPos origin, int clearRadius, int height, boolean fromSapling) {
        // 1. Debe existir suelo sólido fértil debajo
        BlockState ground = level.getBlockState(origin.below());
        if (!ground.is(BlockTags.DIRT) && !ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.MOSS_BLOCK)
                && !ground.is(BlockTags.SAND) && !ground.is(Blocks.PODZOL) && !ground.is(Blocks.FARMLAND)
                && !ground.is(Blocks.STONE) && !ground.is(Blocks.SANDSTONE) && !ground.is(Blocks.COARSE_DIRT)
                && !ground.is(Blocks.NETHERRACK) && !ground.is(Blocks.CRIMSON_NYLIUM) && !ground.is(Blocks.WARPED_NYLIUM)
                && !ground.is(Blocks.SOUL_SOIL) && !ground.is(Blocks.SOUL_SAND) && !ground.is(Blocks.BASALT) && !ground.is(Blocks.BLACKSTONE)) {
            return false;
        }

        // 2. Comprobar despeje vertical mínimo del tronco principal
        int checkHeight = Math.min(height, 8);
        for (int dy = 1; dy <= checkHeight; dy++) {
            BlockPos p = origin.above(dy);
            BlockState s = level.getBlockState(p);
            if (!s.isAir() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.REPLACEABLE) && !s.is(BlockTags.FLOWERS)) {
                return false;
            }
        }

        // 3. Si es generación natural del mundo, comprobar espaciado para no apiñar troncos
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
            leafState = leafState.setValue(LeavesBlock.PERSISTENT, true);
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
            leafState = leafState.setValue(LeavesBlock.PERSISTENT, true);
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

        // 1. Raíces en la base
        placeRootFlares(level, origin, log, 1);

        // 2. Tronco principal
        int forkY = height - 3;
        for (int dy = 0; dy <= forkY; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        // 3. Ramas en cáliz que se abren hacia los 4 cuadrantes
        BlockPos forkCenter = origin.above(forkY);
        BlockPos tip1 = forkCenter.offset(2, 2, 1);
        BlockPos tip2 = forkCenter.offset(-2, 2, 1);
        BlockPos tip3 = forkCenter.offset(0, 2, -2);
        BlockPos tip4 = forkCenter.offset(1, 2, 2);

        placeBranch(level, forkCenter, tip1, log);
        placeBranch(level, forkCenter, tip2, log);
        placeBranch(level, forkCenter, tip3, log);
        placeBranch(level, forkCenter, tip4, log);

        // 4. Cúmulos de follaje esféricos sobre cada rama con frutos
        placeLeafCluster(level, tip1, 2.6, 2.0, leaves);
        placeLeafCluster(level, tip2, 2.6, 2.0, leaves);
        placeLeafCluster(level, tip3, 2.6, 2.0, leaves);
        placeLeafCluster(level, tip4, 2.6, 2.0, leaves);

        // 5. Cúpula central exuberante
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

        // 1. Raíces tabulares gigantescas (Buttress roots) de 2 a 3 bloques de altura
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos r1 = origin.relative(dir);
            BlockPos r2 = origin.relative(dir, 2);
            placeOrientedLog(level, r1, log, Direction.Axis.Y);
            placeOrientedLog(level, r1.above(), log, Direction.Axis.Y);
            placeOrientedLog(level, r2, log, Direction.Axis.Y);
            if (level.getBlockState(r1.below()).isAir()) placeOrientedLog(level, r1.below(), log, Direction.Axis.Y);
            if (level.getBlockState(r2.below()).isAir()) placeOrientedLog(level, r2.below(), log, Direction.Axis.Y);
        }

        // 2. Tronco colosal
        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        // 3. Dosel intermedio tipo paraguas tropical (a 2/3 de altura)
        int midY = height - 6;
        BlockPos midFork = origin.above(midY);
        Direction sideDir = Direction.from2DDataValue(rand.nextInt(4));
        BlockPos midTip = midFork.relative(sideDir, 3).above(1);
        placeBranch(level, midFork, midTip, log);
        placeLeafCluster(level, midTip, 3.2, 1.6, leaves);
        placeHangingTendril(level, midTip.below(), 2 + rand.nextInt(2), leaves);
        placeHangingTendril(level, midTip.relative(sideDir.getClockWise()).below(), 2, leaves);

        // 4. Gran dosel superior con 4 ramas maestras arqueadas
        BlockPos topFork = origin.above(height - 2);
        BlockPos topNorth = topFork.offset(0, 2, -3);
        BlockPos topSouth = topFork.offset(0, 2, 3);
        BlockPos topEast = topFork.offset(3, 2, 0);
        BlockPos topWest = topFork.offset(-3, 2, 0);

        placeBranch(level, topFork, topNorth, log);
        placeBranch(level, topFork, topSouth, log);
        placeBranch(level, topFork, topEast, log);
        placeBranch(level, topFork, topWest, log);

        // 5. Cúmulos de follaje tropical denso
        placeLeafCluster(level, topNorth, 3.2, 2.0, leaves);
        placeLeafCluster(level, topSouth, 3.2, 2.0, leaves);
        placeLeafCluster(level, topEast, 3.2, 2.0, leaves);
        placeLeafCluster(level, topWest, 3.2, 2.0, leaves);

        // Corona central masiva y tendrils colgantes
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
        int height = 24 + rand.nextInt(8); // 24 a 31 bloques
        if (!canPlaceTree(level, origin, 6, height + 6, fromSapling)) {
            return generateStandardAdhigama(level, origin, rand, fromSapling);
        }

        BlockState log = SystemBlocks.ADHIGAMA_LOG.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ADHIGAMA_LEAVES.get().defaultBlockState();

        // 1. Enormes raíces tabulares (Buttress roots) escalonadas de 4 bloques de altura y 4 de radio
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

        // 2. Tronco masivo 3x3 en la base (Y=0 a Y=4) y 2x2 hasta el dosel
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

        // 3. Dosel Nivel 1 (Repisa baja a Y=13)
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

        // 4. Dosel Nivel 2 (Dosel intermedio a Y=19)
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

        // 5. Dosel Nivel 3 (Corona monumental superior)
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

        // Gran cúpula central superior
        placeLeafCluster(level, origin.above(height + 1), 5.8, 3.4, leaves);
        placeLeafCluster(level, origin.above(height + 4), 3.6, 2.0, leaves);

        // Zarcillos colgantes de la corona
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

        // 1. Raíces basales ensanchadas
        placeRootFlares(level, origin, log, 1);

        // 2. Tronco orgánico con leve inclinación/curva natural
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

        // 3. Ramas laterales horizontales y diagonales
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

        // 4. Nubes elipsoidales de follaje dorado en cada punta de rama
        placeLeafCluster(level, bough1, 2.6, 1.5, leaves);
        placeLeafCluster(level, bough2, 2.6, 1.5, leaves);
        placeLeafCluster(level, bough3, 2.4, 1.4, leaves);
        placeLeafCluster(level, bough4, 2.4, 1.4, leaves);

        // 5. Cúpula dorada superior principal
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

    // ==========================================
    // ÁRBOLES DE INAZUMA (Aralia, Otogi, Yumemiru, Maple)
    // ==========================================

    public static boolean generateAraliaTree(LevelAccessor level, BlockPos origin, RandomSource rand, boolean fromSapling) {
        int height = 8 + rand.nextInt(4);
        if (!canPlaceTree(level, origin, 3, height + 3, fromSapling)) return false;

        BlockState log = SystemBlocks.ARALIA_WOOD.get().defaultBlockState();
        BlockState leaves = SystemBlocks.ARALIA_LEAVES.get().defaultBlockState();

        placeRootFlares(level, origin, log, 1);
        for (int dy = 0; dy <= height; dy++) {
            placeOrientedLog(level, origin.above(dy), log, Direction.Axis.Y);
        }

        // Follaje cónico escalonado tipo cedro japonés
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
        // Tronco con curva sinuosa estilo bonsai natural
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

        // Nubes de flor de cerezo (Sakura) con ramas abiertas y caída llorona
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
            // Colgar algunas hojas hacia abajo para dar efecto de cerezo llorón
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

        // Ramas en sombrilla otoñal amplia (Momiji)
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

