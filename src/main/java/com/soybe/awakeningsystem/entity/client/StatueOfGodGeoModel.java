package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.StatueOfGodEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib 3D para la Estatua del Dios de Cartenon Temple.
 */
@SuppressWarnings("removal")
public class StatueOfGodGeoModel extends GeoModel<StatueOfGodEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/statue_of_god.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/statue_of_god.animation.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/statue_of_god.png");

    @Override
    public ResourceLocation getModelResource(StatueOfGodEntity animatable, GeoRenderer<StatueOfGodEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(StatueOfGodEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(StatueOfGodEntity animatable, GeoRenderer<StatueOfGodEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(StatueOfGodEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StatueOfGodEntity animatable) {
        return ANIMATION;
    }
}
