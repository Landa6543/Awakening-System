package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib 3D para el Monarca Demonio Baran.
 */
public class BaranBossRenderer extends GeoEntityRenderer<BaranBossEntity> {

    public BaranBossRenderer(EntityRendererProvider.Context context) {
        super(context, new BaranBossGeoModel());
        this.shadowRadius = 0.75F;
        this.addRenderLayer(new BaranGlowLayer(this));
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, BaranBossEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = 1.35F;
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
