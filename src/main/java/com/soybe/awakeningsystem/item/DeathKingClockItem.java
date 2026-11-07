package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class DeathKingClockItem extends Item {
    public DeathKingClockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            // El reloj solo tiene un poder: salvarte de la muerte. El clic derecho dice cuándo estará listo.
            SystemData data = SystemEvents.data(sp);
            long remainingTicks = sp.getPersistentData().getLong("death_king_rewind_cooldown") - level.getGameTime();
            if (remainingTicks > 0) {
                long seconds = (remainingTicks + 19) / 20;
                sp.displayClientMessage(Component.translatable("awakening_system.item.death_king_clock.recharging",
                        seconds / 60, String.format("%02d", seconds % 60)), true);
                sp.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.6F, 0.8F);
            } else {
                sp.displayClientMessage(Component.translatable("awakening_system.item.death_king_clock.ready"), true);
                sp.playNotifySound(SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 0.6F, 1.4F);
            }

            if (data != null && !data.skills().containsKey("death_king_rewind")) {
                data.skills().put("death_king_rewind", 1);
                data.gainSkillProgress("death_king_rewind", 1);
                AwakeningSystemMod.notify(sp,
                        "awakening_system.item.death_king_clock.skill_title",
                        "awakening_system.item.death_king_clock.skill_desc",
                        0xFFFFD866);
                AwakeningSystemMod.sync(sp);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.lore"));
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.origin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.passive"));
        tooltip.add(Component.translatable("item.awakening_system.death_king_clock.active"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
