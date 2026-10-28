package com.soybe.awakeningsystem.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CasinoChipItem extends Item {
    public enum ChipTier {
        BRONZE(100, Rarity.COMMON, "§6", 0xD47A3B),
        SILVER(1000, Rarity.UNCOMMON, "§f", 0xE0E0E0),
        GOLD(10000, Rarity.RARE, "§e", 0xFFD700),
        PLATINUM(100000, Rarity.EPIC, "§b", 0x4DEEEA),
        BLACK(500000, Rarity.EPIC, "§5", 0x1A1A1A);

        private final int moraValue;
        private final Rarity rarity;
        private final String colorCode;
        private final int hexColor;

        ChipTier(int moraValue, Rarity rarity, String colorCode, int hexColor) {
            this.moraValue = moraValue;
            this.rarity = rarity;
            this.colorCode = colorCode;
            this.hexColor = hexColor;
        }

        public int getMoraValue() { return moraValue; }
        public Rarity getRarity() { return rarity; }
        public String getColorCode() { return colorCode; }
        public int getHexColor() { return hexColor; }

        public String getDisplayName() {
            return switch (this) {
                case BRONZE -> "Bronze";
                case SILVER -> "Silver";
                case GOLD -> "Gold";
                case PLATINUM -> "Platinum";
                case BLACK -> "Clandestine";
            };
        }

        public Component getDisplayNameComponent() {
            return Component.translatable("item.awakening_system.casino_chip.tier." + name().toLowerCase());
        }
    }

    private final ChipTier tier;

    public CasinoChipItem(ChipTier tier, Properties properties) {
        super(properties.stacksTo(64).rarity(tier.getRarity()));
        this.tier = tier;
    }

    public ChipTier getTier() {
        return tier;
    }

    public int getMoraValue() {
        return tier.getMoraValue();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (tier == ChipTier.BLACK) {
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.clandestine_title"));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.clandestine_desc"));
            tooltip.add(Component.literal(tier.getColorCode()).append(Component.translatable("item.awakening_system.casino_chip.black_market_val", String.format("%,d", tier.getMoraValue()))));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.clandestine_warn"));
        } else {
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.official_desc"));
            tooltip.add(Component.literal(tier.getColorCode()).append(Component.translatable("item.awakening_system.casino_chip.official_val", String.format("%,d", tier.getMoraValue()))));
            tooltip.add(Component.translatable("item.awakening_system.casino_chip.official_hint"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
