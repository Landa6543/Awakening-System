package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class MarrowCleansingPillItem extends Item {
    public MarrowCleansingPillItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int openedIdx = -1;
            for (int i = 0; i < 12; i++) {
                if (!data.isMeridianOpen(i)) {
                    data.openMeridian(i);
                    openedIdx = i;
                    break;
                }
            }

            sp.removeAllEffects();
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 45, 0.5D, 0.8D, 0.5D, 0.2D);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 0.6D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.05D);

            sp.playNotifySound(SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.2F);
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.4F);

            if (openedIdx >= 0) {
                data.addCultivationQi(1000);
                sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                sp.displayClientMessage(Component.translatable("item.awakening_system.marrow_cleansing_pill.opened", (openedIdx + 1)), true);
                AwakeningSystemMod.notify(sp, "awakening_system.item.marrow_pill.title", "awakening_system.item.marrow_pill.desc", 0xFF55FF55, String.valueOf(openedIdx + 1));
            } else {
                data.addCultivationQi(4500);
                sp.displayClientMessage(Component.translatable("item.awakening_system.marrow_cleansing_pill.all_open"), true);
                AwakeningSystemMod.notify(sp, "awakening_system.item.marrow_pill.max_title", "awakening_system.item.marrow_pill.max_desc", 0xFF55FF55);
            }

            AwakeningSystemMod.sync(sp);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.marrow_cleansing_pill.desc"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
