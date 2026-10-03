package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CelestialDragonEntity;
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
 * Modelo 3D del Dragón Chino Celestial Ancestral de Murim (Shenlong / Qinglong).
 * - Arquitectura 100% jerárquica de 24 vértebras sin partes invisibles ni hendiduras.
 * - Dimensiones UV calibradas exactamente al generador de texturas de escamas orgánicas.
 * - Flexión serpentina fluida en círculos cerrada (Kirin Fizel 5.1).
 */
public class CelestialDragonModel<T extends CelestialDragonEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "celestial_dragon"), "main");

    private final ModelPart root;
    private final ModelPart neck1;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart whiskerLeft;
    private final ModelPart whiskerLeftTip;
    private final ModelPart whiskerRight;
    private final ModelPart whiskerRightTip;

    private final ModelPart[] chainJoints;

    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    private final ModelPart tailTuft;

    public CelestialDragonModel(ModelPart root) {
        this.root = root;
        this.neck1 = root.getChild("neck1");
        this.head = this.neck1.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.whiskerLeft = this.head.getChild("whisker_left");
        this.whiskerLeftTip = this.whiskerLeft.getChild("tip");
        this.whiskerRight = this.head.getChild("whisker_right");
        this.whiskerRightTip = this.whiskerRight.getChild("tip");

        ModelPart current = this.neck1;
        this.chainJoints = new ModelPart[31];

        // 1. neck2
        current = current.getChild("neck2");
        this.chainJoints[0] = current;

        // 2..23: body1 a body22 (colosal ~50 bloques de longitud)
        ModelPart bFront = null;
        ModelPart bBack = null;
        for (int b = 1; b <= 22; b++) {
            current = current.getChild("body" + b);
            this.chainJoints[b] = current;
            if (b == 2) bFront = current;
            if (b == 16) bBack = current;
        }

        // 24..30: tail1 a tail7
        for (int t = 1; t <= 7; t++) {
            current = current.getChild("tail" + t);
            this.chainJoints[22 + t] = current;
        }

        // 31: tailTuft
        this.tailTuft = current.getChild("tail_tuft");
        this.chainJoints[30] = this.tailTuft;

        this.legFrontLeft = bFront != null ? bFront.getChild("leg_front_left") : this.root;
        this.legFrontRight = bFront != null ? bFront.getChild("leg_front_right") : this.root;
        this.legBackLeft = bBack != null ? bBack.getChild("leg_back_left") : this.root;
        this.legBackRight = bBack != null ? bBack.getChild("leg_back_right") : this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. neck1 (profundidad 10 calibrada exactamente a la textura: z = -4 a +6)
        PartDefinition neck1 = root.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(38, 25).addBox(-1.0F, -8.0F, -4.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 25).addBox(-3.0F, 3.8F, -4.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, -14.0F));

        // Head con cejas 3D prominentes
        PartDefinition head = neck1.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 0).addBox(-3.0F, -1.0F, -16.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-2.5F, -2.5F, -15.0F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                // Cejas 3D arqueadas sobre los ojos ancestrales
                .texOffs(64, 0).addBox(-4.2F, -2.8F, -8.0F, 0.4F, 1.0F, 4.5F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(3.8F, -2.8F, -8.0F, 0.4F, 1.0F, 4.5F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -3.0F));

        // Cuernos ramificados de ciervo imperial en café noble (Stag Antlers hacia atrás y arriba)
        head.addOrReplaceChild("horn_left", CubeListBuilder.create()
                .texOffs(64, 10).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(73, 10).addBox(-0.5F, -15.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(78, 10).addBox(-0.5F, -8.0F, -4.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(73, 17).addBox(-0.5F, -12.0F, 1.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -3.5F, -2.0F, -0.65F, -0.22F, -0.15F));

        head.addOrReplaceChild("horn_right", CubeListBuilder.create()
                .texOffs(64, 10).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(73, 10).addBox(-0.5F, -15.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(78, 10).addBox(-0.5F, -8.0F, -4.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(73, 17).addBox(-0.5F, -12.0F, 1.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -3.5F, -2.0F, -0.65F, 0.22F, 0.15F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(35, 13).addBox(-2.5F, 0.0F, -8.0F, 5.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 3.0F, -8.0F));

        PartDefinition wLeft = head.addOrReplaceChild("whisker_left", CubeListBuilder.create()
                .texOffs(89, 0).addBox(-2.0F, -0.5F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 0.5F, -14.0F));
        wLeft.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(90, 5).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, 0.0F, 1.0F));

        PartDefinition wRight = head.addOrReplaceChild("whisker_right", CubeListBuilder.create()
                .texOffs(89, 0).addBox(0.0F, -0.5F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 0.5F, -14.0F));
        wRight.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(90, 5).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 0.0F, 1.0F));

        // 2. neck2 (offset 5.0F, caja z = -2 a +8 -> solapamiento permanente de 3 unidades con neck1)
        PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(38, 25).addBox(-1.0F, -8.0F, -2.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 25).addBox(-3.0F, 3.8F, -2.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        // 22 Segmentos de cuerpo (~50 bloques colosales)
        PartDefinition prev = neck2;
        for (int b = 1; b <= 22; b++) {
            PartDefinition body = prev.addOrReplaceChild("body" + b, CubeListBuilder.create()
                    .texOffs(0, 25).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                    .texOffs(38, 25).addBox(-1.0F, -8.0F, -2.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                    .texOffs(64, 25).addBox(-3.0F, 3.8F, -2.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                    PartPose.offset(0.0F, 0.0F, 5.0F));

            if (b == 2) addLegs(body, "leg_front", 0.35F);
            if (b == 16) addLegs(body, "leg_back", 0.45F);

            prev = body;
        }

        // 7 Segmentos de Cola cónica
        PartDefinition tail1 = prev.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-3.5F, -3.5F, -2.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(34, 58).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(58, 58).addBox(-2.5F, 3.2F, -2.0F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(30, 76).addBox(-0.5F, -5.0F, -2.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(50, 76).addBox(-2.0F, 2.8F, -2.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(76, 76).addBox(-2.5F, -2.5F, -2.0F, 5.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(104, 76).addBox(-0.5F, -4.5F, -2.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.5F));

        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 92).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.5F));

        PartDefinition tail6 = tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition tail7 = tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 2.5F));

        tail7.addOrReplaceChild("tail_tuft", CubeListBuilder.create()
                .texOffs(44, 92).addBox(-0.5F, -4.0F, 0.0F, 1.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(72, 92).addBox(-3.0F, -0.5F, 2.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(102, 92).addBox(-0.5F, -2.0F, 12.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 2.5F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addLegs(PartDefinition parent, String prefix, float pitchBase) {
        parent.addOrReplaceChild(prefix + "_left", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 45).addBox(-2.0F, 4.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 45).addBox(-1.0F, 6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 45).addBox(-2.0F, 11.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(-2.0F, 11.5F, -4.5F, 1.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(-0.5F, 11.5F, -5.0F, 1.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(1.0F, 11.5F, -4.5F, 1.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(-0.5F, 11.5F, 1.5F, 1.0F, 1.5F, 2.5F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 2.0F, 3.0F, pitchBase, 0.2F, 0.15F));

        parent.addOrReplaceChild(prefix + "_right", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 45).addBox(-2.0F, 4.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 45).addBox(-1.0F, 6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 45).addBox(-2.0F, 11.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(-2.0F, 11.5F, -4.5F, 1.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(-0.5F, 11.5F, -5.0F, 1.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(1.0F, 11.5F, -4.5F, 1.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 45).addBox(-0.5F, 11.5F, 1.5F, 1.0F, 1.5F, 2.5F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, 2.0F, 3.0F, pitchBase, -0.2F, -0.15F));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        float partialTick = ageInTicks - (float) entity.tickCount;
        if (partialTick < 0.0F) partialTick += 1.0F;

        // Si el dragón está en tierra, quieto y sin jinete (NUNCA en vuelo ni cuando está montado)
        boolean isIdleResting = !entity.isVehicle() && entity.onGround() && (entity.isOrderedToSit() || entity.isInSittingPose() || entity.getDeltaMovement().horizontalDistanceSqr() < 0.01D);
        float slowBreathing = Mth.sin(ageInTicks * 0.05F) * 0.02F;

        // 1. Orientación de la Cabeza y Cuello Raíz (neck1)
        if (isIdleResting) {
            this.neck1.xRot = slowBreathing * 0.4F;
            this.neck1.yRot = 0.0F;
            this.head.xRot = slowBreathing * 0.4F;
            this.head.yRot = 0.0F;
        } else {
            // Rotación del cabeceo cinemático en neck1:
            // Al presionar Espacio (subida) -> neck1 se inclina vertical hacia arriba (-65°)
            // Al presionar Shift (picada) -> neck1 se inclina hacia abajo (+65°)
            float basePitch = entity.getSegmentPitch(0, partialTick) * ((float) Math.PI / 180.0F);
            this.neck1.xRot = basePitch;
            this.neck1.yRot = 0.0F;

            if (entity.isVehicle()) {
                float cleanHeadYaw = Mth.wrapDegrees(netHeadYaw);
                this.head.xRot = Mth.clamp((headPitch * ((float)Math.PI / 180F) - basePitch) * 0.20F, -0.35F, 0.35F);
                this.head.yRot = cleanHeadYaw * ((float)Math.PI / 180F) * 0.15F;
            } else {
                this.head.xRot = 0.0F;
                this.head.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.35F;
            }
        }

        if (entity.isRoaring() || entity.isBreathing()) {
            this.jaw.xRot = 0.65F;
        } else {
            this.jaw.xRot = 0.04F + Mth.sin(ageInTicks * 0.1F) * 0.03F;
        }

        // Bigotes
        float wFlutter = Mth.sin(ageInTicks * 0.22F) * 0.14F;
        this.whiskerLeft.yRot = -0.32F + wFlutter;
        this.whiskerLeftTip.yRot = 0.18F + Mth.sin(ageInTicks * 0.22F - 0.6F) * 0.20F;
        this.whiskerLeftTip.zRot = 0.12F + Mth.cos(ageInTicks * 0.18F - 0.6F) * 0.14F;

        this.whiskerRight.yRot = 0.32F - wFlutter;
        this.whiskerRightTip.yRot = -0.18F - Mth.sin(ageInTicks * 0.22F - 0.6F) * 0.20F;
        this.whiskerRightTip.zRot = -0.12F - Mth.cos(ageInTicks * 0.18F - 0.6F) * 0.14F;

        // 2. CINEMÁTICA SERPENTINA DE ESTELA Y REPOSO EN TIERRA
        for (int i = 0; i < this.chainJoints.length; i++) {
            ModelPart joint = this.chainJoints[i];
            int segIndex = i + 1;

            if (isIdleResting) {
                // Reposo noble ancestral en tierra: cuerpo sereno sobre el suelo con respiración sutil de Qi
                float breathVertical = Mth.sin(ageInTicks * 0.05F - (float) i * 0.08F) * 0.012F;
                float groundSettle = (i > 15) ? 0.015F : 0.0F;
                float lazyTail = (i >= 25) ? Mth.sin(ageInTicks * 0.03F + (float) (i - 25) * 0.20F) * 0.03F : 0.0F;

                joint.yRot = lazyTail;
                joint.xRot = breathVertical + groundSettle;
                joint.zRot = 0.0F;
            } else {
                float curYaw = entity.getSegmentYaw(segIndex, partialTick);
                float prevYaw = entity.getSegmentYaw(segIndex - 1, partialTick);
                float bendYaw = Mth.wrapDegrees(curYaw - prevYaw) * ((float) Math.PI / 180.0F);

                float curPitch = entity.getSegmentPitch(segIndex, partialTick);
                float prevPitch = entity.getSegmentPitch(segIndex - 1, partialTick);
                float bendPitch = (curPitch - prevPitch) * ((float) Math.PI / 180.0F);

                // Cinemática pura sin restricciones artificiales para permitir enroscar el dragón libremente
                joint.yRot = bendYaw;
                joint.xRot = bendPitch;
                joint.zRot = -bendYaw * 0.06F;
            }
        }

        if (isIdleResting) {
            this.tailTuft.yRot = Mth.sin(ageInTicks * 0.03F) * 0.05F;
            this.tailTuft.zRot = 0.0F;
        } else {
            this.tailTuft.yRot = 0.0F;
            this.tailTuft.zRot = 0.0F;
        }

        // 3. Patas
        if (isIdleResting) {
            this.legFrontLeft.xRot = 0.25F;
            this.legFrontRight.xRot = 0.25F;
            this.legBackLeft.xRot = 0.35F;
            this.legBackRight.xRot = 0.35F;
        } else {
            float legCycle = ageInTicks * 0.14F;
            this.legFrontLeft.xRot = 0.35F + Mth.cos(legCycle) * 0.18F;
            this.legFrontRight.xRot = 0.35F - Mth.cos(legCycle) * 0.18F;
            this.legBackLeft.xRot = 0.45F - Mth.cos(legCycle) * 0.18F;
            this.legBackRight.xRot = 0.45F + Mth.cos(legCycle) * 0.18F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
