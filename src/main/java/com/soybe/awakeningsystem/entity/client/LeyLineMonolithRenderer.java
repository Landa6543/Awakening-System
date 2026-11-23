package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LeyLineMonolithEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the Monolith; once the challenge starts its gold lines glow (the _glow texture, drawn full bright). */
public class LeyLineMonolithRenderer extends EntityRenderer<LeyLineMonolithEntity> {
    private static final ResourceLocation MONOLITH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyss/ley_line_monolith.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyss/ley_line_monolith_glow.png");

    private final LeyLineMonolithModel model;

    public LeyLineMonolithRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.4F;
        this.model = new LeyLineMonolithModel(context.bakeLayer(LeyLineMonolithModel.LAYER_LOCATION));
    }

    @Override
    public void render(LeyLineMonolithEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // models are made with y growing downwards: turned the right way up and standing on the floor
        poseStack.scale(-1.25F, -1.25F, 1.25F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        float ageInTicks = (float) entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(MONOLITH_TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        if (entity.isActive()) {
            this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LeyLineMonolithEntity entity) {
        return MONOLITH_TEXTURE;
    }
}
