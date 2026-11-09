import com.google.gson.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/**
 * Converts a Blockbench .bbmodel (cubes, any number of textures) into GeckoLib files:
 * geo/NAME.geo.json, animations/NAME.animation.json and textures/entity/NAME.png (all textures packed
 * into one atlas, since a GeckoLib model has one texture).
 * args: model.bbmodel assetsDir name [once=anim1,anim2] (animations to play once even if Blockbench loops them)
 * Follows Blockbench's own Bedrock export: x mirrored, x/y rotations negated, up/down face UVs flipped.
 */
public class BbToGeo {
    public static void main(String[] a) throws Exception {
        JsonObject m = JsonParser.parseString(Files.readString(Path.of(a[0]))).getAsJsonObject();
        Path assets = Path.of(a[1]);
        String name = a[2];
        Set<String> once = new HashSet<>();
        if (a.length > 3 && a[3].startsWith("once=")) once.addAll(Arrays.asList(a[3].substring(5).split(",")));

        // ---- textures: decode and pack into a power-of-two atlas, shelf by shelf, biggest first
        JsonArray texs = m.getAsJsonArray("textures");
        int n = texs.size();
        BufferedImage[] img = new BufferedImage[n];
        double[] su = new double[n], sv = new double[n];   // uv units -> pixels
        for (int i = 0; i < n; i++) {
            JsonObject t = texs.get(i).getAsJsonObject();
            String src = t.get("source").getAsString();
            img[i] = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(src.substring(src.indexOf(',') + 1))));
            double uw = t.has("uv_width") ? t.get("uv_width").getAsDouble() : m.getAsJsonObject("resolution").get("width").getAsDouble();
            double uh = t.has("uv_height") ? t.get("uv_height").getAsDouble() : m.getAsJsonObject("resolution").get("height").getAsDouble();
            su[i] = img[i].getWidth() / uw;
            sv[i] = img[i].getHeight() / uh;
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (x, y) -> img[y].getHeight() - img[x].getHeight());
        int atlasW = 1;
        int maxW = 0, total = 0;
        for (BufferedImage b : img) { maxW = Math.max(maxW, b.getWidth()); total += b.getWidth() * b.getHeight(); }
        while (atlasW < maxW || atlasW * atlasW < total) atlasW *= 2;
        int[] ox = new int[n], oy = new int[n];
        int x = 0, y = 0, shelf = 0;
        for (int i : order) {
            if (x + img[i].getWidth() > atlasW) { x = 0; y += shelf; shelf = 0; }
            ox[i] = x; oy[i] = y;
            x += img[i].getWidth();
            shelf = Math.max(shelf, img[i].getHeight());
        }
        int atlasH = 1;
        while (atlasH < y + shelf) atlasH *= 2;
        // textures Blockbench draws "additive" are glow layers laid over other cubes: they go into NAME_glow.png
        // (to be drawn additively over the model, as Blockbench does) and stay empty in the base texture
        BufferedImage atlas = new BufferedImage(atlasW, atlasH, BufferedImage.TYPE_INT_ARGB);
        BufferedImage glow = new BufferedImage(atlasW, atlasH, BufferedImage.TYPE_INT_ARGB);
        boolean anyGlow = false;
        for (int i = 0; i < n; i++) {
            JsonObject t = texs.get(i).getAsJsonObject();
            boolean additive = t.has("render_mode") && t.get("render_mode").getAsString().equals("additive");
            (additive ? glow : atlas).getGraphics().drawImage(img[i], ox[i], oy[i], null);
            anyGlow |= additive;
        }
        File texOut = assets.resolve("textures/entity/" + name + ".png").toFile();
        texOut.getParentFile().mkdirs();
        ImageIO.write(atlas, "png", texOut);
        if (anyGlow) ImageIO.write(glow, "png", assets.resolve("textures/entity/" + name + "_glow.png").toFile());

        // ---- bones from the outliner
        Map<String, JsonObject> groups = new HashMap<>();
        if (m.has("groups")) for (JsonElement g : m.getAsJsonArray("groups")) groups.put(g.getAsJsonObject().get("uuid").getAsString(), g.getAsJsonObject());
        Map<String, JsonObject> elements = new HashMap<>();
        for (JsonElement e : m.getAsJsonArray("elements")) elements.put(e.getAsJsonObject().get("uuid").getAsString(), e.getAsJsonObject());
        JsonArray bones = new JsonArray();
        Set<String> used = new HashSet<>();
        Map<String, String> boneNameByUuid = new HashMap<>();
        int[] stats = new int[2];
        for (JsonElement node : m.getAsJsonArray("outliner")) walk(node, null, groups, elements, bones, used, boneNameByUuid, ox, oy, su, sv, stats);

        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", "geometry." + name);
        desc.addProperty("texture_width", atlasW);
        desc.addProperty("texture_height", atlasH);
        desc.addProperty("visible_bounds_width", 8);
        desc.addProperty("visible_bounds_height", 8);
        JsonArray vbo = new JsonArray(); vbo.add(0); vbo.add(3); vbo.add(0);
        desc.add("visible_bounds_offset", vbo);
        JsonObject geo = new JsonObject();
        geo.add("description", desc);
        geo.add("bones", bones);
        JsonArray geos = new JsonArray(); geos.add(geo);
        JsonObject geoRoot = new JsonObject();
        geoRoot.addProperty("format_version", "1.12.0");
        geoRoot.add("minecraft:geometry", geos);
        Path geoOut = assets.resolve("geo/" + name + ".geo.json");
        Files.createDirectories(geoOut.getParent());
        Files.writeString(geoOut, new GsonBuilder().create().toJson(geoRoot));

        // ---- animations
        JsonObject anims = new JsonObject();
        if (m.has("animations")) for (JsonElement ae : m.getAsJsonArray("animations")) {
            JsonObject an = ae.getAsJsonObject();
            JsonObject out = new JsonObject();
            String loop = an.has("loop") ? an.get("loop").getAsString() : "once";
            if (once.contains(an.get("name").getAsString())) loop = "once";   // attacks: play once, then let the AI decide
            if (loop.equals("loop")) out.addProperty("loop", true);
            else if (loop.equals("hold")) out.addProperty("loop", "hold_on_last_frame");
            double len = an.has("length") ? an.get("length").getAsDouble() : 0;
            out.addProperty("animation_length", Math.max(len, 0.05));
            JsonObject boneAnims = new JsonObject();
            if (an.has("animators")) for (Map.Entry<String, JsonElement> ent : an.getAsJsonObject("animators").entrySet()) {
                JsonObject animator = ent.getValue().getAsJsonObject();
                if (!animator.has("keyframes")) continue;
                String bone = boneNameByUuid.getOrDefault(ent.getKey(), animator.has("name") ? animator.get("name").getAsString() : null);
                if (bone == null || !used.contains(bone)) continue;
                // GeckoLib needs each channel's keyframes in time order (Blockbench keeps them in the order they
                // were made); a later frame at 0 makes it throw the whole animation away
                Map<String, TreeMap<Double, JsonObject>> sorted = new LinkedHashMap<>();
                for (JsonElement ke : animator.getAsJsonArray("keyframes")) {
                    JsonObject kf = ke.getAsJsonObject();
                    String ch = kf.get("channel").getAsString();
                    if (!ch.equals("rotation") && !ch.equals("position") && !ch.equals("scale")) continue;
                    TreeMap<Double, JsonObject> chan = sorted.computeIfAbsent(ch, k -> new TreeMap<>());
                    JsonArray dps = kf.getAsJsonArray("data_points");
                    JsonObject frame = new JsonObject();
                    String interp = kf.has("interpolation") ? kf.get("interpolation").getAsString() : "linear";
                    // GeckoLib flips x and y of each bone's rest rotation when it loads the model, but adds the
                    // animation's rotations as they are; Blockbench subtracts them. So x and y rotations change sign,
                    // and x of positions too (GeckoLib moves bones along x the other way from Blockbench).
                    int flip = ch.equals("rotation") ? 2 : ch.equals("position") ? 1 : 0;
                    if (dps.size() > 1) {
                        frame.add("pre", vec(dps.get(0).getAsJsonObject(), flip));
                        frame.add("post", vec(dps.get(1).getAsJsonObject(), flip));
                    } else if (interp.equals("catmullrom")) {
                        // GeckoLib only reads the lerp mode next to "vector"; with "post" it rejects the whole animation
                        frame.add("vector", vec(dps.get(0).getAsJsonObject(), flip));
                        frame.addProperty("lerp_mode", "catmullrom");
                    } else {
                        frame.add("post", vec(dps.get(0).getAsJsonObject(), flip));
                    }
                    chan.put(Math.round(kf.get("time").getAsDouble() * 10000) / 10000.0, frame);
                }
                JsonObject channels = new JsonObject();
                for (Map.Entry<String, TreeMap<Double, JsonObject>> ch : sorted.entrySet()) {
                    JsonObject chan = new JsonObject();
                    for (Map.Entry<Double, JsonObject> f : ch.getValue().entrySet()) chan.add(time(f.getKey()), f.getValue());
                    channels.add(ch.getKey(), chan);
                }
                boneAnims.add(bone, channels);
            }
            out.add("bones", boneAnims);
            anims.add(an.get("name").getAsString(), out);
        }
        JsonObject animRoot = new JsonObject();
        animRoot.addProperty("format_version", "1.8.0");
        animRoot.add("animations", anims);
        Path animOut = assets.resolve("animations/" + name + ".animation.json");
        Files.createDirectories(animOut.getParent());
        Files.writeString(animOut, new GsonBuilder().create().toJson(animRoot));
        System.out.println("bones " + bones.size() + ", cubes " + stats[0] + " (skipped " + stats[1] + "), atlas " + atlasW + "x" + atlasH
                + (anyGlow ? " + additive glow texture" : "")
                + ", animations " + anims.keySet());
    }

    static String time(double t) {
        return String.valueOf(Math.round(t * 10000) / 10000.0);
    }

    /** A keyframe value: numbers stay numbers, Molang stays a string; the first {@code flipAxes} axes change sign. */
    static JsonArray vec(JsonObject dp, int flipAxes) {
        JsonArray arr = new JsonArray();
        String[] axes = {"x", "y", "z"};
        for (int i = 0; i < 3; i++) {
            String s = dp.has(axes[i]) ? dp.get(axes[i]).getAsString().trim() : "0";
            if (s.isEmpty()) s = "0";
            boolean flip = i < flipAxes;
            try {
                double d = Double.parseDouble(s);
                arr.add(flip && d != 0 ? -d : d);
            } catch (NumberFormatException e) {
                arr.add(flip ? "-(" + s + ")" : s);
            }
        }
        return arr;
    }

    static JsonArray arr(double... v) {
        JsonArray a = new JsonArray();
        for (double d : v) a.add(Math.round(d * 100000) / 100000.0);
        return a;
    }

    static double[] d3(JsonObject o, String key) {
        if (!o.has(key)) return new double[3];
        JsonArray a = o.getAsJsonArray(key);
        return new double[]{a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble()};
    }

    static void walk(JsonElement node, String parent, Map<String, JsonObject> groups, Map<String, JsonObject> elements, JsonArray bones,
                     Set<String> used, Map<String, String> names, int[] ox, int[] oy, double[] su, double[] sv, int[] stats) {
        if (!node.isJsonObject()) return;   // a loose cube at the root: no bone to hold it
        JsonObject o = node.getAsJsonObject();
        String uuid = o.get("uuid").getAsString();
        JsonObject g = groups.containsKey(uuid) ? groups.get(uuid) : o;
        String name = g.get("name").getAsString();
        String unique = name;
        for (int i = 2; used.contains(unique); i++) unique = name + "_" + i;
        used.add(unique);
        names.put(uuid, unique);
        double[] origin = d3(g, "origin"), rot = d3(g, "rotation");
        JsonObject bone = new JsonObject();
        bone.addProperty("name", unique);
        if (parent != null) bone.addProperty("parent", parent);
        bone.add("pivot", arr(-origin[0], origin[1], origin[2]));
        if (rot[0] != 0 || rot[1] != 0 || rot[2] != 0) bone.add("rotation", arr(-rot[0], -rot[1], rot[2]));
        JsonArray cubes = new JsonArray();
        List<JsonElement> sub = new ArrayList<>();
        for (JsonElement c : o.getAsJsonArray("children")) {
            if (c.isJsonPrimitive()) {
                JsonObject e = elements.get(c.getAsString());
                if (e == null || !"cube".equals(e.has("type") ? e.get("type").getAsString() : "cube") || e.has("export") && !e.get("export").getAsBoolean()) {
                    stats[1]++;
                    continue;
                }
                cubes.add(cube(e, ox, oy, su, sv));
                stats[0]++;
            } else sub.add(c);
        }
        if (!cubes.isEmpty()) bone.add("cubes", cubes);
        bones.add(bone);
        for (JsonElement c : sub) walk(c, unique, groups, elements, bones, used, names, ox, oy, su, sv, stats);
    }

    static JsonObject cube(JsonObject e, int[] ox, int[] oy, double[] su, double[] sv) {
        double[] from = d3(e, "from"), to = d3(e, "to"), origin = d3(e, "origin"), rot = d3(e, "rotation");
        JsonObject c = new JsonObject();
        c.add("origin", arr(-to[0], from[1], from[2]));
        c.add("size", arr(to[0] - from[0], to[1] - from[1], to[2] - from[2]));
        if (e.has("inflate") && e.get("inflate").getAsDouble() != 0) c.addProperty("inflate", e.get("inflate").getAsDouble());
        if (rot[0] != 0 || rot[1] != 0 || rot[2] != 0) {
            c.add("pivot", arr(-origin[0], origin[1], origin[2]));
            c.add("rotation", arr(-rot[0], -rot[1], rot[2]));
        }
        JsonObject uv = new JsonObject();
        JsonObject faces = e.getAsJsonObject("faces");
        for (String dir : new String[]{"north", "east", "south", "west", "up", "down"}) {
            if (!faces.has(dir)) continue;
            JsonObject f = faces.getAsJsonObject(dir);
            if (!f.has("texture") || f.get("texture").isJsonNull()) continue;
            int t = f.get("texture").getAsInt();
            JsonArray u = f.getAsJsonArray("uv");
            double u0 = u.get(0).getAsDouble() * su[t] + ox[t], v0 = u.get(1).getAsDouble() * sv[t] + oy[t];
            double u1 = u.get(2).getAsDouble() * su[t] + ox[t], v1 = u.get(3).getAsDouble() * sv[t] + oy[t];
            JsonObject face = new JsonObject();
            if (dir.equals("up") || dir.equals("down")) {
                face.add("uv", arr(u1, v1));
                face.add("uv_size", arr(u0 - u1, v0 - v1));
            } else {
                face.add("uv", arr(u0, v0));
                face.add("uv_size", arr(u1 - u0, v1 - v0));
            }
            uv.add(dir, face);
        }
        c.add("uv", uv);
        return c;
    }
}
