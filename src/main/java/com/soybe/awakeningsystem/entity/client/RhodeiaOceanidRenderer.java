package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.RhodeiaOceanidBossEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RhodeiaOceanidRenderer extends MobRenderer<RhodeiaOceanidBossEntity, RhodeiaOceanidModel> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "rhodeia_oceanid"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hateful_oceanid.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/emissive_hateful_oceanid.png");

    public RhodeiaOceanidRenderer(EntityRendererProvider.Context context) {
        super(context, new RhodeiaOceanidModel(context.bakeLayer(LAYER_LOCATION)), 1.5F);
        this.addLayer(new RenderLayer<RhodeiaOceanidBossEntity, RhodeiaOceanidModel>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, RhodeiaOceanidBossEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        });
    }

    @Override
    protected void scale(RhodeiaOceanidBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(RhodeiaOceanidBossEntity entity) {
        return TEXTURE;
    }
}
