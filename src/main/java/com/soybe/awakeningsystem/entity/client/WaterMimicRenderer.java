package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WaterMimicEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WaterMimicRenderer extends MobRenderer<WaterMimicEntity, WaterMimicModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "water_mimic"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/waterminion_texture.png");

    public WaterMimicRenderer(EntityRendererProvider.Context context) {
        super(context, new WaterMimicModel(context.bakeLayer(LAYER_LOCATION)), 0.6F);
    }

    @Override
    protected void scale(WaterMimicEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterMimicEntity entity) {
        return TEXTURE;
    }
}
