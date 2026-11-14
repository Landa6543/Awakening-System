package com.soybe.awakeningsystem.entity.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The body every goblin shares and the gear of each kind. A big head with a brow ridge, a hooked nose and a
 * jaw of its own (it gapes when the goblin shrieks or strikes), long two-part ears that droop and flick, a
 * hunched chest over a pot belly, jointed arms and legs, big flat feet. Gear: the scout's hood, scarf, cape
 * and dagger; the warrior's horned helmet, pauldrons, shield and nailed club; the shaman's skull mask,
 * feathers, rags and orb staff. All three textures share the body layout (64x64), gear below it.
 */
public final class GoblinParts {
    private GoblinParts() {}

    public enum Kind { SCOUT, WARRIOR, SHAMAN }

    public static LayerDefinition create(Kind kind) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeListBuilder bodyCubes = CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.0F, -9.0F, -2.0F, 6.0F, 5.0F, 4.0F)                 // chest
                .texOffs(20, 16).addBox(-3.0F, -4.5F, -2.8F, 6.0F, 4.0F, 5.0F)                // pot belly
                .texOffs(42, 16).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 4.0F, 1.0F);               // loincloth
        if (kind == Kind.SCOUT) bodyCubes.texOffs(34, 32).addBox(-3.5F, -9.0F, 2.1F, 7.0F, 9.0F, 1.0F)          // cape
                .texOffs(0, 48).addBox(-3.5F, -9.8F, -2.5F, 7.0F, 2.0F, 5.0F, new CubeDeformation(0.15F));     // scarf
        if (kind == Kind.WARRIOR) bodyCubes.texOffs(22, 44).addBox(-4.8F, -9.6F, -2.3F, 3.0F, 2.0F, 4.0F)
                .texOffs(22, 44).mirror().addBox(1.8F, -9.6F, -2.3F, 3.0F, 2.0F, 4.0F).mirror(false);
        if (kind == Kind.SHAMAN) bodyCubes.texOffs(0, 32).addBox(-3.5F, -9.0F, -3.0F, 7.0F, 11.0F, 6.0F, new CubeDeformation(0.1F));
        PartDefinition body = root.addOrReplaceChild("body", bodyCubes, PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.3F, 0.0F, 0.0F));

        CubeListBuilder headCubes = CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 8.0F, 8.0F)
                .texOffs(34, 0).addBox(-4.0F, -6.0F, -5.2F, 8.0F, 1.0F, 1.0F)                  // brow ridge
                .texOffs(34, 2).addBox(-1.0F, -5.0F, -6.5F, 2.0F, 3.0F, 2.0F)                  // nose
                .texOffs(42, 2).addBox(-0.5F, -2.8F, -7.2F, 1.0F, 1.0F, 1.0F);                 // its hooked tip
        if (kind == Kind.SCOUT) headCubes.texOffs(0, 32).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.45F));
        if (kind == Kind.WARRIOR) headCubes.texOffs(0, 32).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 4.0F, 8.0F, new CubeDeformation(0.4F))
                .texOffs(34, 32).addBox(-5.2F, -11.0F, -1.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(34, 32).mirror().addBox(4.2F, -11.0F, -1.0F, 1.0F, 3.0F, 1.0F).mirror(false);
        if (kind == Kind.SHAMAN) headCubes.texOffs(26, 32).addBox(-3.5F, -7.5F, -5.6F, 7.0F, 7.0F, 1.0F)
                .texOffs(42, 32).addBox(-2.5F, -13.0F, -1.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(42, 32).addBox(-0.5F, -14.0F, -1.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(42, 32).mirror().addBox(1.5F, -13.0F, -1.0F, 1.0F, 5.0F, 1.0F).mirror(false);
        PartDefinition head = body.addOrReplaceChild("head", headCubes, PartPose.offsetAndRotation(0.0F, -9.0F, -0.5F, -0.3F, 0.0F, 0.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(42, 4).addBox(-3.5F, 0.0F, -3.8F, 7.0F, 2.0F, 4.0F)
                        .texOffs(56, 10).addBox(-2.8F, -1.0F, -3.7F, 1.0F, 1.0F, 1.0F)                // lower fangs
                        .texOffs(56, 10).mirror().addBox(1.8F, -1.0F, -3.7F, 1.0F, 1.0F, 1.0F).mirror(false),
                PartPose.offset(0.0F, -1.5F, -0.6F));
        for (int side = -1; side <= 1; side += 2) {
            boolean m = side > 0;
            PartDefinition ear = head.addOrReplaceChild(side < 0 ? "ear_right" : "ear_left", CubeListBuilder.create().mirror(m)
                    .texOffs(34, 10).addBox(m ? 0.0F : -5.0F, -1.5F, -0.5F, 5.0F, 3.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 4.3F, -5.0F, 0.5F, 0.0F, side * 0.5F, side * 0.45F));          // droop out and back
            ear.addOrReplaceChild("tip", CubeListBuilder.create().mirror(m)
                    .texOffs(46, 10).addBox(m ? 0.0F : -4.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 4.5F, -0.3F, 0.0F, 0.0F, side * 0.15F, side * 0.35F));
        }

        for (int side = -1; side <= 1; side += 2) {
            boolean m = side > 0;
            String s = side < 0 ? "right" : "left";
            PartDefinition arm = body.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                    .texOffs(0, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 4.0F, -8.5F, 0.0F, -0.3F, 0.0F, side * 0.12F));
            CubeListBuilder fore = CubeListBuilder.create().mirror(m)
                    .texOffs(8, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                    .texOffs(16, 25).addBox(-1.5F, 3.5F, -1.5F, 3.0F, 2.0F, 3.0F);
            if (side < 0 && kind == Kind.SCOUT) fore.texOffs(50, 32).addBox(-0.5F, 4.0F, -6.5F, 1.0F, 1.0F, 5.0F);
            if (side < 0 && kind == Kind.WARRIOR) fore.texOffs(0, 44).addBox(-1.0F, 3.0F, -9.5F, 2.0F, 2.0F, 9.0F);
            if (side < 0 && kind == Kind.SHAMAN) fore.texOffs(46, 32).addBox(-0.5F, -7.0F, -1.0F, 1.0F, 16.0F, 1.0F)
                    .texOffs(50, 32).addBox(-1.0F, -9.0F, -1.5F, 2.0F, 2.0F, 2.0F);
            if (side > 0 && kind == Kind.WARRIOR) fore.texOffs(38, 32).addBox(1.0F, -3.0F, -4.0F, 1.0F, 8.0F, 8.0F);
            arm.addOrReplaceChild(s + "_forearm", fore, PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.35F, 0.0F, 0.0F));

            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                    .texOffs(28, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 1.8F, 16.0F, 0.0F, -0.25F, 0.0F, 0.0F));
            PartDefinition shin = leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                    .texOffs(36, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.5F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, 3.8F, 0.0F, 0.35F, 0.0F, 0.0F));
            shin.addOrReplaceChild(s + "_foot", CubeListBuilder.create().mirror(m)
                    .texOffs(44, 25).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 2.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, -0.1F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** The parts a goblin model animates. */
    public record Rig(ModelPart body, ModelPart head, ModelPart jaw, ModelPart earR, ModelPart earL, ModelPart tipR, ModelPart tipL,
                      ModelPart rArm, ModelPart lArm, ModelPart rFore, ModelPart lFore, ModelPart rLeg, ModelPart lLeg, ModelPart rShin, ModelPart lShin) {
        public static Rig of(ModelPart root) {
            ModelPart body = root.getChild("body");
            ModelPart head = body.getChild("head");
            ModelPart earR = head.getChild("ear_right"), earL = head.getChild("ear_left");
            ModelPart rArm = body.getChild("right_arm"), lArm = body.getChild("left_arm");
            ModelPart rLeg = root.getChild("right_leg"), lLeg = root.getChild("left_leg");
            return new Rig(body, head, head.getChild("jaw"), earR, earL, earR.getChild("tip"), earL.getChild("tip"),
                    rArm, lArm, rArm.getChild("right_forearm"), lArm.getChild("left_forearm"),
                    rLeg, lLeg, rLeg.getChild("right_shin"), lLeg.getChild("left_shin"));
        }
    }

    /**
     * Scuttling run: knees and elbows bend, the body bobs and sways, ears flop with every step and flick now
     * and then, the jaw hangs open while running. A stab or an overhead blow when it attacks; the shaman
     * lifts its staff and shakes it while hostile.
     */
    public static void animate(Rig r, Kind kind, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float attack, boolean casting) {
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * (kind == Kind.SCOUT ? 1.0F : 0.85F);
        float breath = Mth.sin(ageInTicks * 0.15F);

        r.rLeg().xRot += Mth.cos(w) * 1.0F * amt;
        r.lLeg().xRot += Mth.cos(w + Mth.PI) * 1.0F * amt;
        r.rShin().xRot += Math.max(0.0F, Mth.sin(w)) * 0.9F * amt;
        r.lShin().xRot += Math.max(0.0F, Mth.sin(w + Mth.PI)) * 0.9F * amt;
        r.body().y += -Math.abs(Mth.cos(w)) * 1.0F * amt + 0.8F * amt + breath * 0.25F * (1 - amt);
        r.body().zRot = Mth.sin(w) * 0.1F * amt;
        r.body().xRot += 0.15F * amt;
        r.rArm().xRot += Mth.cos(w + Mth.PI) * 0.9F * amt;
        r.lArm().xRot += Mth.cos(w) * 0.9F * amt;
        r.rFore().xRot += -0.4F * amt;
        r.lFore().xRot += -0.4F * amt;

        r.head().yRot = netHeadYaw * Mth.DEG_TO_RAD;
        r.head().xRot += headPitch * Mth.DEG_TO_RAD + breath * 0.03F;
        r.jaw().xRot = 0.05F + 0.25F * amt + Math.max(0.0F, breath) * 0.05F;
        float flop = Mth.sin(w * 2) * 0.2F * amt;
        float twitch = Mth.sin(ageInTicks * 0.8F) * Math.max(0.0F, Mth.sin(ageInTicks * 0.045F) - 0.75F) * 3.0F;
        r.earR().zRot += flop + twitch * 0.3F;
        r.earL().zRot -= flop + twitch * 0.3F;
        r.tipR().zRot += flop * 1.5F;
        r.tipL().zRot -= flop * 1.5F;

        if (kind == Kind.WARRIOR) {
            r.lArm().xRot += -0.5F;
            r.lFore().xRot += -0.6F;
        }
        if (kind == Kind.SHAMAN) {
            r.rArm().xRot += -0.2F + breath * 0.05F;
            r.rFore().xRot += -0.3F;
            if (casting) {
                r.rArm().xRot = -2.5F + Mth.sin(ageInTicks * 0.9F) * 0.15F;
                r.rFore().xRot = -0.2F;
                r.lArm().xRot = -1.3F;
                r.lArm().zRot = 0.6F;
                r.head().xRot -= 0.35F;
                r.jaw().xRot = 0.5F + Mth.sin(ageInTicks * 1.3F) * 0.15F;     // chanting
            }
        }
        if (attack > 0.0F) {
            float s = Mth.sin(attack * Mth.PI);
            r.jaw().xRot = 0.6F * s;                                             // shrieks as it strikes
            if (kind == Kind.WARRIOR) {
                r.rArm().xRot = Mth.lerp(attack, -2.7F, 0.1F);
                r.rFore().xRot = Mth.lerp(attack, -0.8F, 0.0F);
                r.body().xRot += 0.3F * s;
            } else {
                r.rArm().xRot = -1.3F * s - 0.3F;
                r.rFore().xRot = -0.2F * (1 - s);
                r.rArm().yRot = 0.3F * s;
                r.body().yRot = -0.3F * s;
            }
        }
    }
}
