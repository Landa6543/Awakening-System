package com.soybe.awakeningsystem.block;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.StringRepresentable;

public enum WuXingElement implements StringRepresentable {
    WOOD("wood", 0, "§aMadera (Mù - 木)", 0x55FF55, ParticleTypes.HAPPY_VILLAGER),
    FIRE("fire", 1, "§cFuego (Huǒ - 火)", 0xFF5555, ParticleTypes.FLAME),
    EARTH("earth", 2, "§6Tierra (Tǔ - 土)", 0xFFAA00, ParticleTypes.CAMPFIRE_COSY_SMOKE),
    METAL("metal", 3, "§eMetal (Jīn - 金)", 0xFFFF55, ParticleTypes.CRIT),
    WATER("water", 4, "§bAgua (Shuǐ - 水)", 0x55FFFF, ParticleTypes.SPLASH);

    private final String name;
    private final int index;
    private final String displayName;
    private final int color;
    private final ParticleOptions particle;

    WuXingElement(String name, int index, String displayName, int color, ParticleOptions particle) {
        this.name = name;
        this.index = index;
        this.displayName = displayName;
        this.color = color;
        this.particle = particle;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getIndex() {
        return this.index;
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
