package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JiangshiEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class JiangshiModel extends HierarchicalModel<JiangshiEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jiangshi"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public JiangshiModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cabeza con sombrero oficial mandarín Qing y talismán taoísta sellador
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                // Sombrero mandarín Qing - Ala (Brim)
                .texOffs(32, 0).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                // Sombrero mandarín Qing - Domo/Corona superior (Dome)
                .texOffs(72, 0).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                // Remate central y borla de seda roja (Finial & Tassel)
                .texOffs(100, 0).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                // Talismán taoísta sellador de papel amarillo colgante en la frente
                .texOffs(108, 0).addBox(-2.0F, -8.0F, -4.5F, 4.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Cuerpo con túnica imperial ceremonial
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.2F))
                // Faldón de túnica ceremonial
                .texOffs(24, 16).addBox(-4.5F, 10.0F, -2.5F, 9.0F, 8.0F, 5.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazos estirados rígidamente hacia el frente a 90 grados
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, -1.45F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(72, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, -1.45F, 0.0F, 0.0F));

        // Piernas rígidas
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(JiangshiEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.isSealed()) {
            this.head.xRot = 0.12F;
            this.head.yRot = 0.0F;
            this.rightArm.xRot = -1.45F;
            this.leftArm.xRot = -1.45F;
            this.rightLeg.xRot = 0.0F;
            this.leftLeg.xRot = 0.0F;
            this.root.y = 0.0F;
            return;
        }

        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        // Brazos estirados al frente rígidamente
        this.rightArm.xRot = -1.45F;
        this.leftArm.xRot = -1.45F;

        // Movimiento de salto con piernas juntas
        float hopPhase = Mth.sin(ageInTicks * 0.4F);
        if (entity.getDeltaMovement().y > 0.05D) {
            this.rightLeg.xRot = -0.3F;
            this.leftLeg.xRot = -0.3F;
            this.root.y = -2.0F;
        } else {
            this.rightLeg.xRot = 0.0F;
            this.leftLeg.xRot = 0.0F;
            this.root.y = 0.0F;
        }
    }
}
