package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.CompanionEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

public class CompanionModel extends HierarchicalModel<CompanionEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion"), "main");
    public static final ModelLayerLocation SLIM_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "companion_slim"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CompanionModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(false);
    }

    public static LayerDefinition createBodyLayer(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cabeza y Casco/Gorro (Capa base UV: 0, 0 | Capa exterior UV: 32, 0)
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Torso y Chaqueta (Capa base UV: 16, 16 | Capa exterior UV: 16, 32)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        if (slim) {
            // Brazo derecho y Manga derecha (Slim: 3px ancho UV: 40, 16 | 40, 32)
            root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                    .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F));

            // Brazo izquierdo y Manga izquierda (Slim: 3px ancho UV: 32, 48 | 48, 48)
            root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                    .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                    PartPose.offset(5.0F, 2.5F, 0.0F));
        } else {
            // Brazo derecho y Manga derecha (Default: 4px ancho UV: 40, 16 | 40, 32)
            root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                    .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                    PartPose.offset(-5.0F, 2.0F, 0.0F));

            // Brazo izquierdo y Manga izquierda (Default: 4px ancho UV: 32, 48 | 48, 48)
            root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                    .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                    PartPose.offset(5.0F, 2.0F, 0.0F));
        }

        // Pierna derecha y Pantalón derecho (Capa base UV: 0, 16 | Capa exterior UV: 0, 32)
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        // Pierna izquierda y Pantalón izquierdo (Capa base UV: 16, 48 | Capa exterior UV: 0, 48)
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(CompanionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        if (entity.isOrderedToSit()) {
            // Animación sentada / guardia
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = 0.31415927F;
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = -0.31415927F;
            this.leftLeg.zRot = -0.07853982F;
            this.rightArm.xRot = -0.62831855F;
            this.leftArm.xRot = -0.62831855F;
        } else {
            // Animación de caminar estándar
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 2.0F * limbSwingAmount * 0.5F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        }

        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty()) {
            if (mainHand.getItem() instanceof net.minecraft.world.item.BowItem) {
                this.rightArm.xRot = -1.5F + this.head.xRot;
                this.rightArm.yRot = -0.3F + this.head.yRot;
                this.leftArm.xRot = -1.5F + this.head.xRot;
                this.leftArm.yRot = 0.5F + this.head.yRot;
            } else {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.35F;
                this.rightArm.yRot = -0.12F;
                this.rightArm.zRot = 0.06F;
            }
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        if (arm == HumanoidArm.RIGHT) {
            this.rightArm.translateAndRotate(poseStack);
        } else {
            this.leftArm.translateAndRotate(poseStack);
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
