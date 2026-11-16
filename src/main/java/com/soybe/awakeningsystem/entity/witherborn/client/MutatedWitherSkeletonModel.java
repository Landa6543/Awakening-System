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
 * The Mutated Wither Skeleton, drawn from scratch: about three and a half blocks tall on long thin legs; a spine
 * rising from the pelvis into a chest torn open, five ribs a side arching outward and forward so the hollow shows,
 * and floating in it a cube of dark energy that turns and bobs; a wide collarbone with bone spikes on the
 * shoulders; long arms ending in claws; an elongated skull with a hanging jaw and two horns swept back.
 * When the core is about to burst, the ribs spread wider, the arms go up and the core swells.
 */
public class MutatedWitherSkeletonModel extends HierarchicalModel<MutatedWitherSkeletonEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mutated_wither_skeleton"), "main");

    private final ModelPart root, pelvis, spine, chest, core, neck, skull, jaw, rArm, lArm, rFore, lFore, rLeg, lLeg, rShin, lShin;
    private final List<ModelPart> ribs = new ArrayList<>(), ribTips = new ArrayList<>();

    public MutatedWitherSkeletonModel(ModelPart root) {
        this.root = root;
        this.pelvis = root.getChild("pelvis");
        this.spine = pelvis.getChild("spine");
        this.chest = spine.getChild("chest");
        this.core = chest.getChild("core");
        this.neck = chest.getChild("neck");
        this.skull = neck.getChild("skull");
        this.jaw = skull.getChild("jaw");
        this.rArm = chest.getChild("right_arm");
        this.lArm = chest.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rLeg = pelvis.getChild("right_leg");
        this.lLeg = pelvis.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        for (int i = 0; i < 5; i++)
            for (String s : new String[]{"right", "left"}) {
                ModelPart rib = chest.getChild(s + "_rib" + i);
                ribs.add(rib);
                ribTips.add(rib.getChild(s + "_rib_tip" + i));
            }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // hips at y=-2 (the feet stand on y=24, 26 pixels of leg below)
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(30, 0).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            boolean m = side > 0;
            PartDefinition leg = pelvis.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m).texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                    PartPose.offset(side * 3.0F, 0.0F, 0.0F));
            PartDefinition shin = leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m).texOffs(8, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                    PartPose.offset(0.0F, 13.0F, 0.0F));
            shin.addOrReplaceChild(s + "_foot", CubeListBuilder.create().mirror(m).texOffs(16, 0).addBox(-1.5F, 12.0F, -3.0F, 3.0F, 1.0F, 4.0F), PartPose.ZERO);
        }

        // the spine, leaning a little forward, and the chest on it
        PartDefinition spine = pelvis.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 20.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition chest = spine.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));
        for (int i = 0; i < 5; i++) {
            float y = -1.0F - i * 2.4F;
            float len = 4.0F + (i == 0 || i == 4 ? 0 : 1);
            for (int side = -1; side <= 1; side += 2) {
                String s = side < 0 ? "right" : "left";
                boolean m = side > 0;
                // from the spine out to the side, then curving forward and away: the chest stays open
                PartDefinition rib = chest.addOrReplaceChild(s + "_rib" + i, CubeListBuilder.create().mirror(m).texOffs(62, 0)
                                .addBox(m ? 0.0F : -len, -0.5F, -0.5F, len, 1.0F, 1.0F),
                        PartPose.offsetAndRotation(side * 1.0F, y, 0.5F, 0.0F, side * 0.25F, side * -0.12F));
                rib.addOrReplaceChild(s + "_rib_tip" + i, CubeListBuilder.create().mirror(m).texOffs(62, 2)
                                .addBox(m ? 0.0F : -4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F),
                        PartPose.offsetAndRotation(side * len, 0.0F, 0.0F, 0.0F, side * 1.15F, 0.0F));
            }
        }
        chest.addOrReplaceChild("core", CubeListBuilder.create().texOffs(74, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -6.0F, -3.0F));
        chest.addOrReplaceChild("collarbone", CubeListBuilder.create().texOffs(90, 0).addBox(-8.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));

        // neck and skull: long, the jaw hanging, two horns swept back
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(36, 6).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -13.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition skull = neck.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -8.0F, -5.5F, 8.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, -3.5F, 0.0F));
        skull.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(34, 24).addBox(-3.0F, 0.0F, -6.0F, 6.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 1.0F, 0.12F, 0.0F, 0.0F));
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "right" : "left";
            PartDefinition horn = skull.addOrReplaceChild(s + "_horn", CubeListBuilder.create().texOffs(58, 24).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 3.0F, -7.0F, -1.0F, -0.9F, 0.0F, side * 0.35F));
            horn.addOrReplaceChild(s + "_horn_tip", CubeListBuilder.create().texOffs(62, 24).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, -0.7F, 0.0F, 0.0F));

            // long arms: a spike on the shoulder, claws at the end
            PartDefinition arm = chest.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(side > 0).texOffs(16, 6).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 8.0F, -13.0F, 0.0F, 0.0F, 0.0F, side * -0.12F));
            arm.addOrReplaceChild(s + "_spike", CubeListBuilder.create().texOffs(66, 24).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offsetAndRotation(side * 0.6F, -0.5F, 0.0F, 0.0F, 0.0F, side * 0.5F));
            PartDefinition fore = arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(side > 0).texOffs(24, 6).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, -0.35F, 0.0F, 0.0F));
            CubeListBuilder hand = CubeListBuilder.create().texOffs(70, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F);
            for (int k = 0; k < 3; k++) hand.texOffs(32, 6).addBox(-1.2F + k * 0.9F, 1.6F, -1.2F, 1.0F, 4.0F, 1.0F);
            fore.addOrReplaceChild(s + "_hand", hand, PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -0.25F, 0.0F, 0.0F));
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
        float w = limbSwing * 0.55F;
        // a long, stiff stride
        rLeg.xRot = Mth.cos(w) * 0.7F * amt;
        lLeg.xRot = -Mth.cos(w) * 0.7F * amt;
        rShin.xRot = Math.max(0.0F, -Mth.sin(w)) * 0.6F * amt;
        lShin.xRot = Math.max(0.0F, Mth.sin(w)) * 0.6F * amt;
        rArm.xRot = -Mth.cos(w) * 0.5F * amt;
        lArm.xRot = Mth.cos(w) * 0.5F * amt;
        pelvis.y += Math.abs(Mth.sin(w)) * -0.8F * amt;
        skull.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        skull.xRot = headPitch * Mth.DEG_TO_RAD;
        jaw.xRot += 0.1F + Math.max(0.0F, Mth.sin(age * 0.07F)) * 0.15F;
        // the arms hang a little away and sway when idle
        rArm.zRot += Mth.sin(age * 0.05F) * 0.04F;
        lArm.zRot -= Mth.sin(age * 0.05F) * 0.04F;

        // the core turns and bobs, beating
        core.yRot = age * 0.08F;
        core.xRot = age * 0.05F;
        core.y += Mth.sin(age * 0.1F) * 0.6F;
        float beat = 1.0F + Math.max(0.0F, Mth.sin(age * 0.25F)) * 0.12F;
        int pulse = e == null ? 0 : e.pulse();
        if (pulse > 0) {
            float p = Math.min(1.0F, pulse / (float) MutatedWitherSkeletonEntity.PULSE_WINDUP);
            beat += p * 0.9F;
            rArm.xRot = -2.4F * p;
            lArm.xRot = -2.4F * p;
            rArm.zRot = 0.5F * p;
            lArm.zRot = -0.5F * p;
            skull.xRot -= 0.5F * p;
            jaw.xRot += 0.6F * p;
            for (int i = 0; i < ribs.size(); i++) {
                float side = (i % 2 == 0) ? -1 : 1;
                ribs.get(i).yRot += side * 0.35F * p;
            }
        }
        core.xScale = core.yScale = core.zScale = beat;

        if (this.attackTime > 0.0F) {
            // a two-handed downward rake
            float s = Mth.sin(this.attackTime * Mth.PI);
            rArm.xRot = -2.2F * s + 0.4F;
            lArm.xRot = -2.2F * s + 0.4F;
            rFore.xRot = -0.35F - 0.5F * s;
            lFore.xRot = -0.35F - 0.5F * s;
            jaw.xRot += 0.5F * s;
        }
    }
}
