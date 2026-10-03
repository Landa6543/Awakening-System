package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.hypostasis.CryoHypostasisBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CryoHypostasisRenderer extends HypostasisRenderer<CryoHypostasisBossEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cryo_hypostasis"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/cryo_hypostasis.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/cryo_hypostasis_glow.png");

    public CryoHypostasisRenderer(EntityRendererProvider.Context context) {
        super(context, LAYER_LOCATION, TEXTURE, GLOW_TEXTURE);
    }
}
