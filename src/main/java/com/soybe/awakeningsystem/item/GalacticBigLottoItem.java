package com.soybe.awakeningsystem.item;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GalacticBigLottoItem extends Item {
    public GalacticBigLottoItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            RandomSource rand = sp.getRandom();
            float roll = rand.nextFloat();

            // 20% probabilidad de rotura dramática (Curio Break) y reducir vida a 1 HP (medio corazón)
            if (roll < 0.20F) {
                stack.shrink(1);
                sp.setHealth(1.0F);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.05D);
                    sl.sendParticles(ParticleTypes.LAVA, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), ModSounds.CURIO_BREAK.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
                    sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);
                }
                sp.playNotifySound(ModSounds.CURIO_BREAK.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
                sp.displayClientMessage(Component.literal("§c§l✦ [GRAN LOTERÍA INTERGALÁCTICA] §4¡CRACK! ¡La Lotería se ha roto en mil pedazos! Tu salud cae a 1 HP..."), true);
                return InteractionResultHolder.consume(stack);
            }

            // 80% éxito: Gran recompensa
            String rewardText;
            int prizeTier = rand.nextInt(5);
            switch (prizeTier) {
                case 0 -> {
                    // Protogemas
                    SystemEvents.data(sp).addPrimogems(320);
                    AwakeningSystemMod.sync(sp);
                    rewardText = "§d+320 Protogemas";
                }
                case 1 -> {
                    // Deseo Entrelazado
                    ItemStack fates = new ItemStack(SystemItems.INTERTWINED_FATE.get(), 2);
                    if (!sp.addItem(fates)) sp.drop(fates, false);
                    rewardText = "§b2x Deseo Entrelazado";
                }
                case 2 -> {
                    // Fragmentos Cósmicos
                    int frags = 30 + rand.nextInt(31);
                    ItemStack cosmic = new ItemStack(SystemItems.COSMIC_FRAGMENT.get(), frags);
                    if (!sp.addItem(cosmic)) sp.drop(cosmic, false);
                    rewardText = "§6" + frags + "x Fragmentos Cósmicos";
                }
                case 3 -> {
                    // Ingenio del Héroe
                    ItemStack wit = new ItemStack(SystemItems.HEROS_WIT.get(), 4);
                    if (!sp.addItem(wit)) sp.drop(wit, false);
                    rewardText = "§a4x Ingenio del Héroe (+4,000 EXP)";
                }
                default -> {
                    // Diamantes y Manzana de Oro
                    ItemStack apple = new ItemStack(Items.GOLDEN_APPLE, 2);
                    ItemStack diamonds = new ItemStack(Items.DIAMOND, 5);
                    if (!sp.addItem(apple)) sp.drop(apple, false);
                    if (!sp.addItem(diamonds)) sp.drop(diamonds, false);
                    rewardText = "§b5x Diamantes + 2x Manzanas Doradas";
                }
            }

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.2D);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, sp.getX(), sp.getY() + 1.2D, sp.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.1D);
            }
            sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
            sp.displayClientMessage(Component.literal("§6§l✦ [GRAN LOTERÍA INTERGALÁCTICA] §e¡PREMIO MAYOR! §fHas ganado: " + rewardText), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§e⭐⭐⭐⭐⭐ [Curiosidad del Universo Simulado]").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Un billete de lotería de dudosa procedencia cósmica.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§6▸ Clic derecho: §fTentar a la suerte por fabulosos premios.").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("§c⚠ ADVERTENCIA: §420% de probabilidad de que se destruya y deje tu salud en 1 HP.").withStyle(ChatFormatting.RED));
    }
}
