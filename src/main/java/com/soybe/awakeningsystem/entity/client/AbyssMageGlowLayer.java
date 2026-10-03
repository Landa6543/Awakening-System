package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbstractAbyssMageEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva para ojos y sellos elementales de los Magos del Abismo.
 * Conmuta entre la versión con escudo y sin escudo, y se apaga al estar aturdido.
 */
public class AbyssMageGlowLayer<T extends AbstractAbyssMageEntity> extends RenderLayer<T, AbyssMageModel<T>> {

    public AbyssMageGlowLayer(RenderLayerParent<T, AbyssMageModel<T>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isStunned() || !entity.isAlive()) {
            return;
        }

        String elem = entity.getMageElement().name().toLowerCase();
        String shieldSuffix = entity.isShielded() ? "_shielded" : "";
        ResourceLocation emissiveTex = ResourceLocation.fromNamespaceAndPath(
                AwakeningSystemMod.MOD_ID, "textures/entity/emissive_abyssmage" + shieldSuffix + "_" + elem + ".png");

        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(emissiveTex));
        this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
