package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.entity.ShadowSoulEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ShadowSoulRenderer extends EntityRenderer<ShadowSoulEntity> {
    public ShadowSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowSoulEntity entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
