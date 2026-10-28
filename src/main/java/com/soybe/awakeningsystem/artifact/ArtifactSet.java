package com.soybe.awakeningsystem.artifact;

public enum ArtifactSet {
    CRIMSON_WITCH(
            "crimson_witch",
            "Crimson Witch of Flames",
            "§c",
            5,
            "+20% Fire Damage and Pyro Skills.",
            "Explosions and burns deal +40% additional damage and burn twice as long."
    ),
    GLADIATORS_FINALE(
            "gladiator",
            "Gladiator's Finale",
            "§6",
            5,
            "+18% Physical Attack and Base DMG.",
            "+35% additional damage when hitting with Swords, Daggers, and Spears."
    ),
    VIRIDESCENT_VENERER(
            "viridescent",
            "Viridescent Venerer",
            "§a",
            5,
            "+20% Anemo / Wind DMG and +15% Speed.",
            "Swirls reduce enemy physical and elemental resistance by 30%."
    ),
    MAIDEN_BELOVED(
            "maiden",
            "Maiden Beloved",
            "§d",
            5,
            "+20% Healing Effectiveness and Regeneration.",
            "Healing yourself or a companion restores an additional 25% health to nearby allies."
    ),
    BLOODSTAINED_CHIVALRY(
            "bloodstained",
            "Bloodstained Chivalry",
            "§4",
            5,
            "+25% Physical DMG and Penetration.",
            "Defeating an enemy grants +50% DMG to the next strike and knocks down the target."
    ),
    BERSERKER(
            "berserker",
            "Berserker",
            "§c",
            4,
            "+12% Critical Hit Rate.",
            "When HP drops below 70%, Critical Rate increases by an additional +24%."
    ),
    THE_EXILE(
            "exile",
            "The Exile",
            "§b",
            4,
            "+20% Mana and Energy Recharge.",
            "Activating an ultimate or elemental skill instantly restores Mana for you and allies."
    ),
    BRAVE_HEART(
            "brave_heart",
            "Brave Heart",
            "§e",
            4,
            "+18% General Attack.",
            "+30% increased DMG against enemies that have more than 50% HP remaining."
    ),
    NOBLESSE_OBLIGE(
            "noblesse",
            "Noblesse Oblige",
            "§9",
            5,
            "+20% Ultimate Skill DMG.",
            "Using an Ultimate increases all team members' ATK by 20% for 12s."
    ),
    EMBLEM_OF_SEVERED_FATE(
            "emblem",
            "Emblem of Severed Fate",
            "§5",
            5,
            "+20% Energy and Mana Recharge.",
            "Increases Ultimate Skill DMG by 25% of total Energy Recharge (max 75%)."
    ),
    THUNDERING_FURY(
            "thundering_fury",
            "Thundering Fury",
            "§d",
            5,
            "+15% Electro DMG Bonus.",
            "Increases damage dealt by Overloaded, Electro-Charged, and Superconduct by 40%."
    ),
    BLIZZARD_STRAYER(
            "blizzard_strayer",
            "Blizzard Strayer",
            "§b",
            5,
            "+15% Cryo DMG Bonus.",
            "When attacking Cryo-affected enemies, CRIT Rate +20%. If Frozen, additional +20%."
    ),
    HEART_OF_DEPTH(
            "heart_of_depth",
            "Heart of Depth",
            "§1",
            5,
            "+15% Hydro DMG Bonus.",
            "After using an Elemental Skill, increases Normal and Charged Attack DMG by 30% for 15s."
    ),
    ARCHAIC_PETRA(
            "archaic_petra",
            "Archaic Petra",
            "§6",
            5,
            "+15% Geo DMG Bonus.",
            "Upon obtaining an elemental shard from Crystallize, all party members gain +35% DMG Bonus for that element."
    ),
    DEEPWOOD_MEMORIES(
            "deepwood",
            "Deepwood Memories",
            "§2",
            5,
            "+15% Dendro DMG Bonus.",
            "Skills decrease the Dendro RES of hit opponents by 30% for 8s."
    ),
    SHIMENAWA_REMINISCENCE(
            "shimenawa",
            "Shimenawa's Reminiscence",
            "§c",
            5,
            "+18% General Attack.",
            "When using an Elemental Skill with 15+ Energy, consumes energy to increase Normal/Charged DMG by 50%."
    ),
    DESERT_PAVILION(
            "desert_pavilion",
            "Desert Pavilion Chronicle",
            "§3",
            5,
            "+15% Anemo DMG Bonus.",
            "When a Charged Attack hits, Normal Attack SPD +10% and attack DMG increases by 40% for 15s."
    ),
    OCEAN_HUED_CLAM(
            "ocean_clam",
            "Ocean-Hued Clam",
            "§b",
            5,
            "+15% Healing Bonus.",
            "Accumulates restored health into a Sea-Dyed Foam bubble that explodes dealing Physical DMG equal to 90%."
    ),
    TENACITY_OF_THE_MILLELITH(
            "tenacity",
            "Tenacity of the Millelith",
            "§e",
            5,
            "+20% Max Health.",
            "When an Elemental Skill hits an opponent, party members gain +20% ATK and +30% Shield Strength."
    ),
    GOLDEN_TROUPE(
            "golden_troupe",
            "Golden Troupe",
            "§6",
            5,
            "+20% Elemental Skill DMG.",
            "Increases Elemental Skill DMG by an additional 25% while the character is not active in combat."
    );

    private final String idName;
    private final String displayName;
    private final String colorCode;
    private final String bonus2p;
    private final String bonus4p;
    private final int maxStars;

    ArtifactSet(String idName, String displayName, String colorCode, int maxStars, String bonus2p, String bonus4p) {
        this.idName = idName;
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.maxStars = maxStars;
        this.bonus2p = bonus2p;
        this.bonus4p = bonus4p;
    }

    public String getIdName() {
        return idName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getMaxStars() {
        return maxStars;
    }

    public String getBonus2p() {
        return bonus2p;
    }

    public String getBonus4p() {
        return bonus4p;
    }

    public net.minecraft.network.chat.Component getDisplayNameComponent() {
        return net.minecraft.network.chat.Component.literal(colorCode).append(net.minecraft.network.chat.Component.translatableWithFallback("awakening_system.artifact_set." + idName + ".name", displayName));
    }

    public net.minecraft.network.chat.Component getBonus2pComponent() {
        return net.minecraft.network.chat.Component.translatableWithFallback("awakening_system.artifact_set." + idName + ".bonus2p", bonus2p);
    }

    public net.minecraft.network.chat.Component getBonus4pComponent() {
        return net.minecraft.network.chat.Component.translatableWithFallback("awakening_system.artifact_set." + idName + ".bonus4p", bonus4p);
    }
}
