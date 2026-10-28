package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinShamanEntity;
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

public class GoblinShamanModel extends HierarchicalModel<GoblinShamanEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_shaman"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GoblinShamanModel(ModelPart root) {
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
                .texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-3.5F, -7.0F, -4.0F, 7.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(42, 0).addBox(-1.0F, -10.0F, -3.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.5F, 0.0F));

        head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(24, 10).addBox(-4.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -3.0F, 0.5F, 0.0F, -0.35F, -0.2F));

        head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(24, 10).mirror().addBox(0.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -3.0F, 0.5F, 0.0F, 0.35F, 0.2F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-3.5F, 0.0F, -2.5F, 7.0F, 9.0F, 5.0F, new CubeDeformation(0.2F))
                .texOffs(24, 14).addBox(-4.0F, 7.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 7.5F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.5F, 8.5F, 0.0F));

        rightArm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-1.5F, 0.0F, -2.5F, 1.2F, 15.0F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(40, 26).addBox(-2.5F, -3.5F, -3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(44, 26).addBox(0.3F, -3.5F, -3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 26).addBox(-1.1F, -3.5F, -1.4F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 18).addBox(-2.1F, -2.8F, -3.5F, 2.5F, 2.5F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(52, 26).addBox(-0.9F, 0.5F, -3.8F, 0.5F, 3.5F, 1.5F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 30).mirror().addBox(0.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.5F, 8.5F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(10, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, 16.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(10, 30).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 16.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(GoblinShamanEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.7F) * 0.9F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.7F + Mth.PI) * 0.9F * limbSwingAmount;

        this.rightArm.xRot = -0.4F + Mth.sin(ageInTicks * 0.1F) * 0.08F;
        this.leftArm.xRot = -0.6F + Mth.cos(ageInTicks * 0.12F) * 0.15F;
    }
}
