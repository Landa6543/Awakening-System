package com.soybe.awakeningsystem.block;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.StringRepresentable;

public enum ElementalType implements StringRepresentable {
    WIND("wind", "§bCelestial Wind", 0x55FFFF, ParticleTypes.CLOUD),
    FROST("frost", "§9Glacial Frost", 0x3399FF, ParticleTypes.SNOWFLAKE),
    FIRE("fire", "§cBlazing Fury", 0xFF3300, ParticleTypes.FLAME),
    STORM("storm", "§ePrimordial Lightning", 0xFFFF33, ParticleTypes.ELECTRIC_SPARK);

    private final String name;
    private final String displayName;
    private final int color;
    private final ParticleOptions particle;

    ElementalType(String name, String displayName, int color, ParticleOptions particle) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.particle = particle;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getColor() {
        return this.color;
    }

    public ParticleOptions getParticle() {
        return this.particle;
    }
}
