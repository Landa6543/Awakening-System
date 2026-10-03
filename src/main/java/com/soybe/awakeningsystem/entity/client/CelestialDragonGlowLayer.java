package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CelestialDragonEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva del Dragón Chino Celestial Ancestral de Murim.
 * Ojos dorados penetrantes, cuernos divinos, bigotes y venas de Qi a lo largo de la columna
 * resplandecen intensamente en la oscuridad o entre las nubes de Murim.
 */
public class CelestialDragonGlowLayer extends EyesLayer<CelestialDragonEntity, CelestialDragonModel<CelestialDragonEntity>> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/celestial_dragon_glow.png"));

    public CelestialDragonGlowLayer(RenderLayerParent<CelestialDragonEntity, CelestialDragonModel<CelestialDragonEntity>> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
