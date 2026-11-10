package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

/**
 * Turns the Ancestral Immortal Grotto from rooms standing in a rectangular hole into two domed caverns
 * of living rock (jade veins, moss, dripstone, jade lights), and digs a stepped tunnel up to the surface
 * with a shrine gate, since the grotto had no way in. Pedestals, the sealed gate, the chest and the
 * Jade Guardian keep their places, so the Wu Xing puzzle works as before.
 */
public final class ImmortalGrottoShell {
    private ImmortalGrottoShell() {}

    private static final int HALL_R = 12, HALL_H = 10, SANCTUM_R = 9, SANCTUM_H = 9, SANCTUM_Z = 23;

    public static void reshape(ServerLevel level, BlockPos center) {
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        Random rnd = new Random(center.asLong() * 31L + 7L);

        // 1. rock around and over the two caverns, carved as domes
        for (int x = -15; x <= 15; x++) {
            for (int z = -35; z <= 36; z++) {
                for (int y = -1; y <= 16; y++) {
                    BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                    boolean inside = interior(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (y <= 0 && interior(x, 1, z) && s.isAir()) {
                        // a floor under carved passages
                        level.setBlock(p, y == 0 ? Blocks.DEEPSLATE_TILES.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    } else if (inside) {
                        // the old flat ceiling and straight walls go; floor, pedestals, the sealed gate and decoration stay
                        boolean gate = Math.abs(x) <= 2 && z >= 12 && z <= 14 && y <= 5;
                        boolean natural = isNatural(s);
                        if (!gate && (natural || (isOldShell(s) && (y >= 7 || roomDistance(x, z) >= 7.5D))))
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    } else if (y >= 1 && (s.isAir() || isOldShell(s))) {
                        level.setBlock(p, rock(rnd, y), 2);
                    }
                }
            }
        }
        // 2. dress the cavern surfaces
        for (int x = -15; x <= 15; x++) {
            for (int z = -35; z <= 36; z++) {
                for (int y = 1; y <= 15; y++) {
                    if (!interior(x, y, z)) continue;
                    BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                    if (!level.getBlockState(p).isAir()) continue;
                    BlockPos up = p.above();
                    if (!interior(x, y + 1, z) && isRock(level.getBlockState(up))) {
                        int roll = rnd.nextInt(100);
                        if (roll < 6) level.setBlock(up, Blocks.OCHRE_FROGLIGHT.defaultBlockState(), 2);       // warm jade lamp
                        else if (roll < 12) level.setBlock(up, Blocks.VERDANT_FROGLIGHT.defaultBlockState(), 2);
                        else if (roll < 22 && interior(x, y - 2, z) && level.getBlockState(p.below()).isAir()) stalactite(level, p);
                        else if (roll < 34) level.setBlock(up, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    }
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        BlockPos side = p.relative(d);
                        if (isRock(level.getBlockState(side)) && rnd.nextInt(100) < 9)
                            level.setBlock(side, rnd.nextBoolean() ? Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState(), 2);
                    }
                    // moss carpets around the edge of the floor
                    if (y == 1 && level.getBlockState(p.below()).is(Blocks.GILDED_BLACKSTONE) && rnd.nextInt(3) == 0)
                        level.setBlock(p, Blocks.MOSS_CARPET.defaultBlockState(), 2);
                }
            }
        }
        // hanging jade lanterns in the hall
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3.0D;
            int x = (int) Math.round(Math.sin(a) * 8.0D), z = (int) Math.round(Math.cos(a) * 8.0D);
            hangLantern(level, cx + x, cy, cz + z);
        }
        stairToSurface(level, cx, cy, cz);
    }

    /** Inside one of the caverns, the passage between them or the entrance corridor (local coordinates). */
    private static boolean interior(int x, int y, int z) {
        if (y < 1) return false;
        if (dome(x, y, z, HALL_R, HALL_H)) return true;
        if (dome(x, y, z - SANCTUM_Z, SANCTUM_R, SANCTUM_H)) return true;
        if (Math.abs(x) <= 2 && y <= 5 && z >= HALL_R - 2 && z <= SANCTUM_Z - SANCTUM_R + 2) return true;   // sealed gate passage
        return Math.abs(x) <= 2 && y <= 5 && z >= -29 && z <= -HALL_R + 2;                                   // entrance corridor
    }

    /** Horizontal distance to the centre of the nearer room (the corridors count as far away). */
    private static double roomDistance(int x, int z) {
        double hall = Math.sqrt(x * x + z * z), sanctum = Math.sqrt(x * x + (z - SANCTUM_Z) * (z - SANCTUM_Z));
        if (z < -HALL_R + 2 || (z > HALL_R - 1 && z < SANCTUM_Z - SANCTUM_R + 1)) return 99.0D;
        return Math.min(hall, sanctum);
    }

    private static boolean dome(int x, int y, int z, int r, int h) {
        double horiz = (x * x + z * z) / (double) (r * r);
        double vert = (y - 1) / (double) h;
        return horiz + vert * vert <= 1.0D;
    }

    private static boolean isOldShell(BlockState s) {
        return s.is(Blocks.DEEPSLATE_BRICKS) || s.is(Blocks.CHISELED_DEEPSLATE) || s.is(Blocks.POLISHED_DEEPSLATE)
                || s.is(Blocks.DARK_PRISMARINE) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.DIRT) || s.is(Blocks.GRAVEL)
                || s.is(Blocks.WATER) || s.is(Blocks.LAVA);
    }

    /** Ground the template left in place (it does not write its air): stone, ores, dirt, water... */
    private static boolean isNatural(BlockState s) {
        return s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.DIRT) || s.is(Blocks.GRAVEL) || s.is(Blocks.SAND)
                || s.is(Blocks.CLAY) || s.is(Blocks.WATER) || s.is(Blocks.LAVA) || s.is(BlockTags.COAL_ORES) || s.is(BlockTags.IRON_ORES)
                || s.is(BlockTags.COPPER_ORES) || s.is(BlockTags.GOLD_ORES) || s.is(BlockTags.REDSTONE_ORES) || s.is(BlockTags.LAPIS_ORES)
                || s.is(BlockTags.DIAMOND_ORES) || s.is(BlockTags.EMERALD_ORES) || s.is(Blocks.CALCITE) || s.is(Blocks.SMOOTH_BASALT)
                || s.is(Blocks.AMETHYST_BLOCK) || s.is(Blocks.BUDDING_AMETHYST) || s.is(Blocks.GLOW_LICHEN) || s.is(Blocks.MOSS_BLOCK);
    }

    private static boolean isRock(BlockState s) {
        return s.is(Blocks.DEEPSLATE) || s.is(Blocks.TUFF) || s.is(Blocks.COBBLED_DEEPSLATE) || s.is(Blocks.MOSSY_COBBLESTONE)
                || s.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static BlockState rock(Random rnd, int y) {
        int r = rnd.nextInt(100);
        if (r < 55) return Blocks.DEEPSLATE.defaultBlockState();
        if (r < 75) return Blocks.TUFF.defaultBlockState();
        if (r < 90) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    }

    private static void stalactite(ServerLevel level, BlockPos p) {
        level.setBlock(p, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.BASE), 2);
        level.setBlock(p.below(), Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP), 2);
    }

    private static void hangLantern(ServerLevel level, int x, int floorY, int z) {
        for (int y = floorY + 3; y < floorY + 16; y++) {
            BlockPos p = new BlockPos(x, y, z);
            if (!level.getBlockState(p.above()).isAir()) {
                level.setBlock(p, Blocks.CHAIN.defaultBlockState(), 2);
                level.setBlock(p.below(), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
                return;
            }
        }
    }

    /** A 3-wide stepped tunnel north from the entrance corridor up to the surface, crowned by a shrine gate. */
    private static void stairToSurface(ServerLevel level, int cx, int cy, int cz) {
        int z = cz - 30;
        int y = cy;
        BlockState step = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        for (int i = 0; i < 90; i++, z--) {
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cx, z) - 1;
            if (y >= surface) break;
            y++;
            for (int dx = -2; dx <= 2; dx++) {
                boolean wall = Math.abs(dx) == 2;
                for (int h = -1; h <= 5; h++) {
                    BlockPos p = new BlockPos(cx + dx, y + h, z);
                    BlockState s;
                    if (wall || h == 5) s = (h == 5 && i % 6 == 0) ? Blocks.VERDANT_FROGLIGHT.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                    else if (h == -1) s = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                    else if (h == 0) s = step;
                    else s = Blocks.AIR.defaultBlockState();
                    level.setBlock(p, s, 2);
                }
            }
        }
        // the shrine gate where the stair comes out
        int gy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cx, z);
        for (int dx = -3; dx <= 3; dx += 6)
            for (int h = 0; h <= 5; h++) level.setBlock(new BlockPos(cx + dx, gy + h, z), Blocks.STRIPPED_CRIMSON_HYPHAE.defaultBlockState(), 2);
        for (int dx = -4; dx <= 4; dx++) {
            level.setBlock(new BlockPos(cx + dx, gy + 6, z), Blocks.DARK_PRISMARINE.defaultBlockState(), 2);
            level.setBlock(new BlockPos(cx + dx, gy + 7, z), Blocks.DARK_PRISMARINE_SLAB.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(cx - 5, gy + 6, z), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(new BlockPos(cx + 5, gy + 6, z), Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        level.setBlock(new BlockPos(cx, gy + 5, z), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        for (int dx = -2; dx <= 2; dx++)
            for (int h = 0; h <= 4; h++) level.setBlock(new BlockPos(cx + dx, gy + h, z), Blocks.AIR.defaultBlockState(), 2);
    }
}
