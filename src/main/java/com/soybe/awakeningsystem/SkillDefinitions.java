package com.soybe.awakeningsystem;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import java.util.List;

public final class SkillDefinitions {
    public static final List<String> ALL_BASE_SKILLS = List.of(
            // Original skills
            "stone_strike", "combat_instinct", "enduring_flesh", "windrunner", "deep_breath",
            "ore_sense", "fire_ward", "green_thumb", "artisan_hand", "alchemist_eye",
            "shadow_step", "magnetic_pull", "guardian_stance", "deadeye",
            "dimensional_blink", "seismic_roar", "blaze_ignition", "spectral_shift",
            "piercing_shot", "web_snare", "kinetic_blast", "tidal_surge",
            "shadow_monarch", "phoenix_rebirth", "bloodthirst",

            // New Manhwa skills
            "rulers_authority", "asrahan_mana_blade", "electrification", "nano_blade_overdrive",
            "dragon_dive", "black_fire_wave", "caregiver_blessing", "fourth_wall",
            "estate_workmanship", "nano_healing", "blue_dragon_scales", "dragon_sense", "iron_will",
            "death_king_rewind",

            // Mod integration skills
            "system_core", "beast_bond", "cataclysm_breaker", "twilight_attunement",
            "aether_ascension", "deep_dark_resonance", "adventurer_instinct",
            "eclipse_edge", "eclipse_core",

            // Habilidades de Rango Único (1 Jugador por Mundo/Server)
            "unique_absolute_extraction", "unique_rulers_authority_prime",
            "unique_fourth_wall_infinite", "unique_dragon_breath_annihilation",

            // Habilidades de Series Isekai (Multiverso 2.0)
            "isekai_predator", "isekai_dimensional_slash", "isekai_dragon_roar", "isekai_mana_burst",

            // Habilidades Shonen Legendarias (Multiverso Anime 3.0)
            "shonen_infinite_void", "shonen_getsuga_tensho", "shonen_conquerors_haki", "shonen_zoltraak", "shonen_rasenshuriken"
    );

    private SkillDefinitions() {}

    public enum Rarity {
        COMMON("common"), UNCOMMON("uncommon"), RARE("rare"), EPIC("epic"), LEGENDARY("legendary"), MYTHIC("mythic"), UNIQUE("unique");

        private final String id;

        Rarity(String id) { this.id = id; }
        public String id() { return id; }
    }

    public record Definition(String nameKey, String descriptionKey, String effectKey, Rarity rarity, String requiredMod) {
        public Definition(String nameKey, String descriptionKey, String effectKey) {
            this(nameKey, descriptionKey, effectKey, rarityFor(nameKey), "");
        }
        public Definition(String nameKey, String descriptionKey, String effectKey, Rarity rarity, String requiredMod) {
            this.nameKey = nameKey;
            this.descriptionKey = descriptionKey;
            this.effectKey = effectKey;
            this.rarity = rarity;
            this.requiredMod = requiredMod;
        }
        private static Rarity rarityFor(String nameKey) {
            if (nameKey.contains("unique_") || nameKey.contains("isekai_dimensional_slash") || nameKey.contains("isekai_mana_burst") || nameKey.contains("shonen_infinite_void")) return Rarity.MYTHIC;
            if (nameKey.contains("isekai_predator") || nameKey.contains("isekai_dragon_roar") || nameKey.contains("shonen_getsuga_tensho") || nameKey.contains("shonen_conquerors_haki") || nameKey.contains("shonen_zoltraak") || nameKey.contains("shonen_rasenshuriken")) return Rarity.LEGENDARY;
            if (nameKey.contains("unique_")) return Rarity.UNIQUE;
            if (nameKey.contains("shadow_monarch") || nameKey.contains("phoenix_rebirth") || nameKey.contains("rulers_authority") || nameKey.contains("fourth_wall") || nameKey.contains("death_king_rewind")) return Rarity.MYTHIC;
            if (nameKey.contains("bloodthirst") || nameKey.contains("kinetic_blast") || nameKey.contains("tidal_surge") || nameKey.contains("electrification") || nameKey.contains("nano_blade_overdrive") || nameKey.contains("dragon_dive")) return Rarity.LEGENDARY;
            if (nameKey.contains("fire_ward") || nameKey.contains("alchemist_eye") || nameKey.contains("guardian_stance") || nameKey.contains("deadeye") || nameKey.contains("shadow_step") || nameKey.contains("asrahan_mana_blade") || nameKey.contains("black_fire_wave") || nameKey.contains("caregiver_blessing") || nameKey.contains("blue_dragon_scales") || nameKey.contains("iron_will")) return Rarity.EPIC;
            if (nameKey.contains("piercing_shot") || nameKey.contains("web_snare") || nameKey.contains("enduring_flesh") || nameKey.contains("windrunner") || nameKey.contains("ore_sense") || nameKey.contains("artisan_hand") || nameKey.contains("magnetic_pull") || nameKey.contains("estate_workmanship") || nameKey.contains("nano_healing") || nameKey.contains("dragon_sense")) return Rarity.RARE;
            if (nameKey.contains("combat_instinct") || nameKey.contains("deep_breath") || nameKey.contains("green_thumb")) return Rarity.UNCOMMON;
            return Rarity.COMMON;
        }
        public Component name() { return Component.translatable(nameKey); }
        public Component description() { return Component.translatable(descriptionKey); }
        public Component effect() { return Component.translatable(effectKey); }
    }

    public static String affinity(String skillId) {
        if (skillId.endsWith("_evolution")) skillId = skillId.substring(0, skillId.length() - 10);
        return switch (skillId) {
            case "stone_strike", "combat_instinct", "seismic_roar" -> "combat";
            case "enduring_flesh", "deep_breath", "fire_ward" -> "vitality";
            case "windrunner", "shadow_step", "dimensional_blink" -> "agility";
            case "ore_sense", "artisan_hand" -> "mining";
            case "green_thumb", "alchemist_eye" -> "gathering";
            case "magnetic_pull", "deadeye", "guardian_stance" -> "support";
            case "blaze_ignition", "bloodthirst" -> "combat";
            case "spectral_shift", "phoenix_rebirth" -> "vitality";
            case "piercing_shot", "web_snare", "kinetic_blast", "tidal_surge" -> "combat";
            case "shadow_monarch" -> "combat";
            case "rulers_authority", "asrahan_mana_blade", "electrification", "nano_blade_overdrive" -> "combat";
            case "dragon_dive", "black_fire_wave" -> "combat";
            case "isekai_predator", "isekai_dimensional_slash", "isekai_dragon_roar", "isekai_mana_burst" -> "combat";
            case "shonen_infinite_void", "shonen_getsuga_tensho", "shonen_conquerors_haki", "shonen_zoltraak", "shonen_rasenshuriken" -> "combat";
            case "caregiver_blessing", "fourth_wall", "estate_workmanship", "nano_healing" -> "support";
            case "blue_dragon_scales", "dragon_sense", "iron_will" -> "vitality";
            case "death_king_rewind" -> "vitality";
            case "system_core", "beast_bond", "cataclysm_breaker", "twilight_attunement",
                    "aether_ascension", "deep_dark_resonance", "adventurer_instinct",
                    "eclipse_edge", "eclipse_core" -> "integration";
            default -> "combat";
        };
    }

    public static String categoryFor(String baseSkillId) {
        return switch (baseSkillId) {
            case "stone_strike" -> "mining";
            case "combat_instinct" -> "combat";
            case "enduring_flesh" -> "survival";
            case "windrunner" -> "mobility";
            case "deep_breath" -> "survival";
            case "ore_sense" -> "mining";
            case "fire_ward" -> "survival";
            case "green_thumb" -> "nature";
            case "artisan_hand" -> "mining";
            case "alchemist_eye" -> "nature";
            case "shadow_step" -> "mobility";
            case "magnetic_pull" -> "utility";
            case "guardian_stance" -> "combat";
            case "deadeye" -> "ranged";
            case "dimensional_blink" -> "mobility";
            case "seismic_roar" -> "combat";
            case "blaze_ignition" -> "fire";
            case "spectral_shift" -> "survival";
            case "piercing_shot" -> "ranged";
            case "web_snare" -> "nature";
            case "kinetic_blast" -> "combat";
            case "tidal_surge" -> "depth";
            case "shadow_monarch", "bloodthirst" -> "combat";
            case "phoenix_rebirth", "death_king_rewind" -> "survival";
            case "eclipse_edge", "eclipse_core" -> "eclipse";
            case "rulers_authority" -> "system";
            case "asrahan_mana_blade" -> "combat";
            case "electrification" -> "combat";
            case "nano_blade_overdrive" -> "combat";
            case "dragon_dive" -> "mobility";
            case "black_fire_wave" -> "fire";
            case "caregiver_blessing" -> "survival";
            case "fourth_wall" -> "survival";
            case "estate_workmanship" -> "mining";
            case "nano_healing" -> "survival";
            case "blue_dragon_scales" -> "protection";
            case "dragon_sense" -> "perception";
            case "iron_will" -> "survival";
            case "unique_absolute_extraction" -> "combat";
            case "unique_rulers_authority_prime" -> "system";
            case "unique_fourth_wall_infinite" -> "survival";
            case "unique_dragon_breath_annihilation" -> "combat";
            case "isekai_predator", "isekai_dimensional_slash", "isekai_dragon_roar", "isekai_mana_burst" -> "combat";
            case "shonen_infinite_void", "shonen_getsuga_tensho", "shonen_conquerors_haki", "shonen_zoltraak", "shonen_rasenshuriken" -> "combat";
            default -> null;
        };
    }

    public static boolean isActiveSkill(String skillId) {
        return isActive(skillId);
    }

    public static boolean isPassiveSkill(String skillId) {
        return isPassive(skillId);
    }

    public static Definition get(String skillId) {
        String baseSkillId = skillId.endsWith("_evolution") ? skillId.substring(0, skillId.length() - 10) : skillId;
        return switch (baseSkillId) {
            case "stone_strike" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.stone_strike.name" : "awakening_system.skill.stone_strike_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.stone_strike.description" : "awakening_system.skill.stone_strike_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.stone_strike.effect" : "awakening_system.skill.stone_strike_evolution.effect");
            case "combat_instinct" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.combat_instinct.name" : "awakening_system.skill.combat_instinct_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.combat_instinct.description" : "awakening_system.skill.combat_instinct_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.combat_instinct.effect" : "awakening_system.skill.combat_instinct_evolution.effect");
            case "enduring_flesh" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.enduring_flesh.name" : "awakening_system.skill.enduring_flesh_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.enduring_flesh.description" : "awakening_system.skill.enduring_flesh_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.enduring_flesh.effect" : "awakening_system.skill.enduring_flesh_evolution.effect");
            case "windrunner" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.windrunner.name" : "awakening_system.skill.windrunner_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.windrunner.description" : "awakening_system.skill.windrunner_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.windrunner.effect" : "awakening_system.skill.windrunner_evolution.effect");
            case "deep_breath" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.deep_breath.name" : "awakening_system.skill.deep_breath_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.deep_breath.description" : "awakening_system.skill.deep_breath_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.deep_breath.effect" : "awakening_system.skill.deep_breath_evolution.effect");
            case "ore_sense" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.ore_sense.name" : "awakening_system.skill.ore_sense_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.ore_sense.description" : "awakening_system.skill.ore_sense_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.ore_sense.effect" : "awakening_system.skill.ore_sense_evolution.effect");
            case "fire_ward" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.fire_ward.name" : "awakening_system.skill.fire_ward_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.fire_ward.description" : "awakening_system.skill.fire_ward_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.fire_ward.effect" : "awakening_system.skill.fire_ward_evolution.effect");
            case "green_thumb" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.green_thumb.name" : "awakening_system.skill.green_thumb_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.green_thumb.description" : "awakening_system.skill.green_thumb_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.green_thumb.effect" : "awakening_system.skill.green_thumb_evolution.effect");
            case "artisan_hand" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.artisan_hand.name" : "awakening_system.skill.artisan_hand_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.artisan_hand.description" : "awakening_system.skill.artisan_hand_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.artisan_hand.effect" : "awakening_system.skill.artisan_hand_evolution.effect");
            case "alchemist_eye" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.alchemist_eye.name" : "awakening_system.skill.alchemist_eye_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.alchemist_eye.description" : "awakening_system.skill.alchemist_eye_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.alchemist_eye.effect" : "awakening_system.skill.alchemist_eye_evolution.effect");
            case "shadow_step" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.shadow_step.name" : "awakening_system.skill.shadow_step_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.shadow_step.description" : "awakening_system.skill.shadow_step_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.shadow_step.effect" : "awakening_system.skill.shadow_step_evolution.effect", Rarity.EPIC, "");
            case "magnetic_pull" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.magnetic_pull.name" : "awakening_system.skill.magnetic_pull_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.magnetic_pull.description" : "awakening_system.skill.magnetic_pull_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.magnetic_pull.effect" : "awakening_system.skill.magnetic_pull_evolution.effect", Rarity.RARE, "");
            case "guardian_stance" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.guardian_stance.name" : "awakening_system.skill.guardian_stance_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.guardian_stance.description" : "awakening_system.skill.guardian_stance_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.guardian_stance.effect" : "awakening_system.skill.guardian_stance_evolution.effect");
            case "deadeye" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.deadeye.name" : "awakening_system.skill.deadeye_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.deadeye.description" : "awakening_system.skill.deadeye_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.deadeye.effect" : "awakening_system.skill.deadeye_evolution.effect");
            case "dimensional_blink" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.dimensional_blink.name" : "awakening_system.skill.dimensional_blink_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.dimensional_blink.description" : "awakening_system.skill.dimensional_blink_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.dimensional_blink.effect" : "awakening_system.skill.dimensional_blink_evolution.effect",
                    Rarity.EPIC, "");
            case "seismic_roar" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.seismic_roar.name" : "awakening_system.skill.seismic_roar_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.seismic_roar.description" : "awakening_system.skill.seismic_roar_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.seismic_roar.effect" : "awakening_system.skill.seismic_roar_evolution.effect",
                    Rarity.LEGENDARY, "");
            case "blaze_ignition" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.blaze_ignition.name" : "awakening_system.skill.blaze_ignition_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.blaze_ignition.description" : "awakening_system.skill.blaze_ignition_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.blaze_ignition.effect" : "awakening_system.skill.blaze_ignition_evolution.effect",
                    Rarity.RARE, "");
            case "spectral_shift" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.spectral_shift.name" : "awakening_system.skill.spectral_shift_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.spectral_shift.description" : "awakening_system.skill.spectral_shift_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.spectral_shift.effect" : "awakening_system.skill.spectral_shift_evolution.effect",
                    Rarity.EPIC, "");
            case "piercing_shot" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.piercing_shot.name" : "awakening_system.skill.piercing_shot_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.piercing_shot.description" : "awakening_system.skill.piercing_shot_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.piercing_shot.effect" : "awakening_system.skill.piercing_shot_evolution.effect",
                    Rarity.RARE, "");
            case "web_snare" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.web_snare.name" : "awakening_system.skill.web_snare_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.web_snare.description" : "awakening_system.skill.web_snare_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.web_snare.effect" : "awakening_system.skill.web_snare_evolution.effect",
                    Rarity.RARE, "");
            case "kinetic_blast" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.kinetic_blast.name" : "awakening_system.skill.kinetic_blast_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.kinetic_blast.description" : "awakening_system.skill.kinetic_blast_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.kinetic_blast.effect" : "awakening_system.skill.kinetic_blast_evolution.effect",
                    Rarity.LEGENDARY, "");
            case "tidal_surge" -> new Definition(
                    skillId.equals(baseSkillId) ? "awakening_system.skill.tidal_surge.name" : "awakening_system.skill.tidal_surge_evolution.name",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.tidal_surge.description" : "awakening_system.skill.tidal_surge_evolution.description",
                    skillId.equals(baseSkillId) ? "awakening_system.skill.tidal_surge.effect" : "awakening_system.skill.tidal_surge_evolution.effect",
                    Rarity.LEGENDARY, "");
            case "shadow_monarch" -> new Definition(
                    "awakening_system.skill.shadow_monarch.name",
                    "awakening_system.skill.shadow_monarch.description",
                    "awakening_system.skill.shadow_monarch.effect",
                    Rarity.MYTHIC, "");
            case "phoenix_rebirth" -> new Definition(
                    "awakening_system.skill.phoenix_rebirth.name",
                    "awakening_system.skill.phoenix_rebirth.description",
                    "awakening_system.skill.phoenix_rebirth.effect",
                    Rarity.MYTHIC, "");
            case "death_king_rewind" -> new Definition(
                    "awakening_system.skill.death_king_rewind.name",
                    "awakening_system.skill.death_king_rewind.description",
                    "awakening_system.skill.death_king_rewind.effect",
                    Rarity.MYTHIC, "");
            case "bloodthirst" -> new Definition(
                    "awakening_system.skill.bloodthirst.name",
                    "awakening_system.skill.bloodthirst.description",
                    "awakening_system.skill.bloodthirst.effect",
                    Rarity.LEGENDARY, "");
            case "rulers_authority" -> new Definition(
                    "awakening_system.skill.rulers_authority.name",
                    "awakening_system.skill.rulers_authority.description",
                    "awakening_system.skill.rulers_authority.effect",
                    Rarity.MYTHIC, "");
            case "asrahan_mana_blade" -> new Definition(
                    "awakening_system.skill.asrahan_mana_blade.name",
                    "awakening_system.skill.asrahan_mana_blade.description",
                    "awakening_system.skill.asrahan_mana_blade.effect",
                    Rarity.EPIC, "");
            case "electrification" -> new Definition(
                    "awakening_system.skill.electrification.name",
                    "awakening_system.skill.electrification.description",
                    "awakening_system.skill.electrification.effect",
                    Rarity.LEGENDARY, "");
            case "nano_blade_overdrive" -> new Definition(
                    "awakening_system.skill.nano_blade_overdrive.name",
                    "awakening_system.skill.nano_blade_overdrive.description",
                    "awakening_system.skill.nano_blade_overdrive.effect",
                    Rarity.LEGENDARY, "");
            case "dragon_dive" -> new Definition(
                    "awakening_system.skill.dragon_dive.name",
                    "awakening_system.skill.dragon_dive.description",
                    "awakening_system.skill.dragon_dive.effect",
                    Rarity.LEGENDARY, "");
            case "black_fire_wave" -> new Definition(
                    "awakening_system.skill.black_fire_wave.name",
                    "awakening_system.skill.black_fire_wave.description",
                    "awakening_system.skill.black_fire_wave.effect",
                    Rarity.EPIC, "");
            case "caregiver_blessing" -> new Definition(
                    "awakening_system.skill.caregiver_blessing.name",
                    "awakening_system.skill.caregiver_blessing.description",
                    "awakening_system.skill.caregiver_blessing.effect",
                    Rarity.EPIC, "");
            case "fourth_wall" -> new Definition(
                    "awakening_system.skill.fourth_wall.name",
                    "awakening_system.skill.fourth_wall.description",
                    "awakening_system.skill.fourth_wall.effect",
                    Rarity.MYTHIC, "");
            case "estate_workmanship" -> new Definition(
                    "awakening_system.skill.estate_workmanship.name",
                    "awakening_system.skill.estate_workmanship.description",
                    "awakening_system.skill.estate_workmanship.effect",
                    Rarity.RARE, "");
            case "nano_healing" -> new Definition(
                    "awakening_system.skill.nano_healing.name",
                    "awakening_system.skill.nano_healing.description",
                    "awakening_system.skill.nano_healing.effect",
                    Rarity.RARE, "");
            case "blue_dragon_scales" -> new Definition(
                    "awakening_system.skill.blue_dragon_scales.name",
                    "awakening_system.skill.blue_dragon_scales.description",
                    "awakening_system.skill.blue_dragon_scales.effect",
                    Rarity.EPIC, "");
            case "dragon_sense" -> new Definition(
                    "awakening_system.skill.dragon_sense.name",
                    "awakening_system.skill.dragon_sense.description",
                    "awakening_system.skill.dragon_sense.effect",
                    Rarity.RARE, "");
            case "iron_will" -> new Definition(
                    "awakening_system.skill.iron_will.name",
                    "awakening_system.skill.iron_will.description",
                    "awakening_system.skill.iron_will.effect",
                    Rarity.EPIC, "");
            case "unique_absolute_extraction" -> new Definition(
                    "awakening_system.skill.unique_absolute_extraction.name",
                    "awakening_system.skill.unique_absolute_extraction.description",
                    "awakening_system.skill.unique_absolute_extraction.effect",
                    Rarity.UNIQUE, "");
            case "unique_rulers_authority_prime" -> new Definition(
                    "awakening_system.skill.unique_rulers_authority_prime.name",
                    "awakening_system.skill.unique_rulers_authority_prime.description",
                    "awakening_system.skill.unique_rulers_authority_prime.effect",
                    Rarity.UNIQUE, "");
            case "unique_fourth_wall_infinite" -> new Definition(
                    "awakening_system.skill.unique_fourth_wall_infinite.name",
                    "awakening_system.skill.unique_fourth_wall_infinite.description",
                    "awakening_system.skill.unique_fourth_wall_infinite.effect",
                    Rarity.UNIQUE, "");
            case "unique_dragon_breath_annihilation" -> new Definition(
                    "awakening_system.skill.unique_dragon_breath_annihilation.name",
                    "awakening_system.skill.unique_dragon_breath_annihilation.description",
                    "awakening_system.skill.unique_dragon_breath_annihilation.effect",
                    Rarity.UNIQUE, "");
            case "isekai_predator" -> new Definition(
                    "awakening_system.skill.isekai_predator.name",
                    "awakening_system.skill.isekai_predator.description",
                    "awakening_system.skill.isekai_predator.effect",
                    Rarity.LEGENDARY, "");
            case "isekai_dimensional_slash" -> new Definition(
                    "awakening_system.skill.isekai_dimensional_slash.name",
                    "awakening_system.skill.isekai_dimensional_slash.description",
                    "awakening_system.skill.isekai_dimensional_slash.effect",
                    Rarity.MYTHIC, "");
            case "isekai_dragon_roar" -> new Definition(
                    "awakening_system.skill.isekai_dragon_roar.name",
                    "awakening_system.skill.isekai_dragon_roar.description",
                    "awakening_system.skill.isekai_dragon_roar.effect",
                    Rarity.LEGENDARY, "");
            case "isekai_mana_burst" -> new Definition(
                    "awakening_system.skill.isekai_mana_burst.name",
                    "awakening_system.skill.isekai_mana_burst.description",
                    "awakening_system.skill.isekai_mana_burst.effect",
                    Rarity.MYTHIC, "");

            // --- Habilidades Shonen Legendarias (Multiverso Anime 3.0) ---
            case "shonen_infinite_void" -> new Definition(
                    "awakening_system.skill.shonen_infinite_void.name",
                    "awakening_system.skill.shonen_infinite_void.description",
                    "awakening_system.skill.shonen_infinite_void.effect",
                    Rarity.MYTHIC, "");
            case "shonen_getsuga_tensho" -> new Definition(
                    "awakening_system.skill.shonen_getsuga_tensho.name",
                    "awakening_system.skill.shonen_getsuga_tensho.description",
                    "awakening_system.skill.shonen_getsuga_tensho.effect",
                    Rarity.LEGENDARY, "");
            case "shonen_conquerors_haki" -> new Definition(
                    "awakening_system.skill.shonen_conquerors_haki.name",
                    "awakening_system.skill.shonen_conquerors_haki.description",
                    "awakening_system.skill.shonen_conquerors_haki.effect",
                    Rarity.LEGENDARY, "");
            case "shonen_zoltraak" -> new Definition(
                    "awakening_system.skill.shonen_zoltraak.name",
                    "awakening_system.skill.shonen_zoltraak.description",
                    "awakening_system.skill.shonen_zoltraak.effect",
                    Rarity.LEGENDARY, "");
            case "shonen_rasenshuriken" -> new Definition(
                    "awakening_system.skill.shonen_rasenshuriken.name",
                    "awakening_system.skill.shonen_rasenshuriken.description",
                    "awakening_system.skill.shonen_rasenshuriken.effect",
                    Rarity.LEGENDARY, "");

            // --- Árbol de Habilidades de Clase (8 Clases de Cazador) ---
            // 1. Monarca de las Sombras
            case "shadow_exchange" -> new Definition("awakening_system.skill.shadow_exchange.name", "awakening_system.skill.shadow_exchange.description", "awakening_system.skill.shadow_exchange.effect", Rarity.EPIC, "");
            case "monarchs_domain" -> new Definition("awakening_system.skill.monarchs_domain.name", "awakening_system.skill.monarchs_domain.description", "awakening_system.skill.monarchs_domain.effect", Rarity.LEGENDARY, "");
            case "shadow_colossus_infusion" -> new Definition("awakening_system.skill.shadow_colossus_infusion.name", "awakening_system.skill.shadow_colossus_infusion.description", "awakening_system.skill.shadow_colossus_infusion.effect", Rarity.MYTHIC, "");
            case "stealth_stalker" -> new Definition("awakening_system.skill.stealth_stalker.name", "awakening_system.skill.stealth_stalker.description", "awakening_system.skill.stealth_stalker.effect", Rarity.EPIC, "");
            case "bloodlust_gaze" -> new Definition("awakening_system.skill.bloodlust_gaze.name", "awakening_system.skill.bloodlust_gaze.description", "awakening_system.skill.bloodlust_gaze.effect", Rarity.LEGENDARY, "");
            case "black_heart_resurrection" -> new Definition("awakening_system.skill.black_heart_resurrection.name", "awakening_system.skill.black_heart_resurrection.description", "awakening_system.skill.black_heart_resurrection.effect", Rarity.MYTHIC, "");
            case "phantom_ambush" -> new Definition("awakening_system.skill.phantom_ambush.name", "awakening_system.skill.phantom_ambush.description", "awakening_system.skill.phantom_ambush.effect", Rarity.EPIC, "");
            case "death_mark_execution" -> new Definition("awakening_system.skill.death_mark_execution.name", "awakening_system.skill.death_mark_execution.description", "awakening_system.skill.death_mark_execution.effect", Rarity.EPIC, "");

            // 2. Santo de la Espada de Hua
            case "plum_blossom_rain" -> new Definition("awakening_system.skill.plum_blossom_rain.name", "awakening_system.skill.plum_blossom_rain.description", "awakening_system.skill.plum_blossom_rain.effect", Rarity.EPIC, "");
            case "twenty_four_sword_forms" -> new Definition("awakening_system.skill.twenty_four_sword_forms.name", "awakening_system.skill.twenty_four_sword_forms.description", "awakening_system.skill.twenty_four_sword_forms.effect", Rarity.LEGENDARY, "");
            case "plum_blossom_domain" -> new Definition("awakening_system.skill.plum_blossom_domain.name", "awakening_system.skill.plum_blossom_domain.description", "awakening_system.skill.plum_blossom_domain.effect", Rarity.MYTHIC, "");
            case "sword_intent" -> new Definition("awakening_system.skill.sword_intent.name", "awakening_system.skill.sword_intent.description", "awakening_system.skill.sword_intent.effect", Rarity.EPIC, "");
            case "daoist_clarity" -> new Definition("awakening_system.skill.daoist_clarity.name", "awakening_system.skill.daoist_clarity.description", "awakening_system.skill.daoist_clarity.effect", Rarity.LEGENDARY, "");
            case "sword_heart_awakening" -> new Definition("awakening_system.skill.sword_heart_awakening.name", "awakening_system.skill.sword_heart_awakening.description", "awakening_system.skill.sword_heart_awakening.effect", Rarity.EPIC, "");
            case "limitless_sword_intent" -> new Definition("awakening_system.skill.limitless_sword_intent.name", "awakening_system.skill.limitless_sword_intent.description", "awakening_system.skill.limitless_sword_intent.effect", Rarity.LEGENDARY, "");
            case "plum_blossom_sword_god" -> new Definition("awakening_system.skill.plum_blossom_sword_god.name", "awakening_system.skill.plum_blossom_sword_god.description", "awakening_system.skill.plum_blossom_sword_god.effect", Rarity.MYTHIC, "");

            // 3. Apóstol del Star Stream
            case "readers_viewpoint" -> new Definition("awakening_system.skill.readers_viewpoint.name", "awakening_system.skill.readers_viewpoint.description", "awakening_system.skill.readers_viewpoint.effect", Rarity.EPIC, "");
            case "constellation_favor" -> new Definition("awakening_system.skill.constellation_favor.name", "awakening_system.skill.constellation_favor.description", "awakening_system.skill.constellation_favor.effect", Rarity.LEGENDARY, "");
            case "fourth_wall_shatter" -> new Definition("awakening_system.skill.fourth_wall_shatter.name", "awakening_system.skill.fourth_wall_shatter.description", "awakening_system.skill.fourth_wall_shatter.effect", Rarity.MYTHIC, "");
            case "bookmark_skill" -> new Definition("awakening_system.skill.bookmark_skill.name", "awakening_system.skill.bookmark_skill.description", "awakening_system.skill.bookmark_skill.effect", Rarity.EPIC, "");
            case "bookmark_mastery" -> new Definition("awakening_system.skill.bookmark_mastery.name", "awakening_system.skill.bookmark_mastery.description", "awakening_system.skill.bookmark_mastery.effect", Rarity.EPIC, "");
            case "probability_distortion" -> new Definition("awakening_system.skill.probability_distortion.name", "awakening_system.skill.probability_distortion.description", "awakening_system.skill.probability_distortion.effect", Rarity.LEGENDARY, "");
            case "character_summary_scan" -> new Definition("awakening_system.skill.character_summary_scan.name", "awakening_system.skill.character_summary_scan.description", "awakening_system.skill.character_summary_scan.effect", Rarity.LEGENDARY, "");
            case "star_stream_climax" -> new Definition("awakening_system.skill.star_stream_climax.name", "awakening_system.skill.star_stream_climax.description", "awakening_system.skill.star_stream_climax.effect", Rarity.MYTHIC, "");

            // 4. Berserker Colosal
            case "aura_smash" -> new Definition("awakening_system.skill.aura_smash.name", "awakening_system.skill.aura_smash.description", "awakening_system.skill.aura_smash.effect", Rarity.EPIC, "");
            case "cross_impact" -> new Definition("awakening_system.skill.cross_impact.name", "awakening_system.skill.cross_impact.description", "awakening_system.skill.cross_impact.effect", Rarity.LEGENDARY, "");
            case "valterian_gigantomachia" -> new Definition("awakening_system.skill.valterian_gigantomachia.name", "awakening_system.skill.valterian_gigantomachia.description", "awakening_system.skill.valterian_gigantomachia.effect", Rarity.MYTHIC, "");
            case "pain_suppression" -> new Definition("awakening_system.skill.pain_suppression.name", "awakening_system.skill.pain_suppression.description", "awakening_system.skill.pain_suppression.effect", Rarity.EPIC, "");
            case "iron_muscle_hardening" -> new Definition("awakening_system.skill.iron_muscle_hardening.name", "awakening_system.skill.iron_muscle_hardening.description", "awakening_system.skill.iron_muscle_hardening.effect", Rarity.LEGENDARY, "");
            case "blood_frenzy_passive" -> new Definition("awakening_system.skill.blood_frenzy_passive.name", "awakening_system.skill.blood_frenzy_passive.description", "awakening_system.skill.blood_frenzy_passive.effect", Rarity.EPIC, "");
            case "immortal_berserk" -> new Definition("awakening_system.skill.immortal_berserk.name", "awakening_system.skill.immortal_berserk.description", "awakening_system.skill.immortal_berserk.effect", Rarity.MYTHIC, "");

            // 5. Demonio Celestial
            case "heavenly_demon_steps" -> new Definition("awakening_system.skill.heavenly_demon_steps.name", "awakening_system.skill.heavenly_demon_steps.description", "awakening_system.skill.heavenly_demon_steps.effect", Rarity.EPIC, "");
            case "demonic_blood_slash" -> new Definition("awakening_system.skill.demonic_blood_slash.name", "awakening_system.skill.demonic_blood_slash.description", "awakening_system.skill.demonic_blood_slash.effect", Rarity.LEGENDARY, "");
            case "sky_demon_nine_reversals" -> new Definition("awakening_system.skill.sky_demon_nine_reversals.name", "awakening_system.skill.sky_demon_nine_reversals.description", "awakening_system.skill.sky_demon_nine_reversals.effect", Rarity.MYTHIC, "");
            case "nano_scan_weakness" -> new Definition("awakening_system.skill.nano_scan_weakness.name", "awakening_system.skill.nano_scan_weakness.description", "awakening_system.skill.nano_scan_weakness.effect", Rarity.EPIC, "");
            case "nano_cellular_regeneration" -> new Definition("awakening_system.skill.nano_cellular_regeneration.name", "awakening_system.skill.nano_cellular_regeneration.description", "awakening_system.skill.nano_cellular_regeneration.effect", Rarity.LEGENDARY, "");
            case "asura_bloodline" -> new Definition("awakening_system.skill.asura_bloodline.name", "awakening_system.skill.asura_bloodline.description", "awakening_system.skill.asura_bloodline.effect", Rarity.LEGENDARY, "");
            case "nano_overdrive_suit" -> new Definition("awakening_system.skill.nano_overdrive_suit.name", "awakening_system.skill.nano_overdrive_suit.description", "awakening_system.skill.nano_overdrive_suit.effect", Rarity.MYTHIC, "");

            // 6. Asesino del Vacío
            case "shadow_step_blink" -> new Definition("awakening_system.skill.shadow_step_blink.name", "awakening_system.skill.shadow_step_blink.description", "awakening_system.skill.shadow_step_blink.effect", Rarity.EPIC, "");
            case "vital_strike" -> new Definition("awakening_system.skill.vital_strike.name", "awakening_system.skill.vital_strike.description", "awakening_system.skill.vital_strike.effect", Rarity.LEGENDARY, "");
            case "infinite_mirror_blades" -> new Definition("awakening_system.skill.infinite_mirror_blades.name", "awakening_system.skill.infinite_mirror_blades.description", "awakening_system.skill.infinite_mirror_blades.effect", Rarity.MYTHIC, "");
            case "predator_sense" -> new Definition("awakening_system.skill.predator_sense.name", "awakening_system.skill.predator_sense.description", "awakening_system.skill.predator_sense.effect", Rarity.EPIC, "");
            case "smoke_bomb_escape" -> new Definition("awakening_system.skill.smoke_bomb_escape.name", "awakening_system.skill.smoke_bomb_escape.description", "awakening_system.skill.smoke_bomb_escape.effect", Rarity.LEGENDARY, "");
            case "max_level_privilege" -> new Definition("awakening_system.skill.max_level_privilege.name", "awakening_system.skill.max_level_privilege.description", "awakening_system.skill.max_level_privilege.effect", Rarity.MYTHIC, "");

            // 7. Gran Archimago
            case "elemental_spear" -> new Definition("awakening_system.skill.elemental_spear.name", "awakening_system.skill.elemental_spear.description", "awakening_system.skill.elemental_spear.effect", Rarity.EPIC, "");
            case "gravity_crush" -> new Definition("awakening_system.skill.gravity_crush.name", "awakening_system.skill.gravity_crush.description", "awakening_system.skill.gravity_crush.effect", Rarity.LEGENDARY, "");
            case "supernova_incineration" -> new Definition("awakening_system.skill.supernova_incineration.name", "awakening_system.skill.supernova_incineration.description", "awakening_system.skill.supernova_incineration.effect", Rarity.MYTHIC, "");
            case "mana_shield_barrier" -> new Definition("awakening_system.skill.mana_shield_barrier.name", "awakening_system.skill.mana_shield_barrier.description", "awakening_system.skill.mana_shield_barrier.effect", Rarity.EPIC, "");
            case "arcane_overload" -> new Definition("awakening_system.skill.arcane_overload.name", "awakening_system.skill.arcane_overload.description", "awakening_system.skill.arcane_overload.effect", Rarity.LEGENDARY, "");
            case "time_dilation" -> new Definition("awakening_system.skill.time_dilation.name", "awakening_system.skill.time_dilation.description", "awakening_system.skill.time_dilation.effect", Rarity.MYTHIC, "");

            // 8. Soberano Dragón
            case "dragon_breath" -> new Definition("awakening_system.skill.dragon_breath.name", "awakening_system.skill.dragon_breath.description", "awakening_system.skill.dragon_breath.effect", Rarity.EPIC, "");
            case "meteor_fall" -> new Definition("awakening_system.skill.meteor_fall.name", "awakening_system.skill.meteor_fall.description", "awakening_system.skill.meteor_fall.effect", Rarity.LEGENDARY, "");
            case "breath_of_annihilation" -> new Definition("awakening_system.skill.breath_of_annihilation.name", "awakening_system.skill.breath_of_annihilation.description", "awakening_system.skill.breath_of_annihilation.effect", Rarity.MYTHIC, "");
            case "dragon_scales_hardening" -> new Definition("awakening_system.skill.dragon_scales_hardening.name", "awakening_system.skill.dragon_scales_hardening.description", "awakening_system.skill.dragon_scales_hardening.effect", Rarity.EPIC, "");
            case "dragon_wings_flight" -> new Definition("awakening_system.skill.dragon_wings_flight.name", "awakening_system.skill.dragon_wings_flight.description", "awakening_system.skill.dragon_wings_flight.effect", Rarity.LEGENDARY, "");
            case "dragon_form_avatar" -> new Definition("awakening_system.skill.dragon_form_avatar.name", "awakening_system.skill.dragon_form_avatar.description", "awakening_system.skill.dragon_form_avatar.effect", Rarity.MYTHIC, "");
            case "dm_dragon_dive" -> new Definition("awakening_system.skill.dm_dragon_dive.name", "awakening_system.skill.dm_dragon_dive.description", "awakening_system.skill.dm_dragon_dive.effect", Rarity.EPIC, "");
            case "dm_bloodthirst" -> new Definition("awakening_system.skill.dm_bloodthirst.name", "awakening_system.skill.dm_bloodthirst.description", "awakening_system.skill.dm_bloodthirst.effect", Rarity.EPIC, "");

            // 9. Sanador Celestial (The S-Classes That I Raised)
            case "sh_caregiver_blessing" -> new Definition("awakening_system.skill.sh_caregiver_blessing.name", "awakening_system.skill.sh_caregiver_blessing.description", "awakening_system.skill.sh_caregiver_blessing.effect", Rarity.UNIQUE, "");
            case "sh_holy_touch" -> new Definition("awakening_system.skill.sh_holy_touch.name", "awakening_system.skill.sh_holy_touch.description", "awakening_system.skill.sh_holy_touch.effect", Rarity.EPIC, "");
            case "sh_divine_grace" -> new Definition("awakening_system.skill.sh_divine_grace.name", "awakening_system.skill.sh_divine_grace.description", "awakening_system.skill.sh_divine_grace.effect", Rarity.LEGENDARY, "");
            case "sh_martyr_soul" -> new Definition("awakening_system.skill.sh_martyr_soul.name", "awakening_system.skill.sh_martyr_soul.description", "awakening_system.skill.sh_martyr_soul.effect", Rarity.MYTHIC, "");
            case "sh_sacred_sanctuary" -> new Definition("awakening_system.skill.sh_sacred_sanctuary.name", "awakening_system.skill.sh_sacred_sanctuary.description", "awakening_system.skill.sh_sacred_sanctuary.effect", Rarity.EPIC, "");
            case "sh_cleansing_radiance" -> new Definition("awakening_system.skill.sh_cleansing_radiance.name", "awakening_system.skill.sh_cleansing_radiance.description", "awakening_system.skill.sh_cleansing_radiance.effect", Rarity.LEGENDARY, "");
            case "sh_aegis_immortal" -> new Definition("awakening_system.skill.sh_aegis_immortal.name", "awakening_system.skill.sh_aegis_immortal.description", "awakening_system.skill.sh_aegis_immortal.effect", Rarity.MYTHIC, "");
            case "sh_tamer_bond" -> new Definition("awakening_system.skill.sh_tamer_bond.name", "awakening_system.skill.sh_tamer_bond.description", "awakening_system.skill.sh_tamer_bond.effect", Rarity.EPIC, "");
            case "sh_awakening_inspire" -> new Definition("awakening_system.skill.sh_awakening_inspire.name", "awakening_system.skill.sh_awakening_inspire.description", "awakening_system.skill.sh_awakening_inspire.effect", Rarity.LEGENDARY, "");
            case "sh_beast_empathy" -> new Definition("awakening_system.skill.sh_beast_empathy.name", "awakening_system.skill.sh_beast_empathy.description", "awakening_system.skill.sh_beast_empathy.effect", Rarity.EPIC, "");

            // 10. Emperador del Relámpago (Lightning Degree)
            case "le_electrification" -> new Definition("awakening_system.skill.le_electrification.name", "awakening_system.skill.le_electrification.description", "awakening_system.skill.le_electrification.effect", Rarity.UNIQUE, "");
            case "le_thunder_spear" -> new Definition("awakening_system.skill.le_thunder_spear.name", "awakening_system.skill.le_thunder_spear.description", "awakening_system.skill.le_thunder_spear.effect", Rarity.EPIC, "");
            case "le_plasma_burst" -> new Definition("awakening_system.skill.le_plasma_burst.name", "awakening_system.skill.le_plasma_burst.description", "awakening_system.skill.le_plasma_burst.effect", Rarity.LEGENDARY, "");
            case "le_wrath_of_thunder_god" -> new Definition("awakening_system.skill.le_wrath_of_thunder_god.name", "awakening_system.skill.le_wrath_of_thunder_god.description", "awakening_system.skill.le_wrath_of_thunder_god.effect", Rarity.MYTHIC, "");
            case "le_flash_step" -> new Definition("awakening_system.skill.le_flash_step.name", "awakening_system.skill.le_flash_step.description", "awakening_system.skill.le_flash_step.effect", Rarity.EPIC, "");
            case "le_overdrive_speed" -> new Definition("awakening_system.skill.le_overdrive_speed.name", "awakening_system.skill.le_overdrive_speed.description", "awakening_system.skill.le_overdrive_speed.effect", Rarity.LEGENDARY, "");
            case "le_static_discharge" -> new Definition("awakening_system.skill.le_static_discharge.name", "awakening_system.skill.le_static_discharge.description", "awakening_system.skill.le_static_discharge.effect", Rarity.EPIC, "");
            case "le_lightning_armor" -> new Definition("awakening_system.skill.le_lightning_armor.name", "awakening_system.skill.le_lightning_armor.description", "awakening_system.skill.le_lightning_armor.effect", Rarity.EPIC, "");
            case "le_grounding_soul" -> new Definition("awakening_system.skill.le_grounding_soul.name", "awakening_system.skill.le_grounding_soul.description", "awakening_system.skill.le_grounding_soul.effect", Rarity.LEGENDARY, "");
            case "le_vajra_indestructible" -> new Definition("awakening_system.skill.le_vajra_indestructible.name", "awakening_system.skill.le_vajra_indestructible.description", "awakening_system.skill.le_vajra_indestructible.effect", Rarity.MYTHIC, "");

            // 11. Emperador de Sangre (Doom Breaker)
            case "be_bloodthirst" -> new Definition("awakening_system.skill.be_bloodthirst.name", "awakening_system.skill.be_bloodthirst.description", "awakening_system.skill.be_bloodthirst.effect", Rarity.UNIQUE, "");
            case "be_blood_lance" -> new Definition("awakening_system.skill.be_blood_lance.name", "awakening_system.skill.be_blood_lance.description", "awakening_system.skill.be_blood_lance.effect", Rarity.EPIC, "");
            case "be_crimson_eruption" -> new Definition("awakening_system.skill.be_crimson_eruption.name", "awakening_system.skill.be_crimson_eruption.description", "awakening_system.skill.be_crimson_eruption.effect", Rarity.LEGENDARY, "");
            case "be_blood_apocalypse" -> new Definition("awakening_system.skill.be_blood_apocalypse.name", "awakening_system.skill.be_blood_apocalypse.description", "awakening_system.skill.be_blood_apocalypse.effect", Rarity.MYTHIC, "");
            case "be_crimson_rush" -> new Definition("awakening_system.skill.be_crimson_rush.name", "awakening_system.skill.be_crimson_rush.description", "awakening_system.skill.be_crimson_rush.effect", Rarity.EPIC, "");
            case "be_vampiric_awakening" -> new Definition("awakening_system.skill.be_vampiric_awakening.name", "awakening_system.skill.be_vampiric_awakening.description", "awakening_system.skill.be_vampiric_awakening.effect", Rarity.EPIC, "");
            case "be_berserk_blood" -> new Definition("awakening_system.skill.be_berserk_blood.name", "awakening_system.skill.be_berserk_blood.description", "awakening_system.skill.be_berserk_blood.effect", Rarity.LEGENDARY, "");
            case "be_blood_barrier" -> new Definition("awakening_system.skill.be_blood_barrier.name", "awakening_system.skill.be_blood_barrier.description", "awakening_system.skill.be_blood_barrier.effect", Rarity.EPIC, "");
            case "be_undying_vitality" -> new Definition("awakening_system.skill.be_undying_vitality.name", "awakening_system.skill.be_undying_vitality.description", "awakening_system.skill.be_undying_vitality.effect", Rarity.LEGENDARY, "");
            case "be_dragon_blood_avatar" -> new Definition("awakening_system.skill.be_dragon_blood_avatar.name", "awakening_system.skill.be_dragon_blood_avatar.description", "awakening_system.skill.be_dragon_blood_avatar.effect", Rarity.MYTHIC, "");

            // 12. Monarca de las Bestias (Solo Leveling / White Tiger)
            case "bm_seismic_roar" -> new Definition("awakening_system.skill.bm_seismic_roar.name", "awakening_system.skill.bm_seismic_roar.description", "awakening_system.skill.bm_seismic_roar.effect", Rarity.UNIQUE, "");
            case "bm_predator_leap" -> new Definition("awakening_system.skill.bm_predator_leap.name", "awakening_system.skill.bm_predator_leap.description", "awakening_system.skill.bm_predator_leap.effect", Rarity.EPIC, "");
            case "bm_feral_rend" -> new Definition("awakening_system.skill.bm_feral_rend.name", "awakening_system.skill.bm_feral_rend.description", "awakening_system.skill.bm_feral_rend.effect", Rarity.LEGENDARY, "");
            case "bm_white_tiger_transformation" -> new Definition("awakening_system.skill.bm_white_tiger_transformation.name", "awakening_system.skill.bm_white_tiger_transformation.description", "awakening_system.skill.bm_white_tiger_transformation.effect", Rarity.MYTHIC, "");
            case "bm_scent_tracking" -> new Definition("awakening_system.skill.bm_scent_tracking.name", "awakening_system.skill.bm_scent_tracking.description", "awakening_system.skill.bm_scent_tracking.effect", Rarity.EPIC, "");
            case "bm_pack_frenzy" -> new Definition("awakening_system.skill.bm_pack_frenzy.name", "awakening_system.skill.bm_pack_frenzy.description", "awakening_system.skill.bm_pack_frenzy.effect", Rarity.LEGENDARY, "");
            case "bm_alpha_intimidation" -> new Definition("awakening_system.skill.bm_alpha_intimidation.name", "awakening_system.skill.bm_alpha_intimidation.description", "awakening_system.skill.bm_alpha_intimidation.effect", Rarity.EPIC, "");
            case "bm_thick_fur" -> new Definition("awakening_system.skill.bm_thick_fur.name", "awakening_system.skill.bm_thick_fur.description", "awakening_system.skill.bm_thick_fur.effect", Rarity.EPIC, "");
            case "bm_wild_resilience" -> new Definition("awakening_system.skill.bm_wild_resilience.name", "awakening_system.skill.bm_wild_resilience.description", "awakening_system.skill.bm_wild_resilience.effect", Rarity.LEGENDARY, "");
            case "bm_titan_roar" -> new Definition("awakening_system.skill.bm_titan_roar.name", "awakening_system.skill.bm_titan_roar.description", "awakening_system.skill.bm_titan_roar.effect", Rarity.MYTHIC, "");

            // Variantes y alias de clase
            case "cb_valterian_instinct" -> new Definition("awakening_system.skill.cb_valterian_instinct.name", "awakening_system.skill.cb_valterian_instinct.description", "awakening_system.skill.cb_valterian_instinct.effect", Rarity.UNIQUE, "");
            case "cb_bloodthirst" -> new Definition("awakening_system.skill.cb_bloodthirst.name", "awakening_system.skill.cb_bloodthirst.description", "awakening_system.skill.cb_bloodthirst.effect", Rarity.EPIC, "");
            case "cb_pain_suppression" -> new Definition("awakening_system.skill.cb_pain_suppression.name", "awakening_system.skill.cb_pain_suppression.description", "awakening_system.skill.cb_pain_suppression.effect", Rarity.EPIC, "");
            case "sa_wall_shield" -> new Definition("awakening_system.skill.sa_wall_shield.name", "awakening_system.skill.sa_wall_shield.description", "awakening_system.skill.sa_wall_shield.effect", Rarity.EPIC, "");
            case "sa_pain_suppression" -> new Definition("awakening_system.skill.sa_pain_suppression.name", "awakening_system.skill.sa_pain_suppression.description", "awakening_system.skill.sa_pain_suppression.effect", Rarity.EPIC, "");
            case "dc_nano_blade_overdrive" -> new Definition("awakening_system.skill.dc_nano_blade_overdrive.name", "awakening_system.skill.dc_nano_blade_overdrive.description", "awakening_system.skill.dc_nano_blade_overdrive.effect", Rarity.EPIC, "");
            case "va_phantom_ambush" -> new Definition("awakening_system.skill.va_phantom_ambush.name", "awakening_system.skill.va_phantom_ambush.description", "awakening_system.skill.va_phantom_ambush.effect", Rarity.EPIC, "");
            case "va_stealth_stalker" -> new Definition("awakening_system.skill.va_stealth_stalker.name", "awakening_system.skill.va_stealth_stalker.description", "awakening_system.skill.va_stealth_stalker.effect", Rarity.EPIC, "");
            case "va_death_mark" -> new Definition("awakening_system.skill.va_death_mark.name", "awakening_system.skill.va_death_mark.description", "awakening_system.skill.va_death_mark.effect", Rarity.EPIC, "");
            case "ga_pain_suppression" -> new Definition("awakening_system.skill.ga_pain_suppression.name", "awakening_system.skill.ga_pain_suppression.description", "awakening_system.skill.ga_pain_suppression.effect", Rarity.EPIC, "");

            case "system_core", "beast_bond", "cataclysm_breaker", "twilight_attunement", "aether_ascension", "deep_dark_resonance", "adventurer_instinct", "eclipse_edge", "eclipse_core" -> modSkill(baseSkillId, skillId.equals(baseSkillId));
            default -> {
                ClassSkillTree.SkillNode treeNode = ClassSkillTree.getNode(baseSkillId);
                if (treeNode != null) {
                    yield new Definition(
                            "awakening_system.skill." + baseSkillId + ".name",
                            "awakening_system.skill." + baseSkillId + ".description",
                            "awakening_system.skill." + baseSkillId + ".effect",
                            treeNode.getRarity(),
                            ""
                    );
                }
                yield null;
            }
        };
    }

    private static Definition modSkill(String baseSkillId, boolean base) {
        String suffix = base ? "" : "_evolution";
        return switch (baseSkillId) {
            case "system_core" -> new Definition("awakening_system.skill.system_core" + suffix + ".name", "awakening_system.skill.system_core" + suffix + ".description", "awakening_system.skill.system_core" + suffix + ".effect", Rarity.MYTHIC, "");
            case "beast_bond" -> new Definition("awakening_system.skill.beast_bond" + suffix + ".name", "awakening_system.skill.beast_bond" + suffix + ".description", "awakening_system.skill.beast_bond" + suffix + ".effect", Rarity.RARE, "alexsmobs");
            case "cataclysm_breaker" -> new Definition("awakening_system.skill.cataclysm_breaker" + suffix + ".name", "awakening_system.skill.cataclysm_breaker" + suffix + ".description", "awakening_system.skill.cataclysm_breaker" + suffix + ".effect", Rarity.LEGENDARY, "cataclysm");
            case "twilight_attunement" -> new Definition("awakening_system.skill.twilight_attunement" + suffix + ".name", "awakening_system.skill.twilight_attunement" + suffix + ".description", "awakening_system.skill.twilight_attunement" + suffix + ".effect", Rarity.EPIC, "twilightforest");
            case "aether_ascension" -> new Definition("awakening_system.skill.aether_ascension" + suffix + ".name", "awakening_system.skill.aether_ascension" + suffix + ".description", "awakening_system.skill.aether_ascension" + suffix + ".effect", Rarity.EPIC, "aether");
            case "deep_dark_resonance" -> new Definition("awakening_system.skill.deep_dark_resonance" + suffix + ".name", "awakening_system.skill.deep_dark_resonance" + suffix + ".description", "awakening_system.skill.deep_dark_resonance" + suffix + ".effect", Rarity.LEGENDARY, "deeperdarker");
            case "adventurer_instinct" -> new Definition("awakening_system.skill.adventurer_instinct" + suffix + ".name", "awakening_system.skill.adventurer_instinct" + suffix + ".description", "awakening_system.skill.adventurer_instinct" + suffix + ".effect", Rarity.RARE, "adventurez");
            case "eclipse_edge" -> new Definition("awakening_system.skill.eclipse_edge" + suffix + ".name", "awakening_system.skill.eclipse_edge" + suffix + ".description", "awakening_system.skill.eclipse_edge" + suffix + ".effect", Rarity.LEGENDARY, "eclipse_expansion");
            case "eclipse_core" -> new Definition("awakening_system.skill.eclipse_core" + suffix + ".name", "awakening_system.skill.eclipse_core" + suffix + ".description", "awakening_system.skill.eclipse_core" + suffix + ".effect", Rarity.MYTHIC, "eclipse_expansion");
            default -> null;
        };
    }

    public static boolean isAvailable(String skillId) {
        if (skillId == null || skillId.isEmpty()) return false;
        if (ClassSkillTree.isSkillTreeNode(skillId)) return true;
        Definition definition = get(skillId);
        if (definition == null) return false;
        if ("eclipse_expansion".equals(definition.requiredMod())) {
            return ModList.get().isLoaded("eclipse_expansion") || ModList.get().isLoaded("eclipse");
        }
        return definition.requiredMod().isEmpty() || ModList.get().isLoaded(definition.requiredMod());
    }

    public static boolean isActive(String skillId) {
        if (skillId == null) return false;
        String base = skillId.endsWith("_evolution") ? skillId.substring(0, skillId.length() - 10) : skillId;
        ClassSkillTree.SkillNode treeNode = ClassSkillTree.getNode(base);
        if (treeNode != null) {
            return treeNode.isActive();
        }
        return switch (base) {
            case "stone_strike", "dimensional_blink", "shadow_step", "kinetic_blast", "tidal_surge",
                 "piercing_shot", "web_snare", "seismic_roar", "blaze_ignition",
                 "spectral_shift", "magnetic_pull", "shadow_monarch",
                 "rulers_authority", "asrahan_mana_blade", "electrification",
                 "nano_blade_overdrive", "dragon_dive", "black_fire_wave", "caregiver_blessing",
                 "unique_absolute_extraction", "unique_rulers_authority_prime",
                 "unique_fourth_wall_infinite", "unique_dragon_breath_annihilation", "isekai_predator", "isekai_dimensional_slash", "isekai_dragon_roar", "isekai_mana_burst",
                 "shadow_exchange", "monarchs_domain", "shadow_colossus_infusion", "stealth_stalker", "bloodlust_gaze",
                 "plum_blossom_rain", "twenty_four_sword_forms", "plum_blossom_domain", "daoist_clarity",
                 "readers_viewpoint", "constellation_favor", "fourth_wall_shatter", "star_stream_climax",
                 "aura_smash", "cross_impact", "valterian_gigantomachia", "iron_muscle_hardening",
                 "heavenly_demon_steps", "demonic_blood_slash", "sky_demon_nine_reversals", "nano_overdrive_suit",
                 "shadow_step_blink", "vital_strike", "infinite_mirror_blades", "smoke_bomb_escape",
                 "elemental_spear", "gravity_crush", "supernova_incineration", "arcane_overload", "time_dilation",
                 "dragon_breath", "meteor_fall", "breath_of_annihilation", "dragon_wings_flight", "dragon_form_avatar",
                 "shonen_infinite_void", "shonen_getsuga_tensho", "shonen_conquerors_haki", "shonen_zoltraak", "shonen_rasenshuriken" -> true;
            default -> false;
        };
    }

    public static boolean isPassive(String skillId) {
        return !isActive(skillId);
    }
}
