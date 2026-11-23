package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AbyssChallengeKeyEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the challenge key, with its wire cube and red lines glowing (the _glow texture, full bright). */
public class AbyssChallengeKeyRenderer extends EntityRenderer<AbyssChallengeKeyEntity> {
    private static final ResourceLocation KEY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyss/abyss_challenge_key.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/abyss/abyss_challenge_key_glow.png");

    private final AbyssChallengeKeyModel model;

    public AbyssChallengeKeyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.model = new AbyssChallengeKeyModel(context.bakeLayer(AbyssChallengeKeyModel.LAYER_LOCATION));
    }

    @Override
    public void render(AbyssChallengeKeyEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // models are made with y growing downwards: turned the right way up, standing on the ground
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        float ageInTicks = (float) entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(KEY_TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(GLOW_TEXTURE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssChallengeKeyEntity entity) {
        return KEY_TEXTURE;
    }
}
