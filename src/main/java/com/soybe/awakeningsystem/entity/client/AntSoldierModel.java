package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AntSoldierEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The ant soldier, drawn from scratch as a slender ant knight. A long insect head with great compound eyes,
 * crests swept back, two jointed mandibles hooked at the tips and long antennae in two segments; a thin neck
 * and a wasp waist between a carapace chest and an ant's gaster hanging behind, with a stinger; two small arms
 * folded at the waist; long arms with spiked carapace pauldrons, a curved scythe along each forearm and
 * three talons; insect legs that bend back; and two pairs of wings folded down the back that spread when it
 * pounces. Rears up with its mandibles wide and its arms out when it screeches.
 */
public class AntSoldierModel extends HierarchicalModel<AntSoldierEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ant_soldier"), "main");

    private final ModelPart root, hips, gaster, gaster2, waist, thorax, neck, head, mandR, mandL, antR, antL, antR2, antL2;
    private final ModelPart rArm, lArm, rFore, lFore, rMid, lMid, rLeg, lLeg, rShin, lShin, rTar, lTar;
    private final ModelPart wingR, wingL, wingR2, wingL2;

    public AntSoldierModel(ModelPart root) {
        this.root = root;
        this.hips = root.getChild("hips");
        this.gaster = hips.getChild("gaster");
        this.gaster2 = gaster.getChild("gaster2");
        this.waist = hips.getChild("waist");
        this.thorax = waist.getChild("thorax");
        this.neck = thorax.getChild("neck");
        this.head = neck.getChild("head");
        this.mandR = head.getChild("mandible_right");
        this.mandL = head.getChild("mandible_left");
        this.antR = head.getChild("antenna_right");
        this.antL = head.getChild("antenna_left");
        this.antR2 = antR.getChild("antenna_right_tip");
        this.antL2 = antL.getChild("antenna_left_tip");
        this.rArm = thorax.getChild("right_arm");
        this.lArm = thorax.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rMid = thorax.getChild("right_small_arm");
        this.lMid = thorax.getChild("left_small_arm");
        this.wingR = thorax.getChild("right_wing");
        this.wingL = thorax.getChild("left_wing");
        this.wingR2 = thorax.getChild("right_hind_wing");
        this.wingL2 = thorax.getChild("left_hind_wing");
        this.rLeg = hips.getChild("right_leg");
        this.lLeg = hips.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        this.rTar = rShin.getChild("right_tarsus");
        this.lTar = lShin.getChild("left_tarsus");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition hips = root.addOrReplaceChild("hips", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -2.0F, -2.5F, 6.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 4.6F, 1.0F, 0.15F, 0.0F, 0.0F));

        // ── the gaster hanging behind, two segments and a stinger ──
        PartDefinition gaster = hips.addOrReplaceChild("gaster", CubeListBuilder.create()
                        .texOffs(22, 0).addBox(-3.0F, -2.5F, 0.0F, 6.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 2.2F, 0.75F, 0.0F, 0.0F));
        PartDefinition gaster2 = gaster.addOrReplaceChild("gaster2", CubeListBuilder.create()
                        .texOffs(44, 0).addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 0.3F, 0.0F, 0.0F));
        gaster2.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(70, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, 5.8F, 0.35F, 0.0F, 0.0F));

        // ── wasp waist and the carapace chest ──
        PartDefinition waist = hips.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition thorax = waist.addOrReplaceChild("thorax", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-5.0F, -10.0F, -3.0F, 10.0F, 10.0F, 6.0F)
                        .texOffs(32, 12).addBox(-5.5F, -10.5F, 1.0F, 11.0F, 8.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -2.8F, 0.0F, 0.15F, 0.0F, 0.0F));

        // ── neck and head ──
        PartDefinition neck = thorax.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(92, 0).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, -1.5F, -0.45F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(60, 12).addBox(-3.0F, -5.0F, -4.0F, 6.0F, 5.0F, 7.0F)
                        .texOffs(86, 12).addBox(-2.0F, -4.0F, -5.6F, 4.0F, 4.0F, 2.0F)
                        .texOffs(98, 12).addBox(-3.6F, -4.6F, -3.2F, 1.0F, 3.0F, 3.0F)
                        .texOffs(98, 12).mirror().addBox(2.6F, -4.6F, -3.2F, 1.0F, 3.0F, 3.0F).mirror(false),
                PartPose.offsetAndRotation(0.0F, -2.6F, 0.0F, 0.42F, 0.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            head.addOrReplaceChild(s + "_crest", CubeListBuilder.create().mirror(m).texOffs(106, 12).addBox(-0.5F, -2.0F, 0.0F, 1.0F, 2.0F, 6.0F),
                    PartPose.offsetAndRotation(side * 1.6F, -4.6F, -1.0F, 0.45F, side * 0.15F, 0.0F));
            PartDefinition mand = head.addOrReplaceChild("mandible_" + s, CubeListBuilder.create().mirror(m)
                            .texOffs(0, 29).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F),
                    PartPose.offsetAndRotation(side * 1.6F, -0.8F, -4.6F, 0.25F, side * -0.3F, 0.0F));
            mand.addOrReplaceChild("mandible_" + s + "_tip", CubeListBuilder.create().mirror(m)
                            .texOffs(12, 29).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F),
                    PartPose.offsetAndRotation(0.0F, 0.0F, -4.6F, 0.0F, side * 0.85F, 0.0F));
            PartDefinition ant = head.addOrReplaceChild("antenna_" + s, CubeListBuilder.create()
                            .texOffs(20, 29).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 1.2F, -4.8F, -3.4F, -0.35F, 0.0F, side * -0.35F));
            ant.addOrReplaceChild("antenna_" + s + "_tip", CubeListBuilder.create()
                            .texOffs(24, 29).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, 0.9F, 0.0F, 0.0F));

            // ── long arms: spiked carapace pauldron, scythe along the forearm, three talons ──
            PartDefinition arm = thorax.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                            .texOffs(56, 29).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F)
                            .texOffs(28, 29).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 4.0F, 6.0F),
                    PartPose.offsetAndRotation(side * 6.6F, -8.5F, 0.0F, -0.2F, 0.0F, side * -0.12F));
            arm.addOrReplaceChild(s + "_spike", CubeListBuilder.create().texOffs(52, 29).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 2.0F, -2.3F, 0.5F, -0.3F, 0.0F, side * 0.55F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                            .texOffs(68, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.65F, 0.0F, 0.0F));
            PartDefinition blade = fore.addOrReplaceChild(s + "_scythe", CubeListBuilder.create()
                            .texOffs(84, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 12.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 1.2F, 1.0F, 1.8F, 0.12F, 0.0F, 0.0F));
            blade.addOrReplaceChild(s + "_scythe_tip", CubeListBuilder.create().texOffs(90, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, 11.6F, 0.2F, -0.9F, 0.0F, 0.0F));
            CubeListBuilder hand = CubeListBuilder.create().mirror(m).texOffs(94, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F);
            for (int c = 0; c < 3; c++) hand.texOffs(106, 29).addBox(-1.5F + c * 1.0F, 1.6F, -1.4F, 1.0F, 3.0F, 1.0F);
            fore.addOrReplaceChild(s + "_hand", hand, PartPose.offsetAndRotation(0.0F, 7.8F, 0.0F, -0.2F, 0.0F, 0.0F));

            // ── the small middle arms, folded at the waist ──
            PartDefinition mid = thorax.addOrReplaceChild(s + "_small_arm", CubeListBuilder.create()
                            .texOffs(110, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 4.2F, -2.0F, -1.6F, -0.9F, 0.0F, side * -0.25F));
            mid.addOrReplaceChild(s + "_small_claw", CubeListBuilder.create().texOffs(114, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, 4.6F, 0.0F, 1.3F, 0.0F, 0.0F));

            // ── wings folded down the back: a long fore wing and a shorter hind wing each side ──
            thorax.addOrReplaceChild(s + "_wing", CubeListBuilder.create().mirror(m)
                            .texOffs(48, 44).addBox(m ? 0.0F : -7.0F, 0.0F, 0.0F, 7.0F, 16.0F, 0.0F),
                    PartPose.offsetAndRotation(side * 1.0F, -9.5F, 4.2F, 0.18F, 0.0F, side * 0.1F));
            thorax.addOrReplaceChild(s + "_hind_wing", CubeListBuilder.create().mirror(m)
                            .texOffs(62, 44).addBox(m ? 0.0F : -6.0F, 0.0F, 0.0F, 6.0F, 13.0F, 0.0F),
                    PartPose.offsetAndRotation(side * 1.5F, -7.0F, 4.4F, 0.24F, 0.0F, side * 0.3F));

            // ── legs: thigh forward, shin back, tarsus forward, clawed foot ──
            PartDefinition leg = hips.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 44).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F),
                    PartPose.offsetAndRotation(side * 2.6F, 1.0F, 0.0F, -0.6F, 0.0F, side * -0.06F));
            PartDefinition shin = leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                            .texOffs(12, 44).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, 1.2F, 0.0F, 0.0F));
            PartDefinition tar = shin.addOrReplaceChild(s + "_tarsus", CubeListBuilder.create().mirror(m)
                            .texOffs(20, 44).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.75F, 0.0F, 0.0F));
            CubeListBuilder foot = CubeListBuilder.create().mirror(m).texOffs(28, 44).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 1.0F, 4.0F);
            foot.texOffs(42, 44).addBox(-1.6F, 0.2F, -4.6F, 1.0F, 1.0F, 2.0F).texOffs(42, 44).addBox(0.6F, 0.2F, -4.6F, 1.0F, 1.0F, 2.0F);
            tar.addOrReplaceChild(s + "_foot", foot, PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, 0.0F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AntSoldierEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float idle = 1.0F - amt;
        float w = limbSwing * 0.7F;
        boolean pounce = entity != null && entity.isPouncing();
        boolean screech = entity != null && entity.isScreeching();

        // the head jerks like an insect's; antennae feel the air; mandibles work
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot += headPitch * Mth.DEG_TO_RAD + (Mth.sin(ageInTicks * 0.9F) > 0.92F ? 0.08F : 0.0F);
        antR.xRot += Mth.sin(ageInTicks * 0.2F) * 0.12F;
        antL.xRot += Mth.sin(ageInTicks * 0.2F + 1.3F) * 0.12F;
        antR2.xRot += Mth.sin(ageInTicks * 0.27F) * 0.15F;
        antL2.xRot += Mth.sin(ageInTicks * 0.27F + 0.8F) * 0.15F;
        float chew = Math.max(0.0F, Mth.sin(ageInTicks * 0.35F)) * 0.15F;
        mandR.yRot -= chew;
        mandL.yRot += chew;

        // a quick, springy gait: legs swing far, shins fold, the gaster bobs behind
        float swing = Mth.cos(w) * 0.8F * amt;
        rLeg.xRot += swing;
        lLeg.xRot -= swing;
        rShin.xRot += Math.max(0.0F, Mth.sin(w)) * 0.8F * amt;
        lShin.xRot += Math.max(0.0F, -Mth.sin(w)) * 0.8F * amt;
        rTar.xRot -= Math.max(0.0F, Mth.sin(w)) * 0.4F * amt;
        lTar.xRot -= Math.max(0.0F, -Mth.sin(w)) * 0.4F * amt;
        hips.y += (1.0F - Math.abs(Mth.cos(w))) * 0.9F * amt;
        thorax.xRot += 0.18F * amt + Mth.sin(ageInTicks * 0.08F) * 0.02F * idle;
        gaster.xRot += Mth.sin(ageInTicks * 0.1F) * 0.05F - Math.abs(Mth.cos(w)) * 0.12F * amt;
        gaster2.xRot += Mth.sin(ageInTicks * 0.1F - 0.6F) * 0.05F;
        rArm.xRot += -Mth.cos(w) * 0.45F * amt;
        lArm.xRot += Mth.cos(w) * 0.45F * amt;
        rMid.xRot += Mth.sin(ageInTicks * 0.3F) * 0.08F;
        lMid.xRot += Mth.sin(ageInTicks * 0.3F + 1.5F) * 0.08F;

        // wings: a faint shiver at rest, buzzing open when it pounces
        float shiver = Mth.sin(ageInTicks * 1.7F) * 0.02F;
        wingR.zRot += shiver;
        wingL.zRot -= shiver;
        if (pounce) {
            float buzz = Mth.sin(ageInTicks * 3.1F) * 0.25F;
            wingR.zRot = -1.25F + buzz;
            wingL.zRot = 1.25F - buzz;
            wingR.xRot = 0.9F;
            wingL.xRot = 0.9F;
            wingR2.zRot = -1.0F - buzz;
            wingL2.zRot = 1.0F + buzz;
            wingR2.xRot = 1.0F;
            wingL2.xRot = 1.0F;
            rArm.xRot = -1.6F;
            lArm.xRot = -1.6F;
            rFore.xRot = -0.3F;
            lFore.xRot = -0.3F;
            rLeg.xRot = 0.6F;
            lLeg.xRot = 0.6F;
        }
        if (screech) {
            // rears back, mandibles wide, arms thrown out
            head.xRot -= 0.6F;
            neck.xRot -= 0.3F;
            thorax.xRot -= 0.25F;
            mandR.yRot -= 0.6F;
            mandL.yRot += 0.6F;
            rArm.zRot = 1.1F;
            lArm.zRot = -1.1F;
            rArm.xRot = -0.6F;
            lArm.xRot = -0.6F;
            wingR.zRot = -0.5F;
            wingL.zRot = 0.5F;
        }

        if (this.attackTime > 0.0F) {
            // a scissor cut: both scythes swing in and cross in front of it, the mandibles snapping
            float s = Mth.sin(this.attackTime * Mth.PI);
            rArm.xRot = -0.2F - 1.6F * s;
            lArm.xRot = -0.2F - 1.6F * s;
            rArm.yRot = 0.7F * s;
            lArm.yRot = -0.7F * s;
            rFore.xRot = -0.65F + 0.35F * s;
            lFore.xRot = -0.65F + 0.35F * s;
            thorax.xRot += 0.25F * s;
            mandR.yRot = -0.3F + 0.5F * s;
            mandL.yRot = 0.3F - 0.5F * s;
        }
    }
}
