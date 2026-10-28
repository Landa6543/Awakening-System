package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaPigEntity;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Modelo 3D exclusivo del Cerdo de ManÃ¡.
 * Cuenta con orejas tridimensionales expresivas, colmillos de cristal de manÃ¡
 * y una cola tridimensional curvada en la grupa, enriqueciendo su silueta celestial.
 */
@OnlyIn(Dist.CLIENT)
public class ManaPigModel extends QuadrupedModel<ManaPigEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "mana_pig"), "main");

    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart tail;

    public ManaPigModel(ModelPart root) {
        super(root, false, 4.0F, 4.0F, 2.0F, 2.0F, 24);
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = QuadrupedModel.createBodyMesh(6, CubeDeformation.NONE);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F)
                        .texOffs(22, 20).addBox(2.0F, 1.0F, -8.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(22, 20).mirror().addBox(-3.0F, 1.0F, -8.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, -6.0F)
        );

        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(0.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, -2.5F, -5.0F, 0.15F, 0.0F, -0.35F)
        );

        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(16, 20).mirror().addBox(-1.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, -2.5F, -5.0F, 0.15F, 0.F, 0.35F)
        );

        PartDefinition body = partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(28, 8).addBox(-5.0f, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );

        body.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(16, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                        .texOffs(20, 25).addBox(-0.5F, 2.0F, 0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -1.0F, 0.65F, 0.F, 0.0F)
        );

        CubeListBuilder hindLegBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F);
        partdefinition.addOrReplaceChild(
                "right_hind_leg",
                hindLegBuilder,
                PartPose.offset(-3.0F, 18.0F, 5.0F)
        );
        partdefinition.addOrReplaceChild(
                "left_hind_leg",
                hindLegBuilder,
                PartPose.offset(3.0F, 18.0F, 5.0F)
        );

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(ManaPigEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float earFlutter = Mth.sin(ageInTicks * 0.2F) * 0.08F;
        this.leftEar.zRot = -0.35F - earFlutter;
        this.rightEar.zRot = 0.35F + earFlutter;

        float tailWag = Mth.sin(ageInTicks * 0.35F + limbSwing * 1.2F) * (0.12F + limbSwingAmount * 0.30F);
        this.tail.yRot = tailWag;
    }
}
