package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
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

public class AbyssalOrcModel extends HierarchicalModel<AbyssalOrcEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_orc"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AbyssalOrcModel(ModelPart root) {
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

        // Cabeza ancha con colmillos de marfil inferiores (UV: 0..34, 0..16)
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -8.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                // Colmillo derecho (UV: 36..44, 0..5)
                .texOffs(36, 0).addBox(-3.5F, -3.0F, -4.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                // Colmillo izquierdo (UV: 36..44, 0..5)
                .texOffs(36, 0).mirror().addBox(1.5F, -3.0F, -4.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -1.0F));

        // Torso musculoso y pesado (UV: 0..34, 20..39)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5.5F, 0.0F, -3.0F, 11.0F, 13.0F, 6.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // Brazo derecho robusto con hombrera pesada (UV Brazo: 38..58, 20..38 | Pauldron: 82..106, 0..11)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(38, 20).addBox(-4.0F, -2.5F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                // Hombrera pesada con remaches
                .texOffs(82, 0).addBox(-5.5F, -4.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.2F)),
                PartPose.offset(-6.5F, 0.0F, 0.0F));

        // Brazo izquierdo robusto con hombrera (UV Brazo: 60..80, 20..38 | Pauldron: 82..106, 14..25)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(60, 20).addBox(-1.0F, -2.5F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                // Hombrera
                .texOffs(82, 14).mirror().addBox(-0.5F, -4.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.2F)),
                PartPose.offset(6.5F, 0.0F, 0.0F));

        // Pierna derecha pesada (UV: 0..20, 42..60)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.8F, 11.0F, 0.0F));

        // Pierna izquierda pesada (UV: 22..42, 42..60)
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(22, 42).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.8F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AbyssalOrcEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Cabeza
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Marcha pesada de bruto con balanceo lateral y balanceo de torso
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.55F) * 1.15F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.55F + (float) Math.PI) * 1.15F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.55F + (float) Math.PI) * 0.9F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.55F) * 0.9F * limbSwingAmount;
        this.body.zRot = Mth.sin(limbSwing * 0.55F) * 0.08F * limbSwingAmount;
        this.body.yRot = Mth.sin(limbSwing * 0.55F) * 0.06F * limbSwingAmount;

        // Respiración pesada en reposo (jadeo de bestia y elevación de hombros)
        if (limbSwingAmount < 0.1F) {
            float breath = Mth.sin(ageInTicks * 0.08F);
            this.body.y += breath * 0.5F;
            this.head.y += breath * 0.4F;
            this.body.xRot = breath * 0.03F;
            this.rightArm.zRot = 0.15F + breath * 0.04F;
            this.leftArm.zRot = -0.15F - breath * 0.04F;
        }

        // Postura salvaje durante Frenesí de Sangre
        if (entity.bloodFrenzyTicks > 0) {
            this.body.xRot += 0.16F;
            this.head.xRot -= 0.12F;
            this.rightArm.zRot += 0.22F;
            this.leftArm.zRot -= 0.22F;
            this.rightArm.xRot += 0.15F;
            this.leftArm.xRot += 0.15F;
        }

        // Animación de Golpe Aplastante (Ground Smash a dos manos)
        if (entity.smashAnimationTimeout > 0) {
            float progress = (25 - entity.smashAnimationTimeout) / 25.0F;
            if (progress < 0.5F) {
                // Elevar ambos brazos hacia arriba y arquear torso hacia atrás
                float up = progress * 2.0F;
                this.rightArm.xRot = -2.4F * up;
                this.leftArm.xRot = -2.4F * up;
                this.body.xRot = -0.3F * up;
            } else {
                // Golpe descendente brutal contra el suelo
                float down = (progress - 0.5F) * 2.0F;
                this.rightArm.xRot = -2.4F + down * 2.8F;
                this.leftArm.xRot = -2.4F + down * 2.8F;
                this.body.xRot = -0.3F + down * 0.7F;
            }
        }

        // Animación de Rugido de Guerra
        if (entity.roarAnimationTimeout > 0) {
            float progress = (30 - entity.roarAnimationTimeout) / 30.0F;
            float roarShake = Mth.sin(progress * (float) Math.PI * 6.0F) * 0.12F;
            this.head.xRot = -0.65F + roarShake;
            this.rightArm.zRot = 0.85F;
            this.leftArm.zRot = -0.85F;
            this.rightArm.xRot = -0.4F;
            this.leftArm.xRot = -0.4F;
        }
    }
}
