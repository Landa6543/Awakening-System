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
        NORTHLANDER("Tierras del Norte", "Mondstadt / Liyue / Inazuma", ChatFormatting.AQUA),
        MIDLANDER("Tierras Centrales", "Sumeru / Fontaine / Natlan", ChatFormatting.GOLD);

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
        SWORD("Espada Ligera", "Espadas ágiles de una mano"),
        CLAYMORE("Mandoble", "Grandes espadas pesadas de dos manos"),
        BOW("Arco", "Arcos de precisión de largo alcance"),
        CATALYST("Catalizador", "Orbes y grimorios de canalización mágica"),
        POLEARM("Lanza", "Lanzas y alabardas perforantes");

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
        tooltip.add(Component.literal("§7Serie: " + series.format + series.title));
        tooltip.add(Component.literal("§7Tipo de Arma: §f" + category.name));
        tooltip.add(Component.literal("§8" + category.desc));
        tooltip.add(Component.literal("§eMaterial de Forja: §7Se usa con Cristal Mágico y Trozos de Hierro Blanco para crear armas de 4 estrellas."));
    }
}
