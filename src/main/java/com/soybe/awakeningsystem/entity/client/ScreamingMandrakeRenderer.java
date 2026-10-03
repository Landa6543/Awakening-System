package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ScreamingMandrakeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ScreamingMandrakeRenderer extends MobRenderer<ScreamingMandrakeEntity, ScreamingMandrakeModel<ScreamingMandrakeEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/screaming_mandrake.png");

    public ScreamingMandrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new ScreamingMandrakeModel<>(context.bakeLayer(ScreamingMandrakeModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(ScreamingMandrakeEntity entity) {
        return TEXTURE;
    }
}
