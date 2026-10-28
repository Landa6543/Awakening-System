package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SpiderHeadEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class SpiderHeadGeoModel extends GeoModel<SpiderHeadEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/spider_head.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/spider_head.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/spider_head.animation.json");

    @Override
    public ResourceLocation getModelResource(SpiderHeadEntity animatable, GeoRenderer<SpiderHeadEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(SpiderHeadEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SpiderHeadEntity animatable, GeoRenderer<SpiderHeadEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(SpiderHeadEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SpiderHeadEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(SpiderHeadEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<SpiderHeadEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
