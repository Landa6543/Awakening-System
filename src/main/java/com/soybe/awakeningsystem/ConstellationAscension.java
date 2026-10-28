package com.soybe.awakeningsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Random;

public final class ConstellationAscension {
    private static final Random RNG = new Random();

    private static final List<String> STRENGTH_TITLES = List.of(
            "Sovereign of the Primordial Sword",
            "Destroyer of Mountains and Skies",
            "Lord of Iron Fury",
            "Titan of Blood Judgment",
            "Monarch of the Unbreakable Fist"
    );

    private static final List<String> AGILITY_TITLES = List.of(
            "Shadow of the Silent Wind",
            "Hunter of the Silver Moon",
            "Dance of the Spectral Storm",
            "Ghost of the Swift Abyss",
            "Lightning of the Starry Night"
    );

    private static final List<String> VITALITY_TITLES = List.of(
            "Guardian of Eternal Life",
            "Bastion of Imperishable Fortitude",
            "Heart of the Immortal Phoenix",
            "Lord of the Hundred Wounds",
            "Shield of Endless Twilight"
    );

    private static final List<String> INTELLIGENCE_TITLES = List.of(
            "Architect of Arcane Knowledge",
            "Master of the Infinite Library",
            "Ruler of Cosmic Destiny",
            "Weaver of Stellar Chaos",
            "Master of Void Secrets"
    );

    private static final List<String> WILLPOWER_TITLES = List.of(
            "Monarch of Unyielding Will",
            "Hermit of the Soul Vortex",
            "Judge of the Somber Void",
            "Sovereign of the Crimson Trance",
            "Spirit That Defies Fate"
    );

    private static final List<String> PERCEPTION_TITLES = List.of(
            "Eye of Forgotten Destiny",
            "Watcher of Distant Reality",
            "Oracle of Silent Twilight",
            "Sentinel of Distant Stars",
            "He Who Sees Through the Veil"
    );

    private ConstellationAscension() {}

    public static String generateModifier(SystemData data) {
        int str = data.strength();
        int agi = data.agility();
        int vit = data.vitality();
        int in = data.intelligence();
        int wil = data.willpower();
        int per = data.perception();

        int max = Math.max(str, Math.max(agi, Math.max(vit, Math.max(in, Math.max(wil, per)))));
        List<String> pool;
        if (max == str) pool = STRENGTH_TITLES;
        else if (max == agi) pool = AGILITY_TITLES;
        else if (max == in) pool = INTELLIGENCE_TITLES;
        else if (max == vit) pool = VITALITY_TITLES;
        else if (max == wil) pool = WILLPOWER_TITLES;
        else pool = PERCEPTION_TITLES;

        return pool.get(RNG.nextInt(pool.size()));
    }

    public static boolean triggerAscension(ServerPlayer player, String reason, String customModifier) {
        SystemData data = SystemEvents.data(player);
        if (data == null) return false;
        if (data.isConstellation()) return false;

        String modifier = (customModifier != null && !customModifier.trim().isEmpty())
                ? customModifier.trim()
                : generateModifier(data);

        data.setIsConstellation(true);
        data.setConstellationModifier(modifier);

        if (player.getServer() != null) {
            String pName = player.getScoreboardName();
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0F, 1.0F);

                String titleKey = getTitleTranslationKey(modifier);
                String displayMod = (titleKey != null) ? titleKey : modifier;
                if (p.getUUID().equals(player.getUUID())) {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.constellation.ascension_self_title",
                            "awakening_system.notification.constellation.ascension_self_msg",
                            0xFFFFD700, displayMod, reason);
                } else {
                    AwakeningSystemMod.notify(p,
                            "awakening_system.notification.constellation.ascension_broadcast_title",
                            "awakening_system.notification.constellation.ascension_broadcast_msg",
                            0xFFFFD700, pName, displayMod);
                }
            }
        }

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.2D, 1.5D, 1.2D, 0.15D);
            sl.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.0D, 1.0D, 1.0D, 0.08D);
            sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2D, player.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.0D);
        }

        AwakeningSystemMod.sync(player);
        return true;
    }

    public static boolean isEligibleForAscension(SystemData data) {
        if (data == null || data.isConstellation()) return false;
        // 1. Requisito de Nivel: Al menos Nivel 50
        if (data.level() < 50) return false;
        // 2. Requisito de Fábulas: Al menos 8 fábulas desbloqueadas
        if (data.unlockedFables().size() < 8) return false;
        // 3. Requisito de Peso Narrativo: Al menos 400 puntos de peso
        if (data.fableWeight() < 400) return false;

        // 4. Pirámide Narrativa Canónica del Star Stream:
        // - Al menos 1 Fábula MÍTICA o RELATO GIGANTE
        // - Al menos 2 Fábulas LEGENDARIAS o SEMIMÍTICAS
        // - Al menos 3 Fábulas HISTÓRICAS
        int mythOrGiant = 0;
        int legendaryOrSemi = 0;
        int historical = 0;

        for (String fId : data.unlockedFables()) {
            var f = com.soybe.awakeningsystem.fable.FableDefinitions.get(fId);
            if (f == null) continue;
            switch (f.grade()) {
                case GIANT_STORY, MYTH -> mythOrGiant++;
                case LEGENDARY, SEMI_MYTH -> legendaryOrSemi++;
                case HISTORICAL -> historical++;
            }
        }

        return mythOrGiant >= 1 && legendaryOrSemi >= 2 && historical >= 3;
    }

    public static void checkMonarchBossDefeat(ServerPlayer player, int bossVariant) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        SystemEvents.grantFable(player, "first_monarch_defeat");
        checkFableAscension(player);
    }

    public static void checkRedGateClear(ServerPlayer player, String rank) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        int tier = DungeonManager.getRankTier(rank);
        if (tier >= 7) {
            SystemEvents.grantFable(player, "dungeon_red_gate_conqueror");
            if (tier >= 10) {
                SystemEvents.grantFable(player, "gate_breaker_s_rank");
            }
            checkFableAscension(player);
        }
    }

    public static void checkLevelMilestone(ServerPlayer player, int level) {
        SystemData data = SystemEvents.data(player);
        if (data == null) return;
        if (level >= 100) {
            SystemEvents.grantFable(player, "first_level_100");
        }
        if (level >= 200) {
            SystemEvents.grantFable(player, "level_200_demigod");
        }
        checkFableAscension(player);
    }

    public static void checkFableAscension(ServerPlayer player) {
        SystemData data = SystemEvents.data(player);
        if (data == null || data.isConstellation()) return;

        if (isEligibleForAscension(data)) {
            triggerAscension(player, "awakening_system.ascension.reason.fable_accumulation@@@" + data.fableWeight(), null);
        }
    }

    public static String getTitleTranslationKey(String modifier) {
        if (modifier == null || modifier.isEmpty()) {
            return "awakening_system.constellation.default_modifier";
        }
        return switch (modifier.trim()) {
            case "Sovereign of the Primordial Sword" -> "awakening_system.constellation.title.sovereign_primordial_sword";
            case "Destroyer of Mountains and Skies" -> "awakening_system.constellation.title.destroyer_mountains_skies";
            case "Lord of Iron Fury" -> "awakening_system.constellation.title.lord_iron_fury";
            case "Titan of Blood Judgment" -> "awakening_system.constellation.title.titan_blood_judgment";
            case "Monarch of the Unbreakable Fist" -> "awakening_system.constellation.title.monarch_unbreakable_fist";

            case "Shadow of the Silent Wind" -> "awakening_system.constellation.title.shadow_silent_wind";
            case "Hunter of the Silver Moon" -> "awakening_system.constellation.title.hunter_silver_moon";
            case "Dance of the Spectral Storm" -> "awakening_system.constellation.title.dance_spectral_storm";
            case "Ghost of the Swift Abyss" -> "awakening_system.constellation.title.ghost_swift_abyss";
            case "Lightning of the Starry Night" -> "awakening_system.constellation.title.lightning_starry_night";

            case "Guardian of Eternal Life" -> "awakening_system.constellation.title.guardian_eternal_life";
            case "Bastion of Imperishable Fortitude" -> "awakening_system.constellation.title.bastion_imperishable_fortitude";
            case "Heart of the Immortal Phoenix" -> "awakening_system.constellation.title.heart_immortal_phoenix";
            case "Lord of the Hundred Wounds" -> "awakening_system.constellation.title.lord_hundred_wounds";
            case "Shield of Endless Twilight" -> "awakening_system.constellation.title.shield_endless_twilight";

            case "Architect of Arcane Knowledge" -> "awakening_system.constellation.title.architect_arcane_knowledge";
            case "Master of the Infinite Library" -> "awakening_system.constellation.title.master_infinite_library";
            case "Ruler of Cosmic Destiny" -> "awakening_system.constellation.title.ruler_cosmic_destiny";
            case "Weaver of Stellar Chaos" -> "awakening_system.constellation.title.weaver_stellar_chaos";
            case "Master of Void Secrets" -> "awakening_system.constellation.title.master_void_secrets";

            case "Monarch of Unyielding Will" -> "awakening_system.constellation.title.monarch_unyielding_will";
            case "Hermit of the Soul Vortex" -> "awakening_system.constellation.title.hermit_soul_vortex";
            case "Judge of the Somber Void" -> "awakening_system.constellation.title.judge_somber_void";
            case "Sovereign of the Crimson Trance" -> "awakening_system.constellation.title.sovereign_crimson_trance";
            case "Spirit That Defies Fate" -> "awakening_system.constellation.title.spirit_defies_fate";

            case "Eye of Forgotten Destiny" -> "awakening_system.constellation.title.eye_forgotten_destiny";
            case "Watcher of Distant Reality" -> "awakening_system.constellation.title.watcher_distant_reality";
            case "Oracle of Silent Twilight" -> "awakening_system.constellation.title.oracle_silent_twilight";
            case "Sentinel of Distant Stars" -> "awakening_system.constellation.title.sentinel_distant_stars";
            case "He Who Sees Through the Veil" -> "awakening_system.constellation.title.he_who_sees_veil";

            case "Awakened Constellation" -> "awakening_system.constellation.default_modifier";
            default -> null;
        };
    }

    public static Component getTranslatedTitleComponent(String modifier) {
        String key = getTitleTranslationKey(modifier);
        if (key != null) {
            return Component.translatable(key);
        }
        return (modifier != null && !modifier.isEmpty()) ? Component.literal(modifier) : Component.translatable("awakening_system.constellation.default_modifier");
    }
}
