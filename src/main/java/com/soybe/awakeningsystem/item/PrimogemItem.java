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

public class PrimogemItem extends Item {
    public PrimogemItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int totalPrimos = count * 160;
            data.addPrimogems(totalPrimos);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
            sp.displayClientMessage(Component.literal("§d§l✦ [PROTOGEMAS] §fHas depositado §d+" + totalPrimos + " Protogemas §f(Total: §6" + data.primogems() + "§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Divisa primordial traída de estrellas lejanas.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Se utiliza en el §eSistema de Deseos (Gacha) §7para invocar armas y poderes.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fDepositar §d160 Protogemas §fal monedero").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§e▸ Shift + Clic derecho: §fDepositar todo el stack").withStyle(ChatFormatting.GOLD));
    }
}
