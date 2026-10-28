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

public class RuinCruiserModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("awakening_system", "ruincruisermodel"), "main");

    private final ModelPart root;
    private ModelPart bone;
    private ModelPart bone5;

    public RuinCruiserModel(ModelPart root) {
        this.root = root;
        initParts(root);
    }

    private void initParts(ModelPart root) {
        try {
            this.bone = root.getChild("bone");
        } catch (Exception ignored) {}
        try {
            this.bone5 = root.getChild("bone5");
        } catch (Exception ignored) {}
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();

        PartDefinition part_bone = rootDef.addOrReplaceChild("bone", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, 7.0F, -4.5F, -0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r1 = part_bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
            .texOffs(0, 0).mirror().addBox(-5.0F, -7.0F, -1.5F, 12.0F, 12.0F, 3.0F),
            PartPose.offsetAndRotation(7.9641F, -5.7942F, 0.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition part_cube_r2 = part_bone.addOrReplaceChild("cube_r2", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-7.0F, -7.0F, -1.5F, 12.0F, 12.0F, 3.0F),
            PartPose.offsetAndRotation(-7.9641F, -5.7942F, 0.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition part_bone2 = rootDef.addOrReplaceChild("bone2", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, 8.0F, -4.5F, 0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r3 = part_bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6.0F, -6.0F, -1.5F, 12.0F, 12.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 0.0F, -2.3562F));

        PartDefinition part_bone3 = rootDef.addOrReplaceChild("bone3", CubeListBuilder.create()
            .texOffs(33, 21).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F)
            .texOffs(14, 56).addBox(-1.5F, 2.5F, -0.5F, 3.0F, 2.0F, 1.0F)
            .texOffs(18, 38).addBox(-2.5F, 4.5F, -0.5F, 5.0F, 1.0F, 1.0F)
            .texOffs(0, 25).addBox(-3.5F, 5.5F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.5F, -11.5F, 4.5F, -0.0873F, 0.0F, 1.0472F));

        PartDefinition part_cube_r4 = part_bone3.addOrReplaceChild("cube_r4", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -6.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r5 = part_bone3.addOrReplaceChild("cube_r5", CubeListBuilder.create()
            .texOffs(18, 38).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r6 = part_bone3.addOrReplaceChild("cube_r6", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-2.0F, 9.0F, -0.5F, 7.0F, 2.0F, 1.0F)
            .texOffs(18, 38).addBox(-1.0F, 8.0F, -0.5F, 5.0F, 1.0F, 1.0F)
            .texOffs(14, 56).addBox(0.0F, 6.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(4.0F, -1.5F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition part_cube_r7 = part_bone3.addOrReplaceChild("cube_r7", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r8 = part_bone3.addOrReplaceChild("cube_r8", CubeListBuilder.create()
            .texOffs(18, 38).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(4.5F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r9 = part_bone3.addOrReplaceChild("cube_r9", CubeListBuilder.create()
            .texOffs(14, 56).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r10 = part_bone3.addOrReplaceChild("cube_r10", CubeListBuilder.create()
            .texOffs(14, 56).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_bone4 = rootDef.addOrReplaceChild("bone4", CubeListBuilder.create()
            .texOffs(33, 21).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F)
            .texOffs(14, 56).addBox(-1.5F, 2.5F, -0.5F, 3.0F, 2.0F, 1.0F)
            .texOffs(18, 38).addBox(-2.5F, 4.5F, -0.5F, 5.0F, 1.0F, 1.0F)
            .texOffs(0, 25).addBox(-3.5F, 5.5F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(-20.5F, 12.5F, 4.5F, 0.0F, 0.8727F, -0.5236F));

        PartDefinition part_cube_r11 = part_bone4.addOrReplaceChild("cube_r11", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -6.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r12 = part_bone4.addOrReplaceChild("cube_r12", CubeListBuilder.create()
            .texOffs(18, 38).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r13 = part_bone4.addOrReplaceChild("cube_r13", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-2.0F, 9.0F, -0.5F, 7.0F, 2.0F, 1.0F)
            .texOffs(18, 38).addBox(-1.0F, 8.0F, -0.5F, 5.0F, 1.0F, 1.0F)
            .texOffs(14, 56).addBox(0.0F, 6.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(4.0F, -1.5F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition part_cube_r14 = part_bone4.addOrReplaceChild("cube_r14", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r15 = part_bone4.addOrReplaceChild("cube_r15", CubeListBuilder.create()
            .texOffs(18, 38).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(4.5F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r16 = part_bone4.addOrReplaceChild("cube_r16", CubeListBuilder.create()
            .texOffs(14, 56).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r17 = part_bone4.addOrReplaceChild("cube_r17", CubeListBuilder.create()
            .texOffs(14, 56).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_bone5 = rootDef.addOrReplaceChild("bone5", CubeListBuilder.create()
            .texOffs(33, 21).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F)
            .texOffs(14, 56).addBox(-1.5F, 2.5F, -0.5F, 3.0F, 2.0F, 1.0F)
            .texOffs(18, 38).addBox(-2.5F, 4.5F, -0.5F, 5.0F, 1.0F, 1.0F)
            .texOffs(0, 25).addBox(-3.5F, 5.5F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(17.5F, 15.5F, 4.5F, -0.2182F, -0.7854F, 0.0F));

        PartDefinition part_cube_r18 = part_bone5.addOrReplaceChild("cube_r18", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -6.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r19 = part_bone5.addOrReplaceChild("cube_r19", CubeListBuilder.create()
            .texOffs(18, 38).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_cube_r20 = part_bone5.addOrReplaceChild("cube_r20", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-2.0F, 9.0F, -0.5F, 7.0F, 2.0F, 1.0F)
            .texOffs(18, 38).addBox(-1.0F, 8.0F, -0.5F, 5.0F, 1.0F, 1.0F)
            .texOffs(14, 56).addBox(0.0F, 6.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(4.0F, -1.5F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition part_cube_r21 = part_bone5.addOrReplaceChild("cube_r21", CubeListBuilder.create()
            .texOffs(0, 25).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r22 = part_bone5.addOrReplaceChild("cube_r22", CubeListBuilder.create()
            .texOffs(18, 38).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(4.5F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r23 = part_bone5.addOrReplaceChild("cube_r23", CubeListBuilder.create()
            .texOffs(14, 56).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition part_cube_r24 = part_bone5.addOrReplaceChild("cube_r24", CubeListBuilder.create()
            .texOffs(14, 56).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition part_bb_main = rootDef.addOrReplaceChild("bb_main", CubeListBuilder.create()
            .texOffs(50, 0).addBox(-2.0F, -19.0F, -6.0F, 4.0F, 4.0F, 3.0F)
            .texOffs(32, 48).addBox(-3.0F, -20.0F, -3.0F, 6.0F, 6.0F, 10.0F)
            .texOffs(0, 59).addBox(-1.0F, -23.0F, -2.0F, 2.0F, 3.0F, 2.0F)
            .texOffs(0, 59).addBox(-1.0F, -14.0F, -2.0F, 2.0F, 3.0F, 2.0F)
            .texOffs(0, 38).addBox(3.0F, -18.0F, -2.0F, 3.0F, 2.0F, 2.0F)
            .texOffs(0, 38).addBox(-6.0F, -18.0F, -2.0F, 3.0F, 2.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.bone != null) {
            this.bone.yRot = netHeadYaw * ((float)Math.PI / 180F);
            this.bone.xRot = -0.2618F + headPitch * ((float)Math.PI / 180F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
