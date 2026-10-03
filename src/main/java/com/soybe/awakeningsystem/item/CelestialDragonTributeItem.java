package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Tributo del Dragón Primigenio:
 * Artefacto ceremonial de Qi ancestral elaborado a partir de escamas inmortales y la perla del dragón.
 * Se ofrenda en el Altar del Dragón Celestial en Murim para invocar al Dragón Ancestral de los Nueve Cielos.
 */
public class CelestialDragonTributeItem extends Item {

    public CelestialDragonTributeItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).fireResistant().stacksTo(16));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Brillo encantado de reliquia sagrada
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.awakening_system.celestial_dragon_tribute.title").withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.translatable("item.awakening_system.celestial_dragon_tribute.desc"));
        tooltipComponents.add(Component.translatable("item.awakening_system.celestial_dragon_tribute.use"));
        tooltipComponents.add(Component.translatable("item.awakening_system.celestial_dragon_tribute.lore"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
