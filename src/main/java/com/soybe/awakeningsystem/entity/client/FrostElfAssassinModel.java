package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.FrostElfAssassinEntity;
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

public class FrostElfAssassinModel extends HierarchicalModel<FrostElfAssassinEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "frost_elf_assassin"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public FrostElfAssassinModel(ModelPart root) {
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
                .texOffs(0, 0).addBox(-3.5F, -7.5F, -3.5F, 7.0F, 8.0F, 7.0F, new CubeDeformation(0.1F))
                .texOffs(40, 0).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-4.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 4).addBox(-5.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, -3.5F, 0.0F, 0.08F, -0.45F, -0.18F));

        head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(28, 0).mirror().addBox(0.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 4).mirror().addBox(3.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, -3.5F, 0.0F, 0.08F, 0.45F, 0.18F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F))
                .texOffs(24, 16).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.offset(-4.5F, 0.0F, 0.0F));

        rightArm.addOrReplaceChild("dagger_right", CubeListBuilder.create()
                .texOffs(52, 16).addBox(-1.5F, 9.0F, -0.5F, 1.0F, 1.6F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(52, 21).addBox(-1.7F, 8.8F, 2.5F, 1.4F, 2.0F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(58, 16).addBox(-2.0F, 8.5F, -1.2F, 2.0F, 2.6F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(52, 25).addBox(-1.3F, 9.0F, -8.0F, 0.6F, 1.6F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(52, 33).addBox(-1.2F, 9.3F, -10.0F, 0.4F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 32).mirror().addBox(-0.5F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.offset(4.5F, 0.0F, 0.0F));

        leftArm.addOrReplaceChild("dagger_left", CubeListBuilder.create()
                .texOffs(52, 16).mirror().addBox(0.5F, 9.0F, -0.5F, 1.0F, 1.6F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(52, 21).mirror().addBox(0.3F, 8.8F, 2.5F, 1.4F, 2.0F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(58, 16).mirror().addBox(0.0F, 8.5F, -1.2F, 2.0F, 2.6F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(52, 25).mirror().addBox(0.7F, 9.0F, -8.0F, 0.6F, 1.6F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(52, 33).mirror().addBox(0.8F, 9.3F, -10.0F, 0.4F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(16, 32).addBox(-1.8F, 0.0F, -1.8F, 3.6F, 13.0F, 3.6F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 11.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 32).mirror().addBox(-1.8F, 0.0F, -1.8F, 3.6F, 13.0F, 3.6F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(FrostElfAssassinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.8F) * 1.2F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.8F + (float) Math.PI) * 1.2F * limbSwingAmount;

        this.rightArm.xRot = -0.6F + Mth.sin(limbSwing * 0.8F) * 0.5F * limbSwingAmount;
        this.rightArm.yRot = -0.2F;
        this.leftArm.xRot = -0.6F - Mth.sin(limbSwing * 0.8F) * 0.5F * limbSwingAmount;
        this.leftArm.yRot = 0.2F;

        if (entity.dualSlashAnimationTimeout > 0) {
            float progress = (15 - entity.dualSlashAnimationTimeout) / 15.0F;
            float slash = Mth.sin(progress * (float) Math.PI);
            this.rightArm.xRot = -1.5F + slash * 1.2F;
            this.rightArm.yRot = 0.6F * slash;
            this.leftArm.xRot = -1.5F + slash * 1.2F;
            this.leftArm.yRot = -0.6F * slash;
        }

        if (entity.dashAnimationTimeout > 0) {
            this.body.xRot = 0.5F;
            this.head.xRot = -0.4F;
            this.rightArm.xRot = 0.8F;
            this.leftArm.xRot = 0.8F;
        }
    }
}
