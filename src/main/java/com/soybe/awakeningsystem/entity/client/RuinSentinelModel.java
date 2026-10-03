package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinSentinelEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RuinSentinelModel extends EntityModel<RuinSentinelEntity> {

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public RuinSentinelModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition core = root.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("leg1",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(-4.0F, 12.0F, 3.0F, 0.2F, 0.0F, -0.3F));

        root.addOrReplaceChild("leg2",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(4.0F, 12.0F, 3.0F, 0.2F, 0.0F, 0.3F));

        root.addOrReplaceChild("leg3",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, -5.0F, -0.3F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(RuinSentinelEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.core.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.core.xRot = headPitch * ((float) Math.PI / 180F);

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
        this.leg1.xRot = 0.2F + legSwing;
        this.leg2.xRot = 0.2F - legSwing;
        this.leg3.xRot = -0.3F + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
