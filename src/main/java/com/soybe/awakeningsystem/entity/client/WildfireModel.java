package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WildfireEntity;
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

public class WildfireModel extends HierarchicalModel<WildfireEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wildfire"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart helmet;
    private final ModelPart shields;
    private final ModelPart frontShield;
    private final ModelPart rightShield;
    private final ModelPart backShield;
    private final ModelPart leftShield;

    public WildfireModel(ModelPart root) {
        this.root = root;
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.helmet = this.head.getChild("helmet");
        this.shields = this.root.getChild("shields");
        this.frontShield = this.shields.getChild("frontShield");
        this.rightShield = this.shields.getChild("rightShield");
        this.backShield = this.shields.getChild("backShield");
        this.leftShield = this.shields.getChild("leftShield");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -21.0F, -2.0F, 4.0F, 21.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 26).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -24.0F, 0.0F));

        head.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(0, 43).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition shields = root.addOrReplaceChild("shields",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        shields.addOrReplaceChild("frontShield",
                CubeListBuilder.create().texOffs(17, 0).addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

        shields.addOrReplaceChild("rightShield",
                CubeListBuilder.create().texOffs(17, 0).addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, 1.5708F, 0.0F));

        shields.addOrReplaceChild("backShield",
                CubeListBuilder.create().texOffs(17, 0).addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, 3.1416F, 0.0F));

        shields.addOrReplaceChild("leftShield",
                CubeListBuilder.create().texOffs(17, 0).addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, -1.5708F, 0.0F));

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(WildfireEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Mirada de la cabeza
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Flotación sutil
        float hover = Mth.sin(ageInTicks * 0.12F) * 2.5F;
        this.body.y = 24.0F + hover;
        this.shields.y = 24.0F + hover;

        // Rotación continua de los escudos flotantes
        this.shields.yRot = ageInTicks * 0.065F;

        // Visibilidad según la cantidad de escudos intactos
        int activeShields = entity.getActiveShields();
        this.frontShield.visible = activeShields >= 1;
        this.rightShield.visible = activeShields >= 2;
        this.backShield.visible = activeShields >= 3;
        this.leftShield.visible = activeShields >= 4;

        // Si está ejecutando la onda de choque, los escudos se expanden hacia afuera
        if (entity.isPerformingShockwave()) {
            float progress = entity.getShockwaveProgress();
            float expansion = Mth.sin(progress * (float) Math.PI) * 4.0F;
            this.frontShield.z = -expansion;
            this.backShield.z = expansion;
            this.rightShield.x = expansion;
            this.leftShield.x = -expansion;
        }
    }
}
