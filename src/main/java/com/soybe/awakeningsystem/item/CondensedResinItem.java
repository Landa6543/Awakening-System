package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
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

/**
 * Resina Concentrada (Condensed Resin) de Genshin Impact:
 * Cristal de ámbar puro que almacena energía elemental concentrada.
 * Crafteada con 1 Núcleo de Cristal + 40 Protogemas.
 * Otorga Bendición de Dominio durante 15 minutos: Duplica EXP y botín de mazmorras.
 */
public class CondensedResinItem extends Item {

    public CondensedResinItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            stack.shrink(1);

            sp.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 18000, 1, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 18000, 0, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, true, true));
            sp.getFoodData().eat(20, 20.0F);

            sp.getPersistentData().putLong("condensed_resin_until", sp.level().getGameTime() + 18000L);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 0.4D, 0.5D, 0.4D, 0.05D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.1D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 1.1F);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.7F, 1.2F);
            }

            sp.displayClientMessage(Component.translatable("item.awakening_system.condensed_resin.use_message"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.blessing_title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.blessing_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.blessing_desc2"));
        tooltip.add(Component.translatable("item.awakening_system.condensed_resin.recipe"));
    }
}
