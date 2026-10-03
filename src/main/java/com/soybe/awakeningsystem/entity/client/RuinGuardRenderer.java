package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinGuardEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinGuardRenderer extends MobRenderer<RuinGuardEntity, RuinGuardModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_guard"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_guard/ruin_guard.png");

    public RuinGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinGuardModel(context.bakeLayer(LAYER_LOCATION)), 1.4F);
        this.addLayer(new RuinGuardEyeLayer(this));
    }

    @Override
    protected void scale(RuinGuardEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinGuardEntity entity) {
        return TEXTURE;
    }
}
