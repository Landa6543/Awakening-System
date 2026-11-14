package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.FrostElfAssassinEntity;
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
 * The Frost Elf Assassin, drawn from scratch: slim and quick. Long swept-back ears, silver hair with a
 * ponytail in two pieces, ice-blue eyes over a mask that hides the mouth; a fitted dark leather coat with
 * silver trim, a high collar and a short ragged cloak; bracers and tall boots; and two curved ice daggers held
 * in a reverse grip. Stands low, ready to spring; slashes with both blades in turn.
 */
public class FrostElfAssassinModel extends HierarchicalModel<FrostElfAssassinEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "frost_elf_assassin"), "main");

    private final ModelPart root, body, torso, head, tail, tail2, cloak, cloak2, earR, earL;
    private final ModelPart rArm, lArm, rFore, lFore, rLeg, lLeg, rShin, lShin;

    public FrostElfAssassinModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.torso = body.getChild("torso");
        this.head = torso.getChild("head");
        this.tail = head.getChild("ponytail");
        this.tail2 = tail.getChild("ponytail2");
        this.earR = head.getChild("ear_right");
        this.earL = head.getChild("ear_left");
        this.cloak = torso.getChild("cloak");
        this.cloak2 = cloak.getChild("cloak2");
        this.rArm = torso.getChild("right_arm");
        this.lArm = torso.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rLeg = root.getChild("right_leg");
        this.lLeg = root.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -1.0F, -2.0F, 6.0F, 3.0F, 4.0F)                        // hips
                        .texOffs(20, 0).addBox(-3.0F, 1.5F, -2.2F, 6.0F, 4.0F, 1.0F)                      // coat tail, front
                        .texOffs(34, 0).addBox(-3.0F, 1.5F, 1.2F, 6.0F, 5.0F, 1.0F),                      // and back
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.08F, 0.0F, 0.0F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create()
                        .texOffs(0, 7).addBox(-3.5F, -9.0F, -2.0F, 7.0F, 9.0F, 4.0F)
                        .texOffs(22, 7).addBox(-3.5F, -10.0F, -2.5F, 7.0F, 2.0F, 5.0F, new CubeDeformation(0.1F)),  // high collar
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition cloak = torso.addOrReplaceChild("cloak", CubeListBuilder.create()
                        .texOffs(46, 7).addBox(-3.5F, 0.0F, 0.0F, 7.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 2.3F, 0.12F, 0.0F, 0.0F));
        cloak.addOrReplaceChild("cloak2", CubeListBuilder.create()
                        .texOffs(46, 15).addBox(-3.5F, 0.0F, 0.0F, 7.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.08F, 0.0F, 0.0F));

        // ── head: face, hair, mask, the long ears and the ponytail ──
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 7.0F, 6.0F)
                        .texOffs(24, 20).addBox(-3.0F, -7.5F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.35F))   // hair
                        .texOffs(48, 22).addBox(-3.0F, -6.0F, 2.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.2F))     // hair at the back
                        .texOffs(0, 33).addBox(-3.0F, -3.2F, -3.4F, 6.0F, 3.0F, 1.0F),                            // mask
                PartPose.offset(0.0F, -10.0F, -0.2F));
        for (int side = -1; side <= 1; side += 2) {
            head.addOrReplaceChild(side < 0 ? "ear_right" : "ear_left", CubeListBuilder.create().mirror(side > 0)
                            .texOffs(14, 33).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 6.0F),
                    PartPose.offsetAndRotation(side * 3.1F, -4.0F, -0.5F, 0.3F, side * 0.75F, 0.0F));            // back, out and a little up
        }
        PartDefinition tail = head.addOrReplaceChild("ponytail", CubeListBuilder.create()
                        .texOffs(28, 33).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 3.5F, -0.4F, 0.0F, 0.0F));
        tail.addOrReplaceChild("ponytail2", CubeListBuilder.create()
                        .texOffs(42, 33).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.6F, -0.85F, 0.0F, 0.0F));

        // ── arms with bracers, two curved ice daggers held point-back ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition arm = torso.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 38).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F)
                            .texOffs(12, 41).addBox(-2.0F, -1.6F, -2.0F, 4.0F, 2.0F, 4.0F),               // shoulder guard
                    PartPose.offsetAndRotation(side * 5.0F, -8.3F, 0.0F, -0.35F, 0.0F, side * -0.12F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                            .texOffs(28, 41).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                            .texOffs(40, 41).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.3F))   // bracer
                            .texOffs(52, 41).addBox(-1.0F, 5.5F, -1.0F, 2.0F, 2.0F, 2.0F),                // hand
                    PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.7F, 0.0F, 0.0F));
            PartDefinition dagger = fore.addOrReplaceChild(s + "_dagger", CubeListBuilder.create()
                            .texOffs(0, 48).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F)                 // grip
                            .texOffs(8, 48).addBox(-0.5F, -1.5F, 1.5F, 1.0F, 3.0F, 1.0F)                  // guard
                            .texOffs(12, 48).addBox(-0.5F, -0.5F, 2.5F, 1.0F, 2.0F, 5.0F),                // blade
                    PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.0F, 0.0F, 0.0F));
            dagger.addOrReplaceChild(s + "_dagger_tip", CubeListBuilder.create()
                            .texOffs(40, 50).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 0.5F, 7.2F, 0.45F, 0.0F, 0.0F));
        }

        // ── legs: tall boots ──
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                    PartPose.offsetAndRotation(side * 1.7F, 12.0F, 0.0F, -0.2F, 0.0F, side * -0.06F));
            leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                            .texOffs(36, 55).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.2F))
                            .texOffs(48, 55).addBox(-1.5F, 4.8F, -3.5F, 3.0F, 1.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.25F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(FrostElfAssassinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.8F;
        float breath = Mth.sin(ageInTicks * 0.1F);

        // a fast, low run: long strides, the body leaning in, arms held back with the blades trailing
        rLeg.xRot += Mth.cos(w) * 1.0F * amt;
        lLeg.xRot += -Mth.cos(w) * 1.0F * amt;
        rShin.xRot += Math.max(0.0F, Mth.sin(w)) * 1.1F * amt;
        lShin.xRot += Math.max(0.0F, -Mth.sin(w)) * 1.1F * amt;
        body.y += (1.0F - Math.abs(Mth.cos(w))) * 0.9F * amt + breath * 0.2F * (1 - amt);
        body.xRot += 0.3F * amt;
        torso.yRot = Mth.sin(w) * 0.15F * amt;
        rArm.xRot += -Mth.cos(w) * 0.6F * amt + 0.35F * amt;
        lArm.xRot += Mth.cos(w) * 0.6F * amt + 0.35F * amt;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD - torso.yRot;
        head.xRot = headPitch * Mth.DEG_TO_RAD - body.xRot * 0.7F;
        earR.yRot += Mth.sin(ageInTicks * 0.07F) * 0.04F;
        earL.yRot -= Mth.sin(ageInTicks * 0.07F) * 0.04F;

        // hair and cloak stream behind when running, drift when still
        tail.xRot += 0.35F * amt + Mth.sin(ageInTicks * 0.12F) * 0.06F;          // streams back when running
        tail2.xRot += 0.6F * amt + Mth.sin(ageInTicks * 0.12F - 0.7F) * 0.08F;
        tail.yRot = Mth.sin(w * 0.5F) * 0.2F * amt;
        cloak.xRot += 0.7F * amt + Mth.sin(ageInTicks * 0.09F) * 0.05F;
        cloak2.xRot += 0.4F * amt + Mth.sin(ageInTicks * 0.09F - 0.8F) * 0.08F;

        if (this.attackTime > 0.0F) {
            // a double slash: the right blade cuts across, then the left follows the other way
            float p = this.attackTime;
            float a = Mth.sin(Mth.clamp(p / 0.55F, 0.0F, 1.0F) * Mth.PI);
            float b = Mth.sin(Mth.clamp((p - 0.35F) / 0.65F, 0.0F, 1.0F) * Mth.PI);
            rArm.xRot = -0.35F - 1.4F * a;
            rArm.yRot = 0.9F * a - 0.4F * Mth.clamp(p / 0.55F, 0.0F, 1.0F) * a;
            rArm.zRot = 0.3F * a;
            lArm.xRot = -0.35F - 1.4F * b;
            lArm.yRot = -0.9F * b;
            lArm.zRot = -0.3F * b;
            torso.yRot += -0.5F * a + 0.5F * b;
            body.xRot += 0.2F * Math.max(a, b);
        }
    }
}
