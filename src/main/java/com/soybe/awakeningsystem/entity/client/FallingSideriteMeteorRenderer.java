package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.block.SystemBlocks;
import com.soybe.awakeningsystem.entity.FallingSideriteMeteorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * The falling meteorite: a real siderite block, a little larger than one block, tumbling as it falls and lit
 * by its own heat. The game draws its usual fire around it (the entity always counts as burning).
 */
public class FallingSideriteMeteorRenderer extends EntityRenderer<FallingSideriteMeteorEntity> {

    public FallingSideriteMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(FallingSideriteMeteorEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float spin = (entity.getLifeTicks() + partialTick) * 9.0F;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.8D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(spin));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin * 0.6F));
        poseStack.scale(1.4F, 1.4F, 1.4F);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(SystemBlocks.SIDERITE_BLOCK.get().defaultBlockState(), poseStack, buffer,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingSideriteMeteorEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
