package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SystemUpgradeSmithingTemplateItem extends Item {
    private final String templateName;
    private final ChatFormatting tierColor;

    public SystemUpgradeSmithingTemplateItem(Properties properties, String templateName, ChatFormatting tierColor) {
        super(properties);
        this.templateName = templateName;
        this.tierColor = tierColor;
    }

    @Deprecated
    public SystemUpgradeSmithingTemplateItem(Properties properties, String title, String appliesTo, String ingredients, String tierColor) {
        super(properties.rarity(Rarity.RARE));
        this.templateName = "celestial";
        this.tierColor = ChatFormatting.AQUA;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // ✦ Title ✦
        tooltip.add(Component.literal("✦ ").withStyle(tierColor)
                .append(Component.translatable("item.awakening_system.smithing_template." + templateName + ".title").withStyle(tierColor))
                .append(Component.literal(" ✦").withStyle(tierColor)));

        // Smithing Template / Plantilla de Herrería
        tooltip.add(Component.translatable("item.awakening_system.smithing_template.type").withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.empty());

        // Applies to: ... / Aplica a: ...
        tooltip.add(Component.translatable("item.awakening_system.smithing_template.applies_to").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" "))
                .append(Component.translatable("item.awakening_system.smithing_template." + templateName + ".applies_to").withStyle(tierColor)));

        // Ingredients: ... / Ingredientes: ...
        tooltip.add(Component.translatable("item.awakening_system.smithing_template.ingredients").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" "))
                .append(Component.translatable("item.awakening_system.smithing_template." + templateName + ".ingredients").withStyle(tierColor)));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}
