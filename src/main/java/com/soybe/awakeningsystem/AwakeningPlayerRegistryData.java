package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Almacenamiento persistente en el servidor para registros globales de jugadores.
 * Garantiza que recompensas únicas (como la Guía del Cazador inicial) NUNCA se dupliquen,
 * incluso si el jugador arroja el ítem, lo guarda en cofres, cambia de dimensión o reconecta.
 */
public class AwakeningPlayerRegistryData extends SavedData {
    public static final String DATA_NAME = "awakening_player_registry";
    private final Set<UUID> playersReceivedHunterGuide = new HashSet<>();

    public AwakeningPlayerRegistryData() {}

    public static AwakeningPlayerRegistryData get(MinecraftServer server) {
        if (server == null || server.overworld() == null) {
            return new AwakeningPlayerRegistryData();
        }
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(AwakeningPlayerRegistryData::new, AwakeningPlayerRegistryData::load, null),
                DATA_NAME
        );
    }

    public static AwakeningPlayerRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        AwakeningPlayerRegistryData data = new AwakeningPlayerRegistryData();
        if (tag.contains("ReceivedHunterGuide", Tag.TAG_LIST)) {
            ListTag list = tag.getList("ReceivedHunterGuide", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    data.playersReceivedHunterGuide.add(UUID.fromString(list.getString(i)));
                } catch (Exception ignored) {}
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (UUID uuid : playersReceivedHunterGuide) {
            list.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("ReceivedHunterGuide", list);
        return tag;
    }

    public boolean hasReceivedHunterGuide(UUID playerUuid) {
        if (playerUuid == null) return false;
        return playersReceivedHunterGuide.contains(playerUuid);
    }

    public void markReceivedHunterGuide(UUID playerUuid) {
        if (playerUuid == null) return;
        playersReceivedHunterGuide.add(playerUuid);
        setDirty();
    }
}
