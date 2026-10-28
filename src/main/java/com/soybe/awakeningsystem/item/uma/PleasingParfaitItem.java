package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Parfait de la Felicidad (Pleasing Parfait).
 * Consumible curativo supremo de la Cafetería Tracen.
 * Limpia todas las alteraciones de estado negativas y restaura salud, regeneración y saturación al 100%.
 */
public class PleasingParfaitItem extends Item {

    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .nutrition(20)
            .saturationModifier(1.0F)
            .alwaysEdible()
            .build();

    public PleasingParfaitItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).food(FOOD_PROPERTIES).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> harmful = entity.getActiveEffects().stream()
                    .filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                    .map(MobEffectInstance::getEffect)
                    .toList();
            for (var effectHolder : harmful) {
                entity.removeEffect(effectHolder);
            }

            entity.setHealth(entity.getMaxHealth());

            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, false, true, true));

            if (entity instanceof Player player) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0F);
                player.displayClientMessage(Component.translatable("item.awakening_system.pleasing_parfait.eaten"), true);
            }

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.6F);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + 1.2, entity.getZ(), 16, 0.5, 0.5, 0.5, 0.1);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect1").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect2").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect3").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.pleasing_parfait.effect4").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
