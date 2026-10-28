package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JadeGuardianBossEntity;
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

public class JadeGuardianModel extends HierarchicalModel<JadeGuardianBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_guardian"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart skirtFront;
    private final ModelPart skirtLeft;
    private final ModelPart skirtRight;
    private final ModelPart skirtBack;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public JadeGuardianModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.skirtFront = this.body.getChild("skirt_front");
        this.skirtLeft = this.body.getChild("skirt_left");
        this.skirtRight = this.body.getChild("skirt_right");
        this.skirtBack = this.body.getChild("skirt_back");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. HEAD & IMPERIAL HELMET
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-1.5F, -12.0F, -3.5F, 3.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(54, 0).addBox(-0.5F, -14.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 0).addBox(-3.5F, -2.5F, -4.8F, 7.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 13).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 6).addBox(3.8F, -7.0F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(70, 6).addBox(-4.8F, -7.0F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        // 2. TORSO, LAMELLAR CUIRASS & FAULD
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-7.0F, -10.0F, -4.5F, 14.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(46, 21).addBox(-4.0F, -8.0F, -5.5F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 21).addBox(-6.0F, -2.0F, -4.0F, 12.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-7.5F, 4.0F, -4.8F, 15.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(48, 38).addBox(-2.5F, 3.5F, -5.6F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 36).addBox(3.5F, -14.0F, 4.8F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 36).addBox(-4.5F, -14.0F, 4.8F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("skirt_front", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-4.0F, 0.0F, -0.5F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, -4.1F));

        body.addOrReplaceChild("skirt_back", CubeListBuilder.create()
                .texOffs(54, 50).addBox(-5.0F, 0.0F, -0.5F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, 4.1F));

        body.addOrReplaceChild("skirt_left", CubeListBuilder.create()
                .texOffs(18, 50).addBox(-0.5F, 0.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(6.3F, 8.0F, 0.0F));

        body.addOrReplaceChild("skirt_right", CubeListBuilder.create()
                .texOffs(36, 50).addBox(-0.5F, 0.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-6.3F, 8.0F, 0.0F));

        // 3. RIGHT ARM & BEAST PAULDRON
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 67).addBox(-6.0F, -3.5F, -4.5F, 7.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(32, 67).addBox(-6.5F, -5.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 67).addBox(-4.5F, 1.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(60, 67).addBox(-5.0F, 6.5F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 82).addBox(-5.0F, 9.5F, -3.5F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(26, 82).addBox(-4.5F, 16.5F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.5F, -8.0F, 0.0F));

        // 4. LEFT ARM & BEAST PAULDRON
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(48, 82).addBox(-1.0F, -3.5F, -4.5F, 7.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(80, 82).addBox(4.5F, -5.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(88, 67).addBox(-0.5F, 1.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(88, 78).addBox(-1.0F, 6.5F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 97).addBox(-1.0F, 9.5F, -3.5F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(26, 97).addBox(-0.5F, 16.5F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.5F, -8.0F, 0.0F));

        // 5. RIGHT LEG & GREAVE
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(48, 97).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(72, 97).addBox(-2.5F, 6.0F, -3.8F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(86, 97).addBox(-3.0F, 7.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 112).addBox(-3.5F, 14.0F, -4.5F, 7.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 8.0F, 0.0F));

        // 6. LEFT LEG & GREAVE
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(30, 112).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(54, 112).addBox(-2.5F, 6.0F, -3.8F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 112).addBox(-3.0F, 7.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(92, 112).addBox(-3.5F, 14.0F, -4.5F, 7.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 8.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(JadeGuardianBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Head tracking
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        // Heavy imperial marching stride
        float walkProgress = limbSwing * 0.45F;
        float legSwing = Mth.cos(walkProgress) * 0.85F * limbSwingAmount;
        this.rightLeg.xRot = legSwing;
        this.leftLeg.xRot = -legSwing;
        this.rightArm.xRot = -legSwing * 0.75F;
        this.leftArm.xRot = legSwing * 0.75F;

        // Heavy breathing and rhythmic weight shift
        float breath = Mth.sin(ageInTicks * 0.06F) * 0.02F;
        this.body.yRot = breath;
        this.body.xRot = 0.025F + Mth.sin(ageInTicks * 0.06F) * 0.015F;

        // Armored tassets response to leg strides
        float maxFwd = Math.max(0.0F, Math.max(this.rightLeg.xRot, this.leftLeg.xRot));
        float maxBwd = Math.min(0.0F, Math.min(this.rightLeg.xRot, this.leftLeg.xRot));
        this.skirtFront.xRot = maxFwd * 0.50F;
        this.skirtBack.xRot = maxBwd * 0.50F;
        this.skirtLeft.zRot = 0.08F + (this.leftLeg.xRot > 0.1F ? 0.12F : 0.0F);
        this.skirtRight.zRot = -0.08F - (this.rightLeg.xRot > 0.1F ? 0.12F : 0.0F);

        // Dynamic Boss Attack Poses
        byte attack = entity.getAttackState();
        int ticks = entity.getAttackAnimationTicks();

        if (attack == 1) {
            // Jade Palm Strike: Right arm pulls back then crushes forward with palm thrust
            float p = Mth.clamp(1.0F - ((float) ticks / 20.0F), 0.0F, 1.0F);
            float strike = Mth.sin(p * (float) Math.PI);
            this.rightArm.xRot = -1.65F * strike;
            this.rightArm.yRot = -0.35F * strike;
            this.rightArm.zRot = 0.20F * strike;
            this.leftArm.xRot = 0.40F * strike;
            this.body.yRot = -0.30F * strike;
        } else if (attack == 2) {
            // Earth Tremor (Ground Slam): Raises both gauntlets overhead, arches back, slams down
            float p = Mth.clamp(1.0F - ((float) ticks / 28.0F), 0.0F, 1.0F);
            if (p < 0.6F) {
                float w = p / 0.6F;
                this.rightArm.xRot = -2.35F * w;
                this.leftArm.xRot = -2.35F * w;
                this.body.xRot = -0.22F * w;
            } else {
                float s = (p - 0.6F) / 0.4F;
                this.rightArm.xRot = -2.35F + 1.85F * s;
                this.leftArm.xRot = -2.35F + 1.85F * s;
                this.body.xRot = -0.22F + 0.45F * s;
            }
        } else if (attack == 3) {
            // Qi Barrier: Crosses both gauntlets defensively in front of the chest mirror
            float p = Mth.clamp(1.0F - ((float) ticks / 30.0F), 0.0F, 1.0F);
            float guard = Mth.sin(p * (float) Math.PI);
            this.rightArm.xRot = -0.95F * guard;
            this.rightArm.yRot = 0.35F * guard;
            this.rightArm.zRot = 0.50F * guard;
            this.leftArm.xRot = -0.95F * guard;
            this.leftArm.yRot = -0.35F * guard;
            this.leftArm.zRot = -0.50F * guard;
            this.head.xRot = 0.18F * guard;
        }
    }
}
