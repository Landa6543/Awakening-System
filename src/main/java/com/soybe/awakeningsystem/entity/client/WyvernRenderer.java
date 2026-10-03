package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WyvernEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WyvernRenderer extends MobRenderer<WyvernEntity, WyvernModel> {
    private static final ResourceLocation TEXTURE_POISON = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/wyvern_poison.png");
    private static final ResourceLocation TEXTURE_FIRE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/wyvern_fire.png");
    private static final ResourceLocation TEXTURE_LIGHTNING = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/wyvern_lightning.png");
    private static final ResourceLocation TEXTURE_DEFAULT = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/wyvern.png");

    public WyvernRenderer(EntityRendererProvider.Context context) {
        super(context, new WyvernModel(context.bakeLayer(WyvernModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new WyvernSaddleLayer(this));
    }

    @Override
    protected void scale(WyvernEntity entity, PoseStack poseStack, float partialTickTime) {
        float scale = entity.isAdult() ? 1.35F : 0.60F;
        poseStack.scale(scale, scale, scale);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(WyvernEntity entity) {
        int variant = entity.getVariant();
        return switch (variant) {
            case WyvernEntity.VARIANT_FIRE -> TEXTURE_FIRE;
            case WyvernEntity.VARIANT_LIGHTNING -> TEXTURE_LIGHTNING;
            case WyvernEntity.VARIANT_POISON -> TEXTURE_POISON;
            default -> TEXTURE_DEFAULT;
        };
    }
}
