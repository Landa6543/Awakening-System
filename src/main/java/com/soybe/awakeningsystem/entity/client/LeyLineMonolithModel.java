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
 * Modelo 3D Canónico del Monolito de las Líneas Ley de Genshin Impact.
 * Consta de un pedestal rúnico de piedra antigua, un obelisco levitante central,
 * anillos rúnicos exteriores flotantes que giran en direcciones opuestas y
 * fragmentos de cristal en la cúspide.
 */
public class LeyLineMonolithModel extends HierarchicalModel<LeyLineMonolithEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ley_line_monolith"), "main");

    private final ModelPart root;
    private final ModelPart pedestal;
    private final ModelPart obelisk;
    private final ModelPart core;
    private final ModelPart ringLower;
    private final ModelPart ringUpper;
    private final ModelPart crystals;

    public LeyLineMonolithModel(ModelPart root) {
        this.root = root;
        this.pedestal = root.getChild("pedestal");
        this.obelisk = root.getChild("obelisk");
        this.core = this.obelisk.getChild("core");
        this.ringLower = root.getChild("ring_lower");
        this.ringUpper = root.getChild("ring_upper");
        this.crystals = root.getChild("crystals");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Pedestal de piedra rúnica anclado al suelo (Y = 24.0)
        PartDefinition pedestal = root.addOrReplaceChild("pedestal", CubeListBuilder.create()
                // Plinto 1 (inferior amplio - 24x2x24)
                .texOffs(0, 0).addBox(-12.0F, -2.0F, -12.0F, 24.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
                // Plinto 2 (intermedio - 20x2x20)
                .texOffs(0, 28).addBox(-10.0F, -4.0F, -10.0F, 20.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
                // Plinto 3 (collar del pedestal - 14x3x14)
                .texOffs(0, 52).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                // Brackets rúnicos de las 4 esquinas
                .texOffs(0, 72).addBox(-9.0F, -6.0F, -9.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).addBox(6.0F, -6.0F, -9.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).addBox(-9.0F, -6.0F, 6.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).addBox(6.0F, -6.0F, 6.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 2. Obelisco levitante central (flota sobre el pedestal)
        PartDefinition obelisk = root.addOrReplaceChild("obelisk", CubeListBuilder.create()
                // Pilar inferior principal (10x16x10)
                .texOffs(58, 52).addBox(-5.0F, -16.0F, -5.0F, 10.0F, 16.0F, 10.0F, new CubeDeformation(0.0F))
                // Cúspide cónica escalonada (8x14x8)
                .texOffs(0, 80).addBox(-4.0F, -30.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                // Ápice superior (5x6x5)
                .texOffs(34, 80).addBox(-2.5F, -36.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                // Aletas/nervaduras verticales rúnicas (Norte/Sur: 2x20x14, Este/Oeste: 14x20x2)
                .texOffs(64, 0).addBox(-1.0F, -26.0F, -7.0F, 2.0F, 20.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(96, 0).addBox(-7.0F, -26.0F, -1.0F, 14.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        // Núcleo interno de energía de Líneas Ley
        obelisk.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(42, 94).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 3. Anillo rúnico inferior flotante (gira horariamente)
        root.addOrReplaceChild("ring_lower", CubeListBuilder.create()
                .texOffs(60, 94).addBox(-8.0F, -1.0F, -10.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 94).addBox(-8.0F, -1.0F, 8.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 100).addBox(-10.0F, -1.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(68, 100).addBox(8.0F, -1.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        // 4. Anillo rúnico superior flotante (gira antihorariamente e inclinado)
        root.addOrReplaceChild("ring_upper", CubeListBuilder.create()
                .texOffs(0, 106).addBox(-6.0F, -1.0F, -8.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 106).addBox(-6.0F, -1.0F, 6.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 112).addBox(-8.0F, -1.0F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 112).addBox(6.0F, -1.0F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        // 5. Fragmentos de cristal ley flotantes en la cúspide
        root.addOrReplaceChild("crystals", CubeListBuilder.create()
                .texOffs(26, 112).addBox(5.0F, -10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(26, 112).addBox(-7.0F, -10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(26, 112).addBox(0.0F, -10.0F, 5.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(26, 112).addBox(0.0F, -10.0F, -7.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -20.0F, 0.0F));

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

        // 1. Levitación sinusoidal suave del obelisco central
        float floatOffset = Mth.sin(ageInTicks * 0.05F) * 1.8F;
        this.obelisk.y = 8.0F + floatOffset;

        // 2. Rotación de los anillos rúnicos
        this.ringLower.yRot = ageInTicks * 0.035F;
        this.ringLower.y = 6.0F + floatOffset * 0.5F;

        this.ringUpper.yRot = -ageInTicks * 0.045F;
        this.ringUpper.xRot = 0.10F * Mth.sin(ageInTicks * 0.03F);
        this.ringUpper.y = -10.0F + floatOffset * 0.7F;

        // 3. Rotación del núcleo interno
        this.core.yRot = ageInTicks * 0.08F;

        // 4. Órbita de los cristales superiores
        this.crystals.yRot = ageInTicks * 0.05F;
        this.crystals.y = -20.0F + floatOffset + Mth.cos(ageInTicks * 0.07F) * 1.2F;
    }
}
