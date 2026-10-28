package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity;
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

public class YggdrasilTreantModel extends HierarchicalModel<YggdrasilTreantBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "yggdrasil_treant"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public YggdrasilTreantModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
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
                .texOffs(40, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(85, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, -9.0F, 0.0F, 0.0F, 0.0F, -0.35F));

        head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, -9.0F, 0.0F, 0.0F, 0.0F, 0.35F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(40, 20).addBox(-5.5F, 0.0F, -3.5F, 11.0F, 14.0F, 7.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(24, 45).addBox(-2.0F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(48, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 14.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(72, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(YggdrasilTreantBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        float breath = Mth.sin(ageInTicks * 0.06F) * 0.03F;
        this.body.xRot = breath;
        this.head.y = breath * 2.0F;

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.5F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.5F + Mth.PI) * 1.0F * limbSwingAmount;

        this.rightArm.xRot = Mth.cos(limbSwing * 0.5F + Mth.PI) * 0.8F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.5F) * 0.8F * limbSwingAmount;
    }
}
