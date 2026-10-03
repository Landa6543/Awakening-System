import os

shop_code = '''package com.soybe.awakeningsystem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;

public final class ShopDefinitions {
    public record Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod, int requiredSystemTier) {
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, String requiredMod) {
            this(id, itemId, cost, amount, nameKey, category, requiredMod, 1);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category) {
            this(id, itemId, cost, amount, nameKey, category, "", 1);
        }
        public Entry(String id, String itemId, int cost, int amount, String nameKey, String category, int requiredSystemTier) {
            this(id, itemId, cost, amount, nameKey, category, "", requiredSystemTier);
        }
    }

    private static final List<Entry> ENTRIES = List.of(
            // === CAJAS MISTERIOSAS Y TICKETS (Precios rebalanceados al alza) ===
            new Entry("box_common", "minecraft:chest", 150, 1, "awakening_system.shop.item.box_common", "boxes"),
            new Entry("box_rare", "minecraft:trapped_chest", 450, 1, "awakening_system.shop.item.box_rare", "boxes"),
            new Entry("box_epic", "minecraft:ender_chest", 1800, 1, "awakening_system.shop.item.box_epic", "boxes"),
            new Entry("box_legendary", "minecraft:shulker_box", 5500, 1, "awakening_system.shop.item.box_legendary", "boxes"),
            new Entry("lucky_box", "minecraft:bundle", 350, 1, "awakening_system.shop.item.lucky_box", "boxes"),
            new Entry("roulette_token", "awakening_system:roulette_token", 2500, 1, "item.awakening_system.roulette_token", "boxes"),
            new Entry("dungeon_key", "awakening_system:dungeon_key", 220, 1, "awakening_system.shop.item.dungeon_key", "boxes"),
            new Entry("ranker_tower_ticket", "awakening_system:ranker_tower_ticket", 750, 1, "item.awakening_system.ranker_tower_ticket", "boxes", 2),
            new Entry("roulette_altar", "awakening_system:roulette_altar", 14000, 1, "block.awakening_system.roulette_altar", "equipment", 2),
            new Entry("death_king_clock", "awakening_system:death_king_clock", 2800, 1, "awakening_system.shop.item.death_king_clock", "equipment", 2),

            // === ARMAS BÁSICAS Y AVANZADAS ===
            new Entry("iron_sword", "minecraft:iron_sword", 35, 1, "awakening_system.shop.item.iron_sword", "weapons"),
            new Entry("diamond_sword", "minecraft:diamond_sword", 160, 1, "awakening_system.shop.item.diamond_sword", "weapons"),
            new Entry("netherite_sword", "minecraft:netherite_sword", 550, 1, "awakening_system.shop.item.netherite_sword", "weapons"),
            new Entry("mace", "minecraft:mace", 950, 1, "awakening_system.shop.item.mace", "weapons"),
            new Entry("trident", "minecraft:trident", 650, 1, "awakening_system.shop.item.trident", "weapons"),
            new Entry("bow", "minecraft:bow", 70, 1, "awakening_system.shop.item.bow", "weapons"),
            new Entry("crossbow", "minecraft:crossbow", 110, 1, "awakening_system.shop.item.crossbow", "weapons"),
            new Entry("shield", "minecraft:shield", 50, 1, "awakening_system.shop.item.shield", "equipment"),

            // === ESPADAS Y ARMAS LEGENDARIAS MANHWA & MURIM ===
            new Entry("plum_blossom_sword", "awakening_system:plum_blossom_sword", 2800, 1, "item.awakening_system.plum_blossom_sword", "weapons", 3),
            new Entry("gigantic_greatsword", "awakening_system:gigantic_greatsword", 2600, 1, "item.awakening_system.gigantic_greatsword", "weapons", 2),
            new Entry("transcendent_thorn", "awakening_system:transcendent_thorn", 3000, 1, "item.awakening_system.transcendent_thorn", "weapons", 2),
            new Entry("dawns_ballad", "awakening_system:dawns_ballad", 3200, 1, "item.awakening_system.dawns_ballad", "weapons", 2),
            new Entry("shadow_daggers", "awakening_system:shadow_daggers", 3600, 1, "item.awakening_system.shadow_daggers", "weapons", 1),
            new Entry("radiant_light_sword", "awakening_system:radiant_light_sword", 2600, 1, "item.awakening_system.radiant_light_sword", "weapons", 1),
            new Entry("sky_demon_sword", "awakening_system:sky_demon_sword", 4200, 1, "item.awakening_system.sky_demon_sword", "weapons", 3),
            new Entry("heaven_shaking_sword", "awakening_system:heaven_shaking_sword", 4500, 1, "item.awakening_system.heaven_shaking_sword", "weapons", 3),
            new Entry("asrahan_holy_sword", "awakening_system:asrahan_holy_sword", 3100, 1, "item.awakening_system.asrahan_holy_sword", "weapons", 2),
            new Entry("unbreakable_faith", "awakening_system:unbreakable_faith", 2800, 1, "item.awakening_system.unbreakable_faith", "weapons", 2),
            new Entry("black_dragon_scythe", "awakening_system:black_dragon_scythe", 5000, 1, "item.awakening_system.black_dragon_scythe", "weapons", 2),

            // === ARMADURAS VANILLA ===
            new Entry("leather_helmet", "minecraft:leather_helmet", 30, 1, "awakening_system.shop.item.leather_helmet", "armor"),
            new Entry("leather_chestplate", "minecraft:leather_chestplate", 55, 1, "awakening_system.shop.item.leather_chestplate", "armor"),
            new Entry("leather_leggings", "minecraft:leather_leggings", 45, 1, "awakening_system.shop.item.leather_leggings", "armor"),
            new Entry("leather_boots", "minecraft:leather_boots", 25, 1, "awakening_system.shop.item.leather_boots", "armor"),
            new Entry("chainmail_helmet", "minecraft:chainmail_helmet", 50, 1, "awakening_system.shop.item.chainmail_helmet", "armor"),
            new Entry("chainmail_chestplate", "minecraft:chainmail_chestplate", 100, 1, "awakening_system.shop.item.chainmail_chestplate", "armor"),
            new Entry("chainmail_leggings", "minecraft:chainmail_leggings", 80, 1, "awakening_system.shop.item.chainmail_leggings", "armor"),
            new Entry("chainmail_boots", "minecraft:chainmail_boots", 45, 1, "awakening_system.shop.item.chainmail_boots", "armor"),
            new Entry("iron_helmet", "minecraft:iron_helmet", 80, 1, "awakening_system.shop.item.iron_helmet", "armor"),
            new Entry("iron_chestplate", "minecraft:iron_chestplate", 160, 1, "awakening_system.shop.item.iron_chestplate", "armor"),
            new Entry("iron_leggings", "minecraft:iron_leggings", 130, 1, "awakening_system.shop.item.iron_leggings", "armor"),
            new Entry("iron_boots", "minecraft:iron_boots", 70, 1, "awakening_system.shop.item.iron_boots", "armor"),
            new Entry("golden_helmet", "minecraft:golden_helmet", 70, 1, "awakening_system.shop.item.golden_helmet", "armor"),
            new Entry("golden_chestplate", "minecraft:golden_chestplate", 130, 1, "awakening_system.shop.item.golden_chestplate", "armor"),
            new Entry("golden_leggings", "minecraft:golden_leggings", 110, 1, "awakening_system.shop.item.golden_leggings", "armor"),
            new Entry("golden_boots", "minecraft:golden_boots", 60, 1, "awakening_system.shop.item.golden_boots", "armor"),
            new Entry("diamond_helmet", "minecraft:diamond_helmet", 180, 1, "awakening_system.shop.item.diamond_helmet", "armor"),
            new Entry("diamond_chestplate", "minecraft:diamond_chestplate", 320, 1, "awakening_system.shop.item.diamond_chestplate", "equipment"),
            new Entry("diamond_leggings", "minecraft:diamond_leggings", 240, 1, "awakening_system.shop.item.diamond_leggings", "armor"),
            new Entry("diamond_boots", "minecraft:diamond_boots", 160, 1, "awakening_system.shop.item.diamond_boots", "armor"),
            new Entry("netherite_helmet", "minecraft:netherite_helmet", 600, 1, "awakening_system.shop.item.netherite_helmet", "armor"),
            new Entry("netherite_chestplate", "minecraft:netherite_chestplate", 900, 1, "awakening_system.shop.item.netherite_chestplate", "equipment"),
            new Entry("netherite_leggings", "minecraft:netherite_leggings", 750, 1, "awakening_system.shop.item.netherite_leggings", "armor"),
            new Entry("netherite_boots", "minecraft:netherite_boots", 550, 1, "awakening_system.shop.item.netherite_boots", "armor"),

            // === ARMADURAS TEMÁTICAS MANHWA ===
            new Entry("shadow_monarch_chestplate", "awakening_system:shadow_monarch_chestplate", 2800, 1, "item.awakening_system.shadow_monarch_chestplate", "armor", 1),
            new Entry("shadow_monarch_leggings", "awakening_system:shadow_monarch_leggings", 2200, 1, "item.awakening_system.shadow_monarch_leggings", "armor", 1),
            new Entry("shadow_monarch_boots", "awakening_system:shadow_monarch_boots", 1800, 1, "item.awakening_system.shadow_monarch_boots", "armor", 1),
            new Entry("kim_dokja_coat", "awakening_system:kim_dokja_coat", 2900, 1, "item.awakening_system.kim_dokja_coat", "armor", 2),
            new Entry("kim_dokja_boots", "awakening_system:kim_dokja_boots", 1800, 1, "item.awakening_system.kim_dokja_boots", "armor", 2),
            new Entry("mount_hua_headband", "awakening_system:mount_hua_headband", 1500, 1, "item.awakening_system.mount_hua_headband", "armor", 3),
            new Entry("mount_hua_robe", "awakening_system:mount_hua_robe", 2500, 1, "item.awakening_system.mount_hua_robe", "armor", 3),
            new Entry("mount_hua_pants", "awakening_system:mount_hua_pants", 2100, 1, "item.awakening_system.mount_hua_pants", "armor", 3),
            new Entry("mount_hua_sandals", "awakening_system:mount_hua_sandals", 1500, 1, "item.awakening_system.mount_hua_sandals", "armor", 3),
            new Entry("valter_berserker_chestplate", "awakening_system:valter_berserker_chestplate", 2900, 1, "item.awakening_system.valter_berserker_chestplate", "armor", 2),
            new Entry("valter_berserker_leggings", "awakening_system:valter_berserker_leggings", 2400, 1, "item.awakening_system.valter_berserker_leggings", "armor", 2),
            new Entry("valter_berserker_boots", "awakening_system:valter_berserker_boots", 1800, 1, "item.awakening_system.valter_berserker_boots", "armor", 2),

            // === PIEDRAS DE RUNA Y RELIQUIAS ===
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
            new Entry("fable_star_fragment", "minecraft:echo_shard", 850, 1, "awakening_system.shop.item.fable_star_fragment", "rare", 1),
            new Entry("constellation_nectar", "minecraft:dragon_breath", 350, 1, "awakening_system.shop.item.constellation_nectar", "consumables", 1),
            new Entry("fable_scroll", "minecraft:paper", 1400, 1, "awakening_system.shop.item.fable_scroll", "rare", 2),

            // === CATÁLOGO EXPANDIDO DEL MOD CREATE (Ingeniería y Mecatrónica) ===
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
            new Entry("create_crushing_wheel", "create:crushing_wheel", 850, 2, "awakening_system.shop.item.create_crushing_wheel", "equipment", "create"),
            new Entry("create_blaze_burner", "create:blaze_burner", 550, 1, "awakening_system.shop.item.create_blaze_burner", "equipment", "create"),
            new Entry("create_blaze_cake", "create:blaze_cake", 260, 2, "awakening_system.shop.item.create_blaze_cake", "consumables", "create"),
            new Entry("create_electron_tube", "create:electron_tube", 160, 4, "awakening_system.shop.item.create_electron_tube", "materials", "create"),
            new Entry("create_precision_mechanism", "create:precision_mechanism", 420, 1, "awakening_system.shop.item.create_precision_mechanism", "rare", "create"),
            new Entry("create_steam_engine", "create:steam_engine", 650, 1, "awakening_system.shop.item.create_steam_engine", "equipment", "create"),
            new Entry("create_fluid_pipe", "create:fluid_pipe", 75, 16, "awakening_system.shop.item.create_fluid_pipe", "utility", "create"),
            new Entry("create_mechanical_pump", "create:mechanical_pump", 160, 1, "awakening_system.shop.item.create_mechanical_pump", "equipment", "create"),
            new Entry("create_fluid_tank", "create:fluid_tank", 200, 2, "awakening_system.shop.item.create_fluid_tank", "equipment", "create"),
            new Entry("create_spout", "create:spout", 260, 1, "awakening_system.shop.item.create_spout", "equipment", "create"),
            new Entry("create_item_vault", "create:item_vault", 320, 2, "awakening_system.shop.item.create_item_vault", "utility", "create"),
            new Entry("create_track_pack", "create:track", 110, 32, "awakening_system.shop.item.create_track_pack", "utility", "create"),
            new Entry("create_train_controls", "create:controls", 240, 1, "awakening_system.shop.item.create_train_controls", "utility", "create"),
            new Entry("create_schematicannon", "create:schematicannon", 700, 1, "awakening_system.shop.item.create_schematicannon", "rare", "create"),

            // === CONSUMIBLES, COMIDA Y RAREZAS ===
            new Entry("golden_apple", "minecraft:golden_apple", 95, 1, "awakening_system.shop.item.golden_apple", "consumables"),
            new Entry("enchanted_golden_apple", "minecraft:enchanted_golden_apple", 650, 1, "awakening_system.shop.item.enchanted_golden_apple", "rare"),
            new Entry("totem_of_undying", "minecraft:totem_of_undying", 950, 1, "awakening_system.shop.item.totem", "rare"),
            new Entry("nether_star", "minecraft:nether_star", 3200, 1, "awakening_system.shop.item.nether_star", "rare"),
            new Entry("elytra", "minecraft:elytra", 1500, 1, "awakening_system.shop.item.elytra", "rare"),
            new Entry("heavy_core", "minecraft:heavy_core", 800, 1, "awakening_system.shop.item.heavy_core", "rare"),
            new Entry("shulker_shell", "minecraft:shulker_shell", 180, 2, "awakening_system.shop.item.shulker_shell", "rare"),
            new Entry("ender_pearl", "minecraft:ender_pearl", 45, 4, "awakening_system.shop.item.ender_pearl", "consumables"),
            new Entry("experience_bottle", "minecraft:experience_bottle", 35, 4, "awakening_system.shop.item.experience_bottle", "consumables"),
            new Entry("attribute_reset", "minecraft:experience_bottle", 250, 1, "awakening_system.shop.item.attribute_reset", "consumables"),
            new Entry("energy_elixir", "minecraft:honey_bottle", 75, 1, "awakening_system.shop.item.energy_elixir", "consumables"),
            new Entry("cooked_beef", "minecraft:cooked_beef", 25, 8, "awakening_system.shop.item.cooked_beef", "consumables"),
            new Entry("cooked_porkchop", "minecraft:cooked_porkchop", 25, 8, "awakening_system.shop.item.cooked_porkchop", "consumables"),
            new Entry("bread", "minecraft:bread", 20, 16, "awakening_system.shop.item.bread", "consumables"),
            new Entry("golden_carrot", "minecraft:golden_carrot", 32, 4, "awakening_system.shop.item.golden_carrot", "consumables"),
            new Entry("honey_bottle", "minecraft:honey_bottle", 35, 4, "awakening_system.shop.item.honey_bottle", "consumables"),
            new Entry("chorus_fruit", "minecraft:chorus_fruit", 30, 8, "awakening_system.shop.item.chorus_fruit", "consumables"),

            // === HERRAMIENTAS Y UTILIDAD ===
            new Entry("diamond_pickaxe", "minecraft:diamond_pickaxe", 180, 1, "awakening_system.shop.item.diamond_pickaxe", "tools"),
            new Entry("diamond_axe", "minecraft:diamond_axe", 170, 1, "awakening_system.shop.item.diamond_axe", "tools"),
            new Entry("diamond_shovel", "minecraft:diamond_shovel", 90, 1, "awakening_system.shop.item.diamond_shovel", "tools"),
            new Entry("iron_pickaxe", "minecraft:iron_pickaxe", 45, 1, "awakening_system.shop.item.iron_pickaxe", "tools"),
            new Entry("iron_axe", "minecraft:iron_axe", 45, 1, "awakening_system.shop.item.iron_axe", "tools"),
            new Entry("iron_shovel", "minecraft:iron_shovel", 28, 1, "awakening_system.shop.item.iron_shovel", "tools"),
            new Entry("fishing_rod", "minecraft:fishing_rod", 35, 1, "awakening_system.shop.item.fishing_rod", "tools"),
            new Entry("water_bucket", "minecraft:water_bucket", 55, 1, "awakening_system.shop.item.water_bucket", "utility"),
            new Entry("lava_bucket", "minecraft:lava_bucket", 95, 1, "awakening_system.shop.item.lava_bucket", "utility"),
            new Entry("firework_rocket", "minecraft:firework_rocket", 25, 8, "awakening_system.shop.item.firework_rocket", "utility"),
            new Entry("saddle", "minecraft:saddle", 80, 1, "awakening_system.shop.item.saddle", "utility"),
            new Entry("name_tag", "minecraft:name_tag", 50, 1, "awakening_system.shop.item.name_tag", "utility"),
            new Entry("spyglass", "minecraft:spyglass", 50, 1, "awakening_system.shop.item.spyglass", "utility"),
            new Entry("trial_key", "minecraft:trial_key", 150, 1, "awakening_system.shop.item.trial_key", "rare"),
            new Entry("ominous_trial_key", "minecraft:ominous_trial_key", 320, 1, "awakening_system.shop.item.ominous_trial_key", "rare"),
            new Entry("heart_of_the_sea", "minecraft:heart_of_the_sea", 420, 1, "awakening_system.shop.item.heart_of_the_sea", "rare"),
            new Entry("breeze_rod", "minecraft:breeze_rod", 80, 4, "awakening_system.shop.item.breeze_rod", "materials"),
            new Entry("wind_charge", "minecraft:wind_charge", 45, 8, "awakening_system.shop.item.wind_charge", "utility"),
            new Entry("dragon_breath", "minecraft:dragon_breath", 75, 2, "awakening_system.shop.item.dragon_breath", "materials"),

            // === MATERIALES BÁSICOS ===
            new Entry("diamond", "minecraft:diamond", 110, 4, "awakening_system.shop.item.diamond", "materials"),
            new Entry("netherite_scrap", "minecraft:netherite_scrap", 190, 1, "awakening_system.shop.item.netherite_scrap", "materials"),
            new Entry("iron_ingot", "minecraft:iron_ingot", 45, 4, "awakening_system.shop.item.iron_ingot", "materials"),
            new Entry("gold_ingot", "minecraft:gold_ingot", 55, 4, "awakening_system.shop.item.gold_ingot", "materials"),
            new Entry("copper_ingot", "minecraft:copper_ingot", 30, 8, "awakening_system.shop.item.copper_ingot", "materials"),
            new Entry("lapis_lazuli", "minecraft:lapis_lazuli", 35, 16, "awakening_system.shop.item.lapis_lazuli", "materials"),
            new Entry("redstone", "minecraft:redstone", 35, 16, "awakening_system.shop.item.redstone", "materials"),
            new Entry("quartz", "minecraft:quartz", 35, 16, "awakening_system.shop.item.quartz", "materials"),
            new Entry("blaze_rod", "minecraft:blaze_rod", 65, 2, "awakening_system.shop.item.blaze_rod", "materials"),
            new Entry("slime_ball", "minecraft:slime_ball", 45, 4, "awakening_system.shop.item.slime_ball", "materials"),
            new Entry("echo_shard", "minecraft:echo_shard", 150, 2, "awakening_system.shop.item.echo_shard", "materials"),
            new Entry("obsidian", "minecraft:obsidian", 65, 8, "awakening_system.shop.item.obsidian", "materials"),
            new Entry("emerald", "minecraft:emerald", 60, 2, "awakening_system.shop.item.emerald", "materials"),
            new Entry("amethyst_shard", "minecraft:amethyst_shard", 45, 4, "awakening_system.shop.item.amethyst_shard", "materials"),

            // === COMPATIBILIDAD CON OTROS MODS POPULARES ===
            new Entry("cataclysm_ignitium_ingot", "cataclysm:ignitium_ingot", 280, 2, "awakening_system.shop.item.cataclysm_ignitium", "materials"),
            new Entry("aether_ambrosium", "aether:ambrosium_shard", 75, 4, "awakening_system.shop.item.aether_ambrosium", "materials"),
            new Entry("twilight_carminite", "twilightforest:carminite", 110, 4, "awakening_system.shop.item.twilight_carminite", "materials"),
            new Entry("mowzie_sunmetal", "mowziesmobs:sunmetal_ingot", 240, 2, "awakening_system.shop.item.mowzie_sunmetal", "materials"),
            new Entry("undergarden_depthrock", "undergarden:depthrock", 65, 16, "awakening_system.shop.item.undergarden_depthrock", "materials"),
            new Entry("simplyswords_iron_long_sword", "simplyswords:iron_long_sword", 85, 1, "awakening_system.shop.item.simplyswords_iron_long_sword", "weapons", "simplyswords"),
            new Entry("simplyswords_diamond_long_sword", "simplyswords:diamond_long_sword", 260, 1, "awakening_system.shop.item.simplyswords_diamond_long_sword", "weapons", "simplyswords"),
            new Entry("simplyswords_netherite_long_sword", "simplyswords:netherite_long_sword", 700, 1, "awakening_system.shop.item.simplyswords_netherite_long_sword", "weapons", "simplyswords"),

            // === FARMER'S DELIGHT ===
            new Entry("fd_iron_knife", "farmersdelight:iron_knife", 35, 1, "awakening_system.shop.item.fd_iron_knife", "tools", "farmersdelight"),
            new Entry("fd_diamond_knife", "farmersdelight:diamond_knife", 120, 1, "awakening_system.shop.item.fd_diamond_knife", "tools", "farmersdelight"),
            new Entry("fd_netherite_knife", "farmersdelight:netherite_knife", 450, 1, "awakening_system.shop.item.fd_netherite_knife", "tools", "farmersdelight"),
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
            new Entry("fd_melon_juice", "farmersdelight:melon_juice", 35, 2, "awakening_system.shop.item.fd_melon_juice", "consumables", "farmersdelight")
    );

    private ShopDefinitions() {}

    public static List<Entry> entries() { return ENTRIES; }

    public static Entry get(String id) {
        return ENTRIES.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
    }

    public static boolean isAvailable(Entry entry) {
        return entry != null
                && (entry.requiredMod().isEmpty() || ModList.get().isLoaded(entry.requiredMod()))
                && !createStack(entry).isEmpty();
    }

    public static boolean matchesCategory(Entry entry, String category) {
        if (category == null || category.isEmpty() || "all".equalsIgnoreCase(category)) return true;
        if ("boxes".equalsIgnoreCase(category)) return "boxes".equals(entry.category());
        if ("gear".equalsIgnoreCase(category)) {
            return "weapons".equals(entry.category()) || "equipment".equals(entry.category())
                    || "armor".equals(entry.category()) || "tools".equals(entry.category());
        }
        if ("consumables".equalsIgnoreCase(category)) {
            return "consumables".equals(entry.category()) || "rare".equals(entry.category());
        }
        if ("materials".equalsIgnoreCase(category)) {
            return "materials".equals(entry.category()) || "utility".equals(entry.category());
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
        if (!BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id), entry.amount());
        return stack;
    }

    public static int mobReward(double maxHealth, boolean hostile) {
        int reward = Math.max(1, (int) Math.ceil(maxHealth / 10.0D));
        if (hostile) reward += 1;
        return Math.min(25, reward);
    }

    public static boolean isUnlocked(Entry entry, SystemData data) {
        if (entry == null) return false;
        if (entry.requiredSystemTier() <= 1) return true;
        if (entry.requiredSystemTier() == 2) {
            return data != null && (data.systemUpdated() || data.isekaiUnlocked());
        }
        if (entry.requiredSystemTier() == 3) {
            return data != null && (data.system30Updated() || data.murimUnlocked());
        }
        return true;
    }
}
'''

with open('src/main/java/com/soybe/awakeningsystem/ShopDefinitions.java', 'w', encoding='utf-8') as f:
    f.write(shop_code)

print("ShopDefinitions updated successfully")
