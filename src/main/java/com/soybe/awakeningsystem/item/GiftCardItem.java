package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
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
import java.util.Locale;

public class GiftCardItem extends Item {
    private final int usdValue;

    public GiftCardItem(int usdValue, Rarity rarity) {
        super(new Item.Properties().stacksTo(64).rarity(rarity));
        this.usdValue = usdValue;
    }

    public int getUsdValue() {
        return usdValue;
    }

    public int getCoinCost() {
        return usdValue * 5000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            boolean has40 = data.system40Updated() || data.isSystem40Updating() || data.teyvatUnlocked();
            if (!has40) {
                sp.displayClientMessage(Component.literal("§c✦ Las Tarjetas de Regalo requieren la Versión 4.0 (Teyvat) del Sistema."), true);
                sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResultHolder.fail(stack);
            }
            data.addUsdBalance(this.usdValue);
            sp.displayClientMessage(Component.translatable("awakening_system.gift_card.redeem_success", this.usdValue, String.format(Locale.US, "%.2f", data.usdBalance())), true);
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            AwakeningSystemMod.sync(sp);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_title"));
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_val", usdValue));
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_cost", String.format(Locale.US, "%,d", getCoinCost())));
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_rate"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_redeem_header"));
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_genshin"));
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_hsr"));
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_welkin"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.gift_card.lore_rc_click"));
    }
}
