package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.WhopperflowerEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WhopperflowerModel<T extends WhopperflowerEntity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public WhopperflowerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.rightArm = root.getChild("rightarm");
        this.leftArm = root.getChild("leftarm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 16).addBox(-1.5F, -11.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition leaves = head.addOrReplaceChild("leaves",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.0F, 0.0F, -0.3491F));

        leaves.addOrReplaceChild("head_r1",
                CubeListBuilder.create().texOffs(56, 33).addBox(-1.0F, -8.0F, -0.5F, 3.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -7.0F, -3.5F, 0.8727F, -0.6981F, 0.0F));

        leaves.addOrReplaceChild("head_r2",
                CubeListBuilder.create().texOffs(52, 19).addBox(-2.4088F, -8.7865F, 0.0419F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.9088F, -11.7135F, -7.5419F, 2.138F, -0.6981F, 0.0F));

        PartDefinition leaves2 = head.addOrReplaceChild("leaves2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.0F, 1.5708F, 0.3491F));

        leaves2.addOrReplaceChild("head_r3",
                CubeListBuilder.create().texOffs(56, 33).addBox(-1.0F, -8.0F, -0.5F, 3.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -7.0F, -3.5F, 0.8727F, -0.6981F, 0.0F));

        leaves2.addOrReplaceChild("head_r4",
                CubeListBuilder.create().texOffs(52, 19).addBox(-2.4088F, -8.7865F, 0.0419F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.9088F, -11.7135F, -7.5419F, 2.138F, -0.6981F, 0.0F));

        PartDefinition leaves3 = head.addOrReplaceChild("leaves3",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.6832F, -11.7055F, 4.3444F, 2.6467F, -0.5989F, -2.8086F));

        leaves3.addOrReplaceChild("head_r5",
                CubeListBuilder.create().texOffs(56, 33).addBox(-1.0F, -8.0F, -0.5F, 3.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.1749F, 2.3978F, 4.2074F, 0.8727F, -0.6981F, 0.0F));

        leaves3.addOrReplaceChild("head_r6",
                CubeListBuilder.create().texOffs(52, 19).addBox(-2.4088F, -8.7865F, 0.0419F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.2661F, -2.3156F, 0.1655F, 2.138F, -0.6981F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 19).addBox(-1.0F, -19.0F, -1.0F, 2.0F, 19.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 23).addBox(-1.0F, -17.0F, -2.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(29, 53).addBox(-1.0F, -19.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 23).addBox(-1.0F, -17.0F, 1.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(-3.0F, -8.0F, -2.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(-2.0F, -8.0F, 1.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(-2.0F, -8.0F, -3.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 56).addBox(-2.0F, -8.0F, -3.03F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 42).addBox(1.0F, -8.0F, -2.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 45).addBox(1.0F, -5.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 45).addBox(-2.0F, -5.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 45).addBox(1.0F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 45).addBox(-2.0F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(58, 54).addBox(0.5F, 0.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, -8.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        body.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(58, 54).addBox(-1.5F, 0.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, -8.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        body.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(18, 23).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 23).addBox(-1.0F, -0.5F, -3.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -16.5F, 1.5F, 0.0F, 0.0F, -0.6109F));

        body.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(18, 23).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 23).addBox(-1.0F, -0.5F, -3.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -16.5F, 1.5F, 0.0F, 0.0F, 0.6109F));

        body.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                        .texOffs(30, 25).addBox(-2.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(1.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(1.0F, 7.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-2.0F, 7.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -18.5F, 1.5F, 0.3054F, 0.0F, 0.0F));

        body.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                        .texOffs(30, 25).addBox(1.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-2.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-2.0F, 7.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(1.0F, 7.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -18.5F, -1.5F, -0.3054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(19, 42).addBox(3.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(19, 42).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.5F, 22.0F, 0.0F));

        PartDefinition rightarm = partdefinition.addOrReplaceChild("rightarm",
                CubeListBuilder.create(),
                PartPose.offset(1.5F, 5.5F, 0.0F));

        rightarm.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                        .texOffs(30, 25).addBox(-0.5F, 6.5F, 1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, 2.5F, 1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, 6.5F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, 2.5F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

        PartDefinition leftarm = partdefinition.addOrReplaceChild("leftarm",
                CubeListBuilder.create(),
                PartPose.offset(-1.5F, 5.5F, 0.0F));

        leftarm.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                        .texOffs(30, 25).addBox(-0.5F, 6.5F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, 6.5F, 1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, 2.5F, 1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, 2.5F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 25).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.leg1.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.leg2.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;

        // Balanceo sutil de la flor
        this.head.zRot = Mth.sin(ageInTicks * 0.08F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
