package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SlashWaveEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class SlashWaveRenderer extends EntityRenderer<SlashWaveEntity> {

    private static final ResourceLocation SLASH_CRESCENT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_crescent.png");
    private static final ResourceLocation SLASH_THRUST =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_thrust.png");
    private static final ResourceLocation SLASH_CROSS =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_cross.png");

    public SlashWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SlashWaveEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, 0.0D);

        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot());
        float roll = entity.getRoll();

        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.XP.rotationDegrees(roll));

        ResourceLocation tex = entity.isCross()
                ? SLASH_CROSS
                : (entity.getSlashType() == SlashWaveEntity.TYPE_SOLAR_LIGHT ? SLASH_THRUST : SLASH_CRESCENT);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(tex));

        int r, g, b;
        switch (entity.getSlashType()) {
            case SlashWaveEntity.TYPE_DARK_VOID -> { r = 130; g = 30; b = 210; }
            case SlashWaveEntity.TYPE_CRYSTAL_AQUA -> { r = 70; g = 230; b = 255; }
            case SlashWaveEntity.TYPE_SOLAR_LIGHT -> { r = 255; g = 240; b = 130; }
            case SlashWaveEntity.TYPE_CRIMSON_BLOOD -> { r = 245; g = 25; b = 45; }
            case SlashWaveEntity.TYPE_SILVER_ROSE -> { r = 245; g = 225; b = 245; }
            case SlashWaveEntity.TYPE_CHERRY_BLOSSOM -> { r = 255; g = 135; b = 185; }
            case SlashWaveEntity.TYPE_COSMIC_AMETHYST -> { r = 160; g = 60; b = 255; }
            case SlashWaveEntity.TYPE_PYRO_FLAME -> { r = 255; g = 110; b = 20; }
            case SlashWaveEntity.TYPE_FROST_WIND -> { r = 165; g = 230; b = 255; }
            case SlashWaveEntity.TYPE_CELESTIAL -> { r = 90; g = 235; b = 255; }
            case SlashWaveEntity.TYPE_PRIMORDIAL_GENESIS -> { r = 195; g = 65; b = 255; }
            default -> { r = 255; g = 255; b = 255; }
        }

        int life = entity.getLifetime();
        int age = entity.tickCount;
        float alphaFactor = 1.0F;
        if (age < 2) {
            alphaFactor = (age + partialTick) / 2.0F;
        } else if (age > life - 5) {
            alphaFactor = Math.max(0.0F, (life - (age + partialTick)) / 5.0F);
        }
        int a = (int) (245 * alphaFactor);

        float halfW = entity.getSlashWidth() * 0.5F;
        float halfL = entity.getSlashHeight() * 0.5F;

        Matrix4f matrix = poseStack.last().pose();

        drawVertex(builder, matrix, -halfL, 0.0F, -halfW, 0.0F, 1.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, -halfW, 0.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, halfW, 1.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, -halfL, 0.0F, halfW, 1.0F, 1.0F, r, g, b, a);

        drawVertex(builder, matrix, -halfL, 0.0F, halfW, 1.0F, 1.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, halfW, 1.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, -halfW, 0.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, -halfL, 0.0F, -halfW, 0.0F, 1.0F, r, g, b, a);

        float coreW = halfW * 0.65F;
        float coreL = halfL * 0.65F;
        int cr = Math.min(255, r + 60);
        int cg = Math.min(255, g + 60);
        int cb = Math.min(255, b + 60);
        int ca = (int) (a * 0.9F);

        drawVertex(builder, matrix, -coreL, 0.008F, -coreW, 0.0F, 1.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, 0.008F, -coreW, 0.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, 0.008F, coreW, 1.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, -coreL, 0.008F, coreW, 1.0F, 1.0F, cr, cg, cb, ca);

        drawVertex(builder, matrix, -coreL, -0.008F, coreW, 1.0F, 1.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, -0.008F, coreW, 1.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, -0.008F, -coreW, 0.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, -coreL, -0.008F, -coreW, 0.0F, 1.0F, cr, cg, cb, ca);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void drawVertex(VertexConsumer builder, Matrix4f matrix,
                                   float x, float y, float z,
                                   float u, float v,
                                   int r, int g, int b, int a) {
        builder.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SlashWaveEntity entity) {
        return entity.isCross()
                ? SLASH_CROSS
                : (entity.getSlashType() == SlashWaveEntity.TYPE_SOLAR_LIGHT ? SLASH_THRUST : SLASH_CRESCENT);
    }
}
