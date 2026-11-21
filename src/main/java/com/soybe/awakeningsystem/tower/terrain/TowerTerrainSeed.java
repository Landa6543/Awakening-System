package com.soybe.awakeningsystem.tower.terrain;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gives each world its own tower floors: the floors' land keeps its style but takes a twist from the world seed.
 * The twist is written in the world folder (awakening_tower_terrain.txt) the first time, so it never changes for a
 * world. A world whose floors were made before this keeps the old land (twist 0), or new chunks would not match the
 * ones already there.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class TowerTerrainSeed {
    private TowerTerrainSeed() {}

    @SubscribeEvent
    public static void aboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        long salt = 0L;
        try {
            Path root = server.getWorldPath(LevelResource.ROOT);
            Path file = root.resolve("awakening_tower_terrain.txt");
            if (Files.exists(file)) {
                salt = Long.parseLong(Files.readString(file).trim());
            } else {
                Path dims = root.resolve("dimensions").resolve(AwakeningSystemMod.MOD_ID);
                boolean oldFloors = false;
                if (Files.isDirectory(dims)) {
                    try (var list = Files.list(dims)) {
                        oldFloors = list.anyMatch(p -> p.getFileName().toString().startsWith("tower_floor_"));
                    }
                }
                if (!oldFloors) {
                    long seed = server.getWorldData().worldGenOptions().seed();
                    salt = seed * 0x9E3779B97F4A7C15L;
                    salt ^= salt >>> 29;
                    if (salt == 0L) salt = 1L;
                }
                Files.writeString(file, Long.toString(salt));
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.warn("Tower terrain twist could not be read or saved, using the default land", e);
            salt = 0L;
        }
        TowerFloorTerrain.setWorldSalt(salt);
        AwakeningSystemMod.LOGGER.info("[Tower] floor terrain twist {}", salt);
    }
}
