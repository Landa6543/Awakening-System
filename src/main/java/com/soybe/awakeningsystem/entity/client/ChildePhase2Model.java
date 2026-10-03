package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ChildeBossEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ChildePhase2Model extends EntityModel<ChildeBossEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "childe_phase2"), "main");

    private final ModelPart root;
    private final ModelPart bone2;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ChildePhase2Model(ModelPart root) {
        this.root = root;
        this.bone2 = root.getChild("bone2");
        this.head = this.bone2.getChild("ANIM_HEAD");
        this.leftArm = this.bone2.getChild("ANIM_LEFT_ARM");
        this.rightArm = this.bone2.getChild("ANIM_RIGHT_ARM");
        this.leftLeg = this.bone2.getChild("ANIM_LEFT_LEG");
        this.rightLeg = this.bone2.getChild("ANIM_RIGHT_LEG");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone2 = root.addOrReplaceChild("bone2",
                CubeListBuilder.create(),
                PartPose.offset(-2.0F, 24.0F, 0.0F));

        PartDefinition animHead = bone2.addOrReplaceChild("ANIM_HEAD",
                CubeListBuilder.create()
                        .texOffs(32, 43).addBox(-4.0805F, -8.1359F, -4.0428F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 27).addBox(-4.2239F, -8.1308F, -4.2198F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
                        .texOffs(32, 26).addBox(4.3261F, -18.0249F, 0.4668F, 0.0F, 7.0F, 1.0F)
                        .texOffs(36, 26).addBox(-4.1739F, -18.0249F, 0.4668F, 0.0F, 7.0F, 1.0F),
                PartPose.offset(1.9739F, -24.1192F, 0.4698F));

        animHead.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(38, 26).addBox(0.05F, -3.75F, -0.5F, 0.0F, 7.0F, 1.0F)
                        .texOffs(34, 26).addBox(8.55F, -3.75F, -0.5F, 0.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(-4.2239F, -8.1308F, -0.9698F, -0.5672F, 0.0F, 0.0F));

        PartDefinition body3 = bone2.addOrReplaceChild("Body3",
                CubeListBuilder.create()
                        .texOffs(0, 54).addBox(-3.9094F, -11.3735F, -0.2623F, 8.0F, 6.0F, 4.0F)
                        .texOffs(24, 59).addBox(-3.4094F, -5.3735F, -0.2623F, 7.0F, 6.0F, 4.0F),
                PartPose.offset(1.9094F, -12.6265F, -1.7377F));

        body3.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(22, 69).addBox(-1.75F, 0.0F, 1.5F, 3.0F, 12.0F, 0.0F),
                PartPose.offsetAndRotation(-3.6594F, -1.8735F, -1.2623F, -0.2618F, 0.4363F, 0.0F));

        body3.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(16, 64).addBox(-1.75F, 0.0F, 1.5F, 3.0F, 12.0F, 0.0F),
                PartPose.offsetAndRotation(3.8406F, -1.8735F, -1.2623F, -0.2618F, -0.4363F, 0.0F));

        body3.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(32, 43).addBox(-1.0F, -2.5F, 0.0F, 2.0F, 5.0F, 0.0F),
                PartPose.offsetAndRotation(0.0906F, 2.1265F, -0.7623F, -0.1745F, 0.0F, 0.0F));

        PartDefinition rightArm = bone2.addOrReplaceChild("ANIM_RIGHT_ARM",
                CubeListBuilder.create()
                        .texOffs(58, 65).addBox(-2.7031F, -0.2348F, -2.3789F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(-2.446F, -22.9466F, 0.3784F, 0.0001F, 0.0004F, 0.2899F));

        rightArm.addOrReplaceChild("RightArmLayer_r1",
                CubeListBuilder.create()
                        .texOffs(63, 10).addBox(-1.9934F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(-0.7031F, 4.7652F, -0.1102F, -0.5236F, 0.0F, 0.0F));

        PartDefinition leftArm = bone2.addOrReplaceChild("ANIM_LEFT_ARM",
                CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-0.2698F, -0.0006F, -1.5021F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(6.2608F, -24.0693F, -0.4979F, 0.0F, 0.0F, -0.2618F));

        leftArm.addOrReplaceChild("LeftArmLayer_r1",
                CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-2.0873F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(1.7302F, 8.0057F, -0.4979F, -0.3927F, 0.0F, 0.0F));

        PartDefinition rightLeg = bone2.addOrReplaceChild("ANIM_RIGHT_LEG",
                CubeListBuilder.create()
                        .texOffs(61, 55).addBox(-2.0F, 0.6312F, -1.4316F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.5F, -12.6312F, -0.4204F, -0.2214F, 0.1704F, -0.0381F));

        rightLeg.addOrReplaceChild("RightLegLayer_r1",
                CubeListBuilder.create()
                        .texOffs(60, 39).addBox(-1.99F, -0.5092F, 0.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 7.1404F, -1.2723F, 0.1745F, 0.0F, 0.0F));

        PartDefinition leftLeg = bone2.addOrReplaceChild("ANIM_LEFT_LEG",
                CubeListBuilder.create()
                        .texOffs(60, 23).addBox(-2.007F, 0.1308F, -1.1454F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(4.0F, -12.2757F, -0.5174F, -0.0707F, -0.3737F, -0.0075F));

        leftLeg.addOrReplaceChild("LeftLegLayer_r1",
                CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-1.99F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(-0.007F, 6.1308F, -1.1454F, 0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(ChildeBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.bone2.translateAndRotate(poseStack);
        if (arm == HumanoidArm.RIGHT) {
            this.rightArm.translateAndRotate(poseStack);
        } else {
            this.leftArm.translateAndRotate(poseStack);
        }
        poseStack.translate(-0.0625D, 0.4375D, 0.0D);
    }
}
