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

public class RaidenPuppetModel extends EntityModel<RaidenBossEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "raiden_puppet"), "main");

    private final ModelPart bone60;
    private final ModelPart baal;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart armRight;
    private final ModelPart armLeft;
    private final ModelPart legRight;
    private final ModelPart legLeft;
    private final ModelPart puppetStuff;

    public RaidenPuppetModel(ModelPart root) {
        this.bone60 = root.getChild("bone60");
        this.baal = this.bone60.getChild("baal");
        this.head = this.baal.getChild("head");
        this.body = this.baal.getChild("body");
        this.armRight = this.baal.getChild("armRight");
        this.armLeft = this.baal.getChild("armLeft");
        this.legRight = this.baal.getChild("legRight");
        this.legLeft = this.baal.getChild("legLeft");
        this.puppetStuff = this.bone60.hasChild("puppet_stuff") ? this.bone60.getChild("puppet_stuff") : null;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition part_bone60 = root.addOrReplaceChild("bone60",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_baal = part_bone60.addOrReplaceChild("baal",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_head = part_baal.addOrReplaceChild("head",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.1F, -4.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(32, 0).addBox(-4.0F, -8.1F, -4.0F, 8.0F, 8.0F, 8.0F)
                , PartPose.offsetAndRotation(0.0F, -20.9F, 0.0F, 0.0F, 0.0F, 0.0F));

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

        PartDefinition part_armRight = part_baal.addOrReplaceChild("armRight",
                CubeListBuilder.create()
                .texOffs(83, 95).addBox(-0.8823F, 1.0305F, -1.0F, 2.0F, 8.0F, 2.0F)
                .texOffs(82, 106).addBox(-1.3823F, 0.2805F, -1.5F, 3.0F, 8.0F, 3.0F)
                , PartPose.offsetAndRotation(-4.0F, -20.5F, 0.0F, -0.6941F, 0.0749F, 0.5438F));

        PartDefinition part_armLeft = part_baal.addOrReplaceChild("armLeft",
                CubeListBuilder.create()
                .texOffs(97, 82).addBox(-1.6177F, 0.2805F, -1.5F, 3.0F, 8.0F, 3.0F)
                .texOffs(98, 106).addBox(-1.1177F, 1.0307F, -1.0F, 2.0F, 8.0F, 2.0F)
                , PartPose.offsetAndRotation(4.0F, -20.5F, 0.0F, 0.0F, -0.48F, -0.4363F));

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

        PartDefinition part_body = part_baal.addOrReplaceChild("body",
                CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, -10.499F, -2.3F, 8.0F, 12.0F, 4.0F)
                .texOffs(16, 32).addBox(-4.2F, -10.799F, -2.4F, 8.0F, 12.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -10.5F, 0.0F, 0.0F, 0.0F, 0.0F));

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
                , PartPose.offsetAndRotation(0.1F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition part_cube_r1 = part_bone32.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                .texOffs(11, 76).addBox(-2.0F, -0.5F, -0.8F, 4.0F, 9.0F, 1.0F)
                , PartPose.offsetAndRotation(1.9F, 0.4F, -0.7F, 0.2618F, 0.0F, 0.0F));

        PartDefinition part_bone33 = part_part4.addOrReplaceChild("bone33",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition part_cube_r2 = part_bone33.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                .texOffs(11, 97).addBox(-2.0F, -0.5F, -0.8F, 4.0F, 9.0F, 1.0F)
                , PartPose.offsetAndRotation(-1.9F, 0.4F, -0.7F, 0.2618F, 0.0F, 0.0F));

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

        PartDefinition part_legLeft = part_baal.addOrReplaceChild("legLeft",
                CubeListBuilder.create()
                .texOffs(82, 55).addBox(-1.751F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(82, 68).addBox(-1.951F, 0.0F, -1.7F, 3.0F, 9.0F, 3.0F)
                , PartPose.offsetAndRotation(2.0F, -9.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition part_legRight = part_baal.addOrReplaceChild("legRight",
                CubeListBuilder.create()
                .texOffs(82, 42).addBox(-1.249F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(82, 81).addBox(-1.449F, 0.0F, -1.7F, 3.0F, 9.0F, 3.0F)
                , PartPose.offsetAndRotation(-2.0F, -9.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

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

        PartDefinition part_puppet_stuff = part_bone60.addOrReplaceChild("puppet_stuff",
                CubeListBuilder.create()
                .texOffs(215, 104).addBox(-18.0F, -44.0F, 8.0F, 11.0F, 14.0F, 3.0F)
                .texOffs(178, 162).addBox(-6.0F, -49.0F, 14.0F, 2.0F, 10.0F, 1.0F)
                .texOffs(171, 194).addBox(-24.0F, -42.0F, 13.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(210, 197).addBox(-26.0F, -41.0F, 14.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(210, 197).addBox(-20.0F, -41.0F, 14.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(210, 197).addBox(-3.0F, -41.0F, 14.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(210, 197).addBox(-9.0F, -41.0F, 14.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(171, 194).addBox(-7.0F, -42.0F, 13.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(200, 193).addBox(-16.0F, -54.0F, 13.0F, 6.0F, 6.0F, 3.0F)
                .texOffs(210, 197).addBox(-14.0F, -56.0F, 14.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(210, 197).addBox(-14.0F, -48.0F, 14.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(178, 162).addBox(-23.0F, -49.0F, 14.0F, 2.0F, 10.0F, 1.0F)
                .texOffs(178, 162).addBox(-20.0F, -52.0F, 14.0F, 13.0F, 2.0F, 1.0F)
                .texOffs(234, 242).addBox(-19.0F, -42.0F, 6.0F, 8.0F, 11.0F, 3.0F)
                .texOffs(236, 245).addBox(-21.0F, -40.0F, 5.0F, 7.0F, 8.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r3 = part_puppet_stuff.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-1.0F, -2.5F, -1.0F, 3.0F, 4.0F, 3.0F)
                , PartPose.offsetAndRotation(-27.3377F, -24.2898F, -15.0358F, -0.5968F, -0.9721F, -0.6857F));

        PartDefinition part_cube_r4 = part_puppet_stuff.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.1674F, 0.2753F, -0.5769F, 3.0F, 5.0F, 3.0F)
                , PartPose.offsetAndRotation(-25.6738F, -24.6495F, -14.6565F, -0.9905F, -0.5545F, -0.1356F));

        PartDefinition part_cube_r5 = part_puppet_stuff.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-3.0F, -3.0F, 1.5F, 6.0F, 6.0F, 4.0F)
                , PartPose.offsetAndRotation(13.0F, -38.0F, 20.5F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_cube_r6 = part_puppet_stuff.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                .texOffs(178, 162).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F)
                , PartPose.offsetAndRotation(-22.0F, -48.5F, 14.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_cube_r7 = part_puppet_stuff.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                .texOffs(178, 162).addBox(-0.5F, 8.5F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(178, 162).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 9.0F, 1.0F)
                , PartPose.offsetAndRotation(-22.0F, -38.5F, 14.5F, 0.0F, 0.0F, -0.3054F));

        PartDefinition part_cube_r8 = part_puppet_stuff.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                .texOffs(178, 162).addBox(-1.0F, 8.2F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(178, 162).addBox(-1.3F, -0.8F, -0.5F, 2.0F, 9.0F, 1.0F)
                , PartPose.offsetAndRotation(-5.0F, -38.5F, 14.5F, 0.0F, 0.0F, 0.3054F));

        PartDefinition part_cube_r9 = part_puppet_stuff.addOrReplaceChild("cube_r9",
                CubeListBuilder.create()
                .texOffs(178, 162).addBox(-0.5F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F)
                , PartPose.offsetAndRotation(-5.0F, -48.0F, 14.5F, 0.0F, 0.0F, -0.7854F));

        PartDefinition part_cube_r10 = part_puppet_stuff.addOrReplaceChild("cube_r10",
                CubeListBuilder.create()
                .texOffs(210, 197).addBox(-4.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(210, 197).addBox(2.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(171, 194).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 3.0F)
                , PartPose.offsetAndRotation(-5.0F, -47.0F, 14.5F, 0.0F, 0.0F, -0.3491F));

        PartDefinition part_cube_r11 = part_puppet_stuff.addOrReplaceChild("cube_r11",
                CubeListBuilder.create()
                .texOffs(210, 197).addBox(-4.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(210, 197).addBox(2.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(171, 194).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 3.0F)
                , PartPose.offsetAndRotation(-22.0F, -47.0F, 14.5F, 0.0F, 0.0F, 0.3491F));

        PartDefinition part_bone47 = part_puppet_stuff.addOrReplaceChild("bone47",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(13.0F, -45.3325F, 3.5553F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r12 = part_bone47.addOrReplaceChild("cube_r12",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-2.0F, -1.7102F, -14.0391F, 4.0F, 4.0F, 15.0F)
                , PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1916F, 0.2815F, -0.1201F));

        PartDefinition part_cube_r13 = part_bone47.addOrReplaceChild("cube_r13",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-2.0F, -2.0F, -17.5F, 4.0F, 4.0F, 19.0F)
                .texOffs(6, 218).addBox(-1.0F, -1.0F, 5.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, 7.3325F, 16.9447F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone48 = part_puppet_stuff.addOrReplaceChild("bone48",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-0.4725F, -19.1925F, 12.6245F, 0.0F, 0.0F, 0.7854F));

        PartDefinition part_cube_r14 = part_bone48.addOrReplaceChild("cube_r14",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-2.0F, -1.7102F, -14.0391F, 4.0F, 4.0F, 15.0F)
                , PartPose.offsetAndRotation(2.7549F, -22.2562F, -9.0693F, -0.1916F, 0.2815F, -0.1201F));

        PartDefinition part_cube_r15 = part_bone48.addOrReplaceChild("cube_r15",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-2.0F, -2.0F, -17.5F, 4.0F, 4.0F, 19.0F)
                .texOffs(6, 218).addBox(-1.0F, -1.0F, 1.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(2.7549F, -14.9237F, 7.8755F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone49 = part_puppet_stuff.addOrReplaceChild("bone49",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(17.2809F, -17.8443F, 12.6245F, 0.2132F, 0.0469F, 1.314F));

        PartDefinition part_cube_r16 = part_bone49.addOrReplaceChild("cube_r16",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-2.0F, -1.7102F, -14.0391F, 4.0F, 4.0F, 15.0F)
                , PartPose.offsetAndRotation(0.6336F, -4.5785F, -9.0693F, -0.1916F, 0.2815F, -0.1201F));

        PartDefinition part_cube_r17 = part_bone49.addOrReplaceChild("cube_r17",
                CubeListBuilder.create()
                .texOffs(6, 218).addBox(-2.0F, -2.0F, -17.5F, 4.0F, 4.0F, 19.0F)
                .texOffs(6, 218).addBox(-1.0F, -1.0F, 1.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.6336F, 2.754F, 7.8755F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_rotate_puppet_arm = part_puppet_stuff.addOrReplaceChild("rotate_puppet_arm",
                CubeListBuilder.create()
                .texOffs(164, 236).addBox(-2.5F, -1.5F, -3.0F, 5.0F, 15.0F, 5.0F)
                , PartPose.offsetAndRotation(-19.5F, -34.5F, 6.0F, -0.413F, -0.1451F, 0.9904F));

        PartDefinition part_rotate_puppet_arm2 = part_puppet_stuff.addOrReplaceChild("rotate_puppet_arm2",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(-27.5F, -32.5F, 0.0F, -0.413F, -0.1451F, 0.9904F));

        PartDefinition part_cube_r18 = part_rotate_puppet_arm2.addOrReplaceChild("cube_r18",
                CubeListBuilder.create()
                .texOffs(138, 162).addBox(-3.7147F, -0.0075F, -3.7396F, 6.0F, 10.0F, 8.0F)
                , PartPose.offsetAndRotation(2.9147F, 10.486F, -4.2335F, -1.027F, -0.2324F, 0.2F));

        PartDefinition part_cube_r19 = part_rotate_puppet_arm2.addOrReplaceChild("cube_r19",
                CubeListBuilder.create()
                .texOffs(124, 239).addBox(-3.5F, 0.5F, -2.0F, 6.0F, 11.0F, 6.0F)
                , PartPose.offsetAndRotation(3.5F, 2.0F, 2.0F, -0.7F, -0.0668F, 0.0562F));

        PartDefinition part_bone42 = part_rotate_puppet_arm2.addOrReplaceChild("bone42",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(0.7354F, 21.4335F, -15.1099F, 0.0306F, -0.1719F, -0.1772F));

        PartDefinition part_bone44 = part_bone42.addOrReplaceChild("bone44",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(3.4647F, -5.2421F, 0.7208F, 0.6545F, 0.0F, 0.0F));

        PartDefinition part_cube_r20 = part_bone44.addOrReplaceChild("cube_r20",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-1.6843F, -1.572F, -0.393F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(2.5353F, 7.2421F, -4.7208F, 0.5404F, -0.9433F, -1.8702F));

        PartDefinition part_cube_r21 = part_bone44.addOrReplaceChild("cube_r21",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.3531F, -6.9497F, -0.393F, 2.0F, 7.0F, 2.0F)
                , PartPose.offsetAndRotation(2.5353F, 7.2421F, -4.7208F, -0.7717F, -0.792F, -0.2932F));

        PartDefinition part_cube_r22 = part_bone44.addOrReplaceChild("cube_r22",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.2024F, -2.7726F, -0.3072F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(-1.7262F, 3.4445F, -1.0007F, -1.027F, -0.2324F, 0.2F));

        PartDefinition part_bone45 = part_bone42.addOrReplaceChild("bone45",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(4.4909F, -5.8568F, 2.268F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_cube_r23 = part_bone45.addOrReplaceChild("cube_r23",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-1.5712F, -1.4229F, -3.5497F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(1.5091F, 8.8568F, -4.268F, 0.5404F, -0.9433F, -1.8702F));

        PartDefinition part_cube_r24 = part_bone45.addOrReplaceChild("cube_r24",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.166F, -6.9568F, -3.5497F, 2.0F, 7.0F, 2.0F)
                , PartPose.offsetAndRotation(1.5091F, 8.8568F, -4.268F, -0.7717F, -0.792F, -0.2932F));

        PartDefinition part_cube_r25 = part_bone45.addOrReplaceChild("cube_r25",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.2785F, -3.0932F, -3.4523F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(-2.7524F, 5.0591F, -0.5479F, -1.027F, -0.2324F, 0.2F));

        PartDefinition part_bone43 = part_bone42.addOrReplaceChild("bone43",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(5.4909F, -8.8568F, 2.268F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_cube_r26 = part_bone43.addOrReplaceChild("cube_r26",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-1.5712F, -1.4229F, -3.5497F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(1.5091F, 8.8568F, -4.268F, 0.5404F, -0.9433F, -1.8702F));

        PartDefinition part_cube_r27 = part_bone43.addOrReplaceChild("cube_r27",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.166F, -6.9568F, -3.5497F, 2.0F, 7.0F, 2.0F)
                , PartPose.offsetAndRotation(1.5091F, 8.8568F, -4.268F, -0.7717F, -0.792F, -0.2932F));

        PartDefinition part_cube_r28 = part_bone43.addOrReplaceChild("cube_r28",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.2785F, -3.0932F, -3.4523F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(-2.7524F, 5.0591F, -0.5479F, -1.027F, -0.2324F, 0.2F));

        PartDefinition part_bone46 = part_bone42.addOrReplaceChild("bone46",
                CubeListBuilder.create()
                , PartPose.offsetAndRotation(6.4909F, -9.8568F, 0.268F, 0.0F, 0.0F, 0.0F));

        PartDefinition part_cube_r29 = part_bone46.addOrReplaceChild("cube_r29",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-1.5712F, -1.4229F, -3.5497F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(1.5091F, 8.8568F, -4.268F, 0.5404F, -0.9433F, -1.8702F));

        PartDefinition part_cube_r30 = part_bone46.addOrReplaceChild("cube_r30",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.166F, -6.9568F, -3.5497F, 2.0F, 7.0F, 2.0F)
                , PartPose.offsetAndRotation(1.5091F, 8.8568F, -4.268F, -0.7717F, -0.792F, -0.2932F));

        PartDefinition part_cube_r31 = part_bone46.addOrReplaceChild("cube_r31",
                CubeListBuilder.create()
                .texOffs(220, 168).addBox(-2.2785F, -3.0932F, -3.4523F, 2.0F, 5.0F, 2.0F)
                , PartPose.offsetAndRotation(-2.7524F, 5.0591F, -0.5479F, -1.027F, -0.2324F, 0.2F));

        PartDefinition part_bone54 = part_puppet_stuff.addOrReplaceChild("bone54",
                CubeListBuilder.create()
                .texOffs(206, 0).addBox(-6.0F, -1.5F, -6.5F, 12.0F, 3.0F, 13.0F)
                , PartPose.offsetAndRotation(-8.0F, -3.5F, -8.5F, 0.3193F, 0.2909F, 0.0945F));

        PartDefinition part_cube_r32 = part_bone54.addOrReplaceChild("cube_r32",
                CubeListBuilder.create()
                .texOffs(237, 56).addBox(-4.4129F, -1.5F, -1.4383F, 5.0F, 3.0F, 3.0F)
                , PartPose.offsetAndRotation(-10.5669F, 0.0F, -1.5296F, 0.0F, -1.3526F, 0.0F));

        PartDefinition part_cube_r33 = part_bone54.addOrReplaceChild("cube_r33",
                CubeListBuilder.create()
                .texOffs(150, 20).addBox(-6.5F, -1.49F, -1.5F, 9.0F, 3.0F, 3.0F)
                , PartPose.offsetAndRotation(-6.5F, 0.0F, 3.0F, 0.0F, -0.829F, 0.0F));

        PartDefinition part_bone50 = part_bone54.addOrReplaceChild("bone50",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.01F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(-5.0F, -0.1982F, -5.7828F, 0.1331F, 0.1264F, 0.0341F));

        PartDefinition part_cube_r34 = part_bone50.addOrReplaceChild("cube_r34",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.01F, -2.9912F, -0.5328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r35 = part_bone50.addOrReplaceChild("cube_r35",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.01F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone51 = part_bone54.addOrReplaceChild("bone51",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.0F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(-2.0F, -0.1982F, -5.7828F, -0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r36 = part_bone51.addOrReplaceChild("cube_r36",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.0F, -2.5212F, -1.8328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r37 = part_bone51.addOrReplaceChild("cube_r37",
                CubeListBuilder.create()
                .texOffs(166, 53).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone53 = part_bone54.addOrReplaceChild("bone53",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.0F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(5.0F, -0.1982F, -5.7828F, -0.0866F, -0.1744F, -0.0077F));

        PartDefinition part_cube_r38 = part_bone53.addOrReplaceChild("cube_r38",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.0F, -2.5212F, -1.8328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r39 = part_bone53.addOrReplaceChild("cube_r39",
                CubeListBuilder.create()
                .texOffs(166, 53).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone52 = part_bone54.addOrReplaceChild("bone52",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.0F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(2.0F, -0.1982F, -5.7828F, -0.3054F, 0.0F, 0.0F));

        PartDefinition part_cube_r40 = part_bone52.addOrReplaceChild("cube_r40",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.0F, -1.6212F, -3.5328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r41 = part_bone52.addOrReplaceChild("cube_r41",
                CubeListBuilder.create()
                .texOffs(166, 53).addBox(-1.0F, -1.0F, -5.5F, 2.0F, 2.0F, 6.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone55 = part_puppet_stuff.addOrReplaceChild("bone55",
                CubeListBuilder.create()
                .texOffs(206, 0).mirror(true).addBox(-6.0F, -1.5F, -6.5F, 12.0F, 3.0F, 13.0F)
                , PartPose.offsetAndRotation(11.0F, -3.5F, -8.5F, 0.3193F, -0.2909F, -0.0945F));

        PartDefinition part_cube_r42 = part_bone55.addOrReplaceChild("cube_r42",
                CubeListBuilder.create()
                .texOffs(237, 56).mirror(true).addBox(-0.4129F, -1.5F, -1.4383F, 5.0F, 3.0F, 3.0F)
                , PartPose.offsetAndRotation(10.4331F, 0.0F, -1.5296F, 0.0F, 1.3526F, 0.0F));

        PartDefinition part_cube_r43 = part_bone55.addOrReplaceChild("cube_r43",
                CubeListBuilder.create()
                .texOffs(150, 20).mirror(true).addBox(-2.5F, -1.49F, -1.5F, 9.0F, 3.0F, 3.0F)
                , PartPose.offsetAndRotation(6.5F, 0.0F, 3.0F, 0.0F, 0.829F, 0.0F));

        PartDefinition part_bone56 = part_bone55.addOrReplaceChild("bone56",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.01F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(-5.0F, -0.1982F, -5.7828F, 0.1331F, 0.1264F, 0.0341F));

        PartDefinition part_cube_r44 = part_bone56.addOrReplaceChild("cube_r44",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.01F, -2.9912F, -0.5328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r45 = part_bone56.addOrReplaceChild("cube_r45",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.01F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone57 = part_bone55.addOrReplaceChild("bone57",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.0F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(-2.0F, -0.1982F, -5.7828F, -0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r46 = part_bone57.addOrReplaceChild("cube_r46",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.0F, -2.5212F, -1.8328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r47 = part_bone57.addOrReplaceChild("cube_r47",
                CubeListBuilder.create()
                .texOffs(166, 53).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone58 = part_bone55.addOrReplaceChild("bone58",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.0F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(5.0F, -0.1982F, -5.7828F, -0.0866F, -0.1744F, -0.0077F));

        PartDefinition part_cube_r48 = part_bone58.addOrReplaceChild("cube_r48",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.0F, -2.5212F, -1.8328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r49 = part_bone58.addOrReplaceChild("cube_r49",
                CubeListBuilder.create()
                .texOffs(166, 53).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_bone59 = part_bone55.addOrReplaceChild("bone59",
                CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.0F, -0.8018F, -4.7172F, 2.0F, 2.0F, 5.0F)
                , PartPose.offsetAndRotation(2.0F, -0.1982F, -5.7828F, -0.3054F, 0.0F, 0.0F));

        PartDefinition part_cube_r50 = part_bone59.addOrReplaceChild("cube_r50",
                CubeListBuilder.create()
                .texOffs(213, 41).addBox(-1.0F, -1.6212F, -3.5328F, 2.0F, 2.0F, 4.0F)
                , PartPose.offsetAndRotation(0.0F, -1.4806F, -9.6844F, -0.829F, 0.0F, 0.0F));

        PartDefinition part_cube_r51 = part_bone59.addOrReplaceChild("cube_r51",
                CubeListBuilder.create()
                .texOffs(166, 53).addBox(-1.0F, -1.0F, -5.5F, 2.0F, 2.0F, 6.0F)
                , PartPose.offsetAndRotation(0.0F, 0.1982F, -4.2172F, -0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(RaidenBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        float hoverBob = Mth.sin(ageInTicks * 0.08F) * 2.0F;
        this.bone60.y = 12.0F + hoverBob;

        this.legRight.xRot = 0.25F + Mth.sin(ageInTicks * 0.06F) * 0.04F;
        this.legRight.yRot = -0.05F;
        this.legRight.zRot = 0.08F;

        this.legLeft.xRot = 0.35F - Mth.sin(ageInTicks * 0.06F) * 0.04F;
        this.legLeft.yRot = 0.05F;
        this.legLeft.zRot = -0.08F;

        if (this.puppetStuff != null) {
            float floatWave = Mth.sin(ageInTicks * 0.1F);
            this.puppetStuff.y = floatWave * 4.0F;
            this.puppetStuff.zRot = Mth.cos(ageInTicks * 0.08F) * 0.06F;
        }

        if (entity.isAttacking()) {
            this.armRight.xRot = -1.5F + Mth.sin(ageInTicks * 0.4F) * 0.5F;
            this.armRight.yRot = -0.4F;
        } else {
            this.armRight.xRot = -0.2F + Mth.cos(ageInTicks * 0.05F) * 0.06F;
            this.armRight.yRot = 0.0F;
            this.armLeft.xRot = -0.2F - Mth.cos(ageInTicks * 0.05F) * 0.06F;
            this.armLeft.yRot = 0.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.bone60.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        poseStack.scale(0.0F, 0.0F, 0.0F);
    }
}
