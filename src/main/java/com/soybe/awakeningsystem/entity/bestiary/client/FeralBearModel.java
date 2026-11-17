package com.soybe.awakeningsystem.entity.bestiary.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.bestiary.PatchworkBearEntity;
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
 * The Patchwork Bear once it has turned: a model of its own, not the toy bent out of shape. A hunched beast on bent
 * hind legs, its torso pitched forward, the chest split open down the seam and lined with teeth. The great head is
 * thrust out ahead of the chest, so its jaw hangs open in front of the body with nothing in the way: needle teeth all
 * round, upper and lower. Long arms with heavy paws and long curved black claws.
 */
public class FeralBearModel extends HierarchicalModel<PatchworkBearEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "patchwork_bear_feral"), "main");

    private final ModelPart root, pelvis, torso, head, jaw, armR, armL, foreR, foreL, thighR, thighL, shinR, shinL;

    public FeralBearModel(ModelPart root) {
        this.root = root;
        this.pelvis = root.getChild("pelvis");
        this.torso = pelvis.getChild("torso");
        this.head = torso.getChild("head");
        this.jaw = head.getChild("jaw");
        this.armR = torso.getChild("right_arm");
        this.armL = torso.getChild("left_arm");
        this.foreR = armR.getChild("right_forearm");
        this.foreL = armL.getChild("left_forearm");
        this.thighR = pelvis.getChild("right_thigh");
        this.thighL = pelvis.getChild("left_thigh");
        this.shinR = thighR.getChild("right_shin");
        this.shinL = thighL.getChild("left_shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -3.0F, -3.0F, 12.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 6.0F, 2.0F));

        // bent hind legs: the thigh forward, the shin back, a long clawed foot flat on the ground
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition thigh = pelvis.addOrReplaceChild(s + "_thigh", CubeListBuilder.create().mirror(m).texOffs(38, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                    PartPose.offsetAndRotation(side * 5.0F, 0.0F, 0.0F, -0.6F, 0.0F, side * -0.12F));
            PartDefinition shin = thigh.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m).texOffs(62, 0).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                    PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, 1.0F, 0.0F, 0.0F));
            CubeListBuilder foot = CubeListBuilder.create().mirror(m).texOffs(82, 0).addBox(-3.0F, 0.0F, -6.0F, 6.0F, 3.0F, 7.0F);
            for (int c = 0; c < 3; c++) foot.texOffs(106, 42).addBox(-2.1F + c * 1.6F, 1.6F, -8.5F, 1.0F, 1.0F, 3.0F);
            shin.addOrReplaceChild(s + "_foot", foot, PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, -0.4F, 0.0F, 0.0F));
        }

        // the torso pitched forward, the chest torn open down the middle and lined with teeth
        CubeListBuilder torsoB = CubeListBuilder.create().texOffs(0, 12).addBox(-7.0F, -14.0F, -5.0F, 14.0F, 14.0F, 10.0F);
        for (int i = 0; i < 6; i++) {
            float y = -12.0F + i * 1.8F;
            torsoB.texOffs(118, 36).addBox(-3.6F, y, -5.6F, 2.0F, 1.0F, 1.0F);   // teeth along the left lip of the tear
            torsoB.texOffs(118, 36).addBox(1.6F, y + 0.9F, -5.6F, 2.0F, 1.0F, 1.0F);   // and along the right
        }
        PartDefinition torso = pelvis.addOrReplaceChild("torso", torsoB, PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.5F, 0.0F, 0.0F));

        // the head, thrust forward from the top of the chest, its jaw hinged at the back
        CubeListBuilder skull = CubeListBuilder.create()
                .texOffs(48, 14).addBox(-6.5F, -10.0F, -11.0F, 13.0F, 10.0F, 12.0F)
                .texOffs(98, 14).addBox(-3.5F, -5.0F, -15.0F, 7.0F, 4.0F, 4.0F)       // upper muzzle
                .texOffs(10, 50).addBox(-4.8F, -8.0F, -11.6F, 2.0F, 2.0F, 1.0F)       // what is left of the button eye
                .texOffs(16, 50).addBox(2.2F, -7.6F, -11.4F, 2.0F, 1.0F, 1.0F);       // the glowing eye
        for (int i = 0; i < 10; i++) skull.texOffs(114, 36).addBox(-5.6F + i * 1.2F, -0.3F, -10.6F, 1.0F, 2.0F, 1.0F);   // upper teeth
        for (int i = 0; i < 5; i++) skull.texOffs(114, 36).addBox(-2.9F + i * 1.3F, -1.3F, -14.6F, 1.0F, 2.0F, 1.0F);
        for (int side = -1; side <= 1; side += 2)
            for (int i = 0; i < 4; i++) skull.texOffs(114, 36).addBox(side * 5.3F - 0.5F, -0.3F, -8.6F + i * 2.0F, 1.0F, 2.0F, 1.0F);
        PartDefinition head = torso.addOrReplaceChild("head", skull, PartPose.offsetAndRotation(0.0F, -13.0F, -3.0F, -0.45F, 0.0F, 0.0F));
        CubeListBuilder jawB = CubeListBuilder.create()
                .texOffs(0, 36).addBox(-6.0F, 0.0F, -11.0F, 12.0F, 3.0F, 11.0F)
                .texOffs(98, 22).addBox(-3.0F, 0.0F, -15.0F, 6.0F, 2.0F, 4.0F);
        for (int i = 0; i < 9; i++) jawB.texOffs(114, 36).addBox(-5.0F + i * 1.2F, -1.8F, -10.4F, 1.0F, 2.0F, 1.0F);   // lower teeth
        for (int i = 0; i < 4; i++) jawB.texOffs(114, 36).addBox(-2.3F + i * 1.3F, -1.8F, -14.4F, 1.0F, 2.0F, 1.0F);
        head.addOrReplaceChild("jaw", jawB, PartPose.offsetAndRotation(0.0F, 0.0F, 0.5F, 0.85F, 0.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            head.addOrReplaceChild(s + "_ear", CubeListBuilder.create().mirror(m).texOffs(0, 50).addBox(-1.5F, -3.0F, -1.0F, 3.0F, 3.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 4.8F, -9.5F, -4.0F, -0.4F, 0.0F, side * 0.5F));
            // long arms: a heavy upper arm, a forearm reaching forward, a big paw, four long curved claws
            PartDefinition arm = torso.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m).texOffs(46, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                    PartPose.offsetAndRotation(side * 8.5F, -12.0F, -1.0F, -0.9F, 0.0F, side * -0.35F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m).texOffs(66, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, -0.45F, 0.0F, 0.0F));
            PartDefinition paw = fore.addOrReplaceChild(s + "_paw", CubeListBuilder.create().mirror(m).texOffs(82, 36).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                    PartPose.offset(0.0F, 8.5F, 0.0F));
            for (int c = 0; c < 4; c++) {
                PartDefinition claw = paw.addOrReplaceChild(s + "_claw" + c, CubeListBuilder.create().texOffs(106, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                        PartPose.offsetAndRotation(-2.2F + c * 1.45F, 2.5F, -2.2F, -0.5F, 0.0F, (c - 1.5F) * 0.1F));
                claw.addOrReplaceChild(s + "_claw_tip" + c, CubeListBuilder.create().texOffs(110, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                        PartPose.offsetAndRotation(0.0F, 4.6F, 0.0F, -0.7F, 0.0F, 0.0F));
            }
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
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.7F;
        float breath = Mth.sin(age * 0.12F);

        // a heavy, lumbering run: the legs drive, the torso rolls and dips, the arms swing low
        thighR.xRot += Mth.cos(w) * 0.6F * amt;
        thighL.xRot -= Mth.cos(w) * 0.6F * amt;
        shinR.xRot += Math.max(0.0F, Mth.sin(w)) * 0.5F * amt;
        shinL.xRot += Math.max(0.0F, -Mth.sin(w)) * 0.5F * amt;
        pelvis.y += Math.abs(Mth.cos(w)) * 1.0F * amt;
        torso.zRot += Mth.cos(w) * 0.07F * amt;
        torso.xRot += breath * 0.03F;
        armR.xRot += -Mth.cos(w) * 0.5F * amt;
        armL.xRot += Mth.cos(w) * 0.5F * amt;
        armR.zRot += breath * 0.04F;
        armL.zRot -= breath * 0.04F;

        // the head follows its prey; the jaw works
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.8F;
        head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
        jaw.xRot += Mth.sin(age * 0.45F) * 0.12F;

        if (this.attackTime > 0.0F) {
            // the lunge: it rears back with the jaw wide, then throws itself forward, the claws slashing in across
            // its prey as the jaw snaps shut
            float t = this.attackTime;
            float rear = t < 0.35F ? t / 0.35F : Math.max(0.0F, 1.0F - (t - 0.35F) / 0.2F);
            float strike = t < 0.35F ? 0.0F : Mth.sin((t - 0.35F) / 0.65F * Mth.PI);
            torso.xRot += -0.35F * rear + 0.35F * strike;
            head.xRot += -0.25F * rear + 0.15F * strike;
            jaw.xRot += 0.35F * rear - 0.75F * strike;
            armR.xRot += -1.3F * rear + 0.6F * strike;
            armL.xRot += -1.3F * rear + 0.6F * strike;
            armR.zRot += 0.6F * rear;
            armL.zRot -= 0.6F * rear;
            armR.yRot += -1.0F * strike;
            armL.yRot += 1.0F * strike;
            foreR.xRot -= 0.4F * strike;
            foreL.xRot -= 0.4F * strike;
        }
    }
}
