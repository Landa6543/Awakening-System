package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** The Titán Colosal de Sculk drawn with the mod's "mutant warden" GeckoLib model, head following its gaze. */
public class ColossalSculkWardenModel extends DefaultedEntityGeoModel<ColossalSculkWardenEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/mutant_warden.geo.json");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/mutant_warden.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mutant_warden.png");

    public ColossalSculkWardenModel() {
        super(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutant_warden"), true);
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
