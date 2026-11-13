package com.soybe.awakeningsystem.mortis;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The link ring: a dark red hoop with a white band, worn at the neck or the wrist (Curios slots), meant to
 * work as a telephone. The voice link itself needs a voice mod and is not wired yet; for now using the ring
 * pings every other ring-bearer nearby, so a group can at least call each other over.
 */
public class LinkRingItem extends Item {
    public LinkRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int reached = 0;
            for (Player other : level.players()) {
                if (other == player || other.distanceToSqr(player) > 256 * 256) continue;
                if (!other.getInventory().contains(new ItemStack(this))) continue;
                other.displayClientMessage(Component.translatable("item.awakening_system.link_ring.ping", player.getName())
                        .withStyle(ChatFormatting.RED), true);
                level.playSound(null, other.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
                reached++;
            }
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.6F, 1.2F);
            player.displayClientMessage(Component.translatable(reached > 0 ? "item.awakening_system.link_ring.sent"
                    : "item.awakening_system.link_ring.nobody", reached).withStyle(ChatFormatting.GRAY), true);
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.link_ring.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.link_ring.desc2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
