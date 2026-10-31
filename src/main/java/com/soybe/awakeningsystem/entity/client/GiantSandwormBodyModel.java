package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.GiantSandwormBodyPart;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GiantSandwormBodyModel extends EntityModel<GiantSandwormBodyPart> {
    private final ModelPart body;

    public GiantSandwormBodyModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-4.0F, -6.0F, -5.0F, 8.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 31).addBox(-4.0F, 5.0F, -5.0F, 8.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(36, 20).addBox(5.0F, -4.0F, -5.0F, 1.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(36, 38).addBox(-6.0F, -4.0F, -5.0F, 1.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(GiantSandwormBodyPart entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
