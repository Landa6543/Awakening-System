package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
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

public class BeastmanWarriorModel extends HierarchicalModel<BeastmanWarriorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_warrior"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public BeastmanWarriorModel(ModelPart root) {
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

        // Cabeza con hocico de lobo/bestia y melena
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                // Hocico canino/felino
                .texOffs(32, 0).addBox(-2.5F, -4.0F, -7.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                // Mandíbula inferior y colmillos
                .texOffs(32, 8).addBox(-2.0F, -1.0F, -6.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                // Melena en la nuca
                .texOffs(46, 0).addBox(-3.5F, -7.5F, 3.5F, 7.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -1.0F));

        // Orejas de bestia puntiagudas
        head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -8.0F, 1.0F, 0.0F, 0.0F, -0.3F));

        head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(0, 0).mirror().addBox(-1.5F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -8.0F, 1.0F, 0.0F, 0.0F, 0.3F));

        // Torso musculoso y faldar nórdico de piel
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 13.0F, 6.0F, new CubeDeformation(0.2F))
                // Cuello y hombros de pelaje salvaje
                .texOffs(32, 16).addBox(-6.0F, -1.0F, -4.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.1F))
                // Faldar de combate
                .texOffs(0, 35).addBox(-5.5F, 11.0F, -3.5F, 11.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // Brazo derecho musculoso con gran hacha de guerra
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(36, 29).addBox(-3.5F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.5F, 0.0F, 0.0F));

        // Hacha colosal de caudillo nórdico con filo curvado, púa rompearmaduras y acero salvaje
        rightArm.addOrReplaceChild("battleaxe", CubeListBuilder.create()
                // Mango largo reforzado con envolturas de cuero sostenido en la mano
                .texOffs(56, 16).addBox(-1.9F, 9.5F, -12.0F, 1.8F, 1.8F, 26.0F, new CubeDeformation(0.0F))
                // Pomo pesado rompecráneos con púas
                .texOffs(86, 16).addBox(-2.4F, 9.0F, 13.5F, 2.8F, 2.8F, 2.0F, new CubeDeformation(0.0F))
                // Hoja frontal colosal curvada (barbada)
                .texOffs(56, 38).addBox(-1.9F, 2.0F, -17.0F, 1.8F, 17.0F, 8.0F, new CubeDeformation(0.0F))
                // Cuerno superior del filo
                .texOffs(76, 38).addBox(-1.8F, -1.0F, -19.0F, 1.6F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                // Gancho inferior de la barba
                .texOffs(84, 38).addBox(-1.8F, 18.0F, -15.0F, 1.6F, 3.5F, 4.0F, new CubeDeformation(0.0F))
                // Púa trasera rompearmaduras / pico de cuervo
                .texOffs(92, 38).addBox(-1.9F, 8.0F, -8.0F, 1.8F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(104, 38).addBox(-1.6F, 9.0F, -2.5F, 1.2F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo izquierdo con garras
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(36, 29).mirror().addBox(-1.5F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.5F, 0.0F, 0.0F));

        // Piernas musculosas
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.8F, 11.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 46).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.8F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(BeastmanWarriorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.65F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.65F + Mth.PI) * 1.0F * limbSwingAmount;

        this.rightArm.xRot = Mth.cos(limbSwing * 0.65F + Mth.PI) * 0.7F * limbSwingAmount - 0.4F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.65F) * 0.7F * limbSwingAmount;
    }
}
