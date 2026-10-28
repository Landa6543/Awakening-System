package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.RouletteAltarBlock;
import com.soybe.awakeningsystem.block.entity.RouletteAltarBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RouletteAltarRenderer implements BlockEntityRenderer<RouletteAltarBlockEntity> {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/roulette_arcade.png");

    private final RouletteWheelModel model;
    private final ItemRenderer itemRenderer;

    public RouletteAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new RouletteWheelModel(context.bakeLayer(RouletteWheelModel.LAYER_LOCATION));
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RouletteAltarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Direction facing = blockEntity.getBlockState().hasProperty(RouletteAltarBlock.FACING)
                ? blockEntity.getBlockState().getValue(RouletteAltarBlock.FACING)
                : Direction.NORTH;

        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        model.stand.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        float wheelAngle = Mth.lerp(partialTick, blockEntity.getPrevWheelAngle(), blockEntity.getWheelAngle());
        float needleAngle = 0.0F;
        if (blockEntity.isSpinning()) {
            float cycle = (wheelAngle % 22.5F) / 22.5F;
            if (cycle < 0.70F) {
                needleAngle = (cycle / 0.70F) * 16.0F;
            } else {
                float snap = (cycle - 0.70F) / 0.30F;
                needleAngle = (1.0F - snap) * 16.0F * (float) Math.cos(snap * Math.PI * 2.0);
            }
        }
        model.pointer.zRot = (float) Math.toRadians(needleAngle);
        model.pointer.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.25D, 0.05D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-wheelAngle));

        model.wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        float r = 1.0F;
        var matrix = poseStack.last().pose();

        float zFront = 0.02F;
        vertexConsumer.addVertex(matrix, -r, -r, zFront).setColor(255, 255, 255, 255).setUv(0.5F, 1.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, r, -r, zFront).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, r, r, zFront).setColor(255, 255, 255, 255).setUv(1.0F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, -r, r, zFront).setColor(255, 255, 255, 255).setUv(0.5F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

        float zBack = -0.02F;
        vertexConsumer.addVertex(matrix, -r, r, zBack).setColor(255, 255, 255, 255).setUv(0.5F, 0.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix, r, r, zBack).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix, r, -r, zBack).setColor(255, 255, 255, 255).setUv(1.0F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix, -r, -r, zBack).setColor(255, 255, 255, 255).setUv(0.5F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

        for (int i = 0; i < 8; i++) {
            ItemStack stack = blockEntity.getSlotItem(i);
            if (stack.isEmpty()) continue;

            poseStack.pushPose();
            float slotAngle = i * 45.0F;
            double rad = Math.toRadians(slotAngle);
            double dist = 0.65D;

            double ix = Math.sin(rad) * dist;
            double iy = Math.cos(rad) * dist;

            poseStack.translate(ix, iy, 0.06D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-slotAngle));
            poseStack.scale(0.42F, 0.42F, 0.42F);

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }

        poseStack.popPose();

        if (blockEntity.getWinDisplayTicks() > 0) {
            float winProgress = (45.0F - blockEntity.getWinDisplayTicks() + partialTick) / 45.0F;
            float elevateY = 1.25F + (float) Math.sin(winProgress * Math.PI) * 0.40F;

            poseStack.pushPose();
            poseStack.translate(0.0D, elevateY, 0.45D);
            poseStack.mulPose(Axis.YP.rotationDegrees(winProgress * 540.0F));
            poseStack.scale(0.70F, 0.70F, 0.70F);

            itemRenderer.renderStatic(blockEntity.getWinningItem(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
