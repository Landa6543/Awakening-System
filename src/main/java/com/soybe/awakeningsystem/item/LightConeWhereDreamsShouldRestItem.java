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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LightConeWhereDreamsShouldRestItem extends Item {
    public LightConeWhereDreamsShouldRestItem(Properties properties) {
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
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.path").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.lore").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.stats_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.stat1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.dreams_rest.stat2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.light_cone.equip_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
