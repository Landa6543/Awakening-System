package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RevivalPrismModel extends EntityModel<ElectroRevivalPrismEntity> {

    private final ModelPart root;
    private final ModelPart crystal;
    private final ModelPart haloRing;

    public RevivalPrismModel(ModelPart root) {
        this.root = root;
        this.crystal = root.getChild("crystal");
        this.haloRing = root.getChild("halo_ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition c = root.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        c.addOrReplaceChild("column",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 14.0F, 6.0F),
                PartPose.ZERO);
        c.addOrReplaceChild("spike_top",
                CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -13.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.ZERO);
        c.addOrReplaceChild("spike_bottom",
                CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.ZERO);

        PartDefinition ring = root.addOrReplaceChild("halo_ring", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        ring.addOrReplaceChild("r_n", CubeListBuilder.create().texOffs(0, 22).addBox(-6.0F, -0.5F, -6.0F, 12.0F, 1.0F, 2.0F), PartPose.ZERO);
        ring.addOrReplaceChild("r_s", CubeListBuilder.create().texOffs(0, 22).addBox(-6.0F, -0.5F, 4.0F, 12.0F, 1.0F, 2.0F), PartPose.ZERO);
        ring.addOrReplaceChild("r_w", CubeListBuilder.create().texOffs(0, 22).addBox(-6.0F, -0.5F, -4.0F, 2.0F, 1.0F, 8.0F), PartPose.ZERO);
        ring.addOrReplaceChild("r_e", CubeListBuilder.create().texOffs(0, 22).addBox(4.0F, -0.5F, -4.0F, 2.0F, 1.0F, 8.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ElectroRevivalPrismEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float bob = Mth.sin(ageInTicks * 0.07F) * 1.5F;
        this.root.setPos(0.0F, 12.0F + bob, 0.0F);

        this.crystal.yRot = ageInTicks * 0.04F;
        this.crystal.xRot = Mth.sin(ageInTicks * 0.03F) * 0.08F;

        float pulse = 1.0F + Mth.sin(ageInTicks * 0.15F) * 0.05F;
        this.crystal.xScale = pulse;
        this.crystal.zScale = pulse;

        this.haloRing.yRot = -ageInTicks * 0.07F;
        this.haloRing.zRot = Mth.cos(ageInTicks * 0.05F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
