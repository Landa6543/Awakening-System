package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalNecromancerEntity;
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

public class AbyssalNecromancerModel extends HierarchicalModel<AbyssalNecromancerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_necromancer"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AbyssalNecromancerModel(ModelPart root) {
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
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F))
                .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.5F))
                .texOffs(68, 0).addBox(3.5F, -11.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 0).mirror().addBox(-5.5F, -11.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 14.0F, 5.0F, new CubeDeformation(0.2F))
                .texOffs(0, 38).addBox(-5.0F, 12.0F, -3.0F, 10.0F, 9.0F, 6.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(34, 18).addBox(-3.5F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.1F))
                .texOffs(52, 18).addBox(-4.0F, 5.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.offset(-5.0F, -1.0F, 0.0F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(34, 18).mirror().addBox(-0.5F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.1F))
                .texOffs(52, 18).mirror().addBox(-1.0F, 5.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.offset(5.0F, -1.0F, 0.0F));

        leftArm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(78, 0).addBox(0.5F, -14.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(86, 0).addBox(-0.5F, -18.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 7.0F, -1.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(34, 38).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 11.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(34, 38).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AbyssalNecromancerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;

        this.leftArm.xRot = -0.4F + Mth.sin(ageInTicks * 0.06F) * 0.05F;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;

        if (entity.castAnimationTimeout > 0) {
            float progress = (25 - entity.castAnimationTimeout) / 25.0F;
            float raise = Mth.sin(progress * (float) Math.PI);
            this.rightArm.xRot = -1.6F * raise;
            this.rightArm.yRot = -0.3F * raise;
            this.leftArm.xRot = -1.2F * raise;
        }

        if (entity.staffSwingAnimationTimeout > 0) {
            float progress = (20 - entity.staffSwingAnimationTimeout) / 20.0F;
            this.leftArm.xRot = -1.5F + Mth.sin(progress * (float) Math.PI) * 1.8F;
        }

        if (entity.summonAnimationTimeout > 0) {
            float progress = (35 - entity.summonAnimationTimeout) / 35.0F;

            if (progress < 0.65F) {
                float stretchProgress = progress / 0.65F;
                float easeStretch = Mth.sin(stretchProgress * (float) Math.PI * 0.5F);

                this.rightArm.xRot = -1.45F * easeStretch;
                this.rightArm.yRot = 0.38F * easeStretch;
                this.rightArm.zRot = -0.15F * easeStretch;

                this.leftArm.xRot = -1.45F * easeStretch;
                this.leftArm.yRot = -0.38F * easeStretch;
                this.leftArm.zRot = 0.15F * easeStretch;

                this.body.xRot = -0.10F * easeStretch;
                this.head.xRot = -0.20F * easeStretch;
            } else {
                float slamProgress = (progress - 0.65F) / 0.35F;
                float slamEase = slamProgress * slamProgress;

                this.rightArm.xRot = -1.45F + 2.15F * slamEase;
                this.rightArm.yRot = 0.38F * (1.0F - slamEase * 0.5F);
                this.rightArm.zRot = -0.15F * (1.0F - slamEase);

                this.leftArm.xRot = -1.45F + 2.15F * slamEase;
                this.leftArm.yRot = -0.38F * (1.0F - slamEase * 0.5F);
                this.leftArm.zRot = 0.15F * (1.0F - slamEase);

                this.body.xRot = 0.35F * slamEase;
                this.head.xRot = 0.25F * slamEase;
            }
        }
    }
}
