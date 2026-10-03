package com.soybe.awakeningsystem.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;

import java.util.List;

public class ElementalDemonCrownItem extends ArmorItem {

    private static final net.minecraft.resources.ResourceLocation TEXTURE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/elemental_demon_crown.png");

    public ElementalDemonCrownItem(Properties properties) {
        super(ArmorMaterials.NETHERITE, Type.HELMET, properties.rarity(Rarity.EPIC).fireResistant().durability(1200));
    }

    @Override
    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
        return TEXTURE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("item.awakening_system.elemental_demon_crown.title"));
        tooltip.add(Component.translatable("item.awakening_system.elemental_demon_crown.desc"));
        tooltip.add(Component.translatable("item.awakening_system.elemental_demon_crown.knockback"));
        tooltip.add(Component.translatable("item.awakening_system.elemental_demon_crown.resistance"));
        tooltip.add(Component.translatable("item.awakening_system.elemental_demon_crown.power"));
        tooltip.add(Component.translatable("item.awakening_system.elemental_demon_crown.authority"));
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }
}
