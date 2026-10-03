package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva para el Dragón Ancestral.
 * Proporciona resplandor en la oscuridad a los ojos, cuernos de cristal,
 * runas del pecho y punta de espada según la variante elemental activa.
 */
public class AncientDragonGlowLayer extends RenderLayer<AncientDragonEntity, AncientDragonModel> {
    private static final RenderType GLOW_FIRE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_fire_glow.png"));
    private static final RenderType GLOW_ICE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_ice_glow.png"));
    private static final RenderType GLOW_FOREST = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_forest_glow.png"));
    private static final RenderType GLOW_SHADOW = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_shadow_glow.png"));
    private static final RenderType GLOW_LIGHTNING = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_lightning_glow.png"));
    private static final RenderType GLOW_CELESTIAL = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon/ancient_dragon_celestial_glow.png"));

    public AncientDragonGlowLayer(RenderLayerParent<AncientDragonEntity, AncientDragonModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AncientDragonEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;

        RenderType renderType = switch (entity.getVariant()) {
            case AncientDragonEntity.VARIANT_ICE -> GLOW_ICE;
            case AncientDragonEntity.VARIANT_FOREST -> GLOW_FOREST;
            case AncientDragonEntity.VARIANT_SHADOW -> GLOW_SHADOW;
            case AncientDragonEntity.VARIANT_LIGHTNING -> GLOW_LIGHTNING;
            case AncientDragonEntity.VARIANT_CELESTIAL -> GLOW_CELESTIAL;
            default -> GLOW_FIRE;
        };

        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 15728640,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
