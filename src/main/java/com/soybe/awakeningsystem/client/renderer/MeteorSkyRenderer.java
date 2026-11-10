package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.MeteorStreakPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The falling meteorite as each client sees it, at its real place in the world: a tumbling rock (a small
 * pixel-textured 3D model) trailing the game's own particles, fire, lava and smoke. The explosion on
 * impact is the server's (game particles too). The rock is drawn without fog, so it can be seen falling even
 * beyond the loaded terrain; the particles follow the game's usual rules.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class MeteorSkyRenderer {
    private static final ResourceLocation ROCK = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/misc/meteor_rock.png");

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
        m.rock.add(new Box(new Vector3f(), 1.4F));
        for (int i = 0; i < 7; i++) {
            Vector3f d = new Vector3f(r.nextFloat() - 0.5F, r.nextFloat() - 0.5F, r.nextFloat() - 0.5F).normalize().mul(1.0F + r.nextFloat() * 0.4F);
            m.rock.add(new Box(d, 0.6F + r.nextFloat() * 0.45F));
        }
        m.spinAxis = new Vector3f(r.nextFloat() - 0.5F, r.nextFloat() - 0.5F, r.nextFloat() - 0.5F).normalize();
        METEORS.add(m);
    }

    /** The trail: the game's own particles, spawned where the rock is (always shown, even far away). */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            METEORS.clear();
            return;
        }
        RandomSource rnd = level.getRandom();
        METEORS.removeIf(m -> level.getGameTime() - m.startTick > m.duration);
        for (Meteor m : METEORS) {
            float t = level.getGameTime() - m.startTick;
            Vec3 p = m.at(t), prev = m.at(Math.max(0, t - 1)), back = prev.subtract(p);
            Vec3 drift = back.lengthSqr() < 1.0E-6 ? Vec3.ZERO : back.normalize().scale(0.08);
            // the meteor's own fire trail, spread along the whole stretch it covered this tick so it reads as
            // one continuous streak however fast it falls
            int puffs = 6 + (int) Math.min(18, back.length() * 2);
            for (int i = 0; i < puffs; i++) {
                Vec3 q = p.add(back.scale(rnd.nextDouble()));
                level.addAlwaysVisibleParticle(com.soybe.awakeningsystem.particle.ModParticles.METEOR_TRAIL.get(), true,
                        q.x + rnd.nextGaussian() * 0.5, q.y + rnd.nextGaussian() * 0.5, q.z + rnd.nextGaussian() * 0.5, drift.x, drift.y, drift.z);
            }
            // drops of molten rock falling off it
            if (rnd.nextInt(2) == 0) level.addAlwaysVisibleParticle(ParticleTypes.LAVA, true, p.x, p.y, p.z, 0, 0, 0);
        }
    }

    /** The rock itself. */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (METEORS.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 cam = event.getCamera().getPosition();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        // The camera's rotation goes in exactly once: the vertices carry it, the model-view stack is cleared
        // while drawing (it may already hold it at this stage, which turned the rock with the view)
        Matrix4f pose = new Matrix4f(event.getModelViewMatrix());
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.identity();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, ROCK);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Meteor m : METEORS) {
            float t = (mc.level.getGameTime() - m.startTick) + partial;
            if (t <= m.duration) drawRock(b, pose, cam, m, t);
        }
        var mesh = b.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
        RenderSystem.enableCull();

        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    private static void drawRock(BufferBuilder b, Matrix4f pose, Vec3 cam, Meteor m, float t) {
        Vec3 h = m.at(t);
        Vector3f head = new Vector3f((float) (h.x - cam.x), (float) (h.y - cam.y), (float) (h.z - cam.z));
        Quaternionf spin = new Quaternionf().rotateAxis(t * 0.12F, m.spinAxis);
        for (Box box : m.rock) {
            Vector3f c = new Vector3f(box.centre()).rotate(spin).add(head);
            float s = box.half();
            Vector3f[] k = new Vector3f[8];
            for (int i = 0; i < 8; i++) {
                k[i] = new Vector3f((i & 1) == 0 ? -s : s, (i & 2) == 0 ? -s : s, (i & 4) == 0 ? -s : s).rotate(spin).add(c);
            }
            face(b, pose, k[0], k[1], k[3], k[2], 0.75F);
            face(b, pose, k[4], k[6], k[7], k[5], 0.85F);
            face(b, pose, k[0], k[4], k[5], k[1], 0.65F);
            face(b, pose, k[2], k[3], k[7], k[6], 1.0F);
            face(b, pose, k[0], k[2], k[6], k[4], 0.8F);
            face(b, pose, k[1], k[5], k[7], k[3], 0.9F);
        }
    }

    private static void face(BufferBuilder b, Matrix4f pose, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float shade) {
        b.addVertex(pose, p0.x, p0.y, p0.z).setUv(0, 0).setColor(shade, shade, shade, 1.0F);
        b.addVertex(pose, p1.x, p1.y, p1.z).setUv(1, 0).setColor(shade, shade, shade, 1.0F);
        b.addVertex(pose, p2.x, p2.y, p2.z).setUv(1, 1).setColor(shade, shade, shade, 1.0F);
        b.addVertex(pose, p3.x, p3.y, p3.z).setUv(0, 1).setColor(shade, shade, shade, 1.0F);
    }
}
