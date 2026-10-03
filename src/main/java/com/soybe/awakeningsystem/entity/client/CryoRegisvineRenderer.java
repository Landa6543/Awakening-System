package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CryoRegisvineEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CryoRegisvineRenderer extends MobRenderer<CryoRegisvineEntity, CryoRegisvineModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cryo_regisvine"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/regisvine/cryo_regisvine.png");

    public CryoRegisvineRenderer(EntityRendererProvider.Context context) {
        super(context, new CryoRegisvineModel(context.bakeLayer(LAYER_LOCATION)), 1.8F);
    }

    @Override
    protected void scale(CryoRegisvineEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.3F, 1.3F, 1.3F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(CryoRegisvineEntity entity) {
        return TEXTURE;
    }
}
