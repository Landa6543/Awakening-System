package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.block.AscensionPortalBlock;
import com.soybe.awakeningsystem.block.entity.AscensionPortalBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * Renderer GeckoLib para el Portal Dimensional de la Gran Torre (Ascension Portal / Gate).
 * Renderiza el portal 3D con arcos de piedra y discos de vórtice dimensional animados,
 * iluminación full-bright translúcida y escala majestuosa.
 */
public class AscensionPortalRenderer extends GeoBlockRenderer<AscensionPortalBlockEntity> {

    public AscensionPortalRenderer(BlockEntityRendererProvider.Context context) {
        super(new AscensionPortalGeoModel());
    }

    @Override
    public RenderType getRenderType(AscensionPortalBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void render(AscensionPortalBlockEntity animatable, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // Escalar sutilmente (1.4x) centrado en el bloque para darle porte imponente de Gate
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.scale(1.4F, 1.4F, 1.4F);
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        super.render(animatable, partialTick, poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay);
        poseStack.popPose();
    }

    @Override
    protected Direction getFacing(AscensionPortalBlockEntity animatable) {
        if (animatable != null && animatable.getBlockState() != null && animatable.getBlockState().hasProperty(AscensionPortalBlock.FACING)) {
            return animatable.getBlockState().getValue(AscensionPortalBlock.FACING);
        }
        return Direction.NORTH;
    }
}
