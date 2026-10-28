package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.TallStalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class TallStalkerGeoModel extends GeoModel<TallStalkerEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/tall_stalker.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/tall_stalker.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/tall_stalker.animation.json");

    @Override
    public ResourceLocation getModelResource(TallStalkerEntity animatable, GeoRenderer<TallStalkerEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(TallStalkerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TallStalkerEntity animatable, GeoRenderer<TallStalkerEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(TallStalkerEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TallStalkerEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(TallStalkerEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<TallStalkerEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
