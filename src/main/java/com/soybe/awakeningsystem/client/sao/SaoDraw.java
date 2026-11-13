package com.soybe.awakeningsystem.client.sao;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Drawing pieces shared by the full-dive menus: discs, rings, pointers, rows and the little pictograms. */
final class SaoDraw {
    private SaoDraw() {}

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

    /** One row of a list: white, or orange when chosen, with a round badge and its pictogram on the left. */
    static void row(GuiGraphics g, Font font, int x, int y, int w, int h, Component label, String[] icon, boolean hot, float a) {
        g.fill(x, y, x + w, y + h, alpha(hot ? ORANGE : PANEL, a));
        g.fill(x, y + h - 1, x + w, y + h, alpha(hot ? ORANGE_DARK : 0xFFCFCFCF, a));
        float cy = y + h / 2.0F;
        disc(g, x + 12, cy, 7.5F, alpha(hot ? WHITE : GREY, a));
        icon(g, icon, x + 12 - 5, Math.round(cy) - 5, alpha(hot ? ORANGE : WHITE, a));
        g.drawString(font, label, x + 25, y + (h - 8) / 2, alpha(hot ? WHITE : GREY, a), false);
    }

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
