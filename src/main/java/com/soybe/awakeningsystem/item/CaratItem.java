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

/**
 * Zanahoria Carat de la Academia Tracen (Uma Musume Pretty Derby).
 * Moneda premium utilizada para reclutar Umas en el Gacha y entrenar sus estadísticas.
 */
public class CaratItem extends Item {

    public static final int CARATS_PER_ITEM = 100;

    public CaratItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int totalCarats = count * CARATS_PER_ITEM;
            data.addCarats(totalCarats);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);

            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.3F);
            sp.displayClientMessage(Component.translatable("item.awakening_system.carat.deposit_msg", totalCarats, data.getCarats()), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.carat.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.carat.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.carat.rates").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.carat.deposit1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.carat.deposit_all").withStyle(ChatFormatting.AQUA));
    }
}
