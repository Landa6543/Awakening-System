package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AzhdahaBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AzhdahaModel extends EntityModel<AzhdahaBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "azhdaha_boss"), "main");

    private final ModelPart root;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightHand;
    private final ModelPart leftHand;
    private final ModelPart neck;
    private final ModelPart body;

    public AzhdahaModel(ModelPart root) {
        this.root = root;
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightHand = root.getChild("right_hand");
        this.leftHand = root.getChild("left_hand");
        this.neck = root.getChild("neck");
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        add_right_leg(root);
        add_left_leg(root);
        add_right_hand(root);
        add_left_hand(root);
        add_neck(root);
        add_body(root);

        return LayerDefinition.create(mesh, 816, 816);
    }

    private static void add_right_leg(PartDefinition root) {
        PartDefinition part_right_leg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                .texOffs(560, 61).mirror(true).addBox(-18.7797F, -13.0779F, -19.5906F, 28.0F, 34.0F, 40.0F),
                PartPose.offsetAndRotation(-55.9889F, -53.9414F, 70.3295F, 0.1309F, 0.0F, 0.3054F));

        PartDefinition part_right_leg2 = part_right_leg.addOrReplaceChild("right_leg2",
                CubeListBuilder.create()
                .texOffs(408, 633).mirror(true).addBox(-17.4722F, -0.4569F, -13.5111F, 27.0F, 29.0F, 31.0F),
                PartPose.offsetAndRotation(-2.5111F, 14.4414F, -5.3295F, -0.1309F, 0.0F, -0.3927F));

        PartDefinition part_right_leg3 = part_right_leg2.addOrReplaceChild("right_leg3",
                CubeListBuilder.create()
                .texOffs(181, 477).mirror(true).addBox(-13.4886F, -0.3365F, -11.2803F, 18.0F, 19.0F, 20.0F)
                .texOffs(226, 423).mirror(true).addBox(-14.4886F, 18.6635F, -12.2803F, 21.0F, 16.0F, 22.0F)
                .texOffs(347, 568).mirror(true).addBox(-15.4886F, 18.6635F, -13.2803F, 23.0F, 9.0F, 25.0F),
                PartPose.offsetAndRotation(0.0F, 31.25F, -5.0F, 1.4842F, 0.0151F, 0.0859F));

        PartDefinition part_right_leg4 = part_right_leg3.addOrReplaceChild("right_leg4",
                CubeListBuilder.create()
                .texOffs(689, 296).mirror(true).addBox(-12.9886F, -0.335F, -25.2068F, 18.0F, 16.0F, 32.0F),
                PartPose.offsetAndRotation(0.0F, 32.0F, -5.0F, -0.0431F, 0.0018F, 0.0097F));

        PartDefinition part_cube_r1 = part_right_leg4.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                .texOffs(593, 255).mirror(true).addBox(-1.5F, -13.0F, -5.5F, 2.0F, 23.0F, 12.0F),
                PartPose.offsetAndRotation(-0.9064F, 16.9358F, 9.2383F, -2.2689F, 0.0F, 0.0F));

        PartDefinition part_lower_right_leg = part_right_leg4.addOrReplaceChild("lower_right_leg",
                CubeListBuilder.create()
                .texOffs(419, 201).mirror(true).addBox(-16.503F, -9.3535F, -4.7362F, 23.0F, 19.0F, 8.0F),
                PartPose.offsetAndRotation(1.0144F, 0.017F, -21.5441F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_cube_r2 = part_lower_right_leg.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(true).addBox(8.453F, 0.0F, -17.3309F, 2.0F, 1.0F, 2.0F)
                .texOffs(0, 114).mirror(true).addBox(7.453F, 0.0F, -15.3309F, 4.0F, 1.0F, 1.0F)
                .texOffs(126, 392).mirror(true).addBox(6.453F, -2.0F, -14.3309F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(-4.9875F, -10.9432F, -3.527F, -1.5875F, 0.0403F, 1.1778F));

        PartDefinition part_cube_r3 = part_lower_right_leg.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                .texOffs(78, 564).mirror(true).addBox(4.0667F, 0.0F, -11.7652F, 4.0F, 1.0F, 11.0F)
                .texOffs(416, 526).mirror(true).addBox(3.0667F, -1.0F, -8.7652F, 6.0F, 2.0F, 10.0F)
                .texOffs(644, 461).mirror(true).addBox(2.0667F, -3.0F, -6.7652F, 8.0F, 4.0F, 11.0F)
                .texOffs(78, 564).mirror(true).addBox(-7.9333F, 0.0F, -11.7652F, 4.0F, 1.0F, 11.0F)
                .texOffs(416, 526).mirror(true).addBox(-8.9333F, -1.0F, -8.7652F, 6.0F, 2.0F, 10.0F)
                .texOffs(644, 461).mirror(true).addBox(-9.9333F, -3.0F, -6.7652F, 8.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(-4.9875F, -10.9432F, -3.527F, -1.6144F, 0.0F, 0.0F));

        PartDefinition part_cube_r4 = part_lower_right_leg.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(true).addBox(-9.0254F, 0.0F, -18.4761F, 2.0F, 1.0F, 2.0F)
                .texOffs(0, 114).mirror(true).addBox(-10.0254F, 0.0F, -16.4761F, 4.0F, 1.0F, 1.0F)
                .texOffs(126, 392).mirror(true).addBox(-11.0254F, -2.0F, -15.4761F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(-4.9875F, -10.9432F, -3.527F, -1.5821F, -0.0421F, -1.3088F));

        PartDefinition part_cube_r5 = part_lower_right_leg.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                .texOffs(231, 227).mirror(true).addBox(-5.0F, -2.5F, -7.0F, 10.0F, 5.0F, 14.0F),
                PartPose.offsetAndRotation(-11.003F, -7.3535F, -1.9862F, -1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r6 = part_lower_right_leg.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                .texOffs(444, 228).mirror(true).addBox(-5.0F, -2.5F, -7.0F, 10.0F, 5.0F, 14.0F),
                PartPose.offsetAndRotation(0.997F, -7.3535F, -1.9862F, -1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r7 = part_lower_right_leg.addOrReplaceChild("cube_r7",
                CubeListBuilder.create()
                .texOffs(671, 0).mirror(true).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(3.9405F, -4.6766F, -2.4862F, -1.5708F, 0.0F, 1.309F));

        PartDefinition part_cube_r8 = part_lower_right_leg.addOrReplaceChild("cube_r8",
                CubeListBuilder.create()
                .texOffs(673, 371).mirror(true).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(-13.8852F, -4.6419F, -2.4862F, -1.5708F, 0.0F, -1.309F));

    }

    private static void add_left_leg(PartDefinition root) {
        PartDefinition part_left_leg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                .texOffs(560, 61).mirror(false).addBox(-9.2203F, -13.0779F, -19.5906F, 28.0F, 34.0F, 40.0F),
                PartPose.offsetAndRotation(55.9889F, -53.9414F, 70.3295F, 0.1309F, 0.0F, -0.3054F));

        PartDefinition part_left_leg2 = part_left_leg.addOrReplaceChild("left_leg2",
                CubeListBuilder.create()
                .texOffs(408, 633).mirror(false).addBox(-9.5278F, -0.4569F, -13.5111F, 27.0F, 29.0F, 31.0F),
                PartPose.offsetAndRotation(2.5111F, 14.4414F, -5.3295F, -0.1309F, 0.0F, 0.3927F));

        PartDefinition part_left_leg3 = part_left_leg2.addOrReplaceChild("left_leg3",
                CubeListBuilder.create()
                .texOffs(181, 477).mirror(false).addBox(-4.5114F, -0.3365F, -11.2803F, 18.0F, 19.0F, 20.0F)
                .texOffs(226, 423).mirror(false).addBox(-6.5114F, 18.6635F, -12.2803F, 21.0F, 16.0F, 22.0F)
                .texOffs(347, 568).mirror(false).addBox(-7.5114F, 18.6635F, -13.2803F, 23.0F, 9.0F, 25.0F),
                PartPose.offsetAndRotation(0.0F, 31.25F, -5.0F, 1.4842F, -0.0151F, -0.0859F));

        PartDefinition part_left_leg4 = part_left_leg3.addOrReplaceChild("left_leg4",
                CubeListBuilder.create()
                .texOffs(689, 296).mirror(false).addBox(-5.0114F, -0.335F, -25.2068F, 18.0F, 16.0F, 32.0F),
                PartPose.offsetAndRotation(0.0F, 32.0F, -5.0F, -0.0431F, -0.0018F, -0.0097F));

        PartDefinition part_cube_r9 = part_left_leg4.addOrReplaceChild("cube_r9",
                CubeListBuilder.create()
                .texOffs(593, 255).mirror(false).addBox(-0.5F, -13.0F, -5.5F, 2.0F, 23.0F, 12.0F),
                PartPose.offsetAndRotation(0.9064F, 16.9358F, 9.2383F, -2.2689F, 0.0F, 0.0F));

        PartDefinition part_lower_left_leg = part_left_leg4.addOrReplaceChild("lower_left_leg",
                CubeListBuilder.create()
                .texOffs(419, 201).mirror(false).addBox(-6.497F, -9.3535F, -4.7362F, 23.0F, 19.0F, 8.0F),
                PartPose.offsetAndRotation(-1.0144F, 0.017F, -21.5441F, 0.1309F, 0.0F, 0.0F));

        PartDefinition part_cube_r10 = part_lower_left_leg.addOrReplaceChild("cube_r10",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(false).addBox(-10.453F, 0.0F, -17.3309F, 2.0F, 1.0F, 2.0F)
                .texOffs(0, 114).mirror(false).addBox(-11.453F, 0.0F, -15.3309F, 4.0F, 1.0F, 1.0F)
                .texOffs(126, 392).mirror(false).addBox(-12.453F, -2.0F, -14.3309F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(4.9875F, -10.9432F, -3.527F, -1.5875F, -0.0403F, -1.1778F));

        PartDefinition part_cube_r11 = part_lower_left_leg.addOrReplaceChild("cube_r11",
                CubeListBuilder.create()
                .texOffs(78, 564).mirror(false).addBox(-8.0667F, 0.0F, -11.7652F, 4.0F, 1.0F, 11.0F)
                .texOffs(416, 526).mirror(false).addBox(-9.0667F, -1.0F, -8.7652F, 6.0F, 2.0F, 10.0F)
                .texOffs(644, 461).mirror(false).addBox(-10.0667F, -3.0F, -6.7652F, 8.0F, 4.0F, 11.0F)
                .texOffs(78, 564).mirror(false).addBox(3.9333F, 0.0F, -11.7652F, 4.0F, 1.0F, 11.0F)
                .texOffs(416, 526).mirror(false).addBox(2.9333F, -1.0F, -8.7652F, 6.0F, 2.0F, 10.0F)
                .texOffs(644, 461).mirror(false).addBox(1.9333F, -3.0F, -6.7652F, 8.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(4.9875F, -10.9432F, -3.527F, -1.6144F, 0.0F, 0.0F));

        PartDefinition part_cube_r12 = part_lower_left_leg.addOrReplaceChild("cube_r12",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(false).addBox(7.0254F, 0.0F, -18.4761F, 2.0F, 1.0F, 2.0F)
                .texOffs(0, 114).mirror(false).addBox(6.0254F, 0.0F, -16.4761F, 4.0F, 1.0F, 1.0F)
                .texOffs(126, 392).mirror(false).addBox(5.0254F, -2.0F, -15.4761F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(4.9875F, -10.9432F, -3.527F, -1.5821F, 0.0421F, 1.3088F));

        PartDefinition part_cube_r13 = part_lower_left_leg.addOrReplaceChild("cube_r13",
                CubeListBuilder.create()
                .texOffs(231, 227).mirror(false).addBox(-5.0F, -2.5F, -7.0F, 10.0F, 5.0F, 14.0F),
                PartPose.offsetAndRotation(11.003F, -7.3535F, -1.9862F, -1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r14 = part_lower_left_leg.addOrReplaceChild("cube_r14",
                CubeListBuilder.create()
                .texOffs(444, 228).mirror(false).addBox(-5.0F, -2.5F, -7.0F, 10.0F, 5.0F, 14.0F),
                PartPose.offsetAndRotation(-0.997F, -7.3535F, -1.9862F, -1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r15 = part_lower_left_leg.addOrReplaceChild("cube_r15",
                CubeListBuilder.create()
                .texOffs(671, 0).mirror(false).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(-3.9405F, -4.6766F, -2.4862F, -1.5708F, 0.0F, -1.309F));

        PartDefinition part_cube_r16 = part_lower_left_leg.addOrReplaceChild("cube_r16",
                CubeListBuilder.create()
                .texOffs(673, 371).mirror(false).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(13.8852F, -4.6419F, -2.4862F, -1.5708F, 0.0F, 1.309F));

    }

    private static void add_right_hand(PartDefinition root) {
        PartDefinition part_right_hand = root.addOrReplaceChild("right_hand",
                CubeListBuilder.create()
                .texOffs(604, 393).mirror(true).addBox(-15.8427F, -18.5368F, -13.1699F, 28.0F, 34.0F, 34.0F),
                PartPose.offsetAndRotation(-59.0F, -43.5F, -82.0F, 0.5236F, 0.0F, 0.829F));

        PartDefinition part_right_hand2 = part_right_hand.addOrReplaceChild("right_hand2",
                CubeListBuilder.create()
                .texOffs(632, 476).mirror(true).addBox(-14.378F, -0.8075F, -13.8432F, 24.0F, 31.0F, 32.0F),
                PartPose.offset(0.5352F, 3.2707F, 1.6733F));

        PartDefinition part_right_hand3 = part_right_hand2.addOrReplaceChild("right_hand3",
                CubeListBuilder.create()
                .texOffs(214, 632).mirror(true).addBox(-15.4417F, 0.2545F, -13.5703F, 23.0F, 44.0F, 30.0F)
                .texOffs(610, 573).mirror(true).addBox(-16.4417F, 3.2545F, -14.5703F, 25.0F, 41.0F, 32.0F)
                .texOffs(126, 634).mirror(true).addBox(-11.4417F, -7.7455F, -9.5703F, 15.0F, 8.0F, 22.0F)
                .texOffs(258, 349).mirror(true).addBox(-11.4417F, 0.2545F, 16.4297F, 15.0F, 9.0F, 7.0F),
                PartPose.offsetAndRotation(3.0F, 27.25F, 1.0F, -1.0069F, -0.231F, -0.8029F));

        PartDefinition part_cube_r17 = part_right_hand3.addOrReplaceChild("cube_r17",
                CubeListBuilder.create()
                .texOffs(596, 393).mirror(true).addBox(-1.5F, -11.5F, -5.5F, 3.0F, 23.0F, 11.0F),
                PartPose.offsetAndRotation(-22.4731F, 6.2294F, 0.8587F, 0.1124F, 0.0354F, -0.7965F));

        PartDefinition part_cube_r18 = part_right_hand3.addOrReplaceChild("cube_r18",
                CubeListBuilder.create()
                .texOffs(195, 287).mirror(true).addBox(-1.5F, -12.5F, -5.5F, 3.0F, 25.0F, 11.0F),
                PartPose.offsetAndRotation(-16.2817F, 18.4181F, 19.6901F, -0.6768F, -0.6188F, -0.0442F));

        PartDefinition part_cube_r19 = part_right_hand3.addOrReplaceChild("cube_r19",
                CubeListBuilder.create()
                .texOffs(596, 393).mirror(true).addBox(-1.5F, -11.5F, -5.5F, 3.0F, 23.0F, 11.0F),
                PartPose.offsetAndRotation(-3.9417F, -5.4496F, 24.4155F, -0.5672F, 0.0F, 0.0F));

        PartDefinition part_lower_right_hand = part_right_hand3.addOrReplaceChild("lower_right_hand",
                CubeListBuilder.create()
                .texOffs(714, 0).mirror(true).addBox(-15.9417F, 0.8031F, -7.7596F, 24.0F, 8.0F, 22.0F)
                .texOffs(649, 197).mirror(true).addBox(-2.9417F, 3.8031F, -11.7596F, 10.0F, 5.0F, 14.0F)
                .texOffs(485, 345).mirror(true).addBox(-14.9417F, 3.8031F, -11.7596F, 10.0F, 5.0F, 14.0F)
                .texOffs(644, 461).mirror(true).addBox(-13.9417F, 4.8031F, -14.7596F, 8.0F, 4.0F, 11.0F)
                .texOffs(416, 526).mirror(true).addBox(-12.9417F, 6.8031F, -16.7596F, 6.0F, 2.0F, 10.0F)
                .texOffs(78, 564).mirror(true).addBox(-11.9417F, 7.8031F, -19.7596F, 4.0F, 1.0F, 11.0F)
                .texOffs(644, 461).mirror(true).addBox(-1.9417F, 4.8031F, -14.7596F, 8.0F, 4.0F, 11.0F)
                .texOffs(416, 526).mirror(true).addBox(-0.9417F, 6.8031F, -16.7596F, 6.0F, 2.0F, 10.0F)
                .texOffs(78, 564).mirror(true).addBox(0.0583F, 7.8031F, -19.7596F, 4.0F, 1.0F, 11.0F)
                .texOffs(199, 556).mirror(true).addBox(-14.9417F, -15.1969F, -2.7596F, 22.0F, 16.0F, 17.0F)
                .texOffs(0, 235).mirror(true).addBox(-14.9417F, -8.1969F, -5.7596F, 22.0F, 9.0F, 3.0F)
                .texOffs(314, 46).mirror(true).addBox(-14.9417F, -11.1969F, -3.7596F, 22.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 49.0F, -0.75F, 0.6545F, 0.0F, 0.0F));

        PartDefinition part_cube_r20 = part_lower_right_hand.addOrReplaceChild("cube_r20",
                CubeListBuilder.create()
                .texOffs(532, 683).mirror(true).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(5.0018F, 6.8031F, -2.0827F, 0.0F, -1.309F, 0.0F));

        PartDefinition part_cube_r21 = part_lower_right_hand.addOrReplaceChild("cube_r21",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(true).addBox(11.0F, -0.1667F, 4.1667F, 2.0F, 1.0F, 2.0F)
                .texOffs(0, 114).mirror(true).addBox(10.0F, -0.1667F, 6.1667F, 4.0F, 1.0F, 1.0F)
                .texOffs(126, 392).mirror(true).addBox(9.0F, -2.1667F, 7.1667F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(14.8781F, 7.9698F, -18.5743F, 0.0F, -1.1781F, 0.0F));

        PartDefinition part_cube_r22 = part_lower_right_hand.addOrReplaceChild("cube_r22",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(true).addBox(-1.0F, -0.5F, -5.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-19.1024F, 8.3031F, -3.7303F, 0.0F, 1.309F, 0.0F));

        PartDefinition part_cube_r23 = part_lower_right_hand.addOrReplaceChild("cube_r23",
                CubeListBuilder.create()
                .texOffs(0, 114).mirror(true).addBox(-2.0F, -0.5F, -4.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-17.6535F, 8.3031F, -3.3421F, 0.0F, 1.309F, 0.0F));

        PartDefinition part_cube_r24 = part_lower_right_hand.addOrReplaceChild("cube_r24",
                CubeListBuilder.create()
                .texOffs(126, 392).mirror(true).addBox(-3.0F, -1.5F, -5.0F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(-16.2046F, 7.3031F, -2.9539F, 0.0F, 1.309F, 0.0F));

        PartDefinition part_cube_r25 = part_lower_right_hand.addOrReplaceChild("cube_r25",
                CubeListBuilder.create()
                .texOffs(689, 184).mirror(true).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(-12.8239F, 6.8031F, -2.048F, 0.0F, 1.309F, 0.0F));

    }

    private static void add_left_hand(PartDefinition root) {
        PartDefinition part_left_hand = root.addOrReplaceChild("left_hand",
                CubeListBuilder.create()
                .texOffs(604, 393).mirror(false).addBox(-12.1573F, -18.5368F, -13.1699F, 28.0F, 34.0F, 34.0F),
                PartPose.offsetAndRotation(59.0F, -43.5F, -82.0F, 0.5236F, 0.0F, -0.829F));

        PartDefinition part_left_hand2 = part_left_hand.addOrReplaceChild("left_hand2",
                CubeListBuilder.create()
                .texOffs(632, 476).mirror(false).addBox(-9.622F, -0.8075F, -13.8432F, 24.0F, 31.0F, 32.0F),
                PartPose.offset(-0.5352F, 3.2707F, 1.6733F));

        PartDefinition part_left_hand3 = part_left_hand2.addOrReplaceChild("left_hand3",
                CubeListBuilder.create()
                .texOffs(214, 632).mirror(false).addBox(-7.5583F, 0.2545F, -13.5703F, 23.0F, 44.0F, 30.0F)
                .texOffs(610, 573).mirror(false).addBox(-8.5583F, 3.2545F, -14.5703F, 25.0F, 41.0F, 32.0F)
                .texOffs(126, 634).mirror(false).addBox(-3.5583F, -7.7455F, -9.5703F, 15.0F, 8.0F, 22.0F)
                .texOffs(258, 349).mirror(false).addBox(-3.5583F, 0.2545F, 16.4297F, 15.0F, 9.0F, 7.0F),
                PartPose.offsetAndRotation(-3.0F, 27.25F, 1.0F, -1.0069F, 0.231F, 0.8029F));

        PartDefinition part_cube_r26 = part_left_hand3.addOrReplaceChild("cube_r26",
                CubeListBuilder.create()
                .texOffs(596, 393).mirror(false).addBox(-1.5F, -11.5F, -5.5F, 3.0F, 23.0F, 11.0F),
                PartPose.offsetAndRotation(22.4731F, 6.2294F, 0.8587F, 0.1124F, -0.0354F, 0.7965F));

        PartDefinition part_cube_r27 = part_left_hand3.addOrReplaceChild("cube_r27",
                CubeListBuilder.create()
                .texOffs(195, 287).mirror(false).addBox(-1.5F, -12.5F, -5.5F, 3.0F, 25.0F, 11.0F),
                PartPose.offsetAndRotation(16.2817F, 18.4181F, 19.6901F, -0.6768F, 0.6188F, 0.0442F));

        PartDefinition part_cube_r28 = part_left_hand3.addOrReplaceChild("cube_r28",
                CubeListBuilder.create()
                .texOffs(596, 393).mirror(false).addBox(-1.5F, -11.5F, -5.5F, 3.0F, 23.0F, 11.0F),
                PartPose.offsetAndRotation(3.9417F, -5.4496F, 24.4155F, -0.5672F, 0.0F, 0.0F));

        PartDefinition part_lower_left_hand = part_left_hand3.addOrReplaceChild("lower_left_hand",
                CubeListBuilder.create()
                .texOffs(714, 0).mirror(false).addBox(-8.0583F, 0.8031F, -7.7596F, 24.0F, 8.0F, 22.0F)
                .texOffs(649, 197).mirror(false).addBox(-7.0583F, 3.8031F, -11.7596F, 10.0F, 5.0F, 14.0F)
                .texOffs(485, 345).mirror(false).addBox(4.9417F, 3.8031F, -11.7596F, 10.0F, 5.0F, 14.0F)
                .texOffs(644, 461).mirror(false).addBox(5.9417F, 4.8031F, -14.7596F, 8.0F, 4.0F, 11.0F)
                .texOffs(416, 526).mirror(false).addBox(6.9417F, 6.8031F, -16.7596F, 6.0F, 2.0F, 10.0F)
                .texOffs(78, 564).mirror(false).addBox(7.9417F, 7.8031F, -19.7596F, 4.0F, 1.0F, 11.0F)
                .texOffs(644, 461).mirror(false).addBox(-6.0583F, 4.8031F, -14.7596F, 8.0F, 4.0F, 11.0F)
                .texOffs(416, 526).mirror(false).addBox(-5.0583F, 6.8031F, -16.7596F, 6.0F, 2.0F, 10.0F)
                .texOffs(78, 564).mirror(false).addBox(-4.0583F, 7.8031F, -19.7596F, 4.0F, 1.0F, 11.0F)
                .texOffs(199, 556).mirror(false).addBox(-7.0583F, -15.1969F, -2.7596F, 22.0F, 16.0F, 17.0F)
                .texOffs(0, 235).mirror(false).addBox(-7.0583F, -8.1969F, -5.7596F, 22.0F, 9.0F, 3.0F)
                .texOffs(314, 46).mirror(false).addBox(-7.0583F, -11.1969F, -3.7596F, 22.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 49.0F, -0.75F, 0.6545F, 0.0F, 0.0F));

        PartDefinition part_cube_r29 = part_lower_left_hand.addOrReplaceChild("cube_r29",
                CubeListBuilder.create()
                .texOffs(532, 683).mirror(false).addBox(-4.0F, -2.0F, -6.5F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(-5.0018F, 6.8031F, -2.0827F, 0.0F, 1.309F, 0.0F));

        PartDefinition part_cube_r30 = part_lower_left_hand.addOrReplaceChild("cube_r30",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(false).addBox(-13.0F, -0.1667F, 4.1667F, 2.0F, 1.0F, 2.0F)
                .texOffs(0, 114).mirror(false).addBox(-14.0F, -0.1667F, 6.1667F, 4.0F, 1.0F, 1.0F)
                .texOffs(126, 392).mirror(false).addBox(-15.0F, -2.1667F, 7.1667F, 6.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(-14.8781F, 7.9698F, -18.5743F, 0.0F, 1.1781F, 0.0F));

        PartDefinition part_cube_r31 = part_lower_left_hand.addOrReplaceChild("cube_r31",
                CubeListBuilder.create()
                .texOffs(16, 139).mirror(false).addBox(-1.0F, -0.5F, -5.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(19.1024F, 8.3031F, -3.7303F, 0.0F, -1.309F, 0.0F));

        PartDefinition part_cube_r32 = part_lower_left_hand.addOrReplaceChild("cube_r32",
                CubeListBuilder.create()
                .texOffs(0, 114).mirror(false).addBox(-2.0F, -0.5F, -4.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(17.6535F, 8.3031F, -3.3421F, 0.0F, -1.309F, 0.0F));

        PartDefinition part_cube_r33 = part_lower_left_hand.addOrReplaceChild("cube_r33",
                CubeListBuilder.create()
                .texOffs(126, 392).mirror(false).addBox(-113.035F, -11.5003F, -62.7251F, 6.0F, 3.0F, 10.0F)
                .texOffs(689, 184).mirror(false).addBox(-114.035F, -12.5003F, -60.7251F, 8.0F, 4.0F, 13.0F),
                PartPose.offsetAndRotation(-11.0744F, 17.3034F, 118.2721F, 0.0F, -1.309F, 0.0F));

    }

    private static void add_neck(PartDefinition root) {
        PartDefinition part_neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create()
                .texOffs(394, 0).mirror(false).addBox(-18.2F, -13.7004F, -25.5F, 37.0F, 18.0F, 26.0F)
                .texOffs(0, 376).mirror(false).addBox(-18.2F, 4.2996F, -25.6F, 37.0F, 18.0F, 26.0F),
                PartPose.offset(0.0F, -33.2996F, -101.0F));

        PartDefinition part_cube_r34 = part_neck.addOrReplaceChild("cube_r34",
                CubeListBuilder.create()
                .texOffs(194, 376).mirror(false).addBox(-6.3668F, 10.1227F, 1.9823F, 13.0F, 14.0F, 18.0F),
                PartPose.offsetAndRotation(25.5038F, -36.2734F, -65.8004F, -0.8572F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r35 = part_neck.addOrReplaceChild("cube_r35",
                CubeListBuilder.create()
                .texOffs(260, 556).mirror(true).addBox(-28.0F, -8.5F, -7.0F, 23.0F, 12.0F, 0.0F),
                PartPose.offsetAndRotation(-3.0F, -14.8004F, -30.5F, 0.0F, 1.5708F, 0.0F));

        PartDefinition part_cube_r36 = part_neck.addOrReplaceChild("cube_r36",
                CubeListBuilder.create()
                .texOffs(260, 556).mirror(false).addBox(5.0F, -8.5F, -7.0F, 23.0F, 12.0F, 0.0F),
                PartPose.offsetAndRotation(3.0F, -14.8004F, -30.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition part_neck2 = part_neck.addOrReplaceChild("neck2",
                CubeListBuilder.create()
                .texOffs(268, 0).mirror(false).addBox(-17.5F, -8.7004F, -27.0F, 35.0F, 18.0F, 28.0F)
                .texOffs(268, 0).mirror(false).addBox(-17.5F, -8.7004F, -27.0F, 35.0F, 18.0F, 28.0F)
                .texOffs(444, 255).mirror(false).addBox(-17.5F, 9.2996F, -14.5F, 35.0F, 16.0F, 15.0F)
                .texOffs(231, 201).mirror(false).addBox(-17.3F, 9.2996F, -26.9F, 34.0F, 14.0F, 12.0F),
                PartPose.offset(0.5F, -4.0F, -26.5F));

        PartDefinition part_cube_r37 = part_neck2.addOrReplaceChild("cube_r37",
                CubeListBuilder.create()
                .texOffs(394, 44).mirror(true).addBox(0.0F, -1.5F, -7.0F, 14.0F, 5.0F, 0.0F)
                .texOffs(0, 201).mirror(true).addBox(-7.0F, -6.5F, -7.0F, 7.0F, 10.0F, 0.0F),
                PartPose.offsetAndRotation(-3.5F, -10.8004F, -9.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition part_cube_r38 = part_neck2.addOrReplaceChild("cube_r38",
                CubeListBuilder.create()
                .texOffs(0, 201).mirror(false).addBox(0.0F, -6.5F, -7.0F, 7.0F, 10.0F, 0.0F)
                .texOffs(394, 44).mirror(false).addBox(-14.0F, -1.5F, -7.0F, 14.0F, 5.0F, 0.0F),
                PartPose.offsetAndRotation(2.5F, -10.8004F, -9.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition part_cube_r39 = part_neck2.addOrReplaceChild("cube_r39",
                CubeListBuilder.create()
                .texOffs(462, 608).mirror(true).addBox(-19.5F, -29.5F, 0.0F, 29.0F, 15.0F, 0.0F),
                PartPose.offsetAndRotation(-19.5F, 11.5801F, -13.8355F, 1.5708F, 1.5272F, 1.5708F));

        PartDefinition part_cube_r40 = part_neck2.addOrReplaceChild("cube_r40",
                CubeListBuilder.create()
                .texOffs(100, 376).mirror(true).addBox(-14.5F, -7.5F, 0.0F, 29.0F, 16.0F, 0.0F),
                PartPose.offsetAndRotation(-19.5F, 11.5801F, -13.8355F, -1.5708F, 0.8727F, -1.5708F));

        PartDefinition part_cube_r41 = part_neck2.addOrReplaceChild("cube_r41",
                CubeListBuilder.create()
                .texOffs(462, 608).mirror(false).addBox(-9.5F, -29.5F, 0.0F, 29.0F, 15.0F, 0.0F),
                PartPose.offsetAndRotation(18.5F, 11.5801F, -13.8355F, 1.5708F, -1.5272F, -1.5708F));

        PartDefinition part_cube_r42 = part_neck2.addOrReplaceChild("cube_r42",
                CubeListBuilder.create()
                .texOffs(100, 376).mirror(false).addBox(-14.5F, -7.5F, 0.0F, 29.0F, 16.0F, 0.0F),
                PartPose.offsetAndRotation(18.5F, 11.5801F, -13.8355F, -1.5708F, -0.8727F, 1.5708F));

        PartDefinition part_head = part_neck2.addOrReplaceChild("head",
                CubeListBuilder.create()
                .texOffs(494, 393).mirror(false).addBox(-18.0F, -15.0F, -15.0F, 37.0F, 16.0F, 14.0F)
                .texOffs(284, 137).mirror(false).addBox(-18.0F, -1.0F, -17.0F, 37.0F, 6.0F, 2.0F)
                .texOffs(0, 139).mirror(false).addBox(-3.0F, -3.0F, -16.0F, 7.0F, 2.0F, 1.0F)
                .texOffs(323, 222).mirror(false).addBox(-18.0F, 1.0F, -15.0F, 37.0F, 4.0F, 2.0F)
                .texOffs(362, 137).mirror(false).addBox(-17.0F, 4.0F, -16.75F, 34.0F, 5.0F, 3.0F)
                .texOffs(462, 573).mirror(false).addBox(-17.0F, 0.75F, -13.0F, 35.0F, 23.0F, 12.0F)
                .texOffs(704, 96).mirror(false).addBox(-16.5F, 1.0F, -14.5F, 34.0F, 20.0F, 12.0F)
                .texOffs(226, 408).mirror(false).addBox(-4.0F, -19.5F, -14.4F, 9.0F, 6.0F, 2.0F)
                .texOffs(560, 61).mirror(false).addBox(-7.5F, -44.0F, -11.0F, 16.0F, 29.0F, 2.0F)
                .texOffs(38, 339).mirror(false).addBox(-4.5F, -50.0F, -11.0F, 10.0F, 6.0F, 2.0F),
                PartPose.offset(-0.5F, 2.2996F, -26.0F));

        PartDefinition part_cube_r43 = part_head.addOrReplaceChild("cube_r43",
                CubeListBuilder.create()
                .texOffs(238, 376).mirror(true).addBox(-2.9F, -3.05F, -2.4F, 5.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(-38.8196F, -22.5857F, 14.4217F, -0.4992F, 0.2456F, -1.2147F));

        PartDefinition part_cube_r44 = part_head.addOrReplaceChild("cube_r44",
                CubeListBuilder.create()
                .texOffs(610, 173).mirror(true).addBox(-3.6F, -5.2F, -2.4F, 7.0F, 10.0F, 5.0F),
                PartPose.offsetAndRotation(-32.967F, -18.1735F, 10.9984F, -0.4992F, 0.2456F, -1.2147F));

        PartDefinition part_cube_r45 = part_head.addOrReplaceChild("cube_r45",
                CubeListBuilder.create()
                .texOffs(290, 632).mirror(true).addBox(-4.4F, -4.0F, -2.8F, 9.0F, 8.0F, 6.0F),
                PartPose.offsetAndRotation(-28.3727F, -13.9267F, 8.3415F, -0.3758F, 0.414F, -0.8389F));

        PartDefinition part_cube_r46 = part_head.addOrReplaceChild("cube_r46",
                CubeListBuilder.create()
                .texOffs(237, 477).mirror(true).addBox(-5.6F, -4.0F, -3.6F, 11.0F, 8.0F, 7.0F),
                PartPose.offsetAndRotation(-23.3657F, -5.0583F, 5.6097F, -0.2971F, 0.4646F, -0.6543F));

        PartDefinition part_cube_r47 = part_head.addOrReplaceChild("cube_r47",
                CubeListBuilder.create()
                .texOffs(612, 573).mirror(true).addBox(-4.4F, -4.0F, -3.2F, 9.0F, 8.0F, 6.0F),
                PartPose.offsetAndRotation(-25.6942F, -9.5101F, 6.8537F, -0.0091F, 0.5237F, -0.0612F));

        PartDefinition part_cube_r48 = part_head.addOrReplaceChild("cube_r48",
                CubeListBuilder.create()
                .texOffs(238, 376).mirror(false).addBox(-2.1F, -3.05F, -2.4F, 5.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(38.8196F, -22.5857F, 14.4217F, -0.4992F, -0.2456F, 1.2147F));

        PartDefinition part_cube_r49 = part_head.addOrReplaceChild("cube_r49",
                CubeListBuilder.create()
                .texOffs(610, 173).mirror(false).addBox(-3.4F, -5.2F, -2.4F, 7.0F, 10.0F, 5.0F),
                PartPose.offsetAndRotation(32.967F, -18.1735F, 10.9984F, -0.4992F, -0.2456F, 1.2147F));

        PartDefinition part_cube_r50 = part_head.addOrReplaceChild("cube_r50",
                CubeListBuilder.create()
                .texOffs(290, 632).mirror(false).addBox(-4.6F, -4.0F, -2.8F, 9.0F, 8.0F, 6.0F),
                PartPose.offsetAndRotation(28.3727F, -13.9267F, 8.3415F, -0.3758F, -0.414F, 0.8389F));

        PartDefinition part_cube_r51 = part_head.addOrReplaceChild("cube_r51",
                CubeListBuilder.create()
                .texOffs(612, 573).mirror(false).addBox(-4.6F, -4.0F, -3.2F, 9.0F, 8.0F, 6.0F),
                PartPose.offsetAndRotation(25.6942F, -9.5101F, 6.8537F, -0.0091F, -0.5237F, 0.0612F));

        PartDefinition part_cube_r52 = part_head.addOrReplaceChild("cube_r52",
                CubeListBuilder.create()
                .texOffs(237, 477).mirror(false).addBox(-5.4F, -4.0F, -3.6F, 11.0F, 8.0F, 7.0F),
                PartPose.offsetAndRotation(23.3657F, -5.0583F, 5.6097F, -0.2971F, -0.4646F, 0.6543F));

        PartDefinition part_cube_r53 = part_head.addOrReplaceChild("cube_r53",
                CubeListBuilder.create()
                .texOffs(636, 736).mirror(true).addBox(-14.0035F, 18.273F, 12.4385F, 14.0F, 14.0F, 20.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, 0.0F, 1.2217F, 0.3054F));

        PartDefinition part_cube_r54 = part_head.addOrReplaceChild("cube_r54",
                CubeListBuilder.create()
                .texOffs(240, 127).mirror(true).addBox(-4.1995F, -20.4432F, -40.6165F, 7.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -1.0317F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r55 = part_head.addOrReplaceChild("cube_r55",
                CubeListBuilder.create()
                .texOffs(222, 516).mirror(true).addBox(-4.9995F, -9.3802F, -34.7522F, 9.0F, 6.0F, 11.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -1.3808F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r56 = part_head.addOrReplaceChild("cube_r56",
                CubeListBuilder.create()
                .texOffs(649, 539).mirror(true).addBox(-5.3375F, -2.9474F, -25.3971F, 10.0F, 10.0F, 17.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -1.6426F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r57 = part_head.addOrReplaceChild("cube_r57",
                CubeListBuilder.create()
                .texOffs(360, 46).mirror(true).addBox(-5.8332F, 8.9989F, 14.3582F, 11.0F, 4.0F, 0.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, 2.6771F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r58 = part_head.addOrReplaceChild("cube_r58",
                CubeListBuilder.create()
                .texOffs(578, 142).mirror(true).addBox(-5.8332F, 0.1146F, -9.6256F, 11.0F, 14.0F, 17.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -1.9917F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r59 = part_head.addOrReplaceChild("cube_r59",
                CubeListBuilder.create()
                .texOffs(604, 461).mirror(true).addBox(-5.6332F, 19.0733F, -2.7591F, 11.0F, 12.0F, 18.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -0.0718F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r60 = part_head.addOrReplaceChild("cube_r60",
                CubeListBuilder.create()
                .texOffs(571, 573).mirror(true).addBox(-6.2332F, 1.5417F, 1.5529F, 12.0F, 14.0F, 17.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -1.6426F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r61 = part_head.addOrReplaceChild("cube_r61",
                CubeListBuilder.create()
                .texOffs(194, 376).mirror(false).addBox(-6.3668F, 10.1227F, 1.9823F, 13.0F, 14.0F, 18.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -0.8572F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r62 = part_head.addOrReplaceChild("cube_r62",
                CubeListBuilder.create()
                .texOffs(194, 376).mirror(true).addBox(-6.6332F, 10.1227F, 1.9823F, 13.0F, 14.0F, 18.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -0.8572F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r63 = part_head.addOrReplaceChild("cube_r63",
                CubeListBuilder.create()
                .texOffs(58, 727).mirror(true).addBox(-7.4332F, 18.023F, -2.8015F, 14.0F, 14.0F, 20.0F),
                PartPose.offsetAndRotation(-25.5038F, -34.573F, -16.3004F, -0.0718F, 0.5754F, 0.1764F));

        PartDefinition part_cube_r64 = part_head.addOrReplaceChild("cube_r64",
                CubeListBuilder.create()
                .texOffs(636, 736).mirror(false).addBox(0.0035F, 18.273F, 12.4385F, 14.0F, 14.0F, 20.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, 0.0F, -1.2217F, -0.3054F));

        PartDefinition part_cube_r65 = part_head.addOrReplaceChild("cube_r65",
                CubeListBuilder.create()
                .texOffs(360, 46).mirror(false).addBox(-5.1668F, 8.9989F, 14.3582F, 11.0F, 4.0F, 0.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, 2.6771F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r66 = part_head.addOrReplaceChild("cube_r66",
                CubeListBuilder.create()
                .texOffs(240, 127).mirror(false).addBox(-2.8005F, -20.4432F, -40.6165F, 7.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -1.0317F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r67 = part_head.addOrReplaceChild("cube_r67",
                CubeListBuilder.create()
                .texOffs(222, 516).mirror(false).addBox(-4.0005F, -9.3802F, -34.7522F, 9.0F, 6.0F, 11.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -1.3808F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r68 = part_head.addOrReplaceChild("cube_r68",
                CubeListBuilder.create()
                .texOffs(649, 539).mirror(false).addBox(-4.6625F, -2.9474F, -25.3971F, 10.0F, 10.0F, 17.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -1.6426F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r69 = part_head.addOrReplaceChild("cube_r69",
                CubeListBuilder.create()
                .texOffs(578, 142).mirror(false).addBox(-5.1668F, 0.1146F, -9.6256F, 11.0F, 14.0F, 17.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -1.9917F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r70 = part_head.addOrReplaceChild("cube_r70",
                CubeListBuilder.create()
                .texOffs(571, 573).mirror(false).addBox(-5.7668F, 1.5417F, 1.5529F, 12.0F, 14.0F, 17.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -1.6426F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r71 = part_head.addOrReplaceChild("cube_r71",
                CubeListBuilder.create()
                .texOffs(604, 461).mirror(false).addBox(-5.3668F, 19.0733F, -2.7591F, 11.0F, 12.0F, 18.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -0.0718F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r72 = part_head.addOrReplaceChild("cube_r72",
                CubeListBuilder.create()
                .texOffs(58, 727).mirror(false).addBox(-6.5668F, 18.023F, -2.8015F, 14.0F, 14.0F, 20.0F),
                PartPose.offsetAndRotation(25.5038F, -34.573F, -16.3004F, -0.0718F, -0.5754F, -0.1764F));

        PartDefinition part_cube_r73 = part_head.addOrReplaceChild("cube_r73",
                CubeListBuilder.create()
                .texOffs(347, 568).mirror(true).addBox(-3.2F, -10.8F, -1.2F, 6.0F, 22.0F, 2.0F),
                PartPose.offsetAndRotation(-11.2967F, -22.8334F, -4.6131F, -0.48F, 0.0F, -1.0908F));

        PartDefinition part_cube_r74 = part_head.addOrReplaceChild("cube_r74",
                CubeListBuilder.create()
                .texOffs(26, 98).mirror(true).addBox(-3.2F, -5.9F, -0.8F, 6.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-32.2276F, -56.4533F, 17.3615F, -1.3074F, 0.0403F, -1.0134F));

        PartDefinition part_cube_r75 = part_head.addOrReplaceChild("cube_r75",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(true).addBox(-3.2F, -12.0F, -0.95F, 6.0F, 24.0F, 2.0F),
                PartPose.offsetAndRotation(-22.8711F, -50.1551F, 7.5487F, -0.4784F, 0.0403F, -1.0134F));

        PartDefinition part_cube_r76 = part_head.addOrReplaceChild("cube_r76",
                CubeListBuilder.create()
                .texOffs(493, 633).mirror(true).addBox(-3.2F, -12.0F, -1.2F, 6.0F, 24.0F, 2.0F),
                PartPose.offsetAndRotation(-16.9752F, -36.0069F, 1.4254F, -0.1762F, 0.4488F, 0.0906F));

        PartDefinition part_cube_r77 = part_head.addOrReplaceChild("cube_r77",
                CubeListBuilder.create()
                .texOffs(26, 98).mirror(false).addBox(-2.8F, -5.9F, -0.8F, 6.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(32.2276F, -56.4533F, 17.3615F, -1.3074F, -0.0403F, 1.0134F));

        PartDefinition part_cube_r78 = part_head.addOrReplaceChild("cube_r78",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(false).addBox(-2.8F, -12.0F, -0.95F, 6.0F, 24.0F, 2.0F),
                PartPose.offsetAndRotation(22.8711F, -50.1551F, 7.5487F, -0.4784F, -0.0403F, 1.0134F));

        PartDefinition part_cube_r79 = part_head.addOrReplaceChild("cube_r79",
                CubeListBuilder.create()
                .texOffs(493, 633).mirror(false).addBox(-2.8F, -12.0F, -1.2F, 6.0F, 24.0F, 2.0F),
                PartPose.offsetAndRotation(16.9752F, -36.0069F, 1.4254F, -0.1762F, -0.4488F, -0.0906F));

        PartDefinition part_cube_r80 = part_head.addOrReplaceChild("cube_r80",
                CubeListBuilder.create()
                .texOffs(347, 568).mirror(false).addBox(-2.8F, -10.8F, -1.2F, 6.0F, 22.0F, 2.0F),
                PartPose.offsetAndRotation(11.2967F, -22.8334F, -4.6131F, -0.48F, 0.0F, 1.0908F));

        PartDefinition part_cube_r81 = part_head.addOrReplaceChild("cube_r81",
                CubeListBuilder.create()
                .texOffs(0, 98).mirror(true).addBox(-6.2F, -3.8538F, -1.3999F, 4.0F, 13.0F, 3.0F)
                .texOffs(596, 508).mirror(true).addBox(-2.2F, -11.8538F, -1.3999F, 5.0F, 24.0F, 3.0F)
                .texOffs(0, 98).mirror(false).addBox(15.8F, -3.8538F, -1.3999F, 4.0F, 13.0F, 3.0F)
                .texOffs(199, 545).mirror(true).addBox(-2.2F, 19.1462F, -1.3999F, 5.0F, 24.0F, 3.0F)
                .texOffs(199, 545).mirror(false).addBox(10.8F, 19.1462F, -1.3999F, 5.0F, 24.0F, 3.0F)
                .texOffs(596, 508).mirror(false).addBox(10.8F, -11.8538F, -1.3999F, 5.0F, 24.0F, 3.0F),
                PartPose.offsetAndRotation(-6.8F, -62.6462F, 4.8999F, -0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r82 = part_head.addOrReplaceChild("cube_r82",
                CubeListBuilder.create()
                .texOffs(386, 725).mirror(false).addBox(-4.4F, -33.3541F, -1.3447F, 8.0F, 66.0F, 3.0F),
                PartPose.offsetAndRotation(0.4F, -45.6459F, 0.3447F, -0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r83 = part_head.addOrReplaceChild("cube_r83",
                CubeListBuilder.create()
                .texOffs(26, 420).mirror(true).addBox(-5.5F, -7.2F, -2.0F, 7.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(-52.448F, -59.5461F, 7.8993F, -0.5824F, 0.1084F, -1.1012F));

        PartDefinition part_cube_r84 = part_head.addOrReplaceChild("cube_r84",
                CubeListBuilder.create()
                .texOffs(195, 323).mirror(true).addBox(-6.0F, -8.6F, -2.0F, 12.0F, 17.0F, 4.0F),
                PartPose.offsetAndRotation(-43.4777F, -49.3031F, 1.9429F, -0.3642F, 0.1084F, -1.1012F));

        PartDefinition part_cube_r85 = part_head.addOrReplaceChild("cube_r85",
                CubeListBuilder.create()
                .texOffs(445, 693).mirror(true).addBox(-7.6F, -8.6F, -2.0F, 15.0F, 17.0F, 4.0F),
                PartPose.offsetAndRotation(-32.3752F, -43.7324F, -2.875F, -0.2054F, 0.0741F, -1.273F));

        PartDefinition part_cube_r86 = part_head.addOrReplaceChild("cube_r86",
                CubeListBuilder.create()
                .texOffs(689, 344).mirror(true).addBox(-7.6F, -8.6F, -2.0F, 15.0F, 17.0F, 4.0F),
                PartPose.offsetAndRotation(-20.6441F, -37.7169F, -5.6601F, -0.1074F, 0.0749F, -1.0076F));

        PartDefinition part_cube_r87 = part_head.addOrReplaceChild("cube_r87",
                CubeListBuilder.create()
                .texOffs(268, 314).mirror(true).addBox(-7.6F, -13.4F, -2.0F, 15.0F, 27.0F, 4.0F),
                PartPose.offsetAndRotation(-14.0349F, -25.42F, -7.6F, 0.0F, 0.0F, -0.2182F));

        PartDefinition part_cube_r88 = part_head.addOrReplaceChild("cube_r88",
                CubeListBuilder.create()
                .texOffs(26, 420).mirror(false).addBox(-1.5F, -7.2F, -2.0F, 7.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(52.448F, -59.5461F, 7.8993F, -0.5824F, -0.1084F, 1.1012F));

        PartDefinition part_cube_r89 = part_head.addOrReplaceChild("cube_r89",
                CubeListBuilder.create()
                .texOffs(195, 323).mirror(false).addBox(-6.0F, -8.6F, -2.0F, 12.0F, 17.0F, 4.0F),
                PartPose.offsetAndRotation(43.4777F, -49.3031F, 1.9429F, -0.3642F, -0.1084F, 1.1012F));

        PartDefinition part_cube_r90 = part_head.addOrReplaceChild("cube_r90",
                CubeListBuilder.create()
                .texOffs(445, 693).mirror(false).addBox(-7.4F, -8.6F, -2.0F, 15.0F, 17.0F, 4.0F),
                PartPose.offsetAndRotation(32.3752F, -43.7324F, -2.875F, -0.2054F, -0.0741F, 1.273F));

        PartDefinition part_cube_r91 = part_head.addOrReplaceChild("cube_r91",
                CubeListBuilder.create()
                .texOffs(689, 344).mirror(false).addBox(-7.4F, -8.6F, -2.0F, 15.0F, 17.0F, 4.0F),
                PartPose.offsetAndRotation(20.6441F, -37.7169F, -5.6601F, -0.1074F, -0.0749F, 1.0076F));

        PartDefinition part_cube_r92 = part_head.addOrReplaceChild("cube_r92",
                CubeListBuilder.create()
                .texOffs(268, 314).mirror(false).addBox(-7.4F, -13.4F, -2.0F, 15.0F, 27.0F, 4.0F),
                PartPose.offsetAndRotation(14.0349F, -25.42F, -7.6F, 0.0F, 0.0F, 0.2182F));

        PartDefinition part_cube_r93 = part_head.addOrReplaceChild("cube_r93",
                CubeListBuilder.create()
                .texOffs(240, 98).mirror(true).addBox(-3.546F, -13.6085F, -0.8F, 11.0F, 27.0F, 2.0F)
                .texOffs(26, 111).mirror(false).addBox(-3.546F, -18.6085F, 1.2F, 7.0F, 5.0F, 0.0F),
                PartPose.offsetAndRotation(-15.954F, -23.3915F, -10.4F, 0.0F, 0.0F, -0.5672F));

        PartDefinition part_cube_r94 = part_head.addOrReplaceChild("cube_r94",
                CubeListBuilder.create()
                .texOffs(0, 194).mirror(false).addBox(-3.454F, -18.6085F, -0.8F, 7.0F, 5.0F, 2.0F)
                .texOffs(240, 98).mirror(false).addBox(-7.454F, -13.6085F, -0.8F, 11.0F, 27.0F, 2.0F),
                PartPose.offsetAndRotation(15.954F, -23.3915F, -10.4F, 0.0F, 0.0F, 0.5672F));

        PartDefinition part_cube_r95 = part_head.addOrReplaceChild("cube_r95",
                CubeListBuilder.create()
                .texOffs(126, 634).mirror(true).addBox(-4.5867F, -5.8406F, -0.8F, 9.0F, 14.0F, 2.0F),
                PartPose.offsetAndRotation(-7.6633F, -21.4094F, -13.6F, 0.0F, 0.0F, -0.2618F));

        PartDefinition part_cube_r96 = part_head.addOrReplaceChild("cube_r96",
                CubeListBuilder.create()
                .texOffs(231, 207).mirror(true).addBox(-1.6F, -1.4F, -0.8F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-24.7519F, -18.4453F, -12.0F, 0.0F, 0.0F, -1.0472F));

        PartDefinition part_cube_r97 = part_head.addOrReplaceChild("cube_r97",
                CubeListBuilder.create()
                .texOffs(366, 0).mirror(true).addBox(-3.6F, -8.6F, -0.8F, 7.0F, 17.0F, 2.0F),
                PartPose.offsetAndRotation(-15.0917F, -15.1773F, -12.0F, 0.0F, 0.0F, -1.0472F));

        PartDefinition part_cube_r98 = part_head.addOrReplaceChild("cube_r98",
                CubeListBuilder.create()
                .texOffs(366, 0).mirror(false).addBox(-3.4F, -8.6F, -0.8F, 7.0F, 17.0F, 2.0F),
                PartPose.offsetAndRotation(15.8417F, -15.4273F, -12.0F, 0.0F, 0.0F, 1.0472F));

        PartDefinition part_cube_r99 = part_head.addOrReplaceChild("cube_r99",
                CubeListBuilder.create()
                .texOffs(231, 207).mirror(false).addBox(-1.4F, -1.4F, -0.8F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(25.5019F, -18.6953F, -12.0F, 0.0F, 0.0F, 1.0472F));

        PartDefinition part_cube_r100 = part_head.addOrReplaceChild("cube_r100",
                CubeListBuilder.create()
                .texOffs(231, 227).mirror(true).addBox(-3.8367F, -12.7406F, -0.8F, 5.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(-7.6633F, -18.4094F, -13.6F, 0.0F, 0.0F, -0.2618F));

        PartDefinition part_cube_r101 = part_head.addOrReplaceChild("cube_r101",
                CubeListBuilder.create()
                .texOffs(231, 227).mirror(false).addBox(-1.1633F, -12.7406F, -0.8F, 5.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(8.4133F, -18.6594F, -13.6F, 0.0F, 0.0F, 0.2618F));

        PartDefinition part_cube_r102 = part_head.addOrReplaceChild("cube_r102",
                CubeListBuilder.create()
                .texOffs(126, 634).mirror(false).addBox(-4.4133F, -5.8406F, -0.8F, 9.0F, 14.0F, 2.0F),
                PartPose.offsetAndRotation(8.4133F, -21.6594F, -13.6F, 0.0F, 0.0F, 0.2618F));

        PartDefinition part_mouth = part_head.addOrReplaceChild("mouth",
                CubeListBuilder.create()
                .texOffs(100, 392).mirror(false).addBox(-1.0F, -8.25F, -17.5F, 18.0F, 6.0F, 0.0F)
                .texOffs(596, 521).mirror(false).addBox(-1.0F, 16.75F, -17.5F, 9.0F, 9.0F, 18.0F)
                .texOffs(78, 545).mirror(false).addBox(-1.25F, 25.75F, -10.5F, 5.0F, 9.0F, 10.0F)
                .texOffs(78, 545).mirror(true).addBox(-6.25F, 25.75F, -10.5F, 5.0F, 9.0F, 10.0F)
                .texOffs(244, 589).mirror(false).addBox(-1.0F, -2.25F, -17.5F, 18.0F, 19.0F, 2.0F)
                .texOffs(649, 216).mirror(false).addBox(-1.0F, -2.25F, -0.5F, 18.0F, 19.0F, 2.0F)
                .texOffs(596, 521).mirror(true).addBox(-10.0F, 16.75F, -17.5F, 9.0F, 9.0F, 18.0F)
                .texOffs(244, 589).mirror(true).addBox(-19.0F, -2.25F, -17.5F, 18.0F, 19.0F, 2.0F)
                .texOffs(100, 392).mirror(true).addBox(-19.0F, -8.25F, -17.5F, 18.0F, 6.0F, 0.0F)
                .texOffs(649, 216).mirror(true).addBox(-19.0F, -2.25F, -0.5F, 18.0F, 19.0F, 2.0F),
                PartPose.offsetAndRotation(1.25F, 13.25F, 0.25F, 0.2182F, 0.0F, 0.0F));

        PartDefinition part_cube_r103 = part_mouth.addOrReplaceChild("cube_r103",
                CubeListBuilder.create()
                .texOffs(483, 693).mirror(true).addBox(-9.3F, -17.65F, -9.6F, 18.0F, 19.0F, 0.0F)
                .texOffs(483, 693).mirror(false).addBox(8.7F, -17.65F, -9.6F, 18.0F, 19.0F, 0.0F),
                PartPose.offsetAndRotation(-9.7F, 7.15F, 0.1F, 1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r104 = part_mouth.addOrReplaceChild("cube_r104",
                CubeListBuilder.create()
                .texOffs(692, 586).mirror(true).addBox(-9.3F, -17.65F, -9.35F, 19.0F, 19.0F, 0.0F),
                PartPose.offsetAndRotation(-9.7F, 7.15F, 0.1F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition part_cube_r105 = part_mouth.addOrReplaceChild("cube_r105",
                CubeListBuilder.create()
                .texOffs(692, 586).mirror(false).addBox(-9.7F, -17.65F, -9.35F, 19.0F, 19.0F, 0.0F),
                PartPose.offsetAndRotation(7.7F, 7.15F, 0.1F, 1.5708F, 0.0F, -1.5708F));

        PartDefinition part_jaw = part_mouth.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                .texOffs(0, 339).mirror(false).addBox(0.0F, -5.0F, -5.8333F, 17.0F, 6.0F, 2.0F)
                .texOffs(418, 568).mirror(false).addBox(0.0F, -1.0F, -2.8333F, 5.0F, 2.0F, 12.0F)
                .texOffs(178, 634).mirror(false).addBox(0.0F, 1.0F, -5.8333F, 17.0F, 2.0F, 15.0F)
                .texOffs(0, 339).mirror(true).addBox(-17.0F, -5.0F, -5.8333F, 17.0F, 6.0F, 2.0F)
                .texOffs(178, 634).mirror(true).addBox(-17.0F, 1.0F, -5.8333F, 17.0F, 2.0F, 15.0F)
                .texOffs(418, 568).mirror(true).addBox(-5.0F, -1.0F, -2.8333F, 5.0F, 2.0F, 12.0F),
                PartPose.offset(-1.0F, 1.75F, -9.6667F));

    }

    private static void add_body(PartDefinition root) {
        PartDefinition part_body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                .texOffs(266, 50).mirror(false).addBox(-44.0F, -18.25F, -48.0F, 89.0F, 39.0F, 48.0F)
                .texOffs(0, 423).mirror(false).addBox(-44.0F, 20.75F, -48.0F, 89.0F, 6.0F, 48.0F)
                .texOffs(225, 228).mirror(false).addBox(-40.0F, 15.8205F, -43.1484F, 80.0F, 27.0F, 59.0F)
                .texOffs(0, 0).mirror(false).addBox(32.0F, -18.25F, -55.0F, 13.0F, 39.0F, 7.0F)
                .texOffs(46, 643).mirror(false).addBox(32.0F, 15.75F, -69.0F, 13.0F, 5.0F, 14.0F)
                .texOffs(433, 493).mirror(false).addBox(34.0F, 15.75F, -77.0F, 9.0F, 5.0F, 8.0F)
                .texOffs(226, 423).mirror(false).addBox(35.0F, -12.25F, -59.0F, 7.0F, 12.0F, 4.0F)
                .texOffs(0, 0).mirror(false).addBox(-45.0F, -18.25F, -55.0F, 13.0F, 39.0F, 7.0F)
                .texOffs(226, 423).mirror(false).addBox(-42.0F, -12.25F, -59.0F, 7.0F, 12.0F, 4.0F)
                .texOffs(46, 643).mirror(false).addBox(-45.0F, 15.75F, -69.0F, 13.0F, 5.0F, 14.0F)
                .texOffs(433, 493).mirror(false).addBox(-43.0F, 15.75F, -77.0F, 9.0F, 5.0F, 8.0F)
                .texOffs(0, 0).mirror(false).addBox(25.75F, -20.25F, -48.0F, 9.0F, 0.0F, 0.0F),
                PartPose.offset(0.0F, -51.5F, -52.0F));

        PartDefinition part_cube_r106 = part_body.addOrReplaceChild("cube_r106",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(true).addBox(-3.5F, -28.0F, -16.0F, 7.0F, 56.0F, 32.0F),
                PartPose.offsetAndRotation(-55.9594F, -40.0224F, -3.2538F, 0.1745F, -0.48F, -0.6545F));

        PartDefinition part_cube_r107 = part_body.addOrReplaceChild("cube_r107",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(false).addBox(-3.5F, -28.0F, -16.0F, 7.0F, 56.0F, 32.0F),
                PartPose.offsetAndRotation(55.9594F, -40.0224F, -3.2538F, 0.1745F, 0.48F, 0.6545F));

        PartDefinition part_cube_r108 = part_body.addOrReplaceChild("cube_r108",
                CubeListBuilder.create()
                .texOffs(658, 97).mirror(true).addBox(-2.0F, -22.0F, -19.0F, 4.0F, 44.0F, 38.0F),
                PartPose.offsetAndRotation(-10.016F, -31.8463F, -23.5525F, 0.3927F, 0.0F, 0.1745F));

        PartDefinition part_cube_r109 = part_body.addOrReplaceChild("cube_r109",
                CubeListBuilder.create()
                .texOffs(662, 219).mirror(true).addBox(-2.0F, -20.5F, -18.0F, 4.0F, 41.0F, 36.0F),
                PartPose.offsetAndRotation(-15.1585F, -25.717F, -20.6824F, 0.3927F, 0.0F, 0.1745F));

        PartDefinition part_cube_r110 = part_body.addOrReplaceChild("cube_r110",
                CubeListBuilder.create()
                .texOffs(658, 97).mirror(false).addBox(-2.0F, -22.0F, -19.0F, 4.0F, 44.0F, 38.0F),
                PartPose.offsetAndRotation(10.016F, -31.8463F, -23.5525F, 0.3927F, 0.0F, -0.1745F));

        PartDefinition part_cube_r111 = part_body.addOrReplaceChild("cube_r111",
                CubeListBuilder.create()
                .texOffs(662, 219).mirror(false).addBox(-2.0F, -20.5F, -18.0F, 4.0F, 41.0F, 36.0F),
                PartPose.offsetAndRotation(15.1585F, -25.717F, -20.6824F, 0.3927F, 0.0F, -0.1745F));

        PartDefinition part_cube_r112 = part_body.addOrReplaceChild("cube_r112",
                CubeListBuilder.create()
                .texOffs(485, 314).mirror(true).addBox(-4.5F, -22.0F, -8.0F, 9.0F, 15.0F, 16.0F)
                .texOffs(656, 29).mirror(true).addBox(-6.5F, -7.0F, -16.0F, 13.0F, 35.0F, 32.0F),
                PartPose.offsetAndRotation(-37.9536F, -43.4129F, -38.3938F, 0.4185F, -0.1274F, -0.5071F));

        PartDefinition part_cube_r113 = part_body.addOrReplaceChild("cube_r113",
                CubeListBuilder.create()
                .texOffs(485, 314).mirror(false).addBox(-4.5F, -22.0F, -8.0F, 9.0F, 15.0F, 16.0F)
                .texOffs(656, 29).mirror(false).addBox(-6.5F, -7.0F, -16.0F, 13.0F, 35.0F, 32.0F),
                PartPose.offsetAndRotation(37.9536F, -43.4129F, -38.3938F, 0.4185F, 0.1274F, 0.5071F));

        PartDefinition part_cube_r114 = part_body.addOrReplaceChild("cube_r114",
                CubeListBuilder.create()
                .texOffs(429, 602).mirror(false).addBox(-3.5F, -6.0F, -2.0F, 7.0F, 12.0F, 4.0F)
                .texOffs(429, 602).mirror(false).addBox(73.5F, -6.0F, -2.0F, 7.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-38.5F, -25.25F, -35.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r115 = part_body.addOrReplaceChild("cube_r115",
                CubeListBuilder.create()
                .texOffs(618, 317).mirror(false).addBox(-6.5F, -19.5F, -2.5F, 13.0F, 39.0F, 5.0F)
                .texOffs(618, 317).mirror(false).addBox(70.5F, -19.5F, -2.5F, 13.0F, 39.0F, 5.0F),
                PartPose.offsetAndRotation(-38.5F, -20.75F, -28.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition part_cube_r116 = part_body.addOrReplaceChild("cube_r116",
                CubeListBuilder.create()
                .texOffs(444, 201).mirror(false).addBox(-39.0F, 12.5F, -19.5F, 78.0F, 5.0F, 49.0F),
                PartPose.offsetAndRotation(0.0F, 23.3205F, -13.6484F, -0.3491F, 0.0F, 0.0F));

        PartDefinition part_body2 = part_body.addOrReplaceChild("body2",
                CubeListBuilder.create()
                .texOffs(0, 0).mirror(false).addBox(-55.75F, -23.25F, -2.0F, 111.0F, 52.0F, 46.0F)
                .texOffs(238, 148).mirror(false).addBox(-54.75F, 28.75F, -2.0F, 109.0F, 7.0F, 46.0F)
                .texOffs(274, 423).mirror(false).addBox(-41.5F, 28.75F, 0.0F, 83.0F, 26.0F, 44.0F),
                PartPose.offset(0.0F, -3.5F, 2.0F));

        PartDefinition part_cube_r117 = part_body2.addOrReplaceChild("cube_r117",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(true).addBox(-3.5F, -25.0F, -16.0F, 7.0F, 56.0F, 32.0F),
                PartPose.offsetAndRotation(-71.2297F, -44.9729F, 34.991F, -0.1309F, 0.0F, -0.829F));

        PartDefinition part_cube_r118 = part_body2.addOrReplaceChild("cube_r118",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(false).addBox(-3.5F, -25.0F, -16.0F, 7.0F, 56.0F, 32.0F),
                PartPose.offsetAndRotation(71.2297F, -44.9729F, 34.991F, -0.1309F, 0.0F, 0.829F));

        PartDefinition part_cube_r119 = part_body2.addOrReplaceChild("cube_r119",
                CubeListBuilder.create()
                .texOffs(84, 634).mirror(true).addBox(-2.0F, -29.5F, -17.0F, 4.0F, 59.0F, 34.0F),
                PartPose.offsetAndRotation(-7.5639F, -48.0518F, 14.9925F, 0.0869F, -0.0076F, 0.2615F));

        PartDefinition part_cube_r120 = part_body2.addOrReplaceChild("cube_r120",
                CubeListBuilder.create()
                .texOffs(492, 683).mirror(false).addBox(-2.0F, -24.5F, -16.0F, 4.0F, 49.0F, 32.0F),
                PartPose.offsetAndRotation(-12.4605F, -45.237F, 15.3094F, 0.0869F, -0.0076F, 0.2615F));

        PartDefinition part_cube_r121 = part_body2.addOrReplaceChild("cube_r121",
                CubeListBuilder.create()
                .texOffs(84, 634).mirror(false).addBox(-2.0F, -29.5F, -17.0F, 4.0F, 59.0F, 34.0F),
                PartPose.offsetAndRotation(7.5639F, -48.0518F, 14.9925F, 0.0869F, 0.0076F, -0.2615F));

        PartDefinition part_cube_r122 = part_body2.addOrReplaceChild("cube_r122",
                CubeListBuilder.create()
                .texOffs(564, 692).mirror(false).addBox(-2.0F, -16.5F, -16.0F, 4.0F, 41.0F, 32.0F),
                PartPose.offsetAndRotation(12.4605F, -45.237F, 15.3094F, 0.0869F, 0.0076F, -0.2615F));

        PartDefinition part_cube_r123 = part_body2.addOrReplaceChild("cube_r123",
                CubeListBuilder.create()
                .texOffs(160, 674).mirror(true).addBox(-2.6946F, -36.2623F, 27.9994F, 4.0F, 61.0F, 32.0F),
                PartPose.offsetAndRotation(-12.4605F, -45.237F, 15.3094F, 0.0426F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r124 = part_body2.addOrReplaceChild("cube_r124",
                CubeListBuilder.create()
                .texOffs(160, 674).mirror(false).addBox(-1.3054F, -36.2623F, 27.9994F, 4.0F, 61.0F, 32.0F),
                PartPose.offsetAndRotation(12.4605F, -45.237F, 15.3094F, 0.0426F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r125 = part_body2.addOrReplaceChild("cube_r125",
                CubeListBuilder.create()
                .texOffs(632, 0).mirror(false).addBox(-5.5F, -49.0F, -20.0F, 11.0F, 17.0F, 17.0F)
                .texOffs(694, 371).mirror(false).addBox(-7.5F, -32.0F, -20.0F, 15.0F, 16.0F, 29.0F)
                .texOffs(0, 545).mirror(true).addBox(-9.5F, -16.0F, -20.0F, 19.0F, 58.0F, 40.0F),
                PartPose.offsetAndRotation(-49.5629F, -57.3295F, 5.7363F, 0.1745F, 0.0F, -0.4363F));

        PartDefinition part_cube_r126 = part_body2.addOrReplaceChild("cube_r126",
                CubeListBuilder.create()
                .texOffs(0, 731).mirror(false).addBox(-5.5F, -49.0F, -20.0F, 11.0F, 17.0F, 17.0F)
                .texOffs(232, 706).mirror(false).addBox(-7.5F, -32.0F, -20.0F, 15.0F, 16.0F, 29.0F)
                .texOffs(0, 545).mirror(false).addBox(-9.5F, -16.0F, -20.0F, 19.0F, 58.0F, 40.0F),
                PartPose.offsetAndRotation(49.5629F, -57.3295F, 5.7363F, 0.1745F, 0.0F, 0.4363F));

        PartDefinition part_cube_r127 = part_body2.addOrReplaceChild("cube_r127",
                CubeListBuilder.create()
                .texOffs(400, 0).mirror(true).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F)
                .texOffs(400, 0).mirror(false).addBox(-115.0F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(56.75F, 0.1184F, 31.6256F, -0.3927F, 0.0F, 0.0F));

        PartDefinition part_cube_r128 = part_body2.addOrReplaceChild("cube_r128",
                CubeListBuilder.create()
                .texOffs(500, 508).mirror(true).addBox(-1.5F, -8.5F, -7.5F, 3.0F, 17.0F, 15.0F)
                .texOffs(500, 508).mirror(false).addBox(-115.0F, -8.5F, -7.5F, 3.0F, 17.0F, 15.0F),
                PartPose.offsetAndRotation(56.75F, -9.2548F, 34.4442F, -0.3054F, 0.0F, 0.0F));

        PartDefinition part_cube_r129 = part_body2.addOrReplaceChild("cube_r129",
                CubeListBuilder.create()
                .texOffs(400, 0).mirror(true).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F)
                .texOffs(400, 0).mirror(false).addBox(-115.0F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(56.75F, -18.5293F, 37.1446F, -0.2618F, 0.0F, 0.0F));

        PartDefinition part_cube_r130 = part_body2.addOrReplaceChild("cube_r130",
                CubeListBuilder.create()
                .texOffs(400, 0).mirror(true).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F)
                .texOffs(400, 0).mirror(false).addBox(-115.0F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(56.75F, 3.1784F, 13.0094F, 0.48F, 0.0F, 0.0F));

        PartDefinition part_cube_r131 = part_body2.addOrReplaceChild("cube_r131",
                CubeListBuilder.create()
                .texOffs(500, 508).mirror(true).addBox(3.8358F, -119.6999F, -20.5582F, 3.0F, 17.0F, 15.0F)
                .texOffs(500, 508).mirror(false).addBox(-109.6642F, -119.6999F, -20.5582F, 3.0F, 17.0F, 15.0F),
                PartPose.offsetAndRotation(51.4142F, 86.4706F, 71.0902F, 0.48F, 0.0F, 0.0F));

        PartDefinition part_cube_r132 = part_body2.addOrReplaceChild("cube_r132",
                CubeListBuilder.create()
                .texOffs(400, 0).mirror(true).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F)
                .texOffs(400, 0).mirror(false).addBox(-115.0F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(56.75F, -15.4489F, 3.3127F, 0.48F, 0.0F, 0.0F));

        PartDefinition part_body3 = part_body2.addOrReplaceChild("body3",
                CubeListBuilder.create()
                .texOffs(0, 98).mirror(false).addBox(-49.5F, -26.25F, -0.196F, 98.0F, 52.0F, 44.0F)
                .texOffs(0, 477).mirror(false).addBox(-34.75F, 25.75F, -0.196F, 70.0F, 27.0F, 41.0F)
                .texOffs(214, 372).mirror(false).addBox(-48.0F, 25.75F, -0.196F, 96.0F, 7.0F, 44.0F)
                .texOffs(0, 194).mirror(false).addBox(-44.75F, -18.25F, 43.804F, 89.0F, 40.0F, 53.0F)
                .texOffs(258, 314).mirror(false).addBox(-43.75F, 21.75F, 43.804F, 87.0F, 5.0F, 53.0F),
                PartPose.offset(-0.25F, 3.0F, 41.196F));

        PartDefinition part_cube_r133 = part_body3.addOrReplaceChild("cube_r133",
                CubeListBuilder.create()
                .texOffs(231, 201).mirror(true).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F)
                .texOffs(231, 201).mirror(false).addBox(-93.0F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(46.0F, 2.1338F, 47.7726F, -0.6109F, 0.0F, 0.0F));

        PartDefinition part_cube_r134 = part_body3.addOrReplaceChild("cube_r134",
                CubeListBuilder.create()
                .texOffs(33, 0).mirror(true).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F)
                .texOffs(33, 0).mirror(false).addBox(-93.0F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(46.0F, -15.0684F, 59.8177F, -0.6109F, 0.0F, 0.0F));

        PartDefinition part_cube_r135 = part_body3.addOrReplaceChild("cube_r135",
                CubeListBuilder.create()
                .texOffs(268, 0).mirror(true).addBox(-1.5F, -8.5F, -5.0F, 3.0F, 17.0F, 10.0F)
                .texOffs(268, 0).mirror(false).addBox(-93.0F, -8.5F, -5.0F, 3.0F, 17.0F, 10.0F),
                PartPose.offsetAndRotation(46.0F, -6.4673F, 53.7951F, -0.6109F, 0.0F, 0.0F));

        PartDefinition part_cube_r136 = part_body3.addOrReplaceChild("cube_r136",
                CubeListBuilder.create()
                .texOffs(400, 0).mirror(true).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F)
                .texOffs(400, 0).mirror(false).addBox(-103.0F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(51.0F, -2.745F, 16.3967F, -0.6109F, 0.0F, 0.0F));

        PartDefinition part_cube_r137 = part_body3.addOrReplaceChild("cube_r137",
                CubeListBuilder.create()
                .texOffs(500, 508).mirror(true).addBox(-1.5F, -8.5F, -7.5F, 3.0F, 17.0F, 15.0F)
                .texOffs(500, 508).mirror(false).addBox(-103.0F, -8.5F, -7.5F, 3.0F, 17.0F, 15.0F),
                PartPose.offsetAndRotation(51.0F, -11.3461F, 22.4192F, -0.6109F, 0.0F, 0.0F));

        PartDefinition part_cube_r138 = part_body3.addOrReplaceChild("cube_r138",
                CubeListBuilder.create()
                .texOffs(400, 0).mirror(true).addBox(-1.5F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F)
                .texOffs(400, 0).mirror(false).addBox(-103.0F, -2.0F, -3.5F, 3.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(51.0F, -19.9472F, 28.4418F, -0.6109F, 0.0F, 0.0F));

        PartDefinition part_cube_r139 = part_body3.addOrReplaceChild("cube_r139",
                CubeListBuilder.create()
                .texOffs(0, 545).mirror(true).addBox(-2.0F, -13.0F, -2.5F, 4.0F, 26.0F, 13.0F),
                PartPose.offsetAndRotation(-32.8079F, -23.1674F, 88.9601F, -0.48F, 0.0F, -0.829F));

        PartDefinition part_cube_r140 = part_body3.addOrReplaceChild("cube_r140",
                CubeListBuilder.create()
                .texOffs(0, 287).mirror(true).addBox(-2.5F, -14.5F, -5.5F, 5.0F, 29.0F, 23.0F),
                PartPose.offsetAndRotation(-44.7659F, -25.3087F, 64.1706F, -0.5672F, 0.0F, -0.829F));

        PartDefinition part_cube_r141 = part_body3.addOrReplaceChild("cube_r141",
                CubeListBuilder.create()
                .texOffs(0, 545).mirror(false).addBox(-2.0F, -13.0F, -2.5F, 4.0F, 26.0F, 13.0F),
                PartPose.offsetAndRotation(33.3079F, -23.1674F, 88.9601F, -0.48F, 0.0F, 0.829F));

        PartDefinition part_cube_r142 = part_body3.addOrReplaceChild("cube_r142",
                CubeListBuilder.create()
                .texOffs(0, 287).mirror(false).addBox(-2.5F, -14.5F, -5.5F, 5.0F, 29.0F, 23.0F),
                PartPose.offsetAndRotation(45.2659F, -25.3087F, 64.1706F, -0.5672F, 0.0F, 0.829F));

        PartDefinition part_cube_r143 = part_body3.addOrReplaceChild("cube_r143",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(true).addBox(-1.5F, -21.5F, 2.0F, 7.0F, 56.0F, 32.0F),
                PartPose.offsetAndRotation(-66.3398F, -42.9324F, 32.0487F, -0.7854F, 0.0F, -0.829F));

        PartDefinition part_cube_r144 = part_body3.addOrReplaceChild("cube_r144",
                CubeListBuilder.create()
                .texOffs(0, 643).mirror(false).addBox(-5.5F, -21.5F, 2.0F, 7.0F, 56.0F, 32.0F),
                PartPose.offsetAndRotation(66.8398F, -42.9324F, 32.0487F, -0.7854F, 0.0F, 0.829F));

        PartDefinition part_cube_r145 = part_body3.addOrReplaceChild("cube_r145",
                CubeListBuilder.create()
                .texOffs(524, 573).mirror(true).addBox(-2.0F, -35.5F, -19.5F, 4.0F, 71.0F, 39.0F),
                PartPose.offsetAndRotation(-7.964F, -62.406F, 17.2484F, 0.0426F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r146 = part_body3.addOrReplaceChild("cube_r146",
                CubeListBuilder.create()
                .texOffs(450, 377).mirror(true).addBox(-2.0F, -11.5F, -8.0F, 4.0F, 23.0F, 16.0F),
                PartPose.offsetAndRotation(-7.6934F, -22.8783F, 86.7538F, -0.1309F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r147 = part_body3.addOrReplaceChild("cube_r147",
                CubeListBuilder.create()
                .texOffs(0, 98).mirror(true).addBox(-2.0F, -11.5F, -9.0F, 4.0F, 23.0F, 18.0F),
                PartPose.offsetAndRotation(-2.9418F, -28.2521F, 87.4916F, -0.1309F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r148 = part_body3.addOrReplaceChild("cube_r148",
                CubeListBuilder.create()
                .texOffs(706, 179).mirror(true).addBox(-2.0F, -24.5F, -13.5F, 4.0F, 49.0F, 27.0F),
                PartPose.offsetAndRotation(-11.0755F, -30.3972F, 57.1576F, 0.0F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r149 = part_body3.addOrReplaceChild("cube_r149",
                CubeListBuilder.create()
                .texOffs(408, 693).mirror(true).addBox(-2.0F, -26.0F, -14.5F, 4.0F, 52.0F, 29.0F),
                PartPose.offsetAndRotation(-6.1299F, -37.3102F, 57.1123F, 0.0F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r150 = part_body3.addOrReplaceChild("cube_r150",
                CubeListBuilder.create()
                .texOffs(0, 98).mirror(false).addBox(-2.0F, -11.5F, -9.0F, 4.0F, 23.0F, 18.0F),
                PartPose.offsetAndRotation(3.4418F, -28.2521F, 87.4916F, -0.1309F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r151 = part_body3.addOrReplaceChild("cube_r151",
                CubeListBuilder.create()
                .texOffs(450, 377).mirror(false).addBox(-2.0F, -11.5F, -8.0F, 4.0F, 23.0F, 16.0F),
                PartPose.offsetAndRotation(8.1934F, -22.8783F, 86.7538F, -0.1309F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r152 = part_body3.addOrReplaceChild("cube_r152",
                CubeListBuilder.create()
                .texOffs(408, 693).mirror(false).addBox(-2.0F, -26.0F, -14.5F, 4.0F, 52.0F, 29.0F),
                PartPose.offsetAndRotation(6.6299F, -37.3102F, 57.1123F, 0.0F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r153 = part_body3.addOrReplaceChild("cube_r153",
                CubeListBuilder.create()
                .texOffs(706, 179).mirror(false).addBox(-2.0F, -24.5F, -13.5F, 4.0F, 49.0F, 27.0F),
                PartPose.offsetAndRotation(11.5755F, -30.3972F, 57.1576F, 0.0F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r154 = part_body3.addOrReplaceChild("cube_r154",
                CubeListBuilder.create()
                .texOffs(524, 573).mirror(false).addBox(-2.0F, -35.5F, -19.5F, 4.0F, 71.0F, 39.0F),
                PartPose.offsetAndRotation(8.464F, -62.406F, 17.2484F, 0.0426F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r155 = part_body3.addOrReplaceChild("cube_r155",
                CubeListBuilder.create()
                .texOffs(408, 633).mirror(true).addBox(-2.5F, -9.0F, -6.0F, 5.0F, 6.0F, 9.0F)
                .texOffs(502, 142).mirror(true).addBox(-2.5F, -17.0F, 3.0F, 5.0F, 14.0F, 9.0F)
                .texOffs(320, 725).mirror(true).addBox(-4.5F, -3.0F, -12.0F, 9.0F, 21.0F, 24.0F),
                PartPose.offsetAndRotation(-21.2546F, -36.0789F, 92.804F, 0.0F, 0.0F, -0.0436F));

        PartDefinition part_cube_r156 = part_body3.addOrReplaceChild("cube_r156",
                CubeListBuilder.create()
                .texOffs(130, 545).mirror(true).addBox(-2.5F, -26.5F, 6.0F, 5.0F, 11.0F, 8.0F)
                .texOffs(366, 0).mirror(true).addBox(-3.5F, -15.5F, -6.0F, 7.0F, 6.0F, 20.0F)
                .texOffs(700, 618).mirror(true).addBox(-5.5F, -9.5F, -14.0F, 11.0F, 31.0F, 28.0F),
                PartPose.offsetAndRotation(-28.4007F, -39.2703F, 60.804F, 0.0F, 0.0F, -0.0436F));

        PartDefinition part_cube_r157 = part_body3.addOrReplaceChild("cube_r157",
                CubeListBuilder.create()
                .texOffs(492, 61).mirror(true).addBox(-5.0F, -50.0F, 10.5F, 10.0F, 17.0F, 12.0F)
                .texOffs(712, 435).mirror(true).addBox(-7.0F, -33.0F, -3.5F, 14.0F, 22.0F, 26.0F)
                .texOffs(118, 545).mirror(true).addBox(-9.0F, -11.0F, -22.5F, 18.0F, 44.0F, 45.0F),
                PartPose.offsetAndRotation(-37.2376F, -58.1636F, 22.304F, 0.0F, 0.0F, -0.0873F));

        PartDefinition part_cube_r158 = part_body3.addOrReplaceChild("cube_r158",
                CubeListBuilder.create()
                .texOffs(408, 633).mirror(false).addBox(-2.5F, -9.0F, -6.0F, 5.0F, 6.0F, 9.0F)
                .texOffs(502, 142).mirror(false).addBox(-2.5F, -17.0F, 3.0F, 5.0F, 14.0F, 9.0F)
                .texOffs(320, 725).mirror(false).addBox(-4.5F, -3.0F, -12.0F, 9.0F, 21.0F, 24.0F),
                PartPose.offsetAndRotation(21.7546F, -36.0789F, 92.804F, 0.0F, 0.0F, 0.0436F));

        PartDefinition part_cube_r159 = part_body3.addOrReplaceChild("cube_r159",
                CubeListBuilder.create()
                .texOffs(366, 0).mirror(false).addBox(-3.5F, -15.5F, -6.0F, 7.0F, 6.0F, 20.0F)
                .texOffs(130, 545).mirror(false).addBox(-2.5F, -26.5F, 6.0F, 5.0F, 11.0F, 8.0F)
                .texOffs(700, 618).mirror(false).addBox(-5.5F, -9.5F, -14.0F, 11.0F, 31.0F, 28.0F),
                PartPose.offsetAndRotation(28.9007F, -39.2703F, 60.804F, 0.0F, 0.0F, 0.0436F));

        PartDefinition part_cube_r160 = part_body3.addOrReplaceChild("cube_r160",
                CubeListBuilder.create()
                .texOffs(492, 61).mirror(false).addBox(-5.0F, -50.0F, 10.5F, 10.0F, 17.0F, 12.0F)
                .texOffs(712, 435).mirror(false).addBox(-7.0F, -33.0F, -3.5F, 14.0F, 22.0F, 26.0F)
                .texOffs(118, 545).mirror(false).addBox(-9.0F, -11.0F, -22.5F, 18.0F, 44.0F, 45.0F),
                PartPose.offsetAndRotation(37.7376F, -58.1636F, 22.304F, 0.0F, 0.0F, 0.0873F));

        PartDefinition part_cube_r161 = part_body3.addOrReplaceChild("cube_r161",
                CubeListBuilder.create()
                .texOffs(0, 287).mirror(false).addBox(-33.0F, -13.0F, -31.5F, 66.0F, 26.0F, 63.0F),
                PartPose.offsetAndRotation(0.25F, 24.3993F, 60.1575F, 0.5236F, 0.0F, 0.0F));

        PartDefinition part_tail = part_body3.addOrReplaceChild("tail",
                CubeListBuilder.create()
                .texOffs(222, 493).mirror(false).addBox(-27.0F, -13.25F, 3.0F, 50.0F, 23.0F, 40.0F)
                .texOffs(492, 0).mirror(false).addBox(-26.0F, 9.75F, 3.0F, 48.0F, 17.0F, 44.0F),
                PartPose.offsetAndRotation(3.0F, 8.0F, 92.804F, 0.2182F, 0.0F, 0.0F));

        PartDefinition part_cube_r162 = part_tail.addOrReplaceChild("cube_r162",
                CubeListBuilder.create()
                .texOffs(0, 376).mirror(true).addBox(-1.0F, -9.0F, -4.0F, 2.0F, 18.0F, 8.0F),
                PartPose.offsetAndRotation(-24.7499F, -12.4237F, 31.3431F, -0.2618F, 0.0F, -0.829F));

        PartDefinition part_cube_r163 = part_tail.addOrReplaceChild("cube_r163",
                CubeListBuilder.create()
                .texOffs(416, 493).mirror(true).addBox(-1.0F, -10.0F, -6.5F, 2.0F, 20.0F, 13.0F),
                PartPose.offsetAndRotation(-28.4198F, -15.7865F, 16.63F, -0.2618F, 0.0F, -0.829F));

        PartDefinition part_cube_r164 = part_tail.addOrReplaceChild("cube_r164",
                CubeListBuilder.create()
                .texOffs(0, 376).mirror(false).addBox(-1.0F, -9.0F, -4.0F, 2.0F, 18.0F, 8.0F),
                PartPose.offsetAndRotation(19.2499F, -12.4237F, 31.3431F, -0.2618F, 0.0F, 0.829F));

        PartDefinition part_cube_r165 = part_tail.addOrReplaceChild("cube_r165",
                CubeListBuilder.create()
                .texOffs(416, 493).mirror(false).addBox(-1.0F, -10.0F, -6.5F, 2.0F, 20.0F, 13.0F),
                PartPose.offsetAndRotation(22.9198F, -15.7865F, 16.63F, -0.2618F, 0.0F, 0.829F));

        PartDefinition part_cube_r166 = part_tail.addOrReplaceChild("cube_r166",
                CubeListBuilder.create()
                .texOffs(0, 477).mirror(true).addBox(-0.7608F, -12.1633F, -8.0145F, 4.0F, 24.0F, 16.0F),
                PartPose.offsetAndRotation(-8.9725F, -16.2092F, 19.4657F, -0.1745F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r167 = part_tail.addOrReplaceChild("cube_r167",
                CubeListBuilder.create()
                .texOffs(0, 194).mirror(true).addBox(-0.7608F, -11.6633F, -9.0145F, 4.0F, 23.0F, 18.0F),
                PartPose.offsetAndRotation(-4.2885F, -21.0551F, 20.3754F, -0.1745F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r168 = part_tail.addOrReplaceChild("cube_r168",
                CubeListBuilder.create()
                .texOffs(0, 477).mirror(false).addBox(-7.4698F, -12.4573F, -8.0407F, 4.0F, 24.0F, 16.0F),
                PartPose.offsetAndRotation(9.4725F, -16.2092F, 19.4657F, -0.1745F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r169 = part_tail.addOrReplaceChild("cube_r169",
                CubeListBuilder.create()
                .texOffs(0, 194).mirror(false).addBox(-7.7003F, -12.2512F, -9.0669F, 4.0F, 23.0F, 18.0F),
                PartPose.offsetAndRotation(4.7885F, -21.0551F, 20.3754F, -0.1745F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r170 = part_tail.addOrReplaceChild("cube_r170",
                CubeListBuilder.create()
                .texOffs(26, 194).mirror(true).addBox(-4.7479F, -8.0981F, -4.5F, 5.0F, 7.0F, 8.0F)
                .texOffs(390, 602).mirror(true).addBox(-7.7479F, -1.0981F, -8.5F, 11.0F, 11.0F, 17.0F),
                PartPose.offsetAndRotation(-15.9104F, -22.3046F, 26.5F, 0.0F, 0.0F, -0.0436F));

        PartDefinition part_cube_r171 = part_tail.addOrReplaceChild("cube_r171",
                CubeListBuilder.create()
                .texOffs(26, 194).mirror(false).addBox(-4.7479F, -7.9019F, -4.5F, 5.0F, 7.0F, 8.0F)
                .texOffs(390, 602).mirror(false).addBox(-7.7479F, -0.9019F, -8.5F, 11.0F, 11.0F, 17.0F),
                PartPose.offsetAndRotation(16.4104F, -22.3046F, 26.5F, 0.0F, 0.0F, 0.0436F));

        PartDefinition part_tail2 = part_tail.addOrReplaceChild("tail2",
                CubeListBuilder.create()
                .texOffs(483, 317).mirror(false).addBox(-22.0F, -13.1576F, -7.0383F, 40.0F, 21.0F, 55.0F)
                .texOffs(468, 433).mirror(false).addBox(-21.0F, 7.75F, -13.0F, 38.0F, 15.0F, 60.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 43.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition part_cube_r172 = part_tail2.addOrReplaceChild("cube_r172",
                CubeListBuilder.create()
                .texOffs(484, 423).mirror(false).addBox(-4.2306F, -11.7118F, -8.0634F, 4.0F, 24.0F, 16.0F),
                PartPose.offsetAndRotation(-5.5576F, -15.3706F, 20.1413F, 0.1309F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r173 = part_tail2.addOrReplaceChild("cube_r173",
                CubeListBuilder.create()
                .texOffs(0, 420).mirror(true).addBox(-4.2306F, -11.2118F, -9.0634F, 4.0F, 23.0F, 18.0F),
                PartPose.offsetAndRotation(-0.8876F, -20.2552F, 19.3781F, 0.1309F, 0.0113F, 0.1311F));

        PartDefinition part_cube_r174 = part_tail2.addOrReplaceChild("cube_r174",
                CubeListBuilder.create()
                .texOffs(362, 493).mirror(false).addBox(-4.2306F, -12.2882F, -7.9366F, 4.0F, 24.0F, 16.0F),
                PartPose.offsetAndRotation(6.0576F, -15.3706F, 20.1413F, 0.1309F, -0.0113F, -0.1311F));

        PartDefinition part_cube_r175 = part_tail2.addOrReplaceChild("cube_r175",
                CubeListBuilder.create()
                .texOffs(0, 420).mirror(false).addBox(-4.2306F, -11.7882F, -8.9366F, 4.0F, 23.0F, 18.0F),
                PartPose.offsetAndRotation(1.3876F, -20.2552F, 19.3781F, 0.1309F, -0.0113F, -0.1311F));

        PartDefinition part_tail3 = part_tail2.addOrReplaceChild("tail3",
                CubeListBuilder.create()
                .texOffs(244, 568).mirror(false).addBox(-17.0F, -8.9391F, -0.0793F, 30.0F, 21.0F, 43.0F)
                .texOffs(487, 508).mirror(false).addBox(-16.0F, 12.0F, -11.0F, 28.0F, 12.0F, 53.0F),
                PartPose.offsetAndRotation(0.0F, -4.25F, 40.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition part_cube_r176 = part_tail3.addOrReplaceChild("cube_r176",
                CubeListBuilder.create()
                .texOffs(0, 287).mirror(true).addBox(-26.5F, -0.5F, 13.75F, 10.0F, 21.0F, 0.0F)
                .texOffs(494, 0).mirror(true).addBox(-16.5F, -10.5F, 13.75F, 20.0F, 21.0F, 0.0F),
                PartPose.offsetAndRotation(-29.0F, -20.25F, 16.0F, 0.0F, 1.5708F, 1.5708F));

        PartDefinition part_cube_r177 = part_tail3.addOrReplaceChild("cube_r177",
                CubeListBuilder.create()
                .texOffs(0, 287).mirror(false).addBox(16.5F, -0.5F, 13.75F, 10.0F, 21.0F, 0.0F)
                .texOffs(494, 0).mirror(false).addBox(-3.5F, -10.5F, 13.75F, 20.0F, 21.0F, 0.0F),
                PartPose.offsetAndRotation(23.5F, -20.25F, 16.0F, 0.0F, -1.5708F, -1.5708F));

        PartDefinition part_cube_r178 = part_tail3.addOrReplaceChild("cube_r178",
                CubeListBuilder.create()
                .texOffs(323, 201).mirror(true).addBox(-14.0F, -10.5F, 0.0F, 48.0F, 21.0F, 0.0F),
                PartPose.offsetAndRotation(-18.75F, 12.7291F, 19.6049F, 1.5708F, 0.3927F, 1.5708F));

        PartDefinition part_cube_r179 = part_tail3.addOrReplaceChild("cube_r179",
                CubeListBuilder.create()
                .texOffs(323, 201).mirror(false).addBox(-34.0F, -10.5F, 0.0F, 48.0F, 21.0F, 0.0F),
                PartPose.offsetAndRotation(13.25F, 12.7291F, 19.6049F, 1.5708F, -0.3927F, -1.5708F));

        PartDefinition part_tail4 = part_tail3.addOrReplaceChild("tail4",
                CubeListBuilder.create()
                .texOffs(416, 508).mirror(false).addBox(-15.0F, -9.0087F, -2.0996F, 26.0F, 21.0F, 32.0F)
                .texOffs(503, 255).mirror(false).addBox(-14.0F, 2.0F, -13.0F, 24.0F, 15.0F, 42.0F)
                .texOffs(502, 142).mirror(false).addBox(-13.0F, -6.0087F, 29.9004F, 22.0F, 18.0F, 32.0F)
                .texOffs(195, 314).mirror(false).addBox(-12.0F, 12.0F, 29.0F, 20.0F, 2.0F, 33.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 37.0F, 0.7418F, 0.0F, 0.0F));

        PartDefinition part_cube_r180 = part_tail4.addOrReplaceChild("cube_r180",
                CubeListBuilder.create()
                .texOffs(33, 287).mirror(true).addBox(-81.5F, -15.75F, 14.75F, 15.0F, 21.0F, 0.0F),
                PartPose.offsetAndRotation(-29.0F, -20.25F, -21.0F, 0.0F, 1.5708F, 1.5708F));

        PartDefinition part_cube_r181 = part_tail4.addOrReplaceChild("cube_r181",
                CubeListBuilder.create()
                .texOffs(33, 287).mirror(false).addBox(66.5F, -14.5F, 14.75F, 15.0F, 21.0F, 0.0F),
                PartPose.offsetAndRotation(23.5F, -20.25F, -21.0F, 0.0F, -1.5708F, -1.5708F));

        PartDefinition part_tail5 = part_tail4.addOrReplaceChild("tail5",
                CubeListBuilder.create()
                .texOffs(692, 539).mirror(false).addBox(-11.0F, -5.0F, 2.0F, 18.0F, 15.0F, 32.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 56.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition part_cube_r182 = part_tail5.addOrReplaceChild("cube_r182",
                CubeListBuilder.create()
                .texOffs(78, 545).mirror(false).addBox(-71.6263F, 271.0846F, 27.4794F, 10.0F, 5.0F, 32.0F),
                PartPose.offsetAndRotation(48.6642F, -236.5456F, -73.7864F, 0.484F, 0.4318F, 0.2166F));

        PartDefinition part_cube_r183 = part_tail5.addOrReplaceChild("cube_r183",
                CubeListBuilder.create()
                .texOffs(320, 633).mirror(false).addBox(-22.0F, -46.0F, 0.0F, 44.0F, 92.0F, 0.0F),
                PartPose.offsetAndRotation(47.6189F, -47.0161F, 171.8549F, 1.8708F, -0.5707F, 1.9396F));

        PartDefinition part_cube_r184 = part_tail5.addOrReplaceChild("cube_r184",
                CubeListBuilder.create()
                .texOffs(320, 633).mirror(false).addBox(-22.0F, -46.0F, 0.0F, 44.0F, 92.0F, 0.0F),
                PartPose.offsetAndRotation(-35.9392F, -28.5729F, 157.3623F, 1.6682F, -0.2842F, 0.4388F));

        PartDefinition part_cube_r185 = part_tail5.addOrReplaceChild("cube_r185",
                CubeListBuilder.create()
                .texOffs(320, 633).mirror(false).addBox(-21.5F, -46.0F, 11.0F, 44.0F, 92.0F, 0.0F),
                PartPose.offsetAndRotation(-38.2952F, -38.9666F, 154.5927F, 1.3067F, 0.1442F, 2.8335F));

        PartDefinition part_cube_r186 = part_tail5.addOrReplaceChild("cube_r186",
                CubeListBuilder.create()
                .texOffs(320, 633).mirror(false).addBox(-22.0F, 20.0F, 46.0F, 44.0F, 92.0F, 0.0F),
                PartPose.offsetAndRotation(17.1435F, 19.4153F, 138.2376F, 2.0548F, 0.4318F, 0.2166F));

        PartDefinition part_cube_r187 = part_tail5.addOrReplaceChild("cube_r187",
                CubeListBuilder.create()
                .texOffs(465, 62).mirror(false).addBox(-5.0F, -2.5F, -37.5F, 10.0F, 5.0F, 75.0F),
                PartPose.offsetAndRotation(34.3461F, -36.1372F, 148.5251F, 0.484F, 0.4318F, 0.2166F));

        PartDefinition part_cube_r188 = part_tail5.addOrReplaceChild("cube_r188",
                CubeListBuilder.create()
                .texOffs(610, 646).mirror(false).addBox(-22.5F, -39.0F, 0.0F, 45.0F, 78.0F, 0.0F),
                PartPose.offsetAndRotation(88.5177F, -16.9164F, 105.6908F, -0.5672F, -0.3491F, 1.5708F));

        PartDefinition part_cube_r189 = part_tail5.addOrReplaceChild("cube_r189",
                CubeListBuilder.create()
                .texOffs(610, 646).mirror(false).addBox(-22.0F, -5.5F, 32.0F, 45.0F, 78.0F, 0.0F),
                PartPose.offsetAndRotation(116.5026F, -53.287F, 112.0565F, -0.9754F, 0.9149F, 0.4927F));

        PartDefinition part_cube_r190 = part_tail5.addOrReplaceChild("cube_r190",
                CubeListBuilder.create()
                .texOffs(578, 135).mirror(false).addBox(-2.5F, -2.5F, -28.5F, 5.0F, 5.0F, 57.0F),
                PartPose.offsetAndRotation(80.9699F, -14.8779F, 101.3157F, 0.5954F, 0.9149F, 0.4927F));

        PartDefinition part_cube_r191 = part_tail5.addOrReplaceChild("cube_r191",
                CubeListBuilder.create()
                .texOffs(578, 255).mirror(false).addBox(-2.5F, -2.5F, -28.5F, 5.0F, 5.0F, 57.0F),
                PartPose.offsetAndRotation(30.6465F, -7.7557F, 81.7476F, 1.0343F, 1.1615F, 0.9957F));

        PartDefinition part_cube_r192 = part_tail5.addOrReplaceChild("cube_r192",
                CubeListBuilder.create()
                .texOffs(126, 376).mirror(false).addBox(-9.0F, -3.5F, -16.0F, 18.0F, 7.0F, 32.0F),
                PartPose.offsetAndRotation(-2.0F, -7.336F, 79.1326F, 0.3491F, 0.0F, 0.0F));

        PartDefinition part_cube_r193 = part_tail5.addOrReplaceChild("cube_r193",
                CubeListBuilder.create()
                .texOffs(390, 568).mirror(false).addBox(-2.5F, -1.5F, -31.0F, 5.0F, 3.0F, 62.0F),
                PartPose.offsetAndRotation(-40.4457F, -18.5039F, 127.8134F, 0.3731F, -0.5542F, -0.3558F));

        PartDefinition part_cube_r194 = part_tail5.addOrReplaceChild("cube_r194",
                CubeListBuilder.create()
                .texOffs(331, 493).mirror(false).addBox(-3.5F, -2.0F, -35.5F, 7.0F, 4.0F, 71.0F),
                PartPose.offsetAndRotation(-30.9019F, -22.3121F, 136.5209F, 0.3208F, -0.1842F, -0.2134F));

        PartDefinition part_cube_r195 = part_tail5.addOrReplaceChild("cube_r195",
                CubeListBuilder.create()
                .texOffs(618, 317).mirror(false).addBox(-5.5F, -2.5F, -24.5F, 11.0F, 5.0F, 49.0F),
                PartPose.offsetAndRotation(-16.9331F, -11.4184F, 98.5958F, 0.3638F, -0.5135F, -0.3375F));

        PartDefinition part_cube_r196 = part_tail5.addOrReplaceChild("cube_r196",
                CubeListBuilder.create()
                .texOffs(668, 692).mirror(false).addBox(-9.0F, -6.0707F, -16.0707F, 18.0F, 12.0F, 32.0F),
                PartPose.offsetAndRotation(-2.0F, -0.5402F, 47.9787F, 0.2182F, 0.0F, 0.0F));

    }

    @Override
    public void setupAnim(AzhdahaBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.leftHand.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        this.neck.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.neck.xRot = headPitch * ((float) Math.PI / 180F);
        this.rightLeg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.rightHand.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
