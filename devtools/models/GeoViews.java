import com.google.gson.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Renders a GeckoLib geo.json + texture in its rest pose from several angles (side, front, top, three-quarter,
 * three-quarter from below), fitted to the picture, with simple light so the shapes read. Uses GeoPreview's
 * bone maths. args: geo.json texture.png out.png [cellSize]
 */
public class GeoViews {
    public static void main(String[] a) throws Exception {
        JsonObject g = JsonParser.parseString(Files.readString(Path.of(a[0]))).getAsJsonObject()
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        BufferedImage tex = ImageIO.read(new File(a[1]));
        int tw = g.getAsJsonObject("description").get("texture_width").getAsInt(), th = g.getAsJsonObject("description").get("texture_height").getAsInt();
        int cell = a.length > 3 ? Integer.parseInt(a[3]) : 420;
        // optional pose: {"bone": [rx, ry, rz, px, py, pz], ...} added to the rest pose (an animation frame)
        JsonObject pose = a.length > 4 ? JsonParser.parseString(Files.readString(Path.of(a[4]))).getAsJsonObject() : new JsonObject();
        for (JsonElement be : g.getAsJsonArray("bones")) {
            JsonObject b = be.getAsJsonObject();
            String name = b.get("name").getAsString();
            double[] rot = GeoPreview.d3(b, "rotation"), piv = GeoPreview.d3(b, "pivot");
            if (pose.has(name)) {
                JsonArray p = pose.getAsJsonArray(name);
                for (int i = 0; i < 3; i++) rot[i] += p.get(i).getAsDouble();
                for (int i = 0; i < 3; i++) OFFSET.put(name + i, p.get(3 + i).getAsDouble());
            }
            GeoPreview.bones.put(name, new GeoPreview.Bone(name, b.has("parent") ? b.get("parent").getAsString() : null, piv, rot));
        }
        List<double[][]> quads = new ArrayList<>();
        for (JsonElement be : g.getAsJsonArray("bones")) {
            JsonObject b = be.getAsJsonObject();
            if (!b.has("cubes")) continue;
            for (JsonElement ce : b.getAsJsonArray("cubes")) {
                JsonObject c = ce.getAsJsonObject();
                double[] o = GeoPreview.d3(c, "origin"), s = GeoPreview.d3(c, "size"), cp = GeoPreview.d3(c, "pivot"), cr = GeoPreview.d3(c, "rotation");
                double x0 = o[0], y0 = o[1], z0 = o[2], x1 = o[0] + s[0], y1 = o[1] + s[1], z1 = o[2] + s[2];
                JsonObject uv = c.getAsJsonObject("uv");
                Map<String, double[][]> f = new LinkedHashMap<>();
                f.put("north", new double[][]{{x1, y1, z0}, {x0, y1, z0}, {x0, y0, z0}, {x1, y0, z0}});
                f.put("south", new double[][]{{x0, y1, z1}, {x1, y1, z1}, {x1, y0, z1}, {x0, y0, z1}});
                f.put("east", new double[][]{{x1, y1, z1}, {x1, y1, z0}, {x1, y0, z0}, {x1, y0, z1}});
                f.put("west", new double[][]{{x0, y1, z0}, {x0, y1, z1}, {x0, y0, z1}, {x0, y0, z0}});
                f.put("up", new double[][]{{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}});
                f.put("down", new double[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}});
                for (Map.Entry<String, double[][]> fe : f.entrySet()) {
                    if (uv == null || !uv.has(fe.getKey())) continue;
                    JsonObject fu = uv.getAsJsonObject(fe.getKey());
                    double u = fu.getAsJsonArray("uv").get(0).getAsDouble(), v = fu.getAsJsonArray("uv").get(1).getAsDouble();
                    double us = fu.getAsJsonArray("uv_size").get(0).getAsDouble(), vs = fu.getAsJsonArray("uv_size").get(1).getAsDouble();
                    double[][] q = new double[8][];
                    for (int i = 0; i < 4; i++) q[i] = toModel(GeoPreview.rotate(fe.getValue()[i].clone(), cp, cr), b.get("name").getAsString());
                    q[4] = new double[]{u, v}; q[5] = new double[]{u + us, v}; q[6] = new double[]{u + us, v + vs}; q[7] = new double[]{u, v + vs};
                    quads.add(q);
                }
            }
        }
        // views: yaw, pitch (degrees), label
        Object[][] views = {{90.0, 0.0, "side"}, {180.0, 0.0, "front"}, {180.0, 89.0, "top"}, {215.0, 18.0, "3/4"}, {140.0, -25.0, "below"}, {0.0, 8.0, "back"}};
        int cols = 3, rows = 2;
        BufferedImage out = new BufferedImage(cell * cols, cell * rows, BufferedImage.TYPE_INT_RGB);
        Graphics2D gg = out.createGraphics();
        gg.setColor(new Color(0x5A6E8C)); gg.fillRect(0, 0, out.getWidth(), out.getHeight());
        for (int vi = 0; vi < views.length; vi++) {
            double yaw = Math.toRadians((Double) views[vi][0]), pitch = Math.toRadians((Double) views[vi][1]);
            // project all corners to find the fit
            List<double[][]> proj = new ArrayList<>();
            double minX = 1e9, maxX = -1e9, minY = 1e9, maxY = -1e9;
            for (double[][] q : quads) {
                double[][] p = new double[4][];
                for (int i = 0; i < 4; i++) {
                    double x = q[i][0] * Math.cos(yaw) - q[i][2] * Math.sin(yaw), z = q[i][0] * Math.sin(yaw) + q[i][2] * Math.cos(yaw), y = q[i][1];
                    double y2 = y * Math.cos(pitch) - z * Math.sin(pitch), z2 = y * Math.sin(pitch) + z * Math.cos(pitch);
                    p[i] = new double[]{x, y2, z2};
                    minX = Math.min(minX, x); maxX = Math.max(maxX, x); minY = Math.min(minY, y2); maxY = Math.max(maxY, y2);
                }
                proj.add(p);
            }
            double sc = (cell - 30) / Math.max(maxX - minX, maxY - minY);
            int ox = (vi % cols) * cell, oy = (vi / cols) * cell;
            BufferedImage view = new BufferedImage(cell, cell, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < cell; i++) for (int j = 0; j < cell; j++) view.setRGB(i, j, 0x5A6E8C);
            double[] zbuf = new double[cell * cell];
            Arrays.fill(zbuf, -1e9);
            for (int k = 0; k < quads.size(); k++) {
                double[][] p = proj.get(k), q = quads.get(k);
                double[][] sp = new double[4][];
                for (int i = 0; i < 4; i++) sp[i] = new double[]{cell / 2.0 + (p[i][0] - (minX + maxX) / 2) * sc, cell / 2.0 - (p[i][1] - (minY + maxY) / 2) * sc, p[i][2]};
                // light from the face normal (in model space): top bright, sides medium, bottom dark
                double[] e1 = sub(q[1], q[0]), e2 = sub(q[3], q[0]);
                double[] nrm = norm(cross(e1, e2));
                double light = 0.62 + 0.38 * Math.max(0, nrm[1]) + 0.12 * Math.abs(nrm[0]);
                for (int[] tri : new int[][]{{0, 1, 2}, {0, 2, 3}}) raster(view, cell, zbuf, sp, q, tri, tex, tw, th, light);
            }
            gg.drawImage(view, ox, oy, null);
            gg.setColor(Color.WHITE);
            gg.drawString((String) views[vi][2], ox + 6, oy + 14);
        }
        ImageIO.write(out, "png", new File(a[2]));
    }

    static final Map<String, Double> OFFSET = new HashMap<>();

    /** Like GeoPreview.toModel, but each bone also moves by its animated position offset before turning. */
    static double[] toModel(double[] p, String bone) {
        for (GeoPreview.Bone b = GeoPreview.bones.get(bone); b != null; b = b.parent() == null ? null : GeoPreview.bones.get(b.parent())) {
            p = GeoPreview.rotate(p, b.pivot(), b.rot());
            p = new double[]{p[0] - OFFSET.getOrDefault(b.name() + 0, 0.0), p[1] + OFFSET.getOrDefault(b.name() + 1, 0.0), p[2] + OFFSET.getOrDefault(b.name() + 2, 0.0)};
        }
        return p;
    }

    static double[] sub(double[] a, double[] b) { return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    static double[] cross(double[] a, double[] b) { return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }
    static double[] norm(double[] a) { double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]); return l < 1e-9 ? new double[]{0, 1, 0} : new double[]{a[0] / l, a[1] / l, a[2] / l}; }

    static void raster(BufferedImage out, int view, double[] zbuf, double[][] sp, double[][] q, int[] t, BufferedImage tex, int tw, int th, double light) {
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
            tx = Math.max(0, Math.min(tex.getWidth() - 1, tx)); ty = Math.max(0, Math.min(tex.getHeight() - 1, ty));
            int argb = tex.getRGB(tx, ty);
            if ((argb >>> 24) < 128) continue;
            zbuf[y * view + x] = z;
            int r = (int) Math.min(255, ((argb >> 16) & 255) * light), gr = (int) Math.min(255, ((argb >> 8) & 255) * light), bl = (int) Math.min(255, (argb & 255) * light);
            out.setRGB(x, y, (r << 16) | (gr << 8) | bl);
        }
    }
}
