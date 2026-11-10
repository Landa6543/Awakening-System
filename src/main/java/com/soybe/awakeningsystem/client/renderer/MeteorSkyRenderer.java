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
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * The falling meteorite, drawn by each client at its real place in the world, however far away it is (a real
 * entity would only be sent to players within a few chunks). A tumbling rock with glowing cracks, a trail of
 * fire, smoke that hangs in the sky behind it, and on impact a flash, a fireball, a shock wave along the
 * ground and a column of smoke. None of it is fogged out, so it can be seen far beyond the loaded terrain.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class MeteorSkyRenderer {
    private static final ResourceLocation ROCK = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/misc/meteor_rock.png");
    private static final float EXPLOSION_TICKS = 260.0F;
    private static final float SMOKE_LIFE = 220.0F;
    private static final int DISC_SEGMENTS = 16;

    private record Box(Vector3f centre, float half) {}

    private static final class Meteor {
        Vec3 start, end;
        int duration;
        long startTick;
        final List<Box> rock = new ArrayList<>();
        Vector3f spinAxis;

        Vec3 at(float t) {
            double f = Math.pow(Math.max(0.0F, Math.min(1.0F, t / duration)), 1.4D);
            return start.lerp(end, f);
        }
    }

    private static final List<Meteor> METEORS = new ArrayList<>();

    private MeteorSkyRenderer() {}

    public static void start(MeteorStreakPayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Meteor m = new Meteor();
        m.start = new Vec3(p.sx(), p.sy(), p.sz());
        m.end = new Vec3(p.ex(), p.ey(), p.ez());
        m.duration = Math.max(1, p.durationTicks());
        m.startTick = mc.level.getGameTime();
        // a lumpy rock: a core with a few smaller blocks pushed out of it
        Random r = new Random(p.seed());
        m.rock.add(new Box(new Vector3f(), 1.35F));
        for (int i = 0; i < 7; i++) {
            Vector3f d = new Vector3f(r.nextFloat() - 0.5F, r.nextFloat() - 0.5F, r.nextFloat() - 0.5F).normalize().mul(1.0F + r.nextFloat() * 0.4F);
            m.rock.add(new Box(d, 0.6F + r.nextFloat() * 0.45F));
        }
        m.spinAxis = new Vector3f(r.nextFloat() - 0.5F, r.nextFloat() - 0.5F, r.nextFloat() - 0.5F).normalize();
        METEORS.add(m);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (METEORS.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            METEORS.clear();
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Vector3f right = new Vector3f(camera.getLeftVector()).negate(), up = new Vector3f(camera.getUpVector());
        Matrix4f pose = new Matrix4f(event.getModelViewMatrix());
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        Iterator<Meteor> it = METEORS.iterator();
        while (it.hasNext()) {
            Meteor m = it.next();
            float t = (mc.level.getGameTime() - m.startTick) + partial;
            if (t < 0 || t > m.duration + EXPLOSION_TICKS) {
                it.remove();
                continue;
            }
            // 1. smoke: darkens what is behind it, so normal blending and no depth writes
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder smoke = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            drawTrailSmoke(smoke, pose, cam, right, up, m, t);
            if (t > m.duration) drawExplosionSmoke(smoke, pose, cam, right, up, m, t - m.duration);
            draw(smoke);

            // 2. everything that glows adds to the light behind it
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            BufferBuilder glow = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            if (t <= m.duration) drawFall(glow, pose, cam, right, up, m, t);
            else drawExplosion(glow, pose, cam, right, up, m, t - m.duration);
            draw(glow);

            // 3. the rock itself, solid, hidden by terrain in front of it
            if (t <= m.duration) {
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                RenderSystem.setShaderTexture(0, ROCK);
                BufferBuilder rock = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                drawRock(rock, pose, cam, m, t);
                draw(rock);
                RenderSystem.enableBlend();
            }
        }
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    // ---------------------------------------------------------------- the fall
    private static void drawFall(BufferBuilder b, Matrix4f pose, Vec3 cam, Vector3f right, Vector3f up, Meteor m, float t) {
        Vector3f head = rel(m.at(t), cam);
        // the fiery tail: a ribbon along the path just travelled, white-hot at the rock, red at the end
        int segments = 20;
        float span = Math.min(t, 22.0F);
        for (int i = 0; i < segments; i++) {
            float a = (float) i / segments, c = (float) (i + 1) / segments;
            Vector3f p0 = rel(m.at(t - a * span), cam), p1 = rel(m.at(t - c * span), cam);
            if (p0.distanceSquared(p1) < 1.0E-4F) continue;
            Vector3f dir = new Vector3f(p1).sub(p0);
            Vector3f side0 = new Vector3f(dir).cross(p0).normalize().mul(2.6F * (1 - a));
            Vector3f side1 = new Vector3f(dir).cross(p1).normalize().mul(2.6F * (1 - c));
            float[] col0 = {1.0F, 0.85F - 0.55F * a, 0.5F - 0.45F * a}, col1 = {1.0F, 0.85F - 0.55F * c, 0.5F - 0.45F * c};
            float al0 = 0.9F * (1 - a), al1 = 0.9F * (1 - c);
            tri(b, pose, new Vector3f(p0).add(side0), col0, al0, new Vector3f(p0).sub(side0), col0, al0, new Vector3f(p1).add(side1), col1, al1);
            tri(b, pose, new Vector3f(p0).sub(side0), col0, al0, new Vector3f(p1).sub(side1), col1, al1, new Vector3f(p1).add(side1), col1, al1);
        }
        // heat around the rock
        disc(b, pose, head, right, up, 7.0F, new float[]{1.0F, 0.45F, 0.12F}, 0.55F);
        disc(b, pose, head, right, up, 3.4F, new float[]{1.0F, 0.9F, 0.6F}, 0.8F);
    }

    /** Smoke left in the sky along the path: puffs that drift up, swell and fade. */
    private static void drawTrailSmoke(BufferBuilder b, Matrix4f pose, Vec3 cam, Vector3f right, Vector3f up, Meteor m, float t) {
        int last = (int) Math.min(t, m.duration);
        for (int k = 0; k <= last; k += 2) {
            float age = t - k;
            if (age < 3 || age > SMOKE_LIFE) continue;
            Vec3 p = m.at(k).add(0, age * 0.025D, 0);
            float fade = Math.min(1.0F, (age - 3) / 8.0F) * (1.0F - age / SMOKE_LIFE);
            float radius = 1.6F + age * 0.06F;
            float g = 0.16F + 0.08F * Math.max(0, 1 - age / 30.0F);   // still warm right behind the rock
            disc(b, pose, rel(p, cam), right, up, radius, new float[]{g + 0.06F, g, g - 0.02F}, 0.5F * fade);
        }
    }

    private static void drawRock(BufferBuilder b, Matrix4f pose, Vec3 cam, Meteor m, float t) {
        Vector3f head = rel(m.at(t), cam);
        Quaternionf spin = new Quaternionf().rotateAxis(t * 0.12F, m.spinAxis);
        for (Box box : m.rock) {
            Vector3f c = new Vector3f(box.centre()).rotate(spin).add(head);
            float h = box.half();
            Vector3f[] corner = new Vector3f[8];
            for (int i = 0; i < 8; i++) {
                Vector3f v = new Vector3f((i & 1) == 0 ? -h : h, (i & 2) == 0 ? -h : h, (i & 4) == 0 ? -h : h);
                corner[i] = v.rotate(spin).add(c);
            }
            // each face: four corners and a little shading, so the shape reads even though it is lit by itself
            face(b, pose, corner[0], corner[1], corner[3], corner[2], 0.75F);
            face(b, pose, corner[4], corner[6], corner[7], corner[5], 0.85F);
            face(b, pose, corner[0], corner[4], corner[5], corner[1], 0.65F);
            face(b, pose, corner[2], corner[3], corner[7], corner[6], 1.0F);
            face(b, pose, corner[0], corner[2], corner[6], corner[4], 0.8F);
            face(b, pose, corner[1], corner[5], corner[7], corner[3], 0.9F);
        }
    }

    // ---------------------------------------------------------------- the impact
    private static void drawExplosion(BufferBuilder b, Matrix4f pose, Vec3 cam, Vector3f right, Vector3f up, Meteor m, float u) {
        Vector3f ground = rel(m.end, cam);
        Vector3f above = rel(m.end.add(0, 4, 0), cam);
        // flash
        if (u < 14) disc(b, pose, above, right, up, 20.0F + 45.0F * ease(Math.min(1, u / 6.0F)), new float[]{1.0F, 0.97F, 0.88F}, 1.0F - u / 14.0F);
        // fireball that swells and cools from yellow to red
        if (u < 70) {
            float heat = Math.min(1.0F, u / 60.0F);
            float grow = ease(Math.min(1.0F, u / 18.0F));
            disc(b, pose, rel(m.end.add(0, 4 + 8 * grow, 0), cam), right, up, 8.0F + 16.0F * grow,
                    new float[]{1.0F, 0.75F - 0.45F * heat, 0.3F - 0.25F * heat}, 0.9F * (1.0F - u / 70.0F));
        }
        // shock wave racing out along the ground
        if (u < 45) {
            float radius = 4.0F + 75.0F * ease(u / 45.0F);
            ring(b, pose, rel(m.end.add(0, 0.6, 0), cam), radius, 3.5F, new float[]{1.0F, 0.85F, 0.6F}, 0.55F * (1.0F - u / 45.0F));
        }
        // glowing crater
        disc(b, pose, ground, right, up, 9.0F, new float[]{1.0F, 0.35F, 0.1F}, 0.45F * (1.0F - u / EXPLOSION_TICKS));
    }

    /** The column of smoke rising from the crater, warm at the bottom, grey higher up. */
    private static void drawExplosionSmoke(BufferBuilder b, Matrix4f pose, Vec3 cam, Vector3f right, Vector3f up, Meteor m, float u) {
        float life = 1.0F - u / EXPLOSION_TICKS;
        for (int i = 0; i < 18; i++) {
            float age = u - i * 4.0F;
            if (age <= 0) continue;
            double rise = Math.min(75.0D, age * 0.32D);
            double drift = Math.sin(i * 1.9D) * (1.0D + age * 0.03D);
            Vec3 p = m.end.add(drift, 3 + rise, Math.cos(i * 1.3D) * (1.0D + age * 0.02D));
            float radius = 4.0F + age * 0.07F + (float) rise * 0.08F;
            float warm = (float) Math.max(0, 1 - rise / 14.0D);
            disc(b, pose, rel(p, cam), right, up, radius, new float[]{0.2F + 0.45F * warm, 0.17F + 0.2F * warm, 0.16F + 0.05F * warm},
                    0.65F * Math.min(1.0F, age / 10.0F) * life);
        }
    }

    // ---------------------------------------------------------------- drawing helpers
    private static Vector3f rel(Vec3 p, Vec3 cam) {
        return new Vector3f((float) (p.x - cam.x), (float) (p.y - cam.y), (float) (p.z - cam.z));
    }

    private static float ease(float x) {
        return 1.0F - (1.0F - x) * (1.0F - x) * (1.0F - x);
    }

    /** A soft disc facing the camera: solid in the middle, clear at the edge. */
    private static void disc(BufferBuilder b, Matrix4f pose, Vector3f centre, Vector3f right, Vector3f up, float radius, float[] col, float alpha) {
        if (alpha <= 0.0F) return;
        for (int i = 0; i < DISC_SEGMENTS; i++) {
            double a0 = Math.PI * 2 * i / DISC_SEGMENTS, a1 = Math.PI * 2 * (i + 1) / DISC_SEGMENTS;
            Vector3f e0 = new Vector3f(centre).add(new Vector3f(right).mul((float) Math.cos(a0) * radius)).add(new Vector3f(up).mul((float) Math.sin(a0) * radius));
            Vector3f e1 = new Vector3f(centre).add(new Vector3f(right).mul((float) Math.cos(a1) * radius)).add(new Vector3f(up).mul((float) Math.sin(a1) * radius));
            tri(b, pose, centre, col, alpha, e0, col, 0.0F, e1, col, 0.0F);
        }
    }

    /** A flat ring lying on the ground, bright on its middle line and fading to both edges. */
    private static void ring(BufferBuilder b, Matrix4f pose, Vector3f centre, float radius, float width, float[] col, float alpha) {
        int n = 48;
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            for (int side = -1; side <= 1; side += 2) {
                float rMid = radius, rEdge = radius + side * width;
                Vector3f m0 = new Vector3f(centre).add((float) Math.cos(a0) * rMid, 0, (float) Math.sin(a0) * rMid);
                Vector3f m1 = new Vector3f(centre).add((float) Math.cos(a1) * rMid, 0, (float) Math.sin(a1) * rMid);
                Vector3f o0 = new Vector3f(centre).add((float) Math.cos(a0) * rEdge, 0, (float) Math.sin(a0) * rEdge);
                Vector3f o1 = new Vector3f(centre).add((float) Math.cos(a1) * rEdge, 0, (float) Math.sin(a1) * rEdge);
                tri(b, pose, m0, col, alpha, o0, col, 0.0F, o1, col, 0.0F);
                tri(b, pose, m0, col, alpha, o1, col, 0.0F, m1, col, alpha);
            }
        }
    }

    private static void tri(BufferBuilder b, Matrix4f pose, Vector3f p0, float[] c0, float a0, Vector3f p1, float[] c1, float a1, Vector3f p2, float[] c2, float a2) {
        b.addVertex(pose, p0.x, p0.y, p0.z).setColor(c0[0], c0[1], c0[2], a0);
        b.addVertex(pose, p1.x, p1.y, p1.z).setColor(c1[0], c1[1], c1[2], a1);
        b.addVertex(pose, p2.x, p2.y, p2.z).setColor(c2[0], c2[1], c2[2], a2);
    }

    private static void face(BufferBuilder b, Matrix4f pose, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float shade) {
        b.addVertex(pose, p0.x, p0.y, p0.z).setUv(0, 0).setColor(shade, shade, shade, 1.0F);
        b.addVertex(pose, p1.x, p1.y, p1.z).setUv(1, 0).setColor(shade, shade, shade, 1.0F);
        b.addVertex(pose, p2.x, p2.y, p2.z).setUv(1, 1).setColor(shade, shade, shade, 1.0F);
        b.addVertex(pose, p3.x, p3.y, p3.z).setUv(0, 1).setColor(shade, shade, shade, 1.0F);
    }

    /** A BufferBuilder with no vertices cannot be drawn: the game crashes if it tries. */
    private static void draw(BufferBuilder builder) {
        var mesh = builder.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
    }
}
