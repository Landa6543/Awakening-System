package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GreatSnowboarKingBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GreatSnowboarKingRenderer extends MobRenderer<GreatSnowboarKingBossEntity, GreatSnowboarKingModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "great_snowboar_king"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/snowboar_king.png");

    public GreatSnowboarKingRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatSnowboarKingModel(context.bakeLayer(LAYER_LOCATION)), 1.9F);
    }

    @Override
    protected void scale(GreatSnowboarKingBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.2F, 1.2F, 1.2F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(GreatSnowboarKingBossEntity entity) {
        return TEXTURE;
    }
}
