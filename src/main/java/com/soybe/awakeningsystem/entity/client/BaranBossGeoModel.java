package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BaranBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib 3D para el Monarca Demonio Baran (Demon Monarch Baran).
 * Soporta variante de Jefe (Llamas Blancas con melena) y Soldado de Sombra (Fuego Espectral).
 */
@SuppressWarnings("removal")
public class BaranBossGeoModel extends GeoModel<BaranBossEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/baran.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/baran.animation.json");
    private static final ResourceLocation TEXTURE_BOSS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/baran.png");
    private static final ResourceLocation TEXTURE_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/baran_shadow.png");

    @Override
    public ResourceLocation getModelResource(BaranBossEntity animatable, GeoRenderer<BaranBossEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(BaranBossEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BaranBossEntity animatable, GeoRenderer<BaranBossEntity> renderer) {
        return (animatable != null && animatable.isShadowSoldier()) ? TEXTURE_SHADOW : TEXTURE_BOSS;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(BaranBossEntity animatable) {
        return (animatable != null && animatable.isShadowSoldier()) ? TEXTURE_SHADOW : TEXTURE_BOSS;
    }

    @Override
    public ResourceLocation getAnimationResource(BaranBossEntity animatable) {
        return ANIMATION;
    }
}
