package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ThunderhelmLawachurlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ThunderhelmLawachurlRenderer extends MobRenderer<ThunderhelmLawachurlEntity, StonehideLawachurlModel<ThunderhelmLawachurlEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/thunderhelm_lawachurl.png");

    public ThunderhelmLawachurlRenderer(EntityRendererProvider.Context context) {
        super(context, new StonehideLawachurlModel<>(context.bakeLayer(StonehideLawachurlRenderer.LAYER_LOCATION)), 1.6F);
    }

    @Override
    protected void scale(ThunderhelmLawachurlEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(ThunderhelmLawachurlEntity entity) {
        return TEXTURE;
    }
}
