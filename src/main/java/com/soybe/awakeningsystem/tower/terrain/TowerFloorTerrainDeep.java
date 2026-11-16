package com.soybe.awakeningsystem.tower.terrain;

import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TeyvatCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.hash;
import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.n;

/**
 * Floors 5 to 9 of the Great Tower:
 * <ol start="5">
 *   <li>Great Deep Cavern of Mana Crystals: one huge cavern, colossal pillars from floor to roof, giant
 *       stalactites and stalagmites, crystal spires and mana crystals on floor and roof, calcite only in veins.</li>
 *   <li>Volcanic Bastion of Ash and Magma: ash and basalt plains under a high rock roof, volcanoes with lava in
 *       their craters, narrow magma rivers and basalt columns.</li>
 *   <li>Astral Storm Plateau and Arcane Dunes: rolling dunes, banded mesas and shards of arcane crystal.</li>
 *   <li>Glacial Peaks of Boreal Frost: snowy ridges, glaciers, snow-laden boreal pines and ice spires.</li>
 *   <li>Sacred Sanctuary of the Plum Blossom: terraced hills of cherry trees, petals and stone lanterns.</li>
 * </ol>
 */
final class TowerFloorTerrainDeep {
    private TowerFloorTerrainDeep() {}

    // ── heights ──────────────────────────────────────────────────────────────

    static int height(int floor, int x, int z) {
        return switch (floor) {
            case 5 -> caveFloor(x, z);
            case 6 -> volcanicHeight(x, z);
            case 7 -> dunesHeight(x, z);
            case 8 -> {
                double r = 1.0 - Math.abs(n(8, x, z, 0.006, 3));
                yield 70 + (int) Math.round(48 * r * r * r + 10 * n(8, x + 200, z, 0.02, 2));
            }
            default -> {
                double raw = 70 + 24 * n(9, x, z, 0.007, 3);
                int step = (int) Math.floor(raw / 4.0) * 4;
                double frac = raw / 4.0 - Math.floor(raw / 4.0);
                yield step + (frac > 0.8 ? (int) ((frac - 0.8) * 20) : 0);           // terraces with short risers
            }
        };
    }

    // floor 5: the cavern
    private static int caveFloor(int x, int z) {
        return 24 + (int) Math.round(9 * n(5, x, z, 0.01, 3) + 4 * n(5, x + 70, z, 0.05, 2));
    }

    private static int caveRoof(int x, int z) {
        return 104 + (int) Math.round(9 * n(5, x + 999, z, 0.008, 3));
    }

    // floor 6: ash plains and volcanoes
    private static final int VOLCANO_CELL = 72;

    private static int volcanicHeight(int x, int z) {
        int h = 34 + (int) Math.round(10 * n(6, x, z, 0.008, 3) + 4 * n(6, x - 50, z, 0.04, 2));
        int ci = Math.floorDiv(x, VOLCANO_CELL), cj = Math.floorDiv(z, VOLCANO_CELL);
        for (int a = ci - 1; a <= ci + 1; a++)
            for (int b = cj - 1; b <= cj + 1; b++) {
                if (hash(6, a, b, 1) > 0.5) continue;
                double vx = a * VOLCANO_CELL + 20 + hash(6, a, b, 2) * 32, vz = b * VOLCANO_CELL + 20 + hash(6, a, b, 3) * 32;
                double r = 18 + hash(6, a, b, 4) * 10, d = Math.sqrt((x - vx) * (x - vx) + (z - vz) * (z - vz));
                int peak = 34 + (int) (hash(6, a, b, 5) * 16);
                if (d < r) {
                    double f = 1.0 - d / r;
                    int cone = (int) (peak * Math.pow(f, 1.3));
                    if (d < 4.5) cone = peak - 6;                                  // the crater
                    h = Math.max(h, 34 + cone);
                }
            }
        return h;
    }

    private static boolean volcanoCrater(int x, int z) {
        int ci = Math.floorDiv(x, VOLCANO_CELL), cj = Math.floorDiv(z, VOLCANO_CELL);
        for (int a = ci - 1; a <= ci + 1; a++)
            for (int b = cj - 1; b <= cj + 1; b++) {
                if (hash(6, a, b, 1) > 0.5) continue;
                double vx = a * VOLCANO_CELL + 20 + hash(6, a, b, 2) * 32, vz = b * VOLCANO_CELL + 20 + hash(6, a, b, 3) * 32;
                if ((x - vx) * (x - vx) + (z - vz) * (z - vz) < 4.5 * 4.5) return true;
            }
        return false;
    }

    private static boolean magmaRiver(int x, int z) {
        return Math.abs(n(6, x + 3000, z, 0.006, 2)) < 0.025;
    }

    // floor 7: dunes and mesas
    private static final int MESA_CELL = 64;

    private static int dunesBase(int x, int z) {
        double ridge = 1.0 - Math.abs(n(7, x * 0.8 + z * 0.4, z * 0.8 - x * 0.3, 0.02, 1));
        return 64 + (int) Math.round(7 * ridge * ridge + 4 * n(7, x, z, 0.006, 2));
    }

    private static int dunesHeight(int x, int z) {
        int base = dunesBase(x, z), best = base;
        int ci = Math.floorDiv(x, MESA_CELL), cj = Math.floorDiv(z, MESA_CELL);
        for (int a = ci - 1; a <= ci + 1; a++)
            for (int b = cj - 1; b <= cj + 1; b++) {
                if (hash(7, a, b, 1) > 0.45) continue;
                double mx = a * MESA_CELL + 16 + hash(7, a, b, 2) * 32, mz = b * MESA_CELL + 16 + hash(7, a, b, 3) * 32;
                double r = (12 + hash(7, a, b, 4) * 12) * (1.0 + 0.2 * n(7, x, z, 0.08, 2));
                double d = Math.sqrt((x - mx) * (x - mx) + (z - mz) * (z - mz));
                int top = 64 + 18 + (int) (hash(7, a, b, 5) * 14);
                if (d < r) best = Math.max(best, top - (d > r - 2 ? (int) ((d - (r - 2)) * 4) : 0));
            }
        return best;
    }

    // ── filling ──────────────────────────────────────────────────────────────

    static void fill(int floor, ChunkAccess chunk) {
        TowerFloorTerrain.Writer w = new TowerFloorTerrain.Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        if (floor == 5 || floor == 6) {
            fillCavern(floor, chunk, w, x0, z0, p);
            return;
        }
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int h = height(floor, x, z);
                boolean steep = Math.abs(height(floor, x + 1, z) - height(floor, x - 1, z)) + Math.abs(height(floor, x, z + 1) - height(floor, x, z - 1)) > 5;
                boolean mesa = floor == 7 && h > dunesBase(x, z) + 6;
                for (int y = -64; y <= h; y++) {
                    BlockState s;
                    if (y == -64) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y < 0) s = Blocks.DEEPSLATE.defaultBlockState();
                    else if (floor == 7) s = mesa && y < h - 1 ? arcaneBand(y) : (y > h - 5 ? (y == h && mesa ? Blocks.SAND.defaultBlockState() : Blocks.SAND.defaultBlockState()) : Blocks.SANDSTONE.defaultBlockState());
                    else if (floor == 8) s = y == h ? (steep ? Blocks.STONE.defaultBlockState() : (n(8, x, z, 0.05, 2) > 0.45 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState()))
                            : (y > h - 3 && !steep ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState());
                    else s = y == h ? (steep ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState())
                            : (y > h - 4 ? (steep ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.DIRT.defaultBlockState()) : Blocks.STONE.defaultBlockState());
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                groundSurface(floor, w, x, h + 1, z, steep);
            }
        }
        switch (floor) {
            case 7 -> {
                scatter(7, w, x0, z0, 22, 4, 0.42, 10, (wr, x, y, z, a, b) -> arcaneShard(wr, x, y, z, a));
                scatter(7, w, x0, z0, 9, 1, 0.12, 20, (wr, x, y, z, a, b) -> wr.put(x, y, z, Blocks.DEAD_BUSH.defaultBlockState()));
            }
            case 8 -> {
                scatter(8, w, x0, z0, 7, 4, 0.72, 10, (wr, x, y, z, a, b) -> { if (y < 125 && flat(8, x, z)) borealPine(wr, x, y, z, a); });
                scatter(8, w, x0, z0, 40, 4, 0.3, 20, (wr, x, y, z, a, b) -> iceSpire(wr, x, y, z, a));
            }
            default -> {
                // floor 9's cherry trees are Minecraft's own, grown in the decoration step (cherryTrees below);
                // the hand-shaped ones and the stone lanterns looked wrong (the user asked them out)
            }
        }
    }

    private static boolean flat(int floor, int x, int z) {
        return Math.abs(height(floor, x + 2, z) - height(floor, x - 2, z)) + Math.abs(height(floor, x, z + 2) - height(floor, x, z - 2)) <= 2;
    }

    private static BlockState arcaneBand(int y) {
        return switch (Math.floorMod(y / 3, 5)) {
            case 0 -> Blocks.SANDSTONE.defaultBlockState();
            case 1 -> Blocks.PURPLE_TERRACOTTA.defaultBlockState();
            case 2 -> Blocks.CUT_SANDSTONE.defaultBlockState();
            case 3 -> Blocks.MAGENTA_TERRACOTTA.defaultBlockState();
            default -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        };
    }

    private static void groundSurface(int floor, TowerFloorTerrain.Writer w, int x, int y, int z, boolean steep) {
        if (steep) return;
        double r = hash(floor, x, z, 77);
        switch (floor) {
            case 8 -> { if (w.get(x, y - 1, z).is(Blocks.SNOW_BLOCK) && r < 0.6) w.put(x, y, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + (int) (r * 3))); }
            case 9 -> {
                if (r < 0.22) w.put(x, y, z, Blocks.PINK_PETALS.defaultBlockState().setValue(PinkPetalsBlock.AMOUNT, 1 + (int) (r * 18) % 4)
                        .setValue(PinkPetalsBlock.FACING, Direction.from2DDataValue((int) (r * 97))));
                else if (r < 0.34) w.put(x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                else if (r < 0.35) w.put(x, y, z, Blocks.LILY_OF_THE_VALLEY.defaultBlockState());
            }
            default -> {}
        }
    }

    // ── caverns (floors 5 and 6) ─────────────────────────────────────────────

    private static void fillCavern(int floor, ChunkAccess chunk, TowerFloorTerrain.Writer w, int x0, int z0, BlockPos.MutableBlockPos p) {
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int fl = height(floor, x, z);
                int roof = floor == 5 ? caveRoof(x, z) : 116 + (int) Math.round(5 * n(6, x + 900, z, 0.01, 2));
                boolean river = floor == 6 && magmaRiver(x, z) && !volcanoCrater(x, z);
                boolean crater = floor == 6 && volcanoCrater(x, z);
                for (int y = 0; y <= 127; y++) {
                    BlockState s;
                    if (y <= 1 || y >= 126) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y <= fl) s = floor == 5 ? caveGround(x, y, z, fl) : ashGround(x, y, z, fl, river);
                    else if (y >= roof) s = floor == 5 ? (hash(5, x, z, y) < 0.012 ? Blocks.VERDANT_FROGLIGHT.defaultBlockState() : (y < roof + 3 ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState()))
                            : (hash(6, x, z, y) < 0.01 ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
                    else s = Blocks.AIR.defaultBlockState();
                    if (river && y == fl) s = Blocks.LAVA.defaultBlockState();
                    if (crater && y > fl - 3 && y <= fl) s = Blocks.LAVA.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
            }
        }
        if (floor == 5) {
            scatter(5, w, x0, z0, 46, 10, 0.42, 10, TowerFloorTerrainDeep::caveColumn);
            scatter(5, w, x0, z0, 9, 3, 0.45, 20, (wr, x, y, z, a, b) -> dripCone(wr, x, caveRoof(x, z) - 1, z, a, true));
            scatter(5, w, x0, z0, 11, 3, 0.35, 30, (wr, x, y, z, a, b) -> dripCone(wr, x, y, z, a, false));
            scatter(5, w, x0, z0, 24, 4, 0.5, 40, (wr, x, y, z, a, b) -> crystalSpire(wr, x, y, z, a));
            scatter(5, w, x0, z0, 4, 0, 0.32, 50, (wr, x, y, z, a, b) -> manaCrystal(wr, x, y, z, a, Direction.UP));
            scatter(5, w, x0, z0, 5, 0, 0.25, 60, (wr, x, y, z, a, b) -> manaCrystal(wr, x, caveRoof(x, z) - 1, z, a, Direction.DOWN));
            scatter(5, w, x0, z0, 3, 0, 0.18, 70, (wr, x, y, z, a, b) -> { if (wr.get(x, y - 1, z).is(Blocks.MOSS_BLOCK)) wr.put(x, y, z, Blocks.MOSS_CARPET.defaultBlockState()); });
        } else {
            scatter(6, w, x0, z0, 10, 1, 0.3, 20, (wr, x, y, z, a, b) -> basaltColumn(wr, x, y, z, a));
            scatter(6, w, x0, z0, 18, 2, 0.3, 30, (wr, x, y, z, a, b) -> obsidianSpike(wr, x, y, z, a));
        }
    }

    private static BlockState caveGround(int x, int y, int z, int fl) {
        if (y < fl - 3) return y < 10 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
        double v = n(5, x, z, 0.05, 2);
        if (n(5, x - 400, z + 50, 0.03, 1) > 0.78) return Blocks.CALCITE.defaultBlockState();            // only veins of it
        if (y == fl) return v > 0.4 ? Blocks.MOSS_BLOCK.defaultBlockState() : (v < -0.45 ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState());
        return v > 0.2 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    private static BlockState ashGround(int x, int y, int z, int fl, boolean river) {
        if (y < fl - 3) return Blocks.BLACKSTONE.defaultBlockState();
        if (river && y == fl - 1) return Blocks.MAGMA_BLOCK.defaultBlockState();
        double v = n(6, x, z, 0.06, 2);
        if (y == fl) return v > 0.5 ? Blocks.MAGMA_BLOCK.defaultBlockState() : (v > 0.1 ? Blocks.BASALT.defaultBlockState()
                : (v < -0.4 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState()));
        return Blocks.BASALT.defaultBlockState();
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

    /** A colossal pillar joining the cavern floor and roof, waisted in the middle. */
    private static void caveColumn(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        double rad = 5 + r1 * 4;
        int top = caveRoof(x, z), bottom = y - 2;
        int ri = (int) Math.ceil(rad * 1.3);
        for (int yy = bottom; yy <= top; yy++) {
            double t = (yy - bottom) / (double) Math.max(1, top - bottom);
            double rr = rad * (0.7 + 0.6 * Math.abs(t - 0.5));
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz) + hash(5, x + dx, z + dz, yy / 4) * 1.1;
                    if (d > rr) continue;
                    w.force(x + dx, yy, z + dz, hash(5, x + dx, yy, z + dz) < 0.12 ? Blocks.DRIPSTONE_BLOCK.defaultBlockState()
                            : (yy % 9 == 0 ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState()));
                }
        }
    }

    /** A giant stalactite (from the roof) or stalagmite (from the floor) of dripstone. */
    private static void dripCone(TowerFloorTerrain.Writer w, int x, int y, int z, double r, boolean down) {
        int len = 5 + (int) (r * 15);
        double rad = 1.2 + r * 2.2;
        int step = down ? -1 : 1;
        for (int k = 0; k < len; k++) {
            double rr = rad * (1.0 - (double) k / len);
            int ri = (int) Math.ceil(rr);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rr * rr) w.put(x + dx, y + k * step, z + dz, Blocks.DRIPSTONE_BLOCK.defaultBlockState());
        }
        Direction tip = down ? Direction.DOWN : Direction.UP;
        w.put(x, y + len * step, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, tip)
                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.FRUSTUM));
        w.put(x, y + (len + 1) * step, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, tip)
                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP));
    }

    /** A spire of crystal on a stone foot, crowned and studded with mana crystals. */
    private static void crystalSpire(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 6 + (int) (r * 12);
        for (int k = 0; k < h; k++) {
            double rr = 2.2 * (1.0 - (double) k / h) + 0.5;
            int ri = (int) Math.ceil(rr);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rr * rr)
                        w.put(x + dx, y + k, z + dz, k < 2 && dx * dx + dz * dz > 1 ? Blocks.STONE.defaultBlockState()
                                : (hash(5, x + dx, z + dz, k) < 0.15 ? Blocks.BUDDING_AMETHYST.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState()));
        }
        manaCrystal(w, x, y + h, z, r, Direction.UP);
        for (Direction d : Direction.Plane.HORIZONTAL) manaCrystal(w, x + d.getStepX() * 2, y + 2 + (int) (r * 3), z + d.getStepZ() * 2, r, d);
    }

    /** One of the mod's mana crystal clusters, rarer the stronger. */
    private static void manaCrystal(TowerFloorTerrain.Writer w, int x, int y, int z, double r, Direction facing) {
        BlockState c = TowerFloorTerrain.manaCrystal(5, r);   // only the crystal cave of floor 5 has them
        if (c.hasProperty(TeyvatCrystalBlock.FACING)) c = c.setValue(TeyvatCrystalBlock.FACING, facing);
        w.put(x, y, z, c);
    }

    private static void basaltColumn(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 3 + (int) (r * 10);
        for (int k = 0; k < h; k++) {
            w.put(x, y + k, z, Blocks.BASALT.defaultBlockState());
            if (r > 0.6 && k < h - 2) w.put(x + 1, y + k, z, Blocks.BASALT.defaultBlockState());
        }
        if (r > 0.8) w.put(x, y + h, z, Blocks.MAGMA_BLOCK.defaultBlockState());
    }

    private static void obsidianSpike(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 4 + (int) (r * 8);
        for (int k = 0; k < h; k++) {
            w.put(x, y + k, z, k == h - 1 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
            if (k < h / 3) for (Direction d : Direction.Plane.HORIZONTAL) w.put(x + d.getStepX(), y + k, z + d.getStepZ(), Blocks.OBSIDIAN.defaultBlockState());
        }
    }

    /** A leaning shard of arcane crystal rising from the sand. */
    private static void arcaneShard(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 5 + (int) (r * 9);
        int lean = r > 0.5 ? 1 : -1;
        for (int k = 0; k < h; k++) {
            int ox = (k * lean) / 4;
            w.put(x + ox, y + k, z, k == h - 1 ? Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP)
                    : (k % 3 == 1 ? Blocks.PURPLE_STAINED_GLASS.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState()));
            if (k < h / 2) w.put(x + ox + 1, y + k, z, Blocks.AMETHYST_BLOCK.defaultBlockState());
        }
        w.put(x - 1, y, z, Blocks.CALCITE.defaultBlockState());
    }

    /** A narrow boreal pine with snow resting on every skirt of needles. */
    private static void borealPine(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 7 + (int) (r * 7);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, Blocks.SPRUCE_LOG.defaultBlockState());
        BlockState leaf = TowerFloorTerrain.leaves(Blocks.SPRUCE_LEAVES);
        for (int i = 2; i <= h + 1; i++) {
            double rad = (i % 3 == 0 ? 2.8 : 1.6) * (1.0 - (double) i / (h + 3));
            int ri = (int) Math.ceil(rad);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rad * rad + 0.3 && (dx != 0 || dz != 0 || i > h - 1)) {
                        w.put(x + dx, y + i, z + dz, leaf);
                        if (i % 3 == 0) w.put(x + dx, y + i + 1, z + dz, Blocks.SNOW.defaultBlockState());
                    }
        }
        w.put(x, y + h + 1, z, leaf);
        w.put(x, y + h + 2, z, Blocks.SNOW.defaultBlockState());
    }

    private static void iceSpire(TowerFloorTerrain.Writer w, int x, int y, int z, double r) {
        int h = 8 + (int) (r * 14);
        for (int k = 0; k < h; k++) {
            double rr = 2.0 * (1.0 - (double) k / h) + 0.4;
            int ri = (int) Math.ceil(rr);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rr * rr) w.put(x + dx, y + k, z + dz, k > h * 0.7 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState());
        }
    }

    /** A cherry tree: a leaning trunk forking into two or three boughs under wide pink crowns. */
    private static void cherryTree(TowerFloorTerrain.Writer w, int x, int y, int z, double r1, double r2) {
        BlockState log = Blocks.CHERRY_LOG.defaultBlockState();
        BlockState leaf = TowerFloorTerrain.leaves(Blocks.CHERRY_LEAVES);
        int h = 4 + (int) (r1 * 3);
        for (int i = 0; i < h; i++) w.trunk(x, y + i, z, log);
        int boughs = 2 + (int) (r2 * 2);
        for (int i = 0; i < boughs; i++) {
            double a = r2 * 6.28 + i * 6.28 / boughs;
            int ex = x, ez = z, ey = y + h;
            for (int k = 1; k <= 3; k++) {
                ex = x + (int) Math.round(Math.cos(a) * k);
                ez = z + (int) Math.round(Math.sin(a) * k);
                ey = y + h - 1 + k;
                w.trunk(ex, ey, ez, log);
            }
            TowerFloorTerrain.blob(w, ex, ey + 1, ez, 3.0 + r1, leaf, false);
        }
        TowerFloorTerrain.blob(w, x, y + h + 2, z, 3.2, leaf, false);
    }

    /**
     * Floor 9's cherry trees, grown by Minecraft's own cherry tree feature as in its cherry groves, scattered
     * over the flat grass of this chunk. Runs in the decoration step, when the neighbouring chunks can be written.
     */
    static void cherryTrees(net.minecraft.world.level.WorldGenLevel level, net.minecraft.world.level.chunk.ChunkGenerator generator, ChunkAccess chunk) {
        var features = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE);
        var tree = features.getHolder(net.minecraft.data.worldgen.features.TreeFeatures.CHERRY);
        if (tree.isEmpty()) return;
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        int cell = 9;
        for (int a = Math.floorDiv(x0, cell); a <= Math.floorDiv(x0 + 15, cell); a++)
            for (int b = Math.floorDiv(z0, cell); b <= Math.floorDiv(z0 + 15, cell); b++) {
                if (TowerFloorTerrain.hash(9, a, b, 10) > 0.5) continue;
                int x = a * cell + (int) (TowerFloorTerrain.hash(9, a, b, 11) * cell), z = b * cell + (int) (TowerFloorTerrain.hash(9, a, b, 12) * cell);
                if (x < x0 || x > x0 + 15 || z < z0 || z > z0 + 15 || !flat(9, x, z)) continue;
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, height(9, x, z) + 1, z);
                if (!level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) continue;
                tree.get().value().place(level, generator, net.minecraft.util.RandomSource.create(((long) a << 32) ^ b ^ 9L), pos);
            }
    }

    /** A small stone lantern: base, post, lit cap. */
    private static void stoneLantern(TowerFloorTerrain.Writer w, int x, int y, int z) {
        w.put(x, y, z, Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        w.put(x, y + 1, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
        w.put(x, y + 2, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        w.put(x, y + 3, z, Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
    }
}
