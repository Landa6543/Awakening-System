package com.soybe.awakeningsystem.compat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Manejador de compatibilidad integral con Apotheosis y su sistema de rarezas (Affixes & Rarities).
 * Detecta y aprovecha las rarezas de Apotheosis (Common, Uncommon, Rare, Epic, Mythic, Ancient)
 * en el botín de mazmorras, recompensas de ruleta, venta en tienda, armas levelables y extracción de sombras.
 */
public final class ApotheosisCompatHandler {

    public static final String MOD_ID = "apotheosis";

    public enum ApothRarity {
        NONE("none", "awakening_system.compat.apotheosis.rarity.none", "Común", 0xFFAAAAAA, 0, 1.0),
        COMMON("common", "awakening_system.compat.apotheosis.rarity.common", "Común", 0xFFFFFFFF, 1, 1.25),
        UNCOMMON("uncommon", "awakening_system.compat.apotheosis.rarity.uncommon", "Poco Común", 0xFF55FF55, 2, 1.5),
        RARE("rare", "awakening_system.compat.apotheosis.rarity.rare", "Raro", 0xFF5555FF, 3, 2.2),
        EPIC("epic", "awakening_system.compat.apotheosis.rarity.epic", "Épico", 0xFFAA00AA, 4, 3.8),
        MYTHIC("mythic", "awakening_system.compat.apotheosis.rarity.mythic", "Mítico", 0xFFFF5555, 5, 6.5),
        ANCIENT("ancient", "awakening_system.compat.apotheosis.rarity.ancient", "Ancestral", 0xFF00FFFF, 6, 12.0);

        private final String id;
        private final String translationKey;
        private final String defaultName;
        private final int color;
        private final int tier;
        private final double sellMultiplier;

        ApothRarity(String id, String translationKey, String defaultName, int color, int tier, double sellMultiplier) {
            this.id = id;
            this.translationKey = translationKey;
            this.defaultName = defaultName;
            this.color = color;
            this.tier = tier;
            this.sellMultiplier = sellMultiplier;
        }

        public String id() { return id; }
        public String translationKey() { return translationKey; }
        public String defaultName() { return defaultName; }
        public int color() { return color; }
        public int tier() { return tier; }
        public double sellMultiplier() { return sellMultiplier; }

        public Component getDisplayName() {
            return Component.translatableWithFallback(translationKey, defaultName);
        }

        public String getPrefixFormatted() {
            return switch (this) {
                case COMMON -> "§f[Común] ";
                case UNCOMMON -> "§a[Poco Común] ";
                case RARE -> "§9[Raro] ";
                case EPIC -> "§5[Épico] ";
                case MYTHIC -> "§c§l[Mítico] ";
                case ANCIENT -> "§b§l[Ancestral] ";
                default -> "";
            };
        }

        public static ApothRarity fromString(String str) {
            if (str == null || str.isEmpty()) return NONE;
            String lower = str.toLowerCase(Locale.ROOT);
            if (lower.contains("ancient")) return ANCIENT;
            if (lower.contains("mythic")) return MYTHIC;
            if (lower.contains("epic")) return EPIC;
            if (lower.contains("rare")) return RARE;
            if (lower.contains("uncommon")) return UNCOMMON;
            if (lower.contains("common")) return COMMON;
            return NONE;
        }

        public ApothRarity next() {
            return switch (this) {
                case NONE -> COMMON;
                case COMMON -> UNCOMMON;
                case UNCOMMON -> RARE;
                case RARE -> EPIC;
                case EPIC -> MYTHIC;
                case MYTHIC, ANCIENT -> ANCIENT;
            };
        }
    }

    private static Boolean loadedCache = null;
    private static Method getRarityMethod = null;
    private static boolean methodSearched = false;

    private ApotheosisCompatHandler() {}

    public static boolean isLoaded() {
        if (loadedCache == null) {
            loadedCache = ModList.get().isLoaded(MOD_ID)
                    || ModList.get().isLoaded("apothic_enchanting")
                    || ModList.get().isLoaded("apothic_attributes");
        }
        return loadedCache;
    }

    /**
     * Obtiene la rareza de Apotheosis de un ItemStack.
     * Consulta primero por reflexión sobre AffixHelper de Apotheosis y si no, por NBT/DataComponents.
     */
    public static ApothRarity getRarity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ApothRarity.NONE;

        if (isLoaded()) {
            initReflection();
            if (getRarityMethod != null) {
                try {
                    Object result = getRarityMethod.invoke(null, stack);
                    if (result != null) {
                        String str = result.toString();
                        ApothRarity r = ApothRarity.fromString(str);
                        if (r != ApothRarity.NONE) return r;
                    }
                } catch (Throwable ignored) {}
            }
        }

        // Inspección de DataComponents / NBT CustomData
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("affix_data")) {
                CompoundTag affixTag = tag.getCompound("affix_data");
                if (affixTag.contains("rarity")) {
                    return ApothRarity.fromString(affixTag.getString("rarity"));
                }
            }
            if (tag.contains("apoth.rarity")) {
                return ApothRarity.fromString(tag.getString("apoth.rarity"));
            }
            if (tag.contains("rarity")) {
                return ApothRarity.fromString(tag.getString("rarity"));
            }
        }

        return ApothRarity.NONE;
    }

    /**
     * Aplica o actualiza una rareza de Apotheosis a un ItemStack.
     */
    public static void setRarity(ItemStack stack, ApothRarity rarity) {
        if (stack == null || stack.isEmpty() || rarity == ApothRarity.NONE) return;

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag affixData = tag.contains("affix_data") ? tag.getCompound("affix_data") : new CompoundTag();
            affixData.putString("rarity", "apotheosis:" + rarity.id());
            tag.put("affix_data", affixData);
            tag.putString("apoth.rarity", "apotheosis:" + rarity.id());
        });
    }

    /**
     * Sube de nivel la rareza de Apotheosis de un objeto (usado en el minijuego de apuesta/ruleta de mazmorras).
     */
    public static ItemStack upgradeRarity(ItemStack stack, RandomSource rng) {
        if (stack == null || stack.isEmpty()) return stack;
        ApothRarity current = getRarity(stack);
        ApothRarity next = current.next();
        setRarity(stack, next);
        return stack;
    }

    /**
     * Obtiene la rareza de un mob moddeado / afijado de Apotheosis.
     */
    public static ApothRarity getMobRarity(LivingEntity mob) {
        if (mob == null) return ApothRarity.NONE;

        for (String tag : mob.getTags()) {
            if (tag.startsWith("apoth") || tag.contains("rarity")) {
                ApothRarity r = ApothRarity.fromString(tag);
                if (r != ApothRarity.NONE) return r;
            }
        }

        CompoundTag persistent = mob.getPersistentData();
        if (persistent.contains("apoth.boss")) {
            return ApothRarity.MYTHIC;
        }
        if (persistent.contains("apoth.rarity")) {
            return ApothRarity.fromString(persistent.getString("apoth.rarity"));
        }
        if (persistent.contains("affix_data")) {
            CompoundTag affix = persistent.getCompound("affix_data");
            if (affix.contains("rarity")) {
                return ApothRarity.fromString(affix.getString("rarity"));
            }
        }

        if (mob.getCustomName() != null) {
            String name = mob.getCustomName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("mítico") || name.contains("mythic")) return ApothRarity.MYTHIC;
            if (name.contains("ancestral") || name.contains("ancient")) return ApothRarity.ANCIENT;
            if (name.contains("épico") || name.contains("epic")) return ApothRarity.EPIC;
            if (name.contains("raro") || name.contains("rare")) return ApothRarity.RARE;
            if (name.contains("poco común") || name.contains("uncommon")) return ApothRarity.UNCOMMON;
        }

        return ApothRarity.NONE;
    }

    /**
     * Genera un ítem temático de recompensa de Apotheosis según el tier o rango de mazmorra.
     */
    public static ItemStack createDungeonReward(int tier, RandomSource rng) {
        if (!isLoaded()) return ItemStack.EMPTY;

        String itemPath;
        int count = 1;

        if (tier >= 10) { // Rango S / Puerta Roja
            int roll = rng.nextInt(5);
            if (roll == 0) {
                itemPath = "godforged_pearl";
            } else if (roll == 1) {
                itemPath = "mythic_material";
                count = 2 + rng.nextInt(3);
            } else if (roll == 2) {
                itemPath = "sigil_of_enhancement";
            } else if (roll == 3) {
                itemPath = "prismatic_web";
                count = 1 + rng.nextInt(2);
            } else {
                itemPath = "sigil_of_socketing";
            }
        } else if (tier >= 7) { // Rango A
            int roll = rng.nextInt(4);
            if (roll == 0) {
                itemPath = "epic_material";
                count = 2 + rng.nextInt(2);
            } else if (roll == 1) {
                itemPath = "sigil_of_socketing";
            } else if (roll == 2) {
                itemPath = "vial_of_extraction";
            } else {
                itemPath = "rare_material";
                count = 3 + rng.nextInt(3);
            }
        } else if (tier >= 5) { // Rango B
            int roll = rng.nextInt(3);
            if (roll == 0) {
                itemPath = "rare_material";
                count = 2;
            } else if (roll == 1) {
                itemPath = "sigil_of_withdrawal";
            } else {
                itemPath = "vial_of_expulsion";
            }
        } else { // Rango C / D
            int roll = rng.nextInt(2);
            if (roll == 0) {
                itemPath = "uncommon_material";
                count = 2 + rng.nextInt(3);
            } else {
                itemPath = "gem_dust";
                count = 4 + rng.nextInt(6);
            }
        }

        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(MOD_ID, itemPath);
        if (BuiltInRegistries.ITEM.containsKey(rl)) {
            Item item = BuiltInRegistries.ITEM.get(rl);
            return new ItemStack(item, count);
        }

        return ItemStack.EMPTY;
    }

    private static void initReflection() {
        if (methodSearched) return;
        methodSearched = true;

        String[] classCandidates = new String[]{
                "dev.shadowsoffire.apotheosis.affix.AffixHelper",
                "dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper",
                "dev.shadowsoffire.apoth.affix.AffixHelper"
        };

        for (String cName : classCandidates) {
            try {
                Class<?> clazz = Class.forName(cName);
                getRarityMethod = clazz.getMethod("getRarity", ItemStack.class);
                if (getRarityMethod != null) {
                    AwakeningSystemMod.LOGGER.info("ApotheosisCompatHandler: Vinculado exitosamente con {}", cName);
                    break;
                }
            } catch (Throwable ignored) {}
        }
    }
}
