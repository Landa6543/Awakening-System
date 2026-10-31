package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.GiantSandwormHeadPart;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GiantSandwormHeadModel extends EntityModel<GiantSandwormHeadPart> {
    private final ModelPart head;

    public GiantSandwormHeadModel(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -2.563F));

        PartDefinition body_segment = head.addOrReplaceChild("body_segment", CubeListBuilder.create().texOffs(0, 31).addBox(-4.0F, 5.0F, -2.5F, 8.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-4.0F, -6.0F, -2.5F, 8.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(36, 38).addBox(-6.0F, -4.0F, -2.5F, 1.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(36, 20).addBox(5.0F, -4.0F, -2.5F, 1.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -5.0F, -2.5F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 42).addBox(5.0F, -3.0F, -7.5F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 42).addBox(-6.0F, -3.0F, -7.5F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = mouth.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 53).addBox(-0.5F, -2.125F, -2.5F, 1.0F, 4.25F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1227F, 4.1702F, -5.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r2 = mouth.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(52, 9).addBox(-0.5F, -2.125F, -2.5F, 1.0F, 4.25F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1581F, -4.1591F, -5.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r3 = mouth.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(40, 11).addBox(-0.5F, -2.125F, -2.5F, 1.0F, 4.25F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1227F, 4.1702F, -5.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r4 = mouth.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(52, 0).addBox(-0.5F, -2.125F, -2.5F, 1.0F, 4.25F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1581F, -4.1591F, -5.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r5 = mouth.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(12, 42).addBox(-0.5F, -3.0F, -2.5F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, -5.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r6 = mouth.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -3.0F, -2.5F, 1.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5F, -5.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition back_teeth = head.addOrReplaceChild("back_teeth", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -5.2383F, 0.0F, 0.0F, -0.2182F));

        PartDefinition group29 = back_teeth.addOrReplaceChild("group29", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0879F, 3.4312F, 0.034F, 0.0F, 0.0F, 0.2182F));

        PartDefinition cube_r7 = group29.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(74, 47).addBox(-0.25F, -0.0778F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, -1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r8 = group29.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(40, 68).addBox(-0.25F, -0.1543F, -0.319F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition group31 = back_teeth.addOrReplaceChild("group31", CubeListBuilder.create(), PartPose.offsetAndRotation(2.3176F, -1.0985F, 0.034F, -0.0394F, 0.0812F, -0.3062F));

        PartDefinition cube_r9 = group31.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(16, 63).addBox(-0.25F, -1.75F, -1.3F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.181F, 1.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r10 = group31.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(22, 58).addBox(-0.25F, -0.2F, 0.2F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.119F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group32 = back_teeth.addOrReplaceChild("group32", CubeListBuilder.create(), PartPose.offsetAndRotation(2.4176F, -0.2985F, 0.034F, 0.0154F, -0.1739F, -0.0886F));

        PartDefinition cube_r11 = group32.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(74, 49).addBox(-0.25F, -0.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.119F, 0.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r12 = group32.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(12, 53).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.319F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group33 = back_teeth.addOrReplaceChild("group33", CubeListBuilder.create(), PartPose.offsetAndRotation(2.4176F, 0.7015F, 0.034F, -0.1309F, 0.0F, 0.0873F));

        PartDefinition cube_r13 = group33.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(54, 74).addBox(-0.25F, 0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.219F, -0.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r14 = group33.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(58, 23).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.319F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group35 = back_teeth.addOrReplaceChild("group35", CubeListBuilder.create(), PartPose.offset(-2.0015F, 0.6515F, -0.0617F));

        PartDefinition cube_r15 = group35.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(52, 18).addBox(-0.3F, 0.25F, -1.4F, 0.5F, 0.5F, 1.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -0.45F, 0.0F, -3.1416F, -1.1781F, 3.0718F));

        PartDefinition cube_r16 = group35.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(58, 27).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, -1.5708F, -0.0698F));

        PartDefinition group36 = back_teeth.addOrReplaceChild("group36", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3205F, -0.2985F, 0.034F, 0.0436F, 0.0F, 0.0873F));

        PartDefinition cube_r17 = group36.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(58, 74).addBox(-0.25F, -0.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.219F, 0.7F, -0.0957F, 0.0F, -1.9635F, 0.0F));

        PartDefinition cube_r18 = group36.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(58, 31).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.319F, 0.15F, -0.0957F, 0.0F, -1.5708F, 0.0F));

        PartDefinition group37 = back_teeth.addOrReplaceChild("group37", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3205F, -0.8985F, 0.034F, 0.0F, 0.0F, 0.1571F));

        PartDefinition cube_r19 = group37.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(8, 62).addBox(-0.25F, -1.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.219F, 1.5F, -0.0957F, 0.0F, -1.9635F, 0.0F));

        PartDefinition cube_r20 = group37.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(58, 35).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.319F, -0.05F, -0.0957F, 0.0F, -1.5708F, 0.0F));

        PartDefinition group38 = back_teeth.addOrReplaceChild("group38", CubeListBuilder.create(), PartPose.offsetAndRotation(2.795F, 2.7241F, 0.034F, 0.0F, 0.0F, 0.1309F));

        PartDefinition cube_r21 = group38.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(64, 16).addBox(1.6945F, -0.8219F, 0.3308F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, -1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r22 = group38.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(56, 71).addBox(1.6945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition group30 = back_teeth.addOrReplaceChild("group30", CubeListBuilder.create(), PartPose.offset(-1.155F, 2.4241F, 0.034F));

        PartDefinition cube_r23 = group30.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(36, 72).addBox(-3.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r24 = group30.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(14, 67).addBox(-3.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition group39 = back_teeth.addOrReplaceChild("group39", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5121F, 1.917F, 0.034F, -0.0915F, -0.3042F, 0.1584F));

        PartDefinition cube_r25 = group39.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(56, 18).addBox(-3.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, 0.7071F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r26 = group39.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(24, 72).addBox(-3.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, 0.7071F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group57 = back_teeth.addOrReplaceChild("group57", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.3028F, 3.292F, -0.0563F, -0.0596F, -0.0365F, -0.5487F));

        PartDefinition cube_r27 = group57.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(74, 35).addBox(-1.1945F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r28 = group57.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(66, 51).addBox(-1.1945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group40 = back_teeth.addOrReplaceChild("group40", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.8003F, 3.3616F, -0.0612F, -0.0696F, -0.0049F, -0.0696F));

        PartDefinition cube_r29 = group40.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(74, 33).addBox(-1.1945F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r30 = group40.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(56, 65).addBox(-1.1945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group41 = back_teeth.addOrReplaceChild("group41", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.105F, 2.7241F, 0.034F, -0.0876F, -0.0869F, -0.0796F));

        PartDefinition cube_r31 = group41.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(56, 68).addBox(-2.1946F, -0.8219F, -0.3692F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r32 = group41.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(72, 0).addBox(-2.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group42 = back_teeth.addOrReplaceChild("group42", CubeListBuilder.create(), PartPose.offset(0.045F, -2.7211F, 0.034F));

        PartDefinition cube_r33 = group42.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(74, 60).addBox(-0.25F, -0.4222F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7571F, 0.6071F, 0.0F, 1.9635F, 0.0F, 1.2915F));

        PartDefinition cube_r34 = group42.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(68, 66).addBox(-0.25F, -0.3457F, -0.319F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7571F, 0.6071F, 0.0F, 1.5708F, 0.0F, 1.2915F));

        PartDefinition group46 = back_teeth.addOrReplaceChild("group46", CubeListBuilder.create(), PartPose.offset(0.245F, -4.0211F, 0.034F));

        PartDefinition cube_r35 = group46.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(30, 69).addBox(1.6945F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r36 = group46.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(9, 63).addBox(1.6945F, -0.3457F, 1.6255F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition group43 = back_teeth.addOrReplaceChild("group43", CubeListBuilder.create(), PartPose.offset(0.045F, -2.7211F, 0.034F));

        PartDefinition cube_r37 = group43.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(74, 66).addBox(1.6945F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r38 = group43.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(72, 3).addBox(1.6945F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition group45 = back_teeth.addOrReplaceChild("group45", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.4121F, -2.014F, 0.034F, 0.1745F, 0.0873F, -0.3054F));

        PartDefinition cube_r39 = group45.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(32, 53).addBox(-3.1946F, 0.3219F, -0.3692F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, -0.7071F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r40 = group45.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(28, 72).addBox(-3.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, -0.7071F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group34 = back_teeth.addOrReplaceChild("group34", CubeListBuilder.create(), PartPose.offset(0.145F, -3.9211F, 0.034F));

        PartDefinition cube_r41 = group34.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(20, 75).addBox(-2.1946F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r42 = group34.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(0, 78).addBox(-2.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group47 = back_teeth.addOrReplaceChild("group47", CubeListBuilder.create(), PartPose.offset(0.045F, -2.7211F, 0.034F));

        PartDefinition cube_r43 = group47.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(74, 68).addBox(-2.1946F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r44 = group47.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(72, 6).addBox(-2.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group48 = back_teeth.addOrReplaceChild("group48", CubeListBuilder.create(), PartPose.offsetAndRotation(3.5021F, 2.017F, 0.034F, 0.0F, 0.0F, -0.2182F));

        PartDefinition cube_r45 = group48.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(20, 69).addBox(-0.25F, -0.0778F, -1.9157F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1633F, -0.1633F, 0.0957F, -1.9635F, 0.0F, -0.9425F));

        PartDefinition cube_r46 = group48.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(14, 69).addBox(-0.25F, -0.0587F, -0.0881F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1633F, -0.1633F, 0.0957F, -1.5708F, 0.0F, -0.9425F));

        PartDefinition group49 = back_teeth.addOrReplaceChild("group49", CubeListBuilder.create(), PartPose.offset(-1.5015F, 2.0515F, -0.0617F));

        PartDefinition cube_r47 = group49.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(74, 21).addBox(-1.75F, -0.25F, -1.3F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.55F, 0.1F, -0.1F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r48 = group49.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(58, 38).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.1F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group50 = back_teeth.addOrReplaceChild("group50", CubeListBuilder.create(), PartPose.offset(-0.5015F, 2.0515F, -0.0617F));

        PartDefinition cube_r49 = group50.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(74, 23).addBox(-0.75F, -0.25F, -1.3F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, 0.5F, 0.0F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r50 = group50.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(58, 42).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group51 = back_teeth.addOrReplaceChild("group51", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5485F, 2.3705F, 0.034F, -0.0869F, -0.0076F, -0.0869F));

        PartDefinition cube_r51 = group51.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(74, 70).addBox(0.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.219F, -0.0957F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r52 = group51.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(22, 53).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, -0.319F, -0.0957F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group52 = back_teeth.addOrReplaceChild("group52", CubeListBuilder.create(), PartPose.offsetAndRotation(1.2985F, 2.0515F, -0.0617F, 0.0F, 0.0F, -0.0873F));

        PartDefinition cube_r53 = group52.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(74, 72).addBox(1.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.45F, 0.1F, 0.0F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r54 = group52.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(32, 56).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group53 = back_teeth.addOrReplaceChild("group53", CubeListBuilder.create(), PartPose.offset(-1.5015F, -2.0485F, -0.0617F));

        PartDefinition cube_r55 = group53.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(74, 74).addBox(-1.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.55F, -0.1F, 0.0F, 1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r56 = group53.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(42, 56).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition group54 = back_teeth.addOrReplaceChild("group54", CubeListBuilder.create(), PartPose.offset(-0.5015F, -2.0485F, -0.0617F));

        PartDefinition cube_r57 = group54.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(12, 75).addBox(-0.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, -0.1F, 0.1F, 1.9635F, -0.1396F, 0.0F));

        PartDefinition cube_r58 = group54.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(58, 46).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, -0.1396F, 0.0F));

        PartDefinition group55 = back_teeth.addOrReplaceChild("group55", CubeListBuilder.create(), PartPose.offset(0.0485F, -2.1485F, -0.0617F));

        PartDefinition cube_r59 = group55.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(58, 50).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.45F, 0.1F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r60 = group55.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(16, 75).addBox(0.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.0F));

        PartDefinition group56 = back_teeth.addOrReplaceChild("group56", CubeListBuilder.create(), PartPose.offset(0.0485F, -2.1485F, -0.0617F));

        PartDefinition cube_r61 = group56.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(30, 61).addBox(-0.25F, -0.3457F, -0.319F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.219F, 0.0957F, 1.6611F, -0.2608F, -0.0233F));

        PartDefinition cube_r62 = group56.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(74, 25).addBox(-0.25F, -0.4222F, -1.4657F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.219F, 0.0957F, 2.0538F, -0.2608F, -0.0233F));

        PartDefinition mid_teeth = head.addOrReplaceChild("mid_teeth", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -2.3753F, 0.0F, 0.0F, -0.0873F));

        PartDefinition group44 = mid_teeth.addOrReplaceChild("group44", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0485F, 3.4338F, 0.034F, 0.0F, 0.0F, 0.2182F));

        PartDefinition cube_r63 = group44.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(38, 74).addBox(-0.25F, -0.0778F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, -1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r64 = group44.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(0, 69).addBox(-0.25F, -0.1543F, -0.319F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition group58 = mid_teeth.addOrReplaceChild("group58", CubeListBuilder.create(), PartPose.offsetAndRotation(2.3782F, -1.0959F, 0.034F, -0.0394F, 0.0812F, -0.3062F));

        PartDefinition cube_r65 = group58.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(66, 52).addBox(-0.25F, -1.75F, -1.4F, 0.5F, 0.5F, 1.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.181F, 1.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r66 = group58.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(12, 61).addBox(-0.25F, -0.2F, 0.2F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.119F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group59 = mid_teeth.addOrReplaceChild("group59", CubeListBuilder.create(), PartPose.offsetAndRotation(2.3782F, -0.2959F, 0.034F, 0.0154F, -0.1739F, -0.0886F));

        PartDefinition cube_r67 = group59.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(42, 74).addBox(-0.25F, -0.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.219F, 0.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r68 = group59.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(52, 56).addBox(-0.25F, -0.2F, 0.07F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.419F, -0.05F, -0.0657F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group60 = mid_teeth.addOrReplaceChild("group60", CubeListBuilder.create(), PartPose.offsetAndRotation(2.3782F, 0.7041F, 0.034F, -0.1309F, 0.0F, 0.0873F));

        PartDefinition cube_r69 = group60.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(46, 74).addBox(-0.25F, 0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.219F, -0.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r70 = group60.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(30, 61).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.319F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group61 = mid_teeth.addOrReplaceChild("group61", CubeListBuilder.create(), PartPose.offset(-2.0408F, 0.6541F, -0.0617F));

        PartDefinition cube_r71 = group61.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(50, 74).addBox(-0.3F, 0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -0.45F, 0.0F, -3.1416F, -1.1781F, 3.0718F));

        PartDefinition cube_r72 = group61.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(38, 61).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, 0.0F, -1.5708F, -0.0698F));

        PartDefinition group62 = mid_teeth.addOrReplaceChild("group62", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3598F, -0.2959F, 0.034F, 0.0436F, 0.0F, 0.0873F));

        PartDefinition cube_r73 = group62.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(54, 74).addBox(-0.25F, -0.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.219F, 0.7F, -0.0957F, 0.0F, -1.9635F, 0.0F));

        PartDefinition cube_r74 = group62.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(46, 61).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.319F, 0.15F, -0.0957F, 0.0F, -1.5708F, 0.0F));

        PartDefinition group63 = mid_teeth.addOrReplaceChild("group63", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3598F, -0.8959F, 0.034F, 0.0F, 0.0F, 0.1571F));

        PartDefinition cube_r75 = group63.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(74, 67).addBox(-0.25F, -1.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.219F, 1.5F, -0.0957F, 0.0F, -1.9635F, 0.0F));

        PartDefinition cube_r76 = group63.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(54, 61).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.319F, -0.05F, -0.0957F, 0.0F, -1.5708F, 0.0F));

        PartDefinition group64 = mid_teeth.addOrReplaceChild("group64", CubeListBuilder.create(), PartPose.offsetAndRotation(2.7556F, 2.7267F, 0.034F, 0.0F, 0.0F, 0.1309F));

        PartDefinition cube_r77 = group64.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(72, 45).addBox(1.6945F, -0.8219F, 0.3308F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, -1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r78 = group64.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(52, 71).addBox(1.6945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition group65 = mid_teeth.addOrReplaceChild("group65", CubeListBuilder.create(), PartPose.offset(-1.1944F, 2.4267F, 0.034F));

        PartDefinition cube_r79 = group65.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(0, 72).addBox(-3.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r80 = group65.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(70, 74).addBox(-3.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition group66 = mid_teeth.addOrReplaceChild("group66", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5515F, 1.9196F, 0.034F, -0.0915F, -0.3042F, 0.1584F));

        PartDefinition cube_r81 = group66.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(74, 74).addBox(-3.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, 0.7071F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r82 = group66.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(72, 0).addBox(-3.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, 0.7071F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group67 = mid_teeth.addOrReplaceChild("group67", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.3421F, 3.2947F, -0.0563F, -0.0596F, -0.0365F, -0.5487F));

        PartDefinition cube_r83 = group67.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(72, 51).addBox(-1.1945F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r84 = group67.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(6, 69).addBox(-1.1945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group68 = mid_teeth.addOrReplaceChild("group68", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.8397F, 3.3642F, -0.0612F, -0.0696F, -0.0049F, -0.0696F));

        PartDefinition cube_r85 = group68.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(0, 75).addBox(-1.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r86 = group68.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(12, 69).addBox(-1.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group69 = mid_teeth.addOrReplaceChild("group69", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.1444F, 2.7267F, 0.034F, -0.0876F, -0.0869F, -0.0796F));

        PartDefinition cube_r87 = group69.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(18, 69).addBox(-2.1946F, -0.8219F, -0.3692F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r88 = group69.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(6, 72).addBox(-2.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group70 = mid_teeth.addOrReplaceChild("group70", CubeListBuilder.create(), PartPose.offset(0.0056F, -2.7184F, 0.034F));

        PartDefinition cube_r89 = group70.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(4, 75).addBox(-0.25F, -0.4222F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7571F, 0.6071F, 0.0F, 1.9635F, 0.0F, 1.2915F));

        PartDefinition cube_r90 = group70.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(24, 69).addBox(-0.25F, -0.3457F, -0.319F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7571F, 0.6071F, 0.0F, 1.5708F, 0.0F, 1.2915F));

        PartDefinition group71 = mid_teeth.addOrReplaceChild("group71", CubeListBuilder.create(), PartPose.offset(0.0056F, -3.8184F, 0.034F));

        PartDefinition cube_r91 = group71.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(8, 75).addBox(1.6945F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r92 = group71.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(72, 7).addBox(1.6945F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition group72 = mid_teeth.addOrReplaceChild("group72", CubeListBuilder.create(), PartPose.offset(0.0056F, -2.7184F, 0.034F));

        PartDefinition cube_r93 = group72.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(12, 75).addBox(1.6945F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r94 = group72.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(12, 72).addBox(1.6945F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition group73 = mid_teeth.addOrReplaceChild("group73", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.4515F, -2.0113F, 0.034F, 0.1745F, 0.0873F, -0.3054F));

        PartDefinition cube_r95 = group73.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(70, 4).addBox(-3.1946F, 0.3219F, -0.3692F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, -0.7071F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r96 = group73.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(18, 72).addBox(-3.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, -0.7071F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group74 = mid_teeth.addOrReplaceChild("group74", CubeListBuilder.create(), PartPose.offset(0.1056F, -3.9184F, 0.034F));

        PartDefinition cube_r97 = group74.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(16, 75).addBox(-2.1946F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r98 = group74.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(30, 60).addBox(-2.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group75 = mid_teeth.addOrReplaceChild("group75", CubeListBuilder.create(), PartPose.offset(0.0056F, -2.7184F, 0.034F));

        PartDefinition cube_r99 = group75.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(18, 75).addBox(-2.1946F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r100 = group75.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(24, 72).addBox(-2.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group76 = mid_teeth.addOrReplaceChild("group76", CubeListBuilder.create(), PartPose.offsetAndRotation(3.4627F, 2.0196F, 0.034F, 0.0F, 0.0F, -0.2182F));

        PartDefinition cube_r101 = group76.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(70, 13).addBox(-0.25F, -0.0778F, -1.9157F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1633F, -0.1633F, 0.0957F, -1.9635F, 0.0F, -0.9425F));

        PartDefinition cube_r102 = group76.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(70, 10).addBox(-0.25F, -0.0587F, -0.0881F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1633F, -0.1633F, 0.0957F, -1.5708F, 0.0F, -0.9425F));

        PartDefinition group77 = mid_teeth.addOrReplaceChild("group77", CubeListBuilder.create(), PartPose.offset(-1.5408F, 2.0541F, -0.0617F));

        PartDefinition cube_r103 = group77.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(22, 75).addBox(-1.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.55F, 0.1F, -0.1F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r104 = group77.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(66, 22).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.1F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group78 = mid_teeth.addOrReplaceChild("group78", CubeListBuilder.create(), PartPose.offset(-0.5408F, 2.0541F, -0.0617F));

        PartDefinition cube_r105 = group78.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(26, 75).addBox(-0.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, 0.5F, 0.0F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r106 = group78.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(0, 62).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group79 = mid_teeth.addOrReplaceChild("group79", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5092F, 2.3732F, 0.034F, -0.0869F, -0.0076F, -0.0869F));

        PartDefinition cube_r107 = group79.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(58, 75).addBox(0.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.219F, -0.0957F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r108 = group79.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(12, 57).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, -0.319F, -0.0957F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group80 = mid_teeth.addOrReplaceChild("group80", CubeListBuilder.create(), PartPose.offsetAndRotation(1.2592F, 2.0541F, -0.0617F, 0.0F, 0.0F, -0.0873F));

        PartDefinition cube_r109 = group80.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(62, 75).addBox(1.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.45F, 0.1F, 0.0F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r110 = group80.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(58, 18).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group81 = mid_teeth.addOrReplaceChild("group81", CubeListBuilder.create(), PartPose.offset(-1.5408F, -2.0459F, -0.0617F));

        PartDefinition cube_r111 = group81.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(74, 36).addBox(-1.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.55F, -0.1F, 0.0F, 1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r112 = group81.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(20, 62).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition group82 = mid_teeth.addOrReplaceChild("group82", CubeListBuilder.create(), PartPose.offset(-0.5408F, -2.0459F, -0.0617F));

        PartDefinition cube_r113 = group82.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(74, 69).addBox(-0.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, -0.1F, 0.1F, 1.9635F, -0.1396F, 0.0F));

        PartDefinition cube_r114 = group82.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(62, 54).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, -0.1396F, 0.0F));

        PartDefinition group83 = mid_teeth.addOrReplaceChild("group83", CubeListBuilder.create(), PartPose.offset(0.0092F, -2.1459F, -0.0617F));

        PartDefinition cube_r115 = group83.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(62, 57).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.45F, 0.1F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r116 = group83.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(66, 75).addBox(0.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.0F));

        PartDefinition group84 = mid_teeth.addOrReplaceChild("group84", CubeListBuilder.create(), PartPose.offset(0.0092F, -2.1459F, -0.0617F));

        PartDefinition cube_r117 = group84.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(66, 25).addBox(-0.25F, -0.3457F, -0.319F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.219F, 0.0957F, 1.6611F, -0.2608F, -0.0233F));

        PartDefinition cube_r118 = group84.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(76, 3).addBox(-0.25F, -0.4222F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.219F, 0.0957F, 2.0538F, -0.2608F, -0.0233F));

        PartDefinition front_teeth = head.addOrReplaceChild("front_teeth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -6.6383F));

        PartDefinition group2 = front_teeth.addOrReplaceChild("group2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0393F, 3.4297F, 0.034F, 0.0F, 0.0F, 0.2182F));

        PartDefinition cube_r119 = group2.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(56, 76).addBox(-0.25F, -0.0778F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, -1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r120 = group2.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(0, 70).addBox(-0.25F, -0.1543F, -0.319F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition group3 = front_teeth.addOrReplaceChild("group3", CubeListBuilder.create(), PartPose.offsetAndRotation(2.369F, -1.1F, 0.034F, -0.0394F, 0.0812F, -0.3062F));

        PartDefinition cube_r121 = group3.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(78, 2).addBox(-0.25F, -1.75F, -0.8F, 0.5F, 0.5F, 0.8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.181F, 1.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r122 = group3.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(64, 8).addBox(-0.25F, -0.2F, 0.2F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.119F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group4 = front_teeth.addOrReplaceChild("group4", CubeListBuilder.create(), PartPose.offsetAndRotation(2.369F, -0.3F, 0.034F, 0.0154F, -0.1739F, -0.0886F));

        PartDefinition cube_r123 = group4.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(76, 57).addBox(-0.25F, -0.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.119F, 0.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r124 = group4.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(64, 12).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.219F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group5 = front_teeth.addOrReplaceChild("group5", CubeListBuilder.create(), PartPose.offsetAndRotation(2.369F, 0.7F, 0.034F, -0.1309F, 0.0F, 0.0873F));

        PartDefinition cube_r125 = group5.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(76, 62).addBox(-0.25F, 0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.219F, -0.5F, -0.0957F, 0.0F, 1.9635F, 0.0F));

        PartDefinition cube_r126 = group5.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(16, 65).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.319F, -0.05F, -0.0957F, 0.0F, 1.5708F, 0.0F));

        PartDefinition group6 = front_teeth.addOrReplaceChild("group6", CubeListBuilder.create(), PartPose.offset(-2.05F, 0.65F, -0.0617F));

        PartDefinition cube_r127 = group6.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(76, 64).addBox(-0.3F, 0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -0.45F, 0.0F, -3.1416F, -1.1781F, 3.0718F));

        PartDefinition cube_r128 = group6.addOrReplaceChild("cube_r128", CubeListBuilder.create().texOffs(24, 65).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, 0.0F, -1.5708F, -0.0698F));

        PartDefinition group7 = front_teeth.addOrReplaceChild("group7", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.369F, -0.3F, 0.034F, 0.0436F, 0.0F, 0.0873F));

        PartDefinition cube_r129 = group7.addOrReplaceChild("cube_r129", CubeListBuilder.create().texOffs(74, 76).addBox(-0.25F, -0.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.219F, 0.7F, -0.0957F, 0.0F, -1.9635F, 0.0F));

        PartDefinition cube_r130 = group7.addOrReplaceChild("cube_r130", CubeListBuilder.create().texOffs(32, 65).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.319F, 0.15F, -0.0957F, 0.0F, -1.5708F, 0.0F));

        PartDefinition group8 = front_teeth.addOrReplaceChild("group8", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.369F, -0.9F, 0.034F, 0.0F, 0.0F, 0.1571F));

        PartDefinition cube_r131 = group8.addOrReplaceChild("cube_r131", CubeListBuilder.create().texOffs(76, 59).addBox(-0.25F, -1.75F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.219F, 1.5F, -0.0957F, 0.0F, -1.9635F, 0.0F));

        PartDefinition cube_r132 = group8.addOrReplaceChild("cube_r132", CubeListBuilder.create().texOffs(40, 65).addBox(-0.25F, -0.2F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.319F, -0.05F, -0.0957F, 0.0F, -1.5708F, 0.0F));

        PartDefinition group9 = front_teeth.addOrReplaceChild("group9", CubeListBuilder.create(), PartPose.offsetAndRotation(2.7464F, 2.7226F, 0.034F, 0.0F, 0.0F, 0.1309F));

        PartDefinition cube_r133 = group9.addOrReplaceChild("cube_r133", CubeListBuilder.create().texOffs(74, 29).addBox(1.6945F, -0.8219F, 0.3308F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, -1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r134 = group9.addOrReplaceChild("cube_r134", CubeListBuilder.create().texOffs(62, 72).addBox(1.6945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, -1.5708F, 0.0F, -0.7854F));

        PartDefinition group10 = front_teeth.addOrReplaceChild("group10", CubeListBuilder.create(), PartPose.offset(-1.2035F, 2.4226F, 0.034F));

        PartDefinition cube_r135 = group10.addOrReplaceChild("cube_r135", CubeListBuilder.create().texOffs(54, 72).addBox(-3.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition cube_r136 = group10.addOrReplaceChild("cube_r136", CubeListBuilder.create().texOffs(12, 70).addBox(-3.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition group11 = front_teeth.addOrReplaceChild("group11", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5607F, 1.9155F, 0.034F, -0.0915F, -0.3042F, 0.1584F));

        PartDefinition cube_r137 = group11.addOrReplaceChild("cube_r137", CubeListBuilder.create().texOffs(20, 63).addBox(-3.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, 0.7071F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r138 = group11.addOrReplaceChild("cube_r138", CubeListBuilder.create().texOffs(74, 15).addBox(-3.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, 0.7071F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group12 = front_teeth.addOrReplaceChild("group12", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.3513F, 3.2905F, -0.0563F, -0.0596F, -0.0365F, -0.5487F));

        PartDefinition cube_r139 = group12.addOrReplaceChild("cube_r139", CubeListBuilder.create().texOffs(74, 41).addBox(-1.1945F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r140 = group12.addOrReplaceChild("cube_r140", CubeListBuilder.create().texOffs(68, 16).addBox(-1.1945F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group13 = front_teeth.addOrReplaceChild("group13", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.8489F, 3.3601F, -0.0612F, -0.0696F, -0.0049F, -0.0696F));

        PartDefinition cube_r141 = group13.addOrReplaceChild("cube_r141", CubeListBuilder.create().texOffs(74, 43).addBox(-1.1946F, -0.8219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r142 = group13.addOrReplaceChild("cube_r142", CubeListBuilder.create().texOffs(68, 19).addBox(-1.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1453F, -0.7375F, -0.0049F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group14 = front_teeth.addOrReplaceChild("group14", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.1536F, 2.7226F, 0.034F, -0.0876F, -0.0869F, -0.0796F));

        PartDefinition cube_r143 = group14.addOrReplaceChild("cube_r143", CubeListBuilder.create().texOffs(66, 47).addBox(-2.1946F, -0.8219F, -1.0692F, 0.5F, 0.5F, 2.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r144 = group14.addOrReplaceChild("cube_r144", CubeListBuilder.create().texOffs(68, 72).addBox(-2.1946F, -0.1543F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition group15 = front_teeth.addOrReplaceChild("group15", CubeListBuilder.create(), PartPose.offset(-0.0036F, -2.7226F, 0.034F));

        PartDefinition cube_r145 = group15.addOrReplaceChild("cube_r145", CubeListBuilder.create().texOffs(12, 77).addBox(-0.25F, -0.4222F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7571F, 0.6071F, 0.0F, 1.9635F, 0.0F, 1.2915F));

        PartDefinition cube_r146 = group15.addOrReplaceChild("cube_r146", CubeListBuilder.create().texOffs(6, 70).addBox(-0.25F, -0.3457F, -0.319F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7571F, 0.6071F, 0.0F, 1.5708F, 0.0F, 1.2915F));

        PartDefinition group16 = front_teeth.addOrReplaceChild("group16", CubeListBuilder.create(), PartPose.offset(-0.0036F, -3.8226F, 0.034F));

        PartDefinition cube_r147 = group16.addOrReplaceChild("cube_r147", CubeListBuilder.create().texOffs(74, 45).addBox(1.6945F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r148 = group16.addOrReplaceChild("cube_r148", CubeListBuilder.create().texOffs(48, 72).addBox(1.6945F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition group17 = front_teeth.addOrReplaceChild("group17", CubeListBuilder.create(), PartPose.offset(-0.0036F, -2.7226F, 0.034F));

        PartDefinition cube_r149 = group17.addOrReplaceChild("cube_r149", CubeListBuilder.create().texOffs(16, 77).addBox(1.6945F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.7854F));

        PartDefinition cube_r150 = group17.addOrReplaceChild("cube_r150", CubeListBuilder.create().texOffs(0, 73).addBox(1.6945F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

        PartDefinition group18 = front_teeth.addOrReplaceChild("group18", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.4607F, -2.0155F, 0.034F, 0.1745F, 0.0873F, -0.3054F));

        PartDefinition cube_r151 = group18.addOrReplaceChild("cube_r151", CubeListBuilder.create().texOffs(70, 60).addBox(-3.1946F, 0.3219F, -0.3692F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, -0.7071F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r152 = group18.addOrReplaceChild("cube_r152", CubeListBuilder.create().texOffs(74, 18).addBox(-3.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4571F, -0.7071F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group19 = front_teeth.addOrReplaceChild("group19", CubeListBuilder.create(), PartPose.offset(0.0964F, -3.9226F, 0.034F));

        PartDefinition cube_r153 = group19.addOrReplaceChild("cube_r153", CubeListBuilder.create().texOffs(20, 77).addBox(-2.1946F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r154 = group19.addOrReplaceChild("cube_r154", CubeListBuilder.create().texOffs(78, 1).addBox(-2.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group20 = front_teeth.addOrReplaceChild("group20", CubeListBuilder.create(), PartPose.offset(-0.0036F, -2.7226F, 0.034F));

        PartDefinition cube_r155 = group20.addOrReplaceChild("cube_r155", CubeListBuilder.create().texOffs(24, 77).addBox(-2.1946F, 0.3219F, 0.6308F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -0.7854F));

        PartDefinition cube_r156 = group20.addOrReplaceChild("cube_r156", CubeListBuilder.create().texOffs(6, 73).addBox(-2.1946F, -0.3457F, 1.6255F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -0.7854F));

        PartDefinition group21 = front_teeth.addOrReplaceChild("group21", CubeListBuilder.create(), PartPose.offsetAndRotation(3.4535F, 2.0155F, 0.034F, 0.0F, 0.0F, -0.2182F));

        PartDefinition cube_r157 = group21.addOrReplaceChild("cube_r157", CubeListBuilder.create().texOffs(70, 57).addBox(-0.25F, -0.0778F, -1.9157F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1633F, -0.1633F, 0.0957F, -1.9635F, 0.0F, -0.9425F));

        PartDefinition cube_r158 = group21.addOrReplaceChild("cube_r158", CubeListBuilder.create().texOffs(70, 54).addBox(-0.25F, -0.0587F, -0.0881F, 0.5F, 0.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1633F, -0.1633F, 0.0957F, -1.5708F, 0.0F, -0.9425F));

        PartDefinition group22 = front_teeth.addOrReplaceChild("group22", CubeListBuilder.create(), PartPose.offset(-1.55F, 2.05F, -0.0617F));

        PartDefinition cube_r159 = group22.addOrReplaceChild("cube_r159", CubeListBuilder.create().texOffs(28, 77).addBox(-1.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.55F, 0.1F, -0.1F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r160 = group22.addOrReplaceChild("cube_r160", CubeListBuilder.create().texOffs(48, 65).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.1F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group23 = front_teeth.addOrReplaceChild("group23", CubeListBuilder.create(), PartPose.offset(-0.55F, 2.05F, -0.0617F));

        PartDefinition cube_r161 = group23.addOrReplaceChild("cube_r161", CubeListBuilder.create().texOffs(74, 31).addBox(-0.75F, -0.25F, -1.3F, 0.5F, 0.5F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, 0.5F, 0.0F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r162 = group23.addOrReplaceChild("cube_r162", CubeListBuilder.create().texOffs(0, 66).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group24 = front_teeth.addOrReplaceChild("group24", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 2.369F, 0.034F, -0.0869F, -0.0076F, -0.0869F));

        PartDefinition cube_r163 = group24.addOrReplaceChild("cube_r163", CubeListBuilder.create().texOffs(32, 77).addBox(0.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.019F, -0.0957F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r164 = group24.addOrReplaceChild("cube_r164", CubeListBuilder.create().texOffs(66, 23).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, -0.119F, -0.0957F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group25 = front_teeth.addOrReplaceChild("group25", CubeListBuilder.create(), PartPose.offsetAndRotation(1.25F, 2.05F, -0.0617F, 0.0F, 0.0F, -0.0873F));

        PartDefinition cube_r165 = group25.addOrReplaceChild("cube_r165", CubeListBuilder.create().texOffs(44, 77).addBox(1.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.45F, 0.3F, 0.0F, -1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r166 = group25.addOrReplaceChild("cube_r166", CubeListBuilder.create().texOffs(66, 27).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.2F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition group26 = front_teeth.addOrReplaceChild("group26", CubeListBuilder.create(), PartPose.offset(-1.55F, -2.05F, -0.0617F));

        PartDefinition cube_r167 = group26.addOrReplaceChild("cube_r167", CubeListBuilder.create().texOffs(48, 77).addBox(-1.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.55F, -0.1F, 0.0F, 1.9635F, 0.0F, 0.0F));

        PartDefinition cube_r168 = group26.addOrReplaceChild("cube_r168", CubeListBuilder.create().texOffs(66, 31).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition group27 = front_teeth.addOrReplaceChild("group27", CubeListBuilder.create(), PartPose.offset(-0.55F, -2.05F, -0.0617F));

        PartDefinition cube_r169 = group27.addOrReplaceChild("cube_r169", CubeListBuilder.create().texOffs(60, 77).addBox(-0.75F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, -0.1F, 0.1F, 1.9635F, -0.1396F, 0.0F));

        PartDefinition cube_r170 = group27.addOrReplaceChild("cube_r170", CubeListBuilder.create().texOffs(66, 35).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, -0.1396F, 0.0F));

        PartDefinition group28 = front_teeth.addOrReplaceChild("group28", CubeListBuilder.create(), PartPose.offset(0.0F, -2.15F, -0.0617F));

        PartDefinition cube_r171 = group28.addOrReplaceChild("cube_r171", CubeListBuilder.create().texOffs(66, 39).addBox(-0.2F, -0.25F, 0.0F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.45F, 0.1F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r172 = group28.addOrReplaceChild("cube_r172", CubeListBuilder.create().texOffs(64, 77).addBox(0.25F, -0.25F, -1.0F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.0F));

        PartDefinition group85 = front_teeth.addOrReplaceChild("group85", CubeListBuilder.create(), PartPose.offset(0.0F, -2.15F, -0.0617F));

        PartDefinition cube_r173 = group85.addOrReplaceChild("cube_r173", CubeListBuilder.create().texOffs(66, 43).addBox(-0.25F, -0.3457F, -0.319F, 0.5F, 0.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.219F, 0.0957F, 1.6611F, -0.2608F, -0.0233F));

        PartDefinition cube_r174 = group85.addOrReplaceChild("cube_r174", CubeListBuilder.create().texOffs(68, 77).addBox(-0.25F, -0.4222F, -1.1657F, 0.5F, 0.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.219F, 0.0957F, 2.0538F, -0.2608F, -0.0233F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(GiantSandwormHeadPart entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
