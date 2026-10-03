package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.hypostasis.ElectroRevivalPrismEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ElectroRevivalPrismRenderer extends MobRenderer<ElectroRevivalPrismEntity, RevivalPrismModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "electro_revival_prism"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/electro_prism.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/electro_prism_glow.png");

    public ElectroRevivalPrismRenderer(EntityRendererProvider.Context context) {
        super(context, new RevivalPrismModel(context.bakeLayer(LAYER_LOCATION)), 0.6F);
        this.addLayer(new RenderLayer<ElectroRevivalPrismEntity, RevivalPrismModel>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ElectroRevivalPrismEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(ElectroRevivalPrismEntity entity) {
        return TEXTURE;
    }
}
