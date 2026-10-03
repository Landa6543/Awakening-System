package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class TeleportHelper {

    private TeleportHelper() {}

    /**
     * Finds a safe position for the entity to teleport to, avoiding solid walls, ceilings, and lava.
     * If candidate is unsafe, searches neighboring offsets. If none are safe, falls back to fallbackPos.
     */
    public static Vec3 findSafeTeleportPos(Level level, Entity entity, double targetX, double targetY, double targetZ, Vec3 fallbackPos) {
        BlockPos base = BlockPos.containing(targetX, targetY, targetZ);

        if (isSafe(level, entity, base)) {
            return new Vec3(base.getX() + 0.5D, base.getY(), base.getZ() + 0.5D);
        }

        int[] yOffsets = {0, 1, -1, 2, -2};
        int[][] ringOffsets = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {2, 0}, {-2, 0}, {0, 2}, {0, -2}
        };

        for (int dy : yOffsets) {
            for (int[] offset : ringOffsets) {
                BlockPos candidate = base.offset(offset[0], dy, offset[1]);
                if (isSafe(level, entity, candidate)) {
                    return new Vec3(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
                }
            }
        }

        if (fallbackPos != null) {
            BlockPos fb = BlockPos.containing(fallbackPos);
            if (isSafe(level, entity, fb)) {
                return new Vec3(fb.getX() + 0.5D, fb.getY(), fb.getZ() + 0.5D);
            }
            return fallbackPos;
        }

        return new Vec3(targetX, targetY, targetZ);
    }

    private static boolean isSafe(Level level, Entity entity, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        // Floor must be solid and not hazardous
        if (!below.isSolid() || below.is(Blocks.BARRIER) || below.is(Blocks.LAVA)) return false;

        // Feet and head must not have solid collision shapes
        if (!feet.getCollisionShape(level, pos).isEmpty()) return false;
        if (!head.getCollisionShape(level, pos.above()).isEmpty()) return false;

        // Verify entity bounding box collision
        if (entity != null) {
            double dx = (pos.getX() + 0.5D) - entity.getX();
            double dy = pos.getY() - entity.getY();
            double dz = (pos.getZ() + 0.5D) - entity.getZ();
            return level.noCollision(entity, entity.getBoundingBox().move(dx, dy, dz));
        }

        return true;
    }
}
