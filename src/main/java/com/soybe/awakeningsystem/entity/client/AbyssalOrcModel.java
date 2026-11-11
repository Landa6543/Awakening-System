package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
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

public class AbyssalOrcModel extends HierarchicalModel<AbyssalOrcEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_orc"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AbyssalOrcModel(ModelPart root) {
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

        // A hulking brute: head sunk low and forward between huge shoulders, long arms with giant fists.
        // head: skull, jutting underbite with two rising tusks, pointed ears, black hair
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(32, 0).addBox(-4.5F, -3.0F, -5.8F, 9.0F, 3.0F, 7.0F)
                .texOffs(64, 0).addBox(-3.5F, -5.0F, -6.3F, 1.0F, 3.0F, 1.0F)
                .texOffs(64, 0).mirror().addBox(2.5F, -5.0F, -6.3F, 1.0F, 3.0F, 1.0F)
                .texOffs(68, 0).mirror(false).addBox(-6.0F, -6.0F, -1.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(68, 0).mirror().addBox(4.0F, -6.0F, -1.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(76, 0).mirror(false).addBox(-4.5F, -8.6F, -1.0F, 9.0F, 9.0F, 5.0F),
                PartPose.offset(0.0F, -13.0F, -6.0F));

        // body: narrow waist, belt and torn cloth, a massive chest and traps, long hair down the back
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-6.0F, -8.0F, -3.5F, 12.0F, 8.0F, 7.0F)
                .texOffs(38, 16).addBox(-6.5F, -2.0F, -4.0F, 13.0F, 2.0F, 8.0F)
                .texOffs(104, 0).addBox(-4.0F, -1.0F, -4.6F, 8.0F, 7.0F, 1.0F)
                .texOffs(0, 32).addBox(-9.0F, -19.0F, -6.0F, 18.0F, 11.0F, 10.0F)
                .texOffs(56, 32).addBox(-7.0F, -22.0F, -4.0F, 14.0F, 4.0F, 8.0F)
                .texOffs(80, 16).addBox(-7.0F, -21.0F, 4.0F, 14.0F, 14.0F, 2.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // arms: round shoulder, upper arm, fur cuff, huge forearm and fist hanging past the hips
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-5.0F, -3.0F, -4.0F, 8.0F, 7.0F, 8.0F)
                .texOffs(32, 53).addBox(-4.0F, 3.0F, -3.0F, 6.0F, 8.0F, 6.0F)
                .texOffs(56, 53).addBox(-5.0F, 10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.2F))
                .texOffs(88, 53).addBox(-4.5F, 11.0F, -3.5F, 7.0F, 9.0F, 7.0F)
                .texOffs(0, 69).addBox(-4.5F, 19.0F, -4.5F, 7.0F, 5.0F, 8.0F),
                PartPose.offset(-10.5F, -12.0F, -1.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 53).mirror().addBox(-3.0F, -3.0F, -4.0F, 8.0F, 7.0F, 8.0F)
                .texOffs(32, 53).mirror().addBox(-2.0F, 3.0F, -3.0F, 6.0F, 8.0F, 6.0F)
                .texOffs(56, 53).mirror().addBox(-3.0F, 10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.2F))
                .texOffs(88, 53).mirror().addBox(-2.5F, 11.0F, -3.5F, 7.0F, 9.0F, 7.0F)
                .texOffs(0, 69).mirror().addBox(-2.5F, 19.0F, -4.5F, 7.0F, 5.0F, 8.0F),
                PartPose.offset(10.5F, -12.0F, -1.0F));

        // legs: torn dark trousers, fur at the knee, bandaged shins, bare feet
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(30, 69).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 11.0F, 7.0F)
                .texOffs(58, 69).addBox(-4.0F, 9.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(90, 69).addBox(-3.0F, 11.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                .texOffs(0, 87).addBox(-3.5F, 16.0F, -4.5F, 7.0F, 2.0F, 8.0F),
                PartPose.offset(-4.2F, 6.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(30, 69).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 11.0F, 7.0F)
                .texOffs(58, 69).mirror().addBox(-4.0F, 9.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(90, 69).mirror().addBox(-3.0F, 11.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                .texOffs(0, 87).mirror().addBox(-3.5F, 16.0F, -4.5F, 7.0F, 2.0F, 8.0F),
                PartPose.offset(4.2F, 6.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AbyssalOrcEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.55F) * 1.15F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.55F + (float) Math.PI) * 1.15F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.55F + (float) Math.PI) * 0.9F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.55F) * 0.9F * limbSwingAmount;
        this.body.zRot = Mth.sin(limbSwing * 0.55F) * 0.08F * limbSwingAmount;
        this.body.yRot = Mth.sin(limbSwing * 0.55F) * 0.06F * limbSwingAmount;

        if (limbSwingAmount < 0.1F) {
            float breath = Mth.sin(ageInTicks * 0.08F);
            this.body.y += breath * 0.5F;
            this.head.y += breath * 0.4F;
            this.body.xRot = breath * 0.03F;
            this.rightArm.zRot = 0.15F + breath * 0.04F;
            this.leftArm.zRot = -0.15F - breath * 0.04F;
        }

        if (entity.bloodFrenzyTicks > 0) {
            this.body.xRot += 0.16F;
            this.head.xRot -= 0.12F;
            this.rightArm.zRot += 0.22F;
            this.leftArm.zRot -= 0.22F;
            this.rightArm.xRot += 0.15F;
            this.leftArm.xRot += 0.15F;
        }

        if (entity.smashAnimationTimeout > 0) {
            float progress = (25 - entity.smashAnimationTimeout) / 25.0F;
            if (progress < 0.5F) {
                float up = progress * 2.0F;
                this.rightArm.xRot = -2.4F * up;
                this.leftArm.xRot = -2.4F * up;
                this.body.xRot = -0.3F * up;
            } else {
                float down = (progress - 0.5F) * 2.0F;
                this.rightArm.xRot = -2.4F + down * 2.8F;
                this.leftArm.xRot = -2.4F + down * 2.8F;
                this.body.xRot = -0.3F + down * 0.7F;
            }
        }

        if (entity.roarAnimationTimeout > 0) {
            float progress = (30 - entity.roarAnimationTimeout) / 30.0F;
            float roarShake = Mth.sin(progress * (float) Math.PI * 6.0F) * 0.12F;
            this.head.xRot = -0.65F + roarShake;
            this.rightArm.zRot = 0.85F;
            this.leftArm.zRot = -0.85F;
            this.rightArm.xRot = -0.4F;
            this.leftArm.xRot = -0.4F;
        }
    }
}
