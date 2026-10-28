package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Pesas de Tobillo (Ankle Weights).
 * Equipo de entrenamiento de resistencia para Umas y Cazadores.
 * Aplica Lentitud I permanente al estar equipadas pero duplica la EXP al minar y combatir.
 * Al desequiparlas, otorga un estallido masivo de Velocidad IV durante 15 segundos.
 */
public class AnkleWeightsItem extends ArmorItem {

    public AnkleWeightsItem(Properties properties) {
        super(ArmorMaterials.IRON, Type.BOOTS, properties.rarity(Rarity.UNCOMMON).durability(600));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.title").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.desc").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.effect1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.ankle_weights.hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, net.minecraft.world.item.ArmorMaterial.Layer layer, boolean inner) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.soybe.awakeningsystem.AwakeningSystemMod.MOD_ID, "textures/models/armor/ankle_weights_layer_1.png");
    }
}
