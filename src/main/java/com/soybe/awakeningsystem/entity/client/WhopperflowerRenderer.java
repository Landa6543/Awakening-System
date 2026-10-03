package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WhopperflowerEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class WhopperflowerRenderer extends MobRenderer<WhopperflowerEntity, WhopperflowerModel<WhopperflowerEntity>> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "whopperflower"), "main");

    public WhopperflowerRenderer(EntityRendererProvider.Context context) {
        super(context, new WhopperflowerModel<>(context.bakeLayer(LAYER_LOCATION)), 0.7F);
        this.addLayer(new WhopperflowerGlowLayer(this));
    }

    @Override
    protected void scale(WhopperflowerEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(WhopperflowerEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                "textures/entity/" + entity.getFlowerType().getId() + "_whopperflower.png");
    }

    public static class WhopperflowerGlowLayer extends RenderLayer<WhopperflowerEntity, WhopperflowerModel<WhopperflowerEntity>> {
        public WhopperflowerGlowLayer(RenderLayerParent<WhopperflowerEntity, WhopperflowerModel<WhopperflowerEntity>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, WhopperflowerEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            ResourceLocation glowLoc = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                    "textures/entity/emissive_" + entity.getFlowerType().getId() + "_whopperflower.png");
            VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(glowLoc));
            this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }
}
