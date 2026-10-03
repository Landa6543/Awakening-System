import os

sell_code = '''package com.soybe.awakeningsystem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;

public final class SellDefinitions {
    public record SellEntry(String id, String itemId, int unitPrice, int amount, String nameKey, String category, String requiredMod) {
        public SellEntry(String id, String itemId, int unitPrice, int amount, String nameKey, String category) {
            this(id, itemId, unitPrice, amount, nameKey, category, "");
        }
    }

    private static final List<SellEntry> ENTRIES = List.of(
            // ==========================================
            // === MINERALES, LINGOTES Y BLOQUES (ORES) ===
            // ==========================================
            new SellEntry("sell_coal", "minecraft:coal", 1, 1, "awakening_system.sell.item.coal", "ores"),
            new SellEntry("sell_raw_iron", "minecraft:raw_iron", 2, 1, "awakening_system.sell.item.raw_iron", "ores"),
            new SellEntry("sell_iron_ingot", "minecraft:iron_ingot", 3, 1, "awakening_system.sell.item.iron_ingot", "ores"),
            new SellEntry("sell_raw_copper", "minecraft:raw_copper", 1, 2, "awakening_system.sell.item.raw_copper", "ores"),
            new SellEntry("sell_copper_ingot", "minecraft:copper_ingot", 1, 2, "awakening_system.sell.item.copper_ingot", "ores"),
            new SellEntry("sell_raw_gold", "minecraft:raw_gold", 3, 1, "awakening_system.sell.item.raw_gold", "ores"),
            new SellEntry("sell_gold_ingot", "minecraft:gold_ingot", 4, 1, "awakening_system.sell.item.gold_ingot", "ores"),
            new SellEntry("sell_redstone", "minecraft:redstone", 1, 2, "awakening_system.sell.item.redstone", "ores"),
            new SellEntry("sell_lapis", "minecraft:lapis_lazuli", 1, 2, "awakening_system.sell.item.lapis", "ores"),
            new SellEntry("sell_diamond", "minecraft:diamond", 12, 1, "awakening_system.sell.item.diamond", "ores"),
            new SellEntry("sell_emerald", "minecraft:emerald", 10, 1, "awakening_system.sell.item.emerald", "ores"),
            new SellEntry("sell_quartz", "minecraft:quartz", 1, 2, "awakening_system.sell.item.quartz", "ores"),
            new SellEntry("sell_amethyst", "minecraft:amethyst_shard", 2, 1, "awakening_system.sell.item.amethyst", "ores"),
            new SellEntry("sell_netherite_scrap", "minecraft:netherite_scrap", 35, 1, "awakening_system.sell.item.netherite_scrap", "ores"),
            new SellEntry("sell_netherite_ingot", "minecraft:netherite_ingot", 150, 1, "awakening_system.sell.item.netherite_ingot", "ores"),

            // Bloques de Minerales
            new SellEntry("sell_iron_block", "minecraft:iron_block", 27, 1, "block.minecraft.iron_block", "ores"),
            new SellEntry("sell_gold_block", "minecraft:gold_block", 36, 1, "block.minecraft.gold_block", "ores"),
            new SellEntry("sell_diamond_block", "minecraft:diamond_block", 108, 1, "block.minecraft.diamond_block", "ores"),
            new SellEntry("sell_emerald_block", "minecraft:emerald_block", 90, 1, "block.minecraft.emerald_block", "ores"),
            new SellEntry("sell_copper_block", "minecraft:copper_block", 9, 1, "block.minecraft.copper_block", "ores"),
            new SellEntry("sell_redstone_block", "minecraft:redstone_block", 9, 1, "block.minecraft.redstone_block", "ores"),
            new SellEntry("sell_lapis_block", "minecraft:lapis_block", 9, 1, "block.minecraft.lapis_block", "ores"),
            new SellEntry("sell_amethyst_block", "minecraft:amethyst_block", 8, 1, "block.minecraft.amethyst_block", "ores"),

            // Metales Post-Netherite del Sistema
            new SellEntry("sell_raw_mithril", "awakening_system:raw_mithril", 15, 1, "item.awakening_system.raw_mithril", "ores"),
            new SellEntry("sell_mithril_ingot", "awakening_system:mithril_ingot", 22, 1, "item.awakening_system.mithril_ingot", "ores"),
            new SellEntry("sell_raw_adamantite", "awakening_system:raw_adamantite", 24, 1, "item.awakening_system.raw_adamantite", "ores"),
            new SellEntry("sell_adamantite_ingot", "awakening_system:adamantite_ingot", 36, 1, "item.awakening_system.adamantite_ingot", "ores"),
            new SellEntry("sell_raw_spirit_jade", "awakening_system:raw_spirit_jade", 38, 1, "item.awakening_system.raw_spirit_jade", "ores"),
            new SellEntry("sell_spirit_jade", "awakening_system:spirit_jade", 58, 1, "item.awakening_system.spirit_jade", "ores"),
            new SellEntry("sell_raw_astral", "awakening_system:raw_astral", 50, 1, "item.awakening_system.raw_astral", "ores"),
            new SellEntry("sell_astral_ingot", "awakening_system:astral_ingot", 85, 1, "item.awakening_system.astral_ingot", "ores"),

            // Create Metalúrgica y Procesamiento (si Create está instalado)
            new SellEntry("sell_create_andesite_alloy", "create:andesite_alloy", 4, 2, "item.create.andesite_alloy", "ores", "create"),
            new SellEntry("sell_create_raw_zinc", "create:raw_zinc", 3, 1, "item.create.raw_zinc", "ores", "create"),
            new SellEntry("sell_create_zinc_ingot", "create:zinc_ingot", 5, 1, "item.create.zinc_ingot", "ores", "create"),
            new SellEntry("sell_create_brass_ingot", "create:brass_ingot", 12, 1, "item.create.brass_ingot", "ores", "create"),
            new SellEntry("sell_create_crushed_iron", "create:crushed_raw_iron", 4, 1, "item.create.crushed_raw_iron", "ores", "create"),
            new SellEntry("sell_create_crushed_gold", "create:crushed_raw_gold", 6, 1, "item.create.crushed_raw_gold", "ores", "create"),
            new SellEntry("sell_create_crushed_copper", "create:crushed_raw_copper", 2, 1, "item.create.crushed_raw_copper", "ores", "create"),
            new SellEntry("sell_create_crushed_zinc", "create:crushed_raw_zinc", 4, 1, "item.create.crushed_raw_zinc", "ores", "create"),
            new SellEntry("sell_create_precision_mechanism", "create:precision_mechanism", 140, 1, "item.create.precision_mechanism", "ores", "create"),

            // ==========================================
            // === BOTÍN DE MONSTRUOS Y JEFES (DROPS) ===
            // ==========================================
            new SellEntry("sell_rotten_flesh", "minecraft:rotten_flesh", 1, 2, "awakening_system.sell.item.rotten_flesh", "drops"),
            new SellEntry("sell_bone", "minecraft:bone", 1, 1, "awakening_system.sell.item.bone", "drops"),
            new SellEntry("sell_string", "minecraft:string", 1, 1, "awakening_system.sell.item.string", "drops"),
            new SellEntry("sell_spider_eye", "minecraft:spider_eye", 2, 1, "awakening_system.sell.item.spider_eye", "drops"),
            new SellEntry("sell_gunpowder", "minecraft:gunpowder", 2, 1, "awakening_system.sell.item.gunpowder", "drops"),
            new SellEntry("sell_ender_pearl", "minecraft:ender_pearl", 4, 1, "awakening_system.sell.item.ender_pearl", "drops"),
            new SellEntry("sell_blaze_rod", "minecraft:blaze_rod", 6, 1, "awakening_system.sell.item.blaze_rod", "drops"),
            new SellEntry("sell_ghast_tear", "minecraft:ghast_tear", 16, 1, "awakening_system.sell.item.ghast_tear", "drops"),
            new SellEntry("sell_slime_ball", "minecraft:slime_ball", 3, 1, "awakening_system.sell.item.slime_ball", "drops"),
            new SellEntry("sell_slime_block", "minecraft:slime_block", 25, 1, "block.minecraft.slime_block", "drops"),
            new SellEntry("sell_breeze_rod", "minecraft:breeze_rod", 8, 1, "awakening_system.sell.item.breeze_rod", "drops"),
            new SellEntry("sell_wind_charge", "minecraft:wind_charge", 2, 4, "item.minecraft.wind_charge", "drops"),
            new SellEntry("sell_shulker_shell", "minecraft:shulker_shell", 25, 1, "awakening_system.sell.item.shulker_shell", "drops"),
            new SellEntry("sell_wither_skull", "minecraft:wither_skeleton_skull", 45, 1, "awakening_system.sell.item.wither_skull", "drops"),
            new SellEntry("sell_nether_star", "minecraft:nether_star", 220, 1, "awakening_system.sell.item.nether_star", "drops"),
            new SellEntry("sell_totem", "minecraft:totem_of_undying", 130, 1, "awakening_system.sell.item.totem", "drops"),
            new SellEntry("sell_dragon_head", "minecraft:dragon_head", 350, 1, "block.minecraft.dragon_head", "drops"),
            new SellEntry("sell_dragon_egg", "minecraft:dragon_egg", 1500, 1, "block.minecraft.dragon_egg", "drops"),
            new SellEntry("sell_elytra", "minecraft:elytra", 450, 1, "item.minecraft.elytra", "drops"),
            new SellEntry("sell_heavy_core", "minecraft:heavy_core", 260, 1, "item.minecraft.heavy_core", "drops"),
            new SellEntry("sell_ominous_bottle", "minecraft:ominous_bottle", 65, 1, "item.minecraft.ominous_bottle", "drops"),
            new SellEntry("sell_trial_key", "minecraft:trial_key", 35, 1, "item.minecraft.trial_key", "drops"),
            new SellEntry("sell_ominous_trial_key", "minecraft:ominous_trial_key", 90, 1, "item.minecraft.ominous_trial_key", "drops"),
            new SellEntry("sell_nautilus_shell", "minecraft:nautilus_shell", 25, 1, "item.minecraft.nautilus_shell", "drops"),
            new SellEntry("sell_heart_of_sea", "minecraft:heart_of_the_sea", 130, 1, "item.minecraft.heart_of_the_sea", "drops"),
            new SellEntry("sell_echo_shard", "minecraft:echo_shard", 30, 1, "item.minecraft.echo_shard", "drops"),
            new SellEntry("sell_sculk_catalyst", "minecraft:sculk_catalyst", 45, 1, "block.minecraft.sculk_catalyst", "drops"),
            new SellEntry("sell_magma_cream", "minecraft:magma_cream", 5, 1, "item.minecraft.magma_cream", "drops"),
            new SellEntry("sell_phantom_membrane", "minecraft:phantom_membrane", 8, 1, "item.minecraft.phantom_membrane", "drops"),
            new SellEntry("sell_rabbit_foot", "minecraft:rabbit_foot", 15, 1, "item.minecraft.rabbit_foot", "drops"),
            new SellEntry("sell_prismarine_crystals", "minecraft:prismarine_crystals", 4, 2, "item.minecraft.prismarine_crystals", "drops"),
            new SellEntry("sell_prismarine_shard", "minecraft:prismarine_shard", 2, 2, "item.minecraft.prismarine_shard", "drops"),

            // Drops y Llaves de Mazmorras de Awakening System
            new SellEntry("sell_dungeon_key", "awakening_system:dungeon_key", 45, 1, "awakening_system.shop.item.dungeon_key", "drops"),
            new SellEntry("sell_ranker_ticket", "awakening_system:ranker_tower_ticket", 140, 1, "item.awakening_system.ranker_tower_ticket", "drops"),
            new SellEntry("sell_roulette_token", "awakening_system:roulette_token", 650, 1, "item.awakening_system.roulette_token", "drops"),

            // Píldoras de Cultivo Murim
            new SellEntry("sell_marrow_cleansing_pill", "awakening_system:marrow_cleansing_pill", 95, 1, "item.awakening_system.marrow_cleansing_pill", "drops"),
            new SellEntry("sell_rising_dragon_pill", "awakening_system:rising_dragon_pill", 150, 1, "item.awakening_system.rising_dragon_pill", "drops"),
            new SellEntry("sell_realm_breakthrough_pill", "awakening_system:realm_breakthrough_pill", 240, 1, "item.awakening_system.realm_breakthrough_pill", "drops"),
            new SellEntry("sell_black_tortoise_pill", "awakening_system:black_tortoise_pill", 190, 1, "item.awakening_system.black_tortoise_pill", "drops"),
            new SellEntry("sell_soul_transformation_pill", "awakening_system:soul_transformation_pill", 320, 1, "item.awakening_system.soul_transformation_pill", "drops"),

            // ==========================================
            // === AGRICULTURA, COMIDA Y PESCA (FARMING) ===
            // ==========================================
            new SellEntry("sell_wheat", "minecraft:wheat", 1, 2, "awakening_system.sell.item.wheat", "farming"),
            new SellEntry("sell_carrot", "minecraft:carrot", 1, 2, "awakening_system.sell.item.carrot", "farming"),
            new SellEntry("sell_potato", "minecraft:potato", 1, 2, "awakening_system.sell.item.potato", "farming"),
            new SellEntry("sell_beetroot", "minecraft:beetroot", 1, 2, "awakening_system.sell.item.beetroot", "farming"),
            new SellEntry("sell_sweet_berries", "minecraft:sweet_berries", 1, 4, "awakening_system.sell.item.sweet_berries", "farming"),
            new SellEntry("sell_glow_berries", "minecraft:glow_berries", 1, 2, "awakening_system.sell.item.glow_berries", "farming"),
            new SellEntry("sell_apple", "minecraft:apple", 2, 1, "awakening_system.sell.item.apple", "farming"),
            new SellEntry("sell_melon", "minecraft:melon_slice", 1, 4, "awakening_system.sell.item.melon", "farming"),
            new SellEntry("sell_pumpkin", "minecraft:pumpkin", 1, 1, "awakening_system.sell.item.pumpkin", "farming"),
            new SellEntry("sell_bread", "minecraft:bread", 1, 2, "item.minecraft.bread", "farming"),
            new SellEntry("sell_cake", "minecraft:cake", 12, 1, "block.minecraft.cake", "farming"),
            new SellEntry("sell_cooked_beef", "minecraft:cooked_beef", 2, 1, "awakening_system.sell.item.cooked_beef", "farming"),
            new SellEntry("sell_cooked_porkchop", "minecraft:cooked_porkchop", 2, 1, "awakening_system.sell.item.cooked_porkchop", "farming"),
            new SellEntry("sell_cooked_mutton", "minecraft:cooked_mutton", 2, 1, "awakening_system.sell.item.cooked_mutton", "farming"),
            new SellEntry("sell_golden_carrot", "minecraft:golden_carrot", 6, 1, "awakening_system.sell.item.golden_carrot", "farming"),
            new SellEntry("sell_golden_apple", "minecraft:golden_apple", 22, 1, "awakening_system.sell.item.golden_apple", "farming"),
            new SellEntry("sell_honey_bottle", "minecraft:honey_bottle", 4, 1, "item.minecraft.honey_bottle", "farming"),
            new SellEntry("sell_honeycomb", "minecraft:honeycomb", 3, 2, "item.minecraft.honeycomb", "farming"),
            new SellEntry("sell_honey_block", "minecraft:honey_block", 18, 1, "block.minecraft.honey_block", "farming"),

            // Pesca
            new SellEntry("sell_cod", "minecraft:cod", 2, 1, "item.minecraft.cod", "farming"),
            new SellEntry("sell_cooked_cod", "minecraft:cooked_cod", 3, 1, "item.minecraft.cooked_cod", "farming"),
            new SellEntry("sell_salmon", "minecraft:salmon", 2, 1, "item.minecraft.salmon", "farming"),
            new SellEntry("sell_cooked_salmon", "minecraft:cooked_salmon", 3, 1, "item.minecraft.cooked_salmon", "farming"),
            new SellEntry("sell_tropical_fish", "minecraft:tropical_fish", 6, 1, "item.minecraft.tropical_fish", "farming"),
            new SellEntry("sell_pufferfish", "minecraft:pufferfish", 8, 1, "item.minecraft.pufferfish", "farming"),

            // Fauna y Vegetación de Maná de Awakening System
            new SellEntry("sell_mana_beef", "awakening_system:mana_beef", 8, 1, "item.awakening_system.mana_beef", "farming"),
            new SellEntry("sell_cooked_mana_beef", "awakening_system:cooked_mana_beef", 14, 1, "item.awakening_system.cooked_mana_beef", "farming"),
            new SellEntry("sell_mana_porkchop", "awakening_system:mana_porkchop", 8, 1, "item.awakening_system.mana_porkchop", "farming"),
            new SellEntry("sell_cooked_mana_porkchop", "awakening_system:cooked_mana_porkchop", 14, 1, "item.awakening_system.cooked_mana_porkchop", "farming"),
            new SellEntry("sell_mana_mutton", "awakening_system:mana_mutton", 7, 1, "item.awakening_system.mana_mutton", "farming"),
            new SellEntry("sell_cooked_mana_mutton", "awakening_system:cooked_mana_mutton", 12, 1, "item.awakening_system.cooked_mana_mutton", "farming"),
            new SellEntry("sell_mana_chicken", "awakening_system:mana_chicken_meat", 6, 1, "item.awakening_system.mana_chicken_meat", "farming"),
            new SellEntry("sell_cooked_mana_chicken", "awakening_system:cooked_mana_chicken_meat", 10, 1, "item.awakening_system.cooked_mana_chicken_meat", "farming"),
            new SellEntry("sell_mana_milk", "awakening_system:mana_milk_bucket", 15, 1, "item.awakening_system.mana_milk_bucket", "farming"),
            new SellEntry("sell_mandrake_root", "awakening_system:mandrake_root", 28, 1, "item.awakening_system.mandrake_root", "farming"),
            new SellEntry("sell_yggdrasil_seed", "awakening_system:yggdrasil_heart_seed", 160, 1, "item.awakening_system.yggdrasil_heart_seed", "farming"),

            // Create Alimentos y Cocción
            new SellEntry("sell_create_blaze_cake", "create:blaze_cake", 75, 1, "item.create.blaze_cake", "farming", "create"),

            // Farmer\'s Delight
            new SellEntry("sell_fd_tomato", "farmersdelight:tomato", 1, 1, "awakening_system.sell.item.fd_tomato", "farming", "farmersdelight"),
            new SellEntry("sell_fd_onion", "farmersdelight:onion", 1, 1, "awakening_system.sell.item.fd_onion", "farming", "farmersdelight"),
            new SellEntry("sell_fd_cabbage", "farmersdelight:cabbage", 1, 1, "awakening_system.sell.item.fd_cabbage", "farming", "farmersdelight"),
            new SellEntry("sell_fd_rice", "farmersdelight:rice", 1, 1, "awakening_system.sell.item.fd_rice", "farming", "farmersdelight"),
            new SellEntry("sell_fd_straw", "farmersdelight:straw", 1, 4, "awakening_system.sell.item.fd_straw", "farming", "farmersdelight"),
            new SellEntry("sell_fd_bacon", "farmersdelight:bacon", 2, 1, "awakening_system.sell.item.fd_bacon", "farming", "farmersdelight"),
            new SellEntry("sell_fd_ham", "farmersdelight:ham", 6, 1, "awakening_system.sell.item.fd_ham", "farming", "farmersdelight"),
            new SellEntry("sell_fd_hamburger", "farmersdelight:hamburger", 8, 1, "awakening_system.sell.item.fd_hamburger", "farming", "farmersdelight"),
            new SellEntry("sell_fd_pasta", "farmersdelight:pasta_with_meatballs", 10, 1, "awakening_system.sell.item.fd_pasta", "farming", "farmersdelight"),
            new SellEntry("sell_fd_steak_potatoes", "farmersdelight:steak_and_potatoes", 10, 1, "awakening_system.sell.item.fd_steak_potatoes", "farming", "farmersdelight"),
            new SellEntry("sell_fd_chicken_soup", "farmersdelight:chicken_soup", 8, 1, "awakening_system.sell.item.fd_chicken_soup", "farming", "farmersdelight"),

            // ==========================================
            // === BLOQUES, MADERA Y CONSTRUCCIÓN (BLOCKS) ===
            // ==========================================
            new SellEntry("sell_oak_log", "minecraft:oak_log", 1, 2, "awakening_system.sell.item.oak_log", "blocks"),
            new SellEntry("sell_spruce_log", "minecraft:spruce_log", 1, 2, "awakening_system.sell.item.spruce_log", "blocks"),
            new SellEntry("sell_birch_log", "minecraft:birch_log", 1, 2, "awakening_system.sell.item.birch_log", "blocks"),
            new SellEntry("sell_jungle_log", "minecraft:jungle_log", 1, 2, "block.minecraft.jungle_log", "blocks"),
            new SellEntry("sell_acacia_log", "minecraft:acacia_log", 1, 2, "block.minecraft.acacia_log", "blocks"),
            new SellEntry("sell_dark_oak_log", "minecraft:dark_oak_log", 1, 2, "block.minecraft.dark_oak_log", "blocks"),
            new SellEntry("sell_cherry_log", "minecraft:cherry_log", 1, 2, "block.minecraft.cherry_log", "blocks"),
            new SellEntry("sell_mangrove_log", "minecraft:mangrove_log", 1, 2, "block.minecraft.mangrove_log", "blocks"),
            new SellEntry("sell_cobblestone", "minecraft:cobblestone", 1, 16, "awakening_system.sell.item.cobblestone", "blocks"),
            new SellEntry("sell_deepslate", "minecraft:cobbled_deepslate", 1, 16, "awakening_system.sell.item.deepslate", "blocks"),
            new SellEntry("sell_obsidian", "minecraft:obsidian", 2, 1, "awakening_system.sell.item.obsidian", "blocks"),
            new SellEntry("sell_glass", "minecraft:glass", 1, 4, "block.minecraft.glass", "blocks"),
            new SellEntry("sell_terracotta", "minecraft:terracotta", 1, 4, "block.minecraft.terracotta", "blocks"),
            new SellEntry("sell_sea_lantern", "minecraft:sea_lantern", 8, 1, "block.minecraft.sea_lantern", "blocks"),
            new SellEntry("sell_glowstone", "minecraft:glowstone", 4, 1, "block.minecraft.glowstone", "blocks"),
            new SellEntry("sell_shroomlight", "minecraft:shroomlight", 4, 1, "block.minecraft.shroomlight", "blocks"),
            new SellEntry("sell_feather", "minecraft:feather", 1, 2, "awakening_system.sell.item.feather", "blocks"),
            new SellEntry("sell_leather", "minecraft:leather", 2, 1, "awakening_system.sell.item.leather", "blocks"),
            new SellEntry("sell_fd_rich_soil", "farmersdelight:rich_soil", 2, 1, "awakening_system.sell.item.fd_rich_soil", "blocks", "farmersdelight"),
            new SellEntry("sell_create_cogwheel", "create:cogwheel", 3, 1, "block.create.cogwheel", "blocks", "create"),
            new SellEntry("sell_create_large_cogwheel", "create:large_cogwheel", 6, 1, "block.create.large_cogwheel", "blocks", "create")
    );

    private SellDefinitions() {}

    public static List<SellEntry> entries() {
        return ENTRIES;
    }

    public static SellEntry get(String id) {
        return ENTRIES.stream().filter(e -> e.id().equals(id)).findFirst().orElse(null);
    }

    public static boolean isAvailable(SellEntry entry) {
        if (entry == null) return false;
        if (!entry.requiredMod().isEmpty() && !ModList.get().isLoaded(entry.requiredMod())) return false;
        ResourceLocation rl = ResourceLocation.parse(entry.itemId());
        return BuiltInRegistries.ITEM.containsKey(rl);
    }

    public static boolean matchesCategory(SellEntry entry, String category) {
        if (category == null || category.isEmpty() || "all".equalsIgnoreCase(category)) return true;
        return category.equalsIgnoreCase(entry.category());
    }

    public static List<SellEntry> availableEntries(String category) {
        return ENTRIES.stream()
                .filter(SellDefinitions::isAvailable)
                .filter(entry -> matchesCategory(entry, category))
                .toList();
    }

    public static int countInInventory(Player player, SellEntry entry) {
        if (player == null || entry == null) return 0;
        ResourceLocation rl = ResourceLocation.parse(entry.itemId());
        if (!BuiltInRegistries.ITEM.containsKey(rl)) return 0;
        var targetItem = BuiltInRegistries.ITEM.get(rl);
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(targetItem)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static ItemStack createStack(SellEntry entry) {
        if (entry == null) return ItemStack.EMPTY;
        ResourceLocation id = ResourceLocation.parse(entry.itemId());
        if (!BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.get(id), entry.amount());
    }
}
'''

with open('src/main/java/com/soybe/awakeningsystem/SellDefinitions.java', 'w', encoding='utf-8') as f:
    f.write(sell_code)

print("SellDefinitions updated successfully")
