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

public class IntertwinedFateItem extends Item {
    public IntertwinedFateItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            data.addIntertwinedFates(count);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.6F);
            sp.displayClientMessage(Component.literal("§6§l✦ [DESTINO ENTRELAZADO] §fHas depositado §6+" + count + " Destino(s) §f(Total: §e" + data.intertwinedFates() + "§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Semilla del destino que entrelaza los deseos de diferentes realidades.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7Se utiliza en el §6Banner Promocional de Evento §7para obtener equipo 5★.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§e▸ Clic derecho: §fDepositar en el monedero de Deseos").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§e▸ Shift + Clic derecho: §fDepositar todo el stack").withStyle(ChatFormatting.GOLD));
    }
}
