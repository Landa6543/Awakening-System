package com.soybe.awakeningsystem;

import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TitleDefinitions {
    public static final UUID CREATOR_UUID = UUID.fromString("9c6e2002-2a3d-4a19-bc57-ecd32c1f129d");
    public static final UUID CREATOR_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:LanderAlo".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID CREATOR_SOYBE_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:soybe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID CREATOR_SOYBE_CAP_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Soybe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final String CREATOR_NAME = "LanderAlo";

    public static boolean isCreator(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (CREATOR_UUID.equals(uuid) || CREATOR_OFFLINE_UUID.equals(uuid)
                || CREATOR_SOYBE_OFFLINE_UUID.equals(uuid) || CREATOR_SOYBE_CAP_OFFLINE_UUID.equals(uuid)) {
            return true;
        }
        if (player.getGameProfile() != null) {
            String name = player.getGameProfile().getName();
            if (CREATOR_NAME.equalsIgnoreCase(name) || "soybe".equalsIgnoreCase(name) || "Soybe".equalsIgnoreCase(name) || "soybe_".equalsIgnoreCase(name)) {
                return true;
            }
        }
        String cleanUuid = uuid.toString().replace("-", "");
        return "9c6e20022a3d4a19bc57ecd32c1f129d".equalsIgnoreCase(cleanUuid);
    }

    public static final UUID NANDA_UUID = UUID.fromString("21115ef9-68a9-4612-ac2c-2b27d7527849");
    public static final UUID NANDA_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Nandachan".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID NANDA_SHORT_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Nanda".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public static boolean isNanda(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (NANDA_UUID.equals(uuid) || NANDA_OFFLINE_UUID.equals(uuid) || NANDA_SHORT_OFFLINE_UUID.equals(uuid)) {
            return true;
        }
        String cleanUuid = uuid.toString().replace("-", "");
        return "21115ef968a94612ac2c2b27d7527849".equalsIgnoreCase(cleanUuid);
    }

    public static final UUID MISTIC_POTATO_UUID = UUID.fromString("936dab1b-f931-45b7-9f6d-e9ad2430e66a");
    public static final UUID MISTIC_POTATO_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:MisticPotato".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID MISTIC_POTATO_UNDERSCORE_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Mistic_Potato".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID MISTIC_POTATO_SHORT_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Potato".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public static boolean isMisticPotato(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (MISTIC_POTATO_UUID.equals(uuid) || MISTIC_POTATO_OFFLINE_UUID.equals(uuid)
                || MISTIC_POTATO_UNDERSCORE_OFFLINE_UUID.equals(uuid) || MISTIC_POTATO_SHORT_OFFLINE_UUID.equals(uuid)) {
            return true;
        }
        String cleanUuid = uuid.toString().replace("-", "");
        return "936dab1bf93145b79f6de9ad2430e66a".equalsIgnoreCase(cleanUuid);
    }

    public static final UUID SCHVIS_UUID = UUID.fromString("dddf8989-c266-4b5f-b91a-987f9b3e7c64");
    public static final UUID SCHVIS_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Schvis".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID SCHVIS_LOWER_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:schvis".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public static boolean isSchvis(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (SCHVIS_UUID.equals(uuid) || SCHVIS_OFFLINE_UUID.equals(uuid) || SCHVIS_LOWER_OFFLINE_UUID.equals(uuid)) {
            return true;
        }
        String cleanUuid = uuid.toString().replace("-", "");
        return "dddf8989c2664b5fb91a987f9b3e7c64".equalsIgnoreCase(cleanUuid);
    }

    public static final UUID TOMCRAFT_UUID = UUID.fromString("e846a525-b14a-4f6a-963e-bf8d2f2db519");
    public static final UUID TOMCRAFT_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:Tomcraft2003".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    public static final UUID TOMCRAFT_LOWER_OFFLINE_UUID = UUID.nameUUIDFromBytes("OfflinePlayer:tomcraft2003".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public static boolean isTomcraft(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (TOMCRAFT_UUID.equals(uuid) || TOMCRAFT_OFFLINE_UUID.equals(uuid) || TOMCRAFT_LOWER_OFFLINE_UUID.equals(uuid)) {
            return true;
        }
        if (player.getGameProfile() != null) {
            String name = player.getGameProfile().getName();
            if ("Tomcraft2003".equalsIgnoreCase(name)) {
                return true;
            }
        }
        String cleanUuid = uuid.toString().replace("-", "");
        return "e846a525b14a4f6a963ebf8d2f2db519".equalsIgnoreCase(cleanUuid);
    }

    private TitleDefinitions() {}

    public record Definition(String id, String translationKey, String requiredMod, SkillDefinitions.Rarity rarity, String bonusKey, String descriptionKey) {
        public Definition(String id, String translationKey, String requiredMod) {
            this(id, translationKey, requiredMod, defaultRarity(id), bonusKey(id), "awakening_system.title.description." + id);
        }
        private static String bonusKey(String id) {
            if (id.startsWith("rank_")) return "awakening_system.title.bonus.rank";
            return switch (id) {
                case "stone_breaker", "deep_delver", "librarian", "master_builder" -> "awakening_system.title.bonus.utility";
                case "ironforged", "warden_survivor", "raid_breaker" -> "awakening_system.title.bonus.defense";
                case "diamond_blessed", "village_hero", "master_fisher" -> "awakening_system.title.bonus.luck";
                case "nether_walker", "ender_walker", "sea_explorer" -> "awakening_system.title.bonus.explorer";
                default -> "awakening_system.title.bonus.generic";
            };
        }
        private static SkillDefinitions.Rarity defaultRarity(String id) {
            if (id.startsWith("unique_")) return SkillDefinitions.Rarity.UNIQUE;
            return switch (id) {
                case "rank_f", "rank_e", "rank_d", "rank_c" -> SkillDefinitions.Rarity.COMMON;
                case "rank_b", "rank_a", "rank_s" -> SkillDefinitions.Rarity.UNCOMMON;
                case "rank_ex", "rank_ss", "rank_sss", "rank_ur", "rank_ur_plus" -> SkillDefinitions.Rarity.RARE;
                case "rank_lr", "rank_slr", "rank_mr", "rank_gr" -> SkillDefinitions.Rarity.EPIC;
                case "rank_demigod", "rank_god", "rank_deity", "rank_supreme_being", "rank_transcendental" -> SkillDefinitions.Rarity.LEGENDARY;
                case "rank_absolute", "rank_ancestral", "rank_primordial", "rank_cosmic", "rank_multiversal", "rank_omniversal", "rank_infinite", "rank_omnipotent", "rank_national", "rank_monarch", "rank_transcendent", "rank_ruler", "rank_absolute_creator" -> SkillDefinitions.Rarity.MYTHIC;
                default -> {
                    if (id.contains("conqueror") || id.contains("breaker") || id.contains("slayer")) yield SkillDefinitions.Rarity.LEGENDARY;
                    if (id.contains("champion") || id.contains("hero") || id.equals("evolved")) yield SkillDefinitions.Rarity.EPIC;
                    if (id.contains("hunter") || id.contains("explorer") || id.equals("awakened")) yield SkillDefinitions.Rarity.RARE;
                    yield SkillDefinitions.Rarity.COMMON;
                }
            };
        }
        public Definition(String id, String translationKey, String requiredMod, SkillDefinitions.Rarity rarity, String bonusKey) {
            this(id, translationKey, requiredMod, rarity, bonusKey, "awakening_system.title.description." + id);
        }
    }

    private static final List<Definition> TITLES = List.of(
            new Definition("awakening", "awakening_system.title.awakening", ""),
            new Definition("awakened", "awakening_system.title.awakened", ""),
            new Definition("evolved", "awakening_system.title.evolved", ""),
            new Definition("rank_f", "awakening_system.title.rank_f", ""),
            new Definition("rank_e", "awakening_system.title.rank_e", ""),
            new Definition("rank_d", "awakening_system.title.rank_d", ""),
            new Definition("rank_c", "awakening_system.title.rank_c", ""),
            new Definition("rank_b", "awakening_system.title.rank_b", ""),
            new Definition("rank_a", "awakening_system.title.rank_a", ""),
            new Definition("rank_s", "awakening_system.title.rank_s", ""),
            new Definition("rank_ex", "awakening_system.title.rank_ex", ""),
            new Definition("rank_ss", "awakening_system.title.rank_ss", ""),
            new Definition("rank_sss", "awakening_system.title.rank_sss", ""),
            new Definition("rank_ur", "awakening_system.title.rank_ur", ""),
            new Definition("rank_ur_plus", "awakening_system.title.rank_ur_plus", ""),
            new Definition("rank_lr", "awakening_system.title.rank_lr", ""),
            new Definition("rank_slr", "awakening_system.title.rank_slr", ""),
            new Definition("rank_mr", "awakening_system.title.rank_mr", ""),
            new Definition("rank_gr", "awakening_system.title.rank_gr", ""),
            new Definition("rank_demigod", "awakening_system.title.rank_demigod", ""),
            new Definition("rank_god", "awakening_system.title.rank_god", ""),
            new Definition("rank_deity", "awakening_system.title.rank_deity", ""),
            new Definition("rank_supreme_being", "awakening_system.title.rank_supreme_being", ""),
            new Definition("rank_transcendental", "awakening_system.title.rank_transcendental", ""),
            new Definition("rank_absolute_being", "awakening_system.title.rank_absolute_being", ""),
            new Definition("rank_ancestral", "awakening_system.title.rank_ancestral", ""),
            new Definition("rank_primordial", "awakening_system.title.rank_primordial", ""),
            new Definition("rank_cosmic", "awakening_system.title.rank_cosmic", ""),
            new Definition("rank_multiversal", "awakening_system.title.rank_multiversal", ""),
            new Definition("rank_omniversal", "awakening_system.title.rank_omniversal", ""),
            new Definition("rank_infinite", "awakening_system.title.rank_infinite", ""),
            new Definition("rank_omnipotent", "awakening_system.title.rank_omnipotent", ""),
            new Definition("rank_national", "awakening_system.title.rank_national", ""),
            new Definition("rank_monarch", "awakening_system.title.rank_monarch", ""),
            new Definition("rank_transcendent", "awakening_system.title.rank_transcendent", ""),
            new Definition("rank_ruler", "awakening_system.title.rank_ruler", ""),
            new Definition("rank_absolute", "awakening_system.title.rank_absolute", ""),
            new Definition("rank_absolute_creator", "awakening_system.title.rank_absolute_creator", ""),
            new Definition("rank_supreme_creator", "awakening_system.title.rank_supreme_creator", ""),
            new Definition("rank_chaos_sovereign", "awakening_system.title.rank_chaos_sovereign", ""),
            new Definition("rank_timeless_monarch", "awakening_system.title.rank_timeless_monarch", ""),
            new Definition("rank_astral_deity", "awakening_system.title.rank_astral_deity", ""),
            new Definition("rank_singularity", "awakening_system.title.rank_singularity", ""),
            new Definition("rank_limitless", "awakening_system.title.rank_limitless", ""),
            new Definition("rank_absolute_god", "awakening_system.title.rank_absolute_god", ""),

            new Definition("unique_shadow_monarch", "awakening_system.title.unique_shadow_monarch", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.unique"),
            new Definition("unique_demon_king_salvation", "awakening_system.title.unique_demon_king_salvation", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.unique"),
            new Definition("unique_humanitys_strongest", "awakening_system.title.unique_humanitys_strongest", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.unique"),
            new Definition("unique_dragon_emperor", "awakening_system.title.unique_dragon_emperor", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.unique"),
            new Definition("ranker_of_the_tower", "awakening_system.title.ranker_of_the_tower", "", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.ranker"),
            new Definition("death_king", "awakening_system.title.death_king", "", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.defense"),
            new Definition("stone_breaker", "awakening_system.title.stone_breaker", ""),
            new Definition("deep_delver", "awakening_system.title.deep_delver", ""),
            new Definition("ironforged", "awakening_system.title.ironforged", ""),
            new Definition("diamond_blessed", "awakening_system.title.diamond_blessed", ""),
            new Definition("nether_walker", "awakening_system.title.nether_walker", ""),
            new Definition("ender_walker", "awakening_system.title.ender_walker", ""),
            new Definition("dragon_slayer", "awakening_system.title.dragon_slayer", ""),
            new Definition("village_hero", "awakening_system.title.village_hero", ""),
            new Definition("sea_explorer", "awakening_system.title.sea_explorer", ""),
            new Definition("master_fisher", "awakening_system.title.master_fisher", ""),
            new Definition("beast_hunter", "awakening_system.title.beast_hunter", ""),
            new Definition("raid_breaker", "awakening_system.title.raid_breaker", ""),
            new Definition("ancient_hunter", "awakening_system.title.ancient_hunter", ""),
            new Definition("warden_survivor", "awakening_system.title.warden_survivor", ""),
            new Definition("librarian", "awakening_system.title.librarian", ""),
            new Definition("master_builder", "awakening_system.title.master_builder", ""),
            new Definition("netherite_ascendant", "awakening_system.title.netherite_ascendant", ""),
            new Definition("ender_slayer", "awakening_system.title.ender_slayer", "alexsmobs"),
            new Definition("cataclysm_breaker", "awakening_system.title.cataclysm_breaker", "cataclysm"),
            new Definition("twilight_champion", "awakening_system.title.twilight_champion", "twilightforest"),
            new Definition("mowzie_hunter", "awakening_system.title.mowzie_hunter", "mowziesmobs"),
            new Definition("cataclysm_conqueror", "awakening_system.title.cataclysm_conqueror", "cataclysm"),
            new Definition("mass_destruction", "awakening_system.title.mass_destruction", "bosses_of_mass_destruction"),
            new Definition("iceborn", "awakening_system.title.iceborn", "iceandfire"),
            new Definition("mutant_slayer", "awakening_system.title.mutant_slayer", "mutantmonsters"),
            new Definition("deep_dark_champion", "awakening_system.title.deep_dark_champion", "deeperdarker"),
            new Definition("aether_hero", "awakening_system.title.aether_hero", "aether"),
            new Definition("born_in_chaos_hunter", "awakening_system.title.born_in_chaos_hunter", "born_in_chaos_v1"),
            new Definition("blue_skies_explorer", "awakening_system.title.blue_skies_explorer", "blue_skies"),
            new Definition("undergarden_survivor", "awakening_system.title.undergarden_survivor", "undergarden"),
            new Definition("eclipse_ascendant", "awakening_system.title.eclipse_ascendant", "eclipse_expansion", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.eclipse"),
            new Definition("nine_tribulations_ascendant", "awakening_system.title.nine_tribulations_ascendant", "", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.defense"),
            new Definition("blood_sect_conqueror", "awakening_system.title.blood_sect_conqueror", "", SkillDefinitions.Rarity.LEGENDARY, "awakening_system.title.bonus.attack"),
            new Definition("yggdrasil_guardian", "awakening_system.title.yggdrasil_guardian", "", SkillDefinitions.Rarity.LEGENDARY, "awakening_system.title.bonus.defense"),

            new Definition("create_train_master", "awakening_system.title.create_train_master", "", SkillDefinitions.Rarity.LEGENDARY, "awakening_system.title.bonus.utility"),
            new Definition("create_steam_architect", "awakening_system.title.create_steam_architect", "", SkillDefinitions.Rarity.RARE, "awakening_system.title.bonus.utility"),
            new Definition("create_aeronaut_pioneer", "awakening_system.title.create_aeronaut_pioneer", "", SkillDefinitions.Rarity.RARE, "awakening_system.title.bonus.explorer"),
            new Definition("create_sky_battleship_captain", "awakening_system.title.create_sky_battleship_captain", "", SkillDefinitions.Rarity.LEGENDARY, "awakening_system.title.bonus.attack"),
            new Definition("create_firmament_pilot", "awakening_system.title.create_firmament_pilot", "", SkillDefinitions.Rarity.EPIC, "awakening_system.title.bonus.explorer"),
            new Definition("create_reality_engineer", "awakening_system.title.create_reality_engineer", "", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.utility"),

            new Definition("black_market_dealer", "awakening_system.title.black_market_dealer", "", SkillDefinitions.Rarity.RARE, "awakening_system.title.bonus.luck"),
            new Definition("casino_tycoon", "awakening_system.title.casino_tycoon", "", SkillDefinitions.Rarity.EPIC, "awakening_system.title.bonus.luck"),
            new Definition("pool_shark", "awakening_system.title.pool_shark", "", SkillDefinitions.Rarity.RARE, "awakening_system.title.bonus.utility"),
            new Definition("tracen_champion", "awakening_system.title.tracen_champion", "", SkillDefinitions.Rarity.LEGENDARY, "awakening_system.title.bonus.explorer"),
            new Definition("dao_sage", "awakening_system.title.dao_sage", "", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.attack"),
            new Definition("mod_conqueror", "awakening_system.title.mod_conqueror", "", SkillDefinitions.Rarity.MYTHIC, "awakening_system.title.bonus.unique"),
            new Definition("creator", "awakening_system.title.creator", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.creator", "awakening_system.title.description.creator"),
            new Definition("michi_lover", "awakening_system.title.michi_lover", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.michi_lover", "awakening_system.title.description.michi_lover"),
            new Definition("caffeine_addict", "awakening_system.title.caffeine_addict", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.caffeine_addict", "awakening_system.title.description.caffeine_addict"),
            new Definition("futanari_hentai_master", "awakening_system.title.futanari_hentai_master", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.futanari_hentai_master", "awakening_system.title.description.futanari_hentai_master"),
            new Definition("pokemon_master", "awakening_system.title.pokemon_master", "", SkillDefinitions.Rarity.UNIQUE, "awakening_system.title.bonus.pokemon_master", "awakening_system.title.description.pokemon_master")
    );

    public static List<Definition> availableDefinitions(SystemData data) {
        List<Definition> result = new ArrayList<>();
        for (Definition title : TITLES) {
            if (!title.requiredMod().isEmpty() && !ModList.get().isLoaded(title.requiredMod())) continue;
            if (title.id().equals("creator") && !data.isCreator()) continue;
            if (title.id().equals("michi_lover") && !data.isNanda() && !data.isCreator()) continue;
            if (title.id().equals("caffeine_addict") && !data.isMisticPotato() && !data.isCreator()) continue;
            if (title.id().equals("futanari_hentai_master") && !data.isSchvis() && !data.isCreator()) continue;
            if (title.id().equals("pokemon_master") && !data.isTomcraft() && !data.isCreator()) continue;
            if (title.id().equals("mod_conqueror") && !data.isCreator()
                    && !data.conqueredRankerTower() && !data.conqueredNineTribulationsPagoda()
                    && !data.conqueredBloodSect() && !data.conqueredYggdrasilLabyrinth()
                    && data.bossKills() < 1 && data.level() < 40) {
                continue;
            }
            if (title.id().startsWith("eclipse_")) {
                int required = title.id().contains("ascendant") ? 140 : title.id().contains("herald") ? 100 : 80;
                if (data.level() < required || !data.awakeningComplete()) continue;
            }

            if (title.id().startsWith("unique_") && (!data.awakeningComplete() || data.level() < 50)) continue;
            if (title.id().equals("unique_shadow_monarch") && (!data.skills().containsKey("shadow_monarch") && !data.skills().containsKey("unique_absolute_extraction"))) continue;
            if (title.id().equals("unique_demon_king_salvation") && (!data.skills().containsKey("unique_fourth_wall_infinite") && !data.skills().containsKey("fourth_wall"))) continue;
            if (title.id().equals("unique_dragon_emperor") && (!data.skills().containsKey("unique_dragon_breath_annihilation") && !data.skills().containsKey("dragon_breath"))) continue;
            if (title.id().equals("unique_humanitys_strongest") && data.level() < 100) continue;

            if (title.id().equals("awakening") && !data.awakeningComplete()) continue;
            if (title.id().equals("awakened") && !data.awakeningComplete()) continue;
            if (title.id().equals("evolved") && !data.hasEvolvedSkill()) continue;
            if (title.id().startsWith("rank_")) {
                if (data.isCreator() && (title.id().equals("rank_absolute_creator") || title.id().equals("rank_supreme_creator"))) {
                    // Creador desbloquea sus títulos sin restricción de nivel
                } else {
                    int required = switch (title.id()) {
                        case "rank_f" -> 1; case "rank_e" -> 5; case "rank_d" -> 10; case "rank_c" -> 20; case "rank_b" -> 30; case "rank_a" -> 40; case "rank_s" -> 50;
                        case "rank_ex" -> 70; case "rank_ss" -> 90; case "rank_sss" -> 110; case "rank_ur" -> 130; case "rank_ur_plus" -> 150;
                        case "rank_lr" -> 170; case "rank_slr" -> 190; case "rank_mr" -> 210; case "rank_gr" -> 230; case "rank_demigod" -> 250;
                        case "rank_god" -> 270; case "rank_deity" -> 290; case "rank_supreme_being" -> 310; case "rank_transcendental" -> 330;
                        case "rank_absolute_being" -> 350; case "rank_ancestral" -> 370; case "rank_primordial" -> 390; case "rank_cosmic" -> 410;
                        case "rank_multiversal" -> 430; case "rank_omniversal" -> 450; case "rank_infinite" -> 470; case "rank_omnipotent" -> 490; case "rank_national" -> 510; case "rank_monarch" -> 540; case "rank_transcendent" -> 570; case "rank_ruler" -> 600; case "rank_absolute" -> 630; case "rank_absolute_creator" -> 660;
                        case "rank_supreme_creator" -> 700; case "rank_chaos_sovereign" -> 750; case "rank_timeless_monarch" -> 800; case "rank_astral_deity" -> 850; case "rank_singularity" -> 900; case "rank_limitless" -> 950; case "rank_absolute_god" -> 1000;
                        default -> Integer.MAX_VALUE;
                    };
                    if (data.level() < required) continue;
                }
            }
            if (title.id().equals("death_king") && (!data.skills().containsKey("death_king_rewind") && (data.deaths() < 10 || data.level() < 40))) continue;
            if (title.id().equals("stone_breaker") && data.blocksMined() < 500) continue;
            if (title.id().equals("deep_delver") && data.affinity("depth") < 100) continue;
            if (title.id().equals("ironforged") && data.level() < 5) continue;
            if (title.id().equals("diamond_blessed") && !data.secretMissionComplete()) continue;
            if (title.id().equals("nether_walker") && data.level() < 20) continue;
            if (title.id().equals("ender_walker") && data.level() < 30) continue;
            if (title.id().equals("dragon_slayer") && data.level() < 40) continue;
            if (title.id().equals("village_hero") && data.level() < 10) continue;
            if (title.id().equals("sea_explorer") && data.affinity("depth") < 250) continue;
            if (title.id().equals("master_fisher") && data.affinity("nature") < 250) continue;
            if (title.id().equals("beast_hunter") && data.affinity("combat") < 250) continue;
            if (title.id().equals("raid_breaker") && data.level() < 30) continue;
            if (title.id().equals("ancient_hunter") && data.level() < 50) continue;
            if (title.id().equals("warden_survivor") && data.affinity("survival") < 250) continue;
            if (title.id().equals("librarian") && data.affinity("crafting") < 100) continue;
            if (title.id().equals("master_builder") && data.affinity("crafting") < 250) continue;
            if (title.id().equals("netherite_ascendant") && data.level() < 60) continue;
            if (title.id().equals("ranker_of_the_tower") && !data.conqueredRankerTower()) continue;
            if (title.id().equals("nine_tribulations_ascendant") && !data.conqueredNineTribulationsPagoda()) continue;
            if (title.id().equals("blood_sect_conqueror") && !data.conqueredBloodSect()) continue;
            if (title.id().equals("yggdrasil_guardian") && !data.conqueredYggdrasilLabyrinth()) continue;

            if (title.id().equals("ender_slayer") && (data.creaturesDefeated() < 100 || data.level() < 25)) continue;
            if (title.id().equals("cataclysm_breaker") && (data.bossKills() < 1 || data.level() < 40)) continue;
            if (title.id().equals("twilight_champion") && (data.bossKills() < 1 || data.level() < 25)) continue;
            if (title.id().equals("mowzie_hunter") && (data.bossKills() < 1 || data.level() < 20)) continue;
            if (title.id().equals("cataclysm_conqueror") && (data.bossKills() < 3 || data.level() < 60)) continue;
            if (title.id().equals("mass_destruction") && (data.bossKills() < 1 || data.level() < 35)) continue;
            if (title.id().equals("iceborn") && data.level() < 30) continue;
            if (title.id().equals("mutant_slayer") && (data.creaturesDefeated() < 50 || data.level() < 20)) continue;
            if (title.id().equals("deep_dark_champion") && data.level() < 45) continue;
            if (title.id().equals("aether_hero") && data.level() < 25) continue;
            if (title.id().equals("born_in_chaos_hunter") && data.level() < 25) continue;
            if (title.id().equals("blue_skies_explorer") && data.level() < 20) continue;
            if (title.id().equals("undergarden_survivor") && data.level() < 25) continue;

            if (title.id().equals("create_train_master") && !data.hasFable("first_create_train") && !data.hasFable("steam_colossus")) continue;
            if (title.id().equals("create_steam_architect") && !data.hasFable("steam_colossus") && data.itemsCrafted() < 50) continue;
            if (title.id().equals("create_aeronaut_pioneer") && !data.hasFable("first_create_aircraft") && !data.hasFable("sky_dreadnought_commander")) continue;
            if (title.id().equals("create_sky_battleship_captain") && !data.hasFable("sky_dreadnought_commander")) continue;
            if (title.id().equals("create_firmament_pilot") && !data.hasFable("first_create_aircraft") && !data.hasFable("sky_dreadnought_commander")) continue;
            if (title.id().equals("create_reality_engineer") && (!data.hasFable("first_create_train") || !data.hasFable("first_create_aircraft"))) continue;

            if (title.id().equals("black_market_dealer") && data.level() < 15) continue;
            if (title.id().equals("casino_tycoon") && data.coins() < 5000 && data.coinsSpent() < 2000) continue;
            if (title.id().equals("pool_shark") && data.level() < 10) continue;
            if (title.id().equals("tracen_champion") && data.level() < 25) continue;
            if (title.id().equals("dao_sage") && data.dantianRealm() < 3) continue;

            result.add(title);
        }
        return result;
    }

    public static boolean isAvailable(String titleId) {
        return TITLES.stream().anyMatch(title -> title.id().equals(titleId)
                && (title.requiredMod().isEmpty() || ModList.get().isLoaded(title.requiredMod())));
    }

    public static List<String> available(SystemData data) {
        return availableDefinitions(data).stream().map(Definition::id).toList();
    }

    public static final Definition NONE = new Definition(
            "none",
            "awakening_system.title.none",
            "",
            SkillDefinitions.Rarity.COMMON,
            "awakening_system.title.bonus.none",
            "awakening_system.title.description.none"
    );

    public static Definition get(String id) {
        if (id == null || id.isEmpty() || "none".equalsIgnoreCase(id)) {
            return NONE;
        }
        return TITLES.stream().filter(title -> title.id().equals(id)).findFirst().orElse(NONE);
    }
}
