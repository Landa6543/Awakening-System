package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.block.RouletteAltarBlock;
import com.soybe.awakeningsystem.block.entity.RouletteAltarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * Renderizador GeckoLib 4 para el Altar de la Ruleta (Roulette Altar).
 * Utiliza 100% fielmente la geometría y mapeo UV por cara exportado directamente
 * desde roulette_altar.bbmodel, con rotación procedural física de la rueda y de la aguja.
 */
public class RouletteAltarRenderer extends GeoBlockRenderer<RouletteAltarBlockEntity> {
    private final ItemRenderer itemRenderer;

    public RouletteAltarRenderer(BlockEntityRendererProvider.Context context) {
        super(new RouletteAltarGeoModel());
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public RenderType getRenderType(RouletteAltarBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    protected Direction getFacing(RouletteAltarBlockEntity animatable) {
        if (animatable != null && animatable.getBlockState() != null && animatable.getBlockState().hasProperty(RouletteAltarBlock.FACING)) {
            return animatable.getBlockState().getValue(RouletteAltarBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        // En el modelo Blockbench (roulette_altar.bbmodel), el frente (rueda y aguja) fue modelado
        // mirando hacia +Z (SOUTH), mientras que la convención interna de GeckoLib asume que el frente
        // está en -Z (NORTH). Por tanto, rotamos con el opuesto para que el frente mire siempre hacia el jugador.
        super.rotateBlock(facing.getOpposite(), poseStack);
    }

    @Override
    public void render(RouletteAltarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Calcular luz ambiental y superior para evitar oscurecimiento
        int light = packedLight;
        if (blockEntity.getLevel() != null) {
            BlockPos pos = blockEntity.getBlockPos();
            int lightPos = LevelRenderer.getLightColor(blockEntity.getLevel(), pos);
            int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), pos.above());
            int lightAbove2 = LevelRenderer.getLightColor(blockEntity.getLevel(), pos.above(2));
            light = Math.max(light, Math.max(lightPos, Math.max(lightAbove, lightAbove2)));
        }

        // Iluminación arcade nítida (mínimo bloque 12, mínimo cielo 10)
        int blockLight = Math.max(LightTexture.block(light), 12);
        int skyLight = Math.max(LightTexture.sky(light), 10);
        int effectiveLight = LightTexture.pack(blockLight, skyLight);

        // 1. Renderizar el modelo 3D con GeckoLib (geometría exacta 1:1 y UVs exactos del bbmodel)
        super.render(blockEntity, partialTick, poseStack, bufferSource, effectiveLight, packedOverlay);

        // 2. Renderizar los 8 ítems en los compartimentos de la rueda
        Direction facing = getFacing(blockEntity);
        float wheelAngle = Mth.lerp(partialTick, blockEntity.getPrevWheelAngle(), blockEntity.getWheelAngle());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        rotateBlock(facing, poseStack);

        // El centro de la rueda en el modelo está exactamente en Y=20 (1.25b), Z=0.8 (0.05b)
        // El frente del disco está en Z = 1.1 / 16.0 = 0.06875b.
        // Colocamos los ítems en Z = 0.08b (justo delante del disco y detrás de la aguja Z=0.13b)
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.25D, 0.08D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-wheelAngle));

        for (int i = 0; i < 8; i++) {
            ItemStack stack = blockEntity.getSlotItem(i);
            if (stack.isEmpty()) continue;

            poseStack.pushPose();
            float slotAngle = i * 45.0F;
            double rad = Math.toRadians(slotAngle);
            double dist = 0.65D;

            double ix = Math.sin(rad) * dist;
            double iy = Math.cos(rad) * dist;

            poseStack.translate(ix, iy, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-slotAngle));
            poseStack.scale(0.42F, 0.42F, 0.42F);

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, effectiveLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }
        poseStack.popPose();

        // 3. Animación de ítem premiado flotante y texto holográfico
        float winProgress = 0.0F;
        float elevateY = 1.25F;
        if (blockEntity.getWinDisplayTicks() > 0) {
            winProgress = (60.0F - blockEntity.getWinDisplayTicks() + partialTick) / 60.0F;
            winProgress = Mth.clamp(winProgress, 0.0F, 1.0F);
            elevateY = 1.25F + (float) Math.sin(winProgress * Math.PI) * 0.45F;

            poseStack.pushPose();
            poseStack.translate(0.0D, elevateY, 0.45D);
            poseStack.mulPose(Axis.YP.rotationDegrees(winProgress * 720.0F));
            poseStack.scale(0.70F, 0.70F, 0.70F);

            itemRenderer.renderStatic(blockEntity.getWinningItem(), ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }

        poseStack.popPose();

        // 4. Cartel / holograma 3D mirando al jugador
        if (blockEntity.getWinDisplayTicks() > 0) {
            ItemStack winning = blockEntity.getWinningItem();
            if (!winning.isEmpty()) {
                Minecraft mc = Minecraft.getInstance();
                double distSq = mc.getEntityRenderDispatcher().distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5D,
                        blockEntity.getBlockPos().getY() + 1.5D,
                        blockEntity.getBlockPos().getZ() + 0.5D
                );
                if (distSq < 256.0D) {
                    poseStack.pushPose();
                    poseStack.translate(
                            0.5D + facing.getStepX() * 0.45D,
                            elevateY + 0.45D,
                            0.5D + facing.getStepZ() * 0.45D
                    );
                    poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                    poseStack.scale(-0.022F, -0.022F, 0.022F);

                    Font font = mc.font;
                    Component itemName = Component.literal("§6★ §f").append(winning.getHoverName()).append(Component.literal("§6 ★"));
                    float w1 = font.width(itemName);
                    font.drawInBatch(
                            itemName,
                            -w1 / 2.0F,
                            -11.0F,
                            0xFFFFFFFF,
                            false,
                            poseStack.last().pose(),
                            bufferSource,
                            Font.DisplayMode.POLYGON_OFFSET,
                            0x50000000,
                            LightTexture.FULL_BRIGHT
                    );

                    if (blockEntity.getCoinsWon() > 0) {
                        Component coinsText = Component.literal("§e+" + blockEntity.getCoinsWon() + " §6⛃");
                        float w2 = font.width(coinsText);
                        font.drawInBatch(
                                coinsText,
                                -w2 / 2.0F,
                                1.0F,
                                0xFFFFD700,
                                false,
                                poseStack.last().pose(),
                                bufferSource,
                                Font.DisplayMode.POLYGON_OFFSET,
                                0x50000000,
                                LightTexture.FULL_BRIGHT
                        );
                    }

                    poseStack.popPose();
                }
            }
        }
    }
}
