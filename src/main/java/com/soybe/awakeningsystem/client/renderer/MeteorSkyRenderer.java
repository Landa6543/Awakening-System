package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.MeteorStreakPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Estrella fugaz dibujada en la bóveda del cielo, igual que el sol y la luna: no tiene paralaje,
 * así que siempre parece lejana, y el terreno la tapa al llegar al horizonte.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class MeteorSkyRenderer {

    private static final float SKY_RADIUS = 80.0F;
    // Entra en diagonal: cruza buena parte del cielo de lado antes de llegar al horizonte
    private static final float START_ELEVATION = 40.0F;
    // Termina justo sobre el horizonte, donde estalla; si hay montañas delante, el paisaje lo tapa
    private static final float END_ELEVATION = 1.5F;
    private static final float AZIMUTH_SWEEP = 70.0F;
    private static final float TAIL_LENGTH = 0.34F;
    private static final float TAIL_FADE_TICKS = 14.0F;
    private static final float IMPACT_TICKS = 70.0F;
    private static final int TAIL_SEGMENTS = 22;
    private static final int GLOW_SEGMENTS = 18;

    private static boolean active = false;
    private static long startTick;
    private static float bearing;
    private static float driftSign;
    private static int duration;

    private MeteorSkyRenderer() {}

    public static void start(MeteorStreakPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        startTick = mc.level.getGameTime();
        bearing = payload.bearingDegrees();
        driftSign = payload.driftSign();
        duration = Math.max(1, payload.durationTicks());
        active = true;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!active || event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            active = false;
            return;
        }

        float age = (mc.level.getGameTime() - startTick) + event.getPartialTick().getGameTimeDeltaPartialTick(true);
        if (age < 0.0F || age > duration + IMPACT_TICKS) {
            active = false;
            return;
        }

        float progress = Math.min(1.0F, age / duration);
        float fade = age <= duration ? 1.0F : Math.max(0.0F, 1.0F - (age - duration) / TAIL_FADE_TICKS);
        // En AFTER_SKY la PoseStack llega vacía: sin la rotación de la cámara, la estrella seguiría la vista
        Matrix4f pose = event.getModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        if (fade > 0.0F) drawTail(builder, pose, progress, fade);
        if (age <= duration) {
            Vector3f head = skyPoint(progress);
            drawGlow(builder, pose, head, 4.5F, 1.0F, 0.55F, 0.2F, 0.30F);
            drawGlow(builder, pose, head, 1.6F, 1.0F, 0.95F, 0.85F, 1.0F);
        } else {
            drawImpact(builder, pose, age - duration);
        }
        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Punto del cielo en un momento de la caída, que empieza lenta y va acelerando. */
    private static Vector3f skyPoint(float progress) {
        float eased = (float) Math.pow(progress, 1.5D);
        double azimuth = Math.toRadians(bearing + driftSign * AZIMUTH_SWEEP * (1.0F - eased));
        double elevation = Math.toRadians(START_ELEVATION + (END_ELEVATION - START_ELEVATION) * eased);
        float horizontal = (float) Math.cos(elevation);
        return new Vector3f(
                (float) -Math.sin(azimuth) * horizontal,
                (float) Math.sin(elevation),
                (float) Math.cos(azimuth) * horizontal
        ).mul(SKY_RADIUS);
    }

    private static void drawTail(BufferBuilder builder, Matrix4f pose, float progress, float fade) {
        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            float nearT = (float) i / TAIL_SEGMENTS;
            float farT = (float) (i + 1) / TAIL_SEGMENTS;
            Vector3f near = skyPoint(Math.max(0.0F, progress - nearT * TAIL_LENGTH));
            Vector3f far = skyPoint(Math.max(0.0F, progress - farT * TAIL_LENGTH));
            if (near.distanceSquared(far) < 1.0E-4F) continue;

            Vector3f side = new Vector3f(far).sub(near).cross(near).normalize();
            Vector3f nearSide = new Vector3f(side).mul(0.75F * (1.0F - nearT));
            Vector3f farSide = new Vector3f(side).mul(0.75F * (1.0F - farT));
            float nearAlpha = (1.0F - nearT) * (1.0F - nearT) * fade;
            float farAlpha = (1.0F - farT) * (1.0F - farT) * fade;

            float[] a = tailColor(nearT);
            float[] b = tailColor(farT);
            Vector3f a1 = new Vector3f(near).add(nearSide);
            Vector3f a2 = new Vector3f(near).sub(nearSide);
            Vector3f b1 = new Vector3f(far).add(farSide);
            Vector3f b2 = new Vector3f(far).sub(farSide);

            vertex(builder, pose, a1, a, nearAlpha);
            vertex(builder, pose, a2, a, nearAlpha);
            vertex(builder, pose, b1, b, farAlpha);
            vertex(builder, pose, a2, a, nearAlpha);
            vertex(builder, pose, b2, b, farAlpha);
            vertex(builder, pose, b1, b, farAlpha);
        }
    }

    /**
     * Estallido en el horizonte: un destello blanco que se abre de golpe, un resplandor naranja que
     * se apaga despacio y una columna de fuego y humo que sube desde el punto de impacto.
     */
    private static void drawImpact(BufferBuilder builder, Matrix4f pose, float t) {
        Vector3f ground = skyPoint(1.0F);
        float life = 1.0F - t / IMPACT_TICKS;

        float burst = Math.min(1.0F, t / 4.0F);
        float flashAlpha = t < 3.0F ? 1.0F : Math.max(0.0F, 1.0F - (t - 3.0F) / 18.0F);
        drawGlow(builder, pose, ground, 16.0F + 10.0F * burst, 1.0F, 0.45F, 0.12F, 0.55F * life);
        if (flashAlpha > 0.0F) {
            drawGlow(builder, pose, ground, 3.0F + 13.0F * burst, 1.0F, 0.97F, 0.88F, flashAlpha);
        }

        // La columna crece rápido y luego se queda, perdiendo brillo
        float height = 22.0F * (1.0F - (float) Math.pow(1.0F - Math.min(1.0F, t / 25.0F), 3.0D));
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f side = new Vector3f(ground).cross(up).normalize();
        int segments = 10;
        for (int i = 0; i < segments; i++) {
            float a = (float) i / segments;
            float b = (float) (i + 1) / segments;
            Vector3f lower = new Vector3f(ground).add(0.0F, height * a, 0.0F);
            Vector3f upper = new Vector3f(ground).add(0.0F, height * b, 0.0F);
            // Más ancha arriba, como el humo que se abre al subir
            Vector3f lowerSide = new Vector3f(side).mul(2.0F + 3.0F * a);
            Vector3f upperSide = new Vector3f(side).mul(2.0F + 3.0F * b);
            float[] lowerColor = columnColor(a);
            float[] upperColor = columnColor(b);
            float lowerAlpha = 0.75F * life * (1.0F - a * 0.6F);
            float upperAlpha = 0.75F * life * (1.0F - b * 0.6F);

            vertex(builder, pose, new Vector3f(lower).add(lowerSide), lowerColor, lowerAlpha);
            vertex(builder, pose, new Vector3f(lower).sub(lowerSide), lowerColor, lowerAlpha);
            vertex(builder, pose, new Vector3f(upper).add(upperSide), upperColor, upperAlpha);
            vertex(builder, pose, new Vector3f(lower).sub(lowerSide), lowerColor, lowerAlpha);
            vertex(builder, pose, new Vector3f(upper).sub(upperSide), upperColor, upperAlpha);
            vertex(builder, pose, new Vector3f(upper).add(upperSide), upperColor, upperAlpha);
        }
        // Bola de fuego en la cima de la columna
        if (height > 1.0F) {
            Vector3f top = new Vector3f(ground).add(0.0F, height, 0.0F);
            drawGlow(builder, pose, top, 7.0F, 1.0F, 0.5F, 0.15F, 0.5F * life);
        }
    }

    /** De naranja intenso en la base a rojo apagado arriba. */
    private static float[] columnColor(float t) {
        return new float[]{1.0F - 0.35F * t, 0.62F - 0.45F * t, 0.25F - 0.1F * t};
    }

    /** Del blanco caliente junto a la cabeza al azul y violeta de la cola. */
    private static float[] tailColor(float t) {
        return new float[]{1.0F - 0.65F * t, 0.95F - 0.65F * t, 1.0F - 0.1F * t};
    }

    /** Disco suave orientado hacia la cámara: opaco en el centro y transparente en el borde. */
    private static void drawGlow(BufferBuilder builder, Matrix4f pose, Vector3f center, float radius, float r, float g, float b, float alpha) {
        Vector3f toCamera = new Vector3f(center).normalize();
        Vector3f right = new Vector3f(toCamera).cross(0.0F, 1.0F, 0.0F).normalize();
        Vector3f up = new Vector3f(right).cross(toCamera).normalize();
        float[] color = {r, g, b};

        for (int i = 0; i < GLOW_SEGMENTS; i++) {
            double start = Math.PI * 2.0D * i / GLOW_SEGMENTS;
            double end = Math.PI * 2.0D * (i + 1) / GLOW_SEGMENTS;
            vertex(builder, pose, center, color, alpha);
            vertex(builder, pose, onCircle(center, right, up, radius, start), color, 0.0F);
            vertex(builder, pose, onCircle(center, right, up, radius, end), color, 0.0F);
        }
    }

    private static Vector3f onCircle(Vector3f center, Vector3f right, Vector3f up, float radius, double angle) {
        return new Vector3f(center)
                .add(new Vector3f(right).mul((float) Math.cos(angle) * radius))
                .add(new Vector3f(up).mul((float) Math.sin(angle) * radius));
    }

    private static void vertex(BufferBuilder builder, Matrix4f pose, Vector3f point, float[] color, float alpha) {
        builder.addVertex(pose, point.x, point.y, point.z).setColor(color[0], color[1], color[2], alpha);
    }
}
