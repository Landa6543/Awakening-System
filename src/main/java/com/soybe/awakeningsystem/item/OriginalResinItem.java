package com.soybe.awakeningsystem.item;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Resina Original (Original Resin) de Genshin Impact:
 * Flujo de energía vital primordial de las Líneas Ley de Teyvat.
 * Al consumirse, restaura Energía/Maná y saciedad.
 */
public class OriginalResinItem extends Item {

    public OriginalResinItem(Properties properties) {
        super(properties.stacksTo(64).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            stack.shrink(1);

            SystemData data = SystemEvents.data(sp);
            data.restoreEnergy(20);
            sp.getFoodData().eat(4, 8.0F);
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, true, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.GLOW, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.3D, 0.4D, 0.3D, 0.05D);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.2F, 1.4F);
            }

            sp.displayClientMessage(Component.translatable("item.awakening_system.original_resin.use_message"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.original_resin.badge").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.original_resin.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.awakening_system.original_resin.use").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.original_resin.craft").withStyle(ChatFormatting.AQUA));
    }
}
