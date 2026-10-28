package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RuinScoutModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("awakening_system", "ruinscoutmodel"), "main");

    private final ModelPart root;
    private ModelPart bone5;
    private ModelPart bone;

    public RuinScoutModel(ModelPart root) {
        this.root = root;
        initParts(root);
    }

    private void initParts(ModelPart root) {
        try {
            this.bone5 = root.getChild("bone5");
        } catch (Exception ignored) {}
        try {
            this.bone = root.getChild("bone5").getChild("bone");
        } catch (Exception ignored) {}
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();

        PartDefinition part_bone5 = rootDef.addOrReplaceChild("bone5", CubeListBuilder.create()
            .texOffs(56, 0).addBox(-1.0F, -37.5F, -1.0F, 2.0F, 1.0F, 2.0F)
            .texOffs(0, 52).addBox(-5.5F, -35.5F, -5.5F, 11.0F, 1.0F, 11.0F)
            .texOffs(0, 34).addBox(-2.5F, -36.5F, -2.5F, 5.0F, 1.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 23.5F, -0.5F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_bone4 = part_bone5.addOrReplaceChild("bone4", CubeListBuilder.create(),
            PartPose.offsetAndRotation(9.6286F, -34.0939F, 0.005F, 0.0F, 0.0F, 0.3927F));

        PartDefinition part_cube_r1 = part_bone4.addOrReplaceChild("cube_r1", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition part_bone3 = part_bone5.addOrReplaceChild("bone3", CubeListBuilder.create(),
            PartPose.offsetAndRotation(-9.6185F, -34.0939F, 0.005F, 0.0F, 0.0F, -0.3927F));

        PartDefinition part_cube_r2 = part_bone3.addOrReplaceChild("cube_r2", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        PartDefinition part_bone2 = part_bone5.addOrReplaceChild("bone2", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, -34.0939F, 9.6236F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_cube_r3 = part_bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create()
            .texOffs(0, 0).mirror().addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition part_bone = part_bone5.addOrReplaceChild("bone", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, -34.0939F, -9.6236F, 0.3927F, 0.0F, 0.0F));

        PartDefinition part_cube_r4 = part_bone.addOrReplaceChild("cube_r4", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        PartDefinition part_tentacles = part_bone5.addOrReplaceChild("tentacles", CubeListBuilder.create()
            .texOffs(52, 41).mirror().addBox(-0.6342F, 0.0F, -1.5F, 3.0F, 20.0F, 3.0F),
            PartPose.offsetAndRotation(-4.3658F, -34.5F, 0.0F, 0.0F, 0.0F, 0.1309F));

        PartDefinition part_cube_r5 = part_tentacles.addOrReplaceChild("cube_r5", CubeListBuilder.create()
            .texOffs(52, 19).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.8658F, 19.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition part_tentacles2 = part_bone5.addOrReplaceChild("tentacles2", CubeListBuilder.create()
            .texOffs(52, 41).addBox(-2.3658F, 0.0F, -1.5F, 3.0F, 20.0F, 3.0F),
            PartPose.offsetAndRotation(4.3658F, -34.5F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition part_cube_r6 = part_tentacles2.addOrReplaceChild("cube_r6", CubeListBuilder.create()
            .texOffs(52, 19).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(-0.8658F, 19.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition part_tentacles3 = part_bone5.addOrReplaceChild("tentacles3", CubeListBuilder.create()
            .texOffs(52, 41).addBox(-1.5F, 0.0F, -0.6283F, 3.0F, 20.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -34.5F, -4.3717F, -0.1309F, 0.0F, 0.0F));

        PartDefinition part_cube_r7 = part_tentacles3.addOrReplaceChild("cube_r7", CubeListBuilder.create()
            .texOffs(52, 19).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.5F, 19.0F, 0.3717F, -0.2182F, 0.0F, 0.0F));

        PartDefinition part_tentacles4 = part_bone5.addOrReplaceChild("tentacles4", CubeListBuilder.create()
            .texOffs(52, 41).mirror().addBox(-1.5F, 0.0F, -2.3598F, 3.0F, 20.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -34.5F, 4.3598F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_cube_r8 = part_tentacles4.addOrReplaceChild("cube_r8", CubeListBuilder.create()
            .texOffs(52, 19).mirror().addBox(-2.0F, 0.0F, -1.0F, 3.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(0.5F, 19.0F, -1.3598F, 0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.bone5 != null) {
            this.bone5.yRot = netHeadYaw * ((float)Math.PI / 180F);
            this.bone5.y = 23.5F + Mth.sin(ageInTicks * 0.1F) * 1.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
