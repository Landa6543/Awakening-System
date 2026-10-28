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

        PartDefinition stand = root.addOrReplaceChild("stand", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13.0F, 0.0F, -6.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-12.5F, 0.0F, 2.0F, 3.0F, 1.2F, 1.5F, new CubeDeformation(0.0F))

                .texOffs(0, 0).addBox(9.0F, 0.0F, -6.0F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(9.5F, 0.0F, 2.0F, 3.0F, 1.2F, 1.5F, new CubeDeformation(0.0F))

                .texOffs(64, 0).addBox(-10.0F, 0.0F, -3.5F, 20.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-6.0F, 0.2F, 0.5F, 12.0F, 1.6F, 0.5F, new CubeDeformation(0.0F))

                .texOffs(64, 0).addBox(-9.0F, 2.0F, -3.0F, 18.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-2.5F, 1.5F, -1.0F, 5.0F, 3.0F, 0.8F, new CubeDeformation(0.0F))

                .texOffs(32, 0).addBox(-12.0F, 2.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-10.0F, 8.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-8.0F, 14.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-5.5F, 18.0F, -3.5F, 2.5F, 4.0F, 2.5F, new CubeDeformation(0.0F))

                .texOffs(32, 0).addBox(9.5F, 2.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(7.5F, 8.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(5.5F, 14.0F, -3.5F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(3.0F, 18.0F, -3.5F, 2.5F, 4.0F, 2.5F, new CubeDeformation(0.0F))

                .texOffs(0, 32).addBox(-4.0F, 18.0F, -6.0F, 8.0F, 4.0F, 4.5F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-1.0F, 19.0F, -1.5F, 2.0F, 2.0F, 2.2F, new CubeDeformation(0.0F))

                .texOffs(48, 32).addBox(-1.5F, 22.0F, -5.5F, 3.0F, 15.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 32).addBox(-1.5F, 37.0F, -5.5F, 3.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(48, 32).addBox(-1.0F, 36.0F, 0.8F, 2.0F, 2.0F, 1.2F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("pointer", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-1.2F, -0.6F, -0.6F, 2.4F, 1.2F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-1.0F, 0.0F, -0.4F, 2.0F, 2.6F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-0.5F, 2.6F, -0.4F, 1.0F, 1.2F, 0.8F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 36.8F, 1.6F));

        CubeListBuilder wheelBuilder = CubeListBuilder.create()
                .texOffs(32, 64).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(32, 64).addBox(-1.5F, -1.5F, 0.8F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 64).addBox(-0.8F, -0.8F, 1.8F, 1.6F, 1.6F, 0.8F, new CubeDeformation(0.0F));

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
