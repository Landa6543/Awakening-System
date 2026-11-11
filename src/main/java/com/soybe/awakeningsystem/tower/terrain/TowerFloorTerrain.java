package com.soybe.awakeningsystem.tower.terrain;

import com.soybe.awakeningsystem.tower.TowerSimplexNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Land of the Great Tower's surface floors, none of it Minecraft's own terrain and always dry:
 * <ol>
 *   <li>Golden Plains of the Celestial Dawn: rolling golden meadows, wide-crowned white-trunk trees, boulders
 *       and grassy islets floating in the sky.</li>
 *   <li>Valley of Spectral Mist: misty valleys between ridges, giant spirit trees with roots, branches and
 *       hanging vines, moss and mushrooms.</li>
 *   <li>Celestial Peaks: huge stone spires banded white, grey and orange, green-capped with pines, and the
 *       canyons between them.</li>
 *   <li>Bioluminescent Grove: dark teal ground under giant mushrooms that glow beneath their caps.</li>
 * </ol>
 * Every feature is placed from a grid of cells hashed from its position, so trees that cross a chunk edge
 * are finished by the neighbouring chunk.
 */
public final class TowerFloorTerrain {
    private TowerFloorTerrain() {}

    private static final TowerSimplexNoise[] NOISE = new TowerSimplexNoise[8];
    static {
        for (int i = 0; i < NOISE.length; i++) NOISE[i] = new TowerSimplexNoise(918273645L * (i + 7) + 31L);
    }

    static double n(int floor, double x, double z, double scale, int octaves) {
        return NOISE[floor % NOISE.length].fbm2D(x * scale, z * scale, octaves, 2.0D, 0.5D);
    }

    /** A stable 0..1 value for a cell and a salt. */
    static double hash(int floor, int cx, int cz, int salt) {
        long h = cx * 341873128712L ^ cz * 132897987541L ^ (floor * 2654435761L + salt * 40503L);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        return (h & 0xFFFFFFL) / (double) 0xFFFFFF;
    }

    // ── heights ──────────────────────────────────────────────────────────────

    /** Floors whose land this generator makes (1-15; floor 10 is the titan's vault with its arena). */
    public static boolean isCustom(int floor) {
        return floor >= 1 && floor <= 15;
    }

    /** Floors that are caverns under a rock ceiling (their world is 128 blocks tall, from 0). */
    public static boolean isCave(int floor) {
        return floor == 5 || floor == 6 || floor == 10 || floor == 15;
    }

    public static int height(int floor, int x, int z) {
        if (floor >= 11) return TowerFloorTerrainHigh.height(floor, x, z);
        if (floor == 10) return TowerFloorTerrainVault.height(x, z);
        if (floor >= 5) return TowerFloorTerrainDeep.height(floor, x, z);
        return switch (floor) {
            case 1 -> 72 + (int) Math.round(5 * n(1, x, z, 0.006, 3) + 3 * n(1, x + 500, z, 0.02, 2) + 1.2 * n(1, x, z + 900, 0.07, 1));
            case 2 -> {
                double r = 1.0 - Math.abs(n(2, x, z, 0.005, 3));
                yield 66 + (int) Math.round(26 * r * r * r + 3 * n(2, x + 300, z, 0.03, 2));
            }
            case 3 -> peaksHeight(x, z);
            default -> 68 + (int) Math.round(8 * n(4, x, z, 0.008, 3) + 3 * n(4, x - 700, z, 0.04, 2));
        };
    }

    private static int peaksBase(int x, int z) {
        return 70 + (int) Math.round(6 * n(3, x, z, 0.008, 3) + 2 * n(3, x, z + 400, 0.05, 1));
    }

    private static final int SPIRE_CELL = 26;

    /** The valley floor, raised into a flat-topped spire wherever one of the hashed spire cells reaches. */
    private static int peaksHeight(int x, int z) {
        int base = peaksBase(x, z);
        int best = base;
        int ci = Math.floorDiv(x, SPIRE_CELL), cj = Math.floorDiv(z, SPIRE_CELL);
        for (int a = ci - 1; a <= ci + 1; a++) {
            for (int b = cj - 1; b <= cj + 1; b++) {
                if (hash(3, a, b, 1) > 0.62) continue;
                double sx = a * SPIRE_CELL + 5 + hash(3, a, b, 2) * (SPIRE_CELL - 10);
                double sz = b * SPIRE_CELL + 5 + hash(3, a, b, 3) * (SPIRE_CELL - 10);
                double radius = 4.5 + hash(3, a, b, 4) * 5.0;
                double rr = radius * (1.0 + 0.28 * n(3, x, z, 0.16, 2));
                double d = Math.sqrt((x - sx) * (x - sx) + (z - sz) * (z - sz));
                int spireH = 34 + (int) (hash(3, a, b, 5) * 56);
                int top;
                if (d < rr) top = base + spireH - (d > rr - 1.6 ? (int) ((d - (rr - 1.6)) * 5) : 0);
                else if (d < rr + 3.5) top = base + (int) ((rr + 3.5 - d) * 2.5);      // scree at the foot
                else continue;
                best = Math.max(best, top);
            }
        }
        return best;
    }

    // ── filling a chunk ──────────────────────────────────────────────────────

    public static void fill(int floor, ChunkAccess chunk) {
        if (floor >= 11) {
            TowerFloorTerrainHigh.fill(floor, chunk);
            return;
        }
        if (floor == 10) {
            TowerFloorTerrainVault.fill(chunk);
            return;
        }
        if (floor >= 5) {
            TowerFloorTerrainDeep.fill(floor, chunk);
            return;
        }
        Writer w = new Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int h = height(floor, x, z);
                boolean spire = floor == 3 && h > peaksBase(x, z) + 6;
                for (int y = -64; y <= h; y++) {
                    BlockState s;
                    if (y == -64) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y < 0) s = Blocks.DEEPSLATE.defaultBlockState();
                    else if (y == h) s = surface(floor, x, z, spire);
                    else if (y > h - 4) s = subsurface(floor, x, z, y, h, spire);
                    else s = spire ? band(y) : Blocks.STONE.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                ground(floor, w, x, h + 1, z);
            }
        }
        features(floor, w, x0, z0);
    }

    private static BlockState surface(int floor, int x, int z, boolean spire) {
        double v = n(floor, x, z, 0.09, 2);
        return switch (floor) {
            case 1 -> v > 0.62 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            case 2 -> v > 0.35 ? Blocks.MOSS_BLOCK.defaultBlockState() : (v < -0.55 ? Blocks.PODZOL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
            case 3 -> spire || v < 0.45 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            default -> v > 0.3 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.WARPED_NYLIUM.defaultBlockState();
        };
    }

    private static BlockState subsurface(int floor, int x, int z, int y, int h, boolean spire) {
        if (floor == 4) return Blocks.NETHERRACK.defaultBlockState();
        if (floor == 3 && spire && y < h - 2) return band(y);
        return Blocks.DIRT.defaultBlockState();
    }

    /** The coloured strata of the celestial spires. */
    private static BlockState band(int y) {
        int k = Math.floorMod(y / 3 + (y % 7 == 0 ? 1 : 0), 6);
        return switch (k) {
            case 0 -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
            case 1 -> Blocks.CALCITE.defaultBlockState();
            case 2 -> Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
            case 3 -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
            case 4 -> Blocks.STONE.defaultBlockState();
            default -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
        };
    }

    /** Grass, flowers, ferns, roots... on the ground, one column at a time. */
    private static void ground(int floor, Writer w, int x, int y, int z) {
        double r = hash(floor, x, z, 77);
        BlockState below = w.get(x, y - 1, z);
        BlockState s = null;
        switch (floor) {
            case 1 -> {
                if (r < 0.22) s = Blocks.SHORT_GRASS.defaultBlockState();
                else if (r < 0.245) s = Blocks.DANDELION.defaultBlockState();
                else if (r < 0.26) s = Blocks.ORANGE_TULIP.defaultBlockState();
                else if (r < 0.27) s = Blocks.OXEYE_DAISY.defaultBlockState();
            }
            case 2 -> {
                if (below.is(Blocks.MOSS_BLOCK) && r < 0.18) s = Blocks.MOSS_CARPET.defaultBlockState();
                else if (r < 0.12) s = Blocks.FERN.defaultBlockState();
                else if (r < 0.2) s = Blocks.SHORT_GRASS.defaultBlockState();
                else if (r < 0.215) s = Blocks.BROWN_MUSHROOM.defaultBlockState();
                else if (r < 0.222) s = Blocks.RED_MUSHROOM.defaultBlockState();
                else if (r < 0.228) s = Blocks.LILY_OF_THE_VALLEY.defaultBlockState();
            }
            case 3 -> {
                if (below.is(Blocks.GRASS_BLOCK) && r < 0.18) s = Blocks.SHORT_GRASS.defaultBlockState();
                else if (below.is(Blocks.GRASS_BLOCK) && r < 0.19) s = Blocks.AZURE_BLUET.defaultBlockState();
            }
            default -> {
                if (below.is(Blocks.WARPED_NYLIUM)) {
                    if (r < 0.14) s = Blocks.WARPED_ROOTS.defaultBlockState();
                    else if (r < 0.26) s = Blocks.NETHER_SPROUTS.defaultBlockState();
                    else if (r < 0.28) s = Blocks.TWISTING_VINES.defaultBlockState();
                } else if (r < 0.1) s = Blocks.MOSS_CARPET.defaultBlockState();
            }
        }
        if (s != null) w.put(x, y, z, s);
    }

    // ── features ─────────────────────────────────────────────────────────────

    private interface Feature { void place(Writer w, int x, int y, int z, double r1, double r2); }

    /** Every cell of size {@code cell} near the chunk gets the feature with probability {@code chance}. */
    private static void scatter(int floor, Writer w, int x0, int z0, int cell, int margin, double chance, int salt, Feature f) {
        int c0 = Math.floorDiv(x0 - margin, cell), c1 = Math.floorDiv(x0 + 15 + margin, cell);
        int d0 = Math.floorDiv(z0 - margin, cell), d1 = Math.floorDiv(z0 + 15 + margin, cell);
        for (int a = c0; a <= c1; a++) {
            for (int b = d0; b <= d1; b++) {
                if (hash(floor, a, b, salt) > chance) continue;
                int x = a * cell + (int) (hash(floor, a, b, salt + 1) * cell);
                int z = b * cell + (int) (hash(floor, a, b, salt + 2) * cell);
                f.place(w, x, height(floor, x, z) + 1, z, hash(floor, a, b, salt + 3), hash(floor, a, b, salt + 4));
            }
        }
    }

    private static void features(int floor, Writer w, int x0, int z0) {
        switch (floor) {
            case 1 -> {
                scatter(1, w, x0, z0, 22, 7, 0.45, 10, (wr, x, y, z, a, b) -> goldenTree(wr, x, y, z, a));
                scatter(1, w, x0, z0, 34, 3, 0.35, 20, (wr, x, y, z, a, b) -> boulder(wr, x, y - 1, z, a, Blocks.CALCITE.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState()));
                scatter(1, w, x0, z0, 72, 14, 0.4, 30, TowerFloorTerrain::skyIslet);
            }
            case 2 -> {
                scatter(2, w, x0, z0, 15, 10, 0.68, 10, (wr, x, y, z, a, b) -> spiritTree(wr, x, y, z, a, b));
                scatter(2, w, x0, z0, 7, 3, 0.3, 20, (wr, x, y, z, a, b) -> bush(wr, x, y, z, a));
                scatter(2, w, x0, z0, 26, 3, 0.3, 30, (wr, x, y, z, a, b) -> boulder(wr, x, y - 1, z, a, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.MOSS_BLOCK.defaultBlockState()));
            }
            case 3 -> scatter(3, w, x0, z0, 8, 4, 0.42, 10, (wr, x, y, z, a, b) -> {
                // pines only where the ground is level enough (spire tops and the canyon floor)
                int dh = Math.abs(height(3, x + 2, z) - height(3, x - 2, z)) + Math.abs(height(3, x, z + 2) - height(3, x, z - 2));
                if (dh <= 3) pine(wr, x, y, z, a);
            });
            default -> {
                scatter(4, w, x0, z0, 13, 7, 0.66, 10, (wr, x, y, z, a, b) -> glowShroom(wr, x, y, z, a, b));
                scatter(4, w, x0, z0, 6, 2, 0.25, 20, (wr, x, y, z, a, b) -> smallShroom(wr, x, y, z, a));
            }
        }
    }

    static BlockState leaves(net.minecraft.world.level.block.Block b) {
        return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    /** A white trunk with a wide, layered golden crown. */
    private static void goldenTree(Writer w, int x, int y, int z, double r) {
        int h = 6 + (int) (r * 4);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, Blocks.BIRCH_LOG.defaultBlockState());
        BlockState leaf = leaves(Blocks.OAK_LEAVES);
        int top = y + h;
        double[] radii = {3.2, 4.6, 4.2, 2.8, 1.4};
        for (int l = 0; l < radii.length; l++) {
            int ly = top - 2 + l;
            double rad = radii[l];
            for (int dx = -5; dx <= 5; dx++)
                for (int dz = -5; dz <= 5; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d <= rad && !(d > rad - 0.8 && hash(1, x + dx, z + dz, ly) < 0.35)) w.put(x + dx, ly, z + dz, leaf);
                }
        }
        // a couple of short boughs under the crown
        for (int i = 0; i < 2; i++) {
            Direction d = Direction.from2DDataValue((int) (r * 97) + i * 2);
            for (int k = 1; k <= 2; k++) w.trunk(x + d.getStepX() * k, top - 3 + k / 2, z + d.getStepZ() * k,
                    Blocks.BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, d.getAxis()));
        }
    }

    static void boulder(Writer w, int x, int y, int z, double r, BlockState a, BlockState b) {
        int rad = 1 + (int) (r * 2.5);
        for (int dx = -rad; dx <= rad; dx++)
            for (int dy = 0; dy <= rad; dy++)
                for (int dz = -rad; dz <= rad; dz++)
                    if (dx * dx + dy * dy * 1.6 + dz * dz <= rad * rad + 0.5)
                        w.force(x + dx, y + dy, z + dz, hash(9, x + dx, z + dz, dy) < 0.6 ? a : b);
    }

    /** A grassy islet floating high over the plains, with a golden tree on it. */
    private static void skyIslet(Writer w, int x, int ground, int z, double r1, double r2) {
        int cy = 108 + (int) (r2 * 22);
        int rad = 6 + (int) (r1 * 6);
        for (int dx = -rad - 1; dx <= rad + 1; dx++) {
            for (int dz = -rad - 1; dz <= rad + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz) + hash(1, x + dx, z + dz, 5) * 1.2;
                if (d > rad) continue;
                int depth = (int) ((rad - d) * 1.3) + 1;
                for (int k = 0; k < depth; k++)
                    w.force(x + dx, cy - k, z + dz, k == 0 ? Blocks.GRASS_BLOCK.defaultBlockState()
                            : (k < 3 ? Blocks.DIRT.defaultBlockState() : (hash(1, x + dx, z + dz, k) < 0.15 ? Blocks.CALCITE.defaultBlockState() : Blocks.STONE.defaultBlockState())));
                if (hash(1, x + dx, z + dz, 6) < 0.25) w.put(x + dx, cy + 1, z + dz, Blocks.SHORT_GRASS.defaultBlockState());
            }
        }
        goldenTree(w, x, cy + 1, z, r2);
    }

    /** A giant spirit tree: a 2x2 trunk with root flares, branches and a broad crown with hanging vines. */
    private static void spiritTree(Writer w, int x, int y, int z, double r1, double r2) {
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
        BlockState leaf = leaves(Blocks.DARK_OAK_LEAVES);
        int h = 11 + (int) (r1 * 7);
        for (int i = -1; i < h; i++)
            for (int ox = 0; ox <= 1; ox++) for (int oz = 0; oz <= 1; oz++) w.trunk(x + ox, y + i, z + oz, log);
        // roots spreading over the ground
        for (Direction d : Direction.Plane.HORIZONTAL) {
            int len = 2 + (int) (hash(2, x, z, d.ordinal()) * 2);
            int bx = x + (d.getStepX() > 0 ? 2 : (d.getStepX() < 0 ? -1 : 0)), bz = z + (d.getStepZ() > 0 ? 2 : (d.getStepZ() < 0 ? -1 : 0));
            for (int k = 0; k < len; k++)
                w.trunk(bx + d.getStepX() * k, y - (k > 0 ? 1 : 0), bz + d.getStepZ() * k, log.setValue(RotatedPillarBlock.AXIS, d.getAxis()));
        }
        // branches with leaf clouds, then the crown
        int branches = 3 + (int) (r2 * 2);
        for (int i = 0; i < branches; i++) {
            double a = (r2 * 6.28) + i * 6.28 / branches;
            int by = y + h - 4 - (i % 2) * 2;
            int ex = x, ez = z;
            for (int k = 1; k <= 4; k++) {
                ex = x + (int) Math.round(Math.cos(a) * k);
                ez = z + (int) Math.round(Math.sin(a) * k);
                w.trunk(ex, by + k / 2, ez, log);
            }
            blob(w, ex, by + 2, ez, 2.6, leaf, true);
        }
        blob(w, x, y + h, z, 4.2, leaf, true);
    }

    /** A cloud of leaves; with vines, some strands hang from its underside. */
    static void blob(Writer w, int cx, int cy, int cz, double rad, BlockState leaf, boolean vines) {
        int ri = (int) Math.ceil(rad);
        for (int dx = -ri; dx <= ri; dx++)
            for (int dy = -ri; dy <= ri; dy++)
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy * 1.8 + dz * dz);
                    if (d > rad || (d > rad - 0.9 && hash(5, cx + dx, cz + dz, cy + dy) < 0.3)) continue;
                    w.put(cx + dx, cy + dy, cz + dz, leaf);
                    if (vines && dy < 0 && d > rad - 1.2 && hash(6, cx + dx, cz + dz, cy) < 0.18) {
                        int len = 1 + (int) (hash(7, cx + dx, cz + dz, cy) * 5);
                        for (int k = 1; k <= len; k++)
                            w.put(cx + dx, cy + dy - k, cz + dz, Blocks.VINE.defaultBlockState().setValue(VineBlock.UP, true));
                    }
                }
    }

    private static void bush(Writer w, int x, int y, int z, double r) {
        w.put(x, y, z, r < 0.5 ? Blocks.AZALEA.defaultBlockState() : Blocks.FLOWERING_AZALEA.defaultBlockState());
        if (r > 0.7) blob(w, x, y + 1, z, 1.4, leaves(Blocks.AZALEA_LEAVES), false);
    }

    /** A narrow pine: a spruce trunk with layered skirts of needles. */
    private static void pine(Writer w, int x, int y, int z, double r) {
        int h = 6 + (int) (r * 6);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, Blocks.SPRUCE_LOG.defaultBlockState());
        BlockState leaf = leaves(Blocks.SPRUCE_LEAVES);
        for (int i = 2; i <= h + 1; i++) {
            double rad = (i % 2 == 0 ? 2.6 : 1.6) * (1.0 - (double) i / (h + 3));
            int ri = (int) Math.ceil(rad);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rad * rad + 0.3 && (dx != 0 || dz != 0 || i > h - 1)) w.put(x + dx, y + i, z + dz, leaf);
        }
        w.put(x, y + h + 1, z, leaf);
    }

    /** A giant mushroom glowing under its cap, with weeping vines hanging from the rim. */
    private static void glowShroom(Writer w, int x, int y, int z, double r1, double r2) {
        int h = 7 + (int) (r1 * 9);
        boolean big = r1 > 0.7;
        BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState();
        for (int i = 0; i < h; i++) {
            w.trunk(x, y + i, z, stem);
            if (big) { w.trunk(x + 1, y + i, z, stem); w.trunk(x, y + i, z + 1, stem); w.trunk(x + 1, y + i, z + 1, stem); }
        }
        double rad = (big ? 5.5 : 3.8) + r2;
        BlockState cap = Blocks.WARPED_WART_BLOCK.defaultBlockState();
        int top = y + h;
        int ri = (int) Math.ceil(rad);
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dz = -ri; dz <= ri; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > rad) continue;
                int lift = (int) Math.round(Math.cos(d / rad * 1.4) * 2.5);      // a dome
                w.put(x + dx, top + lift, z + dz, cap);
                if (d > rad - 1.3) w.put(x + dx, top + lift - 1, z + dz, cap);
                // the glowing gills
                if (d < rad - 1.0 && hash(4, x + dx, z + dz, 11) < 0.32) w.put(x + dx, top + lift - 1, z + dz, Blocks.SHROOMLIGHT.defaultBlockState());
                if (d > rad - 1.0 && hash(4, x + dx, z + dz, 12) < 0.25) {
                    int len = 1 + (int) (hash(4, x + dx, z + dz, 13) * 4);
                    for (int k = 2; k <= len + 1; k++)
                        w.put(x + dx, top + lift - k, z + dz, k == len + 1 ? Blocks.WEEPING_VINES.defaultBlockState() : Blocks.WEEPING_VINES_PLANT.defaultBlockState());
                }
            }
        }
    }

    private static void smallShroom(Writer w, int x, int y, int z, double r) {
        int h = 2 + (int) (r * 2);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, Blocks.MUSHROOM_STEM.defaultBlockState());
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) w.put(x + dx, y + h, z + dz, Blocks.WARPED_WART_BLOCK.defaultBlockState());
        w.put(x, y + h - 1 + 0, z + 1, Blocks.SHROOMLIGHT.defaultBlockState());
    }

    /** Writes only into its own chunk, so features crossing a chunk edge are completed by the neighbour. */
    static final class Writer {
        final ChunkAccess chunk;
        final int x0, z0;
        final BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

        Writer(ChunkAccess chunk) {
            this.chunk = chunk;
            this.x0 = chunk.getPos().getMinBlockX();
            this.z0 = chunk.getPos().getMinBlockZ();
        }

        boolean inside(int x, int y, int z) {
            return x >= x0 && x < x0 + 16 && z >= z0 && z < z0 + 16 && y > chunk.getMinBuildHeight() && y < chunk.getMaxBuildHeight() - 1;
        }

        BlockState get(int x, int y, int z) {
            return inside(x, y, z) ? chunk.getBlockState(p.set(x, y, z)) : Blocks.AIR.defaultBlockState();
        }

        /** Places into air or plants only. */
        void put(int x, int y, int z, BlockState s) {
            if (!inside(x, y, z)) return;
            BlockState cur = chunk.getBlockState(p.set(x, y, z));
            if (cur.isAir() || cur.canBeReplaced()) chunk.setBlockState(p, s, false);
        }

        /** Trunks also push through leaves. */
        void trunk(int x, int y, int z, BlockState s) {
            if (!inside(x, y, z)) return;
            BlockState cur = chunk.getBlockState(p.set(x, y, z));
            if (cur.isAir() || cur.canBeReplaced() || cur.getBlock() instanceof LeavesBlock) chunk.setBlockState(p, s, false);
        }

        void force(int x, int y, int z, BlockState s) {
            if (inside(x, y, z)) chunk.setBlockState(p.set(x, y, z), s, false);
        }
    }
}
