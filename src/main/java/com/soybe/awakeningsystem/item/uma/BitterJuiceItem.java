package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Jugo Amargo (Bitter Juice).
 * Bebida de entrenamiento intensivo de Tracen con mecánica de alto riesgo y beneficio.
 * Otorga Fuerza II y duplicación de XP global durante 5 minutos.
 * Al expirar, aplica Hambre II y Debilidad I durante 1 minuto.
 */
public class BitterJuiceItem extends Item {

    public static final String NBT_BITTER_JUICE_TICKS = "UmaBitterJuiceTicks";
    public static final int DURATION_TICKS = 6000;

    public BitterJuiceItem(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(16));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_TICKS, 1, false, true, true));
            if (entity instanceof Player player) {
                player.getPersistentData().putInt(NBT_BITTER_JUICE_TICKS, DURATION_TICKS);
                player.displayClientMessage(Component.translatable("item.awakening_system.bitter_juice.drank"), true);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.title").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.effect3").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.penalty_header").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.bitter_juice.penalty_desc").withStyle(ChatFormatting.GRAY));
    }
}
