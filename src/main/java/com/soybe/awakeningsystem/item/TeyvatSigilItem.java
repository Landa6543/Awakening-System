package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemEvents;
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
 * Sigilos Elementales y Arcanos de Teyvat.
 * Pueden cambiarse por Mora en tiendas de recuerdos o canjearse directamente con click derecho.
 */
public class TeyvatSigilItem extends Item {

    public enum SigilType {
        ANEMO("anemo", "Anemo Sigil", "Mondstadt", 1600, ChatFormatting.GREEN),
        GEO("geo", "Geo Sigil", "Liyue", 1600, ChatFormatting.GOLD),
        ELECTRO("electro", "Electro Sigil", "Inazuma", 1600, ChatFormatting.LIGHT_PURPLE),
        DENDRO("dendro", "Dendro Sigil", "Sumeru", 1600, ChatFormatting.DARK_GREEN),
        HYDRO("hydro", "Hydro Sigil", "Fontaine", 1600, ChatFormatting.BLUE),
        PYRO("pyro", "Pyro Sigil", "Natlan", 1600, ChatFormatting.RED),
        CRYO("cryo", "Cryo Sigil", "Snezhnaya", 1600, ChatFormatting.AQUA),
        GUARDIAN("guardians", "Guardian Sigil", "Teyvat Ruins", 3200, ChatFormatting.AQUA),
        LIGHT_REALM("light_realm", "Light Realm Sigil", "Enkanomiya", 4500, ChatFormatting.YELLOW);

        public final String id;
        public final String title;
        public final String region;
        public final int moraValue;
        public final ChatFormatting format;

        SigilType(String id, String title, String region, int moraValue, ChatFormatting format) {
            this.id = id;
            this.title = title;
            this.region = region;
            this.moraValue = moraValue;
            this.format = format;
        }
    }

    private final SigilType type;

    public TeyvatSigilItem(SigilType type, Properties properties) {
        super(properties.rarity(type == SigilType.LIGHT_REALM || type == SigilType.GUARDIAN ? Rarity.RARE : Rarity.UNCOMMON).stacksTo(64));
        this.type = type;
    }

    public SigilType getType() {
        return type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            ServerLevel sl = (ServerLevel) level;
            var data = SystemEvents.data(sp);
            data.addCoins(type.moraValue);

            stack.shrink(1);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0, sp.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.3F);

            sp.displayClientMessage(Component.translatable("item.awakening_system.teyvat_sigil.redeem_msg", type.moraValue, type.format + type.title), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.teyvat_sigil.region", type.format + type.region));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_sigil.use", String.valueOf(type.moraValue)));
        tooltip.add(Component.translatable("item.awakening_system.teyvat_sigil.lore"));
    }
}
