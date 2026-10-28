package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BloodQiTotemEntity;
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

public class BloodQiTotemModel extends HierarchicalModel<BloodQiTotemEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "blood_qi_totem"), "main");

    private final ModelPart root;
    private final ModelPart pillar;
    private final ModelPart core;

    public BloodQiTotemModel(ModelPart root) {
        this.root = root;
        this.pillar = root.getChild("pillar");
        this.core = root.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("pillar", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(32, 12).addBox(-5.0F, 10.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(BloodQiTotemEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.pillar.yRot = ageInTicks * 0.04F;
        this.pillar.y = 12.0F + Mth.sin(ageInTicks * 0.1F) * 2.0F;

        this.core.yRot = -ageInTicks * 0.08F;
        this.core.xRot = ageInTicks * 0.04F;
        this.core.y = this.pillar.y;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
