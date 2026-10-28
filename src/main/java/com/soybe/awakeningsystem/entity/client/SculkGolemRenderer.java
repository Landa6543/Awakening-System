package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SculkGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SculkGolemRenderer extends MobRenderer<SculkGolemEntity, SculkGolemModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/sculk_golem.png");

    public SculkGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new SculkGolemModel(context.bakeLayer(SculkGolemModel.LAYER_LOCATION)), 1.2F);
    }

    @Override
    protected void scale(SculkGolemEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(SculkGolemEntity entity) {
        return TEXTURE;
    }
}
