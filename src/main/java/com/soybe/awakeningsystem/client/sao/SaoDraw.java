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

    /** What the mouse was over this frame and the last one (0 = nothing), for the hover tick. */
    static int hoverKey, lastHoverKey;

    static void frame(int mouseX, int mouseY, float t) {
        lastHoverKey = hoverKey;
        hoverKey = 0;
        mx = mouseX;
        my = mouseY;
        time = t;
    }

    /** Moves the hover point only (for a panel turned in 3D, the mouse traced onto that panel). */
    static void mouse(int mouseX, int mouseY) {
        mx = mouseX;
        my = mouseY;
    }

    /** Marks the thing under the mouse (any number that tells it apart), so moving onto it ticks once. */
    static void hovering(int key) {
        hoverKey = key == 0 ? 1 : key;
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
        // the game draws text whose alpha is under 4 fully opaque, so a fading text never goes below 4
        // (all but invisible) and a fading-in window doesn't flash its words at the start
        if ((argb >>> 24) >= 4 && a > 0.0F) al = Math.max(4, al);
        return (al << 24) | (argb & 0xFFFFFF);
    }

    // ── smooth circles ──────────────────────────────────────────────────────
    // Circles are real polygons in screen pixels (not GUI pixels), with a one-pixel soft edge, so they stay
    // round and crisp at any GUI scale.

    /** One real screen pixel in GUI units. */
    private static float px() {
        double s = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
        return (float) (1.0D / Math.max(1.0D, s));
    }

    private static int segments(float r) {
        return Math.max(24, Math.min(180, (int) (r / px() * 0.9F)));
    }

    private static com.mojang.blaze3d.vertex.VertexConsumer buf(GuiGraphics g) {
        return g.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());
    }

    private static int clear(int argb) {
        return argb & 0x00FFFFFF;
    }

    /** A filled circle. */
    static void disc(GuiGraphics g, float cx, float cy, float r, int color) {
        radial(g, cx, cy, cx, cy, r, color, color);
    }

    /**
     * A filled circle whose colour runs from {@code inner} at the point (gx, gy) to {@code outer} at the rim;
     * putting (gx, gy) off centre gives the lit, rounded look of a glass bead.
     */
    static void radial(GuiGraphics g, float gx, float gy, float cx, float cy, float r, int inner, int outer) {
        if (r <= 0) return;
        var vc = buf(g);
        var m = g.pose().last().pose();
        int n = segments(r);
        float f = px();
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            vc.addVertex(m, gx, gy, 0).setColor(inner);
            vc.addVertex(m, cx + c1 * r, cy + s1 * r, 0).setColor(outer);
            vc.addVertex(m, cx + c0 * r, cy + s0 * r, 0).setColor(outer);
            vc.addVertex(m, gx, gy, 0).setColor(inner);
            // the soft edge: one screen pixel fading out
            band(vc, m, cx, cy, c0, s0, c1, s1, r, r + f, outer, clear(outer));
        }
    }

    static void ring(GuiGraphics g, float cx, float cy, float r, float thickness, int color) {
        if (r <= 0) return;
        var vc = buf(g);
        var m = g.pose().last().pose();
        int n = segments(r);
        float f = px();
        float in = Math.max(0.0F, r - thickness);
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            band(vc, m, cx, cy, c0, s0, c1, s1, in, r, color, color);
            band(vc, m, cx, cy, c0, s0, c1, s1, r, r + f, color, clear(color));
            if (in > f) band(vc, m, cx, cy, c0, s0, c1, s1, in - f, in, clear(color), color);
        }
    }

    /** One segment of an annulus between radii a (colour ca) and b (colour cb). */
    private static void band(com.mojang.blaze3d.vertex.VertexConsumer vc, org.joml.Matrix4f m, float cx, float cy,
                             float c0, float s0, float c1, float s1, float a, float b, int ca, int cb) {
        vc.addVertex(m, cx + c0 * a, cy + s0 * a, 0).setColor(ca);
        vc.addVertex(m, cx + c1 * a, cy + s1 * a, 0).setColor(ca);
        vc.addVertex(m, cx + c1 * b, cy + s1 * b, 0).setColor(cb);
        vc.addVertex(m, cx + c0 * b, cy + s0 * b, 0).setColor(cb);
    }

    /**
     * A slanted plate (a parallelogram leaning right by {@code skew}), with its colour running from
     * {@code top} to {@code bottom}: the shape of the bars in the series.
     */
    static void slant(GuiGraphics g, float x, float y, float w, float h, float skew, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        var vc = buf(g);
        var m = g.pose().last().pose();
        vc.addVertex(m, x + skew, y, 0).setColor(top);
        vc.addVertex(m, x, y + h, 0).setColor(bottom);
        vc.addVertex(m, x + w, y + h, 0).setColor(bottom);
        vc.addVertex(m, x + w + skew, y, 0).setColor(top);
    }

    /** Health colour: green, turning yellow below half and red below a fifth. */
    static int hpColor(float frac) {
        if (frac > 0.5F) return 0xFF7CD23A;
        if (frac > 0.2F) return 0xFFE8C21E;
        return 0xFFE0402E;
    }

    /** Mixes a colour toward white (k > 0) or black (k < 0), keeping its alpha. */
    static int shade(int argb, float k) {
        int a = argb >>> 24, r = (argb >> 16) & 255, gg = (argb >> 8) & 255, b = argb & 255;
        int t = k > 0 ? 255 : 0;
        float q = Math.abs(k);
        r = (int) (r + (t - r) * q);
        gg = (int) (gg + (t - gg) * q);
        b = (int) (b + (t - b) * q);
        return (a << 24) | (r << 16) | (gg << 8) | b;
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
        if (mx >= x && mx < x + w && my >= y && my < y + h) hovering(x * 7919 + y);
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
        float cy = y + h / 2.0F;
        ring(g, x + 13, cy, 8.5F, 1.0F, alpha(hot ? 0xFFFFE2A8 : 0xFFB8B8BE, a));
        shinyDisc(g, x + 13, cy, 7.2F, hot ? WHITE : 0xFF5E5E66, a);
        icon(g, icon, x + 13 - 5, Math.round(cy) - 5, alpha(hot ? ORANGE : WHITE, a));
        g.drawString(font, label, x + 27, y + (h - 8) / 2, alpha(hot ? WHITE : (over ? 0xFF2E2E34 : 0xFF4A4A52), a), false);
        if (hot) pointerRight(g, x + w - 6, Math.round(cy), 4, alpha(WHITE, a));
    }

    /** A disc with depth: soft shadow under it, the colour, a lighter cap and a small glint. */
    static void shinyDisc(GuiGraphics g, float cx, float cy, float r, int color, float a) {
        // a soft shadow, two layers so it fades out
        disc(g, cx + 0.5F, cy + 1.4F, r + 0.6F, alpha(0x22000000, a));
        disc(g, cx + 0.4F, cy + 1.0F, r, alpha(0x40000000, a));
        // the bead: lit from the upper left, darker toward the lower rim
        radial(g, cx - r * 0.3F, cy - r * 0.38F, cx, cy, r, alpha(shade(color, 0.38F), a), alpha(shade(color, -0.14F), a));
        // a thin rim and a bright line along the top edge
        ring(g, cx, cy, r, Math.max(px(), r * 0.07F), alpha(0x30000000, a));
        ring(g, cx, cy - r * 0.04F, r * 0.9F, Math.max(px(), r * 0.05F), alpha(0x22FFFFFF, a));
        // the glint
        radial(g, cx - r * 0.36F, cy - r * 0.46F, cx - r * 0.36F, cy - r * 0.46F, Math.max(0.9F, r * 0.2F), alpha(0xC0FFFFFF, a), alpha(0x00FFFFFF, a));
    }

    /**
     * A floating panel: shadow, white body fading to light grey, a darker title strip with a thin orange
     * line under it, faint horizontal grain and corner ticks.
     */
    static void panel(GuiGraphics g, int x, int y, int w, int h, int headerH, float a) {
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, alpha(0x55000000, a));
        g.fillGradient(x, y, x + w, y + h, alpha(0xF8FFFFFF, a), alpha(0xF0E4E4E8, a));
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
        if (hover) hovering(Math.round(cx) * 7919 + Math.round(cy) + 1);
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
