package com.soybe.awakeningsystem.item.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ElementalDemonCrownModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "elemental_demon_crown"), "main");

    public ElementalDemonCrownModel(ModelPart root) {
        super(root);
        this.hat.visible = false;
        this.body.visible = false;
        this.rightArm.visible = false;
        this.leftArm.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0F), 0.0F);
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("crown_ring", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -9.5F, -4.5F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.1F))
                .texOffs(36, 0).addBox(-1.5F, -12.5F, -4.8F, 3.0F, 3.0F, 1.0F)
                .texOffs(44, 0).addBox(-4.8F, -11.5F, -1.5F, 1.0F, 2.0F, 3.0F)
                .texOffs(44, 0).mirror().addBox(3.8F, -11.5F, -1.5F, 1.0F, 2.0F, 3.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-6.0F, -11.0F, -1.0F, 3.0F, 4.0F, 3.0F)
                .texOffs(12, 16).addBox(-8.0F, -15.0F, 1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(20, 16).addBox(-9.0F, -18.0F, 3.0F, 2.0F, 3.0F, 2.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(0, 32).addBox(3.0F, -11.0F, -1.0F, 3.0F, 4.0F, 3.0F)
                .texOffs(12, 32).addBox(6.0F, -15.0F, 1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(20, 32).addBox(7.0F, -18.0F, 3.0F, 2.0F, 3.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }
}
