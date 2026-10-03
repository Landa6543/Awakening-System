package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LeyLineMonolithEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador 3D para el Monolito de las Líneas Ley de Genshin Impact.
 */
public class LeyLineMonolithRenderer extends EntityRenderer<LeyLineMonolithEntity> {
    private static final ResourceLocation MONOLITH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyss/ley_line_monolith.png");

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

        float ageInTicks = (float) entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        // Renderizado nítido sin culling para apreciar los anillos orbitales
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LeyLineMonolithEntity entity) {
        return MONOLITH_TEXTURE;
    }
}
