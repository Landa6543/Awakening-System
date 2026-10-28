package com.soybe.awakeningsystem.casino.roulette;

import java.util.Set;

public class RouletteWheel {
    public static final int POCKET_COUNT = 37;

    public static final int[] WHEEL_ORDER = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23,
            10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    public static final Set<Integer> RED_NUMBERS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );

    public enum PocketColor {
        GREEN(0xFF2E7D32, "§aVerde"),
        RED(0xFFC62828, "§cRojo"),
        BLACK(0xFF212121, "§8Negro");

        private final int hexColor;
        private final String displayName;

        PocketColor(int hexColor, String displayName) {
            this.hexColor = hexColor;
            this.displayName = displayName;
        }

        public int getHexColor() {
            return hexColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public net.minecraft.network.chat.Component getComponent() {
            return net.minecraft.network.chat.Component.translatable("awakening_system.roulette.color." + name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    public static PocketColor getColor(int number) {
        if (number == 0) return PocketColor.GREEN;
        return RED_NUMBERS.contains(number) ? PocketColor.RED : PocketColor.BLACK;
    }

    public static int getIndexInWheel(int number) {
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == number) return i;
        }
        return 0;
    }
}
