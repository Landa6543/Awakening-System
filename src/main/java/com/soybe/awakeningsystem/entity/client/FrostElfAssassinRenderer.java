package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.FrostElfAssassinEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FrostElfAssassinRenderer extends MobRenderer<FrostElfAssassinEntity, FrostElfAssassinModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/frost_elf_assassin.png");

    public FrostElfAssassinRenderer(EntityRendererProvider.Context context) {
        super(context, new FrostElfAssassinModel(context.bakeLayer(FrostElfAssassinModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FrostElfAssassinEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(FrostElfAssassinEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.95F, 0.95F, 0.95F);
    }
}
