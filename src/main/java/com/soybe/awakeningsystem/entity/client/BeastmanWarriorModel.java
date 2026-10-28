package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
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

public class BeastmanWarriorModel extends HierarchicalModel<BeastmanWarriorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_warrior"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    private final ModelPart tail;

    public BeastmanWarriorModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
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
                .texOffs(0, 0).addBox(-3.5F, -6.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(28, 0).addBox(-2.5F, -3.0F, -6.5F, 5.0F, 3.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(44, 0).addBox(-2.0F, 0.5F, -6.0F, 4.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(58, 0).addBox(-2.2F, -0.5F, -5.5F, 0.8F, 1.5F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(58, 0).mirror().addBox(1.4F, -0.5F, -5.5F, 0.8F, 1.5F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-2.5F, -6.0F, 2.5F, 5.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -1.0F));

        head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(28, 7).addBox(-1.5F, -3.5F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -6.5F, 0.5F, 0.0F, 0.0F, -0.35F));

        head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(28, 7).mirror().addBox(-1.5F, -3.5F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -6.5F, 0.5F, 0.0F, 0.0F, 0.35F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 13.0F, 6.0F, new CubeDeformation(0.2F))
                .texOffs(32, 14).addBox(-5.5F, 0.5F, -3.5F, 11.0F, 5.0F, 7.0F, new CubeDeformation(0.1F))
                .texOffs(0, 33).addBox(-5.5F, 10.5F, -3.5F, 11.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(36, 26).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(54, 26).addBox(-2.0F, 0.5F, 5.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 10.0F, 2.5F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(68, 14).addBox(-3.5F, -2.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(84, 14).addBox(-4.0F, 6.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.0F, 0.0F, 0.0F));

        // Brutal Barbarian Battleaxe: Centered naturally in the right hand palm
        rightArm.addOrReplaceChild("battleaxe", CubeListBuilder.create()
                .texOffs(64, 64).addBox(-0.9F, -15.0F, -0.9F, 1.8F, 21.0F, 1.8F, new CubeDeformation(0.0F))
                .texOffs(74, 64).addBox(-1.4F, 5.5F, -1.4F, 2.8F, 2.5F, 2.8F, new CubeDeformation(0.0F))
                .texOffs(88, 64).addBox(-1.4F, -15.5F, -2.0F, 2.8F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(64, 88).addBox(-0.9F, -17.5F, -7.5F, 1.8F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(84, 88).addBox(-0.8F, -14.5F, 1.5F, 1.6F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(96, 88).addBox(-0.8F, -18.5F, -0.8F, 1.6F, 3.0F, 1.6F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, 9.5F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(68, 32).mirror().addBox(-0.5F, -2.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(84, 14).mirror().addBox(-1.0F, 6.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.0F, 0.0F, 0.0F));

        // Right leg has its own dedicated UV island at (0, 64)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 84).addBox(-2.5F, 11.0F, -4.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.8F, 11.0F, 0.0F));

        // Left leg has its own dedicated UV island at (24, 64) - eliminates mirroring and missing textures
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(24, 64).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 84).addBox(-2.5F, 11.0F, -4.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.8F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(BeastmanWarriorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F) + Mth.sin(ageInTicks * 0.08F) * 0.03F;

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.65F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.65F + Mth.PI) * 1.0F * limbSwingAmount;

        // Feral battle stance: Axe held firmly forward with controlled weapon weight
        this.rightArm.xRot = -0.52F + Mth.cos(limbSwing * 0.65F + Mth.PI) * 0.25F * limbSwingAmount;
        this.rightArm.yRot = -0.15F;
        this.rightArm.zRot = 0.05F;

        this.leftArm.xRot = Mth.cos(limbSwing * 0.65F) * 0.6F * limbSwingAmount;
        this.leftArm.zRot = 0.08F;

        // Tail wagging
        this.tail.yRot = Mth.cos(ageInTicks * 0.18F) * 0.28F;
    }
}
