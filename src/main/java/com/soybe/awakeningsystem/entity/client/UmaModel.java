package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.uma.UmaRacerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D para corredoras de Uma Musume.
 * Basado en proporciones humanoides estilizadas con orejas equinas móviles en la cabeza
 * y cola de caballo animada con física de carrera en el torso posterior.
 */
public class UmaModel extends HierarchicalModel<UmaRacerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "uma_racer"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public UmaModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 10.0F, 2.0F, 0.35F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, 2.5F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, 2.5F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(UmaRacerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float earFlick = Mth.sin(ageInTicks * 0.15F) * 0.08F;
        this.leftEar.zRot = 0.25F + earFlick;
        this.rightEar.zRot = -0.25F - earFlick;

        boolean isRunning = entity.isRaceRunning();
        boolean isFinished = entity.isFinished();
        int rank = entity.getFinishRank();

        if (isFinished && rank == 1) {
            this.rightArm.xRot = -2.8F;
            this.rightArm.zRot = -0.3F;
            this.leftArm.xRot = -2.8F;
            this.leftArm.zRot = 0.3F;
            this.head.xRot = -0.2F;
            this.tail.xRot = 0.5F + Mth.sin(ageInTicks * 0.3F) * 0.2F;
            return;
        }

        if (isRunning || limbSwingAmount > 0.15F) {
            float speedFactor = isRunning ? 1.4F : 1.0F;
            float leanForward = isRunning ? 0.35F : 0.15F;

            this.body.xRot = leanForward;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - leanForward * 0.6F;

            this.rightLeg.xRot = Mth.cos(limbSwing * 0.85F * speedFactor) * 1.5F * limbSwingAmount;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.85F * speedFactor + (float) Math.PI) * 1.5F * limbSwingAmount;

            this.rightArm.xRot = Mth.cos(limbSwing * 0.85F * speedFactor + (float) Math.PI) * 1.4F * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.85F * speedFactor) * 1.4F * limbSwingAmount;

            this.tail.xRot = 0.75F + Mth.cos(ageInTicks * 0.5F) * 0.25F;
            this.tail.yRot = Mth.sin(ageInTicks * 0.4F) * 0.15F;
        } else {
            float breath = Mth.sin(ageInTicks * 0.08F) * 0.02F;
            this.body.y += breath * 1.5F;
            this.head.y += breath * 1.5F;
            this.tail.xRot = 0.35F + Mth.sin(ageInTicks * 0.05F) * 0.08F;
        }
    }
}
