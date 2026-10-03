package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowInfantryEntity;
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

public class ShadowInfantryModel extends HierarchicalModel<ShadowInfantryEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_infantry"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ShadowInfantryModel(ModelPart root) {
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

        // Cabeza y Yelmo con penacho/cabello de Igris (Capa Base UV: 0, 0 | Capa Exterior UV: 32, 0 con deformación 0.35F)
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.35F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Torso y Coraza de Caballero Oscuro (Capa Base UV: 16, 16 | Capa Exterior UV: 16, 32 con deformación 0.25F)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo derecho y hombrera/guantelete (Capa Base UV: 40, 16 | Capa Exterior UV: 40, 32 con deformación 0.25F)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Brazo izquierdo y hombrera/guantelete (Capa Base UV: 32, 48 | Capa Exterior UV: 48, 48 con deformación 0.25F)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        // Pierna derecha y greba (Capa Base UV: 0, 16 | Capa Exterior UV: 0, 32 con deformación 0.2F)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        // Pierna izquierda y greba (Capa Base UV: 16, 48 | Capa Exterior UV: 0, 48 con deformación 0.2F)
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ShadowInfantryEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Rotación de cabeza según la vista
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Marcha marcial agresiva con leve inclinación frontal
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.3F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.3F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        this.body.xRot = 0.08F * limbSwingAmount;

        // Respiración / Balanceo tétrico en reposo
        if (limbSwingAmount < 0.1F) {
            float breath = Mth.sin(ageInTicks * 0.09F) * 0.04F;
            this.body.y += breath * 3.0F;
            this.head.y += breath * 3.0F;
            this.rightArm.zRot = 0.08F + breath;
            this.leftArm.zRot = -0.08F - breath;
        }

        // Animación de ataque / tajo sombrío (3-fases: carga, estocada y corte)
        if (entity.attackAnimationTimeout > 0) {
            float progress = (24 - entity.attackAnimationTimeout) / 24.0F;
            float swing = Mth.sin(progress * (float) Math.PI);
            this.rightArm.xRot = -1.7F + swing * 1.4F;
            this.rightArm.yRot = -0.6F + swing * 1.1F;
            this.rightArm.zRot = 0.25F;
            this.body.yRot = swing * 0.45F;
            this.leftArm.xRot = 0.3F * swing;
        }

        // Animación de lanzamiento de Hojas del Vacío (Conjuración con ambas manos)
        if (entity.castAnimationTimeout > 0) {
            float progress = (20 - entity.castAnimationTimeout) / 20.0F;
            float pulse = Mth.sin(progress * (float) Math.PI);
            this.rightArm.xRot = -1.4F - pulse * 0.5F;
            this.rightArm.yRot = -0.4F - pulse * 0.3F;
            this.leftArm.xRot = -1.4F - pulse * 0.5F;
            this.leftArm.yRot = 0.4F + pulse * 0.3F;
            this.body.xRot = 0.2F * pulse;
        }
    }
}
