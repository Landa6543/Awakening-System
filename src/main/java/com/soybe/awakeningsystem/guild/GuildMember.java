package com.soybe.awakeningsystem.guild;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class GuildMember {
    private final UUID uuid;
    private String name;
    private GuildRole role;
    private long joinedAt;
    private long contributedMora;
    private int dungeonsCleared;

    public GuildMember(UUID uuid, String name, GuildRole role) {
        this.uuid = uuid;
        this.name = name;
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
        this.contributedMora = 0;
        this.dungeonsCleared = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GuildRole getRole() { return role; }
    public void setRole(GuildRole role) { this.role = role; }
    public long getJoinedAt() { return joinedAt; }
    public long getContributedMora() { return contributedMora; }
    public void addContribution(long mora) { this.contributedMora += mora; }
    public int getDungeonsCleared() { return dungeonsCleared; }
    public void incrementDungeonsCleared() { this.dungeonsCleared++; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("UUID", uuid);
        tag.putString("Name", name);
        tag.putString("Role", role.name());
        tag.putLong("JoinedAt", joinedAt);
        tag.putLong("ContributedMora", contributedMora);
        tag.putInt("DungeonsCleared", dungeonsCleared);
        return tag;
    }

    public static GuildMember load(CompoundTag tag) {
        UUID uuid = tag.getUUID("UUID");
        String name = tag.getString("Name");
        GuildRole role = GuildRole.MEMBER;
        try {
            role = GuildRole.valueOf(tag.getString("Role"));
        } catch (Exception ignored) {}

        GuildMember member = new GuildMember(uuid, name, role);
        member.joinedAt = tag.getLong("JoinedAt");
        member.contributedMora = tag.getLong("ContributedMora");
        member.dungeonsCleared = tag.getInt("DungeonsCleared");
        return member;
    }
}
