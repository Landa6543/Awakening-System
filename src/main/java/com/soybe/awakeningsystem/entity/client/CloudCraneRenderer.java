package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CloudCraneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador principal para la Grulla Celestial de las Nubes (Cloud Crane).
 */
public class CloudCraneRenderer extends MobRenderer<CloudCraneEntity, CloudCraneModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/cloud_crane/cloud_crane.png");

    public CloudCraneRenderer(EntityRendererProvider.Context context) {
        super(context, new CloudCraneModel(context.bakeLayer(CloudCraneModel.LAYER_LOCATION)), 0.65F);
        this.addLayer(new CloudCraneGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CloudCraneEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CloudCraneEntity entity, PoseStack poseStack, float partialTickTime) {
        // Escala esbelta y majestuosa de ave sagrada (1.2x)
        poseStack.scale(1.2F, 1.2F, 1.2F);
    }
}
