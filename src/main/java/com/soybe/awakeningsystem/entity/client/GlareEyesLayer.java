package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GlareEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class GlareEyesLayer extends EyesLayer<GlareEntity, GlareModel> {

    private static final RenderType EYES_RENDER_TYPE =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/glare_eyes.png"));

    public GlareEyesLayer(RenderLayerParent<GlareEntity, GlareModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES_RENDER_TYPE;
    }
}
