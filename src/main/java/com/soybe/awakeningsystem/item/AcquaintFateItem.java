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

public class AcquaintFateItem extends Item {
    public AcquaintFateItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            data.addAcquaintFates(count);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
            sp.displayClientMessage(Component.literal("§b§l✦ [DESTINO DEL ENCUENTRO] §fHas depositado §b+" + count + " Destino(s) §f(Total: §b" + data.acquaintFates() + "§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Semilla del destino que brilla con un tenue fulgor celeste.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Se utiliza en el §bBanner Permanente del Errante §7para invocaciones generales.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fDepositar en el monedero de Deseos").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§e▸ Shift + Clic derecho: §fDepositar todo el stack").withStyle(ChatFormatting.GOLD));
    }
}
