package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Catacumbas Olvidadas: the mod's own dungeon, built straight into the Overworld.
 * <ul>
 *   <li>A ruined gatehouse on the surface, with a real staircase going down.</li>
 *   <li>Below, a maze of stone halls on a 4×4 grid joined by vaulted corridors, with several ways round:
 *       a library, an armoury, a prison, a crypt, a shrine, a storeroom, a fountain, a spider nest and
 *       small guard halls.</li>
 *   <li>On the far side, down a grand staircase, the boss's throne hall with its treasure.</li>
 * </ul>
 * The loot uses the Ruin Gate tables; their rank comes from the distance to the world spawn, like the
 * monster levels. The whole thing is sealed with its own walls, so caves and water it crosses stay out.
 * Chests carry a loot table, not items, so Lootr turns them into per-player chests.
 */
public final class ForgottenCatacombsBuilder {
    private static final int GRID = 4, CELL = 36;
    /** Outward direction of each side of the grid: north, east, south, west. */
    private static final int[][] SIDE_DIR = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
    private static final int BOSS_DROP = 8;

    private enum Theme { ENTRY, HALL, LIBRARY, ARMORY, PRISON, CRYPT, SHRINE, STORAGE, FOUNTAIN, SPIDER, BOSS }

    private static final class Room {
        final int x, z, fy, hx, hz, h;
        final Theme theme;

        Room(int x, int z, int fy, int hx, int hz, int h, Theme theme) {
            this.x = x; this.z = z; this.fy = fy; this.hx = hx; this.hz = hz; this.h = h; this.theme = theme;
        }

        boolean onPath(int px, int pz) {
            return Math.abs(px - x) <= 2 || Math.abs(pz - z) <= 2;
        }
    }

    private record Stair(BlockPos pos, Direction facing) {}

    private final ServerLevel level;
    private final Random rand;
    /** Planned open space; nothing touches the world until the plan is complete. */
    private final Set<Long> air = new LinkedHashSet<>();
    private final List<Stair> stairs = new ArrayList<>();
    /** Floors laid explicitly (the entrance, where it runs above the old ground). */
    private final List<BlockPos> laidFloor = new ArrayList<>();
    private final Map<Long, Integer> surface = new HashMap<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<BlockPos> corridorLights = new ArrayList<>();
    private ResourceKey<LootTable> chestLoot, bossLoot, supplies;
    private String rank;
    private int chests, spawners, monsters;

    private ForgottenCatacombsBuilder(ServerLevel level, long seed) {
        this.level = level;
        this.rand = new Random(seed);
    }

    private static ResourceKey<LootTable> loot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/ruin_gate/" + name));
    }

    /** Loot rank by distance to the world spawn, the same way the monsters get stronger further out. */
    static String rankFor(ServerLevel level, BlockPos pos) {
        double d = Math.sqrt(pos.distSqr(level.getSharedSpawnPos()));
        if (d < 700) return "E";
        if (d < 1500) return "D";
        if (d < 3000) return "C";
        if (d < 5000) return "B";
        if (d < 8000) return "A";
        return "S";
    }

    /** Builds the catacombs centred near {@code surface}; returns the top of the entrance stair, or null if they do not fit. */
    public static BlockPos build(ServerLevel level, BlockPos surface) {
        ForgottenCatacombsBuilder b = new ForgottenCatacombsBuilder(level, level.getSeed() ^ surface.asLong() ^ 0x5CA7AC0B5L);
        return b.buildAt(surface.getX(), surface.getZ());
    }

    private int height(int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
    }

    private BlockPos buildAt(int ox, int oz) {
        // ------------------------------------------------------------ 1. depth: well under the lowest ground of the footprint
        int half = (GRID * CELL) / 2 + 6, lowest = Integer.MAX_VALUE;
        for (int sx = -half; sx <= half; sx += 16) for (int sz = -half; sz <= half; sz += 16) lowest = Math.min(lowest, height(ox + sx, oz + sz));
        int base = Math.max(level.getMinBuildHeight() + 16, lowest - 14);
        if (lowest - base < 10) return null;
        rank = rankFor(level, new BlockPos(ox, base, oz));
        String r = rank.toLowerCase(java.util.Locale.ROOT);
        chestLoot = loot(r);
        bossLoot = loot(r + "_boss");
        supplies = loot("supplies");

        // ------------------------------------------------------------ 2. layout
        int side = rand.nextInt(4), opp = (side + 2) % 4;
        int[] entryCell = cellOnSide(side, 1 + rand.nextInt(2));
        int[] bossCell = cellOnSide(opp, 1 + rand.nextInt(2));
        int[] bossDoor = {bossCell[0] - SIDE_DIR[opp][0], bossCell[1] - SIDE_DIR[opp][1]};   // the room the boss hall opens from
        List<Theme> themes = new ArrayList<>(List.of(Theme.LIBRARY, Theme.ARMORY, Theme.PRISON, Theme.CRYPT, Theme.SHRINE,
                Theme.STORAGE, Theme.FOUNTAIN, Theme.SPIDER));
        Collections.shuffle(themes, rand);
        List<int[]> free = new ArrayList<>();
        for (int gx = 0; gx < GRID; gx++) for (int gz = 0; gz < GRID; gz++) {
            if (same(entryCell, gx, gz) || same(bossCell, gx, gz)) continue;
            free.add(new int[]{gx, gz});
        }
        Collections.shuffle(free, rand);
        Theme[][] theme = new Theme[GRID][GRID];
        for (int[] c : free) theme[c[0]][c[1]] = Theme.HALL;
        for (int i = 0; i < themes.size() && i < free.size(); i++) theme[free.get(i)[0]][free.get(i)[1]] = themes.get(i);
        theme[entryCell[0]][entryCell[1]] = Theme.ENTRY;
        theme[bossCell[0]][bossCell[1]] = Theme.BOSS;

        Room[][] grid = new Room[GRID][GRID];
        Room entry = null, boss = null;
        for (int gx = 0; gx < GRID; gx++) for (int gz = 0; gz < GRID; gz++) {
            Theme t = theme[gx][gz];
            int x = ox + (int) Math.round((gx - (GRID - 1) / 2.0) * CELL);
            int z = oz + (int) Math.round((gz - (GRID - 1) / 2.0) * CELL);
            Room room = switch (t) {
                case ENTRY -> new Room(x, z, base, 6, 6, 8, t);
                case BOSS -> new Room(x + SIDE_DIR[opp][0] * 6, z + SIDE_DIR[opp][1] * 6, base - BOSS_DROP, 16, 16, 16, t);
                case HALL -> new Room(x, z, base, 5, 5, 7, t);
                default -> new Room(x, z, base, 9 + rand.nextInt(3), 9 + rand.nextInt(3), 9 + rand.nextInt(3), t);
            };
            if (same(bossDoor, gx, gz) && t != Theme.ENTRY) {
                // keep the corridor down to the boss long enough for its staircase
                room = new Room(room.x, room.z, room.fy, Math.min(room.hx, 9), Math.min(room.hz, 9), room.h, room.theme);
            }
            grid[gx][gz] = room;
            rooms.add(room);
            if (t == Theme.ENTRY) entry = room;
            if (t == Theme.BOSS) boss = room;
        }

        // ------------------------------------------------------------ 3. links: a spanning tree with some loops; the boss hall is a dead end
        List<int[]> links = new ArrayList<>();
        for (int gx = 0; gx < GRID; gx++) for (int gz = 0; gz < GRID; gz++) {
            if (gx + 1 < GRID) links.add(new int[]{gx, gz, gx + 1, gz});
            if (gz + 1 < GRID) links.add(new int[]{gx, gz, gx, gz + 1});
        }
        links.removeIf(l -> same(bossCell, l[0], l[1]) || same(bossCell, l[2], l[3]));
        Collections.shuffle(links, rand);
        int[] parent = new int[GRID * GRID];
        for (int i = 0; i < parent.length; i++) parent[i] = i;
        List<int[]> used = new ArrayList<>();
        for (int[] l : links) {
            int a = find(parent, l[0] * GRID + l[1]), b = find(parent, l[2] * GRID + l[3]);
            if (a != b) { parent[a] = b; used.add(l); }
            else if (rand.nextInt(100) < 35) used.add(l);
        }

        // ------------------------------------------------------------ 4. plan the open space
        for (Room rm : rooms) planRoom(rm);
        for (int[] l : used) planLink(grid[l[0]][l[1]], grid[l[2]][l[3]]);
        planLink(grid[bossDoor[0]][bossDoor[1]], boss);
        BlockPos top = planEntrance(entry, side);

        // ------------------------------------------------------------ 5. into the world: walls first, then the open space
        for (long l : air) {
            BlockPos p = BlockPos.of(l);
            surface.computeIfAbsent(columnKey(p.getX(), p.getZ()), k -> height(p.getX(), p.getZ()));
        }
        for (long l : air) {
            BlockPos p = BlockPos.of(l);
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (air.contains(n.asLong())) continue;
                int ground = surface.computeIfAbsent(columnKey(n.getX(), n.getZ()), k -> height(n.getX(), n.getZ()));
                if (n.getY() >= ground && d != Direction.DOWN) continue;   // open sky over the entrance stays open
                set(n, d == Direction.DOWN ? floorBlock(n) : d == Direction.UP ? ceilingBlock(n) : wallBlock(n));
            }
        }
        for (long l : air) set(BlockPos.of(l), Blocks.AIR.defaultBlockState());
        for (BlockPos p : laidFloor) set(p, floorBlock(p));
        for (Stair s : stairs) set(s.pos, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, s.facing));

        // ------------------------------------------------------------ 6. rooms, lights, the gatehouse
        for (Room rm : rooms) furnish(rm);
        for (BlockPos p : corridorLights) hangLantern(p);
        gatehouse(top, side);

        boolean ok = walkCheck(top, new BlockPos(boss.x, boss.fy, boss.z));
        if (System.getProperty("awakening.debugGrotto") != null) debugPlan(ox, oz, base);
        AwakeningSystemMod.LOGGER.info("[Catacombs] built at {} (rank {}, floor y={}): {} rooms, {} chests, {} spawners, {} monsters, entrance {}, boss hall reachable: {}",
                new BlockPos(ox, base, oz).toShortString(), rank, base, rooms.size(), chests, spawners, monsters, top.toShortString(), ok);
        return top;
    }

    // ---------------------------------------------------------------- planning
    private int[] cellOnSide(int side, int i) {
        return switch (side) {
            case 0 -> new int[]{i, 0};
            case 1 -> new int[]{GRID - 1, i};
            case 2 -> new int[]{i, GRID - 1};
            default -> new int[]{0, i};
        };
    }

    private static boolean same(int[] c, int gx, int gz) {
        return c[0] == gx && c[1] == gz;
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) i = parent[i] = parent[parent[i]];
        return i;
    }

    private static long columnKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    /** A room is a box with a stepped vault: the top two layers draw in from the walls. */
    private void planRoom(Room r) {
        for (int dy = 0; dy < r.h; dy++) {
            int inset = dy == r.h - 1 ? 2 : dy == r.h - 2 ? 1 : 0;
            for (int dx = -r.hx + inset; dx <= r.hx - inset; dx++)
                for (int dz = -r.hz + inset; dz <= r.hz - inset; dz++) air.add(BlockPos.asLong(r.x + dx, r.fy + dy, r.z + dz));
        }
    }

    /** A straight vaulted corridor between two grid neighbours, with a staircase if their floors differ. */
    private void planLink(Room a, Room b) {
        if (a.fy < b.fy) { Room t = a; a = b; b = t; }   // walk from the higher room down to the lower one
        boolean alongX = a.z == b.z || Math.abs(a.x - b.x) > Math.abs(a.z - b.z);
        int step = alongX ? Integer.signum(b.x - a.x) : Integer.signum(b.z - a.z);
        int from = alongX ? a.x + step * (a.hx - 1) : a.z + step * (a.hz - 1);
        int to = alongX ? b.x - step * (b.hx - 1) : b.z - step * (b.hz - 1);
        int line = alongX ? a.z : a.x;
        Direction travel = alongX ? (step > 0 ? Direction.EAST : Direction.WEST) : (step > 0 ? Direction.SOUTH : Direction.NORTH);
        int n = Math.abs(to - from) + 1, drop = a.fy - b.fy;
        int k0 = n - 3 - drop;   // the staircase ends two blocks before the lower room
        for (int i = 0; i < n; i++) {
            int c = from + step * i;
            int d = Math.max(0, Math.min(drop, i - k0));
            int prev = i == 0 ? 0 : Math.max(0, Math.min(drop, i - 1 - k0));
            int feet = a.fy - d;
            boolean stair = d > prev;
            for (int w = -2; w <= 2; w++) {
                int top = 6 - Math.abs(w);
                for (int dy = 0; dy < top + (stair ? 1 : 0); dy++) {
                    int x = alongX ? c : line + w, z = alongX ? line + w : c;
                    air.add(BlockPos.asLong(x, feet + dy, z));
                }
                if (stair) stairs.add(new Stair(alongX ? new BlockPos(c, feet, line + w) : new BlockPos(line + w, feet, c), travel.getOpposite()));
            }
            if (i % 5 == 2 && !stair) corridorLights.add(alongX ? new BlockPos(c, feet + 5, line) : new BlockPos(line, feet + 5, c));
        }
    }

    /** The way in: a corridor from the entry room out under the ground, then a staircase up to the surface. */
    private BlockPos planEntrance(Room entry, int side) {
        int dx = SIDE_DIR[side][0], dz = SIDE_DIR[side][1];
        int edgeX = entry.x + dx * entry.hx, edgeZ = entry.z + dz * entry.hz;
        // the shortest run whose far end is far enough out to climb back to that spot's ground
        int len = 12, ground = 0;
        for (; len < 120; len++) {
            ground = height(edgeX + dx * len, edgeZ + dz * len);
            if (len >= (ground - entry.fy) + 5) break;
        }
        int drop = Math.max(0, ground - entry.fy);
        Direction travel = Direction.fromDelta(-dx, 0, -dz);   // walking in, towards the room
        for (int i = 0; i <= len; i++) {
            int x = edgeX + dx * (len - i), z = edgeZ + dz * (len - i);
            int d = Math.max(0, Math.min(drop, i - 2));
            int prev = i == 0 ? 0 : Math.max(0, Math.min(drop, i - 3));
            int feet = ground - d;
            boolean stair = d > prev;
            for (int w = -2; w <= 2; w++) {
                int wx = x + (dz != 0 ? w : 0), wz = z + (dx != 0 ? w : 0);
                int top = 6 - Math.abs(w);
                for (int dy = 0; dy < top + (stair ? 1 : 0); dy++) air.add(BlockPos.asLong(wx, feet + dy, wz));
                if (stair) stairs.add(new Stair(new BlockPos(wx, feet, wz), travel.getOpposite()));
                else laidFloor.add(new BlockPos(wx, feet - 1, wz));
            }
            if (i % 5 == 4 && !stair) corridorLights.add(new BlockPos(x, feet + 5, z));
        }
        return new BlockPos(edgeX + dx * len, ground, edgeZ + dz * len);
    }

    // ---------------------------------------------------------------- palette
    private int hash(BlockPos p) {
        long h = p.asLong() * 0x9E3779B97F4A7C15L;
        return (int) ((h >>> 33) & 0x7FFFFFFF);
    }

    private BlockState wallBlock(BlockPos p) {
        int h = hash(p) % 100;
        if (h < 58) return Blocks.STONE_BRICKS.defaultBlockState();
        if (h < 74) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (h < 88) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        if (h < 95) return Blocks.ANDESITE.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private BlockState floorBlock(BlockPos p) {
        int h = hash(p) % 100;
        if (h < 45) return Blocks.STONE_BRICKS.defaultBlockState();
        if (h < 70) return Blocks.POLISHED_ANDESITE.defaultBlockState();
        if (h < 85) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (h < 95) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private BlockState ceilingBlock(BlockPos p) {
        return hash(p) % 100 < 80 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    }

    // ---------------------------------------------------------------- furnishing
    private void furnish(Room r) {
        // a hanging lantern from the top of every vault, and one over each corner of the bigger rooms
        hangLantern(new BlockPos(r.x, r.fy + r.h - 1, r.z));
        if (r.hx >= 5 && r.hz >= 5 && r.theme != Theme.SPIDER) {
            for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2)
                hangLantern(new BlockPos(r.x + sx * (r.hx - 2), r.fy + r.h - 1, r.z + sz * (r.hz - 2)));
        }
        switch (r.theme) {
            case ENTRY -> { pillars(r, Blocks.CHISELED_STONE_BRICKS.defaultBlockState()); cobwebs(r, 4); }
            case HALL -> {
                pillars(r, Blocks.STONE_BRICK_WALL.defaultBlockState());
                cobwebs(r, 3);
                if (rand.nextInt(3) != 0) spawnerIn(r, rand.nextBoolean() ? EntityType.ZOMBIE : EntityType.SPIDER);
            }
            case LIBRARY -> { library(r); spawnerIn(r, EntityType.SKELETON); }
            case ARMORY -> armory(r);
            case PRISON -> prison(r);
            case CRYPT -> crypt(r);
            case SHRINE -> shrine(r);
            case STORAGE -> { storage(r); spawnerIn(r, EntityType.ZOMBIE); }
            case FOUNTAIN -> fountain(r);
            case SPIDER -> spiderNest(r);
            case BOSS -> bossHall(r);
        }
        // a few monsters already waiting in every room, on top of what the spawners bring
        if (r.theme != Theme.BOSS && r.theme != Theme.ENTRY) {
            EntityType<?>[] pool = switch (r.theme) {
                case SPIDER -> new EntityType<?>[]{EntityType.CAVE_SPIDER, EntityType.SPIDER};
                case CRYPT, PRISON -> new EntityType<?>[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.HUSK};
                case FOUNTAIN -> new EntityType<?>[]{EntityType.DROWNED, EntityType.ZOMBIE};
                default -> new EntityType<?>[]{EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER};
            };
            int count = r.theme == Theme.HALL ? 2 + rand.nextInt(2) : 3 + rand.nextInt(3);
            for (int i = 0; i < count; i++) {
                BlockPos p = spotIn(r, false);
                if (p != null) monster(pool[rand.nextInt(pool.length)], p);
            }
        }
    }

    private void monster(EntityType<?> type, BlockPos p) {
        if (!(type.create(level) instanceof net.minecraft.world.entity.Mob mob)) return;
        mob.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rand.nextFloat() * 360F, 0);
        net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(p), MobSpawnType.STRUCTURE, null);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        monsters++;
    }

    /** Cells of the room's floor (the feet level), optionally only those along the walls. */
    private List<BlockPos> cells(Room r, boolean wallsOnly) {
        List<BlockPos> out = new ArrayList<>();
        for (int dx = -r.hx; dx <= r.hx; dx++) for (int dz = -r.hz; dz <= r.hz; dz++) {
            boolean wall = Math.abs(dx) == r.hx || Math.abs(dz) == r.hz;
            if (wallsOnly && !wall) continue;
            int x = r.x + dx, z = r.z + dz;
            if (r.onPath(x, z)) continue;
            out.add(new BlockPos(x, r.fy, z));
        }
        return out;
    }

    private BlockPos spotIn(Room r, boolean wallsOnly) {
        List<BlockPos> c = cells(r, wallsOnly);
        Collections.shuffle(c, rand);
        for (BlockPos p : c) if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) return p;
        return null;
    }

    /** Facing away from the nearest wall, so chests and barrels open towards the room. */
    private Direction inward(Room r, BlockPos p) {
        int dx = p.getX() - r.x, dz = p.getZ() - r.z;
        if (Math.abs(dx) * r.hz >= Math.abs(dz) * r.hx) return dx > 0 ? Direction.WEST : Direction.EAST;
        return dz > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    private void pillars(Room r, BlockState state) {
        for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) {
            int x = r.x + sx * (r.hx - 1), z = r.z + sz * (r.hz - 1);
            // up to the vault: the top layer of the room draws in by two, so the pillar meets the ceiling there
            for (int dy = 0; dy < r.h - 1; dy++) set(new BlockPos(x, r.fy + dy, z), state);
        }
    }

    private void cobwebs(Room r, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos p = spotIn(r, true);
            if (p != null) set(p.above(1 + rand.nextInt(Math.max(1, r.h - 3))), Blocks.COBWEB.defaultBlockState());
        }
    }

    private void library(Room r) {
        for (BlockPos p : cells(r, true)) {
            if (rand.nextInt(6) == 0) continue;
            set(p, Blocks.BOOKSHELF.defaultBlockState());
            set(p.above(), rand.nextInt(4) == 0 ? Blocks.CHISELED_BOOKSHELF.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState());
        }
        for (int i = 0; i < 2; i++) {
            BlockPos p = spotIn(r, false);
            if (p != null) set(p, Blocks.LECTERN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.from2DDataValue(rand.nextInt(4))));
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) set(new BlockPos(r.x + dx, r.fy, r.z + dz), Blocks.RED_CARPET.defaultBlockState());
        chestIn(r, false);
        cobwebs(r, 2);
    }

    private void armory(Room r) {
        BlockState[] stations = {Blocks.ANVIL.defaultBlockState(), Blocks.GRINDSTONE.defaultBlockState(), Blocks.SMITHING_TABLE.defaultBlockState(),
                Blocks.BLAST_FURNACE.defaultBlockState(), Blocks.CHIPPED_ANVIL.defaultBlockState()};
        for (BlockState st : stations) {
            BlockPos p = spotIn(r, true);
            if (p != null) set(p, st);
        }
        for (int i = 0; i < 3; i++) {
            BlockPos p = spotIn(r, true);
            if (p != null) barrel(p, inward(r, p), supplies);
        }
        for (int i = 0; i < 2; i++) {
            BlockPos p = spotIn(r, false);
            if (p == null) continue;
            ArmorStand stand = new ArmorStand(level, p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            stand.setYRot(rand.nextFloat() * 360F);
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(rand.nextBoolean() ? Items.IRON_HELMET : Items.CHAINMAIL_HELMET));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(rand.nextBoolean() ? Items.IRON_CHESTPLATE : Items.CHAINMAIL_CHESTPLATE));
            level.addFreshEntity(stand);
        }
        chestIn(r, true);
        spawnerIn(r, EntityType.SKELETON);
    }

    private void prison(Room r) {
        // cells of iron bars along the two long walls, each with a skull or a cobweb
        boolean alongX = r.hx >= r.hz;
        for (int s = -1; s <= 1; s += 2) {
            for (int a = -(alongX ? r.hx : r.hz); a <= (alongX ? r.hx : r.hz); a++) {
                int x = alongX ? r.x + a : r.x + s * (r.hx - 2);
                int z = alongX ? r.z + s * (r.hz - 2) : r.z + a;
                if (r.onPath(x, z)) continue;
                BlockState bars = a % 3 == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.IRON_BARS.defaultBlockState();
                for (int dy = 0; dy < 3; dy++) set(new BlockPos(x, r.fy + dy, z), bars);
                int cx = alongX ? x : r.x + s * r.hx, cz = alongX ? r.z + s * r.hz : z;
                if (rand.nextInt(4) == 0) set(new BlockPos(cx, r.fy, cz), Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rand.nextInt(16)));
                else if (rand.nextInt(3) == 0) set(new BlockPos(cx, r.fy + 1, cz), Blocks.COBWEB.defaultBlockState());
            }
        }
        for (int i = 0; i < 3; i++) set(new BlockPos(r.x + rand.nextInt(3) - 1, r.fy + r.h - 2, r.z + rand.nextInt(3) - 1), Blocks.CHAIN.defaultBlockState());
        spawnerIn(r, EntityType.ZOMBIE);
    }

    private void crypt(Room r) {
        // rows of stone coffins with candles at their heads
        for (int dx = -r.hx + 2; dx <= r.hx - 2; dx += 3) for (int dz = -r.hz + 1; dz <= r.hz - 1; dz++) {
            int x = r.x + dx, z = r.z + dz;
            if (r.onPath(x, z) || Math.abs(dz) == r.hz - 1 && rand.nextBoolean()) continue;
            set(new BlockPos(x, r.fy, z), Blocks.STONE_BRICK_SLAB.defaultBlockState());
            if (Math.abs(dz) == r.hz - 1)
                set(new BlockPos(x, r.fy + 1, z), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1 + rand.nextInt(3)).setValue(CandleBlock.LIT, true));
        }
        for (int i = 0; i < 4; i++) {
            BlockPos p = spotIn(r, true);
            if (p != null) set(p, rand.nextBoolean() ? Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rand.nextInt(16))
                    : Blocks.SOUL_LANTERN.defaultBlockState());
        }
        chestIn(r, true);
        spawnerIn(r, rand.nextBoolean() ? EntityType.SKELETON : EntityType.HUSK);
        cobwebs(r, 3);
    }

    private void shrine(Room r) {
        pillars(r, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        // an altar off the centre, against the wall away from the doors
        BlockPos altar = new BlockPos(r.x + r.hx - 2, r.fy, r.z + r.hz - 2);
        set(altar, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        set(altar.above(), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos s = altar.relative(d);
            if (!r.onPath(s.getX(), s.getZ())) set(s, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, d.getOpposite()));
        }
        placeChest(altar.offset(-2, 0, 0), Direction.EAST, chestLoot);
        for (int i = 0; i < 4; i++) {
            BlockPos p = spotIn(r, true);
            if (p != null) set(p, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1 + rand.nextInt(4)).setValue(CandleBlock.LIT, true));
        }
    }

    private void storage(Room r) {
        for (BlockPos p : cells(r, true)) {
            int roll = rand.nextInt(10);
            if (roll < 3) barrel(p, inward(r, p), roll == 0 ? supplies : null);
            else if (roll == 3) set(p, Blocks.HAY_BLOCK.defaultBlockState());
            else if (roll == 4) set(p, Blocks.COMPOSTER.defaultBlockState());
        }
        chestIn(r, true);
        cobwebs(r, 3);
    }

    private void fountain(Room r) {
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            BlockPos p = new BlockPos(r.x + dx, r.fy, r.z + dz);
            boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 3;
            set(p, rim ? Blocks.STONE_BRICK_SLAB.defaultBlockState() : Blocks.WATER.defaultBlockState());
            if (!rim) set(p.below(), Blocks.STONE_BRICKS.defaultBlockState());
        }
        // a statue in the middle of the pool (no water on top: it would spill over the rim into the room)
        for (int dy = 0; dy < 3; dy++) set(new BlockPos(r.x, r.fy + dy, r.z), dy == 2 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICK_WALL.defaultBlockState());
        pillars(r, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
        for (int i = 0; i < 6; i++) {
            BlockPos p = spotIn(r, true);
            if (p != null) set(p, Blocks.MOSS_CARPET.defaultBlockState());
        }
    }

    private void spiderNest(Room r) {
        for (int dx = -r.hx; dx <= r.hx; dx++) for (int dz = -r.hz; dz <= r.hz; dz++) for (int dy = 0; dy < r.h - 1; dy++) {
            int x = r.x + dx, z = r.z + dz;
            if (r.onPath(x, z) && dy < 3) continue;
            if (rand.nextInt(dy == 0 ? 6 : 9) == 0) {
                BlockPos p = new BlockPos(x, r.fy + dy, z);
                if (level.getBlockState(p).isAir()) set(p, Blocks.COBWEB.defaultBlockState());
            }
        }
        chestIn(r, true);
        spawnerIn(r, EntityType.CAVE_SPIDER);
    }

    /** The throne hall: two rows of great pillars, braziers, a dais with the throne and the treasure behind it. */
    private void bossHall(Room r) {
        // the way in arrives on one of the hall's centre lines, so the pillars keep off both of them
        for (int dz : new int[]{-12, -5, 5, 12}) for (int sx = -1; sx <= 1; sx += 2) {
            int x = r.x + sx * (r.hx - 3), z = r.z + dz;
            // floor to ceiling: this far in, every layer of the vault is open above the pillar
            for (int dy = 0; dy < r.h; dy++) set(new BlockPos(x, r.fy + dy, z), dy == 0 || dy == r.h - 1
                    ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            set(new BlockPos(x + -sx, r.fy, z), Blocks.SOUL_CAMPFIRE.defaultBlockState());
        }
        for (int dx = -r.hx + 1; dx <= r.hx - 1; dx++) for (int dz = -r.hz + 1; dz <= r.hz - 1; dz++)
            if (Math.abs(dx) <= 2 || (dx + dz) % 5 == 0) set(new BlockPos(r.x + dx, r.fy - 1, r.z + dz), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // the dais sits against the wall opposite the way in
        BlockPos door = nearestDoor(r);
        int ddx = r.x - door.getX(), ddz = r.z - door.getZ();
        Direction in = Math.abs(ddx) >= Math.abs(ddz) ? (ddx >= 0 ? Direction.EAST : Direction.WEST) : (ddz >= 0 ? Direction.SOUTH : Direction.NORTH);
        BlockPos back = new BlockPos(r.x + in.getStepX() * (r.hx - 2), r.fy, r.z + in.getStepZ() * (r.hz - 2));
        Direction across = in.getClockWise();
        for (int w = -2; w <= 2; w++) for (int k = 0; k <= 1; k++) {
            BlockPos p = back.relative(across, w).relative(in.getOpposite(), k);
            set(p, k == 1 ? Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, in) : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        BlockPos throne = back.above();
        set(throne, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, in));
        set(throne.relative(in), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        set(throne.relative(in).above(), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
        placeChest(back.relative(across, 2).above(), in.getOpposite(), bossLoot);
        placeChest(back.relative(across, -2).above(), in.getOpposite(), chestLoot);
        for (int w = -1; w <= 1; w += 2) set(back.relative(across, w).above(), Blocks.SOUL_LANTERN.defaultBlockState());
        for (int i = 0; i < 4; i++) set(new BlockPos(r.x + rand.nextInt(9) - 4, r.fy + r.h - 2, r.z + rand.nextInt(9) - 4), Blocks.CHAIN.defaultBlockState());
        // the Skeleton Lord stands sealed in crystal before its throne, facing the way in
        spawnBoss(back.relative(in.getOpposite(), 4), in.getOpposite().toYRot(), new BlockPos(r.x, r.fy, r.z));
    }

    /** A point on the corridor that leads into the room (its staircase), to tell which side the way in is. */
    private BlockPos nearestDoor(Room r) {
        BlockPos best = new BlockPos(r.x, r.fy, r.z);
        double bestD = Double.MAX_VALUE;
        for (Stair s : stairs) {
            double d = s.pos.distSqr(best);
            if (d < bestD && s.pos.getY() < r.fy + BOSS_DROP + 2) { bestD = d; best = s.pos; }
        }
        return best;
    }

    /**
     * The boss: the Skeleton Lord, waiting as a crystal statue until someone comes close. Held to its hall
     * (around {@code centre}) like the other bosses.
     */
    private void spawnBoss(BlockPos pos, float yaw, BlockPos centre) {
        var boss = com.soybe.awakeningsystem.entity.CatacombEntities.SKELETON_LORD.get().create(level);
        if (boss == null) return;
        boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        boss.setYHeadRot(yaw);
        boss.setYBodyRot(yaw);
        net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(boss, level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        boss.setPersistenceRequired();
        CompoundTag tag = boss.getPersistentData();
        tag.putDouble("arena_center_x", centre.getX() + 0.5);
        tag.putDouble("arena_center_y", centre.getY());
        tag.putDouble("arena_center_z", centre.getZ() + 0.5);
        tag.putDouble("arena_max_radius", 15.0);
        tag.putBoolean("is_overworld_miniboss", true);
        boolean added = level.addFreshEntity(boss);
        AwakeningSystemMod.LOGGER.info("[Catacombs] Skeleton Lord at {}: {}", pos.toShortString(), added ? "waiting in its crystal" : "could not be placed");
    }

    private void hangLantern(BlockPos p) {
        if (!level.getBlockState(p).isAir() || level.getBlockState(p.above()).isAir()) return;
        set(p, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    private void chestIn(Room r, boolean wallsOnly) {
        BlockPos p = spotIn(r, wallsOnly);
        if (p != null) placeChest(p, inward(r, p), chestLoot);
    }

    private void spawnerIn(Room r, EntityType<?> type) {
        BlockPos p = spotIn(r, false);
        if (p == null) return;
        set(p, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(p) instanceof SpawnerBlockEntity s) {
            s.setEntityId(type, level.getRandom());
            spawners++;
        }
    }

    private void placeChest(BlockPos p, Direction facing, ResourceKey<LootTable> table) {
        level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        if (level.getBlockEntity(p) instanceof ChestBlockEntity chest) {
            chest.setLootTable(table, rand.nextLong());
            chests++;
        }
    }

    private void barrel(BlockPos p, Direction facing, ResourceKey<LootTable> table) {
        set(p, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing));
        if (table != null && level.getBlockEntity(p) instanceof BarrelBlockEntity b) b.setLootTable(table, rand.nextLong());
    }

    // ---------------------------------------------------------------- the ruin on the surface
    private void gatehouse(BlockPos top, int side) {
        int dx = SIDE_DIR[side][0], dz = SIDE_DIR[side][1];
        int px = dz != 0 ? 1 : 0, pz = dx != 0 ? 1 : 0;   // across the stair
        Direction in = Direction.fromDelta(-dx, 0, -dz);
        for (int i = 0; i <= 8; i++) {
            int cx = top.getX() - dx * i, cz = top.getZ() - dz * i;
            int ground = top.getY();
            for (int s = -1; s <= 1; s += 2) {
                int wx = cx + px * s * 3, wz = cz + pz * s * 3;
                boolean pillar = i == 0 || i == 8;
                int hgt = pillar ? 7 : 3 + rand.nextInt(4);
                // walls rest on solid ground: fill down to it first
                for (int y = ground - 1; y > ground - 6 && !level.getBlockState(new BlockPos(wx, y, wz)).isSolid(); y--) set(new BlockPos(wx, y, wz), wallBlock(new BlockPos(wx, y, wz)));
                for (int y = 0; y < hgt; y++) {
                    BlockPos p = new BlockPos(wx, ground + y, wz);
                    set(p, pillar ? (y == hgt - 1 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState()) : wallBlock(p));
                }
                if (pillar && i == 0) set(new BlockPos(wx, ground + hgt, wz), Blocks.LANTERN.defaultBlockState());
            }
        }
        // the lintel over the way in, and broken roof slabs further in
        for (int s = -3; s <= 3; s++) {
            set(new BlockPos(top.getX() + px * s, top.getY() + 6, top.getZ() + pz * s), Math.abs(s) == 3
                    ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                    : Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, in).setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
            for (int i = 1; i <= 7; i++) if (rand.nextInt(3) == 0)
                set(new BlockPos(top.getX() - dx * i + px * s, top.getY() + 6, top.getZ() - dz * i + pz * s), Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }
        // rubble and a worn path in front
        for (int i = 0; i < 10; i++) {
            int rx = top.getX() + dx * (1 + rand.nextInt(4)) + px * (rand.nextInt(7) - 3);
            int rz = top.getZ() + dz * (1 + rand.nextInt(4)) + pz * (rand.nextInt(7) - 3);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, rx, rz);
            BlockPos p = new BlockPos(rx, y, rz);
            if (level.getBlockState(p.below()).isSolid() && level.getBlockState(p).canBeReplaced())
                set(rand.nextBoolean() ? p.below() : p, rand.nextBoolean() ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
        }
    }

    /** Dev aid: a top-down picture of the floor plan (lowest open block of each column) in the server folder. */
    private void debugPlan(int ox, int oz, int base) {
        try {
            int r = 110, s = 3;
            java.awt.image.BufferedImage plan = new java.awt.image.BufferedImage(2 * r * s, 2 * r * s, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Map<Long, Integer> low = new HashMap<>();
            for (long l : air) {
                BlockPos p = BlockPos.of(l);
                low.merge(columnKey(p.getX(), p.getZ()), p.getY(), Math::min);
            }
            for (int x = -r; x < r; x++) for (int z = -r; z < r; z++) {
                int col = 0x202020;
                Integer y = low.get(columnKey(ox + x, oz + z));
                if (y != null) {
                    BlockPos p = new BlockPos(ox + x, y, oz + z);
                    BlockState here = level.getBlockState(p);
                    BlockPos shown = here.isAir() ? p.below() : p;
                    col = level.getBlockState(shown).getMapColor(level, shown).col;
                    if (here.is(Blocks.CHEST)) col = 0xFFE040;
                    else if (here.is(Blocks.SPAWNER)) col = 0xFF40FF;
                    double f = Math.max(0.5, Math.min(1.3, 1.0 + (y - base) * 0.05));
                    col = ((int) Math.min(255, ((col >> 16) & 255) * f) << 16) | ((int) Math.min(255, ((col >> 8) & 255) * f) << 8) | (int) Math.min(255, (col & 255) * f);
                }
                for (int i = 0; i < s; i++) for (int j = 0; j < s; j++) plan.setRGB((x + r) * s + i, (z + r) * s + j, col);
            }
            java.io.File dir = level.getServer().getServerDirectory().resolve("catacombs_debug").toFile();
            dir.mkdirs();
            javax.imageio.ImageIO.write(plan, "png", new java.io.File(dir, "plan.png"));
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("[Catacombs] debug plan failed", e);
        }
    }

    // ---------------------------------------------------------------- writing and checking
    private void set(BlockPos p, BlockState state) {
        if (p.getY() <= level.getMinBuildHeight()) return;
        level.setBlock(p, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    private boolean free(BlockPos p) {
        return level.getBlockState(p).getCollisionShape(level, p).isEmpty() && level.getBlockState(p).getFluidState().isEmpty();
    }

    private boolean standable(BlockPos p) {
        return free(p) && free(p.above()) && !level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty();
    }

    /** Walks from the surface to the boss hall (one-block steps) to make sure the way through is open. */
    private boolean walkCheck(BlockPos from, BlockPos to) {
        BlockPos start = null;
        for (int dy = 2; dy >= -2 && start == null; dy--) if (standable(from.above(dy))) start = from.above(dy);
        if (start == null) { AwakeningSystemMod.LOGGER.warn("[Catacombs] walk check: nowhere to stand at the entrance {}", from.toShortString()); return false; }
        Set<Long> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start.asLong());
        int limit = 200000;
        while (!queue.isEmpty() && limit-- > 0) {
            BlockPos p = queue.poll();
            if (Math.abs(p.getX() - to.getX()) <= 2 && Math.abs(p.getZ() - to.getZ()) <= 2 && Math.abs(p.getY() - to.getY()) <= 1) return true;
            for (Direction d : Direction.Plane.HORIZONTAL) for (int dy = 1; dy >= -1; dy--) {
                BlockPos n = p.relative(d).above(dy);
                if (seen.contains(n.asLong()) || !standable(n)) continue;
                if (dy == 1 && !free(p.above(2))) continue;   // head room for the step up
                seen.add(n.asLong());
                queue.add(n);
            }
        }
        AwakeningSystemMod.LOGGER.warn("[Catacombs] walk check: the boss hall at {} cannot be reached ({} spots visited)", to.toShortString(), seen.size());
        return false;
    }
}
