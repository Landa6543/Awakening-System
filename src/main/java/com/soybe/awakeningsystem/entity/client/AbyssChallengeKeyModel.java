package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssChallengeKeyEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The challenge key of the "defeat the enemies" chambers, in the Spiral Abyss style: a floating red staff with a
 * frame round its middle, a horned head whose horns sweep up and curl in, and a blue wire cube turning over it.
 * Built by our own generator (MakeAbyssPieces).
 */
public class AbyssChallengeKeyModel extends HierarchicalModel<AbyssChallengeKeyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_challenge_key"), "main");

    private final ModelPart root;
    private final ModelPart staff;
    private final ModelPart cube;

    public AbyssChallengeKeyModel(ModelPart root) {
        this.root = root;
        this.staff = root.getChild("staff");
        this.cube = root.getChild("cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition staff = root.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -28F, -1F, 2F, 22F, 2F)
                .texOffs(0, 24).addBox(-1.5F, -7F, -1.5F, 3F, 2F, 3F)
                .texOffs(8, 0).addBox(-4F, -17F, -1F, 1F, 8F, 2F)
                .texOffs(8, 0).addBox(3F, -17F, -1F, 1F, 8F, 2F)
                .texOffs(0, 29).addBox(-4F, -18F, -1F, 8F, 1F, 2F)
                .texOffs(0, 29).addBox(-4F, -10F, -1F, 8F, 1F, 2F)
                .texOffs(22, 0).addBox(-2F, -33F, -1F, 4F, 5F, 2F)
                .texOffs(12, 24).addBox(-1.5F, -36F, -1F, 3F, 3F, 2F)
                .texOffs(22, 24).addBox(-1F, -32F, -1.5F, 2F, 2F, 3F)
                .texOffs(52, 0).addBox(-0.5F, -41F, -0.5F, 1F, 5F, 1F),
                PartPose.offset(0F, 24F, 0F));

        for (int sgn : new int[]{-1, 1}) {
            String side = sgn < 0 ? "_left" : "_right";
            PartDefinition horn = staff.addOrReplaceChild("horn" + side, CubeListBuilder.create()
                    .texOffs(32, 24).addBox(sgn < 0 ? -9F : 0F, -1.5F, -1F, 9F, 3F, 2F),
                    PartPose.offsetAndRotation(sgn * 1.5F, -30F, 0F, 0F, 0F, sgn * -0.6981317F));
            PartDefinition tip = horn.addOrReplaceChild("tip" + side, CubeListBuilder.create()
                    .texOffs(14, 0).addBox(-1F, -6F, -1F, 2F, 6F, 2F),
                    PartPose.offsetAndRotation(sgn * 8.5F, -0.5F, 0F, 0F, 0F, sgn * 0.08726646F));
            tip.addOrReplaceChild("hook" + side, CubeListBuilder.create()
                    .texOffs(54, 24).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                    PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, sgn * -0.7853982F));
            staff.addOrReplaceChild("inner" + side, CubeListBuilder.create()
                    .texOffs(20, 29).addBox(sgn < 0 ? -6F : 0F, -1F, -0.5F, 6F, 2F, 1F),
                    PartPose.offsetAndRotation(sgn * 1.5F, -28F, 0F, 0F, 0F, sgn * -0.34906584F));
            staff.addOrReplaceChild("curl" + side, CubeListBuilder.create()
                    .texOffs(34, 29).addBox(sgn < 0 ? -4F : 0F, 0F, -0.5F, 4F, 1F, 1F),
                    PartPose.offsetAndRotation(sgn * 2F, -28F, 0F, 0F, 0F, sgn * 0.61086524F));
        }

        root.addOrReplaceChild("cube", CubeListBuilder.create()
                .texOffs(44, 29).addBox(-4F, -4F, -4F, 8F, 1F, 1F)
                .texOffs(44, 29).addBox(-4F, -4F, 3F, 8F, 1F, 1F)
                .texOffs(44, 29).addBox(-4F, 3F, -4F, 8F, 1F, 1F)
                .texOffs(44, 29).addBox(-4F, 3F, 3F, 8F, 1F, 1F)
                .texOffs(34, 0).addBox(-4F, -3F, -4F, 1F, 6F, 1F)
                .texOffs(34, 0).addBox(-4F, -3F, 3F, 1F, 6F, 1F)
                .texOffs(34, 0).addBox(3F, -3F, -4F, 1F, 6F, 1F)
                .texOffs(34, 0).addBox(3F, -3F, 3F, 1F, 6F, 1F)
                .texOffs(38, 0).addBox(-4F, -4F, -3F, 1F, 1F, 6F)
                .texOffs(38, 0).addBox(-4F, 3F, -3F, 1F, 1F, 6F)
                .texOffs(38, 0).addBox(3F, -4F, -3F, 1F, 1F, 6F)
                .texOffs(38, 0).addBox(3F, 3F, -3F, 1F, 1F, 6F),
                PartPose.offset(0F, -22F, 0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(AbyssChallengeKeyEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float hover = Mth.sin(ageInTicks * 0.08F) * 1.2F;
        this.staff.y = 24.0F + hover;
        // the cube tumbles slowly over the head
        this.cube.y = -22.0F + hover;
        this.cube.yRot = ageInTicks * 0.04F;
        this.cube.xRot = 0.6F + Mth.sin(ageInTicks * 0.03F) * 0.25F;
        this.cube.zRot = 0.6F;
    }
}
