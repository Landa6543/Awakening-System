package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssChallengeKeyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador 3D para la Llave Roja de Desafío del Abismo de Genshin Impact.
 */
public class AbyssChallengeKeyRenderer extends EntityRenderer<AbyssChallengeKeyEntity> {
    private static final ResourceLocation KEY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyss/abyss_challenge_key.png");

    private final AbyssChallengeKeyModel model;

    public AbyssChallengeKeyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.model = new AbyssChallengeKeyModel(context.bakeLayer(AbyssChallengeKeyModel.LAYER_LOCATION));
    }

    @Override
    public void render(AbyssChallengeKeyEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = (float) entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssChallengeKeyEntity entity) {
        return KEY_TEXTURE;
    }
}
