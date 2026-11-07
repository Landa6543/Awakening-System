package com.soybe.awakeningsystem.workshop;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public enum HunterForgeQuality {
    NORMAL("Normal", "§aNormal", ChatFormatting.GREEN, 0x55FF55, 1.08F, 1, "+8% Daño / +1 Def"),
    RARE("Rare", "§bRare", ChatFormatting.AQUA, 0x55FFFF, 1.18F, 2, "+18% Daño / +2 Def"),
    EPIC("Epic", "§dEpic", ChatFormatting.DARK_PURPLE, 0xAA00AA, 1.30F, 3, "+30% Daño / +3 Def"),
    LEGENDARY("Legendary", "§6Legendary", ChatFormatting.GOLD, 0xFFAA00, 1.50F, 5, "+50% Daño / +5 Def / Forja Maestra");

    public static final String TAG_QUALITY = "HunterForgeQuality";
    public static final String TAG_TIMESTAMP = "HunterForgeTimestamp";

    private final String id;
    private final String formattedName;
    private final ChatFormatting formatting;
    private final int colorHex;
    private final float damageMultiplier;
    private final int armorBonus;
    private final String bonusDescription;

    HunterForgeQuality(String id, String formattedName, ChatFormatting formatting, int colorHex, float damageMultiplier, int armorBonus, String bonusDescription) {
        this.id = id;
        this.formattedName = formattedName;
        this.formatting = formatting;
        this.colorHex = colorHex;
        this.damageMultiplier = damageMultiplier;
        this.armorBonus = armorBonus;
        this.bonusDescription = bonusDescription;
    }

    public String getId() { return id; }
    public String getFormattedName() { return formattedName; }
    public ChatFormatting getFormatting() { return formatting; }
    public int getColorHex() { return colorHex; }
    public float getDamageMultiplier() { return damageMultiplier; }
    public int getArmorBonus() { return armorBonus; }
    public String getBonusDescription() { return bonusDescription; }

    public Component getDisplayName() {
        return Component.translatable("awakening_system.workshop.quality." + id.toLowerCase()).withStyle(formatting);
    }

    public static HunterForgeQuality fromName(String name) {
        if (name == null || name.isBlank()) return NORMAL;
        for (HunterForgeQuality q : values()) {
            if (q.name().equalsIgnoreCase(name) || q.id.equalsIgnoreCase(name)) {
                return q;
            }
        }
        return NORMAL;
    }

    public static HunterForgeQuality evaluateFromTiming(int barPosition, int dexterityBonus) {
        // Bar position goes from 0 to 100.
        // Base windows:
        // Legendary: 92..100 (reduced to 88..100 with high dexterity)
        // Epic: 72..91
        // Rare: 42..71
        // Normal: 0..41
        int effectivePos = Math.min(100, barPosition + Math.max(0, dexterityBonus / 10));
        if (effectivePos >= 92) {
            return LEGENDARY;
        } else if (effectivePos >= 72) {
            return EPIC;
        } else if (effectivePos >= 42) {
            return RARE;
        } else {
            return NORMAL;
        }
    }

    public static void applyQuality(ItemStack stack, HunterForgeQuality quality) {
        if (stack == null || stack.isEmpty()) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(TAG_QUALITY, quality.name());
            tag.putLong(TAG_TIMESTAMP, System.currentTimeMillis());
        });
    }

    public static HunterForgeQuality getQuality(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.contains(TAG_QUALITY)) {
            return fromName(cd.copyTag().getString(TAG_QUALITY));
        }
        return null;
    }

    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack == null || stack.isEmpty() || tooltip == null) return;
        HunterForgeQuality quality = getQuality(stack);
        if (quality != null) {
            tooltip.add(Component.translatable("awakening_system.workshop.quality_badge", quality.getDisplayName())
                    .withStyle(quality.getFormatting(), ChatFormatting.BOLD));
            tooltip.add(Component.translatable("awakening_system.workshop.quality_bonus." + quality.getId().toLowerCase())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
