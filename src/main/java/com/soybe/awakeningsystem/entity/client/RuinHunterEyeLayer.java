package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinHunterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa de brillo emisivo del ojo y visor del Cazador de las Ruinas.
 * Brilla en la oscuridad total excepto cuando el autómata queda aturdido por un disparo crítico.
 */
public class RuinHunterEyeLayer extends RenderLayer<RuinHunterEntity, RuinHunterModel> {

    private static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_hunter/emissive_ruin_hunter.png");

    public RuinHunterEyeLayer(RenderLayerParent<RuinHunterEntity, RuinHunterModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, RuinHunterEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        // Si el Cazador está aturdido (ojo impactado) o muerto, el núcleo se apaga
        if (entity.isStunned() || !entity.isAlive()) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(EMISSIVE_TEXTURE));
        this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
