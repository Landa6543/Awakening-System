package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalLordBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ElementalLordRenderer extends MobRenderer<ElementalLordBossEntity, ElementalLordModel> {
    private static final ResourceLocation TEXTURE_WIND = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_lord_wind.png");
    private static final ResourceLocation TEXTURE_FROST = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_lord_frost.png");
    private static final ResourceLocation TEXTURE_FIRE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_lord_fire.png");
    private static final ResourceLocation TEXTURE_STORM = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_lord_storm.png");

    public ElementalLordRenderer(EntityRendererProvider.Context context) {
        super(context, new ElementalLordModel(context.bakeLayer(ElementalLordModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(ElementalLordBossEntity entity) {
        return switch (entity.getElementalType()) {
            case WIND -> TEXTURE_WIND;
            case FROST -> TEXTURE_FROST;
            case FIRE -> TEXTURE_FIRE;
            case STORM -> TEXTURE_STORM;
        };
    }

    @Override
    protected void scale(ElementalLordBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
