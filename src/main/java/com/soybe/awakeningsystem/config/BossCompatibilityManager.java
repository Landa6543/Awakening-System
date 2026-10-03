package com.soybe.awakeningsystem.config;

import com.google.gson.*;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ConstellationDefinitions;
import com.soybe.awakeningsystem.SystemConfig;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Motor universal de compatibilidad de jefes para Awakening System.
 * Detecta y soporta jefes de cualquier mod (Cataclysm, Mowzie's Mobs, Twilight Forest, Iron's Spells, etc.)
 * a través de:
 * 1. Tags unificadas (#c:bosses, #forge:bosses, #awakening_system:bosses).
 * 2. Archivo de configuración 'config/awakening_system/custom_bosses.json'.
 * 3. Detección heurística inteligente por palabras clave, barras de vida y dimensiones.
 */
public final class BossCompatibilityManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final TagKey<EntityType<?>> C_BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", "bosses"));
    public static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses"));
    public static final TagKey<EntityType<?>> AWAKENING_BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("awakening_system", "bosses"));

    public record BossEntry(String entityId, int customExp, int coinBonus, String fable) {}

    private static final Map<String, BossEntry> WHITELIST_BOSSES = new HashMap<>();
    private static final Set<String> KEYWORDS = new HashSet<>(List.of(
            "boss", "dragon", "titan", "monarch", "overlord", "behemoth",
            "leviathan", "harbinger", "monstrosity", "ignis", "wroughtnaut",
            "lich", "hydra", "demon_king", "patriarch", "wither", "warden"
    ));

    private static boolean autoDetect = true;
    private static float minHealth = 80.0F;
    private static boolean initialized = false;

    private BossCompatibilityManager() {}

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
        WHITELIST_BOSSES.clear();
        try {
            Path dir = getConfigDir();
            File folder = dir.toFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = dir.resolve("custom_bosses.json").toFile();
            if (!file.exists()) {
                createDefaultBossJson(file);
            }

            try (FileReader reader = new FileReader(file)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("auto_detect_modded_bosses")) {
                    autoDetect = root.get("auto_detect_modded_bosses").getAsBoolean();
                }
                if (root.has("boss_min_health")) {
                    minHealth = root.get("boss_min_health").getAsFloat();
                }
                if (root.has("boss_whitelist")) {
                    JsonArray arr = root.getAsJsonArray("boss_whitelist");
                    for (JsonElement el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        String id = obj.get("entity").getAsString();
                        int exp = obj.has("customExp") ? obj.get("customExp").getAsInt() : 0;
                        int coins = obj.has("coinBonus") ? obj.get("coinBonus").getAsInt() : 0;
                        String fable = obj.has("fable") ? obj.get("fable").getAsString() : "";
                        WHITELIST_BOSSES.put(id, new BossEntry(id, exp, coins, fable));
                    }
                }
                if (root.has("boss_name_keywords")) {
                    JsonArray arr = root.getAsJsonArray("boss_name_keywords");
                    for (JsonElement el : arr) {
                        KEYWORDS.add(el.getAsString().toLowerCase());
                    }
                }
                AwakeningSystemMod.LOGGER.info("Compatibilidad de jefes cargada: {} jefes en whitelist, auto-detección: {}.", WHITELIST_BOSSES.size(), autoDetect);
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al cargar custom_bosses.json", e);
        }
    }

    private static void createDefaultBossJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Awakening System - Configuración de Compatibilidad Universal de Jefes. Permite registrar cualquier jefe de mods externos (ej: Cataclysm, Mowzie's, Twilight Forest, Iron's Spells).");
            root.addProperty("auto_detect_modded_bosses", true);
            root.addProperty("boss_min_health", 80.0);

            JsonArray whitelist = new JsonArray();
            addBoss(whitelist, "minecraft:ender_dragon", 500, 300, "solo_dragon_slayer");
            addBoss(whitelist, "minecraft:wither", 400, 250, "first_wither");
            addBoss(whitelist, "minecraft:warden", 450, 300, "warden_subjugator");
            addBoss(whitelist, "minecraft:elder_guardian", 200, 150, "");
            addBoss(whitelist, "awakening_system:baran_boss", 600, 500, "first_monarch_defeat");
            addBoss(whitelist, "awakening_system:igris_boss", 550, 450, "first_monarch_defeat");
            addBoss(whitelist, "awakening_system:ancient_celestial_dragon_boss", 800, 600, "solo_dragon_slayer");
            addBoss(whitelist, "awakening_system:elemental_demon_king", 600, 500, "giant_story_demon_king");
            addBoss(whitelist, "awakening_system:blood_patriarch", 650, 500, "");
            addBoss(whitelist, "awakening_system:yggdrasil_treant", 500, 400, "");

            // Jefes conocidos de mods populares
            addBoss(whitelist, "cataclysm:netherite_monstrosity", 700, 600, "");
            addBoss(whitelist, "cataclysm:the_harbinger", 750, 650, "");
            addBoss(whitelist, "cataclysm:the_leviathan", 800, 700, "");
            addBoss(whitelist, "cataclysm:ignis", 850, 750, "");
            addBoss(whitelist, "cataclysm:ender_guardian", 800, 700, "");
            addBoss(whitelist, "mowziesmobs:ferrous_wroughtnaut", 400, 300, "");
            addBoss(whitelist, "mowziesmobs:frostmaw", 500, 400, "");
            addBoss(whitelist, "twilightforest:naga", 350, 250, "");
            addBoss(whitelist, "twilightforest:lich", 450, 350, "");
            addBoss(whitelist, "twilightforest:hydra", 600, 500, "");
            addBoss(whitelist, "twilightforest:ur_ghast", 550, 450, "");
            addBoss(whitelist, "twilightforest:snow_queen", 500, 400, "");
            addBoss(whitelist, "irons_spellbooks:dead_king", 650, 500, "");
            root.add("boss_whitelist", whitelist);

            JsonArray kw = new JsonArray();
            for (String k : KEYWORDS) {
                kw.add(k);
            }
            root.add("boss_name_keywords", kw);

            GSON.toJson(root, writer);
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("No se pudo crear custom_bosses.json por defecto", e);
        }
    }

    private static void addBoss(JsonArray arr, String id, int exp, int coins, String fable) {
        JsonObject o = new JsonObject();
        o.addProperty("entity", id);
        o.addProperty("customExp", exp);
        o.addProperty("coinBonus", coins);
        if (fable != null && !fable.isEmpty()) {
            o.addProperty("fable", fable);
        }
        arr.add(o);
    }

    /**
     * Determina si una entidad viva es considerada un Jefe por el Sistema.
     */
    public static boolean isBoss(LivingEntity entity) {
        if (entity == null) return false;
        init();

        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String idStr = (id != null) ? id.toString() : "";

        // 1. Whitelist explícita en JSON
        if (WHITELIST_BOSSES.containsKey(idStr)) {
            return true;
        }

        // 2. Tags unificadas de NeoForge / Forge / Common
        if (type.is(C_BOSSES) || type.is(FORGE_BOSSES) || type.is(AWAKENING_BOSSES)) {
            return true;
        }

        // 3. Jefes vanilla clásicos
        if (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof Warden || entity instanceof ElderGuardian) {
            return true;
        }

        // 4. Jefes nativos de Awakening System
        String ns = (id != null) ? id.getNamespace() : "";
        if ("awakening_system".equals(ns)) {
            String path = id.getPath();
            if (path.contains("boss") || path.contains("demon_king") || path.contains("patriarch") || path.contains("treant") || path.contains("hypostasis") || path.contains("dragon")) {
                return true;
            }
        }

        // 5. Detección heurística de jefes de otros mods
        if (autoDetect) {
            String path = (id != null) ? id.getPath().toLowerCase() : "";
            for (String kw : KEYWORDS) {
                if (path.contains(kw)) {
                    return true;
                }
            }

            // Entidades que no pueden cambiar de dimensión y tienen mucha vida (típico de jefes de mods)
            if (!entity.canChangeDimensions(entity.level(), entity.level()) && entity.getMaxHealth() >= minHealth) {
                return true;
            }

            // Vida extrema
            if (entity.getMaxHealth() >= 200.0F) {
                return true;
            }
        }

        return false;
    }

    /**
     * Procesa la derrota de un jefe compatible: recompensas de EXP, monedas, fábulas y anuncios.
     */
    public static void handleBossDefeat(LivingEntity boss, ServerPlayer player, SystemData data) {
        init();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType());
        String idStr = (id != null) ? id.toString() : "";
        BossEntry entry = WHITELIST_BOSSES.get(idStr);

        // 1. Experiencia de jefe
        int exp = (entry != null && entry.customExp() > 0)
                ? entry.customExp()
                : Math.min(1000, Math.max(80, (int) (boss.getMaxHealth() * 0.20F)));
        SystemEvents.awardExperience(player, data, exp);

        // 2. Monedas
        int bonusCoins = (entry != null && entry.coinBonus() > 0) ? entry.coinBonus() : 150;
        data.addCoins(bonusCoins);

        // 3. Fábulas y logros
        if (entry != null && entry.fable() != null && !entry.fable().isEmpty()) {
            SystemEvents.grantFable(player, entry.fable());
        }

        // 4. Caída especial: Esencia del Jefe / Núcleo de Maná / Ficha de Ruleta
        if (player.getRandom().nextFloat() < 0.40F) {
            ItemStack token = new ItemStack(SystemItems.ROULETTE_TOKEN.get(), 1 + player.getRandom().nextInt(2));
            boss.spawnAtLocation(token);
        }
        if (player.getRandom().nextFloat() < 0.30F) {
            ItemStack soul = new ItemStack(SystemItems.SOUL_SOLID.get(), 1);
            boss.spawnAtLocation(soul);
        }

        // 5. Anuncio en chat del Sistema
        String bossName = boss.getDisplayName().getString();
        Component msg = Component.literal(String.format("§6✦ [SISTEMA] §e¡El cazador §f%s §eha derrotado al Jefe §c%s§e! §7(+%d EXP, +%d Monedas)",
                player.getGameProfile().getName(), bossName, exp, bonusCoins));
        player.serverLevel().players().forEach(p -> p.sendSystemMessage(msg));
    }
}
