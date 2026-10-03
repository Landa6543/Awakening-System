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

public class ThousandYearGinsengItem extends Item {
    public static final FoodProperties GINSENG_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(1.2F)
            .alwaysEdible()
            .build();

    public ThousandYearGinsengItem(Properties properties) {
        super(properties.food(GINSENG_FOOD).rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            SystemData data = SystemEvents.data(player);
            data.addCultivationQi(500);
            data.addExperience(150);
            AwakeningSystemMod.sync(player);

            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 45, 0.5, 0.8, 0.5, 0.2);
                sl.sendParticles(ParticleTypes.COMPOSTER, player.getX(), player.getY() + 0.5, player.getZ(), 30, 0.6, 0.5, 0.6, 0.05);
            }

            player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.4F);
            AwakeningSystemMod.notify(player,
                    "awakening_system.item.thousand_year_ginseng.consumed_title",
                    "awakening_system.item.thousand_year_ginseng.consumed_desc",
                    0xFFFFD700);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.item.thousand_year_ginseng.tooltip_lore"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.item.thousand_year_ginseng.tooltip_effect"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
