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

    /** The banner's outline: pointed at the top and the bottom. true where the banner is. */
    private static boolean inside(int x, int y, int inset) {
        int w = BANNER_W - inset * 2, mid = BANNER_W / 2;
        int top = 14, bottom = BANNER_H - 14;
        int dx = Math.abs(x - mid);
        if (y < inset || y >= BANNER_H - inset) return false;
        int half = w / 2;
        if (y < top) return dx <= half * (y - inset) / (double) (top - inset);
        if (y > bottom) return dx <= half * (BANNER_H - inset - 1 - y) / (double) (BANNER_H - inset - 1 - bottom);
        return dx <= half;
    }

    /** A floor's colours: the hall's dark, its light, the doorway's glow. */
    private static int[] palette(int floor) {
        if (floor <= 2) return new int[]{0x243F3A, 0x8EC0B8, 0x7FF0FF};
        if (floor <= 5) return new int[]{0x5A3420, 0xD9A878, 0xFFE2AE};
        if (floor <= 8) return new int[]{0x22225E, 0x7E86D8, 0xC8B8FF};
        if (floor <= 10) return new int[]{0x1C2E66, 0x8FA8E0, 0xBFD8FF};
        if (floor == 11) return new int[]{0x3A2266, 0xB396E6, 0xE0C8FF};
        return new int[]{0x101838, 0x5868B8, 0xA8C0FF};
    }

    /** state: 0 open, 1 locked, 2 reward taken; selected draws a bright white rim. */
    public static ResourceLocation banner(int floor, int state, boolean selected) {
        String key = "b" + floor + "_" + state + (selected ? "s" : "");
        ResourceLocation done = CACHE.get(key);
        if (done != null) return done;
        NativeImage img = new NativeImage(BANNER_W, BANNER_H, true);
        boolean locked = state == 1;
        int[] p = palette(floor);
        int mid = BANNER_W / 2;
        Random r = new Random(floor * 131L);
        for (int y = 0; y < BANNER_H; y++)
            for (int x = 0; x < BANNER_W; x++) {
                if (!inside(x, y, 0)) continue;
                int col;
                if (!inside(x, y, 3)) col = selected ? 0xFFFFFF : 0xC7DCEA;                  // the glass rim
                else if (!inside(x, y, 4)) col = 0x2C4458;                                   // a dark line inside it
                else if (y < 98) {
                    // the hall: dark above, its light below, pillars at both sides
                    double t = (y - 4) / 94.0;
                    col = mix(p[0], p[1], t * t);
                    int dx = Math.abs(x - mid);
                    if (dx >= 13 && dx <= 17) col = mix(col, p[1], 0.45 - (dx == 15 ? 0 : 0.15));
                    if (floor == 12 && y < 70 && r.nextInt(30) == 0) col = 0xE8F0FF;          // the stars over the last floor
                } else {
                    col = mix(0xDCE7EF, 0xAFC3D4, (y - 98) / 40.0);                         // the frosted foot
                }
                img.setPixelRGBA(x, y, c(col));
            }
        // the doorway at the top of the stair, lit from inside, with a figure's shadow in it
        for (int y = 42; y < 70; y++)
            for (int x = mid - 6; x <= mid + 6; x++) {
                boolean arch = y < 48 && Math.abs(x - mid) > (y - 42) + 1;
                if (arch) continue;
                img.setPixelRGBA(x, y, c(mix(p[2], 0xFFFFFF, (y - 42) / 60.0)));
            }
        for (int y = 50; y < 69; y++)
            for (int x = mid - 2; x <= mid + 1; x++)
                if (y > 52 || Math.abs(x - mid + 0.5) < 1.5) img.setPixelRGBA(x, y, c(mix(p[0], 0x000000, 0.3)));
        // the stair, widening towards the viewer
        for (int s = 0; s < 9; s++) {
            int y = 70 + s * 3, half = 7 + s * 1;
            for (int dy = 0; dy < 3; dy++)
                for (int x = mid - half; x <= mid + half; x++)
                    if (inside(x, y + dy, 5)) img.setPixelRGBA(x, y + dy, c(dy == 0 ? mix(p[1], 0xFFFFFF, 0.45) : mix(p[1], p[0], 0.35)));
        }
        // the light's glow on the hall's floor
        for (int y = 92; y < 98; y++)
            for (int x = mid - 9; x <= mid + 9; x++)
                if (inside(x, y, 5)) blend(img, x, y, p[2], 0.35 * (1 - Math.abs(x - mid) / 10.0));
        // the number's disc: white with a grey ring
        for (int y = DISC_Y - 10; y <= DISC_Y + 10; y++)
            for (int x = mid - 10; x <= mid + 10; x++) {
                double d = Math.hypot(x - mid, y - DISC_Y);
                if (d <= 10) img.setPixelRGBA(x, y, c(d > 8.6 ? 0x9FB2C2 : (d > 7.6 ? 0xFFFFFF : 0xF2F5F8)));
            }
        // the diamond with the chest (or a lock)
        for (int y = DIAMOND_Y - 10; y <= DIAMOND_Y + 10; y++)
            for (int x = mid - 10; x <= mid + 10; x++) {
                int md = Math.abs(x - mid) + Math.abs(y - DIAMOND_Y);
                if (md <= 10) img.setPixelRGBA(x, y, c(md >= 9 ? 0x8C9EAE : 0x3A4A5C));
            }
        if (locked) {
            for (int y = DIAMOND_Y - 5; y <= DIAMOND_Y - 2; y++) {
                img.setPixelRGBA(mid - 3, y, c(0xF2F5F8));
                img.setPixelRGBA(mid + 3, y, c(0xF2F5F8));
            }
            for (int x = mid - 3; x <= mid + 3; x++) img.setPixelRGBA(x, DIAMOND_Y - 5, c(0xF2F5F8));
            for (int y = DIAMOND_Y - 1; y <= DIAMOND_Y + 4; y++)
                for (int x = mid - 4; x <= mid + 4; x++)
                    img.setPixelRGBA(x, y, c((x == mid && y >= DIAMOND_Y + 1 && y <= DIAMOND_Y + 2) ? 0x3A4A5C : 0xF2F5F8));
        } else {
            for (int y = DIAMOND_Y - 4; y <= DIAMOND_Y + 4; y++)
                for (int x = mid - 6; x <= mid + 6; x++) {
                    int col = (y == DIAMOND_Y - 1) ? 0x6B4414 : (Math.abs(x - mid) <= 1 && y >= DIAMOND_Y - 2 && y <= DIAMOND_Y + 1 ? 0xFFF3C2 : 0xE8B848);
                    if (y == DIAMOND_Y - 4 || x == mid - 6 || x == mid + 6) col = 0x8A5A1A;
                    img.setPixelRGBA(x, y, c(col));
                }
            if (state == 2) {
                // a green tick under the diamond
                int ty = DIAMOND_Y + 14;
                for (int y = ty - 4; y <= ty + 4; y++)
                    for (int x = mid - 4; x <= mid + 4; x++)
                        if (Math.hypot(x - mid, y - ty) <= 4.4) img.setPixelRGBA(x, y, c(0x1E3A2E));
                int[][] tick = {{-2, 0}, {-1, 1}, {0, 2}, {1, 1}, {2, 0}, {3, -1}};
                for (int[] t : tick) img.setPixelRGBA(mid + t[0], ty + t[1] - 1, c(0x6EE07A));
            }
        }
        if (locked) {
            // the whole banner dimmed and cooled
            for (int y = 0; y < BANNER_H; y++)
                for (int x = 0; x < BANNER_W; x++)
                    if (inside(x, y, 4) && !(Math.abs(x - mid) + Math.abs(y - DIAMOND_Y) <= 10) && Math.hypot(x - mid, y - DISC_Y) > 10)
                        blend(img, x, y, 0x40505E, 0.45);
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
