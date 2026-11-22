package com.soybe.awakeningsystem.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Where a player can be put down: two free blocks with solid ground under them. A wanted spot that is inside
 * the ground (or the trees) moves up out of it; one hanging over nothing drops to the ground below.
 */
public final class SafeSpot {
    private SafeSpot() {}

    private static boolean free(ServerLevel level, BlockPos p) {
        var s = level.getBlockState(p);
        return !s.blocksMotion() && s.getFluidState().isEmpty();
    }

    private static boolean ground(ServerLevel level, BlockPos p) {
        var s = level.getBlockState(p);
        return s.blocksMotion() && !s.is(BlockTags.LEAVES);
    }

    public static Vec3 near(ServerLevel level, Vec3 wanted) {
        BlockPos p = BlockPos.containing(wanted);
        level.getChunk(p);
        // up out of the ground, a block at a time
        for (int i = 0; i < 64; i++) {
            BlockPos q = p.above(i);
            if (free(level, q) && free(level, q.above()) && ground(level, q.below()))
                return new Vec3(wanted.x, q.getY(), wanted.z);
            if (free(level, q) && free(level, q.above())) {
                // in the open: down to the first ground under it (not far)
                for (int d = 1; d <= 12; d++) {
                    BlockPos r = q.below(d);
                    if (ground(level, r) && free(level, r.above()) && free(level, r.above(2)))
                        return new Vec3(wanted.x, r.getY() + 1, wanted.z);
                }
                break;
            }
        }
        // nothing near: the ground of that column (leaves left out), whatever its height
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
        return new Vec3(wanted.x, y, wanted.z);
    }
}
