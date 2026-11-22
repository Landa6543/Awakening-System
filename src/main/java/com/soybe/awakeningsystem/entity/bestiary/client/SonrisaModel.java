package com.soybe.awakeningsystem.entity.bestiary.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.bestiary.SonrisaEntity;
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
 * Sonrisa after the user's concept art: a boulder made of four crossed blocks (rounded like a ball), a few
 * shards of crystal on top, the face carved on its front. Asleep its face has no mouth (a texture without one);
 * awake, the grin shows. It rolls forward as it moves, and attacks by rolling forward into its prey.
 */
public class SonrisaModel extends HierarchicalModel<SonrisaEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "sonrisa"), "main");

    private final ModelPart root, ball;

    public SonrisaModel(ModelPart root) {
        this.root = root;
        this.ball = root.getChild("ball");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("ball", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-11.0F, -11.0F, -11.0F, 22.0F, 22.0F, 22.0F)
                        .texOffs(88, 0).addBox(-13.0F, -8.0F, -8.0F, 26.0F, 16.0F, 16.0F)
                        .texOffs(172, 0).addBox(-8.0F, -13.0F, -8.0F, 16.0F, 26.0F, 16.0F)
                        .texOffs(0, 44).addBox(-8.0F, -8.0F, -13.0F, 16.0F, 16.0F, 26.0F)
                        .texOffs(120, 44).addBox(-6.0F, -15.0F, -2.0F, 2.0F, 4.0F, 2.0F)   // crystal shards on top
                        .texOffs(120, 44).addBox(3.0F, -14.5F, 1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(120, 44).addBox(-1.0F, -14.0F, 5.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(SonrisaEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float pt = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float m = e == null ? 0 : e.mouth(pt);
        // it rolls forward the way it moves (the top of the ball goes ahead of it)
        ball.xRot = limbSwing * 0.32F;
        float breath = 1.0F + Mth.sin(age * 0.08F) * 0.01F * m;
        ball.xScale = ball.yScale = ball.zScale = breath;
        ball.y += Mth.sin(age * 0.6F) * 0.3F * Math.min(1.0F, limbSwingAmount);
        // the attack: one whole roll forward into its prey, with a little hop at the top of the turn
        if (this.attackTime > 0.0F) {
            ball.xRot += this.attackTime * Mth.TWO_PI;
            ball.y -= Mth.sin(this.attackTime * Mth.PI) * 3.0F;
            ball.z -= Mth.sin(this.attackTime * Mth.PI) * 4.0F;
        }
    }
}
