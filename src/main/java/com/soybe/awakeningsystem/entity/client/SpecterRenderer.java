package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SpecterEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SpecterRenderer extends MobRenderer<SpecterEntity, SpecterModel<SpecterEntity>> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "specter"), "main");

    public SpecterRenderer(EntityRendererProvider.Context context) {
        super(context, new SpecterModel<>(context.bakeLayer(LAYER_LOCATION)), 0.6F);
        this.addLayer(new SpecterGlowLayer(this));
    }

    @Override
    protected void scale(SpecterEntity entity, PoseStack poseStack, float partialTickTime) {
        float s = entity.isEnraged() ? 1.35F : 1.0F;
        poseStack.scale(s, s, s);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(SpecterEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                "textures/entity/specter_" + entity.getElement().getId() + ".png");
    }

    public static class SpecterGlowLayer extends RenderLayer<SpecterEntity, SpecterModel<SpecterEntity>> {
        public SpecterGlowLayer(RenderLayerParent<SpecterEntity, SpecterModel<SpecterEntity>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SpecterEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            ResourceLocation glowLoc = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID,
                    "textures/entity/emissive_specter_" + entity.getElement().getId() + ".png");
            VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(glowLoc));
            this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }
}
