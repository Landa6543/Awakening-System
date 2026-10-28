package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.GenshinSlimeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D Autentico y Canonico del Slime Grande de Genshin Impact.
 * Creado a partir del modelo geometrico original de 26 cajas grandes con mapeo UV canónico de 128x128.
 * Incluye fisica elastica squash & stretch identica a la del slime Isekai y accesorios elementales de Teyvat.
 */
public class GenshinLargeSlimeModel extends HierarchicalModel<GenshinSlimeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_large_slime"), "main");

    private final ModelPart root;
    private final ModelPart bb_main;

    private final ModelPart anemoWings;
    private final ModelPart pyroFlame;
    private final ModelPart cryoHelmet;
    private final ModelPart electroAntenna;
    private final ModelPart geoHelmet;
    private final ModelPart dendroLeaves;

    public GenshinLargeSlimeModel(ModelPart root) {
        this.root = root;
        this.bb_main = root.getChild("bb_main");

        this.anemoWings = this.bb_main.getChild("anemo_wings");
        this.pyroFlame = this.bb_main.getChild("pyro_flame");
        this.cryoHelmet = this.bb_main.getChild("cryo_helmet");
        this.electroAntenna = this.bb_main.getChild("electro_antenna");
        this.geoHelmet = this.bb_main.getChild("geo_helmet");
        this.dendroLeaves = this.bb_main.getChild("dendro_leaves");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bb_main = root.addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(0, 110).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(7, 110).addBox(-6.0F, -22.0F, -8.0F, 12.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).addBox(8.0F, -4.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).addBox(10.0F, -6.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).addBox(10.0F, -16.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).addBox(8.0F, -18.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).addBox(6.0F, -20.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(72, 0).addBox(-12.0F, -6.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).mirror().addBox(-12.0F, -16.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).mirror().addBox(-10.0F, -18.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).mirror().addBox(-8.0F, -20.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).addBox(-14.0F, -14.0F, -8.0F, 2.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(92, 0).mirror().addBox(12.0F, -14.0F, -8.0F, 2.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(73, 0).addBox(-10.0F, -4.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -18.0F, -10.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 113).addBox(-6.0F, -20.0F, -10.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(120, 114).addBox(-10.0F, -16.0F, -10.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(120, 114).addBox(8.0F, -16.0F, -10.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(120, 118).addBox(-12.0F, -14.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(120, 118).addBox(10.0F, -14.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 118).addBox(-12.0F, -14.0F, 8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 114).addBox(-10.0F, -16.0F, 8.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 110).addBox(-8.0F, -18.0F, 8.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(19, 114).addBox(-6.0F, -20.0F, 8.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 114).addBox(8.0F, -16.0F, 8.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 118).addBox(10.0F, -14.0F, 8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bb_main.addOrReplaceChild("anemo_wings", CubeListBuilder.create()
                .texOffs(0, 62).addBox(8.0F, -20.0F, -3.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(8.0F, -22.0F, -3.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 58).addBox(8.0F, -24.0F, -3.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 56).addBox(-18.0F, -24.0F, -3.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 82).addBox(-16.0F, -22.0F, -3.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).addBox(-14.0F, -20.0F, -3.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        bb_main.addOrReplaceChild("pyro_flame", CubeListBuilder.create()
                .texOffs(22, 45).addBox(-1.5F, -30.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(22, 45).addBox(-1.5F, -26.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition cryoHelmet = bb_main.addOrReplaceChild("cryo_helmet", CubeListBuilder.create()
                .texOffs(0, 49).addBox(14.0F, -23.0F, -11.0F, 2.0F, 10.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-16.0F, -23.0F, -11.0F, 2.0F, 10.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-15.0F, -23.0F, 9.0F, 30.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-14.0F, -26.0F, -10.0F, 28.0F, 3.0F, 19.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(2.0F, -23.0F, -12.0F, 13.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-1.0F, -23.0F, -12.0F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 49).addBox(-15.0F, -23.0F, -12.0F, 14.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        cryoHelmet.addOrReplaceChild("crystal1", CubeListBuilder.create()
                .texOffs(36, 51).addBox(-3.0F, -14.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -25.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("electro_antenna", CubeListBuilder.create()
                .texOffs(60, 66).addBox(-1.0F, -16.0F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 66).addBox(-2.0F, -15.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 114).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -21.0F, -1.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition geoHelmet = bb_main.addOrReplaceChild("geo_helmet", CubeListBuilder.create()
                .texOffs(0, 43).addBox(14.0F, -23.0F, -11.0F, 2.0F, 10.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-16.0F, -23.0F, -11.0F, 2.0F, 10.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-15.0F, -23.0F, 9.0F, 30.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-14.0F, -26.0F, -10.0F, 28.0F, 3.0F, 19.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(2.0F, -23.0F, -12.0F, 13.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-1.0F, -23.0F, -12.0F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-15.0F, -23.0F, -12.0F, 14.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        geoHelmet.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(60, 70).addBox(-12.0F, -14.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, 0.0F, 0.0F, -0.2618F));
        geoHelmet.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(60, 70).mirror().addBox(6.0F, -14.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition dendroLeaves = bb_main.addOrReplaceChild("dendro_leaves", CubeListBuilder.create(), PartPose.ZERO);
        dendroLeaves.addOrReplaceChild("flower1", CubeListBuilder.create()
                .texOffs(88, 39).addBox(-3.0F, -11.5F, -1.5F, 6.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, 0.3F, 0.0F, 0.0F));
        dendroLeaves.addOrReplaceChild("flower2", CubeListBuilder.create()
                .texOffs(59, 48).addBox(-2.5F, -10.0F, -1.5F, 6.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.3F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(GenshinSlimeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.bb_main.resetPose();
        this.anemoWings.resetPose();
        this.pyroFlame.resetPose();
        this.electroAntenna.resetPose();
        this.dendroLeaves.resetPose();

        GenshinSlimeEntity.Element el = entity.getElement();
        this.anemoWings.visible = (el == GenshinSlimeEntity.Element.ANEMO);
        this.pyroFlame.visible = (el == GenshinSlimeEntity.Element.PYRO);
        this.cryoHelmet.visible = (el == GenshinSlimeEntity.Element.CRYO);
        this.electroAntenna.visible = (el == GenshinSlimeEntity.Element.ELECTRO);
        this.geoHelmet.visible = (el == GenshinSlimeEntity.Element.GEO);
        this.dendroLeaves.visible = (el == GenshinSlimeEntity.Element.DENDRO);

        float partialTick = ageInTicks - (float) entity.tickCount;
        float squishFactor = Mth.lerp(partialTick, entity.prevSquish, entity.squish);

        float breathe = 0.0F;
        if (Math.abs(squishFactor) < 0.25F) {
            breathe = Mth.sin(ageInTicks * 0.12F) * 0.035F;
        }

        this.bb_main.y = 24.0F;

        if (squishFactor < 0.0F) {
            float s = -squishFactor;
            this.bb_main.yScale = Math.max(0.30F, 1.0F - s * 0.45F) + breathe;
            this.bb_main.xScale = 1.0F + s * 0.40F - breathe * 0.5F;
            this.bb_main.zScale = 1.0F + s * 0.40F - breathe * 0.5F;
        } else {
            float t = squishFactor;
            this.bb_main.yScale = 1.0F + t * 0.55F + breathe;
            this.bb_main.xScale = Math.max(0.60F, 1.0F - t * 0.30F) - breathe * 0.5F;
            this.bb_main.zScale = Math.max(0.60F, 1.0F - t * 0.30F) - breathe * 0.5F;
        }

        if (el == GenshinSlimeEntity.Element.ANEMO) {
            float flapSpeed = entity.onGround() ? 0.40F : 0.85F;
            float wingFlap = Mth.sin(ageInTicks * flapSpeed) * 0.40F;
            this.anemoWings.zRot = wingFlap;
        } else if (el == GenshinSlimeEntity.Element.PYRO) {
            this.pyroFlame.yRot = ageInTicks * 0.08F;
        } else if (el == GenshinSlimeEntity.Element.ELECTRO) {
            this.electroAntenna.zRot = (Mth.sin(ageInTicks * 0.5F) > 0.7F) ? 0.08F : -0.08F;
        } else if (el == GenshinSlimeEntity.Element.DENDRO) {
            this.dendroLeaves.zRot = Mth.sin(ageInTicks * 0.12F) * 0.06F;
        }
    }
}

