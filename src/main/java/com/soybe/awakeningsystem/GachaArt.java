package com.soybe.awakeningsystem;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

/**
 * The wish window's night sky, painted once in code: deep blue going to violet, a nebula and the Milky Way
 * across it, hundreds of stars (the brightest with glints), a few constellations drawn in faint gold, dark
 * peaks, and a sea of clouds lit from above at the bottom. The window adds the twinkling and shooting stars.
 */
public final class GachaArt {
    private GachaArt() {}

    public static final int W = 480, H = 270;
    private static ResourceLocation sky;

    private static int c(int rgb) {
        return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int rgbOf(int abgr) {
        return ((abgr & 0xFF) << 16) | (abgr & 0xFF00) | ((abgr >> 16) & 0xFF);
    }

    private static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static void blend(NativeImage img, int x, int y, int rgb, double t) {
        if (x < 0 || y < 0 || x >= W || y >= H || t <= 0) return;
        img.setPixelRGBA(x, y, c(mix(rgbOf(img.getPixelRGBA(x, y)), rgb, t)));
    }

    // smooth value noise, a few octaves of it
    private static double hash(int x, int y) {
        long h = x * 374761393L + y * 668265263L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0;
    }

    private static double noise(double x, double y) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        double fx = x - ix, fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        double a = hash(ix, iy), b = hash(ix + 1, iy), cc = hash(ix, iy + 1), d = hash(ix + 1, iy + 1);
        return a + (b - a) * fx + (cc - a) * fy + (a - b - cc + d) * fx * fy;
    }

    private static double fbm(double x, double y) {
        double v = 0, amp = 0.5;
        for (int o = 0; o < 5; o++) {
            v += noise(x, y) * amp;
            x *= 2.03;
            y *= 2.03;
            amp *= 0.5;
        }
        return v;
    }

    public static ResourceLocation sky() {
        if (sky != null) return sky;
        NativeImage img = new NativeImage(W, H, true);
        // the sky: night blue at the top, violet lower down, a warm glow on the horizon
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                double t = y / (double) H;
                int col = t < 0.6 ? mix(0x060A1C, 0x1C1646, t / 0.6) : mix(0x1C1646, 0x46306E, (t - 0.6) / 0.4);
                // the nebula: soft violet and teal clouds of light
                double n = fbm(x / 70.0, y / 55.0);
                double n2 = fbm(x / 40.0 + 13, y / 40.0 + 7);
                col = mix(col, 0x6A3CA8, Math.max(0, n - 0.5) * 1.1);
                col = mix(col, 0x2E8A9C, Math.max(0, n2 - 0.58) * 1.0);
                // the Milky Way, a band of dust across the sky
                double band = Math.abs((y - H * 0.55) - (x - W * 0.5) * -0.45) / 40.0;
                col = mix(col, 0xB8A8E8, Math.max(0, 1 - band) * 0.25 * fbm(x / 18.0, y / 18.0));
                img.setPixelRGBA(x, y, c(col));
            }
        Random r = new Random(1312);
        // stars: hundreds of faint ones, more along the Milky Way
        for (int i = 0; i < 1100; i++) {
            int x = r.nextInt(W), y = r.nextInt((int) (H * 0.8));
            double band = Math.abs((y - H * 0.55) - (x - W * 0.5) * -0.45) / 40.0;
            if (band > 1 && r.nextInt(3) == 0) continue;
            blend(img, x, y, r.nextInt(5) == 0 ? 0xFFE6B0 : 0xEAF0FF, 0.35 + r.nextDouble() * 0.6);
        }
        // the brightest, with a cross of light, some of them joined into constellations
        int[][] bright = new int[44][];
        for (int i = 0; i < bright.length; i++) {
            int x = 10 + r.nextInt(W - 20), y = 8 + r.nextInt((int) (H * 0.62));
            bright[i] = new int[]{x, y};
            int col = i % 4 == 0 ? 0xFFE8B0 : 0xF4F8FF;
            int len = 2 + r.nextInt(3);
            for (int k = -len; k <= len; k++) {
                double f = 1 - Math.abs(k) / (len + 1.0);
                blend(img, x + k, y, col, f);
                blend(img, x, y + k, col, f);
            }
            for (int dy = -2; dy <= 2; dy++)
                for (int dx = -2; dx <= 2; dx++) blend(img, x + dx, y + dy, col, 0.18 * (1 - Math.hypot(dx, dy) / 3));
        }
        for (int k = 0; k + 1 < 14; k++) {
            if (k % 5 == 4) continue;
            int[] a = bright[k], b = bright[k + 1];
            if (Math.hypot(a[0] - b[0], a[1] - b[1]) > 120) continue;
            int steps = (int) Math.hypot(a[0] - b[0], a[1] - b[1]);
            for (int s = 0; s < steps; s += 2)
                blend(img, a[0] + (b[0] - a[0]) * s / steps, a[1] + (b[1] - a[1]) * s / steps, 0xE8C878, 0.22);
        }
        // far peaks, dark against the glow
        for (int x = 0; x < W; x++) {
            double peak = H * 0.74 - Math.abs(Math.sin(x * 0.021) * 22 + Math.sin(x * 0.053 + 1) * 10) - fbm(x / 20.0, 3) * 18;
            for (int y = (int) peak; y < H; y++) blend(img, x, y, 0x140E2A, 0.85);
            for (int y = (int) peak; y < peak + 2; y++) blend(img, x, y, 0x8A74C8, 0.35);
        }
        // the sea of clouds: rounded tops lit from above, layer over layer, paler towards the front
        for (int layer = 0; layer < 4; layer++) {
            double base = H * (0.78 + layer * 0.06);
            int top = mix(0x8C7CC8, 0xE8E0FF, layer / 3.0), body = mix(0x3A2E6A, 0x7A6AB0, layer / 3.0);
            for (int x = 0; x < W; x++) {
                double crest = base - Math.abs(Math.sin(x * (0.035 - layer * 0.004) + layer * 2)) * (10 + layer * 3) - fbm(x / 25.0, layer * 5) * 10;
                for (int y = (int) crest; y < H; y++) {
                    double d = y - crest;
                    blend(img, x, y, d < 2 ? top : mix(top, body, Math.min(1, d / 14)), 0.9);
                }
            }
        }
        sky = Minecraft.getInstance().getTextureManager().register(AwakeningSystemMod.MOD_ID + "_gacha_sky", new DynamicTexture(img));
        return sky;
    }
}
