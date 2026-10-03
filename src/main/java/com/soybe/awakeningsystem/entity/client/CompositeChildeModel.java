package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.ChildeBossEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;

public class CompositeChildeModel extends EntityModel<ChildeBossEntity> implements ArmedModel {
    private final PlayerModel<ChildeBossEntity> phase1Model;
    private final ChildePhase2Model phase2Model;
    private ChildeBossEntity currentEntity;

    public CompositeChildeModel(PlayerModel<ChildeBossEntity> phase1Model, ChildePhase2Model phase2Model) {
        this.phase1Model = phase1Model;
        this.phase2Model = phase2Model;
        this.phase1Model.young = false;
    }

    @Override
    public void setupAnim(ChildeBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.currentEntity = entity;
        this.phase1Model.young = false;
        if (entity.getPhase() == 2) {
            this.phase2Model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.phase1Model.setAllVisible(true);
            if (entity.getMainHandItem().is(net.minecraft.world.item.Items.BOW)) {
                this.phase1Model.rightArmPose = entity.getTarget() != null 
                        ? net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW 
                        : net.minecraft.client.model.HumanoidModel.ArmPose.ITEM;
            } else if (!entity.getMainHandItem().isEmpty()) {
                this.phase1Model.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.ITEM;
            } else {
                this.phase1Model.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY;
            }
            this.phase1Model.leftArmPose = entity.getOffhandItem().isEmpty() 
                    ? net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY 
                    : net.minecraft.client.model.HumanoidModel.ArmPose.ITEM;
            this.phase1Model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (currentEntity != null && currentEntity.getPhase() == 2) {
            this.phase2Model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        } else {
            this.phase1Model.young = false;
            this.phase1Model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        if (currentEntity != null && currentEntity.getPhase() == 2) {
            this.phase2Model.translateToHand(arm, poseStack);
        } else {
            this.phase1Model.translateToHand(arm, poseStack);
        }
    }
}
