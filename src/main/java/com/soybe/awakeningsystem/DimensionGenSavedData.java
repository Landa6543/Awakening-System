package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class DimensionGenSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_dimension_gen_data";
    private boolean isekaiTreeGenerated = false;
    private boolean murimSectGenerated = false;

    public static DimensionGenSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(DimensionGenSavedData::new, DimensionGenSavedData::load, null),
                DATA_NAME
        );
    }

    public static DimensionGenSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DimensionGenSavedData data = new DimensionGenSavedData();
        data.isekaiTreeGenerated = tag.getBoolean("IsekaiTreeGenerated");
        data.murimSectGenerated = tag.getBoolean("MurimSectGenerated");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("IsekaiTreeGenerated", isekaiTreeGenerated);
        tag.putBoolean("MurimSectGenerated", murimSectGenerated);
        return tag;
    }

    public boolean isIsekaiTreeGenerated() {
        return isekaiTreeGenerated;
    }

    public void setIsekaiTreeGenerated(boolean val) {
        this.isekaiTreeGenerated = val;
        setDirty();
    }

    public boolean isMurimSectGenerated() {
        return murimSectGenerated;
    }

    public void setMurimSectGenerated(boolean val) {
        this.murimSectGenerated = val;
        setDirty();
    }
}
