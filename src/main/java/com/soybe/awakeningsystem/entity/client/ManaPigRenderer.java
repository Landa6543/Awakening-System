package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaPigEntity;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ManaPigRenderer extends MobRenderer<ManaPigEntity, ManaPigModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/animal/mana_pig.png");

    public ManaPigRenderer(EntityRendererProvider.Context context) {
        super(context, new ManaPigModel(context.bakeLayer(ManaPigModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaPigEntity entity) {
        return TEXTURE;
    }
}
