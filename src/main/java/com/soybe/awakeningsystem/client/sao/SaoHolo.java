package com.soybe.awakeningsystem.client.sao;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

/**
 * Floating panels for the full-dive menu: the GUI is drawn through a perspective camera that sees the screen
 * plane exactly as the flat GUI would, and each panel can be turned on its own pivot so it hangs in the air in
 * front of the player, leaning toward them. Mouse positions are traced back onto a turned panel so clicks and
 * hovering still land where the eye sees them.
 *
 * <p>Depth is pinned to the flat GUI's z = 0 plane while drawing, so the turned panels layer exactly like
 * the flat ones did and nothing pokes through anything else.
 */
final class SaoHolo {
    private SaoHolo() {}

    private static final float FOV = 40.0F;
    private static Matrix4f saved;
    private static boolean active;
    private static float parX, parY;
    private static long lastNanos;

    /** Distance from the eye to the screen plane, in GUI units, for a screen {@code h} tall. */
    static float eye(int h) {
        return h * 0.5F / (float) Math.tan(Math.toRadians(FOV / 2.0F));
    }

    static void begin(GuiGraphics g, int w, int h) {
        if (active) return;
        g.flush();
        saved = new Matrix4f(RenderSystem.getProjectionMatrix());
        float d = eye(h);
        Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(FOV), w / (float) h, 1.0F, d * 4.0F)
                .scale(1.0F, -1.0F, 1.0F).translate(-w * 0.5F, -h * 0.5F, -d);
        // pin the depth: z in clip space = 0 for every vertex (the flat GUI's z = 0 plane)
        proj.m02(0.0F).m12(0.0F).m22(0.0F).m32(0.0F);
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.identity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(proj, VertexSorting.DISTANCE_TO_ORIGIN);
        active = true;
    }

    static void end(GuiGraphics g) {
        if (!active) return;
        g.flush();
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(saved, VertexSorting.ORTHOGRAPHIC_Z);
        active = false;
    }

    /** A panel turned by yaw / pitch (degrees) around the pivot (px, py) on the screen plane. */
    static Matrix4f plane(float px, float py, float yaw, float pitch) {
        return plane(px, py, 0.0F, yaw, pitch);
    }

    /** As above, pushed {@code depth} away from (negative) or toward the player. */
    static Matrix4f plane(float px, float py, float depth, float yaw, float pitch) {
        return new Matrix4f().translate(px, py, depth)
                .rotateY((float) Math.toRadians(yaw))
                .rotateX((float) Math.toRadians(pitch))
                .translate(-px, -py, 0.0F);
    }

    /** Draws what follows on {@code plane}; pair with {@link #pop}. */
    static void push(GuiGraphics g, Matrix4f plane) {
        g.pose().pushPose();
        if (!active) return;
        g.pose().last().pose().mul(plane);
        g.pose().scale(1.0F, 1.0F, 0.001F);          // GUI layers keep their order but add no depth
    }

    static void pop(GuiGraphics g) {
        g.pose().popPose();
    }

    /** Where the mouse at (mx, my) falls on the panel, in the panel's own flat coordinates. */
    static float[] unproject(Matrix4f plane, double mx, double my, int w, int h) {
        Matrix4f inv = new Matrix4f(plane).invert();
        Vector4f eye = inv.transform(new Vector4f(w * 0.5F, h * 0.5F, eye(h), 1.0F));
        Vector4f pt = inv.transform(new Vector4f((float) mx, (float) my, 0.0F, 1.0F));
        float dx = pt.x - eye.x, dy = pt.y - eye.y, dz = pt.z - eye.z;
        if (Math.abs(dz) < 1.0E-6F) return new float[]{(float) mx, (float) my};
        float t = -eye.z / dz;
        return new float[]{eye.x + dx * t, eye.y + dy * t};
    }

    /** Where a point of the panel appears on the flat screen. */
    static float[] project(Matrix4f plane, float x, float y, int w, int h) {
        Vector4f p = plane.transform(new Vector4f(x, y, 0.0F, 1.0F));
        float d = eye(h), k = d / (d - p.z);
        return new float[]{w * 0.5F + (p.x - w * 0.5F) * k, h * 0.5F + (p.y - h * 0.5F) * k};
    }

    /** The mouse's place on the screen from -1 to 1, eased so the panels drift after it. */
    static float[] parallax(double mx, double my, int w, int h) {
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0.016F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        float tx = Mth.clamp((float) (mx / w * 2.0 - 1.0), -1.0F, 1.0F);
        float ty = Mth.clamp((float) (my / h * 2.0 - 1.0), -1.0F, 1.0F);
        float k = 1.0F - (float) Math.exp(-dt * 5.0F);
        parX += (tx - parX) * k;
        parY += (ty - parY) * k;
        return new float[]{parX, parY};
    }
}
