package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.block.entity.PlushieBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieBlockEntityRenderer implements BlockEntityRenderer<PlushieBlockEntity> {

    public PlushieBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (state.isAir()) return;

        poseStack.pushPose();

        // Animación elástica de apachurrado (Squash & Stretch Bounce)
        float squashTicks = blockEntity.getSquashTicks(partialTick);
        if (squashTicks > 0.0F) {
            float progress = squashTicks / 10.0F; // 1.0 -> 0.0
            float squash = Mth.sin(progress * (float) Math.PI); // 0.0 -> 1.0 -> 0.0
            // Pivotar exactamente desde la base central del bloque (X=0.5, Y=0.0, Z=0.5)
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.scale(1.0F + squash * 0.32F, 1.0F - squash * 0.42F, 1.0F + squash * 0.32F);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
        }

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var model = blockRenderer.getBlockModel(state);
        var vertexConsumer = bufferSource.getBuffer(RenderType.cutout());

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                vertexConsumer,
                state,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
                RenderType.cutout()
        );

        poseStack.popPose();
    }
}
