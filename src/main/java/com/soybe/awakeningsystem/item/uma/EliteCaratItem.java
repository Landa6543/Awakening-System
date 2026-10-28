package com.soybe.awakeningsystem.item.uma;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
 * Carats Mejoradas (Zanahorias de Élite de Tracen).
 * Alimento premium que otorga Velocidad III y Salto Elevado II por 4 minutos.
 */
public class EliteCaratItem extends Item {

    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.8F)
            .alwaysEdible()
            .build();

    public EliteCaratItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).food(FOOD_PROPERTIES).stacksTo(64));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 4800, 2, false, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 4800, 1, false, true, true));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.header").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.effect1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.effect2").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.awakening_system.uma.elite_carat.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
