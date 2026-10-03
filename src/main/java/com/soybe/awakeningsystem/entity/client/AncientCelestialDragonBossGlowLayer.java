package com.soybe.awakeningsystem.entity.client;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.entity.AncientCelestialDragonBossEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Capa emisiva del Dragón Celestial Ancestral Titánico (Boss):
 * Ojos dorados feroces, runas arcanas de Qi ancestral, venas de relámpago a lo largo de las 64 vértebras
 * y la punta llameante de la cola resplandecen en plena tormenta y oscuridad.
 */
public class AncientCelestialDragonBossGlowLayer extends EyesLayer<AncientCelestialDragonBossEntity, AncientCelestialDragonBossModel<AncientCelestialDragonBossEntity>> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/ancient_dragon_boss_glow.png"));

    public AncientCelestialDragonBossGlowLayer(RenderLayerParent<AncientCelestialDragonBossEntity, AncientCelestialDragonBossModel<AncientCelestialDragonBossEntity>> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }
}
