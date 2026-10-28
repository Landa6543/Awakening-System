package com.soybe.awakeningsystem.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.common.minimap.highlight.ChunkHighlighter;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;

/**
 * Renderizador de chunks de gremios para Xaero's Minimap.
 * Aplica colores personalizados por gremio (interior semitransparente ~32% y bordes exteriores gruesos ~82%),
 * fusionando chunks contiguos del mismo gremio y mostrando tooltips de estado.
 */
public class XaeroMinimapGuildHighlighter extends ChunkHighlighter {

    public XaeroMinimapGuildHighlighter() {
        super(true);
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
    public void addChunkHighlightTooltips(InfoDisplayCompiler compiler, ResourceKey<Level> dimension, int chunkX, int chunkZ, int width) {
        XaeroGuildMapIntegration.addMinimapTooltips(compiler, dimension, chunkX, chunkZ);
    }
}
