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
        super(context, new ManaCowModel(context.bakeLayer(ManaCowModel.LAYER)), 0.7F);
        this.addLayer(new ManaAnimalsClient.Glow<>(this, "mana_cow"));
    }

    @Override
    public ResourceLocation getTextureLocation(ManaCowEntity entity) {
        return TEXTURE;
    }
}
