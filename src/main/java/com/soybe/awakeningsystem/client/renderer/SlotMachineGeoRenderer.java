package com.soybe.awakeningsystem.client.renderer;

import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.block.entity.SlotMachineBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SlotMachineGeoRenderer extends GeoBlockRenderer<SlotMachineBlockEntity> {
    public SlotMachineGeoRenderer(BlockEntityRendererProvider.Context context) {
        super(new SlotMachineGeoModel());
    }

    @Override
    public RenderType getRenderType(SlotMachineBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public void render(SlotMachineBlockEntity animatable, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = getFacing(animatable);
        Direction backDir = facing.getOpposite();
        poseStack.pushPose();
        poseStack.translate(backDir.getStepX() * 0.5D, 0.0D, backDir.getStepZ() * 0.5D);
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    protected Direction getFacing(SlotMachineBlockEntity animatable) {
        if (animatable.getBlockState().hasProperty(SlotMachineBlock.FACING)) {
            return animatable.getBlockState().getValue(SlotMachineBlock.FACING);
        }
        return Direction.NORTH;
    }
}
