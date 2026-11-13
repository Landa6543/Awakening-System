package com.soybe.awakeningsystem.client.sao;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Drawing pieces shared by the full-dive menus: discs, rings, pointers, rows and the little pictograms. */
final class SaoDraw {
    private SaoDraw() {}

    /** Where the mouse is and the time, set by each full-dive screen before it draws, for hover and shine. */
    static int mx = -1, my = -1;
    static float time;

    static void frame(int mouseX, int mouseY, float t) {
        mx = mouseX;
        my = mouseY;
        time = t;
    }

    static final int ORANGE = 0xFFEDA500;
    static final int ORANGE_DARK = 0xFFC98700;
    static final int WHITE = 0xFFF7F7F7;
    static final int PANEL = 0xF2FFFFFF;
    static final int PANEL_SOFT = 0xD9DADADA;
    static final int GREY = 0xFF6E6E6E;
    static final int GREY_LIGHT = 0xFFB4B4B4;
    static final int GREY_DARK = 0xFF484848;

    static int alpha(int argb, float a) {
        int al = (int) (((argb >>> 24) & 255) * Math.max(0.0F, Math.min(1.0F, a)));
        return (al << 24) | (argb & 0xFFFFFF);
    }

    /** A filled circle, drawn as one-pixel rows. */
    static void disc(GuiGraphics g, float cx, float cy, float r, int color) {
        int y0 = (int) Math.floor(cy - r), y1 = (int) Math.ceil(cy + r);
        for (int y = y0; y <= y1; y++) {
            float dy = y + 0.5F - cy;
            float half = r * r - dy * dy;
            if (half <= 0) continue;
            float w = (float) Math.sqrt(half);
            g.fill(Math.round(cx - w), y, Math.round(cx + w), y + 1, color);
        }
    }

    static void ring(GuiGraphics g, float cx, float cy, float r, float thickness, int color) {
        int y0 = (int) Math.floor(cy - r), y1 = (int) Math.ceil(cy + r);
        float inner = r - thickness;
        for (int y = y0; y <= y1; y++) {
            float dy = y + 0.5F - cy;
            float o = r * r - dy * dy;
            if (o <= 0) continue;
            float ow = (float) Math.sqrt(o);
            float i = inner * inner - dy * dy;
            if (i <= 0) {
                g.fill(Math.round(cx - ow), y, Math.round(cx + ow), y + 1, color);
            } else {
                float iw = (float) Math.sqrt(i);
                g.fill(Math.round(cx - ow), y, Math.round(cx - iw), y + 1, color);
                g.fill(Math.round(cx + iw), y, Math.round(cx + ow), y + 1, color);
            }
        }
    }

    /** A small triangle pointing left (tip at x,y). */
    static void pointerLeft(GuiGraphics g, int x, int y, int size, int color) {
        for (int i = 0; i < size; i++) g.fill(x + i, y - i, x + i + 1, y + i + 1, color);
    }

    static void pointerRight(GuiGraphics g, int x, int y, int size, int color) {
        for (int i = 0; i < size; i++) g.fill(x - i - 1, y - i, x - i, y + i + 1, color);
    }

    /** One row of a list: a soft white plate, or orange when chosen, with a ringed badge and its pictogram. */
    static void row(GuiGraphics g, Font font, int x, int y, int w, int h, Component label, String[] icon, boolean hot, float a) {
        g.fill(x + 1, y + 2, x + w + 1, y + h + 2, alpha(0x40000000, a));                       // drop shadow
        boolean over = !hot && mx >= x && mx < x + w && my >= y && my < y + h;
        if (over) x += 2;                                                                          // nudges toward the mouse
        if (hot) g.fillGradient(x, y, x + w, y + h, alpha(0xFFF7B52A, a), alpha(0xFFE29400, a));
        else if (over) g.fillGradient(x, y, x + w, y + h, alpha(0xFFFFFFFF, a), alpha(0xFFFFF3DC, a));
        else g.fillGradient(x, y, x + w, y + h, alpha(0xFAFFFFFF, a), alpha(0xF2E9E9EC, a));
        if (hot) {                                                                                  // a band of light sweeping across
            int sweep = (int) ((time * 4) % (w + 40)) - 20;
            for (int i = 0; i < 14; i++) {
                int sx = x + sweep + i;
                if (sx >= x && sx < x + w) g.fill(sx, y + 1, sx + 1, y + h - 1, alpha(0x40FFFFFF, a * (1 - Math.abs(i - 7) / 7.0F)));
            }
        }
        if (over) g.fill(x, y + h - 1, x + w, y + h, alpha(ORANGE, a));
        g.fill(x, y, x + w, y + 1, alpha(hot ? 0xFFFFD27A : 0xFFFFFFFF, a));                    // top light edge
        g.fill(x, y + h - 1, x + w, y + h, alpha(hot ? ORANGE_DARK : 0xFFC9C9CE, a));
        g.fill(x, y, x + 2, y + h, alpha(hot ? 0xFFFFFFFF : ORANGE, a * (hot ? 0.8F : 0.55F)));   // accent bar
        // faint diagonal grain across the plate
        for (int i = 8; i < w; i += 6) g.fill(x + i, y + 2, x + i + 1, y + h - 2, alpha(hot ? 0x14FFFFFF : 0x0A000000, a));
        float cy = y + h / 2.0F;
        ring(g, x + 13, cy, 8.5F, 1.0F, alpha(hot ? 0xFFFFE2A8 : 0xFFB8B8BE, a));
        shinyDisc(g, x + 13, cy, 7.2F, hot ? WHITE : 0xFF5E5E66, a);
        icon(g, icon, x + 13 - 5, Math.round(cy) - 5, alpha(hot ? ORANGE : WHITE, a));
        g.drawString(font, label, x + 27, y + (h - 8) / 2, alpha(hot ? WHITE : (over ? 0xFF2E2E34 : 0xFF4A4A52), a), false);
        if (hot) pointerRight(g, x + w - 6, Math.round(cy), 4, alpha(WHITE, a));
    }

    /** A disc with depth: soft shadow under it, the colour, a lighter cap and a small glint. */
    static void shinyDisc(GuiGraphics g, float cx, float cy, float r, int color, float a) {
        disc(g, cx + 0.6F, cy + 1.2F, r, alpha(0x50000000, a));
        disc(g, cx, cy, r, alpha(color, a));
        disc(g, cx, cy - r * 0.28F, r * 0.72F, alpha(0x30FFFFFF, a));
        disc(g, cx - r * 0.35F, cy - r * 0.45F, Math.max(1.0F, r * 0.18F), alpha(0x90FFFFFF, a));
    }

    /**
     * A floating panel: shadow, white body fading to light grey, a darker title strip with a thin orange
     * line under it, faint horizontal grain and corner ticks.
     */
    static void panel(GuiGraphics g, int x, int y, int w, int h, int headerH, float a) {
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, alpha(0x55000000, a));
        g.fillGradient(x, y, x + w, y + h, alpha(0xF8FFFFFF, a), alpha(0xF0E4E4E8, a));
        for (int yy = y + headerH + 3; yy < y + h - 2; yy += 3) g.fill(x + 3, yy, x + w - 3, yy + 1, alpha(0x07000000, a));
        if (headerH > 0) {
            g.fillGradient(x, y, x + w, y + headerH, alpha(0xFFFDFDFD, a), alpha(0xFFE6E6EA, a));
            g.fill(x, y + headerH, x + w, y + headerH + 1, alpha(ORANGE, a));
            g.fill(x + w / 2 - 18, y + headerH, x + w / 2 + 18, y + headerH + 2, alpha(ORANGE, a));
        }
        g.fill(x, y, x + w, y + 1, alpha(0xFFFFFFFF, a));
        g.fill(x, y + h - 1, x + w, y + h, alpha(0xFFBDBDC3, a));
        int t = 0xFF8A8A92;
        g.fill(x, y, x + 6, y + 1, alpha(t, a)); g.fill(x, y, x + 1, y + 6, alpha(t, a));
        g.fill(x + w - 6, y, x + w, y + 1, alpha(t, a)); g.fill(x + w - 1, y, x + w, y + 6, alpha(t, a));
        g.fill(x, y + h - 1, x + 6, y + h, alpha(t, a)); g.fill(x, y + h - 6, x + 1, y + h, alpha(t, a));
        g.fill(x + w - 6, y + h - 1, x + w, y + h, alpha(t, a)); g.fill(x + w - 1, y + h - 6, x + w, y + h, alpha(t, a));
    }

    /** A progress bar with a groove, a glossy fill and tick marks every tenth. */
    static void bar(GuiGraphics g, int x, int y, int w, int h, float frac, int color, float a) {
        g.fill(x, y, x + w, y + h, alpha(0xFFCACACF, a));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, alpha(0xFFF4F4F6, a));
        int fw = Math.round((w - 2) * Math.max(0.0F, Math.min(1.0F, frac)));
        if (fw > 0) {
            g.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, alpha(color, a));
            g.fill(x + 1, y + 1, x + 1 + fw, y + 1 + Math.max(1, (h - 2) / 2), alpha(0x40FFFFFF, a));
        }
        for (int i = 1; i < 10; i++) g.fill(x + w * i / 10, y + 1, x + w * i / 10 + 1, y + h - 1, alpha(0x18000000, a));
    }

    /** A small round button with a label under or inside it. */
    static boolean roundButton(GuiGraphics g, float cx, float cy, float r, String[] icon, int color, boolean enabled, int mouseX, int mouseY, float a) {
        boolean hover = enabled && (mouseX - cx) * (mouseX - cx) + (mouseY - cy) * (mouseY - cy) <= r * r;
        ring(g, cx, cy, r + 2, 1.0F, alpha(hover ? color : 0xFFB8B8BE, a));
        shinyDisc(g, cx, cy, r, enabled ? (hover ? color : WHITE) : 0xFFD2D2D6, a);
        icon(g, icon, Math.round(cx) - 5, Math.round(cy) - 5, alpha(enabled ? (hover ? WHITE : color) : 0xFFA0A0A6, a));
        return hover;
    }

    /** The dim world behind the menus: darker towards the edges, with a faint grid of light. */
    static void backdrop(GuiGraphics g, int w, int h, float a) {
        g.fillGradient(0, 0, w, h, alpha(0x40101018, a), alpha(0x70101018, a));
        g.fillGradient(0, 0, w, h / 5, alpha(0x50000000, a), 0);
        g.fillGradient(0, h - h / 5, w, h, 0, alpha(0x50000000, a));
        for (int x = 0; x < w; x += 24) g.fill(x, 0, x + 1, h, alpha(0x06FFFFFF, a));
        for (int y = 0; y < h; y += 24) g.fill(0, y, w, y + 1, alpha(0x06FFFFFF, a));
    }

    static final String[] PLUS = {
            "...........", "....###....", "....###....", "....###....", ".#########.", ".#########.",
            ".#########.", "....###....", "....###....", "....###....", "..........."};
    static final String[] MINUS = {
            "...........", "...........", "...........", "...........", ".#########.", ".#########.",
            ".#########.", "...........", "...........", "...........", "..........."};
    static final String[] UP = {
            ".....#.....", "....###....", "...#####...", "..#######..", ".#########.", "....###....",
            "....###....", "....###....", "....###....", "....###....", "..........."};
    static final String[] FIST = {
            "...........", "..#.#.#.#..", ".#########.", ".#########.", ".#########.", ".#########.",
            "..#######..", "..#######..", "...#####...", "...#####...", "..........."};

    /** Draws an 11x11 pictogram written as rows of '#'. */
    static void icon(GuiGraphics g, String[] rows, int x, int y, int color) {
        if (rows == null) return;
        for (int r = 0; r < rows.length; r++) {
            String line = rows[r];
            int start = -1;
            for (int c = 0; c <= line.length(); c++) {
                boolean on = c < line.length() && line.charAt(c) == '#';
                if (on && start < 0) start = c;
                if (!on && start >= 0) {
                    g.fill(x + start, y + r, x + c, y + r + 1, color);
                    start = -1;
                }
            }
        }
    }

    // ── pictograms (11 wide, 11 tall) ────────────────────────────────────────
    static final String[] PERSON = {
            "....###....", "...#####...", "...#####...", "...#####...", "....###....", "...........",
            "..#######..", ".#########.", ".#########.", ".#########.", ".#########."};
    static final String[] PEOPLE = {
            "..##...##..", ".####.####.", ".####.####.", "..##...##..", "...........", ".###...###.",
            "#####.#####", "#####.#####", "#####.#####", "#####.#####", "..........."};
    static final String[] CHAT = {
            "...........", ".#########.", "###########", "###########", "###########", "###########",
            "###########", ".#########.", "...###.....", "...##......", "...#......."};
    static final String[] SCROLL = {
            ".#########.", ".#########.", ".##.....##.", ".#########.", ".##.....##.", ".#########.",
            ".##...####.", ".#########.", ".#########.", "...........", "..........."};
    static final String[] COIN = {
            "...#####...", "..#######..", ".###.#.###.", ".##.....##.", ".###.#####.", ".##.....##.",
            ".#####.###.", ".##.....##.", ".###.#.###.", "..#######..", "...#####..."};
    static final String[] GEAR = {
            "....###....", ".#.#####.#.", ".#########.", "..###.###..", "###.....###", "###.....###",
            "###.....###", "..###.###..", ".#########.", ".#.#####.#.", "....###...."};
    static final String[] SWORD = {
            ".........##", "........###", ".......###.", "......###..", ".....###...", "#...###....",
            "##.###.....", ".####......", "..###......", ".##.##.....", "##...#....."};
    static final String[] SHIELD = {
            "...........", ".#########.", ".#########.", ".#########.", ".#########.", ".#########.",
            "..#######..", "..#######..", "...#####...", "....###....", ".....#....."};
    static final String[] STAR = {
            ".....#.....", ".....#.....", "....###....", "###########", ".#########.", "..#######..",
            "..#######..", ".####.####.", ".###...###.", "##.......##", "..........."};
    static final String[] TROPHY = {
            "###########", "#.#######.#", "#.#######.#", ".#########.", "..#######..", "...#####...",
            "....###....", ".....#.....", ".....#.....", "...#####...", "..#######.."};
    static final String[] BARS = {
            "...........", ".......###.", ".......###.", "...###.###.", "...###.###.", "...###.###.",
            "##.###.###.", "##.###.###.", "##.###.###.", "##.###.###.", "..........."};
    static final String[] DOOR = {
            ".######....", ".#....#....", ".#....#.#..", ".#....#..#.", ".#..#######", ".#....#..#.",
            ".#....#.#..", ".#....#....", ".######....", "...........", "..........."};
    static final String[] BACK = {
            "...........", "....#......", "...##......", "..###......", ".##########", "###########",
            ".##########", "..###......", "...##......", "....#......", "..........."};
    static final String[] SWAP = {
            "......#....", ".......#...", "#########..", ".......#...", "......#....", "....#......",
            "...#.......", "..#########", "...#.......", "....#......", "..........."};
    static final String[] CROSS = {
            "...........", ".##.....##.", ".###...###.", "..###.###..", "...#####...", "....###....",
            "...#####...", "..###.###..", ".###...###.", ".##.....##.", "..........."};
    static final String[] CHECK = {
            "...........", ".........##", "........###", ".......###.", "......###..", "##...###...",
            "###.###....", ".#####.....", "..###......", "...#.......", "..........."};
}
