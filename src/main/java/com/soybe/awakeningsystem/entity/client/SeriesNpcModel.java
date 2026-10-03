package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SeriesNpcEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class SeriesNpcModel extends HierarchicalModel<SeriesNpcEntity> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "series_npc"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public SeriesNpcModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
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

        // Brazo derecho y Manga derecha (Capa base UV: 40, 16 | Capa exterior UV: 40, 32)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        // Brazo izquierdo y Manga izquierda (Capa base UV: 32, 48 | Capa exterior UV: 48, 48)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

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
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SeriesNpcEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Giro natural de cabeza siguiendo al jugador
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Ciclo de marcha humanoide
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;

        // Respiración / Balanceo sutil en reposo
        if (limbSwingAmount < 0.1F) {
            float breath = Mth.sin(ageInTicks * 0.06F) * 0.025F;
            this.body.y += breath * 1.5F;
            this.head.y += breath * 1.5F;
            this.rightArm.y += breath * 1.5F;
            this.leftArm.y += breath * 1.5F;
            this.rightArm.zRot = 0.05F + breath;
            this.leftArm.zRot = -0.05F - breath;
        }

        // Postura marcial erguida sosteniendo armas insignes
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();

        if (!mainHand.isEmpty()) {
            boolean isBow = mainHand.getItem() instanceof net.minecraft.world.item.BowItem
                    || entity.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.GANYU
                    || entity.getNpcProfile() == com.soybe.awakeningsystem.entity.NpcProfile.MARCH_7TH;

            if (isBow) {
                // Postura de arquera elegante con arco en mano
                this.rightArm.xRot = -0.40F;
                this.rightArm.yRot = -0.20F;
                this.rightArm.zRot = 0.08F;
            } else {
                // Postura marcial heroica sosteniendo espada / lanza / daga
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.35F;
                this.rightArm.yRot = -0.12F;
                this.rightArm.zRot = 0.06F;
            }
        }

        if (!offHand.isEmpty()) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.35F;
            this.leftArm.yRot = 0.12F;
            this.leftArm.zRot = -0.06F;
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
}
