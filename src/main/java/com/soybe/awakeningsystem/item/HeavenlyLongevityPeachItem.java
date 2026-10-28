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

/**
 * Melocotón milenario de la longevidad e inmortalidad celestial de Murim (Pantao / 蟠桃).
 * Fruto supremo que purifica el alma, cura venenos y desata un inmenso flujo de Qi.
 */
public class HeavenlyLongevityPeachItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(1.4F)
            .alwaysEdible()
            .build();

    public HeavenlyLongevityPeachItem(Properties properties) {
        super(properties.food(FOOD).rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.addCultivationQi(150);
                data.addExperience(100);
                data.restoreEnergy(80);
                AwakeningSystemMod.sync(player);
            }

            player.removeAllEffects();

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.2D, player.getZ(), 60, 0.8D, 1.0D, 0.8D, 0.2D);
                sl.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 1.2D, 1.0D, 1.2D, 0.1D);
            }

            player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
            player.displayClientMessage(Component.translatableWithFallback("item.awakening_system.heavenly_longevity_peach.msg", "§6§l🍑 [PEACH OF IMMORTALITY] §eThe divine fruit regenerates your body and meridians (+150 Supreme Qi)!"), true);
        }
        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.desc1"));
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.desc2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.on_consume"));
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.effect2"));
        tooltip.add(Component.translatable("item.awakening_system.heavenly_longevity_peach.effect3"));
    }
}
