package com.soybe.awakeningsystem.entity.kaisel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KaiselRenderer extends GeoEntityRenderer<AbstractKaiselEntity> {
    public KaiselRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KaiselGeoModel());
        this.shadowRadius = 3.6F;
    }

    @Override
    public RenderType getRenderType(AbstractKaiselEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, AbstractKaiselEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        if (isReRender) return;
        poseStack.scale(1.1F, 1.1F, 1.1F);
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
