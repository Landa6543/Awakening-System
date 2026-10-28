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

public class RuinDestroyerModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("awakening_system", "ruindestroyermodel"), "main");

    private final ModelPart root;
    private ModelPart HEAD;

    public RuinDestroyerModel(ModelPart root) {
        this.root = root;
        initParts(root);
    }

    private void initParts(ModelPart root) {
        try {
            this.HEAD = root.getChild("HEAD");
        } catch (Exception ignored) {}
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();

        PartDefinition part_HEAD = rootDef.addOrReplaceChild("HEAD", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.6837F, -2.1867F, -5.1115F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r1 = part_HEAD.addOrReplaceChild("cube_r1", CubeListBuilder.create()
            .texOffs(0, 27).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offsetAndRotation(0.0811F, -2.5475F, -1.6314F, -1.8733F, 0.1525F, 0.1201F));

        PartDefinition part_cube_r2 = part_HEAD.addOrReplaceChild("cube_r2", CubeListBuilder.create()
            .texOffs(52, 0).addBox(7.0F, 2.0F, -1.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(-7.9189F, -6.5475F, -5.6314F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r3 = part_HEAD.addOrReplaceChild("cube_r3", CubeListBuilder.create()
            .texOffs(36, 30).mirror().addBox(-8.0F, -1.0F, -3.0F, 8.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(8.0811F, -6.5475F, -5.6314F, 0.0F, -0.0873F, -0.0436F));

        PartDefinition part_cube_r4 = part_HEAD.addOrReplaceChild("cube_r4", CubeListBuilder.create()
            .texOffs(36, 30).addBox(0.0F, -1.0F, -3.0F, 8.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(-7.9189F, -6.5475F, -5.6314F, 0.0F, 0.0873F, 0.0436F));

        PartDefinition part_cube_r5 = part_HEAD.addOrReplaceChild("cube_r5", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(-3.8483F, -2.4987F, -5.8885F, 0.0436F, 0.0F, 0.829F));

        PartDefinition part_cube_r6 = part_HEAD.addOrReplaceChild("cube_r6", CubeListBuilder.create()
            .texOffs(0, 0).mirror().addBox(-5.0F, -1.0F, -3.0F, 10.0F, 2.0F, 6.0F),
            PartPose.offsetAndRotation(3.5239F, -2.6591F, -5.8885F, 0.0436F, 0.0F, -0.7854F));

        PartDefinition part_bb_main = rootDef.addOrReplaceChild("bb_main", CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r7 = part_bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create()
            .texOffs(6, 15).addBox(7.0F, 1.0F, 0.6553F, 1.0F, 2.0F, 1.0F)
            .texOffs(6, 15).addBox(7.0F, -5.0F, 0.6553F, 1.0F, 2.0F, 1.0F)
            .texOffs(6, 15).addBox(7.0F, -1.342F, -3.0F, 1.0F, 1.0F, 2.0F)
            .texOffs(6, 15).addBox(7.0F, -1.342F, 3.0F, 1.0F, 1.0F, 2.0F)
            .texOffs(22, 15).addBox(3.0F, -3.0F, -1.0F, 6.0F, 4.0F, 4.0F)
            .texOffs(0, 27).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offsetAndRotation(2.4157F, -19.658F, -0.1553F, -1.6039F, -0.9947F, -1.1882F));

        PartDefinition part_cube_r8 = part_bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create()
            .texOffs(15, 55).mirror().addBox(0.0F, -1.0F, -8.0F, 8.0F, 1.0F, 8.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1705F, -0.0376F, -0.215F));

        PartDefinition part_cube_r9 = part_bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create()
            .texOffs(15, 55).addBox(-8.0F, -1.0F, -8.0F, 8.0F, 1.0F, 8.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1705F, 0.0376F, 0.215F));

        PartDefinition part_cube_r10 = part_bb_main.addOrReplaceChild("cube_r10", CubeListBuilder.create()
            .texOffs(15, 55).mirror().addBox(-4.0F, -0.5F, -4.0F, 8.0F, 1.0F, 8.0F),
            PartPose.offsetAndRotation(3.797F, -2.0279F, 3.7041F, 1.3526F, -1.3963F, -1.5708F));

        PartDefinition part_cube_r11 = part_bb_main.addOrReplaceChild("cube_r11", CubeListBuilder.create()
            .texOffs(0, 27).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offsetAndRotation(1.6696F, -23.5124F, -2.2971F, -2.4726F, 0.356F, -0.8616F));

        PartDefinition part_cube_r12 = part_bb_main.addOrReplaceChild("cube_r12", CubeListBuilder.create()
            .texOffs(0, 27).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offsetAndRotation(0.9764F, -11.7328F, -0.5003F, -0.4673F, -0.5982F, -2.9911F));

        PartDefinition part_cube_r13 = part_bb_main.addOrReplaceChild("cube_r13", CubeListBuilder.create()
            .texOffs(0, 27).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0865F, -0.0114F, 1.0031F));

        PartDefinition part_cube_r14 = part_bb_main.addOrReplaceChild("cube_r14", CubeListBuilder.create()
            .texOffs(15, 55).addBox(-4.0F, -0.5F, -4.0F, 8.0F, 1.0F, 8.0F),
            PartPose.offsetAndRotation(-3.797F, -2.0279F, 3.7041F, 1.3526F, 1.3963F, 1.5708F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.HEAD != null) {
            this.HEAD.yRot = netHeadYaw * ((float)Math.PI / 180F);
            this.HEAD.xRot = headPitch * ((float)Math.PI / 180F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
