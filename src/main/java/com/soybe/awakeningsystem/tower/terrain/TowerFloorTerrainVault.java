package com.soybe.awakeningsystem.tower.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.hash;
import static com.soybe.awakeningsystem.tower.terrain.TowerFloorTerrain.n;

/**
 * Floor 10, the Monumental Vault of the Colossal Sculk Titan (boss floor): a vast dark vault with sculk
 * creeping over its deepslate floor and colossal brick columns holding up the roof. In the middle stands
 * the titan's arena, a raised round platform ringed by a low wall and soul lanterns, under giant bone ribs
 * arching across it; two raised causeways on piers lead from it over the chasm to the entrance (x -140)
 * and the way up (x +140).
 */
final class TowerFloorTerrainVault {
    private TowerFloorTerrainVault() {}

    static final int DECK = 30;                       // the arena and causeways' walking level
    private static final int ARENA_R = 30, WAY_HALF = 3, WAY_END = 150, END_R = 11;

    static boolean onArena(int x, int z) {
        return x * x + z * z <= ARENA_R * ARENA_R;
    }

    static boolean onWay(int x, int z) {
        boolean causeway = Math.abs(z) <= WAY_HALF && Math.abs(x) <= WAY_END;
        boolean landing = (x + 140) * (x + 140) + z * z <= END_R * END_R || (x - 140) * (x - 140) + z * z <= END_R * END_R;
        return causeway || landing;
    }

    static int height(int x, int z) {
        if (onArena(x, z) || onWay(x, z)) return DECK;
        return 18 + (int) Math.round(6 * n(10, x, z, 0.01, 3) + 2 * n(10, x + 40, z, 0.06, 1));
    }

    private static int roof(int x, int z) {
        return 112 + (int) Math.round(6 * n(10, x + 900, z, 0.008, 2));
    }

    static void fill(ChunkAccess chunk) {
        TowerFloorTerrain.Writer w = new TowerFloorTerrain.Writer(chunk);
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int ground = 18 + (int) Math.round(6 * n(10, x, z, 0.01, 3) + 2 * n(10, x + 40, z, 0.06, 1));
                int top = roof(x, z);
                for (int y = 0; y <= 127; y++) {
                    BlockState s;
                    if (y <= 1 || y >= 126) s = Blocks.BEDROCK.defaultBlockState();
                    else if (y <= ground) s = y == ground ? groundTop(x, z) : (y > ground - 3 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                    else if (y >= top) s = hash(10, x, z, y) < 0.006 ? Blocks.SCULK_CATALYST.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
                    else s = Blocks.AIR.defaultBlockState();
                    chunk.setBlockState(p.set(x, y, z), s, false);
                }
                if (onArena(x, z)) arenaColumn(w, x, z, ground);
                else if (onWay(x, z)) wayColumn(w, x, z, ground);
            }
        }
        // colossal columns holding up the vault, kept clear of the arena and the causeways
        scatter(w, x0, z0, 34, 8, 0.6, 10, (wr, x, z, r) -> {
            if (x * x + z * z < 46 * 46 || Math.abs(z) < 12) return;
            column(wr, x, z, r);
        });
        ribs(w);
    }

    private static BlockState groundTop(int x, int z) {
        double v = n(10, x, z, 0.05, 2);
        if (v > 0.25) return Blocks.SCULK.defaultBlockState();
        if (v < -0.5) return Blocks.SOUL_SOIL.defaultBlockState();
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    /** The arena: solid down to the ground, rings in its floor, the titan's seal, a low wall and lanterns. */
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
        if (d > ARENA_R - 1.5 && Math.abs(z) > WAY_HALF + 1) {
            double a = Math.toDegrees(Math.atan2(z, x)) + 360;
            boolean post = ((int) a) % 30 < 4;
            w.force(x, DECK + 1, z, post ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState());
            if (post) {
                w.force(x, DECK + 2, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                w.force(x, DECK + 3, z, Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }
    }

    /** A causeway: a deck on piers every twelve blocks, with railings and lanterns. */
    private static void wayColumn(TowerFloorTerrain.Writer w, int x, int z, int ground) {
        boolean landing = Math.abs(z) > WAY_HALF || Math.abs(Math.abs(x) - 140) <= END_R;
        boolean pier = landing || Math.floorMod(x, 12) <= 1;
        for (int y = ground + 1; y < DECK - 1; y++) if (pier) w.force(x, y, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        w.force(x, DECK - 1, z, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        w.force(x, DECK, z, Math.abs(z) == WAY_HALF && !landing ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState());
        if (!landing && Math.abs(z) == WAY_HALF) {
            boolean post = Math.floorMod(x, 12) == 0;
            w.force(x, DECK + 1, z, post ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState());
            if (post) w.force(x, DECK + 2, z, Blocks.SOUL_LANTERN.defaultBlockState());
        }
    }

    private interface Feature { void place(TowerFloorTerrain.Writer w, int x, int z, double r); }

    private static void scatter(TowerFloorTerrain.Writer w, int x0, int z0, int cell, int margin, double chance, int salt, Feature f) {
        int c0 = Math.floorDiv(x0 - margin, cell), c1 = Math.floorDiv(x0 + 15 + margin, cell);
        int d0 = Math.floorDiv(z0 - margin, cell), d1 = Math.floorDiv(z0 + 15 + margin, cell);
        for (int a = c0; a <= c1; a++)
            for (int b = d0; b <= d1; b++) {
                if (hash(10, a, b, salt) > chance) continue;
                f.place(w, a * cell + 6 + (int) (hash(10, a, b, salt + 1) * (cell - 12)), b * cell + 6 + (int) (hash(10, a, b, salt + 2) * (cell - 12)), hash(10, a, b, salt + 3));
            }
    }

    /** A colossal column of deepslate bricks from floor to roof, banded, with sculk creeping up its foot. */
    private static void column(TowerFloorTerrain.Writer w, int x, int z, double r) {
        int half = 2 + (r > 0.5 ? 1 : 0);
        int bottom = height(x, z) - 1, top = roof(x, z) + 1;
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
        w.put(x + half + 1, bottom + 6, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    /** Three giant bone ribs arching over the arena from rim to rim: the titan's ribcage. */
    private static void ribs(TowerFloorTerrain.Writer w) {
        for (int k = -1; k <= 1; k++) {
            int zOff = k * 11;
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
        // the spine running along the top of the ribs
        for (int z = -11; z <= 12; z++) w.force(0, DECK + 37, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
    }
}
