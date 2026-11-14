package com.soybe.awakeningsystem.client.sao;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * With the full-dive style on, creatures carry the series' floating status instead of the "[Lv. X] name"
 * label: the name, and under it a thin slanted health bar (green, yellow below half, red below a fifth) with
 * a white trail where it was just hurt. It shows wherever the normal label would, and on any creature that is
 * hurt and near. Players keep their own badges.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, value = Dist.CLIENT)
public final class SaoEntityBars {
    private SaoEntityBars() {}

    /** Per creature: shown health, trailing health, last update time. */
    private static final Map<Integer, float[]> bars = new HashMap<>();
    private static final long START = System.nanoTime();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onNameTag(RenderNameTagEvent event) {
        if (!UiStyle.fullDive()) return;
        if (!(event.getEntity() instanceof LivingEntity le) || le instanceof Player || le instanceof ArmorStand) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || le.isDeadOrDying() || le.isInvisibleTo(mc.player)) return;
        boolean labelled = event.canRender() == TriState.TRUE;
        boolean hurtNear = le instanceof Mob && le.getHealth() < le.getMaxHealth() && le.distanceToSqr(mc.player) < 24 * 24;
        if (!labelled && !hurtNear) return;
        event.setCanRender(TriState.FALSE);
        // townsfolk and other peaceful people only show their name; fighters show their health too
        boolean fighter = le instanceof Enemy || hurtNear
                || (le instanceof Mob mob && mob.getTarget() == mc.player);
        draw(mc, le, cleanName(le, labelled ? event.getContent() : le.getDisplayName()), fighter,
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    /** The label without the "[♥ hp/max]" numbers a skill may add: the bar shows health in this style. */
    private static Component cleanName(LivingEntity le, Component content) {
        if (!content.getString().contains("❤")) return content;
        if (le.hasCustomName()) return le.getCustomName();
        MutableComponent out = content.plainCopy().setStyle(content.getStyle());
        for (Component part : content.getSiblings()) if (!part.getString().contains("❤")) out.append(part);
        return out;
    }

    private static void draw(Minecraft mc, LivingEntity le, Component name, boolean fighter, PoseStack ps, MultiBufferSource buffers, int light) {
        // seconds since the game started, small enough to keep its precision in a float (the wall clock in
        // milliseconds is not: stored in a float it rounds to minutes and the bar never moved)
        float now = (System.nanoTime() - START) / 1.0E9F;
        float hp = le.getHealth(), max = Math.max(1.0F, le.getMaxHealth());
        if (bars.size() > 512) bars.clear();
        float[] b = bars.computeIfAbsent(le.getId(), k -> new float[]{hp, hp, now});
        float dt = Mth.clamp(now - b[2], 0.0F, 0.1F);
        b[2] = now;
        b[0] += (hp - b[0]) * Math.min(1.0F, dt * 14.0F);
        if (hp >= b[1]) b[1] = hp;
        else b[1] -= (b[1] - hp) * Math.min(1.0F, dt * 2.0F);

        ps.pushPose();
        ps.translate(0.0F, le.getBbHeight() + 0.5F, 0.0F);
        ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ps.scale(0.025F, -0.025F, 0.025F);

        Font font = mc.font;
        ps.pushPose();
        ps.scale(0.8F, 0.8F, 0.8F);
        float tw = font.width(name);
        font.drawInBatch(name, -tw / 2.0F, fighter ? -11.0F : 0.0F, 0xFFFFFFFF, false, ps.last().pose(), buffers, Font.DisplayMode.NORMAL, 0x40000000, light);
        ps.popPose();

        if (fighter) {
            Matrix4f m = ps.last().pose();
            VertexConsumer vc = buffers.getBuffer(RenderType.textBackground());
            float w = 56.0F, h = 4.0F, k = 2.0F, x = -w / 2.0F, y = 0.0F;
            float frac = Mth.clamp(b[0] / max, 0.0F, 1.0F), trail = Math.max(frac, Mth.clamp(b[1] / max, 0.0F, 1.0F));
            int c = SaoDraw.hpColor(frac), frame = 0xD0202028;
            // the frame as four strips around the bar, and inside it three pieces side by side, so nothing
            // overlaps (overlapping flat pieces flicker or hide each other in the world)
            piece(vc, m, x - 1.5F, y - 1.0F, w + 3.0F, 1.0F, 0, frame, light);                       // top
            piece(vc, m, x - 1.5F, y + h, w + 3.0F, 1.0F, 0, frame, light);                          // bottom
            piece(vc, m, x - 1.5F, y, 1.5F, h, k, frame, light);                                     // left
            piece(vc, m, x + w, y, 1.5F, h, k, frame, light);                                        // right
            float fw = w * frac, tw2 = w * trail;
            piece(vc, m, x, y, fw, 1.2F * 1.0F, 0, SaoDraw.shade(c, 0.35F), light);                 // lit top of the fill
            piece(vc, m, x, y + 1.2F, fw, h - 1.2F, 0, c, light);
            piece(vc, m, x + fw, y, tw2 - fw, h, 0, 0xFFEDEDED, light);                              // the damage trail
            piece(vc, m, x + tw2, y, w - tw2, h, 0, 0xE0101014, light);                              // the empty groove
        }
        ps.popPose();
    }

    /**
     * One flat piece of the bar between x and x + w, leaning with the bar: the bar's slant is {@code k} over
     * its full height, so every piece takes the share of it that its own rows cover.
     */
    private static void piece(VertexConsumer vc, Matrix4f m, float x, float y, float w, float h, float ignored, int argb, int light) {
        if (w <= 0.0F || h <= 0.0F) return;
        float k = 2.0F, top = 0.0F, bh = 4.0F;
        float sTop = k * (1.0F - (y - top) / bh), sBot = k * (1.0F - (y + h - top) / bh);
        vc.addVertex(m, x + sTop, y, 0).setColor(argb).setLight(light);
        vc.addVertex(m, x + sBot, y + h, 0).setColor(argb).setLight(light);
        vc.addVertex(m, x + w + sBot, y + h, 0).setColor(argb).setLight(light);
        vc.addVertex(m, x + w + sTop, y, 0).setColor(argb).setLight(light);
    }
}