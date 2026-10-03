package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JadeArmoredBeastEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class JadeArmoredBeastRenderer extends MobRenderer<JadeArmoredBeastEntity, JadeArmoredBeastModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/murim/jade_armored_beast.png");

    public JadeArmoredBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new JadeArmoredBeastModel(context.bakeLayer(JadeArmoredBeastModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    public ResourceLocation getTextureLocation(JadeArmoredBeastEntity entity) {
        return TEXTURE;
    }
}
