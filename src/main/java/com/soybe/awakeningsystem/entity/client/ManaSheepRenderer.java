package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaSheepEntity;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ManaSheepRenderer extends MobRenderer<ManaSheepEntity, SheepModel<ManaSheepEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/animal/mana_sheep.png");

    public ManaSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
        this.addLayer(new ManaSheepFurLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(ManaSheepEntity entity) {
        return TEXTURE;
    }
}
