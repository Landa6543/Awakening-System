package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ManaPixieEntity;
import net.minecraft.client.model.EntityModel;
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

/**
 * Modelo 3D de la Hada de Maná Silvana (Mana Pixie).
 * Diminuto ser feérico alado con 4 alas batientes, orejas élficas puntiagudas
 * y un orbe de maná puro que levita y gira entre sus manos.
 */
public class ManaPixieModel<T extends ManaPixieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pixie"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWingUpper;
    private final ModelPart rightWingUpper;
    private final ModelPart leftWingLower;
    private final ModelPart rightWingLower;
    private final ModelPart manaOrb;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ManaPixieModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftWingUpper = this.body.getChild("left_wing_upper");
        this.rightWingUpper = this.body.getChild("right_wing_upper");
        this.leftWingLower = this.body.getChild("left_wing_lower");
        this.rightWingLower = this.body.getChild("right_wing_lower");
        this.manaOrb = this.body.getChild("mana_orb");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Torso / vestido feérico (3x4x2) + falda de pétalos acampanada (5x3x4)
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                // Torso / corsé
                .texOffs(0, 11).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                // Falda de pétalos acampanada
                .texOffs(10, 11).addBox(-2.5F, 3.0F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Cabeza estilizada (5x5x5) con volumen de cabello trasero, mechones laterales, orejas élficas y flor
        body.addOrReplaceChild("head", CubeListBuilder.create()
                // Cabeza base y rostro
                .texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                // Cabello posterior estilizado que termina sobre los hombros (5x4x1)
                .texOffs(20, 0).addBox(-2.5F, -5.0F, 2.5F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                // Mechón lateral izquierdo
                .texOffs(34, 0).addBox(-3.0F, -3.0F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                // Mechón lateral derecho
                .texOffs(38, 0).addBox(2.0F, -3.0F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                // Oreja élfica puntiaguda izquierda
                .texOffs(42, 0).addBox(-4.5F, -3.5F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                // Oreja élfica puntiaguda derecha
                .texOffs(48, 0).addBox(2.5F, -3.5F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                // Horquilla de flor silvestre
                .texOffs(54, 0).addBox(-2.5F, -6.2F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -0.5F));

        // Brazos delicados (1x4x1)
        body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(28, 11).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.8F, 1.0F, 0.0F, -0.6F, 0.25F, 0.15F));

        body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(32, 11).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.8F, 1.0F, 0.0F, -0.6F, -0.25F, -0.15F));

        // Piernas feéricas delicadas (1x4x1)
        body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(36, 11).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 5.5F, 0.0F, 0.25F, 0.0F, 0.08F));

        body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(40, 11).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 5.5F, 0.0F, 0.25F, 0.0F, -0.08F));

        // Orbe de Maná Puro (3x3x3)
        body.addOrReplaceChild("mana_orb", CubeListBuilder.create()
                .texOffs(44, 11).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.5F, -2.6F));

        // 4 Alas Feéricas Anguladas hacia Atrás (dx=0, siluetas botánicas/vitral)
        body.addOrReplaceChild("left_wing_upper", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0.0F, -6.0F, 0.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 1.2F, 0.8F, 0.25F, 0.75F, -0.20F));

        body.addOrReplaceChild("right_wing_upper", CubeListBuilder.create()
                .texOffs(16, 20).addBox(0.0F, -6.0F, 0.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, 1.2F, 0.8F, 0.25F, -0.75F, 0.20F));

        body.addOrReplaceChild("left_wing_lower", CubeListBuilder.create()
                .texOffs(32, 20).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 2.4F, 0.8F, -0.15F, 0.60F, -0.10F));

        body.addOrReplaceChild("right_wing_lower", CubeListBuilder.create()
                .texOffs(44, 20).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, 2.4F, 0.8F, -0.15F, -0.60F, 0.10F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        // Flotación senoidal suave en el aire
        this.body.y = 14.0F + Mth.sin(ageInTicks * 0.15F) * 1.2F;

        // Cabeceo y giro de cabeza
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Aleteo feérico suave de alta frecuencia (flutter hacia atrás y afuera sin atravesar el cuerpo)
        float flutter = Mth.cos(ageInTicks * 22.0F * ((float)Math.PI / 180F)) * (float)Math.PI * 0.12F;
        this.leftWingUpper.yRot = 0.75F - flutter;
        this.rightWingUpper.yRot = -0.75F + flutter;
        this.leftWingLower.yRot = 0.60F - flutter * 0.8F;
        this.rightWingLower.yRot = -0.60F + flutter * 0.8F;

        // Rotación y levitación del Orbe de Maná
        this.manaOrb.yRot = ageInTicks * 0.08F;
        this.manaOrb.y = 3.0F + Mth.sin(ageInTicks * 0.22F) * 0.4F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
