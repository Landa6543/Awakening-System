package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ShadowDirewolfEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D para el Lobo Plateado de las Sombras (Shadow Direwolf - Ranga).
 * Cuenta con melena de pelaje en el cuello, cuerno de relámpago en la frente,
 * cola tupida articulada y animaciones de carrera, aullido y sentado.
 */
public class ShadowDirewolfModel extends HierarchicalModel<ShadowDirewolfEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "shadow_direwolf"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart mane;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart horn;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;
    private final ModelPart tail1;
    private final ModelPart tail2;

    public ShadowDirewolfModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.mane = this.body.getChild("mane");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.horn = this.head.getChild("horn");

        this.legFrontLeft = this.body.getChild("leg_front_left");
        this.legFrontRight = this.body.getChild("leg_front_right");
        this.legBackLeft = this.body.getChild("leg_back_left");
        this.legBackRight = this.body.getChild("leg_back_right");

        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -7.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        body.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(50, 0)
                .addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 7.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -0.5F, -4.5F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(85, 0)
                .addBox(-2.5F, -4.0F, -4.5F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, -6.5F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 25)
                .addBox(-3.0F, -3.0F, -4.5F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(26, 25)
                .addBox(-2.0F, 0.0F, -8.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(65, 25)
                .addBox(-2.8F, -5.5F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(73, 25)
                .addBox(0.8F, -5.5F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -3.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(46, 25)
                .addBox(-1.5F, 0.0F, -4.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.0F, -4.0F));

        head.addOrReplaceChild("horn", CubeListBuilder.create()
                .texOffs(82, 25)
                .addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.8F, -3.5F, -0.35F, 0.0F, 0.0F));

        body.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                .texOffs(0, 42)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(-0.05F)),
                PartPose.offset(-3.0F, 1.0F, -4.5F));

        body.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                .texOffs(14, 42)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(-0.05F)),
                PartPose.offset(3.0F, 1.0F, -4.5F));

        PartDefinition legBackLeft = body.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                .texOffs(30, 42)
                .addBox(-2.0F, 0.0F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 0.0F, 4.5F));
        legBackLeft.addOrReplaceChild("leg_back_left_shin", CubeListBuilder.create()
                .texOffs(50, 42)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 6.0F, 0.5F));

        PartDefinition legBackRight = body.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                .texOffs(65, 42)
                .addBox(-2.0F, 0.0F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 0.0F, 4.5F));
        legBackRight.addOrReplaceChild("leg_back_right_shin", CubeListBuilder.create()
                .texOffs(85, 42)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 6.0F, 0.5F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 60)
                .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.65F, 0.0F, 0.0F));

        tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(20, 60)
                .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.5F, 0.35F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ShadowDirewolfEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean isSitting = entity.isInSittingPose();
        boolean isHowling = entity.isHowling();

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.65F;
        this.neck.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.35F;

        if (isHowling) {
            this.neck.xRot = -0.6F;
            this.head.xRot = -0.65F;
            this.jaw.xRot = 0.45F;
            this.tail1.xRot = -0.3F;
            this.tail1.yRot = Mth.sin(ageInTicks * 0.3F) * 0.15F;
        } else {
            this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.7F;
        }

        if (isSitting) {
            this.body.y = 16.5F;
            this.body.xRot = -0.45F;

            this.legBackLeft.xRot = -1.15F;
            this.legBackRight.xRot = -1.15F;
            this.legBackLeft.y = 2.5F;
            this.legBackRight.y = 2.5F;

            this.legFrontLeft.xRot = 0.45F;
            this.legFrontRight.xRot = 0.45F;
            this.legFrontLeft.y = 0.0F;
            this.legFrontRight.y = 0.0F;

            this.neck.xRot = 0.35F;
            this.tail1.xRot = 0.9F;
            this.tail1.yRot = 0.35F + Mth.sin(ageInTicks * 0.05F) * 0.08F;
            this.tail2.xRot = 0.15F;
            this.tail2.yRot = 0.25F;
        } else {
            this.legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
            this.legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.2F * limbSwingAmount;
            this.legBackLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
            this.legBackRight.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount;

            this.tail1.yRot = Mth.cos(limbSwing * 0.5F) * 0.4F * limbSwingAmount + Mth.sin(ageInTicks * 0.08F) * 0.08F;
            this.tail2.yRot = Mth.cos(limbSwing * 0.5F - 0.4F) * 0.3F * limbSwingAmount + Mth.sin(ageInTicks * 0.08F - 0.5F) * 0.1F;

            this.body.y = 13.0F + Mth.sin(ageInTicks * 0.06F) * 0.15F;
        }
    }
}
