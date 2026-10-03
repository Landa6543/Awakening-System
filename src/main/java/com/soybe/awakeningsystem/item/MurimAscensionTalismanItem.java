package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.MurimWorldManager;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
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

public class MurimAscensionTalismanItem extends Item {
    public MurimAscensionTalismanItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            if (data.dantianRealm() >= 1 || data.isekaiUnlocked() || data.systemUpdated()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                MurimWorldManager.teleportToMurim(sp);
                return InteractionResultHolder.consume(stack);
            } else {
                sp.displayClientMessage(Component.translatable("awakening_system.item.murim_talisman.realm_too_low"), true);
                sp.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.item.murim_talisman.tooltip_lore"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.item.murim_talisman.tooltip_desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
