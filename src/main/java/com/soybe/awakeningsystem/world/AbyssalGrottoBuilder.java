package com.soybe.awakeningsystem.world;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TeyvatCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Gruta Abisal (roadmap sección 20): una red de cuevas que se encuentra explorando el mundo, sin puerta.
 * <ul>
 *   <li>La entrada es una boca al pie de un peñasco, con un túnel que baja en diagonal bajo la roca.</li>
 *   <li>Abajo hay una red de túneles anchos y sinuosos sobre una cuadrícula de 4×4 salas: hay varios
 *       caminos, vueltas y bloques de roca entre ellos, y la red baja a medida que se aleja de la entrada.</li>
 *   <li>Nueve salas temáticas (cristales, geoda, nido de arañas, osario, setas, lago, lava, campamento
 *       minero y cripta), cruces pequeños, callejones sin salida con un cofre escondido y, en la esquina
 *       más lejana y profunda, la arena del jefe.</li>
 * </ul>
 * Los cofres llevan una tabla de botín y no objetos sueltos, como los de las mazmorras: así Lootr
 * los convierte en cofres por jugador.
 */
public final class AbyssalGrottoBuilder {
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/abyssal_grotto"));
    public static final ResourceKey<LootTable> TREASURE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "chests/abyssal_grotto_treasure"));

    private static final int GRID = 4, CELL = 26;
    /** Outward direction of each side of the grid: north, east, south, west. */
    private static final int[][] SIDE_DIR = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private enum Theme { ENTRY, JUNCTION, CRYSTAL, GEODE, SPIDER, BONES, MUSHROOM, LAKE, LAVA, CAMP, CRYPT, POCKET, BOSS }

    private static final class Room {
        final int x, z, fy, r, h;
        final Theme theme;
        final List<BlockPos> cells = new ArrayList<>();

        Room(int x, int z, int fy, int r, int h, Theme theme) {
            this.x = x; this.z = z; this.fy = fy; this.r = r; this.h = h; this.theme = theme;
        }

        double dist(int px, int pz) {
            return Math.hypot(px - x, pz - z);
        }
    }

    private final ServerLevel level;
    private final Random rand;
    private final double[] phase = new double[9];
    private final Set<Long> air = new HashSet<>();
    private final List<BlockPos> carvedList = new ArrayList<>();
    /** Walking space of tunnels: kept free of dripstone, crystals, nests and chests. */
    private final Set<Long> keep = new HashSet<>();
    /** Floor blocks laid under tunnels and rooms once all the carving is done. */
    private final List<BlockPos> floors = new ArrayList<>();
    /** Ground height of each column before the grotto touched it, to tell open sky from caves to seal. */
    private final Map<Long, Integer> surface = new HashMap<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<BlockPos> chestList = new ArrayList<>();
    private final List<BlockPos> taken = new ArrayList<>();
    private final BlockState wallA, wallB, wallC;
    private int chests, spawners, sealed, originX, originZ;

    private AbyssalGrottoBuilder(ServerLevel level, long seed) {
        this.level = level;
        this.rand = new Random(seed);
        for (int i = 0; i < phase.length; i++) phase[i] = rand.nextDouble() * Math.PI * 2;
        this.wallA = Blocks.DEEPSLATE.defaultBlockState();
        this.wallB = Blocks.TUFF.defaultBlockState();
        this.wallC = SystemBlocks.ABYSSAL_STONE.get().defaultBlockState();
    }

    /** Builds the grotto centred near {@code surface}; returns the entrance mouth, or null if it does not fit. */
    public static BlockPos build(ServerLevel level, BlockPos surface) {
        level.getChunk(surface.getX() >> 4, surface.getZ() >> 4);
        int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, surface.getX(), surface.getZ());
        int base = Math.max(level.getMinBuildHeight() + 24, Math.min(40, surfaceY - 30));
        if (surfaceY - base < 20) return null;
        AbyssalGrottoBuilder b = new AbyssalGrottoBuilder(level, level.getSeed() ^ surface.asLong());
        return b.buildAt(surface.getX(), surface.getZ(), base);
    }

    private BlockPos buildAt(int ox, int oz, int base) {
        // ------------------------------------------------------------ 1. layout on a 4x4 grid
        originX = ox;
        originZ = oz;
        int side = rand.nextInt(4), opp = (side + 2) % 4;
        int[] entryCell = cellOnSide(side, 1 + rand.nextInt(2));
        int[] bossCell = cellOnSide(opp, rand.nextBoolean() ? 0 : GRID - 1);
        List<Theme> themes = new ArrayList<>(List.of(Theme.CRYSTAL, Theme.GEODE, Theme.SPIDER, Theme.BONES,
                Theme.MUSHROOM, Theme.LAKE, Theme.LAVA, Theme.CAMP, Theme.CRYPT));
        Collections.shuffle(themes, rand);
        List<int[]> free = new ArrayList<>();
        for (int gx = 0; gx < GRID; gx++) for (int gz = 0; gz < GRID; gz++) {
            if (gx == entryCell[0] && gz == entryCell[1] || gx == bossCell[0] && gz == bossCell[1]) continue;
            free.add(new int[]{gx, gz});
        }
        Collections.shuffle(free, rand);
        Theme[][] theme = new Theme[GRID][GRID];
        for (int[] c : free) theme[c[0]][c[1]] = Theme.JUNCTION;
        for (int i = 0; i < themes.size() && i < free.size(); i++) theme[free.get(i)[0]][free.get(i)[1]] = themes.get(i);
        theme[entryCell[0]][entryCell[1]] = Theme.ENTRY;
        theme[bossCell[0]][bossCell[1]] = Theme.BOSS;

        Room[][] grid = new Room[GRID][GRID];
        Room entry = null, boss = null;
        for (int gx = 0; gx < GRID; gx++) for (int gz = 0; gz < GRID; gz++) {
            Theme t = theme[gx][gz];
            int steps = Math.abs(gx - entryCell[0]) + Math.abs(gz - entryCell[1]);   // the network sinks as it goes
            int x = ox + (int) Math.round((gx - (GRID - 1) / 2.0) * CELL) + rand.nextInt(9) - 4;
            int z = oz + (int) Math.round((gz - (GRID - 1) / 2.0) * CELL) + rand.nextInt(9) - 4;
            int fy = base - 2 * steps + rand.nextInt(5) - 2;
            Room room = switch (t) {
                case ENTRY -> new Room(x, z, base, 6, 6, t);
                case BOSS -> new Room(
                        ox + (int) Math.round((gx - (GRID - 1) / 2.0) * CELL) + (gx == 0 ? -7 : gx == GRID - 1 ? 7 : 0),
                        oz + (int) Math.round((gz - (GRID - 1) / 2.0) * CELL) + (gz == 0 ? -7 : gz == GRID - 1 ? 7 : 0),
                        base - 14, 14, 13, t);
                case JUNCTION -> new Room(x, z, fy, 3 + rand.nextInt(2), 5, t);
                case LAKE, LAVA -> new Room(x, z, fy, 9, 8, t);
                default -> new Room(x, z, fy, 7 + rand.nextInt(2), 7 + rand.nextInt(3), t);
            };
            grid[gx][gz] = room;
            rooms.add(room);
            if (t == Theme.ENTRY) entry = room;
            if (t == Theme.BOSS) boss = room;
        }

        // ------------------------------------------------------------ 2. which rooms are joined
        // a random spanning tree over the grid (everything reachable) plus some extra links for loops
        List<int[]> links = new ArrayList<>();
        for (int gx = 0; gx < GRID; gx++) for (int gz = 0; gz < GRID; gz++) {
            if (gx + 1 < GRID) links.add(new int[]{gx, gz, gx + 1, gz});
            if (gz + 1 < GRID) links.add(new int[]{gx, gz, gx, gz + 1});
        }
        Collections.shuffle(links, rand);
        int[] parent = new int[GRID * GRID];
        for (int i = 0; i < parent.length; i++) parent[i] = i;
        List<int[]> used = new ArrayList<>();
        for (int[] l : links) {
            int a = find(parent, l[0] * GRID + l[1]), b = find(parent, l[2] * GRID + l[3]);
            if (a != b) { parent[a] = b; used.add(l); }
            else if (rand.nextInt(100) < 40) used.add(l);
        }

        // ------------------------------------------------------------ 3. carve rooms and tunnels
        for (Room r : rooms) carveRoom(r);
        for (int[] l : used) connect(grid[l[0]][l[1]], grid[l[2]][l[3]]);

        // dead ends with a hidden chest at the end
        int spurs = 0;
        for (int attempt = 0; attempt < 40 && spurs < 3; attempt++) {
            Room from = rooms.get(rand.nextInt(rooms.size()));
            if (from.theme == Theme.BOSS || from.theme == Theme.ENTRY || from.theme == Theme.POCKET) continue;
            double a = rand.nextDouble() * Math.PI * 2;
            int len = 13 + rand.nextInt(7);
            int ex = from.x + (int) Math.round(Math.cos(a) * (from.r + len));
            int ez = from.z + (int) Math.round(Math.sin(a) * (from.r + len));
            boolean clash = false;
            for (Room o : rooms) if (o != from && o.dist(ex, ez) < o.r + 7) { clash = true; break; }
            if (clash) continue;
            Room pocket = new Room(ex, ez, from.fy - rand.nextInt(3), 3, 4, Theme.POCKET);
            carveRoom(pocket);
            rooms.add(pocket);
            corridor(from.x + Math.cos(a) * (from.r - 1.5), from.z + Math.sin(a) * (from.r - 1.5), from.fy,
                    ex - Math.cos(a) * 1.5, ez - Math.sin(a) * 1.5, pocket.fy, 2.1, 0.3);
            spurs++;
        }

        // the entrance: a mouth at the foot of a rocky outcrop, with a tunnel sloping down under it
        int[] sd = SIDE_DIR[side];
        // the shortest tunnel that still reaches the surface at a gentle slope (one block down every 1.35)
        int mx = 0, mz = 0, mouthY = 0;
        for (int length = 12; length <= 120; length++) {
            mx = entry.x + sd[0] * (entry.r + length);
            mz = entry.z + sd[1] * (entry.r + length);
            mouthY = height(Heightmap.Types.OCEAN_FLOOR, mx, mz);
            if (length >= (mouthY - entry.fy) * 1.35 + 2) break;
        }
        outcrop(mx, mz, -sd[0], -sd[1]);
        corridor(mx + sd[0] * 2, mz + sd[1] * 2, mouthY, entry.x + sd[0] * (entry.r - 1.5), entry.z + sd[1] * (entry.r - 1.5),
                entry.fy, 2.6, 0.08);

        // ------------------------------------------------------------ 4. walls, seals and floors
        skin();
        for (BlockPos f : floors)
            if (!isSolid(f.getX(), f.getY(), f.getZ())) set(f.getX(), f.getY(), f.getZ(), wallA, 2);
        assignCells();

        // ------------------------------------------------------------ 5. what each room holds
        for (Room r : rooms) furnish(r);
        decorate();
        BlockPos mouth = new BlockPos(mx, mouthY, mz);
        AwakeningSystemMod.LOGGER.info("[Gruta Abisal] construida en {} {} {}: {} salas, {} bloques excavados, {} huecos sellados, {} cofres, {} generadores",
                ox, base, oz, rooms.size(), carvedList.size(), sealed, chests, spawners);
        if (System.getProperty("awakening.debugGrotto") != null) {
            debugPlan(ox, oz, base);
            walkCheck(ox, oz, base, mouth, boss);
        }
        return mouth;
    }

    private static int[] cellOnSide(int side, int i) {
        return switch (side) {
            case 0 -> new int[]{i, 0};
            case 1 -> new int[]{GRID - 1, i};
            case 2 -> new int[]{i, GRID - 1};
            default -> new int[]{0, i};
        };
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) i = parent[i] = parent[parent[i]];
        return i;
    }

    // ---------------------------------------------------------------- shapes
    /** A rounded chamber: flat floor (so tunnels meet it cleanly), a domed roof and an irregular outline. */
    private void carveRoom(Room r) {
        for (int dx = -r.r - 3; dx <= r.r + 3; dx++) for (int dz = -r.r - 3; dz <= r.r + 3; dz++) {
            int x = r.x + dx, z = r.z + dz;
            double rr = r.r * (1 + 0.18 * noise(x * 0.15, r.fy * 0.1, z * 0.15));
            double d = Math.hypot(dx, dz);
            if (d > rr) continue;
            double k = d / rr;
            int ceil = r.fy + Math.max(3, (int) Math.round(r.h * Math.sqrt(1 - k * k)) + 1);
            for (int y = r.fy; y <= ceil; y++) carve(x, y, z);
            floors.add(new BlockPos(x, r.fy - 1, z));
        }
    }

    /** A tunnel between two rooms, leaving each one from its rim. */
    private void connect(Room a, Room b) {
        double dx = b.x - a.x, dz = b.z - a.z, len = Math.hypot(dx, dz);
        double ux = dx / len, uz = dz / len;
        double radius = 2.6 + rand.nextDouble() * 0.6;
        corridor(a.x + ux * (a.r - 1.5), a.z + uz * (a.r - 1.5), a.fy,
                b.x - ux * (b.r - 1.5), b.z - uz * (b.r - 1.5), b.fy, radius, 0.22);
    }

    /**
     * A winding tunnel (a bent curve between the two points), about 5–6 blocks wide and 5 tall.
     * <p>
     * Each column takes its height from the nearest point of the curve, so the floor rises as one smooth
     * ramp instead of stacked terraces. A little noise breaks the edge of every one-block rise into an
     * irregular line, like worn rock, and the roof follows the floor.
     */
    private void corridor(double ax, double az, int aWalk, double bx, double bz, int bWalk, double radius, double bend) {
        double len = Math.hypot(bx - ax, bz - az);
        if (len < 1) return;
        if (Math.abs(bWalk - aWalk) > len) bWalk = aWalk + (int) Math.signum(bWalk - aWalk) * (int) len;
        double px = -(bz - az) / len, pz = (bx - ax) / len;
        double off = (rand.nextDouble() * 2 - 1) * len * bend;
        double mx = (ax + bx) / 2 + px * off, mz = (az + bz) / 2 + pz * off;
        int n = (int) Math.ceil(len * 2.5) + 1;
        // column -> {distance to the curve, height along the curve, local radius}
        Map<Long, double[]> cols = new HashMap<>();
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n, u = 1 - t;
            double x = u * u * ax + 2 * u * t * mx + t * t * bx;
            double z = u * u * az + 2 * u * t * mz + t * t * bz;
            double walk = aWalk + (bWalk - aWalk) * t;
            double r = radius + 0.45 * noise(x * 0.21, walk * 0.1, z * 0.21);
            int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
            for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
                int cx = ix + dx, cz = iz + dz;
                double d = Math.hypot(cx + 0.5 - x, cz + 0.5 - z);
                if (d > r) continue;
                long key = BlockPos.asLong(cx, 0, cz);
                double[] c = cols.get(key);
                if (c == null || d < c[0]) cols.put(key, new double[]{d, walk, r});
            }
        }
        boolean sloped = aWalk != bWalk;
        Map<Long, Integer> heights = new HashMap<>();
        for (Map.Entry<Long, double[]> e : cols.entrySet()) {
            int cx = BlockPos.getX(e.getKey()), cz = BlockPos.getZ(e.getKey());
            double walk = e.getValue()[1];
            double jitter = sloped ? 0.42 * noise(cx * 0.37, walk * 0.15, cz * 0.37) : 0;
            heights.put(e.getKey(), (int) Math.floor(walk + 0.5 + jitter));
        }
        // never a rise of two blocks between neighbouring columns: it could not be climbed back up
        for (boolean changed = true; changed; ) {
            changed = false;
            for (Map.Entry<Long, Integer> e : heights.entrySet()) {
                int cx = BlockPos.getX(e.getKey()), cz = BlockPos.getZ(e.getKey());
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    Integer ny = heights.get(BlockPos.asLong(cx + dir.getStepX(), 0, cz + dir.getStepZ()));
                    if (ny != null && e.getValue() > ny + 1) { e.setValue(ny + 1); changed = true; }
                }
            }
        }
        for (Map.Entry<Long, double[]> e : cols.entrySet()) {
            int cx = BlockPos.getX(e.getKey()), cz = BlockPos.getZ(e.getKey());
            double d = e.getValue()[0], r = e.getValue()[2];
            int y = heights.get(e.getKey());
            int top = d < r - 1.3 ? 4 : 3;
            for (int h = 0; h <= top; h++) carve(cx, y + h, cz);
            if (d < 1.6) for (int h = 0; h <= 2; h++) keep(cx, y + h, cz);
            floors.add(new BlockPos(cx, y - 1, cz));
        }
    }

    /**
     * Rocky outcrop over the entrance: a steep face where the mouth opens and a back that slopes down
     * over the tunnel, in stone, andesite and mossy cobblestone with moss on top.
     */
    private void outcrop(int mx, int mz, int ix, int iz) {
        int px = -iz, pz = ix;
        for (int t = -1; t <= 14; t++) for (int s = -7; s <= 7; s++) {
            double hmax = t <= 0 ? 8.5 : 8.5 - t * 0.55;
            double hh = hmax - Math.abs(s) * 0.85 + 1.5 * noise(t * 0.4, 2.0, s * 0.4);
            if (hh < 1) continue;
            int x = mx + ix * t + px * s, z = mz + iz * t + pz * s;
            int g = height(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            surface.putIfAbsent(BlockPos.asLong(x, 0, z), g);
            int top = g + (int) hh;
            for (int y = g - 1; y < top; y++) {
                BlockState st = level.getBlockState(new BlockPos(x, y, z));
                if (!st.isAir() && !st.canBeReplaced() && y >= g) continue;
                int roll = rand.nextInt(100);
                BlockState rock = y == top - 1 && roll < 45 ? Blocks.MOSS_BLOCK.defaultBlockState()
                        : roll < 50 ? Blocks.STONE.defaultBlockState()
                        : roll < 70 ? Blocks.ANDESITE.defaultBlockState()
                        : roll < 85 ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                set(x, y, z, rock, 2);
            }
        }
    }

    // ---------------------------------------------------------------- rooms
    private void assignCells() {
        for (BlockPos p : carvedList) {
            Room best = null;
            double bestD = Double.MAX_VALUE;
            for (Room r : rooms) {
                double d = r.dist(p.getX(), p.getZ());
                if (d <= r.r * 1.2 + 1 && p.getY() >= r.fy - 2 && p.getY() <= r.fy + r.h + 4 && d / r.r < bestD) {
                    best = r;
                    bestD = d / r.r;
                }
            }
            if (best != null) best.cells.add(p);
        }
    }

    private void furnish(Room r) {
        BlockState crystal = SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get().defaultBlockState();
        switch (r.theme) {
            case ENTRY -> {
                for (BlockPos p : floorCells(r)) if (rand.nextInt(100) < 35) set(p.getX(), p.getY() - 1, p.getZ(), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                for (int i = 0; i < 2; i++) hangingLantern(r, Blocks.SOUL_LANTERN);
            }
            case JUNCTION -> {
                if (rand.nextBoolean()) hangingLantern(r, Blocks.LANTERN);
            }
            case CRYSTAL -> {
                for (BlockPos p : r.cells) {
                    if (!isAir(p) || keep.contains(p.asLong())) continue;
                    int roll = rand.nextInt(100);
                    if (onFloor(p) && roll < 11 && !nearKeep(p)) set(p, crystal.setValue(TeyvatCrystalBlock.FACING, Direction.UP));
                    else if (onFloor(p) && roll < 25) set(p.getX(), p.getY() - 1, p.getZ(), wallC, 2);
                    else if (onCeiling(p) && roll < 5) set(p, crystal.setValue(TeyvatCrystalBlock.FACING, Direction.DOWN));
                    else if (roll < 7) wallCrystal(p, crystal);
                }
                for (int i = 0; i < 3; i++) ceilingLight(r, Blocks.SHROOMLIGHT.defaultBlockState());
                chestIn(r, LOOT);
            }
            case GEODE -> {
                for (BlockPos p : r.cells) for (Direction d : Direction.values()) {
                    BlockPos n = p.relative(d);
                    if (air.contains(n.asLong()) || d == Direction.DOWN && keep.contains(p.asLong())) continue;
                    if (n.getY() < r.fy - 1) continue;
                    BlockState st = level.getBlockState(n);
                    if (st.isAir() || !st.getFluidState().isEmpty() || st.hasBlockEntity()) continue;
                    set(n, rand.nextInt(100) < 72 ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
                }
                for (BlockPos p : r.cells) {
                    if (!isAir(p) || keep.contains(p.asLong()) || nearKeep(p) && onFloor(p)) continue;
                    if (rand.nextInt(100) < 10) {
                        Direction from = solidSide(p);
                        if (from != null) set(p, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, from.getOpposite()));
                    }
                }
                chestIn(r, LOOT);
            }
            case SPIDER -> {
                for (BlockPos p : r.cells) {
                    if (!isAir(p) || keep.contains(p.asLong())) continue;
                    boolean byWall = solidSide(p) != null || onCeiling(p);
                    if (byWall && rand.nextInt(100) < 20 || onFloor(p) && rand.nextInt(100) < 5 && !nearKeep(p))
                        set(p, Blocks.COBWEB.defaultBlockState());
                }
                spawnerIn(r, EntityType.CAVE_SPIDER);
                for (BlockPos p : floorCells(r)) if (rand.nextInt(100) < 3 && !nearKeep(p)) set(p, Blocks.SKELETON_SKULL.defaultBlockState());
            }
            case BONES -> {
                for (BlockPos p : floorCells(r)) {
                    int roll = rand.nextInt(100);
                    if (roll < 22) set(p.getX(), p.getY() - 1, p.getZ(), Blocks.BONE_BLOCK.defaultBlockState(), 2);
                    else if (roll < 45) set(p.getX(), p.getY() - 1, p.getZ(), Blocks.SOUL_SOIL.defaultBlockState(), 2);
                    else if (roll < 49 && !nearKeep(p)) set(p, Blocks.SKELETON_SKULL.defaultBlockState());
                }
                spawnerIn(r, EntityType.SKELETON);
                for (int i = 0; i < 2; i++) {
                    BlockPos p = spotIn(r, 3);
                    if (p != null) set(p, Blocks.SOUL_LANTERN.defaultBlockState());
                }
            }
            case MUSHROOM -> {
                for (BlockPos p : floorCells(r)) {
                    set(p.getX(), p.getY() - 1, p.getZ(), rand.nextInt(100) < 80 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.MYCELIUM.defaultBlockState(), 2);
                    if (nearKeep(p)) continue;
                    int roll = rand.nextInt(100);
                    if (roll < 18) set(p, Blocks.MOSS_CARPET.defaultBlockState());
                    else if (roll < 23) set(p, (rand.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState());
                }
                for (BlockPos p : r.cells) if (onCeiling(p) && isAir(p) && !keep.contains(p.asLong()) && rand.nextInt(100) < 7) caveVines(p);
                for (int i = 0; i < 3; i++) {
                    BlockPos p = spotIn(r, 3);
                    if (p != null) hugeMushroom(p);
                }
                for (int i = 0; i < 2; i++) ceilingLight(r, Blocks.SHROOMLIGHT.defaultBlockState());
            }
            case LAKE -> {
                pool(r, Blocks.WATER.defaultBlockState(), 2, 0.68);
                BlockPos island = new BlockPos(r.x, r.fy, r.z);
                if (!keep.contains(island.asLong()) && isAir(island)) {
                    placeChest(island, Direction.from2DDataValue(rand.nextInt(4)), LOOT);
                    taken.add(island);
                }
                for (BlockPos p : r.cells) if (isAir(p) && solidSide(p) != null && rand.nextInt(100) < 6)
                    lichen(p);
                for (int i = 0; i < 2; i++) ceilingLight(r, Blocks.SHROOMLIGHT.defaultBlockState());
            }
            case LAVA -> {
                pool(r, Blocks.LAVA.defaultBlockState(), 1, 0.6);
                for (BlockPos p : floorCells(r)) {
                    double k = r.dist(p.getX(), p.getZ()) / r.r;
                    if (k > 0.6 && k < 0.8) set(p.getX(), p.getY() - 1, p.getZ(),
                            rand.nextBoolean() ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BASALT.defaultBlockState(), 2);
                }
            }
            case CAMP -> {
                for (BlockPos p : floorCells(r))
                    if (r.dist(p.getX(), p.getZ()) < r.r * 0.6 && rand.nextInt(100) < 55) set(p.getX(), p.getY() - 1, p.getZ(), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                for (int i = 0; i < 3; i++) {
                    BlockPos p = spotIn(r, 3);
                    if (p == null) continue;
                    set(p, Blocks.SPRUCE_FENCE.defaultBlockState());
                    if (isAir(p.above())) set(p.above(), Blocks.SPRUCE_FENCE.defaultBlockState());
                    if (isAir(p.above(2))) set(p.above(2), Blocks.LANTERN.defaultBlockState());
                }
                BlockPos table = spotIn(r, 2);
                if (table != null) set(table, Blocks.CRAFTING_TABLE.defaultBlockState());
                BlockPos barrel = spotIn(r, 2);
                if (barrel != null) set(barrel, Blocks.BARREL.defaultBlockState());
                BlockPos rails = spotIn(r, 3);
                if (rails != null) for (int k = 0; k < 4; k++) {
                    BlockPos q = rails.east(k);
                    if (isAir(q) && isSolid(q.getX(), q.getY() - 1, q.getZ()) && !keep.contains(q.asLong())) set(q, Blocks.RAIL.defaultBlockState());
                }
                chestIn(r, LOOT);
            }
            case CRYPT -> {
                for (BlockPos p : floorCells(r)) set(p.getX(), p.getY() - 1, p.getZ(),
                        rand.nextInt(100) < 70 ? Blocks.DEEPSLATE_TILES.defaultBlockState() : Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(), 2);
                for (BlockPos p : r.cells) for (Direction d : Direction.Plane.HORIZONTAL) {
                    BlockPos n = p.relative(d);
                    if (!air.contains(n.asLong()) && isSolid(n.getX(), n.getY(), n.getZ()) && rand.nextInt(100) < 55)
                        set(n, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                }
                for (int i = 0; i < 3; i++) {
                    BlockPos p = spotIn(r, 3);
                    if (p == null) continue;
                    BlockPos q = p.relative(Direction.from2DDataValue(rand.nextInt(4)));
                    set(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    if (isAir(q) && !keep.contains(q.asLong())) set(q, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    if (isAir(p.above())) set(p.above(), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true).setValue(CandleBlock.CANDLES, 1 + rand.nextInt(3)));
                }
                spawnerIn(r, EntityType.ZOMBIE);
                chestIn(r, LOOT);
            }
            case POCKET -> {
                BlockPos p = new BlockPos(r.x, r.fy, r.z);
                if (isAir(p) && isSolid(r.x, r.fy - 1, r.z)) placeChest(p, Direction.from2DDataValue(rand.nextInt(4)), LOOT);
                else chestIn(r, LOOT);
                BlockPos c = spotIn(r, 1);
                if (c != null) set(c, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true).setValue(CandleBlock.CANDLES, 2));
            }
            case BOSS -> furnishBoss(r);
        }
    }

    /** Boss arena: polished centre, four great pillars, a ring of lights in the roof and the treasure. */
    private void furnishBoss(Room r) {
        for (BlockPos p : floorCells(r)) {
            double d = r.dist(p.getX(), p.getZ());
            if (d < 5) set(p.getX(), p.getY() - 1, p.getZ(), rand.nextInt(5) == 0 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
            else if (d < 5.8) set(p.getX(), p.getY() - 1, p.getZ(), Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
        }
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + i * Math.PI / 2;
            int px = r.x + (int) Math.round(Math.cos(a) * 9), pz = r.z + (int) Math.round(Math.sin(a) * 9);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) == 2) continue;
                for (int y = r.fy; y < r.fy + r.h + 4; y++) {
                    BlockPos q = new BlockPos(px + dx, y, pz + dz);
                    if (!air.contains(q.asLong()) || keep.contains(q.asLong())) continue;
                    set(q, (y - r.fy) % 4 == 0 ? Blocks.CHISELED_TUFF.defaultBlockState() : Blocks.TUFF_BRICKS.defaultBlockState());
                }
            }
            // a sconce on the pillar: a slab sticking out, and the lantern hanging under it (not floating)
            set(px, r.fy + 4, pz + 2, Blocks.TUFF_BRICK_SLAB.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP), 2);
            set(px, r.fy + 3, pz + 2, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        }
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI * 2 / 12;
            int x = r.x + (int) Math.round(Math.cos(a) * 6), z = r.z + (int) Math.round(Math.sin(a) * 6);
            for (int y = r.fy + r.h + 3; y > r.fy + 3; y--) {
                if (isAir(new BlockPos(x, y - 1, z)) && !isAir(new BlockPos(x, y, z))) { set(x, y, z, Blocks.SHROOMLIGHT.defaultBlockState(), 2); break; }
            }
        }
        // treasure on the far side of the arena, away from the grid
        double ax = r.x - originX, az = r.z - originZ;
        double al = Math.max(1, Math.hypot(ax, az));
        BlockPos treasure = null;
        for (int k = 10; k >= 4 && treasure == null; k--) {
            BlockPos p = new BlockPos(r.x + (int) Math.round(ax / al * k), r.fy, r.z + (int) Math.round(az / al * k));
            if (isAir(p) && isSolid(p.getX(), p.getY() - 1, p.getZ()) && !keep.contains(p.asLong())) treasure = p;
        }
        if (treasure == null) treasure = spotIn(r, 2);
        if (treasure != null) {
            Direction face = Direction.getNearest(-ax, 0, -az);
            placeChest(treasure, face, TREASURE);
            BlockPos l = treasure.relative(face.getClockWise(), 2), l2 = treasure.relative(face.getCounterClockWise(), 2);
            if (isAir(l)) set(l, Blocks.SOUL_LANTERN.defaultBlockState());
            if (isAir(l2)) set(l2, Blocks.SOUL_LANTERN.defaultBlockState());
        }
        spawnGrottoBoss(new BlockPos(r.x, r.fy - 1, r.z));
    }

    /**
     * The grotto's keeper: a creature of the abyss from this world (not one of Teyvat's, which belong to the fourth
     * dimension), held to the arena like the other overworld minibosses.
     */
    private void spawnGrottoBoss(BlockPos pos) {
        net.minecraft.world.entity.EntityType<?>[] types = {
                com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_BEHOLDER.get(),
                com.soybe.awakeningsystem.entity.ModEntities.ABYSSAL_NECROMANCER.get(),
                com.soybe.awakeningsystem.entity.ModEntities.SCULK_GOLEM.get()
        };
        var entity = types[rand.nextInt(types.length)].create(level);
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) return;
        mob.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
        mob.setPersistenceRequired();
        var tag = mob.getPersistentData();
        tag.putDouble("arena_center_x", pos.getX() + 0.5D);
        tag.putDouble("arena_center_y", pos.getY() + 1.0D);
        tag.putDouble("arena_center_z", pos.getZ() + 0.5D);
        tag.putDouble("arena_max_radius", 35.0D);
        tag.putBoolean("is_overworld_miniboss", true);
        level.addFreshEntity(mob);
    }

    /** Sinks the middle of a room into a pool; the tunnels crossing it stay as causeways. */
    private void pool(Room r, BlockState fluid, int depth, double extent) {
        for (BlockPos p : floorCells(r)) {
            double d = r.dist(p.getX(), p.getZ());
            if (d > r.r * extent || d < 2.3 && fluid.is(Blocks.WATER)) continue;
            if (keep.contains(p.asLong()) || nearKeep(p)) continue;
            for (int k = 1; k <= depth; k++) set(p.getX(), p.getY() - k, p.getZ(), fluid, 3);
            BlockPos under = p.below(depth + 1);
            if (!isSolid(under.getX(), under.getY(), under.getZ())) set(under, wallA);
        }
    }

    // ---------------------------------------------------------------- general decoration
    private void decorate() {
        BlockState crystal = SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get().defaultBlockState();
        for (BlockPos p : carvedList) {
            if (!isAir(p)) continue;
            boolean kept = keep.contains(p.asLong());
            int roll = rand.nextInt(1000);
            if (onCeiling(p) && roll < 9) dripstone(p.getX(), p.getY(), p.getZ(), Direction.DOWN, 2 + rand.nextInt(4));
            else if (kept) continue;
            else if (onFloor(p) && roll < 14 && !nearKeep(p)) dripstone(p.getX(), p.getY(), p.getZ(), Direction.UP, 1 + rand.nextInt(3));
            else if (onFloor(p) && roll < 19 && !nearKeep(p)) set(p, crystal.setValue(TeyvatCrystalBlock.FACING, Direction.UP));
            else if (roll < 45 && solidSide(p) != null) lichen(p);
            else if (roll < 49) wallCrystal(p, crystal);
        }
    }

    private void lichen(BlockPos p) {
        Direction d = solidSide(p);
        if (d != null) set(p, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(d), true));
    }

    private void wallCrystal(BlockPos p, BlockState crystal) {
        Direction d = solidSide(p);
        if (d != null && !keep.contains(p.asLong())) set(p, crystal.setValue(TeyvatCrystalBlock.FACING, d.getOpposite()));
    }

    private void dripstone(int x, int y, int z, Direction tip, int len) {
        int step = tip == Direction.DOWN ? -1 : 1;
        // shorten it so it never reaches into a path, and stalactites leave head room below them
        int fit = 0;
        for (int i = 0; i < len; i++) {
            int yy = y + step * i;
            if (!isAir(new BlockPos(x, yy, z)) || keep.contains(BlockPos.asLong(x, yy, z))) break;
            if (tip == Direction.DOWN) {
                boolean clear = true;
                for (int k = 1; k <= 3; k++) if (!isAir(new BlockPos(x, yy - k, z))) { clear = false; break; }
                if (!clear) break;
            }
            fit++;
        }
        if (fit == 0) return;
        set(x, y - step, z, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
        for (int i = 0; i < fit; i++) {
            DripstoneThickness t = fit == 1 || i == fit - 1 ? DripstoneThickness.TIP
                    : i == fit - 2 ? DripstoneThickness.FRUSTUM
                    : i == 0 ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            set(x, y + step * i, z, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, tip)
                    .setValue(PointedDripstoneBlock.THICKNESS, t), 2);
        }
    }

    private void caveVines(BlockPos top) {
        int len = 1 + rand.nextInt(4), fit = 0;
        for (int i = 0; i < len; i++) {
            BlockPos q = top.below(i);
            if (!isAir(q) || keep.contains(q.asLong()) || !isAir(q.below(2))) break;
            fit++;
        }
        for (int i = 0; i < fit; i++) {
            boolean last = i == fit - 1;
            BlockState st = (last ? Blocks.CAVE_VINES : Blocks.CAVE_VINES_PLANT).defaultBlockState()
                    .setValue(BlockStateProperties.BERRIES, rand.nextInt(100) < 35);
            set(top.below(i), st);
        }
    }

    private void hugeMushroom(BlockPos at) {
        int stem = 3 + rand.nextInt(3);
        for (int k = 0; k <= stem + 1; k++) if (!isAir(at.above(k))) return;
        for (int k = 0; k < stem; k++) set(at.above(k), Blocks.MUSHROOM_STEM.defaultBlockState());
        boolean red = rand.nextBoolean();
        BlockState cap = (red ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK).defaultBlockState();
        int cy = at.getY() + stem;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
            BlockPos q = new BlockPos(at.getX() + dx, cy, at.getZ() + dz);
            if (isAir(q) && !keep.contains(q.asLong())) set(q, cap);
            if (red && (Math.abs(dx) == 2 || Math.abs(dz) == 2)) {
                BlockPos s = q.below();
                if (isAir(s) && !keep.contains(s.asLong())) set(s, cap);
            }
        }
    }

    private void hangingLantern(Room r, Block lantern) {
        List<BlockPos> options = new ArrayList<>();
        for (BlockPos p : r.cells) if (isAir(p) && onCeiling(p) && p.getY() <= r.fy + 6) options.add(p);
        if (options.isEmpty()) return;
        set(options.get(rand.nextInt(options.size())), lantern.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    private void ceilingLight(Room r, BlockState light) {
        List<BlockPos> options = new ArrayList<>();
        for (BlockPos p : r.cells) if (isAir(p) && onCeiling(p)) options.add(p.above());
        if (!options.isEmpty()) set(options.get(rand.nextInt(options.size())), light);
    }

    // ---------------------------------------------------------------- spots, chests and spawners
    private List<BlockPos> floorCells(Room r) {
        List<BlockPos> out = new ArrayList<>();
        for (BlockPos p : r.cells) if (isAir(p) && onFloor(p) && !keep.contains(p.asLong())) out.add(p);
        return out;
    }

    /** A free floor spot in a room, off the paths and at least {@code spacing} blocks from earlier ones. */
    private BlockPos spotIn(Room r, int spacing) {
        List<BlockPos> options = new ArrayList<>();
        for (BlockPos p : floorCells(r)) {
            if (nearKeep(p) || !isAir(p.above())) continue;
            boolean crowded = false;
            for (BlockPos t : taken) if (t.distSqr(p) < spacing * spacing) { crowded = true; break; }
            if (!crowded) options.add(p);
        }
        if (options.isEmpty()) return null;
        BlockPos pick = options.get(rand.nextInt(options.size()));
        taken.add(pick);
        return pick;
    }

    private void chestIn(Room r, ResourceKey<LootTable> loot) {
        BlockPos p = spotIn(r, 4);
        if (p != null) placeChest(p, Direction.from2DDataValue(rand.nextInt(4)), loot);
    }

    private void spawnerIn(Room r, EntityType<?> type) {
        BlockPos p = spotIn(r, 4);
        if (p == null) return;
        set(p, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(p) instanceof SpawnerBlockEntity s) {
            s.setEntityId(type, level.getRandom());
            spawners++;
        }
    }

    private void placeChest(BlockPos p, Direction facing, ResourceKey<LootTable> loot) {
        level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        if (level.getBlockEntity(p) instanceof ChestBlockEntity chest) {
            chest.setLootTable(loot, rand.nextLong());
            chests++;
            chestList.add(p);
        }
    }

    // ---------------------------------------------------------------- carving helpers
    private void carve(int x, int y, int z) {
        if (y <= level.getMinBuildHeight() + 4) return;
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = level.getBlockState(p);
        if (s.is(Blocks.BEDROCK)) return;
        surface.computeIfAbsent(BlockPos.asLong(x, 0, z), k -> level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z));
        if (air.add(p.asLong())) carvedList.add(p);
        if (!s.isAir()) level.setBlock(p, Blocks.CAVE_AIR.defaultBlockState(), 2);
    }

    private void keep(int x, int y, int z) {
        keep.add(BlockPos.asLong(x, y, z));
    }

    /** True when a path cell is right next to this one (same level or one up), so nothing blocks the way. */
    private boolean nearKeep(BlockPos p) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = 0; dy <= 1; dy++)
            if (keep.contains(BlockPos.asLong(p.getX() + dx, p.getY() + dy, p.getZ() + dz))) return true;
        return false;
    }

    private int surfaceAt(int x, int z) {
        Integer h = surface.get(BlockPos.asLong(x, 0, z));
        return h != null ? h : height(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    /** Heightmap value, loading the chunk first: an unloaded chunk would report the bottom of the world. */
    private int height(Heightmap.Types type, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(type, x, z);
    }

    /**
     * Gives every wall the grotto's stone. Air, water or lava next to the grotto that is not open sky
     * (another cave, an aquifer, a lake bed) gets walled off too, so the grotto never floods or has holes.
     * Near the surface the natural rock is left as it is.
     */
    private void skin() {
        for (BlockPos p : carvedList) {
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (air.contains(n.asLong())) continue;
                BlockState s = level.getBlockState(n);
                if (s.is(Blocks.BEDROCK) || s.hasBlockEntity()) continue;
                boolean open = s.isAir() || !s.getFluidState().isEmpty() || s.canBeReplaced();
                int ground = surfaceAt(n.getX(), n.getZ());
                if (open) {
                    if (n.getY() >= ground) continue;   // open sky at the entrance
                    sealed++;
                } else if (n.getY() >= ground - 3) continue;
                double v = noise(n.getX() * 0.18, n.getY() * 0.18, n.getZ() * 0.18);
                level.setBlock(n, v > 0.45 ? wallC : (v > -0.1 ? wallA : wallB), 2);
            }
        }
    }

    private boolean onFloor(BlockPos p) {
        return isSolid(p.getX(), p.getY() - 1, p.getZ());
    }

    private boolean onCeiling(BlockPos p) {
        return isSolid(p.getX(), p.getY() + 1, p.getZ());
    }

    private Direction solidSide(BlockPos p) {
        for (Direction d : Direction.Plane.HORIZONTAL) if (isSolid(p.getX() + d.getStepX(), p.getY(), p.getZ() + d.getStepZ())) return d;
        return null;
    }

    private boolean isAir(BlockPos p) {
        return level.getBlockState(p).isAir();
    }

    private boolean isSolid(int x, int y, int z) {
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = level.getBlockState(p);
        return !s.isAir() && s.getFluidState().isEmpty() && s.isSolidRender(level, p);
    }

    private void set(BlockPos p, BlockState state) {
        set(p.getX(), p.getY(), p.getZ(), state, 2);
    }

    private void set(int x, int y, int z, BlockState state, int flags) {
        if (y <= level.getMinBuildHeight()) return;
        level.setBlock(new BlockPos(x, y, z), state, flags | Block.UPDATE_KNOWN_SHAPE);
    }

    private double noise(double x, double y, double z) {
        return (Math.sin(x * 1.7 + phase[0]) * Math.cos(y * 1.3 + phase[1])
                + Math.sin(z * 1.5 + phase[2]) * Math.cos(x * 0.9 + phase[3])
                + Math.sin(y * 1.1 + phase[4]) * Math.cos(z * 1.2 + phase[5])
                + 0.5 * Math.sin((x + z) * 2.3 + phase[6]) + 0.5 * Math.sin((y - x) * 2.1 + phase[7])) / 4.0;
    }

    // ---------------------------------------------------------------- dev aids
    /** Writes a floor plan as a PNG: the floor under every open column, coloured by what is there. */
    private void debugPlan(int ox, int oz, int base) {
        try {
            int r = 110, s = 3;
            java.awt.image.BufferedImage plan = new java.awt.image.BufferedImage(2 * r * s, 2 * r * s, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Map<Long, Integer> top = new HashMap<>();
            for (BlockPos p : carvedList) {
                long k = BlockPos.asLong(p.getX(), 0, p.getZ());
                if (!top.containsKey(k) || p.getY() < top.get(k)) top.put(k, p.getY());
            }
            for (int x = -r; x < r; x++) for (int z = -r; z < r; z++) {
                int col = 0x303030;
                Integer y = top.get(BlockPos.asLong(ox + x, 0, oz + z));
                if (y != null) {
                    // the lowest open cell's contents if it holds something, else the floor under it
                    BlockState here = level.getBlockState(new BlockPos(ox + x, y, oz + z));
                    col = here.isAir() ? shade(colour(level.getBlockState(new BlockPos(ox + x, y - 1, oz + z))), y - base) : colour(here);
                }
                fillCell(plan, (x + r) * s, (z + r) * s, s, col);
            }
            java.io.File dir = level.getServer().getServerDirectory().resolve("grotto_debug").toFile();
            dir.mkdirs();
            javax.imageio.ImageIO.write(plan, "png", new java.io.File(dir, "plan.png"));
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("[Gruta Abisal] debug plan failed", e);
        }
    }

    private static int shade(int rgb, int dy) {
        double f = Math.max(0.45, Math.min(1.3, 1.0 + dy * 0.035));
        int r = (int) Math.min(255, ((rgb >> 16) & 255) * f), g = (int) Math.min(255, ((rgb >> 8) & 255) * f), b = (int) Math.min(255, (rgb & 255) * f);
        return r << 16 | g << 8 | b;
    }

    private static void fillCell(java.awt.image.BufferedImage img, int px, int py, int s, int col) {
        for (int i = 0; i < s; i++) for (int j = 0; j < s; j++) img.setRGB(px + i, py + j, col);
    }

    private static int colour(BlockState st) {
        if (st.is(Blocks.CHEST)) return 0xFFE040;
        if (st.is(Blocks.SPAWNER)) return 0xFF40FF;
        if (st.is(Blocks.LAVA) || st.is(Blocks.MAGMA_BLOCK)) return 0xFF6A10;
        if (st.is(Blocks.WATER)) return 0x3070FF;
        if (st.is(Blocks.SHROOMLIGHT) || st.is(Blocks.SOUL_LANTERN) || st.is(Blocks.LANTERN)) return 0xFFB060;
        if (st.is(Blocks.AMETHYST_BLOCK) || st.is(Blocks.AMETHYST_CLUSTER) || st.is(Blocks.CALCITE)) return 0xB070FF;
        if (st.getBlock() instanceof TeyvatCrystalBlock) return 0x40F0FF;
        if (st.is(Blocks.MOSS_BLOCK) || st.is(Blocks.MOSS_CARPET) || st.is(Blocks.MYCELIUM)) return 0x50B050;
        if (st.is(Blocks.COBWEB) || st.is(Blocks.BONE_BLOCK)) return 0xF0F0F0;
        if (st.is(Blocks.SOUL_SOIL)) return 0x5A4430;
        if (st.is(Blocks.SPRUCE_PLANKS)) return 0x8A6234;
        if (st.is(Blocks.DEEPSLATE_TILES) || st.is(Blocks.CRACKED_DEEPSLATE_TILES) || st.is(Blocks.POLISHED_DEEPSLATE)) return 0x8A8A9A;
        if (st.is(Blocks.TUFF_BRICKS)) return 0xB0B4A0;
        if (st.is(Blocks.TUFF)) return 0x707468;
        if (st.is(Blocks.DEEPSLATE) || st.is(Blocks.COBBLED_DEEPSLATE)) return 0x4A4A55;
        if (st.getBlock() == SystemBlocks.ABYSSAL_STONE.get()) return 0x3A2E5A;
        return 0x9A9A9A;
    }

    /**
     * Walks the grotto like a player (steps of one block up, drops of at most three) from the entrance
     * and back out from the boss arena, and logs what can and cannot be reached.
     */
    private void walkCheck(int ox, int oz, int base, BlockPos mouth, Room boss) {
        int[] box = {ox - 115, ox + 115, base - 40, Math.max(base, mouth.getY()) + 12, oz - 115, oz + 115};
        BlockPos arena = new BlockPos(boss.x, boss.fy, boss.z);
        for (int dy = 2; dy >= -3; dy--) if (standable(mouth.getX(), mouth.getY() + dy, mouth.getZ())) { mouth = mouth.above(dy); break; }
        Set<Long> in = walk(mouth, box);
        Set<Long> out = walk(arena, box);
        BlockPos highest = arena;
        for (long k : out) if (BlockPos.getY(k) > highest.getY()) highest = BlockPos.of(k);
        AwakeningSystemMod.LOGGER.info("[Gruta Abisal] desde la arena se sube hasta {} ({} celdas)", highest.toShortString(), out.size());
        int shown = 0;
        for (long k : out) {
            BlockPos p = BlockPos.of(k);
            for (Direction d : Direction.Plane.HORIZONTAL) for (int dy = 1; dy <= 3; dy++) {
                BlockPos q = p.relative(d).above(dy);
                if (in.contains(q.asLong()) && !out.contains(q.asLong()) && shown < 6) {
                    shown++;
                    AwakeningSystemMod.LOGGER.info("[Gruta Abisal] escalón sin subida: {} -> {} ({} / {} / {})", p.toShortString(), q.toShortString(),
                            level.getBlockState(q.below()), level.getBlockState(p.above(2)), level.getBlockState(q.above()));
                }
            }
        }
        int chestsIn = 0;
        for (BlockPos c : chestList) if (touches(in, c)) chestsIn++;
        int roomsIn = 0;
        for (Room r : rooms) {
            for (BlockPos p : r.cells) if (in.contains(p.asLong())) { roomsIn++; break; }
        }
        int floorsTotal = 0, lost = 0;
        for (BlockPos p : carvedList) {
            if (!standable(p.getX(), p.getY(), p.getZ())) continue;
            floorsTotal++;
            if (!in.contains(p.asLong())) lost++;
        }
        AwakeningSystemMod.LOGGER.info("[Gruta Abisal] entrada {} (pisable {}, abajo {}), arena {} (pisable {})", mouth.toShortString(),
                standable(mouth.getX(), mouth.getY(), mouth.getZ()), level.getBlockState(mouth.below()), arena.toShortString(),
                standable(arena.getX(), arena.getY(), arena.getZ()));
        AwakeningSystemMod.LOGGER.info("[Gruta Abisal] recorrido: arena del jefe={}, salida desde la arena={}, salas visitadas {}/{}, cofres alcanzables {}/{}, suelo sin acceso {}/{}",
                in.contains(arena.asLong()), touchesPos(out, mouth), roomsIn, rooms.size(), chestsIn, chestList.size(), lost, floorsTotal);
    }

    private boolean touchesPos(Set<Long> reached, BlockPos p) {
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) for (int dy = -2; dy <= 2; dy++)
            if (reached.contains(p.offset(dx, dy, dz).asLong())) return true;
        return false;
    }

    private boolean touches(Set<Long> reached, BlockPos c) {
        for (Direction d : Direction.Plane.HORIZONTAL) for (int dy = -1; dy <= 1; dy++)
            if (reached.contains(c.relative(d).above(dy).asLong())) return true;
        return reached.contains(c.above().asLong());
    }

    private Set<Long> walk(BlockPos start, int[] box) {
        Set<Long> seen = new HashSet<>();
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        if (!standable(start.getX(), start.getY(), start.getZ())) return seen;
        seen.add(start.asLong());
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            boolean headroom = passable(p.getX(), p.getY() + 2, p.getZ());
            for (Direction d : Direction.Plane.HORIZONTAL) {
                int x = p.getX() + d.getStepX(), z = p.getZ() + d.getStepZ();
                if (x < box[0] || x > box[1] || z < box[4] || z > box[5]) continue;
                int found = Integer.MIN_VALUE;
                if (headroom && standable(x, p.getY() + 1, z)) found = p.getY() + 1;
                else if (passable(x, p.getY(), z) && passable(x, p.getY() + 1, z)) {
                    for (int k = 0; k <= 3; k++) {
                        int y = p.getY() - k;
                        if (y < box[2]) break;
                        if (standable(x, y, z)) { found = y; break; }
                        if (!passable(x, y, z)) break;
                    }
                }
                if (found == Integer.MIN_VALUE || found > box[3]) continue;
                BlockPos n = new BlockPos(x, found, z);
                if (seen.add(n.asLong())) queue.add(n);
            }
        }
        return seen;
    }

    private boolean passable(int x, int y, int z) {
        BlockPos p = new BlockPos(x, y, z);
        BlockState s = level.getBlockState(p);
        return s.getCollisionShape(level, p).isEmpty() && !s.is(Blocks.LAVA);
    }

    private boolean standable(int x, int y, int z) {
        BlockPos below = new BlockPos(x, y - 1, z);
        return passable(x, y, z) && passable(x, y + 1, z) && !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }
}
