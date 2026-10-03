package com.soybe.awakeningsystem.fable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FableWorldSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_fable_world_data";

    private final Map<String, UUID> worldFirstHolders = new HashMap<>();
    private final Map<String, String> worldFirstPlayerNames = new HashMap<>();

    public FableWorldSavedData() {}

    public static FableWorldSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(FableWorldSavedData::new, FableWorldSavedData::load, null),
                DATA_NAME
        );
    }

    public static FableWorldSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FableWorldSavedData data = new FableWorldSavedData();
        if (tag.contains("WorldFirsts", Tag.TAG_LIST)) {
            ListTag list = tag.getList("WorldFirsts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                String fableId = entry.getString("FableId");
                UUID uuid = entry.getUUID("UUID");
                String name = entry.getString("PlayerName");
                data.worldFirstHolders.put(fableId, uuid);
                data.worldFirstPlayerNames.put(fableId, name);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<String, UUID> entry : worldFirstHolders.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putString("FableId", entry.getKey());
            c.putUUID("UUID", entry.getValue());
            c.putString("PlayerName", worldFirstPlayerNames.getOrDefault(entry.getKey(), "Desconocido"));
            list.add(c);
        }
        tag.put("WorldFirsts", list);
        return tag;
    }

    public synchronized boolean tryClaimWorldFirst(String fableId, UUID playerUuid, String playerName) {
        if (worldFirstHolders.containsKey(fableId)) {
            return false; // Ya fue reclamada por otro jugador
        }
        worldFirstHolders.put(fableId, playerUuid);
        worldFirstPlayerNames.put(fableId, playerName);
        setDirty();
        return true;
    }

    public boolean isWorldFirstClaimed(String fableId) {
        return worldFirstHolders.containsKey(fableId);
    }

    public UUID getWorldFirstHolder(String fableId) {
        return worldFirstHolders.get(fableId);
    }

    public String getWorldFirstPlayerName(String fableId) {
        return worldFirstPlayerNames.getOrDefault(fableId, "Desconocido");
    }
}
