package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GalaxyLotteryItem extends Item {

    public GalaxyLotteryItem() {
        super(new Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§5§l['Curio' Gran Lotería Galáctica Interastral]").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§7El artefacto de azar cósmico más célebre de la Corporación de la Paz Interastral."));
        tooltip.add(Component.literal("§b✦ Al romper Vasijas o abrir Cofres del Tesoro:"));
        tooltip.add(Component.literal("§e  - 75% Probabilidad: ¡PREMIO MAYOR! Genera una lluvia de"));
        tooltip.add(Component.literal("§e    Protogemas, Fragmentos Cósmicos y Destinos Entrelazados."));
        tooltip.add(Component.literal("§c  - 25% Probabilidad: ¡LOTERÍA ROTA! El objeto se destruye"));
        tooltip.add(Component.literal("§c    en mil pedazos y reduce tu salud a medio corazón."));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
