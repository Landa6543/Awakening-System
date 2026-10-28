package com.soybe.awakeningsystem.casino.roulette;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RouletteBetEntry(
        RouletteBetType type,
        int param,
        long amount,
        int chipTier
) {
    public static final StreamCodec<ByteBuf, RouletteBetEntry> STREAM_CODEC = StreamCodec.of(
            (buf, entry) -> {
                ByteBufCodecs.VAR_INT.encode(buf, entry.type().ordinal());
                ByteBufCodecs.VAR_INT.encode(buf, entry.param());
                ByteBufCodecs.VAR_LONG.encode(buf, entry.amount());
                ByteBufCodecs.VAR_INT.encode(buf, entry.chipTier());
            },
            buf -> new RouletteBetEntry(
                    RouletteBetType.values()[ByteBufCodecs.VAR_INT.decode(buf)],
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
            )
    );

    public long calculatePayout(int winningNumber) {
        if (type.wins(winningNumber, param)) {
            return amount + (amount * type.getPayoutMultiplier());
        }
        return 0L;
    }
}
