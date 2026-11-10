package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Elemental Demon King's castle in the middle of the Elemental Floating Castles. It replaces the old
 * black block in the centre: a walled court with four round corner towers, gates towards the four
 * elemental islands, and a keep whose windows glow in each element's colour, a stair inside and the
 * open throne terrace on top where the King waits.
 */
public final class ElementalCastleKeep {
    private ElementalCastleKeep() {}

    private static final int WALL = 12, KEEP = 6, KEEP_H = 22, TERRACE = 8;
    private static final BlockState STONE = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState DARK = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState TRIM = Blocks.GILDED_BLACKSTONE.defaultBlockState();
    private static final BlockState PILLAR = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState ROOF = Blocks.CRIMSON_PLANKS.defaultBlockState();

    /** The courtyard floor: the top of the central island, found below a spot inside the walls. */
    public static int floorY(ServerLevel level, BlockPos center) {
        for (int y = center.getY() + 14; y > center.getY() - 12; y--) {
            BlockPos p = new BlockPos(center.getX() + 9, y, center.getZ() + 9);
            if (!level.getBlockState(p).isAir() && level.getBlockState(p).isSolid()) return y;
        }
        return center.getY();
    }

    /** Where the King stands: the middle of the throne terrace. */
    public static BlockPos throne(ServerLevel level, BlockPos center) {
        return new BlockPos(center.getX(), floorY(level, center) + KEEP_H + 2, center.getZ());
    }

    public static void build(ServerLevel level, BlockPos center) {
        int cx = center.getX(), cz = center.getZ();
        int fy = floorY(level, center);
        // clear the old castle above the courtyard floor, keeping the bridges and the islands
        for (int x = -22; x <= 22; x++)
            for (int z = -22; z <= 22; z++) {
                // outside the walls only from above the bridge railings
                int from = (Math.abs(x) > 16 || Math.abs(z) > 16) ? fy + 4 : fy + 1;
                for (int y = from; y <= fy + 80; y++) {
                    BlockPos p = new BlockPos(cx + x, y, cz + z);
                    if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        // courtyard floor
        for (int x = -WALL; x <= WALL; x++)
            for (int z = -WALL; z <= WALL; z++)
                level.setBlock(new BlockPos(cx + x, fy, cz + z), (Math.abs(x) + Math.abs(z)) % 6 == 0 ? TRIM : DARK, 2);

        curtainWall(level, cx, fy, cz);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) tower(level, cx + sx * WALL, fy, cz + sz * WALL);
        keep(level, cx, fy, cz);
        terrace(level, cx, fy + KEEP_H, cz);
        // the stair comes out through a hatch in the terrace
        set(level, cx + hatch[0], fy + KEEP_H, cz + hatch[1], Blocks.AIR.defaultBlockState());
        set(level, cx + hatch[0], fy + KEEP_H + 1, cz + hatch[1], Blocks.AIR.defaultBlockState());
    }

    private static int[] hatch = {0, 0};

    /** The four elemental islands, 42 blocks out: wind north, frost south, fire east, storm west. */
    public static void decorateIslands(ServerLevel level, BlockPos center) {
        int cx = center.getX(), cz = center.getZ();
        island(level, cx, cz - 42, center.getY(), 0);
        island(level, cx, cz + 42, center.getY(), 1);
        island(level, cx + 42, cz, center.getY(), 2);
        island(level, cx - 42, cz, center.getY(), 3);
    }

    private static void island(ServerLevel level, int ix, int iz, int cy, int element) {
        int fy = cy;
        for (int y = cy + 14; y > cy - 12; y--) {
            BlockPos p = new BlockPos(ix + 6, y, iz + 3);
            if (level.getBlockState(p).isSolid()) { fy = y; break; }
        }
        // clear what stood on the island (keeping its floor), then raise its shrine
        for (int x = -12; x <= 12; x++)
            for (int z = -12; z <= 12; z++)
                if (x * x + z * z <= 144)
                    for (int y = fy + 1; y <= fy + 14; y++) {
                        BlockPos p = new BlockPos(ix + x, y, iz + z);
                        if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
        BlockState body, top, cap, accent;
        switch (element) {
            case 0 -> { body = Blocks.CALCITE.defaultBlockState(); top = Blocks.CYAN_STAINED_GLASS.defaultBlockState(); cap = Blocks.END_ROD.defaultBlockState(); accent = Blocks.WHITE_STAINED_GLASS.defaultBlockState(); }
            case 1 -> { body = Blocks.PACKED_ICE.defaultBlockState(); top = Blocks.BLUE_ICE.defaultBlockState(); cap = Blocks.SNOW_BLOCK.defaultBlockState(); accent = Blocks.POWDER_SNOW_CAULDRON.defaultBlockState(); }
            case 2 -> { body = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(); top = Blocks.MAGMA_BLOCK.defaultBlockState(); cap = Blocks.CAMPFIRE.defaultBlockState(); accent = Blocks.LAVA.defaultBlockState(); }
            default -> { body = Blocks.CUT_COPPER.defaultBlockState(); top = Blocks.PURPLE_STAINED_GLASS.defaultBlockState(); cap = Blocks.LIGHTNING_ROD.defaultBlockState(); accent = Blocks.AMETHYST_BLOCK.defaultBlockState(); }
        }
        // the direction of the bridge to the castle is left open
        int dirX = element == 2 ? -1 : element == 3 ? 1 : 0, dirZ = element == 0 ? 1 : element == 1 ? -1 : 0;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0D + Math.PI / 8.0D;
            int px = (int) Math.round(Math.sin(a) * 9.0D), pz = (int) Math.round(Math.cos(a) * 9.0D);
            if (px * dirX + pz * dirZ > 6) continue;                 // keep the bridge end clear
            int h = 8 + (i % 2) * 3;
            for (int ox = 0; ox <= 1; ox++)
                for (int oz = 0; oz <= 1; oz++)
                    for (int y = 1; y <= h; y++) set(level, ix + px + ox, fy + y, iz + pz + oz, (y == h || y == h / 2) ? top : body);
            set(level, ix + px, fy + h + 1, iz + pz, cap);
            if (element == 1) {
                // icicles hanging around the frost pillars
                set(level, ix + px, fy + h + 2, iz + pz, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.PointedDripstoneBlock.TIP_DIRECTION, Direction.UP));
            }
        }
        // the element itself, set in the floor around the arena
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2.0D + Math.PI / 4.0D;
            int px = (int) Math.round(Math.sin(a) * 6.0D), pz = (int) Math.round(Math.cos(a) * 6.0D);
            set(level, ix + px, fy, iz + pz, accent.is(Blocks.LAVA) || accent.is(Blocks.POWDER_SNOW_CAULDRON) ? body : accent);
            if (accent.is(Blocks.LAVA)) {
                set(level, ix + px, fy, iz + pz, Blocks.LAVA.defaultBlockState());
                set(level, ix + px, fy - 1, iz + pz, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    BlockPos side = new BlockPos(ix + px, fy, iz + pz).relative(d);
                    if (level.getBlockState(side).isAir()) level.setBlock(side, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                }
            } else if (accent.is(Blocks.POWDER_SNOW_CAULDRON)) {
                set(level, ix + px, fy + 1, iz + pz, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
            }
        }
        if (element == 1) {
            // snow drifts on the frost island
            for (int x = -10; x <= 10; x++)
                for (int z = -10; z <= 10; z++)
                    if (x * x + z * z <= 100 && ((x * 31 + z * 17) & 3) == 0 && level.getBlockState(new BlockPos(ix + x, fy + 1, iz + z)).isAir()
                            && level.getBlockState(new BlockPos(ix + x, fy, iz + z)).isSolid())
                        set(level, ix + x, fy + 1, iz + z, Blocks.SNOW.defaultBlockState());
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState s) {
        level.setBlock(new BlockPos(x, y, z), s, 2);
    }

    private static void curtainWall(ServerLevel level, int cx, int fy, int cz) {
        for (int i = -WALL; i <= WALL; i++) {
            for (int side = 0; side < 4; side++) {
                int x = side < 2 ? i : (side == 2 ? -WALL : WALL);
                int z = side < 2 ? (side == 0 ? -WALL : WALL) : i;
                boolean gate = Math.abs(i) <= 1;
                for (int y = 1; y <= 7; y++) {
                    if (gate && y <= 4) continue;
                    set(level, cx + x, fy + y, cz + z, y == 7 ? TRIM : (y == 5 && gate ? TRIM : STONE));
                }
                // crenellations
                if (Math.abs(i) % 2 == 0) set(level, cx + x, fy + 8, cz + z, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
            }
        }
    }

    private static void tower(ServerLevel level, int tx, int fy, int tz) {
        int h = 18;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > 3.2D) continue;
                for (int y = 1; y <= h; y++) {
                    boolean shell = d > 2.0D;
                    BlockState s = shell ? (y % 6 == 0 ? TRIM : STONE) : Blocks.AIR.defaultBlockState();
                    if (shell && y % 6 == 3 && (x == 0 || z == 0)) s = Blocks.RED_STAINED_GLASS.defaultBlockState();
                    set(level, tx + x, fy + y, tz + z, s);
                }
                set(level, tx + x, fy + h + 1, tz + z, d > 2.6D ? Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState() : DARK);
            }
        }
        // pointed roof
        for (int i = 0; i < 9; i++) {
            double r = 3.4D * (1.0D - i / 9.0D);
            for (int x = -4; x <= 4; x++)
                for (int z = -4; z <= 4; z++)
                    if (Math.sqrt(x * x + z * z) <= r) set(level, tx + x, fy + h + 2 + i, tz + z, ROOF);
        }
        set(level, tx, fy + h + 11, tz, Blocks.END_ROD.defaultBlockState());
        set(level, tx, fy + h, tz, Blocks.SHROOMLIGHT.defaultBlockState());
    }

    /** Window colour for each face: wind north, frost south, fire east, storm west (where their islands lie). */
    private static BlockState elementGlass(int x, int z) {
        if (Math.abs(z) >= Math.abs(x)) return z < 0 ? Blocks.CYAN_STAINED_GLASS.defaultBlockState() : Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        return x > 0 ? Blocks.ORANGE_STAINED_GLASS.defaultBlockState() : Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
    }

    private static void keep(ServerLevel level, int cx, int fy, int cz) {
        for (int x = -KEEP; x <= KEEP; x++) {
            for (int z = -KEEP; z <= KEEP; z++) {
                boolean edge = Math.abs(x) == KEEP || Math.abs(z) == KEEP;
                boolean corner = Math.abs(x) == KEEP && Math.abs(z) == KEEP;
                for (int y = 1; y <= KEEP_H; y++) {
                    BlockState s = Blocks.AIR.defaultBlockState();
                    if (edge) {
                        int along = Math.abs(x) == KEEP ? z : x;
                        boolean window = !corner && Math.abs(along) <= 2 && y % 7 >= 2 && y % 7 <= 5;
                        boolean door = Math.abs(along) <= 1 && y <= 4 && z == KEEP;
                        if (door) s = Blocks.AIR.defaultBlockState();
                        else if (corner) s = PILLAR;
                        else if (y % 7 == 0) s = TRIM;
                        else if (window) s = elementGlass(x, z);
                        else s = STONE;
                    }
                    set(level, cx + x, fy + y, cz + z, s);
                }
            }
        }
        // a lit hall inside
        for (int y = 6; y <= KEEP_H; y += 7)
            set(level, cx, fy + y, cz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        for (int y = 6; y <= KEEP_H; y += 7) set(level, cx, fy + y + 1, cz, DARK);
        // a stair climbing along the inner walls up to the terrace
        int[][] ring = innerRing(KEEP - 1);
        int start = 0;
        for (int i = 0; i < ring.length; i++) if (ring[i][0] == 2 && ring[i][1] == KEEP - 1) start = i;
        for (int step = 0; step < KEEP_H - 1; step++) {
            int[] p = ring[(start + step) % ring.length];
            int y = fy + 1 + step;
            for (int under = fy + 1; under < y; under++)
                if (level.getBlockState(new BlockPos(cx + p[0], under, cz + p[1])).isAir()) set(level, cx + p[0], under, cz + p[1], PILLAR);
            set(level, cx + p[0], y, cz + p[1], Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP));
            for (int a = 1; a <= 3; a++) set(level, cx + p[0], y + a, cz + p[1], Blocks.AIR.defaultBlockState());
        }
        hatch = ring[(start + KEEP_H - 2) % ring.length];
    }

    /** The squares of a ring of radius r, walked anticlockwise from the south side. */
    private static int[][] innerRing(int r) {
        java.util.List<int[]> out = new java.util.ArrayList<>();
        for (int x = -r; x < r; x++) out.add(new int[]{x, r});
        for (int z = r; z > -r; z--) out.add(new int[]{r, z});
        for (int x = r; x > -r; x--) out.add(new int[]{x, -r});
        for (int z = -r; z < r; z++) out.add(new int[]{-r, z});
        return out.toArray(new int[0][]);
    }

    private static void terrace(ServerLevel level, int cx, int ty, int cz) {
        for (int x = -TERRACE; x <= TERRACE; x++) {
            for (int z = -TERRACE; z <= TERRACE; z++) {
                boolean rim = Math.abs(x) == TERRACE || Math.abs(z) == TERRACE;
                set(level, cx + x, ty, cz + z, rim ? TRIM : ((x + z) % 2 == 0 ? DARK : STONE));
                if (rim) set(level, cx + x, ty + 1, cz + z, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
                if (rim && Math.abs(x) > KEEP) set(level, cx + x, ty - 1, cz + z, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, x > 0 ? Direction.WEST : Direction.EAST).setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
            }
        }
        // four elemental braziers on the corners
        BlockState[] fires = {Blocks.SOUL_CAMPFIRE.defaultBlockState(), Blocks.CAMPFIRE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState(), Blocks.CRYING_OBSIDIAN.defaultBlockState()};
        int k = 0;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                int x = cx + sx * (TERRACE - 1), z = cz + sz * (TERRACE - 1);
                set(level, x, ty + 1, z, PILLAR);
                set(level, x, ty + 2, z, PILLAR);
                set(level, x, ty + 3, z, fires[k++]);
            }
        }
        // the throne at the back, facing the stair
        int bz = cz - TERRACE + 2;
        for (int x = -2; x <= 2; x++) set(level, cx + x, ty + 1, bz, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState());
        set(level, cx, ty + 1, bz, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        for (int y = 1; y <= 4; y++) set(level, cx, ty + 1 + y, bz - 1, y == 4 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
        set(level, cx - 1, ty + 2, bz, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        set(level, cx + 1, ty + 2, bz, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
    }
}
