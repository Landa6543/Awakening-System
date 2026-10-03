package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WhiteThunderTigerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador principal para el Tigre Blanco del Trueno (White Thunder Tiger / Byakko).
 */
public class WhiteThunderTigerRenderer extends MobRenderer<WhiteThunderTigerEntity, WhiteThunderTigerModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/white_thunder_tiger/white_thunder_tiger.png");

    public WhiteThunderTigerRenderer(EntityRendererProvider.Context context) {
        super(context, new WhiteThunderTigerModel(context.bakeLayer(WhiteThunderTigerModel.LAYER_LOCATION)), 0.85F);
        this.addLayer(new WhiteThunderTigerGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(WhiteThunderTigerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(WhiteThunderTigerEntity entity, PoseStack poseStack, float partialTickTime) {
        // Escala regia de bestia espiritual felina (1.3x)
        poseStack.scale(1.3F, 1.3F, 1.3F);
    }
}
