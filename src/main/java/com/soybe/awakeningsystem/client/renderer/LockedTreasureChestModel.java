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

        root.addOrReplaceChild("bottom", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 9.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lid = root.addOrReplaceChild("lid", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.0F, 0.0F, -14.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 7.0F));

        lid.addOrReplaceChild("latch", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("chains", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("padlock", CubeListBuilder.create()
                .texOffs(40, 44).addBox(-2.0F, 4.0F, -8.4F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 54).addBox(-1.5F, 8.8F, -8.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 54).addBox(0.5F, 8.8F, -8.4F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 54).addBox(-1.5F, 10.5F, -8.4F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
