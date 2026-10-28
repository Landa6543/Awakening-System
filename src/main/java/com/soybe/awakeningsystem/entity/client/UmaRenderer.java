package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.uma.UmaRacerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador de entidades de corredoras Uma Musume.
 * Aplica texturas por corredora, renderiza etiquetas con arquetipo táctico / puesto
 * y controla efectos dinámicos de sprint.
 */
public class UmaRenderer extends MobRenderer<UmaRacerEntity, UmaModel> {

    private static final ResourceLocation DEFAULT_TRACEN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/uma/tracen_default.png");

    private static final java.util.Map<com.soybe.awakeningsystem.uma.UmaProfile, ResourceLocation> TEXTURE_CACHE =
            new java.util.EnumMap<>(com.soybe.awakeningsystem.uma.UmaProfile.class);

    public UmaRenderer(EntityRendererProvider.Context context) {
        super(context, new UmaModel(context.bakeLayer(UmaModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(UmaRacerEntity entity) {
        com.soybe.awakeningsystem.uma.UmaProfile profile = entity.getUmaProfile();
        if (profile == null) return DEFAULT_TRACEN_TEXTURE;
        ResourceLocation cached = TEXTURE_CACHE.get(profile);
        if (cached != null) return cached;
        ResourceLocation loc = profile.getTextureLocation();
        try {
            if (net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(loc).isPresent()) {
                TEXTURE_CACHE.put(profile, loc);
                return loc;
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Resource not found or error loading Uma texture {}: {}", loc, e.getMessage());
        }
        return DEFAULT_TRACEN_TEXTURE;
    }

    @Override
    protected void renderNameTag(UmaRacerEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        double distSq = this.entityRenderDispatcher.distanceToSqr(entity);

        if (entity.isFinished()) {
            int rank = entity.getFinishRank();
            String medal = rank == 1 ? "§6🥇 " : (rank == 2 ? "§f🥈 " : (rank == 3 ? "§c🥉 " : "§7#" + rank + " "));
            Component customTag = Component.literal(medal + entity.getUmaProfile().getDisplayName());
            super.renderNameTag(entity, customTag, poseStack, bufferSource, packedLight, partialTick);
        } else if (entity.isRaceRunning()) {
            Component raceTag;
            if (entity.isSkillActive()) {
                raceTag = Component.literal("§6⚡ 『" + entity.getSkillName() + "』 ⚡ §e" + entity.getUmaProfile().getDisplayName());
            } else {
                raceTag = Component.literal("§e" + entity.getUmaProfile().getDisplayName() + " §7[" + entity.getUmaProfile().getTactique() + "]");
            }
            super.renderNameTag(entity, raceTag, poseStack, bufferSource, packedLight, partialTick);
        } else {
            if (distSq > 36.0) {
                Component dorsalTag = Component.literal("§6§l#" + (entity.getLaneIndex() + 1));
                super.renderNameTag(entity, dorsalTag, poseStack, bufferSource, packedLight, partialTick);
            } else {
                Component idleTag = Component.literal("§e#" + (entity.getLaneIndex() + 1) + " §f" + entity.getUmaProfile().getDisplayName() + " §a(" + entity.getUmaProfile().getBaseOdds() + "x)");
                super.renderNameTag(entity, idleTag, poseStack, bufferSource, packedLight, partialTick);
            }
        }
    }
}
