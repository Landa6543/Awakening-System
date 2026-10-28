package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.StatueOfGodEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib 3D para la Estatua del Dios Ancestral de Cartenon Temple.
 */
public class StatueOfGodRenderer extends GeoEntityRenderer<StatueOfGodEntity> {

    public StatueOfGodRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StatueOfGodGeoModel());
        this.shadowRadius = 3.5F;
        this.addRenderLayer(new StatueOfGodLayer(this));
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, StatueOfGodEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = 3.4F;
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
