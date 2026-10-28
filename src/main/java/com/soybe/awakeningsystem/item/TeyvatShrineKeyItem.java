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
        ANEMO("Mondstadt", "Anemo Shrine of Depths", ChatFormatting.GREEN),
        GEO("Liyue", "Geo Shrine of Depths", ChatFormatting.GOLD),
        ELECTRO("Inazuma", "Electro Shrine of Depths", ChatFormatting.LIGHT_PURPLE),
        DENDRO("Sumeru", "Dendro Shrine of Depths", ChatFormatting.DARK_GREEN);

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
        tooltip.add(Component.translatable("item.awakening_system.teyvat_shrine_key.nation", region.format + region.region));
        tooltip.add(Component.literal("§e" + region.title));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_shrine_key.lore"));
    }
}
