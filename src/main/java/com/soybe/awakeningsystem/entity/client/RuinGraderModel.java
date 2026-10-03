package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinGraderEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

public class RuinGraderModel extends EntityModel<RuinGraderEntity> {

    private final ModelPart root;
    private final ModelPart ruinGuard;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public RuinGraderModel(ModelPart root) {
        this.root = root;
        this.ruinGuard = root.getChild("RuinGuard");
        this.leftLeg = root.getChild("left_leg_ANIM");
        this.rightLeg = root.getChild("right_leg_ANIM");
        this.leftArm = root.getChild("left_arm_ANIM");
        this.rightArm = root.getChild("right_arm_ANIM");
    }

    public static LayerDefinition createBodyLayer() {
        return RuinGuardModel.createBodyLayer();
    }

    @Override
    public void setupAnim(RuinGraderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isStunned()) {
            this.ruinGuard.y = 19.0F;
            this.leftLeg.xRot = -1.2F;
            this.rightLeg.xRot = -1.2F;
            this.leftArm.xRot = 0.8F;
            this.rightArm.xRot = 0.8F;
        } else if (entity.isFiringLaser()) {
            this.ruinGuard.y = 15.0F;
            this.ruinGuard.xRot = 0.15F;
            this.leftArm.xRot = -0.5F;
            this.rightArm.xRot = -0.5F;
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.xRot = 0.0F;
        } else {
            this.ruinGuard.y = 15.0F;
            this.ruinGuard.xRot = 0.0F;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.5F * limbSwingAmount;
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
