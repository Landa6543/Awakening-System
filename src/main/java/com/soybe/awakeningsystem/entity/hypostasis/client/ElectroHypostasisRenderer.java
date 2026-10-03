package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.hypostasis.ElectroHypostasisBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ElectroHypostasisRenderer extends HypostasisRenderer<ElectroHypostasisBossEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_hypostasis"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/electro_hypostasis.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/electro_hypostasis_glow.png");

    public ElectroHypostasisRenderer(EntityRendererProvider.Context context) {
        super(context, LAYER_LOCATION, TEXTURE, GLOW_TEXTURE);
    }
}
