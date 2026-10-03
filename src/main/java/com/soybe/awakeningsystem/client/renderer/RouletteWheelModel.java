package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RouletteWheelModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "roulette_wheel"), "main");

    public final ModelPart stand;
    public final ModelPart pointer;
    public final ModelPart wheel;

    public RouletteWheelModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.stand = root.getChild("stand");
        this.pointer = root.getChild("pointer");
        this.wheel = root.getChild("wheel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. SOPORTE DE CABALLETE ROBUSTO Y BASEPLATE (Asentado firmemente en el suelo Y=0)
        PartDefinition stand = root.addOrReplaceChild("stand", CubeListBuilder.create()
                // Peana izquierda biselada y extendida en el suelo (4x2x8, Z: -6 a +2)
                .texOffs(0, 0).addBox(-13.0F, 0.0F, -6.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-12.5F, 0.0F, 2.0F, 3.0F, 1.2F, 1.5F, new CubeDeformation(0.0F))

                // Peana derecha biselada y extendida en el suelo (4x2x8, Z: -6 a +2)
                .texOffs(0, 0).addBox(9.0F, 0.0F, -6.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(9.5F, 0.0F, 2.0F, 3.0F, 1.2F, 1.5F, new CubeDeformation(0.0F))

                // Plinto / Travesaño inferior a ras de suelo que une ambas patas (20x2x4, Z: -3.5 a +0.5)
                .texOffs(64, 0).addBox(-10.0F, 0.0F, -3.5F, 20.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                // Placa frontal dorada / Ranura decorativa de fichas sobre el plinto
                .texOffs(64, 0).addBox(-6.0F, 0.2F, 0.5F, 12.0F, 1.6F, 0.5F, new CubeDeformation(0.0F))

                // Travesaño intermedio de arriostramiento (18x2x2, Y: 2.0 a 4.0)
                .texOffs(64, 0).addBox(-9.0F, 2.0F, -3.0F, 18.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                // Medallón / Escudo dorado central
                .texOffs(64, 0).addBox(-2.5F, 1.5F, -1.0F, 5.0F, 3.0F, 0.8F, new CubeDeformation(0.0F))

                // Columna / Montante izquierdo en ángulo que asciende hacia el eje (detrás de la rueda, Z: -3.5)
                .texOffs(32, 0).addBox(-12.0F, 2.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-10.0F, 8.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-8.0F, 14.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-5.5F, 18.0F, -3.5F, 2.5F, 4.0F, 2.5F, new CubeDeformation(0.0F))

                // Columna / Montante derecho en ángulo hacia el eje
                .texOffs(32, 0).addBox(9.5F, 2.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(7.5F, 8.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(5.5F, 14.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(3.0F, 18.0F, -3.5F, 2.5F, 4.0F, 2.5F, new CubeDeformation(0.0F))

                // Caja de rodamientos posterior cilíndrica/octogonal abrazando el eje desde atrás (8x4x4.5)
                .texOffs(0, 32).addBox(-4.0F, 18.0F, -6.0F, 8.0F, 4.0F, 4.5F, new CubeDeformation(0.0F))
                // Eje central de acero que pasa hacia adelante
                .texOffs(0, 32).addBox(-1.0F, 19.0F, -1.5F, 2.0F, 2.0F, 2.2F, new CubeDeformation(0.0F))

                // Arco superior en voladizo para sostener la aguja desde atrás
                // Columna vertical posterior (3x15x2)
                .texOffs(48, 32).addBox(-1.5F, 22.0F, -5.5F, 3.0F, 15.0F, 2.0F, new CubeDeformation(0.0F))
                // Brazo voladizo horizontal superior que pasa por encima de la rueda (3x2x7)
                .texOffs(48, 32).addBox(-1.5F, 37.0F, -5.5F, 3.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                // Nudillo de anclaje de la aguja en el frente superior
                .texOffs(48, 32).addBox(-1.0F, 36.0F, 0.8F, 2.0F, 2.0F, 1.2F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 2. AGUJA / FLAPPER MECÁNICO SUPERIOR (Pivota sobre el arco a las 12 en punto en Z: 1.6)
        root.addOrReplaceChild("pointer", CubeListBuilder.create()
                // Perno de bisagra
                .texOffs(0, 64).addBox(-1.2F, -0.6F, -0.6F, 2.4F, 1.2F, 1.2F, new CubeDeformation(0.0F))
                // Pala triangular de rubí con borde dorado
                .texOffs(0, 64).addBox(-1.0F, 0.0F, -0.4F, 2.0F, 2.6F, 0.8F, new CubeDeformation(0.0F))
                // Punta afilada indicadora que apunta a las 16 clavijas de la rueda
                .texOffs(0, 64).addBox(-0.5F, 2.6F, -0.4F, 1.0F, 1.2F, 0.8F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 36.8F, 1.6F));

        // 3. RUEDA VERTICAL 3D (Buje compacto y 16 clavijas físicas de latón en radio 15.2 unidades)
        CubeListBuilder wheelBuilder = CubeListBuilder.create()
                // Buje base de latón (5x5x0.8)
                .texOffs(32, 64).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 0.8F, new CubeDeformation(0.0F))
                // Cúpula central dorada en flor de loto (3x3x1.0)
                .texOffs(32, 64).addBox(-1.5F, -1.5F, 0.8F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                // Remate central cónico del eje (1.6x1.6x0.8)
                .texOffs(32, 64).addBox(-0.8F, -0.8F, 1.8F, 1.6F, 1.6F, 0.8F, new CubeDeformation(0.0F));

        // 16 Clavijas físicas 3D de latón en el perímetro exterior (radio 15.2 unidades)
        for (int i = 0; i < 16; i++) {
            double rad = Math.toRadians(i * 22.5);
            float px = (float) (Math.sin(rad) * 15.2);
            float py = (float) (-Math.cos(rad) * 15.2);
            wheelBuilder.texOffs(64, 64).addBox(px - 0.6F, py - 0.6F, 0.1F, 1.2F, 1.2F, 1.2F, new CubeDeformation(0.0F));
        }

        root.addOrReplaceChild("wheel", wheelBuilder, PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        stand.render(poseStack, buffer, packedLight, packedOverlay, color);
        pointer.render(poseStack, buffer, packedLight, packedOverlay, color);
        wheel.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
