package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AncientDragonEntity;
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
import net.minecraft.world.phys.Vec3;

/**
 * Modelo del Dragón Ancestral Occidental Colosal.
 * - Cuerpo masivo y musculoso agrandado.
 * - Alas de murciélago orgánicas con doble cara texturizada al 100% (Cero huecos, cero distorsión).
 * - Hocico y fauces limpios sin colmillos deformes.
 * - Cola 3D robusta con pala romboidal maciza al final.
 */
public class AncientDragonModel extends HierarchicalModel<AncientDragonEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart saddle;
    private final ModelPart spinesMiddle;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftHorn;
    private final ModelPart leftHornTip;
    private final ModelPart rightHorn;
    private final ModelPart rightHornTip;
    private final ModelPart leftCheekFrill;
    private final ModelPart rightCheekFrill;

    private final ModelPart leftWing;
    private final ModelPart leftWingMid;
    private final ModelPart leftWingTip;
    private final ModelPart rightWing;
    private final ModelPart rightWingMid;
    private final ModelPart rightWingTip;

    private final ModelPart frontLeftLeg;
    private final ModelPart frontLeftShin;
    private final ModelPart frontLeftFoot;
    private final ModelPart frontRightLeg;
    private final ModelPart frontRightShin;
    private final ModelPart frontRightFoot;

    private final ModelPart backLeftLeg;
    private final ModelPart backLeftShin;
    private final ModelPart backLeftFoot;
    private final ModelPart backRightLeg;
    private final ModelPart backRightShin;
    private final ModelPart backRightFoot;

    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tailSpade;

    public AncientDragonModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.saddle = this.body.getChild("saddle");
        this.spinesMiddle = this.body.getChild("spines_middle");

        this.neck1 = this.body.getChild("neck1");
        this.neck2 = this.neck1.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.head = this.neck3.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftHorn = this.head.getChild("left_horn");
        this.leftHornTip = this.leftHorn.getChild("tip");
        this.rightHorn = this.head.getChild("right_horn");
        this.rightHornTip = this.rightHorn.getChild("tip");
        this.leftCheekFrill = this.head.getChild("left_cheek_frill");
        this.rightCheekFrill = this.head.getChild("right_cheek_frill");

        this.leftWing = this.body.getChild("left_wing");
        this.leftWingMid = this.leftWing.getChild("left_wing_mid");
        this.leftWingTip = this.leftWingMid.getChild("left_wing_tip");
        this.rightWing = this.body.getChild("right_wing");
        this.rightWingMid = this.rightWing.getChild("right_wing_mid");
        this.rightWingTip = this.rightWingMid.getChild("right_wing_tip");

        this.frontLeftLeg = this.body.getChild("front_left_leg");
        this.frontLeftShin = this.frontLeftLeg.getChild("shin");
        this.frontLeftFoot = this.frontLeftShin.getChild("foot");

        this.frontRightLeg = this.body.getChild("front_right_leg");
        this.frontRightShin = this.frontRightLeg.getChild("shin");
        this.frontRightFoot = this.frontRightShin.getChild("foot");

        this.backLeftLeg = this.body.getChild("back_left_leg");
        this.backLeftShin = this.backLeftLeg.getChild("shin");
        this.backLeftFoot = this.backLeftShin.getChild("foot");

        this.backRightLeg = this.body.getChild("back_right_leg");
        this.backRightShin = this.backRightLeg.getChild("shin");
        this.backRightFoot = this.backRightShin.getChild("foot");

        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.tail4 = this.tail3.getChild("tail4");
        this.tailSpade = this.tail4.getChild("spade");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10.0F, -9.0F, -18.0F, 20.0F, 18.0F, 36.0F, new CubeDeformation(0.0F))
                .texOffs(112, 0).addBox(-8.5F, 6.2F, -16.0F, 17.0F, 4.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(148, 187).addBox(-1.5F, -13.0F, -16.0F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(112, 160).addBox(-1.5F, -13.0F, 6.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        body.addOrReplaceChild("spines_middle", CubeListBuilder.create()
                .texOffs(204, 104).addBox(-1.5F, -13.0F, -8.0F, 3.0F, 4.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("saddle", CubeListBuilder.create()
                .texOffs(168, 122).addBox(-7.5F, -10.5F, -7.0F, 15.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(82, 54).addBox(-5.5F, -12.5F, -7.0F, 11.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(226, 154).addBox(-5.5F, -12.5F, 2.0F, 11.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(248, 48).addBox(-10.2F, -7.0F, -3.0F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(150, 60).addBox(9.2F, -7.0F, -3.0F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(232, 69).addBox(-13.0F, -7.0F, -4.0F, 3.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(232, 122).addBox(10.0F, -7.0F, -4.0F, 3.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition neck1 = body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(112, 36).addBox(-6.0F, -6.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(170, 187).addBox(-1.0F, -9.0F, -10.0F, 2.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(112, 187).addBox(-4.5F, 4.2F, -10.0F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, -17.0F, -0.35F, 0.0F, 0.0F));

        PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(206, 48).addBox(-5.0F, -5.0F, -11.0F, 10.0F, 10.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(222, 198).addBox(-1.0F, -8.0F, -9.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(192, 187).addBox(-4.0F, 3.5F, -9.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, -10.0F, -0.25F, 0.0F, 0.0F));

        PartDefinition neck3 = neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(82, 60).addBox(-4.5F, -4.5F, -10.0F, 9.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(178, 78).addBox(-1.0F, -7.0F, -8.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(150, 78).addBox(-3.5F, 3.0F, -8.0F, 7.0F, 2.5F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.5F, -9.0F, 0.20F, 0.0F, 0.0F));

        PartDefinition head = neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(160, 59).addBox(-5.5F, -5.0F, -10.0F, 11.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(112, 144).addBox(-4.0F, -2.0F, -20.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(78, 79).addBox(-4.5F, -6.5F, -8.0F, 9.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(238, 104).addBox(-1.0F, -8.5F, -7.0F, 2.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.40F, 0.0F, 0.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(42, 74).addBox(-4.0F, 0.0F, -10.0F, 8.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(110, 79).addBox(-2.0F, -0.8F, -7.0F, 4.0F, 1.5F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 2.0F, -10.0F, 0.15F, 0.0F, 0.0F));

        PartDefinition leftHorn = head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(170, 144).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -4.0F, -1.5F, 0.30F, -0.25F, -0.20F));
        leftHorn.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(112, 123).addBox(-1.0F, -1.5F, 0.0F, 2.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 10.5F, 0.25F, -0.15F, 0.0F));

        PartDefinition rightHorn = head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(198, 144).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, -4.0F, -1.5F, 0.30F, 0.25F, 0.20F));
        rightHorn.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(136, 123).addBox(-1.0F, -1.5F, 0.0F, 2.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 10.5F, 0.25F, 0.15F, 0.0F));

        head.addOrReplaceChild("left_cheek_frill", CubeListBuilder.create()
                .texOffs(132, 79).addBox(-5.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, 1.0F, -3.0F, 0.0F, -0.35F, -0.20F));

        head.addOrReplaceChild("right_cheek_frill", CubeListBuilder.create()
                .texOffs(112, 199).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, 1.0F, -3.0F, 0.0F, 0.35F, 0.20F));

        PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror()
                .texOffs(112, 88).addBox("bone", 0.0F, -2.5F, -2.5F, 24.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-30, 88).addBox("skin", 0.0F, 0.0F, 1.0F, 24.0F, 0.0F, 30.0F, new CubeDeformation(0.0F)),
                PartPose.offset(10.0F, -5.0F, -14.0F));

        PartDefinition leftWingMid = leftWing.addOrReplaceChild("left_wing_mid", CubeListBuilder.create().mirror()
                .texOffs(176, 88).addBox("bone", 0.0F, -2.0F, -2.0F, 22.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-24, 122).addBox("skin", 0.0F, 0.0F, 1.0F, 22.0F, 0.0F, 24.0F, new CubeDeformation(0.0F)),
                PartPose.offset(24.0F, 0.0F, 0.0F));

        leftWingMid.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().mirror()
                .texOffs(112, 136).addBox("bone", 0.0F, -1.5F, -1.5F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-16, 150).addBox("skin", 0.0F, 0.0F, 1.0F, 20.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(22.0F, 0.0F, 0.0F));

        PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(112, 88).addBox("bone", -24.0F, -2.5F, -2.5F, 24.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-30, 88).addBox("skin", -24.0F, 0.0F, 1.0F, 24.0F, 0.0F, 30.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-10.0F, -5.0F, -14.0F));

        PartDefinition rightWingMid = rightWing.addOrReplaceChild("right_wing_mid", CubeListBuilder.create()
                .texOffs(176, 88).addBox("bone", -22.0F, -2.0F, -2.0F, 22.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-24, 122).addBox("skin", -22.0F, 0.0F, 1.0F, 22.0F, 0.0F, 24.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-24.0F, 0.0F, 0.0F));

        rightWingMid.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(112, 136).addBox("bone", -20.0F, -1.5F, -1.5F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-16, 150).addBox("skin", -20.0F, 0.0F, 1.0F, 20.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-22.0F, 0.0F, 0.0F));

        PartDefinition frontLeftLeg = body.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(120, 60).addBox(-3.5F, 0.0F, -4.0F, 7.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, 5.0F, -10.0F));
        PartDefinition frontLeftShin = frontLeftLeg.addOrReplaceChild("shin", CubeListBuilder.create()
                .texOffs(232, 138).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        frontLeftShin.addOrReplaceChild("foot", CubeListBuilder.create()
                .texOffs(224, 187).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition frontRightLeg = body.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(202, 69).addBox(-3.5F, 0.0F, -4.0F, 7.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 5.0F, -10.0F));
        PartDefinition frontRightShin = frontRightLeg.addOrReplaceChild("shin", CubeListBuilder.create()
                .texOffs(148, 144).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        frontRightShin.addOrReplaceChild("foot", CubeListBuilder.create()
                .texOffs(192, 198).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition backLeftLeg = body.addOrReplaceChild("back_left_leg", CubeListBuilder.create()
                .texOffs(210, 0).addBox(-4.5F, 0.0F, -5.5F, 9.0F, 13.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.5F, 4.0F, 10.0F));
        PartDefinition backLeftShin = backLeftLeg.addOrReplaceChild("shin", CubeListBuilder.create()
                .texOffs(112, 104).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 2.5F));
        backLeftShin.addOrReplaceChild("foot", CubeListBuilder.create()
                .texOffs(170, 159).addBox(-4.0F, 0.0F, -7.5F, 8.0F, 3.5F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition backRightLeg = body.addOrReplaceChild("back_right_leg", CubeListBuilder.create()
                .texOffs(210, 24).addBox(-4.5F, 0.0F, -5.5F, 9.0F, 13.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.5F, 4.0F, 10.0F));
        PartDefinition backRightShin = backRightLeg.addOrReplaceChild("shin", CubeListBuilder.create()
                .texOffs(140, 104).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 2.5F));
        backRightShin.addOrReplaceChild("foot", CubeListBuilder.create()
                .texOffs(206, 159).addBox(-4.0F, 0.0F, -7.5F, 8.0F, 3.5F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(160, 36).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(138, 160).addBox(-1.0F, -8.0F, 1.0F, 2.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, 17.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 54).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(164, 173).addBox(-1.0F, -7.0F, 1.0F, 2.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 12.5F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(42, 54).addBox(-3.5F, -3.5F, 0.0F, 7.0F, 7.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(190, 173).addBox(-1.0F, -6.0F, 1.0F, 2.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 12.5F));

        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(168, 104).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(216, 173).addBox(-1.0F, -5.0F, 1.0F, 2.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 12.5F));

        tail4.addOrReplaceChild("spade", CubeListBuilder.create()
                .texOffs(132, 174).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 75).addBox(-4.0F, -1.5F, 2.0F, 8.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(112, 174).addBox(-1.0F, -3.5F, 1.0F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 12.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AncientDragonEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean saddled = entity.isSaddled();
        this.saddle.visible = saddled;
        this.spinesMiddle.visible = !saddled;

        float partialTick = ageInTicks - (float) entity.tickCount;
        if (partialTick < 0.0F) partialTick += 1.0F;
        float flyProgress = entity.getFlyProgress(partialTick);
        if (entity.isFlying() && !entity.onGround()) {
            flyProgress = Math.max(flyProgress, 0.35F);
        }
        float groundProgress = Math.max(0.0F, 1.0F - flyProgress * 1.6F);
        float walkAmt = (flyProgress > 0.15F) ? 0.0F : (Math.min(1.0F, limbSwingAmount * 1.4F) * groundProgress);

        float headYawClamped = Mth.clamp(netHeadYaw, -35.0F, 35.0F) * ((float) Math.PI / 180.0F);
        float headPitchClamped = Mth.clamp(headPitch, -25.0F, 25.0F) * ((float) Math.PI / 180.0F);

        this.neck1.yRot = headYawClamped * 0.20F;
        this.neck2.yRot = headYawClamped * 0.25F;
        this.neck3.yRot = headYawClamped * 0.30F;
        this.head.yRot = headYawClamped * 0.25F;

        this.neck1.xRot += headPitchClamped * 0.15F;
        this.neck2.xRot += headPitchClamped * 0.20F;
        this.neck3.xRot += headPitchClamped * 0.25F;
        this.head.xRot += headPitchClamped * 0.20F;

        float breathCycle = ageInTicks * 0.065F;
        float breathSine = Mth.sin(breathCycle);
        float breathCosine = Mth.cos(breathCycle);

        this.body.y += breathSine * 0.35F * groundProgress;
        this.body.xRot += breathSine * 0.025F;

        this.neck1.xRot -= breathSine * 0.030F;
        this.neck2.xRot += breathCosine * 0.025F;
        this.neck3.xRot -= breathSine * 0.020F;

        this.head.zRot += breathCosine * 0.015F;
        this.leftCheekFrill.yRot += breathSine * 0.05F;
        this.rightCheekFrill.yRot -= breathSine * 0.05F;
        this.leftHorn.xRot += breathSine * 0.02F;
        this.rightHorn.xRot += breathSine * 0.02F;

        if (entity.isRoaring()) {
            this.jaw.xRot = 0.55F + Mth.sin(ageInTicks * 0.45F) * 0.12F;
            this.head.xRot -= 0.15F;
            this.neck1.xRot -= 0.08F;
        } else {
            this.jaw.xRot = 0.05F;
        }

        float flightTailTime = ageInTicks * 0.065F;
        float idleTailTime = ageInTicks * 0.038F;

        float turnTrail = -headYawClamped * 0.25F;

        float airY1 = Mth.sin(flightTailTime) * 0.045F + turnTrail * 0.30F;
        float airY2 = Mth.sin(flightTailTime - 0.45F) * 0.060F + turnTrail * 0.50F;
        float airY3 = Mth.sin(flightTailTime - 0.90F) * 0.075F + turnTrail * 0.70F;
        float airY4 = Mth.sin(flightTailTime - 1.35F) * 0.085F + turnTrail * 0.90F;
        float airYSpade = Mth.sin(flightTailTime - 1.80F) * 0.095F + turnTrail * 0.40F;

        float vertWave = Mth.cos(flightTailTime * 0.5F);
        float airX1 = -0.02F + vertWave * 0.018F;
        float airX2 = 0.01F + Mth.cos(flightTailTime * 0.5F - 0.35F) * 0.022F;
        float airX3 = 0.02F + Mth.cos(flightTailTime * 0.5F - 0.70F) * 0.025F;
        float airX4 = 0.03F + Mth.cos(flightTailTime * 0.5F - 1.05F) * 0.028F;
        float airXSpade = 0.01F + Mth.cos(flightTailTime * 0.5F - 1.40F) * 0.030F;

        float idleY1 = Mth.sin(idleTailTime) * 0.030F;
        float idleY2 = Mth.sin(idleTailTime - 0.40F) * 0.040F;
        float idleY3 = Mth.sin(idleTailTime - 0.80F) * 0.050F;
        float idleY4 = Mth.sin(idleTailTime - 1.20F) * 0.060F;
        float idleYSpade = Mth.sin(idleTailTime - 1.60F) * 0.070F;

        float walkTailPhase = limbSwing * 0.55F;
        float walkSway1 = -Mth.sin(walkTailPhase) * 0.07F * walkAmt;
        float walkSway2 = -Mth.sin(walkTailPhase - 0.40F) * 0.09F * walkAmt;
        float walkSway3 = -Mth.sin(walkTailPhase - 0.80F) * 0.11F * walkAmt;
        float walkSway4 = -Mth.sin(walkTailPhase - 1.20F) * 0.13F * walkAmt;
        float walkSwaySpade = -Mth.sin(walkTailPhase - 1.60F) * 0.15F * walkAmt;

        float groundY1 = idleY1 + walkSway1;
        float groundY2 = idleY2 + walkSway2;
        float groundY3 = idleY3 + walkSway3;
        float groundY4 = idleY4 + walkSway4;
        float groundYSpade = idleYSpade + walkSwaySpade;

        float groundX1 = 0.06F;
        float groundX2 = 0.05F;
        float groundX3 = -0.02F;
        float groundX4 = -0.04F;
        float groundXSpade = 0.02F;

        this.tail1.yRot = Mth.lerp(flyProgress, groundY1, airY1);
        this.tail1.xRot = Mth.lerp(flyProgress, groundX1, airX1);

        this.tail2.yRot = Mth.lerp(flyProgress, groundY2, airY2);
        this.tail2.xRot = Mth.lerp(flyProgress, groundX2, airX2);

        this.tail3.yRot = Mth.lerp(flyProgress, groundY3, airY3);
        this.tail3.xRot = Mth.lerp(flyProgress, groundX3, airX3);

        this.tail4.yRot = Mth.lerp(flyProgress, groundY4, airY4);
        this.tail4.xRot = Mth.lerp(flyProgress, groundX4, airX4);

        this.tailSpade.yRot = Mth.lerp(flyProgress, groundYSpade, airYSpade);
        this.tailSpade.xRot = Mth.lerp(flyProgress, groundXSpade, airXSpade);
        this.tailSpade.zRot = 0.0F;

        float flapSpeed = 0.085F;
        float flapRad = ageInTicks * flapSpeed;
        float wingStroke = Mth.sin(flapRad);
        float wingSweep = Mth.cos(flapRad);

        Vec3 vel = entity.getDeltaMovement();
        boolean isGliding = Math.abs(vel.y) < 0.08D && vel.horizontalDistanceSqr() > 0.04D;
        float glideFactor = isGliding ? 0.35F : 1.0F;

        float airX = 0.12F + wingSweep * 0.08F * glideFactor;
        float airY = -0.10F + wingSweep * 0.05F * glideFactor;
        float airZ = (-wingStroke * 0.60F - 0.08F) * glideFactor + (1.0F - glideFactor) * -0.10F;
        float airMidZ = (-Mth.sin(flapRad - 0.35F) * 0.35F) * glideFactor + (1.0F - glideFactor) * -0.05F;
        float airTipZ = (-Mth.sin(flapRad - 0.70F) * 0.40F) * glideFactor + (1.0F - glideFactor) * -0.05F;

        float groundX = 0.15F;
        float groundY = -0.65F;
        float groundZ = 0.45F;
        float groundMidY = -0.15F;
        float groundMidZ = -0.25F;
        float groundTipX = 0.0F;
        float groundTipY = -0.10F;
        float groundTipZ = 0.15F;

        float groundWingBounce = breathSine * 0.02F;
        this.leftWing.xRot = Mth.lerp(flyProgress, groundX + groundWingBounce, airX);
        this.leftWing.yRot = Mth.lerp(flyProgress, groundY, airY);
        this.leftWing.zRot = Mth.lerp(flyProgress, groundZ, airZ);

        this.leftWingMid.xRot = 0.0F;
        this.leftWingMid.yRot = Mth.lerp(flyProgress, groundMidY, 0.0F);
        this.leftWingMid.zRot = Mth.lerp(flyProgress, groundMidZ, airMidZ);

        this.leftWingTip.xRot = Mth.lerp(flyProgress, groundTipX, 0.0F);
        this.leftWingTip.yRot = Mth.lerp(flyProgress, groundTipY, 0.0F);
        this.leftWingTip.zRot = Mth.lerp(flyProgress, groundTipZ, airTipZ);

        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.zRot = -this.leftWing.zRot;

        this.rightWingMid.xRot = this.leftWingMid.xRot;
        this.rightWingMid.yRot = -this.leftWingMid.yRot;
        this.rightWingMid.zRot = -this.leftWingMid.zRot;

        this.rightWingTip.xRot = this.leftWingTip.xRot;
        this.rightWingTip.yRot = -this.leftWingTip.yRot;
        this.rightWingTip.zRot = -this.leftWingTip.zRot;

        if (flyProgress > 0.05F) {
            float bodyPitch = Mth.clamp(entity.getXRot(), -30.0F, 30.0F) * ((float) Math.PI / 180.0F);
            this.body.xRot += bodyPitch * flyProgress;

            float turnRoll = Mth.clamp(headYawClamped * 0.6F, -0.35F, 0.35F);
            this.body.zRot += turnRoll * flyProgress;
        }

        float airFL_Leg = 0.80F;
        float airFL_Shin = -0.45F;
        float airFL_Foot = 0.25F;

        float airBL_Leg = 0.90F;
        float airBL_Shin = 0.40F;
        float airBL_Foot = -0.35F;

        float idleFL_Leg = 0.08F;
        float idleFL_Shin = -0.12F;
        float idleFL_Foot = 0.04F;

        float idleBL_Leg = -0.06F;
        float idleBL_Shin = 0.14F;
        float idleBL_Foot = -0.08F;

        float walkSpeed = 0.55F;
        float walkSwing = Mth.sin(limbSwing * walkSpeed);
        float walkCos = Mth.cos(limbSwing * walkSpeed);

        this.body.zRot += walkCos * 0.04F * walkAmt;
        this.body.y += Math.abs(walkSwing) * 0.6F * walkAmt;

        float walkFL_Leg = idleFL_Leg + walkSwing * 0.65F * walkAmt;
        float walkFL_Shin = idleFL_Shin + Math.max(0.0F, -walkSwing * 0.55F) * walkAmt;
        float walkFL_Foot = idleFL_Foot - Math.max(0.0F, -walkSwing * 0.35F) * walkAmt;

        float walkFR_Leg = idleFL_Leg - walkSwing * 0.65F * walkAmt;
        float walkFR_Shin = idleFL_Shin + Math.max(0.0F, walkSwing * 0.55F) * walkAmt;
        float walkFR_Foot = idleFL_Foot - Math.max(0.0F, walkSwing * 0.35F) * walkAmt;

        float walkBL_Leg = idleBL_Leg - walkSwing * 0.55F * walkAmt;
        float walkBL_Shin = idleBL_Shin + Math.max(0.0F, walkSwing * 0.45F) * walkAmt;
        float walkBL_Foot = idleBL_Foot - Math.max(0.0F, walkSwing * 0.30F) * walkAmt;

        float walkBR_Leg = idleBL_Leg + walkSwing * 0.55F * walkAmt;
        float walkBR_Shin = idleBL_Shin + Math.max(0.0F, -walkSwing * 0.45F) * walkAmt;
        float walkBR_Foot = idleBL_Foot - Math.max(0.0F, -walkSwing * 0.30F) * walkAmt;

        this.frontLeftLeg.xRot = Mth.lerp(flyProgress, walkFL_Leg, airFL_Leg);
        this.frontLeftShin.xRot = Mth.lerp(flyProgress, walkFL_Shin, airFL_Shin);
        this.frontLeftFoot.xRot = Mth.lerp(flyProgress, walkFL_Foot, airFL_Foot);

        this.frontRightLeg.xRot = Mth.lerp(flyProgress, walkFR_Leg, airFL_Leg);
        this.frontRightShin.xRot = Mth.lerp(flyProgress, walkFR_Shin, airFL_Shin);
        this.frontRightFoot.xRot = Mth.lerp(flyProgress, walkFR_Foot, airFL_Foot);

        this.backLeftLeg.xRot = Mth.lerp(flyProgress, walkBL_Leg, airBL_Leg);
        this.backLeftShin.xRot = Mth.lerp(flyProgress, walkBL_Shin, airBL_Shin);
        this.backLeftFoot.xRot = Mth.lerp(flyProgress, walkBL_Foot, airBL_Foot);

        this.backRightLeg.xRot = Mth.lerp(flyProgress, walkBR_Leg, airBL_Leg);
        this.backRightShin.xRot = Mth.lerp(flyProgress, walkBR_Shin, airBL_Shin);
        this.backRightFoot.xRot = Mth.lerp(flyProgress, walkBR_Foot, airBL_Foot);
    }
}
