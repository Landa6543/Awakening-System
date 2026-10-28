package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JadeArmoredBeastEntity;
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

public class JadeArmoredBeastModel extends HierarchicalModel<JadeArmoredBeastEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_armored_beast"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart tail;

    public JadeArmoredBeastModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-6.0F, -6.5F, -6.5F, 12.0F, 4.0F, 13.0F, new CubeDeformation(0.2F))
                .texOffs(0, 40).addBox(-1.5F, -8.5F, -5.0F, 3.0F, 3.5F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(50, 22).addBox(5.2F, -2.0F, -6.0F, 1.5F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(50, 22).mirror().addBox(-6.7F, -2.0F, -6.0F, 1.5F, 5.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Head skull base: solid foundation connected to torso
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(74, 0).addBox(-3.0F, -1.5F, -9.0F, 6.0F, 3.5F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(94, 0).addBox(-4.0F, -5.5F, -6.0F, 8.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(74, 8).addBox(-2.5F, 2.0F, -8.5F, 5.0F, 1.5F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(100, 8).addBox(-1.0F, -7.5F, -7.0F, 2.0F, 3.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(92, 8).addBox(-3.2F, -0.5F, -8.0F, 0.8F, 2.5F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(92, 8).mirror().addBox(2.4F, -0.5F, -8.0F, 0.8F, 2.5F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(116, 8).addBox(3.5F, -6.0F, -3.0F, 1.5F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(116, 8).mirror().addBox(-5.0F, -6.0F, -3.0F, 1.5F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.5F, -7.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(76, 16).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(96, 16).addBox(-2.5F, -2.5F, 5.5F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(114, 16).addBox(-1.0F, -4.0F, 6.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(114, 16).addBox(-1.0F, 2.0F, 6.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(114, 16).addBox(-4.0F, -1.0F, 6.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(114, 16).addBox(2.0F, -1.0F, 6.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, 7.0F));

        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(64, 56).addBox(-2.5F, 5.0F, -3.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 17.0F, -5.0F));

        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(16, 56).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(64, 56).mirror().addBox(-2.5F, 5.0F, -3.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 17.0F, -5.0F));

        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(32, 56).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(78, 56).addBox(-2.5F, 5.0F, -3.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 17.0F, 5.0F));

        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(48, 56).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(78, 56).mirror().addBox(-2.5F, 5.0F, -3.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 17.0F, 5.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(JadeArmoredBeastEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F) + Mth.sin(ageInTicks * 0.08F) * 0.04F;

        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6F) * 1.0F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6F + Mth.PI) * 1.0F * limbSwingAmount;
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6F + Mth.PI) * 1.0F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6F) * 1.0F * limbSwingAmount;

        this.tail.yRot = Mth.cos(ageInTicks * 0.15F) * 0.35F;
        this.tail.zRot = Mth.sin(ageInTicks * 0.15F) * 0.10F;
    }
}
