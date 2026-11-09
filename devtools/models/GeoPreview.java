import com.google.gson.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;

/**
 * Software render of a GeckoLib geo.json + texture in its rest pose, to check the UVs: a front view, a back
 * view and a three-quarter view side by side. args: geo.json texture.png out.png
 */
public class GeoPreview {
    record Bone(String name, String parent, double[] pivot, double[] rot) {}

    static Map<String, Bone> bones = new HashMap<>();

    public static void main(String[] a) throws Exception {
        JsonObject g = JsonParser.parseString(Files.readString(Path.of(a[0]))).getAsJsonObject()
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        BufferedImage tex = ImageIO.read(new File(a[1]));
        int tw = g.getAsJsonObject("description").get("texture_width").getAsInt(), th = g.getAsJsonObject("description").get("texture_height").getAsInt();
        List<double[][]> quads = new ArrayList<>();   // 4 corners xyz + 4 uv
        for (JsonElement be : g.getAsJsonArray("bones")) {
            JsonObject b = be.getAsJsonObject();
            bones.put(b.get("name").getAsString(), new Bone(b.get("name").getAsString(), b.has("parent") ? b.get("parent").getAsString() : null,
                    d3(b, "pivot"), d3(b, "rotation")));
        }
        for (JsonElement be : g.getAsJsonArray("bones")) {
            JsonObject b = be.getAsJsonObject();
            if (!b.has("cubes")) continue;
            for (JsonElement ce : b.getAsJsonArray("cubes")) {
                JsonObject c = ce.getAsJsonObject();
                double[] o = d3(c, "origin"), s = d3(c, "size"), cp = d3(c, "pivot"), cr = d3(c, "rotation");
                double inf = c.has("inflate") ? c.get("inflate").getAsDouble() : 0;
                double x0 = o[0] - inf, y0 = o[1] - inf, z0 = o[2] - inf, x1 = o[0] + s[0] + inf, y1 = o[1] + s[1] + inf, z1 = o[2] + s[2] + inf;
                JsonObject uv = c.getAsJsonObject("uv");
                // faces in bedrock space (north = -z)
                Map<String, double[][]> f = new LinkedHashMap<>();
                f.put("north", new double[][]{{x1, y1, z0}, {x0, y1, z0}, {x0, y0, z0}, {x1, y0, z0}});
                f.put("south", new double[][]{{x0, y1, z1}, {x1, y1, z1}, {x1, y0, z1}, {x0, y0, z1}});
                f.put("east", new double[][]{{x1, y1, z1}, {x1, y1, z0}, {x1, y0, z0}, {x1, y0, z1}});
                f.put("west", new double[][]{{x0, y1, z0}, {x0, y1, z1}, {x0, y0, z1}, {x0, y0, z0}});
                f.put("up", new double[][]{{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}});
                f.put("down", new double[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}});
                for (Map.Entry<String, double[][]> fe : f.entrySet()) {
                    if (!uv.has(fe.getKey())) continue;
                    JsonObject fu = uv.getAsJsonObject(fe.getKey());
                    double u = fu.getAsJsonArray("uv").get(0).getAsDouble(), v = fu.getAsJsonArray("uv").get(1).getAsDouble();
                    double us = fu.getAsJsonArray("uv_size").get(0).getAsDouble(), vs = fu.getAsJsonArray("uv_size").get(1).getAsDouble();
                    double[][] q = new double[8][];
                    for (int i = 0; i < 4; i++) {
                        double[] p = fe.getValue()[i].clone();
                        p = rotate(p, cp, cr);
                        p = toModel(p, b.get("name").getAsString());
                        q[i] = p;
                    }
                    // GeckoLib's mapping: corner 0 = (u, v), 1 = (u+us, v), 2 = (u+us, v+vs), 3 = (u, v+vs)
                    q[4] = new double[]{u, v}; q[5] = new double[]{u + us, v}; q[6] = new double[]{u + us, v + vs}; q[7] = new double[]{u, v + vs};
                    quads.add(q);
                }
            }
        }
        int view = 300, scale = 3;
        BufferedImage out = new BufferedImage(view * 3, view, BufferedImage.TYPE_INT_RGB);
        for (int px = 0; px < out.getWidth(); px++) for (int py = 0; py < out.getHeight(); py++) out.setRGB(px, py, 0x404050);
        double[] yaws = {180, 0, 215};
        for (int vi = 0; vi < 3; vi++) {
            double[] zbuf = new double[view * view];
            Arrays.fill(zbuf, -1e9);
            double yaw = Math.toRadians(yaws[vi]);
            for (double[][] q : quads) {
                double[][] sp = new double[4][];
                for (int i = 0; i < 4; i++) {
                    double x = q[i][0] * Math.cos(yaw) - q[i][2] * Math.sin(yaw), z = q[i][0] * Math.sin(yaw) + q[i][2] * Math.cos(yaw);
                    sp[i] = new double[]{view / 2.0 + x * scale, view - 10 - q[i][1] * scale, z};
                }
                for (int[] tri : new int[][]{{0, 1, 2}, {0, 2, 3}}) raster(out, vi * view, view, zbuf, sp, q, tri, tex, tw, th);
            }
        }
        ImageIO.write(out, "png", new File(a[2]));
    }

    static void raster(BufferedImage out, int offX, int view, double[] zbuf, double[][] sp, double[][] q, int[] t, BufferedImage tex, int tw, int th) {
        double[] A = sp[t[0]], B = sp[t[1]], C = sp[t[2]];
        double area = (B[0] - A[0]) * (C[1] - A[1]) - (B[1] - A[1]) * (C[0] - A[0]);
        if (Math.abs(area) < 1e-9) return;
        int minX = (int) Math.max(0, Math.floor(Math.min(A[0], Math.min(B[0], C[0])))), maxX = (int) Math.min(view - 1, Math.ceil(Math.max(A[0], Math.max(B[0], C[0]))));
        int minY = (int) Math.max(0, Math.floor(Math.min(A[1], Math.min(B[1], C[1])))), maxY = (int) Math.min(view - 1, Math.ceil(Math.max(A[1], Math.max(B[1], C[1]))));
        for (int y = minY; y <= maxY; y++) for (int x = minX; x <= maxX; x++) {
            double px = x + 0.5, py = y + 0.5;
            double w0 = ((B[0] - px) * (C[1] - py) - (B[1] - py) * (C[0] - px)) / area;
            double w1 = ((C[0] - px) * (A[1] - py) - (C[1] - py) * (A[0] - px)) / area;
            double w2 = 1 - w0 - w1;
            if (w0 < 0 || w1 < 0 || w2 < 0) continue;
            double z = w0 * A[2] + w1 * B[2] + w2 * C[2];
            if (z <= zbuf[y * view + x]) continue;
            double u = w0 * q[4 + t[0]][0] + w1 * q[4 + t[1]][0] + w2 * q[4 + t[2]][0];
            double v = w0 * q[4 + t[0]][1] + w1 * q[4 + t[1]][1] + w2 * q[4 + t[2]][1];
            int tx = (int) Math.floor(u * tex.getWidth() / tw), ty = (int) Math.floor(v * tex.getHeight() / th);
            if (tx < 0 || ty < 0 || tx >= tex.getWidth() || ty >= tex.getHeight()) continue;
            int argb = tex.getRGB(tx, ty);
            if ((argb >>> 24) < 128) continue;
            zbuf[y * view + x] = z;
            out.setRGB(offX + x, y, argb & 0xFFFFFF);
        }
    }

    static double[] rotate(double[] p, double[] pivot, double[] r) {
        double x = p[0] - pivot[0], y = p[1] - pivot[1], z = p[2] - pivot[2];
        double rx = Math.toRadians(r[0]), ry = Math.toRadians(r[1]), rz = Math.toRadians(r[2]);
        // GeckoLib applies Z, then Y, then X (as matrix multiplications), so the point sees X first
        double y1 = y * Math.cos(rx) - z * Math.sin(rx), z1 = y * Math.sin(rx) + z * Math.cos(rx); y = y1; z = z1;
        double x2 = x * Math.cos(ry) + z * Math.sin(ry), z2 = -x * Math.sin(ry) + z * Math.cos(ry); x = x2; z = z2;
        double x3 = x * Math.cos(rz) - y * Math.sin(rz), y3 = x * Math.sin(rz) + y * Math.cos(rz); x = x3; y = y3;
        return new double[]{x + pivot[0], y + pivot[1], z + pivot[2]};
    }

    static double[] toModel(double[] p, String bone) {
        for (Bone b = bones.get(bone); b != null; b = b.parent == null ? null : bones.get(b.parent)) p = rotate(p, b.pivot, b.rot);
        return p;
    }

    static double[] d3(JsonObject o, String k) {
        if (!o.has(k)) return new double[3];
        JsonArray a = o.getAsJsonArray(k);
        return new double[]{a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble()};
    }
}
