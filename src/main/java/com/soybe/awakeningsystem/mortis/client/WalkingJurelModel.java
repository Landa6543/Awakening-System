package com.soybe.awakeningsystem.mortis.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.mortis.WalkingJurelEntity;
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

/** A horse mackerel standing on two thin human legs: long silver body, forked tail, dorsal and side fins. */
public class WalkingJurelModel extends HierarchicalModel<WalkingJurelEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "walking_jurel"), "main");

    private final ModelPart root, body, tail, rightFin, leftFin, rightLeg, leftLeg;

    public WalkingJurelModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.tail = body.getChild("tail");
        this.rightFin = body.getChild("right_fin");
        this.leftFin = body.getChild("left_fin");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // body: pivot under its middle, head to the front (-z)
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -7.0F, -8.0F, 6.0F, 7.0F, 16.0F)
                .texOffs(0, 23).addBox(-0.5F, -10.0F, -2.0F, 1.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-0.5F, -4.5F, 0.0F, 1.0F, 9.0F, 5.0F),
                PartPose.offset(0.0F, -3.5F, 8.0F));
        body.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(16, 23).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 2.0F, 4.0F),
                PartPose.offset(-3.0F, -2.5F, -3.0F));
        body.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(16, 23).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 4.0F),
                PartPose.offset(3.0F, -2.5F, -3.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(26, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(34, 23).addBox(-1.0F, 8.5F, -2.5F, 2.0F, 1.5F, 1.5F),
                PartPose.offset(-1.6F, 14.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(26, 23).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(34, 23).mirror().addBox(-1.0F, 8.5F, -2.5F, 2.0F, 1.5F, 1.5F),
                PartPose.offset(1.6F, 14.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 48);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(WalkingJurelEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float amt = Math.min(limbSwingAmount, 1.0F);
        float walk = limbSwing * 0.9F;
        // short fast steps, the whole fish waddling from side to side
        rightLeg.xRot = Mth.cos(walk) * 1.1F * amt;
        leftLeg.xRot = Mth.cos(walk + Mth.PI) * 1.1F * amt;
        body.zRot = Mth.sin(walk) * 0.12F * amt;
        body.y += Math.abs(Mth.sin(walk)) * -0.8F * amt;
        body.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.3F;
        // the tail never stops, the fins paddle
        tail.yRot = Mth.sin(ageInTicks * 0.35F) * 0.35F + Mth.sin(walk) * 0.3F * amt;
        rightFin.zRot = 0.35F + Mth.sin(ageInTicks * 0.3F) * 0.25F;
        leftFin.zRot = -0.35F - Mth.sin(ageInTicks * 0.3F) * 0.25F;

        if (entity.isDancing()) {
            // the ritual: high kicks, fins wide open, tail beating hard, body bouncing
            float d = ageInTicks * 0.6F;
            rightLeg.xRot = -Math.max(0, Mth.sin(d)) * 1.4F;
            leftLeg.xRot = -Math.max(0, Mth.sin(d + Mth.PI)) * 1.4F;
            rightLeg.zRot = 0.25F;
            leftLeg.zRot = -0.25F;
            body.xRot = Mth.sin(d * 2) * 0.15F;
            body.zRot = Mth.sin(d) * 0.25F;
            body.y -= Math.abs(Mth.sin(d)) * 1.2F;
            tail.yRot = Mth.sin(ageInTicks * 0.9F) * 0.6F;
            rightFin.zRot = 1.1F + Mth.sin(ageInTicks * 0.8F) * 0.4F;
            leftFin.zRot = -1.1F - Mth.sin(ageInTicks * 0.8F) * 0.4F;
        }
    }
}
