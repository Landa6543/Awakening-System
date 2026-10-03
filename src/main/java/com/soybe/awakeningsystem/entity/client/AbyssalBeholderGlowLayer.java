package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalBeholderEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva del Ojo Vigilante del Abismo.
 * Otorga brillo en la oscuridad total al gran iris central, mini-ojos de tentáculos y runas arcanas.
 */
public class AbyssalBeholderGlowLayer extends EyesLayer<AbyssalBeholderEntity, AbyssalBeholderModel<AbyssalBeholderEntity>> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_beholder_glow.png"));

    public AbyssalBeholderGlowLayer(RenderLayerParent<AbyssalBeholderEntity, AbyssalBeholderModel<AbyssalBeholderEntity>> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
