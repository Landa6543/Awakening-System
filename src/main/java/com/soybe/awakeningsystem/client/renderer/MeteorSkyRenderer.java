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
    private static final float TAIL_FADE_TICKS = 10.0F;
    private static final float IMPACT_TICKS = 120.0F;
    private static final int TAIL_SEGMENTS = 22;
    private static final int GLOW_SEGMENTS = 18;
    private static final int SMOKE_PUFFS = 8;

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
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // El humo va primero y con mezcla normal: oscurece el cielo en vez de sumarle luz
        if (age > duration) {
            RenderSystem.defaultBlendFunc();
            BufferBuilder smoke = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            drawSmoke(smoke, pose, age - duration);
            drawIfAny(smoke);
        }

        // Todo lo que brilla se suma a la luz del cielo
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        if (fade > 0.0F) drawTail(builder, pose, progress, fade);
        if (age <= duration) {
            Vector3f head = skyPoint(progress);
            drawGlow(builder, pose, head, 4.5F, 1.0F, 0.55F, 0.2F, 0.30F);
            drawGlow(builder, pose, head, 1.6F, 1.0F, 0.95F, 0.85F, 1.0F);
        } else {
            drawImpact(builder, pose, age - duration);
        }
        drawIfAny(builder);

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
     * Estallido en el horizonte: un destello blanco, una cúpula de fuego que se hincha sobre el suelo,
     * una onda de choque que corre a lo largo del horizonte y brasas que siguen brillando un rato.
     */
    private static void drawImpact(BufferBuilder builder, Matrix4f pose, float t) {
        Vector3f ground = skyPoint(1.0F);
        Vector3f side = horizontalSide(ground);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        float life = Math.max(0.0F, 1.0F - t / IMPACT_TICKS);

        // Destello: blanco y breve
        float burst = easeOut(Math.min(1.0F, t / 4.0F));
        float flash = t < 2.0F ? 1.0F : Math.max(0.0F, 1.0F - (t - 2.0F) / 14.0F);
        if (flash > 0.0F) {
            drawGlow(builder, pose, ground, 4.0F + 16.0F * burst, 1.0F, 0.97F, 0.9F, flash);
        }

        // Cúpula de fuego: media esfera que crece sobre el horizonte y se enfría de amarillo a rojo
        float domeGrow = easeOut(Math.min(1.0F, t / 16.0F));
        float domeLife = Math.max(0.0F, 1.0F - t / 60.0F);
        if (domeLife > 0.0F) {
            float heat = Math.min(1.0F, t / 50.0F);
            drawHalfGlow(builder, pose, ground, side, up, 4.0F + 9.0F * domeGrow,
                    1.0F, 0.75F - 0.4F * heat, 0.35F - 0.25F * heat, 0.85F * domeLife * domeLife);
        }

        // Onda de choque: una franja de luz que se abre a lo largo del horizonte
        float waveLife = Math.max(0.0F, 1.0F - t / 32.0F);
        if (waveLife > 0.0F) {
            float width = 6.0F + 46.0F * easeOut(Math.min(1.0F, t / 32.0F));
            drawHorizonBand(builder, pose, ground, side, width, 1.1F, new float[]{1.0F, 0.8F, 0.55F}, 0.7F * waveLife);
        }

        // Brasas: el resplandor rojizo del cráter, que dura hasta el final
        drawHalfGlow(builder, pose, ground, side, up, 7.0F, 1.0F, 0.35F, 0.12F, 0.4F * life);
    }

    /** Humo oscuro que sube despacio y se ensancha; abajo lo tiñe el fuego del cráter. */
    private static void drawSmoke(BufferBuilder builder, Matrix4f pose, float t) {
        Vector3f ground = skyPoint(1.0F);
        Vector3f side = horizontalSide(ground);
        float life = Math.max(0.0F, 1.0F - t / IMPACT_TICKS);
        for (int i = 0; i < SMOKE_PUFFS; i++) {
            float puffAge = t - 6.0F - i * 7.0F;
            if (puffAge <= 0.0F) continue;
            float rise = Math.min(18.0F, puffAge * 0.16F);
            float drift = (float) Math.sin(i * 1.7F) * (1.0F + puffAge * 0.03F);
            Vector3f center = new Vector3f(ground).add(0.0F, 2.0F + rise, 0.0F).add(new Vector3f(side).mul(drift));
            float radius = 3.5F + puffAge * 0.07F;
            float fadeIn = Math.min(1.0F, puffAge / 8.0F);
            // Recién salido, el humo refleja el fuego; al subir se vuelve gris oscuro
            float warm = Math.max(0.0F, 1.0F - rise / 8.0F);
            float r = 0.16F + 0.4F * warm, g = 0.13F + 0.16F * warm, b = 0.14F + 0.04F * warm;
            drawGlow(builder, pose, center, radius, r, g, b, 0.6F * fadeIn * life);
        }
    }

    /** Un BufferBuilder sin vértices no se puede dibujar: el juego se cierra si se intenta. */
    private static void drawIfAny(BufferBuilder builder) {
        var mesh = builder.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
    }

    private static Vector3f horizontalSide(Vector3f point) {
        return new Vector3f(point).cross(0.0F, 1.0F, 0.0F).normalize();
    }

    private static float easeOut(float x) {
        return 1.0F - (1.0F - x) * (1.0F - x) * (1.0F - x);
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

    /** Medio disco suave apoyado en el horizonte, como una cúpula que sale del suelo. */
    private static void drawHalfGlow(BufferBuilder builder, Matrix4f pose, Vector3f center, Vector3f side, Vector3f up,
                                     float radius, float r, float g, float b, float alpha) {
        float[] color = {r, g, b};
        for (int i = 0; i < GLOW_SEGMENTS; i++) {
            double start = Math.PI * i / GLOW_SEGMENTS;
            double end = Math.PI * (i + 1) / GLOW_SEGMENTS;
            vertex(builder, pose, center, color, alpha);
            vertex(builder, pose, onCircle(center, side, up, radius, start), color, 0.0F);
            vertex(builder, pose, onCircle(center, side, up, radius, end), color, 0.0F);
        }
    }

    /** Franja horizontal que se desvanece hacia los extremos y hacia arriba y abajo. */
    private static void drawHorizonBand(BufferBuilder builder, Matrix4f pose, Vector3f center, Vector3f side,
                                        float halfWidth, float halfHeight, float[] color, float alpha) {
        Vector3f top = new Vector3f(center).add(0.0F, halfHeight, 0.0F);
        Vector3f bottom = new Vector3f(center).add(0.0F, -halfHeight, 0.0F);
        for (int dir = -1; dir <= 1; dir += 2) {
            Vector3f end = new Vector3f(center).add(new Vector3f(side).mul(halfWidth * dir));
            vertex(builder, pose, center, color, alpha);
            vertex(builder, pose, top, color, 0.0F);
            vertex(builder, pose, end, color, 0.0F);
            vertex(builder, pose, center, color, alpha);
            vertex(builder, pose, bottom, color, 0.0F);
            vertex(builder, pose, end, color, 0.0F);
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
