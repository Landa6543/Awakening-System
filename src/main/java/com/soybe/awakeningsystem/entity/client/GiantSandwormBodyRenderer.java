package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GiantSandwormBodyPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GiantSandwormBodyRenderer extends EntityRenderer<GiantSandwormBodyPart> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/sandworm/sandworm_body.png");
    private final GiantSandwormBodyModel model;

    public GiantSandwormBodyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new GiantSandwormBodyModel(ctx.bakeLayer(GiantSandwormModelLayers.BODY));
    }

    @Override
    public ResourceLocation getTextureLocation(GiantSandwormBodyPart entity) {
        return TEXTURE;
    }

    @Override
    public void render(GiantSandwormBodyPart entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);

        int renderAreaScale = entity.getRenderScale();

        float lerpedYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float lerpedPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(lerpedYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-lerpedPitch));

        float roll = (entity.tickCount + partialTick) * 10.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

        poseStack.scale(renderAreaScale, renderAreaScale, 15.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
