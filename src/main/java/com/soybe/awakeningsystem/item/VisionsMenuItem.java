package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Selector de Visiones (Visions Menu):
 * Abre la interfaz elemental de Teyvat para sintonizar o cambiar la Visión activa del jugador.
 */
public class VisionsMenuItem extends Item {

    public VisionsMenuItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            com.soybe.awakeningsystem.client.gui.VisionSelectionScreen.open();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6⭐⭐⭐⭐⭐ [Compendio Elemental de Teyvat]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Un tomo arcaico otorgado por las Siete Estatuas de Los Siete.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fAbre el menú para resonar con cualquiera de los 7 elementos primordiales.").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§b▸ Como el Viajero, puedes cambiar tu elemento libremente.").withStyle(ChatFormatting.AQUA));
    }
}
