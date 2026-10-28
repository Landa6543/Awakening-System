package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.network.OpenRoulettePayload;
import com.soybe.awakeningsystem.casino.network.RouletteResultS2CPayload;
import com.soybe.awakeningsystem.casino.network.RouletteSpinC2SPayload;
import com.soybe.awakeningsystem.casino.roulette.RouletteBetEntry;
import com.soybe.awakeningsystem.casino.roulette.RouletteEngine;
import com.soybe.awakeningsystem.item.CasinoChipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public final class RouletteServerHandler {
    private RouletteServerHandler() {}

    public static void openTable(ServerPlayer player, BlockPos pos) {
        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalRouletteTableBlock;
        boolean isVip = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.VipRouletteTableBlock;

        long chipBalance = CasinoChipHelper.getTotalChipValue(player, isIllegal);

        int bronze = countChips(player, CasinoChipItem.ChipTier.BRONZE);
        int silver = countChips(player, CasinoChipItem.ChipTier.SILVER);
        int gold = countChips(player, CasinoChipItem.ChipTier.GOLD);
        int platinum = countChips(player, CasinoChipItem.ChipTier.PLATINUM);
        int black = countChips(player, CasinoChipItem.ChipTier.BLACK);

        boolean isSpinning = false;
        int remainingTicks = 0;
        int totalTicks = 140;
        int winNum = -1;
        long lastPay = 0L;
        if (player.level().getBlockEntity(pos) instanceof com.soybe.awakeningsystem.block.entity.RouletteTableBlockEntity tableBe) {
            isSpinning = tableBe.isSpinning();
            remainingTicks = tableBe.getSpinTicks();
            totalTicks = tableBe.getTotalSpinDuration();
            winNum = tableBe.getWinningNumber();
            lastPay = tableBe.getLastPayout();
        }

        PacketDistributor.sendToPlayer(player, new OpenRoulettePayload(
                pos, chipBalance, bronze, silver, gold, platinum, isIllegal ? black : 0,
                isSpinning, remainingTicks, totalTicks, winNum, lastPay, isIllegal, isVip
        ));
    }

    public static void handleSpin(ServerPlayer player, RouletteSpinC2SPayload payload) {
        if (player == null || payload == null || player.level() == null) return;
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        if (payload.bets() == null || payload.bets().isEmpty()) {
            player.displayClientMessage(Component.translatable("awakening_system.casino.roulette.empty_bets"), true);
            return;
        }

        boolean isIllegal = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.IllegalRouletteTableBlock;

        SystemData data = SystemEvents.data(player);
        long requiredChipValue = 0L;
        Map<CasinoChipItem.ChipTier, Integer> requiredChips = new HashMap<>();

        for (RouletteBetEntry bet : payload.bets()) {
            if (bet.chipTier() >= 0 && bet.chipTier() < CasinoChipItem.ChipTier.values().length) {
                CasinoChipItem.ChipTier tier = CasinoChipItem.ChipTier.values()[bet.chipTier()];
                if (!isIllegal && tier == CasinoChipItem.ChipTier.BLACK) {
                    player.displayClientMessage(Component.translatable("awakening_system.casino.illegal_chip_rejected"), true);
                    return;
                }
                requiredChips.put(tier, requiredChips.getOrDefault(tier, 0) + 1);
            } else {
                requiredChipValue += bet.amount();
            }
        }

        for (Map.Entry<CasinoChipItem.ChipTier, Integer> entry : requiredChips.entrySet()) {
            if (countChips(player, entry.getKey()) < entry.getValue()) {
                player.displayClientMessage(Component.translatable("awakening_system.casino.roulette.insufficient_tier_chips", entry.getKey().getDisplayName()), true);
                return;
            }
        }

        if (requiredChipValue > 0 && !CasinoChipHelper.hasEnoughChips(player, requiredChipValue, isIllegal)) {
            player.displayClientMessage(Component.translatable("awakening_system.casino.insufficient_chips"), true);
            return;
        }

        for (Map.Entry<CasinoChipItem.ChipTier, Integer> entry : requiredChips.entrySet()) {
            consumeChips(player, entry.getKey(), entry.getValue());
        }
        if (requiredChipValue > 0) {
            CasinoChipHelper.consumeChipValue(player, requiredChipValue, isIllegal);
        }

        RouletteEngine.SpinResult result = RouletteEngine.evaluate(payload.bets());
        long finalPayout = result.totalPayout();
        boolean isVip = player.level().getBlockState(pos).getBlock() instanceof com.soybe.awakeningsystem.block.VipRouletteTableBlock;
        if (isVip && finalPayout > 0) {
            long vipBonus = (long) (finalPayout * 0.50);
            finalPayout += vipBonus;
            player.displayClientMessage(Component.translatable("awakening_system.casino.roulette.vip_bonus", String.format(java.util.Locale.US, "%,d", vipBonus), String.format(java.util.Locale.US, "%,d", finalPayout)), true);
        }

        if (player.level().getBlockEntity(payload.pos()) instanceof com.soybe.awakeningsystem.block.entity.RouletteTableBlockEntity tableBe) {
            tableBe.startSpin(result.winningNumber(), player, finalPayout, result.totalBet());
        }

        if (finalPayout > 0) {
            CasinoChipHelper.giveChips(player, finalPayout, isIllegal);
            data.addCasinoWinnings(finalPayout);
            if (player.level() instanceof ServerLevel sl && finalPayout >= 50000L) {
                CasinoTrophyManager.tryClaimTrophy(sl, player, 3, "Gran Jackpot de Ruleta (" + String.format(java.util.Locale.US, "%,d", finalPayout) + " Fichas)");
            }
        }

        com.soybe.awakeningsystem.AwakeningSystemMod.sync(player);

        long remainingChips = CasinoChipHelper.getTotalChipValue(player, isIllegal);
        PacketDistributor.sendToPlayer(player, new RouletteResultS2CPayload(
                payload.pos(),
                result.winningNumber(),
                result.totalBet(),
                finalPayout,
                remainingChips
        ));
    }

    private static void spawnWinParticles(ServerPlayer player, BlockPos pos, boolean bigWin) {
        if (player.level() instanceof ServerLevel sl) {
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 1.0D;
            double z = pos.getZ() + 0.5D;
            int count = bigWin ? 40 : 18;
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, count, 0.5D, 0.4D, 0.5D, 0.1D);
            sl.sendParticles(ParticleTypes.WAX_ON, x, y + 0.2D, z, count / 2, 0.4D, 0.3D, 0.4D, 0.05D);
            if (bigWin) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y + 0.5D, z, 25, 0.4D, 0.4D, 0.4D, 0.15D);
            }
        }
    }

    private static int countChips(ServerPlayer player, CasinoChipItem.ChipTier tier) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof CasinoChipItem chip && chip.getTier() == tier) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeChips(ServerPlayer player, CasinoChipItem.ChipTier tier, int countToConsume) {
        int remaining = countToConsume;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (remaining <= 0) break;
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof CasinoChipItem chip && chip.getTier() == tier) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
    }
}
