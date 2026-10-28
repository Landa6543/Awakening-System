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

public class AwakeningCoinItem extends Item {
    public AwakeningCoinItem(Properties properties) {
        super(properties.stacksTo(64).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                int count = stack.getCount();
                stack.shrink(count);

                int coinsAwarded = count * 2;
                SystemData data = SystemEvents.data(sp);
                data.addCoins(coinsAwarded);
                AwakeningSystemMod.sync(sp);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.3F);

                sp.displayClientMessage(Component.translatable("awakening_system.bank.coins_deposited", coinsAwarded, data.coins()), true);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean lookingUp = player.getXRot() < -45.0F;
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            com.soybe.awakeningsystem.entity.AwakeningCoinEntity coin =
                    new com.soybe.awakeningsystem.entity.AwakeningCoinEntity(level, sp, lookingUp);

            if (lookingUp) {
                coin.shootFromRotation(sp, -88.0F, sp.getYRot(), 0.0F, 0.75F, 0.0F);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 2.0F);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.8F);
                sp.displayClientMessage(Component.translatable("awakening_system.item.coin.flip"), true);
            } else {
                coin.shootFromRotation(sp, sp.getXRot(), sp.getYRot(), 0.0F, 1.4F, 1.0F);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.9F, 1.4F);
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 0.6F, 1.8F);
            }

            level.addFreshEntity(coin);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.awakening_coin.desc"));
        tooltip.add(Component.translatable("item.awakening_system.awakening_coin.right_click"));
        tooltip.add(Component.translatable("item.awakening_system.awakening_coin.flip"));
        tooltip.add(Component.translatable("item.awakening_system.awakening_coin.deposit"));
        tooltip.add(Component.translatable("item.awakening_system.awakening_coin.altar"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
