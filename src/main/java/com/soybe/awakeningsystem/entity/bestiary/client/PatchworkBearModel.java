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
 * The Patchwork Bear after the user's concept art. As a toy: a big round head with a muzzle, round ears, a button
 * for one eye; a plump body with a round belly; short arms with round paws; stubby legs. Turned: it crouches low and
 * hunches forward, the arms reach out with long black claws, and the whole lower half of the head drops open as a
 * jaw down to the chest, a maw of needle teeth above and below.
 */
public class PatchworkBearModel extends HierarchicalModel<PatchworkBearEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "patchwork_bear"), "main");

    private final ModelPart root, body, head, jaw, earR, earL, armR, armL, legR, legL, clawsAR, clawsAL, clawsLR, clawsLL;

    public PatchworkBearModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.earR = head.getChild("right_ear");
        this.earL = head.getChild("left_ear");
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

        // a plump body with a round belly on short legs (the hips at y=15)
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -12.0F, -3.5F, 12.0F, 12.0F, 7.0F)
                        .texOffs(38, 0).addBox(-5.0F, -10.0F, -4.5F, 10.0F, 9.0F, 1.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        // the head: the upper half (crown, eyes, upper muzzle, upper teeth) and the jaw (chin, lower muzzle,
        // lower teeth), which closed makes the whole round head and open drops down to the chest
        CubeListBuilder skull = CubeListBuilder.create()
                .texOffs(0, 19).addBox(-6.0F, -9.0F, -6.0F, 12.0F, 8.0F, 11.0F)
                .texOffs(46, 19).addBox(-3.0F, -5.0F, -9.0F, 6.0F, 4.0F, 3.0F)        // upper muzzle
                .texOffs(92, 19).addBox(-4.4F, -7.2F, -6.6F, 2.0F, 2.0F, 1.0F)        // the button eye
                .texOffs(98, 19).addBox(2.0F, -6.8F, -6.3F, 1.0F, 1.0F, 1.0F);        // the real eye
        for (int i = 0; i < 10; i++) skull.texOffs(102, 19).addBox(-5.0F + i * 1.1F, -1.5F, -5.6F, 1.0F, 2.0F, 1.0F);   // upper teeth
        for (int i = 0; i < 4; i++) skull.texOffs(102, 19).addBox(-2.3F + i * 1.3F, -1.5F, -8.6F, 1.0F, 2.0F, 1.0F);
        for (int side = -1; side <= 1; side += 2)
            for (int i = 0; i < 4; i++) skull.texOffs(102, 19).addBox(side * 5.0F - 0.5F, -1.5F, -4.5F + i * 2.0F, 1.0F, 2.0F, 1.0F);
        PartDefinition head = body.addOrReplaceChild("head", skull, PartPose.offset(0.0F, -12.0F, -0.5F));
        CubeListBuilder jawB = CubeListBuilder.create()
                .texOffs(46, 25).addBox(-5.5F, 0.0F, -10.0F, 11.0F, 3.0F, 10.0F)      // chin
                .texOffs(64, 19).addBox(-3.0F, 0.0F, -13.0F, 6.0F, 2.0F, 3.0F);       // lower muzzle
        for (int i = 0; i < 9; i++) jawB.texOffs(102, 19).addBox(-4.6F + i * 1.1F, -1.5F, -9.5F, 1.0F, 2.0F, 1.0F);   // lower teeth
        for (int i = 0; i < 3; i++) jawB.texOffs(102, 19).addBox(-1.8F + i * 1.3F, -1.5F, -12.6F, 1.0F, 2.0F, 1.0F);
        head.addOrReplaceChild("jaw", jawB, PartPose.offset(0.0F, -1.0F, 4.0F));

        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            head.addOrReplaceChild(s + "_ear", CubeListBuilder.create().mirror(m).texOffs(82, 19).addBox(-1.5F, -3.0F, -1.0F, 3.0F, 3.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 4.5F, -8.5F, 0.0F, 0.0F, 0.0F, side * 0.25F));
            // short arms ending in round paws, long black claws hidden in them
            PartDefinition arm = body.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                            .texOffs(0, 38).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                            .texOffs(16, 38).addBox(-2.5F, 8.0F, -2.5F, 5.0F, 3.0F, 5.0F),
                    PartPose.offsetAndRotation(side * 7.5F, -10.0F, 0.0F, 0.0F, 0.0F, side * -0.12F));
            CubeListBuilder ac = CubeListBuilder.create();
            for (int c = 0; c < 3; c++) ac.texOffs(56, 38).addBox(-1.6F + c * 1.3F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F);
            arm.addOrReplaceChild(s + "_arm_claws", ac, PartPose.offsetAndRotation(0.0F, 10.5F, -1.8F, -0.5F, 0.0F, 0.0F));
            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m).texOffs(36, 38).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                    PartPose.offset(side * 3.5F, 15.0F, 0.5F));
            CubeListBuilder lc = CubeListBuilder.create();
            for (int c = 0; c < 3; c++) lc.texOffs(60, 38).addBox(-1.6F + c * 1.3F, -0.5F, -3.0F, 1.0F, 1.0F, 3.0F);
            leg.addOrReplaceChild(s + "_leg_claws", lc, PartPose.offset(0.0F, 8.5F, -2.5F));
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
        float o = 0.0F;   // the toy only: once turned, the bear is drawn with FeralBearModel
        float plush = 1.0F - o;
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.9F;

        head.yRot = 0.0F;   // a toy's head does not follow anyone
        head.xRot = 0.0F;

        // the toy: a stiff, tottering waddle, rocking side to side, the big head lolling
        body.zRot += Mth.sin(w) * 0.14F * amt * plush + Mth.sin(age * 0.05F) * 0.03F * plush;
        legR.xRot += Mth.cos(w) * 0.5F * amt;
        legL.xRot -= Mth.cos(w) * 0.5F * amt;
        armR.xRot += -Mth.cos(w) * 0.3F * amt * plush;
        armL.xRot += Mth.cos(w) * 0.3F * amt * plush;
        head.zRot += Mth.sin(age * 0.04F) * 0.1F * plush;

        // the beast: standing up big, only a little crouched, legs braced wide, arms out to the sides with the claws
        // reaching forward; the head thrust forward over the chest and the jaw hanging open in front of it, working
        float chomp = Mth.sin(age * 0.45F) * 0.1F * o;
        body.y += 1.2F * o;
        body.xRot += 0.15F * o;
        legR.xRot -= 0.15F * o;
        legL.xRot -= 0.15F * o;
        legR.zRot += 0.22F * o;
        legL.zRot -= 0.22F * o;
        head.z -= 3.0F * o;
        head.y -= 0.5F * o;
        head.xRot -= 0.1F * o;
        jaw.xRot = 0.7F * o + chomp;
        armR.xRot += (-0.75F + Mth.cos(w) * 0.35F * amt) * o;
        armL.xRot += (-0.75F - Mth.cos(w) * 0.35F * amt) * o;
        armR.zRot += 0.65F * o + Mth.sin(age * 0.12F) * 0.06F * o;
        armL.zRot -= 0.65F * o + Mth.sin(age * 0.12F) * 0.06F * o;
        earR.zRot += 0.35F * o;
        earL.zRot -= 0.35F * o;
        clawsAR.visible = clawsAL.visible = clawsLR.visible = clawsLL.visible = o > 0.3F;

        if (this.attackTime > 0.0F) {
            // the lunge: it rears back with the jaw wide, then throws itself forward, both clawed arms slashing in
            // across its prey as the jaw snaps shut
            float t = this.attackTime;
            float rear = t < 0.35F ? t / 0.35F : Math.max(0.0F, 1.0F - (t - 0.35F) / 0.2F);
            float strike = t < 0.35F ? 0.0F : Mth.sin((t - 0.35F) / 0.65F * Mth.PI);
            body.xRot += -0.25F * rear + 0.4F * strike;
            head.xRot += -0.3F * rear + 0.25F * strike;
            jaw.xRot = 0.7F + 0.45F * rear - 0.75F * strike;
            armR.xRot = -1.9F * rear - 0.5F * strike - 0.3F;
            armL.xRot = -1.9F * rear - 0.5F * strike - 0.3F;
            armR.yRot = -1.1F * strike;
            armL.yRot = 1.1F * strike;
            armR.zRot = 0.9F * rear + 0.2F;
            armL.zRot = -0.9F * rear - 0.2F;
        }
    }
}
