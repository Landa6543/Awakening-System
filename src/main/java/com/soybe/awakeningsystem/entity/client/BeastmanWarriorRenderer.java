package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BeastmanWarriorRenderer extends MobRenderer<BeastmanWarriorEntity, BeastmanWarriorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/beastman/beastman_warrior.png");

    public BeastmanWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastmanWarriorModel(context.bakeLayer(BeastmanWarriorModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    public ResourceLocation getTextureLocation(BeastmanWarriorEntity entity) {
        return TEXTURE;
    }
}
