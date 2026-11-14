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
 * The body every goblin shares (big head with long ears and a long nose, hunched torso, long arms, big
 * feet, loincloth) and the gear of each kind: the scout's hood, cape and dagger, the warrior's horned
 * helmet, shield and club, the shaman's skull mask, feathers, rags and staff. All three textures share
 * the body layout (64x64) so their gear sits in the space below it.
 */
public final class GoblinParts {
    private GoblinParts() {}

    public enum Kind { SCOUT, WARRIOR, SHAMAN }

    public static LayerDefinition create(Kind kind) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeListBuilder bodyCubes = CubeListBuilder.create()
                .texOffs(0, 14).addBox(-3.0F, -9.0F, -2.0F, 6.0F, 9.0F, 4.0F)
                .texOffs(40, 22).addBox(-3.0F, -1.0F, -2.6F, 6.0F, 4.0F, 1.0F);           // loincloth
        if (kind == Kind.SCOUT) bodyCubes.texOffs(30, 28).addBox(-3.5F, -9.0F, 2.2F, 7.0F, 8.0F, 1.0F);   // cape
        if (kind == Kind.WARRIOR) bodyCubes.texOffs(8, 42).addBox(-4.5F, -9.5F, -2.5F, 3.0F, 2.0F, 4.0F)  // shoulder pad
                .texOffs(8, 42).mirror().addBox(1.5F, -9.5F, -2.5F, 3.0F, 2.0F, 4.0F).mirror(false);
        if (kind == Kind.SHAMAN) bodyCubes.texOffs(0, 28).addBox(-3.5F, -9.0F, -2.5F, 7.0F, 12.0F, 5.0F, new CubeDeformation(0.1F));   // rags
        PartDefinition body = root.addOrReplaceChild("body", bodyCubes, PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, 0.22F, 0.0F, 0.0F));

        CubeListBuilder headCubes = CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 7.0F)
                .texOffs(30, 0).addBox(-1.0F, -3.5F, -6.0F, 2.0F, 3.0F, 2.0F);            // the long nose
        if (kind == Kind.SCOUT) headCubes.texOffs(0, 28).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 7.0F, new CubeDeformation(0.45F));   // hood
        if (kind == Kind.WARRIOR) headCubes.texOffs(0, 28).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 4.0F, 7.0F, new CubeDeformation(0.4F))   // helmet
                .texOffs(30, 28).addBox(-4.6F, -10.0F, -1.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(30, 28).mirror().addBox(3.6F, -10.0F, -1.0F, 1.0F, 3.0F, 1.0F).mirror(false);
        if (kind == Kind.SHAMAN) headCubes.texOffs(24, 28).addBox(-3.0F, -6.5F, -5.0F, 6.0F, 6.0F, 1.0F)                     // skull mask
                .texOffs(38, 28).addBox(-2.5F, -12.0F, -1.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(38, 28).addBox(-0.5F, -13.0F, -1.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(38, 28).mirror().addBox(1.5F, -12.0F, -1.0F, 1.0F, 5.0F, 1.0F).mirror(false);
        PartDefinition head = body.addOrReplaceChild("head", headCubes, PartPose.offsetAndRotation(0.0F, -9.0F, -0.5F, -0.22F, 0.0F, 0.0F));
        head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(38, 0).addBox(-6.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-3.8F, -4.5F, 0.5F, 0.0F, -0.35F, -0.35F));
        head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(38, 0).mirror().addBox(0.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(3.8F, -4.5F, 0.5F, 0.0F, 0.35F, 0.35F));

        // arms: long and thin, big hands; each kind carries its gear
        CubeListBuilder rArm = CubeListBuilder.create()
                .texOffs(20, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F)
                .texOffs(28, 14).addBox(-1.5F, 7.0F, -1.5F, 3.0F, 2.0F, 3.0F);
        if (kind == Kind.SCOUT) rArm.texOffs(46, 28).addBox(-0.5F, 7.5F, -6.5F, 1.0F, 1.0F, 5.0F);                    // dagger
        if (kind == Kind.WARRIOR) rArm.texOffs(0, 42).addBox(-1.0F, 6.0F, -9.0F, 2.0F, 2.0F, 9.0F);                    // club, held forward
        if (kind == Kind.SHAMAN) rArm.texOffs(42, 28).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 16.0F, 1.0F)                   // staff
                .texOffs(46, 28).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 2.0F, 2.0F);                                         // its orb
        body.addOrReplaceChild("right_arm", rArm, PartPose.offsetAndRotation(-4.0F, -8.0F, 0.0F, -0.22F, 0.0F, 0.0F));
        CubeListBuilder lArm = CubeListBuilder.create().mirror()
                .texOffs(20, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F)
                .texOffs(28, 14).addBox(-1.5F, 7.0F, -1.5F, 3.0F, 2.0F, 3.0F).mirror(false);
        if (kind == Kind.WARRIOR) lArm.texOffs(34, 28).addBox(1.0F, 1.0F, -4.0F, 1.0F, 8.0F, 8.0F);                   // round-ish wooden shield
        body.addOrReplaceChild("left_arm", lArm, PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, -0.22F, 0.0F, 0.0F));

        for (int side = -1; side <= 1; side += 2) {
            CubeListBuilder leg = CubeListBuilder.create().mirror(side > 0)
                    .texOffs(40, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                    .texOffs(48, 14).addBox(-1.5F, 5.0F, -3.0F, 3.0F, 2.0F, 4.0F);                                   // big flat feet
            root.addOrReplaceChild(side < 0 ? "right_leg" : "left_leg", leg, PartPose.offset(side * 1.7F, 17.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** The parts a goblin model animates. */
    public record Rig(ModelPart body, ModelPart head, ModelPart earR, ModelPart earL, ModelPart rArm, ModelPart lArm, ModelPart rLeg, ModelPart lLeg) {
        public static Rig of(ModelPart root) {
            ModelPart body = root.getChild("body");
            ModelPart head = body.getChild("head");
            return new Rig(body, head, head.getChild("ear_right"), head.getChild("ear_left"),
                    body.getChild("right_arm"), body.getChild("left_arm"), root.getChild("right_leg"), root.getChild("left_leg"));
        }
    }

    /**
     * Scuttling run with a bob, ears flicking, head looking round; a stab or swing with the right arm when it
     * attacks. {@code casting} raises the shaman's staff and shakes it.
     */
    public static void animate(Rig r, Kind kind, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float attack, boolean casting) {
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * (kind == Kind.SCOUT ? 0.95F : 0.8F);
        r.rLeg().xRot = Mth.cos(w) * 1.1F * amt;
        r.lLeg().xRot = Mth.cos(w + Mth.PI) * 1.1F * amt;
        r.body().y += -Math.abs(Mth.sin(w)) * 1.2F * amt;
        r.body().zRot = Mth.sin(w) * 0.08F * amt;
        r.body().xRot += 0.12F * amt;
        r.rArm().xRot += Mth.cos(w + Mth.PI) * 0.9F * amt;
        r.lArm().xRot += Mth.cos(w) * 0.9F * amt;
        r.rArm().zRot = 0.1F;
        r.lArm().zRot = -0.1F;
        float breath = Mth.sin(ageInTicks * 0.15F);
        r.head().yRot = netHeadYaw * Mth.DEG_TO_RAD;
        r.head().xRot += headPitch * Mth.DEG_TO_RAD + breath * 0.03F;
        // ears twitch now and then
        float twitch = Mth.sin(ageInTicks * 0.7F) * Math.max(0.0F, Mth.sin(ageInTicks * 0.05F) - 0.7F) * 2.0F;
        r.earR().zRot += twitch * 0.4F + Mth.sin(w) * 0.15F * amt;
        r.earL().zRot -= twitch * 0.4F + Mth.sin(w) * 0.15F * amt;
        if (kind == Kind.WARRIOR) r.lArm().xRot += -0.6F;                       // shield up
        if (kind == Kind.SHAMAN) {
            r.rArm().xRot += -0.35F + breath * 0.05F;
            if (casting) {
                r.rArm().xRot = -2.6F + Mth.sin(ageInTicks * 0.9F) * 0.15F;
                r.lArm().xRot = -1.4F;
                r.lArm().zRot = -0.6F;
                r.head().xRot -= 0.3F;
            }
        }
        if (attack > 0.0F) {
            float s = Mth.sin(attack * Mth.PI);
            if (kind == Kind.WARRIOR) {                                         // overhead club blow
                r.rArm().xRot = Mth.lerp(attack, -2.6F, 0.2F);
                r.body().xRot += 0.25F * s;
            } else {                                                            // quick forward stab
                r.rArm().xRot = -1.5F * s - 0.2F;
                r.rArm().yRot = 0.3F * s;
                r.body().yRot = -0.25F * s;
            }
        }
    }
}
