package com.soybe.awakeningsystem.casino.pool;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.casino.CasinoChipHelper;
import com.soybe.awakeningsystem.casino.pool.network.OpenPoolTablePayload;
import com.soybe.awakeningsystem.casino.pool.network.PoolTableActionC2SPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PoolTableServerHandler {
    private PoolTableServerHandler() {}

    public static void openTable(ServerPlayer player, BlockPos masterPos) {
        if (player == null || masterPos == null || player.level() == null) return;
        if (player.level().getBlockEntity(masterPos) instanceof PoolTableBlockEntity table) {
            if (table.getPlayer1Name().equals("Jugador 1")) {
                table.setPlayer1(player);
            } else if (table.getGameMode() == PoolTableBlockEntity.MODE_PVP 
                    && table.getGameStatus() == PoolTableBlockEntity.STATUS_WAITING_P2 
                    && !table.isP1(player)) {
                int bet = table.getBetAmount();
                if (bet > 0) {
                    if (table.isIllegal()) {
                        if (PoolTableBlockEntity.consumeBlackChips(player, bet)) {
                            table.addBetToPot(bet);
                            table.setPlayer2(player);
                        } else {
                            player.displayClientMessage(Component.translatable("awakening_system.casino.pool.insufficient_black_chips", bet), true);
                        }
                    } else {
                        if (CasinoChipHelper.hasEnoughChips(player, bet, false) && CasinoChipHelper.consumeChipValue(player, bet, false)) {
                            table.addBetToPot(bet);
                            table.setPlayer2(player);
                        } else {
                            player.displayClientMessage(Component.translatable("awakening_system.casino.pool.insufficient_chips", bet), true);
                        }
                    }
                } else {
                    table.setPlayer2(player);
                }
            }

            Direction facing = table.getBlockState().hasProperty(PoolTableBlock.FACING)
                    ? table.getBlockState().getValue(PoolTableBlock.FACING)
                    : Direction.NORTH;

            boolean isIllegal = table.isIllegal();
            PacketDistributor.sendToPlayer(player, new OpenPoolTablePayload(masterPos, facing, isIllegal));
            table.broadcastState();
        }
    }

    public static void handleAction(ServerPlayer player, PoolTableActionC2SPayload payload) {
        if (player == null || payload == null || player.level() == null) return;
        BlockPos pos = payload.tablePos();
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;
        if (player.level().getBlockEntity(pos) instanceof PoolTableBlockEntity table) {
            switch (payload.actionType()) {
                case PoolTableActionC2SPayload.ACTION_SHOOT -> {
                    if (table.isCurrentPlayer(player)) {
                        table.shoot(payload.aimAngle(), payload.power());
                    }
                }
                case PoolTableActionC2SPayload.ACTION_AIM -> {
                    if (table.isCurrentPlayer(player)) {
                        table.updateAim(payload.aimAngle(), payload.power());
                    }
                }
                case PoolTableActionC2SPayload.ACTION_RESET_GAME -> {
                    int mode = payload.param1();
                    int bet = payload.param2();
                    table.resetGame(mode, bet, player);
                }
                case PoolTableActionC2SPayload.ACTION_JOIN_P2 -> {
                    if (!table.isP1(player)) {
                        int bet = table.getBetAmount();
                        if (bet > 0) {
                            if (table.isIllegal()) {
                                if (!PoolTableBlockEntity.consumeBlackChips(player, bet)) {
                                    player.displayClientMessage(Component.translatable("awakening_system.casino.pool.insufficient_black_chips", bet), true);
                                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                                    return;
                                }
                                table.addBetToPot(bet);
                            } else {
                                if (!CasinoChipHelper.hasEnoughChips(player, bet, false) || !CasinoChipHelper.consumeChipValue(player, bet, false)) {
                                    player.displayClientMessage(Component.translatable("awakening_system.casino.pool.insufficient_chips", bet), true);
                                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.0F);
                                    return;
                                }
                                table.addBetToPot(bet);
                            }
                        }
                        table.setPlayer2(player);
                        table.broadcastState();
                    }
                }
                case PoolTableActionC2SPayload.ACTION_LEAVE -> {
                }
            }
        }
    }
}
