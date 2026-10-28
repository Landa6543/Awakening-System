package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.TallStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TallStalkerRenderer extends GeoEntityRenderer<TallStalkerEntity> {

    public TallStalkerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TallStalkerGeoModel());
        this.shadowRadius = 0.6F;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, TallStalkerEntity animatable, software.bernie.geckolib.cache.object.BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = 1.0F;
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
