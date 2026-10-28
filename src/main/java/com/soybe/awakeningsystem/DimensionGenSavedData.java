package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class DimensionGenSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_dimension_gen_data";
    private boolean isekaiTreeGenerated = false;
    private boolean murimSectGenerated = false;
    private boolean limboSanctuaryGenerated = false;
    private int limboCardinalVersion = 0;
    private boolean towerFloor10BossDefeated = false;
    private boolean towerFloor20BossDefeated = false;
    private long bannerEpochStartTick = 0L;

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
        data.limboSanctuaryGenerated = tag.getBoolean("LimboSanctuaryGenerated");
        data.limboCardinalVersion = tag.contains("LimboCardinalVersion") ? tag.getInt("LimboCardinalVersion") : 0;
        data.towerFloor10BossDefeated = tag.getBoolean("TowerFloor10BossDefeated");
        data.towerFloor20BossDefeated = tag.getBoolean("TowerFloor20BossDefeated");
        data.bannerEpochStartTick = tag.contains("BannerEpochStartTick") ? tag.getLong("BannerEpochStartTick") : 0L;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("IsekaiTreeGenerated", isekaiTreeGenerated);
        tag.putBoolean("MurimSectGenerated", murimSectGenerated);
        tag.putBoolean("LimboSanctuaryGenerated", limboSanctuaryGenerated);
        tag.putInt("LimboCardinalVersion", limboCardinalVersion);
        tag.putBoolean("TowerFloor10BossDefeated", towerFloor10BossDefeated);
        tag.putBoolean("TowerFloor20BossDefeated", towerFloor20BossDefeated);
        tag.putLong("BannerEpochStartTick", bannerEpochStartTick);
        return tag;
    }

    public boolean isTowerFloor10BossDefeated() {
        return towerFloor10BossDefeated;
    }

    public void setTowerFloor10BossDefeated(boolean val) {
        this.towerFloor10BossDefeated = val;
        setDirty();
    }

    public boolean isTowerFloor20BossDefeated() {
        return towerFloor20BossDefeated;
    }

    public void setTowerFloor20BossDefeated(boolean val) {
        this.towerFloor20BossDefeated = val;
        setDirty();
    }

    public long getBannerEpochStartTick() {
        return bannerEpochStartTick;
    }

    public void setBannerEpochStartTick(long tick) {
        this.bannerEpochStartTick = tick;
        setDirty();
    }

    public boolean isIsekaiTreeGenerated() {
        return isekaiTreeGenerated;
    }

    public void setIsekaiTreeGenerated(boolean val) {
        this.isekaiTreeGenerated = val;
        setDirty();
    }

    public boolean isLimboSanctuaryGenerated() {
        return limboSanctuaryGenerated && limboCardinalVersion >= 4;
    }

    public void setLimboSanctuaryGenerated(boolean val) {
        this.limboSanctuaryGenerated = val;
        if (val) this.limboCardinalVersion = 4;
        setDirty();
    }

    public int getLimboCardinalVersion() {
        return limboCardinalVersion;
    }

    public void setLimboCardinalVersion(int val) {
        this.limboCardinalVersion = val;
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
