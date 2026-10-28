package com.soybe.awakeningsystem.client.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Locale;

/**
 * Renderizador de haces de luz 3D (Beacon Beams) y etiquetas flotantes holográficas en el mundo
 * para los Waypoints activos de Awakening System (fiel estilo JourneyMap / Xaero's).
 * - Haz radiante de alta tecnología visible desde cualquier distancia (bedrock hasta la estratosfera).
 * - Etiqueta holográfica 3D que flota sobre el punto con 'Nombre (XXm)' y diamante coloreado.
 * - Doble pase (SEE_THROUGH + NORMAL) para máxima legibilidad a través de montañas y muros.
 */
public final class WaypointBeaconRenderer {
    private WaypointBeaconRenderer() {}

    public static boolean isDimensionMatching(String wpDim, String currentDim) {
        if (wpDim == null || wpDim.isBlank() || wpDim.equalsIgnoreCase("all") || currentDim == null) {
            return true;
        }
        String d1 = wpDim.toLowerCase(Locale.ROOT).trim();
        String d2 = currentDim.toLowerCase(Locale.ROOT).trim();
        if (d1.equals(d2) || d1.contains(d2) || d2.contains(d1)) {
            return true;
        }
        String s1 = d1.contains(":") ? d1.substring(d1.indexOf(':') + 1) : d1;
        String s2 = d2.contains(":") ? d2.substring(d2.indexOf(':') + 1) : d2;
        return s1.equals(s2) || s1.contains(s2) || s2.contains(s1);
    }

    public static void render(RenderLevelStageEvent event) {
        if (true) return; // Desactivado por solicitud del usuario
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        String currentDim = mc.level.dimension().location().toString();
        List<Waypoint> waypoints = WaypointManager.getAllWaypoints();
        if (waypoints.isEmpty()) return;

        Camera camera = event.getCamera();
        var camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        float gameTime = mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(true);
        int minBuildY = mc.level.getMinBuildHeight();
        int maxBuildY = mc.level.getMaxBuildHeight();

        boolean anyRendered = false;

        for (Waypoint wp : waypoints) {
            if (!wp.isEnabled()) continue;
            if (!isDimensionMatching(wp.getDimension(), currentDim)) continue;

            double wx = wp.getX() + 0.5D;
            double wy = wp.getY();
            double wz = wp.getZ() + 0.5D;

            double rx = wx - camPos.x;
            double ry = wy - camPos.y;
            double rz = wz - camPos.z;

            double distSq = rx * rx + ry * ry + rz * rz;
            double dist = Math.sqrt(distSq);

            // Distancia máxima de renderizado: 4096 bloques
            if (distSq > 16777216.0D) continue;

            int r = (int) (wp.getRed() * 255);
            int g = (int) (wp.getGreen() * 255);
            int b = (int) (wp.getBlue() * 255);

            // 1. Haz de Luz Vertical Láser (Atraviesa todo el mundo de bedrock a cielo)
            if (wp.isShowBeam()) {
                float alphaFactor = 1.0F;
                if (dist < 3.0D) {
                    alphaFactor = (float) Math.max(0.4D, dist / 3.0D);
                }

                float y0 = (float) (minBuildY - wy);
                float y1 = (float) (maxBuildY + 128 - wy);

                renderWaypointBeam(poseStack, bufferSource, rx, ry, rz, r, g, b, y0, y1, gameTime, alphaFactor);
                anyRendered = true;
            }

            // 2. Etiqueta Flotante 3D Holográfica estilo JourneyMap (Nombre + Distancia en una sola línea con diamante)
            if (wp.isShowLabel()) {
                renderFloatingLabel(poseStack, bufferSource, mc.font, wp, rx, ry, rz, dist);
                anyRendered = true;
            }
        }

        if (anyRendered) {
            bufferSource.endBatch(RenderType.beaconBeam(AwakeningSystemClient.BEACON_BEAM_TEX, true));
        }
    }

    private static void renderWaypointBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                           double rx, double ry, double rz, int r, int g, int b,
                                           float y0, float y1, float gameTime, float alphaFactor) {
        float outerRadius = 0.18F;
        float innerRadius = 0.08F;

        poseStack.pushPose();
        poseStack.translate(rx, ry, rz);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.beaconBeam(AwakeningSystemClient.BEACON_BEAM_TEX, true));

        // Núcleo brillante
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-gameTime * 2.2F));
        int rCore = Math.min(255, r + 70);
        int gCore = Math.min(255, g + 70);
        int bCore = Math.min(255, b + 70);
        int coreAlpha = (int) (235 * alphaFactor);
        renderBeamSegment(poseStack, builder, rCore, gCore, bCore, coreAlpha, y0, y1, innerRadius, -gameTime * 0.18F);
        poseStack.popPose();

        // Halo exterior translúcido
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(gameTime * 1.4F));
        int haloAlpha = (int) (140 * alphaFactor);
        renderBeamSegment(poseStack, builder, r, g, b, haloAlpha, y0, y1, outerRadius, -gameTime * 0.10F);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderBeamSegment(PoseStack poseStack, VertexConsumer builder,
                                          int r, int g, int b, int alpha,
                                          float y0, float y1, float radius, float vOffset) {
        Matrix4f matrix = poseStack.last().pose();
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = vOffset;
        float v1 = vOffset + (y1 - y0) * 0.08F;

        float r1 = radius;
        float r2 = -radius;

        // 4 Caras (doble cara para visibilidad 360 grados completa)
        addQuad(matrix, builder, r, g, b, alpha, r2, r1, r1, r1, y0, y1, u0, u1, v0, v1);
        addQuad(matrix, builder, r, g, b, alpha, r1, r1, r2, r1, y0, y1, u0, u1, v0, v1);

        addQuad(matrix, builder, r, g, b, alpha, r1, r2, r2, r2, y0, y1, u0, u1, v0, v1);
        addQuad(matrix, builder, r, g, b, alpha, r2, r2, r1, r2, y0, y1, u0, u1, v0, v1);

        addQuad(matrix, builder, r, g, b, alpha, r2, r2, r2, r1, y0, y1, u0, u1, v0, v1);
        addQuad(matrix, builder, r, g, b, alpha, r2, r1, r2, r2, y0, y1, u0, u1, v0, v1);

        addQuad(matrix, builder, r, g, b, alpha, r1, r1, r1, r2, y0, y1, u0, u1, v0, v1);
        addQuad(matrix, builder, r, g, b, alpha, r1, r2, r1, r1, y0, y1, u0, u1, v0, v1);
    }

    private static void addQuad(Matrix4f m, VertexConsumer b, int red, int grn, int blu, int a,
                                float x0, float z0, float x1, float z1, float y0, float y1,
                                float u0, float u1, float v0, float v1) {
        b.addVertex(m, x0, y0, z0).setColor(red, grn, blu, a).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);

        b.addVertex(m, x1, y0, z1).setColor(red, grn, blu, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);

        b.addVertex(m, x1, y1, z1).setColor(red, grn, blu, a).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);

        b.addVertex(m, x0, y1, z0).setColor(red, grn, blu, a).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);
    }

    private static void renderFloatingLabel(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                            Font font, Waypoint wp, double rx, double ry, double rz, double dist) {
        // 1. Desaparición/Desvanecimiento a corta distancia estilo JourneyMap (<12m fade, <=4m oculto)
        if (dist <= 4.0D) {
            return;
        }
        float fadeAlpha = (dist < 12.0D) ? (float) Math.min(1.0F, (dist - 4.0D) / 8.0D) : 1.0F;

        // 2. Anclaje de rayo de visión a distancia óptima (26m) para evitar niebla / recorte de render
        double clampedDist = Math.min(dist, 26.0D);
        double dirX = rx / dist;
        double dirY = ry / dist;
        double dirZ = rz / dist;

        double posX = dirX * clampedDist;
        double posY = dirY * clampedDist + 1.8D;
        double posZ = dirZ * clampedDist;

        poseStack.pushPose();
        poseStack.translate(posX, posY, posZ);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        // Escala fija y calibrada: tamaño de nametag nítido y constante, no se agranda al alejarse
        float scale = (float) (clampedDist * 0.0013D + 0.006D);
        poseStack.scale(scale, -scale, scale);

        // Texto completo de Waypoint en 3D siempre visible: "Nombre (XXm)"
        String labelText = wp.getName() + " (" + (int) Math.round(dist) + "m)";
        float textW = font.width(labelText);
        float halfW = textW / 2.0F;

        Matrix4f matrix = poseStack.last().pose();
        int light = LightTexture.FULL_BRIGHT;

        int alphaByte = (int) (255 * fadeAlpha);
        int bgAlpha = (int) (185 * fadeAlpha);

        int wpColor = wp.getColor();
        int cr = (wpColor >> 16) & 0xFF;
        int cg = (wpColor >> 8) & 0xFF;
        int cb = wpColor & 0xFF;

        int maxComp = Math.max(cr, Math.max(cg, cb));
        if (maxComp < 75) {
            float boost = 100.0F / Math.max(1, maxComp);
            cr = Math.min(255, (int) (cr * boost));
            cg = Math.min(255, (int) (cg * boost));
            cb = Math.min(255, (int) (cb * boost));
        }

        int textColor = (alphaByte << 24) | (cr << 16) | (cg << 8) | cb;
        int bgColor = (bgAlpha << 24) | (wp.isDeathPoint() ? 0x330008 : 0x050C14);

        // 1. Pase SEE_THROUGH: Texto coloreado sobre placa oscura visible a través de montañas y muros
        font.drawInBatch(
                labelText,
                -halfW,
                -13,
                textColor,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                bgColor,
                light
        );

        // 2. Símbolo (Calavera para Muerte, Diamante para Waypoint estándar)
        String symbol = wp.isDeathPoint() ? "☠" : "◆";
        float symbolHalfW = font.width(symbol) / 2.0F;
        font.drawInBatch(
                symbol,
                -symbolHalfW,
                -1,
                textColor,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0x00000000,
                light
        );

        // 3. Pase NORMAL: En primer plano nítido
        font.drawInBatch(
                labelText,
                -halfW,
                -13,
                textColor,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0x00000000,
                light
        );

        font.drawInBatch(
                symbol,
                -symbolHalfW,
                -1,
                textColor,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0x00000000,
                light
        );

        poseStack.popPose();
    }
}
