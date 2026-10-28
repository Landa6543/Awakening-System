package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.DokkaebiEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DokkaebiModel extends HierarchicalModel<DokkaebiEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dokkaebi"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightHorn;
    private final ModelPart leftHorn;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public DokkaebiModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHorn = this.head.getChild("right_horn");
        this.leftHorn = this.head.getChild("left_horn");
        this.torso = this.body.getChild("torso");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = this.body.getChild("right_leg");
        this.leftLeg = this.body.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -7.0F, 0.0F, -0.15F, 0.0F, -0.25F));

        head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -7.0F, 0.0F, -0.15F, 0.0F, 0.25F));

        body.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.5F, 0.0F, -4.0F, 9.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(44, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 1.5F, 0.0F, 0.0F, 0.0F, 0.25F));

        body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(36, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, 1.5F, 0.0F, 0.0F, 0.0F, -0.25F));

        body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 7.0F, 0.0F));

        body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(36, 16).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 7.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DokkaebiEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float bob = Mth.sin(ageInTicks * 0.12F) * 1.5F;
        this.body.y = 14.0F + bob;
        this.body.yRot = Mth.sin(ageInTicks * 0.05F) * 0.04F;

        this.head.yRot = netHeadYaw * (Mth.PI / 180.0F);
        this.head.xRot = headPitch * (Mth.PI / 180.0F);

        this.rightArm.zRot = 0.25F + Mth.sin(ageInTicks * 0.08F) * 0.06F;
        this.leftArm.zRot = -0.25F - Mth.sin(ageInTicks * 0.08F) * 0.06F;
        this.rightArm.xRot = Mth.cos(ageInTicks * 0.07F) * 0.05F;
        this.leftArm.xRot = -Mth.cos(ageInTicks * 0.07F) * 0.05F;

        this.rightLeg.xRot = Mth.cos(ageInTicks * 0.1F) * 0.08F;
        this.leftLeg.xRot = -Mth.cos(ageInTicks * 0.1F) * 0.08F;
    }
}
