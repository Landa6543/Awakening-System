package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.block.entity.TombstoneBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * Renderizador GeckoLib 4 para la Lápida del Sistema (Deadlock Tombstone).
 * Aplica escala 0.60F (altura ~1.91 bloques, monumental y proporcionada),
 * sombreado cutout para las almas y la piedra, y placa de nombre flotante.
 */
public class TombstoneBlockEntityRenderer extends GeoBlockRenderer<TombstoneBlockEntity> {

    public TombstoneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new TombstoneGeoModel());
        this.withScale(0.60F);
    }

    @Override
    public RenderType getRenderType(TombstoneBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public void render(TombstoneBlockEntity animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        // Renderizar placa de nombre flotante si no se está destruyendo
        if (!animatable.isBreakingDown()) {
            String ownerName = animatable.getOwnerName();
            if (!ownerName.isEmpty()) {
                Minecraft mc = Minecraft.getInstance();
                double distSq = mc.getEntityRenderDispatcher().distanceToSqr(
                        animatable.getBlockPos().getX() + 0.5D,
                        animatable.getBlockPos().getY() + 1.0D,
                        animatable.getBlockPos().getZ() + 0.5D
                );
                if (distSq < 256.0D) { // Visible a menos de 16 bloques
                    poseStack.pushPose();
                    poseStack.translate(0.5D, 2.05D, 0.5D);
                    poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                    poseStack.scale(-0.02F, -0.02F, 0.02F);

                    Font font = mc.font;
                    Component nameComp = Component.literal("§b☠ Lápida de §f" + ownerName);
                    float x = -font.width(nameComp) / 2.0F;
                    font.drawInBatch(nameComp, x, 0, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0x40000000, packedLight);
                    poseStack.popPose();
                }
            }
        }
    }
}
