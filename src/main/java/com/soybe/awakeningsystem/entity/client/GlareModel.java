package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GlareEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GlareModel extends HierarchicalModel<GlareEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "glare"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart topAzalea;
    private final ModelPart bottomAzalea;
    private final ModelPart secondLayer;
    private final ModelPart thirdLayer;
    private final ModelPart fourthLayer;
    private final ModelPart[] layers;

    public GlareModel(ModelPart root) {
        this.root = root;
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.eyes = this.head.getChild("eyes");
        this.topAzalea = this.head.getChild("topAzalea");
        this.bottomAzalea = this.head.getChild("bottomAzalea");
        this.secondLayer = this.bottomAzalea.getChild("secondLayer");
        this.thirdLayer = this.secondLayer.getChild("thirdLayer");
        this.fourthLayer = this.thirdLayer.getChild("fourthLayer");

        this.layers = new ModelPart[]{
            this.secondLayer,
            this.thirdLayer,
            this.fourthLayer
        };
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, 0.0F, -3.0F, 12.0F, 9.0F, 9.0F, new CubeDeformation(-0.02F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("eyes", CubeListBuilder.create()
                .texOffs(33, 0).addBox(2.0F, -1.0F, -0.3F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(33, 0).addBox(-4.0F, -1.0F, -0.3F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, 5.0F, -3.0F));

        head.addOrReplaceChild("topAzalea", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 14.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bottomAzalea = head.addOrReplaceChild("bottomAzalea", CubeListBuilder.create()
                .texOffs(18, 101).mirror().addBox(-7.0F, 0.75F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(-0.01F)).mirror(false)
                .texOffs(0, 40).addBox(-7.0F, -4.0F, -7.0F, 14.0F, 10.0F, 14.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition secondLayer = bottomAzalea.addOrReplaceChild("secondLayer", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 7.0F, 12.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition thirdLayer = secondLayer.addOrReplaceChild("thirdLayer", CubeListBuilder.create()
                .texOffs(0, 83).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        thirdLayer.addOrReplaceChild("fourthLayer", CubeListBuilder.create()
                .texOffs(0, 100).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(GlareEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Flotación suave y orgánica
        float bob = Mth.sin(ageInTicks * 0.12F) * 1.5F;
        this.body.y = 1.0F + bob;

        // Balanceo físico de las capas de follaje
        float movementForce = Mth.sin(limbSwing * 0.1F) * limbSwingAmount * 0.75F;
        float absMovement = Math.abs(movementForce);

        if (absMovement >= 0.001F) {
            this.head.xRot = (float) Math.toRadians(40 * absMovement);
            this.head.zRot = (float) Math.toRadians(15 * movementForce);

            for (ModelPart layer : this.layers) {
                layer.xRot = (float) Math.toRadians(30 * absMovement);
                layer.zRot = (float) Math.toRadians(15 * movementForce);
            }
        } else {
            this.head.xRot = (float) Math.toRadians(0.5F * Mth.sin(ageInTicks * 0.125F));
            this.head.zRot = (float) Math.toRadians(0.5F * Mth.cos(ageInTicks * 0.125F));

            for (ModelPart layer : this.layers) {
                layer.xRot = (float) Math.toRadians(0.75F * Mth.sin(ageInTicks * 0.1F));
                layer.zRot = (float) Math.toRadians(0.75F * Mth.cos(ageInTicks * 0.1F));
            }
        }

        // Orientación de la mirada
        this.head.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.5F;
        this.head.xRot += headPitch * ((float) Math.PI / 180F) * 0.5F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
