package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ManaPixieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ManaPixieRenderer extends MobRenderer<ManaPixieEntity, ManaPixieModel<ManaPixieEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/mana_pixie.png");

    public ManaPixieRenderer(EntityRendererProvider.Context context) {
        super(context, new ManaPixieModel<>(context.bakeLayer(ManaPixieModel.LAYER_LOCATION)), 0.25F);
        this.addLayer(new ManaPixieGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ManaPixieEntity entity) {
        return TEXTURE;
    }
}
