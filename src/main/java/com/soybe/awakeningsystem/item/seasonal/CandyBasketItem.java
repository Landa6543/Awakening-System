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

public class CandyBasketItem extends Item {

    public CandyBasketItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isHalloweenActive()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("awakening_system.msg.noche_de_brujas_en_letargo_la"), true);
            }
            return InteractionResultHolder.fail(held);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            held.shrink(1);

            // Spooky sounds & particles
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GHAST_WARN, SoundSource.PLAYERS, 0.7F, 1.3F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITCH_CELEBRATE, SoundSource.PLAYERS, 0.9F, 1.1F);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(),
                    30, 0.5D, 0.5D, 0.5D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(),
                    20, 0.4D, 0.4D, 0.4D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(),
                    25, 0.4D, 0.4D, 0.4D, 0.1D);

            // Give candies
            int count = 2 + serverLevel.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                ItemStack candy = getRandomHalloweenCandy(serverLevel);
                if (!candy.isEmpty()) {
                    if (!player.getInventory().add(candy)) {
                        player.drop(candy, false);
                    }
                }
            }

            // Give coins
            int coinReward = 40 + serverLevel.random.nextInt(120);
            SystemData data = SystemEvents.data(serverPlayer);
            if (data != null) {
                data.addCoins(coinReward);
                serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.monedas_del_sistema_cosecha_de_almas", String.valueOf(coinReward)).withStyle(ChatFormatting.GOLD));
            }

            serverPlayer.sendSystemMessage(Component.translatable("awakening_system.msg.canasta_de_dulces_has_abierto_una")
                    .withStyle(ChatFormatting.GOLD));

            return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    private static ItemStack getRandomHalloweenCandy(ServerLevel level) {
        int pick = level.random.nextInt(4);
        Item item = switch (pick) {
            case 0 -> SystemItems.SOUL_CANDY != null && SystemItems.SOUL_CANDY.isBound() ? SystemItems.SOUL_CANDY.value() : Items.SUGAR;
            case 1 -> SystemItems.MANA_GUMMY != null && SystemItems.MANA_GUMMY.isBound() ? SystemItems.MANA_GUMMY.value() : Items.SWEET_BERRIES;
            case 2 -> SystemItems.BERSERKER_CHOCOLATE != null && SystemItems.BERSERKER_CHOCOLATE.isBound() ? SystemItems.BERSERKER_CHOCOLATE.value() : Items.COCOA_BEANS;
            default -> SystemItems.HAUNTED_CANDY != null && SystemItems.HAUNTED_CANDY.isBound() ? SystemItems.HAUNTED_CANDY.value() : Items.GLOW_BERRIES;
        };
        return new ItemStack(item, 1 + level.random.nextInt(2));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("awakening_system.msg.canasta_de_dulces_de_halloween"));
        tooltip.add(Component.translatable("awakening_system.msg.canastilla_calabaza_rebosante_de_golosinas_de"));
        tooltip.add(Component.translatable("awakening_system.msg.clic_derecho_abrir_para_cosechar_dulces"));
        if (!com.soybe.awakeningsystem.seasonal.SeasonalEventManager.isHalloweenActive()) {
            tooltip.add(Component.translatable("awakening_system.msg.bloqueado_fuera_de_temporada"));
            tooltip.add(Component.translatable("awakening_system.msg.solo_activo_del_15_de_octubre"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
