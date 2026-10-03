package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.YggdrasilTreantBossEntity;
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

public class YggdrasilTreantModel extends HierarchicalModel<YggdrasilTreantBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "yggdrasil_treant"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public YggdrasilTreantModel(ModelPart root) {
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

        // Cabeza de corteza antigua y corona de hojas (texOffs 0, 0)
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                // Corona de follaje y hojas sobresalientes (texOffs 40, 0)
                .texOffs(40, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Cuernos / Ramas superiores de la cabeza
        head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(85, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, -9.0F, 0.0F, 0.0F, 0.0F, -0.35F));

        head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, -9.0F, 0.0F, 0.0F, 0.0F, 0.35F));

        // Tronco macizo de Yggdrasil (texOffs 0, 20)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                // Capa exterior de musgo y líquenes (texOffs 40, 20)
                .texOffs(40, 20).addBox(-5.5F, 0.0F, -3.5F, 11.0F, 14.0F, 7.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo derecho de rama retorcida (texOffs 0, 45)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.0F, 2.0F, 0.0F));

        // Brazo izquierdo de rama retorcida (texOffs 24, 45)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(24, 45).addBox(-2.0F, -2.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.0F, 2.0F, 0.0F));

        // Pata raíz derecha (texOffs 48, 45)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(48, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 14.0F, 0.0F));

        // Pata raíz izquierda (texOffs 72, 45)
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(72, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(YggdrasilTreantBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Movimiento de cabeza
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        // Respiración suave del tronco
        float breath = Mth.sin(ageInTicks * 0.06F) * 0.03F;
        this.body.xRot = breath;
        this.head.y = breath * 2.0F;

        // Marcha de raíces pesadas
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.5F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.5F + Mth.PI) * 1.0F * limbSwingAmount;

        // Balanceo de brazos de madera
        this.rightArm.xRot = Mth.cos(limbSwing * 0.5F + Mth.PI) * 0.8F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.5F) * 0.8F * limbSwingAmount;
    }
}
