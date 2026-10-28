package com.soybe.awakeningsystem;

import net.minecraft.network.chat.Component;
import java.util.*;

public final class ClassSkillTree {
    private ClassSkillTree() {}

    public enum NodeType {
        ROOT,
        MAJOR_ACTIVE,
        MAJOR_PASSIVE,
        ATTRIBUTE_NODE
    }

    public enum StatType {
        NONE,
        ATTACK_DAMAGE,
        MAX_HEALTH,
        MAX_MANA,
        ATTACK_SPEED,
        MOVEMENT_SPEED,
        ARMOR,
        KNOCKBACK_RESISTANCE
    }

    public record SkillNode(
            String skillId,
            String hunterClass,
            String branch,
            int tier,
            int gridX,
            int gridY,
            int requiredLevel,
            int spCost,
            List<String> prerequisiteIds,
            NodeType nodeType,
            StatType statType,
            double statValue,
            String customName,
            String customDesc,
            String iconSymbol
    ) {
        public SkillNode(String skillId, String hunterClass, String branch, int tier, int gridX, int gridY, int requiredLevel, int spCost, List<String> prerequisiteIds, NodeType nodeType, String iconSymbol) {
            this(skillId, hunterClass, branch, tier, gridX, gridY, requiredLevel, spCost, prerequisiteIds, nodeType, StatType.NONE, 0.0D, null, null, iconSymbol);
        }

        public SkillNode(String skillId, String hunterClass, String branch, int tier, int gridX, int gridY, int requiredLevel, int spCost, List<String> prerequisiteIds, StatType statType, double statValue, String customName, String customDesc, String iconSymbol) {
            this(skillId, hunterClass, branch, tier, gridX, gridY, requiredLevel, spCost, prerequisiteIds, NodeType.ATTRIBUTE_NODE, statType, statValue, customName, customDesc, iconSymbol);
        }

        public SkillNode(String skillId, String hunterClass, String branch, int tier, int gridX, int gridY, int requiredLevel, int spCost, List<String> prerequisiteIds, StatType statType, double statValue, String iconSymbol) {
            this(skillId, hunterClass, branch, tier, gridX, gridY, requiredLevel, spCost, prerequisiteIds, NodeType.ATTRIBUTE_NODE, statType, statValue, null, null, iconSymbol);
        }

        public boolean isRoot() {
            return nodeType == NodeType.ROOT;
        }

        public boolean isAttribute() {
            return nodeType == NodeType.ATTRIBUTE_NODE;
        }

        public boolean isActive() {
            return nodeType == NodeType.MAJOR_ACTIVE;
        }

        public String prerequisiteSkillId() {
            return (prerequisiteIds != null && !prerequisiteIds.isEmpty()) ? prerequisiteIds.get(0) : null;
        }

        public Component getName() {
            if (customName != null && !customName.isEmpty()) {
                String skillKey = "awakening_system.skill." + skillId + ".name";
                String nodeKey = "awakening_system.skill_tree.node." + skillId + ".name";
                return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, customName).getString());
            }
            String skillKey = "awakening_system.skill." + skillId + ".name";
            String nodeKey = "awakening_system.skill_tree.node." + skillId + ".name";
            return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, skillId).getString());
        }

        public Component getDescription() {
            if (customDesc != null && !customDesc.isEmpty()) {
                String skillKey = "awakening_system.skill." + skillId + ".description";
                String nodeKey = "awakening_system.skill_tree.node." + skillId + ".desc";
                return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, customDesc).getString());
            }
            String skillKey = "awakening_system.skill." + skillId + ".description";
            String nodeKey = "awakening_system.skill_tree.node." + skillId + ".desc";
            return Component.translatableWithFallback(skillKey, Component.translatableWithFallback(nodeKey, "").getString());
        }

        public Component getEffect() {
            if (isAttribute() && statType != StatType.NONE) {
                String valStr = (statValue > 0 ? "+" : "") + (statType == StatType.MOVEMENT_SPEED ? String.format(Locale.US, "%.1f%%", statValue * 100) : (statType == StatType.ATTACK_SPEED ? String.format(Locale.US, "%.0f%%", statValue * 100) : String.format(Locale.US, "%.1f", statValue)));
                return Component.literal(valStr + " ").append(statName(statType));
            }
            String skillKey = "awakening_system.skill." + skillId + ".effect";
            return Component.translatableWithFallback(skillKey, "");
        }

        public SkillDefinitions.Rarity getRarity() {
            if (isRoot()) return SkillDefinitions.Rarity.UNIQUE;
            if (isAttribute()) return SkillDefinitions.Rarity.UNCOMMON;
            if (nodeType == NodeType.MAJOR_ACTIVE || nodeType == NodeType.MAJOR_PASSIVE) return SkillDefinitions.Rarity.EPIC;
            return SkillDefinitions.Rarity.RARE;
        }
    }

    public static Component statName(StatType type) {
        return switch (type) {
            case ATTACK_DAMAGE -> Component.translatable("awakening_system.stat.attack_damage");
            case MAX_HEALTH -> Component.translatable("awakening_system.stat.max_health");
            case MAX_MANA -> Component.translatable("awakening_system.stat.max_mana");
            case ATTACK_SPEED -> Component.translatable("awakening_system.stat.attack_speed");
            case MOVEMENT_SPEED -> Component.translatable("awakening_system.stat.movement_speed");
            case ARMOR -> Component.translatable("awakening_system.stat.armor");
            case KNOCKBACK_RESISTANCE -> Component.translatable("awakening_system.stat.knockback_resistance");
            default -> Component.empty();
        };
    }

    private static final Map<String, List<SkillNode>> CLASS_TREES = new LinkedHashMap<>();
    private static final Map<String, Map<String, SkillNode>> NODES_BY_CLASS_AND_SKILL = new LinkedHashMap<>();
    private static final Map<String, SkillNode> NODES_BY_SKILL = new LinkedHashMap<>();

    private static void register(SkillNode node) {
        CLASS_TREES.computeIfAbsent(node.hunterClass(), k -> new ArrayList<>()).add(node);
        NODES_BY_CLASS_AND_SKILL.computeIfAbsent(node.hunterClass(), k -> new LinkedHashMap<>()).put(node.skillId(), node);
        NODES_BY_SKILL.put(node.skillId(), node);
    }

    static {
        register(new SkillNode("shadow_monarch", "shadow_monarch", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "👑"));
        register(new SkillNode("sm_str_1", "shadow_monarch", "branch_a", 1, 255, 75, 30, 1, List.of("shadow_monarch"), StatType.ATTACK_DAMAGE, 2.5D, "⚔"));
        register(new SkillNode("shadow_exchange", "shadow_monarch", "branch_a", 2, 230, 135, 35, 1, List.of("sm_str_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("sm_mana_1", "shadow_monarch", "branch_a", 2, 280, 135, 35, 1, List.of("sm_str_1"), StatType.MAX_MANA, 50.0D, "🔮"));
        register(new SkillNode("monarchs_domain", "shadow_monarch", "branch_a", 3, 255, 195, 45, 1, List.of("shadow_exchange", "sm_mana_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("sm_armor_1", "shadow_monarch", "branch_a", 4, 230, 255, 55, 1, List.of("monarchs_domain"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("shadow_colossus_infusion", "shadow_monarch", "branch_a", 4, 280, 255, 55, 1, List.of("monarchs_domain"), NodeType.MAJOR_PASSIVE, "☠"));
        register(new SkillNode("sm_apex_1", "shadow_monarch", "branch_a", 5, 255, 320, 65, 1, List.of("sm_armor_1", "shadow_colossus_infusion"), StatType.ATTACK_DAMAGE, 5.0D, "★"));

        register(new SkillNode("sm_spd_1", "shadow_monarch", "branch_b", 1, 425, 75, 30, 1, List.of("shadow_monarch"), StatType.MOVEMENT_SPEED, 0.025D, "⚡"));
        register(new SkillNode("stealth_stalker", "shadow_monarch", "branch_b", 2, 400, 135, 35, 1, List.of("sm_spd_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("sm_atrspd_1", "shadow_monarch", "branch_b", 2, 450, 135, 35, 1, List.of("sm_spd_1"), StatType.ATTACK_SPEED, 0.04D, "⚔"));
        register(new SkillNode("bloodlust_gaze", "shadow_monarch", "branch_b", 3, 425, 195, 45, 1, List.of("stealth_stalker", "sm_atrspd_1"), NodeType.MAJOR_ACTIVE, "☠"));
        register(new SkillNode("sm_crit_1", "shadow_monarch", "branch_b", 4, 400, 255, 55, 1, List.of("bloodlust_gaze"), StatType.ATTACK_DAMAGE, 3.5D, "🗡"));
        register(new SkillNode("phantom_ambush", "shadow_monarch", "branch_b", 4, 450, 255, 55, 1, List.of("bloodlust_gaze"), NodeType.MAJOR_PASSIVE, "✦"));
        register(new SkillNode("sm_apex_2", "shadow_monarch", "branch_b", 5, 425, 320, 65, 1, List.of("sm_crit_1", "phantom_ambush"), StatType.ATTACK_SPEED, 0.08D, "★"));

        register(new SkillNode("sm_hp_1", "shadow_monarch", "branch_c", 1, 85, 75, 30, 1, List.of("shadow_monarch"), StatType.MAX_HEALTH, 6.0D, "❤"));
        register(new SkillNode("rulers_authority", "shadow_monarch", "branch_c", 2, 60, 135, 35, 1, List.of("sm_hp_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("sm_res_1", "shadow_monarch", "branch_c", 2, 110, 135, 35, 1, List.of("sm_hp_1"), StatType.KNOCKBACK_RESISTANCE, 0.15D, "🛡"));
        register(new SkillNode("black_heart_resurrection", "shadow_monarch", "branch_c", 3, 85, 195, 45, 1, List.of("rulers_authority", "sm_res_1"), NodeType.MAJOR_PASSIVE, "❤"));
        register(new SkillNode("sm_hp_2", "shadow_monarch", "branch_c", 4, 60, 255, 55, 1, List.of("black_heart_resurrection"), StatType.MAX_HEALTH, 10.0D, "❤"));
        register(new SkillNode("death_mark_execution", "shadow_monarch", "branch_c", 4, 110, 255, 55, 1, List.of("black_heart_resurrection"), NodeType.MAJOR_PASSIVE, "☠"));
        register(new SkillNode("sm_apex_3", "shadow_monarch", "branch_c", 5, 85, 320, 65, 1, List.of("sm_hp_2", "death_mark_execution"), StatType.ARMOR, 5.0D, "★"));

        register(new SkillNode("asrahan_mana_blade", "sword_saint", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🗡"));
        register(new SkillNode("ss_atrspd_1", "sword_saint", "branch_a", 1, 255, 75, 30, 1, List.of("asrahan_mana_blade"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("plum_blossom_rain", "sword_saint", "branch_a", 2, 230, 135, 35, 1, List.of("ss_atrspd_1"), NodeType.MAJOR_ACTIVE, "🌸"));
        register(new SkillNode("ss_atk_1", "sword_saint", "branch_a", 2, 280, 135, 35, 1, List.of("ss_atrspd_1"), StatType.ATTACK_DAMAGE, 2.0D, "🗡"));
        register(new SkillNode("twenty_four_sword_forms", "sword_saint", "branch_a", 3, 255, 195, 45, 1, List.of("plum_blossom_rain", "ss_atk_1"), NodeType.MAJOR_ACTIVE, "🌸"));
        register(new SkillNode("ss_spd_1", "sword_saint", "branch_a", 4, 230, 255, 55, 1, List.of("twenty_four_sword_forms"), StatType.MOVEMENT_SPEED, 0.02D, "⚡"));
        register(new SkillNode("plum_blossom_domain", "sword_saint", "branch_a", 4, 280, 255, 55, 1, List.of("twenty_four_sword_forms"), NodeType.MAJOR_ACTIVE, "🌸"));
        register(new SkillNode("plum_blossom_sword_god", "sword_saint", "branch_a", 5, 255, 320, 65, 1, List.of("ss_spd_1", "plum_blossom_domain"), NodeType.MAJOR_ACTIVE, "★"));

        register(new SkillNode("ss_atk_2", "sword_saint", "branch_b", 1, 425, 75, 30, 1, List.of("asrahan_mana_blade"), StatType.ATTACK_DAMAGE, 2.5D, "⚔"));
        register(new SkillNode("sword_intent", "sword_saint", "branch_b", 2, 400, 135, 35, 1, List.of("ss_atk_2"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("ss_mana_1", "sword_saint", "branch_b", 2, 450, 135, 35, 1, List.of("ss_atk_2"), StatType.MAX_MANA, 40.0D, "🔮"));
        register(new SkillNode("daoist_clarity", "sword_saint", "branch_b", 3, 425, 195, 45, 1, List.of("sword_intent", "ss_mana_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("ss_atrspd_2", "sword_saint", "branch_b", 4, 400, 255, 55, 1, List.of("daoist_clarity"), StatType.ATTACK_SPEED, 0.06D, "⚔"));
        register(new SkillNode("sword_heart_awakening", "sword_saint", "branch_b", 4, 450, 255, 55, 1, List.of("daoist_clarity"), NodeType.MAJOR_PASSIVE, "❤"));
        register(new SkillNode("limitless_sword_intent", "sword_saint", "branch_b", 5, 425, 320, 65, 1, List.of("ss_atrspd_2", "sword_heart_awakening"), NodeType.MAJOR_PASSIVE, "★"));

        register(new SkillNode("ss_hp_1", "sword_saint", "branch_c", 1, 85, 75, 30, 1, List.of("asrahan_mana_blade"), StatType.MAX_HEALTH, 6.0D, "❤"));
        register(new SkillNode("ss_armor_1", "sword_saint", "branch_c", 2, 60, 135, 35, 1, List.of("ss_hp_1"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("ss_kb_1", "sword_saint", "branch_c", 2, 110, 135, 35, 1, List.of("ss_hp_1"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "🛡"));
        register(new SkillNode("iron_will", "sword_saint", "branch_c", 3, 85, 195, 45, 1, List.of("ss_armor_1", "ss_kb_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("ss_hp_2", "sword_saint", "branch_c", 4, 60, 255, 55, 1, List.of("iron_will"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("ss_armor_2", "sword_saint", "branch_c", 4, 110, 255, 55, 1, List.of("iron_will"), StatType.ARMOR, 4.0D, "🛡"));
        register(new SkillNode("ss_apex_3", "sword_saint", "branch_c", 5, 85, 320, 65, 1, List.of("ss_hp_2", "ss_armor_2"), StatType.ATTACK_DAMAGE, 4.0D, "★"));

        register(new SkillNode("fourth_wall", "star_apostle", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "📜"));
        register(new SkillNode("sa_mana_1", "star_apostle", "branch_a", 1, 255, 75, 30, 1, List.of("fourth_wall"), StatType.MAX_MANA, 50.0D, "🔮"));
        register(new SkillNode("readers_viewpoint", "star_apostle", "branch_a", 2, 230, 135, 35, 1, List.of("sa_mana_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("sa_atk_1", "star_apostle", "branch_a", 2, 280, 135, 35, 1, List.of("sa_mana_1"), StatType.ATTACK_DAMAGE, 2.0D, "⚔"));
        register(new SkillNode("constellation_favor", "star_apostle", "branch_a", 3, 255, 195, 45, 1, List.of("readers_viewpoint", "sa_atk_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("sa_spd_1", "star_apostle", "branch_a", 4, 230, 255, 55, 1, List.of("constellation_favor"), StatType.MOVEMENT_SPEED, 0.025D, "⚡"));
        register(new SkillNode("fourth_wall_shatter", "star_apostle", "branch_a", 4, 280, 255, 55, 1, List.of("constellation_favor"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("star_stream_climax", "star_apostle", "branch_a", 5, 255, 320, 65, 1, List.of("sa_spd_1", "fourth_wall_shatter"), NodeType.MAJOR_ACTIVE, "★"));

        register(new SkillNode("sa_atrspd_1", "star_apostle", "branch_b", 1, 425, 75, 30, 1, List.of("fourth_wall"), StatType.ATTACK_SPEED, 0.04D, "⚔"));
        register(new SkillNode("bookmark_skill", "star_apostle", "branch_b", 2, 400, 135, 35, 1, List.of("sa_atrspd_1"), NodeType.MAJOR_ACTIVE, "📑"));
        register(new SkillNode("sa_mana_2", "star_apostle", "branch_b", 2, 450, 135, 35, 1, List.of("sa_atrspd_1"), StatType.MAX_MANA, 60.0D, "🔮"));
        register(new SkillNode("probability_distortion", "star_apostle", "branch_b", 3, 425, 195, 45, 1, List.of("bookmark_skill", "sa_mana_2"), NodeType.MAJOR_ACTIVE, "🎲"));
        register(new SkillNode("sa_atk_2", "star_apostle", "branch_b", 4, 400, 255, 55, 1, List.of("probability_distortion"), StatType.ATTACK_DAMAGE, 3.5D, "⚔"));
        register(new SkillNode("bookmark_mastery", "star_apostle", "branch_b", 4, 450, 255, 55, 1, List.of("probability_distortion"), NodeType.MAJOR_PASSIVE, "📑"));
        register(new SkillNode("character_summary_scan", "star_apostle", "branch_b", 5, 425, 320, 65, 1, List.of("sa_atk_2", "bookmark_mastery"), NodeType.MAJOR_PASSIVE, "★"));

        register(new SkillNode("sa_hp_1", "star_apostle", "branch_c", 1, 85, 75, 30, 1, List.of("fourth_wall"), StatType.MAX_HEALTH, 6.0D, "❤"));
        register(new SkillNode("sa_wall_shield", "star_apostle", "branch_c", 2, 60, 135, 35, 1, List.of("sa_hp_1"), NodeType.MAJOR_PASSIVE, "🧱"));
        register(new SkillNode("sa_armor_1", "star_apostle", "branch_c", 2, 110, 135, 35, 1, List.of("sa_hp_1"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("sa_pain_suppression", "star_apostle", "branch_c", 3, 85, 195, 45, 1, List.of("sa_wall_shield", "sa_armor_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("sa_hp_2", "star_apostle", "branch_c", 4, 60, 255, 55, 1, List.of("sa_pain_suppression"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("sa_res_1", "star_apostle", "branch_c", 4, 110, 255, 55, 1, List.of("sa_pain_suppression"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "🛡"));
        register(new SkillNode("sa_apex_3", "star_apostle", "branch_c", 5, 85, 320, 65, 1, List.of("sa_hp_2", "sa_res_1"), StatType.MAX_HEALTH, 10.0D, "★"));

        register(new SkillNode("cb_valterian_instinct", "colossal_berserker", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🪓"));
        register(new SkillNode("cb_atk_1", "colossal_berserker", "branch_a", 1, 255, 75, 30, 1, List.of("cb_valterian_instinct"), StatType.ATTACK_DAMAGE, 3.0D, "⚔"));
        register(new SkillNode("aura_smash", "colossal_berserker", "branch_a", 2, 230, 135, 35, 1, List.of("cb_atk_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("cb_kb_1", "colossal_berserker", "branch_a", 2, 280, 135, 35, 1, List.of("cb_atk_1"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "🛡"));
        register(new SkillNode("cross_impact", "colossal_berserker", "branch_a", 3, 255, 195, 45, 1, List.of("aura_smash", "cb_kb_1"), NodeType.MAJOR_ACTIVE, "⚔"));
        register(new SkillNode("cb_atk_2", "colossal_berserker", "branch_a", 4, 230, 255, 55, 1, List.of("cross_impact"), StatType.ATTACK_DAMAGE, 4.0D, "🪓"));
        register(new SkillNode("valterian_gigantomachia", "colossal_berserker", "branch_a", 4, 280, 255, 55, 1, List.of("cross_impact"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("cb_apex_1", "colossal_berserker", "branch_a", 5, 255, 320, 65, 1, List.of("cb_atk_2", "valterian_gigantomachia"), StatType.ATTACK_DAMAGE, 6.0D, "★"));

        register(new SkillNode("cb_hp_1", "colossal_berserker", "branch_b", 1, 425, 75, 30, 1, List.of("cb_valterian_instinct"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("iron_muscle_hardening", "colossal_berserker", "branch_b", 2, 400, 135, 35, 1, List.of("cb_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("cb_armor_1", "colossal_berserker", "branch_b", 2, 450, 135, 35, 1, List.of("cb_hp_1"), StatType.ARMOR, 3.5D, "🛡"));
        register(new SkillNode("immortal_berserk", "colossal_berserker", "branch_b", 3, 425, 195, 45, 1, List.of("iron_muscle_hardening", "cb_armor_1"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("cb_hp_2", "colossal_berserker", "branch_b", 4, 400, 255, 55, 1, List.of("immortal_berserk"), StatType.MAX_HEALTH, 12.0D, "❤"));
        register(new SkillNode("blood_frenzy_passive", "colossal_berserker", "branch_b", 4, 450, 255, 55, 1, List.of("immortal_berserk"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("cb_apex_2", "colossal_berserker", "branch_b", 5, 425, 320, 65, 1, List.of("cb_hp_2", "blood_frenzy_passive"), StatType.MAX_HEALTH, 16.0D, "★"));

        register(new SkillNode("cb_atrspd_1", "colossal_berserker", "branch_c", 1, 85, 75, 30, 1, List.of("cb_valterian_instinct"), StatType.ATTACK_SPEED, 0.04D, "⚔"));
        register(new SkillNode("cb_bloodthirst", "colossal_berserker", "branch_c", 2, 60, 135, 35, 1, List.of("cb_atrspd_1"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("cb_spd_1", "colossal_berserker", "branch_c", 2, 110, 135, 35, 1, List.of("cb_atrspd_1"), StatType.MOVEMENT_SPEED, 0.02D, "⚡"));
        register(new SkillNode("seismic_roar", "colossal_berserker", "branch_c", 3, 85, 195, 45, 1, List.of("cb_bloodthirst", "cb_spd_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("cb_armor_2", "colossal_berserker", "branch_c", 4, 60, 255, 55, 1, List.of("seismic_roar"), StatType.ARMOR, 4.0D, "🛡"));
        register(new SkillNode("cb_pain_suppression", "colossal_berserker", "branch_c", 4, 110, 255, 55, 1, List.of("seismic_roar"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("cb_apex_3", "colossal_berserker", "branch_c", 5, 85, 320, 65, 1, List.of("cb_armor_2", "cb_pain_suppression"), StatType.KNOCKBACK_RESISTANCE, 0.40D, "★"));

        register(new SkillNode("black_fire_wave", "demonic_cultivator", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🔥"));
        register(new SkillNode("dc_atk_1", "demonic_cultivator", "branch_a", 1, 255, 75, 30, 1, List.of("black_fire_wave"), StatType.ATTACK_DAMAGE, 2.5D, "⚔"));
        register(new SkillNode("heavenly_demon_steps", "demonic_cultivator", "branch_a", 2, 230, 135, 35, 1, List.of("dc_atk_1"), NodeType.MAJOR_ACTIVE, "💨"));
        register(new SkillNode("dc_atrspd_1", "demonic_cultivator", "branch_a", 2, 280, 135, 35, 1, List.of("dc_atk_1"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("demonic_blood_slash", "demonic_cultivator", "branch_a", 3, 255, 195, 45, 1, List.of("heavenly_demon_steps", "dc_atrspd_1"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("dc_atk_2", "demonic_cultivator", "branch_a", 4, 230, 255, 55, 1, List.of("demonic_blood_slash"), StatType.ATTACK_DAMAGE, 4.0D, "⚔"));
        register(new SkillNode("sky_demon_nine_reversals", "demonic_cultivator", "branch_a", 4, 280, 255, 55, 1, List.of("demonic_blood_slash"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("dc_apex_1", "demonic_cultivator", "branch_a", 5, 255, 320, 65, 1, List.of("dc_atk_2", "sky_demon_nine_reversals"), StatType.ATTACK_DAMAGE, 5.0D, "★"));

        register(new SkillNode("dc_mana_1", "demonic_cultivator", "branch_b", 1, 425, 75, 30, 1, List.of("black_fire_wave"), StatType.MAX_MANA, 50.0D, "🔮"));
        register(new SkillNode("nano_scan_weakness", "demonic_cultivator", "branch_b", 2, 400, 135, 35, 1, List.of("dc_mana_1"), NodeType.MAJOR_ACTIVE, "🔍"));
        register(new SkillNode("dc_spd_1", "demonic_cultivator", "branch_b", 2, 450, 135, 35, 1, List.of("dc_mana_1"), StatType.MOVEMENT_SPEED, 0.02D, "⚡"));
        register(new SkillNode("nano_cellular_regeneration", "demonic_cultivator", "branch_b", 3, 425, 195, 45, 1, List.of("nano_scan_weakness", "dc_spd_1"), NodeType.MAJOR_PASSIVE, "💉"));
        register(new SkillNode("dc_armor_1", "demonic_cultivator", "branch_b", 4, 400, 255, 55, 1, List.of("nano_cellular_regeneration"), StatType.ARMOR, 3.5D, "🛡"));
        register(new SkillNode("nano_overdrive_suit", "demonic_cultivator", "branch_b", 4, 450, 255, 55, 1, List.of("nano_cellular_regeneration"), NodeType.MAJOR_ACTIVE, "🤖"));
        register(new SkillNode("dc_apex_2", "demonic_cultivator", "branch_b", 5, 425, 320, 65, 1, List.of("dc_armor_1", "nano_overdrive_suit"), StatType.ATTACK_SPEED, 0.08D, "★"));

        register(new SkillNode("dc_hp_1", "demonic_cultivator", "branch_c", 1, 85, 75, 30, 1, List.of("black_fire_wave"), StatType.MAX_HEALTH, 6.0D, "❤"));
        register(new SkillNode("dc_nano_blade_overdrive", "demonic_cultivator", "branch_c", 2, 60, 135, 35, 1, List.of("dc_hp_1"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("dc_mana_2", "demonic_cultivator", "branch_c", 2, 110, 135, 35, 1, List.of("dc_hp_1"), StatType.MAX_MANA, 60.0D, "🔮"));
        register(new SkillNode("asura_bloodline", "demonic_cultivator", "branch_c", 3, 85, 195, 45, 1, List.of("dc_nano_blade_overdrive", "dc_mana_2"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("dc_hp_2", "demonic_cultivator", "branch_c", 4, 60, 255, 55, 1, List.of("asura_bloodline"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("dc_kb_1", "demonic_cultivator", "branch_c", 4, 110, 255, 55, 1, List.of("asura_bloodline"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "🛡"));
        register(new SkillNode("dc_apex_3", "demonic_cultivator", "branch_c", 5, 85, 320, 65, 1, List.of("dc_hp_2", "dc_kb_1"), StatType.MAX_HEALTH, 12.0D, "★"));

        register(new SkillNode("nano_blade_overdrive", "void_assassin", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🗡"));
        register(new SkillNode("va_spd_1", "void_assassin", "branch_a", 1, 255, 75, 30, 1, List.of("nano_blade_overdrive"), StatType.MOVEMENT_SPEED, 0.03D, "⚡"));
        register(new SkillNode("shadow_step_blink", "void_assassin", "branch_a", 2, 230, 135, 35, 1, List.of("va_spd_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("va_atk_1", "void_assassin", "branch_a", 2, 280, 135, 35, 1, List.of("va_spd_1"), StatType.ATTACK_DAMAGE, 2.5D, "⚔"));
        register(new SkillNode("vital_strike", "void_assassin", "branch_a", 3, 255, 195, 45, 1, List.of("shadow_step_blink", "va_atk_1"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("va_crit_1", "void_assassin", "branch_a", 4, 230, 255, 55, 1, List.of("vital_strike"), StatType.ATTACK_DAMAGE, 3.5D, "🗡"));
        register(new SkillNode("infinite_mirror_blades", "void_assassin", "branch_a", 4, 280, 255, 55, 1, List.of("vital_strike"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("va_apex_1", "void_assassin", "branch_a", 5, 255, 320, 65, 1, List.of("va_crit_1", "infinite_mirror_blades"), StatType.ATTACK_DAMAGE, 5.0D, "★"));

        register(new SkillNode("va_atrspd_1", "void_assassin", "branch_b", 1, 425, 75, 30, 1, List.of("nano_blade_overdrive"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("predator_sense", "void_assassin", "branch_b", 2, 400, 135, 35, 1, List.of("va_atrspd_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("va_mana_1", "void_assassin", "branch_b", 2, 450, 135, 35, 1, List.of("va_atrspd_1"), StatType.MAX_MANA, 40.0D, "🔮"));
        register(new SkillNode("smoke_bomb_escape", "void_assassin", "branch_b", 3, 425, 195, 45, 1, List.of("predator_sense", "va_mana_1"), NodeType.MAJOR_ACTIVE, "💨"));
        register(new SkillNode("va_atrspd_2", "void_assassin", "branch_b", 4, 400, 255, 55, 1, List.of("smoke_bomb_escape"), StatType.ATTACK_SPEED, 0.07D, "⚔"));
        register(new SkillNode("max_level_privilege", "void_assassin", "branch_b", 4, 450, 255, 55, 1, List.of("smoke_bomb_escape"), NodeType.MAJOR_PASSIVE, "✦"));
        register(new SkillNode("va_phantom_ambush", "void_assassin", "branch_b", 5, 425, 320, 65, 1, List.of("va_atrspd_2", "max_level_privilege"), NodeType.MAJOR_PASSIVE, "★"));

        register(new SkillNode("va_hp_1", "void_assassin", "branch_c", 1, 85, 75, 30, 1, List.of("nano_blade_overdrive"), StatType.MAX_HEALTH, 5.0D, "❤"));
        register(new SkillNode("va_stealth_stalker", "void_assassin", "branch_c", 2, 60, 135, 35, 1, List.of("va_hp_1"), NodeType.MAJOR_ACTIVE, "👁"));
        register(new SkillNode("va_spd_2", "void_assassin", "branch_c", 2, 110, 135, 35, 1, List.of("va_hp_1"), StatType.MOVEMENT_SPEED, 0.02D, "⚡"));
        register(new SkillNode("va_death_mark", "void_assassin", "branch_c", 3, 85, 195, 45, 1, List.of("va_stealth_stalker", "va_spd_2"), NodeType.MAJOR_PASSIVE, "☠"));
        register(new SkillNode("va_armor_1", "void_assassin", "branch_c", 4, 60, 255, 55, 1, List.of("va_death_mark"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("va_hp_2", "void_assassin", "branch_c", 4, 110, 255, 55, 1, List.of("va_death_mark"), StatType.MAX_HEALTH, 7.0D, "❤"));
        register(new SkillNode("va_apex_3", "void_assassin", "branch_c", 5, 85, 320, 65, 1, List.of("va_armor_1", "va_hp_2"), StatType.MOVEMENT_SPEED, 0.04D, "★"));

        register(new SkillNode("isekai_mana_burst", "grand_archmage", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🔮"));
        register(new SkillNode("ga_mana_1", "grand_archmage", "branch_a", 1, 255, 75, 30, 1, List.of("isekai_mana_burst"), StatType.MAX_MANA, 60.0D, "🔮"));
        register(new SkillNode("elemental_spear", "grand_archmage", "branch_a", 2, 230, 135, 35, 1, List.of("ga_mana_1"), NodeType.MAJOR_ACTIVE, "⚡"));
        register(new SkillNode("ga_atk_1", "grand_archmage", "branch_a", 2, 280, 135, 35, 1, List.of("ga_mana_1"), StatType.ATTACK_DAMAGE, 2.5D, "⚔"));
        register(new SkillNode("gravity_crush", "grand_archmage", "branch_a", 3, 255, 195, 45, 1, List.of("elemental_spear", "ga_atk_1"), NodeType.MAJOR_ACTIVE, "🌀"));
        register(new SkillNode("ga_mana_2", "grand_archmage", "branch_a", 4, 230, 255, 55, 1, List.of("gravity_crush"), StatType.MAX_MANA, 80.0D, "🔮"));
        register(new SkillNode("supernova_incineration", "grand_archmage", "branch_a", 4, 280, 255, 55, 1, List.of("gravity_crush"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("ga_apex_1", "grand_archmage", "branch_a", 5, 255, 320, 65, 1, List.of("ga_mana_2", "supernova_incineration"), StatType.MAX_MANA, 120.0D, "★"));

        register(new SkillNode("ga_spd_1", "grand_archmage", "branch_b", 1, 425, 75, 30, 1, List.of("isekai_mana_burst"), StatType.MOVEMENT_SPEED, 0.02D, "⚡"));
        register(new SkillNode("mana_shield_barrier", "grand_archmage", "branch_b", 2, 400, 135, 35, 1, List.of("ga_spd_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("ga_armor_1", "grand_archmage", "branch_b", 2, 450, 135, 35, 1, List.of("ga_spd_1"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("arcane_overload", "grand_archmage", "branch_b", 3, 425, 195, 45, 1, List.of("mana_shield_barrier", "ga_armor_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("ga_atrspd_1", "grand_archmage", "branch_b", 4, 400, 255, 55, 1, List.of("arcane_overload"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("time_dilation", "grand_archmage", "branch_b", 4, 450, 255, 55, 1, List.of("arcane_overload"), NodeType.MAJOR_ACTIVE, "⏳"));
        register(new SkillNode("ga_apex_2", "grand_archmage", "branch_b", 5, 425, 320, 65, 1, List.of("ga_atrspd_1", "time_dilation"), StatType.ATTACK_DAMAGE, 5.0D, "★"));

        register(new SkillNode("ga_hp_1", "grand_archmage", "branch_c", 1, 85, 75, 30, 1, List.of("isekai_mana_burst"), StatType.MAX_HEALTH, 5.0D, "❤"));
        register(new SkillNode("ga_mana_3", "grand_archmage", "branch_c", 2, 60, 135, 35, 1, List.of("ga_hp_1"), StatType.MAX_MANA, 70.0D, "🔮"));
        register(new SkillNode("ga_armor_2", "grand_archmage", "branch_c", 2, 110, 135, 35, 1, List.of("ga_hp_1"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("ga_pain_suppression", "grand_archmage", "branch_c", 3, 85, 195, 45, 1, List.of("ga_mana_3", "ga_armor_2"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("ga_hp_2", "grand_archmage", "branch_c", 4, 60, 255, 55, 1, List.of("ga_pain_suppression"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("ga_kb_1", "grand_archmage", "branch_c", 4, 110, 255, 55, 1, List.of("ga_pain_suppression"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "🛡"));
        register(new SkillNode("ga_apex_3", "grand_archmage", "branch_c", 5, 85, 320, 65, 1, List.of("ga_hp_2", "ga_kb_1"), StatType.MAX_HEALTH, 12.0D, "★"));

        register(new SkillNode("isekai_dragon_roar", "dragon_monarch", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🐉"));
        register(new SkillNode("dm_atk_1", "dragon_monarch", "branch_a", 1, 255, 75, 30, 1, List.of("isekai_dragon_roar"), StatType.ATTACK_DAMAGE, 3.0D, "🔥"));
        register(new SkillNode("dragon_breath", "dragon_monarch", "branch_a", 2, 230, 135, 35, 1, List.of("dm_atk_1"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("dm_mana_1", "dragon_monarch", "branch_a", 2, 280, 135, 35, 1, List.of("dm_atk_1"), StatType.MAX_MANA, 50.0D, "🔮"));
        register(new SkillNode("meteor_fall", "dragon_monarch", "branch_a", 3, 255, 195, 45, 1, List.of("dragon_breath", "dm_mana_1"), NodeType.MAJOR_ACTIVE, "☄"));
        register(new SkillNode("dm_atk_2", "dragon_monarch", "branch_a", 4, 230, 255, 55, 1, List.of("meteor_fall"), StatType.ATTACK_DAMAGE, 4.0D, "🔥"));
        register(new SkillNode("breath_of_annihilation", "dragon_monarch", "branch_a", 4, 280, 255, 55, 1, List.of("meteor_fall"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("dm_apex_1", "dragon_monarch", "branch_a", 5, 255, 320, 65, 1, List.of("dm_atk_2", "breath_of_annihilation"), StatType.ATTACK_DAMAGE, 6.0D, "★"));

        register(new SkillNode("dm_armor_1", "dragon_monarch", "branch_b", 1, 425, 75, 30, 1, List.of("isekai_dragon_roar"), StatType.ARMOR, 3.5D, "🛡"));
        register(new SkillNode("dragon_scales_hardening", "dragon_monarch", "branch_b", 2, 400, 135, 35, 1, List.of("dm_armor_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("dm_hp_1", "dragon_monarch", "branch_b", 2, 450, 135, 35, 1, List.of("dm_armor_1"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("dragon_wings_flight", "dragon_monarch", "branch_b", 3, 425, 195, 45, 1, List.of("dragon_scales_hardening", "dm_hp_1"), NodeType.MAJOR_ACTIVE, "🪽"));
        register(new SkillNode("dm_armor_2", "dragon_monarch", "branch_b", 4, 400, 255, 55, 1, List.of("dragon_wings_flight"), StatType.ARMOR, 4.5D, "🛡"));
        register(new SkillNode("dragon_form_avatar", "dragon_monarch", "branch_b", 4, 450, 255, 55, 1, List.of("dragon_wings_flight"), NodeType.MAJOR_ACTIVE, "🐉"));
        register(new SkillNode("dm_apex_2", "dragon_monarch", "branch_b", 5, 425, 320, 65, 1, List.of("dm_armor_2", "dragon_form_avatar"), StatType.MAX_HEALTH, 14.0D, "★"));

        register(new SkillNode("dm_kb_1", "dragon_monarch", "branch_c", 1, 85, 75, 30, 1, List.of("isekai_dragon_roar"), StatType.KNOCKBACK_RESISTANCE, 0.25D, "🛡"));
        register(new SkillNode("dm_dragon_dive", "dragon_monarch", "branch_c", 2, 60, 135, 35, 1, List.of("dm_kb_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("dm_spd_1", "dragon_monarch", "branch_c", 2, 110, 135, 35, 1, List.of("dm_kb_1"), StatType.MOVEMENT_SPEED, 0.02D, "⚡"));
        register(new SkillNode("dm_bloodthirst", "dragon_monarch", "branch_c", 3, 85, 195, 45, 1, List.of("dm_dragon_dive", "dm_spd_1"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("dm_hp_2", "dragon_monarch", "branch_c", 4, 60, 255, 55, 1, List.of("dm_bloodthirst"), StatType.MAX_HEALTH, 10.0D, "❤"));
        register(new SkillNode("dm_atrspd_1", "dragon_monarch", "branch_c", 4, 110, 255, 55, 1, List.of("dm_bloodthirst"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("dm_apex_3", "dragon_monarch", "branch_c", 5, 85, 320, 65, 1, List.of("dm_hp_2", "dm_atrspd_1"), StatType.KNOCKBACK_RESISTANCE, 0.35D, "★"));

        register(new SkillNode("sh_caregiver_blessing", "spiritual_healer", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "✨"));
        register(new SkillNode("sh_mana_1", "spiritual_healer", "branch_a", 1, 255, 75, 30, 1, List.of("sh_caregiver_blessing"), StatType.MAX_MANA, 50.0D, "🔮"));
        register(new SkillNode("sh_holy_touch", "spiritual_healer", "branch_a", 2, 230, 135, 35, 1, List.of("sh_mana_1"), NodeType.MAJOR_ACTIVE, "💚"));
        register(new SkillNode("sh_hp_1", "spiritual_healer", "branch_a", 2, 280, 135, 35, 1, List.of("sh_mana_1"), StatType.MAX_HEALTH, 6.0D, "❤"));
        register(new SkillNode("sh_divine_grace", "spiritual_healer", "branch_a", 3, 255, 195, 45, 1, List.of("sh_holy_touch", "sh_hp_1"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("sh_res_1", "spiritual_healer", "branch_a", 4, 230, 255, 55, 1, List.of("sh_divine_grace"), StatType.KNOCKBACK_RESISTANCE, 0.20D, "🛡"));
        register(new SkillNode("sh_martyr_soul", "spiritual_healer", "branch_a", 4, 280, 255, 55, 1, List.of("sh_divine_grace"), NodeType.MAJOR_PASSIVE, "👼"));
        register(new SkillNode("sh_apex_miracle", "spiritual_healer", "branch_a", 5, 255, 320, 65, 1, List.of("sh_res_1", "sh_martyr_soul"), StatType.MAX_HEALTH, 14.0D, "★"));

        register(new SkillNode("sh_armor_1", "spiritual_healer", "branch_b", 1, 425, 75, 30, 1, List.of("sh_caregiver_blessing"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("sh_sacred_sanctuary", "spiritual_healer", "branch_b", 2, 400, 135, 35, 1, List.of("sh_armor_1"), NodeType.MAJOR_ACTIVE, "⛪"));
        register(new SkillNode("sh_mana_2", "spiritual_healer", "branch_b", 2, 450, 135, 35, 1, List.of("sh_armor_1"), StatType.MAX_MANA, 60.0D, "🔮"));
        register(new SkillNode("sh_cleansing_radiance", "spiritual_healer", "branch_b", 3, 425, 195, 45, 1, List.of("sh_sacred_sanctuary", "sh_mana_2"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("sh_armor_2", "spiritual_healer", "branch_b", 4, 400, 255, 55, 1, List.of("sh_cleansing_radiance"), StatType.ARMOR, 4.0D, "🛡"));
        register(new SkillNode("sh_aegis_immortal", "spiritual_healer", "branch_b", 4, 450, 255, 55, 1, List.of("sh_cleansing_radiance"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("sh_apex_sanctuary", "spiritual_healer", "branch_b", 5, 425, 320, 65, 1, List.of("sh_armor_2", "sh_aegis_immortal"), StatType.ARMOR, 6.0D, "★"));

        register(new SkillNode("sh_spd_1", "spiritual_healer", "branch_c", 1, 85, 75, 30, 1, List.of("sh_caregiver_blessing"), StatType.MOVEMENT_SPEED, 0.025D, "⚡"));
        register(new SkillNode("sh_tamer_bond", "spiritual_healer", "branch_c", 2, 60, 135, 35, 1, List.of("sh_spd_1"), NodeType.MAJOR_ACTIVE, "🐾"));
        register(new SkillNode("sh_hp_2", "spiritual_healer", "branch_c", 2, 110, 135, 35, 1, List.of("sh_spd_1"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("sh_awakening_inspire", "spiritual_healer", "branch_c", 3, 85, 195, 45, 1, List.of("sh_tamer_bond", "sh_hp_2"), NodeType.MAJOR_ACTIVE, "🌟"));
        register(new SkillNode("sh_atrspd_1", "spiritual_healer", "branch_c", 4, 60, 255, 55, 1, List.of("sh_awakening_inspire"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("sh_beast_empathy", "spiritual_healer", "branch_c", 4, 110, 255, 55, 1, List.of("sh_awakening_inspire"), NodeType.MAJOR_PASSIVE, "🕊"));
        register(new SkillNode("sh_apex_caregiver", "spiritual_healer", "branch_c", 5, 85, 320, 65, 1, List.of("sh_atrspd_1", "sh_beast_empathy"), StatType.MAX_MANA, 100.0D, "★"));

        register(new SkillNode("le_electrification", "lightning_emperor", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "⚡"));
        register(new SkillNode("le_atk_1", "lightning_emperor", "branch_a", 1, 255, 75, 30, 1, List.of("le_electrification"), StatType.ATTACK_DAMAGE, 2.5D, "⚔"));
        register(new SkillNode("le_thunder_spear", "lightning_emperor", "branch_a", 2, 230, 135, 35, 1, List.of("le_atk_1"), NodeType.MAJOR_ACTIVE, "⚡"));
        register(new SkillNode("le_mana_1", "lightning_emperor", "branch_a", 2, 280, 135, 35, 1, List.of("le_atk_1"), StatType.MAX_MANA, 50.0D, "🔮"));
        register(new SkillNode("le_plasma_burst", "lightning_emperor", "branch_a", 3, 255, 195, 45, 1, List.of("le_thunder_spear", "le_mana_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("le_atk_2", "lightning_emperor", "branch_a", 4, 230, 255, 55, 1, List.of("le_plasma_burst"), StatType.ATTACK_DAMAGE, 4.0D, "⚔"));
        register(new SkillNode("le_wrath_of_thunder_god", "lightning_emperor", "branch_a", 4, 280, 255, 55, 1, List.of("le_plasma_burst"), NodeType.MAJOR_ACTIVE, "🌩"));
        register(new SkillNode("le_apex_thunder", "lightning_emperor", "branch_a", 5, 255, 320, 65, 1, List.of("le_atk_2", "le_wrath_of_thunder_god"), StatType.ATTACK_DAMAGE, 6.0D, "★"));

        register(new SkillNode("le_spd_1", "lightning_emperor", "branch_b", 1, 425, 75, 30, 1, List.of("le_electrification"), StatType.MOVEMENT_SPEED, 0.03D, "⚡"));
        register(new SkillNode("le_flash_step", "lightning_emperor", "branch_b", 2, 400, 135, 35, 1, List.of("le_spd_1"), NodeType.MAJOR_ACTIVE, "💨"));
        register(new SkillNode("le_atrspd_1", "lightning_emperor", "branch_b", 2, 450, 135, 35, 1, List.of("le_spd_1"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("le_overdrive_speed", "lightning_emperor", "branch_b", 3, 425, 195, 45, 1, List.of("le_flash_step", "le_atrspd_1"), NodeType.MAJOR_ACTIVE, "⚡"));
        register(new SkillNode("le_spd_2", "lightning_emperor", "branch_b", 4, 400, 255, 55, 1, List.of("le_overdrive_speed"), StatType.MOVEMENT_SPEED, 0.04D, "⚡"));
        register(new SkillNode("le_static_discharge", "lightning_emperor", "branch_b", 4, 450, 255, 55, 1, List.of("le_overdrive_speed"), NodeType.MAJOR_PASSIVE, "⚡"));
        register(new SkillNode("le_apex_speed", "lightning_emperor", "branch_b", 5, 425, 320, 65, 1, List.of("le_spd_2", "le_static_discharge"), StatType.ATTACK_SPEED, 0.08D, "★"));

        register(new SkillNode("le_hp_1", "lightning_emperor", "branch_c", 1, 85, 75, 30, 1, List.of("le_electrification"), StatType.MAX_HEALTH, 6.0D, "❤"));
        register(new SkillNode("le_lightning_armor", "lightning_emperor", "branch_c", 2, 60, 135, 35, 1, List.of("le_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("le_armor_1", "lightning_emperor", "branch_c", 2, 110, 135, 35, 1, List.of("le_hp_1"), StatType.ARMOR, 3.0D, "🛡"));
        register(new SkillNode("le_grounding_soul", "lightning_emperor", "branch_c", 3, 85, 195, 45, 1, List.of("le_lightning_armor", "le_armor_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("le_hp_2", "lightning_emperor", "branch_c", 4, 60, 255, 55, 1, List.of("le_grounding_soul"), StatType.MAX_HEALTH, 10.0D, "❤"));
        register(new SkillNode("le_vajra_indestructible", "lightning_emperor", "branch_c", 4, 110, 255, 55, 1, List.of("le_grounding_soul"), NodeType.MAJOR_ACTIVE, "✨"));
        register(new SkillNode("le_apex_vajra", "lightning_emperor", "branch_c", 5, 85, 320, 65, 1, List.of("le_hp_2", "le_vajra_indestructible"), StatType.MAX_HEALTH, 14.0D, "★"));

        register(new SkillNode("be_bloodthirst", "blood_emperor", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🩸"));
        register(new SkillNode("be_atk_1", "blood_emperor", "branch_a", 1, 255, 75, 30, 1, List.of("be_bloodthirst"), StatType.ATTACK_DAMAGE, 3.0D, "⚔"));
        register(new SkillNode("be_blood_lance", "blood_emperor", "branch_a", 2, 230, 135, 35, 1, List.of("be_atk_1"), NodeType.MAJOR_ACTIVE, "🗡"));
        register(new SkillNode("be_mana_1", "blood_emperor", "branch_a", 2, 280, 135, 35, 1, List.of("be_atk_1"), StatType.MAX_MANA, 40.0D, "🔮"));
        register(new SkillNode("be_crimson_eruption", "blood_emperor", "branch_a", 3, 255, 195, 45, 1, List.of("be_blood_lance", "be_mana_1"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("be_atk_2", "blood_emperor", "branch_a", 4, 230, 255, 55, 1, List.of("be_crimson_eruption"), StatType.ATTACK_DAMAGE, 4.5D, "⚔"));
        register(new SkillNode("be_blood_apocalypse", "blood_emperor", "branch_a", 4, 280, 255, 55, 1, List.of("be_crimson_eruption"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("be_apex_damage", "blood_emperor", "branch_a", 5, 255, 320, 65, 1, List.of("be_atk_2", "be_blood_apocalypse"), StatType.ATTACK_DAMAGE, 6.0D, "★"));

        register(new SkillNode("be_atrspd_1", "blood_emperor", "branch_b", 1, 425, 75, 30, 1, List.of("be_bloodthirst"), StatType.ATTACK_SPEED, 0.04D, "⚔"));
        register(new SkillNode("be_crimson_rush", "blood_emperor", "branch_b", 2, 400, 135, 35, 1, List.of("be_atrspd_1"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("be_spd_1", "blood_emperor", "branch_b", 2, 450, 135, 35, 1, List.of("be_atrspd_1"), StatType.MOVEMENT_SPEED, 0.025D, "⚡"));
        register(new SkillNode("be_vampiric_awakening", "blood_emperor", "branch_b", 3, 425, 195, 45, 1, List.of("be_crimson_rush", "be_spd_1"), NodeType.MAJOR_PASSIVE, "🩸"));
        register(new SkillNode("be_atrspd_2", "blood_emperor", "branch_b", 4, 400, 255, 55, 1, List.of("be_vampiric_awakening"), StatType.ATTACK_SPEED, 0.06D, "⚔"));
        register(new SkillNode("be_berserk_blood", "blood_emperor", "branch_b", 4, 450, 255, 55, 1, List.of("be_vampiric_awakening"), NodeType.MAJOR_ACTIVE, "🔥"));
        register(new SkillNode("be_apex_frenzy", "blood_emperor", "branch_b", 5, 425, 320, 65, 1, List.of("be_atrspd_2", "be_berserk_blood"), StatType.ATTACK_SPEED, 0.08D, "★"));

        register(new SkillNode("be_hp_1", "blood_emperor", "branch_c", 1, 85, 75, 30, 1, List.of("be_bloodthirst"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("be_blood_barrier", "blood_emperor", "branch_c", 2, 60, 135, 35, 1, List.of("be_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("be_armor_1", "blood_emperor", "branch_c", 2, 110, 135, 35, 1, List.of("be_hp_1"), StatType.ARMOR, 3.5D, "🛡"));
        register(new SkillNode("be_undying_vitality", "blood_emperor", "branch_c", 3, 85, 195, 45, 1, List.of("be_blood_barrier", "be_armor_1"), NodeType.MAJOR_PASSIVE, "❤"));
        register(new SkillNode("be_hp_2", "blood_emperor", "branch_c", 4, 60, 255, 55, 1, List.of("be_undying_vitality"), StatType.MAX_HEALTH, 12.0D, "❤"));
        register(new SkillNode("be_dragon_blood_avatar", "blood_emperor", "branch_c", 4, 110, 255, 55, 1, List.of("be_undying_vitality"), NodeType.MAJOR_ACTIVE, "🐉"));
        register(new SkillNode("be_apex_immortal", "blood_emperor", "branch_c", 5, 85, 320, 65, 1, List.of("be_hp_2", "be_dragon_blood_avatar"), StatType.MAX_HEALTH, 16.0D, "★"));

        register(new SkillNode("bm_seismic_roar", "beast_monarch", "root", 0, 255, 25, 30, 0, List.of(), NodeType.ROOT, "🐯"));
        register(new SkillNode("bm_atk_1", "beast_monarch", "branch_a", 1, 255, 75, 30, 1, List.of("bm_seismic_roar"), StatType.ATTACK_DAMAGE, 3.0D, "⚔"));
        register(new SkillNode("bm_predator_leap", "beast_monarch", "branch_a", 2, 230, 135, 35, 1, List.of("bm_atk_1"), NodeType.MAJOR_ACTIVE, "🐾"));
        register(new SkillNode("bm_atrspd_1", "beast_monarch", "branch_a", 2, 280, 135, 35, 1, List.of("bm_atk_1"), StatType.ATTACK_SPEED, 0.04D, "⚔"));
        register(new SkillNode("bm_feral_rend", "beast_monarch", "branch_a", 3, 255, 195, 45, 1, List.of("bm_predator_leap", "bm_atrspd_1"), NodeType.MAJOR_ACTIVE, "🩸"));
        register(new SkillNode("bm_atk_2", "beast_monarch", "branch_a", 4, 230, 255, 55, 1, List.of("bm_feral_rend"), StatType.ATTACK_DAMAGE, 4.5D, "⚔"));
        register(new SkillNode("bm_white_tiger_transformation", "beast_monarch", "branch_a", 4, 280, 255, 55, 1, List.of("bm_feral_rend"), NodeType.MAJOR_ACTIVE, "🐯"));
        register(new SkillNode("bm_apex_claws", "beast_monarch", "branch_a", 5, 255, 320, 65, 1, List.of("bm_atk_2", "bm_white_tiger_transformation"), StatType.ATTACK_DAMAGE, 6.0D, "★"));

        register(new SkillNode("bm_spd_1", "beast_monarch", "branch_b", 1, 425, 75, 30, 1, List.of("bm_seismic_roar"), StatType.MOVEMENT_SPEED, 0.03D, "⚡"));
        register(new SkillNode("bm_scent_tracking", "beast_monarch", "branch_b", 2, 400, 135, 35, 1, List.of("bm_spd_1"), NodeType.MAJOR_ACTIVE, "👃"));
        register(new SkillNode("bm_atrspd_2", "beast_monarch", "branch_b", 2, 450, 135, 35, 1, List.of("bm_spd_1"), StatType.ATTACK_SPEED, 0.05D, "⚔"));
        register(new SkillNode("bm_pack_frenzy", "beast_monarch", "branch_b", 3, 425, 195, 45, 1, List.of("bm_scent_tracking", "bm_atrspd_2"), NodeType.MAJOR_ACTIVE, "🐺"));
        register(new SkillNode("bm_spd_2", "beast_monarch", "branch_b", 4, 400, 255, 55, 1, List.of("bm_pack_frenzy"), StatType.MOVEMENT_SPEED, 0.04D, "⚡"));
        register(new SkillNode("bm_alpha_intimidation", "beast_monarch", "branch_b", 4, 450, 255, 55, 1, List.of("bm_pack_frenzy"), NodeType.MAJOR_PASSIVE, "👑"));
        register(new SkillNode("bm_apex_instinct", "beast_monarch", "branch_b", 5, 425, 320, 65, 1, List.of("bm_spd_2", "bm_alpha_intimidation"), StatType.ATTACK_SPEED, 0.08D, "★"));

        register(new SkillNode("bm_hp_1", "beast_monarch", "branch_c", 1, 85, 75, 30, 1, List.of("bm_seismic_roar"), StatType.MAX_HEALTH, 8.0D, "❤"));
        register(new SkillNode("bm_thick_fur", "beast_monarch", "branch_c", 2, 60, 135, 35, 1, List.of("bm_hp_1"), NodeType.MAJOR_ACTIVE, "🛡"));
        register(new SkillNode("bm_armor_1", "beast_monarch", "branch_c", 2, 110, 135, 35, 1, List.of("bm_hp_1"), StatType.ARMOR, 3.5D, "🛡"));
        register(new SkillNode("bm_wild_resilience", "beast_monarch", "branch_c", 3, 85, 195, 45, 1, List.of("bm_thick_fur", "bm_armor_1"), NodeType.MAJOR_PASSIVE, "🛡"));
        register(new SkillNode("bm_hp_2", "beast_monarch", "branch_c", 4, 60, 255, 55, 1, List.of("bm_wild_resilience"), StatType.MAX_HEALTH, 12.0D, "❤"));
        register(new SkillNode("bm_titan_roar", "beast_monarch", "branch_c", 4, 110, 255, 55, 1, List.of("bm_wild_resilience"), NodeType.MAJOR_ACTIVE, "💥"));
        register(new SkillNode("bm_apex_hide", "beast_monarch", "branch_c", 5, 85, 320, 65, 1, List.of("bm_hp_2", "bm_titan_roar"), StatType.KNOCKBACK_RESISTANCE, 0.35D, "★"));
    }

    public static final int MAX_SKILL_LEVEL = 3;

    public static String normalizeClassId(String hunterClass) {
        if (hunterClass == null) return "";
        return switch (hunterClass.toLowerCase(Locale.ROOT)) {
            case "necromancer" -> "shadow_monarch";
            case "swordsman", "warrior" -> "sword_saint";
            case "mage" -> "grand_archmage";
            case "assassin" -> "void_assassin";
            case "berserker" -> "colossal_berserker";
            case "healer" -> "spiritual_healer";
            case "lightning" -> "lightning_emperor";
            case "blood" -> "blood_emperor";
            case "beast" -> "beast_monarch";
            case "dragon" -> "dragon_monarch";
            default -> hunterClass;
        };
    }

    public static boolean isSkillTreeNode(String skillId) {
        return NODES_BY_SKILL.containsKey(skillId);
    }

    public static List<SkillNode> getNodesForClass(String hunterClass) {
        return CLASS_TREES.getOrDefault(normalizeClassId(hunterClass), Collections.emptyList());
    }

    public static int getPointsInBranch(SystemData data, String hunterClass, String branch) {
        if (data == null || hunterClass == null || branch == null) return 0;
        List<SkillNode> nodes = getNodesForClass(hunterClass);
        int total = 0;
        for (SkillNode node : nodes) {
            if (branch.equalsIgnoreCase(node.branch())) {
                int lvl = data.skills().getOrDefault(node.skillId(), 0);
                total += lvl;
            }
        }
        return total;
    }

    public static SkillNode getNode(String hunterClass, String skillId) {
        if (hunterClass != null) {
            Map<String, SkillNode> map = NODES_BY_CLASS_AND_SKILL.get(normalizeClassId(hunterClass));
            if (map != null && map.containsKey(skillId)) {
                return map.get(skillId);
            }
        }
        return NODES_BY_SKILL.get(skillId);
    }

    public static SkillNode getNode(String skillId) {
        return NODES_BY_SKILL.get(skillId);
    }

    public static int getRequiredLevelForRank(SkillNode node, int targetRank) {
        if (targetRank <= 1) return node.requiredLevel();
        if (targetRank == 2) return node.requiredLevel() + 10;
        return node.requiredLevel() + 20;
    }

    public static int getSpCostForRank(SkillNode node, int targetRank) {
        if (targetRank <= 1) return node.spCost();
        return 1;
    }

    /**
     * Sistema de Niebla de Guerra (Fog of War):
     * Un nodo es visible solo si ya está desbloqueado, si es la raíz, o si alguno de sus prerrequisitos inmediatos ya está desbloqueado.
     * Si no, permanece completamente oculto ("no se deberían de ver").
     */
    public static boolean isRevealed(SystemData data, SkillNode node) {
        if (node == null) return false;
        if (node.isRoot()) return true;
        if (data.skills().containsKey(node.skillId())) return true;
        if (node.prerequisiteIds() == null || node.prerequisiteIds().isEmpty()) return true;
        for (String prereq : node.prerequisiteIds()) {
            if (data.skills().containsKey(prereq)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canUnlock(SystemData data, String skillId) {
        SkillNode node = getNode(data != null ? data.hunterClass() : null, skillId);
        if (node == null) return false;
        if (!node.hunterClass().equals(normalizeClassId(data.hunterClass()))) return false;
        if (data.skills().containsKey(skillId)) return false;
        if (!isRevealed(data, node)) return false;
        if (data.level() < node.requiredLevel()) return false;
        if (data.skillPoints() < node.spCost()) return false;
        return true;
    }

    public static boolean canUpgrade(SystemData data, String skillId) {
        SkillNode node = getNode(data != null ? data.hunterClass() : null, skillId);
        if (node == null) return false;
        if (node.isAttribute()) return false;
        if (!node.hunterClass().equals(normalizeClassId(data.hunterClass()))) return false;
        int curLvl = data.skills().getOrDefault(skillId, 0);
        if (curLvl < 1 || curLvl >= MAX_SKILL_LEVEL) return false;
        int nextRank = curLvl + 1;
        if (data.level() < getRequiredLevelForRank(node, nextRank)) return false;
        if (data.skillPoints() < getSpCostForRank(node, nextRank)) return false;
        return true;
    }

    public static double getTotalAttributeBonus(SystemData data, StatType type) {
        if (data == null || !data.hasHunterClass()) return 0.0D;
        List<SkillNode> nodes = CLASS_TREES.get(normalizeClassId(data.hunterClass()));
        if (nodes == null) return 0.0D;
        double total = 0.0D;
        for (SkillNode node : nodes) {
            if (node.isAttribute() && node.statType() == type && data.skills().containsKey(node.skillId())) {
                total += node.statValue();
            }
        }
        return total;
    }

    public static String getRankStars(int level) {
        return switch (level) {
            case 1 -> "★☆☆";
            case 2 -> "★★☆";
            case 3 -> "★★★";
            default -> "☆☆☆";
        };
    }

    public static Component getRankTitle(int level) {
        return switch (level) {
            case 1 -> Component.translatable("awakening_system.skill_tree.rank_1");
            case 2 -> Component.translatable("awakening_system.skill_tree.rank_2");
            case 3 -> Component.translatable("awakening_system.skill_tree.rank_3");
            default -> Component.translatable("awakening_system.skill_tree.locked");
        };
    }

    public static Component getUpgradeBonusDescription(SkillNode node, int targetRank) {
        if (node.isActive()) {
            if (targetRank == 2) {
                return Component.translatable("awakening_system.skill_tree.upgrade.active_2");
            } else if (targetRank == 3) {
                return Component.translatable("awakening_system.skill_tree.upgrade.active_3");
            }
        } else {
            if (targetRank == 2) {
                return Component.translatable("awakening_system.skill_tree.upgrade.passive_2");
            } else if (targetRank == 3) {
                return Component.translatable("awakening_system.skill_tree.upgrade.passive_3");
            }
        }
        return Component.empty();
    }
}
