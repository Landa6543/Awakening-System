package com.soybe.awakeningsystem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

import java.util.Locale;

public enum ShadowRank {
    SOLDIER(0, "awakening_system.shadow.rank.soldier", "§7[SOLDADO]", 0xFFB0B0B0, 10, 0, 0, 0, 1.0F, 0.0D),
    ELITE(1, "awakening_system.shadow.rank.elite", "§b[ÉLITE]", 0xFF70E8FF, 25, 1, 1, 1, 1.35F, 2.0D),
    KNIGHT(2, "awakening_system.shadow.rank.knight", "§9[CABALLERO]", 0xFF5588FF, 50, 2, 1, 1, 1.75F, 4.0D),
    ELITE_KNIGHT(3, "awakening_system.shadow.rank.elite_knight", "§5[CABALLERO ÉLITE]", 0xFFBB55FF, 100, 3, 2, 2, 2.25F, 7.0D),
    COMMANDER(4, "awakening_system.shadow.rank.commander", "§c§l[COMANDANTE]", 0xFFFF5555, 200, 4, 3, 2, 3.0F, 11.0D),
    GRAND_MARSHAL(5, "awakening_system.shadow.rank.marshal", "§6§l[GRAN MARISCAL]", 0xFFFFD700, -1, 5, 4, 3, 4.0F, 16.0D);

    private final int level;
    private final String langKey;
    private final String prefix;
    private final int color;
    private final int killsToPromote;
    private final int strengthAmp;
    private final int resistanceAmp;
    private final int speedAmp;
    private final float healthMultiplier;
    private final double bonusAttackDamage;

    ShadowRank(int level, String langKey, String prefix, int color, int killsToPromote,
               int strengthAmp, int resistanceAmp, int speedAmp, float healthMultiplier, double bonusAttackDamage) {
        this.level = level;
        this.langKey = langKey;
        this.prefix = prefix;
        this.color = color;
        this.killsToPromote = killsToPromote;
        this.strengthAmp = strengthAmp;
        this.resistanceAmp = resistanceAmp;
        this.speedAmp = speedAmp;
        this.healthMultiplier = healthMultiplier;
        this.bonusAttackDamage = bonusAttackDamage;
    }

    public int getLevel() {
        return level;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getColor() {
        return color;
    }

    public int getKillsToPromote() {
        return killsToPromote;
    }

    public int getStrengthAmp() {
        return strengthAmp;
    }

    public int getResistanceAmp() {
        return resistanceAmp;
    }

    public int getSpeedAmp() {
        return speedAmp;
    }

    public float getHealthMultiplier() {
        return healthMultiplier;
    }

    public double getBonusAttackDamage() {
        return bonusAttackDamage;
    }

    public Component getDisplayName() {
        return Component.translatable(langKey);
    }

    public static ShadowRank byLevel(int level) {
        for (ShadowRank r : values()) {
            if (r.level == level) return r;
        }
        return level >= 5 ? GRAND_MARSHAL : SOLDIER;
    }

    public static ShadowRank determineInitialRank(Mob mob) {
        if (mob == null) return SOLDIER;
        String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString().toLowerCase(Locale.ROOT);
        String customName = mob.getCustomName() != null ? mob.getCustomName().getString().toLowerCase(Locale.ROOT) : "";

        // Jefes Supremos -> Rango Comandante (Rank 4)
        if (typeId.contains("wither") || typeId.contains("warden") || typeId.contains("dragon")
                || typeId.contains("baran") || typeId.contains("blood_patriarch") || typeId.contains("demon_king")
                || customName.contains("baran") || customName.contains("patriarca") || customName.contains("monarca")
                || customName.contains("boss") || customName.contains("jefe") || mob.getMaxHealth() >= 250.0F) {
            return COMMANDER;
        }

        // Mini-jefes, élites pesados y monstruos gigantes -> Rango Caballero (Rank 2)
        if (typeId.contains("ravager") || typeId.contains("iron_golem") || typeId.contains("evoker")
                || typeId.contains("elder_guardian") || typeId.contains("piglin_brute") || typeId.contains("goblin_shaman")
                || typeId.contains("beholder") || mob.getMaxHealth() >= 60.0F) {
            return KNIGHT;
        }

        // Monstruos avanzados, guerreros y atacantes de rango -> Rango Élite (Rank 1)
        if (typeId.contains("goblin_warrior") || typeId.contains("enderman") || typeId.contains("witch")
                || typeId.contains("vindicator") || typeId.contains("guardian") || typeId.contains("blaze")
                || typeId.contains("drowned") || mob.getMaxHealth() >= 30.0F) {
            return ELITE;
        }

        // Infantería y monstruos básicos -> Rango Soldado (Rank 0)
        return SOLDIER;
    }
}
