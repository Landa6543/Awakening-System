package com.soybe.awakeningsystem.tower.terrain;

import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TeyvatCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.chunk.ChunkAccess;

import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.hash;
import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.n;

/**
 * Floor 10, the Monumental Vault of the Colossal Sculk Titan (boss floor): a vast cave to explore, its
 * rolling floor patched with sculk, moss and soul soil, colossal brick columns holding up the roof, giant
 * stalactites and stalagmites and mana crystals on floor and roof. The player arrives in one corner and the
 * way up is in the opposite one; in the middle rises the titan's arena, a round platform reached by four
 * ramps, with a low parapet and soul lanterns, under giant bone ribs arching across it.
 */
final class TowerFloorTerrainVault {
    private TowerFloorTerrainVault() {}

    static final int DECK = 30;                       // the arena's floor
    private static final int ARENA_R = 30, RAMP_HALF = 2;

    static boolean onArena(int x, int z) {
        return x * x + z * z <= ARENA_R * ARENA_R;
    }

    private static int caveGround(int x, int z) {
        return 14 + (int) Math.round(7 * n(10, x, z, 0.01, 3) + 3 * n(10, x + 40, z, 0.06, 1));
    }

    /** Height of a ramp running out from the arena along an axis, or -1 off the ramps. */
    private static int ramp(int x, int z) {
        int along, across;
        if (Math.abs(z) <= RAMP_HALF && Math.abs(x) > ARENA_R - 1) { along = Math.abs(x); across = z; }
        else if (Math.abs(x) <= RAMP_HALF && Math.abs(z) > ARENA_R - 1) { along = Math.abs(z); across = x; }
        else return -1;
        return DECK - (along - ARENA_R + 1) / 2;
    }

    static int height(int x, int z) {
        if (onArena(x, z)) return DECK;
        int g = caveGround(x, z);
        int r = ramp(x, z);
        return Math.max(g, r);
    }

    private static int roof(int x, int z) {
        return 104 + (int) Math.round(8 * n(10, x + 900, z, 0.008, 2));
    }

    static void fill(ChunkAccess chunk) {
        TowerFloorTerrain.Writer w = new TowerFloorTerrain.Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int ground = caveGround(x, z);
                int top = roof(x, z);
                for (int y = 0; y <= 127; y++) {
                    BlockState s;
                    if (y <= 1 || y >= 126) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y <= ground) s = y == ground ? groundTop(x, z) : (y > ground - 3 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                    else if (y >= top) s = hash(10, x, z, y) < 0.008 ? Blocks.SCULK_CATALYST.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
                    else s = Blocks.AIR.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                if (onArena(x, z)) arenaColumn(w, x, z, ground);
                else {
                    int r = ramp(x, z);
                    if (r > ground) rampColumn(w, x, z, ground, r);
                }
            }
        }
        // colossal columns, kept clear of the arena and its ramps
        scatter(w, x0, z0, 36, 8, 0.55, 10, (wr, x, z, r) -> {
            if (x * x + z * z < 44 * 44 || Math.abs(x) < 9 || Math.abs(z) < 9) return;
            column(wr, x, z, r);
        });
        scatter(w, x0, z0, 10, 3, 0.45, 20, (wr, x, z, r) -> { if (!near(x, z)) drip(wr, x, roof(x, z) - 1, z, r, true); });
        scatter(w, x0, z0, 12, 3, 0.35, 30, (wr, x, z, r) -> { if (!near(x, z)) drip(wr, x, height(x, z) + 1, z, r, false); });
        scatter(w, x0, z0, 4, 0, 0.3, 40, (wr, x, z, r) -> { if (!onArena(x, z)) crystal(wr, x, height(x, z) + 1, z, r, Direction.UP); });
        scatter(w, x0, z0, 5, 0, 0.22, 50, (wr, x, z, r) -> crystal(wr, x, roof(x, z) - 1, z, r, Direction.DOWN));
        ribs(w);
    }

    /** Keeps big rock out of the arena, its ramps and the corners where the portals stand. */
    private static boolean near(int x, int z) {
        return x * x + z * z < 40 * 40 || ramp(x, z) >= 0
                || (x + 250) * (x + 250) + (z + 250) * (z + 250) < 14 * 14 || (x - 250) * (x - 250) + (z - 250) * (z - 250) < 14 * 14;
    }

    private static BlockState groundTop(int x, int z) {
        double v = n(10, x, z, 0.05, 2);
        if (v > 0.3) return Blocks.SCULK.defaultBlockState();
        if (v < -0.55) return Blocks.SOUL_SOIL.defaultBlockState();
        if (v < -0.3) return Blocks.MOSS_BLOCK.defaultBlockState();
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    /** The arena: solid down to the ground, rings in its floor, the titan's seal, a low parapet and lanterns. */
    private static void arenaColumn(TowerFloorTerrain.Writer w, int x, int z, int ground) {
        double d = Math.sqrt(x * x + z * z);
        for (int y = ground + 1; y < DECK; y++) w.force(x, y, z, d > ARENA_R - 1.5 ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        BlockState f;
        if (d < 6) f = Blocks.SCULK_CATALYST.defaultBlockState();
        else if (d < 7.2) f = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
        else if (Math.abs(d - 14) < 0.6 || Math.abs(d - 22) < 0.6) f = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        else if (d > ARENA_R - 1.5) f = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
        else f = ((int) d) % 2 == 0 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
        w.force(x, DECK, z, f);
        // a solid parapet (no wall posts), open where the ramps arrive
        boolean rampGap = Math.abs(x) <= RAMP_HALF + 1 || Math.abs(z) <= RAMP_HALF + 1;
        if (d > ARENA_R - 1.5 && !rampGap) {
            double a = Math.toDegrees(Math.atan2(z, x)) + 360;
            boolean post = ((int) a) % 30 < 4;
            w.force(x, DECK + 1, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            if (post) {
                w.force(x, DECK + 2, z, Blocks.CHISELED_DEEPSLATE.defaultBlockState());
                w.force(x, DECK + 3, z, Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }
    }

    /** A ramp of deepslate bricks sloping from the arena down to the cave floor. */
    private static void rampColumn(TowerFloorTerrain.Writer w, int x, int z, int ground, int top) {
        boolean edge = (Math.abs(z) <= RAMP_HALF ? Math.abs(z) : Math.abs(x)) == RAMP_HALF;
        for (int y = ground + 1; y <= top; y++)
            w.force(x, y, z, y == top ? (edge ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState()) : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
    }

    private interface Feature { void place(TowerFloorTerrain.Writer w, int x, int z, double r); }

    private static void scatter(TowerFloorTerrain.Writer w, int x0, int z0, int cell, int margin, double chance, int salt, Feature f) {
        int c0 = Math.floorDiv(x0 - margin, cell), c1 = Math.floorDiv(x0 + 15 + margin, cell);
        int d0 = Math.floorDiv(z0 - margin, cell), d1 = Math.floorDiv(z0 + 15 + margin, cell);
        for (int a = c0; a <= c1; a++)
            for (int b = d0; b <= d1; b++) {
                if (hash(10, a, b, salt) > chance) continue;
                int span = Math.max(1, cell - 2);
                f.place(w, a * cell + 1 + (int) (hash(10, a, b, salt + 1) * span), b * cell + 1 + (int) (hash(10, a, b, salt + 2) * span), hash(10, a, b, salt + 3));
            }
    }

    /** A colossal column of deepslate bricks from floor to roof, banded, with sculk creeping up its foot. */
    private static void column(TowerFloorTerrain.Writer w, int x, int z, double r) {
        if (near(x, z)) return;
        int half = 2 + (r > 0.5 ? 1 : 0);
        int bottom = caveGround(x, z) - 1, top = roof(x, z) + 1;
        for (int y = bottom; y <= top; y++) {
            for (int dx = -half - 1; dx <= half + 1; dx++)
                for (int dz = -half - 1; dz <= half + 1; dz++) {
                    boolean core = Math.abs(dx) <= half && Math.abs(dz) <= half;
                    boolean flare = (y < bottom + 3 || y > top - 3) && !(Math.abs(dx) == half + 1 && Math.abs(dz) == half + 1);
                    if (!core && !flare) continue;
                    BlockState s = y % 9 == 0 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState()
                            : (y < bottom + 5 && hash(10, x + dx, z + dz, y) < 0.45 ? Blocks.SCULK.defaultBlockState()
                            : (hash(10, x + dx, y, z + dz) < 0.15 ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState()));
                    w.force(x + dx, y, z + dz, s);
                }
        }
        w.put(x + half + 1, bottom + 7, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    /** A giant stalactite (from the roof) or stalagmite (from the floor) of deepslate and dripstone. */
    private static void drip(TowerFloorTerrain.Writer w, int x, int y, int z, double r, boolean down) {
        int len = 5 + (int) (r * 14);
        double rad = 1.0 + r * 2.0;
        int step = down ? -1 : 1;
        for (int k = 0; k < len; k++) {
            double rr = rad * (1.0 - (double) k / len);
            int ri = (int) Math.ceil(rr);
            for (int dx = -ri; dx <= ri; dx++)
                for (int dz = -ri; dz <= ri; dz++)
                    if (dx * dx + dz * dz <= rr * rr)
                        w.put(x + dx, y + k * step, z + dz, k < len / 3 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.DRIPSTONE_BLOCK.defaultBlockState());
        }
        Direction tip = down ? Direction.DOWN : Direction.UP;
        w.put(x, y + len * step, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, tip)
                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP));
    }

    /** Mana crystals of the vault: rank C to A. */
    private static void crystal(TowerFloorTerrain.Writer w, int x, int y, int z, double r, Direction facing) {
        BlockState c;
        if (r < 0.55) c = SystemBlocks.MANA_CRYSTAL_CLUSTER_C.get().defaultBlockState();
        else if (r < 0.88) c = SystemBlocks.MANA_CRYSTAL_CLUSTER_B.get().defaultBlockState();
        else c = SystemBlocks.MANA_CRYSTAL_CLUSTER_A.get().defaultBlockState();
        if (c.hasProperty(TeyvatCrystalBlock.FACING)) c = c.setValue(TeyvatCrystalBlock.FACING, facing);
        w.put(x, y, z, c);
    }

    /** Three giant bone ribs arching over the arena from rim to rim, joined by a spine: the titan's ribcage. */
    private static void ribs(TowerFloorTerrain.Writer w) {
        for (int k = -1; k <= 1; k++) {
            int zOff = k * 11 + 5;                 // off the ramp running along z = 0
            double span = Math.sqrt(ARENA_R * ARENA_R - zOff * zOff) - 2;
            for (int i = 0; i <= 180; i++) {
                double t = Math.toRadians(i);
                int x = (int) Math.round(Math.cos(t) * span);
                int y = DECK + 1 + (int) Math.round(Math.sin(t) * 34);
                for (int tz = 0; tz <= 1; tz++)
                    for (int ty = 0; ty <= 1; ty++)
                        w.force(x, y + ty, zOff + tz, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
        }
        for (int z = -6; z <= 17; z++) w.force(0, DECK + 37, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
    }
}
