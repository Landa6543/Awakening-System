package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ElementalDemonKingModel extends HierarchicalModel<ElementalDemonKingBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "elemental_demon_king"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public ElementalDemonKingModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 16).addBox(-5.5F, -6.0F, 0.0F, 2.0F, 3.0F, 1.0F)
                        .texOffs(32, 16).mirror().addBox(3.5F, -6.0F, 0.0F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        head.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 9.0F)
                        .texOffs(32, 12).addBox(-1.5F, -10.5F, -4.7F, 3.0F, 3.0F, 1.0F)
                        .texOffs(40, 12).addBox(-4.7F, -10.0F, -1.5F, 1.0F, 2.0F, 3.0F)
                        .texOffs(40, 12).addBox(3.7F, -10.0F, -1.5F, 1.0F, 2.0F, 3.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("right_horn_base",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-6.0F, -9.0F, -2.0F, 3.0F, 4.0F, 3.0F)
                        .texOffs(12, 16).addBox(-7.0F, -13.0F, -1.0F, 3.0F, 5.0F, 3.0F)
                        .texOffs(24, 16).addBox(-6.5F, -17.0F, 0.0F, 2.0F, 5.0F, 2.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("left_horn_base",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(3.0F, -9.0F, -2.0F, 3.0F, 4.0F, 3.0F)
                        .texOffs(12, 24).addBox(4.0F, -13.0F, -1.0F, 3.0F, 5.0F, 3.0F)
                        .texOffs(24, 24).addBox(4.5F, -17.0F, 0.0F, 2.0F, 5.0F, 2.0F),
                PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-6.0F, 0.0F, -3.0F, 12.0F, 16.0F, 6.0F)
                        .texOffs(36, 32).addBox(-5.0F, 1.0F, -3.8F, 10.0F, 8.0F, 1.0F)
                        .texOffs(36, 42).addBox(-6.5F, 13.0F, -3.5F, 13.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        body.addOrReplaceChild("right_pauldron",
                CubeListBuilder.create()
                        .texOffs(0, 54).addBox(-10.5F, -2.5F, -4.0F, 5.0F, 5.0F, 8.0F)
                        .texOffs(0, 68).addBox(-11.5F, -5.0F, -1.5F, 2.0F, 3.0F, 3.0F),
                PartPose.ZERO);

        body.addOrReplaceChild("left_pauldron",
                CubeListBuilder.create()
                        .texOffs(26, 54).addBox(5.5F, -2.5F, -4.0F, 5.0F, 5.0F, 8.0F)
                        .texOffs(26, 68).addBox(9.5F, -5.0F, -1.5F, 2.0F, 3.0F, 3.0F),
                PartPose.ZERO);

        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 96).addBox(-22.0F, -5.0F, 2.0F, 22.0F, 16.0F, 0.2F)
                        .texOffs(46, 96).addBox(-18.0F, 11.0F, 2.0F, 18.0F, 14.0F, 0.2F),
                PartPose.offset(-2.0F, 2.0F, 2.0F));

        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 96).mirror().addBox(0.0F, -5.0F, 2.0F, 22.0F, 16.0F, 0.2F)
                        .texOffs(46, 96).mirror().addBox(0.0F, 11.0F, 2.0F, 18.0F, 14.0F, 0.2F),
                PartPose.offset(2.0F, 2.0F, 2.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(70, 0).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                        .texOffs(88, 0).addBox(-3.5F, 8.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(-7.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(70, 20).addBox(-1.0F, -1.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                        .texOffs(88, 16).addBox(-1.5F, 8.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(7.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 75).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                        .texOffs(16, 75).addBox(-2.5F, 6.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(-3.0F, 8.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(36, 75).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                        .texOffs(52, 75).addBox(-2.5F, 6.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(3.0F, 8.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ElementalDemonKingBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6F) * 1.2F * limbSwingAmount * 0.5F;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6F + (float) Math.PI) * 1.2F * limbSwingAmount * 0.5F;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6F + (float) Math.PI) * 1.1F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6F) * 1.1F * limbSwingAmount * 0.5F;

        // Wings flap backwards behind the body
        float wingFlap = Mth.cos(ageInTicks * 0.08F) * 0.18F;
        this.rightWing.yRot = 0.42F + wingFlap;
        this.leftWing.yRot = -0.42F - wingFlap;
        this.rightWing.zRot = 0.12F + Mth.sin(ageInTicks * 0.08F) * 0.06F;
        this.leftWing.zRot = -0.12F - Mth.sin(ageInTicks * 0.08F) * 0.06F;
    }
}
