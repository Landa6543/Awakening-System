package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WhiteThunderTigerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D para el Tigre Blanco del Trueno (White Thunder Tiger / Byakko).
 * Bestia felina imponente con anatomía realista:
 * - Cabeza felina con mejillas peludas prominentes (ruffs laterales), hocico ancho,
 *   colmillos afilados visibles, mandíbula articulada y orejas redondeadas.
 * - Cuello robusto, torso musculoso, patas poderosas y cola en 3 segmentos articulados.
 * - Animaciones fluidas: caminar, correr, sentarse y rugido de trueno al cielo.
 */
public class WhiteThunderTigerModel extends HierarchicalModel<WhiteThunderTigerEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "white_thunder_tiger"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart mane;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackLeftShin;
    private final ModelPart legBackRight;
    private final ModelPart legBackRightShin;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;

    public WhiteThunderTigerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.mane = this.body.getChild("mane");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");

        this.legFrontLeft = this.body.getChild("leg_front_left");
        this.legFrontRight = this.body.getChild("leg_front_right");
        this.legBackLeft = this.body.getChild("leg_back_left");
        this.legBackLeftShin = this.legBackLeft.getChild("leg_back_left_shin");
        this.legBackRight = this.body.getChild("leg_back_right");
        this.legBackRightShin = this.legBackRight.getChild("leg_back_right_shin");

        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.5F, -4.5F, -8.0F, 9.0F, 9.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        body.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(50, 0)
                .addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 7.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -0.5F, -4.5F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(85, 0)
                .addBox(-3.0F, -4.0F, -6.0F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, -7.5F, -0.15F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 26)
                .addBox(-3.5F, -3.5F, -4.0F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(26, 26)
                .addBox(-2.5F, -0.5F, -7.5F, 5.0F, 3.5F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(60, 26)
                .addBox(-4.8F, -0.5F, -2.5F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(72, 26)
                .addBox(2.8F, -0.5F, -2.5F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(84, 26)
                .addBox(-3.5F, -5.2F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(90, 26)
                .addBox(1.5F, -5.2F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(96, 26)
                .addBox(-2.2F, 2.0F, -6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 26)
                .addBox(1.2F, 2.0F, -6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -6.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(44, 26)
                .addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.0F, -3.2F));

        body.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                .texOffs(0, 42)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(-0.05F)),
                PartPose.offset(-3.5F, 1.0F, -5.5F));

        body.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                .texOffs(16, 42)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(-0.05F)),
                PartPose.offset(3.5F, 1.0F, -5.5F));

        PartDefinition legBackLeft = body.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                .texOffs(32, 42)
                .addBox(-2.5F, 0.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.5F, 0.0F, 5.0F));
        legBackLeft.addOrReplaceChild("leg_back_left_shin", CubeListBuilder.create()
                .texOffs(54, 42)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.5F));

        PartDefinition legBackRight = body.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                .texOffs(70, 42)
                .addBox(-2.5F, 0.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.5F, 0.0F, 5.0F));
        legBackRight.addOrReplaceChild("leg_back_right_shin", CubeListBuilder.create()
                .texOffs(92, 42)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.5F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 62)
                .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.5F, 8.0F, -0.6F, 0.0F, 0.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(18, 62)
                .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.5F, 0.25F, 0.0F, 0.0F));

        tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(36, 62)
                .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.5F, 0.25F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(WhiteThunderTigerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean isSitting = entity.isInSittingPose();
        boolean isRoaring = entity.isRoaring();

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.65F;
        this.neck.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.35F;

        if (isRoaring) {
            this.neck.xRot = -0.5F;
            this.head.xRot = -0.45F;
            this.jaw.xRot = 0.6F;
            this.tail1.xRot = -0.2F;
            this.tail1.yRot = Mth.sin(ageInTicks * 0.4F) * 0.25F;
        } else {
            this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.65F;
        }

        if (isSitting) {
            this.body.y = 15.0F;
            this.body.xRot = -0.45F;

            this.legBackLeft.xRot = -0.85F;
            this.legBackRight.xRot = -0.85F;
            this.legBackLeft.y = 3.0F;
            this.legBackRight.y = 3.0F;
            this.legBackLeft.z = 4.0F;
            this.legBackRight.z = 4.0F;

            this.legBackLeftShin.xRot = 1.15F;
            this.legBackRightShin.xRot = 1.15F;
            this.legBackLeftShin.y = 5.0F;
            this.legBackRightShin.y = 5.0F;

            this.legFrontLeft.xRot = 0.45F;
            this.legFrontRight.xRot = 0.45F;
            this.legFrontLeft.y = 0.2F;
            this.legFrontRight.y = 0.2F;
            this.legFrontLeft.z = -5.0F;
            this.legFrontRight.z = -5.0F;

            this.neck.xRot = 0.35F;
            this.tail1.xRot = 0.85F;
            this.tail1.yRot = 0.35F + Mth.sin(ageInTicks * 0.05F) * 0.08F;
            this.tail2.xRot = 0.15F;
            this.tail3.xRot = 0.1F;
        } else {
            this.legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
            this.legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.1F * limbSwingAmount;
            this.legBackLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
            this.legBackRight.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F * limbSwingAmount;
            this.legBackLeftShin.xRot = Math.max(0.0F, Mth.sin(limbSwing * 0.6662F) * 0.35F * limbSwingAmount);
            this.legBackRightShin.xRot = Math.max(0.0F, Mth.sin(limbSwing * 0.6662F + (float) Math.PI) * 0.35F * limbSwingAmount);

            this.tail1.yRot = Mth.cos(limbSwing * 0.5F) * 0.35F * limbSwingAmount + Mth.sin(ageInTicks * 0.08F) * 0.06F;
            this.tail2.yRot = Mth.cos(limbSwing * 0.5F - 0.5F) * 0.35F * limbSwingAmount + Mth.sin(ageInTicks * 0.08F - 0.4F) * 0.08F;
            this.tail3.yRot = Mth.cos(limbSwing * 0.5F - 1.0F) * 0.35F * limbSwingAmount + Mth.sin(ageInTicks * 0.08F - 0.8F) * 0.1F;

            this.body.y = 12.0F + Mth.sin(ageInTicks * 0.06F) * 0.15F;
        }
    }
}
