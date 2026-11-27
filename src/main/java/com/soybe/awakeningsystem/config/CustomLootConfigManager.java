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

    private static final Map<Integer, List<LootItem>> ROULETTE_SLOTS = new HashMap<>();

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

    private static void loadRouletteConfig(File file) {
        ROULETTE_SLOTS.clear();
        if (!file.exists()) {
            createDefaultRouletteJson(file);
        } else {
            // Auto-migración a version 2 si el archivo es antiguo
            try (FileReader reader = new FileReader(file)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (!root.has("version") || root.get("version").getAsInt() < 2) {
                    AwakeningSystemMod.LOGGER.info("Actualizando roulette_rewards.json a versión 2 con botines modernos.");
                    createDefaultRouletteJson(file);
                }
            } catch (Exception ignored) {}
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
            root.addProperty("version", 2);
            root.addProperty("_comment", "Awakening System - Roulette Altar Rewards Configuration. Supports items from any mod.");

            JsonObject slots = new JsonObject();

            // Slot 0: Gran Premio Mítico / Armas de Grado Divino y Lingotes Celestiales
            JsonObject s0 = new JsonObject();
            s0.addProperty("title", "Gran Premio Mítico y Armas Celestiales");
            JsonArray a0 = new JsonArray();
            addJsonItem(a0, "awakening_system:celestial_ingot", 2, 2, 12, "Lingote Celestial x2");
            addJsonItem(a0, "awakening_system:primordial_jade_cutter", 1, 1, 10, "Cortador de Jade Primordial");
            addJsonItem(a0, "awakening_system:mistsplitter_reforged", 1, 1, 10, "Reflejo de las Tinieblas");
            addJsonItem(a0, "awakening_system:staff_of_homa", 1, 1, 10, "Báculo de Homa");
            addJsonItem(a0, "awakening_system:elucidator", 1, 1, 10, "Elucidator");
            addJsonItem(a0, "awakening_system:dark_repulser", 1, 1, 10, "Dark Repulser");
            addJsonItem(a0, "awakening_system:plum_blossom_sword", 1, 1, 10, "Espada de Flor de Ciruelo");
            addJsonItem(a0, "awakening_system:sky_demon_sword", 1, 1, 10, "Espada del Demonio Celestial");
            addJsonItem(a0, "awakening_system:asrahan_holy_sword", 1, 1, 10, "Espada Sagrada de Asrahan");
            addJsonItem(a0, "awakening_system:black_dragon_scythe", 1, 1, 10, "Guadaña del Dragón Negro");
            s0.add("items", a0);
            slots.add("0", s0);

            // Slot 1: Reliquias Celestiales y Píldoras de Reencarnación
            JsonObject s1 = new JsonObject();
            s1.addProperty("title", "Reliquias Celestiales y Reencarnación");
            JsonArray a1 = new JsonArray();
            addJsonItem(a1, "awakening_system:primordial_ingot", 2, 3, 15, "Lingote Primordial x2-x3");
            addJsonItem(a1, "awakening_system:celestial_dragon_core", 1, 1, 12, "Núcleo del Dragón Celestial");
            addJsonItem(a1, "awakening_system:celestial_dragon_pearl", 1, 1, 15, "Perla del Dragón Celestial");
            addJsonItem(a1, "awakening_system:pill_reincarnation", 1, 1, 15, "Píldora del Renacimiento Primordial");
            addJsonItem(a1, "awakening_system:death_king_clock", 1, 1, 15, "Reloj del Rey de la Muerte");
            addJsonItem(a1, "minecraft:nether_star", 2, 3, 20, "Estrella del Nether x2-x3");
            s1.add("items", a1);
            slots.add("1", s1);

            // Slot 2: Minerales y Metales Supremos de Progresión
            JsonObject s2 = new JsonObject();
            s2.addProperty("title", "Minerales y Metales Supremos");
            JsonArray a2 = new JsonArray();
            addJsonItem(a2, "awakening_system:mithril_ingot", 3, 6, 20, "Lingote de Mithril x3-x6");
            addJsonItem(a2, "awakening_system:astralite_ingot", 3, 6, 20, "Lingote de Astralita x3-x6");
            addJsonItem(a2, "awakening_system:adamantite_ingot", 3, 6, 20, "Lingote de Adamantita x3-x6");
            addJsonItem(a2, "awakening_system:abyssal_ingot", 3, 6, 22, "Lingote Abisal x3-x6");
            addJsonItem(a2, "awakening_system:soul_alloy_ingot", 4, 8, 25, "Lingote de Aleación de Almas x4-x8");
            addJsonItem(a2, "minecraft:netherite_ingot", 3, 6, 25, "Lingote de Netherite x3-x6");
            s2.add("items", a2);
            slots.add("2", s2);

            // Slot 3: Artefactos de Protección y Talismanes Divinos
            JsonObject s3 = new JsonObject();
            s3.addProperty("title", "Artefactos de Protección y Talismanes");
            JsonArray a3 = new JsonArray();
            addJsonItem(a3, "awakening_system:unbreakable_faith", 1, 1, 15, "Fe Inquebrantable");
            addJsonItem(a3, "awakening_system:shadow_daggers", 1, 1, 18, "Dagas de la Sombra");
            addJsonItem(a3, "awakening_system:transcendent_thorn", 1, 1, 18, "Espina Trascendente");
            addJsonItem(a3, "minecraft:totem_of_undying", 2, 3, 25, "Tótem de la Inmortalidad x2-x3");
            addJsonItem(a3, "awakening_system:murim_ascension_talisman", 1, 1, 25, "Talismán de Ascensión Murim");
            addJsonItem(a3, "awakening_system:astral_crystal_scale", 3, 5, 25, "Escama de Cristal Astral x3-x5");
            addJsonItem(a3, "awakening_system:immortal_dragon_scale", 3, 5, 25, "Escama de Dragón Inmortal x3-x5");
            s3.add("items", a3);
            slots.add("3", s3);

            // Slot 4: Consumibles Sagrados del Sistema y Alimentos Místicos
            JsonObject s4 = new JsonObject();
            s4.addProperty("title", "Consumibles Sagrados del Sistema");
            JsonArray a4 = new JsonArray();
            addJsonItem(a4, "awakening_system:shadow_apple", 2, 4, 20, "Manzana de las Sombras x2-x4");
            addJsonItem(a4, "awakening_system:spirit_qi_carrot", 8, 16, 25, "Zanahoria de Qi Espiritual x8-x16");
            addJsonItem(a4, "minecraft:enchanted_golden_apple", 2, 4, 22, "Manzana Dorada Encantada x2-x4");
            addJsonItem(a4, "awakening_system:pill_bone_transformation", 3, 5, 22, "Píldora de Transformación Ósea x3-x5");
            addJsonItem(a4, "awakening_system:pill_qi_purification", 3, 5, 22, "Píldora de Purificación de Qi x3-x5");
            addJsonItem(a4, "awakening_system:thousand_year_ginseng", 3, 5, 20, "Ginseng Milenario x3-x5");
            addJsonItem(a4, "awakening_system:snow_lotus", 3, 5, 20, "Loto de Nieve x3-x5");
            s4.add("items", a4);
            slots.add("4", s4);

            // Slot 5: Metales Refinados y Riqueza
            JsonObject s5 = new JsonObject();
            s5.addProperty("title", "Metales Refinados y Riqueza");
            JsonArray a5 = new JsonArray();
            addJsonItem(a5, "awakening_system:refined_white_iron_ingot", 6, 12, 25, "Lingote de Hierro Blanco Refinado x6-x12");
            addJsonItem(a5, "awakening_system:soul_forge_ingot", 4, 8, 25, "Lingote de Forja de Almas x4-x8");
            addJsonItem(a5, "minecraft:netherite_block", 1, 2, 18, "Bloque de Netherite x1-x2");
            addJsonItem(a5, "minecraft:diamond_block", 3, 6, 25, "Bloque de Diamante x3-x6");
            addJsonItem(a5, "minecraft:emerald_block", 6, 12, 25, "Bloque de Esmeralda x6-x12");
            addJsonItem(a5, "minecraft:ancient_debris", 4, 8, 22, "Escombros Ancestrales x4-x8");
            s5.add("items", a5);
            slots.add("5", s5);

            // Slot 6: Fichas de Ruleta y Suministros de Expedición
            JsonObject s6 = new JsonObject();
            s6.addProperty("title", "Fichas y Suministros de Expedición");
            JsonArray a6 = new JsonArray();
            addJsonItem(a6, "awakening_system:roulette_token", 3, 6, 28, "Ficha de Ruleta x3-x6");
            addJsonItem(a6, "awakening_system:bottled_mana_pixie", 2, 4, 22, "Hada de Maná Embotellada x2-x4");
            addJsonItem(a6, "minecraft:golden_apple", 6, 12, 25, "Manzanas Doradas x6-x12");
            addJsonItem(a6, "minecraft:experience_bottle", 32, 64, 30, "Frascos de Experiencia x32-x64");
            addJsonItem(a6, "minecraft:ender_pearl", 16, 32, 30, "Perlas de Ender x16-x32");
            s6.add("items", a6);
            slots.add("6", s6);

            // Slot 7: Monedas del Sistema y Fortuna
            JsonObject s7 = new JsonObject();
            s7.addProperty("title", "Fortuna y Monedas del Sistema");
            JsonArray a7 = new JsonArray();
            addJsonItem(a7, "awakening_system:awakening_coin", 32, 64, 45, "Monedas del Sistema x32-x64");
            addJsonItem(a7, "awakening_system:mora_bundle", 1, 2, 25, "Fardo Dorado de Mora (50,000 Mora)");
            addJsonItem(a7, "awakening_system:mora_pouch", 2, 4, 30, "Bolsa de Mora del Aventurero (10,000 Mora)");
            s7.add("items", a7);
            slots.add("7", s7);

            root.add("slots", slots);
            GSON.toJson(root, writer);
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo crear roulette_rewards.json por defecto", e);
        }
    }

    public static int getWorldTier(net.minecraft.world.level.Level level) {
        if (level == null) return 1;
        String path = level.dimension().location().getPath().toLowerCase(java.util.Locale.ROOT);
        if (path.contains("teyvat") || path.contains("celestial")) return 4;
        if (path.contains("murim") || path.contains("pagoda")) return 3;
        if (path.contains("isekai")) return 2;
        return 1;
    }

    public static int getItemWorldTier(String itemId) {
        if (itemId == null) return 1;
        String id = itemId.toLowerCase(java.util.Locale.ROOT);
        if (id.contains("celestial_ingot") || id.contains("primordial_ingot")
                || id.contains("celestial_dragon_") || id.contains("pill_reincarnation")
                || id.contains("death_king_clock") || id.contains("astral_crystal_scale")
                || id.contains("primordial_jade") || id.contains("mistsplitter")
                || id.contains("staff_of_homa") || id.contains("mora")
                || id.contains("refined_white_iron")) {
            return 4;
        }
        if (id.contains("spirit_qi_") || id.contains("ginseng") || id.contains("snow_lotus")
                || id.contains("dragon_fruit") || id.contains("spirit_grass")
                || id.contains("pill_bone") || id.contains("pill_qi")
                || id.contains("murim_") || id.contains("plum_blossom")
                || id.contains("sky_demon") || id.contains("asrahan")
                || id.contains("black_dragon_scythe") || id.contains("spirit_jade")
                || id.contains("astral_ingot")) {
            return 3;
        }
        if (id.contains("mithril") || id.contains("adamantite")
                || id.contains("elucidator") || id.contains("dark_repulser")
                || id.contains("transcendent_thorn") || id.contains("immortal_dragon_scale")) {
            return 2;
        }
        return 1;
    }

    public static ItemStack getRouletteSlotItem(int slot, RandomSource rand) {
        return getRouletteSlotItem(slot, rand, 1);
    }

    public static ItemStack getRouletteSlotItem(int slot, RandomSource rand, int worldTier) {
        init();
        int s = Math.floorMod(slot, 8);
        List<LootItem> items = ROULETTE_SLOTS.get(s);
        if (items != null && !items.isEmpty()) {
            List<LootItem> eligible = new ArrayList<>();
            for (LootItem li : items) {
                if (getItemWorldTier(li.itemId) <= worldTier) {
                    eligible.add(li);
                }
            }
            if (!eligible.isEmpty()) {
                LootItem chosen = pickWeighted(eligible, rand);
                if (chosen != null) {
                    ItemStack stack = resolveStack(chosen, rand);
                    if (!stack.isEmpty()) return stack;
                }
            }
        }
        return RouletteAltarBlockEntity.getDefaultFallbackItem(s, worldTier);
    }

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
            if (!CHEST_BOXES.containsKey("box_mythic") || CHEST_BOXES.get("box_mythic").isEmpty()) {
                CHEST_BOXES.put("box_mythic", createDefaultMythicList());
            }
            if (!CHEST_BOXES.containsKey("box_primordial") || CHEST_BOXES.get("box_primordial").isEmpty()) {
                CHEST_BOXES.put("box_primordial", createDefaultPrimordialList());
            }
            AwakeningSystemMod.LOGGER.info("Configuración de Cajas de la Tienda cargada con {} categorías.", CHEST_BOXES.size());
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo leer chest_rewards.json, usando valores por defecto.", e);
            if (!CHEST_BOXES.containsKey("box_mythic")) CHEST_BOXES.put("box_mythic", createDefaultMythicList());
            if (!CHEST_BOXES.containsKey("box_primordial")) CHEST_BOXES.put("box_primordial", createDefaultPrimordialList());
        }
    }

    private static List<LootItem> createDefaultMythicList() {
        List<LootItem> list = new ArrayList<>();
        list.add(new LootItem("awakening_system:mithril_ingot", 3, 6, 25, "Mithril Ingots", 500));
        list.add(new LootItem("awakening_system:adamantite_ingot", 2, 5, 22, "Adamantite Ingots", 750));
        list.add(new LootItem("awakening_system:astral_ingot", 2, 4, 18, "Astral Ingots", 1000));
        list.add(new LootItem("awakening_system:enchanted_mithril_apple", 1, 2, 12, "Enchanted Mithril Apple", 1500));
        list.add(new LootItem("awakening_system:adamantite_apple", 1, 2, 12, "Adamantite Apple", 1500));
        list.add(new LootItem("awakening_system:spirit_jade_apple", 1, 2, 10, "Spirit Jade Apple", 2000));
        list.add(new LootItem("awakening_system:shadow_apple", 1, 2, 8, "Shadow Apple", 2500));
        list.add(new LootItem("awakening_system:realm_breakthrough_pill", 1, 1, 6, "Realm Breakthrough Pill", 3000));
        list.add(new LootItem("awakening_system:adamantite_war_hammer", 1, 1, 4, "Adamantite War Hammer", 5000));
        return list;
    }

    private static List<LootItem> createDefaultPrimordialList() {
        List<LootItem> list = new ArrayList<>();
        list.add(new LootItem("awakening_system:celestial_ingot", 2, 4, 25, "Celestial Ingots", 2000));
        list.add(new LootItem("awakening_system:primordial_ingot", 2, 4, 22, "Primordial Ingots", 3500));
        list.add(new LootItem("awakening_system:enchanted_celestial_apple", 1, 2, 15, "Enchanted Celestial Apple", 4000));
        list.add(new LootItem("awakening_system:primordial_apple", 1, 2, 12, "Primordial Apple", 5000));
        list.add(new LootItem("awakening_system:shadow_apple", 2, 4, 10, "Shadow Apples", 5000));
        list.add(new LootItem("awakening_system:celestial_dragon_pearl", 1, 1, 10, "Celestial Dragon Pearl", 8000));
        list.add(new LootItem("awakening_system:primordial_rebirth_pill", 1, 1, 8, "Pill of Primordial Rebirth", 10000));
        list.add(new LootItem("awakening_system:primordial_jade_cutter", 1, 1, 4, "Primordial Jade Cutter", 20000));
        list.add(new LootItem("awakening_system:primordial_jade_winged_spear", 1, 1, 4, "Primordial Jade Winged Spear", 20000));
        return list;
    }

    private static void createDefaultChestJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Awakening System - Shop Box Loot Configuration (box_common, box_rare, box_epic, box_legendary, box_mythic, box_primordial, lucky_box). You can add items from any mod.");

            JsonObject boxes = new JsonObject();

            JsonArray bCommon = new JsonArray();
            addJsonChestItem(bCommon, "minecraft:iron_ingot", 6, 10, 35, "Iron Ingots", 0);
            addJsonChestItem(bCommon, "minecraft:coal", 16, 24, 25, "Coal", 0);
            addJsonChestItem(bCommon, "minecraft:cooked_beef", 12, 16, 20, "Cooked Beef", 0);
            addJsonChestItem(bCommon, "minecraft:gold_ingot", 5, 8, 10, "Gold Ingots", 50);
            addJsonChestItem(bCommon, "awakening_system:refined_white_iron_ingot", 2, 4, 10, "Refined White Iron Ingot", 50);
            boxes.add("box_common", bCommon);

            JsonArray bRare = new JsonArray();
            addJsonChestItem(bRare, "minecraft:gold_ingot", 10, 16, 30, "Gold Ingots", 50);
            addJsonChestItem(bRare, "minecraft:diamond", 3, 6, 25, "Diamonds", 100);
            addJsonChestItem(bRare, "minecraft:ender_pearl", 6, 10, 15, "Ender Pearls", 0);
            addJsonChestItem(bRare, "minecraft:golden_apple", 2, 4, 10, "Golden Apples", 150);
            addJsonChestItem(bRare, "awakening_system:spirit_qi_carrot", 2, 4, 10, "Spirit Qi Carrot", 100);
            addJsonChestItem(bRare, "awakening_system:refined_white_iron_ingot", 4, 8, 10, "Refined White Iron Ingots", 100);
            boxes.add("box_rare", bRare);

            JsonArray bEpic = new JsonArray();
            addJsonChestItem(bEpic, "minecraft:diamond", 6, 12, 30, "Diamonds", 200);
            addJsonChestItem(bEpic, "minecraft:netherite_scrap", 2, 4, 25, "Netherite Scraps", 300);
            addJsonChestItem(bEpic, "awakening_system:abyssal_ingot", 2, 4, 20, "Abyssal Ingots", 350);
            addJsonChestItem(bEpic, "minecraft:golden_apple", 4, 8, 15, "Golden Apples", 250);
            addJsonChestItem(bEpic, "minecraft:enchanted_golden_apple", 1, 2, 10, "Enchanted Golden Apple", 500);
            addJsonChestItem(bEpic, "awakening_system:shadow_apple", 1, 1, 8, "Shadow Apple", 600);
            boxes.add("box_epic", bEpic);

            JsonArray bLeg = new JsonArray();
            addJsonChestItem(bLeg, "minecraft:netherite_ingot", 2, 4, 25, "Lingotes de Netherite", 500);
            addJsonChestItem(bLeg, "awakening_system:soul_alloy_ingot", 2, 4, 20, "Soul Alloy Ingots", 600);
            addJsonChestItem(bLeg, "awakening_system:astralite_ingot", 2, 4, 18, "Astralite Ingots", 700);
            addJsonChestItem(bLeg, "minecraft:totem_of_undying", 1, 2, 15, "Totem of Undying", 600);
            addJsonChestItem(bLeg, "minecraft:enchanted_golden_apple", 2, 4, 12, "Enchanted Golden Apples", 800);
            addJsonChestItem(bLeg, "awakening_system:shadow_apple", 2, 3, 10, "Shadow Apples", 900);
            addJsonChestItem(bLeg, "minecraft:diamond_block", 2, 5, 10, "Diamond Blocks", 750);
            addJsonChestItem(bLeg, "minecraft:nether_star", 1, 2, 8, "Nether Star", 1500);
            boxes.add("box_legendary", bLeg);

            JsonArray bMythic = new JsonArray();
            addJsonChestItem(bMythic, "awakening_system:mithril_ingot", 3, 6, 25, "Mithril Ingots", 500);
            addJsonChestItem(bMythic, "awakening_system:adamantite_ingot", 2, 5, 22, "Adamantite Ingots", 750);
            addJsonChestItem(bMythic, "awakening_system:astral_ingot", 2, 4, 18, "Astral Ingots", 1000);
            addJsonChestItem(bMythic, "awakening_system:enchanted_mithril_apple", 1, 2, 12, "Enchanted Mithril Apple", 1500);
            addJsonChestItem(bMythic, "awakening_system:adamantite_apple", 1, 2, 12, "Adamantite Apple", 1500);
            addJsonChestItem(bMythic, "awakening_system:spirit_jade_apple", 1, 2, 10, "Spirit Jade Apple", 2000);
            addJsonChestItem(bMythic, "awakening_system:shadow_apple", 1, 2, 8, "Shadow Apple", 2500);
            addJsonChestItem(bMythic, "awakening_system:realm_breakthrough_pill", 1, 1, 6, "Realm Breakthrough Pill", 3000);
            addJsonChestItem(bMythic, "awakening_system:adamantite_war_hammer", 1, 1, 4, "Adamantite War Hammer", 5000);
            boxes.add("box_mythic", bMythic);

            JsonArray bPrimordial = new JsonArray();
            addJsonChestItem(bPrimordial, "awakening_system:celestial_ingot", 2, 4, 25, "Celestial Ingots", 2000);
            addJsonChestItem(bPrimordial, "awakening_system:primordial_ingot", 2, 4, 22, "Primordial Ingots", 3500);
            addJsonChestItem(bPrimordial, "awakening_system:enchanted_celestial_apple", 1, 2, 15, "Enchanted Celestial Apple", 4000);
            addJsonChestItem(bPrimordial, "awakening_system:primordial_apple", 1, 2, 12, "Primordial Apple", 5000);
            addJsonChestItem(bPrimordial, "awakening_system:shadow_apple", 2, 4, 10, "Shadow Apples", 5000);
            addJsonChestItem(bPrimordial, "awakening_system:celestial_dragon_pearl", 1, 1, 10, "Celestial Dragon Pearl", 8000);
            addJsonChestItem(bPrimordial, "awakening_system:primordial_rebirth_pill", 1, 1, 8, "Pill of Primordial Rebirth", 10000);
            addJsonChestItem(bPrimordial, "awakening_system:primordial_jade_cutter", 1, 1, 4, "Primordial Jade Cutter", 20000);
            addJsonChestItem(bPrimordial, "awakening_system:primordial_jade_winged_spear", 1, 1, 4, "Primordial Jade Winged Spear", 20000);
            boxes.add("box_primordial", bPrimordial);

            JsonArray bLucky = new JsonArray();
            addJsonChestItem(bLucky, "minecraft:iron_ingot", 4, 8, 35, "Iron", 25);
            addJsonChestItem(bLucky, "minecraft:gold_ingot", 4, 8, 25, "Gold", 50);
            addJsonChestItem(bLucky, "minecraft:lapis_lazuli", 8, 16, 20, "Lapis Lazuli", 30);
            addJsonChestItem(bLucky, "awakening_system:vigor_potion", 1, 2, 12, "Vigor Potion", 100);
            addJsonChestItem(bLucky, "awakening_system:spirit_qi_carrot", 1, 2, 10, "Spirit Qi Carrot", 75);
            addJsonChestItem(bLucky, "minecraft:diamond", 1, 2, 5, "Diamonds", 100);
            addJsonChestItem(bLucky, "minecraft:enchanted_golden_apple", 1, 1, 2, "Grand Prize: Enchanted Apple", 1000);
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
                    int originalCount = stack.getCount();
                    ItemStack toGive = stack.copy();
                    if (!player.getInventory().add(toGive)) {
                        player.drop(toGive, false);
                    }
                    ids.add(chosen.itemId());
                    counts.add(originalCount);

                    String summary = (chosen.displayName() != null && !chosen.displayName().isEmpty())
                            ? chosen.displayName() + " x" + originalCount
                            : stack.getHoverName().getString() + " x" + originalCount;

                    return new BoxLootResult(summary, chosen.bonusCoins(), ids, counts);
                }
            }
        }

        ItemStack fallback = new ItemStack(Items.IRON_INGOT, 4);
        if (!player.getInventory().add(fallback)) {
            player.drop(fallback, false);
        }
        ids.add("minecraft:iron_ingot");
        counts.add(4);
        return new BoxLootResult("Hierro x4", 0, ids, counts);
    }

    /**
     * Several rolls from one box's pool without handing anything out (the guild loot bag gives a handful of
     * these at once). Bonus coins of every roll are added to {@code coinsOut[0]}.
     */
    public static List<ItemStack> rollStacks(String boxId, int rolls, RandomSource rand, int[] coinsOut) {
        init();
        List<ItemStack> out = new ArrayList<>();
        List<LootItem> items = CHEST_BOXES.get(boxId);
        if (items == null || items.isEmpty()) return out;
        for (int i = 0; i < rolls; i++) {
            LootItem chosen = pickWeighted(items, rand);
            if (chosen == null) continue;
            ItemStack stack = resolveStack(chosen, rand);
            if (stack.isEmpty()) continue;
            out.add(stack);
            coinsOut[0] += Math.max(0, chosen.bonusCoins());
        }
        return out;
    }

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
                int maxStack = Math.min(64, it.getDefaultMaxStackSize());
                return new ItemStack(it, Math.min(Math.max(1, count), maxStack));
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Error parsing custom loot item: {}", e.getMessage(), e);
        }
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
