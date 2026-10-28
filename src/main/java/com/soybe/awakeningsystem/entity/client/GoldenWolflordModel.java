package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GoldenWolflordBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GoldenWolflordModel extends EntityModel<GoldenWolflordBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "golden_wolflord"), "main");

    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart body;

    public GoldenWolflordModel(ModelPart root) {
        this.bone = root.getChild("bone");
        this.head = this.bone.getChild("head");
        this.body = this.bone.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = bone.addOrReplaceChild("head",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.9154F, -10.9715F, -29.7953F, 24.0F, 24.0F, 24.0F)
                .texOffs(0, 50).addBox(-8.9154F, -1.9715F, -47.7953F, 18.0F, 9.0F, 18.0F)
                .texOffs(0, 0).addBox(-8.9154F, 7.0285F, -44.7953F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(6.0846F, 7.0285F, -44.7953F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(6.0546F, 7.0285F, -35.7953F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(-8.8854F, 7.0285F, -35.7953F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(2.9154F, -40.0285F, -3.2047F));

        head.addOrReplaceChild("head_r1",
                CubeListBuilder.create()
                .texOffs(100, 145).addBox(-3.5F, -0.2105F, -3.1126F, 7.0F, 5.0F, 8.0F)
                , PartPose.offsetAndRotation(-9.4154F, -10.1738F, 2.1538F, 0.934F, 0.4239F, -0.5148F));

        head.addOrReplaceChild("head_r2",
                CubeListBuilder.create()
                .texOffs(100, 145).addBox(-4.5F, -2.5F, 0.0F, 9.0F, 5.0F, 8.0F)
                , PartPose.offsetAndRotation(-0.4154F, -8.4715F, 2.2047F, 0.7418F, 0.0F, 0.0F));

        head.addOrReplaceChild("head_r3",
                CubeListBuilder.create()
                .texOffs(0, 172).addBox(-2.5F, -4.8139F, -29.7701F, 5.0F, 2.0F, 31.0F)
                , PartPose.offsetAndRotation(-14.0652F, -29.8323F, 25.6657F, 0.7345F, -0.4054F, 0.1258F));

        head.addOrReplaceChild("head_r4",
                CubeListBuilder.create()
                .texOffs(85, 223).addBox(-2.5F, -4.8321F, -1.2348F, 5.0F, 2.0F, 31.0F)
                , PartPose.offsetAndRotation(-14.0652F, -29.8323F, 25.6657F, 0.2109F, -0.4054F, 0.1258F));

        head.addOrReplaceChild("head_r5",
                CubeListBuilder.create()
                .texOffs(85, 223).addBox(-3.263F, -0.0193F, -0.8F, 5.0F, 2.0F, 31.0F)
                , PartPose.offsetAndRotation(25.1554F, -34.2522F, 23.5799F, 0.3316F, 0.5476F, 0.1355F));

        head.addOrReplaceChild("head_r6",
                CubeListBuilder.create()
                .texOffs(0, 172).addBox(-2.5F, 0.5F, 6.0F, 5.0F, 2.0F, 31.0F)
                .texOffs(100, 145).addBox(-2.5F, -1.5F, -2.0F, 7.0F, 5.0F, 8.0F)
                , PartPose.offsetAndRotation(7.5846F, -8.4715F, 2.2047F, 0.8552F, 0.5476F, 0.1355F));

        head.addOrReplaceChild("head_r7",
                CubeListBuilder.create()
                .texOffs(0, 230).addBox(-4.5F, -2.5F, -1.5F, 9.0F, 5.0F, 21.0F)
                , PartPose.offsetAndRotation(8.5846F, -10.4715F, -4.2953F, 0.6646F, -0.3173F, 0.7111F));

        head.addOrReplaceChild("head_r8",
                CubeListBuilder.create()
                .texOffs(0, 230).addBox(-4.5F, -2.5F, -1.5F, 9.0F, 5.0F, 21.0F)
                , PartPose.offsetAndRotation(-0.4154F, -10.4715F, -4.2953F, 0.6981F, 0.0F, 0.0F));

        head.addOrReplaceChild("head_r9",
                CubeListBuilder.create()
                .texOffs(0, 230).addBox(-5.5F, -2.5F, -1.5F, 9.0F, 5.0F, 21.0F)
                , PartPose.offsetAndRotation(-8.4154F, -10.4715F, -4.2953F, 0.6058F, 0.4507F, -0.9563F));

        head.addOrReplaceChild("head_r10",
                CubeListBuilder.create()
                .texOffs(238, 0).addBox(-4.5F, -15.0F, -1.5F, 6.0F, 6.0F, 3.0F)
                .texOffs(125, 0).mirror(true).addBox(-4.5F, -9.0F, -1.5F, 9.0F, 12.0F, 3.0F)
                , PartPose.offsetAndRotation(-7.4154F, -10.9715F, -19.2953F, 0.0F, 0.0F, -0.3054F));

        head.addOrReplaceChild("head_r11",
                CubeListBuilder.create()
                .texOffs(238, 0).mirror(true).addBox(-1.5F, -15.0F, -1.5F, 6.0F, 6.0F, 3.0F)
                .texOffs(125, 0).addBox(-4.5F, -9.0F, -1.5F, 9.0F, 12.0F, 3.0F)
                , PartPose.offsetAndRotation(7.5846F, -10.9715F, -19.2953F, 0.0F, 0.0F, 0.3054F));

        head.addOrReplaceChild("head_r12",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(4.5F, -6.0F, -16.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(4.5F, -6.0F, -10.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(-7.5F, -6.0F, -10.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(-7.5F, -6.0F, -16.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(86, 2).mirror(true).addBox(-11.0F, -2.0F, -22.0F, 2.0F, 2.0F, 11.0F)
                .texOffs(86, 2).mirror(true).addBox(9.0F, -2.0F, -22.0F, 2.0F, 2.0F, 11.0F)
                .texOffs(72, 53).mirror(true).addBox(-9.0F, -3.0F, -18.0F, 18.0F, 6.0F, 18.0F)
                , PartPose.offsetAndRotation(0.0846F, 10.0285F, -29.7953F, 0.4363F, 0.0F, 0.0F));

        head.addOrReplaceChild("head_r13",
                CubeListBuilder.create()
                .texOffs(184, 0).addBox(1.0F, -2.0F, -3.0F, 8.0F, 6.0F, 27.0F)
                , PartPose.offsetAndRotation(9.0846F, -4.9715F, 9.2047F, 0.2531F, -0.0198F, 1.8444F));

        head.addOrReplaceChild("head_r14",
                CubeListBuilder.create()
                .texOffs(184, 0).addBox(-5.0F, -2.0F, -3.0F, 8.0F, 6.0F, 27.0F)
                , PartPose.offsetAndRotation(9.0846F, -4.9715F, 9.2047F, 0.2449F, -0.4677F, 1.1095F));

        head.addOrReplaceChild("head_r15",
                CubeListBuilder.create()
                .texOffs(184, 0).addBox(-10.0F, 1.0F, -3.0F, 8.0F, 6.0F, 27.0F)
                , PartPose.offsetAndRotation(-11.9154F, -7.9715F, 9.2047F, 0.2435F, -0.1011F, -1.6857F));

        head.addOrReplaceChild("head_r16",
                CubeListBuilder.create()
                .texOffs(184, 0).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 27.0F)
                , PartPose.offsetAndRotation(-11.9154F, -7.9715F, 9.2047F, 0.3665F, 0.4079F, -0.8989F));

        head.addOrReplaceChild("head_r17",
                CubeListBuilder.create()
                .texOffs(184, 0).addBox(-5.0F, -3.0F, 1.0F, 9.0F, 6.0F, 27.0F)
                , PartPose.offsetAndRotation(0.0846F, -7.9715F, 9.2047F, 0.6109F, 0.0F, 0.0F));

        head.addOrReplaceChild("head_r18",
                CubeListBuilder.create()
                .texOffs(206, 37).mirror(true).addBox(-0.5F, -5.0F, -1.0F, 1.0F, 10.0F, 24.0F)
                .texOffs(230, 79).addBox(-0.5F, -3.0F, 23.0F, 1.0F, 6.0F, 12.0F)
                , PartPose.offsetAndRotation(-12.4154F, 1.0285F, -24.7953F, 0.2618F, -0.0873F, 0.0F));

        head.addOrReplaceChild("head_r19",
                CubeListBuilder.create()
                .texOffs(230, 79).addBox(-0.5F, -3.0F, 23.0F, 1.0F, 6.0F, 12.0F)
                .texOffs(206, 37).addBox(-0.5F, -5.0F, -1.0F, 1.0F, 10.0F, 24.0F)
                , PartPose.offsetAndRotation(12.5846F, 1.0285F, -24.7953F, 0.2618F, 0.0873F, 0.0F));

        head.addOrReplaceChild("head_r20",
                CubeListBuilder.create()
                .texOffs(151, 58).mirror(true).addBox(-18.5F, -2.5F, -2.0F, 19.0F, 1.0F, 6.0F)
                , PartPose.offsetAndRotation(-11.4154F, 12.5285F, -12.7953F, 0.0F, 1.0472F, -0.1309F));

        head.addOrReplaceChild("head_r21",
                CubeListBuilder.create()
                .texOffs(151, 58).addBox(-0.5F, -2.5F, -2.0F, 19.0F, 1.0F, 6.0F)
                , PartPose.offsetAndRotation(11.5846F, 12.5285F, -12.7953F, 0.0F, -1.0472F, 0.1309F));

        PartDefinition body = bone.addOrReplaceChild("body",
                CubeListBuilder.create()
                .texOffs(0, 85).addBox(-9.0F, -43.0F, -9.0F, 24.0F, 17.0F, 22.0F)
                .texOffs(65, 169).addBox(-5.0F, -26.0F, -5.0F, 16.0F, 2.0F, 11.0F)
                .texOffs(143, 174).addBox(-3.0F, -24.0F, -3.0F, 12.0F, 2.0F, 7.0F)
                .texOffs(79, 197).addBox(-5.0F, -25.0F, 14.0F, 16.0F, 3.0F, 11.0F)
                .texOffs(45, 136).addBox(-3.0F, -22.0F, 16.0F, 12.0F, 3.0F, 7.0F)
                .texOffs(118, 23).addBox(-7.0F, -48.0F, -9.0F, 20.0F, 5.0F, 17.0F)
                .texOffs(0, 85).addBox(-9.0F, -40.0F, 13.0F, 24.0F, 15.0F, 24.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("head_r22",
                CubeListBuilder.create()
                .texOffs(0, 134).addBox(-0.5F, -4.0F, -14.0F, 1.0F, 8.0F, 18.0F)
                .texOffs(0, 134).mirror(true).addBox(-25.5F, -4.0F, -14.0F, 1.0F, 8.0F, 18.0F)
                , PartPose.offsetAndRotation(15.5F, -39.0F, 5.0F, 0.9163F, 0.0F, 0.0F));

        body.addOrReplaceChild("head_r23",
                CubeListBuilder.create()
                .texOffs(220, 149).addBox(-19.0F, 0.0F, 30.3301F, 14.0F, 5.0F, 4.0F)
                .texOffs(230, 164).addBox(5.0F, 1.0F, 30.3301F, 10.0F, 3.0F, 3.0F)
                .texOffs(150, 154).addBox(-5.0F, -3.0F, 23.3301F, 10.0F, 11.0F, 43.0F)
                .texOffs(172, 221).addBox(-9.0F, -3.0F, -0.6699F, 18.0F, 11.0F, 24.0F)
                , PartPose.offsetAndRotation(53.7682F, -75.0113F, 39.8597F, -2.5846F, 0.2367F, 2.9492F));

        body.addOrReplaceChild("head_r24",
                CubeListBuilder.create()
                .texOffs(172, 221).addBox(-9.0F, -2.0F, -23.0622F, 18.0F, 11.0F, 24.0F)
                , PartPose.offsetAndRotation(53.7682F, -75.0113F, 39.8597F, -3.1082F, 0.2367F, 2.9492F));

        body.addOrReplaceChild("head_r25",
                CubeListBuilder.create()
                .texOffs(49, 220).addBox(-11.0F, -7.5F, 12.0F, 22.0F, 15.0F, 5.0F)
                .texOffs(137, 211).addBox(9.0F, -1.5F, -9.0F, 5.0F, 5.0F, 18.0F)
                .texOffs(137, 211).addBox(-14.0F, -1.5F, -9.0F, 5.0F, 5.0F, 18.0F)
                .texOffs(172, 221).addBox(-9.0F, -5.5F, -12.0F, 18.0F, 11.0F, 24.0F)
                , PartPose.offsetAndRotation(46.897F, -61.3315F, 68.0125F, -2.6654F, 0.7497F, 2.4767F));

        body.addOrReplaceChild("head_r26",
                CubeListBuilder.create()
                .texOffs(137, 211).addBox(-19.951F, -14.8027F, 18.4104F, 5.0F, 5.0F, 18.0F)
                .texOffs(137, 211).addBox(3.049F, -14.8027F, 18.4104F, 5.0F, 5.0F, 18.0F)
                .texOffs(172, 221).addBox(-14.951F, -19.8027F, 13.4104F, 18.0F, 11.0F, 24.0F)
                , PartPose.offsetAndRotation(3.0F, -39.5F, 59.0F, -0.1804F, 1.4238F, -0.1836F));

        body.addOrReplaceChild("head_r27",
                CubeListBuilder.create()
                .texOffs(172, 107).addBox(-9.0F, -5.3137F, 0.7054F, 18.0F, 11.0F, 24.0F)
                , PartPose.offsetAndRotation(3.0F, -39.5F, 59.0F, 0.859F, 0.8246F, 0.1752F));

        body.addOrReplaceChild("head_r28",
                CubeListBuilder.create()
                .texOffs(104, 87).addBox(-9.0F, -5.5F, 1.0F, 18.0F, 11.0F, 24.0F)
                , PartPose.offsetAndRotation(3.0F, -32.5F, 36.0F, 0.2618F, 0.0F, 0.0F));

        body.addOrReplaceChild("head_r29",
                CubeListBuilder.create()
                .texOffs(135, 137).mirror(true).addBox(-3.5F, -6.5F, 0.0F, 7.0F, 9.0F, 17.0F)
                .texOffs(170, 71).mirror(true).addBox(-3.5F, -6.5F, -17.0F, 7.0F, 17.0F, 17.0F)
                , PartPose.offsetAndRotation(-24.5F, -36.5F, 8.0F, 0.0F, -0.1745F, 0.0F));

        body.addOrReplaceChild("head_r30",
                CubeListBuilder.create()
                .texOffs(135, 137).addBox(-3.5F, -6.5F, 0.0F, 7.0F, 9.0F, 17.0F)
                .texOffs(170, 71).addBox(-3.5F, -6.5F, -17.0F, 7.0F, 17.0F, 17.0F)
                , PartPose.offsetAndRotation(31.5F, -36.5F, 8.0F, 0.0F, 0.1745F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(GoldenWolflordBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        float wave = Mth.sin(ageInTicks * 0.12F);
        float waveCos = Mth.cos(ageInTicks * 0.12F);
        this.body.yRot = wave * 0.15F;
        this.body.zRot = waveCos * 0.08F;
        this.bone.y = 24.0F + wave * 4.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.bone.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
