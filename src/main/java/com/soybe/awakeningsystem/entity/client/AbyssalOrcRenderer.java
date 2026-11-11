package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbyssalOrcRenderer extends MobRenderer<AbyssalOrcEntity, AbyssalOrcModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_orc.png");

    public AbyssalOrcRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssalOrcModel(context.bakeLayer(AbyssalOrcModel.LAYER_LOCATION)), 0.95F);
    }

    /** Blood orcs tower over a player: about three blocks tall. */
    @Override
    protected void scale(AbyssalOrcEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalOrcEntity entity) {
        return TEXTURE;
    }
}
