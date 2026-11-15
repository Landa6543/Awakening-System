package com.soybe.awakeningsystem.tower.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.hash;
import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.n;

/**
 * Floors 16 to 20 of the Great Tower, each with its own land and nothing of Minecraft's own terrain:
 * <ol start="16">
 *   <li>Void Obsidian Bastion: stepped mesas of end stone under the End's sky, obsidian spires veined with
 *       crying obsidian, chorus "trees", amethyst and islets floating overhead.</li>
 *   <li>Celestial Peaks of the Immortal Dao: a green valley under rounded karst towers, mossy, with trees on
 *       their crowns, and bamboo groves below.</li>
 *   <li>Golden Temple of the Solar Calamity: red sandstone tablelands cut by canyons with banded walls,
 *       hoodoos, cacti and dead bushes, and fallen golden columns.</li>
 *   <li>Antechamber of the Abyssal Webs: a dead forest at night, its bare trees strung with webs, ash and
 *       gravel underfoot and giant bone ribs. The way to the queen.</li>
 *   <li>Royal Chamber of the Nest Queen (boss): a vast cave of silk cocoons, webs and pillars; in the middle
 *       the queen's round arena, joined by a causeway to both portals.</li>
 * </ol>
 */
final class TowerFloorTerrainBeyond {
    private TowerFloorTerrainBeyond() {}

    // ── heights ──────────────────────────────────────────────────────────────

    static int height(int floor, int x, int z) {
        return switch (floor) {
            case 16 -> {
                double v = 9 * n(16, x, z, 0.006, 3) + 2 * n(16, x + 400, z, 0.03, 1);
                yield 70 + (int) Math.floor(v / 4.0) * 4;                         // stepped mesas
            }
            case 17 -> daoHeight(x, z);
            case 18 -> {
                double r = Math.abs(n(18, x, z, 0.0045, 3));                      // canyons wind where the noise crosses zero
                double c = smooth((r - 0.07) / 0.17);
                int base = 66 + (int) Math.round(3 * n(18, x + 77, z, 0.03, 2));
                yield base + (int) Math.round(c * 36 / 6.0) * 6;                  // canyon walls in ledges of 6
            }
            case 19 -> {
                double crater = Math.max(0, n(19, x - 600, z, 0.012, 2) - 0.45) * 22;
                yield 68 + (int) Math.round(5 * n(19, x, z, 0.008, 3) + 2 * n(19, x, z + 300, 0.04, 2) - crater);
            }
            default -> nestHeight(x, z);
        };
    }

    static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    // floor 17: valley with karst towers
    private static final int KARST_CELL = 24;

    private static int daoValley(int x, int z) {
        return 67 + (int) Math.round(5 * n(17, x, z, 0.008, 3) + 2 * n(17, x - 200, z, 0.04, 2));
    }

    private static int daoHeight(int x, int z) {
        int base = daoValley(x, z), best = base;
        int ci = Math.floorDiv(x, KARST_CELL), cj = Math.floorDiv(z, KARST_CELL);
        for (int a = ci - 1; a <= ci + 1; a++)
            for (int b = cj - 1; b <= cj + 1; b++) {
                if (hash(17, a, b, 1) > 0.5) continue;
                double sx = a * KARST_CELL + 6 + hash(17, a, b, 2) * (KARST_CELL - 12);
                double sz = b * KARST_CELL + 6 + hash(17, a, b, 3) * (KARST_CELL - 12);
                double rad = 4.5 + hash(17, a, b, 4) * 4.5;
                double rr = rad * (1.0 + 0.22 * n(17, x, z, 0.14, 2));
                double d = Math.hypot(x - sx, z - sz);
                if (d >= rr) continue;
                int tall = 30 + (int) (hash(17, a, b, 5) * 38);
                double dome = Math.sqrt(Math.max(0, 1 - Math.pow(d / rr, 6)));    // straight walls, rounded crown
                best = Math.max(best, base + (int) Math.round(tall * dome));
            }
        return best;
    }

    private static boolean karst(int x, int z) {
        return daoHeight(x, z) > daoValley(x, z) + 5;
    }

    // floor 20: the nest cavern and the queen's raised arena in its middle. The portals stand in the cavern's far
    // corners, so the way to her is a long walk through the nest, past her brood, and then up a ramp that winds
    // once round the arena's pillar to its one way in.
    static final int DECK = 28;                         // the arena's floor (the queen stands on it)
    private static final int ARENA_R = 24;
    private static final int RAMP_IN = ARENA_R + 1, RAMP_OUT = ARENA_R + 5, RAMP_FOOT = 13;
    /** The angle where the ramp starts at the cave floor (toward the entry corner) and, a full turn on, meets the arena. */
    private static final double RAMP_START = Math.atan2(-1, -1);

    static boolean onArena(int x, int z) {
        return x * x + z * z <= ARENA_R * ARENA_R;
    }

    /** How far round the ramp a column is, 0 at its foot to 1 at the arena, or -1 when it is not on the ramp. */
    private static double rampAt(int x, int z) {
        double d = Math.hypot(x, z);
        if (d < RAMP_IN || d >= RAMP_OUT) return -1;
        double s = (Math.atan2(z, x) - RAMP_START) / (Math.PI * 2);
        return s - Math.floor(s);
    }

    private static int rampY(double s) {
        return RAMP_FOOT + (int) Math.round(s * (DECK - RAMP_FOOT));
    }

    /** The arena's way in: the part of its rim where the ramp arrives. */
    private static boolean arenaEntry(int x, int z) {
        double s = (Math.atan2(z, x) - RAMP_START) / (Math.PI * 2);
        s -= Math.floor(s);
        return s > 0.93;
    }

    private static int nestGround(int x, int z) {
        return 12 + (int) Math.round(7 * n(20, x, z, 0.011, 3) + 3 * n(20, x + 60, z, 0.06, 1));
    }

    private static int nestRoof(int x, int z) {
        return 96 + (int) Math.round(10 * n(20, x + 999, z, 0.008, 3));
    }

    private static int nestHeight(int x, int z) {
        if (onArena(x, z)) return DECK;
        double s = rampAt(x, z);
        if (s >= 0) return Math.max(nestGround(x, z), rampY(s));
        return nestGround(x, z);
    }

    // ── filling ──────────────────────────────────────────────────────────────

    static void fill(int floor, ChunkAccess chunk) {
        TowerFloorTerrain.Writer w = new TowerFloorTerrain.Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        if (floor == 20) {
            fillNest(chunk, w, x0, z0, p);
            return;
        }
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int h = height(floor, x, z);
                int slope = Math.abs(height(floor, x + 1, z) - height(floor, x - 1, z)) + Math.abs(height(floor, x, z + 1) - height(floor, x, z - 1));
                boolean steep = slope > 4;
                for (int y = -64; y <= h; y++) {
                    BlockState s = y == -64 ? Blocks.BEDROCK.defaultBlockState() : y < 0 ? Blocks.DEEPSLATE.defaultBlockState() : ground(floor, x, y, z, h, steep);
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                if (!steep) plants(floor, w, x, h + 1, z);
            }
        switch (floor) {
            case 16 -> {
                scatter(16, w, x0, z0, 12, 4, 0.45, 10, (wr, x, y, z, a, b) -> { if (flat(16, x, z)) chorus(wr, x, y, z, a, b); });
                scatter(16, w, x0, z0, 46, 7, 0.55, 20, (wr, x, y, z, a, b) -> spire(wr, x, y, z, a));
                scatter(16, w, x0, z0, 80, 14, 0.45, 30, TowerFloorTerrainBeyond::voidIslet);
            }
            case 17 -> {
                scatter(17, w, x0, z0, 9, 6, 0.5, 10, (wr, x, y, z, a, b) -> { if (!karst(x, z) && flat(17, x, z)) bambooGrove(wr, x, z, a); });
                scatter(17, w, x0, z0, 8, 4, 0.5, 20, (wr, x, y, z, a, b) -> { if (karst(x, z) && flat(17, x, z)) crownTree(wr, x, y, z, a); });
                scatter(17, w, x0, z0, 7, 2, 0.25, 30, (wr, x, y, z, a, b) -> { if (!karst(x, z) && flat(17, x, z)) azalea(wr, x, y, z, a); });
            }
            case 18 -> {
                scatter(18, w, x0, z0, 14, 3, 0.35, 10, (wr, x, y, z, a, b) -> { if (y < 80 && flat(18, x, z)) hoodoo(wr, x, y, z, a, b); });
                scatter(18, w, x0, z0, 52, 6, 0.4, 20, (wr, x, y, z, a, b) -> { if (flat(18, x, z)) goldenColumns(wr, x, y, z, a, b); });
                scatter(18, w, x0, z0, 18, 4, 0.35, 30, (wr, x, y, z, a, b) -> { if (y > 90 && flat(18, x, z)) deadTree(wr, x, y, z, a, Blocks.STRIPPED_ACACIA_LOG.defaultBlockState()); });
            }
            default -> {
                scatter(19, w, x0, z0, 10, 6, 0.4, 10, (wr, x, y, z, a, b) -> { if (flat(19, x, z)) webbedTree(wr, x, y, z, a, b); });
                scatter(19, w, x0, z0, 70, 16, 0.5, 20, TowerFloorTerrainBeyond::ribcage);
                scatter(19, w, x0, z0, 22, 3, 0.4, 30, (wr, x, y, z, a, b) -> TowerFloorTerrain.boulder(wr, x, y - 1, z, a, Blocks.TUFF.defaultBlockState(), Blocks.COBBLED_DEEPSLATE.defaultBlockState()));
            }
        }
    }

    private static boolean flat(int floor, int x, int z) {
        return Math.abs(height(floor, x + 2, z) - height(floor, x - 2, z)) + Math.abs(height(floor, x, z + 2) - height(floor, x, z - 2)) <= 3;
    }

    private static BlockState ground(int floor, int x, int y, int z, int h, boolean steep) {
        double v = n(floor, x, z, 0.06, 2);
        switch (floor) {
            case 16 -> {
                if (y > h - 6) {
                    if (y == h && v > 0.5) return Blocks.END_STONE_BRICKS.defaultBlockState();
                    if (hash(16, x, z, y) < 0.004) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    return Blocks.END_STONE.defaultBlockState();
                }
                return hash(16, x, z, y + 500) < 0.05 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
            }
            case 17 -> {
                boolean tower = karst(x, z);
                if (y == h && !steep) return tower || v < 0.35 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.PODZOL.defaultBlockState();
                if (y > h - 3 && !steep) return Blocks.DIRT.defaultBlockState();
                double m = n(17, x + y, z - y, 0.07, 2);
                if (y > h - 2 && steep && m > 0.1) return Blocks.MOSS_BLOCK.defaultBlockState();
                return m > 0.35 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : m < -0.35 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
            case 18 -> {
                if (y == h && !steep) return h > 90 ? (v > 0.3 ? Blocks.RED_SAND.defaultBlockState() : Blocks.RED_SANDSTONE.defaultBlockState())
                        : (v > 0.2 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.RED_SAND.defaultBlockState());
                if (y > 68) return band(y);
                return y > h - 4 ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
            default -> {
                if (y == h) {
                    if (steep) return Blocks.TUFF.defaultBlockState();
                    return v > 0.45 ? Blocks.SOUL_SOIL.defaultBlockState() : v > 0.1 ? Blocks.COARSE_DIRT.defaultBlockState()
                            : v < -0.4 ? Blocks.GRAVEL.defaultBlockState() : Blocks.PODZOL.defaultBlockState();
                }
                if (y > h - 3) return Blocks.DIRT.defaultBlockState();
                return y > h - 10 ? Blocks.TUFF.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
            }
        }
    }

    /** The canyon walls' coloured strata (no white). */
    private static BlockState band(int y) {
        return switch (Math.floorMod(y / 3 + (y % 11 == 0 ? 2 : 0), 7)) {
            case 0 -> Blocks.TERRACOTTA.defaultBlockState();
            case 1 -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
            case 2 -> Blocks.RED_SANDSTONE.defaultBlockState();
            case 3 -> Blocks.RED_TERRACOTTA.defaultBlockState();
            case 4 -> Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
            case 5 -> Blocks.BROWN_TERRACOTTA.defaultBlockState();
            default -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
        };
    }

    private static void plants(int floor, TowerFloorTerrain.Writer w, int x, int y, int z) {
        double r = hash(floor, x, z, 77);
        BlockState below = w.get(x, y - 1, z);
        switch (floor) {
            case 16 -> {
                if (r < 0.012) w.put(x, y, z, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
                else if (r < 0.03) w.put(x, y, z, Blocks.SMALL_AMETHYST_BUD.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
            }
            case 17 -> {
                if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.PODZOL)) return;
                if (r < 0.26) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                else if (r < 0.34) w.put(x, y, z, Blocks.FERN.defaultBlockState());
                else if (r < 0.355) w.put(x, y, z, Blocks.LILY_OF_THE_VALLEY.defaultBlockState());
                else if (r < 0.36) w.put(x, y, z, Blocks.BAMBOO_SAPLING.defaultBlockState());
            }
            case 18 -> {
                if (below.is(Blocks.RED_SAND) || below.is(Blocks.COARSE_DIRT)) {
                    if (r < 0.03) w.put(x, y, z, Blocks.DEAD_BUSH.defaultBlockState());
                    else if (r < 0.036 && below.is(Blocks.RED_SAND)) cactus(w, x, y, z, r);
                }
            }
            default -> {
                if (r < 0.005) w.put(x, y, z, Blocks.COBWEB.defaultBlockState());
                else if (r < 0.06 && below.is(Blocks.PODZOL)) w.put(x, y, z, Blocks.DEAD_BUSH.defaultBlockState());
            }
        }
    }

    private static void cactus(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 1 + (int) (r * 100) % 3;
        for (int i = 0; i < h; i++) w.put(x, y + i, z, Blocks.CACTUS.defaultBlockState());
    }

    // ── features ─────────────────────────────────────────────────────────────

    private interface Feature { void place(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2); }

    private static void scatter(int floor, TowerFloorTerrain.Writer w, int x0, int z0, int cell, int margin, double chance, int salt, Feature f) {
        int c0 = Math.floorDiv(x0 - margin, cell), c1 = Math.floorDiv(x0 + 15 + margin, cell);
        int d0 = Math.floorDiv(z0 - margin, cell), d1 = Math.floorDiv(z0 + 15 + margin, cell);
        for (int a = c0; a <= c1; a++)
            for (int b = d0; b <= d1; b++) {
                if (hash(floor, a, b, salt) > chance) continue;
                int x = a * cell + (int) (hash(floor, a, b, salt + 1) * cell);
                int z = b * cell + (int) (hash(floor, a, b, salt + 2) * cell);
                f.place(w, x, height(floor, x, z) + 1, z, hash(floor, a, b, salt + 3), hash(floor, a, b, salt + 4));
            }
    }

    /** A chorus "tree": a stem with branches that step out and up, ending in flowers, its joins drawn properly. */
    private static void chorus(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        Set<Long> plant = new HashSet<>(), flowers = new HashSet<>();
        int stem = 3 + (int) (r1 * 5);
        for (int i = 0; i < stem; i++) plant.add(BlockPos.asLong(x, y + i, z));
        int branches = 2 + (int) (r2 * 3);
        for (int b = 0; b < branches; b++) {
            Direction d = Direction.from2DDataValue((int) (hash(16, x, z, b) * 4));
            int by = y + stem - 1 - (b % 2) * 2;
            int bx = x, bz = z;
            int out = 1 + (int) (hash(16, x + b, z, 3) * 2), up = 1 + (int) (hash(16, x, z + b, 4) * 3);
            for (int k = 0; k < out; k++) { bx += d.getStepX(); bz += d.getStepZ(); plant.add(BlockPos.asLong(bx, by, bz)); }
            for (int k = 1; k <= up; k++) plant.add(BlockPos.asLong(bx, by + k, bz));
            flowers.add(BlockPos.asLong(bx, by + up + 1, bz));
        }
        flowers.add(BlockPos.asLong(x, y + stem, z));
        Set<Long> all = new HashSet<>(plant);
        all.addAll(flowers);
        for (long l : plant) {
            BlockPos at = BlockPos.of(l);
            BlockState s = Blocks.CHORUS_PLANT.defaultBlockState();
            for (Map.Entry<Direction, net.minecraft.world.level.block.state.properties.BooleanProperty> e : PipeBlock.PROPERTY_BY_DIRECTION.entrySet()) {
                boolean joined = all.contains(at.relative(e.getKey()).asLong()) || (e.getKey() == Direction.DOWN && at.getY() == y);
                s = s.setValue(e.getValue(), joined);
            }
            w.put(at.getX(), at.getY(), at.getZ(), s);
        }
        for (long l : flowers) {
            BlockPos at = BlockPos.of(l);
            w.put(at.getX(), at.getY(), at.getZ(), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5));
        }
    }

    /** A tapering obsidian spire veined with crying obsidian, an end rod burning on its tip. */
    private static void spire(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int tall = 14 + (int) (r * 22);
        double rad = 2.2 + r * 1.8;
        for (int k = -2; k < tall; k++) {
            double rr = rad * (1 - (double) Math.max(0, k) / (tall + 2));
            int ri = (int) Math.ceil(rr);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rr * rr + 0.3)
                        w.force(x + dx, y + k, z + dz, hash(16, x + dx, z + dz, k) < 0.12 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
        }
        w.put(x, y + tall, z, Blocks.END_ROD.defaultBlockState());
    }

    /** An end-stone islet floating in the void sky, with a chorus tree on it. */
    private static void voidIslet(TowerFloorTerrain.Writer w, int x, int ground, int z, double r1, double r2) {
        int cy = 118 + (int) (r2 * 26), rad = 6 + (int) (r1 * 7);
        for (int dx = -rad - 1; dx <= rad + 1; dx++)
            for (int dz = -rad - 1; dz <= rad + 1; dz++) {
                double d = Math.hypot(dx, dz) + hash(16, x + dx, z + dz, 5) * 1.2;
                if (d > rad) continue;
                int depth = (int) ((rad - d) * 1.4) + 1;
                for (int k = 0; k < depth; k++)
                    w.force(x + dx, cy - k, z + dz, k > 2 && hash(16, x + dx, z + dz, k) < 0.15 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState());
            }
        chorus(w, x, cy + 1, z, r2, r1);
    }

    /** A clump of tall bamboo stalks, leafy at the top. */
    private static void bambooGrove(TowerFloorTerrain.Writer w, int cx, int cz, double r) {
        int count = 6 + (int) (r * 10);
        for (int i = 0; i < count; i++) {
            int x = cx + (int) Math.round((hash(17, cx, cz, i * 3) - 0.5) * 7), z = cz + (int) Math.round((hash(17, cx, cz, i * 3 + 1) - 0.5) * 7);
            if (karst(x, z)) continue;
            int y = height(17, x, z) + 1, h = 8 + (int) (hash(17, x, z, 9) * 9);
            if (!w.get(x, y - 1, z).is(Blocks.GRASS_BLOCK) && !w.get(x, y - 1, z).is(Blocks.PODZOL) && !w.get(x, y - 1, z).is(Blocks.DIRT)) continue;
            for (int k = 0; k < h; k++) {
                BambooLeaves leaves = k >= h - 2 ? BambooLeaves.LARGE : k == h - 3 ? BambooLeaves.SMALL : BambooLeaves.NONE;
                w.put(x, y + k, z, Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.AGE, 1).setValue(BambooStalkBlock.LEAVES, leaves));
            }
        }
    }

    /** A wind-shaped tree on a karst tower's crown. */
    private static void crownTree(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        boolean pine = r < 0.5;
        int h = 4 + (int) (r * 4);
        BlockState log = (pine ? Blocks.SPRUCE_LOG : Blocks.JUNGLE_LOG).defaultBlockState();
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, log);
        TowerFloorTerrain.blob(w, x, y + h, z, pine ? 2.2 : 3.0, TowerFloorTerrain.leaves(pine ? Blocks.SPRUCE_LEAVES : Blocks.AZALEA_LEAVES), !pine);
    }

    private static void azalea(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        w.put(x, y, z, r < 0.5 ? Blocks.AZALEA.defaultBlockState() : Blocks.FLOWERING_AZALEA.defaultBlockState());
    }

    /** A hoodoo: a banded pillar of soft rock with a harder capstone. */
    private static void hoodoo(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        int tall = 6 + (int) (r1 * 10);
        double rad = 1.0 + r2 * 1.4;
        int ri = (int) Math.ceil(rad + 1);
        for (int k = -1; k < tall; k++) {
            double rr = rad * (k > tall - 3 ? 1.35 : 1 - 0.2 * Math.sin(k * 0.6));
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rr * rr + 0.4)
                        w.force(x + dx, y + k, z + dz, k >= tall - 2 ? Blocks.RED_SANDSTONE.defaultBlockState() : band(y + k));
        }
    }

    /** Ruined golden columns of the solar temple, some standing, one fallen. */
    private static void goldenColumns(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        BlockState shaft = Blocks.CUT_RED_SANDSTONE.defaultBlockState(), ring = Blocks.CHISELED_RED_SANDSTONE.defaultBlockState(), gold = Blocks.GOLD_BLOCK.defaultBlockState();
        int count = 3 + (int) (r1 * 3);
        for (int i = 0; i < count; i++) {
            int cx = x + (i % 3) * 5 - 5, cz = z + (i / 3) * 6 - 3;
            int cy = height(18, cx, cz) + 1, tall = 3 + (int) (hash(18, cx, cz, 4) * 7);
            for (int k = -1; k < tall; k++) w.force(cx, cy + k, cz, k == 0 || k % 4 == 3 ? ring : shaft);
            if (tall > 7) w.force(cx, cy + tall, cz, gold);
        }
        // a fallen drum lying beside them
        Direction d = Direction.from2DDataValue((int) (r2 * 4));
        for (int k = 0; k < 4; k++) {
            int fx = x + 6 + d.getStepX() * k, fz = z + 6 + d.getStepZ() * k;
            w.force(fx, height(18, fx, fz) + 1, fz, k == 3 ? gold : shaft);
        }
    }

    /** A bare dead tree with crooked branches. */
    private static void deadTree(TowerFloorTerrain.Writer w, int x, int y, int z, double r, BlockState log) {
        int h = 4 + (int) (r * 5);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, log);
        for (int b = 0; b < 3; b++) {
            Direction d = Direction.from2DDataValue((int) (hash(19, x, z, b) * 4));
            int by = y + h - 1 - b, bx = x, bz = z;
            for (int k = 1; k <= 2 + b % 2; k++) {
                bx += d.getStepX(); bz += d.getStepZ();
                w.trunk(bx, by + k / 2, bz, log.hasProperty(RotatedPillarBlock.AXIS) ? log.setValue(RotatedPillarBlock.AXIS, d.getAxis()) : log);
            }
        }
    }

    /** A dead tree of the antechamber, its branches strung with webs. */
    private static void webbedTree(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        deadTree(w, x, y, z, r1, (r2 < 0.5 ? Blocks.STRIPPED_SPRUCE_LOG : Blocks.STRIPPED_DARK_OAK_LOG).defaultBlockState());
        int h = 4 + (int) (r1 * 5);
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = h - 4; dy <= h + 1; dy++)
                for (int dz = -3; dz <= 3; dz++)
                    if (hash(19, x + dx, z + dz, y + dy) < 0.035) w.put(x + dx, y + dy, z + dz, Blocks.COBWEB.defaultBlockState());
    }

    /** The bones of something huge: a spine with ribs arching over it, half sunk in the ash. */
    private static void ribcage(TowerFloorTerrain.Writer w, int x, int ground, int z, double r1, double r2) {
        boolean alongX = r2 < 0.5;
        int len = 24 + (int) (r1 * 20), rib = 7 + (int) (r1 * 5);
        BlockState spineState = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, alongX ? Direction.Axis.X : Direction.Axis.Z);
        for (int k = 0; k < len; k++) {
            int sx = alongX ? x + k : x, sz = alongX ? z : z + k;
            int gy = height(19, sx, sz);
            w.force(sx, gy, sz, spineState);
            if (k % 4 != 1 || k > len - 6) continue;
            double size = rib * (1 - Math.abs(k - len * 0.4) / len);
            for (int side = -1; side <= 1; side += 2)
                for (double a = 0; a <= Math.PI * 0.95; a += 0.12) {
                    int off = (int) Math.round(Math.sin(a) * size) * side, up = (int) Math.round((1 - Math.cos(a)) * size * 0.9);
                    int rx = alongX ? sx : sx + off, rz = alongX ? sz + off : sz;
                    w.force(rx, gy + up, rz, Blocks.BONE_BLOCK.defaultBlockState());
                }
        }
    }

    // ── floor 20: the nest ───────────────────────────────────────────────────

    private static void fillNest(ChunkAccess chunk, TowerFloorTerrain.Writer w, int x0, int z0, BlockPos.MutableBlockPos p) {
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int g = nestGround(x, z), roof = nestRoof(x, z);
                boolean deck = onArena(x, z);
                double ramp = rampAt(x, z);
                for (int y = 0; y <= 127; y++) {
                    BlockState s;
                    if (y <= 1 || y >= 126) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y <= g) s = y == g ? nestFloor(x, z) : y > g - 4 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState();
                    else if (y >= roof) s = hash(20, x, z, y) < 0.02 ? Blocks.SCULK.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
                    else s = Blocks.AIR.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                if (deck) deckColumn(w, x, z, g);
                else if (ramp >= 0) rampColumn(w, x, z, g, ramp);
                else {
                    double r = hash(20, x, z, 7);
                    if (r < 0.05) w.put(x, g + 1, z, Blocks.COBWEB.defaultBlockState());
                    else if (r < 0.065) w.put(x, g + 1, z, Blocks.BONE_BLOCK.defaultBlockState());
                    if (hash(20, x, z, 8) < 0.03) for (int k = 1; k <= 1 + (int) (hash(20, x, z, 9) * 4); k++) w.put(x, roof - k, z, Blocks.COBWEB.defaultBlockState());
                }
            }
        scatter(20, w, x0, z0, 30, 8, 0.6, 10, (wr, x, y, z, a, b) -> { if (clear(x, z, 36)) silkPillar(wr, x, z, a); });
        scatter(20, w, x0, z0, 11, 4, 0.5, 20, (wr, x, y, z, a, b) -> { if (clear(x, z, 30)) cocoon(wr, x, nestGround(x, z) + 1, z, a); });
        scatter(20, w, x0, z0, 9, 2, 0.4, 30, (wr, x, y, z, a, b) -> { if (clear(x, z, 30)) cocoon(wr, x, nestRoof(x, z) - 1, z, -a); });
        if (Math.abs(x0) < 80 && Math.abs(z0) < 80) strands(w);
    }

    /** Keeps big things off the arena, its ramp and the portals in the far corners. */
    private static boolean clear(int x, int z, int arena) {
        int r = Math.max(arena, RAMP_OUT + 6);
        return x * x + z * z > r * r && Math.hypot(Math.abs(x) - 220, Math.abs(z) - 220) > 24;
    }

    private static BlockState nestFloor(int x, int z) {
        double v = n(20, x, z, 0.05, 2);
        if (v > 0.4) return Blocks.SCULK.defaultBlockState();
        if (v < -0.5) return Blocks.SOUL_SOIL.defaultBlockState();
        return v < -0.2 ? Blocks.TUFF.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
    }

    private static final BlockState SILK = Blocks.MUSHROOM_STEM.defaultBlockState();
    private static final BlockState EGG = Blocks.VERDANT_FROGLIGHT.defaultBlockState();

    /**
     * The arena. It is the queen's nest: a disc whose floor is drawn as a spider web, held up
     * by an upturned cone of silk-wrapped rock with webs hanging under its overhang, and ringed by a low silk rim with
     * glowing eggs in it, open only where the ramp arrives.
     */
    private static void deckColumn(TowerFloorTerrain.Writer w, int x, int z, int ground) {
        boolean arena = onArena(x, z);
        double d = Math.hypot(x, z);
        if (arena) {
            for (int y = ground; y < DECK; y++) {
                double t = (DECK - y) / (double) Math.max(1, DECK - ground);      // 0 under the deck, 1 at the cave floor
                double rr = ARENA_R * (1 - 0.72 * Math.pow(t, 0.7)) + 1.5 * n(20, x + y * 7, z, 0.2, 1);
                if (d <= rr) w.force(x, y, z, hash(20, x, z, y + 40) < 0.3 ? SILK : Blocks.DEEPSLATE.defaultBlockState());
                else if (d <= rr + 1.5 && hash(20, x, z, y + 90) < 0.25) w.put(x, y, z, Blocks.COBWEB.defaultBlockState());
            }
            w.force(x, DECK, z, webFloor(x, z, d));
            boolean entry = arenaEntry(x, z);
            if (d > ARENA_R - 1.5 && !entry) {
                double h = hash(20, x, z, 1);
                if (h < 0.08) w.force(x, DECK + 1, z, EGG);
                else if (h < 0.7) w.force(x, DECK + 1, z, SILK);
                if (h > 0.2 && h < 0.45) w.force(x, DECK + 2, z, SILK);
                else if (h >= 0.45 && h < 0.6) w.put(x, DECK + 2, z, Blocks.COBWEB.defaultBlockState());
            }
            return;
        }
    }

    /**
     * The ramp: a walk of rock four blocks thick that winds once round the arena's pillar, from the cave floor up
     * to the arena's way in, its outer edge wrapped in silk, lit by soul lanterns, and webs hanging underneath.
     */
    private static void rampColumn(TowerFloorTerrain.Writer w, int x, int z, int ground, double s) {
        int top = Math.max(ground, rampY(s));
        double d = Math.hypot(x, z);
        boolean outer = d >= RAMP_OUT - 1.2;
        boolean pier = (int) (s * 40) % 5 == 0;                     // a rock pier down to the floor now and then
        int bottom = pier ? ground : Math.max(ground, top - 4);
        for (int y = bottom; y < top; y++) w.force(x, y, z, hash(20, x, z, y) < 0.2 ? SILK : Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        if (!pier && top - 4 > ground && hash(20, x, z, 5) < 0.2)
            for (int k = 5; k <= 5 + (int) (hash(20, x, z, 6) * 4) && top - k > ground; k++) w.put(x, top - k, z, Blocks.COBWEB.defaultBlockState());
        w.force(x, top, z, outer ? SILK : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        if (outer && (int) (s * 120) % 9 == 0) {
            w.force(x, top + 1, z, Blocks.DEEPSLATE_TILE_WALL.defaultBlockState());
            w.force(x, top + 2, z, Blocks.SOUL_LANTERN.defaultBlockState());
        }
    }

    /** The arena floor: dark tiles with a spider web drawn on them in silk (twelve spokes and rings). */
    private static BlockState webFloor(int x, int z, double d) {
        if (d < 1.5) return SILK;
        double a = Math.atan2(z, x), step = Math.PI / 6;
        double off = Math.abs(a - Math.round(a / step) * step);
        boolean spoke = d * Math.sin(off) < 0.55;
        boolean ring = d > 3 && Math.abs(d - Math.round(d / 4.5) * 4.5) < 0.5;
        if (spoke || ring) return SILK;
        return hash(20, x, z, 3) < 0.15 ? Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    /** Thick silk strands from the arena's rim up to the roof, as if the nest hung from them. */
    private static void strands(TowerFloorTerrain.Writer w) {
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(60 * i);   // none at the arena's way in
            double sx = Math.cos(a) * (ARENA_R - 1), sz = Math.sin(a) * (ARENA_R - 1);
            double ex = Math.cos(a) * (ARENA_R + 26), ez = Math.sin(a) * (ARENA_R + 26);
            int ey = nestRoof((int) ex, (int) ez) + 1;
            int steps = 140;
            for (int s = 0; s <= steps; s++) {
                double t = s / (double) steps, sag = Math.sin(t * Math.PI) * 4;   // a slight sag in the middle
                int x = (int) Math.round(sx + (ex - sx) * t), z = (int) Math.round(sz + (ez - sz) * t);
                int y = (int) Math.round(DECK + 1 + (ey - DECK - 1) * t - sag);
                w.force(x, y, z, SILK);
                w.force(x, y - 1, z, SILK);
                if (hash(20, x, z, y + 500) < 0.3) w.put(x + 1, y, z, Blocks.COBWEB.defaultBlockState());
                if (hash(20, x, z, y + 600) < 0.3) w.put(x, y, z + 1, Blocks.COBWEB.defaultBlockState());
            }
        }
    }

    /** A rock pillar from floor to roof, wrapped in silk. */
    private static void silkPillar(TowerFloorTerrain.Writer w, int x, int z, double r) {
        int bottom = nestGround(x, z), top = nestRoof(x, z);
        double rad = 2.5 + r * 3;
        int ri = (int) Math.ceil(rad + 1.5);
        for (int y = bottom - 1; y <= top; y++) {
            double t = (y - bottom) / (double) Math.max(1, top - bottom);
            double rr = rad * (0.7 + 0.6 * Math.abs(t - 0.5));
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.hypot(dx, dz);
                    if (d <= rr) w.force(x + dx, y, z + dz, hash(20, x + dx, z + dz, y) < 0.25 ? Blocks.MUSHROOM_STEM.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState());
                    else if (d <= rr + 1.2 && hash(20, x + dx, z + dz, y + 300) < 0.18) w.put(x + dx, y, z + dz, Blocks.COBWEB.defaultBlockState());
                }
        }
    }

    /** A silk cocoon (an egg sac): a lump of pale silk with webs round it; {@code r < 0} hangs it from the roof. */
    private static void cocoon(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        boolean hanging = r < 0;
        double rad = 1.2 + Math.abs(r) * 1.6;
        int ri = (int) Math.ceil(rad + 1);
        for (int dx = -ri; dx <= ri; dx++)
            for (int dy = -ri; dy <= ri; dy++)
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy * 0.6 + dz * dz);
                    int yy = hanging ? y - ri + dy : y + ri + dy - 1;
                    if (d <= rad) w.force(x + dx, yy, z + dz, d > rad - 1 && hash(20, x + dx, z + dz, yy + 70) < 0.12 ? EGG : SILK);
                    else if (d <= rad + 1 && hash(20, x + dx, z + dz, yy) < 0.3) w.put(x + dx, yy, z + dz, Blocks.COBWEB.defaultBlockState());
                }
    }

}
