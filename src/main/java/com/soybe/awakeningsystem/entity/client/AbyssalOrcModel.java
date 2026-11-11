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
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public AbyssalOrcModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // body (pivot at the hips): waist, belt and cloth flap, broad chest, thick traps.
        // Head and arms hang from it, so they follow every lean and twist of the torso.
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5.5F, -9.0F, -3.5F, 11.0F, 9.0F, 7.0F)
                .texOffs(36, 16).addBox(-6.0F, -2.5F, -4.0F, 12.0F, 2.0F, 8.0F)
                .texOffs(94, 0).addBox(-3.5F, -0.5F, -4.7F, 7.0F, 7.0F, 1.0F)
                .texOffs(0, 32).addBox(-9.0F, -20.0F, -5.0F, 18.0F, 12.0F, 9.0F)
                .texOffs(76, 16).addBox(-6.5F, -22.5F, -3.0F, 13.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // head: skull, jutting jaw with two rising tusks, pointed ears, a short mohawk
        body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.5F, 8.0F, 8.0F, 8.0F)
                .texOffs(32, 0).addBox(-4.5F, -3.2F, -5.3F, 9.0F, 3.0F, 6.0F)
                .texOffs(62, 0).addBox(-3.5F, -5.5F, -5.8F, 1.0F, 3.0F, 1.0F)
                .texOffs(62, 0).mirror().addBox(2.5F, -5.5F, -5.8F, 1.0F, 3.0F, 1.0F)
                .texOffs(66, 0).mirror(false).addBox(-5.8F, -6.0F, -1.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(66, 0).mirror().addBox(3.8F, -6.0F, -1.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(72, 0).mirror(false).addBox(-1.5F, -11.0F, -4.0F, 3.0F, 3.5F, 8.0F),
                PartPose.offset(0.0F, -21.0F, -0.5F));

        // arms: shoulder and upper arm; the forearm (fur cuff, forearm, fist) bends at the elbow
        for (int side = -1; side <= 1; side += 2) {
            String name = side < 0 ? "right" : "left";
            boolean m = side > 0;
            float s = side < 0 ? 0.0F : 1.0F;            // left-side boxes are shifted by one pixel to mirror round the pivot
            PartDefinition arm = body.addOrReplaceChild(name + "_arm", CubeListBuilder.create().mirror(m)
                    .texOffs(54, 32).addBox(-4.5F + s, -2.5F, -4.2F, 8.0F, 7.0F, 8.0F)
                    .texOffs(86, 32).addBox(-3.5F + s, 4.0F, -3.0F, 6.0F, 7.0F, 6.0F),
                    PartPose.offset(side * 10.5F, -18.0F, 0.0F));
            arm.addOrReplaceChild(name + "_forearm", CubeListBuilder.create().mirror(m)
                    .texOffs(0, 53).addBox(-4.0F + s, -0.5F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.3F))
                    .texOffs(28, 53).addBox(-4.0F + s, 0.5F, -3.5F, 7.0F, 8.0F, 7.0F)
                    .texOffs(56, 53).addBox(-4.5F + s, 8.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                    PartPose.offset(0.0F, 11.0F, 0.0F));
        }

        // legs: trousers with fur at the knee; the shin (wraps and bare foot) bends at the knee
        for (int side = -1; side <= 1; side += 2) {
            String name = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition leg = root.addOrReplaceChild(name + "_leg", CubeListBuilder.create().mirror(m)
                    .texOffs(88, 53).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F)
                    .texOffs(0, 70).addBox(-4.0F, 8.5F, -4.0F, 8.0F, 2.0F, 8.0F),
                    PartPose.offset(side * 4.0F, 6.0F, 0.0F));
            leg.addOrReplaceChild(name + "_shin", CubeListBuilder.create().mirror(m)
                    .texOffs(32, 70).addBox(-3.0F, 0.2F, -3.0F, 6.0F, 6.0F, 6.0F)
                    .texOffs(56, 70).addBox(-3.5F, 6.0F, -4.5F, 7.0F, 2.0F, 8.0F),
                    PartPose.offset(0.0F, 10.0F, 0.0F));
        }

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

    /** Fades a pose in over the first part of an action and out over the last part. */
    private static float envelope(float p, float in, float out) {
        return ease(p / in) * (1.0F - ease((p - (1.0F - out)) / out));
    }

    @Override
    public void setupAnim(AbyssalOrcEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float frac = ageInTicks - Mth.floor(ageInTicks);
        float amt = Math.min(limbSwingAmount, 1.0F);
        float walk = limbSwing * 0.42F;
        float breath = Mth.sin(ageInTicks * 0.07F);

        // ── stride: thighs swing, knees fold as each foot lifts, arms swing opposite with loose elbows,
        //    the torso twists against the hips, leans in and the whole upper body rises and falls
        float sR = Mth.sin(walk), sL = Mth.sin(walk + Mth.PI);
        this.rightLeg.xRot = Mth.cos(walk) * 0.75F * amt;
        this.leftLeg.xRot = Mth.cos(walk + Mth.PI) * 0.75F * amt;
        this.rightShin.xRot = Math.max(0.0F, sR) * 1.0F * amt;
        this.leftShin.xRot = Math.max(0.0F, sL) * 1.0F * amt;
        this.rightArm.xRot = Mth.cos(walk + Mth.PI) * 0.6F * amt;
        this.leftArm.xRot = Mth.cos(walk) * 0.6F * amt;
        this.rightForearm.xRot = -0.25F - (0.5F + 0.5F * Mth.cos(walk)) * 0.45F * amt;
        this.leftForearm.xRot = -0.25F - (0.5F + 0.5F * Mth.cos(walk + Mth.PI)) * 0.45F * amt;
        this.body.yRot = Mth.cos(walk) * 0.14F * amt;
        this.body.zRot = Mth.sin(walk) * 0.035F * amt;
        this.body.xRot = 0.09F * amt;
        float bob = Math.abs(Mth.cos(walk)) * 0.8F * amt;
        this.body.y += 0.8F * amt - bob;
        this.rightLeg.y += 0.8F * amt - bob;
        this.leftLeg.y += 0.8F * amt - bob;

        // arms hang out from the huge lats; standing still, it breathes: chest rises, shoulders roll
        this.rightArm.zRot = 0.14F + breath * 0.035F * (1.0F - amt);
        this.leftArm.zRot = -0.14F - breath * 0.035F * (1.0F - amt);
        this.body.xRot += -breath * 0.025F * (1.0F - amt);
        this.body.y += breath * 0.3F * (1.0F - amt);

        // the head looks where it wants, against the twist of the torso it sits on
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD - this.body.yRot;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD - this.body.xRot * 0.6F;

        // ── frenzy: leans in with its fists raised, ready to swing
        if (entity.bloodFrenzyTicks > 0) {
            this.body.xRot += 0.12F;
            this.head.xRot -= 0.12F;
            this.rightForearm.xRot -= 0.55F;
            this.leftForearm.xRot -= 0.55F;
            this.rightArm.xRot -= 0.2F;
            this.leftArm.xRot -= 0.2F;
        }

        // ── smash (on every landed blow): both fists come down together, knees bend, then it straightens up
        if (entity.smashAnimationTimeout > 0) {
            float p = (30 - entity.smashAnimationTimeout + frac) / 30.0F;
            float raise = 1.0F - ease(p / 0.12F);                       // fists start high, fall in a few frames
            float hit = envelope(p, 0.12F, 0.55F);
            float armX = Mth.lerp(raise, -1.1F, -2.6F) * Math.max(hit, raise);
            this.rightArm.xRot = armX;
            this.leftArm.xRot = armX;
            this.rightArm.zRot = 0.25F;
            this.leftArm.zRot = -0.25F;
            this.rightForearm.xRot = -0.3F - 0.6F * raise;
            this.leftForearm.xRot = -0.3F - 0.6F * raise;
            this.body.xRot = 0.5F * hit - 0.2F * raise;
            this.head.xRot -= 0.3F * hit;
            float crouch = 2.4F * hit;
            this.body.y += crouch;
            this.rightLeg.y += crouch;
            this.leftLeg.y += crouch;
            this.rightLeg.xRot = -0.55F * hit;
            this.leftLeg.xRot = -0.55F * hit;
            this.rightShin.xRot = 0.95F * hit;
            this.leftShin.xRot = 0.95F * hit;
        } else if (this.attackTime > 0.0F) {
            // ── a blow that missed: the right fist hooks across, the torso turning into it
            float p = this.attackTime;
            float wind = p < 0.3F ? ease(p / 0.3F) : 1.0F - ease((p - 0.3F) / 0.2F);
            float strike = p < 0.3F ? 0.0F : ease((p - 0.3F) / 0.15F) * (1.0F - ease((p - 0.6F) / 0.4F));
            this.rightArm.xRot = -1.9F * wind - 1.4F * strike;
            this.rightArm.yRot = 0.5F * strike;
            this.rightForearm.xRot = -1.3F * wind - 0.15F * strike;
            this.body.yRot += 0.35F * wind - 0.45F * strike;
            this.body.xRot += 0.15F * strike;
            this.leftArm.xRot = 0.35F * wind - 0.3F * strike;
        }

        // ── roar: head thrown back, chest out, arms flexed wide like a show of strength, shaking with it
        if (entity.roarAnimationTimeout > 0) {
            float p = (30 - entity.roarAnimationTimeout + frac) / 30.0F;
            float in = envelope(p, 0.18F, 0.2F);
            float shake = Mth.sin(ageInTicks * 2.4F) * 0.05F * in;
            this.head.xRot = Mth.lerp(in, this.head.xRot, -0.75F) + shake;
            this.body.xRot = Mth.lerp(in, this.body.xRot, -0.2F);
            this.rightArm.zRot = Mth.lerp(in, this.rightArm.zRot, 1.15F) + shake;
            this.leftArm.zRot = Mth.lerp(in, this.leftArm.zRot, -1.15F) - shake;
            this.rightArm.xRot = Mth.lerp(in, this.rightArm.xRot, -0.2F);
            this.leftArm.xRot = Mth.lerp(in, this.leftArm.xRot, -0.2F);
            this.rightForearm.xRot = Mth.lerp(in, this.rightForearm.xRot, -1.5F);
            this.leftForearm.xRot = Mth.lerp(in, this.leftForearm.xRot, -1.5F);
        }
    }
}
