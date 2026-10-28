package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class DarkStaminaElixirItem extends Item {

    public static final String NBT_DARK_ELIXIR_TICKS = "UmaDarkElixirTicks";
    public static final int DURATION_TICKS = 3600;

    public DarkStaminaElixirItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 28;
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
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, 2, false, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, DURATION_TICKS, 1, false, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_TICKS, 0, false, true, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + 1.0D, entity.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.08D);
                sl.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + 1.2D, entity.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.05D);
            }

            if (entity instanceof Player player) {
                player.getPersistentData().putInt(NBT_DARK_ELIXIR_TICKS, DURATION_TICKS);
                player.displayClientMessage(Component.translatable("item.awakening_system.dark_stamina_elixir.drank"), true);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.title").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.effect3").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.penalty_header").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.dark_stamina.penalty_desc").withStyle(ChatFormatting.GRAY));
    }
}
