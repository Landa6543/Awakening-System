package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowInfantryEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadowInfantryRenderer extends MobRenderer<ShadowInfantryEntity, ShadowInfantryModel> {
    private static final ResourceLocation TEXTURE_CYAN =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/shadow_infantry.png");
    private static final ResourceLocation TEXTURE_RED =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/shadow_infantry_red.png");

    public ShadowInfantryRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowInfantryModel(context.bakeLayer(ShadowInfantryModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowInfantryEntity entity) {
        if (entity.isFriendlyShadow()) {
            return TEXTURE_CYAN;
        }
        return TEXTURE_RED;
    }
}
