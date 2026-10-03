package com.soybe.awakeningsystem.guild;

public enum GuildPerk {
    BOND_OF_HUNTERS("bond_of_hunters", "Vínculo de Cazadores", "✦ +10% Experiencia para todos los miembros", "✦", 1, 5000),
    COMMON_TREASURY("common_treasury", "Tesoro Compartido", "🪙 +15% Moras obtenidas al derrotar monstruos", "🪙", 2, 10000),
    REGENERATION_AURA("regeneration_aura", "Aura de Restauración", "❤ +20% Regeneración pasiva de Vida y Maná", "❤", 3, 20000),
    COLLECTIVE_FORTITUDE("collective_fortitude", "Fortaleza Colectiva", "🛡 +3 Armadura y +10% Resistencia a Empuje", "🛡", 4, 35000),
    GUILD_MIGHT("guild_might", "Poder del Gremio", "⚔ +10% Daño de Ataque y +5% Probabilidad Crítica", "⚔", 5, 50000),
    WARP_BEACON("warp_beacon", "Faro de Teletransporte", "🌀 Desbloquea teletransporte a la Sede (/guild home)", "🌀", 6, 75000),
    DUNGEON_RESONANCE("dungeon_resonance", "Resonancia de Mazmorra", "☠ +15% Daño contra Jefes de Mazmorras y Grietas", "☠", 7, 100000);

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
