package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.block.SubwayDoorBlock;
import com.soybe.awakeningsystem.block.entity.SubwayDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renderer dinámico para la Puerta Corrediza de Metro (Línea 3 de Seúl).
 * Desplaza suavemente el panel de la puerta lateralmente hacia el interior del marco
 * con una curva de aceleración y desaceleración neumática suave (SmoothStep).
 */
public class SubwayDoorRenderer implements BlockEntityRenderer<SubwayDoorBlockEntity> {

    public SubwayDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SubwayDoorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof SubwayDoorBlock)) return;

        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);

        float rawProgress = be.getSlideProgress(partialTick);
        float t = Mth.clamp(rawProgress, 0.0F, 1.0F);
        // Curva SmoothStep para simular el émbolo de aire comprimido (amortiguación suave en los extremos)
        float smoothProgress = t * t * (3.0F - 2.0F * t);

        // Vector de desplazamiento lateral:
        // Bisagra izquierda -> se abre hacia la izquierda de la dirección enfrentada (counter-clockwise)
        // Bisagra derecha   -> se abre hacia la derecha de la dirección enfrentada (clockwise)
        Direction slideDir = (hinge == DoorHingeSide.LEFT) ? facing.getCounterClockWise() : facing.getClockWise();
        double offsetX = slideDir.getStepX() * (14.0D / 16.0D) * smoothProgress;
        double offsetZ = slideDir.getStepZ() * (14.0D / 16.0D) * smoothProgress;

        poseStack.pushPose();
        poseStack.translate(offsetX, 0.0D, offsetZ);

        // Renderizamos el modelo de la puerta en su geometría cerrada, aplicándole la traslación calculada
        BlockState renderState = state.setValue(DoorBlock.OPEN, false);
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var model = blockRenderer.getBlockModel(renderState);
        var vertexConsumer = bufferSource.getBuffer(RenderType.cutout());

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                vertexConsumer,
                renderState,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                RenderType.cutout()
        );

        poseStack.popPose();
    }
}
