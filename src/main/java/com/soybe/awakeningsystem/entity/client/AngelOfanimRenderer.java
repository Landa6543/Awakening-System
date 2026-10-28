package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.entity.LimboOphanimEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Renderer GeckoLib para el Ángel Ofanim del Limbo.
 * Se renderiza con iluminación total celestial (fullbright) para que sus ojos,
 * aros dorados y núcleo brillen con esplendor divino en la oscuridad del Limbo.
 */
public class AngelOfanimRenderer extends GeoEntityRenderer<LimboOphanimEntity> {

    private static final Set<LimboOphanimEntity> PLAYING_SOUND_ENTITIES =
            Collections.newSetFromMap(new WeakHashMap<>());

    public AngelOfanimRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AngelOfanimGeoModel());
        this.shadowRadius = 0.9F;
    }

    @Override
    protected int getBlockLightLevel(LimboOphanimEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(LimboOphanimEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(LimboOphanimEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ensureAmbientSound(entity);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, LightTexture.FULL_BRIGHT);
    }

    public static void ensureAmbientSound(LimboOphanimEntity entity) {
        if (entity != null && entity.isAlive() && !entity.isRemoved()) {
            synchronized (PLAYING_SOUND_ENTITIES) {
                if (!PLAYING_SOUND_ENTITIES.contains(entity)) {
                    PLAYING_SOUND_ENTITIES.add(entity);
                    Minecraft.getInstance().getSoundManager().play(new OphanimAmbientSoundInstance(entity));
                }
            }
        }
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, LimboOphanimEntity animatable, software.bernie.geckolib.cache.object.BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float scale = 2.2F; // Escala imponente de ángel trono celestial
        super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    protected float getDeathMaxRotation(LimboOphanimEntity animatable) {
        return 0.0F;
    }
}
