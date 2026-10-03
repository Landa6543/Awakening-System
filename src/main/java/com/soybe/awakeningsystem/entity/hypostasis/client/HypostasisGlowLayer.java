package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HypostasisGlowLayer<T extends HypostasisBaseBossEntity> extends RenderLayer<T, HypostasisModel<T>> {

    private final ResourceLocation glowTexture;

    public HypostasisGlowLayer(RenderLayerParent<T, HypostasisModel<T>> renderer, ResourceLocation glowTexture) {
        super(renderer);
        this.glowTexture = glowTexture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(this.glowTexture));
        this.getParentModel().renderCore(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
