package com.soybe.awakeningsystem.blackmarket;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.blackmarket.network.BlackMarketActionC2SPayload;
import com.soybe.awakeningsystem.blackmarket.network.OpenBlackMarketPayload;
import com.soybe.awakeningsystem.casino.pool.PoolTableBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Gestor del lado del servidor para las transacciones ilícitas del Mercado Negro.
 * Permite canjear divisas, adquirir máquinas de casino clandestinas y comprar consumibles prohibidos.
 */
public final class BlackMarketServerHandler {
    private BlackMarketServerHandler() {}

    public static final int TRADE_BUY_CHIP_1 = 1;
    public static final int TRADE_BUY_CHIP_5 = 2;
    public static final int TRADE_SELL_CHIP_1 = 3;
    public static final int TRADE_SELL_CHIP_5 = 4;
    public static final int TRADE_NETHERITE_FOR_CHIPS = 5;
    public static final int TRADE_GOLD_FOR_CHIPS = 6;
    public static final int TRADE_BUY_PLATINUM_CHIP_1 = 7;
    public static final int TRADE_BUY_PLATINUM_CHIP_10 = 8;
    public static final int TRADE_SELL_PLATINUM_CHIP_1 = 9;

    public static final int TRADE_ILLEGAL_SLOT = 10;
    public static final int TRADE_ILLEGAL_POOL = 11;
    public static final int TRADE_CLANDESTINE_CASHIER = 12;
    public static final int TRADE_ILLEGAL_BLACKJACK = 13;
    public static final int TRADE_ILLEGAL_ROULETTE = 14;
    public static final int TRADE_WHOLESALE_BLACK_CHIP_1 = 15;
    public static final int TRADE_WHOLESALE_BLACK_CHIP_10 = 16;
    public static final int TRADE_BUY_CLANDESTINE_KEY = 17;
    public static final int TRADE_BUY_BLACK_MARKET_HATCH = 18;

    public static final int TRADE_DARK_ELIXIR_1 = 20;
    public static final int TRADE_DARK_ELIXIR_3 = 21;
    public static final int TRADE_GOLD_HORSESHOE = 22;
    public static final int TRADE_GOLDEN_GACHA_TICKET = 23;

    public static void openMarket(ServerPlayer player) {
        if (player == null) return;
        SystemData data = SystemEvents.data(player);
        long mora = data.mora();
        int chips = PoolTableBlockEntity.countBlackChips(player);
        PacketDistributor.sendToPlayer(player, new OpenBlackMarketPayload(mora, chips));
        player.playNotifySound(SoundEvents.CHEST_OPEN, SoundSource.PLAYERS, 0.9F, 0.7F);
    }

    public static void handleAction(ServerPlayer player, BlackMarketActionC2SPayload payload) {
        handleTrade(player, payload);
    }

    public static void handleTrade(ServerPlayer player, BlackMarketActionC2SPayload payload) {
        if (player == null || payload == null) return;

        SystemData data = SystemEvents.data(player);
        int tradeId = payload.tradeId();
        boolean success = false;
        Component successMsg = null;

        switch (tradeId) {
            case TRADE_BUY_CHIP_1 -> {
                if (data.mora() >= 500_000L) {
                    data.spendMora(500_000);
                    giveItem(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.chip_buy_1", "500,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.mora", "500,000"));
                }
            }
            case TRADE_BUY_CHIP_5 -> {
                if (data.mora() >= 2_500_000L) {
                    data.spendMora(2_500_000);
                    giveItem(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 5));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.chip_buy_5", "2,500,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.mora", "2,500,000"));
                }
            }
            case TRADE_SELL_CHIP_1 -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 1)) {
                    data.addMora(1_000_000);
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.chip_sell_1", "1,000,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.no_chips"));
                }
            }
            case TRADE_SELL_CHIP_5 -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 5)) {
                    data.addMora(5_000_000);
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.chip_sell_5", "5,000,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 5));
                }
            }
            case TRADE_NETHERITE_FOR_CHIPS -> {
                if (consumeItem(player, Items.NETHERITE_INGOT, 1)) {
                    giveItem(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 2));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.netherite");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.netherite"));
                }
            }
            case TRADE_GOLD_FOR_CHIPS -> {
                if (consumeItem(player, Items.GOLD_BLOCK, 8)) {
                    giveItem(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.gold");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.gold"));
                }
            }
            case TRADE_ILLEGAL_SLOT -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 5)) {
                    giveItem(player, new ItemStack(SystemItems.ILLEGAL_SLOT_MACHINE.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.slot");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 5));
                }
            }
            case TRADE_ILLEGAL_POOL -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 8)) {
                    giveItem(player, new ItemStack(SystemItems.ILLEGAL_POOL_TABLE.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.pool");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 8));
                }
            }
            case TRADE_CLANDESTINE_CASHIER -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 3)) {
                    giveItem(player, new ItemStack(SystemItems.CLANDESTINE_CASHIER.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.cashier");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 3));
                }
            }
            case TRADE_ILLEGAL_BLACKJACK -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 6)) {
                    giveItem(player, new ItemStack(SystemItems.ILLEGAL_BLACKJACK_TABLE.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.blackjack");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 6));
                }
            }
            case TRADE_ILLEGAL_ROULETTE -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 6)) {
                    giveItem(player, new ItemStack(SystemItems.ILLEGAL_ROULETTE_TABLE.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.roulette");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 6));
                }
            }
            case TRADE_WHOLESALE_BLACK_CHIP_1 -> {
                if (data.mora() >= 500_000L) {
                    data.spendMora(500_000);
                    giveItem(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.wholesale_1", "500,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.mora", "500,000"));
                }
            }
            case TRADE_WHOLESALE_BLACK_CHIP_10 -> {
                if (data.mora() >= 5_000_000L) {
                    data.spendMora(5_000_000);
                    giveItem(player, new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), 10));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.wholesale_10", "5,000,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.mora", "5,000,000"));
                }
            }
            case TRADE_BUY_CLANDESTINE_KEY -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 1)) {
                    giveItem(player, new ItemStack(SystemItems.CLANDESTINE_VAULT_KEY.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.key_chips");
                } else if (data.mora() >= 500_000L) {
                    data.spendMora(500_000);
                    giveItem(player, new ItemStack(SystemItems.CLANDESTINE_VAULT_KEY.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.key_mora", "500,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.key", "500,000"));
                }
            }
            case TRADE_BUY_BLACK_MARKET_HATCH -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 2)) {
                    giveItem(player, new ItemStack(SystemItems.BLACK_MARKET_HATCH.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.hatch_chips");
                } else if (data.mora() >= 1_000_000L) {
                    data.spendMora(1_000_000);
                    giveItem(player, new ItemStack(SystemItems.BLACK_MARKET_HATCH.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.hatch_mora", "1,000,000");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.hatch", "1,000,000"));
                }
            }
            case TRADE_DARK_ELIXIR_1 -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 1)) {
                    giveItem(player, new ItemStack(SystemItems.DARK_STAMINA_ELIXIR.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.elixir_1");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 1));
                }
            }
            case TRADE_DARK_ELIXIR_3 -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 2)) {
                    giveItem(player, new ItemStack(SystemItems.DARK_STAMINA_ELIXIR.get(), 3));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.elixir_3");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 2));
                }
            }
            case TRADE_GOLD_HORSESHOE -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 2)) {
                    giveItem(player, new ItemStack(SystemItems.GOLD_HORSESHOE.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.horseshoe");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 2));
                }
            }
            case TRADE_GOLDEN_GACHA_TICKET -> {
                if (PoolTableBlockEntity.consumeBlackChips(player, 3)) {
                    giveItem(player, new ItemStack(SystemItems.GOLDEN_GACHA_TICKET.get(), 1));
                    success = true;
                    successMsg = Component.translatable("awakening_system.blackmarket.success.ticket");
                } else {
                    fail(player, Component.translatable("awakening_system.blackmarket.fail.chips", 3));
                }
            }
        }

        if (success) {
            AwakeningSystemMod.sync(player);
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (successMsg != null) {
                player.displayClientMessage(Component.translatable("awakening_system.blackmarket.prefix").append(successMsg), true);
            }
            long newMora = data.mora();
            int newChips = PoolTableBlockEntity.countBlackChips(player);
            PacketDistributor.sendToPlayer(player, new OpenBlackMarketPayload(newMora, newChips));
        }
    }

    private static void fail(ServerPlayer player, Component message) {
        player.displayClientMessage(Component.translatable("awakening_system.blackmarket.prefix").append(message), true);
        player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 0.9F);
    }

    public static boolean consumeItem(Player player, Item item, int amount) {
        if (player.isCreative()) return true;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(item)) {
                count += stack.getCount();
            }
        }
        if (count < amount) return false;
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(item)) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public static void giveItem(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
