package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Representa los Tótems del Árbol Jerárquico del Sistema:
 * - Tótems Base por Mundo (Overworld, Ice Cave / Escarcha Glacial, Murim Qi / Shaolin, Teyvat Archon)
 * - Tótems Trascendentes de Forja Evolutiva (Abisal, Aleación de Almas, Astralita, Mithril, Adamantita, etc.)
 */
public class AwakeningTotemItem extends Item {

    public enum TotemType {
        OVERWORLD("overworld_totem", ChatFormatting.GREEN),
        GLACIAL_FROST("glacial_frost_totem", ChatFormatting.AQUA),
        MURIM_QI("murim_qi_totem", ChatFormatting.GOLD),
        TEYVAT_ARCHON("teyvat_archon_totem", ChatFormatting.DARK_GREEN),
        ABYSSAL_RESONANT("abyssal_resonant_totem", ChatFormatting.DARK_AQUA),
        SOULFORGE("soulforge_totem", ChatFormatting.DARK_PURPLE),
        ASTRALITE("astralite_totem", ChatFormatting.LIGHT_PURPLE),
        MITHRIL("mithril_totem", ChatFormatting.BLUE),
        ADAMANTITE("adamantite_totem", ChatFormatting.RED),
        SPIRIT_JADE("spirit_jade_totem", ChatFormatting.GREEN),
        ASTRAL("astral_totem", ChatFormatting.YELLOW),
        CELESTIAL("celestial_totem", ChatFormatting.GOLD),
        PRIMORDIAL("primordial_totem", ChatFormatting.LIGHT_PURPLE),
        AWAKENING_MONARCH("awakening_monarch_totem", ChatFormatting.DARK_GRAY);

        private final String id;
        private final ChatFormatting accentColor;

        TotemType(String id, ChatFormatting accentColor) {
            this.id = id;
            this.accentColor = accentColor;
        }

        public String getId() {
            return id;
        }

        public ChatFormatting getAccentColor() {
            return accentColor;
        }
    }

    private final TotemType totemType;

    public AwakeningTotemItem(Properties properties, TotemType totemType) {
        super(properties);
        this.totemType = totemType;
    }

    public TotemType getTotemType() {
        return totemType;
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return totemType == TotemType.GLACIAL_FROST;
    }

    public static boolean isHoldingTotem(LivingEntity entity, TotemType type) {
        if (entity == null) return false;
        ItemStack main = entity.getMainHandItem();
        if (main.getItem() instanceof AwakeningTotemItem tot && tot.getTotemType() == type) {
            return true;
        }
        ItemStack off = entity.getOffhandItem();
        return off.getItem() instanceof AwakeningTotemItem tot && tot.getTotemType() == type;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Los tótems superiores brillan con encantamiento cósmico / rúnico
        return totemType != TotemType.OVERWORLD;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system." + totemType.getId() + ".type_desc")
                .withStyle(totemType.getAccentColor()));
        tooltip.add(Component.translatable("item.awakening_system." + totemType.getId() + ".desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system." + totemType.getId() + ".passive_desc"));
        tooltip.add(Component.translatable("item.awakening_system.totem.activation_hint")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
