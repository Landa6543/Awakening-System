package com.soybe.awakeningsystem.abyss;

import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * The Spiral Abyss window, laid out after the game's own: the Corridor's eight floor banners in a row (and the
 * rift into the Spire), the Spire's four, the place's name at the top left, a hint and Confirm / Leave at the
 * bottom; Confirm (or a second click on a banner) opens the floor's view with its ley line disorder and its three
 * chambers' stars, where the floor is started and its reward collected. Pictures: {@link AbyssArt}.
 */
public final class SpiralAbyssScreen extends Screen {

    private int unlockedFloor;
    private final int[] starsPerFloor = new int[SpiralAbyssManager.TOTAL_FLOORS];
    private final boolean[] claimedRewards = new boolean[SpiralAbyssManager.TOTAL_FLOORS];
    private int totalStars;
    private int playerLevel;
    private int primogems;
    private int coins;
    private int intertwinedFates;

    private int selectedTab = 0;
    private int corridorPage = 0;
    private int selectedFloor = 1;
    private int selectedChamber = 1;

    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"
    };

    public SpiralAbyssScreen(OpenSpiralAbyssPayload payload) {
        super(Component.translatable("gui.awakening_system.spiral_abyss.title"));
        updateData(payload);

        this.selectedFloor = Math.max(1, Math.min(this.unlockedFloor, SpiralAbyssManager.TOTAL_FLOORS));
        if (this.selectedFloor >= 9) {
            this.selectedTab = 1;
        } else {
            this.selectedTab = 0;
            this.corridorPage = (this.selectedFloor > 4) ? 1 : 0;
        }
    }

    public void updateData(OpenSpiralAbyssPayload payload) {
        this.unlockedFloor = Math.max(1, payload.currentUnlockedFloor());
        System.arraycopy(payload.starsPerFloor(), 0, this.starsPerFloor, 0, Math.min(SpiralAbyssManager.TOTAL_FLOORS, payload.starsPerFloor().length));
        System.arraycopy(payload.claimedRewards(), 0, this.claimedRewards, 0, Math.min(SpiralAbyssManager.TOTAL_FLOORS, payload.claimedRewards().length));
        this.totalStars = payload.totalStars();
        this.playerLevel = payload.playerLevel();
        this.primogems = payload.primogems();
        this.coins = payload.coins();
        this.intertwinedFates = payload.intertwinedFates();
        this.spireResetAt = System.currentTimeMillis() + payload.spireResetMs();
        this.history = List.copyOf(payload.history());
    }

    /** When (this computer's clock) the Spire's floors start again. */
    private long spireResetAt;
    /** The latest clears of every floor, oldest first. */
    private List<SpiralAbyssManager.ClearRecord> history = List.of();
    /** The floor's record of clears is open over its view. */
    private boolean historyOpen;

    /** "13d 4h", or "5h 20m" in the last day. */
    private static String timeLeft(long ms) {
        long min = Math.max(0, ms) / 60000L;
        long d = min / 1440, hr = (min % 1440) / 60, m = min % 60;
        return d > 0 ? Component.translatable("gui.awakening_system.spiral_abyss.days_hours", d, hr).getString()
                : Component.translatable("gui.awakening_system.spiral_abyss.hours_minutes", hr, m).getString();
    }

    private String resetLine() {
        return "⟳ " + Component.translatable("gui.awakening_system.spiral_abyss.reset_in", timeLeft(spireResetAt - System.currentTimeMillis())).getString();
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(ModSounds.ABYSS_ENTER.value(), 0.75F, 1.0F);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    // ---- layout, worked out the same way for drawing and for clicks

    /** The floor's detail view is open (after Confirm), instead of the row of banners. */
    private boolean detailView;

    private int bannerH() {
        return Math.max(90, Math.min((int) (this.height * 0.56), 300));
    }

    private int bannerW() {
        return (bannerH() * AbyssArt.BANNER_W) / AbyssArt.BANNER_H;
    }

    /** How many banners the row shows, and the first floor: the Corridor's eight and its rift, or the Spire's four. */
    private int rowCount() {
        return selectedTab == 0 ? 9 : 4;
    }

    private int rowFirstFloor() {
        return selectedTab == 0 ? 1 : 9;
    }

    /** The left edge of banner i of the row, with the banners spread to fit the window. */
    private int[] rowGeometry() {
        int n = rowCount();
        int bw = bannerW(), bh = bannerH();
        int gap = Math.max(4, bw / 3);
        int total = n * bw + (n - 1) * gap;
        float scale = 1.0F;
        if (total > this.width - 40) {
            scale = (this.width - 40) / (float) total;
            bw = (int) (bw * scale);
            bh = (int) (bh * scale);
            gap = (int) (gap * scale);
            total = n * bw + (n - 1) * gap;
        }
        int x0 = (this.width - total) / 2;
        int y0 = (int) (this.height * 0.47F) - bh / 2;
        return new int[]{x0, y0, bw, bh, gap};
    }

    private boolean over(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---- drawing

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = this.width, h = this.height;
        renderHall(graphics, w, h);
        renderHeader(graphics, mouseX, mouseY, w);
        if (detailView) renderDetailView(graphics, mouseX, mouseY, w, h);
        else renderRow(graphics, mouseX, mouseY, w, h);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** The Corridor's hall or the Spire's eye, covering the window, darkened at the edges. */
    private void renderHall(GuiGraphics graphics, int w, int h) {
        var tex = selectedTab == 0 ? AbyssArt.corridor() : AbyssArt.spire();
        float scale = Math.max(w / (float) AbyssArt.BG_W, h / (float) AbyssArt.BG_H);
        int dw = Math.round(AbyssArt.BG_W * scale), dh = Math.round(AbyssArt.BG_H * scale);
        graphics.blit(tex, (w - dw) / 2, (h - dh) / 2, dw, dh, 0, 0, AbyssArt.BG_W, AbyssArt.BG_H, AbyssArt.BG_W, AbyssArt.BG_H);
        long now = System.currentTimeMillis();
        int cx = w / 2, cy = (int) (h * (selectedTab == 0 ? 0.545F : 0.5F));
        int glowCol = selectedTab == 0 ? 0x7FE6D8 : 0xA88CF0;
        // the hall's dial turns, as in the game: a big ring one way and the star inside it the other way
        // (in place of the old "breathing" light, drawn as blocky stripes)
        boolean spireHall = selectedTab == 1;
        int size = (int) (Math.min(w, h) * (spireHall ? 0.92F : 0.5F));
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        for (int layer = 0; layer < 2; layer++) {
            boolean outer = layer == 0;
            float turn = (float) ((now % 1800000L) / (outer ? 90000.0 : -60000.0) * Math.PI * 2);
            int s = outer ? size : (int) (size * 0.62F);
            graphics.pose().pushPose();
            graphics.pose().translate(cx, cy, 0);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(turn));
            graphics.setColor(1, 1, 1, spireHall ? 0.85F : 0.7F);
            graphics.blit(AbyssArt.halo(spireHall, outer), -s / 2, -s / 2, s, s, 0, 0, AbyssArt.HALO, AbyssArt.HALO, AbyssArt.HALO, AbyssArt.HALO);
            graphics.setColor(1, 1, 1, 1);
            graphics.pose().popPose();
        }
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        // motes of light drifting upwards through the hall
        java.util.Random rnd = new java.util.Random(77);
        for (int i = 0; i < 46; i++) {
            float speed = 0.02F + rnd.nextFloat() * 0.03F;
            float ph = ((now / 1000.0F) * speed + rnd.nextFloat()) % 1.0F;
            int px = (int) (rnd.nextFloat() * w + Math.sin(now / 1500.0 + i) * 8);
            int py = (int) (h - ph * (h + 20));
            int a = (int) (Math.sin(ph * Math.PI) * (60 + rnd.nextInt(90)));
            // tiny specks, not squares: one pixel with a fainter cross round it
            graphics.fill(px, py, px + 1, py + 1, (a << 24) | glowCol);
            if (rnd.nextInt(3) == 0) {
                int fa = (a / 3) << 24;
                graphics.fill(px - 1, py, px, py + 1, fa | glowCol);
                graphics.fill(px + 1, py, px + 2, py + 1, fa | glowCol);
                graphics.fill(px, py - 1, px + 1, py, fa | glowCol);
                graphics.fill(px, py + 1, px + 1, py + 2, fa | glowCol);
            }
        }
        graphics.fillGradient(0, 0, w, 50, 0x99060C1C, 0x00060C1C);
        graphics.fillGradient(0, h - 70, w, h, 0x00060C1C, 0xAA060C1C);
    }

    /** The place's name at the top left (with its diamond mark), the stars and currencies at the top right. */
    private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY, int w) {
        int y = 14;
        diamond(graphics, 22, y + 4, 6, 0xFFC9A55A, 0xFF5A4520);
        String place = Component.translatable(selectedTab == 0 ? "gui.awakening_system.spiral_abyss.tab_corridor" : "gui.awakening_system.spiral_abyss.tab_spire").getString();
        graphics.drawString(this.font, place, 34, y, 0xFFE8D29A, true);
        if (detailView)
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.floor_title", selectedFloor).getString(), 34, y + 11, 0xFFB8C4D0, false);
        // the Spire starts again every cycle: how long is left of this one
        if (selectedTab == 1 && (!detailView || selectedFloor >= 9))
            graphics.drawString(this.font, resetLine(), 34, y + (detailView ? 22 : 11), 0xFFC8B8FF, false);
        int x = w - 14;
        String fates = "✦ " + this.intertwinedFates;
        x -= this.font.width(fates);
        graphics.drawString(this.font, fates, x, y, 0xFF9FE0FF, true);
        String primo = "◆ " + this.primogems;
        x -= this.font.width(primo) + 14;
        graphics.drawString(this.font, primo, x, y, 0xFFFFE08A, true);
        String stars = "★ " + this.totalStars + " / 108";
        x -= this.font.width(stars) + 14;
        graphics.drawString(this.font, stars, x, y, 0xFFFFFFFF, true);
    }

    /** A small diamond (a square on its point), used as marks and buttons. */
    private void diamond(GuiGraphics g, int cx, int cy, int r, int fill, int edge) {
        for (int dy = -r; dy <= r; dy++) {
            int half = r - Math.abs(dy);
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, (Math.abs(dy) == r || half == 0) ? edge : fill);
            g.fill(cx - half, cy + dy, cx - half + 1, cy + dy + 1, edge);
            g.fill(cx + half, cy + dy, cx + half + 1, cy + dy + 1, edge);
        }
    }

    /** A banner and what is written on it: the number in its disc, the stars won (x/9), the difficulty marks. */
    private void drawBanner(GuiGraphics graphics, int floor, int x, int y, int bw, int bh, boolean selected) {
        int idx = floor - 1;
        boolean locked = floor > unlockedFloor;
        int state = locked ? 1 : (claimedRewards[idx] ? 2 : 0);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        int tw = AbyssArt.BANNER_W * AbyssArt.K, th = AbyssArt.BANNER_H * AbyssArt.K;
        graphics.blit(AbyssArt.banner(floor, state, selected), x, y, bw, bh, 0, 0, tw, th, tw, th);
        float s = bh / (float) AbyssArt.BANNER_H;
        int cx = x + bw / 2;
        long now = System.currentTimeMillis();
        if (!locked) {
            // the doorway's light breathes, and a mote of it drifts up the stair now and then
            float glow = (float) (Math.sin(now / 700.0 + floor) * 0.5 + 0.5);
            int gx = (int) (cx - 6 * s), gy = (int) (y + 44 * s), gw = (int) (12 * s), gh = (int) (26 * s);
            graphics.fillGradient(gx, gy, gx + gw, gy + gh, ((int) (20 + glow * 50) << 24) | 0xFFFFFF, 0x00FFFFFF);
            for (int m = 0; m < 3; m++) {
                float ph = ((now / 1800.0F) + m / 3.0F + floor * 0.17F) % 1.0F;
                int mx = (int) (cx + Math.sin(ph * 6 + m) * 5 * s), my = (int) (y + (96 - ph * 50) * s);
                int a = (int) (Math.sin(ph * Math.PI) * 200);
                graphics.fill(mx, my, mx + Math.max(1, (int) s), my + Math.max(1, (int) s), (a << 24) | 0xFFFFFF);
            }
        }
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        // the number, dark on the white disc
        String num = String.valueOf(floor);
        graphics.pose().pushPose();
        float ts = Math.max(1.0F, s * 1.25F);
        graphics.pose().translate(cx, y + AbyssArt.DISC_Y * s, 0);
        graphics.pose().scale(ts, ts, 1);
        graphics.drawString(this.font, num, -this.font.width(num) / 2, -4, 0xFF2A3A4C, false);
        graphics.pose().popPose();
        // under the diamond: progress if it is begun but not finished
        int under = (int) (y + (AbyssArt.DIAMOND_Y + 12) * s);
        int stars = starsPerFloor[idx];
        if (state == 2) {
            // collected: the tick is painted under the diamond; nothing else to write
        } else if (!locked) {
            String prog = stars + "/9";
            int pw = this.font.width(prog) + 6;
            graphics.fill(cx - pw / 2, under - 1, cx + pw / 2, under + 9, 0xCC2E3E50);
            graphics.drawString(this.font, prog, cx - this.font.width(prog) / 2, under, 0xFFE8EEF4, false);
        } else if (locked) {
            String lk = Component.translatable("gui.awakening_system.spiral_abyss.locked").getString();
            int pw = this.font.width(lk) + 6;
            graphics.fill(cx - pw / 2, under - 1, cx + pw / 2, under + 9, 0xCC3A4656);
            graphics.drawString(this.font, lk, cx - this.font.width(lk) / 2, under, 0xFFCBD5E1, false);
        }
        // difficulty marks at the foot: one diamond on the first floors, two from floor 5
        // well below the progress label (it read through "LOCKED" before)
        int marks = floor <= 4 ? 1 : 2;
        int my = Math.max(under + 14, (int) (y + (AbyssArt.BANNER_H - 11) * s));
        for (int m = 0; m < marks; m++) {
            int mx = cx + (m - (marks - 1) / 2.0F > 0 ? 4 : (marks == 1 ? 0 : -4));
            diamond(graphics, mx, my, 3, 0xFFFFFFFF, 0xFF8C9EAE);
        }
        // a reward ready to collect shines on the disc
        if (!locked && stars >= 3 && !claimedRewards[idx]) {
            float pulse = (float) (Math.sin(System.currentTimeMillis() / 300.0) * 0.5 + 0.5);
            graphics.fill(cx + (int) (8 * s), y + (int) (14 * s), cx + (int) (12 * s), y + (int) (18 * s), ((int) (120 + pulse * 135) << 24) | 0xFFD84A);
        }
    }

    /** The row of banners, the hint at the bottom left and the Confirm / Leave buttons at the bottom right. */
    private void renderRow(GuiGraphics graphics, int mouseX, int mouseY, int w, int h) {
        int[] g = rowGeometry();
        int n = rowCount();
        for (int i = 0; i < n; i++) {
            int x = g[0] + i * (g[2] + g[4]);
            if (selectedTab == 0 && i == 8) {
                // the rift into the Spire, open once the Corridor's last floor is reached
                boolean open = unlockedFloor >= 9;
                boolean hover = over(mouseX, mouseY, x, g[1], g[2], g[3]);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                int ry = g[1] - (hover ? 3 : 0);
                graphics.blit(AbyssArt.rift(open), x, ry, g[2], g[3], 0, 0, AbyssArt.BANNER_W, AbyssArt.BANNER_H, AbyssArt.BANNER_W, AbyssArt.BANNER_H);
                // the rift swirls: sparks spiralling into its dark heart
                graphics.enableScissor(x + g[2] / 8, ry + g[3] / 10, x + g[2] - g[2] / 8, ry + g[3] - g[3] / 10);
                long tnow = System.currentTimeMillis();
                int rcx = x + g[2] / 2, rcy = ry + g[3] / 2;
                // a slow pulse of light in its heart, and streaks of sparks spiralling into it
                float beat = (float) (Math.sin(tnow / 500.0) * 0.5 + 0.5);
                for (int k = 6; k >= 1; k--) {
                    int rr = (int) (g[2] * 0.09F * k * (0.9F + beat * 0.25F));
                    graphics.fill(rcx - rr, rcy - rr * 2, rcx + rr, rcy + rr * 2, ((int) ((open ? 18 : 8) + beat * 10) << 24) | 0xB070FF);
                }
                for (int sp = 0; sp < 70; sp++) {
                    float ph = ((tnow / 2200.0F) + sp / 70.0F) % 1.0F;
                    double ang = sp * 2.4 + ph * 9.0;
                    double rad = (1.0 - ph) * g[3] * 0.5;
                    int a = (int) (Math.sin(ph * Math.PI) * (open ? 255 : 120));
                    int col = sp % 4 == 0 ? 0xFFFFFF : (sp % 4 == 1 ? 0xE0B8FF : 0xA060FF);
                    // each spark leaves a short tail behind it along its curve
                    for (int tail = 0; tail < 4; tail++) {
                        double ta = ang - tail * 0.12, tr = rad + tail * 2.5;
                        int px = (int) (rcx + Math.cos(ta) * tr * 0.42), py = (int) (rcy + Math.sin(ta) * tr);
                        int ta2 = Math.max(0, a - tail * 60);
                        graphics.fill(px, py, px + (tail == 0 ? 3 : 2), py + (tail == 0 ? 3 : 2), (ta2 << 24) | col);
                    }
                }
                graphics.disableScissor();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                if (!open) graphics.drawCenteredString(this.font, "🔒", x + g[2] / 2, g[1] + g[3] / 2 - 4, 0xFFE0D8F0);
                continue;
            }
            int floor = rowFirstFloor() + i;
            boolean sel = floor == selectedFloor;
            boolean hover = over(mouseX, mouseY, x, g[1], g[2], g[3]);
            int lift = sel ? 6 : (hover ? 3 : 0);
            int extra = sel ? g[2] / 10 : 0;
            drawBanner(graphics, floor, x - extra, g[1] - lift - extra, g[2] + extra * 2, g[3] + extra * 2, sel);
        }
        // the Spire's way back to the Corridor: a round arrow at the left
        if (selectedTab == 1) {
            int bx = 30, by = g[1] + g[3] / 2;
            boolean hover = over(mouseX, mouseY, bx - 14, by - 14, 28, 28);
            circle(graphics, bx, by, 13, hover ? 0xEE3A5A66 : 0xCC22343E, 0xFFBFD8E0);
            graphics.drawCenteredString(this.font, "◀", bx, by - 4, 0xFFEAF6FA);
        }
        // the hint
        String hint = Component.translatable(selectedTab == 0 ? "gui.awakening_system.spiral_abyss.hint_corridor" : "gui.awakening_system.spiral_abyss.hint_spire").getString();
        graphics.drawString(this.font, hint, 30, h - 40, 0xFFE8D29A, true);
        // the buttons
        String[] labels = rowLabels();
        int[] bx = buttonXs(labels);
        actionButton(graphics, mouseX, mouseY, bx[0], h - 34, "○", labels[0]);
        actionButton(graphics, mouseX, mouseY, bx[1], h - 34, "✕", labels[1]);
    }

    private void circle(GuiGraphics g, int cx, int cy, int r, int fill, int edge) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, fill);
            g.fill(cx - half, cy + dy, cx - half + 1, cy + dy + 1, edge);
            g.fill(cx + half, cy + dy, cx + half + 1, cy + dy + 1, edge);
        }
        g.fill(cx - (int) Math.sqrt(r * r - (r - 1) * (r - 1)), cy - r, cx + (int) Math.sqrt(r * r - (r - 1) * (r - 1)) + 1, cy - r + 1, edge);
        g.fill(cx - (int) Math.sqrt(r * r - (r - 1) * (r - 1)), cy + r, cx + (int) Math.sqrt(r * r - (r - 1) * (r - 1)) + 1, cy + r + 1, edge);
    }

    private String[] rowLabels() {
        return new String[]{Component.translatable("gui.awakening_system.spiral_abyss.confirm").getString(),
                Component.translatable("gui.awakening_system.spiral_abyss.leave").getString()};
    }

    private String[] detailLabels() {
        int idx = selectedFloor - 1;
        boolean canClaim = starsPerFloor[idx] >= 3 && !claimedRewards[idx];
        return new String[]{Component.translatable("gui.awakening_system.spiral_abyss.history").getString(),
                Component.translatable("gui.awakening_system.spiral_abyss.chamber_treasure").getString(),
                Component.translatable(claimedRewards[idx] ? "gui.awakening_system.spiral_abyss.rewards_claimed"
                        : (canClaim ? "gui.awakening_system.spiral_abyss.collect" : "gui.awakening_system.spiral_abyss.collect_locked")).getString(),
                Component.translatable(selectedFloor > unlockedFloor ? "gui.awakening_system.spiral_abyss.challenge_locked" : "gui.awakening_system.spiral_abyss.start").getString(),
                Component.translatable("gui.awakening_system.spiral_abyss.back").getString()};
    }

    private int buttonWidth(String label) {
        return 24 + this.font.width(label);
    }

    /** The buttons of the bottom row, laid from the right edge leftwards with room between them. */
    private int[] buttonXs(String[] labels) {
        int[] xs = new int[labels.length];
        int x = this.width - 24;
        for (int i = labels.length - 1; i >= 0; i--) {
            x -= buttonWidth(labels[i]);
            xs[i] = x;
            x -= 16;
        }
        return xs;
    }

    /** A Genshin-like button: a round mark and its word. */
    private void actionButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, String mark, String label) {
        boolean hover = over(mouseX, mouseY, x, y, buttonWidth(label), 18);
        if (hover) g.fill(x - 3, y - 2, x + buttonWidth(label) + 2, y + 19, 0x44FFFFFF);
        circle(g, x + 8, y + 8, 8, hover ? 0xFFF2F5F8 : 0x00000000, 0xFFE8EEF4);
        g.drawCenteredString(this.font, mark, x + 8, y + 4, hover ? 0xFF2A3A4C : 0xFFE8EEF4);
        g.drawString(this.font, label, x + 21, y + 4, hover ? 0xFFFFFFFF : 0xFFE8EEF4, true);
    }

    /** The chamber treasure window is open over the floor's view. */
    private boolean treasureOpen;
    /** The creatures shown for each chamber and half (made once, kept while the window is open). */
    private final java.util.Map<String, List<net.minecraft.world.entity.Mob>> previews = new java.util.HashMap<>();

    /**
     * Who waits in a chamber's half: the very creatures the server sends (SpiralAbyssManager.createAbyssMob),
     * made here only to be drawn, one of each kind.
     */
    private List<net.minecraft.world.entity.Mob> chamberMobs(int floor, int chamber, int half) {
        String key = floor + ":" + chamber + ":" + half;
        List<net.minecraft.world.entity.Mob> done = previews.get(key);
        if (done != null) return done;
        List<net.minecraft.world.entity.Mob> list = new java.util.ArrayList<>();
        java.util.Set<String> kinds = new java.util.HashSet<>();
        if (this.minecraft != null && this.minecraft.level != null) {
            // the same count as the wave on the server: 6 + floor/2 + 2 per chamber, one more in the second half
            int count = 6 + floor / 2 + chamber * 2 + (half == 2 ? 1 : 0);
            for (int i = 0; i < count && list.size() < 4; i++) {
                try {
                    net.minecraft.world.entity.Mob m = SpiralAbyssManager.createAbyssMob(this.minecraft.level, floor, chamber, half, i);
                    if (m == null) continue;
                    String kind = m.getType().toString() + "|" + (m.getCustomName() != null ? m.getCustomName().getString() : "")
                            + (m instanceof com.soybe.awakeningsystem.entity.HilichurlEntity hc ? hc.getRole() : "");
                    if (kinds.add(kind)) list.add(m);
                } catch (Throwable ignored) {
                }
            }
        }
        previews.put(key, list);
        return list;
    }

    /** A creature's card: a framed portrait with the creature turning slowly in it, its level under it. */
    private void mobCard(GuiGraphics g, net.minecraft.world.entity.Mob mob, int x, int y, int size, int level, int mouseX, int mouseY) {
        boolean boss = mob.getBbHeight() > 2.2F || mob instanceof net.minecraft.world.entity.monster.WitherSkeleton
                || mob instanceof net.minecraft.world.entity.animal.IronGolem;
        g.fillGradient(x, y, x + size, y + size, boss ? 0xFF8A5A2A : 0xFF5A4A8C, boss ? 0xFFD49A4A : 0xFF9A84D0);
        g.renderOutline(x, y, size, size, 0xFFE8E2F4);
        g.enableScissor(x + 1, y + 1, x + size - 1, y + size - 1);
        float h = Math.max(0.6F, mob.getBbHeight());
        float scale = (size * 0.62F) / h;
        float turn = (System.currentTimeMillis() % 12000L) / 12000.0F * (float) Math.PI * 2;
        org.joml.Quaternionf pose = new org.joml.Quaternionf().rotateZ((float) Math.PI).rotateY(turn);
        try {
            net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory(g, x + size / 2.0F, y + size * 0.88F, scale,
                    new org.joml.Vector3f(), pose, null, mob);
        } catch (Throwable ignored) {
        }
        g.disableScissor();
        g.fill(x, y + size - 10, x + size, y + size, 0xCC2A2438);
        String lv = Component.translatable("gui.awakening_system.spiral_abyss.level_short", level).getString();
        g.drawCenteredString(this.font, lv, x + size / 2, y + size - 9, 0xFFE8E2F4);
        if (over(mouseX, mouseY, x, y, size, size)) g.renderTooltip(this.font, mob.getDisplayName(), mouseX, mouseY);
    }

    /** The floor's own view: its banner at the left; at the right its ley line disorder, chambers and foes. */
    private void renderDetailView(GuiGraphics graphics, int mouseX, int mouseY, int w, int h) {
        int bh = Math.min((int) (h * 0.62), 260), bw = (bh * AbyssArt.BANNER_W) / AbyssArt.BANNER_H;
        int bx = Math.max(24, w / 7 - bw / 2), by = (h - bh) / 2;
        drawBanner(graphics, selectedFloor, bx, by, bw, bh, true);
        int px = bx + bw + Math.max(20, w / 20), pw = Math.min(w - px - 30, 420);
        int py = Math.max(40, by - 10);
        int idx = selectedFloor - 1;
        // a translucent sheet behind the text, like the game's
        graphics.fillGradient(px - 10, py - 8, px + pw + 10, h - 50, 0x88101830, 0x55101830);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.floor_title", selectedFloor).getString(), px, py, 0xFFFFFFFF, true);
        String badge = getElementalBadge(selectedFloor);
        graphics.drawString(this.font, badge, px + pw - this.font.width(badge), py, getElementalColor(selectedFloor), true);
        graphics.fill(px, py + 12, px + pw, py + 13, 0x66E8EEF4);
        int y = py + 18;
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.disorder_title").getString(), px, y, 0xFFE8D29A, false);
        y += 11;
        for (var line : this.font.split(Component.literal("✦ " + getLeyLineDisorderText(selectedFloor)), pw)) {
            graphics.drawString(this.font, line, px, y, 0xFFD8E2EA, false);
            y += 10;
        }
        y += 6;
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.challenge_target").getString(), px, y, 0xFFE8D29A, false);
        y += 12;
        int stars = starsPerFloor[idx];
        for (int c = 1; c <= 3; c++) {
            int rowY = y + (c - 1) * 21;
            boolean sel = c == selectedChamber;
            boolean hover = over(mouseX, mouseY, px, rowY, pw, 18);
            graphics.fill(px, rowY, px + pw, rowY + 18, sel ? 0xCC3A4C66 : (hover ? 0xAA2E3C52 : 0x99202C40));
            if (sel) graphics.renderOutline(px, rowY, pw, 18, 0xFFE8EEF4);
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.chamber_tab", c).getString(), px + 8, rowY + 5, 0xFFFFFFFF, false);
            int won = Math.max(0, Math.min(3, stars - (c - 1) * 3));
            for (int s = 0; s < 3; s++)
                graphics.drawString(this.font, "✹", px + pw - 40 + s * 12, rowY + 5, s < won ? 0xFFFFF2C8 : 0xFF56647A, false);
        }
        y += 3 * 21 + 6;
        // the foes of the chosen chamber, the first and second half side by side
        int rec = getRecommendedLevel(selectedFloor);
        int halves = SpiralAbyssManager.isTwoHalvesFloor(selectedFloor) ? 2 : 1;
        int card = Math.max(28, Math.min(46, (pw - 30) / (halves * 4 + 1)));
        for (int half = 1; half <= halves; half++) {
            int hx = px + (half - 1) * (pw / 2 + 6);
            List<net.minecraft.world.entity.Mob> mobs = chamberMobs(selectedFloor, selectedChamber, half);
            for (int i = 0; i < mobs.size(); i++)
                mobCard(graphics, mobs.get(i), hx + i * (card + 4), y, card, rec + (selectedChamber - 1) * 2 + (half - 1), mouseX, mouseY);
            String label = Component.translatable(halves == 1 ? "gui.awakening_system.spiral_abyss.single_half" : (half == 1
                    ? "gui.awakening_system.spiral_abyss.half_first_short" : "gui.awakening_system.spiral_abyss.half_second_short")).getString();
            int span = Math.max(card, mobs.size() * (card + 4) - 4);
            graphics.drawCenteredString(this.font, label, hx + span / 2, y + card + 4, 0xFFE8E2F4);
        }
        y += card + 18;
        String lvl = Component.translatable("gui.awakening_system.spiral_abyss.level_gauge", rec,
                Component.translatable(this.playerLevel >= rec ? "gui.awakening_system.spiral_abyss.level_met" : "gui.awakening_system.spiral_abyss.level_low").getString()).getString();
        graphics.drawString(this.font, lvl, px, y, this.playerLevel >= rec ? 0xFF7EE08A : 0xFFF08080, false);
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.starts_at", selectedFloor, selectedChamber).getString(),
                px, y + 12, 0xFF9AA6B8, false);
        String[] labels = detailLabels();
        int[] bxs = buttonXs(labels);
        actionButton(graphics, mouseX, mouseY, bxs[0], h - 34, "≡", labels[0]);
        actionButton(graphics, mouseX, mouseY, bxs[1], h - 34, "❖", labels[1]);
        actionButton(graphics, mouseX, mouseY, bxs[2], h - 34, "□", labels[2]);
        actionButton(graphics, mouseX, mouseY, bxs[3], h - 34, "▷", labels[3]);
        actionButton(graphics, mouseX, mouseY, bxs[4], h - 34, "✕", labels[4]);
        if (treasureOpen) renderTreasure(graphics, mouseX, mouseY, w, h);
        if (historyOpen) renderHistory(graphics, mouseX, mouseY, w, h);
    }

    /** A team member as written in a record: "@" a shadow of a creature, "#" a creature, else a given name. */
    private static String memberName(String s) {
        if (s.startsWith("@"))
            return Component.translatable("awakening_system.msg.sombra", Component.translatable(s.substring(1)).getString()).getString();
        if (s.startsWith("#")) return Component.translatable(s.substring(1)).getString();
        return s;
    }

    /** The floor's record: its latest clears, newest first, each with its stars, date and team. */
    private void renderHistory(GuiGraphics g, int mouseX, int mouseY, int w, int h) {
        g.fill(0, 0, w, h, 0x99000000);
        int ww = Math.min(w - 60, 440), wh = Math.min(h - 60, 250);
        int x = (w - ww) / 2, y = (h - wh) / 2;
        g.fill(x, y, x + ww, y + wh, 0xF0303A52);
        g.renderOutline(x, y, ww, wh, 0xFF9AA8C0);
        g.renderOutline(x + 3, y + 3, ww - 6, wh - 6, 0x66C8D0E0);
        for (int[] cn : new int[][]{{x, y}, {x + ww - 6, y}, {x, y + wh - 6}, {x + ww - 6, y + wh - 6}}) g.fill(cn[0], cn[1], cn[0] + 6, cn[1] + 6, 0xFFE8D29A);
        g.drawCenteredString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.history_title", selectedFloor).getString(), x + ww / 2, y + 10, 0xFFE8D29A);
        g.drawString(this.font, "✕", x + ww - 16, y + 9, over(mouseX, mouseY, x + ww - 20, y + 5, 14, 14) ? 0xFFFFFFFF : 0xFFB8C0D0, false);
        List<SpiralAbyssManager.ClearRecord> mine = new java.util.ArrayList<>();
        for (var r : history) if (r.floor() == selectedFloor) mine.add(0, r);
        int rowY = y + 28;
        if (mine.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.history_empty").getString(), x + ww / 2, y + wh / 2 - 4, 0xFFB8C4D0);
            return;
        }
        java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT);
        int rowH = Math.min(40, (wh - 36) / Math.max(1, mine.size()));
        for (var r : mine) {
            g.fill(x + 12, rowY, x + ww - 12, rowY + rowH - 4, 0xCC404C66);
            g.drawString(this.font, r.player(), x + 20, rowY + 5, 0xFFFFFFFF, false);
            String st = "✹ " + r.stars() + "/9";
            g.drawString(this.font, st, x + ww - 20 - this.font.width(st), rowY + 5, r.stars() >= 9 ? 0xFFFFE08A : 0xFFFFF2C8, false);
            String date = df.format(new java.util.Date(r.timeMs()));
            g.drawString(this.font, date, x + ww - 30 - this.font.width(st) - this.font.width(date), rowY + 5, 0xFF9AA6B8, false);
            StringBuilder team = new StringBuilder();
            for (String s : r.team()) team.append(team.isEmpty() ? "" : ", ").append(memberName(s));
            String line = Component.translatable("gui.awakening_system.spiral_abyss.history_team",
                    team.isEmpty() ? Component.translatable("gui.awakening_system.spiral_abyss.history_alone").getString() : team.toString()).getString();
            if (rowH >= 30) g.drawString(this.font, this.font.plainSubstrByWidth(line, ww - 40), x + 20, rowY + 18, 0xFFC8D0E0, false);
            rowY += rowH;
        }
    }

    /** The rewards of one star step of a floor, as the server gives them (SpiralAbyssManager.handleClaimReward). */
    private List<ItemStack> stepRewards(int step) {
        List<ItemStack> out = new java.util.ArrayList<>();
        int[] primos = {100, 100, 100}, coins = {20000, 30000, 50000}, fates = {0, 1, 2};
        if (SystemItems.PRIMOGEM.isBound()) out.add(new ItemStack(SystemItems.PRIMOGEM.get(), primos[step]));
        if (SystemItems.MORA.isBound()) out.add(new ItemStack(SystemItems.MORA.get(), 1));
        if (fates[step] > 0 && SystemItems.INTERTWINED_FATE.isBound()) out.add(new ItemStack(SystemItems.INTERTWINED_FATE.get(), fates[step]));
        return out;
    }

    /** The chamber treasure window: each star step of the floor and what it gives, with the chamber chest. */
    private void renderTreasure(GuiGraphics g, int mouseX, int mouseY, int w, int h) {
        g.fill(0, 0, w, h, 0x99000000);
        int ww = Math.min(w - 60, 440), wh = Math.min(h - 60, 230);
        int x = (w - ww) / 2, y = (h - wh) / 2;
        g.fill(x, y, x + ww, y + wh, 0xF0303A52);
        g.renderOutline(x, y, ww, wh, 0xFF9AA8C0);
        g.renderOutline(x + 3, y + 3, ww - 6, wh - 6, 0x66C8D0E0);
        for (int[] cn : new int[][]{{x, y}, {x + ww - 6, y}, {x, y + wh - 6}, {x + ww - 6, y + wh - 6}}) g.fill(cn[0], cn[1], cn[0] + 6, cn[1] + 6, 0xFFE8D29A);
        String title = Component.translatable("gui.awakening_system.spiral_abyss.chamber_treasure").getString();
        g.drawCenteredString(this.font, title, x + ww / 2, y + 10, 0xFFE8D29A);
        g.drawString(this.font, "✕", x + ww - 16, y + 9, over(mouseX, mouseY, x + ww - 20, y + 5, 14, 14) ? 0xFFFFFFFF : 0xFFB8C0D0, false);
        int rowY = y + 28;
        String[] steps = {"✹", "✹✹", "✹✹✹"};
        int[] needs = {3, 6, 9};
        int[] coinAmounts = {20000, 30000, 50000};
        for (int s = 0; s < 3; s++) {
            g.fill(x + 12, rowY, x + ww - 12, rowY + 40, 0xCC404C66);
            String name = Component.translatable("gui.awakening_system.spiral_abyss.treasure_step", selectedFloor, needs[s]).getString();
            g.drawString(this.font, name, x + 22, rowY + 9, 0xFFFFFFFF, false);
            g.drawString(this.font, steps[s], x + 22, rowY + 22, starsPerFloor[selectedFloor - 1] >= needs[s] ? 0xFFFFF2C8 : 0xFF6A7890, false);
            List<ItemStack> items = stepRewards(s);
            int ix = x + ww - 22 - items.size() * 34;
            for (ItemStack it : items) {
                g.fill(ix, rowY + 4, ix + 30, rowY + 36, 0xFF56688A);
                g.renderItem(it, ix + 7, rowY + 7);
                String amount = it.is(SystemItems.MORA.get()) ? formatNumber(coinAmounts[s]) : String.valueOf(it.getCount());
                g.drawCenteredString(this.font, amount, ix + 15, rowY + 25, 0xFFFFFFFF);
                ix += 34;
            }
            rowY += 44;
        }
        // the chest each chamber leaves when cleared
        g.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.chamber_chest", formatNumber(20000 * selectedFloor), 100, 2 + selectedFloor).getString(),
                x + 16, rowY + 4, 0xFFC8D0E0, false);
    }

    // ---- clicks

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int w = this.width, h = this.height;
        if (detailView && (treasureOpen || historyOpen)) {
            // any click closes the treasure window or the record
            treasureOpen = false;
            historyOpen = false;
            playClickSound();
            return true;
        }
        if (detailView) {
            int bh = Math.min((int) (h * 0.62), 260), bw = (bh * AbyssArt.BANNER_W) / AbyssArt.BANNER_H;
            int bx = Math.max(24, w / 7 - bw / 2), by = (h - bh) / 2;
            int px = bx + bw + Math.max(20, w / 20), pw = Math.min(w - px - 30, 420);
            int py = Math.max(40, by - 10);
            int idx = selectedFloor - 1;
            int y = py + 18 + 11 + 10 * this.font.split(Component.literal("✦ " + getLeyLineDisorderText(selectedFloor)), pw).size() + 6 + 12;
            for (int c = 1; c <= 3; c++) {
                if (over(mouseX, mouseY, px, y + (c - 1) * 21, pw, 18)) {
                    if (selectedChamber != c) playClickSound();
                    selectedChamber = c;
                    return true;
                }
            }
            String[] labels = detailLabels();
            int[] bxs = buttonXs(labels);
            if (over(mouseX, mouseY, bxs[0], h - 34, buttonWidth(labels[0]), 18)) {
                historyOpen = true;
                playClickSound();
                return true;
            }
            if (over(mouseX, mouseY, bxs[1], h - 34, buttonWidth(labels[1]), 18)) {
                treasureOpen = true;
                playClickSound();
                return true;
            }
            if (over(mouseX, mouseY, bxs[2], h - 34, buttonWidth(labels[2]), 18)) {
                if (starsPerFloor[idx] >= 3 && !claimedRewards[idx]) {
                    PacketDistributor.sendToServer(new ClaimAbyssRewardPayload(this.selectedFloor));
                    if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                } else playClickSound();
                return true;
            }
            if (over(mouseX, mouseY, bxs[3], h - 34, buttonWidth(labels[3]), 18)) {
                if (this.selectedFloor <= this.unlockedFloor) {
                    PacketDistributor.sendToServer(new StartAbyssFloorPayload(this.selectedFloor));
                    if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.playSound(ModSounds.ABYSS_ENTER.value(), 0.9F, 1.0F);
                    this.onClose();
                } else if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 1.0F);
                }
                return true;
            }
            if (over(mouseX, mouseY, bxs[4], h - 34, buttonWidth(labels[4]), 18)) {
                detailView = false;
                playClickSound();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        // the row of banners
        int[] g = rowGeometry();
        for (int i = 0; i < rowCount(); i++) {
            int x = g[0] + i * (g[2] + g[4]);
            if (!over(mouseX, mouseY, x, g[1] - 8, g[2], g[3] + 8)) continue;
            if (selectedTab == 0 && i == 8) {
                if (unlockedFloor >= 9) {
                    selectedTab = 1;
                    selectedFloor = Math.max(9, Math.min(unlockedFloor, 12));
                    if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.playSound(ModSounds.ABYSS_ENTER.value(), 0.6F, 1.2F);
                } else if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 1.0F);
                }
                return true;
            }
            int floor = rowFirstFloor() + i;
            if (floor == selectedFloor && floor <= unlockedFloor) {
                // a second click on the chosen banner opens it, as Confirm does
                detailView = true;
                selectedChamber = 1;
            }
            if (selectedFloor != floor) playClickSound();
            selectedFloor = floor;
            return true;
        }
        if (selectedTab == 1 && over(mouseX, mouseY, 16, g[1] + g[3] / 2 - 14, 28, 28)) {
            selectedTab = 0;
            selectedFloor = Math.max(1, Math.min(unlockedFloor, 8));
            playClickSound();
            return true;
        }
        String[] rl = rowLabels();
        int[] rbx = buttonXs(rl);
        if (over(mouseX, mouseY, rbx[0], h - 34, buttonWidth(rl[0]), 18)) {
            if (selectedFloor <= unlockedFloor) {
                detailView = true;
                selectedChamber = 1;
                playClickSound();
            } else if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 1.0F);
            }
            return true;
        }
        if (over(mouseX, mouseY, rbx[1], h - 34, buttonWidth(rl[1]), 18)) {
            playClickSound();
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Escape in a floor's view goes back to the row first
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && detailView) {
            if (treasureOpen || historyOpen) {
                treasureOpen = false;
                historyOpen = false;
            } else detailView = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playClickSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(ModSounds.SYSTEM_CLICK.value(), 0.5F, 1.2F);
        }
    }

    private static int getRecommendedLevel(int floor) {
        return switch (floor) {
            case 1 -> 25;
            case 2 -> 40;
            case 3 -> 45;
            case 4 -> 50;
            case 5 -> 55;
            case 6 -> 60;
            case 7 -> 70;
            case 8 -> 70;
            case 9 -> 72;
            case 10 -> 80;
            case 11 -> 88;
            case 12 -> 95;
            default -> 25;
        };
    }

    private static String getElementalBadge(int floor) {
        String key = (floor >= 1 && floor <= 12) ? ("gui.awakening_system.spiral_abyss.badge." + floor) : "gui.awakening_system.spiral_abyss.badge.default";
        return Component.translatable(key).getString();
    }

    private static int getElementalColor(int floor) {
        return switch (floor) {
            case 1 -> 0xFF22D3EE;
            case 2 -> 0xFFFB923C;
            case 3 -> 0xFFC084FC;
            case 4 -> 0xFF67E8F9;
            case 5 -> 0xFF60A5FA;
            case 6 -> 0xFFFBBF24;
            case 7 -> 0xFFFACC15;
            case 8 -> 0xFFF87171;
            default -> 0xFFE879F9;
        };
    }

    private static String getTacticalTip(int floor) {
        String key = (floor >= 1 && floor <= 12) ? ("gui.awakening_system.spiral_abyss.tactic." + floor) : "gui.awakening_system.spiral_abyss.tactic.default";
        return Component.translatable(key).getString();
    }

    private static String getLeyLineDisorderText(int floor) {
        String key = (floor >= 1 && floor <= 12) ? ("gui.awakening_system.spiral_abyss.disorder." + floor) : "gui.awakening_system.spiral_abyss.disorder.default";
        return Component.translatable(key).getString();
    }

    private static String getChamberMonsters(int floor, int chamber) {
        String tier = (floor <= 3) ? "f1_3" : ((floor <= 6) ? "f4_6" : "f7_12");
        String chKey = switch (chamber) {
            case 1 -> "c1";
            case 2 -> "c2";
            case 3 -> "c3";
            default -> "default";
        };
        return Component.translatable("gui.awakening_system.spiral_abyss.monsters." + tier + "." + chKey).getString();
    }

    private static String getChamberHalfMonsters(int floor, int chamber, int half) {
        String tier = (floor <= 6) ? "half_low" : "half_high";
        String halfStr = (half == 1) ? "h1" : "h2";
        String chKey = switch (chamber) {
            case 1 -> "c1";
            case 2 -> "c2";
            case 3 -> "c3";
            default -> "default";
        };
        return Component.translatable("gui.awakening_system.spiral_abyss." + tier + "." + halfStr + "." + chKey).getString();
    }

    private static String formatNumber(int val) {
        if (val >= 1_000_000) {
            return String.format(java.util.Locale.US, "%.1fM", val / 1_000_000.0);
        } else if (val >= 10_000) {
            return String.format(java.util.Locale.US, "%.1fk", val / 1000.0);
        }
        return String.valueOf(val);
    }
}
