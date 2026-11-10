package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * The Elemental Demon King's sky citadel, built from scratch: a floating island of dark rock carrying a
 * walled castle (four round towers, four gatehouses, a courtyard with an elemental compass, a tall keep
 * and the open throne terrace on top), joined by stone bridges to four elemental islands, each with its own
 * ground and shrine (wind north, frost south, fire east, storm west), 42 blocks out.
 */
public final class ElementalSkyCitadel {
    private ElementalSkyCitadel() {}

    private static final int ISLAND_R = 24, WALL = 15, KEEP = 7, KEEP_H = 30, TERRACE = 10, SIDE_R = 13, SIDE_D = 42;

    private static ServerLevel level;
    private static int cx, cy, cz;

    private static final BlockState STONE = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState STONE_CRACKED = Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState TRIM = Blocks.GILDED_BLACKSTONE.defaultBlockState();
    private static final BlockState PILLAR = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState TILE = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState ROOF = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /** Where the Elemental Demon King stands: the middle of the throne terrace. */
    public static BlockPos throne(BlockPos center) {
        return new BlockPos(center.getX(), center.getY() + KEEP_H + 2, center.getZ());
    }

    public static synchronized void build(ServerLevel lvl, BlockPos center) {
        level = lvl;
        cx = center.getX();
        cy = center.getY();
        cz = center.getZ();
        clear();
        island(0, 0, ISLAND_R, 22, Theme.CORE);
        island(0, -SIDE_D, SIDE_R, 14, Theme.WIND);
        island(0, SIDE_D, SIDE_R, 14, Theme.FROST);
        island(SIDE_D, 0, SIDE_R, 14, Theme.FIRE);
        island(-SIDE_D, 0, SIDE_R, 14, Theme.STORM);
        for (Direction d : Direction.Plane.HORIZONTAL) bridge(d);
        courtyard();
        curtainWall();
        for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) tower(sx * WALL, sz * WALL);
        for (Direction d : Direction.Plane.HORIZONTAL) gatehouse(d);
        keep();
        terrace();
        shrine(0, -SIDE_D, Theme.WIND);
        shrine(0, SIDE_D, Theme.FROST);
        shrine(SIDE_D, 0, Theme.FIRE);
        shrine(-SIDE_D, 0, Theme.STORM);
        level = null;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void set(int x, int y, int z, BlockState s) {
        level.setBlock(new BlockPos(cx + x, cy + y, cz + z), s, 2);
    }

    private static BlockState get(int x, int y, int z) {
        return level.getBlockState(new BlockPos(cx + x, cy + y, cz + z));
    }

    /** A stable pseudo-random value 0..1 for a column. */
    private static double noise(int x, int z, int salt) {
        long h = (x * 73856093L) ^ (z * 19349663L) ^ (salt * 83492791L) ^ (cx * 31L + cz);
        h = (h ^ (h >>> 13)) * 0x5bd1e995L;
        return ((h ^ (h >>> 15)) & 0xFFFF) / 65535.0D;
    }

    private static void clear() {
        for (int x = -60; x <= 60; x++)
            for (int z = -60; z <= 60; z++)
                for (int y = -26; y <= 64; y++) {
                    BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                    if (!level.getBlockState(p).isAir()) level.setBlock(p, AIR, 2);
                }
    }

    // ── islands ──────────────────────────────────────────────────────────────

    private enum Theme { CORE, WIND, FROST, FIRE, STORM }

    /** An upside-down cone of rock with a ragged edge, its top flush with the citadel's ground level. */
    private static void island(int ox, int oz, int r, int depth, Theme t) {
        for (int x = -r - 3; x <= r + 3; x++) {
            for (int z = -r - 3; z <= r + 3; z++) {
                double d = Math.sqrt(x * x + z * z);
                double edge = r + (noise(ox + x, oz + z, 1) - 0.5D) * 3.0D;
                if (d > edge) continue;
                double f = d / edge;
                int bottom = (int) Math.round(depth * Math.pow(1.0D - f, 0.75D) + noise(ox + x, oz + z, 2) * 3.0D);
                for (int y = 0; y >= -bottom; y--) {
                    BlockState s = y == 0 ? top(t, ox + x, oz + z, d, r) : (y > -3 ? sub(t) : under(t, ox + x, oz + z, y));
                    set(ox + x, y, oz + z, s);
                }
                // roots, chains and drips hanging under the rock
                if (noise(ox + x, oz + z, 3) < 0.04D && f < 0.85D) {
                    int len = 2 + (int) (noise(ox + x, oz + z, 4) * 5);
                    for (int i = 1; i <= len; i++) set(ox + x, -bottom - i, oz + z, hanging(t, i == len));
                }
            }
        }
    }

    private static BlockState top(Theme t, int x, int z, double d, int r) {
        double n = noise(x, z, 5);
        return switch (t) {
            case CORE -> d > r - 2 ? (n < 0.5 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState()) : TILE;
            case WIND -> n < 0.55 ? Blocks.MOSS_BLOCK.defaultBlockState() : (n < 0.85 ? Blocks.CALCITE.defaultBlockState() : Blocks.WHITE_CONCRETE_POWDER.defaultBlockState());
            case FROST -> n < 0.7 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
            case FIRE -> n < 0.45 ? Blocks.BASALT.defaultBlockState() : (n < 0.8 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState());
            case STORM -> n < 0.5 ? Blocks.TUFF.defaultBlockState() : (n < 0.8 ? Blocks.POLISHED_TUFF.defaultBlockState() : Blocks.OXIDIZED_COPPER.defaultBlockState());
        };
    }

    private static BlockState sub(Theme t) {
        return switch (t) {
            case CORE -> Blocks.DEEPSLATE.defaultBlockState();
            case WIND -> Blocks.CALCITE.defaultBlockState();
            case FROST -> Blocks.PACKED_ICE.defaultBlockState();
            case FIRE -> Blocks.BLACKSTONE.defaultBlockState();
            case STORM -> Blocks.TUFF.defaultBlockState();
        };
    }

    private static BlockState under(Theme t, int x, int z, int y) {
        double n = noise(x * 3 + y, z * 5 - y, 6);
        if (n < 0.06) {
            return switch (t) {
                case CORE -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                case WIND -> Blocks.SEA_LANTERN.defaultBlockState();
                case FROST -> Blocks.BLUE_ICE.defaultBlockState();
                case FIRE -> Blocks.MAGMA_BLOCK.defaultBlockState();
                case STORM -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            };
        }
        return switch (t) {
            case CORE -> n < 0.5 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
            case WIND -> n < 0.6 ? Blocks.STONE.defaultBlockState() : Blocks.DIORITE.defaultBlockState();
            case FROST -> n < 0.6 ? Blocks.STONE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
            case FIRE -> n < 0.6 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
            case STORM -> n < 0.6 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
        };
    }

    private static BlockState hanging(Theme t, boolean end) {
        return switch (t) {
            case CORE -> end ? Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true) : Blocks.CHAIN.defaultBlockState();
            case WIND -> Blocks.VINE.defaultBlockState();
            case FROST -> Blocks.PACKED_ICE.defaultBlockState();
            case FIRE -> end ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.WEEPING_VINES_PLANT.defaultBlockState();
            case STORM -> end ? Blocks.AMETHYST_CLUSTER.defaultBlockState() : Blocks.CHAIN.defaultBlockState();
        };
    }

    /** A five-wide stone bridge with railings and lanterns between the citadel and an island. */
    private static void bridge(Direction d) {
        int from = ISLAND_R - 3, to = SIDE_D - SIDE_R + 3;
        for (int i = from; i <= to; i++) {
            int bx = d.getStepX() * i, bz = d.getStepZ() * i;
            int px = d.getStepZ() != 0 ? 1 : 0, pz = d.getStepX() != 0 ? 1 : 0;
            for (int w = -3; w <= 3; w++) {
                int x = bx + px * w, z = bz + pz * w;
                if (Math.abs(w) <= 2) {
                    set(x, 0, z, Math.abs(w) == 2 ? STONE : TILE);
                    set(x, -1, z, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                    for (int y = 1; y <= 4; y++) if (get(x, y, z).isSolid()) set(x, y, z, AIR);
                } else {
                    boolean post = (i - from) % 5 == 0;
                    set(x, 0, z, STONE);
                    set(x, 1, z, post ? PILLAR : Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
                    if (post) {
                        set(x, 2, z, PILLAR);
                        set(x, 3, z, Blocks.SOUL_LANTERN.defaultBlockState());
                    }
                }
            }
        }
    }

    // ── the castle ───────────────────────────────────────────────────────────

    private static void courtyard() {
        for (int x = -WALL + 1; x <= WALL - 1; x++) {
            for (int z = -WALL + 1; z <= WALL - 1; z++) {
                BlockState s = (Math.abs(x) + Math.abs(z)) % 8 == 0 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : TILE;
                // the elemental compass: one coloured arm towards each island
                if (Math.abs(x) <= 1 && z < -KEEP - 1) s = Blocks.CYAN_TERRACOTTA.defaultBlockState();
                if (Math.abs(x) <= 1 && z > KEEP + 1) s = Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
                if (Math.abs(z) <= 1 && x > KEEP + 1) s = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
                if (Math.abs(z) <= 1 && x < -KEEP - 1) s = Blocks.PURPLE_TERRACOTTA.defaultBlockState();
                set(x, 0, z, s);
            }
        }
        // braziers in the four corners of the court
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                set(sx * 10, 1, sz * 10, PILLAR);
                set(sx * 10, 2, sz * 10, Blocks.SOUL_CAMPFIRE.defaultBlockState());
            }
    }

    private static void curtainWall() {
        for (int i = -WALL; i <= WALL; i++) {
            for (int side = 0; side < 4; side++) {
                int x = side < 2 ? i : (side == 2 ? -WALL : WALL);
                int z = side < 2 ? (side == 0 ? -WALL : WALL) : i;
                for (int y = 1; y <= 9; y++) {
                    BlockState s = y == 9 ? TRIM : ((y + i) % 7 == 0 ? STONE_CRACKED : STONE);
                    if (y == 5 && Math.abs(i) % 6 == 3) s = Blocks.RED_STAINED_GLASS.defaultBlockState();
                    set(x, y, z, s);
                }
                // walkway behind the battlements and the merlons
                int ix = side < 2 ? i : (side == 2 ? -WALL + 1 : WALL - 1);
                int iz = side < 2 ? (side == 0 ? -WALL + 1 : WALL - 1) : i;
                if (Math.abs(ix) < WALL && Math.abs(iz) < WALL) set(ix, 9, iz, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                set(x, 10, z, Math.abs(i) % 2 == 0 ? STONE : Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState());
                // buttresses on the outside
                if (Math.abs(i) % 6 == 0 && Math.abs(i) < WALL) {
                    int bx = side < 2 ? i : (side == 2 ? -WALL - 1 : WALL + 1);
                    int bz = side < 2 ? (side == 0 ? -WALL - 1 : WALL + 1) : i;
                    for (int y = 1; y <= 6; y++) set(bx, y, bz, PILLAR);
                    set(bx, 7, bz, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, side == 0 ? Direction.SOUTH : side == 1 ? Direction.NORTH : side == 2 ? Direction.EAST : Direction.WEST));
                }
            }
        }
    }

    private static void tower(int tx, int tz) {
        int h = 22;
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > 4.3D) continue;
                boolean shell = d > 3.2D;
                for (int y = 0; y <= h; y++) {
                    BlockState s;
                    if (y == 0 || y == 10 || y == 20) s = shell ? TRIM : TILE;
                    else if (shell) s = (y % 10 == 5 && (x == 0 || z == 0)) ? Blocks.RED_STAINED_GLASS.defaultBlockState() : STONE;
                    else s = AIR;
                    set(tx + x, y, tz + z, s);
                }
                if (d > 3.5D) set(tx + x, h + 1, tz + z, ((int) Math.round(Math.toDegrees(Math.atan2(z, x)) / 30.0D)) % 2 == 0 ? STONE : AIR);
            }
        }
        // a tall pointed roof
        for (int i = 0; i < 13; i++) {
            double r = 4.8D * (1.0D - i / 13.0D);
            for (int x = -5; x <= 5; x++)
                for (int z = -5; z <= 5; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d <= r && d > r - 1.3D) set(tx + x, h + 2 + i, tz + z, i % 4 == 3 ? TRIM : ROOF);
                }
        }
        set(tx, h + 15, tz, Blocks.LIGHTNING_ROD.defaultBlockState());
        set(tx, h + 1, tz, Blocks.SHROOMLIGHT.defaultBlockState());
        // a ladder inside
        for (int y = 1; y < h; y++) set(tx, y, tz + 2, Blocks.LADDER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
    }

    /** A gatehouse over the wall where each bridge arrives, the portcullis drawn up. */
    private static void gatehouse(Direction d) {
        int ox = d.getStepX() * WALL, oz = d.getStepZ() * WALL;
        int px = d.getStepZ() != 0 ? 1 : 0, pz = d.getStepX() != 0 ? 1 : 0;
        for (int w = -4; w <= 4; w++) {
            for (int depth = -1; depth <= 1; depth++) {
                int x = ox + px * w + d.getStepX() * depth, z = oz + pz * w + d.getStepZ() * depth;
                for (int y = 1; y <= 13; y++) {
                    boolean opening = Math.abs(w) <= 2 && (y <= 5 || (y == 6 && Math.abs(w) <= 1));
                    BlockState s = opening ? AIR : (y == 13 ? TRIM : (Math.abs(w) == 4 ? PILLAR : STONE));
                    if (!opening && y == 9 && Math.abs(w) <= 1 && depth == 1) s = Blocks.RED_STAINED_GLASS.defaultBlockState();
                    set(x, y, z, s);
                }
                set(x, 14, z, Math.abs(w) % 2 == 0 ? STONE : AIR);
            }
            // the raised portcullis
            if (Math.abs(w) <= 2) set(ox + px * w, 6, oz + pz * w, Blocks.IRON_BARS.defaultBlockState());
        }
        int lx = ox + d.getStepX() * 2, lz = oz + d.getStepZ() * 2;
        set(lx + px * 3, 4, lz + pz * 3, Blocks.SOUL_LANTERN.defaultBlockState());
        set(lx - px * 3, 4, lz - pz * 3, Blocks.SOUL_LANTERN.defaultBlockState());
    }

    /** Window colour for each face of the keep: the element whose island lies that way. */
    private static BlockState elementGlass(int x, int z) {
        if (Math.abs(z) >= Math.abs(x)) return z < 0 ? Blocks.CYAN_STAINED_GLASS.defaultBlockState() : Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        return x > 0 ? Blocks.ORANGE_STAINED_GLASS.defaultBlockState() : Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
    }

    private static void keep() {
        for (int x = -KEEP; x <= KEEP; x++) {
            for (int z = -KEEP; z <= KEEP; z++) {
                boolean edge = Math.abs(x) == KEEP || Math.abs(z) == KEEP;
                boolean corner = Math.abs(x) == KEEP && Math.abs(z) == KEEP;
                for (int y = 1; y <= KEEP_H; y++) {
                    BlockState s = AIR;
                    if (edge) {
                        int along = Math.abs(x) == KEEP ? z : x;
                        boolean window = !corner && Math.abs(along) <= 2 && y % 10 >= 3 && y % 10 <= 8 && y > 2;
                        boolean door = Math.abs(along) <= 1 && y <= 4 && z == KEEP;
                        if (door) s = AIR;
                        else if (corner) s = PILLAR;
                        else if (y % 10 == 0) s = TRIM;
                        else if (window) s = elementGlass(x, z);
                        else s = STONE;
                    } else if (y % 10 == 0 && y < KEEP_H) {
                        s = TILE;                                                      // floors inside
                    }
                    set(x, y, z, s);
                }
                // buttresses at the corners, stepping out
                if (corner) for (int y = 1; y <= KEEP_H - 4; y++) {
                    set(x + Integer.signum(x), y, z, PILLAR);
                    set(x, y, z + Integer.signum(z), PILLAR);
                }
            }
        }
        // a lit hall on each storey
        for (int y = 9; y < KEEP_H; y += 10) set(0, y, 0, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // a stair winding up the inside of the walls through every floor
        int r = KEEP - 1;
        java.util.List<int[]> ring = new java.util.ArrayList<>();
        for (int x = -r; x < r; x++) ring.add(new int[]{x, r});
        for (int z = r; z > -r; z--) ring.add(new int[]{r, z});
        for (int x = r; x > -r; x--) ring.add(new int[]{x, -r});
        for (int z = -r; z < r; z++) ring.add(new int[]{-r, z});
        int start = 0;
        for (int i = 0; i < ring.size(); i++) if (ring.get(i)[0] == 2 && ring.get(i)[1] == r) start = i;
        for (int step = 0; step < KEEP_H - 1; step++) {
            int[] p = ring.get((start + step) % ring.size());
            int y = 1 + step;
            set(p[0], y, p[1], Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
            for (int a = 1; a <= 3; a++) set(p[0], y + a, p[1], AIR);
            if (y > 1 && get(p[0], y - 1, p[1]).isAir()) set(p[0], y - 1, p[1], PILLAR);
        }
        hatch = ring.get((start + KEEP_H - 2) % ring.size());
    }

    private static int[] hatch = {0, 0};

    /** The open throne terrace on top of the keep, wider than the keep, where the King waits. */
    private static void terrace() {
        int ty = KEEP_H;
        for (int x = -TERRACE; x <= TERRACE; x++) {
            for (int z = -TERRACE; z <= TERRACE; z++) {
                boolean rim = Math.abs(x) == TERRACE || Math.abs(z) == TERRACE;
                double d = Math.sqrt(x * x + z * z);
                BlockState f = rim ? TRIM : (Math.abs(d - 6.0D) < 0.6D ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : ((x + z) % 2 == 0 ? TILE : STONE));
                set(x, ty, z, f);
                if (rim) set(x, ty + 1, z, (Math.abs(x) + Math.abs(z)) % 4 == 0 ? PILLAR : Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
                // corbels under the overhang
                if (Math.abs(x) > KEEP || Math.abs(z) > KEEP)
                    set(x, ty - 1, z, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
            }
        }
        set(hatch[0], ty, hatch[1], AIR);
        set(hatch[0], ty + 1, hatch[1], AIR);
        // four crystal pillars, one per element, on the corners
        BlockState[] crystals = {Blocks.CYAN_STAINED_GLASS.defaultBlockState(), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
                Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), Blocks.PURPLE_STAINED_GLASS.defaultBlockState()};
        int[][] corners = {{-TERRACE + 2, -TERRACE + 2}, {TERRACE - 2, TERRACE - 2}, {TERRACE - 2, -TERRACE + 2}, {-TERRACE + 2, TERRACE - 2}};
        for (int i = 0; i < 4; i++) {
            int x = corners[i][0], z = corners[i][1];
            set(x, ty + 1, z, TRIM);
            for (int y = 2; y <= 5; y++) set(x, ty + y, z, crystals[i]);
            set(x, ty + 6, z, Blocks.END_ROD.defaultBlockState());
            set(x, ty + 3, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // the great throne against the north edge, facing the stair
        int bz = -TERRACE + 3;
        for (int x = -2; x <= 2; x++) {
            set(x, ty + 1, bz, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState());
            set(x, ty + 1, bz - 1, STONE);
        }
        set(0, ty + 1, bz, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        for (int y = 2; y <= 6; y++) set(0, ty + y, bz - 1, y == 6 ? TRIM : Blocks.CRYING_OBSIDIAN.defaultBlockState());
        for (int s = -1; s <= 1; s += 2) {
            set(s, ty + 2, bz, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
            for (int y = 2; y <= 4; y++) set(s * 2, ty + y, bz - 1, y == 4 ? TRIM : PILLAR);
        }
    }

    // ── the elemental shrines ────────────────────────────────────────────────

    private static void shrine(int ox, int oz, Theme t) {
        // the arena ring in the middle of the island
        for (int x = -9; x <= 9; x++) {
            for (int z = -9; z <= 9; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > 9.3D) continue;
                BlockState f = switch (t) {
                    case WIND -> Math.abs(d - 7.5D) < 0.6D ? Blocks.CYAN_TERRACOTTA.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState();
                    case FROST -> Math.abs(d - 7.5D) < 0.6D ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
                    case FIRE -> Math.abs(d - 7.5D) < 0.6D ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.POLISHED_BLACKSTONE.defaultBlockState();
                    default -> Math.abs(d - 7.5D) < 0.6D ? Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState() : Blocks.POLISHED_TUFF.defaultBlockState();
                };
                set(ox + x, 0, oz + z, f);
            }
        }
        // eight themed monoliths around the arena, the side facing the bridge left open
        int toX = -Integer.signum(ox), toZ = -Integer.signum(oz);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0D + Math.PI / 8.0D;
            int px = (int) Math.round(Math.sin(a) * 10.5D), pz = (int) Math.round(Math.cos(a) * 10.5D);
            if (px * toX + pz * toZ > 7) continue;
            int h = 6 + (i % 2) * 4;
            for (int y = 1; y <= h; y++) {
                for (int ax = 0; ax <= 1; ax++)
                    for (int az = 0; az <= 1; az++) set(ox + px + ax, y, oz + pz + az, monolith(t, y, h));
            }
            set(ox + px, h + 1, oz + pz, crown(t));
        }
        // a few touches of the element on the ground beyond the arena
        for (int i = 0; i < 10; i++) {
            double a = noise(ox + i, oz - i, 9) * Math.PI * 2.0D;
            int rr = 10 + (int) (noise(ox - i, oz + i, 10) * 2);
            int x = ox + (int) Math.round(Math.sin(a) * rr), z = oz + (int) Math.round(Math.cos(a) * rr);
            switch (t) {
                case WIND -> { set(x, 1, z, Blocks.AZALEA.defaultBlockState()); }
                case FROST -> { set(x, 1, z, Blocks.PACKED_ICE.defaultBlockState()); set(x, 2, z, Blocks.PACKED_ICE.defaultBlockState()); set(x, 3, z, Blocks.ICE.defaultBlockState()); }
                case FIRE -> { set(x, 0, z, Blocks.LAVA.defaultBlockState()); }
                default -> { set(x, 1, z, Blocks.AMETHYST_CLUSTER.defaultBlockState()); }
            }
        }
        if (t == Theme.WIND) {
            // stones floating in the wind above the island
            for (int i = 0; i < 6; i++) {
                int x = ox + (int) ((noise(i, ox, 11) - 0.5D) * 22), z = oz + (int) ((noise(oz, i, 12) - 0.5D) * 22), y = 9 + (int) (noise(i, i, 13) * 8);
                set(x, y, z, Blocks.CALCITE.defaultBlockState());
                set(x + 1, y, z, Blocks.MOSS_BLOCK.defaultBlockState());
                set(x, y - 1, z, Blocks.CALCITE.defaultBlockState());
            }
        }
    }

    private static BlockState monolith(Theme t, int y, int h) {
        return switch (t) {
            case WIND -> y % 3 == 0 ? Blocks.CYAN_STAINED_GLASS.defaultBlockState() : Blocks.CALCITE.defaultBlockState();
            case FROST -> y > h - 2 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
            case FIRE -> y % 4 == 2 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.POLISHED_BASALT.defaultBlockState();
            default -> y % 3 == 1 ? Blocks.WAXED_OXIDIZED_COPPER.defaultBlockState() : Blocks.WAXED_CUT_COPPER.defaultBlockState();
        };
    }

    private static BlockState crown(Theme t) {
        return switch (t) {
            case WIND -> Blocks.END_ROD.defaultBlockState();
            case FROST -> Blocks.SNOW_BLOCK.defaultBlockState();
            case FIRE -> Blocks.CAMPFIRE.defaultBlockState();
            default -> Blocks.LIGHTNING_ROD.defaultBlockState();
        };
    }
}
