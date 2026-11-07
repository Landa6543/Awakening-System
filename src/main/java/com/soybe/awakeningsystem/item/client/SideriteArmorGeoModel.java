package com.soybe.awakeningsystem.item.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.item.ManhwaArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class SideriteArmorGeoModel extends GeoModel<ManhwaArmorItem> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/siderite_armor.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/models/armor/siderite_armor.png");

    @Override
    public ResourceLocation getModelResource(ManhwaArmorItem animatable, GeoRenderer<ManhwaArmorItem> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(ManhwaArmorItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ManhwaArmorItem animatable, GeoRenderer<ManhwaArmorItem> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(ManhwaArmorItem animatable) {
        return TEXTURE;
    }

    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/siderite_armor.animation.json");

    @Override
    public ResourceLocation getAnimationResource(ManhwaArmorItem animatable) {
        return ANIMATION;
    }
}
