package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.WaterMimicEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WaterMimicModel extends EntityModel<WaterMimicEntity> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public WaterMimicModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg0 = root.getChild("leg0");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, -6.0F));

        head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(21, 24).addBox(-0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 2.0F)
                        .texOffs(20, 24).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(3.5F, 1.0F, -8.5F, 0.0F, -0.1309F, 0.0F));

        head.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(21, 23).addBox(-0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 2.0F)
                        .texOffs(21, 24).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-3.5F, 1.0F, -8.5F, 0.0F, 0.1309F, 0.0F));

        root.addOrReplaceChild("leg0",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(-3.0F, 18.0F, 7.0F));

        root.addOrReplaceChild("leg1",
                CubeListBuilder.create().mirror()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(3.0F, 18.0F, 7.0F));

        root.addOrReplaceChild("leg2",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(-3.0F, 18.0F, -5.0F));

        root.addOrReplaceChild("leg3",
                CubeListBuilder.create().mirror()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(3.0F, 18.0F, -5.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(WaterMimicEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        this.leg0.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg1.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg2.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg3.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
