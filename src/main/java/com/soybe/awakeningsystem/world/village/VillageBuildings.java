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

    record Style(Block roofStairs, Block roofSlab, Block roofFull, Block floor, Block frame, Block beam, Block plaster) {}

    static final Style SPRUCE = new Style(Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.WHITE_TERRACOTTA);
    static final Style DARK = new Style(Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_PLANKS, Blocks.OAK_PLANKS,
            Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.WHITE_TERRACOTTA);
    static final Style BRICK = new Style(Blocks.BRICK_STAIRS, Blocks.BRICK_SLAB, Blocks.BRICKS, Blocks.SPRUCE_PLANKS,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.WHITE_TERRACOTTA);
    static final Style SLATE = new Style(Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES, Blocks.SPRUCE_PLANKS,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.WHITE_TERRACOTTA);

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
        for (int x = 0; x < p.w; x++)
            for (int z = 0; z < p.d; z++) {
                boolean edgeX = x == 0 || x == p.w - 1, edgeZ = z == 0 || z == p.d - 1;
                if (!edgeX && !edgeZ) continue;
                boolean corner = edgeX && edgeZ;
                boolean post = corner || (edgeZ && x % 4 == 0) || (edgeX && z % 4 == 0);
                for (int y = 1; y <= top; y++) {
                    BlockState s;
                    boolean beam = y == top;
                    for (int b : beams) beam |= y == b;
                    if (post) s = log(st.frame(), Direction.Axis.Y);
                    else if (beam) s = log(st.beam(), edgeZ ? Direction.Axis.X : Direction.Axis.Z);
                    else if (y == 1) s = cobbleMix(p, x, y, z).defaultBlockState();
                    else s = st.plaster().defaultBlockState();
                    p.set(x, y, z, s);
                }
            }
    }

    /** Stone walls (for the chapel and the tower). */
    static void stoneWalls(Plot p, int top) {
        for (int x = 0; x < p.w; x++)
            for (int z = 0; z < p.d; z++) {
                if (x != 0 && z != 0 && x != p.w - 1 && z != p.d - 1) continue;
                for (int y = 1; y <= top; y++) p.set(x, y, z, stoneMix(p, x, y, z));
            }
    }

    /** A gable roof with its ridge running front to back, a block of overhang all round, and filled gables. */
    static void roof(Plot p, int top, Style st) {
        int w = p.w;
        for (int i = 0; ; i++) {
            int l = -1 + i, r = w - i, y = top + 1 + i;
            if (l > r) break;
            for (int z = -1; z <= p.d; z++) {
                if (l == r) {
                    p.set(l, y, z, slab(st.roofSlab(), SlabType.BOTTOM));
                } else {
                    p.set(l, y, z, stairs(st.roofStairs(), EAST));
                    p.set(r, y, z, stairs(st.roofStairs(), WEST));
                    if (i > 0 && (z == -1 || z == p.d)) {
                        // the underside of the overhang at the gable ends
                        p.set(l, y - 1, z, stairsTop(st.roofStairs(), WEST));
                        p.set(r, y - 1, z, stairsTop(st.roofStairs(), EAST));
                    }
                }
            }
            // the gable triangles
            for (int x = l; x <= r; x++) {
                if (y - 1 <= top) continue;
                for (int z : new int[]{0, p.d - 1}) p.set(x, y - 1, z, x == w / 2 ? log(st.frame(), Direction.Axis.Y) : st.plaster().defaultBlockState());
            }
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
        BlockState lower = door.defaultBlockState().setValue(DoorBlock.FACING, face).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT).setValue(DoorBlock.OPEN, false);
        p.set(x, 1, z, lower);
        p.set(x, 2, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        int ox = x + face.getStepX(), oz = z + face.getStepZ();
        p.set(ox, 0, oz, Blocks.STONE_BRICKS);
        p.set(ox, 1, oz, Blocks.AIR);
        p.set(ox, 2, oz, Blocks.AIR);
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

    static List<Resident> smallHouse(Plot p, Style st, String job, Block workstation) {
        int top = 4;
        site(p, top + 6, st.floor());
        walls(p, top, st);
        roof(p, top, st);
        door(p, 4, 0, NORTH, Blocks.SPRUCE_DOOR);
        window(p, 2, 2, 0, 2);
        window(p, 6, 2, 0, 2);
        window(p, 0, 2, 3, 2);
        window(p, 8, 2, 3, 2);
        window(p, 4, 2, 6, 2);
        // tie beam with a lantern
        for (int x = 1; x <= 7; x++) p.set(x, top, 3, log(st.beam(), Direction.Axis.X));
        p.set(4, top - 1, 3, lantern(true));
        // bed and chest in the back left corner
        bed(p, 1, 1, 4, SOUTH, Blocks.RED_BED);
        chest(p, 2, 1, 5, NORTH, "house");
        p.set(1, 1, 1, Blocks.BOOKSHELF);
        p.set(1, 2, 1, Blocks.POTTED_POPPY);
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
        for (int x = 3; x <= 5; x++) p.set(x, 1, 4, Blocks.RED_CARPET);
        return List.of(new Resident(new BlockPos(4, 1, 2), job));
    }

    // ── the two-storey house (9 x 9) ─────────────────────────────────────────

    static List<Resident> tallHouse(Plot p, Style st, String job, Block workstation) {
        int mid = 4, top = 8;
        site(p, top + 6, st.floor());
        walls(p, top, st, mid);
        roof(p, top, st);
        // upper floor with a hole for the ladder
        p.fill(1, mid, 1, p.w - 2, mid, p.d - 2, st.floor());
        p.set(7, mid, 7, Blocks.AIR);
        for (int y = 1; y <= mid; y++) p.set(7, y, 7, facing(Blocks.LADDER, NORTH));
        door(p, 4, 0, NORTH, Blocks.DARK_OAK_DOOR);
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
        bed(p, 1, mid + 1, 6, SOUTH, Blocks.BLUE_BED);
        bed(p, 3, mid + 1, 6, SOUTH, Blocks.BLUE_BED);
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
        return List.of(new Resident(new BlockPos(6, 1, 8), "cleric"), new Resident(new BlockPos(5, 1, 5), ""), new Resident(new BlockPos(9, 1, 5), ""),
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
        return List.of(new Resident(new BlockPos(4, 1, 4), "weaponsmith"), new Resident(new BlockPos(7, 1, 4), "toolsmith"),
                new Resident(new BlockPos(5, 1, 2), "armorer"), new Resident(new BlockPos(8, 1, 2), "mason"), new Resident(new BlockPos(8, 1, -2), "fletcher"));
    }

    // ── the chapel (11 x 17) with a bell tower over the front ─────────────────

    static List<Resident> chapel(Plot p) {
        Style st = SLATE;
        int top = 7;
        site(p, top + 16, Blocks.POLISHED_ANDESITE);
        stoneWalls(p, top);
        roof(p, top, new Style(st.roofStairs(), st.roofSlab(), st.roofFull(), st.floor(), Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.STONE_BRICKS));
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
                    boolean belfry = y >= tTop - 3 && y <= tTop - 1 && (x == 5 || z == 1);
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
        for (int i = 0; i <= 2; i++) {
            int y = tTop + 1 + i;
            for (int x = 3 + i; x <= 7 - i; x++)
                for (int z = -1 + i; z <= 3 - i; z++) {
                    boolean edge = x == 3 + i || x == 7 - i || z == -1 + i || z == 3 - i;
                    if (!edge) continue;
                    Direction f = x == 3 + i ? EAST : x == 7 - i ? WEST : z == -1 + i ? SOUTH : NORTH;
                    p.set(x, y, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, f));
                }
        }
        p.set(5, tTop + 3, 1, Blocks.DEEPSLATE_TILE_WALL);
        p.set(5, tTop + 4, 1, Blocks.LIGHTNING_ROD);
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
        Style st = new Style(Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS,
                Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS);
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
        site(p, 6, Blocks.STONE_BRICKS);
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
        site(p, 5, Blocks.COBBLESTONE);
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
