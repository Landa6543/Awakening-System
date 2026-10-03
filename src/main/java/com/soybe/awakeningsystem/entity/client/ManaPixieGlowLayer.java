package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ManaPixieEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva de la Hada de Maná Silvana.
 * Hace que el orbe de maná, sus ojos feéricos, cabellera luminosa y bordes de las alas
 * brillen intensamente en la oscuridad de la noche o en cuevas oscuras.
 */
public class ManaPixieGlowLayer extends EyesLayer<ManaPixieEntity, ManaPixieModel<ManaPixieEntity>> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mana_pixie_glow.png"));

    public ManaPixieGlowLayer(RenderLayerParent<ManaPixieEntity, ManaPixieModel<ManaPixieEntity>> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
