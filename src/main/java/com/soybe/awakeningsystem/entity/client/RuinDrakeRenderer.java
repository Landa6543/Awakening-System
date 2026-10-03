package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinDrakeEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinDrakeRenderer extends MobRenderer<RuinDrakeEntity, RuinDrakeModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_drake"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_drake_skywatch.png");

    public RuinDrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinDrakeModel(context.bakeLayer(LAYER_LOCATION)), 1.4F);
    }

    @Override
    protected void scale(RuinDrakeEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.2F, 1.2F, 1.2F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinDrakeEntity entity) {
        return TEXTURE;
    }
}
