package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.PerpetualMechanicalArrayBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class PerpetualMechanicalArrayRenderer extends MobRenderer<PerpetualMechanicalArrayBossEntity, PerpetualMechanicalArrayModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "perpetual_mechanical_array"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/perpetual_mechanical_array.png");
    private static final ResourceLocation TEXTURE_DEFENSE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/perpetual_mechanical_array_defense.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/emissive_perpetual_mechanical_array.png");

    public PerpetualMechanicalArrayRenderer(EntityRendererProvider.Context context) {
        super(context, new PerpetualMechanicalArrayModel(context.bakeLayer(LAYER_LOCATION)), 1.4F);
        this.addLayer(new RenderLayer<PerpetualMechanicalArrayBossEntity, PerpetualMechanicalArrayModel>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, PerpetualMechanicalArrayBossEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        });
    }

    @Override
    protected void scale(PerpetualMechanicalArrayBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(PerpetualMechanicalArrayBossEntity entity) {
        return entity.isDefenseMode() ? TEXTURE_DEFENSE : TEXTURE;
    }
}
