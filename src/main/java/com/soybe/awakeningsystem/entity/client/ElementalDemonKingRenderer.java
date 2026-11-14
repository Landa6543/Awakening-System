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
        // the model is about 3.5 blocks tall at full size, horns included; he stands about 4 blocks
        poseStack.scale(1.15F, 1.15F, 1.15F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
