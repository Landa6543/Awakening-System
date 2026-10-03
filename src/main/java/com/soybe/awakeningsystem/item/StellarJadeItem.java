package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
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

public class StellarJadeItem extends Item {
    public StellarJadeItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int totalPrimos = count * 160;
            data.addPrimogems(totalPrimos);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.05D);
            }
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.6F);
            sp.displayClientMessage(Component.literal("§b§l✦ [JADE ESTELAR] §fHas depositado §b+" + totalPrimos + " Fondos Estelares §f(Total: §6" + data.primogems() + "§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§b⭐⭐⭐⭐ [Moneda Interestelar de los Raíles]").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§7Polvo estelar coagulado que centellea en la inmensidad del cosmos.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§b▸ Clic derecho: §fDepositar §e160 Jades §fal monedero").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§b▸ Shift + Clic derecho: §fDepositar todo el stack").withStyle(ChatFormatting.GOLD));
    }
}
