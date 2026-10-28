package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Herradura Sagrada de Oro (Gold Horseshoe).
 * Modificador pasivo supremo: incrementa la velocidad de movimiento base en un +25%
 * y elimina por completo todo el daño por caída (Feather Falling infinito).
 */
public class GoldHorseshoeItem extends Item {

    public GoldHorseshoeItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.gold_horseshoe.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
