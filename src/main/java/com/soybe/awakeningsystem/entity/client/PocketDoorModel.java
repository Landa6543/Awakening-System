package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.PocketDoorEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo 3D de la Puerta Física de Bolsillo (Sección 29).
 * Incluye marco de madera noble reforzado, umbral inferior, dintel superior
 * y hoja de puerta batiente articulada sobre bisagras con picaporte metálico 3D.
 */
public class PocketDoorModel extends HierarchicalModel<PocketDoorEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pocket_door"), "main");

    private final ModelPart root;
    private final ModelPart frame;
    private final ModelPart door;

    public PocketDoorModel(ModelPart root) {
        this.root = root;
        this.frame = root.getChild("frame");
        this.door = root.getChild("door");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Marco exterior fijo (dintel, postes laterales y umbral)
        PartDefinition frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
                // Poste izquierdo
                .texOffs(0, 0).addBox(-8.0F, -36.0F, -2.0F, 2.0F, 36.0F, 4.0F, new CubeDeformation(0.0F))
                // Poste derecho
                .texOffs(12, 0).addBox(6.0F, -36.0F, -2.0F, 2.0F, 36.0F, 4.0F, new CubeDeformation(0.0F))
                // Dintel superior
                .texOffs(0, 40).addBox(-8.0F, -38.0F, -2.5F, 16.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                // Umbral inferior
                .texOffs(0, 47).addBox(-9.0F, -1.0F, -3.0F, 18.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // Hoja batiente de la puerta (pivota en X = -6.0F sobre las bisagras)
        PartDefinition door = root.addOrReplaceChild("door", CubeListBuilder.create()
                // Panel de madera principal
                .texOffs(24, 0).addBox(0.0F, -36.0F, -1.0F, 12.0F, 35.0F, 2.0F, new CubeDeformation(0.0F))
                // Picaporte / Manija metálica dorada (ambos lados)
                .texOffs(0, 54).addBox(9.5F, -19.0F, -2.2F, 1.5F, 3.0F, 4.4F, new CubeDeformation(0.0F))
                // Bisagras de hierro decorativas
                .texOffs(14, 54).addBox(-0.5F, -32.0F, -1.3F, 1.5F, 3.0F, 2.6F, new CubeDeformation(0.0F))
                .texOffs(14, 54).addBox(-0.5F, -8.0F, -1.3F, 1.5F, 3.0F, 2.6F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(PocketDoorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // La animación de apertura se controla directamente en el renderer mediante setDoorAngle
    }

    public void setDoorAngle(float degrees) {
        this.door.yRot = -(float) Math.toRadians(degrees);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.frame.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.door.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
