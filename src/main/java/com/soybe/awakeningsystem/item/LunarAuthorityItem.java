package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Representa una de las Tres Autoridades Lunares de las Antiguas Diosas de la Luna (Aria, Sonnet, Canon).
 * Necesarias para purificar el debilitamiento de Columbina y despertar su verdadero poder como Diosa Trilunar.
 */
public class LunarAuthorityItem extends Item {

    public enum MoonGoddess {
        ARIA("Aria", "Luna Llena", "§b", "item.awakening_system.lunar_authority_aria.desc"),
        SONNET("Sonnet", "Luna Creciente", "§d", "item.awakening_system.lunar_authority_sonnet.desc"),
        CANON("Canon", "Luna Nueva", "§5", "item.awakening_system.lunar_authority_canon.desc");

        private final String name;
        private final String phase;
        private final String colorCode;
        private final String descKey;

        MoonGoddess(String name, String phase, String colorCode, String descKey) {
            this.name = name;
            this.phase = phase;
            this.colorCode = colorCode;
            this.descKey = descKey;
        }

        public String getGoddessName() { return name; }
        public String getPhase() { return phase; }
        public String getColorCode() { return colorCode; }
        public String getDescKey() { return descKey; }
    }

    private final MoonGoddess goddess;

    public LunarAuthorityItem(MoonGoddess goddess, Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.EPIC));
        this.goddess = goddess;
    }

    public MoonGoddess getGoddess() {
        return goddess;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, 1.5F);
        } else {
            player.displayClientMessage(Component.literal(goddess.getColorCode() + "✦ [" + goddess.getGoddessName() + " - " + goddess.getPhase() + "] §fEsta autoridad cósmica pulsa con energía lunar. Llévala al Altar Trilunar para despertar a Columbina."), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(goddess.getColorCode() + "✦ Autoridad de " + goddess.getGoddessName() + " (" + goddess.getPhase() + ")").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable(goddess.getDescKey()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§7[Reliquia Sagrada de las Tres Diosas de la Luna]").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("§e▸ Consagración: §fColoca las 3 autoridades en el §dAltar Trilunar").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
