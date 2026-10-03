package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.LockedTreasureChestBlock;
import com.soybe.awakeningsystem.block.entity.LockedTreasureChestBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class LockedTreasureChestRenderer implements BlockEntityRenderer<LockedTreasureChestBlockEntity> {
    public static final ResourceLocation TEXTURE_OVERWORLD =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/chest/locked_chest_overworld.png");
    public static final ResourceLocation TEXTURE_NETHER =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/chest/locked_chest_nether.png");
    public static final ResourceLocation TEXTURE_END =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/chest/locked_chest_end.png");
    public static final ResourceLocation TEXTURE_TEYVAT =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/chest/locked_chest_teyvat.png");
    public static final ResourceLocation TEXTURE_ISEKAI =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/chest/locked_chest_isekai.png");
    public static final ResourceLocation TEXTURE_MURIM =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/chest/locked_chest_murim.png");

    private final LockedTreasureChestModel model;

    public LockedTreasureChestRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new LockedTreasureChestModel(context.bakeLayer(LockedTreasureChestModel.LAYER_LOCATION));
    }

    @Override
    public void render(LockedTreasureChestBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Direction facing = blockEntity.getBlockState().hasProperty(LockedTreasureChestBlock.FACING)
                ? blockEntity.getBlockState().getValue(LockedTreasureChestBlock.FACING)
                : Direction.NORTH;

        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        boolean isUnlockedForMe = (mc.player != null && blockEntity.isUnlockedFor(mc.player.getUUID()));
        boolean isLocked = !isUnlockedForMe;

        // Control suave de apertura de la tapa (solo si no está con candado)
        float openness = blockEntity.getOpenness(partialTick);
        if (isLocked) {
            openness = 0.0F;
        }
        // Pivota hacia arriba y hacia atrás sobre el eje X hasta 90 grados
        this.model.lid.xRot = (openness * ((float) Math.PI / 2.0F));

        ResourceLocation texture = TEXTURE_OVERWORLD;
        if (blockEntity.getBlockState().getBlock() instanceof LockedTreasureChestBlock chestBlock) {
            switch (chestBlock.getTheme()) {
                case OVERWORLD -> texture = TEXTURE_OVERWORLD;
                case NETHER -> texture = TEXTURE_NETHER;
                case END -> texture = TEXTURE_END;
                case TEYVAT -> texture = TEXTURE_TEYVAT;
                case ISEKAI -> texture = TEXTURE_ISEKAI;
                case MURIM -> texture = TEXTURE_MURIM;
                case AUTO -> {
                    if (blockEntity.getLevel() != null) {
                        String path = blockEntity.getLevel().dimension().location().getPath().toLowerCase(java.util.Locale.ROOT);
                        if (path.contains("nether")) texture = TEXTURE_NETHER;
                        else if (path.contains("end")) texture = TEXTURE_END;
                        else if (path.contains("teyvat")) texture = TEXTURE_TEYVAT;
                        else if (path.contains("isekai")) texture = TEXTURE_ISEKAI;
                        else if (path.contains("murim")) texture = TEXTURE_MURIM;
                    }
                }
            }
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(texture));

        // Base y tapa abisagrada
        this.model.bottom.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        this.model.lid.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);

        // Si está cerrado con candado, renderizar cadenas 3D y el gran candado de oro con grillete
        if (isLocked) {
            this.model.chains.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
            this.model.padlock.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        }

        poseStack.popPose();
    }
}
