package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SystemBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Guía Sagrada del Rito de la Diosa Trilunar.
 * Detalla el lore, la situación de Columbina, la búsqueda del Santuario de las Lunas
 * y el progreso en la recolección de las Tres Autoridades Lunares (Aria, Sonnet, Canon).
 */
public class TrilunarGuideItem extends Item {

    public TrilunarGuideItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
            com.soybe.awakeningsystem.AwakeningSystemClient.openTrilunarGuideScreen();
            return InteractionResultHolder.sidedSuccess(stack, true);
        } else if (player instanceof ServerPlayer sp) {
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.3F);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§d✦ Pergamino Sagrado de Columbina").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7Contiene la crónica de las Tres Diosas de la Luna.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§6Objetivo: §fAyuda a Columbina a recuperar su poder perdido.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§bMisión: §7Superar las pruebas de Aria, Sonnet y Canon.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§dMeta: §eDespertar su poder como Diosa Trilunar (+50% Protos/día).").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§e▸ Clic derecho: §fConsultar estado de la misión y guía del santuario").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
