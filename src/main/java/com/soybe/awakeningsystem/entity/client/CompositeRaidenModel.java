package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RaidenBossEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.HumanoidArm;

public class CompositeRaidenModel extends EntityModel<RaidenBossEntity> implements ArmedModel {
    private final RaidenShogunModel phase1Model;
    private final RaidenPuppetModel phase2Model;
    private RaidenBossEntity currentEntity;

    public CompositeRaidenModel(RaidenShogunModel phase1Model, RaidenPuppetModel phase2Model) {
        this.phase1Model = phase1Model;
        this.phase2Model = phase2Model;
    }

    @Override
    public void setupAnim(RaidenBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.currentEntity = entity;
        if (entity.getPhase() == 2) {
            this.phase2Model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.phase1Model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (currentEntity != null && currentEntity.getPhase() == 2) {
            this.phase2Model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        } else {
            this.phase1Model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        if (currentEntity != null && currentEntity.getPhase() == 2) {
            poseStack.scale(0.0F, 0.0F, 0.0F);
        } else {
            this.phase1Model.translateToHand(arm, poseStack);
        }
    }
}
