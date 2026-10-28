package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.BeruBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib 3D para Beru (Rey Hormiga / Ant King).
 */
public class BeruBossRenderer extends GeoEntityRenderer<BeruBossEntity> {

    public BeruBossRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BeruBossGeoModel());
        this.shadowRadius = 0.8F;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, BeruBossEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = 1.35F;
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
