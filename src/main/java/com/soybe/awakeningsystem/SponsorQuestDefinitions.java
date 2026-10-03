package com.soybe.awakeningsystem;

import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Map;

public final class SponsorQuestDefinitions {
    public record Quest(
            String id,
            String constellationId,
            String titleKey,
            String descriptionKey,
            String referenceKey,
            int targetCount,
            int coinReward,
            String bonusStat,
            int bonusStatAmount
    ) {
        public Component title() { return Component.translatable(titleKey); }
        public Component description() { return Component.translatable(descriptionKey); }
        public Component reference() { return Component.translatable(referenceKey); }
    }

    private static final Map<String, List<Quest>> SPONSOR_QUESTS = Map.of(
            "prisoner_of_the_golden_headband", List.of(
                    new Quest("wukong_1", "prisoner_of_the_golden_headband",
                            "awakening_system.sponsor_quest.wukong_1.title",
                            "awakening_system.sponsor_quest.wukong_1.desc",
                            "awakening_system.sponsor_quest.wukong_1.ref",
                            25, 300, "strength", 4),
                    new Quest("wukong_2", "prisoner_of_the_golden_headband",
                            "awakening_system.sponsor_quest.wukong_2.title",
                            "awakening_system.sponsor_quest.wukong_2.desc",
                            "awakening_system.sponsor_quest.wukong_2.ref",
                            50, 600, "willpower", 6)
            ),
            "abyssal_black_dragon", List.of(
                    new Quest("black_dragon_1", "abyssal_black_dragon",
                            "awakening_system.sponsor_quest.black_dragon_1.title",
                            "awakening_system.sponsor_quest.black_dragon_1.desc",
                            "awakening_system.sponsor_quest.black_dragon_1.ref",
                            30, 350, "intelligence", 5),
                    new Quest("black_dragon_2", "abyssal_black_dragon",
                            "awakening_system.sponsor_quest.black_dragon_2.title",
                            "awakening_system.sponsor_quest.black_dragon_2.desc",
                            "awakening_system.sponsor_quest.black_dragon_2.ref",
                            60, 700, "strength", 8)
            ),
            "demon_like_judge_of_fire", List.of(
                    new Quest("uriel_1", "demon_like_judge_of_fire",
                            "awakening_system.sponsor_quest.uriel_1.title",
                            "awakening_system.sponsor_quest.uriel_1.desc",
                            "awakening_system.sponsor_quest.uriel_1.ref",
                            20, 300, "vitality", 5),
                    new Quest("uriel_2", "demon_like_judge_of_fire",
                            "awakening_system.sponsor_quest.uriel_2.title",
                            "awakening_system.sponsor_quest.uriel_2.desc",
                            "awakening_system.sponsor_quest.uriel_2.ref",
                            45, 650, "vitality", 8)
            ),
            "architect_of_the_system", List.of(
                    new Quest("ashborn_1", "architect_of_the_system",
                            "awakening_system.sponsor_quest.ashborn_1.title",
                            "awakening_system.sponsor_quest.ashborn_1.desc",
                            "awakening_system.sponsor_quest.ashborn_1.ref",
                            15, 400, "intelligence", 6),
                    new Quest("ashborn_2", "architect_of_the_system",
                            "awakening_system.sponsor_quest.ashborn_2.title",
                            "awakening_system.sponsor_quest.ashborn_2.desc",
                            "awakening_system.sponsor_quest.ashborn_2.ref",
                            1, 800, "agility", 8)
            ),
            "salvation_reader", List.of(
                    new Quest("dokja_1", "salvation_reader",
                            "awakening_system.sponsor_quest.dokja_1.title",
                            "awakening_system.sponsor_quest.dokja_1.desc",
                            "awakening_system.sponsor_quest.dokja_1.ref",
                            10, 350, "perception", 5),
                    new Quest("dokja_2", "salvation_reader",
                            "awakening_system.sponsor_quest.dokja_2.title",
                            "awakening_system.sponsor_quest.dokja_2.desc",
                            "awakening_system.sponsor_quest.dokja_2.ref",
                            3, 750, "intelligence", 10)
            )
    );

    private SponsorQuestDefinitions() {}

    public static List<Quest> getQuestsForSponsor(String constellationId) {
        return SPONSOR_QUESTS.getOrDefault(constellationId, List.of());
    }

    public static Quest getQuest(String questId) {
        if (questId == null || questId.isEmpty()) return null;
        for (var list : SPONSOR_QUESTS.values()) {
            for (var q : list) {
                if (q.id().equals(questId)) return q;
            }
        }
        return null;
    }
}
