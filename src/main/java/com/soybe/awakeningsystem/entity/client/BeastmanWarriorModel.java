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
 * Beastman warrior: a wolf-headed brute. Muzzle with fangs, tall ears, a mane over the shoulders, a deep
 * furred chest under leather straps and a bone pauldron, a bushy tail, and digitigrade legs (the knee bends
 * back) ending in clawed paws. Swings a heavy war axe two-handed from overhead.
 */
public class BeastmanWarriorModel extends HierarchicalModel<BeastmanWarriorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "beastman_warrior"), "main");

    private final ModelPart root, body, head, jaw, earR, earL, tail, rArm, lArm, rFore, lFore, rLeg, lLeg, rShin, lShin, rFoot, lFoot;

    public BeastmanWarriorModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.earR = head.getChild("ear_right");
        this.earL = head.getChild("ear_left");
        this.tail = body.getChild("tail");
        this.rArm = body.getChild("right_arm");
        this.lArm = body.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rLeg = root.getChild("right_leg");
        this.lLeg = root.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        this.rFoot = rShin.getChild("right_foot");
        this.lFoot = lShin.getChild("left_foot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 13).addBox(-4.0F, -5.0F, -2.5F, 8.0F, 5.0F, 5.0F)                     // hips
                .texOffs(0, 13).addBox(-5.0F, -16.0F, -3.0F, 10.0F, 11.0F, 6.0F)                   // chest
                .texOffs(58, 13).addBox(-5.5F, -17.0F, -3.5F, 11.0F, 4.0F, 7.0F, new CubeDeformation(0.2F))   // mane
                .texOffs(62, 0).addBox(-3.0F, -1.0F, -3.2F, 6.0F, 7.0F, 1.0F),                     // loincloth
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.12F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -6.0F, -4.0F, 7.0F, 6.0F, 7.0F)
                .texOffs(28, 0).addBox(-2.0F, -3.5F, -8.0F, 4.0F, 3.0F, 4.0F),                     // muzzle
                PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, -0.12F, 0.0F, 0.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, 0.0F, -3.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, -0.5F, -4.3F));
        head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-2.3F, -5.6F, -0.5F, -0.15F, 0.0F, -0.25F));
        head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(2.3F, -5.6F, -0.5F, -0.15F, 0.0F, 0.25F));

        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 9.0F)
                .texOffs(98, 0).addBox(-1.5F, -1.5F, 7.0F, 3.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 2.3F, -0.65F, 0.0F, 0.0F));

        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            CubeListBuilder upper = CubeListBuilder.create().mirror(m).texOffs(0, 30).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F);
            if (side < 0) upper.texOffs(12, 30).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 3.0F, 5.0F);              // bone pauldron on the axe arm
            PartDefinition arm = body.addOrReplaceChild(s + "_arm", upper,
                    PartPose.offsetAndRotation(side * 6.3F, -14.0F, 0.0F, -0.12F, 0.0F, side * 0.08F));
            CubeListBuilder fore = CubeListBuilder.create().mirror(m)
                    .texOffs(32, 30).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                    .texOffs(44, 30).addBox(-2.0F, 5.5F, -2.0F, 4.0F, 2.0F, 4.0F);
            if (side < 0) fore.texOffs(0, 45).addBox(-0.5F, 6.0F, -11.0F, 1.0F, 1.0F, 14.0F)               // the axe haft
                    .texOffs(30, 45).addBox(-0.5F, 3.0F, -13.0F, 1.0F, 6.0F, 5.0F);                         // and its blade
            arm.addOrReplaceChild(s + "_forearm", fore, PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.25F, 0.0F, 0.0F));

            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                    .texOffs(60, 30).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 8.0F, 4.0F),
                    PartPose.offsetAndRotation(side * 2.6F, 6.0F, 0.0F, -0.35F, 0.0F, 0.0F));
            PartDefinition shin = leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                    .texOffs(74, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.5F, 0.85F, 0.0F, 0.0F));                        // knee bends back
            shin.addOrReplaceChild(s + "_foot", CubeListBuilder.create().mirror(m)
                    .texOffs(82, 30).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 2.0F, 5.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.5F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(BeastmanWarriorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.6F;
        float breath = Mth.sin(ageInTicks * 0.09F);

        // loping stride: thighs swing, the bent-back shins fold further as each paw lifts
        rLeg.xRot += Mth.cos(w) * 0.75F * amt;
        lLeg.xRot += Mth.cos(w + Mth.PI) * 0.75F * amt;
        rShin.xRot += Math.max(0.0F, Mth.sin(w)) * 0.6F * amt;
        lShin.xRot += Math.max(0.0F, Mth.sin(w + Mth.PI)) * 0.6F * amt;
        body.y += -Math.abs(Mth.cos(w)) * 1.0F * amt + 1.0F * amt;
        rLeg.y += -Math.abs(Mth.cos(w)) * 1.0F * amt + 1.0F * amt;
        lLeg.y += -Math.abs(Mth.cos(w)) * 1.0F * amt + 1.0F * amt;
        body.xRot += 0.12F * amt - breath * 0.02F * (1 - amt);
        body.yRot = Mth.cos(w) * 0.1F * amt;
        lArm.xRot += Mth.cos(w) * 0.6F * amt;
        rArm.xRot += Mth.cos(w + Mth.PI) * 0.25F * amt - 0.35F;            // axe held ready
        lFore.xRot += -0.2F - 0.2F * amt;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD - body.yRot;
        head.xRot += headPitch * Mth.DEG_TO_RAD;
        jaw.xRot = 0.08F + Math.max(0.0F, breath) * 0.12F;                  // panting
        earR.zRot += Mth.sin(ageInTicks * 0.05F) * 0.05F;
        earL.zRot -= Mth.sin(ageInTicks * 0.05F) * 0.05F;
        tail.yRot = Mth.sin(ageInTicks * 0.16F) * 0.35F + Mth.sin(w) * 0.2F * amt;
        tail.xRot += Mth.sin(ageInTicks * 0.08F) * 0.06F;

        if (this.attackTime > 0.0F) {
            // two-handed chop: axe raised over the shoulder, then brought down hard, body following
            float p = this.attackTime;
            float up = p < 0.35F ? p / 0.35F : 1.0F - (p - 0.35F) / 0.65F;
            float down = p < 0.35F ? 0.0F : Mth.sin((p - 0.35F) / 0.65F * Mth.PI);
            rArm.xRot = -2.8F * up - 0.6F * down;
            rFore.xRot = -0.9F * up + 0.2F * down;
            lArm.xRot = -2.2F * up - 0.5F * down;
            lArm.zRot = -0.3F * up;
            body.xRot += -0.25F * up + 0.4F * down;
            jaw.xRot = 0.5F * Math.max(up, down);
        }
    }
}
