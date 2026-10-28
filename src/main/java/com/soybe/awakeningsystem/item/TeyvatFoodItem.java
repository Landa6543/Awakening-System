package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

public class TeyvatFoodItem extends Item {
    public enum FoodType {
        ADEPTUS_TEMPTATION(
                "Adeptus' Temptation",
                "§6★★★★★ [Legendary Dish of Liyue]",
                "An exquisite simmered delicacy that delights even gods.",
                10,
                1.2F,
                Rarity.EPIC
        ),
        SWEET_MADAME(
                "Sweet Madame",
                "§a★★ [Mondstadt Specialty]",
                "Golden roasted fowl seasoned with sweet flowers. Instantly comforts the body.",
                8,
                0.8F,
                Rarity.UNCOMMON
        ),
        ALMOND_TOFU(
                "Almond Tofu",
                "§b★★★ [Wangshu Inn Specialty]",
                "Sweet dessert favored by the Conqueror of Demons, Xiao. Clears the mind.",
                6,
                0.6F,
                Rarity.RARE
        ),
        SUNSETTIA(
                "Sunsettia",
                "§e★ [Wild Fruit of Teyvat]",
                "A plump and sweet fruit growing on Cuihua trees. Revitalizes the body.",
                4,
                0.5F,
                Rarity.COMMON
        );

        final String title;
        final String starsBadge;
        final String description;
        final int nutrition;
        final float saturation;
        final Rarity rarity;

        FoodType(String title, String starsBadge, String description, int nutrition, float saturation, Rarity rarity) {
            this.title = title;
            this.starsBadge = starsBadge;
            this.description = description;
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.rarity = rarity;
        }
        public String getId() {
            return switch (this) {
                case ADEPTUS_TEMPTATION -> "adeptus_temptation";
                case SWEET_MADAME -> "sweet_madame";
                case ALMOND_TOFU -> "almond_tofu";
                case SUNSETTIA -> "sunsettia";
            };
        }
    }

    private final FoodType foodType;

    public TeyvatFoodItem(FoodType foodType, Properties properties) {
        super(properties.rarity(foodType.rarity).food(
                new FoodProperties.Builder()
                        .nutrition(foodType.nutrition)
                        .saturationModifier(foodType.saturation)
                        .alwaysEdible()
                        .build()
        ));
        this.foodType = foodType;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof Player player) {
            switch (foodType) {
                case ADEPTUS_TEMPTATION -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, false));
                    player.displayClientMessage(Component.translatable("awakening_system.food.adeptus_temptation.msg"), false);
                }
                case SWEET_MADAME -> {
                    player.heal(16.0F);
                    player.removeEffect(MobEffects.POISON);
                    player.removeEffect(MobEffects.WITHER);
                    player.displayClientMessage(Component.translatable("awakening_system.food.sweet_madame.msg"), true);
                }
                case ALMOND_TOFU -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 1, false, false));
                    player.displayClientMessage(Component.translatable("awakening_system.food.almond_tofu.msg"), true);
                }
                case SUNSETTIA -> {
                    player.heal(4.0F);
                    player.displayClientMessage(Component.translatable("awakening_system.food.sunsettia.msg"), true);
                }
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String id = foodType.getId();
        tooltip.add(Component.translatableWithFallback("awakening_system.food." + id + ".badge", foodType.starsBadge).withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7").append(Component.translatableWithFallback("awakening_system.food." + id + ".desc", foodType.description)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("awakening_system.food." + id + ".effect"));
    }
}
