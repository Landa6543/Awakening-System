package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.entity.DragonNestBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DragonNestRenderer implements BlockEntityRenderer<DragonNestBlockEntity> {
    public static final ResourceLocation NEST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/dragon_nest.png");

    private final DragonNestModel nestModel;
    private final AncientDragonEggModel eggModel;

    public DragonNestRenderer(BlockEntityRendererProvider.Context context) {
        this.nestModel = new DragonNestModel(context.bakeLayer(DragonNestModel.LAYER_LOCATION));
        this.eggModel = new AncientDragonEggModel(context.bakeLayer(AncientDragonEggModel.LAYER_LOCATION));
    }

    @Override
    public void render(DragonNestBlockEntity nest, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(1.55F, 1.0F, 1.55F);

        VertexConsumer nestConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(NEST_TEXTURE));
        this.nestModel.renderToBuffer(poseStack, nestConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();

        int eggCount = nest.getEggCount();
        if (eggCount == 1) {
            renderEgg(poseStack, buffer, packedLight, packedOverlay,
                    nest.getEggVariant1(), 0.0D, 0.14D, 0.0D,
                    4.0F, 15.0F, 6.0F, 0.80F);
        } else if (eggCount >= 2) {
            renderEgg(poseStack, buffer, packedLight, packedOverlay,
                    nest.getEggVariant1(), -0.32D, 0.14D, -0.08D,
                    5.0F, -20.0F, -14.0F, 0.64F);

            renderEgg(poseStack, buffer, packedLight, packedOverlay,
                    nest.getEggVariant2(), 0.32D, 0.14D, 0.08D,
                    -5.0F, 25.0F, 14.0F, 0.64F);
        }
    }

    /**
     * Renderiza un huevo de dragón escalado con precisión desde su base,
     * apoyado exactamente sobre el lecho del nido sin hundirse en el suelo ni flotar en el aire.
     */
    private void renderEgg(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                           int variant, double offsetX, double offsetY, double offsetZ,
                           float rotX, float rotY, float rotZ, float scale) {
        poseStack.pushPose();

        poseStack.translate(0.5D + offsetX, offsetY, 0.5D + offsetZ);

        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

        poseStack.scale(scale, scale, scale);

        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        ResourceLocation eggTex = AncientDragonEggRenderer.getTextureForVariant(variant);
        VertexConsumer eggConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(eggTex));
        this.eggModel.renderToBuffer(poseStack, eggConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        poseStack.popPose();
    }
}
