package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TeleportHelper {

    private TeleportHelper() {}

    /**
     * Finds a safe position for the entity to teleport to, avoiding solid walls, ceilings, lava, and barriers.
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

        if (!below.isSolid() || below.is(Blocks.BARRIER) || below.is(Blocks.LAVA)) return false;

        if (!feet.getCollisionShape(level, pos).isEmpty() || feet.is(Blocks.BARRIER)) return false;
        if (!head.getCollisionShape(level, pos.above()).isEmpty() || head.is(Blocks.BARRIER)) return false;

        if (entity != null) {
            // Prevent teleporting across magical barrier walls (line-of-sight barrier check)
            Vec3 start = entity.position().add(0, entity.getEyeHeight() * 0.5D, 0);
            Vec3 end = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            BlockHitResult hit = level.clip(new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    entity
            ));
            if (hit.getType() != HitResult.Type.MISS) {
                BlockState hitState = level.getBlockState(hit.getBlockPos());
                if (hitState.is(Blocks.BARRIER)) {
                    return false;
                }
            }

            double dx = (pos.getX() + 0.5D) - entity.getX();
            double dy = pos.getY() - entity.getY();
            double dz = (pos.getZ() + 0.5D) - entity.getZ();
            return level.noCollision(entity, entity.getBoundingBox().move(dx, dy, dz));
        }

        return true;
    }
}
