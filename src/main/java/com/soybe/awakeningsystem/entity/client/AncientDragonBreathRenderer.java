package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.entity.AncientDragonBreathEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AncientDragonBreathRenderer extends EntityRenderer<AncientDragonBreathEntity> {
    private static final ResourceLocation DUMMY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/particle/glitter_0.png");

    public AncientDragonBreathRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(AncientDragonBreathEntity entity) {
        return DUMMY_TEXTURE;
    }
}
