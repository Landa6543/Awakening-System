package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.SpecterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class SpecterModel<T extends SpecterEntity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart bb_main;

    public SpecterModel(ModelPart root) {
        this.root = root;
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -19.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(39, 0).addBox(-1.0F, -23.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 0).mirror().addBox(-3.0F, -21.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(56, 0).addBox(1.0F, -21.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 53).addBox(-1.0F, -20.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 61).addBox(-5.0F, -20.0F, -1.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.0F, -20.0F, -3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(1.0F, -20.0F, -3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(1.0F, -20.0F, 1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.0F, -20.0F, 1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 39).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(29, 49).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bb_main.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(0, 33).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -7.0F, 1.5F, 0.2618F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(0, 33).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -7.0F, -1.5F, -0.2618F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("cube_r3",
                CubeListBuilder.create().texOffs(0, 33).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.5F, -7.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        bb_main.addOrReplaceChild("cube_r4",
                CubeListBuilder.create().texOffs(0, 33).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -7.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        bb_main.addOrReplaceChild("cube_r5",
                CubeListBuilder.create().texOffs(0, 51).addBox(-5.5F, -6.0F, 1.0218F, 5.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.6486F, -16.6156F, 9.9782F, -2.6591F, -0.8138F, 0.0017F));

        bb_main.addOrReplaceChild("cube_r6",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.5F, -6.0F, -0.3453F, 5.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(6.8479F, -16.4602F, 6.8453F, -2.5102F, 0.7879F, 0.1022F));

        bb_main.addOrReplaceChild("cube_r7",
                CubeListBuilder.create().texOffs(0, 51).addBox(-2.5F, -10.0F, 1.0F, 5.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, -19.0F, -4.5F, 2.3736F, -0.5236F, -0.0349F));

        bb_main.addOrReplaceChild("cube_r8",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-3.0F, -10.0F, 0.0F, 5.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-5.0F, -19.0F, -5.0F, 2.3736F, 0.5236F, -0.0349F));

        bb_main.addOrReplaceChild("cube_r9",
                CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -20.5F, 2.0F, 0.0F, -1.5708F, 0.0F));

        bb_main.addOrReplaceChild("cube_r10",
                CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -20.5F, -2.0F, 0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float bob = Mth.sin(ageInTicks * 0.1F) * 2.0F;
        this.bb_main.y = 24.0F + bob;
        this.bb_main.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.bb_main.xRot = headPitch * ((float) Math.PI / 180F);

        if (entity.isEnraged()) {
            this.bb_main.yRot += ageInTicks * 0.15F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
