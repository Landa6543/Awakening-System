package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.RuinSerpentBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RuinSerpentModel extends EntityModel<RuinSerpentBossEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart drill;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;

    public RuinSerpentModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.drill = this.head.getChild("drill");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -12.0F, 12.0F, 12.0F, 14.0F)
                        .texOffs(0, 30).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 8.0F),
                PartPose.offset(0.0F, 16.0F, -10.0F));

        head.addOrReplaceChild("drill",
                CubeListBuilder.create()
                        .texOffs(50, 0).addBox(-4.0F, -4.0F, -18.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(50, 20).addBox(-2.0F, -2.0F, -24.0F, 4.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("body1",
                CubeListBuilder.create()
                        .texOffs(0, 56).addBox(-7.0F, -7.0F, 0.0F, 14.0F, 14.0F, 14.0F),
                PartPose.offset(0.0F, 16.0F, -4.0F));

        root.addOrReplaceChild("body2",
                CubeListBuilder.create()
                        .texOffs(0, 86).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 12.0F, 14.0F),
                PartPose.offset(0.0F, 17.0F, 8.0F));

        root.addOrReplaceChild("body3",
                CubeListBuilder.create()
                        .texOffs(0, 114).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 16.0F),
                PartPose.offset(0.0F, 18.0F, 20.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(RuinSerpentBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Giro continuo de la broca perforadora
        this.drill.zRot = ageInTicks * 0.6F;

        // Ondulación serpenteante
        float wave = Mth.sin(ageInTicks * 0.15F) * 0.3F;
        this.body1.yRot = wave;
        this.body2.yRot = -wave * 1.2F;
        this.body3.yRot = wave * 1.5F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
