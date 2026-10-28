import json

with open(r'C:\Users\soybe\Desktop\Mods NeoForge\maquina\Casino addon by groberpc RP\models\blocks\casino4.geo.json') as f:
    d = json.load(f)

cubes = d['minecraft:geometry'][0]['bones'][1]['cubes'] # body cubes
lever_cubes = d['minecraft:geometry'][0]['bones'][2]['cubes'] # bone4

out_lines = []
out_lines.append('''package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

/**
 * Renderizador de alta fidelidad para el chasis de la máquina tragamonedas (casino4.geo.json).
 * Mapeo UV exacto sobre textura 128x128 con visera inclinada a -45° y marco de pantalla a -44°.
 */
public class Casino4CabinetRenderer {

    public static void renderCabinet(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
''')

def emit_cube(c, idx):
    res = []
    ox, oy, oz = c['origin']
    sx, sy, sz = c['size']
    x0, y0, z0 = ox, oy, oz
    x1, y1, z1 = ox + sx, oy + sy, oz + sz
    
    rot = c.get('rotation')
    piv = c.get('pivot')
    
    indent = '        '
    if rot and piv:
        res.append(f'{indent}poseStack.pushPose();')
        res.append(f'{indent}poseStack.translate({piv[0]:.3f}F, {piv[1]:.3f}F, {piv[2]:.3f}F);')
        res.append(f'{indent}poseStack.mulPose(Axis.ZP.rotationDegrees({rot[2]:.1f}F));')
        res.append(f'{indent}poseStack.translate({-piv[0]:.3f}F, {-piv[1]:.3f}F, {-piv[2]:.3f}F);')
        
    res.append(f'{indent}Matrix4f mat{idx} = poseStack.last().pose();')
    
    for face_name, uvd in c.get('uv', {}).items():
        u, v = uvd['uv']
        uw, vh = uvd['uv_size']
        u_rot = uvd.get('uv_rotation', 0)
        
        u0 = u / 128.0
        v0 = v / 128.0
        u1 = (u + uw) / 128.0
        v1 = (v + vh) / 128.0
        
        res.append(f'{indent}drawFace(mat{idx}, consumer, "{face_name}", {x0:.3f}F, {y0:.3f}F, {z0:.3f}F, {x1:.3f}F, {y1:.3f}F, {z1:.3f}F, {u0:.5f}F, {v0:.5f}F, {u1:.5f}F, {v1:.5f}F, {u_rot}, light, overlay);')
        
    if rot and piv:
        res.append(f'{indent}poseStack.popPose();')
        
    return '\n'.join(res)

for i, c in enumerate(cubes):
    out_lines.append(f'        // --- Cube {i} ---')
    out_lines.append(emit_cube(c, i))
    out_lines.append('')

out_lines.append('''    }

    public static void renderLever(PoseStack poseStack, VertexConsumer consumer, float leverAngle, int light, int overlay) {
        poseStack.pushPose();
        // Pivot de la palanca según casino4.geo.json bone4: [-1.25, 8.25, 8.25]
        poseStack.translate(-1.25F, 8.25F, 8.25F);
        // Inclinar la palanca hacia adelante en rotación con leverAngle
        poseStack.mulPose(Axis.ZP.rotationDegrees(-leverAngle));
        poseStack.translate(1.25F, -8.25F, -8.25F);

        // Brazo de la palanca (Arm) con su rotación base de -45°
        poseStack.pushPose();
        poseStack.translate(-1.0F, 7.5F, 8.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
        poseStack.translate(1.0F, -7.5F, -8.5F);
        Matrix4f armMat = poseStack.last().pose();
        // Arm cube: origin [-1.25, 10.5, 7.5], size [1, 3, 1]
        drawBox(armMat, consumer, -1.25F, 10.5F, 7.5F, -0.25F, 13.5F, 8.5F, 21.0F/128.0F, 2.0F/128.0F, 23.0F/128.0F, 8.0F/128.0F, light, overlay);
        // Knob cube: origin [-1.5, 13.5, 7.25], size [1.5, 1.5, 1.5]
        drawBox(armMat, consumer, -1.5F, 13.5F, 7.25F, 0.0F, 15.0F, 8.75F, 107.0F/128.0F, 18.0F/128.0F, 110.0F/128.0F, 24.0F/128.0F, light, overlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void drawBox(Matrix4f mat, VertexConsumer consumer,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float u0, float v0, float u1, float v1,
                                int light, int overlay) {
        drawFace(mat, consumer, "north", x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, 0, light, overlay);
        drawFace(mat, consumer, "south", x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, 0, light, overlay);
        drawFace(mat, consumer, "west",  x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, 0, light, overlay);
        drawFace(mat, consumer, "east",  x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, 0, light, overlay);
        drawFace(mat, consumer, "up",    x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, 0, light, overlay);
        drawFace(mat, consumer, "down",  x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, 0, light, overlay);
    }

    private static void drawFace(Matrix4f mat, VertexConsumer consumer,
                                 String face, float x0, float y0, float z0, float x1, float y1, float z1,
                                 float u0, float v0, float u1, float v1, int uvRot,
                                 int light, int overlay) {
        float minU = Math.min(u0, u1);
        float maxU = Math.max(u0, u1);
        float minV = Math.min(v0, v1);
        float maxV = Math.max(v0, v1);

        float uvA_u = minU, uvA_v = maxV;
        float uvB_u = maxU, uvB_v = maxV;
        float uvC_u = maxU, uvC_v = minV;
        float uvD_u = minU, uvD_v = minV;

        if (uvRot == 90) {
            float t_u = uvA_u, t_v = uvA_v;
            uvA_u = uvD_u; uvA_v = uvD_v;
            uvD_u = uvC_u; uvD_v = uvC_v;
            uvC_u = uvB_u; uvC_v = uvB_v;
            uvB_u = t_u;   uvB_v = t_v;
        }

        switch (face) {
            case "north" -> {
                // Facing -Z
                vertex(mat, consumer, x1, y0, z0, uvB_u, uvB_v, 0, 0, -1, light, overlay);
                vertex(mat, consumer, x0, y0, z0, uvA_u, uvA_v, 0, 0, -1, light, overlay);
                vertex(mat, consumer, x0, y1, z0, uvD_u, uvD_v, 0, 0, -1, light, overlay);
                vertex(mat, consumer, x1, y1, z0, uvC_u, uvC_v, 0, 0, -1, light, overlay);
            }
            case "south" -> {
                // Facing +Z
                vertex(mat, consumer, x0, y0, z1, uvA_u, uvA_v, 0, 0, 1, light, overlay);
                vertex(mat, consumer, x1, y0, z1, uvB_u, uvB_v, 0, 0, 1, light, overlay);
                vertex(mat, consumer, x1, y1, z1, uvC_u, uvC_v, 0, 0, 1, light, overlay);
                vertex(mat, consumer, x0, y1, z1, uvD_u, uvD_v, 0, 0, 1, light, overlay);
            }
            case "west" -> {
                // Facing -X
                vertex(mat, consumer, x0, y0, z0, uvA_u, uvA_v, -1, 0, 0, light, overlay);
                vertex(mat, consumer, x0, y0, z1, uvB_u, uvB_v, -1, 0, 0, light, overlay);
                vertex(mat, consumer, x0, y1, z1, uvC_u, uvC_v, -1, 0, 0, light, overlay);
                vertex(mat, consumer, x0, y1, z0, uvD_u, uvD_v, -1, 0, 0, light, overlay);
            }
            case "east" -> {
                // Facing +X
                vertex(mat, consumer, x1, y0, z1, uvB_u, uvB_v, 1, 0, 0, light, overlay);
                vertex(mat, consumer, x1, y0, z0, uvA_u, uvA_v, 1, 0, 0, light, overlay);
                vertex(mat, consumer, x1, y1, z0, uvD_u, uvD_v, 1, 0, 0, light, overlay);
                vertex(mat, consumer, x1, y1, z1, uvC_u, uvC_v, 1, 0, 0, light, overlay);
            }
            case "up" -> {
                // Facing +Y
                vertex(mat, consumer, x0, y1, z0, uvD_u, uvD_v, 0, 1, 0, light, overlay);
                vertex(mat, consumer, x0, y1, z1, uvA_u, uvA_v, 0, 1, 0, light, overlay);
                vertex(mat, consumer, x1, y1, z1, uvB_u, uvB_v, 0, 1, 0, light, overlay);
                vertex(mat, consumer, x1, y1, z0, uvC_u, uvC_v, 0, 1, 0, light, overlay);
            }
            case "down" -> {
                // Facing -Y
                vertex(mat, consumer, x0, y0, z1, uvA_u, uvA_v, 0, -1, 0, light, overlay);
                vertex(mat, consumer, x0, y0, z0, uvD_u, uvD_v, 0, -1, 0, light, overlay);
                vertex(mat, consumer, x1, y0, z0, uvC_u, uvC_v, 0, -1, 0, light, overlay);
                vertex(mat, consumer, x1, y0, z1, uvB_u, uvB_v, 0, -1, 0, light, overlay);
            }
        }
    }

    private static void vertex(Matrix4f mat, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int light, int overlay) {
        consumer.addVertex(mat, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }
}
''')

with open(r'src/main/java/com/soybe/awakeningsystem/client/renderer/Casino4CabinetRenderer.java', 'w', encoding='utf-8') as out:
    out.write('\n'.join(out_lines))

print('Generated Casino4CabinetRenderer.java successfully!')
