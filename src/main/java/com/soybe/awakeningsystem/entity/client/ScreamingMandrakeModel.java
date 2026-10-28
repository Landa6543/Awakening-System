package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ScreamingMandrakeEntity;
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

/**
 * Modelo 3D de la Mandrágora Cantarina (Screaming Mandrake).
 * Planta / Raíz mágica con doble estado:
 * - Enterrada: Solo se ven las hojas y flor mística en la superficie.
 * - Desenterrada: Raíz tuberosa rechoncha con carita gritona, bracitos en alto
 *   agitándose histéricamente y patitas correteando.
 */
public class ScreamingMandrakeModel<T extends ScreamingMandrakeEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "screaming_mandrake"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart foliage;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ScreamingMandrakeModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.foliage = this.body.getChild("foliage");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -4.5F, -3.8F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition foliage = body.addOrReplaceChild("foliage", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 15).addBox(-2.5F, -4.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(8, 15).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        foliage.addOrReplaceChild("leaf_north", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-2.0F, -0.5F, -6.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, -1.0F, -0.32F, 0.0F, 0.0F));

        foliage.addOrReplaceChild("leaf_south", CubeListBuilder.create()
                .texOffs(20, 23).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, 0.32F, 0.0F, 0.0F));

        foliage.addOrReplaceChild("leaf_east", CubeListBuilder.create()
                .texOffs(40, 23).addBox(0.0F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.32F));

        foliage.addOrReplaceChild("leaf_west", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-6.0F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.32F));

        foliage.addOrReplaceChild("leaf_ne", CubeListBuilder.create()
                .texOffs(20, 31).addBox(-2.0F, -0.5F, -4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.2F, 0.0F, -0.22F, 0.785F, 0.0F));

        foliage.addOrReplaceChild("leaf_sw", CubeListBuilder.create()
                .texOffs(36, 31).addBox(-2.0F, -0.5F, -4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.2F, 0.0F, -0.22F, -2.356F, 0.0F));

        body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.2F, -5.0F, 0.0F, 0.0F, 0.0F, 0.3F));

        body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.2F, -5.0F, 0.0F, 0.0F, 0.0F, -0.3F));

        body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(40, 6).addBox(-0.75F, 0.0F, -1.0F, 1.5F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, 0.0F, 0.0F));

        body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(40, 12).addBox(-0.75F, 0.0F, -1.0F, 1.5F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        if (entity.isBuried()) {
            this.body.y = 28.5F;
            this.foliage.y = -8.0F;
            this.foliage.zRot = Mth.sin(ageInTicks * 0.08F) * 0.06F;
            this.leftArm.visible = false;
            this.rightArm.visible = false;
            this.leftLeg.visible = false;
            this.rightLeg.visible = false;
        } else {
            this.leftArm.visible = true;
            this.rightArm.visible = true;
            this.leftLeg.visible = true;
            this.rightLeg.visible = true;
            this.body.y = 20.0F;

            this.body.yRot = netHeadYaw * ((float)Math.PI / 180F);

            if (entity.isScreaming() || entity.getDeltaMovement().horizontalDistanceSqr() > 0.005D) {
                float flailSpeed = 0.85F;
                this.leftArm.zRot = 2.4F + Mth.sin(ageInTicks * flailSpeed) * 0.4F;
                this.rightArm.zRot = -2.4F + Mth.cos(ageInTicks * flailSpeed) * 0.4F;

                this.body.xRot = (Mth.sin(ageInTicks * 1.2F) * 0.12F) + (headPitch * ((float)Math.PI / 180F) * 0.5F);
                this.body.zRot = Mth.cos(ageInTicks * 0.9F) * 0.08F;

                this.leftLeg.xRot = Mth.cos(limbSwing * 1.6F) * 1.3F * limbSwingAmount;
                this.rightLeg.xRot = Mth.cos(limbSwing * 1.6F + (float)Math.PI) * 1.3F * limbSwingAmount;
            } else {
                this.leftArm.zRot = 0.3F + Mth.sin(ageInTicks * 0.1F) * 0.05F;
                this.rightArm.zRot = -0.3F - Mth.sin(ageInTicks * 0.1F) * 0.05F;
                this.leftLeg.xRot = 0.0F;
                this.rightLeg.xRot = 0.0F;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
