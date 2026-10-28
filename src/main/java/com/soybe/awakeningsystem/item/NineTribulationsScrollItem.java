package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
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

public class NineTribulationsScrollItem extends Item {
    public NineTribulationsScrollItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            var data = SystemEvents.data(sp);
            data.addCultivationQi(1500);
            data.addCoins(50000);
            data.addExperience(15000);
            AwakeningSystemMod.sync(sp);

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 70, 0.8D, 1.0D, 0.8D, 0.08D);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 80, 1.0D, 1.2D, 1.0D, 0.15D);

            sp.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            AwakeningSystemMod.notify(sp,
                    "item.awakening_system.nine_tribulations_scroll.used_title",
                    "item.awakening_system.nine_tribulations_scroll.used_desc",
                    0xFFFFD700);

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.nine_tribulations_scroll.desc"));
        tooltip.add(Component.translatable("item.awakening_system.nine_tribulations_scroll.lore1"));
        tooltip.add(Component.translatable("item.awakening_system.nine_tribulations_scroll.lore2"));
        tooltip.add(Component.translatable("item.awakening_system.nine_tribulations_scroll.tag"));
    }
}
