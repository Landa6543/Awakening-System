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
import net.minecraft.world.phys.Vec3;

/**
 * Modelo 3D Autentico y Canonico del Slime Pequeno de Genshin Impact.
 * Creado a partir del modelo geometrico original de 26 cajas con mapeo UV canónico de 64x64.
 * Incluye fisica elastica squash & stretch identica a la del slime Isekai y accesorios elementales de Teyvat.
 */
public class GenshinSlimeModel extends HierarchicalModel<GenshinSlimeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "genshin_slime"), "main");

    private final ModelPart root;
    private final ModelPart bb_main;

    private final ModelPart anemoWings;
    private final ModelPart cryoSpikes;
    private final ModelPart electroAntenna;
    private final ModelPart geoHorns;
    private final ModelPart dendroLeaves;

    public GenshinSlimeModel(ModelPart root) {
        this.root = root;
        this.bb_main = root.getChild("bb_main");

        this.anemoWings = this.bb_main.getChild("anemo_wings");
        this.cryoSpikes = this.bb_main.getChild("cryo_spikes");
        this.electroAntenna = this.bb_main.getChild("electro_antenna");
        this.geoHorns = this.bb_main.getChild("geo_horns");
        this.dendroLeaves = this.bb_main.getChild("dendro_leaves");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Contenedor principal con base anclada al nivel del suelo (Y = 24.0)
        PartDefinition bb_main = root.addOrReplaceChild("bb_main", CubeListBuilder.create()
                // 26 Cajas base del dumpling/mochi redondo original de Genshin Impact (UV 64x64)
                .texOffs(0, 55).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(4.0F, -2.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(5.0F, -3.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-5.0F, -2.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-6.0F, -3.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(-7.0F, -7.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).mirror().addBox(6.0F, -7.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).mirror().addBox(-6.0F, -8.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).mirror().addBox(-5.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).mirror().addBox(-4.0F, -10.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(5.0F, -8.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(4.0F, -9.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 0).addBox(3.0F, -10.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(-3.0F, -11.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                // Rostro frontal con ojos canónicos de Genshin (UV 0, 0)
                .texOffs(0, 0).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(-5.0F, -8.0F, -5.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(-6.0F, -7.0F, -5.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(4.0F, -8.0F, -5.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(5.0F, -7.0F, -5.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-3.0F, -10.0F, -5.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                // Dorso y laterales traseros
                .texOffs(0, 58).addBox(5.0F, -7.0F, 4.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(4.0F, -8.0F, 4.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 57).addBox(-3.0F, -10.0F, 4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 54).addBox(-4.0F, -9.0F, 4.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-5.0F, -8.0F, 4.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 58).addBox(-6.0F, -7.0F, 4.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 1. Alas Anemo (Anemo Wings)
        PartDefinition anemoWings = bb_main.addOrReplaceChild("anemo_wings", CubeListBuilder.create()
                .texOffs(0, 29).mirror().addBox(-9.0F, -13.0F, -1.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 31).addBox(5.0F, -11.0F, -1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(5.0F, -12.0F, -1.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(5.0F, -13.0F, -1.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 28).addBox(-10.0F, -13.0F, -1.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(33, 41).addBox(-9.0F, -12.0F, -1.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-8.0F, -11.0F, -1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 2. Puas de Hielo Cryo (Cryo Spikes)
        bb_main.addOrReplaceChild("cryo_spikes", CubeListBuilder.create()
                .texOffs(56, 30).addBox(-1.0F, -10.0F, -6.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(56, 30).addBox(7.0F, -7.0F, -4.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 30).addBox(7.0F, -6.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 30).addBox(7.0F, -7.0F, 2.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 30).addBox(-8.0F, -7.0F, -4.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 30).addBox(-8.0F, -6.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 30).addBox(-8.0F, -7.0F, 2.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 3. Antena Electro (Electro Antenna)
        bb_main.addOrReplaceChild("electro_antenna", CubeListBuilder.create()
                .texOffs(29, 33).addBox(-1.0F, -7.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-0.5F, -5.5F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -10.5F, -0.5F, -0.4363F, 0.0F, 0.0F));

        // 4. Cuernos Geo (Geo Horns)
        PartDefinition geoHorns = bb_main.addOrReplaceChild("geo_horns", CubeListBuilder.create(), PartPose.ZERO);
        geoHorns.addOrReplaceChild("horn1", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-5.0F, -5.5F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -10.0F, -3.0F, 0.0F, 0.0F, -0.2618F));
        geoHorns.addOrReplaceChild("horn2", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-0.5F, -4.0F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -10.0F, -3.0F, 0.0F, 0.0F, 0.2618F));
        geoHorns.addOrReplaceChild("horn3", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-5.5F, -6.5F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -10.0F, -3.0F, 0.0F, 0.0F, -0.4800F));
        geoHorns.addOrReplaceChild("horn4", CubeListBuilder.create()
                .texOffs(0, 26).addBox(0.0F, -5.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -10.0F, -3.0F, 0.0F, 0.0F, 0.4800F));

        // 5. Brote Dendro (Dendro Foliage)
        PartDefinition dendroLeaves = bb_main.addOrReplaceChild("dendro_leaves", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition stem1 = dendroLeaves.addOrReplaceChild("stem1", CubeListBuilder.create()
                .texOffs(15, 27).addBox(-0.5F, -2.0937F, -0.0774F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -10.9063F, -2.9226F, 0.3771F, 0.5603F, 0.0662F));
        stem1.addOrReplaceChild("leaf1", CubeListBuilder.create()
                .texOffs(26, 34).addBox(-1.5F, -3.2352F, -2.9643F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -7.5315F, -1.1131F, 1.1781F, 0.0F, 0.0F));
        stem1.addOrReplaceChild("leaf2", CubeListBuilder.create()
                .texOffs(19, 29).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0937F, 0.4226F, 0.4363F, 0.0F, 0.0F));

        PartDefinition stem2 = dendroLeaves.addOrReplaceChild("stem2", CubeListBuilder.create()
                .texOffs(15, 27).addBox(-0.5F, -2.0937F, -0.0774F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.5F, -10.9063F, -2.9226F, 0.3064F, -0.6225F, 0.0005F));
        stem2.addOrReplaceChild("leaf3", CubeListBuilder.create()
                .texOffs(26, 34).addBox(-1.5F, -3.2352F, -2.9643F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -7.5315F, -1.1131F, 1.1781F, 0.0F, 0.0F));
        stem2.addOrReplaceChild("leaf4", CubeListBuilder.create()
                .texOffs(19, 29).addBox(-0.5F, -1.0F, -1.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0937F, 0.4226F, 0.4363F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
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
        this.electroAntenna.resetPose();
        this.dendroLeaves.resetPose();

        // Control de visibilidad elemental
        GenshinSlimeEntity.Element el = entity.getElement();
        this.anemoWings.visible = (el == GenshinSlimeEntity.Element.ANEMO);
        this.cryoSpikes.visible = (el == GenshinSlimeEntity.Element.CRYO);
        this.electroAntenna.visible = (el == GenshinSlimeEntity.Element.ELECTRO);
        this.geoHorns.visible = (el == GenshinSlimeEntity.Element.GEO);
        this.dendroLeaves.visible = (el == GenshinSlimeEntity.Element.DENDRO);

        // Interpolación fluida de squash & stretch idéntica a la del slime Isekai
        float partialTick = ageInTicks - (float) entity.tickCount;
        float squishFactor = Mth.lerp(partialTick, entity.prevSquish, entity.squish);

        // Respiración suave en reposo
        float breathe = 0.0F;
        if (Math.abs(squishFactor) < 0.25F) {
            breathe = Mth.sin(ageInTicks * 0.12F) * 0.035F;
        }

        // Base siempre anclada al suelo (Y = 24.0)
        this.bb_main.y = 24.0F;

        if (squishFactor < 0.0F) {
            // SQUASH: Impacto potente contra el piso (se aplasta y ensancha ampliamente)
            float s = -squishFactor; // 0.0 a 1.45
            this.bb_main.yScale = Math.max(0.30F, 1.0F - s * 0.45F) + breathe;
            this.bb_main.xScale = 1.0F + s * 0.40F - breathe * 0.5F;
            this.bb_main.zScale = 1.0F + s * 0.40F - breathe * 0.5F;
        } else {
            // STRETCH: Salto parabólico en el aire (se estira esbelto hacia arriba)
            float t = squishFactor; // 0.0 a 1.0
            this.bb_main.yScale = 1.0F + t * 0.55F + breathe;
            this.bb_main.xScale = Math.max(0.60F, 1.0F - t * 0.30F) - breathe * 0.5F;
            this.bb_main.zScale = Math.max(0.60F, 1.0F - t * 0.30F) - breathe * 0.5F;
        }

        // Balanceo secundario elemental
        if (el == GenshinSlimeEntity.Element.ANEMO) {
            float flapSpeed = entity.onGround() ? 0.45F : 0.90F;
            float wingFlap = Mth.sin(ageInTicks * flapSpeed) * 0.45F;
            this.anemoWings.zRot = wingFlap;
        } else if (el == GenshinSlimeEntity.Element.ELECTRO) {
            this.electroAntenna.zRot = (Mth.sin(ageInTicks * 0.6F) > 0.7F) ? 0.08F : -0.08F;
        } else if (el == GenshinSlimeEntity.Element.DENDRO) {
            this.dendroLeaves.zRot = Mth.sin(ageInTicks * 0.15F) * 0.08F;
        }
    }
}

