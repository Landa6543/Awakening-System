package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class WildfireCrownItem extends ArmorItem {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("awakening_system", "textures/models/armor/wildfire_crown_layer_1.png");

    public WildfireCrownItem(Properties properties) {
        super(ArmorMaterials.NETHERITE, Type.HELMET, properties
                .rarity(Rarity.EPIC)
                .durability(Type.HELMET.getDurability(42))
                .fireResistant());
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
        return TEXTURE;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet == stack) {
                // Inmunidad ígnea y visión en lava constante
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, false));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6✦ Corona del Jefe Wildfire").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("§7\"Canaliza la esencia ardiente del coloso de fuego del Nether.\""));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§e✦ Bendición Ígnea Primordial:").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  §7Otorga resistencia absoluta y permanente al fuego y a la lava mientras se lleve equipada."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
