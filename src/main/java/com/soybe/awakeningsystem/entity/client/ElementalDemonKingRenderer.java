package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ElementalDemonKingRenderer extends MobRenderer<ElementalDemonKingBossEntity, ElementalDemonKingModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_demon_king.png");

    public ElementalDemonKingRenderer(EntityRendererProvider.Context context) {
        super(context, new ElementalDemonKingModel(context.bakeLayer(ElementalDemonKingModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new ElementalDemonKingGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ElementalDemonKingBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(ElementalDemonKingBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
