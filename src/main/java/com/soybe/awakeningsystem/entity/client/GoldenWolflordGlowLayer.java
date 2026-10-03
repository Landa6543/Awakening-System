package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoldenWolflordBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class GoldenWolflordGlowLayer extends RenderLayer<GoldenWolflordBossEntity, GoldenWolflordModel> {
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/golden_wolflord_glow.png");

    public GoldenWolflordGlowLayer(RenderLayerParent<GoldenWolflordBossEntity, GoldenWolflordModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GoldenWolflordBossEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!entity.isAlive()) return;
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE));
        this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
