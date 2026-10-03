package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinShamanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoblinShamanRenderer extends MobRenderer<GoblinShamanEntity, GoblinShamanModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_shaman.png");

    public GoblinShamanRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinShamanModel(context.bakeLayer(GoblinShamanModel.LAYER_LOCATION)), 0.38F);
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinShamanEntity entity) {
        return TEXTURE;
    }
}
