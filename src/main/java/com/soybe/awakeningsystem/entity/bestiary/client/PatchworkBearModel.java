package com.soybe.awakeningsystem.entity.bestiary.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.bestiary.PatchworkBearEntity;
import net.minecraft.client.Minecraft;
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
 * The Patchwork Bear after the user's concept art. A plump teddy: round head with a snout, round ears, a button
 * for one eye; a body whose belly is two flaps sewn shut down the middle; stubby arms and legs. Turned, the flaps
 * tear apart (the upper swinging up, the lower down) into a maw of needle teeth with stuffing behind, the snout's
 * jaw drops, black claws come out of the paws, and it hunches forward with its arms spread.
 */
public class PatchworkBearModel extends HierarchicalModel<PatchworkBearEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "patchwork_bear"), "main");

    private final ModelPart root, body, head, jaw, earR, earL, bellyUp, bellyDown, armR, armL, legR, legL, clawsAR, clawsAL, clawsLR, clawsLL;

    public PatchworkBearModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.earR = head.getChild("right_ear");
        this.earL = head.getChild("left_ear");
        this.bellyUp = body.getChild("belly_upper");
        this.bellyDown = body.getChild("belly_lower");
        this.armR = body.getChild("right_arm");
        this.armL = body.getChild("left_arm");
        this.legR = root.getChild("right_leg");
        this.legL = root.getChild("left_leg");
        this.clawsAR = armR.getChild("right_arm_claws");
        this.clawsAL = armL.getChild("left_arm_claws");
        this.clawsLR = legR.getChild("right_leg_claws");
        this.clawsLL = legL.getChild("left_leg_claws");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // body: the back half of the torso; its front face is the inside of the maw
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -13.0F, -1.0F, 11.0F, 13.0F, 4.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        // the belly: two flaps sewn together across the middle; teeth on their inner faces
        CubeListBuilder up = CubeListBuilder.create().texOffs(32, 0).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 6.0F, 3.0F);
        for (int i = 0; i < 7; i++) up.texOffs(24, 44).addBox(-4.4F + i * 1.4F, 3.5F, 0.0F, 1.0F, 3.0F, 1.0F);
        body.addOrReplaceChild("belly_upper", up, PartPose.offset(0.0F, -13.0F, -1.0F));
        CubeListBuilder down = CubeListBuilder.create().texOffs(32, 9).addBox(-5.0F, -7.0F, -3.0F, 10.0F, 7.0F, 3.0F);
        for (int i = 0; i < 6; i++) down.texOffs(24, 44).addBox(-3.7F + i * 1.4F, -6.5F, 0.0F, 1.0F, 3.0F, 1.0F);
        body.addOrReplaceChild("belly_lower", down, PartPose.offset(0.0F, 0.0F, -1.0F));

        // head: round, with a snout, a jaw that can drop, round ears, a button eye and a real one
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 9.0F, 9.0F)
                        .texOffs(40, 24).addBox(-2.5F, -4.0F, -7.0F, 5.0F, 3.0F, 2.0F)       // the snout
                        .texOffs(68, 24).addBox(-3.6F, -6.8F, -5.6F, 2.0F, 2.0F, 1.0F)       // the button eye
                        .texOffs(76, 24).addBox(1.6F, -6.4F, -5.3F, 1.0F, 1.0F, 1.0F),       // the real eye
                PartPose.offset(0.0F, -13.0F, -0.5F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(40, 31).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, -1.0F, -4.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            head.addOrReplaceChild(s + "_ear", CubeListBuilder.create().mirror(m).texOffs(56, 24).addBox(-1.5F, -3.0F, -1.0F, 3.0F, 3.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 3.8F, -8.0F, 0.0F, 0.0F, 0.0F, side * 0.3F));
            // stubby arms with black claws hidden in the paws
            PartDefinition arm = body.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m).texOffs(0, 44).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 10.0F, 4.0F),
                    PartPose.offsetAndRotation(side * 7.5F, -11.0F, 0.5F, 0.0F, 0.0F, side * -0.15F));
            CubeListBuilder ac = CubeListBuilder.create();
            for (int c = 0; c < 3; c++) ac.texOffs(16, 44).addBox(-1.5F + c * 1.2F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F);
            arm.addOrReplaceChild(s + "_arm_claws", ac, PartPose.offsetAndRotation(0.0F, 8.5F, -1.5F, -0.4F, 0.0F, 0.0F));
            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m).texOffs(32, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F),
                    PartPose.offset(side * 3.0F, 13.0F, 0.5F));
            CubeListBuilder lc = CubeListBuilder.create();
            for (int c = 0; c < 3; c++) lc.texOffs(16, 44).addBox(-1.6F + c * 1.3F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F);
            leg.addOrReplaceChild(s + "_leg_claws", lc, PartPose.offset(0.0F, 10.5F, -2.5F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(PatchworkBearEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float pt = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float o = e == null ? 0 : e.open(pt);
        float plush = 1.0F - o;
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.9F;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        // the toy: a stiff, tottering waddle, rocking side to side, the head lolling
        body.zRot += Mth.sin(w) * 0.14F * amt * plush + Mth.sin(age * 0.05F) * 0.03F * plush;
        legR.xRot += Mth.cos(w) * 0.5F * amt;
        legL.xRot -= Mth.cos(w) * 0.5F * amt;
        armR.xRot += -Mth.cos(w) * 0.3F * amt * plush;
        armL.xRot += Mth.cos(w) * 0.3F * amt * plush;
        head.zRot += Mth.sin(age * 0.04F) * 0.12F * plush;
        earR.zRot += Mth.sin(age * 0.1F) * 0.04F;
        earL.zRot -= Mth.sin(age * 0.1F) * 0.04F;

        // the beast: hunched, arms spread with the claws out, the belly torn wide and chomping
        float chomp = Mth.sin(age * 0.55F) * 0.22F * o;
        body.xRot += 0.35F * o;
        head.xRot -= 0.35F * o;
        armR.zRot += 0.9F * o;
        armL.zRot -= 0.9F * o;
        armR.xRot += -0.9F * o + Mth.cos(w) * 0.4F * amt * o;
        armL.xRot += -0.9F * o - Mth.cos(w) * 0.4F * amt * o;
        bellyUp.xRot = -1.75F * o + chomp;
        bellyDown.xRot = 1.5F * o - chomp;
        jaw.xRot = 0.8F * o + chomp * 0.6F;
        clawsAR.visible = clawsAL.visible = clawsLR.visible = clawsLL.visible = o > 0.3F;

        if (this.attackTime > 0.0F) {
            // a lunging bite: the maw snaps shut on its prey, the arms rake in
            float s = Mth.sin(this.attackTime * Mth.PI);
            body.xRot += 0.4F * s;
            bellyUp.xRot += 1.2F * s;
            bellyDown.xRot -= 1.0F * s;
            armR.yRot = -0.8F * s;
            armL.yRot = 0.8F * s;
        }
    }
}
