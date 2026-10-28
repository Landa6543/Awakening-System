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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LightConeBeforeDawnItem extends Item {
    public LightConeBeforeDawnItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable("item.awakening_system.light_cone.use_msg"), true);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.light_cone.passing_shore.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.path").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.stats_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.before_dawn.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
