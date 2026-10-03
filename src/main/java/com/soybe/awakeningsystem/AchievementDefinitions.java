package com.soybe.awakeningsystem;

import java.util.List;

public final class AchievementDefinitions {
    public record Definition(String id, String nameKey, String descriptionKey, String statRequired, int target, int expReward, int coinReward) {}

    private static final List<Definition> DEFINITIONS = List.of(
            new Definition("first_blood", "awakening_system.achievement.first_blood", "awakening_system.achievement.first_blood.desc", "creatures_defeated", 1, 30, 5),
            new Definition("monster_hunter", "awakening_system.achievement.monster_hunter", "awakening_system.achievement.monster_hunter.desc", "creatures_defeated", 50, 150, 25),
            new Definition("quarry_master", "awakening_system.achievement.quarry_master", "awakening_system.achievement.quarry_master.desc", "blocks_mined", 100, 100, 15),
            new Definition("ore_hound", "awakening_system.achievement.ore_hound", "awakening_system.achievement.ore_hound.desc", "ores_mined", 30, 150, 20),
            new Definition("artisan", "awakening_system.achievement.artisan", "awakening_system.achievement.artisan.desc", "items_crafted", 25, 80, 10),
            new Definition("awakened_fury", "awakening_system.achievement.awakened_fury", "awakening_system.achievement.awakened_fury.desc", "skills_activated", 10, 120, 20),
            new Definition("commerce", "awakening_system.achievement.commerce", "awakening_system.achievement.commerce.desc", "purchases", 5, 100, 15),
            new Definition("disciplined", "awakening_system.achievement.disciplined", "awakening_system.achievement.disciplined.desc", "missions_completed", 5, 200, 30),
            new Definition("boss_slayer", "awakening_system.achievement.boss_slayer", "awakening_system.achievement.boss_slayer.desc", "boss_kills", 1, 300, 50),
            new Definition("marathon", "awakening_system.achievement.marathon", "awakening_system.achievement.marathon.desc", "distance_moved", 1000, 150, 20),
            new Definition("gold_hoarder", "awakening_system.achievement.gold_hoarder", "awakening_system.achievement.gold_hoarder.desc", "coins", 500, 250, 40)
    );

    private AchievementDefinitions() {}

    public static List<Definition> definitions() { return DEFINITIONS; }

    public static int statValue(SystemData data, String statKey) {
        return switch (statKey) {
            case "creatures_defeated" -> data.creaturesDefeated();
            case "blocks_mined" -> data.blocksMined();
            case "ores_mined" -> data.oresMined();
            case "items_crafted" -> data.itemsCrafted();
            case "skills_activated" -> data.skillsActivated();
            case "purchases" -> data.purchases();
            case "missions_completed" -> data.missionsCompleted();
            case "damage_dealt" -> data.damageDealt();
            case "damage_taken" -> data.damageTaken();
            case "boss_kills" -> data.bossKills();
            case "distance_moved" -> data.distanceMoved();
            case "coins" -> data.coins();
            default -> 0;
        };
    }
}
