package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.WyvernEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WyvernModel extends HierarchicalModel<WyvernEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "wyvern"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;
    private final ModelPart tailStinger;

    public WyvernModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
        this.tail = this.body.getChild("tail");
        this.tailStinger = this.tail.getChild("tail_stinger");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Torso principal
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -5.0F, -10.0F, 12.0F, 10.0F, 20.0F)
                        .texOffs(0, 30).addBox(-4.5F, -7.0F, -8.0F, 9.0F, 2.0F, 16.0F), // Espinas dorsales
                PartPose.offset(0.0F, 10.0F, 0.0F));

        // Cuello serpentino
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(44, 30).addBox(-3.5F, -12.0F, -5.0F, 7.0F, 12.0F, 7.0F)
                        .texOffs(0, 48).addBox(-1.0F, -14.0F, 1.0F, 2.0F, 14.0F, 3.0F), // Cresta del cuello
                PartPose.offsetAndRotation(0.0F, -2.0F, -9.0F, 0.45F, 0.0F, 0.0F));

        // Cabeza dracónica con cuernos
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-4.0F, -3.5F, -9.0F, 8.0F, 6.0F, 9.0F) // Hocico y cráneo
                        .texOffs(72, 15).addBox(-6.0F, -6.5F, -2.0F, 3.0F, 4.0F, 8.0F) // Cuerno izquierdo
                        .texOffs(72, 15).mirror().addBox(3.0F, -6.5F, -2.0F, 3.0F, 4.0F, 8.0F), // Cuerno derecho
                PartPose.offsetAndRotation(0.0F, -11.0F, -2.0F, -0.25F, 0.0F, 0.0F));

        // Mandíbula inferior móvil
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(64, 27).addBox(-3.5F, 0.0F, -8.5F, 7.0F, 3.0F, 9.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        // Ala Izquierda Wyvern
        PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(0, 65).addBox(0.0F, -2.0F, -3.0F, 16.0F, 3.0F, 5.0F) // Hombro/brazo
                        .texOffs(0, 75).addBox(16.0F, -1.5F, -2.0F, 22.0F, 2.0F, 4.0F) // Antebrazo
                        .texOffs(48, 75).addBox(0.0F, 0.0F, 2.0F, 38.0F, 1.0F, 28.0F), // Membrana alar
                PartPose.offsetAndRotation(6.0F, -3.0F, -6.0F, 0.0F, 0.0F, 0.15F));

        // Ala Derecha Wyvern
        PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(0, 65).mirror().addBox(-16.0F, -2.0F, -3.0F, 16.0F, 3.0F, 5.0F)
                        .texOffs(0, 75).mirror().addBox(-38.0F, -1.5F, -2.0F, 22.0F, 2.0F, 4.0F)
                        .texOffs(48, 75).mirror().addBox(-38.0F, 0.0F, 2.0F, 38.0F, 1.0F, 28.0F),
                PartPose.offsetAndRotation(-6.0F, -3.0F, -6.0F, 0.0F, 0.0F, -0.15F));

        // Pata Izquierda de rapaz
        PartDefinition leftLeg = body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(30, 48).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 8.0F, 6.0F) // Muslo
                        .texOffs(16, 48).addBox(-2.0F, 8.0F, -1.0F, 4.0F, 7.0F, 3.0F) // Garra
                        .texOffs(90, 48).addBox(-3.0F, 14.0F, -6.0F, 6.0F, 2.0F, 8.0F), // Talones
                PartPose.offset(5.5F, 2.0F, 5.0F));

        // Pata Derecha de rapaz
        PartDefinition rightLeg = body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(30, 48).mirror().addBox(-2.5F, 0.0F, -3.0F, 5.0F, 8.0F, 6.0F)
                        .texOffs(16, 48).mirror().addBox(-2.0F, 8.0F, -1.0F, 4.0F, 7.0F, 3.0F)
                        .texOffs(90, 48).mirror().addBox(-3.0F, 14.0F, -6.0F, 6.0F, 2.0F, 8.0F),
                PartPose.offset(-5.5F, 2.0F, 5.0F));

        // Cola larga articulada
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(72, 39).addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 18.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 10.0F, -0.15F, 0.0F, 0.0F));

        // Extremo de la cola con aguijón ponzoñoso / púa
        tail.addOrReplaceChild("tail_stinger", CubeListBuilder.create()
                        .texOffs(104, 0).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 4.0F, 16.0F)
                        .texOffs(104, 20).addBox(-1.0F, -5.0F, 12.0F, 2.0F, 8.0F, 5.0F), // Aguijón
                PartPose.offset(0.0F, 0.0F, 18.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(WyvernEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.7F;
        this.head.xRot = headPitch * ((float)Math.PI / 180F) * 0.7F;

        boolean flying = entity.isFlying();
        float flapSpeed = flying ? 0.35F : 0.08F;
        float flap = Mth.sin(ageInTicks * flapSpeed);

        if (flying) {
            // Aleteo vigoroso en vuelo
            this.leftWing.zRot = 0.2F + flap * 0.55F;
            this.rightWing.zRot = -0.2F - flap * 0.55F;

            // Piernas plegadas hacia atrás en aerodinámica
            this.leftLeg.xRot = 0.85F;
            this.rightLeg.xRot = 0.85F;

            // Cola balanceándose suavemente en el flujo de aire
            this.tail.yRot = Mth.sin(ageInTicks * 0.15F) * 0.2F;
            this.tailStinger.yRot = Mth.sin(ageInTicks * 0.15F + 1.0F) * 0.3F;
        } else {
            // Reposo o caminata en tierra
            this.leftWing.zRot = 0.65F + flap * 0.08F;
            this.rightWing.zRot = -0.65F - flap * 0.08F;

            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.2F * limbSwingAmount;

            this.tail.yRot = Mth.cos(limbSwing * 0.4F) * 0.35F * limbSwingAmount;
        }

        // Si está rugiendo o lanzando aliento: abrir mandíbula inferior
        if (entity.isRoaring()) {
            this.jaw.xRot = 0.65F;
            this.neck.xRot += 0.25F;
        }
    }
}
