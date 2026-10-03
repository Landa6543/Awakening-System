package com.soybe.awakeningsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UniqueRankManager extends SavedData {
    public static final String DATA_NAME = "awakening_unique_ranks";

    public static final Set<String> UNIQUE_TITLES = Set.of(
            "unique_shadow_monarch",
            "unique_demon_king_salvation",
            "unique_humanitys_strongest",
            "unique_dragon_emperor"
    );

    public static final Set<String> UNIQUE_SKILLS = Set.of(
            "unique_absolute_extraction",
            "unique_rulers_authority_prime",
            "unique_fourth_wall_infinite",
            "unique_dragon_breath_annihilation"
    );

    private final Map<String, UUID> uniqueTitleHolders = new HashMap<>();
    private final Map<String, UUID> uniqueSkillHolders = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();

    public UniqueRankManager() {}

    public static UniqueRankManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(UniqueRankManager::new, UniqueRankManager::load, null),
                DATA_NAME
        );
    }

    public static UniqueRankManager load(CompoundTag tag, HolderLookup.Provider registries) {
        UniqueRankManager manager = new UniqueRankManager();
        if (tag.contains("Titles", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Titles", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                manager.uniqueTitleHolders.put(entry.getString("Id"), entry.getUUID("UUID"));
            }
        }
        if (tag.contains("Skills", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Skills", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                manager.uniqueSkillHolders.put(entry.getString("Id"), entry.getUUID("UUID"));
            }
        }
        if (tag.contains("Names", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Names", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                manager.playerNames.put(entry.getUUID("UUID"), entry.getString("Name"));
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag titleList = new ListTag();
        for (var entry : uniqueTitleHolders.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", entry.getKey());
            t.putUUID("UUID", entry.getValue());
            titleList.add(t);
        }
        tag.put("Titles", titleList);

        ListTag skillList = new ListTag();
        for (var entry : uniqueSkillHolders.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("Id", entry.getKey());
            t.putUUID("UUID", entry.getValue());
            skillList.add(t);
        }
        tag.put("Skills", skillList);

        ListTag nameList = new ListTag();
        for (var entry : playerNames.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", entry.getKey());
            t.putString("Name", entry.getValue());
            nameList.add(t);
        }
        tag.put("Names", nameList);

        return tag;
    }

    public static boolean isUniqueTitle(String titleId) {
        return UNIQUE_TITLES.contains(titleId);
    }

    public static boolean isUniqueSkill(String skillId) {
        return UNIQUE_SKILLS.contains(skillId);
    }

    public boolean canClaimTitle(String titleId, UUID playerId) {
        UUID current = uniqueTitleHolders.get(titleId);
        return current == null || current.equals(playerId);
    }

    public boolean claimTitle(String titleId, ServerPlayer player) {
        if (!canClaimTitle(titleId, player.getUUID())) {
            return false;
        }
        uniqueTitleHolders.put(titleId, player.getUUID());
        playerNames.put(player.getUUID(), player.getScoreboardName());
        setDirty();
        return true;
    }

    public void relinquishTitle(String titleId, UUID playerId) {
        if (playerId.equals(uniqueTitleHolders.get(titleId))) {
            uniqueTitleHolders.remove(titleId);
            setDirty();
        }
    }

    public boolean canClaimSkill(String skillId, UUID playerId) {
        UUID current = uniqueSkillHolders.get(skillId);
        return current == null || current.equals(playerId);
    }

    public boolean claimSkill(String skillId, ServerPlayer player) {
        if (!canClaimSkill(skillId, player.getUUID())) {
            return false;
        }
        uniqueSkillHolders.put(skillId, player.getUUID());
        playerNames.put(player.getUUID(), player.getScoreboardName());
        setDirty();
        return true;
    }

    public void relinquishSkill(String skillId, UUID playerId) {
        if (playerId.equals(uniqueSkillHolders.get(skillId))) {
            uniqueSkillHolders.remove(skillId);
            setDirty();
        }
    }

    public String getTitleHolderName(String titleId) {
        UUID id = uniqueTitleHolders.get(titleId);
        if (id == null) return null;
        return playerNames.getOrDefault(id, "Desconocido");
    }

    public String getSkillHolderName(String skillId) {
        UUID id = uniqueSkillHolders.get(skillId);
        if (id == null) return null;
        return playerNames.getOrDefault(id, "Desconocido");
    }

    public Map<String, UUID> getUniqueTitleHolders() {
        return uniqueTitleHolders;
    }

    public Map<String, UUID> getUniqueSkillHolders() {
        return uniqueSkillHolders;
    }
}
