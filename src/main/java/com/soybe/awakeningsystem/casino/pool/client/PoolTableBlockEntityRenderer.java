package com.soybe.awakeningsystem.casino.pool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.casino.pool.PoolBall;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlock;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

import java.util.List;

public class PoolTableBlockEntityRenderer implements BlockEntityRenderer<PoolTableBlockEntity> {

    public PoolTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PoolTableBlockEntity table, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Direction facing = table.getBlockState().hasProperty(PoolTableBlock.FACING)
                ? table.getBlockState().getValue(PoolTableBlock.FACING)
                : Direction.NORTH;

        Direction right = facing.getClockWise();

        double centerX = 0.5D + right.getStepX() * 1.0D + facing.getStepX() * 1.5D;
        double centerZ = 0.5D + right.getStepZ() * 1.0D + facing.getStepZ() * 1.5D;
        double surfaceY = 0.880D;

        poseStack.translate(centerX, surfaceY, centerZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        int blockLight = (packedLight & 0xFFFF) >> 4;
        int skyLight = (packedLight >>> 16) >> 4;
        int ballLight = net.minecraft.client.renderer.LightTexture.pack(Math.max(13, blockLight), Math.max(13, skyLight));

        List<PoolBall> balls = table.getBalls();
        for (PoolBall ball : balls) {
            if (ball.pocketed) continue;

            float bx = ball.x;
            float bz = ball.z;

            renderBilliardBall(poseStack, builder, ball, bx, bz, ballLight);
        }

        PoolBall cue = table.getCueBall();
        if (cue != null && !cue.pocketed && (table.isAiming() || table.getGameStatus() == PoolTableBlockEntity.STATUS_READY)) {
            renderCueStick(poseStack, builder, cue.x, cue.z, table.getCueAimAngle(), table.getCuePower(), ballLight);
        }

        poseStack.popPose();
    }

    private void renderBilliardBall(PoseStack poseStack, VertexConsumer builder, PoolBall ball, float x, float z, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(x, PoolBall.RADIUS, z);

        int color = ball.getColorRGB();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float rad = PoolBall.RADIUS;
        Matrix4f mat = poseStack.last().pose();

        if (ball.isStripe()) {
            renderSphereSegment(mat, builder, rad, 255, 255, 255, 0.45F, 1.0F, packedLight);
            renderSphereSegment(mat, builder, rad, r, g, b, -0.45F, 0.45F, packedLight);
            renderSphereSegment(mat, builder, rad, 255, 255, 255, -1.0F, -0.45F, packedLight);
        } else {
            renderSphereSegment(mat, builder, rad, r, g, b, -1.0F, 1.0F, packedLight);
        }

        poseStack.popPose();
    }

    private void renderSphereSegment(Matrix4f matrix, VertexConsumer builder, float radius, int red, int green, int blue, float yMinNorm, float yMaxNorm, int packedLight) {
        int stacks = 8;
        int slices = 14;

        for (int i = 0; i < stacks; i++) {
            float v1 = (float) i / stacks;
            float v2 = (float) (i + 1) / stacks;

            float y1Norm = yMinNorm + v1 * (yMaxNorm - yMinNorm);
            float y2Norm = yMinNorm + v2 * (yMaxNorm - yMinNorm);

            float y1 = y1Norm * radius;
            float y2 = y2Norm * radius;

            float r1 = (float) Math.sqrt(Math.max(0.0F, radius * radius - y1 * y1));
            float r2 = (float) Math.sqrt(Math.max(0.0F, radius * radius - y2 * y2));

            float lightMult1 = 0.85F + 0.15F * Math.max(0.0F, y1Norm);
            float lightMult2 = 0.85F + 0.15F * Math.max(0.0F, y2Norm);
            int cr1 = (int) Math.min(255, red * lightMult1);
            int cg1 = (int) Math.min(255, green * lightMult1);
            int cb1 = (int) Math.min(255, blue * lightMult1);
            int cr2 = (int) Math.min(255, red * lightMult2);
            int cg2 = (int) Math.min(255, green * lightMult2);
            int cb2 = (int) Math.min(255, blue * lightMult2);

            for (int j = 0; j < slices; j++) {
                double a1 = (j * Math.PI * 2.0) / slices;
                double a2 = ((j + 1) * Math.PI * 2.0) / slices;

                float x11 = (float) (Math.cos(a1) * r1);
                float z11 = (float) (Math.sin(a1) * r1);
                float x12 = (float) (Math.cos(a2) * r1);
                float z12 = (float) (Math.sin(a2) * r1);

                float x21 = (float) (Math.cos(a1) * r2);
                float z21 = (float) (Math.sin(a1) * r2);
                float x22 = (float) (Math.cos(a2) * r2);
                float z22 = (float) (Math.sin(a2) * r2);

                builder.addVertex(matrix, x11, y1, z11).setColor(cr1, cg1, cb1, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x11, y1, z11);
                builder.addVertex(matrix, x21, y2, z21).setColor(cr2, cg2, cb2, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x21, y2, z21);
                builder.addVertex(matrix, x22, y2, z22).setColor(cr2, cg2, cb2, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x22, y2, z22);
                builder.addVertex(matrix, x12, y1, z12).setColor(cr1, cg1, cb1, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x12, y1, z12);
            }
        }
    }

    private void renderCueStick(PoseStack poseStack, VertexConsumer builder, float cueX, float cueZ, float aimAngle, float power, int packedLight) {
        poseStack.pushPose();

        float pullback = 0.04F + power * 0.18F;
        poseStack.translate(cueX, PoolBall.RADIUS * 1.1F, cueZ);
        poseStack.mulPose(Axis.YP.rotation(aimAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(8.0F));
        poseStack.translate(0.0D, 0.0D, -pullback);

        Matrix4f mat = poseStack.last().pose();
        float stickLen = 1.15F;
        float tipR = 0.008F;
        float buttR = 0.016F;

        renderCylinder(mat, builder, 0.0F, -0.03F, tipR, tipR, 0xFF, 0xFF, 0xFF, packedLight);
        renderCylinder(mat, builder, 0.0F, -0.006F, tipR, tipR, 0x1E, 0x88, 0xE5, packedLight);

        renderCylinder(mat, builder, -0.03F, -0.75F, tipR, (tipR + buttR) * 0.5F, 0xDE, 0xB8, 0x87, packedLight);

        renderCylinder(mat, builder, -0.75F, -stickLen, (tipR + buttR) * 0.5F, buttR, 0x21, 0x21, 0x21, packedLight);

        poseStack.popPose();
    }

    private void renderCylinder(Matrix4f matrix, VertexConsumer builder, float z1, float z2, float r1, float r2, int red, int green, int blue, int packedLight) {
        int sides = 8;
        for (int i = 0; i < sides; i++) {
            double a1 = (i * Math.PI * 2.0) / sides;
            double a2 = ((i + 1) * Math.PI * 2.0) / sides;

            float x11 = (float) (Math.cos(a1) * r1);
            float y11 = (float) (Math.sin(a1) * r1);
            float x12 = (float) (Math.cos(a2) * r1);
            float y12 = (float) (Math.sin(a2) * r1);

            float x21 = (float) (Math.cos(a1) * r2);
            float y21 = (float) (Math.sin(a1) * r2);
            float x22 = (float) (Math.cos(a2) * r2);
            float y22 = (float) (Math.sin(a2) * r2);

            builder.addVertex(matrix, x11, y11, z1).setColor(red, green, blue, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x11, y11, 0);
            builder.addVertex(matrix, x21, y21, z2).setColor(red, green, blue, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x21, y21, 0);
            builder.addVertex(matrix, x22, y22, z2).setColor(red, green, blue, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x22, y22, 0);
            builder.addVertex(matrix, x12, y12, z1).setColor(red, green, blue, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x12, y12, 0);
        }
    }
}
