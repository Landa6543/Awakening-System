package com.soybe.awakeningsystem.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Efectos Atmosféricos Especiales del Limbo (The Limbo Dimension):
 * Purgatorio dimensional de oscuridad absoluta.
 * Atmósfera 100% negra (#000000) sin dispersión lumínica.
 */
public class LimboSpecialEffects extends DimensionSpecialEffects {
    public LimboSpecialEffects() {
        super(Float.NaN, false, SkyType.NONE, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // Oscuridad absoluta: todo el espectro de niebla es negro puro
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }
}
