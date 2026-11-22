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

    public static final int BG_W = 320, BG_H = 180;
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

    // ---- the Corridor's hall

    public static ResourceLocation corridor() {
        if (corridor != null) return corridor;
        NativeImage img = new NativeImage(BG_W, BG_H, true);
        int cx = BG_W / 2, cy = 98;
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                // deep blue, lighter towards the middle where a teal glow sits behind the arch
                double d = Math.hypot(x - cx, (y - cy) * 1.1);
                int base = mix(0x0B1A33, 0x13304A, y / (double) BG_H);
                base = mix(base, 0x2F8C88, Math.max(0, 1 - d / 95) * 0.75);
                base = mix(base, 0x6FD0C2, Math.max(0, 1 - d / 34) * 0.55);
                img.setPixelRGBA(x, y, c(base));
            }
        int line = 0x4E8FA6;
        // the great arch: rings round the glow, broken here and there as interlaced bands are
        for (int k = 0; k < 6; k++) {
            double r = 40 + k * 13;
            for (int seg = 0; seg < 9; seg++) {
                double a0 = Math.PI + seg * Math.PI / 9 + 0.05, a1 = a0 + Math.PI / 9 - 0.12;
                arc(img, cx, cy, r, a0, a1, line, 0.55 - k * 0.06);
            }
            // the arch's sides come down to the floor
            for (int y = cy; y < BG_H; y++) {
                blend(img, (int) (cx - r), y, line, 0.45 - k * 0.05);
                blend(img, (int) (cx + r), y, line, 0.45 - k * 0.05);
            }
        }
        // the knotwork crowning the arch: loops interlaced above the centre
        for (int i = -3; i <= 3; i++) {
            arc(img, cx + i * 14, cy - 92, 12, 0, Math.PI * 2, line, 0.5);
            arc(img, cx + i * 14 + 7, cy - 80, 9, Math.PI, Math.PI * 2, line, 0.45);
        }
        for (int i = -1; i <= 1; i += 2) {
            arc(img, cx + i * 70, cy - 40, 26, 0, Math.PI * 2, line, 0.35);
            arc(img, cx + i * 70, cy - 40, 18, Math.PI * 0.2, Math.PI * 1.6, line, 0.35);
        }
        // the round window of light in the middle and the floor it shines on
        arc(img, cx, cy, 30, 0, Math.PI * 2, 0xBFF3EA, 0.5);
        arc(img, cx, cy, 34, 0, Math.PI * 2, 0x8FD8CC, 0.35);
        for (int y = 150; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++)
                blend(img, x, y, 0x0A1428, (y - 150) / 40.0);
        // dark tall frames at both sides, as if seen from inside the hall
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < 34; x++) {
                blend(img, x, y, 0x081325, 0.55 * (1 - x / 34.0));
                blend(img, BG_W - 1 - x, y, 0x081325, 0.55 * (1 - x / 34.0));
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
