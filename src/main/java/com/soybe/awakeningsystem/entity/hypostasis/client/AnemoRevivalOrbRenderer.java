package com.soybe.awakeningsystem.entity.hypostasis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.hypostasis.AnemoRevivalOrbEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AnemoRevivalOrbRenderer extends MobRenderer<AnemoRevivalOrbEntity, RevivalOrbModel<AnemoRevivalOrbEntity>> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "anemo_revival_orb"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/anemo_orb.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/hypostasis/anemo_orb_glow.png");

    public AnemoRevivalOrbRenderer(EntityRendererProvider.Context context) {
        super(context, new RevivalOrbModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5F);
        this.addLayer(new RenderLayer<AnemoRevivalOrbEntity, RevivalOrbModel<AnemoRevivalOrbEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AnemoRevivalOrbEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(AnemoRevivalOrbEntity entity) {
        return TEXTURE;
    }
}
