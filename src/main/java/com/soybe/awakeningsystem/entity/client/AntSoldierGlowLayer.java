package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AntSoldierEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva de ojos facetados violetas/carmesí y venas de maná del Soldado Hormiga Gigante.
 */
public class AntSoldierGlowLayer extends EyesLayer<AntSoldierEntity, AntSoldierModel> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ant_soldier/ant_soldier_glow.png"));

    public AntSoldierGlowLayer(RenderLayerParent<AntSoldierEntity, AntSoldierModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
