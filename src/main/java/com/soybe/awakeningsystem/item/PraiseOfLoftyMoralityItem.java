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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PraiseOfLoftyMoralityItem extends Item {
    public PraiseOfLoftyMoralityItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            stack.shrink(1);
            sp.addEffect(new MobEffectInstance(MobEffects.LUCK, 600, 1, false, true, true));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, false, true, true));

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
            }
            sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            sp.displayClientMessage(Component.literal("§e§l✦ [ELOGIO A LA ALTA MORAL] §f¡El cosmos bendice tus virtuosas acciones! (+Suerte II y Regeneración II)"), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§e⭐⭐⭐⭐ [Prueba de Virtud y Decoro]").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§7Un brillante distintivo otorgado a quienes realizan actos de bondad desinteresada en Belobog.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§a▸ Clic derecho: §fConsumir para recibir la bendición de la Suerte y la Salud").withStyle(ChatFormatting.GREEN));
    }
}
