package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SkeletonLordEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SkeletonLordRenderer extends GeoEntityRenderer<SkeletonLordEntity> {
    public SkeletonLordRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonLordModel());
        this.shadowRadius = 1.2F;
        addRenderLayer(new AdditiveGlowLayer(this));
    }

    /**
     * The crystal's glow: the model drawn a second time with skeleton_lord_glow.png, added on top of what is
     * already there (the same "additive" mode the texture has in Blockbench). Only the glow cubes have
     * pixels in that texture, so nothing else changes.
     */
    static final class AdditiveGlowLayer extends GeoRenderLayer<SkeletonLordEntity> {
        private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/skeleton_lord_glow.png");

        AdditiveGlowLayer(GeoRenderer<SkeletonLordEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, SkeletonLordEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (animatable.state() != SkeletonLordEntity.STATUE) return;   // the crystal is gone once it wakes
            RenderType glow = RenderType.eyes(GLOW);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick,
                    0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }
}
