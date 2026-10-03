package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinGuardEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa de brillo emisivo del ojo óptico del Guardián de las Ruinas.
 * Brilla en la oscuridad total excepto cuando el autómata queda aturdido por un disparo crítico.
 */
public class RuinGuardEyeLayer extends RenderLayer<RuinGuardEntity, RuinGuardModel> {

    private static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_guard/emissive_ruin_guard.png");

    public RuinGuardEyeLayer(RenderLayerParent<RuinGuardEntity, RuinGuardModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, RuinGuardEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        // Si el Guardián está aturdido (ojo roto) o muerto, el ojo se apaga completamente
        if (entity.isStunned() || !entity.isAlive()) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(EMISSIVE_TEXTURE));
        this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
