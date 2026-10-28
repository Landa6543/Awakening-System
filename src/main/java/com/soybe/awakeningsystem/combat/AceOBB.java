package com.soybe.awakeningsystem.combat;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 3D Oriented Bounding Box (OBB) representation for sweeping arc weapon attacks.
 * Employs the Separating Axis Theorem (SAT) for exact collision detection
 * between the weapon swing volume and entity bounding boxes.
 */
public class AceOBB {
    public Vec3 center;
    public Vec3 extent; // Half sizes in each dimension

    // Unit orthogonal basis vectors
    public Vec3 axisX;
    public Vec3 axisY;
    public Vec3 axisZ;

    // Precomputed scaled axes (axis * extent)
    public Vec3 scaledAxisX;
    public Vec3 scaledAxisY;
    public Vec3 scaledAxisZ;

    // 8 Corner vertices
    public Vec3[] vertices;

    public AceOBB(Vec3 center, double width, double height, double depth, float pitch, float yaw) {
        this.center = center;
        this.extent = new Vec3(width / 2.0, height / 2.0, depth / 2.0);

        // Compute forward (Z), up (Y), and right (X) direction vectors from pitch and yaw
        this.axisZ = Vec3.directionFromRotation(pitch, yaw).normalize();
        this.axisY = Vec3.directionFromRotation(pitch - 90.0F, yaw).normalize();
        this.axisX = this.axisZ.cross(this.axisY).normalize();

        updateVertices();
    }

    public AceOBB(AABB aabb) {
        this.center = new Vec3(
                (aabb.maxX + aabb.minX) / 2.0,
                (aabb.maxY + aabb.minY) / 2.0,
                (aabb.maxZ + aabb.minZ) / 2.0
        );
        this.extent = new Vec3(
                Math.abs(aabb.maxX - aabb.minX) / 2.0,
                Math.abs(aabb.maxY - aabb.minY) / 2.0,
                Math.abs(aabb.maxZ - aabb.minZ) / 2.0
        );
        this.axisX = new Vec3(1.0, 0.0, 0.0);
        this.axisY = new Vec3(0.0, 1.0, 0.0);
        this.axisZ = new Vec3(0.0, 0.0, 1.0);

        updateVertices();
    }

    public AceOBB offsetAlongAxisZ(double offset) {
        this.center = this.center.add(this.axisZ.scale(offset));
        updateVertices();
        return this;
    }

    public void updateVertices() {
        this.scaledAxisX = axisX.scale(extent.x);
        this.scaledAxisY = axisY.scale(extent.y);
        this.scaledAxisZ = axisZ.scale(extent.z);

        // 8 Corners of the box
        this.vertices = new Vec3[] {
                center.subtract(scaledAxisX).subtract(scaledAxisY).subtract(scaledAxisZ),
                center.add(scaledAxisX).subtract(scaledAxisY).subtract(scaledAxisZ),
                center.add(scaledAxisX).add(scaledAxisY).subtract(scaledAxisZ),
                center.subtract(scaledAxisX).add(scaledAxisY).subtract(scaledAxisZ),
                center.subtract(scaledAxisX).subtract(scaledAxisY).add(scaledAxisZ),
                center.add(scaledAxisX).subtract(scaledAxisY).add(scaledAxisZ),
                center.add(scaledAxisX).add(scaledAxisY).add(scaledAxisZ),
                center.subtract(scaledAxisX).add(scaledAxisY).add(scaledAxisZ)
        };
    }

    /**
     * Determines whether a given 3D point is inside this oriented box.
     */
    public boolean contains(Vec3 point) {
        Vec3 d = point.subtract(center);
        double projX = Math.abs(d.dot(axisX));
        double projY = Math.abs(d.dot(axisY));
        double projZ = Math.abs(d.dot(axisZ));

        return projX <= extent.x && projY <= extent.y && projZ <= extent.z;
    }

    /**
     * Checks if this oriented bounding box intersects a standard axis-aligned bounding box (AABB).
     */
    public boolean intersects(AABB aabb) {
        AceOBB other = new AceOBB(aabb);
        return intersects(this, other);
    }

    /**
     * Separating Axis Theorem (SAT) collision test between two OBBs.
     */
    public static boolean intersects(AceOBB a, AceOBB b) {
        // Test 3 face normals of box A
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisX)) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisY)) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisZ)) return false;

        // Test 3 face normals of box B
        if (isSeparated(a.vertices, b.vertices, b.scaledAxisX)) return false;
        if (isSeparated(a.vertices, b.vertices, b.scaledAxisY)) return false;
        if (isSeparated(a.vertices, b.vertices, b.scaledAxisZ)) return false;

        // Test 9 edge cross products
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisX))) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisY))) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisZ))) return false;

        if (isSeparated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisX))) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisY))) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisZ))) return false;

        if (isSeparated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisX))) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisY))) return false;
        if (isSeparated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisZ))) return false;

        return true;
    }

    private static boolean isSeparated(Vec3[] vertsA, Vec3[] vertsB, Vec3 axis) {
        if (axis.lengthSqr() < 1e-6) {
            return false;
        }

        double aMin = Double.POSITIVE_INFINITY;
        double aMax = Double.NEGATIVE_INFINITY;
        double bMin = Double.POSITIVE_INFINITY;
        double bMax = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 8; i++) {
            double projA = vertsA[i].dot(axis);
            if (projA < aMin) aMin = projA;
            if (projA > aMax) aMax = projA;

            double projB = vertsB[i].dot(axis);
            if (projB < bMin) bMin = projB;
            if (projB > bMax) bMax = projB;
        }

        double longSpan = Math.max(aMax, bMax) - Math.min(aMin, bMin);
        double sumSpan = (aMax - aMin) + (bMax - bMin);
        return longSpan >= sumSpan;
    }
}
