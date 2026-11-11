package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** The Titán Colosal de Sculk: its own GeckoLib model (a hunched deepslate-and-sculk giant), head following its gaze. */
public class ColossalSculkWardenModel extends DefaultedEntityGeoModel<ColossalSculkWardenEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/sculk_titan.geo.json");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/sculk_titan.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/sculk_titan.png");

    public ColossalSculkWardenModel() {
        super(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sculk_titan"), true);
    }

    @Override
    public ResourceLocation getModelResource(ColossalSculkWardenEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ColossalSculkWardenEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ColossalSculkWardenEntity animatable) {
        return ANIMATION;
    }
}
