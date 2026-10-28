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
import com.soybe.awakeningsystem.block.TavernChairBlock;
import com.soybe.awakeningsystem.block.CasinoChairBlock;
import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.block.ClandestineCashierBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTablePart;
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
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.Level;
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
                for (int y = ySurface; y >= ySurface - 2; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.getFluidState().isEmpty()) {
                        break;
                    }
                    if (s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS) && !s.is(BlockTags.REPLACEABLE)) {
                        break;
                    }
                    BlockState placeState = (y == ySurface) ? block.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                    level.setBlock(p, placeState, 2);
                }
            }
        }
    }

    public static void clearRoom(ServerLevel level, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!level.getBlockState(p).getFluidState().isEmpty()) {
                        continue; // Protect water and liquids
                    }
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
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

        if (!level.getBlockState(pos.below()).isSolid()) {
            level.setBlock(pos.below(), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }

        AABB box = new AABB(pos).inflate(6.0D);
        List<SeriesNpcEntity> nearby = level.getEntitiesOfClass(SeriesNpcEntity.class, box, e -> e.isAlive() && !e.isRemoved() && e.getNpcProfile() == profile);
        if (nearby.isEmpty()) {
            SeriesNpcEntity npc = new SeriesNpcEntity(type, level, profile);
            npc.moveTo(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D, 0.0F, 0.0F);
            npc.setHomePos(pos);
            npc.setPersistenceRequired();
            level.addFreshEntityWithPassengers(npc);
        } else {
            SeriesNpcEntity existing = nearby.get(0);
            if (existing.getY() < pos.getY() - 0.5D) {
                existing.teleportTo(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D);
                existing.setHomePos(pos);
            }
        }
    }

    public static ItemStack makeRelic(Item item, int count, String name, String series, String description) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("\u00A77" + description),
                Component.translatable("item.awakening_system.relic.tag", series)
        )));
        return stack;
    }

    public static ItemStack makeLoreBook(String title, String author, List<String> pages, String series, String description) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(title));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("§7" + description),
                Component.translatable("item.awakening_system.lore_book.tag", series)
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

    public static void buildHunterOffice(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.POLISHED_DEEPSLATE);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 11, cz - 8, cz + 8);

        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 1 && z >= -7 && z <= 2) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

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

        level.setBlock(new BlockPos(cx - 2, cy + 1, cz + 5), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz + 5), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        for (int y = 1; y <= 6; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        if (z == -8 && Math.abs(x) <= 1 && y <= 3) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        if (Math.abs(x) == 8 && Math.abs(z) == 8) {
                            level.setBlock(p, Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
                        } else if (z == -8 && Math.abs(x) == 2) {
                            level.setBlock(p, Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
                        } else if (Math.abs(x) == 8 && z == 0) {
                            level.setBlock(p, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
                        } else if (y >= 2 && y <= 5) {
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

        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 4, cz - 9), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy + 3, cz - 9), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 9), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 3, cz - 9), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 9), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(cx + x, cy, cz - 9), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState(), 2);
        }

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

        level.setBlock(new BlockPos(cx - 7, cy + 8, cz - 7), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 9, cz - 7), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 8, cz + 7), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 9, cz + 7), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        int[][] lights = new int[][]{{-5, -4}, {5, -4}, {-5, 4}, {5, 4}};
        for (int[] lt : lights) {
            level.setBlock(new BlockPos(cx + lt[0], cy + 7, cz + lt[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        for (int x = -6; x <= -3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz - 5), Blocks.DARK_OAK_WOOD.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz - 4), Blocks.DARK_OAK_WOOD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 5), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 5), Blocks.BLACK_CONCRETE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz - 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 5), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 2, cz - 5), Blocks.TARGET.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 5), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 4), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 5), Blocks.ANVIL.defaultBlockState(), 2);

        BlockPos throneBase = new BlockPos(cx, cy + 1, cz + 6);
        level.setBlock(throneBase, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(throneBase.above(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz + 7), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 7), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz + 7), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 7), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 7), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy + 6, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 5, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 4, cz + 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 6, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 5, cz + 5), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 4, cz + 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx + 6, cy + 1, cz + 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.DIAMOND_SWORD, 1, "\u00A75\u00A7lRasaka's Poison Fang", "Solo Leveling", "Dagger forged from the paralyzing venom of the great dungeon ruler."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7d\u00A7lHunter Dossier: Ahjin Guild", "Solo Leveling", "Classified official record of the Korean Hunters Association."));
            chest.setItem(2, SystemItems.coin(600));
            chest.setItem(3, new ItemStack(Items.GOLDEN_CARROT, 16));
            chest.setItem(4, new ItemStack(Items.ENDER_PEARL, 8));
            chest.setItem(5, new ItemStack(Items.POTION));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.SUNG_JINWOO.get(), NpcProfile.SUNG_JINWOO);
        spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz - 3), ModEntities.CHA_HAEIN.get(), NpcProfile.CHA_HAEIN);
    }

    public static void buildSubwayCarriage(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 10, cz + 10, cy - 2, Blocks.STONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy - 1, cy + 10, cz - 10, cz + 10);

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

        for (int z : new int[]{-7, 0, 7}) {
            level.setBlock(new BlockPos(cx - 7, cy + 6, cz + z), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 7, cy + 5, cz + z), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
            level.setBlock(new BlockPos(cx + 7, cy + 6, cz + z), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 7, cy + 5, cz + z), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }
        level.setBlock(new BlockPos(cx - 7, cy + 5, cz - 9), Blocks.COBWEB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 5, cz + 9), Blocks.COBWEB.defaultBlockState(), 2);

        for (int z = -10; z <= 10; z++) {
            for (int x = -7; x <= -3; x++) {
                level.setBlock(new BlockPos(cx + x, cy - 1, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                BlockPos p = new BlockPos(cx + x, cy + 1, cz + z);
                if (x == -3) {
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

        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 5), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 4), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 4), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 5), Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

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

        for (int z = -10; z <= 10; z++) {
            level.setBlock(new BlockPos(cx - 2, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.LEFT_RAIL), 2);
            level.setBlock(new BlockPos(cx - 1, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.TIE_1), 2);
            level.setBlock(new BlockPos(cx, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.TIE_CENTER), 2);
            level.setBlock(new BlockPos(cx + 1, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.TIE_2), 2);
            level.setBlock(new BlockPos(cx + 2, cy, cz + z), SystemBlocks.SUBWAY_WIDE_RAIL_TRACK.get().defaultBlockState().setValue(SubwayWideRailTrackBlock.SHAPE, SubwayWideRailTrackBlock.WideTrackShape.NORTH_SOUTH).setValue(SubwayWideRailTrackBlock.SECTION, SubwayWideRailTrackBlock.TrackSection.RIGHT_RAIL), 2);

            BlockPos pRailOpposite = new BlockPos(cx + 5, cy, cz + z);
            level.setBlock(pRailOpposite, SystemBlocks.SUBWAY_RAIL_TRACK.get().defaultBlockState().setValue(RailBlock.SHAPE, net.minecraft.world.level.block.state.properties.RailShape.NORTH_SOUTH), 2);
        }

        level.setBlock(new BlockPos(cx + 6, cy, cz - 8), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 8), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 2, cz - 8), Blocks.REDSTONE_TORCH.defaultBlockState(), 2);

        for (int z = -8; z <= 8; z++) {
            for (int x = -2; x <= 3; x++) {
                level.setBlock(new BlockPos(cx + x, cy + 1, cz + z), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        int[] bogieZ = new int[]{-6, -5, 5, 6};
        for (int bz : bogieZ) {
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + bz), SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, Direction.NORTH).setValue(SubwayTrainWheelBlock.TYPE, SubwayTrainWheelBlock.WheelType.LEFT), 2);
            level.setBlock(new BlockPos(cx, cy + 1, cz + bz), SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, Direction.NORTH).setValue(SubwayTrainWheelBlock.TYPE, SubwayTrainWheelBlock.WheelType.AXLE), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 1, cz + bz), SystemBlocks.SUBWAY_TRAIN_WHEEL.get().defaultBlockState().setValue(SubwayTrainWheelBlock.FACING, Direction.NORTH).setValue(SubwayTrainWheelBlock.TYPE, SubwayTrainWheelBlock.WheelType.RIGHT), 2);
        }

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

        for (int y = 3; y <= 5; y++) {
            for (int z = -8; z <= 8; z++) {
                for (int x = -2; x <= 3; x++) {
                    if (x == -2 || x == 3 || Math.abs(z) == 8) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        if (x == -2 && ((z >= -4 && z <= -3) || (z >= 3 && z <= 4)) && y <= 4) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        if (Math.abs(z) == 8 && (x == 0 || x == 1) && y <= 4) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

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

        for (int z = -5; z <= 5; z += 2) {
            level.setBlock(new BlockPos(cx, cy + 5, cz + z), SystemBlocks.SUBWAY_GRAB_HANDLE.get().defaultBlockState().setValue(SubwayGrabHandleBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 5, cz + z), SystemBlocks.SUBWAY_GRAB_HANDLE.get().defaultBlockState().setValue(SubwayGrabHandleBlock.FACING, Direction.NORTH), 2);
        }

        for (int z = -6; z <= 6; z++) {
            if (!(z >= -4 && z <= -3) && !(z >= 3 && z <= 4)) {
                level.setBlock(new BlockPos(cx - 1, cy + 3, cz + z), Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            }
            level.setBlock(new BlockPos(cx + 2, cy + 3, cz + z), Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 5, cz - 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.SOUTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.LEFT), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 5, cz - 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.SOUTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.RIGHT), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz + 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.NORTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.RIGHT), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 5, cz + 6), SystemBlocks.SUBWAY_DIGITAL_SCREEN.get().defaultBlockState().setValue(SubwayDigitalScreenBlock.FACING, Direction.NORTH).setValue(SubwayDigitalScreenBlock.TYPE, SubwayDigitalScreenBlock.ScreenType.LEFT), 2);

        BlockPos barrelPos = new BlockPos(cx + 2, cy + 3, cz + 6);
        level.setBlock(barrelPos, Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(barrelPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(barrelPos);
        if (be instanceof Container barrel) {
            barrel.setItem(0, makeRelic(Items.IRON_SWORD, 1, "\u00A7f\u00A7lUnbroken Faith", "ORV", "Sword wielded by Kim Dokja to rewrite the scenarios of the Star Stream."));
            barrel.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7e\u00A7lSurvival Guide: Scenario 1", "ORV", "Chapter 1: Three ways to survive in a ruined world."));
            barrel.setItem(2, SystemItems.coin(750));
            barrel.setItem(3, new ItemStack(Items.GOLDEN_APPLE, 3));
            barrel.setItem(4, new ItemStack(Items.BREAD, 16));
            barrel.setItem(5, new ItemStack(Items.TORCH, 16));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 4, cz - 2), ModEntities.KIM_DOKJA.get(), NpcProfile.KIM_DOKJA);
        spawnNpcIfAbsent(level, new BlockPos(cx + 1, cy + 4, cz + 2), ModEntities.YOO_JOONGHYUK.get(), NpcProfile.YOO_JOONGHYUK);
    }

    public static void buildFronteraEstate(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.STONE_BRICKS);
        // Only clear interior room air - strictly avoid cutting external trees or hillsides
        clearRoom(level, cx - 7, cx + 7, cy + 1, cy + 5, cz - 7, cz + 7);

        // Floor (cy)
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 1 && z >= -7 && z <= 3) {
                    level.setBlock(p, Blocks.RED_WOOL.defaultBlockState(), 2);
                } else if ((Math.abs(x) == 2 && z >= -7 && z <= 3) || (Math.abs(x) <= 2 && z == 4)) {
                    level.setBlock(p, Blocks.YELLOW_WOOL.defaultBlockState(), 2);
                } else if (x >= 4 && z >= 4) {
                    level.setBlock(p, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
                }
            }
        }

        // Walls (cy + 1 to cy + 5)
        for (int y = 1; y <= 5; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        if (z == -8 && (x == 0 || x == 1) && y <= 2) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        boolean isPillar = (Math.abs(x) == 8 && Math.abs(z) == 8) ||
                                           (Math.abs(x) == 8 && z == 0) ||
                                           (Math.abs(z) == 8 && (x == -4 || x == 4));

                        if (isPillar) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        } else if (y == 1) {
                            level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                        } else if (y == 5) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Math.abs(x) == 8 ? Direction.Axis.Z : Direction.Axis.X), 2);
                        } else if (y == 3 && (Math.abs(x) == 4 || Math.abs(z) == 4)) {
                            level.setBlock(p, Blocks.GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.CALCITE.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        // Front doors
        level.setBlock(new BlockPos(cx, cy + 1, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.HINGE, net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.HINGE, net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT), 2);

        // Lanterns beside front door on the building wall
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 8), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 8), Blocks.LANTERN.defaultBlockState(), 2);

        // Clear interior attic room air under roof
        for (int step = 1; step <= 8; step++) {
            int yRoof = cy + 5 + step;
            int xLeft = cx - 9 + step;
            int xRight = cx + 9 - step;
            for (int z = cz - 7; z <= cz + 7; z++) {
                for (int x = xLeft + 1; x < xRight; x++) {
                    level.setBlock(new BlockPos(x, yRoof, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // Complete A-frame Dark Oak Gable Roof (with front and back eaves overhang)
        for (int z = -9; z <= 9; z++) {
            int rz = cz + z;
            // Eaves at step 0 (x = cx - 9 and cx + 9 at y = cy + 5)
            level.setBlock(new BlockPos(cx - 9, cy + 5, rz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            level.setBlock(new BlockPos(cx + 9, cy + 5, rz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
            level.setBlock(new BlockPos(cx - 9, cy + 4, rz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.TOP), 2);
            level.setBlock(new BlockPos(cx + 9, cy + 4, rz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.TOP), 2);

            for (int step = 1; step <= 8; step++) {
                int yRoof = cy + 5 + step;
                int xLeft = cx - 9 + step;
                int xRight = cx + 9 - step;

                level.setBlock(new BlockPos(xLeft, yRoof, rz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                level.setBlock(new BlockPos(xRight, yRoof, rz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

                // Gable ends at front (cz - 8) and back (cz + 8)
                if (z == -8 || z == 8) {
                    for (int fillX = xLeft + 1; fillX < xRight; fillX++) {
                        BlockPos fillPos = new BlockPos(fillX, yRoof, rz);
                        if ((fillX == cx || fillX == cx + 1) && (yRoof == cy + 8 || yRoof == cy + 9)) {
                            level.setBlock(fillPos, Blocks.GLASS_PANE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(fillPos, Blocks.WHITE_TERRACOTTA.defaultBlockState(), 2);
                        }
                    }
                }
            }

            // Ridge line (step 9: peak ridge at x = cx, yRoof = cy + 14)
            level.setBlock(new BlockPos(cx, cy + 13, rz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 14, rz), Blocks.DARK_OAK_SLAB.defaultBlockState(), 2);
        }

        // Interior Roof Trusses
        for (int trussZ : new int[]{-4, 0, 4}) {
            for (int x = -7; x <= 7; x++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + trussZ), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 2);
            }
            for (int yf = 6; yf <= 12; yf++) {
                level.setBlock(new BlockPos(cx, cy + yf, cz + trussZ), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            }
        }

        // Central Chandelier
        level.setBlock(new BlockPos(cx, cy + 8, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        // Fireplace & Chimney
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
        level.setBlock(new BlockPos(cx + 7, cy + 12, cz + 5), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 12, cz + 6), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 7, cy + 12, cz + 6), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);

        // Lloyd's Architect Office
        BlockPos draftingTablePos = new BlockPos(cx - 5, cy + 1, cz + 4);
        level.setBlock(draftingTablePos, SystemBlocks.ARCHITECT_DRAFTING_TABLE.get().defaultBlockState().setValue(ArchitectDraftingTableBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 3), SystemBlocks.TAVERN_CHAIR.get().defaultBlockState().setValue(TavernChairBlock.FACING, Direction.NORTH), 2);

        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 2), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 2, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 3), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 2, cz + 3), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 4), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz + 5), Blocks.STONECUTTER.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 6, cy + 1, cz + 6), Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 6), Blocks.RAW_IRON_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz + 6), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx - 4, cy + 1, cz + 5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.DIAMOND_SHOVEL, 1, "\u00A76\u00A7lLloyd Frontera's Golden Shovel", "Estate Developer", "Water flows downward and money flows into my pocket!"));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7e\u00A7lFrontera Aqueduct Blueprints", "Estate Developer", "Hydrostatic calculations and civil engineering design certified by Lloyd Frontera."));
            chest.setItem(2, SystemItems.coin(500));
            chest.setItem(3, new ItemStack(Items.IRON_PICKAXE));
            chest.setItem(4, new ItemStack(Items.BRICK, 32));
            chest.setItem(5, new ItemStack(Items.BREAD, 16));
        }

        // Javier's Training Area
        level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 4), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 2, cz - 4), Blocks.TARGET.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 3), Blocks.GRINDSTONE.defaultBlockState().setValue(net.minecraft.world.level.block.GrindstoneBlock.FACING, Direction.WEST), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 5), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 4), Blocks.ANVIL.defaultBlockState(), 2);

        // Corner natural greenery bushes (persistent oak leaves)
        int[][] cornerBushes = {{-9, -8}, {9, -8}, {-9, 8}, {9, 8}, {-8, -9}, {8, -9}, {-8, 9}, {8, 9}};
        for (int[] cb : cornerBushes) {
            BlockPos bPos = new BlockPos(cx + cb[0], cy + 1, cz + cb[1]);
            if (level.getBlockState(bPos).isAir()) {
                level.setBlock(bPos, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2);
            }
        }

        spawnNpcIfAbsent(level, new BlockPos(cx - 4, cy + 1, cz + 3), ModEntities.LLOYD_FRONTERA.get(), NpcProfile.LLOYD_FRONTERA);
        spawnNpcIfAbsent(level, new BlockPos(cx + 4, cy + 1, cz - 3), ModEntities.JAVIER_ASRAHAN.get(), NpcProfile.JAVIER_ASRAHAN);
    }

    public static void buildDemonPagoda(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy, Blocks.POLISHED_BLACKSTONE);
        clearRoom(level, cx - 7, cx + 7, cy + 2, cy + 15, cz - 7, cz + 7);

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

        int[][] cornerLamps = new int[][]{{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] cl : cornerLamps) {
            BlockPos lp = new BlockPos(cx + cl[0], cy + 2, cz + cl[1]);
            level.setBlock(lp, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
            level.setBlock(lp.above(), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        int[][] mainPillars = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {-5, 0}, {5, 0}};
        for (int[] pil : mainPillars) {
            for (int y = 2; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.POLISHED_BASALT.defaultBlockState(), 2);
            }
        }

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

        int[][] midPillars = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] pil : midPillars) {
            for (int y = 7; y <= 10; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.POLISHED_BASALT.defaultBlockState(), 2);
            }
        }

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

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(new BlockPos(cx + x, cy + 12, cz + z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy + 13, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 14, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 15, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.BELL.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 3), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 3, cz - 3), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 4), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 2), Blocks.ANVIL.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 2, cz + 4), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 4), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 3), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 3), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        BlockPos chestPos = new BlockPos(cx + 4, cy + 2, cz + 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A74\u00A7lHeavenly Demon Sword", "Nano Machine", "Forged with the unwavering atomic precision of Nano-Machines."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7c\u00A7lDemonic Cult Manual: Shadow Step", "Nano Machine", "Definitive martial techniques inherited by Cheon Yeo-woon."));
            chest.setItem(2, SystemItems.coin(600));
            chest.setItem(3, new ItemStack(Items.GOLDEN_APPLE, 3));
            chest.setItem(4, new ItemStack(Items.POTION));
            chest.setItem(5, new ItemStack(Items.LAPIS_LAZULI, 12));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 2), ModEntities.CHEON_YEOWOON.get(), NpcProfile.CHEON_YEOWOON);
    }

    public static void buildAetherSanctuary(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.QUARTZ_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 15, cz - 8, cz + 8);

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

        for (int[] coord : pillarCoords) {
            int inX = Integer.compare(0, coord[0]);
            int inZ = Integer.compare(0, coord[1]);
            level.setBlock(new BlockPos(cx + coord[0] + inX, cy + 8, cz + coord[1] + inZ), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 4, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 4, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 4, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz - 1), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy, cz), Blocks.WATER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz - 1), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz + 1), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 3), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 3), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 2), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 4), Blocks.LECTERN.defaultBlockState(), 2);

        BlockPos altarPos = new BlockPos(cx + 4, cy + 1, cz - 3);
        level.setBlock(altarPos, Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 2);
        level.setBlock(altarPos.above(), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz - 3), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 3), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy + 1, cz + 4);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, new ItemStack(SystemItems.DEATH_KING_CLOCK.get()));
            chest.setItem(1, makeRelic(Items.DIAMOND_SWORD, 1, "\u00A79\u00A7lDawn's Ballad", "TBATE", "Translucent sword capable of channeling the purest aether in the realm."));
            chest.setItem(2, makeRelic(Items.BOOK, 1, "\u00A7b\u00A7lTreatise on Aether and Dicathen", "TBATE", "Writings by Arthur Leywin on the nature of Djinn runes."));
            chest.setItem(3, SystemItems.coin(600));
            chest.setItem(4, new ItemStack(Items.ENDER_PEARL, 8));
            chest.setItem(5, new ItemStack(Items.DIAMOND, 3));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 2), ModEntities.ARTHUR_LEYWIN.get(), NpcProfile.ARTHUR_LEYWIN);
    }

    public static void buildRankerTower(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 18, cz - 8, cz + 8);

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

        for (int y = 2; y <= 5; y++) {
            level.setBlock(new BlockPos(cx - 4, cy + y, cz), Blocks.BONE_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 4, cy + y, cz), Blocks.BONE_BLOCK.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 6, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 6, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.BONE_BLOCK.defaultBlockState(), 2);

        int[][] braziers = new int[][]{{-4, -3}, {4, -3}};
        for (int[] bz : braziers) {
            BlockPos bp = new BlockPos(cx + bz[0], cy + 1, cz + bz[1]);
            level.setBlock(bp, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
            level.setBlock(bp.above(), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
            level.setBlock(bp.above().north(), Blocks.IRON_BARS.defaultBlockState(), 2);
            level.setBlock(bp.above().south(), Blocks.IRON_BARS.defaultBlockState(), 2);
        }

        BlockPos throneBase = new BlockPos(cx, cy + 2, cz + 4);
        level.setBlock(throneBase, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(throneBase.above(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 5), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 5), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 5), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz + 5), Blocks.WITHER_SKELETON_SKULL.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 4, cy + 2, cz + 4), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz + 3), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz + 2), Blocks.SMITHING_TABLE.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx + 4, cy + 2, cz + 4);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A7c\u00A7lVigrid - Holy Sword of Cain", "Second Life Ranker", "Ancestral blade forged to overthrow gods and dragons of the Tower."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A76\u00A7lCha Jeong-woo's Diary: For Yeon-woo", "Second Life Ranker", "By the time you hear this, I am probably already dead..."));
            chest.setItem(2, SystemItems.coin(800));
            chest.setItem(3, new ItemStack(Items.NETHERITE_SCRAP, 2));
            chest.setItem(4, new ItemStack(Items.FIRE_CHARGE, 12));
            chest.setItem(5, new ItemStack(Items.GOLDEN_APPLE, 4));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 2), ModEntities.CHA_YEONWOO.get(), NpcProfile.CHA_YEONWOO);
    }

    public static void buildMountHuaPavilion(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.STONE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 15, cz - 8, cz + 8);

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

        int[][] pavilionPillars = new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {-4, 0}, {4, 0}};
        for (int[] pil : pavilionPillars) {
            for (int y = 2; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.STRIPPED_CHERRY_LOG.defaultBlockState(), 2);
            }
        }

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if ((Math.abs(x) == 4 || Math.abs(z) == 4) && !(z == -4 && Math.abs(x) <= 1)) {
                    BlockPos fp = new BlockPos(cx + x, cy + 2, cz + z);
                    level.setBlock(fp, Blocks.CHERRY_FENCE.defaultBlockState(), 2);
                }
            }
        }

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

        int[][] leavesCorners = new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
        for (int[] lc : leavesCorners) {
            level.setBlock(new BlockPos(cx + lc[0], cy + 7, cz + lc[1]), Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 2), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz - 2), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 2, cz - 4), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 6, cy + 3, cz - 4), Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 3), Blocks.GRINDSTONE.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx + 3, cy + 2, cz + 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 3, cz + 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 3), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 2, cz + 3), Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx - 3, cy + 2, cz + 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.DIAMOND_SWORD, 1, "\u00A7a\u00A7lPlum Blossom Dragon Sword", "Mount Hua", "Taoist sword that causes sword petals to bloom with every precise strike."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A7e\u00A7lTwenty-Four Plum Blossom Sword Manual", "Mount Hua", "Authored by the Mount Hua Sword Saint, Chung Myung."));
            chest.setItem(2, SystemItems.coin(750));
            chest.setItem(3, new ItemStack(Items.CHERRY_SAPLING, 4));
            chest.setItem(4, new ItemStack(Items.POTION));
            chest.setItem(5, new ItemStack(Items.GOLDEN_APPLE, 3));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 2, cz + 1), ModEntities.CHUNG_MYUNG.get(), NpcProfile.CHUNG_MYUNG);
    }

    public static void buildValterCamp(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.MUD_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 17, cz - 8, cz + 8);

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

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x * x + z * z <= 3) {
                    level.setBlock(new BlockPos(cx + x, cy + 2, cz + z), Blocks.STONE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + x, cy + 3, cz + z), Blocks.ANDESITE.defaultBlockState(), 2);
                }
            }
        }
        level.setBlock(new BlockPos(cx, cy + 4, cz), Blocks.TUFF.defaultBlockState(), 2);

        for (int y = 3; y <= 7; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + y, cz + 1), Blocks.IRON_BLOCK.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy + 8, cz - 1), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz + 1), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz + 2), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.NETHERITE_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.NETHERITE_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 11, cz), Blocks.HEAVY_CORE.defaultBlockState(), 2);

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

        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 4), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz - 4), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 3), Blocks.SMOKER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 4), Blocks.STRIPPED_OAK_WOOD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 4), Blocks.STRIPPED_OAK_WOOD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 3, cy + 2, cz - 5), Blocks.BROWN_CARPET.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 5), Blocks.WHITE_CARPET.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx + 4, cy + 2, cz - 3), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 2, cz - 3), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 2, cz - 4), Blocks.BLAST_FURNACE.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx + 3, cy + 2, cz + 5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A7c\u00A7lValter's Colossal Greatsword", "Survival Story", "A colossal blade forged to channel pure physical momentum without limits."));
            chest.setItem(1, makeRelic(Items.BOOK, 1, "\u00A76\u00A7lSurvival Guide: Level 5 Error", "Survival Story", "Notes by Ryu Hanbin after surviving 20 years in the Rocky Mountain Dimension."));
            chest.setItem(2, SystemItems.coin(700));
            chest.setItem(3, new ItemStack(Items.COOKED_BEEF, 32));
            chest.setItem(4, new ItemStack(Items.IRON_BLOCK, 2));
            chest.setItem(5, new ItemStack(Items.POTION));
        }

        spawnNpcIfAbsent(level, new BlockPos(cx + 2, cy + 2, cz - 1), ModEntities.RYU_HANBIN.get(), NpcProfile.RYU_HANBIN);
    }

    public static void buildShinsooShrine(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.PRISMARINE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 16, cz - 8, cz + 8);

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

        int[][] susPillars = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] sp : susPillars) {
            level.setBlock(new BlockPos(cx + sp[0], cy, cz + sp[1]), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + sp[0], cy + 1, cz + sp[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

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

        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz - 1), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        int[][] lightBeacons = new int[][]{{-6, 0}, {6, 0}, {0, -6}, {0, 6}};
        for (int[] lb : lightBeacons) {
            for (int y = 3; y <= 7; y++) {
                level.setBlock(new BlockPos(cx + lb[0], cy + y, cz + lb[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + lb[0], cy + 8, cz + lb[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + lb[0], cy + 9, cz + lb[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx - 3, cy + 3, cz - 3), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 3, cz - 3), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 3, cz - 4), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx + 3, cy + 3, cz - 3), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 4, cz - 3), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy + 3, cz + 5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, makeRelic(Items.NETHERITE_SWORD, 1, "\u00A7b\u00A7lDivine Needle: Black March", "Tower of God", "One of the legendary 13 Month Series weapons granted by King Zahard."));
            chest.setItem(1, makeRelic(Items.NETHER_STAR, 1, "\u00A7c\u00A7lFragment of the Primordial Thorn", "Tower of God", "Divine artifact of an outer God capable of bending the laws of Shinsu."));
            chest.setItem(2, SystemItems.coin(800));
            chest.setItem(3, new ItemStack(Items.PRISMARINE_CRYSTALS, 16));
            chest.setItem(4, new ItemStack(Items.DIAMOND, 3));
            chest.setItem(5, new ItemStack(Items.POTION));
        }

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

        int[][] pillars = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] pOff : pillars) {
            for (int h = 2; h <= 5; h++) {
                level.setBlock(new BlockPos(cx + pOff[0], cy + h, cz + pOff[1]), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + pOff[0], cy + 6, cz + pOff[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + pOff[0], cy + 7, cz + pOff[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

        for (int sign : new int[]{-2, 2}) {
            int px = cx + sign;
            level.setBlock(new BlockPos(px, cy + 2, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 3, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 4, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 5, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 7, cz), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);

        for (int dx = -1; dx <= 1; dx++) {
            for (int h = 2; h <= 5; h++) {
                level.setBlock(new BlockPos(cx + dx, cy + h, cz), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        BlockPos lecternPos = new BlockPos(cx, cy + 2, cz - 3);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 3), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 3), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

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

        for (int step = -2; step <= 2; step++) {
            level.setBlock(new BlockPos(cx + step, cy + 1, cz - 12), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + step, cy + 1, cz + 12), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx - 12, cy + 1, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            level.setBlock(new BlockPos(cx + 12, cy + 1, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        }

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

        for (int step = -1; step <= 1; step++) {
            level.setBlock(new BlockPos(cx + step, cy + 2, cz - 8), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + step, cy + 2, cz + 8), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx - 8, cy + 2, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
            level.setBlock(new BlockPos(cx + 8, cy + 2, cz + step), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= 16) {
                    BlockPos p = new BlockPos(cx + dx, cy + 3, cz + dz);
                    level.setBlock(p, (distSq >= 9) ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(cx, cy + 3, cz - 5), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 5), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 3, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 3, cz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

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

    public static void buildRoulettePavilion(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy - 1, Blocks.COBBLESTONE);
        clearRoom(level, cx - 7, cx + 7, cy, cy + 13, cz - 7, cz + 7);

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

        for (int z = -5; z <= 2; z++) {
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + z), Blocks.YELLOW_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 1, cz + z), Blocks.YELLOW_CARPET.defaultBlockState(), 2);
        }

        for (int y = 1; y <= 4; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);

                        if (z == -5 && x == 0 && y <= 2) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            continue;
                        }

                        if (Math.abs(x) == 5 && Math.abs(z) == 5) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        }
                        else if (((Math.abs(x) == 5 && (z == -1 || z == 0)) || (z == -5 && (x == -2 || x == 2))) && (y >= 2 && y <= 3)) {
                            level.setBlock(p, Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
                        }
                        else if (y == 1) {
                            level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
                        } else {
                            level.setBlock(p, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        level.setBlock(new BlockPos(cx, cy, cz - 6), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx - 1, cy, cz - 6), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy, cz - 6), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);

        for (int z = -6; z <= 6; z++) {
            for (int step = -1; step <= 4; step++) {
                int yR = cy + 5 + step;
                int xL = cx - 5 + step;
                int xR = cx + 5 - step;

                if (z == -6 || z == 6) {
                    level.setBlock(new BlockPos(xL, yR, cz + z), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                    level.setBlock(new BlockPos(xR, yR, cz + z), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
                } else {
                    level.setBlock(new BlockPos(xL, yR, cz + z), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
                    level.setBlock(new BlockPos(xR, yR, cz + z), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
                }

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

        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 5, cz - 2), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 5, cz + 2), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 8, cz - 1), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz - 1), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz - 1), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz - 1), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        for (int y = 1; y <= 9; y++) {
            level.setBlock(new BlockPos(cx + 4, cy + y, cz + 3), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz + 3), Blocks.HAY_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 2, cz + 3), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 10, cz + 3), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        BlockPos altarPos = new BlockPos(cx, cy + 1, cz + 2);
        if (com.soybe.awakeningsystem.block.SystemBlocks.ROULETTE_ALTAR.isBound()) {
            level.setBlock(altarPos, com.soybe.awakeningsystem.block.SystemBlocks.ROULETTE_ALTAR.get().defaultBlockState().setValue(com.soybe.awakeningsystem.block.RouletteAltarBlock.FACING, Direction.NORTH), 2);
        } else {
            level.setBlock(altarPos, Blocks.LODESTONE.defaultBlockState(), 2);
        }

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

        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz - 2), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz - 1), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 1, cz + 1), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz), Blocks.CRIMSON_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        level.setBlock(new BlockPos(cx + 3, cy + 1, cz - 2), Blocks.RAW_GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 2), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 1), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx + 3, cy + 1, cz - 1);
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
            container.setItem(13, makeRelic(Items.PAPER, 1, "\u00A76\u00A7l✦ Shrine of Fortune Manual ✦",
                    "Awakening System", "Use a Roulette Token or Nether Star directly on the physical roulette to test your luck"));
        }
    }

    public static void populateRoulettePavilion(ServerLevel level, BlockPos center) {
        BlockPos chestPos = new BlockPos(center.getX() + 3, center.getY() + 1, center.getZ() - 1);
        fillRouletteChest(level, chestPos);
    }

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

        ensureFoundation(level, cx - 10, cx + 10, cz - 10, cz + 10, cy, Blocks.SMOOTH_QUARTZ);
        clearRoom(level, cx - 10, cx + 10, cy + 1, cy + 14, cz - 10, cz + 10);

        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 9.4D) continue;

                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);

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
                else if (dist >= 3.2D) {
                    BlockPos bottomPos = p.below();
                    if ((Math.abs(dx) == 4 && Math.abs(dz) == 2) || (Math.abs(dx) == 2 && Math.abs(dz) == 4)) {
                        level.setBlock(bottomPos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(bottomPos, Blocks.PRISMARINE_BRICKS.defaultBlockState(), 2);
                    }

                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);

                    if ((dx * 3 + dz * 7) % 11 == 0 && (Math.abs(dx) >= 4 || Math.abs(dz) >= 4)) {
                        level.setBlock(p.above(), Blocks.LILY_PAD.defaultBlockState(), 2);
                    }
                }
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

        int[][] pillarCoords = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] coord : pillarCoords) {
            int px = cx + coord[0];
            int pz = cz + coord[1];

            level.setBlock(new BlockPos(px, cy + 1, pz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            for (int h = 2; h <= 6; h++) {
                level.setBlock(new BlockPos(px, cy + h, pz), Blocks.QUARTZ_PILLAR.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(px, cy + 7, pz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 8, pz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);

            BlockPos hangingChain = new BlockPos(px + (coord[0] > 0 ? -1 : 1), cy + 6, pz);
            level.setBlock(hangingChain, Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(hangingChain.below(), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 7, cz - 5), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 7, cz + 5), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }
        for (int z = -4; z <= 4; z++) {
            level.setBlock(new BlockPos(cx - 5, cy + 7, cz + z), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 5, cy + 7, cz + z), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        }

        int[][] fountains = {{0, -6}, {0, 6}, {-6, 0}, {6, 0}};
        for (int[] f : fountains) {
            int fx = cx + f[0];
            int fz = cz + f[1];
            level.setBlock(new BlockPos(fx, cy + 2, fz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(fx, cy + 3, fz), Blocks.END_ROD.defaultBlockState(), 2);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy + 1, cz + dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        BlockPos altarPos = new BlockPos(cx, cy + 2, cz);
        if (com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.isBound()) {
            level.setBlock(altarPos, com.soybe.awakeningsystem.block.SystemBlocks.GODDESS_ALTAR.get().defaultBlockState()
                    .setValue(com.soybe.awakeningsystem.block.GoddessAltarBlock.FACING, Direction.SOUTH), 2);
        } else {
            level.setBlock(altarPos, Blocks.LODESTONE.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx - 2, cy + 2, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 3, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 3, cz), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        int sx = cx;
        int sz = cz + 2;

        level.setBlock(new BlockPos(sx, cy + 2, sz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, cy + 2, sz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 2, sz), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);

        level.setBlock(new BlockPos(sx, cy + 3, sz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, cy + 3, sz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 3, sz), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        level.setBlock(new BlockPos(sx, cy + 4, sz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);

        level.setBlock(new BlockPos(sx - 1, cy + 4, sz - 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 4, sz - 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(sx, cy + 4, sz - 1), Blocks.SEA_LANTERN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(sx, cy + 5, sz), Blocks.CALCITE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx, cy + 6, sz), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx, cy + 6, sz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(sx - 1, cy + 4, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(sx + 1, cy + 4, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        level.setBlock(new BlockPos(sx - 2, cy + 5, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(sx + 2, cy + 5, sz + 1), Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);

        level.setBlock(new BlockPos(sx - 3, cy + 6, sz + 1), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 3, cy + 6, sz + 1), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 2);

        level.setBlock(new BlockPos(sx - 3, cy + 7, sz + 1), Blocks.END_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 3, cy + 7, sz + 1), Blocks.END_ROD.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy + 2, cz - 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillGoddessChest(level, chestPos);
    }

    private static void fillGoddessChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(0, makeRelic(Items.PAPER, 1, "\u00A7b\u00A7l✦ Scroll of the Goddess of Light ✦",
                    "Isekai Sanctum", "Offer gems to the Altar for blessings or monster remains to challenge Celestial Judgment. Spring waters cleanse all evil."));
        }
    }

    public static void populateGoddessShrine(ServerLevel level, BlockPos center) {
        BlockPos chestPos = new BlockPos(center.getX(), center.getY() + 2, center.getZ() - 3);
        fillGoddessChest(level, chestPos);
    }

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

        ensureFoundation(level, cx - 13, cx + 13, cz - 13, cz + 13, cy, Blocks.STONE);
        clearRoom(level, cx - 13, cx + 13, cy + 1, cy + 15, cz - 13, cz + 13);

        for (int dx = -13; dx <= 13; dx++) {
            for (int dz = -13; dz <= 13; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 13.5D) continue;

                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);

                if (dist >= 10.0D) {
                    int hillHeight = (int) (1 + (dist - 10.0D) * 1.2D);
                    for (int h = 0; h <= hillHeight; h++) {
                        BlockPos rockPos = new BlockPos(cx + dx, cy + 1 + h, cz + dz);
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

                    BlockPos top = new BlockPos(cx + dx, cy + 2 + hillHeight, cz + dz);
                    if ((dx + dz) % 5 == 0) {
                        level.setBlock(top, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
                    } else if ((dx * dz) % 7 == 0) {
                        level.setBlock(top, Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2);
                    }
                }
                else {
                    if ((dx + dz) % 5 == 0) {
                        level.setBlock(p, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    } else if ((dx * 2 + dz) % 7 == 0) {
                        level.setBlock(p, Blocks.ANDESITE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    }
                    level.setBlock(p.below(), Blocks.DIRT.defaultBlockState(), 2);

                    if (dx <= -6 && Math.abs(dz) <= 7 && (dx * 3 + dz * 2) % 4 == 0) {
                        int bHeight = 3 + Math.abs((dx + dz) % 4);
                        for (int bh = 1; bh <= bHeight; bh++) {
                            level.setBlock(p.above(bh), Blocks.BAMBOO.defaultBlockState(), 2);
                        }
                    }

                    if ((dx * 7 + dz * 5) % 9 == 0 && dx > -5 && dz > 5) {
                        level.setBlock(p.above(), Blocks.PINK_PETALS.defaultBlockState(), 2);
                    }
                }
            }
        }

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

                if (dist >= 3.2D) {
                    if ((dx + dz) % 2 == 0) {
                        level.setBlock(waterPos, Blocks.SMOOTH_BASALT.defaultBlockState(), 2);
                    } else {
                        level.setBlock(waterPos, Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState(), 2);
                    }
                    level.setBlock(floorPos, Blocks.DEEPSLATE.defaultBlockState(), 2);
                } else {
                    level.setBlock(heatPos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

                    if (dist <= 1.5D) {
                        level.setBlock(floorPos, Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
                    } else if ((dx + dz) % 3 == 0) {
                        level.setBlock(floorPos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    } else {
                        level.setBlock(floorPos, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                    }

                    level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(hx - 3, cy + 2, hz - 2), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(hx - 3, cy + 3, hz - 2), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(hx + 3, cy + 2, hz + 2), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(hx + 3, cy + 3, hz + 2), Blocks.LANTERN.defaultBlockState(), 2);

        int px = cx + 4;
        int pz = cz - 3;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos floor = new BlockPos(px + dx, cy + 1, pz + dz);
                boolean isBorder = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                level.setBlock(floor, isBorder ? Blocks.STRIPPED_CHERRY_WOOD.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                level.setBlock(floor.below(), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }

        level.setBlock(new BlockPos(px - 4, cy + 1, pz - 1), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(px - 4, cy + 1, pz), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(px - 4, cy + 1, pz + 1), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        int[][] columns = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] col : columns) {
            int colX = px + col[0];
            int colZ = pz + col[1];
            for (int h = 2; h <= 5; h++) {
                level.setBlock(new BlockPos(colX, cy + h, colZ), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            }
        }

        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(new BlockPos(px + dx, cy + 5, pz - 3), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px + dx, cy + 5, pz + 3), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        }
        for (int dz = -3; dz <= 3; dz++) {
            level.setBlock(new BlockPos(px - 3, cy + 5, pz + dz), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px + 3, cy + 5, pz + dz), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos roofPos = new BlockPos(px + dx, cy + 6, pz + dz);
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    Direction facingDir;
                    if (Math.abs(dx) >= Math.abs(dz)) {
                        facingDir = dx > 0 ? Direction.EAST : Direction.WEST;
                    } else {
                        facingDir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
                    }
                    level.setBlock(roofPos, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facingDir), 2);
                }
                else if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    level.setBlock(roofPos, Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
                else {
                    level.setBlock(roofPos, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                }
            }
        }

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

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(px + dx, cy + 8, pz + dz), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(px, cy + 8, pz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(px, cy + 9, pz), Blocks.END_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(px, cy + 5, pz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(px, cy + 4, pz), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        int[][] eaveLanterns = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] eLantern : eaveLanterns) {
            BlockPos ePos = new BlockPos(px + eLantern[0], cy + 5, pz + eLantern[1]);
            level.setBlock(ePos, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        level.setBlock(new BlockPos(px, cy + 2, pz), Blocks.RED_CARPET.defaultBlockState(), 2);

        level.setBlock(new BlockPos(px, cy + 2, pz - 2), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(px, cy + 3, pz - 2), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 2);

        BlockPos lecternPos = new BlockPos(px - 2, cy + 2, pz);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST), 2);

        BlockPos chestPos = new BlockPos(px - 2, cy + 2, pz - 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillSpiritValleyChest(level, chestPos);

        spawnSpiritBeastIfAbsent(level, new BlockPos(hx - 2, cy + 1, hz - 4), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Immortal Beast] Spiritual Crane");
        spawnSpiritBeastIfAbsent(level, new BlockPos(hx + 3, cy + 1, hz + 3), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Immortal Beast] Spiritual Crane");
        spawnSpiritBeastIfAbsent(level, new BlockPos(cx + 6, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Spirit Beast] Nine-Tailed Fox");
        spawnSpiritBeastIfAbsent(level, new BlockPos(cx - 7, cy + 1, cz + 5), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Spirit Beast] Nine-Tailed Fox");
    }

    private static void fillSpiritValleyChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(0, makeRelic(Items.PAPER, 1, "\u00A7a\u00A7l✦ Spirit Beast Valley Manual ✦",
                    "Murim Realm", "The hot springs and pavilion mat concentrate supreme heavenly Qi. Meditate here to multiply your Dantian cultivation x3."));
        }
    }

    public static void populateSpiritValley(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int hx = cx - 4;
        int hz = cz + 2;
        int px = cx + 4;
        int pz = cz - 3;

        BlockPos chestPos = new BlockPos(px - 2, cy + 2, pz - 1);
        fillSpiritValleyChest(level, chestPos);

        spawnSpiritBeastIfAbsent(level, new BlockPos(hx - 2, cy + 1, hz - 4), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Immortal Beast] Spiritual Crane");
        spawnSpiritBeastIfAbsent(level, new BlockPos(hx + 3, cy + 1, hz + 3), com.soybe.awakeningsystem.entity.ModEntities.CLOUD_CRANE.get(), "\u00A7e[Immortal Beast] Spiritual Crane");
        spawnSpiritBeastIfAbsent(level, new BlockPos(cx + 6, cy + 1, cz + 4), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Spirit Beast] Nine-Tailed Fox");
        spawnSpiritBeastIfAbsent(level, new BlockPos(cx - 7, cy + 1, cz + 5), com.soybe.awakeningsystem.entity.ModEntities.SPIRIT_FOX.get(), "\u00A7d[Spirit Beast] Nine-Tailed Fox");
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

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy, Blocks.POLISHED_BLACKSTONE);
        clearRoom(level, cx - 8, cx + 8, cy + 1, cy + 50, cz - 8, cz + 8);

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 7.5D) continue;

                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
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

        int floorHeight = 4;
        int totalFloors = 9;

        for (int floorIndex = 0; floorIndex < totalFloors; floorIndex++) {
            int baseY = cy + 2 + (floorIndex * floorHeight);
            int topY = baseY + floorHeight - 1;

            if (floorIndex > 0) {
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        level.setBlock(new BlockPos(cx + dx, baseY - 1, cz + dz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                        if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                            level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), Blocks.RED_CARPET.defaultBlockState(), 2);
                        }
                    }
                }
            }

            int[][] cornerPosts = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
            for (int[] post : cornerPosts) {
                for (int y = baseY; y <= topY; y++) {
                    level.setBlock(new BlockPos(cx + post[0], y, cz + post[1]), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                }
            }

            for (int dx = -4; dx <= 4; dx++) {
                for (int zSign : new int[]{-5, 5}) {
                    if (floorIndex == 0 && zSign == -5) {
                        if (dx == 0) {
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
                for (int xSign : new int[]{-5, 5}) {
                    level.setBlock(new BlockPos(cx + xSign, baseY, cz + dz), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + xSign, baseY + 1, cz + dz), Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + xSign, baseY + 2, cz + dz), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + xSign, topY, cz + dz), Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState(), 2);
                }
            }

            if (floorIndex < totalFloors - 1) {
                int wall = floorIndex % 4;
                switch (wall) {
                    case 0 -> {
                        for (int step = 0; step < 4; step++) {
                            int stepDz = 2 - step;
                            int stepY = baseY + step;
                            BlockPos sPos = new BlockPos(cx + 4, stepY, cz + stepDz);
                            level.setBlock(sPos, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                            for (int fillY = baseY; fillY < stepY; fillY++) {
                                level.setBlock(new BlockPos(cx + 4, fillY, cz + stepDz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                            }
                            for (int head = 1; head <= 3; head++) {
                                level.setBlock(sPos.above(head), Blocks.AIR.defaultBlockState(), 2);
                            }
                            level.setBlock(new BlockPos(cx + 3, stepY + 1, cz + stepDz), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                        }
                    }
                    case 1 -> {
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

            int[][] eaveCorners = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
            for (int[] ec : eaveCorners) {
                BlockPos lPos = new BlockPos(cx + ec[0], eaveY - 1, cz + ec[1]);
                level.setBlock(lPos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
            }

            if (floorIndex > 0) {
                level.setBlock(new BlockPos(cx - 3, topY, cz - 3), Blocks.CHAIN.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx - 3, topY - 1, cz - 3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                level.setBlock(new BlockPos(cx + 3, topY, cz + 3), Blocks.CHAIN.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 3, topY - 1, cz + 3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(cx + dx, cy + 1, cz + dz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        BlockPos gatePos = new BlockPos(cx, cy + 2, cz);
        level.setBlock(gatePos.below(), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(gatePos.west(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.west().above(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.east(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.east().above(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(gatePos.above(2), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(gatePos.above(3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        level.setBlock(gatePos, Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(gatePos.above(), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.FACING, Direction.NORTH), 2);

        BlockPos altarPos = new BlockPos(cx, cy + 2, cz + 2);
        level.setBlock(altarPos, Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 2);
        level.setBlock(altarPos.north(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(altarPos.south(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(altarPos.east(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(altarPos.west(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        BlockPos lecternPos = new BlockPos(cx - 3, cy + 2, cz - 2);
        level.setBlock(lecternPos, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST), 2);

        BlockPos chestPos = new BlockPos(cx - 3, cy + 2, cz - 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        level.setBlock(chestPos.above(), Blocks.AIR.defaultBlockState(), 2);
        fillNineTribulationsChest(level, chestPos);

        int roofBaseY = cy + 2 + (totalFloors * floorHeight);

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlock(new BlockPos(cx + dx, roofBaseY, cz + dz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, roofBaseY - 1, cz + dz), Blocks.IRON_BLOCK.defaultBlockState(), 2);
            }
        }
        BlockPos beaconPos = new BlockPos(cx, roofBaseY, cz);
        level.setBlock(beaconPos, Blocks.BEACON.defaultBlockState(), 2);
        level.setBlock(beaconPos.above(), Blocks.WHITE_STAINED_GLASS.defaultBlockState(), 2);

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

        level.setBlock(new BlockPos(cx, roofBaseY + 2, cz), Blocks.WHITE_STAINED_GLASS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, roofBaseY + 3, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, roofBaseY + 4, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, roofBaseY + 5, cz), Blocks.END_ROD.defaultBlockState(), 2);
    }

    private static void fillNineTribulationsChest(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(0, makeRelic(Items.PAPER, 1, "§6§l✦ Nine Tribulations Scroll ✦",
                    "Great Murim Pagoda", "Ascend the 9 tiers and overcome elemental tribulations to purify your Dantian. Only true immortals reach the celestial summit."));
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

    public static void populateNineTribulationsPagoda(ServerLevel level, BlockPos center) {
        BlockPos chestPos = new BlockPos(center.getX() - 3, center.getY() + 2, center.getZ() - 1);
        fillNineTribulationsChest(level, chestPos);
    }

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

        ensureFoundation(level, cx - 11, cx + 11, cz - 9, cz + 9, cy - 1, Blocks.DEEPSLATE_BRICKS);
        clearRoom(level, cx - 13, cx + 13, cy, cy + 18, cz - 11, cz + 11);

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

        for (int y = 1; y <= 5; y++) {
            int py = cy + y;
            for (int x = -10; x <= 10; x++) {
                for (int z = -8; z <= 8; z++) {
                    boolean isEdge = (Math.abs(x) == 10 || Math.abs(z) == 8);
                    if (!isEdge) continue;

                    BlockPos p = new BlockPos(cx + x, py, cz + z);

                    boolean isCorner = (Math.abs(x) == 10 && Math.abs(z) == 8);
                    if (isCorner) {
                        level.setBlock(p, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
                        continue;
                    }

                    if (y == 1) {
                        level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                        continue;
                    }

                    if (y == 5) {
                        level.setBlock(p, Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), 2);
                        continue;
                    }

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
                        level.setBlock(p, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        level.setBlock(new BlockPos(cx, cy + 1, cz - 8), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 8), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 8), Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH).setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 2);

        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 8), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz - 8), Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(), 2);

        for (int px = -2; px <= 2; px++) {
            level.setBlock(new BlockPos(cx + px, cy, cz - 9), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + px, cy, cz - 10), Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + px, cy + 3, cz - 9), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 9), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 1, cz - 10), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz - 10), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz - 10), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz - 10), Blocks.AIR.defaultBlockState(), 2);

        for (int x = -4; x <= 4; x++) {
            if (x == 3) continue;
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 4), Blocks.DARK_OAK_WOOD.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 4), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        for (int x = -4; x <= 4; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 7), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 2, cz + 7), (x == 0) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz + 6), Blocks.AIR.defaultBlockState(), 2);

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 5), ModEntities.SERIES_NPC.get(), NpcProfile.ELENA_RECEPTIONIST);

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
        level.setBlock(new BlockPos(cx - 8, cy + 4, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 8, cy + 3, cz), Blocks.LANTERN.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 2);

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

        int[] beamZ = {-4, 0, 4};
        for (int bz : beamZ) {
            for (int x = -9; x <= 9; x++) {
                level.setBlock(new BlockPos(cx + x, cy + 5, cz + bz), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 2);
            }
        }

        int[][] lanternPoints = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, 0}};
        for (int[] lp : lanternPoints) {
            int lx = cx + lp[0];
            int lz = cz + lp[1];
            level.setBlock(new BlockPos(lx, cy + 4, lz), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(lx, cy + 3, lz), Blocks.LANTERN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 2);
        }

        for (int layer = 0; layer <= 6; layer++) {
            int ry = cy + 5 + layer;
            int zNorth = -9 + layer;
            int zSouth = 9 - layer;

            if (zNorth > zSouth) break;

            for (int x = -11; x <= 11; x++) {
                BlockPos pNorth = new BlockPos(cx + x, ry, cz + zNorth);
                level.setBlock(pNorth, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);

                BlockPos pSouth = new BlockPos(cx + x, ry, cz + zSouth);
                level.setBlock(pSouth, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);

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

                for (int z = zNorth + 1; z < zSouth; z++) {
                    BlockPos pMid = new BlockPos(cx + x, ry, cz + z);
                    if (layer == 6 || zNorth + 1 == zSouth - 1) {
                        level.setBlock(pMid, Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                    } else if (Math.abs(x) == 10) {
                        level.setBlock(pMid, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int y = 1; y <= 13; y++) {
            int py = cy + y;
            for (int z = -1; z <= 1; z++) {
                level.setBlock(new BlockPos(cx + 9, py, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 10, py, cz + z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx + 9, cy + 1, cz), Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 1, cz), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 2, cz), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 3, cz), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 3, cz - 1), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 3, cz + 1), Blocks.LANTERN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx + 9, cy + 14, cz), Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 10, cy + 14, cz), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 9, cy + 14, cz - 1), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 9, cy + 14, cz + 1), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 9, cy + 15, cz), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2);
    }

    public static void populateAdventurersGuild(ServerLevel level, BlockPos center) {
        spawnNpcIfAbsent(level, new BlockPos(center.getX(), center.getY() + 1, center.getZ() + 5), ModEntities.SERIES_NPC.get(), NpcProfile.ELENA_RECEPTIONIST);
    }

    public static void buildBloodSectFortress(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 15, cx + 15, cz - 15, cz + 15, cy - 1, Blocks.POLISHED_BLACKSTONE);
        clearRoom(level, cx - 15, cx + 15, cy, cy + 16, cz - 15, cz + 15);

        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 15 || Math.abs(z) == 15) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                } else if (z >= 6 && z <= 14 && Math.abs(x) <= 10) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    level.setBlock(p.above(2), ((x + z) % 2 == 0) ? Blocks.CRIMSON_PLANKS.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                } else if (Math.abs(x) <= 1 && z >= -14 && z <= 5) {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    level.setBlock(p.above(), Blocks.RED_CARPET.defaultBlockState(), 2);
                } else if ((x * x + (z + 4) * (z + 4)) <= 9) {
                    level.setBlock(p, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(p, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }

        for (int i = -15; i <= 15; i++) {
            buildFortressWallSegment(level, cx + i, cy, cz + 15);
            buildFortressWallSegment(level, cx - 15, cy, cz + i);
            buildFortressWallSegment(level, cx + 15, cy, cz + i);

            if (Math.abs(i) > 2) {
                buildFortressWallSegment(level, cx + i, cy, cz - 15);
            }
        }

        for (int i = -15; i <= 15; i++) {
            boolean wallTooth = Math.abs(i) % 2 == 0;
            setFortressBattlement(level, cx + i, cy + 5, cz + 15, wallTooth);
            setFortressBattlement(level, cx - 15, cy + 5, cz + i, wallTooth);
            setFortressBattlement(level, cx + 15, cy + 5, cz + i, wallTooth);
            if (Math.abs(i) > 2) {
                setFortressBattlement(level, cx + i, cy + 5, cz - 15, wallTooth);
            }
        }

        for (int i = -14; i <= 14; i++) {
            level.setBlock(new BlockPos(cx + i, cy + 3, cz + 14), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 14, cy + 3, cz + i), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 14, cy + 3, cz + i), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            if (Math.abs(i) > 3) {
                level.setBlock(new BlockPos(cx + i, cy + 3, cz - 14), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 2);
            }
        }

        int[][] towerCorners = {{-15, -15}, {12, -15}, {-15, 12}, {12, 12}};
        for (int[] corner : towerCorners) {
            buildBloodTower(level, cx + corner[0], cy, cz + corner[1]);
        }

        buildBloodGatehouse(level, cx, cy, cz - 15);

        buildBloodCourtyard(level, cx, cy, cz);

        buildBloodPavilion(level, cx, cy, cz);

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
                        level.setBlock(new BlockPos(px, cy + y, pz), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                level.setBlock(new BlockPos(px, cy + 6, pz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

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
        level.setBlock(new BlockPos(minX + 1, cy + 9, minZ + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 2, cy + 9, minZ + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 1, cy + 9, minZ + 2), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 2, cy + 9, minZ + 2), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(minX + 1, cy + 10, minZ + 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildBloodGatehouse(ServerLevel level, int cx, int cy, int cz) {
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

        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 5, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 5, cz + 1), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 4, cz), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP)
                    .setValue(StairBlock.FACING, Direction.NORTH), 2);
        }

        level.setBlock(new BlockPos(cx - 1, cy + 3, cz), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 3, cz), Blocks.IRON_BARS.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 1, cy + 1, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.NORTH), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz + 1), Blocks.CRIMSON_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.NORTH), 2);

        level.setBlock(new BlockPos(cx - 3, cy + 4, cz - 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 4, cz - 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildBloodCourtyard(ServerLevel level, int cx, int cy, int cz) {
        int[][] dummyCoords = {{-4, -8}, {4, -8}};
        for (int[] dc : dummyCoords) {
            BlockPos p = new BlockPos(cx + dc[0], cy + 1, cz + dc[1]);
            level.setBlock(p, Blocks.OAK_FENCE.defaultBlockState(), 2);
            level.setBlock(p.above(), Blocks.HAY_BLOCK.defaultBlockState(), 2);
            level.setBlock(p.above(2), Blocks.CARVED_PUMPKIN.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx - 6, cy + 1, cz - 1), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 6, cy + 1, cz - 1), Blocks.GRINDSTONE.defaultBlockState(), 2);

        buildTotemPedestal(level, cx - 8, cy, cz - 4);

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
        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 5), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(cx + x, cy + 2, cz + 6), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }

        int[][] pavilionPillars = {{-8, 7}, {-3, 7}, {3, 7}, {8, 7}, {-8, 13}, {-3, 13}, {3, 13}, {8, 13}};
        for (int[] pil : pavilionPillars) {
            for (int y = 3; y <= 7; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.POLISHED_BASALT.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + pil[0], cy + 3, cz + pil[1] - 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        for (int x = -9; x <= 9; x++) {
            for (int y = 3; y <= 7; y++) {
                level.setBlock(new BlockPos(cx + x, cy + y, cz + 14), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
        }

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

        for (int x = -5; x <= 5; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 12, cz + 11), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 5, cy + 13, cz + 11), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 5, cy + 13, cz + 11), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        BlockPos thronePos = new BlockPos(cx, cy + 3, cz + 10);
        level.setBlock(thronePos.below(), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH), 2);
        level.setBlock(thronePos.above(), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos.west(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(thronePos.east(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(thronePos.above().west(), Blocks.RED_BANNER.defaultBlockState(), 2);
        level.setBlock(thronePos.above().east(), Blocks.RED_BANNER.defaultBlockState(), 2);

        buildTotemPedestal(level, cx, cy + 2, cz + 13);

        BlockPos chestPos = new BlockPos(cx + 2, cy + 3, cz + 13);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(13, makeRelic(Items.WRITTEN_BOOK, 1,
                    "§4§l✦ Blood Sect Secret Art Manual ✦",
                    "Blood Sect - Murim",
                    "Forbidden technique of the Demonic Cult. Grants dark martial mastery and unleashes Crimson Qi."));
            container.setItem(11, makeRelic(Items.HEART_OF_THE_SEA, 3,
                    "§c§l✦ Blood Condensation Pill ✦",
                    "Demonic Treasure",
                    "Essence of vital energy that purifies meridians and heals lethal wounds."));
            container.setItem(15, new ItemStack(Items.GOLD_INGOT, 32));
        }
    }

    private static void spawnBloodSectEntities(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        BlockPos bossPos = new BlockPos(cx, cy + 3, cz + 9);
        AABB checkBoss = new AABB(bossPos).inflate(35.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity.class, checkBoss, e -> e.isAlive()).isEmpty()) {
            var boss = new com.soybe.awakeningsystem.entity.BloodPatriarchBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.BLOOD_PATRIARCH_BOSS.get(), level);
            boss.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 0.0F, 0.0F);
            boss.setPersistenceRequired();
            level.addFreshEntity(boss);
        }

        int[][] totemCoords = {
                {-8, cy + 3, -4},
                {8, cy + 3, -4},
                {0, cy + 5, 13}
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

    public static void populateBloodSectFortress(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        BlockPos chestPos = new BlockPos(cx + 2, cy + 3, cz + 13);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(13, makeRelic(Items.WRITTEN_BOOK, 1,
                    "\u00A74\u00A7l✦ Blood Sect Secret Art Manual ✦",
                    "Blood Sect - Murim",
                    "Forbidden technique of the Demonic Cult. Grants dark martial mastery and unleashes Crimson Qi."));
            container.setItem(11, makeRelic(Items.HEART_OF_THE_SEA, 3,
                    "\u00A7c\u00A7l✦ Blood Condensation Pill ✦",
                    "Demonic Treasure",
                    "Essence of vital energy that purifies meridians and heals lethal wounds."));
            container.setItem(15, new ItemStack(Items.GOLD_INGOT, 32));
        }
        spawnBloodSectEntities(level, center);
    }

    public static void buildYggdrasilRootLabyrinth(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 16, cx + 16, cz - 16, cz + 16, cy - 9, Blocks.DEEPSLATE);
        clearRoom(level, cx - 16, cx + 16, cy - 8, cy - 1, cz - 16, cz + 16);
        clearRoom(level, cx - 16, cx + 16, cy, cy + 8, cz - 16, cz + 16);

        buildSurfaceRootMound(level, cx, cy, cz);

        buildSubterraneanLabyrinth(level, cx, cy, cz);

        buildEntranceStairway(level, cx, cy, cz);

        buildSapSpring(level, cx, cy, cz);

        buildTreantSanctum(level, cx, cy, cz);

        spawnYggdrasilEntities(level, center);
    }

    private static void buildSurfaceRootMound(ServerLevel level, int cx, int cy, int cz) {
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
                    level.setBlock(rootPos.below(), Blocks.CHAIN.defaultBlockState(), 2);
                    level.setBlock(rootPos.below(2), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void buildEntranceStairway(ServerLevel level, int cx, int cy, int cz) {
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

        for (int x = -1; x <= 1; x++) {
            for (int y = -7; y <= -5; y++) {
                level.setBlock(new BlockPos(cx + x, cy + y, cz - 3), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + x, cy + y, cz + 3), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int y = -8; y <= 3; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx, cy - 4, cz), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);

        int[][] steps = {
                {2, 0, 0},
                {2, 1, -1},
                {1, 2, -2},
                {0, 2, -3},
                {-1, 2, -4},
                {-2, 1, -5},
                {-2, 0, -6},
                {-2, -1, -7},
                {-1, -2, -7},
                {0, -2, -7}
        };

        for (int[] s : steps) {
            int sx = cx + s[0];
            int sz = cz + s[1];
            int sy = cy + s[2];
            BlockPos stepPos = new BlockPos(sx, sy, sz);

            level.setBlock(stepPos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
            for (int h = 1; h <= 4; h++) {
                level.setBlock(stepPos.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        level.setBlock(new BlockPos(cx + 3, cy + 1, cz), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 1, cz), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
    }

    private static void buildSubterraneanLabyrinth(ServerLevel level, int cx, int cy, int cz) {
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                BlockPos floorPos = new BlockPos(cx + x, cy - 8, cz + z);
                BlockPos roofPos = new BlockPos(cx + x, cy - 1, cz + z);

                if ((x * x + z * z) % 7 == 0) {
                    level.setBlock(floorPos, Blocks.EMERALD_ORE.defaultBlockState(), 2);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(floorPos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(floorPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                }

                if (Math.abs(x) <= 3 && Math.abs(z) <= 3) {
                    continue;
                }

                if ((x + z) % 3 == 0) {
                    level.setBlock(roofPos, Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
                } else {
                    level.setBlock(roofPos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                }

                if ((x * 5 + z * 3) % 11 == 0 && Math.abs(x) > 3 && Math.abs(z) > 3) {
                    level.setBlock(roofPos.below(), Blocks.SPORE_BLOSSOM.defaultBlockState(), 2);
                } else if ((x * 7 + z * 5) % 9 == 0 && Math.abs(x) > 3 && Math.abs(z) > 3) {
                    level.setBlock(roofPos.below(), Blocks.HANGING_ROOTS.defaultBlockState(), 2);
                }
            }
        }

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
            level.setBlock(new BlockPos(px, cy - 5, pz - 2), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy - 5, pz + 2), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
        }
    }

    private static void buildSapSpring(ServerLevel level, int cx, int cy, int cz) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy - 8, cz - 9 + dz);
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    level.setBlock(p, Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState(), 2);
                } else {
                    level.setBlock(p.below(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(cx - 1, cy - 7, cz - 10), Blocks.LILY_PAD.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy - 7, cz - 8), Blocks.LILY_PAD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy - 8, cz - 9), Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy - 7, cz - 9), Blocks.OCHRE_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy - 6, cz - 9), Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 4, cy - 8, cz - 9), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy - 7, cz - 9), Blocks.POTTED_FERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy - 8, cz - 9), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy - 7, cz - 9), Blocks.POTTED_FERN.defaultBlockState(), 2);
    }

    private static void buildTreantSanctum(ServerLevel level, int cx, int cy, int cz) {
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

        for (int y = -6; y <= -4; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz + 13), Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + y, cz + 13), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, cy + y, cz + 13), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 2, cy - 6, cz + 13), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy - 6, cz + 13), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy - 6, cz + 12);
        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState()
                .setValue(LockedTreasureChestBlock.FACING, Direction.SOUTH)
                .setValue(LockedTreasureChestBlock.LOCKED, true), 3);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(11, makeRelic(Items.HEART_OF_THE_SEA, 3,
                    "§a§l✦ Yggdrasil Mana Dew ✦",
                    "Vital Essence",
                    "Concentrated drop of World Tree sap. Restores all mana and purifies the body."));
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

        BlockPos bossPos = new BlockPos(cx, cy - 6, cz + 8);
        AABB checkBoss = new AABB(bossPos).inflate(30.0D);
        if (level.getEntitiesOfClass(com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity.class, checkBoss, e -> e.isAlive()).isEmpty()) {
            var boss = new com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity(
                    com.soybe.awakeningsystem.entity.ModEntities.YGGDRASIL_TREANT_BOSS.get(), level);
            boss.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 0.0F, 0.0F);
            boss.setPersistenceRequired();
            level.addFreshEntity(boss);
        }

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

    public static void populateYggdrasilRootLabyrinth(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        BlockPos chestPos = new BlockPos(cx, cy - 6, cz + 12);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            container.setItem(11, makeRelic(Items.HEART_OF_THE_SEA, 3,
                    "\u00A7a\u00A7l✦ Yggdrasil Mana Dew ✦",
                    "Vital Essence",
                    "Concentrated drop of World Tree sap. Restores all mana and purifies the body."));
            container.setItem(13, new ItemStack(Items.GOLDEN_APPLE, 16));
            container.setItem(14, new ItemStack(Items.GOLD_INGOT, 32));
            container.setItem(15, new ItemStack(Items.EMERALD, 48));
            container.setItem(12, new ItemStack(Items.DIAMOND, 12));
        }
        spawnYggdrasilEntities(level, center);
    }

    public static void buildAncestralImmortalGrotto(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        clearRoom(level, cx - 12, cx + 12, cy, cy + 8, cz - 12, cz + 12);

        for (int x = cx - 13; x <= cx + 13; x++) {
            for (int z = cz - 13; z <= cz + 13; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist <= 13.5D) {
                    level.setBlock(new BlockPos(x, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, cy, z), (dist >= 12.0D) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);

                    if (dist >= 11.5D && dist <= 13.5D) {
                        for (int y = cy + 1; y <= cy + 7; y++) {
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

                    level.setBlock(new BlockPos(x, cy + 8, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        buildGrottoEntrance(level, cx, cy, cz);

        buildWuXingChamber(level, cx, cy, cz);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                BlockPos gateBlock = new BlockPos(cx + dx, cy + dy, cz + 13);
                level.setBlock(gateBlock, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
            }
        }
        for (int sign : new int[]{-3, 3}) {
            for (int dy = 1; dy <= 5; dy++) {
                level.setBlock(new BlockPos(cx + sign, cy + dy, cz + 13), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            }
        }

        int innerCz = cz + 23;
        clearRoom(level, cx - 9, cx + 9, cy, cy + 8, innerCz - 9, innerCz + 9);

        for (int x = cx - 10; x <= cx + 10; x++) {
            for (int z = innerCz - 10; z <= innerCz + 10; z++) {
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - innerCz) * (z - innerCz));
                if (dist <= 10.5D) {
                    level.setBlock(new BlockPos(x, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, cy, z), (dist >= 9.0D) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);

                    if (dist >= 8.5D && dist <= 10.5D) {
                        for (int y = cy + 1; y <= cy + 7; y++) {
                            level.setBlock(new BlockPos(x, y, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                        }
                    }
                    level.setBlock(new BlockPos(x, cy + 8, z), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }

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

        buildImmortalSanctum(level, cx, cy, innerCz);

        spawnJadeGuardian(level, new BlockPos(cx, cy + 1, innerCz));
    }

    private static void buildWuXingChamber(ServerLevel level, int cx, int cy, int cz) {
        level.setBlock(new BlockPos(cx, cy, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy, cz), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy, cz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz + 1), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy, cz - 1), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);

        BlockPos waterPos = new BlockPos(cx, cy + 1, cz - 7);
        level.setBlock(waterPos.below(), Blocks.PACKED_ICE.defaultBlockState(), 2);
        level.setBlock(waterPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.WATER), 3);
        level.setBlock(waterPos.offset(-1, 0, 0), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
        level.setBlock(waterPos.offset(1, 0, 0), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);

        BlockPos woodPos = new BlockPos(cx + 7, cy + 1, cz - 2);
        level.setBlock(woodPos.below(), Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
        level.setBlock(woodPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.WOOD), 3);
        level.setBlock(woodPos.offset(0, 0, -1), Blocks.BAMBOO_MOSAIC.defaultBlockState(), 2);
        level.setBlock(woodPos.offset(0, 0, 1), Blocks.BAMBOO_MOSAIC.defaultBlockState(), 2);

        BlockPos metalPos = new BlockPos(cx + 4, cy + 1, cz + 6);
        level.setBlock(metalPos.below(), Blocks.IRON_BLOCK.defaultBlockState(), 2);
        level.setBlock(metalPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.METAL), 3);
        level.setBlock(metalPos.offset(-1, 0, 0), Blocks.COPPER_BLOCK.defaultBlockState(), 2);
        level.setBlock(metalPos.offset(1, 0, 0), Blocks.COPPER_BLOCK.defaultBlockState(), 2);

        BlockPos firePos = new BlockPos(cx - 4, cy + 1, cz + 6);
        level.setBlock(firePos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
        level.setBlock(firePos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.FIRE), 3);
        level.setBlock(firePos.offset(-1, 0, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(firePos.offset(1, 0, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);

        BlockPos earthPos = new BlockPos(cx - 7, cy + 1, cz - 2);
        level.setBlock(earthPos.below(), Blocks.MUD_BRICKS.defaultBlockState(), 2);
        level.setBlock(earthPos, SystemBlocks.FIVE_ELEMENTS_PEDESTAL.get().defaultBlockState()
                .setValue(FiveElementsPedestalBlock.ELEMENT, WuXingElement.EARTH), 3);
        level.setBlock(earthPos.offset(0, 0, -1), Blocks.TERRACOTTA.defaultBlockState(), 2);
        level.setBlock(earthPos.offset(0, 0, 1), Blocks.TERRACOTTA.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 8, cy + 4, cz - 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 4, cz - 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 8, cy + 4, cz + 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 4, cz + 8), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void buildImmortalSanctum(ServerLevel level, int cx, int cy, int cz) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy + 1, cz + dz);
                boolean edge = (Math.abs(dx) == 2 || Math.abs(dz) == 2);
                level.setBlock(p, edge ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            }
        }

        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz), Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz), Blocks.EMERALD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 1), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz + 1), Blocks.AIR.defaultBlockState(), 2);
        for (int h = 1; h <= 3; h++) {
            level.setBlock(new BlockPos(cx, cy + 2 + h, cz), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, cy + 2 + h, cz + 1), Blocks.AIR.defaultBlockState(), 2);
        }

        BlockPos chestPos = new BlockPos(cx, cy + 2, cz);
        level.setBlock(chestPos, SystemBlocks.LOCKED_TREASURE_CHEST.get().defaultBlockState()
                .setValue(LockedTreasureChestBlock.FACING, Direction.SOUTH)
                .setValue(LockedTreasureChestBlock.LOCKED, true), 3);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container container) {
            container.clearContent();
            if (SystemItems.WU_XING_MANUAL.isBound()) {
                container.setItem(13, new ItemStack(SystemItems.WU_XING_MANUAL.get()));
            }
            if (SystemItems.SOUL_TRANSFORMATION_PILL.isBound()) {
                container.setItem(11, new ItemStack(SystemItems.SOUL_TRANSFORMATION_PILL.get(), 8));
            }
            if (SystemItems.THOUSAND_YEAR_GINSENG.isBound()) {
                container.setItem(15, new ItemStack(SystemItems.THOUSAND_YEAR_GINSENG.get(), 4));
            }
            container.setItem(10, new ItemStack(Items.EMERALD, 48));
            container.setItem(12, new ItemStack(Items.GOLD_INGOT, 32));
            container.setItem(14, new ItemStack(Items.DIAMOND, 16));
            container.setItem(16, new ItemStack(Items.NETHERITE_INGOT, 4));
        }

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
        clearRoom(level, cx - 2, cx + 2, cy + 1, cy + 5, cz - 28, cz - 12);

        for (int z = cz - 28; z <= cz - 12; z++) {
            for (int dx = -2; dx <= 2; dx++) {
                level.setBlock(new BlockPos(cx + dx, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy, z), (Math.abs(dx) == 2) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy + 6, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }

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

            for (int y = cy + 1; y <= cy + 5; y++) {
                BlockState mat = (y % 2 == 0) ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState();
                level.setBlock(new BlockPos(cx - 3, y, z), mat, 2);
                level.setBlock(new BlockPos(cx + 3, y, z), mat, 2);
            }
        }

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

        for (int dx = -3; dx <= 3; dx++) {
            for (int y = cy + 1; y <= cy + 6; y++) {
                if (Math.abs(dx) == 3) {
                    level.setBlock(new BlockPos(cx + dx, y, cz - 28), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(cx + dx, y, cz - 29), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
                }
            }
        }
        for (int dx = -4; dx <= 4; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz - 28), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 7, cz - 28), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(new BlockPos(cx + dx, cy + 6, cz - 29), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
        }
        level.setBlock(new BlockPos(cx - 3, cy + 7, cz - 29), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 7, cz - 29), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz - 28), Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);

        for (int z = cz - 32; z <= cz - 29; z++) {
            for (int dx = -3; dx <= 3; dx++) {
                if (z == cz - 32) {
                    level.setBlock(new BlockPos(cx + dx, cy, z), Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                } else {
                    level.setBlock(new BlockPos(cx + dx, cy, z), (Math.abs(dx) == 3) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(cx + dx, cy - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);

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

    public static void populateAncestralImmortalGrotto(ServerLevel level, BlockPos center) {
        spawnJadeGuardian(level, new BlockPos(center.getX(), center.getY() + 1, center.getZ() + 15));
    }

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

        clearRoom(level, cx - 65, cx + 65, cy - 16, cy + 62, cz - 65, cz + 65);

        buildNexusPlaza(level, cx, cy, cz);

        buildElementalBridge(level, cx, cy, cz, Direction.NORTH);
        buildElementalBridge(level, cx, cy, cz, Direction.WEST);
        buildElementalBridge(level, cx, cy, cz, Direction.SOUTH);
        buildElementalBridge(level, cx, cy, cz, Direction.EAST);

        buildWindCitadel(level, cx, cy, cz - 48);
        buildFrostBastion(level, cx - 48, cy, cz);
        buildMagmaFortress(level, cx, cy, cz + 48);
        buildStormSpire(level, cx + 48, cy, cz);

        buildCentralDemonCastle(level, cx, cy + 32, cz);

        buildAscensionSpire(level, cx, cy, cz);

        sealAscensionWithBarrier(level, cx, cy, cz);

        spawnElementalDemonKing(level, new BlockPos(cx, cy + 36, cz + 10));
    }

    private static void buildNexusPlaza(ServerLevel level, int cx, int cy, int cz) {
        buildIslandKeel(level, cx, cy, cz, 12, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS);

        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 12 * 12) {
                    BlockPos p = new BlockPos(cx + dx, cy, cz + dz);
                    if (d2 >= 10 * 10) {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                    } else if (d2 >= 6 * 6) {
                        level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                        if ((dx == 0 && Math.abs(dz) == 8) || (Math.abs(dx) == 8 && dz == 0) || (Math.abs(dx) == 6 && Math.abs(dz) == 6)) {
                            level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        }
                    } else {
                        level.setBlock(p, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 12 * 12 && d2 >= 11 * 11) {
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

        int[][] lamps = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        for (int[] l : lamps) {
            level.setBlock(new BlockPos(cx + l[0], cy + 1, cz + l[1]), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + l[0], cy + 2, cz + l[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + l[0], cy + 3, cz + l[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }
    }

    private static void buildAscensionSpire(ServerLevel level, int cx, int cy, int cz) {
        for (int dy = 1; dy <= 35; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    level.setBlock(new BlockPos(cx + dx, cy + dy, cz + dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int dy = 1; dy <= 32; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    boolean isWall = (Math.abs(dx) == 5 || Math.abs(dz) == 5);
                    if (isWall) {
                        BlockPos wp = new BlockPos(cx + dx, cy + dy, cz + dz);
                        boolean isCorner = (Math.abs(dx) == 5 && Math.abs(dz) == 5);

                        boolean northGate = (dz == -5 && Math.abs(dx) <= 1 && dy <= 4);
                        boolean southGate = (dz == 5 && Math.abs(dx) <= 1 && dy <= 4);
                        boolean westGate = (dx == -5 && Math.abs(dz) <= 1 && dy <= 4);
                        boolean eastGate = (dx == 5 && Math.abs(dz) <= 1 && dy <= 4);

                        boolean isWindow = (dy == 10 || dy == 18 || dy == 26) && (
                                (Math.abs(dz) == 5 && Math.abs(dx) <= 1) || (Math.abs(dx) == 5 && Math.abs(dz) <= 1)
                        );

                        if (northGate || southGate || westGate || eastGate) {
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

        for (int w = -1; w <= 1; w++) {
            level.setBlock(new BlockPos(cx + w, cy + 5, cz - 5), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + w, cy + 5, cz + 5), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 5, cy + 5, cz + w), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 5, cy + 5, cz + w), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        }

        BlockPos soulPos = new BlockPos(cx, cy + 1, cz);
        level.setBlock(soulPos, Blocks.SOUL_SAND.defaultBlockState(), 2);
        for (int dy = 2; dy <= 32; dy++) {
            level.setBlock(new BlockPos(cx, cy + dy, cz), Blocks.WATER.defaultBlockState(), 2);
        }
        for (int dy = 1; dy <= 32; dy++) {
            int[][] shaftWalls = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] sw : shaftWalls) {
                if (sw[0] == 0 && sw[1] == -1 && (dy == 1 || dy == 2)) {
                    level.setBlock(new BlockPos(cx + sw[0], cy + dy, cz + sw[1]), Blocks.WARPED_FENCE_GATE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true), 2);
                }
                else if (sw[0] == 0 && sw[1] == 1 && (dy == 32 || dy == 33)) {
                    level.setBlock(new BlockPos(cx + sw[0], cy + dy, cz + sw[1]), Blocks.WARPED_FENCE_GATE.defaultBlockState().setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true), 2);
                } else {
                    level.setBlock(new BlockPos(cx + sw[0], cy + dy, cz + sw[1]), Blocks.TINTED_GLASS.defaultBlockState(), 2);
                }
            }
            int[][] shaftCorners = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int[] sc : shaftCorners) {
                level.setBlock(new BlockPos(cx + sc[0], cy + dy, cz + sc[1]), (dy % 4 == 0) ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
            }
        }

        for (int i = 0; i <= 6; i++) {
            int sx = -3 + i;
            int sy = cy + 1 + i;
            for (int sz = 3; sz <= 4; sz++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int rx = 3; rx <= 4; rx++) {
            for (int rz = 2; rz <= 4; rz++) {
                BlockPos p = new BlockPos(cx + rx, cy + 8, cz + rz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int i = 0; i <= 6; i++) {
            int sz = 1 - i;
            int sy = cy + 9 + i;
            for (int sx = 3; sx <= 4; sx++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int rx = 2; rx <= 4; rx++) {
            for (int rz = -4; rz <= -3; rz++) {
                BlockPos p = new BlockPos(cx + rx, cy + 16, cz + rz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 8 * 8 && d2 >= 5 * 5) {
                    BlockPos p = new BlockPos(cx + dx, cy + 16, cz + dz);
                    level.setBlock(p, (d2 >= 7 * 7) ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                    for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
                    if (d2 <= 8 * 8 && d2 >= 7 * 7) {
                        level.setBlock(p.above(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
                        if ((dx + dz) % 3 == 0) {
                            level.setBlock(p.above(2), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        for (int rz = -4; rz <= -3; rz++) {
            for (int dh = 1; dh <= 3; dh++) {
                level.setBlock(new BlockPos(cx + 5, cy + 16 + dh, cz + rz), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int i = 0; i <= 6; i++) {
            int sx = 1 - i;
            int sy = cy + 17 + i;
            for (int sz = -4; sz <= -3; sz++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int rx = -4; rx <= -3; rx++) {
            for (int rz = -4; rz <= -2; rz++) {
                BlockPos p = new BlockPos(cx + rx, cy + 24, cz + rz);
                level.setBlock(p, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int i = 0; i <= 6; i++) {
            int sz = -1 + i;
            int sy = cy + 25 + i;
            for (int sx = -4; sx <= -3; sx++) {
                BlockPos p = new BlockPos(cx + sx, sy, cz + sz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 4; h++) level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int dx = -4; dx <= 2; dx++) {
            for (int dz = 1; dz <= 4; dz++) {
                BlockPos p = new BlockPos(cx + dx, cy + 32, cz + dz);
                level.setBlock(p, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                for (int h = 1; h <= 5; h++) {
                    level.setBlock(p.above(h), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 33, cz + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx + 2, cy + 33, cz + 2), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 33, cz + 3), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 34, cz + 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 34, cz + 1), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void sealAscensionWithBarrier(ServerLevel level, int cx, int cy, int cz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                level.setBlock(new BlockPos(cx + dx, cy + dy, cz - 5), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + dx, cy + dy, cz + 5), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx - 5, cy + dy, cz + dx), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(new BlockPos(cx + 5, cy + dy, cz + dx), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
    private static void buildCentralDemonCastle(ServerLevel level, int cx, int cy, int cz) {
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

        for (int dy = 1; dy <= 10; dy++) {
            for (int dx = -14; dx <= 14; dx++) {
                for (int dz = -14; dz <= 14; dz++) {
                    boolean isWall = (Math.abs(dx) == 14 || Math.abs(dz) == 14);
                    if (isWall) {
                        boolean northGate = (dz == -14 && Math.abs(dx) <= 2 && dy <= 6);
                        if (!northGate) {
                            BlockPos wp = new BlockPos(cx + dx, cy + dy, cz + dz);
                            if (dy == 10) {
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

        int[][] towerCenters = {{-14, -14}, {14, -14}, {-14, 14}, {14, 14}};
        for (int[] tc : towerCenters) {
            int tx = cx + tc[0];
            int tz = cz + tc[1];
            for (int dy = 1; dy <= 23; dy++) {
                int r = (dy <= 17) ? 2 : (dy <= 19 ? 3 : (23 - dy));
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
                if (dy == 18) {
                    level.setBlock(new BlockPos(tx, cy + dy, tz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
                }
            }
            level.setBlock(new BlockPos(tx, cy + 24, tz), Blocks.END_ROD.defaultBlockState(), 2);
        }

        buildDemonKingThroneRoom(level, cx, cy, cz);
    }

    private static void buildDemonKingThroneRoom(ServerLevel level, int cx, int cy, int cz) {
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
            level.setBlock(new BlockPos(cx + p[0], cy + 9, cz + p[1]), Blocks.SOUL_TORCH.defaultBlockState(), 2);
        }

        int[][] chandeliers = {{-4, 0}, {4, 0}, {-4, 6}, {4, 6}};
        for (int[] ch : chandeliers) {
            for (int dy = 10; dy >= 7; dy--) {
                level.setBlock(new BlockPos(cx + ch[0], cy + dy, cz + ch[1]), Blocks.CHAIN.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx + ch[0], cy + 6, cz + ch[1]), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

        for (int dz = -13; dz <= 7; dz++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos carpetPos = new BlockPos(cx + w, cy + 1, cz + dz);
                level.setBlock(carpetPos, (w == 0) ? Blocks.RED_CARPET.defaultBlockState() : Blocks.YELLOW_CARPET.defaultBlockState(), 2);
            }
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = 7; dz <= 12; dz++) {
                BlockPos daisPos = new BlockPos(cx + dx, cy + 1, cz + dz);
                level.setBlock(daisPos, Blocks.NETHERITE_BLOCK.defaultBlockState(), 2);
                level.setBlock(daisPos.above(), (Math.abs(dx) == 4 || dz == 12) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }

        BlockPos thronePos = new BlockPos(cx, cy + 3, cz + 10);
        level.setBlock(thronePos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos.west(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        level.setBlock(thronePos.east(), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), 2);
        for (int dy = 1; dy <= 4; dy++) {
            level.setBlock(thronePos.above(dy), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
            if (dy <= 3) {
                level.setBlock(thronePos.above(dy).west(), Blocks.OBSIDIAN.defaultBlockState(), 2);
                level.setBlock(thronePos.above(dy).east(), Blocks.OBSIDIAN.defaultBlockState(), 2);
            }
        }
        level.setBlock(thronePos.above(5), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(thronePos.above(6), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

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

    private static void buildElementalBridge(ServerLevel level, int cx, int cy, int cz, Direction dir) {
        for (int step = 11; step <= 34; step++) {
            int bx = cx + dir.getStepX() * step;
            int bz = cz + dir.getStepZ() * step;

            for (int w = -2; w <= 2; w++) {
                int px = (dir.getAxis() == Direction.Axis.Z) ? (bx + w) : bx;
                int pz = (dir.getAxis() == Direction.Axis.X) ? (bz + w) : bz;

                BlockPos floorPos = new BlockPos(px, cy, pz);
                if (Math.abs(w) == 2) {
                    level.setBlock(floorPos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
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

                level.setBlock(floorPos.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                if (Math.abs(w) <= 1 && (step % 3 == 0 || step == 11 || step == 34)) {
                    level.setBlock(floorPos.below(2), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void buildWindCitadel(ServerLevel level, int ix, int cy, int iz) {
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.PRISMARINE, Blocks.DARK_PRISMARINE);

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

                    if ((Math.abs(dx) == 6 && Math.abs(dz) == 6) || (Math.abs(dx) == 10 && dz == 0) || (dx == 0 && Math.abs(dz) == 10)) {
                        level.setBlock(p, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
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

        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(ix + pil[0], cy + 9, iz + pil[1]), Blocks.END_ROD.defaultBlockState(), 2);
        }

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

        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.WIND);
    }

    private static void buildFrostBastion(ServerLevel level, int ix, int cy, int iz) {
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.PACKED_ICE, Blocks.BLUE_ICE);

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

        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
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

        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.BLUE_ICE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }

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

        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.FROST);
    }

    private static void buildMagmaFortress(ServerLevel level, int ix, int cy, int iz) {
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.POLISHED_BASALT, Blocks.NETHER_BRICKS);

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

        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
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

        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.RED_NETHER_BRICKS.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }

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

        spawnElementalLord(level, new BlockPos(ix, cy + 2, iz), ElementalType.FIRE);
    }

    private static void buildStormSpire(ServerLevel level, int ix, int cy, int iz) {
        buildIslandKeel(level, ix, cy, iz, 14, Blocks.DEEPSLATE_BRICKS, Blocks.CUT_COPPER);

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

        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 14 * 14 && d2 >= 13 * 13) {
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

        int[][] pillars = {{-8, -8}, {8, -8}, {-8, 8}, {8, 8}};
        for (int[] pil : pillars) {
            for (int dy = 1; dy <= 7; dy++) {
                level.setBlock(new BlockPos(ix + pil[0], cy + dy, iz + pil[1]), Blocks.CUT_COPPER.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(ix + pil[0], cy + 8, iz + pil[1]), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        }

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

    public static void populateElementalFloatingCastles(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = Math.max(180, center.getY());
        int cz = center.getZ();

        spawnElementalDemonKing(level, new BlockPos(cx, cy + 36, cz + 10));
        spawnElementalLord(level, new BlockPos(cx, cy + 2, cz - 42), ElementalType.WIND);
        spawnElementalLord(level, new BlockPos(cx, cy + 2, cz + 42), ElementalType.FROST);
        spawnElementalLord(level, new BlockPos(cx + 42, cy + 2, cz), ElementalType.FIRE);
        spawnElementalLord(level, new BlockPos(cx - 42, cy + 2, cz), ElementalType.STORM);
    }

    public static void buildHunterExpeditionCamp(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 11, cx + 9, cz - 8, cz + 8, cy - 1, Blocks.DIRT);
        clearRoom(level, cx - 11, cx + 9, cy, cy + 9, cz - 8, cz + 8);

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

        for (int z = -4; z <= 4; z++) {
            level.setBlock(new BlockPos(cx, cy - 1, cz + z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        }
        for (int x = -3; x <= 5; x++) {
            level.setBlock(new BlockPos(cx + x, cy - 1, cz), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        }

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

        buildGrandExpeditionTent(level, cx, cy, cz);

        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy - 1, cz - 2), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy, cz - 2),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.SOUTH), 2);
        }
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy - 1, cz + 2), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, cy, cz + 2),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.NORTH), 2);
        }
        for (int dz = -1; dz <= 1; dz++) {
            level.setBlock(new BlockPos(cx + 2, cy - 1, cz + dz), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 2, cy, cz + dz),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.WEST), 2);
        }
        for (int dz = -1; dz <= 1; dz++) {
            level.setBlock(new BlockPos(cx - 2, cy - 1, cz + dz), Blocks.COARSE_DIRT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 2, cy, cz + dz),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.EAST), 2);
        }

        buildSupplyAwning(level, cx + 5, cy, cz - 3);

        buildWatchpost(level, cx + 5, cy, cz + 4);

        level.setBlock(new BlockPos(cx - 10, cy, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 10, cy + 1, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy, cz - 6), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 8, cy + 1, cz - 6), Blocks.LANTERN.defaultBlockState(), 2);
    }

    private static void buildGrandExpeditionTent(ServerLevel level, int cx, int cy, int cz) {
        for (int x = cx - 11; x <= cx - 3; x++) {
            for (int z = cz - 5; z <= cz + 5; z++) {
                for (int y = 0; y <= 6; y++) {
                    level.setBlock(new BlockPos(x, cy + y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int x = cx - 10; x <= cx - 3; x++) {
            for (int z = cz - 4; z <= cz + 4; z++) {
                level.setBlock(new BlockPos(x, cy - 1, z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx - 2, cy - 1, cz), Blocks.DIRT_PATH.defaultBlockState(), 2);

        for (int x = cx - 10; x <= cx - 3; x++) {
            level.setBlock(new BlockPos(x, cy + 4, cz),
                    Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 2);
        }

        for (int x = cx - 10; x <= cx - 3; x++) {
            level.setBlock(new BlockPos(x, cy + 3, cz - 1), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy + 3, cz + 1), Blocks.GREEN_WOOL.defaultBlockState(), 2);

            level.setBlock(new BlockPos(x, cy + 2, cz - 2), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy + 2, cz + 2), Blocks.GREEN_WOOL.defaultBlockState(), 2);

            level.setBlock(new BlockPos(x, cy + 1, cz - 3), Blocks.GREEN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy + 1, cz + 3), Blocks.GREEN_WOOL.defaultBlockState(), 2);

            level.setBlock(new BlockPos(x, cy, cz - 4), Blocks.BROWN_WOOL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, cy, cz + 4), Blocks.BROWN_WOOL.defaultBlockState(), 2);
        }

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

        for (int y = 0; y <= 2; y++) {
            level.setBlock(new BlockPos(cx - 3, cy + y, cz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 3, cy + y, cz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        }

        for (int x = cx - 9; x <= cx - 4; x++) {
            level.setBlock(new BlockPos(x, cy, cz), Blocks.RED_CARPET.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx - 4, cy + 3, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 2, cz),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        level.setBlock(new BlockPos(cx - 7, cy + 3, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 2, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx - 7, cy + 1, cz),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        BlockPos campChest = new BlockPos(cx - 9, cy, cz - 1);
        level.setBlock(campChest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), 2);
        fillHunterCampChest(level, campChest);

        BlockPos bedHead = new BlockPos(cx - 9, cy, cz - 2);
        BlockPos bedFoot = new BlockPos(cx - 8, cy, cz - 2);
        level.setBlock(bedHead, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.WEST).setValue(BedBlock.PART, BedPart.HEAD), 2);
        level.setBlock(bedFoot, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.WEST).setValue(BedBlock.PART, BedPart.FOOT), 2);

        level.setBlock(new BlockPos(cx - 9, cy, cz + 2),
                Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx - 8, cy, cz + 2), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 7, cy, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
    }

    private static void buildSupplyAwning(ServerLevel level, int sx, int sy, int sz) {
        level.setBlock(new BlockPos(sx - 1, sy, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy + 1, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy + 1, sz - 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy + 1, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy + 1, sz + 1), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState st = ((dx + dz) % 2 == 0) ? Blocks.BROWN_WOOL.defaultBlockState() : Blocks.YELLOW_WOOL.defaultBlockState();
                level.setBlock(new BlockPos(sx + dx, sy + 2, sz + dz), st, 2);
            }
        }

        level.setBlock(new BlockPos(sx, sy - 1, sz), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx, sy, sz), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx - 1, sy, sz), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy, sz), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(sx + 1, sy + 1, sz),
                Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 2);
        level.setBlock(new BlockPos(sx, sy, sz + 1), Blocks.CAULDRON.defaultBlockState(), 2);
    }

    private static void buildWatchpost(ServerLevel level, int wx, int wy, int wz) {
        for (int y = 0; y <= 2; y++) {
            level.setBlock(new BlockPos(wx - 1, wy + y, wz - 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(wx + 1, wy + y, wz - 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(wx - 1, wy + y, wz + 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
            level.setBlock(new BlockPos(wx + 1, wy + y, wz + 1), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(wx + dx, wy + 3, wz + dz), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                    if (dx == 0 && dz == -1) continue;
                    level.setBlock(new BlockPos(wx + dx, wy + 4, wz + dz), Blocks.SPRUCE_FENCE.defaultBlockState(), 2);
                }
            }
        }

        for (int y = 0; y <= 3; y++) {
            level.setBlock(new BlockPos(wx, wy + y, wz - 1),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 2);
        }

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

            container.setItem(9, com.soybe.awakeningsystem.item.WornLoreBookItem.createVolume(1));

            container.setItem(11, new ItemStack(Items.GOLDEN_APPLE, 2));
            container.setItem(13, new ItemStack(Items.TORCH, 32));
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(18, new ItemStack(SystemItems.AWAKENING_COIN.get(), 25));
            }
        }
    }

    public static void populateHunterExpeditionCamp(ServerLevel level, BlockPos center) {
        fillHunterCampChest(level, new BlockPos(center.getX() + 4, center.getY() + 1, center.getZ() + 2));
    }

    public static void buildRuinedStargazerOutpost(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 8, cx + 8, cz - 8, cz + 8, cy - 1, Blocks.DEEPSLATE_BRICKS);
        clearRoom(level, cx - 8, cx + 8, cy, cy + 22, cz - 8, cz + 8);

        for (int y = 0; y <= 12; y++) {
            int py = cy + y;
            boolean isFloor1 = (y == 0);
            boolean isFloor2 = (y == 6);
            boolean isRoof = (y == 12);

            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos p = new BlockPos(cx + x, py, cz + z);

                    if (isStargazerWall(x, z)) {
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
                        else if ((y == 2 || y == 3) && ((z == -6 && (x == 0 || x == 1)) || (x == -6 && (z == 0 || z == 1)))) {
                            level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 2);
                        }
                        else if ((y == 8 || y == 9) && ((z == 6 && (x == 0 || x == 1)) || (x == 6 && (z == 0 || z == 1)))) {
                            level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 2);
                        }
                        else {
                            int pick = (Math.abs(x) * 7 + Math.abs(z) * 13 + y * 3) % 5;
                            BlockState st = (pick == 0) ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() :
                                    (pick == 1) ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() :
                                    (pick == 2) ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() :
                                            Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                            level.setBlock(p, st, 2);
                        }
                    } else if (isStargazerInside(x, z)) {
                        if (isFloor1 || isFloor2 || isRoof) {
                            if (isFloor2 && (x == 3 || x == 4) && (z >= -3 && z <= 2)) {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                                continue;
                            }
                            if (isRoof && (x == -4 || x == -3) && (z >= -3 && z <= 2)) {
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                                continue;
                            }

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

        for (int i = 0; i <= 5; i++) {
            int stepY = cy + 1 + i;
            int sz = cz - 3 + i;

            for (int dx = 3; dx <= 4; dx++) {
                int sx = cx + dx;
                for (int underY = cy + 1; underY < stepY; underY++) {
                    level.setBlock(new BlockPos(sx, underY, sz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(sx, stepY, sz),
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
                for (int clearY = stepY + 1; clearY <= cy + 11; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, sz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int dx = 3; dx <= 4; dx++) {
            int sx = cx + dx;
            for (int underY = cy + 1; underY < cy + 6; underY++) {
                level.setBlock(new BlockPos(sx, underY, cz + 3), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(sx, cy + 6, cz + 3), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            for (int fz = 3; fz <= 5; fz++) {
                for (int clearY = cy + 7; clearY <= cy + 11; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, cz + fz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i <= 5; i++) {
            int stepY = cy + 7 + i;
            int sz = cz + 2 - i;

            for (int dx = -4; dx <= -3; dx++) {
                int sx = cx + dx;
                for (int underY = cy + 6; underY < stepY; underY++) {
                    level.setBlock(new BlockPos(sx, underY, sz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(sx, stepY, sz),
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
                for (int clearY = stepY + 1; clearY <= cy + 18; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, sz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int dx = -4; dx <= -3; dx++) {
            int sx = cx + dx;
            for (int underY = cy + 6; underY < cy + 12; underY++) {
                level.setBlock(new BlockPos(sx, underY, cz - 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(sx, cy + 12, cz - 4), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            for (int fz = -5; fz <= -4; fz++) {
                for (int clearY = cy + 13; clearY <= cy + 18; clearY++) {
                    level.setBlock(new BlockPos(sx, clearY, cz + fz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

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

        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz - 1), Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz - 1), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 0), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 2, cz + 0), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 1), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 5, cy + 1, cz + 2), Blocks.BARREL.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy + 1, cz - 5), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 5), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 0, cy + 1, cz - 5), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 0, cy + 2, cz - 5), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 5), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        for (int z = 1; z <= 5; z++) {
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.RED_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + z), Blocks.RED_CARPET.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 5, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(cx + dx, cy + 7, cz - 5), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx, cy + 7, cz - 4), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        BlockPos astroChest = new BlockPos(cx, cy + 8, cz - 4);
        level.setBlock(astroChest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        fillStargazerChest(level, astroChest);

        level.setBlock(new BlockPos(cx - 2, cy + 7, cz - 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 8, cz - 4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 7, cz - 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 8, cz - 4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 3, cy + 7, cz - 4), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 7, cz - 4), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy + 7, cz + 5), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 7, cz + 5), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 8, cz + 5), Blocks.LANTERN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 11, cz),
                Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz),
                Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        BlockPos scopeBase = new BlockPos(cx, cy + 13, cz);
        level.setBlock(scopeBase, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(scopeBase.above(), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        BlockPos deskPos = new BlockPos(cx - 1, cy + 13, cz);
        level.setBlock(deskPos, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(deskPos.above(), Blocks.SOUL_LANTERN.defaultBlockState(), 2);

        if (SystemBlocks.CAMPFIRE_LOG_SEAT.isBound()) {
            level.setBlock(new BlockPos(cx + 1, cy + 13, cz),
                    SystemBlocks.CAMPFIRE_LOG_SEAT.get().defaultBlockState().setValue(CampfireLogSeatBlock.FACING, Direction.WEST), 2);
        }
    }

    private static boolean isStargazerWall(int x, int z) {
        int ax = Math.abs(x);
        int az = Math.abs(z);
        if (az == 6 && ax <= 3) return true;
        if (ax == 6 && az <= 3) return true;
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

            container.setItem(9, com.soybe.awakeningsystem.item.WornLoreBookItem.createVolume(2));

            container.setItem(13, new ItemStack(Items.GOLD_INGOT, 12));
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(18, new ItemStack(SystemItems.AWAKENING_COIN.get(), 40));
            }
        }
    }

    public static void populateRuinedStargazerOutpost(ServerLevel level, BlockPos center) {
        fillStargazerChest(level, new BlockPos(center.getX() + 2, center.getY() + 1, center.getZ() - 1));
    }

    public static void buildAncientLoreStele(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 9, cx + 9, cz - 9, cz + 9, cy - 1, Blocks.POLISHED_BLACKSTONE_BRICKS);
        clearRoom(level, cx - 9, cx + 9, cy, cy + 16, cz - 9, cz + 9);

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 8.2D) continue;

                BlockPos pBase = new BlockPos(cx + dx, cy, cz + dz);

                if (dist > 5.5D) {
                    int pattern = (Math.abs(dx) + Math.abs(dz)) % 3;
                    BlockState st = (pattern == 0) ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() :
                            (pattern == 1) ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() :
                                    Blocks.POLISHED_BLACKSTONE.defaultBlockState();
                    level.setBlock(pBase, st, 2);
                }
                else if (dist > 2.8D) {
                    level.setBlock(pBase, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 2);
                    BlockPos pUp1 = pBase.above();
                    BlockState ringSt = (dist > 4.2D)
                            ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                            : Blocks.GILDED_BLACKSTONE.defaultBlockState();
                    level.setBlock(pUp1, ringSt, 2);
                }
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

        int[][] cornerPillars = { {5, 5}, {-5, 5}, {5, -5}, {-5, -5} };
        for (int[] p : cornerPillars) {
            int px = cx + p[0];
            int pz = cz + p[1];

            for (int h = 1; h <= 4; h++) {
                level.setBlock(new BlockPos(px, cy + h, pz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(px, cy + 5, pz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 6, pz), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);

            level.setBlock(new BlockPos(px, cy + 1, pz - 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, cy + 1, pz + 1), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(cx - 1, cy + 3, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 3, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz - 1), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz + 1), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 4, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 6, cz),
                Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 4), 2);

        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 8, cz), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 11, cz), Blocks.END_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy + 6, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 6, cz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz - 2), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 6, cz + 2), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);

        BlockPos altarBase = new BlockPos(cx, cy + 2, cz - 3);
        level.setBlock(altarBase, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx, cy + 3, cz - 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        fillSteleChest(level, chestPos);

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

            container.setItem(9, com.soybe.awakeningsystem.item.WornLoreBookItem.createVolume(3));

            if (SystemItems.ROULETTE_TOKEN.isBound()) {
                container.setItem(13, new ItemStack(SystemItems.ROULETTE_TOKEN.get(), 2));
            }
            if (SystemItems.AWAKENING_COIN.isBound()) {
                container.setItem(18, new ItemStack(SystemItems.AWAKENING_COIN.get(), 64));
            }
        }
    }

    public static void populateAncientLoreStele(ServerLevel level, BlockPos center) {
        fillSteleChest(level, new BlockPos(center.getX(), center.getY() + 1, center.getZ() + 3));
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

        int[][] pillars = new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] pil : pillars) {
            for (int y = 2; y <= 6; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            }
        }

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if ((Math.abs(x) == 4 || Math.abs(z) == 4) && !(z == -4 && Math.abs(x) <= 1)) {
                    BlockPos fp = new BlockPos(cx + x, cy + 2, cz + z);
                    level.setBlock(fp, Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
                }
            }
        }

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

        int[][] eavesCorners = new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int[] c : eavesCorners) {
            BlockPos lanternPos = new BlockPos(cx + c[0], cy + 5, cz + c[1]);
            level.setBlock(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

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

        level.setBlock(new BlockPos(cx, cy + 9, cz), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 10, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx, cy + 5, cz), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        level.setBlock(new BlockPos(cx, cy + 1, cz), Blocks.NETHERRACK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.SOUL_FIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.CAULDRON.defaultBlockState(), 2);

        level.setBlock(new BlockPos(cx - 2, cy + 2, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 2, cy + 3, cz + 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 2, cz + 2), Blocks.BREWING_STAND.defaultBlockState(), 2);

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

        spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx - 1, cy + 2, cz - 1));
    }

    public static void populateAncientAlchemyPavilion(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        BlockPos chestPos = new BlockPos(cx + 2, cy + 2, cz - 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.clearContent();
            if (SystemItems.TAOIST_TALISMAN.isBound()) {
                chest.setItem(0, new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 2));
            }
            chest.setItem(1, SystemItems.coin(180));
            chest.setItem(2, new ItemStack(Items.EMERALD, 6));
            chest.setItem(3, new ItemStack(Items.BLAZE_POWDER, 4));
            chest.setItem(4, new ItemStack(Items.NETHER_WART, 5));
            chest.setItem(5, new ItemStack(Items.GOLDEN_CARROT, 3));
            chest.setItem(6, new ItemStack(Items.BREWING_STAND, 1));
        }
        spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx - 1, cy + 2, cz - 1));
    }

    public static void buildTaoistBambooShrine(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        ensureFoundation(level, cx - 6, cx + 6, cz - 6, cz + 6, cy - 1, Blocks.MOSSY_STONE_BRICKS);
        clearRoom(level, cx - 6, cx + 6, cy, cy + 12, cz - 6, cz + 6);

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

        int[][] pillars = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] pil : pillars) {
            for (int y = 2; y <= 5; y++) {
                level.setBlock(new BlockPos(cx + pil[0], cy + y, cz + pil[1]), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            }
        }

        for (int x = -3; x <= 3; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 5, cz - 3), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + x, cy + 5, cz + 3), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
        }
        for (int z = -3; z <= 3; z++) {
            level.setBlock(new BlockPos(cx - 3, cy + 5, cz + z), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 3, cy + 5, cz + z), Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState(), 2);
        }

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

        level.setBlock(new BlockPos(cx, cy + 6, cz), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 7, cz), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);

        int[][] corners = new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] c : corners) {
            BlockPos lanternPos = new BlockPos(cx + c[0], cy + 4, cz + c[1]);
            level.setBlock(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        level.setBlock(new BlockPos(cx, cy + 2, cz), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz), Blocks.WHITE_CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true), 2);

        level.setBlock(new BlockPos(cx, cy + 2, cz + 2), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);

        BlockPos chestPos = new BlockPos(cx - 2, cy + 2, cz - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.setItem(0, new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 2));
            chest.setItem(1, SystemItems.coin(120));
            chest.setItem(2, new ItemStack(Items.GOLDEN_APPLE, 1));
            chest.setItem(3, new ItemStack(Items.BAMBOO, 8));
        }

        spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx + 1, cy + 2, cz + 1));
    }

    public static void populateTaoistBambooShrine(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        BlockPos chestPos = new BlockPos(cx - 2, cy + 2, cz - 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof Container chest) {
            chest.clearContent();
            if (SystemItems.TAOIST_TALISMAN.isBound()) {
                chest.setItem(0, new ItemStack(SystemItems.TAOIST_TALISMAN.get(), 2));
            }
            chest.setItem(1, SystemItems.coin(120));
            chest.setItem(2, new ItemStack(Items.GOLDEN_APPLE, 1));
            chest.setItem(3, new ItemStack(Items.BAMBOO, 8));
        }
        spawnMountHuaDiscipleIfAbsent(level, new BlockPos(cx + 1, cy + 2, cz + 1));
    }
    public static boolean isInsideBlackMarketDen(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        if (level instanceof ServerLevel sl && sl.getServer() != null) {
            SeriesStructureWorldGen.SeriesStructureData data = SeriesStructureWorldGen.SeriesStructureData.get(sl.getServer());
            if (data != null) {
                for (var entry : data.getAll().entrySet()) {
                    if (entry.getKey().startsWith("black_market_den")) {
                        BlockPos center = entry.getValue();
                        if (Math.abs(pos.getX() - center.getX()) <= 10
                                && pos.getZ() >= center.getZ() - 18 && pos.getZ() <= center.getZ() + 10
                                && pos.getY() >= center.getY() - 2 && pos.getY() <= center.getY() + 85) {
                            return true;
                        }
                    }
                }
            }
        }
        List<SeriesNpcEntity> dealers = level.getEntitiesOfClass(
                SeriesNpcEntity.class,
                new AABB(pos).inflate(16),
                npc -> npc.getNpcProfile() == NpcProfile.BLACK_MARKET_DEALER
        );
        return !dealers.isEmpty();
    }

    public static void buildBlackMarketDen(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        clearRoom(level, cx - 16, cx + 16, cy - 1, cy + 10, cz - 16, cz + 16);
        if (TeyvatStructureManager.placeNbtStructure(level, center, "black_market_den", -16, -1, -16)) {
            spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.BLACK_MARKET_DEALER.get(), NpcProfile.BLACK_MARKET_DEALER);
            buildBlackMarketSurfaceHut(level, cx, cy, cz);
            return;
        }

        if (BlackMarketJsonHelper.hasCustomStructure()) {
            int placed = BlackMarketJsonHelper.importStructure(level, center, "default");
            if (placed > 0) {
                spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.BLACK_MARKET_DEALER.get(), NpcProfile.BLACK_MARKET_DEALER);
                buildBlackMarketSurfaceHut(level, cx, cy, cz);
                return;
            }
        }

        clearRoom(level, cx - 6, cx + 6, cy + 1, cy + 5, cz - 6, cz + 6);

        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                    level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                } else if ((Math.abs(x) == 2 && Math.abs(z) == 2) || (x == 0 && z == 0)) {
                    level.setBlock(p, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                } else if ((x + z) % 3 == 0) {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                }
            }
        }

        for (int y = 1; y <= 5; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                        BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                        if (Math.abs(x) == 6 && Math.abs(z) == 6) {
                            level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                        } else if (z == -6 && (x >= -1 && x <= 1) && y <= 3) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                        } else {
                            if ((x * 7 + z * 11 + y) % 4 == 0) {
                                level.setBlock(p, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), 2);
                            } else {
                                level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos p = new BlockPos(cx + x, cy + 5, cz + z);
                if (x == 0 || z == 0 || Math.abs(x) == 4 || Math.abs(z) == 4) {
                    level.setBlock(p, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                } else {
                    level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        int[][] lampCoords = new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] coord : lampCoords) {
            level.setBlock(new BlockPos(cx + coord[0], cy + 4, cz + coord[1]), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + coord[0], cy + 3, cz + coord[1]), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }

        for (int z = -5; z <= 2; z++) {
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + z), Blocks.BLACK_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 1, cz + z), Blocks.BLACK_CARPET.defaultBlockState(), 2);
        }

        for (int x = -2; x <= 2; x++) {
            level.setBlock(new BlockPos(cx + x, cy + 1, cz + 3), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        }
        if (SystemBlocks.CLANDESTINE_CASHIER.isBound()) {
            level.setBlock(new BlockPos(cx, cy + 2, cz + 3), SystemBlocks.CLANDESTINE_CASHIER.get().defaultBlockState().setValue(ClandestineCashierBlock.FACING, Direction.SOUTH), 2);
        }

        spawnNpcIfAbsent(level, new BlockPos(cx, cy + 1, cz + 4), ModEntities.BLACK_MARKET_DEALER.get(), NpcProfile.BLACK_MARKET_DEALER);

        level.setBlock(new BlockPos(cx - 2, cy + 1, cz + 5), Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 1, cz + 5), Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), 2);
        level.setBlock(new BlockPos(cx + 2, cy + 1, cz + 5), Blocks.BOOKSHELF.defaultBlockState(), 2);

        BlockPos lootChestPos = new BlockPos(cx + 1, cy + 1, cz + 5);
        level.setBlock(lootChestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 2);
        BlockEntity chestBe = level.getBlockEntity(lootChestPos);
        if (chestBe instanceof Container chest) {
            if (SystemItems.CASINO_CHIP_BLACK.isBound()) {
                chest.setItem(0, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 4));
            }
            if (SystemItems.DARK_STAMINA_ELIXIR.isBound()) {
                chest.setItem(1, new ItemStack(SystemItems.DARK_STAMINA_ELIXIR.get(), 2));
            }
            chest.setItem(2, new ItemStack(Items.NETHERITE_SCRAP, 2));
            chest.setItem(3, new ItemStack(Items.GOLD_INGOT, 16));
            if (SystemItems.GOLDEN_GACHA_TICKET.isBound()) {
                chest.setItem(4, new ItemStack(SystemItems.GOLDEN_GACHA_TICKET.get(), 1));
            }
            chest.setItem(5, SystemItems.coin(5000));
        }

        placeIllegalPoolTable(level, new BlockPos(cx - 4, cy + 1, cz - 1), Direction.SOUTH);
        level.setBlock(new BlockPos(cx - 4, cy + 4, cz), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 4, cy + 3, cz), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        if (SystemBlocks.ILLEGAL_SLOT_MACHINE.isBound()) {
            BlockState front = SystemBlocks.ILLEGAL_SLOT_MACHINE.get().defaultBlockState()
                    .setValue(SlotMachineBlock.FACING, Direction.WEST)
                    .setValue(SlotMachineBlock.PART, SlotMachineBlock.SlotMachinePart.FRONT);
            BlockState back = SystemBlocks.ILLEGAL_SLOT_MACHINE.get().defaultBlockState()
                    .setValue(SlotMachineBlock.FACING, Direction.WEST)
                    .setValue(SlotMachineBlock.PART, SlotMachineBlock.SlotMachinePart.BACK);
            level.setBlock(new BlockPos(cx + 4, cy + 1, cz - 2), front, 2);
            level.setBlock(new BlockPos(cx + 5, cy + 1, cz - 2), back, 2);
            level.setBlock(new BlockPos(cx + 4, cy + 1, cz), front, 2);
            level.setBlock(new BlockPos(cx + 5, cy + 1, cz), back, 2);
        }
        if (SystemBlocks.CLANDESTINE_CHAIR.isBound()) {
            level.setBlock(new BlockPos(cx + 3, cy + 1, cz - 2), SystemBlocks.CLANDESTINE_CHAIR.get().defaultBlockState().setValue(CasinoChairBlock.FACING, Direction.EAST), 2);
            level.setBlock(new BlockPos(cx + 3, cy + 1, cz), SystemBlocks.CLANDESTINE_CHAIR.get().defaultBlockState().setValue(CasinoChairBlock.FACING, Direction.EAST), 2);
        }

        level.setBlock(new BlockPos(cx + 4, cy + 1, cz + 3), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 4, cy + 2, cz + 3), Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 3, cy + 1, cz + 3), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        level.setBlock(new BlockPos(cx - 1, cy + 1, cz - 6), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx - 1, cy + 2, cz - 6), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 1, cz - 6), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx + 1, cy + 2, cz - 6), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz - 6), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X), 2);
        level.setBlock(new BlockPos(cx, cy + 1, cz - 6), Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(new BlockPos(cx, cy + 2, cz - 6), Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);

        for (int z = -12; z <= -7; z++) {
            for (int x = -1; x <= 1; x++) {
                level.setBlock(new BlockPos(cx + x, cy, cz + z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                for (int y = 1; y <= 3; y++) {
                    level.setBlock(new BlockPos(cx + x, cy + y, cz + z), Blocks.AIR.defaultBlockState(), 2);
                }
                level.setBlock(new BlockPos(cx + x, cy + 4, cz + z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(cx, cy + 1, cz + z), Blocks.PURPLE_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, cy + 1, cz + z), Blocks.BLACK_CARPET.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, cy + 1, cz + z), Blocks.BLACK_CARPET.defaultBlockState(), 2);

            for (int y = 0; y <= 4; y++) {
                BlockState wallState = ((z + y) % 3 == 0) ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                level.setBlock(new BlockPos(cx - 2, cy + y, cz + z), wallState, 2);
                level.setBlock(new BlockPos(cx + 2, cy + y, cz + z), wallState, 2);
            }
        }
        level.setBlock(new BlockPos(cx, cy + 4, cz - 9), Blocks.CHAIN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(cx, cy + 3, cz - 9), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);

        buildBlackMarketSurfaceHut(level, cx, cy, cz);
    }

    public static void buildBlackMarketSurfaceHut(ServerLevel level, int cx, int cy, int cz) {
        // Buscar la altura máxima real de la superficie exterior en el perímetro 5x5 con acceso directo al cielo
        int surfaceY = 64;
        for (int x = -2; x <= 2; x++) {
            for (int z = -15; z <= -10; z++) {
                int h = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx + x, cz + z);
                if (h > surfaceY) {
                    surfaceY = h;
                }
            }
        }
        if (surfaceY < 62) {
            surfaceY = 64;
        }

        // 1. Pozo vertical continuo desde la compuerta de superficie hasta la guarida clandestina
        for (int y = cy + 1; y < surfaceY; y++) {
            level.setBlock(new BlockPos(cx, y, cz - 12), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, y, cz - 13), Blocks.AIR.defaultBlockState(), 2);

            level.setBlock(new BlockPos(cx, y, cz - 14), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx, y, cz - 13), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH), 2);

            level.setBlock(new BlockPos(cx - 1, y, cz - 12), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, y, cz - 12), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx - 1, y, cz - 13), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + 1, y, cz - 13), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);

            if (y > cy + 3) {
                level.setBlock(new BlockPos(cx, y, cz - 11), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            }

            if ((y - cy) % 6 == 0 && y < surfaceY - 1) {
                level.setBlock(new BlockPos(cx + 1, y, cz - 12), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
            }
        }

        // 2. Limpiar columna de aire despejada sobre toda la plataforma para evitar laderas, tierra o nieve
        for (int x = -2; x <= 2; x++) {
            for (int z = -15; z <= -10; z++) {
                for (int y = surfaceY + 1; y <= surfaceY + 6; y++) {
                    level.setBlock(new BlockPos(cx + x, y, cz + z), Blocks.AIR.defaultBlockState(), 2);
                }
                // Nivelar plataforma perimetral a ras de suelo
                if (x != 0 || (z != -12 && z != -13)) {
                    level.setBlock(new BlockPos(cx + x, surfaceY, cz + z), Blocks.COBBLED_DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        // Doble compuerta/trampilla en el piso
        level.setBlock(new BlockPos(cx, surfaceY, cz - 13), Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH).setValue(TrapDoorBlock.HALF, Half.BOTTOM).setValue(TrapDoorBlock.OPEN, false), 2);
        level.setBlock(new BlockPos(cx, surfaceY, cz - 12), Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH).setValue(TrapDoorBlock.HALF, Half.BOTTOM).setValue(TrapDoorBlock.OPEN, false), 2);

        // Registrar la ubicación de la compuerta exterior en los datos guardados
        if (level.getServer() != null) {
            SeriesStructureWorldGen.SeriesStructureData data = SeriesStructureWorldGen.SeriesStructureData.get(level.getServer());
            if (data != null) {
                data.recordStructure("black_market_surface_hatch", new BlockPos(cx, surfaceY, cz - 12));
            }
        }
    }

    public static void placeIllegalPoolTable(ServerLevel level, BlockPos masterPos, Direction facing) {
        if (!SystemBlocks.ILLEGAL_POOL_TABLE.isBound()) return;
        BlockState base = SystemBlocks.ILLEGAL_POOL_TABLE.get().defaultBlockState().setValue(PoolTableBlock.FACING, facing);
        for (PoolTablePart part : PoolTablePart.values()) {
            BlockPos partPos = PoolTablePart.getPartPos(masterPos, part, facing);
            level.setBlock(partPos, base.setValue(PoolTableBlock.PART, part), 3);
        }
    }
}
