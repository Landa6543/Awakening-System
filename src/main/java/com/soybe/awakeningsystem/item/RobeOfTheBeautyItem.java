package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Curio Canónico del Universo Simulado & PrimogemCraft:
 * Manto de la Belleza (Robe of The Beauty).
 * Aumenta el daño de ataque en un +16% por cada 100 Fragmentos Cósmicos en posesión.
 */
public class RobeOfTheBeautyItem extends Item {

    public RobeOfTheBeautyItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.EPIC).fireResistant());
    }

    public static int countCosmicFragments(Player player) {
        if (player == null) return 0;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && SystemItems.COSMIC_FRAGMENT.isBound() && stack.is(SystemItems.COSMIC_FRAGMENT.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static double getDamageMultiplier(Player player) {
        if (player == null) return 1.0;
        boolean hasRobe = false;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && SystemItems.ROBE_OF_THE_BEAUTY.isBound() && stack.is(SystemItems.ROBE_OF_THE_BEAUTY.get())) {
                hasRobe = true;
                break;
            }
        }
        if (!hasRobe) return 1.0;

        int fragments = countCosmicFragments(player);
        int tiers = fragments / 100;
        double bonus = Math.min(0.80, tiers * 0.16);
        return 1.0 + bonus;
    }

    public static boolean tryCosmicPreservation(ServerPlayer player, float damage) {
        int fragments = countCosmicFragments(player);
        if (fragments < 30) return false;

        int toRemove = 30;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && SystemItems.COSMIC_FRAGMENT.isBound() && stack.is(SystemItems.COSMIC_FRAGMENT.get())) {
                int take = Math.min(toRemove, stack.getCount());
                stack.shrink(take);
                toRemove -= take;
                if (toRemove <= 0) break;
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1, false, false, true));

        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.1D);
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.2D, player.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.15D);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 1.2F);
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.5F, 0.9F);
        }

        player.displayClientMessage(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_msg"), true);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.badge").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.passive_desc2"));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientTooltip.addCosmicFragmentTooltip(tooltip);
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_desc1"));
        tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.preservation_desc2"));
    }

    private static class ClientTooltip {
        static void addCosmicFragmentTooltip(List<Component> tooltip) {
            net.minecraft.world.entity.player.Player clientPlayer = net.minecraft.client.Minecraft.getInstance().player;
            if (clientPlayer != null) {
                int frags = countCosmicFragments(clientPlayer);
                int bonus = (int) (Math.min(0.80, (frags / 100) * 0.16) * 100);
                tooltip.add(Component.translatable("item.awakening_system.robe_of_the_beauty.current_bonus", bonus, frags));
            }
        }
    }
}
