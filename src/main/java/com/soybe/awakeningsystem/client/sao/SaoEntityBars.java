package com.soybe.awakeningsystem.client.sao;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
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
        draw(mc, le, labelled ? event.getContent() : le.getDisplayName(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    private static void draw(Minecraft mc, LivingEntity le, Component name, PoseStack ps, MultiBufferSource buffers, int light) {
        long now = System.currentTimeMillis();
        float hp = le.getHealth(), max = Math.max(1.0F, le.getMaxHealth());
        if (bars.size() > 512) bars.clear();
        float[] b = bars.computeIfAbsent(le.getId(), k -> new float[]{hp, hp, now});
        float dt = Math.min(0.1F, (now - b[2]) / 1000.0F);
        b[2] = now;
        b[0] += (hp - b[0]) * Math.min(1.0F, dt * 14.0F);
        if (hp >= b[1]) b[1] = hp;
        else b[1] -= (b[1] - hp) * Math.min(1.0F, dt * 2.0F);

        ps.pushPose();
        ps.translate(0.0F, le.getBbHeight() + 0.5F, 0.0F);
        ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ps.scale(0.025F, -0.025F, 0.025F);
        Matrix4f m = ps.last().pose();

        Font font = mc.font;
        float tw = font.width(name);
        font.drawInBatch(name, -tw / 2.0F, -10.0F, 0xFFFFFFFF, false, m, buffers, Font.DisplayMode.NORMAL, 0x50000000, light);

        float w = Math.max(48.0F, Math.min(80.0F, tw)), h = 5.0F, skew = 2.5F, x = -w / 2.0F, y = 1.0F;
        VertexConsumer vc = buffers.getBuffer(RenderType.textBackground());
        float frac = Mth.clamp(b[0] / max, 0.0F, 1.0F), trail = Mth.clamp(b[1] / max, 0.0F, 1.0F);
        slant(vc, m, x - 1.5F, y - 1.0F, w + 3.0F, h + 2.0F, skew, 0.0F, 0xC0202028, light);
        slant(vc, m, x, y, w, h, skew, -0.01F, 0xE0101014, light);
        if (trail > frac) slant(vc, m, x, y, w * trail, h, skew, -0.02F, 0xFFEDEDED, light);
        int c = SaoDraw.hpColor(frac);
        slant(vc, m, x, y, w * frac, h, skew, -0.03F, c, light);
        slant(vc, m, x + 1.0F, y, Math.max(0.0F, (w - 2.0F) * frac), 1.2F, 0.6F, -0.04F, 0x60FFFFFF, light);
        ps.popPose();
    }

    private static void slant(VertexConsumer vc, Matrix4f m, float x, float y, float w, float h, float skew, float z, int argb, int light) {
        if (w <= 0) return;
        vc.addVertex(m, x + skew, y, z).setColor(argb).setLight(light);
        vc.addVertex(m, x, y + h, z).setColor(argb).setLight(light);
        vc.addVertex(m, x + w, y + h, z).setColor(argb).setLight(light);
        vc.addVertex(m, x + w + skew, y, z).setColor(argb).setLight(light);
    }
}
