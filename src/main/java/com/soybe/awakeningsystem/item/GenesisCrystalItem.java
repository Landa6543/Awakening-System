package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
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

public class GenesisCrystalItem extends Item {
    public GenesisCrystalItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            SystemData data = SystemEvents.data(sp);
            int count = player.isShiftKeyDown() ? stack.getCount() : 1;
            int totalPrimos = count * 300;
            data.addPrimogems(totalPrimos);
            stack.shrink(count);
            AwakeningSystemMod.sync(sp);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
            }
            sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.4F);
            sp.displayClientMessage(Component.literal("§b§l✦ [CRISTAL GÉNESIS] §fCanjeados por §d+" + totalPrimos + " Protogemas §f(Total: §6" + data.primogems() + "§f)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§b⭐⭐⭐⭐⭐ [Energía Primordial del Cosmos]").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("§7Cristal de energía pura formado en el origen del firmamento.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§b▸ Clic derecho: §fCanjear por §d+300 Protogemas").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("§b▸ Shift + Clic derecho: §fCanjear todo el stack").withStyle(ChatFormatting.DARK_AQUA));
    }
}
