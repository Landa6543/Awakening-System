package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SeriesNpcRenderer extends MobRenderer<SeriesNpcEntity, SeriesNpcModel> {

    public SeriesNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new SeriesNpcModel(context.bakeLayer(SeriesNpcModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(SeriesNpcEntity entity) {
        return entity.getNpcProfile().getTextureLocation();
    }
}
