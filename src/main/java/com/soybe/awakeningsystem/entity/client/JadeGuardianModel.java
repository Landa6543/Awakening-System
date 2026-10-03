package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JadeGuardianBossEntity;
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

public class JadeGuardianModel extends HierarchicalModel<JadeGuardianBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_guardian"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public JadeGuardianModel(ModelPart root) {
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

        // 1. Cabeza de Jade Ancestral con yelmo ceremonial (texOffs 0, 0)
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                // Cresta ceremonial dorada
                .texOffs(32, 0).addBox(-1.5F, -11.0F, -3.0F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        // 2. Torso colosal de jade y armadura de placas doradas (texOffs 0, 16)
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-7.0F, -10.0F, -4.5F, 14.0F, 14.0F, 9.0F, new CubeDeformation(0.0F))
                // Núcleo de Qi elemental en el pecho
                .texOffs(46, 16).addBox(-3.0F, -7.0F, -5.2F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 3. Brazo derecho colosal (Palma de Jade) (texOffs 0, 39)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-4.5F, -2.0F, -3.0F, 5.0F, 16.0F, 6.0F, new CubeDeformation(0.0F))
                // Hombrera acorazada
                .texOffs(24, 39).addBox(-5.5F, -3.5F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.5F, -8.0F, 0.0F));

        // 4. Brazo izquierdo colosal (texOffs 0, 39)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 39).mirror().addBox(-0.5F, -2.0F, -3.0F, 5.0F, 16.0F, 6.0F, new CubeDeformation(0.0F))
                // Hombrera acorazada izquierda
                .texOffs(24, 39).mirror().addBox(-0.5F, -3.5F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.5F, -8.0F, 0.0F));

        // 5. Pierna derecha robusta (texOffs 52, 39)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(52, 39).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 4.0F, 0.0F));

        // 6. Pierna izquierda robusta (texOffs 52, 39)
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(52, 39).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 4.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(JadeGuardianBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Marcha pesada de autómata de jade
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.5F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.5F + (float) Math.PI) * 1.0F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.5F + (float) Math.PI) * 0.8F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.5F) * 0.8F * limbSwingAmount;

        // Respiración / Pulso de Qi
        float breath = Mth.sin(ageInTicks * 0.08F) * 0.03F;
        this.body.yRot = breath;
    }
}
