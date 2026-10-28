package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ElementalGemItem extends Item {
    private final String elementName;
    private final String tierName;
    private final String colorCode;
    private final int stars;

    public ElementalGemItem(String elementName, String tierName, String colorCode, int stars, Properties properties) {
        super(properties.stacksTo(64).rarity(stars >= 5 ? Rarity.EPIC : (stars >= 4 ? Rarity.RARE : Rarity.UNCOMMON)));
        this.elementName = elementName;
        this.tierName = tierName;
        this.colorCode = colorCode;
        this.stars = stars;
    }

    public String getElementName() {
        return elementName;
    }

    public String getTierName() {
        return tierName;
    }

    public int getStars() {
        return stars;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String starsStr = "★".repeat(stars);
        tooltip.add(Component.translatable("item.awakening_system.elemental_gem.header", colorCode, starsStr, elementName).withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.elemental_gem.desc", elementName).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.elemental_gem.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
