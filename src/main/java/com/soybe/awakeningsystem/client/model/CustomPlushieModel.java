package com.soybe.awakeningsystem.client.model;

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

public class CustomPlushieModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "custom_player_plushie"), "main");

    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart jacket;
    public final ModelPart rightArm;
    public final ModelPart rightSleeve;
    public final ModelPart leftArm;
    public final ModelPart leftSleeve;
    public final ModelPart rightLeg;
    public final ModelPart rightPants;
    public final ModelPart leftLeg;
    public final ModelPart leftPants;

    public CustomPlushieModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.jacket = root.getChild("jacket");
        this.rightArm = root.getChild("right_arm");
        this.rightSleeve = root.getChild("right_sleeve");
        this.leftArm = root.getChild("left_arm");
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightLeg = root.getChild("right_leg");
        this.rightPants = root.getChild("right_pants");
        this.leftLeg = root.getChild("left_leg");
        this.leftPants = root.getChild("left_pants");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.35F)),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, -7.0F, -2.0F, 8.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("jacket", CubeListBuilder.create()
                .texOffs(16, 32).addBox(-4.0F, -7.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(-4.3F, -6.5F, 0.0F, -0.45F, 0.15F, 0.15F));

        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(-4.3F, -6.5F, 0.0F, -0.45F, 0.15F, 0.15F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(4.3F, -6.5F, 0.0F, -0.45F, -0.15F, -0.15F));

        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                .texOffs(48, 48).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(4.3F, -6.5F, 0.0F, -0.45F, -0.15F, -0.15F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-1.7F, 0.0F, -1.7F, 3.4F, 5.5F, 3.4F),
                PartPose.offsetAndRotation(-2.0F, -1.6F, -1.0F, -1.45F, 0.06F, 0.0F));

        root.addOrReplaceChild("right_pants", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1.7F, 0.0F, -1.7F, 3.4F, 5.5F, 3.4F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(-2.0F, -1.6F, -1.0F, -1.45F, 0.06F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-1.7F, 0.0F, -1.7F, 3.4F, 5.5F, 3.4F),
                PartPose.offsetAndRotation(2.0F, -1.6F, -1.0F, -1.45F, -0.06F, 0.0F));

        root.addOrReplaceChild("left_pants", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-1.7F, 0.0F, -1.7F, 3.4F, 5.5F, 3.4F, new CubeDeformation(0.2F)),
                PartPose.offsetAndRotation(2.0F, -1.6F, -1.0F, -1.45F, -0.06F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.hat.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jacket.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightArm.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightSleeve.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftArm.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftSleeve.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightPants.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftPants.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
