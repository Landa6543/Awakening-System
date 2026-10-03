package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CloudCraneEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva para la corona carmesí y los ojos celestiales de la Grulla de las Nubes.
 */
public class CloudCraneGlowLayer extends EyesLayer<CloudCraneEntity, CloudCraneModel> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/cloud_crane/cloud_crane_glow.png"));

    public CloudCraneGlowLayer(RenderLayerParent<CloudCraneEntity, CloudCraneModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
