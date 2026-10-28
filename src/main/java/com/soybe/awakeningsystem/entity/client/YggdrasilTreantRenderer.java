package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class YggdrasilTreantRenderer extends MobRenderer<YggdrasilTreantBossEntity, YggdrasilTreantModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/yggdrasil_treant.png");

    public YggdrasilTreantRenderer(EntityRendererProvider.Context context) {
        super(context, new YggdrasilTreantModel(context.bakeLayer(YggdrasilTreantModel.LAYER_LOCATION)), 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(YggdrasilTreantBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(YggdrasilTreantBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.7F, 1.7F, 1.7F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
