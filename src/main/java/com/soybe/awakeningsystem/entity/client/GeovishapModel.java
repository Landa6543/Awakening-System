package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;

public class GeovishapModel<T extends Monster> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;

    public GeovishapModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        return JadeArmoredBeastModel.createBodyLayer();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.5F;
        this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.5F;

        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;

        this.tail.yRot = Mth.cos(limbSwing * 0.6662F) * 0.3F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
