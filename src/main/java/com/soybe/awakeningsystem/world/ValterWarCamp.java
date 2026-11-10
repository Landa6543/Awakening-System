package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.Random;

/**
 * Valter's war camp, where Ryu Hanbin waits: a palisade of sharpened logs with a gate and a watchtower,
 * the chieftain's longhouse, a raised dais with a throne in the middle (Ryu Hanbin stands on it), a great
 * bonfire with log seats, a training ground with straw targets and a forge.
 * Local +Z is south, where the gate is. Ryu Hanbin's spot stays at (+2, +2, -1).
 */
public final class ValterWarCamp {
    private ValterWarCamp() {}

    private static final int R = 13;
    private static ServerLevel level;
    private static BlockPos o;

    public static synchronized void build(ServerLevel lvl, BlockPos center) {
        level = lvl;
        o = center;
        Random rnd = new Random(center.asLong());
        ground(rnd);
        palisade();
        watchtower(7, -7);
        longhouse();
        dais();
        bonfire();
        training();
        forge();
        banners();
        level = null;
    }

    private static void set(int x, int y, int z, BlockState s) {
        level.setBlock(o.offset(x, y, z), s, 2);
    }

    private static void ground(Random rnd) {
        for (int x = -R - 1; x <= R + 1; x++) {
            for (int z = -R - 1; z <= R + 1; z++) {
                if (x * x + z * z > (R + 1) * (R + 1)) continue;
                for (int y = -1; y >= -8; y--) {
                    BlockPos p = o.offset(x, y, z);
                    if (level.getBlockState(p).isSolid() && y < -1) break;
                    level.setBlock(p, Blocks.DIRT.defaultBlockState(), 2);
                }
                int r = rnd.nextInt(10);
                set(x, 0, z, r < 4 ? Blocks.COARSE_DIRT.defaultBlockState() : r < 7 ? Blocks.PACKED_MUD.defaultBlockState()
                        : r < 9 ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
                for (int y = 1; y <= 16; y++) {
                    BlockPos p = o.offset(x, y, z);
                    if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    /** A ring of sharpened logs, with a gate to the south flanked by skulls on spikes. */
    private static void palisade() {
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d < R - 0.5D || d > R + 0.5D) continue;
                if (z > 0 && Math.abs(x) <= 2) continue;                       // the gate
                int h = 4 + ((x * 7 + z * 13) & 1);
                for (int y = 1; y <= h; y++) set(x, y, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
                set(x, h + 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());     // the sharpened tip
            }
        }
        for (int sx = -3; sx <= 3; sx += 6) {
            for (int y = 1; y <= 7; y++) set(sx, y, R, Blocks.DARK_OAK_LOG.defaultBlockState());
            set(sx, 8, R, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 0));
            set(sx, 6, R + 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        for (int x = -3; x <= 3; x++) set(x, 7, R, Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        set(0, 6, R, Blocks.RED_WOOL.defaultBlockState());
    }

    private static void watchtower(int tx, int tz) {
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                for (int y = 1; y <= 8; y++) set(tx + sx, y, tz + sz, Blocks.SPRUCE_LOG.defaultBlockState());
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                set(tx + x, 8, tz + z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                if (Math.abs(x) == 2 || Math.abs(z) == 2) set(tx + x, 9, tz + z, Blocks.SPRUCE_FENCE.defaultBlockState());
            }
        }
        for (int y = 1; y < 8; y++) set(tx, y, tz + 1, Blocks.LADDER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
        set(tx, 8, tz + 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState());
        set(tx + 1, 10, tz + 1, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.SIGNAL_FIRE, true));
        set(tx + 1, 9, tz + 1, Blocks.SPRUCE_PLANKS.defaultBlockState());
    }

    /** The chieftain's longhouse along the north side, door to the south. */
    private static void longhouse() {
        int x0 = -7, x1 = 3, z0 = -10, z1 = -5;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                set(x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                for (int y = 1; y <= 4; y++) {
                    BlockState s = Blocks.AIR.defaultBlockState();
                    if (corner) s = Blocks.SPRUCE_LOG.defaultBlockState();
                    else if (edge) s = (y == 2 && (x + z) % 3 == 0) ? Blocks.SPRUCE_FENCE.defaultBlockState() : Blocks.MUD_BRICKS.defaultBlockState();
                    set(x, y, z, s);
                }
            }
        }
        // door
        set(-2, 1, z1, Blocks.AIR.defaultBlockState());
        set(-2, 2, z1, Blocks.AIR.defaultBlockState());
        // a pitched roof of spruce stairs running east-west
        int mid = (z0 + z1) / 2;
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                int dz = z - mid;
                int y = 5 + (3 - Math.min(3, Math.abs(dz)));
                BlockState s = dz == 0
                        ? Blocks.DARK_OAK_PLANKS.defaultBlockState()
                        : Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dz < 0 ? Direction.SOUTH : Direction.NORTH);
                if (Math.abs(dz) <= 3 || z == z0 - 1 || z == z1 + 1) set(x, Math.max(5, y), z, s);
            }
        }
        for (int x = x0; x <= x1; x++) for (int z = z0 + 1; z <= z1 - 1; z++) set(x, 5, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
        // close the gables under the roof
        for (int x : new int[]{x0, x1})
            for (int z = z0; z <= z1; z++)
                for (int y = 5; y < 5 + (3 - Math.min(3, Math.abs(z - mid))); y++) set(x, y, z, Blocks.MUD_BRICKS.defaultBlockState());
        // inside: furs, a long table, a chest
        for (int x = x0 + 2; x <= x1 - 2; x++) set(x, 1, mid, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        set(x0 + 1, 1, z0 + 1, Blocks.BROWN_WOOL.defaultBlockState());
        set(x0 + 1, 1, z0 + 2, Blocks.BROWN_CARPET.defaultBlockState());
        set(x1 - 1, 1, z0 + 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        set(-2, 4, mid, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    /** The raised dais with the throne: Ryu Hanbin stands at (2, 2, -1). */
    private static void dais() {
        for (int x = 0; x <= 4; x++) {
            for (int z = -3; z <= 1; z++) {
                boolean edge = x == 0 || x == 4 || z == -3 || z == 1;
                set(x, 1, z, edge ? Blocks.MUD_BRICKS.defaultBlockState() : Blocks.PACKED_MUD.defaultBlockState());
            }
        }
        for (int x = 1; x <= 3; x++) set(x, 1, 2, Blocks.MUD_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        set(2, 2, -3, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        set(2, 3, -3, Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(1, 2, -3, Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(3, 2, -3, Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(2, 4, -3, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 8));
        set(0, 2, -3, Blocks.RED_WOOL.defaultBlockState());
        set(4, 2, -3, Blocks.RED_WOOL.defaultBlockState());
    }

    /** A great bonfire in a stone ring, with log seats around it. */
    private static void bonfire() {
        int bx = 2, bz = 6;
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                if (Math.abs(x) == 2 || Math.abs(z) == 2) set(bx + x, 0, bz + z, Blocks.COBBLESTONE.defaultBlockState());
        set(bx, 1, bz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.SIGNAL_FIRE, false));
        set(bx, 0, bz, Blocks.MAGMA_BLOCK.defaultBlockState());
        BlockState seat = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "campfire_log_seat")).defaultBlockState();
        boolean modSeat = !seat.isAir();
        int[][] spots = {{-3, 0, 1}, {3, 0, 3}, {0, -3, 0}, {0, 3, 2}};
        for (int[] s : spots) {
            BlockState b = modSeat && seat.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)
                    ? seat.setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.from2DDataValue(s[2]))
                    : Blocks.STRIPPED_OAK_LOG.defaultBlockState();
            set(bx + s[0], 1, bz + s[1], b);
        }
    }

    /** Straw targets on posts to the west. */
    private static void training() {
        for (int i = 0; i < 3; i++) {
            int x = -10, z = -2 + i * 4;
            set(x, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(x, 2, z, Blocks.HAY_BLOCK.defaultBlockState());
            set(x, 3, z, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(net.minecraft.world.level.block.CarvedPumpkinBlock.FACING, Direction.EAST));
            set(x + 1, 0, z, Blocks.PACKED_MUD.defaultBlockState());
        }
        set(-7, 1, 3, Blocks.HAY_BLOCK.defaultBlockState());
        set(-7, 2, 3, Blocks.HAY_BLOCK.defaultBlockState());
        set(-6, 1, 3, Blocks.HAY_BLOCK.defaultBlockState());
    }

    /** The forge under a lean-to on the east side. */
    private static void forge() {
        int fx = 8, fz = 2;
        for (int x = -1; x <= 2; x++)
            for (int z = -2; z <= 2; z++) set(fx + x, 0, fz + z, Blocks.COBBLESTONE.defaultBlockState());
        for (int z = -2; z <= 2; z += 4) for (int y = 1; y <= 3; y++) set(fx - 1, y, fz + z, Blocks.SPRUCE_LOG.defaultBlockState());
        for (int x = -1; x <= 2; x++)
            for (int z = -2; z <= 2; z++) set(fx + x, 4, fz + z, Blocks.SPRUCE_SLAB.defaultBlockState());
        set(fx + 2, 1, fz - 1, Blocks.BLAST_FURNACE.defaultBlockState().setValue(BlastFurnaceBlock.FACING, Direction.WEST));
        set(fx + 2, 1, fz, Blocks.LAVA_CAULDRON.defaultBlockState());
        set(fx + 2, 1, fz + 1, Blocks.SMOKER.defaultBlockState().setValue(net.minecraft.world.level.block.SmokerBlock.FACING, Direction.WEST));
        set(fx, 1, fz, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.NORTH));
        set(fx, 1, fz + 2, Blocks.GRINDSTONE.defaultBlockState());
        set(fx + 1, 1, fz - 2, Blocks.SMITHING_TABLE.defaultBlockState());
        set(fx, 3, fz - 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    /** Red war banners on tall poles around the court. */
    private static void banners() {
        int[][] poles = {{-5, -3}, {7, -4}, {-4, 9}, {9, 8}};
        for (int[] p : poles) {
            for (int y = 1; y <= 5; y++) set(p[0], y, p[1], Blocks.DARK_OAK_FENCE.defaultBlockState());
            set(p[0], 6, p[1], Blocks.RED_WOOL.defaultBlockState());
            set(p[0], 6, p[1] + 1, Blocks.RED_WALL_BANNER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.SOUTH));
        }
    }
}
