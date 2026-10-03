package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AstralDragonEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva para el Dragón Bebé de Cristal Astral.
 * Hace que los cuernos, gemas y ojos brillen radiantemente en la oscuridad.
 */
public class AstralDragonGlowLayer extends EyesLayer<AstralDragonEntity, AstralDragonModel> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/astral_dragon/astral_dragon_glow.png"));

    public AstralDragonGlowLayer(RenderLayerParent<AstralDragonEntity, AstralDragonModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
