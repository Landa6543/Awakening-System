package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemClient;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ShadowRank;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.entity.ShadowSoulEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ShadowSoulRenderer extends EntityRenderer<ShadowSoulEntity> {
    public ShadowSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowSoulEntity entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    protected void renderNameTag(ShadowSoulEntity entity, Component displayName, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, float partialTick) {
        Component dynamicName = displayName;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            SystemData data = mc.player.getData(AwakeningSystemMod.PLAYER_SYSTEM);
            String boundKey = null;
            if (data != null) {
                for (int i = 0; i < 5; i++) {
                    String sk = data.getEquippedActiveSkill(i);
                    if ("shadow_monarch".equals(sk) || "unique_absolute_extraction".equals(sk)) {
                        KeyMapping km = switch (i) {
                            case 0 -> AwakeningSystemClient.ACTIVATE_SKILL;
                            case 1 -> AwakeningSystemClient.SKILL_SLOT_2;
                            case 2 -> AwakeningSystemClient.SKILL_SLOT_3;
                            case 3 -> AwakeningSystemClient.SKILL_SLOT_4;
                            case 4 -> AwakeningSystemClient.SKILL_SLOT_5;
                            default -> null;
                        };
                        if (km != null) {
                            boundKey = km.getTranslatedKeyMessage().getString();
                        }
                        break;
                    }
                }
            }

            ShadowRank r = ShadowRank.byLevel(entity.getShadowRank());
            if (boundKey != null && !boundKey.isBlank()) {
                dynamicName = Component.translatable("awakening_system.shadow.soul_name_prompt_key", r.getDisplayName(), entity.getMobNameComponent(), boundKey);
            } else {
                dynamicName = Component.translatable("awakening_system.shadow.soul_name_prompt", r.getDisplayName(), entity.getMobNameComponent());
            }
        }
        super.renderNameTag(entity, dynamicName, poseStack, bufferSource, packedLight, partialTick);
    }
}
