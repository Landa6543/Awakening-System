package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoblinScoutEntity;
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

public class GoblinScoutModel extends HierarchicalModel<GoblinScoutEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "goblin_scout"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GoblinScoutModel(ModelPart root) {
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

        // Cabeza con nariz puntiaguda y orejas largas de duende
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                // Nariz alargada
                .texOffs(24, 0).addBox(-0.5F, -2.5F, -4.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        // Oreja derecha alargada
        head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(24, 4).addBox(-4.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 7).addBox(-5.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -3.0F, 0.5F, 0.0F, -0.35F, -0.2F));

        // Oreja izquierda alargada
        head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(24, 4).mirror().addBox(0.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 7).mirror().addBox(3.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -3.0F, 0.5F, 0.0F, 0.35F, 0.2F));

        // Cuerpo pequeño
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        // Brazo derecho con daga
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(20, 12).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 10.0F, 0.0F));

        // Daga rústica envenenada con guarda y filo serrado
        rightArm.addOrReplaceChild("dagger", CubeListBuilder.create()
                // Empuñadura envuelta
                .texOffs(28, 12).addBox(-1.5F, 5.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                // Guarda de hierro oxidado
                .texOffs(28, 17).addBox(-2.0F, 4.5F, -1.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                // Hoja aserrada y acanalada
                .texOffs(28, 20).addBox(-1.2F, 4.7F, -7.0F, 0.5F, 1.6F, 5.5F, new CubeDeformation(0.0F))
                // Punta con gancho de sangrado
                .texOffs(28, 27).addBox(-1.2F, 4.0F, -8.0F, 0.5F, 1.0F, 1.5F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brazo izquierdo
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(20, 12).mirror().addBox(0.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 10.0F, 0.0F));

        // Piernas cortas
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, 17.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 24).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 17.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(GoblinScoutEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.8F) * 1.2F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.8F + Mth.PI) * 1.2F * limbSwingAmount;

        this.rightArm.xRot = Mth.cos(limbSwing * 0.8F + Mth.PI) * 1.0F * limbSwingAmount - 0.2F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.8F) * 1.0F * limbSwingAmount;
    }
}
