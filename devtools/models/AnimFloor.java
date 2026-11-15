import com.google.gson.*;

import java.nio.file.*;
import java.util.*;

/**
 * For each animation of a GeckoLib model, the lowest point of the model (in model pixels; the ground is y = 0)
 * at every keyframe, so feet that sink through the floor show up as negative numbers. Numeric keyframes only.
 * args: geo.json animation.json
 */
public class AnimFloor {
    public static void main(String[] a) throws Exception {
        JsonObject g = JsonParser.parseString(Files.readString(Path.of(a[0]))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject anims = JsonParser.parseString(Files.readString(Path.of(a[1]))).getAsJsonObject().getAsJsonObject("animations");
        JsonArray bones = g.getAsJsonArray("bones");
        report("rest", bones, new JsonObject(), 0);
        for (Map.Entry<String, JsonElement> e : anims.entrySet()) {
            JsonObject an = e.getValue().getAsJsonObject();
            JsonObject ab = an.has("bones") ? an.getAsJsonObject("bones") : new JsonObject();
            TreeSet<Double> times = new TreeSet<>();
            for (Map.Entry<String, JsonElement> b : ab.entrySet())
                for (String ch : new String[]{"rotation", "position"}) {
                    JsonElement v = b.getValue().getAsJsonObject().get(ch);
                    if (v != null && v.isJsonObject()) for (String k : v.getAsJsonObject().keySet()) try { times.add(Double.parseDouble(k)); } catch (NumberFormatException ignored) {}
                }
            if (times.isEmpty()) times.add(0.0);
            double worst = 1e9, at = 0;
            for (double t : times) {
                double m = lowest(bones, ab, t);
                if (m < worst) { worst = m; at = t; }
            }
            System.out.printf(Locale.ROOT, "%-40s lowest %7.2f at t=%.2f%n", e.getKey(), worst, at);
        }
    }

    static void report(String name, JsonArray bones, JsonObject ab, double t) {
        System.out.printf(Locale.ROOT, "%-40s lowest %7.2f%n", name, lowest(bones, ab, t));
    }

    /** Value of a channel at time t: linear between numeric keyframes (or a constant array). */
    static double[] sample(JsonElement ch, double t) {
        if (ch == null) return new double[3];
        if (ch.isJsonArray()) return vec(ch);
        TreeMap<Double, double[]> keys = new TreeMap<>();
        for (Map.Entry<String, JsonElement> k : ch.getAsJsonObject().entrySet()) {
            JsonElement v = k.getValue();
            if (v.isJsonObject()) v = v.getAsJsonObject().has("post") ? v.getAsJsonObject().get("post") : v.getAsJsonObject().get("vector");
            if (v == null) continue;
            if (v.isJsonObject() && v.getAsJsonObject().has("vector")) v = v.getAsJsonObject().get("vector");
            try { keys.put(Double.parseDouble(k.getKey()), vec(v)); } catch (Exception ignored) {}
        }
        if (keys.isEmpty()) return new double[3];
        Map.Entry<Double, double[]> lo = keys.floorEntry(t), hi = keys.ceilingEntry(t);
        if (lo == null) return hi.getValue();
        if (hi == null || hi.getKey().equals(lo.getKey())) return lo.getValue();
        double f = (t - lo.getKey()) / (hi.getKey() - lo.getKey());
        double[] r = new double[3];
        for (int i = 0; i < 3; i++) r[i] = lo.getValue()[i] + f * (hi.getValue()[i] - lo.getValue()[i]);
        return r;
    }

    static double[] vec(JsonElement v) {
        double[] r = new double[3];
        if (v.isJsonArray()) for (int i = 0; i < 3 && i < v.getAsJsonArray().size(); i++) {
            JsonElement x = v.getAsJsonArray().get(i);
            try { r[i] = x.getAsDouble(); } catch (Exception ignored) {}
        }
        return r;
    }

    static double lowest(JsonArray bones, JsonObject ab, double t) {
        Map<String, double[][]> info = new HashMap<>();   // name -> {pivot, rot, pos}
        Map<String, String> parent = new HashMap<>();
        for (JsonElement be : bones) {
            JsonObject b = be.getAsJsonObject();
            String n = b.get("name").getAsString();
            double[] rot = GeoPreview.d3(b, "rotation"), pos = new double[3];
            if (ab.has(n)) {
                double[] r = sample(ab.getAsJsonObject(n).get("rotation"), t), p = sample(ab.getAsJsonObject(n).get("position"), t);
                for (int i = 0; i < 3; i++) { rot[i] += r[i]; pos[i] = p[i]; }
            }
            info.put(n, new double[][]{GeoPreview.d3(b, "pivot"), rot, pos});
            if (b.has("parent")) parent.put(n, b.get("parent").getAsString());
        }
        double min = 1e9;
        for (JsonElement be : bones) {
            JsonObject b = be.getAsJsonObject();
            if (!b.has("cubes")) continue;
            for (JsonElement ce : b.getAsJsonArray("cubes")) {
                JsonObject c = ce.getAsJsonObject();
                double[] o = GeoPreview.d3(c, "origin"), s = GeoPreview.d3(c, "size"), cp = GeoPreview.d3(c, "pivot"), cr = GeoPreview.d3(c, "rotation");
                for (int k = 0; k < 8; k++) {
                    double[] p = {o[0] + ((k & 1) != 0 ? s[0] : 0), o[1] + ((k & 2) != 0 ? s[1] : 0), o[2] + ((k & 4) != 0 ? s[2] : 0)};
                    p = GeoPreview.rotate(p, cp, cr);
                    for (String n = b.get("name").getAsString(); n != null; n = parent.get(n)) {
                        double[][] in = info.get(n);
                        p = GeoPreview.rotate(p, in[0], in[1]);
                        p = new double[]{p[0] - in[2][0], p[1] + in[2][1], p[2] + in[2][2]};
                    }
                    min = Math.min(min, p[1]);
                }
            }
        }
        return min;
    }
}
