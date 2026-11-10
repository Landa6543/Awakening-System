package com.soybe.awakeningsystem.tower;

import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.block.TowerMissionBoardBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * The Great Tower as it stands in the Overworld: a dark stepped tower that narrows as it climbs, with
 * glowing rune windows, floating halo rings, a beacon beam at the summit and, inside, the vestibule
 * with the Altar of Origin and the gate to the first floor.
 *
 * Everything is built in local coordinates with the entrance at local +Z, then turned to face {@code front}.
 */
public final class GreatTowerBuilder {
    private GreatTowerBuilder() {}

    /** Where the important pieces ended up, in world coordinates. */
    public record Layout(BlockPos origin, Direction front, BlockPos altar, BlockPos portal, BlockPos board, BlockPos entrance) {}

    // tiers: radius, height of the tier
    private static final int[] TIER_RADIUS = {19, 16, 13, 10};
    private static final int[] TIER_HEIGHT = {64, 56, 50, 35};
    private static final int SPIRE_HEIGHT = 30;
    private static final int PLAZA_RADIUS = 30;
    private static final int VESTIBULE_CEILING = 24;

    private static final BlockState WALL = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState WALL_DARK = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState TRIM = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState PILLAR = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState FLOOR = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState GLOW = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState RUNE_GLASS = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
    private static final BlockState WINDOW_GLASS = Blocks.PURPLE_STAINED_GLASS.defaultBlockState();

    private static ServerLevel level;
    private static BlockPos o;
    private static Rotation rot;

    public static synchronized Layout build(ServerLevel lvl, BlockPos origin, Direction front) {
        level = lvl;
        o = origin;
        rot = switch (front) {
            case NORTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.CLOCKWISE_90;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        // the whole tower must fit under the build limit
        int room = lvl.getMaxBuildHeight() - 4 - origin.getY();
        int total = 0;
        for (int h : TIER_HEIGHT) total += h;
        total += SPIRE_HEIGHT;
        double scale = Math.min(1.0D, room / (double) total);
        int[] heights = new int[TIER_HEIGHT.length];
        for (int i = 0; i < heights.length; i++) heights[i] = Math.max(16, (int) (TIER_HEIGHT[i] * scale));
        int spire = Math.max(10, (int) (SPIRE_HEIGHT * scale));

        clearSite();
        plaza();
        int base = 1;
        for (int t = 0; t < TIER_RADIUS.length; t++) {
            tier(t, base, heights[t]);
            base += heights[t];
            if (t + 1 < TIER_RADIUS.length) ledge(base, TIER_RADIUS[t], TIER_RADIUS[t + 1]);
            else ledge(base, TIER_RADIUS[t], 0);
            base++;
        }
        // floating rings around the second and third tiers
        halo(heights[0] + heights[1] / 2 + 1, TIER_RADIUS[1] + 6);
        halo(heights[0] + heights[1] + heights[2] / 2 + 3, TIER_RADIUS[2] + 6);
        spire(base, spire);

        plinth();
        vestibule();
        BlockPos altar = altar();
        BlockPos portal = gate();
        BlockPos board = board();
        Layout layout = new Layout(origin, front, altar, portal, board, w(0, 1, TIER_RADIUS[0] + 3));
        level = null;
        return layout;
    }

    /** Rebuilds only the Altar of Origin (for towers raised before the altar changed); returns the crystal. */
    public static synchronized BlockPos rebuildAltar(ServerLevel lvl, BlockPos origin, Direction front) {
        level = lvl;
        o = origin;
        rot = switch (front) {
            case NORTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.CLOCKWISE_90;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        for (int x = -4; x <= 4; x++)
            for (int z = -4; z <= 4; z++)
                for (int y = 1; y <= 6; y++) set(x, y, z, Blocks.AIR.defaultBlockState());
        BlockPos crystal = altar();
        level = null;
        return crystal;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static BlockPos w(int x, int y, int z) {
        return switch (rot) {
            case CLOCKWISE_90 -> o.offset(-z, y, x);
            case CLOCKWISE_180 -> o.offset(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> o.offset(z, y, -x);
            default -> o.offset(x, y, z);
        };
    }

    private static void set(int x, int y, int z, BlockState state) {
        level.setBlock(w(x, y, z), state.rotate(rot), 2);
    }

    private static double dist(int x, int z) {
        return Math.sqrt(x * x + z * z);
    }

    /** Angle in degrees 0..360 of a local column, 0 at the front. */
    private static double angle(int x, int z) {
        return (Math.toDegrees(Math.atan2(x, z)) + 360.0D) % 360.0D;
    }

    // ── outside ──────────────────────────────────────────────────────────────

    private static void clearSite() {
        int r = PLAZA_RADIUS + 2;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = dist(x, z);
                if (d > r) continue;
                // footing down to solid ground, so the plaza never floats over a slope
                for (int y = 0; y >= -14; y--) {
                    BlockPos p = w(x, y, z);
                    BlockState below = level.getBlockState(p);
                    if (y < -2 && below.isSolid() && !below.is(Blocks.WATER)) break;
                    set(x, y, z, y == 0 ? FLOOR : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                }
                for (int y = 1; y <= 40; y++) {
                    BlockPos p = w(x, y, z);
                    if (!level.getBlockState(p).isAir()) set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void plaza() {
        for (int x = -PLAZA_RADIUS; x <= PLAZA_RADIUS; x++) {
            for (int z = -PLAZA_RADIUS; z <= PLAZA_RADIUS; z++) {
                double d = dist(x, z);
                if (d > PLAZA_RADIUS) continue;
                BlockState s = FLOOR;
                if (d > PLAZA_RADIUS - 1.2D) s = TRIM;
                else if (Math.abs(d - 24.0D) < 0.6D) s = TILES;
                else if (Math.abs(d - 22.0D) < 0.5D && ((int) angle(x, z)) % 15 < 3) s = GLOW;
                // a paved road straight out of the door
                if (z > TIER_RADIUS[0] && Math.abs(x) <= 2) s = Math.abs(x) == 2 ? TRIM : TILES;
                set(x, 0, z, s);
            }
        }
    }

    private static void tier(int t, int y0, int height) {
        int r = TIER_RADIUS[t];
        BlockState skin = t % 2 == 0 ? WALL : WALL_DARK;
        for (int x = -r - 1; x <= r + 1; x++) {
            for (int z = -r - 1; z <= r + 1; z++) {
                double d = dist(x, z);
                if (d > r + 0.5D || d < r - 1.6D) continue;
                double a = angle(x, z);
                boolean pilaster = (a % 30.0D) < 2.2D || (a % 30.0D) > 27.8D;
                boolean outer = d > r - 0.6D;
                for (int y = y0; y < y0 + height; y++) {
                    int ty = y - y0;
                    BlockState s = skin;
                    if (pilaster) {
                        s = PILLAR;
                    } else if (ty % 16 == 0) {
                        s = TRIM;                                   // pale bands
                    } else if (t == 0 && ty == 40) {
                        s = outer ? RUNE_GLASS : GLOW;              // glowing rune belt
                    } else {
                        double wa = a % 30.0D;
                        boolean windowCol = wa > 12.0D && wa < 18.0D;
                        int wy = ty % 16;
                        if (windowCol && wy >= 4 && wy <= 12) s = outer ? WINDOW_GLASS : GLOW;
                    }
                    set(x, y, z, s);
                }
                // pilasters stand out of the wall
                if (pilaster && outer) {
                    for (int y = y0; y < y0 + height; y++) {
                        int px = (int) Math.round(x * (r + 1.0D) / Math.max(1.0D, d));
                        int pz = (int) Math.round(z * (r + 1.0D) / Math.max(1.0D, d));
                        set(px, y, pz, y % 16 == 0 ? TRIM : PILLAR);
                    }
                }
            }
        }
        // the inside of the upper tiers is sealed every 16 blocks so nothing looks hollow through a window
        if (t > 0) {
            for (int x = -r + 1; x <= r - 1; x++)
                for (int z = -r + 1; z <= r - 1; z++)
                    if (dist(x, z) < r - 1.5D) set(x, y0, z, TILES);
        }
    }

    /** The flat roof between two tiers, with a parapet and corner lights. */
    private static void ledge(int y, int rOuter, int rInner) {
        for (int x = -rOuter - 1; x <= rOuter + 1; x++) {
            for (int z = -rOuter - 1; z <= rOuter + 1; z++) {
                double d = dist(x, z);
                if (d > rOuter + 1.5D) continue;
                if (d >= rInner - 1.0D || rInner == 0) set(x, y, z, d > rOuter + 0.5D ? TRIM : FLOOR);
                if (d > rOuter + 0.5D) {
                    double a = angle(x, z);
                    boolean post = (a % 30.0D) < 3.0D;
                    set(x, y + 1, z, post ? WALL_DARK : Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
                    if (post) set(x, y + 2, z, Blocks.SOUL_LANTERN.defaultBlockState());
                }
            }
        }
    }

    /** A broad ring floating in the air around the tower: the "this is not a normal building" touch. */
    private static void halo(int y, int r) {
        for (int x = -r - 3; x <= r + 3; x++) {
            for (int z = -r - 3; z <= r + 3; z++) {
                double d = dist(x, z);
                if (d < r - 1.5D || d > r + 1.5D) continue;
                int a = (int) angle(x, z);
                boolean rim = d > r + 0.5D || d < r - 0.5D;
                set(x, y, z, rim ? TRIM : (a % 24 < 4 ? GLOW : RUNE_GLASS));
                if (!rim) {
                    set(x, y + 1, z, Blocks.QUARTZ_SLAB.defaultBlockState());
                    set(x, y - 1, z, Blocks.QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                }
            }
        }
    }

    /** A wide stepped skirt around the foot of the tower, with buttresses. */
    private static void plinth() {
        int r = TIER_RADIUS[0];
        for (int x = -r - 4; x <= r + 4; x++) {
            for (int z = -r - 4; z <= r + 4; z++) {
                double d = dist(x, z);
                if (d <= r - 0.5D || d > r + 3.5D) continue;
                if (z > 0 && Math.abs(x) <= 3) continue;               // keep the doorway clear
                int h = d > r + 2.5D ? 2 : (d > r + 1.5D ? 4 : 6);
                for (int y = 1; y <= h; y++) set(x, y, z, y == h ? TRIM : WALL_DARK);
            }
        }
    }

    /** The narrowing spire with a beacon beam shooting up from the very top. */
    private static void spire(int y0, int height) {
        int r0 = TIER_RADIUS[TIER_RADIUS.length - 1] - 3;
        for (int i = 0; i < height; i++) {
            double r = r0 * (1.0D - i / (double) height) + 0.6D;
            for (int x = -r0; x <= r0; x++) {
                for (int z = -r0; z <= r0; z++) {
                    double d = dist(x, z);
                    if (d > r) continue;
                    boolean edge = d > r - 1.2D;
                    BlockState s = edge ? ((i % 8 == 0) ? TRIM : WALL_DARK) : Blocks.AIR.defaultBlockState();
                    if (edge && i % 8 == 4) s = RUNE_GLASS;
                    set(x, y0 + i, z, s);
                }
            }
        }
        int top = y0 + height;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) set(x, top - 1, z, Blocks.IRON_BLOCK.defaultBlockState());
        set(0, top, 0, Blocks.BEACON.defaultBlockState());
        set(0, top + 1, 0, Blocks.CYAN_STAINED_GLASS.defaultBlockState());
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++)
                if (x != 0 || z != 0) set(x, top, z, Blocks.CRYING_OBSIDIAN.defaultBlockState());
    }

    // ── inside ───────────────────────────────────────────────────────────────

    private static void vestibule() {
        int r = TIER_RADIUS[0] - 2;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = dist(x, z);
                if (d > r + 0.4D) continue;
                // floor with a bright inlaid ring around the altar
                BlockState f = FLOOR;
                if (d < 4.5D) f = TRIM;
                else if (Math.abs(d - 6.0D) < 0.55D) f = (((int) angle(x, z)) % 30 < 15) ? GLOW : TRIM;
                else if (Math.abs(d - 11.0D) < 0.5D) f = TILES;
                set(x, 0, z, f);
                for (int y = 1; y < VESTIBULE_CEILING; y++) set(x, y, z, Blocks.AIR.defaultBlockState());
                set(x, VESTIBULE_CEILING, z, (Math.abs(d - 8.0D) < 0.5D) ? GLOW : TILES);
            }
        }
        // lanterns hanging from the ceiling (the hall itself is left open, without columns)
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(22.5D + i * 45.0D);
            int lx = (int) Math.round(Math.sin(a) * 10.0D), lz = (int) Math.round(Math.cos(a) * 10.0D);
            set(lx, VESTIBULE_CEILING - 1, lz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        // the doorway: 5 wide, 9 tall, pointed
        int rr = TIER_RADIUS[0];
        for (int z = rr - 3; z <= rr + 2; z++) {
            for (int x = -3; x <= 3; x++) {
                for (int y = 1; y <= 11; y++) {
                    boolean opening = Math.abs(x) <= 2 && (y <= 8 || (y == 9 && Math.abs(x) <= 1) || (y == 10 && x == 0));
                    if (opening) set(x, y, z, Blocks.AIR.defaultBlockState());
                    else if (z > rr - 2 && (Math.abs(x) == 3 || y >= 9)) set(x, y, z, y == 11 ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : TRIM);
                }
            }
        }
        for (int x = -2; x <= 2; x++) for (int z = rr - 3; z <= rr + 2; z++) set(x, 0, z, TILES);
    }

    /**
     * The Altar of Origin: a stepped dais, four rune pillars with amethyst on top around it, and in the
     * middle a pedestal holding the floating crystal of measurement.
     */
    /**
     * The Altar of Origin, kept open and flat: a circle of quartz set into the floor with lights flush in it,
     * and in the middle a single slender stand holding the crystal at eye height. Nothing else stands there.
     */
    private static BlockPos altar() {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > 4.4D) continue;
                BlockState f;
                if (d < 1.5D) f = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
                else if (Math.abs(d - 3.0D) < 0.55D) f = (((int) Math.round(Math.toDegrees(Math.atan2(z, x)) + 360)) % 60 < 20) ? GLOW : Blocks.SMOOTH_QUARTZ.defaultBlockState();
                else if (d > 3.9D) f = TRIM;
                else f = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                set(x, 0, z, f);
            }
        }
        set(0, 1, 0, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        set(0, 2, 0, SystemBlocks.POTENTIAL_CRYSTAL.get().defaultBlockState());
        return w(0, 2, 0);
    }

    /** The round gate at the back of the vestibule, facing the door. */
    private static BlockPos gate() {
        int z = -(TIER_RADIUS[0] - 3);
        for (int x = -5; x <= 5; x++) {
            for (int y = 1; y <= 11; y++) {
                double d = Math.sqrt(x * x + (y - 5.5D) * (y - 5.5D));
                if (d > 5.6D) continue;
                if (d > 4.4D) set(x, y, z, ((int) (Math.toDegrees(Math.atan2(x, y - 5.5D)) + 360) % 40 < 8) ? GLOW : Blocks.CRYING_OBSIDIAN.defaultBlockState());
                else set(x, y, z, Blocks.AIR.defaultBlockState());
                set(x, y, z - 1, WALL_DARK);
            }
        }
        // two steps up to it
        for (int x = -3; x <= 3; x++) set(x, 1, z + 1, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        BlockPos p = w(0, 2, z);
        BlockState portal = SystemBlocks.ASCENSION_PORTAL.get().defaultBlockState()
                .setValue(AscensionPortalBlock.FACING, Direction.SOUTH)
                .setValue(AscensionPortalBlock.AXIS, Direction.Axis.X)
                .setValue(AscensionPortalBlock.PORTAL_TYPE, AscensionPortalBlock.PortalType.BASE);
        level.setBlock(p, portal.rotate(rot), 3);
        return p;
    }

    /**
     * The tower's mission board, inside the vestibule on the left of the door: it hangs on a flat stretch of
     * wall built for it (the board is drawn against the block behind it, so it needs a straight wall).
     */
    private static BlockPos board() {
        // outside, beside the road to the door, on a framed stone panel so it does not hang in the air
        int x = -7, z = TIER_RADIUS[0] + 5;
        for (int dx = -3; dx <= 3; dx++) {
            for (int y = 1; y <= 4; y++) {
                boolean frame = Math.abs(dx) == 3 || y == 4;
                set(x + dx, y, z - 1, frame ? TRIM : WALL_DARK);
            }
            set(x + dx, 5, z - 1, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState());
            set(x + dx, 0, z, TILES);
        }
        set(x - 3, 1, z, Blocks.SOUL_LANTERN.defaultBlockState());
        set(x + 3, 1, z, Blocks.SOUL_LANTERN.defaultBlockState());
        BlockPos p = w(x, 1, z);
        level.setBlock(p, SystemBlocks.TOWER_MISSION_BOARD.get().defaultBlockState()
                .setValue(TowerMissionBoardBlock.FACING, Direction.SOUTH).rotate(rot), 3);
        return p;
    }
}
