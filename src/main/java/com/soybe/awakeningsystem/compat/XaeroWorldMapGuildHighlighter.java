package com.soybe.awakeningsystem.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;

/**
 * Renderizador de chunks de gremios para Xaero's World Map (Pantalla completa del mapa).
 * Controla hash de región, coloreado poligonal con fusión de bordes internos y tooltips al pasar el ratón.
 */
public class XaeroWorldMapGuildHighlighter extends ChunkHighlighter {

    public XaeroWorldMapGuildHighlighter() {
        super(true);
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return XaeroGuildMapIntegration.getRegionClaimsHash(dimension, regionX, regionZ);
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return XaeroGuildMapIntegration.hasRegionClaims(dimension, regionX, regionZ);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaeroGuildMapIntegration.isChunkClaimed(dimension, chunkX, chunkZ);
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaeroGuildMapIntegration.computeChunkColors(this.resultStore, dimension, chunkX, chunkZ);
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaeroGuildMapIntegration.getHighlightTooltip(dimension, chunkX, chunkZ);
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaeroGuildMapIntegration.getHighlightTooltip(dimension, chunkX, chunkZ);
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int chunkX, int chunkZ, int width) {
        Component tip = XaeroGuildMapIntegration.getHighlightTooltip(dimension, chunkX, chunkZ);
        if (tip != null && list != null) {
            list.add(tip);
        }
    }
}
