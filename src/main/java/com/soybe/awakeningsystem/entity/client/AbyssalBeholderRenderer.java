package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalBeholderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AbyssalBeholderRenderer extends MobRenderer<AbyssalBeholderEntity, AbyssalBeholderModel<AbyssalBeholderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/abyssal_beholder.png");
    private static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace(
            "textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(BEAM_LOCATION);

    public AbyssalBeholderRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssalBeholderModel<>(context.bakeLayer(AbyssalBeholderModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new AbyssalBeholderGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalBeholderEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AbyssalBeholderEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public void render(AbyssalBeholderEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        LivingEntity target = entity.getBeamTarget();
        if (entity.isChannelingBeam() && target != null) {
            renderVoidBeam(entity, target, partialTicks, poseStack, buffer);
        }
    }

    private void renderVoidBeam(AbyssalBeholderEntity entity, LivingEntity target, float partialTicks,
                                PoseStack poseStack, MultiBufferSource buffer) {
        float gameTime = (float)entity.level().getGameTime() + partialTicks;
        float vScroll = gameTime * 0.5F % 1.0F;
        float eyeHeight = entity.getEyeHeight();

        poseStack.pushPose();
        poseStack.translate(0.0F, eyeHeight, 0.0F);

        Vec3 targetPos = getTargetInterpolatedPos(target, partialTicks);
        Vec3 beholderPos = getTargetInterpolatedPos(entity, partialTicks).add(0.0D, eyeHeight, 0.0D);
        Vec3 diff = targetPos.subtract(beholderPos);
        float distance = (float)(diff.length() + 0.1D);
        diff = diff.normalize();

        float pitch = (float)Math.acos(diff.y);
        float yaw = (float)Math.atan2(diff.z, diff.x);

        poseStack.mulPose(Axis.YP.rotationDegrees((((float)Math.PI / 2F) - yaw) * (180F / (float)Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * (180F / (float)Math.PI)));

        VertexConsumer consumer = buffer.getBuffer(BEAM_RENDER_TYPE);
        PoseStack.Pose lastPose = poseStack.last();

        float beamRadius = 0.18F;
        // Tinte púrpura cósmico abisal
        int r = 230;
        int g = 40;
        int b = 170;
        int alpha = 240;

        for (int i = 0; i < 4; i++) {
            float rot = i * ((float)Math.PI / 2.0F);
            float cos = Mth.cos(rot) * beamRadius;
            float sin = Mth.sin(rot) * beamRadius;
            float nextCos = Mth.cos(rot + ((float)Math.PI / 2.0F)) * beamRadius;
            float nextSin = Mth.sin(rot + ((float)Math.PI / 2.0F)) * beamRadius;

            float u0 = 0.0F;
            float u1 = 0.5F;
            float v0 = -1.0F + vScroll;
            float v1 = distance * 0.5F + v0;

            addQuadVertex(consumer, lastPose, cos, distance, sin, r, g, b, alpha, u1, v1);
            addQuadVertex(consumer, lastPose, cos, 0.0F, sin, r, g, b, alpha, u1, v0);
            addQuadVertex(consumer, lastPose, nextCos, 0.0F, nextSin, r, g, b, alpha, u0, v0);
            addQuadVertex(consumer, lastPose, nextCos, distance, nextSin, r, g, b, alpha, u0, v1);
        }

        poseStack.popPose();
    }

    private static void addQuadVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                      float x, float y, float z,
                                      int r, int g, int b, int a,
                                      float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // Brillo total
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private Vec3 getTargetInterpolatedPos(LivingEntity entity, float partialTicks) {
        double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTicks, entity.yOld, entity.getY()) + entity.getBbHeight() * 0.5D;
        double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        return new Vec3(x, y, z);
    }
}
