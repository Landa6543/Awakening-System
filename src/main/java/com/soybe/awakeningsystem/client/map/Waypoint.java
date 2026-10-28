package com.soybe.awakeningsystem.client.map;

import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de datos para un Waypoint personalizado del sistema cartográfico de Awakening System.
 * Inspirado en Xaero's Minimap y JourneyMap, con soporte para color HEX/RGB 100% libre.
 */
public class Waypoint {
    private String id;
    private String name;
    private String dimension;
    private int x;
    private int y;
    private int z;
    private int color; // Formato RGB: 0xRRGGBB
    private Boolean enabled = true;
    private Boolean showBeam = true;
    private Boolean showLabel = true;
    private Boolean isDeathPoint = false;

    public Waypoint() {
        this.id = UUID.randomUUID().toString();
        this.name = "Waypoint";
        this.dimension = "minecraft:overworld";
        this.x = 0;
        this.y = 64;
        this.z = 0;
        this.color = 0x00E5FF; // Cian eléctrico por defecto
        this.enabled = true;
        this.showBeam = true;
        this.showLabel = true;
    }

    public Waypoint(String name, String dimension, int x, int y, int z, int color) {
        this.id = UUID.randomUUID().toString();
        this.name = name != null && !name.isBlank() ? name : "Waypoint";
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color & 0xFFFFFF;
        this.enabled = true;
        this.showBeam = true;
        this.showLabel = true;
    }

    public Waypoint(String id, String name, String dimension, int x, int y, int z, int color, boolean enabled, boolean showBeam, boolean showLabel) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null && !name.isBlank() ? name : "Waypoint";
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color & 0xFFFFFF;
        this.enabled = enabled;
        this.showBeam = showBeam;
        this.showLabel = showLabel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
    }

    public String getHexColor() {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    public void setHexColor(String hex) {
        if (hex == null) return;
        String clean = hex.trim();
        if (clean.startsWith("#")) clean = clean.substring(1);
        try {
            this.color = Integer.parseInt(clean, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {}
    }

    public float getRed() {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    public float getGreen() {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    public float getBlue() {
        return (color & 0xFF) / 255.0F;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowBeam() {
        return showBeam == null || showBeam;
    }

    public void setShowBeam(boolean showBeam) {
        this.showBeam = showBeam;
    }

    public boolean isShowLabel() {
        return showLabel == null || showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isDeathPoint() {
        return isDeathPoint != null && isDeathPoint;
    }

    public void setDeathPoint(boolean deathPoint) {
        this.isDeathPoint = deathPoint;
    }

    public double distanceToSq(double px, double py, double pz) {
        double dx = px - x;
        double dy = py - y;
        double dz = pz - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distanceTo(double px, double py, double pz) {
        return Math.sqrt(distanceToSq(px, py, pz));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return Objects.equals(id, waypoint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
