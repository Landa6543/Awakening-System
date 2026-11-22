package com.soybe.awakeningsystem.abyss;

import com.mojang.blaze3d.platform.NativeImage;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The Spiral Abyss window's pictures, painted here in code and drawn scaled up, after the look of the game's own
 * Abyss: the Corridor's hall (a great arch of interlaced lines round a teal glow), the Spire's great eye, and the
 * floor banners: pointed at both ends, a pale glass rim, the floor's hall (pillars, a stair up to a lit doorway)
 * in its group's colours, and a frosted foot with the chest in a diamond. Our own drawing, done in code.
 */
public final class AbyssArt {
    private AbyssArt() {}

    public static final int BG_W = 480, BG_H = 270;
    public static final int BANNER_W = 50, BANNER_H = 150;
    /** Where the number's disc sits on a banner (its centre), and the diamond with the chest. */
    public static final int DISC_Y = 24, DIAMOND_Y = 112;

    private static ResourceLocation corridor, spire;
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    private static int c(int rgb) {
        return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int ca(int rgb, int a) {
        return (a << 24) | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int rgbOf(int abgr) {
        return ((abgr & 0xFF) << 16) | (abgr & 0xFF00) | ((abgr >> 16) & 0xFF);
    }

    static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private static void blend(NativeImage img, int x, int y, int rgb, double t) {
        if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) return;
        img.setPixelRGBA(x, y, c(mix(rgbOf(img.getPixelRGBA(x, y)), rgb, t)));
    }

    private static ResourceLocation upload(String name, NativeImage img) {
        return Minecraft.getInstance().getTextureManager().register(AwakeningSystemMod.MOD_ID + "_abyss_" + name, new DynamicTexture(img));
    }

    /** A thin circle (or part of one, from a0 to a1 in radians) of light over the picture. */
    private static void arc(NativeImage img, double cx, double cy, double r, double a0, double a1, int rgb, double t) {
        int n = (int) Math.max(40, r * 7);
        for (int i = 0; i <= n; i++) {
            double a = a0 + (a1 - a0) * i / n;
            blend(img, (int) Math.round(cx + Math.cos(a) * r), (int) Math.round(cy + Math.sin(a) * r), rgb, t);
        }
    }

    // ---- smooth strokes for the halls (each pixel takes as much of the colour as the line covers it)

    private static double cover(double dist, double w) {
        return Math.max(0, Math.min(1, w / 2 + 0.5 - dist));
    }

    /** Part of an ellipse (angles in radians, y downwards: PI to 2 PI is its upper half), w pixels thick. */
    private static void strokeArc(NativeImage img, double cx, double cy, double rx, double ry, double a0, double a1, double w, int rgb, double alpha) {
        int x0 = (int) Math.max(0, cx - rx - w), x1 = (int) Math.min(img.getWidth() - 1, cx + rx + w);
        int y0 = (int) Math.max(0, cy - ry - w), y1 = (int) Math.min(img.getHeight() - 1, cy + ry + w);
        for (int y = y0; y <= y1; y++)
            for (int x = x0; x <= x1; x++) {
                double dx = x + 0.5 - cx, dy = (y + 0.5 - cy) * rx / ry;
                double ang = Math.atan2(dy, dx);
                if (ang < 0) ang += Math.PI * 2;
                if (ang < a0 || ang > a1) continue;
                double c = cover(Math.abs(Math.hypot(dx, dy) - rx) * Math.min(1, ry / rx + 0.35), w);
                if (c > 0) blend(img, x, y, rgb, c * alpha);
            }
    }

    private static void strokeLine(NativeImage img, double xa, double ya, double xb, double yb, double w, int rgb, double alpha) {
        int x0 = (int) Math.max(0, Math.min(xa, xb) - w), x1 = (int) Math.min(img.getWidth() - 1, Math.max(xa, xb) + w);
        int y0 = (int) Math.max(0, Math.min(ya, yb) - w), y1 = (int) Math.min(img.getHeight() - 1, Math.max(ya, yb) + w);
        double vx = xb - xa, vy = yb - ya, len2 = vx * vx + vy * vy;
        for (int y = y0; y <= y1; y++)
            for (int x = x0; x <= x1; x++) {
                double px = x + 0.5 - xa, py = y + 0.5 - ya;
                double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, (px * vx + py * vy) / len2));
                double c = cover(Math.hypot(px - vx * t, py - vy * t), w);
                if (c > 0) blend(img, x, y, rgb, c * alpha);
            }
    }

    private static void glow(NativeImage img, double cx, double cy, double r, int rgb, double alpha) {
        for (int y = (int) Math.max(0, cy - r); y < Math.min(img.getHeight(), cy + r); y++)
            for (int x = (int) Math.max(0, cx - r); x < Math.min(img.getWidth(), cx + r); x++) {
                double d = Math.hypot(x + 0.5 - cx, y + 0.5 - cy) / r;
                if (d < 1) blend(img, x, y, rgb, alpha * (1 - d) * (1 - d));
            }
    }

    /** A thick band of the arch: a dark body, pale edges, and a braid of two lines crossing along it. */
    private static void band(NativeImage img, double cx, double cy, double rx, double ry, double floorY, double w, boolean braid) {
        int body = 0x123248, edge = 0x5E9FB4, cord = 0x8CCAD4;
        strokeArc(img, cx, cy, rx, ry, Math.PI, Math.PI * 2, w, body, 0.95);
        strokeLine(img, cx - rx, cy, cx - rx, floorY, w, body, 0.95);
        strokeLine(img, cx + rx, cy, cx + rx, floorY, w, body, 0.95);
        for (int s = -1; s <= 1; s += 2) {
            double o = s * w / 2;
            strokeArc(img, cx, cy, rx + o, ry + o, Math.PI, Math.PI * 2, 1.3, edge, 0.85);
            strokeLine(img, cx - rx - o, cy, cx - rx - o, floorY, 1.3, edge, 0.85);
            strokeLine(img, cx + rx + o, cy, cx + rx + o, floorY, 1.3, edge, 0.85);
        }
        if (!braid) return;
        // the braid: two cords weaving round each other along the band's middle
        int n = (int) (Math.PI * (rx + ry) * 1.2);
        for (int k = 0; k <= n; k++) {
            double a = Math.PI + Math.PI * k / n;
            for (int c = 0; c < 2; c++) {
                double off = Math.sin(k * 0.22 + c * Math.PI) * (w * 0.32);
                double px = cx + Math.cos(a) * (rx + off), py = cy + Math.sin(a) * (ry + off);
                blend(img, (int) px, (int) py, cord, 0.75);
            }
        }
        for (double y = cy; y < floorY; y += 0.5)
            for (int s = -1; s <= 1; s += 2)
                for (int c = 0; c < 2; c++) {
                    double off = Math.sin(y * 0.4 + c * Math.PI) * (w * 0.32);
                    blend(img, (int) (cx + s * (rx + off)), (int) y, cord, 0.6);
                }
    }

    // ---- the Corridor's hall

    public static ResourceLocation corridor() {
        if (corridor != null) return corridor;
        NativeImage img = new NativeImage(BG_W, BG_H, true);
        double cx = BG_W / 2.0, cy = BG_H * 0.61, floorY = BG_H - 22;
        // deep blue round a teal heart
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                double d = Math.hypot((x - cx) / (BG_W * 0.62), (y - cy) / (BG_H * 0.75));
                int col = mix(0x235F6C, 0x10283E, Math.min(1, d * 1.15));
                col = mix(col, 0x081324, Math.max(0, d - 0.75) * 1.6);
                img.setPixelRGBA(x, y, c(col));
            }
        // the side arches, half out of sight, darker
        for (int s = -1; s <= 1; s += 2) {
            band(img, cx + s * BG_W * 0.47, cy + 10, 62, 92, floorY, 7, false);
            strokeArc(img, cx + s * BG_W * 0.47, cy + 10, 48, 76, Math.PI, Math.PI * 2, 2, 0x2E6378, 0.6);
        }
        // the mullions of the great window, behind the light
        for (int k = -3; k <= 3; k++) {
            if (k == 0) continue;
            double x = cx + k * 26;
            strokeLine(img, x, cy - Math.sqrt(Math.max(0, 1 - Math.pow(k * 26 / 128.0, 2))) * 112, x, floorY, 1.4, 0x3A7E92, 0.45);
        }
        // the round window of light at the heart of the hall
        glow(img, cx, cy - 6, 150, 0x5FD2C6, 0.45);
        glow(img, cx, cy - 6, 64, 0xCFFAF2, 0.55);
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                double d = Math.hypot(x + 0.5 - cx, y + 0.5 - (cy - 6));
                if (d < 54) blend(img, x, y, 0x8EE6DA, 0.35 * (1 - d / 54));
            }
        strokeArc(img, cx, cy - 6, 56, 56, 0, Math.PI * 2, 3.2, 0xD9FBF4, 0.85);
        strokeArc(img, cx, cy - 6, 64, 64, 0, Math.PI * 2, 1.6, 0x8ACFC8, 0.7);
        strokeArc(img, cx, cy - 6, 47, 47, 0, Math.PI * 2, 1.0, 0xEFFFFC, 0.45);
        // the great arch: two bands, the outer one braided
        band(img, cx, cy, 170, 138, floorY, 13, true);
        band(img, cx, cy, 140, 113, floorY, 7, false);
        // knotwork crowning the arch: three interlaced rings and a pointed leaf between them
        double topY = cy - 138;
        for (int k = -1; k <= 1; k++) {
            strokeArc(img, cx + k * 22, topY + (k == 0 ? -2 : 8), 13, 13, 0, Math.PI * 2, 2.4, 0x123248, 0.95);
            strokeArc(img, cx + k * 22, topY + (k == 0 ? -2 : 8), 13, 13, 0, Math.PI * 2, 1.1, 0x8CCAD4, 0.8);
        }
        strokeArc(img, cx - 9, topY + 24, 14, 22, Math.PI * 1.2, Math.PI * 1.95, 1.6, 0x8CCAD4, 0.7);
        strokeArc(img, cx + 9, topY + 24, 14, 22, Math.PI * 1.05, Math.PI * 1.8, 1.6, 0x8CCAD4, 0.7);
        // tracery inside the arch, above the window: arcs joining the window to the bands
        for (int k = -1; k <= 1; k += 2) {
            strokeArc(img, cx + k * 70, cy - 70, 48, 48, Math.PI * 1.05, Math.PI * 1.95, 1.4, 0x4E92A6, 0.6);
            strokeArc(img, cx + k * 96, cy - 20, 30, 40, Math.PI * 0.9, Math.PI * 2.0, 1.2, 0x4E92A6, 0.45);
        }
        // the floor, with the window's light lying on it
        for (int y = (int) floorY; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                blend(img, x, y, 0x0A1828, 0.55 + (y - floorY) / 40.0);
                double e = Math.hypot((x - cx) / 120.0, (y - floorY - 6) / 7.0);
                if (e < 1) blend(img, x, y, 0x6FD8CC, 0.3 * (1 - e));
            }
        strokeLine(img, 0, floorY, BG_W, floorY, 1.2, 0x4E92A6, 0.5);
        // the edges of the picture in shade
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                double vx = Math.abs(x - cx) / cx, vy = Math.abs(y - BG_H / 2.0) / (BG_H / 2.0);
                double v = Math.max(0, Math.max(vx, vy * 0.9) - 0.72) / 0.28;
                if (v > 0) blend(img, x, y, 0x050B16, v * 0.7);
            }
        corridor = upload("corridor", img);
        return corridor;
    }

    // ---- the Spire's great eye

    public static ResourceLocation spire() {
        if (spire != null) return spire;
        NativeImage img = new NativeImage(BG_W, BG_H, true);
        int cx = BG_W / 2, cy = BG_H / 2;
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                double d = Math.hypot(x - cx, (y - cy) * 1.15);
                int base = mix(0x1A1233, 0x2A1E4E, Math.max(0, 1 - d / 160));
                if (d < 26) base = mix(0x05040C, 0x120C24, d / 26);         // the dark pupil
                img.setPixelRGBA(x, y, c(base));
            }
        // the eye's rings, with ticks round them like a dial
        for (int k = 0; k < 5; k++) {
            double r = 34 + k * 20;
            arc(img, cx, cy, r, 0, Math.PI * 2, 0x7A64B8, 0.45 - k * 0.06);
            int ticks = 48 + k * 12;
            for (int t = 0; t < ticks; t++) {
                double a = t * Math.PI * 2 / ticks;
                int len = (t % 4 == 0) ? 7 : 3;
                for (int s = 0; s < len; s++)
                    blend(img, (int) (cx + Math.cos(a) * (r + 2 + s)), (int) (cy + Math.sin(a) * (r + 2 + s)), 0x9A86D8, 0.3);
            }
        }
        // light streaks spreading out of the pupil
        Random r = new Random(5);
        for (int i = 0; i < 60; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            double from = 30 + r.nextInt(30), to = from + 20 + r.nextInt(70);
            for (double s = from; s < to; s += 0.7)
                blend(img, (int) (cx + Math.cos(a) * s), (int) (cy + Math.sin(a) * s * 0.85), 0xB8A8F0, 0.18 * (1 - (s - from) / (to - from)));
        }
        spire = upload("spire", img);
        return spire;
    }

    // ---- banners

    /** Banners are painted at this many pixels per design unit (the design is 50 x 150). */
    public static final int K = 2;

    /** The banner's outline in design units: pointed at the top and the bottom. true where the banner is. */
    private static boolean inside(double x, double y, double inset) {
        double mid = BANNER_W / 2.0, half = BANNER_W / 2.0 - inset;
        double top = 14, bottom = BANNER_H - 14;
        double dx = Math.abs(x - mid);
        if (y < inset || y >= BANNER_H - inset) return false;
        if (y < top) return dx <= half * (y - inset) / (top - inset);
        if (y > bottom) return dx <= half * (BANNER_H - inset - y) / (BANNER_H - inset - bottom);
        return dx <= half;
    }

    /** A floor's colours: the hall's dark, its light, the doorway's glow. */
    private static int[] palette(int floor) {
        if (floor <= 2) return new int[]{0x1F3A36, 0x8EC0B8, 0x8FF4FF};
        if (floor <= 5) return new int[]{0x4E2C1A, 0xD9A878, 0xFFE6B4};
        if (floor <= 8) return new int[]{0x1E1E58, 0x7E86D8, 0xCDBEFF};
        if (floor <= 10) return new int[]{0x1A2A60, 0x8FA8E0, 0xC4DCFF};
        if (floor == 11) return new int[]{0x34205E, 0xB396E6, 0xE4CEFF};
        return new int[]{0x0E1634, 0x5868B8, 0xA8C0FF};
    }

    private static void star(NativeImage img, int cx, int cy, int r, int rgb) {
        for (int i = -r; i <= r; i++) {
            int a = 255 - Math.abs(i) * 200 / Math.max(1, r);
            if (cx + i >= 0 && cx + i < img.getWidth()) img.setPixelRGBA(cx + i, cy, ca(rgb, a));
            if (cy + i >= 0 && cy + i < img.getHeight()) img.setPixelRGBA(cx, cy + i, ca(rgb, a));
        }
    }

    /** state: 0 open, 1 locked, 2 reward taken; selected draws a bright white rim. */
    public static ResourceLocation banner(int floor, int state, boolean selected) {
        String key = "b" + floor + "_" + state + (selected ? "s" : "");
        ResourceLocation done = CACHE.get(key);
        if (done != null) return done;
        int W = BANNER_W * K, H = BANNER_H * K;
        NativeImage img = new NativeImage(W, H, true);
        boolean locked = state == 1;
        int[] p = palette(floor);
        double mid = BANNER_W / 2.0;
        Random r = new Random(floor * 131L);
        for (int py = 0; py < H; py++)
            for (int px = 0; px < W; px++) {
                double x = (px + 0.5) / K, y = (py + 0.5) / K;
                if (!inside(x, y, 0)) continue;
                int col;
                double dx = Math.abs(x - mid);
                if (!inside(x, y, 1.2)) col = selected ? 0xFFFFFF : 0xBFD3E2;                    // the glass rim, outer
                else if (!inside(x, y, 2.6)) col = selected ? 0xEAF6FF : mix(0x8FA9BE, 0xD8E8F2, y / BANNER_H);   // its inner band
                else if (!inside(x, y, 3.3)) col = 0x22384C;                                     // a dark line inside it
                else if (y < 98) {
                    double t = (y - 4) / 94.0;
                    col = mix(p[0], p[1], t * t * 0.9);
                    // the pointed arch's lattice above the doorway
                    if (y < 40) {
                        double archR = Math.hypot(dx, (y - 40) * 0.7);
                        if (Math.abs(archR - 14) < 0.5 || Math.abs(archR - 18) < 0.5) col = mix(col, p[1], 0.45);
                        if ((int) (x * 2) % 6 == 0 && archR > 14 && archR < 18) col = mix(col, p[1], 0.3);
                    }
                    // tall windows on the back wall, with arched tops
                    for (double wx : new double[]{mid - 8.5, mid + 8.5}) {
                        double wdx = Math.abs(x - wx);
                        if (wdx < 1.6 && y > 34 && y < 66 && (y > 37 || wdx < 1.6 - (37 - y))) col = mix(col, p[2], 0.28 + 0.1 * Math.sin(y));
                    }
                    // the pillars at both sides, with capitals and fluting
                    if (dx >= 15.5 && dx <= 20) {
                        col = mix(p[0], p[1], 0.35 + t * 0.4);
                        if (Math.abs(dx - 17.7) < 0.35) col = mix(col, 0x000000, 0.25);
                        if (dx > 19.4) col = mix(col, 0xFFFFFF, 0.12);
                        if (Math.abs(y - 36) < 1.2 || Math.abs(y - 90) < 1.2) col = mix(p[1], 0xFFFFFF, 0.35);
                    }
                    // the floor: tiles in perspective towards the door, with the door's light on them
                    if (y > 82) {
                        double depth = (y - 82) / 16.0;
                        double lane = (x - mid) / (0.25 + depth);
                        if (Math.abs(lane - Math.round(lane / 4) * 4) < 0.18 || Math.abs(Math.sin(depth * 9)) < 0.08) col = mix(col, 0x000000, 0.18);
                        col = mix(col, p[2], Math.max(0, 0.45 - dx / 22) * (1 - depth * 0.4));
                    }
                    if (floor == 12 && y < 72 && dx < 15 && r.nextInt(25) == 0) col = 0xE8F0FF;    // stars over the last floor
                } else {
                    // the frosted foot, with a soft sparkle
                    col = mix(0xE2ECF3, 0xA9BDCE, (y - 98) / 42.0);
                    if (r.nextInt(60) == 0) col = mix(col, 0xFFFFFF, 0.7);
                    if (Math.abs(y - 99) < 0.6) col = mix(p[1], 0xFFFFFF, 0.3);
                }
                img.setPixelRGBA(px, py, c(col));
            }
        // the doorway, lit from inside, with a statue's dark shape in its light
        for (int py = 42 * K; py < 72 * K; py++)
            for (int px = (int) ((mid - 6.5) * K); px <= (int) ((mid + 6.5) * K); px++) {
                double x = (px + 0.5) / K, y = (py + 0.5) / K, dx = Math.abs(x - mid);
                if (y < 49 && dx > 6.5 - (49 - y) * 0.9) continue;
                int col = mix(p[2], 0xFFFFFF, Math.max(0, 0.5 - dx / 13) + (y - 42) / 80.0);
                boolean statue = (dx < 1.3 && y > 50 && y < 70) || (dx < 2.4 && y > 54 && y < 70) || (Math.hypot(dx, y - 52) < 1.6);
                if (statue) col = mix(p[0], 0x000000, 0.35);
                img.setPixelRGBA(px, py, c(col));
            }
        // the stair, widening towards the viewer, its edges catching the light
        for (int s = 0; s < 10; s++) {
            double y0 = 71 + s * 2.6, half = 7 + s * 0.9;
            for (int py = (int) (y0 * K); py < (int) ((y0 + 2.6) * K); py++)
                for (int px = (int) ((mid - half) * K); px <= (int) ((mid + half) * K); px++) {
                    double x = (px + 0.5) / K, y = (py + 0.5) / K;
                    if (!inside(x, y, 3.5) || y > 97) continue;
                    boolean edge = y - y0 < 0.6;
                    int col = edge ? mix(p[1], 0xFFFFFF, 0.55) : mix(p[1], p[0], 0.3 + (y - y0) / 8);
                    col = mix(col, p[2], Math.max(0, 0.3 - Math.abs(x - mid) / 30));
                    img.setPixelRGBA(px, py, c(col));
                }
        }
        // the number's disc: white with a grey ring and a fine inner line
        for (int py = (DISC_Y - 11) * K; py <= (DISC_Y + 11) * K; py++)
            for (int px = (int) ((mid - 11) * K); px <= (int) ((mid + 11) * K); px++) {
                double d = Math.hypot((px + 0.5) / K - mid, (py + 0.5) / K - DISC_Y);
                if (d > 10.5) continue;
                int col = d > 9.4 ? 0x8C9FB0 : (d > 8.6 ? 0xFFFFFF : (Math.abs(d - 7.4) < 0.25 ? 0xC9D4DE : 0xF4F7FA));
                img.setPixelRGBA(px, py, c(col));
            }
        // the diamond holding the chest (or the lock), bevelled
        for (int py = (DIAMOND_Y - 11) * K; py <= (DIAMOND_Y + 11) * K; py++)
            for (int px = (int) ((mid - 11) * K); px <= (int) ((mid + 11) * K); px++) {
                double x = (px + 0.5) / K, y = (py + 0.5) / K;
                double md = Math.abs(x - mid) + Math.abs(y - DIAMOND_Y);
                if (md > 10.5) continue;
                int col = md > 9.6 ? 0xE6EEF4 : (md > 8.6 ? 0x7D90A2 : mix(0x46586C, 0x2E3C4E, (y - DIAMOND_Y + 9) / 18));
                img.setPixelRGBA(px, py, c(col));
            }
        int cx = (int) (mid * K), cy = DIAMOND_Y * K;
        if (locked) {
            for (int py = cy - 11; py <= cy - 3; py++)
                for (int px = cx - 7; px <= cx + 7; px++) {
                    double d = Math.hypot(px - cx, (py - (cy - 3)) * 1.0);
                    if (d >= 4.5 && d <= 7 && py <= cy - 3) img.setPixelRGBA(px, py, c(0xF2F5F8));
                }
            for (int py = cy - 3; py <= cy + 8; py++)
                for (int px = cx - 8; px <= cx + 8; px++)
                    img.setPixelRGBA(px, py, c((Math.abs(px - cx) <= 1 && py >= cy && py <= cy + 4) ? 0x3A4A5C : (py == cy - 3 ? 0xFFFFFF : 0xE4EAF0)));
        } else {
            // a golden chest: lid, body, bands and a lock plate
            for (int py = cy - 8; py <= cy + 8; py++)
                for (int px = cx - 12; px <= cx + 12; px++) {
                    int col;
                    boolean lid = py < cy - 1;
                    boolean rim = Math.abs(px - cx) == 12 || py == cy - 8 || py == cy + 8 || py == cy - 1;
                    boolean band = Math.abs(px - (cx - 7)) <= 1 || Math.abs(px - (cx + 7)) <= 1;
                    col = lid ? mix(0xF2C860, 0xC88A2A, (py - cy + 8) / 7.0) : mix(0xD49A34, 0x9A6418, (py - cy) / 8.0);
                    if (band) col = 0xFFE9A0;
                    if (rim) col = 0x6B4414;
                    if (Math.abs(px - cx) <= 2 && py >= cy - 3 && py <= cy + 3) col = (Math.abs(px - cx) <= 0 && py >= cy) ? 0x3A2408 : 0xFFF5D0;
                    img.setPixelRGBA(px, py, c(col));
                }
            if (state == 2) {
                int ty = cy + 26;
                for (int py = ty - 7; py <= ty + 7; py++)
                    for (int px = cx - 7; px <= cx + 7; px++) {
                        double d = Math.hypot(px - cx, py - ty);
                        if (d <= 7.2) img.setPixelRGBA(px, py, c(d > 6.2 ? 0x9AD8A4 : 0x1E3A2E));
                    }
                int[][] tick = {{-4, 0}, {-3, 1}, {-2, 2}, {-1, 3}, {0, 2}, {1, 1}, {2, 0}, {3, -1}, {4, -2}};
                for (int[] tk : tick) {
                    img.setPixelRGBA(cx + tk[0], ty + tk[1], c(0x6EE07A));
                    img.setPixelRGBA(cx + tk[0], ty + tk[1] - 1, c(0x6EE07A));
                }
            }
        }
        // little stars at the banner's top and bottom points
        star(img, (int) (mid * K), 6 * K, 4, 0xFFFFFF);
        star(img, (int) (mid * K), H - 6 * K, 4, 0xFFFFFF);
        if (locked) {
            for (int py = 0; py < H; py++)
                for (int px = 0; px < W; px++) {
                    double x = (px + 0.5) / K, y = (py + 0.5) / K;
                    if (inside(x, y, 3.3) && Math.abs(x - mid) + Math.abs(y - DIAMOND_Y) > 10.5 && Math.hypot(x - mid, y - DISC_Y) > 10.5)
                        blend(img, px, py, 0x3A4856, 0.5);
                }
        }
        ResourceLocation id = upload(key, img);
        CACHE.put(key, id);
        return id;
    }

    /** The way into the Spire at the end of the Corridor: a banner-shaped rift of purple light. */
    public static ResourceLocation rift(boolean open) {
        String key = "rift" + (open ? "o" : "c");
        ResourceLocation done = CACHE.get(key);
        if (done != null) return done;
        NativeImage img = new NativeImage(BANNER_W, BANNER_H, true);
        int cx = BANNER_W / 2, cy = BANNER_H / 2;
        Random r = new Random(9);
        for (int y = 0; y < BANNER_H; y++)
            for (int x = 0; x < BANNER_W; x++) {
                if (!inside(x, y, 0)) continue;
                int col;
                if (!inside(x, y, 3)) col = open ? 0xB48CFF : 0x7A6A98;
                else {
                    double a = Math.atan2(y - cy, (x - cx) * 2.5), d = Math.hypot((x - cx) * 2.5, y - cy);
                    double streak = Math.pow(Math.abs(Math.sin(a * 9 + d * 0.06)), 6);
                    col = mix(0x14062E, 0x7A2CE0, Math.min(1, streak * 0.8 + (1 - d / 90) * 0.4));
                    if (d < 10) col = mix(0x000000, col, d / 10);
                    if (r.nextInt(70) == 0) col = 0xE8D8FF;
                    if (!open) col = mix(col, 0x2A2438, 0.55);
                }
                img.setPixelRGBA(x, y, c(col));
            }
        ResourceLocation id = upload(key, img);
        CACHE.put(key, id);
        return id;
    }
}
