package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.ElementalDemonKingBossEntity;
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
 * The Elemental Demon King: obsidian armour veined with lava, a glowing heart-gem, a crown set with a gem
 * for each element, great curved horns, bat wings, a long cape and clawed gauntlets. Four elemental orbs
 * (fire, frost, earth, storm) circle him slowly.
 */
public class ElementalDemonKingModel extends HierarchicalModel<ElementalDemonKingBossEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "elemental_demon_king"), "main");

    private final ModelPart root, body, head, jaw, cape, rWing, lWing, rArm, lArm, rFore, lFore, rLeg, lLeg, rShin, lShin;
    private final ModelPart[] orbs = new ModelPart[4];

    public ElementalDemonKingModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.cape = body.getChild("cape");
        this.rWing = body.getChild("right_wing");
        this.lWing = body.getChild("left_wing");
        this.rArm = body.getChild("right_arm");
        this.lArm = body.getChild("left_arm");
        this.rFore = rArm.getChild("right_forearm");
        this.lFore = lArm.getChild("left_forearm");
        this.rLeg = root.getChild("right_leg");
        this.lLeg = root.getChild("left_leg");
        this.rShin = rLeg.getChild("right_shin");
        this.lShin = lLeg.getChild("left_shin");
        for (int i = 0; i < 4; i++) orbs[i] = root.getChild("orb_" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-4.5F, -5.0F, -2.5F, 9.0F, 6.0F, 5.0F)                 // waist
                .texOffs(0, 16).addBox(-6.5F, -16.0F, -3.5F, 13.0F, 11.0F, 7.0F)               // breastplate
                .texOffs(68, 16).addBox(-2.0F, -12.5F, -4.3F, 4.0F, 4.0F, 1.0F)                // heart-gem
                .texOffs(78, 16).addBox(-5.0F, 0.0F, -3.2F, 10.0F, 7.0F, 1.0F)                 // front tasset
                .texOffs(78, 16).addBox(-5.0F, 0.0F, 2.2F, 10.0F, 7.0F, 1.0F),                 // back tasset
                PartPose.offset(0.0F, 2.0F, 0.0F));
        body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(100, 16).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 26.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 3.6F, 0.08F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(52, 0).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 2.0F, 9.0F)                  // crown band
                .texOffs(88, 0).addBox(-0.5F, -11.0F, -5.0F, 1.0F, 2.0F, 1.0F)                 // four gem prongs
                .texOffs(88, 0).addBox(-0.5F, -11.0F, 4.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(88, 0).addBox(-5.0F, -11.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(88, 0).addBox(4.0F, -11.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -16.0F, -0.5F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, -1.0F, 0.2F));
        for (int side = -1; side <= 1; side += 2) {
            boolean m = side > 0;
            PartDefinition h1 = head.addOrReplaceChild(side < 0 ? "horn_right" : "horn_left", CubeListBuilder.create().mirror(m)
                    .texOffs(92, 0).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                    PartPose.offsetAndRotation(side * 3.6F, -6.5F, -0.5F, -0.2F, 0.0F, side * 1.0F));
            PartDefinition h2 = h1.addOrReplaceChild("mid", CubeListBuilder.create().mirror(m)
                    .texOffs(104, 0).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, -4.6F, 0.0F, -0.2F, 0.0F, -side * 0.75F));
            h2.addOrReplaceChild("tip", CubeListBuilder.create().mirror(m)
                    .texOffs(112, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -4.6F, 0.0F, -0.25F, 0.0F, -side * 0.6F));
        }

        for (int side = -1; side <= 1; side += 2) {
            boolean m = side > 0;
            String s = side < 0 ? "right" : "left";
            body.addOrReplaceChild(s + "_wing", CubeListBuilder.create().mirror(m)
                    .texOffs(0, 64).addBox(m ? 0.0F : -18.0F, -16.0F, 0.0F, 18.0F, 20.0F, 0.0F),
                    PartPose.offsetAndRotation(side * 2.5F, -13.0F, 3.8F, 0.0F, side * -0.5F, side * -0.15F));
            PartDefinition arm = body.addOrReplaceChild(s + "_arm", CubeListBuilder.create().mirror(m)
                    .texOffs(34, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                    .texOffs(0, 34).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 5.0F, 8.0F)               // pauldron
                    .texOffs(30, 34).addBox(-0.5F + side * 1.5F, -7.0F, -0.5F, 1.0F, 4.0F, 1.0F),   // its spike
                    PartPose.offsetAndRotation(side * 8.5F, -14.0F, 0.0F, 0.0F, 0.0F, side * 0.1F));
            arm.addOrReplaceChild(s + "_forearm", CubeListBuilder.create().mirror(m)
                    .texOffs(50, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                    .texOffs(66, 34).addBox(-2.5F, 7.5F, -2.5F, 5.0F, 3.0F, 5.0F),            // clawed gauntlet
                    PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.2F, 0.0F, 0.0F));

            PartDefinition leg = root.addOrReplaceChild(s + "_leg", CubeListBuilder.create().mirror(m)
                    .texOffs(0, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F),
                    PartPose.offset(side * 3.0F, 2.0F, 0.0F));
            leg.addOrReplaceChild(s + "_shin", CubeListBuilder.create().mirror(m)
                    .texOffs(16, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
                    .texOffs(32, 47).addBox(-2.5F, 0.0F, -2.7F, 5.0F, 7.0F, 1.0F)               // greave
                    .texOffs(44, 47).addBox(-2.5F, 10.0F, -4.0F, 5.0F, 3.0F, 6.0F),            // armoured boot
                    PartPose.offset(0.0F, 9.0F, 0.0F));
        }
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("orb_" + i, CubeListBuilder.create().texOffs(i * 12, 86).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                    PartPose.offset(0.0F, -8.0F, 0.0F));
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
        float w = limbSwing * 0.5F;
        float breath = Mth.sin(ageInTicks * 0.07F);

        // a heavy, deliberate stride
        rLeg.xRot = Mth.cos(w) * 0.7F * amt;
        lLeg.xRot = Mth.cos(w + Mth.PI) * 0.7F * amt;
        rShin.xRot = Math.max(0.0F, Mth.sin(w)) * 0.6F * amt;
        lShin.xRot = Math.max(0.0F, Mth.sin(w + Mth.PI)) * 0.6F * amt;
        body.y += -Math.abs(Mth.cos(w)) * 0.8F * amt + 0.8F * amt + breath * 0.3F * (1 - amt);
        body.yRot = Mth.cos(w) * 0.08F * amt;
        rArm.xRot = Mth.cos(w + Mth.PI) * 0.45F * amt;
        lArm.xRot = Mth.cos(w) * 0.45F * amt;
        rArm.zRot += 0.05F * breath;
        lArm.zRot -= 0.05F * breath;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD - body.yRot;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        jaw.xRot = Math.max(0.0F, breath) * 0.08F;

        // wings rise and fold slowly, the cape stirs
        float flap = Mth.sin(ageInTicks * 0.06F);
        rWing.yRot += flap * 0.18F;
        lWing.yRot -= flap * 0.18F;
        rWing.zRot += flap * 0.08F;
        lWing.zRot -= flap * 0.08F;
        cape.xRot += 0.06F * amt + Mth.sin(ageInTicks * 0.05F) * 0.04F + 0.25F * amt * Math.abs(Mth.sin(w));

        // four elemental orbs circling at chest height, each bobbing on its own
        for (int i = 0; i < 4; i++) {
            float a = ageInTicks * 0.045F + i * Mth.HALF_PI;
            orbs[i].x = Mth.cos(a) * 15.0F;
            orbs[i].z = Mth.sin(a) * 15.0F;
            orbs[i].y = -6.0F + Mth.sin(ageInTicks * 0.1F + i * 1.7F) * 2.0F + body.y - 2.0F;
            orbs[i].yRot = ageInTicks * 0.1F;
            orbs[i].xRot = ageInTicks * 0.07F;
        }

        if (this.attackTime > 0.0F) {
            // a raking claw: the right gauntlet sweeps across, the body twisting into it
            float p = this.attackTime;
            float wind = p < 0.3F ? p / 0.3F : 1.0F - (p - 0.3F) / 0.7F;
            float rake = p < 0.3F ? 0.0F : Mth.sin((p - 0.3F) / 0.7F * Mth.PI);
            rArm.xRot = -2.2F * wind - 1.2F * rake;
            rArm.yRot = 0.6F * rake;
            rFore.xRot = -0.8F * wind;
            body.yRot += 0.3F * wind - 0.45F * rake;
            jaw.xRot = 0.4F * Math.max(wind, rake);
        }
    }
}
