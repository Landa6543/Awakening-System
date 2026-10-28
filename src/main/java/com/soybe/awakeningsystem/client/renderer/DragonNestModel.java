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

/**
 * Modelo 3D del Nido de Dragón Ancestral.
 * Diseñado como una gran estructura cóncava de ramas carbonizadas,
 * troncos petrificados y un lecho interior de paja y ceniza.
 */
public class DragonNestModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "dragon_nest"), "main");

    public final ModelPart root;
    public final ModelPart base;
    public final ModelPart strawCenter;
    public final ModelPart rimNorth;
    public final ModelPart rimSouth;
    public final ModelPart rimEast;
    public final ModelPart rimWest;
    public final ModelPart rimCornerNW;
    public final ModelPart rimCornerNE;
    public final ModelPart rimCornerSW;
    public final ModelPart rimCornerSE;

    public DragonNestModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.base = root.getChild("base");
        this.strawCenter = root.getChild("straw_center");
        this.rimNorth = root.getChild("rim_north");
        this.rimSouth = root.getChild("rim_south");
        this.rimEast = root.getChild("rim_east");
        this.rimWest = root.getChild("rim_west");
        this.rimCornerNW = root.getChild("rim_corner_nw");
        this.rimCornerNE = root.getChild("rim_corner_ne");
        this.rimCornerSW = root.getChild("rim_corner_sw");
        this.rimCornerSE = root.getChild("rim_corner_se");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        part.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.0F, 0.0F, -12.0F, 24.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        part.addOrReplaceChild("straw_center", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        part.addOrReplaceChild("rim_north", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-11.0F, -4.0F, -14.0F, 22.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        part.addOrReplaceChild("rim_south", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-11.0F, -4.0F, 10.0F, 22.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        part.addOrReplaceChild("rim_east", CubeListBuilder.create()
                .texOffs(54, 48).addBox(10.0F, -4.0F, -10.0F, 4.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        part.addOrReplaceChild("rim_west", CubeListBuilder.create()
                .texOffs(54, 48).addBox(-14.0F, -4.0F, -10.0F, 4.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        part.addOrReplaceChild("rim_corner_nw", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-4.0F, -4.5F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-9.5F, 22.0F, -9.5F, 0.0F, 0.7854F, 0.0F));

        part.addOrReplaceChild("rim_corner_ne", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-4.0F, -4.5F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(9.5F, 22.0F, -9.5F, 0.0F, -0.7854F, 0.0F));

        part.addOrReplaceChild("rim_corner_sw", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-4.0F, -4.5F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-9.5F, 22.0F, 9.5F, 0.0F, -0.7854F, 0.0F));

        part.addOrReplaceChild("rim_corner_se", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-4.0F, -4.5F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(9.5F, 22.0F, 9.5F, 0.0F, 0.7854F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
