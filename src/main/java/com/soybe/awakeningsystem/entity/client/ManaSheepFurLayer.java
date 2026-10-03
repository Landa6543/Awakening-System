package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.animal.ManaSheepEntity;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class ManaSheepFurLayer extends RenderLayer<ManaSheepEntity, SheepModel<ManaSheepEntity>> {

    private static final ResourceLocation FUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/animal/mana_sheep_fur.png");

    private final SheepFurModel<ManaSheepEntity> model;

    public ManaSheepFurLayer(RenderLayerParent<ManaSheepEntity, SheepModel<ManaSheepEntity>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new SheepFurModel<>(modelSet.bakeLayer(ModelLayers.SHEEP_FUR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       ManaSheepEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isSheared()) {
            if (entity.isInvisible()) return;

            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(FUR_TEXTURE));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                    net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                    0xFFFFFFFF);
        }
    }
}
