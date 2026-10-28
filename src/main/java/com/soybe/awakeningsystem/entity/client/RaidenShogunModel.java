package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RaidenBossEntity;
import net.minecraft.client.model.ArmedModel;
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
import net.minecraft.world.entity.HumanoidArm;

public class RaidenShogunModel extends EntityModel<RaidenBossEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "raiden_shogun"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart armRight;
    private final ModelPart armLeft;
    private final ModelPart legRight;
    private final ModelPart legLeft;

    public RaidenShogunModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.armRight = root.getChild("armRight");
        this.armLeft = root.getChild("armLeft");
        this.legRight = root.getChild("legRight");
        this.legLeft = root.getChild("legLeft");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition part_head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.1F, -4.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(32, 0).addBox(-4.0F, -8.1F, -4.0F, 8.0F, 8.0F, 8.0F)
                , PartPose.offsetAndRotation(0.0F, 3.1F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_headDeco = part_head.addOrReplaceChild("headDeco",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.0F, 17.9F, -0.8F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_TrueEventaille = part_headDeco.addOrReplaceChild("TrueEventaille",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-4.2F, -23.35F, 1.55F, -0.0873F, 0.4363F, 0.0F));

        PartDefinition part_Eventaille3 = part_TrueEventaille.addOrReplaceChild("Eventaille3",
                CubeListBuilder.create()
                .texOffs(27, 73).addBox(0.0067F, -0.75F, -3.0385F, 1.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.05F, 0.0F, 0.3927F, 0.0F));

        PartDefinition part_bone13 = part_TrueEventaille.addOrReplaceChild("bone13",
                CubeListBuilder.create()
                .texOffs(0, 76).addBox(0.075F, 0.0F, -3.0385F, 1.0F, 1.0F, 3.0F)
                , PartPose.offsetAndRotation(-0.1F, 0.05F, 0.05F, -0.2849F, 0.274F, -0.8249F));

        PartDefinition part_bone15 = part_TrueEventaille.addOrReplaceChild("bone15",
                CubeListBuilder.create()
                .texOffs(0, 76).addBox(0.075F, 0.0F, -3.0385F, 1.0F, 1.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.65F, 0.45F, 0.1414F, 0.6614F, -0.7517F));

        PartDefinition part_bone12 = part_TrueEventaille.addOrReplaceChild("bone12",
                CubeListBuilder.create()
                .texOffs(0, 76).addBox(-0.0318F, 0.0329F, -2.801F, 1.0F, 1.0F, 3.0F)
                , PartPose.offsetAndRotation(-0.1F, -0.3F, -0.05F, -0.4614F, 0.0823F, -0.796F));

        PartDefinition part_bone14 = part_TrueEventaille.addOrReplaceChild("bone14",
                CubeListBuilder.create()
                .texOffs(0, 76).addBox(-0.0318F, 0.0329F, -2.801F, 1.0F, 1.0F, 3.0F)
                , PartPose.offsetAndRotation(-0.1F, -0.3F, -0.05F, -0.4614F, 0.0823F, -0.796F));

        PartDefinition part_Eventaille4 = part_TrueEventaille.addOrReplaceChild("Eventaille4",
                CubeListBuilder.create()
                .texOffs(27, 73).addBox(0.0065F, -0.75F, -3.0385F, 1.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.05F, -0.2618F, 0.3927F, 0.0F));

        PartDefinition part_bone11 = part_TrueEventaille.addOrReplaceChild("bone11",
                CubeListBuilder.create()
                .texOffs(0, 76).addBox(-0.1318F, 0.0829F, -3.001F, 1.0F, 1.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.05F, 0.05F, -0.1133F, 0.4552F, -0.864F));

        PartDefinition part_Eventaille5 = part_TrueEventaille.addOrReplaceChild("Eventaille5",
                CubeListBuilder.create()
                .texOffs(27, 73).addBox(0.0066F, -0.75F, -3.0385F, 1.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.05F, 0.2618F, 0.3927F, 0.0F));

        PartDefinition part_Eventaille6 = part_TrueEventaille.addOrReplaceChild("Eventaille6",
                CubeListBuilder.create()
                .texOffs(27, 73).addBox(0.0064F, -0.75F, -3.0385F, 1.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.05F, 0.5236F, 0.3927F, 0.0F));

        PartDefinition part_Eventaille7 = part_TrueEventaille.addOrReplaceChild("Eventaille7",
                CubeListBuilder.create()
                .texOffs(27, 73).addBox(0.0066F, -0.75F, -3.0385F, 1.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.05F, -0.5236F, 0.3927F, 0.0F));

        PartDefinition part_Eventaille8 = part_TrueEventaille.addOrReplaceChild("Eventaille8",
                CubeListBuilder.create()
                .texOffs(0, 83).addBox(0.0066F, 0.0294F, -2.5885F, 1.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(-0.1F, -1.0F, 0.05F, -0.6425F, -0.0352F, -0.6323F));

        PartDefinition part_TrueFlower = part_headDeco.addOrReplaceChild("TrueFlower",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-5.8F, -23.1F, -0.1F, 0.0F, -0.9599F, 0.0F));

        PartDefinition part_Flower = part_TrueFlower.addOrReplaceChild("Flower",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-5.8F, -23.1F, -0.1F, -0.0013F, -0.0334F, -1.5774F));

        PartDefinition part_bone = part_Flower.addOrReplaceChild("bone",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(0.3657F, 4.3241F, 10.0818F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, 0.0F, 0.0F));

        PartDefinition part_bone2 = part_Flower.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.6678F, 2.677F, 10.3723F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -1.2566F, 0.0F));

        PartDefinition part_bone3 = part_Flower.addOrReplaceChild("bone3",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.3235F, -1.6097F, 11.1281F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -2.5133F, 0.0F));

        PartDefinition part_bone4 = part_Flower.addOrReplaceChild("bone4",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-0.6194F, -0.7564F, -0.192F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, -0.2159F, -0.3893F, -1.3963F, 2.5133F, 0.0F));

        PartDefinition part_bone5 = part_Flower.addOrReplaceChild("bone5",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-1.8615F, -1.6134F, -1.0563F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, 0.7841F, -1.3893F, -1.3963F, 1.2566F, 0.0F));

        PartDefinition part_Flower2 = part_TrueFlower.addOrReplaceChild("Flower2",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.7F, 0.6F, -1.1F, 0.0264F, -0.0323F, -1.3591F));

        PartDefinition part_bone6 = part_Flower2.addOrReplaceChild("bone6",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(0.3657F, 4.3241F, 10.0818F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, 0.0F, 0.0F));

        PartDefinition part_bone7 = part_Flower2.addOrReplaceChild("bone7",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.6678F, 2.677F, 10.3723F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -1.2566F, 0.0F));

        PartDefinition part_bone8 = part_Flower2.addOrReplaceChild("bone8",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.3235F, -1.6097F, 11.1281F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -2.5133F, 0.0F));

        PartDefinition part_bone9 = part_Flower2.addOrReplaceChild("bone9",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-0.6194F, -0.7564F, -0.192F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, -0.2159F, -0.3893F, -1.3963F, 2.5133F, 0.0F));

        PartDefinition part_bone10 = part_Flower2.addOrReplaceChild("bone10",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-1.8615F, -1.6134F, -1.0563F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, 0.7841F, -1.3893F, -1.3963F, 1.2566F, 0.0F));

        PartDefinition part_Papillon = part_headDeco.addOrReplaceChild("Papillon",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-4.0F, -23.5F, 2.4F, 0.4363F, 0.0F, 0.0F));

        PartDefinition part_bone16 = part_Papillon.addOrReplaceChild("bone16",
                CubeListBuilder.create()
                .texOffs(0, 90).addBox(0.0F, -1.5F, -1.5F, 1.0F, 3.0F, 2.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition part_bone17 = part_Papillon.addOrReplaceChild("bone17",
                CubeListBuilder.create()
                .texOffs(7, 90).addBox(0.0F, -1.5F, 0.0F, 1.0F, 3.0F, 2.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition part_bone18 = part_headDeco.addOrReplaceChild("bone18",
                CubeListBuilder.create()
                .texOffs(0, 96).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(5, 96).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                , PartPose.offsetAndRotation(-3.9F, -22.5F, 2.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition part_bone19 = part_headDeco.addOrReplaceChild("bone19",
                CubeListBuilder.create()
                .texOffs(0, 100).addBox(-0.01F, -4.5F, -2.0F, 1.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(-4.2F, -23.3F, -0.3F, 0.1308F, -0.0057F, 0.0433F));

        PartDefinition part_Hairs = part_head.addOrReplaceChild("Hairs",
                CubeListBuilder.create()
                .texOffs(100, 0).addBox(-3.5F, 0.0F, -1.0F, 7.0F, 2.0F, 1.0F)
                .texOffs(100, 4).addBox(-3.0F, 2.0F, -1.0F, 6.0F, 1.0F, 1.0F)
                .texOffs(100, 7).addBox(-2.5F, 2.5F, -1.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(100, 10).addBox(-3.0F, 3.5F, -1.0F, 6.0F, 2.0F, 1.0F)
                .texOffs(100, 14).addBox(-2.5F, 5.0F, -1.0F, 5.0F, 2.0F, 1.0F)
                .texOffs(100, 22).addBox(-2.5F, 7.0F, -1.0F, 5.0F, 2.0F, 1.0F)
                .texOffs(100, 26).addBox(-2.0F, 8.5F, -1.0F, 4.0F, 4.0F, 1.0F)
                .texOffs(100, 18).addBox(-2.0F, 5.0F, -1.0F, 4.0F, 2.0F, 1.0F)
                .texOffs(85, 0).addBox(2.0F, 4.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 0).addBox(1.5F, 5.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 0).addBox(1.5F, 7.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 0).addBox(-2.5F, 7.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 0).addBox(-2.5F, 5.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 6).addBox(-2.5F, 10.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 3).addBox(1.5F, 10.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 0).addBox(-3.0F, 4.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 9).addBox(1.0F, 11.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 9).addBox(-2.0F, 11.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(100, 32).addBox(-1.5F, 12.0F, -1.0F, 3.0F, 2.0F, 1.0F)
                .texOffs(100, 36).addBox(-1.0F, 13.5F, -1.0F, 2.0F, 7.0F, 1.0F)
                .texOffs(85, 12).addBox(0.5F, 12.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 12).addBox(-1.5F, 12.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(85, 15).addBox(-1.0F, 19.0F, -1.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(85, 18).addBox(-0.5F, 20.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(90, 18).addBox(0.5F, 14.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(92, 15).addBox(1.0F, 13.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(90, 18).addBox(-2.0F, 14.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(92, 15).addBox(-1.5F, 13.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(100, 45).addBox(-1.5F, 16.0F, -1.0F, 3.0F, 1.0F, 1.0F)
                , PartPose.offsetAndRotation(0.0F, -0.3F, 4.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_bone34 = part_head.addOrReplaceChild("bone34",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-2.2F, -2.1F, -3.35F, -3.0218F, -0.0536F, -2.839F));

        PartDefinition part_bone35 = part_bone34.addOrReplaceChild("bone35",
                CubeListBuilder.create()
                .texOffs(112, 71).addBox(0.0F, -1.0F, 0.0F, 1.0F, 4.0F, 1.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, -0.6F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_bone36 = part_bone34.addOrReplaceChild("bone36",
                CubeListBuilder.create()
                .texOffs(117, 71).addBox(-0.8188F, -1.2889F, -0.5174F, 1.0F, 2.0F, 1.0F)
                , PartPose.offsetAndRotation(0.6F, 4.0F, -0.1F, 0.0F, 0.0F, 0.1745F));

        PartDefinition part_bone37 = part_bone34.addOrReplaceChild("bone37",
                CubeListBuilder.create()
                .texOffs(106, 71).addBox(-0.2189F, -0.0967F, -0.5F, 1.0F, 2.0F, 1.0F)
                , PartPose.offsetAndRotation(0.5F, 4.0F, -0.1F, 0.0F, 0.0F, 1.2654F));

        PartDefinition part_bone38 = part_head.addOrReplaceChild("bone38",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(2.8F, 0.6F, -3.45F, -0.1311F, 0.0038F, -0.0872F));

        PartDefinition part_bone39 = part_bone38.addOrReplaceChild("bone39",
                CubeListBuilder.create()
                .texOffs(112, 71).addBox(0.0F, -1.0F, 0.0F, 1.0F, 4.0F, 1.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, -0.6F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_bone40 = part_bone38.addOrReplaceChild("bone40",
                CubeListBuilder.create()
                .texOffs(117, 71).addBox(-0.8188F, -1.2889F, -0.5174F, 1.0F, 2.0F, 1.0F)
                , PartPose.offsetAndRotation(0.6F, 4.0F, -0.1F, 0.0F, 0.0F, 0.1745F));

        PartDefinition part_bone41 = part_bone38.addOrReplaceChild("bone41",
                CubeListBuilder.create()
                .texOffs(106, 71).addBox(-0.2189F, -0.0967F, -0.5F, 1.0F, 2.0F, 1.0F)
                , PartPose.offsetAndRotation(0.5F, 4.0F, -0.1F, 0.0F, 0.0F, 1.2654F));

        PartDefinition part_armRight = root.addOrReplaceChild("armRight",
                CubeListBuilder.create()
                .texOffs(83, 95).addBox(-0.8823F, 1.0305F, -1.0F, 2.0F, 8.0F, 2.0F)
                .texOffs(82, 106).addBox(-1.3823F, 0.2805F, -1.5F, 3.0F, 8.0F, 3.0F)
                , PartPose.offsetAndRotation(-4.0F, 3.5F, 0.0F, 0.0F, 0.0F, 0.4363F));

        PartDefinition part_armLeft = root.addOrReplaceChild("armLeft",
                CubeListBuilder.create()
                .texOffs(97, 82).addBox(-1.6177F, 0.2805F, -1.5F, 3.0F, 8.0F, 3.0F)
                .texOffs(98, 106).addBox(-1.1177F, 1.0307F, -1.0F, 2.0F, 8.0F, 2.0F)
                , PartPose.offsetAndRotation(4.0F, 3.5F, 0.0F, 0.0F, 0.0F, -0.4363F));

        PartDefinition part_armLeftDeco2 = part_armLeft.addOrReplaceChild("armLeftDeco2",
                CubeListBuilder.create()
                .texOffs(53, 84).addBox(0.4F, 0.3F, -0.5F, 1.0F, 8.0F, 4.0F)
                .texOffs(67, 72).addBox(0.9F, 0.3F, 1.0F, 1.0F, 5.0F, 2.0F)
                .texOffs(67, 80).addBox(0.9F, 0.79F, 0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(71, 80).addBox(0.9F, 0.79F, 2.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(65, 88).addBox(0.9F, 4.8F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(65, 92).addBox(0.9F, 4.8F, 2.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(71, 88).addBox(0.9F, 4.3F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(71, 88).addBox(0.9F, 4.3F, 3.0F, 1.0F, 1.0F, 1.0F)
                , PartPose.offsetAndRotation(2.8F, 0.0F, 2.0F, 3.1416F, 0.0F, 3.098F));

        PartDefinition part_bone30 = part_armLeftDeco2.addOrReplaceChild("bone30",
                CubeListBuilder.create()
                .texOffs(53, 97).addBox(-1.0F, -0.71F, -2.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(60, 97).addBox(-0.5F, -0.7101F, -2.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(53, 104).addBox(-1.0F, -0.7101F, -0.49F, 1.0F, 4.0F, 1.0F)
                .texOffs(58, 104).addBox(-1.0F, 0.281F, -1.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(58, 107).addBox(-1.0F, 2.2899F, -0.99F, 1.0F, 1.0F, 1.0F)
                .texOffs(63, 107).addBox(-1.0F, 2.7899F, -0.99F, 1.0F, 1.0F, 1.0F)
                , PartPose.offsetAndRotation(1.4F, 1.5F, 3.5F, -3.1416F, -0.7854F, 3.1416F));

        PartDefinition part_bone31 = part_armLeftDeco2.addOrReplaceChild("bone31",
                CubeListBuilder.create()
                .texOffs(53, 97).addBox(-0.5F, -0.71F, -2.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(60, 97).addBox(0.0F, -0.7101F, -2.0F, 1.0F, 4.0F, 2.0F)
                .texOffs(53, 104).addBox(-0.5F, -0.7101F, -0.49F, 1.0F, 4.0F, 1.0F)
                .texOffs(58, 104).addBox(-0.5F, 0.281F, -1.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(58, 107).addBox(-0.5F, 2.2899F, -0.99F, 1.0F, 1.0F, 1.0F)
                .texOffs(63, 107).addBox(-0.5F, 2.7899F, -0.99F, 1.0F, 1.0F, 1.0F)
                , PartPose.offsetAndRotation(1.4F, 1.5F, 0.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition part_body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, -10.499F, -2.3F, 8.0F, 12.0F, 4.0F)
                .texOffs(16, 32).addBox(-4.2F, -10.799F, -2.4F, 8.0F, 12.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, 13.5F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_RedKnot = part_body.addOrReplaceChild("RedKnot",
                CubeListBuilder.create()
                .texOffs(53, 110).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 2.0F, 1.0F)
                .texOffs(53, 114).addBox(-2.0F, 0.275F, 0.01F, 3.0F, 2.0F, 1.0F)
                , PartPose.offsetAndRotation(0.5F, -10.4F, -2.3F, -0.1309F, 0.0F, 0.0F));

        PartDefinition part_BackDeco = part_body.addOrReplaceChild("BackDeco",
                CubeListBuilder.create()
                .texOffs(98, 49).addBox(-4.0F, 0.0F, -1.0F, 8.0F, 8.0F, 1.0F)
                .texOffs(98, 59).addBox(-4.0F, 0.0F, -1.0F, 8.0F, 8.0F, 1.0F)
                , PartPose.offsetAndRotation(-0.1F, -5.5F, 2.2F, 0.0873F, 0.0F, 0.0F));

        PartDefinition part_skirt = part_body.addOrReplaceChild("skirt",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.0F, 10.5F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_part2 = part_skirt.addOrReplaceChild("part2",
                CubeListBuilder.create()
                .texOffs(27, 83).addBox(-0.1F, -0.1F, -2.3F, 1.0F, 2.0F, 4.0F)
                .texOffs(27, 111).addBox(-0.1F, -0.1F, -2.3F, 1.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(-3.9F, -9.1F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_part1 = part_skirt.addOrReplaceChild("part1",
                CubeListBuilder.create()
                .texOffs(27, 79).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 2.0F, 1.0F)
                .texOffs(27, 119).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 3.0F, 1.0F)
                , PartPose.offsetAndRotation(0.0F, -9.1F, -2.2F, -0.0873F, 0.0F, 0.0F));

        PartDefinition part_part3 = part_skirt.addOrReplaceChild("part3",
                CubeListBuilder.create()
                .texOffs(27, 90).addBox(-0.9F, -0.1F, -2.3F, 1.0F, 2.0F, 4.0F)
                .texOffs(0, 109).addBox(-0.9F, -0.1F, -2.3F, 1.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(3.9F, -9.1F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_part4 = part_skirt.addOrReplaceChild("part4",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.0F, -9.1F, 2.2F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_bone32 = part_part4.addOrReplaceChild("bone32",
                CubeListBuilder.create()
                .texOffs(11, 76).addBox(-0.1F, -0.1F, -1.5F, 4.0F, 9.0F, 1.0F)
                , PartPose.offsetAndRotation(0.1F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition part_bone33 = part_part4.addOrReplaceChild("bone33",
                CubeListBuilder.create()
                .texOffs(11, 97).addBox(-3.9F, -0.1F, -1.5F, 4.0F, 9.0F, 1.0F)
                , PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition part_RedRibbonBehind = part_body.addOrReplaceChild("RedRibbonBehind",
                CubeListBuilder.create()
                .texOffs(55, 19).addBox(-9.0F, 0.0F, -1.0F, 9.0F, 7.0F, 1.0F)
                .texOffs(55, 28).addBox(-9.0F, 0.0F, -1.0F, 9.0F, 7.0F, 1.0F)
                .texOffs(55, 19).addBox(-9.0F, 0.0F, -0.5F, 9.0F, 7.0F, 1.0F)
                .texOffs(67, 39).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 7.0F, 1.0F)
                .texOffs(67, 48).addBox(-1.0F, 5.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(72, 48).addBox(-2.0F, 6.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(73, 60).addBox(-2.5F, 0.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(73, 63).addBox(-1.5F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(73, 54).addBox(-9.0F, 6.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(62, 38).addBox(-7.5F, 4.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(62, 44).addBox(-5.0F, 3.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(62, 47).addBox(-4.0F, 5.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(62, 47).addBox(-3.5F, 6.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(57, 38).addBox(-4.5F, 4.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(62, 41).addBox(-7.0F, 3.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(62, 41).addBox(-6.5F, 3.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(57, 42).addBox(-6.0F, 2.5F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(73, 57).addBox(-8.5F, 6.0F, -1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(66, 54).addBox(-9.0F, 0.5F, -1.0F, 2.0F, 6.0F, 1.0F)
                .texOffs(66, 51).addBox(-7.0F, 1.0F, -1.0F, 5.0F, 1.0F, 1.0F)
                , PartPose.offsetAndRotation(-4.5F, -8.5F, 1.8F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition part_legLeft = root.addOrReplaceChild("legLeft",
                CubeListBuilder.create()
                .texOffs(82, 55).addBox(-1.751F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(82, 68).addBox(-1.951F, 0.0F, -1.7F, 3.0F, 9.0F, 3.0F)
                , PartPose.offsetAndRotation(2.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_legRight = root.addOrReplaceChild("legRight",
                CubeListBuilder.create()
                .texOffs(82, 42).addBox(-1.249F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(82, 81).addBox(-1.449F, 0.0F, -1.7F, 3.0F, 9.0F, 3.0F)
                , PartPose.offsetAndRotation(-2.0F, 15.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_TrueFlower2 = part_legRight.addOrReplaceChild("TrueFlower2",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-1.9F, 6.6F, 0.5F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_Flower3 = part_TrueFlower2.addOrReplaceChild("Flower3",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.8F, -0.8F, -1.0F, -0.0013F, -0.0334F, -1.5774F));

        PartDefinition part_bone20 = part_Flower3.addOrReplaceChild("bone20",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(0.3657F, 4.3241F, 10.0818F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, 0.0F, 0.0F));

        PartDefinition part_bone21 = part_Flower3.addOrReplaceChild("bone21",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.6678F, 2.677F, 10.3723F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -1.2566F, 0.0F));

        PartDefinition part_bone22 = part_Flower3.addOrReplaceChild("bone22",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.3235F, -1.6097F, 11.1281F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -2.5133F, 0.0F));

        PartDefinition part_bone23 = part_Flower3.addOrReplaceChild("bone23",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-0.6194F, -0.7564F, -0.192F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, -0.2159F, -0.3893F, -1.3963F, 2.5133F, 0.0F));

        PartDefinition part_bone24 = part_Flower3.addOrReplaceChild("bone24",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-1.8615F, -1.6134F, -1.0563F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, 0.7841F, -1.3893F, -1.3963F, 1.2566F, 0.0F));

        PartDefinition part_Flower4 = part_TrueFlower2.addOrReplaceChild("Flower4",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.7F, 0.6F, -0.2F, 0.0264F, -0.0323F, -1.3591F));

        PartDefinition part_bone25 = part_Flower4.addOrReplaceChild("bone25",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(0.3657F, 4.3241F, 10.0818F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, 0.0F, 0.0F));

        PartDefinition part_bone26 = part_Flower4.addOrReplaceChild("bone26",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.6678F, 2.677F, 10.3723F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -1.2566F, 0.0F));

        PartDefinition part_bone27 = part_Flower4.addOrReplaceChild("bone27",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-3.3235F, -1.6097F, 11.1281F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(-0.7226F, -11.2159F, 3.6107F, -1.3963F, -2.5133F, 0.0F));

        PartDefinition part_bone28 = part_Flower4.addOrReplaceChild("bone28",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-0.6194F, -0.7564F, -0.192F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, -0.2159F, -0.3893F, -1.3963F, 2.5133F, 0.0F));

        PartDefinition part_bone29 = part_Flower4.addOrReplaceChild("bone29",
                CubeListBuilder.create()
                .texOffs(43, 70).addBox(-1.8615F, -1.6134F, -1.0563F, 1.0F, 1.0F, 0.0F)
                , PartPose.offsetAndRotation(0.2774F, 0.7841F, -1.3893F, -1.3963F, 1.2566F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(RaidenBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        this.armRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * 0.5F;
        this.armLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
        this.legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

        if (entity.isAttacking()) {
            this.armRight.xRot = -1.2F + Mth.sin(ageInTicks * 0.3F) * 0.4F;
            this.armRight.yRot = -0.3F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.armRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.armLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.legRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.legLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        if (arm == HumanoidArm.RIGHT) {
            this.armRight.translateAndRotate(poseStack);
        } else {
            this.armLeft.translateAndRotate(poseStack);
        }
    }
}
