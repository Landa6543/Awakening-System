package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import com.soybe.awakeningsystem.block.SlotMachineBlock;
import com.soybe.awakeningsystem.block.entity.SlotMachineBlockEntity;
import com.soybe.awakeningsystem.casino.network.ClandestineOwnerActionC2SPayload;
import com.soybe.awakeningsystem.casino.network.CloseSlotMachineC2SPayload;
import com.soybe.awakeningsystem.casino.network.OpenClandestineOwnerPayload;
import com.soybe.awakeningsystem.casino.network.SlotSpinC2SPayload;
import com.soybe.awakeningsystem.casino.network.SlotSpinResultS2CPayload;
import com.soybe.awakeningsystem.casino.slot.SlotLines;
import com.soybe.awakeningsystem.casino.slot.SlotReels;
import com.soybe.awakeningsystem.casino.slot.SlotSpinEngine;
import com.soybe.awakeningsystem.casino.slot.SlotSpinResult;
import com.soybe.awakeningsystem.casino.slot.SlotSymbol;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class SlotMachineServerHandler {

    public static void handleSpin(ServerPlayer player, SlotSpinC2SPayload payload) {
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof SlotMachineBlockEntity slotEntity)) return;
        if (slotEntity.isInUse() && !slotEntity.isUsedBy(player)) return;

        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof SlotMachineBlock smb && smb.isIllegal();

        int betChipsPerLine = Math.max(1, payload.betBase());
        int linesMode = Math.min(3, Math.max(1, payload.linesMode()));
        int lineCount = SlotLines.getLineCount(linesMode);
        int totalBetChips = betChipsPerLine * lineCount;

        if (isIllegal) {
            int availableChips = SlotMachineBlockEntity.countBlackChips(player);
            if (availableChips < totalBetChips) {
                player.sendSystemMessage(Component.translatable("awakening_system.slot.insufficient_black_chips", totalBetChips), true);
                return;
            }
            SlotMachineBlockEntity.consumeBlackChips(player, totalBetChips);
        } else {
            int availableChips = SlotMachineBlockEntity.countLegalChips(player);
            if (availableChips < totalBetChips) {
                player.sendSystemMessage(Component.translatable("awakening_system.slot.insufficient_gold_chips", totalBetChips), true);
                return;
            }
            SlotMachineBlockEntity.consumeLegalChips(player, totalBetChips);
        }

        SlotSpinEngine.SpinOutcome outcome = SlotSpinEngine.spinOutcome(betChipsPerLine, linesMode);
        SlotSpinResult result = outcome.result();
        long totalChipsWon = result.totalWin();

        boolean isJackpot = false;
        List<SlotSpinResultS2CPayload.WinEntry> wins = new ArrayList<>();

        for (var line : result.lines()) {
            if (line.win()) {
                int symOrd = line.symbol() != null ? line.symbol().ordinal() : -1;
                wins.add(new SlotSpinResultS2CPayload.WinEntry(line.lineIndex(), symOrd, line.count(), line.multiplier(), line.lineWin()));
                if (line.symbol() == SlotSymbol.SEVEN && line.count() >= 3) {
                    isJackpot = true;
                }
            }
        }

        SystemData data = SystemEvents.data(player);

        if (isIllegal) {
            if (totalChipsWon == 0) {
                slotEntity.onCustomerLostBet(totalBetChips);
            } else {
                ItemStack wonStack = new ItemStack(SystemItems.CASINO_CHIP_BLACK.get(), (int) totalChipsWon);
                if (!player.getInventory().add(wonStack)) {
                    player.drop(wonStack, false);
                }
                player.sendSystemMessage(Component.translatable("awakening_system.slot.won_black_chips", totalChipsWon), true);
            }
        } else {
            if (totalChipsWon > 0) {
                ItemStack wonStack = new ItemStack(SystemItems.CASINO_CHIP_GOLD.get(), (int) totalChipsWon);
                if (!player.getInventory().add(wonStack)) {
                    player.drop(wonStack, false);
                }
                player.sendSystemMessage(Component.translatable("awakening_system.slot.won_gold_chips", totalChipsWon), true);
            }
        }

        if (isJackpot) {
            data.addCoins(1600);
            ItemStack trophy = com.soybe.awakeningsystem.item.CasinoTrophyItem.createTrophy(
                    3, player.getName().getString(),
                    isIllegal ? "Underworld Clandestine Jackpot (777)" : "Grand Casino 777 Jackpot"
            );
            if (!player.getInventory().add(trophy)) player.drop(trophy, false);
        }

        SlotSymbol s0 = SlotReels.get(SlotReels.STRIPS[0], outcome.stop1());
        SlotSymbol s1 = SlotReels.get(SlotReels.STRIPS[1], outcome.stop2());
        SlotSymbol s2 = SlotReels.get(SlotReels.STRIPS[2], outcome.stop3());

        slotEntity.startMoraSpin(player, s0, s1, s2, totalChipsWon > 0, isJackpot, totalChipsWon);

        AwakeningSystemMod.sync(player);

        int newChipBalance = isIllegal ? SlotMachineBlockEntity.countBlackChips(player) : SlotMachineBlockEntity.countLegalChips(player);

        SlotSpinResultS2CPayload response = new SlotSpinResultS2CPayload(
                outcome.stop1(), outcome.stop2(), outcome.stop3(),
                totalChipsWon, (long) newChipBalance, linesMode, isJackpot, wins
        );
        PacketDistributor.sendToPlayer(player, response);
    }

    public static void handleOwnerAction(ServerPlayer player, ClandestineOwnerActionC2SPayload payload) {
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof SlotMachineBlockEntity slotEntity)) return;

        if (!player.isCreative() && !player.hasPermissions(2) && !slotEntity.isOwner(player)) {
            player.sendSystemMessage(Component.translatable("awakening_system.slot.owner_only"), true);
            return;
        }

        switch (payload.action()) {
            case 0 -> { // Depositar reserva
                int depositAmount = payload.amount() <= 0 ? SlotMachineBlockEntity.countBlackChips(player) : payload.amount();
                if (depositAmount <= 0) {
                    player.sendSystemMessage(Component.translatable("awakening_system.slot.no_black_chips_deposit"), true);
                    return;
                }
                boolean ok = slotEntity.depositReserve(player, depositAmount);
                if (ok) {
                    player.playNotifySound(SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.8F, 1.2F);
                    player.sendSystemMessage(Component.translatable("awakening_system.slot.deposit_success", depositAmount), true);
                } else {
                    player.sendSystemMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
                }
            }
            case 1 -> { // Retirar recaudación (ganancias de clientes)
                int collected = slotEntity.withdrawVault(player);
                if (collected > 0) {
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8F, 1.5F);
                    player.sendSystemMessage(Component.translatable("awakening_system.slot.withdraw_vault_success", collected), true);
                } else {
                    player.sendSystemMessage(Component.translatable("awakening_system.slot.no_vault_earnings"), true);
                }
            }
            case 2 -> { // Retirar reserva
                int withdrawAmount = payload.amount() <= 0 ? slotEntity.getReserveChips() : payload.amount();
                int taken = slotEntity.withdrawReserve(player, withdrawAmount);
                if (taken > 0) {
                    player.playNotifySound(SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.8F, 1.0F);
                    player.sendSystemMessage(Component.translatable("awakening_system.slot.withdraw_reserve_success", taken), true);
                } else {
                    player.sendSystemMessage(Component.translatable("awakening_system.slot.no_reserve_chips"), true);
                }
            }
        }

        PacketDistributor.sendToPlayer(player, new OpenClandestineOwnerPayload(
                pos,
                slotEntity.getOwnerName(),
                slotEntity.getReserveChips(),
                slotEntity.getCollectedVaultChips(),
                slotEntity.getTotalCustomerSpins(),
                SlotMachineBlockEntity.countBlackChips(player)
        ));
    }

    public static void handleClose(ServerPlayer player, CloseSlotMachineC2SPayload payload) {
        BlockPos pos = payload.pos();
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof SlotMachineBlockEntity slotEntity) {
            slotEntity.unlock(player);
        }
    }
}
