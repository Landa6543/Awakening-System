package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SkeletonLordEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/** GeckoLib model of the Skeleton Lord; the crystal it sleeps in disappears once it wakes. */
@SuppressWarnings("removal")
public class SkeletonLordModel extends GeoModel<SkeletonLordEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/skeleton_lord.geo.json");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/skeleton_lord.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/skeleton_lord.png");
    private static final String[] CRYSTALS = {"head_crystals", "crystal_brow", "crystal_hand", "crystal_fingerbase", "crystal_fingertips",
            "crystal_thumbbase", "crystal_thumbtip", "crystal_arm", "crystal_chest", "crystal_waist", "crystal_leg", "crystalized_pauldron",
            "R_crystalized_pauldron"};

    @Override
    public ResourceLocation getModelResource(SkeletonLordEntity animatable, GeoRenderer<SkeletonLordEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(SkeletonLordEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SkeletonLordEntity animatable, GeoRenderer<SkeletonLordEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(SkeletonLordEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SkeletonLordEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(SkeletonLordEntity animatable, long instanceId, AnimationState<SkeletonLordEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        boolean sealed = animatable.state() == SkeletonLordEntity.STATUE;
        for (String name : CRYSTALS) {
            GeoBone bone = getAnimationProcessor().getBone(name);
            if (bone != null) bone.setHidden(!sealed);
        }
    }
}
