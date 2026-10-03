package com.soybe.awakeningsystem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * Piedras de Resonancia de Óculos de Teyvat.
 * Emite pulsos luminosos arcanos para rastrear la energía elemental más cercana.
 */
public class OculusResonanceStoneItem extends Item {

    private final OculusItem.OculusElement element;

    public OculusResonanceStoneItem(OculusItem.OculusElement element, Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(16));
        this.element = element;
    }

    public OculusItem.OculusElement getElement() {
        return element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.5, sp.getZ(), 30, 0.8, 0.8, 0.8, 0.1);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.6F);

            sp.displayClientMessage(Component.literal("§e✦ La " + element.format + "Piedra de Resonancia (" + element.title + ") §evibra suavemente, sintonizando las corrientes elementales de la región...")
                    .withStyle(ChatFormatting.GOLD), true);
            sp.getCooldowns().addCooldown(this, 100);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Elemento: " + element.format + element.title));
        tooltip.add(Component.literal("§7Región: " + element.format + element.region));
        tooltip.add(Component.literal("§e[Click Derecho] §7Sintoniza la brújula elemental para rastrear el " + element.title + " más cercano."));
    }
}
