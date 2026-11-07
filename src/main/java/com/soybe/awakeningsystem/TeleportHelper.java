package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TeleportHelper {

    private TeleportHelper() {}

    /**
     * Clamps a teleport destination vector strictly within arena bounds (Ranker Tower, Dungeons, Pagoda).
     * If outside the inner room radius or ceiling, places the entity safely at the boundary inside the floor.
     */
    public static Vec3 clampToArenaBounds(Level level, Entity entity, double targetX, double targetY, double targetZ) {
        if (level == null || entity == null) {
            return new Vec3(targetX, targetY, targetZ);
        }

        // 1. Ranker Tower Chamber Bounding
        if (level.dimension().equals(TowerManager.RANKER_TOWER) && entity instanceof Player player) {
            var session = TowerManager.getSession(player.getUUID());
            if (session != null && session.floorOrigin != null) {
                double centerX = session.floorOrigin.getX() + 0.5D;
                double centerZ = session.floorOrigin.getZ() + 0.5D;
                double dx = targetX - centerX;
                double dz = targetZ - centerZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                double maxInnerRadius = TowerManager.ARENA_RADIUS - 2.5D; // Inner limit inside walls

                if (dist > maxInnerRadius && dist > 0.001D) {
                    dx = (dx / dist) * maxInnerRadius;
                    dz = (dz / dist) * maxInnerRadius;
                    targetX = centerX + dx;
                    targetZ = centerZ + dz;
                }

                double minY = session.floorOrigin.getY() + 1.0D;
                double maxY = session.floorOrigin.getY() + TowerManager.CHAMBER_HEIGHT - 1.0D; // Strictly below ceiling/roof
                targetY = Math.max(minY, Math.min(maxY, targetY));
            }
        }

        // 2. Dungeon Realm Room Bounding
        if (level.dimension().equals(DungeonManager.DUNGEON_REALM) && entity instanceof Player player) {
            var session = DungeonManager.getSession(player.getUUID());
            if (session != null && session.arenaCenter != null) {
                BlockPos roomCenter = DungeonManager.getCurrentRoomCenter(session);
                if (roomCenter == null) roomCenter = session.arenaCenter;

                double centerX = roomCenter.getX() + 0.5D;
                double centerZ = roomCenter.getZ() + 0.5D;
                double dx = targetX - centerX;
                double dz = targetZ - centerZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                double maxRadius = 15.0D;

                if (dist > maxRadius && dist > 0.001D) {
                    dx = (dx / dist) * maxRadius;
                    dz = (dz / dist) * maxRadius;
                    targetX = centerX + dx;
                    targetZ = centerZ + dz;
                }

                double minY = roomCenter.getY() + 1.0D;
                double maxY = roomCenter.getY() + 12.0D;
                targetY = Math.max(minY, Math.min(maxY, targetY));
            }
        }

        // 3. Pagoda Realm Bounding
        if (level.dimension().equals(PagodaTrialManager.PAGODA_REALM) && entity instanceof Player player) {
            var session = PagodaTrialManager.getSession(player.getUUID());
            if (session != null && session.stageOrigin != null) {
                double centerX = session.stageOrigin.getX() + 0.5D;
                double centerZ = session.stageOrigin.getZ() + 0.5D;
                double dx = targetX - centerX;
                double dz = targetZ - centerZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                double maxRadius = 13.5D;

                if (dist > maxRadius && dist > 0.001D) {
                    dx = (dx / dist) * maxRadius;
                    dz = (dz / dist) * maxRadius;
                    targetX = centerX + dx;
                    targetZ = centerZ + dz;
                }

                double minY = session.stageOrigin.getY() + 1.0D;
                double maxY = session.stageOrigin.getY() + 10.0D;
                targetY = Math.max(minY, Math.min(maxY, targetY));
            }
        }

        return new Vec3(targetX, targetY, targetZ);
    }

    /**
     * Finds a safe position for the entity to teleport to, avoiding solid walls, ceilings, lava, and barriers.
     * If candidate is unsafe, searches neighboring offsets. If none are safe, falls back to fallbackPos.
     */
    public static Vec3 findSafeTeleportPos(Level level, Entity entity, double targetX, double targetY, double targetZ, Vec3 fallbackPos) {
        Vec3 clamped = clampToArenaBounds(level, entity, targetX, targetY, targetZ);
        targetX = clamped.x;
        targetY = clamped.y;
        targetZ = clamped.z;

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
                    return clampToArenaBounds(level, entity, candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
                }
            }
        }

        if (fallbackPos != null) {
            Vec3 clampedFallback = clampToArenaBounds(level, entity, fallbackPos.x, fallbackPos.y, fallbackPos.z);
            BlockPos fb = BlockPos.containing(clampedFallback);
            if (isSafe(level, entity, fb)) {
                return new Vec3(fb.getX() + 0.5D, fb.getY(), fb.getZ() + 0.5D);
            }
            return clampedFallback;
        }

        return clamped;
    }

    private static boolean isSafe(Level level, Entity entity, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        if (!below.isSolid() || below.is(Blocks.BARRIER) || below.is(Blocks.LAVA)) return false;

        if (!feet.getCollisionShape(level, pos).isEmpty() || feet.is(Blocks.BARRIER)) return false;
        if (!head.getCollisionShape(level, pos.above()).isEmpty() || head.is(Blocks.BARRIER)) return false;

        // In Ranker Tower, prevent choosing any position on or above the chamber ceiling or outside walls
        if (level.dimension().equals(TowerManager.RANKER_TOWER) && entity instanceof Player player) {
            var session = TowerManager.getSession(player.getUUID());
            if (session != null && session.floorOrigin != null) {
                if (pos.getY() >= session.floorOrigin.getY() + TowerManager.CHAMBER_HEIGHT) {
                    return false;
                }
                double dx = (pos.getX() + 0.5D) - (session.floorOrigin.getX() + 0.5D);
                double dz = (pos.getZ() + 0.5D) - (session.floorOrigin.getZ() + 0.5D);
                if ((dx * dx + dz * dz) > (TowerManager.ARENA_RADIUS - 2.0D) * (TowerManager.ARENA_RADIUS - 2.0D)) {
                    return false;
                }
            }
        }

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
