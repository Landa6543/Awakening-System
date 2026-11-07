package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShadowAppleItem extends Item {

    public ShadowAppleItem() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(6)
                        .saturationModifier(1.2F)
                        .alwaysEdible()
                        .build())
                .rarity(Rarity.EPIC)
                .stacksTo(64));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            // Efectos oficiales de la Sección 24.2:
            // Regeneración V (30s = 600t)
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 4));
            // Absorción VI (2 min = 2400t) -> 24 corazones extra
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 5));
            // Resistencia al Fuego (2 min = 2400t)
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400, 0));
            // Visión Nocturna Táctica del Cazador (2 min = 2400t)
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0));

            // Regeneración pasiva ultra-rápida de sombras durante 2 minutos
            player.getPersistentData().putLong("shadow_apple_buff_until", level.getGameTime() + 2400L);

            // Efectos audiovisuales del Sistema / Monarca
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.75F, 1.4F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 0.6, 0.5, 0.08);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 0.8, player.getZ(), 25, 0.4, 0.5, 0.4, 0.05);
            }

            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.restoreMana((int) (data.maxMana() * 0.35F));
                AwakeningSystemMod.sync(player);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.shadow_apple.desc1").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.awakening_system.shadow_apple.desc2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(" "));
        tooltip.add(Component.translatable("item.awakening_system.shadow_apple.effect1").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.shadow_apple.effect2").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.awakening_system.shadow_apple.effect3").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.awakening_system.shadow_apple.effect4").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
