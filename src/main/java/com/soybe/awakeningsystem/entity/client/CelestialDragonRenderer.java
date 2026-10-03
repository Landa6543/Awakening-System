package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CelestialDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CelestialDragonRenderer extends MobRenderer<CelestialDragonEntity, CelestialDragonModel<CelestialDragonEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/celestial_dragon.png");

    public CelestialDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new CelestialDragonModel<>(context.bakeLayer(CelestialDragonModel.LAYER_LOCATION)), 3.0F);
        this.addLayer(new CelestialDragonGlowLayer(this));
    }

    @Override
    protected void scale(CelestialDragonEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(3.6F, 3.6F, 3.6F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(CelestialDragonEntity entity) {
        return TEXTURE;
    }
}
