import re
import os

with open(r'C:\Users\soybe\Desktop\Mods NeoForge\genshin_nature_workspace\src\main\java\net\mcreator\genshinnature\entity\renderer\StonehidelawachurlRenderer.java', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

constructor = re.search(r'public Modelstonehide_lawachurl\(\)\s*\{(.*?)\n\t\t\}', text, re.DOTALL).group(1)

parts = {}

for line in constructor.splitlines():
    line = line.strip()
    m = re.match(r'(\w+)\s*=\s*new ModelRenderer\(this\);', line)
    if m:
        parts[m.group(1)] = {'pivot': (0.0, 0.0, 0.0), 'rot': (0.0, 0.0, 0.0), 'parent': None, 'children': [], 'boxes': []}
        continue
    m = re.match(r'(\w+)\.setRotationPoint\(([^,]+),\s*([^,]+),\s*([^)]+)\);', line)
    if m:
        parts[m.group(1)]['pivot'] = (float(m.group(2).replace('F','')), float(m.group(3).replace('F','')), float(m.group(4).replace('F','')))
        continue
    m = re.match(r'setRotationAngle\((\w+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\);', line)
    if m:
        parts[m.group(1)]['rot'] = (float(m.group(2).replace('F','')), float(m.group(3).replace('F','')), float(m.group(4).replace('F','')))
        continue
    m = re.match(r'(\w+)\.addChild\((\w+)\);', line)
    if m:
        parent, child = m.group(1), m.group(2)
        parts[child]['parent'] = parent
        parts[parent]['children'].append(child)
        continue
    m = re.match(r'(\w+)\.setTextureOffset\((\d+),\s*(\d+)\)\.addBox\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*(\w+)\);', line)
    if m:
        parts[m.group(1)]['boxes'].append((int(m.group(2)), int(m.group(3)),
            float(m.group(4).replace('F','')), float(m.group(5).replace('F','')), float(m.group(6).replace('F','')),
            float(m.group(7).replace('F','')), float(m.group(8).replace('F','')), float(m.group(9).replace('F','')),
            float(m.group(10).replace('F','')), m.group(11) == 'true'))

code = []
code.append('package com.soybe.awakeningsystem.entity.client;')
code.append('')
code.append('import com.mojang.blaze3d.vertex.PoseStack;')
code.append('import com.mojang.blaze3d.vertex.VertexConsumer;')
code.append('import com.soybe.awakeningsystem.entity.StonehideLawachurlEntity;')
code.append('import net.minecraft.client.model.EntityModel;')
code.append('import net.minecraft.client.model.geom.ModelPart;')
code.append('import net.minecraft.client.model.geom.PartPose;')
code.append('import net.minecraft.client.model.geom.builders.*;')
code.append('import net.minecraft.util.Mth;')
code.append('')
code.append('public class StonehideLawachurlModel extends EntityModel<StonehideLawachurlEntity> {')
code.append('')
code.append('    private final ModelPart root;')
code.append('    private final ModelPart head;')
code.append('    private final ModelPart rightHand;')
code.append('    private final ModelPart leftHand;')
code.append('    private final ModelPart leftLeg;')
code.append('    private final ModelPart rightLeg;')
code.append('    private final ModelPart body;')
code.append('')
code.append('    public StonehideLawachurlModel(ModelPart root) {')
code.append('        this.root = root;')
code.append('        this.head = root.getChild("head");')
code.append('        this.rightHand = root.getChild("right_hand");')
code.append('        this.leftHand = root.getChild("left_hand");')
code.append('        this.leftLeg = root.getChild("left_leg");')
code.append('        this.rightLeg = root.getChild("right_leg");')
code.append('        this.body = root.getChild("body");')
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

code.append('        return LayerDefinition.create(mesh, 224, 224);')
code.append('    }')
code.append('')
code.append('    @Override')
code.append('    public void setupAnim(StonehideLawachurlEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {')
code.append('        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);')
code.append('        this.head.xRot = headPitch * ((float) Math.PI / 180F);')
code.append('')
code.append('        // Marcha de simio colosal')
code.append('        this.rightLeg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;')
code.append('        this.leftLeg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;')
code.append('        this.rightHand.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;')
code.append('        this.leftHand.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;')
code.append('')
code.append('        if (entity.isLeaping()) {')
code.append('            // Brazos alzados para el impacto sísmico hacia el suelo')
code.append('            this.rightHand.xRot = -2.2F;')
code.append('            this.leftHand.xRot = -2.2F;')
code.append('        }')
code.append('    }')
code.append('')
code.append('    @Override')
code.append('    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {')
code.append('        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);')
code.append('    }')
code.append('}')
code.append('')

output_path = r'src/main/java/com/soybe/awakeningsystem/entity/client/StonehideLawachurlModel.java'
with open(output_path, 'w', encoding='utf-8') as out:
    out.write('\n'.join(code))
print(f'Successfully generated {output_path} with {len(code)} lines.')
