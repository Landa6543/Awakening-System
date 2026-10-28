package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BathysmalVishapEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BathysmalVishapRenderer extends MobRenderer<BathysmalVishapEntity, BathysmalVishapModel<BathysmalVishapEntity>> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "bathysmal_vishap"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/bathysmal_vishap.png");

    public BathysmalVishapRenderer(EntityRendererProvider.Context context) {
        super(context, new BathysmalVishapModel<>(context.bakeLayer(LAYER_LOCATION)), 1.3F);
    }

    @Override
    protected void scale(BathysmalVishapEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(BathysmalVishapEntity entity) {
        return TEXTURE;
    }
}
