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
 * Modelo 3D de la Palanca Mecánica Lateral Awakening System.
 * Incluye base metálica dorada fijada al chasis lateral, eje de rotación,
 * varilla de acero cromado y el icónico pomo esférico rojo rubí.
 */
public class SlotMachineLeverModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "slot_machine_lever"), "main");

    public final ModelPart mount;
    public final ModelPart arm;

    public SlotMachineLeverModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.mount = root.getChild("mount");
        this.arm = root.getChild("arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.8F, -2.5F, -2.5F, 1.6F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(32, 32).addBox(-2.2F, -1.4F, -1.4F, 1.8F, 2.8F, 2.8F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("arm", CubeListBuilder.create()
                .texOffs(32, 32).addBox(-2.6F, -1.0F, -1.0F, 1.2F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 32).addBox(-2.3F, -2.0F, -0.8F, 1.0F, 1.0F, 1.6F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-2.2F, -10.5F, -0.6F, 0.8F, 8.5F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(32, 32).addBox(-2.3F, -11.5F, -0.8F, 1.0F, 1.0F, 1.6F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-3.3F, -14.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.5F, -14.9F, -0.7F, 1.4F, 0.4F, 1.4F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        mount.render(poseStack, buffer, packedLight, packedOverlay, color);
        arm.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
