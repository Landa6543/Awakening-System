package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowDirewolfEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva de ojos, cuerno y marcas de relámpago para el Lobo Plateado de las Sombras.
 */
public class ShadowDirewolfGlowLayer extends EyesLayer<ShadowDirewolfEntity, ShadowDirewolfModel> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/shadow_direwolf/shadow_direwolf_glow.png"));

    public ShadowDirewolfGlowLayer(RenderLayerParent<ShadowDirewolfEntity, ShadowDirewolfModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
