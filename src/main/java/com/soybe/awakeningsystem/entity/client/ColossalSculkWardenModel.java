package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ColossalSculkWardenEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D para el Coloso Primordial de Sculk (Piso 10).
 * - Orejas auténticas del Warden oficial de Minecraft (planos 16x16 transparentes con cuernos acústicos).
 * - UVs limpias sin solapamiento (brazos en 76, 16; orejas en 52, 32 y 58, 0).
 * - Cero parpadeo ni Z-fighting (sin capas superpuestas).
 * - Animaciones agresivas y dinámicas de ataque, respiración profunda y zancadas con inercia.
 */
public class ColossalSculkWardenModel extends EntityModel<ColossalSculkWardenEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "colossal_sculk_warden"), "main");

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart waist;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart rightTendril;
    private final ModelPart leftTendril;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ColossalSculkWardenModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.waist = this.bone.getChild("waist");
        this.chest = this.waist.getChild("chest");
        this.head = this.chest.getChild("head");
        this.rightTendril = this.head.getChild("right_tendril");
        this.leftTendril = this.head.getChild("left_tendril");
        this.rightArm = this.chest.getChild("right_arm");
        this.leftArm = this.chest.getChild("left_arm");
        this.rightLeg = this.bone.getChild("right_leg");
        this.leftLeg = this.bone.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        // 1. Cintura / Torso Inferior Articulado (10x6x8)
        PartDefinition waist = bone.addOrReplaceChild("waist",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-5.0F, -6.0F, -4.0F, 10.0F, 6.0F, 8.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        // 2. Tórax Monolítico (18x12x10) - Una sola caja limpia sin cajas coplanares
        PartDefinition chest = waist.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        // Tórax principal
                        .texOffs(0, 0).addBox(-9.0F, -12.0F, -5.0F, 18.0F, 12.0F, 10.0F)
                        // Espina dorsal central superior
                        .texOffs(26, 58).addBox(-1.5F, -17.0F, 4.0F, 3.0F, 7.0F, 3.0F)
                        // Espina dorsal izquierda
                        .texOffs(26, 58).addBox(3.0F, -14.0F, 4.0F, 3.0F, 5.0F, 3.0F)
                        // Espina dorsal derecha
                        .texOffs(26, 58).addBox(-6.0F, -14.0F, 4.0F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        // 3. Yelmo Ancestral Tallado (8x8x8)
        PartDefinition head = chest.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -12.0F, -1.0F));

        // Antenas Acústicas Auténticas del Warden Oficial de Minecraft (Planos 16x16)
        // Oreja derecha: UV (52, 32), se expande hacia la derecha de -4 a -20
        head.addOrReplaceChild("right_tendril",
                CubeListBuilder.create()
                        .texOffs(52, 32).addBox(-16.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F),
                PartPose.offset(-4.0F, -5.0F, 0.0F));

        // Oreja izquierda: UV (58, 0), se expande hacia la izquierda de +4 a +20
        head.addOrReplaceChild("left_tendril",
                CubeListBuilder.create()
                        .texOffs(58, 0).addBox(0.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F),
                PartPose.offset(4.0F, -5.0F, 0.0F));

        // 4. Brazos Colosales Demoledores (6x26x6) en UV (76, 16) y hombreras en UV dedicada (0, 80)
        chest.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(76, 16).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 26.0F, 6.0F)
                        .texOffs(0, 80).addBox(-4.0F, -3.0F, -3.5F, 7.0F, 6.0F, 7.0F),
                PartPose.offset(-12.0F, -10.0F, 0.0F));

        chest.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(76, 16).mirror().addBox(-3.0F, -2.0F, -3.0F, 6.0F, 26.0F, 6.0F)
                        .texOffs(0, 80).mirror().addBox(-3.0F, -3.0F, -3.5F, 7.0F, 6.0F, 7.0F),
                PartPose.offset(12.0F, -10.0F, 0.0F));

        // 5. Patas Pilares Pesadas de Titán (6x16x6)
        bone.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 58).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 16.0F, 6.0F),
                PartPose.offset(-5.0F, -16.0F, 0.0F));

        bone.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 58).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 16.0F, 6.0F),
                PartPose.offset(5.0F, -16.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(ColossalSculkWardenEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset poses
        this.bone.resetPose();
        this.waist.resetPose();
        this.chest.resetPose();
        this.head.resetPose();
        this.rightTendril.resetPose();
        this.leftTendril.resetPose();
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        this.rightLeg.resetPose();
        this.leftLeg.resetPose();

        // 1. Orientación y mirada del yelmo
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        // 2. Respiración profunda y viva (claramente visible)
        float breath = Mth.sin(ageInTicks * 0.08F);
        float breathCos = Mth.cos(ageInTicks * 0.08F);
        this.waist.xRot = 0.12F + breath * 0.05F;
        this.chest.xRot = 0.08F + breath * 0.04F;
        this.chest.y += breath * 0.7F;

        // Balanceo suave de la cabeza escuchando vibraciones
        this.head.zRot += 0.05F * Mth.sin(ageInTicks * 0.05F);
        this.head.xRot += 0.03F * breath;

        // Vibración acústica de las antenas del Warden (estrictamente xRot como en vanilla para simetría perfecta)
        float tendrilWave = Mth.cos(ageInTicks * 2.25F) * (float) Math.PI * 0.10F;
        this.leftTendril.xRot = tendrilWave;
        this.rightTendril.xRot = -tendrilWave;
        this.leftTendril.yRot = 0.0F;
        this.rightTendril.yRot = 0.0F;
        this.leftTendril.zRot = 0.0F;
        this.rightTendril.zRot = 0.0F;

        // Postura natural de reposo de los brazos colosales (con leve vaivén)
        this.rightArm.zRot = -0.10F - breath * 0.03F;
        this.leftArm.zRot = 0.10F + breath * 0.03F;
        this.rightArm.xRot = 0.10F + breathCos * 0.05F;
        this.leftArm.xRot = 0.10F + breathCos * 0.05F;

        // 3. Locomoción: Zancadas pesadas con balanceo y torsión del torso
        float walkSpeed = 0.50F;
        float walkDegree = 0.85F;
        float legSwing = Mth.cos(limbSwing * walkSpeed) * walkDegree * limbSwingAmount;

        this.rightLeg.xRot = legSwing;
        this.leftLeg.xRot = -legSwing;

        // Inercia pesada de los brazos demoledores al caminar
        this.rightArm.xRot += -legSwing * 0.90F;
        this.leftArm.xRot += legSwing * 0.90F;

        // Caída vertical y torsión en cada pisada pesada
        this.bone.y += Math.abs(Mth.sin(limbSwing * walkSpeed)) * 2.0F * limbSwingAmount;
        this.waist.zRot += Mth.cos(limbSwing * walkSpeed * 0.5F) * 0.05F * limbSwingAmount;
        this.chest.yRot += -Mth.sin(limbSwing * walkSpeed) * 0.07F * limbSwingAmount;

        // 4. Animación de Rugido e Intimidación
        if (entity.isRoaring()) {
            this.waist.xRot = -0.25F;
            this.chest.xRot = -0.25F;
            this.head.xRot = -0.55F;
            this.rightArm.xRot = -1.35F;
            this.leftArm.xRot = -1.35F;
            this.rightArm.zRot = -0.35F;
            this.leftArm.zRot = 0.35F;
            this.leftTendril.xRot = tendrilWave * 2.5F;
            this.rightTendril.xRot = -tendrilWave * 2.5F;
            this.leftTendril.zRot = 0.0F;
            this.rightTendril.zRot = 0.0F;
        }

        // 5. Animaciones de Ataque Mejoradas y Viscerales
        if (this.attackTime > 0.0F) {
            float progress = this.attackTime;
            boolean isGroundSlam = (entity.tickCount / 40) % 2 == 0;

            if (isGroundSlam) {
                // ATAQUE 1: Doble Aplastamiento Sísmico al Suelo (Ground Slam)
                if (progress < 0.40F) {
                    float prep = progress / 0.40F;
                    this.waist.xRot = -0.35F * prep;
                    this.chest.xRot = -0.35F * prep;
                    this.rightArm.xRot = -2.5F * prep;
                    this.leftArm.xRot = -2.5F * prep;
                    this.head.xRot = -0.35F * prep;
                    this.leftTendril.xRot = tendrilWave * 2.0F;
                    this.rightTendril.xRot = -tendrilWave * 2.0F;
                } else {
                    float strike = (progress - 0.40F) / 0.60F;
                    float slamRot = Mth.lerp(strike, -2.5F, 0.90F);
                    this.waist.xRot = Mth.lerp(strike, 0.45F, 0.12F);
                    this.chest.xRot = Mth.lerp(strike, 0.38F, 0.08F);
                    this.rightArm.xRot = slamRot;
                    this.leftArm.xRot = slamRot;
                    this.bone.y += 2.5F * (1.0F - strike);
                }
            } else {
                // ATAQUE 2: Gancho Demoledor Derecho con Torsión Total
                if (progress < 0.35F) {
                    float prep = progress / 0.35F;
                    this.chest.yRot = 0.55F * prep;
                    this.waist.yRot = 0.30F * prep;
                    this.rightArm.xRot = -2.1F * prep;
                    this.rightArm.yRot = -0.50F * prep;
                    this.leftArm.xRot = 0.50F * prep;
                    this.head.yRot += -0.20F * prep;
                } else {
                    float strike = (progress - 0.35F) / 0.65F;
                    this.chest.yRot = Mth.lerp(strike, 0.55F, -0.50F);
                    this.waist.yRot = Mth.lerp(strike, 0.30F, -0.30F);
                    this.waist.xRot = Mth.lerp(strike, 0.12F, 0.35F);
                    this.rightArm.xRot = Mth.lerp(strike, -2.1F, 1.0F);
                    this.rightArm.yRot = Mth.lerp(strike, -0.50F, 0.45F);
                    this.leftArm.xRot = Mth.lerp(strike, 0.50F, -0.70F);
                }
            }
        }

        // 6. Reacción Física al Daño (Flinch al ser golpeado)
        if (entity.hurtTime > 0) {
            float hurtFactor = (float) entity.hurtTime / 10.0F;
            this.waist.xRot -= 0.18F * hurtFactor;
            this.chest.xRot -= 0.12F * hurtFactor;
            this.head.xRot += 0.12F * hurtFactor;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
