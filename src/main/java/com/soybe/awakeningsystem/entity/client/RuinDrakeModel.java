package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinDrakeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RuinDrakeModel extends EntityModel<RuinDrakeEntity> {

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public RuinDrakeModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.head = this.bone.getChild("head");
        this.leftWing = this.bone.getChild("left_wing");
        this.rightWing = this.bone.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(48, 46).addBox(-10.0F, -10.0F, -14.0F, 20.0F, 10.0F, 24.0F)
                        .texOffs(0, 0).addBox(-13.0F, -32.0F, -14.0F, 26.0F, 22.0F, 24.0F),
                PartPose.offset(0.0F, 2.0F, 8.0F));

        bone.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 88).addBox(-6.0F, -9.8F, -20.2F, 12.0F, 14.0F, 18.0F),
                PartPose.offset(0.0F, -24.2F, -10.5F));

        bone.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 120).addBox(-12.6F, -4.7F, -3.9F, 12.0F, 10.0F, 10.0F)
                        .texOffs(100, 23).addBox(-36.0F, 0.0F, -3.0F, 36.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-19.5F, -33.5F, 0.0F, 0.0F, 0.0F, 0.8727F));

        bone.addOrReplaceChild("right_wing",
                CubeListBuilder.create().mirror()
                        .texOffs(0, 120).addBox(0.6F, -4.7F, -3.9F, 12.0F, 10.0F, 10.0F)
                        .texOffs(100, 23).addBox(0.0F, 0.0F, -3.0F, 36.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(18.8F, -34.4F, 4.0F, 0.0F, 0.0F, -0.8727F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(RuinDrakeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        float wingFlap = Mth.sin(ageInTicks * 0.25F) * 0.35F;
        this.leftWing.zRot = 0.8727F + wingFlap;
        this.rightWing.zRot = -0.8727F - wingFlap;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
