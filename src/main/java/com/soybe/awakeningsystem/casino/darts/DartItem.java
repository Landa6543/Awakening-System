package com.soybe.awakeningsystem.casino.darts;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DartItem extends Item {

    public DartItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 72000;
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.SPEAR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int duration = this.getUseDuration(stack, entity) - timeLeft;
        if (duration < 3) {
            return;
        }

        float speed;
        float inaccuracy;

        boolean isSneaking = player.isCrouching();

        if (duration < 10) {
            float t = duration / 10.0F;
            speed = 1.0F + t * 0.8F;
            inaccuracy = 1.8F - t * 0.9F;
        } else if (duration <= 30) {
            speed = 2.15F;
            inaccuracy = isSneaking ? 0.08F : 0.22F;
        } else {
            float fatigue = Math.min(2.0F, (duration - 30) / 20.0F);
            speed = 2.05F;
            inaccuracy = (isSneaking ? 0.20F : 0.45F) + fatigue * 0.5F;
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.85F, 1.4F / (level.getRandom().nextFloat() * 0.2F + 0.9F));

        if (!level.isClientSide) {
            DartEntity dart = new DartEntity(level, player);
            dart.setItem(stack);
            dart.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, inaccuracy);
            level.addFreshEntity(dart);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.dart.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.dart.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.dart.fp_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.dart.fp_desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.dart.throw_right").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.awakening_system.dart.crouch_shift").withStyle(ChatFormatting.AQUA));
    }
}
