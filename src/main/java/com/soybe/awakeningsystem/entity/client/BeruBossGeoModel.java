package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BeruBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib 3D para Beru (Rey Hormiga / Ant King).
 */
@SuppressWarnings("removal")
public class BeruBossGeoModel extends GeoModel<BeruBossEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/ant_king_dark.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/ant_king_dark.animation.json");
    private static final ResourceLocation TEXTURE_BOSS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/bery_default.png");
    private static final ResourceLocation TEXTURE_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/bery_shadow.png");

    @Override
    public ResourceLocation getModelResource(BeruBossEntity animatable, GeoRenderer<BeruBossEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(BeruBossEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BeruBossEntity animatable, GeoRenderer<BeruBossEntity> renderer) {
        return (animatable != null && animatable.isShadowSoldier()) ? TEXTURE_SHADOW : TEXTURE_BOSS;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(BeruBossEntity animatable) {
        return (animatable != null && animatable.isShadowSoldier()) ? TEXTURE_SHADOW : TEXTURE_BOSS;
    }

    @Override
    public ResourceLocation getAnimationResource(BeruBossEntity animatable) {
        return ANIMATION;
    }
}
