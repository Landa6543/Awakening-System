import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Builds Kaisel, the shadow wyvern, as a GeckoLib model following the user's references (referencias/kaisel/):
 * a long serpentine neck with broad belly plates, a long narrow beaked head with a fan crown of thin horns and a
 * long toothed jaw, a lean body with ragged back spikes, raptor legs with long hooked talons, a whip tail that
 * curls at the end, and huge wings with torn trailing edges. Painted flat like the comic (colour areas and lines,
 * no pixel noise): the living wyvern bone-white with grey lines and golden eyes; the shadow blue-black with cyan
 * cracks, a glowing belly, glowing crown, teeth and talon tips.
 * args: outGeo.json outLiving.png outShadow.png outShadowGlow.png
 *
 * Units are model pixels (16 = one block). Kaisel faces north (-z); the feet stand on y = 0.
 */
public class KaiselGen {
    // ── model ───────────────────────────────────────────────────────────────
    static final class Bone {
        final String name, parent;
        final double[] pivot, rot;
        final List<Cube> cubes = new ArrayList<>();
        Bone(String name, String parent, double[] pivot, double[] rot) { this.name = name; this.parent = parent; this.pivot = pivot; this.rot = rot; }
    }

    static final class Cube {
        final double[] origin, size, pivot, rot;
        final String part;
        final int seed;
        final Map<String, int[]> uv = new LinkedHashMap<>();   // face -> u, v, w, h
        Cube(double[] origin, double[] size, double[] pivot, double[] rot, String part, int seed) {
            this.origin = origin; this.size = size; this.pivot = pivot; this.rot = rot; this.part = part; this.seed = seed;
        }
    }

    static final List<Bone> BONES = new ArrayList<>();
    static int cubeCount = 0;

    static Bone bone(String name, String parent, double px, double py, double pz, double rx, double ry, double rz) {
        Bone b = new Bone(name, parent, new double[]{px, py, pz}, new double[]{rx, ry, rz});
        BONES.add(b);
        return b;
    }

    static Cube box(Bone b, double x0, double y0, double z0, double sx, double sy, double sz, String part) {
        Cube c = new Cube(new double[]{x0, y0, z0}, new double[]{sx, sy, sz}, null, null, part, cubeCount++);
        b.cubes.add(c);
        return c;
    }

    static Cube rbox(Bone b, double x0, double y0, double z0, double sx, double sy, double sz, double px, double py, double pz,
                     double rx, double ry, double rz, String part) {
        Cube c = new Cube(new double[]{x0, y0, z0}, new double[]{sx, sy, sz}, new double[]{px, py, pz}, new double[]{rx, ry, rz}, part, cubeCount++);
        b.cubes.add(c);
        return c;
    }

    /** A centred box: centre x, bottom y, front z, width, height, length. */
    static Cube cbox(Bone b, double cx, double y0, double z0, double w, double h, double l, String part) {
        return box(b, cx - w / 2, y0, z0, w, h, l, part);
    }

    /** A ragged spike leaning back (toward +z) from a point on top of a bone. */
    static void spike(Bone b, double x, double y, double z, double w, double h, double lean, String part) {
        rbox(b, x - w / 2, y, z - w / 2, w, h, w, x, y, z, -lean, 0, 0, part);
    }

    static void build() {
        bone("root", null, 0, 0, 0, 0, 0, 0);
        // ── body: lean chest with a keel of belly plates, narrow waist, hips; ragged spikes down the back
        Bone body = bone("body", "root", 0, 29, 0, 0, 0, 0);
        cbox(body, 0, 21, -15, 15, 13, 15, "chest");          // chest
        cbox(body, 0, 18, -14, 9, 3, 13, "belly");            // keel
        cbox(body, 0, 22, 0, 11, 10, 9, "scale");             // waist
        cbox(body, 0, 20.5, 0.5, 7, 1.5, 8, "belly");
        cbox(body, 0, 22.5, 9, 12, 10, 10, "scale");          // hips
        cbox(body, 0, 21, 9.5, 8, 1.5, 9, "belly");
        double[][] back = {{-13, 34, 5.5}, {-9, 34, 7}, {-5, 34, 5}, {-1, 32, 6}, {3, 32, 4.5}, {7, 32.5, 5.5}, {11, 32.5, 4}, {15, 32.5, 5}};
        for (double[] s : back) spike(body, 0, s[1], s[0], 1.6, s[2], 32, "spike");
        for (int sd = -1; sd <= 1; sd += 2) {                 // smaller ragged spikes along the flanks
            spike(body, 6.5 * sd, 31, -10, 1.2, 3, 40, "spike");
            spike(body, 5 * sd, 30.5, 4, 1.2, 2.5, 40, "spike");
        }
        // ── neck: five segments in an S, belly plates underneath, a spike on each
        String parent = "body";
        double y = 30, z = -14;
        double[][] neck = {{8.5, 8, 9, 38}, {7.8, 7.4, 9, 18}, {7.2, 6.8, 9, 4}, {6.6, 6.2, 9, -16}, {6, 5.8, 8, -26}};
        for (int i = 0; i < neck.length; i++) {
            double w = neck[i][0], h = neck[i][1], l = neck[i][2];
            Bone n = bone("neck" + (i + 1), parent, 0, y, z, neck[i][3], 0, 0);
            cbox(n, 0, y - h / 2, z - l, w, h, l + 0.5, "scale");
            cbox(n, 0, y - h / 2 - 1.2, z - l + 0.3, w - 1.5, 1.3, l - 0.2, "belly");
            spike(n, 0, y + h / 2, z - l / 2, 1.3, 3 - i * 0.25, 35, "spike");
            parent = n.name; z -= l;
        }
        // ── head: narrow skull, long beak-like snout, brow plates, golden eyes, many fine teeth
        Bone head = bone("head", parent, 0, y, z, -6, 0, 0);
        cbox(head, 0, y - 4, z - 10, 10.5, 8.5, 10.5, "scale");       // skull
        cbox(head, 0, y + 3.5, z - 10, 8, 1.6, 8, "plate");           // forehead plate
        cbox(head, 0, y - 3.4, z - 20, 7.5, 6.2, 10.5, "scale");      // snout
        cbox(head, 0, y + 1.8, z - 20, 4.6, 1.5, 10, "plate");        // nose ridge
        cbox(head, 0, y - 2.8, z - 28, 5.2, 4.8, 8.5, "scale");       // beak
        cbox(head, 0, y - 3.6, z - 31.5, 3, 3.8, 3.6, "plate");       // beak tip, hooked down
        cbox(head, 0, y - 5, z - 31.8, 1.6, 1.6, 1.8, "plate");
        for (int sd = -1; sd <= 1; sd += 2) {
            box(head, sd < 0 ? -5.3 : 5.2, y - 0.6, z - 8, 0.1, 1.7, 3.6, "eye");
            box(head, (sd < 0 ? -5.8 : 4.8), y + 0.9, z - 9, 1, 1.1, 5, "plate");   // brow ridge
            for (int t = 0; t < 8; t++)                                         // upper teeth
                box(head, sd * (3 - t * 0.12) - 0.4, y - 5.2, z - 11.5 - t * 2.4, 0.8, 1.5 + (t == 0 ? 0.9 : 0), 0.8, "tooth");
        }
        Bone jaw = bone("jaw", "head", 0, y - 4, z - 3, 0, 0, 0);
        cbox(jaw, 0, y - 7.2, z - 30, 6, 3.2, 27, "scale");
        cbox(jaw, 0, y - 7.8, z - 29, 5, 0.6, 25, "belly");
        cbox(jaw, 0, y - 4.05, z - 29, 4.6, 0.1, 25, "mouth");
        for (int sd = -1; sd <= 1; sd += 2)
            for (int t = 0; t < 9; t++)
                box(jaw, sd * (2.4 - t * 0.05) - 0.35, y - 4.2, z - 28.5 + t * 2.6, 0.7, 1.4 + (t == 0 ? 1 : 0), 0.7, "tooth");
        // the crown: a fan of long thin horns sweeping back and up, longest in the middle rows
        double[][] crown = {
                // x, y offset, z offset, length, up-angle, out-angle, thickness
                {2.4, 4, -6.5, 26, 34, 8, 2}, {-2.4, 4, -6.5, 26, 34, -8, 2},
                {4, 3, -5, 22, 20, 20, 1.8}, {-4, 3, -5, 22, 20, -20, 1.8},
                {4.8, 1, -3.5, 17, 8, 32, 1.5}, {-4.8, 1, -3.5, 17, 8, -32, 1.5},
                {4.5, -1.8, -3, 12, -4, 42, 1.3}, {-4.5, -1.8, -3, 12, -4, -42, 1.3},
                {0, 4.6, -9, 10, 46, 0, 1.4}};
        for (int i = 0; i < crown.length; i++) {
            double[] c = crown[i];
            Bone h = bone("crown" + (i + 1), "head", c[0], y + c[1], z + c[2], -c[4], c[5], 0);
            double t = c[6];
            box(h, c[0] - t / 2, y + c[1] - t / 2, z + c[2], t, t, c[3] * 0.55, "crown");
            box(h, c[0] - t * 0.35, y + c[1] - t * 0.35, z + c[2] + c[3] * 0.55, t * 0.7, t * 0.7, c[3] * 0.45, "crowntip");
        }
        for (int sd = -1; sd <= 1; sd += 2)                     // cheek spikes behind the jaw
            rbox(jaw, sd * 2.5 - 0.5, y - 6.2, z - 4, 1, 1, 6, sd * 2.5, y - 5.7, z - 4, -10, sd * 30, 0, "crown");
        // ── tail: a long whip, thinning, curling up at the end, ragged spikes on top
        parent = "body"; y = 28; z = 18.5;
        double[][] tail = {{9, 8.5, 12, 14}, {7.6, 7, 12, -2}, {6.2, 5.8, 12, -4}, {5, 4.6, 12, -4}, {4, 3.6, 12, -6},
                {3.1, 2.8, 11, -12}, {2.4, 2.2, 10, -24}, {1.8, 1.6, 9, -36}, {1.3, 1.2, 8, -44}};
        for (int i = 0; i < tail.length; i++) {
            double w = tail[i][0], h = tail[i][1], l = tail[i][2];
            Bone t = bone("tail" + (i + 1), parent, 0, y, z, tail[i][3], 0, 0);
            cbox(t, 0, y - h / 2, z - 0.3, w, h, l + 0.3, "scale");
            if (w > 3) cbox(t, 0, y - h / 2 - 0.8, z + 0.3, w - 1.4, 0.8, l - 0.6, "belly");
            if (i < 7) spike(t, 0, y + h / 2, z + l * 0.5, Math.max(0.8, 1.4 - i * 0.08), Math.max(1.5, 3.4 - i * 0.3), 40, "spike");
            parent = t.name; z += l;
        }
        // ── raptor legs: thick thigh, backward shin with a spur, upright foot, three long toes and a back toe
        for (int sd = -1; sd <= 1; sd += 2) {
            String side = sd < 0 ? "left" : "right";
            double x = 7.5 * sd;
            Bone thigh = bone("thigh_" + side, "body", x, 28, 12, -20, 0, 0);
            cbox(thigh, x, 15.5, 6.5, 7.5, 14, 11, "scale");
            spike(thigh, x + 3.3 * sd, 24, 14, 1, 3, 70, "spike");
            Bone shin = bone("shin_" + side, "thigh_" + side, x, 17, 12, 62, 0, 0);
            cbox(shin, x, 5, 9.5, 4.8, 13, 4.8, "scale");
            spike(shin, x, 13, 14, 1, 2.6, 80, "spike");
            Bone foot = bone("foot_" + side, "shin_" + side, x, 6, 12, -42, 0, 0);
            cbox(foot, x, -0.5, 10, 4, 8, 4, "scale");
            Bone toes = bone("toes_" + side, "foot_" + side, x, 0.5, 11, 0, 0, 0);
            for (int t = -1; t <= 1; t++) {
                double tx = x + t * 1.7;
                Bone toe = bone("toe" + (t + 2) + "_" + side, "toes_" + side, tx, 0.5, 10.5, 0, t * 16, 0);
                cbox(toe, tx, -0.2, 3, 1.8, 1.8, 7.8, "scale");
                rbox(toe, tx - 0.7, -1.2, -0.6, 1.4, 2, 4.4, tx, 0.4, 3, 34, 0, 0, "talon");
            }
            cbox(toes, x, -0.2, 11, 1.3, 1.3, 3.5, "scale");                         // back toe
            rbox(toes, x - 0.5, -0.9, 14, 1, 1.4, 2.4, x, 0.2, 14.3, -30, 0, 0, "talon");
        }
        // ── wings: shoulder, long forearm, wrist claw, four fingers with a torn membrane
        for (int sd = -1; sd <= 1; sd += 2) {
            String side = sd < 0 ? "left" : "right";
            double sx = 7.5 * sd;
            Bone arm = bone("wing_" + side, "body", sx, 31, -9, 0, 16 * sd, 14 * sd);
            box(arm, sd > 0 ? sx : sx - 20, 29, -11, 20, 4, 4, "scale");
            spike(arm, sx + 19 * sd, 33, -9, 1.2, 2.5, 50, "spike");
            triMembrane(arm, sd, sx, 30, -7.2, 20, 100, 32, 3, false);
            double ex = sx + 20 * sd;
            Bone fore = bone("forearm_" + side, "wing_" + side, ex, 31, -9, 0, -32 * sd, -8 * sd);
            box(fore, sd > 0 ? ex : ex - 26, 29.5, -10.5, 26, 3, 3, "scale");
            triMembrane(fore, sd, ex, 30.4, -7.6, 26, 80, 38, 3, false);
            triMembrane(fore, sd, ex, 30.35, -7.6, 3, 150, 34, 0, false);
            double wx = ex + 26 * sd;
            Bone hand = bone("hand_" + side, "forearm_" + side, wx, 31, -9, 0, 0, 0);
            box(hand, wx - 1.6, 29.4, -10.6, 3.2, 3.2, 3.2, "scale");
            rbox(hand, wx - 0.6, 30.2, -16, 1.2, 1.2, 6, wx, 31, -10.5, 0, -12 * sd, 0, "scale");   // thumb
            rbox(hand, wx - 0.5, 29.4, -18.8, 1, 1.6, 3.2, wx, 30.4, -16, 30, -12 * sd, 0, "talon");
            double[][] fingers = {{6, 48}, {34, 44}, {60, 38}, {84, 30}};
            for (int f = 0; f < fingers.length; f++) {
                double ang = fingers[f][0], len = fingers[f][1];
                Bone fb = bone("finger" + (f + 1) + "_" + side, "hand_" + side, wx, 31, -9, 0, -ang * sd, 0);
                box(fb, sd > 0 ? wx : wx - len, 30.3, -9.7, len, 1.4, 1.4, "bone");
                rbox(fb, (sd > 0 ? wx + len : wx - len) - 0.5, 30.4, -10.8, 1, 1, 2.4, sd > 0 ? wx + len : wx - len, 31, -9, 0, 0, 0, "talon");
                double gap = f < fingers.length - 1 ? fingers[f + 1][0] - ang : 34;
                double len2 = f < fingers.length - 1 ? fingers[f + 1][1] : len * 0.85;
                triMembrane(fb, sd, wx, 30.6, -8.3, len, gap, len2, 5, true);
            }
        }
    }

    /**
     * A triangular wing membrane in a bone's own frame, filled with thin strips along the bone (x, toward +x when
     * s > 0): corners at the bone's start, along it at {@code len}, and at {@code len2} turned {@code gap} degrees
     * toward +z. The trailing edge bows inward and, if {@code torn}, has ragged notches like the comic's wings.
     */
    static void triMembrane(Bone b, int s, double x0, double y, double z0, double len, double gap, double len2, double scallop, boolean torn) {
        double bx = len, bz = 0, cx = len2 * Math.cos(Math.toRadians(gap)), cz = len2 * Math.sin(Math.toRadians(gap));
        double zmax = Math.max(bz, cz), step = 1.5;
        Random r = new Random(Math.round(x0 * 31 + len * 7 + gap));
        for (double zs = 0; zs < zmax - 0.2; zs += step) {
            double zc = zs + step / 2;
            double lo = 1e9, hi = -1e9;
            double[][] edges = {{0, 0, bx, bz}, {bx, bz, cx, cz}, {cx, cz, 0, 0}};
            for (double[] e : edges) {
                if ((zc - e[1]) * (zc - e[3]) > 0 || e[1] == e[3]) continue;
                double t = (zc - e[1]) / (e[3] - e[1]), x = e[0] + t * (e[2] - e[0]);
                lo = Math.min(lo, x); hi = Math.max(hi, x);
            }
            if (hi - lo < 0.8) continue;
            double tEdge = (zc - bz) / Math.max(1e-6, cz - bz);
            hi -= scallop * Math.sin(Math.PI * Math.max(0, Math.min(1, tEdge)));
            if (torn && r.nextInt(3) == 0) hi -= 1.5 + r.nextDouble() * 3;      // a rip in the trailing edge
            if (hi - lo < 0.8) continue;
            double xa = s > 0 ? x0 + lo : x0 - hi;
            box(b, xa, y, z0 + zs, hi - lo, 0.4, step + 0.05, "membrane");
        }
    }

    // ── UV packing (one rectangle per face, 1 texel per model pixel, 2 texels for small parts) ───────────
    static int texW = 256, texH;

    static void pack() {
        List<int[]> rects = new ArrayList<>();    // w, h, cubeIndex*8+face
        List<Cube> all = new ArrayList<>();
        String[] faces = {"north", "south", "east", "west", "up", "down"};
        for (Bone b : BONES) for (Cube c : b.cubes) {
            all.add(c);
            int w = Math.max(1, (int) Math.ceil(c.size[0])), h = Math.max(1, (int) Math.ceil(c.size[1])), d = Math.max(1, (int) Math.ceil(c.size[2]));
            int[][] dims = {{w, h}, {w, h}, {d, h}, {d, h}, {w, d}, {w, d}};
            for (int f = 0; f < 6; f++) rects.add(new int[]{dims[f][0], dims[f][1], (all.size() - 1) * 8 + f});
        }
        rects.sort((p, q) -> q[1] != p[1] ? q[1] - p[1] : q[0] - p[0]);
        int x = 0, y = 0, rowH = 0;
        for (int[] r : rects) {
            if (x + r[0] > texW) { x = 0; y += rowH; rowH = 0; }
            all.get(r[2] / 8).uv.put(faces[r[2] % 8], new int[]{x, y, r[0], r[1]});
            x += r[0]; rowH = Math.max(rowH, r[1]);
        }
        int need = y + rowH;
        texH = 16;
        while (texH < need) texH *= 2;
    }

    // ── painting: flat colour areas and lines, like the comic ────────────────
    record Palette(int skin, int skinShade, int line, int crack, int belly, int bellyLine, int plate, int crown, int crownTip,
                   int talon, int talonTip, int membrane, int membraneVein, int eye, int mouth, int tooth, boolean glows) {}

    static final Palette LIVING = new Palette(0xEDEFF1, 0xC9CDD3, 0x5C616B, 0x8C929C, 0xF8F9FA, 0xA5ACB6, 0xF4F5F6,
            0xF2F3F4, 0xD3D7DD, 0xE4DDD2, 0x3A3530, 0x4E525C, 0x33363E, 0xFFB01C, 0x5E1A24, 0xFFFFFF, false);
    static final Palette SHADOW = new Palette(0x0D1222, 0x080B16, 0x03050B, 0x3FCBFF, 0xA8F0FF, 0x3796D8, 0x141B33,
            0xC8F6FF, 0xEFFDFF, 0x0A0E1A, 0x8FEAFF, 0x080B16, 0x16294D, 0xEFFFFF, 0x0A2240, 0xDDFBFF, true);

    static int mix(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t), g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t), bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    static void paint(BufferedImage img, BufferedImage glow, Palette p) {
        for (Bone b : BONES) for (Cube c : b.cubes)
            for (Map.Entry<String, int[]> e : c.uv.entrySet()) {
                int[] r = e.getValue();
                for (int v = 0; v < r[3]; v++)
                    for (int u = 0; u < r[2]; u++) {
                        long px = texel(c, e.getKey(), u, v, r[2], r[3], p);
                        int col = (int) (px & 0xFFFFFF);
                        boolean lit = (px >> 32) != 0;
                        img.setRGB(r[0] + u, r[1] + v, 0xFF000000 | col);
                        if (glow != null) glow.setRGB(r[0] + u, r[1] + v, lit ? (0xFF000000 | col) : 0);
                    }
            }
    }

    static long lit(int col, boolean glowing) { return (glowing ? 1L << 32 : 0) | (col & 0xFFFFFFL); }

    /** A few long cracks across a face, the comic's lines over the skin (cyan and glowing on the shadow). */
    static boolean crackAt(int seed, int u, int v, int w, int h) {
        if (w < 6 || h < 4) return false;
        Random r = new Random(seed * 7919L + w * 31L + h);
        int n = 1 + (w * h) / 110;
        for (int i = 0; i < n; i++) {
            // a long, slightly wandering line drawn in small steps so it never breaks into dots
            double x = r.nextInt(w), y = r.nextInt(h);
            double ang = r.nextDouble() * Math.PI * 2;
            int len = Math.max(w, h) / 2 + r.nextInt(Math.max(2, Math.max(w, h) / 2));
            for (int k = 0; k < len * 2; k++) {
                if ((int) Math.floor(x) == u && (int) Math.floor(y) == v) return true;
                x += Math.cos(ang) * 0.5; y += Math.sin(ang) * 0.5;
                if (k % 6 == 5) ang += r.nextDouble() * 0.8 - 0.4;
            }
        }
        return false;
    }

    static long texel(Cube c, String face, int u, int v, int w, int h, Palette p) {
        boolean top = face.equals("up"), bottom = face.equals("down"), side = !top && !bottom;
        boolean edge = u == 0 || v == 0 || u == w - 1 || v == h - 1;
        switch (c.part) {
            case "scale": {
                if (bottom) return lit(p.skinShade, false);
                // flat skin, darker toward the bottom of side faces, an outline on the lower edge, and cracks
                int base = side && h > 3 && v > h * 0.6 ? p.skinShade : p.skin;
                if (side && v == h - 1) base = p.line;
                if (crackAt(c.seed * 13 + face.hashCode(), u, v, w, h)) return lit(p.crack, p.glows);
                return lit(base, false);
            }
            case "belly": {
                // broad ventral plates: bands with a dark seam, brightest in the middle (the shadow's glowing belly)
                int along = bottom || top ? v : u, across = bottom || top ? u : v, span = bottom || top ? w : h;
                boolean seam = along % 3 == 2;
                double mid = span <= 1 ? 1 : 1 - Math.abs(across - (span - 1) / 2.0) / (span / 2.0);
                int col = seam ? p.bellyLine : mix(p.belly, p.glows ? 0xFFFFFF : p.belly, mid * 0.5);
                return lit(col, p.glows);
            }
            case "chest": {
                // the breast: skin on the sides and top, the glowing plated belly on the front and underside
                if (face.equals("north") || bottom) {
                    boolean seam = v % 3 == 2;
                    double mid = 1 - Math.abs(u - (w - 1) / 2.0) / (w / 2.0);
                    return lit(seam ? p.bellyLine : mix(p.belly, p.glows ? 0xFFFFFF : p.belly, mid * 0.5), p.glows);
                }
                if (crackAt(c.seed * 13 + face.hashCode(), u, v, w, h)) return lit(p.crack, p.glows);
                return lit(side && v > h * 0.6 ? p.skinShade : p.skin, false);
            }
            case "plate": return lit(edge && v == h - 1 ? p.line : p.plate, false);
            case "spike": {
                // ragged spikes: dark with a bright tip on the shadow
                boolean tip = side ? v < Math.max(1, h / 3) : top;
                return lit(tip && p.glows ? p.crack : (edge ? p.line : p.skinShade), tip && p.glows);
            }
            case "crown": return lit(edge && side && v == h - 1 ? p.crownTip : p.crown, p.glows);
            case "crowntip": return lit(p.crownTip, p.glows);
            case "talon": {
                boolean tip = side ? u < Math.max(1, w / 2) : true;
                return lit(tip ? p.talonTip : p.talon, tip && p.glows);
            }
            case "tooth": return lit(p.tooth, p.glows);
            case "bone": return lit(edge ? p.line : p.skinShade, false);
            case "membrane": {
                if (side) return lit(p.line, false);
                boolean vein = (u + v * 2) % 11 == 0 || u == 0;
                return lit(vein ? p.membraneVein : p.membrane, false);
            }
            case "eye": return lit(p.eye, true);
            case "mouth": return lit(p.mouth, p.glows);
            default: return lit(0xFF00FF, false);
        }
    }

    // ── writing ──────────────────────────────────────────────────────────────
    static String f(double d) {
        if (Math.abs(d) < 1e-9) return "0";
        String s = String.format(Locale.ROOT, "%.3f", d).replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }

    static String a3(double[] v) { return "[" + f(v[0]) + ", " + f(v[1]) + ", " + f(v[2]) + "]"; }

    static String geo() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"format_version\": \"1.12.0\",\n  \"minecraft:geometry\": [\n    {\n      \"description\": {\n");
        sb.append("        \"identifier\": \"geometry.kaisel\",\n        \"texture_width\": ").append(texW).append(",\n        \"texture_height\": ").append(texH).append(",\n");
        sb.append("        \"visible_bounds_width\": 12,\n        \"visible_bounds_height\": 6,\n        \"visible_bounds_offset\": [0, 2, 0]\n      },\n      \"bones\": [\n");
        for (int i = 0; i < BONES.size(); i++) {
            Bone b = BONES.get(i);
            sb.append("        {\"name\": \"").append(b.name).append("\"");
            if (b.parent != null) sb.append(", \"parent\": \"").append(b.parent).append("\"");
            sb.append(", \"pivot\": ").append(a3(b.pivot));
            if (b.rot[0] != 0 || b.rot[1] != 0 || b.rot[2] != 0) sb.append(", \"rotation\": ").append(a3(b.rot));
            if (!b.cubes.isEmpty()) {
                sb.append(", \"cubes\": [\n");
                for (int j = 0; j < b.cubes.size(); j++) {
                    Cube c = b.cubes.get(j);
                    sb.append("          {\"origin\": ").append(a3(c.origin)).append(", \"size\": ").append(a3(c.size));
                    if (c.pivot != null) sb.append(", \"pivot\": ").append(a3(c.pivot)).append(", \"rotation\": ").append(a3(c.rot));
                    sb.append(", \"uv\": {");
                    int k = 0;
                    for (Map.Entry<String, int[]> e : c.uv.entrySet()) {
                        int[] r = e.getValue();
                        sb.append(k++ > 0 ? ", " : "").append("\"").append(e.getKey()).append("\": {\"uv\": [").append(r[0]).append(", ").append(r[1])
                                .append("], \"uv_size\": [").append(r[2]).append(", ").append(r[3]).append("]}");
                    }
                    sb.append("}}").append(j < b.cubes.size() - 1 ? ",\n" : "\n");
                }
                sb.append("        ]");
            }
            sb.append("}").append(i < BONES.size() - 1 ? ",\n" : "\n");
        }
        sb.append("      ]\n    }\n  ]\n}\n");
        return sb.toString();
    }

    public static void main(String[] a) throws Exception {
        build();
        pack();
        Files.write(new File(a[0]).toPath(), geo().getBytes(StandardCharsets.UTF_8));
        BufferedImage living = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB), livingGlow = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB);
        paint(living, livingGlow, LIVING);                     // only the golden eyes glow on the living wyvern
        ImageIO.write(living, "png", new File(a[1]));
        ImageIO.write(livingGlow, "png", new File(a[1].replace(".png", "_glowmask.png")));
        BufferedImage shadow = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB), glow = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB);
        paint(shadow, glow, SHADOW);
        ImageIO.write(shadow, "png", new File(a[2]));
        ImageIO.write(glow, "png", new File(a[3]));
        int cubes = 0;
        for (Bone b : BONES) cubes += b.cubes.size();
        System.out.println("bones " + BONES.size() + ", cubes " + cubes + ", texture " + texW + "x" + texH);
    }
}
