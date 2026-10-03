package com.soybe.awakeningsystem.config;

import com.google.gson.*;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents.BoxLootResult;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.entity.RouletteAltarBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Gestor de botín y recompensas configurables para la Ruleta del Sistema y las Cajas de la Tienda.
 * Permite a los creadores de modpacks y jugadores personalizar al 100% el contenido,
 * incluyendo ítems de cualquier otro mod instalado mediante archivos JSON en config/awakening_system/.
 */
public final class CustomLootConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record LootItem(String itemId, int minCount, int maxCount, int weight, String displayName, int bonusCoins) {}

    // 8 slots de la Ruleta (0 al 7)
    private static final Map<Integer, List<LootItem>> ROULETTE_SLOTS = new HashMap<>();

    // Cajas de la tienda: box_common, box_rare, box_epic, box_legendary, lucky_box
    private static final Map<String, List<LootItem>> CHEST_BOXES = new HashMap<>();

    private static boolean initialized = false;

    private CustomLootConfigManager() {}

    public static Path getConfigDir() {
        try {
            return FMLPaths.CONFIGDIR.get().resolve("awakening_system");
        } catch (Throwable t) {
            return Paths.get("config", "awakening_system");
        }
    }

    public static void init() {
        if (initialized) return;
        reload();
        initialized = true;
    }

    public static void reload() {
        try {
            Path dir = getConfigDir();
            File folder = dir.toFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }

            loadRouletteConfig(dir.resolve("roulette_rewards.json").toFile());
            loadChestConfig(dir.resolve("chest_rewards.json").toFile());
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al cargar configuraciones de botín personalizadas de Awakening System", e);
        }
    }

    // ==========================================
    // 1. CONFIGURACIÓN DE LA RULETA
    // ==========================================

    private static void loadRouletteConfig(File file) {
        ROULETTE_SLOTS.clear();
        if (!file.exists()) {
            createDefaultRouletteJson(file);
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("slots")) {
                JsonObject slots = root.getAsJsonObject("slots");
                for (int i = 0; i < 8; i++) {
                    String key = String.valueOf(i);
                    if (slots.has(key)) {
                        JsonObject slotObj = slots.getAsJsonObject(key);
                        JsonArray itemsArr = slotObj.getAsJsonArray("items");
                        List<LootItem> list = new ArrayList<>();
                        for (JsonElement el : itemsArr) {
                            JsonObject itemObj = el.getAsJsonObject();
                            String id = itemObj.get("item").getAsString();
                            int min = itemObj.has("minCount") ? itemObj.get("minCount").getAsInt() : (itemObj.has("count") ? itemObj.get("count").getAsInt() : 1);
                            int max = itemObj.has("maxCount") ? itemObj.get("maxCount").getAsInt() : min;
                            int weight = itemObj.has("weight") ? itemObj.get("weight").getAsInt() : 10;
                            String name = itemObj.has("displayName") ? itemObj.get("displayName").getAsString() : "";
                            list.add(new LootItem(id, min, max, weight, name, 0));
                        }
                        if (!list.isEmpty()) {
                            ROULETTE_SLOTS.put(i, list);
                        }
                    }
                }
            }
            AwakeningSystemMod.LOGGER.info("Configuración de Ruleta cargada con {} slots personalizados.", ROULETTE_SLOTS.size());
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo leer roulette_rewards.json, usando valores por defecto.", e);
        }
    }

    private static void createDefaultRouletteJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Awakening System - Configuración de Recompensas de la Ruleta (Roulette Altar). Puedes añadir ítems de cualquier mod (ej: 'cataclysm:ignitium_ingot', 'iron_spells:scroll').");

            JsonObject slots = new JsonObject();

            // Slot 0: Mítico
            JsonObject s0 = new JsonObject();
            s0.addProperty("title", "Gran Premio Mítico");
            JsonArray a0 = new JsonArray();
            addJsonItem(a0, "awakening_system:plum_blossom_sword", 1, 1, 10, "Espada de Flor de Ciruelo");
            addJsonItem(a0, "awakening_system:sky_demon_sword", 1, 1, 10, "Espada del Demonio Celestial");
            addJsonItem(a0, "awakening_system:asrahan_holy_sword", 1, 1, 10, "Espada Sagrada de Asrahan");
            addJsonItem(a0, "awakening_system:black_dragon_scythe", 1, 1, 10, "Guadaña del Dragón Negro");
            addJsonItem(a0, "awakening_system:heaven_shaking_sword", 1, 1, 10, "Espada Devastadora Celestial");
            addJsonItem(a0, "awakening_system:dawns_ballad", 1, 1, 10, "Balada del Alba");
            addJsonItem(a0, "awakening_system:radiant_light_sword", 1, 1, 10, "Espada de Luz Radiante");
            addJsonItem(a0, "awakening_system:gigantic_greatsword", 1, 1, 10, "Mandoble Gigantesco");
            s0.add("items", a0);
            slots.add("0", s0);

            // Slot 1: Reliquias
            JsonObject s1 = new JsonObject();
            s1.addProperty("title", "Reliquias Celestiales");
            JsonArray a1 = new JsonArray();
            addJsonItem(a1, "awakening_system:celestial_dragon_pearl", 1, 1, 10, "Perla del Dragón Celestial");
            addJsonItem(a1, "awakening_system:death_king_clock", 1, 1, 10, "Reloj del Rey de la Muerte");
            addJsonItem(a1, "awakening_system:shadow_daggers", 1, 1, 15, "Dagas de la Sombra");
            addJsonItem(a1, "awakening_system:transcendent_thorn", 1, 1, 15, "Espina Trascendente");
            addJsonItem(a1, "minecraft:nether_star", 1, 1, 20, "Estrella del Nether");
            s1.add("items", a1);
            slots.add("1", s1);

            // Slot 2: Escudos y Tótems
            JsonObject s2 = new JsonObject();
            s2.addProperty("title", "Escudos y Protección");
            JsonArray a2 = new JsonArray();
            addJsonItem(a2, "awakening_system:unbreakable_faith", 1, 1, 15, "Fe Inquebrantable");
            addJsonItem(a2, "minecraft:totem_of_undying", 1, 1, 25, "Tótem de la Inmortalidad");
            addJsonItem(a2, "minecraft:enchanted_golden_apple", 2, 2, 25, "Manzana Dorada Encantada x2");
            addJsonItem(a2, "awakening_system:bottled_mana_pixie", 1, 1, 20, "Hada de Maná Embotellada");
            s2.add("items", a2);
            slots.add("2", s2);

            // Slot 3: Escamas y Talismanes
            JsonObject s3 = new JsonObject();
            s3.addProperty("title", "Escamas Ancestrales y Talismanes");
            JsonArray a3 = new JsonArray();
            addJsonItem(a3, "awakening_system:immortal_dragon_scale", 2, 2, 25, "Escama de Dragón Inmortal x2");
            addJsonItem(a3, "awakening_system:astral_crystal_scale", 2, 2, 25, "Escama de Cristal Astral x2");
            addJsonItem(a3, "awakening_system:murim_ascension_talisman", 1, 1, 25, "Talismán de Ascensión Murim");
            addJsonItem(a3, "minecraft:netherite_block", 1, 1, 15, "Bloque de Netherite");
            s3.add("items", a3);
            slots.add("3", s3);

            // Slot 4: Hierbas Sagradas
            JsonObject s4 = new JsonObject();
            s4.addProperty("title", "Hierbas y Elixires Sagrados");
            JsonArray a4 = new JsonArray();
            addJsonItem(a4, "awakening_system:thousand_year_ginseng", 2, 2, 20, "Ginseng Milenario x2");
            addJsonItem(a4, "awakening_system:snow_lotus", 2, 2, 20, "Loto de Nieve x2");
            addJsonItem(a4, "awakening_system:wild_ginseng", 3, 3, 25, "Ginseng Silvestre x3");
            addJsonItem(a4, "awakening_system:mandrake_root", 3, 3, 25, "Raíz de Mandrágora x3");
            addJsonItem(a4, "minecraft:golden_apple", 4, 4, 20, "Manzanas Doradas x4");
            s4.add("items", a4);
            slots.add("4", s4);

            // Slot 5: Minerales y Bloques de Tesoro
            JsonObject s5 = new JsonObject();
            s5.addProperty("title", "Minerales y Bloques de Tesoro");
            JsonArray a5 = new JsonArray();
            addJsonItem(a5, "minecraft:netherite_ingot", 2, 3, 20, "Lingote de Netherite");
            addJsonItem(a5, "minecraft:diamond_block", 2, 3, 25, "Bloque de Diamante");
            addJsonItem(a5, "minecraft:gold_block", 4, 8, 30, "Bloque de Oro");
            addJsonItem(a5, "minecraft:emerald_block", 4, 8, 30, "Bloque de Esmeralda");
            addJsonItem(a5, "minecraft:ancient_debris", 3, 5, 20, "Escombros Ancestrales");
            s5.add("items", a5);
            slots.add("5", s5);

            // Slot 6: Fichas y Recursos de Aventura
            JsonObject s6 = new JsonObject();
            s6.addProperty("title", "Fichas y Recursos de Aventura");
            JsonArray a6 = new JsonArray();
            addJsonItem(a6, "awakening_system:roulette_token", 2, 4, 25, "Ficha de Ruleta");
            addJsonItem(a6, "minecraft:experience_bottle", 16, 32, 35, "Frasco de Experiencia");
            addJsonItem(a6, "minecraft:ender_pearl", 8, 16, 30, "Perla de Ender");
            addJsonItem(a6, "minecraft:golden_apple", 3, 6, 25, "Manzanas Doradas");
            s6.add("items", a6);
            slots.add("6", s6);

            // Slot 7: Monedas del Sistema
            JsonObject s7 = new JsonObject();
            s7.addProperty("title", "Monedas del Sistema");
            JsonArray a7 = new JsonArray();
            addJsonItem(a7, "awakening_system:awakening_coin", 32, 64, 50, "Monedas del Sistema");
            addJsonItem(a7, "awakening_system:mora", 10000, 25000, 20, "Mora Celestial");
            s7.add("items", a7);
            slots.add("7", s7);

            root.add("slots", slots);
            GSON.toJson(root, writer);
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo crear roulette_rewards.json por defecto", e);
        }
    }

    public static ItemStack getRouletteSlotItem(int slot, RandomSource rand) {
        init();
        int s = Math.floorMod(slot, 8);
        List<LootItem> items = ROULETTE_SLOTS.get(s);
        if (items != null && !items.isEmpty()) {
            LootItem chosen = pickWeighted(items, rand);
            if (chosen != null) {
                ItemStack stack = resolveStack(chosen, rand);
                if (!stack.isEmpty()) return stack;
            }
        }
        return RouletteAltarBlockEntity.getDefaultFallbackItem(s);
    }

    // ==========================================
    // 2. CONFIGURACIÓN DE CAJAS DE LA TIENDA
    // ==========================================

    private static void loadChestConfig(File file) {
        CHEST_BOXES.clear();
        if (!file.exists()) {
            createDefaultChestJson(file);
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("boxes")) {
                JsonObject boxes = root.getAsJsonObject("boxes");
                for (String boxId : boxes.keySet()) {
                    JsonArray itemsArr = boxes.getAsJsonArray(boxId);
                    List<LootItem> list = new ArrayList<>();
                    for (JsonElement el : itemsArr) {
                        JsonObject itemObj = el.getAsJsonObject();
                        String id = itemObj.get("item").getAsString();
                        int min = itemObj.has("minCount") ? itemObj.get("minCount").getAsInt() : (itemObj.has("count") ? itemObj.get("count").getAsInt() : 1);
                        int max = itemObj.has("maxCount") ? itemObj.get("maxCount").getAsInt() : min;
                        int weight = itemObj.has("weight") ? itemObj.get("weight").getAsInt() : 10;
                        String name = itemObj.has("displayName") ? itemObj.get("displayName").getAsString() : "";
                        int coins = itemObj.has("coins") ? itemObj.get("coins").getAsInt() : 0;
                        list.add(new LootItem(id, min, max, weight, name, coins));
                    }
                    if (!list.isEmpty()) {
                        CHEST_BOXES.put(boxId, list);
                    }
                }
            }
            AwakeningSystemMod.LOGGER.info("Configuración de Cajas de la Tienda cargada con {} categorías.", CHEST_BOXES.size());
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo leer chest_rewards.json, usando valores por defecto.", e);
        }
    }

    private static void createDefaultChestJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Awakening System - Configuración de Botín para las Cajas de la Tienda (box_common, box_rare, box_epic, box_legendary, lucky_box). Puedes añadir cualquier ítem de otros mods.");

            JsonObject boxes = new JsonObject();

            // box_common
            JsonArray bCommon = new JsonArray();
            addJsonChestItem(bCommon, "minecraft:iron_ingot", 6, 10, 40, "Lingotes de Hierro", 0);
            addJsonChestItem(bCommon, "minecraft:coal", 16, 24, 30, "Carbón", 0);
            addJsonChestItem(bCommon, "minecraft:cooked_beef", 12, 16, 20, "Carne Cocinada", 0);
            addJsonChestItem(bCommon, "minecraft:gold_ingot", 5, 8, 10, "Lingotes de Oro", 50);
            boxes.add("box_common", bCommon);

            // box_rare
            JsonArray bRare = new JsonArray();
            addJsonChestItem(bRare, "minecraft:gold_ingot", 10, 16, 40, "Lingotes de Oro", 50);
            addJsonChestItem(bRare, "minecraft:diamond", 3, 6, 35, "Diamantes", 100);
            addJsonChestItem(bRare, "minecraft:ender_pearl", 6, 10, 15, "Perlas de Ender", 0);
            addJsonChestItem(bRare, "minecraft:golden_apple", 2, 4, 10, "Manzanas Doradas", 150);
            boxes.add("box_rare", bRare);

            // box_epic
            JsonArray bEpic = new JsonArray();
            addJsonChestItem(bEpic, "minecraft:diamond", 6, 12, 40, "Diamantes", 200);
            addJsonChestItem(bEpic, "minecraft:netherite_scrap", 2, 4, 30, "Fragmentos de Netherite", 300);
            addJsonChestItem(bEpic, "minecraft:golden_apple", 4, 8, 20, "Manzanas Doradas", 250);
            addJsonChestItem(bEpic, "minecraft:enchanted_golden_apple", 1, 2, 10, "Manzana Dorada Encantada", 500);
            boxes.add("box_epic", bEpic);

            // box_legendary
            JsonArray bLeg = new JsonArray();
            addJsonChestItem(bLeg, "minecraft:netherite_ingot", 2, 4, 30, "Lingotes de Netherite", 500);
            addJsonChestItem(bLeg, "minecraft:totem_of_undying", 1, 2, 25, "Tótem de la Inmortalidad", 600);
            addJsonChestItem(bLeg, "minecraft:enchanted_golden_apple", 2, 4, 20, "Manzanas Doradas Encantadas", 800);
            addJsonChestItem(bLeg, "minecraft:diamond_block", 2, 5, 15, "Bloques de Diamante", 750);
            addJsonChestItem(bLeg, "minecraft:nether_star", 1, 2, 10, "Estrella del Nether", 1500);
            boxes.add("box_legendary", bLeg);

            // lucky_box
            JsonArray bLucky = new JsonArray();
            addJsonChestItem(bLucky, "minecraft:iron_ingot", 5, 10, 35, "Hierro", 25);
            addJsonChestItem(bLucky, "minecraft:gold_ingot", 6, 12, 30, "Oro", 50);
            addJsonChestItem(bLucky, "minecraft:diamond", 2, 6, 25, "Diamantes", 100);
            addJsonChestItem(bLucky, "minecraft:enchanted_golden_apple", 1, 2, 10, "Gran Premio: Manzana Encantada", 1000);
            boxes.add("lucky_box", bLucky);

            root.add("boxes", boxes);
            GSON.toJson(root, writer);
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo crear chest_rewards.json por defecto", e);
        }
    }

    public static BoxLootResult rollBoxLoot(String boxId, ServerPlayer player, RandomSource rand) {
        init();
        List<LootItem> items = CHEST_BOXES.get(boxId);
        List<String> ids = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        if (items != null && !items.isEmpty()) {
            LootItem chosen = pickWeighted(items, rand);
            if (chosen != null) {
                ItemStack stack = resolveStack(chosen, rand);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    ids.add(chosen.itemId());
                    counts.add(stack.getCount());

                    String summary = (chosen.displayName() != null && !chosen.displayName().isEmpty())
                            ? chosen.displayName() + " x" + stack.getCount()
                            : stack.getHoverName().getString() + " x" + stack.getCount();

                    return new BoxLootResult(summary, chosen.bonusCoins(), ids, counts);
                }
            }
        }

        // Fallback genérico si no hay configuración
        ItemStack fallback = new ItemStack(Items.IRON_INGOT, 4);
        if (!player.getInventory().add(fallback)) {
            player.drop(fallback, false);
        }
        ids.add("minecraft:iron_ingot");
        counts.add(4);
        return new BoxLootResult("Hierro x4", 0, ids, counts);
    }

    // ==========================================
    // UTILIDADES DE PROBABILIDAD Y RESOLUCIÓN
    // ==========================================

    private static LootItem pickWeighted(List<LootItem> items, RandomSource rand) {
        int totalWeight = 0;
        for (LootItem it : items) {
            totalWeight += Math.max(1, it.weight());
        }
        if (totalWeight <= 0) return items.get(0);

        int roll = rand.nextInt(totalWeight);
        int current = 0;
        for (LootItem it : items) {
            current += Math.max(1, it.weight());
            if (roll < current) {
                return it;
            }
        }
        return items.get(0);
    }

    private static ItemStack resolveStack(LootItem item, RandomSource rand) {
        try {
            ResourceLocation rl = ResourceLocation.parse(item.itemId());
            if (BuiltInRegistries.ITEM.containsKey(rl)) {
                Item it = BuiltInRegistries.ITEM.get(rl);
                int count = item.minCount();
                if (item.maxCount() > item.minCount()) {
                    count = item.minCount() + rand.nextInt(item.maxCount() - item.minCount() + 1);
                }
                return new ItemStack(it, Math.max(1, count));
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    private static void addJsonItem(JsonArray arr, String itemId, int min, int max, int weight, String name) {
        JsonObject o = new JsonObject();
        o.addProperty("item", itemId);
        if (min == max) {
            o.addProperty("count", min);
        } else {
            o.addProperty("minCount", min);
            o.addProperty("maxCount", max);
        }
        o.addProperty("weight", weight);
        if (name != null && !name.isEmpty()) {
            o.addProperty("displayName", name);
        }
        arr.add(o);
    }

    private static void addJsonChestItem(JsonArray arr, String itemId, int min, int max, int weight, String name, int coins) {
        JsonObject o = new JsonObject();
        o.addProperty("item", itemId);
        o.addProperty("minCount", min);
        o.addProperty("maxCount", max);
        o.addProperty("weight", weight);
        o.addProperty("displayName", name);
        if (coins > 0) {
            o.addProperty("coins", coins);
        }
        arr.add(o);
    }
}
