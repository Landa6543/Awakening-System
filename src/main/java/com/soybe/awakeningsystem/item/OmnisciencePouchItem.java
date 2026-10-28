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
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.desc"));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.header"));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.omniscience_pouch.desc2"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
