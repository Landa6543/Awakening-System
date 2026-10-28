package com.soybe.awakeningsystem.uma;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soybe.awakeningsystem.block.entity.CustomRacetrackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor de exportación e importación en formato JSON para hipódromos personalizados.
 * Permite guardar, compartir y editar externamente en config/awakening_system/custom_racetracks/
 * los waypoints y propiedades del trazado del circuito.
 */
public final class RacetrackJsonHelper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RacetrackJsonHelper() {}

    public static Path getTracksDirectory() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("awakening_system/custom_racetracks");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dir;
    }

    /**
     * Exporta el trazado actual de la Terminal a un archivo JSON legible y editable.
     */
    public static File exportTrack(CustomRacetrackBlockEntity terminal, ServerLevel level) {
        if (terminal == null) return null;

        List<BlockPos> waypoints = terminal.getWaypoints();
        BlockPos corePos = terminal.getBlockPos();
        String trackName = terminal.getTrackName();
        if (trackName == null || trackName.isBlank()) {
            trackName = "Pista Personalizada";
        }

        JsonObject root = new JsonObject();
        root.addProperty("track_name", trackName);
        root.addProperty("exported_at", LocalDateTime.now().format(DATE_FORMAT));

        JsonObject posObj = new JsonObject();
        posObj.addProperty("x", corePos.getX());
        posObj.addProperty("y", corePos.getY());
        posObj.addProperty("z", corePos.getZ());
        root.add("terminal_core_pos", posObj);

        root.addProperty("total_waypoints", waypoints.size());
        root.addProperty("estimated_length_meters", Math.round(calculateDistance(waypoints) * 10.0) / 10.0);

        JsonArray wpArray = new JsonArray();
        for (int i = 0; i < waypoints.size(); i++) {
            BlockPos p = waypoints.get(i);
            JsonObject wp = new JsonObject();
            wp.addProperty("index", i);
            wp.addProperty("type", i == 0 ? "START_LINE" : (i == waypoints.size() - 1 ? "FINISH_LINE" : "WAYPOINT"));
            wp.addProperty("x", p.getX());
            wp.addProperty("y", p.getY());
            wp.addProperty("z", p.getZ());
            wpArray.add(wp);
        }
        root.add("waypoints", wpArray);

        Path dir = getTracksDirectory();
        String safeName = trackName.toLowerCase().replaceAll("[^a-z0-9_-]", "_") + ".json";
        File file = dir.resolve(safeName).toFile();
        File quickAccessFile = dir.resolve("custom_racetrack.json").toFile();

        try {
            String jsonStr = GSON.toJson(root);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonStr);
            }
            try (FileWriter writer = new FileWriter(quickAccessFile)) {
                writer.write(jsonStr);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Importa un archivo JSON desde config/awakening_system/custom_racetracks/ y actualiza la Terminal.
     */
    public static boolean importTrack(CustomRacetrackBlockEntity terminal, String fileName) {
        if (terminal == null || fileName == null || fileName.isBlank()) return false;

        Path dir = getTracksDirectory();
        String actualName = fileName.endsWith(".json") ? fileName : (fileName + ".json");
        File file = dir.resolve(actualName).toFile();

        if (!file.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("waypoints")) return false;

            if (root.has("track_name")) {
                terminal.setTrackName(root.get("track_name").getAsString());
            }

            JsonArray wpArray = root.getAsJsonArray("waypoints");
            List<BlockPos> loadedPoints = new ArrayList<>();
            for (int i = 0; i < wpArray.size(); i++) {
                JsonObject wp = wpArray.get(i).getAsJsonObject();
                int x = wp.get("x").getAsInt();
                int y = wp.get("y").getAsInt();
                int z = wp.get("z").getAsInt();
                loadedPoints.add(new BlockPos(x, y, z));
            }

            if (!loadedPoints.isEmpty()) {
                terminal.setWaypoints(loadedPoints);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lista todos los archivos de pistas guardados en la carpeta de configuración.
     */
    public static List<String> listSavedTracks() {
        Path dir = getTracksDirectory();
        File[] files = dir.toFile().listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) return Collections.emptyList();

        List<String> names = new ArrayList<>();
        for (File f : files) {
            names.add(f.getName());
        }
        return names;
    }

    private static double calculateDistance(List<BlockPos> points) {
        if (points.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            total += Math.sqrt(points.get(i).distSqr(points.get(i + 1)));
        }
        return total;
    }
}
