package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbstractAbyssMageEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo 3D con barrera elemental activa para el Mago del Abismo.
 * Posee textura de resolución 64x128 (128x256 píxeles) y geometría perimetral del escudo.
 */
public class AbyssMageShieldedModel<T extends AbstractAbyssMageEntity> extends AbyssMageModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_mage_shielded"), "main");

    private final ModelPart shield;

    public AbyssMageShieldedModel(ModelPart root) {
        super(root);
        this.shield = root.getChild("shield");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addBodyParts(root);

        root.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(18, 73).addBox(11.0F, -40.0F, -8.0F, 2.0F, 40.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(-13.0F, -40.0F, -8.0F, 2.0F, 40.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(-11.0F, -38.0F, -7.5F, 2.0F, 36.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(9.0F, -38.0F, -7.5F, 2.0F, 36.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(9.0F, -38.0F, 8.5F, 2.0F, 36.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(-11.0F, -38.0F, 8.5F, 2.0F, 36.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(-12.5F, -38.0F, -6.0F, 1.0F, 36.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(-12.5F, -38.0F, 6.0F, 1.0F, 36.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(11.5F, -38.0F, 6.0F, 1.0F, 36.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 71).addBox(11.5F, -38.0F, -6.0F, 1.0F, 36.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 71).addBox(-13.0F, -40.0F, 8.0F, 2.0F, 40.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).addBox(11.0F, -40.0F, 8.0F, 2.0F, 40.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 88).addBox(11.0F, -40.0F, -6.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(17, 88).addBox(-13.0F, -40.0F, -6.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(17, 88).addBox(-13.0F, -2.0F, -6.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(17, 88).addBox(11.0F, -2.0F, -6.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(16, 94).addBox(-11.0F, -40.0F, 8.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 93).addBox(-11.0F, -40.0F, -8.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 93).addBox(-11.0F, -2.0F, -8.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 93).addBox(-11.0F, -2.0F, 8.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (this.shield != null) {
            this.shield.yRot = ageInTicks * 0.02F;
        }
    }
}
