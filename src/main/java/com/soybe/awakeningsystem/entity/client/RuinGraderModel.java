package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinGraderEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RuinGraderModel extends EntityModel<RuinGraderEntity> {

    private final ModelPart root;
    private final ModelPart ruinGuard;
    private final ModelPart cabeza;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart ruinPelvis;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public RuinGraderModel(ModelPart root) {
        this.root = root;
        this.ruinGuard = root.getChild("RuinGuard");
        this.cabeza = this.ruinGuard.getChild("CABEZA");
        this.leftArm = this.cabeza.getChild("LEFT_ARM");
        this.rightArm = this.cabeza.getChild("RIGHT_ARM");
        this.ruinPelvis = this.ruinGuard.getChild("RuinPelvis");
        this.leftLeg = this.ruinPelvis.getChild("LEFT_LEG");
        this.rightLeg = this.ruinPelvis.getChild("RIGHT_LEG");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition part_RuinGuard = root.addOrReplaceChild("RuinGuard",
                CubeListBuilder.create(), PartPose.offset(0.0000F, 15.0000F, 0.0000F));

        PartDefinition part_CABEZA = part_RuinGuard.addOrReplaceChild("CABEZA",
                CubeListBuilder.create()
                .texOffs(204, 131).mirror(false).addBox(-10.0000F, 18.0000F, -8.0000F, 19.0000F, 11.0000F, 4.0000F)
                .texOffs(211, 0).mirror(false).addBox(-6.0000F, 15.0000F, -10.0000F, 11.0000F, 7.0000F, 4.0000F)
                .texOffs(93, 47).mirror(false).addBox(-13.0000F, 3.0000F, -6.0000F, 24.0000F, 15.0000F, 11.0000F, new CubeDeformation(-0.0200F))
                .texOffs(90, 39).mirror(false).addBox(-13.0000F, 13.0000F, 5.0000F, 24.0000F, 5.0000F, 1.0000F)
                .texOffs(0, 94).mirror(false).addBox(-13.0000F, 3.0000F, -7.0000F, 24.0000F, 2.0000F, 1.0000F)
                .texOffs(27, 132).mirror(false).addBox(-11.0000F, -2.0000F, -7.0000F, 20.0000F, 2.0000F, 1.0000F)
                .texOffs(94, 36).mirror(false).addBox(-11.0000F, -2.0000F, 5.0000F, 20.0000F, 2.0000F, 1.0000F)
                .texOffs(0, 91).mirror(false).addBox(-13.0000F, 3.0000F, 5.0000F, 24.0000F, 2.0000F, 1.0000F)
                .texOffs(238, 240).mirror(false).addBox(11.0000F, 3.0000F, -7.0000F, 1.0000F, 2.0000F, 13.0000F)
                .texOffs(54, 116).mirror(false).addBox(11.0000F, 5.0000F, -7.0000F, 1.0000F, 8.0000F, 1.0000F)
                .texOffs(110, 163).mirror(false).addBox(8.0000F, 0.0000F, -7.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(106, 163).mirror(false).addBox(8.0000F, 0.0000F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(91, 163).mirror(false).addBox(-11.0000F, 0.0000F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(163, 58).mirror(false).addBox(-11.0000F, 0.0000F, -7.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(97, 0).mirror(false).addBox(11.0000F, 5.0000F, 5.0000F, 1.0000F, 8.0000F, 1.0000F)
                .texOffs(8, 39).mirror(false).addBox(-14.0000F, 5.0000F, 5.0000F, 1.0000F, 8.0000F, 1.0000F)
                .texOffs(8, 0).mirror(false).addBox(-14.0000F, 5.0000F, -7.0000F, 1.0000F, 8.0000F, 1.0000F)
                .texOffs(240, 71).mirror(false).addBox(11.0000F, 13.0000F, -7.0000F, 1.0000F, 5.0000F, 13.0000F, new CubeDeformation(-0.0200F))
                .texOffs(75, 125).mirror(false).addBox(-14.0000F, 13.0000F, -7.0000F, 1.0000F, 5.0000F, 13.0000F, new CubeDeformation(-0.0200F))
                .texOffs(240, 218).mirror(false).addBox(-14.0000F, 3.0000F, -7.0000F, 1.0000F, 2.0000F, 13.0000F)
                .texOffs(128, 179).mirror(false).addBox(-11.0000F, -2.0000F, -6.0000F, 1.0000F, 2.0000F, 11.0000F)
                .texOffs(117, 163).mirror(false).addBox(8.0000F, -2.0000F, -6.0000F, 1.0000F, 2.0000F, 11.0000F)
                .texOffs(0, 177).mirror(false).addBox(-10.0000F, -2.0000F, -6.0000F, 18.0000F, 5.0000F, 11.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-1.0000F, -39.0000F, 7.0000F, 0.0000F, 0.0000F, -3.1416F));

        PartDefinition part_cube_r1 = part_CABEZA.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                .texOffs(0, 177).mirror(false).addBox(-25.0000F, 23.0000F, 31.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(171, 115).mirror(false).addBox(-27.0000F, 21.0000F, 32.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(171, 111).mirror(false).addBox(-30.0000F, 19.0000F, 33.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(170, 28).mirror(false).addBox(-37.0000F, 15.0000F, 36.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(50, 91).mirror(false).addBox(-31.0000F, 15.0000F, 34.0000F, 3.0000F, 6.0000F, 1.0000F)
                .texOffs(170, 24).mirror(false).addBox(-34.0000F, 15.0000F, 35.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(130, 169).mirror(false).addBox(-40.0000F, 13.0000F, 37.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(117, 168).mirror(false).addBox(-23.0000F, 25.0000F, 30.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(128, 155).mirror(false).addBox(-38.0000F, 10.0000F, 37.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(35, 153).mirror(false).addBox(-21.0000F, 24.0000F, 29.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(79, 9).mirror(false).addBox(-20.0000F, 24.0000F, 28.0000F, 6.0000F, 3.0000F, 1.0000F)
                .texOffs(158, 129).mirror(false).addBox(-19.0000F, 24.0000F, 27.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(75, 143).mirror(false).addBox(-15.0000F, 24.0000F, 26.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(55, 159).mirror(false).addBox(-13.0000F, 24.0000F, 25.0000F, 13.0000F, 3.0000F, 1.0000F)
                .texOffs(158, 135).mirror(false).addBox(-6.0000F, 25.0000F, 23.0000F, 13.0000F, 3.0000F, 2.0000F)
                .texOffs(128, 183).mirror(false).addBox(-32.0000F, 36.0000F, 35.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 181).mirror(false).addBox(-34.0000F, 34.0000F, 36.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(128, 179).mirror(false).addBox(-29.0000F, 35.0000F, 34.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(94, 18).mirror(false).addBox(-26.0000F, 36.0000F, 33.0000F, 3.0000F, 6.0000F, 1.0000F)
                .texOffs(0, 97).mirror(false).addBox(-31.0000F, 47.0000F, 33.0000F, 3.0000F, 6.0000F, 1.0000F)
                .texOffs(17, 214).mirror(false).addBox(-30.0000F, 47.0000F, 32.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 214).mirror(false).addBox(-38.0000F, 55.0000F, 35.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(92, 212).mirror(false).addBox(-36.0000F, 52.0000F, 35.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(46, 210).mirror(false).addBox(-34.0000F, 51.0000F, 34.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(97, 216).mirror(false).addBox(-27.0000F, 47.0000F, 31.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 218).mirror(false).addBox(-25.0000F, 44.0000F, 31.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(141, 186).mirror(false).addBox(-24.0000F, 40.0000F, 32.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(78, 189).mirror(false).addBox(-23.0000F, 41.0000F, 31.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(17, 218).mirror(false).addBox(-23.0000F, 43.0000F, 30.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(221, 146).mirror(false).addBox(-21.0000F, 42.0000F, 29.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(90, 47).mirror(false).addBox(-20.0000F, 42.0000F, 28.0000F, 6.0000F, 3.0000F, 1.0000F)
                .texOffs(147, 170).mirror(false).addBox(-19.0000F, 42.0000F, 27.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 168).mirror(false).addBox(-15.0000F, 42.0000F, 26.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(240, 89).mirror(false).addBox(-13.0000F, 42.0000F, 25.0000F, 13.0000F, 3.0000F, 1.0000F)
                .texOffs(185, 126).mirror(false).addBox(-6.0000F, 41.0000F, 23.0000F, 13.0000F, 3.0000F, 2.0000F)
                , PartPose.offsetAndRotation(-35.4178F, 37.9447F, -11.0000F, 0.0000F, 0.4363F, -1.5708F));

        PartDefinition part_cube_r2 = part_CABEZA.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                .texOffs(59, 212).mirror(false).addBox(-8.0000F, -4.0000F, 17.0000F, 14.0000F, 13.0000F, 5.0000F)
                .texOffs(0, 39).mirror(false).addBox(-20.0000F, 11.9082F, 4.0536F, 38.0000F, 5.0000F, 14.0000F)
                .texOffs(0, 0).mirror(false).addBox(-17.0000F, -9.0918F, 2.0536F, 32.0000F, 24.0000F, 15.0000F)
                , PartPose.offsetAndRotation(0.0000F, 29.0000F, -7.0000F, 0.4363F, 0.0000F, 0.0000F));

        PartDefinition part_cube_r3 = part_CABEZA.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                .texOffs(0, 135).mirror(false).addBox(10.0000F, -12.0000F, 0.0000F, 6.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-3.9000F, 29.0000F, -7.0000F, 0.0000F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r4 = part_CABEZA.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                .texOffs(90, 51).mirror(false).addBox(-12.0000F, -16.0000F, 0.0000F, 6.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-3.9000F, 29.0000F, -7.0000F, 0.0000F, 0.0000F, 0.7854F));

        PartDefinition part_cube_r5 = part_CABEZA.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                .texOffs(54, 125).mirror(false).addBox(9.0000F, -12.0000F, 0.0000F, 7.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-11.6000F, 29.0000F, -7.0000F, 0.0000F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r6 = part_CABEZA.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                .texOffs(90, 45).mirror(false).addBox(-12.0000F, -16.0000F, 0.0000F, 11.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-11.6000F, 29.0000F, -7.0000F, 0.0000F, 0.0000F, 0.7854F));

        PartDefinition part_cube_r7 = part_CABEZA.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                .texOffs(67, 73).mirror(false).addBox(-12.0000F, -16.0000F, 0.0000F, 7.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(4.0000F, 29.0000F, -7.0000F, 0.0000F, 0.0000F, 0.7854F));

        PartDefinition part_cube_r8 = part_CABEZA.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                .texOffs(114, 45).mirror(false).addBox(5.0000F, -12.0000F, 0.0000F, 11.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(4.0000F, 29.0000F, -7.0000F, 0.0000F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r9 = part_CABEZA.addOrReplaceChild("cube_r9",
                CubeListBuilder.create()
                .texOffs(158, 114).mirror(false).addBox(-1.7345F, -8.8517F, -11.1384F, 3.0000F, 8.0000F, 7.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(0.0000F, 29.0000F, -7.0000F, -0.6109F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r10 = part_CABEZA.addOrReplaceChild("cube_r10",
                CubeListBuilder.create()
                .texOffs(131, 231).mirror(false).addBox(-2.3345F, -13.8517F, -7.1384F, 7.0000F, 7.0000F, 4.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(0.0000F, 29.0000F, -7.0000F, -0.4363F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r11 = part_CABEZA.addOrReplaceChild("cube_r11",
                CubeListBuilder.create()
                .texOffs(147, 155).mirror(false).addBox(-1.6655F, -7.8517F, -11.1384F, 3.0000F, 8.0000F, 7.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(0.0000F, 29.0000F, -7.0000F, -0.6109F, 0.0000F, 0.7854F));

        PartDefinition part_cube_r12 = part_CABEZA.addOrReplaceChild("cube_r12",
                CubeListBuilder.create()
                .texOffs(246, 47).mirror(false).addBox(-5.6655F, -12.8517F, -7.1384F, 7.0000F, 7.0000F, 4.0000F)
                , PartPose.offsetAndRotation(0.0000F, 29.0000F, -7.0000F, -0.4363F, 0.0000F, 0.7854F));

        PartDefinition part_RuinEye = part_CABEZA.addOrReplaceChild("RuinEye",
                CubeListBuilder.create()
                .texOffs(35, 193).mirror(false).addBox(-3.1000F, -1.3000F, -6.0000F, 5.0000F, 5.0000F, 1.0000F)
                , PartPose.offsetAndRotation(-1.2000F, 30.0000F, -8.0000F, 0.0000F, 0.0000F, -3.1416F));

        PartDefinition part_cube_r13 = part_RuinEye.addOrReplaceChild("cube_r13",
                CubeListBuilder.create()
                .texOffs(94, 25).mirror(false).addBox(-6.3223F, -11.3223F, -7.0000F, 3.0000F, 1.0000F, 1.0000F)
                .texOffs(58, 146).mirror(false).addBox(-12.3223F, -11.3223F, -7.0000F, 4.0000F, 1.0000F, 1.0000F)
                .texOffs(0, 66).mirror(false).addBox(-12.3223F, -5.3223F, -7.0000F, 1.0000F, 4.0000F, 1.0000F)
                .texOffs(0, 58).mirror(false).addBox(-3.3223F, -11.3223F, -7.0000F, 1.0000F, 4.0000F, 1.0000F)
                .texOffs(130, 163).mirror(false).addBox(-3.3223F, -5.3223F, -7.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(48, 146).mirror(false).addBox(-6.3223F, -2.3223F, -7.0000F, 4.0000F, 1.0000F, 1.0000F)
                .texOffs(0, 104).mirror(false).addBox(-11.3223F, -2.3223F, -7.0000F, 3.0000F, 1.0000F, 1.0000F)
                .texOffs(138, 163).mirror(false).addBox(-12.3223F, -10.3223F, -7.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(67, 58).mirror(false).addBox(-13.3223F, -12.3223F, -6.0000F, 12.0000F, 12.0000F, 1.0000F)
                .texOffs(0, 193).mirror(false).addBox(-14.3223F, -13.3223F, -5.0000F, 14.0000F, 14.0000F, 7.0000F)
                , PartPose.offsetAndRotation(0.0000F, 11.0000F, 1.0000F, 0.0000F, 0.0000F, 0.7854F));

        PartDefinition part_RuinNeck = part_CABEZA.addOrReplaceChild("RuinNeck",
                CubeListBuilder.create()
                .texOffs(185, 207).mirror(false).addBox(-10.0000F, -10.5326F, -4.1513F, 19.0000F, 9.0000F, 4.0000F)
                .texOffs(27, 159).mirror(false).addBox(-6.0000F, -13.5326F, -6.1513F, 11.0000F, 7.0000F, 6.0000F)
                , PartPose.offsetAndRotation(-1.0000F, 26.0000F, -5.0000F, -0.4363F, 0.0000F, -3.1416F));

        PartDefinition part_cube_r14 = part_RuinNeck.addOrReplaceChild("cube_r14",
                CubeListBuilder.create()
                .texOffs(0, 116).mirror(false).addBox(39.1483F, -41.2728F, -48.2774F, 3.0000F, 8.0000F, 7.0000F)
                , PartPose.offsetAndRotation(0.0000F, 63.3756F, 10.7951F, -0.7218F, 0.2382F, -0.6610F));

        PartDefinition part_cube_r15 = part_RuinNeck.addOrReplaceChild("cube_r15",
                CubeListBuilder.create()
                .texOffs(196, 248).mirror(false).addBox(42.1483F, -48.2728F, -38.5774F, 7.0000F, 7.0000F, 4.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(0.0000F, 63.3756F, 10.7951F, -0.4363F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r16 = part_RuinNeck.addOrReplaceChild("cube_r16",
                CubeListBuilder.create()
                .texOffs(0, 153).mirror(false).addBox(-50.1483F, -44.9728F, -34.5774F, 3.0000F, 8.0000F, 7.0000F)
                , PartPose.offsetAndRotation(0.0000F, 63.3756F, 10.7951F, -0.6545F, -0.3927F, 0.7854F));

        PartDefinition part_cube_r17 = part_RuinNeck.addOrReplaceChild("cube_r17",
                CubeListBuilder.create()
                .texOffs(250, 101).mirror(false).addBox(-50.1483F, -47.2728F, -38.5774F, 7.0000F, 7.0000F, 4.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(0.0000F, 63.3756F, 10.7951F, -0.4363F, 0.0000F, 0.7854F));

        PartDefinition part_LEFT_ARM = part_CABEZA.addOrReplaceChild("LEFT_ARM",
                CubeListBuilder.create(), PartPose.offsetAndRotation(-15.0000F, 30.0000F, 5.0000F, 0.0000F, 0.0000F, -3.1416F));

        PartDefinition part_LRuinShoulder = part_LEFT_ARM.addOrReplaceChild("LRuinShoulder",
                CubeListBuilder.create(), PartPose.offset(4.0000F, 0.0000F, 0.0000F));

        PartDefinition part_cube_r18 = part_LRuinShoulder.addOrReplaceChild("cube_r18",
                CubeListBuilder.create()
                .texOffs(181, 80).mirror(false).addBox(-11.0000F, -61.0000F, -51.0000F, 10.0000F, 14.0000F, 11.0000F)
                , PartPose.offsetAndRotation(-18.0000F, 69.0000F, -12.0000F, -0.7854F, 0.0000F, 0.4363F));

        PartDefinition part_cube_r19 = part_LRuinShoulder.addOrReplaceChild("cube_r19",
                CubeListBuilder.create()
                .texOffs(137, 198).mirror(false).addBox(-18.0000F, -76.0000F, 2.0000F, 23.0000F, 14.0000F, 1.0000F)
                .texOffs(83, 144).mirror(false).addBox(-13.9522F, -80.7842F, 4.0000F, 15.0000F, 4.0000F, 15.0000F)
                .texOffs(67, 74).mirror(false).addBox(-19.0000F, -77.0000F, 3.0000F, 25.0000F, 16.0000F, 17.0000F)
                .texOffs(89, 197).mirror(false).addBox(-18.0000F, -76.0000F, 20.0000F, 23.0000F, 14.0000F, 1.0000F)
                , PartPose.offsetAndRotation(-18.0000F, 69.0000F, -12.0000F, 0.0000F, 0.0000F, 0.4363F));

        PartDefinition part_LRuinMid = part_LRuinShoulder.addOrReplaceChild("LRuinMid",
                CubeListBuilder.create()
                .texOffs(94, 0).mirror(false).addBox(-27.0000F, -5.0000F, -5.0000F, 29.0000F, 9.0000F, 9.0000F)
                .texOffs(234, 0).mirror(false).addBox(-10.0000F, -6.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(233, 105).mirror(false).addBox(-15.0000F, -6.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(223, 231).mirror(false).addBox(-20.0000F, -6.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(142, 231).mirror(false).addBox(-25.0000F, -6.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(28, 249).mirror(false).addBox(-25.0000F, 5.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(159, 231).mirror(false).addBox(-20.0000F, 5.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(180, 180).mirror(false).addBox(-15.0000F, 5.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(90, 128).mirror(false).addBox(-10.0000F, 5.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                , PartPose.offsetAndRotation(7.0000F, 12.0000F, 0.0000F, 0.0000F, 0.0000F, -1.5708F));

        PartDefinition part_LRuinLower = part_LRuinMid.addOrReplaceChild("LRuinLower",
                CubeListBuilder.create(), PartPose.offset(-25.5061F, -0.6533F, 0.0000F));

        PartDefinition part_cube_r20 = part_LRuinLower.addOrReplaceChild("cube_r20",
                CubeListBuilder.create()
                .texOffs(251, 0).mirror(false).addBox(21.0000F, -33.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(251, 22).mirror(false).addBox(16.0000F, -33.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(161, 0).mirror(false).addBox(0.0000F, -33.0000F, -3.0000F, 4.0000F, 3.0000F, 5.0000F)
                .texOffs(183, 72).mirror(false).addBox(1.0000F, -33.0000F, 2.0000F, 11.0000F, 3.0000F, 3.0000F)
                .texOffs(240, 209).mirror(false).addBox(1.0000F, -33.0000F, -6.0000F, 11.0000F, 3.0000F, 3.0000F)
                .texOffs(243, 191).mirror(false).addBox(2.0000F, -30.0000F, -3.0000F, 8.0000F, 2.0000F, 5.0000F)
                .texOffs(212, 83).mirror(false).addBox(7.0000F, -33.0000F, -3.0000F, 4.0000F, 3.0000F, 5.0000F)
                .texOffs(0, 58).mirror(false).addBox(4.0000F, -33.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(212, 61).mirror(false).addBox(4.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(251, 93).mirror(false).addBox(11.0000F, -33.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(223, 209).mirror(false).addBox(11.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(17, 227).mirror(false).addBox(16.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(229, 49).mirror(false).addBox(21.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(163, 54).mirror(false).addBox(10.0000F, -43.0000F, -5.0000F, 19.0000F, 9.0000F, 9.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-25.9117F, 38.5980F, -12.0000F, 0.0000F, -0.4363F, 0.0000F));

        PartDefinition part_cube_r21 = part_LRuinLower.addOrReplaceChild("cube_r21",
                CubeListBuilder.create()
                .texOffs(125, 213).mirror(false).addBox(3.0000F, -43.0000F, -5.0000F, 7.0000F, 9.0000F, 9.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-25.9117F, 38.5980F, -12.0000F, 0.0000F, -0.4363F, 0.0000F));

        PartDefinition part_LPod = part_LRuinShoulder.addOrReplaceChild("LPod",
                CubeListBuilder.create(), PartPose.offset(6.0000F, -5.0000F, 0.0000F));

        PartDefinition part_cube_r22 = part_LPod.addOrReplaceChild("cube_r22",
                CubeListBuilder.create()
                .texOffs(174, 164).mirror(false).addBox(-12.9522F, -89.7842F, 5.0000F, 13.0000F, 1.0000F, 13.0000F)
                .texOffs(65, 127).mirror(false).addBox(-0.9522F, -86.7842F, 18.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(161, 0).mirror(false).addBox(-4.9522F, -86.7842F, 18.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(227, 44).mirror(false).addBox(-13.9522F, -88.7842F, 18.0000F, 15.0000F, 2.0000F, 1.0000F)
                .texOffs(27, 159).mirror(false).addBox(-8.9522F, -86.7842F, 18.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(118, 111).mirror(false).addBox(-13.9522F, -86.7842F, 18.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(150, 158).mirror(false).addBox(-13.9522F, -86.7842F, 17.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(3, 156).mirror(false).addBox(-13.9522F, -86.7842F, 13.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(147, 155).mirror(false).addBox(-13.9522F, -86.7842F, 9.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(138, 155).mirror(false).addBox(-13.9522F, -86.7842F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(157, 240).mirror(false).addBox(-13.9522F, -88.7842F, 5.0000F, 1.0000F, 2.0000F, 13.0000F)
                .texOffs(215, 127).mirror(false).addBox(-13.9522F, -88.7842F, 4.0000F, 15.0000F, 2.0000F, 1.0000F)
                .texOffs(118, 107).mirror(false).addBox(-13.9522F, -86.7842F, 4.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(27, 153).mirror(false).addBox(-8.9522F, -86.7842F, 4.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 153).mirror(false).addBox(-4.9522F, -86.7842F, 4.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(13, 116).mirror(false).addBox(-0.9522F, -86.7842F, 4.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(138, 77).mirror(false).addBox(0.0478F, -86.7842F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(138, 73).mirror(false).addBox(0.0478F, -86.7842F, 9.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(67, 135).mirror(false).addBox(0.0478F, -86.7842F, 13.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(63, 135).mirror(false).addBox(0.0478F, -86.7842F, 17.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(88, 240).mirror(false).addBox(0.0478F, -88.7842F, 5.0000F, 1.0000F, 2.0000F, 13.0000F)
                .texOffs(161, 18).mirror(false).addBox(-13.9522F, -86.7842F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(141, 180).mirror(false).addBox(-12.9522F, -88.7842F, 5.0000F, 13.0000F, 5.0000F, 13.0000F)
                .texOffs(172, 242).mirror(false).addBox(-9.9522F, -83.7842F, 8.0000F, 7.0000F, 3.0000F, 7.0000F)
                , PartPose.offsetAndRotation(-24.0000F, 74.0000F, -12.0000F, 0.0000F, 0.0000F, 0.4363F));

        PartDefinition part_RIGHT_ARM = part_CABEZA.addOrReplaceChild("RIGHT_ARM",
                CubeListBuilder.create(), PartPose.offsetAndRotation(19.0000F, 32.0000F, 5.0000F, 0.0000F, 0.0000F, -3.1416F));

        PartDefinition part_RRuinShoulder = part_RIGHT_ARM.addOrReplaceChild("RRuinShoulder",
                CubeListBuilder.create(), PartPose.offsetAndRotation(2.0000F, 5.0000F, 0.0000F, 0.0000F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r23 = part_RRuinShoulder.addOrReplaceChild("cube_r23",
                CubeListBuilder.create()
                .texOffs(47, 185).mirror(false).addBox(-20.0000F, -61.0000F, -51.0000F, 10.0000F, 14.0000F, 11.0000F)
                , PartPose.offsetAndRotation(-20.5061F, 64.3467F, -12.0000F, -0.7854F, 0.0000F, 0.4363F));

        PartDefinition part_cube_r24 = part_RRuinShoulder.addOrReplaceChild("cube_r24",
                CubeListBuilder.create()
                .texOffs(185, 111).mirror(false).addBox(-26.0000F, -76.0000F, 2.0000F, 23.0000F, 14.0000F, 1.0000F)
                .texOffs(136, 92).mirror(false).addBox(-22.4428F, -80.4887F, 4.0000F, 15.0000F, 4.0000F, 15.0000F)
                .texOffs(0, 58).mirror(false).addBox(-27.0000F, -77.0000F, 3.0000F, 25.0000F, 16.0000F, 17.0000F)
                .texOffs(193, 192).mirror(false).addBox(-26.0000F, -76.0000F, 20.0000F, 23.0000F, 14.0000F, 1.0000F)
                , PartPose.offsetAndRotation(-20.5061F, 64.3467F, -12.0000F, 0.0000F, 0.0000F, 0.4363F));

        PartDefinition part_RRuinMid = part_RRuinShoulder.addOrReplaceChild("RRuinMid",
                CubeListBuilder.create()
                .texOffs(94, 18).mirror(false).addBox(-26.9117F, -3.4020F, -5.0000F, 29.0000F, 9.0000F, 9.0000F)
                .texOffs(239, 135).mirror(false).addBox(-9.9117F, -4.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(0, 238).mirror(false).addBox(-14.9117F, -4.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(236, 164).mirror(false).addBox(-19.9117F, -4.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(234, 22).mirror(false).addBox(-24.9117F, -4.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(180, 252).mirror(false).addBox(-24.9117F, -7.4020F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(71, 252).mirror(false).addBox(-19.9117F, -7.4020F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(55, 252).mirror(false).addBox(-14.9117F, -7.4020F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(39, 252).mirror(false).addBox(-9.9117F, -7.4020F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                , PartPose.offsetAndRotation(-12.5061F, 0.3467F, 0.0000F, 0.0000F, 0.0000F, -0.7854F));

        PartDefinition part_RRuinLower = part_RRuinMid.addOrReplaceChild("RRuinLower",
                CubeListBuilder.create(), PartPose.offset(-25.0000F, 0.0000F, 0.0000F));

        PartDefinition part_cube_r25 = part_RRuinLower.addOrReplaceChild("cube_r25",
                CubeListBuilder.create()
                .texOffs(111, 253).mirror(false).addBox(21.0000F, -47.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(127, 253).mirror(false).addBox(16.0000F, -47.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(97, 212).mirror(false).addBox(4.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(240, 233).mirror(false).addBox(1.0000F, -47.0000F, -6.0000F, 11.0000F, 3.0000F, 3.0000F)
                .texOffs(193, 242).mirror(false).addBox(1.0000F, -47.0000F, 2.0000F, 11.0000F, 3.0000F, 3.0000F)
                .texOffs(185, 138).mirror(false).addBox(0.0000F, -47.0000F, -3.0000F, 4.0000F, 3.0000F, 5.0000F)
                .texOffs(243, 198).mirror(false).addBox(2.0000F, -49.0000F, -3.0000F, 8.0000F, 2.0000F, 5.0000F)
                .texOffs(114, 212).mirror(false).addBox(7.0000F, -47.0000F, -3.0000F, 4.0000F, 3.0000F, 5.0000F)
                .texOffs(0, 66).mirror(false).addBox(4.0000F, -47.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(143, 253).mirror(false).addBox(11.0000F, -47.0000F, -3.0000F, 3.0000F, 3.0000F, 5.0000F)
                .texOffs(45, 230).mirror(false).addBox(11.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(73, 230).mirror(false).addBox(16.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(114, 231).mirror(false).addBox(21.0000F, -44.0000F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(157, 213).mirror(false).addBox(3.0000F, -43.0000F, -5.0000F, 7.0000F, 9.0000F, 9.0000F, new CubeDeformation(-0.0200F))
                .texOffs(174, 146).mirror(false).addBox(10.0000F, -43.0000F, -5.0000F, 19.0000F, 9.0000F, 9.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-25.9117F, 39.5980F, -12.0000F, 0.0000F, -0.4363F, 0.0000F));

        PartDefinition part_RPod = part_RRuinShoulder.addOrReplaceChild("RPod",
                CubeListBuilder.create(), PartPose.offset(2.0000F, -10.0000F, 0.0000F));

        PartDefinition part_cube_r26 = part_RPod.addOrReplaceChild("cube_r26",
                CubeListBuilder.create()
                .texOffs(184, 178).mirror(false).addBox(-21.4428F, -89.4887F, 5.0000F, 13.0000F, 1.0000F, 13.0000F)
                .texOffs(11, 58).mirror(false).addBox(-9.4428F, -86.4887F, 18.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(134, 73).mirror(false).addBox(-13.4428F, -86.4887F, 18.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(101, 128).mirror(false).addBox(-8.4428F, -86.4887F, 17.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(90, 128).mirror(false).addBox(-8.4428F, -86.4887F, 13.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(83, 125).mirror(false).addBox(-8.4428F, -86.4887F, 9.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(75, 125).mirror(false).addBox(-8.4428F, -86.4887F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(102, 163).mirror(false).addBox(-8.4428F, -88.4887F, 5.0000F, 1.0000F, 2.0000F, 13.0000F)
                .texOffs(14, 131).mirror(false).addBox(-22.4428F, -86.4887F, 13.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(75, 131).mirror(false).addBox(-22.4428F, -86.4887F, 9.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(83, 131).mirror(false).addBox(-22.4428F, -86.4887F, 5.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(228, 194).mirror(false).addBox(-22.4428F, -88.4887F, 5.0000F, 1.0000F, 2.0000F, 13.0000F)
                .texOffs(133, 110).mirror(false).addBox(-22.4428F, -86.4887F, 17.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(133, 114).mirror(false).addBox(-17.4428F, -86.4887F, 18.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(8, 9).mirror(false).addBox(-22.4428F, -86.4887F, 18.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(90, 125).mirror(false).addBox(-22.4428F, -88.4887F, 18.0000F, 15.0000F, 2.0000F, 1.0000F)
                .texOffs(134, 77).mirror(false).addBox(-17.4428F, -86.4887F, 4.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(49, 135).mirror(false).addBox(-13.4428F, -86.4887F, 4.0000F, 1.0000F, 3.0000F, 1.0000F)
                .texOffs(11, 66).mirror(false).addBox(-9.4428F, -86.4887F, 4.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 116).mirror(false).addBox(-22.4428F, -86.4887F, 4.0000F, 2.0000F, 3.0000F, 1.0000F)
                .texOffs(158, 140).mirror(false).addBox(-22.4428F, -88.4887F, 4.0000F, 15.0000F, 2.0000F, 1.0000F)
                .texOffs(89, 179).mirror(false).addBox(-21.4428F, -88.4887F, 5.0000F, 13.0000F, 5.0000F, 13.0000F)
                .texOffs(133, 111).mirror(false).addBox(-18.4428F, -83.4887F, 8.0000F, 7.0000F, 3.0000F, 7.0000F)
                , PartPose.offsetAndRotation(-22.5061F, 74.3467F, -12.0000F, 0.0000F, 0.0000F, 0.4363F));

        PartDefinition part_RuinPelvis = part_RuinGuard.addOrReplaceChild("RuinPelvis",
                CubeListBuilder.create()
                .texOffs(170, 22).mirror(false).addBox(-10.0000F, -1.0000F, -5.0000F, 18.0000F, 1.0000F, 11.0000F)
                .texOffs(27, 116).mirror(false).addBox(-5.0000F, -6.0000F, -5.0000F, 8.0000F, 5.0000F, 11.0000F)
                , PartPose.offsetAndRotation(-1.0000F, -37.0000F, 6.0000F, 0.0000F, 0.0000F, -3.1416F));

        PartDefinition part_cube_r27 = part_RuinPelvis.addOrReplaceChild("cube_r27",
                CubeListBuilder.create()
                .texOffs(0, 50).mirror(false).addBox(10.0000F, -12.0000F, 0.0000F, 6.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                .texOffs(27, 135).mirror(false).addBox(8.0000F, -15.0000F, -12.0000F, 10.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                .texOffs(138, 45).mirror(false).addBox(10.0000F, -12.0000F, -12.0000F, 5.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-4.0000F, 16.0000F, 6.0000F, 0.0000F, 0.0000F, -0.7854F));

        PartDefinition part_cube_r28 = part_RuinPelvis.addOrReplaceChild("cube_r28",
                CubeListBuilder.create()
                .texOffs(90, 136).mirror(false).addBox(-11.0000F, -16.0000F, 0.0000F, 5.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                .texOffs(67, 71).mirror(false).addBox(-15.0000F, -19.0000F, -12.0000F, 11.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                .texOffs(79, 13).mirror(false).addBox(-12.0000F, -16.0000F, -12.0000F, 6.0000F, 1.0000F, 1.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-4.0000F, 16.0000F, 6.0000F, 0.0000F, 0.0000F, 0.7854F));

        PartDefinition part_RIGHT_LEG = part_RuinPelvis.addOrReplaceChild("RIGHT_LEG",
                CubeListBuilder.create()
                .texOffs(59, 107).mirror(false).addBox(-1.9117F, -4.4020F, -5.0000F, 25.0000F, 9.0000F, 9.0000F)
                .texOffs(27, 116).mirror(false).addBox(21.0883F, -4.4020F, -7.0000F, 2.0000F, 9.0000F, 2.0000F)
                .texOffs(223, 83).mirror(false).addBox(10.0883F, -5.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(206, 220).mirror(false).addBox(5.0883F, -5.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(178, 220).mirror(false).addBox(0.0883F, -5.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                , PartPose.offsetAndRotation(7.4939F, -2.6533F, 2.0000F, 0.0000F, 0.0000F, -1.5708F));

        PartDefinition part_RRuinLegBottom = part_RIGHT_LEG.addOrReplaceChild("RRuinLegBottom",
                CubeListBuilder.create()
                .texOffs(152, 36).mirror(false).addBox(-0.9117F, -4.4020F, -5.0000F, 20.0000F, 9.0000F, 9.0000F)
                .texOffs(223, 178).mirror(false).addBox(-8.9117F, -1.4020F, 10.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 131).mirror(false).addBox(-7.9117F, -1.4020F, 9.0000F, 6.0000F, 3.0000F, 1.0000F)
                .texOffs(0, 172).mirror(false).addBox(-6.9117F, -1.4020F, 8.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(27, 172).mirror(false).addBox(-2.9117F, -1.4020F, 7.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(246, 129).mirror(false).addBox(-0.9117F, -1.4020F, 6.0000F, 13.0000F, 3.0000F, 1.0000F)
                .texOffs(196, 105).mirror(false).addBox(6.0883F, -1.4020F, 4.0000F, 13.0000F, 3.0000F, 2.0000F)
                .texOffs(130, 163).mirror(false).addBox(2.0883F, -6.4020F, -5.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(174, 164).mirror(false).addBox(0.0883F, -6.4020F, -7.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(141, 180).mirror(false).addBox(2.0883F, 4.5980F, -5.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(174, 170).mirror(false).addBox(0.0883F, 4.5980F, -7.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(59, 91).mirror(false).addBox(2.0883F, -6.4020F, -7.0000F, 2.0000F, 13.0000F, 2.0000F)
                .texOffs(210, 34).mirror(false).addBox(4.0883F, -6.4020F, -7.0000F, 2.0000F, 13.0000F, 13.0000F)
                .texOffs(133, 121).mirror(false).addBox(-7.9117F, -2.4020F, -9.0000F, 7.0000F, 5.0000F, 2.0000F)
                .texOffs(140, 36).mirror(false).addBox(-0.9117F, -3.4020F, -9.0000F, 7.0000F, 7.0000F, 2.0000F)
                .texOffs(117, 163).mirror(false).addBox(6.0883F, -1.4020F, -9.0000F, 3.0000F, 3.0000F, 2.0000F)
                .texOffs(236, 157).mirror(false).addBox(6.0883F, -1.4020F, -7.0000F, 13.0000F, 3.0000F, 2.0000F)
                .texOffs(170, 0).mirror(false).addBox(4.0883F, -5.4020F, -6.0000F, 15.0000F, 11.0000F, 11.0000F)
                , PartPose.offset(21.0000F, 0.0000F, 0.0000F));

        PartDefinition part_cube_r29 = part_RRuinLegBottom.addOrReplaceChild("cube_r29",
                CubeListBuilder.create()
                .texOffs(158, 111).mirror(false).addBox(32.0000F, -33.0000F, 41.0000F, 2.0000F, 1.0000F, 23.0000F, new CubeDeformation(-0.0200F))
                .texOffs(48, 125).mirror(false).addBox(32.0000F, -44.0000F, 41.0000F, 2.0000F, 11.0000F, 23.0000F, new CubeDeformation(-0.0200F))
                .texOffs(147, 156).mirror(false).addBox(32.0000F, -45.0000F, 41.0000F, 2.0000F, 1.0000F, 23.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-46.9117F, 38.5980F, -12.0000F, 0.0000F, 0.7854F, 0.0000F));

        PartDefinition part_RRuinLegFoot2 = part_RRuinLegBottom.addOrReplaceChild("RRuinLegFoot2",
                CubeListBuilder.create()
                .texOffs(106, 107).mirror(false).addBox(0.0000F, -7.0000F, -10.0000F, 3.0000F, 16.0000F, 21.0000F)
                .texOffs(201, 34).mirror(false).addBox(-1.0000F, -7.0000F, -17.0000F, 4.0000F, 4.0000F, 7.0000F)
                .texOffs(91, 163).mirror(false).addBox(-1.0000F, 5.0000F, -17.0000F, 4.0000F, 4.0000F, 7.0000F)
                .texOffs(48, 135).mirror(false).addBox(-1.0000F, -1.0000F, -17.0000F, 4.0000F, 4.0000F, 7.0000F)
                .texOffs(154, 111).mirror(false).addBox(-2.0000F, -1.0000F, -10.0000F, 2.0000F, 4.0000F, 3.0000F)
                , PartPose.offset(19.0000F, -1.0000F, 0.0000F));

        PartDefinition part_LEFT_LEG = part_RuinPelvis.addOrReplaceChild("LEFT_LEG",
                CubeListBuilder.create()
                .texOffs(0, 98).mirror(false).addBox(-1.9117F, -4.4020F, -5.0000F, 25.0000F, 9.0000F, 9.0000F)
                .texOffs(219, 153).mirror(false).addBox(10.0883F, -5.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(217, 11).mirror(false).addBox(5.0883F, -5.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                .texOffs(0, 214).mirror(false).addBox(0.0883F, -5.4020F, -6.0000F, 3.0000F, 11.0000F, 11.0000F)
                , PartPose.offsetAndRotation(-9.5061F, -2.6533F, 2.0000F, 0.0000F, 0.0000F, -1.5708F));

        PartDefinition part_cube_r30 = part_LEFT_LEG.addOrReplaceChild("cube_r30",
                CubeListBuilder.create()
                .texOffs(131, 121).mirror(false).addBox(32.0000F, -44.0000F, 41.0000F, 2.0000F, 11.0000F, 23.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-25.9117F, 38.5980F, -12.0000F, 0.0000F, 0.7854F, 0.0000F));

        PartDefinition part_LRuinLegBottom2 = part_LEFT_LEG.addOrReplaceChild("LRuinLegBottom2",
                CubeListBuilder.create()
                .texOffs(134, 73).mirror(false).addBox(0.0883F, -4.4020F, -5.0000F, 20.0000F, 9.0000F, 9.0000F)
                .texOffs(223, 186).mirror(false).addBox(7.0883F, -1.4020F, 4.0000F, 13.0000F, 3.0000F, 2.0000F)
                .texOffs(250, 112).mirror(false).addBox(0.0883F, -1.4020F, 6.0000F, 13.0000F, 3.0000F, 1.0000F)
                .texOffs(78, 185).mirror(false).addBox(-1.9117F, -1.4020F, 7.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(147, 174).mirror(false).addBox(-5.9117F, -1.4020F, 8.0000F, 10.0000F, 3.0000F, 1.0000F)
                .texOffs(152, 54).mirror(false).addBox(-6.9117F, -1.4020F, 9.0000F, 6.0000F, 3.0000F, 1.0000F)
                .texOffs(223, 182).mirror(false).addBox(-7.9117F, -1.4020F, 10.0000F, 4.0000F, 3.0000F, 1.0000F)
                .texOffs(50, 163).mirror(false).addBox(5.0883F, -5.4020F, -6.0000F, 15.0000F, 11.0000F, 11.0000F)
                .texOffs(29, 201).mirror(false).addBox(5.0883F, -6.4020F, -7.0000F, 2.0000F, 13.0000F, 13.0000F)
                .texOffs(90, 234).mirror(false).addBox(7.0883F, -1.4020F, -7.0000F, 13.0000F, 3.0000F, 2.0000F)
                .texOffs(160, 155).mirror(false).addBox(7.0883F, -1.4020F, -9.0000F, 3.0000F, 3.0000F, 2.0000F)
                .texOffs(79, 0).mirror(false).addBox(0.0883F, -3.4020F, -9.0000F, 7.0000F, 7.0000F, 2.0000F)
                .texOffs(148, 213).mirror(false).addBox(-6.9117F, -2.4020F, -9.0000F, 7.0000F, 5.0000F, 2.0000F)
                .texOffs(161, 18).mirror(false).addBox(3.0883F, 4.5980F, -5.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(27, 153).mirror(false).addBox(1.0883F, 4.5980F, -7.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(0, 0).mirror(false).addBox(3.0883F, -6.4020F, -7.0000F, 2.0000F, 13.0000F, 2.0000F)
                .texOffs(0, 39).mirror(false).addBox(1.0883F, -4.4020F, -7.0000F, 2.0000F, 9.0000F, 2.0000F)
                .texOffs(75, 125).mirror(false).addBox(3.0883F, -6.4020F, -5.0000F, 2.0000F, 2.0000F, 4.0000F)
                .texOffs(75, 131).mirror(false).addBox(1.0883F, -6.4020F, -7.0000F, 2.0000F, 2.0000F, 4.0000F)
                , PartPose.offset(20.0000F, 0.0000F, 0.0000F));

        PartDefinition part_cube_r31 = part_LRuinLegBottom2.addOrReplaceChild("cube_r31",
                CubeListBuilder.create()
                .texOffs(0, 153).mirror(false).addBox(32.0000F, -33.0000F, 41.0000F, 2.0000F, 1.0000F, 23.0000F, new CubeDeformation(-0.0200F))
                .texOffs(120, 155).mirror(false).addBox(32.0000F, -45.0000F, 41.0000F, 2.0000F, 1.0000F, 23.0000F, new CubeDeformation(-0.0200F))
                , PartPose.offsetAndRotation(-45.9117F, 38.5980F, -12.0000F, 0.0000F, 0.7854F, 0.0000F));

        PartDefinition part_LRuinLegFoot = part_LRuinLegBottom2.addOrReplaceChild("LRuinLegFoot",
                CubeListBuilder.create()
                .texOffs(0, 116).mirror(false).addBox(0.0000F, -7.0000F, -10.0000F, 3.0000F, 16.0000F, 21.0000F)
                .texOffs(62, 230).mirror(false).addBox(-1.0000F, -7.0000F, -17.0000F, 4.0000F, 4.0000F, 7.0000F)
                .texOffs(34, 227).mirror(false).addBox(-1.0000F, 5.0000F, -17.0000F, 4.0000F, 4.0000F, 7.0000F)
                .texOffs(195, 220).mirror(false).addBox(-1.0000F, -1.0000F, -17.0000F, 4.0000F, 4.0000F, 7.0000F)
                .texOffs(13, 153).mirror(false).addBox(-2.0000F, -1.0000F, -10.0000F, 2.0000F, 4.0000F, 3.0000F)
                , PartPose.offset(20.0000F, -1.0000F, 0.0000F));

        return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(RuinGraderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isStunned()) {
            this.ruinGuard.y = 19.0F;
            this.leftLeg.yRot = -1.2F;
            this.rightLeg.yRot = -1.2F;
            this.leftArm.xRot = 0.8F;
            this.rightArm.xRot = 0.8F;
        } else if (entity.isFiringLaser()) {
            this.ruinGuard.y = 15.0F;
            this.ruinGuard.xRot = 0.15F;
            this.leftArm.xRot = -0.5F;
            this.rightArm.xRot = -0.5F;
            this.leftLeg.yRot = 0.0F;
            this.rightLeg.yRot = 0.0F;
        } else {
            this.ruinGuard.y = 15.0F;
            this.ruinGuard.xRot = 0.0F;
            this.leftLeg.yRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
            this.rightLeg.yRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.5F * limbSwingAmount;
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
