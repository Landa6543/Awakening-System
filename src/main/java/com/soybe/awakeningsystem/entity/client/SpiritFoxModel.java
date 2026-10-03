package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.SpiritFoxEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo geométrico para el Zorro Espiritual de Nueve Colas.
 * Presenta una figura ágil y elegante con hocico fino, orejas erguidas
 * y nueve colas independientes en abanico con animación ondulatoria fluida.
 */
public class SpiritFoxModel extends HierarchicalModel<SpiritFoxEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "spirit_fox"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart legFrontRight;
    private final ModelPart legFrontLeft;
    private final ModelPart legBackRight;
    private final ModelPart legBackLeft;
    private final ModelPart[] tails = new ModelPart[9];

    public SpiritFoxModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.legFrontRight = this.body.getChild("leg_front_right");
        this.legFrontLeft = this.body.getChild("leg_front_left");
        this.legBackRight = this.body.getChild("leg_back_right");
        this.legBackLeft = this.body.getChild("leg_back_left");
        for (int i = 0; i < 9; i++) {
            this.tails[i] = this.body.getChild("tail" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cuerpo principal (Y = 16.0F)
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Cabeza con hocico y orejas
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(36, 0)
                .addBox(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)) // Cráneo
                .texOffs(0, 18)
                .addBox(-1.5F, -0.8F, -8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)) // Hocico fino
                .texOffs(14, 18)
                .addBox(-2.8F, -6.0F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)) // Oreja derecha
                .texOffs(22, 18)
                .addBox(0.8F, -6.0F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), // Oreja izquierda
                PartPose.offset(0.0F, -1.5F, -5.5F));

        // Patas delanteras pequeñitas y estilizadas (1.8x5.0x1.8)
        body.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                .texOffs(0, 24)
                .addBox(-0.9F, 0.0F, -0.9F, 1.8F, 5.0F, 1.8F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 3.0F, -3.8F));

        body.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                .texOffs(0, 24)
                .addBox(-0.9F, 0.0F, -0.9F, 1.8F, 5.0F, 1.8F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 3.0F, -3.8F));

        // Patas traseras pequeñitas y estilizadas (1.8x5.0x1.8)
        body.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                .texOffs(10, 24)
                .addBox(-0.9F, 0.0F, -0.9F, 1.8F, 5.0F, 1.8F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 3.0F, 3.8F));

        body.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                .texOffs(10, 24)
                .addBox(-0.9F, 0.0F, -0.9F, 1.8F, 5.0F, 1.8F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 3.0F, 3.8F));

        // Las Nueve Colas Espirituales en Abanico Radial Elevado hacia el Cielo
        // Cada cola posee su propio origen distribuido en la grupa, grosor estilizado (1.7x1.7x9.5)
        // y apertura tridimensional (yaw, pitch ascendente y roll de expansión)
        for (int i = 0; i < 9; i++) {
            int idx = i - 4;
            int dist = Math.abs(idx);
            float rootX = idx * 0.55F;
            float rootY = -2.2F + dist * 0.12F;
            float rootZ = 5.6F - dist * 0.10F;

            float pitch = 1.05F - dist * 0.08F; // Elevación arqueada hacia arriba
            float yaw = idx * 0.30F;            // Despliegue en abanico abierto
            float roll = idx * 0.16F;           // Inclinación 3D hacia los costados

            body.addOrReplaceChild("tail" + i, CubeListBuilder.create()
                    .texOffs(0, 36)
                    .addBox(-0.85F, -0.85F, 0.0F, 1.7F, 1.7F, 9.5F, new CubeDeformation(0.0F)),
                    PartPose.offsetAndRotation(rootX, rootY, rootZ, pitch, yaw, roll));
        }

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(SpiritFoxEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Movimiento de la cabeza
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);

        boolean isSitting = entity.isInSittingPose() || entity.isOrderedToSit();

        if (isSitting) {
            // Postura de descanso / meditación: el zorro se asienta con elegancia
            this.body.y = 19.5F;
            this.body.xRot = -0.28F;

            this.legFrontRight.xRot = 0.28F;
            this.legFrontLeft.xRot = 0.28F;
            this.legBackRight.xRot = -1.35F;
            this.legBackLeft.xRot = -1.35F;
            this.legBackRight.y = 1.2F;
            this.legBackLeft.y = 1.2F;

            // Las 9 colas permanecen erguidas en abanico sagrado detrás de él
            for (int i = 0; i < 9; i++) {
                int idx = i - 4;
                int dist = Math.abs(idx);
                float sitPitch = 1.25F - dist * 0.06F;
                float sitYaw = idx * 0.28F;
                float sitRoll = idx * 0.14F;
                float breathe = Mth.sin(ageInTicks * 0.08F + i * 0.3F) * 0.05F;

                this.tails[i].xRot = sitPitch + breathe;
                this.tails[i].yRot = sitYaw;
                this.tails[i].zRot = sitRoll;
            }
        } else {
            // Postura de pie / marcha
            this.body.y = 16.0F;
            this.body.xRot = 0.0F;

            // Locomoción ágil de patas pequeñitas (trote ligero y vivaz)
            this.legFrontRight.xRot = Mth.cos(limbSwing * 0.85F) * 0.9F * limbSwingAmount;
            this.legFrontLeft.xRot = Mth.cos(limbSwing * 0.85F + (float) Math.PI) * 0.9F * limbSwingAmount;
            this.legBackRight.xRot = Mth.cos(limbSwing * 0.85F + (float) Math.PI) * 0.9F * limbSwingAmount;
            this.legBackLeft.xRot = Mth.cos(limbSwing * 0.85F) * 0.9F * limbSwingAmount;

            // Las 9 colas se elevan hacia arriba, separadas en abanico y con ondulación mística continua
            for (int i = 0; i < 9; i++) {
                int idx = i - 4;
                int dist = Math.abs(idx);
                float basePitch = 1.05F - dist * 0.08F;
                float baseYaw = idx * 0.30F;
                float baseRoll = idx * 0.16F;

                // Desfase armónico en cascada entre cola y cola
                float phase = i * 0.48F;
                float wavePitch = Mth.sin(ageInTicks * 0.09F + phase) * 0.08F;
                float waveYaw = Mth.cos(ageInTicks * 0.08F + phase) * 0.07F;
                float runSway = Mth.cos(limbSwing * 0.6662F) * 0.10F * limbSwingAmount;

                this.tails[i].xRot = basePitch + wavePitch - (limbSwingAmount * 0.10F);
                this.tails[i].yRot = baseYaw + waveYaw + runSway;
                this.tails[i].zRot = baseRoll + Mth.sin(ageInTicks * 0.07F + phase) * 0.05F;
            }
        }
    }
}
