package com.soybe.awakeningsystem;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MurimWorldManager {
    private MurimWorldManager() {}

    public static final ResourceKey<Level> MURIM_WORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "murim_world")
    );

    public static final int SECT_ISLAND_Y = 235;
    public static final BlockPos SECT_SPAWN_POS = new BlockPos(0, SECT_ISLAND_Y + 2, 0);
    public static final BlockPos RETURN_PORTAL_POS = new BlockPos(0, SECT_ISLAND_Y + 2, 14);

    private static volatile boolean sectIslandGenerated = false;

    public static void teleportToMurim(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel murimLevel = server.getLevel(MURIM_WORLD);
        if (murimLevel == null) {
            player.displayClientMessage(Component.translatable("awakening_system.murim.dimension_unavailable"), true);
            return;
        }

        // Si estaba en la Torre, remover sesion
        if (TowerManager.isInTower(player.getUUID())) {
            TowerManager.exitTowerToDimension(player, murimLevel, SECT_SPAWN_POS);
        }

        // Asegurar la generación del complejo de la secta inmortal flotante
        ensureSectIsland(murimLevel);

        // Teletransportar al patio ceremonial de la secta
        player.teleportTo(murimLevel, 0.5D, (double) SECT_SPAWN_POS.getY(), 0.5D, 180.0F, 0.0F);

        // Actualizar datos del jugador al Sistema 3.0 (Reino Murim) con protocolo de 5 minutos (6000 ticks)
        SystemData data = SystemEvents.data(player);
        if (!data.system30Updated() && !data.isSystem30Updating()) {
            data.setSystem30Updating(true);
            data.setSystem30UpdateRemainingTicks(5 * 60 * 20);
            AwakeningSystemMod.sync(player);

            // Efectos cinematográficos de ascensión espiritual
            murimLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, 0.5D, SECT_ISLAND_Y + 2.5D, 0.5D, 150, 2.0D, 1.5D, 2.0D, 0.3D);
            murimLevel.sendParticles(ParticleTypes.END_ROD, 0.5D, SECT_ISLAND_Y + 2.0D, 0.5D, 100, 2.0D, 1.2D, 2.0D, 0.08D);
            murimLevel.sendParticles(ParticleTypes.DRAGON_BREATH, 0.5D, SECT_ISLAND_Y + 2.0D, 0.5D, 60, 1.5D, 1.0D, 1.5D, 0.05D);

            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.4F, 0.9F);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.4F);

            AwakeningSystemMod.notify(player,
                    "awakening_system.murim.update_protocol_title",
                    "awakening_system.murim.update_protocol_desc",
                    0xFFFFD700);

            player.displayClientMessage(Component.translatable("awakening_system.hud.system_30_update_protocol_started"), true);
        } else {
            murimLevel.sendParticles(ParticleTypes.END_ROD, 0.5D, SECT_ISLAND_Y + 2.0D, 0.5D, 40, 1.5D, 1.0D, 1.5D, 0.05D);
            player.playNotifySound(ModSounds.GATE_TRAVEL.value(), SoundSource.PLAYERS, 0.8F, 0.85F);
            player.displayClientMessage(Component.translatable("awakening_system.dimension.murim_welcome"), true);
        }
    }

    public static void completeSystem30Update(ServerPlayer player, SystemData data) {
        data.setSystem30Updating(false);
        data.setSystem30UpdateRemainingTicks(0);
        data.setSystem30Updated(true);
        data.setMurimUnlocked(true);
        if (data.dantianRealm() == 0) {
            data.setDantianRealm(1); // Despierta el Dantian al Reino de Condensación de Qi
        }
        data.addCoins(200000);
        data.addExperience(35000);
        data.addCultivationQi(500);

        AwakeningSystemMod.sync(player);

        ServerLevel sl = player.serverLevel();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py + 1.5D, pz, 300, 2.5D, 2.0D, 2.5D, 0.4D);
        sl.sendParticles(ParticleTypes.FLASH, px, py + 1.5D, pz, 10, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.FIREWORK, px, py + 2.0D, pz, 100, 1.5D, 1.5D, 1.5D, 0.2D);
        sl.sendParticles(ParticleTypes.DRAGON_BREATH, px, py + 1.5D, pz, 80, 2.0D, 1.5D, 2.0D, 0.05D);

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
        player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.0F);
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 1.3F);

        AwakeningSystemMod.notify(player,
                "awakening_system.murim.system_30_complete_title",
                "awakening_system.murim.system_30_complete_desc",
                0xFFFFD700);

        if (data.murimPhysique().isEmpty()) {
            player.displayClientMessage(Component.translatable("awakening_system.dimension.murim_martial_trial"), false);
        }

        AwakeningSystemMod.notifyAll(player.getServer(),
                "awakening_system.murim.universal_broadcast_title",
                "awakening_system.murim.universal_broadcast_desc",
                0xFFFFD700, player.getName().getString());
    }

    public static synchronized void ensureSectIsland(ServerLevel level) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            DimensionGenSavedData savedData = DimensionGenSavedData.get(server);
            if (savedData.isMurimSectGenerated()) {
                sectIslandGenerated = true;
                return;
            }
        }

        if (!sectIslandGenerated) {
            // Cargar chunks esenciales que cubren la isla entera (-2 a +2, 25 chunks)
            for (int cx = -2; cx <= 2; cx++) {
                for (int cz = -2; cz <= 2; cz++) {
                    level.getChunk(cx, cz);
                }
            }

            generateSectIsland(level);
            sectIslandGenerated = true;
            if (server != null) {
                DimensionGenSavedData.get(server).setMurimSectGenerated(true);
            }
        }
    }

    private static final int FAST_FLAG = 18;

    /**
     * Genera la Isla Flotante Central de la Gran Secta Inmortal con su Pagoda,
     * Pabellón de Alquimia con cofres de Ginseng y portal de retorno a Yggdrasil.
     */
    public static void generateSectIsland(ServerLevel level) {
        RandomSource random = RandomSource.create(level.getSeed() ^ 0x4D5552494D3330L);
        BlockPos center = new BlockPos(0, SECT_ISLAND_Y, 0);

        int r = 24;

        // 1. Cuerpo de la Isla Flotante (Cono invertido orgánico suspendido en el cielo)
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= r) {
                    // Profundidad de la roca flotante
                    int maxDepth = (int) Math.round((1.0 - (dist / r)) * 16.0) + (int)(Math.sin(dx * 0.4) * 1.5);
                    
                    // Capa superficial
                    BlockPos top = center.offset(dx, 0, dz);
                    level.setBlock(top, Blocks.GRASS_BLOCK.defaultBlockState(), FAST_FLAG);
                    
                    // Subsuelo de tierra y piedra flotante (cáscara optimizada para rendimiento)
                    for (int depth = 1; depth <= maxDepth; depth++) {
                        if (depth <= 3 || depth >= maxDepth - 2 || dist >= r * 0.7) {
                            BlockPos p = top.below(depth);
                            if (depth <= 3) {
                                level.setBlock(p, Blocks.DIRT.defaultBlockState(), FAST_FLAG);
                            } else if (depth <= 8) {
                                level.setBlock(p, (depth % 2 == 0) ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState(), FAST_FLAG);
                            } else {
                                level.setBlock(p, ((dx + dz) % 3 == 0) ? Blocks.DRIPSTONE_BLOCK.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState(), FAST_FLAG);
                            }
                        }
                    }

                    // Estalactitas colgantes bajo la isla (apuntando hacia abajo)
                    if (maxDepth > 6 && random.nextInt(6) == 0) {
                        BlockPos dripBase = top.below(maxDepth);
                        level.setBlock(dripBase, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), FAST_FLAG);
                        BlockPos tip = top.below(maxDepth + 1);
                        level.setBlock(tip, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                                .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP), FAST_FLAG);
                    }
                }
            }
        }

        // 2. Gran Pabellón Imperial de Bienvenida de la Secta Inmortal
        buildSectPagoda(level, center);

        // 3. Portal de Retorno Ceremonial en (0, SECT_ISLAND_Y + 1, 14)
        buildReturnGate(level, center.offset(0, 1, 14));

        // 4. Plantas silvestres raras de Qingseng (Ginseng Milenario) dispersas orgánicamente
        scatterWildGinseng(level, center, 28, 2, random);
    }

    private static void buildSectPagoda(ServerLevel level, BlockPos origin) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // 1. Cimientos y Gran Terraza Octogonal Elevada (Y = cy - 2 hasta cy + 1)
        int pr = 10;
        for (int dx = -pr; dx <= pr; dx++) {
            for (int dz = -pr; dz <= pr; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= pr) {
                    // Cimientos hacia abajo
                    for (int dy = -2; dy <= 0; dy++) {
                        BlockPos bp = new BlockPos(cx + dx, cy + dy, cz + dz);
                        level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), FAST_FLAG);
                    }
                    // Pavimento superficial (cy + 1)
                    BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
                    BlockState floor;
                    if (d >= pr - 1.2) {
                        floor = Blocks.GILDED_BLACKSTONE.defaultBlockState();
                    } else if (d <= 3.5) {
                        floor = ((dx + dz) % 2 == 0) ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState();
                    } else {
                        floor = Blocks.SMOOTH_QUARTZ.defaultBlockState();
                    }
                    level.setBlock(p, floor, FAST_FLAG);
                }
            }
        }

        // 2. Escaleras de Acceso en las 4 Direcciones Cardinales (Norte, Sur, Este, Oeste)
        for (int i = -2; i <= 2; i++) {
            level.setBlock(new BlockPos(cx + i, cy + 1, cz - pr), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH), FAST_FLAG);
            level.setBlock(new BlockPos(cx + i, cy + 1, cz + pr), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH), FAST_FLAG);
            level.setBlock(new BlockPos(cx - pr, cy + 1, cz + i), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST), FAST_FLAG);
            level.setBlock(new BlockPos(cx + pr, cy + 1, cz + i), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST), FAST_FLAG);
        }

        // 3. Estrado Central de Meditación Qi & Yin-Yang en (cx, cy + 1, cz)
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.SEA_LANTERN.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.LIGHT.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 1), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx, cy + 1, cz - 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), FAST_FLAG);

        // 4. 8 Columnas Vermilion / Roble Oscuro del Primer Nivel (del suelo cy + 2 a cy + 8)
        int[][] outerPillars = {
                {-6, -3}, {-6, 3}, {6, -3}, {6, 3},
                {-3, -6}, {3, -6}, {-3, 6}, {3, 6}
        };
        for (int[] pil : outerPillars) {
            // Plinto
            level.setBlock(new BlockPos(cx + pil[0], cy + 2, cz + pil[1]), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), FAST_FLAG);
            for (int h = 3; h <= 7; h++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + h, cz + pil[1]), Blocks.DARK_OAK_LOG.defaultBlockState(), FAST_FLAG);
            }
            // Capitel
            level.setBlock(new BlockPos(cx + pil[0], cy + 8, cz + pil[1]), Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
        }

        // 5. Vigas Maestras y Dinteles Horizontales que unen todas las columnas (cy + 8)
        // Cierran por completo el perímetro sin huecos flotantes
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);
                boolean isRing = (absX == 6 && absZ <= 3) || (absZ == 6 && absX <= 3) || (absX + absZ == 9);
                if (isRing) {
                    level.setBlock(new BlockPos(cx + dx, cy + 8, cz + dz), Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(), FAST_FLAG);
                }
            }
        }

        // 6. Alero Inferior Curvado Tradicional (cy + 8 y cy + 9) con ménsulas y tejas de prismarina oscura
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int maxD = Math.max(Math.abs(dx), Math.abs(dz));
                int sumD = Math.abs(dx) + Math.abs(dz);
                if (maxD == 7 || (maxD == 8 && sumD >= 14)) {
                    // Borde exterior del alero con curva alzada en las esquinas
                    boolean corner = (sumD >= 14);
                    int eaveY = corner ? cy + 9 : cy + 8;
                    level.setBlock(new BlockPos(cx + dx, eaveY, cz + dz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), FAST_FLAG);
                } else if (maxD == 6) {
                    level.setBlock(new BlockPos(cx + dx, cy + 9, cz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), FAST_FLAG);
                }
            }
        }

        // Faroles de Almas colgantes con cadenas bajo las 4 esquinas del alero inferior
        int[][] lanternCorners = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] lc : lanternCorners) {
            level.setBlock(new BlockPos(cx + lc[0], cy + 8, cz + lc[1]), Blocks.CHAIN.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(cx + lc[0], cy + 7, cz + lc[1]), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), FAST_FLAG);
        }

        // 7. Segundo Nivel / Torreón Interior de la Pagoda (4 Columnas Maestras Interiores)
        int[][] innerPillars = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] pil : innerPillars) {
            for (int h = 2; h <= 12; h++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + h, cz + pil[1]), Blocks.DARK_OAK_LOG.defaultBlockState(), FAST_FLAG);
            }
            level.setBlock(new BlockPos(cx + pil[0], cy + 13, cz + pil[1]), Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
        }

        // Vigas transversales del segundo nivel (cy + 12)
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 12, cz - 3), Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(cx + x, cy + 12, cz + 3), Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(), FAST_FLAG);
        }
        for (int z = -3; z <= 3; z++) {
            level.setBlock(new BlockPos(cx - 3, cy + 12, cz + z), Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(cx + 3, cy + 12, cz + z), Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(), FAST_FLAG);
        }

        // 8. Tejado Superior Piramidal de Tejas Orientales (cy + 13 a cy + 16)
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int maxD = Math.max(Math.abs(dx), Math.abs(dz));
                if (maxD == 5) {
                    level.setBlock(new BlockPos(cx + dx, cy + 13, cz + dz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), FAST_FLAG);
                } else if (maxD == 4) {
                    level.setBlock(new BlockPos(cx + dx, cy + 13, cz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), FAST_FLAG);
                } else if (maxD == 3) {
                    level.setBlock(new BlockPos(cx + dx, cy + 14, cz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), FAST_FLAG);
                } else if (maxD == 2) {
                    level.setBlock(new BlockPos(cx + dx, cy + 15, cz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), FAST_FLAG);
                } else if (maxD <= 1) {
                    level.setBlock(new BlockPos(cx + dx, cy + 16, cz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), FAST_FLAG);
                }
            }
        }

        // 9. Aguja Celestial Dorada de Coronación (cy + 17 y cy + 18)
        level.setBlock(new BlockPos(cx, cy + 17, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx, cy + 18, cz), Blocks.END_ROD.defaultBlockState(), FAST_FLAG);
    }

    public static void ensureReturnPortal(ServerLevel level) {
        BlockPos returnPos = RETURN_PORTAL_POS;
        if (!level.getBlockState(returnPos.offset(2, 3, 0)).is(Blocks.SEA_LANTERN)) {
            buildReturnGate(level, returnPos.below());
        }
        // Despejar bloques residuales en el vano para que quede 1 solo portal animado
        for (int dx = -1; dx <= 1; dx++) {
            for (int h = 1; h <= 4; h++) {
                BlockPos p = returnPos.below().offset(dx, h, 0);
                if (level.getBlockState(p).is(com.soybe.awakeningsystem.block.SystemBlocks.ASCENSION_PORTAL.get())) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), FAST_FLAG);
                }
            }
        }
    }

    private static void buildReturnGate(ServerLevel level, BlockPos gatePos) {
        // Marco Monumental de Retorno a Yggdrasil en Murim
        int cx = gatePos.getX();
        int cy = gatePos.getY(); // cy = SECT_ISLAND_Y + 1 (196)
        int cz = gatePos.getZ();

        // 1. Terraza ceremonial del portal en Murim (cy)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floorPos = new BlockPos(cx + dx, cy, cz + dz);
                boolean edge = (Math.abs(dx) == 3 || Math.abs(dz) == 2);
                level.setBlock(floorPos, edge ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState(), FAST_FLAG);
            }
        }

        // 2. Pilares de Piedra Profunda y Obsidiana Llorosa en X = -2 y X = +2 (cy + 1 a cy + 4)
        for (int sign : new int[]{-2, 2}) {
            int px = cx + sign;
            level.setBlock(new BlockPos(px, cy + 1, cz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(px, cy + 2, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(px, cy + 3, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(px, cy + 4, cz), Blocks.SEA_LANTERN.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(px, cy + 5, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), FAST_FLAG);
        }

        // 3. Dintel y tejado de pagoda tradicional china a cy + 5 y cy + 6
        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 5, cz), Blocks.DARK_PRISMARINE.defaultBlockState(), FAST_FLAG);
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), FAST_FLAG);
        }
        // Aleros alzados en los extremos (X = ±4)
        level.setBlock(new BlockPos(cx - 4, cy + 6, cz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx + 4, cy + 6, cz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), FAST_FLAG);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), FAST_FLAG);

        // 4. Vano interior del portal: despejado con aire para renderizar 1 solo portal vórtice dimensional unificado (gate_murim.png)
        for (int dx = -1; dx <= 1; dx++) {
            for (int h = 1; h <= 4; h++) {
                BlockPos portalPos = new BlockPos(cx + dx, cy + h, cz);
                level.setBlock(portalPos, Blocks.AIR.defaultBlockState(), FAST_FLAG);
            }
        }
    }

    private static void cleanupUnwantedMossAndFoliage(ServerLevel level, BlockPos center) {
        BlockPos mossArea = center.offset(15, 1, 0);
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -2; dy <= 4; dy++) {
                    BlockPos p = mossArea.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    if (state.is(Blocks.MOSS_BLOCK)) {
                        level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), FAST_FLAG);
                    } else if (state.is(Blocks.FLOWERING_AZALEA) || state.is(Blocks.AZALEA) || state.is(Blocks.BAMBOO)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), FAST_FLAG);
                    }
                }
            }
        }
    }

    public static void scatterWildGinseng(ServerLevel level, BlockPos center, int radius, int count, RandomSource random) {
        int placed = 0;
        for (int attempts = 0; attempts < count * 10 && placed < count; attempts++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dz * dz < 25) continue;
            BlockPos target = center.offset(dx, 0, dz);
            BlockPos ground = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, target);
            BlockState below = level.getBlockState(ground.below());
            if ((below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.MOSS_BLOCK) || below.is(Blocks.DIRT)) && level.getBlockState(ground).isAir()) {
                level.setBlock(ground, com.soybe.awakeningsystem.block.SystemBlocks.WILD_GINSENG.get().defaultBlockState(), FAST_FLAG);
                placed++;
            }
        }
    }
}
