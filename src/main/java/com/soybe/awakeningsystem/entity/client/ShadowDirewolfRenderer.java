package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowDirewolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador principal para el Lobo Plateado de las Sombras (Shadow Direwolf).
 */
public class ShadowDirewolfRenderer extends MobRenderer<ShadowDirewolfEntity, ShadowDirewolfModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/shadow_direwolf/shadow_direwolf.png");

    public ShadowDirewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowDirewolfModel(context.bakeLayer(ShadowDirewolfModel.LAYER_LOCATION)), 0.75F);
        this.addLayer(new ShadowDirewolfGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowDirewolfEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(ShadowDirewolfEntity entity, PoseStack poseStack, float partialTickTime) {
        // Escala imponente de Lobo Alfa / Direwolf (1.3x tamaño respecto a modelo base)
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }
}
