package com.soybe.awakeningsystem.combat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Combat modes for the Awakening Combat Engine (ACE).
 * Inspired by Epic Fight's stance system, allowing seamless transitions
 * between building/mining and active martial combat.
 */
public enum AceCombatMode {
    MINING("mining", "⛏", ChatFormatting.AQUA, "awakening_system.combat.mode.mining"),
    COMBAT("combat", "⚔", ChatFormatting.GOLD, "awakening_system.combat.mode.combat");

    private final String id;
    private final String icon;
    private final ChatFormatting color;
    private final String translationKey;

    AceCombatMode(String id, String icon, ChatFormatting color, String translationKey) {
        this.id = id;
        this.icon = icon;
        this.color = color;
        this.translationKey = translationKey;
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public Component getFormattedBanner() {
        return Component.literal(icon + " [")
                .withStyle(color)
                .append(getDisplayName())
                .append(Component.literal("] " + icon));
    }

    public AceCombatMode next() {
        return this == MINING ? COMBAT : MINING;
    }

    public static AceCombatMode fromId(String id) {
        if ("combat".equalsIgnoreCase(id)) {
            return COMBAT;
        }
        return MINING;
    }
}
