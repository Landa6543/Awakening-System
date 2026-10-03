package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.block.FiveElementsPedestalBlock;
import com.soybe.awakeningsystem.block.WuXingElement;

import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.ElementalType;
import com.soybe.awakeningsystem.block.ElementalAltarBlock;
import com.soybe.awakeningsystem.block.ElementalBarrierBlock;
import com.soybe.awakeningsystem.entity.ElementalLordBossEntity;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
import com.soybe.awakeningsystem.block.CampfireLogSeatBlock;
import com.soybe.awakeningsystem.block.LockedTreasureChestBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.SubwayRailTrackBlock;
import com.soybe.awakeningsystem.block.SubwayWideRailTrackBlock;
import com.soybe.awakeningsystem.block.SubwayTrainWheelBlock;
import com.soybe.awakeningsystem.block.SubwayDigitalScreenBlock;
import com.soybe.awakeningsystem.block.SubwayGrabHandleBlock;
import com.soybe.awakeningsystem.block.ArchitectDraftingTableBlock;
import com.soybe.awakeningsystem.block.DimensionalRiftStandBlock;
import com.soybe.awakeningsystem.block.TavernChairBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;

import com.soybe.awakeningsystem.entity.ModEntities;
import com.soybe.awakeningsystem.entity.NpcProfile;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.StairsShape;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.server.network.Filterable;

import java.util.List;

public final class SeriesStructureBuilder {
    private SeriesStructureBuilder() {}

    public static void ensureFoundation(ServerLevel level, int minX, int maxX, int minZ, int maxZ, int ySurface, net.minecraft.world.level.block.Block block) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = ySurface; y >= ySurface - 32; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    // Solo detener el cimiento si es terreno sólido real (no hojas, no troncos flotantes, no agua)
                    if (s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS) && s.getFluidState().isEmpty() && !s.is(BlockTags.REPLACEABLE)) {
                        break;
                    }
                    level.setBlock(p, block.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void clearRoom(ServerLevel level, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    public static BlockState getOutwardStair(net.minecraft.world.level.block.Block stairBlock, int dx, int dz, int rx, int rz) {
        BlockState state = stairBlock.defaultBlockState();
        boolean isNorth = (dz == -rz);
        boolean isSouth = (dz == rz);
        boolean isWest = (dx == -rx);
        boolean isEast = (dx == rx);

        if (isNorth && isWest) {
            return state.setValue(StairBlock.FACING, Direction.NORTH)
                        .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
        } else if (isNorth && isEast) {
            return state.setValue(StairBlock.FACING, Direction.NORTH)
                        .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        } else if (isSouth && isWest) {
            return state.setValue(StairBlock.FACING, Direction.SOUTH)
                        .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        } else if (isSouth && isEast) {
            return state.setValue(StairBlock.FACING, Direction.SOUTH)
                        .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
        } else if (isNorth) {
            return state.setValue(StairBlock.FACING, Direction.NORTH)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        } else if (isSouth) {
            return state.setValue(StairBlock.FACING, Direction.SOUTH)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        } else if (isWest) {
            return state.setValue(StairBlock.FACING, Direction.WEST)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        } else if (isEast) {
            return state.setValue(StairBlock.FACING, Direction.EAST)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        }
        return state;
    }

    public static void spawnNpcIfAbsent(ServerLevel level, BlockPos pos, EntityType<SeriesNpcEntity> type, NpcProfile profile) {
        level.getChunk(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 2);

        // Asegurar que el suelo bajo el NPC sea firme y s\u00f3lido
        if (!level.getBlockState(pos.below()).isSolid()) {
            level.setBlock(pos.below(), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }

        // Comprobar si ya existe el NPC en un radio estrecho (6 bloques) de esta posici\u00f3n concreta
        AABB box = new AABB(pos).inflate(6.0D);
        List<SeriesNpcEntity> nearby = level.getEntitiesOfClass(SeriesNpcEntity.class, box, e -> e.isAlive() && !e.isRemoved() && e.getNpcProfile() == profile);
        if (nearby.isEmpty()) {
            SeriesNpcEntity npc = new SeriesNpcEntity(type, level, profile);
            npc.moveTo(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D, 0.0F, 0.0F);
            npc.setHomePos(pos);
            npc.setPersistenceRequired();
            level.addFreshEntityWithPassengers(npc);
        }
    }

    public static ItemStack makeRelic(Item item, int count, String name, String series, String description) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("\u00A77" + description),
                Component.literal("\u00A78[Reliquia Legendaria: " + series + "]")
        )));
        return stack;
    }

    public static ItemStack makeLoreBook(String title, String author, List<String> pages, String series, String description) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(title));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("§7" + description),
                Component.literal("§8[Lore de Yggdrasil: " + series + "]")
        )));
        try {
            var filterableTitle = Filterable.passThrough(title);
            List<Filterable<Component>> pageList = new java.util.ArrayList<>();
            for (String page : pages) {
                pageList.add(Filterable.passThrough(Component.literal(page)));
            }
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                    filterableTitle, author, 0, pageList, true
            ));
        } catch (Throwable ignored) {}
        return stack;
    }

    public static void placeLecternWithBook(ServerLevel level, BlockPos pos, Direction facing, ItemStack book) {
        BlockState state = Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, facing)
                .setValue(LecternBlock.HAS_BOOK, true);
        level.setBlock(pos, state, 3);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LecternBlockEntity lectern) {
            lectern.setBook(book);
        }
    }

    // =========================================================================
    // 1. SOLO LEVELING \u2014 SEDE CENTRAL DE CAZADORES DE LA ASOCIACI\u00d3N AHJIN
    //    (Sung Jin-Woo & Cha Hae-In)
    // =========================================================================
    public static void buildHunterOffice(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.POLISHED_DEEPSLATE);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 11, cz - 8, cz + 8);

        // 1. Pavimento corporativo de alta tecnología
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 1 && z >= -7 && z <= 2) {
                    // Alfombra central púrpura del Monarca de las Sombras
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // Círculo de Extracción de Sombras del Monarca en la parte posterior (cz = 3..7)
        for (int x = -3; x <= 3; x++) {
            for (int z = 3; z <= 7; z++) {
                int dx = x;
                int dz = z - 5;
                if (dx * dx + dz * dz <= 5) {
                    BlockPos p = new BlockPos(cx + x, cy, cz + z);
                    level.setBlock(p, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(cx, cy, cz + 5), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 5), Blocks.TINTED_GLASS.defaultBlockState(), 2);

        // Braseros ceremoniales de fuego de alma en el círculo de sombras
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz + 5), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz + 5), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        // 2. Muros exteriores modernos con relieve, pilares de la asociación y ventanales tintados
        for (int y = 1; y <= 6; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        // Gran pórtico de entrada al frente (z = -8, x = -1..1)
                        if (z == -8 && Math.abs(x) <= 1 && y <= 3) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        // Columnas maestras en esquinas y portal
                        if (Math.abs(x) == 8 && Math.abs(z) == 8) {
                            level.setBlock(p, Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
                        } else if (z == -8 && Math.abs(x) == 2) {
                            level.setBlock(p, Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
                        } else if (Math.abs(x) == 8 && z == 0) {
                            level.setBlock(p, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                        } else if (y >= 2 && y <= 5) {
                            // Ventanales modernos rehundidos con cristal tintado
                            if ((Math.abs(x) >= 3 && Math.abs(x) <= 6 && Math.abs(z) == 8) ||
                                    (Math.abs(z) >= 2 && Math.abs(z) <= 5 && Math.abs(x) == 8)) {
                                 level.setBlock(p, Blocks.TINTED_GLASS.defaultBlockState(), 2);
                            } else {
                                level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                            }
                        } else {
                            level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Marquesina voladiza sobre la entrada exterior (z = -9)
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 4, cz - 9), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy + 3, cz - 9), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 9), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 3, cz - 9), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 9), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Escalera de acceso exterior al frente
        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(cx + x, cy, cz - 9), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState(), 2);
        }

        // 3. Techo t\u00e9cnico corporativo con alero y claraboya
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 7, cz + z);
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) {
                    level.setBlock(p, Blocks.TINTED_GLASS.defaultBlockState(), 2);
                } else if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }
        }

        // Antenas de telecomunicaciones
        level.setBlock(new BlockPos(cx - 7, cy + 8, cz - 7), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 9, cz - 7), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 8, cz + 7), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 9, cz + 7), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Iluminaci\u00f3n empotrada a ras de techo
        int[][] lights = new int[][]{{-5, -4}, {5, -4}, {-5, 4}, {5, 4}};
        for (int[] lt : lights) {
            level.setBlock(new BlockPos(cx + lt[0], cy + 7, cz + lt[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        // 4. Mostrador de recepción y terminal de cazadores (Frente izquierdo)
        for (int x = -6; x <= -3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz - 5), Blocks.DARK_OAK_WOOD.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz - 4), Blocks.DARK_OAK_WOOD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 5), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 5), Blocks.BLACK_CONCRETE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz - 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // 5. Estudio de esgrima y entrenamiento de Cha Hae-In (Frente derecho)
        level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 5), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 2, cz - 5), Blocks.TARGET.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 5), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 4), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 5), Blocks.ANVIL.defaultBlockState(), 2);

        // 6. Trono del Monarca de las Sombras (Sung Jin-Woo) en el fondo
        BlockPos throneBase = new BlockPos(cx, cy + 1, cz + 6);
        level.setBlock(throneBase, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(throneBase.above(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz + 7), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 7), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz + 7), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 7), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 7), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);

        // Faroles de maná ceremoniales sobre el trono
        level.setBlock(new BlockPos(cx - 2, cy + 6, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 5, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 4, cz + 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 6, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 5, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 4, cz + 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // Cofre acorazado del Gremio Ahjin
        BlockPos chestPos = new BlockPos(cx + 6, cy + 1, cz + 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.DIAMOND_SWORD, 1, "\u00A75\u00A7lColmillo Venenoso de Rasaka", "Solo Leveling", "Daga forjada del veneno paralizante del gran gobernante de la mazmorra."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7d\u00A7lExpediente de Cazadores: Gremio Ahjin", "Solo Leveling", "Registro oficial clasificado de la Asociaci\u00F3n Coreana de Cazadores."));
            chest.setItem(2, SystemItems.coin(600));
            chest.setItem(3, new ItemStack(Items.GOLDEN_CARROT, 16));
            chest.setItem(4, new ItemStack(Items.ENDER_PEARL, 8));
            chest.setItem(5, new ItemStack(Items.POTION));
        }

        // Spawn seguro y fijado de Sung Jin-Woo y Cha Hae-In
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.SUNG_JINWOO.get(), NpcProfile.SUNG_JINWOO);
        spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz - 3), ModEntities.CHA_HAEIN.get(), NpcProfile.CHA_HAEIN);
    }

    // =========================================================================
    // 2. ORV \u2014 VAG\u00d3N 3807 Y ESTACI\u00d3N RUINOSA DE METRO DE SE\u00daL (ESCENARIO 1)
    //    (Kim Dokja & Yoo Joonghyuk)
    // =========================================================================
    public static void buildSubwayCarriage(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 10, cz + 10, cy - 2, Blocks.STONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy - 1, cy + 10, cz - 10, cz + 10);

        // 1. Túnel ferroviario subterráneo: Muros abovedados de piedra industrial agrietada
        for (int z = -10; z <= 10; z++) {
            for (int y = -1; y <= 6; y++) {
                BlockPos pL = new BlockPos(cx - 8, cy + y, cz + z);
                if ((y + z) % 5 == 0) level.setBlock(pL, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 2);
                else if ((y + z) % 7 == 0) level.setBlock(pL, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
                else level.setBlock(pL, Blocks.STONE_BRICKS.defaultBlockState(), 2);

                BlockPos pR = new BlockPos(cx + 8, cy + y, cz + z);
                if ((y + z) % 4 == 0) level.setBlock(pR, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 2);
                else level.setBlock(pR, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            }
        }

        // Techo abovedado del túnel (losas de piedra lisa y vigas de soporte)
        for (int z = -10; z <= 10; z++) {
            for (int x = -8; x <= 8; x++) {
                BlockPos p = new BlockPos(cx + x, cy + 7, cz + z);
                if (x == -8 || x == 8) {
                    level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                } else if (Math.abs(z) % 5 == 0) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 2);
                }
            }
        }

        // Iluminación industrial del túnel y cables colgantes
        for (int z : new int[]{-7, 0, 7}) {
            level.setBlock(new BlockPos(cx - 7, cy + 6, cz + z), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 7, cy + 5, cz + z), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
            level.setBlock(new BlockPos(cx + 7, cy + 6, cz + z), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 7, cy + 5, cz + z), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }
        level.setBlock(new BlockPos(cx - 7, cy + 5, cz - 9), Blocks.COBWEB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 5, cz + 9), Blocks.COBWEB.defaultBlockState(), 2);

        // 2. Andén de la estación de metro (x = -7 a -3, nivel cy + 1)
        for (int z = -10; z <= 10; z++) {
            for (int x = -7; x <= -3; x++) {
                level.setBlock(new BlockPos(cx + x, cy - 1, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (x == -3) {
                    // Línea de seguridad amarilla del andén
                    level.setBlock(p, Blocks.YELLOW_CONCRETE.defaultBlockState(), 2);
                } else if (x == -4) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else if ((x + z) % 4 == 0) {
                    level.setBlock(p, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                }
            }
        }

        // Bancos de espera del andén
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 5), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 4), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 4), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 5), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        // Gran Portal de la Fisura Dimensional en el andén (portal decorativo de 3 bloques de alto x 2 de ancho)
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz), SystemBlocks.DIMENSIONAL_RIFT_STAND.get().defaultBlockState().setValue(DimensionalRiftStandBlock.FACING, Direction.EAST).setValue(DimensionalRiftStandBlock.PART, DimensionalRiftStandBlock.RiftPart.LOWER_LEFT), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz), SystemBlocks.DIMENSIONAL_RIFT_STAND.get().defaultBlockState().setValue(DimensionalRiftStandBlock.FACING, Direction.EAST).setValue(DimensionalRiftStandBlock.PART, DimensionalRiftStandBlock.RiftPart.MIDDLE_LEFT), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 4, cz), SystemBlocks.DIMENSIONAL_RIFT_STAND.get().defaultBlockState().setValue(DimensionalRiftStandBlock.FACING, Direction.EAST).setValue(DimensionalRiftStandBlock.PART, DimensionalRiftStandBlock.RiftPart.UPPER_LEFT), 2);

        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 1), SystemBlocks.DIMENSIONAL_RIFT_STAND.get().defaultBlockState().setValue(DimensionalRiftStandBlock.FACING, Direction.EAST).setValue(DimensionalRiftStandBlock.PART, DimensionalRiftStandBlock.RiftPart.LOWER_RIGHT), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz + 1), SystemBlocks.DIMENSIONAL_RIFT_STAND.get().defaultBlockState().setValue(DimensionalRiftStandBlock.FACING, Direction.EAST).setValue(DimensionalRiftStandBlock.PART, DimensionalRiftStandBlock.RiftPart.MIDDLE_RIGHT), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 4, cz + 1), SystemBlocks.DIMENSIONAL_RIFT_STAND.get().defaultBlockState().setValue(DimensionalRiftStandBlock.FACING, Direction.EAST).setValue(DimensionalRiftStandBlock.PART, DimensionalRiftStandBlock.RiftPart.UPPER_RIGHT), 2);

        // 3. Foso de balasto y VÍAS FÉRREAS 3D REALISTAS (x = -2 a 7, nivel cy)
        for (int z = -10; z <= 10; z++) {
            for (int x = -2; x <= 7; x++) {
                BlockPos p = new BlockPos(cx + x, cy - 1, cz + z);
                if ((x + z) % 3 == 0) {
                    level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.GRAVEL.defaultBlockState(), 2);
                }
            }
        }

        // VÍA FÉRREA ANCHA DE 5 BLOQUES (SUBWAY_WIDE_RAIL_TRACK) continua bajo el vagón:
        for (int z = -10; z <= 10; z++) {
            level.setBlock(new BlockPos(cx - 2, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.LEFT_RAIL), 2);
            level.setBlock(new BlockPos(cx - 1, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.TIE_1), 2);
            level.setBlock(new BlockPos(cx, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.TIE_CENTER), 2);
            level.setBlock(new BlockPos(cx + 1, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.TIE_2), 2);
            level.setBlock(new BlockPos(cx + 2, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.RIGHT_RAIL), 2);

            // Vía férrea estándar para vagonetas en el túnel (x = 5)
            BlockPos pRailOpposite = new BlockPos(cx + 5, cy, cz + z);
            level.setBlock(pRailOpposite, SystemBlocks.SUBWAY_RAIL_TRACK.get().defaultBlockState().setValue(RailBlock.SHAPE, net.minecraft.world.level.block.state.properties.RailShape.NORTH_SOUTH), 2);
        }

        // Semáforo ferroviario en el túnel
        level.setBlock(new BlockPos(cx + 6, cy, cz - 8), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 8), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 2, cz - 8), Blocks.REDSTONE_TORCH.defaultBlockState(), 2);

        // 4. Vagón 3807 detallado (x = -2 a 3, z = -8 a 8, piso interior en cy + 2)
        // Despejar espacio bajo el vagón a nivel cy + 1
        for (int z = -8; z <= 8; z++) {
            for (int x = -2; x <= 3; x++) {
                level.setBlock(new BlockPos(cx + x, cy + 1, cz + z), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Bogies y ruedas pesadas de acero 3D (SUBWAY_TRAIN_WHEEL) reposando exactamente sobre las vías
        int[] bogieZ = new int[]{-6, -5, 5, 6};
        for (int bz : bogieZ) {
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + bz), SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, Direction.NORTH).setValue(SubwayTrainWheelBlock.TYPE, SubwayTrainWheelBlock.WheelType.LEFT), 2);
            level.setBlock(new BlockPos(cx, cy + 1, cz + bz), SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, Direction.NORTH).setValue(SubwayTrainWheelBlock.TYPE, SubwayTrainWheelBlock.WheelType.AXLE), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 1, cz + bz), SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, Direction.NORTH).setValue(SubwayTrainWheelBlock.TYPE, SubwayTrainWheelBlock.WheelType.RIGHT), 2);
        }

        // Piso interior del vagón
        for (int z = -8; z <= 8; z++) {
            for (int x = -2; x <= 3; x++) {
                BlockPos p = new BlockPos(cx + x, cy + 2, cz + z);
                if (x == 0 || x == 1) {
                    level.setBlock(p, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                }
            }
        }

        // Carrocería de acero del vagón, puertas correderas y ventanales
        for (int y = 3; y <= 5; y++) {
            for (int z = -8; z <= 8; z++) {
                for (int x = -2; x <= 3; x++) {
                    if (x == -2 || x == 3 || Math.abs(z) == 8) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        // Puertas dobles de acceso hacia el andén (abiertas)
                        if (x == -2 && ((z >= -4 && z <= -3) || (z >= 3 && z <= 4)) && y <= 4) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        // Puertas de paso entre vagones (en los extremos z = -8 y 8)
                        if (Math.abs(z) == 8 && (x == 0 || x == 1) && y <= 4) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        // Ventanales laterales tintados con detalles apocalípticos
                        if ((x == -2 || x == 3) && y == 4 && Math.abs(z) <= 6 &&
                                !(x == -2 && ((z >= -4 && z <= -3) || (z >= 3 && z <= 4)))) {
                            if (z == 1 || z == -1) {
                                level.setBlock(p, Blocks.COBWEB.defaultBlockState(), 2);
                            } else {
                                level.setBlock(p, Blocks.TINTED_GLASS.defaultBlockState(), 2);
                            }
                        } else if (y == 3) {
                            level.setBlock(p, Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.IRON_BLOCK.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Techo del vagón (nivel cy + 6)
        for (int z = -8; z <= 8; z++) {
            for (int x = -2; x <= 3; x++) {
                BlockPos p = new BlockPos(cx + x, cy + 6, cz + z);
                if ((x == 0 || x == 1) && (Math.abs(z) == 2 || Math.abs(z) == 6)) {
                    level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 2);
                }
            }
        }

        // Pasamanos colgantes en 3D (SUBWAY_GRAB_HANDLE) suspendidos a lo largo del pasillo central
        for (int z = -5; z <= 5; z += 2) {
            level.setBlock(new BlockPos(cx, cy + 5, cz + z), SystemBlocks.SUBWAY_GRAB_HANDLE.get().defaultBlockState().setValue(SubwayGrabHandleBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 5, cz + z), SystemBlocks.SUBWAY_GRAB_HANDLE.get().defaultBlockState().setValue(SubwayGrabHandleBlock.FACING, Direction.NORTH), 2);
        }

        // Asientos laterales del vagón
        for (int z = -6; z <= 6; z++) {
            if (!(z >= -4 && z <= -3) && !(z >= 3 && z <= 4)) {
                level.setBlock(new BlockPos(cx - 1, cy + 3, cz + z), Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            }
            level.setBlock(new BlockPos(cx + 2, cy + 3, cz + z), Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        }

        // Pantallas digitales duales panorámicas del Star Stream (SUBWAY_DIGITAL_SCREEN)
        // Suspendidas del techo sobre las puertas en ambos extremos
        level.setBlock(new BlockPos(cx, cy + 5, cz - 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.SOUTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.LEFT), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 5, cz - 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.SOUTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.RIGHT), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz + 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.NORTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.RIGHT), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 5, cz + 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.NORTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.LEFT), 2);

        // Barril de provisiones de emergencia del Escenario 1
        BlockPos barrelPos = new BlockPos(cx + 2, cy + 3, cz + 6);
        level.setBlock(barrelPos, Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(barrelPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(barrelPos);
        if (be instanceof Container barrel) {
            barrel.setItem(0, makeRelic(Items.IRON_SWORD, 1, "\u00A7f\u00A7lFe Inquebrantable (Unbroken Faith)", "ORV", "Espada empu\u00f1ada por Kim Dokja para reescribir los escenarios del Star Stream."));
            barrel.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7e\u00A7lGu\u00eda de Supervivencia: Escenario 1", "ORV", "Cap\u00edtulo 1: Tres formas de sobrevivir en un mundo destruido."));
            barrel.setItem(2, SystemItems.coin(750));
            barrel.setItem(3, new ItemStack(Items.GOLDEN_APPLE, 3));
            barrel.setItem(4, new ItemStack(Items.BREAD, 16));
            barrel.setItem(5, new ItemStack(Items.TORCH, 16));
        }

        // Spawn seguro y fijado de Kim Dokja y Yoo Joonghyuk
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 3, cz - 2), ModEntities.KIM_DOKJA.get(), NpcProfile.KIM_DOKJA);
        spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 3, cz + 2), ModEntities.YOO_JOONGHYUK.get(), NpcProfile.YOO_JOONGHYUK);
    }

    // =========================================================================
    // 3. THE GREATEST ESTATE DEVELOPER — FINCA Y TALLER DE INGENIERÍA DE FRONTERA
    //    (Lloyd Frontera & Javier Asrahan)
    // =========================================================================
    public static void buildFronteraEstate(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.STONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 13, cz - 8, cz + 8);

        // 1. Cimientos y suelo señorial medieval con alfombra noble
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 1 && z >= -7 && z <= 3) {
                    // Alfombra roja señorial que conduce al despacho de Lloyd
                    level.setBlock(p, Blocks.RED_WOOL.defaultBlockState(), 2);
                } else if ((Math.abs(x) == 2 && z >= -7 && z <= 3) || (Math.abs(x) <= 2 && z == 4)) {
                    // Borde dorado de la alfombra
                    level.setBlock(p, Blocks.YELLOW_WOOL.defaultBlockState(), 2);
                } else if (x >= 4 && z >= 4) {
                    // Zona de la chimenea de piedra labrada
                    level.setBlock(p, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
                }
            }
        }

        // 2. Muros de entramado de madera señorial (Half-timbered Tudor style)
        for (int y = 1; y <= 5; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        // Gran puerta de entrada en el frontis norte (z = -8)
                        if (z == -8 && (x == 0 || x == 1) && y <= 3) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        // Columnas estructurales de roble oscuro pelado en esquinas y pilares
                        boolean isPillar = (Math.abs(x) == 8 && Math.abs(z) == 8) ||
                                           (Math.abs(x) == 8 && z == 0) ||
                                           (Math.abs(z) == 8 && (x == -4 || x == 4));

                        if (isPillar) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        } else if (y == 1) {
                            // Zócalo de piedra labrada
                            level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                        } else if (y == 5) {
                            // Viga durmiente horizontal de soporte
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Math.abs(x) == 8 ? Direction.Axis.Z : Direction.Axis.X), 2);
                        } else if (y == 3 && (Math.abs(x) == 4 || Math.abs(z) == 4)) {
                            // Ventanas con vitrales emplomados
                            level.setBlock(p, Blocks.GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            // Yeso blanco / calcita medieval
                            level.setBlock(p, Blocks.CALCITE.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Puertas dobles de abeto señoriales
        level.setBlock(new BlockPos(cx, cy + 1, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.HINGE, net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.HINGE, net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT), 2);

        // 3. Tejado a dos aguas empinado con vigas de armadura de madera abiertas (High Vaulted Ceiling)
        for (int z = -8; z <= 8; z++) {
            for (int step = 0; step <= 6; step++) {
                int yRoof = cy + 5 + step;
                int xLeft = cx - 8 + step;
                int xRight = cx + 8 - step;

                // Pendiente exterior del tejado de abeto oscuro
                if (step < 6) {
                    level.setBlock(new BlockPos(xLeft, yRoof, cz + z), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                    level.setBlock(new BlockPos(xRight, yRoof, cz + z), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
                } else {
                    // Cumbrera central del tejado
                    level.setBlock(new BlockPos(xLeft, yRoof, cz + z), Blocks.DARK_OAK_SLAB.defaultBlockState(), 2);
                }

                // Frontones cerrados en los extremos z = -8 y z = 8
                if (z == -8 || z == 8) {
                    for (int fillX = xLeft + 1; fillX < xRight; fillX++) {
                        level.setBlock(new BlockPos(fillX, yRoof, cz + z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Armaduras y vigas transversales de soporte interior del techo (Cerchas de madera abiertas)
        for (int trussZ : new int[]{-4, 0, 4}) {
            for (int x = -7; x <= 7; x++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + trussZ), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 2);
            }
            // Tirante vertical (King post) hasta la cumbrera
            level.setBlock(new BlockPos(cx, cy + 6, cz + trussZ), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 7, cz + trussZ), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 8, cz + trussZ), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        }

        // Gran lámpara central colgante
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // 4. Gran Chimenea y Hogar de piedra con tiro de humo a la cumbrera
        for (int y = 1; y <= 11; y++) {
            level.setBlock(new BlockPos(cx + 6, cy + y, cz + 5), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 7, cy + y, cz + 5), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 6, cy + y, cz + 6), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 7, cy + y, cz + 6), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz + 5), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 1, cz + 5), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 2, cz + 5), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 12, cz + 5), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        // 5. ESTUDIO Y DESPACHO DE ARQUITECTURA E INGENIERÍA CIVIL DE LLOYD FRONTERA
        // MESA DE PLANOS DE LLOYD (ARCHITECT_DRAFTING_TABLE) con su silla de taberna
        BlockPos draftingTablePos = new BlockPos(cx - 5, cy + 1, cz + 4);
        level.setBlock(draftingTablePos, SystemBlocks.ARCHITECT_DRAFTING_TABLE.get().defaultBlockState().setValue(ArchitectDraftingTableBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 3), SystemBlocks.TAVERN_CHAIR.get().defaultBlockState().setValue(TavernChairBlock.FACING, Direction.NORTH), 2);

        // Estanterías con tratados de hidráulica y cálculo de estructuras
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 2), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 2, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 3), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 2, cz + 3), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);

        // Mesa de cartografía y útiles de agrimensura de Lloyd
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 4), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 5), Blocks.STONECUTTER.defaultBlockState(), 2);

        // Reserva de lingotes de oro y hierro de Lloyd (¡los beneficios de la finca!)
        level.setBlock(new BlockPos(cx - 6, cy + 1, cz + 6), Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 6), Blocks.RAW_IRON_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 6), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        // Cofre de planos y herramientas legendarias de Frontera
        BlockPos chestPos = new BlockPos(cx - 4, cy + 1, cz + 5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.DIAMOND_SHOVEL, 1, "\u00A76\u00A7lPala de Oro de Lloyd Frontera", "Estate Developer", "\u00A1El agua fluye cuesta abajo y el dinero fluye a mi bolsillo!"));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7e\u00A7lPlanos del Acueducto de Frontera", "Estate Developer", "C\u00E1lculos hidrost\u00E1ticos y dise\u00F1o civil certificado por Lloyd Frontera."));
            chest.setItem(2, SystemItems.coin(500));
            chest.setItem(3, new ItemStack(Items.IRON_PICKAXE));
            chest.setItem(4, new ItemStack(Items.BRICK, 32));
            chest.setItem(5, new ItemStack(Items.BREAD, 16));
        }

        // 6. RINCÓN DE ENTRENAMIENTO DE ESGRIMA Y FORJA DE JAVIER ASRAHAN
        level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 4), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 2, cz - 4), Blocks.TARGET.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 3), Blocks.GRINDSTONE.defaultBlockState().setValue(net.minecraft.world.level.block.GrindstoneBlock.FACING, Direction.WEST), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 5), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 4), Blocks.ANVIL.defaultBlockState(), 2);

        // Spawn seguro y fijado de Lloyd Frontera y Javier Asrahan
        spawnNpcIfAbsent(level, new BlockPos(cx - 4, cy + 1, cz + 3), ModEntities.LLOYD_FRONTERA.get(), NpcProfile.LLOYD_FRONTERA);
        spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz - 3), ModEntities.JAVIER_ASRAHAN.get(), NpcProfile.JAVIER_ASRAHAN);
    }

    // =========================================================================
    // 4. NANO MACHINE \u2014 GRAN PAGODA DEL CULTO DEMON\u00cdACO CELESTIAL
    //    (Cheon Yeo-woon)
    // =========================================================================
    public static void buildDemonPagoda(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.POLISHED_BLACKSTONE);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 16, cz - 8, cz + 8);

        // 1. Terraza elevada de piedra negra
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 2 && Math.abs(z) <= 2) {
                    if (x * x + z * z <= 2) {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.CRIMSON_PLANKS.defaultBlockState(), 2);
                    }
                } else {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // Faroles de piedra en los 4 extremos
        int[][] cornerLamps = new int[][]{{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] cl : cornerLamps) {
            BlockPos lp = new BlockPos(cx + cl[0], cy + 2, cz + cl[1]);
            level.setBlock(lp, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
            level.setBlock(lp.above(), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        // 2. Nivel 1 de la Pagoda: Columnas maestras de basalto pulido
        int[][] mainPillars = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {-5, 0}, {5, 0}};
        for (int[] pil : mainPillars) {
            for (int y = 2; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.POLISHED_BASALT.defaultBlockState(), 2);
            }
        }

        // Aleros del Nivel 1 (cy + 6)
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 6, cz + z);
                if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                    level.setBlock(p, Blocks.NETHER_BRICK_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }

        // 3. Nivel 2 de la Pagoda (cy + 7 a 10)
        int[][] midPillars = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] pil : midPillars) {
            for (int y = 7; y <= 10; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.POLISHED_BASALT.defaultBlockState(), 2);
            }
        }

        // Aleros del Nivel 2 (cy + 10)
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 10, cz + z);
                if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                    level.setBlock(p, Blocks.NETHER_BRICK_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }

        // 4. Cúpula y Aguja Celestial (cy + 11 a 15)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 12, cz + z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy + 13, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 14, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 15, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Campana ceremonial del templo
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.BELL.defaultBlockState(), 2);

        // 5. Patio marcial: Muñecos de entrenamiento
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 3), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 3, cz - 3), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 4), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 2), Blocks.ANVIL.defaultBlockState(), 2);

        // 6. Altar y Trono del Demonio Celestial (Cheon Yeo-woon)
        level.setBlock(new BlockPos(cx, cy + 2, cz + 4), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 4), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // Braseros de fuego de almas
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 3), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 3), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        // Cofre de reliquias del Culto Demon\u00edaco
        BlockPos chestPos = new BlockPos(cx + 4, cy + 2, cz + 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A74\u00A7lEspada del Demonio Celestial", "Nano Machine", "Forjada con la precisi\u00F3n at\u00F3mica inquebrantable de las Nano-M\u00E1quinas."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7c\u00A7lManual del Culto Demon\u00EDaco: Paso de Sombras", "Nano Machine", "T\u00E9cnicas marciales definitivas heredadas por Cheon Yeo-woon."));
            chest.setItem(2, SystemItems.coin(600));
            chest.setItem(3, new ItemStack(Items.GOLDEN_APPLE, 3));
            chest.setItem(4, new ItemStack(Items.POTION));
            chest.setItem(5, new ItemStack(Items.LAPIS_LAZULI, 12));
        }

        // Spawn seguro y fijado de Cheon Yeo-woon
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 2), ModEntities.CHEON_YEOWOON.get(), NpcProfile.CHEON_YEOWOON);
    }

    // =========================================================================
    // 5. TBATE \u2014 SANTUARIO CELESTIAL DE \u00c9TER DE DICATHEN
    //    (Arthur Leywin)
    // =========================================================================
    public static void buildAetherSanctuary(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.QUARTZ_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 15, cz - 8, cz + 8);

        // 1. Suelo conc\u00e9ntrico de cuarzo liso y geoda de amatista
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 8.2) {
                    BlockPos p = new BlockPos(cx + x, cy, cz + z);
                    if (dist <= 2.5) {
                        level.setBlock(p, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    } else if (dist <= 5.5) {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 2. 8 Columnas corintias de cuarzo con linternas marinas
        int[][] pillarCoords = new int[][]{
                {-6, 0}, {6, 0}, {0, -6}, {0, 6},
                {-4, -4}, {-4, 4}, {4, -4}, {4, 4}
        };

        for (int[] coord : pillarCoords) {
            for (int y = 1; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + coord[0], cy + y, cz + coord[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + coord[0], cy + 7, cz + coord[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + coord[0], cy + 8, cz + coord[1]), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }

        // Arbotantes
        for (int[] coord : pillarCoords) {
            int inX = Integer.compare(0, coord[0]);
            int inZ = Integer.compare(0, coord[1]);
            level.setBlock(new BlockPos(cx + coord[0] + inX, cy + 8, cz + coord[1] + inZ), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
        }

        // 3. N\u00facleo Flotante de \u00c9ter
        level.setBlock(new BlockPos(cx, cy + 4, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 4, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 4, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz - 1), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        // Estanque de man\u00e1 puro bajo el n\u00facleo
        level.setBlock(new BlockPos(cx, cy, cz), Blocks.WATER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz - 1), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 1), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        // 4. Estudio del Reino del Coraz\u00f3n y Biblioteca Djinn
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 3), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 3), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 2), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 4), Blocks.LECTERN.defaultBlockState(), 2);

        // 5. Altar del Rey de la Muerte
        BlockPos altarPos = new BlockPos(cx + 4, cy + 1, cz - 3);
        level.setBlock(altarPos, Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 2);
        level.setBlock(altarPos.above(), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz - 3), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 3), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // Cofre de reliquias sagradas de Dicathen
        BlockPos chestPos = new BlockPos(cx, cy + 1, cz + 4);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, new ItemStack(SystemItems.DEATH_KING_CLOCK.get()));
            chest.setItem(1, makeRelic(Items.DIAMOND_SWORD, 1, "\u00A79\u00A7lBalada del Amanecer (Dawn's Ballad)", "TBATE", "Espada transl\u00FAcida capaz de canalizar el \u00E9ter m\u00E1s puro del reino."));
            chest.setItem(2, makeRelic(Items.BOOK, 1, "\u00A7b\u00A7lTratado del \u00C9ter y Dicathen", "TBATE", "Escritos de Arthur Leywin sobre la naturaleza de las runas Djinn."));
            chest.setItem(3, SystemItems.coin(600));
            chest.setItem(4, new ItemStack(Items.ENDER_PEARL, 8));
            chest.setItem(5, new ItemStack(Items.DIAMOND, 3));
        }

        // Spawn seguro y fijado de Arthur Leywin
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 2), ModEntities.ARTHUR_LEYWIN.get(), NpcProfile.ARTHUR_LEYWIN);
    }

    // =========================================================================
    // 6. SECOND LIFE RANKER \u2014 OBELISCO DEL DRAG\u00d3N NEGRO / TORRE DE CA\u00cdN
    //    (Cha Yeon-woo)
    // =========================================================================
    public static void buildRankerTower(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 18, cz - 8, cz + 8);

        // 1. Basti\u00f3n escalonado de piedra negra
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState(), 2);
                } else if (Math.abs(x) == 7 || Math.abs(z) == 7) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 2 && Math.abs(z) <= 2) {
                    level.setBlock(p, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.RED_NETHER_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // 2. 4 Grandes Agujas G\u00f3ticas en las esquinas (r = 7)
        int[][] spires = new int[][]{{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] sp : spires) {
            int sx = cx + sp[0];
            int sz = cz + sp[1];
            for (int y = 2; y <= 12; y++) {
                level.setBlock(new BlockPos(sx, cy + y, sz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(sx, cy + 13, sz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

            int inX = sp[0] > 0 ? -1 : 1;
            int inZ = sp[1] > 0 ? -1 : 1;
            BlockPos bracketPos = new BlockPos(sx + inX, cy + 12, sz + inZ);
            level.setBlock(bracketPos, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState(), 2);
            level.setBlock(bracketPos.below(), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(bracketPos.below(2), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        // 3. Arcos costillares de huesos de drag\u00f3n sobre el patio
        for (int y = 2; y <= 5; y++) {
            level.setBlock(new BlockPos(cx - 4, cy + y, cz), Blocks.BONE_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 4, cy + y, cz), Blocks.BONE_BLOCK.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 6, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 6, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.BONE_BLOCK.defaultBlockState(), 2);

        // 4. Canales de magma contenidos
        int[][] braziers = new int[][]{{-4, -3}, {4, -3}};
        for (int[] bz : braziers) {
            BlockPos bp = new BlockPos(cx + bz[0], cy + 1, cz + bz[1]);
            level.setBlock(bp, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
            level.setBlock(bp.above(), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
            level.setBlock(bp.above().north(), Blocks.IRON_BARS.defaultBlockState(), 2);
            level.setBlock(bp.above().south(), Blocks.IRON_BARS.defaultBlockState(), 2);
        }

        // 5. Trono Oscuro de Ca\u00edn sobre grada elevada
        BlockPos throneBase = new BlockPos(cx, cy + 2, cz + 4);
        level.setBlock(throneBase, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(throneBase.above(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 5), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 5), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz + 5), Blocks.WITHER_SKELETON_SKULL.defaultBlockState(), 2);

        // 6. Fragua legendaria de Vigrid
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz + 4), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz + 3), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz + 2), Blocks.SMITHING_TABLE.defaultBlockState(), 2);

        // Cofre de reliquias de la Torre
        BlockPos chestPos = new BlockPos(cx + 4, cy + 2, cz + 4);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A7c\u00A7lVigrid - Espada Sagrada de Ca\u00EDn", "Second Life Ranker", "Hoja ancestral forjada para derrocar dioses y dragones de la Torre."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A76\u00A7lDiario de Cha Jeong-woo: Para Yeon-woo", "Second Life Ranker", "Para cuando escuches esto, probablemente ya est\u00E9 muerto..."));
            chest.setItem(2, SystemItems.coin(800));
            chest.setItem(3, new ItemStack(Items.NETHERITE_SCRAP, 2));
            chest.setItem(4, new ItemStack(Items.FIRE_CHARGE, 12));
            chest.setItem(5, new ItemStack(Items.GOLDEN_APPLE, 4));
        }

        // Spawn seguro y fijado de Cha Yeon-woo (Ca\u00edn)
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 2), ModEntities.CHA_YEONWOO.get(), NpcProfile.CHA_YEONWOO);
    }

    // =========================================================================
    // 7. RETURN OF THE MOUNT HUA SECT \u2014 PABELL\u00d3N DE LAS FLORES DE CIRUELO
    //    (Chung Myung)
    // =========================================================================
    public static void buildMountHuaPavilion(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.STONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 15, cz - 8, cz + 8);

        // 1. Plataforma tradicional de piedra con pasarela de cerezo
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 4 && Math.abs(z) <= 4) {
                    if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                        level.setBlock(p, Blocks.CALCITE.defaultBlockState(), 2);
                    } else if ((x + z) % 2 == 0) {
                        level.setBlock(p, Blocks.CHERRY_PLANKS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.STRIPPED_CHERRY_LOG.defaultBlockState(), 2);
                    }
                } else {
                    level.setBlock(p, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                    if ((x * 3 + z * 7) % 5 == 0) {
                        level.setBlock(p.above(), Blocks.PINK_CARPET.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 2. Columnas orientales del pabellón
        int[][] pavilionPillars = new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {-4, 0}, {4, 0}};
        for (int[] pil : pavilionPillars) {
            for (int y = 2; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.STRIPPED_CHERRY_LOG.defaultBlockState(), 2);
            }
        }

        // Barandilla de cerezo
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if ((Math.abs(x) == 4 || Math.abs(z) == 4) && !(z == -4 && Math.abs(x) <= 1)) {
                    BlockPos fp = new BlockPos(cx + x, cy + 2, cz + z);
                    level.setBlock(fp, Blocks.CHERRY_FENCE.defaultBlockState(), 2);
                }
            }
        }

        // 3. Tejado curvado de prismarina oscura
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 6, cz + z);
                if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                    level.setBlock(p, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }

        // Nivel superior del tejado
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 8, cz + z);
                if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                    level.setBlock(p, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Follaje de cerezos en flor en las esquinas
        int[][] leavesCorners = new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
        for (int[] lc : leavesCorners) {
            level.setBlock(new BlockPos(cx + lc[0], cy + 7, cz + lc[1]), Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2);
        }

        // Farolillos colgantes
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // 4. Puesto de entrenamiento: 10,000 estocadas
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 2), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz - 2), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 4), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz - 4), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 3), Blocks.GRINDSTONE.defaultBlockState(), 2);

        // 5. Bodega secreta de licor de ciruelo de Chung Myung
        level.setBlock(new BlockPos(cx + 3, cy + 2, cz + 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 3, cz + 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 3), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 2, cz + 3), Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);

        // Cofre de reliquias del Monte Hua
        BlockPos chestPos = new BlockPos(cx - 3, cy + 2, cz + 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.DIAMOND_SWORD, 1, "\u00A7a\u00A7lEspada del Drag\u00F3n de Ciruelo", "Mount Hua", "Espada tao\u00EDsta que hace brotar p\u00E9talos de espada con cada estocada certera."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7e\u00A7lManual de la Espada de las 24 Flores de Ciruelo", "Mount Hua", "Escrito por el Santo de la Espada del Monte Hua, Chung Myung."));
            chest.setItem(2, SystemItems.coin(750));
            chest.setItem(3, new ItemStack(Items.CHERRY_SAPLING, 4));
            chest.setItem(4, new ItemStack(Items.POTION));
            chest.setItem(5, new ItemStack(Items.GOLDEN_APPLE, 3));
        }

        // Spawn seguro y fijado de Chung Myung
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 1), ModEntities.CHUNG_MYUNG.get(), NpcProfile.CHUNG_MYUNG);
    }

    // =========================================================================
    // 8. SURVIVAL STORY OF A SWORD KING \u2014 BASTI\u00d3N VALTER Y LA ESPADA COLOSAL
    //    (Ryu Hanbin)
    // =========================================================================
    public static void buildValterCamp(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.MUD_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 17, cz - 8, cz + 8);

        // 1. Suelo salvaje de tierra apisonada y barro
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.COBBLESTONE_STAIRS.defaultBlockState(), 2);
                } else if ((x + z) % 3 == 0) {
                    level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                }
            }
        }

        // 2. El Monolito Rocoso Central y LA ESPADA COLOSAL DE HANBIN (8 bloques de alto)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x * x + z * z <= 3) {
                    level.setBlock(new BlockPos(cx + x, cy + 2, cz + z), Blocks.STONE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + x, cy + 3, cz + z), Blocks.ANDESITE.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(cx, cy + 4, cz), Blocks.TUFF.defaultBlockState(), 2);

        // Hoja colosal
        for (int y = 3; y <= 7; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + y, cz + 1), Blocks.IRON_BLOCK.defaultBlockState(), 2);
        }
        // Guarda
        level.setBlock(new BlockPos(cx, cy + 8, cz - 1), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz + 1), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz + 2), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.NETHERITE_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.NETHERITE_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 11, cz), Blocks.HEAVY_CORE.defaultBlockState(), 2);

        // 3. Refugio de los Bárbaros Valter al fondo (pilares y tejado simétricos)
        for (int y = 2; y <= 6; y++) {
            level.setBlock(new BlockPos(cx - 5, cy + y, cz + 5), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 5, cy + y, cz + 5), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 5, cy + y, cz + 7), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 5, cy + y, cz + 7), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + y, cz + 7), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }

        for (int x = -6; x <= 6; x++) {
            for (int z = 4; z <= 8; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 6, cz + z), Blocks.MUD_BRICK_SLAB.defaultBlockState(), 2);
            }
        }

        // 4. Gran fogata b\u00e1rbara con asador
        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 4), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz - 4), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 3), Blocks.SMOKER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 4), Blocks.STRIPPED_OAK_WOOD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 4), Blocks.STRIPPED_OAK_WOOD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 5), Blocks.BROWN_CARPET.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 5), Blocks.WHITE_CARPET.defaultBlockState(), 2);

        // 5. Fragua b\u00e1rbara
        level.setBlock(new BlockPos(cx + 4, cy + 2, cz - 3), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 2, cz - 3), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 2, cz - 4), Blocks.BLAST_FURNACE.defaultBlockState(), 2);

        // Cofre de reliquias de Valter
        BlockPos chestPos = new BlockPos(cx + 3, cy + 2, cz + 5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A7c\u00A7lMandoble Colosal de Valter", "Survival Story", "Una descomunal hoja forjada para canalizar el \u00EDmpetu f\u00EDsico puro sin l\u00EDmites."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A76\u00A7lGu\u00EDa de Supervivencia: Error de Nivel 5", "Survival Story", "Anotaciones de Ryu Hanbin tras sobrevivir 20 a\u00F1os en la Dimensi\u00F3n Rocosa."));
            chest.setItem(2, SystemItems.coin(700));
            chest.setItem(3, new ItemStack(Items.COOKED_BEEF, 32));
            chest.setItem(4, new ItemStack(Items.IRON_BLOCK, 2));
            chest.setItem(5, new ItemStack(Items.POTION));
        }

        // Spawn seguro y fijado de Ryu Hanbin
        spawnNpcIfAbsent(level, new BlockPos(cx + 2, cy + 2, cz - 1), ModEntities.RYU_HANBIN.get(), NpcProfile.RYU_HANBIN);
    }

    // =========================================================================
    // 9. TOWER OF GOD \u2014 SANTUARIO FLOTANTE DE SHINSOO Y SUSPENDIUM
    //    (Twenty-Fifth Baam)
    // =========================================================================
    public static void buildShinsooShrine(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.PRISMARINE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 16, cz - 8, cz + 8);

        // 1. Plataforma de Suspendium Levitante
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 8.2) {
                    BlockPos p = new BlockPos(cx + x, cy + 2, cz + z);
                    if (dist <= 2.8) {
                        level.setBlock(p, Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(), 2);
                    } else if (dist <= 5.5) {
                        level.setBlock(p, Blocks.PRISMARINE_BRICKS.defaultBlockState(), 2);
                    } else if (dist <= 7.2) {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Pilares inferiores de anclaje de Suspendium
        int[][] susPillars = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] sp : susPillars) {
            level.setBlock(new BlockPos(cx + sp[0], cy, cz + sp[1]), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + sp[0], cy + 1, cz + sp[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

        // 2. Canales circulares de Shinsoo puro
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d >= 3.0 && d <= 4.2) {
                    BlockPos p = new BlockPos(cx + x, cy + 2, cz + z);
                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 2);
                }
            }
        }

        // 3. Orbe Central de Shinsoo Flotante
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz - 1), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        // Balizas superiores de Portador de Luz
        int[][] lightBeacons = new int[][]{{-6, 0}, {6, 0}, {0, -6}, {0, 6}};
        for (int[] lb : lightBeacons) {
            for (int y = 3; y <= 7; y++) {
                level.setBlock(new BlockPos(cx + lb[0], cy + y, cz + lb[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + lb[0], cy + 8, cz + lb[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + lb[0], cy + 9, cz + lb[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

        // 4. Terminal de Evaluaci\u00f3n y Faro del Portador de Luz
        level.setBlock(new BlockPos(cx - 3, cy + 3, cz - 3), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 3, cz - 3), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 3, cz - 4), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);

        // 5. Pedestal Sagrado de la Aguja Marzo Negro
        level.setBlock(new BlockPos(cx + 3, cy + 3, cz - 3), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 4, cz - 3), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Cofre de reliquias de la Torre de Dios
        BlockPos chestPos = new BlockPos(cx, cy + 3, cz + 5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A7b\u00A7lAguja Divina: Marzo Negro (Black March)", "Tower of God", "Una de las legendarias 13 Armas de la Serie del Mes concedidas por el Rey Zahard."));
            chest.setItem(1, makeRelic(Items.NETHER_STAR, 1, "\u00A7c\u00A7lFragmento de la Espina Primordial", "Tower of God", "Artefacto divino de un Dios exterior capaz de doblegar las leyes del Shinsoo."));
            chest.setItem(2, SystemItems.coin(800));
            chest.setItem(3, new ItemStack(Items.PRISMARINE_CRYSTALS, 16));
            chest.setItem(4, new ItemStack(Items.DIAMOND, 3));
            chest.setItem(5, new ItemStack(Items.POTION));
        }

        // Spawn seguro y fijado de Twenty-Fifth Baam
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 3, cz + 1), ModEntities.TWENTY_FIFTH_BAAM.get(), NpcProfile.TWENTY_FIFTH_BAAM);
    }

    /**
     * Pabell\u00f3n de Ascensi\u00f3n Celestial en la dimensi\u00f3n Isekai:
     * Estructura sagrada aislada lejos del Gran \u00c1rbol donde se puede acceder al Reino Inmortal de Murim.
     */
    public static void buildAscensionPavilion(ServerLevel level, BlockPos pos) {
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();

        ensureFoundation(level, cx - 7, cx + 7, cz - 7, cz + 7, cy, Blocks.POLISHED_DEEPSLATE);
        clearRoom(level, cx - 7, cx + 7, cy + 1, cy + 10, cz - 7, cz + 7);

        // 1. Plataforma octagonal ceremonial
        int r = 6;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 8 && Math.abs(dx) <= 5 && Math.abs(dz) <= 5) {
                    BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
                    boolean border = (Math.abs(dx) == 5 || Math.abs(dz) == 5 || Math.abs(dx) + Math.abs(dz) == 8);
                    level.setBlock(p, border ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                    level.setBlock(p.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }

        // 2. 4 Pilares ornamentales con linternas marinas en las esquinas
        int[][] pillars = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] pOff : pillars) {
            for (int h = 2; h <= 5; h++) {
                level.setBlock(new BlockPos(cx + pOff[0], cy + h, cz + pOff[1]), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + pOff[0], cy + 6, cz + pOff[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + pOff[0], cy + 7, cz + pOff[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

        // 3. Torii Celestial Central con el Portal a Murim
        for (int sign : new int[]{-2, 2}) {
            int px = cx + sign;
            level.setBlock(new BlockPos(px, cy + 2, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 3, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 4, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 5, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        // Dintel y remate del Torii
        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 7, cz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);

        // Suelo central limpio del portal
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);

        // Despejar vano interior
        for (int dx = -1; dx <= 1; dx++) {
            for (int h = 2; h <= 5; h++) {
                level.setBlock(new BlockPos(cx + dx, cy + h, cz), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // 4. Atril ceremonial con inscripci\u00f3n tao\u00edsta
        BlockPos lecternPos = new BlockPos(cx, cy + 2, cz - 3);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 3), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 3), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        // 5. Techo de hojas de cerezo y azalea celestial flotante
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    level.setBlock(new BlockPos(cx + dx, cy + 8, cz + dz), Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2);
                }
            }
        }
    }

    /**
     * Gran Templo Altar del Drag\u00f3n de los Nueve Cielos (Estilo Xianxia Tian Tan):
     * Monumental plataforma ceremonial circular de 3 terrazas de m\u00e1rmol de cuarzo blanco puro,
     * escalinatas cardinales con balaustradas, pilares de cuarzo con faroles de Qi de alma,
     * braseros rituales y pedestal de loto central para el Altar Celestial.
     */
    public static void buildDragonSanctuary(ServerLevel level, BlockPos pos) {
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();

        ensureFoundation(level, cx - 16, cx + 16, cz - 16, cz + 16, cy, Blocks.SMOOTH_QUARTZ);
        clearRoom(level, cx - 16, cx + 16, cy + 1, cy + 24, cz - 16, cz + 16);
        for (int ox = -24; ox <= 24; ox++) {
            for (int oz = -24; oz <= 24; oz++) {
                if (ox * ox + oz * oz <= 576) {
                    for (int oy = 1; oy <= 30; oy++) {
                        BlockPos cp = new BlockPos(cx + ox, cy + oy, cz + oz);
                        BlockState st = level.getBlockState(cp);
                        if (st.is(BlockTags.LEAVES) || st.is(BlockTags.LOGS) || st.is(Blocks.VINE) || (!st.isAir() && !st.isSolid())) {
                            level.setBlock(cp, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 1. PRIMERA TERRAZA (Radio 11 en cy + 1)
        for (int dx = -11; dx <= 11; dx++) {
            for (int dz = -11; dz <= 11; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= 121) {
                    BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
                    if (distSq >= 100) {
                        level.setBlock(p, Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                        boolean isCardinalStair = (Math.abs(dx) <= 2 && dz * dz >= 81) || (Math.abs(dz) <= 2 && dx * dx >= 81);
                        if (!isCardinalStair && distSq >= 110) {
                            level.setBlock(p.above(), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
                        }
                    } else {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    }
                    level.setBlock(p.below(), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                }
            }
        }

        // Escalinatas cardinales primera terraza
        for (int step = -2; step <= 2; step++) {
            level.setBlock(new BlockPos(cx + step, cy + 1, cz - 12), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + step, cy + 1, cz + 12), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx - 12, cy + 1, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            level.setBlock(new BlockPos(cx + 12, cy + 1, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        }

        // 2. SEGUNDA TERRAZA (Radio 7 en cy + 2)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= 49) {
                    BlockPos p = new BlockPos(cx + dx, cy + 2, cz + dz);
                    if (distSq >= 36) {
                        level.setBlock(p, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Escalinatas cardinales segunda terraza
        for (int step = -1; step <= 1; step++) {
            level.setBlock(new BlockPos(cx + step, cy + 2, cz - 8), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + step, cy + 2, cz + 8), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx - 8, cy + 2, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            level.setBlock(new BlockPos(cx + 8, cy + 2, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        }

        // 3. TERCERA TERRAZA (Radio 4 en cy + 3)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= 16) {
                    BlockPos p = new BlockPos(cx + dx, cy + 3, cz + dz);
                    level.setBlock(p, (distSq >= 9) ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                }
            }
        }

        // Escalinatas cardinales tercera terraza
        level.setBlock(new BlockPos(cx, cy + 3, cz - 5), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 5), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 3, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 3, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        // 4. PEDESTAL DE LOTO CELESTIAL (3x3 en cy + 4)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy + 4, cz + dz);
                if (dx == 0 && dz == 0) {
                    level.setBlock(p, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
                } else if (dx == 0) {
                    level.setBlock(p, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dz > 0 ? Direction.SOUTH : Direction.NORTH), 2);
                } else if (dz == 0) {
                    level.setBlock(p, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dx > 0 ? Direction.EAST : Direction.WEST), 2);
                } else {
                    level.setBlock(p, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        // 5. ALTAR DEL DRAG\u00d3N ANCESTRAL EN EL V\u00d3RTICE CENTRAL (cy + 5)
        BlockPos altarPos = new BlockPos(cx, cy + 5, cz);
        if (com.soybe.awakeningsystem.block.SystemBlocks.CELESTIAL_DRAGON_ALTAR.isBound()) {
            level.setBlock(altarPos, com.soybe.awakeningsystem.block.SystemBlocks.CELESTIAL_DRAGON_ALTAR.get().defaultBlockState(), 2);
        } else {
            level.setBlock(altarPos, Blocks.LODESTONE.defaultBlockState(), 2);
        }

        int[][] altarCandles = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int[] cOff : altarCandles) {
            BlockPos cp = new BlockPos(cx + cOff[0], cy + 5, cz + cOff[1]);
            level.setBlock(cp, Blocks.END_ROD.defaultBlockState(), 2);
        }

        // 6. CUATRO PILARES CELESTIALES DE CUARZO DE LAS CUATRO DIRECCIONES (Si Xiang)
        int[][] pillars = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] pOff : pillars) {
            int px = cx + pOff[0];
            int pz = cz + pOff[1];
            level.setBlock(new BlockPos(px, cy + 2, pz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            for (int h = 3; h <= 7; h++) {
                level.setBlock(new BlockPos(px, cy + h, pz), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
            for (int cdx = -1; cdx <= 1; cdx++) {
                for (int cdz = -1; cdz <= 1; cdz++) {
                    level.setBlock(new BlockPos(px + cdx, cy + 8, pz + cdz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
                }
            }

            level.setBlock(new BlockPos(px, cy + 9, pz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 10, pz), Blocks.END_ROD.defaultBlockState(), 2);

            int armX = px + (pOff[0] > 0 ? -1 : 1);
            int armZ = pz + (pOff[1] > 0 ? -1 : 1);
            level.setBlock(new BlockPos(armX, cy + 7, armZ), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(armX, cy + 6, armZ),
                    Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

            int bX = px + (pOff[0] > 0 ? -1 : 1);
            int bZ = pz;
            level.setBlock(new BlockPos(bX, cy + 2, bZ), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);
        }
    }

    // =========================================================================
    // 12. CHOZA M\u00cdSTICA DE LA FORTUNA / CASA DE LA ADIVINA DE LA RULETA
    // =========================================================================
    public static void buildRoulettePavilion(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 0. Cimientos y despeje de la choza (11x11, altura 13)
        ensureFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy - 1, Blocks.COBBLESTONE);
        clearRoom(level, cx - 7, cx + 7, cy, cy + 13, cz - 7, cz + 7);

        // 1. Suelo acogedor de madera de abeto con reborde de roble oscuro
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    level.setBlock(p, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                }
            }
        }

        // Alfombra ceremonial de la adivina: p\u00farpura y dorada guiando desde la entrada hasta la ruleta
        for (int z = -5; z <= 2; z++) {
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + z), Blocks.YELLOW_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 1, cz + z), Blocks.YELLOW_CARPET.defaultBlockState(), 2);
        }

        // 2. Muros de la choza m\u00edstica (cy + 1 a cy + 4)
        for (int y = 1; y <= 4; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        // Entrada frontal de la choza (z = -5, x = 0, y <= 2)
                        if (z == -5 && x == 0 && y <= 2) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        // Pilares esquineros de roble oscuro descortezado
                        if (Math.abs(x) == 5 && Math.abs(z) == 5) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        }
                        // Ventanales laterales y frontales místicos con bloques de cristal púrpura sólidos (sin cortes ni huecos extraños)
                        else if (((Math.abs(x) == 5 && (z == -1 || z == 0)) || (z == -5 && (x == -2 || x == 2))) && (y >= 2 && y <= 3)) {
                            level.setBlock(p, Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
                        }
                        // Zócalo de piedra labrada en la base de las paredes
                        else if (y == 1) {
                            level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Porche de entrada con escaleras de abeto, base sólida de piedra y faroles de bienvenida
        level.setBlock(new BlockPos(cx, cy, cz - 6), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        // Base sólida para las vallas (evita que queden flotando en el aire)
        level.setBlock(new BlockPos(cx - 1, cy, cz - 6), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy, cz - 6), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);

        // 3. Tejado rústico a dos aguas con aleros empinados y voladizo exterior extendido (cy + 4 a cy + 9)
        for (int z = -6; z <= 6; z++) {
            for (int step = -1; step <= 4; step++) {
                int yR = cy + 5 + step;
                int xL = cx - 5 + step;
                int xR = cx + 5 - step;

                // Alero exterior y tejas de pizarra profunda (deepslate tiles)
                if (z == -6 || z == 6) {
                    level.setBlock(new BlockPos(xL, yR, cz + z), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                    level.setBlock(new BlockPos(xR, yR, cz + z), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
                } else {
                    level.setBlock(new BlockPos(xL, yR, cz + z), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                    level.setBlock(new BlockPos(xR, yR, cz + z), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
                }

                // Cierre interior del tejado (solo para step >= 0 para no alterar las paredes a cy + 4)
                if (step >= 0) {
                    for (int fillX = xL + 1; fillX < xR; fillX++) {
                        if (step == 4) {
                            level.setBlock(new BlockPos(fillX, yR, cz + z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                        } else if (z == -5 || z == 5) {
                            level.setBlock(new BlockPos(fillX, yR, cz + z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Vigas maestras interiores de madera a cy + 5
        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 5, cz - 2), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 5, cz + 2), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        }

        // Lámparas mágicas suspendidas con cadenas continuas conectadas hasta el techo (cy + 8 a cy + 5)
        level.setBlock(new BlockPos(cx, cy + 8, cz - 1), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz - 1), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz - 1), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz - 1), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // 4. Chimenea rústica de piedra con humo visible al exterior
        for (int y = 1; y <= 9; y++) {
            level.setBlock(new BlockPos(cx + 4, cy + y, cz + 3), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz + 3), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 2, cz + 3), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 10, cz + 3), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        // 5. LA RULETA FÍSICA 3D - Orientada hacia el NORTE (mirando de frente al jugador al entrar)
        BlockPos altarPos = new BlockPos(cx, cy + 1, cz + 2);
        if (com.soybe.awakeningsystem.block.SystemBlocks.ROULETTE_ALTAR.isBound()) {
            level.setBlock(altarPos, com.soybe.awakeningsystem.block.SystemBlocks.ROULETTE_ALTAR.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.RouletteAltarBlock.FACING, Direction.NORTH), 2);
        } else {
            level.setBlock(altarPos, Blocks.LODESTONE.defaultBlockState(), 2);
        }

        // Muro de decoración mística y estanterías tras la ruleta (cz = 4)
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz + 4), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 4), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz + 4), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 4), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 4), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 4), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz + 4), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 4), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz + 4), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 4), Blocks.BOOKSHELF.defaultBlockState(), 2);

        // 6. Rincón de la Adivina y Alquimia (Lateral izquierdo)
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 2), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 1), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz + 1), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz), Blocks.CRIMSON_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        // 7. Tesoro de Monedas y Cofre de Fichas de la Ruleta (Lateral derecho)
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz - 2), Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 1), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx + 3, cy + 1, cz - 1);
        // Cofre mirando hacia el centro de la sala (OESTE)
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillRouletteChest(level, chestPos);
    }

    private static void fillRouletteChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            if (SystemItems.ROULETTE_TOKEN.isBound()) {
                container.setItem(0, new ItemStack(SystemItems.ROULETTE_TOKEN.get(), 2));
            } else {
                container.setItem(0, new ItemStack(Items.NETHER_STAR, 1));
            }
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(4, new ItemStack(SystemItems.AWAKENING_COIN.get(), 64));
                container.setItem(5, new ItemStack(SystemItems.AWAKENING_COIN.get(), 36));
            }
            container.setItem(8, new ItemStack(Items.GOLDEN_APPLE, 2));
            container.setItem(13, makeRelic(Items.PAPER, 1, "\u00A76\u00A7l✦ Manual del Santuario de la Fortuna ✦",
                    "Sistema de Despertar", "Usa una Ficha de Ruleta o una Estrella del Nether directamente en la ruleta f\u00edsica para probar tu suerte"));
        }
    }

    // =========================================================================
    // 13. SANTUARIO DE LA DIOSA Y MANANTIAL DE MANÁ (DIMENSIÓN ISEKAI)
    // =========================================================================
    /**
     * Santuario de la Diosa de la Luz y Manantial Sagrado de Maná:
     * Estructura circular celestial de mármol de cuarzo blanco, rodeada por un
     * manantial de maná cristalino con propiedades curativas y purificadoras.
     * En el centro se erige el Altar de la Diosa ante una majestuosa estatua alada.
     */
    public static void buildGoddessShrine(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos y despeje de la cámara celestial
        ensureFoundation(level, cx - 10, cx + 10, cz - 10, cz + 10, cy, Blocks.SMOOTH_QUARTZ);
        clearRoom(level, cx - 10, cx + 10, cy + 1, cy + 14, cz - 10, cz + 10);

        // 2. Plataforma circular, calzadas y cuenca del Manantial de Maná
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 9.4D) continue;

                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);

                // Perímetro exterior: escalón de cuarzo
                if (dist >= 8.3D) {
                    Direction faceOut;
                    if (Math.abs(dx) >= Math.abs(dz)) {
                        faceOut = dx > 0 ? Direction.EAST : Direction.WEST;
                    } else {
                        faceOut = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                    }
                    level.setBlock(p, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, faceOut), 2);
                    level.setBlock(p.below(), Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                }
                // Calzada exterior ceremonial (distancia 6.3 a 8.3)
                else if (dist >= 6.3D) {
                    boolean isCardinal = (Math.abs(dx) <= 1 && Math.abs(dz) >= 6) || (Math.abs(dz) <= 1 && Math.abs(dx) >= 6);
                    if (isCardinal && (Math.abs(dx) == 7 || Math.abs(dz) == 7)) {
                        level.setBlock(p, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                    } else if ((dx + dz) % 2 == 0) {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                    }
                    level.setBlock(p.below(), Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                }
                // Cuenca del Manantial de Maná Sagrado (distancia 3.2 a 6.3)
                else if (dist >= 3.2D) {
                    // Fondo submarino místico
                    BlockPos bottomPos = p.below();
                    if ((Math.abs(dx) == 4 && Math.abs(dz) == 2) || (Math.abs(dx) == 2 && Math.abs(dz) == 4)) {
                        level.setBlock(bottomPos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(bottomPos, Blocks.PRISMARINE_BRICKS.defaultBlockState(), 2);
                    }

                    // Agua viva del Manantial
                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);

                    // Nenúfares mágicos flotantes
                    if ((dx * 3 + dz * 7) % 11 == 0 && (Math.abs(dx) >= 4 || Math.abs(dz) >= 4)) {
                        level.setBlock(p.above(), Blocks.LILY_PAD.defaultBlockState(), 2);
                    }
                }
                // Isla Central Sagrada (distancia < 3.2)
                else {
                    level.setBlock(p.below(), Blocks.QUARTZ_BRICKS.defaultBlockState(), 2);
                    if (dist >= 2.5D) {
                        level.setBlock(p, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 3. Cuatro Columnas Divinas en las diagonales del templo (px, pz = +-5)
        int[][] pillarCoords = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] coord : pillarCoords) {
            int px = cx + coord[0];
            int pz = cz + coord[1];

            level.setBlock(new BlockPos(px, cy + 1, pz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            for (int h = 2; h <= 6; h++) {
                level.setBlock(new BlockPos(px, cy + h, pz), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
            // Capitel con linterna marina y remate
            level.setBlock(new BlockPos(px, cy + 7, pz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 8, pz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);

            // Linternas de almas colgantes en las columnas
            BlockPos hangingChain = new BlockPos(px + (coord[0] > 0 ? -1 : 1), cy + 6, pz);
            level.setBlock(hangingChain, Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(hangingChain.below(), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        // Arcos de conexión celestial entre columnas (cy + 7)
        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 7, cz - 5), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 7, cz + 5), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }
        for (int z = -4; z <= 4; z++) {
            level.setBlock(new BlockPos(cx - 5, cy + 7, cz + z), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 5, cy + 7, cz + z), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }

        // 4. Caños y Fuentes del Manantial desde los puntos cardinales hacia la cuenca
        int[][] fountains = {{0, -6}, {0, 6}, {-6, 0}, {6, 0}};
        for (int[] f : fountains) {
            int fx = cx + f[0];
            int fz = cz + f[1];
            level.setBlock(new BlockPos(fx, cy + 2, fz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(fx, cy + 3, fz), Blocks.END_ROD.defaultBlockState(), 2);
        }

        // 5. Pedestal Central Elevado y Altar de la Diosa
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy + 1, cz + dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        // Altar de la Diosa (Mirando al Sur, hacia la entrada del santuario)
        BlockPos altarPos = new BlockPos(cx, cy + 2, cz);
        if (com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.isBound()) {
            level.setBlock(altarPos, com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.get().defaultBlockState()
                    .setValue(com.soybe.awakeningsystem.block.GoddessAltarBlock.FACING, Direction.SOUTH), 2);
        } else {
            level.setBlock(altarPos, Blocks.LODESTONE.defaultBlockState(), 2);
        }

        // Pedestales laterales con Geodas de Amatista sagradas
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 3, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 3, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        // 6. Majestuosa Estatua de la Diosa de la Luz con Alas Celestiales (cz = +2)
        int sx = cx;
        int sz = cz + 2;

        // Base y pedestal de la estatua
        level.setBlock(new BlockPos(sx, cy + 2, sz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, cy + 2, sz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 2, sz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);

        // Túnica y torso
        level.setBlock(new BlockPos(sx, cy + 3, sz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, cy + 3, sz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 3, sz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        level.setBlock(new BlockPos(sx, cy + 4, sz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);

        // Brazos sosteniendo el Orbe Sagrado de Maná
        level.setBlock(new BlockPos(sx - 1, cy + 4, sz - 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 4, sz - 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(sx, cy + 4, sz - 1), Blocks.SEA_LANTERN.defaultBlockState(), 2);

        // Cabeza y Corona de Luz (Halo Dorado)
        level.setBlock(new BlockPos(sx, cy + 5, sz), Blocks.CALCITE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx, cy + 6, sz), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx, cy + 6, sz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        // Alas de Ángel Celestial desplegadas
        level.setBlock(new BlockPos(sx - 1, cy + 4, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 4, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        level.setBlock(new BlockPos(sx - 2, cy + 5, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(sx + 2, cy + 5, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        level.setBlock(new BlockPos(sx - 3, cy + 6, sz + 1), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 3, cy + 6, sz + 1), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);

        level.setBlock(new BlockPos(sx - 3, cy + 7, sz + 1), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 3, cy + 7, sz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        // 7. Cofre Sagrado de Ofrendas y Reliquias Isekai (cz = -3)
        BlockPos chestPos = new BlockPos(cx, cy + 2, cz - 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillGoddessChest(level, chestPos);
    }

    private static void fillGoddessChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            // Solo contiene el pergamino sagrado con las instrucciones divinas del altar (sin loot gratis)
            container.setItem(0, makeRelic(Items.PAPER, 1, "\u00A7b\u00A7l✦ Pergamino de la Diosa de la Luz ✦",
                    "Isekai Sanctum", "Ofrenda gemas al Altar para recibir bendiciones o restos de monstruos para desafiar el Juicio Celestial. Las aguas del manantial purifican todo mal."));
        }
    }


    // =========================================================================
    // 14. VALLE DE LAS BESTIAS ESPIRITUALES Y MANANTIAL DE JADE (DIMENSIÓN MURIM)
    // =========================================================================
    /**
     * Valle de las Bestias Espirituales y Manantial Termal de Jade:
     * Hermoso valle montañoso oriental con aguas termales humeantes, vetas de jade espiritual,
     * bambú, cerezos en flor y un pabellón taoísta tradicional de meditación de loto.
     * En él habitan en armonía las Grullas Celestiales y los Zorros Espirituales de Nueve Colas.
     */
    public static void buildSpiritBeastValley(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos y despeje del valle
        ensureFoundation(level, cx - 13, cx + 13, cz - 13, cz + 13, cy, Blocks.STONE);
        clearRoom(level, cx - 13, cx + 13, cy + 1, cy + 15, cz - 13, cz + 13);

        // 2. Orografía natural del valle (paredes de roca, vetas de jade, musgo y bambú)
        for (int dx = -13; dx <= 13; dx++) {
            for (int dz = -13; dz <= 13; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 13.5D) continue;

                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);

                // Cresta exterior y acantilados del valle (distancia >= 10)
                if (dist >= 10.0D) {
                    int hillHeight = (int) (1 + (dist - 10.0D) * 1.2D);
                    for (int h = 0; h <= hillHeight; h++) {
                        BlockPos rockPos = new BlockPos(cx + dx, cy + 1 + h, cz + dz);
                        // Vetas de Jade Espiritual incrustadas en las rocas
                        if ((dx * 5 + dz * 3 + h) % 11 == 0) {
                            level.setBlock(rockPos, Blocks.EMERALD_ORE.defaultBlockState(), 2);
                        } else if ((dx * 3 + dz * 7 + h) % 13 == 0) {
                            level.setBlock(rockPos, Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(), 2);
                        } else if ((dx + dz + h) % 4 == 0) {
                            level.setBlock(rockPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                        } else if ((dx + dz) % 3 == 0) {
                            level.setBlock(rockPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                        } else {
                            level.setBlock(rockPos, Blocks.STONE.defaultBlockState(), 2);
                        }
                    }

                    // Vegetación sobre los riscos
                    BlockPos top = new BlockPos(cx + dx, cy + 2 + hillHeight, cz + dz);
                    if ((dx + dz) % 5 == 0) {
                        level.setBlock(top, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
                    } else if ((dx * dz) % 7 == 0) {
                        level.setBlock(top, Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2);
                    }
                }
                // Terraza intermedia y pradera del valle (distancia < 10)
                else {
                    // Suelo natural de hierba, musgo y andesita
                    if ((dx + dz) % 5 == 0) {
                        level.setBlock(p, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    } else if ((dx * 2 + dz) % 7 == 0) {
                        level.setBlock(p, Blocks.ANDESITE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    }
                    level.setBlock(p.below(), Blocks.DIRT.defaultBlockState(), 2);

                    // Bambúes esbeltos en las laderas oeste y norte
                    if (dx <= -6 && Math.abs(dz) <= 7 && (dx * 3 + dz * 2) % 4 == 0) {
                        int bHeight = 3 + Math.abs((dx + dz) % 4);
                        for (int bh = 1; bh <= bHeight; bh++) {
                            level.setBlock(p.above(bh), Blocks.BAMBOO.defaultBlockState(), 2);
                        }
                    }

                    // Flores y azaleas orientales
                    if ((dx * 7 + dz * 5) % 9 == 0 && dx > -5 && dz > 5) {
                        level.setBlock(p.above(), Blocks.PINK_PETALS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 3. Manantial Termal de Jade Espiritual (Cuenca humeante: cx - 4, cz + 2)
        int hx = cx - 4;
        int hz = cz + 2;
        int springRadius = 4;
        for (int dx = -springRadius; dx <= springRadius; dx++) {
            for (int dz = -springRadius; dz <= springRadius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 4.2D) continue;

                BlockPos waterPos = new BlockPos(hx + dx, cy + 1, hz + dz);
                BlockPos floorPos = new BlockPos(hx + dx, cy, hz + dz);
                BlockPos heatPos = new BlockPos(hx + dx, cy - 1, hz + dz);

                // Borde de rocas de río / basalto
                if (dist >= 3.2D) {
                    if ((dx + dz) % 2 == 0) {
                        level.setBlock(waterPos, Blocks.SMOOTH_BASALT.defaultBlockState(), 2);
                    } else {
                        level.setBlock(waterPos, Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState(), 2);
                    }
                    level.setBlock(floorPos, Blocks.DEEPSLATE.defaultBlockState(), 2);
                } else {
                    // Fondo del manantial con Vetas de Jade y calor geotérmico
                    level.setBlock(heatPos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2); // Emite vapor de agua

                    if (dist <= 1.5D) {
                        level.setBlock(floorPos, Blocks.EMERALD_BLOCK.defaultBlockState(), 2); // Corazón de Jade
                    } else if ((dx + dz) % 3 == 0) {
                        level.setBlock(floorPos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(floorPos, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                    }

                    // Agua termal tibia
                    level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        // Geoda de Amatista y farolillo místico al borde del manantial
        level.setBlock(new BlockPos(hx - 3, cy + 2, hz - 2), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(hx - 3, cy + 3, hz - 2), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(hx + 3, cy + 2, hz + 2), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(hx + 3, cy + 3, hz + 2), Blocks.LANTERN.defaultBlockState(), 2);

        // 4. Pabellón Taoísta de Meditación del Loto (cx + 4, cz - 3)
        int px = cx + 4;
        int pz = cz - 3;

        // Plataforma de madera del pabellón (7x7)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos floor = new BlockPos(px + dx, cy + 1, pz + dz);
                boolean isBorder = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                level.setBlock(floor, isBorder ? Blocks.STRIPPED_CHERRY_WOOD.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                level.setBlock(floor.below(), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }

        // Escalinata de acceso al pabellón (centrada simétricamente mirando al manantial)
        level.setBlock(new BlockPos(px - 4, cy + 1, pz - 1), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(px - 4, cy + 1, pz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(px - 4, cy + 1, pz + 1), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        // 4 Columnas del pabellón y vigas de soporte estructural (100% conectadas)
        int[][] columns = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] col : columns) {
            int colX = px + col[0];
            int colZ = pz + col[1];
            for (int h = 2; h <= 5; h++) {
                level.setBlock(new BlockPos(colX, cy + h, colZ), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            }
        }

        // Vigas horizontales de soporte conectando las 4 columnas (cy + 5)
        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(px + dx, cy + 5, pz - 3), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px + dx, cy + 5, pz + 3), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        }
        for (int dz = -3; dz <= 3; dz++) {
            level.setBlock(new BlockPos(px - 3, cy + 5, pz + dz), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px + 3, cy + 5, pz + dz), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        }

        // Tejado continuo de pagoda tradicional oriental (100% conectado sin huecos ni bloques al aire)
        // Capa 1: Aleros exteriores y cielo raso interior macizo (cy + 6)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos roofPos = new BlockPos(px + dx, cy + 6, pz + dz);
                // Borde exterior: escaleras con aleros curvados
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    Direction facingDir;
                    if (Math.abs(dx) >= Math.abs(dz)) {
                        facingDir = dx > 0 ? Direction.EAST : Direction.WEST;
                    } else {
                        facingDir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                    }
                    level.setBlock(roofPos, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facingDir), 2);
                }
                // Anillo intermedio estructural (dx = 3 o dz = 3)
                else if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    level.setBlock(roofPos, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
                // Cielo raso interior de madera noble
                else {
                    level.setBlock(roofPos, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                }
            }
        }

        // Capa 2: Inclinación media de la pagoda (cy + 7)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos roofMid = new BlockPos(px + dx, cy + 7, pz + dz);
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    Direction facingDir;
                    if (Math.abs(dx) >= Math.abs(dz)) {
                        facingDir = dx > 0 ? Direction.EAST : Direction.WEST;
                    } else {
                        facingDir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                    }
                    level.setBlock(roofMid, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facingDir), 2);
                } else if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    level.setBlock(roofMid, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                } else {
                    level.setBlock(roofMid, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }

        // Capa 3: Cúpula central y remate de pagoda (cy + 8 a cy + 9)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(px + dx, cy + 8, pz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(px, cy + 8, pz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(px, cy + 9, pz), Blocks.END_ROD.defaultBlockState(), 2);

        // Farol central suspendido del cielo raso continuo de madera
        level.setBlock(new BlockPos(px, cy + 5, pz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(px, cy + 4, pz), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Faroles colgantes en los 4 aleros exteriores
        int[][] eaveLanterns = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] eLantern : eaveLanterns) {
            BlockPos ePos = new BlockPos(px + eLantern[0], cy + 5, pz + eLantern[1]);
            level.setBlock(ePos, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        // Interior del pabellón:
        // Esterilla de meditación central
        level.setBlock(new BlockPos(px, cy + 2, pz), Blocks.RED_CARPET.defaultBlockState(), 2);

        // Pebetero de incienso relajante
        level.setBlock(new BlockPos(px, cy + 2, pz - 2), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(px, cy + 3, pz - 2), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        // Atril Ceremonial y Cofre con ÚNICAMENTE el pergamino explicativo (sin loot gratis)
        BlockPos lecternPos = new BlockPos(px - 2, cy + 2, pz);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST), 2);

        BlockPos chestPos = new BlockPos(px - 2, cy + 2, pz - 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillSpiritValleyChest(level, chestPos);

        // 5. Fauna Sagrada: Bestias Espirituales Inmortales
        spawnSpiritBeastIfAbsent(level, new BlockPos(hx - 2, cy + 1, hz - 4), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Bestia Inmortal] Grulla Espiritual");
        spawnSpiritBeastIfAbsent(level, new BlockPos(hx + 3, cy + 1, hz + 3), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Bestia Inmortal] Grulla Espiritual");
        spawnSpiritBeastIfAbsent(level, new BlockPos(cx + 6, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Bestia Espiritual] Zorro de Nueve Colas");
        spawnSpiritBeastIfAbsent(level, new BlockPos(cx - 7, cy + 1, cz + 5), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Bestia Espiritual] Zorro de Nueve Colas");
    }

    private static void fillSpiritValleyChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            // Contiene ÚNICAMENTE el pergamino instructivo, sin loot desbalanceado
            container.setItem(0, makeRelic(Items.PAPER, 1, "\u00A7a\u00A7l✦ Manual del Valle de las Bestias Espirituales ✦",
                    "Reino Murim", "Las aguas termales y la esterilla del pabell\u00f3n concentran el Qi supremo del cielo y la tierra. Medita aqu\u00ed para multiplicar tu cultivo de Dantian x3."));
        }
    }

    public static void spawnSpiritBeastIfAbsent(ServerLevel level, BlockPos pos, EntityType<? extends net.minecraft.world.entity.Mob> type, String customName) {
        level.getChunk(pos);
        AABB box = new AABB(pos).inflate(16.0D);
        List<? extends net.minecraft.world.entity.Mob> nearby = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, box,
                e -> e.isAlive() && !e.isRemoved() && e.getType() == type);
        if (nearby.size() < 2) {
            net.minecraft.world.entity.Mob beast = type.create(level);
            if (beast != null) {
                beast.moveTo(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                beast.setCustomName(Component.literal(customName));
                beast.setCustomNameVisible(true);
                beast.setPersistenceRequired();
                level.addFreshEntity(beast);
            }
        }
    }


    // =========================================================================
    // 15. LA PAGODA DE LAS NUEVE TRIBULACIONES (DIMENSIÓN MURIM)
    // =========================================================================
    /**
     * Monumental Pagoda de Nueve Pisos del Reino Inmortal de Murim:
     * Majestuosa torre oriental de 9 niveles (~48 bloques de altura) consagrada a las
     * nueve tribulaciones celestiales de templanza del Dantian y ascensión del alma marcial.
     * Cuenta con aleros curvados de teja prismarina, faroles de alma, ventanas orientales
     * y un faro de luz celestial en la cumbre.
     */
    public static void buildNineTribulationsPagoda(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos masivos y despeje de la torre (15x15 x 52 de altura)
        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy, Blocks.POLISHED_BLACKSTONE);
        clearRoom(level, cx - 8, cx + 8, cy + 1, cy + 50, cz - 8, cz + 8);

        // 2. Plataforma octagonal ceremonial de la base (cy + 1)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 7.5D) continue;

                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
                // Escalinatas radiales exteriores
                if (dist >= 6.5D) {
                    Direction faceOut;
                    if (Math.abs(dx) >= Math.abs(dz)) {
                        faceOut = dx > 0 ? Direction.EAST : Direction.WEST;
                    } else {
                        faceOut = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                    }
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, faceOut), 2);
                    level.setBlock(p.below(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else {
                    boolean isRune = (Math.abs(dx) == Math.abs(dz)) || (dx == 0) || (dz == 0);
                    level.setBlock(p, isRune ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    level.setBlock(p.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }

        // 3. Estructura de los 9 Pisos Verticales de la Pagoda
        int floorHeight = 4;
        int totalFloors = 9;

        for (int floorIndex = 0; floorIndex < totalFloors; floorIndex++) {
            int baseY = cy + 2 + (floorIndex * floorHeight);
            int topY = baseY + floorHeight - 1;

            // Piso de madera maciza para los niveles superiores (2 al 9)
            if (floorIndex > 0) {
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        level.setBlock(new BlockPos(cx + dx, baseY - 1, cz + dz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                        // Alfombra central ceremonial roja en cada piso
                        if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                            level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), Blocks.RED_CARPET.defaultBlockState(), 2);
                        }
                    }
                }
            }

            // 4 Columnas maestras de madera carmesí imperial
            int[][] cornerPosts = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
            for (int[] post : cornerPosts) {
                for (int y = baseY; y <= topY; y++) {
                    level.setBlock(new BlockPos(cx + post[0], y, cz + post[1]), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                }
            }

            // Vigas perimetrales y ventanas de papel oriental en las paredes
            for (int dx = -4; dx <= 4; dx++) {
                // Paredes Norte y Sur
                for (int zSign : new int[]{-5, 5}) {
                    // Puerta de entrada solo en Piso 1 Sur (cz = -5, dx = 0, y = baseY..baseY+1)
                    if (floorIndex == 0 && zSign == -5) {
                        if (dx == 0) {
                            // Puerta exterior carmesí de entrada a la Pagoda
                            level.setBlock(new BlockPos(cx, baseY, cz + zSign), Blocks.CRIMSON_DOOR.defaultBlockState()
                                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                                    .setValue(DoorBlock.FACING, Direction.NORTH), 2);
                            level.setBlock(new BlockPos(cx, baseY + 1, cz + zSign), Blocks.CRIMSON_DOOR.defaultBlockState()
                                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                                    .setValue(DoorBlock.FACING, Direction.NORTH), 2);
                            level.setBlock(new BlockPos(cx, baseY + 2, cz + zSign), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState()
                                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
                            level.setBlock(new BlockPos(cx, topY, cz + zSign), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                            continue;
                        } else if (Math.abs(dx) == 1) {
                            level.setBlock(new BlockPos(cx + dx, baseY, cz + zSign), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                            level.setBlock(new BlockPos(cx + dx, baseY + 1, cz + zSign), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                            level.setBlock(new BlockPos(cx + dx, baseY + 2, cz + zSign), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                            level.setBlock(new BlockPos(cx + dx, topY, cz + zSign), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                            continue;
                        }
                    }
                    level.setBlock(new BlockPos(cx + dx, baseY, cz + zSign), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + dx, baseY + 1, cz + zSign), Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + dx, baseY + 2, cz + zSign), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + dx, topY, cz + zSign), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                }
            }
            for (int dz = -4; dz <= 4; dz++) {
                // Paredes Este y Oeste
                for (int xSign : new int[]{-5, 5}) {
                    level.setBlock(new BlockPos(cx + xSign, baseY, cz + dz), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + xSign, baseY + 1, cz + dz), Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + xSign, baseY + 2, cz + dz), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + xSign, topY, cz + dz), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                }
            }

            // Escalinata de ascenso perimetral continua (Pisos 0 a 7 conectando hacia el siguiente nivel)
            if (floorIndex < totalFloors - 1) {
                int wall = floorIndex % 4;
                switch (wall) {
                    case 0 -> {
                        // Pared Este (dx = 4): Escalinata que sube hacia el Norte (dz: 2 -> -1)
                        for (int step = 0; step < 4; step++) {
                            int stepDz = 2 - step;
                            int stepY = baseY + step;
                            BlockPos sPos = new BlockPos(cx + 4, stepY, cz + stepDz);
                            level.setBlock(sPos, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                            // Relleno sólido inferior
                            for (int fillY = baseY; fillY < stepY; fillY++) {
                                level.setBlock(new BlockPos(cx + 4, fillY, cz + stepDz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            }
                            // Despejar altura para la cabeza
                            for (int head = 1; head <= 3; head++) {
                                level.setBlock(sPos.above(head), Blocks.AIR.defaultBlockState(), 2);
                            }
                            // Barandilla de seguridad interior
                            level.setBlock(new BlockPos(cx + 3, stepY + 1, cz + stepDz), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                        }
                    }
                    case 1 -> {
                        // Pared Norte (dz = -4): Escalinata que sube hacia el Oeste (dx: 2 -> -1)
                        for (int step = 0; step < 4; step++) {
                            int stepDx = 2 - step;
                            int stepY = baseY + step;
                            BlockPos sPos = new BlockPos(cx + stepDx, stepY, cz - 4);
                            level.setBlock(sPos, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
                            for (int fillY = baseY; fillY < stepY; fillY++) {
                                level.setBlock(new BlockPos(cx + stepDx, fillY, cz - 4), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            }
                            for (int head = 1; head <= 3; head++) {
                                level.setBlock(sPos.above(head), Blocks.AIR.defaultBlockState(), 2);
                            }
                            level.setBlock(new BlockPos(cx + stepDx, stepY + 1, cz - 3), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                        }
                    }
                    case 2 -> {
                        // Pared Oeste (dx = -4): Escalinata que sube hacia el Sur (dz: -2 -> 1)
                        for (int step = 0; step < 4; step++) {
                            int stepDz = -2 + step;
                            int stepY = baseY + step;
                            BlockPos sPos = new BlockPos(cx - 4, stepY, cz + stepDz);
                            level.setBlock(sPos, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
                            for (int fillY = baseY; fillY < stepY; fillY++) {
                                level.setBlock(new BlockPos(cx - 4, fillY, cz + stepDz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            }
                            for (int head = 1; head <= 3; head++) {
                                level.setBlock(sPos.above(head), Blocks.AIR.defaultBlockState(), 2);
                            }
                            level.setBlock(new BlockPos(cx - 3, stepY + 1, cz + stepDz), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                        }
                    }
                    case 3 -> {
                        // Pared Sur (dz = 4): Escalinata que sube hacia el Este (dx: -2 -> 1)
                        for (int step = 0; step < 4; step++) {
                            int stepDx = -2 + step;
                            int stepY = baseY + step;
                            BlockPos sPos = new BlockPos(cx + stepDx, stepY, cz + 4);
                            level.setBlock(sPos, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                            for (int fillY = baseY; fillY < stepY; fillY++) {
                                level.setBlock(new BlockPos(cx + stepDx, fillY, cz + 4), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            }
                            for (int head = 1; head <= 3; head++) {
                                level.setBlock(sPos.above(head), Blocks.AIR.defaultBlockState(), 2);
                            }
                            level.setBlock(new BlockPos(cx + stepDx, stepY + 1, cz + 3), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            // Aleros curvados de pagoda que sobresalen en cada piso (en topY)
            int eaveY = topY;
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    if (Math.abs(dx) == 6 || Math.abs(dz) == 6) {
                        Direction fDir;
                        if (Math.abs(dx) >= Math.abs(dz)) {
                            fDir = dx > 0 ? Direction.EAST : Direction.WEST;
                        } else {
                            fDir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                        }
                        level.setBlock(new BlockPos(cx + dx, eaveY, cz + dz), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, fDir), 2);
                    }
                }
            }

            // Faroles de alma colgando de las 4 esquinas de los aleros de cada piso
            int[][] eaveCorners = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
            for (int[] ec : eaveCorners) {
                BlockPos lPos = new BlockPos(cx + ec[0], eaveY - 1, cz + ec[1]);
                level.setBlock(lPos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
            }

            // Faroles interiores iluminando cada piso
            if (floorIndex > 0) {
                level.setBlock(new BlockPos(cx - 3, topY, cz - 3), Blocks.CHAIN.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx - 3, topY - 1, cz - 3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                level.setBlock(new BlockPos(cx + 3, topY, cz + 3), Blocks.CHAIN.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 3, topY - 1, cz + 3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
            }
        }

        // 4. Piso 1 Interior: Santuario de las Nueve Tribulaciones
        // Plataforma ceremonial del Sanctum Gate
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy + 1, cz + dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        // Arco Místico y Puerta de Entrada a las Nueve Tribulaciones ("Bigger on the inside")
        BlockPos gatePos = new BlockPos(cx, cy + 2, cz);
        level.setBlock(gatePos.below(), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(gatePos.west(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.west().above(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.east(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.east().above(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.above(2), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(gatePos.above(3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Puerta del portal a la dimensión de las tribulaciones
        level.setBlock(gatePos, Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(gatePos.above(), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.FACING, Direction.NORTH), 2);

        // Altar de Resurrección detrás del pórtico
        BlockPos altarPos = new BlockPos(cx, cy + 2, cz + 2);
        level.setBlock(altarPos, Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 2);
        level.setBlock(altarPos.north(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(altarPos.south(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(altarPos.east(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(altarPos.west(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // Atril Ceremonial con el Pergamino explicativo y cofre sin loot gratis
        BlockPos lecternPos = new BlockPos(cx - 3, cy + 2, cz - 2);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST), 2);

        BlockPos chestPos = new BlockPos(cx - 3, cy + 2, cz - 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillNineTribulationsChest(level, chestPos);

        // 5. Cúspide Celestial de la Pagoda (Piso 9 y Tejado Dorado)
        int roofBaseY = cy + 2 + (totalFloors * floorHeight);

        // Bóveda dorada y faro de luz celestial
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlock(new BlockPos(cx + dx, roofBaseY, cz + dz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, roofBaseY - 1, cz + dz), Blocks.IRON_BLOCK.defaultBlockState(), 2);
            }
        }
        // Faro celestial (Beacon)
        BlockPos beaconPos = new BlockPos(cx, roofBaseY, cz);
        level.setBlock(beaconPos, Blocks.BEACON.defaultBlockState(), 2);
        level.setBlock(beaconPos.above(), Blocks.WHITE_STAINED_GLASS.defaultBlockState(), 2);

        // Tejado piramidal de la cúspide
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos rPos = new BlockPos(cx + dx, roofBaseY + 1, cz + dz);
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    Direction fDir;
                    if (Math.abs(dx) >= Math.abs(dz)) {
                        fDir = dx > 0 ? Direction.EAST : Direction.WEST;
                    } else {
                        fDir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                    }
                    level.setBlock(rPos, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, fDir), 2);
                }
            }
        }

        // Remate de aguja dorada con vara del End
        level.setBlock(new BlockPos(cx, roofBaseY + 2, cz), Blocks.WHITE_STAINED_GLASS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, roofBaseY + 3, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, roofBaseY + 4, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, roofBaseY + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);
    }

    private static void fillNineTribulationsChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(0, makeRelic(Items.PAPER, 1, "§6§l✦ Pergamino de las Nueve Tribulaciones ✦",
                    "Gran Pagoda de Murim", "Asciende los 9 niveles y supera las tribulaciones elementales para purificar tu Dantian. Solo los verdaderos inmortales alcanzan la cima celestial."));
            if (com.soybe.awakeningsystem.SystemItems.SOUL_TRANSFORMATION_PILL.isBound()) {
                container.setItem(1, new ItemStack(com.soybe.awakeningsystem.SystemItems.SOUL_TRANSFORMATION_PILL.get(), 4));
            }
            if (com.soybe.awakeningsystem.SystemItems.SNOW_LOTUS.isBound()) {
                container.setItem(2, new ItemStack(com.soybe.awakeningsystem.SystemItems.SNOW_LOTUS.get(), 2));
            }
            if (com.soybe.awakeningsystem.SystemItems.WU_XING_MANUAL.isBound()) {
                container.setItem(3, new ItemStack(com.soybe.awakeningsystem.SystemItems.WU_XING_MANUAL.get(), 1));
            }
        }
    }


    // =========================================================================
    // 16. GREMIO DE AVENTUREROS Y TABLÓN DE CACERÍAS (DIMENSIÓN ISEKAI)
    // =========================================================================
    /**
     * Gremio de Aventureros Isekai (Reconstrucción Arquitectónica Definitiva):
     * Majestuosa taberna/sede gremial de 1 SOLA PLANTA (21x17 bloques).
     * - Paleta Medieval: Cimientos de pizarra profunda pulida, muros cálidos de ladrillos de adobe (Mud Bricks),
     *   pilares esquineros de abeto descortezado y tejado a dos aguas de roble oscuro con aleros caídos.
     * - Sin fugas exteriores: Las paredes perimetrales son continuas y uniformes sin parches raros de librerías por fuera.
     * - Iluminación estructural fija: Cero faroles flotantes. Todos anclados a cadenas que bajan de vigas de techo continuas.
     * - Ventanas empotradas: Paneles de cristal limpios de 1x2 firmemente enmarcados entre ladrillos de adobe.
     * - Gran Tablón de Cacerías: Un único bloque de gran detalle 3D colgado en la pared oeste, sin estantes que bloqueen el paso.
     * - Recepción: Elena estacionaria detrás del mostrador con carné gremial y ascenso de rango.
     */
    public static void buildAdventurersGuild(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos y despeje de terreno (25x21x18 de altura libre)
        ensureFoundation(level, cx - 11, cx + 11, cz - 9, cz + 9, cy - 1, Blocks.DEEPSLATE_BRICKS);
        clearRoom(level, cx - 13, cx + 13, cy, cy + 18, cz - 11, cz + 11);

        // 2. Pavimento del Salón (y = cy)
        for (int x = -10; x <= 10; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 10 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                }
            }
        }

        // 3. Muros Perimetrales Exteriores (y = cy + 1 a cy + 5)
        // x = ±10, z = ±8
        for (int y = 1; y <= 5; y++) {
            int py = cy + y;
            for (int x = -10; x <= 10; x++) {
                for (int z = -8; z <= 8; z++) {
                    boolean isEdge = (Math.abs(x) == 10 || Math.abs(z) == 8);
                    if (!isEdge) continue;

                    BlockPos p = new BlockPos(cx + x, py, cz + z);

                    // Pilares esquineros de soporte estructural
                    boolean isCorner = (Math.abs(x) == 10 && Math.abs(z) == 8);
                    if (isCorner) {
                        level.setBlock(p, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
                        continue;
                    }

                    // Zócalo de piedra basal (y = 1)
                    if (y == 1) {
                        level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                        continue;
                    }

                    // Viga superior perimetral continua (y = 5)
                    if (y == 5) {
                        level.setBlock(p, Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), 2);
                        continue;
                    }

                    // Ventanas empotradas limpias de 1x2 (y = 2 y 3)
                    boolean isWindow = (y == 2 || y == 3) && (
                            (z == -8 && (Math.abs(x) == 5)) ||
                            (z == 8  && (Math.abs(x) == 5)) ||
                            (x == 10 && (z == -4 || z == 4)) ||
                            (x == -10 && (z == -4 || z == 4))
                    );

                    if (isWindow) {
                        BlockState paneState = Blocks.GLASS_PANE.defaultBlockState();
                        if (x == -10 || x == 10) {
                            paneState = paneState.setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH, true)
                                                 .setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH, true);
                        } else {
                            paneState = paneState.setValue(net.minecraft.world.level.block.IronBarsBlock.EAST, true)
                                                 .setValue(net.minecraft.world.level.block.IronBarsBlock.WEST, true);
                        }
                        level.setBlock(p, paneState, 3);
                    } else {
                        // Muros cálidos de ladrillos de adobe medieval (Mud Bricks)
                        level.setBlock(p, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 4. Entrada Principal Centrada (z = -8, x = 0)
        level.setBlock(new BlockPos(cx, cy + 1, cz - 8), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 8), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH).setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 2);

        // Marco de madera noble en la puerta
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz - 8), Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), 2);

        // Porche Exterior de Entrada (sin faroles en el porche, postes de valla limpios)
        for (int px = -2; px <= 2; px++) {
            level.setBlock(new BlockPos(cx + px, cy, cz - 9), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + px, cy, cz - 10), Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            // Tejadillo del porche
            level.setBlock(new BlockPos(cx + px, cy + 3, cz - 9), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }
        // Columnas del porche (limpias, sin faroles encima)
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz - 10), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz - 10), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 10), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 10), Blocks.AIR.defaultBlockState(), 2);

        // 6. Mostrador de Recepción y Elena (z = +4 a +6)
        for (int x = -4; x <= 4; x++) {
            if (x == 3) continue; // Entrada libre
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 4), Blocks.DARK_OAK_WOOD.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 4), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // Fondo de recepción (z = +7): estanterías de archivos y blasón oficial del gremio
        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 7), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 2, cz + 7), (x == 0) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz + 6), Blocks.AIR.defaultBlockState(), 2);

        // Elena estacionaria detrás del mostrador (x = 0, z = +5)
        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 5), ModEntities.SERIES_NPC.get(), NpcProfile.ELENA_RECEPTIONIST);

        // 7. GRAN TABLÓN DE ANUNCIOS Y CACERÍAS (Pared Oeste: x = cx - 9, z = cz)
        // La pared exterior (x = cx - 10) es de ladrillos de adobe sólidos sin agujeros.
        // El Gran Tablón 3D de un solo bloque (3 de ancho x 2 de alto) se monta centrado a y = cy + 2.
        // Limpiamos los 6 espacios de la silueta (z: cz-1..cz+1, y: cy+2..cy+3) para que no haya bloques flotantes ni colisiones.
        for (int zOff = -1; zOff <= 1; zOff++) {
            for (int yOff = 0; yOff <= 1; yOff++) {
                BlockPos p = new BlockPos(cx - 9, cy + 2 + yOff, cz + zOff);
                if (zOff == 0 && yOff == 0) {
                    if (com.soybe.awakeningsystem.block.SystemBlocks.BOUNTY_BOARD.isBound()) {
                        level.setBlock(p, com.soybe.awakeningsystem.block.SystemBlocks.BOUNTY_BOARD.get().defaultBlockState()
                                .setValue(com.soybe.awakeningsystem.block.BountyBoardBlock.FACING, Direction.EAST), 2);
                    }
                } else {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        // Iluminación dedicada del Tablón (farol colgante con cadena anclada a la viga de abeto frente al tablón)
        level.setBlock(new BlockPos(cx - 8, cy + 4, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 8, cy + 3, cz), Blocks.LANTERN.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 2);

        // 8. Mesas y Sillas de Taberna Funcionales (4 grupos simétricos en el salón)
        int[][] tablePositions = {{-4, -3}, {4, -3}, {-4, 1}, {4, 1}};
        for (int[] t : tablePositions) {
            int tx = cx + t[0];
            int tz = cz + t[1];

            if (com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_TABLE.isBound()) {
                level.setBlock(new BlockPos(tx, cy + 1, tz), com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_TABLE.get().defaultBlockState(), 2);
            } else {
                level.setBlock(new BlockPos(tx, cy + 1, tz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }

            if (com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_CHAIR.isBound()) {
                level.setBlock(new BlockPos(tx, cy + 1, tz - 1), com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_CHAIR.get().defaultBlockState()
                        .setValue(com.soybe.awakeningsystem.block.TavernChairBlock.FACING, Direction.SOUTH), 2);
                level.setBlock(new BlockPos(tx, cy + 1, tz + 1), com.soybe.awakeningsystem.block.SystemBlocks.TAVERN_CHAIR.get().defaultBlockState()
                        .setValue(com.soybe.awakeningsystem.block.TavernChairBlock.FACING, Direction.NORTH), 2);
            }
        }

        // 9. VIGAS TRANSVERSALES CONTINUAS (y = cy + 5)
        // Cruce completo de este a oeste de pared a pared (x = -9 a +9), sin piezas flotantes
        int[] beamZ = {-4, 0, 4};
        for (int bz : beamZ) {
            for (int x = -9; x <= 9; x++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + bz), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            }
        }

        // 10. ILUMINACIÓN INTERIOR FIJA ANCLADA A CADENAS (Cero faroles flotantes)
        // Cuelgan directamente de las vigas transversales continuas
        int[][] lanternPoints = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, 0}};
        for (int[] lp : lanternPoints) {
            int lx = cx + lp[0];
            int lz = cz + lp[1];
            // Cadena bajo la viga sólida
            level.setBlock(new BlockPos(lx, cy + 4, lz), Blocks.CHAIN.defaultBlockState(), 2);
            // Farol colgante anclado a la cadena
            level.setBlock(new BlockPos(lx, cy + 3, lz), Blocks.LANTERN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 2);
        }

        // 11. GRAN TEJADO A DOS AGUAS CON ALEROS SOBRESALIENTES Y CAÍDA INFERIOR (y = cy + 5 a cy + 11)
        for (int layer = 0; layer <= 6; layer++) {
            int ry = cy + 5 + layer;
            int zNorth = -9 + layer;
            int zSouth = 9 - layer;

            if (zNorth > zSouth) break;

            for (int x = -11; x <= 11; x++) {
                // Fila Norte
                BlockPos pNorth = new BlockPos(cx + x, ry, cz + zNorth);
                level.setBlock(pNorth, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);

                // Fila Sur
                BlockPos pSouth = new BlockPos(cx + x, ry, cz + zSouth);
                level.setBlock(pSouth, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);

                // Alero inferior caído con escaleras invertidas (en la primera capa ry = cy + 5)
                if (layer == 0) {
                    BlockPos pNorthUnder = new BlockPos(cx + x, ry - 1, cz + zNorth);
                    level.setBlock(pNorthUnder, Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH)
                            .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP), 2);

                    BlockPos pSouthUnder = new BlockPos(cx + x, ry - 1, cz + zSouth);
                    level.setBlock(pSouthUnder, Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH)
                            .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP), 2);
                }

                // Cúspide y cierre de frontones este y oeste con ladrillos de adobe
                for (int z = zNorth + 1; z < zSouth; z++) {
                    BlockPos pMid = new BlockPos(cx + x, ry, cz + z);
                    if (layer == 6 || zNorth + 1 == zSouth - 1) {
                        // Bloques de madera maciza en la cima en lugar de losas
                        level.setBlock(pMid, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                    } else if (Math.abs(x) == 10) {
                        level.setBlock(pMid, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 12. CHIMENEA CONTINUA DE LADRILLOS DE PIEDRA (Pared Este: x = +9 a +10, z = -1 a +1)
        // Columna maciza 100% continua que sube desde el suelo atravesando el tejado hasta la cima exterior
        for (int y = 1; y <= 13; y++) {
            int py = cy + y;
            for (int z = -1; z <= 1; z++) {
                level.setBlock(new BlockPos(cx + 9, py, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 10, py, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }
        // Hogar interior de taberna (y = cy + 1 a cy + 3)
        level.setBlock(new BlockPos(cx + 9, cy + 1, cz), Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 1, cz), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 2, cz), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 3, cz), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 3, cz - 1), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 3, cz + 1), Blocks.LANTERN.defaultBlockState(), 2);

        // Remate de chimenea exterior sobre el tejado con salida continua de humo
        level.setBlock(new BlockPos(cx + 9, cy + 14, cz), Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 10, cy + 14, cz), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 9, cy + 14, cz - 1), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 9, cy + 14, cz + 1), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 9, cy + 15, cz), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);
    }

    // =========================================================================
    // 17. MURIM — FORTALEZA DE LA SECTA DE SANGRE / CULTO DEMONÍACO
    //     (Patriarca de la Sangre, Discípulos y Estandartes de Qi Demoníaco)
    // =========================================================================
    public static void buildBloodSectFortress(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos y despeje de terreno (31x31 bloques)
        ensureFoundation(level, cx - 15, cx + 15, cz - 15, cz + 15, cy - 1, Blocks.POLISHED_BLACKSTONE);
        clearRoom(level, cx - 15, cx + 15, cy, cy + 16, cz - 15, cz + 15);

        // 2. Pavimento del patio y plataforma ceremonial
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 15 || Math.abs(z) == 15) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else if (z >= 6 && z <= 14 && Math.abs(x) <= 10) {
                    // Base elevada del Pabellón del Patriarca
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    level.setBlock(p.above(2), ((x + z) % 2 == 0) ? Blocks.CRIMSON_PLANKS.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 1 && z >= -14 && z <= 5) {
                    // Avenida procesional central alfombrada en rojo sangre
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.RED_CARPET.defaultBlockState(), 2);
                } else if ((x * x + (z + 4) * (z + 4)) <= 9) {
                    // Círculo marcial de entrenamiento
                    level.setBlock(p, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // 3. Murallas exteriores de fortaleza (altura 4 bloques)
        for (int i = -15; i <= 15; i++) {
            // Pared Norte (z = 15)
            buildFortressWallSegment(level, cx + i, cy, cz + 15);
            // Pared Oeste (x = -15)
            buildFortressWallSegment(level, cx - 15, cy, cz + i);
            // Pared Este (x = 15)
            buildFortressWallSegment(level, cx + 15, cy, cz + i);

            // Pared Sur (z = -15, dejando entrada libre en x = -2..2)
            if (Math.abs(i) > 2) {
                buildFortressWallSegment(level, cx + i, cy, cz - 15);
            }
        }

        // Almenas / parapetos superiores en las murallas (cy + 5)
        for (int i = -15; i <= 15; i++) {
            boolean wallTooth = Math.abs(i) % 2 == 0;
            setFortressBattlement(level, cx + i, cy + 5, cz + 15, wallTooth);
            setFortressBattlement(level, cx - 15, cy + 5, cz + i, wallTooth);
            setFortressBattlement(level, cx + 15, cy + 5, cz + i, wallTooth);
            if (Math.abs(i) > 2) {
                setFortressBattlement(level, cx + i, cy + 5, cz - 15, wallTooth);
            }
        }

        // Camino de ronda interior (cy + 3) a lo largo de las murallas
        for (int i = -14; i <= 14; i++) {
            level.setBlock(new BlockPos(cx + i, cy + 3, cz + 14), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 14, cy + 3, cz + i), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 14, cy + 3, cz + i), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            if (Math.abs(i) > 3) {
                level.setBlock(new BlockPos(cx + i, cy + 3, cz - 14), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            }
        }

        // 4. Cuatro Grandes Torres de Vigilancia en las esquinas (NW, NE, SW, SE)
        int[][] towerCorners = {{-15, -15}, {12, -15}, {-15, 12}, {12, 12}};
        for (int[] corner : towerCorners) {
            buildBloodTower(level, cx + corner[0], cy, cz + corner[1]);
        }

        // 5. Puerta de Asedio de la Fortaleza (Sur: cz - 15, cx - 3 a cx + 3)
        buildBloodGatehouse(level, cx, cy, cz - 15);

        // 6. Patio Marcial de Suplicio y Armas (cz = -12 a 5)
        buildBloodCourtyard(level, cx, cy, cz);

        // 7. Salón Principal y Trono del Gran Patriarca (cz = 6 a 14)
        buildBloodPavilion(level, cx, cy, cz);

        // 8. Generar Entidades Defensoras y Jefe
        spawnBloodSectEntities(level, center);
    }

    private static void buildFortressWallSegment(ServerLevel level, int x, int cy, int z) {
        for (int y = 1; y <= 4; y++) {
            level.setBlock(new BlockPos(x, cy + y, z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        }
    }

    private static void setFortressBattlement(ServerLevel level, int x, int y, int z, boolean wall) {
        BlockPos p = new BlockPos(x, y, z);
        if (wall) {
            level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), 2);
        } else {
            level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState(), 2);
        }
    }

    private static void buildBloodTower(ServerLevel level, int minX, int cy, int minZ) {
        // Torre 4x4 elevándose hasta cy + 7
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                int px = minX + x;
                int pz = minZ + z;
                for (int y = 1; y <= 7; y++) {
                    if (x == 0 || x == 3 || z == 0 || z == 3) {
                        if (y == 3 && (x == 1 || x == 2 || z == 1 || z == 2)) {
                            level.setBlock(new BlockPos(px, cy + y, pz), Blocks.IRON_BARS.defaultBlockState(), 2);
                        } else {
                            level.setBlock(new BlockPos(px, cy + y, pz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                        }
                    } else {
                        // Interior
                        level.setBlock(new BlockPos(px, cy + y, pz), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                // Piso superior de la torre en cy + 6
                level.setBlock(new BlockPos(px, cy + 6, pz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        // Tejado oriental de la torre con escaleras de carmesí (cy + 8)
        for (int x = -1; x <= 4; x++) {
            for (int z = -1; z <= 4; z++) {
                BlockPos p = new BlockPos(minX + x, cy + 8, minZ + z);
                if (x == -1 || x == 4 || z == -1 || z == 4) {
                    level.setBlock(p, Blocks.CRIMSON_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.CRIMSON_PLANKS.defaultBlockState(), 2);
                }
            }
        }
        // Cúspide de la torre
        level.setBlock(new BlockPos(minX + 1, cy + 9, minZ + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 2, cy + 9, minZ + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 1, cy + 9, minZ + 2), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 2, cy + 9, minZ + 2), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 1, cy + 10, minZ + 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildBloodGatehouse(ServerLevel level, int cx, int cy, int cz) {
        // Torres laterales de la puerta (cx - 5 a -3 y cx + 3 a 5)
        for (int y = 1; y <= 7; y++) {
            for (int z = 0; z <= 2; z++) {
                for (int x = -5; x <= -3; x++) {
                    level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                }
                for (int x = 3; x <= 5; x++) {
                    level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // Arco de entrada superior sobre el umbral
        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 5, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 5, cz + 1), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 4, cz), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP)
                    .setValue(StairBlock.FACING, Direction.NORTH), 2);
        }

        // Reja de asedio superior (rastrillo de barrotes)
        level.setBlock(new BlockPos(cx - 1, cy + 3, cz), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 3, cz), Blocks.IRON_BARS.defaultBlockState(), 2);

        // Puertas de madera carmesí
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.NORTH), 2);

        // Faroles del portal de asedio
        level.setBlock(new BlockPos(cx - 3, cy + 4, cz - 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 4, cz - 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildBloodCourtyard(ServerLevel level, int cx, int cy, int cz) {
        // Muñecos de entrenamiento marcial
        int[][] dummyCoords = {{-4, -8}, {4, -8}};
        for (int[] dc : dummyCoords) {
            BlockPos p = new BlockPos(cx + dc[0], cy + 1, cz + dc[1]);
            level.setBlock(p, Blocks.OAK_FENCE.defaultBlockState(), 2);
            level.setBlock(p.above(), Blocks.HAY_BLOCK.defaultBlockState(), 2);
            level.setBlock(p.above(2), Blocks.CARVED_PUMPKIN.defaultBlockState(), 2);
        }

        // Armeros / soportes de armas
        level.setBlock(new BlockPos(cx - 6, cy + 1, cz - 1), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 1), Blocks.GRINDSTONE.defaultBlockState(), 2);

        // 1. Pedestal del Estandarte Oeste (cx - 8, cy, cz - 4)
        buildTotemPedestal(level, cx - 8, cy, cz - 4);

        // 2. Pedestal del Estandarte Este (cx + 8, cy, cz - 4)
        buildTotemPedestal(level, cx + 8, cy, cz - 4);
    }

    private static void buildTotemPedestal(ServerLevel level, int x, int cy, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = new BlockPos(x + dx, cy + 1, z + dz);
                if (dx == 0 && dz == 0) {
                    level.setBlock(p, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(x, cy + 2, z), Blocks.RED_NETHER_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x - 1, cy + 2, z), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x + 1, cy + 2, z), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildBloodPavilion(ServerLevel level, int cx, int cy, int cz) {
        // Escaleras ceremoniales de entrada al Pabellón (z = 5 y 6, x = -3..3)
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 5), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + x, cy + 2, cz + 6), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }

        // 8 Columnas maestras de basalto pulido (cy + 2 a cy + 7)
        int[][] pavilionPillars = {{-8, 7}, {-3, 7}, {3, 7}, {8, 7}, {-8, 13}, {-3, 13}, {3, 13}, {8, 13}};
        for (int[] pil : pavilionPillars) {
            for (int y = 3; y <= 7; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.POLISHED_BASALT.defaultBlockState(), 2);
            }
            // Farol en la base de la columna
            level.setBlock(new BlockPos(cx + pil[0], cy + 3, cz + pil[1] - 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        // Pared trasera del pabellón (z = 14, x = -9..9, y = cy + 3 a cy + 7)
        for (int x = -9; x <= 9; x++) {
            for (int y = 3; y <= 7; y++) {
                level.setBlock(new BlockPos(cx + x, cy + y, cz + 14), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
        }

        // Tejado oriental de doble alero (Alero inferior en cy + 8)
        for (int x = -11; x <= 11; x++) {
            for (int z = 6; z <= 15; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 8, cz + z);
                if (x == -11 || x == 11 || z == 6 || z == 15) {
                    level.setBlock(p, Blocks.CRIMSON_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }

        // Nivel superior del tejado (cy + 11 y cy + 12)
        for (int x = -7; x <= 7; x++) {
            for (int z = 8; z <= 14; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 11, cz + z);
                if (x == -7 || x == 7 || z == 8 || z == 14) {
                    level.setBlock(p, Blocks.CRIMSON_STAIRS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }

        // Cresta dorada de la cima del pabellón (cy + 13)
        for (int x = -5; x <= 5; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 12, cz + 11), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 5, cy + 13, cz + 11), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 13, cz + 11), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // --- TRONO DEL PATRIARCA (cx, cy + 3, cz + 10) ---
        BlockPos thronePos = new BlockPos(cx, cy + 3, cz + 10);
        level.setBlock(thronePos.below(), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH), 2);
        level.setBlock(thronePos.above(), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos.west(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(thronePos.east(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(thronePos.above().west(), Blocks.RED_BANNER.defaultBlockState(), 2);
        level.setBlock(thronePos.above().east(), Blocks.RED_BANNER.defaultBlockState(), 2);

        // --- ALTAR DEL ESTANDARTE MAESTRO DE SANGRE (cx, cy + 3, cz + 13) ---
        buildTotemPedestal(level, cx, cy + 2, cz + 13);

        // --- COFRE DEL TESORO ANCESTRAL DE LA SECTA (cx + 2, cy + 3, cz + 13) ---
        BlockPos chestPos = new BlockPos(cx + 2, cy + 3, cz + 13);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(13, makeRelic(Items.WRITTEN_BOOK, 1,
                    "§4§l✦ Manual del Arte Secreto de la Secta de Sangre ✦",
                    "Secta de Sangre - Murim",
                    "Técnica prohibida del Culto Demoníaco. Otorga maestría marcial oscura y desata el Qi Carmesí."));
            container.setItem(11, makeRelic(Items.HEART_OF_THE_SEA, 3,
                    "§c§l✦ Píldora de Condensación de Sangre ✦",
                    "Tesoro Demoníaco",
                    "Concentrado de esencia vital que purifica los meridianos y sana heridas letales."));
            container.setItem(15, new ItemStack(Items.GOLD_INGOT, 32));
        }
    }

    private static void spawnBloodSectEntities(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Patriarca de la Sangre (Jefe frente a su trono)
        BlockPos bossPos = new BlockPos(cx, cy + 3, cz + 9);
        AABB checkBoss = new AABB(bossPos).inflate(35.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity.class, checkBoss, e -> e.isAlive()).isEmpty()) {
            var boss = new com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS.get(), level);
            boss.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 0.0F, 0.0F);
            boss.setPersistenceRequired();
            level.addFreshEntity(boss);
        }

        // 2. Tres Estandartes de Qi Demoníaco
        int[][] totemCoords = {
                {-8, cy + 3, -4},  // Oeste
                {8, cy + 3, -4},   // Este
                {0, cy + 5, 13}    // Altar Maestro
        };
        for (int[] tc : totemCoords) {
            BlockPos tp = new BlockPos(cx + tc[0], tc[1], cz + tc[2]);
            AABB checkTotem = new AABB(tp).inflate(4.0D);
            if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.BloodQiTotemEntity.class, checkTotem, e -> e.isAlive()).isEmpty()) {
                var totem = new com.soybe.awakeningsystem.entity.BloodQiTotemEntity(
                        com.soybe.awakeningsystem.entity.ModEntities.BLOOD_QI_TOTEM.get(), level);
                totem.moveTo(tp.getX() + 0.5D, tp.getY(), tp.getZ() + 0.5D, 0.0F, 0.0F);
                level.addFreshEntity(totem);
            }
        }

        // 3. Discípulos de la Secta de Sangre
        int[][] discipleCoords = {
                {-3, cy + 1, -2},
                {3, cy + 1, -2},
                {-5, cy + 1, -6},
                {5, cy + 1, -6},
                {-13, cy + 7, -13},
                {13, cy + 7, -13}
        };
        for (int[] dc : discipleCoords) {
            BlockPos dp = new BlockPos(cx + dc[0], dc[1], cz + dc[2]);
            AABB checkDisc = new AABB(dp).inflate(5.0D);
            if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.BloodSectDiscipleEntity.class, checkDisc, e -> e.isAlive()).isEmpty()) {
                var disciple = new com.soybe.awakeningsystem.entity.BloodSectDiscipleEntity(
                        com.soybe.awakeningsystem.entity.ModEntities.BLOOD_SECT_DISCIPLE.get(), level);
                disciple.moveTo(dp.getX() + 0.5D, dp.getY(), dp.getZ() + 0.5D, 0.0F, 0.0F);
                disciple.setPersistenceRequired();
                level.addFreshEntity(disciple);
            }
        }
    }

    // =========================================================================
    // 18. ISEKAI — LABERINTO DE LAS RAÍCES DE YGGDRASIL / MAZMORRA ANCESTRAL
    //     (Treant Ancestral de Yggdrasil, Manantial de Savia y Sagrario)
    // =========================================================================
    public static void buildYggdrasilRootLabyrinth(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos subterráneos y despeje de terreno (33x33 bloques)
        ensureFoundation(level, cx - 16, cx + 16, cz - 16, cz + 16, cy - 9, Blocks.DEEPSLATE);
        clearRoom(level, cx - 16, cx + 16, cy - 8, cy - 1, cz - 16, cz + 16);
        clearRoom(level, cx - 16, cx + 16, cy, cy + 8, cz - 16, cz + 16);

        // 2. Túmulo de raíces vivas en superficie
        buildSurfaceRootMound(level, cx, cy, cz);

        // 3. Estructura general de la cámara subterránea del laberinto (techo, paredes y columnas)
        buildSubterraneanLabyrinth(level, cx, cy, cz);

        // 4. Escalera ceremonial abierta de descenso (despeja el pozo 5x5 y conecta superficie con el subsuelo)
        buildEntranceStairway(level, cx, cy, cz);

        // 5. Manantial Sagrado de Savia de Yggdrasil (Sector Sur)
        buildSapSpring(level, cx, cy, cz);

        // 6. Sagrario del Núcleo Raíz y Trono del Treant (Sector Norte)
        buildTreantSanctum(level, cx, cy, cz);

        // 7. Generar Entidades Defensoras y Jefe
        spawnYggdrasilEntities(level, center);
    }

    private static void buildSurfaceRootMound(ServerLevel level, int cx, int cy, int cz) {
        // Suelo exterior del túmulo vegetal
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist <= 16.0D) {
                    BlockPos p = new BlockPos(cx + x, cy, cz + z);
                    if (dist > 3.0D) {
                        if (dist > 13.0D) {
                            level.setBlock(p, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                        } else if ((x + z) % 3 == 0) {
                            level.setBlock(p, Blocks.ROOTED_DIRT.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                        }
                        if (dist > 5.0D && dist < 14.0D && (x * 7 + z * 3) % 5 == 0) {
                            level.setBlock(p.above(), Blocks.MOSS_CARPET.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Arcos de raíces colosales que emergen hacia las diagonales
        int[][] directions = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int[] dir : directions) {
            int dx = dir[0];
            int dz = dir[1];
            for (int step = 3; step <= 13; step++) {
                int px = cx + dx * step;
                int pz = cz + dz * step;
                int archY = cy + (int) Math.round(Math.sin((step - 3) * Math.PI / 10.0D) * 4.0D);
                BlockPos rootPos = new BlockPos(px, archY, pz);
                level.setBlock(rootPos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                level.setBlock(rootPos.above(), Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 2);
                if (step == 8) {
                    // Faroles de rana colgantes iluminando los arcos
                    level.setBlock(rootPos.below(), Blocks.CHAIN.defaultBlockState(), 2);
                    level.setBlock(rootPos.below(2), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void buildEntranceStairway(ServerLevel level, int cx, int cy, int cz) {
        // Despejar completamente todo el pozo vertical central de 5x5 desde cy + 5 hasta el suelo cy - 8
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -8; y <= 5; y++) {
                    if (y == -8) {
                        level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Paredes del pozo de descenso (anillo en radio 3, de cy - 7 a cy - 1)
        for (int i = -3; i <= 3; i++) {
            for (int y = -7; y <= -1; y++) {
                BlockPos pN = new BlockPos(cx + i, cy + y, cz - 3);
                BlockPos pS = new BlockPos(cx + i, cy + y, cz + 3);
                BlockPos pW = new BlockPos(cx - 3, cy + y, cz + i);
                BlockPos pE = new BlockPos(cx + 3, cy + y, cz + i);

                BlockState wall = ((i + y) % 3 == 0) ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState() : Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                level.setBlock(pN, wall, 2);
                level.setBlock(pS, wall, 2);
                level.setBlock(pW, wall, 2);
                level.setBlock(pE, wall, 2);
            }
        }

        // Abrir paso al sur en la base (hacia el manantial y el laberinto, z = -3) y al norte (hacia el sagrario, z = 3)
        for (int x = -1; x <= 1; x++) {
            for (int y = -7; y <= -5; y++) {
                level.setBlock(new BlockPos(cx + x, cy + y, cz - 3), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + x, cy + y, cz + 3), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Columna central de madera de Yggdrasil (cx, cz)
        for (int y = -8; y <= 3; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }

        // Farol de rana verde en el pilar central para iluminar la bajada
        level.setBlock(new BlockPos(cx, cy - 4, cz), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);

        // Peldaños de escalera en espiral amplia y suave (descendiendo desde cy hacia cy - 7)
        int[][] steps = {
                {2, 0, 0},    // cy      (x=2, z=0): Entrada en superficie
                {2, 1, -1},   // cy - 1  (x=2, z=1)
                {1, 2, -2},   // cy - 2  (x=1, z=2)
                {0, 2, -3},   // cy - 3  (x=0, z=2)
                {-1, 2, -4},  // cy - 4  (x=-1, z=2)
                {-2, 1, -5},  // cy - 5  (x=-2, z=1)
                {-2, 0, -6},  // cy - 6  (x=-2, z=0)
                {-2, -1, -7}, // cy - 7  (x=-2, z=-1): Llegada al piso subterráneo
                {-1, -2, -7}, // cy - 7  (x=-1, z=-2): Conexión directa con corredor
                {0, -2, -7}   // cy - 7  (x=0, z=-2): Paso libre
        };

        for (int[] s : steps) {
            int sx = cx + s[0];
            int sz = cz + s[1];
            int sy = cy + s[2];
            BlockPos stepPos = new BlockPos(sx, sy, sz);

            level.setBlock(stepPos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
            // Garantizar despeje vertical total sobre cada escalón
            for (int h = 1; h <= 4; h++) {
                level.setBlock(stepPos.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Faroles ornamentales de rana en la entrada de la superficie (cy + 1)
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
    }

    private static void buildSubterraneanLabyrinth(ServerLevel level, int cx, int cy, int cz) {
        // 1. Pavimento del laberinto a cy - 8 y techo a cy - 1
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                BlockPos floorPos = new BlockPos(cx + x, cy - 8, cz + z);
                BlockPos roofPos = new BlockPos(cx + x, cy - 1, cz + z);

                // Suelo orgánico rúnico
                if ((x * x + z * z) % 7 == 0) {
                    level.setBlock(floorPos, Blocks.EMERALD_ORE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(floorPos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(floorPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                }

                // Omitir el hueco central de la escalera para no tapar la entrada ni los peldaños
                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) {
                    continue;
                }

                // Techo abovedado de raíces vivientes
                if ((x + z) % 3 == 0) {
                    level.setBlock(roofPos, Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
                } else {
                    level.setBlock(roofPos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                }

                // Elementos colgantes del techo (esporas y raíces en cy - 2)
                if ((x * 5 + z * 3) % 11 == 0 && Math.abs(x) > 3 && Math.abs(z) > 3) {
                    level.setBlock(roofPos.below(), Blocks.SPORE_BLOSSOM.defaultBlockState(), 2);
                } else if ((x * 7 + z * 5) % 9 == 0 && Math.abs(x) > 3 && Math.abs(z) > 3) {
                    level.setBlock(roofPos.below(), Blocks.HANGING_ROOTS.defaultBlockState(), 2);
                }
            }
        }

        // 2. Muros perimetrales (x = ±16 o z = ±16, y = cy - 8 a cy - 1)
        for (int i = -16; i <= 16; i++) {
            for (int y = -8; y <= -1; y++) {
                boolean logPillar = (i % 4 == 0);
                BlockState wallState = logPillar ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState() : Blocks.MOSSY_STONE_BRICKS.defaultBlockState();

                level.setBlock(new BlockPos(cx + i, cy + y, cz + 16), wallState, 2);
                level.setBlock(new BlockPos(cx + i, cy + y, cz - 16), wallState, 2);
                level.setBlock(new BlockPos(cx + 16, cy + y, cz + i), wallState, 2);
                level.setBlock(new BlockPos(cx - 16, cy + y, cz + i), wallState, 2);
            }
        }

        // 3. Cuatro Columnas Colosales de Raíces de Yggdrasil (3x3 en x = ±8, z = ±8)
        int[][] pillarCoords = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pc : pillarCoords) {
            int px = cx + pc[0];
            int pz = cz + pc[1];
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int y = -8; y <= -1; y++) {
                        BlockPos p = new BlockPos(px + dx, cy + y, pz + dz);
                        if (dx == 0 && dz == 0) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        } else if ((dx + dz + y) % 2 == 0) {
                            level.setBlock(p, Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        }
                    }
                }
            }
            // Faroles de rana verdes empotrados en las columnas para iluminación mágica
            level.setBlock(new BlockPos(px, cy - 5, pz - 2), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy - 5, pz + 2), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
        }
    }

    private static void buildSapSpring(ServerLevel level, int cx, int cy, int cz) {
        // Estanque central de savia sagrada (5x5, cz = -9, cx = 0)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy - 8, cz - 9 + dz);
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    // Borde del estanque de ladrillos musgosos
                    level.setBlock(p, Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState(), 2);
                } else {
                    // Fondo luminoso del estanque
                    level.setBlock(p.below(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        // Nenúfares en la superficie del estanque sagrado
        level.setBlock(new BlockPos(cx - 1, cy - 7, cz - 10), Blocks.LILY_PAD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy - 7, cz - 8), Blocks.LILY_PAD.defaultBlockState(), 2);

        // Brote místico de savia en el centro del estanque
        level.setBlock(new BlockPos(cx, cy - 8, cz - 9), Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy - 7, cz - 9), Blocks.OCHRE_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy - 6, cz - 9), Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 2);

        // Pedestales laterales con mandrágoras cultivadas
        level.setBlock(new BlockPos(cx - 4, cy - 8, cz - 9), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy - 7, cz - 9), Blocks.POTTED_FERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy - 8, cz - 9), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy - 7, cz - 9), Blocks.POTTED_FERN.defaultBlockState(), 2);
    }

    private static void buildTreantSanctum(ServerLevel level, int cx, int cy, int cz) {
        // Plataforma ritual elevada (9x9 a cy - 7, cz = +9)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy - 7, cz + 9 + dz);
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    level.setBlock(p, Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM), 2);
                } else if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    level.setBlock(p, Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
                } else if ((dx + dz) % 2 == 0) {
                    level.setBlock(p, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        // Trono de Raíces Ancestrales (cx, cz + 13, y = cy - 6 a cy - 4)
        for (int y = -6; y <= -4; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz + 13), Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + y, cz + 13), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, cy + y, cz + 13), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy - 6, cz + 13), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy - 6, cz + 13), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);

        // Cofre del Tesoro Ancestral de Yggdrasil con Cadenas y Candado (cx, cy - 6, cz + 12)
        BlockPos chestPos = new BlockPos(cx, cy - 6, cz + 12);
        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState()
                .setValue(LockedTreasureChestBlock.FACING, Direction.SOUTH)
                .setValue(LockedTreasureChestBlock.LOCKED, true), 3);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(11, makeRelic(Items.HEART_OF_THE_SEA, 3,
                    "§a§l✦ Rocío de Maná de Yggdrasil ✦",
                    "Esencia Vital",
                    "Gota concentrada de la savia del Árbol del Mundo. Restaura todo el mana y purifica el cuerpo."));
            container.setItem(13, new ItemStack(Items.GOLDEN_APPLE, 16));
            container.setItem(14, new ItemStack(Items.GOLD_INGOT, 32));
            container.setItem(15, new ItemStack(Items.EMERALD, 48));
            container.setItem(12, new ItemStack(Items.DIAMOND, 12));
        }
    }

    private static void spawnYggdrasilEntities(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Jefe: Treant Ancestral de Yggdrasil
        BlockPos bossPos = new BlockPos(cx, cy - 6, cz + 8);
        AABB checkBoss = new AABB(bossPos).inflate(30.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity.class, checkBoss, e -> e.isAlive()).isEmpty()) {
            var boss = new com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get(), level);
            boss.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 0.0F, 0.0F);
            boss.setPersistenceRequired();
            level.addFreshEntity(boss);
        }

        // 2. Mandrágoras Chillonas en el sector del estanque
        int[][] mandrakeCoords = {
                {-3, cy - 7, -9},
                {3, cy - 7, -9},
                {-2, cy - 7, -13},
                {2, cy - 7, -13}
        };
        for (int[] mc : mandrakeCoords) {
            BlockPos mp = new BlockPos(cx + mc[0], mc[1], cz + mc[2]);
            AABB checkMandrake = new AABB(mp).inflate(4.0D);
            if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.ScreamingMandrakeEntity.class, checkMandrake, e -> e.isAlive()).isEmpty()) {
                var mandrake = new com.soybe.awakeningsystem.entity.ScreamingMandrakeEntity(
                        com.soybe.awakeningsystem.entity.ModEntities.SCREAMING_MANDRAKE.get(), level);
                mandrake.moveTo(mp.getX() + 0.5D, mp.getY(), mp.getZ() + 0.5D, 0.0F, 0.0F);
                mandrake.setPersistenceRequired();
                level.addFreshEntity(mandrake);
            }
        }

        // 3. Hadas de Maná revoloteando en el manantial
        int[][] pixieCoords = {
                {-1, cy - 5, -9},
                {1, cy - 5, -9}
        };
        for (int[] pc : pixieCoords) {
            BlockPos pp = new BlockPos(cx + pc[0], pc[1], cz + pc[2]);
            AABB checkPixie = new AABB(pp).inflate(4.0D);
            if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.ManaPixieEntity.class, checkPixie, e -> e.isAlive()).isEmpty()) {
                var pixie = new com.soybe.awakeningsystem.entity.ManaPixieEntity(
                        com.soybe.awakeningsystem.entity.ModEntities.MANA_PIXIE.get(), level);
                pixie.moveTo(pp.getX() + 0.5D, pp.getY(), pp.getZ() + 0.5D, 0.0F, 0.0F);
                pixie.setPersistenceRequired();
                level.addFreshEntity(pixie);
            }
        }
    }

    // =========================================================================
    // ESTRUCTURA 7: GRUTAS SECRETAS DE INMORTALES ANCESTRALES (MURIM)
    // Puzle de los Cinco Elementos (Wu Xing), Sarcófago Ancestral y Guardián de Jade
    // =========================================================================

    public static void buildAncestralImmortalGrotto(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Despejar y excavar la Cámara 1: Sala del Puzle Wu Xing (25x25x9 de cy a cy + 8)
        clearRoom(level, cx - 12, cx + 12, cy, cy + 8, cz - 12, cz + 12);

        // Suelo, muros y techo de piedra profunda y ladrillos arcanos
        for (int x = cx - 13; x <= cx + 13; x++) {
            for (int z = cz - 13; z <= cz + 13; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist <= 13.5D) {
                    // Cimientos y suelo
                    level.setBlock(new BlockPos(x, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, cy, z), (dist >= 12.0D) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);

                    // Muros perimetrales
                    if (dist >= 11.5D && dist <= 13.5D) {
                        for (int y = cy + 1; y <= cy + 7; y++) {
                            // Abrir entrada Norte (hacia el túnel exterior) y paso Sur (hacia la compuerta del Sanctum)
                            if (z < cz && Math.abs(x - cx) <= 2 && y <= cy + 5) {
                                continue;
                            }
                            if (z > cz && Math.abs(x - cx) <= 2 && y <= cy + 5) {
                                continue;
                            }
                            BlockPos wallPos = new BlockPos(x, y, z);
                            BlockState mat = (y % 2 == 0) ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
                            level.setBlock(wallPos, mat, 2);
                        }
                    }

                    // Techo abovedado
                    level.setBlock(new BlockPos(x, cy + 8, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        // 2. Construir la Gran Entrada Ceremonial y Fachada Exterior (Norte)
        buildGrottoEntrance(level, cx, cy, cz);

        // 3. Construir el Puzle de los Cinco Elementos en la Cámara 1
        buildWuXingChamber(level, cx, cy, cz);

        // 3. Compuerta Sellada del Inmortal en Z = cz + 13 (ancho 5, alto 4)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                BlockPos gateBlock = new BlockPos(cx + dx, cy + dy, cz + 13);
                level.setBlock(gateBlock, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
            }
        }
        // Pilares del marco de la compuerta
        for (int sign : new int[]{-3, 3}) {
            for (int dy = 1; dy <= 5; dy++) {
                level.setBlock(new BlockPos(cx + sign, cy + dy, cz + 13), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            }
        }

        // 4. Excavación y construcción de la Cámara 2: Sanctum del Sarcófago Ancestral (19x19x9)
        int innerCz = cz + 23;
        clearRoom(level, cx - 9, cx + 9, cy, cy + 8, innerCz - 9, innerCz + 9);

        for (int x = cx - 10; x <= cx + 10; x++) {
            for (int z = innerCz - 10; z <= innerCz + 10; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - innerCz) * (z - innerCz));
                if (dist <= 10.5D) {
                    level.setBlock(new BlockPos(x, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, cy, z), (dist >= 9.0D) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);

                    // Muros de cuarzo y prismarina
                    if (dist >= 8.5D && dist <= 10.5D) {
                        for (int y = cy + 1; y <= cy + 7; y++) {
                            level.setBlock(new BlockPos(x, y, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                        }
                    }
                    level.setBlock(new BlockPos(x, cy + 8, z), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }

        // Pasillo de unión entre ambas salas (Z: cz + 13 a cz + 15)
        for (int z = cz + 13; z <= cz + 15; z++) {
            for (int dx = -2; dx <= 2; dx++) {
                level.setBlock(new BlockPos(cx + dx, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy + 5, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }
            for (int y = cy + 1; y <= cy + 4; y++) {
                level.setBlock(new BlockPos(cx - 3, y, z), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 3, y, z), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            }
        }

        // 5. Construir Sanctum del Sarcófago, Cofre con Candado y Jefe
        buildImmortalSanctum(level, cx, cy, innerCz);

        // 6. Spawnear Jefe Guardián de Jade Ancestral
        spawnJadeGuardian(level, new BlockPos(cx, cy + 1, innerCz));
    }

    private static void buildWuXingChamber(ServerLevel level, int cx, int cy, int cz) {
        // Taiji / Yin-Yang central a cy
        level.setBlock(new BlockPos(cx, cy, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz + 1), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz - 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);

        // 5 Pedestales Elementales en Pentagrama Taoísta no secuencial (Estrella de Qi)
        // 1. Agua (Shuǐ - Norte, cz - 7) [Paso 5 / Último elemento]
        BlockPos waterPos = new BlockPos(cx, cy + 1, cz - 7);
        level.setBlock(waterPos.below(), Blocks.PACKED_ICE.defaultBlockState(), 2);
        level.setBlock(waterPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.WATER), 3);
        level.setBlock(waterPos.offset(-1, 0, 0), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
        level.setBlock(waterPos.offset(1, 0, 0), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);

        // 2. Madera (Mù - Este, cx + 7, cz - 2) [Paso 1 / Primer elemento]
        BlockPos woodPos = new BlockPos(cx + 7, cy + 1, cz - 2);
        level.setBlock(woodPos.below(), Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
        level.setBlock(woodPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.WOOD), 3);
        level.setBlock(woodPos.offset(0, 0, -1), Blocks.BAMBOO_MOSAIC.defaultBlockState(), 2);
        level.setBlock(woodPos.offset(0, 0, 1), Blocks.BAMBOO_MOSAIC.defaultBlockState(), 2);

        // 3. Metal (Jīn - Sureste, cx + 4, cz + 6) [Paso 4]
        BlockPos metalPos = new BlockPos(cx + 4, cy + 1, cz + 6);
        level.setBlock(metalPos.below(), Blocks.IRON_BLOCK.defaultBlockState(), 2);
        level.setBlock(metalPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.METAL), 3);
        level.setBlock(metalPos.offset(-1, 0, 0), Blocks.COPPER_BLOCK.defaultBlockState(), 2);
        level.setBlock(metalPos.offset(1, 0, 0), Blocks.COPPER_BLOCK.defaultBlockState(), 2);

        // 4. Fuego (Huǒ - Suroeste, cx - 4, cz + 6) [Paso 2]
        BlockPos firePos = new BlockPos(cx - 4, cy + 1, cz + 6);
        level.setBlock(firePos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
        level.setBlock(firePos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.FIRE), 3);
        level.setBlock(firePos.offset(-1, 0, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(firePos.offset(1, 0, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);

        // 5. Tierra (Tǔ - Oeste, cx - 7, cz - 2) [Paso 3]
        BlockPos earthPos = new BlockPos(cx - 7, cy + 1, cz - 2);
        level.setBlock(earthPos.below(), Blocks.MUD_BRICKS.defaultBlockState(), 2);
        level.setBlock(earthPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.EARTH), 3);
        level.setBlock(earthPos.offset(0, 0, -1), Blocks.TERRACOTTA.defaultBlockState(), 2);
        level.setBlock(earthPos.offset(0, 0, 1), Blocks.TERRACOTTA.defaultBlockState(), 2);

        // Faroles de Almas en las paredes de la sala
        level.setBlock(new BlockPos(cx - 8, cy + 4, cz - 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 4, cz - 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 8, cy + 4, cz + 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 4, cz + 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildImmortalSanctum(ServerLevel level, int cx, int cy, int cz) {
        // Plataforma central del Sarcófago Ancestral (5x5 a cy + 1)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
                boolean edge = (Math.abs(dx) == 2 || Math.abs(dz) == 2);
                level.setBlock(p, edge ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            }
        }

        // Pedestal de Jade Imperial (Cofre despejado sin bloques de obsidiana que lo tapen)
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz), Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz), Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 1), Blocks.AIR.defaultBlockState(), 2);
        for (int h = 1; h <= 3; h++) {
            level.setBlock(new BlockPos(cx, cy + 2 + h, cz), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 2 + h, cz + 1), Blocks.AIR.defaultBlockState(), 2);
        }

        // Cofre del Tesoro Ancestral con Candado y Cadenas 3D en (cx, cy + 2, cz)
        BlockPos chestPos = new BlockPos(cx, cy + 2, cz);
        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState()
                .setValue(LockedTreasureChestBlock.FACING, Direction.SOUTH)
                .setValue(LockedTreasureChestBlock.LOCKED, true), 3);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            // Manual de Cultivo Wu Xing
            if (SystemItems.WU_XING_MANUAL.isBound()) {
                container.setItem(13, new ItemStack(SystemItems.WU_XING_MANUAL.get()));
            }
            // Píldoras de Transformación de Alma
            if (SystemItems.SOUL_TRANSFORMATION_PILL.isBound()) {
                container.setItem(11, new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get(), 8));
            }
            // Ginseng Milenario
            if (SystemItems.THOUSAND_YEAR_GINSENG.isBound()) {
                container.setItem(15, new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get(), 4));
            }
            container.setItem(10, new ItemStack(Items.EMERALD, 48));
            container.setItem(12, new ItemStack(Items.GOLD_INGOT, 32));
            container.setItem(14, new ItemStack(Items.DIAMOND, 16));
            container.setItem(16, new ItemStack(Items.NETHERITE_INGOT, 4));
        }

        // 4 Pilares celestiales con varillas del End en las esquinas de la plataforma
        for (int signX : new int[]{-3, 3}) {
            for (int signZ : new int[]{-3, 3}) {
                for (int h = 1; h <= 4; h++) {
                    level.setBlock(new BlockPos(cx + signX, cy + h, cz + signZ), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(cx + signX, cy + 5, cz + signZ), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + signX, cy + 6, cz + signZ), Blocks.END_ROD.defaultBlockState(), 2);
            }
        }
    }

    private static void spawnJadeGuardian(ServerLevel level, BlockPos center) {
        AABB checkBoss = new AABB(center).inflate(25.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.JadeGuardianBossEntity.class, checkBoss, e -> e.isAlive()).isEmpty()) {
            var boss = new com.soybe.awakeningsystem.entity.JadeGuardianBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.JADE_GUARDIAN_BOSS.get(), level);
            boss.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, 180.0F, 0.0F);
            boss.setPersistenceRequired();
            level.addFreshEntity(boss);
        }
    }

    private static void buildGrottoEntrance(ServerLevel level, int cx, int cy, int cz) {
        // 1. Despejar el túnel de entrada Norte desde cz - 12 hasta cz - 28 (ancho 5, alto 5)
        clearRoom(level, cx - 2, cx + 2, cy + 1, cy + 5, cz - 28, cz - 12);

        // 2. Construcción continua de suelo, cimientos, muros y techo desde cz - 28 hasta cz - 12
        for (int z = cz - 28; z <= cz - 12; z++) {
            // Suelo y subsuelo continuo (cero huecos en el piso)
            for (int dx = -2; dx <= 2; dx++) {
                level.setBlock(new BlockPos(cx + dx, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy, z), (Math.abs(dx) == 2) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy + 6, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }

            // Cimientos profundos hacia abajo: rellenar con pizarra para que NUNCA flote en el aire
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -2; dy >= -15; dy--) {
                    BlockPos foundationPos = new BlockPos(cx + dx, cy + dy, z);
                    BlockState existing = level.getBlockState(foundationPos);
                    if (existing.isAir() || existing.canBeReplaced()) {
                        level.setBlock(foundationPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    } else {
                        break;
                    }
                }
            }

            // Muros laterales continuos
            for (int y = cy + 1; y <= cy + 5; y++) {
                BlockState mat = (y % 2 == 0) ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
                level.setBlock(new BlockPos(cx - 3, y, z), mat, 2);
                level.setBlock(new BlockPos(cx + 3, y, z), mat, 2);
            }
        }

        // 3. Arcos ceremoniales y faroles espirituales dentro del túnel
        int[] archZ = {cz - 16, cz - 21, cz - 25};
        for (int az : archZ) {
            level.setBlock(new BlockPos(cx - 2, cy + 1, az), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 2, cy + 2, az), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 2, cy + 3, az), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 2, cy + 4, az), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

            level.setBlock(new BlockPos(cx + 2, cy + 1, az), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 2, cy + 2, az), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 2, cy + 3, az), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 2, cy + 4, az), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

            level.setBlock(new BlockPos(cx, cy + 5, az), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        // 4. Gran Pórtico Paifang de la Boca de la Gruta en cz - 28 a cz - 29
        for (int dx = -3; dx <= 3; dx++) {
            for (int y = cy + 1; y <= cy + 6; y++) {
                if (Math.abs(dx) == 3) {
                    level.setBlock(new BlockPos(cx + dx, y, cz - 28), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + dx, y, cz - 29), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }
        // Viga maestra y aleros curvados tradicionales
        for (int dx = -4; dx <= 4; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz - 28), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 7, cz - 28), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz - 29), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 7, cz - 29), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 7, cz - 29), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz - 28), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);

        // 5. Terraza exterior y escalones de acceso en cz - 29 a cz - 32
        for (int z = cz - 32; z <= cz - 29; z++) {
            for (int dx = -3; dx <= 3; dx++) {
                if (z == cz - 32) {
                    level.setBlock(new BlockPos(cx + dx, cy, z), Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                } else {
                    level.setBlock(new BlockPos(cx + dx, cy, z), (Math.abs(dx) == 3) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(cx + dx, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);

                // Cimientos de la terraza hacia abajo hasta terreno firme
                for (int dy = -2; dy >= -15; dy--) {
                    BlockPos fp = new BlockPos(cx + dx, cy + dy, z);
                    BlockState ex = level.getBlockState(fp);
                    if (ex.isAir() || ex.canBeReplaced()) {
                        level.setBlock(fp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    } else {
                        break;
                    }
                }
            }
        }
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz - 31), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 31), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz - 31), Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 2, cz - 31), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // 6. Despejar el camino natural hacia el exterior frente a la fachada (cz - 29 a cz - 38)
        for (int z = cz - 29; z >= cz - 38; z--) {
            int topY = Math.min(cy + 10, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, cx, z) + 3);
            for (int dx = -3; dx <= 3; dx++) {
                for (int y = cy + 1; y <= topY; y++) {
                    BlockPos p = new BlockPos(cx + dx, y, z);
                    if (!level.getBlockState(p).isAir() && !level.getBlockState(p).is(Blocks.BEDROCK)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // =========================================================================
    // 18. CASTILLOS FLOTANTES DE LOS REYES ELEMENTALES (DIMENSIÓN ISEKAI - ENDGAME)
    // =========================================================================
    /**
     * Megamazmorra celestial definitiva en 3 niveles verticales (según diseño del usuario):
     * - Nivel Inferior (Y ≈ 180): Gran Plaza del Nexus Central abierta conectada mediante 4 puentes
     *   cardinales a las 4 Ciudadelas de los Mini-Jefes (Viento, Escarcha, Magma y Tormenta - 29x29).
     * - Eje Vertical: Gran Columna de Ascensión Celestial con escalinata de caracol y Plataforma
     *   Intermedia Mirador (Y ≈ 196). El acceso a la subida está sellado por la Gran Barrera de Éter.
     * - Nivel Superior (Y ≈ 212 - 245): El Gran Castillo Flotante del Rey Demonio suspendido en el cenit
     *   sobre el complejo (33x33 exterior, murallas almenadas, 4 torres cilíndricas de 23 bloques,
     *   Gran Salón del Trono Imperial, Rey Demonio de los Elementos y Cofre del Tesoro Mítico 3D).
     */
    public static void buildElementalFloatingCastles(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = Math.max(180, center.getY());
        int cz = center.getZ();

        // 1. Limpieza del espacio aéreo para todo el complejo vertical (62 bloques de altura total)
        clearRoom(level, cx - 65, cx + 65, cy - 16, cy + 62, cz - 65, cz + 65);

        // 2. Nivel Inferior: Gran Plaza del Nexus Central (Radio 12)
        buildNexusPlaza(level, cx, cy, cz);

        // 3. Nivel Inferior: 4 Puentes Cardinales hacia las Ciudadelas (conectan step = 11 a 34)
        buildElementalBridge(level, cx, cy, cz, Direction.NORTH);
        buildElementalBridge(level, cx, cy, cz, Direction.WEST);
        buildElementalBridge(level, cx, cy, cz, Direction.SOUTH);
        buildElementalBridge(level, cx, cy, cz, Direction.EAST);

        // 4. Nivel Inferior: 4 Ciudadelas de Mini-Jefes (29x29) en los cuatro extremos cardinales
        buildWindCitadel(level, cx, cy, cz - 48);
        buildFrostBastion(level, cx - 48, cy, cz);
        buildMagmaFortress(level, cx, cy, cz + 48);
        buildStormSpire(level, cx + 48, cy, cz);

        // 4. Nivel Superior: El Gran Castillo Flotante del Rey Demonio (elevado a cy + 32)
        buildCentralDemonCastle(level, cx, cy + 32, cz);

        // 5. Eje Vertical: Gran Aguja de Ascensión Celestial y Mirador (de cy a cy + 32)
        buildAscensionSpire(level, cx, cy, cz);

        // 6. Sellar los 4 portales de entrada de la Aguja con la Gran Barrera de Éter
        sealAscensionWithBarrier(level, cx, cy, cz);

        // 7. Spawnear al Rey Demonio de los Elementos en su Trono Imperial Superior
        spawnElementalDemonKing(level, new BlockPos(cx, cy + 36, cz + 10));
    }

    // =========================================================================
    // NIVEL INFERIOR: PLAZA DEL NEXUS CENTRAL (RADIO 12)
    // =========================================================================
    private static void buildNexusPlaza(ServerLevel level, int cx, int cy, int cz) {
        // Cimientos flotantes inferiores cónicos
        buildIslandKeel(level, cx, cy, cz, 12, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS);

        // Suelo de la Plaza (radio 12)
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 12 * 12) {
                    BlockPos p = new BlockPos(cx + dx, cy, cz + dz);
                    if (d2 >= 10 * 10) {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    } else if (d2 >= 6 * 6) {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                        // Balizas de linternas marinas en el anillo
                        if ((dx == 0 && Math.abs(dz) == 8) || (Math.abs(dx) == 8 && dz == 0) || (Math.abs(dx) == 6 && Math.abs(dz) == 6)) {
                            level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        }
                    } else {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Barandilla perimetral con salidas abiertas hacia los 4 puentes (ancho 5)
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 12 * 12 && d2 >= 11 * 11) {
                    // Dejar abiertas las 4 salidas cardinales hacia los puentes
                    boolean northExit = (dz <= -10 && Math.abs(dx) <= 2);
                    boolean southExit = (dz >= 10 && Math.abs(dx) <= 2);
                    boolean westExit = (dx <= -10 && Math.abs(dz) <= 2);
                    boolean eastExit = (dx >= 10 && Math.abs(dz) <= 2);

                    if (!northExit && !southExit && !westExit && !eastExit) {
                        BlockPos wp = new BlockPos(cx + dx, cy + 1, cz + dz);
                        level.setBlock(wp, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 4 Farolas monumentales en los ángulos diagonales (±7, ±7)
        int[][] lamps = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] l : lamps) {
            level.setBlock(new BlockPos(cx + l[0], cy + 1, cz + l[1]), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + l[0], cy + 2, cz + l[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + l[0], cy + 3, cz + l[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }
    }

    // =========================================================================
    // EJE VERTICAL: GRAN AGUJA DE ASCENSIÓN CELESTIAL Y MIRADOR (cy a cy + 32)
    // =========================================================================
    private static void buildAscensionSpire(ServerLevel level, int cx, int cy, int cz) {
        // 1. Limpiar completamente el interior de la torre (dx = -4..4, dz = -4..4, dy = 1..35)
        // para garantizar que no queden bloques flotantes ni residuales de la quilla del castillo
        for (int dy = 1; dy <= 35; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    level.setBlock(new BlockPos(cx + dx, cy + dy, cz + dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // 2. Muros perimetrales exteriores de la Aguja Monumental (11x11, dx = -5..5, dz = -5..5)
        for (int dy = 1; dy <= 32; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    boolean isWall = (Math.abs(dx) == 5 || Math.abs(dz) == 5);
                    if (isWall) {
                        BlockPos wp = new BlockPos(cx + dx, cy + dy, cz + dz);
                        boolean isCorner = (Math.abs(dx) == 5 && Math.abs(dz) == 5);

                        // Portales inferiores en los 4 puntos cardinales (dy = 1..4, ancho 3)
                        boolean northGate = (dz == -5 && Math.abs(dx) <= 1 && dy <= 4);
                        boolean southGate = (dz == 5 && Math.abs(dx) <= 1 && dy <= 4);
                        boolean westGate = (dx == -5 && Math.abs(dz) <= 1 && dy <= 4);
                        boolean eastGate = (dx == 5 && Math.abs(dz) <= 1 && dy <= 4);

                        // Ventanales luminosos en las alturas intermedias (dy = 10, 18, 26)
                        boolean isWindow = (dy == 10 || dy == 18 || dy == 26) && (
                                (Math.abs(dz) == 5 && Math.abs(dx) <= 1) || (Math.abs(dx) == 5 && Math.abs(dz) <= 1)
                        );

                        if (northGate || southGate || westGate || eastGate) {
                            // Dejar vano de paso abierto para los portales
                            level.setBlock(wp, Blocks.AIR.defaultBlockState(), 2);
                        } else if (isWindow) {
                            level.setBlock(wp, Blocks.TINTED_GLASS.defaultBlockState(), 2);
                        } else if (isCorner) {
                            level.setBlock(wp, (dy % 4 == 0) ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(wp, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Dinteles dorados sobre los 4 arcos de entrada en cy + 5
        for (int w = -1; w <= 1; w++) {
            level.setBlock(new BlockPos(cx + w, cy + 5, cz - 5), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + w, cy + 5, cz + 5), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 5, cy + 5, cz + w), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 5, cy + 5, cz + w), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        }

        // 3. Elevador Celestial de Éter (Columna Central de Burbujas Soul Sand)
        // Núcleo en cx, cz: Suelo Soul Sand en cy + 1, agua continua hasta cy + 32
        BlockPos soulPos = new BlockPos(cx, cy + 1, cz);
        level.setBlock(soulPos, Blocks.SOUL_SAND.defaultBlockState(), 2);
        for (int dy = 2; dy <= 32; dy++) {
            level.setBlock(new BlockPos(cx, cy + dy, cz), Blocks.WATER.defaultBlockState(), 2);
        }
        // Caja de cristal tintado y cuarzo que contiene el elevador
        for (int dy = 1; dy <= 32; dy++) {
            int[][] shaftWalls = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] sw : shaftWalls) {
                // Puerta de entrada en la base (North dz = -1, dy = 1..2)
                if (sw[0] == 0 && sw[1] == -1 && (dy == 1 || dy == 2)) {
                    level.setBlock(new BlockPos(cx + sw[0], cy + dy, cz + sw[1]), Blocks.WARPED_FENCE_GATE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true), 2);
                }
                // Puerta de salida en el castillo superior (South dz = 1, dy = 32..33)
                else if (sw[0] == 0 && sw[1] == 1 && (dy == 32 || dy == 33)) {
                    level.setBlock(new BlockPos(cx + sw[0], cy + dy, cz + sw[1]), Blocks.WARPED_FENCE_GATE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true), 2);
                } else {
                    level.setBlock(new BlockPos(cx + sw[0], cy + dy, cz + sw[1]), Blocks.TINTED_GLASS.defaultBlockState(), 2);
                }
            }
            // Columnas esquineras del pozo del elevador
            int[][] shaftCorners = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int[] sc : shaftCorners) {
                level.setBlock(new BlockPos(cx + sc[0], cy + dy, cz + sc[1]), (dy % 4 == 0) ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
            }
        }

        // 4. Gran Escalinata Cuadrada Perimetral (Ancho 2 bloques, contigua, 100% sólida y transitable)
        // Tramo 1 (Muro Sur): dy = 1..7, x va de -3 a 3, z = 3..4
        for (int i = 0; i <= 6; i++) {
            int sx = -3 + i;
            int sy = cy + 1 + i;
            for (int sz = 3; sz <= 4; sz++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Rellano 1 (Esquina Sureste): dy = 8, x = 3..4, z = 2..4
        for (int rx = 3; rx <= 4; rx++) {
            for (int rz = 2; rz <= 4; rz++) {
                BlockPos p = new BlockPos(cx + rx, cy + 8, cz + rz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Tramo 2 (Muro Este): dy = 9..15, z va de 1 a -3, x = 3..4
        for (int i = 0; i <= 6; i++) {
            int sz = 1 - i;
            int sy = cy + 9 + i;
            for (int sx = 3; sx <= 4; sx++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Rellano 2 (Esquina Noreste y Mirador Intermedio): dy = 16, x = 2..4, z = -3..-4
        for (int rx = 2; rx <= 4; rx++) {
            for (int rz = -4; rz <= -3; rz++) {
                BlockPos p = new BlockPos(cx + rx, cy + 16, cz + rz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Mirador Exterior Intermedio (Y = cy + 16, Radio 8 alrededor de la torre)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 8 * 8 && d2 >= 5 * 5) {
                    BlockPos p = new BlockPos(cx + dx, cy + 16, cz + dz);
                    level.setBlock(p, (d2 >= 7 * 7) ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
                    // Barandilla
                    if (d2 <= 8 * 8 && d2 >= 7 * 7) {
                        level.setBlock(p.above(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
                        if ((dx + dz) % 3 == 0) {
                            level.setBlock(p.above(2), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        // Puerta de acceso al Mirador en el muro Este (x = 5, z = -3..-4, dy = 17..19)
        for (int rz = -4; rz <= -3; rz++) {
            for (int dh = 1; dh <= 3; dh++) {
                level.setBlock(new BlockPos(cx + 5, cy + 16 + dh, cz + rz), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Tramo 3 (Muro Norte): dy = 17..23, x va de 1 a -3, z = -3..-4
        for (int i = 0; i <= 6; i++) {
            int sx = 1 - i;
            int sy = cy + 17 + i;
            for (int sz = -4; sz <= -3; sz++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Rellano 3 (Esquina Noroeste): dy = 24, x = -3..-4, z = -2..-4
        for (int rx = -4; rx <= -3; rx++) {
            for (int rz = -4; rz <= -2; rz++) {
                BlockPos p = new BlockPos(cx + rx, cy + 24, cz + rz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Tramo 4 (Muro Oeste): dy = 25..31, z va de -1 a 3, x = -3..-4
        for (int i = 0; i <= 6; i++) {
            int sz = -1 + i;
            int sy = cy + 25 + i;
            for (int sx = -4; sx <= -3; sx++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // 5. Llegada y Desembarco Superior en el Gran Castillo (Y = cy + 32)
        // Desembarca directamente en el Atrio del Salón del Trono (dx = -4..2, dz = 1..4)
        for (int dx = -4; dx <= 2; dx++) {
            for (int dz = 1; dz <= 4; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy + 32, cz + dz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 5; h++) {
                    level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // Barandilla de seguridad en el atrio superior de llegada para proteger el hueco de la escalera
        for (int dx = -2; dx <= 2; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 33, cz + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx + 2, cy + 33, cz + 2), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 33, cz + 3), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 34, cz + 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 34, cz + 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    // =========================================================================
    // SELLADO DE LA BARRERA DE ÉTER EN LOS 4 PORTALES DE LA AGUJA
    // =========================================================================
    private static void sealAscensionWithBarrier(ServerLevel level, int cx, int cy, int cz) {
        // Dejar los 4 portales de entrada completamente abiertos y limpios con aire para permitir libre paso sin barreras invisibles
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                level.setBlock(new BlockPos(cx + dx, cy + dy, cz - 5), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy + dy, cz + 5), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx - 5, cy + dy, cz + dx), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 5, cy + dy, cz + dx), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
    // =========================================================================
    // NIVEL SUPERIOR: GRAN CASTILLO FLOTANTE DEL REY DEMONIO (33x33)
    // =========================================================================
    private static void buildCentralDemonCastle(ServerLevel level, int cx, int cy, int cz) {
        // A. Cimientos flotantes inferiores de la fortaleza (Cono invertido macizo de roca y obsidiana)
        for (int dy = -1; dy >= -15; dy--) {
            int r = (int) Math.round(18.0 * (1.0 - (double) (-dy) / 17.0));
            int rSq = r * r;
            int innerSq = (r - 2) * (r - 2);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= rSq) {
                        if (Math.abs(dx) <= 5 && Math.abs(dz) <= 5) continue;
                        BlockPos p = new BlockPos(cx + dx, cy + dy, cz + dz);
                        if (dy <= -13) {
                            level.setBlock(p, (dx == 0 && dz == 0) ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState(), 2);
                        } else if (d2 >= innerSq) {
                            level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, (dx + dz) % 2 == 0 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // B. Suelo principal y plataforma exterior a nivel cy (33x33)
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy, cz + dz);
                boolean edge = (Math.abs(dx) >= 15 || Math.abs(dz) >= 15);
                boolean walkway = (Math.abs(dx) >= 12 || Math.abs(dz) >= 12);
                if (edge) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else if (walkway) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, (dx + dz) % 2 == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                }
            }
        }

        // C. Murallas Almenadas Exteriores (Altura 10 bloques, de cy + 1 a cy + 10)
        for (int dy = 1; dy <= 10; dy++) {
            for (int dx = -14; dx <= 14; dx++) {
                for (int dz = -14; dz <= 14; dz++) {
                    boolean isWall = (Math.abs(dx) == 14 || Math.abs(dz) == 14);
                    if (isWall) {
                        // Portón Norte abierto (entrada desde el puente de desembarco, dz = -14, dx entre -2 y 2, dy 1..6)
                        boolean northGate = (dz == -14 && Math.abs(dx) <= 2 && dy <= 6);
                        if (!northGate) {
                            BlockPos wp = new BlockPos(cx + dx, cy + dy, cz + dz);
                            if (dy == 10) {
                                // Almenas superiores en la muralla
                                boolean crenel = (Math.abs(dx) % 2 == 0 && Math.abs(dz) % 2 == 0);
                                level.setBlock(wp, crenel ? Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            } else if (dy >= 8) {
                                level.setBlock(wp, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                            } else {
                                level.setBlock(wp, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }

        // D. 4 Grandes Torres Cilíndricas Esquineras (Diámetro 5, Altura 23 bloques, dy = 1 a 23)
        int[][] towerCenters = {{-14, -14}, {14, -14}, {-14, 14}, {14, 14}};
        for (int[] tc : towerCenters) {
            int tx = cx + tc[0];
            int tz = cz + tc[1];
            for (int dy = 1; dy <= 23; dy++) {
                int r = (dy <= 17) ? 2 : (dy <= 19 ? 3 : (23 - dy)); // Ensanchamiento almenado y aguja cónica
                for (int ox = -r; ox <= r; ox++) {
                    for (int oz = -r; oz <= r; oz++) {
                        if (ox * ox + oz * oz <= r * r) {
                            BlockPos tp = new BlockPos(tx + ox, cy + dy, tz + oz);
                            if (dy >= 20) {
                                level.setBlock(tp, Blocks.OBSIDIAN.defaultBlockState(), 2);
                            } else if (dy >= 17) {
                                boolean crenel = (dy == 19 && (ox + oz) % 2 != 0);
                                level.setBlock(tp, crenel ? Blocks.AIR.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            } else {
                                boolean outer = (ox * ox + oz * oz >= (r - 1) * (r - 1));
                                level.setBlock(tp, outer ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
                // Iluminación con linternas de fuego de almas en las almenas de las torres
                if (dy == 18) {
                    level.setBlock(new BlockPos(tx, cy + dy, tz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                }
            }
            // Balizas luminosas en la punta de las agujas celestiales
            level.setBlock(new BlockPos(tx, cy + 24, tz), Blocks.END_ROD.defaultBlockState(), 2);
        }

        // E. Gran Salón del Trono Interior
        buildDemonKingThroneRoom(level, cx, cy, cz);
    }

    private static void buildDemonKingThroneRoom(ServerLevel level, int cx, int cy, int cz) {
        // 1. Columnatas Corintias Interiores (8 Columnas de basalto pulido y faros marinos)
        int[][] pillars = {
                {-8, -8}, {8, -8},
                {-8, -2}, {8, -2},
                {-8, 4}, {8, 4},
                {-8, 10}, {8, 10}
        };
        for (int[] p : pillars) {
            for (int dy = 1; dy <= 8; dy++) {
                BlockPos colPos = new BlockPos(cx + p[0], cy + dy, cz + p[1]);
                if (dy == 1 || dy == 8) {
                    level.setBlock(colPos, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else if (dy == 4) {
                    level.setBlock(colPos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(colPos, Blocks.POLISHED_BASALT.defaultBlockState(), 2);
                }
            }
            // Antorchas de fuego de almas en los capiteles
            level.setBlock(new BlockPos(cx + p[0], cy + 9, cz + p[1]), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        }

        // 2. Candelabros Colgantes de Fuego de Almas desde las vigas del techo
        int[][] chandeliers = {{-4, 0}, {4, 0}, {-4, 6}, {4, 6}};
        for (int[] ch : chandeliers) {
            for (int dy = 10; dy >= 7; dy--) {
                level.setBlock(new BlockPos(cx + ch[0], cy + dy, cz + ch[1]), Blocks.CHAIN.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + ch[0], cy + 6, cz + ch[1]), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        // 3. Alfombra Imperial Roja y Dorada (Ancho 3, desde la entrada norte dz = -13 hasta el trono dz = 7)
        for (int dz = -13; dz <= 7; dz++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos carpetPos = new BlockPos(cx + w, cy + 1, cz + dz);
                level.setBlock(carpetPos, (w == 0) ? Blocks.RED_CARPET.defaultBlockState() : Blocks.YELLOW_CARPET.defaultBlockState(), 2);
            }
        }

        // 4. Estrado Imperial Elevado del Trono del Rey Demonio (dz = 7 a 12, ancho 9, altura +2)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = 7; dz <= 12; dz++) {
                BlockPos daisPos = new BlockPos(cx + dx, cy + 1, cz + dz);
                level.setBlock(daisPos, Blocks.NETHERITE_BLOCK.defaultBlockState(), 2);
                level.setBlock(daisPos.above(), (Math.abs(dx) == 4 || dz == 12) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        // 5. El Trono Demoníaco (Ubicado en cx, cy + 3, cz + 10)
        BlockPos thronePos = new BlockPos(cx, cy + 3, cz + 10);
        // Base del asiento
        level.setBlock(thronePos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos.west(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(thronePos.east(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        // Respaldo colosal del trono
        for (int dy = 1; dy <= 4; dy++) {
            level.setBlock(thronePos.above(dy), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            if (dy <= 3) {
                level.setBlock(thronePos.above(dy).west(), Blocks.OBSIDIAN.defaultBlockState(), 2);
                level.setBlock(thronePos.above(dy).east(), Blocks.OBSIDIAN.defaultBlockState(), 2);
            }
        }
        level.setBlock(thronePos.above(5), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos.above(6), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // 6. Cofre del Tesoro Mítico 3D (Frente al estrado del trono, cz = 4)
        BlockPos chestPos = new BlockPos(cx, cy + 1, cz + 4);
        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof net.minecraft.world.Container container) {
            container.setItem(0, new ItemStack(SystemItems.ELEMENTAL_DEMON_CROWN.get()));
            container.setItem(1, new ItemStack(SystemItems.CHAOS_MANA_CORE.get(), 2));
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(4, SystemItems.coin(64));
                container.setItem(5, SystemItems.coin(64));
            }
            container.setItem(12, new ItemStack(Items.NETHERITE_INGOT, 8));
            container.setItem(13, new ItemStack(Items.DIAMOND, 32));
            container.setItem(14, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 3));
            container.setItem(16, new ItemStack(Items.GOLDEN_APPLE, 16));
        }
    }

    // =========================================================================
    // PUENTES CARDINALES HACIA LAS 4 CIUDADELAS (step = 11 a 34)
    // =========================================================================
    private static void buildElementalBridge(ServerLevel level, int cx, int cy, int cz, Direction dir) {
        // Conexión continua desde la salida de la Plaza del Nexus (step = 11) hasta el portal de la ciudadela (step = 34)
        for (int step = 11; step <= 34; step++) {
            int bx = cx + dir.getStepX() * step;
            int bz = cz + dir.getStepZ() * step;

            for (int w = -2; w <= 2; w++) {
                int px = (dir.getAxis() == Direction.Axis.Z) ? (bx + w) : bx;
                int pz = (dir.getAxis() == Direction.Axis.X) ? (bz + w) : bz;

                // 1. Suelo principal a nivel cy
                BlockPos floorPos = new BlockPos(px, cy, pz);
                if (Math.abs(w) == 2) {
                    level.setBlock(floorPos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    // Barandilla
                    level.setBlock(floorPos.above(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
                    if (step % 4 == 0) {
                        level.setBlock(floorPos.above(2), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        level.setBlock(floorPos.above(3), Blocks.END_ROD.defaultBlockState(), 2);
                    }
                } else if (Math.abs(w) == 1) {
                    level.setBlock(floorPos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                } else {
                    boolean lamp = (step % 5 == 0);
                    level.setBlock(floorPos, lamp ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                }

                // 2. Arco estructural inferior de soporte
                level.setBlock(floorPos.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                if (Math.abs(w) <= 1 && (step % 3 == 0 || step == 11 || step == 34)) {
                    level.setBlock(floorPos.below(2), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
        }
    }

    // =========================================================================
    // 1. CIUDADELA DEL VIENTO (COLISEO DEL NORTE - RADIO 14, 29x29)
    // =========================================================================
    private static void buildWindCitadel(ServerLevel level, int ix, int cy, int iz) {
        // Cimientos flotantes cónicos inferiores
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.PRISMARINE, Blocks.DARK_PRISMARINE);

        // Suelo del Coliseo (radio 14)
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14) {
                    BlockPos p = new BlockPos(ix + dx, cy, iz + dz);
                    if (d2 >= 12 * 12) {
                        level.setBlock(p, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                    } else if (d2 >= 8 * 8) {
                        level.setBlock(p, (dx + dz) % 2 == 0 ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    } else if (d2 >= 4 * 4) {
                        level.setBlock(p, Blocks.PRISMARINE_BRICKS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    }

                    // Incrustaciones de linternas marinas
                    if ((Math.abs(dx) == 6 && Math.abs(dz) == 6) || (Math.abs(dx) == 10 && dz == 0) || (dx == 0 && Math.abs(dz) == 10)) {
                        level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Muralla perimetral almenada con arcos de viento (radio 13-14)
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
                    // Entrada orientada al Sur (hacia el puente, dz >= 12, dx entre -2 y 2)
                    boolean entrance = (dz >= 12 && Math.abs(dx) <= 2);
                    if (!entrance) {
                        for (int dy = 1; dy <= 3; dy++) {
                            level.setBlock(new BlockPos(ix + dx, cy + dy, iz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                        }
                        level.setBlock(new BlockPos(ix + dx, cy + 4, iz + dz), Blocks.PRISMARINE_WALL.defaultBlockState(), 2);
                        if ((dx + dz) % 3 == 0) {
                            level.setBlock(new BlockPos(ix + dx, cy + 5, iz + dz), Blocks.END_ROD.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 4 Columnas colosales con balizas celestiales en los cuadrantes cardinales
        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(ix + pil[0], cy + 9, iz + pil[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

        // Santuario central elevado del Altar (radio 3, altura +1 y +2)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 3 * 3) {
                    level.setBlock(new BlockPos(ix + dx, cy + 1, iz + dz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(ix, cy + 2, iz), SystemBlocks.ELEMENTAL_ALTAR.get().defaultBlockState()
                .setValue(ElementalAltarBlock.ELEMENT, ElementalType.WIND)
                .setValue(ElementalAltarBlock.ACTIVE, false), 3);
        level.setBlock(new BlockPos(ix - 1, cy + 2, iz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix + 1, cy + 2, iz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz - 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz + 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);

        // Spawn del Mini-Jefe: Señor del Viento
        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.WIND);
    }

    // =========================================================================
    // 2. BASTIÓN GLACIAL (COLISEO DEL OESTE - RADIO 14, 29x29)
    // =========================================================================
    private static void buildFrostBastion(ServerLevel level, int ix, int cy, int iz) {
        // Cimientos inferiores de hielo eterno
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.PACKED_ICE, Blocks.BLUE_ICE);

        // Suelo del Coliseo de Hielo
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14) {
                    BlockPos p = new BlockPos(ix + dx, cy, iz + dz);
                    if (d2 >= 12 * 12) {
                        level.setBlock(p, Blocks.POLISHED_DIORITE.defaultBlockState(), 2);
                    } else if (d2 >= 8 * 8) {
                        level.setBlock(p, (dx + dz) % 2 == 0 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState(), 2);
                    } else if (d2 >= 4 * 4) {
                        level.setBlock(p, Blocks.BLUE_ICE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.PACKED_ICE.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Muralla almenada de hielo compacto y diorita pulida
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
                    // Entrada orientada al Este (hacia el puente, dx >= 12, dz entre -2 y 2)
                    boolean entrance = (dx >= 12 && Math.abs(dz) <= 2);
                    if (!entrance) {
                        for (int dy = 1; dy <= 3; dy++) {
                            level.setBlock(new BlockPos(ix + dx, cy + dy, iz + dz), Blocks.PACKED_ICE.defaultBlockState(), 2);
                        }
                        level.setBlock(new BlockPos(ix + dx, cy + 4, iz + dz), Blocks.DIORITE_WALL.defaultBlockState(), 2);
                        if ((dx + dz) % 4 == 0) {
                            level.setBlock(new BlockPos(ix + dx, cy + 5, iz + dz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 4 Agujas de Escarcha en los cuadrantes
        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.BLUE_ICE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        // Pedestal Glacial Central del Altar (radio 3)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 3 * 3) {
                    level.setBlock(new BlockPos(ix + dx, cy + 1, iz + dz), Blocks.BLUE_ICE.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(ix, cy + 2, iz), SystemBlocks.ELEMENTAL_ALTAR.get().defaultBlockState()
                .setValue(ElementalAltarBlock.ELEMENT, ElementalType.FROST)
                .setValue(ElementalAltarBlock.ACTIVE, false), 3);
        level.setBlock(new BlockPos(ix - 1, cy + 2, iz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix + 1, cy + 2, iz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz - 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz + 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);

        // Spawn del Mini-Jefe: Señor de la Escarcha
        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.FROST);
    }

    // =========================================================================
    // 3. FORTALEZA DE MAGMA (COLISEO DEL SUR - RADIO 14, 29x29)
    // =========================================================================
    private static void buildMagmaFortress(ServerLevel level, int ix, int cy, int iz) {
        // Cimientos inferiores de roca volcánica
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.POLISHED_BASALT, Blocks.NETHER_BRICKS);

        // Suelo del Coliseo de Fuego
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14) {
                    BlockPos p = new BlockPos(ix + dx, cy, iz + dz);
                    if (d2 >= 12 * 12) {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    } else if (d2 >= 8 * 8) {
                        boolean ringAccent = (d2 >= 9 * 9 && d2 <= 10 * 10 && (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz)));
                        level.setBlock(p, ringAccent ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BASALT.defaultBlockState(), 2);
                    } else if (d2 >= 4 * 4) {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Muralla almenada de ladrillos del Nether
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
                    // Entrada orientada al Norte (hacia el puente, dz <= -12, dx entre -2 y 2)
                    boolean entrance = (dz <= -12 && Math.abs(dx) <= 2);
                    if (!entrance) {
                        for (int dy = 1; dy <= 3; dy++) {
                            level.setBlock(new BlockPos(ix + dx, cy + dy, iz + dz), Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                        }
                        level.setBlock(new BlockPos(ix + dx, cy + 4, iz + dz), Blocks.NETHER_BRICK_WALL.defaultBlockState(), 2);
                        if ((dx + dz) % 3 == 0) {
                            level.setBlock(new BlockPos(ix + dx, cy + 5, iz + dz), Blocks.LANTERN.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 4 Bastiones de fuego en los cuadrantes
        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.RED_NETHER_BRICKS.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }

        // Pedestal Central de Obsidiana y Netherite del Altar (radio 3)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 3 * 3) {
                    level.setBlock(new BlockPos(ix + dx, cy + 1, iz + dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(ix, cy + 2, iz), SystemBlocks.ELEMENTAL_ALTAR.get().defaultBlockState()
                .setValue(ElementalAltarBlock.ELEMENT, ElementalType.FIRE)
                .setValue(ElementalAltarBlock.ACTIVE, false), 3);
        level.setBlock(new BlockPos(ix - 1, cy + 2, iz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix + 1, cy + 2, iz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz - 1), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz + 1), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // Spawn del Mini-Jefe: Señor del Magma
        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.FIRE);
    }

    // =========================================================================
    // 4. AGUJA DE LA TORMENTA (COLISEO DEL ESTE - RADIO 14, 29x29)
    // =========================================================================
    private static void buildStormSpire(ServerLevel level, int ix, int cy, int iz) {
        // Cimientos inferiores de cobre y pizarra
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.DEEPSLATE_BRICKS, Blocks.CUT_COPPER);

        // Suelo del Coliseo de la Tormenta
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14) {
                    BlockPos p = new BlockPos(ix + dx, cy, iz + dz);
                    if (d2 >= 12 * 12) {
                        level.setBlock(p, Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), 2);
                    } else if (d2 >= 8 * 8) {
                        level.setBlock(p, (dx + dz) % 2 == 0 ? Blocks.CUT_COPPER.defaultBlockState() : Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), 2);
                    } else if (d2 >= 4 * 4) {
                        level.setBlock(p, Blocks.CUT_COPPER.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Muralla almenada de cobre y pizarra profunda
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
                    // Entrada orientada al Oeste (hacia el puente, dx <= -12, dz entre -2 y 2)
                    boolean entrance = (dx <= -12 && Math.abs(dz) <= 2);
                    if (!entrance) {
                        for (int dy = 1; dy <= 3; dy++) {
                            level.setBlock(new BlockPos(ix + dx, cy + dy, iz + dz), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                        }
                        level.setBlock(new BlockPos(ix + dx, cy + 4, iz + dz), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
                        if ((dx + dz) % 4 == 0) {
                            level.setBlock(new BlockPos(ix + dx, cy + 5, iz + dz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // 4 Pilares de pararrayos de tormenta
        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.CUT_COPPER.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        }

        // Pedestal Central de Cobre Cincelado del Altar (radio 3)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 3 * 3) {
                    level.setBlock(new BlockPos(ix + dx, cy + 1, iz + dz), Blocks.OXIDIZED_CHISELED_COPPER.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(ix, cy + 2, iz), SystemBlocks.ELEMENTAL_ALTAR.get().defaultBlockState()
                .setValue(ElementalAltarBlock.ELEMENT, ElementalType.STORM)
                .setValue(ElementalAltarBlock.ACTIVE, false), 3);
        level.setBlock(new BlockPos(ix - 1, cy + 2, iz), Blocks.CUT_COPPER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix + 1, cy + 2, iz), Blocks.CUT_COPPER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz - 1), Blocks.CUT_COPPER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ix, cy + 2, iz + 1), Blocks.CUT_COPPER.defaultBlockState(), 2);

        // Spawn del Mini-Jefe: Señor de la Tormenta
        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.STORM);
    }

    private static void buildIslandKeel(ServerLevel level, int ix, int cy, int iz, int baseRadius, net.minecraft.world.level.block.Block core, net.minecraft.world.level.block.Block shell) {
        for (int dy = -1; dy >= -12; dy--) {
            int r = (int) Math.round(baseRadius * (1.0 - (double) (-dy) / 14.0));
            int rSq = r * r;
            int innerSq = (r - 2) * (r - 2);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= rSq) {
                        BlockPos p = new BlockPos(ix + dx, cy + dy, iz + dz);
                        level.setBlock(p, (d2 >= innerSq) ? shell.defaultBlockState() : core.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void spawnElementalDemonKing(ServerLevel level, BlockPos center) {
        AABB checkBoss = new AABB(center).inflate(35.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity.class, checkBoss, e -> e.isAlive()).isEmpty()) {
            var boss = new com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_DEMON_KING.get(), level);
            boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 180.0F, 0.0F);
            boss.setPersistenceRequired();
            level.addFreshEntity(boss);
        }
    }

    private static void spawnElementalLord(ServerLevel level, BlockPos center, ElementalType type) {
        AABB checkBoss = new AABB(center).inflate(25.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.ElementalLordBossEntity.class, checkBoss, e -> e.getElementalType() == type && e.isAlive()).isEmpty()) {
            var lord = new com.soybe.awakeningsystem.entity.ElementalLordBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.ELEMENTAL_LORD.get(), level);
            lord.setElementalType(type);
            lord.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
            lord.setPersistenceRequired();
            level.addFreshEntity(lord);
        }
    }

    // =========================================================================
    // 25. CAMPAMENTO DE EXPEDICIÓN ABANDONADO (DIMENSIÓN ISEKAI)
    // =========================================================================
    public static void buildHunterExpeditionCamp(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos rústicos y despeje de terreno amplio (21x17)
        ensureFoundation(level, cx - 11, cx + 9, cz - 8, cz + 8, cy - 1, Blocks.DIRT);
        clearRoom(level, cx - 11, cx + 9, cy, cy + 9, cz - 8, cz + 8);

        // Suelo orgánico detallado: tierra tosca, raíces, barro, caminos de tablones
        java.util.Random rnd = new java.util.Random((long) cx * 31237L ^ (long) cz * 71829L);
        for (int x = -11; x <= 9; x++) {
            for (int z = -8; z <= 8; z++) {
                int distSq = x * x + z * z;
                if (distSq > 95) continue;
                BlockPos p = new BlockPos(cx + x, cy - 1, cz + z);
                int r = rnd.nextInt(10);
                if (r < 3) {
                    level.setBlock(p, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                } else if (r < 5) {
                    level.setBlock(p, Blocks.ROOTED_DIRT.defaultBlockState(), 2);
                } else if (r < 7) {
                    level.setBlock(p, Blocks.PACKED_MUD.defaultBlockState(), 2);
                } else if (r < 9) {
                    level.setBlock(p, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // Caminos de tablones de abeto que conectan el campamento
        for (int z = -4; z <= 4; z++) {
            level.setBlock(new BlockPos(cx, cy - 1, cz + z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        }
        for (int x = -3; x <= 5; x++) {
            level.setBlock(new BlockPos(cx + x, cy - 1, cz), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        }

        // 2. Hoguera central con zona pavimentada de adoquines
        BlockPos firePos = new BlockPos(cx, cy, cz);
        level.setBlock(firePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 2);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy - 1, cz + dz), Blocks.COBBLESTONE.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy - 1, cz - 2), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy - 1, cz + 2), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy - 1, cz + 3), Blocks.COBBLESTONE.defaultBlockState(), 2);

        // 3. Gran Carpa de Expedición (Lado Oeste: réplica exacta del diseño fotográfico del usuario)
        buildGrandExpeditionTent(level, cx, cy, cz);

        // BANCOS DE TRONCOS RÚSTICOS PARA SENTARSE (EXACTAMENTE 3 EN CADA LADO: NORTE, SUR, ESTE, OESTE)
        // Tronco alargado Norte (3 asientos mirando al Sur hacia la fogata)
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy - 1, cz - 2), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy, cz - 2),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.SOUTH), 2);
        }
        // Tronco alargado Sur (3 asientos mirando al Norte hacia la fogata)
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy - 1, cz + 2), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy, cz + 2),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.NORTH), 2);
        }
        // Tronco alargado Este (3 asientos mirando al Oeste hacia la fogata)
        for (int dz = -1; dz <= 1; dz++) {
            level.setBlock(new BlockPos(cx + 2, cy - 1, cz + dz), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 2, cy, cz + dz),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.WEST), 2);
        }
        // Tronco alargado Oeste (3 asientos mirando al Este hacia la fogata)
        for (int dz = -1; dz <= 1; dz++) {
            level.setBlock(new BlockPos(cx - 2, cy - 1, cz + dz), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 2, cy, cz + dz),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.EAST), 2);
        }

        // 4. Cobertizo de Armería y Suministros (Lado Este: cx + 5, cz - 3)
        buildSupplyAwning(level, cx + 5, cy, cz - 3);

        // 5. Puesto de Vigía Elevado (Lado Este: cx + 5, cz + 4)
        buildWatchpost(level, cx + 5, cy, cz + 4);

        // 6. Faroles y postes perimetrales
        level.setBlock(new BlockPos(cx - 10, cy, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 10, cy + 1, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 1, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);
    }

    private static void buildGrandExpeditionTent(ServerLevel level, int cx, int cy, int cz) {
        // Despejar completamente todo el volumen de la carpa (interior y lona) hasta cx - 3
        for (int x = cx - 11; x <= cx - 3; x++) {
            for (int z = cz - 5; z <= cz + 5; z++) {
                for (int y = 0; y <= 6; y++) {
                    level.setBlock(new BlockPos(x, cy + y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // Suelo entarimado completo de madera de roble claro (Oak Planks)
        for (int x = cx - 10; x <= cx - 3; x++) {
            for (int z = cz - 4; z <= cz + 4; z++) {
                level.setBlock(new BlockPos(x, cy - 1, z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        // Sendero de tierra apisonada en la entrada
        level.setBlock(new BlockPos(cx - 2, cy - 1, cz), Blocks.DIRT_PATH.defaultBlockState(), 2);

        // Viga cumbrera horizontal a altura cy + 4 (desde x = cx - 10 hasta cx - 3 a lo largo de X)
        for (int x = cx - 10; x <= cx - 3; x++) {
            level.setBlock(new BlockPos(x, cy + 4, cz),
                    Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 2);
        }

        // Estructura de lona escalonada a dos aguas (inclinación en Z hacia Norte y Sur)
        for (int x = cx - 10; x <= cx - 3; x++) {
            // Nivel 1 (Tier 1): Lana verde superior a cy + 3 en z = cz - 1 y cz + 1
            level.setBlock(new BlockPos(x, cy + 3, cz - 1), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy + 3, cz + 1), Blocks.GREEN_WOOL.defaultBlockState(), 2);

            // Nivel 2 (Tier 2): Lana verde intermedia a cy + 2 en z = cz - 2 y cz + 2
            level.setBlock(new BlockPos(x, cy + 2, cz - 2), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy + 2, cz + 2), Blocks.GREEN_WOOL.defaultBlockState(), 2);

            // Nivel 3 (Tier 3): Lana verde baja a cy + 1 en z = cz - 3 y cz + 3
            level.setBlock(new BlockPos(x, cy + 1, cz - 3), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy + 1, cz + 3), Blocks.GREEN_WOOL.defaultBlockState(), 2);

            // Nivel 4 (Tier 4): Zócalo perimetral de lana marrón a cy + 0 en z = cz - 4 y cz + 4
            level.setBlock(new BlockPos(x, cy, cz - 4), Blocks.BROWN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy, cz + 4), Blocks.BROWN_WOOL.defaultBlockState(), 2);
        }

        // Pared trasera de fondo de la carpa (x = cx - 10)
        // Pilar maestro vertical central de tronco de abeto
        for (int y = 0; y <= 3; y++) {
            level.setBlock(new BlockPos(cx - 10, cy + y, cz), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
        }
        for (int dz = -3; dz <= 3; dz++) {
            if (dz == 0) continue;
            level.setBlock(new BlockPos(cx - 10, cy, cz + dz), Blocks.BROWN_WOOL.defaultBlockState(), 2);
            if (Math.abs(dz) <= 2) {
                level.setBlock(new BlockPos(cx - 10, cy + 1, cz + dz), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            }
            if (Math.abs(dz) == 1) {
                level.setBlock(new BlockPos(cx - 10, cy + 2, cz + dz), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            }
        }

        // Entrada frontal (x = cx - 3):
        // Postes de valla de abeto de 3 bloques de altura en z = cz - 1 y z = cz + 1
        for (int y = 0; y <= 2; y++) {
            level.setBlock(new BlockPos(cx - 3, cy + y, cz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 3, cy + y, cz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        }

        // =====================================================================
        // MOBILIARIO INTERIOR (Réplica exacta de media_1789599797648.png)
        // =====================================================================

        // Pasillo central: Alfombra roja desde la entrada hasta el fondo
        for (int x = cx - 9; x <= cx - 4; x++) {
            level.setBlock(new BlockPos(x, cy, cz), Blocks.RED_CARPET.defaultBlockState(), 2);
        }

        // Farol colgante delantero (x = cx - 4): Cadena a cy + 3, farol a cy + 2
        level.setBlock(new BlockPos(cx - 4, cy + 3, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Farol colgante trasero (x = cx - 7): Dos cadenas y farol a cy + 1
        level.setBlock(new BlockPos(cx - 7, cy + 3, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 2, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // LADO DERECHO (Mirando desde la entrada hacia el fondo: cz - 1 y cz - 2)
        // Cofre de expedición con el Tomo I de Lore junto al pilar central
        BlockPos campChest = new BlockPos(cx - 9, cy, cz - 1);
        level.setBlock(campChest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        fillHunterCampChest(level, campChest);

        // Cama roja con almohada blanca junto al cofre
        BlockPos bedHead = new BlockPos(cx - 9, cy, cz - 2);
        BlockPos bedFoot = new BlockPos(cx - 8, cy, cz - 2);
        level.setBlock(bedHead, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.WEST).setValue(BedBlock.PART, BedPart.HEAD), 2);
        level.setBlock(bedFoot, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.WEST).setValue(BedBlock.PART, BedPart.FOOT), 2);

        // LADO IZQUIERDO (Mirando desde la entrada hacia el fondo: cz + 2)
        // Barril acostado
        level.setBlock(new BlockPos(cx - 9, cy, cz + 2),
                Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.EAST), 2);
        // Mesa de crafteo
        level.setBlock(new BlockPos(cx - 8, cy, cz + 2), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        // Librería
        level.setBlock(new BlockPos(cx - 7, cy, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
    }


    private static void buildSupplyAwning(ServerLevel level, int sx, int sy, int sz) {
        // 4 postes de soporte de valla de abeto
        level.setBlock(new BlockPos(sx - 1, sy, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy + 1, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy + 1, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy + 1, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy + 1, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);

        // Toldo de lana marrón y amarilla
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState st = ((dx + dz) % 2 == 0) ? Blocks.BROWN_WOOL.defaultBlockState() : Blocks.YELLOW_WOOL.defaultBlockState();
                level.setBlock(new BlockPos(sx + dx, sy + 2, sz + dz), st, 2);
            }
        }

        // Estación de herrería y almacenamiento bajo el toldo
        level.setBlock(new BlockPos(sx, sy - 1, sz), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx, sy, sz), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy, sz), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy, sz), Blocks.BARREL.defaultBlockState(), 2);
        // BARRIL APILADO CON FACING VÁLIDO (corregido crash de axis en barrel)
        level.setBlock(new BlockPos(sx + 1, sy + 1, sz),
                Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 2);
        level.setBlock(new BlockPos(sx, sy, sz + 1), Blocks.CAULDRON.defaultBlockState(), 2);
    }

    private static void buildWatchpost(ServerLevel level, int wx, int wy, int wz) {
        // 4 patas de troncos de abeto de altura 3
        for (int y = 0; y <= 2; y++) {
            level.setBlock(new BlockPos(wx - 1, wy + y, wz - 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(wx + 1, wy + y, wz - 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(wx - 1, wy + y, wz + 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(wx + 1, wy + y, wz + 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
        }

        // Plataforma de 3x3 a altura wy + 3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(wx + dx, wy + 3, wz + dz), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            }
        }

        // Barandillas de vallas alrededor de la plataforma
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                    if (dx == 0 && dz == -1) continue; // acceso de escalera
                    level.setBlock(new BlockPos(wx + dx, wy + 4, wz + dz), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
                }
            }
        }

        // Escalera de mano en el lado norte (wz - 1)
        for (int y = 0; y <= 3; y++) {
            level.setBlock(new BlockPos(wx, wy + y, wz - 1),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 2);
        }

        // Farol de vigía en esquina
        level.setBlock(new BlockPos(wx + 1, wy + 5, wz + 1), Blocks.LANTERN.defaultBlockState(), 2);
    }

    private static void fillHunterCampChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            ItemStack slowFallPotion = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    Items.POTION, net.minecraft.world.item.alchemy.Potions.SLOW_FALLING);
            container.setItem(0, slowFallPotion);
            container.setItem(1, new ItemStack(Items.SPYGLASS, 1));
            container.setItem(4, new ItemStack(Items.IRON_PICKAXE, 1));
            container.setItem(8, new ItemStack(Items.STRING, 16));

            // Libro desgastado de Lore Vol. I
            container.setItem(9, com.soybe.awakeningsystem.item.WornLoreBookItem.createVolume(1));

            container.setItem(11, new ItemStack(Items.GOLDEN_APPLE, 2));
            container.setItem(13, new ItemStack(Items.TORCH, 32));
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(18, new ItemStack(SystemItems.AWAKENING_COIN.get(), 25));
            }
        }
    }

    // =========================================================================
    // 26. TORRE DE OBSERVACIÓN ASTRAL EN RUINAS (DIMENSIÓN ISEKAI) - GRAN TORRE 13x13 HERMÉTICA
    // =========================================================================
    public static void buildRuinedStargazerOutpost(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimientos sólidos y despeje amplio del volumen total
        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.DEEPSLATE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 22, cz - 8, cz + 8);

        // 2. Construcción de muros 100% herméticos (4-conectados, CERO fugas diagonales)
        // Tres niveles:
        // - Planta Baja: cy + 0 (suelo) a cy + 5 (techo a cy + 6)
        // - Planta Alta: cy + 6 (suelo) a cy + 11 (techo a cy + 12)
        // - Azotea Observatorio: cy + 12 (cubierta abierta al cielo)
        for (int y = 0; y <= 12; y++) {
            int py = cy + y;
            boolean isFloor1 = (y == 0);
            boolean isFloor2 = (y == 6);
            boolean isRoof = (y == 12);

            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos p = new BlockPos(cx + x, py, cz + z);

                    if (isStargazerWall(x, z)) {
                        // Gran portal de entrada arqueado en Planta Baja (Pared Sur, z = 6, x = -1 y 0)
                        if (z == 6 && (x == -1 || x == 0) && (y >= 1 && y <= 3)) {
                            if (y == 3) {
                                Direction stairDir = (x == -1) ? Direction.WEST : Direction.EAST;
                                level.setBlock(p, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                                        .setValue(StairBlock.FACING, stairDir)
                                        .setValue(StairBlock.HALF, Half.TOP), 2);
                            } else {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                        // Ventanales con barrotes de hierro en Planta Baja (y = 2 y 3)
                        else if ((y == 2 || y == 3) && ((z == -6 && (x == 0 || x == 1)) || (x == -6 && (z == 0 || z == 1)))) {
                            level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 2);
                        }
                        // Ventanales con barrotes de hierro en Planta Alta (y = 8 y 9)
                        else if ((y == 8 || y == 9) && ((z == 6 && (x == 0 || x == 1)) || (x == 6 && (z == 0 || z == 1)))) {
                            level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 2);
                        }
                        // Muros macizos de cantería ancestral envejecida
                        else {
                            int pick = (Math.abs(x) * 7 + Math.abs(z) * 13 + y * 3) % 5;
                            BlockState st = (pick == 0) ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() :
                                    (pick == 1) ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() :
                                    (pick == 2) ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() :
                                            Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                            level.setBlock(p, st, 2);
                        }
                    } else if (isStargazerInside(x, z)) {
                        // Suelos espaciosos con aperturas limpias para escaleras
                        if (isFloor1 || isFloor2 || isRoof) {
                            // Hueco de escalera 1 en Planta Alta (y = 6): tramo Este (x = 3..4, z = -3..2)
                            if (isFloor2 && (x == 3 || x == 4) && (z >= -3 && z <= 2)) {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                                continue;
                            }
                            // Hueco de escalera 2 en Azotea (y = 12): tramo Oeste (x = -4..-3, z = -3..2)
                            if (isRoof && (x == -4 || x == -3) && (z >= -3 && z <= 2)) {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                                continue;
                            }

                            // Rosa de los vientos con luz central en planta baja
                            if (isFloor1 && x == 0 && z == 0) {
                                level.setBlock(p, Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
                                continue;
                            }

                            BlockState floorSt = ((x + z) % 2 == 0)
                                    ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                                    : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                            level.setBlock(p, floorSt, 2);
                        }
                    }
                }
            }
        }

        // 3. Escalera Tramo 1: Planta Baja (cy + 0) -> Planta Alta (cy + 6)
        // Ubicada junto al Muro Este (x = 3 y x = 4), sube recta hacia el Sur (6 escalones exactos de 2 bloques de ancho)
        // El 6º escalón (i = 5, sz = cz + 2, y = cy + 6) conecta a ras perfecto con el suelo de Planta Alta (como en media_1789603719069.png)
        for (int i = 0; i <= 5; i++) {
            int stepY = cy + 1 + i; // y = 1, 2, 3, 4, 5, 6
            int sz = cz - 3 + i;    // z = -3, -2, -1, 0, 1, 2

            for (int dx = 3; dx <= 4; dx++) {
                int sx = cx + dx;
                // Soporte sólido de pizarra únicamente por debajo del peldaño
                for (int underY = cy + 1; underY < stepY; underY++) {
                    level.setBlock(new BlockPos(sx, underY, sz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                // Peldaño de abeto orientado hacia el Sur
                level.setBlock(new BlockPos(sx, stepY, sz),
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
                // Despeje vertical de aire garantizado sobre la cabeza (hasta el techo de Planta Alta)
                for (int clearY = stepY + 1; clearY <= cy + 11; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, sz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        // Desembarco de Planta Alta en z = 3 (altura cy + 6, suelo continuo de tablones de abeto como en media_1789603719069.png)
        for (int dx = 3; dx <= 4; dx++) {
            int sx = cx + dx;
            for (int underY = cy + 1; underY < cy + 6; underY++) {
                level.setBlock(new BlockPos(sx, underY, cz + 3), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(sx, cy + 6, cz + 3), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            // Despeje de aire total sobre el desembarco y espacio frontal (z = 3, 4, 5)
            for (int fz = 3; fz <= 5; fz++) {
                for (int clearY = cy + 7; clearY <= cy + 11; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, cz + fz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // 4. Escalera Tramo 2: Planta Alta (cy + 6) -> Azotea Observatorio (cy + 12)
        // Ubicada junto al Muro Oeste (x = -4 y x = -3), sube recta hacia el Norte (6 escalones exactos de 2 bloques de ancho)
        // El 6º escalón (i = 5, sz = cz - 3, y = cy + 12) conecta a ras perfecto con la cubierta de la Azotea (como en media_1789603710941.png)
        for (int i = 0; i <= 5; i++) {
            int stepY = cy + 7 + i; // y = 7, 8, 9, 10, 11, 12
            int sz = cz + 2 - i;    // z = 2, 1, 0, -1, -2, -3

            for (int dx = -4; dx <= -3; dx++) {
                int sx = cx + dx;
                for (int underY = cy + 6; underY < stepY; underY++) {
                    level.setBlock(new BlockPos(sx, underY, sz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                // Peldaño de abeto orientado hacia el Norte
                level.setBlock(new BlockPos(sx, stepY, sz),
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                // Despeje de aire abierto al cielo
                for (int clearY = stepY + 1; clearY <= cy + 18; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, sz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        // Desembarco de la Azotea en z = -4 (altura cy + 12, suelo continuo de tablones de abeto como en media_1789603710941.png)
        for (int dx = -4; dx <= -3; dx++) {
            int sx = cx + dx;
            for (int underY = cy + 6; underY < cy + 12; underY++) {
                level.setBlock(new BlockPos(sx, underY, cz - 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(sx, cy + 12, cz - 4), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            // Despeje de aire total sobre el desembarco y espacio frontal hacia el norte (z = -4, -5)
            for (int fz = -5; fz <= -4; fz++) {
                for (int clearY = cy + 13; clearY <= cy + 18; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, cz + fz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // 5. Parapeto y almenas en la Azotea (cy + 13 y cy + 14)
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                if (isStargazerWall(x, z)) {
                    BlockPos p13 = new BlockPos(cx + x, cy + 13, cz + z);
                    level.setBlock(p13, Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);

                    BlockPos p14 = new BlockPos(cx + x, cy + 14, cz + z);
                    boolean merlon = (Math.abs(x) + Math.abs(z)) % 2 == 0;
                    level.setBlock(p14, merlon ? Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);

                    if (merlon && (Math.abs(x) == 6 || Math.abs(z) == 6)) {
                        level.setBlock(p14.above(), Blocks.LANTERN.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 6. Mobiliario y detalles interiores amplios (más de 85 bloques de superficie libre)
        // --- PLANTA BAJA: Gran Biblioteca y Salón de Cartografía ---
        // Gran Biblioteca en Pared Oeste (x = -5):
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 1), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 1), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 0), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz + 0), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 1), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 2), Blocks.BARREL.defaultBlockState(), 2);

        // Laboratorio Alquímico en Pared Norte (z = -5):
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz - 5), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 5), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 0, cy + 1, cz - 5), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 0, cy + 2, cz - 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 5), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        // Alfombra roja en el pasillo de entrada
        for (int z = 1; z <= 5; z++) {
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.RED_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + z), Blocks.RED_CARPET.defaultBlockState(), 2);
        }

        // Lámpara central colgante en Planta Baja
        level.setBlock(new BlockPos(cx, cy + 5, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // --- PLANTA ALTA: Santuario Celestial y Altar del Tomo II de Lore ---
        // Gran Altar Astral en la Pared Norte (z = -4 y -5):
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 7, cz - 5), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy + 7, cz - 4), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        BlockPos astroChest = new BlockPos(cx, cy + 8, cz - 4);
        level.setBlock(astroChest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        fillStargazerChest(level, astroChest);

        // Pedestales con Faroles de Almas y Cristales de Amatista flanqueando el cofre
        level.setBlock(new BlockPos(cx - 2, cy + 7, cz - 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 8, cz - 4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 7, cz - 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 8, cz - 4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 7, cz - 4), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 7, cz - 4), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        // Zona de estudio sur en Planta Alta
        level.setBlock(new BlockPos(cx - 2, cy + 7, cz + 5), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 7, cz + 5), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 8, cz + 5), Blocks.LANTERN.defaultBlockState(), 2);

        // Lámpara central colgante en Planta Alta
        level.setBlock(new BlockPos(cx, cy + 11, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz),
                Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // --- AZOTEA OBSERVATORIO: Gran Telescopio Celestial al Aire Libre ---
        BlockPos scopeBase = new BlockPos(cx, cy + 13, cz);
        level.setBlock(scopeBase, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(scopeBase.above(), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Mesa de cartas celestes
        BlockPos deskPos = new BlockPos(cx - 1, cy + 13, cz);
        level.setBlock(deskPos, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(deskPos.above(), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        // Asiento de tronco de observación junto al telescopio
        if (SystemBlocks.CAMPFIRE_LOG_SEAT.isBound()) {
            level.setBlock(new BlockPos(cx + 1, cy + 13, cz),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.WEST), 2);
        }
    }

    private static boolean isStargazerWall(int x, int z) {
        int ax = Math.abs(x);
        int az = Math.abs(z);
        // Paredes planas Norte y Sur (z = ±6, x de -3 a 3)
        if (az == 6 && ax <= 3) return true;
        // Paredes planas Este y Oeste (x = ±6, z de -3 a 3)
        if (ax == 6 && az <= 3) return true;
        // Esquinas biseladas cerradas (4-conectadas, CERO fugas diagonales)
        if (ax == 4 && (az == 6 || az == 5)) return true;
        if (ax == 5 && (az == 5 || az == 4)) return true;
        if (ax == 6 && az == 4) return true;
        return false;
    }

    private static boolean isStargazerInside(int x, int z) {
        if (isStargazerWall(x, z)) return false;
        int ax = Math.abs(x);
        int az = Math.abs(z);
        if (ax > 5 || az > 5) return false;
        if (ax == 5 && az == 5) return false;
        return true;
    }



    private static void fillStargazerChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(0, new ItemStack(Items.SPYGLASS, 1));
            container.setItem(2, new ItemStack(Items.AMETHYST_SHARD, 8));

            // Libro desgastado de Lore Vol. II
            container.setItem(9, com.soybe.awakeningsystem.item.WornLoreBookItem.createVolume(2));

            container.setItem(13, new ItemStack(Items.GOLD_INGOT, 12));
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(18, new ItemStack(SystemItems.AWAKENING_COIN.get(), 40));
            }
        }
    }

    // =========================================================================
    // 27. ESTELA RÚNICA ANCESTRAL (DIMENSIÓN ISEKAI) - MONUMENTO MONOLÍTICO
    // =========================================================================
    public static void buildAncientLoreStele(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 1. Cimentación masiva y despeje (área 19x19)
        ensureFoundation(level, cx - 9, cx + 9, cz - 9, cz + 9, cy - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
        clearRoom(level, cx - 9, cx + 9, cy, cy + 16, cz - 9, cz + 9);

        // 2. Plataforma Ritual en Gradas Concéntricas (Tiered Dais)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 8.2D) continue;

                BlockPos pBase = new BlockPos(cx + dx, cy, cz + dz);

                // Nivel 0 (suelo del templo)
                if (dist > 5.5D) {
                    int pattern = (Math.abs(dx) + Math.abs(dz)) % 3;
                    BlockState st = (pattern == 0) ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() :
                            (pattern == 1) ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() :
                                    Blocks.POLISHED_BLACKSTONE.defaultBlockState();
                    level.setBlock(pBase, st, 2);
                }
                // Nivel 1 elevado (grada intermedia: cy + 1 para dist <= 5.5)
                else if (dist > 2.8D) {
                    level.setBlock(pBase, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    BlockPos pUp1 = pBase.above();
                    BlockState ringSt = (dist > 4.2D)
                            ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                            : Blocks.GILDED_BLACKSTONE.defaultBlockState();
                    level.setBlock(pUp1, ringSt, 2);
                }
                // Nivel 2 elevado (sanctum central: cy + 2 para dist <= 2.8)
                else {
                    level.setBlock(pBase, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(pBase.above(1), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    BlockPos pUp2 = pBase.above(2);
                    BlockState coreRing = (dist < 1.4D)
                            ? Blocks.GOLD_BLOCK.defaultBlockState()
                            : Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    level.setBlock(pUp2, coreRing, 2);
                }
            }
        }

        // Vías ceremoniales de acceso al santuario en los 4 puntos cardinales con bloques sólidos
        for (int i = 3; i <= 5; i++) {
            level.setBlock(new BlockPos(cx, cy, cz + i), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 1, cz + i), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);

            level.setBlock(new BlockPos(cx, cy, cz - i), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 1, cz - i), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);

            level.setBlock(new BlockPos(cx + i, cy, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + i, cy + 1, cz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);

            level.setBlock(new BlockPos(cx - i, cy, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - i, cy + 1, cz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy, cz + 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz - 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy, cz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy, cz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);

        // 3. Cuatro Grandes Pilares Guardianes de Fuego de Almas (en las 4 esquinas: cx ± 5, cz ± 5)
        int[][] cornerPillars = { {5, 5}, {-5, 5}, {5, -5}, {-5, -5} };
        for (int[] p : cornerPillars) {
            int px = cx + p[0];
            int pz = cz + p[1];

            // Base del pilar (cy+1 a cy+4)
            for (int h = 1; h <= 4; h++) {
                level.setBlock(new BlockPos(px, cy + h, pz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
            // Capitel con piedra negra cincelada
            level.setBlock(new BlockPos(px, cy + 5, pz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
            // Brasero de Fuego de Almas en la cima
            level.setBlock(new BlockPos(px, cy + 6, pz), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);

            // Muros protectores a los lados
            level.setBlock(new BlockPos(px, cy + 1, pz - 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 1, pz + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        }

        // 4. Gran Obelisco Monolítico Central (11 bloques de altura, cy+2 a cy+11)
        // Refuerzos de base sólida en cy+3
        level.setBlock(new BlockPos(cx - 1, cy + 3, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 3, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz - 1), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 1), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);

        // Fuste del obelisco (cy+3 a cy+9)
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // Núcleo Arcano Luminoso (Nexo dimensional cargado)
        level.setBlock(new BlockPos(cx, cy + 6, cz),
                Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 2);

        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);

        // Cúspide del obelisco: Bloque de Oro + Vara del End emanando partículas celestiales
        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 11, cz), Blocks.END_ROD.defaultBlockState(), 2);

        // Runas de obsidiana llorosa orbitando a cy+6
        level.setBlock(new BlockPos(cx - 2, cy + 6, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 6, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz - 2), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz + 2), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        // 5. Altar de Ofrendas y Cofre de Reliquias Ancestrales (Lado Sur: cx, cy+2, cz - 3)
        BlockPos altarBase = new BlockPos(cx, cy + 2, cz - 3);
        level.setBlock(altarBase, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy + 3, cz - 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        fillSteleChest(level, chestPos);

        // Antorchas de fuego de almas flanqueando el altar
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 3), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 3), Blocks.SOUL_TORCH.defaultBlockState(), 2);
    }

    private static void fillSteleChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(0, new ItemStack(Items.GHAST_TEAR, 2));
            container.setItem(4, new ItemStack(Items.CRYING_OBSIDIAN, 4));
            container.setItem(8, new ItemStack(Items.GOLD_BLOCK, 2));

            // Libro desgastado de Lore Vol. III
            container.setItem(9, com.soybe.awakeningsystem.item.WornLoreBookItem.createVolume(3));

            if (SystemItems.ROULETTE_TOKEN.isBound()) {
                container.setItem(13, new ItemStack(SystemItems.ROULETTE_TOKEN.get(), 2));
            }
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(18, new ItemStack(SystemItems.AWAKENING_COIN.get(), 64));
            }
        }
    }

    public static void spawnMountHuaDiscipleIfAbsent(ServerLevel level, BlockPos pos) {
        level.getChunk(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);

        AABB box = new AABB(pos).inflate(16.0D);
        List<com.soybe.awakeningsystem.entity.MountHuaDiscipleEntity> existing =
                level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.MountHuaDiscipleEntity.class, box);
        if (!existing.isEmpty()) return;

        com.soybe.awakeningsystem.entity.MountHuaDiscipleEntity disciple =
                com.soybe.awakeningsystem.entity.ModEntities.MOUNT_HUA_DISCIPLE.get().create(level);
        if (disciple != null) {
            disciple.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
            disciple.setPersistenceRequired();
            level.addFreshEntity(disciple);
        }
    }

    public static void buildAncientAlchemyPavilion(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 7, cx + 7, cz - 7, cz + 7, cy - 1, Blocks.POLISHED_DEEPSLATE);
        clearRoom(level, cx - 7, cx + 7, cy, cy + 14, cz - 7, cz + 7);

        // 1. Plataforma de losetas de pizarra y mosaico de bambú
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                    level.setBlock(p, getOutwardStair(Blocks.DEEPSLATE_TILE_STAIRS, x, z, 6, 6), 2);
                } else if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 2 && Math.abs(z) <= 2) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.BAMBOO_PLANKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.BAMBOO_MOSAIC.defaultBlockState(), 2);
                }
            }
        }

        // 2. Columnas de bambú pulido
        int[][] pillars = new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] pil : pillars) {
            for (int y = 2; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            }
        }

        // Barandilla de bambú pelado con entrada abierta al frente (z = -4, |x| <= 1)
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if ((Math.abs(x) == 4 || Math.abs(z) == 4) && !(z == -4 && Math.abs(x) <= 1)) {
                    BlockPos fp = new BlockPos(cx + x, cy + 2, cz + z);
                    level.setBlock(fp, Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        // 3. Tejado pagoda nivel 1 (losetas de pizarra oscura con alero a radio 5)
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 6, cz + z);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    level.setBlock(p, getOutwardStair(Blocks.DEEPSLATE_TILE_STAIRS, x, z, 5, 5), 2);
                } else {
                    level.setBlock(p, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }
        }

        // Farolillos colgantes en las esquinas del alero inferior
        int[][] eavesCorners = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] c : eavesCorners) {
            BlockPos lanternPos = new BlockPos(cx + c[0], cy + 5, cz + c[1]);
            level.setBlock(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        // 4. Segundo Nivel / Transición de la Pagoda (cy + 7) - Conecta sólidamente sin huecos flotantes
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 7, cz + z);
                if ((Math.abs(x) == 2 && Math.abs(z) == 2)) {
                    level.setBlock(p, Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
                } else if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }
        }

        // 5. Tejado superior de la pagoda (cy + 8, radio 3 con cobre y losetas)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 8, cz + z);
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    level.setBlock(p, getOutwardStair(Blocks.DEEPSLATE_TILE_STAIRS, x, z, 3, 3), 2);
                } else {
                    level.setBlock(p, Blocks.COPPER_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        // Cúspide y remate tradicional de la pagoda
        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Farolillo interior central
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // 6. Centro Alquímico: Caldero sobre fuego de alma
        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.NETHERRACK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.SOUL_FIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.CAULDRON.defaultBlockState(), 2);

        // Estantes con manuales y destilador
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 3, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 2), Blocks.BREWING_STAND.defaultBlockState(), 2);

        // Cofre con ingredientes de alquimia taoísta
        BlockPos chestPos = new BlockPos(cx + 2, cy + 2, cz - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 2));
            chest.setItem(1, SystemItems.coin(180));
            chest.setItem(2, new ItemStack(Items.EMERALD, 6));
            chest.setItem(3, new ItemStack(Items.BLAZE_POWDER, 4));
            chest.setItem(4, new ItemStack(Items.NETHER_WART, 5));
            chest.setItem(5, new ItemStack(Items.GOLDEN_CARROT, 3));
            chest.setItem(6, new ItemStack(Items.BREWING_STAND, 1));
        }

        // Discípulo guardián alquimista
        spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx - 1, cy + 2, cz - 1));
    }

    public static void buildTaoistBambooShrine(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy - 1, Blocks.MOSSY_STONE_BRICKS);
        clearRoom(level, cx - 6, cx + 6, cy, cy + 12, cz - 6, cz + 6);

        // 1. Suelo del santuario
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    level.setBlock(p, getOutwardStair(Blocks.MOSSY_STONE_BRICK_STAIRS, x, z, 5, 5), 2);
                } else if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    level.setBlock(p, Blocks.CALCITE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.BAMBOO_MOSAIC.defaultBlockState(), 2);
                }
            }
        }

        // 2. Pilares de bambú
        int[][] pillars = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] pil : pillars) {
            for (int y = 2; y <= 5; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            }
        }

        // Vigas de soporte entre pilares
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 5, cz - 3), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 5, cz + 3), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
        }
        for (int z = -3; z <= 3; z++) {
            level.setBlock(new BlockPos(cx - 3, cy + 5, cz + z), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 3, cy + 5, cz + z), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
        }

        // Tejado (alero a radio 4 con escaleras orientadas al exterior)
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 5, cz + z);
                if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                    level.setBlock(p, getOutwardStair(Blocks.MOSSY_STONE_BRICK_STAIRS, x, z, 4, 4), 2);
                } else {
                    level.setBlock(p, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        // Cúspide espiritual con Amatista y Pararrayos místico
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        // Farolillos colgantes en las 4 esquinas del alero
        int[][] corners = new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] c : corners) {
            BlockPos lanternPos = new BlockPos(cx + c[0], cy + 4, cz + c[1]);
            level.setBlock(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        // 3. Altar Central de Meditación
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.WHITE_CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true), 2);

        // Sahumerio de alma e incienso taoísta
        level.setBlock(new BlockPos(cx, cy + 2, cz + 2), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);

        // Cofre de ofrendas taoístas
        BlockPos chestPos = new BlockPos(cx - 2, cy + 2, cz - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 2));
            chest.setItem(1, SystemItems.coin(120));
            chest.setItem(2, new ItemStack(Items.GOLDEN_APPLE, 1));
            chest.setItem(3, new ItemStack(Items.BAMBOO, 8));
        }

        // Discípulo taoísta meditando
        spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx + 1, cy + 2, cz + 1));
    }
}
