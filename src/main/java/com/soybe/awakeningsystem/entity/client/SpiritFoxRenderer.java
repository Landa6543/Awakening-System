package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SpiritFoxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritFoxRenderer extends MobRenderer<SpiritFoxEntity, SpiritFoxModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/spirit_fox/spirit_fox.png");

    public SpiritFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritFoxModel(context.bakeLayer(SpiritFoxModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritFoxEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SpiritFoxEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.95F, 0.95F, 0.95F);
    }
}
