package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
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

public class MoraItem extends Item {
    public MoraItem(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int totalMora = count * 1000;
            data.addMora(totalMora);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);
            sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.9F, 1.4F);
            sp.displayClientMessage(Component.literal("§e§l✦ [MORA] §fHas depositado §e+" + String.format("%,d", totalMora) + " Mora §f(Saldo: §6" + String.format("%,d", data.mora()) + " Mora§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Moneda universal acuñada por el Señor del Geo Morax.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7El catalizador de toda transacción comercial, forja y mejora en Teyvat.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§e▸ Clic derecho: §fDepositar §e1,000 Mora §fen tu monedero").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§e▸ Shift + Clic derecho: §fDepositar todo el stack").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§b▸ Se utiliza en las Gangas de Paimon y mejoras").withStyle(ChatFormatting.AQUA));
    }
}
