package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class HerosWitItem extends Item {
    public HerosWitItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int expPerItem = 1000;
            int totalExp = count * expPerItem;

            sp.giveExperiencePoints(totalExp);
            stack.shrink(count);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
            }
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.2F);
            sp.displayClientMessage(Component.literal("§a§l✦ [INGENIO DEL HÉROE] §f¡Has obtenido §e+" + totalExp + " EXP §f(" + count + "x libros)!"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§9⭐⭐⭐⭐ [Material de EXP de Aventurero]").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("§7Las experiencias y conocimientos de grandes héroes que caminaron por Teyvat.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§a▸ Clic derecho: §fConsumir para ganar §e+1,000 EXP").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§a▸ Shift + Clic derecho: §fConsumir todo el stack de golpe").withStyle(ChatFormatting.DARK_GREEN));
    }
}
