package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.entity.RouletteAltarBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@SuppressWarnings("removal")
public class RouletteAltarGeoModel extends GeoModel<RouletteAltarBlockEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/roulette_altar.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/roulette_arcade.png");

    @Override
    public ResourceLocation getModelResource(RouletteAltarBlockEntity animatable, software.bernie.geckolib.renderer.GeoRenderer<RouletteAltarBlockEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(RouletteAltarBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RouletteAltarBlockEntity animatable, software.bernie.geckolib.renderer.GeoRenderer<RouletteAltarBlockEntity> renderer) {
        return TEXTURE;
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(RouletteAltarBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RouletteAltarBlockEntity animatable) {
        return null;
    }

    @Override
    public void setCustomAnimations(RouletteAltarBlockEntity animatable, long instanceId, AnimationState<RouletteAltarBlockEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        float partialTick = animationState.getPartialTick();
        float wheelAngle = Mth.lerp(partialTick, animatable.getPrevWheelAngle(), animatable.getWheelAngle());

        float needleAngle = 0.0F;
        if (animatable.isSpinning()) {
            float cycle = (wheelAngle % 22.5F) / 22.5F;
            if (cycle < 0.70F) {
                needleAngle = (cycle / 0.70F) * 16.0F;
            } else {
                float snap = (cycle - 0.70F) / 0.30F;
                needleAngle = (1.0F - snap) * 16.0F * (float) Math.cos(snap * Math.PI * 2.0);
            }
        }
        final float finalNeedleAngle = needleAngle;

        getBone("wheel").ifPresent(wheel -> {
            wheel.setRotZ(-wheelAngle * Mth.DEG_TO_RAD);
        });

        getBone("pointer").ifPresent(pointer -> {
            pointer.setRotZ(finalNeedleAngle * Mth.DEG_TO_RAD);
        });
    }
}
