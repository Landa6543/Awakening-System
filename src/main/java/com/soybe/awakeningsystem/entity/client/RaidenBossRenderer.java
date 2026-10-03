package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RaidenBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class RaidenBossRenderer extends MobRenderer<RaidenBossEntity, CompositeRaidenModel> {
    private static final ResourceLocation TEXTURE_PHASE1 =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/raiden_shogun.png");
    private static final ResourceLocation TEXTURE_PHASE2 =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/raiden_puppet.png");

    public RaidenBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CompositeRaidenModel(
                new RaidenShogunModel(context.bakeLayer(RaidenShogunModel.LAYER_LOCATION)),
                new RaidenPuppetModel(context.bakeLayer(RaidenPuppetModel.LAYER_LOCATION))
        ), 0.6F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(RaidenBossEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.getPhase() == 2) {
            poseStack.scale(1.28F, 1.28F, 1.28F);
        } else {
            poseStack.scale(1.12F, 1.12F, 1.12F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(RaidenBossEntity entity) {
        return entity.getPhase() == 2 ? TEXTURE_PHASE2 : TEXTURE_PHASE1;
    }
}
