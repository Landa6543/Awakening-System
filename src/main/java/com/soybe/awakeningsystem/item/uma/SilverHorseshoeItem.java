package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Herradura Sagrada de Plata (Silver Horseshoe).
 * Modificador pasivo que incrementa la velocidad de movimiento base en un +15%
 * con solo llevarlo en el inventario o ranura de armadura.
 */
public class SilverHorseshoeItem extends Item {

    public SilverHorseshoeItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.title").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.silver_horseshoe.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
