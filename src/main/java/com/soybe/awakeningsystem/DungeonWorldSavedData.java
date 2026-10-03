package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class DungeonWorldSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_dungeon_world_data";

    private int nextRunIndex = 0;

    public DungeonWorldSavedData() {}

    public static DungeonWorldSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(DungeonWorldSavedData::new, DungeonWorldSavedData::load, null),
                DATA_NAME
        );
    }

    public static DungeonWorldSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DungeonWorldSavedData data = new DungeonWorldSavedData();
        data.nextRunIndex = tag.getInt("NextRunIndex");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("NextRunIndex", nextRunIndex);
        return tag;
    }

    public synchronized int getNextRunIndexAndIncrement() {
        int val = nextRunIndex++;
        setDirty();
        return val;
    }
}
