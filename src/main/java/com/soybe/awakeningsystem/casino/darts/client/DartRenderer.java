package com.soybe.awakeningsystem.casino.darts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.casino.darts.DartEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class DartRenderer extends EntityRenderer<DartEntity> {

    private static final ResourceLocation DART_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/item/dart.png");

    public DartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.05F;
    }

    @Override
    public void render(DartEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (entity.isStuck()) {
            Direction facing = entity.getStuckFacing();
            switch (facing) {
                case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
                case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                case UP    -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                case DOWN  -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(-4.0F));
        } else {
            float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        }

        poseStack.scale(0.42F, 0.42F, 0.42F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(DART_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        renderBox(matrix, consumer, -0.015F, -0.015F, -0.65F, 0.015F, 0.015F, -0.30F, 220, 225, 235, 255, packedLight);

        renderBox(matrix, consumer, -0.042F, -0.042F, -0.30F, 0.042F, 0.042F, 0.15F, 235, 185, 45, 255, packedLight);
        renderBox(matrix, consumer, -0.044F, -0.044F, -0.15F, 0.044F, 0.044F, -0.10F, 140, 105, 25, 255, packedLight);
        renderBox(matrix, consumer, -0.044F, -0.044F, 0.00F, 0.044F, 0.044F, 0.05F, 140, 105, 25, 255, packedLight);

        renderBox(matrix, consumer, -0.022F, -0.022F, 0.15F, 0.022F, 0.022F, 0.45F, 35, 40, 48, 255, packedLight);

        renderFin(matrix, consumer, 0.0F, -0.18F, 0.35F, 0.0F, 0.18F, 0.75F, 220, 30, 45, 255, packedLight);
        renderFin(matrix, consumer, -0.18F, 0.0F, 0.35F, 0.18F, 0.0F, 0.75F, 220, 30, 45, 255, packedLight);

        renderBox(matrix, consumer, -0.005F, 0.16F, 0.70F, 0.005F, 0.18F, 0.76F, 255, 215, 60, 255, packedLight);
        renderBox(matrix, consumer, -0.005F, -0.18F, 0.70F, 0.005F, -0.16F, 0.76F, 255, 215, 60, 255, packedLight);
        renderBox(matrix, consumer, 0.16F, -0.005F, 0.70F, 0.18F, 0.005F, 0.76F, 255, 215, 60, 255, packedLight);
        renderBox(matrix, consumer, -0.18F, -0.005F, 0.70F, -0.16F, 0.005F, 0.76F, 255, 215, 60, 255, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderBox(Matrix4f matrix, VertexConsumer consumer,
                           float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ,
                           int r, int g, int b, int a, int light) {
        addQuad(matrix, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, 0, 0, -1, r, g, b, a, light);
        addQuad(matrix, consumer, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, 0, 0, 1, r, g, b, a, light);
        addQuad(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0, 1, 0, r, g, b, a, light);
        addQuad(matrix, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, 0, -1, 0, r, g, b, a, light);
        addQuad(matrix, consumer, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, -1, 0, 0, r, g, b, a, light);
        addQuad(matrix, consumer, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 1, 0, 0, r, g, b, a, light);
    }

    private void renderFin(Matrix4f matrix, VertexConsumer consumer,
                           float x1, float y1, float z1,
                           float x2, float y2, float z2,
                           int r, int g, int b, int a, int light) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);

        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, -1, 0);
    }

    private void addQuad(Matrix4f matrix, VertexConsumer consumer,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float nx, float ny, float nz,
                         int r, int g, int b, int a, int light) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(DartEntity entity) {
        return DART_TEXTURE;
    }
}
