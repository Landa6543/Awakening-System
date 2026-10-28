package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LimboEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class LimboEntityGeoModel extends GeoModel<LimboEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/limbo_entity.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/limbo_entity.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/limbo_entity.animation.json");

    @Override
    public ResourceLocation getModelResource(LimboEntity animatable, GeoRenderer<LimboEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(LimboEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LimboEntity animatable, GeoRenderer<LimboEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(LimboEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LimboEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(LimboEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<LimboEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
