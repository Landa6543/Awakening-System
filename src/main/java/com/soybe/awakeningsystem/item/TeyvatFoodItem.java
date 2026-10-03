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
                "Tentación de Adeptus",
                "§6★★★★★ [Plato Legendario de Liyue]",
                "Exquisito manjar cocinado a fuego lento que deleita incluso a los dioses.",
                10,
                1.2F,
                Rarity.EPIC
        ),
        SWEET_MADAME(
                "Pollo Dulce",
                "§a★★ [Especialidad de Mondstadt]",
                "Pollo asado dorado con flores dulces. Reconforta el cuerpo al instante.",
                8,
                0.8F,
                Rarity.UNCOMMON
        ),
        ALMOND_TOFU(
                "Tofu de Almendras",
                "§b★★★ [Especialidad de la Posada Wangshu]",
                "Postre dulce favorito del Cazador de Demonios Xiao. Aclara los pensamientos.",
                6,
                0.6F,
                Rarity.RARE
        ),
        SUNSETTIA(
                "Fruta del Ocaso",
                "§e★ [Fruta Silvestre de Teyvat]",
                "Fruta rolliza y dulce que crece en los árboles de Cuihua. Reconforta el cuerpo.",
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
                    player.displayClientMessage(Component.literal("§6§l✦ [TENTACIÓN DE ADEPTUS] §f¡Poder celestial imbuido! (+30% Daño de Ataque, Absorción y Regeneración durante 5 min)."), false);
                }
                case SWEET_MADAME -> {
                    player.heal(16.0F);
                    player.removeEffect(MobEffects.POISON);
                    player.removeEffect(MobEffects.WITHER);
                    player.displayClientMessage(Component.literal("§a§l✦ [POLLO DULCE] §f¡Salud restaurada (+16 HP) y efectos negativos purificados!"), true);
                }
                case ALMOND_TOFU -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 1, false, false));
                    player.displayClientMessage(Component.literal("§b§l✦ [TOFU DE ALMENDRAS] §f¡La agilidad de Xiao fluye en ti! (+Velocidad y Suerte aumentadas durante 5 min)."), true);
                }
                case SUNSETTIA -> {
                    player.heal(4.0F);
                    player.displayClientMessage(Component.literal("§e§l✦ [FRUTA DEL OCASO] §f¡Dulce y jugosa! (+4 HP restaurados)."), true);
                }
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(foodType.starsBadge).withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7" + foodType.description).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        switch (foodType) {
            case ADEPTUS_TEMPTATION -> tooltip.add(Component.literal("§6▸ Efecto: §fFuerza II, Absorción II y Regeneración por 5 min.").withStyle(ChatFormatting.YELLOW));
            case SWEET_MADAME -> tooltip.add(Component.literal("§a▸ Efecto: §fCura +16 HP al instante y purga veneno/descomposición.").withStyle(ChatFormatting.GREEN));
            case ALMOND_TOFU -> tooltip.add(Component.literal("§b▸ Efecto: §fVelocidad II, Prisa II y Suerte II por 5 min.").withStyle(ChatFormatting.AQUA));
            case SUNSETTIA -> tooltip.add(Component.literal("§e▸ Efecto: §fRestaura 4 HP al instante.").withStyle(ChatFormatting.YELLOW));
        }
    }
}
