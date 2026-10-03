package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.FrostarmLawachurlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FrostarmLawachurlRenderer extends MobRenderer<FrostarmLawachurlEntity, StonehideLawachurlModel<FrostarmLawachurlEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/frostarm_lawachurl.png");

    public FrostarmLawachurlRenderer(EntityRendererProvider.Context context) {
        super(context, new StonehideLawachurlModel<>(context.bakeLayer(StonehideLawachurlRenderer.LAYER_LOCATION)), 1.6F);
    }

    @Override
    protected void scale(FrostarmLawachurlEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(FrostarmLawachurlEntity entity) {
        return TEXTURE;
    }
}
