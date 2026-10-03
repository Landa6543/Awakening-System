package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo 3D del Huevo de Dragón Ancestral al estilo de los huevos de Wyvern de ARK:
 * Forma ovoidal / sub-cilíndrica de gran porte con escamas pétreas superpuestas,
 * base redondeada y cresta cónica estilizada.
 */
public class AncientDragonEggModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "ancient_dragon_egg"), "main");

    public final ModelPart root;
    public final ModelPart eggBase;
    public final ModelPart lowerBelly;
    public final ModelPart middleBody;
    public final ModelPart upperBody;
    public final ModelPart dome;
    public final ModelPart crestTip;

    public AncientDragonEggModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.eggBase = root.getChild("egg_base");
        this.lowerBelly = this.eggBase.getChild("lower_belly");
        this.middleBody = this.lowerBelly.getChild("middle_body");
        this.upperBody = this.middleBody.getChild("upper_body");
        this.dome = this.upperBody.getChild("dome");
        this.crestTip = this.dome.getChild("crest_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        // 1. Base cónica suave redondeada apoyada en el piso
        PartDefinition eggBase = part.addOrReplaceChild("egg_base", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 2. Vientre inferior ensanchado
        PartDefinition lowerBelly = eggBase.addOrReplaceChild("lower_belly", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // 3. Cintura central (la sección más ancha del huevo)
        PartDefinition middleBody = lowerBelly.addOrReplaceChild("middle_body", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-5.5F, -5.0F, -5.5F, 11.0F, 5.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        // 4. Bóveda superior que empieza a afilarse suavemente hacia la punta
        PartDefinition upperBody = middleBody.addOrReplaceChild("upper_body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.0F, 0.0F));

        // 5. Cúpula redondeada
        PartDefinition dome = upperBody.addOrReplaceChild("dome", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        // 6. Punta superior rocosa
        dome.addOrReplaceChild("crest_tip", CubeListBuilder.create()
                .texOffs(36, 10).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
