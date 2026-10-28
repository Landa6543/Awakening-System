package com.soybe.awakeningsystem.item;

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

/**
 * Resina Efímera (Transient Resin) de Genshin Impact:
 * Obtenida comúnmente en la Tetera de Relajatetera.
 * Al usarse con clic derecho, se transforma en 60 Resinas Originales.
 */
public class TransientResinItem extends Item {

    public TransientResinItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            stack.shrink(1);

            ItemStack resin60 = new ItemStack(SystemItems.ORIGINAL_RESIN.get(), 60);
            if (!sp.getInventory().add(resin60)) {
                sp.drop(resin60, false);
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.4D, 0.5D, 0.4D, 0.1D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.2D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }

            sp.displayClientMessage(Component.translatable("item.awakening_system.transient_resin.use_message"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.transient_resin.badge").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.transient_resin.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.transient_resin.use").withStyle(ChatFormatting.YELLOW));
    }
}
