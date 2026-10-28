package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.IgrisBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib 3D para el Comandante Carmesí Igris (Igris Boss & Shadow).
 * Utiliza el modelo y animaciones oficiales de Solo Leveling con alternancia
 * de textura entre Caballero Carmesí y Sombra de Igris.
 */
@SuppressWarnings("removal")
public class IgrisBossGeoModel extends GeoModel<IgrisBossEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/igris.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/igris.animation.json");
    private static final ResourceLocation TEXTURE_BOSS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/red_igris.png");
    private static final ResourceLocation TEXTURE_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/dark_igris.png");

    @Override
    public ResourceLocation getModelResource(IgrisBossEntity animatable, GeoRenderer<IgrisBossEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(IgrisBossEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(IgrisBossEntity animatable, GeoRenderer<IgrisBossEntity> renderer) {
        return (animatable != null && animatable.isShadowSoldier()) ? TEXTURE_SHADOW : TEXTURE_BOSS;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(IgrisBossEntity animatable) {
        return (animatable != null && animatable.isShadowSoldier()) ? TEXTURE_SHADOW : TEXTURE_BOSS;
    }

    @Override
    public ResourceLocation getAnimationResource(IgrisBossEntity animatable) {
        return ANIMATION;
    }
}
