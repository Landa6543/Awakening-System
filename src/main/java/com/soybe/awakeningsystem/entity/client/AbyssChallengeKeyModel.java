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
 * Modelo 3D Canónico de la Llave Roja de Desafío / Espada del Abismo de Genshin Impact.
 * Consta de un pedestal rúnico de obsidiana, una espada ceremonial carmesí levitante
 * y un dial orbital giratorio con nodos rúnicos cardinales.
 */
public class AbyssChallengeKeyModel extends HierarchicalModel<AbyssChallengeKeyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_challenge_key"), "main");

    private final ModelPart root;
    private final ModelPart pedestal;
    private final ModelPart sword;
    private final ModelPart dial;

    public AbyssChallengeKeyModel(ModelPart root) {
        this.root = root;
        this.pedestal = root.getChild("pedestal");
        this.sword = root.getChild("sword");
        this.dial = root.getChild("dial");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Pedestal rúnico de obsidiana anclado al suelo (Y = 24.0)
        PartDefinition pedestal = root.addOrReplaceChild("pedestal", CubeListBuilder.create()
                // Base disco (14x2x14)
                .texOffs(0, 0).addBox(-7.0F, -2.0F, -7.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                // Plinto collar (10x2x10)
                .texOffs(0, 18).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                // Zócalo central de inserción (6x1x6)
                .texOffs(0, 31).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 2. Espada Ceremonial Carmesí levitante
        PartDefinition sword = root.addOrReplaceChild("sword", CubeListBuilder.create()
                // Pomo de rubí superior (3x3x3)
                .texOffs(32, 18).addBox(-1.5F, -24.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                // Empuñadura / Hilt (2x6x2)
                .texOffs(46, 18).addBox(-1.0F, -21.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                // Gavilanes de la guardia alada (14x2x3)
                .texOffs(0, 40).addBox(-7.0F, -15.0F, -1.5F, 14.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                // Joya central del guardamanos (4x4x4)
                .texOffs(36, 40).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                // Base ancha de la hoja (6x6x2)
                .texOffs(0, 47).addBox(-3.0F, -13.0F, -1.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                // Cuerpo principal de la hoja carmesí (5x8x2)
                .texOffs(18, 47).addBox(-2.5F, -7.0F, -1.0F, 5.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                // Punta de la hoja apuntando hacia abajo (3x4x1)
                .texOffs(34, 47).addBox(-1.5F, 1.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        // 3. Dial orbital giratorio con nodos rúnicos cardinales
        PartDefinition dial = root.addOrReplaceChild("dial", CubeListBuilder.create()
                // Segmentos del anillo
                .texOffs(0, 57).addBox(-6.0F, -0.5F, -7.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 57).addBox(-6.0F, -0.5F, 6.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 52).addBox(-7.0F, -0.5F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(28, 52).addBox(6.0F, -0.5F, -5.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                // 4 Nodos rúnicos cardinales (diamantes brillantes)
                .texOffs(52, 52).addBox(-1.0F, -1.5F, -7.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 52).addBox(-1.0F, -1.5F, 5.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 52).addBox(-7.5F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 52).addBox(5.5F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 0.0F));

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

        // 1. Levitación sinusoidal suave de la espada y del dial
        float hover = Mth.sin(ageInTicks * 0.08F) * 1.5F;
        this.sword.y = 8.0F + hover;
        this.sword.yRot = ageInTicks * 0.035F;

        // 2. Dial orbital girando en dirección opuesta con leve cabeceo
        this.dial.y = 9.0F + hover;
        this.dial.yRot = -ageInTicks * 0.05F;
        this.dial.xRot = 0.08F * Mth.sin(ageInTicks * 0.04F);
    }
}
