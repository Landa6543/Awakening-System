package com.soybe.awakeningsystem.casino;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemData;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.block.entity.SlotMachineBlockEntity;
import com.soybe.awakeningsystem.casino.network.CloseSlotMachineC2SPayload;
import com.soybe.awakeningsystem.casino.network.SlotSpinC2SPayload;
import com.soybe.awakeningsystem.casino.network.SlotSpinResultS2CPayload;
import com.soybe.awakeningsystem.casino.slot.SlotLines;
import com.soybe.awakeningsystem.casino.slot.SlotSpinEngine;
import com.soybe.awakeningsystem.casino.slot.SlotSpinResult;
import com.soybe.awakeningsystem.casino.slot.SlotSymbol;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

        int betBase = Math.max(100, payload.betBase());
        int linesMode = Math.min(3, Math.max(1, payload.linesMode()));
        int lineCount = SlotLines.getLineCount(linesMode);
        long totalBet = (long) betBase * lineCount;

        SystemData data = SystemEvents.data(player);
        if (data.mora() < totalBet) {
            player.sendSystemMessage(Component.literal("§cSaldo insuficiente de Mora para realizar esta tirada."), true);
            return;
        }

        // Descontar la apuesta de Mora
        data.spendMora((int) totalBet);

        // Generar giro
        SlotSpinEngine.SpinOutcome outcome = SlotSpinEngine.spinOutcome(betBase, linesMode);
        SlotSpinResult result = outcome.result();
        long totalWin = result.totalWin();

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

        // Acreditar ganancias en Mora
        if (totalWin > 0) {
            data.addMora((int) totalWin);
        }

        // Si es JACKPOT de 777: Premio especial adicional de 1,600 Protogemas + anuncio
        if (isJackpot) {
            data.addCoins(1600); // 1600 Protogemas
            player.server.getPlayerList().broadcastSystemMessage(
                    Component.literal("§6§l✦ ¡JACKPOT CELESTIAL! §eEl jugador §f" + player.getName().getString() +
                            " §eha obtenido §6TRIPLE 777 §een la Tragamonedas Celestial ganando §6" +
                            String.format("%,d", totalWin) + " Mora §ey §d+1,600 Protogemas§e!"), false);

            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, 60, 0.8D, 1.0D, 0.8D, 0.15D);
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 50, 0.5D, 0.5D, 0.5D, 0.2D);
            }
        }

        AwakeningSystemMod.sync(player);

        // Responder al cliente
        SlotSpinResultS2CPayload response = new SlotSpinResultS2CPayload(
                outcome.stop1(), outcome.stop2(), outcome.stop3(),
                totalWin, data.mora(), linesMode, isJackpot, wins
        );
        PacketDistributor.sendToPlayer(player, response);
    }

    public static void handleClose(ServerPlayer player, CloseSlotMachineC2SPayload payload) {
        BlockPos pos = payload.pos();
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof SlotMachineBlockEntity slotEntity) {
            slotEntity.unlock(player);
        }
    }
}
