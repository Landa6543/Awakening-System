package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.DemonicCultivatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DemonicCultivatorRenderer extends MobRenderer<DemonicCultivatorEntity, DemonicCultivatorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/murim/demonic_cultivator.png");

    public DemonicCultivatorRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonicCultivatorModel(context.bakeLayer(DemonicCultivatorModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DemonicCultivatorEntity entity) {
        return TEXTURE;
    }
}
