package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.MitachurlEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D Autentico y Canonico del Mitachurl de Genshin Impact (Genshin Nature Revived).
 * Estructura completa de 256x256 con mascara osea tallada con cuernos curvados masivos,
 * melena de cuello y hombros, armadura tribal, Gran Hacha Ardiente (Blazing Axe)
 * y Muro de Escudos Gigante (Shieldwall).
 */
public class MitachurlModel extends HierarchicalModel<MitachurlEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mitachurl"), "main");

    private final ModelPart root;
    private final ModelPart bone4;
    private final ModelPart cabeza;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart fireaxe;
    private final ModelPart axeLeftArm;
    private final ModelPart shieldArm;
    private final ModelPart shield;

    public MitachurlModel(ModelPart root) {
        this.root = root;
        this.bone4 = root.getChild("bone4");
        this.fireaxe = root.getChild("fireaxe");
        this.shield = this.bone4.getChild("shield");

        ModelPart bone = this.bone4.getChild("bone");
        ModelPart belt = bone.getChild("belt");
        this.rightLeg = belt.getChild("RIGHT_LEG");
        this.leftLeg = belt.getChild("LEFT_LEG");

        ModelPart rotatetors = this.bone4.getChild("rotatetors");
        this.cabeza = rotatetors.getChild("CABEZA");
        this.axeLeftArm = rotatetors.getChild("lefthand2");
        this.shieldArm = rotatetors.getChild("BRAZO_LEFT");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone4 = buildMitachurlBody(root);
        buildMitachurlWeapons(root, bone4);

        return LayerDefinition.create(mesh, 256, 256);
    }

    private static PartDefinition buildMitachurlBody(PartDefinition root) {
        PartDefinition bone4 = root.addOrReplaceChild("bone4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -10.7031F, 0.0F, 0.1484F, 0.0F, 0.0F));
        PartDefinition bone4_bone = bone4.addOrReplaceChild("bone", CubeListBuilder.create()
                .texOffs(60, 32).addBox(-5.5000F, 0.8362F, -3.3867F, 11.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(44, 18).addBox(-6.5000F, -5.1638F, -4.1289F, 13.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 6.1442F, 3.0977F, -0.1484F, -0.1571F, 0.0F));
        PartDefinition bone4_bone_cube_r1 = bone4_bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(110, 35).addBox(-1.1992F, -5.1992F, -2.6367F, 2.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.5000F, -0.1638F, -0.3555F, 0.0F, 0.0F, -0.1265F));
        PartDefinition bone4_bone_cube_r2 = bone4_bone.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                .texOffs(0, 110).addBox(-6.8869F, -37.4557F, 1.2106F, 2.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.4667F, 32.6029F, -4.2028F, 0.0F, 0.0F, 0.1265F));
        PartDefinition bone4_bone_belt = bone4_bone.addOrReplaceChild("belt", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-6.8984F, 0.7734F, -3.7500F, 14.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(68, 43).addBox(-6.5273F, 0.0F, 0.1367F, 13.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(126, 89).addBox(1.5352F, 0.0F, -3.5273F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(126, 40).addBox(-6.5352F, 0.0F, -3.5273F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(46, 92).addBox(-1.6484F, 0.2734F, -4.7500F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 3.3011F, -0.3477F));
        PartDefinition bone4_bone_belt_cube_r3 = bone4_bone_belt.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                .texOffs(78, 10).addBox(-6.5000F, -2.6602F, 0.2070F, 13.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 4.5000F, 4.0F, 0.2138F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_cube_r4 = bone4_bone_belt.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                .texOffs(35, 99).addBox(-2.5260F, -1.2844F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.2891F, 0.1647F, 2.1953F, -2.4959F, 0.6000F, -2.0061F));
        PartDefinition bone4_bone_belt_cube_r5 = bone4_bone_belt.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-0.7773F, -2.2819F, -1.5000F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.2891F, 0.1647F, 2.1953F, 2.7469F, 0.7755F, 2.8127F));
        PartDefinition bone4_bone_belt_cube_r6 = bone4_bone_belt.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                .texOffs(35, 99).addBox(-2.5260F, -1.2844F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.7383F, 0.6413F, -2.1680F, 2.6489F, -0.1012F, -1.6156F));
        PartDefinition bone4_bone_belt_cube_r7 = bone4_bone_belt.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-0.7773F, -2.2819F, -1.5000F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.7383F, 0.6413F, -2.1680F, 2.9992F, -0.4832F, -2.7011F));
        PartDefinition bone4_bone_belt_cube_r8 = bone4_bone_belt.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                .texOffs(0, 78).addBox(-6.5000F, -0.4669F, 0.4837F, 13.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 2.2006F, -3.5622F, -0.4756F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_LEFT_LEG = bone4_bone_belt.addOrReplaceChild("LEFT_LEG", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.6911F, 1.8166F, 0.4428F, 0.6972F, 0.5786F, 0.3528F));
        PartDefinition bone4_bone_belt_LEFT_LEG_cube_r9 = bone4_bone_belt_LEFT_LEG.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                .texOffs(96, 25).addBox(-1.2760F, 5.8705F, 1.2215F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4365F, -2.8184F, -0.9748F, -0.4781F, 0.0345F, 0.0943F));
        PartDefinition bone4_bone_belt_LEFT_LEG_cube_r10 = bone4_bone_belt_LEFT_LEG.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                .texOffs(40, 97).addBox(-0.4348F, 3.9609F, -1.8975F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0227F, -4.8166F, -0.1351F, -0.1571F, 0.0F, 0.3491F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11 = bone4_bone_belt_LEFT_LEG.addOrReplaceChild("bone11", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.4365F, 12.8587F, -5.0083F, -0.0584F, -0.0664F, -0.2125F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_cube_r11 = bone4_bone_belt_LEFT_LEG_bone11.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                .texOffs(86, 125).addBox(-2.8732F, 5.1669F, -4.2135F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(128, 16).addBox(0.4901F, 5.1473F, -4.2565F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -6.8320F, -1.6523F, 0.3040F, 0.0568F, 0.0235F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_cube_r12 = bone4_bone_belt_LEFT_LEG_bone11.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                .texOffs(16, 116).addBox(-1.6927F, 2.2613F, -2.9330F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.6805F, -2.1340F, -1.0391F, 0.2528F, 0.0163F, 0.0134F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2 = bone4_bone_belt_LEFT_LEG_bone11.addOrReplaceChild("bone2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.8764F, 10.6335F, 0.3784F, -0.2617F, -0.0040F, -0.0610F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r13 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r13", CubeListBuilder.create()
                .texOffs(72, 14).addBox(-0.1507F, 4.9468F, -6.5321F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -6.2500F, -5.8867F, 0.8415F, 0.2269F, 0.0478F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r14 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r14", CubeListBuilder.create()
                .texOffs(81, 74).addBox(-0.5000F, 4.9822F, -6.0498F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.4764F, -5.8561F, -6.2187F, 0.7767F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r15 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r15", CubeListBuilder.create()
                .texOffs(82, 49).addBox(-0.8493F, 4.9468F, -6.5321F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.2064F, -6.2500F, -5.8867F, 0.8415F, -0.2269F, -0.0478F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r16 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r16", CubeListBuilder.create()
                .texOffs(98, 41).addBox(-0.7751F, 6.7156F, -4.5717F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0661F, -7.4174F, -4.1124F, 0.4139F, -0.2269F, -0.0478F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r17 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r17", CubeListBuilder.create()
                .texOffs(10, 110).addBox(0.6055F, 6.6539F, -4.6964F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.9489F, -7.4174F, -4.1124F, 0.4145F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r18 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r18", CubeListBuilder.create()
                .texOffs(129, 0).addBox(-1.4382F, 6.7156F, -4.5554F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.9489F, -7.4174F, -4.1124F, 0.4075F, 0.1509F, 0.0141F));
        PartDefinition bone4_bone_belt_LEFT_LEG_bone11_bone2_cube_r19 = bone4_bone_belt_LEFT_LEG_bone11_bone2.addOrReplaceChild("cube_r19", CubeListBuilder.create()
                .texOffs(116, 51).addBox(-2.8281F, 6.2536F, -2.3174F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.7770F, -7.1330F, 0.2681F, -0.0087F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_RIGHT_LEG = bone4_bone_belt.addOrReplaceChild("RIGHT_LEG", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.8005F, 3.4533F, 0.5483F, -0.2618F, -0.2618F, 0.0F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_cube_r20 = bone4_bone_belt_RIGHT_LEG.addOrReplaceChild("cube_r20", CubeListBuilder.create()
                .texOffs(64, 102).addBox(-3.7240F, 5.8705F, 1.2215F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3197F, -4.4551F, -0.9880F, -0.4781F, -0.0345F, -0.0943F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_cube_r21 = bone4_bone_belt_RIGHT_LEG.addOrReplaceChild("cube_r21", CubeListBuilder.create()
                .texOffs(96, 101).addBox(-5.5652F, 3.9609F, -1.8975F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.1395F, -6.4533F, -0.1483F, -0.1571F, 0.0F, -0.3491F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12 = bone4_bone_belt_RIGHT_LEG.addOrReplaceChild("bone12", CubeListBuilder.create(),
                PartPose.offset(3.1961F, 15.6055F, -10.5298F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_cube_r22 = bone4_bone_belt_RIGHT_LEG_bone12.addOrReplaceChild("cube_r22", CubeListBuilder.create()
                .texOffs(40, 129).addBox(-2.4901F, 5.1473F, -4.2565F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 126).addBox(-1.1268F, 5.1669F, -4.2135F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.8764F, -11.2155F, 3.8559F, 0.3040F, -0.0568F, -0.0235F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_cube_r23 = bone4_bone_belt_RIGHT_LEG_bone12.addOrReplaceChild("cube_r23", CubeListBuilder.create()
                .texOffs(58, 118).addBox(-2.3073F, 2.2613F, -2.9330F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.1959F, -6.5175F, 4.4691F, 0.2528F, -0.0163F, -0.0134F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3 = bone4_bone_belt_RIGHT_LEG_bone12.addOrReplaceChild("bone3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.4297F, 5.3242F, 7.7227F, 0.2967F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r24 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r24", CubeListBuilder.create()
                .texOffs(84, 59).addBox(-0.8493F, 4.9468F, -6.5321F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.4297F, -5.3242F, -7.7227F, 0.8415F, -0.2269F, -0.0478F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r25 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r25", CubeListBuilder.create()
                .texOffs(74, 85).addBox(-0.5000F, 4.9822F, -6.0498F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0467F, -4.9303F, -8.0546F, 0.7767F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r26 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r26", CubeListBuilder.create()
                .texOffs(54, 88).addBox(-0.1507F, 4.9898F, -6.5321F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.7767F, -5.3242F, -7.7227F, 0.8415F, 0.2269F, 0.0478F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r27 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r27", CubeListBuilder.create()
                .texOffs(130, 69).addBox(-1.9393F, -5.0522F, -3.1318F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7166F, 4.9188F, -2.7823F, 0.4139F, 0.2269F, 0.0478F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r28 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r28", CubeListBuilder.create()
                .texOffs(130, 76).addBox(-0.4081F, -5.0652F, -2.9987F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7166F, 4.9188F, -2.7823F, 0.4145F, 0.0F, 0.0F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r29 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r29", CubeListBuilder.create()
                .texOffs(130, 95).addBox(1.2938F, -5.1118F, -3.2448F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7166F, 4.9188F, -2.7823F, 0.4075F, -0.1509F, -0.0141F));
        PartDefinition bone4_bone_belt_RIGHT_LEG_bone12_bone3_cube_r30 = bone4_bone_belt_RIGHT_LEG_bone12_bone3.addOrReplaceChild("cube_r30", CubeListBuilder.create()
                .texOffs(118, 61).addBox(-2.1719F, 6.2536F, -2.3174F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.3473F, -6.2073F, -1.5678F, -0.0087F, 0.0F, 0.0F));
        PartDefinition bone4_rotatetors = bone4.addOrReplaceChild("rotatetors", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -2.6240F, 2.1197F, 0.2597F, -0.4184F, -0.0646F));
        PartDefinition bone4_rotatetors_cube_r31 = bone4_rotatetors.addOrReplaceChild("cube_r31", CubeListBuilder.create()
                .texOffs(12, 0).addBox(-7.5000F, -2.2489F, -3.1247F, 15.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0880F, -1.6122F, 0.1091F, 0.0F, 0.0F));
        PartDefinition bone4_rotatetors_righthand2 = bone4_rotatetors.addOrReplaceChild("righthand2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(7.7999F, -2.8628F, -0.0904F, -0.2857F, 0.4333F, 0.4894F));
        PartDefinition bone4_rotatetors_righthand2_cube_r32 = bone4_rotatetors_righthand2.addOrReplaceChild("cube_r32", CubeListBuilder.create()
                .texOffs(74, 118).addBox(6.5087F, -6.3284F, -1.7282F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.2650F, 2.9409F, -1.3506F, 0.0783F, -0.0760F, 0.7344F));
        PartDefinition bone4_rotatetors_righthand2_cube_r33 = bone4_rotatetors_righthand2.addOrReplaceChild("cube_r33", CubeListBuilder.create()
                .texOffs(20, 87).addBox(4.2384F, -4.5521F, -1.7478F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.2650F, 2.9409F, -1.3506F, 0.1090F, -0.0038F, -0.0002F));
        PartDefinition bone4_rotatetors_righthand2_bone5 = bone4_rotatetors_righthand2.addOrReplaceChild("bone5", CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.2741F, 1.1019F, -0.3656F, 0.1675F, 0.3940F, 0.6403F));
        PartDefinition bone4_rotatetors_righthand2_bone5_cube_r34 = bone4_rotatetors_righthand2_bone5.addOrReplaceChild("cube_r34", CubeListBuilder.create()
                .texOffs(104, 14).addBox(11.2384F, -4.2240F, -1.3767F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-11.2226F, 1.8390F, -0.9851F, 0.1090F, -0.0038F, -0.0002F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8 = bone4_rotatetors_righthand2_bone5.addOrReplaceChild("bone8", CubeListBuilder.create(),
                PartPose.offsetAndRotation(7.2735F, -0.8041F, -0.0348F, 0.6174F, 0.5305F, 0.3572F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_cube_r35 = bone4_rotatetors_righthand2_bone5_bone8.addOrReplaceChild("cube_r35", CubeListBuilder.create()
                .texOffs(104, 0).addBox(23.3321F, -4.8568F, -2.3767F, 3.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(118, 69).addBox(17.7149F, -4.2943F, -1.8298F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(114, 107).addBox(20.7149F, -4.6068F, -1.8298F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-18.4961F, 2.6431F, -0.9503F, 0.1090F, -0.0038F, -0.0002F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_cube_r36 = bone4_rotatetors_righthand2_bone5_bone8.addOrReplaceChild("cube_r36", CubeListBuilder.create()
                .texOffs(86, 66).addBox(4.2721F, 1.2485F, 7.1732F, 9.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.4964F, -1.5045F, -9.9518F, 0.1090F, -0.0038F, -0.0002F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9 = bone4_rotatetors_righthand2_bone5_bone8.addOrReplaceChild("bone9", CubeListBuilder.create(),
                PartPose.offsetAndRotation(7.6960F, 1.1028F, 0.2648F, 1.6656F, 0.1250F, 0.0605F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r37 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r37", CubeListBuilder.create()
                .texOffs(130, 105).addBox(-26.8179F, 1.5673F, 21.5699F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, 2.7807F, 0.6451F, 2.9489F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r38 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r38", CubeListBuilder.create()
                .texOffs(130, 109).addBox(-22.6801F, 0.3598F, 25.5488F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, 2.9779F, 0.8399F, 3.0230F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r39 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r39", CubeListBuilder.create()
                .texOffs(131, 24).addBox(-27.4218F, -0.9766F, 22.1379F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, -3.0554F, 0.6610F, -3.1104F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r40 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r40", CubeListBuilder.create()
                .texOffs(52, 62).addBox(22.1668F, -9.6475F, 17.9941F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, 7.9363F, -1.6787F, 1.2145F, 0.0146F, 0.3759F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r41 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r41", CubeListBuilder.create()
                .texOffs(131, 50).addBox(17.3881F, 1.8999F, 25.7836F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, -0.0571F, 0.9281F, -0.1493F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r42 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r42", CubeListBuilder.create()
                .texOffs(52, 132).addBox(17.4740F, 0.3598F, 25.7172F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, 0.1799F, 0.9171F, 0.1472F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r43 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r43", CubeListBuilder.create()
                .texOffs(132, 123).addBox(17.6945F, -1.2978F, 25.7836F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, 0.3934F, 0.8661F, 0.4208F));
        PartDefinition bone4_rotatetors_righthand2_bone5_bone8_bone9_cube_r44 = bone4_rotatetors_righthand2_bone5_bone8_bone9.addOrReplaceChild("cube_r44", CubeListBuilder.create()
                .texOffs(120, 32).addBox(27.1485F, -1.2108F, 0.1043F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-26.2416F, -1.0637F, -1.6787F, 0.1090F, -0.0001F, 0.0038F));
        PartDefinition bone4_rotatetors_lefthand2 = bone4_rotatetors.addOrReplaceChild("lefthand2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-6.7999F, -2.8628F, -0.0904F, -0.2296F, 0.1688F, -0.5781F));
        PartDefinition bone4_rotatetors_lefthand2_cube_r45 = bone4_rotatetors_lefthand2.addOrReplaceChild("cube_r45", CubeListBuilder.create()
                .texOffs(32, 116).addBox(-8.5087F, -6.3284F, -1.7282F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.7185F, 2.7599F, -1.0452F, 0.0783F, 0.0760F, -0.7344F));
        PartDefinition bone4_rotatetors_lefthand2_cube_r46 = bone4_rotatetors_lefthand2.addOrReplaceChild("cube_r46", CubeListBuilder.create()
                .texOffs(54, 85).addBox(-11.2384F, -4.5521F, -1.7478F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.7185F, 2.7599F, -1.0452F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition bone4_rotatetors_lefthand2_bone6 = bone4_rotatetors_lefthand2.addOrReplaceChild("bone6", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-3.8205F, 0.9209F, -0.0601F, -0.5824F, -0.2563F, -0.2014F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_cube_r47 = bone4_rotatetors_lefthand2_bone6.addOrReplaceChild("cube_r47", CubeListBuilder.create()
                .texOffs(102, 87).addBox(-18.2384F, -4.2240F, -1.3767F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(11.2226F, 1.8390F, -0.9851F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7 = bone4_rotatetors_lefthand2_bone6.addOrReplaceChild("bone7", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-7.2735F, -0.8041F, -0.0348F, 1.6795F, -1.1894F, -2.4299F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_cube_r48 = bone4_rotatetors_lefthand2_bone6_bone7.addOrReplaceChild("cube_r48", CubeListBuilder.create()
                .texOffs(22, 102).addBox(-26.3321F, -4.8568F, -2.3767F, 3.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(117, 0).addBox(-20.7149F, -4.2943F, -1.8298F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(114, 97).addBox(-25.7149F, -4.6068F, -1.8298F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(18.4961F, 2.6431F, -0.9503F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_cube_r49 = bone4_rotatetors_lefthand2_bone6_bone7.addOrReplaceChild("cube_r49", CubeListBuilder.create()
                .texOffs(84, 49).addBox(-13.2721F, 1.2485F, 7.1732F, 9.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.4964F, -1.5045F, -9.9518F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10 = bone4_rotatetors_lefthand2_bone6_bone7.addOrReplaceChild("bone10", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-8.3992F, 0.5012F, 0.2648F, 0.3568F, -0.0060F, 0.1062F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r50 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r50", CubeListBuilder.create()
                .texOffs(26, 83).addBox(21.8179F, 1.5673F, 21.5699F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 2.7807F, -0.6451F, -2.9489F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r51 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r51", CubeListBuilder.create()
                .texOffs(40, 88).addBox(17.6801F, 0.3598F, 25.5488F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 2.9779F, -0.8399F, -3.0230F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r52 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r52", CubeListBuilder.create()
                .texOffs(58, 97).addBox(22.4218F, -0.9766F, 21.1379F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, -3.0554F, -0.6610F, 3.1104F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r53 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r53", CubeListBuilder.create()
                .texOffs(12, 18).addBox(-27.2879F, -6.6554F, 10.4120F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 1.2145F, -0.0146F, -0.3759F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r54 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r54", CubeListBuilder.create()
                .texOffs(102, 97).addBox(-22.3881F, 1.8999F, 25.7836F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, -0.0571F, -0.9281F, 0.1493F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r55 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r55", CubeListBuilder.create()
                .texOffs(107, 83).addBox(-22.4740F, 0.3598F, 25.7172F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 0.1799F, -0.9171F, -0.1472F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r56 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r56", CubeListBuilder.create()
                .texOffs(126, 46).addBox(-22.6945F, -1.2978F, 25.7836F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 0.3934F, -0.8661F, -0.4208F));
        PartDefinition bone4_rotatetors_lefthand2_bone6_bone7_bone10_cube_r57 = bone4_rotatetors_lefthand2_bone6_bone7_bone10.addOrReplaceChild("cube_r57", CubeListBuilder.create()
                .texOffs(118, 115).addBox(-33.1485F, -1.2108F, 0.1043F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 0.1090F, 0.0001F, -0.0038F));
        PartDefinition bone4_rotatetors_CABEZA = bone4_rotatetors.addOrReplaceChild("CABEZA", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0486F, -8.8762F, -2.8027F, -0.0413F, 0.2132F, -0.0019F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r58 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r58", CubeListBuilder.create()
                .texOffs(121, 78).addBox(-0.6914F, -3.0F, -2.5820F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.2606F, 10.4338F, 6.7141F, -1.4953F, -0.0234F, 0.2432F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r59 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r59", CubeListBuilder.create()
                .texOffs(70, 129).addBox(0.2578F, -3.0F, -2.5820F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.8735F, 10.4338F, 6.7141F, -1.4953F, 0.0234F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r60 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r60", CubeListBuilder.create()
                .texOffs(60, 3).addBox(-1.5000F, -2.3282F, 3.8359F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(68, 42).addBox(-0.5391F, -2.3282F, 1.5000F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.7101F, 0.1719F, -5.7123F, 0.1761F, 0.2049F, 0.1957F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r61 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r61", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-4.5000F, 0.3711F, 0.7148F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.8781F, -2.6874F, -5.7123F, 0.6778F, -0.0401F, 0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r62 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r62", CubeListBuilder.create()
                .texOffs(29, 18).addBox(-4.5000F, 0.6249F, -2.0F, 8.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0764F, -5.6719F, -0.0190F, 0.3506F, -0.0401F, 0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r63 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r63", CubeListBuilder.create()
                .texOffs(91, 53).addBox(0.0F, -3.5000F, -3.5000F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.8946F, 0.7747F, 6.5649F, -0.3001F, 0.1215F, 0.1050F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r64 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r64", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-3.5000F, 0.3320F, -3.1836F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5156F, -4.0513F, 8.0584F, -0.0378F, -0.0401F, 0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r65 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r65", CubeListBuilder.create()
                .texOffs(21, 78).addBox(-3.0F, 0.3164F, -2.5000F, 6.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.8602F, -0.3515F, 10.0243F, -0.6137F, -0.0401F, 0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r66 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r66", CubeListBuilder.create()
                .texOffs(35, 55).addBox(0.1055F, -3.5000F, -3.5000F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.7541F, 3.5588F, 6.8140F, -0.9754F, 0.0427F, 0.0608F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r67 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r67", CubeListBuilder.create()
                .texOffs(102, 120).addBox(-0.1758F, -3.5000F, -1.6953F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0216F, 6.2986F, 6.9099F, -1.4921F, -0.0066F, 0.0257F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r68 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r68", CubeListBuilder.create()
                .texOffs(81, 25).addBox(-2.5000F, -0.0039F, -2.8008F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0365F, 5.5427F, 10.8492F, -1.2316F, -0.0161F, 0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r69 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r69", CubeListBuilder.create()
                .texOffs(7, 0).addBox(-1.0F, 0.2695F, -2.5195F, 2.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.6686F, 10.1358F, 10.4223F, -1.2423F, -0.0234F, 0.2432F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r70 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r70", CubeListBuilder.create()
                .texOffs(60, 3).addBox(1.5000F, -2.3282F, 4.8359F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(68, 42).addBox(0.5391F, -2.3282F, 1.5000F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.8073F, 0.1719F, -5.7123F, 0.1761F, -0.2049F, -0.1957F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r71 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r71", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-3.5000F, 0.3711F, 0.7148F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.9753F, -2.6874F, -5.7123F, 0.6778F, 0.0401F, -0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r72 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r72", CubeListBuilder.create()
                .texOffs(29, 18).addBox(-3.5000F, 0.6249F, -2.0F, 8.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.1736F, -5.6719F, -0.0190F, 0.3506F, 0.0401F, -0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r73 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r73", CubeListBuilder.create()
                .texOffs(91, 53).addBox(0.0F, -3.5000F, -3.5000F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-6.9918F, 0.7747F, 6.5649F, -0.3001F, -0.1215F, -0.1050F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r74 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r74", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-3.5000F, 0.3320F, -3.1836F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.6129F, -4.0513F, 8.0584F, -0.0378F, 0.0401F, -0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r75 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r75", CubeListBuilder.create()
                .texOffs(21, 78).addBox(-3.0F, 0.3164F, -2.5000F, 6.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.9575F, -0.3515F, 10.0243F, -0.6137F, 0.0401F, -0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r76 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r76", CubeListBuilder.create()
                .texOffs(35, 55).addBox(-0.1055F, -3.5000F, -3.5000F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.8513F, 3.5588F, 6.8140F, -0.9754F, -0.0427F, -0.0608F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r77 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r77", CubeListBuilder.create()
                .texOffs(102, 120).addBox(0.1758F, -3.5000F, -1.6953F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.1189F, 6.2986F, 6.9099F, -1.4921F, 0.0066F, -0.0257F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r78 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r78", CubeListBuilder.create()
                .texOffs(81, 25).addBox(-2.5000F, -0.0039F, -2.8008F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.1338F, 5.5427F, 10.8492F, -1.2316F, 0.0161F, -0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r79 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r79", CubeListBuilder.create()
                .texOffs(7, 0).addBox(-1.0F, 0.2695F, -2.5195F, 2.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7658F, 10.1358F, 10.4223F, -1.2423F, 0.0234F, -0.2432F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r80 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r80", CubeListBuilder.create()
                .texOffs(121, 78).mirror() .addBox(-1.3086F, -3.0F, -2.5820F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4633F, 10.4338F, 6.7141F, -1.4953F, 0.0234F, -0.2432F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r81 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r81", CubeListBuilder.create()
                .texOffs(42, 109).addBox(-2.0703F, -3.1055F, -3.0742F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.4375F, 6.3298F, 7.2924F, -1.4934F, -0.0161F, 0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r82 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r82", CubeListBuilder.create()
                .texOffs(108, 128).addBox(1.8750F, -3.5000F, -2.5000F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5620F, 5.8578F, 6.8616F, -1.4934F, 0.0161F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r83 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r83", CubeListBuilder.create()
                .texOffs(42, 109).mirror() .addBox(-2.9297F, -3.1055F, -3.0742F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.6402F, 6.3298F, 7.2924F, -1.4934F, 0.0161F, -0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r84 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r84", CubeListBuilder.create()
                .texOffs(81, 78).addBox(-0.2309F, -10.9003F, -2.7521F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5642F, 5.2139F, -0.4956F, -0.9742F, -0.0161F, 0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r85 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r85", CubeListBuilder.create()
                .texOffs(105, 52).addBox(-1.3863F, -10.9003F, -2.7521F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0486F, 5.2139F, -0.4956F, -0.9742F, 0.0161F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r86 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r86", CubeListBuilder.create()
                .texOffs(81, 78).mirror() .addBox(-5.7691F, -10.9003F, -2.7521F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3615F, 5.2139F, -0.4956F, -0.9742F, 0.0161F, -0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r87 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r87", CubeListBuilder.create()
                .texOffs(107, 69).addBox(-1.1402F, -10.7714F, -1.0060F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0486F, 5.2139F, -0.4956F, -0.2979F, 0.0161F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r88 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r88", CubeListBuilder.create()
                .texOffs(60, 71).addBox(-0.4184F, -10.7714F, -1.0060F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5642F, 5.2139F, -0.4956F, -0.2979F, -0.0161F, 0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r89 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r89", CubeListBuilder.create()
                .texOffs(60, 71).mirror() .addBox(-6.5816F, -10.7714F, -1.0060F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3615F, 5.2139F, -0.4956F, -0.2979F, 0.0161F, -0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r90 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r90", CubeListBuilder.create()
                .texOffs(84, 107).addBox(-0.5659F, -10.4839F, -0.3747F, 1.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0486F, 5.2139F, -0.4956F, 0.1079F, 0.0161F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r91 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r91", CubeListBuilder.create()
                .texOffs(12, 62).addBox(-6.9956F, -10.4057F, -0.3747F, 8.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(36, 36).addBox(-7.0386F, -9.4057F, -3.3513F, 8.0F, 18.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3615F, 5.2139F, -0.4956F, 0.1079F, 0.0161F, -0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r92 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r92", CubeListBuilder.create()
                .texOffs(61, 55).addBox(-1.0044F, -10.4057F, -0.3747F, 8.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(12, 18).addBox(-0.9535F, -9.4069F, -3.3747F, 8.0F, 18.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5642F, 5.2139F, -0.4956F, 0.1079F, -0.0161F, 0.1475F));
        PartDefinition bone4_rotatetors_CABEZA_cube_r93 = bone4_rotatetors_CABEZA.addOrReplaceChild("cube_r93", CubeListBuilder.create()
                .texOffs(42, 62).addBox(-0.4961F, -9.4881F, -3.3278F, 1.0F, 18.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0486F, 5.2139F, -0.4956F, 0.1091F, 0.0F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate = bone4_rotatetors_CABEZA.addOrReplaceChild("headrotate", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.2196F, -0.4413F, -4.9428F, 0.0228F, 0.0273F, 0.0433F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r94 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r94", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-4.5000F, -0.1406F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0977F, -2.2461F, -0.7695F, 0.7607F, -0.0401F, 0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r95 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r95", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-3.5000F, -0.1406F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.7557F, -2.2461F, -0.7695F, 0.7607F, 0.0401F, -0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r96 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r96", CubeListBuilder.create()
                .texOffs(12, 5).mirror() .addBox(3.9727F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.6193F, 0.3280F, -1.2848F, 0.3334F, 0.2414F, -0.0297F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r97 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r97", CubeListBuilder.create()
                .texOffs(118, 132).mirror() .addBox(4.0F, 0.4057F, 0.6434F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-8.6841F, -0.9835F, -5.4946F, -0.0715F, 0.2250F, -0.0539F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r98 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r98", CubeListBuilder.create()
                .texOffs(111, 24).mirror() .addBox(2.0F, -0.4682F, -1.4800F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-6.6841F, -0.9835F, -5.4946F, -0.7173F, 0.2250F, -0.0539F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r99 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r99", CubeListBuilder.create()
                .texOffs(68, 97).mirror() .addBox(-0.5000F, 0.4805F, -4.3682F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.6841F, -0.9835F, -5.4946F, -1.0446F, 0.2250F, -0.0539F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r100 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r100", CubeListBuilder.create()
                .texOffs(68, 97).addBox(-0.5000F, 0.4805F, -4.3682F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.7762F, -0.9835F, -5.4946F, -1.0446F, -0.2250F, 0.0539F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r101 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r101", CubeListBuilder.create()
                .texOffs(111, 24).addBox(-4.0F, -0.4682F, -1.4800F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.7762F, -0.9835F, -5.4946F, -0.7173F, -0.2250F, 0.0539F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r102 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r102", CubeListBuilder.create()
                .texOffs(118, 132).addBox(-6.0F, 0.4057F, 0.6434F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(9.7762F, -0.9835F, -5.4946F, -0.0715F, -0.2250F, 0.0539F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r103 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r103", CubeListBuilder.create()
                .texOffs(12, 5).addBox(-5.9727F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(8.7113F, 0.3280F, -1.2848F, 0.3334F, -0.2414F, 0.0297F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r104 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r104", CubeListBuilder.create()
                .texOffs(51, 6).addBox(-4.0F, -1.0F, -0.5000F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.4288F, 0.7502F, -2.9939F, 0.0F, 0.1309F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r105 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r105", CubeListBuilder.create()
                .texOffs(59, 6).addBox(0.0F, -1.0F, -0.5000F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3155F, 0.7502F, -2.9939F, 0.0F, -0.1309F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r106 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r106", CubeListBuilder.create()
                .texOffs(12, 66).addBox(-7.0F, -2.0F, -0.2500F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3546F, 3.7502F, -3.2439F, 0.0F, 0.1309F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r107 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r107", CubeListBuilder.create()
                .texOffs(60, 75).addBox(4.0F, -2.0F, -0.2500F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(70, 118).addBox(0.0F, -2.0F, -0.5000F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3155F, 3.7502F, -3.2439F, 0.0F, -0.1309F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r108 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r108", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-1.9365F, -1.5000F, 0.3767F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.6923F, 6.2658F, -3.4650F, 0.0660F, 0.1306F, 0.0086F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r109 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r109", CubeListBuilder.create()
                .texOffs(68, 43).addBox(-0.0635F, -1.5000F, 0.3767F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.3624F, 6.2658F, -3.4650F, 0.0660F, -0.1306F, -0.0086F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r110 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r110", CubeListBuilder.create()
                .texOffs(12, 62).addBox(-1.9365F, -1.5000F, -0.6233F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3624F, 6.2658F, -2.9650F, 0.0660F, 0.1306F, 0.0086F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r111 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r111", CubeListBuilder.create()
                .texOffs(60, 71).addBox(-0.0635F, -1.5000F, -0.6233F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3624F, 6.2658F, -2.9650F, 0.0660F, -0.1306F, -0.0086F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r112 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r112", CubeListBuilder.create()
                .texOffs(28, 116).addBox(-4.0F, -2.0F, -0.5000F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.4288F, 3.7502F, -3.2439F, 0.0F, 0.1309F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r113 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r113", CubeListBuilder.create()
                .texOffs(80, 131).addBox(-1.0034F, -3.6894F, -0.9939F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1710F, 2.9263F, -1.2476F, 0.2163F, -0.0243F, 0.0023F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r114 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r114", CubeListBuilder.create()
                .texOffs(116, 123).addBox(0.4386F, -3.8168F, -0.9308F, 6.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.3447F, 2.9263F, -1.2476F, 0.2174F, -0.0103F, 0.1480F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r115 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r115", CubeListBuilder.create()
                .texOffs(124, 7).addBox(-6.4386F, -3.8168F, -0.9308F, 6.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5811F, 2.9263F, -1.2476F, 0.2174F, 0.0103F, -0.1480F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r116 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r116", CubeListBuilder.create()
                .texOffs(128, 128).addBox(-0.6033F, -8.8396F, -4.6375F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1710F, 5.6553F, 4.4472F, 0.2749F, 0.0F, 0.0F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r117 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r117", CubeListBuilder.create()
                .texOffs(80, 92).addBox(-6.3535F, -8.8795F, -4.7274F, 7.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5811F, 5.6553F, 4.4472F, 0.2720F, 0.0401F, -0.1429F));
        PartDefinition bone4_rotatetors_CABEZA_headrotate_cube_r118 = bone4_rotatetors_CABEZA_headrotate.addOrReplaceChild("cube_r118", CubeListBuilder.create()
                .texOffs(0, 95).addBox(-0.3848F, -8.8795F, -4.7469F, 7.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.3447F, 5.6553F, 4.4472F, 0.2720F, -0.0401F, 0.1429F));
        PartDefinition rotatetors_BRAZO_LEFT = bone4_rotatetors.addOrReplaceChild("BRAZO_LEFT", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-6.7999F, -2.8628F, -0.0904F, -0.1960F, 0.2917F, -0.2506F));
        PartDefinition rotatetors_BRAZO_LEFT_cube_r29 = rotatetors_BRAZO_LEFT.addOrReplaceChild("cube_r29", CubeListBuilder.create()
                .texOffs(138, 6).addBox(-8.5087F, -6.3284F, -1.7282F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.7185F, 2.7599F, -1.0452F, 0.0783F, 0.0760F, -0.7344F));
        PartDefinition rotatetors_BRAZO_LEFT_cube_r30 = rotatetors_BRAZO_LEFT.addOrReplaceChild("cube_r30", CubeListBuilder.create()
                .texOffs(112, 116).addBox(-11.2384F, -4.5521F, -1.7478F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.7185F, 2.7599F, -1.0452F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6 = rotatetors_BRAZO_LEFT.addOrReplaceChild("bone6", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-3.8205F, 0.9209F, -0.0601F, -0.3796F, 0.0F, -0.3054F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_cube_r31 = rotatetors_BRAZO_LEFT_bone6.addOrReplaceChild("cube_r31", CubeListBuilder.create()
                .texOffs(125, 53).addBox(-18.2384F, -4.2240F, -1.3767F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(11.2226F, 1.8390F, -0.9851F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7 = rotatetors_BRAZO_LEFT_bone6.addOrReplaceChild("bone7", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-7.2735F, -0.8041F, -0.0348F, 0.6714F, -0.7399F, -0.7224F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_cube_r32 = rotatetors_BRAZO_LEFT_bone6_bone7.addOrReplaceChild("cube_r32", CubeListBuilder.create()
                .texOffs(0, 99).addBox(-26.3321F, -4.8568F, -2.3767F, 3.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(94, 140).addBox(-20.7149F, -4.2943F, -1.8298F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(136, 18).addBox(-25.7149F, -4.6068F, -1.8298F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(18.4961F, 2.6431F, -0.9503F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_cube_r33 = rotatetors_BRAZO_LEFT_bone6_bone7.addOrReplaceChild("cube_r33", CubeListBuilder.create()
                .texOffs(84, 116).addBox(-13.2721F, 1.2485F, 7.1732F, 9.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.4964F, -1.5045F, -9.9518F, 0.1090F, 0.0038F, 0.0002F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10 = rotatetors_BRAZO_LEFT_bone6_bone7.addOrReplaceChild("bone10", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-8.3992F, 0.5012F, 0.2648F, 1.8585F, -0.1581F, -0.2093F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r34 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r34", CubeListBuilder.create()
                .texOffs(76, 47).addBox(21.8179F, 1.5673F, 21.5699F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 2.7807F, -0.6451F, -2.9489F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r35 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r35", CubeListBuilder.create()
                .texOffs(110, 0).addBox(17.6801F, 0.3598F, 25.5488F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 2.9779F, -0.8399F, -3.0230F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r36 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r36", CubeListBuilder.create()
                .texOffs(66, 121).addBox(22.4218F, -0.9766F, 21.1379F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, -3.0554F, -0.6610F, 3.1104F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r37 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r37", CubeListBuilder.create()
                .texOffs(13, 40).addBox(-27.2879F, -6.6554F, 10.4120F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 1.2145F, -0.0146F, -0.3759F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r38 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r38", CubeListBuilder.create()
                .texOffs(85, 126).addBox(-22.3881F, 1.8999F, 25.7836F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, -0.0571F, -0.9281F, 0.1493F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r39 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r39", CubeListBuilder.create()
                .texOffs(107, 128).addBox(-22.4740F, 0.3598F, 25.7172F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 0.1799F, -0.9171F, -0.1472F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r40 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r40", CubeListBuilder.create()
                .texOffs(140, 42).addBox(-22.6945F, -1.2978F, 25.7836F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 0.3934F, -0.8661F, -0.4208F));
        PartDefinition rotatetors_BRAZO_LEFT_bone6_bone7_bone10_cube_r41 = rotatetors_BRAZO_LEFT_bone6_bone7_bone10.addOrReplaceChild("cube_r41", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-33.1485F, -1.2108F, 0.1043F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(27.4417F, -1.7327F, -1.7318F, 0.1090F, 0.0001F, -0.0038F));

        return bone4;
    }

    private static void buildMitachurlWeapons(PartDefinition root, PartDefinition bone4) {
        PartDefinition fireaxe = root.addOrReplaceChild("fireaxe", CubeListBuilder.create(),
                PartPose.offsetAndRotation(22.1328F, 8.2730F, -4.0230F, -0.0216F, 0.4897F, -0.8131F));
        PartDefinition fireaxe_cube_r119 = fireaxe.addOrReplaceChild("cube_r119", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-1.5000F, 27.4558F, -1.4908F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 134).addBox(-2.0F, -28.0442F, -1.9908F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(116, 24).addBox(-2.5000F, -32.0442F, -2.4908F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(28, 127).addBox(-2.0F, 23.7058F, -1.9908F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 116).addBox(0.0F, -44.9558F, 3.4908F, 0.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(152, 12).addBox(-0.5000F, -47.9558F, -14.0092F, 1.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(12, 32).addBox(0.0F, -47.9558F, -14.0092F, 0.0F, 18.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(60, 32).addBox(-1.0F, -41.9558F, 2.4908F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 113).addBox(-2.0F, -42.9558F, -2.5092F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5000F, -46.2058F, -1.5092F, 3.0F, 71.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 130).addBox(-1.0F, 21.7058F, -0.9908F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.3105F, -24.0331F, -6.8812F, 0.3272F, 0.0F, 0.0F));

        PartDefinition bone4_shield = bone4.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(48, 7).addBox(-4.2426F, -22.1213F, -1.5000F, 3.0F, 20.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 30).addBox(1.5074F, -39.1213F, -4.5000F, 2.0F, 50.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(62, 41).addBox(2.7574F, -17.8713F, -4.5000F, 2.0F, 14.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(62, 62).addBox(2.7574F, 6.1287F, -4.5000F, 2.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(62, 0).addBox(2.7574F, -49.1213F, -4.5000F, 2.0F, 27.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.5074F, -55.6213F, -11.5000F, 0.0F, 16.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(105, 104).addBox(1.0152F, -39.6213F, -4.5000F, 3.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(105, 104).addBox(1.0152F, 9.1287F, -4.5000F, 3.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                , PartPose.offsetAndRotation(-4.8478F, 19.2254F, -25.7707F, -0.1484F, 1.5708F, 0.0F));
        PartDefinition bone4_shield_cube_r1 = bone4_shield.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(0, 146).addBox(-1.5000F, -4.5000F, -1.5000F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.7854F));
        PartDefinition bone4_shield_cube_r2 = bone4_shield.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                .texOffs(0, 146).addBox(-1.5000F, -3.5000F, -1.5000F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0000F, -24.2426F, 0.0000F, 0.0000F, 0.0000F, 0.7854F));
        PartDefinition bone4_shield_cube_r3 = bone4_shield.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                .texOffs(45, 149).addBox(-2.5000F, -25.0000F, 0.0000F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(45, 149).addBox(-2.5000F, -73.7500F, 0.0000F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5266F, 34.1287F, 5.3939F, 0.0000F, -0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r4 = bone4_shield.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                .texOffs(70, 126).addBox(-2.5000F, -25.0000F, -9.0000F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(70, 126).addBox(-2.5000F, -73.7500F, -9.0000F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5266F, 34.1287F, -4.3939F, 0.0000F, 0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r5 = bone4_shield.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                .texOffs(23, 83).addBox(-1.5000F, -25.0000F, -9.0000F, 2.0F, 15.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(191, 2).addBox(-1.5000F, 2.2500F, -9.0000F, 2.0F, 21.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.2687F, -14.1213F, -4.3939F, 0.0000F, 0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r6 = bone4_shield.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                .texOffs(193, 77).addBox(0.0000F, -4.5000F, -4.5000F, 1.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.3253F, -43.6213F, 10.4354F, -0.3927F, -0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r7 = bone4_shield.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                .texOffs(193, 58).addBox(0.0000F, -4.5000F, -3.5000F, 1.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.3253F, -43.6213F, -9.9354F, 0.3927F, 0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r8 = bone4_shield.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                .texOffs(160, 0).addBox(-1.5000F, -25.0000F, 0.0000F, 2.0F, 15.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(167, 45).addBox(-1.5000F, 2.2500F, 0.0000F, 2.0F, 21.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.2687F, -14.1213F, 5.3939F, 0.0000F, -0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r9 = bone4_shield.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                .texOffs(169, 78).addBox(-1.5000F, -25.0000F, -9.0000F, 2.0F, 50.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0187F, -14.1213F, -4.3939F, 0.0000F, 0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r10 = bone4_shield.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                .texOffs(22, 40).addBox(-2.0000F, -25.0000F, -9.5000F, 3.0F, 50.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0187F, -14.1213F, -4.1439F, 0.0000F, 0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r11 = bone4_shield.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                .texOffs(22, 40).addBox(-2.0000F, -25.0000F, 7.5000F, 3.0F, 50.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.7687F, -14.1213F, 5.1439F, 0.0000F, -0.2138F, 0.0000F));
        PartDefinition bone4_shield_cube_r12 = bone4_shield.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-1.5000F, -25.0000F, 0.0000F, 2.0F, 50.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0187F, -14.1213F, 5.3939F, 0.0000F, -0.2138F, 0.0000F));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MitachurlEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.cabeza.yRot = netHeadYaw * (Mth.PI / 180F);
        this.cabeza.xRot = headPitch * (Mth.PI / 180F);

        this.rightLeg.xRot = -0.2618F + Mth.cos(limbSwing) * 0.8F * limbSwingAmount;
        this.leftLeg.xRot = 0.6972F - Mth.cos(limbSwing) * 0.8F * limbSwingAmount;

        boolean isAxe = (entity.getMitachurlType() == MitachurlEntity.MitachurlType.BLAZING_AXE);
        this.fireaxe.visible = isAxe;
        this.axeLeftArm.visible = isAxe;
        this.shieldArm.visible = !isAxe;
        this.shield.visible = !isAxe;

        if (isAxe) {
            if (entity.getTarget() != null) {
                this.fireaxe.xRot = -0.0216F + Mth.sin(ageInTicks * 0.25F) * 0.2F;
            }
        } else {
            if (entity.isCharging()) {
                this.bone4.xRot = 0.3F;
            }
        }
    }
}
