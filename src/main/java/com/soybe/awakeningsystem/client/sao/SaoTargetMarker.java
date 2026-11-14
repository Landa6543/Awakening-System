package com.soybe.awakeningsystem.client.sao;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * With the full-dive style on, a crystal cursor floats over the creature the player is looking at (or the
 * last one they hit): an upside-down diamond that turns and bobs, red for enemies and green for the rest,
 * with a faint glow around it. It fades in and out instead of popping.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class SaoTargetMarker {
    private SaoTargetMarker() {}

    private static Entity target;
    private static float fade;
    private static long lastFrame;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !UiStyle.fullDive() || mc.options.hideGui) return;

        Entity now = pick(mc);
        long t = System.currentTimeMillis();
        float dt = lastFrame == 0 ? 0.016F : Math.min(0.1F, (t - lastFrame) / 1000.0F);
        lastFrame = t;
        if (now != null && now != target) {
            target = now;
            fade = Math.min(fade, 0.2F);
        }
        fade = Mth.clamp(fade + (now != null ? dt * 5.0F : -dt * 4.0F), 0.0F, 1.0F);
        if (target == null || fade <= 0.0F || target.isRemoved() || !target.isAlive()) {
            if (fade <= 0.0F) target = null;
            return;
        }

        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cam = event.getCamera().getPosition();
        double x = Mth.lerp(partial, target.xOld, target.getX()) - cam.x;
        double y = Mth.lerp(partial, target.yOld, target.getY()) - cam.y;
        double z = Mth.lerp(partial, target.zOld, target.getZ()) - cam.z;
        float age = (mc.player.tickCount + partial);
        float bob = Mth.sin(age * 0.12F) * 0.06F;
        float scale = Mth.clamp(target.getBbWidth() * 0.6F, 0.6F, 2.2F);

        PoseStack ps = event.getPoseStack();
        ps.pushPose();
        ps.translate(x, y + target.getBbHeight() + 0.45F * scale + bob, z);
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(age * 3.0F));
        ps.scale(scale, scale, scale);
        Matrix4f m = ps.last().pose();

        boolean enemy = target instanceof Enemy;
        int base = enemy ? 0xE8341F : 0x3FD15A;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer solid = buffers.getBuffer(RenderType.debugQuads());
        diamond(solid, m, 0.15F, 0.11F, 0.30F, base, (int) (235 * fade), true);
        buffers.endBatch(RenderType.debugQuads());
        VertexConsumer glow = buffers.getBuffer(RenderType.lightning());
        diamond(glow, m, 0.21F, 0.16F, 0.40F, base, (int) (70 * fade), false);
        buffers.endBatch(RenderType.lightning());
        ps.popPose();
    }

    /** What the player is aiming at, or what they hit in the last five seconds, within 32 blocks. */
    private static Entity pick(Minecraft mc) {
        Entity aim = mc.crosshairPickEntity;
        if (aim instanceof LivingEntity le && le.isAlive() && !le.isInvisible()) return aim;
        LivingEntity hit = mc.player.getLastHurtMob();
        if (hit != null && hit.isAlive() && mc.player.tickCount - mc.player.getLastHurtMobTimestamp() < 100
                && hit.distanceToSqr(mc.player) < 32 * 32) return hit;
        return null;
    }

    /**
     * An upside-down diamond: a short pyramid on top, a long point below, eight faces. Faces are shaded by
     * which way they lean, so it reads as solid.
     */
    private static void diamond(VertexConsumer vc, Matrix4f m, float r, float up, float down, int rgb, int alpha, boolean shade) {
        float[][] rim = {{r, 0}, {0, r}, {-r, 0}, {0, -r}};
        for (int i = 0; i < 4; i++) {
            float[] a = rim[i], b = rim[(i + 1) % 4];
            float light = shade ? 0.75F + 0.25F * (float) Math.cos(i * Math.PI / 2 + 0.6) : 1.0F;
            tri(vc, m, 0, up, 0, a[0], 0, a[1], b[0], 0, b[1], rgb, Math.min(1.0F, light + 0.2F), alpha);
            tri(vc, m, 0, -down, 0, b[0], 0, b[1], a[0], 0, a[1], rgb, light * 0.8F, alpha);
        }
    }

    private static void tri(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1,
                            float x2, float y2, float z2, int rgb, float light, int alpha) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * light));
        int g = Math.min(255, (int) (((rgb >> 8) & 255) * light));
        int b = Math.min(255, (int) ((rgb & 255) * light));
        vc.addVertex(m, x0, y0, z0).setColor(r, g, b, alpha);
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, alpha);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, alpha);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, alpha);
    }
}
