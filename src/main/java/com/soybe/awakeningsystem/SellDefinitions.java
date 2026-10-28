package com.soybe.awakeningsystem;

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
            // ==================== EQUIPO Y ARMAS (GEAR) ====================
            new SellEntry("sell_wooden_sword", "minecraft:wooden_sword", 10, 1, "item.minecraft.wooden_sword", "gear"),
            new SellEntry("sell_stone_sword", "minecraft:stone_sword", 20, 1, "item.minecraft.stone_sword", "gear"),
            new SellEntry("sell_iron_sword", "minecraft:iron_sword", 55, 1, "item.minecraft.iron_sword", "gear"),
            new SellEntry("sell_golden_sword", "minecraft:golden_sword", 40, 1, "item.minecraft.golden_sword", "gear"),
            new SellEntry("sell_diamond_sword", "minecraft:diamond_sword", 6200, 1, "item.minecraft.diamond_sword", "gear"),
            new SellEntry("sell_netherite_sword", "minecraft:netherite_sword", 42000, 1, "item.minecraft.netherite_sword", "gear"),
            new SellEntry("sell_bow", "minecraft:bow", 50, 1, "item.minecraft.bow", "gear"),
            new SellEntry("sell_crossbow", "minecraft:crossbow", 80, 1, "item.minecraft.crossbow", "gear"),
            new SellEntry("sell_trident", "minecraft:trident", 24000, 1, "item.minecraft.trident", "gear"),
            new SellEntry("sell_mace", "minecraft:mace", 34000, 1, "item.minecraft.mace", "gear"),

            new SellEntry("sell_iron_helmet", "minecraft:iron_helmet", 180, 1, "item.minecraft.iron_helmet", "gear"),
            new SellEntry("sell_iron_chestplate", "minecraft:iron_chestplate", 300, 1, "item.minecraft.iron_chestplate", "gear"),
            new SellEntry("sell_iron_leggings", "minecraft:iron_leggings", 250, 1, "item.minecraft.iron_leggings", "gear"),
            new SellEntry("sell_iron_boots", "minecraft:iron_boots", 150, 1, "item.minecraft.iron_boots", "gear"),

            new SellEntry("sell_golden_helmet", "minecraft:golden_helmet", 350, 1, "item.minecraft.golden_helmet", "gear"),
            new SellEntry("sell_golden_chestplate", "minecraft:golden_chestplate", 550, 1, "item.minecraft.golden_chestplate", "gear"),
            new SellEntry("sell_golden_leggings", "minecraft:golden_leggings", 480, 1, "item.minecraft.golden_leggings", "gear"),
            new SellEntry("sell_golden_boots", "minecraft:golden_boots", 300, 1, "item.minecraft.golden_boots", "gear"),

            new SellEntry("sell_diamond_helmet", "minecraft:diamond_helmet", 10500, 1, "item.minecraft.diamond_helmet", "gear"),
            new SellEntry("sell_diamond_chestplate", "minecraft:diamond_chestplate", 18000, 1, "item.minecraft.diamond_chestplate", "gear"),
            new SellEntry("sell_diamond_leggings", "minecraft:diamond_leggings", 14500, 1, "item.minecraft.diamond_leggings", "gear"),
            new SellEntry("sell_diamond_boots", "minecraft:diamond_boots", 9000, 1, "item.minecraft.diamond_boots", "gear"),

            // Armadura y Armas Abisales (Entre Diamante y Netherita)
            new SellEntry("sell_abyssal_helmet", "awakening_system:abyssal_helmet", 21000, 1, "item.awakening_system.abyssal_helmet", "gear"),
            new SellEntry("sell_abyssal_chestplate", "awakening_system:abyssal_chestplate", 33000, 1, "item.awakening_system.abyssal_chestplate", "gear"),
            new SellEntry("sell_abyssal_leggings", "awakening_system:abyssal_leggings", 26000, 1, "item.awakening_system.abyssal_leggings", "gear"),
            new SellEntry("sell_abyssal_boots", "awakening_system:abyssal_boots", 18000, 1, "item.awakening_system.abyssal_boots", "gear"),
            new SellEntry("sell_abyssal_sword", "awakening_system:abyssal_sword", 19000, 1, "item.awakening_system.abyssal_sword", "gear"),
            new SellEntry("sell_abyssal_pickaxe", "awakening_system:abyssal_pickaxe", 18000, 1, "item.awakening_system.abyssal_pickaxe", "gear"),
            new SellEntry("sell_abyssal_axe", "awakening_system:abyssal_axe", 16500, 1, "item.awakening_system.abyssal_axe", "gear"),
            new SellEntry("sell_abyssal_shovel", "awakening_system:abyssal_shovel", 10500, 1, "item.awakening_system.abyssal_shovel", "gear"),
            new SellEntry("sell_abyssal_hoe", "awakening_system:abyssal_hoe", 8000, 1, "item.awakening_system.abyssal_hoe", "gear"),

            new SellEntry("sell_netherite_helmet", "minecraft:netherite_helmet", 36000, 1, "item.minecraft.netherite_helmet", "gear"),
            new SellEntry("sell_netherite_chestplate", "minecraft:netherite_chestplate", 56000, 1, "item.minecraft.netherite_chestplate", "gear"),
            new SellEntry("sell_netherite_leggings", "minecraft:netherite_leggings", 46000, 1, "item.minecraft.netherite_leggings", "gear"),
            new SellEntry("sell_netherite_boots", "minecraft:netherite_boots", 34000, 1, "item.minecraft.netherite_boots", "gear"),

            // Armaduras de Aleacion de Almas y Astralita
            new SellEntry("sell_soul_alloy_helmet", "awakening_system:soul_alloy_helmet", 60000, 1, "item.awakening_system.soul_alloy_helmet", "gear"),
            new SellEntry("sell_soul_alloy_chestplate", "awakening_system:soul_alloy_chestplate", 93000, 1, "item.awakening_system.soul_alloy_chestplate", "gear"),
            new SellEntry("sell_soul_alloy_leggings", "awakening_system:soul_alloy_leggings", 78000, 1, "item.awakening_system.soul_alloy_leggings", "gear"),
            new SellEntry("sell_soul_alloy_boots", "awakening_system:soul_alloy_boots", 56000, 1, "item.awakening_system.soul_alloy_boots", "gear"),

            new SellEntry("sell_astralite_helmet", "awakening_system:astralite_helmet", 90000, 1, "item.awakening_system.astralite_helmet", "gear"),
            new SellEntry("sell_astralite_chestplate", "awakening_system:astralite_chestplate", 135000, 1, "item.awakening_system.astralite_chestplate", "gear"),
            new SellEntry("sell_astralite_leggings", "awakening_system:astralite_leggings", 112000, 1, "item.awakening_system.astralite_leggings", "gear"),
            new SellEntry("sell_astralite_boots", "awakening_system:astralite_boots", 82000, 1, "item.awakening_system.astralite_boots", "gear"),

            // Armaduras de Mundo 2 (Mithril y Adamantita)
            new SellEntry("sell_mithril_helmet", "awakening_system:mithril_helmet", 120000, 1, "item.awakening_system.mithril_helmet", "gear"),
            new SellEntry("sell_mithril_chestplate", "awakening_system:mithril_chestplate", 180000, 1, "item.awakening_system.mithril_chestplate", "gear"),
            new SellEntry("sell_mithril_leggings", "awakening_system:mithril_leggings", 150000, 1, "item.awakening_system.mithril_leggings", "gear"),
            new SellEntry("sell_mithril_boots", "awakening_system:mithril_boots", 110000, 1, "item.awakening_system.mithril_boots", "gear"),

            new SellEntry("sell_adamantite_helmet", "awakening_system:adamantite_helmet", 160000, 1, "item.awakening_system.adamantite_helmet", "gear"),
            new SellEntry("sell_adamantite_chestplate", "awakening_system:adamantite_chestplate", 240000, 1, "item.awakening_system.adamantite_chestplate", "gear"),
            new SellEntry("sell_adamantite_leggings", "awakening_system:adamantite_leggings", 200000, 1, "item.awakening_system.adamantite_leggings", "gear"),
            new SellEntry("sell_adamantite_boots", "awakening_system:adamantite_boots", 145000, 1, "item.awakening_system.adamantite_boots", "gear"),

            // Armaduras de Mundo 3 (Jade Espiritual y Metal Astral)
            new SellEntry("sell_spirit_jade_helmet", "awakening_system:spirit_jade_helmet", 210000, 1, "item.awakening_system.spirit_jade_helmet", "gear"),
            new SellEntry("sell_spirit_jade_chestplate", "awakening_system:spirit_jade_chestplate", 310000, 1, "item.awakening_system.spirit_jade_chestplate", "gear"),
            new SellEntry("sell_spirit_jade_leggings", "awakening_system:spirit_jade_leggings", 260000, 1, "item.awakening_system.spirit_jade_leggings", "gear"),
            new SellEntry("sell_spirit_jade_boots", "awakening_system:spirit_jade_boots", 190000, 1, "item.awakening_system.spirit_jade_boots", "gear"),

            new SellEntry("sell_astral_helmet", "awakening_system:astral_helmet", 280000, 1, "item.awakening_system.astral_helmet", "gear"),
            new SellEntry("sell_astral_chestplate", "awakening_system:astral_chestplate", 420000, 1, "item.awakening_system.astral_chestplate", "gear"),
            new SellEntry("sell_astral_leggings", "awakening_system:astral_leggings", 350000, 1, "item.awakening_system.astral_leggings", "gear"),
            new SellEntry("sell_astral_boots", "awakening_system:astral_boots", 250000, 1, "item.awakening_system.astral_boots", "gear"),

            // Armaduras de Mundo 4 (Celestial y Primordial)
            new SellEntry("sell_celestial_helmet", "awakening_system:celestial_helmet", 380000, 1, "item.awakening_system.celestial_helmet", "gear"),
            new SellEntry("sell_celestial_chestplate", "awakening_system:celestial_chestplate", 570000, 1, "item.awakening_system.celestial_chestplate", "gear"),
            new SellEntry("sell_celestial_leggings", "awakening_system:celestial_leggings", 480000, 1, "item.awakening_system.celestial_leggings", "gear"),
            new SellEntry("sell_celestial_boots", "awakening_system:celestial_boots", 340000, 1, "item.awakening_system.celestial_boots", "gear"),

            new SellEntry("sell_primordial_helmet", "awakening_system:primordial_helmet", 520000, 1, "item.awakening_system.primordial_helmet", "gear"),
            new SellEntry("sell_primordial_chestplate", "awakening_system:primordial_chestplate", 780000, 1, "item.awakening_system.primordial_chestplate", "gear"),
            new SellEntry("sell_primordial_leggings", "awakening_system:primordial_leggings", 650000, 1, "item.awakening_system.primordial_leggings", "gear"),
            new SellEntry("sell_primordial_boots", "awakening_system:primordial_boots", 460000, 1, "item.awakening_system.primordial_boots", "gear"),

            new SellEntry("sell_simplyswords_iron_long_sword", "simplyswords:iron_long_sword", 90, 1, "awakening_system.shop.item.simplyswords_iron_long_sword", "gear", "simplyswords"),
            new SellEntry("sell_simplyswords_diamond_long_sword", "simplyswords:diamond_long_sword", 2100, 1, "awakening_system.shop.item.simplyswords_diamond_long_sword", "gear", "simplyswords"),
            new SellEntry("sell_simplyswords_netherite_long_sword", "simplyswords:netherite_long_sword", 15000, 1, "awakening_system.shop.item.simplyswords_netherite_long_sword", "gear", "simplyswords"),

            new SellEntry("sell_fd_iron_knife", "farmersdelight:iron_knife", 25, 1, "awakening_system.shop.item.fd_iron_knife", "gear", "farmersdelight"),
            new SellEntry("sell_fd_diamond_knife", "farmersdelight:diamond_knife", 90, 1, "awakening_system.shop.item.fd_diamond_knife", "gear", "farmersdelight"),
            new SellEntry("sell_fd_netherite_knife", "farmersdelight:netherite_knife", 340, 1, "awakening_system.shop.item.fd_netherite_knife", "gear", "farmersdelight"),
            new SellEntry("sell_fd_skillet", "farmersdelight:skillet", 55, 1, "awakening_system.shop.item.fd_skillet", "gear", "farmersdelight"),

            // Sophisticated Backpacks
            new SellEntry("sell_sb_backpack", "sophisticatedbackpacks:backpack", 300, 1, "item.sophisticatedbackpacks.backpack", "gear", "sophisticatedbackpacks"),
            new SellEntry("sell_sb_copper_backpack", "sophisticatedbackpacks:copper_backpack", 700, 1, "item.sophisticatedbackpacks.copper_backpack", "gear", "sophisticatedbackpacks"),
            new SellEntry("sell_sb_iron_backpack", "sophisticatedbackpacks:iron_backpack", 1800, 1, "item.sophisticatedbackpacks.iron_backpack", "gear", "sophisticatedbackpacks"),
            new SellEntry("sell_sb_gold_backpack", "sophisticatedbackpacks:gold_backpack", 4500, 1, "item.sophisticatedbackpacks.gold_backpack", "gear", "sophisticatedbackpacks"),
            new SellEntry("sell_sb_diamond_backpack", "sophisticatedbackpacks:diamond_backpack", 13000, 1, "item.sophisticatedbackpacks.diamond_backpack", "gear", "sophisticatedbackpacks"),
            new SellEntry("sell_sb_netherite_backpack", "sophisticatedbackpacks:netherite_backpack", 38000, 1, "item.sophisticatedbackpacks.netherite_backpack", "gear", "sophisticatedbackpacks"),

            // ==================== MINERALES Y LINGOTES BALANCEADOS ====================
            new SellEntry("sell_coal", "minecraft:coal", 4, 1, "awakening_system.sell.item.coal", "ores"),
            new SellEntry("sell_raw_iron", "minecraft:raw_iron", 7, 1, "awakening_system.sell.item.raw_iron", "ores"),
            new SellEntry("sell_iron_ingot", "minecraft:iron_ingot", 9, 1, "awakening_system.sell.item.iron_ingot", "ores"),
            new SellEntry("sell_raw_copper", "minecraft:raw_copper", 2, 1, "awakening_system.sell.item.raw_copper", "ores"),
            new SellEntry("sell_copper_ingot", "minecraft:copper_ingot", 3, 1, "awakening_system.sell.item.copper_ingot", "ores"),
            new SellEntry("sell_raw_gold", "minecraft:raw_gold", 9, 1, "awakening_system.sell.item.raw_gold", "ores"),
            new SellEntry("sell_gold_ingot", "minecraft:gold_ingot", 11, 1, "awakening_system.sell.item.gold_ingot", "ores"),
            new SellEntry("sell_redstone", "minecraft:redstone", 3, 1, "awakening_system.sell.item.redstone", "ores"),
            new SellEntry("sell_lapis", "minecraft:lapis_lazuli", 2, 1, "awakening_system.sell.item.lapis", "ores"),
            new SellEntry("sell_diamond", "minecraft:diamond", 550, 1, "awakening_system.sell.item.diamond", "ores"),
            new SellEntry("sell_emerald", "minecraft:emerald", 40, 1, "awakening_system.sell.item.emerald", "ores"),
            new SellEntry("sell_quartz", "minecraft:quartz", 4, 1, "awakening_system.sell.item.quartz", "ores"),
            new SellEntry("sell_amethyst", "minecraft:amethyst_shard", 8, 1, "awakening_system.sell.item.amethyst", "ores"),
            new SellEntry("sell_netherite_scrap", "minecraft:netherite_scrap", 3000, 1, "awakening_system.sell.item.netherite_scrap", "ores"),
            new SellEntry("sell_netherite_ingot", "minecraft:netherite_ingot", 13500, 1, "awakening_system.sell.item.netherite_ingot", "ores"),

            new SellEntry("sell_coal_block", "minecraft:coal_block", 36, 1, "block.minecraft.coal_block", "ores"),
            new SellEntry("sell_iron_block", "minecraft:iron_block", 81, 1, "block.minecraft.iron_block", "ores"),
            new SellEntry("sell_gold_block", "minecraft:gold_block", 99, 1, "block.minecraft.gold_block", "ores"),
            new SellEntry("sell_diamond_block", "minecraft:diamond_block", 4950, 1, "block.minecraft.diamond_block", "ores"),
            new SellEntry("sell_netherite_block", "minecraft:netherite_block", 121500, 1, "block.minecraft.netherite_block", "ores"),
            new SellEntry("sell_emerald_block", "minecraft:emerald_block", 360, 1, "block.minecraft.emerald_block", "ores"),
            new SellEntry("sell_copper_block", "minecraft:copper_block", 27, 1, "block.minecraft.copper_block", "ores"),
            new SellEntry("sell_redstone_block", "minecraft:redstone_block", 27, 1, "block.minecraft.redstone_block", "ores"),
            new SellEntry("sell_lapis_block", "minecraft:lapis_block", 18, 1, "block.minecraft.lapis_block", "ores"),
            new SellEntry("sell_amethyst_block", "minecraft:amethyst_block", 32, 1, "block.minecraft.amethyst_block", "ores"),

            new SellEntry("sell_raw_abyssal_ore", "awakening_system:raw_abyssal_ore", 120, 1, "item.awakening_system.raw_abyssal_ore", "ores"),
            new SellEntry("sell_abyssal_ingot", "awakening_system:abyssal_ingot", 180, 1, "item.awakening_system.abyssal_ingot", "ores"),
            new SellEntry("sell_abyssal_block", "awakening_system:abyssal_block", 1620, 1, "block.awakening_system.abyssal_block", "ores"),

            new SellEntry("sell_raw_mithril", "awakening_system:raw_mithril", 220, 1, "item.awakening_system.raw_mithril", "ores"),
            new SellEntry("sell_mithril_ingot", "awakening_system:mithril_ingot", 350, 1, "item.awakening_system.mithril_ingot", "ores"),
            new SellEntry("sell_mithril_block", "awakening_system:mithril_block", 3150, 1, "block.awakening_system.mithril_block", "ores"),

            new SellEntry("sell_raw_adamantite", "awakening_system:raw_adamantite", 420, 1, "item.awakening_system.raw_adamantite", "ores"),
            new SellEntry("sell_adamantite_ingot", "awakening_system:adamantite_ingot", 650, 1, "item.awakening_system.adamantite_ingot", "ores"),
            new SellEntry("sell_adamantite_block", "awakening_system:adamantite_block", 5850, 1, "block.awakening_system.adamantite_block", "ores"),

            new SellEntry("sell_raw_spirit_jade", "awakening_system:raw_spirit_jade", 750, 1, "item.awakening_system.raw_spirit_jade", "ores"),
            new SellEntry("sell_spirit_jade", "awakening_system:spirit_jade", 1200, 1, "item.awakening_system.spirit_jade", "ores"),
            new SellEntry("sell_spirit_jade_block", "awakening_system:spirit_jade_block", 10800, 1, "block.awakening_system.spirit_jade_block", "ores"),

            new SellEntry("sell_raw_astral", "awakening_system:raw_astral", 1400, 1, "item.awakening_system.raw_astral", "ores"),
            new SellEntry("sell_astral_ingot", "awakening_system:astral_ingot", 2200, 1, "item.awakening_system.astral_ingot", "ores"),
            new SellEntry("sell_astral_block", "awakening_system:astral_block", 19800, 1, "block.awakening_system.astral_block", "ores"),

            new SellEntry("sell_raw_celestial", "awakening_system:raw_celestial", 2600, 1, "item.awakening_system.raw_celestial", "ores"),
            new SellEntry("sell_celestial_ingot", "awakening_system:celestial_ingot", 4000, 1, "item.awakening_system.celestial_ingot", "ores"),
            new SellEntry("sell_celestial_nugget", "awakening_system:celestial_nugget", 440, 1, "item.awakening_system.celestial_nugget", "ores"),
            new SellEntry("sell_celestial_block", "awakening_system:celestial_block", 36000, 1, "block.awakening_system.celestial_block", "ores"),

            new SellEntry("sell_raw_primordial", "awakening_system:raw_primordial", 5200, 1, "item.awakening_system.raw_primordial", "ores"),
            new SellEntry("sell_primordial_ingot", "awakening_system:primordial_ingot", 8000, 1, "item.awakening_system.primordial_ingot", "ores"),
            new SellEntry("sell_primordial_nugget", "awakening_system:primordial_nugget", 880, 1, "item.awakening_system.primordial_nugget", "ores"),
            new SellEntry("sell_primordial_block", "awakening_system:primordial_block", 72000, 1, "block.awakening_system.primordial_block", "ores"),

            // Cristales de Maná de la Gran Torre
            new SellEntry("sell_mana_crystal_shard_e", "awakening_system:mana_crystal_shard_e", 350, 1, "item.awakening_system.mana_crystal_shard_e", "ores"),
            new SellEntry("sell_mana_crystal_cluster_e", "awakening_system:mana_crystal_cluster_e", 1500, 1, "block.awakening_system.mana_crystal_cluster_e", "ores"),
            new SellEntry("sell_mana_crystal_shard_d", "awakening_system:mana_crystal_shard_d", 1200, 1, "item.awakening_system.mana_crystal_shard_d", "ores"),
            new SellEntry("sell_mana_crystal_cluster_d", "awakening_system:mana_crystal_cluster_d", 5000, 1, "block.awakening_system.mana_crystal_cluster_d", "ores"),
            new SellEntry("sell_mana_crystal_shard_c", "awakening_system:mana_crystal_shard_c", 3500, 1, "item.awakening_system.mana_crystal_shard_c", "ores"),
            new SellEntry("sell_mana_crystal_cluster_c", "awakening_system:mana_crystal_cluster_c", 15000, 1, "block.awakening_system.mana_crystal_cluster_c", "ores"),
            new SellEntry("sell_mana_crystal_shard_b", "awakening_system:mana_crystal_shard_b", 10000, 1, "item.awakening_system.mana_crystal_shard_b", "ores"),
            new SellEntry("sell_mana_crystal_cluster_b", "awakening_system:mana_crystal_cluster_b", 40000, 1, "block.awakening_system.mana_crystal_cluster_b", "ores"),
            new SellEntry("sell_mana_crystal_shard_a", "awakening_system:mana_crystal_shard_a", 25000, 1, "item.awakening_system.mana_crystal_shard_a", "ores"),
            new SellEntry("sell_mana_crystal_cluster_a", "awakening_system:mana_crystal_cluster_a", 100000, 1, "block.awakening_system.mana_crystal_cluster_a", "ores"),
            new SellEntry("sell_mana_crystal_shard_s", "awakening_system:mana_crystal_shard_s", 60000, 1, "item.awakening_system.mana_crystal_shard_s", "ores"),
            new SellEntry("sell_mana_crystal_cluster_s", "awakening_system:mana_crystal_cluster_s", 250000, 1, "block.awakening_system.mana_crystal_cluster_s", "ores"),

            new SellEntry("sell_white_iron_chunk", "awakening_system:white_iron_chunk", 3, 1, "item.awakening_system.white_iron_chunk", "ores"),
            new SellEntry("sell_crystal_chunk", "awakening_system:crystal_chunk", 6, 1, "item.awakening_system.crystal_chunk", "ores"),
            new SellEntry("sell_magical_crystal_chunk", "awakening_system:magical_crystal_chunk", 14, 1, "item.awakening_system.magical_crystal_chunk", "ores"),
            new SellEntry("sell_starsilver_chunk", "awakening_system:starsilver_chunk", 5, 1, "item.awakening_system.starsilver_chunk", "ores"),

            new SellEntry("sell_agnidus_sliver", "awakening_system:agnidus_agate_sliver", 8, 1, "item.awakening_system.agnidus_agate_sliver", "ores"),
            new SellEntry("sell_agnidus_fragment", "awakening_system:agnidus_agate_fragment", 24, 1, "item.awakening_system.agnidus_agate_fragment", "ores"),
            new SellEntry("sell_agnidus_chunk", "awakening_system:agnidus_agate_chunk", 72, 1, "item.awakening_system.agnidus_agate_chunk", "ores"),
            new SellEntry("sell_agnidus_gemstone", "awakening_system:agnidus_agate_gemstone", 216, 1, "item.awakening_system.agnidus_agate_gemstone", "ores"),

            new SellEntry("sell_varunada_sliver", "awakening_system:varunada_lazurite_sliver", 8, 1, "item.awakening_system.varunada_lazurite_sliver", "ores"),
            new SellEntry("sell_varunada_fragment", "awakening_system:varunada_lazurite_fragment", 24, 1, "item.awakening_system.varunada_lazurite_fragment", "ores"),
            new SellEntry("sell_varunada_chunk", "awakening_system:varunada_lazurite_chunk", 72, 1, "item.awakening_system.varunada_lazurite_chunk", "ores"),
            new SellEntry("sell_varunada_gemstone", "awakening_system:varunada_lazurite_gemstone", 216, 1, "item.awakening_system.varunada_lazurite_gemstone", "ores"),

            new SellEntry("sell_vajrada_sliver", "awakening_system:vajrada_amethyst_sliver", 8, 1, "item.awakening_system.vajrada_amethyst_sliver", "ores"),
            new SellEntry("sell_vajrada_fragment", "awakening_system:vajrada_amethyst_fragment", 24, 1, "item.awakening_system.vajrada_amethyst_fragment", "ores"),
            new SellEntry("sell_vajrada_chunk", "awakening_system:vajrada_amethyst_chunk", 72, 1, "item.awakening_system.vajrada_amethyst_chunk", "ores"),
            new SellEntry("sell_vajrada_gemstone", "awakening_system:vajrada_amethyst_gemstone", 216, 1, "item.awakening_system.vajrada_amethyst_gemstone", "ores"),

            new SellEntry("sell_vayuda_sliver", "awakening_system:vayuda_turquoise_sliver", 8, 1, "item.awakening_system.vayuda_turquoise_sliver", "ores"),
            new SellEntry("sell_vayuda_fragment", "awakening_system:vayuda_turquoise_fragment", 24, 1, "item.awakening_system.vayuda_turquoise_fragment", "ores"),
            new SellEntry("sell_vayuda_chunk", "awakening_system:vayuda_turquoise_chunk", 72, 1, "item.awakening_system.vayuda_turquoise_chunk", "ores"),
            new SellEntry("sell_vayuda_gemstone", "awakening_system:vayuda_turquoise_gemstone", 216, 1, "item.awakening_system.vayuda_turquoise_gemstone", "ores"),

            new SellEntry("sell_shivada_sliver", "awakening_system:shivada_jade_sliver", 8, 1, "item.awakening_system.shivada_jade_sliver", "ores"),
            new SellEntry("sell_shivada_fragment", "awakening_system:shivada_jade_fragment", 24, 1, "item.awakening_system.shivada_jade_fragment", "ores"),
            new SellEntry("sell_shivada_chunk", "awakening_system:shivada_jade_chunk", 72, 1, "item.awakening_system.shivada_jade_chunk", "ores"),
            new SellEntry("sell_shivada_gemstone", "awakening_system:shivada_jade_gemstone", 216, 1, "item.awakening_system.shivada_jade_gemstone", "ores"),

            new SellEntry("sell_prithiva_sliver", "awakening_system:prithiva_topaz_sliver", 8, 1, "item.awakening_system.prithiva_topaz_sliver", "ores"),
            new SellEntry("sell_prithiva_fragment", "awakening_system:prithiva_topaz_fragment", 24, 1, "item.awakening_system.prithiva_topaz_fragment", "ores"),
            new SellEntry("sell_prithiva_chunk", "awakening_system:prithiva_topaz_chunk", 72, 1, "item.awakening_system.prithiva_topaz_chunk", "ores"),
            new SellEntry("sell_prithiva_gemstone", "awakening_system:prithiva_topaz_gemstone", 216, 1, "item.awakening_system.prithiva_topaz_gemstone", "ores"),

            new SellEntry("sell_nagadus_sliver", "awakening_system:nagadus_emerald_sliver", 8, 1, "item.awakening_system.nagadus_emerald_sliver", "ores"),
            new SellEntry("sell_nagadus_fragment", "awakening_system:nagadus_emerald_fragment", 24, 1, "item.awakening_system.nagadus_emerald_fragment", "ores"),
            new SellEntry("sell_nagadus_chunk", "awakening_system:nagadus_emerald_chunk", 72, 1, "item.awakening_system.nagadus_emerald_chunk", "ores"),
            new SellEntry("sell_nagadus_gemstone", "awakening_system:nagadus_emerald_gemstone", 216, 1, "item.awakening_system.nagadus_emerald_gemstone", "ores"),

            new SellEntry("sell_create_andesite_alloy", "create:andesite_alloy", 4, 2, "item.create.andesite_alloy", "ores", "create"),
            new SellEntry("sell_create_raw_zinc", "create:raw_zinc", 3, 1, "item.create.raw_zinc", "ores", "create"),
            new SellEntry("sell_create_zinc_ingot", "create:zinc_ingot", 5, 1, "item.create.zinc_ingot", "ores", "create"),
            new SellEntry("sell_create_brass_ingot", "create:brass_ingot", 12, 1, "item.create.brass_ingot", "ores", "create"),
            new SellEntry("sell_create_crushed_iron", "create:crushed_raw_iron", 4, 1, "item.create.crushed_raw_iron", "ores", "create"),
            new SellEntry("sell_create_crushed_gold", "create:crushed_raw_gold", 6, 1, "item.create.crushed_raw_gold", "ores", "create"),
            new SellEntry("sell_create_crushed_copper", "create:crushed_raw_copper", 2, 1, "item.create.crushed_raw_copper", "ores", "create"),
            new SellEntry("sell_create_crushed_zinc", "create:crushed_raw_zinc", 4, 1, "item.create.crushed_raw_zinc", "ores", "create"),
            new SellEntry("sell_create_precision_mechanism", "create:precision_mechanism", 140, 1, "item.create.precision_mechanism", "ores", "create"),

            // ==================== BOTIN DE MONSTRUOS (DROPS) ====================
            new SellEntry("sell_leather", "minecraft:leather", 3, 1, "awakening_system.sell.item.leather", "drops"),
            new SellEntry("sell_feather", "minecraft:feather", 2, 1, "awakening_system.sell.item.feather", "drops"),
            new SellEntry("sell_flint", "minecraft:flint", 3, 1, "awakening_system.sell.item.flint", "drops"),
            new SellEntry("sell_clay_ball", "minecraft:clay_ball", 1, 2, "awakening_system.sell.item.clay_ball", "drops"),
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
            new SellEntry("sell_shulker_shell", "minecraft:shulker_shell", 120, 1, "awakening_system.sell.item.shulker_shell", "drops"),
            new SellEntry("sell_wither_skull", "minecraft:wither_skeleton_skull", 75, 1, "awakening_system.sell.item.wither_skull", "drops"),
            new SellEntry("sell_nether_star", "minecraft:nether_star", 1200, 1, "awakening_system.sell.item.nether_star", "drops"),
            new SellEntry("sell_totem", "minecraft:totem_of_undying", 1500, 1, "awakening_system.sell.item.totem", "drops"),
            new SellEntry("sell_dragon_head", "minecraft:dragon_head", 1000, 1, "block.minecraft.dragon_head", "drops"),
            new SellEntry("sell_dragon_egg", "minecraft:dragon_egg", 5000, 1, "block.minecraft.dragon_egg", "drops"),
            new SellEntry("sell_elytra", "minecraft:elytra", 6000, 1, "item.minecraft.elytra", "drops"),
            new SellEntry("sell_heavy_core", "minecraft:heavy_core", 2500, 1, "item.minecraft.heavy_core", "drops"),
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

            new SellEntry("sell_dungeon_key", "awakening_system:dungeon_key", 45, 1, "awakening_system.shop.item.dungeon_key", "drops"),
            new SellEntry("sell_ranker_ticket", "awakening_system:ranker_tower_ticket", 140, 1, "item.awakening_system.ranker_tower_ticket", "drops"),
            new SellEntry("sell_roulette_token", "awakening_system:roulette_token", 650, 1, "item.awakening_system.roulette_token", "drops"),

            new SellEntry("sell_marrow_cleansing_pill", "awakening_system:marrow_cleansing_pill", 95, 1, "item.awakening_system.marrow_cleansing_pill", "drops"),
            new SellEntry("sell_rising_dragon_pill", "awakening_system:rising_dragon_pill", 150, 1, "item.awakening_system.rising_dragon_pill", "drops"),
            new SellEntry("sell_realm_breakthrough_pill", "awakening_system:realm_breakthrough_pill", 240, 1, "item.awakening_system.realm_breakthrough_pill", "drops"),
            new SellEntry("sell_black_tortoise_pill", "awakening_system:black_tortoise_pill", 190, 1, "item.awakening_system.black_tortoise_pill", "drops"),
            new SellEntry("sell_soul_transformation_pill", "awakening_system:soul_transformation_pill", 320, 1, "item.awakening_system.soul_transformation_pill", "drops"),

            // Genshin Mob Drops
            new SellEntry("sell_damaged_mask", "awakening_system:damaged_mask", 2, 1, "item.awakening_system.damaged_mask", "drops"),
            new SellEntry("sell_stained_mask", "awakening_system:stained_mask", 6, 1, "item.awakening_system.stained_mask", "drops"),
            new SellEntry("sell_ominous_mask", "awakening_system:ominous_mask", 18, 1, "item.awakening_system.ominous_mask", "drops"),
            new SellEntry("sell_divining_scroll", "awakening_system:divining_scroll", 2, 1, "item.awakening_system.divining_scroll", "drops"),
            new SellEntry("sell_sealed_scroll", "awakening_system:sealed_scroll", 6, 1, "item.awakening_system.sealed_scroll", "drops"),
            new SellEntry("sell_forbidden_curse_scroll", "awakening_system:forbidden_curse_scroll", 18, 1, "item.awakening_system.forbidden_curse_scroll", "drops"),
            new SellEntry("sell_firm_arrowhead", "awakening_system:firm_arrowhead", 2, 1, "item.awakening_system.firm_arrowhead", "drops"),
            new SellEntry("sell_sharp_arrowhead", "awakening_system:sharp_arrowhead", 6, 1, "item.awakening_system.sharp_arrowhead", "drops"),
            new SellEntry("sell_weathered_arrowhead", "awakening_system:weathered_arrowhead", 18, 1, "item.awakening_system.weathered_arrowhead", "drops"),
            new SellEntry("sell_heavy_horn", "awakening_system:heavy_horn", 4, 1, "item.awakening_system.heavy_horn", "drops"),
            new SellEntry("sell_black_bronze_horn", "awakening_system:black_bronze_horn", 12, 1, "item.awakening_system.black_bronze_horn", "drops"),
            new SellEntry("sell_black_crystal_horn", "awakening_system:black_crystal_horn", 36, 1, "item.awakening_system.black_crystal_horn", "drops"),
            new SellEntry("sell_dead_ley_line_branch", "awakening_system:dead_ley_line_branch", 4, 1, "item.awakening_system.dead_ley_line_branch", "drops"),
            new SellEntry("sell_dead_ley_line_leaves", "awakening_system:dead_ley_line_leaves", 12, 1, "item.awakening_system.dead_ley_line_leaves", "drops"),
            new SellEntry("sell_ley_line_sprouts", "awakening_system:ley_line_sprouts", 36, 1, "item.awakening_system.ley_line_sprouts", "drops"),
            new SellEntry("sell_chaos_device", "awakening_system:chaos_device", 5, 1, "item.awakening_system.chaos_device", "drops"),
            new SellEntry("sell_chaos_circuit", "awakening_system:chaos_circuit", 15, 1, "item.awakening_system.chaos_circuit", "drops"),
            new SellEntry("sell_chaos_core", "awakening_system:chaos_core", 45, 1, "item.awakening_system.chaos_core", "drops"),
            new SellEntry("sell_mist_grass_pollen", "awakening_system:mist_grass_pollen", 3, 1, "item.awakening_system.mist_grass_pollen", "drops"),
            new SellEntry("sell_mist_grass", "awakening_system:mist_grass", 9, 1, "item.awakening_system.mist_grass", "drops"),
            new SellEntry("sell_mist_grass_wick", "awakening_system:mist_grass_wick", 27, 1, "item.awakening_system.mist_grass_wick", "drops"),
            new SellEntry("sell_hunter_sac", "awakening_system:hunter_sac", 5, 1, "item.awakening_system.hunter_sac", "drops"),
            new SellEntry("sell_recruit_insignia", "awakening_system:recruits_insignia", 3, 1, "item.awakening_system.recruits_insignia", "drops"),
            new SellEntry("sell_sergeant_insignia", "awakening_system:sergeants_insignia", 9, 1, "item.awakening_system.sergeants_insignia", "drops"),
            new SellEntry("sell_lieutenant_insignia", "awakening_system:lieutenants_insignia", 27, 1, "item.awakening_system.lieutenants_insignia", "drops"),
            new SellEntry("sell_treasure_hoarder_insignia", "awakening_system:treasure_hoarder_insignia", 2, 1, "item.awakening_system.treasure_hoarder_insignia", "drops"),
            new SellEntry("sell_silver_raven_insignia", "awakening_system:silver_raven_insignia", 6, 1, "item.awakening_system.silver_raven_insignia", "drops"),
            new SellEntry("sell_golden_raven_insignia", "awakening_system:golden_raven_insignia", 18, 1, "item.awakening_system.golden_raven_insignia", "drops"),

            // Mod Drops
            new SellEntry("sell_mowzie_naga_fang", "mowziesmobs:naga_fang", 18, 1, "awakening_system.sell.item.mowzie_naga_fang", "drops", "mowziesmobs"),
            new SellEntry("sell_iceandfire_dragon_scales", "iceandfire:dragonscales_red", 45, 1, "awakening_system.sell.item.iceandfire_dragon_scales", "drops", "iceandfire"),
            new SellEntry("sell_iceandfire_dragon_blood", "iceandfire:fire_dragon_blood", 60, 1, "awakening_system.sell.item.iceandfire_dragon_blood", "drops", "iceandfire"),

            // ==================== AGRICULTURA Y COMIDA (FARMING) ====================
            new SellEntry("sell_wheat", "minecraft:wheat", 1, 4, "awakening_system.sell.item.wheat", "farming"),
            new SellEntry("sell_carrot", "minecraft:carrot", 1, 4, "awakening_system.sell.item.carrot", "farming"),
            new SellEntry("sell_potato", "minecraft:potato", 1, 4, "awakening_system.sell.item.potato", "farming"),
            new SellEntry("sell_beetroot", "minecraft:beetroot", 1, 4, "awakening_system.sell.item.beetroot", "farming"),
            new SellEntry("sell_melon_slice", "minecraft:melon_slice", 1, 8, "awakening_system.sell.item.melon_slice", "farming"),
            new SellEntry("sell_pumpkin", "minecraft:pumpkin", 1, 2, "awakening_system.sell.item.pumpkin", "farming"),
            new SellEntry("sell_sugar_cane", "minecraft:sugar_cane", 1, 4, "awakening_system.sell.item.sugar_cane", "farming"),
            new SellEntry("sell_kelp", "minecraft:kelp", 1, 8, "awakening_system.sell.item.kelp", "farming"),
            new SellEntry("sell_cactus", "minecraft:cactus", 1, 4, "awakening_system.sell.item.cactus", "farming"),
            new SellEntry("sell_bamboo", "minecraft:bamboo", 1, 8, "awakening_system.sell.item.bamboo", "farming"),
            new SellEntry("sell_cocoa_beans", "minecraft:cocoa_beans", 1, 4, "awakening_system.sell.item.cocoa_beans", "farming"),
            new SellEntry("sell_nether_wart", "minecraft:nether_wart", 2, 2, "awakening_system.sell.item.nether_wart", "farming"),
            new SellEntry("sell_chorus_fruit", "minecraft:chorus_fruit", 2, 2, "awakening_system.sell.item.chorus_fruit", "farming"),
            new SellEntry("sell_sweet_berries", "minecraft:sweet_berries", 1, 6, "awakening_system.sell.item.sweet_berries", "farming"),
            new SellEntry("sell_glow_berries", "minecraft:glow_berries", 1, 4, "awakening_system.sell.item.glow_berries", "farming"),
            new SellEntry("sell_apple", "minecraft:apple", 1, 2, "awakening_system.sell.item.apple", "farming"),
            new SellEntry("sell_golden_apple", "minecraft:golden_apple", 95, 1, "item.minecraft.golden_apple", "farming"),
            new SellEntry("sell_enchanted_golden_apple", "minecraft:enchanted_golden_apple", 850, 1, "item.minecraft.enchanted_golden_apple", "farming"),
            new SellEntry("sell_honey_bottle", "minecraft:honey_bottle", 4, 1, "item.minecraft.honey_bottle", "farming"),
            new SellEntry("sell_honeycomb", "minecraft:honeycomb", 2, 2, "item.minecraft.honeycomb", "farming"),
            new SellEntry("sell_egg", "minecraft:egg", 1, 8, "item.minecraft.egg", "farming"),

            new SellEntry("sell_ginseng", "awakening_system:thousand_year_ginseng", 180, 1, "item.awakening_system.thousand_year_ginseng", "farming"),
            new SellEntry("sell_snow_lotus", "awakening_system:mystic_snow_lotus", 180, 1, "item.awakening_system.mystic_snow_lotus", "farming"),
            new SellEntry("sell_dragon_fruit", "awakening_system:nine_leaf_dragon_fruit", 220, 1, "item.awakening_system.nine_leaf_dragon_fruit", "farming"),
            new SellEntry("sell_spirit_grass", "awakening_system:azure_spirit_grass", 120, 1, "item.awakening_system.azure_spirit_grass", "farming"),

            new SellEntry("sell_fd_cabbage", "farmersdelight:cabbage", 1, 3, "awakening_system.sell.item.fd_cabbage", "farming", "farmersdelight"),
            new SellEntry("sell_fd_tomato", "farmersdelight:tomato", 1, 3, "awakening_system.sell.item.fd_tomato", "farming", "farmersdelight"),
            new SellEntry("sell_fd_onion", "farmersdelight:onion", 1, 3, "awakening_system.sell.item.fd_onion", "farming", "farmersdelight"),
            new SellEntry("sell_fd_rice", "farmersdelight:rice", 1, 3, "awakening_system.sell.item.fd_rice", "farming", "farmersdelight"),

            // ==================== BLOQUES Y MATERIALES (BLOCKS) ====================
            new SellEntry("sell_oak_log", "minecraft:oak_log", 1, 4, "awakening_system.sell.item.oak_log", "blocks"),
            new SellEntry("sell_spruce_log", "minecraft:spruce_log", 1, 4, "awakening_system.sell.item.spruce_log", "blocks"),
            new SellEntry("sell_birch_log", "minecraft:birch_log", 1, 4, "awakening_system.sell.item.birch_log", "blocks"),
            new SellEntry("sell_jungle_log", "minecraft:jungle_log", 1, 4, "awakening_system.sell.item.jungle_log", "blocks"),
            new SellEntry("sell_acacia_log", "minecraft:acacia_log", 1, 4, "awakening_system.sell.item.acacia_log", "blocks"),
            new SellEntry("sell_dark_oak_log", "minecraft:dark_oak_log", 1, 4, "awakening_system.sell.item.dark_oak_log", "blocks"),
            new SellEntry("sell_mangrove_log", "minecraft:mangrove_log", 1, 4, "awakening_system.sell.item.mangrove_log", "blocks"),
            new SellEntry("sell_cherry_log", "minecraft:cherry_log", 1, 4, "awakening_system.sell.item.cherry_log", "blocks"),
            new SellEntry("sell_crimson_stem", "minecraft:crimson_stem", 1, 4, "awakening_system.sell.item.crimson_stem", "blocks"),
            new SellEntry("sell_warped_stem", "minecraft:warped_stem", 1, 4, "awakening_system.sell.item.warped_stem", "blocks"),
            new SellEntry("sell_cuihua_log", "awakening_system:cuihua_log", 1, 2, "block.awakening_system.cuihua_log", "blocks"),
            new SellEntry("sell_adhigama_log", "awakening_system:adhigama_log", 1, 2, "block.awakening_system.adhigama_log", "blocks"),
            new SellEntry("sell_sandbearer_log", "awakening_system:sandbearer_log", 1, 2, "block.awakening_system.sandbearer_log", "blocks"),
            new SellEntry("sell_mana_wood_log", "awakening_system:mana_wood_log", 2, 2, "block.awakening_system.mana_wood_log", "blocks"),
            new SellEntry("sell_eldoria_log", "awakening_system:eldoria_log", 2, 2, "block.awakening_system.eldoria_log", "blocks"),
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
