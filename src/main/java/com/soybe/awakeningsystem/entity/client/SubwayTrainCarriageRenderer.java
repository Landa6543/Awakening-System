package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.entity.SubwayTrainCarriageEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renderer tridimensional para el Vagón de Metro Funcional (ORV Coche 3707 - Línea 3 de Seúl).
 * Proporciones de 3 bloques de ancho para vías grandes.
 * Carrocería de acero inoxidable cepillado (sin andesita), franjas naranjas de la Línea 3,
 * ventanales panorámicos de cristal tintado, faros LED delanteros, bogies ferroviarios sobre rieles
 * y cabina con consola de conducción integrada.
 */
public class SubwayTrainCarriageRenderer extends EntityRenderer<SubwayTrainCarriageEntity> {

    private static final ResourceLocation TEX_METAL = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/block/subway_stainless_steel.png");
    private static final ResourceLocation TEX_ORANGE = ResourceLocation.withDefaultNamespace("textures/block/orange_concrete.png");
    private static final ResourceLocation TEX_DARK = ResourceLocation.withDefaultNamespace("textures/block/polished_blackstone.png");
    private static final ResourceLocation TEX_GLASS = ResourceLocation.withDefaultNamespace("textures/block/tinted_glass.png");
    private static final ResourceLocation TEX_HEADLIGHT = ResourceLocation.withDefaultNamespace("textures/block/sea_lantern.png");
    private static final ResourceLocation TEX_WHEEL = ResourceLocation.withDefaultNamespace("textures/block/iron_block.png");
    private static final ResourceLocation TEX_SEAT_BLUE = ResourceLocation.withDefaultNamespace("textures/block/blue_concrete.png");
    private static final ResourceLocation TEX_CONTROLLER = ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/block/subway_train_controller.png");

    public SubwayTrainCarriageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.8F;
    }

    @Override
    public ResourceLocation getTextureLocation(SubwayTrainCarriageEntity entity) {
        return TEX_METAL;
    }

    @Override
    public void render(SubwayTrainCarriageEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));

        Matrix4f m = poseStack.last().pose();

        float halfW = 1.46F;
        float halfL = 4.40F;
        float floorY = 0.45F;
        float roofY = 2.80F;
        float[] bogieZ = new float[] { -2.80F, 2.80F };

        VertexConsumer darkBuf = bufferSource.getBuffer(RenderType.entitySolid(TEX_DARK));
        renderBox(m, darkBuf, -halfW, 0.18F, -halfL, halfW, floorY, halfL, packedLight);
        renderBox(m, darkBuf, -halfW * 0.75F, roofY, -halfL * 0.90F, halfW * 0.75F, roofY + 0.18F, halfL * 0.90F, packedLight);
        for (float bz : bogieZ) {
            renderBox(m, darkBuf, -halfW * 0.85F, 0.10F, bz - 0.20F, halfW * 0.85F, 0.26F, bz + 0.20F, packedLight);
        }

        VertexConsumer wheelBuf = bufferSource.getBuffer(RenderType.entitySolid(TEX_WHEEL));
        for (float bz : bogieZ) {
            renderBox(m, wheelBuf, -1.15F, 0.00F, bz - 0.40F, -0.85F, 0.50F, bz + 0.40F, packedLight);
            renderBox(m, wheelBuf, 0.85F, 0.00F, bz - 0.40F, 1.15F, 0.50F, bz + 0.40F, packedLight);
        }

        VertexConsumer metalBuf = bufferSource.getBuffer(RenderType.entitySolid(TEX_METAL));
        renderBox(m, metalBuf, -halfW, floorY, -halfL, -halfW + 0.10F, floorY + 0.75F, halfL, packedLight);
        renderBox(m, metalBuf, halfW - 0.10F, floorY, -halfL, halfW, floorY + 0.75F, halfL, packedLight);
        renderBox(m, metalBuf, -halfW, floorY, -halfL, halfW, roofY, -halfL + 0.15F, packedLight);
        renderBox(m, metalBuf, -halfW, floorY, halfL - 0.30F, halfW, floorY + 0.95F, halfL, packedLight);
        renderBox(m, metalBuf, -halfW, roofY - 0.18F, -halfL, halfW, roofY, halfL, packedLight);

        VertexConsumer orangeBuf = bufferSource.getBuffer(RenderType.entitySolid(TEX_ORANGE));
        renderBox(m, orangeBuf, -halfW - 0.01F, floorY + 0.75F, -halfL, -halfW + 0.11F, floorY + 1.00F, halfL, packedLight);
        renderBox(m, orangeBuf, halfW - 0.11F, floorY + 0.75F, -halfL, halfW + 0.01F, floorY + 1.00F, halfL, packedLight);
        renderBox(m, orangeBuf, -halfW, floorY + 0.95F, halfL - 0.30F, halfW, floorY + 1.20F, halfL, packedLight);

        VertexConsumer seatBuf = bufferSource.getBuffer(RenderType.entitySolid(TEX_SEAT_BLUE));
        renderBox(m, seatBuf, -halfW + 0.12F, floorY + 0.10F, -2.5F, -halfW + 0.65F, floorY + 0.40F, 1.5F, packedLight);
        renderBox(m, seatBuf, halfW - 0.65F, floorY + 0.10F, -2.5F, halfW - 0.12F, floorY + 0.40F, 1.5F, packedLight);

        VertexConsumer ctrlBuf = bufferSource.getBuffer(RenderType.entitySolid(TEX_CONTROLLER));
        renderBox(m, ctrlBuf, -0.40F, floorY + 0.10F, halfL - 1.10F, 0.40F, floorY + 0.85F, halfL - 0.40F, packedLight);

        VertexConsumer lightBuf = bufferSource.getBuffer(RenderType.entityTranslucent(TEX_HEADLIGHT));
        renderBox(m, lightBuf, -halfW + 0.25F, floorY + 0.50F, halfL - 0.02F, -halfW + 0.60F, floorY + 0.80F, halfL + 0.02F, LightTexture.FULL_BRIGHT);
        renderBox(m, lightBuf, halfW - 0.60F, floorY + 0.50F, halfL - 0.02F, halfW - 0.25F, floorY + 0.80F, halfL + 0.02F, LightTexture.FULL_BRIGHT);

        VertexConsumer glassBuf = bufferSource.getBuffer(RenderType.entityTranslucent(TEX_GLASS));
        renderBox(m, glassBuf, -halfW + 0.02F, floorY + 1.00F, -halfL + 0.4F, -halfW + 0.08F, roofY - 0.20F, halfL - 0.4F, packedLight);
        renderBox(m, glassBuf, halfW - 0.08F, floorY + 1.00F, -halfL + 0.4F, halfW - 0.02F, roofY - 0.20F, halfL - 0.4F, packedLight);
        renderBox(m, glassBuf, -halfW + 0.20F, floorY + 1.20F, halfL - 0.28F, halfW - 0.20F, roofY - 0.20F, halfL - 0.22F, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderBox(Matrix4f m, VertexConsumer b,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2, int light) {
        addQuad(m, b, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, light, 0, -1, 0);
        addQuad(m, b, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, light, 0, 1, 0);
        addQuad(m, b, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, light, 0, 0, -1);
        addQuad(m, b, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, light, 0, 0, 1);
        addQuad(m, b, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, light, -1, 0, 0);
        addQuad(m, b, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, light, 1, 0, 0);
    }

    private static void addQuad(Matrix4f m, VertexConsumer b,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float x4, float y4, float z4,
                                int light, float nx, float ny, float nz) {
        b.addVertex(m, x1, y1, z1).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        b.addVertex(m, x2, y2, z2).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        b.addVertex(m, x3, y3, z3).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        b.addVertex(m, x4, y4, z4).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
    }
}
