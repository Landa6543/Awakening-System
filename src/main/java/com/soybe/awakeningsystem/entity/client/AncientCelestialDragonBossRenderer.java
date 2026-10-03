package com.soybe.awakeningsystem.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderizador del Dragón Celestial Ancestral Titánico (Boss):
 * - Escala masiva (4.2F) para un cuerpo robusto de más de 100 bloques de estela serpentina.
 * - Anti-culling total con frustum extendido de 180 bloques.
 * - Capa emisiva resplandeciente en tormenta y oscuridad.
 */
public class AncientCelestialDragonBossRenderer extends MobRenderer<AncientCelestialDragonBossEntity, AncientCelestialDragonBossModel<AncientCelestialDragonBossEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon_boss.png");

    public AncientCelestialDragonBossRenderer(EntityRendererProvider.Context context) {
        super(context, new AncientCelestialDragonBossModel<>(context.bakeLayer(AncientCelestialDragonBossModel.LAYER_LOCATION)), 5.0F);
        this.addLayer(new AncientCelestialDragonBossGlowLayer(this));
    }

    @Override
    protected void scale(AncientCelestialDragonBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(4.2F, 4.2F, 4.2F);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public boolean shouldRender(AncientCelestialDragonBossEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        if (super.shouldRender(entity, frustum, camX, camY, camZ)) {
            return true;
        }
        return frustum.isVisible(entity.getRenderBoundingBox());
    }

    @Override
    public ResourceLocation getTextureLocation(AncientCelestialDragonBossEntity entity) {
        return TEXTURE;
    }
}
