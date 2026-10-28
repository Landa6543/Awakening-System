package com.soybe.awakeningsystem.combat;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Mathematical utilities for spatial vectors, distances, and angles
 * in the Awakening Combat Engine (ACE).
 */
public final class AceCollisionHelper {
    private AceCollisionHelper() {}

    /**
     * Calculates the angle in degrees between two 3D vectors.
     */
    public static double angleBetween(Vec3 a, Vec3 b) {
        double lenA = a.length();
        double lenB = b.length();
        if (lenA <= 1e-6 || lenB <= 1e-6) {
            return 0.0;
        }
        double cosineTheta = a.dot(b) / (lenA * lenB);
        // Clamp to avoid numerical floating-point inaccuracies outside [-1.0, 1.0]
        cosineTheta = Math.max(-1.0, Math.min(1.0, cosineTheta));
        double angle = Math.acos(cosineTheta) * (180.0 / Math.PI);
        return Double.isNaN(angle) ? 0.0 : angle;
    }

    /**
     * Calculates the shortest distance vector FROM the given origin point TO the given AABB bounding box.
     */
    public static Vec3 distanceVector(Vec3 point, AABB box) {
        double dx = 0.0;
        if (box.minX > point.x) {
            dx = box.minX - point.x;
        } else if (box.maxX < point.y && box.maxX < point.x) {
            dx = box.maxX - point.x;
        }

        double dy = 0.0;
        if (box.minY > point.y) {
            dy = box.minY - point.y;
        } else if (box.maxY < point.y) {
            dy = box.maxY - point.y;
        }

        double dz = 0.0;
        if (box.minZ > point.z) {
            dz = box.minZ - point.z;
        } else if (box.maxZ < point.z) {
            dz = box.maxZ - point.z;
        }

        return new Vec3(dx, dy, dz);
    }

    /**
     * Returns the scalar Euclidean distance from a point to an AABB.
     */
    public static double distance(Vec3 point, AABB box) {
        return distanceVector(point, box).length();
    }
}
