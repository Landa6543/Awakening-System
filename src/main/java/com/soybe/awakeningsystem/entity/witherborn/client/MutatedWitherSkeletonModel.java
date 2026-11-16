package com.soybe.awakeningsystem.entity.witherborn.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.witherborn.MutatedWitherSkeletonEntity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * The Mutated Wither Skeleton, after the user's concept art: a towering black skeleton with a wide barrel of a
 * ribcage, seven curved ribs a side wrapping round towards the front and leaving it open, and in the hollow a
 * large dark sphere (the core) that turns, bobs and swells; the lumbar spine bare below the ribs down to a broad
 * pelvis with flaring hip wings; long legs with knobbed knees and long feet; wide shoulders with knobs, long arms
 * with knobbed elbows and big hands of four long curved claws; a skull with a hanging jaw and two horns curving up.
 */
public class MutatedWitherSkeletonModel extends HierarchicalModel<MutatedWitherSkeletonEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutated_wither_skeleton"), "main");

    private static final int RIB_ROWS = 7;

    private final ModelPart root, pelvis, spine, chest, core, skull, jaw, rArm, lArm, rFore, lFore, rLeg, lLeg, rShin, lShin;
    private final List<ModelPart> ribs = new ArrayList<>();

    public MutatedWitherSkeletonModel(ModelPart root) {
        this.root = root;
        this.pelvis = root.getChild("pelvis");
        this.spine = pelvis.getChild("spine");
        this.chest = spine.getChild("chest");
        this.core = chest.getChild("core");
        this.skull = chest.getChild("neck").getChild("skull");
        this.jaw = skull.getChild("jaw");
        this.rArm = chest.getChild("right_arm");
        this.lArm = chest.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rLeg = pelvis.getChild("right_leg");
        this.lLeg = pelvis.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        for (int i = 0; i < RIB_ROWS; i++)
            for (String s : new String[]{"right", "left"}) ribs.add(chest.getChild(s + "_rib" + i));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // the hips stand 31 pixels above the feet (y=24 is the ground)
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 28).addBox(-6.0F, -2.0F, -2.0F, 12.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            // the flaring wings of the hip bone
            pelvis.addOrReplaceChild(s + "_hip_wing", CubeListBuilder.create().mirror(m).texOffs(32, 28).addBox(m ? 0.0F : -5.0F, -5.0F, -0.5F, 5.0F, 5.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 2.0F, -1.0F, 0.0F, 0.0F, side * 0.35F, side * -0.45F));
            // legs: femur, a knee knob, tibia, a long foot with three toes
            PartDefinition leg = pelvis.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m).texOffs(44, 28).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                    PartPose.offsetAndRotation(side * 4.5F, 1.5F, 0.0F, 0.0F, 0.0F, side * -0.06F));
            PartDefinition shin = leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                            .texOffs(56, 28).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 4.0F)
                            .texOffs(72, 30).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                    PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, 0.0F, side * 0.06F));
            CubeListBuilder foot = CubeListBuilder.create().mirror(m).texOffs(0, 36).addBox(-2.0F, 0.0F, -6.0F, 4.0F, 2.0F, 7.0F);
            for (int t = 0; t < 3; t++) foot.texOffs(30, 36).addBox(-1.6F + t * 1.3F, 1.0F, -9.0F, 1.0F, 1.0F, 3.0F);
            shin.addOrReplaceChild(s + "_foot", foot, PartPose.offset(0.0F, 13.5F, 0.0F));
        }

        // the lumbar spine, bare, rising from the pelvis to the ribcage
        CubeListBuilder lumbar = CubeListBuilder.create().texOffs(84, 4).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 24.0F, 2.0F);
        for (int v = 0; v < 4; v++) lumbar.texOffs(92, 6).addBox(-2.0F, -2.5F - v * 2.6F, -1.5F, 4.0F, 2.0F, 3.0F);
        PartDefinition spine = pelvis.addOrReplaceChild("spine", lumbar, PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, 0.1F, 0.0F, 0.0F));

        // the chest: its top (the shoulders) 26 pixels above the hips
        PartDefinition chest = spine.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 0.0F));
        for (int i = 0; i < RIB_ROWS; i++) {
            float y = -13.0F + i * 2.0F;
            // the barrel: widest in the middle, the ribs curving forward and in, the front left open
            float out = 4.0F + 2.0F * Mth.sin((i + 0.6F) / RIB_ROWS * Mth.PI);
            float fwd = 4.0F + 1.2F * Mth.sin((i + 0.6F) / RIB_ROWS * Mth.PI);
            for (int side = -1; side <= 1; side += 2) {
                String s = side < 0 ? "right" : "left";
                boolean m = side > 0;
                PartDefinition rib = chest.addOrReplaceChild(s + "_rib" + i, CubeListBuilder.create().mirror(m).texOffs(74, 4)
                                .addBox(m ? 0.0F : -out, -0.5F, -0.5F, out, 1.0F, 1.0F),
                        PartPose.offsetAndRotation(side * 1.0F, y, 1.5F, 0.0F, side * 0.45F, side * -0.18F));
                PartDefinition mid = rib.addOrReplaceChild(s + "_rib_mid" + i, CubeListBuilder.create().mirror(m).texOffs(74, 4)
                                .addBox(m ? 0.0F : -fwd, -0.5F, -0.5F, fwd, 1.0F, 1.0F),
                        PartPose.offsetAndRotation(side * (out - 0.3F), 0.0F, 0.0F, 0.0F, side * 1.1F, 0.0F));
                mid.addOrReplaceChild(s + "_rib_end" + i, CubeListBuilder.create().mirror(m).texOffs(74, 6)
                                .addBox(m ? 0.0F : -3.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                        PartPose.offsetAndRotation(side * (fwd - 0.3F), 0.0F, 0.0F, 0.15F, side * 0.75F, 0.0F));
            }
        }
        // the core: a large dark sphere (three crossed blocks) floating in the open chest
        chest.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F)
                        .texOffs(20, 16).addBox(-2.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F)
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, -7.0F, -3.0F));
        // collarbone and shoulders
        chest.addOrReplaceChild("collarbone", CubeListBuilder.create().texOffs(74, 0).addBox(-9.0F, -1.0F, -1.0F, 18.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -15.0F, 0.5F));

        // neck and skull: the jaw hanging open, two horns curving up and out
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(66, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.5F, -0.2F, 0.0F, 0.0F));
        PartDefinition skull = neck.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, 0.2F, 0.0F, 0.0F));
        skull.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, 0.0F, -5.0F, 6.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 0.15F, 0.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition horn = skull.addOrReplaceChild(s + "_horn", CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 3.0F, -7.5F, -1.0F, 0.15F, 0.0F, side * 0.55F));
            horn.addOrReplaceChild(s + "_horn_tip", CubeListBuilder.create().texOffs(62, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -3.6F, 0.0F, 0.0F, 0.0F, side * -0.75F));

            // long arms: a shoulder knob, humerus, elbow knob, forearm, a big hand of four long claws
            PartDefinition arm = chest.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                            .texOffs(114, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)
                            .texOffs(92, 11).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 15.0F, 3.0F),
                    PartPose.offsetAndRotation(side * 9.5F, -15.0F, 0.5F, 0.0F, 0.0F, side * -0.18F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                            .texOffs(114, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F)
                            .texOffs(104, 11).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 14.0F, 3.0F),
                    PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, -0.3F, 0.0F, side * 0.1F));
            PartDefinition hand = fore.addOrReplaceChild(s + "_hand", CubeListBuilder.create().mirror(m).texOffs(56, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 14.5F, 0.0F, -0.15F, 0.0F, 0.0F));
            for (int f = 0; f < 4; f++) {
                float fx = -1.6F + f * 1.05F;
                PartDefinition finger = hand.addOrReplaceChild(s + "_finger" + f, CubeListBuilder.create().texOffs(22, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                        PartPose.offsetAndRotation(fx, 2.6F, -1.4F + (f == 0 ? 1.4F : 0.0F), -0.25F, 0.0F, (f - 1.5F) * 0.12F));
                finger.addOrReplaceChild(s + "_claw" + f, CubeListBuilder.create().texOffs(26, 36).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                        PartPose.offsetAndRotation(0.0F, 5.6F, 0.0F, -0.7F, 0.0F, 0.0F));
            }
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(MutatedWitherSkeletonEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(1.0F, limbSwingAmount);
        float w = limbSwing * 0.5F;
        // a long, menacing stride, knees bending as the legs come forward
        rLeg.xRot += Mth.cos(w) * 0.6F * amt;
        lLeg.xRot -= Mth.cos(w) * 0.6F * amt;
        rShin.xRot += Math.max(0.0F, -Mth.sin(w)) * 0.7F * amt;
        lShin.xRot += Math.max(0.0F, Mth.sin(w)) * 0.7F * amt;
        rArm.xRot += -Mth.cos(w) * 0.45F * amt;
        lArm.xRot += Mth.cos(w) * 0.45F * amt;
        pelvis.y += Math.abs(Mth.sin(w)) * -0.8F * amt;
        spine.xRot += 0.08F + Mth.sin(age * 0.06F) * 0.02F;      // a stoop that breathes
        skull.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        skull.xRot += headPitch * Mth.DEG_TO_RAD;
        jaw.xRot += Math.max(0.0F, Mth.sin(age * 0.07F)) * 0.15F;
        rArm.zRot += Mth.sin(age * 0.05F) * 0.04F;
        lArm.zRot -= Mth.sin(age * 0.05F) * 0.04F;

        // the core turns and bobs, beating
        core.yRot = age * 0.06F;
        core.y += Mth.sin(age * 0.1F) * 0.5F;
        float beat = 1.0F + Math.max(0.0F, Mth.sin(age * 0.25F)) * 0.08F;
        int pulse = e == null ? 0 : e.pulse();
        if (pulse > 0) {
            float p = Math.min(1.0F, pulse / (float) MutatedWitherSkeletonEntity.PULSE_WINDUP);
            beat += p * 0.6F;
            rArm.zRot += 1.1F * p;
            lArm.zRot -= 1.1F * p;
            rArm.xRot -= 0.6F * p;
            lArm.xRot -= 0.6F * p;
            skull.xRot -= 0.5F * p;
            jaw.xRot += 0.6F * p;
            for (int i = 0; i < ribs.size(); i++) ribs.get(i).yRot += ((i % 2 == 0) ? -1 : 1) * 0.3F * p;   // the chest opens wider
        }
        core.xScale = core.yScale = core.zScale = beat;

        if (this.attackTime > 0.0F) {
            // a raking blow with both clawed hands
            float s = Mth.sin(this.attackTime * Mth.PI);
            rArm.xRot = -2.0F * s + 0.3F;
            lArm.xRot = -2.0F * s + 0.3F;
            rFore.xRot = -0.3F - 0.6F * s;
            lFore.xRot = -0.3F - 0.6F * s;
            jaw.xRot += 0.5F * s;
        }
    }
}
