package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalNecromancerEntity;
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

/**
 * The Abyssal Necromancer, drawn from scratch: a gaunt, hunched sorcerer. A deep hood around a skull with a
 * jaw of its own and soul-lit eyes; a mantle of bones and jutting ribs over the shoulders; a robe that falls to
 * the ground in four ragged panels that sway; long bell sleeves with skeletal hands; a staff of vertebrae
 * crowned with a skull and a floating soul flame; a grimoire chained at the hip; and two souls circling him.
 */
public class AbyssalNecromancerModel extends HierarchicalModel<AbyssalNecromancerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_necromancer"), "main");

    private final ModelPart root, body, torso, head, jaw, rArm, lArm, rFore, lFore, flame, souls, soul1, soul2;
    private final ModelPart robeF, robeB, robeR, robeL, rLeg, lLeg;

    public AbyssalNecromancerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.torso = body.getChild("torso");
        this.head = torso.getChild("head");
        this.jaw = head.getChild("jaw");
        this.rArm = torso.getChild("right_arm");
        this.lArm = torso.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.flame = rFore.getChild("right_hand").getChild("staff").getChild("flame");
        this.souls = body.getChild("souls");
        this.soul1 = souls.getChild("soul_0");
        this.soul2 = souls.getChild("soul_1");
        this.robeF = body.getChild("robe_front");
        this.robeB = body.getChild("robe_back");
        this.robeR = body.getChild("robe_right");
        this.robeL = body.getChild("robe_left");
        this.rLeg = root.getChild("right_leg");
        this.lLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -1.0F, -2.0F, 7.0F, 3.0F, 4.0F)                        // waist sash
                        .texOffs(66, 28).addBox(3.4F, -0.5F, -1.5F, 2.0F, 4.0F, 3.0F),                    // grimoire
                PartPose.offset(0.0F, 10.0F, 0.0F));
        // the robe in four ragged panels that hang from the waist to the ground
        body.addOrReplaceChild("robe_front", CubeListBuilder.create().texOffs(0, 41).addBox(-3.5F, 0.0F, -0.5F, 7.0F, 13.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, -2.2F, -0.06F, 0.0F, 0.0F));
        body.addOrReplaceChild("robe_back", CubeListBuilder.create().texOffs(16, 41).addBox(-4.0F, 0.0F, -0.5F, 8.0F, 13.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, 2.2F, 0.08F, 0.0F, 0.0F));
        body.addOrReplaceChild("robe_right", CubeListBuilder.create().texOffs(34, 41).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-3.8F, 1.5F, 0.0F, 0.0F, 0.0F, 0.08F));
        body.addOrReplaceChild("robe_left", CubeListBuilder.create().texOffs(34, 41).mirror().addBox(-0.5F, 0.0F, -2.0F, 1.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(3.8F, 1.5F, 0.0F, 0.0F, 0.0F, -0.08F));

        // ── chest, the bone mantle and its ribs ──
        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create()
                        .texOffs(0, 11).addBox(-4.0F, -9.0F, -2.5F, 8.0F, 9.0F, 5.0F)
                        .texOffs(22, 0).addBox(-5.5F, -10.0F, -3.5F, 11.0F, 4.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        for (int i = 0; i < 4; i++) {
            float x = -3.75F + i * 2.5F;
            torso.addOrReplaceChild("rib_" + i, CubeListBuilder.create().texOffs(76, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                    PartPose.offsetAndRotation(x, -9.5F, 3.0F, -0.5F, 0.0F, x * -0.06F));
        }

        // ── the hooded skull ──
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(58, 11).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F)
                        .texOffs(26, 11).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
                        .texOffs(80, 0).addBox(-1.5F, -8.0F, 2.5F, 3.0F, 3.0F, 3.0F),                    // the hood's peak
                PartPose.offsetAndRotation(0.0F, -9.5F, -0.8F, -0.18F, 0.0F, 0.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(58, 0).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, -1.0F, 0.2F));

        // ── arms in bell sleeves, skeletal hands ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition arm = torso.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 28).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F),
                    PartPose.offsetAndRotation(side * 5.6F, -8.0F, 0.0F, -0.1F, 0.0F, side * 0.12F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                            .texOffs(16, 28).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                    PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.25F, 0.0F, 0.0F));
            CubeListBuilder hand = CubeListBuilder.create().mirror(m).texOffs(36, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F);
            for (int f = 0; f < 3; f++) hand.texOffs(44, 28).addBox(-1.2F + f * 0.9F, 1.6F, -1.0F, 1.0F, 3.0F, 1.0F);
            PartDefinition handPart = fore.addOrReplaceChild(s + "_hand", hand, PartPose.offset(0.0F, 6.5F, 0.0F));
            if (side < 0) {
                // the staff: a spine of vertebrae, a skull at the top and a soul flame floating over it
                PartDefinition staff = handPart.addOrReplaceChild("staff", CubeListBuilder.create()
                                .texOffs(92, 0).addBox(-0.5F, -18.0F, -0.5F, 1.0F, 28.0F, 1.0F)
                                .texOffs(48, 28).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                                .texOffs(48, 28).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                                .texOffs(48, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                                .texOffs(96, 0).addBox(-2.0F, -22.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                        PartPose.offsetAndRotation(-0.5F, 1.5F, -0.5F, 0.0F, 0.0F, -0.3F));      // leaning out, clear of the hood
                staff.addOrReplaceChild("flame", CubeListBuilder.create().texOffs(112, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                        PartPose.offset(0.0F, -25.0F, 0.0F));
            }
        }

        // ── two souls circling him ──
        PartDefinition souls = body.addOrReplaceChild("souls", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));
        souls.addOrReplaceChild("soul_0", CubeListBuilder.create().texOffs(112, 6).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(8.0F, 0.0F, 0.0F));
        souls.addOrReplaceChild("soul_1", CubeListBuilder.create().texOffs(112, 6).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-8.0F, -3.0F, 0.0F));

        // ── thin legs under the robe, bare bone feet ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(side > 0)
                            .texOffs(44, 41).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                            .texOffs(56, 28).addBox(-1.0F, 11.0F, -2.5F, 2.0F, 1.0F, 3.0F),
                    PartPose.offset(side * 1.8F, 12.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AbyssalNecromancerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.6F;
        float drift = Mth.sin(ageInTicks * 0.07F);
        boolean hostile = entity != null && entity.isAggressive();

        // he glides more than he walks: a slow rise and fall, the robe swaying, the legs barely seen
        body.y += drift * 0.4F;
        torso.xRot += drift * 0.03F;
        rLeg.xRot = Mth.cos(w) * 0.5F * amt;
        lLeg.xRot = -Mth.cos(w) * 0.5F * amt;
        robeF.xRot += -Math.abs(Mth.cos(w)) * 0.25F * amt - 0.12F * amt + Mth.sin(ageInTicks * 0.09F) * 0.04F;
        robeB.xRot += 0.2F * amt + Mth.sin(ageInTicks * 0.09F + 1.0F) * 0.05F;
        robeR.zRot += Mth.sin(ageInTicks * 0.08F) * 0.04F + Math.max(0.0F, Mth.cos(w)) * 0.12F * amt;
        robeL.zRot -= Mth.sin(ageInTicks * 0.08F + 2.0F) * 0.04F + Math.max(0.0F, -Mth.cos(w)) * 0.12F * amt;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD;
        jaw.xRot = 0.05F + Math.max(0.0F, Mth.sin(ageInTicks * 0.13F)) * 0.1F;

        // the staff arm is held out; the free hand hangs, fingers curling; when hostile both hands rise to cast
        rArm.xRot = -0.35F + Mth.sin(ageInTicks * 0.06F) * 0.04F;
        rFore.xRot = -0.55F;
        lArm.xRot = Mth.cos(w) * 0.25F * amt;
        lArm.zRot += Mth.sin(ageInTicks * 0.05F) * 0.04F;
        if (hostile) {
            lArm.xRot = -1.2F + Mth.sin(ageInTicks * 0.3F) * 0.08F;
            lArm.zRot = 0.35F;
            lFore.xRot = -0.4F;
            rArm.xRot = -0.75F;
            jaw.xRot = 0.25F + Mth.sin(ageInTicks * 0.6F) * 0.12F;              // chanting
        }

        // the soul flame flickers; the souls circle at different heights
        float f = 1.0F + Mth.sin(ageInTicks * 0.4F) * 0.12F;
        flame.xScale = f;
        flame.yScale = 1.0F + Mth.sin(ageInTicks * 0.53F) * 0.18F;
        flame.zScale = f;
        flame.yRot = ageInTicks * 0.1F;
        souls.yRot = ageInTicks * (hostile ? 0.12F : 0.05F);
        soul1.y += Mth.sin(ageInTicks * 0.1F) * 1.5F;
        soul2.y += Mth.sin(ageInTicks * 0.1F + Mth.PI) * 1.5F;

        if (this.attackTime > 0.0F) {
            // the staff is thrust forward, the skull leading
            float s = Mth.sin(this.attackTime * Mth.PI);
            rArm.xRot = -0.35F - 1.2F * s;
            rFore.xRot = -0.55F + 0.4F * s;
            torso.xRot += 0.2F * s;
            jaw.xRot = 0.45F * s;
        }
    }
}
