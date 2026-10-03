package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WhiteThunderTigerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva de ojos celestes/dorados y rayas místicas de trueno para el Tigre Blanco del Trueno.
 */
public class WhiteThunderTigerGlowLayer extends EyesLayer<WhiteThunderTigerEntity, WhiteThunderTigerModel> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/white_thunder_tiger/white_thunder_tiger_glow.png"));

    public WhiteThunderTigerGlowLayer(RenderLayerParent<WhiteThunderTigerEntity, WhiteThunderTigerModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
