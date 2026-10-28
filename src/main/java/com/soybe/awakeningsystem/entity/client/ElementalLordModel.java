package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalLordBossEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ElementalLordModel extends HierarchicalModel<ElementalLordBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "elemental_lord"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart orb1;
    private final ModelPart orb2;

    public ElementalLordModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.orb1 = this.body.getChild("orb1");
        this.orb2 = this.body.getChild("orb2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-5.0F, -11.0F, -2.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(32, 0).mirror().addBox(3.0F, -11.0F, -2.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(40, 0).addBox(-1.0F, -12.0F, -3.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 14.0F, 6.0F)
                        .texOffs(32, 16).addBox(-8.0F, -1.0F, -3.0F, 3.0F, 4.0F, 6.0F)
                        .texOffs(32, 16).mirror().addBox(5.0F, -1.0F, -3.0F, 3.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        body.addOrReplaceChild("orb1",
                CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(-9.0F, 4.0F, 0.0F));

        body.addOrReplaceChild("orb2",
                CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(9.0F, 4.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(-6.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(16, 36).addBox(-1.0F, -1.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(6.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(32, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(-2.5F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(32, 36).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(2.5F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ElementalLordBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount * 0.5F;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount * 0.5F;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount * 0.5F;

        float angle = ageInTicks * 0.08F;
        this.orb1.x = -Mth.sin(angle) * 10.0F;
        this.orb1.z = Mth.cos(angle) * 10.0F;
        this.orb1.y = 4.0F + Mth.sin(ageInTicks * 0.15F) * 2.0F;

        this.orb2.x = Mth.sin(angle) * 10.0F;
        this.orb2.z = -Mth.cos(angle) * 10.0F;
        this.orb2.y = 4.0F - Mth.sin(ageInTicks * 0.15F) * 2.0F;
    }
}
