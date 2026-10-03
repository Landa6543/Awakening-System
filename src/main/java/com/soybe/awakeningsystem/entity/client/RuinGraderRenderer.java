package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinGraderEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinGraderRenderer extends MobRenderer<RuinGraderEntity, RuinGraderModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_grader"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_grader.png");

    public RuinGraderRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinGraderModel(context.bakeLayer(LAYER_LOCATION)), 1.8F);
    }

    @Override
    protected void scale(RuinGraderEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinGraderEntity entity) {
        return TEXTURE;
    }
}
