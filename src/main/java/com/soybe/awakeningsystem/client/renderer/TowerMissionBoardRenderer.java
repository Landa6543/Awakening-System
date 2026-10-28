package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.block.TowerMissionBoardBlock;
import com.soybe.awakeningsystem.block.entity.TowerMissionBoardBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renderer diegético para el Tablón de Misiones de la Torre.
 * Escala el modelo base a 5 bloques de largo (-32 a +48) y 3 bloques de alto (0 a 48),
 * y lo orienta con precisión matemática para coincidir exactamente con el VoxelShape de cada dirección.
 */
public class TowerMissionBoardRenderer implements BlockEntityRenderer<TowerMissionBoardBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public TowerMissionBoardRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TowerMissionBoardBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof TowerMissionBoardBlock)) return;

        Direction facing = state.getValue(TowerMissionBoardBlock.FACING);

        poseStack.pushPose();

        // 1. Pivotar al centro horizontal del bloque (0.5, 0.0, 0.5)
        poseStack.translate(0.5D, 0.0D, 0.5D);

        // 2. Rotación: Invertida 180° para que la cara frontal (con carteles de misiones, recompensas y pines)
        // mire directamente hacia el jugador y la habitación, y la espalda quede pegada a la pared.
        float rotY = switch (facing) {
            case NORTH -> 180.0F;
            case SOUTH -> 0.0F;
            case WEST  -> 90.0F;
            case EAST  -> 270.0F;
            default    -> 180.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));

        // 3. Escalar en el espacio local:
        // Ancho (X): de 48px a 80px (5 bloques de ancho) = 1.666667F
        // Alto (Y): de 32px a 48px (3 bloques de alto) = 1.500000F
        // Espesor (Z): 1.0F
        // Pivote de escala en la cara trasera pegada a la pared (Z = -0.5 tras invertir rotación)
        poseStack.translate(0.0D, 0.0D, -0.5D);
        poseStack.scale(1.666667F, 1.5F, 1.0F);
        poseStack.translate(0.0D, 0.0D, 0.5D);

        // 4. Regresar del pivote de bloque
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        BakedModel model = blockRenderer.getBlockModel(state);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)),
                state,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                null
        );

        poseStack.popPose();
    }
}
