package com.soybe.awakeningsystem.entity.kaisel;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KaiselGeoModel extends GeoModel<AbstractKaiselEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/kaisel.geo.json");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/kaisel.animation.json");
    private static final ResourceLocation TEXTURE_ALIVE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/kaisel_alive.png");
    private static final ResourceLocation TEXTURE_SHADOW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/kaisel_shadow.png");

    @Override
    public ResourceLocation getModelResource(AbstractKaiselEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractKaiselEntity animatable) {
        return animatable != null && animatable.isShadowVariant() ? TEXTURE_SHADOW : TEXTURE_ALIVE;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractKaiselEntity animatable) {
        return ANIMATIONS;
    }
}
