package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CloudCraneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D para la Grulla Celestial de las Nubes (Cloud Crane).
 * Basado fielmente en la ilustracion de referencia de la grulla en vuelo con envergadura amplia:
 * - Alas de gran envergadura articuladas en 3 secciones con plumas primarias digitadas en las puntas (fingers).
 * - Borde de ataque blanco y remeras secundarias y primarias negro azabache/carbon.
 * - Cuello que se estira horizontalmente en vuelo como una flecha aerodinamica.
 * - Patas zancudas que se estiran horizontalmente hacia atras sobrepasando la cola en vuelo.
 * - Postura de reposo/acostarse como ave real (gallina/pato): vientre apoyado en el suelo, patas 100% ocultas bajo el plumaje, alas plegadas a los costados.
 */
public class CloudCraneModel extends HierarchicalModel<CloudCraneEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "cloud_crane"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart neckLower;
    private final ModelPart neckUpper;
    private final ModelPart head;
    private final ModelPart beakBase;
    private final ModelPart beakTip;
    private final ModelPart crown;

    private final ModelPart wingLeftInner;
    private final ModelPart wingLeftMid;
    private final ModelPart wingLeftOuter;
    private final ModelPart wingLeftTips;

    private final ModelPart wingRightInner;
    private final ModelPart wingRightMid;
    private final ModelPart wingRightOuter;
    private final ModelPart wingRightTips;

    private final ModelPart legLeft;
    private final ModelPart legRight;

    public CloudCraneModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
        this.neckLower = this.body.getChild("neck_lower");
        this.neckUpper = this.neckLower.getChild("neck_upper");
        this.head = this.neckUpper.getChild("head");
        this.beakBase = this.head.getChild("beak_base");
        this.beakTip = this.beakBase.getChild("beak_tip");
        this.crown = this.head.getChild("crown");

        this.wingLeftInner = this.body.getChild("wing_left_inner");
        this.wingLeftMid = this.wingLeftInner.getChild("wing_left_mid");
        this.wingLeftOuter = this.wingLeftMid.getChild("wing_left_outer");
        this.wingLeftTips = this.wingLeftOuter.getChild("wing_left_tips");

        this.wingRightInner = this.body.getChild("wing_right_inner");
        this.wingRightMid = this.wingRightInner.getChild("wing_right_mid");
        this.wingRightOuter = this.wingRightMid.getChild("wing_right_outer");
        this.wingRightTips = this.wingRightOuter.getChild("wing_right_tips");

        this.legLeft = this.body.getChild("leg_left");
        this.legRight = this.body.getChild("leg_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-3.0F, -3.5F, -6.0F, 6.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(36, 0)
                .addBox(-2.5F, -2.0F, -2.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, -6.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(54, 0)
                .addBox(-2.0F, -1.0F, 0.0F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.5F, 5.0F, -0.25F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail_plumes", CubeListBuilder.create()
                .texOffs(78, 0)
                .addBox(-2.5F, -0.5F, 0.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.0F, 4.0F, 0.15F, 0.0F, 0.0F));

        PartDefinition neckLower = body.addOrReplaceChild("neck_lower", CubeListBuilder.create()
                .texOffs(0, 20)
                .addBox(-1.5F, -7.0F, -1.5F, 3.0F, 8.0F, 3.5F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.5F, -6.5F, 0.35F, 0.0F, 0.0F));

        PartDefinition neckUpper = neckLower.addOrReplaceChild("neck_upper", CubeListBuilder.create()
                .texOffs(16, 20)
                .addBox(-1.0F, -7.0F, -1.5F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -6.5F, 0.0F, -0.45F, 0.0F, 0.0F));

        PartDefinition head = neckUpper.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(28, 20)
                .addBox(-1.5F, -2.0F, -2.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.15F, 0.0F, 0.0F));

        PartDefinition beakBase = head.addOrReplaceChild("beak_base", CubeListBuilder.create()
                .texOffs(46, 20)
                .addBox(-0.75F, -0.8F, -4.0F, 1.5F, 1.6F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -2.5F));

        beakBase.addOrReplaceChild("beak_tip", CubeListBuilder.create()
                .texOffs(60, 20)
                .addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.15F, -4.0F));

        head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(72, 20)
                .addBox(-1.0F, -0.8F, 0.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.3F, -1.5F));

        PartDefinition wingLeftInner = body.addOrReplaceChild("wing_left_inner", CubeListBuilder.create()
                .texOffs(0, 36)
                .addBox(-7.0F, -0.6F, -3.0F, 7.0F, 1.2F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(26, 36)
                .addBox(-6.5F, -0.4F, 2.0F, 6.5F, 0.8F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, -1.5F, -2.0F));

        PartDefinition wingLeftMid = wingLeftInner.addOrReplaceChild("wing_left_mid", CubeListBuilder.create()
                .texOffs(50, 36)
                .addBox(-7.0F, -0.5F, -2.5F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(76, 36)
                .addBox(-7.0F, -0.3F, 2.5F, 7.0F, 0.7F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.0F, 0.0F, 0.0F));

        PartDefinition wingLeftOuter = wingLeftMid.addOrReplaceChild("wing_left_outer", CubeListBuilder.create()
                .texOffs(0, 48)
                .addBox(-6.0F, -0.4F, -2.0F, 6.0F, 0.8F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(22, 48)
                .addBox(-6.5F, -0.3F, 2.0F, 6.5F, 0.7F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.0F, 0.0F, 0.0F));

        wingLeftOuter.addOrReplaceChild("wing_left_tips", CubeListBuilder.create()
                .texOffs(48, 48)
                .addBox(-4.0F, -0.2F, -1.5F, 4.0F, 0.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 48)
                .addBox(-5.5F, -0.2F, 0.5F, 5.5F, 0.5F, 2.2F, new CubeDeformation(0.0F))
                .texOffs(80, 48)
                .addBox(-5.0F, -0.2F, 2.7F, 5.0F, 0.5F, 2.2F, new CubeDeformation(0.0F))
                .texOffs(96, 48)
                .addBox(-4.0F, -0.2F, 4.9F, 4.0F, 0.5F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.0F, 0.0F, 0.0F));

        PartDefinition wingRightInner = body.addOrReplaceChild("wing_right_inner", CubeListBuilder.create()
                .texOffs(0, 58)
                .addBox(0.0F, -0.6F, -3.0F, 7.0F, 1.2F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(26, 58)
                .addBox(0.0F, -0.4F, 2.0F, 6.5F, 0.8F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, -1.5F, -2.0F));

        PartDefinition wingRightMid = wingRightInner.addOrReplaceChild("wing_right_mid", CubeListBuilder.create()
                .texOffs(50, 58)
                .addBox(0.0F, -0.5F, -2.5F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(76, 58)
                .addBox(0.0F, -0.3F, 2.5F, 7.0F, 0.7F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.0F, 0.0F, 0.0F));

        PartDefinition wingRightOuter = wingRightMid.addOrReplaceChild("wing_right_outer", CubeListBuilder.create()
                .texOffs(0, 70)
                .addBox(0.0F, -0.4F, -2.0F, 6.0F, 0.8F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(22, 70)
                .addBox(0.0F, -0.3F, 2.0F, 6.5F, 0.7F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.0F, 0.0F, 0.0F));

        wingRightOuter.addOrReplaceChild("wing_right_tips", CubeListBuilder.create()
                .texOffs(48, 70)
                .addBox(0.0F, -0.2F, -1.5F, 4.0F, 0.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 70)
                .addBox(0.0F, -0.2F, 0.5F, 5.5F, 0.5F, 2.2F, new CubeDeformation(0.0F))
                .texOffs(80, 70)
                .addBox(0.0F, -0.2F, 2.7F, 5.0F, 0.5F, 2.2F, new CubeDeformation(0.0F))
                .texOffs(96, 70)
                .addBox(0.0F, -0.2F, 4.9F, 4.0F, 0.5F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.0F, 0.0F, 0.0F));

        PartDefinition legLeft = body.addOrReplaceChild("leg_left", CubeListBuilder.create()
                .texOffs(0, 85)
                .addBox(-0.6F, 0.0F, -0.6F, 1.2F, 11.0F, 1.2F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.8F, 3.5F, 1.0F));
        legLeft.addOrReplaceChild("foot_left", CubeListBuilder.create()
                .texOffs(10, 85)
                .addBox(-1.5F, 10.5F, -3.5F, 3.0F, 0.5F, 4.5F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition legRight = body.addOrReplaceChild("leg_right", CubeListBuilder.create()
                .texOffs(30, 85)
                .addBox(-0.6F, 0.0F, -0.6F, 1.2F, 11.0F, 1.2F, new CubeDeformation(0.0F)),
                PartPose.offset(1.8F, 3.5F, 1.0F));
        legRight.addOrReplaceChild("foot_right", CubeListBuilder.create()
                .texOffs(40, 85)
                .addBox(-1.5F, 10.5F, -3.5F, 3.0F, 0.5F, 4.5F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(CloudCraneEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean isSitting = entity.isInSittingPose();
        boolean isFlying = (!entity.onGround() && !isSitting) || entity.isCraneFlying() || (entity.isVehicle() && !entity.onGround());

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.65F;
        this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.60F;

        if (isFlying) {
            this.legLeft.visible = true;
            this.legRight.visible = true;

            this.body.y = 10.0F;
            this.body.xRot = 0.05F;

            this.neckLower.xRot = 1.45F;
            this.neckUpper.xRot = -0.30F;
            this.head.xRot += -1.15F;

            float flap = Mth.cos(ageInTicks * 0.32F);
            float flapMid = Mth.cos(ageInTicks * 0.32F - 0.35F);
            float flapOuter = Mth.cos(ageInTicks * 0.32F - 0.70F);
            float flapTips = Mth.cos(ageInTicks * 0.32F - 1.05F);

            this.wingLeftInner.xRot = 0.04F;
            this.wingLeftInner.yRot = 0.06F;
            this.wingLeftInner.zRot = flap * 0.38F;

            this.wingLeftMid.xRot = 0.0F;
            this.wingLeftMid.yRot = 0.08F;
            this.wingLeftMid.zRot = flapMid * 0.30F;

            this.wingLeftOuter.xRot = 0.0F;
            this.wingLeftOuter.yRot = 0.12F;
            this.wingLeftOuter.zRot = flapOuter * 0.22F;

            this.wingLeftTips.xRot = 0.0F;
            this.wingLeftTips.yRot = 0.15F;
            this.wingLeftTips.zRot = flapTips * 0.18F;

            this.wingRightInner.xRot = 0.04F;
            this.wingRightInner.yRot = -0.06F;
            this.wingRightInner.zRot = -flap * 0.38F;

            this.wingRightMid.xRot = 0.0F;
            this.wingRightMid.yRot = -0.08F;
            this.wingRightMid.zRot = -flapMid * 0.30F;

            this.wingRightOuter.xRot = 0.0F;
            this.wingRightOuter.yRot = -0.12F;
            this.wingRightOuter.zRot = -flapOuter * 0.22F;

            this.wingRightTips.xRot = 0.0F;
            this.wingRightTips.yRot = -0.15F;
            this.wingRightTips.zRot = -flapTips * 0.18F;

            this.legLeft.xRot = 1.58F;
            this.legRight.xRot = 1.58F;
            this.legLeft.y = 2.0F;
            this.legRight.y = 2.0F;
            this.legLeft.z = 4.0F;
            this.legRight.z = 4.0F;

            this.tail.xRot = -0.05F + Mth.sin(ageInTicks * 0.2F) * 0.03F;

        } else if (isSitting) {
            this.body.y = 20.5F;
            this.body.xRot = 0.0F;

            this.legLeft.visible = false;
            this.legRight.visible = false;

            this.wingLeftInner.xRot = -0.05F;
            this.wingLeftInner.yRot = 1.46F;
            this.wingLeftInner.zRot = -0.18F;

            this.wingLeftMid.xRot = 0.02F;
            this.wingLeftMid.yRot = -0.10F;
            this.wingLeftMid.zRot = 0.05F;

            this.wingLeftOuter.xRot = 0.02F;
            this.wingLeftOuter.yRot = -0.12F;
            this.wingLeftOuter.zRot = 0.05F;

            this.wingLeftTips.xRot = 0.0F;
            this.wingLeftTips.yRot = -0.08F;
            this.wingLeftTips.zRot = 0.0F;

            this.wingRightInner.xRot = -0.05F;
            this.wingRightInner.yRot = -1.46F;
            this.wingRightInner.zRot = 0.18F;

            this.wingRightMid.xRot = 0.02F;
            this.wingRightMid.yRot = 0.10F;
            this.wingRightMid.zRot = -0.05F;

            this.wingRightOuter.xRot = 0.02F;
            this.wingRightOuter.yRot = 0.12F;
            this.wingRightOuter.zRot = -0.05F;

            this.wingRightTips.xRot = 0.0F;
            this.wingRightTips.yRot = 0.08F;
            this.wingRightTips.zRot = 0.0F;

            this.neckLower.xRot = 0.15F;
            this.neckUpper.xRot = -0.30F;

        } else {
            this.body.y = 11.0F;
            this.legLeft.visible = true;
            this.legRight.visible = true;

            this.wingLeftInner.xRot = -0.05F;
            this.wingLeftInner.yRot = 1.46F;
            this.wingLeftInner.zRot = -0.18F;

            this.wingLeftMid.xRot = 0.02F;
            this.wingLeftMid.yRot = -0.10F;
            this.wingLeftMid.zRot = 0.05F;

            this.wingLeftOuter.xRot = 0.02F;
            this.wingLeftOuter.yRot = -0.12F;
            this.wingLeftOuter.zRot = 0.05F;

            this.wingLeftTips.xRot = 0.0F;
            this.wingLeftTips.yRot = -0.08F;
            this.wingLeftTips.zRot = 0.0F;

            this.wingRightInner.xRot = -0.05F;
            this.wingRightInner.yRot = -1.46F;
            this.wingRightInner.zRot = 0.18F;

            this.wingRightMid.xRot = 0.02F;
            this.wingRightMid.yRot = 0.10F;
            this.wingRightMid.zRot = -0.05F;

            this.wingRightOuter.xRot = 0.02F;
            this.wingRightOuter.yRot = 0.12F;
            this.wingRightOuter.zRot = -0.05F;

            this.wingRightTips.xRot = 0.0F;
            this.wingRightTips.yRot = 0.08F;
            this.wingRightTips.zRot = 0.0F;

            this.legLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 0.75F * limbSwingAmount;
            this.legRight.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.75F * limbSwingAmount;

            this.neckLower.xRot = 0.35F + Mth.cos(limbSwing * 0.6662F) * 0.08F * limbSwingAmount;
            this.neckUpper.xRot = -0.45F;
        }
    }
}
