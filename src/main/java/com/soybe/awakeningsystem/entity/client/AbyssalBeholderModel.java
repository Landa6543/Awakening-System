package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalBeholderEntity;
import net.minecraft.client.model.EntityModel;
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

/**
 * Modelo 3D estilo Ojo de Cthulhu para el Ojo Vigilante del Abismo.
 * Esfera ocular gigante redondeada con córnea sobresaliente, nervio óptico posterior
 * y 6 tentáculos/nervios sangrientos que azotan en el aire durante el vuelo.
 */
public class AbyssalBeholderModel<T extends AbyssalBeholderEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_beholder"), "main");

    private final ModelPart root;
    private final ModelPart eyeball;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;
    private final ModelPart tentacle3;
    private final ModelPart tentacle4;
    private final ModelPart tentacle5;
    private final ModelPart tentacle6;

    public AbyssalBeholderModel(ModelPart root) {
        this.root = root;
        this.eyeball = root.getChild("eyeball");
        ModelPart nerveCluster = this.eyeball.getChild("nerve_cluster");
        this.tentacle1 = nerveCluster.getChild("tentacle_1");
        this.tentacle2 = nerveCluster.getChild("tentacle_2");
        this.tentacle3 = nerveCluster.getChild("tentacle_3");
        this.tentacle4 = nerveCluster.getChild("tentacle_4");
        this.tentacle5 = nerveCluster.getChild("tentacle_5");
        this.tentacle6 = nerveCluster.getChild("tentacle_6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Esfera principal del Ojo (16x16x16) con placas redondeadas
        PartDefinition eyeball = root.addOrReplaceChild("eyeball", CubeListBuilder.create()
                // Núcleo esférico 16x16x16
                .texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                // Córnea e iris frontal sobresaliente (12x12x2)
                .texOffs(66, 0).addBox(-6.0F, -6.0F, -9.5F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                // Curvatura superior esférica
                .texOffs(0, 34).addBox(-7.0F, -9.5F, -7.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                // Curvatura inferior esférica
                .texOffs(58, 34).addBox(-7.0F, 7.5F, -7.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                // Curvatura lateral izquierda
                .texOffs(0, 52).addBox(-9.5F, -7.0F, -7.0F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
                // Curvatura lateral derecha
                .texOffs(34, 52).addBox(7.5F, -7.0F, -7.0F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
                // Tronco del nervio óptico trasero
                .texOffs(68, 52).addBox(-4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 14.4F, 0.0F));

        // Racimo carnoso posterior que conecta los tentáculos
        PartDefinition nerveCluster = eyeball.addOrReplaceChild("nerve_cluster", CubeListBuilder.create()
                .texOffs(96, 52).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 13.0F));

        // 6 tentáculos / nervios sangrientos que flotan y azotan hacia atrás (+Z)
        nerveCluster.addOrReplaceChild("tentacle_1", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, -2.5F, 4.0F));

        nerveCluster.addOrReplaceChild("tentacle_2", CubeListBuilder.create()
                .texOffs(34, 82).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, -2.5F, 4.0F));

        nerveCluster.addOrReplaceChild("tentacle_3", CubeListBuilder.create()
                .texOffs(68, 82).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.2F, 0.0F, 4.0F));

        nerveCluster.addOrReplaceChild("tentacle_4", CubeListBuilder.create()
                .texOffs(0, 100).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.2F, 0.0F, 4.0F));

        nerveCluster.addOrReplaceChild("tentacle_5", CubeListBuilder.create()
                .texOffs(34, 100).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 2.5F, 4.0F));

        nerveCluster.addOrReplaceChild("tentacle_6", CubeListBuilder.create()
                .texOffs(68, 100).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 2.5F, 4.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        // Flotación en el aire con oscilación sutil centrada en la hitbox
        this.eyeball.y = 14.4F + Mth.sin(ageInTicks * 0.10F) * 0.8F;

        // Orientación 3D del ojo hacia la presa
        this.eyeball.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.eyeball.xRot = headPitch * ((float)Math.PI / 180F);

        // Azote ondulante de los 6 tentáculos traseros
        float waveSpeed = entity.isCharging() ? 0.45F : 0.20F;
        float ampX = entity.isCharging() ? 0.35F : 0.22F;
        float ampY = entity.isCharging() ? 0.30F : 0.18F;

        this.tentacle1.xRot = Mth.sin(ageInTicks * waveSpeed + 0.0F) * ampX;
        this.tentacle1.yRot = Mth.cos(ageInTicks * waveSpeed + 0.0F) * ampY;

        this.tentacle2.xRot = Mth.sin(ageInTicks * waveSpeed + 1.2F) * ampX;
        this.tentacle2.yRot = Mth.cos(ageInTicks * waveSpeed + 1.2F) * ampY;

        this.tentacle3.xRot = Mth.sin(ageInTicks * waveSpeed + 2.4F) * ampX;
        this.tentacle3.yRot = Mth.cos(ageInTicks * waveSpeed + 2.4F) * ampY;

        this.tentacle4.xRot = Mth.sin(ageInTicks * waveSpeed + 3.6F) * ampX;
        this.tentacle4.yRot = Mth.cos(ageInTicks * waveSpeed + 3.6F) * ampY;

        this.tentacle5.xRot = Mth.sin(ageInTicks * waveSpeed + 4.8F) * ampX;
        this.tentacle5.yRot = Mth.cos(ageInTicks * waveSpeed + 4.8F) * ampY;

        this.tentacle6.xRot = Mth.sin(ageInTicks * waveSpeed + 6.0F) * ampX;
        this.tentacle6.yRot = Mth.cos(ageInTicks * waveSpeed + 6.0F) * ampY;

        // Inclinación durante la embestida aérea o canalización
        if (entity.isCharging()) {
            this.eyeball.zRot = Mth.sin(ageInTicks * 0.8F) * 0.08F;
        } else if (entity.isChannelingBeam()) {
            float jitter = Mth.sin(ageInTicks * 2.0F) * 0.04F;
            this.eyeball.xRot += jitter;
            this.eyeball.yRot += jitter;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
