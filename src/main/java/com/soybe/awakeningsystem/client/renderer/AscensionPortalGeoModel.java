package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import com.soybe.awakeningsystem.block.entity.AscensionPortalBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Modelo GeckoLib para el Portal Dimensional (Ascension Portal / Gate).
 * Conecta la geometría del portal con arcos de piedra y vórtices rotatorios 3D.
 * Adapta la textura dinámicamente según el tipo de portal (Base Púrpura, Ascenso Azul, Retorno Carmesí).
 */
@SuppressWarnings("removal")
public class AscensionPortalGeoModel extends GeoModel<AscensionPortalBlockEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "geo/portal_gate.geo.json");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "animations/portal_gate.animation.json");

    private static final ResourceLocation TEXTURE_PURPLE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/portal_purple.png");
    private static final ResourceLocation TEXTURE_BLUE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/portal.png");
    private static final ResourceLocation TEXTURE_RED =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/portal_red.png");

    @Override
    public ResourceLocation getModelResource(AscensionPortalBlockEntity animatable, GeoRenderer<AscensionPortalBlockEntity> renderer) {
        return MODEL;
    }

    @Override
    @Deprecated
    public ResourceLocation getModelResource(AscensionPortalBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AscensionPortalBlockEntity animatable, GeoRenderer<AscensionPortalBlockEntity> renderer) {
        return resolveTexture(animatable);
    }

    @Override
    @Deprecated
    public ResourceLocation getTextureResource(AscensionPortalBlockEntity animatable) {
        return resolveTexture(animatable);
    }

    private ResourceLocation resolveTexture(AscensionPortalBlockEntity animatable) {
        if (animatable != null && animatable.getBlockState() != null && animatable.getBlockState().hasProperty(AscensionPortalBlock.PORTAL_TYPE)) {
            AscensionPortalBlock.PortalType type = animatable.getBlockState().getValue(AscensionPortalBlock.PORTAL_TYPE);
            if (type == AscensionPortalBlock.PortalType.ASCEND) {
                return TEXTURE_BLUE;
            } else if (type == AscensionPortalBlock.PortalType.RETURN) {
                return TEXTURE_RED;
            }
        }
        return TEXTURE_PURPLE;
    }

    @Override
    public ResourceLocation getAnimationResource(AscensionPortalBlockEntity animatable) {
        return ANIMATION;
    }
}
