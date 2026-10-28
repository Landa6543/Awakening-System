package com.soybe.awakeningsystem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DungeonWorldSavedData extends SavedData {
    public static final String DATA_NAME = "awakening_dungeon_world_data";

    private int nextRunIndex = 0;
    private final List<CompoundTag> savedPermanentRifts = new ArrayList<>();
    private boolean riftsLoaded = false;

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
        if (tag.contains("PermanentRifts", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PermanentRifts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                data.savedPermanentRifts.add(list.getCompound(i));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("NextRunIndex", nextRunIndex);
        ListTag list = new ListTag();
        for (DungeonManager.WorldRift rift : DungeonManager.getPermanentRifts()) {
            CompoundTag rt = new CompoundTag();
            rt.putInt("X", rift.pos.getX());
            rt.putInt("Y", rift.pos.getY());
            rt.putInt("Z", rift.pos.getZ());
            rt.putString("Rank", rift.rank);
            rt.putString("Biome", rift.biome);
            rt.putString("Dimension", rift.dimension.location().toString());
            rt.putBoolean("IsRedGate", rift.isRedGate);
            rt.putBoolean("IsPlayerSummoned", rift.isPlayerSummoned);
            rt.putFloat("Yaw", rift.yaw);
            if (rift.owningGuildId != null) {
                rt.putString("OwningGuild", rift.owningGuildId);
            }
            rt.putInt("RentalFee", rift.rentalFeeMora);
            rt.putBoolean("FeeInCoins", rift.feeInCoins);
            rt.putLong("CooldownUntil", rift.cooldownUntil);

            ListTag rentersList = new ListTag();
            for (Map.Entry<UUID, Long> entry : rift.rentalAccessMap.entrySet()) {
                CompoundTag rTag = new CompoundTag();
                rTag.putUUID("UUID", entry.getKey());
                rTag.putLong("Expiry", entry.getValue());
                rentersList.add(rTag);
            }
            rt.put("Renters", rentersList);

            list.add(rt);
        }
        tag.put("PermanentRifts", list);
        return tag;
    }

    public void ensureRiftsLoaded(MinecraftServer server) {
        if (riftsLoaded) return;
        riftsLoaded = true;
        for (CompoundTag rt : savedPermanentRifts) {
            try {
                BlockPos pos = new BlockPos(rt.getInt("X"), rt.getInt("Y"), rt.getInt("Z"));
                String rank = rt.getString("Rank");
                String biome = rt.getString("Biome");
                ResourceLocation dimLoc = ResourceLocation.parse(rt.getString("Dimension"));
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, dimLoc);
                boolean isRed = rt.getBoolean("IsRedGate");
                float yaw = rt.getFloat("Yaw");

                DungeonManager.WorldRift rift = new DungeonManager.WorldRift(pos, rank, biome, dim, Long.MAX_VALUE, isRed, yaw, true);
                rift.isNewlySpawned = false;
                rift.spawnTimeMs = 0L;
                if (rt.contains("IsPlayerSummoned")) {
                    rift.isPlayerSummoned = rt.getBoolean("IsPlayerSummoned");
                }
                if (rt.contains("OwningGuild")) {
                    rift.owningGuildId = rt.getString("OwningGuild");
                }
                if (rt.contains("RentalFee")) {
                    rift.rentalFeeMora = rt.getInt("RentalFee");
                }
                if (rt.contains("FeeInCoins")) {
                    rift.feeInCoins = rt.getBoolean("FeeInCoins");
                }
                if (rt.contains("CooldownUntil")) {
                    rift.cooldownUntil = rt.getLong("CooldownUntil");
                }
                if (rt.contains("Renters", Tag.TAG_LIST)) {
                    ListTag rentersList = rt.getList("Renters", Tag.TAG_COMPOUND);
                    for (int j = 0; j < rentersList.size(); j++) {
                        CompoundTag rTag = rentersList.getCompound(j);
                        if (rTag.hasUUID("UUID")) {
                            rift.rentalAccessMap.put(rTag.getUUID("UUID"), rTag.getLong("Expiry"));
                        }
                    }
                }
                DungeonManager.registerPermanentRiftDirect(rift);
            } catch (Exception e) {
                AwakeningSystemMod.LOGGER.error("Error al cargar portal permanente desde SavedData", e);
            }
        }
        savedPermanentRifts.clear();
        DungeonManager.enforceRiftSeparation(server);
    }

    public synchronized int getNextRunIndexAndIncrement() {
        int val = nextRunIndex++;
        setDirty();
        return val;
    }
}
