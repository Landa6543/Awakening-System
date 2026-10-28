package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AstralDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador principal para el Dragón Bebé de Cristal Astral.
 * Utiliza el modelo anatómico oficial de la Dragona del Fin escalado a tamaño cría/compañero.
 */
public class AstralDragonRenderer extends MobRenderer<AstralDragonEntity, AstralDragonModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/astral_dragon/astral_dragon.png");

    public AstralDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new AstralDragonModel(context.bakeLayer(AstralDragonModel.LAYER_LOCATION)), 0.35F);
        this.addLayer(new AstralDragonGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(AstralDragonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AstralDragonEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.75F, 0.75F, 0.75F);
    }
}
