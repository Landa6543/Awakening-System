package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.FallingSideriteMeteorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador de la estrella fugaz / meteorito de Siderita.
 * En lugar de renderizar un bloque girando en el cielo, el proyectil viaja como
 * un bólido celeste resplandeciente envuelto en su intensa estela cósmica y cola de partículas.
 */
public class FallingSideriteMeteorRenderer extends EntityRenderer<FallingSideriteMeteorEntity> {

    public FallingSideriteMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(FallingSideriteMeteorEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // El cuerpo del meteorito es un bólido cósmico guiado por estelas de partículas de luz,
        // sin un bloque cúbico antiestético estancado en el cielo.
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingSideriteMeteorEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
