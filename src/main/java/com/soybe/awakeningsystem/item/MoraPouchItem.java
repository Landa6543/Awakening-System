package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class MoraPouchItem extends Item {
    private final int moraAmount;
    private final String tierTitle;

    public MoraPouchItem(int moraAmount, String tierTitle, Properties properties) {
        super(properties.stacksTo(64).rarity(moraAmount >= 50000 ? Rarity.RARE : Rarity.UNCOMMON));
        this.moraAmount = moraAmount;
        this.tierTitle = tierTitle;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int totalMora = count * moraAmount;

            SystemData data = SystemEvents.data(sp);
            data.addMora(totalMora);
            AwakeningSystemMod.sync(sp);
            stack.shrink(count);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.1D);
            }
            sp.playNotifySound(ModSounds.GENSHIN_CHEST_OPEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            sp.displayClientMessage(Component.literal("§6§l✦ [" + tierTitle.toUpperCase() + "] §f¡Has recibido §e+" + String.format("%,d", totalMora) + " Mora §f(Saldo: §6" + String.format("%,d", data.mora()) + "§f)!"), true);

            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6✦ " + tierTitle).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7Contiene §e" + String.format("%,d", moraAmount) + " Mora §7acuñada por Morax.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§a▸ Clic derecho: §fAbrir y depositar en tu monedero del Sistema.").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("§a▸ Shift + Clic derecho: §fAbrir todo el lote de golpe.").withStyle(ChatFormatting.DARK_GREEN));
    }
}
