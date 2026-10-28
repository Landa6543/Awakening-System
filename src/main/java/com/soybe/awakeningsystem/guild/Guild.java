package com.soybe.awakeningsystem.guild;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class Guild {
    private final String id;
    private String name;
    private String tag;
    private String motto;
    private UUID leaderUuid;
    private final Map<UUID, GuildMember> members = new LinkedHashMap<>();
    private int level = 1;
    private long exp = 0;
    private long moraVault = 0;
    private long coinVault = 0;
    private final Set<String> unlockedPerks = new HashSet<>();
    private final Set<String> claimedChunks = new HashSet<>();
    private final Set<String> alliedGuilds = new HashSet<>();

    private int huntAbyssProgress = 0;
    private int huntAbyssGoal = 50;
    private int huntBossProgress = 0;
    private int huntBossGoal = 3;
    private int huntEliteProgress = 0;
    private int huntEliteGoal = 20;
    private int huntChestsProgress = 0;
    private int huntChestsGoal = 5;

    private boolean hasHq = false;
    private String hqDimension = "minecraft:overworld";
    private double hqX = 0, hqY = 64, hqZ = 0;
    private float hqYaw = 0, hqPitch = 0;

    public Guild(String id, String name, String tag, UUID leaderUuid) {
        this.id = id;
        this.name = name;
        this.tag = tag.toUpperCase(Locale.ROOT);
        this.motto = "Hunters Guild";
        this.leaderUuid = leaderUuid;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag.toUpperCase(Locale.ROOT); }
    public String getMotto() { return motto; }
    public void setMotto(String motto) { this.motto = motto; }
    public UUID getLeaderUuid() { return leaderUuid; }
    public void setLeaderUuid(UUID leaderUuid) { this.leaderUuid = leaderUuid; }
    public Map<UUID, GuildMember> getMembers() { return members; }
    public int getLevel() { return level; }
    public long getExp() { return exp; }
    public long getMoraVault() { return moraVault; }
    public long getCoinVault() { return coinVault; }
    public Set<String> getUnlockedPerks() { return unlockedPerks; }
    public Set<String> getClaimedChunks() { return claimedChunks; }
    public Set<String> getAlliedGuilds() { return Collections.unmodifiableSet(alliedGuilds); }
    public boolean isAlliedWith(String guildId) { return guildId != null && alliedGuilds.contains(guildId); }
    public void addAlly(String guildId) { if (guildId != null && !guildId.equals(this.id)) alliedGuilds.add(guildId); }
    public void removeAlly(String guildId) { alliedGuilds.remove(guildId); }

    public int getMaxClaims() {
        return (level + 1) * (level + 1);
    }

    public boolean claimChunk(String dim, int chunkX, int chunkZ) {
        if (claimedChunks.size() >= getMaxClaims()) return false;
        return claimedChunks.add(dim + "#" + chunkX + "#" + chunkZ);
    }

    public boolean unclaimChunk(String dim, int chunkX, int chunkZ) {
        return claimedChunks.remove(dim + "#" + chunkX + "#" + chunkZ);
    }

    public void unclaimAllChunks() {
        claimedChunks.clear();
    }

    public boolean isChunkClaimed(String dim, int chunkX, int chunkZ) {
        return claimedChunks.contains(dim + "#" + chunkX + "#" + chunkZ);
    }

    public boolean hasHq() { return hasHq; }
    public String getHqDimension() { return hqDimension; }
    public double getHqX() { return hqX; }
    public double getHqY() { return hqY; }
    public double getHqZ() { return hqZ; }
    public float getHqYaw() { return hqYaw; }
    public float getHqPitch() { return hqPitch; }

    public void setHq(String dim, double x, double y, double z, float yaw, float pitch) {
        this.hasHq = true;
        this.hqDimension = dim;
        this.hqX = x;
        this.hqY = y;
        this.hqZ = z;
        this.hqYaw = yaw;
        this.hqPitch = pitch;
    }

    public long getExpForNextLevel() {
        return level * 10000L;
    }

    public boolean addExp(long amount) {
        this.exp += amount;
        boolean leveledUp = false;
        while (level < 10 && this.exp >= getExpForNextLevel()) {
            this.exp -= getExpForNextLevel();
            this.level++;
            leveledUp = true;
        }
        return leveledUp;
    }

    public void depositMora(long amount) {
        this.moraVault += amount;
        addExp(amount / 500L);
    }

    public boolean withdrawMora(long amount) {
        if (this.moraVault >= amount) {
            this.moraVault -= amount;
            return true;
        }
        return false;
    }

    public void depositCoins(long amount) {
        this.coinVault += amount;
        addExp(amount / 5L);
    }

    public boolean withdrawCoins(long amount) {
        if (this.coinVault >= amount) {
            this.coinVault -= amount;
            return true;
        }
        return false;
    }

    public int getHuntAbyssProgress() { return huntAbyssProgress; }
    public int getHuntAbyssGoal() { return huntAbyssGoal; }
    public int getHuntBossProgress() { return huntBossProgress; }
    public int getHuntBossGoal() { return huntBossGoal; }
    public int getHuntEliteProgress() { return huntEliteProgress; }
    public int getHuntEliteGoal() { return huntEliteGoal; }
    public int getHuntChestsProgress() { return huntChestsProgress; }
    public int getHuntChestsGoal() { return huntChestsGoal; }

    public void progressHunt(String type, int amount, net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        boolean completed = false;
        long expReward = 0;
        long moraReward = 0;
        long coinReward = 0;
        String questName = "";

        switch (type.toLowerCase(Locale.ROOT)) {
            case "abyss" -> {
                this.huntAbyssProgress += amount;
                if (this.huntAbyssProgress >= this.huntAbyssGoal) {
                    this.huntAbyssProgress = 0;
                    this.huntAbyssGoal = Math.min(200, this.huntAbyssGoal + 15);
                    completed = true;
                    expReward = 1500;
                    moraReward = 25000;
                    coinReward = 250;
                    questName = "Abyss & Shadow Purge";
                }
            }
            case "boss" -> {
                this.huntBossProgress += amount;
                if (this.huntBossProgress >= this.huntBossGoal) {
                    this.huntBossProgress = 0;
                    this.huntBossGoal = Math.min(10, this.huntBossGoal + 1);
                    completed = true;
                    expReward = 2500;
                    moraReward = 50000;
                    coinReward = 500;
                    questName = "Major Boss Hunt";
                }
            }
            case "elite" -> {
                this.huntEliteProgress += amount;
                if (this.huntEliteProgress >= this.huntEliteGoal) {
                    this.huntEliteProgress = 0;
                    this.huntEliteGoal = Math.min(60, this.huntEliteGoal + 5);
                    completed = true;
                    expReward = 1000;
                    moraReward = 15000;
                    coinReward = 150;
                    questName = "Elite Monster Extermination";
                }
            }
            case "chest" -> {
                this.huntChestsProgress += amount;
                if (this.huntChestsProgress >= this.huntChestsGoal) {
                    this.huntChestsProgress = 0;
                    this.huntChestsGoal = Math.min(15, this.huntChestsGoal + 2);
                    completed = true;
                    expReward = 2000;
                    moraReward = 30000;
                    coinReward = 300;
                    questName = "Dungeons & Chests Conquest";
                }
            }
        }

        if (completed) {
            addExp(expReward);
            this.moraVault += moraReward;
            this.coinVault += coinReward;
            GuildSavedData.get(server).setDirty();
            GuildSavedData.get(server).broadcastToGuild(this, server, net.minecraft.network.chat.Component.translatable("guild.msg.hunt_completed", questName, expReward, moraReward, coinReward));
            GuildSavedData.get(server).syncAllGuildMembers(this, server);
        } else {
            GuildSavedData.get(server).setDirty();
        }
    }

    public boolean hasPerk(GuildPerk perk) {
        return perk != null && unlockedPerks.contains(perk.getId());
    }

    public boolean hasPerk(String perkId) {
        return unlockedPerks.contains(perkId);
    }

    public boolean unlockPerk(GuildPerk perk) {
        if (perk == null || hasPerk(perk)) return false;
        if (this.level < perk.getRequiredGuildLevel()) return false;
        if (!withdrawMora(perk.getCostMora())) return false;
        unlockedPerks.add(perk.getId());
        return true;
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public GuildMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    public GuildRole getMemberRole(UUID uuid) {
        GuildMember m = members.get(uuid);
        return m != null ? m.getRole() : null;
    }

    public void addMember(GuildMember member) {
        members.put(member.getUuid(), member);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putString("Name", name);
        tag.putString("Tag", this.tag);
        tag.putString("Motto", motto);
        tag.putUUID("Leader", leaderUuid);
        tag.putInt("Level", level);
        tag.putLong("Exp", exp);
        tag.putLong("MoraVault", moraVault);
        tag.putLong("CoinVault", coinVault);

        ListTag memberList = new ListTag();
        for (GuildMember m : members.values()) {
            memberList.add(m.save());
        }
        tag.put("Members", memberList);

        ListTag perkList = new ListTag();
        for (String p : unlockedPerks) {
            perkList.add(StringTag.valueOf(p));
        }
        tag.put("Perks", perkList);

        ListTag chunkList = new ListTag();
        for (String c : claimedChunks) {
            chunkList.add(StringTag.valueOf(c));
        }
        tag.put("ClaimedChunks", chunkList);

        tag.putBoolean("HasHq", hasHq);
        if (hasHq) {
            tag.putString("HqDim", hqDimension);
            tag.putDouble("HqX", hqX);
            tag.putDouble("HqY", hqY);
            tag.putDouble("HqZ", hqZ);
            tag.putFloat("HqYaw", hqYaw);
            tag.putFloat("HqPitch", hqPitch);
        }

        tag.putInt("HuntAbyssProg", huntAbyssProgress);
        tag.putInt("HuntAbyssGoal", huntAbyssGoal);
        tag.putInt("HuntBossProg", huntBossProgress);
        tag.putInt("HuntBossGoal", huntBossGoal);
        tag.putInt("HuntEliteProg", huntEliteProgress);
        tag.putInt("HuntEliteGoal", huntEliteGoal);
        tag.putInt("HuntChestsProg", huntChestsProgress);
        tag.putInt("HuntChestsGoal", huntChestsGoal);

        ListTag aList = new ListTag();
        for (String allyId : alliedGuilds) {
            aList.add(StringTag.valueOf(allyId));
        }
        tag.put("AlliedGuilds", aList);

        return tag;
    }

    public static Guild load(CompoundTag tag) {
        String id = tag.getString("Id");
        String name = tag.getString("Name");
        String gTag = tag.getString("Tag");
        UUID leader = tag.getUUID("Leader");

        Guild guild = new Guild(id, name, gTag, leader);
        guild.motto = tag.getString("Motto");
        guild.level = Math.max(1, tag.getInt("Level"));
        guild.exp = tag.getLong("Exp");
        guild.moraVault = tag.getLong("MoraVault");
        guild.coinVault = tag.getLong("CoinVault");

        guild.huntAbyssProgress = tag.getInt("HuntAbyssProg");
        guild.huntAbyssGoal = tag.contains("HuntAbyssGoal") ? tag.getInt("HuntAbyssGoal") : 50;
        guild.huntBossProgress = tag.getInt("HuntBossProg");
        guild.huntBossGoal = tag.contains("HuntBossGoal") ? tag.getInt("HuntBossGoal") : 3;
        guild.huntEliteProgress = tag.getInt("HuntEliteProg");
        guild.huntEliteGoal = tag.contains("HuntEliteGoal") ? tag.getInt("HuntEliteGoal") : 20;
        guild.huntChestsProgress = tag.getInt("HuntChestsProg");
        guild.huntChestsGoal = tag.contains("HuntChestsGoal") ? tag.getInt("HuntChestsGoal") : 5;

        if (tag.contains("Members", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Members", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                GuildMember m = GuildMember.load(list.getCompound(i));
                guild.members.put(m.getUuid(), m);
            }
        }

        if (tag.contains("Perks", Tag.TAG_LIST)) {
            ListTag pList = tag.getList("Perks", Tag.TAG_STRING);
            for (int i = 0; i < pList.size(); i++) {
                guild.unlockedPerks.add(pList.getString(i));
            }
        }

        if (tag.contains("ClaimedChunks", Tag.TAG_LIST)) {
            ListTag cList = tag.getList("ClaimedChunks", Tag.TAG_STRING);
            for (int i = 0; i < cList.size(); i++) {
                guild.claimedChunks.add(cList.getString(i));
            }
        }

        if (tag.contains("AlliedGuilds", Tag.TAG_LIST)) {
            ListTag aList = tag.getList("AlliedGuilds", Tag.TAG_STRING);
            for (int i = 0; i < aList.size(); i++) {
                guild.alliedGuilds.add(aList.getString(i));
            }
        }

        guild.hasHq = tag.getBoolean("HasHq");
        if (guild.hasHq) {
            guild.hqDimension = tag.getString("HqDim");
            guild.hqX = tag.getDouble("HqX");
            guild.hqY = tag.getDouble("HqY");
            guild.hqZ = tag.getDouble("HqZ");
            guild.hqYaw = tag.getFloat("HqYaw");
            guild.hqPitch = tag.getFloat("HqPitch");
        }

        return guild;
    }
}
