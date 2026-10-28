package com.soybe.awakeningsystem.client.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gestor en memoria y persistencia de Waypoints del mod Awakening System.
 * Almacena los marcadores de forma 100% aislada por cada mundo o servidor
 * en config/awakening_waypoints/<world_id>.json (estilo JourneyMap/Xaero's),
 * evitando que los waypoints de una partida anterior aparezcan en mundos nuevos.
 */
public final class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Waypoint> WAYPOINTS = new CopyOnWriteArrayList<>();
    private static String currentWorldId = null;
    private static File waypointsFile = null;

    private WaypointManager() {}

    private static Object lastCheckedLevel = null;

    /**
     * Sincroniza dinámicamente el mundo activo del jugador.
     * Si el jugador cambió de mundo o creó una partida nueva, guarda el mundo anterior
     * y carga exclusivamente los waypoints correspondientes a la partida actual.
     */
    public static synchronized void ensureCurrentWorld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            // Fuera de partida (menú principal)
            if (currentWorldId != null) {
                save();
                WAYPOINTS.clear();
                currentWorldId = null;
                waypointsFile = null;
                lastCheckedLevel = null;
            }
            return;
        }

        if (mc.level == lastCheckedLevel && currentWorldId != null) {
            return;
        }

        String worldId = getWorldIdentifier();
        if (currentWorldId == null || !currentWorldId.equals(worldId)) {
            if (currentWorldId != null && waypointsFile != null) {
                save();
            }
            currentWorldId = worldId;
            loadForWorld(worldId);
        }
        lastCheckedLevel = mc.level;
    }

    public static synchronized void onWorldExit() {
        lastCheckedLevel = null;
        if (currentWorldId != null) {
            save();
            WAYPOINTS.clear();
            currentWorldId = null;
            waypointsFile = null;
        }
    }

    private static void loadForWorld(String worldId) {
        try {
            WAYPOINTS.clear();
            File baseDir = new File(new File(Minecraft.getInstance().gameDirectory, "config"), "awakening_waypoints");
            if (!baseDir.exists()) baseDir.mkdirs();
            waypointsFile = new File(baseDir, worldId + ".json");

            if (waypointsFile.exists()) {
                try (FileReader reader = new FileReader(waypointsFile)) {
                    Type listType = new TypeToken<ArrayList<Waypoint>>() {}.getType();
                    List<Waypoint> loadedList = GSON.fromJson(reader, listType);
                    if (loadedList != null) {
                        for (Waypoint wp : loadedList) {
                            if (wp != null) {
                                if (wp.getDimension() == null || wp.getDimension().isBlank()) {
                                    wp.setDimension("minecraft:overworld");
                                }
                                wp.setEnabled(wp.isEnabled());
                                wp.setShowBeam(wp.isShowBeam());
                                wp.setShowLabel(wp.isShowLabel());
                                wp.setDeathPoint(wp.isDeathPoint());
                                WAYPOINTS.add(wp);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void save() {
        try {
            if (waypointsFile == null) {
                String worldId = currentWorldId != null ? currentWorldId : getWorldIdentifier();
                File baseDir = new File(new File(Minecraft.getInstance().gameDirectory, "config"), "awakening_waypoints");
                if (!baseDir.exists()) baseDir.mkdirs();
                waypointsFile = new File(baseDir, worldId + ".json");
            }

            try (FileWriter writer = new FileWriter(waypointsFile)) {
                GSON.toJson(new ArrayList<>(WAYPOINTS), writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Waypoint> getAllWaypoints() {
        ensureCurrentWorld();
        return Collections.unmodifiableList(WAYPOINTS);
    }

    public static List<Waypoint> getWaypointsForDimension(String dimension) {
        ensureCurrentWorld();
        if (dimension == null) return Collections.emptyList();
        List<Waypoint> result = new ArrayList<>();
        for (Waypoint wp : WAYPOINTS) {
            if (WaypointBeaconRenderer.isDimensionMatching(wp.getDimension(), dimension)) {
                result.add(wp);
            }
        }
        return result;
    }

    public static void addWaypoint(Waypoint waypoint) {
        if (waypoint == null) return;
        ensureCurrentWorld();
        WAYPOINTS.removeIf(w -> w.getId().equals(waypoint.getId()));
        WAYPOINTS.add(waypoint);
        save();
    }

    public static void removeWaypoint(String id) {
        if (id == null) return;
        ensureCurrentWorld();
        WAYPOINTS.removeIf(w -> w.getId().equals(id));
        save();
    }

    public static void updateWaypoint(Waypoint waypoint) {
        if (waypoint == null) return;
        ensureCurrentWorld();
        for (int i = 0; i < WAYPOINTS.size(); i++) {
            if (WAYPOINTS.get(i).getId().equals(waypoint.getId())) {
                WAYPOINTS.set(i, waypoint);
                save();
                return;
            }
        }
        addWaypoint(waypoint);
    }

    public static Waypoint getWaypointById(String id) {
        ensureCurrentWorld();
        for (Waypoint wp : WAYPOINTS) {
            if (wp.getId().equals(id)) return wp;
        }
        return null;
    }

    public static void toggleWaypoint(String id) {
        ensureCurrentWorld();
        Waypoint wp = getWaypointById(id);
        if (wp != null) {
            wp.setEnabled(!wp.isEnabled());
            save();
        }
    }

    private static long lastDeathRecordedTime = 0L;

    public static void recordDeathWaypoint(String dimension, int x, int y, int z) {
        long now = System.currentTimeMillis();
        if (now - lastDeathRecordedTime < 3500L) {
            return;
        }
        lastDeathRecordedTime = now;

        ensureCurrentWorld();

        // 1. Limpiar puntos de muerte anteriores en este mundo
        WAYPOINTS.removeIf(wp -> wp.isDeathPoint() || (wp.getId() != null && wp.getId().startsWith("death_point")));

        // 2. Crear nuevo punto de muerte en rojo carmesí #FF2244 con haz de luz 3D
        String deathName = net.minecraft.network.chat.Component.translatable("awakening_system.waypoint.death_name").getString();
        if (deathName == null || deathName.isBlank()) {
            deathName = "☠ Última Muerte";
        }
        Waypoint deathWp = new Waypoint("death_point", deathName, dimension, x, y, z, 0xFF2244, true, true, true);
        deathWp.setDeathPoint(true);
        WAYPOINTS.add(0, deathWp);
        save();

        // 3. Notificación del Sistema en el chat del jugador
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.gui.getChat().addMessage(net.minecraft.network.chat.Component.translatable("awakening_system.waypoint.death_registered", x, y, z));
        }
    }

    public static String getWorldIdentifier() {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (mc.getSingleplayerServer() != null) {
                String name = mc.getSingleplayerServer().getWorldData().getLevelName();
                return "sp_" + sanitize(name);
            } else if (mc.getCurrentServer() != null) {
                return "mp_" + sanitize(mc.getCurrentServer().ip);
            }
        } catch (Throwable ignored) {}
        return "world_default";
    }

    private static String sanitize(String input) {
        if (input == null || input.isBlank()) return "unknown";
        return input.replace(':', '_').replace('/', '_').replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
