package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Capa de brillo ardiente en la oscuridad para el Monarca Demonio Baran.
 * Soporta capa de brillo blanco/carmesí para el Jefe y cian espectral para la Sombra.
 */
public class BaranGlowLayer extends GeoRenderLayer<BaranBossEntity> {
    private static final ResourceLocation LAYER_BOSS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/baran_hlow.png");
    private static final ResourceLocation LAYER_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/baran_shadow_glow.png");

    public BaranGlowLayer(GeoRenderer<BaranBossEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, BaranBossEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        ResourceLocation glowTex = (animatable != null && animatable.isShadowSoldier()) ? LAYER_SHADOW : LAYER_BOSS;
        RenderType glowRenderType = RenderType.eyes(glowTex);
        this.getRenderer().reRender(this.getDefaultBakedModel(animatable), poseStack, bufferSource, animatable,
                glowRenderType, bufferSource.getBuffer(glowRenderType), partialTick, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
