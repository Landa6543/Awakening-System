package com.soybe.awakeningsystem.client.sao;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * With the full-dive style on, whatever dies (mobs and players alike) does not fall over: it flashes white
 * and bursts into glass shards that fly out, spin, catch the light and fade, with the series' shatter sound.
 * Everything here is client side: every player sees it on their own screen.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class SaoShatter {
    private SaoShatter() {}

    private static final RandomSource RANDOM = RandomSource.create();
    private static final SoundEvent DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sao.death"));

    /** Entities that already burst (by id), so each one shatters once. */
    private static final Set<Integer> burst = new HashSet<>();
    private static final List<Shard> shards = new ArrayList<>();
    private static final List<Flash> flashes = new ArrayList<>();

    private static final class Shard {
        double x, y, z, ox, oy, oz, vx, vy, vz;
        float size, rx, ry, rz, sx, sy, sz;
        int age, life, tint, hold;
    }

    private record Flash(double x, double y, double z, float radius, int born) {}

    private static int clock;

    private static boolean on() {
        return UiStyle.fullDive();
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            burst.clear();
            shards.clear();
            flashes.clear();
            return;
        }
        if (mc.isPaused()) return;
        clock++;
        if (on()) {
            AABB around = mc.player.getBoundingBox().inflate(64.0D);
            for (Entity e : mc.level.entitiesForRendering()) {
                if (!(e instanceof LivingEntity le) || e instanceof ArmorStand) continue;
                if (le.isDeadOrDying() && !burst.contains(e.getId()) && around.intersects(e.getBoundingBox())) {
                    burst.add(e.getId());
                    shatter(mc, le);
                }
            }
        }
        if (clock % 200 == 0) burst.removeIf(id -> mc.level.getEntity(id) == null);
        for (Iterator<Shard> it = shards.iterator(); it.hasNext(); ) {
            Shard s = it.next();
            s.ox = s.x; s.oy = s.y; s.oz = s.z;
            if (++s.age >= s.life) {
                it.remove();
                continue;
            }
            if (s.age <= s.hold) continue;                  // the body stands as glass for a moment, then bursts
            s.x += s.vx; s.y += s.vy; s.z += s.vz;
            s.vx *= 0.9; s.vz *= 0.9;
            s.vy = s.vy * 0.9 - 0.012;
            s.rx += s.sx; s.ry += s.sy; s.rz += s.sz;
        }
        flashes.removeIf(f -> clock - f.born() > 8);
    }

    /** Breaks a body into shards spread through its box, flying outward and up from its middle. */
    private static void shatter(Minecraft mc, LivingEntity e) {
        AABB box = e.getBoundingBox();
        double w = box.getXsize(), h = box.getYsize(), d = box.getZsize();
        Vec3 c = box.getCenter();
        float big = (float) Math.cbrt(w * h * d);
        int count = Mth.clamp((int) (110 * w * h * d) + 40, 40, 320);
        for (int i = 0; i < count; i++) {
            Shard s = new Shard();
            s.x = s.ox = box.minX + RANDOM.nextDouble() * w;
            s.y = s.oy = box.minY + RANDOM.nextDouble() * h;
            s.z = s.oz = box.minZ + RANDOM.nextDouble() * d;
            double dx = s.x - c.x, dy = s.y - c.y, dz = s.z - c.z;
            double len = Math.max(0.05, Math.sqrt(dx * dx + dy * dy + dz * dz));
            // outward from the middle in every direction, the same on all sides (no shove backwards)
            double push = 0.07 + RANDOM.nextDouble() * 0.14;
            s.vx = dx / len * push + (RANDOM.nextDouble() - 0.5) * 0.04;
            s.vy = dy / len * push * 0.7 + 0.03 + RANDOM.nextDouble() * 0.06;
            s.vz = dz / len * push + (RANDOM.nextDouble() - 0.5) * 0.04;
            s.size = (0.06F + RANDOM.nextFloat() * 0.1F) * Mth.clamp(big, 0.6F, 2.2F);
            s.hold = 2 + RANDOM.nextInt(3);
            s.rx = RANDOM.nextFloat() * 360; s.ry = RANDOM.nextFloat() * 360; s.rz = RANDOM.nextFloat() * 360;
            s.sx = (RANDOM.nextFloat() - 0.5F) * 30; s.sy = (RANDOM.nextFloat() - 0.5F) * 30; s.sz = (RANDOM.nextFloat() - 0.5F) * 30;
            s.life = s.hold + 22 + RANDOM.nextInt(18);
            // pale glass, a few shards caught bluer or whiter
            int pick = RANDOM.nextInt(10);
            s.tint = pick < 5 ? 0xBFE9FF : pick < 8 ? 0xE8F7FF : 0x8FD0FF;
            shards.add(s);
        }
        flashes.add(new Flash(c.x, c.y, c.z, Math.max(0.6F, big * 0.9F), clock));
        mc.level.playLocalSound(c.x, c.y, c.z, DEATH, SoundSource.NEUTRAL, 1.0F, 0.95F + RANDOM.nextFloat() * 0.1F, false);
    }

    /**
     * The very frame a creature is seen dying, it turns to glass: it bursts here (if the tick has not caught
     * it yet) and its body is never drawn again, so it neither falls over nor flies back.
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        if (!on()) return;
        LivingEntity e = event.getEntity();
        if (!e.isDeadOrDying() || e instanceof ArmorStand) return;
        if (burst.add(e.getId())) shatter(Minecraft.getInstance(), e);
        event.setCanceled(true);
    }

    /**
     * Before any creature is drawn this frame, the ones that just died burst and leave the world on this
     * screen (not on the server, which still kills them, drops their loot and removes them). Many of the mod's
     * creatures are drawn by GeckoLib, which never asks whether a body should be hidden, so removing it is the
     * only way to be sure it is never seen falling over. Players are hidden instead (see onRenderLiving).
     */
    private static void burstTheDead(Minecraft mc) {
        if (!on() || mc.level == null || mc.player == null) return;
        List<Entity> gone = new ArrayList<>();
        AABB around = mc.player.getBoundingBox().inflate(96.0D);
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || !le.isDeadOrDying()) continue;
            if (!around.intersects(e.getBoundingBox())) continue;
            if (burst.add(e.getId())) shatter(mc, le);
            if (!(e instanceof net.minecraft.world.entity.player.Player)) gone.add(e);
        }
        for (Entity e : gone) mc.level.removeEntity(e.getId(), Entity.RemovalReason.KILLED);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            burstTheDead(Minecraft.getInstance());
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (shards.isEmpty() && flashes.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cam = event.getCamera().getPosition();
        PoseStack ps = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        VertexConsumer solid = buffers.getBuffer(RenderType.debugQuads());
        for (Shard s : shards) {
            float life = (s.age + pt) / s.life;
            float fade = life < 0.6F ? 1.0F : 1.0F - (life - 0.6F) / 0.4F;
            float size = s.size * (life < 0.7F ? 1.0F : 1.0F - (life - 0.7F) / 0.3F * 0.7F);
            ps.pushPose();
            ps.translate(Mth.lerp(pt, s.ox, s.x) - cam.x, Mth.lerp(pt, s.oy, s.y) - cam.y, Mth.lerp(pt, s.oz, s.z) - cam.z);
            ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(s.rx + s.sx * pt));
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(s.ry + s.sy * pt));
            ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(s.rz + s.sz * pt));
            ps.scale(size, size, size);
            shard(solid, ps.last().pose(), s.tint, (int) (215 * fade));
            ps.popPose();
        }
        buffers.endBatch(RenderType.debugQuads());

        VertexConsumer glow = buffers.getBuffer(RenderType.lightning());
        for (Shard s : shards) {
            float life = (s.age + pt) / s.life;
            float fade = Math.max(0.0F, 1.0F - life);
            ps.pushPose();
            ps.translate(Mth.lerp(pt, s.ox, s.x) - cam.x, Mth.lerp(pt, s.oy, s.y) - cam.y, Mth.lerp(pt, s.oz, s.z) - cam.z);
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(s.ry + s.sy * pt));
            ps.scale(s.size * 1.6F, s.size * 1.6F, s.size * 1.6F);
            shard(glow, ps.last().pose(), 0x9FDFFF, (int) (90 * fade));
            ps.popPose();
        }
        // the white flash where the body was: a few crossed glowing planes that swell and fade
        for (Flash f : flashes) {
            float t = (clock - f.born() + pt) / 8.0F;
            float r = f.radius() * (0.6F + t * 1.2F);
            int a = (int) (200 * (1.0F - t));
            if (a <= 0) continue;
            ps.pushPose();
            ps.translate(f.x() - cam.x, f.y() - cam.y, f.z() - cam.z);
            for (int k = 0; k < 3; k++) {
                ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(60));
                flash(glow, ps.last().pose(), r, 0xEAF8FF, a);
            }
            ps.popPose();
        }
        buffers.endBatch(RenderType.lightning());
    }

    /** A glass splinter: a flattened, uneven tetrahedron with lighter and darker faces. */
    private static void shard(VertexConsumer vc, Matrix4f m, int rgb, int alpha) {
        float[] a = {0.0F, 1.0F, 0.0F}, b = {-0.55F, -0.45F, 0.35F}, c = {0.6F, -0.35F, 0.25F}, d = {0.05F, -0.5F, -0.45F};
        tri(vc, m, a, b, c, rgb, 1.0F, alpha);
        tri(vc, m, a, c, d, rgb, 0.85F, alpha);
        tri(vc, m, a, d, b, rgb, 0.7F, alpha);
        tri(vc, m, b, d, c, rgb, 0.6F, alpha);
    }

    private static void tri(VertexConsumer vc, Matrix4f m, float[] p, float[] q, float[] r, int rgb, float light, int alpha) {
        int cr = Math.min(255, (int) (((rgb >> 16) & 255) * light)), cg = Math.min(255, (int) (((rgb >> 8) & 255) * light)),
                cb = Math.min(255, (int) ((rgb & 255) * light));
        vc.addVertex(m, p[0], p[1], p[2]).setColor(cr, cg, cb, alpha);
        vc.addVertex(m, q[0], q[1], q[2]).setColor(cr, cg, cb, alpha);
        vc.addVertex(m, r[0], r[1], r[2]).setColor(cr, cg, cb, alpha);
        vc.addVertex(m, r[0], r[1], r[2]).setColor(cr, cg, cb, alpha);
    }

    /** A diamond of light in the XY plane: bright in the middle, fading to nothing at its four points. */
    private static void flash(VertexConsumer vc, Matrix4f m, float r, int rgb, int alpha) {
        int cr = (rgb >> 16) & 255, cg = (rgb >> 8) & 255, cb = rgb & 255;
        float[][] pts = {{0, r}, {r, 0}, {0, -r}, {-r, 0}};
        for (int i = 0; i < 4; i++) {
            float[] p = pts[i], q = pts[(i + 1) % 4];
            vc.addVertex(m, 0, 0, 0).setColor(cr, cg, cb, alpha);
            vc.addVertex(m, p[0], p[1], 0).setColor(cr, cg, cb, 0);
            vc.addVertex(m, q[0], q[1], 0).setColor(cr, cg, cb, 0);
            vc.addVertex(m, q[0], q[1], 0).setColor(cr, cg, cb, 0);
        }
    }
}
