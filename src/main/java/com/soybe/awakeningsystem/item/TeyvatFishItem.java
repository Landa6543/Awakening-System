package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Peces de las aguas de Teyvat (Genshin Impact).
 * Pueden consumirse directamente o procesarse en recetas culinarias y forja de armas de pesca.
 */
public class TeyvatFishItem extends Item {

    public enum FishVariety {
        MEDAKA("Medaka", "A lively common fish of Teyvat", Rarity.COMMON, 2, 0.3F, ChatFormatting.GRAY),
        AIZEN_MEDAKA("Aizen Medaka", "Blue fish native to the lakes of Mondstadt", Rarity.UNCOMMON, 3, 0.4F, ChatFormatting.AQUA),
        SWEET_FLOWER_MEDAKA("Sweet-Flower Medaka", "Sweet golden-hued fish", Rarity.UNCOMMON, 3, 0.4F, ChatFormatting.GOLD),
        GLAZE_MEDAKA("Glaze Medaka", "Pure species from the crystalline waters of Liyue", Rarity.UNCOMMON, 4, 0.5F, ChatFormatting.YELLOW),
        CRYSTALFISH("Crystalfish", "Translucent body almost invisible underwater", Rarity.RARE, 4, 0.6F, ChatFormatting.WHITE),
        VENOMSPINE_FISH("Venomspine Fish", "Pointed fins laden with potent venom", Rarity.RARE, 2, 0.2F, ChatFormatting.DARK_PURPLE),
        ABIDING_ANGELFISH("Abiding Angelfish", "Rare golden fish revered by the Adepti", Rarity.EPIC, 6, 0.8F, ChatFormatting.GOLD),
        RAIMEI_ANGELFISH("Raimei Angelfish", "An electrifying fish that swims in stormy Inazuma waters", Rarity.EPIC, 6, 0.8F, ChatFormatting.LIGHT_PURPLE);

        public final String name;
        public final String description;
        public final Rarity rarity;
        public final int nutrition;
        public final float saturation;
        public final ChatFormatting format;

        FishVariety(String name, String description, Rarity rarity, int nutrition, float saturation, ChatFormatting format) {
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.format = format;
        }
    }

    private final FishVariety variety;

    public TeyvatFishItem(FishVariety variety, Properties properties) {
        super(properties
                .rarity(variety.rarity)
                .stacksTo(64)
                .food(new FoodProperties.Builder()
                        .nutrition(variety.nutrition)
                        .saturationModifier(variety.saturation)
                        .build()));
        this.variety = variety;
    }

    public FishVariety getVariety() {
        return variety;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_fish.variety").append(Component.translatableWithFallback("item.awakening_system.teyvat_fish." + variety.name().toLowerCase() + ".name", variety.name).withStyle(variety.format)));
        tooltip.add(Component.translatableWithFallback("item.awakening_system.teyvat_fish." + variety.name().toLowerCase() + ".desc", "§8" + variety.description));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_fish.hint"));
    }
}
