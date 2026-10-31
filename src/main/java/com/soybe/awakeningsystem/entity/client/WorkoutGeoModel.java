package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WorkoutEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.Optional;
import java.util.UUID;

/**
 * Modelo GeckoLib para los ejercicios de Misiones Diarias.
 * Carga el modelo biped de misiones (variante default o slim) y enlaza dinámicamente la skin del jugador que entrena.
 */
@SuppressWarnings("removal")
public class WorkoutGeoModel extends GeoModel<WorkoutEntity> {
    private static final ResourceLocation MODEL_DEFAULT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/misiones_animacion.geo.json");
    private static final ResourceLocation MODEL_SLIM =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/misiones_animacion_slim.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/misiones_animacion.animation.json");
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/misiones_animacion.png");

    public boolean isSlim(WorkoutEntity animatable) {
        if (animatable != null) {
            Entity passenger = animatable.getFirstPassenger();
            if (passenger instanceof AbstractClientPlayer clientPlayer) {
                return clientPlayer.getSkin().model() == PlayerSkin.Model.SLIM;
            }

            Optional<UUID> ownerUuid = animatable.getOwnerUUID();
            if (ownerUuid.isPresent()) {
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) {
                    var playerInfo = conn.getPlayerInfo(ownerUuid.get());
                    if (playerInfo != null) {
                        return playerInfo.getSkin().model() == PlayerSkin.Model.SLIM;
                    }
                }
                return DefaultPlayerSkin.get(ownerUuid.get()).model() == PlayerSkin.Model.SLIM;
            }
        }
        return false;
    }

    @Override
    public ResourceLocation getModelResource(WorkoutEntity animatable, GeoRenderer<WorkoutEntity> renderer) {
        return isSlim(animatable) ? MODEL_SLIM : MODEL_DEFAULT;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(WorkoutEntity animatable) {
        return isSlim(animatable) ? MODEL_SLIM : MODEL_DEFAULT;
    }

    @Override
    public ResourceLocation getTextureResource(WorkoutEntity animatable, GeoRenderer<WorkoutEntity> renderer) {
        return resolveTexture(animatable);
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(WorkoutEntity animatable) {
        return resolveTexture(animatable);
    }

    private ResourceLocation resolveTexture(WorkoutEntity animatable) {
        if (animatable != null) {
            Entity passenger = animatable.getFirstPassenger();
            if (passenger instanceof AbstractClientPlayer clientPlayer) {
                return clientPlayer.getSkin().texture();
            }

            Optional<UUID> ownerUuid = animatable.getOwnerUUID();
            if (ownerUuid.isPresent()) {
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) {
                    var playerInfo = conn.getPlayerInfo(ownerUuid.get());
                    if (playerInfo != null) {
                        return playerInfo.getSkin().texture();
                    }
                }
                return DefaultPlayerSkin.get(ownerUuid.get()).texture();
            }
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WorkoutEntity animatable) {
        return ANIMATION;
    }
}
