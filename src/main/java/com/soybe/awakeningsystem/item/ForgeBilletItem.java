package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Prototipos de Forja de Teyvat (Northlander & Midlander Billets).
 * Materiales base esenciales para forjar armas de 4 estrellas.
 */
public class ForgeBilletItem extends Item {

    public enum BilletSeries {
        NORTHLANDER("Northlander", "Mondstadt / Liyue / Inazuma", ChatFormatting.AQUA),
        MIDLANDER("Midlander", "Sumeru / Fontaine / Natlan", ChatFormatting.GOLD);

        public final String title;
        public final String regions;
        public final ChatFormatting format;

        BilletSeries(String title, String regions, ChatFormatting format) {
            this.title = title;
            this.regions = regions;
            this.format = format;
        }
    }

    public enum WeaponCategory {
        SWORD("Sword", "Agile one-handed swords"),
        CLAYMORE("Claymore", "Heavy two-handed greatswords"),
        BOW("Bow", "Long-range precision bows"),
        CATALYST("Catalyst", "Magical channeling orbs and grimoires"),
        POLEARM("Polearm", "Piercing spears and halberds");

        public final String name;
        public final String desc;

        WeaponCategory(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    private final BilletSeries series;
    private final WeaponCategory category;

    public ForgeBilletItem(BilletSeries series, WeaponCategory category, Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(16));
        this.series = series;
        this.category = category;
    }

    public BilletSeries getSeries() {
        return series;
    }

    public WeaponCategory getCategory() {
        return category;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component seriesComp = Component.translatableWithFallback("item.awakening_system.forge_billet.series_name." + series.name().toLowerCase(), series.title).withStyle(series.format);
        tooltip.add(Component.translatable("item.awakening_system.forge_billet.series", seriesComp));
        Component catComp = Component.translatableWithFallback("item.awakening_system.forge_billet.category_name." + category.name().toLowerCase(), category.name);
        tooltip.add(Component.translatable("item.awakening_system.forge_billet.category", catComp));
        tooltip.add(Component.translatableWithFallback("item.awakening_system.forge_billet.category_desc." + category.name().toLowerCase(), "§8" + category.desc).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.awakening_system.forge_billet.material"));
    }
}
