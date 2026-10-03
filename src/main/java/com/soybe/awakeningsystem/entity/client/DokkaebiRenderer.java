package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.DokkaebiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DokkaebiRenderer extends MobRenderer<DokkaebiEntity, DokkaebiModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/dokkaebi.png");

    public DokkaebiRenderer(EntityRendererProvider.Context context) {
        super(context, new DokkaebiModel(context.bakeLayer(DokkaebiModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(DokkaebiEntity entity) {
        return TEXTURE;
    }
}
