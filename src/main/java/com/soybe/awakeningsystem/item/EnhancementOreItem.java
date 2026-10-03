package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EnhancementOreItem extends Item {
    private final int expValue;
    private final int stars;

    public EnhancementOreItem(int expValue, int stars, Properties properties) {
        super(properties.stacksTo(64).rarity(stars >= 4 ? Rarity.RARE : Rarity.UNCOMMON));
        this.expValue = expValue;
        this.stars = stars;
    }

    public int getExpValue() {
        return expValue;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starsStr = "★".repeat(stars);
        tooltip.add(Component.literal("§9" + starsStr + " [Mineral de Mejora de Armas y Artefactos]").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7Mineral condensado que contiene §e+" + String.format("%,d", expValue) + " EXP §7de fortalecimiento.").withStyle(ChatFormatting.GRAY));
    }
}
