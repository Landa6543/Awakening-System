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
    private static final ResourceLocation SLASH_INK_WUXIA =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_ink_wuxia.png");
    private static final ResourceLocation SLASH_SHADOW_CLAW =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_shadow_claw.png");
    private static final ResourceLocation SLASH_HEAVY_CLEAVE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_heavy_cleave.png");
    private static final ResourceLocation SLASH_CELESTIAL =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_celestial_blade.png");
    private static final ResourceLocation SLASH_SCYTHE_REAP =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_scythe_reap.png");
    private static final ResourceLocation SLASH_IMPACT_BURST =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/vfx/slash_impact_burst.png");

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

        ResourceLocation tex = getTextureLocation(entity);
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(tex));

        int r, g, b;
        switch (entity.getSlashType()) {
            case SlashWaveEntity.TYPE_DARK_VOID -> { r = 145; g = 35; b = 225; }
            case SlashWaveEntity.TYPE_CRYSTAL_AQUA -> { r = 70; g = 230; b = 255; }
            case SlashWaveEntity.TYPE_SOLAR_LIGHT -> { r = 255; g = 240; b = 130; }
            case SlashWaveEntity.TYPE_CRIMSON_BLOOD -> { r = 245; g = 25; b = 45; }
            case SlashWaveEntity.TYPE_SILVER_ROSE -> { r = 245; g = 225; b = 245; }
            case SlashWaveEntity.TYPE_CHERRY_BLOSSOM -> { r = 255; g = 135; b = 185; }
            case SlashWaveEntity.TYPE_COSMIC_AMETHYST -> { r = 175; g = 75; b = 255; }
            case SlashWaveEntity.TYPE_PYRO_FLAME -> { r = 255; g = 110; b = 20; }
            case SlashWaveEntity.TYPE_FROST_WIND -> { r = 165; g = 230; b = 255; }
            case SlashWaveEntity.TYPE_CELESTIAL -> { r = 90; g = 235; b = 255; }
            case SlashWaveEntity.TYPE_PRIMORDIAL_GENESIS -> { r = 210; g = 85; b = 255; }
            case SlashWaveEntity.TYPE_EARTH -> { r = 215; g = 110; b = 35; }
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

        int cr = Math.min(255, r + 65);
        int cg = Math.min(255, g + 65);
        int cb = Math.min(255, b + 65);
        int ca = (int) (a * 0.9F);

        int style = entity.getSlashStyle();

        if (entity.isCross() || style == SlashWaveEntity.STYLE_CROSS) {
            // Dual Crossing Cut (+32 and -32 deg)
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(-32.0F));
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL, halfW, r, g, b, a, cr, cg, cb, ca);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(32.0F));
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL, halfW, r, g, b, a, cr, cg, cb, ca);
            poseStack.popPose();
        } else if (style == SlashWaveEntity.STYLE_THRUST) {
            // Piercing Spiral Thrust (orthogonal planes)
            poseStack.pushPose();
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL, halfW, r, g, b, a, cr, cg, cb, ca);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL, halfW, r, g, b, a, cr, cg, cb, ca);
            poseStack.popPose();
        } else {
            // Volumetric cutting blade: Primary angled plane + secondary cross plane + vertical energy spine for 100% 3D visibility
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(-16.0F));
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL, halfW, r, g, b, a, cr, cg, cb, ca);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(22.0F));
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL * 0.90F, halfW * 0.90F, r, g, b, (int)(a * 0.82F), cr, cg, cb, ca);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            renderDoubleSidedQuad(builder, poseStack.last().pose(), halfL * 0.75F, halfW * 0.40F, r, g, b, (int)(a * 0.55F), cr, cg, cb, ca);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderDoubleSidedQuad(VertexConsumer builder, Matrix4f matrix,
                                             float halfL, float halfW,
                                             int r, int g, int b, int a,
                                             int cr, int cg, int cb, int ca) {
        // Outer Blade Layer - Top Face
        drawVertex(builder, matrix, -halfL, 0.0F, -halfW, 0.0F, 1.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, -halfW, 0.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, halfW, 1.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, -halfL, 0.0F, halfW, 1.0F, 1.0F, r, g, b, a);

        // Outer Blade Layer - Bottom Face (Reverse winding for 100% two-sided visibility)
        drawVertex(builder, matrix, -halfL, 0.0F, halfW, 1.0F, 1.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, halfW, 1.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, halfL, 0.0F, -halfW, 0.0F, 0.0F, r, g, b, a);
        drawVertex(builder, matrix, -halfL, 0.0F, -halfW, 0.0F, 1.0F, r, g, b, a);

        // Inner Emissive Core (Luminous Ridge)
        float coreW = halfW * 0.65F;
        float coreL = halfL * 0.65F;

        drawVertex(builder, matrix, -coreL, 0.008F, -coreW, 0.0F, 1.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, 0.008F, -coreW, 0.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, 0.008F, coreW, 1.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, -coreL, 0.008F, coreW, 1.0F, 1.0F, cr, cg, cb, ca);

        drawVertex(builder, matrix, -coreL, -0.008F, coreW, 1.0F, 1.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, -0.008F, coreW, 1.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, coreL, -0.008F, -coreW, 0.0F, 0.0F, cr, cg, cb, ca);
        drawVertex(builder, matrix, -coreL, -0.008F, -coreW, 0.0F, 1.0F, cr, cg, cb, ca);
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
        return switch (entity.getSlashStyle()) {
            case SlashWaveEntity.STYLE_CROSS -> SLASH_CROSS;
            case SlashWaveEntity.STYLE_THRUST -> SLASH_THRUST;
            case SlashWaveEntity.STYLE_INK_WUXIA -> SLASH_INK_WUXIA;
            case SlashWaveEntity.STYLE_SHADOW_CLAW -> SLASH_SHADOW_CLAW;
            case SlashWaveEntity.STYLE_HEAVY_CLEAVE -> SLASH_HEAVY_CLEAVE;
            case SlashWaveEntity.STYLE_CELESTIAL -> SLASH_CELESTIAL;
            case SlashWaveEntity.STYLE_SCYTHE_REAP -> SLASH_SCYTHE_REAP;
            default -> (entity.isCross() ? SLASH_CROSS : (entity.getSlashType() == SlashWaveEntity.TYPE_SOLAR_LIGHT ? SLASH_THRUST : SLASH_CRESCENT));
        };
    }
}
