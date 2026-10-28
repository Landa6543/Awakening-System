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
 * Fragmento Cósmico (Cosmic Fragment) de PrimogemCraft & Universo Simulado:
 * Moneda y material estelar de alta pureza.
 * - Sinergia con Manto de la Belleza (+16% Daño por cada 100).
 * - Clic Derecho: Repara un 25% de durabilidad del objeto en la mano secundaria (consume 16).
 * - Shift + Clic Derecho: Condensa 160 Fragmentos Cósmicos en 1 Destino Entrelazado.
 */
public class CosmicFragmentItem extends Item {

    public CosmicFragmentItem(Properties properties) {
        super(properties.stacksTo(64).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (sp.isShiftKeyDown()) {
                int total = countFragments(sp);
                if (total >= 160) {
                    removeFragments(sp, 160);
                    ItemStack fate = new ItemStack(SystemItems.INTERTWINED_FATE.get(), 1);
                    if (!sp.addItem(fate)) {
                        sp.drop(fate, false);
                    }

                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 35, 0.4D, 0.6D, 0.4D, 0.2D);
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.1D);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                    }

                    sp.displayClientMessage(Component.translatable("item.awakening_system.cosmic_fragment.synthesis_success"), true);
                    return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
                } else {
                    sp.displayClientMessage(Component.translatable("item.awakening_system.cosmic_fragment.synthesis_fail", total), true);
                    return InteractionResultHolder.fail(held);
                }
            }

            ItemStack offhand = sp.getOffhandItem();
            if (!offhand.isEmpty() && offhand.isDamageableItem() && offhand.isDamaged()) {
                int total = countFragments(sp);
                if (total >= 16) {
                    removeFragments(sp, 16);
                    int maxDur = offhand.getMaxDamage();
                    int repairAmount = Math.max(1, maxDur / 4);
                    offhand.setDamageValue(Math.max(0, offhand.getDamageValue() - repairAmount));

                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 15, 0.3D, 0.4D, 0.3D, 0.05D);
                        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
                    }

                    sp.displayClientMessage(Component.translatable("item.awakening_system.cosmic_fragment.repair_success"), true);
                    return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
                } else {
                    sp.displayClientMessage(Component.translatable("item.awakening_system.cosmic_fragment.repair_fail"), true);
                    return InteractionResultHolder.fail(held);
                }
            } else {
                sp.displayClientMessage(Component.translatable("item.awakening_system.cosmic_fragment.usage_hint"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    private static int countFragments(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && SystemItems.COSMIC_FRAGMENT.isBound() && stack.is(SystemItems.COSMIC_FRAGMENT.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeFragments(ServerPlayer player, int amount) {
        int left = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && SystemItems.COSMIC_FRAGMENT.isBound() && stack.is(SystemItems.COSMIC_FRAGMENT.get())) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
                if (left <= 0) break;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.badge").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synergy_title").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synergy_desc"));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.repair_title").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.repair_desc"));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synth_title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.awakening_system.cosmic_fragment.synth_desc"));
    }
}
