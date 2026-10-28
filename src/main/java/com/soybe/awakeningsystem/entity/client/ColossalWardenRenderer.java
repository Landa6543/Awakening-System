package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer colosal para el Titán Colosal de Sculk (Piso 10).
 * Renderiza el modelo 3D con textura nativa y sin capas superpuestas para evitar Z-fighting / parpadeos.
 */
public class ColossalWardenRenderer extends MobRenderer<ColossalSculkWardenEntity, ColossalSculkWardenModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/colossal_sculk_warden.png");

    public ColossalWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new ColossalSculkWardenModel(context.bakeLayer(ColossalSculkWardenModel.LAYER_LOCATION)), 2.2F);
    }

    @Override
    protected void scale(ColossalSculkWardenEntity livingEntity, PoseStack poseStack, float partialTickTime) {
        // Escala monumental del Titán (1.8x)
        poseStack.scale(1.8F, 1.8F, 1.8F);
        super.scale(livingEntity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(ColossalSculkWardenEntity entity) {
        return TEXTURE;
    }
}
