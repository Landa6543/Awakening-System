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

public class WuXingManualItem extends Item {
    public WuXingManualItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            var data = SystemEvents.data(sp);
            data.addCultivationQi(1200);
            data.addCoins(350000);
            data.addExperience(30000);
            AwakeningSystemMod.sync(sp);

            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 12000, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12000, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 100, 1.2D, 1.2D, 1.2D, 0.2D);
            sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 5, 0, 0, 0, 0);

            sp.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.1F);
            sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

            AwakeningSystemMod.notify(sp,
                    "item.awakening_system.wu_xing_manual.learned_title",
                    "item.awakening_system.wu_xing_manual.learned_desc",
                    0xFF00FF7F);

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.wu_xing_manual.desc"));
        tooltip.add(Component.translatable("item.awakening_system.wu_xing_manual.lore"));
    }
}
