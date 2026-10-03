package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.GreatSnowboarKingBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class GreatSnowboarKingModel extends EntityModel<GreatSnowboarKingBossEntity> {

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public GreatSnowboarKingModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.head = this.bone.getChild("head");
        this.leg0 = this.bone.getChild("leg0");
        this.leg1 = this.bone.getChild("leg1");
        this.leg2 = this.bone.getChild("leg2");
        this.leg3 = this.bone.getChild("leg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bone.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(84, 24).addBox(-15.0F, -30.0F, -21.0F, 30.0F, 48.0F, 24.0F),
                PartPose.offsetAndRotation(0.0F, -39.0F, 6.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition head = bone.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12.0F, -12.0F, -24.0F, 24.0F, 24.0F, 24.0F)
                        .texOffs(48, 48).addBox(-6.0F, 0.0F, -27.0F, 12.0F, 9.0F, 3.0F),
                PartPose.offset(0.0F, -36.0F, -18.0F));

        head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(63, 73).addBox(-1.5F, -3.0F, -7.5F, 3.0F, 3.0F, 6.0F)
                        .texOffs(62, 73).addBox(-1.5F, 0.0F, -4.5F, 3.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(10.5F, 3.0F, -25.5F, 0.0F, -0.1309F, 0.0F));

        head.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(63, 70).addBox(-1.5F, -3.0F, -7.5F, 3.0F, 3.0F, 6.0F)
                        .texOffs(63, 71).addBox(-1.5F, 0.0F, -4.5F, 3.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(-10.5F, 3.0F, -25.5F, 0.0F, 0.1309F, 0.0F));

        bone.addOrReplaceChild("leg0",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 18.0F, 12.0F),
                PartPose.offset(-9.0F, -18.0F, 21.0F));

        bone.addOrReplaceChild("leg1",
                CubeListBuilder.create().mirror()
                        .texOffs(0, 48).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 18.0F, 12.0F),
                PartPose.offset(9.0F, -18.0F, 21.0F));

        bone.addOrReplaceChild("leg2",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 18.0F, 12.0F),
                PartPose.offset(-9.0F, -18.0F, -15.0F));

        bone.addOrReplaceChild("leg3",
                CubeListBuilder.create().mirror()
                        .texOffs(0, 48).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 18.0F, 12.0F),
                PartPose.offset(9.0F, -18.0F, -15.0F));

        return LayerDefinition.create(mesh, 192, 96);
    }

    @Override
    public void setupAnim(GreatSnowboarKingBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        if (entity.isCharging()) {
            this.head.xRot = 0.35F;
            float chargeAnim = ageInTicks * 1.5F;
            this.leg0.xRot = Mth.cos(chargeAnim) * 1.2F;
            this.leg1.xRot = Mth.cos(chargeAnim + (float) Math.PI) * 1.2F;
            this.leg2.xRot = Mth.cos(chargeAnim + (float) Math.PI) * 1.2F;
            this.leg3.xRot = Mth.cos(chargeAnim) * 1.2F;
        } else {
            this.leg0.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
            this.leg1.xRot = Mth.cos(limbSwing * 1.0F) * limbSwingAmount;
            this.leg2.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
            this.leg3.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
