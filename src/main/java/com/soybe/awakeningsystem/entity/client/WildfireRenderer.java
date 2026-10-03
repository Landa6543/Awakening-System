package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WildfireEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class WildfireRenderer extends MobRenderer<WildfireEntity, WildfireModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/wildfire/wildfire.png");

    public WildfireRenderer(EntityRendererProvider.Context context) {
        super(context, new WildfireModel(context.bakeLayer(WildfireModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    protected void scale(WildfireEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    protected int getBlockLightLevel(WildfireEntity entity, BlockPos pos) {
        // Brillo ardiente constante (como Blaze)
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(WildfireEntity entity) {
        return TEXTURE;
    }
}
