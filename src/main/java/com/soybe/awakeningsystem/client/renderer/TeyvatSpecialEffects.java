package com.soybe.awakeningsystem.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Efectos Atmosféricos Especiales de la Dimensión de Teyvat (Genshin Impact):
 * Nubes altas estilizadas (Y=192), niebla matutina serena con paleta pastel celestial,
 * amaneceres y atardeceres dorados característicos del mundo de Teyvat.
 */
public class TeyvatSpecialEffects extends DimensionSpecialEffects {
    public TeyvatSpecialEffects() {
        super(192.0F, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply((brightness * 0.94F + 0.08F), (brightness * 0.94F + 0.08F), (brightness * 0.91F + 0.12F));
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        float f = Mth.cos(timeOfDay * ((float)Math.PI * 2F)) - 0.0F;
        if (f >= -0.4F && f <= 0.4F) {
            float f1 = (f + 0.0F) / 0.4F * 0.5F + 0.5F;
            float f2 = 1.0F - (1.0F - Mth.sin(f1 * (float)Math.PI)) * 0.99F;
            f2 *= f2;
            float[] colors = new float[4];
            colors[0] = f1 * 0.35F + 0.65F;
            colors[1] = f1 * f1 * 0.7F + 0.25F;
            colors[2] = f1 * f1 * 0.15F + 0.25F;
            colors[3] = f2;
            return colors;
        }
        return null;
    }
}
