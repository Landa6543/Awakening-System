package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RuinHunterEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RuinHunterRenderer extends MobRenderer<RuinHunterEntity, RuinHunterModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ruin_hunter"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ruin_hunter/ruin_hunter.png");

    public RuinHunterRenderer(EntityRendererProvider.Context context) {
        super(context, new RuinHunterModel(context.bakeLayer(LAYER_LOCATION)), 0.9F);
        this.addLayer(new RuinHunterEyeLayer(this));
    }

    @Override
    protected void scale(RuinHunterEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(RuinHunterEntity entity) {
        return TEXTURE;
    }
}
