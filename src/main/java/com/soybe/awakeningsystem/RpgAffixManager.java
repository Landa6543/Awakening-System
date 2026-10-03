package com.soybe.awakeningsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.CustomData;

import java.util.Random;

public final class RpgAffixManager {
    private static final Random RNG = new Random();

    public enum Prefix {
        NONE("", 0.0F),
        FEROCIOUS("Feroz", 0.25F), // +25% Daño físico
        MYSTIC("Místico", 0.35F), // +35% Daño mágico
        COLOSSAL("Colosal", 6.0F), // +6 HP
        VAMPIRIC("Vampírico", 0.06F), // 6% robo de vida (balanceado para evitar inmortalidad)
        TITANIC("Titánico", 4.0F), // +4 armadura
        SWIFT("Raudo", 0.15F); // +15% velocidad

        public final String title;
        public final float value;

        Prefix(String title, float value) {
            this.title = title;
            this.value = value;
        }
    }

    public enum Suffix {
        NONE("", 0.0F),
        OF_THE_TEMPEST("de la Tempestad", 0.25F), // +25% velocidad de ataque
        OF_THE_ARCHON("del Arconte", 0.40F), // +40% regeneración de maná
        OF_DESTINY("del Destino", 0.20F), // +20% crítico
        OF_SHADOWS("de las Sombras", 0.35F); // +35% daño furtivo

        public final String title;
        public final float value;

        Suffix(String title, float value) {
            this.title = title;
            this.value = value;
        }
    }

    public enum Rarity {
        COMMON("Común", ChatFormatting.GRAY),
        RARE("Raro", ChatFormatting.BLUE),
        EPIC("Épico", ChatFormatting.DARK_PURPLE),
        LEGENDARY("Legendario", ChatFormatting.GOLD),
        MYTHIC("Mítico", ChatFormatting.RED);

        public final String displayName;
        public final ChatFormatting format;

        Rarity(String displayName, ChatFormatting format) {
            this.displayName = displayName;
            this.format = format;
        }
    }

    private RpgAffixManager() {}

    public static void rollAffixes(ItemStack stack, int level) {
        if (stack.isEmpty() || (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof ArmorItem) && !(stack.getItem() instanceof net.minecraft.world.item.AxeItem))) {
            return;
        }

        Prefix[] prefixes = Prefix.values();
        Suffix[] suffixes = Suffix.values();

        Prefix pre = prefixes[1 + RNG.nextInt(prefixes.length - 1)];
        Suffix suf = suffixes[1 + RNG.nextInt(suffixes.length - 1)];

        Rarity rarity;
        if (level >= 80) rarity = RNG.nextBoolean() ? Rarity.MYTHIC : Rarity.LEGENDARY;
        else if (level >= 50) rarity = RNG.nextBoolean() ? Rarity.LEGENDARY : Rarity.EPIC;
        else if (level >= 25) rarity = RNG.nextBoolean() ? Rarity.EPIC : Rarity.RARE;
        else rarity = Rarity.RARE;

        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA) != null
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();

        tag.putString("rpg_prefix", pre.name());
        tag.putString("rpg_suffix", suf.name());
        tag.putString("rpg_rarity", rarity.name());
        tag.putInt("rpg_level", level);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Aplicar nombre decorativo
        String original = stack.getHoverName().getString();
        String newName = rarity.format + "[" + rarity.displayName.toUpperCase() + "] " + pre.title + " " + original + " " + suf.title;
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(newName));
    }

    public static boolean hasAffixes(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().contains("rpg_prefix");
    }

    public static float getDamageBonus(ItemStack stack) {
        if (!hasAffixes(stack)) return 0.0F;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        try {
            Prefix p = Prefix.valueOf(tag.getString("rpg_prefix"));
            if (p == Prefix.FEROCIOUS) return p.value;
        } catch (Exception ignored) {}
        return 0.0F;
    }

    public static float getLifesteal(ItemStack stack) {
        if (!hasAffixes(stack)) return 0.0F;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        try {
            Prefix p = Prefix.valueOf(tag.getString("rpg_prefix"));
            if (p == Prefix.VAMPIRIC) return p.value;
        } catch (Exception ignored) {}
        return 0.0F;
    }
}
