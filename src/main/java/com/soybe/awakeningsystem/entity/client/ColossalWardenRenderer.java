package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Draws the Titán Colosal de Sculk at titan size, with its soul core and spines glowing as it breathes. */
public class ColossalWardenRenderer extends GeoEntityRenderer<ColossalSculkWardenEntity> {

    public ColossalWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new ColossalSculkWardenModel());
        this.shadowRadius = 2.4F;
        withScale(1.55F);
        addRenderLayer(new BreathingGlow(this));
    }

    /** The glow texture drawn again on top at full brightness, fading in and out with the breathing. */
    static final class BreathingGlow extends GeoRenderLayer<ColossalSculkWardenEntity> {
        private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/sculk_titan_glow.png");

        BreathingGlow(GeoRenderer<ColossalSculkWardenEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, ColossalSculkWardenEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            float wave = (Mth.sin((animatable.tickCount + partialTick) * 0.07F) + 1.0F) * 0.5F;
            float strength = 0.35F + 0.65F * wave * wave;
            int c = (int) (255 * strength);
            int color = 0xFF000000 | (c << 16) | (c << 8) | c;
            RenderType glow = RenderType.eyes(GLOW);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick,
                    0xF000F0, OverlayTexture.NO_OVERLAY, color);
        }
    }
}
