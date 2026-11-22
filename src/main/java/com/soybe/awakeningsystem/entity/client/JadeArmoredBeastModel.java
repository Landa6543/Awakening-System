package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.JadeArmoredBeastEntity;
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
 * The Spirit Jade Armored Beast, made anew: a heavy qilin-like beast on four legs, its back covered by four
 * overlapping plates of jade with a ridge of spikes, a horned head with a pale mane and a beard, a jaw that
 * snaps, and a segmented tail ending in a spiked club. The texture (and its glow: eyes, the plates' veins and
 * the spikes) is laid out for exactly these boxes.
 */
public class JadeArmoredBeastModel extends HierarchicalModel<JadeArmoredBeastEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "jade_armored_beast"), "main");

    private final ModelPart root, body, head, jaw, tail, tailMid, tailClub;
    private final ModelPart[] plates = new ModelPart[4];
    private final ModelPart rightFrontLeg, leftFrontLeg, rightHindLeg, leftHindLeg;

    public JadeArmoredBeastModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.tail = body.getChild("tail");
        this.tailMid = tail.getChild("tail_mid");
        this.tailClub = tailMid.getChild("tail_club");
        for (int i = 0; i < 4; i++) plates[i] = body.getChild("plate" + i);
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -5.0F, -10.0F, 12.0F, 10.0F, 20.0F)     // the trunk
                        .texOffs(0, 30).addBox(-5.0F, 4.0F, -8.0F, 10.0F, 2.0F, 16.0F),      // the pale belly plates
                PartPose.offset(0.0F, 11.0F, 0.0F));

        // four plates of jade over the back, each a little raised at its rear edge like scales, with a spike
        for (int i = 0; i < 4; i++) {
            body.addOrReplaceChild("plate" + i, CubeListBuilder.create()
                            .texOffs(64, i * 8).addBox(-7.0F, -3.0F, -2.5F, 14.0F, 3.0F, 5.0F)
                            .texOffs(104, 0).addBox(-1.0F, -5.5F, -1.0F, 2.0F, 3.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, -4.0F, -7.0F + i * 4.8F, -0.12F, 0.0F, 0.0F));
        }

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 7.0F, 6.0F)         // the skull
                        .texOffs(28, 48).addBox(-3.0F, -1.0F, -11.0F, 6.0F, 4.0F, 5.0F)       // the snout
                        .texOffs(52, 48).addBox(-5.0F, -5.0F, -1.0F, 10.0F, 8.0F, 3.0F)       // the mane
                        .texOffs(104, 16).addBox(-1.5F, 3.0F, -5.0F, 3.0F, 3.0F, 2.0F),       // the beard
                PartPose.offset(0.0F, -2.0F, -10.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(28, 57).addBox(-3.0F, 0.0F, -5.0F, 6.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 3.0F, -6.0F));
        // the horns sweep back and up, each with a tip turned higher
        for (int s = -1; s <= 1; s += 2) {
            PartDefinition horn = head.addOrReplaceChild(s < 0 ? "horn_r" : "horn_l", CubeListBuilder.create()
                            .texOffs(104, 8).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                    PartPose.offsetAndRotation(s * 2.8F, -3.5F, -3.0F, -0.55F, 0.0F, s * 0.25F));
            horn.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(112, 8).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.6F, 0.0F, 0.0F));
        }

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 64).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 9.5F, -0.25F, 0.0F, 0.0F));
        PartDefinition mid = tail.addOrReplaceChild("tail_mid", CubeListBuilder.create().texOffs(22, 64).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.5F, 0.2F, 0.0F, 0.0F));
        mid.addOrReplaceChild("tail_club", CubeListBuilder.create()
                        .texOffs(42, 64).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 3.0F)
                        .texOffs(104, 0).addBox(-1.0F, -5.0F, 0.5F, 2.0F, 3.0F, 2.0F)
                        .texOffs(104, 0).addBox(-5.0F, -1.0F, 0.5F, 2.0F, 2.0F, 2.0F)
                        .texOffs(104, 0).addBox(3.0F, -1.0F, 0.5F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 5.5F));

        leg(root, "right_front_leg", -4.5F, -6.0F, false);
        leg(root, "left_front_leg", 4.5F, -6.0F, true);
        leg(root, "right_hind_leg", -4.5F, 7.0F, false);
        leg(root, "left_hind_leg", 4.5F, 7.0F, true);
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** A thick leg: a plated thigh, a shin, a broad paw with three dark claws. */
    private static void leg(PartDefinition root, String name, float x, float z, boolean mirror) {
        PartDefinition upper = root.addOrReplaceChild(name, CubeListBuilder.create().mirror(mirror)
                        .texOffs(60, 64).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(x, 15.0F, z));
        upper.addOrReplaceChild("lower", CubeListBuilder.create().mirror(mirror)
                        .texOffs(80, 64).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                        .texOffs(96, 64).addBox(-2.5F, 3.0F, -3.0F, 5.0F, 2.0F, 5.0F)
                        .texOffs(116, 64).addBox(-2.0F, 4.0F, -4.5F, 1.0F, 1.0F, 2.0F)
                        .texOffs(116, 64).addBox(-0.5F, 4.0F, -4.5F, 1.0F, 1.0F, 2.0F)
                        .texOffs(116, 64).addBox(1.0F, 4.0F, -4.5F, 1.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(JadeArmoredBeastEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        // the head looks about; the jaw hangs a little open, and snaps on an attack
        this.head.yRot = netHeadYaw * (Mth.PI / 180F) * 0.8F;
        this.head.xRot = headPitch * (Mth.PI / 180F) * 0.6F + Mth.sin(ageInTicks * 0.08F) * 0.04F;
        this.jaw.xRot = 0.08F + Mth.sin(ageInTicks * 0.1F) * 0.03F;
        if (this.attackTime > 0.0F) {
            float bite = Mth.sin(this.attackTime * Mth.PI);
            this.jaw.xRot += 0.7F * bite;
            this.head.xRot -= 0.35F * bite;
            this.body.xRot = 0.08F * bite;
        }
        // a heavy, rolling walk: diagonal legs together, the body swaying
        float walk = limbSwing * 0.55F;
        float amount = Math.min(1.0F, limbSwingAmount);
        this.rightFrontLeg.xRot = Mth.cos(walk) * 0.8F * amount;
        this.leftHindLeg.xRot = Mth.cos(walk) * 0.8F * amount;
        this.leftFrontLeg.xRot = Mth.cos(walk + Mth.PI) * 0.8F * amount;
        this.rightHindLeg.xRot = Mth.cos(walk + Mth.PI) * 0.8F * amount;
        this.body.zRot = Mth.cos(walk) * 0.05F * amount;
        this.body.y += Math.abs(Mth.sin(walk)) * -0.6F * amount;
        // the plates rise and settle as it breathes
        for (int i = 0; i < 4; i++) this.plates[i].xRot = -0.12F - Mth.sin(ageInTicks * 0.09F + i * 0.6F) * 0.04F;
        // the tail swings behind it, the club last
        this.tail.yRot = Mth.cos(ageInTicks * 0.12F) * 0.25F + Mth.cos(walk) * 0.15F * amount;
        this.tailMid.yRot = Mth.cos(ageInTicks * 0.12F - 0.6F) * 0.2F;
        this.tailClub.yRot = Mth.cos(ageInTicks * 0.12F - 1.2F) * 0.2F;
    }
}
