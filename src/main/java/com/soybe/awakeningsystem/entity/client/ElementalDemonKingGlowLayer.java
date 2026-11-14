package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Demon King's light. The cracks in his armour, the core in his chest, his eyes, the orb and the sigil are
 * painted white and take the colour of his phase: storm blue while he is above half health, burning orange
 * below it, void violet in the last fifth, pulsing slowly. The four halo crystals keep their own colours.
 */
public class ElementalDemonKingGlowLayer extends RenderLayer<ElementalDemonKingBossEntity, ElementalDemonKingModel> {
    private static final RenderType PHASE_GLOW = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_demon_king_glow.png"));
    private static final RenderType CRYSTAL_GLOW = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/boss/elemental_demon_king_crystals.png"));

    public ElementalDemonKingGlowLayer(RenderLayerParent<ElementalDemonKingBossEntity, ElementalDemonKingModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ElementalDemonKingBossEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!entity.isAlive()) return;
        float hp = entity.getHealth() / Math.max(1.0F, entity.getMaxHealth());
        int rgb = hp > 0.5F ? 0x5CD8FF : hp > 0.2F ? 0xFF7A2A : 0xB04CFF;
        float pulse = 0.75F + 0.25F * Mth.sin(ageInTicks * 0.12F);
        int r = (int) (((rgb >> 16) & 255) * pulse), g = (int) (((rgb >> 8) & 255) * pulse), b = (int) ((rgb & 255) * pulse);
        int color = 0xFF000000 | (r << 16) | (g << 8) | b;
        this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(PHASE_GLOW), 15728880, OverlayTexture.NO_OVERLAY, color);
        this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(CRYSTAL_GLOW), 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
