package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class OmnisciencePouchItem extends Item {

    public OmnisciencePouchItem() {
        super(new Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l['Curio' Bolsa Omnisciente]").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7Un mítico talismán proveniente de las profundidades del Universo Simulado."));
        tooltip.add(Component.literal("§e✦ Fortuna Astral:"));
        tooltip.add(Component.literal("§7  Al romper Vasijas de Teyvat o abrir Cofres del Tesoro,"));
        tooltip.add(Component.literal("§a  duplica automáticamente todas las recompensas y materiales obtenidos."));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
