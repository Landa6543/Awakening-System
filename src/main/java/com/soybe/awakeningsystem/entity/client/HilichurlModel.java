package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.HilichurlEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D Autentico y Canonico del Hilichurl de Genshin Impact (Genshin Nature Revived).
 * Incluye mascara de hueso tallada con relieves 3D, orejas puntiagudas inclinadas,
 * garrote de combate tribal, garrote ardiente berserker, ballesta de cazador y escudo de madera tallado.
 */
public class HilichurlModel<T extends HilichurlEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "hilichurl"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    private final ModelPart fighterClub;
    private final ModelPart berserkerClub;
    private final ModelPart crossbow;
    private final ModelPart shield;

    public HilichurlModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");

        this.fighterClub = this.rightArm.getChild("fighter_club");
        this.berserkerClub = this.rightArm.getChild("berserker_club");
        this.crossbow = this.rightArm.getChild("crossbow");
        this.shield = this.leftArm.getChild("shield");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(49, 0).addBox(-2.0F, -8.0F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(49, 0).addBox(1.0F, -8.0F, -5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 6).addBox(2.0F, -7.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 6).addBox(-3.0F, -7.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(54, 0).addBox(-2.0F, -5.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 12).addBox(2.0F, -4.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 12).mirror() .addBox(-3.0F, -4.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 7).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create()
                .texOffs(26, 0).mirror() .addBox(-5.5000F, -0.5000F, -1.5000F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(26, 0).addBox(-0.5000F, -0.5000F, -1.5000F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5000F, -8.5000F, -1.5000F, 0.6545F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition fighterClub = rightArm.addOrReplaceChild("fighter_club", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.0F, 9.5000F, -4.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(48, 51).addBox(-2.0F, 8.5000F, -9.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition berserkerClub = rightArm.addOrReplaceChild("berserker_club", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-1.0F, 9.5000F, -4.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(42, 53).addBox(-2.0F, 8.5000F, -12.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition crossbow = rightArm.addOrReplaceChild("crossbow", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition cb_r2 = crossbow.addOrReplaceChild("cb_r2", CubeListBuilder.create()
                .texOffs(52, 61).addBox(-1.9683F, 1.5000F, -7.2172F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 10.0F, -4.0F, 1.0665F, 0.4380F, 1.0676F));
        PartDefinition cb_r3 = crossbow.addOrReplaceChild("cb_r3", CubeListBuilder.create()
                .texOffs(52, 61).mirror() .addBox(-2.6405F, 1.5000F, -6.4239F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 10.0F, -4.0F, 1.2130F, -0.5615F, -0.5462F));
        PartDefinition cb_r4 = crossbow.addOrReplaceChild("cb_r4", CubeListBuilder.create()
                .texOffs(22, 56).mirror() .addBox(0.5000F, 1.5000F, -6.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 56).addBox(-4.5000F, 1.5000F, -6.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 53).addBox(-1.5000F, 1.5000F, -7.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 10.0F, -4.0F, 0.9207F, -0.0891F, 0.2959F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).mirror() .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition shield = leftArm.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(50, 52).addBox(2.0F, 4.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(58, 47).addBox(2.0F, 5.0F, 3.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 48).addBox(2.0F, 3.0F, 4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(52, 48).mirror() .addBox(2.0F, 3.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(58, 47).mirror() .addBox(2.0F, 5.0F, -5.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(45, 51).addBox(2.0F, 10.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9000F, 12.0F, 0.0F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 16).mirror() .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9000F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(HilichurlEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.head.xRot = headPitch * (Mth.PI / 180F);

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = -Mth.cos(limbSwing) * 1.0F * limbSwingAmount;

        HilichurlEntity.HilichurlRole role = entity.getRole();
        boolean isShooter = (role == HilichurlEntity.HilichurlRole.SHOOTER);
        boolean isGuard = (role == HilichurlEntity.HilichurlRole.GUARD);
        boolean isBerserker = (role == HilichurlEntity.HilichurlRole.BERSERKER);
        boolean isFighter = (role == HilichurlEntity.HilichurlRole.FIGHTER);

        this.fighterClub.visible = isFighter;
        this.berserkerClub.visible = isBerserker;
        this.crossbow.visible = isShooter;
        this.shield.visible = isGuard;

        if (isBerserker && entity.getTarget() != null) {
            this.rightArm.xRot = -2.2F + Mth.sin(ageInTicks * 0.35F) * 0.25F;
            this.rightArm.zRot = -0.2F;
            this.leftArm.xRot = 0.4F;
        } else if (isGuard && entity.isBlocking()) {
            this.leftArm.xRot = -1.2F;
            this.leftArm.yRot = 0.55F;
            this.leftArm.zRot = 0.45F;
            this.rightArm.xRot = -0.7F;
            this.rightArm.yRot = -0.2F;
        } else if (isShooter) {
            if (entity.getTarget() != null) {
                this.rightArm.xRot = -1.55F + headPitch * (Mth.PI / 180F);
                this.rightArm.yRot = -0.35F + netHeadYaw * (Mth.PI / 180F);
                this.leftArm.xRot = -1.45F;
                this.leftArm.yRot = 0.6F;
            } else {
                this.rightArm.xRot = -0.98F;
                this.rightArm.yRot = -0.25F;
                this.leftArm.xRot = -1.08F;
                this.leftArm.yRot = 0.59F;
            }
        }
    }
}
