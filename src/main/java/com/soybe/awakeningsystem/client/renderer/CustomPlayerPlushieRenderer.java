package com.soybe.awakeningsystem.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.block.CustomPlayerPlushieBlock;
import com.soybe.awakeningsystem.block.entity.CustomPlayerPlushieBlockEntity;
import com.soybe.awakeningsystem.client.model.CustomPlushieModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.state.BlockState;
import java.util.UUID;

public class CustomPlayerPlushieRenderer implements BlockEntityRenderer<CustomPlayerPlushieBlockEntity> {
    private static final ResourceLocation LANDER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/landeralo_skin.png");

    private final CustomPlushieModel model;

    public CustomPlayerPlushieRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CustomPlushieModel(context.bakeLayer(CustomPlushieModel.LAYER_LOCATION));
    }

    @Override
    public void render(CustomPlayerPlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (state.isAir() || !(state.getBlock() instanceof CustomPlayerPlushieBlock)) return;

        ResolvableProfile profile = blockEntity.getOwnerProfile();
        ResourceLocation skinTexture = null;

        if (profile != null) {
            String profileName = profile.name().orElse("");
            UUID profileId = profile.id().orElse(null);

            var localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null && ((profileId != null && profileId.equals(localPlayer.getUUID()))
                    || (!profileName.isEmpty() && profileName.equalsIgnoreCase(localPlayer.getGameProfile().getName())))) {
                skinTexture = localPlayer.getSkin().texture();
            }

            if (skinTexture == null) {
                if (profileName.equalsIgnoreCase("LanderAlo")) {
                    skinTexture = LANDER_TEXTURE;
                } else if (profileName.equalsIgnoreCase("Schvis") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.SCHVIS_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/schvisplushie.png");
                } else if (profileName.toLowerCase().startsWith("techidna") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.TECHIDNA_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/techidnaplushie.png");
                } else if (profileName.toLowerCase().startsWith("tomcraft") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.TOMCRAFT_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/tomcraftplushie.png");
                } else if (profileName.equalsIgnoreCase("Nandachan") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.NANDACHAN_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/nandachanplushie.png");
                } else if (profileName.equalsIgnoreCase("MisticPotato") || profileName.equalsIgnoreCase("Potato") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.MISTIC_POTATO_UUID))) {
                    skinTexture = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/misticpotatoplushie.png");
                }
            }

            if (skinTexture == null) {
                try {
                    var insecureSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile.gameProfile());
                    if (insecureSkin != null && insecureSkin.texture() != null) {
                        skinTexture = insecureSkin.texture();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (skinTexture == null) {
            var localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                skinTexture = localPlayer.getSkin().texture();
            } else if (profile != null && profile.id().isPresent()) {
                skinTexture = DefaultPlayerSkin.get(profile.id().get()).texture();
            } else {
                skinTexture = DefaultPlayerSkin.getDefaultTexture();
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);

        Direction facing = state.getValue(CustomPlayerPlushieBlock.FACING);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));

        float squashTicks = blockEntity.getSquashTicks(partialTick);
        if (squashTicks > 0.0F) {
            float progress = squashTicks / 10.0F;
            float squash = Mth.sin(progress * (float) Math.PI);
            poseStack.scale(1.0F + squash * 0.32F, 1.0F - squash * 0.42F, 1.0F + squash * 0.32F);
        }

        float scale = 0.88F;
        poseStack.scale(-scale, -scale, scale);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
        this.model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }
}
