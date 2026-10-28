package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.ModSounds;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.casino.network.CasinoExchangeC2SPayload;
import com.soybe.awakeningsystem.casino.network.OpenCasinoCashierPayload;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.Locale;

public class CasinoCashierServerHandler {
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private static final Item[] CHIP_ITEMS = {
            SystemItems.CASINO_CHIP_BRONZE.get(),
            SystemItems.CASINO_CHIP_SILVER.get(),
            SystemItems.CASINO_CHIP_GOLD.get(),
            SystemItems.CASINO_CHIP_PLATINUM.get()
    };

    private static final int[] CHIP_VALUES = { 100, 1000, 10000, 100000 };

    public static void handleExchange(ServerPlayer player, CasinoExchangeC2SPayload payload) {
        int action = payload.action();
        int tierIndex = payload.tierIndex();
        if (tierIndex < 0 || tierIndex >= CHIP_ITEMS.length) {
            player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.invalid_tier"), true);
            return;
        }
        int reqAmount = payload.amount();

        Item chipItem = CHIP_ITEMS[tierIndex];
        int chipValue = CHIP_VALUES[tierIndex];
        Component chipName = Component.translatable(chipItem.getDescriptionId());

        SystemData data = SystemEvents.data(player);

        if (action == 0) {
            int buyAmount = Math.max(1, Math.min(64, reqAmount));
            long totalCost = (long) chipValue * buyAmount;

            if (data.mora() < totalCost) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.insufficient_mora", buyAmount, chipName), true);
                return;
            }

            data.spendMora((int) totalCost);

            ItemStack chipStack = new ItemStack(chipItem, buyAmount);
            if (!player.getInventory().add(chipStack)) {
                player.drop(chipStack, false);
            }

            player.playNotifySound(ModSounds.CASINO_INSERT_COIN.get(), SoundSource.PLAYERS, 1.0F, 1.1F);
            AwakeningSystemMod.sync(player);
            PacketDistributor.sendToPlayer(player, new OpenCasinoCashierPayload(data.mora()));

            player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.bought", buyAmount, chipName, NF.format(totalCost)), true);

        } else if (action == 1) {
            int countInInventory = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(chipItem)) {
                    countInInventory += stack.getCount();
                }
            }

            if (countInInventory <= 0) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.no_chips", chipName), true);
                return;
            }

            int toSell = reqAmount <= 0 ? countInInventory : Math.min(reqAmount, countInInventory);

            int remainingToRemove = toSell;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(chipItem)) {
                    int take = Math.min(remainingToRemove, stack.getCount());
                    stack.shrink(take);
                    remainingToRemove -= take;
                    if (remainingToRemove <= 0) break;
                }
            }

            long totalGain = (long) chipValue * toSell;
            data.addMora((int) totalGain);

            player.playNotifySound(ModSounds.CASINO_INSERT_COIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            AwakeningSystemMod.sync(player);
            PacketDistributor.sendToPlayer(player, new OpenCasinoCashierPayload(data.mora()));

            player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.sold", toSell, chipName, NF.format(totalGain)), true);
        }
    }

    public static void handleClandestineExchange(ServerPlayer player, com.soybe.awakeningsystem.casino.network.ClandestineExchangeC2SPayload payload) {
        int action = payload.action();
        int reqAmount = payload.amount();
        net.minecraft.core.BlockPos pos = payload.pos();
        Item chipItem = SystemItems.CASINO_CHIP_BLACK.get();
        int chipValue = 1000000;

        SystemData data = SystemEvents.data(player);
        com.soybe.awakeningsystem.block.entity.ClandestineCashierBlockEntity cashierBe = null;
        if (pos != null && !pos.equals(net.minecraft.core.BlockPos.ZERO)) {
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D) {
                net.minecraft.world.level.block.entity.BlockEntity be = player.level().getBlockEntity(pos);
                if (be instanceof com.soybe.awakeningsystem.block.entity.ClandestineCashierBlockEntity cbe) {
                    cashierBe = cbe;
                }
            }
        }

        boolean isOwner = cashierBe != null && cashierBe.isOwner(player);
        int currentStock = cashierBe != null ? cashierBe.getStockChips() : -1;

        if (action == 0) { // Comprar fichas
            int buyAmount = Math.max(1, Math.min(64, reqAmount));

            if (cashierBe != null) {
                if (cashierBe.getStockChips() <= 0) {
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.clandestine_out_of_stock"), true);
                    return;
                }
                buyAmount = Math.min(buyAmount, cashierBe.getStockChips());
            }

            long totalCost = (long) chipValue * buyAmount;

            if (data.mora() < totalCost) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.clandestine_insufficient_mora", NF.format(totalCost), buyAmount), true);
                return;
            }

            data.spendMora((int) totalCost);

            ItemStack chipStack = new ItemStack(chipItem, buyAmount);
            if (!player.getInventory().add(chipStack)) {
                player.drop(chipStack, false);
            }

            if (cashierBe != null) {
                cashierBe.setStockChips(cashierBe.getStockChips() - buyAmount);
                cashierBe.addCollectedMora(totalCost);
                cashierBe.addTotalChipsSold(buyAmount);
                cashierBe.setChanged();
                currentStock = cashierBe.getStockChips();
            }

            player.playNotifySound(ModSounds.CASINO_INSERT_COIN.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
            AwakeningSystemMod.sync(player);
            PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.casino.network.OpenClandestineCashierPayload(pos, data.mora(), currentStock, isOwner));

            player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.clandestine_bought", buyAmount, NF.format(totalCost)), true);

        } else if (action == 1) { // Canjear / vender fichas
            int countInInventory = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(chipItem)) {
                    countInInventory += stack.getCount();
                }
            }

            if (countInInventory <= 0) {
                player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.clandestine_no_chips"), true);
                return;
            }

            int toSell = reqAmount <= 0 ? countInInventory : Math.min(reqAmount, countInInventory);

            int remainingToRemove = toSell;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(chipItem)) {
                    int take = Math.min(remainingToRemove, stack.getCount());
                    stack.shrink(take);
                    remainingToRemove -= take;
                    if (remainingToRemove <= 0) break;
                }
            }

            long totalGain = (long) chipValue * toSell;
            data.addMora((int) totalGain);

            if (cashierBe != null) {
                // Las fichas vendidas por los clientes ingresan al stock del cambiador
                cashierBe.setStockChips(cashierBe.getStockChips() + toSell);
                cashierBe.setChanged();
                currentStock = cashierBe.getStockChips();
            }

            player.playNotifySound(ModSounds.CASINO_INSERT_COIN.get(), SoundSource.PLAYERS, 1.0F, 0.9F);
            AwakeningSystemMod.sync(player);
            PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.casino.network.OpenClandestineCashierPayload(pos, data.mora(), currentStock, isOwner));

            player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.clandestine_sold", toSell, NF.format(totalGain)), true);
        }
    }

    public static void handleClandestineCashierOwnerAction(ServerPlayer player, com.soybe.awakeningsystem.casino.network.ClandestineCashierOwnerActionC2SPayload payload) {
        net.minecraft.core.BlockPos pos = payload.pos();
        if (pos == null || pos.equals(net.minecraft.core.BlockPos.ZERO)) return;
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        net.minecraft.world.level.block.entity.BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof com.soybe.awakeningsystem.block.entity.ClandestineCashierBlockEntity cashierBe)) return;

        if (!player.isCreative() && !player.hasPermissions(2) && !cashierBe.isOwner(player)) {
            player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_only"), true);
            return;
        }

        switch (payload.action()) {
            case 0 -> { // Depositar stock de fichas
                int depositAmount = payload.amount() <= 0 ? com.soybe.awakeningsystem.block.entity.ClandestineCashierBlockEntity.countBlackChips(player) : payload.amount();
                int deposited = cashierBe.depositStock(player, depositAmount);
                if (deposited > 0) {
                    player.playNotifySound(net.minecraft.sounds.SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.8F, 1.2F);
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_deposited", deposited), true);
                } else {
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_no_chips"), true);
                }
            }
            case 1 -> { // Retirar stock de fichas
                int withdrawAmount = payload.amount() <= 0 ? cashierBe.getStockChips() : payload.amount();
                int taken = cashierBe.withdrawStock(player, withdrawAmount);
                if (taken > 0) {
                    player.playNotifySound(net.minecraft.sounds.SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.8F, 1.0F);
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_withdrawn", taken), true);
                } else {
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_no_stock"), true);
                }
            }
            case 2 -> { // Retirar mora recaudada
                long mora = cashierBe.withdrawCollectedMora(player);
                if (mora > 0) {
                    player.playNotifySound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8F, 1.5F);
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_mora_withdrawn", NF.format(mora)), true);
                } else {
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.cashier.owner_no_mora"), true);
                }
            }
            case 3 -> { // Abrir/refrescar pantalla de dueño
                // Deja caer a la respuesta de abajo
            }
            case 4 -> { // Cambiar a vista de cliente
                PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.casino.network.OpenClandestineCashierPayload(
                        pos, SystemEvents.data(player).mora(), cashierBe.getStockChips(), true
                ));
                return;
            }
        }

        PacketDistributor.sendToPlayer(player, new com.soybe.awakeningsystem.casino.network.OpenClandestineCashierOwnerPayload(
                pos,
                cashierBe.getOwnerName(),
                cashierBe.getStockChips(),
                cashierBe.getCollectedMora(),
                cashierBe.getTotalChipsSold(),
                com.soybe.awakeningsystem.block.entity.ClandestineCashierBlockEntity.countBlackChips(player),
                SystemEvents.data(player).mora()
        ));
    }
}
