package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
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

public class SnowLotusItem extends Item {
    public static final FoodProperties LOTUS_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(1.0F)
            .alwaysEdible()
            .build();

    public SnowLotusItem(Properties properties) {
        super(properties.food(LOTUS_FOOD).rarity(Rarity.RARE).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            data.addCultivationQi(300);
            data.addExperience(100);
            AwakeningSystemMod.sync(player);

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2, false, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.8, 0.6, 0.05);
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.5, 0.4, 0.08);
            }

            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.5F);
            AwakeningSystemMod.notify(player,
                    "awakening_system.item.snow_lotus.consumed_title",
                    "awakening_system.item.snow_lotus.consumed_desc",
                    0xFF8BE9FD);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.item.snow_lotus.tooltip_lore"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.item.snow_lotus.tooltip_effect"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
