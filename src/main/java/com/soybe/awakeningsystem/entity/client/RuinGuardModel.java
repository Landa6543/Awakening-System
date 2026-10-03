package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinGuardEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RuinGuardModel extends EntityModel<RuinGuardEntity> {

    private final ModelPart root;
    private final ModelPart ruinGuard;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public RuinGuardModel(ModelPart root) {
        this.root = root;
        this.ruinGuard = root.getChild("RuinGuard");
        this.leftLeg = root.getChild("left_leg_ANIM");
        this.rightLeg = root.getChild("right_leg_ANIM");
        this.leftArm = root.getChild("left_arm_ANIM");
        this.rightArm = root.getChild("right_arm_ANIM");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition part_RuinGuard = root.addOrReplaceChild("RuinGuard",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition part_h_head = part_RuinGuard.addOrReplaceChild("h_head",
                CubeListBuilder.create()
                .texOffs(116, 176).mirror(false).addBox(-10.0F, 18.0F, -8.0F, 19.0F, 11.0F, 4.0F)
                .texOffs(62, 159).mirror(false).addBox(-6.0F, 15.0F, -10.0F, 11.0F, 7.0F, 4.0F)
                .texOffs(93, 47).mirror(false).addBox(-13.0F, 3.0F, -6.0F, 24.0F, 15.0F, 11.0F, new CubeDeformation(-0.02F))
                .texOffs(90, 39).mirror(false).addBox(-13.0F, 13.0F, 5.0F, 24.0F, 5.0F, 1.0F)
                .texOffs(0, 111).mirror(false).addBox(-13.0F, 3.0F, -7.0F, 24.0F, 2.0F, 1.0F)
                .texOffs(214, 180).mirror(false).addBox(-11.0F, -2.0F, -7.0F, 20.0F, 2.0F, 1.0F)
                .texOffs(212, 22).mirror(false).addBox(-11.0F, -2.0F, 5.0F, 20.0F, 2.0F, 1.0F)
                .texOffs(94, 36).mirror(false).addBox(-13.0F, 3.0F, 5.0F, 24.0F, 2.0F, 1.0F)
                .texOffs(229, 140).mirror(false).addBox(11.0F, 3.0F, -7.0F, 1.0F, 2.0F, 13.0F)
                .texOffs(12, 58).mirror(false).addBox(11.0F, 5.0F, -7.0F, 1.0F, 8.0F, 1.0F)
                .texOffs(75, 71).mirror(false).addBox(8.0F, 0.0F, -7.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(71, 71).mirror(false).addBox(8.0F, 0.0F, 5.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(67, 71).mirror(false).addBox(-11.0F, 0.0F, 5.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(0, 71).mirror(false).addBox(-11.0F, 0.0F, -7.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(8, 58).mirror(false).addBox(11.0F, 5.0F, 5.0F, 1.0F, 8.0F, 1.0F)
                .texOffs(4, 58).mirror(false).addBox(-14.0F, 5.0F, 5.0F, 1.0F, 8.0F, 1.0F)
                .texOffs(0, 58).mirror(false).addBox(-14.0F, 5.0F, -7.0F, 1.0F, 8.0F, 1.0F)
                .texOffs(227, 162).mirror(false).addBox(11.0F, 13.0F, -7.0F, 1.0F, 5.0F, 13.0F, new CubeDeformation(-0.02F))
                .texOffs(224, 91).mirror(false).addBox(-14.0F, 13.0F, -7.0F, 1.0F, 5.0F, 13.0F, new CubeDeformation(-0.02F))
                .texOffs(228, 220).mirror(false).addBox(-14.0F, 3.0F, -7.0F, 1.0F, 2.0F, 13.0F)
                .texOffs(41, 230).mirror(false).addBox(-11.0F, -2.0F, -6.0F, 1.0F, 2.0F, 11.0F)
                .texOffs(17, 230).mirror(false).addBox(8.0F, -2.0F, -6.0F, 1.0F, 2.0F, 11.0F)
                .texOffs(156, 114).mirror(false).addBox(-10.0F, -2.0F, -6.0F, 18.0F, 5.0F, 11.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-1.0F, -39.0F, 7.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r1 = part_h_head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                .texOffs(0, 170).mirror(false).addBox(-8.0F, -4.0F, 17.0F, 14.0F, 13.0F, 5.0F)
                .texOffs(0, 39).mirror(false).addBox(-20.0F, 11.9082F, 4.0536F, 38.0F, 5.0F, 14.0F)
                .texOffs(0, 0).mirror(false).addBox(-17.0F, -9.0918F, 2.0536F, 32.0F, 24.0F, 15.0F),
                PartPose.offsetAndRotation(0.0F, 29.0F, -7.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition part_cube_r2 = part_h_head.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                .texOffs(0, 10).mirror(false).addBox(10.0F, -12.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-3.9F, 29.0F, -7.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_cube_r3 = part_h_head.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                .texOffs(0, 12).mirror(false).addBox(-12.0F, -16.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-3.9F, 29.0F, -7.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_cube_r4 = part_h_head.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                .texOffs(0, 39).mirror(false).addBox(9.0F, -12.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-11.6F, 29.0F, -7.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_cube_r5 = part_h_head.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                .texOffs(0, 41).mirror(false).addBox(-12.0F, -16.0F, 0.0F, 11.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-11.6F, 29.0F, -7.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_cube_r6 = part_h_head.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                .texOffs(0, 43).mirror(false).addBox(-12.0F, -16.0F, 0.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(4.0F, 29.0F, -7.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_cube_r7 = part_h_head.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                .texOffs(0, 45).mirror(false).addBox(5.0F, -12.0F, 0.0F, 11.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(4.0F, 29.0F, -7.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_cube_r8 = part_h_head.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                .texOffs(211, 47).mirror(false).addBox(-2.3345F, -13.8517F, -7.1384F, 7.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(0.0F, 29.0F, -7.0F, -0.4363F, 0.0F, -0.7854F));

        PartDefinition part_cube_r9 = part_h_head.addOrReplaceChild("cube_r9",
                CubeListBuilder.create()
                .texOffs(173, 211).mirror(false).addBox(-5.6655F, -12.8517F, -7.1384F, 7.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(0.0F, 29.0F, -7.0F, -0.4363F, 0.0F, 0.7854F));

        PartDefinition part_RuinEye = part_h_head.addOrReplaceChild("RuinEye",
                CubeListBuilder.create()
                .texOffs(0, 0).mirror(false).addBox(-3.1F, -1.3F, -6.0F, 5.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-1.2F, 30.0F, -8.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r10 = part_RuinEye.addOrReplaceChild("cube_r10",
                CubeListBuilder.create()
                .texOffs(9, 68).mirror(false).addBox(-6.3223F, -11.3223F, -7.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(0, 51).mirror(false).addBox(-12.3223F, -11.3223F, -7.0F, 4.0F, 1.0F, 1.0F)
                .texOffs(7, 70).mirror(false).addBox(-12.3223F, -5.3223F, -7.0F, 1.0F, 4.0F, 1.0F)
                .texOffs(11, 70).mirror(false).addBox(-3.3223F, -11.3223F, -7.0F, 1.0F, 4.0F, 1.0F)
                .texOffs(79, 0).mirror(false).addBox(-3.3223F, -5.3223F, -7.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(0, 67).mirror(false).addBox(-6.3223F, -2.3223F, -7.0F, 4.0F, 1.0F, 1.0F)
                .texOffs(0, 69).mirror(false).addBox(-11.3223F, -2.3223F, -7.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(79, 4).mirror(false).addBox(-12.3223F, -10.3223F, -7.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(67, 58).mirror(false).addBox(-13.3223F, -12.3223F, -6.0F, 12.0F, 12.0F, 1.0F)
                .texOffs(170, 15).mirror(false).addBox(-14.3223F, -13.3223F, -5.0F, 14.0F, 14.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 1.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_RuinNeck = part_h_head.addOrReplaceChild("RuinNeck",
                CubeListBuilder.create()
                .texOffs(162, 176).mirror(false).addBox(-10.0F, -10.5326F, -4.1513F, 19.0F, 9.0F, 4.0F)
                .texOffs(0, 139).mirror(false).addBox(-6.0F, -13.5326F, -6.1513F, 11.0F, 7.0F, 6.0F),
                PartPose.offsetAndRotation(-1.0F, 26.0F, -5.0F, -0.4363F, 0.0F, -3.1416F));

        PartDefinition part_cube_r11 = part_RuinNeck.addOrReplaceChild("cube_r11",
                CubeListBuilder.create()
                .texOffs(90, 213).mirror(false).addBox(42.1483F, -48.2728F, -38.5774F, 7.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(0.0F, 63.3756F, 10.7951F, -0.4363F, 0.0F, -0.7854F));

        PartDefinition part_cube_r12 = part_RuinNeck.addOrReplaceChild("cube_r12",
                CubeListBuilder.create()
                .texOffs(190, 222).mirror(false).addBox(-50.1483F, -47.2728F, -38.5774F, 7.0F, 7.0F, 4.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(0.0F, 63.3756F, 10.7951F, -0.4363F, 0.0F, 0.7854F));

        PartDefinition part_RuinPelvis = part_RuinGuard.addOrReplaceChild("RuinPelvis",
                CubeListBuilder.create()
                .texOffs(158, 130).mirror(false).addBox(-10.0F, -1.0F, -5.0F, 18.0F, 1.0F, 11.0F)
                .texOffs(179, 69).mirror(false).addBox(-5.0F, -6.0F, -5.0F, 8.0F, 5.0F, 11.0F),
                PartPose.offsetAndRotation(-1.0F, -37.0F, 6.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r13 = part_RuinPelvis.addOrReplaceChild("cube_r13",
                CubeListBuilder.create()
                .texOffs(0, 6).mirror(false).addBox(10.0F, -12.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 49).mirror(false).addBox(10.0F, -12.0F, -12.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-4.0F, 16.0F, 6.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_cube_r14 = part_RuinPelvis.addOrReplaceChild("cube_r14",
                CubeListBuilder.create()
                .texOffs(0, 47).mirror(false).addBox(-11.0F, -16.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F))
                .texOffs(0, 8).mirror(false).addBox(-12.0F, -16.0F, -12.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-4.0F, 16.0F, 6.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_left_leg_ANIM = root.addOrReplaceChild("left_leg_ANIM",
                CubeListBuilder.create()
                .texOffs(105, 107).mirror(false).addBox(-1.9117F, -4.402F, -5.0F, 25.0F, 9.0F, 9.0F)
                .texOffs(213, 211).mirror(false).addBox(15.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(101, 213).mirror(false).addBox(10.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(73, 213).mirror(false).addBox(5.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(212, 153).mirror(false).addBox(0.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F),
                PartPose.offsetAndRotation(9.4939F, -19.6533F, 8.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition part_LRuinLegBottom2 = part_left_leg_ANIM.addOrReplaceChild("LRuinLegBottom2",
                CubeListBuilder.create()
                .texOffs(136, 143).mirror(false).addBox(0.0883F, -4.402F, -5.0F, 20.0F, 9.0F, 9.0F)
                .texOffs(156, 211).mirror(false).addBox(16.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(209, 80).mirror(false).addBox(11.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(45, 208).mirror(false).addBox(6.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(208, 25).mirror(false).addBox(1.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F),
                PartPose.offset(20.0F, 0.0F, 0.0F));

        PartDefinition part_LRuinLegFoot = part_LRuinLegBottom2.addOrReplaceChild("LRuinLegFoot",
                CubeListBuilder.create()
                .texOffs(40, 154).mirror(false).addBox(0.0F, -7.0F, -10.0F, 3.0F, 16.0F, 16.0F)
                .texOffs(168, 189).mirror(false).addBox(-1.0F, -7.0F, -17.0F, 4.0F, 4.0F, 7.0F)
                .texOffs(44, 186).mirror(false).addBox(-1.0F, 5.0F, -17.0F, 4.0F, 4.0F, 7.0F)
                .texOffs(45, 91).mirror(false).addBox(-1.0F, -1.0F, -17.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offset(20.0F, -1.0F, 0.0F));

        PartDefinition part_right_leg_ANIM = root.addOrReplaceChild("right_leg_ANIM",
                CubeListBuilder.create()
                .texOffs(96, 125).mirror(false).addBox(-1.9117F, -4.402F, -5.0F, 25.0F, 9.0F, 9.0F)
                .texOffs(226, 69).mirror(false).addBox(15.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(118, 224).mirror(false).addBox(10.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(223, 47).mirror(false).addBox(5.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(173, 222).mirror(false).addBox(0.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F),
                PartPose.offsetAndRotation(-9.5061F, -19.6533F, 8.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition part_RRuinLegBottom = part_right_leg_ANIM.addOrReplaceChild("RRuinLegBottom",
                CubeListBuilder.create()
                .texOffs(151, 89).mirror(false).addBox(-0.9117F, -4.402F, -5.0F, 20.0F, 9.0F, 9.0F)
                .texOffs(222, 113).mirror(false).addBox(15.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(0, 219).mirror(false).addBox(10.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(218, 0).mirror(false).addBox(5.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(214, 189).mirror(false).addBox(0.0883F, -5.402F, -6.0F, 3.0F, 11.0F, 11.0F),
                PartPose.offset(21.0F, 0.0F, 0.0F));

        PartDefinition part_RRuinLegFoot2 = part_RRuinLegBottom.addOrReplaceChild("RRuinLegFoot2",
                CubeListBuilder.create()
                .texOffs(78, 159).mirror(false).addBox(0.0F, -7.0F, -10.0F, 3.0F, 16.0F, 16.0F)
                .texOffs(34, 208).mirror(false).addBox(-1.0F, -7.0F, -17.0F, 4.0F, 4.0F, 7.0F)
                .texOffs(128, 191).mirror(false).addBox(-1.0F, 5.0F, -17.0F, 4.0F, 4.0F, 7.0F)
                .texOffs(100, 191).mirror(false).addBox(-1.0F, -1.0F, -17.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offset(19.0F, -1.0F, 0.0F));

        PartDefinition part_left_arm_ANIM = root.addOrReplaceChild("left_arm_ANIM",
                CubeListBuilder.create(),
                PartPose.offset(15.0F, -53.0F, 12.0F));

        PartDefinition part_LRuinShoulder = part_left_arm_ANIM.addOrReplaceChild("LRuinShoulder",
                CubeListBuilder.create(),
                PartPose.offset(4.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r15 = part_LRuinShoulder.addOrReplaceChild("cube_r15",
                CubeListBuilder.create()
                .texOffs(164, 161).mirror(false).addBox(-18.0F, -76.0F, 2.0F, 23.0F, 14.0F, 1.0F)
                .texOffs(76, 143).mirror(false).addBox(-18.0F, -78.0F, 4.0F, 15.0F, 1.0F, 15.0F)
                .texOffs(67, 74).mirror(false).addBox(-19.0F, -77.0F, 3.0F, 25.0F, 16.0F, 17.0F)
                .texOffs(170, 0).mirror(false).addBox(-18.0F, -76.0F, 20.0F, 23.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(-18.0F, 69.0F, -12.0F, 0.0F, 0.0F, 0.4363F));

        PartDefinition part_LRuinMid = part_LRuinShoulder.addOrReplaceChild("LRuinMid",
                CubeListBuilder.create()
                .texOffs(94, 18).mirror(false).addBox(-27.0F, -5.0F, -5.0F, 29.0F, 9.0F, 9.0F)
                .texOffs(17, 208).mirror(false).addBox(-10.0F, -6.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(206, 58).mirror(false).addBox(-15.0F, -6.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(205, 131).mirror(false).addBox(-20.0F, -6.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(203, 102).mirror(false).addBox(-25.0F, -6.0F, -6.0F, 3.0F, 11.0F, 11.0F),
                PartPose.offsetAndRotation(7.0F, 12.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_LRuinLower = part_LRuinMid.addOrReplaceChild("LRuinLower",
                CubeListBuilder.create(),
                PartPose.offset(-25.5061F, -0.6533F, 0.0F));

        PartDefinition part_cube_r16 = part_LRuinLower.addOrReplaceChild("cube_r16",
                CubeListBuilder.create()
                .texOffs(197, 178).mirror(false).addBox(11.0F, -44.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(196, 200).mirror(false).addBox(16.0F, -44.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(128, 202).mirror(false).addBox(21.0F, -44.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(152, 36).mirror(false).addBox(10.0F, -43.0F, -5.0F, 19.0F, 9.0F, 9.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-25.9117F, 38.598F, -12.0F, 0.0F, -0.4363F, 0.0F));

        PartDefinition part_LPod = part_LRuinShoulder.addOrReplaceChild("LPod",
                CubeListBuilder.create(),
                PartPose.offset(6.0F, -5.0F, 0.0F));

        PartDefinition part_cube_r17 = part_LPod.addOrReplaceChild("cube_r17",
                CubeListBuilder.create()
                .texOffs(45, 107).mirror(false).addBox(-18.0F, -95.0F, 4.0F, 15.0F, 5.0F, 15.0F)
                .texOffs(39, 127).mirror(false).addBox(-17.0F, -90.0F, 5.0F, 13.0F, 12.0F, 13.0F),
                PartPose.offsetAndRotation(-24.0F, 74.0F, -12.0F, 0.0F, 0.0F, 0.4363F));

        PartDefinition part_right_arm_ANIM = root.addOrReplaceChild("right_arm_ANIM",
                CubeListBuilder.create(),
                PartPose.offset(-20.0F, -54.0F, 12.0F));

        PartDefinition part_RRuinShoulder = part_right_arm_ANIM.addOrReplaceChild("RRuinShoulder",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(2.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_cube_r18 = part_RRuinShoulder.addOrReplaceChild("cube_r18",
                CubeListBuilder.create()
                .texOffs(116, 161).mirror(false).addBox(-26.0F, -76.0F, 2.0F, 23.0F, 14.0F, 1.0F)
                .texOffs(134, 73).mirror(false).addBox(-18.0F, -78.0F, 4.0F, 15.0F, 1.0F, 15.0F)
                .texOffs(0, 58).mirror(false).addBox(-27.0F, -77.0F, 3.0F, 25.0F, 16.0F, 17.0F)
                .texOffs(163, 54).mirror(false).addBox(-26.0F, -76.0F, 20.0F, 23.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(-20.5061F, 64.3467F, -12.0F, 0.0F, 0.0F, 0.4363F));

        PartDefinition part_RRuinMid = part_RRuinShoulder.addOrReplaceChild("RRuinMid",
                CubeListBuilder.create()
                .texOffs(94, 0).mirror(false).addBox(-26.9117F, -3.402F, -5.0F, 29.0F, 9.0F, 9.0F)
                .texOffs(0, 197).mirror(false).addBox(-9.9117F, -4.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(111, 191).mirror(false).addBox(-14.9117F, -4.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(83, 191).mirror(false).addBox(-19.9117F, -4.402F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(179, 189).mirror(false).addBox(-24.9117F, -4.402F, -6.0F, 3.0F, 11.0F, 11.0F),
                PartPose.offsetAndRotation(-12.5061F, 0.3467F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_RRuinLower = part_RRuinMid.addOrReplaceChild("RRuinLower",
                CubeListBuilder.create(),
                PartPose.offset(-25.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r19 = part_RRuinLower.addOrReplaceChild("cube_r19",
                CubeListBuilder.create()
                .texOffs(27, 186).mirror(false).addBox(11.0F, -44.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(55, 186).mirror(false).addBox(16.0F, -44.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(151, 189).mirror(false).addBox(21.0F, -44.0F, -6.0F, 3.0F, 11.0F, 11.0F)
                .texOffs(0, 152).mirror(false).addBox(10.0F, -43.0F, -5.0F, 19.0F, 9.0F, 9.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-25.9117F, 39.598F, -12.0F, 0.0F, -0.4363F, 0.0F));

        PartDefinition part_RPod = part_RRuinShoulder.addOrReplaceChild("RPod",
                CubeListBuilder.create(),
                PartPose.offset(2.0F, -10.0F, 0.0F));

        PartDefinition part_cube_r20 = part_RPod.addOrReplaceChild("cube_r20",
                CubeListBuilder.create()
                .texOffs(0, 91).mirror(false).addBox(-18.0F, -95.0F, 4.0F, 15.0F, 5.0F, 15.0F)
                .texOffs(0, 114).mirror(false).addBox(-17.0F, -90.0F, 5.0F, 13.0F, 12.0F, 13.0F),
                PartPose.offsetAndRotation(-22.5061F, 74.3467F, -12.0F, 0.0F, 0.0F, 0.4363F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(RuinGuardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Animación de marcha pesada del autómata
        this.leftLeg.yRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.rightLeg.yRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;

        // Ataques especiales: Giro en torbellino
        if (entity.isSpinning()) {
            float spin = ageInTicks * 0.8F;
            this.ruinGuard.yRot = spin;
            this.leftArm.xRot = -1.5708F; // Brazos horizontales extendidos
            this.rightArm.xRot = -1.5708F;
        } else if (entity.isStunned()) {
            // Núcleo ocular dañado: cabeza gacha apagada
            this.ruinGuard.yRot = 0.0F;
            this.ruinGuard.xRot = 0.35F;
            this.leftArm.xRot = 0.5F;
            this.rightArm.xRot = 0.5F;
        } else {
            this.ruinGuard.xRot = 0.0F;
            this.ruinGuard.yRot = 0.0F;
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
