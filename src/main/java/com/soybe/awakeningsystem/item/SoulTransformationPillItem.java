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

public class SoulTransformationPillItem extends Item {
    public SoulTransformationPillItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            var data = SystemEvents.data(sp);
            data.addCultivationQi(3000);
            data.addCoins(100000);
            AwakeningSystemMod.sync(sp);

            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2));

            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.6D, 0.6D, 0.6D, 0.1D);

            sp.playNotifySound(SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.9F, 1.2F);
            sp.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.4F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.soul_transformation_pill.used"), true);

            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.soul_transformation_pill.desc"));
    }
}
