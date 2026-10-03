package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GeovishapEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GeovishapRenderer extends MobRenderer<GeovishapEntity, GeovishapModel<GeovishapEntity>> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geovishap"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/geovishap.png");

    public GeovishapRenderer(EntityRendererProvider.Context context) {
        super(context, new GeovishapModel<>(context.bakeLayer(LAYER_LOCATION)), 1.3F);
    }

    @Override
    protected void scale(GeovishapEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.35F, 1.35F, 1.35F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(GeovishapEntity entity) {
        return TEXTURE;
    }
}
