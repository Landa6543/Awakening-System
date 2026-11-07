package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.block.ChristmasTreeBlock;
import com.soybe.awakeningsystem.block.entity.ChristmasTreeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ChristmasTreeBlockEntityRenderer extends GeoBlockRenderer<ChristmasTreeBlockEntity> {

    public ChristmasTreeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new ChristmasTreeGeoModel());
        this.withScale(2.3F);
    }

    @Override
    public RenderType getRenderType(ChristmasTreeBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }

    @Override
    protected Direction getFacing(ChristmasTreeBlockEntity animatable) {
        if (animatable != null && animatable.getBlockState() != null && animatable.getBlockState().hasProperty(ChristmasTreeBlock.FACING)) {
            return animatable.getBlockState().getValue(ChristmasTreeBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    public void render(ChristmasTreeBlockEntity animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Toggle the visibility of the "gifts" bone according to daily claim state!
        getGeoModel().getBone("gifts").ifPresent(b -> b.setHidden(!animatable.hasGift()));
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}

