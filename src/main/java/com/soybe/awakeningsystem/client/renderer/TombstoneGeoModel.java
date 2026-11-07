package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.entity.TombstoneBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class TombstoneGeoModel extends GeoModel<TombstoneBlockEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/tombstone.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/tombstone.animation.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/tombstone.png");

    @Override
    public ResourceLocation getModelResource(TombstoneBlockEntity animatable, GeoRenderer<TombstoneBlockEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(TombstoneBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TombstoneBlockEntity animatable, GeoRenderer<TombstoneBlockEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(TombstoneBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TombstoneBlockEntity animatable) {
        return ANIMATION;
    }
}
