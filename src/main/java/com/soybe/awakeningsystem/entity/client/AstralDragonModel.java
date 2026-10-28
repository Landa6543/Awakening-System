package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AstralDragonEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo geométrico para el Dragón Bebé de Cristal Astral.
 * Basado fielmente en la anatomía y alas de la Dragona del Fin (Ender Dragon):
 * - Hueso superior y antebrazo articulados.
 * - 3 varillas óseas radiantes desde el codo y espolón/garra carpiana.
 * - Membrana con recortes festoneados parabólicos profundos y puntas afiladas.
 * - Cuello arqueado, hocico reptiliano alargado, cuernos curvados, lomo espinado y cola de diamante.
 */
public class AstralDragonModel extends HierarchicalModel<AstralDragonEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "astral_dragon"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftHorn;
    private final ModelPart rightHorn;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;

    public AstralDragonModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftHorn = this.head.getChild("left_horn");
        this.rightHorn = this.head.getChild("right_horn");

        this.leftWing = this.body.getChild("left_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");

        this.rightWing = this.body.getChild("right_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");

        this.legFrontLeft = this.body.getChild("leg_front_left");
        this.legFrontRight = this.body.getChild("leg_front_right");
        this.legBackLeft = this.body.getChild("leg_back_left");
        this.legBackRight = this.body.getChild("leg_back_right");

        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.5F, -2.5F, -3.5F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16)
                .addBox(-0.5F, -3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-1.5F, -3.5F, -2.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(30, 10)
                .addBox(-0.5F, -4.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.5F, -3.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(50, 0)
                .addBox(-2.5F, -2.5F, -3.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(75, 0)
                .addBox(-1.5F, -0.5F, -6.5F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(95, 10)
                .addBox(-3.5F, -1.8F, 0.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(105, 10)
                .addBox(2.5F, -1.8F, 0.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.5F, -2.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(75, 10)
                .addBox(-1.5F, 0.0F, -3.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.5F, -2.5F));

        head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(95, 0)
                .addBox(-0.5F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.3F, -2.4F, 0.8F, -0.55F, -0.12F, -0.25F));

        head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(105, 0)
                .addBox(-0.5F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.3F, -2.4F, 0.8F, -0.55F, 0.12F, 0.25F));

        PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 50)
                .addBox(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 58)
                .addBox(-10.0F, 0.0F, 1.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, -1.5F, -1.0F));

        leftWing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(25, 50)
                .addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 50)
                .addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 58)
                .addBox(-6.0F, 0.0F, 1.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-10.0F, 0.0F, 0.0F));

        PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(65, 50)
                .addBox(0.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(65, 58)
                .addBox(0.0F, 0.0F, 1.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, -1.5F, -1.0F));

        rightWing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(90, 50)
                .addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 50)
                .addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(95, 58)
                .addBox(0.0F, 0.0F, 1.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(10.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                .texOffs(0, 30)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30)
                .addBox(-1.0F, 4.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 2.0F, -2.0F));

        body.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                .texOffs(12, 30)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 30)
                .addBox(-1.0F, 4.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 2.0F, -2.0F));

        body.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                .texOffs(24, 30)
                .addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 30)
                .addBox(-1.0F, 4.0F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.2F, 2.0F, 2.5F));

        body.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                .texOffs(38, 30)
                .addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 30)
                .addBox(-1.0F, 4.0F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.2F, 2.0F, 2.5F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(55, 30)
                .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(55, 35)
                .addBox(-0.5F, -2.5F, 1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.5F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(75, 30)
                .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(75, 35)
                .addBox(-0.5F, -2.0F, 1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(95, 30)
                .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(110, 30)
                .addBox(-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AstralDragonEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean onGround = entity.onGround();
        boolean wantsToSit = entity.isInSittingPose() || entity.isOrderedToSit();
        boolean isSitting = wantsToSit && onGround;
        boolean isFlying = !onGround;

        this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.7F;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.7F;
        this.neck.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.3F;

        if (isSitting) {
            float breathe = Mth.sin(ageInTicks * 0.08F);
            float tailSway = Mth.sin(ageInTicks * 0.06F);

            this.body.y = 19.8F + breathe * 0.3F;
            this.neck.xRot = -0.12F + breathe * 0.04F;

            this.head.zRot = Mth.sin(ageInTicks * 0.04F) * 0.08F;
            this.jaw.xRot = Math.max(0.0F, Mth.sin(ageInTicks * 0.05F)) * 0.08F;

            this.legBackLeft.xRot = -1.15F;
            this.legBackLeft.yRot = 0.35F;
            this.legBackRight.xRot = -1.15F;
            this.legBackRight.yRot = -0.35F;

            this.legFrontLeft.xRot = -0.30F;
            this.legFrontRight.xRot = -0.30F;

            this.leftWing.xRot = 0.10F;
            this.leftWing.yRot = 0.85F;
            this.leftWing.zRot = 0.15F + breathe * 0.02F;
            this.leftWingTip.xRot = 0.0F;
            this.leftWingTip.yRot = -0.15F;
            this.leftWingTip.zRot = -0.05F;

            this.rightWing.xRot = 0.10F;
            this.rightWing.yRot = -0.85F;
            this.rightWing.zRot = -0.15F - breathe * 0.02F;
            this.rightWingTip.xRot = 0.0F;
            this.rightWingTip.yRot = 0.15F;
            this.rightWingTip.zRot = 0.05F;

            this.tail1.yRot = 0.70F + tailSway * 0.08F;
            this.tail2.yRot = 0.90F + tailSway * 0.15F;
            this.tail3.yRot = 0.80F + Mth.sin(ageInTicks * 0.10F) * 0.20F;
            this.tail3.zRot = Mth.cos(ageInTicks * 0.10F) * 0.10F;
        } else if (isFlying) {
            float flap = Mth.sin(ageInTicks * 0.45F);
            float tipFlap = Mth.sin(ageInTicks * 0.45F - 0.40F);

            this.leftWing.xRot = 0.0F;
            this.leftWing.yRot = 0.0F;
            this.leftWing.zRot = flap * 0.40F;

            this.rightWing.xRot = 0.0F;
            this.rightWing.yRot = 0.0F;
            this.rightWing.zRot = -flap * 0.40F;

            this.leftWingTip.xRot = 0.0F;
            this.leftWingTip.yRot = 0.0F;
            this.leftWingTip.zRot = tipFlap * 0.25F;

            this.rightWingTip.xRot = 0.0F;
            this.rightWingTip.yRot = 0.0F;
            this.rightWingTip.zRot = -tipFlap * 0.25F;

            this.neck.xRot = -0.35F + Mth.sin(ageInTicks * 0.12F) * 0.05F;

            this.legFrontLeft.xRot = 0.55F;
            this.legFrontRight.xRot = 0.55F;
            this.legBackLeft.xRot = 0.80F;
            this.legBackRight.xRot = 0.80F;

            this.tail1.yRot = Mth.cos(ageInTicks * 0.18F) * 0.20F;
            this.tail2.yRot = Mth.cos(ageInTicks * 0.18F - 0.6F) * 0.28F;
            this.tail3.yRot = Mth.cos(ageInTicks * 0.18F - 1.2F) * 0.35F;

            this.body.y = 17.0F + Mth.sin(ageInTicks * 0.15F) * 0.75F;
        } else {
            this.body.y = 17.0F;
            this.neck.xRot = -0.2F;

            this.legFrontRight.xRot = Mth.cos(limbSwing * 0.8F) * 0.75F * limbSwingAmount;
            this.legFrontLeft.xRot = Mth.cos(limbSwing * 0.8F + (float) Math.PI) * 0.75F * limbSwingAmount;
            this.legBackLeft.xRot = Mth.cos(limbSwing * 0.8F) * 0.75F * limbSwingAmount;
            this.legBackRight.xRot = Mth.cos(limbSwing * 0.8F + (float) Math.PI) * 0.75F * limbSwingAmount;

            this.leftWing.xRot = 0.0F;
            this.leftWing.yRot = 0.0F;
            this.leftWing.zRot = 0.08F + Mth.sin(ageInTicks * 0.08F) * 0.02F;
            this.leftWingTip.xRot = 0.0F;
            this.leftWingTip.yRot = 0.0F;
            this.leftWingTip.zRot = -0.06F;

            this.rightWing.xRot = 0.0F;
            this.rightWing.yRot = 0.0F;
            this.rightWing.zRot = -0.08F - Mth.sin(ageInTicks * 0.08F) * 0.02F;
            this.rightWingTip.xRot = 0.0F;
            this.rightWingTip.yRot = 0.0F;
            this.rightWingTip.zRot = 0.06F;

            this.tail1.yRot = Mth.sin(limbSwing * 0.5F) * 0.3F * limbSwingAmount;
            this.tail2.yRot = Mth.sin(limbSwing * 0.5F - 0.4F) * 0.3F * limbSwingAmount;
            this.tail3.yRot = Mth.sin(limbSwing * 0.5F - 0.8F) * 0.35F * limbSwingAmount;
        }
    }
}
