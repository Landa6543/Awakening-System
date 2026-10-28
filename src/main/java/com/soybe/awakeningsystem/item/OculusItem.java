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
 * Óculos Elementales de Teyvat (Anemóculus, Geóculus, Electróculus, Dendróculus).
 * Esferas de energía pura condensada que se ofrecen a las Estatuas de los Siete para aumentar el aguante/maná.
 */
public class OculusItem extends Item {

    public enum OculusElement {
        ANEMO("Anemoculus", "Mondstadt", 0xFF55FFAA, ChatFormatting.GREEN),
        GEO("Geoculus", "Liyue", 0xFFFFD700, ChatFormatting.GOLD),
        ELECTRO("Electroculus", "Inazuma", 0xFFAA44FF, ChatFormatting.LIGHT_PURPLE),
        DENDRO("Dendroculus", "Sumeru", 0xFF55DD33, ChatFormatting.DARK_GREEN);

        public final String title;
        public final String region;
        public final int color;
        public final ChatFormatting format;

        OculusElement(String title, String region, int color, ChatFormatting format) {
            this.title = title;
            this.region = region;
            this.color = color;
            this.format = format;
        }
    }

    private final OculusElement element;

    public OculusItem(OculusElement element, Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
        this.element = element;
    }

    public OculusElement getElement() {
        return element;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            sp.giveExperiencePoints(50);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.2, sp.getZ(), 25, 0.4, 0.6, 0.4, 0.2);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.5F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.oculus.resonates", element.format + element.title), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.oculus.region", element.format + element.region));
        tooltip.add(Component.translatable("item.awakening_system.oculus.substance"));
        tooltip.add(Component.translatable("item.awakening_system.oculus.lore"));
    }
}
