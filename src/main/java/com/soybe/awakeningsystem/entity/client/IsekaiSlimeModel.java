package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.IsekaiSlimeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo personalizado para el Slime de Fantasía Isekai.
 * Presenta una silueta fluida redondeada en forma de lágrima/gota con punta curvada y ojos anime expresivos.
 */
public class IsekaiSlimeModel extends HierarchicalModel<IsekaiSlimeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "isekai_slime"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart base;
    private final ModelPart belly;
    private final ModelPart dome;
    private final ModelPart crown;
    private final ModelPart tip;
    private final ModelPart rightEye;
    private final ModelPart leftEye;
    private final ModelPart mouth;

    public IsekaiSlimeModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.base = this.body.getChild("base");
        this.belly = this.body.getChild("belly");
        this.dome = this.body.getChild("dome");
        this.crown = this.body.getChild("crown");
        this.tip = this.body.getChild("tip");
        this.rightEye = this.body.getChild("right_eye");
        this.leftEye = this.body.getChild("left_eye");
        this.mouth = this.body.getChild("mouth");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        body.addOrReplaceChild("belly", CubeListBuilder.create()
                .texOffs(0, 12)
                .addBox(-6.0F, -7.0F, -6.0F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        body.addOrReplaceChild("dome", CubeListBuilder.create()
                .texOffs(0, 29)
                .addBox(-4.5F, -10.0F, -4.5F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        body.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(0, 41)
                .addBox(-3.0F, -12.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition tipPart = body.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(24, 41)
                .addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(36, 41)
                .addBox(-1.0F, -3.5F, 0.2F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        body.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(48, 0)
                .addBox(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, -5.0F, -6.0F));

        body.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(54, 0)
                .addBox(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, -5.0F, -6.0F));

        body.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(48, 5)
                .addBox(-0.5F, -0.5F, -0.2F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.2F, -6.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(IsekaiSlimeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float partialTick = ageInTicks - (float) entity.tickCount;
        float squishFactor = Mth.lerp(partialTick, entity.prevSquish, entity.squish);

        float breathe = 0.0F;
        if (Math.abs(squishFactor) < 0.25F) {
            breathe = Mth.sin(ageInTicks * 0.12F) * 0.035F;
        }

        this.body.y = 24.0F;

        if (squishFactor < 0.0F) {
            float s = -squishFactor;

            this.body.yScale = Math.max(0.32F, 1.0F - s * 0.42F) + breathe;
            this.body.xScale = 1.0F + s * 0.38F - breathe * 0.5F;
            this.body.zScale = 1.0F + s * 0.38F - breathe * 0.5F;

            this.tip.y = -12.0F + s * 5.2F;
            this.tip.yScale = Math.max(0.20F, 1.0F - s * 0.55F);
            this.tip.xScale = 1.0F + s * 0.35F;
            this.tip.zScale = 1.0F + s * 0.35F;

            this.crown.y = s * 3.6F;
            this.crown.yScale = Math.max(0.30F, 1.0F - s * 0.45F);
            this.crown.xScale = 1.0F + s * 0.28F;
            this.crown.zScale = 1.0F + s * 0.28F;

            this.dome.y = s * 2.2F;
            this.dome.yScale = Math.max(0.40F, 1.0F - s * 0.38F);
            this.dome.xScale = 1.0F + s * 0.22F;
            this.dome.zScale = 1.0F + s * 0.22F;

            this.belly.y = s * 0.9F;
            this.belly.yScale = Math.max(0.48F, 1.0F - s * 0.32F);
            this.belly.xScale = 1.0F + s * 0.32F;
            this.belly.zScale = 1.0F + s * 0.32F;

            this.rightEye.y = -5.0F + s * 1.8F;
            this.leftEye.y = -5.0F + s * 1.8F;
            this.mouth.y = -3.2F + s * 1.3F;
            this.rightEye.yScale = Math.max(0.38F, 1.0F - s * 0.35F);
            this.leftEye.yScale = Math.max(0.38F, 1.0F - s * 0.35F);
            this.rightEye.xScale = 1.0F + s * 0.25F;
            this.leftEye.xScale = 1.0F + s * 0.25F;

        } else {
            float t = squishFactor;

            this.body.yScale = 1.0F + t * 0.52F + breathe;
            this.body.xScale = Math.max(0.60F, 1.0F - t * 0.28F) - breathe * 0.5F;
            this.body.zScale = Math.max(0.60F, 1.0F - t * 0.28F) - breathe * 0.5F;

            this.tip.y = -12.0F - t * 3.2F;
            this.tip.yScale = 1.0F + t * 0.35F;
            this.tip.xScale = Math.max(0.65F, 1.0F - t * 0.22F);
            this.tip.zScale = Math.max(0.65F, 1.0F - t * 0.22F);
            this.tip.xRot = (headPitch * ((float) Math.PI / 180.0F) * 0.3F) - (limbSwingAmount * 0.25F) - t * 0.38F;

            this.crown.y = -t * 1.8F;
            this.dome.y = -t * 0.9F;

            this.rightEye.y = -5.0F - t * 0.6F;
            this.leftEye.y = -5.0F - t * 0.6F;
        }

        float tipWobble = Mth.sin(ageInTicks * 0.18F + limbSwing * 0.8F) * 0.16F;
        this.tip.zRot += tipWobble;

        float eyeLook = netHeadYaw * ((float) Math.PI / 180.0F) * 0.2F;
        this.rightEye.yRot = eyeLook;
        this.leftEye.yRot = eyeLook;
    }
}
