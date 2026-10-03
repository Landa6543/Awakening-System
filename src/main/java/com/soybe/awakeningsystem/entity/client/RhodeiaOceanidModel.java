package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RhodeiaOceanidBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RhodeiaOceanidModel extends EntityModel<RhodeiaOceanidBossEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftarm;
    private final ModelPart rightarm;
    private final ModelPart bone;

    public RhodeiaOceanidModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftarm = root.getChild("leftarm");
        this.rightarm = root.getChild("rightarm");
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(0, 0).addBox(-2.0F, -12.0F, -4.01F, 4.0F, 3.0F, 8.0F)
                        .texOffs(52, 6).addBox(-1.0F, -13.0F, -2.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(58, 0).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, 1.5F, -0.7418F, 0.0F, 0.0F));

        head.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(43, 0).addBox(1.0F, -9.5F, 0.0F, 1.0F, 4.0F, 1.0F)
                        .texOffs(28, 0).addBox(-1.0F, -5.5F, -2.0F, 3.0F, 4.0F, 4.0F)
                        .texOffs(0, 0).addBox(-3.0F, -1.5F, -4.01F, 5.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(3.0F, -8.5F, 0.0F, 0.0F, 0.0F, 0.829F));

        head.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(43, 0).addBox(-2.0F, -9.5F, 0.0F, 1.0F, 4.0F, 1.0F)
                        .texOffs(28, 0).addBox(-2.0F, -5.5F, -2.0F, 3.0F, 4.0F, 4.0F)
                        .texOffs(0, 0).addBox(-2.0F, -1.5F, -4.01F, 5.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(-3.0F, -8.5F, 0.0F, 0.0F, 0.0F, -0.829F));

        PartDefinition leftarm = root.addOrReplaceChild("leftarm",
                CubeListBuilder.create()
                        .texOffs(28, 43).addBox(-14.0F, -3.0F, -2.0F, 15.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 0.5F, 0.0F, 0.1745F, -0.5672F));

        leftarm.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(40, 51).addBox(-9.5F, -2.0F, -1.5F, 9.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-13.5F, -1.0F, -0.5F, 0.3819F, -0.4891F, -0.7073F));

        leftarm.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                        .texOffs(40, 51).addBox(-9.5F, -2.0F, -1.5F, 9.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-13.5F, -1.0F, -0.5F, 0.2075F, 0.5788F, 0.1056F));

        leftarm.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                        .texOffs(40, 51).addBox(-9.5F, -2.0F, -1.5F, 9.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-13.5F, -1.0F, -0.5F, 0.0F, 0.0F, -0.2618F));

        PartDefinition rightarm = root.addOrReplaceChild("rightarm",
                CubeListBuilder.create().mirror()
                        .texOffs(28, 43).addBox(-1.0F, -3.0F, -2.0F, 15.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(2.0F, -2.0F, 0.5F, 0.0F, -0.1745F, 0.5672F));

        rightarm.addOrReplaceChild("cube_r7",
                CubeListBuilder.create().mirror()
                        .texOffs(40, 51).addBox(0.5F, -2.0F, -1.5F, 9.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(13.5F, -1.0F, -0.5F, 0.3298F, 0.4703F, 0.6469F));

        rightarm.addOrReplaceChild("cube_r8",
                CubeListBuilder.create().mirror()
                        .texOffs(40, 51).addBox(0.5F, -2.0F, -1.5F, 9.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(13.5F, -1.0F, -0.5F, 0.1447F, -0.6855F, 0.0356F));

        rightarm.addOrReplaceChild("cube_r9",
                CubeListBuilder.create().mirror()
                        .texOffs(40, 51).addBox(0.5F, -2.0F, -1.5F, 9.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(13.5F, -1.0F, -0.5F, 0.0F, 0.0F, 0.2618F));

        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(44, 11).addBox(-2.5F, -33.0F, -2.5F, 5.0F, 26.0F, 5.0F)
                        .texOffs(35, 29).addBox(-1.5F, -31.5F, -3.5F, 3.0F, 5.0F, 1.0F)
                        .texOffs(36, 35).addBox(-1.0F, -26.5F, -3.5F, 2.0F, 1.0F, 1.0F)
                        .texOffs(37, 35).addBox(-0.5F, -25.5F, -3.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(34, 30).addBox(1.5F, -32.0F, -3.5F, 1.0F, 3.0F, 1.0F)
                        .texOffs(34, 30).addBox(2.5F, -31.5F, -3.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(34, 30).addBox(3.5F, -31.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                        .texOffs(34, 30).addBox(-2.5F, -32.0F, -3.5F, 1.0F, 3.0F, 1.0F)
                        .texOffs(34, 30).addBox(-3.5F, -31.5F, -3.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(34, 30).addBox(-4.5F, -31.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bone.addOrReplaceChild("cube_r10",
                CubeListBuilder.create()
                        .texOffs(52, 58).addBox(3.5F, 7.5F, -2.5F, 1.0F, 1.0F, 5.0F)
                        .texOffs(0, 57).addBox(2.5F, 5.5F, -2.5F, 2.0F, 2.0F, 5.0F)
                        .texOffs(16, 53).addBox(1.5F, -0.5F, -2.5F, 3.0F, 6.0F, 5.0F)
                        .texOffs(16, 53).addBox(-0.5F, -1.5F, -2.5F, 2.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(1.0F, -6.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        bone.addOrReplaceChild("cube_r11",
                CubeListBuilder.create()
                        .texOffs(52, 58).addBox(-4.5F, 7.5F, -2.5F, 1.0F, 1.0F, 5.0F)
                        .texOffs(0, 57).addBox(-4.5F, 5.5F, -2.5F, 2.0F, 2.0F, 5.0F)
                        .texOffs(16, 53).addBox(-4.5F, -0.5F, -2.5F, 3.0F, 6.0F, 5.0F)
                        .texOffs(16, 53).addBox(-1.5F, -1.5F, -2.5F, 2.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(-1.0F, -6.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(RhodeiaOceanidBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float wingFlap = Mth.sin(ageInTicks * 0.15F) * 0.25F;
        this.leftarm.zRot = -0.5672F + wingFlap;
        this.rightarm.zRot = 0.5672F - wingFlap;

        this.bone.yRot = Mth.sin(ageInTicks * 0.05F) * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
