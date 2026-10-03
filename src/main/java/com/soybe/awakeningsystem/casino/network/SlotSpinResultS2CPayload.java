package com.soybe.awakeningsystem.casino.network;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.casino.slot.SlotLineResult;
import com.soybe.awakeningsystem.casino.slot.SlotSymbol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SlotSpinResultS2CPayload(
        int stop1,
        int stop2,
        int stop3,
        long totalMoraWon,
        long newMoraBalance,
        int linesMode,
        boolean isJackpot,
        List<WinEntry> wins
) implements CustomPacketPayload {
    public static final Type<SlotSpinResultS2CPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "slot_spin_result_s2c"));

    public record WinEntry(int lineIndex, int symbolOrdinal, int count, int multiplier, long lineWin) {
        public SlotLineResult toLineResult() {
            SlotSymbol sym = symbolOrdinal >= 0 && symbolOrdinal < SlotSymbol.values().length ? SlotSymbol.values()[symbolOrdinal] : null;
            return new SlotLineResult(lineWin > 0, sym, count, multiplier, lineWin, lineIndex);
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotSpinResultS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.stop1);
                buf.writeVarInt(p.stop2);
                buf.writeVarInt(p.stop3);
                buf.writeVarLong(p.totalMoraWon);
                buf.writeVarLong(p.newMoraBalance);
                buf.writeVarInt(p.linesMode);
                buf.writeBoolean(p.isJackpot);

                buf.writeVarInt(p.wins.size());
                for (WinEntry w : p.wins) {
                    buf.writeVarInt(w.lineIndex);
                    buf.writeVarInt(w.symbolOrdinal);
                    buf.writeVarInt(w.count);
                    buf.writeVarInt(w.multiplier);
                    buf.writeVarLong(w.lineWin);
                }
            },
            buf -> {
                int s1 = buf.readVarInt();
                int s2 = buf.readVarInt();
                int s3 = buf.readVarInt();
                long won = buf.readVarLong();
                long bal = buf.readVarLong();
                int mode = buf.readVarInt();
                boolean jackpot = buf.readBoolean();

                int winCount = buf.readVarInt();
                List<WinEntry> wins = new ArrayList<>(winCount);
                for (int i = 0; i < winCount; i++) {
                    wins.add(new WinEntry(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong()));
                }

                return new SlotSpinResultS2CPayload(s1, s2, s3, won, bal, mode, jackpot, wins);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
