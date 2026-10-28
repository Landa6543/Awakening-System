package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SculkGolemEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SculkGolemModel extends EntityModel<SculkGolemEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sculk_golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public SculkGolemModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftAntenna = this.head.getChild("left_antenna");
        this.rightAntenna = this.head.getChild("right_antenna");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F)
                        .texOffs(42, 41).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F)
                        .texOffs(42, 63).addBox(-2.0F, 0.0F, 5.1F, 6.0F, 11.0F, 0.0F)
                        .texOffs(42, 52).addBox(-4.0F, 0.0F, 5.15F, 6.0F, 11.0F, 0.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(20, 23).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F)
                        .texOffs(44, 23).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));

        head.addOrReplaceChild("left_antenna",
                CubeListBuilder.create()
                        .texOffs(52, 23).addBox(-10.0F, -10.0F, 0.0F, 10.0F, 10.0F, 0.0F),
                PartPose.offset(-4.0F, -8.0F, 0.0F));

        head.addOrReplaceChild("right_antenna",
                CubeListBuilder.create()
                        .texOffs(52, 23).mirror().addBox(0.0F, -10.0F, 0.0F, 10.0F, 10.0F, 0.0F),
                PartPose.offset(4.0F, -8.0F, 0.0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-1.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F),
                PartPose.offset(10.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 23).mirror().addBox(-3.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F),
                PartPose.offset(-10.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 59).addBox(-2.5F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
                PartPose.offset(5.0F, 8.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(20, 41).addBox(-3.5F, 0.0F, -2.5F, 6.0F, 16.0F, 5.0F),
                PartPose.offset(-4.0F, 8.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(SculkGolemEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isStatue()) {
            // Postura de estatua dormida: cuerpo y cabeza inclinados hacia adelante mirando al suelo
            this.body.xRot = 0.35F;
            this.body.yRot = 0.0F;
            this.body.zRot = 0.0F;
            this.body.z = -0.75F;
            this.body.y = -5.0F;

            this.head.xRot = 0.50F;
            this.head.yRot = 0.0F;

            this.leftArm.xRot = -0.25F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.05F;
            this.leftArm.y = 0.0F;

            this.rightArm.xRot = -0.25F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = -0.05F;
            this.rightArm.y = 0.0F;

            this.leftAntenna.xRot = -0.20F;
            this.rightAntenna.xRot = -0.20F;

            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        } else {
            this.body.xRot = 0.0F;
            this.body.yRot = 0.0F;
            this.body.zRot = 0.0F;
            this.body.z = 0.0F;
            this.body.y = -7.0F;

            this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
            this.head.xRot = headPitch * ((float)Math.PI / 180F);

            float twitch = Mth.sin(ageInTicks * 0.15F) * 0.12F;
            this.leftAntenna.xRot = twitch;
            this.rightAntenna.xRot = twitch;

            this.rightLeg.xRot = -1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;
            this.leftLeg.xRot = 1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;
            this.rightLeg.yRot = 0.0F;
            this.leftLeg.yRot = 0.0F;

            if (entity.isAttacking() || entity.getAttackAnimationTick() > 0) {
                float attackProgress = (float)entity.getAttackAnimationTick() / 15.0F;
                float swing = Mth.sin(attackProgress * (float)Math.PI);
                this.rightArm.xRot = -2.2F * swing;
                this.leftArm.xRot = -2.2F * swing;
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
                this.rightArm.zRot = 0.0F;
                this.leftArm.zRot = 0.0F;
                this.rightArm.y = 0.0F;
                this.leftArm.y = 0.0F;
            } else {
                this.rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
                this.leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
                this.rightArm.zRot = 0.0F;
                this.leftArm.zRot = 0.0F;
                this.rightArm.y = 0.0F;
                this.leftArm.y = 0.0F;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
