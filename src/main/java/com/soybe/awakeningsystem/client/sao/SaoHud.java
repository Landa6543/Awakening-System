package com.soybe.awakeningsystem.client.sao;

import com.soybe.awakeningsystem.SystemData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import static com.soybe.awakeningsystem.client.sao.SaoDraw.*;

/**
 * The player's status in the top-left corner in the full-dive look: the rank on a glass bead, the name on a
 * white slanted plate with the experience under it, and a long slanted health bar (green, yellow, red) with a
 * white trail where damage was just taken, the mana bar under it and the numbers and level beneath.
 */
public final class SaoHud {
    private SaoHud() {}

    /**
     * One slot of the skill row: its key, what is in it (a short symbol or an item), whether anything is,
     * the seconds left on its cooldown, and its kind (0 skill, 1 stigma, 2 rune) for the colour.
     */
    public record Bead(String key, String symbol, net.minecraft.world.item.ItemStack item, boolean filled, int cooldown, int kind) {}

    /**
     * The skill row as glass beads, drawn at the row's origin in the same 20 + 3 pixel steps as the classic
     * slots: each bead carries its skill (or item), its key on a small slanted tag underneath, and while
     * cooling down it darkens and shows the seconds left.
     */
    public static void skillBeads(GuiGraphics g, Minecraft mc, java.util.List<Bead> beads, boolean locked) {
        var font = mc.font;
        float t = (System.nanoTime() % 100_000_000_000L) / 1.0E9F;
        for (int i = 0; i < beads.size(); i++) {
            Bead b = beads.get(i);
            float cx = i * 23 + 10, cy = 9;
            int accent = b.kind() == 1 ? 0xFFFFC94A : b.kind() == 2 ? 0xFFA45BFF : ORANGE;
            boolean ready = b.filled() && b.cooldown() <= 0 && !locked;
            if (ready) ring(g, cx, cy, 10.8F, 1.0F, alpha(accent, 0.55F + 0.25F * (float) Math.sin(t * 3.0 + i)));
            else ring(g, cx, cy, 10.8F, 1.0F, 0xFFB8B8BE);
            shinyDisc(g, cx, cy, 9.2F, b.filled() ? 0xFFF7F7F7 : 0xFFCFCFD4, 1.0F);
            if (b.item() != null && !b.item().isEmpty()) {
                g.pose().pushPose();
                g.pose().translate(cx - 6, cy - 6, 0);
                g.pose().scale(0.75F, 0.75F, 1.0F);
                g.renderItem(b.item(), 0, 0);
                g.pose().popPose();
            } else if (b.filled()) {
                String s = b.symbol() == null ? "" : b.symbol();
                g.drawString(font, s, Math.round(cx) - font.width(s) / 2, Math.round(cy) - 4, b.kind() == 0 ? 0xFF3A3A42 : accent, false);
            } else icon(g, PLUS, Math.round(cx) - 5, Math.round(cy) - 5, 0xFFB0B0B6);
            if (b.filled() && b.cooldown() > 0) {
                g.pose().pushPose();
                g.pose().translate(0, 0, 150);
                disc(g, cx, cy, 9.2F, 0xA0101014);
                String cd = b.cooldown() >= 60 ? (b.cooldown() / 60) + "m" : b.cooldown() + "s";
                g.drawString(font, cd, Math.round(cx) - font.width(cd) / 2, Math.round(cy) - 4, 0xFFFF8A7A, false);
                g.pose().popPose();
            }
            if (locked) disc(g, cx, cy, 9.2F, 0x90303038);
            // the key on a little slanted tag under the bead
            g.pose().pushPose();
            g.pose().translate(0, 0, 160);
            int kw = font.width(b.key());
            float tx = cx - kw * 0.35F - 3;
            slant(g, tx, cy + 8, kw * 0.7F + 6, 6, 1.5F, 0xF0FFFFFF, 0xF0DCDCE2);
            g.pose().translate(cx - kw * 0.35F, cy + 8.6F, 0);
            g.pose().scale(0.7F, 0.7F, 1.0F);
            g.drawString(font, b.key(), 0, 0, ready ? 0xFFC98700 : 0xFF4A4A52, false);
            g.pose().popPose();
        }
    }

    private static float hpShown = -1, hpTrail = -1, mpShown = -1;
    private static long last;
    private static float appear;

    public static void render(GuiGraphics g, Minecraft mc, SystemData d) {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        float dt = last == 0 ? 0.016F : Math.min(0.1F, (now - last) / 1000.0F);
        last = now;
        appear = Math.min(1.0F, appear + dt * 3.0F);
        float a = appear * appear * (3 - 2 * appear);

        float hp = mc.player.getHealth(), max = Math.max(1.0F, mc.player.getMaxHealth());
        if (hpShown < 0) hpShown = hpTrail = hp;
        hpShown += (hp - hpShown) * Math.min(1.0F, dt * 14.0F);
        if (hp >= hpTrail) hpTrail = hp;
        else hpTrail -= (hpTrail - hp) * Math.min(1.0F, dt * 2.2F);
        float mp = d.mana(), mpMax = Math.max(1, d.maxMana());
        if (mpShown < 0) mpShown = mp;
        mpShown += (mp - mpShown) * Math.min(1.0F, dt * 10.0F);

        int x = 6 - Math.round((1.0F - a) * 20), y = 6;
        var font = mc.font;

        // the rank bead
        String rank = d.evaluatedRank() == null || d.evaluatedRank().isEmpty() ? "?" : d.evaluatedRank();
        ring(g, x + 11, y + 12, 11.5F, 1.0F, alpha(0xFFB8B8BE, a));
        shinyDisc(g, x + 11, y + 12, 10, ORANGE, a);
        String rk = rank.length() > 2 ? rank.substring(0, 2) : rank;
        g.drawString(font, rk, x + 11 - font.width(rk) / 2, y + 8, alpha(WHITE, a), false);

        // the name plate and experience
        int nx = x + 25, nw = 74;
        slant(g, nx + 1, y + 6, nw, 14, 4, alpha(0x40000000, a), alpha(0x40000000, a));
        slant(g, nx, y + 5, nw, 14, 4, alpha(0xFAFFFFFF, a), alpha(0xF0DCDCE2, a));
        slant(g, nx + 3, y + 5, 2, 14, 4, alpha(ORANGE, a), alpha(ORANGE_DARK, a));
        String name = font.plainSubstrByWidth(mc.player.getName().getString(), nw - 14);
        g.drawString(font, name, nx + 9 + (nw - 14 - font.width(name)) / 2, y + 8, alpha(0xFF3A3A42, a), false);
        float exp = Mth.clamp(d.experience() / (float) Math.max(1, d.nextLevelExperience()), 0.0F, 1.0F);
        slant(g, nx - 1, y + 20, nw, 2, 1, alpha(0x90303038, a), alpha(0x90303038, a));
        slant(g, nx - 1, y + 20, nw * exp, 2, 1, alpha(0xFFFFC94A, a), alpha(ORANGE, a));

        // the health bar: dark frame, groove, white trail, coloured fill, a line of light on top
        int bx = nx + nw + 3, bw = 150;
        slant(g, bx, y + 5, bw, 14, 4, alpha(0xC0202028, a), alpha(0xC0303038, a));
        slant(g, bx + 2, y + 7, bw - 4, 10, 3, alpha(0xE0101014, a), alpha(0xE0181820, a));
        float frac = Mth.clamp(hpShown / max, 0.0F, 1.0F), trail = Mth.clamp(hpTrail / max, 0.0F, 1.0F);
        if (trail > frac) slant(g, bx + 2, y + 7, (bw - 4) * trail, 10, 3, alpha(0xFFF4F4F4, a), alpha(0xFFD8D8D8, a));
        int c = hpColor(frac);
        slant(g, bx + 2, y + 7, (bw - 4) * frac, 10, 3, alpha(shade(c, 0.3F), a), alpha(shade(c, -0.15F), a));
        slant(g, bx + 4, y + 7, Math.max(0, (bw - 8) * frac), 2, 1, alpha(0x50FFFFFF, a), alpha(0x20FFFFFF, a));
        // tick marks every tenth
        for (int i = 1; i < 10; i++) slant(g, bx + 2 + (bw - 4) * i / 10.0F, y + 7, 0.6F, 10, 3, alpha(0x30000000, a), alpha(0x30000000, a));

        // mana under it
        float mfrac = Mth.clamp(mpShown / mpMax, 0.0F, 1.0F);
        slant(g, bx - 2, y + 20, bw * 0.62F, 4, 1, alpha(0xC0202028, a), alpha(0xC0202028, a));
        slant(g, bx - 1, y + 21, (bw * 0.62F - 2) * mfrac, 2, 1, alpha(0xFF6EC1FF, a), alpha(0xFF2E86D6, a));

        // numbers and level
        String nums = Math.round(hp) + "/" + Math.round(max);
        String lv = "Lv " + d.level();
        g.pose().pushPose();
        g.pose().translate(bx + bw * 0.62F + 4, y + 20, 0);
        g.pose().scale(0.75F, 0.75F, 1.0F);
        g.drawString(font, nums, 0, 0, alpha(WHITE, a), true);
        g.drawString(font, lv, font.width(nums) + 8, 0, alpha(0xFFFFD27A, a), true);
        g.pose().popPose();
    }
}
