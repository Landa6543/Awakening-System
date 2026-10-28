package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Reloj de Alarma (Alarm Clock).
 * Utilitario de supervivencia salvavidas de Uma Musume.
 * Funciona como un Tótem de la Inmortalidad automático desde cualquier ranura del inventario.
 */
public class AlarmClockItem extends Item {

    public AlarmClockItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_desc1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.mech_desc2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.alarm_clock.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
