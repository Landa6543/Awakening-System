package com.soybe.awakeningsystem;

import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Random;

public final class ConstellationDefinitions {
    public record Constellation(
            String id,
            String nameKey,
            String titleKey,
            String stigmaNameKey,
            String stigmaEffectKey,
            String attributeBonus,
            int defaultDonation
    ) {
        public Component name() { return Component.translatable(nameKey); }
        public Component title() { return Component.translatable(titleKey); }
        public Component stigmaName() { return Component.translatable(stigmaNameKey); }
        public Component stigmaEffect() { return Component.translatable(stigmaEffectKey); }
    }

    private static final List<Constellation> CONSTELLATIONS = List.of(
            new Constellation(
                    "demon_like_judge_of_fire",
                    "awakening_system.constellation.demon_like_judge_of_fire.name",
                    "awakening_system.constellation.demon_like_judge_of_fire.title",
                    "awakening_system.constellation.demon_like_judge_of_fire.stigma",
                    "awakening_system.constellation.demon_like_judge_of_fire.stigma_effect",
                    "vitality",
                    40
            ),
            new Constellation(
                    "abyssal_black_dragon",
                    "awakening_system.constellation.abyssal_black_dragon.name",
                    "awakening_system.constellation.abyssal_black_dragon.title",
                    "awakening_system.constellation.abyssal_black_dragon.stigma",
                    "awakening_system.constellation.abyssal_black_dragon.stigma_effect",
                    "strength",
                    50
            ),
            new Constellation(
                    "secretive_plotter",
                    "awakening_system.constellation.secretive_plotter.name",
                    "awakening_system.constellation.secretive_plotter.title",
                    "awakening_system.constellation.secretive_plotter.stigma",
                    "awakening_system.constellation.secretive_plotter.stigma_effect",
                    "agility",
                    35
            ),
            new Constellation(
                    "prisoner_of_the_golden_headband",
                    "awakening_system.constellation.prisoner_of_the_golden_headband.name",
                    "awakening_system.constellation.prisoner_of_the_golden_headband.title",
                    "awakening_system.constellation.prisoner_of_the_golden_headband.stigma",
                    "awakening_system.constellation.prisoner_of_the_golden_headband.stigma_effect",
                    "willpower",
                    45
            ),
            new Constellation(
                    "master_of_the_underworld",
                    "awakening_system.constellation.master_of_the_underworld.name",
                    "awakening_system.constellation.master_of_the_underworld.title",
                    "awakening_system.constellation.master_of_the_underworld.stigma",
                    "awakening_system.constellation.master_of_the_underworld.stigma_effect",
                    "perception",
                    40
            ),
            new Constellation(
                    "queen_of_the_darkest_night",
                    "awakening_system.constellation.queen_of_the_darkest_night.name",
                    "awakening_system.constellation.queen_of_the_darkest_night.title",
                    "awakening_system.constellation.queen_of_the_darkest_night.stigma",
                    "awakening_system.constellation.queen_of_the_darkest_night.stigma_effect",
                    "vitality",
                    60
            ),
            new Constellation(
                    "salvation_reader",
                    "awakening_system.constellation.salvation_reader.name",
                    "awakening_system.constellation.salvation_reader.title",
                    "awakening_system.constellation.salvation_reader.stigma",
                    "awakening_system.constellation.salvation_reader.stigma_effect",
                    "intelligence",
                    75
            ),

            new Constellation(
                    "architect_of_the_system",
                    "awakening_system.constellation.architect_of_the_system.name",
                    "awakening_system.constellation.architect_of_the_system.title",
                    "awakening_system.constellation.architect_of_the_system.stigma",
                    "awakening_system.constellation.architect_of_the_system.stigma_effect",
                    "intelligence",
                    70
            ),

            new Constellation(
                    "iron_blooded_sword_saint",
                    "awakening_system.constellation.iron_blooded_sword_saint.name",
                    "awakening_system.constellation.iron_blooded_sword_saint.title",
                    "awakening_system.constellation.iron_blooded_sword_saint.stigma",
                    "awakening_system.constellation.iron_blooded_sword_saint.stigma_effect",
                    "strength",
                    55
            ),
            new Constellation(
                    "sacred_water_dragon",
                    "awakening_system.constellation.sacred_water_dragon.name",
                    "awakening_system.constellation.sacred_water_dragon.title",
                    "awakening_system.constellation.sacred_water_dragon.stigma",
                    "awakening_system.constellation.sacred_water_dragon.stigma_effect",
                    "vitality",
                    50
            ),

            new Constellation(
                    "martial_king_nayuta",
                    "awakening_system.constellation.martial_king_nayuta.name",
                    "awakening_system.constellation.martial_king_nayuta.title",
                    "awakening_system.constellation.martial_king_nayuta.stigma",
                    "awakening_system.constellation.martial_king_nayuta.stigma_effect",
                    "strength",
                    65
            ),

            new Constellation(
                    "caregiver_of_the_abyss",
                    "awakening_system.constellation.caregiver_of_the_abyss.name",
                    "awakening_system.constellation.caregiver_of_the_abyss.title",
                    "awakening_system.constellation.caregiver_of_the_abyss.stigma",
                    "awakening_system.constellation.caregiver_of_the_abyss.stigma_effect",
                    "willpower",
                    45
            ),

            new Constellation(
                    "murim_heavenly_demon",
                    "awakening_system.constellation.murim_heavenly_demon.name",
                    "awakening_system.constellation.murim_heavenly_demon.title",
                    "awakening_system.constellation.murim_heavenly_demon.stigma",
                    "awakening_system.constellation.murim_heavenly_demon.stigma_effect",
                    "agility",
                    60
            ),

            new Constellation(
                    "master_of_mobius_gacha",
                    "awakening_system.constellation.master_of_mobius_gacha.name",
                    "awakening_system.constellation.master_of_mobius_gacha.title",
                    "awakening_system.constellation.master_of_mobius_gacha.stigma",
                    "awakening_system.constellation.master_of_mobius_gacha.stigma_effect",
                    "perception",
                    50
            ),

            new Constellation(
                    "blue_dragon_of_retribution",
                    "awakening_system.constellation.blue_dragon_of_retribution.name",
                    "awakening_system.constellation.blue_dragon_of_retribution.title",
                    "awakening_system.constellation.blue_dragon_of_retribution.stigma",
                    "awakening_system.constellation.blue_dragon_of_retribution.stigma_effect",
                    "strength",
                    65
            ),

            new Constellation(
                    "master_of_steel",
                    "awakening_system.constellation.master_of_steel.name",
                    "awakening_system.constellation.master_of_steel.title",
                    "awakening_system.constellation.master_of_steel.stigma",
                    "awakening_system.constellation.master_of_steel.stigma_effect",
                    "vitality",
                    45
            ),
            new Constellation(
                    "maritime_war_god",
                    "awakening_system.constellation.maritime_war_god.name",
                    "awakening_system.constellation.maritime_war_god.title",
                    "awakening_system.constellation.maritime_war_god.stigma",
                    "awakening_system.constellation.maritime_war_god.stigma_effect",
                    "willpower",
                    60
            ),
            new Constellation(
                    "first_sword_of_goryeo",
                    "awakening_system.constellation.first_sword_of_goryeo.name",
                    "awakening_system.constellation.first_sword_of_goryeo.title",
                    "awakening_system.constellation.first_sword_of_goryeo.stigma",
                    "awakening_system.constellation.first_sword_of_goryeo.stigma_effect",
                    "strength",
                    70
            ),
            new Constellation(
                    "flame_emperor",
                    "awakening_system.constellation.flame_emperor.name",
                    "awakening_system.constellation.flame_emperor.title",
                    "awakening_system.constellation.flame_emperor.stigma",
                    "awakening_system.constellation.flame_emperor.stigma_effect",
                    "strength",
                    55
            ),
            new Constellation(
                    "death_king_hunter",
                    "awakening_system.constellation.death_king_hunter.name",
                    "awakening_system.constellation.death_king_hunter.title",
                    "awakening_system.constellation.death_king_hunter.stigma",
                    "awakening_system.constellation.death_king_hunter.stigma_effect",
                    "vitality",
                    60
            ),
            new Constellation(
                    "abyssal_eye_stalker",
                    "awakening_system.constellation.abyssal_eye_stalker.name",
                    "awakening_system.constellation.abyssal_eye_stalker.title",
                    "awakening_system.constellation.abyssal_eye_stalker.stigma",
                    "awakening_system.constellation.abyssal_eye_stalker.stigma_effect",
                    "perception",
                    50
            ),
            new Constellation(
                    "great_sage_heaven_equal",
                    "awakening_system.constellation.great_sage_heaven_equal.name",
                    "awakening_system.constellation.great_sage_heaven_equal.title",
                    "awakening_system.constellation.great_sage_heaven_equal.stigma",
                    "awakening_system.constellation.great_sage_heaven_equal.stigma_effect",
                    "strength",
                    80
            ),
            new Constellation(
                    "nine_heavens_golden_dragon",
                    "awakening_system.constellation.nine_heavens_golden_dragon.name",
                    "awakening_system.constellation.nine_heavens_golden_dragon.title",
                    "awakening_system.constellation.nine_heavens_golden_dragon.stigma",
                    "awakening_system.constellation.nine_heavens_golden_dragon.stigma_effect",
                    "vitality",
                    75
            ),
            new Constellation(
                    "scribe_of_cosmic_destiny",
                    "awakening_system.constellation.scribe_of_cosmic_destiny.name",
                    "awakening_system.constellation.scribe_of_cosmic_destiny.title",
                    "awakening_system.constellation.scribe_of_cosmic_destiny.stigma",
                    "awakening_system.constellation.scribe_of_cosmic_destiny.stigma_effect",
                    "intelligence",
                    85
            ),
            new Constellation(
                    "monarch_of_eternal_calamity",
                    "awakening_system.constellation.monarch_of_eternal_calamity.name",
                    "awakening_system.constellation.monarch_of_eternal_calamity.title",
                    "awakening_system.constellation.monarch_of_eternal_calamity.stigma",
                    "awakening_system.constellation.monarch_of_eternal_calamity.stigma_effect",
                    "strength",
                    70
            ),
            new Constellation(
                    "primordial_shadow_lord",
                    "awakening_system.constellation.primordial_shadow_lord.name",
                    "awakening_system.constellation.primordial_shadow_lord.title",
                    "awakening_system.constellation.primordial_shadow_lord.stigma",
                    "awakening_system.constellation.primordial_shadow_lord.stigma_effect",
                    "agility",
                    80
            ),
            new Constellation(
                    "lightning_titan_olympus",
                    "awakening_system.constellation.lightning_titan_olympus.name",
                    "awakening_system.constellation.lightning_titan_olympus.title",
                    "awakening_system.constellation.lightning_titan_olympus.stigma",
                    "awakening_system.constellation.lightning_titan_olympus.stigma_effect",
                    "strength",
                    75
            ),
            new Constellation(
                    "silver_moon_huntress",
                    "awakening_system.constellation.silver_moon_huntress.name",
                    "awakening_system.constellation.silver_moon_huntress.title",
                    "awakening_system.constellation.silver_moon_huntress.stigma",
                    "awakening_system.constellation.silver_moon_huntress.stigma_effect",
                    "perception",
                    60
            ),
            new Constellation(
                    "master_of_the_infinite_labyrinth",
                    "awakening_system.constellation.master_of_the_infinite_labyrinth.name",
                    "awakening_system.constellation.master_of_the_infinite_labyrinth.title",
                    "awakening_system.constellation.master_of_the_infinite_labyrinth.stigma",
                    "awakening_system.constellation.master_of_the_infinite_labyrinth.stigma_effect",
                    "willpower",
                    55
            ),
            new Constellation(
                    "eternal_rebirth_phoenix",
                    "awakening_system.constellation.eternal_rebirth_phoenix.name",
                    "awakening_system.constellation.eternal_rebirth_phoenix.title",
                    "awakening_system.constellation.eternal_rebirth_phoenix.stigma",
                    "awakening_system.constellation.eternal_rebirth_phoenix.stigma_effect",
                    "vitality",
                    70
            ),
            new Constellation(
                    "ruler_of_the_howling_gales",
                    "awakening_system.constellation.ruler_of_the_howling_gales.name",
                    "awakening_system.constellation.ruler_of_the_howling_gales.title",
                    "awakening_system.constellation.ruler_of_the_howling_gales.stigma",
                    "awakening_system.constellation.ruler_of_the_howling_gales.stigma_effect",
                    "agility",
                    65
            ),
            new Constellation(
                    "judge_of_the_underworld_scales",
                    "awakening_system.constellation.judge_of_the_underworld_scales.name",
                    "awakening_system.constellation.judge_of_the_underworld_scales.title",
                    "awakening_system.constellation.judge_of_the_underworld_scales.stigma",
                    "awakening_system.constellation.judge_of_the_underworld_scales.stigma_effect",
                    "willpower",
                    60
            ),
            new Constellation(
                    "weaver_of_forgotten_fate",
                    "awakening_system.constellation.weaver_of_forgotten_fate.name",
                    "awakening_system.constellation.weaver_of_forgotten_fate.title",
                    "awakening_system.constellation.weaver_of_forgotten_fate.stigma",
                    "awakening_system.constellation.weaver_of_forgotten_fate.stigma_effect",
                    "intelligence",
                    70
            ),
            new Constellation(
                    "plum_blossom_sword_saint",
                    "awakening_system.constellation.plum_blossom_sword_saint.name",
                    "awakening_system.constellation.plum_blossom_sword_saint.title",
                    "awakening_system.constellation.plum_blossom_sword_saint.stigma",
                    "awakening_system.constellation.plum_blossom_sword_saint.stigma_effect",
                    "agility",
                    65
            ),
            new Constellation(
                    "hell_dimension_survivor",
                    "awakening_system.constellation.hell_dimension_survivor.name",
                    "awakening_system.constellation.hell_dimension_survivor.title",
                    "awakening_system.constellation.hell_dimension_survivor.stigma",
                    "awakening_system.constellation.hell_dimension_survivor.stigma_effect",
                    "strength",
                    70
            ),
            new Constellation(
                    "reaper_of_drifting_moon",
                    "awakening_system.constellation.reaper_of_drifting_moon.name",
                    "awakening_system.constellation.reaper_of_drifting_moon.title",
                    "awakening_system.constellation.reaper_of_drifting_moon.stigma",
                    "awakening_system.constellation.reaper_of_drifting_moon.stigma_effect",
                    "perception",
                    60
            ),
            new Constellation(
                    "tower_gate_opener",
                    "awakening_system.constellation.tower_gate_opener.name",
                    "awakening_system.constellation.tower_gate_opener.title",
                    "awakening_system.constellation.tower_gate_opener.stigma",
                    "awakening_system.constellation.tower_gate_opener.stigma_effect",
                    "willpower",
                    85
            ),
            new Constellation(
                    "supreme_regressor_of_steel",
                    "awakening_system.constellation.supreme_regressor_of_steel.name",
                    "awakening_system.constellation.supreme_regressor_of_steel.title",
                    "awakening_system.constellation.supreme_regressor_of_steel.stigma",
                    "awakening_system.constellation.supreme_regressor_of_steel.stigma_effect",
                    "strength",
                    80
            ),
            new Constellation(
                    "shadow_monarch_ashborn",
                    "awakening_system.constellation.shadow_monarch_ashborn.name",
                    "awakening_system.constellation.shadow_monarch_ashborn.title",
                    "awakening_system.constellation.shadow_monarch_ashborn.stigma",
                    "awakening_system.constellation.shadow_monarch_ashborn.stigma_effect",
                    "willpower",
                    90
            ),
            new Constellation(
                    "radiant_sword_princess",
                    "awakening_system.constellation.radiant_sword_princess.name",
                    "awakening_system.constellation.radiant_sword_princess.title",
                    "awakening_system.constellation.radiant_sword_princess.stigma",
                    "awakening_system.constellation.radiant_sword_princess.stigma_effect",
                    "agility",
                    65
            ),
            new Constellation(
                    "dragon_emperor_of_destruction",
                    "awakening_system.constellation.dragon_emperor_of_destruction.name",
                    "awakening_system.constellation.dragon_emperor_of_destruction.title",
                    "awakening_system.constellation.dragon_emperor_of_destruction.stigma",
                    "awakening_system.constellation.dragon_emperor_of_destruction.stigma_effect",
                    "vitality",
                    85
            ),
            new Constellation(
                    "mad_dog_of_the_spear",
                    "awakening_system.constellation.mad_dog_of_the_spear.name",
                    "awakening_system.constellation.mad_dog_of_the_spear.title",
                    "awakening_system.constellation.mad_dog_of_the_spear.stigma",
                    "awakening_system.constellation.mad_dog_of_the_spear.stigma_effect",
                    "strength",
                    60
            ),
            new Constellation(
                    "strategist_of_the_blue_flame",
                    "awakening_system.constellation.strategist_of_the_blue_flame.name",
                    "awakening_system.constellation.strategist_of_the_blue_flame.title",
                    "awakening_system.constellation.strategist_of_the_blue_flame.stigma",
                    "awakening_system.constellation.strategist_of_the_blue_flame.stigma_effect",
                    "intelligence",
                    75
            ),
            new Constellation(
                    "righteous_sword_of_mount_hua",
                    "awakening_system.constellation.righteous_sword_of_mount_hua.name",
                    "awakening_system.constellation.righteous_sword_of_mount_hua.title",
                    "awakening_system.constellation.righteous_sword_of_mount_hua.stigma",
                    "awakening_system.constellation.righteous_sword_of_mount_hua.stigma_effect",
                    "agility",
                    65
            ),
            new Constellation(
                    "death_defying_regressor",
                    "awakening_system.constellation.death_defying_regressor.name",
                    "awakening_system.constellation.death_defying_regressor.title",
                    "awakening_system.constellation.death_defying_regressor.stigma",
                    "awakening_system.constellation.death_defying_regressor.stigma_effect",
                    "willpower",
                    70
            ),
            new Constellation(
                    "grand_marshal_of_shadows",
                    "awakening_system.constellation.grand_marshal_of_shadows.name",
                    "awakening_system.constellation.grand_marshal_of_shadows.title",
                    "awakening_system.constellation.grand_marshal_of_shadows.stigma",
                    "awakening_system.constellation.grand_marshal_of_shadows.stigma_effect",
                    "vitality",
                    75
            ),
            new Constellation(
                    "blood_red_commander",
                    "awakening_system.constellation.blood_red_commander.name",
                    "awakening_system.constellation.blood_red_commander.title",
                    "awakening_system.constellation.blood_red_commander.stigma",
                    "awakening_system.constellation.blood_red_commander.stigma_effect",
                    "strength",
                    70
            )
    );

    private static final Random RNG = new Random();

    private ConstellationDefinitions() {}

    public static List<Constellation> constellations() {
        return CONSTELLATIONS;
    }

    public static Constellation get(String id) {
        return CONSTELLATIONS.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
    }

    public enum ReactionType {
        LEVEL_UP, LOW_HEALTH, BOSS_KILL, DUNGEON_ENTER, COMBO_TRANCE
    }

    public static String reactionKey(String constellationId, ReactionType type) {
        return reactionKey(constellationId, type, RNG.nextInt(3));
    }

    public static String reactionKey(String constellationId, ReactionType type, int variant) {
        String suffix = switch (type) {
            case LEVEL_UP -> "react.level_up";
            case LOW_HEALTH -> "react.low_health";
            case BOSS_KILL -> "react.boss_kill";
            case DUNGEON_ENTER -> "react.dungeon_enter";
            case COMBO_TRANCE -> "react.combo";
        };
        if (variant > 0) {
            return "awakening_system.constellation." + constellationId + "." + suffix + "." + variant;
        }
        return "awakening_system.constellation." + constellationId + "." + suffix;
    }

    public static Constellation randomConstellation() {
        return CONSTELLATIONS.get(RNG.nextInt(CONSTELLATIONS.size()));
    }

    public static Constellation randomOtherConstellation(String excludeId) {
        List<Constellation> filtered = CONSTELLATIONS.stream()
                .filter(c -> excludeId == null || !c.id().equals(excludeId))
                .toList();
        if (filtered.isEmpty()) return randomConstellation();
        return filtered.get(RNG.nextInt(filtered.size()));
    }
}
