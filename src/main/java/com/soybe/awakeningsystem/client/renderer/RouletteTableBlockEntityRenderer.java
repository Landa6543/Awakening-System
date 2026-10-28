package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.RouletteTableBlock;
import com.soybe.awakeningsystem.block.entity.RouletteTableBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RouletteTableBlockEntityRenderer implements BlockEntityRenderer<RouletteTableBlockEntity> {
    public static final ResourceLocation WHEEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/casino_roulette_wheel.png");
    public static final ResourceLocation BRASS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/casino_table_brass.png");

    private static final int SEGMENTS = 24;

    public RouletteTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RouletteTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Direction facing = blockEntity.getBlockState().hasProperty(RouletteTableBlock.FACING)
                ? blockEntity.getBlockState().getValue(RouletteTableBlock.FACING)
                : Direction.NORTH;

        Direction right = facing.getClockWise();

        double centerX = 0.5D + right.getStepX() * 0.5D;
        double centerZ = 0.5D + right.getStepZ() * 0.5D;

        poseStack.translate(centerX, 0.0D, centerZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        float wheelAngle = blockEntity.getInterpolatedWheelAngle(partialTick);
        float ballAngle = blockEntity.getInterpolatedBallAngle(partialTick);
        float spinProgress = blockEntity.getSpinProgress(partialTick);
        boolean isSpinning = blockEntity.isSpinning();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(wheelAngle));

        VertexConsumer wheelConsumer = bufferSource.getBuffer(RenderType.entityCutout(WHEEL_TEXTURE));
        renderRotorDisk(poseStack, wheelConsumer, packedLight, packedOverlay);

        poseStack.popPose();

        VertexConsumer brassConsumer = bufferSource.getBuffer(RenderType.entitySolid(BRASS_TEXTURE));

        renderDeepBowl(poseStack, brassConsumer, packedLight, packedOverlay);

        renderDeflectorStuds(poseStack, brassConsumer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(wheelAngle));
        renderSpindleAndCrossbar(poseStack, brassConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        float ballRadius;
        float ballY;

        if (isSpinning) {
            if (spinProgress > 0.35F) {
                ballRadius = 0.320F;
                ballY = 0.895F;
            } else {
                float fallT = 1.0F - (spinProgress / 0.35F);
                ballRadius = Mth.lerp(fallT, 0.320F, 0.225F);
                ballY = Mth.lerp(fallT, 0.895F, 0.868F);
                ballY += (float) (Math.abs(Math.sin(fallT * Math.PI * 5.0D)) * 0.008F * (1.0F - fallT));
            }
        } else {
            ballRadius = 0.225F;
            ballY = 0.868F;
        }

        float bRad = ballAngle * Mth.DEG_TO_RAD;
        float bx = (float) Math.cos(bRad) * ballRadius;
        float bz = (float) Math.sin(bRad) * ballRadius;

        renderIvoryBall3D(poseStack, brassConsumer, bx, ballY, bz, 0.035F, packedOverlay);

        poseStack.popPose();
    }

    /**
     * Renderiza el cuenco 3D: anillo superior inclinado y pista de rodadura perimetral.
     */
    private void renderDeepBowl(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        var pose = poseStack.last();
        var mat = pose.pose();

        float rOuter = 0.385F;
        float yTop = 0.935F;

        float rTrackOuter = 0.335F;
        float yTrack = 0.880F;

        float rTrackInner = 0.275F;
        float yInner = 0.862F;

        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (2.0D * Math.PI * i / SEGMENTS);
            float a1 = (float) (2.0D * Math.PI * (i + 1) / SEGMENTS);

            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);

            consumer.addVertex(mat, rTrackOuter * c0, yTrack, rTrackOuter * s0).setColor(210, 180, 90, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rTrackOuter * c1, yTrack, rTrackOuter * s1).setColor(210, 180, 90, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rOuter * c1, yTop, rOuter * s1).setColor(235, 205, 120, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rOuter * c0, yTop, rOuter * s0).setColor(235, 205, 120, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);

            consumer.addVertex(mat, rTrackInner * c0, yInner, rTrackInner * s0).setColor(190, 160, 75, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rTrackInner * c1, yInner, rTrackInner * s1).setColor(190, 160, 75, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rTrackOuter * c1, yTrack, rTrackOuter * s1).setColor(210, 180, 90, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rTrackOuter * c0, yTrack, rTrackOuter * s0).setColor(210, 180, 90, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    /**
     * Renderiza 8 deflectores de impacto de latón en 3D (diamantes perimetrales).
     */
    private void renderDeflectorStuds(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        var pose = poseStack.last();
        var mat = pose.pose();
        float studRadius = 0.310F;
        float studY = 0.890F;
        float halfSize = 0.012F;

        for (int i = 0; i < 8; i++) {
            float ang = (float) (i * Math.PI / 4.0D);
            float sx = (float) Math.cos(ang) * studRadius;
            float sz = (float) Math.sin(ang) * studRadius;

            consumer.addVertex(mat, sx - halfSize, studY + 0.008F, sz - halfSize).setColor(255, 230, 130, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, sx - halfSize, studY + 0.008F, sz + halfSize).setColor(255, 230, 130, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, sx + halfSize, studY + 0.008F, sz + halfSize).setColor(255, 230, 130, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, sx + halfSize, studY + 0.008F, sz - halfSize).setColor(255, 230, 130, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    /**
     * Renderiza el disco del rotor con la textura de las 37 casillas.
     */
    private void renderRotorDisk(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        var pose = poseStack.last();
        var mat = pose.pose();
        float rInner = 0.055F;
        float rOuter = 0.270F;
        float yDisk = 0.865F;

        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (2.0D * Math.PI * i / SEGMENTS);
            float a1 = (float) (2.0D * Math.PI * (i + 1) / SEGMENTS);

            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);

            float u0In = 0.5F + 0.1F * c0;
            float v0In = 0.5F + 0.1F * s0;
            float u1In = 0.5F + 0.1F * c1;
            float v1In = 0.5F + 0.1F * s1;

            float u0Out = 0.5F + 0.49F * c0;
            float v0Out = 0.5F + 0.49F * s0;
            float u1Out = 0.5F + 0.49F * c1;
            float v1Out = 0.5F + 0.49F * s1;

            consumer.addVertex(mat, rInner * c0, yDisk, rInner * s0).setColor(255, 255, 255, 255).setUv(u0In, v0In).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rInner * c1, yDisk, rInner * s1).setColor(255, 255, 255, 255).setUv(u1In, v1In).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rOuter * c1, yDisk, rOuter * s1).setColor(255, 255, 255, 255).setUv(u1Out, v1Out).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, rOuter * c0, yDisk, rOuter * s0).setColor(255, 255, 255, 255).setUv(u0Out, v0Out).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    /**
     * Renderiza la torreta central y la cruceta de 4 brazos en 3D.
     */
    private void renderSpindleAndCrossbar(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        var pose = poseStack.last();
        var mat = pose.pose();

        float coneRBottom = 0.055F;
        float coneRTop = 0.025F;
        float yBottom = 0.865F;
        float yTop = 0.940F;

        for (int i = 0; i < 12; i++) {
            float a0 = (float) (2.0D * Math.PI * i / 12.0D);
            float a1 = (float) (2.0D * Math.PI * (i + 1) / 12.0D);
            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);

            consumer.addVertex(mat, coneRBottom * c0, yBottom, coneRBottom * s0).setColor(235, 205, 110, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, coneRBottom * c1, yBottom, coneRBottom * s1).setColor(235, 205, 110, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, coneRTop * c1, yTop, coneRTop * s1).setColor(255, 225, 130, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(mat, coneRTop * c0, yTop, coneRTop * s0).setColor(255, 225, 130, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        }

        float tr = 0.028F;
        consumer.addVertex(mat, -tr, yTop + 0.012F, -tr).setColor(255, 235, 140, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, -tr, yTop + 0.012F, tr).setColor(255, 235, 140, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, tr, yTop + 0.012F, tr).setColor(255, 235, 140, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, tr, yTop + 0.012F, -tr).setColor(255, 235, 140, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);

        float armL = 0.090F;
        float armW = 0.009F;
        float armY = 0.932F;
        float armH = 0.012F;

        renderBox(poseStack, consumer, -armL, armY, -armW, armL, armY + armH, armW, 255, 215, 0, light, overlay);
        renderBox(poseStack, consumer, -armW, armY, -armL, armW, armY + armH, armL, 255, 215, 0, light, overlay);

        float knobR = 0.014F;
        renderBox(poseStack, consumer, -armL - knobR, armY - 0.002F, -knobR, -armL + knobR, armY + armH + 0.002F, knobR, 255, 235, 120, light, overlay);
        renderBox(poseStack, consumer, armL - knobR, armY - 0.002F, -knobR, armL + knobR, armY + armH + 0.002F, knobR, 255, 235, 120, light, overlay);
        renderBox(poseStack, consumer, -knobR, armY - 0.002F, -armL - knobR, knobR, armY + armH + 0.002F, -armL + knobR, 255, 235, 120, light, overlay);
        renderBox(poseStack, consumer, -knobR, armY - 0.002F, armL - knobR, knobR, armY + armH + 0.002F, armL + knobR, 255, 235, 120, light, overlay);
    }

    /**
     * Renderiza la bolita de marfil en 3D (cubo con 6 caras iluminadas a máxima luminiscencia).
     */
    private void renderIvoryBall3D(PoseStack poseStack, VertexConsumer consumer, float cx, float cy, float cz, float radius, int overlay) {
        var pose = poseStack.last();
        var mat = pose.pose();
        int light = 0x00F000F0;
        int r = 255, g = 255, b = 250, a = 255;

        float x0 = cx - radius, x1 = cx + radius;
        float y0 = cy - radius, y1 = cy + radius;
        float z0 = cz - radius, z1 = cz + radius;

        consumer.addVertex(mat, x0, y1, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, x0, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, x1, y1, z0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);

        consumer.addVertex(mat, x0, y0, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(mat, x1, y0, z0).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(mat, x1, y0, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);

        consumer.addVertex(mat, x1, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        consumer.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        consumer.addVertex(mat, x0, y1, z0).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        consumer.addVertex(mat, x1, y1, z0).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);

        consumer.addVertex(mat, x0, y0, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        consumer.addVertex(mat, x1, y0, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        consumer.addVertex(mat, x0, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);

        consumer.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);
        consumer.addVertex(mat, x0, y0, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);
        consumer.addVertex(mat, x0, y1, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);
        consumer.addVertex(mat, x0, y1, z0).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, -1, 0, 0);

        consumer.addVertex(mat, x1, y0, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
        consumer.addVertex(mat, x1, y0, z0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
        consumer.addVertex(mat, x1, y1, z0).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 1, 0, 0);
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int light, int overlay) {
        var pose = poseStack.last();
        var mat = pose.pose();

        consumer.addVertex(mat, x0, y1, z0).setColor(r, g, b, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, x0, y1, z1).setColor(r, g, b, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(mat, x1, y1, z0).setColor(r, g, b, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);

        consumer.addVertex(mat, x0, y0, z1).setColor(r, g, b, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(mat, x0, y0, z0).setColor(r, g, b, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(mat, x1, y0, z0).setColor(r, g, b, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(mat, x1, y0, z1).setColor(r, g, b, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);

        consumer.addVertex(mat, x1, y0, z0).setColor(r, g, b, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        consumer.addVertex(mat, x0, y0, z0).setColor(r, g, b, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        consumer.addVertex(mat, x0, y1, z0).setColor(r, g, b, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
        consumer.addVertex(mat, x1, y1, z0).setColor(r, g, b, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);

        consumer.addVertex(mat, x0, y0, z1).setColor(r, g, b, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        consumer.addVertex(mat, x1, y0, z1).setColor(r, g, b, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
        consumer.addVertex(mat, x0, y1, z1).setColor(r, g, b, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
    }
}
