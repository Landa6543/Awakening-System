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

public class MasterlessStarglitterItem extends Item {
    public MasterlessStarglitterItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(999));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            data.addStarglitter(count);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.8F);
            sp.displayClientMessage(Component.literal("§6§l✦ [BRILLO ESTELAR] §fHas depositado §6+" + count + " Brillo Estelar sin Dueño §f(Saldo: §6" + data.starglitter() + "§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Preciado resplandor estelar obtenido de Deseos 4★ y 5★.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Canjeable en las §eGangas de Paimon §7por Pactos de Compañeros y Destinos.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§6▸ Clic derecho: §fDepositar en el saldo de Gangas").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§6▸ Shift + Clic derecho: §fDepositar todo el stack").withStyle(ChatFormatting.GOLD));
    }
}
