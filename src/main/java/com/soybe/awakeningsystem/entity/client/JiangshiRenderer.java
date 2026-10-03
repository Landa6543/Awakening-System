package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JiangshiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class JiangshiRenderer extends MobRenderer<JiangshiEntity, JiangshiModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/murim/jiangshi.png");

    public JiangshiRenderer(EntityRendererProvider.Context context) {
        super(context, new JiangshiModel(context.bakeLayer(JiangshiModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(JiangshiEntity entity) {
        return TEXTURE;
    }
}
