package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaCowEntity;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ManaCowRenderer extends MobRenderer<ManaCowEntity, CowModel<ManaCowEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/animal/mana_cow.png");

    public ManaCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaCowEntity entity) {
        return TEXTURE;
    }
}
