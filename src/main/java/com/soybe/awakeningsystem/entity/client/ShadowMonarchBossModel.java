package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowMonarchBossEntity;
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

public class ShadowMonarchBossModel extends HierarchicalModel<ShadowMonarchBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_monarch_boss"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ShadowMonarchBossModel(ModelPart root) {
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

        // Cabeza con Capa Exterior 3D (Base UV: 0, 0 | Capa Exterior UV: 32, 0 con deformación 0.4F)
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Torso del Monarca (Base UV: 16, 16 | Capa Exterior UV: 16, 32 con deformación 0.35F)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo derecho (Base UV: 40, 16 | Capa Exterior UV: 40, 32 con deformación 0.3F)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Brazo izquierdo (Base UV: 32, 48 | Capa Exterior UV: 48, 48 con deformación 0.3F)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        // Pierna derecha (Base UV: 0, 16 | Capa Exterior UV: 0, 32 con deformación 0.25F)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        // Pierna izquierda (Base UV: 16, 48 | Capa Exterior UV: 0, 48 con deformación 0.25F)
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ShadowMonarchBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Cabeza
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Levitación flotante continua de Monarca de las Sombras
        float hover = Mth.sin(ageInTicks * 0.08F) * 1.6F;
        this.root.y += hover;

        // Marcha de jefe
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6F) * 1.2F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6F + (float) Math.PI) * 0.8F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6F) * 0.8F * limbSwingAmount;

        // Postura estática imponente y respiración noble
        if (limbSwingAmount < 0.15F) {
            float idleBreath = Mth.sin(ageInTicks * 0.07F);
            this.body.xRot = -0.05F + idleBreath * 0.02F;
            this.rightArm.zRot = 0.14F + idleBreath * 0.03F;
            this.leftArm.zRot = -0.14F - idleBreath * 0.03F;
        }

        // Animación de Gran Tajo (Slash 360 con Gran Espada)
        if (entity.slashAnimationTimeout > 0) {
            float progress = (20 - entity.slashAnimationTimeout) / 20.0F;
            float swing = Mth.sin(progress * (float) Math.PI);
            this.rightArm.xRot = -1.7F + swing * 0.5F;
            this.rightArm.yRot = -1.2F + swing * 2.5F;
            this.body.yRot = -0.4F + swing * 0.9F;
        }

        // Animación de Conjuración / Invocación con Mano Izquierda
        if (entity.castAnimationTimeout > 0) {
            float progress = (30 - entity.castAnimationTimeout) / 30.0F;
            float castPulse = Mth.sin(progress * (float) Math.PI * 2.0F) * 0.2F;
            this.leftArm.xRot = -2.5F + castPulse;
            this.leftArm.zRot = -0.5F;
            this.head.xRot = -0.4F;
        }

        // Animación de Autoridad del Gobernante (Telequinesis / Void Pull)
        if (entity.pullAnimationTimeout > 0) {
            float progress = (30 - entity.pullAnimationTimeout) / 30.0F;
            float pulse = Mth.sin(progress * (float) Math.PI * 3.0F) * 0.12F;
            this.rightArm.xRot = -1.55F + pulse;
            this.rightArm.yRot = -0.3F;
            this.leftArm.xRot = -1.55F + pulse;
            this.leftArm.yRot = 0.3F;
            this.body.xRot = 0.12F;
            this.head.xRot = -0.25F;
        }
    }
}
