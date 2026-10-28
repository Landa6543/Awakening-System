package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MotherSpiderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib para la Araña Madre (Mother Spider) y su variante Reina Jefe.
 * Vincula la geometría 3D articular, la textura y las animaciones de movimiento, ataque y telaraña.
 */
@SuppressWarnings("removal")
public class MotherSpiderGeoModel extends GeoModel<MotherSpiderEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/mother_spider.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mother_spider.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/mother_spider.animation.json");

    @Override
    public ResourceLocation getModelResource(MotherSpiderEntity animatable, GeoRenderer<MotherSpiderEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(MotherSpiderEntity animatable) {
        return getModelResource(animatable, null);
    }

    @Override
    public ResourceLocation getTextureResource(MotherSpiderEntity animatable, GeoRenderer<MotherSpiderEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(MotherSpiderEntity animatable) {
        return getTextureResource(animatable, null);
    }

    @Override
    public ResourceLocation getAnimationResource(MotherSpiderEntity animatable) {
        return ANIMATION;
    }
}
