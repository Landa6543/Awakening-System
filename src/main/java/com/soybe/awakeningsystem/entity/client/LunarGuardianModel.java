package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LunarGuardianEntity;
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

public class LunarGuardianModel extends HierarchicalModel<LunarGuardianEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "lunar_guardian"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public LunarGuardianModel(ModelPart root) {
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

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(36, 20).addBox(-5.5F, 0.0F, -3.5F, 11.0F, 14.0F, 7.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-4.0F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(98, 0).addBox(-5.0F, -3.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.4F)),
                PartPose.offset(-6.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(76, 22).addBox(-1.0F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(98, 14).addBox(-1.0F, -3.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.4F)),
                PartPose.offset(6.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(24, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(LunarGuardianEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.5F) * 1.2F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.5F + (float) Math.PI) * 1.2F * limbSwingAmount;

        this.rightArm.xRot = Mth.cos(limbSwing * 0.5F + (float) Math.PI) * 1.0F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.5F) * 1.0F * limbSwingAmount;

        this.rightArm.zRot = 0.15F;
        this.leftArm.zRot = -0.15F;
    }
}
