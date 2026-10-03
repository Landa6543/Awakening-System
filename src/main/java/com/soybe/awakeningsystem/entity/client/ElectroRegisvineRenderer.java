package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElectroRegisvineEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ElectroRegisvineRenderer extends MobRenderer<ElectroRegisvineEntity, ElectroRegisvineModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_regisvine"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/regisvine/electro_regisvine.png");

    public ElectroRegisvineRenderer(EntityRendererProvider.Context context) {
        super(context, new ElectroRegisvineModel(context.bakeLayer(LAYER_LOCATION)), 1.8F);
    }

    @Override
    protected void scale(ElectroRegisvineEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.3F, 1.3F, 1.3F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(ElectroRegisvineEntity entity) {
        return TEXTURE;
    }
}
