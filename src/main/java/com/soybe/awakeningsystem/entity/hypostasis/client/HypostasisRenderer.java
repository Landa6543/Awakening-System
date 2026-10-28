package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.hypostasis.HypostasisBaseBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public abstract class HypostasisRenderer<T extends HypostasisBaseBossEntity> extends MobRenderer<T, HypostasisModel<T>> {

    private final ResourceLocation textureLocation;

    public HypostasisRenderer(EntityRendererProvider.Context context, ModelLayerLocation layerLocation,
                              ResourceLocation textureLocation, ResourceLocation glowTexture) {
        super(context, new HypostasisModel<>(context.bakeLayer(layerLocation)), 1.2F);
        this.textureLocation = textureLocation;
        this.addLayer(new HypostasisGlowLayer<>(this, glowTexture));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.75F, 1.75F, 1.75F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.textureLocation;
    }
}
