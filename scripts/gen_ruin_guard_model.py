import re
import os

with open(r'C:\Users\soybe\Desktop\Mods NeoForge\genshin_nature_workspace\src\main\java\net\mcreator\genshinnature\entity\renderer\RuinguardRenderer.java', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

constructor = re.search(r'public Modelruin_guard\(\)\s*\{(.*?)\n\t\t\}', text, re.DOTALL).group(1)

parts = {}

for line in constructor.splitlines():
    line = line.strip()
    m = re.match(r'(\w+)\s*=\s*new ModelRenderer\(this\);', line)
    if m:
        name = m.group(1)
        parts[name] = {'pivot': (0.0, 0.0, 0.0), 'rot': (0.0, 0.0, 0.0), 'parent': None, 'children': [], 'boxes': []}
        continue
    m = re.match(r'(\w+)\.setRotationPoint\(([^,]+),\s*([^,]+),\s*([^)]+)\);', line)
    if m:
        name = m.group(1)
        x = float(m.group(2).replace('F', ''))
        y = float(m.group(3).replace('F', ''))
        z = float(m.group(4).replace('F', ''))
        parts[name]['pivot'] = (x, y, z)
        continue
    m = re.match(r'setRotationAngle\((\w+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\);', line)
    if m:
        name = m.group(1)
        rx = float(m.group(2).replace('F', ''))
        ry = float(m.group(3).replace('F', ''))
        rz = float(m.group(4).replace('F', ''))
        parts[name]['rot'] = (rx, ry, rz)
        continue
    m = re.match(r'(\w+)\.addChild\((\w+)\);', line)
    if m:
        parent = m.group(1)
        child = m.group(2)
        parts[child]['parent'] = parent
        parts[parent]['children'].append(child)
        continue
    m = re.match(r'(\w+)\.setTextureOffset\((\d+),\s*(\d+)\)\.addBox\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*(\w+)\);', line)
    if m:
        name = m.group(1)
        u, v = int(m.group(2)), int(m.group(3))
        x = float(m.group(4).replace('F', ''))
        y = float(m.group(5).replace('F', ''))
        z = float(m.group(6).replace('F', ''))
        dx = float(m.group(7).replace('F', ''))
        dy = float(m.group(8).replace('F', ''))
        dz = float(m.group(9).replace('F', ''))
        delta = float(m.group(10).replace('F', ''))
        mirror = m.group(11) == 'true'
        parts[name]['boxes'].append((u, v, x, y, z, dx, dy, dz, delta, mirror))
        continue

code = []
code.append('package com.soybe.awakeningsystem.entity.client;')
code.append('')
code.append('import com.mojang.blaze3d.vertex.PoseStack;')
code.append('import com.mojang.blaze3d.vertex.VertexConsumer;')
code.append('import com.soybe.awakeningsystem.entity.RuinGuardEntity;')
code.append('import net.minecraft.client.model.EntityModel;')
code.append('import net.minecraft.client.model.geom.ModelPart;')
code.append('import net.minecraft.client.model.geom.PartPose;')
code.append('import net.minecraft.client.model.geom.builders.*;')
code.append('import net.minecraft.util.Mth;')
code.append('')
code.append('public class RuinGuardModel extends EntityModel<RuinGuardEntity> {')
code.append('')
code.append('    private final ModelPart root;')
code.append('    private final ModelPart ruinGuard;')
code.append('    private final ModelPart leftLeg;')
code.append('    private final ModelPart rightLeg;')
code.append('    private final ModelPart leftArm;')
code.append('    private final ModelPart rightArm;')
code.append('')
code.append('    public RuinGuardModel(ModelPart root) {')
code.append('        this.root = root;')
code.append('        this.ruinGuard = root.getChild("RuinGuard");')
code.append('        this.leftLeg = root.getChild("left_leg_ANIM");')
code.append('        this.rightLeg = root.getChild("right_leg_ANIM");')
code.append('        this.leftArm = root.getChild("left_arm_ANIM");')
code.append('        this.rightArm = root.getChild("right_arm_ANIM");')
code.append('    }')
code.append('')
code.append('    public static LayerDefinition createBodyLayer() {')
code.append('        MeshDefinition mesh = new MeshDefinition();')
code.append('        PartDefinition root = mesh.getRoot();')
code.append('')

def generate_part_code(p_name, parent_var_name):
    p = parts[p_name]
    var_name = f'part_{p_name}'
    builder_str = 'CubeListBuilder.create()'
    for b in p['boxes']:
        u, v, x, y, z, dx, dy, dz, delta, mirror = b
        if delta != 0.0:
            builder_str += f'\n                .texOffs({u}, {v}).mirror({str(mirror).lower()}).addBox({x}F, {y}F, {z}F, {dx}F, {dy}F, {dz}F, new CubeDeformation({delta}F))'
        else:
            builder_str += f'\n                .texOffs({u}, {v}).mirror({str(mirror).lower()}).addBox({x}F, {y}F, {z}F, {dx}F, {dy}F, {dz}F)'
    
    px, py, pz = p['pivot']
    rx, ry, rz = p['rot']
    if rx != 0.0 or ry != 0.0 or rz != 0.0:
        pose_str = f'PartPose.offsetAndRotation({px}F, {py}F, {pz}F, {rx}F, {ry}F, {rz}F)'
    else:
        pose_str = f'PartPose.offset({px}F, {py}F, {pz}F)'
    
    code.append(f'        PartDefinition {var_name} = {parent_var_name}.addOrReplaceChild("{p_name}",')
    code.append(f'                {builder_str},')
    code.append(f'                {pose_str});')
    code.append('')
    for child in p['children']:
        generate_part_code(child, var_name)

root_names = [k for k, v in parts.items() if v['parent'] is None]
for r_name in root_names:
    generate_part_code(r_name, 'root')

code.append('        return LayerDefinition.create(mesh, 256, 256);')
code.append('    }')
code.append('')
code.append('    @Override')
code.append('    public void setupAnim(RuinGuardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {')
code.append('        // Animación de marcha pesada del autómata')
code.append('        this.leftLeg.yRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;')
code.append('        this.rightLeg.yRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;')
code.append('        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;')
code.append('        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;')
code.append('')
code.append('        // Ataques especiales: Giro en torbellino')
code.append('        if (entity.isSpinning()) {')
code.append('            float spin = ageInTicks * 0.8F;')
code.append('            this.ruinGuard.yRot = spin;')
code.append('            this.leftArm.xRot = -1.5708F; // Brazos horizontales extendidos')
code.append('            this.rightArm.xRot = -1.5708F;')
code.append('        } else if (entity.isStunned()) {')
code.append('            // Núcleo ocular dañado: cabeza gacha apagada')
code.append('            this.ruinGuard.yRot = 0.0F;')
code.append('            this.ruinGuard.xRot = 0.35F;')
code.append('            this.leftArm.xRot = 0.5F;')
code.append('            this.rightArm.xRot = 0.5F;')
code.append('        } else {')
code.append('            this.ruinGuard.xRot = 0.0F;')
code.append('            this.ruinGuard.yRot = 0.0F;')
code.append('            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;')
code.append('            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;')
code.append('        }')
code.append('    }')
code.append('')
code.append('    @Override')
code.append('    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {')
code.append('        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);')
code.append('    }')
code.append('}')
code.append('')

output_path = r'src/main/java/com/soybe/awakeningsystem/entity/client/RuinGuardModel.java'
os.makedirs(os.path.dirname(output_path), exist_ok=True)
with open(output_path, 'w', encoding='utf-8') as out:
    out.write('\n'.join(code))
print(f'Successfully generated {output_path} with {len(code)} lines.')
