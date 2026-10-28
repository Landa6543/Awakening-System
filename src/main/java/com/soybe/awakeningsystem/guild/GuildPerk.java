package com.soybe.awakeningsystem.guild;

public enum GuildPerk {
    BOND_OF_HUNTERS("bond_of_hunters", "Hunter's Bond", "✦ +10% Experience for all members", "✦", 1, 5000),
    COMMON_TREASURY("common_treasury", "Shared Treasury", "🪙 +15% Mora earned when defeating monsters", "🪙", 2, 10000),
    REGENERATION_AURA("regeneration_aura", "Restoration Aura", "❤ +20% Passive Health and Mana Regeneration", "❤", 3, 20000),
    COLLECTIVE_FORTITUDE("collective_fortitude", "Collective Fortitude", "🛡 +3 Armor and +10% Knockback Resistance", "🛡", 4, 35000),
    GUILD_MIGHT("guild_might", "Guild Might", "⚔ +10% Attack Damage and +5% Critical Chance", "⚔", 5, 50000),
    WARP_BEACON("warp_beacon", "Warp Beacon", "🌀 Unlocks teleportation to Headquarters (/guild home)", "🌀", 6, 75000),
    DUNGEON_RESONANCE("dungeon_resonance", "Dungeon Resonance", "☠ +15% Damage against Dungeon and Rift Bosses", "☠", 7, 100000);

    private final String id;
    private final String title;
    private final String description;
    private final String icon;
    private final int requiredGuildLevel;
    private final int costMora;

    GuildPerk(String id, String title, String description, String icon, int requiredGuildLevel, int costMora) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.requiredGuildLevel = requiredGuildLevel;
        this.costMora = costMora;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public net.minecraft.network.chat.Component getTitleComponent() {
        return net.minecraft.network.chat.Component.translatable("awakening_system.guild.perk." + id + ".title");
    }
    public net.minecraft.network.chat.Component getDescriptionComponent() {
        return net.minecraft.network.chat.Component.translatable("awakening_system.guild.perk." + id + ".desc");
    }
    public String getIcon() { return icon; }
    public int getRequiredGuildLevel() { return requiredGuildLevel; }
    public int getCostMora() { return costMora; }

    public static GuildPerk fromId(String id) {
        for (GuildPerk p : values()) {
            if (p.id.equalsIgnoreCase(id)) return p;
        }
        return null;
    }
}
