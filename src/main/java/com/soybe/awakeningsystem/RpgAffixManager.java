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
        FEROCIOUS("Ferocious", 0.25F),
        MYSTIC("Mystic", 0.35F),
        COLOSSAL("Colossal", 6.0F),
        VAMPIRIC("Vampiric", 0.06F),
        TITANIC("Titanic", 4.0F),
        SWIFT("Swift", 0.15F);

        public final String title;
        public final float value;

        Prefix(String title, float value) {
            this.title = title;
            this.value = value;
        }
    }

    public enum Suffix {
        NONE("", 0.0F),
        OF_THE_TEMPEST("of the Tempest", 0.25F),
        OF_THE_ARCHON("of the Archon", 0.40F),
        OF_DESTINY("of Destiny", 0.20F),
        OF_SHADOWS("of Shadows", 0.35F);

        public final String title;
        public final float value;

        Suffix(String title, float value) {
            this.title = title;
            this.value = value;
        }
    }

    public enum Rarity {
        COMMON("Common", ChatFormatting.GRAY),
        RARE("Rare", ChatFormatting.BLUE),
        EPIC("Epic", ChatFormatting.DARK_PURPLE),
        LEGENDARY("Legendary", ChatFormatting.GOLD),
        MYTHIC("Mythic", ChatFormatting.RED);

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
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Invalid prefix in getDamageBonus: {}", tag.getString("rpg_prefix"), e);
        }
        return 0.0F;
    }

    public static float getLifesteal(ItemStack stack) {
        if (!hasAffixes(stack)) return 0.0F;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        try {
            Prefix p = Prefix.valueOf(tag.getString("rpg_prefix"));
            if (p == Prefix.VAMPIRIC) return p.value;
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.debug("Invalid prefix in getLifesteal: {}", tag.getString("rpg_prefix"), e);
        }
        return 0.0F;
    }
}
