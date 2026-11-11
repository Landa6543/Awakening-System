package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssalOrcEntity;
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
 * Blood Orc: an upright, heavily muscled brute. Head on top of thick traps, broad chest over a narrower
 * waist, long arms with giant fists. No two pieces share a face plane, so nothing flickers.
 */
public class AbyssalOrcModel extends HierarchicalModel<AbyssalOrcEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyssal_orc"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AbyssalOrcModel(ModelPart root) {
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

        // head: skull, jutting jaw with two rising tusks, pointed ears, a short mohawk
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.5F, 8.0F, 8.0F, 8.0F)
                .texOffs(32, 0).addBox(-4.5F, -3.2F, -5.3F, 9.0F, 3.0F, 6.0F)
                .texOffs(62, 0).addBox(-3.5F, -5.5F, -5.8F, 1.0F, 3.0F, 1.0F)
                .texOffs(62, 0).mirror().addBox(2.5F, -5.5F, -5.8F, 1.0F, 3.0F, 1.0F)
                .texOffs(66, 0).mirror(false).addBox(-5.8F, -6.0F, -1.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(66, 0).mirror().addBox(3.8F, -6.0F, -1.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(72, 0).mirror(false).addBox(-1.5F, -11.0F, -4.0F, 3.0F, 3.5F, 8.0F),
                PartPose.offset(0.0F, -15.0F, -0.5F));

        // body: waist, belt and cloth flap, broad chest, thick traps up to the neck
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5.5F, -9.0F, -3.5F, 11.0F, 9.0F, 7.0F)
                .texOffs(36, 16).addBox(-6.0F, -2.5F, -4.0F, 12.0F, 2.0F, 8.0F)
                .texOffs(94, 0).addBox(-3.5F, -0.5F, -4.7F, 7.0F, 7.0F, 1.0F)
                .texOffs(0, 32).addBox(-9.0F, -20.0F, -5.0F, 18.0F, 12.0F, 9.0F)
                .texOffs(76, 16).addBox(-6.5F, -22.5F, -3.0F, 13.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // arms: round shoulder, upper arm, fur cuff, huge forearm and fist
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(54, 32).addBox(-4.5F, -2.5F, -4.2F, 8.0F, 7.0F, 8.0F)
                .texOffs(86, 32).addBox(-3.5F, 4.0F, -3.0F, 6.0F, 7.0F, 6.0F)
                .texOffs(0, 53).addBox(-4.0F, 10.5F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.3F))
                .texOffs(28, 53).addBox(-4.0F, 11.5F, -3.5F, 7.0F, 8.0F, 7.0F)
                .texOffs(56, 53).addBox(-4.5F, 19.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                PartPose.offset(-10.5F, -12.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(54, 32).mirror().addBox(-3.5F, -2.5F, -4.2F, 8.0F, 7.0F, 8.0F)
                .texOffs(86, 32).mirror().addBox(-2.5F, 4.0F, -3.0F, 6.0F, 7.0F, 6.0F)
                .texOffs(0, 53).mirror().addBox(-3.0F, 10.5F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.3F))
                .texOffs(28, 53).mirror().addBox(-3.0F, 11.5F, -3.5F, 7.0F, 8.0F, 7.0F)
                .texOffs(56, 53).mirror().addBox(-3.5F, 19.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                PartPose.offset(10.5F, -12.0F, 0.0F));

        // legs: torn trousers, fur at the knee, bandaged shins, bare feet
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(88, 53).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F)
                .texOffs(0, 70).addBox(-4.0F, 8.5F, -4.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(32, 70).addBox(-3.0F, 10.2F, -3.0F, 6.0F, 6.0F, 6.0F)
                .texOffs(56, 70).addBox(-3.5F, 16.0F, -4.5F, 7.0F, 2.0F, 8.0F),
                PartPose.offset(-4.0F, 6.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(88, 53).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F)
                .texOffs(0, 70).mirror().addBox(-4.0F, 8.5F, -4.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(32, 70).mirror().addBox(-3.0F, 10.2F, -3.0F, 6.0F, 6.0F, 6.0F)
                .texOffs(56, 70).mirror().addBox(-3.5F, 16.0F, -4.5F, 7.0F, 2.0F, 8.0F),
                PartPose.offset(4.0F, 6.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    private static float ease(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    @Override
    public void setupAnim(AbyssalOrcEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float frac = ageInTicks - Mth.floor(ageInTicks);

        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;

        // heavy stride: legs and arms swing opposite, the torso twists and the whole upper body bobs
        float walk = limbSwing * 0.45F;
        float amt = Math.min(limbSwingAmount, 1.0F);
        this.rightLeg.xRot = Mth.cos(walk) * 0.85F * amt;
        this.leftLeg.xRot = Mth.cos(walk + Mth.PI) * 0.85F * amt;
        this.rightArm.xRot = Mth.cos(walk + Mth.PI) * 0.7F * amt;
        this.leftArm.xRot = Mth.cos(walk) * 0.7F * amt;
        this.body.yRot = Mth.cos(walk) * 0.12F * amt;
        this.body.zRot = Mth.sin(walk) * 0.04F * amt;
        float bob = Math.abs(Mth.sin(walk)) * 1.2F * amt;
        this.body.y += bob;
        this.head.y += bob;
        this.rightArm.y += bob;
        this.leftArm.y += bob;
        this.head.yRot += this.body.yRot * 0.5F;

        // arms hang a little out from the huge lats; they breathe in and out when standing
        float breath = Mth.sin(ageInTicks * 0.07F);
        this.rightArm.zRot = 0.12F + breath * 0.03F * (1.0F - amt);
        this.leftArm.zRot = -0.12F - breath * 0.03F * (1.0F - amt);
        if (amt < 0.15F) {
            this.body.y += breath * 0.35F;
            this.head.y += breath * 0.35F;
            this.rightArm.y += breath * 0.35F;
            this.leftArm.y += breath * 0.35F;
            this.head.xRot += breath * 0.03F;
        }

        if (entity.bloodFrenzyTicks > 0) {                       // frenzy: chest forward, fists ready
            this.body.xRot += 0.12F;
            this.head.xRot -= 0.1F;
            this.rightArm.xRot -= 0.35F;
            this.leftArm.xRot -= 0.35F;
            this.rightArm.zRot += 0.15F;
            this.leftArm.zRot -= 0.15F;
        }

        // normal blow: winds the right fist back over the shoulder, then drives it down and across
        if (this.attackTime > 0.0F) {
            float p = this.attackTime;
            if (p < 0.35F) {
                float w = ease(p / 0.35F);
                this.rightArm.xRot = -2.3F * w;
                this.rightArm.zRot = 0.12F + 0.25F * w;
                this.body.yRot += 0.35F * w;
                this.body.xRot -= 0.1F * w;
                this.leftArm.xRot = 0.3F * w;
            } else {
                float s = ease((p - 0.35F) / 0.65F);
                this.rightArm.xRot = Mth.lerp(s, -2.3F, -0.35F);
                this.rightArm.yRot = Mth.lerp(s, 0.0F, 0.45F);
                this.rightArm.zRot = Mth.lerp(s, 0.37F, 0.05F);
                this.body.yRot += Mth.lerp(s, 0.35F, -0.4F);
                this.body.xRot += Mth.lerp(s, -0.1F, 0.18F);
                this.leftArm.xRot = Mth.lerp(s, 0.3F, -0.25F);
            }
        }

        // ground smash: both fists rise overhead, then come down together while it drops into a crouch
        if (entity.smashAnimationTimeout > 0) {
            float p = (30 - entity.smashAnimationTimeout + frac) / 30.0F;
            float up = ease(p / 0.45F), down = ease((p - 0.45F) / 0.25F), back = ease((p - 0.8F) / 0.2F);
            float armX = p < 0.45F ? -2.8F * up : Mth.lerp(down, -2.8F, -0.4F);
            float bend = p < 0.45F ? -0.25F * up : Mth.lerp(down, -0.25F, 0.45F);
            float crouch = p < 0.45F ? 0.0F : 2.5F * down;
            if (p > 0.8F) {
                armX = Mth.lerp(back, armX, 0.0F);
                bend = Mth.lerp(back, bend, 0.0F);
                crouch = Mth.lerp(back, crouch, 0.0F);
            }
            this.rightArm.xRot = armX;
            this.leftArm.xRot = armX;
            this.rightArm.zRot = 0.2F;
            this.leftArm.zRot = -0.2F;
            this.body.xRot = bend;
            this.body.y += crouch;
            this.head.y += crouch;
            this.rightArm.y += crouch;
            this.leftArm.y += crouch;
            this.rightLeg.xRot = -0.35F * crouch / 2.5F;
            this.leftLeg.xRot = -0.35F * crouch / 2.5F;
        }

        // roar: head thrown back, chest out, arms flung wide and shaking
        if (entity.roarAnimationTimeout > 0) {
            float p = (30 - entity.roarAnimationTimeout + frac) / 30.0F;
            float in = ease(p / 0.2F) * (1.0F - ease((p - 0.85F) / 0.15F));
            float shake = Mth.sin(ageInTicks * 2.2F) * 0.06F * in;
            this.head.xRot = -0.7F * in + shake;
            this.body.xRot = -0.18F * in;
            this.rightArm.zRot = 0.12F + 0.9F * in + shake;
            this.leftArm.zRot = -0.12F - 0.9F * in - shake;
            this.rightArm.xRot = -0.5F * in;
            this.leftArm.xRot = -0.5F * in;
        }
    }
}
