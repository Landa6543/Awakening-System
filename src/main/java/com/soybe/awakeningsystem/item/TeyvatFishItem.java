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
        MEDAKA("Medaka", "Pez común y vivaz de Teyvat", Rarity.COMMON, 2, 0.3F, ChatFormatting.GRAY),
        AIZEN_MEDAKA("Medaka Aizen", "Pez azul que habita los lagos de Mondstadt", Rarity.UNCOMMON, 3, 0.4F, ChatFormatting.AQUA),
        SWEET_FLOWER_MEDAKA("Medaka Flor Dulce", "Pez dulce de tonos dorados", Rarity.UNCOMMON, 3, 0.4F, ChatFormatting.GOLD),
        GLAZE_MEDAKA("Medaka Esmaltado", "Especie pura de aguas cristalinas de Liyue", Rarity.UNCOMMON, 4, 0.5F, ChatFormatting.YELLOW),
        CRYSTALFISH("Pez Cristalino", "Cuerpo translúcido casi invisible en el agua", Rarity.RARE, 4, 0.6F, ChatFormatting.WHITE),
        VENOMSPINE_FISH("Pez Espina Venenosa", "Aletas puntiagudas cargadas de veneno", Rarity.RARE, 2, 0.2F, ChatFormatting.DARK_PURPLE),
        ABIDING_ANGELFISH("Pez Ángel Sagrado", "Raro pez dorado venerado por los Adeptos", Rarity.EPIC, 6, 0.8F, ChatFormatting.GOLD),
        RAIMEI_ANGELFISH("Pez Ángel Raimei", "Pez electrizante que nada en aguas tormentosas de Inazuma", Rarity.EPIC, 6, 0.8F, ChatFormatting.LIGHT_PURPLE);

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
        tooltip.add(Component.literal("§7Variedad: " + variety.format + variety.name));
        tooltip.add(Component.literal("§8" + variety.description));
        tooltip.add(Component.literal("§ePesca de Teyvat: §7Utilizado en el Gremio de Pescadores para canjes y recetas culinarias."));
    }
}
