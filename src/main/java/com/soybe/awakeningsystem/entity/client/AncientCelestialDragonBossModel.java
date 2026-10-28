package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity;
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
 * Modelo del Dragón Celestial Ancestral Titánico (Boss):
 * - Cadena colosal de 64 vértebras articuladas (+100 bloques de longitud en mundo).
 * - Proporciones robustas y cinemática serpentina continua sin deformaciones ni cortes.
 * - Textura blanca celestial (Bai Long): escamas de jade blanco perlado, astas de ciervo café noble y fuego divino.
 */
public class AncientCelestialDragonBossModel<T extends AncientCelestialDragonBossEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_celestial_dragon_boss"), "main");

    public static final int CHAIN_JOINTS_COUNT = 63;

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

    public AncientCelestialDragonBossModel(ModelPart root) {
        this.root = root;
        this.neck1 = root.getChild("neck1");
        this.head = this.neck1.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.whiskerLeft = this.head.getChild("whisker_left");
        this.whiskerLeftTip = this.whiskerLeft.getChild("tip");
        this.whiskerRight = this.head.getChild("whisker_right");
        this.whiskerRightTip = this.whiskerRight.getChild("tip");

        ModelPart current = this.neck1;
        this.chainJoints = new ModelPart[CHAIN_JOINTS_COUNT];

        current = current.getChild("neck2");
        this.chainJoints[0] = current;

        ModelPart bFront = null;
        ModelPart bBack = null;
        for (int b = 1; b <= 48; b++) {
            current = current.getChild("body" + b);
            this.chainJoints[b] = current;
            if (b == 4) bFront = current;
            if (b == 36) bBack = current;
        }

        for (int t = 1; t <= 13; t++) {
            current = current.getChild("tail" + t);
            this.chainJoints[48 + t] = current;
        }

        this.tailTuft = current.getChild("tail_tuft");
        this.chainJoints[62] = this.tailTuft;

        this.legFrontLeft = bFront != null ? bFront.getChild("leg_front_left") : this.root;
        this.legFrontRight = bFront != null ? bFront.getChild("leg_front_right") : this.root;
        this.legBackLeft = bBack != null ? bBack.getChild("leg_back_left") : this.root;
        this.legBackRight = bBack != null ? bBack.getChild("leg_back_right") : this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition neck1 = root.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(38, 25).addBox(-1.0F, -8.0F, -4.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 25).addBox(-3.0F, 3.8F, -4.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, -14.0F));

        PartDefinition head = neck1.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 0).addBox(-3.0F, -1.0F, -16.0F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-2.5F, -2.5F, -15.0F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-4.2F, -2.8F, -8.0F, 0.4F, 1.0F, 4.5F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(3.8F, -2.8F, -8.0F, 0.4F, 1.0F, 4.5F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -3.0F));

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

        PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(38, 25).addBox(-1.0F, -8.0F, -2.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 25).addBox(-3.0F, 3.8F, -2.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        PartDefinition prev = neck2;
        for (int b = 1; b <= 48; b++) {
            PartDefinition body = prev.addOrReplaceChild("body" + b, CubeListBuilder.create()
                    .texOffs(0, 25).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                    .texOffs(38, 25).addBox(-1.0F, -8.0F, -2.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                    .texOffs(64, 25).addBox(-3.0F, 3.8F, -2.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                    PartPose.offset(0.0F, 0.0F, 5.0F));

            if (b == 4) addLegs(body, "leg_front", 0.35F);
            if (b == 36) addLegs(body, "leg_back", 0.45F);

            prev = body;
        }

        PartDefinition tail1 = prev.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-3.5F, -3.5F, -2.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(34, 58).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(58, 58).addBox(-2.5F, 3.2F, -2.0F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-3.5F, -3.5F, -2.0F, 7.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(34, 58).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(58, 58).addBox(-2.5F, 3.2F, -2.0F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(30, 76).addBox(-0.5F, -5.0F, -2.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(50, 76).addBox(-2.0F, 2.8F, -2.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 76).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(30, 76).addBox(-0.5F, -5.0F, -2.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(50, 76).addBox(-2.0F, 2.8F, -2.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.5F));

        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(76, 76).addBox(-2.5F, -2.5F, -2.0F, 5.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(104, 76).addBox(-0.5F, -4.5F, -2.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.5F));

        PartDefinition tail6 = tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(76, 76).addBox(-2.5F, -2.5F, -2.0F, 5.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(104, 76).addBox(-0.5F, -4.5F, -2.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        PartDefinition tail7 = tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(0, 92).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        PartDefinition tail8 = tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(0, 92).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.5F));

        PartDefinition tail9 = tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.5F));

        PartDefinition tail10 = tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition tail11 = tail10.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition tail12 = tail11.addOrReplaceChild("tail12", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 2.5F));

        PartDefinition tail13 = tail12.addOrReplaceChild("tail13", CubeListBuilder.create()
                .texOffs(24, 92).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 2.5F));

        tail13.addOrReplaceChild("tail_tuft", CubeListBuilder.create()
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

        if (entity.isFallen()) {
            float breath = Mth.sin(ageInTicks * 0.08F) * 0.04F;
            this.neck1.zRot = 1.25F + breath;
            this.neck1.xRot = 0.15F;
            this.neck1.yRot = 0.30F;
            this.head.zRot = 0.20F;
            this.head.xRot = 0.10F;
            this.head.yRot = 0.20F;
            this.jaw.xRot = 0.32F + breath * 2.0F;

            this.whiskerLeft.yRot = 0.10F;
            this.whiskerLeft.zRot = 0.65F;
            this.whiskerRight.yRot = 0.10F;
            this.whiskerRight.zRot = -0.65F;

            for (int i = 0; i < this.chainJoints.length; i++) {
                ModelPart joint = this.chainJoints[i];
                if (joint == null) continue;
                float wave = Mth.sin(ageInTicks * 0.05F + i * 0.12F) * 0.025F;
                joint.yRot = Mth.sin(i * 0.22F) * 0.15F;
                joint.xRot = wave;
                joint.zRot = (i < 8) ? (1.25F / (i + 1.2F)) : 0.0F;
            }

            this.legFrontLeft.xRot = 0.10F;
            this.legFrontLeft.zRot = -0.85F;
            this.legFrontRight.xRot = 0.10F;
            this.legFrontRight.zRot = 0.85F;
            this.legBackLeft.xRot = 0.15F;
            this.legBackLeft.zRot = -0.85F;
            this.legBackRight.xRot = 0.15F;
            this.legBackRight.zRot = 0.85F;
            return;
        }

        double horizSpeedSq = entity.getDeltaMovement().horizontalDistanceSqr();
        float speed = (float) Math.sqrt(horizSpeedSq);
        float speedNorm = Mth.clamp(speed * 2.0F, 0.0F, 1.0F);

        // Constant angular frequency: strictly monotonic base time eliminates phase-shearing jitter
        float bossBaseTime = ageInTicks * 0.055F;
        float bossWaveK = 0.18F; // Spatial wavelength: ~35 vertebrae per cycle

        boolean isRoaringOrBreathing = entity.isRoaring() || entity.isBreathing();
        float bossYawAmp = (0.032F + speedNorm * 0.018F) * (isRoaringOrBreathing ? 1.3F : 1.0F);
        float bossPitchAmp = (0.012F + speedNorm * 0.008F) * (isRoaringOrBreathing ? 1.3F : 1.0F);

        // Neck1 is root of chain: keep yRot strictly 0.0F
        float basePitch = entity.getSegmentPitch(0, partialTick) * ((float) Math.PI / 180.0F);
        this.neck1.xRot = Mth.clamp(basePitch, -0.65F, 0.65F);
        this.neck1.yRot = 0.0F;
        this.neck1.zRot = 0.0F;

        // Head aims naturally without jerking
        float cleanHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -50.0F, 50.0F) * ((float) Math.PI / 180.0F);
        float cleanHeadPitch = Mth.clamp(headPitch, -35.0F, 35.0F) * ((float) Math.PI / 180.0F);
        this.head.yRot = cleanHeadYaw * 0.40F;
        this.head.xRot = cleanHeadPitch * 0.30F;

        if (isRoaringOrBreathing) {
            this.jaw.xRot = 0.65F;
        } else {
            this.jaw.xRot = 0.04F + Mth.sin(ageInTicks * 0.10F) * 0.03F;
        }

        float wFlutter = Mth.sin(ageInTicks * 0.10F) * (0.08F + speedNorm * 0.08F);
        this.whiskerLeft.yRot = -0.32F + wFlutter;
        this.whiskerLeftTip.yRot = 0.18F + Mth.sin(ageInTicks * 0.14F - 0.6F) * 0.14F;
        this.whiskerLeftTip.zRot = 0.12F + Mth.cos(ageInTicks * 0.11F - 0.6F) * 0.10F;

        this.whiskerRight.yRot = 0.32F - wFlutter;
        this.whiskerRightTip.yRot = -0.18F - Mth.sin(ageInTicks * 0.14F - 0.6F) * 0.14F;
        this.whiskerRightTip.zRot = -0.12F - Mth.cos(ageInTicks * 0.11F - 0.6F) * 0.10F;

        for (int i = 0; i < this.chainJoints.length; i++) {
            ModelPart joint = this.chainJoints[i];
            if (joint == null) continue;
            int segIndex = i + 1;

            float curYaw = entity.getSegmentYaw(segIndex, partialTick);
            float prevYaw = entity.getSegmentYaw(segIndex - 1, partialTick);
            float rawDiffYaw = Mth.wrapDegrees(curYaw - prevYaw);
            float bendYaw = Mth.clamp(rawDiffYaw, -12.0F, 12.0F) * ((float) Math.PI / 180.0F);

            float curPitch = entity.getSegmentPitch(segIndex, partialTick);
            float prevPitch = entity.getSegmentPitch(segIndex - 1, partialTick);
            float rawDiffPitch = curPitch - prevPitch;
            float bendPitch = Mth.clamp(rawDiffPitch, -8.0F, 8.0F) * ((float) Math.PI / 180.0F);

            float segOffset = (float) i * bossWaveK;
            float easeIn = Math.min(1.0F, (i + 1) * 0.08F);
            float waveYaw = Mth.sin(bossBaseTime - segOffset) * (bossYawAmp * easeIn);
            float wavePitch = Mth.cos(bossBaseTime - segOffset) * (bossPitchAmp * easeIn);

            joint.yRot = bendYaw + waveYaw;
            joint.xRot = bendPitch + wavePitch;
            joint.zRot = -bendYaw * 0.08F + waveYaw * 0.06F;
        }

        float legCycle = ageInTicks * 0.055F;
        float legSwing = Mth.cos(legCycle) * 0.12F * (0.35F + speedNorm * 0.65F);
        float legBase = 0.38F;
        this.legFrontLeft.xRot = legBase + legSwing;
        this.legFrontRight.xRot = legBase - legSwing;
        this.legBackLeft.xRot = (legBase + 0.10F) - legSwing;
        this.legBackRight.xRot = (legBase + 0.10F) + legSwing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
