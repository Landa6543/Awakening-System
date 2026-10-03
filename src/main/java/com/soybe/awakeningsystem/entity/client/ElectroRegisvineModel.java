package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.ElectroRegisvineEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

public class ElectroRegisvineModel extends EntityModel<ElectroRegisvineEntity> {

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart leftarm;
    private final ModelPart rightarm;

    public ElectroRegisvineModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.head = root.getChild("head");
        this.leftarm = root.getChild("leftarm");
        this.rightarm = root.getChild("rightarm");
    }

    public static LayerDefinition createBodyLayer() {
        return CryoRegisvineModel.createBodyLayer();
    }

    @Override
    public void setupAnim(ElectroRegisvineEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.bone.xRot = 0.0F;
        this.bone.yRot = 0.0F;
        this.bone.zRot = 0.0F;

        if (entity.isStunned()) {
            this.head.xRot = 1.35F;
            this.head.yRot = 0.0F;
            this.leftarm.xRot = 1.2F;
            this.leftarm.zRot = 0.4F;
            this.rightarm.xRot = -2.4F;
            this.rightarm.zRot = 2.4F;
        } else if (entity.isSlamming()) {
            float progress = entity.getSlamProgress(ageInTicks);
            float slamAngle = Mth.sin(progress * (float) Math.PI) * 1.1F;
            this.head.xRot = slamAngle;
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.5F;
            this.leftarm.xRot = slamAngle * 0.6F;
            this.rightarm.xRot = -2.9138F + slamAngle * 0.6F;
        } else if (entity.isSpinning()) {
            float spin = ageInTicks * 0.45F;
            this.head.yRot = spin;
            this.head.xRot = 0.2F;
            this.leftarm.yRot = spin;
            this.rightarm.yRot = spin;
        } else {
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.6F;

            float armWave = Mth.cos(ageInTicks * 0.08F) * 0.12F;
            this.leftarm.xRot = armWave;
            this.rightarm.xRot = -2.9138F - armWave;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
