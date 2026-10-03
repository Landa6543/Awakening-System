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

public class LockedTreasureChestModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "locked_treasure_chest"), "main");

    public final ModelPart bottom;
    public final ModelPart lid;
    public final ModelPart latch;
    public final ModelPart chains;
    public final ModelPart padlock;

    public LockedTreasureChestModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.latch = this.lid.getChild("latch");
        this.chains = root.getChild("chains");
        this.padlock = root.getChild("padlock");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Base maciza del cofre (14x10x14, centrada, de Y=0 a Y=10)
        root.addOrReplaceChild("bottom", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 2. Tapa abisagrada en la parte trasera superior (Pivote Y=9, Z=7)
        PartDefinition lid = root.addOrReplaceChild("lid", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.0F, 0.0F, -14.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 7.0F));

        // 3. Pasador / Broche de cierre frontal montado rígidamente en la tapa
        lid.addOrReplaceChild("latch", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 4. Cadenas de Hierro 3D que envuelven el cofre (horizontal y verticalmente)
        root.addOrReplaceChild("chains", CubeListBuilder.create()
                .texOffs(0, 44)
                .addBox(-7.2F, 4.0F, -7.2F, 14.4F, 2.0F, 0.4F, new CubeDeformation(0.0F))
                .addBox(-7.2F, 4.0F, 6.8F, 14.4F, 2.0F, 0.4F, new CubeDeformation(0.0F))
                .addBox(-7.2F, 4.0F, -6.8F, 0.4F, 2.0F, 13.6F, new CubeDeformation(0.0F))
                .addBox(6.8F, 4.0F, -6.8F, 0.4F, 2.0F, 13.6F, new CubeDeformation(0.0F))
                .addBox(-1.5F, 0.0F, -7.2F, 3.0F, 10.0F, 0.4F, new CubeDeformation(0.0F))
                .addBox(-1.5F, 9.5F, -7.2F, 3.0F, 4.7F, 0.4F, new CubeDeformation(0.0F))
                .addBox(-1.5F, 14.1F, -7.0F, 3.0F, 0.3F, 14.0F, new CubeDeformation(0.0F))
                .addBox(-1.5F, 0.0F, 6.8F, 3.0F, 14.0F, 0.4F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 5. Candado Dorado 3D Prominente con Grillete de Acero en el frontal
        root.addOrReplaceChild("padlock", CubeListBuilder.create()
                // Cuerpo del candado (4x5x2) que sobresale físicamente del frontal
                .texOffs(40, 44).addBox(-2.0F, 4.0F, -9.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                // Grillete / Arco de acero en 3D
                .texOffs(40, 54).addBox(-1.5F, 9.0F, -8.6F, 0.8F, 2.4F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(40, 54).addBox(0.7F, 9.0F, -8.6F, 0.8F, 2.4F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(40, 54).addBox(-1.5F, 11.2F, -8.6F, 3.0F, 0.8F, 0.8F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
