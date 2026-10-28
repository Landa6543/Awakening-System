package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LimboDivineOrbProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib para el Orbe Divino del Limbo.
 */
@SuppressWarnings("removal")
public class DivineOrbGeoModel extends GeoModel<LimboDivineOrbProjectileEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/orbe_divino.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/orbe_divino.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/orbe_divino.animation.json");

    @Override
    public ResourceLocation getModelResource(LimboDivineOrbProjectileEntity animatable, GeoRenderer<LimboDivineOrbProjectileEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(LimboDivineOrbProjectileEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LimboDivineOrbProjectileEntity animatable, GeoRenderer<LimboDivineOrbProjectileEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(LimboDivineOrbProjectileEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LimboDivineOrbProjectileEntity animatable) {
        return ANIMATION;
    }
}
