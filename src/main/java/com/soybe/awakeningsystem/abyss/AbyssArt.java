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
 * The Spiral Abyss window's pictures, painted here in code as small pixel art and drawn scaled up: a hall of
 * arches and columns open onto a night sky with a glowing ring in the middle, and one tall banner per floor
 * (numeral, a stair climbing to a door under the stars, a chest below). All our own drawing.
 */
public final class AbyssArt {
    private AbyssArt() {}

    public static final int BG_W = 320, BG_H = 150;
    public static final int BANNER_W = 48, BANNER_H = 96;

    private static ResourceLocation background;
    private static final Map<String, ResourceLocation> BANNERS = new HashMap<>();

    // ---- colours (ABGR is what NativeImage takes; written as RGB and turned)
    private static int c(int rgb) {
        return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int mix(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private static ResourceLocation upload(String name, NativeImage img) {
        DynamicTexture tex = new DynamicTexture(img);
        return Minecraft.getInstance().getTextureManager().register(AwakeningSystemMod.MOD_ID + "_abyss_" + name, tex);
    }

    // ---- the hall

    public static ResourceLocation background() {
        if (background != null) return background;
        NativeImage img = new NativeImage(BG_W, BG_H, true);
        Random r = new Random(17);
        // the sky: deep blue at the top, turning teal near the horizon
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++)
                img.setPixelRGBA(x, y, c(mix(0x0B1838, 0x3A86A8, Math.pow(y / (double) BG_H, 1.4))));
        // stars
        for (int i = 0; i < 90; i++) {
            int x = r.nextInt(BG_W), y = r.nextInt(BG_H * 2 / 3);
            img.setPixelRGBA(x, y, c(mix(0x9FD8FF, 0xFFFFFF, r.nextDouble())));
        }
        // clouds along the bottom, in blocky steps
        for (int x = 0; x < BG_W; x += 3) {
            int top = BG_H - 34 + (int) (Math.sin(x * 0.07) * 5 + Math.sin(x * 0.19) * 3);
            for (int y = top; y < BG_H; y++)
                for (int dx = 0; dx < 3 && x + dx < BG_W; dx++)
                    img.setPixelRGBA(x + dx, y, c(mix(0x9FCBE0, 0xD8EEF5, (y - top) / 40.0 + (((x / 3 + y / 3) & 1) * 0.08))));
        }
        // the glowing ring in the middle
        int cx = BG_W / 2, cy = 66;
        for (int y = 0; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++) {
                double d = Math.hypot(x - cx, (y - cy) * 1.05);
                if (d < 46) {
                    int base = img.getPixelRGBA(x, y);
                    int rgb = ((base & 0xFF) << 16) | (base & 0xFF00) | ((base >> 16) & 0xFF);
                    double glow = Math.max(0, 1 - d / 46);
                    img.setPixelRGBA(x, y, c(mix(rgb, 0x9FF7F0, glow * glow * 0.9)));
                }
                if (d < 20) img.setPixelRGBA(x, y, c(mix(0xE6FFFC, 0xB8FBF3, d / 20)));
                if (Math.abs(d - 23) < 1.0 || Math.abs(d - 27) < 0.6) img.setPixelRGBA(x, y, c(0xDFFFFB));
            }
        // a slim mark through the ring
        for (int y = cy - 17; y <= cy + 22; y++) img.setPixelRGBA(cx, y, c(0x5FC6C0));
        // the arches: pairs of columns and pointed arches, nearer ones darker and bigger
        int[][] arches = {{cx, 70, 92, 0x1E6A72}, {cx, 100, 120, 0x175560}, {cx, 136, 150, 0x10404C}};
        for (int[] a : arches) {
            int half = a[1], topY = 150 - a[2], col = a[3];
            for (int side = -1; side <= 1; side += 2) {
                int px = a[0] + side * half;
                // the column
                for (int y = topY + a[2] / 2; y < BG_H; y++)
                    for (int dx = -5; dx <= 5; dx++) {
                        int x = px + dx;
                        if (x < 0 || x >= BG_W) continue;
                        img.setPixelRGBA(x, y, c(Math.abs(dx) == 5 ? mix(col, 0x7FE0D8, 0.35) : col));
                    }
                // the arch: a curve from the column's top to the middle
                for (int t = 0; t <= 200; t++) {
                    double s = t / 200.0;
                    int x = (int) Math.round(px - side * half * s);
                    int y = (int) Math.round(topY + a[2] / 2.0 - Math.sin(s * Math.PI / 2) * (a[2] / 2.0 - 4));
                    for (int w = -3; w <= 3; w++)
                        if (x >= 0 && x < BG_W && y + w >= 0 && y + w < BG_H)
                            img.setPixelRGBA(x, y + w, c(Math.abs(w) == 3 ? mix(col, 0x7FE0D8, 0.35) : col));
                }
            }
        }
        // the floor of the hall
        for (int y = BG_H - 8; y < BG_H; y++)
            for (int x = 0; x < BG_W; x++)
                img.setPixelRGBA(x, y, c(y == BG_H - 8 ? 0x2E7F86 : (((x / 6) & 1) == 0 ? 0x123F49 : 0x0F3640)));
        background = upload("hall", img);
        return background;
    }

    // ---- a floor's banner

    /** state: 0 open, 1 locked, 2 its reward taken. */
    public static ResourceLocation banner(int floor, int state) {
        String key = floor + "_" + state;
        ResourceLocation done = BANNERS.get(key);
        if (done != null) return done;
        NativeImage img = new NativeImage(BANNER_W, BANNER_H, true);
        boolean locked = state == 1;
        int frame = locked ? 0x3B4656 : 0x1D5FA8, frameLight = locked ? 0x6B7686 : 0x46C6E8, frameDark = locked ? 0x1E2530 : 0x0E2E5C;
        // the pointed top and the body
        for (int y = 0; y < BANNER_H; y++)
            for (int x = 0; x < BANNER_W; x++) {
                int cxp = BANNER_W / 2;
                boolean inside = y >= 10 || Math.abs(x - cxp) <= y * 2.4;
                if (y > BANNER_H - 6) inside = Math.abs(x - cxp) <= (BANNER_H - y) * 4;
                if (!inside) continue;
                img.setPixelRGBA(x, y, c(frame));
            }
        // the inner picture: a starry sky over a stair to a door
        int ix0 = 6, ix1 = BANNER_W - 7, iy0 = 22, iy1 = BANNER_H - 20;
        Random r = new Random(floor * 31L);
        for (int y = iy0; y <= iy1; y++)
            for (int x = ix0; x <= ix1; x++)
                img.setPixelRGBA(x, y, c(locked ? mix(0x1A1F28, 0x2E3644, (y - iy0) / (double) (iy1 - iy0))
                        : mix(0x0A1A4A, 0x2D63B5, (y - iy0) / (double) (iy1 - iy0))));
        for (int i = 0; i < 14; i++)
            img.setPixelRGBA(ix0 + r.nextInt(ix1 - ix0), iy0 + r.nextInt((iy1 - iy0) / 2), c(locked ? 0x8892A0 : 0xD6F2FF));
        int mid = BANNER_W / 2;
        // the door at the top of the stair
        for (int y = iy0 + 14; y < iy0 + 30; y++)
            for (int x = mid - 4; x <= mid + 4; x++)
                img.setPixelRGBA(x, y, c(y < iy0 + 17 && Math.abs(x - mid) > y - iy0 - 14 ? (locked ? 0x2E3644 : 0x1A3D80) : (locked ? 0x58606C : 0x8FE8FF)));
        // the stair, widening towards the viewer
        for (int s = 0; s < 8; s++) {
            int y = iy0 + 30 + s * 3, half = 4 + s * 2;
            for (int dy = 0; dy < 3; dy++)
                for (int x = mid - half; x <= mid + half; x++)
                    if (x >= ix0 && x <= ix1 && y + dy <= iy1)
                        img.setPixelRGBA(x, y + dy, c(dy == 0 ? (locked ? 0x9AA3AF : 0xA8E6F5) : (locked ? 0x5D6570 : 0x3B7FB8)));
        }
        // side pillars inside the frame
        for (int y = iy0 + 6; y <= iy1; y++) {
            img.setPixelRGBA(ix0 + 2, y, c(locked ? 0x4A5462 : 0x2A8FC8));
            img.setPixelRGBA(ix1 - 2, y, c(locked ? 0x4A5462 : 0x2A8FC8));
        }
        // the frame's edges
        for (int y = 10; y < BANNER_H - 6; y++) {
            img.setPixelRGBA(0, y, c(frameDark));
            img.setPixelRGBA(BANNER_W - 1, y, c(frameDark));
            img.setPixelRGBA(2, y, c(frameLight));
            img.setPixelRGBA(BANNER_W - 3, y, c(frameLight));
        }
        for (int x = ix0 - 1; x <= ix1 + 1; x++) {
            img.setPixelRGBA(x, iy0 - 1, c(frameLight));
            img.setPixelRGBA(x, iy1 + 1, c(frameLight));
        }
        // the numeral's disc at the top
        int dcy = 13;
        for (int y = dcy - 8; y <= dcy + 8; y++)
            for (int x = mid - 8; x <= mid + 8; x++) {
                double d = Math.hypot(x - mid, y - dcy);
                if (d <= 8) img.setPixelRGBA(x, y, c(d > 6.8 ? frameLight : (locked ? 0xB8BEC8 : 0xF4FBFF)));
            }
        // the chest (or an open one once its reward is taken, or a lock)
        int chy = BANNER_H - 15;
        for (int y = chy; y < chy + 8; y++)
            for (int x = mid - 6; x <= mid + 6; x++) {
                int col;
                if (locked) col = (Math.abs(x - mid) <= 1 && y >= chy + 2) ? 0x2A2F38 : 0x8A929E;
                else if (state == 2) col = y < chy + 3 ? 0x3A2410 : 0x8A5A22;
                else col = (y == chy + 3 || Math.abs(x - mid) <= 1 && y >= chy + 2 && y <= chy + 5) ? 0xFFE27A : 0xB8742A;
                img.setPixelRGBA(x, y, c(col));
            }
        ResourceLocation id = upload("banner_" + key, img);
        BANNERS.put(key, id);
        return id;
    }
}
