package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinWarriorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoblinWarriorRenderer extends MobRenderer<GoblinWarriorEntity, GoblinWarriorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/goblin/goblin_warrior.png");

    public GoblinWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinWarriorModel(context.bakeLayer(GoblinWarriorModel.LAYER_LOCATION)), 0.42F);
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinWarriorEntity entity) {
        return TEXTURE;
    }
}
