package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.PlushieBlock;
import com.soybe.awakeningsystem.block.entity.PlushieBlockEntity;
import com.soybe.awakeningsystem.client.model.CustomPlushieModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;

public class PlushieBlockEntityRenderer implements BlockEntityRenderer<PlushieBlockEntity> {

    public record TributeInfo(String name, UUID uuid, ResourceLocation fallbackTexture) {}

    public static final Map<String, TributeInfo> TRIBUTES = Map.of(
            "landeraloplushie", new TributeInfo("LanderAlo", UUID.fromString("9c6e2002-2a3d-4a19-bc57-ecd32c1f129d"),
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/landeraloplushie.png")),
            "schvisplushie", new TributeInfo("Schvis", UUID.fromString("dddf8989-c266-4b5f-b91a-987f9b3e7c64"),
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/schvisplushie.png")),
            "techidnaplushie", new TributeInfo("Techidnaaaa_", UUID.fromString("87f53b32-38b1-4233-9209-8da99fec7315"),
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/techidnaplushie.png")),
            "tomcraftplushie", new TributeInfo("TomCraft2003", UUID.fromString("e846a525-b14a-4f6a-963e-bf8d2f2db519"),
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/tomcraftplushie.png")),
            "nandachanplushie", new TributeInfo("Nandachan", UUID.fromString("21115ef9-68a9-4612-ac2c-2b27d7527849"),
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/nandachanplushie.png")),
            "misticpotatoplushie", new TributeInfo("MisticPotato", UUID.fromString("936dab1b-f931-45b7-9f6d-e9ad2430e66a"),
                    ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/misticpotatoplushie.png"))
    );

    public static final Map<String, ResourceLocation> CHIBI_SKIN_PLUSHIES = Map.of(
            "hinaplushie", ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/hinaplushie.png"),
            "shirokoplushie", ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/shirokoplushie.png"),
            "arisplushie", ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/arisplushie.png"),
            "amiyaplushie", ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/amiyaplushie.png"),
            "kaltsitplushie", ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/kaltsitplushie.png"),
            "chenplushie", ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/chenplushie.png")
    );

    private final CustomPlushieModel playerModel;

    public PlushieBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.playerModel = new CustomPlushieModel(context.bakeLayer(CustomPlushieModel.LAYER_LOCATION));
    }

    @Override
    public void render(PlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (state.isAir()) return;

        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        TributeInfo tribute = TRIBUTES.get(blockId);
        ResourceLocation chibiTexture = tribute != null ? tribute.fallbackTexture() : CHIBI_SKIN_PLUSHIES.get(blockId);

        poseStack.pushPose();

        float squashTicks = blockEntity.getSquashTicks(partialTick);
        float squash = 0.0F;
        if (squashTicks > 0.0F) {
            float progress = squashTicks / 10.0F;
            squash = Mth.sin(progress * (float) Math.PI);
        }

        if (chibiTexture != null) {
            poseStack.translate(0.5D, 0.0D, 0.5D);

            Direction facing = state.hasProperty(PlushieBlock.FACING) ? state.getValue(PlushieBlock.FACING) : Direction.NORTH;
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));

            if (squash > 0.0F) {
                poseStack.scale(1.0F + squash * 0.32F, 1.0F - squash * 0.42F, 1.0F + squash * 0.32F);
            }

            float scale = 0.88F;
            poseStack.scale(-scale, -scale, scale);

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(chibiTexture));
            this.playerModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        } else {
            if (squash > 0.0F) {
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
        }

        poseStack.popPose();
    }
}
