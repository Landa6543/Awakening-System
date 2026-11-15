import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Kaisel's animations, written as functions of time (waves with a delay between joints, so wings, neck and tail
 * move like whips, not like planks) and baked to a GeckoLib animation file at 20 frames a second.
 *   KaiselAnim out.animation.json              writes all the animations
 *   KaiselAnim pose <name> <time> out.json     writes one frame, for GeoViews to draw
 *
 * Rotations are in the model's own (Bedrock) convention, added to the rest pose: +x lifts parts that point
 * forward (the neck) and lowers parts that point back (the tail) and swings legs forward; +z lifts the right wing.
 */
public class KaiselAnim {
    static final String[] SIDES = {"left", "right"};

    /** One frame: bone -> {rx, ry, rz, px, py, pz}. */
    static final class Pose {
        final Map<String, double[]> m = new LinkedHashMap<>();
        void rot(String bone, double x, double y, double z) { double[] v = get(bone); v[0] += x; v[1] += y; v[2] += z; }
        void pos(String bone, double x, double y, double z) { double[] v = get(bone); v[3] += x; v[4] += y; v[5] += z; }
        double[] get(String bone) { return m.computeIfAbsent(bone, k -> new double[6]); }
        /** A wing joint on one side: the left side mirrors y and z. */
        void wing(String bone, int s, double x, double y, double z) { rot(bone + "_" + SIDES[s > 0 ? 1 : 0], x, y * s, z * s); }
    }

    static double sin(double a) { return Math.sin(a); }
    static double cos(double a) { return Math.cos(a); }
    static double smooth(double t) { t = Math.max(0, Math.min(1, t)); return t * t * (3 - 2 * t); }
    /** 0 -> 1 -> 0 over [a, b, c, d]: rises a..b, holds b..c, falls c..d. */
    static double env(double t, double a, double b, double c, double d) {
        return t < a ? 0 : t < b ? smooth((t - a) / (b - a)) : t < c ? 1 : t < d ? 1 - smooth((t - c) / (d - c)) : 0;
    }

    // ── shared pieces ────────────────────────────────────────────────────────
    /** Wings folded along the body, as when standing or walking; {@code breath} adds a little life. */
    static void foldedWings(Pose p, double breath) {
        for (int s = -1; s <= 1; s += 2) {
            // elbow high at the shoulder, forearm folded forward along it, fingers swept back over the flank
            p.wing("wing", s, 0, -62, 30 + breath);
            p.wing("forearm", s, 0, 152, -40);
            p.wing("hand", s, 0, -30, 10);
            for (int f = 1; f <= 4; f++) p.wing("finger" + f, s, 0, -118 + f * 18, -18 + f * 2);
        }
    }

    /** Neck and head held straight out in front, for flying. */
    static void flightNeck(Pose p, double wave) {
        double[] off = {-34, -16, -4, 14, 24};
        for (int i = 0; i < 5; i++) p.rot("neck" + (i + 1), off[i] + wave * sin(i * 0.6), 0, 0);
        p.rot("head", 6, 0, 0);
    }

    /** Legs drawn back under the tail, talons curled, for flying. */
    static void tuckedLegs(Pose p) {
        for (String side : SIDES) {
            p.rot("thigh_" + side, -48, 0, 0);
            p.rot("shin_" + side, -20, 0, 0);
            p.rot("foot_" + side, 30, 0, 0);
            for (int t = 1; t <= 3; t++) p.rot("toe" + t + "_" + side, 35, 0, 0);
        }
    }

    /** A sideways wave down the tail, each segment a little later and wider than the one before. */
    static void tailSway(Pose p, double ph, double amp, double lift) {
        for (int i = 1; i <= 9; i++) p.rot("tail" + i, lift * sin(ph - i * 0.55), amp * (0.4 + i * 0.12) * sin(ph - i * 0.55), 0);
    }

    // ── the animations ───────────────────────────────────────────────────────
    static Pose idle(double t) {
        double ph = t / 4.0 * 2 * Math.PI;
        Pose p = new Pose();
        p.pos("body", 0, 0.35 * sin(ph), 0);
        p.rot("body", 1.2 * sin(ph), 0, 0);
        for (int i = 1; i <= 5; i++) p.rot("neck" + i, 2.2 * sin(ph - i * 0.45), 1.5 * sin(ph * 0.5 - i * 0.4), 0);
        p.rot("head", -2.5 * sin(ph - 2.6), 3 * sin(ph * 0.5 - 2.4), 0);
        p.rot("jaw", -1.5 - 1.5 * sin(ph - 1.2), 0, 0);
        tailSway(p, ph * 0.5, 4, 1.5);
        foldedWings(p, 2 * sin(ph));
        return p;
    }

    static Pose walk(double t) {
        double ph = t / 1.6 * 2 * Math.PI;
        Pose p = new Pose();
        p.pos("body", 0, -0.6 + 0.6 * Math.abs(sin(ph)), 0);
        p.rot("body", 0, 3 * sin(ph), 1.5 * sin(ph));
        for (int s = 0; s < 2; s++) {
            String side = SIDES[s];
            double q = ph + (s == 0 ? 0 : Math.PI);
            p.rot("thigh_" + side, 26 * sin(q), 0, 0);
            p.rot("shin_" + side, -16 * Math.max(0, -cos(q)) + 6 * sin(q), 0, 0);
            p.rot("foot_" + side, -18 * sin(q) + 14 * Math.max(0, -cos(q)), 0, 0);
            for (int k = 1; k <= 3; k++) p.rot("toe" + k + "_" + side, 18 * Math.max(0, -cos(q)), 0, 0);
        }
        for (int i = 1; i <= 5; i++) p.rot("neck" + i, 3.5 * sin(2 * ph - i * 0.5), 3 * sin(ph - i * 0.4), 0);
        p.rot("head", -3 * sin(2 * ph - 2.6), -4 * sin(ph - 2.2), 0);
        tailSway(p, ph, 7, 2);
        foldedWings(p, 1.5 * sin(2 * ph));
        return p;
    }

    static Pose fly(double t) {
        double ph = t / 1.5 * 2 * Math.PI;
        Pose p = new Pose();
        // the body rises as the wings come down
        p.pos("body", 0, -1.4 * sin(ph), 0);
        p.rot("body", 2 * cos(ph), 0, 0);
        for (int s = -1; s <= 1; s += 2) {
            p.wing("wing", s, 0, 8 * cos(ph), 2 + 34 * sin(ph));
            p.wing("forearm", s, 0, -4 * cos(ph - 0.9), 18 * sin(ph - 0.9));
            p.wing("hand", s, 0, 6 * sin(ph - 1.5), 14 * sin(ph - 1.6));
            for (int f = 1; f <= 4; f++) p.wing("finger" + f, s, 0, -3 * f * sin(ph - 1.8), 6 * sin(ph - 1.9 - f * 0.15));
        }
        flightNeck(p, 3 * sin(ph - 1));
        p.rot("jaw", -2, 0, 0);
        tuckedLegs(p);
        p.rot("tail1", -12, 0, 0);
        tailSway(p, ph, 3, 4);
        return p;
    }

    static Pose glide(double t) {
        double ph = t / 6.0 * 2 * Math.PI;
        Pose p = new Pose();
        p.pos("body", 0, 0.8 * sin(ph), 0);
        p.rot("body", 1.5 * sin(ph), 0, 3 * sin(ph * 0.5));
        for (int s = -1; s <= 1; s += 2) {
            p.wing("wing", s, 0, 2 * sin(ph), 4 + 3 * sin(ph));
            p.wing("forearm", s, 0, 0, 2 * sin(ph - 0.6));
            p.wing("hand", s, 0, 0, 2 * sin(ph - 1.2));
        }
        flightNeck(p, 1.5 * sin(ph));
        tuckedLegs(p);
        p.rot("tail1", -12, 0, 0);
        tailSway(p, ph, 5, 2);
        return p;
    }

    /** A lunging bite: rear back, strike forward with the jaws wide, snap them shut, recover. Neck and head only. */
    static Pose bite(double t) {
        Pose p = new Pose();
        double back = env(t, 0.0, 0.22, 0.24, 0.34), strike = env(t, 0.26, 0.38, 0.48, 0.8);
        double open = env(t, 0.12, 0.3, 0.36, 0.46);
        double[] up = {12, 6, 4, 2, 0}, fwd = {-22, -14, -8, 6, 10};
        for (int i = 0; i < 5; i++) p.rot("neck" + (i + 1), up[i] * back + fwd[i] * strike, 0, 0);
        p.rot("head", 14 * back - 8 * strike, 0, 0);
        p.rot("jaw", -38 * open, 0, 0);
        return p;
    }

    /** A roar: head thrown up, jaws wide and trembling, the crown raised. Neck, head and crown only. */
    static Pose roar(double t) {
        Pose p = new Pose();
        double up = env(t, 0.0, 0.35, 1.5, 2.0), open = env(t, 0.2, 0.45, 1.45, 1.8);
        double shake = open * 1.5 * sin(t * 80);
        double[] lift = {18, 12, 6, -4, -8};
        for (int i = 0; i < 5; i++) p.rot("neck" + (i + 1), lift[i] * up + shake * 0.4, 0, 0);
        p.rot("head", 22 * up + shake, 0, 0);
        p.rot("jaw", -42 * open, 0, 0);
        for (int c = 1; c <= 9; c++) p.rot("crown" + c, -10 * up, 0, 0);
        return p;
    }

    record Anim(String name, double length, boolean loop, java.util.function.DoubleFunction<Pose> f) {}

    static final List<Anim> ANIMS = List.of(
            new Anim("idle", 4.0, true, KaiselAnim::idle),
            new Anim("walk", 1.6, true, KaiselAnim::walk),
            new Anim("fly", 1.5, true, KaiselAnim::fly),
            new Anim("glide", 6.0, true, KaiselAnim::glide),
            new Anim("bite", 0.8, false, KaiselAnim::bite),
            new Anim("roar", 2.0, false, KaiselAnim::roar));

    static String f(double d) {
        if (Math.abs(d) < 0.005) return "0";
        String s = String.format(Locale.ROOT, "%.2f", d).replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }

    public static void main(String[] a) throws Exception {
        if (a[0].equals("pose")) {
            Anim an = ANIMS.stream().filter(x -> x.name.equals(a[1])).findFirst().orElseThrow();
            Pose p = an.f.apply(Double.parseDouble(a[2]));
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, double[]> e : p.m.entrySet()) {
                double[] v = e.getValue();
                sb.append(i++ > 0 ? ", " : "").append("\"").append(e.getKey()).append("\": [").append(f(v[0])).append(", ").append(f(v[1])).append(", ")
                        .append(f(v[2])).append(", ").append(f(v[3])).append(", ").append(f(v[4])).append(", ").append(f(v[5])).append("]");
            }
            Files.write(new File(a[3]).toPath(), sb.append("}").toString().getBytes(StandardCharsets.UTF_8));
            return;
        }
        StringBuilder sb = new StringBuilder("{\n  \"format_version\": \"1.8.0\",\n  \"animations\": {\n");
        for (int k = 0; k < ANIMS.size(); k++) {
            Anim an = ANIMS.get(k);
            int frames = (int) Math.round(an.length * 20);
            List<Pose> poses = new ArrayList<>();
            for (int i = 0; i <= frames; i++) poses.add(an.f.apply(Math.min(an.length, i / 20.0)));
            Set<String> bones = new LinkedHashSet<>();
            for (Pose p : poses) bones.addAll(p.m.keySet());
            sb.append("    \"animation.kaisel.").append(an.name).append("\": {\n");
            if (an.loop) sb.append("      \"loop\": true,\n");
            sb.append("      \"animation_length\": ").append(f(an.length)).append(",\n      \"bones\": {\n");
            int bi = 0;
            for (String bone : bones) {
                sb.append("        \"").append(bone).append("\": {");
                boolean moves = false;
                for (Pose p : poses) { double[] v = p.m.get(bone); if (v != null && (Math.abs(v[3]) + Math.abs(v[4]) + Math.abs(v[5]) > 1e-4)) moves = true; }
                for (int kind = 0; kind < (moves ? 2 : 1); kind++) {
                    sb.append(kind == 0 ? "\"rotation\": {" : ", \"position\": {");
                    for (int i = 0; i <= frames; i++) {
                        double[] v = poses.get(i).m.getOrDefault(bone, new double[6]);
                        int o = kind * 3;
                        sb.append(i > 0 ? ", " : "").append("\"").append(f(Math.min(an.length, i / 20.0))).append("\": [")
                                .append(f(v[o])).append(", ").append(f(v[o + 1])).append(", ").append(f(v[o + 2])).append("]");
                    }
                    sb.append("}");
                }
                sb.append("}").append(++bi < bones.size() ? ",\n" : "\n");
            }
            sb.append("      }\n    }").append(k < ANIMS.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  }\n}\n");
        Files.write(new File(a[0]).toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("animations: " + ANIMS.size());
    }
}
