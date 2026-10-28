package com.soybe.awakeningsystem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;

public final class ShopDefinitions {
    public record Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod, int requiredSystemTier, String requiredDimension, int minLevel) {
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod, int requiredSystemTier, String requiredDimension) {
            this(id, itemId, cost, amount, nameKey, category, requiredMod, requiredSystemTier, requiredDimension, 0);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod, int requiredSystemTier) {
            this(id, itemId, cost, amount, nameKey, category, requiredMod, requiredSystemTier, "", 0);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod) {
            this(id, itemId, cost, amount, nameKey, category, requiredMod, 1, "", 0);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category) {
            this(id, itemId, cost, amount, nameKey, category, "", 1, "", 0);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, int requiredSystemTier) {
            this(id, itemId, cost, amount, nameKey, category, "", requiredSystemTier, "", 0);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod, String requiredDimension) {
            this(id, itemId, cost, amount, nameKey, category, requiredMod, 1, requiredDimension, 0);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, int requiredSystemTier, int minLevel) {
            this(id, itemId, cost, amount, nameKey, category, "", requiredSystemTier, "", minLevel);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod, int requiredSystemTier, int minLevel) {
            this(id, itemId, cost, amount, nameKey, category, requiredMod, requiredSystemTier, "", minLevel);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, int requiredSystemTier, String requiredDimension, int minLevel) {
            this(id, itemId, cost, amount, nameKey, category, "", requiredSystemTier, requiredDimension, minLevel);
        }
    }

    private static final List<Entry> ENTRIES = List.of(
            // --- BOXES & TICKETS ---
            new Entry("box_common", "minecraft:chest", 350, 1, "awakening_system.shop.item.box_common", "boxes", "", 1, "", 10),
            new Entry("lucky_box", "minecraft:bundle", 850, 1, "awakening_system.shop.item.lucky_box", "boxes", "", 1, "", 20),
            new Entry("box_rare", "minecraft:trapped_chest", 2200, 1, "awakening_system.shop.item.box_rare", "boxes", "", 1, "", 35),
            new Entry("box_epic", "minecraft:ender_chest", 6500, 1, "awakening_system.shop.item.box_epic", "boxes", "", 1, "", 55),
            new Entry("box_legendary", "minecraft:shulker_box", 20000, 1, "awakening_system.shop.item.box_legendary", "boxes", "", 1, "", 75),
            new Entry("plushies_lootbox", "awakening_system:plushieslootbox", 5000, 1, "awakening_system.shop.item.plushieslootbox", "boxes"),
            new Entry("roulette_token", "awakening_system:roulette_token", 3500, 1, "item.awakening_system.roulette_token", "boxes", 1, 20),
            new Entry("dungeon_key", "awakening_system:dungeon_key", 450, 1, "awakening_system.shop.item.dungeon_key", "boxes"),
            new Entry("overworld_dungeon_key", "awakening_system:overworld_dungeon_key", 500, 1, "item.awakening_system.overworld_dungeon_key", "boxes"),
            new Entry("nether_flame_key", "awakening_system:nether_flame_key", 750, 1, "item.awakening_system.nether_flame_key", "boxes", "", 1, "minecraft:the_nether"),
            new Entry("end_void_key", "awakening_system:end_void_key", 1200, 1, "item.awakening_system.end_void_key", "boxes", "", 2, "minecraft:the_end"),
            new Entry("teyvat_shrine_key", "awakening_system:teyvat_shrine_key", 450, 1, "item.awakening_system.teyvat_shrine_key", "boxes", "", 3),
            new Entry("isekai_mana_key", "awakening_system:isekai_mana_key", 400, 1, "item.awakening_system.isekai_mana_key", "boxes", "", 2),
            new Entry("murim_dao_key", "awakening_system:murim_dao_key", 450, 1, "item.awakening_system.murim_dao_key", "boxes", "", 3),
            new Entry("ranker_tower_ticket", "awakening_system:ranker_tower_ticket", 1500, 1, "item.awakening_system.ranker_tower_ticket", "boxes"),
            new Entry("ranker_return_scroll", "awakening_system:ranker_return_scroll", 800, 1, "item.awakening_system.ranker_return_scroll", "boxes"),
            new Entry("roulette_altar", "awakening_system:roulette_altar", 30000, 1, "block.awakening_system.roulette_altar", "equipment", 1, 25),
            new Entry("death_king_clock", "awakening_system:death_king_clock", 2800, 1, "awakening_system.shop.item.death_king_clock", "equipment", 2, 30),

            // --- HORNOS POR DIMENSION (Mundo 1 a Mundo 4) ---
            new Entry("abyssal_furnace", "awakening_system:abyssal_furnace", 45000, 1, "block.awakening_system.abyssal_furnace", "utility", 1, "minecraft:the_nether", 100),
            new Entry("soul_alloy_furnace", "awakening_system:soul_alloy_furnace", 60000, 1, "block.awakening_system.soul_alloy_furnace", "utility", 1, "minecraft:the_nether", 115),
            new Entry("astralite_furnace", "awakening_system:astralite_furnace", 75000, 1, "block.awakening_system.astralite_furnace", "utility", 1, "minecraft:the_end", 125),
            new Entry("mithril_furnace", "awakening_system:mithril_furnace", 85000, 1, "block.awakening_system.mithril_furnace", "utility", 2, "awakening_system:isekai", 135),
            new Entry("adamantite_furnace", "awakening_system:adamantite_furnace", 150000, 1, "block.awakening_system.adamantite_furnace", "utility", 2, "awakening_system:isekai", 175),
            new Entry("spirit_jade_furnace", "awakening_system:spirit_jade_furnace", 260000, 1, "block.awakening_system.spirit_jade_furnace", "utility", 3, "awakening_system:murim", 215),
            new Entry("astral_furnace", "awakening_system:astral_furnace", 450000, 1, "block.awakening_system.astral_furnace", "utility", 3, "awakening_system:murim", 255),
            new Entry("celestial_furnace", "awakening_system:celestial_furnace", 750000, 1, "block.awakening_system.celestial_furnace", "utility", 4, "awakening_system:teyvat_world", 295),
            new Entry("primordial_furnace", "awakening_system:primordial_furnace", 1250000, 1, "block.awakening_system.primordial_furnace", "utility", 4, "awakening_system:teyvat_world", 340),

            // --- MEJORAS DE EQUIPO POR DIMENSION ---
            new Entry("upgrade_netherite_to_abyssal", "awakening_system:upgrade_netherite_to_abyssal", 35000, 1, "item.awakening_system.upgrade_netherite_to_abyssal", "utility", 1, "minecraft:the_nether", 100),
            new Entry("upgrade_abyssal_to_soul_alloy", "awakening_system:upgrade_abyssal_to_soul_alloy", 48000, 1, "item.awakening_system.upgrade_abyssal_to_soul_alloy", "utility", 1, "minecraft:the_nether", 115),
            new Entry("upgrade_soul_alloy_to_astralite", "awakening_system:upgrade_soul_alloy_to_astralite", 60000, 1, "item.awakening_system.upgrade_soul_alloy_to_astralite", "utility", 1, "minecraft:the_end", 125),
            new Entry("upgrade_netherite_to_mithril", "awakening_system:upgrade_netherite_to_mithril", 65000, 1, "item.awakening_system.upgrade_netherite_to_mithril", "utility", 2, "awakening_system:isekai", 135),
            new Entry("upgrade_mithril_to_adamantite", "awakening_system:upgrade_mithril_to_adamantite", 110000, 1, "item.awakening_system.upgrade_mithril_to_adamantite", "utility", 2, "awakening_system:isekai", 175),
            new Entry("upgrade_adamantite_to_spirit_jade", "awakening_system:upgrade_adamantite_to_spirit_jade", 180000, 1, "item.awakening_system.upgrade_adamantite_to_spirit_jade", "utility", 3, "awakening_system:murim", 215),
            new Entry("upgrade_spirit_jade_to_astral", "awakening_system:upgrade_spirit_jade_to_astral", 290000, 1, "item.awakening_system.upgrade_spirit_jade_to_astral", "utility", 3, "awakening_system:murim", 255),
            new Entry("upgrade_astral_to_celestial", "awakening_system:upgrade_astral_to_celestial", 480000, 1, "item.awakening_system.upgrade_astral_to_celestial", "utility", 4, "awakening_system:teyvat_world", 295),
            new Entry("upgrade_celestial_to_primordial", "awakening_system:upgrade_celestial_to_primordial", 850000, 1, "item.awakening_system.upgrade_celestial_to_primordial", "utility", 4, "awakening_system:teyvat_world", 340),

            // --- BOLSAS DIMENSIONALES & RELIQUIAS ---
            new Entry("wanderer_pouch", "awakening_system:wanderer_pouch", 15000, 1, "item.awakening_system.wanderer_pouch", "relics", 1, 20),
            new Entry("adventurer_pouch", "awakening_system:adventurer_pouch", 45000, 1, "item.awakening_system.adventurer_pouch", "relics", 2, 40),
            new Entry("outlander_pouch", "awakening_system:outlander_pouch", 120000, 1, "item.awakening_system.outlander_pouch", "relics", 3, 70),
            new Entry("emergency_pouch", "awakening_system:emergency_pouch", 350000, 1, "item.awakening_system.emergency_pouch", "relics", 4, 100),
            new Entry("omniscience_pouch", "awakening_system:omniscience_pouch", 250000, 1, "item.awakening_system.omniscience_pouch", "relics", 3, 90),

            new Entry("gift_card_5", "awakening_system:gift_card_5", 25000, 1, "item.awakening_system.gift_card_5", "boxes", 4),
            new Entry("gift_card_15", "awakening_system:gift_card_15", 75000, 1, "item.awakening_system.gift_card_15", "boxes", 4),
            new Entry("gift_card_30", "awakening_system:gift_card_30", 150000, 1, "item.awakening_system.gift_card_30", "boxes", 4),
            new Entry("gift_card_50", "awakening_system:gift_card_50", 250000, 1, "item.awakening_system.gift_card_50", "boxes", 4),
            new Entry("gift_card_100", "awakening_system:gift_card_100", 500000, 1, "item.awakening_system.gift_card_100", "boxes", 4),

            new Entry("crystal_core", "awakening_system:crystal_core", 800, 1, "item.awakening_system.crystal_core", "relics", "", 3, "awakening_system:teyvat"),
            new Entry("original_resin", "awakening_system:original_resin", 1800, 1, "awakening_system.shop.item.original_resin", "consumables", "", 3, "awakening_system:teyvat"),
            new Entry("condensed_resin", "awakening_system:condensed_resin", 4500, 1, "awakening_system.shop.item.condensed_resin", "consumables", "", 3, "awakening_system:teyvat"),
            new Entry("geosigil", "awakening_system:geosigil", 150, 1, "item.awakening_system.geosigil", "relics", 4),
            new Entry("mora_pouch", "awakening_system:mora_pouch", 15000, 1, "item.awakening_system.mora_pouch", "boxes", "", 3, "awakening_system:teyvat"),
            new Entry("hunter_bestiary", "awakening_system:hunter_bestiary", 150, 1, "item.awakening_system.hunter_bestiary", "utility"),

            // --- ARMAS & PROGRESIÓN (CAPADAS POR NIVEL) ---
            new Entry("iron_sword", "minecraft:iron_sword", 75, 1, "awakening_system.shop.item.iron_sword", "weapons", 1, 10),
            new Entry("diamond_sword", "minecraft:diamond_sword", 8500, 1, "awakening_system.shop.item.diamond_sword", "weapons", 1, 45),
            new Entry("abyssal_sword", "awakening_system:abyssal_sword", 26000, 1, "item.awakening_system.abyssal_sword", "weapons", 1, 60),
            new Entry("netherite_sword", "minecraft:netherite_sword", 55000, 1, "awakening_system.shop.item.netherite_sword", "weapons", "", 1, "minecraft:the_nether", 75),
            new Entry("mace", "minecraft:mace", 45000, 1, "awakening_system.shop.item.mace", "weapons", 1, 55),
            new Entry("trident", "minecraft:trident", 32000, 1, "awakening_system.shop.item.trident", "weapons", 1, 50),
            new Entry("bow", "minecraft:bow", 70, 1, "awakening_system.shop.item.bow", "weapons", 1, 10),
            new Entry("crossbow", "minecraft:crossbow", 110, 1, "awakening_system.shop.item.crossbow", "weapons", 1, 10),
            new Entry("shield", "minecraft:shield", 60, 1, "awakening_system.shop.item.shield", "equipment", 1, 10),

            // --- ESPADAS PROPIAS POR DIMENSION ---
            new Entry("mithril_sword", "awakening_system:mithril_sword", 145000, 1, "item.awakening_system.mithril_sword", "weapons", 2, "awakening_system:isekai", 135),
            new Entry("adamantite_sword", "awakening_system:adamantite_sword", 220000, 1, "item.awakening_system.adamantite_sword", "weapons", 2, "awakening_system:isekai", 175),
            new Entry("spirit_jade_sword", "awakening_system:spirit_jade_sword", 320000, 1, "item.awakening_system.spirit_jade_sword", "weapons", 3, "awakening_system:murim", 215),
            new Entry("astral_sword", "awakening_system:astral_sword", 480000, 1, "item.awakening_system.astral_sword", "weapons", 3, "awakening_system:murim", 255),
            new Entry("celestial_sword", "awakening_system:celestial_sword", 750000, 1, "item.awakening_system.celestial_sword", "weapons", 4, "awakening_system:teyvat_world", 295),
            new Entry("primordial_sword", "awakening_system:primordial_sword", 1200000, 1, "item.awakening_system.primordial_sword", "weapons", 4, "awakening_system:teyvat_world", 340),

            // --- ARMAS DE FANTASÍA Y LEYENDA (CAPADAS POR NIVEL Y DIMENSION) ---
            new Entry("plum_blossom_sword", "awakening_system:plum_blossom_sword", 68000, 1, "item.awakening_system.plum_blossom_sword", "weapons", 1, "awakening_system:murim", 90),
            new Entry("gigantic_greatsword", "awakening_system:gigantic_greatsword", 75000, 1, "item.awakening_system.gigantic_greatsword", "weapons", 1, "minecraft:the_nether", 80),
            new Entry("transcendent_thorn", "awakening_system:transcendent_thorn", 82000, 1, "item.awakening_system.transcendent_thorn", "weapons", 1, "minecraft:the_nether", 95),
            new Entry("dawns_ballad", "awakening_system:dawns_ballad", 88000, 1, "item.awakening_system.dawns_ballad", "weapons", 1, "awakening_system:isekai", 110),
            new Entry("radiant_light_sword", "awakening_system:radiant_light_sword", 84000, 1, "item.awakening_system.radiant_light_sword", "weapons", 1, "awakening_system:isekai", 125),
            new Entry("shadow_daggers", "awakening_system:shadow_daggers", 98000, 1, "item.awakening_system.shadow_daggers", "weapons", 1, "awakening_system:isekai", 140),
            new Entry("unbreakable_faith", "awakening_system:unbreakable_faith", 86000, 1, "item.awakening_system.unbreakable_faith", "weapons", 1, "awakening_system:isekai", 155),
            new Entry("asrahan_holy_sword", "awakening_system:asrahan_holy_sword", 105000, 1, "item.awakening_system.asrahan_holy_sword", "weapons", 1, "awakening_system:isekai", 170),
            new Entry("sky_demon_sword", "awakening_system:sky_demon_sword", 160000, 1, "item.awakening_system.sky_demon_sword", "weapons", 2, "awakening_system:murim", 225),
            new Entry("heaven_shaking_sword", "awakening_system:heaven_shaking_sword", 195000, 1, "item.awakening_system.heaven_shaking_sword", "weapons", 3, "awakening_system:murim", 265),
            new Entry("black_dragon_scythe", "awakening_system:black_dragon_scythe", 260000, 1, "item.awakening_system.black_dragon_scythe", "weapons", 3, "awakening_system:murim", 310),

            // --- ARMADURAS VANILLA (CAPADAS POR NIVEL) ---
            new Entry("leather_helmet", "minecraft:leather_helmet", 30, 1, "awakening_system.shop.item.leather_helmet", "armor"),
            new Entry("leather_chestplate", "minecraft:leather_chestplate", 55, 1, "awakening_system.shop.item.leather_chestplate", "armor"),
            new Entry("leather_leggings", "minecraft:leather_leggings", 45, 1, "awakening_system.shop.item.leather_leggings", "armor"),
            new Entry("leather_boots", "minecraft:leather_boots", 25, 1, "awakening_system.shop.item.leather_boots", "armor"),
            new Entry("chainmail_helmet", "minecraft:chainmail_helmet", 50, 1, "awakening_system.shop.item.chainmail_helmet", "armor", 1, 5),
            new Entry("chainmail_chestplate", "minecraft:chainmail_chestplate", 100, 1, "awakening_system.shop.item.chainmail_chestplate", "armor", 1, 5),
            new Entry("chainmail_leggings", "minecraft:chainmail_leggings", 80, 1, "awakening_system.shop.item.chainmail_leggings", "armor", 1, 5),
            new Entry("chainmail_boots", "minecraft:chainmail_boots", 45, 1, "awakening_system.shop.item.chainmail_boots", "armor", 1, 5),
            new Entry("iron_helmet", "minecraft:iron_helmet", 150, 1, "awakening_system.shop.item.iron_helmet", "armor", 1, 10),
            new Entry("iron_chestplate", "minecraft:iron_chestplate", 300, 1, "awakening_system.shop.item.iron_chestplate", "armor", 1, 10),
            new Entry("iron_leggings", "minecraft:iron_leggings", 240, 1, "awakening_system.shop.item.iron_leggings", "armor", 1, 10),
            new Entry("iron_boots", "minecraft:iron_boots", 140, 1, "awakening_system.shop.item.iron_boots", "armor", 1, 10),
            new Entry("golden_helmet", "minecraft:golden_helmet", 80, 1, "awakening_system.shop.item.golden_helmet", "armor", 1, 15),
            new Entry("golden_chestplate", "minecraft:golden_chestplate", 150, 1, "awakening_system.shop.item.golden_chestplate", "armor", 1, 15),
            new Entry("golden_leggings", "minecraft:golden_leggings", 120, 1, "awakening_system.shop.item.golden_leggings", "armor", 1, 15),
            new Entry("golden_boots", "minecraft:golden_boots", 70, 1, "awakening_system.shop.item.golden_boots", "armor", 1, 15),
            new Entry("diamond_helmet", "minecraft:diamond_helmet", 14000, 1, "awakening_system.shop.item.diamond_helmet", "armor", 1, 45),
            new Entry("diamond_chestplate", "minecraft:diamond_chestplate", 24000, 1, "awakening_system.shop.item.diamond_chestplate", "armor", 1, 45),
            new Entry("diamond_leggings", "minecraft:diamond_leggings", 19000, 1, "awakening_system.shop.item.diamond_leggings", "armor", 1, 45),
            new Entry("diamond_boots", "minecraft:diamond_boots", 12000, 1, "awakening_system.shop.item.diamond_boots", "armor", 1, 45),

            // --- ARMADURA ABISAL (NIVEL INTERMEDIO DIAMANTE -> NETHERITA) ---
            new Entry("abyssal_helmet", "awakening_system:abyssal_helmet", 28000, 1, "item.awakening_system.abyssal_helmet", "armor", 1, 60),
            new Entry("abyssal_chestplate", "awakening_system:abyssal_chestplate", 45000, 1, "item.awakening_system.abyssal_chestplate", "armor", 1, 60),
            new Entry("abyssal_leggings", "awakening_system:abyssal_leggings", 36000, 1, "item.awakening_system.abyssal_leggings", "armor", 1, 60),
            new Entry("abyssal_boots", "awakening_system:abyssal_boots", 24000, 1, "item.awakening_system.abyssal_boots", "armor", 1, 60),

            // --- ARMADURA NETHERITA ---
            new Entry("netherite_helmet", "minecraft:netherite_helmet", 48000, 1, "awakening_system.shop.item.netherite_helmet", "armor", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_chestplate", "minecraft:netherite_chestplate", 75000, 1, "awakening_system.shop.item.netherite_chestplate", "armor", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_leggings", "minecraft:netherite_leggings", 62000, 1, "awakening_system.shop.item.netherite_leggings", "armor", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_boots", "minecraft:netherite_boots", 45000, 1, "awakening_system.shop.item.netherite_boots", "armor", "", 1, "minecraft:the_nether", 75),

            // --- ARMADURA DE ALEACION DE ALMAS (NETHER HIGH-TIER) ---
            new Entry("soul_alloy_helmet", "awakening_system:soul_alloy_helmet", 80000, 1, "item.awakening_system.soul_alloy_helmet", "armor", 1, "minecraft:the_nether", 95),
            new Entry("soul_alloy_chestplate", "awakening_system:soul_alloy_chestplate", 125000, 1, "item.awakening_system.soul_alloy_chestplate", "armor", 1, "minecraft:the_nether", 95),
            new Entry("soul_alloy_leggings", "awakening_system:soul_alloy_leggings", 105000, 1, "item.awakening_system.soul_alloy_leggings", "armor", 1, "minecraft:the_nether", 95),
            new Entry("soul_alloy_boots", "awakening_system:soul_alloy_boots", 75000, 1, "item.awakening_system.soul_alloy_boots", "armor", 1, "minecraft:the_nether", 95),

            // --- ARMADURA DE ASTRALITA (THE END HIGH-TIER) ---
            new Entry("astralite_helmet", "awakening_system:astralite_helmet", 120000, 1, "item.awakening_system.astralite_helmet", "armor", 1, "minecraft:the_end", 115),
            new Entry("astralite_chestplate", "awakening_system:astralite_chestplate", 180000, 1, "item.awakening_system.astralite_chestplate", "armor", 1, "minecraft:the_end", 115),
            new Entry("astralite_leggings", "awakening_system:astralite_leggings", 150000, 1, "item.awakening_system.astralite_leggings", "armor", 1, "minecraft:the_end", 115),
            new Entry("astralite_boots", "awakening_system:astralite_boots", 110000, 1, "item.awakening_system.astralite_boots", "armor", 1, "minecraft:the_end", 115),

            // --- ARMADURAS PROPIAS POR DIMENSION ---
            new Entry("mithril_helmet", "awakening_system:mithril_helmet", 160000, 1, "item.awakening_system.mithril_helmet", "armor", 2, "awakening_system:isekai", 135),
            new Entry("mithril_chestplate", "awakening_system:mithril_chestplate", 240000, 1, "item.awakening_system.mithril_chestplate", "armor", 2, "awakening_system:isekai", 135),
            new Entry("mithril_leggings", "awakening_system:mithril_leggings", 200000, 1, "item.awakening_system.mithril_leggings", "armor", 2, "awakening_system:isekai", 135),
            new Entry("mithril_boots", "awakening_system:mithril_boots", 150000, 1, "item.awakening_system.mithril_boots", "armor", 2, "awakening_system:isekai", 135),

            new Entry("adamantite_helmet", "awakening_system:adamantite_helmet", 280000, 1, "item.awakening_system.adamantite_helmet", "armor", 2, "awakening_system:isekai", 175),
            new Entry("adamantite_chestplate", "awakening_system:adamantite_chestplate", 420000, 1, "item.awakening_system.adamantite_chestplate", "armor", 2, "awakening_system:isekai", 175),
            new Entry("adamantite_leggings", "awakening_system:adamantite_leggings", 350000, 1, "item.awakening_system.adamantite_leggings", "armor", 2, "awakening_system:isekai", 175),
            new Entry("adamantite_boots", "awakening_system:adamantite_boots", 260000, 1, "item.awakening_system.adamantite_boots", "armor", 2, "awakening_system:isekai", 175),

            new Entry("spirit_jade_helmet", "awakening_system:spirit_jade_helmet", 420000, 1, "item.awakening_system.spirit_jade_helmet", "armor", 3, "awakening_system:murim", 215),
            new Entry("spirit_jade_chestplate", "awakening_system:spirit_jade_chestplate", 620000, 1, "item.awakening_system.spirit_jade_chestplate", "armor", 3, "awakening_system:murim", 215),
            new Entry("spirit_jade_leggings", "awakening_system:spirit_jade_leggings", 520000, 1, "item.awakening_system.spirit_jade_leggings", "armor", 3, "awakening_system:murim", 215),
            new Entry("spirit_jade_boots", "awakening_system:spirit_jade_boots", 390000, 1, "item.awakening_system.spirit_jade_boots", "armor", 3, "awakening_system:murim", 215),

            new Entry("astral_helmet", "awakening_system:astral_helmet", 650000, 1, "item.awakening_system.astral_helmet", "armor", 3, "awakening_system:murim", 255),
            new Entry("astral_chestplate", "awakening_system:astral_chestplate", 950000, 1, "item.awakening_system.astral_chestplate", "armor", 3, "awakening_system:murim", 255),
            new Entry("astral_leggings", "awakening_system:astral_leggings", 800000, 1, "item.awakening_system.astral_leggings", "armor", 3, "awakening_system:murim", 255),
            new Entry("astral_boots", "awakening_system:astral_boots", 600000, 1, "item.awakening_system.astral_boots", "armor", 3, "awakening_system:murim", 255),

            new Entry("celestial_helmet", "awakening_system:celestial_helmet", 1100000, 1, "item.awakening_system.celestial_helmet", "armor", 4, "awakening_system:teyvat_world", 295),
            new Entry("celestial_chestplate", "awakening_system:celestial_chestplate", 1600000, 1, "item.awakening_system.celestial_chestplate", "armor", 4, "awakening_system:teyvat_world", 295),
            new Entry("celestial_leggings", "awakening_system:celestial_leggings", 1350000, 1, "item.awakening_system.celestial_leggings", "armor", 4, "awakening_system:teyvat_world", 295),
            new Entry("celestial_boots", "awakening_system:celestial_boots", 980000, 1, "item.awakening_system.celestial_boots", "armor", 4, "awakening_system:teyvat_world", 295),

            new Entry("primordial_helmet", "awakening_system:primordial_helmet", 2200000, 1, "item.awakening_system.primordial_helmet", "armor", 4, "awakening_system:teyvat_world", 340),
            new Entry("primordial_chestplate", "awakening_system:primordial_chestplate", 3200000, 1, "item.awakening_system.primordial_chestplate", "armor", 4, "awakening_system:teyvat_world", 340),
            new Entry("primordial_leggings", "awakening_system:primordial_leggings", 2700000, 1, "item.awakening_system.primordial_leggings", "armor", 4, "awakening_system:teyvat_world", 340),
            new Entry("primordial_boots", "awakening_system:primordial_boots", 1950000, 1, "item.awakening_system.primordial_boots", "armor", 4, "awakening_system:teyvat_world", 340),

            // --- RUNAS Y RELIQUIAS DE COMBATE ---
            new Entry("rune_stone_rulers_authority", "awakening_system:rune_stone_rulers_authority", 4500, 1, "item.awakening_system.rune_stone_rulers_authority", "rare", 2),
            new Entry("rune_stone_dragon_eyes", "awakening_system:rune_stone_dragon_eyes", 3000, 1, "item.awakening_system.rune_stone_dragon_eyes", "rare", 2),
            new Entry("rune_stone_bloodthirst", "awakening_system:rune_stone_bloodthirst", 3200, 1, "item.awakening_system.rune_stone_bloodthirst", "rare", 1),
            new Entry("rune_stone_shadow_step", "awakening_system:rune_stone_shadow_step", 2800, 1, "item.awakening_system.rune_stone_shadow_step", "rare", 1),
            new Entry("rune_stone_iron_body", "awakening_system:rune_stone_iron_body", 2900, 1, "item.awakening_system.rune_stone_iron_body", "rare", 1),
            new Entry("rune_stone_fourth_wall", "awakening_system:rune_stone_fourth_wall", 5200, 1, "item.awakening_system.rune_stone_fourth_wall", "rare", 2),
            new Entry("demon_monarch_ring", "awakening_system:demon_monarch_ring", 3000, 1, "item.awakening_system.demon_monarch_ring", "equipment", 1),
            new Entry("asrahan_mana_chalice", "awakening_system:asrahan_mana_chalice", 2800, 1, "item.awakening_system.asrahan_mana_chalice", "equipment", 2),
            new Entry("king_slayer_necklace", "awakening_system:king_slayer_necklace", 2700, 1, "item.awakening_system.king_slayer_necklace", "equipment", 1),
            new Entry("phoenix_feather", "awakening_system:phoenix_feather", 3600, 1, "item.awakening_system.phoenix_feather", "equipment", 2),
            new Entry("fable_star_fragment", "awakening_system:fable_star_fragment", 18500, 1, "awakening_system.shop.item.fable_star_fragment", "rare", 2, 35),
            new Entry("constellation_nectar", "awakening_system:constellation_nectar", 350, 1, "awakening_system.shop.item.constellation_nectar", "consumables", 1),
            new Entry("tribulation_shield_pill", "awakening_system:tribulation_shield_pill", 1200, 1, "item.awakening_system.tribulation_shield_pill", "consumables", 3),
            new Entry("realm_breakthrough_pill", "awakening_system:realm_breakthrough_pill", 3500, 1, "item.awakening_system.realm_breakthrough_pill", "consumables", 3),
            new Entry("fable_scroll", "awakening_system:fable_scroll", 32000, 1, "awakening_system.shop.item.fable_scroll", "rare", 3, 50),

            // --- CREATE MOD (CON NIVELES REBALANCEADOS) ---
            new Entry("create_wrench", "create:wrench", 120, 1, "awakening_system.shop.item.create_wrench", "tools", "create"),
            new Entry("create_goggles", "create:goggles", 150, 1, "awakening_system.shop.item.create_goggles", "equipment", "create"),
            new Entry("create_andesite_alloy", "create:andesite_alloy", 80, 16, "awakening_system.shop.item.create_andesite_alloy", "materials", "create"),
            new Entry("create_brass_ingot", "create:brass_ingot", 160, 8, "awakening_system.shop.item.create_brass_ingot", "materials", "create"),
            new Entry("create_zinc_ingot", "create:zinc_ingot", 90, 8, "awakening_system.shop.item.create_zinc_ingot", "materials", "create"),
            new Entry("create_copper_casing", "create:copper_casing", 120, 8, "awakening_system.shop.item.create_copper_casing", "materials", "create"),
            new Entry("create_brass_casing", "create:brass_casing", 140, 8, "awakening_system.shop.item.create_brass_casing", "materials", "create"),
            new Entry("create_andesite_casing", "create:andesite_casing", 90, 8, "awakening_system.shop.item.create_andesite_casing", "materials", "create"),
            new Entry("create_shaft", "create:shaft", 45, 16, "awakening_system.shop.item.create_shaft", "utility", "create"),
            new Entry("create_cogwheel", "create:cogwheel", 70, 8, "awakening_system.shop.item.create_cogwheel", "utility", "create"),
            new Entry("create_large_cogwheel", "create:large_cogwheel", 100, 8, "awakening_system.shop.item.create_large_cogwheel", "utility", "create"),
            new Entry("create_gearbox", "create:gearbox", 120, 2, "awakening_system.shop.item.create_gearbox", "utility", "create"),
            new Entry("create_belt_connector", "create:belt_connector", 80, 8, "awakening_system.shop.item.create_belt_connector", "utility", "create"),
            new Entry("create_chute", "create:chute", 95, 4, "awakening_system.shop.item.create_chute", "utility", "create"),
            new Entry("create_water_wheel", "create:water_wheel", 160, 1, "awakening_system.shop.item.create_water_wheel", "equipment", "create"),
            new Entry("create_large_water_wheel", "create:large_water_wheel", 280, 1, "awakening_system.shop.item.create_large_water_wheel", "equipment", "create"),
            new Entry("create_windmill_bearing", "create:windmill_bearing", 220, 1, "awakening_system.shop.item.create_windmill_bearing", "equipment", "create"),
            new Entry("create_mechanical_press", "create:mechanical_press", 340, 1, "awakening_system.shop.item.create_mechanical_press", "equipment", "create"),
            new Entry("create_mechanical_mixer", "create:mechanical_mixer", 340, 1, "awakening_system.shop.item.create_mechanical_mixer", "equipment", "create"),
            new Entry("create_basin", "create:basin", 140, 1, "awakening_system.shop.item.create_basin", "equipment", "create"),
            new Entry("create_mechanical_drill", "create:mechanical_drill", 300, 1, "awakening_system.shop.item.create_mechanical_drill", "tools", "create"),
            new Entry("create_mechanical_saw", "create:mechanical_saw", 300, 1, "awakening_system.shop.item.create_mechanical_saw", "tools", "create"),
            new Entry("create_deployer", "create:deployer", 420, 1, "awakening_system.shop.item.create_deployer", "equipment", "create"),
            new Entry("create_crushing_wheel", "create:crushing_wheel", 850, 2, "awakening_system.shop.item.create_crushing_wheel", "equipment", "create", 1, 35),
            new Entry("create_blaze_burner", "create:blaze_burner", 550, 1, "awakening_system.shop.item.create_blaze_burner", "equipment", "create"),
            new Entry("create_blaze_cake", "create:blaze_cake", 260, 2, "awakening_system.shop.item.create_blaze_cake", "consumables", "create"),
            new Entry("create_electron_tube", "create:electron_tube", 160, 4, "awakening_system.shop.item.create_electron_tube", "materials", "create"),
            new Entry("create_precision_mechanism", "create:precision_mechanism", 420, 1, "awakening_system.shop.item.create_precision_mechanism", "rare", "create", 1, 25),
            new Entry("create_steam_engine", "create:steam_engine", 650, 1, "awakening_system.shop.item.create_steam_engine", "equipment", "create", 1, 30),
            new Entry("create_fluid_pipe", "create:fluid_pipe", 75, 16, "awakening_system.shop.item.create_fluid_pipe", "utility", "create"),
            new Entry("create_mechanical_pump", "create:mechanical_pump", 160, 1, "awakening_system.shop.item.create_mechanical_pump", "equipment", "create"),
            new Entry("create_fluid_tank", "create:fluid_tank", 200, 2, "awakening_system.shop.item.create_fluid_tank", "equipment", "create"),
            new Entry("create_spout", "create:spout", 260, 1, "awakening_system.shop.item.create_spout", "equipment", "create"),
            new Entry("create_item_vault", "create:item_vault", 320, 2, "awakening_system.shop.item.create_item_vault", "utility", "create"),
            new Entry("create_track_pack", "create:track", 110, 32, "awakening_system.shop.item.create_track_pack", "utility", "create"),
            new Entry("create_train_controls", "create:controls", 240, 1, "awakening_system.shop.item.create_train_controls", "utility", "create"),
            new Entry("create_schematicannon", "create:schematicannon", 700, 1, "awakening_system.shop.item.create_schematicannon", "rare", "create", 1, 20),

            // --- MATERIALES BÁSICOS ---
            new Entry("oak_log", "minecraft:oak_log", 20, 16, "block.minecraft.oak_log", "materials"),
            new Entry("spruce_log", "minecraft:spruce_log", 20, 16, "block.minecraft.spruce_log", "materials"),
            new Entry("birch_log", "minecraft:birch_log", 20, 16, "block.minecraft.birch_log", "materials"),
            new Entry("cobblestone", "minecraft:cobblestone", 15, 32, "block.minecraft.cobblestone", "materials"),
            new Entry("stone", "minecraft:stone", 20, 32, "block.minecraft.stone", "materials"),
            new Entry("deepslate", "minecraft:deepslate", 25, 32, "block.minecraft.deepslate", "materials"),
            new Entry("coal", "minecraft:coal", 40, 16, "item.minecraft.coal", "materials"),
            new Entry("copper_ingot", "minecraft:copper_ingot", 50, 8, "item.minecraft.copper_ingot", "materials"),
            new Entry("iron_ingot", "minecraft:iron_ingot", 80, 8, "item.minecraft.iron_ingot", "materials"),
            new Entry("gold_ingot", "minecraft:gold_ingot", 120, 8, "item.minecraft.gold_ingot", "materials"),
            new Entry("redstone", "minecraft:redstone", 60, 16, "item.minecraft.redstone", "materials"),
            new Entry("lapis_lazuli", "minecraft:lapis_lazuli", 60, 16, "item.minecraft.lapis_lazuli", "materials"),
            new Entry("diamond", "minecraft:diamond", 500, 2, "item.minecraft.diamond", "materials"),
            new Entry("emerald", "minecraft:emerald", 200, 4, "item.minecraft.emerald", "materials"),
            new Entry("amethyst_shard", "minecraft:amethyst_shard", 80, 8, "item.minecraft.amethyst_shard", "materials"),
            new Entry("nether_quartz", "minecraft:quartz", 90, 16, "item.minecraft.quartz", "materials"),
            new Entry("obsidian", "minecraft:obsidian", 160, 8, "block.minecraft.obsidian", "materials"),
            new Entry("crying_obsidian", "minecraft:crying_obsidian", 200, 4, "block.minecraft.crying_obsidian", "materials"),
            new Entry("string", "minecraft:string", 30, 16, "item.minecraft.string", "materials"),
            new Entry("leather", "minecraft:leather", 40, 8, "item.minecraft.leather", "materials"),
            new Entry("blaze_rod", "minecraft:blaze_rod", 150, 4, "item.minecraft.blaze_rod", "materials"),
            new Entry("glowstone_dust", "minecraft:glowstone_dust", 80, 16, "item.minecraft.glowstone_dust", "materials"),

            // --- CONSUMIBLES & RELIQUIAS VANILLA ---
            new Entry("golden_apple", "minecraft:golden_apple", 400, 1, "awakening_system.shop.item.golden_apple", "consumables"),
            new Entry("enchanted_golden_apple", "minecraft:enchanted_golden_apple", 12000, 1, "awakening_system.shop.item.enchanted_golden_apple", "rare", 1, 40),
            new Entry("totem_of_undying", "minecraft:totem_of_undying", 16000, 1, "awakening_system.shop.item.totem", "rare", 1, 45),
            new Entry("nether_star", "minecraft:nether_star", 9500, 1, "awakening_system.shop.item.nether_star", "rare", "", 1, "minecraft:the_nether", 60),
            new Entry("elytra", "minecraft:elytra", 45000, 1, "awakening_system.shop.item.elytra", "rare", "", 2, "minecraft:the_end", 70),
            new Entry("heavy_core", "minecraft:heavy_core", 18000, 1, "awakening_system.shop.item.heavy_core", "rare", 1, 50),
            new Entry("shulker_shell", "minecraft:shulker_shell", 1200, 2, "awakening_system.shop.item.shulker_shell", "rare", "", 2, "minecraft:the_end", 60),
            new Entry("ender_pearl", "minecraft:ender_pearl", 240, 4, "awakening_system.shop.item.ender_pearl", "consumables"),
            new Entry("experience_bottle", "minecraft:experience_bottle", 200, 4, "awakening_system.shop.item.experience_bottle", "consumables"),
            new Entry("attribute_reset", "awakening_system:olive_elixir", 30000, 1, "awakening_system.shop.item.attribute_reset", "consumables"),
            new Entry("energy_elixir", "awakening_system:vigor_potion", 300, 1, "awakening_system.shop.item.energy_elixir", "consumables"),
            new Entry("cooked_beef", "minecraft:cooked_beef", 25, 8, "awakening_system.shop.item.cooked_beef", "consumables"),
            new Entry("cooked_porkchop", "minecraft:cooked_porkchop", 25, 8, "awakening_system.shop.item.cooked_porkchop", "consumables"),
            new Entry("cooked_mutton", "minecraft:cooked_mutton", 25, 8, "item.minecraft.cooked_mutton", "consumables"),
            new Entry("cooked_chicken", "minecraft:cooked_chicken", 25, 8, "item.minecraft.cooked_chicken", "consumables"),
            new Entry("cooked_salmon", "minecraft:cooked_salmon", 30, 8, "item.minecraft.cooked_salmon", "consumables"),
            new Entry("cooked_cod", "minecraft:cooked_cod", 25, 8, "item.minecraft.cooked_cod", "consumables"),
            new Entry("baked_potato", "minecraft:baked_potato", 20, 12, "item.minecraft.baked_potato", "consumables"),
            new Entry("bread", "minecraft:bread", 20, 16, "awakening_system.shop.item.bread", "consumables"),
            new Entry("golden_carrot", "minecraft:golden_carrot", 120, 4, "awakening_system.shop.item.golden_carrot", "consumables"),
            new Entry("honey_bottle", "minecraft:honey_bottle", 35, 4, "awakening_system.shop.item.honey_bottle", "consumables"),
            new Entry("pumpkin_pie", "minecraft:pumpkin_pie", 35, 4, "item.minecraft.pumpkin_pie", "consumables"),
            new Entry("cake", "minecraft:cake", 45, 1, "block.minecraft.cake", "consumables"),
            new Entry("mushroom_stew", "minecraft:mushroom_stew", 30, 2, "item.minecraft.mushroom_stew", "consumables"),
            new Entry("rabbit_stew", "minecraft:rabbit_stew", 55, 2, "item.minecraft.rabbit_stew", "consumables"),
            new Entry("beetroot_soup", "minecraft:beetroot_soup", 25, 2, "item.minecraft.beetroot_soup", "consumables"),
            new Entry("cookie", "minecraft:cookie", 20, 16, "item.minecraft.cookie", "consumables"),
            new Entry("apple", "minecraft:apple", 15, 8, "item.minecraft.apple", "consumables"),
            new Entry("sweet_berries", "minecraft:sweet_berries", 15, 16, "item.minecraft.sweet_berries", "consumables"),
            new Entry("glow_berries", "minecraft:glow_berries", 20, 12, "item.minecraft.glow_berries", "consumables"),
            new Entry("glistering_melon", "minecraft:glistering_melon_slice", 45, 4, "item.minecraft.glistering_melon_slice", "consumables"),
            new Entry("chorus_fruit", "minecraft:chorus_fruit", 30, 8, "awakening_system.shop.item.chorus_fruit", "consumables", "", 2, "minecraft:the_end", 50),

            // --- HERRAMIENTAS & PROGRESIÓN (CAPADAS POR NIVEL) ---
            new Entry("iron_pickaxe", "minecraft:iron_pickaxe", 95, 1, "awakening_system.shop.item.iron_pickaxe", "tools", 1, 10),
            new Entry("iron_axe", "minecraft:iron_axe", 90, 1, "awakening_system.shop.item.iron_axe", "tools", 1, 10),
            new Entry("iron_shovel", "minecraft:iron_shovel", 50, 1, "awakening_system.shop.item.iron_shovel", "tools", 1, 10),

            new Entry("diamond_pickaxe", "minecraft:diamond_pickaxe", 5500, 1, "awakening_system.shop.item.diamond_pickaxe", "tools", 1, 45),
            new Entry("diamond_axe", "minecraft:diamond_axe", 5200, 1, "awakening_system.shop.item.diamond_axe", "tools", 1, 45),
            new Entry("diamond_shovel", "minecraft:diamond_shovel", 3000, 1, "awakening_system.shop.item.diamond_shovel", "tools", 1, 45),

            // --- HERRAMIENTAS ABISALES (INTERMEDIO DIAMANTE -> NETHERITA) ---
            new Entry("abyssal_pickaxe", "awakening_system:abyssal_pickaxe", 24000, 1, "item.awakening_system.abyssal_pickaxe", "tools", 1, 60),
            new Entry("abyssal_axe", "awakening_system:abyssal_axe", 22000, 1, "item.awakening_system.abyssal_axe", "tools", 1, 60),
            new Entry("abyssal_shovel", "awakening_system:abyssal_shovel", 14000, 1, "item.awakening_system.abyssal_shovel", "tools", 1, 60),
            new Entry("abyssal_hoe", "awakening_system:abyssal_hoe", 11000, 1, "item.awakening_system.abyssal_hoe", "tools", 1, 60),

            // --- HERRAMIENTAS NETHERITA ---
            new Entry("netherite_pickaxe", "minecraft:netherite_pickaxe", 45000, 1, "item.minecraft.netherite_pickaxe", "tools", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_axe", "minecraft:netherite_axe", 42000, 1, "item.minecraft.netherite_axe", "tools", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_shovel", "minecraft:netherite_shovel", 28000, 1, "item.minecraft.netherite_shovel", "tools", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_hoe", "minecraft:netherite_hoe", 22000, 1, "item.minecraft.netherite_hoe", "tools", "", 1, "minecraft:the_nether", 75),
            new Entry("fishing_rod", "minecraft:fishing_rod", 35, 1, "awakening_system.shop.item.fishing_rod", "tools"),

            // --- HERRAMIENTAS PROPIAS POR DIMENSION ---

            new Entry("mithril_pickaxe", "awakening_system:mithril_pickaxe", 110000, 1, "item.awakening_system.mithril_pickaxe", "tools", 2, "awakening_system:isekai", 135),
            new Entry("mithril_axe", "awakening_system:mithril_axe", 100000, 1, "item.awakening_system.mithril_axe", "tools", 2, "awakening_system:isekai", 135),
            new Entry("mithril_shovel", "awakening_system:mithril_shovel", 68000, 1, "item.awakening_system.mithril_shovel", "tools", 2, "awakening_system:isekai", 135),
            new Entry("mithril_hoe", "awakening_system:mithril_hoe", 55000, 1, "item.awakening_system.mithril_hoe", "tools", 2, "awakening_system:isekai", 135),

            new Entry("adamantite_pickaxe", "awakening_system:adamantite_pickaxe", 175000, 1, "item.awakening_system.adamantite_pickaxe", "tools", 2, "awakening_system:isekai", 175),
            new Entry("adamantite_axe", "awakening_system:adamantite_axe", 160000, 1, "item.awakening_system.adamantite_axe", "tools", 2, "awakening_system:isekai", 175),
            new Entry("adamantite_shovel", "awakening_system:adamantite_shovel", 110000, 1, "item.awakening_system.adamantite_shovel", "tools", 2, "awakening_system:isekai", 175),
            new Entry("adamantite_hoe", "awakening_system:adamantite_hoe", 88000, 1, "item.awakening_system.adamantite_hoe", "tools", 2, "awakening_system:isekai", 175),

            new Entry("spirit_jade_pickaxe", "awakening_system:spirit_jade_pickaxe", 240000, 1, "item.awakening_system.spirit_jade_pickaxe", "tools", 3, "awakening_system:murim", 215),
            new Entry("spirit_jade_axe", "awakening_system:spirit_jade_axe", 220000, 1, "item.awakening_system.spirit_jade_axe", "tools", 3, "awakening_system:murim", 215),
            new Entry("spirit_jade_shovel", "awakening_system:spirit_jade_shovel", 150000, 1, "item.awakening_system.spirit_jade_shovel", "tools", 3, "awakening_system:murim", 215),
            new Entry("spirit_jade_hoe", "awakening_system:spirit_jade_hoe", 120000, 1, "item.awakening_system.spirit_jade_hoe", "tools", 3, "awakening_system:murim", 215),

            new Entry("astral_pickaxe", "awakening_system:astral_pickaxe", 360000, 1, "item.awakening_system.astral_pickaxe", "tools", 3, "awakening_system:murim", 255),
            new Entry("astral_axe", "awakening_system:astral_axe", 340000, 1, "item.awakening_system.astral_axe", "tools", 3, "awakening_system:murim", 255),
            new Entry("astral_shovel", "awakening_system:astral_shovel", 220000, 1, "item.awakening_system.astral_shovel", "tools", 3, "awakening_system:murim", 255),
            new Entry("astral_hoe", "awakening_system:astral_hoe", 180000, 1, "item.awakening_system.astral_hoe", "tools", 3, "awakening_system:murim", 255),

            new Entry("celestial_pickaxe", "awakening_system:celestial_pickaxe", 550000, 1, "item.awakening_system.celestial_pickaxe", "tools", 4, "awakening_system:teyvat_world", 295),
            new Entry("celestial_axe", "awakening_system:celestial_axe", 550000, 1, "item.awakening_system.celestial_axe", "tools", 4, "awakening_system:teyvat_world", 295),
            new Entry("celestial_shovel", "awakening_system:celestial_shovel", 380000, 1, "item.awakening_system.celestial_shovel", "tools", 4, "awakening_system:teyvat_world", 295),
            new Entry("celestial_hoe", "awakening_system:celestial_hoe", 300000, 1, "item.awakening_system.celestial_hoe", "tools", 4, "awakening_system:teyvat_world", 295),

            new Entry("primordial_pickaxe", "awakening_system:primordial_pickaxe", 950000, 1, "item.awakening_system.primordial_pickaxe", "tools", 4, "awakening_system:teyvat_world", 340),
            new Entry("primordial_axe", "awakening_system:primordial_axe", 950000, 1, "item.awakening_system.primordial_axe", "tools", 4, "awakening_system:teyvat_world", 340),
            new Entry("primordial_shovel", "awakening_system:primordial_shovel", 650000, 1, "item.awakening_system.primordial_shovel", "tools", 4, "awakening_system:teyvat_world", 340),
            new Entry("primordial_hoe", "awakening_system:primordial_hoe", 500000, 1, "item.awakening_system.primordial_hoe", "tools", 4, "awakening_system:teyvat_world", 340),

            // --- UTILIDADES ---
            new Entry("water_bucket", "minecraft:water_bucket", 55, 1, "awakening_system.shop.item.water_bucket", "utility"),
            new Entry("lava_bucket", "minecraft:lava_bucket", 95, 1, "awakening_system.shop.item.lava_bucket", "utility"),
            new Entry("firework_rocket", "minecraft:firework_rocket", 25, 8, "awakening_system.shop.item.firework_rocket", "utility"),
            new Entry("saddle", "minecraft:saddle", 80, 1, "awakening_system.shop.item.saddle", "utility"),
            new Entry("name_tag", "minecraft:name_tag", 50, 1, "awakening_system.shop.item.name_tag", "utility"),
            new Entry("spyglass", "minecraft:spyglass", 50, 1, "awakening_system.shop.item.spyglass", "utility"),
            new Entry("trial_key", "minecraft:trial_key", 400, 1, "awakening_system.shop.item.trial_key", "rare"),
            new Entry("ominous_trial_key", "minecraft:ominous_trial_key", 900, 1, "awakening_system.shop.item.ominous_trial_key", "rare"),
            new Entry("heart_of_the_sea", "minecraft:heart_of_the_sea", 420, 1, "awakening_system.shop.item.heart_of_the_sea", "rare"),
            new Entry("breeze_rod", "minecraft:breeze_rod", 80, 4, "awakening_system.shop.item.breeze_rod", "materials"),
            new Entry("wind_charge", "minecraft:wind_charge", 45, 8, "awakening_system.shop.item.wind_charge", "utility"),
            new Entry("dragon_breath", "minecraft:dragon_breath", 75, 2, "awakening_system.shop.item.dragon_breath", "materials", "", 2, "minecraft:the_end", 60),

            // --- MINERALES Y MATERIALES BRUTOS (CAPADOS POR NIVEL) ---
            new Entry("coal", "minecraft:coal", 20, 8, "item.minecraft.coal", "materials"),
            new Entry("leather", "minecraft:leather", 15, 4, "item.minecraft.leather", "materials"),
            new Entry("string", "minecraft:string", 12, 4, "item.minecraft.string", "materials"),
            new Entry("feather", "minecraft:feather", 10, 4, "item.minecraft.feather", "materials"),
            new Entry("flint", "minecraft:flint", 15, 4, "item.minecraft.flint", "materials"),
            new Entry("bone", "minecraft:bone", 12, 4, "item.minecraft.bone", "materials"),
            new Entry("bone_meal", "minecraft:bone_meal", 15, 8, "item.minecraft.bone_meal", "materials"),
            new Entry("arrow", "minecraft:arrow", 20, 16, "item.minecraft.arrow", "materials"),
            new Entry("gunpowder", "minecraft:gunpowder", 30, 4, "item.minecraft.gunpowder", "materials"),
            new Entry("paper", "minecraft:paper", 15, 8, "item.minecraft.paper", "materials"),
            new Entry("book", "minecraft:book", 25, 1, "item.minecraft.book", "materials"),
            new Entry("clay_ball", "minecraft:clay_ball", 15, 8, "item.minecraft.clay_ball", "materials"),
            new Entry("diamond", "minecraft:diamond", 1400, 2, "awakening_system.shop.item.diamond", "materials", 1, 30),
            new Entry("netherite_scrap", "minecraft:netherite_scrap", 4000, 1, "awakening_system.shop.item.netherite_scrap", "materials", "", 1, "minecraft:the_nether", 70),
            new Entry("netherite_ingot", "minecraft:netherite_ingot", 28000, 1, "item.minecraft.netherite_ingot", "materials", "", 1, "minecraft:the_nether", 75),
            new Entry("netherite_stick", "awakening_system:netherite_stick", 1500, 1, "item.awakening_system.netherite_stick", "materials", "", 1, "minecraft:the_nether", 50),
            new Entry("iron_ingot", "minecraft:iron_ingot", 45, 4, "awakening_system.shop.item.iron_ingot", "materials"),
            new Entry("gold_ingot", "minecraft:gold_ingot", 55, 4, "awakening_system.shop.item.gold_ingot", "materials"),
            new Entry("copper_ingot", "minecraft:copper_ingot", 30, 8, "awakening_system.shop.item.copper_ingot", "materials"),
            new Entry("lapis_lazuli", "minecraft:lapis_lazuli", 35, 16, "awakening_system.shop.item.lapis_lazuli", "materials"),
            new Entry("redstone", "minecraft:redstone", 35, 16, "awakening_system.shop.item.redstone", "materials"),
            new Entry("quartz", "minecraft:quartz", 35, 16, "awakening_system.shop.item.quartz", "materials", "", 1, "minecraft:the_nether"),
            new Entry("blaze_rod", "minecraft:blaze_rod", 65, 2, "awakening_system.shop.item.blaze_rod", "materials", "", 1, "minecraft:the_nether"),
            new Entry("slime_ball", "minecraft:slime_ball", 45, 4, "awakening_system.shop.item.slime_ball", "materials"),
            new Entry("echo_shard", "minecraft:echo_shard", 150, 2, "awakening_system.shop.item.echo_shard", "materials", 1, 40),
            new Entry("obsidian", "minecraft:obsidian", 65, 8, "awakening_system.shop.item.obsidian", "materials"),
            new Entry("emerald", "minecraft:emerald", 60, 2, "awakening_system.shop.item.emerald", "materials"),
            new Entry("amethyst_shard", "minecraft:amethyst_shard", 45, 4, "awakening_system.shop.item.amethyst_shard", "materials"),

            // --- PLANTILLAS DE SMITHING POR DIMENSION ---
            new Entry("abyssal_smithing_template", "awakening_system:abyssal_upgrade_smithing_template", 45000, 1, "item.awakening_system.abyssal_upgrade_smithing_template", "materials", 1, "minecraft:the_nether", 100),
            new Entry("mithril_smithing_template", "awakening_system:mithril_upgrade_smithing_template", 75000, 1, "item.awakening_system.mithril_upgrade_smithing_template", "materials", 2, "awakening_system:isekai", 135),
            new Entry("adamantite_smithing_template", "awakening_system:adamantite_upgrade_smithing_template", 110000, 1, "item.awakening_system.adamantite_upgrade_smithing_template", "materials", 2, "awakening_system:isekai", 175),
            new Entry("spirit_jade_smithing_template", "awakening_system:spirit_jade_upgrade_smithing_template", 160000, 1, "item.awakening_system.spirit_jade_upgrade_smithing_template", "materials", 3, "awakening_system:murim", 215),
            new Entry("astral_smithing_template", "awakening_system:astral_upgrade_smithing_template", 240000, 1, "item.awakening_system.astral_upgrade_smithing_template", "materials", 3, "awakening_system:murim", 255),
            new Entry("celestial_smithing_template", "awakening_system:celestial_upgrade_smithing_template", 380000, 1, "item.awakening_system.celestial_upgrade_smithing_template", "materials", 4, "awakening_system:teyvat_world", 295),
            new Entry("primordial_smithing_template", "awakening_system:primordial_upgrade_smithing_template", 650000, 1, "item.awakening_system.primordial_upgrade_smithing_template", "materials", 4, "awakening_system:teyvat_world", 340),

            // --- MODS EXTERNOS COMPATIBLES (PRECIOS REBALANCEADOS) ---
            new Entry("cataclysm_ignitium_ingot", "cataclysm:ignitium_ingot", 16000, 2, "awakening_system.shop.item.cataclysm_ignitium", "materials", "cataclysm", 1, "minecraft:the_nether", 85),
            new Entry("aether_ambrosium", "aether:ambrosium_shard", 150, 4, "awakening_system.shop.item.aether_ambrosium", "materials", "aether", 1, "aether:the_aether", 20),
            new Entry("twilight_carminite", "twilightforest:carminite", 850, 4, "awakening_system.shop.item.twilight_carminite", "materials", "twilightforest", 1, "twilightforest:twilight_forest", 35),
            new Entry("mowzie_sunmetal", "mowziesmobs:sunmetal_ingot", 3200, 2, "awakening_system.shop.item.mowzie_sunmetal", "materials", "mowziesmobs", 1, 45),
            new Entry("undergarden_depthrock", "undergarden:depthrock", 100, 16, "awakening_system.shop.item.undergarden_depthrock", "materials", "undergarden", 1, "undergarden:undergarden", 25),
            new Entry("simplyswords_iron_long_sword", "simplyswords:iron_long_sword", 120, 1, "awakening_system.shop.item.simplyswords_iron_long_sword", "weapons", "simplyswords", 1, 10),
            new Entry("simplyswords_diamond_long_sword", "simplyswords:diamond_long_sword", 2800, 1, "awakening_system.shop.item.simplyswords_diamond_long_sword", "weapons", "simplyswords", 1, 45),
            new Entry("simplyswords_netherite_long_sword", "simplyswords:netherite_long_sword", 19500, 1, "awakening_system.shop.item.simplyswords_netherite_long_sword", "weapons", "simplyswords", 1, "minecraft:the_nether", 75),

            // --- APOTHEOSIS / APOTHIC (COMPATIBILIDAD INTEGRADA) ---
            new Entry("apoth_common_material", "apotheosis:common_material", 200, 1, "item.apotheosis.common_material", "materials", "apotheosis", 1, 15),
            new Entry("apoth_gem_dust", "apotheosis:gem_dust", 650, 1, "item.apotheosis.gem_dust", "materials", "apotheosis", 1, 25),
            new Entry("apoth_uncommon_material", "apotheosis:uncommon_material", 550, 1, "item.apotheosis.uncommon_material", "materials", "apotheosis", 1, 30),
            new Entry("apoth_sigil_unnaming", "apotheosis:sigil_of_unnaming", 2500, 1, "item.apotheosis.sigil_of_unnaming", "utility", "apotheosis", 1, 20),
            new Entry("apoth_scrap_tome", "apotheosis:scrap_tome", 3500, 1, "item.apotheosis.scrap_tome", "utility", "apotheosis", 1, 30),
            new Entry("apoth_sigil_withdrawal", "apotheosis:sigil_of_withdrawal", 6500, 1, "item.apotheosis.sigil_of_withdrawal", "utility", "apotheosis", 2, 35),
            new Entry("apoth_vial_expulsion", "apotheosis:vial_of_expulsion", 5000, 1, "item.apotheosis.vial_of_expulsion", "utility", "apotheosis", 2, 35),
            new Entry("apoth_vial_extraction", "apotheosis:vial_of_extraction", 5000, 1, "item.apotheosis.vial_of_extraction", "utility", "apotheosis", 2, 35),
            new Entry("apoth_sigil_socketing", "apotheosis:sigil_of_socketing", 8000, 1, "item.apotheosis.sigil_of_socketing", "rare", "apotheosis", 2, 40),
            new Entry("apoth_rare_material", "apotheosis:rare_material", 1400, 1, "item.apotheosis.rare_material", "materials", "apotheosis", 2, 45),
            new Entry("apoth_sigil_enhancement", "apotheosis:sigil_of_enhancement", 12000, 1, "item.apotheosis.sigil_of_enhancement", "rare", "apotheosis", 3, 55),
            new Entry("apoth_epic_material", "apotheosis:epic_material", 3800, 1, "item.apotheosis.epic_material", "materials", "apotheosis", 3, 60),
            new Entry("apoth_prismatic_web", "apotheosis:prismatic_web", 14000, 1, "item.apotheosis.prismatic_web", "rare", "apotheosis", 3, 70),
            new Entry("apoth_mythic_material", "apotheosis:mythic_material", 9500, 1, "item.apotheosis.mythic_material", "materials", "apotheosis", 4, 75),
            new Entry("apoth_godforged_pearl", "apotheosis:godforged_pearl", 25000, 1, "item.apotheosis.godforged_pearl", "rare", "apotheosis", 4, 80),

            // --- PAM'S HARVESTCRAFT 2 - FOOD EXTENDED ---
            new Entry("phc_pancakes", "pamhc2foodextended:pancakesitem", 35, 4, "item.pamhc2foodextended.pancakesitem", "consumables", "pamhc2foodextended", 1, 1),
            new Entry("phc_waffles", "pamhc2foodextended:wafflesitem", 35, 4, "item.pamhc2foodextended.wafflesitem", "consumables", "pamhc2foodextended", 1, 1),
            new Entry("phc_cinnamon_toast", "pamhc2foodextended:cinnamontoastitem", 25, 4, "item.pamhc2foodextended.cinnamontoastitem", "consumables", "pamhc2foodextended", 1, 1),
            new Entry("phc_hot_chocolate", "pamhc2foodextended:hotchocolateitem", 30, 2, "item.pamhc2foodextended.hotchocolateitem", "consumables", "pamhc2foodextended", 1, 1),
            new Entry("phc_cupcake", "pamhc2foodextended:chocolatecupcakeitem", 35, 4, "item.pamhc2foodextended.chocolatecupcakeitem", "consumables", "pamhc2foodextended", 1, 1),
            new Entry("phc_brownie", "pamhc2foodextended:brownieitem", 30, 4, "item.pamhc2foodextended.brownieitem", "consumables", "pamhc2foodextended", 1, 1),
            new Entry("phc_cutting_board", "pamhc2foodcore:cuttingboarditem", 30, 1, "item.pamhc2foodcore.cuttingboarditem", "utility", "pamhc2foodextended", 1, 1),
            new Entry("phc_spaghetti", "pamhc2foodextended:spagettiandmeatballsitem", 55, 2, "item.pamhc2foodextended.spagettiandmeatballsitem", "consumables", "pamhc2foodextended", 1, 5),
            new Entry("phc_cheesecake", "pamhc2foodextended:cheesecakeitem", 45, 2, "item.pamhc2foodextended.cheesecakeitem", "consumables", "pamhc2foodextended", 1, 5),
            new Entry("phc_strawberry_smoothie", "pamhc2foodextended:strawberrysmoothieitem", 35, 2, "item.pamhc2foodextended.strawberrysmoothieitem", "consumables", "pamhc2foodextended", 1, 5),
            new Entry("phc_pot", "pamhc2foodcore:potitem", 65, 1, "item.pamhc2foodcore.potitem", "utility", "pamhc2foodextended", 1, 5),
            new Entry("phc_skillet", "pamhc2foodcore:skilletitem", 55, 1, "item.pamhc2foodcore.skilletitem", "utility", "pamhc2foodextended", 1, 5),
            new Entry("phc_bakeware", "pamhc2foodcore:bakewareitem", 50, 1, "item.pamhc2foodcore.bakewareitem", "utility", "pamhc2foodextended", 1, 5),
            new Entry("phc_deluxe_cheeseburger", "pamhc2foodextended:deluxecheeseburgeritem", 65, 2, "item.pamhc2foodextended.deluxecheeseburgeritem", "consumables", "pamhc2foodextended", 1, 10),
            new Entry("phc_shepherds_pie", "pamhc2foodextended:shepherdspieitem", 60, 2, "item.pamhc2foodextended.shepherdspieitem", "consumables", "pamhc2foodextended", 1, 10),
            new Entry("phc_epic_bacon", "pamhc2foodextended:epicbaconitem", 70, 2, "item.pamhc2foodextended.epicbaconitem", "consumables", "pamhc2foodextended", 1, 15),
            new Entry("phc_meat_feast", "pamhc2foodextended:meatfeastitem", 80, 1, "item.pamhc2foodextended.meatfeastitem", "consumables", "pamhc2foodextended", 1, 15),
            new Entry("phc_energy_drink", "pamhc2foodextended:energydrinkitem", 50, 2, "item.pamhc2foodextended.energydrinkitem", "consumables", "pamhc2foodextended", 1, 15),
            new Entry("phc_hearty_breakfast", "pamhc2foodextended:heartybreakfastitem", 85, 1, "item.pamhc2foodextended.heartybreakfastitem", "consumables", "pamhc2foodextended", 1, 20),

            // --- FARMER'S DELIGHT (PRECIOS DE COMIDA INTACTOS) ---
            new Entry("fd_iron_knife", "farmersdelight:iron_knife", 35, 1, "awakening_system.shop.item.fd_iron_knife", "tools", "farmersdelight"),
            new Entry("fd_diamond_knife", "farmersdelight:diamond_knife", 120, 1, "awakening_system.shop.item.fd_diamond_knife", "tools", "farmersdelight", 1, 45),
            new Entry("fd_netherite_knife", "farmersdelight:netherite_knife", 450, 1, "awakening_system.shop.item.fd_netherite_knife", "tools", "farmersdelight", 1, "minecraft:the_nether", 75),
            new Entry("fd_skillet", "farmersdelight:skillet", 75, 1, "awakening_system.shop.item.fd_skillet", "weapons", "farmersdelight"),
            new Entry("fd_cooking_pot", "farmersdelight:cooking_pot", 85, 1, "awakening_system.shop.item.fd_cooking_pot", "utility", "farmersdelight"),
            new Entry("fd_cutting_board", "farmersdelight:cutting_board", 28, 1, "awakening_system.shop.item.fd_cutting_board", "utility", "farmersdelight"),
            new Entry("fd_stove", "farmersdelight:stove", 110, 1, "awakening_system.shop.item.fd_stove", "utility", "farmersdelight"),
            new Entry("fd_rich_soil", "farmersdelight:rich_soil", 55, 8, "awakening_system.shop.item.fd_rich_soil", "materials", "farmersdelight"),
            new Entry("fd_canvas", "farmersdelight:canvas", 40, 4, "awakening_system.shop.item.fd_canvas", "materials", "farmersdelight"),
            new Entry("fd_tomato_seeds", "farmersdelight:tomato_seeds", 20, 4, "awakening_system.shop.item.fd_tomato_seeds", "materials", "farmersdelight"),
            new Entry("fd_cabbage_seeds", "farmersdelight:cabbage_seeds", 20, 4, "awakening_system.shop.item.fd_cabbage_seeds", "materials", "farmersdelight"),
            new Entry("fd_rice", "farmersdelight:rice", 20, 8, "awakening_system.shop.item.fd_rice", "materials", "farmersdelight"),
            new Entry("fd_hamburger", "farmersdelight:hamburger", 40, 2, "awakening_system.shop.item.fd_hamburger", "consumables", "farmersdelight"),
            new Entry("fd_pasta", "farmersdelight:pasta_with_meatballs", 50, 2, "awakening_system.shop.item.fd_pasta", "consumables", "farmersdelight"),
            new Entry("fd_steak_potatoes", "farmersdelight:steak_and_potatoes", 55, 2, "awakening_system.shop.item.fd_steak_potatoes", "consumables", "farmersdelight"),
            new Entry("fd_roast_chicken", "farmersdelight:roast_chicken_block", 95, 1, "awakening_system.shop.item.fd_roast_chicken", "consumables", "farmersdelight"),
            new Entry("fd_apple_cider", "farmersdelight:apple_cider", 35, 2, "awakening_system.shop.item.fd_apple_cider", "consumables", "farmersdelight"),
            new Entry("fd_hot_cocoa", "farmersdelight:hot_cocoa", 35, 2, "awakening_system.shop.item.fd_hot_cocoa", "consumables", "farmersdelight"),
            new Entry("fd_melon_juice", "farmersdelight:melon_juice", 35, 2, "awakening_system.shop.item.fd_melon_juice", "consumables", "farmersdelight"),
            new Entry("fd_bacon_and_eggs", "farmersdelight:bacon_and_eggs", 45, 2, "item.farmersdelight.bacon_and_eggs", "consumables", "farmersdelight"),
            new Entry("fd_noodle_soup", "farmersdelight:noodle_soup", 50, 2, "item.farmersdelight.noodle_soup", "consumables", "farmersdelight"),
            new Entry("fd_chicken_soup", "farmersdelight:chicken_soup", 45, 2, "item.farmersdelight.chicken_soup", "consumables", "farmersdelight"),
            new Entry("fd_vegetable_soup", "farmersdelight:vegetable_soup", 40, 2, "item.farmersdelight.vegetable_soup", "consumables", "farmersdelight"),
            new Entry("fd_fish_stew", "farmersdelight:fish_stew", 50, 2, "item.farmersdelight.fish_stew", "consumables", "farmersdelight"),
            new Entry("fd_fried_rice", "farmersdelight:fried_rice", 45, 2, "item.farmersdelight.fried_rice", "consumables", "farmersdelight"),
            new Entry("fd_bone_broth", "farmersdelight:bone_broth", 35, 2, "item.farmersdelight.bone_broth", "consumables", "farmersdelight"),
            new Entry("fd_grilled_salmon", "farmersdelight:grilled_salmon", 55, 2, "item.farmersdelight.grilled_salmon", "consumables", "farmersdelight"),
            new Entry("fd_ratatouille", "farmersdelight:ratatouille", 45, 2, "item.farmersdelight.ratatouille", "consumables", "farmersdelight"),
            new Entry("fd_squid_ink_pasta", "farmersdelight:squid_ink_pasta", 60, 2, "item.farmersdelight.squid_ink_pasta", "consumables", "farmersdelight"),
            new Entry("fd_mutton_wrap", "farmersdelight:mutton_wrap", 50, 2, "item.farmersdelight.mutton_wrap", "consumables", "farmersdelight"),
            new Entry("fd_barbecue_stick", "farmersdelight:barbecue_stick", 40, 4, "item.farmersdelight.barbecue_stick", "consumables", "farmersdelight"),
            new Entry("fd_dumplings", "farmersdelight:dumplings", 45, 4, "item.farmersdelight.dumplings", "consumables", "farmersdelight"),
            new Entry("fd_stuffed_potato", "farmersdelight:stuffed_potato", 40, 4, "item.farmersdelight.stuffed_potato", "consumables", "farmersdelight"),
            new Entry("fd_cabbage_rolls", "farmersdelight:cabbage_rolls", 40, 4, "item.farmersdelight.cabbage_rolls", "consumables", "farmersdelight"),
            new Entry("fd_apple_pie", "farmersdelight:apple_pie_slice", 30, 4, "item.farmersdelight.apple_pie_slice", "consumables", "farmersdelight"),
            new Entry("fd_sweet_berry_cheesecake", "farmersdelight:sweet_berry_cheesecake_slice", 30, 4, "item.farmersdelight.sweet_berry_cheesecake_slice", "consumables", "farmersdelight"),
            new Entry("fd_chocolate_pie", "farmersdelight:chocolate_pie_slice", 30, 4, "item.farmersdelight.chocolate_pie_slice", "consumables", "farmersdelight"),
            new Entry("fd_glow_berry_custard", "farmersdelight:glow_berry_custard", 40, 2, "item.farmersdelight.glow_berry_custard", "consumables", "farmersdelight"),
            new Entry("fd_fruit_salad", "farmersdelight:fruit_salad", 35, 2, "item.farmersdelight.fruit_salad", "consumables", "farmersdelight"),
            new Entry("fd_nether_salad", "farmersdelight:nether_salad", 45, 2, "item.farmersdelight.nether_salad", "consumables", "farmersdelight", 1, "minecraft:the_nether", 30),
            new Entry("fd_stuffed_pumpkin", "farmersdelight:stuffed_pumpkin_block", 120, 1, "block.farmersdelight.stuffed_pumpkin_block", "consumables", "farmersdelight"),
            new Entry("fd_honey_glazed_ham", "farmersdelight:honey_glazed_ham_block", 130, 1, "block.farmersdelight.honey_glazed_ham_block", "consumables", "farmersdelight"),
            new Entry("fd_shepherds_pie_block", "farmersdelight:shepherds_pie_block", 120, 1, "block.farmersdelight.shepherds_pie_block", "consumables", "farmersdelight"),

            // --- SOPHISTICATED BACKPACKS (MOD COMPATIBILITY) ---
            new Entry("sb_backpack", "sophisticatedbackpacks:backpack", 650, 1, "item.sophisticatedbackpacks.backpack", "utility", "sophisticatedbackpacks"),
            new Entry("sb_copper_backpack", "sophisticatedbackpacks:copper_backpack", 1500, 1, "item.sophisticatedbackpacks.copper_backpack", "utility", "sophisticatedbackpacks", 1, 15),
            new Entry("sb_iron_backpack", "sophisticatedbackpacks:iron_backpack", 3800, 1, "item.sophisticatedbackpacks.iron_backpack", "utility", "sophisticatedbackpacks", 1, 25),
            new Entry("sb_gold_backpack", "sophisticatedbackpacks:gold_backpack", 9500, 1, "item.sophisticatedbackpacks.gold_backpack", "utility", "sophisticatedbackpacks", 1, 35),
            new Entry("sb_diamond_backpack", "sophisticatedbackpacks:diamond_backpack", 26000, 1, "item.sophisticatedbackpacks.diamond_backpack", "utility", "sophisticatedbackpacks", 1, 50),
            new Entry("sb_netherite_backpack", "sophisticatedbackpacks:netherite_backpack", 75000, 1, "item.sophisticatedbackpacks.netherite_backpack", "utility", "sophisticatedbackpacks", 1, "minecraft:the_nether", 75),
            new Entry("sb_upgrade_base", "sophisticatedbackpacks:upgrade_base", 350, 1, "item.sophisticatedbackpacks.upgrade_base", "utility", "sophisticatedbackpacks"),
            new Entry("sb_pickup_upgrade", "sophisticatedbackpacks:pickup_upgrade", 850, 1, "item.sophisticatedbackpacks.pickup_upgrade", "utility", "sophisticatedbackpacks", 1, 10),
            new Entry("sb_magnet_upgrade", "sophisticatedbackpacks:magnet_upgrade", 1800, 1, "item.sophisticatedbackpacks.magnet_upgrade", "utility", "sophisticatedbackpacks", 1, 20),
            new Entry("sb_feeding_upgrade", "sophisticatedbackpacks:feeding_upgrade", 950, 1, "item.sophisticatedbackpacks.feeding_upgrade", "utility", "sophisticatedbackpacks", 1, 10),
            new Entry("sb_compacting_upgrade", "sophisticatedbackpacks:compacting_upgrade", 2400, 1, "item.sophisticatedbackpacks.compacting_upgrade", "utility", "sophisticatedbackpacks", 1, 20),
            new Entry("sb_void_upgrade", "sophisticatedbackpacks:void_upgrade", 1200, 1, "item.sophisticatedbackpacks.void_upgrade", "utility", "sophisticatedbackpacks", 1, 15),
            new Entry("sb_smelting_upgrade", "sophisticatedbackpacks:smelting_upgrade", 2000, 1, "item.sophisticatedbackpacks.smelting_upgrade", "utility", "sophisticatedbackpacks", 1, 20),
            new Entry("sb_crafting_upgrade", "sophisticatedbackpacks:crafting_upgrade", 850, 1, "item.sophisticatedbackpacks.crafting_upgrade", "utility", "sophisticatedbackpacks"),
            new Entry("sb_stack_upgrade_tier_1", "sophisticatedbackpacks:stack_upgrade_tier_1", 3800, 1, "item.sophisticatedbackpacks.stack_upgrade_tier_1", "utility", "sophisticatedbackpacks", 1, 25),
            new Entry("sb_stack_upgrade_tier_2", "sophisticatedbackpacks:stack_upgrade_tier_2", 11000, 1, "item.sophisticatedbackpacks.stack_upgrade_tier_2", "utility", "sophisticatedbackpacks", 1, 40)
    );

    private ShopDefinitions() {}

    public static List<Entry> entries() { return ENTRIES; }

    public static Entry get(String id) {
        return ENTRIES.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
    }

    public static boolean isAvailable(Entry entry) {
        if (entry == null) return false;
        if (!entry.requiredMod().isEmpty()) {
            if ("apotheosis".equals(entry.requiredMod())) {
                if (!ModList.get().isLoaded("apotheosis") && !ModList.get().isLoaded("apothic_enchanting") && !ModList.get().isLoaded("apothic_attributes")) {
                    return false;
                }
            } else if ("pamhc2foodextended".equals(entry.requiredMod())) {
                if (!ModList.get().isLoaded("pamhc2foodextended") && !ModList.get().isLoaded("pamhc2foodcore")) {
                    return false;
                }
            } else if (!ModList.get().isLoaded(entry.requiredMod())) {
                return false;
            }
        }
        return !createStack(entry).isEmpty();
    }

    public static boolean matchesCategory(Entry entry, String category) {
        if (category == null || category.isEmpty() || "all".equalsIgnoreCase(category)) return true;
        
        if ("relics".equalsIgnoreCase(category)) return "relics".equals(entry.category());
        
        if ("boxes".equalsIgnoreCase(category)) return "boxes".equals(entry.category());
        if ("gear".equalsIgnoreCase(category)) {
            return "weapons".equals(entry.category()) || "equipment".equals(entry.category())
                    || "armor".equals(entry.category()) || "tools".equals(entry.category())
                    || "relics".equals(entry.category()) || "utility".equals(entry.category());
        }
        if ("consumables".equalsIgnoreCase(category)) {
            return "consumables".equals(entry.category()) || "rare".equals(entry.category());
        }
        if ("materials".equalsIgnoreCase(category)) {
            return "materials".equals(entry.category());
        }
        return category.equalsIgnoreCase(entry.category());
    }

    public static List<Entry> availableEntries() {
        return availableEntries("all");
    }

    public static List<Entry> availableEntries(String category) {
        return ENTRIES.stream()
                .filter(ShopDefinitions::isAvailable)
                .filter(entry -> matchesCategory(entry, category))
                .toList();
    }

    public static ItemStack createStack(Entry entry) {
        if (entry == null) return ItemStack.EMPTY;
        ResourceLocation id = ResourceLocation.parse(entry.itemId());
        if (BuiltInRegistries.ITEM.containsKey(id)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(id), entry.amount());
        }
        // Fallback para modulaciones de Apotheosis (apothic_enchanting / apothic_attributes)
        if ("apotheosis".equals(id.getNamespace())) {
            ResourceLocation fallbackEnch = ResourceLocation.fromNamespaceAndPath("apothic_enchanting", id.getPath());
            if (BuiltInRegistries.ITEM.containsKey(fallbackEnch)) {
                return new ItemStack(BuiltInRegistries.ITEM.get(fallbackEnch), entry.amount());
            }
            ResourceLocation fallbackAttr = ResourceLocation.fromNamespaceAndPath("apothic_attributes", id.getPath());
            if (BuiltInRegistries.ITEM.containsKey(fallbackAttr)) {
                return new ItemStack(BuiltInRegistries.ITEM.get(fallbackAttr), entry.amount());
            }
        }
        // Fallback para Pam's HarvestCraft 2 (pamhc2foodcore / pamhc2foodextended)
        if ("pamhc2foodextended".equals(id.getNamespace())) {
            ResourceLocation fallbackPam = ResourceLocation.fromNamespaceAndPath("pamhc2foodcore", id.getPath());
            if (BuiltInRegistries.ITEM.containsKey(fallbackPam)) {
                return new ItemStack(BuiltInRegistries.ITEM.get(fallbackPam), entry.amount());
            }
        }
        return ItemStack.EMPTY;
    }

    public static int mobReward(double maxHealth, boolean hostile) {
        int reward = Math.max(1, (int) Math.ceil(maxHealth / 10.0D));
        if (hostile) reward += 1;
        return Math.min(25, reward);
    }

    public static boolean isUnlocked(Entry entry, SystemData data) {
        if (entry == null || !isAvailable(entry)) return false;
        if (data == null) return entry.requiredSystemTier() <= 1 && entry.requiredDimension().isEmpty() && entry.minLevel() <= 0;

        // 1. Bloqueo estricto por Nivel de Jugador
        if (entry.minLevel() > 0 && data.level() < entry.minLevel()) {
            return false;
        }

        // 2. Bloqueo estricto por Dimensión (ambos requisitos de nivel y dimensión deben cumplirse obligatoriamente)
        if (!entry.requiredDimension().isEmpty()) {
            if (!data.hasVisitedDimension(entry.requiredDimension())) {
                return false;
            }
        }

        // 3. Bloqueo por Tier de Sistema
        if (entry.requiredSystemTier() <= 1) return true;
        boolean has40 = data.system40Updated() || data.isSystem40Updating() || data.teyvatUnlocked();
        boolean has30 = has40 || data.system30Updated() || data.isSystem30Updating() || data.murimUnlocked();
        boolean has20 = has30 || data.systemUpdated() || data.isSystemUpdating() || data.isekaiUnlocked();
        if (entry.requiredSystemTier() == 2) {
            return has20;
        }
        if (entry.requiredSystemTier() == 3) {
            return has30;
        }
        if (entry.requiredSystemTier() >= 4) {
            return has40 || data.hasVisitedDimension("awakening_system:teyvat_world") || data.hasVisitedDimension("awakening_system:teyvat")
                    || data.conqueredRankerTower() || data.savedTowerFloor() >= 50 || data.dantianRealm() >= 3 || data.conqueredNineTribulationsPagoda();
        }
        return true;
    }
}
