import re
import os

with open(r'C:\Users\soybe\Desktop\Mods NeoForge\genshin_nature_workspace\src\main\java\net\mcreator\genshinnature\entity\renderer\CryoregisvineRenderer.java', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

constructor = re.search(r'public Modelcryo_regisvine\(\)\s*\{(.*?)\n\t\t\}', text, re.DOTALL).group(1)

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
code.append('import com.soybe.awakeningsystem.entity.CryoRegisvineEntity;')
code.append('import net.minecraft.client.model.EntityModel;')
code.append('import net.minecraft.client.model.geom.ModelPart;')
code.append('import net.minecraft.client.model.geom.PartPose;')
code.append('import net.minecraft.client.model.geom.builders.*;')
code.append('import net.minecraft.util.Mth;')
code.append('')
code.append('public class CryoRegisvineModel extends EntityModel<CryoRegisvineEntity> {')
code.append('')
code.append('    private final ModelPart root;')
code.append('    private final ModelPart bone;')
code.append('    private final ModelPart head;')
code.append('    private final ModelPart leftarm;')
code.append('    private final ModelPart rightarm;')
code.append('')
code.append('    public CryoRegisvineModel(ModelPart root) {')
code.append('        this.root = root;')
code.append('        this.bone = root.getChild("bone");')
code.append('        this.head = root.getChild("head");')
code.append('        this.leftarm = root.getChild("leftarm");')
code.append('        this.rightarm = root.getChild("rightarm");')
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

code.append('        return LayerDefinition.create(mesh, 128, 128);')
code.append('    }')
code.append('')
code.append('    @Override')
code.append('    public void setupAnim(CryoRegisvineEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {')
code.append('        // Movimiento base de balanceo de planta viva')
code.append('        float breathe = Mth.sin(ageInTicks * 0.05F) * 0.04F;')
code.append('        this.bone.zRot = breathe;')
code.append('')
code.append('        if (entity.isStunned()) {')
code.append('            // Estado noqueado: la cabeza yace desplomada en el suelo')
code.append('            this.head.xRot = 0.95F;')
code.append('            this.head.yRot = 0.0F;')
code.append('            this.head.zRot = 0.0F;')
code.append('            this.leftarm.xRot = 0.6F;')
code.append('            this.leftarm.zRot = 0.4F;')
code.append('            this.rightarm.xRot = -2.4F;')
code.append('            this.rightarm.zRot = 2.4F;')
code.append('        } else if (entity.isSlamming()) {')
code.append('            // Animación de cabezazo sísmico al suelo')
code.append('            float progress = entity.getSlamProgress(ageInTicks);')
code.append('            float slamAngle = Mth.sin(progress * (float) Math.PI) * 1.1F;')
code.append('            this.head.xRot = slamAngle;')
code.append('            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.5F;')
code.append('            this.leftarm.xRot = slamAngle * 0.6F;')
code.append('            this.rightarm.xRot = -2.9138F + slamAngle * 0.6F;')
code.append('        } else if (entity.isSpinning()) {')
code.append('            // Giro devastador en 360 grados de látigos y ramas')
code.append('            float spin = ageInTicks * 0.45F;')
code.append('            this.head.yRot = spin;')
code.append('            this.head.xRot = 0.2F;')
code.append('            this.leftarm.yRot = spin;')
code.append('            this.rightarm.yRot = spin;')
code.append('        } else {')
code.append('            // Seguimiento del objetivo con la cabeza corola')
code.append('            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);')
code.append('            this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.6F;')
code.append('')
code.append('            // Ondulación natural de látigos de sarmiento')
code.append('            float armWave = Mth.cos(ageInTicks * 0.08F) * 0.12F;')
code.append('            this.leftarm.xRot = armWave;')
code.append('            this.rightarm.xRot = -2.9138F - armWave;')
code.append('        }')
code.append('    }')
code.append('')
code.append('    @Override')
code.append('    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {')
code.append('        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);')
code.append('    }')
code.append('}')
code.append('')

output_path = r'src/main/java/com/soybe/awakeningsystem/entity/client/CryoRegisvineModel.java'
os.makedirs(os.path.dirname(output_path), exist_ok=True)
with open(output_path, 'w', encoding='utf-8') as out:
    out.write('\n'.join(code))
print(f'Successfully generated {output_path} with {len(code)} lines.')
