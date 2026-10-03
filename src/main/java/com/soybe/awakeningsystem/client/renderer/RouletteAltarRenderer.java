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

        // 1. Orientar la ruleta hacia el jugador, apoyada directamente en el piso (Y=0.0D)
        Direction facing = blockEntity.getBlockState().hasProperty(RouletteAltarBlock.FACING)
                ? blockEntity.getBlockState().getValue(RouletteAltarBlock.FACING)
                : Direction.NORTH;

        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        // 2. Renderizar soporte de caballete en A (ESTRICTAMENTE DETRÁS DE LA RUEDA)
        model.stand.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        // 3. Animación de aleteo elástico de la aguja superior al chocar contra las 16 clavijas
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

        // 4. Renderizar la Gran Rueda Vertical Monumental (Diámetro 2.0 bloques = Radio 1.0D)
        poseStack.pushPose();
        // Centro del eje vertical a 1.25 bloques sobre el suelo
        poseStack.translate(0.0D, 1.25D, 0.05D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-wheelAngle));

        // Renderizar el buje central compacto y las 16 clavijas físicas 3D
        model.wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        // Renderizar carátula circular de alta definición (Doble cara: Frontal y Trasera)
        float r = 1.0F; // 1.0 bloque de radio = 2.0 bloques de diámetro
        var matrix = poseStack.last().pose();

        // Cara frontal (+Z): UV [0.5, 1.0], [0.5, 1.0]
        float zFront = 0.02F;
        vertexConsumer.addVertex(matrix, -r, -r, zFront).setColor(255, 255, 255, 255).setUv(0.5F, 1.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, r, -r, zFront).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, r, r, zFront).setColor(255, 255, 255, 255).setUv(1.0F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix, -r, r, zFront).setColor(255, 255, 255, 255).setUv(0.5F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

        // Cara posterior (-Z): UV [0.5, 1.0], [0.0, 0.5]
        float zBack = -0.02F;
        vertexConsumer.addVertex(matrix, -r, r, zBack).setColor(255, 255, 255, 255).setUv(0.5F, 0.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix, r, r, zBack).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix, r, -r, zBack).setColor(255, 255, 255, 255).setUv(1.0F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix, -r, -r, zBack).setColor(255, 255, 255, 255).setUv(0.5F, 0.5F)
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

        // Renderizar los 8 ítems físicos en 3D en las 8 casillas (radio generoso r = 0.65D)
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

        poseStack.popPose(); // Fin rotación de la rueda

        // 5. Presentación triunfal del ítem ganador al detenerse
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
