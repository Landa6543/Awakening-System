package com.soybe.awakeningsystem.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor centralizado de integración con Xaero's Minimap y Xaero's World Map.
 * Almacena la caché de claims sincronizados en cliente, genera paletas de colores únicas
 * por gremio y orquesta el refresco en caliente de los renders de mapas.
 */
public final class XaeroGuildMapIntegration {

    private static final Map<String, String> CHUNK_TO_GUILD = new ConcurrentHashMap<>();
    private static final Set<String> CLAIMED_REGIONS = ConcurrentHashMap.newKeySet();
    private static final Map<String, CompoundTag> GUILD_INFO = new ConcurrentHashMap<>();
    private static int claimsVersion = 1;

    private XaeroGuildMapIntegration() {}

    /**
     * Actualiza la base de datos de chunks reclamados a partir del CompoundTag recibido del servidor.
     */
    public static void updateClaims(CompoundTag claimsTag) {
        if (claimsTag == null) return;

        CHUNK_TO_GUILD.clear();
        CLAIMED_REGIONS.clear();
        GUILD_INFO.clear();

        if (claimsTag.contains("_Guilds")) {
            CompoundTag guildsCompound = claimsTag.getCompound("_Guilds");
            for (String gKey : guildsCompound.getAllKeys()) {
                GUILD_INFO.put(gKey, guildsCompound.getCompound(gKey));
            }
        }

        for (String key : claimsTag.getAllKeys()) {
            if (key.startsWith("_")) continue;
            String guildTag = claimsTag.getString(key);
            CHUNK_TO_GUILD.put(key, guildTag);

            int firstHash = key.indexOf('#');
            int lastHash = key.lastIndexOf('#');
            if (firstHash > 0 && lastHash > firstHash) {
                try {
                    String dim = key.substring(0, firstHash);
                    int cx = Integer.parseInt(key.substring(firstHash + 1, lastHash));
                    int cz = Integer.parseInt(key.substring(lastHash + 1));
                    CLAIMED_REGIONS.add(dim + "#" + (cx >> 5) + "#" + (cz >> 5));
                } catch (Exception ignored) {}
            }
        }

        claimsVersion++;
    }

    public static boolean hasRegionClaims(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (dimension == null) return false;
        return CLAIMED_REGIONS.contains(dimension.location().toString() + "#" + regionX + "#" + regionZ);
    }

    public static boolean isChunkClaimed(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (dimension == null) return false;
        return CHUNK_TO_GUILD.containsKey(dimension.location().toString() + "#" + chunkX + "#" + chunkZ);
    }

    public static int getRegionClaimsHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!hasRegionClaims(dimension, regionX, regionZ)) return 0;
        return claimsVersion ^ (regionX * 31 + regionZ);
    }

    public static int getGuildColor(String idOrTag) {
        if (idOrTag == null || idOrTag.isEmpty()) return 0x3498DB;
        CompoundTag info = GUILD_INFO.get(idOrTag);
        if (info != null && info.contains("color")) {
            return info.getInt("color");
        }
        int hash = Math.abs(idOrTag.hashCode());
        float hue = (hash % 360) / 360.0f;
        return Color.HSBtoRGB(hue, 0.75f, 0.90f) & 0xFFFFFF;
    }

    public static int[] computeChunkColors(int[] resultStore, ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (dimension == null || resultStore == null || resultStore.length < 5) return null;
        String dimStr = dimension.location().toString();
        String myGuild = CHUNK_TO_GUILD.get(dimStr + "#" + chunkX + "#" + chunkZ);
        if (myGuild == null) return null;

        int rgb = getGuildColor(myGuild);
        int fillAlpha = 0x50;
        int borderAlpha = 0xD0;
        int fillColor = (fillAlpha << 24) | (rgb & 0xFFFFFF);
        int borderColor = (borderAlpha << 24) | (rgb & 0xFFFFFF);

        String north = CHUNK_TO_GUILD.get(dimStr + "#" + chunkX + "#" + (chunkZ - 1));
        String east  = CHUNK_TO_GUILD.get(dimStr + "#" + (chunkX + 1) + "#" + chunkZ);
        String south = CHUNK_TO_GUILD.get(dimStr + "#" + chunkX + "#" + (chunkZ + 1));
        String west  = CHUNK_TO_GUILD.get(dimStr + "#" + (chunkX - 1) + "#" + chunkZ);

        resultStore[0] = fillColor;
        resultStore[1] = Objects.equals(north, myGuild) ? fillColor : borderColor;
        resultStore[2] = Objects.equals(east, myGuild)  ? fillColor : borderColor;
        resultStore[3] = Objects.equals(south, myGuild) ? fillColor : borderColor;
        resultStore[4] = Objects.equals(west, myGuild)  ? fillColor : borderColor;

        return resultStore;
    }

    public static Component getHighlightTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (dimension == null) return null;
        String guildTag = CHUNK_TO_GUILD.get(dimension.location().toString() + "#" + chunkX + "#" + chunkZ);
        if (guildTag == null) return null;

        CompoundTag info = GUILD_INFO.get(guildTag);
        String name = (info != null && info.contains("name")) ? info.getString("name") : guildTag;
        String tag = (info != null && info.contains("tag")) ? info.getString("tag") : guildTag;
        int level = (info != null && info.contains("level")) ? info.getInt("level") : 1;

        return Component.literal("§6🛡️ [Guild] §f" + name + " §7[§b" + tag + "§7] §e(Lv. " + level + ")");
    }

    public static void addMinimapTooltips(xaero.hud.minimap.info.render.compile.InfoDisplayCompiler compiler,
                                          ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        Component tip = getHighlightTooltip(dimension, chunkX, chunkZ);
        if (tip != null && compiler != null) {
            compiler.addLine(tip);
        }
    }

    public static void tick() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        if (ModList.get().isLoaded("xaerominimap")) {
            MinimapHook.tick();
        }
        if (ModList.get().isLoaded("xaeroworldmap")) {
            WorldMapHook.tick();
        }
    }

    public static void requestRefresh() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        if (ModList.get().isLoaded("xaerominimap")) {
            MinimapHook.refresh();
        }
        if (ModList.get().isLoaded("xaeroworldmap")) {
            WorldMapHook.refresh();
        }
    }

    private static class MinimapHook {
        private static Object registeredSession = null;

        static void tick() {
            try {
                xaero.common.XaeroMinimapSession session = xaero.common.XaeroMinimapSession.getCurrentSession();
                if (session != null && session != registeredSession) {
                    xaero.common.minimap.MinimapProcessor proc = session.getMinimapProcessor();
                    if (proc != null) {
                        xaero.common.minimap.write.MinimapWriter writer = proc.getMinimapWriter();
                        if (writer != null) {
                            Field f = xaero.common.minimap.write.MinimapWriter.class.getDeclaredField("highlighterRegistry");
                            f.setAccessible(true);
                            xaero.common.minimap.highlight.HighlighterRegistry reg =
                                    (xaero.common.minimap.highlight.HighlighterRegistry) f.get(writer);
                            if (reg != null) {
                                boolean found = false;
                                for (xaero.common.minimap.highlight.AbstractHighlighter h : reg.getHighlighters()) {
                                    if (h instanceof XaeroMinimapGuildHighlighter) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    reg.register(new XaeroMinimapGuildHighlighter());
                                    registeredSession = session;
                                    refresh();
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        static void refresh() {
            try {
                xaero.common.XaeroMinimapSession session = xaero.common.XaeroMinimapSession.getCurrentSession();
                if (session != null) {
                    xaero.common.minimap.MinimapProcessor proc = session.getMinimapProcessor();
                    if (proc != null) {
                        xaero.common.minimap.write.MinimapWriter writer = proc.getMinimapWriter();
                        if (writer != null && writer.getDimensionHighlightHandler() != null) {
                            writer.getDimensionHighlightHandler().requestRefresh();
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private static class WorldMapHook {
        private static Object registeredSession = null;

        static void tick() {
            try {
                xaero.map.WorldMapSession session = xaero.map.WorldMapSession.getCurrentSession();
                if (session != null && session != registeredSession) {
                    xaero.map.MapProcessor proc = session.getMapProcessor();
                    if (proc != null) {
                        xaero.map.highlight.HighlighterRegistry reg = proc.getHighlighterRegistry();
                        if (reg != null) {
                            boolean found = false;
                            for (xaero.map.highlight.AbstractHighlighter h : reg.getHighlighters()) {
                                if (h instanceof XaeroWorldMapGuildHighlighter) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                reg.register(new XaeroWorldMapGuildHighlighter());
                                registeredSession = session;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        static void refresh() {
        }
    }
}
