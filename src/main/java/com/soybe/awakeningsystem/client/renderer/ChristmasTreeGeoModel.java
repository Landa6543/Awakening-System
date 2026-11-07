package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.entity.ChristmasTreeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class ChristmasTreeGeoModel extends GeoModel<ChristmasTreeBlockEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/christmas_tree.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/christmas_tree.animation.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/christmas_tree.png");

    @Override
    public ResourceLocation getModelResource(ChristmasTreeBlockEntity animatable, GeoRenderer<ChristmasTreeBlockEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(ChristmasTreeBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChristmasTreeBlockEntity animatable, GeoRenderer<ChristmasTreeBlockEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(ChristmasTreeBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChristmasTreeBlockEntity animatable) {
        return ANIMATION;
    }
}
