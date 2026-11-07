package com.soybe.awakeningsystem.item.seasonal;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ChristmasGiftBoxItem extends Item {

    public ChristmasGiftBoxItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isChristmasActive()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("awakening_system.msg.espiritu_invernal_en_letargo_la_caja"), true);
            }
            return InteractionResultHolder.fail(held);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            held.shrink(1);

            // Celebration sound and particles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0D, player.getZ(),
                    30, 0.5D, 0.5D, 0.5D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(),
                    25, 0.4D, 0.4D, 0.4D, 0.1D);

            // 1. Give festive candies / confectionery
            int candiesCount = 1 + serverLevel.random.nextInt(3);
            for (int i = 0; i < candiesCount; i++) {
                ItemStack candy = getRandomFestiveSweet(serverLevel);
                if (!candy.isEmpty()) {
                    if (!player.getInventory().add(candy)) {
                        player.drop(candy, false);
                    }
                }
            }

            // 2. Give coins/mora
            int coinReward = 50 + serverLevel.random.nextInt(150);
            SystemData data = SystemEvents.data(serverPlayer);
            if (data != null) {
                data.addCoins(coinReward);
                serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.monedas_del_sistema_regalo_festivo", String.valueOf(coinReward)).withStyle(ChatFormatting.GOLD));
            }

            // 3. High tier loot chance
            float highTierRoll = serverLevel.random.nextFloat();
            if (highTierRoll < 0.20F) {
                ItemStack diamond = new ItemStack(Items.DIAMOND, 1 + serverLevel.random.nextInt(3));
                if (!player.getInventory().add(diamond)) player.drop(diamond, false);
            } else if (highTierRoll < 0.35F && SystemItems.PRIMOGEM != null && SystemItems.PRIMOGEM.isBound()) {
                ItemStack gem = new ItemStack(SystemItems.PRIMOGEM.value(), 1);
                if (!player.getInventory().add(gem)) player.drop(gem, false);
            }

            // 4. Rare Chance (5%) for Christmas Tree Sword
            if (serverLevel.random.nextFloat() < 0.05F && SystemItems.XMAS_TREE_SWORD != null && SystemItems.XMAS_TREE_SWORD.isBound()) {
                ItemStack sword = new ItemStack(SystemItems.XMAS_TREE_SWORD.value());
                if (!player.getInventory().add(sword)) {
                    player.drop(sword, false);
                }
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.0F);
                serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.premio_mayor_festivo_has_obtenido_la")
                        .withStyle(ChatFormatting.GOLD));
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.regalo_navideno_has_abierto_una_caja")
                        .withStyle(ChatFormatting.GREEN));
            }

            return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    private static ItemStack getRandomFestiveSweet(ServerLevel level) {
        int pick = level.random.nextInt(5);
        Item item = switch (pick) {
            case 0 -> SystemItems.GINGERBREAD_COOKIE != null && SystemItems.GINGERBREAD_COOKIE.isBound() ? SystemItems.GINGERBREAD_COOKIE.value() : Items.COOKIE;
            case 1 -> SystemItems.CANDY_CANE != null && SystemItems.CANDY_CANE.isBound() ? SystemItems.CANDY_CANE.value() : Items.SUGAR;
            case 2 -> SystemItems.HOT_CHOCOLATE != null && SystemItems.HOT_CHOCOLATE.isBound() ? SystemItems.HOT_CHOCOLATE.value() : Items.HONEY_BOTTLE;
            case 3 -> SystemItems.ALMOND_NOUGAT != null && SystemItems.ALMOND_NOUGAT.isBound() ? SystemItems.ALMOND_NOUGAT.value() : Items.PUMPKIN_PIE;
            default -> SystemItems.POLAR_PEPPERMINT != null && SystemItems.POLAR_PEPPERMINT.isBound() ? SystemItems.POLAR_PEPPERMINT.value() : Items.APPLE;
        };
        return new ItemStack(item, 1 + level.random.nextInt(2));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.msg.paquete_festivo_de_navidad"));
        tooltip.add(Component.translatable("awakening_system.msg.un_regalo_envuelto_con_lazos_brillantes"));
        tooltip.add(Component.translatable("awakening_system.msg.clic_derecho_abrir_para_recibir_dulces"));
        tooltip.add(Component.translatable("awakening_system.msg.oportunidad_rara_espada_arbol_de_navidad"));
        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isChristmasActive()) {
            tooltip.add(Component.translatable("awakening_system.msg.bloqueado_fuera_de_temporada"));
            tooltip.add(Component.translatable("awakening_system.msg.solo_activo_del_de_diciembre_al"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
