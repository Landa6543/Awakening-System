package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class RevivalOrbModel<T extends net.minecraft.world.entity.Entity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart ring1;
    private final ModelPart ring2;
    private final ModelPart wings;

    public RevivalOrbModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        this.ring1 = root.getChild("ring1");
        this.ring2 = root.getChild("ring2");
        this.wings = root.getChild("wings");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Núcleo de diamante de viento (7x7x7)
        root.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 2. Anillo de viento ecuatorial (hollow square ring 12x1x12)
        PartDefinition r1 = root.addOrReplaceChild("ring1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        r1.addOrReplaceChild("bar_n", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, -0.5F, -6.0F, 12.0F, 1.0F, 2.0F), PartPose.ZERO);
        r1.addOrReplaceChild("bar_s", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, -0.5F, 4.0F, 12.0F, 1.0F, 2.0F), PartPose.ZERO);
        r1.addOrReplaceChild("bar_w", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, -0.5F, -4.0F, 2.0F, 1.0F, 8.0F), PartPose.ZERO);
        r1.addOrReplaceChild("bar_e", CubeListBuilder.create().texOffs(0, 16).addBox(4.0F, -0.5F, -4.0F, 2.0F, 1.0F, 8.0F), PartPose.ZERO);

        // 3. Anillo de viento meridional vertical (1x12x12)
        PartDefinition r2 = root.addOrReplaceChild("ring2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        r2.addOrReplaceChild("vbar_t", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, -6.0F, -6.0F, 1.0F, 2.0F, 12.0F), PartPose.ZERO);
        r2.addOrReplaceChild("vbar_b", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, 4.0F, -6.0F, 1.0F, 2.0F, 12.0F), PartPose.ZERO);
        r2.addOrReplaceChild("vbar_f", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, -4.0F, 4.0F, 1.0F, 8.0F, 2.0F), PartPose.ZERO);
        r2.addOrReplaceChild("vbar_k", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, -4.0F, -6.0F, 1.0F, 8.0F, 2.0F), PartPose.ZERO);

        // 4. 3 Aspas/Alas curvadas de viento flotantes
        PartDefinition w = root.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        for (int i = 0; i < 3; i++) {
            float angle = i * ((float) Math.PI * 2.0F / 3.0F);
            float wx = Mth.cos(angle) * 7.5F;
            float wz = Mth.sin(angle) * 7.5F;
            w.addOrReplaceChild("wing_" + i,
                    CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 6.0F, 1.0F),
                    PartPose.offsetAndRotation(wx, 0.0F, wz, 0.3F, -angle, 0.2F));
        }

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float bob = Mth.sin(ageInTicks * 0.08F) * 1.8F;
        this.root.setPos(0.0F, 14.0F + bob, 0.0F);

        // Núcleo de diamante que rota a 45 grados y pulsa
        this.core.xRot = 0.7854F + Mth.sin(ageInTicks * 0.06F) * 0.15F;
        this.core.zRot = 0.7854F;
        this.core.yRot = ageInTicks * 0.05F;

        float pulse = 1.0F + Mth.sin(ageInTicks * 0.12F) * 0.06F;
        this.core.xScale = pulse;
        this.core.yScale = pulse;
        this.core.zScale = pulse;

        // Anillos giroscópicos con rotaciones cruzadas continuas
        this.ring1.yRot = ageInTicks * 0.04F;
        this.ring1.xRot = Mth.sin(ageInTicks * 0.03F) * 0.15F;

        this.ring2.yRot = -ageInTicks * 0.05F;
        this.ring2.zRot = 0.7854F + Mth.cos(ageInTicks * 0.04F) * 0.15F;

        // Alas de viento que orbitan el núcleo
        this.wings.yRot = ageInTicks * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
