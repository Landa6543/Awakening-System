package com.soybe.awakeningsystem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.tower.TowerWayUpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * A blue column of light over the way up of the tower floor the player is on, seen from anywhere on the floor.
 * Further than the land is drawn, it is drawn nearer, in the portal's direction, so the fog never hides it.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class TowerWayUpBeam {
    private TowerWayUpBeam() {}

    private static Vec3 target;

    public static void set(TowerWayUpPayload p) {
        target = p.has() ? new Vec3(p.x() + 0.5, p.y(), p.z() + 0.5) : null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || target == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Vec3 cam = event.getCamera().getPosition();
        double dx = target.x - cam.x, dz = target.z - cam.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 12) return;   // standing at it: no need
        double reach = Math.max(48, Math.min(120, mc.options.getEffectiveRenderDistance() * 16 * 0.75));
        double k = dist > reach ? reach / dist : 1.0;
        double bx = cam.x + dx * k, bz = cam.z + dz * k;
        double by = target.y + 3;
        // wider the further it is, so it stays a clear line on the horizon
        float width = (float) Math.max(0.3, Math.min(6.0, dist * k * 0.03));
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(bx - cam.x - 0.5, by - cam.y, bz - cam.z - 0.5);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        long time = mc.level.getGameTime();
        BeaconRenderer.renderBeaconBeam(pose, buffers, BeaconRenderer.BEAM_LOCATION, event.getPartialTick().getGameTimeDeltaPartialTick(false),
                1.0F, time, 0, 220, 0xFF6FB8FF, width, width * 1.25F);
        buffers.endBatch();
        pose.popPose();
    }
}
