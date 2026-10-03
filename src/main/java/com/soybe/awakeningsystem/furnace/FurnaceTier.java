package com.soybe.awakeningsystem.furnace;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Niveles de Hornos Avanzados de Ultra-Velocidad (Post-Netherite)
 * Addon de Iron Furnaces / Standalone.
 */
public enum FurnaceTier {
    ABYSSAL("abyssal", 35, "6x", 0x4A0E4E, ChatFormatting.DARK_PURPLE, 0.0F),
    MITHRIL("mithril", 20, "10x", 0x00E5FF, ChatFormatting.AQUA, 0.0F),
    ADAMANTITE("adamantite", 12, "16x", 0x9B111E, ChatFormatting.DARK_RED, 0.0F),
    SPIRIT_JADE("spirit_jade", 8, "25x", 0x00FF88, ChatFormatting.GREEN, 0.20F),
    ASTRAL("astral", 5, "40x", 0xFFD700, ChatFormatting.GOLD, 0.40F);

    private final String id;
    private final int cookTime;
    private final String speedDisplay;
    private final int flameColor;
    private final ChatFormatting textFormat;
    private final float doubleOutputChance;

    FurnaceTier(String id, int cookTime, String speedDisplay, int flameColor, ChatFormatting textFormat, float doubleOutputChance) {
        this.id = id;
        this.cookTime = cookTime;
        this.speedDisplay = speedDisplay;
        this.flameColor = flameColor;
        this.textFormat = textFormat;
        this.doubleOutputChance = doubleOutputChance;
    }

    public String getId() {
        return id;
    }

    public int getCookTime() {
        return cookTime;
    }

    public String getSpeedDisplay() {
        return speedDisplay;
    }

    public int getFlameColor() {
        return flameColor;
    }

    public ChatFormatting getTextFormat() {
        return textFormat;
    }

    public float getDoubleOutputChance() {
        return doubleOutputChance;
    }

    public Component getDisplayName() {
        return Component.translatable("block.awakening_system." + id + "_furnace").withStyle(textFormat);
    }
}
