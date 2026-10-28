package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Tracen Clover / Puntos de Spark (Herraduras de Intercambio).
 * Moneda de compasión (Pity / Spark) obtenida en las tiradas del Gacha.
 * Garantiza la adquisición directa de armas celestiales y amuletos legendarios en el sistema de intercambio.
 */
public class CloverSparkItem extends Item {

    public CloverSparkItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.title").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc1").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.desc2").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.uma.clover_spark.hint").withStyle(ChatFormatting.DARK_GREEN));
    }
}
