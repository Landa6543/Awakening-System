package com.soybe.awakeningsystem;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class SystemAttributeEffects {
    private SystemAttributeEffects() {}

    public static void apply(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        int vitPoints = Math.max(0, data.vitality() - SystemData.ATTR_BASE);
        apply(player, Attributes.MAX_HEALTH, "vitality", vitPoints * 2.0D);
        int strPoints = Math.max(0, data.strength() - SystemData.ATTR_BASE);
        apply(player, Attributes.ATTACK_DAMAGE, "strength", strPoints * 0.5D);
        apply(player, Attributes.ATTACK_SPEED, "agility", diminishing(data.agility(), SystemData.ATTR_BASE, 0.015D));
        apply(player, Attributes.LUCK, "perception", diminishing(data.perception(), SystemData.ATTR_BASE, 0.1D));
        apply(player, Attributes.ATTACK_SPEED, "intelligence", diminishing(data.intelligence(), SystemData.ATTR_BASE, 0.02D));
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "willpower", diminishing(data.willpower(), SystemData.ATTR_BASE, 0.02D));
        if (data.isSkillEnabled("blue_dragon_scales")) {
            apply(player, Attributes.ARMOR_TOUGHNESS, "skill_blue_dragon_toughness", 2.0D);
            apply(player, Attributes.ARMOR, "skill_blue_dragon_armor", 2.0D);
        } else {
            apply(player, Attributes.ARMOR_TOUGHNESS, "skill_blue_dragon_toughness", 0.0D);
            apply(player, Attributes.ARMOR, "skill_blue_dragon_armor", 0.0D);
        }

        // Hunter Class Awakening Modifiers
        String hClass = data.hunterClass();
        apply(player, Attributes.ATTACK_SPEED, "hunter_sword_saint_spd", "sword_saint".equals(hClass) ? 0.35D : ("demonic_cultivator".equals(hClass) ? 0.15D : ("void_assassin".equals(hClass) ? 0.25D : ("lightning_emperor".equals(hClass) ? 0.20D : ("blood_emperor".equals(hClass) ? 0.10D : 0.0D)))));
        apply(player, Attributes.MAX_HEALTH, "hunter_colossal_hp", "colossal_berserker".equals(hClass) ? 16.0D : ("dragon_monarch".equals(hClass) ? 10.0D : ("spiritual_healer".equals(hClass) ? 6.0D : 0.0D)));
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "hunter_colossal_kb", "colossal_berserker".equals(hClass) ? 0.5D : ("dragon_monarch".equals(hClass) ? 0.3D : 0.0D));
        apply(player, Attributes.ATTACK_DAMAGE, "hunter_demonic_dmg", "demonic_cultivator".equals(hClass) ? 3.0D : ("dragon_monarch".equals(hClass) ? 2.5D : ("beast_monarch".equals(hClass) ? 2.5D : ("blood_emperor".equals(hClass) ? 2.0D : 0.0D))));
        apply(player, Attributes.MOVEMENT_SPEED, "hunter_void_spd", "void_assassin".equals(hClass) ? 0.035D : ("lightning_emperor".equals(hClass) ? 0.030D : ("beast_monarch".equals(hClass) ? 0.020D : 0.0D)));
        apply(player, Attributes.ARMOR, "hunter_dragon_armor", "dragon_monarch".equals(hClass) ? 4.0D : ("spiritual_healer".equals(hClass) ? 4.0D : 0.0D));
        apply(player, Attributes.ARMOR_TOUGHNESS, "hunter_dragon_toughness", "dragon_monarch".equals(hClass) ? 2.0D : 0.0D);
        apply(player, Attributes.LUCK, "hunter_archmage_luck", "grand_archmage".equals(hClass) ? 1.0D : 0.0D);

        // Cross-Mod Magic Compatibility (Iron's Spells 'n Spellbooks & Ars Nouveau)
        com.soybe.awakeningsystem.compat.MagicModCompatHandler.applyMagicAttributes(player, data);

        // Class Skill Tree Attribute Node Modifiers
        double treeAtk = ClassSkillTree.getTotalAttributeBonus(data, ClassSkillTree.StatType.ATTACK_DAMAGE);
        apply(player, Attributes.ATTACK_DAMAGE, "tree_bonus_atk", treeAtk);

        double treeHp = ClassSkillTree.getTotalAttributeBonus(data, ClassSkillTree.StatType.MAX_HEALTH);
        apply(player, Attributes.MAX_HEALTH, "tree_bonus_hp", treeHp);

        double treeSpd = ClassSkillTree.getTotalAttributeBonus(data, ClassSkillTree.StatType.MOVEMENT_SPEED);
        apply(player, Attributes.MOVEMENT_SPEED, "tree_bonus_spd", treeSpd);

        double treeAtkSpd = ClassSkillTree.getTotalAttributeBonus(data, ClassSkillTree.StatType.ATTACK_SPEED);
        apply(player, Attributes.ATTACK_SPEED, "tree_bonus_atk_spd", treeAtkSpd);

        double treeArmor = ClassSkillTree.getTotalAttributeBonus(data, ClassSkillTree.StatType.ARMOR);
        apply(player, Attributes.ARMOR, "tree_bonus_armor", treeArmor);

        double treeKb = ClassSkillTree.getTotalAttributeBonus(data, ClassSkillTree.StatType.KNOCKBACK_RESISTANCE);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "tree_bonus_kb", treeKb);


        String classId = data.classId();
        clearClassModifiers(player);
        switch (classId) {
            case "miner" -> {
                apply(player, Attributes.MOVEMENT_SPEED, "class_miner_speed", 0.01D);
                apply(player, Attributes.LUCK, "class_miner_luck", 0.5D);
            }
            case "warrior" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "class_warrior_damage", 1.0D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "class_warrior_resistance", 0.05D);
            }
            case "sage" -> {
                apply(player, Attributes.LUCK, "class_sage_luck", 1.0D);
                apply(player, Attributes.ATTACK_SPEED, "class_sage_attack_speed", 0.03D);
            }
            case "wanderer" -> apply(player, Attributes.MAX_HEALTH, "class_wanderer_health", 1.0D);
            default -> { }
        }
        String titleId = data.equippedTitle();
        clearTitleModifiers(player);
        switch (titleId) {
            case "stone_breaker" -> {
                apply(player, Attributes.MOVEMENT_SPEED, "title_stone_breaker_speed", 0.01D);
                apply(player, Attributes.LUCK, "title_stone_breaker_luck", 0.5D);
            }
            case "deep_delver" -> apply(player, Attributes.MAX_HEALTH, "title_deep_delver_health", 2.0D);
            case "ironforged" -> apply(player, Attributes.KNOCKBACK_RESISTANCE, "title_ironforged_resistance", 0.08D);
            case "diamond_blessed" -> apply(player, Attributes.LUCK, "title_diamond_blessed_luck", 1.0D);
            case "nether_walker" -> apply(player, Attributes.MOVEMENT_SPEED, "title_nether_walker_speed", 0.015D);
            case "ender_walker" -> apply(player, Attributes.MAX_HEALTH, "title_ender_walker_health", 2.0D);
            case "dragon_slayer" -> apply(player, Attributes.ATTACK_DAMAGE, "title_dragon_slayer_damage", 2.0D);
            case "village_hero" -> apply(player, Attributes.LUCK, "title_village_hero_luck", 1.0D);
            case "sea_explorer" -> apply(player, Attributes.MAX_HEALTH, "title_sea_explorer_health", 2.0D);
            case "master_fisher" -> apply(player, Attributes.LUCK, "title_master_fisher_luck", 1.5D);
            case "beast_hunter" -> apply(player, Attributes.ATTACK_DAMAGE, "title_beast_hunter_damage", 1.0D);
            case "raid_breaker" -> apply(player, Attributes.KNOCKBACK_RESISTANCE, "title_raid_breaker_resistance", 0.1D);
            case "ancient_hunter" -> apply(player, Attributes.ATTACK_DAMAGE, "title_ancient_hunter_damage", 2.5D);
            case "warden_survivor" -> apply(player, Attributes.MAX_HEALTH, "title_warden_survivor_health", 4.0D);
            case "librarian" -> apply(player, Attributes.ATTACK_SPEED, "title_librarian_attack_speed", 0.04D);
            case "master_builder" -> apply(player, Attributes.MAX_HEALTH, "title_master_builder_health", 2.0D);
            case "netherite_ascendant" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_netherite_damage", 3.0D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "title_netherite_resistance", 0.12D);
            }
            case "collector" -> apply(player, Attributes.LUCK, "title_collector_luck", 1.0D);
            case "alchemist_master" -> apply(player, Attributes.MAX_HEALTH, "title_alchemist_master_health", 2.0D);
            case "speedster" -> apply(player, Attributes.MOVEMENT_SPEED, "title_speedster_speed", 0.02D);
            case "berserker" -> apply(player, Attributes.ATTACK_DAMAGE, "title_berserker_damage", 2.0D);
            case "immortal" -> apply(player, Attributes.MAX_HEALTH, "title_immortal_health", 6.0D);
            case "awakened_legend" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_legend_damage", 2.0D);
                apply(player, Attributes.MAX_HEALTH, "title_legend_health", 4.0D);
                apply(player, Attributes.MOVEMENT_SPEED, "title_legend_speed", 0.01D);
            }
            case "reborn_champion" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_reborn_damage", 2.5D);
                apply(player, Attributes.MAX_HEALTH, "title_reborn_health", 5.0D);
                apply(player, Attributes.MOVEMENT_SPEED, "title_reborn_speed", 0.015D);
                apply(player, Attributes.LUCK, "title_reborn_luck", 1.5D);
            }
            case "unique_shadow_monarch" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_unique_monarch_damage", 8.0D);
                apply(player, Attributes.MAX_HEALTH, "title_unique_monarch_health", 20.0D);
                apply(player, Attributes.ARMOR, "title_unique_monarch_armor", 6.0D);
                apply(player, Attributes.MOVEMENT_SPEED, "title_unique_monarch_speed", 0.03D);
            }
            case "unique_demon_king_salvation" -> {
                apply(player, Attributes.MAX_HEALTH, "title_unique_salvation_health", 24.0D);
                apply(player, Attributes.ARMOR, "title_unique_salvation_armor", 8.0D);
                apply(player, Attributes.LUCK, "title_unique_salvation_luck", 4.0D);
            }
            case "unique_humanitys_strongest" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_unique_strongest_damage", 10.0D);
                apply(player, Attributes.ATTACK_SPEED, "title_unique_strongest_speed", 0.15D);
                apply(player, Attributes.MOVEMENT_SPEED, "title_unique_strongest_move", 0.04D);
            }
            case "unique_dragon_emperor" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_unique_dragon_damage", 12.0D);
                apply(player, Attributes.MAX_HEALTH, "title_unique_dragon_health", 16.0D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "title_unique_dragon_kb", 0.35D);
            }
            case "ranker_of_the_tower" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_ranker_damage", 6.0D);
                apply(player, Attributes.MAX_HEALTH, "title_ranker_health", 14.0D);
                apply(player, Attributes.MOVEMENT_SPEED, "title_ranker_speed", 0.03D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "title_ranker_kb", 0.25D);
            }
            case "rank_f", "rank_e", "rank_d", "rank_c", "rank_b", "rank_a", "rank_s", "rank_ex", "rank_ss", "rank_sss", "rank_ur", "rank_ur_plus", "rank_lr", "rank_slr", "rank_mr", "rank_gr", "rank_demigod", "rank_god", "rank_deity", "rank_supreme_being", "rank_transcendental", "rank_absolute_being", "rank_ancestral", "rank_primordial", "rank_cosmic", "rank_multiversal", "rank_omniversal", "rank_infinite", "rank_omnipotent", "rank_national", "rank_monarch", "rank_transcendent", "rank_ruler", "rank_absolute", "rank_absolute_creator", "rank_supreme_creator", "rank_chaos_sovereign", "rank_timeless_monarch", "rank_astral_deity", "rank_singularity", "rank_limitless", "rank_absolute_god" -> {
                int rankLevel = switch (titleId) {
                    case "rank_f" -> 1; case "rank_e" -> 5; case "rank_d" -> 10; case "rank_c" -> 20; case "rank_b" -> 30; case "rank_a" -> 40; case "rank_s" -> 50; case "rank_ex" -> 70; case "rank_ss" -> 90; case "rank_sss" -> 110; case "rank_ur" -> 130; case "rank_ur_plus" -> 150; case "rank_lr" -> 170; case "rank_slr" -> 190; case "rank_mr" -> 210; case "rank_gr" -> 230; case "rank_demigod" -> 250; case "rank_god" -> 270; case "rank_deity" -> 290; case "rank_supreme_being" -> 310; case "rank_transcendental" -> 330; case "rank_absolute_being" -> 350; case "rank_ancestral" -> 370; case "rank_primordial" -> 390; case "rank_cosmic" -> 410; case "rank_multiversal" -> 430; case "rank_omniversal" -> 450; case "rank_infinite" -> 470; case "rank_omnipotent" -> 490; case "rank_national" -> 510; case "rank_monarch" -> 540; case "rank_transcendent" -> 570; case "rank_ruler" -> 600; case "rank_absolute" -> 630; case "rank_absolute_creator" -> 660; case "rank_supreme_creator" -> 700; case "rank_chaos_sovereign" -> 750; case "rank_timeless_monarch" -> 800; case "rank_astral_deity" -> 850; case "rank_singularity" -> 900; case "rank_limitless" -> 950; case "rank_absolute_god" -> 1000; default -> 1;
                };
                double scale = 0.5D + rankLevel / 100.0D;
                apply(player, Attributes.ATTACK_DAMAGE, "title_rank_damage", scale);
                apply(player, Attributes.MAX_HEALTH, "title_rank_health", scale * 1.5D);
            }

            case "awakened" -> apply(player, Attributes.MAX_HEALTH, "title_awakened_health", 1.0D);
            case "evolved" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "title_evolved_damage", 1.0D);
                apply(player, Attributes.MOVEMENT_SPEED, "title_evolved_speed", 0.01D);
            }
            case "awakening" -> apply(player, Attributes.MAX_HEALTH, "title_awakening_health", 0.5D);
            default -> {
                apply(player, Attributes.LUCK, "title_generic_luck", 1.0D);
                apply(player, Attributes.MAX_HEALTH, "title_generic_health", 1.0D);
            }
        }

        // Estigma de Constelación Patrocinadora (ORV)
        String sponsor = data.sponsorConstellation();
        clearConstellationModifiers(player);
        switch (sponsor) {
            case "abyssal_black_dragon" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_dragon_damage", 2.0D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_dragon_resistance", 0.1D);
            }
            case "demon_like_judge_of_fire" -> {
                apply(player, Attributes.MAX_HEALTH, "constellation_judge_health", 4.0D);
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_judge_damage", 1.0D);
            }
            case "secretive_plotter" -> {
                apply(player, Attributes.MOVEMENT_SPEED, "constellation_plotter_speed", 0.02D);
                apply(player, Attributes.ATTACK_SPEED, "constellation_plotter_attack_speed", 0.05D);
            }
            case "prisoner_of_the_golden_headband" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_headband_damage", 1.5D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_headband_resistance", 0.15D);
            }
            case "master_of_the_underworld" -> {
                apply(player, Attributes.LUCK, "constellation_underworld_luck", 1.5D);
                apply(player, Attributes.MAX_HEALTH, "constellation_underworld_health", 2.0D);
            }
            case "queen_of_the_darkest_night" -> {
                apply(player, Attributes.LUCK, "constellation_queen_luck", 2.0D);
                apply(player, Attributes.MAX_HEALTH, "constellation_queen_health", 4.0D);
            }
            case "architect_of_the_system" -> {
                apply(player, Attributes.MOVEMENT_SPEED, "constellation_architect_speed", 0.02D);
                apply(player, Attributes.ATTACK_SPEED, "constellation_architect_attack_speed", 0.06D);
            }
            case "iron_blooded_sword_saint" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_asrahan_damage", 2.5D);
                apply(player, Attributes.ATTACK_SPEED, "constellation_asrahan_attack_speed", 0.06D);
            }
            case "sacred_water_dragon" -> {
                apply(player, Attributes.MAX_HEALTH, "constellation_podong_health", 4.0D);
                apply(player, Attributes.LUCK, "constellation_podong_luck", 1.0D);
            }
            case "martial_king_nayuta" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_martial_damage", 3.0D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_martial_resistance", 0.2D);
            }
            case "caregiver_of_the_abyss" -> {
                apply(player, Attributes.MAX_HEALTH, "constellation_caregiver_health", 2.0D);
                apply(player, Attributes.MOVEMENT_SPEED, "constellation_caregiver_speed", 0.02D);
            }
            case "murim_heavenly_demon" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_nano_damage", 2.0D);
                apply(player, Attributes.ATTACK_SPEED, "constellation_nano_attack_speed", 0.08D);
            }
            case "master_of_mobius_gacha" -> {
                apply(player, Attributes.LUCK, "constellation_gacha_luck", 3.0D);
                apply(player, Attributes.ATTACK_SPEED, "constellation_gacha_attack_speed", 0.04D);
            }
            case "blue_dragon_of_retribution" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_zephyr_damage", 2.5D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_zephyr_resistance", 0.15D);
            }
            case "salvation_reader" -> {
                apply(player, Attributes.MAX_HEALTH, "constellation_reader_health", 3.0D);
                apply(player, Attributes.LUCK, "constellation_reader_luck", 2.0D);
            }
            case "plum_blossom_sword_saint" -> {
                apply(player, Attributes.ATTACK_SPEED, "constellation_plum_attack_speed", 0.10D);
                apply(player, Attributes.MOVEMENT_SPEED, "constellation_plum_speed", 0.03D);
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_plum_damage", 1.5D);
            }
            case "hell_dimension_survivor" -> {
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_hanbin_damage", 3.5D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_hanbin_resistance", 0.25D);
                apply(player, Attributes.MAX_HEALTH, "constellation_hanbin_health", 4.0D);
            }
            case "reaper_of_drifting_moon" -> {
                apply(player, Attributes.MOVEMENT_SPEED, "constellation_reaper_speed", 0.04D);
                apply(player, Attributes.ATTACK_SPEED, "constellation_reaper_attack_speed", 0.08D);
                apply(player, Attributes.LUCK, "constellation_reaper_luck", 2.0D);
            }
            case "tower_gate_opener" -> {
                apply(player, Attributes.MAX_HEALTH, "constellation_tower_health", 6.0D);
                apply(player, Attributes.ATTACK_DAMAGE, "constellation_tower_damage", 2.5D);
                apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_tower_resistance", 0.15D);
            }
            default -> {}
        }

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void clearConstellationModifiers(ServerPlayer player) {
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_dragon_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_dragon_resistance", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_judge_health", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_judge_damage", 0.0D);
        apply(player, Attributes.MOVEMENT_SPEED, "constellation_plotter_speed", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_plotter_attack_speed", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_headband_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_headband_resistance", 0.0D);
        apply(player, Attributes.LUCK, "constellation_underworld_luck", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_underworld_health", 0.0D);
        apply(player, Attributes.LUCK, "constellation_queen_luck", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_queen_health", 0.0D);
        apply(player, Attributes.MOVEMENT_SPEED, "constellation_architect_speed", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_architect_attack_speed", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_asrahan_damage", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_asrahan_attack_speed", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_podong_health", 0.0D);
        apply(player, Attributes.LUCK, "constellation_podong_luck", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_martial_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_martial_resistance", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_caregiver_health", 0.0D);
        apply(player, Attributes.MOVEMENT_SPEED, "constellation_caregiver_speed", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_nano_damage", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_nano_attack_speed", 0.0D);
        apply(player, Attributes.LUCK, "constellation_gacha_luck", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_gacha_attack_speed", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_zephyr_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_zephyr_resistance", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_reader_health", 0.0D);
        apply(player, Attributes.LUCK, "constellation_reader_luck", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_plum_attack_speed", 0.0D);
        apply(player, Attributes.MOVEMENT_SPEED, "constellation_plum_speed", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_plum_damage", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_hanbin_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_hanbin_resistance", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_hanbin_health", 0.0D);
        apply(player, Attributes.MOVEMENT_SPEED, "constellation_reaper_speed", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "constellation_reaper_attack_speed", 0.0D);
        apply(player, Attributes.LUCK, "constellation_reaper_luck", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "constellation_tower_health", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "constellation_tower_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "constellation_tower_resistance", 0.0D);
    }

    private static void clearTitleModifiers(ServerPlayer player) {
        String[] ids = {"rank_damage", "rank_health", "stone_breaker_speed", "stone_breaker_luck", "deep_delver_health", "ironforged_resistance", "diamond_blessed_luck", "nether_walker_speed", "ender_walker_health", "dragon_slayer_damage", "village_hero_luck", "sea_explorer_health", "master_fisher_luck", "beast_hunter_damage", "raid_breaker_resistance", "ancient_hunter_damage", "warden_survivor_health", "librarian_attack_speed", "master_builder_health", "netherite_damage", "netherite_resistance", "ex_damage", "ex_health", "rank_s_damage", "rank_ss_damage", "rank_ss_health", "rank_sss_damage", "rank_sss_health", "rank_ex_plus_damage", "rank_ex_plus_health", "rank_transcendent_damage", "rank_transcendent_health", "rank_transcendent_speed", "rank_singularity_damage", "rank_singularity_health", "rank_singularity_luck", "ranker_damage", "ranker_health", "ranker_speed", "ranker_kb", "awakened_health", "evolved_damage", "evolved_speed", "awakening_health", "generic_luck", "generic_health"};
        for (String id : ids) clearModifier(player, "title_" + id);
    }

    private static void clearModifier(ServerPlayer player, String id) {
        for (Holder<Attribute> attribute : new Holder[]{Attributes.ATTACK_DAMAGE, Attributes.MAX_HEALTH, Attributes.MOVEMENT_SPEED, Attributes.LUCK, Attributes.ATTACK_SPEED, Attributes.KNOCKBACK_RESISTANCE}) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) instance.removeModifier(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, id));
        }
    }

    private static void clearClassModifiers(ServerPlayer player) {
        apply(player, Attributes.MOVEMENT_SPEED, "class_miner_speed", 0.0D);
        apply(player, Attributes.LUCK, "class_miner_luck", 0.0D);
        apply(player, Attributes.ATTACK_DAMAGE, "class_warrior_damage", 0.0D);
        apply(player, Attributes.KNOCKBACK_RESISTANCE, "class_warrior_resistance", 0.0D);
        apply(player, Attributes.LUCK, "class_sage_luck", 0.0D);
        apply(player, Attributes.ATTACK_SPEED, "class_sage_attack_speed", 0.0D);
        apply(player, Attributes.MAX_HEALTH, "class_wanderer_health", 0.0D);
    }


    private static double diminishing(int value, int baseline, double perPoint) {
        int points = value - baseline;
        if (points <= 0) return 0.0D;
        double result = 0.0D;
        for (int i = 0; i < points; i++) result += perPoint * Math.pow(0.92D, i);
        return result;
    }

    private static void apply(ServerPlayer player, Holder<Attribute> attribute, String id, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, id);
        instance.removeModifier(modifierId);
        if (value != 0.0D) {
            instance.addPermanentModifier(new AttributeModifier(modifierId, value, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
