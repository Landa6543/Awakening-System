package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LimboOphanimEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib para el Ángel Ofanim del Limbo.
 * Carga la geometría con tres aros concéntricos giroscópicos y la animación de rotación continua.
 */
@SuppressWarnings("removal")
public class AngelOfanimGeoModel extends GeoModel<LimboOphanimEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/angel_ofanim.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/angel_ofanim.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/angel_ofanim.animation.json");

    @Override
    public ResourceLocation getModelResource(LimboOphanimEntity animatable, GeoRenderer<LimboOphanimEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(LimboOphanimEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LimboOphanimEntity animatable, GeoRenderer<LimboOphanimEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(LimboOphanimEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LimboOphanimEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(LimboOphanimEntity animatable, long instanceId, software.bernie.geckolib.animation.AnimationState<LimboOphanimEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        software.bernie.geckolib.cache.object.GeoBone eye = getAnimationProcessor().getBone("eye");
        if (eye != null) {
            software.bernie.geckolib.model.data.EntityModelData entityData = animationState.getData(software.bernie.geckolib.constant.DataTickets.ENTITY_MODEL_DATA);
            if (entityData != null) {
                eye.setRotX(entityData.headPitch() * net.minecraft.util.Mth.DEG_TO_RAD);
                eye.setRotY(entityData.netHeadYaw() * net.minecraft.util.Mth.DEG_TO_RAD);
            }
        }
    }
}
