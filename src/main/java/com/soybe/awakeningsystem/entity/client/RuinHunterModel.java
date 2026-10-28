package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinHunterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RuinHunterModel extends EntityModel<RuinHunterEntity> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart facePlate;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart ringBase;

    public RuinHunterModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.leftWing = this.body.getChild("bone3");
        this.rightWing = this.body.getChild("bone2");
        this.facePlate = this.body.getChild("bone");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.ringBase = this.body.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(70, 0).mirror(false).addBox(-7.0F, -21.0F, -2.0F, 14.0F, 15.0F, 15.0F)
                        .texOffs(0, 81).mirror(false).addBox(-17.0F, -47.0F, -5.0F, 34.0F, 26.0F, 21.0F)
                        .texOffs(13, 81).mirror(false).addBox(-4.0F, -49.0F, -5.0F, 9.0F, 2.0F, 21.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bone3 = body.addOrReplaceChild("bone3",
                CubeListBuilder.create()
                        .texOffs(130, 0).mirror(true).addBox(-2.9333F, -4.6786F, 0.1348F, 6.0F, 6.0F, 9.0F),
                PartPose.offsetAndRotation(-11.0667F, -39.3214F, 15.8652F, 1.0915F, 0.1128F, -0.7606F));

        bone3.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(98, 129).mirror(true).addBox(-2.0F, -1.3301F, -3.3397F, 4.0F, 4.0F, 27.0F),
                PartPose.offsetAndRotation(-0.0333F, -15.1786F, 30.6778F, 1.0472F, 0.0F, 0.0F));

        bone3.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(98, 50).mirror(true).addBox(-3.1F, -3.0F, -0.5F, 6.0F, 6.0F, 25.0F),
                PartPose.offsetAndRotation(0.0667F, -1.6786F, 8.6348F, 0.5236F, 0.0F, 0.0F));

        PartDefinition bone2 = body.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(130, 0).mirror(false).addBox(-3.0F, -4.6786F, -0.8652F, 6.0F, 6.0F, 9.0F),
                PartPose.offsetAndRotation(11.0F, -39.3214F, 16.8652F, 0.746F, 0.0964F, 0.6996F));

        bone2.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(98, 129).mirror(true).addBox(-2.0F, -1.3301F, -3.3397F, 4.0F, 4.0F, 27.0F),
                PartPose.offsetAndRotation(-0.1F, -15.1786F, 29.6778F, 1.0472F, 0.0F, 0.0F));

        bone2.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(98, 50).mirror(false).addBox(-2.9F, -3.0F, -0.5F, 6.0F, 6.0F, 25.0F),
                PartPose.offsetAndRotation(0.0F, -1.6786F, 7.6348F, 0.5236F, 0.0F, 0.0F));

        body.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(112, 34).mirror(false).addBox(-9.92F, -32.52F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(-7.28F, -29.88F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(29, 54).mirror(false).addBox(-8.08F, -43.64F, -6.16F, 16.0F, 14.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(-2.36F, -29.88F, -6.16F, 5.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(-9.92F, -43.08F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(-7.28F, -45.72F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(-2.36F, -45.72F, -6.16F, 5.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(-9.92F, -39.04F, -6.16F, 2.0F, 5.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(5.04F, -45.72F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(7.68F, -43.08F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(7.68F, -39.04F, -6.16F, 2.0F, 5.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(7.68F, -32.52F, -6.16F, 2.0F, 2.0F, 2.0F)
                        .texOffs(112, 34).mirror(false).addBox(5.04F, -29.88F, -6.16F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 26).mirror(true).addBox(-5.25F, -0.5F, -6.5F, 10.0F, 8.0F, 13.0F),
                PartPose.offset(-21.75F, -44.5F, 5.5F));

        left_arm.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                        .texOffs(0, 50).mirror(true).addBox(-2.4548F, 13.38F, -3.6936F, 6.0F, 6.0F, 6.0F)
                        .texOffs(89, 31).mirror(true).addBox(-2.11F, -0.62F, -2.8789F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(-3.2952F, 20.3036F, -3.3064F, -1.0413F, 0.056F, 0.211F));

        left_arm.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                        .texOffs(66, 31).mirror(true).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 16.0F, 5.0F),
                PartPose.offsetAndRotation(0.25F, 6.5F, 0.0F, -0.2559F, 0.056F, 0.211F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 26).mirror(false).addBox(-4.75F, -0.5F, -6.5F, 10.0F, 8.0F, 13.0F),
                PartPose.offset(21.75F, -44.5F, 5.5F));

        right_arm.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                        .texOffs(0, 50).mirror(false).addBox(-3.0452F, 13.38F, -3.6936F, 6.0F, 6.0F, 6.0F)
                        .texOffs(89, 31).mirror(true).addBox(-2.89F, -0.62F, -2.8789F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(3.2952F, 20.3036F, -3.3064F, -1.0413F, -0.056F, -0.211F));

        right_arm.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                        .texOffs(66, 31).mirror(true).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 16.0F, 5.0F),
                PartPose.offsetAndRotation(-0.25F, 6.5F, 0.0F, -0.2559F, -0.056F, -0.211F));

        PartDefinition bb_main = body.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(44, 0).mirror(false).addBox(-2.0F, -1.0F, -8.0F, 4.0F, 1.0F, 6.0F)
                        .texOffs(0, 4).mirror(true).addBox(-7.5F, -1.0F, -2.0F, 1.0F, 1.0F, 15.0F)
                        .texOffs(0, 4).mirror(false).addBox(6.5F, -1.0F, -2.0F, 1.0F, 1.0F, 15.0F)
                        .texOffs(0, 0).mirror(false).addBox(-6.5F, -1.0F, -2.0F, 13.0F, 1.0F, 1.0F)
                        .texOffs(25, 11).mirror(false).addBox(-6.5F, -0.5F, -1.0F, 13.0F, 0.0F, 13.0F)
                        .texOffs(25, 4).mirror(false).addBox(4.0F, -1.0F, -6.0F, 3.0F, 1.0F, 4.0F)
                        .texOffs(25, 4).mirror(false).addBox(-7.0F, -1.0F, -6.0F, 3.0F, 1.0F, 4.0F)
                        .texOffs(44, 26).mirror(false).addBox(7.5F, -2.0F, 3.0F, 2.0F, 3.0F, 8.0F)
                        .texOffs(44, 26).mirror(true).addBox(-9.5F, -2.0F, 3.0F, 2.0F, 3.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("cube_r9",
                CubeListBuilder.create()
                        .texOffs(0, 0).mirror(true).addBox(-6.5F, -0.5F, -0.5F, 13.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 12.5F, -3.1416F, 0.0F, 3.1416F));

        return LayerDefinition.create(mesh, 160, 160);
    }

    @Override
    public void setupAnim(RuinHunterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.xRot = 0.0F;
        this.body.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.4F;
        this.body.zRot = 0.0F;

        this.leftWing.xRot = 1.0915F;
        this.leftWing.yRot = 0.1128F;
        this.leftWing.zRot = -0.7606F;

        this.rightWing.xRot = 0.746F;
        this.rightWing.yRot = 0.0964F;
        this.rightWing.zRot = 0.6996F;

        this.leftArm.xRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.leftArm.zRot = 0.0F;

        this.rightArm.xRot = 0.0F;
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;

        if (entity.isStunned()) {
            this.body.y = 24.0F;
            this.body.xRot = 0.55F;
            this.leftArm.xRot = 0.4F;
            this.leftArm.zRot = 0.25F;
            this.rightArm.xRot = 0.4F;
            this.rightArm.zRot = -0.25F;
            this.leftWing.xRot = 0.6F;
            this.rightWing.xRot = 0.4F;
            return;
        }

        float hoverBob = Mth.sin(ageInTicks * 0.12F) * 2.0F;
        this.body.y = 22.0F + hoverBob;

        float wingPulse = Mth.sin(ageInTicks * 0.15F) * 0.06F;
        this.leftWing.zRot = -0.7606F + wingPulse;
        this.rightWing.zRot = 0.6996F - wingPulse;

        if (entity.isSpinning()) {
            float spinAngle = ageInTicks * 1.4F;
            this.leftArm.yRot = spinAngle;
            this.rightArm.yRot = spinAngle + (float) Math.PI;
            this.leftArm.xRot = -0.1F;
            this.rightArm.xRot = -0.1F;
            return;
        }

        if (entity.isBombarding()) {
            this.body.xRot = 0.25F + headPitch * ((float) Math.PI / 180F) * 0.3F;
            this.leftArm.xRot = -1.3F;
            this.leftArm.zRot = 0.4F;
            this.rightArm.xRot = -1.3F;
            this.rightArm.zRot = -0.4F;
            this.leftWing.xRot = 1.35F;
            this.rightWing.xRot = 1.05F;
            return;
        }

        if (entity.isDashing()) {
            this.body.xRot = 0.45F;
            this.leftArm.xRot = -1.0F;
            this.leftArm.yRot = 0.35F;
            this.rightArm.xRot = -1.0F;
            this.rightArm.yRot = -0.35F;
            return;
        }

        float bladeSway = Mth.cos(ageInTicks * 0.08F) * 0.12F;
        this.leftArm.xRot = bladeSway;
        this.rightArm.xRot = -bladeSway;
        this.leftArm.zRot = 0.05F;
        this.rightArm.zRot = -0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
