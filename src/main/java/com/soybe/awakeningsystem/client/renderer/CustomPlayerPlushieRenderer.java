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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomPlayerPlushieRenderer implements BlockEntityRenderer<CustomPlayerPlushieBlockEntity> {
    private static final ResourceLocation LANDER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/landeralo_skin.png");
    private static final Map<String, ResolvableProfile> RESOLVED_PROFILES = new ConcurrentHashMap<>();
    private static final Set<String> RESOLVING_KEYS = ConcurrentHashMap.newKeySet();

    private final CustomPlushieModel model;

    public CustomPlayerPlushieRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CustomPlushieModel(context.bakeLayer(CustomPlushieModel.LAYER_LOCATION));
    }

    /**
     * Resolves a player plushie's skin in real time: checks local player and online player list first,
     * then async-resolved Mojang/server profiles via SkinManager, falling back to bundled textures only
     * if no live skin is available.
     */
    public static ResourceLocation resolveLiveSkin(ResolvableProfile profile) {
        return resolveLiveSkin(profile, null);
    }

    public static ResourceLocation resolveLiveSkin(ResolvableProfile profile, ResourceLocation explicitFallback) {
        Minecraft mc = Minecraft.getInstance();
        if (profile != null) {
            String profileName = profile.name().orElse("");
            UUID profileId = profile.id().orElse(null);

            var localPlayer = mc.player;
            if (localPlayer != null && ((profileId != null && profileId.equals(localPlayer.getUUID()))
                    || (!profileName.isEmpty() && profileName.equalsIgnoreCase(localPlayer.getGameProfile().getName())))) {
                return localPlayer.getSkin().texture();
            }

            if (mc.getConnection() != null) {
                for (var info : mc.getConnection().getOnlinePlayers()) {
                    var gp = info.getProfile();
                    if ((profileId != null && profileId.equals(gp.getId()))
                            || (!profileName.isEmpty() && profileName.equalsIgnoreCase(gp.getName()))) {
                        return info.getSkin().texture();
                    }
                }
            }

            String cacheKey = profileId != null ? profileId.toString() : profileName.toLowerCase(java.util.Locale.ROOT);
            ResolvableProfile effective = profile;
            if (!cacheKey.isEmpty()) {
                ResolvableProfile cached = RESOLVED_PROFILES.get(cacheKey);
                if (cached != null) {
                    effective = cached;
                } else if (!profile.properties().containsKey("textures") && RESOLVING_KEYS.add(cacheKey)) {
                    java.util.concurrent.CompletableFuture<java.util.Optional<com.mojang.authlib.GameProfile>> lookup;
                    if (profileId != null) {
                        lookup = net.minecraft.world.level.block.entity.SkullBlockEntity.fetchGameProfile(profileId)
                                .thenCompose(opt -> {
                                    if (opt.isPresent() && opt.get().getProperties().containsKey("textures")) {
                                        return java.util.concurrent.CompletableFuture.completedFuture(opt);
                                    }
                                    if (!profileName.isEmpty()) {
                                        return net.minecraft.world.level.block.entity.SkullBlockEntity.fetchGameProfile(profileName);
                                    }
                                    return java.util.concurrent.CompletableFuture.completedFuture(opt);
                                });
                    } else {
                        lookup = net.minecraft.world.level.block.entity.SkullBlockEntity.fetchGameProfile(profileName);
                    }
                    lookup.thenAccept(opt -> opt.ifPresent(gp -> {
                        if (gp.getProperties().containsKey("textures")) {
                            RESOLVED_PROFILES.put(cacheKey, new ResolvableProfile(gp));
                        }
                    }));
                }
            }

            try {
                var gp = effective.gameProfile();
                var insecureSkin = mc.getSkinManager().getInsecureSkin(gp);
                if (insecureSkin != null && insecureSkin.texture() != null) {
                    ResourceLocation defTex = DefaultPlayerSkin.get(gp.getId()).texture();
                    if (!insecureSkin.texture().equals(defTex)) {
                        return insecureSkin.texture();
                    }
                }
            } catch (Exception ignored) {
            }

            if (explicitFallback != null) {
                return explicitFallback;
            }

            if (profileName.equalsIgnoreCase("LanderAlo")) {
                return LANDER_TEXTURE;
            } else if (profileName.equalsIgnoreCase("Schvis") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.SCHVIS_UUID))) {
                return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/schvisplushie.png");
            } else if (profileName.toLowerCase().startsWith("techidna") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.TECHIDNA_UUID))) {
                return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/techidnaplushie.png");
            } else if (profileName.toLowerCase().startsWith("tomcraft") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.TOMCRAFT_UUID))) {
                return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/tomcraftplushie.png");
            } else if (profileName.equalsIgnoreCase("Nandachan") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.NANDACHAN_UUID))) {
                return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/nandachanplushie.png");
            } else if (profileName.equalsIgnoreCase("MisticPotato") || profileName.equalsIgnoreCase("Potato") || (profileId != null && profileId.equals(CustomPlayerPlushieBlock.MISTIC_POTATO_UUID))) {
                return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/block/misticpotatoplushie.png");
            }

            if (profileId != null) {
                return DefaultPlayerSkin.get(profileId).texture();
            }
        }

        if (explicitFallback != null) {
            return explicitFallback;
        }
        if (mc.player != null) {
            return mc.player.getSkin().texture();
        }
        return DefaultPlayerSkin.getDefaultTexture();
    }

    @Override
    public void render(CustomPlayerPlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (state.isAir() || !(state.getBlock() instanceof CustomPlayerPlushieBlock)) return;

        ResourceLocation skinTexture = resolveLiveSkin(blockEntity.getOwnerProfile());

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
