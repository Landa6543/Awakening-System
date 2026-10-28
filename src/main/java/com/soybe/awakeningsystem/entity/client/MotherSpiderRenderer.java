package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.entity.MotherSpiderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib para la Araña Madre (Mother Spider) y la Reina Araña Madre (Boss).
 * Maneja dinámicamente la escala proporcional (1.0x para la araña normal, 2.2x para la versión jefe colosal)
 * y corrige la orientación del modelo para que camine de frente (+90° respecto a su eje de modelado).
 */
public class MotherSpiderRenderer extends GeoEntityRenderer<MotherSpiderEntity> {

    public MotherSpiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MotherSpiderGeoModel());
        this.shadowRadius = 0.8F;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, MotherSpiderEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = animatable.getSpiderScale();
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    protected float getDeathMaxRotation(MotherSpiderEntity animatable) {
        return 0.0F;
    }

    @Override
    protected void applyRotations(MotherSpiderEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
    }

    @Override
    public int getPackedOverlay(MotherSpiderEntity animatable, float u, float partialTick) {
        if (animatable.isDeadOrDying()) {
            return net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
        }
        return super.getPackedOverlay(animatable, u, partialTick);
    }

    @Override
    public void render(MotherSpiderEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = 0.8F * entity.getSpiderScale();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
