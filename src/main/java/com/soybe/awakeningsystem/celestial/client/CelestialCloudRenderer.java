package com.soybe.awakeningsystem.celestial.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.celestial.CelestialCloudEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The nebula turns slowly and the skull cloud plays its ten frames; both always face the camera and fade in and out. */
public class CelestialCloudRenderer extends EntityRenderer<CelestialCloudEntity> {
    private static final ResourceLocation NEBULA = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/celestial/nebula_cloud.png");
    private static final ResourceLocation SKULLS = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/celestial/vehemence_skull.png");
    private static final int SKULL_FRAMES = 10;

    public CelestialCloudRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CelestialCloudEntity e, float entityYaw, float pt, PoseStack pose, MultiBufferSource buf, int light) {
        float age = e.tickCount + pt, r = e.radius();
        float fade = Mth.clamp(Math.min(age / 8.0F, (e.life() - age) / 12.0F), 0.0F, 1.0F);
        int alpha = (int) (fade * 230);
        if (alpha <= 2) return;
        pose.pushPose();
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        if (e.kind() == CelestialCloudEntity.NEBULA) {
            pose.mulPose(Axis.ZP.rotationDegrees(age * 3));
            float s = r * 2.2F * (0.85F + 0.15F * fade);
            CelestialBoltRenderer.quad(pose, buf.getBuffer(RenderType.entityTranslucent(NEBULA)), s, s, 0, 1, (alpha << 24) | 0xFFFFFF);
        } else {
            int frame = ((int) (age / 3)) % SKULL_FRAMES;
            float w = r * 1.5F;
            CelestialBoltRenderer.quad(pose, buf.getBuffer(RenderType.entityTranslucent(SKULLS)), w, w * 27 / 20F,
                    frame / (float) SKULL_FRAMES, (frame + 1) / (float) SKULL_FRAMES, (alpha << 24) | 0xFFFFFF);
        }
        pose.popPose();
        super.render(e, entityYaw, pt, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CelestialCloudEntity e) {
        return e.kind() == CelestialCloudEntity.NEBULA ? NEBULA : SKULLS;
    }
}
