package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbstractAbyssMageEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo 3D canónico del Mago del Abismo (Genshin Impact).
 * Incluye túnica flotante, orejas puntiagudas, máscara estilizada,
 * animaciones de levitación arcana, ritual de cántico y pataleo en el suelo al estar aturdido.
 */
public class AbyssMageModel<T extends AbstractAbyssMageEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "abyss_mage"), "main");

    protected final ModelPart root;
    protected final ModelPart head;
    protected final ModelPart body;
    protected final ModelPart rightArm;
    protected final ModelPart leftArm;
    protected final ModelPart rightLeg;
    protected final ModelPart leftLeg;

    public AbyssMageModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addBodyParts(root);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static void addBodyParts(PartDefinition root) {
        // 1. Cabeza con hocico y orejas
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("snout", CubeListBuilder.create()
                .texOffs(37, 9).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.0F, -4.0F, 0.3927F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -8.0F, -1.0F, 0.0F, 0.0F, 0.2182F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -8.0F, -1.0F, 0.0F, 0.0F, -0.2182F));

        // 2. Cuerpo y faldas de la túnica
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, 0.0F, -3.0F, 12.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("skirt_right", CubeListBuilder.create()
                .texOffs(50, 33).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        body.addOrReplaceChild("skirt_front", CubeListBuilder.create()
                .texOffs(41, 19).addBox(2.5F, 0.0F, -0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 17).mirror().addBox(4.5F, 0.0F, -0.5F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 17).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, 12.0F, -2.5F, -0.3491F, 0.0F, 0.0F));

        body.addOrReplaceChild("skirt_left", CubeListBuilder.create()
                .texOffs(50, 33).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.5F, 12.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

        body.addOrReplaceChild("skirt_back", CubeListBuilder.create()
                .texOffs(41, 19).addBox(2.5F, 0.0F, -0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 17).mirror().addBox(4.5F, 0.0F, -0.5F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 17).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, 12.0F, 2.5F, 0.3491F, 0.0F, 0.0F));

        // 3. Brazos
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(50, 48).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(50, 48).mirror().addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 2.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        // 4. Piernas
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 49).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 49).mirror().addBox(-1.0F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Estado 1: Aturdido (Indefenso en el suelo pataleando)
        if (entity.isStunned()) {
            this.root.xRot = (float)Math.PI * 0.42F;
            this.root.y = 17.0F;

            // Pataleo cómico canónico de Genshin
            this.rightLeg.xRot = Mth.sin(ageInTicks * 0.7F) * 0.6F;
            this.leftLeg.xRot = -Mth.sin(ageInTicks * 0.7F) * 0.6F;
            this.rightArm.xRot = Mth.sin(ageInTicks * 0.5F) * 0.4F;
            this.leftArm.xRot = -Mth.sin(ageInTicks * 0.5F) * 0.4F;
            return;
        }

        // Estado 2: Cántico ritual de regeneración de escudo
        if (entity.isChanting()) {
            this.root.xRot = 0.0F;
            this.root.y = 0.0F + Mth.sin(ageInTicks * 0.2F) * 1.2F;
            // Brazos levantados al cielo canalizando
            this.rightArm.xRot = -(float)Math.PI * 0.85F + Mth.sin(ageInTicks * 0.3F) * 0.15F;
            this.leftArm.xRot = -(float)Math.PI * 0.85F + Mth.sin(ageInTicks * 0.3F) * 0.15F;
            this.rightArm.zRot = 0.35F;
            this.leftArm.zRot = -0.35F;
            return;
        }

        // Estado 3: Flotación normal con escudo
        this.root.xRot = 0.0F;
        float bob = Mth.sin(ageInTicks * 0.12F) * 2.0F;
        this.root.y = -2.0F + bob;

        // Movimiento de brazos al caminar/flotar
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.4F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
        this.rightLeg.xRot = 0.15F + Mth.sin(ageInTicks * 0.1F) * 0.1F;
        this.leftLeg.xRot = 0.15F - Mth.sin(ageInTicks * 0.1F) * 0.1F;
    }
}
