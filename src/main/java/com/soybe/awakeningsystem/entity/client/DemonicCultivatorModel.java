package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.DemonicCultivatorEntity;
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

public class DemonicCultivatorModel extends HierarchicalModel<DemonicCultivatorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "demonic_cultivator"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public DemonicCultivatorModel(ModelPart root) {
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

        // Cabeza con sombrero cónico tradicional (douli) y máscara/velo
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                // Sombrero cónico tradicional (douli)
                .texOffs(32, 0).addBox(-6.0F, -9.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(40, 14).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                // Velo oscuro
                .texOffs(0, 48).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 6.0F, 9.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Cuerpo con túnica marcial negra y faja carmesí
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F))
                // Faldón de túnica marcial
                .texOffs(24, 22).addBox(-4.5F, 11.0F, -2.5F, 9.0F, 7.0F, 5.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo derecho con espada marcial china (Dao / Jian)
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2.5F, -1.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offset(-4.5F, 2.0F, 0.0F));

        // Espada marcial china del Culto Demoníaco (Sable Dao de Qi Carmesí con hoja esbelta y punta afilada)
        rightArm.addOrReplaceChild("sword", CubeListBuilder.create()
                // Empuñadura envuelta en seda negra
                .texOffs(48, 24).addBox(-1.4F, 8.7F, -0.5F, 0.8F, 1.1F, 4.2F, new CubeDeformation(0.0F))
                // Pomo dorado de dragón
                .texOffs(56, 24).addBox(-1.5F, 8.5F, 3.7F, 1.0F, 1.5F, 1.0F, new CubeDeformation(0.0F))
                // Borla imperial de seda roja con anillo de jade colgante
                .texOffs(62, 24).addBox(-1.2F, 9.8F, 3.9F, 0.4F, 4.2F, 0.4F, new CubeDeformation(0.0F))
                // Guarda circular tradicional de bronce oscuro con fauces de dragón
                .texOffs(48, 30).addBox(-2.0F, 8.0F, -1.8F, 2.0F, 2.4F, 1.3F, new CubeDeformation(0.0F))
                // Hoja curva principal de sable Dao (esbelta y delgada, grosor 0.35F)
                .texOffs(48, 34).addBox(-1.2F, 8.4F, -15.5F, 0.35F, 1.6F, 13.8F, new CubeDeformation(0.0F))
                // Transición afilada de la punta (tapering elegante)
                .texOffs(78, 34).addBox(-1.15F, 8.6F, -18.5F, 0.28F, 1.2F, 3.0F, new CubeDeformation(0.0F))
                // Punta de aguja afilada (sin bloques planos)
                .texOffs(86, 34).addBox(-1.1F, 8.8F, -20.5F, 0.2F, 0.8F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo izquierdo con manga amplia
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 32).mirror().addBox(-0.5F, -1.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offset(4.5F, 2.0F, 0.0F));

        // Piernas
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(DemonicCultivatorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.7F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.7F + Mth.PI) * 1.0F * limbSwingAmount;

        // Postura de espadachín
        this.rightArm.xRot = Mth.cos(limbSwing * 0.7F + Mth.PI) * 0.8F * limbSwingAmount - 0.35F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.7F) * 0.8F * limbSwingAmount;
    }
}
