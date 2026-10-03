package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.PerpetualMechanicalArrayBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class PerpetualMechanicalArrayModel extends EntityModel<PerpetualMechanicalArrayBossEntity> {

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart insideCube;
    private final ModelPart outsideCubes;

    public PerpetualMechanicalArrayModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.insideCube = this.bone.getChild("inside_cube");
        this.outsideCubes = this.bone.getChild("outside_cubes");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 2.4702F, -0.5156F, -2.6325F));

        bone.addOrReplaceChild("inside_cube",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        bone.addOrReplaceChild("outside_cubes",
                CubeListBuilder.create()
                        .texOffs(88, 108).mirror().addBox(-11.0F, -20.0F, 1.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).addBox(1.0F, -20.0F, 1.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).mirror().addBox(-12.0F, -22.0F, -12.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).addBox(1.0F, -20.0F, -11.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).addBox(-11.0F, -8.0F, -11.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).mirror().addBox(1.0F, -8.0F, -11.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).addBox(-11.0F, -8.0F, 1.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(88, 108).mirror().addBox(2.0F, -6.0F, 2.0F, 10.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(PerpetualMechanicalArrayBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isParalyzed()) {
            this.bone.y = 18.0F;
            this.outsideCubes.xRot = 0.5F;
            this.outsideCubes.zRot = 0.5F;
        } else {
            this.bone.y = 10.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;
            this.bone.yRot = ageInTicks * 0.05F;
            this.insideCube.yRot = -ageInTicks * 0.08F;
            this.outsideCubes.yRot = ageInTicks * 0.04F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
