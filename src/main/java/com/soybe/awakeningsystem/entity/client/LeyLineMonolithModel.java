package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.LeyLineMonolithEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Ley Line Monolith of the defence chambers, in the Spiral Abyss style: a tapered teal crystal framed with
 * gold lines, on a stepped slate base ringed by jagged rocks. The gold lights up when the challenge starts
 * (see the glow layer in the renderer). Built by our own generator (MakeAbyssPieces).
 */
public class LeyLineMonolithModel extends HierarchicalModel<LeyLineMonolithEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ley_line_monolith"), "main");

    private final ModelPart root;
    private final ModelPart crystal;

    public LeyLineMonolithModel(ModelPart root) {
        this.root = root;
        this.crystal = root.getChild("crystal");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11F, -2F, -11F, 22F, 2F, 22F)
                .texOffs(0, 24).addBox(-9F, -4F, -9F, 18F, 2F, 18F)
                .texOffs(0, 61).addBox(-7F, -5F, -7F, 14F, 1F, 14F),
                PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
        // jagged rocks round the foot, leaning outwards
        float lean = -0.38397244F;
        float[][] big = {{-7F, -7F, 0.7853982F}, {7F, -7F, -0.7853982F}, {-7F, 7F, 2.3561945F}, {7F, 7F, -2.3561945F}};
        for (int i = 0; i < big.length; i++) {
            base.addOrReplaceChild("spike" + i, CubeListBuilder.create()
                    .texOffs(0, 76).addBox(-2.5F, -9F, -1F, 5F, 9F, 2F),
                    PartPose.offsetAndRotation(big[i][0], -4F, big[i][1], lean, big[i][2], 0F));
        }
        float[][] small = {{0F, -9F, 0F}, {0F, 9F, 3.1415927F}, {-9F, 0F, 1.5707964F}, {9F, 0F, -1.5707964F}};
        for (int i = 0; i < small.length; i++) {
            base.addOrReplaceChild("spike" + (i + 4), CubeListBuilder.create()
                    .texOffs(50, 76).addBox(-1.5F, -6F, -1F, 3F, 6F, 2F),
                    PartPose.offsetAndRotation(small[i][0], -4F, small[i][1], lean, small[i][2], 0F));
        }
        root.addOrReplaceChild("crystal", CubeListBuilder.create()
                .texOffs(72, 24).addBox(-6F, -6F, -6F, 12F, 6F, 12F)
                .texOffs(0, 44).addBox(-5.5F, -12F, -5.5F, 11F, 6F, 11F)
                .texOffs(44, 44).addBox(-5F, -18F, -5F, 10F, 6F, 10F)
                .texOffs(56, 61).addBox(-4.5F, -24F, -4.5F, 9F, 6F, 9F)
                .texOffs(92, 61).addBox(-4F, -28F, -4F, 8F, 4F, 8F)
                .texOffs(14, 76).addBox(-4.5F, -30F, -4.5F, 9F, 2F, 9F)
                .texOffs(60, 76).addBox(-2F, -32F, -2F, 4F, 2F, 4F),
                PartPose.offsetAndRotation(0F, 19F, 0F, 0F, 0F, 0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(LeyLineMonolithEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        // once awake, the crystal lifts a little off its base and breathes
        if (entity.isActive()) this.crystal.y = 18.4F + Mth.sin(ageInTicks * 0.06F) * 0.4F;
    }
}
