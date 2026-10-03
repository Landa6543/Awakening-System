package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.hypostasis.GeoHypostasisBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GeoHypostasisRenderer extends HypostasisRenderer<GeoHypostasisBossEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo_hypostasis"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/geo_hypostasis.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/geo_hypostasis_glow.png");

    public GeoHypostasisRenderer(EntityRendererProvider.Context context) {
        super(context, LAYER_LOCATION, TEXTURE, GLOW_TEXTURE);
    }
}
