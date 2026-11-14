package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.BeastmanWarriorEntity;
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
 * Beastman warrior, drawn from scratch: a hunched lion-wolf brute half again a man's height. A great mane
 * wraps its shoulders and runs up into a crest between swept-back ears; the long muzzle has a jaw of its own
 * and fangs top and bottom; the eyes glow. A heavy chest leans over a lean waist with a leather belt and
 * fur flaps; long arms end in thick forearms with bracers and clawed hands, a horned skull for a pauldron on
 * the left, a crude cleaver in the right. It stands on beast legs (the knee bends back) with clawed paws, and
 * a lion's tail with a tuft swings behind.
 *
 * <p>Every part is jointed: hips, waist, chest, neck, head and jaw; shoulder, elbow and wrist; hip, knee,
 * ankle and paw; and the tail in two pieces with its tuft.
 */
public class BeastmanWarriorModel extends HierarchicalModel<BeastmanWarriorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_warrior"), "main");

    private final ModelPart root, hips, waist, chest, neck, head, jaw, earR, earL, tail, tail2;
    private final ModelPart rArm, lArm, rFore, lFore, rHand, lHand;
    private final ModelPart rLeg, lLeg, rShin, lShin, rAnkle, lAnkle, rPaw, lPaw;

    public BeastmanWarriorModel(ModelPart root) {
        this.root = root;
        this.hips = root.getChild("hips");
        this.waist = hips.getChild("waist");
        this.chest = waist.getChild("chest");
        this.neck = chest.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.earR = head.getChild("ear_right");
        this.earL = head.getChild("ear_left");
        this.tail = hips.getChild("tail");
        this.tail2 = tail.getChild("tail2");
        this.rArm = chest.getChild("right_arm");
        this.lArm = chest.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rHand = rFore.getChild("right_hand");
        this.lHand = lFore.getChild("left_hand");
        this.rLeg = hips.getChild("right_leg");
        this.lLeg = hips.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        this.rAnkle = rShin.getChild("right_ankle");
        this.lAnkle = lShin.getChild("left_ankle");
        this.rPaw = rAnkle.getChild("right_paw");
        this.lPaw = lAnkle.getChild("left_paw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── hips, belt and fur flaps ──
        PartDefinition hips = root.addOrReplaceChild("hips", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-5.0F, -4.0F, -3.0F, 10.0F, 6.0F, 6.0F)                       // pelvis
                        .texOffs(0, 34).addBox(-5.5F, -1.5F, -3.5F, 11.0F, 2.0F, 7.0F)                       // belt
                        .texOffs(90, 73).addBox(-3.0F, 0.5F, -3.8F, 6.0F, 9.0F, 1.0F)                        // front flap
                        .texOffs(104, 73).addBox(-3.5F, 0.5F, 2.8F, 7.0F, 8.0F, 1.0F),                       // back flap
                PartPose.offsetAndRotation(0.0F, 0.9F, 1.5F, 0.12F, 0.0F, 0.0F));

        PartDefinition waist = hips.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(32, 20).addBox(-4.5F, -7.0F, -3.0F, 9.0F, 7.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.12F, 0.0F, 0.0F));

        // ── chest: ribcage, the hump of the shoulders, the mane round it all, a strap across ──
        PartDefinition chest = waist.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -11.0F, -4.5F, 14.0F, 11.0F, 9.0F)
                        .texOffs(92, 20).addBox(-5.0F, -13.0F, -2.0F, 10.0F, 3.0F, 6.0F)
                        .texOffs(46, 0).addBox(-7.5F, -13.0F, -5.0F, 15.0F, 5.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -6.5F, 0.0F, 0.1F, 0.0F, 0.0F));
        chest.addOrReplaceChild("strap", CubeListBuilder.create()
                        .texOffs(0, 94).addBox(-1.0F, -9.5F, -0.5F, 2.0F, 18.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, -5.1F, 0.0F, 0.0F, 0.72F));

        // ── neck and head ──
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(100, 34).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, -3.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(96, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 7.0F)                       // skull
                        .texOffs(32, 94).addBox(-4.0F, -6.2F, -4.6F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.15F)) // brow
                        .texOffs(60, 34).addBox(-2.5F, -4.5F, -9.0F, 5.0F, 3.0F, 5.0F)                      // muzzle
                        .texOffs(50, 94).addBox(-1.0F, -4.8F, -9.4F, 2.0F, 1.0F, 1.0F)                      // nose
                        .texOffs(62, 20).addBox(-5.0F, -6.5F, -1.5F, 10.0F, 7.0F, 5.0F)                     // cheek ruff
                        .texOffs(36, 34).addBox(-3.0F, -9.5F, -2.5F, 6.0F, 3.0F, 6.0F)                      // crest
                        .texOffs(64, 94).addBox(-2.0F, -1.6F, -8.5F, 1.0F, 2.0F, 1.0F)                      // fangs
                        .texOffs(64, 94).mirror().addBox(1.0F, -1.6F, -8.5F, 1.0F, 2.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(0.0F, -3.5F, -1.0F, -0.1F, 0.0F, 0.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(80, 34).addBox(-2.0F, 0.0F, -5.5F, 4.0F, 2.0F, 6.0F)
                        .texOffs(68, 94).addBox(-1.7F, -0.9F, -5.2F, 1.0F, 1.0F, 1.0F)
                        .texOffs(68, 94).mirror().addBox(0.7F, -0.9F, -5.2F, 1.0F, 1.0F, 1.0F).mirror(false),
                PartPose.offsetAndRotation(0.0F, -1.6F, -3.2F, 0.1F, 0.0F, 0.0F));
        head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(58, 94).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -6.5F, 0.0F, -0.65F, 0.0F, -0.45F));
        head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(58, 94).mirror().addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -6.5F, 0.0F, -0.65F, 0.0F, 0.45F));

        // ── arms ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            CubeListBuilder upper = CubeListBuilder.create().mirror(m)
                    .texOffs(24, 45).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 10.0F, 5.0F)                         // upper arm
                    .texOffs(0, 45).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.2F)); // shoulder
            if (side > 0) upper.mirror(false)
                    .texOffs(64, 61).addBox(-4.0F, -4.2F, -4.0F, 8.0F, 3.0F, 8.0F)                          // skull pauldron
                    .texOffs(82, 94).addBox(1.5F, -8.0F, -2.5F, 1.0F, 4.0F, 1.0F)                           // its horns
                    .texOffs(82, 94).addBox(1.5F, -8.0F, 1.5F, 1.0F, 4.0F, 1.0F);
            PartDefinition arm = chest.addOrReplaceChild(s + "_arm", upper,
                    PartPose.offsetAndRotation(side * 8.3F, -9.5F, 0.5F, -0.3F, 0.0F, side * -0.12F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                            .texOffs(44, 45).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F)
                            .texOffs(68, 45).addBox(-3.0F, 1.5F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.35F)), // bracer
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.4F, 0.0F, 0.0F));
            CubeListBuilder hand = CubeListBuilder.create().mirror(m)
                    .texOffs(46, 61).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 3.0F, 4.0F);
            for (int c = 0; c < 4; c++) hand.texOffs(72, 94).addBox(-2.3F + c * 1.3F, 2.6F, -2.4F, 1.0F, 3.0F, 1.0F);
            PartDefinition handPart = fore.addOrReplaceChild(s + "_hand", hand,
                    PartPose.offsetAndRotation(0.0F, 8.8F, 0.0F, -0.15F, 0.0F, 0.0F));
            if (side < 0) {
                // the cleaver: a long haft forward out of the fist, a broad hooked blade hanging from its head
                handPart.addOrReplaceChild("cleaver", CubeListBuilder.create()
                                .texOffs(0, 73).addBox(-1.0F, -1.0F, -13.0F, 2.0F, 2.0F, 18.0F)            // haft
                                .texOffs(12, 94).addBox(-1.0F, -1.0F, 4.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)) // pommel
                                .texOffs(20, 94).addBox(-1.5F, -1.5F, -13.5F, 3.0F, 3.0F, 3.0F)           // binding
                                .texOffs(40, 73).addBox(-0.5F, -2.0F, -19.0F, 1.0F, 9.0F, 7.0F)           // blade
                                .texOffs(6, 94).addBox(-0.5F, -5.0F, -17.5F, 1.0F, 3.0F, 2.0F),           // spike
                        PartPose.offsetAndRotation(0.0F, 1.8F, 0.0F, 0.0F, 0.0F, 0.0F));
            }

            // ── legs: thigh forward, shin back, ankle forward, paw flat ──
            PartDefinition leg = hips.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                            .texOffs(92, 45).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                    PartPose.offsetAndRotation(side * 3.4F, 1.0F, 0.0F, -0.62F, 0.0F, side * -0.05F));
            PartDefinition shin = leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 61).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, 1.2F, 0.0F, 0.0F));
            PartDefinition ankle = shin.addOrReplaceChild(s + "_ankle", CubeListBuilder.create().mirror(m)
                            .texOffs(16, 61).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.85F, 0.0F, 0.0F));
            CubeListBuilder paw = CubeListBuilder.create().mirror(m)
                    .texOffs(28, 61).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 5.0F);
            for (int c = 0; c < 3; c++) paw.texOffs(76, 94).addBox(-1.8F + c * 1.3F, 0.8F, -5.6F, 1.0F, 1.0F, 2.0F);
            ankle.addOrReplaceChild(s + "_paw", paw, PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.15F, 0.0F, 0.0F));
        }

        // ── the tail: up and back, then drooping, with a tuft ──
        PartDefinition tail = hips.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(96, 61).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -1.5F, 2.6F, 0.55F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                        .texOffs(56, 73).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 7.0F)
                        .texOffs(74, 73).addBox(-2.0F, -2.0F, 5.5F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.5F, 0.5F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(BeastmanWarriorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.55F;
        float idle = 1.0F - amt;
        float breath = Mth.sin(ageInTicks * 0.08F);

        // breathing: the chest swells and the shoulders rise, more when standing
        chest.xRot += breath * 0.03F * idle;
        chest.y += breath * 0.25F * idle;
        rArm.zRot += breath * 0.03F * idle;
        lArm.zRot -= breath * 0.03F * idle;

        // the stride of a beast: thighs swing far, the bent-back shins fold as each paw lifts and the paw
        // tips back off the ground; the hips drop on each footfall and roll from side to side
        float sR = Mth.sin(w), sL = Mth.sin(w + Mth.PI);
        rLeg.xRot += Mth.cos(w) * 0.7F * amt;
        lLeg.xRot += Mth.cos(w + Mth.PI) * 0.7F * amt;
        rShin.xRot += Math.max(0.0F, sR) * 0.75F * amt;
        lShin.xRot += Math.max(0.0F, sL) * 0.75F * amt;
        rAnkle.xRot -= Math.max(0.0F, sR) * 0.45F * amt;
        lAnkle.xRot -= Math.max(0.0F, sL) * 0.45F * amt;
        rPaw.xRot += Math.max(0.0F, -Mth.cos(w)) * 0.5F * amt;
        lPaw.xRot += Math.max(0.0F, -Mth.cos(w + Mth.PI)) * 0.5F * amt;
        hips.y += (1.0F - Math.abs(Mth.cos(w))) * 1.2F * amt;
        hips.zRot = Mth.cos(w) * 0.06F * amt;
        hips.yRot = Mth.sin(w) * 0.08F * amt;
        waist.yRot = -Mth.sin(w) * 0.12F * amt;                              // shoulders turn against the hips
        chest.xRot += 0.12F * amt;                                            // leans into the run

        // arms swing against the legs; the cleaver arm swings less, the free arm hangs heavy and loose
        lArm.xRot += Mth.cos(w) * 0.55F * amt;
        rArm.xRot += Mth.cos(w + Mth.PI) * 0.3F * amt;
        lFore.xRot += -0.15F - Math.max(0.0F, Mth.cos(w)) * 0.35F * amt;
        rFore.xRot += -0.1F * amt;
        lHand.xRot += Mth.sin(ageInTicks * 0.1F) * 0.06F;

        // head: looks where it looks, counter-bobbing the stride; jaw pants, ears twitch
        neck.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.4F;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.6F - waist.yRot;
        head.xRot += headPitch * Mth.DEG_TO_RAD * 0.8F - (1.0F - Math.abs(Mth.cos(w))) * 0.08F * amt;
        jaw.xRot += 0.06F + Math.max(0.0F, breath) * 0.1F + amt * 0.12F;
        float twitch = Mth.sin(ageInTicks * 0.31F) > 0.97F ? 0.25F : 0.0F;
        earR.zRot += Mth.sin(ageInTicks * 0.05F) * 0.05F - twitch;
        earL.zRot -= Mth.sin(ageInTicks * 0.05F) * 0.05F - twitch;

        // the tail: a slow lash at rest, swinging with the stride when moving, the tuft trailing behind
        tail.yRot = Mth.sin(ageInTicks * 0.09F) * 0.3F * idle + Mth.sin(w) * 0.35F * amt;
        tail.xRot += Mth.sin(ageInTicks * 0.06F) * 0.05F - 0.2F * amt;
        tail2.yRot = Mth.sin(ageInTicks * 0.09F - 0.9F) * 0.35F * idle + Mth.sin(w - 0.8F) * 0.3F * amt;
        tail2.xRot += 0.15F * amt;

        if (this.attackTime > 0.0F) {
            // overhead chop with both hands in three beats: the cleaver goes back over the shoulder while the
            // body rears and the jaw opens; it comes down hard in front with the chest and hips following
            // through; then everything settles back
            float p = this.attackTime;
            float wind = phase(p, 0.0F, 0.35F), strike = phase(p, 0.35F, 0.6F), back = phase(p, 0.6F, 1.0F);
            float arm = p < 0.35F ? Mth.lerp(wind, -0.3F, -3.0F) : p < 0.6F ? Mth.lerp(strike, -3.0F, -0.85F) : Mth.lerp(back, -0.85F, -0.3F);
            float fore = p < 0.35F ? Mth.lerp(wind, -0.4F, -1.25F) : p < 0.6F ? Mth.lerp(strike, -1.25F, -0.1F) : Mth.lerp(back, -0.1F, -0.4F);
            float lean = p < 0.35F ? Mth.lerp(wind, 0.0F, -0.28F) : p < 0.6F ? Mth.lerp(strike, -0.28F, 0.45F) : Mth.lerp(back, 0.45F, 0.0F);
            float open = p < 0.6F ? Math.max(wind, 1.0F - strike * 0.3F) : 1.0F - back;
            rArm.xRot = arm;
            rArm.zRot = -0.12F + 0.15F * (arm + 0.3F) / -2.7F;
            rFore.xRot = fore;
            rHand.xRot = -0.15F + (p < 0.6F ? strike * 0.5F : 0.5F * (1.0F - back));
            lArm.xRot = arm * 0.9F;
            lArm.zRot = 0.12F - 0.45F * (arm + 0.3F) / -2.7F;                 // the free hand joins the grip
            lFore.xRot = fore * 0.9F;
            chest.xRot += lean;
            waist.xRot += lean * 0.5F;
            neck.xRot -= lean * 0.4F;
            jaw.xRot = 0.1F + 0.55F * Mth.clamp(open, 0.0F, 1.0F);
            tail.xRot += -lean * 0.8F;
        }
    }

    /** 0 before {@code a}, 1 after {@code b}, eased in between. */
    private static float phase(float p, float a, float b) {
        float x = Mth.clamp((p - a) / (b - a), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}