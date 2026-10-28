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
 * Piñón milenario de los pinos ancestrales del Dao en Murim.
 * Endurece el cuerpo de cultivo y otorga resistencia inquebrantable.
 */
public class CelestialPineNutItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(1.0F)
            .alwaysEdible()
            .build();

    public CelestialPineNutItem(Properties properties) {
        super(properties.food(FOOD).rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            if (data != null) {
                data.addCultivationQi(45);
                data.addExperience(50);
                AwakeningSystemMod.sync(player);
            }

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 0, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.1D);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 0.6D, player.getZ(), 15, 0.4D, 0.4D, 0.4D, 0.05D);
            }

            player.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 1.2F);
            player.displayClientMessage(Component.translatableWithFallback("item.awakening_system.celestial_pine_nut.msg", "§2§l🌲 [DAO PINE NUT] §fThe stone essence of the thousand-year pine strengthens your physique (+45 Qi)."), true);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.celestial_pine_nut.title").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.awakening_system.celestial_pine_nut.desc"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.awakening_system.celestial_pine_nut.on_consume"));
        tooltip.add(Component.translatable("item.awakening_system.celestial_pine_nut.effect1"));
        tooltip.add(Component.translatable("item.awakening_system.celestial_pine_nut.effect2"));
    }
}
