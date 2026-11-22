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
        graphics.blit(AbyssArt.banner(floor, state, selected), x, y, bw, bh, 0, 0, AbyssArt.BANNER_W, AbyssArt.BANNER_H, AbyssArt.BANNER_W, AbyssArt.BANNER_H);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        float s = bh / (float) AbyssArt.BANNER_H;
        int cx = x + bw / 2;
        // the number, dark on the white disc
        String num = String.valueOf(floor);
        graphics.pose().pushPose();
        float ts = Math.max(1.0F, s * 1.25F);
        graphics.pose().translate(cx, y + AbyssArt.DISC_Y * s, 0);
        graphics.pose().scale(ts, ts, 1);
        graphics.drawString(this.font, num, -this.font.width(num) / 2, -4, 0xFF2A3A4C, false);
        graphics.pose().popPose();
        // under the diamond: progress if it is begun but not finished
        int under = (int) (y + (AbyssArt.DIAMOND_Y + 13) * s);
        int stars = starsPerFloor[idx];
        if (!locked && state != 2) {
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
        int marks = floor <= 4 ? 1 : 2;
        int my = (int) (y + (AbyssArt.BANNER_H - 22) * s);
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
                graphics.blit(AbyssArt.rift(open), x, g[1] - (hover ? 3 : 0), g[2], g[3], 0, 0, AbyssArt.BANNER_W, AbyssArt.BANNER_H, AbyssArt.BANNER_W, AbyssArt.BANNER_H);
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
        actionButton(graphics, mouseX, mouseY, w - 30 - 70, h - 34, "✕", Component.translatable("gui.awakening_system.spiral_abyss.leave").getString());
        actionButton(graphics, mouseX, mouseY, w - 30 - 160, h - 34, "○", Component.translatable("gui.awakening_system.spiral_abyss.confirm").getString());
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

    /** A Genshin-like button: a round mark and its word. 80 wide, 16 tall. */
    private void actionButton(GuiGraphics g, int mouseX, int mouseY, int x, int y, String mark, String label) {
        boolean hover = over(mouseX, mouseY, x, y, 84, 18);
        circle(g, x + 8, y + 8, 8, hover ? 0xFFF2F5F8 : 0x00000000, 0xFFE8EEF4);
        g.drawCenteredString(this.font, mark, x + 8, y + 4, hover ? 0xFF2A3A4C : 0xFFE8EEF4);
        g.drawString(this.font, label, x + 21, y + 4, hover ? 0xFFFFFFFF : 0xFFE8EEF4, true);
    }

    /** The floor's own view: its banner at the left; at the right its ley line disorder and its three chambers. */
    private void renderDetailView(GuiGraphics graphics, int mouseX, int mouseY, int w, int h) {
        int bh = Math.min((int) (h * 0.62), 260), bw = (bh * AbyssArt.BANNER_W) / AbyssArt.BANNER_H;
        int bx = Math.max(24, w / 6 - bw / 2), by = (h - bh) / 2;
        drawBanner(graphics, selectedFloor, bx, by, bw, bh, true);
        int px = bx + bw + Math.max(20, w / 18), pw = Math.min(w - px - 30, 360);
        int py = by + 4;
        int idx = selectedFloor - 1;
        // the title row: Floor N and the place's mood at the right
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.floor_title", selectedFloor).getString(), px, py, 0xFFFFFFFF, true);
        String badge = getElementalBadge(selectedFloor);
        graphics.drawString(this.font, badge, px + pw - this.font.width(badge), py, getElementalColor(selectedFloor), true);
        graphics.fill(px, py + 12, px + pw, py + 13, 0x66E8EEF4);
        // the ley line disorder
        int y = py + 19;
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.disorder_title").getString(), px, y, 0xFFE8D29A, false);
        y += 11;
        for (var line : this.font.split(Component.literal("✦ " + getLeyLineDisorderText(selectedFloor)), pw)) {
            graphics.drawString(this.font, line, px, y, 0xFFD8E2EA, false);
            y += 10;
        }
        y += 8;
        graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.challenge_target").getString(), px, y, 0xFFE8D29A, false);
        y += 12;
        // the three chambers, each with its three stars
        int stars = starsPerFloor[idx];
        for (int c = 1; c <= 3; c++) {
            int rowY = y + (c - 1) * 22;
            boolean sel = c == selectedChamber;
            boolean hover = over(mouseX, mouseY, px, rowY, pw, 19);
            graphics.fill(px, rowY, px + pw, rowY + 19, sel ? 0xCC3A4C60 : (hover ? 0xAA2E3C4E : 0x99202C3A));
            if (sel) graphics.renderOutline(px, rowY, pw, 19, 0xFFE8EEF4);
            graphics.drawString(this.font, Component.translatable("gui.awakening_system.spiral_abyss.chamber_tab", c).getString(), px + 8, rowY + 6, 0xFFFFFFFF, false);
            int won = Math.max(0, Math.min(3, stars - (c - 1) * 3));
            for (int s = 0; s < 3; s++)
                graphics.drawString(this.font, "★", px + pw - 40 + s * 11, rowY + 6, s < won ? 0xFFFFE08A : 0xFF5A6878, false);
        }
        y += 3 * 22 + 6;
        // what waits in the chosen chamber, and the level asked
        String monsters = selectedFloor >= 5
                ? getChamberHalfMonsters(selectedFloor, selectedChamber, 1) + " / " + getChamberHalfMonsters(selectedFloor, selectedChamber, 2)
                : getChamberMonsters(selectedFloor, selectedChamber);
        for (var line : this.font.split(Component.literal(Component.translatable("gui.awakening_system.spiral_abyss.monsters_prefix").getString() + " " + monsters), pw)) {
            graphics.drawString(this.font, line, px, y, 0xFFB8C4D0, false);
            y += 10;
        }
        int rec = getRecommendedLevel(selectedFloor);
        String lvl = Component.translatable("gui.awakening_system.spiral_abyss.level_gauge", rec,
                Component.translatable(this.playerLevel >= rec ? "gui.awakening_system.spiral_abyss.level_met" : "gui.awakening_system.spiral_abyss.level_low").getString()).getString();
        graphics.drawString(this.font, lvl, px, y + 2, this.playerLevel >= rec ? 0xFF7EE08A : 0xFFF08080, false);
        // the buttons: collect the reward, start, back
        boolean canClaim = stars >= 3 && !claimedRewards[idx];
        int bxs = w - 30 - 250;
        actionButton(graphics, mouseX, mouseY, bxs, h - 34, "□", Component.translatable(claimedRewards[idx] ? "gui.awakening_system.spiral_abyss.rewards_claimed"
                : (canClaim ? "gui.awakening_system.spiral_abyss.claim_button" : "gui.awakening_system.spiral_abyss.rewards_requirement")).getString());
        actionButton(graphics, mouseX, mouseY, w - 30 - 160, h - 34, "▷", Component.translatable(selectedFloor > unlockedFloor
                ? "gui.awakening_system.spiral_abyss.challenge_locked" : "gui.awakening_system.spiral_abyss.start").getString());
        actionButton(graphics, mouseX, mouseY, w - 30 - 70, h - 34, "✕", Component.translatable("gui.awakening_system.spiral_abyss.back").getString());
    }

    // ---- clicks

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int w = this.width, h = this.height;
        if (detailView) {
            int bh = Math.min((int) (h * 0.62), 260), bw = (bh * AbyssArt.BANNER_W) / AbyssArt.BANNER_H;
            int bx = Math.max(24, w / 6 - bw / 2), by = (h - bh) / 2;
            int px = bx + bw + Math.max(20, w / 18), pw = Math.min(w - px - 30, 360);
            int idx = selectedFloor - 1;
            // the chamber rows sit below the disorder text: worked out as drawn
            int y = by + 4 + 19 + 11 + 10 * this.font.split(Component.literal("✦ " + getLeyLineDisorderText(selectedFloor)), pw).size() + 8 + 12;
            for (int c = 1; c <= 3; c++) {
                if (over(mouseX, mouseY, px, y + (c - 1) * 22, pw, 19)) {
                    if (selectedChamber != c) playClickSound();
                    selectedChamber = c;
                    return true;
                }
            }
            if (over(mouseX, mouseY, w - 30 - 250, h - 34, 84, 18)) {
                if (starsPerFloor[idx] >= 3 && !claimedRewards[idx]) {
                    PacketDistributor.sendToServer(new ClaimAbyssRewardPayload(this.selectedFloor));
                    if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                } else playClickSound();
                return true;
            }
            if (over(mouseX, mouseY, w - 30 - 160, h - 34, 84, 18)) {
                if (this.selectedFloor <= this.unlockedFloor) {
                    PacketDistributor.sendToServer(new StartAbyssFloorPayload(this.selectedFloor));
                    if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.playSound(ModSounds.ABYSS_ENTER.value(), 0.9F, 1.0F);
                    this.onClose();
                } else if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 1.0F);
                }
                return true;
            }
            if (over(mouseX, mouseY, w - 30 - 70, h - 34, 84, 18)) {
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
        if (over(mouseX, mouseY, w - 30 - 160, h - 34, 84, 18)) {
            if (selectedFloor <= unlockedFloor) {
                detailView = true;
                selectedChamber = 1;
                playClickSound();
            } else if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 1.0F);
            }
            return true;
        }
        if (over(mouseX, mouseY, w - 30 - 70, h - 34, 84, 18)) {
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
            detailView = false;
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
