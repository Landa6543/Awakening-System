package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
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
 * The Elemental Demon King, drawn from scratch: a demon monarch in black plate split by glowing cracks. A
 * horned helm with a crown of spikes and burning eyes; a core gem in the chest; spiked pauldrons; a long cape
 * in two pieces and a skirt of plates over the legs; a sceptre crowned with an orb in the right hand and a
 * sigil burning in the left palm. Behind his head hangs an octagonal halo carrying four elemental crystals
 * (fire, frost, storm, wind) that turns slowly. The glow (cracks, core, eyes, orb, sigil) takes the colour of
 * his phase, see {@link ElementalDemonKingGlowLayer}.
 */
public class ElementalDemonKingModel extends HierarchicalModel<ElementalDemonKingBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "elemental_demon_king"), "main");

    private final ModelPart root, body, torso, head, cape, cape2, halo, rArm, lArm, rFore, lFore, rHand, lHand, orb;
    private final ModelPart rLeg, lLeg, rShin, lShin, tassetF, tassetB, tassetR, tassetL;
    private final ModelPart[] crystals = new ModelPart[4];

    public ElementalDemonKingModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.torso = body.getChild("torso");
        this.head = torso.getChild("head");
        this.cape = torso.getChild("cape");
        this.cape2 = cape.getChild("cape2");
        this.halo = torso.getChild("halo");
        for (int i = 0; i < 4; i++) crystals[i] = halo.getChild("crystal_" + i);
        this.rArm = torso.getChild("right_arm");
        this.lArm = torso.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rHand = rFore.getChild("right_hand");
        this.lHand = lFore.getChild("left_hand");
        this.orb = rHand.getChild("sceptre").getChild("orb");
        this.rLeg = root.getChild("right_leg");
        this.lLeg = root.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        this.tassetF = body.getChild("tasset_front");
        this.tassetB = body.getChild("tasset_back");
        this.tassetR = body.getChild("tasset_right");
        this.tassetL = body.getChild("tasset_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── waist and the skirt of plates ──
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(78, 0).addBox(-5.0F, -3.0F, -3.0F, 10.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        body.addOrReplaceChild("tasset_front", CubeListBuilder.create().texOffs(48, 47).addBox(-4.5F, 0.0F, -0.5F, 9.0F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.4F, -0.08F, 0.0F, 0.0F));
        body.addOrReplaceChild("tasset_back", CubeListBuilder.create().texOffs(68, 47).addBox(-5.0F, 0.0F, -0.5F, 10.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 3.4F, 0.1F, 0.0F, 0.0F));
        body.addOrReplaceChild("tasset_right", CubeListBuilder.create().texOffs(90, 47).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 10.0F, 6.0F),
                PartPose.offsetAndRotation(-5.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.12F));
        body.addOrReplaceChild("tasset_left", CubeListBuilder.create().texOffs(90, 47).mirror().addBox(-0.5F, 0.0F, -3.0F, 1.0F, 10.0F, 6.0F),
                PartPose.offsetAndRotation(5.4F, 0.0F, 0.0F, 0.0F, 0.0F, -0.12F));

        // ── torso: abdomen, chest with its core, gorget ──
        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.5F, -6.0F, -3.0F, 9.0F, 6.0F, 6.0F)
                        .texOffs(0, 0).addBox(-7.0F, -14.0F, -4.0F, 14.0F, 8.0F, 8.0F)
                        .texOffs(110, 0).addBox(-1.5F, -12.0F, -4.6F, 3.0F, 3.0F, 1.0F)
                        .texOffs(30, 16).addBox(-4.5F, -16.0F, -3.5F, 9.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // ── the cape, from behind the shoulders to near the ground, in two pieces ──
        PartDefinition cape = torso.addOrReplaceChild("cape", CubeListBuilder.create()
                        .texOffs(0, 31).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 13.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -14.5F, 4.2F, 0.08F, 0.0F, 0.0F));
        cape.addOrReplaceChild("cape2", CubeListBuilder.create()
                        .texOffs(30, 31).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.05F, 0.0F, 0.0F));

        // ── head: helm, mask, crown with spikes, two great horns sweeping back ──
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(94, 16).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F)
                        .texOffs(110, 5).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 3.0F, 1.0F)
                        .texOffs(62, 16).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.1F))
                        .texOffs(90, 64).addBox(-0.5F, -12.0F, -4.2F, 1.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, -16.0F, -0.5F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            head.addOrReplaceChild(s + "_spike", CubeListBuilder.create().texOffs(90, 64).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 2.2F, -7.0F, -3.6F, -0.1F, 0.0F, side * 0.3F));
            PartDefinition horn = head.addOrReplaceChild(s + "_horn", CubeListBuilder.create().texOffs(0, 73).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                    PartPose.offsetAndRotation(side * 3.3F, -6.0F, 0.5F, 0.3F, 0.0F, side * 0.85F));
            PartDefinition mid = horn.addOrReplaceChild(s + "_horn_mid", CubeListBuilder.create().texOffs(12, 73).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, -4.6F, 0.0F, 0.6F, 0.0F, side * -0.4F));
            mid.addOrReplaceChild(s + "_horn_tip", CubeListBuilder.create().texOffs(20, 73).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -4.6F, 0.0F, 0.7F, 0.0F, side * -0.25F));
        }

        // ── the elemental halo behind the head: eight bars in an octagon, four crystals on it ──
        PartDefinition halo = torso.addOrReplaceChild("halo", CubeListBuilder.create(),
                PartPose.offset(0.0F, -21.0F, 7.0F));
        for (int i = 0; i < 8; i++)
            halo.addOrReplaceChild("halo_bar_" + i, CubeListBuilder.create().texOffs(34, 70).addBox(-3.5F, -9.5F, -0.5F, 7.0F, 1.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, i * Mth.PI / 4.0F));
        int[] crystalU = {78, 62, 70, 78}, crystalV = {64, 71, 71, 71};
        for (int i = 0; i < 4; i++) {
            float a = i * Mth.HALF_PI + Mth.PI / 4.0F;
            halo.addOrReplaceChild("crystal_" + i, CubeListBuilder.create().texOffs(crystalU[i], crystalV[i]).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.offsetAndRotation(Mth.sin(a) * 9.6F, -Mth.cos(a) * 9.6F, 0.0F, 0.0F, 0.0F, -a));
        }

        // ── arms: spiked pauldron, upper arm, gauntlet, clawed hand ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition arm = torso.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                            .texOffs(100, 31).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 9.0F, 5.0F)
                            .texOffs(44, 0).addBox(-4.0F, -3.5F, -4.5F, 8.0F, 5.0F, 9.0F),
                    PartPose.offsetAndRotation(side * 9.0F, -12.5F, 0.0F, 0.0F, 0.0F, side * -0.08F));
            for (int k = 0; k < 3; k++)
                arm.addOrReplaceChild(s + "_spike_" + k, CubeListBuilder.create().texOffs(98, 64).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                        PartPose.offsetAndRotation(side * 1.5F, -3.3F, -2.5F + k * 2.5F, 0.0F, 0.0F, side * (0.45F + k * 0.08F)));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 47).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                    PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.2F, 0.0F, 0.0F));
            CubeListBuilder hand = CubeListBuilder.create().mirror(m).texOffs(0, 64).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 3.0F, 4.0F);
            for (int c = 0; c < 4; c++) hand.texOffs(112, 47).addBox(-2.3F + c * 1.3F, 2.6F, -1.8F, 1.0F, 3.0F, 1.0F);
            PartDefinition handPart = fore.addOrReplaceChild(s + "_hand", hand, PartPose.offset(0.0F, 7.8F, 0.0F));
            if (side < 0) {
                // the sceptre, upright through the fist, an orb held in a cage of prongs at the top
                PartDefinition sceptre = handPart.addOrReplaceChild("sceptre", CubeListBuilder.create()
                                .texOffs(104, 47).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 30.0F, 2.0F)
                                .texOffs(50, 64).addBox(-1.5F, -19.0F, -1.5F, 3.0F, 2.0F, 3.0F)
                                .texOffs(120, 47).addBox(-0.5F, 12.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                        PartPose.offsetAndRotation(0.0F, 1.8F, -0.5F, 0.0F, 0.0F, 0.0F));
                for (int k = 0; k < 4; k++) {
                    float a = k * Mth.HALF_PI + Mth.PI / 4.0F;
                    sceptre.addOrReplaceChild("prong_" + k, CubeListBuilder.create().texOffs(116, 47).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                            PartPose.offsetAndRotation(Mth.sin(a) * 1.6F, -18.5F, Mth.cos(a) * 1.6F, Mth.cos(a) * 0.35F, 0.0F, -Mth.sin(a) * 0.35F));
                }
                sceptre.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(18, 64).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                        PartPose.offset(0.0F, -22.0F, 0.0F));
            }
        }

        // ── legs: armoured thigh, shin with a knee guard and a pointed sabaton ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                            .texOffs(60, 31).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                    PartPose.offset(side * 2.8F, 2.0F, 0.0F));
            leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                            .texOffs(80, 31).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.1F))
                            .texOffs(34, 64).addBox(-3.0F, -1.0F, -3.4F, 6.0F, 3.0F, 2.0F)
                            .texOffs(24, 47).addBox(-2.5F, 8.0F, -5.0F, 5.0F, 2.0F, 7.0F),
                    PartPose.offset(0.0F, 10.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ElementalDemonKingBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float idle = 1.0F - amt;
        float w = limbSwing * 0.45F;
        float breath = Mth.sin(ageInTicks * 0.06F);

        // a king's bearing: a slow breath, the head turning to whoever he looks at
        torso.xRot = breath * 0.02F;
        torso.y += breath * 0.3F * idle;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.7F;

        // the halo turns, its crystals spin on themselves and bob
        halo.zRot = ageInTicks * 0.015F;
        halo.y += Mth.sin(ageInTicks * 0.05F) * 0.4F;
        for (int i = 0; i < 4; i++) {
            crystals[i].yRot = ageInTicks * 0.08F + i;
            crystals[i].zRot += Mth.sin(ageInTicks * 0.1F + i * 1.7F) * 0.15F;
        }
        // the orb breathes
        float pulse = 1.0F + Mth.sin(ageInTicks * 0.15F) * 0.08F;
        orb.xScale = pulse;
        orb.yScale = pulse;
        orb.zScale = pulse;
        orb.yRot = ageInTicks * 0.05F;

        // a measured stride; the plates of the skirt ride on the legs
        float swing = Mth.cos(w) * 0.5F * amt;
        rLeg.xRot = swing;
        lLeg.xRot = -swing;
        rShin.xRot = Math.max(0.0F, Mth.sin(w)) * 0.45F * amt;
        lShin.xRot = Math.max(0.0F, -Mth.sin(w)) * 0.45F * amt;
        body.y += (1.0F - Math.abs(Mth.cos(w))) * 0.8F * amt;
        body.yRot = Mth.sin(w) * 0.05F * amt;
        tassetF.xRot += -Math.max(Math.abs(swing), 0.0F) * 0.6F;
        tassetB.xRot += Math.abs(swing) * 0.5F;
        tassetR.zRot += Math.max(0.0F, -swing) * 0.2F;
        tassetL.zRot -= Math.max(0.0F, swing) * 0.2F;

        // arms: the sceptre arm held steady, the free arm swinging a little, the sigil hand half open
        rArm.xRot = -0.15F - swing * 0.2F;
        rFore.xRot = -0.35F;
        lArm.xRot = -swing * 0.6F;
        lArm.zRot += Mth.sin(ageInTicks * 0.05F) * 0.03F;
        lFore.xRot = -0.25F;
        lHand.xRot = -0.3F;

        // the cape sways and lifts behind him as he walks
        cape.xRot += 0.06F + amt * 0.35F + Mth.sin(ageInTicks * 0.07F) * 0.04F;
        cape2.xRot += amt * 0.2F + Mth.sin(ageInTicks * 0.07F - 0.8F) * 0.06F;
        cape.zRot = Mth.sin(ageInTicks * 0.05F) * 0.03F;

        if (this.attackTime > 0.0F) {
            // the sceptre is raised high and brought down like a judgement, the free hand thrown forward
            // with the sigil blazing; the body leans into it
            float p = this.attackTime;
            float wind = phase(p, 0.0F, 0.4F), strike = phase(p, 0.4F, 0.62F), back = phase(p, 0.62F, 1.0F);
            float arm = p < 0.4F ? Mth.lerp(wind, -0.15F, -2.9F) : p < 0.62F ? Mth.lerp(strike, -2.9F, -0.7F) : Mth.lerp(back, -0.7F, -0.15F);
            float lean = p < 0.4F ? Mth.lerp(wind, 0.0F, -0.15F) : p < 0.62F ? Mth.lerp(strike, -0.15F, 0.3F) : Mth.lerp(back, 0.3F, 0.0F);
            float thrust = p < 0.62F ? Math.max(wind * 0.4F, strike) : 1.0F - back;
            rArm.xRot = arm;
            rFore.xRot = -0.35F + (p < 0.4F ? -0.5F * wind : -0.5F * (1.0F - strike));
            lArm.xRot = -1.4F * thrust;
            lArm.zRot = 0.1F * thrust;
            lFore.xRot = -0.25F + 0.2F * thrust;
            lHand.xRot = -0.3F - 0.9F * thrust;
            torso.xRot += lean;
            torso.yRot = -0.15F * thrust;
            cape.xRot += -lean * 0.6F;
        }
    }

    /** 0 before {@code a}, 1 after {@code b}, eased in between. */
    private static float phase(float p, float a, float b) {
        float x = Mth.clamp((p - a) / (b - a), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}
