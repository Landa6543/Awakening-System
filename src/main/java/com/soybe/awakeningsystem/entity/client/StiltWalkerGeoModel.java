package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.StiltWalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class StiltWalkerGeoModel extends GeoModel<StiltWalkerEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/stilt_walker.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/stilt_walker.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/stilt_walker.animation.json");

    @Override
    public ResourceLocation getModelResource(StiltWalkerEntity animatable, GeoRenderer<StiltWalkerEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(StiltWalkerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(StiltWalkerEntity animatable, GeoRenderer<StiltWalkerEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(StiltWalkerEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StiltWalkerEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(StiltWalkerEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<StiltWalkerEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
