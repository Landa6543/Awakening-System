package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Llaves de los Santuarios de las Profundidades de Teyvat (Shrine of Depths Keys).
 * Permite desbloquear los sellos antiguos y cofres de lujo en las ruinas arcanas de cada nación.
 */
public class TeyvatShrineKeyItem extends Item {

    public enum ShrineRegion {
        ANEMO("Mondstadt", "Santuario de las Profundidades de Anemo", ChatFormatting.GREEN),
        GEO("Liyue", "Santuario de las Profundidades de Geo", ChatFormatting.GOLD),
        ELECTRO("Inazuma", "Santuario de las Profundidades de Electro", ChatFormatting.LIGHT_PURPLE),
        DENDRO("Sumeru", "Santuario de las Profundidades de Dendro", ChatFormatting.DARK_GREEN);

        public final String region;
        public final String title;
        public final ChatFormatting format;

        ShrineRegion(String region, String title, ChatFormatting format) {
            this.region = region;
            this.title = title;
            this.format = format;
        }
    }

    private final ShrineRegion region;

    public TeyvatShrineKeyItem(ShrineRegion region, Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
        this.region = region;
    }

    public ShrineRegion getRegion() {
        return region;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Nación: " + region.format + region.region));
        tooltip.add(Component.literal("§e" + region.title));
        tooltip.add(Component.literal("§8Capaz de romper los sellos sagrados que bloquean el acceso al tesoro de las civilizaciones pasadas."));
    }
}
