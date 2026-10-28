package com.soybe.awakeningsystem.client.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.item.TrackMarkerWandItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Previsualizador holográfico 3D de circuitos para la Varita Trazadora de Pistas (Track Marker Wand).
 * Dibuja líneas láser volumétricas, balizas verticales y circuitos cerrados en tiempo real mientras
 * el jugador sostiene la varita topográfica de Tracen.
 */
public final class RacetrackWandPreview {

    private RacetrackWandPreview() {}

    public static boolean isHoldingWand(Player player) {
        if (player == null || SystemItems.TRACK_MARKER_WAND == null || !SystemItems.TRACK_MARKER_WAND.isBound()) {
            return false;
        }
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return (main.is(SystemItems.TRACK_MARKER_WAND.get()) || off.is(SystemItems.TRACK_MARKER_WAND.get()));
    }

    public static ItemStack getWandItem(Player player) {
        if (player == null || SystemItems.TRACK_MARKER_WAND == null || !SystemItems.TRACK_MARKER_WAND.isBound()) {
            return ItemStack.EMPTY;
        }
        ItemStack main = player.getMainHandItem();
        if (main.is(SystemItems.TRACK_MARKER_WAND.get())) return main;
        ItemStack off = player.getOffhandItem();
        if (off.is(SystemItems.TRACK_MARKER_WAND.get())) return off;
        return ItemStack.EMPTY;
    }

    public static void renderPreview(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!isHoldingWand(mc.player)) return;

        ItemStack wand = getWandItem(mc.player);
        if (wand.isEmpty()) return;

        List<BlockPos> waypoints = TrackMarkerWandItem.getWaypoints(wand);

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float gameTime = mc.level.getGameTime() + partialTick;
        Vec3 camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();

        float pulse = 0.85F + 0.15F * (float) Math.sin(gameTime * 0.25F);
        int alphaSolid = (int) (220 * pulse);
        int alphaLine = (int) (180 * pulse);
        int alphaLoop = (int) (140 * pulse);

        VertexConsumer beamBuffer = bufferSource.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();

        for (int i = 0; i < waypoints.size(); i++) {
            BlockPos p = waypoints.get(i);
            double px = p.getX() + 0.5D - camPos.x;
            double py = p.getY() + 0.15D - camPos.y;
            double pz = p.getZ() + 0.5D - camPos.z;

            int r = (i == 0) ? 255 : ((i % 2 == 0) ? 46 : 0);
            int g = (i == 0) ? 215 : ((i % 2 == 0) ? 204 : 229);
            int b = (i == 0) ? 0 : ((i % 2 == 0) ? 113 : 255);

            drawHologramDisc(mat, beamBuffer, px, py, pz, (i == 0) ? 0.75F : 0.5F, r, g, b, alphaSolid);

            drawBeamSegment(mat, beamBuffer, px, py, pz, px, py + 2.5D, pz, 0.14F, r, g, b, (int) (alphaSolid * 0.75F));
        }

        for (int i = 0; i < waypoints.size() - 1; i++) {
            BlockPos p1 = waypoints.get(i);
            BlockPos p2 = waypoints.get(i + 1);

            double x1 = p1.getX() + 0.5D - camPos.x;
            double y1 = p1.getY() + 0.25D - camPos.y;
            double z1 = p1.getZ() + 0.5D - camPos.z;

            double x2 = p2.getX() + 0.5D - camPos.x;
            double y2 = p2.getY() + 0.25D - camPos.y;
            double z2 = p2.getZ() + 0.5D - camPos.z;

            drawBeamSegment(mat, beamBuffer, x1, y1, z1, x2, y2, z2, 0.16F, 46, 204, 113, alphaLine);

            double segDx = x2 - x1;
            double segDz = z2 - z1;
            double segLen = Math.sqrt(segDx * segDx + segDz * segDz);
            if (segLen > 0.1) {
                double nx = -segDz / segLen * 3.5;
                double nz = segDx / segLen * 3.5;

                drawBeamSegment(mat, beamBuffer, x1 + nx, y1, z1 + nz, x2 + nx, y2, z2 + nz, 0.08F, 52, 152, 219, (int) (alphaLine * 0.55F));
                drawBeamSegment(mat, beamBuffer, x1 - nx, y1, z1 - nz, x2 - nx, y2, z2 - nz, 0.08F, 52, 152, 219, (int) (alphaLine * 0.55F));

                float arrowT = ((gameTime * 0.04F) + (float) i * 0.25F) % 1.0F;
                double ax = x1 + segDx * arrowT;
                double ay = y1 + 0.05D;
                double az = z1 + segDz * arrowT;
                drawHologramDisc(mat, beamBuffer, ax, ay, az, 0.28F, 241, 196, 15, (int) (alphaSolid * 0.85F));
            }
        }

        if (waypoints.size() >= 3) {
            BlockPos last = waypoints.get(waypoints.size() - 1);
            BlockPos first = waypoints.get(0);

            double x1 = last.getX() + 0.5D - camPos.x;
            double y1 = last.getY() + 0.25D - camPos.y;
            double z1 = last.getZ() + 0.5D - camPos.z;

            double x2 = first.getX() + 0.5D - camPos.x;
            double y2 = first.getY() + 0.25D - camPos.y;
            double z2 = first.getZ() + 0.5D - camPos.z;

            drawBeamSegment(mat, beamBuffer, x1, y1, z1, x2, y2, z2, 0.14F, 255, 193, 7, alphaLoop);
        }

        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult bhr) {
            BlockPos targetPos = bhr.getBlockPos().above();
            double tx = targetPos.getX() + 0.5D - camPos.x;
            double ty = targetPos.getY() + 0.15D - camPos.y;
            double tz = targetPos.getZ() + 0.5D - camPos.z;

            drawHologramDisc(mat, beamBuffer, tx, ty, tz, 0.55F, 0, 229, 255, (int) (alphaSolid * 0.8F));

            if (!waypoints.isEmpty()) {
                BlockPos last = waypoints.get(waypoints.size() - 1);
                if (!last.equals(targetPos)) {
                    double lx = last.getX() + 0.5D - camPos.x;
                    double ly = last.getY() + 0.25D - camPos.y;
                    double lz = last.getZ() + 0.5D - camPos.z;

                    drawBeamSegment(mat, beamBuffer, lx, ly, lz, tx, ty, tz, 0.12F, 0, 229, 255, (int) (alphaLine * 0.8F));
                }
            }
        }

        poseStack.popPose();

        double totalLen = TrackMarkerWandItem.calculateTotalDistance(waypoints);
        mc.player.displayClientMessage(Component.translatable("awakening_system.racetrack_preview.hud",
                waypoints.size(), String.format(java.util.Locale.US, "%.0f", totalLen)), true);
    }

    private static void drawBeamSegment(Matrix4f m, VertexConsumer b,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        float width, int red, int green, int blue, int alpha) {
        float half = width * 0.5F;
        b.addVertex(m, (float) (x1 - half), (float) y1, (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (x1 + half), (float) y1, (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (x2 + half), (float) y2, (float) z2).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (x2 - half), (float) y2, (float) z2).setColor(red, green, blue, alpha);

        b.addVertex(m, (float) x1, (float) (y1 - half), (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) x1, (float) (y1 + half), (float) z1).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) x2, (float) (y2 + half), (float) z2).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) x2, (float) (y2 - half), (float) z2).setColor(red, green, blue, alpha);
    }

    private static void drawHologramDisc(Matrix4f m, VertexConsumer b,
                                         double cx, double cy, double cz,
                                         float radius, int red, int green, int blue, int alpha) {
        float r = radius;
        b.addVertex(m, (float) (cx - r), (float) cy, (float) (cz - r)).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (cx + r), (float) cy, (float) (cz - r)).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (cx + r), (float) cy, (float) (cz + r)).setColor(red, green, blue, alpha);
        b.addVertex(m, (float) (cx - r), (float) cy, (float) (cz + r)).setColor(red, green, blue, alpha);
    }
}
