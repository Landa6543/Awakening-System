package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowInfantryEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadowInfantryRenderer extends MobRenderer<ShadowInfantryEntity, ShadowInfantryModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/shadow_infantry.png");

    public ShadowInfantryRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowInfantryModel(context.bakeLayer(ShadowInfantryModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowInfantryEntity entity) {
        return TEXTURE;
    }
}
