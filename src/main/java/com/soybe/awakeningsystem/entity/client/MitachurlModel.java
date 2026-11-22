package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Our own Mitachurl (made for this mod, with its own texture): a hulking, hunched brute behind a bone mask
 * with curling horns, a fur collar over its shoulders, long heavy arms, wrapped calves, and either a burning
 * axe at its side or a plank shieldwall held in front. Parts and texture come from the same generator, so
 * every box finds its picture.
 */
public class MitachurlModel extends HierarchicalModel<MitachurlEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl"), "main");

    private final ModelPart root, body, head, rightArm, leftArm, rightForearm, leftForearm, rightLeg, leftLeg, axe, shield;

    public MitachurlModel(ModelPart root) {
        this.root = root;
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
        this.rightForearm = rightArm.getChild("right_forearm");
        this.leftForearm = leftArm.getChild("left_forearm");
        this.axe = rightForearm.getChild("axe");
        this.shield = leftForearm.getChild("shield");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(38, 55).addBox(-3.5F, 0F, -3.5F, 7F, 9F, 7F),
                PartPose.offsetAndRotation(-5.5F, 5F, 1F, 0F, 0F, 0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(92, 73).addBox(-3F, 0F, -3F, 6F, 8F, 6F)
                .texOffs(52, 89).addBox(-3.5F, 8F, -5F, 7F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(38, 55).addBox(-3.5F, 0F, -3.5F, 7F, 9F, 7F),
                PartPose.offsetAndRotation(5.5F, 5F, 1F, 0F, 0F, 0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(92, 73).addBox(-3F, 0F, -3F, 6F, 8F, 6F)
                .texOffs(52, 89).addBox(-3.5F, 8F, -5F, 7F, 2F, 8F),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-9F, -19F, -6F, 18F, 19F, 12F)
                .texOffs(94, 89).addBox(-7F, -1F, -6.4F, 14F, 8F, 0F)
                .texOffs(0, 73).addBox(-9.5F, -3F, -6.5F, 19F, 3F, 13F)
                .texOffs(0, 34).addBox(-11F, -22F, -7.5F, 22F, 6F, 15F),
                PartPose.offsetAndRotation(0F, 5F, 1F, 0.32F, 0F, 0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-5F, -8F, -6F, 10F, 9F, 9F)
                .texOffs(0, 89).addBox(-6F, -10F, -7.5F, 12F, 12F, 2F),
                PartPose.offsetAndRotation(0F, -19F, -9F, -0.32F, 0F, 0F));
        PartDefinition right_horn = head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(82, 89).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F),
                PartPose.offsetAndRotation(-4.5F, -9F, -6F, -0.35F, 0F, -0.75F));
        PartDefinition right_horn2 = right_horn.addOrReplaceChild("right_horn2", CubeListBuilder.create()
                .texOffs(0, 103).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(0F, -6F, 0F, -0.45F, 0F, 0.55F));
        PartDefinition right_horn3 = right_horn2.addOrReplaceChild("right_horn3", CubeListBuilder.create()
                .texOffs(28, 103).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(0F, -5F, 0F, -0.5F, 0F, 0.6F));
        PartDefinition left_horn = head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(82, 89).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F),
                PartPose.offsetAndRotation(4.5F, -9F, -6F, -0.35F, 0F, 0.75F));
        PartDefinition left_horn2 = left_horn.addOrReplaceChild("left_horn2", CubeListBuilder.create()
                .texOffs(0, 103).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(0F, -6F, 0F, -0.45F, 0F, -0.55F));
        PartDefinition left_horn3 = left_horn2.addOrReplaceChild("left_horn3", CubeListBuilder.create()
                .texOffs(28, 103).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(0F, -5F, 0F, -0.5F, 0F, -0.6F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(94, 34).addBox(-3.5F, -2F, -3.5F, 7F, 13F, 7F),
                PartPose.offsetAndRotation(-11.5F, -16F, 0F, -0.32F, 0F, -0.1F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(64, 73).addBox(-3.5F, 0F, -3.5F, 7F, 9F, 7F)
                .texOffs(28, 89).addBox(-3F, 9F, -3F, 6F, 5F, 6F),
                PartPose.offsetAndRotation(0F, 11F, 0F, -0.25F, 0F, 0F));
        PartDefinition axe = right_forearm.addOrReplaceChild("axe", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -24F, -1F, 2F, 32F, 2F)
                .texOffs(74, 34).addBox(-0.5F, -26F, -10F, 1F, 12F, 9F)
                .texOffs(16, 103).addBox(-1.5F, -25F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(-4F, 11.5F, -2F, 0.7F, 0F, 0F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(94, 34).addBox(-3.5F, -2F, -3.5F, 7F, 13F, 7F),
                PartPose.offsetAndRotation(11.5F, -16F, 0F, -0.32F, 0F, 0.1F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(64, 73).addBox(-3.5F, 0F, -3.5F, 7F, 9F, 7F)
                .texOffs(28, 89).addBox(-3F, 9F, -3F, 6F, 5F, 6F),
                PartPose.offsetAndRotation(0F, 11F, 0F, -0.25F, 0F, 0F));
        PartDefinition shield = left_forearm.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-12F, -26F, -7F, 24F, 30F, 3F)
                .texOffs(8, 103).addBox(-9F, -31F, -6.5F, 2F, 5F, 2F)
                .texOffs(8, 103).addBox(-1F, -31F, -6.5F, 2F, 5F, 2F)
                .texOffs(8, 103).addBox(7F, -31F, -6.5F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(-9F, 11.5F, -4F, 0.45F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MitachurlEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        boolean isAxe = entity.getMitachurlType() == MitachurlEntity.MitachurlType.BLAZING_AXE;
        this.axe.visible = isAxe;
        this.shield.visible = !isAxe;

        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.6F;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;

        // a heavy, swaying walk: legs stride, the body rolls, the free arm swings
        float walk = Mth.cos(limbSwing * 0.55F) * 1.1F * limbSwingAmount;
        this.rightLeg.xRot = walk;
        this.leftLeg.xRot = -walk;
        this.body.zRot = Mth.sin(limbSwing * 0.55F) * 0.06F * limbSwingAmount;
        this.body.xRot += Mth.sin(ageInTicks * 0.06F) * 0.02F;   // breathing
        this.rightArm.xRot += -walk * 0.5F;
        this.leftArm.xRot += walk * 0.5F;

        if (isAxe) {
            // the axe comes up and down over its head when it swings at you
            float swing = this.attackTime;
            if (swing > 0) {
                float s = Mth.sin(swing * Mth.PI);
                this.rightArm.xRot -= s * 2.4F;
                this.rightForearm.xRot -= s * 0.6F;
                this.body.xRot += s * 0.25F;
            }
        } else if (entity.isCharging()) {
            // shield forward and head down, charging
            this.body.xRot += 0.3F;
            this.leftArm.xRot -= 0.5F;
        }
    }
}