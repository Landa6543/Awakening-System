package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.PaimonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PaimonRenderer extends MobRenderer<PaimonEntity, PaimonModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/paimon.png");

    public PaimonRenderer(EntityRendererProvider.Context context) {
        super(context, new PaimonModel(context.bakeLayer(PaimonModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    protected void scale(PaimonEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.65F, 0.65F, 0.65F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(PaimonEntity entity) {
        return TEXTURE;
    }
}
