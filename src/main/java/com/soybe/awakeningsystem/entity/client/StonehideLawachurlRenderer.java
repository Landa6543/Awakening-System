package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.StonehideLawachurlEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StonehideLawachurlRenderer extends MobRenderer<StonehideLawachurlEntity, StonehideLawachurlModel<StonehideLawachurlEntity>> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "stonehide_lawachurl"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/lawachurl/stonehide_lawachurl.png");

    public StonehideLawachurlRenderer(EntityRendererProvider.Context context) {
        super(context, new StonehideLawachurlModel<>(context.bakeLayer(LAYER_LOCATION)), 1.6F);
    }

    @Override
    protected void scale(StonehideLawachurlEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(StonehideLawachurlEntity entity) {
        return TEXTURE;
    }
}
