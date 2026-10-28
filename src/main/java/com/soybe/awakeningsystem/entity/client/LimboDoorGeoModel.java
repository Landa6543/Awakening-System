package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LimboDoorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib 3D para la Puerta del Limbo.
 */
@SuppressWarnings("removal")
public class LimboDoorGeoModel extends GeoModel<LimboDoorEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/limbo_door.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/limbo_door.png");

    @Override
    public ResourceLocation getModelResource(LimboDoorEntity animatable, GeoRenderer<LimboDoorEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(LimboDoorEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LimboDoorEntity animatable, GeoRenderer<LimboDoorEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(LimboDoorEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LimboDoorEntity animatable) {
        return null;
    }
}
