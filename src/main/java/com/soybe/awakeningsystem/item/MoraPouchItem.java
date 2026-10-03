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
            sp.displayClientMessage(Component.translatable("awakening_system.item.mora_pouch.opened", Component.translatable(this.getDescriptionId()).getString().toUpperCase(), String.format(java.util.Locale.US, "%,d", totalMora), String.format(java.util.Locale.US, "%,d", data.mora())), true);

            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("awakening_system.item.mora_pouch.desc", String.format(java.util.Locale.US, "%,d", moraAmount)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("awakening_system.item.mora_pouch.use1").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("awakening_system.item.mora_pouch.use2").withStyle(ChatFormatting.DARK_GREEN));
    }
}
