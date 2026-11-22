package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws our own Mitachurl: one texture for both kinds; the model shows the axe or the shieldwall. */
public class MitachurlRenderer extends MobRenderer<MitachurlEntity, MitachurlModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/mitachurl/mitachurl.png");

    public MitachurlRenderer(EntityRendererProvider.Context context) {
        super(context, new MitachurlModel(context.bakeLayer(MitachurlModel.LAYER_LOCATION)), 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(MitachurlEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(MitachurlEntity entity, PoseStack poseStack, float partialTickTime) {
        // the model stands about 52 pixels tall: scaled to fill its 3.7-block frame
        poseStack.scale(1.15F, 1.15F, 1.15F);
    }
}
