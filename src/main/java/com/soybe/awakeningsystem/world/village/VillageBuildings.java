package com.soybe.awakeningsystem.world.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

import java.util.ArrayList;
import java.util.List;

import static com.soybe.awakeningsystem.world.village.Plot.facing;
import static net.minecraft.core.Direction.*;

/**
 * The buildings of the medieval village, each with its inside furnished: houses, a two-storey house, the
 * tavern, the smithy, the chapel with its bell tower, the library, the barn and the watchtower. Every method
 * builds on a {@link Plot} in local coordinates (front at z = 0 facing north, floor at y = 0) and returns where
 * its villagers should stand and what they work as.
 */
final class VillageBuildings {
    private VillageBuildings() {}

    /** A villager to put in a building: local spot and profession id ("" for none, "golem", "cat"). */
    record Resident(BlockPos pos, String job) {}

    // ── materials ────────────────────────────────────────────────────────────

    /** How a building's walls are made: a timber frame filled in, solid masonry with corner stones, or stacked logs. */
    enum Walls { FRAMED, SOLID, LOGS }

    /**
     * A building's materials: roof, floor, frame (corner posts), beam (horizontal timbers or bands), the wall
     * filling, the footing at the bottom of the walls and how many rows of it.
     */
    record Style(Block roofStairs, Block roofSlab, Block floor, Block frame, Block beam, Block plaster, Block footing, int footRows, Walls kind) {}

    // the house palettes: different wood, wall, base and roof, so no two neighbours look alike
    // (no white terracotta, mud brick, packed mud or calcite: they looked wrong; no half-stone, half-wood walls)
    static final Style SPRUCE = new Style(Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.OAK_PLANKS, Blocks.COBBLESTONE, 1, Walls.FRAMED);
    static final Style DARK = new Style(Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.OAK_PLANKS,
            Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.SPRUCE_PLANKS, Blocks.COBBLESTONE, 1, Walls.FRAMED);
    static final Style BRICK = new Style(Blocks.BRICK_STAIRS, Blocks.BRICK_SLAB, Blocks.SPRUCE_PLANKS,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.SPRUCE_PLANKS, Blocks.COBBLESTONE, 1, Walls.FRAMED);
    static final Style SLATE = new Style(Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.SPRUCE_PLANKS,
            Blocks.SPRUCE_LOG, Blocks.SPRUCE_LOG, Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, 1, Walls.SOLID);
    static final Style BIRCH = new Style(Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.SPRUCE_PLANKS,
            Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.BIRCH_PLANKS, Blocks.COBBLESTONE, 1, Walls.FRAMED);
    static final Style OAK_CABIN = new Style(Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.OAK_PLANKS,
            Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LOG, Blocks.COBBLESTONE, 1, Walls.LOGS);
    // kept for the old names used by the landmarks
    static final Style MUD = BRICK;
    static final Style COTTAGE = new Style(Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS,
            Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, 1, Walls.SOLID);
    static final Style TOWNHOUSE = new Style(Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DARK_OAK_PLANKS,
            Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.BRICKS, Blocks.STONE_BRICKS, 1, Walls.SOLID);
    static final Style CABIN = new Style(Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.SPRUCE_PLANKS,
            Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_LOG, Blocks.COBBLESTONE, 1, Walls.LOGS);
    static final Style STONE_HALF = new Style(Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DARK_OAK_PLANKS,
            Blocks.DARK_OAK_LOG, Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, 1, Walls.SOLID);

    static final Style[] HOUSE_STYLES = {SPRUCE, DARK, BRICK, SLATE, BIRCH, OAK_CABIN, COTTAGE, TOWNHOUSE, CABIN};

    static BlockState stairs(Block b, Direction f) {
        return b.defaultBlockState().setValue(StairBlock.FACING, f);
    }

    static BlockState stairsTop(Block b, Direction f) {
        return stairs(b, f).setValue(StairBlock.HALF, Half.TOP);
    }

    static BlockState slab(Block b, SlabType t) {
        return b.defaultBlockState().setValue(SlabBlock.TYPE, t);
    }

    static BlockState log(Block b, Direction.Axis a) {
        BlockState s = b.defaultBlockState();
        return s.hasProperty(RotatedPillarBlock.AXIS) ? s.setValue(RotatedPillarBlock.AXIS, a) : s;
    }

    static BlockState lantern(boolean hanging) {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging);
    }

    static BlockState pane(Block b, Direction.Axis along) {
        BlockState s = b.defaultBlockState();
        return along == Direction.Axis.X ? s.setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true)
                : s.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true);
    }

    static BlockState fence(Block b, boolean n, boolean e, boolean s, boolean w) {
        return b.defaultBlockState().setValue(BlockStateProperties.NORTH, n).setValue(BlockStateProperties.EAST, e)
                .setValue(BlockStateProperties.SOUTH, s).setValue(BlockStateProperties.WEST, w);
    }

    static BlockState candles(int n) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, n).setValue(CandleBlock.LIT, true);
    }

    static Block stoneMix(Plot p, int x, int y, int z) {
        int n = p.noise(x, y, z) % 10;
        return n < 6 ? Blocks.STONE_BRICKS : n < 8 ? Blocks.MOSSY_STONE_BRICKS : n < 9 ? Blocks.CRACKED_STONE_BRICKS : Blocks.COBBLESTONE;
    }

    static Block cobbleMix(Plot p, int x, int y, int z) {
        int n = p.noise(x, y, z) % 10;
        return n < 6 ? Blocks.COBBLESTONE : n < 8 ? Blocks.MOSSY_COBBLESTONE : Blocks.STONE_BRICKS;
    }

    // ── shared pieces ────────────────────────────────────────────────────────

    /** Ground under the building filled down to the earth, the site cleared, a stone floor rim and a plank floor. */
    static void site(Plot p, int height, Block floor) {
        site(p, height, floor, true);
    }

    /** {@code ring}: raise the ground round the building to its floor (not for stalls and the well on the square). */
    static void site(Plot p, int height, Block floor, boolean ring) {
        for (int x = -2; x <= p.w + 1; x++)
            for (int z = -2; z <= p.d + 1; z++) {
                BlockPos col = p.pos(x, 0, z);
                boolean inside = x >= 0 && x < p.w && z >= 0 && z < p.d;
                // cut the hillside away above the floor
                for (int y = 1; y <= height + 6; y++) {
                    BlockPos q = p.pos(x, y, z);
                    BlockState s = p.canvas.get(q);
                    if (inside ? !s.isAir() : VillageCanvas.clearable(s)) p.canvas.set(q, Blocks.AIR.defaultBlockState());
                }
                if (!inside && !ring) continue;
                if (!inside) {
                    // a ring of grass at floor level so the house does not float above or sink in a ditch
                    int g = p.canvas.ground(col.getX(), col.getZ());
                    if (g < p.floorY) for (int y = g + 1; y <= p.floorY; y++)
                        p.canvas.set(new BlockPos(col.getX(), y, col.getZ()), (y == p.floorY ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState());
                    continue;
                }
                int g = p.canvas.ground(col.getX(), col.getZ());
                for (int y = Math.min(g, p.floorY - 1); y < p.floorY; y++) p.set(x, y - p.floorY, z, stoneMix(p, x, y, z));
                boolean rim = x == 0 || z == 0 || x == p.w - 1 || z == p.d - 1;
                p.set(x, 0, z, rim ? stoneMix(p, x, 0, z) : floor);
            }
    }

    /**
     * Timber-framed walls from y = 1 to top: dark log corners and posts, a cobble footing, plaster between,
     * a beam along the top. {@code storeys} lists the y of each beam row (floor levels inside).
     */
    static void walls(Plot p, int top, Style st, int... beams) {
        walls(p, 0, p.w - 1, 0, p.d - 1, top, st, beams);
    }

    /** The same walls around the rectangle x0..x1, z0..z1 (for a wing). */
    static void walls(Plot p, int x0, int x1, int z0, int z1, int top, Style st, int... beams) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                boolean edgeX = x == x0 || x == x1, edgeZ = z == z0 || z == z1;
                if (!edgeX && !edgeZ) continue;
                boolean corner = edgeX && edgeZ;
                boolean post = corner || (edgeZ && x % 4 == 0) || (edgeX && z % 4 == 0);
                Direction.Axis along = edgeZ ? Direction.Axis.X : Direction.Axis.Z;
                for (int y = 1; y <= top; y++) {
                    BlockState s;
                    boolean beam = y == top;
                    for (int b : beams) beam |= y == b;
                    boolean foot = y <= st.footRows();
                    switch (st.kind()) {
                        case SOLID -> {
                            if (corner) s = log(st.frame(), Direction.Axis.Y);
                            else if (foot) s = footing(p, st, x, y, z);
                            else if (beam) s = log(st.beam(), along);
                            else s = st.plaster() == Blocks.COBBLESTONE ? cobbleMix(p, x, y, z).defaultBlockState() : st.plaster().defaultBlockState();
                        }
                        case LOGS -> {
                            if (corner) s = log(st.frame(), Direction.Axis.Y);
                            else if (foot) s = footing(p, st, x, y, z);
                            else s = log(st.plaster(), along);
                        }
                        default -> {
                            if (post && !(foot && st.footRows() > 1)) s = log(st.frame(), Direction.Axis.Y);
                            else if (foot) s = footing(p, st, x, y, z);
                            else if (beam) s = log(st.beam(), along);
                            else s = st.plaster().defaultBlockState();
                        }
                    }
                    p.set(x, y, z, s);
                }
            }
    }

    static BlockState footing(Plot p, Style st, int x, int y, int z) {
        if (st.footing() == Blocks.COBBLESTONE) return cobbleMix(p, x, y, z).defaultBlockState();
        if (st.footing() == Blocks.STONE_BRICKS) return stoneMix(p, x, y, z).defaultBlockState();
        return st.footing().defaultBlockState();
    }

    /** A flat plank ceiling over the top storey, so the underside of the roof is never seen from inside. */
    static void ceiling(Plot p, int x0, int x1, int z0, int z1, int top, Block b) {
        p.fill(x0 + 1, top, z0 + 1, x1 - 1, top, z1 - 1, b);
    }

    static void ceiling(Plot p, int top, Block b) {
        p.fill(1, top, 1, p.w - 2, top, p.d - 2, b);
    }

    /** Stone walls (for the chapel and the tower). */
    static void stoneWalls(Plot p, int top) {
        for (int x = 0; x < p.w; x++)
            for (int z = 0; z < p.d; z++) {
                if (x != 0 && z != 0 && x != p.w - 1 && z != p.d - 1) continue;
                for (int y = 1; y <= top; y++) p.set(x, y, z, stoneMix(p, x, y, z));
            }
    }

    /** A gable roof with its ridge running front to back (gables to the street), with a ceiling under it. */
    static void roof(Plot p, int top, Style st) {
        roof(p, top, st, false);
    }

    /**
     * A gable roof over the whole building with a block of overhang all round and filled gables, and a ceiling
     * under it. {@code ridgeAcross}: the ridge runs along the front, so the street sees the eaves instead of a gable.
     */
    static void roof(Plot p, int top, Style st, boolean ridgeAcross) {
        ceiling(p, top, st.floor());
        roof(p, 0, p.w - 1, 0, p.d - 1, top, st, ridgeAcross);
    }

    /** A gable roof over the rectangle x0..x1, z0..z1 (no ceiling), ridge along z or, with {@code across}, along x. */
    static void roof(Plot p, int x0, int x1, int z0, int z1, int top, Style st, boolean across) {
        if (!across) {
            for (int i = 0; ; i++) {
                int l = x0 - 1 + i, r = x1 + 1 - i, y = top + 1 + i;
                if (l > r) break;
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    if (l == r) p.set(l, y, z, slab(st.roofSlab(), SlabType.BOTTOM));
                    else {
                        p.set(l, y, z, stairs(st.roofStairs(), EAST));
                        p.set(r, y, z, stairs(st.roofStairs(), WEST));
                        if (i > 0 && (z == z0 - 1 || z == z1 + 1)) {
                            p.set(l, y - 1, z, stairsTop(st.roofStairs(), WEST));
                            p.set(r, y - 1, z, stairsTop(st.roofStairs(), EAST));
                        }
                    }
                }
                for (int x = l; x <= r; x++) {
                    if (y - 1 <= top) continue;
                    for (int z : new int[]{z0, z1}) p.set(x, y - 1, z, gable(st, x == (x0 + x1) / 2));
                }
            }
        } else {
            for (int i = 0; ; i++) {
                int f = z0 - 1 + i, b = z1 + 1 - i, y = top + 1 + i;
                if (f > b) break;
                for (int x = x0 - 1; x <= x1 + 1; x++) {
                    if (f == b) p.set(x, y, f, slab(st.roofSlab(), SlabType.BOTTOM));
                    else {
                        p.set(x, y, f, stairs(st.roofStairs(), SOUTH));
                        p.set(x, y, b, stairs(st.roofStairs(), NORTH));
                        if (i > 0 && (x == x0 - 1 || x == x1 + 1)) {
                            p.set(x, y - 1, f, stairsTop(st.roofStairs(), NORTH));
                            p.set(x, y - 1, b, stairsTop(st.roofStairs(), SOUTH));
                        }
                    }
                }
                for (int z = f; z <= b; z++) {
                    if (y - 1 <= top) continue;
                    for (int x : new int[]{x0, x1}) p.set(x, y - 1, z, gable(st, z == (z0 + z1) / 2));
                }
            }
        }
    }

    // ── features that make houses differ in shape, not only in colour ────────

    /** A covered porch over the front door: two posts, a slab roof and a plank deck. */
    static void porch(Plot p, Style st, int x0, int x1) {
        for (int x = x0; x <= x1; x++)
            for (int z = -2; z <= -1; z++) {
                p.set(x, 0, z, st.floor());
                p.set(x, 3, z, slab(st.roofSlab(), SlabType.BOTTOM));
            }
        for (int x : new int[]{x0, x1}) {
            p.set(x, 1, -2, log(st.frame(), Direction.Axis.Y));
            p.set(x, 2, -2, log(st.frame(), Direction.Axis.Y));
        }
        p.set(x0, 2, -1, lantern(false).setValue(LanternBlock.HANGING, false));
        p.set(x1 - 1, 1, -2, Blocks.POTTED_RED_TULIP);
    }

    /** A brick chimney stack against the outside of the left wall, smoking at the top. */
    static void chimney(Plot p, int z, int height) {
        for (int y = 0; y <= height; y++) p.set(-1, y, z, Blocks.BRICKS);
        p.set(-1, height + 1, z, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
    }

    /** A little stone tower on the front right corner, rising above the roof with a pointed cap. */
    static void cornerTower(Plot p, int top, Style st) {
        int x0 = p.w - 3, z0 = 0, h = top + 5;
        for (int x = x0; x <= x0 + 2; x++)
            for (int z = z0; z <= z0 + 2; z++)
                for (int y = 1; y <= h; y++) {
                    boolean ring = x == x0 || x == x0 + 2 || z == z0 || z == z0 + 2;
                    if (ring) p.set(x, y, z, stoneMix(p, x, y, z));
                    else if (y > top) p.set(x, y, z, Blocks.AIR);
                }
        p.set(x0 + 1, h - 1, z0, pane(Blocks.GLASS_PANE, Direction.Axis.X));
        p.set(x0 + 2, h - 1, z0 + 1, pane(Blocks.GLASS_PANE, Direction.Axis.Z));
        for (int x = x0 - 1; x <= x0 + 3; x++)
            for (int z = z0 - 1; z <= z0 + 3; z++) {
                boolean ring = x == x0 - 1 || x == x0 + 3 || z == z0 - 1 || z == z0 + 3;
                if (!ring) { p.set(x, h + 1, z, st.roofSlab() == Blocks.STONE_BRICK_SLAB ? Blocks.DEEPSLATE_TILES : Blocks.DEEPSLATE_TILES); continue; }
                Direction f = x == x0 - 1 ? EAST : x == x0 + 3 ? WEST : z == z0 - 1 ? SOUTH : NORTH;
                p.set(x, h + 1, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, f));
            }
        for (int x = x0; x <= x0 + 2; x++)
            for (int z = z0; z <= z0 + 2; z++) {
                boolean ring = x == x0 || x == x0 + 2 || z == z0 || z == z0 + 2;
                Direction f = x == x0 ? EAST : x == x0 + 2 ? WEST : z == z0 ? SOUTH : NORTH;
                p.set(x, h + 2, z, ring ? stairs(Blocks.DEEPSLATE_TILE_STAIRS, f) : Blocks.DEEPSLATE_TILES.defaultBlockState());
            }
        p.set(x0 + 1, h + 3, z0 + 1, Blocks.DEEPSLATE_TILE_WALL);
    }

    private static BlockState gable(Style st, boolean middle) {
        if (st.kind() == Walls.LOGS) return st.floor().defaultBlockState();
        if (st.kind() == Walls.SOLID) return st.plaster() == Blocks.COBBLESTONE ? Blocks.SPRUCE_PLANKS.defaultBlockState() : st.plaster().defaultBlockState();
        return middle ? log(st.frame(), Direction.Axis.Y) : st.plaster().defaultBlockState();
    }

    /**
     * A small gable over the front door on a roof whose ridge runs along the front: a front-facing roof wing
     * {@code 2 * half + 1} wide centred on {@code cx}, which sinks into the main roof behind it.
     */
    static void frontGable(Plot p, int top, Style st, int cx, int half) {
        for (int i = 0; ; i++) {
            int l = cx - half - 1 + i, r = cx + half + 1 - i, y = top + 1 + i;
            if (l > r) break;
            for (int z = -1; z <= p.d / 2; z++) {
                if (y < top + 2 + z) continue;                 // hidden inside the main roof from here back
                if (l == r) p.set(l, y, z, slab(st.roofSlab(), SlabType.BOTTOM));
                else {
                    p.set(l, y, z, stairs(st.roofStairs(), EAST));
                    p.set(r, y, z, stairs(st.roofStairs(), WEST));
                }
            }
            if (i > 0 && l < r) {
                p.set(l, y - 1, -1, stairsTop(st.roofStairs(), WEST));
                p.set(r, y - 1, -1, stairsTop(st.roofStairs(), EAST));
            }
            for (int x = l + 1; x <= r - 1; x++) if (y > top) p.set(x, y, 0, gable(st, x == cx));
        }
    }

    /** A window of glass panes {@code h} tall in the wall at (x, z); a sill outside the wall. */
    static void window(Plot p, int x, int y, int z, int h) {
        boolean alongX = z == 0 || z == p.d - 1;
        for (int k = 0; k < h; k++) p.set(x, y + k, z, pane(Blocks.GLASS_PANE, alongX ? Direction.Axis.X : Direction.Axis.Z));
        // a flower box under front and back windows
        if (alongX) {
            int oz = z == 0 ? -1 : p.d;
            p.set(x, y - 1, oz, stairsTop(Blocks.SPRUCE_STAIRS, z == 0 ? SOUTH : NORTH));
        }
    }

    /** A door in the front wall (or another wall with {@code face}), with a step and a lantern beside it outside. */
    static void door(Plot p, int x, int z, Direction face, Block door) {
        // facing inwards puts the door leaf on the outer face of the wall, flush with the outside
        BlockState lower = door.defaultBlockState().setValue(DoorBlock.FACING, face.getOpposite()).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT).setValue(DoorBlock.OPEN, false);
        p.set(x, 1, z, lower);
        p.set(x, 2, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        int ox = x + face.getStepX(), oz = z + face.getStepZ();
        p.set(ox, 0, oz, Blocks.STONE_BRICKS);
        p.set(ox, 1, oz, Blocks.AIR);
        p.set(ox, 2, oz, Blocks.AIR);
        // a house standing above the ground gets steps down from its door
        BlockPos step = p.pos(ox, 0, oz);
        int drop = Math.min(8, p.floorY - p.canvas.ground(step.getX(), step.getZ()));
        for (int y = -1; y >= -drop - 1; y--) p.set(ox, y, oz, Blocks.COBBLESTONE);
        for (int k = 1; k <= drop; k++) {
            int sx = ox + face.getStepX() * k, sz = oz + face.getStepZ() * k;
            p.set(sx, -k, sz, stairs(Blocks.COBBLESTONE_STAIRS, face.getOpposite()));
            for (int y = -k - 1; y >= -drop - 1; y--) p.set(sx, y, sz, Blocks.COBBLESTONE);
            for (int y = -k + 1; y <= -k + 3; y++) p.set(sx, y, sz, Blocks.AIR);
        }
    }

    static void bed(Plot p, int x, int y, int z, Direction toHead, Block bed) {
        p.set(x, y, z, bed.defaultBlockState().setValue(BedBlock.FACING, toHead).setValue(BedBlock.PART, BedPart.FOOT));
        p.set(x + toHead.getStepX(), y, z + toHead.getStepZ(), bed.defaultBlockState().setValue(BedBlock.FACING, toHead).setValue(BedBlock.PART, BedPart.HEAD));
    }

    /** A little table: a fence post with a cloth on it. */
    static void table(Plot p, int x, int y, int z, Block cloth) {
        p.set(x, y, z, Blocks.SPRUCE_FENCE);
        p.set(x, y + 1, z, cloth);
    }

    /** A chair facing {@code toward} (a stair with its back away from it). */
    static void chair(Plot p, int x, int y, int z, Direction toward) {
        p.set(x, y, z, stairs(Blocks.SPRUCE_STAIRS, toward.getOpposite()));
    }

    static void chest(Plot p, int x, int y, int z, Direction front, String loot) {
        p.set(x, y, z, facing(Blocks.CHEST, front));
        if (loot != null) p.canvas.lootLater(p.pos(x, y, z), loot);
    }

    // ── the small house (9 x 7) ──────────────────────────────────────────────

    private static final Block[] BEDS = {Blocks.RED_BED, Blocks.BLUE_BED, Blocks.GREEN_BED, Blocks.BROWN_BED, Blocks.YELLOW_BED, Blocks.CYAN_BED, Blocks.WHITE_BED};
    private static final Block[] RUGS = {Blocks.RED_CARPET, Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.BLUE_CARPET, Blocks.ORANGE_CARPET, Blocks.LIGHT_GRAY_CARPET};
    private static final Block[] POTS = {Blocks.POTTED_POPPY, Blocks.POTTED_FERN, Blocks.POTTED_BLUE_ORCHID, Blocks.POTTED_DANDELION,
            Blocks.POTTED_AZURE_BLUET, Blocks.POTTED_RED_TULIP, Blocks.POTTED_CORNFLOWER, Blocks.POTTED_LILY_OF_THE_VALLEY};
    private static final Block[] DOORS = {Blocks.SPRUCE_DOOR, Blocks.DARK_OAK_DOOR, Blocks.OAK_DOOR, Blocks.BIRCH_DOOR, Blocks.MANGROVE_DOOR};

    static Block pick(Plot p, Block[] from, int salt) {
        return from[(p.noise(salt, 7, salt * 3) >> 3) % from.length];
    }

    /** Extra features a house may have, as bits. */
    static final int PORCH = 1, CHIMNEY = 2, TOWER = 4;

    static List<Resident> smallHouse(Plot p, Style st, String job, Block workstation) {
        return smallHouse(p, st, job, workstation, false, false, 0);
    }

    /** {@code across}: ridge along the front; {@code gable}: a little front gable over the door (with {@code across}). */
    static List<Resident> smallHouse(Plot p, Style st, String job, Block workstation, boolean across, boolean gable, int extras) {
        int top = 4;
        site(p, top + 6, st.floor());
        walls(p, top, st);
        roof(p, top, st, across);
        if (across && gable) frontGable(p, top, st, 4, 2);
        if ((extras & PORCH) != 0) porch(p, st, 2, 6);
        if ((extras & CHIMNEY) != 0) chimney(p, 3, top + (across ? 4 : 5) + 1);
        door(p, 4, 0, NORTH, pick(p, DOORS, 1));
        // rug, bed and pots change from house to house
        Block rug = pick(p, RUGS, 2), bedColour = pick(p, BEDS, 3);
        p.set(1, 2, 1, pick(p, POTS, 4));
        window(p, 2, 2, 0, 2);
        window(p, 6, 2, 0, 2);
        window(p, 0, 2, 3, 2);
        window(p, 8, 2, 3, 2);
        window(p, 4, 2, 6, 2);
        // tie beam with a lantern
        for (int x = 1; x <= 7; x++) p.set(x, top, 3, log(st.beam(), Direction.Axis.X));
        p.set(4, top - 1, 3, lantern(true));
        // bed and chest in the back left corner
        bed(p, 1, 1, 4, SOUTH, bedColour);
        chest(p, 2, 1, 5, NORTH, "house");
        p.set(1, 1, 1, Blocks.BOOKSHELF);
        // kitchen on the right
        p.set(7, 1, 3, facing(Blocks.FURNACE, WEST));
        p.set(7, 1, 4, Blocks.CRAFTING_TABLE);
        p.set(7, 1, 5, facing(Blocks.BARREL, UP));
        p.set(7, 2, 5, Blocks.POTTED_FERN);
        if (workstation != null) p.set(7, 1, 1, workstation.defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? facing(workstation, WEST) : workstation.defaultBlockState());
        // table with two chairs, a rug
        table(p, 4, 1, 3, Blocks.WHITE_CARPET);
        chair(p, 3, 1, 3, EAST);
        chair(p, 5, 1, 3, WEST);
        for (int x = 3; x <= 5; x++) p.set(x, 1, 4, rug);
        return List.of(new Resident(new BlockPos(4, 1, 2), job));
    }

    // ── the long house (11 x 7): kitchen and bedroom divided by a wall ────────

    static List<Resident> longHouse(Plot p, Style st, String job, boolean across, int extras) {
        int top = 4;
        site(p, top + 6, st.floor());
        walls(p, top, st);
        roof(p, top, st, across);
        if ((extras & PORCH) != 0) porch(p, st, 1, 5);
        if ((extras & CHIMNEY) != 0) chimney(p, 3, top + (across ? 4 : 6) + 1);
        door(p, 3, 0, NORTH, pick(p, DOORS, 1));
        window(p, 1, 2, 0, 2);
        window(p, 7, 2, 0, 2);
        window(p, 9, 2, 0, 2);
        window(p, 0, 2, 3, 2);
        window(p, 10, 2, 3, 2);
        window(p, 3, 2, 6, 2);
        window(p, 8, 2, 6, 2);
        // the dividing wall with its own door
        for (int z = 1; z <= 5; z++) for (int y = 1; y < top; y++) p.set(6, y, z, st.kind() == Walls.LOGS ? log(st.plaster(), Direction.Axis.Z) : st.floor().defaultBlockState());
        BlockState inner = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        p.set(6, 1, 3, inner);
        p.set(6, 2, 3, inner.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        // kitchen
        p.set(1, 1, 5, facing(Blocks.SMOKER, NORTH));
        p.set(2, 1, 5, facing(Blocks.FURNACE, NORTH));
        p.set(3, 1, 5, Blocks.CRAFTING_TABLE);
        p.set(4, 1, 5, facing(Blocks.BARREL, UP));
        p.set(4, 2, 5, pick(p, POTS, 5));
        p.set(1, 1, 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        table(p, 3, 1, 3, Blocks.WHITE_CARPET);
        chair(p, 2, 1, 3, EAST);
        chair(p, 4, 1, 3, WEST);
        // bedroom
        Block bedColour = pick(p, BEDS, 3);
        bed(p, 9, 1, 4, SOUTH, bedColour);
        bed(p, 7, 1, 4, SOUTH, bedColour);
        chest(p, 8, 1, 5, NORTH, "house");
        p.set(9, 1, 1, Blocks.BOOKSHELF);
        p.set(9, 2, 1, pick(p, POTS, 6));
        p.fill(7, 1, 2, 9, 1, 3, pick(p, RUGS, 2));
        for (int x = 1; x <= 9; x++) p.set(x, top, 3, log(st.beam(), Direction.Axis.X));
        p.set(3, top - 1, 3, lantern(true));
        p.set(8, top - 1, 3, lantern(true));
        return List.of(new Resident(new BlockPos(3, 1, 2), job));
    }

    // ── the L house (9 x 11): a hall across the front, a bedroom wing behind, a garden in the corner ──

    static List<Resident> lHouse(Plot p, Style st, String job, int extras) {
        int top = 4;
        site(p, top + 7, st.floor());
        walls(p, 0, 8, 0, 6, top, st);
        walls(p, 0, 4, 6, 10, top, st);
        ceiling(p, 0, 8, 0, 6, top, st.floor());
        ceiling(p, 0, 4, 6, 10, top, st.floor());
        p.fill(1, top, 6, 3, top, 6, st.floor());
        roof(p, 0, 8, 0, 6, top, st, true);
        roof(p, 0, 4, 6, 10, top, st, false);
        // the way through to the wing
        for (int y = 1; y <= 2; y++) for (int x = 1; x <= 3; x++) p.set(x, y, 6, Blocks.AIR);
        door(p, 5, 0, NORTH, pick(p, DOORS, 1));
        if ((extras & CHIMNEY) != 0) chimney(p, 3, top + 5);
        window(p, 2, 2, 0, 2);
        window(p, 7, 2, 0, 2);
        window(p, 8, 2, 3, 2);
        window(p, 0, 2, 3, 2);
        window(p, 0, 2, 8, 2);
        window(p, 2, 2, 10, 2);
        window(p, 4, 2, 8, 2);
        // the garden in the corner: grass, flowers, a fence with a gate, a bench
        for (int x = 5; x <= 8; x++)
            for (int z = 7; z <= 10; z++) {
                p.set(x, 0, z, Blocks.GRASS_BLOCK);
                for (int y = 1; y <= 3; y++) p.set(x, y, z, Blocks.AIR);
                boolean edge = x == 8 || z == 10;
                if (edge) p.set(x, 1, z, x == 8 && z == 8 ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, EAST)
                        : Blocks.SPRUCE_FENCE.defaultBlockState());
                else if ((x + z) % 2 == 0) p.set(x, 1, z, pick(p, new Block[]{Blocks.POPPY, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY, Blocks.ALLIUM}, x * 7 + z));
            }
        p.set(6, 1, 7, stairs(Blocks.SPRUCE_STAIRS, SOUTH));
        p.set(7, 1, 7, stairs(Blocks.SPRUCE_STAIRS, SOUTH));
        p.set(5, 1, 9, Blocks.COMPOSTER);
        // the hall: kitchen on the right, table in the middle
        p.set(7, 1, 5, facing(Blocks.FURNACE, NORTH));
        p.set(6, 1, 5, Blocks.CRAFTING_TABLE);
        p.set(5, 1, 5, facing(Blocks.SMOKER, NORTH));
        p.set(7, 1, 1, facing(Blocks.BARREL, UP));
        p.set(7, 2, 1, pick(p, POTS, 4));
        table(p, 4, 1, 3, Blocks.WHITE_CARPET);
        chair(p, 3, 1, 3, EAST);
        chair(p, 5, 1, 3, WEST);
        p.fill(3, 1, 4, 5, 1, 4, pick(p, RUGS, 2));
        p.set(1, 1, 1, Blocks.BOOKSHELF);
        p.set(1, 2, 1, pick(p, POTS, 5));
        // the wing: bedroom
        Block bedColour = pick(p, BEDS, 3);
        bed(p, 1, 1, 8, SOUTH, bedColour);
        chest(p, 2, 1, 9, NORTH, "house");
        p.set(3, 1, 9, facing(Blocks.BARREL, NORTH));
        p.set(3, 1, 7, pick(p, RUGS, 6));
        for (int x = 1; x <= 7; x++) p.set(x, top, 3, log(st.beam(), Direction.Axis.X));
        p.set(4, top - 1, 3, lantern(true));
        p.set(2, top - 1, 8, lantern(true));
        return List.of(new Resident(new BlockPos(4, 1, 2), job));
    }

    // ── the bakery (9 x 7): brick oven, flour sacks, a counter for the bread ──

    static List<Resident> bakery(Plot p) {
        Style st = MUD;
        int top = 4;
        site(p, top + 8, Blocks.SPRUCE_PLANKS);
        walls(p, top, st);
        roof(p, top, st, true);
        frontGable(p, top, st, 4, 2);
        door(p, 4, 0, NORTH, Blocks.OAK_DOOR);
        window(p, 2, 2, 0, 2);
        window(p, 6, 2, 0, 2);
        window(p, 0, 2, 3, 2);
        // the oven: a brick dome in the back right corner with its chimney
        p.fill(6, 1, 4, 7, 2, 5, Blocks.BRICKS);
        p.set(6, 1, 4, facing(Blocks.SMOKER, NORTH));
        p.set(7, 1, 4, facing(Blocks.FURNACE, NORTH));
        for (int y = 3; y <= top + 6; y++) p.set(7, y, 5, Blocks.BRICKS);
        p.set(7, top + 7, 5, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        // flour and wheat
        p.set(1, 1, 5, Blocks.HAY_BLOCK);
        p.set(2, 1, 5, Blocks.HAY_BLOCK);
        p.set(1, 2, 5, Blocks.HAY_BLOCK);
        p.set(1, 1, 4, facing(Blocks.BARREL, UP));
        p.set(3, 1, 5, Blocks.COMPOSTER);
        // the counter facing the door
        for (int x = 2; x <= 6; x++) p.set(x, 1, 2, slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
        p.set(3, 2, 2, Blocks.CAKE);
        p.set(5, 2, 2, candles(2));
        p.set(4, top - 1, 3, lantern(true));
        for (int x = 1; x <= 7; x++) p.set(x, top, 3, log(st.beam(), Direction.Axis.X));
        return List.of(new Resident(new BlockPos(4, 1, 3), "baker"), new Resident(new BlockPos(4, 1, 1), "cat"));
    }

    // ── the manor (15 x 11, two storeys) where the village head lives ───────

    static List<Resident> manor(Plot p) {
        Style st = STONE_HALF;
        int mid = 5, top = 10;
        site(p, top + 10, Blocks.DARK_OAK_PLANKS);
        walls(p, top, st, mid);
        roof(p, top, new Style(Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DARK_OAK_PLANKS, st.frame(), st.beam(),
                st.plaster(), st.footing(), st.footRows(), st.kind()), true);
        frontGable(p, top, new Style(Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DARK_OAK_PLANKS, st.frame(), st.beam(),
                st.plaster(), st.footing(), st.footRows(), st.kind()), 7, 3);
        p.fill(1, mid, 1, 13, mid, 9, Blocks.DARK_OAK_PLANKS);
        door(p, 7, 0, NORTH, Blocks.DARK_OAK_DOOR);
        for (int x : new int[]{2, 4, 10, 12}) { window(p, x, 2, 0, 2); window(p, x, 7, 0, 2); window(p, x, 7, 10, 2); window(p, x, 2, 10, 2); }
        for (int z : new int[]{3, 7}) { window(p, 0, 2, z, 2); window(p, 14, 2, z, 2); window(p, 0, 7, z, 2); window(p, 14, 7, z, 2); }
        p.set(7, 7, 0, pane(Blocks.GLASS_PANE, Direction.Axis.X));
        p.set(7, 8, 0, pane(Blocks.GLASS_PANE, Direction.Axis.X));
        // great hall: a long table, a fireplace, banners of colour (carpets) and shelves
        for (int x = 4; x <= 10; x++) { p.set(x, 1, 5, Blocks.DARK_OAK_FENCE); p.set(x, 2, 5, Blocks.RED_CARPET); }
        for (int x = 4; x <= 10; x += 2) { chair(p, x, 1, 4, SOUTH); chair(p, x, 1, 6, NORTH); }
        chair(p, 3, 1, 5, EAST);
        p.set(6, 2, 5, candles(3));
        p.set(9, 2, 5, candles(2));
        for (int y = 1; y <= top + 6; y++) for (int z = 4; z <= 6; z++) p.set(14, y, z, Blocks.STONE_BRICKS);
        p.fill(13, 1, 4, 13, 3, 6, Blocks.STONE_BRICKS);
        p.set(13, 1, 5, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        p.set(13, 2, 5, Blocks.AIR);
        p.set(14, top + 7, 5, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        for (int x = 1; x <= 4; x++) for (int y = 1; y <= 3; y++) p.set(x, y, 9, Blocks.BOOKSHELF);
        p.set(2, 1, 8, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, SOUTH));
        p.fill(5, 1, 7, 9, 1, 8, Blocks.RED_CARPET);
        // stairs up along the left wall
        for (int k = 0; k < 5; k++) {
            p.set(1, 1 + k, 7 - k, stairs(Blocks.DARK_OAK_STAIRS, NORTH));
            p.set(1, mid, 7 - k, k == 4 ? stairs(Blocks.DARK_OAK_STAIRS, NORTH) : Blocks.AIR.defaultBlockState());
        }
        for (int z = 4; z <= 7; z++) p.set(2, mid + 1, z, Blocks.DARK_OAK_FENCE);
        for (int z = 1; z <= 9; z += 4) for (int x = 2; x <= 13; x++) p.set(x, mid, z, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
        p.set(7, mid - 1, 5, Blocks.CHAIN);
        p.set(7, mid - 2, 5, lantern(true));
        p.set(4, mid - 1, 2, lantern(true));
        p.set(10, mid - 1, 2, lantern(true));
        // the lord's rooms upstairs
        bed(p, 10, mid + 1, 8, SOUTH, Blocks.PURPLE_BED);
        bed(p, 11, mid + 1, 8, SOUTH, Blocks.PURPLE_BED);
        chest(p, 12, mid + 1, 9, NORTH, "house");
        chest(p, 9, mid + 1, 9, NORTH, "house");
        p.fill(8, mid + 1, 4, 12, mid + 1, 7, Blocks.PURPLE_CARPET);
        p.set(13, mid + 1, 1, facing(Blocks.BARREL, NORTH));
        p.set(12, mid + 1, 1, facing(Blocks.BARREL, NORTH));
        for (int x = 4; x <= 6; x++) p.set(x, mid + 1, 9, Blocks.BOOKSHELF);
        table(p, 5, mid + 1, 6, Blocks.WHITE_CARPET);
        chair(p, 5, mid + 1, 7, NORTH);
        p.set(3, mid + 1, 1, pick(p, POTS, 9));
        for (int x = 1; x <= 13; x++) p.set(x, top, 5, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
        p.set(7, top - 1, 5, lantern(true));
        // a little garden in front: hedges and flowers either side of the path
        for (int x = 1; x <= 13; x++) if (x < 6 || x > 8) {
            p.set(x, 1, -2, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            p.set(x, 1, -3, (x % 2 == 0 ? Blocks.POPPY : Blocks.OXEYE_DAISY).defaultBlockState());
        }
        return List.of(new Resident(new BlockPos(7, 1, 2), "librarian"), new Resident(new BlockPos(6, mid + 1, 3), ""),
                new Resident(new BlockPos(5, 1, 2), "cat"), new Resident(new BlockPos(7, 1, -5), "golem"));
    }

    // ── the two-storey house (9 x 9) ─────────────────────────────────────────

    static List<Resident> tallHouse(Plot p, Style st, String job, Block workstation) {
        return tallHouse(p, st, job, workstation, false, false, 0);
    }

    static List<Resident> tallHouse(Plot p, Style st, String job, Block workstation, boolean across, boolean gable, int extras) {
        int mid = 4, top = 8;
        site(p, top + 10, st.floor());
        walls(p, top, st, mid);
        roof(p, top, st, across);
        if (across && gable) frontGable(p, top, st, 4, 2);
        if ((extras & TOWER) != 0) cornerTower(p, top, st);
        if ((extras & CHIMNEY) != 0) chimney(p, 4, top + 5 + 1);
        // upper floor with a hole for the ladder
        p.fill(1, mid, 1, p.w - 2, mid, p.d - 2, st.floor());
        p.set(7, mid, 7, Blocks.AIR);
        for (int y = 1; y <= mid; y++) p.set(7, y, 7, facing(Blocks.LADDER, NORTH));
        door(p, 4, 0, NORTH, pick(p, DOORS, 1));
        for (int x : new int[]{2, 6}) { window(p, x, 2, 0, 2); window(p, x, 6, 0, 2); window(p, x, 6, 8, 2); }
        for (int z : new int[]{3, 5}) { window(p, 0, 2, z, 2); window(p, 8, 2, z, 2); window(p, 0, 6, z, 1); window(p, 8, 6, z, 1); }
        // ground floor: kitchen and dining
        p.set(1, 1, 7, facing(Blocks.SMOKER, EAST));
        p.set(2, 1, 7, Blocks.CRAFTING_TABLE);
        p.set(3, 1, 7, facing(Blocks.BARREL, UP));
        p.set(3, 2, 7, Blocks.POTTED_RED_TULIP);
        p.set(1, 1, 6, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        p.set(1, 1, 1, Blocks.BOOKSHELF);
        p.set(1, 2, 1, Blocks.BOOKSHELF);
        table(p, 4, 1, 4, Blocks.WHITE_CARPET);
        table(p, 5, 1, 4, Blocks.WHITE_CARPET);
        chair(p, 3, 1, 4, EAST);
        chair(p, 6, 1, 4, WEST);
        chair(p, 4, 1, 5, NORTH);
        chair(p, 5, 1, 3, SOUTH);
        if (workstation != null) p.set(7, 1, 1, workstation.defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? facing(workstation, WEST) : workstation.defaultBlockState());
        p.set(4, mid - 1, 4, lantern(true));
        // upper floor: bedroom
        Block bedColour = pick(p, BEDS, 3);
        bed(p, 1, mid + 1, 6, SOUTH, bedColour);
        bed(p, 3, mid + 1, 6, SOUTH, bedColour);
        p.set(2, mid + 1, 7, facing(Blocks.CHEST, NORTH));
        p.canvas.lootLater(p.pos(2, mid + 1, 7), "house");
        p.set(5, mid + 1, 7, facing(Blocks.BARREL, NORTH));
        p.set(6, mid + 1, 7, facing(Blocks.BARREL, NORTH));
        p.set(5, mid + 2, 7, Blocks.POTTED_BLUE_ORCHID);
        p.fill(2, mid + 1, 2, 5, mid + 1, 4, Blocks.BROWN_CARPET);
        p.set(7, mid + 1, 1, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, SOUTH));
        for (int x = 1; x <= 7; x++) p.set(x, top, 4, log(st.beam(), Direction.Axis.X));
        p.set(4, top - 1, 4, lantern(true));
        return List.of(new Resident(new BlockPos(4, 1, 2), job), new Resident(new BlockPos(3, mid + 1, 3), ""));
    }

    // ── the tavern (13 x 11, two storeys) ────────────────────────────────────

    static List<Resident> tavern(Plot p) {
        Style st = BRICK;
        int mid = 5, top = 9;
        site(p, top + 8, Blocks.SPRUCE_PLANKS);
        walls(p, top, st, mid);
        roof(p, top, st);
        p.fill(1, mid, 1, 11, mid, 9, Blocks.SPRUCE_PLANKS);
        door(p, 6, 0, NORTH, Blocks.SPRUCE_DOOR);
        for (int x : new int[]{2, 3, 9, 10}) { window(p, x, 2, 0, 2); window(p, x, 7, 0, 2); window(p, x, 7, 10, 2); }
        for (int z : new int[]{3, 7}) { window(p, 12, 2, z, 2); window(p, 0, 7, z, 2); window(p, 12, 7, z, 2); }
        // fireplace and chimney in the left wall
        for (int y = 1; y <= top + 7; y++) for (int z = 3; z <= 5; z++) p.set(0, y, z, Blocks.BRICKS);
        p.fill(1, 1, 3, 1, 3, 5, Blocks.BRICKS);
        p.set(1, 1, 4, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        p.set(1, 2, 4, Blocks.AIR);
        p.set(1, 3, 4, Blocks.BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        p.set(0, top + 8, 4, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        // bar: counter, barrels behind, the brewing corner
        for (int x = 3; x <= 9; x++) p.set(x, 1, 7, log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        p.set(10, 1, 7, Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, EAST));
        for (int x = 2; x <= 10; x++) {
            p.set(x, 1, 9, facing(Blocks.BARREL, NORTH));
            if (x % 2 == 0) p.set(x, 2, 9, facing(Blocks.BARREL, NORTH));
        }
        p.set(4, 2, 7, Blocks.BREWING_STAND);
        p.set(7, 2, 7, candles(3));
        p.set(1, 1, 9, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        p.set(1, 1, 8, facing(Blocks.SMOKER, EAST));
        // tables and chairs
        for (int[] t : new int[][]{{4, 3}, {8, 3}}) {
            table(p, t[0], 1, t[1], Blocks.RED_CARPET);
            chair(p, t[0] - 1, 1, t[1], EAST);
            chair(p, t[0] + 1, 1, t[1], WEST);
            chair(p, t[0], 1, t[1] - 1, SOUTH);
            chair(p, t[0], 1, t[1] + 1, NORTH);
        }
        // stairs to the rooms along the right wall
        for (int k = 0; k < 5; k++) {
            p.set(11, 1 + k, 1 + k, stairs(Blocks.SPRUCE_STAIRS, SOUTH));
            p.set(11, mid, 1 + k, k == 4 ? stairs(Blocks.SPRUCE_STAIRS, SOUTH) : Blocks.AIR.defaultBlockState());
        }
        for (int k = 0; k < 4; k++) p.set(10, mid + 1, 1 + k, fence(Blocks.SPRUCE_FENCE, k > 0, false, k < 3, false));
        p.set(6, mid - 1, 5, lantern(true));
        p.set(3, mid - 1, 3, lantern(true));
        p.set(9, mid - 1, 3, lantern(true));
        // rooms upstairs: beds along the back, chests, a rug and a table by the front windows
        for (int x : new int[]{1, 4, 7}) {
            bed(p, x, mid + 1, 8, SOUTH, Blocks.WHITE_BED);
            p.set(x + 1, mid + 1, 9, facing(Blocks.CHEST, NORTH));
            p.canvas.lootLater(p.pos(x + 1, mid + 1, 9), "house");
        }
        p.fill(2, mid + 1, 4, 8, mid + 1, 6, Blocks.GREEN_CARPET);
        table(p, 3, mid + 1, 2, Blocks.WHITE_CARPET);
        chair(p, 2, mid + 1, 2, EAST);
        p.set(5, mid + 1, 1, Blocks.BOOKSHELF);
        p.set(5, mid + 2, 1, Blocks.POTTED_DANDELION);
        for (int x = 1; x <= 11; x++) p.set(x, top, 5, log(st.beam(), Direction.Axis.X));
        p.set(6, top - 1, 5, lantern(true));
        // a hanging sign board outside: a beam with a lantern
        p.set(6, 4, -1, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        p.set(6, 3, -1, lantern(true));
        return List.of(new Resident(new BlockPos(6, 1, 8), "innkeeper"), new Resident(new BlockPos(5, 1, 5), "barmaid"), new Resident(new BlockPos(9, 1, 5), ""),
                new Resident(new BlockPos(4, mid + 1, 5), ""), new Resident(new BlockPos(6, 1, 2), "cat"));
    }

    // ── the smithy (11 x 9, open-fronted workshop) ───────────────────────────

    static List<Resident> smithy(Plot p) {
        Style st = DARK;
        int top = 5;
        site(p, top + 6, Blocks.COBBLESTONE);
        p.fill(1, 0, 1, p.w - 2, 0, p.d - 2, Blocks.COBBLESTONE);
        stoneWalls(p, 2);
        walls(p, top, st);
        // an open front: only the posts stay
        for (int x = 1; x <= 9; x++) for (int y = 1; y <= top - 1; y++) if (x != 5) p.set(x, y, 0, Blocks.AIR);
        for (int y = 1; y <= top - 1; y++) p.set(5, y, 0, log(st.frame(), Direction.Axis.Y));
        roof(p, top, st);
        window(p, 0, 3, 3, 1);
        window(p, 10, 3, 3, 1);
        // forge in the back left: brick hearth with fire, chimney through the roof
        p.fill(1, 1, 6, 3, 2, 7, Blocks.BRICKS);
        p.set(2, 1, 6, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        p.set(2, 2, 6, Blocks.AIR);
        p.set(2, 3, 6, Blocks.BRICKS);
        for (int y = 3; y <= top + 8; y++) p.set(2, y, 7, Blocks.BRICKS);
        p.set(2, top + 9, 7, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        p.set(4, 1, 7, facing(Blocks.BLAST_FURNACE, NORTH));
        p.set(5, 1, 7, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        p.set(3, 1, 3, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, EAST));
        p.set(6, 1, 6, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR).setValue(GrindstoneBlock.FACING, NORTH));
        p.set(8, 1, 7, Blocks.SMITHING_TABLE);
        chest(p, 9, 1, 7, NORTH, "smith");
        p.set(9, 1, 6, facing(Blocks.BARREL, UP));
        p.set(9, 1, 5, Blocks.STONECUTTER.defaultBlockState().setValue(StonecutterBlock.FACING, WEST));
        p.set(9, 1, 1, Blocks.FLETCHING_TABLE);
        for (int x = 1; x <= 9; x++) {
            p.set(x, top, 4, log(st.beam(), Direction.Axis.X));
            p.set(x, top, 1, log(st.beam(), Direction.Axis.X));
        }
        p.set(5, top - 1, 4, lantern(true));
        p.set(5, top - 1, 1, lantern(true));
        // a log pile and some iron outside the open front
        p.set(1, 1, -1, log(Blocks.OAK_LOG, Direction.Axis.X));
        p.set(2, 1, -1, log(Blocks.OAK_LOG, Direction.Axis.X));
        p.set(1, 2, -1, log(Blocks.OAK_LOG, Direction.Axis.X));
        // one smith runs the forge
        return List.of(new Resident(new BlockPos(4, 1, 4), "weaponsmith"));
    }

    // ── the chapel (11 x 17) with a bell tower over the front ─────────────────

    static List<Resident> chapel(Plot p) {
        Style st = SLATE;
        int top = 7;
        site(p, top + 16, Blocks.POLISHED_ANDESITE);
        stoneWalls(p, top);
        roof(p, top, new Style(st.roofStairs(), st.roofSlab(), Blocks.DARK_OAK_PLANKS, Blocks.STONE_BRICKS, Blocks.STONE_BRICKS,
                Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, 1, Walls.SOLID));
        // tall stained windows along the sides, a round one over the altar
        for (int z = 6; z <= 14; z += 3) {
            for (int x : new int[]{0, 10})
                for (int y = 2; y <= 5; y++) p.set(x, y, z, pane(y == 5 ? Blocks.YELLOW_STAINED_GLASS_PANE : Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Direction.Axis.Z));
        }
        for (int x = 4; x <= 6; x++) for (int y = 4; y <= 6; y++)
            if (!(x != 5 && y != 5)) p.set(x, y, 16, pane(Blocks.ORANGE_STAINED_GLASS_PANE, Direction.Axis.X));
        // buttresses
        for (int z = 4; z <= 16; z += 4) for (int y = 1; y <= 3; y++) {
            p.set(-1, y, z, y == 3 ? stairs(Blocks.STONE_BRICK_STAIRS, EAST) : Blocks.STONE_BRICKS.defaultBlockState());
            p.set(11, y, z, y == 3 ? stairs(Blocks.STONE_BRICK_STAIRS, WEST) : Blocks.STONE_BRICKS.defaultBlockState());
        }
        // pews either side of a red aisle, the altar at the back
        p.fill(4, 1, 1, 6, 1, 14, Blocks.RED_CARPET);
        for (int z = 6; z <= 12; z += 2)
            for (int x = 1; x <= 9; x++) if (x < 4 || x > 6) p.set(x, 1, z, stairs(Blocks.DARK_OAK_STAIRS, NORTH));
        p.fill(3, 0, 14, 7, 0, 15, Blocks.POLISHED_DIORITE);
        for (int x = 4; x <= 6; x++) p.set(x, 1, 15, Blocks.CHISELED_STONE_BRICKS);
        p.set(4, 2, 15, candles(3));
        p.set(6, 2, 15, candles(3));
        p.set(5, 2, 15, Blocks.POTTED_WHITE_TULIP);
        p.set(5, 1, 13, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, NORTH));
        p.set(2, 1, 15, Blocks.BREWING_STAND);
        p.set(8, 1, 15, facing(Blocks.CHEST, NORTH));
        p.canvas.lootLater(p.pos(8, 1, 15), "temple");
        for (int z = 5; z <= 13; z += 4) {
            for (int x = 1; x <= 9; x++) p.set(x, top, z, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
            p.set(5, top - 1, z, Blocks.CHAIN);
            p.set(5, top - 2, z, lantern(true));
        }
        // the front: a double door
        door(p, 5, 0, NORTH, Blocks.DARK_OAK_DOOR);
        p.set(4, 3, 0, stairsTop(Blocks.STONE_BRICK_STAIRS, EAST));
        p.set(6, 3, 0, stairsTop(Blocks.STONE_BRICK_STAIRS, WEST));
        // the bell tower over the entrance: up through the roof, open belfry, pointed cap
        int tTop = top + 9;
        for (int x = 3; x <= 7; x++)
            for (int z = -1; z <= 3; z++)
                for (int y = top + 1; y <= tTop; y++) {
                    boolean wall = x == 3 || x == 7 || z == -1 || z == 3;
                    // belfry openings to the front and the sides only: the back one looked into the nave's roof space
                    boolean belfry = y >= tTop - 3 && y <= tTop - 1 && ((x == 5 && z == -1) || (z == 1 && (x == 3 || x == 7)));
                    p.set(x, y, z, wall && !belfry ? stoneMix(p, x, y, z).defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
        for (int x = 3; x <= 7; x++) for (int y = 1; y <= top; y++) p.set(x, y, -1, stoneMix(p, x, y, -1));
        p.set(5, 1, -1, Blocks.AIR);
        p.set(5, 2, -1, Blocks.AIR);
        p.set(5, 1, 0, Blocks.AIR);
        p.set(5, 2, 0, Blocks.AIR);
        door(p, 5, -1, NORTH, Blocks.DARK_OAK_DOOR);
        p.fill(4, top + 1, 0, 6, top + 1, 2, Blocks.STONE_BRICKS);   // belfry floor
        p.set(5, tTop - 1, 1, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING).setValue(BellBlock.FACING, NORTH));
        p.fill(4, tTop, 0, 6, tTop, 2, Blocks.STONE_BRICKS);
        // a cornice of upturned stairs, a pinnacle at each corner, and a tall solid spire in the middle
        for (int x = 2; x <= 8; x++)
            for (int z = -2; z <= 4; z++) {
                boolean ring = x == 2 || x == 8 || z == -2 || z == 4;
                if (!ring) continue;
                Direction f = x == 2 ? EAST : x == 8 ? WEST : z == -2 ? SOUTH : NORTH;
                p.set(x, tTop, z, stairsTop(Blocks.STONE_BRICK_STAIRS, f.getOpposite()));
            }
        for (int[] c : new int[][]{{3, -1}, {7, -1}, {3, 3}, {7, 3}}) {
            p.set(c[0], tTop + 1, c[1], Blocks.STONE_BRICK_WALL);
            p.set(c[0], tTop + 2, c[1], Blocks.STONE_BRICK_WALL);
            p.set(c[0], tTop + 3, c[1], Blocks.LANTERN);
        }
        for (int i = 0; i <= 1; i++) {
            int y = tTop + 1 + i * 2;
            int r = 1 - i;                                            // a 3x3 then a 1x1 layer, each two blocks tall
            for (int x = 5 - r; x <= 5 + r; x++)
                for (int z = 1 - r; z <= 1 + r; z++) {
                    boolean edge = x == 5 - r || x == 5 + r || z == 1 - r || z == 1 + r;
                    p.set(x, y, z, Blocks.DEEPSLATE_TILES);
                    if (edge && r > 0) {
                        Direction f = x == 5 - r ? EAST : x == 5 + r ? WEST : z == 1 - r ? SOUTH : NORTH;
                        p.set(x, y + 1, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, f));
                    } else p.set(x, y + 1, z, Blocks.DEEPSLATE_TILES);
                }
        }
        p.set(5, tTop + 5, 1, Blocks.DEEPSLATE_TILE_WALL);
        p.set(5, tTop + 6, 1, Blocks.DEEPSLATE_TILE_WALL);
        p.set(5, tTop + 7, 1, Blocks.LIGHTNING_ROD);
        return List.of(new Resident(new BlockPos(5, 1, 11), "cleric"), new Resident(new BlockPos(2, 1, 8), ""));
    }

    // ── the library (11 x 9) ─────────────────────────────────────────────────

    static List<Resident> library(Plot p) {
        Style st = SPRUCE;
        int top = 6;
        site(p, top + 7, Blocks.DARK_OAK_PLANKS);
        walls(p, top, st);
        roof(p, top, st);
        door(p, 5, 0, NORTH, Blocks.SPRUCE_DOOR);
        for (int x : new int[]{3, 7}) window(p, x, 2, 0, 3);
        for (int z : new int[]{2, 6}) { window(p, 0, 3, z, 2); window(p, 10, 3, z, 2); }
        // shelves along the walls
        for (int z = 1; z <= 7; z++) for (int y = 1; y <= 3; y++) {
            if (z != 2 && z != 6 || y < 3) { p.set(1, y, z, Blocks.BOOKSHELF); p.set(9, y, z, Blocks.BOOKSHELF); }
        }
        for (int x = 2; x <= 8; x++) for (int y = 1; y <= 3; y++) p.set(x, y, 7, Blocks.BOOKSHELF);
        p.set(5, 1, 7, facing(Blocks.CHISELED_BOOKSHELF, NORTH));
        // reading tables
        for (int x : new int[]{4, 6}) for (int z = 3; z <= 4; z++) p.set(x, 1, z, slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
        p.set(4, 2, 3, candles(2));
        p.set(6, 2, 4, candles(1));
        chair(p, 3, 1, 3, EAST);
        chair(p, 7, 1, 4, WEST);
        p.set(5, 1, 5, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, NORTH));
        p.set(2, 1, 1, Blocks.CARTOGRAPHY_TABLE);
        p.set(8, 1, 1, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, WEST));
        p.fill(3, 1, 1, 7, 1, 2, Blocks.LIGHT_BLUE_CARPET);
        p.set(5, 1, 1, Blocks.AIR);
        // chandelier
        for (int x = 1; x <= 9; x++) p.set(x, top, 4, log(st.beam(), Direction.Axis.X));
        p.set(5, top - 1, 4, Blocks.CHAIN);
        p.set(5, top - 2, 4, lantern(true));
        return List.of(new Resident(new BlockPos(5, 1, 2), "librarian"), new Resident(new BlockPos(3, 1, 5), "cartographer"));
    }

    // ── the barn (13 x 11) with stalls and a hay loft ───────────────────────

    static List<Resident> barn(Plot p) {
        Style st = new Style(Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS,
                Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.COBBLESTONE, 1, Walls.FRAMED);
        int top = 6;
        site(p, top + 8, Blocks.COARSE_DIRT);
        walls(p, top, st);
        roof(p, top, st);
        // the big front opening and a back door to the paddock
        for (int x = 5; x <= 7; x++) for (int y = 1; y <= 3; y++) p.set(x, y, 0, Blocks.AIR);
        for (int y = 1; y <= 2; y++) p.set(6, y, 10, Blocks.AIR);
        p.set(6, 1, 10, Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, NORTH));
        for (int z : new int[]{3, 7}) { window(p, 0, 3, z, 1); window(p, 12, 3, z, 1); }
        // stalls along both sides
        for (int z = 1; z <= 9; z++) {
            boolean divider = z == 1 || z == 4 || z == 7;
            p.set(4, 1, z, divider ? fence(Blocks.SPRUCE_FENCE, z > 1, true, true, true) : Blocks.AIR.defaultBlockState());
            p.set(8, 1, z, divider ? fence(Blocks.SPRUCE_FENCE, z > 1, true, true, true) : Blocks.AIR.defaultBlockState());
            if (divider) for (int x : new int[]{1, 2, 3, 9, 10, 11}) p.set(x, 1, z, fence(Blocks.SPRUCE_FENCE, false, true, false, true));
        }
        for (int z : new int[]{2, 5, 8}) {
            p.set(1, 1, z, Blocks.HAY_BLOCK);
            p.set(11, 1, z, Blocks.COMPOSTER);
            p.set(11, 1, z + 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        }
        // hay loft over the back half
        p.fill(1, 4, 6, 11, 4, 9, Blocks.SPRUCE_PLANKS);
        for (int x = 1; x <= 11; x++) if (x != 6) p.set(x, 5, 6, fence(Blocks.SPRUCE_FENCE, false, x < 11, false, x > 1));
        for (int x = 1; x <= 11; x += 2) for (int z = 8; z <= 9; z++) { p.set(x, 5, z, Blocks.HAY_BLOCK); if (z == 9) p.set(x, 6, z, Blocks.HAY_BLOCK); }
        for (int y = 1; y <= 4; y++) p.set(10, y, 9, facing(Blocks.LADDER, NORTH));
        for (int x = 1; x <= 11; x++) p.set(x, top, 3, log(st.beam(), Direction.Axis.X));
        p.set(6, top - 1, 3, lantern(true));
        return List.of(new Resident(new BlockPos(6, 1, 4), "shepherd"), new Resident(new BlockPos(6, 1, 2), "leatherworker"));
    }

    // ── the watchtower (5 x 5) ───────────────────────────────────────────────

    static List<Resident> tower(Plot p) {
        int top = 11;
        site(p, top + 5, Blocks.SPRUCE_PLANKS);
        stoneWalls(p, top);
        door(p, 2, 0, NORTH, Blocks.SPRUCE_DOOR);
        for (int y = 1; y <= top; y++) p.set(2, y, 3, facing(Blocks.LADDER, NORTH));
        for (int y : new int[]{5, 9}) for (int x : new int[]{0, 4}) p.set(x, y, 2, Blocks.IRON_BARS.defaultBlockState()
                .setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true));
        // battlements on a wider top
        for (int x = -1; x <= 5; x++)
            for (int z = -1; z <= 5; z++) {
                boolean edge = x == -1 || x == 5 || z == -1 || z == 5;
                if (x >= 0 && x <= 4 && z >= 0 && z <= 4 && !edge) {
                    if (x == 0 || x == 4 || z == 0 || z == 4) p.set(x, top + 1, z, Blocks.SPRUCE_PLANKS);
                    continue;
                }
                p.set(x, top + 1, z, stairsTop(Blocks.STONE_BRICK_STAIRS, x == -1 ? EAST : x == 5 ? WEST : z == -1 ? SOUTH : NORTH));
                if (edge) {
                    p.set(x, top + 2, z, Blocks.STONE_BRICKS);
                    if ((x + z) % 2 == 0) p.set(x, top + 3, z, Blocks.STONE_BRICK_WALL);
                }
            }
        p.fill(0, top + 1, 0, 4, top + 1, 4, Blocks.SPRUCE_PLANKS);
        p.set(2, top + 1, 3, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, NORTH));
        p.set(0, top + 2, 0, Blocks.LANTERN.defaultBlockState());
        p.set(4, top + 2, 4, Blocks.LANTERN.defaultBlockState());
        return List.of(new Resident(new BlockPos(2, 1, -2), "golem"));
    }

    // ── open-air pieces ──────────────────────────────────────────────────────

    /** The well in the square: a stone basin of water under a little roof. */
    static void well(Plot p) {
        site(p, 6, Blocks.STONE_BRICKS, false);
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
            boolean rim = x == 0 || z == 0 || x == 4 || z == 4;
            p.set(x, 0, z, Blocks.STONE_BRICKS);
            p.set(x, -1, z, Blocks.STONE_BRICKS);
            p.set(x, 1, z, rim ? Blocks.STONE_BRICK_WALL.defaultBlockState() : Blocks.WATER.defaultBlockState());
            if (!rim) p.set(x, 0, z, Blocks.WATER);
        }
        for (int[] c : new int[][]{{0, 0}, {4, 0}, {0, 4}, {4, 4}})
            for (int y = 2; y <= 3; y++) p.set(c[0], y, c[1], fence(Blocks.DARK_OAK_FENCE, false, false, false, false));
        for (int x = -1; x <= 5; x++) for (int z = -1; z <= 5; z++)
            p.set(x, 4, z, x == -1 || x == 5 || z == -1 || z == 5 ? slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM) : Blocks.DARK_OAK_PLANKS.defaultBlockState());
        p.set(2, 3, 2, Blocks.CHAIN);
        p.set(2, 2, 2, lantern(true));
    }

    /** A market stall: four posts, a striped canopy, a counter with wares. */
    static List<Resident> stall(Plot p, Block wool, Block wool2, Block ware, String job) {
        site(p, 5, Blocks.COBBLESTONE, false);
        for (int[] c : new int[][]{{0, 0}, {3, 0}, {0, 2}, {3, 2}})
            for (int y = 1; y <= 2; y++) p.set(c[0], y, c[1], fence(Blocks.SPRUCE_FENCE, false, false, false, false));
        for (int x = -1; x <= 4; x++) for (int z = -1; z <= 3; z++) p.set(x, 3, z, (x + 10) % 2 == 0 ? wool : wool2);
        for (int x = 1; x <= 2; x++) p.set(x, 1, 0, slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
        p.set(1, 2, 0, Blocks.POTTED_CORNFLOWER);
        p.set(1, 1, 2, facing(Blocks.BARREL, UP));
        p.set(2, 1, 2, ware.defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? facing(ware, NORTH) : ware.defaultBlockState());
        return new ArrayList<>(List.of(new Resident(new BlockPos(1, 1, 1), job)));
    }
}
