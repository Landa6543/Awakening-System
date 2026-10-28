package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.block.CasinoWallDisplayStandBlock;
import com.soybe.awakeningsystem.block.entity.CasinoWallDisplayStandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CasinoWallDisplayStandRenderer implements BlockEntityRenderer<CasinoWallDisplayStandBlockEntity> {

    public CasinoWallDisplayStandRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CasinoWallDisplayStandBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getDisplayedItem();
        if (stack.isEmpty()) return;

        Direction facing = blockEntity.getBlockState().hasProperty(CasinoWallDisplayStandBlock.FACING)
                ? blockEntity.getBlockState().getValue(CasinoWallDisplayStandBlock.FACING)
                : Direction.NORTH;

        poseStack.pushPose();

        // Translate to the top center of the shelf
        double offsetX = 0.5D;
        double offsetZ = 0.5D;

        switch (facing) {
            case NORTH -> offsetZ = 0.656D;
            case SOUTH -> offsetZ = 0.344D;
            case WEST  -> offsetX = 0.656D;
            case EAST  -> offsetX = 0.344D;
        }

        float scale;
        double offsetY;
        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
            // Trophy / block item: large, grand championship scale (nearly 2x larger than before)
            scale = 1.15F;
            // Cushion top is at Y = 8.0/16 = 0.50D.
            // Model FIXED display applies 0.75 scale and centers model at -0.5 in Y.
            // Base rests flush on the cushion: offsetY = 0.50D + 0.5D * (scale * 0.75D).
            offsetY = 0.50D + 0.5D * (scale * 0.75D);
        } else {
            scale = 0.85F;
            offsetY = 0.65D;
        }

        poseStack.translate(offsetX, offsetY, offsetZ);

        float rotY = switch (facing) {
            case SOUTH -> 0.0F;
            case WEST  -> 90.0F;
            case NORTH -> 180.0F;
            case EAST  -> 270.0F;
            default    -> 0.0F;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );

        poseStack.popPose();
    }
}
